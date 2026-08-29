<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c"
           uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Forgot Password</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="auth-container">
    <h2>Forgot Password</h2>
    <p class="subtext">Enter your email address and we
        will send you a link to reset your password.</p>

    <c:if test="${not empty message}">
        <div class="message-success">${message}</div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="message-error">${error}</div>
    </c:if>

    <form method="post"
          action="${pageContext.request.contextPath}
                  /auth/forgot-password">
        <input type="hidden"
               name="${_csrf.parameterName}"
               value="${_csrf.token}"/>

        <label for="email">Email Address</label>
        <input type="email" id="email" name="email"
               required
               placeholder="Enter your email"/>

        <button type="submit" class="btn">
            Send Reset Link
        </button>
    </form>

    <p style="margin-top:16px; font-size:13px;">
        <a href="${pageContext.request.contextPath}
                 /auth/login">
            Back to Login
        </a>
    </p>
</div>
</body>
</html>