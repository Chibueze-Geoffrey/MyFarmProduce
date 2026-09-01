package com.myfarmproduce.application.service;

import com.myfarmproduce.domain.entity.Admin;

import java.util.Optional;

public interface AdminAuthService {
    Optional<Admin> validateCredentials(String email, String password);

    Optional<Admin> getById(Integer id);
}
