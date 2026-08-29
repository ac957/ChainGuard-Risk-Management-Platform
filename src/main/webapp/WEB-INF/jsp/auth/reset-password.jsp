<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Reset Password</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="auth-container">

    <h2>Reset Your Password</h2>
    <p class="subtext">Enter your new password below.</p>

    <c:if test="${not empty error}">
        <div class="message-error">${error}</div>
    </c:if>

    <form method="post"
          action="${pageContext.request.contextPath}
                  /auth/reset-password">
        <input type="hidden"
               name="${_csrf.parameterName}"
               value="${_csrf.token}"/>
        <input type="hidden"
               name="token"
               value="${token}"/>

        <label for="newPassword">New Password</label>
        <p style="font-size:12px; color:#888;
                  margin:0 0 4px 0;">
            8-64 characters, must include uppercase,
            lowercase, a number and a special character
            (@ $ ! % * ? &)
        </p>
        <input type="password"
               id="newPassword"
               name="newPassword"
               required
               minlength="8"
               maxlength="64"
               placeholder="Enter new password"/>

        <label for="confirmPassword">
            Confirm Password
        </label>
        <input type="password"
               id="confirmPassword"
               name="confirmPassword"
               required
               placeholder="Re-enter your password"/>

        <div class="actions">
            <button type="submit" class="btn">
                Reset Password
            </button>
        </div>
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