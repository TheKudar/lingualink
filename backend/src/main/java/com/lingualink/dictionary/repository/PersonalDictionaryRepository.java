package com.lingualink.dictionary.repository;

import com.lingualink.dictionary.entity.PersonalDictionary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PersonalDictionaryRepository extends JpaRepository<PersonalDictionary, Long> {
    List<PersonalDictionary> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<PersonalDictionary> findByIdAndUserId(Long id, Long userId);
}
