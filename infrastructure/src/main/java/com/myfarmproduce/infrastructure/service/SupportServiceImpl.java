package com.myfarmproduce.infrastructure.service;

import com.myfarmproduce.application.service.SupportAssistant;
import com.myfarmproduce.application.service.SupportService;
import com.myfarmproduce.domain.entity.SupportMessage;
import com.myfarmproduce.domain.entity.SupportTicket;
import com.myfarmproduce.domain.enums.SupportSender;
import com.myfarmproduce.domain.enums.SupportTicketStatus;
import com.myfarmproduce.infrastructure.repository.CustomerRepository;
import com.myfarmproduce.infrastructure.repository.SupportMessageRepository;
import com.myfarmproduce.infrastructure.repository.SupportTicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SupportServiceImpl implements SupportService {

    private final SupportTicketRepository tickets;
    private final SupportMessageRepository messages;
    private final CustomerRepository customers;
    private final SupportAssistant assistant;

    public SupportServiceImpl(SupportTicketRepository tickets, SupportMessageRepository messages,
                               CustomerRepository customers, SupportAssistant assistant) {
        this.tickets = tickets;
        this.messages = messages;
        this.customers = customers;
        this.assistant = assistant;
    }

    @Override
    public SupportTicket startTicket(Integer customerId, String firstMessage) {
        String subject = firstMessage.trim();
        if (subject.length() > 60) subject = subject.substring(0, 60) + "…";

        SupportTicket ticket = new SupportTicket();
        ticket.setCustomer(customers.getReferenceById(customerId));
        ticket.setSubject(StringUtils.hasText(subject) ? subject : "Support request");
        ticket.setStatus(SupportTicketStatus.Open);
        tickets.save(ticket);

        sendCustomerMessage(ticket.getId(), customerId, firstMessage);
        return ticket;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SupportTicket> getTicket(Integer ticketId, Integer restrictToCustomerId) {
        Optional<SupportTicket> ticket = restrictToCustomerId == null
                ? tickets.findById(ticketId)
                : tickets.findByIdAndCustomerId(ticketId, restrictToCustomerId);
        ticket.ifPresent(t -> t.setMessages(t.getMessages().stream()
                .sorted(Comparator.comparing(SupportMessage::getCreatedAt)).toList()));
        return ticket;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicket> getCustomerTickets(Integer customerId) {
        return tickets.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicket> getAllTickets() {
        return tickets.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public SupportMessage sendCustomerMessage(Integer ticketId, Integer customerId, String content) {
        SupportTicket ticket = tickets.findByIdAndCustomerId(ticketId, customerId)
                .orElseThrow(() -> new IllegalStateException("Ticket not found."));

        SupportMessage customerMsg = new SupportMessage();
        customerMsg.setTicket(ticket);
        customerMsg.setSender(SupportSender.Customer);
        customerMsg.setContent(content.trim());
        ticket.getMessages().add(customerMsg);
        messages.save(customerMsg);

        List<SupportMessage> history = ticket.getMessages().stream()
                .sorted(Comparator.comparing(SupportMessage::getCreatedAt)).toList();
        String replyText = assistant.getReply(customerId, content, history);

        SupportMessage reply = new SupportMessage();
        reply.setTicket(ticket);
        reply.setSender(SupportSender.Assistant);
        reply.setContent(replyText);
        return messages.save(reply);
    }
}
