package com.nishikatakagi.genzdictionary.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSender {

    private static final String EMAIL = "quocviet242003@gmail.com"; // Thay bằng email Gmail của bạn
    private static final String PASSWORD = "vgmkoxewhdxzuyra"; // Thay bằng mật khẩu ứng dụng

    public static void sendEmail(String recipient, String subject, String body, EmailCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL, PASSWORD);
                }
            });

            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(EMAIL));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
                message.setSubject(subject);
                message.setText(body);
                Transport.send(message);

                // Gửi callback thành công về luồng chính
                handler.post(() -> {
                    if (callback != null) {
                        callback.onEmailSent();
                    }
                });
            } catch (MessagingException e) {
                Log.e("EmailSender", "Error sending email: " + e.getMessage());
                // Gửi callback thất bại về luồng chính
                handler.post(() -> {
                    if (callback != null) {
                        callback.onEmailFailed(e.getMessage());
                    }
                });
            } finally {
                executor.shutdown();
            }
        });
    }

    public interface EmailCallback {
        void onEmailSent();
        void onEmailFailed(String error);
    }
}