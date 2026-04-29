package com.lingualink.course.repository;

import com.lingualink.course.entity.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    List<Exercise> findByLessonIdOrderByOrderIndexAsc(Long lessonId);

    Optional<Exercise> findByIdAndLessonIdAndLessonModuleIdAndLessonModuleCourseId(
            Long id,
            Long lessonId,
            Long moduleId,
            Long courseId
    );

    long countByLessonModuleCourseId(Long courseId);
}
