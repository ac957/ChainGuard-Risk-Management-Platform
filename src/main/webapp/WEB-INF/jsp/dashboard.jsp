<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Dashboard</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<c:set var="currentPage" value="dashboard" scope="request"/>
<%@ include file="/WEB-INF/jsp/navbar.jsp" %>

<div class="container">

    <%-- PAGE HEADER --%>
    <div class="page-header">
        <div>
            <h2>Welcome back</h2>
            <p class="subtext">
                <strong>${organizationName}</strong>
                &nbsp;|&nbsp;
                Role: <strong>${systemRole}</strong>
            </p>
        </div>
        <a class="btn"
           href="${pageContext.request.contextPath}/risks/create">
            + Submit Risk
        </a>
    </div>
        <div class="guide-bar" id="guide-dashboard">
            <div class="guide-bar-header"
                 onclick="toggleGuide('guide-dashboard')">
                <div class="guide-bar-header-left">
                    <div class="guide-icon">ℹ</div>
                    <div>
                        <span>How does this page work?</span>
                        <p>Click to learn about your risk dashboard</p>
                    </div>
                </div>
                <span class="guide-chevron"
                      id="chevron-guide-dashboard">▾</span>
            </div>
            <div class="guide-body"
                 id="body-guide-dashboard">
                <div class="guide-card">
                    <span class="guide-card-icon">⚠️</span>
                    <h4>What is a Risk?</h4>
                    <p>A risk is any event or condition that could
                        negatively impact your organisation's operations,
                        finances or reputation. Identifying risks early
                        allows you to act before they become problems.</p>
                </div>
                <div class="guide-card">
                    <span class="guide-card-icon">📊</span>
                    <h4>Your Statistics</h4>
                    <p>The cards below show a live summary of your
                        organisation's risk landscape. Click each card
                        to learn what the number means and why it
                        matters.</p>
                </div>
                <div class="guide-card">
                    <span class="guide-card-icon">🔄</span>
                    <h4>Risk Lifecycle</h4>
                    <p>Every risk moves through stages — New, Under
                        Review, Mitigating, Resolved and Closed. Your
                        goal is to progress risks through this lifecycle
                        by assigning and completing mitigation actions.</p>
                </div>
                <div class="guide-card">
                    <span class="guide-card-icon">🏢</span>
                    <h4>Why This Matters</h4>
                    <p>Organisations that manage risks proactively are
                        more resilient. ChainGuard helps your team
                        identify, assess and mitigate risks in a
                        structured and transparent way.</p>
                </div>
            </div>
        </div>

    <hr/>

        <%-- QUICK STATS --%>
        <div class="stats-row">

            <div class="stat-card flip-card"
                 onclick="this.classList.toggle('flipped')">
                <div class="flip-card-inner">
                    <div class="flip-card-front">
                        <span class="stat-number">${totalRisks}</span>
                        <span class="stat-label">
                    <c:choose>
                        <c:when test="${isAdmin}">
                            Active Org Risks
                        </c:when>
                        <c:when test="${isManager}">
                            Risks You Own
                        </c:when>
                        <c:otherwise>
                            Your Assigned Tasks
                        </c:otherwise>
                    </c:choose>
                </span>
                        <span class="flip-hint">click to learn more</span>
                    </div>
                    <div class="flip-card-back">
                        <p>
                            <c:choose>
                                <c:when test="${isAdmin}">
                                    All active risks currently
                                    being tracked across your
                                    organisation.
                                </c:when>
                                <c:when test="${isManager}">
                                    Active risks where you are the
                                    designated owner and are
                                    responsible for mitigation.
                                </c:when>
                                <c:otherwise>
                                    Mitigation tasks assigned
                                    to you to help reduce
                                    active risks.
                                </c:otherwise>
                            </c:choose>
                        </p>
                        <span class="flip-hint">click to go back</span>
                    </div>
                </div>
            </div>

            <div class="stat-card stat-card-high flip-card"
                 onclick="this.classList.toggle('flipped')">
                <div class="flip-card-inner">
                    <div class="flip-card-front">
                        <span class="stat-number">${highSeverityCount}</span>
                        <span class="stat-label">High Severity</span>
                        <span class="flip-hint">click to learn more</span>
                    </div>
                    <div class="flip-card-back">
                        <p>
                            Active risks scoring 15 or above.
                            Calculated by likelihood x impact.
                            These need immediate attention.
                        </p>
                        <span class="flip-hint">click to go back</span>
                    </div>
                </div>
            </div>

            <div class="stat-card stat-card-warn flip-card"
                 onclick="this.classList.toggle('flipped')">
                <div class="flip-card-inner">
                    <div class="flip-card-front">
                        <span class="stat-number">${unresolvedCount}</span>
                        <span class="stat-label">Unresolved</span>
                        <span class="flip-hint">click to learn more</span>
                    </div>
                    <div class="flip-card-back">
                        <p>
                            Risks still requiring action.
                            A risk is resolved only when all
                            mitigation tasks are marked Done.
                        </p>
                        <span class="flip-hint">click to go back</span>
                    </div>
                </div>
            </div>

            <div class="stat-card flip-card"
                 onclick="this.classList.toggle('flipped')">
                <div class="flip-card-inner">
                    <div class="flip-card-front">
                        <span class="stat-number">${resolvedCount}</span>
                        <span class="stat-label">Resolved / Closed</span>
                        <span class="flip-hint">click to learn more</span>
                    </div>
                    <div class="flip-card-back">
                        <p>
                            Risks fully mitigated and signed off.
                            Closed risks are archived for audit
                            purposes.
                        </p>
                        <span class="flip-hint">click to go back</span>
                    </div>
                </div>
            </div>

        </div>

    <%-- RECENT RISKS SNAPSHOT --%>
    <div class="section-header">
        <h3>${recentRisksHeading}</h3>
        <a href="${pageContext.request.contextPath}/risks">
            View all →
        </a>
    </div>

    <c:if test="${empty recentRisks}">
        <p class="empty-state">No risks submitted yet.</p>
    </c:if>

    <c:if test="${not empty recentRisks}">
        <div class="table-wrapper">
            <table class="risk-table">
                <thead>
                <tr>
                    <th>Title</th>
                    <th>Category</th>
                    <th>Severity</th>
                    <th>Status</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="risk" items="${recentRisks}">
                    <tr>
                        <td>${risk.riskTitle}</td>
                        <td>${risk.category.name}</td>
                        <td>
                            <span class="badge
                                <c:choose>
                                    <c:when test='${risk.severityScore >= 15}'>
                                        badge-high
                                    </c:when>
                                    <c:when test='${risk.severityScore >= 8}'>
                                        badge-medium
                                    </c:when>
                                    <c:otherwise>
                                        badge-low
                                    </c:otherwise>
                                </c:choose>">
                                    ${risk.severityScore}
                            </span>
                        </td>
                        <td>${risk.status}</td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </c:if>

    <hr/>

    <%-- RECENT NOTIFICATIONS SNAPSHOT --%>
    <div class="section-header">
        <h3>
            Recent Notifications
            <c:if test="${unreadCount > 0}">
                <span class="badge badge-high">${unreadCount} new</span>
            </c:if>
        </h3>
        <a href="${pageContext.request.contextPath}/notifications">
            View all →
        </a>
    </div>

    <c:if test="${empty recentNotifications}">
        <p class="empty-state">No notifications yet.</p>
    </c:if>

    <c:if test="${not empty recentNotifications}">
        <ul class="notification-list">
            <c:forEach var="notification" items="${recentNotifications}">
                <li class="notification-item">
                    <strong>${notification.type}</strong> —
                        ${notification.message}
                    <br/>
                    <small class="muted">
                            ${notification.createdAt}
                    </small>
                </li>
            </c:forEach>
        </ul>
    </c:if>

        <script>
            function toggleGuide(id) {
                var body = document.getElementById('body-' + id);
                var chevron = document.getElementById('chevron-' + id);
                body.classList.toggle('open');
                chevron.classList.toggle('open');
            }
        </script>

</div>
</body>
</html>