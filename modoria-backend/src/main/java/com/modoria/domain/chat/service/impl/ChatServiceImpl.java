package com.modoria.domain.chat.service.impl;

import com.modoria.domain.chat.entity.ChatMessage;
import com.modoria.domain.chat.entity.ChatRoom;
import com.modoria.domain.user.entity.User;
import com.modoria.domain.chat.dto.request.CreateChatRoomRequest;
import com.modoria.domain.chat.dto.request.SendMessageRequest;
import com.modoria.domain.chat.dto.response.ChatMessageResponse;
import com.modoria.domain.chat.dto.response.ChatRoomResponse;
import com.modoria.infrastructure.exceptions.resource.ResourceNotFoundException;
import com.modoria.domain.chat.mapper.ChatMapper;
import com.modoria.domain.chat.repository.ChatMessageRepository;
import com.modoria.domain.chat.repository.ChatRoomRepository;
import com.modoria.domain.user.repository.UserRepository;
import com.modoria.domain.chat.service.ChatService;
import com.modoria.domain.ai.service.AIChatBotService;
import com.modoria.domain.support.service.SupportQueueService;
import com.modoria.domain.chat.dto.request.EscalationRequest;
import com.modoria.domain.chat.enums.SupportType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.modoria.domain.chat.enums.ChatRoomPriority;
import com.modoria.domain.chat.enums.ChatRoomStatus;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ChatServiceImpl implements ChatService {

        private final ChatRoomRepository chatRoomRepository;
        private final ChatMessageRepository chatMessageRepository;
        private final UserRepository userRepository;
        private final ChatMapper chatMapper;
        private final SimpMessagingTemplate messagingTemplate; // For real-time updates
        private final AIChatBotService aiChatBotService;
        private final SupportQueueService supportQueueService;
        private final com.modoria.domain.notification.service.NotificationService notificationService;

        @Override
        public ChatRoomResponse createRoom(CreateChatRoomRequest request) {
                log.info("Creating chat room with subject: {}", request.getSubject());
                User customer = getCurrentUser();

                ChatRoom room = ChatRoom.builder()
                                .customer(customer)
                                .subject(request.getSubject())
                                .priority(ChatRoomPriority
                                                .valueOf(request.getPriority() != null
                                                                ? request.getPriority().toUpperCase()
                                                                : "MEDIUM"))
                                .status(ChatRoomStatus.OPEN)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();

                room = chatRoomRepository.save(room);

                // Notify admins/support (e.g., via websocket topic /topic/admin/rooms)
                messagingTemplate.convertAndSend("/topic/admin/rooms", chatMapper.toResponse(room));

                // Notify Support Roles via Notification Service
                notificationService.notifyRole("ROLE_SUPPORT",
                                "New Chat Room",
                                "A new chat room has been created: " + room.getSubject(),
                                com.modoria.domain.notification.enums.NotificationType.NEW_CHAT_MESSAGE,
                                "/admin/chats/" + room.getId(),
                                "{\"roomId\":" + room.getId() + "}");

                notificationService.notifyRole("ROLE_ADMIN",
                                "New Chat Room",
                                "A new chat room has been created: " + room.getSubject(),
                                com.modoria.domain.notification.enums.NotificationType.NEW_CHAT_MESSAGE,
                                "/admin/chats/" + room.getId(),
                                "{\"roomId\":" + room.getId() + "}");

                // Trigger AI Welcome Message
                aiChatBotService.sendWelcomeMessage(room.getId());

                log.info("Chat room created with ID: {}", room.getId());
                return chatMapper.toResponse(room);
        }

        @Override
        @Transactional(readOnly = true)
        public ChatRoomResponse getRoomById(Long id) {
                ChatRoom room = chatRoomRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("ChatRoom", "id", id));
                return chatMapper.toResponse(room);
        }

        @Override
        @Transactional(readOnly = true)
        public Page<ChatRoomResponse> getCurrentUserRooms(Pageable pageable) {
                User user = getCurrentUser();
                return chatRoomRepository.findByCustomerId(user.getId(), pageable)
                                .map(chatMapper::toResponse);
        }

        @Override
        @Transactional(readOnly = true)
        public Page<ChatRoomResponse> getUnassignedRooms(Pageable pageable) {
                return chatRoomRepository.findBySupportAgentIsNullAndStatus(
                                ChatRoomStatus.OPEN, pageable)
                                .map(chatMapper::toResponse);
        }

        @Override
        @Transactional(readOnly = true)
        public Page<ChatRoomResponse> getSupportAgentRooms(Pageable pageable) {
                User agent = getCurrentUser();
                return chatRoomRepository.findBySupportAgentId(agent.getId(), pageable)
                                .map(chatMapper::toResponse);
        }

        @Override
        @Transactional(readOnly = true)
        public List<ChatMessageResponse> getRoomMessages(Long roomId) {
                if (!chatRoomRepository.existsById(roomId)) {
                        throw new ResourceNotFoundException("ChatRoom", "id", roomId);
                }
                return chatMapper.toMessageResponseList(
                                chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(roomId));
        }

        @Override
        public ChatMessageResponse sendMessage(Long roomId, SendMessageRequest request) {
                User sender = getCurrentUser();
                ChatRoom room = chatRoomRepository.findById(roomId)
                                .orElseThrow(() -> new ResourceNotFoundException("ChatRoom", "id", roomId));

                // Validation: Ensure sender is participant
                // omitted for brevity

                ChatMessage message = ChatMessage.builder()
                                .chatRoom(room)
                                .sender(sender)
                                .content(request.getContent())
                                .createdAt(LocalDateTime.now())
                                .read(false)
                                .build();

                message = chatMessageRepository.save(message);

                room.setUpdatedAt(LocalDateTime.now());
                chatRoomRepository.save(room);

                ChatMessageResponse response = chatMapper.toMessageResponse(message);

                // Broadcast to room topic
                messagingTemplate.convertAndSend("/topic/room/" + roomId, response);

                // Trigger AI Response if applicable
                aiChatBotService.handleCustomerMessage(roomId, request.getContent());

                log.debug("Message sent to room {}: {}", roomId, message.getId());
                return response;
        }

        @Override
        public ChatRoomResponse assignAgent(Long roomId) {
                User agent = getCurrentUser(); // Assuming current user is Support Agent
                ChatRoom room = chatRoomRepository.findById(roomId)
                                .orElseThrow(() -> new ResourceNotFoundException("ChatRoom", "id", roomId));

                room.assignAgent(agent);
                room = chatRoomRepository.save(room);

                // Notify Customer
                notificationService.sendNotification(room.getCustomer(),
                                "Agent Assigned",
                                agent.getFullName() + " has joined the chat.",
                                com.modoria.domain.notification.enums.NotificationType.NEW_CHAT_MESSAGE,
                                "/chats/" + room.getId(),
                                "{\"roomId\":" + room.getId() + ",\"agentId\":" + agent.getId() + "}");

                log.info("Agent {} assigned to room {}", agent.getEmail(), roomId);
                return chatMapper.toResponse(room);
        }

        @Override
        public ChatRoomResponse resolveRoom(Long roomId) {
                ChatRoom room = chatRoomRepository.findById(roomId)
                                .orElseThrow(() -> new ResourceNotFoundException("ChatRoom", "id", roomId));
                room.resolve();
                return chatMapper.toResponse(chatRoomRepository.save(room));
        }

        @Override
        public ChatRoomResponse closeRoom(Long roomId) {
                ChatRoom room = chatRoomRepository.findById(roomId)
                                .orElseThrow(() -> new ResourceNotFoundException("ChatRoom", "id", roomId));
                room.close();
                return chatMapper.toResponse(chatRoomRepository.save(room));
        }

        @Override
        public void markMessagesAsRead(Long roomId) {
                // Logic to mark messages as read for the *other* party
                // For simplicity, mark all as read
                List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdAndReadFalse(roomId);
                messages.forEach(m -> m.setRead(true));
                chatMessageRepository.saveAll(messages);
        }

        @Override
        public long getUnreadCount(Long roomId) {
                return chatMessageRepository.countByChatRoomIdAndReadFalse(roomId);
        }

        @Override
        public ChatRoomResponse requestEscalation(Long roomId, EscalationRequest request) {
                ChatRoom room = chatRoomRepository.findById(roomId)
                                .orElseThrow(() -> new ResourceNotFoundException("ChatRoom", "id", roomId));

                // Update room state
                room.setSupportType(SupportType.WAITING_AGENT);
                room.setHumanEscalationRequested(true);
                room.setEscalationReason(request.getReason());

                room = chatRoomRepository.save(room);

                // Add to support queue
                supportQueueService.addToQueue(room);

                // Notify customer via WebSocket (optional, as frontend gets updated room)
                messagingTemplate.convertAndSend("/topic/room/" + roomId, chatMapper.toResponse(room));

                // Notify Support Agents of escalation
                notificationService.notifyRole("ROLE_SUPPORT",
                                "Chat Escalation Requested",
                                "Room #" + roomId + " requires human assistance.",
                                com.modoria.domain.notification.enums.NotificationType.CHAT_ESCALATION,
                                "/admin/chats/" + roomId,
                                "{\"roomId\":" + roomId + "}");

                log.warn("Escalation requested for room {}: {}", roomId, request.getReason());
                return chatMapper.toResponse(room);
        }

        private User getCurrentUser() {
                String email = SecurityContextHolder.getContext().getAuthentication().getName();
                return userRepository.findByEmail(email)
                                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        }
}
