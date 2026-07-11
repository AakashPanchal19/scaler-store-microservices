package com.scaler.store.notification.controller;

import com.scaler.store.common.models.ApiResponse;
import com.scaler.store.notification.model.NotificationRecord;
import com.scaler.store.notification.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<NotificationRecord>>> getHistory() {
        List<NotificationRecord> history = notificationService.getNotificationHistory();
        return ResponseEntity.ok(ApiResponse.ok("Notification history retrieved", history));
    }
}
