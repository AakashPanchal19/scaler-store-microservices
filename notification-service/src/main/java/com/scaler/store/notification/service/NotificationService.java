package com.scaler.store.notification.service;

import com.scaler.store.common.models.NotificationType;
import com.scaler.store.notification.model.NotificationRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final List<NotificationRecord> notificationHistory = new CopyOnWriteArrayList<>();

    public void sendEmail(String recipient, NotificationType type, String subject, String body) {
        log.info("================== [DISPATCHING EMAIL VIA AWS SES SIMULATOR] ==================");
        log.info("To: {}", recipient);
        log.info("Type: {}", type);
        log.info("Subject: {}", subject);
        log.info("Body:\n{}", body);
        log.info("===============================================================================");

        NotificationRecord record = new NotificationRecord(
                UUID.randomUUID().toString(),
                recipient,
                type,
                subject,
                body,
                "SENT_SUCCESS"
        );
        notificationHistory.add(0, record);

        // Keep last 100 notifications in memory
        if (notificationHistory.size() > 100) {
            notificationHistory.remove(notificationHistory.size() - 1);
        }
    }

    public List<NotificationRecord> getNotificationHistory() {
        return Collections.unmodifiableList(new ArrayList<>(notificationHistory));
    }
}
