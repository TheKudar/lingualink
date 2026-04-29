package com.lingualink.course.repository;

import com.lingualink.course.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByModuleIdOrderByOrderIndexAsc(Long moduleId);

    Optional<Lesson> findByIdAndModuleId(Long id, Long moduleId);

    Optional<Lesson> findByIdAndModuleIdAndModuleCourseId(Long id, Long moduleId, Long courseId);

    long countByModuleCourseId(Long courseId);
}
