package com.lingualink.course.repository;

import com.lingualink.course.entity.Course;
import com.lingualink.course.entity.CourseLanguage;
import com.lingualink.course.entity.CourseLevel;
import com.lingualink.course.entity.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    // Существующие методы...
    List<Course> findByCreatorId(Long creatorId);
    Page<Course> findByCreatorId(Long creatorId, Pageable pageable);

    // Новый метод для проверки уникальности
    boolean existsByTitleAndCreatorId(String title, Long creatorId);

    @Query("SELECT c FROM Course c WHERE c.status = 'PUBLISHED' " +
            "AND (:keyword IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:language IS NULL OR c.language = :language) " +
            "AND (:level IS NULL OR c.level = :level) " +
            "AND (:minPrice IS NULL OR c.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR c.price <= :maxPrice) " +
            "AND (:minRating IS NULL OR c.rating >= :minRating)")
    Page<Course> findPublishedCourses(@Param("keyword") String keyword,
                                      @Param("language") CourseLanguage language,
                                      @Param("level") CourseLevel level,
                                      @Param("minPrice") BigDecimal minPrice,
                                      @Param("maxPrice") BigDecimal maxPrice,
                                      @Param("minRating") Double minRating,
                                      Pageable pageable);

    boolean existsByIdAndStatus(Long id, CourseStatus status);

    // Новые методы для обновления статистики
    @Modifying
    @Query("UPDATE Course c SET c.totalStudents = c.totalStudents + 1 WHERE c.id = :courseId")
    void incrementStudentCount(@Param("courseId") Long courseId);

    @Modifying
    @Query("UPDATE Course c SET c.rating = :newRating, " +
            "c.reviewsCount = c.reviewsCount + 1 WHERE c.id = :courseId")
    void updateCourseRating(@Param("courseId") Long courseId,
                            @Param("newRating") Double newRating);

    // Поиск курсов для модерации (админские методы)
    Page<Course> findByStatus(CourseStatus status, Pageable pageable);

    // Популярные курсы
    Page<Course> findByStatusOrderByRatingDescTotalStudentsDesc(CourseStatus status, Pageable pageable);

    // Новые курсы
    Page<Course> findByStatusOrderByCreatedAtDesc(CourseStatus status, Pageable pageable);
}