<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Understanding Your Risk</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .edu-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 16px;
            margin-bottom: 16px;
        }
        .edu-card {
            background: #fff;
            border: 1.5px solid #5eead4;
            border-radius: 12px;
            padding: 20px 24px;
        }
        .edu-card-full {
            background: #fff;
            border: 1.5px solid #5eead4;
            border-radius: 12px;
            padding: 20px 24px;
            margin-bottom: 16px;
        }
        .edu-card-icon {
            font-size: 20px;
            margin-bottom: 10px;
            display: block;
        }
        .edu-card h3 {
            font-size: 14px;
            font-weight: 600;
            color: #0f766e;
            margin-bottom: 8px;
        }
        .edu-card p,
        .edu-card-full p {
            font-size: 14px;
            color: #111111;
            line-height: 1.7;
            margin: 0;
        }
        .edu-card-full h3 {
            font-size: 14px;
            font-weight: 600;
            color: #0f766e;
            margin-bottom: 8px;
        }
        .score-bar-wrap {
            background: #f0fdfa;
            border-radius: 999px;
            height: 12px;
            width: 100%;
            margin: 10px 0 6px 0;
            overflow: hidden;
            border: 1px solid #99f6e4;
        }
        .score-bar-fill {
            height: 100%;
            border-radius: 999px;
            background: #0f766e;
            transition: width 1s ease;
        }
        .score-label {
            font-size: 13px;
            color: #0f766e;
            font-weight: 600;
        }
        .severity-pill {
            display: inline-block;
            padding: 4px 14px;
            border-radius: 999px;
            font-size: 13px;
            font-weight: 600;
            margin-bottom: 12px;
        }
        .severity-high {
            background: #fef2f2;
            color: #991b1b;
            border: 1px solid #fca5a5;
        }
        .severity-medium {
            background: #fffbeb;
            color: #92400e;
            border: 1px solid #fcd34d;
        }
        .severity-low {
            background: #f0fdfa;
            color: #0f766e;
            border: 1px solid #5eead4;
        }
        .risk-summary-header {
            background: #0f766e;
            border-radius: 12px;
            padding: 24px 28px;
            margin-bottom: 24px;
            color: #fff;
        }
        .risk-summary-header h2 {
            font-size: 20px;
            font-weight: 700;
            color: #fff;
            margin-bottom: 4px;
        }
        .risk-summary-header p {
            font-size: 14px;
            color: #ccfbf1;
            margin: 0;
        }
        .risk-summary-header .meta {
            display: flex;
            gap: 20px;
            margin-top: 14px;
            flex-wrap: wrap;
        }
        .risk-summary-header .meta-item {
            font-size: 13px;
            color: #ccfbf1;
        }
        .risk-summary-header .meta-item strong {
            color: #fff;
            display: block;
            font-size: 22px;
            font-weight: 700;
        }
        .next-steps {
            background: #f0fdfa;
            border: 1.5px solid #5eead4;
            border-radius: 12px;
            padding: 20px 24px;
            margin-bottom: 16px;
        }
        .next-steps h3 {
            font-size: 14px;
            font-weight: 600;
            color: #0f766e;
            margin-bottom: 12px;
        }
        .step {
            display: flex;
            gap: 12px;
            align-items: flex-start;
            margin-bottom: 10px;
        }
        .step-num {
            background: #0f766e;
            color: #fff;
            border-radius: 999px;
            width: 24px;
            height: 24px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 12px;
            font-weight: 600;
            flex-shrink: 0;
        }
        .step p {
            font-size: 14px;
            color: #111111;
            line-height: 1.6;
            margin: 0;
        }
        .feedback-card {
            background: #fff;
            border: 1.5px solid #5eead4;
            border-left: 4px solid #0f766e;
            border-radius: 12px;
            padding: 20px 24px;
            margin-bottom: 24px;
        }
        .feedback-card h3 {
            font-size: 14px;
            font-weight: 600;
            color: #0f766e;
            margin-bottom: 8px;
        }
        .feedback-card p {
            font-size: 14px;
            color: #111111;
            line-height: 1.7;
            margin: 0;
        }
    </style>
</head>
<body>

<c:set var="currentPage" value="risks" scope="request"/>
<%@ include file="/WEB-INF/jsp/navbar.jsp" %>

