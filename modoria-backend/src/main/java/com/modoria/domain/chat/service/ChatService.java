package com.modoria.domain.chat.service;

import com.modoria.domain.chat.dto.request.CreateChatRoomRequest;
import com.modoria.domain.chat.dto.request.EscalationRequest;
import com.modoria.domain.chat.dto.request.SendMessageRequest;
import com.modoria.domain.chat.dto.response.ChatMessageResponse;
import com.modoria.domain.chat.dto.response.ChatRoomResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for chat operations.
 */
public interface ChatService {

    ChatRoomResponse createRoom(CreateChatRoomRequest request);

    ChatRoomResponse getRoomById(Long id);

    Page<ChatRoomResponse> getCurrentUserRooms(Pageable pageable);

    Page<ChatRoomResponse> getUnassignedRooms(Pageable pageable);

    Page<ChatRoomResponse> getSupportAgentRooms(Pageable pageable);

    List<ChatMessageResponse> getRoomMessages(Long roomId);

    ChatMessageResponse sendMessage(Long roomId, SendMessageRequest request);

    ChatRoomResponse assignAgent(Long roomId);

    ChatRoomResponse resolveRoom(Long roomId);

    ChatRoomResponse closeRoom(Long roomId);

    void markMessagesAsRead(Long roomId);

    long getUnreadCount(Long roomId);

    ChatRoomResponse requestEscalation(Long roomId, EscalationRequest request);
}
