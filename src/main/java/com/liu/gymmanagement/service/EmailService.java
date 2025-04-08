package com.liu.gymmanagement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    private final Map<String, String> verificationCodes = new ConcurrentHashMap<>();

    public void sendVerificationCode(String email) {
        String code = generateCode();
        verificationCodes.put(email, code);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("注册验证码");
        message.setText("欢迎注册BUPT健身预约系统，您的验证码是：" + code + "，5分钟内有效。");

        mailSender.send(message);
    }

    public boolean verifyCode(String email, String inputCode) {
        String correctCode = verificationCodes.get(email);
        return inputCode.equals(correctCode);
    }

    private String generateCode() {
        return String.valueOf(new Random().nextInt(900000) + 100000); // 6位验证码
    }
}
