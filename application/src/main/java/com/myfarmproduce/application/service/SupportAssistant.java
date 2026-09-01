package com.myfarmproduce.application.service;

import com.myfarmproduce.domain.entity.SupportMessage;

import java.util.List;

public interface SupportAssistant {
    String getReply(Integer customerId, String latestMessage, List<SupportMessage> history);
}
