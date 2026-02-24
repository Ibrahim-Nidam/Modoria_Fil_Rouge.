package com.modoria.domain.ai.service;

import com.modoria.domain.chat.dto.response.ChatMessageResponse;
import com.modoria.domain.chat.entity.ChatMessage;
import com.modoria.domain.chat.entity.ChatRoom;

import java.util.List;

/**
 * Service for AI chatbot functionality in support system.
 */
public interface AIChatBotService {

    /**
     * Send welcome message when a new chat room is created.
     *
     * @param chatRoomId ID of the newly created chat room
     */
    void sendWelcomeMessage(Long chatRoomId);

    /**
     * Handle incoming customer message and generate AI response.
     *
     * @param chatRoomId      ID of the chat room
     * @param customerMessage The message from the customer
     */
    void handleCustomerMessage(Long chatRoomId, String customerMessage);

    /**
     * Generate AI response based on customer message and room context.
     *
     * @param room            The chat room
     * @param customerMessage The customer's question
     * @return AI-generated response
     */
    ChatMessageResponse generateAIResponse(ChatRoom room, String customerMessage);

    /**
     * Check if customer message is requesting human escalation.
     *
     * @param message Customer message
     * @return true if escalation detected
     */
    boolean isEscalationRequest(String message);

    /**
     * Build conversation history context for AI prompt.
     *
     * @param messages List of chat messages
     * @return Formatted conversation history
     */
    String buildConversationContext(List<ChatMessage> messages);
}
