package com.lingualink.course.service;

import com.lingualink.common.exception.AppException;
import com.lingualink.course.dto.ModuleCreateRequest;
import com.lingualink.course.dto.ModuleResponse;
import com.lingualink.course.entity.Course;
import com.lingualink.course.entity.CourseStatus;
import com.lingualink.course.entity.Module;
import com.lingualink.course.mapper.ModuleMapper;
import com.lingualink.course.repository.CourseRepository;
import com.lingualink.course.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final CourseRepository courseRepository;
    private final ModuleMapper moduleMapper;

    @Transactional
    public ModuleResponse createModule(Long courseId, ModuleCreateRequest request, Long currentUserId, boolean isAdmin) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException("Course not found with id: " + courseId));

        // Проверка прав: только создатель курса или админ
        if (!isAdmin && !Objects.equals(course.getCreatorId(), currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to modify this course");
        }

        Module module = Module.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .orderIndex(request.getOrderIndex())
                .course(course)
                .build();

        Module saved = moduleRepository.save(module);
        log.info("com.lingualink.course.entity.Module created with id: {} for course: {}", saved.getId(), courseId);

        return moduleMapper.toResponse(saved);
    }

    public List<ModuleResponse> getModulesByCourse(Long courseId, Long currentUserId, boolean isAdmin) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException("Course not found with id: " + courseId));

        // Проверка доступа к курсу
        if (!canAccessCourse(course, currentUserId, isAdmin)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have access to this course");
        }

        List<Module> modules = moduleRepository.findByCourseIdWithLessons(courseId);
        return modules.stream()
                .map(moduleMapper::toResponse)
                .toList();
    }

    @Transactional
    public ModuleResponse updateModule(Long courseId, Long moduleId, ModuleCreateRequest request,
                                       Long currentUserId, boolean isAdmin) {
        Module module = moduleRepository.findByIdAndCourseId(moduleId, courseId)
                .orElseThrow(() -> new AppException("com.lingualink.course.entity.Module not found with id: " + moduleId + " for course: " + courseId));

        Course course = module.getCourse();
        if (!isAdmin && !Objects.equals(course.getCreatorId(), currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to modify this module");
        }

        if (request.getTitle() != null) {
            module.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            module.setDescription(request.getDescription());
        }
        if (request.getOrderIndex() != null) {
            module.setOrderIndex(request.getOrderIndex());
        }

        Module updated = moduleRepository.save(module);
        return moduleMapper.toResponse(updated);
    }

    @Transactional
    public void deleteModule(Long courseId, Long moduleId, Long currentUserId, boolean isAdmin) {
        Module module = moduleRepository.findByIdAndCourseId(moduleId, courseId)
                .orElseThrow(() -> new AppException("com.lingualink.course.entity.Module not found with id: " + moduleId + " for course: " + courseId));

        Course course = module.getCourse();
        if (!isAdmin && !Objects.equals(course.getCreatorId(), currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to delete this module");
        }

        moduleRepository.delete(module);
        log.info("com.lingualink.course.entity.Module deleted with id: {} from course: {}", moduleId, courseId);
    }

    private boolean canAccessCourse(Course course, Long userId, boolean isAdmin) {
        if (isAdmin) return true;
        if (Objects.equals(course.getCreatorId(), userId)) return true;
        return course.getStatus() == CourseStatus.PUBLISHED;
    }
}