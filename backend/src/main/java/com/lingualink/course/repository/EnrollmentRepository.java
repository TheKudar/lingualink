package com.lingualink.course.repository;

import com.lingualink.course.entity.Enrollment;
import com.lingualink.course.entity.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    boolean existsByStudentIdAndCourseIdAndStatusIn(Long studentId, Long courseId, Collection<EnrollmentStatus> statuses);

    long countByCourseIdAndStatusIn(Long courseId, Collection<EnrollmentStatus> statuses);

    Page<Enrollment> findByStudentIdOrderByEnrolledAtDesc(Long studentId, Pageable pageable);
}
