<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Register</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="auth-container">

    <h2>Create Account</h2>

    <%
        String error = (String) request.getAttribute("error");
        String emailValue = (String) request.getAttribute("emailValue");
        String fullNameValue = (String) request.getAttribute("fullNameValue");
    %>

    <% if (error != null) { %>
    <div class="message-error"><%= error %></div>
    <% } %>

    <form method="post"
          action="${pageContext.request.contextPath}/auth/register"
          autocomplete="off">

        <input type="hidden"
               name="${_csrf.parameterName}"
               value="${_csrf.token}"/>

        <label for="fullName">Full name</label>
        <input name="fullName" id="fullName" type="text"
               placeholder="Full name"
               required minlength="2" maxlength="50"
               value="<%= (fullNameValue != null ? fullNameValue : "") %>"/>

        <label for="email">Email</label>
        <input name="email" id="email" type="email"
               placeholder="Email"
               required autocomplete="email"
               value="<%= (emailValue != null ? emailValue : "") %>"/>

        <label for="password">Password</label>
        <input name="password" id="password" type="password"
               placeholder="Password"
               required minlength="8" maxlength="64"
               autocomplete="new-password"/>
        <span class="field-hint">
            Must be at least 8 characters and include an uppercase
            letter, a lowercase letter, a number, and a special
            character e.g. @ $ ! % * ? &
        </span>

        <label for="confirmPassword">Confirm password</label>
        <input name="confirmPassword" id="confirmPassword"
               type="password"
               placeholder="Confirm password"
               required minlength="8" maxlength="64"
               autocomplete="new-password"/>

        <div class="actions">
            <button type="submit">Register</button>
            <a class="btn secondary"
               href="${pageContext.request.contextPath}/auth/login">
                Back to login
            </a>
        </div>
    </form>
</div>

</body>
</html>