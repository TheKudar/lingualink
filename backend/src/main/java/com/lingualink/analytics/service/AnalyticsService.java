package com.lingualink.analytics.service;

import com.lingualink.analytics.dto.*;
import com.lingualink.analytics.entity.AnalyticsEvent;
import com.lingualink.analytics.entity.AnalyticsEventType;
import com.lingualink.analytics.repository.AnalyticsEventRepository;
import com.lingualink.common.exception.AppException;
import com.lingualink.course.entity.Course;
import com.lingualink.course.entity.Exercise;
import com.lingualink.course.entity.Lesson;
import com.lingualink.course.repository.CourseRepository;
import com.lingualink.course.repository.ExerciseRepository;
import com.lingualink.course.repository.LessonRepository;
import com.lingualink.course.service.CourseAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    private static final String COMPLETED_STATUS = "COMPLETED";

    private final AnalyticsEventRepository analyticsEventRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final ExerciseRepository exerciseRepository;
    private final CourseAccessService courseAccessService;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public AnalyticsEventResponse trackEvent(AnalyticsEventRequest request, Long userId, boolean isAdmin) {
        Course course = getCourse(request.courseId());
        courseAccessService.validateCourseContentAccess(course, userId, isAdmin);
        validateEventScope(course.getId(), request.lessonId(), request.exerciseId());

        AnalyticsEvent event = analyticsEventRepository.save(AnalyticsEvent.builder()
                .userId(userId)
                .courseId(course.getId())
                .lessonId(request.lessonId())
                .exerciseId(request.exerciseId())
                .eventType(request.eventType())
                .correct(request.correct())
                .durationSeconds(request.durationSeconds())
                .metadata(trimToNull(request.metadata()))
                .build());

        return toResponse(event);
    }

    @Transactional
    public void recordLessonComplete(Long courseId, Long lessonId, Long userId) {
        analyticsEventRepository.save(AnalyticsEvent.builder()
                .userId(userId)
                .courseId(courseId)
                .lessonId(lessonId)
                .eventType(AnalyticsEventType.LESSON_COMPLETE)
                .build());
    }

    @Transactional
    public void recordQuestionAnswered(Long courseId, Long lessonId, Long exerciseId, Long userId, boolean correct) {
        analyticsEventRepository.save(AnalyticsEvent.builder()
                .userId(userId)
                .courseId(courseId)
                .lessonId(lessonId)
                .exerciseId(exerciseId)
                .eventType(AnalyticsEventType.QUESTION_ANSWERED)
                .correct(correct)
                .build());
    }

    public CourseAnalyticsOverviewResponse getOverview(Long courseId, Long creatorId, boolean isAdmin) {
        validateCreatorAccess(courseId, creatorId, isAdmin);

        LocalDateTime now = LocalDateTime.now();
        long dau = countDistinctActiveUsers(courseId, now.minusDays(1));
        long wau = countDistinctActiveUsers(courseId, now.minusDays(7));
        long enrolledUsers = countNumber("""
                SELECT COUNT(*)
                FROM enrollments
                WHERE course_id = ?
                """, courseId);
        long completedUsers = countNumber("""
                SELECT COUNT(*)
                FROM enrollments
                WHERE course_id = ? AND status = ?
                """, courseId, COMPLETED_STATUS);

        double completionRate = percentage(completedUsers, enrolledUsers);
        Double averageSessionDuration = queryNullableDouble("""
                SELECT AVG(duration_seconds)
                FROM analytics_events
                WHERE course_id = ?
                  AND event_type = 'COURSE_EXIT'
                  AND duration_seconds IS NOT NULL
                """, courseId);
        Double averageTotalTimeSpentPerUser = queryNullableDouble("""
                SELECT AVG(user_total)
                FROM (
                    SELECT user_id, SUM(duration_seconds) AS user_total
                    FROM analytics_events
                    WHERE course_id = ?
                      AND duration_seconds IS NOT NULL
                      AND event_type IN ('COURSE_EXIT', 'LESSON_COMPLETE')
                    GROUP BY user_id
                ) totals
                """, courseId);
        Double averageTimeToComplete = averageTimeToCompleteSeconds(courseId);

        return new CourseAnalyticsOverviewResponse(
                courseId,
                dau,
                wau,
                enrolledUsers,
                completedUsers,
                completionRate,
                averageSessionDuration,
                averageTotalTimeSpentPerUser,
                averageTimeToComplete,
                getLessonTimes(courseId)
        );
    }

    public List<QuestionAnalyticsResponse> getQuestionAnalytics(Long courseId, Long creatorId, boolean isAdmin) {
        validateCreatorAccess(courseId, creatorId, isAdmin);

        return jdbcTemplate.query("""
                SELECT e.id AS question_id,
                       e.lesson_id AS lesson_id,
                       e.question AS question,
                       COUNT(ea.id) AS total_answers,
                       COALESCE(SUM(CASE WHEN ea.correct = TRUE THEN 1 ELSE 0 END), 0) AS correct_answers,
                       COALESCE(SUM(CASE WHEN ea.correct = TRUE THEN 0 ELSE 1 END), 0) AS incorrect_answers
                FROM exercises e
                JOIN lessons l ON l.id = e.lesson_id
                JOIN modules m ON m.id = l.module_id
                LEFT JOIN exercise_attempts ea ON ea.exercise_id = e.id
                WHERE m.course_id = ?
                GROUP BY e.id, e.lesson_id, e.question
                HAVING COUNT(ea.id) > 0
                ORDER BY
                    CASE WHEN COUNT(ea.id) = 0 THEN 0
                         ELSE COALESCE(SUM(CASE WHEN ea.correct = TRUE THEN 0 ELSE 1 END), 0) * 1.0 / COUNT(ea.id)
                    END DESC,
                    COUNT(ea.id) DESC
                """, (rs, rowNum) -> {
            long totalAnswers = rs.getLong("total_answers");
            long correctAnswers = rs.getLong("correct_answers");
            long incorrectAnswers = rs.getLong("incorrect_answers");
            return new QuestionAnalyticsResponse(
                    rs.getLong("question_id"),
                    rs.getLong("lesson_id"),
                    rs.getString("question"),
                    totalAnswers,
                    correctAnswers,
                    incorrectAnswers,
                    percentage(incorrectAnswers, totalAnswers)
            );
        }, courseId);
    }

    public List<DropoffLessonAnalyticsResponse> getDropoff(Long courseId, Long creatorId, boolean isAdmin) {
        validateCreatorAccess(courseId, creatorId, isAdmin);

        List<LessonOrderRow> lessons = jdbcTemplate.query("""
                SELECT l.id AS lesson_id,
                       m.id AS module_id,
                       l.title AS lesson_title
                FROM lessons l
                JOIN modules m ON m.id = l.module_id
                WHERE m.course_id = ?
                ORDER BY m.order_index ASC, l.order_index ASC
                """, (rs, rowNum) -> new LessonOrderRow(
                rs.getLong("lesson_id"),
                rs.getLong("module_id"),
                rs.getString("lesson_title"),
                rowNum
        ), courseId);

        if (lessons.isEmpty()) {
            return List.of();
        }

        Map<Long, Integer> lessonPositionById = new HashMap<>();
        for (LessonOrderRow lesson : lessons) {
            lessonPositionById.put(lesson.lessonId(), lesson.position());
        }

        List<EnrollmentRow> enrollments = jdbcTemplate.query("""
                SELECT student_id, status
                FROM enrollments
                WHERE course_id = ?
                """, (rs, rowNum) -> new EnrollmentRow(
                rs.getLong("student_id"),
                rs.getString("status")
        ), courseId);

        Map<Long, Set<Long>> completedUsersByLesson = new HashMap<>();
        Map<Long, Integer> maxCompletedPositionByStudent = new HashMap<>();
        jdbcTemplate.query("""
                SELECT lp.student_id, lp.lesson_id
                FROM lesson_progress lp
                JOIN lessons l ON l.id = lp.lesson_id
                JOIN modules m ON m.id = l.module_id
                WHERE m.course_id = ?
                  AND lp.completed = TRUE
                """, rs -> {
            long studentId = rs.getLong("student_id");
            long lessonId = rs.getLong("lesson_id");
            Integer position = lessonPositionById.get(lessonId);
            if (position == null) {
                return;
            }
            completedUsersByLesson.computeIfAbsent(lessonId, ignored -> new HashSet<>()).add(studentId);
            maxCompletedPositionByStudent.merge(studentId, position, Math::max);
        }, courseId);

        Map<Long, Long> startedUsersByLesson = queryLongMap("""
                SELECT lesson_id, COUNT(DISTINCT user_id) AS users_count
                FROM analytics_events
                WHERE course_id = ?
                  AND event_type = 'LESSON_START'
                  AND lesson_id IS NOT NULL
                GROUP BY lesson_id
                """, "lesson_id", "users_count", courseId);

        Map<Long, Long> stoppedUsersByLesson = new HashMap<>();
        for (EnrollmentRow enrollment : enrollments) {
            if (COMPLETED_STATUS.equals(enrollment.status())) {
                continue;
            }
            int stoppedPosition = maxCompletedPositionByStudent.getOrDefault(enrollment.studentId(), 0);
            LessonOrderRow stoppedLesson = lessons.get(stoppedPosition);
            stoppedUsersByLesson.merge(stoppedLesson.lessonId(), 1L, Long::sum);
        }

        long enrolledUsers = enrollments.size();
        List<DropoffLessonAnalyticsResponse> response = new ArrayList<>();
        for (LessonOrderRow lesson : lessons) {
            long completedUsers = completedUsersByLesson.getOrDefault(lesson.lessonId(), Set.of()).size();
            long startedUsers = Math.max(
                    startedUsersByLesson.getOrDefault(lesson.lessonId(), 0L),
                    completedUsers
            );
            long stoppedUsers = stoppedUsersByLesson.getOrDefault(lesson.lessonId(), 0L);
            response.add(new DropoffLessonAnalyticsResponse(
                    lesson.lessonId(),
                    lesson.moduleId(),
                    lesson.lessonTitle(),
                    lesson.position() + 1,
                    startedUsers,
                    completedUsers,
                    stoppedUsers,
                    percentage(stoppedUsers, enrolledUsers)
            ));
        }
        return response;
    }

    private List<LessonTimeAnalyticsResponse> getLessonTimes(Long courseId) {
        return jdbcTemplate.query("""
                SELECT l.id AS lesson_id,
                       m.id AS module_id,
                       l.title AS lesson_title,
                       AVG(ae.duration_seconds) AS avg_seconds,
                       SUM(ae.duration_seconds) AS total_seconds
                FROM lessons l
                JOIN modules m ON m.id = l.module_id
                LEFT JOIN analytics_events ae
                    ON ae.lesson_id = l.id
                   AND ae.event_type = 'LESSON_COMPLETE'
                   AND ae.duration_seconds IS NOT NULL
                WHERE m.course_id = ?
                GROUP BY l.id, m.id, l.title, m.order_index, l.order_index
                ORDER BY m.order_index ASC, l.order_index ASC
                """, (rs, rowNum) -> new LessonTimeAnalyticsResponse(
                rs.getLong("lesson_id"),
                rs.getLong("module_id"),
                rs.getString("lesson_title"),
                getNullableDouble(rs, "avg_seconds"),
                getNullableLong(rs, "total_seconds")
        ), courseId);
    }

    private void validateCreatorAccess(Long courseId, Long creatorId, boolean isAdmin) {
        Course course = getCourse(courseId);
        if (!isAdmin && !Objects.equals(course.getCreatorId(), creatorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the course creator or an admin can view analytics");
        }
    }

    private void validateEventScope(Long courseId, Long lessonId, Long exerciseId) {
        if (lessonId != null) {
            Lesson lesson = lessonRepository.findById(lessonId)
                    .orElseThrow(() -> new AppException("Lesson not found with id: " + lessonId));
            if (!Objects.equals(lesson.getModule().getCourse().getId(), courseId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lesson does not belong to this course");
            }
        }

        if (exerciseId != null) {
            Exercise exercise = exerciseRepository.findById(exerciseId)
                    .orElseThrow(() -> new AppException("Exercise not found with id: " + exerciseId));
            if (!Objects.equals(exercise.getLesson().getModule().getCourse().getId(), courseId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Exercise does not belong to this course");
            }
            if (lessonId != null && !Objects.equals(exercise.getLesson().getId(), lessonId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Exercise does not belong to this lesson");
            }
        }
    }

    private Course getCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException("Course not found with id: " + courseId));
    }

    private long countDistinctActiveUsers(Long courseId, LocalDateTime since) {
        return countNumber("""
                SELECT COUNT(DISTINCT user_id)
                FROM analytics_events
                WHERE course_id = ?
                  AND occurred_at >= ?
                """, courseId, Timestamp.valueOf(since));
    }

    private long countNumber(String sql, Object... args) {
        Number value = jdbcTemplate.queryForObject(sql, Number.class, args);
        return value == null ? 0L : value.longValue();
    }

    private Double queryNullableDouble(String sql, Object... args) {
        Number value = jdbcTemplate.queryForObject(sql, Number.class, args);
        return value == null ? null : value.doubleValue();
    }

    private Map<Long, Long> queryLongMap(String sql, String keyColumn, String valueColumn, Object... args) {
        Map<Long, Long> result = new HashMap<>();
        jdbcTemplate.query(sql, (RowCallbackHandler) rs -> result.put(rs.getLong(keyColumn), rs.getLong(valueColumn)), args);
        return result;
    }

    private Double averageTimeToCompleteSeconds(Long courseId) {
        List<CompletionTimeRow> rows = jdbcTemplate.query("""
                SELECT enrolled_at, completed_at
                FROM enrollments
                WHERE course_id = ?
                  AND status = ?
                  AND completed_at IS NOT NULL
                """, (rs, rowNum) -> new CompletionTimeRow(
                rs.getTimestamp("enrolled_at").toLocalDateTime(),
                rs.getTimestamp("completed_at").toLocalDateTime()
        ), courseId, COMPLETED_STATUS);

        return rows.stream()
                .mapToLong(row -> Duration.between(row.enrolledAt(), row.completedAt()).getSeconds())
                .average()
                .stream()
                .boxed()
                .findFirst()
                .orElse(null);
    }

    private AnalyticsEventResponse toResponse(AnalyticsEvent event) {
        return new AnalyticsEventResponse(
                event.getId(),
                event.getUserId(),
                event.getCourseId(),
                event.getLessonId(),
                event.getExerciseId(),
                event.getEventType(),
                event.getCorrect(),
                event.getDurationSeconds(),
                event.getOccurredAt()
        );
    }

    private double percentage(long numerator, long denominator) {
        if (denominator == 0) {
            return 0.0;
        }
        return Math.round((numerator * 10000.0 / denominator)) / 100.0;
    }

    private Double getNullableDouble(ResultSet rs, String column) throws SQLException {
        Number value = (Number) rs.getObject(column);
        return value == null ? null : value.doubleValue();
    }

    private Long getNullableLong(ResultSet rs, String column) throws SQLException {
        Number value = (Number) rs.getObject(column);
        return value == null ? null : value.longValue();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private record CompletionTimeRow(LocalDateTime enrolledAt, LocalDateTime completedAt) {
    }

    private record LessonOrderRow(Long lessonId, Long moduleId, String lessonTitle, int position) {
    }

    private record EnrollmentRow(Long studentId, String status) {
    }
}
