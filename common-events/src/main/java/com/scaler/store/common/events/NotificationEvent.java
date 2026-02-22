package com.scaler.store.common.events;

import com.scaler.store.common.models.NotificationType;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

public class NotificationEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String recipientEmail;
    private String recipientPhone;
    private NotificationType type;
    private String subject;
    private String message;
    private Map<String, Object> metadata;
    private LocalDateTime timestamp;

    public NotificationEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public NotificationEvent(Long userId, String recipientEmail, String recipientPhone,
                             NotificationType type, String subject, String message, Map<String, Object> metadata) {
        this.userId = userId;
        this.recipientEmail = recipientEmail;
        this.recipientPhone = recipientPhone;
        this.type = type;
        this.subject = subject;
        this.message = message;
        this.metadata = metadata;
        this.timestamp = LocalDateTime.now();
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }

    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
