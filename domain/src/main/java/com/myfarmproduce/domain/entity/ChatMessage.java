package com.myfarmproduce.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** A message in the shared community chat room. */
@Entity
@Table(name = "chat_messages")
@Getter
@Setter
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "sender_name", nullable = false, length = 150)
    private String senderName = "";

    @Column(nullable = false, length = 2000)
    private String content = "";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
