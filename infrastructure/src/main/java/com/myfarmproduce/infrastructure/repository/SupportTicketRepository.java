package com.myfarmproduce.infrastructure.repository;

import com.myfarmproduce.domain.entity.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Integer> {
    List<SupportTicket> findByCustomerIdOrderByCreatedAtDesc(Integer customerId);

    List<SupportTicket> findAllByOrderByCreatedAtDesc();

    Optional<SupportTicket> findByIdAndCustomerId(Integer id, Integer customerId);
}
