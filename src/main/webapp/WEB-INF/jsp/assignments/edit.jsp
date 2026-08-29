<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Edit Assignment</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<c:set var="currentPage" value="risks" scope="request"/>
<%@ include file="/WEB-INF/jsp/navbar.jsp" %>

<div class="container">

    <div class="page-header">
        <div>
            <h2>Edit Mitigation Assignment</h2>
            <p class="subtext">
                Risk: <strong>${assignment.risk.riskTitle}</strong>
            </p>
        </div>
        <a class="btn secondary"
           href="${pageContext.request.contextPath}/risks">
            Back to Risks
        </a>
    </div>

    <c:if test="${not empty error}">
        <div class="message-error">${error}</div>
    </c:if>

    <c:if test="${not empty success}">
        <div class="message-success">${success}</div>
    </c:if>

    <div class="card">
        <form action="${pageContext.request.contextPath}
                      /assignments/${assignment.assignmentId}/edit"
              method="post">

            <input type="hidden"
                   name="${_csrf.parameterName}"
                   value="${_csrf.token}"/>

            <%-- assignee — editable by manager/admin only --%>
            <c:choose>
                <c:when test="${isAdminOrManager}">
                    <label for="assignedToUserId">
                        Assigned To
                    </label>
                    <select id="assignedToUserId"
                            name="assignedToUserId">
                        <c:forEach var="profile"
                                   items="${profiles}">
                            <option value="${profile.user.userId}"
                                    <c:if test="${profile.user.userId ==
                                    assignment.assignedTo.userId}">
                                        selected
                                    </c:if>>
                                    ${profile.fullName}
                                — ${profile.jobRole}
                            </option>
                        </c:forEach>
                    </select>
                </c:when>
                <c:otherwise>
                    <label>Assigned To</label>
                    <p class="muted" style="margin-top:4px;">
                            ${assignment.assignedTo.email}
                    </p>
                </c:otherwise>
            </c:choose>

            <%-- action details — editable by manager/admin only --%>
            <label for="actionDetails">Action Details</label>
            <c:choose>
                <c:when test="${isAdminOrManager}">
                    <textarea id="actionDetails"
                              name="actionDetails"
                              rows="4">${assignment.actionDetails}</textarea>
                </c:when>
                <c:otherwise>
                    <p class="muted" style="margin-top:4px;">
                            ${assignment.actionDetails}
                    </p>
                </c:otherwise>
            </c:choose>

            <%-- deadline — editable by manager/admin only --%>
            <label for="deadline">Deadline</label>
            <c:choose>
                <c:when test="${isAdminOrManager}">
                    <input type="date"
                           id="deadline"
                           name="deadline"
                           value="${assignment.deadline}"
                           min="${currentDate}"/>
                </c:when>
                <c:otherwise>
                    <p class="muted" style="margin-top:4px;">
                        <c:choose>
                            <c:when test="${assignment.deadline
                                != null}">
                                ${assignment.deadline}
                            </c:when>
                            <c:otherwise>No deadline set</c:otherwise>
                        </c:choose>
                    </p>
                </c:otherwise>
            </c:choose>

            <%-- status — editable by all roles --%>
            <label for="status">Status</label>
            <select id="status" name="status" required>
                <c:choose>
                    <c:when test="${assignment.status
                        == 'OVERDUE'}">
                        <option value="OVERDUE" selected>
                            Overdue (set automatically)
                        </option>
                    </c:when>
                    <c:otherwise>
                        <option value="ASSIGNED"
                            ${assignment.status == 'ASSIGNED'
                                    ? 'selected' : ''}>
                            Assigned
                        </option>
                        <option value="IN_PROGRESS"
                            ${assignment.status == 'IN_PROGRESS'
                                    ? 'selected' : ''}>
                            In Progress
                        </option>
                        <option value="DONE"
                            ${assignment.status == 'DONE'
                                    ? 'selected' : ''}>
                            Done
                        </option>
                    </c:otherwise>
                </c:choose>
            </select>

            <div class="actions">
                <button type="submit" class="btn">
                    Save Changes
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