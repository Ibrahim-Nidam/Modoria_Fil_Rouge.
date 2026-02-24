package com.modoria.domain.chat.repository;

import com.modoria.domain.chat.entity.ChatRoom;
import com.modoria.domain.chat.enums.ChatRoomStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ChatRoom entity operations.
 */
@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Page<ChatRoom> findByCustomerId(Long customerId, Pageable pageable);

    Page<ChatRoom> findBySupportAgentId(Long supportAgentId, Pageable pageable);

    Page<ChatRoom> findByStatus(ChatRoomStatus status, Pageable pageable);

    Page<ChatRoom> findBySupportAgentIsNullAndStatus(ChatRoomStatus status, Pageable pageable);

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.supportAgent IS NULL AND cr.status = 'OPEN'")
    List<ChatRoom> findOpenRoomsWithoutAgent();

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.id = :id")
    Optional<ChatRoom> findByIdWithMessages(@Param("id") Long id);

    long countByStatus(ChatRoomStatus status);

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.customer.id = :customerId")
    List<ChatRoom> findActiveRoomsByCustomerId(@Param("customerId") Long customerId);
}



