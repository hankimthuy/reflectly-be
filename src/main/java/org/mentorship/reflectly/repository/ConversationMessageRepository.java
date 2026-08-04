package org.mentorship.reflectly.repository;

import org.mentorship.reflectly.model.ConversationMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationMessageRepository extends JpaRepository<ConversationMessageEntity, String> {

    List<ConversationMessageEntity> findByConversationIdOrderByCreatedDateAsc(String conversationId);

    @Modifying
    @Query("UPDATE ConversationMessageEntity m SET m.content = NULL WHERE m.conversation.id = :conversationId")
    void purgeContentByConversationId(@Param("conversationId") String conversationId);
}
