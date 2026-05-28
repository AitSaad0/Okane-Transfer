package com.okane.service.impl;


import com.okane.service.EmailService;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class EmailServiceImpl implements EmailService {

    @Override
    public void send(
            String to,
            String subject,
            String body
    ) {

        final String username = "youssoufihabib8@gmail.com";
        final String password = "xybf yfzv gici mriz";

        Properties props = new Properties();

        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(
                props,
                new Authenticator() {
                    protected PasswordAuthentication
                    getPasswordAuthentication() {

                        return new PasswordAuthentication(
                                username,
                                password
                        );
                    }
                });

        try {

            Message message = new MimeMessage(session);

            message.setFrom(
                    new InternetAddress(username)
            );

            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(to)
            );

            message.setSubject(subject);

            message.setText(body);

            Transport.send(message);

        } catch (MessagingException e) {

            throw new RuntimeException(e);
        }
    }
}
