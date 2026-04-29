package com.lingualink.course.repository;

import com.lingualink.course.entity.ExerciseAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExerciseAttemptRepository extends JpaRepository<ExerciseAttempt, Long> {
    long countByStudentIdAndExerciseLessonModuleCourseId(Long studentId, Long courseId);

    @Query("""
            SELECT COUNT(DISTINCT ea.exercise.id)
            FROM ExerciseAttempt ea
            WHERE ea.studentId = :studentId
              AND ea.correct = true
              AND ea.exercise.lesson.module.course.id = :courseId
            """)
    long countCompletedExercisesForCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
}
