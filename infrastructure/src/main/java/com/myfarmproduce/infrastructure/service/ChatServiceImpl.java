package com.myfarmproduce.infrastructure.service;

import com.myfarmproduce.application.service.ChatService;
import com.myfarmproduce.domain.entity.ChatMessage;
import com.myfarmproduce.infrastructure.repository.ChatMessageRepository;
import com.myfarmproduce.infrastructure.repository.CustomerRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessages;
    private final CustomerRepository customers;

    public ChatServiceImpl(ChatMessageRepository chatMessages, CustomerRepository customers) {
        this.chatMessages = chatMessages;
        this.customers = customers;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessage> getRecent(int count) {
        List<ChatMessage> recent = new ArrayList<>(chatMessages.findAllByOrderByCreatedAtDesc(PageRequest.of(0, count)));
        Collections.reverse(recent); // oldest first for display
        return recent;
    }

    @Override
    public ChatMessage addMessage(Integer customerId, String senderName, String content) {
        ChatMessage message = new ChatMessage();
        message.setCustomer(customers.getReferenceById(customerId));
        message.setSenderName(senderName);
        message.setContent(content.trim());
        return chatMessages.save(message);
    }
}
