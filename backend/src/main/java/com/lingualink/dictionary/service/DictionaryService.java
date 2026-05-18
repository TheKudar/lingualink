package com.lingualink.dictionary.service;

import com.lingualink.common.exception.AppException;
import com.lingualink.dictionary.dto.DictionaryCreateRequest;
import com.lingualink.dictionary.dto.DictionaryEntryRequest;
import com.lingualink.dictionary.dto.DictionaryEntryResponse;
import com.lingualink.dictionary.dto.DictionaryResponse;
import com.lingualink.dictionary.entity.DictionaryEntry;
import com.lingualink.dictionary.entity.PersonalDictionary;
import com.lingualink.dictionary.repository.DictionaryEntryRepository;
import com.lingualink.dictionary.repository.PersonalDictionaryRepository;
import com.lingualink.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DictionaryService {

    private final PersonalDictionaryRepository dictionaryRepository;
    private final DictionaryEntryRepository entryRepository;
    private final UserRepository userRepository;

    public List<DictionaryResponse> listDictionaries(Long userId) {
        requireUser(userId);
        return dictionaryRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public DictionaryResponse getDictionary(Long dictionaryId, Long userId) {
        return toResponse(getDictionaryForUser(dictionaryId, userId));
    }

    @Transactional
    public DictionaryResponse createDictionary(DictionaryCreateRequest request, Long userId) {
        requireUser(userId);

        PersonalDictionary dictionary = PersonalDictionary.builder()
                .name(request.name().trim())
                .userId(userId)
                .build();

        return toResponse(dictionaryRepository.save(dictionary));
    }

    @Transactional
    public DictionaryResponse updateDictionary(Long dictionaryId, DictionaryCreateRequest request, Long userId) {
        PersonalDictionary dictionary = getDictionaryForUser(dictionaryId, userId);
        dictionary.setName(request.name().trim());
        return toResponse(dictionaryRepository.save(dictionary));
    }

    @Transactional
    public void deleteDictionary(Long dictionaryId, Long userId) {
        PersonalDictionary dictionary = getDictionaryForUser(dictionaryId, userId);
        dictionaryRepository.delete(dictionary);
    }

    @Transactional
    public DictionaryResponse addEntry(Long dictionaryId, DictionaryEntryRequest request, Long userId) {
        PersonalDictionary dictionary = getDictionaryForUser(dictionaryId, userId);

        DictionaryEntry entry = DictionaryEntry.builder()
                .sourceWord(request.sourceWord().trim())
                .targetWord(request.targetWord().trim())
                .dictionary(dictionary)
                .build();

        dictionary.getEntries().add(entry);
        entryRepository.save(entry);

        return toResponse(dictionary);
    }

    @Transactional
    public DictionaryResponse updateEntry(Long dictionaryId, Long entryId, DictionaryEntryRequest request, Long userId) {
        PersonalDictionary dictionary = getDictionaryForUser(dictionaryId, userId);
        DictionaryEntry entry = getEntryForDictionary(entryId, dictionaryId);

        entry.setSourceWord(request.sourceWord().trim());
        entry.setTargetWord(request.targetWord().trim());
        entryRepository.save(entry);

        return toResponse(dictionary);
    }

    @Transactional
    public void deleteEntry(Long dictionaryId, Long entryId, Long userId) {
        PersonalDictionary dictionary = getDictionaryForUser(dictionaryId, userId);
        DictionaryEntry entry = getEntryForDictionary(entryId, dictionary.getId());
        dictionary.getEntries().remove(entry);
        entryRepository.delete(entry);
    }

    private PersonalDictionary getDictionaryForUser(Long dictionaryId, Long userId) {
        requireUser(userId);
        return dictionaryRepository.findByIdAndUserId(dictionaryId, userId)
                .orElseThrow(() -> new AppException("Dictionary not found with id: " + dictionaryId));
    }

    private DictionaryEntry getEntryForDictionary(Long entryId, Long dictionaryId) {
        return entryRepository.findByIdAndDictionaryId(entryId, dictionaryId)
                .orElseThrow(() -> new AppException("Dictionary entry not found with id: " + entryId));
    }

    private void requireUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new AppException("User not found with id: " + userId);
        }
    }

    private DictionaryResponse toResponse(PersonalDictionary dictionary) {
        List<DictionaryEntryResponse> entries = dictionary.getEntries()
                .stream()
                .map(this::toEntryResponse)
                .toList();

        return new DictionaryResponse(
                dictionary.getId(),
                dictionary.getName(),
                entries,
                dictionary.getCreatedAt(),
                dictionary.getUpdatedAt()
        );
    }

    private DictionaryEntryResponse toEntryResponse(DictionaryEntry entry) {
        return new DictionaryEntryResponse(
                entry.getId(),
                entry.getSourceWord(),
                entry.getTargetWord(),
                entry.getCreatedAt(),
                entry.getUpdatedAt()
        );
    }
}
