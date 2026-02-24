package com.modoria.domain.chat.mapper;

import com.modoria.domain.chat.entity.ChatRoom;
import com.modoria.domain.chat.entity.ChatMessage;
import com.modoria.domain.chat.dto.response.ChatRoomResponse;
import com.modoria.domain.chat.dto.response.ChatMessageResponse;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * Simple mapper for Chat entities.
 */
@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface ChatMapper {

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", ignore = true)
    @Mapping(target = "supportAgentId", source = "supportAgent.id")
    @Mapping(target = "supportAgentName", ignore = true)
    @Mapping(target = "messageCount", ignore = true)
    @Mapping(target = "unreadCount", ignore = true)
    @Mapping(target = "lastMessageAt", ignore = true)
    @Mapping(target = "recentMessages", ignore = true)
    ChatRoomResponse toResponse(ChatRoom chatRoom);

    @Mapping(target = "chatRoomId", source = "chatRoom.id")
    @Mapping(target = "senderId", expression = "java(chatMessage.getSender() != null ? chatMessage.getSender().getId() : null)")
    @Mapping(target = "senderName", ignore = true)
    @Mapping(target = "senderAvatar", expression = "java(chatMessage.getSender() != null ? chatMessage.getSender().getAvatarUrl() : null)")
    ChatMessageResponse toMessageResponse(ChatMessage chatMessage);

    List<ChatRoomResponse> toResponseList(List<ChatRoom> chatRooms);

    List<ChatMessageResponse> toMessageResponseList(List<ChatMessage> messages);

    @AfterMapping
    default void calculateRoomFields(@MappingTarget ChatRoomResponse response, ChatRoom chatRoom) {
        if (chatRoom.getCustomer() != null) {
            response.setCustomerName(chatRoom.getCustomer().getFullName());
        }
        if (chatRoom.getSupportAgent() != null) {
            response.setSupportAgentName(chatRoom.getSupportAgent().getFullName());
        }
        response.setMessageCount(chatRoom.getMessageCount());

        if (chatRoom.getLastMessage() != null) {
            response.setLastMessageAt(chatRoom.getLastMessage().getCreatedAt());
        } else {
            response.setLastMessageAt(chatRoom.getCreatedAt());
        }
    }

    @AfterMapping
    default void calculateMessageFields(@MappingTarget ChatMessageResponse response, ChatMessage chatMessage) {
        if (chatMessage.getSender() != null) {
            response.setSenderName(chatMessage.getSender().getFullName());
        } else if (chatMessage.getSenderType() != null) {
            response.setSenderName(chatMessage.getSenderType().name()); // "AI" or "SYSTEM"
        } else {
            response.setSenderName("Unknown");
        }
    }
}
