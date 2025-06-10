package com.esa.moviestar.libraries;

import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;
//This class manages all the server communications and email sending, using jakarta mail
public class EmailService {
    private String senderEmail;
    private String senderPassword;
    private final Properties emailProperties;
    private Session mailSession;

    public EmailService() {
        senderEmail = "moviestarclient@gmail.com";
        senderPassword = "eenu rbsi obnl hzha";
        this.emailProperties = new Properties();
        //Setting the gmail's smtp server
        emailProperties.put("mail.smtp.host", "smtp.gmail.com");
        //Submission port
        emailProperties.put("mail.smtp.port", "587");
        //Authentication requirement
        emailProperties.put("mail.smtp.auth", "true");

        emailProperties.put("mail.smtp.starttls.enable", "true");

        this.mailSession = Session.getInstance(emailProperties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });
    }

    public void sendEmail(String recipientEmail, String verificationCode) throws MessagingException{
        try {
            String subject = "Your MovieStar Password Reset Code";
            String body = String.format("""
                Hello,
                
                We received a request to reset your MovieStar account password.
                
                Your verification code is: %s
                
                If you did not request this, please ignore this message.
                
                Thank you,
                The MovieStar Team
                """, verificationCode);
            //Gets the message from the session and sends it to the user email
            MimeMessage message = new MimeMessage(this.mailSession);
            message.setContent(body, "text/plain; charset=UTF-8");
            message.setFrom(new InternetAddress(this.senderEmail));

            message.setReplyTo(InternetAddress.parse( this.senderEmail));
            message.setHeader("Content-Type",  "text/plain; charset=UTF-8");
            message.setSentDate( new java.util.Date());
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
            message.setSubject(subject, "UTF-8");


            Transport.send(message);

            System.out.println("EmailService: Email sent successfully to " + recipientEmail);
        }
        catch (MessagingException e) {
            System.err.println("EmailService: Failed to send email: " + e.getMessage());
            throw e;
        }
    }

}
