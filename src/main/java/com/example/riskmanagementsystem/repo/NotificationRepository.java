package com.example.riskmanagementsystem.repo;

import com.example.riskmanagementsystem.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUser_UserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByUser_UserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    boolean existsByUser_UserIdAndMessageAndType(Long userId, String message, String type);
}