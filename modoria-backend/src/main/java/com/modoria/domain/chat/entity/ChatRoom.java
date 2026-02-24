package com.modoria.domain.chat.entity;

import com.modoria.domain.base.BaseEntity;
import com.modoria.domain.chat.enums.ChatRoomPriority;
import com.modoria.domain.chat.enums.ChatRoomStatus;
import com.modoria.domain.chat.enums.SupportType;
import com.modoria.domain.user.entity.User;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * ChatRoom entity representing a conversation between customer and support.
 */
@Entity
@Table(name = "chat_rooms", indexes = {
        @Index(name = "idx_chatroom_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ChatRoom extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "support_agent_id")
    private User supportAgent;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();

    @Column(name = "subject", length = 255)
    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private ChatRoomStatus status = ChatRoomStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20)
    @Builder.Default
    private ChatRoomPriority priority = ChatRoomPriority.NORMAL;

    @Column(name = "is_read_by_customer")
    @Builder.Default
    private Boolean isReadByCustomer = true;

    @Column(name = "is_read_by_support")
    @Builder.Default
    private Boolean isReadBySupport = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "support_type", length = 20)
    @Builder.Default
    private SupportType supportType = SupportType.AI_ONLY;

    @Column(name = "human_escalation_requested")
    @Builder.Default
    private Boolean humanEscalationRequested = false;

    @Column(name = "escalation_reason", length = 500)
    private String escalationReason;

    // Helper methods
    public void addMessage(ChatMessage message) {
        messages.add(message);
        message.setChatRoom(this);
    }

    public ChatMessage getLastMessage() {
        return messages.isEmpty() ? null : messages.get(messages.size() - 1);
    }

    public int getMessageCount() {
        return messages.size();
    }

    public void assignAgent(User agent) {
        this.supportAgent = agent;
        this.status = ChatRoomStatus.IN_PROGRESS;
    }

    public void resolve() {
        this.status = ChatRoomStatus.RESOLVED;
    }

    public void close() {
        this.status = ChatRoomStatus.CLOSED;
    }
}
