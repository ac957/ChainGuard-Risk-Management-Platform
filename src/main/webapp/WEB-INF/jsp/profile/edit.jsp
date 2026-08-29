<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Edit Profile</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<c:set var="currentPage" value="profile" scope="request"/>
<%@ include file="/WEB-INF/jsp/navbar.jsp" %>

<div class="auth-container">

    <div class="page-header">
        <h2>My Profile</h2>
    </div>

    <c:if test="${not empty param.saved}">
        <div class="message-success">
            Profile updated successfully.
        </div>
    </c:if>

    <c:if test="${not empty error}">
        <div class="message-error">${error}</div>
    </c:if>

    <form method="post"
          action="${pageContext.request.contextPath}/profile/edit"
          autocomplete="off">

        <input type="hidden"
               name="${_csrf.parameterName}"
               value="${_csrf.token}"/>

        <label for="fullName">Full Name</label>
        <input id="fullName" name="fullName" type="text"
               required minlength="2" maxlength="50"
               value="${fullName}"/>

        <label for="jobRole">Job Role</label>
        <input id="jobRole" name="jobRole" type="text"
               required minlength="2" maxlength="50"
               placeholder="e.g. Risk Analyst, Manager"
               value="${jobRole}"/>

        <label for="department">Department</label>
        <input id="department" name="department" type="text"
               required minlength="2" maxlength="50"
               placeholder="e.g. Operations, Logistics"
               value="${department}"/>

        <label for="phoneNumber">Phone Number (optional)</label>
        <input id="phoneNumber" name="phoneNumber" type="text"
               maxlength="20"
               placeholder="e.g. +44 7911 123456"
               value="${phoneNumber}"/>

        <div class="actions">
            <button type="submit" class="btn">
                Save Changes
            </button>
            <a class="btn secondary"
               href="${pageContext.request.contextPath}/dashboard">
                Cancel
            </a>
        </div>

    </form>
</div>

</body>
</html>
