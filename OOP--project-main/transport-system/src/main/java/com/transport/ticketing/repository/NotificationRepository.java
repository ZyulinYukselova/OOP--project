package com.transport.ticketing.repository;

import com.transport.ticketing.model.Notification;

import java.util.List;
import java.util.stream.Collectors;

public class NotificationRepository extends InMemoryCrudRepository<Notification> {
    public List<Notification> findByUserId(String userId) {
        return findAll().stream()
                .filter(n -> n.getUserId().equals(userId))
                .collect(Collectors.toList());
    }
}
