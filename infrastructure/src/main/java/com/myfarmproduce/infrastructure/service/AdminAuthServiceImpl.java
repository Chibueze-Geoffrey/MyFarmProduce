package com.myfarmproduce.infrastructure.service;

import com.myfarmproduce.application.service.AdminAuthService;
import com.myfarmproduce.application.service.PasswordHasher;
import com.myfarmproduce.domain.entity.Admin;
import com.myfarmproduce.infrastructure.repository.AdminRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class AdminAuthServiceImpl implements AdminAuthService {

    private final AdminRepository admins;
    private final PasswordHasher hasher;

    public AdminAuthServiceImpl(AdminRepository admins, PasswordHasher hasher) {
        this.admins = admins;
        this.hasher = hasher;
    }

    @Override
    public Optional<Admin> validateCredentials(String email, String password) {
        String normalizedEmail = email.trim().toLowerCase();
        return admins.findByEmail(normalizedEmail)
                .filter(a -> hasher.verify(a.getPasswordHash(), password));
    }

    @Override
    public Optional<Admin> getById(Integer id) {
        return admins.findById(id);
    }
}
