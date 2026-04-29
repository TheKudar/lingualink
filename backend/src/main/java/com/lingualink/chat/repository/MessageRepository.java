package com.lingualink.chat.repository;

import com.lingualink.chat.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByConversationIdOrderBySentAtAsc(Long conversationId, Pageable pageable);
}
