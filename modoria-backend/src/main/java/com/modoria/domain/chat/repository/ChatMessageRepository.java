package com.modoria.domain.chat.repository;

import com.modoria.domain.chat.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for ChatMessage entity operations.
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    Page<ChatMessage> findByChatRoomIdOrderByCreatedAtAsc(Long chatRoomId, Pageable pageable);

    List<ChatMessage> findByChatRoomIdOrderByCreatedAtAsc(Long chatRoomId);

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom.id = :roomId AND cm.read = false")
    List<ChatMessage> findUnreadMessages(@Param("roomId") Long roomId);

    @Modifying
    @Query("UPDATE ChatMessage cm SET cm.read = true WHERE cm.chatRoom.id = :roomId AND cm.read = false")
    void markAllAsRead(@Param("roomId") Long roomId);

    List<ChatMessage> findByChatRoomIdAndReadFalse(Long chatRoomId);

    long countByChatRoomIdAndReadFalse(Long chatRoomId);

    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.chatRoom.id = :roomId AND cm.read = false")
    long countUnreadMessages(@Param("roomId") Long roomId);
}


