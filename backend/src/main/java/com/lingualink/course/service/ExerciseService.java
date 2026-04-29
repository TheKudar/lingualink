package com.lingualink.course.service;

import com.lingualink.common.exception.AppException;
import com.lingualink.course.dto.ExerciseAnswerRequest;
import com.lingualink.course.dto.ExerciseAttemptResponse;
import com.lingualink.course.dto.ExerciseCreateRequest;
import com.lingualink.course.dto.ExerciseResponse;
import com.lingualink.course.entity.Course;
import com.lingualink.course.entity.Enrollment;
import com.lingualink.course.entity.EnrollmentStatus;
import com.lingualink.course.entity.Exercise;
import com.lingualink.course.entity.ExerciseAttempt;
import com.lingualink.course.entity.ExerciseType;
import com.lingualink.course.entity.Lesson;
import com.lingualink.course.repository.EnrollmentRepository;
import com.lingualink.course.repository.ExerciseAttemptRepository;
import com.lingualink.course.repository.ExerciseRepository;
import com.lingualink.course.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final ExerciseAttemptRepository exerciseAttemptRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseAccessService courseAccessService;
    private final EnrollmentService enrollmentService;
    private final CourseProgressService courseProgressService;

    @Transactional
    public ExerciseResponse createExercise(
            Long courseId,
            Long moduleId,
            Long lessonId,
            ExerciseCreateRequest request,
            Long currentUserId,
            boolean isAdmin
    ) {
        Lesson lesson = getLesson(lessonId, moduleId, courseId);
        Course course = lesson.getModule().getCourse();
        validateCanModify(course, currentUserId, isAdmin);
        validateExerciseRequest(request);

        Exercise exercise = Exercise.builder()
                .lesson(lesson)
                .type(request.type())
                .question(request.question().trim())
                .options(normalizeOptions(request.options()))
                .correctAnswer(request.correctAnswer().trim())
                .explanation(trimToNull(request.explanation()))
                .orderIndex(request.orderIndex())
                .build();

        return toResponse(exerciseRepository.save(exercise), true);
    }

    public List<ExerciseResponse> getExercises(
            Long courseId,
            Long moduleId,
            Long lessonId,
            Long currentUserId,
            boolean isAdmin
    ) {
        Lesson lesson = getLesson(lessonId, moduleId, courseId);
        Course course = lesson.getModule().getCourse();
        courseAccessService.validateCourseContentAccess(course, currentUserId, isAdmin);

        boolean includeAnswer = isAdmin || Objects.equals(course.getCreatorId(), currentUserId);
        return exerciseRepository.findByLessonIdOrderByOrderIndexAsc(lessonId).stream()
                .map(exercise -> toResponse(exercise, includeAnswer))
                .toList();
    }

    public ExerciseResponse getExercise(
            Long courseId,
            Long moduleId,
            Long lessonId,
            Long exerciseId,
            Long currentUserId,
            boolean isAdmin
    ) {
        Exercise exercise = getExercise(exerciseId, lessonId, moduleId, courseId);
        Course course = exercise.getLesson().getModule().getCourse();
        courseAccessService.validateCourseContentAccess(course, currentUserId, isAdmin);

        boolean includeAnswer = isAdmin || Objects.equals(course.getCreatorId(), currentUserId);
        return toResponse(exercise, includeAnswer);
    }

    @Transactional
    public ExerciseResponse updateExercise(
            Long courseId,
            Long moduleId,
            Long lessonId,
            Long exerciseId,
            ExerciseCreateRequest request,
            Long currentUserId,
            boolean isAdmin
    ) {
        Exercise exercise = getExercise(exerciseId, lessonId, moduleId, courseId);
        Course course = exercise.getLesson().getModule().getCourse();
        validateCanModify(course, currentUserId, isAdmin);
        validateExerciseRequest(request);

        exercise.setType(request.type());
        exercise.setQuestion(request.question().trim());
        exercise.setOptions(normalizeOptions(request.options()));
        exercise.setCorrectAnswer(request.correctAnswer().trim());
        exercise.setExplanation(trimToNull(request.explanation()));
        exercise.setOrderIndex(request.orderIndex());

        return toResponse(exerciseRepository.save(exercise), true);
    }

    @Transactional
    public void deleteExercise(Long courseId, Long moduleId, Long lessonId, Long exerciseId, Long currentUserId, boolean isAdmin) {
        Exercise exercise = getExercise(exerciseId, lessonId, moduleId, courseId);
        Course course = exercise.getLesson().getModule().getCourse();
        validateCanModify(course, currentUserId, isAdmin);

        exerciseRepository.delete(exercise);
    }

    @Transactional
    public ExerciseAttemptResponse submitAnswer(
            Long courseId,
            Long moduleId,
            Long lessonId,
            Long exerciseId,
            ExerciseAnswerRequest request,
            Long studentId
    ) {
        Exercise exercise = getExercise(exerciseId, lessonId, moduleId, courseId);
        Enrollment enrollment = enrollmentService.getTrackableEnrollment(studentId, courseId);

        boolean correct = normalizeAnswer(request.answer()).equals(normalizeAnswer(exercise.getCorrectAnswer()));
        ExerciseAttempt attempt = exerciseAttemptRepository.save(ExerciseAttempt.builder()
                .studentId(studentId)
                .exercise(exercise)
                .answer(request.answer().trim())
                .correct(correct)
                .build());

        CourseProgressService.CourseProgressSnapshot snapshot =
                courseProgressService.calculateProgress(studentId, courseId);

        if (snapshot.totalItems() > 0
                && snapshot.completedItems() >= snapshot.totalItems()
                && enrollment.getStatus() != EnrollmentStatus.COMPLETED) {
            enrollment.setStatus(EnrollmentStatus.COMPLETED);
            enrollment.setCompletedAt(attempt.getAttemptedAt());
            enrollmentRepository.save(enrollment);
        }

        return new ExerciseAttemptResponse(
                attempt.getId(),
                exercise.getId(),
                courseId,
                attempt.getAnswer(),
                correct,
                exercise.getCorrectAnswer(),
                exercise.getExplanation(),
                attempt.getAttemptedAt(),
                snapshot.totalLessons(),
                snapshot.completedLessons(),
                snapshot.totalExercises(),
                snapshot.completedExercises(),
                snapshot.exerciseAttempts(),
                snapshot.totalItems(),
                snapshot.completedItems(),
                snapshot.progressPercentage()
        );
    }

    private Lesson getLesson(Long lessonId, Long moduleId, Long courseId) {
        return lessonRepository.findByIdAndModuleIdAndModuleCourseId(lessonId, moduleId, courseId)
                .orElseThrow(() -> new AppException(
                        "Lesson not found with id: " + lessonId + " for module: " + moduleId + " and course: " + courseId
                ));
    }

    private Exercise getExercise(Long exerciseId, Long lessonId, Long moduleId, Long courseId) {
        return exerciseRepository.findByIdAndLessonIdAndLessonModuleIdAndLessonModuleCourseId(
                        exerciseId,
                        lessonId,
                        moduleId,
                        courseId
                )
                .orElseThrow(() -> new AppException("Exercise not found with id: " + exerciseId));
    }

    private void validateCanModify(Course course, Long currentUserId, boolean isAdmin) {
        if (!isAdmin && !Objects.equals(course.getCreatorId(), currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to modify this course");
        }
    }

    private void validateExerciseRequest(ExerciseCreateRequest request) {
        if (request.type() == ExerciseType.MULTIPLE_CHOICE) {
            List<String> options = normalizeOptions(request.options());
            if (options.size() < 2) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Multiple choice exercises require at least two options");
            }
            boolean containsCorrectAnswer = options.stream()
                    .anyMatch(option -> normalizeAnswer(option).equals(normalizeAnswer(request.correctAnswer())));
            if (!containsCorrectAnswer) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Correct answer must match one of the options");
            }
        }
    }

    private ExerciseResponse toResponse(Exercise exercise, boolean includeAnswer) {
        return new ExerciseResponse(
                exercise.getId(),
                exercise.getLesson().getId(),
                exercise.getType(),
                exercise.getQuestion(),
                exercise.getOptions(),
                includeAnswer ? exercise.getCorrectAnswer() : null,
                includeAnswer ? exercise.getExplanation() : null,
                exercise.getOrderIndex()
        );
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private List<String> normalizeOptions(List<String> options) {
        if (options == null) {
            return List.of();
        }
        return options.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(option -> !option.isEmpty())
                .toList();
    }

    private String normalizeAnswer(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
