package com.modoria.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
@Profile("dev")
public class DevMailConfig {

    @Bean
    @Primary
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("localhost");
        mailSender.setPort(25);
        mailSender.setUsername("dev@modoria.com");
        mailSender.setPassword("dev");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "false");
        props.put("mail.smtp.starttls.enable", "false");
        props.put("mail.debug", "false");

        // Add timeouts to prevent hanging
        props.put("mail.smtp.connectiontimeout", "2000"); // 2 seconds
        props.put("mail.smtp.timeout", "2000"); // 2 seconds
        props.put("mail.smtp.writetimeout", "2000"); // 2 seconds

        return mailSender;
    }
}
