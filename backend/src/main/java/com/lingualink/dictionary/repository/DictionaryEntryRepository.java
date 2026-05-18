package com.lingualink.dictionary.repository;

import com.lingualink.dictionary.entity.DictionaryEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DictionaryEntryRepository extends JpaRepository<DictionaryEntry, Long> {
    Optional<DictionaryEntry> findByIdAndDictionaryId(Long id, Long dictionaryId);
}
