<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Submit Risk</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<c:set var="currentPage" value="submit" scope="request"/>
<%@ include file="/WEB-INF/jsp/navbar.jsp" %>

<div class="guide-bar" id="guide-submit">
    <div class="guide-bar-header"
         onclick="toggleGuide('guide-submit')">
        <div class="guide-bar-header-left">
            <div class="guide-icon">ℹ</div>
            <div>
                <span>How does this page work?</span>
                <p>Click to learn how to submit a risk</p>
            </div>
        </div>
        <span class="guide-chevron"
              id="chevron-guide-submit">▾</span>
    </div>
    <div class="guide-body"
         id="body-guide-submit">
        <div class="guide-card">
            <span class="guide-card-icon">🔍</span>
            <h4>Risk Identification</h4>
            <p>Risk identification is the first step in the
                ISO 31000 process. Recognising potential threats
                early allows your organisation to prepare an
                appropriate response.</p>
        </div>
        <div class="guide-card">
            <span class="guide-card-icon">📏</span>
            <h4>Likelihood vs Impact</h4>
            <p>Likelihood is how probable the risk is to occur.
                Impact is how severe the consequences would be.
                Together they calculate your severity score.</p>
        </div>
        <div class="guide-card">
            <span class="guide-card-icon">🗂️</span>
            <h4>Choosing a Category</h4>
            <p>Categorising your risk helps identify patterns
                across your organisation. Select the category
                that best reflects the nature of the risk.</p>
        </div>
        <div class="guide-card">
            <span class="guide-card-icon">🤖</span>
            <h4>AI Guidance</h4>
            <p>After submitting, ChainGuard's AI will generate
                a personalised educational report explaining
                your risk, its severity and what good
                mitigation looks like.</p>
        </div>
    </div>
</div>

<div class="auth-container">

    <div class="page-header">
        <div>
            <h2>Submit a Risk</h2>
            <p class="subtext">
                Complete the form below to record a new risk.
                The severity score will be calculated automatically
                and displayed on the dashboard.
            </p>
        </div>

        <a class="btn secondary"
           href="${pageContext.request.contextPath}/risks">
            Cancel
        </a>
    </div>

    <c:if test="${not empty error}">
        <div class="message-error">${error}</div>
    </c:if>

    <form method="post"
          action="${pageContext.request.contextPath}/risks/create">

        <input type="hidden"
               name="${_csrf.parameterName}"
               value="${_csrf.token}"/>

        <label for="riskTitle">Risk Title</label>
        <input id="riskTitle" type="text" name="riskTitle"
               required minlength="5" maxlength="100"
               placeholder="e.g. Supplier delivery failure"
               value="${param.riskTitle}"/>

        <label for="description">Description</label>
        <textarea id="description" name="description"
                  rows="4" required minlength="20"
                  placeholder="Describe the risk in detail — include what could go wrong, who is affected and why">${param.description}</textarea>

        <label for="categoryId">Risk Category</label>
        <select id="categoryId" name="categoryId" required
                onchange="showCategoryDescription()">
            <option value="">Select a category</option>
            <c:forEach var="category" items="${categories}">
                <option value="${category.categoryId}"
                        data-description="${category.description}">
                        ${category.name}
                </option>
            </c:forEach>
        </select>

        <div id="categoryDescription"
             style="display:none; margin-top:8px;
                    padding:10px 14px;
                    background:#f0f4ff;
                    border-left:3px solid #1a2e4a;
                    border-radius:4px; font-size:13px;
                    color:#444; line-height:1.6;">
        </div>

        <hr/>

        <h3>Risk Level Calculator</h3>
        <p class="subtext">
            Rate the likelihood and impact of this risk on a
            scale of 1 to 5.
        </p>

        <label for="likelihood">Likelihood (1-5)</label>
        <select id="likelihood" name="likelihood">
            <option value="1">1 — Rare</option>
            <option value="2">2 — Unlikely</option>
            <option value="3">3 — Possible</option>
            <option value="4">4 — Likely</option>
            <option value="5">5 — Almost Certain</option>
        </select>

        <label for="impact">Impact (1-5)</label>
        <select id="impact" name="impact">
            <option value="1">1 — Insignificant</option>
            <option value="2">2 — Minor</option>
            <option value="3">3 — Moderate</option>
            <option value="4">4 — Major</option>
            <option value="5">5 — Severe</option>
        </select>

        <label for="riskProximity">Risk Proximity</label>
        <select id="riskProximity" name="riskProximity">
            <option value="1">This week</option>
            <option value="2">This month</option>
            <option value="3">This quarter</option>
            <option value="3">This year</option>
            <option value="4">1+ years</option>
        </select>

        <div class="form-group">
            <label>
                <input type="checkbox" name="showEducation" value="true" checked />
                Show me AI educational guidance after submission
            </label>
        </div>

        <div class="actions">
            <button type="submit" class="btn">
                Submit Risk
            </button>
        </div>

    </form>
</div>

<script>
    function showCategoryDescription() {
        var select = document.getElementById('categoryId');
        var selected = select.options[select.selectedIndex];
        var description = selected.getAttribute(
            'data-description');
        var box = document.getElementById(
            'categoryDescription');

        if (description && description.trim() !== '') {
            box.textContent = description;
            box.style.display = 'block';
        } else {
            box.style.display = 'none';
        }
    }

        function toggleGuide(id) {
        var body = document.getElementById('body-' + id);
        var chevron = document.getElementById('chevron-' + id);
        body.classList.toggle('open');
        chevron.classList.toggle('open');
    }
</script>

</body>
</html>