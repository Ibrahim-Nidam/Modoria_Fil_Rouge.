package com.modoria.domain.chat.enums;

/**
 * Enum representing who sent a message in the chat.
 */
public enum MessageSenderType {
    /**
     * Message sent by AI bot.
     */
    AI,

    /**
     * Message sent by customer.
     */
    CUSTOMER,

    /**
     * Message sent by support agent (human).
     */
    SUPPORT_AGENT,

    /**
     * System-generated message (e.g., "Agent joined", "Added to queue").
     */
    SYSTEM
}
