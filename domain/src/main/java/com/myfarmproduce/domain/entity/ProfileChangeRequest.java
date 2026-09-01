package com.myfarmproduce.domain.entity;

import com.myfarmproduce.domain.enums.ChangeRequestStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** A customer's request to change a locked field (phone/email). Applied by an admin. */
@Entity
@Table(name = "profile_change_requests")
@Getter
@Setter
public class ProfileChangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /** "Phone" or "Email". */
    @Column(nullable = false, length = 20)
    private String field = "";

    @Column(name = "current_value", nullable = false, length = 256)
    private String currentValue = "";

    @Column(name = "requested_value", nullable = false, length = 256)
    private String requestedValue = "";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChangeRequestStatus status = ChangeRequestStatus.Pending;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "admin_note", length = 1000)
    private String adminNote;
}
