package com.example.riskmanagementsystem.service;

import com.example.riskmanagementsystem.dto.AIGuidance;
import com.example.riskmanagementsystem.dto.RiskEducation;
import com.example.riskmanagementsystem.model.Risk;
import com.example.riskmanagementsystem.model.UserProfile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AIGuidanceService {

    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model}")
    private String model;

    private Client client;

    public AIGuidanceService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        this.client = Client.builder()
                .apiKey(apiKey)
                .build();
    }

    public RiskEducation generateRiskEducation(Risk risk) {
        try {
            String prompt = """
                You are an educational risk management assistant
                for a supply chain and logistics risk management
                system called ChainGuard.

                A user has just submitted a risk. Your job is
                to help them understand the risk they submitted
                in plain, accessible language — as if you are
                teaching them about risk management for the
                first time. Be clear, educational and
                encouraging.

                Return ONLY valid JSON. Do not include any
                explanation outside the JSON. Do not use
                markdown. Output must start with { and end
                with }.

                Use exactly this structure:
                {
                  "whatThisRiskMeans": "string",
                  "severityExplained": "string",
                  "commonCauses": "string",
                  "whatGoodMitigationLooksLike": "string",
                  "whatHappensNext": "string",
                  "submissionQualityFeedback": "string",
                  "literacyScore": number between 1 and 10,
                  "literacyScoreReason": "string"
                }

                Guidelines:
                - whatThisRiskMeans: explain in 2-3 plain
                  English sentences what this type of risk is
                  and why it matters in a supply chain context.
                  Do not repeat the title back — explain the
                  concept.
                - severityExplained: explain what the specific
                  likelihood score of %s and impact score of %s
                  mean in practice. What does a severity score
                  of %s out of 25 actually mean for this
                  organisation? Use plain language.
                - commonCauses: list 2-3 common root causes of
                  this type of risk in supply chain environments
                  in plain language. Write as flowing sentences
                  not bullet points.
                - whatGoodMitigationLooksLike: describe in 2-3
                  sentences what effective mitigation of this
                  type of risk typically involves. Be
                  educational and specific to the category.
                - whatHappensNext: explain the ChainGuard risk
                  lifecycle in plain English — the risk will
                  now be reviewed by a manager, a mitigation
                  task will be assigned to a team member, and
                  the risk will be tracked through to
                  resolution. Make this feel reassuring and
                  clear.
                - submissionQualityFeedback: give constructive
                  feedback on the quality of their submission.
                  Was the description detailed enough? Was the
                  category appropriate? Was the severity
                  scoring reasonable? Be encouraging and
                  specific.
                - literacyScore: rate the quality of this risk
                  submission from 1 to 10 based on description
                  detail, category appropriateness and severity
                  scoring accuracy. Return as a number only.
                - literacyScoreReason: explain in one sentence
                  why you gave this score and what the user
                  could do to improve future submissions.

                Risk Title: %s
                Risk Category: %s
                Risk Description: %s
                Likelihood: %s out of 5
                Impact: %s out of 5
                Severity Score: %s out of 25
                """.formatted(
                    safe(risk.getLikelihood()),
                    safe(risk.getImpact()),
                    safe(risk.getSeverityScore()),
                    safe(risk.getRiskTitle()),
                    risk.getCategory() != null
                            ? safe(risk.getCategory().getName())
                            : "Not provided",
                    safe(risk.getDescription()),
                    safe(risk.getLikelihood()),
                    safe(risk.getImpact()),
                    safe(risk.getSeverityScore())
            );

            GenerateContentResponse response =
                    client.models.generateContent(
                            model, prompt, null);

            String jsonText = response.text()
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            if (!jsonText.startsWith("{")) {
                throw new RuntimeException(
                        "Gemini did not return JSON. "
                                + "Raw response: " + jsonText);
            }

            return objectMapper.readValue(
                    jsonText, RiskEducation.class);

        } catch (Exception e) {
            RiskEducation fallback = new RiskEducation();

            if (e.getMessage() != null
                    && (e.getMessage().contains("429")
                    || e.getMessage().contains("503"))) {

                fallback.setWhatThisRiskMeans(
                        "AI educational guidance is temporarily "
                                + "unavailable due to high demand. "
                                + "Please try again shortly.");
                fallback.setSeverityExplained(
                        "Your severity score of "
                                + safe(risk.getSeverityScore())
                                + " out of 25 was calculated by "
                                + "multiplying your likelihood score "
                                + "of " + safe(risk.getLikelihood())
                                + " by your impact score of "
                                + safe(risk.getImpact()) + ".");
                fallback.setCommonCauses(
                        "Please try again shortly for AI "
                                + "generated educational content "
                                + "about common causes of this risk.");
                fallback.setWhatGoodMitigationLooksLike(
                        "Your risk manager will assign a "
                                + "mitigation task to a team member "
                                + "shortly. They will define the "
                                + "specific steps to address this risk.");
                fallback.setWhatHappensNext(
                        "Your risk has been submitted successfully. "
                                + "A manager will review it and assign "
                                + "a mitigation action. You will "
                                + "receive a notification when this "
                                + "happens.");
                fallback.setSubmissionQualityFeedback(
                        "Thank you for submitting this risk. "
                                + "AI feedback on your submission "
                                + "quality is temporarily unavailable.");
                fallback.setLiteracyScore(0);
                fallback.setLiteracyScoreReason(
                        "Score unavailable — AI service "
                                + "temporarily unavailable.");

            } else {

                fallback.setWhatThisRiskMeans(
                        "Educational guidance could not be "
                                + "generated for this risk at this time.");
                fallback.setSeverityExplained(
                        "Your severity score of "
                                + safe(risk.getSeverityScore())
                                + " out of 25 was calculated by "
                                + "multiplying your likelihood score "
                                + "of " + safe(risk.getLikelihood())
                                + " by your impact score of "
                                + safe(risk.getImpact())
                                + ". Scores of 15 or above are "
                                + "considered high severity and "
                                + "require prompt attention.");
                fallback.setCommonCauses(
                        "Please consult your organisation's "
                                + "risk management policy for "
                                + "guidance on common causes of "
                                + "this risk category.");
                fallback.setWhatGoodMitigationLooksLike(
                        "Effective mitigation typically involves "
                                + "identifying the root cause, "
                                + "assigning clear ownership and "
                                + "setting a realistic deadline "
                                + "for resolution.");
                fallback.setWhatHappensNext(
                        "Your risk has been submitted successfully "
                                + "and is now in the risk register. "
                                + "A manager will review it and "
                                + "assign a mitigation action to "
                                + "a team member. You will receive "
                                + "a notification when this happens.");
                fallback.setSubmissionQualityFeedback(
                        "Thank you for submitting this risk. "
                                + "For best results ensure your "
                                + "description is detailed and "
                                + "specific, and that your likelihood "
                                + "and impact scores accurately "
                                + "reflect the risk.");
                fallback.setLiteracyScore(0);
                fallback.setLiteracyScoreReason(
                        "Score unavailable — please try "
                                + "viewing this page again.");
            }
            return fallback;
        }
    }

    public AIGuidance generateGuidance(Risk risk,
                                       List<UserProfile> profiles) {
        try {
            // build assignee list string for Gemini
            StringBuilder assigneeList = new StringBuilder();
            for (UserProfile profile : profiles) {
                assigneeList.append("- ")
                        .append(profile.getFullName())
                        .append(" (")
                        .append(profile.getJobRole() != null
                                ? profile.getJobRole()
                                : "No job role specified")
                        .append(")\n");
            }

            String prompt = """
                You are an AI assistant for a supply chain
                risk management system.

                Based on the risk details below, return ONLY
                valid JSON.
                Do not include any explanation outside the JSON.
                Do not use markdown.
                Do not start with phrases like "Here is the JSON"
                or "It seems".
                Output must start with { and end with }.

                Use exactly this structure:
                {
                  "riskSummary": "string",
                  "urgencyLevel": "LOW or MODERATE or HIGH",
                  "recommendedActions": ["action1", "action2",
                                         "action3"],
                  "recommendedAssigneeType": "string",
                  "recommendedAssigneeName": "string",
                  "recommendedAssigneeReason": "string",
                  "monitoringAdvice": "string",
                  "explanation": "string"
                }

                For recommendedAssigneeName choose the most
                suitable person from the Available Team Members
                list below based on their job role and the
                nature of the risk. If no suitable match exists
                set it to "No specific recommendation".
                For recommendedAssigneeReason explain in one
                sentence why that person is best suited.
                For recommendedAssigneeType describe the type
                of role best suited if no specific person fits.

                Risk Title: %s
                Risk Category: %s
                Risk Description: %s
                Impact: %s
                Likelihood: %s
                Severity Score: %s

                Available Team Members:
                %s
                """.formatted(
                    safe(risk.getRiskTitle()),
                    risk.getCategory() != null
                            ? safe(risk.getCategory().getName())
                            : "Not provided",
                    safe(risk.getDescription()),
                    safe(risk.getImpact()),
                    safe(risk.getLikelihood()),
                    safe(risk.getSeverityScore()),
                    assigneeList.toString()
            );

            GenerateContentResponse response =
                    client.models.generateContent(
                            model, prompt, null);

            String jsonText = response.text()
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            if (!jsonText.startsWith("{")) {
                throw new RuntimeException(
                        "Gemini did not return JSON. " +
                                "Raw response: " + jsonText);
            }

            return objectMapper.readValue(
                    jsonText, AIGuidance.class);

        } catch (Exception e) {
            AIGuidance fallback = new AIGuidance();

            if (e.getMessage() != null
                    && (e.getMessage().contains("429")
                    || e.getMessage().contains("503"))) {

                fallback.setRiskSummary(
                        "AI guidance is temporarily unavailable.");
                fallback.setUrgencyLevel("UNKNOWN");
                fallback.setRecommendedActions(List.of(
                        "Please try generating AI guidance again "
                                + "in a few moments"));
                fallback.setRecommendedAssigneeType(
                        "Please assign manually based on "
                                + "team member job roles and capabilities ");
                fallback.setRecommendedAssigneeName(
                        "Unavailable");
                fallback.setRecommendedAssigneeReason(
                        "Please select an assignee manually.");
                fallback.setMonitoringAdvice(
                        "Please try again shortly");
                fallback.setExplanation(
                        "The AI service is currently experiencing "
                                + "high demand. Please wait a moment "
                                + "and try again.");

            } else {

                fallback.setRiskSummary(
                        "AI guidance could not be generated "
                                + "for this risk.");
                fallback.setUrgencyLevel("UNKNOWN");
                fallback.setRecommendedActions(List.of(
                        "Please review this risk manually",
                        "Consider the likelihood and impact "
                                + "scores when assigning mitigation",
                        "Consult your risk management policy "
                                + "for guidance on this risk category"));
                fallback.setRecommendedAssigneeType(
                        "Please assign based on team member "
                                + "expertise and job role");
                fallback.setRecommendedAssigneeName(
                        "Unavailable");
                fallback.setRecommendedAssigneeReason(
                        "Please select an assignee manually.");
                fallback.setMonitoringAdvice(
                        "Monitor this risk manually until "
                                + "AI guidance becomes available");
                fallback.setExplanation(
                        "AI guidance could not be generated "
                                + "at this time. Please proceed with "
                                + "manual assignment.");
            }
            return fallback;
        }
    }

    public String generateAnalysis(String dataSummary) {
        try {
            String prompt = """
                You are an expert risk management advisor 
                for a logistics and supply chain company.
                
                Below is a summary of the organisation's 
                current risk data from their risk register.
                Analyse this data and provide clear, 
                educational insights that help the organisation
                understand their risk landscape and improve
                their risk management practices.
                
                Structure your response with these sections:
                1. Overall Risk Posture
                2. Key Concerns
                3. Positive Indicators
                4. Strategic Recommendations
                5. Future Focus Areas
                6. ISO 31000 Alignment Notes
                
                Guidelines for each section:
                - Overall Risk Posture: summarise the general 
                  health of the organisation's risk profile in 
                  2-3 sentences
                - Key Concerns: identify the most critical risks
                  or patterns that need immediate attention
                - Positive Indicators: highlight what the 
                  organisation is doing well based on the data
                - Strategic Recommendations: provide 3-4 
                  concrete, actionable steps the organisation 
                  should take to improve their risk management
                - Future Focus Areas: advise on what risk 
                  categories or processes the organisation 
                  should prioritise over the next 3-6 months
                  to build long term resilience
                - ISO 31000 Alignment Notes: explain how the 
                  current risk data reflects or deviates from 
                  ISO 31000 principles such as integration,
                  continual improvement and structured process
                
                Write in plain English suitable for a logistics
                manager, not a technical audience.
                Be specific and educational — explain why each
                recommendation matters, not just what to do.
                Do not use markdown formatting or bullet symbols.
                Use numbered points within sections if needed.
                Keep each section concise — 2 to 4 sentences.
                
                Risk Data Summary:
                %s
                """.formatted(dataSummary);

            GenerateContentResponse response =
                    client.models.generateContent(
                            model, prompt, null);

            return response.text().trim();

        } catch (Exception e) {
            if (e.getMessage() != null
                    && (e.getMessage().contains("429")
                    || e.getMessage().contains("503"))) {
                return "AI Risk Analysis — Temporarily Unavailable\n\n"
                        + "The AI analysis service is currently "
                        + "experiencing high demand. This is usually "
                        + "temporary.\n\n"
                        + "What you can do in the meantime:\n"
                        + "1. Review the charts above to identify "
                        + "patterns in your risk data manually\n"
                        + "2. Pay particular attention to any risks "
                        + "in the high severity band\n"
                        + "3. Check the status breakdown for risks "
                        + "that have been in NEW status for a long time\n"
                        + "4. Try the Analyse with AI button again "
                        + "in a few minutes";
            }
            return "AI Risk Analysis — Unavailable\n\n"
                    + "The AI analysis could not be generated "
                    + "at this time.\n\n"
                    + "What you can do in the meantime:\n"
                    + "1. Review the Risk Impact/Probability Matrix "
                    + "to identify high likelihood, high impact risks\n"
                    + "2. Focus mitigation efforts on risks in the "
                    + "top right quadrant of the matrix\n"
                    + "3. Review risks that have been unresolved "
                    + "for an extended period\n"
                    + "4. Please try again later";
        }
    }

    private String safe(Object value) {
        return value == null ? "Not provided" : value.toString();
    }
}