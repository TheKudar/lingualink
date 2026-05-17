package com.lingualink.report.repository;

import com.lingualink.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    List<Report> findAllByOrderByCreatedAtDesc();

    void deleteByCourseId(Long courseId);
}
