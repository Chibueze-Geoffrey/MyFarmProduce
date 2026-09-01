package com.myfarmproduce.application.service;

import com.myfarmproduce.domain.entity.Customer;

import java.util.Optional;

public interface AuthService {
    Optional<Customer> register(String name, String email, String phone, String password);

    Optional<Customer> validateCredentials(String email, String password);

    void changePassword(Integer customerId, String newPassword);
}
