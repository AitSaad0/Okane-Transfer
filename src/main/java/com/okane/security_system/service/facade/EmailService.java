package com.okane.security_system.service.facade;

public interface EmailService {
    void send(
            String to,
            String subject,
            String body
    );
}
