package com.lingualink.course.dto;

import com.lingualink.course.entity.CourseLanguage;
import com.lingualink.course.entity.CourseLevel;
import com.lingualink.course.entity.CourseStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CourseResponse {
    private Long id;
    private String title;
    private String description;
    private CourseLanguage language;
    private CourseLevel level;
    private Long creatorId;
    private String creatorName;        // новое поле
    private String creatorAvatarUrl;   // новое поле
    private BigDecimal price;
    private Double rating;
    private Integer reviewsCount;
    private Integer totalStudents;
    private String coverImageUrl;
    private CourseStatus status;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Конструкторы
    public CourseResponse() {}

    public CourseResponse(Long id, String title, String description, CourseLanguage language,
                          CourseLevel level, Long creatorId, BigDecimal price, Double rating,
                          Integer reviewsCount, Integer totalStudents, String coverImageUrl,
                          CourseStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.language = language;
        this.level = level;
        this.creatorId = creatorId;
        this.price = price;
        this.rating = rating;
        this.reviewsCount = reviewsCount;
        this.totalStudents = totalStudents;
        this.coverImageUrl = coverImageUrl;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public CourseLanguage getLanguage() { return language; }
    public void setLanguage(CourseLanguage language) { this.language = language; }

    public CourseLevel getLevel() { return level; }
    public void setLevel(CourseLevel level) { this.level = level; }

    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }

    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }

    public String getCreatorAvatarUrl() { return creatorAvatarUrl; }
    public void setCreatorAvatarUrl(String creatorAvatarUrl) { this.creatorAvatarUrl = creatorAvatarUrl; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public Integer getReviewsCount() { return reviewsCount; }
    public void setReviewsCount(Integer reviewsCount) { this.reviewsCount = reviewsCount; }

    public Integer getTotalStudents() { return totalStudents; }
    public void setTotalStudents(Integer totalStudents) { this.totalStudents = totalStudents; }

    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }

    public CourseStatus getStatus() { return status; }
    public void setStatus(CourseStatus status) { this.status = status; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
