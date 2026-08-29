<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Edit Risk</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<jsp:include page="/WEB-INF/jsp/navbar.jsp"/>

<div class="container">
    <div class="page-header">
        <div>
            <h2>Edit Risk</h2>
            <p class="subtext">You can only edit this risk
                while it is in New status.</p>
        </div>
    </div>

    <c:if test="${not empty error}">
        <div class="message-error">${error}</div>
    </c:if>

    <c:if test="${not empty success}">
        <div class="message-success">${success}</div>
    </c:if>

    <div class="card">
        <form method="post"
              action="${pageContext.request.contextPath}
                      /risks/${risk.riskId}/edit">

            <input type="hidden"
                   name="${_csrf.parameterName}"
                   value="${_csrf.token}"/>

            <label for="riskTitle">Risk Title</label>
            <input type="text" id="riskTitle"
                   name="riskTitle"
                   value="${risk.riskTitle}"
                   required minlength="5" maxlength="100"/>

            <label for="categoryId">Category</label>
            <select id="categoryId" name="categoryId" required>
                <c:forEach var="cat" items="${categories}">
                    <option value="${cat.categoryId}"
                            <c:if test="${cat.categoryId ==
                                risk.category.categoryId}">
                                selected
                            </c:if>>
                            ${cat.name}
                    </option>
                </c:forEach>
            </select>

            <label for="description">Description</label>
            <textarea id="description" name="description"
                      rows="4" required
                      minlength="20">${risk.description}</textarea>

            <label for="likelihood">Likelihood (1-5)</label>
            <select id="likelihood" name="likelihood" required>
                <c:forEach begin="1" end="5" var="i">
                    <option value="${i}"
                            <c:if test="${risk.likelihood == i}">
                                selected
                            </c:if>>${i}</option>
                </c:forEach>
            </select>

            <label for="impact">Impact (1-5)</label>
            <select id="impact" name="impact" required>
                <c:forEach begin="1" end="5" var="i">
                    <option value="${i}"
                            <c:if test="${risk.impact == i}">
                                selected
                            </c:if>>${i}</option>
                </c:forEach>
            </select>

            <div class="actions">
                <button type="submit" class="btn">
                    Save Changes
                </button>
                <a href="${pageContext.request.contextPath}/risks"
                   class="btn secondary">
                    Cancel
                </a>
            </div>
        </form>
    </div>
</div>

<script>
    function toggleGuide(id) {
        var body = document.getElementById('body-' + id);
        var chevron = document.getElementById('chevron-' + id);
        body.classList.toggle('open');
        chevron.classList.toggle('open');
    }
</script>

</body>
</html>
