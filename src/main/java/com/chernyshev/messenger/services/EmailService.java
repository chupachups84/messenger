package com.chernyshev.messenger.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String username;
    public void sendEmailConfirmationEmail(String toEmail,String confirmationToken) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(username);
        mailMessage.setTo(toEmail);
        mailMessage.setSubject("Подтверждение адреса электронной почты");
        mailMessage.setText("Для подтверждения адреса электронной почты, перейдите по следующей ссылке:\n"
                + "http://localhost:8080/api/v1/auth/confirmation?confirmationToken=" + confirmationToken);
        mailSender.send(mailMessage);
    }
}
