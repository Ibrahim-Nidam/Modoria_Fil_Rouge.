package com.modoria.infrastructure.mail.impl;

import com.modoria.infrastructure.mail.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    @Override
    public void sendWelcomeEmail(String to, String name) {
        log.info("Sending welcome email to {}", to);
        String subject = "Welcome to Modoria!";
        String content = String
                .format("<h1>Welcome, %s!</h1><p>We are thrilled to have you join the Modoria community.</p>", name);
        sendHtmlEmail(to, subject, content);
    }

    @Async
    @Override
    public void sendOrderConfirmation(String to, Long orderId, String orderNumber) {
        log.info("Sending order confirmation to {}", to);
        String subject = "Order Confirmation #" + orderNumber;
        String content = String.format(
                "<h1>Order Confirmed!</h1><p>Thank you for your order #%s.</p><p>We are processing it now.</p>",
                orderNumber);
        sendHtmlEmail(to, subject, content);
    }

    @Async
    @Override
    public void sendShippingUpdate(String to, String orderNumber, String trackingNumber) {
        log.info("Sending shipping update for order {}", orderNumber);
        String subject = "Your Order #" + orderNumber + " Has Shipped";
        String content = String.format(
                "<h1>Good News!</h1><p>Your order #%s is on its way.</p><p>Tracking Number: <strong>%s</strong></p>",
                orderNumber, trackingNumber);
        sendHtmlEmail(to, subject, content);
    }

    @Async
    @Override
    public void sendPasswordReset(String to, String token) {
        log.info("Sending password reset to {}", to);
        String subject = "Modoria Password Reset";
        String content = String.format(
                "<h1>Reset Password</h1><p>Use this token to reset your password: <strong>%s</strong></p>", token);
        sendHtmlEmail(to, subject, content);
    }

    private void sendHtmlEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail != null && !fromEmail.isEmpty() ? fromEmail : "noreply@modoria.com");
            if (to != null)
                helper.setTo(to);
            if (subject != null)
                helper.setSubject(subject);
            if (content != null)
                helper.setText(content, true);

            mailSender.send(message);
            log.info("Email sent successfully to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}", to, e);
            // Don't rethrow, just log, as email failure shouldn't rollback transactions
            // usually
        }
    }
}
