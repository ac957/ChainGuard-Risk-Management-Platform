package com.example.riskmanagementsystem.dto;

import java.util.List;

// This class represents the AI response (structured output)
public class AIGuidance {

    // Short summary of the risk
    private String riskSummary;

    // LOW / MODERATE / HIGH
    private String urgencyLevel;

    // List of suggested mitigation actions
    private List<String> recommendedActions;

    // What type of person should handle it (e.g. Operations Manager)
    private String recommendedAssigneeType;

    // How to monitor the risk
    private String monitoringAdvice;

    // Why the AI suggested this (explanation)
    private String explanation;

    private String recommendedAssigneeName;
    private String recommendedAssigneeReason;

    public String getRecommendedAssigneeName() {
        return recommendedAssigneeName; }

    // Getters and setters (used by Spring + Jackson)

    public String getRiskSummary() {
        return riskSummary;
    }

    public void setRiskSummary(String riskSummary) {
        this.riskSummary = riskSummary;
    }

    public String getUrgencyLevel() {
        return urgencyLevel;
    }

    public void setUrgencyLevel(String urgencyLevel) {
        this.urgencyLevel = urgencyLevel;
    }

    public List<String> getRecommendedActions() {
        return recommendedActions;
    }

    public void setRecommendedActions(List<String> recommendedActions) {
        this.recommendedActions = recommendedActions;
    }

    public String getRecommendedAssigneeType() {
        return recommendedAssigneeType;
    }

    public void setRecommendedAssigneeType(String recommendedAssigneeType) {
        this.recommendedAssigneeType = recommendedAssigneeType;
    }

    public String getMonitoringAdvice() {
        return monitoringAdvice;
    }

    public void setMonitoringAdvice(String monitoringAdvice) {
        this.monitoringAdvice = monitoringAdvice;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public void setRecommendedAssigneeName(
            String recommendedAssigneeName) {
        this.recommendedAssigneeName = recommendedAssigneeName; }

    public String getRecommendedAssigneeReason() {
        return recommendedAssigneeReason; }
    public void setRecommendedAssigneeReason(
            String recommendedAssigneeReason) {
        this.recommendedAssigneeReason = recommendedAssigneeReason; }
}