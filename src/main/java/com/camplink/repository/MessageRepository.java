package com.camplink.repository;

import com.camplink.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, String> {
    List<Message> findByConversationIdOrderBySentAtAsc(String conversationId);
}
