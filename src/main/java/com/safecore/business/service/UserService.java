package com.safecore.business.service;

import com.safecore.business.domain.User;
import java.util.Optional;

public interface UserService {
    User register(String email, String plainPassword);
    Optional<User> login(String email, String plainPassword);
}