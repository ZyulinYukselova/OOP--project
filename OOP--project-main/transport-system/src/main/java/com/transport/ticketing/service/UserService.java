package com.transport.ticketing.service;

import com.transport.ticketing.exception.ValidationException;
import com.transport.ticketing.model.Role;
import com.transport.ticketing.model.User;
import com.transport.ticketing.repository.UserRepository;

import java.util.Optional;

public class UserService {
    private final UserRepository users;

    public UserService(UserRepository users) {
        this.users = users;
    }

    public User createUser(String email, String displayName, Role role) {
        Optional<User> existing = users.findByEmail(email);
        if (existing.isPresent()) {
            throw new ValidationException("Email already exists");
        }
        User user = new User(email, displayName, role);
        return users.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return users.findByEmail(email);
    }

    public Optional<User> findById(String id) {
        return users.findById(id);
    }
}
