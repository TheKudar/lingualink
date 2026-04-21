package com.lingualink.course.service;

import com.lingualink.common.exception.AppException;
import com.lingualink.course.dto.LessonCreateRequest;
import com.lingualink.course.dto.LessonResponse;
import com.lingualink.course.entity.Course;
import com.lingualink.course.entity.CourseStatus;
import com.lingualink.course.entity.Lesson;
import com.lingualink.course.entity.Module;
import com.lingualink.course.mapper.LessonMapper;
import com.lingualink.course.repository.LessonRepository;
import com.lingualink.course.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final LessonMapper lessonMapper;

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
                .content(request.getContent())
                .orderIndex(request.getOrderIndex())
                .module(module)
                .build();

        Lesson saved = lessonRepository.save(lesson);
        log.info("Lesson created with id: {} in module: {}", saved.getId(), moduleId);

        return lessonMapper.toResponse(saved);
    }

    public LessonResponse getLesson(Long courseId, Long moduleId, Long lessonId,
                                    Long currentUserId, boolean isAdmin) {
        Lesson lesson = lessonRepository.findByIdAndModuleId(lessonId, moduleId)
                .orElseThrow(() -> new AppException("Lesson not found"));

        Course course = lesson.getModule().getCourse();
        if (!isAdmin && !Objects.equals(course.getCreatorId(), currentUserId) &&
                course.getStatus() != CourseStatus.PUBLISHED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have access to this lesson");
        }

        return lessonMapper.toResponse(lesson);
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
            lesson.setContent(request.getContent());
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
}