package com.modoria.domain.chat.entity;

import com.modoria.domain.base.BaseEntity;
import com.modoria.domain.chat.enums.MessageSenderType;
import com.modoria.domain.user.entity.User;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * ChatMessage entity representing a message in a chat conversation.
 */
@Entity
@Table(name = "chat_messages", indexes = {
        @Index(name = "idx_chatmsg_room", columnList = "chat_room_id"),
        @Index(name = "idx_chatmsg_sender", columnList = "sender_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ChatMessage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = true)
    private User sender;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "message_type", length = 20)
    @Builder.Default
    private String messageType = "TEXT";

    @Column(name = "is_read")
    @Builder.Default
    private Boolean read = false;

    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;

    @Column(name = "attachment_type", length = 50)
    private String attachmentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", length = 20)
    private MessageSenderType senderType;

    @Column(name = "ai_confidence")
    private Double aiConfidence;

    // Message type constants
    public static final String TYPE_TEXT = "TEXT";
    public static final String TYPE_IMAGE = "IMAGE";
    public static final String TYPE_FILE = "FILE";
    public static final String TYPE_SYSTEM = "SYSTEM";

    // Helper methods
    public void markAsRead() {
        this.read = true;
    }

    public boolean isFromCustomer() {
        return chatRoom != null &&
                chatRoom.getCustomer() != null &&
                sender.getId().equals(chatRoom.getCustomer().getId());
    }

    public boolean isFromSupport() {
        return chatRoom != null &&
                chatRoom.getSupportAgent() != null &&
                sender.getId().equals(chatRoom.getSupportAgent().getId());
    }
}