<div class="container">

    <%-- page header --%>
    <div class="page-header">
        <div>
            <h2>Understanding Your Risk</h2>
            <p class="subtext">
                AI-generated educational summary for your
                submitted risk
            </p>
        </div>
        <a class="btn secondary"
           href="${pageContext.request.contextPath}/risks">
            Go to Risk Register
        </a>
    </div>

    <%-- risk summary header banner --%>
    <div class="risk-summary-header">
        <h2>${risk.riskTitle}</h2>
        <p>${risk.category.name} &nbsp;|&nbsp;
            Submitted successfully</p>
        <div class="meta">
            <div class="meta-item">
                <strong>${risk.likelihood}</strong>
                Likelihood
            </div>
            <div class="meta-item">
                <strong>${risk.impact}</strong>
                Impact
            </div>
            <div class="meta-item">
                <strong>${risk.severityScore}/25</strong>
                Severity Score
            </div>
            <div class="meta-item">
                <strong>${risk.status}</strong>
                Current Status
            </div>
        </div>
    </div>

    <%-- severity pill --%>
    <c:choose>
        <c:when test="${risk.severityScore >= 15}">
            <span class="severity-pill severity-high">
                High Severity Risk
            </span>
        </c:when>
        <c:when test="${risk.severityScore >= 8}">
            <span class="severity-pill severity-medium">
                Medium Severity Risk
            </span>
        </c:when>
        <c:otherwise>
            <span class="severity-pill severity-low">
                Low Severity Risk
            </span>
        </c:otherwise>
    </c:choose>

    <%-- what this risk means --%>
    <div class="edu-card-full">
        <span class="edu-card-icon">💡</span>
        <h3>What This Risk Means</h3>
        <p>${education.whatThisRiskMeans}</p>
    </div>

    <%-- two column grid --%>
    <div class="edu-grid">

        <div class="edu-card">
            <span class="edu-card-icon">📊</span>
            <h3>Understanding Your Severity Score</h3>
            <p>${education.severityExplained}</p>
        </div>

        <div class="edu-card">
            <span class="edu-card-icon">🔍</span>
            <h3>Common Causes of This Risk</h3>
            <p>${education.commonCauses}</p>
        </div>

        <div class="edu-card">
            <span class="edu-card-icon">🛡️</span>
            <h3>What Good Mitigation Looks Like</h3>
            <p>${education.whatGoodMitigationLooksLike}</p>
        </div>

        <div class="edu-card">
            <span class="edu-card-icon">✅</span>
            <h3>Submission Quality Feedback</h3>
            <p>${education.submissionQualityFeedback}</p>
        </div>

    </div>

    <%-- risk literacy score --%>
    <c:if test="${education.literacyScore > 0}">
        <div class="edu-card-full">
            <span class="edu-card-icon">🎯</span>
            <h3>Your Risk Literacy Score</h3>
            <div class="score-bar-wrap">
                <div class="score-bar-fill"
                     style="width: ${education.literacyScore * 10}%">
                </div>
            </div>
            <span class="score-label">
                ${education.literacyScore} / 10
            </span>
            <p style="margin-top:8px;">
                    ${education.literacyScoreReason}
            </p>
        </div>
    </c:if>

    <%-- what happens next --%>
    <div class="next-steps">
        <h3>What Happens Next in ChainGuard</h3>
        <div class="step">
            <div class="step-num">1</div>
            <p>Your risk has been added to the organisation's
                risk register and is currently in
                <strong>New</strong> status.</p>
        </div>
        <div class="step">
            <div class="step-num">2</div>
            <p>A manager will review your submission and
                assign ownership. The risk will move to
                <strong>Under Review</strong> status.</p>
        </div>
        <div class="step">
            <div class="step-num">3</div>
            <p>A mitigation task will be assigned to a
                team member with a deadline. The risk will
                move to <strong>Mitigating</strong> status.</p>
        </div>
        <div class="step">
            <div class="step-num">4</div>
            <p>Once the mitigation task is completed the
                risk will be marked as
                <strong>Resolved</strong> and formally
                <strong>Closed</strong> by an administrator.</p>
        </div>
        <p style="margin-top:12px; font-size:13px;
                  color:#0d9488;">
            ${education.whatHappensNext}
        </p>
    </div>

    <%-- submission quality feedback highlight --%>
    <div class="feedback-card">
        <h3>A Note From ChainGuard</h3>
        <p>The more detail you provide when submitting risks,
            the better ChainGuard's AI can guide your team
            towards effective mitigation. Clear descriptions,
            accurate severity scoring and appropriate
            categorisation all contribute to better outcomes
            for your organisation.</p>
    </div>

    <%-- actions --%>
    <div class="actions">
        <a href="${pageContext.request.contextPath}/risks"
           class="btn">
            View Risk Register
        </a>
        <a href="${pageContext.request.contextPath}/risks/create"
           class="btn secondary">
            Submit Another Risk
        </a>
    </div>

</div>

</body>
</html>