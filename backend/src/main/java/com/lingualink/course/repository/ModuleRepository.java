package com.lingualink.course.repository;

import com.lingualink.course.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {

    List<Module> findByCourseIdOrderByOrderIndexAsc(Long courseId);

    @Query("SELECT m FROM Module m JOIN FETCH m.lessons WHERE m.course.id = :courseId ORDER BY m.orderIndex ASC")
    List<Module> findByCourseIdWithLessons(@Param("courseId") Long courseId);

    @Query("SELECT m FROM Module m WHERE m.id = :id AND m.course.id = :courseId")
    Optional<Module> findByIdAndCourseId(@Param("id") Long id, @Param("courseId") Long courseId);
}