package com.lingualink.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.hamcrest.Matchers.is;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthAndUserFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void registerCreatesUserAndAllowsFetchingCurrentProfile() throws Exception {
        String requestBody = """
                {
                  "email": "student@example.com",
                  "username": "student.one",
                  "firstName": "Student",
                  "lastName": "One",
                  "password": "secret123"
                }
                """;

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
        String accessToken = responseJson.path("data").path("accessToken").asText();

        User savedUser = userRepository.findByEmailIgnoreCase("student@example.com")
                .orElseThrow();

        assertThat(savedUser.getUsername()).isEqualTo("student.one");
        assertThat(savedUser.getFirstName()).isEqualTo("Student");
        assertThat(savedUser.getLastName()).isEqualTo("One");
        assertThat(passwordEncoder.matches("secret123", savedUser.getPassword())).isTrue();

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("student@example.com"))
                .andExpect(jsonPath("$.username").value("student.one"))
                .andExpect(jsonPath("$.firstName").value("Student"))
                .andExpect(jsonPath("$.lastName").value("One"))
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void registerAllowsCreatorRole() throws Exception {
        String requestBody = """
                {
                  "email": "creator@example.com",
                  "username": "creator.one",
                  "firstName": "Creator",
                  "lastName": "One",
                  "password": "secret123",
                  "role": "CREATOR"
                }
                """;

        String accessToken = registerAndExtractToken(requestBody);

        User savedUser = userRepository.findByEmailIgnoreCase("creator@example.com")
                .orElseThrow();

        assertThat(savedUser.getRole()).isEqualTo(UserRole.CREATOR);

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("CREATOR"));
    }

    @Test
    void registerRejectsAdminRole() throws Exception {
        String requestBody = """
                {
                  "email": "admin@example.com",
                  "username": "admin.one",
                  "firstName": "Admin",
                  "lastName": "One",
                  "password": "secret123",
                  "role": "ADMIN"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Admin registration is not allowed"));
    }

    @Test
    void adminCanUpdateUserRoleAndStatus() throws Exception {
        User admin = userRepository.save(User.builder()
                .email("admin@example.com")
                .username("admin.one")
                .firstName("Admin")
                .lastName("One")
                .password(passwordEncoder.encode("secret123"))
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build());

        User student = userRepository.save(User.builder()
                .email("student2@example.com")
                .username("student.two")
                .firstName("Student")
                .lastName("Two")
                .password(passwordEncoder.encode("secret123"))
                .role(UserRole.STUDENT)
                .status(UserStatus.ACTIVE)
                .build());

        String adminToken = loginAndExtractToken("admin@example.com", "secret123");

        mockMvc.perform(patch("/api/users/{id}/management", student.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "role": "CREATOR",
                                  "status": "BLOCKED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role", is("CREATOR")))
                .andExpect(jsonPath("$.status", is("BLOCKED")));

        User updatedUser = userRepository.findById(student.getId()).orElseThrow();
        assertThat(updatedUser.getRole()).isEqualTo(UserRole.CREATOR);
        assertThat(updatedUser.getStatus()).isEqualTo(UserStatus.BLOCKED);
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
}
