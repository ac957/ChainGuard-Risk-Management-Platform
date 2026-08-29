package com.example.riskmanagementsystem.controller;

import com.example.riskmanagementsystem.model.OrganizationMember;
import com.example.riskmanagementsystem.model.User;
import com.example.riskmanagementsystem.repo.OrganizationMemberRepository;
import com.example.riskmanagementsystem.repo.UserRepository;
import com.example.riskmanagementsystem.service.NotificationService;
import com.example.riskmanagementsystem.service.OrganizationService;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {

    private final UserRepository userRepo;
    private final OrganizationMemberRepository memberRepo;
    private final NotificationService notificationService;
    private final OrganizationService orgService;

    public GlobalModelAdvice(
            UserRepository userRepo,
            OrganizationMemberRepository memberRepo,
            NotificationService notificationService,
            OrganizationService orgService) {
        this.userRepo = userRepo;
        this.memberRepo = memberRepo;
        this.notificationService = notificationService;
        this.orgService = orgService;
    }

    @ModelAttribute
    public void addGlobalAttributes(
            Authentication auth, Model model) {

        // only run if user is authenticated
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(
                auth.getPrincipal())) {
            return;
        }

        try {
            String email = auth.getName();
            User user = userRepo.findByEmail(email)
                    .orElse(null);
            if (user == null) return;

            // find approved membership
            OrganizationMember membership = memberRepo
                    .findFirstByUser_UserIdAndStatus(
                            user.getUserId(), "APPROVED")
                    .orElse(null);

            if (membership == null) return;

            String systemRole = membership.getRole().getName();
            boolean isAdmin = "ADMIN".equalsIgnoreCase(
                    systemRole);
            boolean isManager = "MANAGER".equalsIgnoreCase(
                    systemRole);
            boolean isUser = "USER".equalsIgnoreCase(
                    systemRole);

            model.addAttribute("systemRole", systemRole);
            model.addAttribute("isAdmin", isAdmin);
            model.addAttribute("isManager", isManager);
            model.addAttribute("isUser", isUser);
            model.addAttribute("organizationName",
                    membership.getOrganization().getOrgName());

            model.addAttribute("currentUserId", user.getUserId());

            // unread notification count for navbar badge
            model.addAttribute("unreadCount",
                    notificationService
                            .getUnreadNotificationsForUser(
                                    user.getUserId()).size());

            // pending approval count for admin navbar badge
            if (isAdmin) {
                model.addAttribute("pendingCount",
                        orgService.getPendingRequestsCount(
                                user));
            } else {
                model.addAttribute("pendingCount", 0);
            }

        } catch (Exception e) {
            // fail silently — don't break page rendering
            // if global attributes can't be loaded
            System.err.println(
                    "GlobalModelAdvice error: "
                            + e.getMessage());
        }
    }
}
