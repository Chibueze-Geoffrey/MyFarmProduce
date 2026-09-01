package com.myfarmproduce.application.service;

public interface EmailSender {
    void send(String to, String subject, String htmlBody);
}
