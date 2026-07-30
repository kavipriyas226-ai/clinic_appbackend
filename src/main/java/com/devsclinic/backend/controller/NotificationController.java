package com.devsclinic.backend.controller;

import com.devsclinic.backend.model.Notification;
import com.devsclinic.backend.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<Notification> getAll() {
        return notificationService.getAll();
    }

    @GetMapping("/unread-count")
    public Map<String, Long> getUnreadCount() {
        return Map.of("count", notificationService.getUnreadCount());
    }

    @PutMapping("/{id}/read")
    public Notification markRead(@PathVariable String id) {
        return notificationService.markRead(id);
    }

    @PutMapping("/read-all")
    public void markAllRead() {
        notificationService.markAllRead();
    }
}
