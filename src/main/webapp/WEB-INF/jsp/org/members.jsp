<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Organisation Members</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<c:set var="currentPage" value="members" scope="request"/>
<%@ include file="/WEB-INF/jsp/navbar.jsp" %>

<div class="container">

    <div class="page-header">
        <div>
            <h2>Organisation Members</h2>
            <p class="subtext">
                <strong>${organizationName}</strong>
                &nbsp;|&nbsp;
                Manage your organisation's members
                and access levels
            </p>
        </div>
    </div>

    <c:if test="${not empty successMessage}">
        <div class="message-success">
                ${successMessage}
        </div>
    </c:if>

    <c:if test="${not empty errorMessage}">
        <div class="message-error">
                ${errorMessage}
        </div>
    </c:if>

    <c:if test="${empty members}">
        <p class="empty-state">
            No other members in your organisation yet.
        </p>
    </c:if>

    <c:if test="${not empty members}">
        <div class="table-wrapper">
            <table class="risk-table">
                <thead>
                <tr>
                    <th>Full Name</th>
                    <th>Email</th>
                    <th>Job Role</th>
                    <th>Department</th>
                    <th>System Role</th>
                    <th>Change Role</th>
                    <th>Actions</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="member" items="${members}">
                    <c:set var="profile"
                           value="${profileMap[member.user.userId]}"/>
                    <tr>
                        <td>
                            <c:choose>
                                <c:when test="${profile != null}">
                                    ${profile.fullName}
                                </c:when>
                                <c:otherwise>
                                    <span class="muted">
                                        No profile
                                    </span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>${member.user.email}</td>
                        <td>
                            <c:choose>
                                <c:when test="${profile != null
                                    && not empty profile.jobRole}">
                                    ${profile.jobRole}
                                </c:when>
                                <c:otherwise>
                                    <span class="muted">—</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${profile != null
                                    && not empty profile.department}">
                                    ${profile.department}
                                </c:when>
                                <c:otherwise>
                                    <span class="muted">—</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <span class="badge
                                <c:choose>
                                    <c:when test='${member.role.name
                                        == "MANAGER"}'>
                                        badge-medium
                                    </c:when>
                                    <c:otherwise>
                                        badge-low
                                    </c:otherwise>
                                </c:choose>">
                                    ${member.role.name}
                            </span>
                        </td>

                            <%-- change role form --%>
                        <td>
                            <form action="${pageContext.request.contextPath}
                                          /org/members/change-role"
                                  method="post">
                                <input type="hidden"
                                       name="${_csrf.parameterName}"
                                       value="${_csrf.token}"/>
                                <input type="hidden"
                                       name="membershipId"
                                       value="${member.membershipId}"/>
                                <div style="display:flex;
                                            gap:8px;
                                            align-items:center;">
                                    <select name="newRole"
                                            style="font-size:12px;
                                                   padding:4px;
                                                   border-radius:4px;
                                                   border:1px solid #ccc;">
                                        <option value="USER"
                                            ${member.role.name == 'USER'
                                                    ? 'selected' : ''}>
                                            User
                                        </option>
                                        <option value="MANAGER"
                                            ${member.role.name == 'MANAGER'
                                                    ? 'selected' : ''}>
                                            Manager
                                        </option>
                                    </select>
                                    <button type="submit"
                                            class="btn btn-small">
                                        Update
                                    </button>
                                </div>
                            </form>
                        </td>

                            <%-- remove member form --%>
                        <td>
                            <form action="${pageContext.request.contextPath}
                                          /org/members/remove"
                                  method="post"
                                  onsubmit="return confirm(
                                          'Are you sure you want to remove '
                                          + '${member.user.email}'
                                          + ' from the organisation? '
                                          + 'They will be notified by email.');">
                                <input type="hidden"
                                       name="${_csrf.parameterName}"
                                       value="${_csrf.token}"/>
                                <input type="hidden"
                                       name="membershipId"
                                       value="${member.membershipId}"/>
                                <button type="submit"
                                        class="btn btn-small secondary">
                                    Remove
                                </button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </c:if>

</div>
</body>
</html>