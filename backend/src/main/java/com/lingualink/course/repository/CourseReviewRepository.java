package com.lingualink.course.repository;

import com.lingualink.course.entity.CourseReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseReviewRepository extends JpaRepository<CourseReview, Long> {
    boolean existsByCourseIdAndStudentId(Long courseId, Long studentId);

    long countByCourseId(Long courseId);

    Page<CourseReview> findByCourseIdOrderByCreatedAtDesc(Long courseId, Pageable pageable);

    @Query("SELECT COALESCE(AVG(cr.rating), 0) FROM CourseReview cr WHERE cr.courseId = :courseId")
    Double calculateAverageRating(@Param("courseId") Long courseId);
}
