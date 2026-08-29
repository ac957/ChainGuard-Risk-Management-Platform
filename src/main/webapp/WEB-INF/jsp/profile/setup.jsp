<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.example.riskmanagementsystem.model.Organization" %>
<!DOCTYPE html>
<html>
<head>
    <title>Complete Your Profile</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="auth-container">

    <div class="page-header">
        <div>
            <h2>Complete Your Profile</h2>
            <p class="subtext">
                Fill in your details and set up your organisation
                before continuing.
            </p>
        </div>
    </div>

    <%
        String error = (String) request.getAttribute("error");
        String fullName = (String) request.getAttribute("fullName");
        String jobRole = (String) request.getAttribute("jobRole");
        String department = (String) request.getAttribute("department");
        String phoneNumber = (String) request.getAttribute("phoneNumber");
        List<Organization> orgs =
                (List<Organization>) request.getAttribute("orgs");
    %>

    <% if (error != null) { %>
    <div class="message-error"><%= error %></div>
    <% } %>

    <form method="post"
          action="${pageContext.request.contextPath}/profile/setup"
          autocomplete="off">

        <input type="hidden"
               name="${_csrf.parameterName}"
               value="${_csrf.token}"/>

        <h3>Personal Details</h3>

        <label for="fullName">Full Name</label>
        <input id="fullName" name="fullName" type="text"
               required minlength="2" maxlength="50"
               value="<%= fullName != null ? fullName : "" %>"/>

        <label for="jobRole">Job Role</label>
        <input id="jobRole" name="jobRole" type="text"
               required minlength="2" maxlength="50"
               placeholder="e.g. Risk Analyst, Manager"
               value="<%= jobRole != null ? jobRole : "" %>"/>

        <label for="department">Department</label>
        <input id="department" name="department" type="text"
               required minlength="2" maxlength="50"
               placeholder="e.g. Operations, Logistics"
               value="<%= department != null ? department : "" %>"/>

        <label for="phoneNumber">Phone Number (optional)</label>
        <input id="phoneNumber" name="phoneNumber" type="text"
               maxlength="20"
               placeholder="e.g. +44 7911 123456"
               value="<%= phoneNumber != null ? phoneNumber : "" %>"/>

        <hr/>

        <h3>Organisation</h3>
        <p class="subtext">
            Create a new organisation or join an existing one.
        </p>

        <div class="org-toggle">
            <label>
                <input type="radio" name="orgAction"
                       value="join" checked
                       onclick="showJoin()"/>
                Join an existing organisation
            </label>
            <label style="margin-left: 16px;">
                <input type="radio" name="orgAction"
                       value="create"
                       onclick="showCreate()"/>
                Create a new organisation
            </label>
        </div>

        <div id="joinSection" style="margin-top: 16px;">
            <label for="orgId">Select organisation</label>
            <select id="orgId" name="orgId">
                <option value="">Choose...</option>
                <%
                    if (orgs != null) {
                        for (Organization o : orgs) {
                %>
                <option value="<%= o.getOrgId() %>">
                    <%= o.getOrgName() %>
                </option>
                <%
                        }
                    }
                %>
            </select>
        </div>

        <div id="createSection"
             style="display:none; margin-top: 16px;">
            <label for="orgName">Organisation Name</label>
            <input id="orgName" name="orgName" type="text"
                   minlength="2" maxlength="100"
                   placeholder="e.g. ABC Logistics"/>

            <label for="specialisation">
                Specialisation (optional)
            </label>
            <input id="specialisation" name="industry"
                   type="text" maxlength="100"
                   placeholder="e.g. Cold chain food,
                                 Cotton supply,
                                 Pharmaceutical freight"/>
        </div>

        <div class="actions">
            <button type="submit" class="btn">
                Complete Profile
            </button>
        </div>

    </form>
</div>

<script>
    function showJoin() {
        document.getElementById('joinSection').style.display = 'block';
        document.getElementById('createSection').style.display = 'none';
    }
    function showCreate() {
        document.getElementById('joinSection').style.display = 'none';
        document.getElementById('createSection').style.display = 'block';
    }
</script>

</body>
</html>