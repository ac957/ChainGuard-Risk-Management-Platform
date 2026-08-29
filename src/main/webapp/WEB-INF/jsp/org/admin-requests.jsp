<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.example.riskmanagementsystem.model.OrganizationMember" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Admin Approvals</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<c:set var="currentPage" value="approvals" scope="request"/>
<%@ include file="/WEB-INF/jsp/navbar.jsp" %>

<div class="container">

    <div class="page-header">
        <h2>Pending Join Requests</h2>
    </div>

    <%
        String error = (String) request.getAttribute("error");
        List<OrganizationMember> pending =
                (List<OrganizationMember>) request.getAttribute("pending");
    %>

    <% if (request.getParameter("approved") != null) { %>
    <div class="message-success">
        Member approved successfully.
    </div>
    <% } %>

    <% if (request.getParameter("memberRejected") != null) { %>
    <div class="message-success">
        Member request rejected.
    </div>
    <% } %>

    <% if (error != null) { %>
    <div class="message-error"><%= error %></div>
    <% } %>

    <% if (pending == null || pending.isEmpty()) { %>
    <p class="empty-state">No pending requests right now.</p>
    <% } else { %>

    <div class="table-wrapper">
        <table class="risk-table">
            <thead>
            <tr>
                <th>Organisation</th>
                <th>User</th>
                <th>Requested</th>
                <th>Assign Role</th>
                <th>Actions</th>
            </tr>
            </thead>
            <tbody>
            <% for (OrganizationMember m : pending) { %>
            <tr>
                <td><%= m.getOrganization().getOrgName() %></td>
                <td><%= m.getUser().getEmail() %></td>
                <td><%= m.getJoinedAt() %></td>

                <td>
                    <form method="post"
                          action="${pageContext.request.contextPath}
                                  /org/admin/approve">

                        <input type="hidden"
                               name="${_csrf.parameterName}"
                               value="${_csrf.token}"/>

                        <input type="hidden" name="membershipId"
                               value="<%= m.getMembershipId() %>"/>
                        <div style="display:flex; gap:8px;
                                    align-items:center;">
                            <select name="roleName" required>
                                <option value="">Select role</option>
                                <option value="USER">User</option>
                                <option value="MANAGER">Manager</option>
                            </select>
                            <button type="submit" class="btn btn-small">
                                Approve
                            </button>
                        </div>
                    </form>
                </td>

                <td>
                    <form action="${pageContext.request.contextPath}/org/admin/reject"
                          method="post"
                          onsubmit="return confirm(
                                  'Are you sure you want to reject this request from '
                                  + '<%= m.getUser().getEmail() %>? '
                                  + 'This cannot be undone and they will not be '
                                  + 'able to rejoin this organisation.');">
                        <input type="hidden"
                               name="${_csrf.parameterName}"
                               value="${_csrf.token}"/>
                        <input type="hidden"
                               name="membershipId"
                               value="<%= m.getMembershipId() %>"/>
                        <button type="submit"
                                class="btn btn-small secondary">
                            Reject
                        </button>
                    </form>
                </td>
            </tr>
            <% } %>
            </tbody>
        </table>
    </div>

    <% } %>

</div>
</body>
</html>
