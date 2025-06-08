package com.esa.moviestar.libraries;

import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;

public class EmailService {
    private String senderEmail;
    private String senderPassword;
    private final Properties emailProperties;
    private Session mailSession;

    public EmailService() {
        senderEmail = "moviestarclient@gmail.com";
        senderPassword = "eenu rbsi obnl hzha";
        this.emailProperties = new Properties();

        emailProperties.put("mail.smtp.host", "smtp.gmail.com");

        emailProperties.put("mail.smtp.port", "587");

        emailProperties.put("mail.smtp.auth", "true");

        emailProperties.put("mail.smtp.starttls.enable", "true");

        this.mailSession = Session.getInstance(emailProperties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });
    }
    public void sendEmail(String recipientEmail, String subject, String body) throws MessagingException {
        try {
            MimeMessage message = new MimeMessage(this.mailSession);
            message.setFrom(new InternetAddress(this.senderEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
            message.setSubject(subject, "UTF-8");

            // Set proper headers
            message.setHeader("Content-Type",  "text/plain; charset=UTF-8");
            message.setSentDate( new java.util.Date());
            message.setReplyTo(InternetAddress.parse( this.senderEmail));

            // Set body with UTF-8
            message.setContent(body, "text/plain; charset=UTF-8");

            Transport.send(message);
            System.out.println("EmailService: Email sent successfully to " + recipientEmail);
        } catch (MessagingException e) {
            System.err.println("EmailService: Failed to send email: " + e.getMessage());
            throw e;
        }
    }

}
