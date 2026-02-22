package com.scaler.store.common.events;

import java.io.Serializable;
import java.time.LocalDateTime;

public class PasswordResetEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String email;
    private String resetToken;
    private String resetUrl;
    private LocalDateTime expiryTime;
    private LocalDateTime timestamp;

    public PasswordResetEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public PasswordResetEvent(Long userId, String email, String resetToken, String resetUrl, LocalDateTime expiryTime) {
        this.userId = userId;
        this.email = email;
        this.resetToken = resetToken;
        this.resetUrl = resetUrl;
        this.expiryTime = expiryTime;
        this.timestamp = LocalDateTime.now();
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }

    public String getResetUrl() { return resetUrl; }
    public void setResetUrl(String resetUrl) { this.resetUrl = resetUrl; }

    public LocalDateTime getExpiryTime() { return expiryTime; }
    public void setExpiryTime(LocalDateTime expiryTime) { this.expiryTime = expiryTime; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
