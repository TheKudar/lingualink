package com.lingualink.reading.service;

import com.lingualink.common.exception.AppException;
import com.lingualink.course.entity.CourseLanguage;
import com.lingualink.course.entity.CourseLevel;
import com.lingualink.reading.dto.ReadingMaterialRequest;
import com.lingualink.reading.dto.ReadingMaterialResponse;
import com.lingualink.reading.entity.ReadingMaterial;
import com.lingualink.reading.repository.ReadingMaterialRepository;
import com.lingualink.user.entity.User;
import com.lingualink.user.entity.UserRole;
import com.lingualink.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReadingMaterialService {

    private final ReadingMaterialRepository readingMaterialRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReadingMaterialResponse create(ReadingMaterialRequest request, Long creatorId) {
        User creator = getUser(creatorId);
        requireCreatorOrAdmin(creator);

        ReadingMaterial material = ReadingMaterial.builder()
                .title(request.title().trim())
                .language(request.language())
                .level(request.level())
                .content(sanitizeContent(request.content()))
                .creatorId(creatorId)
                .build();

        return toResponse(readingMaterialRepository.save(material));
    }

    public Page<ReadingMaterialResponse> list(CourseLanguage language, CourseLevel level, Pageable pageable) {
        Page<ReadingMaterial> materials;
        if (language != null && level != null) {
            materials = readingMaterialRepository.findByLanguageAndLevel(language, level, pageable);
        } else if (language != null) {
            materials = readingMaterialRepository.findByLanguage(language, pageable);
        } else if (level != null) {
            materials = readingMaterialRepository.findByLevel(level, pageable);
        } else {
            materials = readingMaterialRepository.findAll(pageable);
        }

        return materials.map(this::toResponse);
    }

    public ReadingMaterialResponse getById(Long id) {
        return toResponse(getMaterial(id));
    }

    @Transactional
    public ReadingMaterialResponse update(Long id, ReadingMaterialRequest request, Long currentUserId, boolean isAdmin) {
        ReadingMaterial material = getMaterial(id);
        validateCanModify(material, currentUserId, isAdmin);

        material.setTitle(request.title().trim());
        material.setLanguage(request.language());
        material.setLevel(request.level());
        material.setContent(sanitizeContent(request.content()));

        return toResponse(readingMaterialRepository.save(material));
    }

    @Transactional
    public void delete(Long id, Long currentUserId, boolean isAdmin) {
        ReadingMaterial material = getMaterial(id);
        validateCanModify(material, currentUserId, isAdmin);
        readingMaterialRepository.delete(material);
    }

    private ReadingMaterial getMaterial(Long id) {
        return readingMaterialRepository.findById(id)
                .orElseThrow(() -> new AppException("Reading material not found with id: " + id));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found with id: " + userId));
    }

    private void requireCreatorOrAdmin(User user) {
        if (user.getRole() != UserRole.CREATOR && user.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only creators can manage reading materials");
        }
    }

    private void validateCanModify(ReadingMaterial material, Long currentUserId, boolean isAdmin) {
        User currentUser = getUser(currentUserId);
        requireCreatorOrAdmin(currentUser);

        if (!isAdmin && !Objects.equals(material.getCreatorId(), currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to modify this reading material");
        }
    }

    private ReadingMaterialResponse toResponse(ReadingMaterial material) {
        return new ReadingMaterialResponse(
                material.getId(),
                material.getTitle(),
                material.getLanguage(),
                material.getLevel(),
                material.getContent(),
                material.getCreatorId(),
                material.getCreatedAt(),
                material.getUpdatedAt()
        );
    }

    private String sanitizeContent(String content) {
        return Jsoup.clean(content, Safelist.relaxed()
                .addProtocols("a", "href", "http", "https", "mailto")
                .addProtocols("img", "src", "http", "https"));
    }
}
