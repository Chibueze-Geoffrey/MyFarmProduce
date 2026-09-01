package com.myfarmproduce.application.service;

import com.myfarmproduce.domain.entity.SupportMessage;
import com.myfarmproduce.domain.entity.SupportTicket;

import java.util.List;
import java.util.Optional;

public interface SupportService {
    SupportTicket startTicket(Integer customerId, String firstMessage);

    Optional<SupportTicket> getTicket(Integer ticketId, Integer restrictToCustomerId);

    List<SupportTicket> getCustomerTickets(Integer customerId);

    List<SupportTicket> getAllTickets();

    SupportMessage sendCustomerMessage(Integer ticketId, Integer customerId, String content);
}
