<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Risks</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<c:set var="currentPage" value="risks" scope="request"/>
<%@ include file="/WEB-INF/jsp/navbar.jsp" %>

<div class="container">

    <%-- page header --%>
    <div class="page-header">
        <div>
            <h2>Risk Register</h2>
            <p class="subtext">
                <strong>${organizationName}</strong>
                &nbsp;|&nbsp;
                All submitted risks for your organisation
            </p>
        </div>
        <a class="btn"
           href="${pageContext.request.contextPath}/risks/create">
            + Submit Risk
        </a>
    </div>

    <%-- guide bar --%>
        <div class="guide-bar" id="guide-risks">
            <div class="guide-bar-header"
                 onclick="toggleGuide('guide-risks')">
                <div class="guide-bar-header-left">
                    <div class="guide-icon">ℹ</div>
                    <div>
                        <span>How does this page work?</span>
                        <p>Click to learn about the risk register
                            and what each column means</p>
                    </div>
                </div>
                <span class="guide-chevron"
                      id="chevron-guide-risks">▾</span>
            </div>
            <div class="guide-body"
                 id="body-guide-risks">
                <div class="guide-card">
                    <span class="guide-card-icon">📋</span>
                    <h4>Risk</h4>
                    <p>The title and submitter of the risk. Click
                        any row to expand it and see the full
                        description, likelihood, impact and
                        mitigation details.</p>
                </div>
                <div class="guide-card">
                    <span class="guide-card-icon">🗂️</span>
                    <h4>Category</h4>
                    <p>The type of risk — for example Supplier Risk,
                        Financial Risk or Cybersecurity Risk.
                        Categories help your organisation identify
                        patterns and recurring problem areas.</p>
                </div>
                <div class="guide-card">
                    <span class="guide-card-icon">🎯</span>
                    <h4>Severity Score</h4>
                    <p>Calculated by multiplying likelihood by impact
                        (both rated 1 to 5). Scores of 1 to 7 are low,
                        8 to 14 are medium and 15 to 25 are high
                        severity requiring immediate attention.</p>
                </div>
                <div class="guide-card">
                    <span class="guide-card-icon">🔄</span>
                    <h4>Status</h4>
                    <p>The current stage of the risk in its lifecycle.
                        New means just submitted. Under Review means a
                        manager is assessing it. Mitigating means
                        action is underway. Resolved means complete.
                        Closed means formally archived.</p>
                </div>
                <div class="guide-card">
                    <span class="guide-card-icon">👤</span>
                    <h4>Owner</h4>
                    <p>The manager responsible for overseeing
                        mitigation of this risk. Every risk should
                        have an owner assigned. Unowned risks are
                        at greater risk of being overlooked.
                        Admins can assign owners from this column.</p>
                </div>
                <div class="guide-card">
                    <span class="guide-card-icon">🛡️</span>
                    <h4>Mitigation</h4>
                    <p>Shows the current status of the mitigation
                        task assigned to this risk. Assigned means
                        a task exists. In Progress means work has
                        started. Done means the task is complete.
                        Overdue means the deadline has passed.</p>
                </div>
                <div class="guide-card">
                    <span class="guide-card-icon">📅</span>
                    <h4>Deadline</h4>
                    <p>The date by which the mitigation task must
                        be completed. If this date passes without the
                        task being marked as Done, the system will
                        automatically mark it as Overdue and notify
                        the relevant users.</p>
                </div>
                <div class="guide-card">
                    <span class="guide-card-icon">⚡</span>
                    <h4>Actions</h4>
                    <p>What you can do with this risk. Assign creates
                        a mitigation task. Edit allows you to update
                        a risk you submitted while it is still New.
                        Update lets assigned users mark their task
                        progress. Closed risks cannot be edited.</p>
                </div>
            </div>
        </div>

    <%-- pdf export — manager and admin only --%>
    <c:if test="${isAdmin || isManager}">
        <div class="filter-bar" style="margin-bottom:16px;">
                <select id="pdf-severity" class="inline-select">
                    <option value="">All Severities</option>
                    <option value="HIGH">High</option>
                    <option value="MEDIUM">Medium</option>
                    <option value="LOW">Low</option>
                </select>
                <select id="pdf-status" class="inline-select">
                    <option value="">All Statuses</option>
                    <option value="NEW">New</option>
                    <option value="UNDER REVIEW">Under Review</option>
                    <option value="MITIGATING">Mitigating</option>
                    <option value="RESOLVED">Resolved</option>
                    <option value="CLOSED">Closed</option>
                </select>
                <select id="pdf-category" class="inline-select">
                    <option value="">All Categories</option>
                    <c:forEach var="cat" items="${categories}">
                        <option value="${cat.name}">${cat.name}</option>
                    </c:forEach>
                </select>
            <button class="btn secondary" onclick="exportPdf()">
                Export PDF Report
            </button>
        </div>
    </c:if>

    <%-- flash messages --%>
    <c:if test="${not empty param.assigned}">
        <div class="message-success">
            Mitigation action assigned successfully.
        </div>
    </c:if>
    <c:if test="${not empty param.assignmentUpdated}">
        <div class="message-success">
            Assignment updated successfully.
        </div>
    </c:if>
    <c:if test="${not empty param.created}">
        <div class="message-success">
            Risk submitted successfully.
        </div>
    </c:if>
    <c:if test="${not empty statusError}">
        <div class="message-error">${statusError}</div>
    </c:if>
    <c:if test="${not empty statusUpdated}">
        <div class="message-success">
            Risk status updated successfully.
        </div>
    </c:if>
    <c:if test="${not empty param.ownerAssigned}">
        <div class="message-success">
            Risk owner assigned successfully.
        </div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="message-error">${error}</div>
    </c:if>

    <c:if test="${empty risks}">
        <p class="empty-state">
            No risks have been submitted yet.
        </p>
    </c:if>

    <c:if test="${not empty risks}">

        <%-- filter bar --%>
        <div class="filter-bar">
            <input type="text"
                   id="searchInput"
                   placeholder="Search by title..."
                   oninput="filterTable()">

            <select id="filterStatus" onchange="filterTable()">
                <option value="">All Active Risks</option>
                <option value="NEW">New</option>
                <option value="UNDER REVIEW">Under Review</option>
                <option value="MITIGATING">Mitigating</option>
                <option value="RESOLVED">Resolved</option>
                <option value="CLOSED">Closed</option>
            </select>

            <select id="filterCategory" onchange="filterTable()">
                <option value="">All Categories</option>
                <c:forEach var="category" items="${categories}">
                    <option value="${category.name}">
                            ${category.name}
                    </option>
                </c:forEach>
            </select>

            <select id="filterSeverity" onchange="filterTable()">
                <option value="">All Severities</option>
                <option value="low">Low (1-7)</option>
                <option value="medium">Medium (8-14)</option>
                <option value="high">High (15+)</option>
            </select>

                <%-- owner filter — role aware --%>
            <c:choose>
                <c:when test="${isAdmin}">
            <select id="filterOwner" onchange="filterTable()">
                        <option value="">All Owners</option>
                        <option value="unassigned">Unassigned</option>
                <c:forEach var="entry" items="${ownerNamesByUserId}">
                    <option value="${entry.value}">
                            ${entry.value}
                    </option>
                </c:forEach>
            </select>
                </c:when>
                <c:when test="${isManager}">
                    <select id="filterOwner" onchange="filterTable()">
                        <option value="">All Risks</option>
                        <option value="me">My Risks</option>
                        <option value="unassigned">Unassigned</option>
                    </select>
                </c:when>
                <c:otherwise>
                    <input type="hidden" id="filterOwner" value=""/>
                </c:otherwise>
            </c:choose>

            <button onclick="clearFilters()"
                    class="btn secondary btn-small">
                Clear Filters
            </button>

            <span id="filterCount" class="muted"></span>
        </div>

        <p class="muted" style="margin-bottom:12px;">
            Click any row to see full mitigation details.
        </p>

        <div class="table-wrapper">
            <table class="risk-table" id="riskTable">
                <thead>
                <tr>
                    <th>Risk</th>
                    <th>Category</th>
                    <th>Severity</th>
                    <th>Status</th>
                    <th>Owner</th>
                    <th>Mitigation</th>
                    <th>Deadline</th>
                    <th>Actions</th>
                </tr>
                </thead>
                <tbody id="riskTableBody">
                <c:forEach var="risk" items="${risks}"
                           varStatus="loop">
                    <c:set var="assignment"
                           value="${assignmentsByRiskId[risk.riskId]}"/>
                    <c:set var="isRiskOwner"
                           value="${risk.riskOwner != null
                               && risk.riskOwner.userId
                               == currentUserId}"/>

                    <%-- admin can assign mitigation only
                         if no risk owner has been set --%>
                    <c:set var="adminCanAssign"
                           value="${isAdmin && risk.riskOwner == null}"/>

                    <c:choose>
                        <c:when test="${risk.severityScore >= 15}">
                            <c:set var="severityBand" value="high"/>
                        </c:when>
                        <c:when test="${risk.severityScore >= 8}">
                            <c:set var="severityBand" value="medium"/>
                        </c:when>
                        <c:otherwise>
                            <c:set var="severityBand" value="low"/>
                        </c:otherwise>
                    </c:choose>

                    <c:set var="ownerName"
                           value="${ownerNamesByUserId[risk.riskOwner.userId]}"/>

                    <%-- main risk row --%>
                    <tr class="risk-row"
                        onclick="toggleExpand('expand-${loop.index}')"
                        data-title="${risk.riskTitle.toLowerCase()}"
                        data-status="${risk.status}"
                        data-category="${risk.category.name}"
                        data-severity="${severityBand}"
                        data-owner="${not empty ownerName
                            ? ownerName : 'unassigned'}"
                        data-owner-id="${risk.riskOwner != null
                            ? risk.riskOwner.userId : ''}"
                        data-assignee-id="${assignment != null
                            ? assignment.assignedTo.userId : ''}"
                        data-closed="${risk.status == 'CLOSED'
                            ? 'true' : 'false'}">

                            <%-- risk title + submitted by --%>
                        <td>
                            <strong>${risk.riskTitle}</strong>
                            <span class="expand-hint">▸ details</span>
                            <c:if test="${risk.submittedBy != null}">
                                <div class="submitted-by">
                                    Submitted by
                                        ${submittedByNames[risk.submittedBy.userId] != null
                                                ? submittedByNames[risk.submittedBy.userId]
                                                : risk.submittedBy.email}
                                </div>
                            </c:if>
                        </td>

                            <%-- category --%>
                        <td>${risk.category.name}</td>

                            <%-- severity badge --%>
                        <td>
                            <span class="badge
                                <c:choose>
                                    <c:when test='${risk.severityScore >= 15}'>badge-high</c:when>
                                    <c:when test='${risk.severityScore >= 8}'>badge-medium</c:when>
                                    <c:otherwise>badge-low</c:otherwise>
                                </c:choose>">
                                    ${risk.severityScore}
                            </span>
                        </td>

                            <%-- risk status --%>
                        <td onclick="event.stopPropagation()">
                            <c:choose>
                                <c:when test="${isRiskOwner || isAdmin}">
                                    <form action="${pageContext.request.contextPath}
                                                  /risks/${risk.riskId}/status"
                                          method="post">
                                        <input type="hidden"
                                               name="${_csrf.parameterName}"
                                               value="${_csrf.token}"/>
                                        <select name="status"
                                                onchange="this.form.submit()"
                                                class="inline-select">
                                            <option value="UNDER REVIEW"
                                                ${risk.status == 'UNDER REVIEW' ? 'selected' : ''}>
                                                Under Review
                                            </option>
                                            <option value="MITIGATING"
                                                ${risk.status == 'MITIGATING' ? 'selected' : ''}>
                                                Mitigating
                                            </option>
                                            <option value="RESOLVED"
                                                ${risk.status == 'RESOLVED' ? 'selected' : ''}>
                                                Resolved
                                            </option>
                                            <c:if test="${isAdmin}">
                                                <option value="CLOSED"
                                                    ${risk.status == 'CLOSED' ? 'selected' : ''}>
                                                    Closed
                                                </option>
                                            </c:if>
                                        </select>
                                    </form>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge
                                        <c:choose>
                                            <c:when test='${risk.status == "RESOLVED"
                                                || risk.status == "CLOSED"}'>badge-low</c:when>
                                            <c:when test='${risk.status == "MITIGATING"
                                                || risk.status == "UNDER REVIEW"}'>badge-medium</c:when>
                                            <c:otherwise>badge-high</c:otherwise>
                                        </c:choose>">
                                            ${risk.status}
                                    </span>
                                </c:otherwise>
                            </c:choose>
                        </td>

                                    <%-- risk owner --%>
                                <td onclick="event.stopPropagation()">
                                    <c:choose>
                                        <c:when test="${isAdmin}">
                                            <form action="${pageContext.request.contextPath}
                  /risks/${risk.riskId}/owner"
                                                  method="post">
                                                <input type="hidden"
                                                       name="${_csrf.parameterName}"
                                                       value="${_csrf.token}"/>
                                                <select name="ownerUserId"
                                                        onchange="this.form.submit()"
                                                        class="inline-select">
                                                    <option value="">Assign owner</option>
                                                    <c:forEach var="profile"
                                                               items="${managerProfiles}">
                                                        <option value="${profile.user.userId}"
                                                            ${risk.riskOwner != null
                                                                    && risk.riskOwner.userId == profile.user.userId
                                                                    ? 'selected' : ''}>
                                                                ${profile.fullName}
                                                        </option>
                                                    </c:forEach>
                                                </select>
                                            </form>
                                        </c:when>
                                        <c:when test="${risk.riskOwner != null}">
                                            <span class="badge badge-low">
                                                 ${ownerNamesByUserId[risk.riskOwner.userId]}
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="muted">Unassigned</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                            <%-- mitigation status badge --%>
                        <td>
                            <c:choose>
                                <c:when test="${assignment != null}">
                                    <span class="badge
                                        <c:choose>
                                            <c:when test='${assignment.status == "DONE"}'>badge-low</c:when>
                                            <c:when test='${assignment.status == "IN_PROGRESS"}'>badge-medium</c:when>
                                            <c:when test='${assignment.status == "OVERDUE"}'>badge-high</c:when>
                                            <c:otherwise>badge-medium</c:otherwise>
                                        </c:choose>">
                                            ${assignment.status}
                                    </span>
                                </c:when>
                                <c:otherwise>
                                    <span class="muted">—</span>
                                </c:otherwise>
                            </c:choose>
                        </td>

                            <%-- deadline --%>
                        <td>
                            <c:choose>
                                <c:when test="${assignment != null
                                    && assignment.deadline != null}">
                                    ${assignment.deadline}
                                </c:when>
                                <c:otherwise>
                                    <span class="muted">—</span>
                                </c:otherwise>
                            </c:choose>
                        </td>

                            <%-- actions --%>
                        <td onclick="event.stopPropagation()">
                            <div class="action-buttons">

                                    <%-- edit risk — submitter only when NEW --%>
                                <c:if test="${risk.submittedBy.userId == currentUserId
                                        && risk.status == 'NEW'}">
                                    <a class="btn btn-small secondary"
                                       href="${pageContext.request.contextPath}
                                              /risks/${risk.riskId}/edit">
                                        Edit Risk Details
                                    </a>
                                </c:if>

                                    <%-- assign mitigation:
                                         risk owner (manager) can always assign
                                         admin can only assign if no risk owner set --%>
                                <c:if test="${assignment == null
                                    && risk.status != 'CLOSED'
                                    && (isRiskOwner || adminCanAssign)}">
                                    <a class="btn btn-small"
                                       href="${pageContext.request.contextPath}
                                             /risks/${risk.riskId}/assign">
                                        Assign Mitigation
                                    </a>
                                </c:if>

                                    <%-- edit mitigation assignment —
                                         admin or manager who is risk owner --%>
                                <c:if test="${assignment != null
                                    && risk.status != 'CLOSED'
                                    && (isAdmin || (isManager && isRiskOwner))}">
                                    <a class="btn btn-small secondary"
                                       href="${pageContext.request.contextPath}
                                              /assignments/${assignment.assignmentId}/edit">
                                        Edit Mitigation Assignment
                                    </a>
                                </c:if>

                                    <%-- update progress — assigned user only --%>
                                <c:if test="${assignment != null
                                    && risk.status != 'CLOSED'
                                    && !isAdmin && !isManager
                                    && assignment.assignedTo.userId == currentUserId}">
                                    <a class="btn btn-small secondary"
                                       href="${pageContext.request.contextPath}
                                              /assignments/${assignment.assignmentId}/edit">
                                        Update Mitigation Progress
                                    </a>
                                </c:if>

                                    <%-- closed label --%>
                                <c:if test="${risk.status == 'CLOSED'}">
                                    <span class="muted">Closed</span>
                                </c:if>

                                    <%-- no action available --%>
                                <c:if test="${assignment == null
                                    && !isRiskOwner && !adminCanAssign
                                    && risk.status != 'CLOSED'}">
                                    <span class="muted">—</span>
                                </c:if>

                            </div>
                        </td>
                    </tr>

                    <%-- expandable detail row --%>
                    <tr class="expand-row" id="expand-${loop.index}">
                        <td colspan="8">
                            <div class="expand-detail">

                                <div class="expand-detail-item">
                                    <span class="expand-detail-label">Description</span>
                                    <span class="expand-detail-value">
                                            ${not empty risk.description ? risk.description : '—'}
                                    </span>
                                </div>

                                <div class="expand-detail-item">
                                    <span class="expand-detail-label">Likelihood</span>
                                    <span class="expand-detail-value">
                                        ${risk.likelihood} / 5
                                    </span>
                                </div>

                                <div class="expand-detail-item">
                                    <span class="expand-detail-label">Impact</span>
                                    <span class="expand-detail-value">
                                        ${risk.impact} / 5
                                    </span>
                                </div>

                                <c:if test="${assignment != null}">
                                    <div class="expand-detail-item">
                                        <span class="expand-detail-label">Assigned To</span>
                                        <span class="expand-detail-value">
                                                ${assignment.assignedTo.email}
                                        </span>
                                    </div>
                                    <div class="expand-detail-item">
                                        <span class="expand-detail-label">Action Details</span>
                                        <span class="expand-detail-value">
                                                ${assignment.actionDetails}
                                        </span>
                                    </div>
                                </c:if>

                                <c:if test="${assignment == null}">
                                    <div class="expand-detail-item">
                                        <span class="expand-detail-label">Mitigation</span>
                                        <span class="expand-detail-value muted">
                                            No mitigation assigned yet
                                        </span>
                                    </div>
                                </c:if>

                            </div>
                        </td>
                    </tr>

                </c:forEach>
                </tbody>
            </table>
        </div>

        <p id="noResults"
           class="muted"
           style="display:none; margin-top:16px;">
            No risks match your search or filters.
        </p>

    </c:if>

