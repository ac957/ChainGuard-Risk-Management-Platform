<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Assign Mitigation</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<c:set var="currentPage" value="risks" scope="request"/>
<%@ include file="/WEB-INF/jsp/navbar.jsp" %>

<div class="container">

    <div class="page-header">
            <div>
                <h2>Assign Mitigation Action</h2>
                <p class="subtext">
                    Risk: <strong>${risk.riskTitle}</strong>
                </p>
            </div>
        <a class="btn secondary"
           href="${pageContext.request.contextPath}/risks">
            Back to Risks
        </a>
    </div>

    <%-- AI guidance section --%>
    <c:choose>
        <c:when test="${aiGuidance != null}">
            <div class="notification-item"
                 style="margin-bottom: 20px;">
                <h3 style="margin-bottom: 12px;">AI Guidance</h3>

                <p><strong>Summary:</strong>
                        ${aiGuidance.riskSummary}</p>

                <p style="margin-top: 8px;">
                    <strong>Urgency:</strong>
                    <span class="badge
                        <c:choose>
                            <c:when test='${aiGuidance.urgencyLevel == "HIGH"}'>badge-high</c:when>
                            <c:when test='${aiGuidance.urgencyLevel == "MODERATE"}'>badge-medium</c:when>
                            <c:otherwise>badge-low</c:otherwise>
                        </c:choose>">
                            ${aiGuidance.urgencyLevel}
                    </span>
                </p>

                <p style="margin-top: 8px;">
                    <strong>Suggested assignee:</strong>
                        ${aiGuidance.recommendedAssigneeType}
                </p>

                <c:if test="${not empty aiGuidance.recommendedAssigneeName
                    && aiGuidance.recommendedAssigneeName != 'Unavailable'
                    && aiGuidance.recommendedAssigneeName != 'No specific recommendation'}">
                    <p style="margin-top: 8px;">
                        <strong>Recommended person:</strong>
                        <span class="badge badge-low">
                                ${aiGuidance.recommendedAssigneeName}
                        </span>
                    </p>
                    <p style="margin-top: 4px;">
                        <strong>Reason:</strong>
                            ${aiGuidance.recommendedAssigneeReason}
                    </p>
                </c:if>

                <p style="margin-top: 8px;">
                    <strong>Recommended actions:</strong>
                </p>
                <ul style="margin-top: 4px; padding-left: 20px;">
                    <c:forEach var="action"
                               items="${aiGuidance.recommendedActions}">
                        <li style="margin-bottom: 4px;">${action}</li>
                    </c:forEach>
                </ul>

                <p style="margin-top: 8px;">
                    <strong>Monitoring advice:</strong>
                        ${aiGuidance.monitoringAdvice}
                </p>

                <p style="margin-top: 8px;">
                    <strong>Explanation:</strong>
                        ${aiGuidance.explanation}
                </p>

                <div class="actions" style="margin-top: 12px;">
                    <a class="btn secondary"
                       href="${pageContext.request.contextPath}
                             /risks/${risk.riskId}/assign/ai-guidance">
                        Regenerate AI Guidance
                    </a>
                </div>
            </div>
        </c:when>

        <c:otherwise>
            <div class="notification-item"
                 style="margin-bottom: 20px; text-align: center;">
                <p class="subtext" style="margin-bottom: 12px;">
                    Generate AI guidance to get recommended
                    mitigation actions, urgency level and assignee
                    suggestions based on this risk.
                </p>
                <a class="btn secondary"
                   href="${pageContext.request.contextPath}
                         /risks/${risk.riskId}/assign/ai-guidance">
                    Generate AI Guidance
                </a>
            </div>
        </c:otherwise>
    </c:choose>

    <hr/>

    <c:if test="${not empty error}">
        <div class="message-error">${error}</div>
    </c:if>

    <div class="card">
    <form action="${pageContext.request.contextPath}
                  /risks/${risk.riskId}/assign"
          method="post">

        <input type="hidden"
               name="${_csrf.parameterName}"
               value="${_csrf.token}"/>

        <label for="assignedToUserId">Assign to</label>
        <select id="assignedToUserId"
                name="assignedToUserId" required>
            <option value="">Select a team member</option>
            <c:forEach var="profile" items="${profiles}">
                <option value="${profile.user.userId}">
                        ${profile.fullName} — ${profile.jobRole}
                </option>
            </c:forEach>
        </select>

        <label for="actionDetails">Action Details</label>
        <textarea id="actionDetails" name="actionDetails"
                      rows="5" required minlength="20"
                      placeholder="Describe what needs to be done — be specific about the steps required">
            </textarea>

        <label for="deadline">Deadline</label>
        <input id="deadline" type="date" name="deadline"
               required
               min="${currentDate}"/>

        <div class="actions">
            <button type="submit" class="btn">
                Assign Action
            </button>
                <a href="${pageContext.request.contextPath}/risks"
                   class="btn secondary">
                    Cancel
                </a>
            </div>

        </form>
    </div>

</div>

</body>
</html>