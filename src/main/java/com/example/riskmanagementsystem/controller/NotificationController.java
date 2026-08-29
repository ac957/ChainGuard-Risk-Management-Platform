package com.example.riskmanagementsystem.controller;

import com.example.riskmanagementsystem.model.Notification;
import com.example.riskmanagementsystem.model.User;
import com.example.riskmanagementsystem.repo.UserRepository;
import com.example.riskmanagementsystem.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepo;

    public NotificationController(
            NotificationService notificationService,
            UserRepository userRepo) {
        this.notificationService = notificationService;
        this.userRepo = userRepo;
    }

    @GetMapping
    public String notificationList(Authentication auth,
                                   Model model) {

        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/auth/login";
        }

        String email = auth.getName();
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new
                        IllegalStateException("User not found."));

        // mark all as read when user visits page
        notificationService.markAllAsRead(user.getUserId());

        List<Notification> notifications =
                notificationService.getNotificationsForUser(
                        user.getUserId());

        // build distinct set of types present in this
        // user's notifications for the filter dropdown
        Set<String> notificationTypes = notifications.stream()
                .map(Notification::getType)
                .collect(Collectors.toCollection(
                        LinkedHashSet::new));

        // override GlobalModelAdvice with 0 since
        // page marks all as read
        model.addAttribute("unreadCount", 0);
        model.addAttribute("notifications", notifications);
        model.addAttribute("notificationTypes",
                notificationTypes);

        return "notifications/list";
    }
}