</div>

<script>
    var myId = '${currentUserId}';

    function toggleExpand(id) {
        var row = document.getElementById(id);
        if (row) {
            row.classList.toggle('open');
            var mainRow = row.previousElementSibling;
            if (mainRow) {
                var hint = mainRow.querySelector('.expand-hint');
                if (hint) {
                    hint.textContent = row.classList.contains('open')
                        ? '▾ hide' : '▸ details';
                }
            }
        }
    }

    function filterTable() {
        var search = document.getElementById('searchInput')
            .value.toLowerCase().trim();
        var status = document.getElementById('filterStatus').value;
        var category = document.getElementById('filterCategory').value;
        var severity = document.getElementById('filterSeverity').value;
        var ownerEl = document.getElementById('filterOwner');
        var owner = ownerEl ? ownerEl.value : '';

        var rows = document.querySelectorAll(
            '#riskTableBody tr.risk-row');
        var visibleCount = 0;

        rows.forEach(function (row) {
            var expandRow = row.nextElementSibling;
            var title = row.getAttribute('data-title') || '';
            var rowStatus = row.getAttribute('data-status') || '';
            var rowCategory = row.getAttribute('data-category') || '';
            var rowSeverity = row.getAttribute('data-severity') || '';
            var rowOwner = row.getAttribute('data-owner') || '';
            var rowOwnerId = row.getAttribute('data-owner-id') || '';
            var isClosed = row.getAttribute('data-closed') === 'true';

            if (isClosed && status !== 'CLOSED') {
                row.style.display = 'none';
                if (expandRow) expandRow.style.display = 'none';
                return;
            }

            if (status === 'CLOSED' && !isClosed) {
                row.style.display = 'none';
                if (expandRow) expandRow.style.display = 'none';
                return;
            }

            var matchSearch = title.includes(search);
            var matchStatus = !status || status === 'CLOSED'
                || rowStatus === status;
            var matchCategory = !category || rowCategory === category;
            var matchSeverity = !severity || rowSeverity === severity;
            var matchOwner = !owner ||
                (owner === 'me'
                    ? rowOwnerId === myId
                    : owner === 'unassigned'
                        ? rowOwner === 'unassigned'
                        : rowOwner.toLowerCase()
                        === owner.toLowerCase());

            var visible = matchSearch && matchStatus
                && matchCategory && matchSeverity && matchOwner;

            row.style.display = visible ? '' : 'none';
            if (expandRow) expandRow.style.display = visible ? '' : 'none';
            if (visible) visibleCount++;
        });

        document.getElementById('filterCount').textContent =
            visibleCount + ' risk' +
            (visibleCount !== 1 ? 's' : '') + ' shown';

        document.getElementById('noResults').style.display =
            visibleCount === 0 ? 'block' : 'none';
    }

    function clearFilters() {
        document.getElementById('searchInput').value = '';
        document.getElementById('filterStatus').value = '';
        document.getElementById('filterCategory').value = '';
        document.getElementById('filterSeverity').value = '';
        var ownerEl = document.getElementById('filterOwner');
        if (ownerEl) ownerEl.value = '';
        filterTable();
    }

    function exportPdf() {
        const severity = document.getElementById('pdf-severity').value;
        const status = document.getElementById('pdf-status').value;
        const category = document.getElementById('pdf-category').value;
        let url = '${pageContext.request.contextPath}'
            + '/risks/export/pdf?';
        if (severity) url += 'severity='
            + encodeURIComponent(severity) + '&';
        if (status) url += 'status='
            + encodeURIComponent(status) + '&';
        if (category) url += 'category='
            + encodeURIComponent(category) + '&';
        window.location.href = url;
    }

    function toggleGuide(id) {
        var body = document.getElementById('body-' + id);
        var chevron = document.getElementById('chevron-' + id);
        body.classList.toggle('open');
        chevron.classList.toggle('open');
    }

    filterTable();
</script>

</body>
</html>
