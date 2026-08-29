<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Notifications</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<c:set var="currentPage" value="notifications" scope="request"/>
<%@ include file="/WEB-INF/jsp/navbar.jsp" %>

<div class="container">

    <div class="page-header">
        <div>
            <h2>Notifications</h2>
            <p class="subtext">
                Stay informed about risk activity across
                your organisation
            </p>
        </div>
    </div>

    <div class="guide-bar" id="guide-notifications">
        <div class="guide-bar-header"
             onclick="toggleGuide('guide-notifications')">
            <div class="guide-bar-header-left">
                <div class="guide-icon">ℹ</div>
                <div>
                    <span>How does this page work?</span>
                    <p>Click to learn about your notifications</p>
                </div>
            </div>
            <span class="guide-chevron"
                  id="chevron-guide-notifications">▾</span>
        </div>
        <div class="guide-body"
             id="body-guide-notifications">
            <div class="guide-card">
                <span class="guide-card-icon">🔔</span>
                <h4>Why Notifications Matter</h4>
                <p>Timely notifications ensure risks and
                    mitigation tasks are never overlooked.
                    ISO 31000 emphasises that risk information
                    should reach the right people promptly.</p>
            </div>
            <div class="guide-card">
                <span class="guide-card-icon">⏰</span>
                <h4>Overdue Alerts</h4>
                <p>If a mitigation task deadline passes without
                    being completed the system automatically
                    marks it as overdue and notifies the assigned
                    user, risk owner and administrator.</p>
            </div>
            <div class="guide-card">
                <span class="guide-card-icon">📧</span>
                <h4>Email Notifications</h4>
                <p>Critical events such as high severity risk
                    submissions and membership changes also
                    trigger email notifications so stakeholders
                    are informed even when not logged in.</p>
            </div>
        </div>
    </div>

    <c:if test="${empty notifications}">
        <p class="empty-state">No notifications yet.</p>
    </c:if>

    <c:if test="${not empty notifications}">

        <%-- filter bar --%>
        <div class="filter-bar">
            <input type="text"
                   id="searchInput"
                   placeholder="Search notifications..."
                   oninput="filterNotifications()"/>

                <%-- dropdown built from types the user
                     actually has — no empty filter options --%>
            <select id="filterType"
                    onchange="filterNotifications()">
                <option value="">All Types</option>
                <c:forEach var="type"
                           items="${notificationTypes}">
                    <option value="${type}">
                        <c:choose>
                            <c:when test="${type == 'HIGH_RISK_SUBMITTED'}">
                                High Risk Submitted
                            </c:when>
                            <c:when test="${type == 'RISK_SUBMITTED'}">
                                Risk Submitted
                            </c:when>
                            <c:when test="${type == 'RISK_ASSIGNED'}">
                                Risk Assigned
                            </c:when>
                            <c:when test="${type == 'RISK_OWNER_ASSIGNED'}">
                                Risk Owner Assigned
                            </c:when>
                            <c:when test="${type == 'ASSIGNMENT_UPDATED'}">
                                Assignment Updated
                            </c:when>
                            <c:when test="${type == 'OVERDUE'}">
                                Overdue
                            </c:when>
                            <c:when test="${type == 'ASSIGNMENT_OVERDUE'}">
                                Assignment Overdue
                            </c:when>
                            <c:when test="${type == 'RISK_RESOLVED'}">
                                Risk Resolved
                            </c:when>
                            <c:when test="${type == 'ROLE_CHANGED'}">
                                Role Changed
                            </c:when>
                            <c:when test="${type == 'LOW_RISK_SUBMITTED'}">
                                Low Risk Submitted
                            </c:when>
                            <c:when test="${type == 'MODERATE_RISK_SUBMITTED'}">
                                Moderate Risk Submitted
                            </c:when>
                            <c:when test="${type == 'ASSIGNMENT_CREATED'}">
                                Risk Assigned
                            </c:when>
                            <c:when test="${type == 'STATUS_CHANGED'}">
                                Status Changed
                            </c:when>
                            <c:when test="${type == 'DEADLINE_NEAR'}">
                                Deadline Near</c:when>
                            <c:otherwise>
                                ${type}
                            </c:otherwise>
                        </c:choose>
                    </option>
                </c:forEach>
            </select>

            <button onclick="clearNotifFilters()"
                    class="btn secondary btn-small">
                Clear Filters
            </button>

            <span id="notifCount" class="muted"></span>
        </div>

        <%-- notification list --%>
        <ul class="notification-list" id="notificationList">
            <c:forEach var="notification"
                       items="${notifications}">
                <li class="notification-item
                        ${!notification.read ?
                        'notification-unread' : ''}"
                    data-message="${notification.message
                        .toLowerCase()}"
                    data-type="${notification.type}">

                    <div style="display:flex;
                                justify-content:space-between;
                                align-items:center;
                                flex-wrap:wrap;
                                gap:8px;">
                        <span class="badge
                            <c:choose>
                                <c:when test='${notification.type
                                    == "HIGH_RISK_SUBMITTED"}'>
                                    badge-high
                                </c:when>
                                 <c:when test='${notification.type == "OVERDUE"
                                    || notification.type == "ASSIGNMENT_OVERDUE"
                                    || notification.type == "DEADLINE_NEAR"}'>
                                    badge-medium
                                </c:when>
                                <c:otherwise>
                                    badge-low
                                </c:otherwise>
                            </c:choose>">
                            <c:choose>
                                <c:when test='${notification.type
                                    == "HIGH_RISK_SUBMITTED"}'>
                                    High Risk
                                </c:when>
                                <c:when test='${notification.type
                                    == "RISK_SUBMITTED"}'>
                                    Submitted
                                </c:when>
                                <c:when test='${notification.type
                                    == "RISK_ASSIGNED"}'>
                                    Assigned
                                </c:when>
                                <c:when test='${notification.type
                                    == "RISK_OWNER_ASSIGNED"}'>
                                    Owner Assigned
                                </c:when>
                                <c:when test='${notification.type
                                    == "ASSIGNMENT_UPDATED"}'>
                                    Assignment Updated
                                </c:when>
                                <c:when test='${notification.type
                                    == "OVERDUE"
                                    || notification.type
                                    == "ASSIGNMENT_OVERDUE"}'>
                                    Overdue
                                </c:when>
                                <c:when test='${notification.type
                                    == "RISK_RESOLVED"}'>
                                    Resolved
                                </c:when>
                                <c:when test='${notification.type
                                    == "ROLE_CHANGED"}'>
                                    Role Changed
                                </c:when>
                                <c:when test='${notification.type == "LOW_RISK_SUBMITTED"}'>
                                    Low Risk
                                </c:when>
                                <c:when test='${notification.type == "MODERATE_RISK_SUBMITTED"}'>
                                    Moderate Risk
                                </c:when>
                                <c:when test='${notification.type == "ASSIGNMENT_CREATED"}'>
                                    Assigned
                                </c:when>
                                <c:when test='${notification.type == "STATUS_CHANGED"}'>
                                    Status Changed
                                </c:when>
                                <c:when test='${notification.type == "DEADLINE_NEAR"}'>
                                    Deadline Near</c:when>
                                <c:otherwise>
                                    ${notification.type}
                                </c:otherwise>
                            </c:choose>
                        </span>

                        <small class="muted">
                                ${notification.createdAt}
                        </small>
                    </div>

                    <p style="margin-top:8px; font-size:14px;
                              color:#111111;">
                            ${notification.message}
                    </p>

                </li>
            </c:forEach>
        </ul>

        <p id="noNotifResults"
           class="muted"
           style="display:none; margin-top:16px;">
            No notifications match your search or filters.
        </p>

    </c:if>

</div>

<script>
    function toggleGuide(id) {
        var body = document.getElementById('body-' + id);
        var chevron = document.getElementById('chevron-' + id);
        body.classList.toggle('open');
        chevron.classList.toggle('open');
    }

    function filterNotifications() {
        var search = document.getElementById('searchInput')
            .value.toLowerCase().trim();
        var type = document.getElementById('filterType').value;

        var items = document.querySelectorAll(
            '#notificationList li');
        var visible = 0;

        items.forEach(function (item) {
            var message = item.getAttribute('data-message') || '';
            var itemType = item.getAttribute('data-type') || '';

            var matchSearch = message.includes(search);
            var matchType = !type || itemType === type;

            var show = matchSearch && matchType;
            item.style.display = show ? '' : 'none';
            if (show) visible++;
        });

        document.getElementById('notifCount').textContent =
            visible + ' notification' +
            (visible !== 1 ? 's' : '') + ' shown';

        document.getElementById('noNotifResults').style.display =
            visible === 0 ? 'block' : 'none';
    }

    function clearNotifFilters() {
        document.getElementById('searchInput').value = '';
        document.getElementById('filterType').value = '';
        filterNotifications();
    }

    filterNotifications();
</script>

</body>
</html>
