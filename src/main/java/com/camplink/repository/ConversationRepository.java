package com.camplink.repository;

import com.camplink.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, String> {

    @Query("SELECT c FROM Conversation c JOIN c.participants p WHERE p.id = :userId ORDER BY c.updatedAt DESC NULLS LAST")
    List<Conversation> findByParticipantId(@Param("userId") String userId);
}
