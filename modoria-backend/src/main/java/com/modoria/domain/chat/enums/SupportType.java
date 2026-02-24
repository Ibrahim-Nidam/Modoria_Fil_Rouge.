package com.modoria.domain.chat.enums;

/**
 * Enum representing the type of support being provided in a chat room.
 */
public enum SupportType {
    /**
     * AI-only support - customer is chatting with the AI bot.
     */
    AI_ONLY,

    /**
     * Customer has requested human support and is waiting in queue.
     */
    WAITING_AGENT,

    /**
     * Human agent has been assigned and is actively handling the chat.
     */
    HUMAN_ASSIGNED
}
