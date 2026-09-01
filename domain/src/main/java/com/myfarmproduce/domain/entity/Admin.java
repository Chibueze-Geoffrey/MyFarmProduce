package com.myfarmproduce.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** Store administrator - a separate account type from site customers. */
@Entity
@Table(name = "admins")
@Getter
@Setter
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 150)
    private String name = "";

    @Column(nullable = false, unique = true, length = 256)
    private String email = "";

    @Column(name = "password_hash", nullable = false, length = 512)
    private String passwordHash = "";

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
