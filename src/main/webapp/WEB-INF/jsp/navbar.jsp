<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<nav class="navbar">
    <div class="nav-brand">
        <a href="${pageContext.request.contextPath}/dashboard">
            ChainGuard
        </a>
    </div>
    <div class="nav-links">

        <a href="${pageContext.request.contextPath}/dashboard"
           class="${currentPage == 'dashboard' ? 'active' : ''}">
            Dashboard
        </a>

        <a href="${pageContext.request.contextPath}/risks"
           class="${currentPage == 'risks' ? 'active' : ''}">
            Risks
        </a>

        <a href="${pageContext.request.contextPath}/analytics"
           class="${currentPage == 'analytics' ? 'active' : ''}">
            Analytics
        </a>

        <a href="${pageContext.request.contextPath}/notifications"
           class="${currentPage == 'notifications' ? 'active' : ''}">
            Notifications
            <c:if test="${unreadCount > 0}">
                <span class="nav-badge">${unreadCount}</span>
            </c:if>
        </a>

        <%-- admin dropdown --%>
        <c:if test="${isAdmin}">
            <div class="nav-dropdown" id="adminDropdown">
                <button class="nav-dropdown-trigger
                    ${currentPage == 'approvals'
                    || currentPage == 'members'
                    ? 'active' : ''}"
                        onclick="toggleAdminDropdown(event)"
                        type="button">
                    Admin
                    <c:if test="${pendingCount > 0}">
                        <span class="nav-badge">
                                ${pendingCount}
                        </span>
                    </c:if>
                    <span class="chevron" id="adminChevron">
                        ▾
                    </span>
                </button>
                <div class="nav-dropdown-menu"
                     id="adminDropdownMenu">
                    <a href="${pageContext.request.contextPath}
                              /org/admin/requests"
                       class="${currentPage == 'approvals'
                           ? 'active' : ''}">
                        Approvals
                        <c:if test="${pendingCount > 0}">
                            <span class="nav-dropdown-badge">
                                    ${pendingCount}
                            </span>
                        </c:if>
                    </a>
                    <a href="${pageContext.request.contextPath}
                              /org/members"
                       class="${currentPage == 'members'
                           ? 'active' : ''}">
                        Members
                    </a>
                </div>
            </div>
        </c:if>

        <a href="${pageContext.request.contextPath}/profile/edit"
           class="${currentPage == 'profile' ? 'active' : ''}">
            My Profile
        </a>

        <form action="${pageContext.request.contextPath}
                      /auth/logout"
              method="post"
              style="margin:0; padding:0;">
            <input type="hidden"
                   name="${_csrf.parameterName}"
                   value="${_csrf.token}"/>
            <button type="submit" class="btn-nav-logout">
                Logout
            </button>
        </form>

    </div>
</nav>

<script>
    function toggleAdminDropdown(event) {
        event.stopPropagation();
        var menu = document.getElementById('adminDropdownMenu');
        var chevron = document.getElementById('adminChevron');
        var isOpen = menu.classList.contains('open');

        if (isOpen) {
            menu.classList.remove('open');
            chevron.style.transform = 'rotate(0deg)';
        } else {
            menu.classList.add('open');
            chevron.style.transform = 'rotate(180deg)';
        }
    }

    // close dropdown when clicking anywhere else on the page
    document.addEventListener('click', function () {
        var menu = document.getElementById('adminDropdownMenu');
        var chevron = document.getElementById('adminChevron');
        if (menu && menu.classList.contains('open')) {
            menu.classList.remove('open');
            if (chevron) chevron.style.transform = 'rotate(0deg)';
        }
    });
</script>