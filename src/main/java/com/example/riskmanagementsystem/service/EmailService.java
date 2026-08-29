package com.example.riskmanagementsystem.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // sent when a user is approved into an organisation
    public void sendApprovalEmail(String toEmail,
                                  String fullName,
                                  String orgName,
                                  String roleName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("You have been approved — "
                    + orgName);
            helper.setText(
                    "<div style='font-family:Arial,sans-serif;"
                            + "max-width:600px;margin:auto;padding:24px;"
                            + "border:1px solid #e0e0e0;border-radius:8px'>"
                            + "<h2 style='color:#2d6a4f'>Welcome to "
                            + orgName + "!</h2>"
                            + "<p>Hi " + fullName + ",</p>"
                            + "<p>Your request to join <strong>"
                            + orgName + "</strong> has been approved.</p>"
                            + "<p>You have been assigned the role of "
                            + "<strong>" + roleName + "</strong>.</p>"
                            + "<p>You can now log in and start using "
                            + "the risk management system.</p>"
                            + "<br><p style='color:#888;font-size:12px'>"
                            + "This is an automated message from the "
                            + "Risk Management System.</p>"
                            + "</div>",
                    true);

            mailSender.send(message);

        } catch (Exception e) {
            System.err.println("Failed to send approval email: "
                    + e.getMessage());
        }
    }

    public void sendRejectionEmail(String toEmail,
                                   String orgName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("ChainGuard — Organisation "
                    + "Request Update");
            helper.setText(
                    "<div style='font-family:Arial,sans-serif;"
                            + "max-width:600px;margin:auto;padding:24px;"
                            + "border:1px solid #e0e0e0;"
                            + "border-radius:8px'>"
                            + "<h2 style='color:#dc3545'>"
                            + "Request Not Approved</h2>"
                            + "<p>Your request to join <strong>"
                            + orgName + "</strong> on ChainGuard "
                            + "has not been approved by the "
                            + "administrator.</p>"
                            + "<p>If you believe this was a mistake "
                            + "please contact your organisation "
                            + "administrator directly.</p>"
                            + "<p style='margin-top:12px'>You may log "
                            + "back in and request to join a different "
                            + "organisation if applicable.</p>"
                            + "<br><p style='color:#888;font-size:12px'>"
                            + "This is an automated message from "
                            + "ChainGuard Risk Management System.</p>"
                            + "</div>",
                    true);

            mailSender.send(message);

        } catch (Exception e) {
            System.err.println(
                    "Failed to send rejection email: "
                            + e.getMessage());
        }
    }

    // sent to admin when a high severity risk is submitted
    public void sendHighSeverityAlert(String toEmail,
                                      String adminName,
                                      String orgName,
                                      String riskTitle,
                                      String category,
                                      int likelihood,
                                      int impact,
                                      int severityScore,
                                      String submittedByEmail) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("HIGH SEVERITY RISK ALERT — "
                    + riskTitle);
            helper.setText(
                    "<div style='font-family:Arial,sans-serif;"
                            + "max-width:600px;margin:auto;padding:24px;"
                            + "border:1px solid #e0e0e0;border-radius:8px'>"
                            + "<h2 style='color:#dc3545'>⚠ High Severity "
                            + "Risk Submitted</h2>"
                            + "<p>Hi " + adminName + ",</p>"
                            + "<p>A high severity risk has been submitted "
                            + "in <strong>" + orgName
                            + "</strong> and requires your attention.</p>"
                            + "<table style='width:100%;border-collapse:"
                            + "collapse;margin-top:16px'>"
                            + "<tr style='background:#f8f9fa'>"
                            + "<td style='padding:8px;border:1px solid #ddd;"
                            + "font-weight:bold'>Risk Title</td>"
                            + "<td style='padding:8px;border:1px solid #ddd'>"
                            + riskTitle + "</td></tr>"
                            + "<tr><td style='padding:8px;border:1px solid "
                            + "#ddd;font-weight:bold'>Category</td>"
                            + "<td style='padding:8px;border:1px solid #ddd'>"
                            + category + "</td></tr>"
                            + "<tr style='background:#f8f9fa'>"
                            + "<td style='padding:8px;border:1px solid #ddd;"
                            + "font-weight:bold'>Likelihood</td>"
                            + "<td style='padding:8px;border:1px solid #ddd'>"
                            + likelihood + " / 5</td></tr>"
                            + "<tr><td style='padding:8px;border:1px solid "
                            + "#ddd;font-weight:bold'>Impact</td>"
                            + "<td style='padding:8px;border:1px solid #ddd'>"
                            + impact + " / 5</td></tr>"
                            + "<tr style='background:#fff3cd'>"
                            + "<td style='padding:8px;border:1px solid #ddd;"
                            + "font-weight:bold'>Severity Score</td>"
                            + "<td style='padding:8px;border:1px solid #ddd;"
                            + "color:#dc3545;font-weight:bold'>"
                            + severityScore + " (HIGH)</td></tr>"
                            + "<tr><td style='padding:8px;border:1px solid "
                            + "#ddd;font-weight:bold'>Submitted By</td>"
                            + "<td style='padding:8px;border:1px solid #ddd'>"
                            + submittedByEmail + "</td></tr>"
                            + "</table>"
                            + "<p style='margin-top:20px'>Please log in to "
                            + "the risk management system to review and assign "
                            + "a risk owner as soon as possible.</p>"
                            + "<br><p style='color:#888;font-size:12px'>"
                            + "This is an automated message from the "
                            + "Risk Management System.</p>"
                            + "</div>",
                    true);

            mailSender.send(message);

        } catch (Exception e) {
            System.err.println(
                    "Failed to send high severity alert: "
                            + e.getMessage());
        }
    }
    public void sendRoleChangeEmail(String toEmail,
                                    String orgName,
                                    String oldRole,
                                    String newRole) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("ChainGuard — Your Role Has Been Updated");
            helper.setText(
                    "<div style='font-family:Arial,sans-serif;"
                            + "max-width:600px;margin:auto;padding:24px;"
                            + "border:1px solid #e0e0e0;"
                            + "border-radius:8px'>"
                            + "<h2 style='color:#1a2e4a'>"
                            + "Role Update</h2>"
                            + "<p>Your role in <strong>"
                            + orgName + "</strong> has been updated "
                            + "by the administrator.</p>"
                            + "<table style='width:100%;margin-top:16px;"
                            + "border-collapse:collapse'>"
                            + "<tr style='background:#f8f9fa'>"
                            + "<td style='padding:8px;border:1px solid #ddd;"
                            + "font-weight:bold'>Previous Role</td>"
                            + "<td style='padding:8px;border:1px solid #ddd'>"
                            + oldRole + "</td></tr>"
                            + "<tr>"
                            + "<td style='padding:8px;border:1px solid #ddd;"
                            + "font-weight:bold'>New Role</td>"
                            + "<td style='padding:8px;border:1px solid #ddd'>"
                            + newRole + "</td></tr>"
                            + "</table>"
                            + "<p style='margin-top:16px'>Your access "
                            + "permissions have been updated accordingly. "
                            + "Please log in to see your updated access.</p>"
                            + "<br><p style='color:#888;font-size:12px'>"
                            + "This is an automated message from "
                            + "ChainGuard Risk Management System.</p>"
                            + "</div>",
                    true);

            mailSender.send(message);

        } catch (Exception e) {
            System.err.println(
                    "Failed to send role change email: "
                            + e.getMessage());
        }
    }

    public void sendRemovalEmail(String toEmail,
                                 String orgName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("ChainGuard — Organisation "
                    + "Membership Update");
            helper.setText(
                    "<div style='font-family:Arial,sans-serif;"
                            + "max-width:600px;margin:auto;padding:24px;"
                            + "border:1px solid #e0e0e0;"
                            + "border-radius:8px'>"
                            + "<h2 style='color:#dc3545'>"
                            + "Membership Removed</h2>"
                            + "<p>Your membership in <strong>"
                            + orgName + "</strong> has been removed "
                            + "by the administrator.</p>"
                            + "<p style='margin-top:12px'>You will no "
                            + "longer be able to access this "
                            + "organisation's risk management system.</p>"
                            + "<p style='margin-top:12px'>If you believe "
                            + "this was a mistake please contact your "
                            + "organisation administrator directly or you."
                            + "can join another organisation</p>"
                            + "<br><p style='color:#888;font-size:12px'>"
                            + "This is an automated message from "
                            + "ChainGuard Risk Management System.</p>"
                            + "</div>",
                    true);

            mailSender.send(message);

        } catch (Exception e) {
            System.err.println(
                    "Failed to send removal email: "
                            + e.getMessage());
        }
    }
    public void sendPasswordResetEmail(String toEmail,
                                       String resetLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject(
                    "ChainGuard — Password Reset Request");
            helper.setText(
                    "<div style='font-family:Arial,sans-serif;"
                            + "max-width:600px;margin:auto;padding:24px;"
                            + "border:1px solid #e0e0e0;"
                            + "border-radius:8px'>"
                            + "<h2 style='color:#1a2e4a'>"
                            + "Password Reset Request</h2>"
                            + "<p>You recently requested to reset "
                            + "your password for your ChainGuard "
                            + "account.</p>"
                            + "<p>Click the button below to reset it. "
                            + "This link will expire in 30 minutes.</p>"
                            + "<a href='" + resetLink + "' "
                            + "style='display:inline-block;"
                            + "margin-top:16px;padding:12px 24px;"
                            + "background:#1a2e4a;color:white;"
                            + "text-decoration:none;border-radius:6px;"
                            + "font-weight:bold'>Reset Password</a>"
                            + "<p style='margin-top:20px;color:#888;"
                            + "font-size:12px'>If you did not request "
                            + "a password reset please ignore this "
                            + "email. Your password will not be "
                            + "changed.</p>"
                            + "<br><p style='color:#888;font-size:12px'>"
                            + "This is an automated message from "
                            + "ChainGuard Risk Management System.</p>"
                            + "</div>",
                    true);

            mailSender.send(message);

        } catch (Exception e) {
            System.err.println(
                    "Failed to send password reset email: "
                            + e.getMessage());
        }
    }
}
