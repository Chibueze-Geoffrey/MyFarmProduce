package com.myfarmproduce.infrastructure.service;

import com.myfarmproduce.application.service.AuthService;
import com.myfarmproduce.application.service.PasswordHasher;
import com.myfarmproduce.domain.entity.Customer;
import com.myfarmproduce.infrastructure.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final CustomerRepository customers;
    private final PasswordHasher hasher;

    public AuthServiceImpl(CustomerRepository customers, PasswordHasher hasher) {
        this.customers = customers;
        this.hasher = hasher;
    }

    @Override
    public Optional<Customer> register(String name, String email, String phone, String password) {
        String normalizedEmail = email.trim().toLowerCase();
        if (customers.existsByEmail(normalizedEmail)) return Optional.empty();

        Customer customer = new Customer();
        customer.setName(name.trim());
        customer.setEmail(normalizedEmail);
        customer.setPhone(phone.trim());
        customer.setPasswordHash(hasher.hash(password));
        customer.setAdmin(false);
        customers.save(customer);
        return Optional.of(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> validateCredentials(String email, String password) {
        String normalizedEmail = email.trim().toLowerCase();
        return customers.findByEmail(normalizedEmail)
                .filter(c -> hasher.verify(c.getPasswordHash(), password));
    }

    @Override
    public void changePassword(Integer customerId, String newPassword) {
        Customer customer = customers.findById(customerId)
                .orElseThrow(() -> new IllegalStateException("Customer not found."));
        customer.setPasswordHash(hasher.hash(newPassword));
        customer.setMustChangePassword(false);
    }
}
