package com.lingualink.course.repository;

import com.lingualink.course.entity.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {
    Optional<LessonProgress> findByStudentIdAndLessonId(Long studentId, Long lessonId);

    @Query("""
            SELECT COUNT(lp)
            FROM LessonProgress lp
            JOIN Lesson l ON l.id = lp.lessonId
            JOIN l.module m
            WHERE lp.studentId = :studentId
              AND lp.completed = true
              AND m.course.id = :courseId
            """)
    long countCompletedLessonsForCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
}
