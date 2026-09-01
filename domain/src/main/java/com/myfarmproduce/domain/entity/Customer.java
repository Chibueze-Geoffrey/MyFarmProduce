package com.myfarmproduce.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customers")
@Getter
@Setter
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 150)
    private String name = "";

    @Column(nullable = false, length = 30)
    private String phone = "";

    @Column(nullable = false, unique = true, length = 256)
    private String email = "";

    @Column(name = "password_hash", nullable = false, length = 512)
    private String passwordHash = "";

    @Column(name = "is_admin", nullable = false)
    private boolean admin;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    /** True until an admin-created user changes the default password on first login. */
    @Column(name = "must_change_password", nullable = false)
    private boolean mustChangePassword;

    @OneToMany(mappedBy = "customer")
    private List<Order> orders = new ArrayList<>();
}
