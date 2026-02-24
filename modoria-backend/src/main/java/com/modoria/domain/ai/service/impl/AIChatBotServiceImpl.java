package com.modoria.domain.ai.service.impl;

import com.modoria.domain.ai.service.AIChatBotService;
import com.modoria.domain.ai.service.OllamaService;
import com.modoria.domain.ai.service.OrderKnowledgeService;
import com.modoria.domain.ai.service.ProductKnowledgeService;
import com.modoria.domain.chat.dto.response.ChatMessageResponse;
import com.modoria.domain.chat.entity.ChatMessage;
import com.modoria.domain.chat.entity.ChatRoom;
import com.modoria.domain.chat.enums.MessageSenderType;
import com.modoria.domain.chat.enums.SupportType;
import com.modoria.domain.chat.mapper.ChatMapper;
import com.modoria.domain.chat.repository.ChatMessageRepository;
import com.modoria.domain.chat.repository.ChatRoomRepository;
import com.modoria.domain.product.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIChatBotServiceImpl implements AIChatBotService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final OllamaService ollamaService;
    private final ProductKnowledgeService productKnowledgeService;
    private final OrderKnowledgeService orderKnowledgeService;
    private final ChatMapper chatMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${modoria.ai.enabled:true}")
    private boolean aiEnabled;

    @Override
    @Async
    @Transactional
    public void sendWelcomeMessage(Long chatRoomId) {
        if (!aiEnabled)
            return;

        try {
            // Give a small delay to ensure client is subscribed
            // Thread.sleep(500); // Bad practice in prod, but ok for simpler handling here

            ChatRoom room = chatRoomRepository.findById(chatRoomId).orElse(null);
            if (room == null)
                return;

            String welcomeText = "Hello! I'm Modoria's AI assistant. " +
                    "I can help you with product information, sizing, availability, and general store questions. " +
                    "How can I assist you today?";

            ChatMessage welcomeMsg = ChatMessage.builder()
                    .chatRoom(room)
                    .senderType(MessageSenderType.AI)
                    .sender(null) // AI message has no user sender
                    .content(welcomeText)
                    .createdAt(LocalDateTime.now())
                    .read(false)
                    .aiConfidence(1.0)
                    .build();

            chatMessageRepository.save(welcomeMsg);

            // Broadcast
            ChatMessageResponse response = chatMapper.toMessageResponse(welcomeMsg);
            messagingTemplate.convertAndSend("/topic/room/" + chatRoomId, response);

        } catch (Exception e) {
            log.error("Error sending welcome message", e);
        }
    }

    @Override
    @Async
    @Transactional
    public void handleCustomerMessage(Long chatRoomId, String customerMessage) {
        if (!aiEnabled)
            return;

        ChatRoom room = chatRoomRepository.findById(chatRoomId).orElse(null);
        if (room == null || room.getSupportType() != SupportType.AI_ONLY) {
            log.info("AI skipping message for room {}: Not in AI_ONLY mode (SupportType: {})", chatRoomId,
                    room != null ? room.getSupportType() : "NULL");
            return;
        }

        // Logic check: Ensure the last message was indeed from a customer
        List<ChatMessage> messages = room.getMessages();
        if (!messages.isEmpty()) {
            ChatMessage lastMessage = messages.get(messages.size() - 1);
            if (lastMessage.getSenderType() == MessageSenderType.AI) {
                log.debug("AI skipping response to its own message in room {}", chatRoomId);
                return;
            }
        }

        try {
            // Check for escalation request but don't auto-act, just log or tag
            // Real escalation happens via specific endpoint, so we just generate response
            // normally
            // possibly suggesting human if confidence low or keywords matched.

            ChatMessageResponse response = generateAIResponse(room, customerMessage);

            ChatMessage aiMsg = ChatMessage.builder()
                    .chatRoom(room)
                    .senderType(MessageSenderType.AI)
                    .sender(null)
                    .content(response.getContent())
                    .createdAt(LocalDateTime.now())
                    .read(false)
                    .aiConfidence(response.getAiConfidence())
                    .build();

            chatMessageRepository.save(aiMsg);

            ChatMessageResponse finalResponse = chatMapper.toMessageResponse(aiMsg);
            messagingTemplate.convertAndSend("/topic/room/" + chatRoomId, finalResponse);

        } catch (Exception e) {
            log.error("Error handling customer message for AI", e);
        }
    }

    @Override
    public ChatMessageResponse generateAIResponse(ChatRoom room, String customerMessage) {
        // 1. Get Conversation History
        List<ChatMessage> history = chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(room.getId());
        String historyContext = buildConversationContext(history);
        log.debug("Built conversation context with {} messages", history.size());

        // 2. Intent Detection & Knowledge Retrieval
        String knowledgeContext = "";

        if (isOrderRelated(customerMessage)) {
            knowledgeContext = orderKnowledgeService.getOrderHistoryContext(room.getCustomer().getId());
        }

        List<Product> products = productKnowledgeService.findRelevantProducts(customerMessage);
        if (!products.isEmpty()) {
            String productContext = productKnowledgeService.buildProductContext(products);
            knowledgeContext = knowledgeContext.isEmpty() ? productContext
                    : knowledgeContext + "\n\nRelevant Products:\n" + productContext;
        }

        // 3. Generate Answer
        String fullContext = historyContext;
        if (!knowledgeContext.isEmpty()) {
            fullContext += "\n\nKnowledge Base:\n" + knowledgeContext;
        }

        log.debug("Sending prompt to Ollama with full context length: {}", fullContext.length());
        String answer = ollamaService.generateResponse(customerMessage, fullContext);

        return ChatMessageResponse.builder()
                .content(answer)
                .senderType(MessageSenderType.AI.name())
                .aiConfidence(0.95)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Override
    public boolean isEscalationRequest(String message) {
        return ollamaService.detectEscalationRequest(message);
    }

    @Override
    public String buildConversationContext(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty())
            return "";

        int start = Math.max(0, messages.size() - 10);
        List<ChatMessage> recent = messages.subList(start, messages.size());

        return recent.stream()
                .map(m -> {
                    String role = m.getSenderType() == MessageSenderType.AI ? "AI" : "Customer";
                    if (m.getSenderType() == null && m.getSender() != null) {
                        // Fallback for old messages
                        role = m.isFromSupport() ? "Support" : "Customer";
                    }
                    return role + ": " + m.getContent();
                })
                .collect(Collectors.joining("\n"));
    }

    private boolean isOrderRelated(String message) {
        String msg = message.toLowerCase();
        return msg.contains("order") || msg.contains("track") || msg.contains("shipping") ||
                msg.contains("bought") || msg.contains("purchase") || msg.contains("delivery");
    }
}
