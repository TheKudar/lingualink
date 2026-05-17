package com.lingualink.course.service;

import com.lingualink.common.exception.AppException;
import com.lingualink.course.dto.LessonCreateRequest;
import com.lingualink.course.dto.LessonResponse;
import com.lingualink.course.entity.Course;
import com.lingualink.course.entity.Lesson;
import com.lingualink.course.entity.LessonProgress;
import com.lingualink.course.entity.Module;
import com.lingualink.course.mapper.LessonMapper;
import com.lingualink.course.repository.LessonProgressRepository;
import com.lingualink.course.repository.LessonRepository;
import com.lingualink.course.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LessonService {

    private final LessonRepository lessonRepository;
    private final ModuleRepository moduleRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final LessonMapper lessonMapper;
    private final CourseAccessService courseAccessService;

    @Transactional
    public LessonResponse createLesson(Long courseId, Long moduleId, LessonCreateRequest request,
                                       Long currentUserId, boolean isAdmin) {
        Module module = moduleRepository.findByIdAndCourseId(moduleId, courseId)
                .orElseThrow(() -> new AppException("com.lingualink.course.entity.Module not found with id: " + moduleId + " for course: " + courseId));

        Course course = module.getCourse();
        if (!isAdmin && !Objects.equals(course.getCreatorId(), currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to modify this course");
        }

        Lesson lesson = Lesson.builder()
                .title(request.getTitle())
                .content(sanitizeContent(request.getContent()))
                .orderIndex(request.getOrderIndex())
                .module(module)
                .build();

        Lesson saved = lessonRepository.save(lesson);
        log.info("Lesson created with id: {} in module: {}", saved.getId(), moduleId);

        return lessonMapper.toResponse(saved);
    }

    public LessonResponse getLesson(Long courseId, Long moduleId, Long lessonId,
                                    Long currentUserId, boolean isAdmin) {
        Lesson lesson = lessonRepository.findByIdAndModuleIdAndModuleCourseId(lessonId, moduleId, courseId)
                .orElseThrow(() -> new AppException(
                        "Lesson not found with id: " + lessonId + " for module: " + moduleId + " and course: " + courseId
                ));

        Course course = lesson.getModule().getCourse();
        courseAccessService.validateCourseContentAccess(course, currentUserId, isAdmin);

        return toResponseWithProgress(lesson, currentUserId);
    }

    @Transactional
    public LessonResponse updateLesson(Long courseId, Long moduleId, Long lessonId,
                                       LessonCreateRequest request, Long currentUserId, boolean isAdmin) {
        Lesson lesson = lessonRepository.findByIdAndModuleId(lessonId, moduleId)
                .orElseThrow(() -> new AppException("Lesson not found"));

        Course course = lesson.getModule().getCourse();
        if (!isAdmin && !Objects.equals(course.getCreatorId(), currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to modify this lesson");
        }

        if (request.getTitle() != null) {
            lesson.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            lesson.setContent(sanitizeContent(request.getContent()));
        }
        if (request.getOrderIndex() != null) {
            lesson.setOrderIndex(request.getOrderIndex());
        }

        Lesson updated = lessonRepository.save(lesson);
        return lessonMapper.toResponse(updated);
    }

    @Transactional
    public void deleteLesson(Long courseId, Long moduleId, Long lessonId,
                             Long currentUserId, boolean isAdmin) {
        Lesson lesson = lessonRepository.findByIdAndModuleId(lessonId, moduleId)
                .orElseThrow(() -> new AppException("Lesson not found"));

        Course course = lesson.getModule().getCourse();
        if (!isAdmin && !Objects.equals(course.getCreatorId(), currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to delete this lesson");
        }

        lessonRepository.delete(lesson);
        log.info("Lesson deleted with id: {} from module: {}", lessonId, moduleId);
    }

    private String sanitizeContent(String content) {
        if (content == null) {
            return null;
        }

        Safelist safelist = Safelist.relaxed()
                .addProtocols("a", "href", "http", "https", "mailto")
                .addProtocols("img", "src", "http", "https");

        return Jsoup.clean(content, safelist);
    }

    private LessonResponse toResponseWithProgress(Lesson lesson, Long currentUserId) {
        LessonResponse response = lessonMapper.toResponse(lesson);
        lessonProgressRepository.findByStudentIdAndLessonId(currentUserId, lesson.getId())
                .ifPresentOrElse(
                        progress -> applyProgress(response, progress),
                        () -> response.setCompleted(false)
                );
        return response;
    }

    private void applyProgress(LessonResponse response, LessonProgress progress) {
        response.setCompleted(Boolean.TRUE.equals(progress.getCompleted()));
        response.setCompletedAt(progress.getCompletedAt());
    }
}
