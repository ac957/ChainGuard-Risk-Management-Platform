package com.example.riskmanagementsystem.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RiskEducation {

    private String whatThisRiskMeans;
    private String severityExplained;
    private String commonCauses;
    private String whatGoodMitigationLooksLike;
    private String whatHappensNext;
    private String submissionQualityFeedback;
    private int literacyScore;
    private String literacyScoreReason;

    public String getWhatThisRiskMeans() {
        return whatThisRiskMeans;
    }
    public void setWhatThisRiskMeans(String whatThisRiskMeans) {
        this.whatThisRiskMeans = whatThisRiskMeans;
    }
    public String getSeverityExplained() {
        return severityExplained;
    }
    public void setSeverityExplained(String severityExplained) {
        this.severityExplained = severityExplained;
    }
    public String getCommonCauses() {
        return commonCauses;
    }
    public void setCommonCauses(String commonCauses) {
        this.commonCauses = commonCauses;
    }
    public String getWhatGoodMitigationLooksLike() {
        return whatGoodMitigationLooksLike;
    }
    public void setWhatGoodMitigationLooksLike(
            String whatGoodMitigationLooksLike) {
        this.whatGoodMitigationLooksLike =
                whatGoodMitigationLooksLike;
    }
    public String getWhatHappensNext() {
        return whatHappensNext;
    }
    public void setWhatHappensNext(String whatHappensNext) {
        this.whatHappensNext = whatHappensNext;
    }
    public String getSubmissionQualityFeedback() {
        return submissionQualityFeedback;
    }
    public void setSubmissionQualityFeedback(
            String submissionQualityFeedback) {
        this.submissionQualityFeedback =
                submissionQualityFeedback;
    }
    public int getLiteracyScore() {
        return literacyScore;
    }
    public void setLiteracyScore(int literacyScore) {
        this.literacyScore = literacyScore;
    }
    public String getLiteracyScoreReason() {
        return literacyScoreReason;
    }
    public void setLiteracyScoreReason(
            String literacyScoreReason) {
        this.literacyScoreReason = literacyScoreReason;
    }
}