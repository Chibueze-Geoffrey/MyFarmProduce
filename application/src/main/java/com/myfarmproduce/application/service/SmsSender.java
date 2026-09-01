package com.myfarmproduce.application.service;

public interface SmsSender {
    void send(String phone, String message);
}
