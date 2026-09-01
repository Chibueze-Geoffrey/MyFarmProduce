package com.myfarmproduce.domain.entity;

import com.myfarmproduce.domain.enums.SupportSender;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "support_messages")
@Getter
@Setter
public class SupportMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private SupportTicket ticket;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SupportSender sender;

    @Column(nullable = false, length = 2000)
    private String content = "";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
