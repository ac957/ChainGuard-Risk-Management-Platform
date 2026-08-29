<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Membership Declined</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="container">

    <div class="page-header">
        <h2>Membership Request Declined</h2>
    </div>

    <div class="message-error">
        Your request to join this organisation has not been
        approved by the administrator.
    </div>

    <p>A notification email has been sent to your registered
        email address with further details.</p>

    <p style="margin-top: 12px;">If you believe this was a
        mistake please contact your organisation administrator
        directly for further assistance.</p>

    <p style="margin-top: 12px;">Alternatively you may request
        to join a different organisation below.</p>

    <div class="actions">
        <a class="btn"
           href="${pageContext.request.contextPath}
                 /profile/setup?rejected=true">
            Request to Join Another Organisation
        </a>
        <a class="btn secondary"
           href="${pageContext.request.contextPath}/auth/login">
            Back to Login
        </a>
    </div>

</div>
</body>
</html>