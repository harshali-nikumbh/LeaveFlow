
package com.settribe.leavemanagement.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String recipient, String resetLink) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(senderEmail);
        message.setTo(recipient);
        message.setSubject("LeaveFlow - Password Reset");

        message.setText(
                "Hello,\n\n" +
                        "We received a request to reset your LeaveFlow password.\n\n" +
                        "Click the link below to reset your password:\n" +
                        resetLink + "\n\n" +
                        "This link expires in 15 minutes and can only be used once.\n\n" +
                        "If you did not request this, please ignore this email.\n\n" +
                        "Regards,\nLeaveFlow Team");

        mailSender.send(message);
    }
}
