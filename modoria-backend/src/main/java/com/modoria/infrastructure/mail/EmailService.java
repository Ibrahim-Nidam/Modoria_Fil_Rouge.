package com.modoria.infrastructure.mail;

public interface EmailService {
    void sendWelcomeEmail(String to, String name);

    void sendOrderConfirmation(String to, Long orderId, String orderNumber);

    void sendShippingUpdate(String to, String orderNumber, String trackingNumber);

    void sendPasswordReset(String to, String token);
}
