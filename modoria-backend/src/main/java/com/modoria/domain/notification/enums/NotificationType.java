package com.modoria.domain.notification.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enumeration for types of notifications.
 */
@Getter
@RequiredArgsConstructor
public enum NotificationType {
    ORDER_STATUS_UPDATE("Order status has changed"),
    NEW_CHAT_MESSAGE("New message in your chat room"),
    CHAT_ESCALATION("Chat has been escalated to a human agent"),
    LOW_STOCK_ALERT("Inventory item is below threshold"),
    AI_INSIGHT_READY("AI Business Insight report is ready"),
    SYSTEM_ALERT("General system notification");

    private final String defaultMessage;
}
