package com.lingualink.course.service;

import com.lingualink.common.exception.AppException;
import com.lingualink.course.dto.CourseCreateRequest;
import com.lingualink.course.dto.CourseResponse;
import com.lingualink.course.dto.CourseSummaryResponse;
import com.lingualink.course.dto.CourseUpdateRequest;
import com.lingualink.course.entity.Course;
import com.lingualink.course.entity.CourseLanguage;
import com.lingualink.course.entity.CourseLevel;
import com.lingualink.course.entity.CourseStatus;
import com.lingualink.course.mapper.CourseMapper;
import com.lingualink.course.repository.CourseRepository;
import com.lingualink.user.entity.User;
import com.lingualink.user.entity.UserRole;
import com.lingualink.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseMapper courseMapper;

    @Transactional
    public CourseResponse createCourse(CourseCreateRequest request, Long creatorId) {
        log.info("Creating course with title: {} by creator: {}", request.title(), creatorId);

        // Проверяем, существует ли пользователь
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new AppException("User not found with id: " + creatorId));

        // Проверяем, что у пользователя роль CREATOR или ADMIN
        if (creator.getRole() != UserRole.CREATOR && creator.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only creators can create courses");
        }

        // Проверяем, нет ли уже курса с таким названием у этого создателя
        if (courseRepository.existsByTitleAndCreatorId(request.title(), creatorId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Course with this title already exists for this creator");
        }

        Course course = Course.builder()
                .title(request.title())
                .description(request.description())
                .language(request.language())
                .level(request.level())
                .creatorId(creatorId)
                .price(request.price() != null ? request.price() : BigDecimal.ZERO)
                .coverImageUrl(request.coverImageUrl())
                .status(CourseStatus.DRAFT)
                .rejectionReason(null)
                .rating(0.0)
                .reviewsCount(0)
                .totalStudents(0)
                .build();

        Course savedCourse = courseRepository.save(course);
        log.info("Course created with id: {}", savedCourse.getId());

        return enrichWithCreatorInfo(savedCourse);
    }

    public CourseResponse getCourseById(Long id, Long currentUserId, boolean isAdmin) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new AppException("Course not found with id: " + id));

        // Проверяем права доступа
        if (!canAccessCourse(course, currentUserId, isAdmin)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You don't have permission to access this course");
        }

        return enrichWithCreatorInfo(course);
    }

    @Transactional
    public CourseResponse updateCourse(Long id, CourseUpdateRequest request,
                                       Long currentUserId, boolean isAdmin) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new AppException("Course not found with id: " + id));

        if (!canModifyCourse(course, currentUserId, isAdmin)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You don't have permission to modify this course");
        }

        // Для CourseUpdateRequest используем геттеры
        if (request.getTitle() != null) {
            if (!request.getTitle().equals(course.getTitle()) &&
                    courseRepository.existsByTitleAndCreatorId(request.getTitle(), course.getCreatorId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Course with this title already exists");
            }
            course.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            course.setDescription(request.getDescription());
        }

        if (request.getLanguage() != null) {
            course.setLanguage(request.getLanguage());
        }

        if (request.getLevel() != null) {
            course.setLevel(request.getLevel());
        }

        if (request.getPrice() != null) {
            course.setPrice(request.getPrice());
        }

        if (request.getCoverImageUrl() != null) {
            course.setCoverImageUrl(request.getCoverImageUrl());
        }

        if (request.getStatus() != null) {
            if (isAdmin) {
                course.setStatus(request.getStatus());
                if (request.getStatus() != CourseStatus.REJECTED) {
                    course.setRejectionReason(null);
                }
            } else if (Objects.equals(course.getCreatorId(), currentUserId)) {
                if ((course.getStatus() == CourseStatus.DRAFT || course.getStatus() == CourseStatus.REJECTED) &&
                        request.getStatus() == CourseStatus.PENDING_REVIEW) {
                    course.setStatus(CourseStatus.PENDING_REVIEW);
                    course.setRejectionReason(null);
                } else if (request.getStatus() == CourseStatus.ARCHIVED &&
                        (course.getStatus() == CourseStatus.PUBLISHED ||
                                course.getStatus() == CourseStatus.DRAFT)) {
                    course.setStatus(CourseStatus.ARCHIVED);
                }
            }
        }

        Course updatedCourse = courseRepository.save(course);
        log.info("Course updated with id: {}", updatedCourse.getId());

        return enrichWithCreatorInfo(updatedCourse);
    }

    @Transactional
    public void deleteCourse(Long id, Long currentUserId, boolean isAdmin) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new AppException("Course not found with id: " + id));

        // Только админ или создатель может удалить курс
        if (!canModifyCourse(course, currentUserId, isAdmin)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You don't have permission to delete this course");
        }

        // Мягкое удаление - помечаем как архивированный вместо физического удаления
        if (!isAdmin) {
            course.setStatus(CourseStatus.ARCHIVED);
            courseRepository.save(course);
            log.info("Course archived with id: {}", id);
        } else {
            courseRepository.delete(course);
            log.info("Course permanently deleted with id: {}", id);
        }
    }

    public Page<CourseSummaryResponse> getPublishedCourses(String keyword,
                                                           CourseLanguage language,
                                                           CourseLevel level,
                                                           BigDecimal minPrice,
                                                           BigDecimal maxPrice,
                                                           Double minRating,
                                                           Pageable pageable) {
        Page<Course> courses = courseRepository.findPublishedCourses(
                keyword, language, level, minPrice, maxPrice, minRating, pageable);

        return courses.map(this::toSummaryResponse);
    }

    public Page<CourseSummaryResponse> getCreatorCourses(Long creatorId, Pageable pageable) {
        return courseRepository.findByCreatorId(creatorId, pageable)
                .map(this::toSummaryResponse);
    }

    public Page<CourseResponse> getPendingReviewCourses(Pageable pageable) {
        return courseRepository.findByStatus(CourseStatus.PENDING_REVIEW, pageable)
                .map(this::enrichWithCreatorInfo);
    }

    @Transactional
    public CourseResponse approveCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new AppException("Course not found with id: " + id));

        course.setStatus(CourseStatus.PUBLISHED);
        course.setRejectionReason(null);

        return enrichWithCreatorInfo(courseRepository.save(course));
    }

    @Transactional
    public CourseResponse rejectCourse(Long id, String reason) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new AppException("Course not found with id: " + id));

        course.setStatus(CourseStatus.REJECTED);
        course.setRejectionReason(reason.trim());

        return enrichWithCreatorInfo(courseRepository.save(course));
    }

    @Transactional
    public void incrementStudentCount(Long courseId) {
        courseRepository.incrementStudentCount(courseId);
    }

    @Transactional
    public void updateCourseRating(Long courseId, Double newRating) {
        courseRepository.updateCourseRating(courseId, newRating);
    }

    // Приватные методы
    private boolean canAccessCourse(Course course, Long userId, boolean isAdmin) {
        // Админ имеет доступ ко всему
        if (isAdmin) return true;

        // Создатель имеет доступ к своему курсу
        if (Objects.equals(course.getCreatorId(), userId)) return true;

        // Опубликованные курсы доступны всем
        return course.getStatus() == CourseStatus.PUBLISHED;
    }

    private boolean canModifyCourse(Course course, Long userId, boolean isAdmin) {
        // Админ может модифицировать любой курс
        if (isAdmin) return true;

        // Пользователь может модифицировать только свой курс
        return Objects.equals(course.getCreatorId(), userId);
    }

    private CourseResponse enrichWithCreatorInfo(Course course) {
        CourseResponse response = courseMapper.toResponse(course);

        // Добавляем информацию о создателе
        userRepository.findById(course.getCreatorId()).ifPresent(creator -> {
            response.setCreatorName(creator.getFirstName() + " " + creator.getLastName());
            response.setCreatorAvatarUrl(creator.getAvatarUrl());
        });

        return response;
    }

    private CourseSummaryResponse toSummaryResponse(Course course) {
        return new CourseSummaryResponse(
                course.getId(),
                course.getTitle(),
                course.getLanguage(),
                course.getLevel(),
                course.getPrice(),
                course.getRating(),
                course.getReviewsCount(),
                course.getTotalStudents(),
                course.getCoverImageUrl(),
                course.getCreatedAt()
        );
    }
}
