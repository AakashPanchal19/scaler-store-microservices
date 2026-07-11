package com.scaler.store.notification.model;

import com.scaler.store.common.models.NotificationType;

import java.io.Serializable;
import java.time.LocalDateTime;

public class NotificationRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String recipient;
    private NotificationType type;
    private String subject;
    private String message;
    private String status;
    private LocalDateTime timestamp;

    public NotificationRecord() {
        this.timestamp = LocalDateTime.now();
    }

    public NotificationRecord(String id, String recipient, NotificationType type, String subject, String message, String status) {
        this.id = id;
        this.recipient = recipient;
        this.type = type;
        this.subject = subject;
        this.message = message;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
