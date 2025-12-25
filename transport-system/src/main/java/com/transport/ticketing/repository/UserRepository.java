package com.transport.ticketing.repository;

import com.transport.ticketing.model.User;

import java.util.Optional;

public class UserRepository extends InMemoryCrudRepository<User> {
    public Optional<User> findByEmail(String email) {
        return findAll().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }
}
