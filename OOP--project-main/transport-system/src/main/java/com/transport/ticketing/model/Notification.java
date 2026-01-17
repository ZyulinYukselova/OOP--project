package com.transport.ticketing.model;

import com.transport.ticketing.util.IdGenerator;
import java.time.Instant;

public class Notification extends BaseEntity {
    private final String userId;
    private final NotificationType type;
    private final String payload;
    private Instant readAt;

    public Notification(String userId, NotificationType type, String payload) {
        super(IdGenerator.nextNotificationId());
        this.userId = userId;
        this.type = type;
        this.payload = payload;
    }

    public String getUserId() {
        return userId;
    }

    public NotificationType getType() {
        return type;
    }

    public String getPayload() {
        return payload;
    }

    public Instant getReadAt() {
        return readAt;
    }

    public void markRead() {
        this.readAt = Instant.now();
    }
}
