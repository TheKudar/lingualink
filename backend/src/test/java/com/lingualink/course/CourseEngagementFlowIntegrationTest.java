package com.lingualink.course;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingualink.course.entity.Course;
import com.lingualink.course.entity.CourseStatus;
import com.lingualink.course.repository.CourseRepository;
import com.lingualink.course.repository.CourseReviewRepository;
import com.lingualink.course.repository.EnrollmentRepository;
import com.lingualink.course.repository.LessonProgressRepository;
import com.lingualink.course.repository.LessonRepository;
import com.lingualink.course.repository.ModuleRepository;
import com.lingualink.user.entity.User;
import com.lingualink.user.entity.UserRole;
import com.lingualink.user.entity.UserStatus;
import com.lingualink.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CourseEngagementFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseReviewRepository courseReviewRepository;

    @Autowired
    private LessonProgressRepository lessonProgressRepository;

    @BeforeEach
    void setUp() {
        lessonProgressRepository.deleteAll();
        courseReviewRepository.deleteAll();
        enrollmentRepository.deleteAll();
        lessonRepository.deleteAll();
        moduleRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void studentCanEnrollCompleteLessonTrackProgressAndReviewCourse() throws Exception {
        User admin = userRepository.save(User.builder()
                .email("admin@example.com")
                .username("admin.one")
                .firstName("Admin")
                .lastName("One")
                .password(passwordEncoder.encode("secret123"))
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build());

        String creatorToken = registerAndExtractToken("""
                {
                  "email": "creator@example.com",
                  "username": "creator.one",
                  "firstName": "Creator",
                  "lastName": "One",
                  "password": "secret123",
                  "role": "CREATOR"
                }
                """);
        String studentToken = registerAndExtractToken("""
                {
                  "email": "student@example.com",
                  "username": "student.one",
                  "firstName": "Student",
                  "lastName": "One",
                  "password": "secret123"
                }
                """);
        String adminToken = loginAndExtractToken("admin@example.com", "secret123");

        Long courseId = extractId(mockMvc.perform(post("/api/courses")
                        .header("Authorization", "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "English for A1",
                                  "description": "Starter course",
                                  "language": "ENGLISH",
                                  "level": "A1",
                                  "price": 99.99
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("English for A1"))
                .andReturn()
                .getResponse()
                .getContentAsString());

        Long moduleId = extractId(mockMvc.perform(post("/api/courses/{courseId}/modules", courseId)
                        .header("Authorization", "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Module 1",
                                  "description": "Basics",
                                  "orderIndex": 0
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString());

        Long lessonId = extractId(mockMvc.perform(post("/api/courses/{courseId}/modules/{moduleId}/lessons", courseId, moduleId)
                        .header("Authorization", "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Lesson 1",
                                  "content": "<p>Hello</p>",
                                  "orderIndex": 0
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString());

        mockMvc.perform(put("/api/courses/{courseId}", courseId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "PUBLISHED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        mockMvc.perform(get("/api/courses/{courseId}/modules", courseId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/courses/{courseId}/modules/{moduleId}/lessons/{lessonId}", courseId, moduleId, lessonId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(post("/api/courses/{courseId}/enroll", courseId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.courseId").value(courseId))
                .andExpect(jsonPath("$.enrollmentStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.progressPercentage").value(0));

        mockMvc.perform(get("/api/courses/{courseId}/modules", courseId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(moduleId))
                .andExpect(jsonPath("$[0].lessons[0].id").value(lessonId));

        mockMvc.perform(get("/api/courses/{courseId}/modules/{moduleId}/lessons/{lessonId}", courseId, moduleId, lessonId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(lessonId))
                .andExpect(jsonPath("$.title").value("Lesson 1"));

        mockMvc.perform(get("/api/courses/my-enrollments")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].courseId").value(courseId))
                .andExpect(jsonPath("$.content[0].totalLessons").value(1))
                .andExpect(jsonPath("$.content[0].completedLessons").value(0));

        mockMvc.perform(get("/api/courses/{courseId}/progress", courseId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseId").value(courseId))
                .andExpect(jsonPath("$.progressPercentage").value(0))
                .andExpect(jsonPath("$.completedLessons").value(0))
                .andExpect(jsonPath("$.totalLessons").value(1));

        mockMvc.perform(post("/api/courses/{courseId}/modules/{moduleId}/lessons/{lessonId}/complete", courseId, moduleId, lessonId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lessonId").value(lessonId))
                .andExpect(jsonPath("$.completed").value(true))
                .andExpect(jsonPath("$.progressPercentage").value(100));

        mockMvc.perform(get("/api/courses/{courseId}/progress", courseId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enrollmentStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.completedLessons").value(1))
                .andExpect(jsonPath("$.progressPercentage").value(100));

        mockMvc.perform(post("/api/courses/{courseId}/reviews", courseId)
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rating": 5,
                                  "comment": "Great course"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.courseId").value(courseId))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.studentUsername").value("student.one"));

        mockMvc.perform(get("/api/courses/{courseId}/reviews", courseId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].courseId").value(courseId))
                .andExpect(jsonPath("$.content[0].rating").value(5));

        Course course = courseRepository.findById(courseId).orElseThrow();
        assertThat(course.getStatus()).isEqualTo(CourseStatus.PUBLISHED);
        assertThat(course.getTotalStudents()).isEqualTo(1);
        assertThat(course.getReviewsCount()).isEqualTo(1);
        assertThat(course.getRating()).isEqualTo(5.0);
        assertThat(course.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(99.99));
        assertThat(admin.getRole()).isEqualTo(UserRole.ADMIN);
    }

    private String registerAndExtractToken(String requestBody) throws Exception {
        String responseBody = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode responseJson = objectMapper.readTree(responseBody);
        return responseJson.path("data").path("accessToken").asText();
    }

    private String loginAndExtractToken(String email, String password) throws Exception {
        String responseBody = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode responseJson = objectMapper.readTree(responseBody);
        return responseJson.path("data").path("accessToken").asText();
    }

    private Long extractId(String responseBody) throws Exception {
        return objectMapper.readTree(responseBody).path("id").asLong();
    }
}
