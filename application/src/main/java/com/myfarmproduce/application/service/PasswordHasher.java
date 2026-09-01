package com.myfarmproduce.application.service;

public interface PasswordHasher {
    String hash(String password);

    boolean verify(String hash, String password);
}
