package com.lingualink.reading.repository;

import com.lingualink.course.entity.CourseLanguage;
import com.lingualink.course.entity.CourseLevel;
import com.lingualink.reading.entity.ReadingMaterial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadingMaterialRepository extends JpaRepository<ReadingMaterial, Long> {
    Page<ReadingMaterial> findByLanguageAndLevel(CourseLanguage language, CourseLevel level, Pageable pageable);
    Page<ReadingMaterial> findByLanguage(CourseLanguage language, Pageable pageable);
    Page<ReadingMaterial> findByLevel(CourseLevel level, Pageable pageable);
}
