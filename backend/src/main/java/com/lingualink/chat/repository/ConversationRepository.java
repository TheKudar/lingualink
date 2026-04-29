package com.lingualink.chat.repository;

import com.lingualink.chat.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Optional<Conversation> findByParticipantOneIdAndParticipantTwoId(Long participantOneId, Long participantTwoId);

    List<Conversation> findByParticipantOneIdOrParticipantTwoIdOrderByCreatedAtDesc(Long participantOneId, Long participantTwoId);
}
