<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Login</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="auth-container">

    <h2>Login</h2>

    <%
        String logout = request.getParameter("logout");
        String registered = request.getParameter("registered");
        String error = request.getParameter("error");
        String pending = request.getParameter("pending");
    %>

    <% if (logout != null) { %>
    <div class="message-success">You have been logged out.</div>
    <% } else if (registered != null) { %>
    <div class="message-success">
        Registration successful — please log in.
    </div>
    <% } %>

    <% if (error != null) { %>
    <div class="message-error">
        Invalid email or password. Please try again.
    </div>
    <% } %>

    <% if ("true".equals(pending)) { %>
    <div class="message-info">
        Your organisation request is awaiting admin approval.
        You will be notified once your account has been reviewed.
    </div>
    <% } %>

    <c:if test="${not empty param.restricted}">
        <div class="message-error">
            Your account has been restricted from making
            further join requests. You have been rejected
            from 3 organisations. Please contact support
            for assistance.
        </div>
    </c:if>

    <c:if test="${not empty param.passwordReset}">
        <div class="message-success">
            Your password has been reset successfully.
            Please log in with your new password.
        </div>
    </c:if>

    <form method="post"
          action="${pageContext.request.contextPath}/auth/login"
          autocomplete="off">

        <input type="hidden"
               name="${_csrf.parameterName}"
               value="${_csrf.token}"/>

        <label for="email">Email</label>
        <input id="email" name="email" type="email"
               placeholder="Email" required
               autocomplete="email"/>

        <label for="password">Password</label>
        <input id="password" name="password" type="password"
               placeholder="Password" required
               autocomplete="current-password"/>

        <div class="actions">
            <button type="submit" class="btn">Login</button>
            <a class="btn secondary"
               href="${pageContext.request.contextPath}
                     /auth/register">
                Register
            </a>
        </div>

        <p style="margin-top:12px; font-size:13px;
                  text-align:center;">
            <a href="${pageContext.request.contextPath}
                     /auth/forgot-password">
                Forgot your password?
            </a>
        </p>

    </form>
</div>
</body>
</html>