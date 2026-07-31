package com.devsclinic.backend.repository;

import com.devsclinic.backend.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findAllByOrderByCreatedAtDesc();
    long countByReadFalse();
    void deleteByItemId(String itemId);
}
