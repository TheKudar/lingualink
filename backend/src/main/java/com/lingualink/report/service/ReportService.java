package com.lingualink.report.service;

import com.lingualink.common.exception.AppException;
import com.lingualink.course.entity.Course;
import com.lingualink.course.entity.CourseStatus;
import com.lingualink.course.repository.CourseRepository;
import com.lingualink.report.dto.ReportCreateRequest;
import com.lingualink.report.dto.ReportResponse;
import com.lingualink.report.entity.Report;
import com.lingualink.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;
    private final CourseRepository courseRepository;

    @Transactional
    public ReportResponse createReport(Long userId, ReportCreateRequest request) {
        Long courseId = request.courseId();
        if (!courseRepository.existsById(courseId)) {
            throw new AppException("Курс не найден: " + courseId);
        }

        if (reportRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Вы уже отправляли жалобу на этот курс");
        }

        Report report = Report.builder()
                .userId(userId)
                .courseId(courseId)
                .message(request.message().trim())
                .build();

        return toResponse(reportRepository.save(report));
    }

    public List<ReportResponse> getAllReports() {
        return reportRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void banReportedCourse(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new AppException("Жалоба не найдена: " + reportId));

        Course course = courseRepository.findById(report.getCourseId())
                .orElseThrow(() -> new AppException("Курс не найден: " + report.getCourseId()));

        course.setStatus(CourseStatus.ARCHIVED);
        courseRepository.save(course);
        reportRepository.deleteByCourseId(course.getId());
    }

    @Transactional
    public void keepReportedCourse(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new AppException("Жалоба не найдена: " + reportId));

        reportRepository.delete(report);
    }

    private ReportResponse toResponse(Report report) {
        return new ReportResponse(
                report.getId(),
                report.getUserId(),
                report.getCourseId(),
                report.getMessage(),
                report.getCreatedAt()
        );
    }
}
