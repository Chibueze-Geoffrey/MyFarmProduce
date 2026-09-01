package com.myfarmproduce.application.service;

import com.myfarmproduce.domain.entity.ChatMessage;

import java.util.List;

public interface ChatService {
    List<ChatMessage> getRecent(int count);

    ChatMessage addMessage(Integer customerId, String senderName, String content);
}
