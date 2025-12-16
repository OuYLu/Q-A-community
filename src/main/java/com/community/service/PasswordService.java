package com.community.service;

public interface PasswordService {
    String hash(String rawPassword);

    boolean matches(String rawPassword, String hashed);
}
