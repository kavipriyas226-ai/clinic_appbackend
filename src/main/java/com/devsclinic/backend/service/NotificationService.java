package com.devsclinic.backend.service;

import com.devsclinic.backend.exception.ResourceNotFoundException;
import com.devsclinic.backend.model.InventoryItem;
import com.devsclinic.backend.model.Notification;
import com.devsclinic.backend.repository.NotificationRepository;
import com.devsclinic.backend.util.SequentialIdGenerator;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<Notification> getAll() {
        return notificationRepository.findAllByOrderByCreatedAtDesc();
    }

    public long getUnreadCount() {
        return notificationRepository.countByReadFalse();
    }

    public Notification markRead(String id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + id));
        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    public void markAllRead() {
        List<Notification> unread = notificationRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(n -> !n.isRead())
                .toList();
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    /** Raises a new low-stock alert for the given item. Called once per low-stock "crossing" event. */
    public void createLowStockNotification(InventoryItem item) {
        List<String> existingIds = notificationRepository.findAll().stream().map(Notification::getId).toList();
        String newId = SequentialIdGenerator.next(existingIds, "NTF-", 1);

        Notification notification = Notification.builder()
                .id(newId)
                .type("LOW_STOCK")
                .message(item.getName() + " is low on stock (" + item.getStock() + " left, threshold " + item.getThreshold() + ")")
                .itemId(item.getId())
                .itemName(item.getName())
                .stock(item.getStock())
                .threshold(item.getThreshold())
                .read(false)
                .createdAt(Instant.now())
                .build();

        notificationRepository.save(notification);
    }
}
