package com.example.riskmanagementsystem.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// Manages risk identification, analysis, status and ownership
@Entity
@Table(name = "risks")
public class Risk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long riskId;

    // The organisation this risk belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id", nullable = false)
    private Organization organization;

    // The user who submitted the risk
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by_user_id", nullable = false)
    private User submittedBy;

    // The category used to classify the risk
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // The manager assigned as the owner of this risk
    // Only admins can assign this — only managers can be owners
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "risk_owner_user_id")
    private User riskOwner;

    // Short title for the risk
    @Column(nullable = false)
    private String riskTitle;

    // Main description of the risk
    @Column(nullable = false, length = 3000)
    private String description;

    // 1–5 scale for how likely the risk is
    @Column(nullable = false)
    private Integer likelihood;

    // 1–5 scale for how serious the impact is
    @Column(nullable = false)
    private Integer impact;

    @Column(length = 50)
    private String riskProximity;


    // Calculated score, usually likelihood × impact
    @Column(nullable = false)
    private Integer severityScore;

    // Current status of the risk
    // Example values: NEW, IN_PROGRESS, MITIGATED, CLOSED
    @Column(nullable = false, length = 30)
    private String status = "NEW";

    // Date and time the risk was created
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getRiskId() {
        return riskId;
    }

    public void setRiskId(Long riskId) {
        this.riskId = riskId;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public User getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(User submittedBy) {
        this.submittedBy = submittedBy;
    }

    public User getRiskOwner() { return riskOwner; }
    public void setRiskOwner(User riskOwner) {
        this.riskOwner = riskOwner; }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getRiskTitle() {
        return riskTitle;
    }

    public void setRiskTitle(String riskTitle) {
        this.riskTitle = riskTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getLikelihood() {
        return likelihood;
    }

    public void setLikelihood(Integer likelihood) {
        this.likelihood = likelihood;
    }

    public Integer getImpact() {
        return impact;
    }

    public void setImpact(Integer impact) {
        this.impact = impact;
    }

    public String getRiskProximity() {
        return riskProximity;
    }

    public void setRiskProximity(String riskProximity) {
        this.riskProximity = riskProximity;
    }

    public Integer getSeverityScore() {
        return severityScore;
    }

    public void setSeverityScore(Integer severityScore) {
        this.severityScore = severityScore;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}