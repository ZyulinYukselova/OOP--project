package com.transport.ticketing.service;

import com.transport.ticketing.exception.NotFoundException;
import com.transport.ticketing.model.Notification;
import com.transport.ticketing.model.NotificationType;
import com.transport.ticketing.repository.NotificationRepository;

import java.util.List;

public class NotificationService {
    private final NotificationRepository notifications;

    public NotificationService(NotificationRepository notifications) {
        this.notifications = notifications;
    }

    public Notification notify(String userId, NotificationType type, String payload) {
        Notification notification = new Notification(userId, type, payload);
        return notifications.save(notification);
    }

    public List<Notification> getForUser(String userId) {
        return notifications.findByUserId(userId);
    }

    public Notification markRead(String notificationId) {
        Notification notification = notifications.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("Notification not found"));
        notification.markRead();
        return notifications.save(notification);
    }
}
