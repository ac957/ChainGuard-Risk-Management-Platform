<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Risk Analytics</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">
    <c:if test="${!noData}">
        <script type="text/javascript"
                src="https://www.gstatic.com/charts/loader.js">
        </script>
    </c:if>
    <style>
        .charts-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 24px;
            margin-top: 24px;
        }
        .chart-card {
            background: #fff;
            border: 1px solid #e0e0e0;
            border-radius: 8px;
            padding: 20px;
        }
        .chart-card h3 {
            margin: 0 0 4px 0;
            font-size: 15px;
            color: #333;
        }
        .chart-card p {
            font-size: 12px;
            color: #888;
            margin: 0 0 12px 0;
        }
        .chart-card.full-width {
            grid-column: span 2;
        }
        .ai-panel {
            margin-top: 24px;
            background: #f8f9ff;
            border: 1px solid #d0d7ff;
            border-radius: 8px;
            padding: 24px;
        }
        .ai-panel h3 {
            margin: 0 0 8px 0;
            color: #3a3a8c;
        }
        .ai-output {
            white-space: pre-wrap;
            line-height: 1.8;
            color: #333;
            font-size: 14px;
            margin-top: 16px;
        }
        .ai-loading {
            color: #888;
            font-style: italic;
            margin-top: 12px;
        }
    </style>
</head>
<body>

<c:set var="currentPage" value="analytics" scope="request"/>
<%@ include file="/WEB-INF/jsp/navbar.jsp" %>

<div class="container">

    <div class="page-header">
        <div>
            <h2>Risk Analytics</h2>
            <p class="subtext">
                <strong>${organizationName}</strong>
                &nbsp;|&nbsp;
                Visual overview of your organisation's risk landscape
            </p>
        </div>
    </div>

    <div class="guide-bar" id="guide-analytics">
        <div class="guide-bar-header"
             onclick="toggleGuide('guide-analytics')">
            <div class="guide-bar-header-left">
                <div class="guide-icon">ℹ</div>
                <div>
                    <span>How does this page work?</span>
                    <p>Click to learn about the analytics dashboard</p>
                </div>
            </div>
            <span class="guide-chevron"
                  id="chevron-guide-analytics">▾</span>
        </div>
        <div class="guide-body"
             id="body-guide-analytics">
            <div class="guide-card">
                <span class="guide-card-icon">🗺️</span>
                <h4>Risk Heatmap</h4>
                <p>The Risk Impact/Probability Matrix plots each
                    risk by likelihood and impact. Risks in the
                    top right are highest priority — use this to
                    focus mitigation efforts first.</p>
            </div>
            <div class="guide-card">
                <span class="guide-card-icon">📈</span>
                <h4>Trends Over Time</h4>
                <p>The submission timeline shows when risks are
                    being identified. Consistent activity suggests
                    a healthy risk management culture in your
                    organisation.</p>
            </div>
            <div class="guide-card">
                <span class="guide-card-icon">🍩</span>
                <h4>Status Breakdown</h4>
                <p>The status chart shows how risks are distributed
                    across the lifecycle. A large proportion of New
                    risks suggests mitigation planning is needed.</p>
            </div>
            <div class="guide-card">
                <span class="guide-card-icon">🤖</span>
                <h4>AI Analysis</h4>
                <p>Click Analyse with AI to generate an educational
                    analysis of your risk posture aligned with
                    ISO 31000, highlighting patterns and providing
                    strategic recommendations.</p>
            </div>
        </div>
    </div>

    <%-- empty state — no risks submitted yet --%>
    <c:if test="${noData}">
        <div class="card" style="margin-top:24px;
                                  text-align:center;
                                  padding:48px 24px;">
            <p style="font-size:32px; margin-bottom:12px;">
                📊
            </p>
            <h3 style="margin-bottom:8px;">No risk data yet</h3>
            <p class="subtext">
                Analytics will appear here once your organisation
                has submitted risks. Submit your first risk to
                get started.
            </p>
            <a class="btn"
               style="margin-top:16px; display:inline-block;"
               href="${pageContext.request.contextPath}/risks/create">
                Submit a Risk
            </a>
        </div>
    </c:if>

    <%-- charts — only shown when data exists --%>
    <c:if test="${!noData}">

    <div class="charts-grid">

        <%-- heatmap — full width --%>
        <div class="chart-card full-width">
            <h3>Risk Impact / Probability Matrix</h3>
            <p>Each bubble represents a risk plotted by likelihood
                (x-axis) and impact (y-axis).
                Red = high severity, amber = medium, green = low.</p>
            <div id="heatmapChart"
                 style="width:100%; height:400px;"></div>
        </div>

        <%-- severity distribution --%>
        <div class="chart-card">
            <h3>Risk Severity Distribution</h3>
            <p>Number of risks in each severity band.</p>
            <div id="severityChart"
                 style="width:100%; height:300px;"></div>
        </div>

        <%-- status breakdown --%>
        <div class="chart-card">
            <h3>Risk Status Breakdown</h3>
            <p>Current lifecycle stage of all risks.</p>
            <div id="statusChart"
                 style="width:100%; height:300px;"></div>
        </div>

        <%-- risks by category --%>
        <div class="chart-card">
            <h3>Risks by Category</h3>
            <p>Which risk categories appear most frequently.</p>
            <div id="categoryChart"
                 style="width:100%; height:300px;"></div>
        </div>

        <%-- risks over time --%>
        <div class="chart-card">
            <h3>Risks Submitted Over Time</h3>
            <p>Monthly trend of risk submissions.</p>
            <div id="timelineChart"
                 style="width:100%; height:300px;"></div>
        </div>

    </div>

    <div class="ai-panel">
        <h3>AI Risk Analysis</h3>
        <p style="font-size:13px; color:#555;">
            Click the button below to get an AI generated analysis
            of your risk data based on ISO 31000 principles.
            The analysis will identify patterns, highlight concerns
            and provide educational recommendations.
        </p>
        <button class="btn" onclick="generateAnalysis()">
            Analyse with AI
        </button>
        <div id="aiOutput"></div>
    </div>

    </c:if>

    <%-- ai panel disabled state when no data --%>
    <c:if test="${noData}">
        <div class="ai-panel" style="margin-top:24px;">
            <h3>AI Risk Analysis</h3>
            <p style="font-size:13px; color:#555;">
                AI analysis is available once risks have been
                submitted.
            </p>
            <button class="btn secondary" disabled>
                Analyse with AI
            </button>
        </div>
    </c:if>

</div>

<c:if test="${!noData}">
<script type="text/javascript">

    // load Google Charts
    google.charts.load('current', {'packages': ['corechart', 'bar']});
    google.charts.setOnLoadCallback(drawAllCharts);

    function drawAllCharts() {
        drawHeatmap();
        drawSeverity();
        drawStatus();
        drawCategory();
        drawTimeline();
    }

    // --- heatmap (bubble chart) ---
    function drawHeatmap() {
        var rawData = ${heatmapData};

        var dataArray = [['Risk', 'Likelihood', 'Impact',
            'Severity', 'Score']];

        rawData.forEach(function (r) {
            var severity;
            if (r.score >= 15) severity = 'High';
            else if (r.score >= 8) severity = 'Medium';
            else severity = 'Low';

            dataArray.push([
                r.title,
                r.x,
                r.y,
                severity,
                r.score
            ]);
        });

            var data = google.visualization
                .arrayToDataTable(dataArray);
            var options = {
                title: 'Risk Impact / Probability Matrix',
                hAxis: {
                    title: 'Likelihood (1-5)',
                    minValue: 0, maxValue: 6,
                    ticks: [1, 2, 3, 4, 5]
                },
                vAxis: {
                    title: 'Impact (1-5)',
                    minValue: 0, maxValue: 6,
                    ticks: [1, 2, 3, 4, 5]
                },
                bubble: {textStyle: {fontSize: 11}},
                colors: ['#28a745', '#dc3545', '#ffa500'],
                legend: {position: 'right'},
                chartArea: {width: '75%', height: '75%'}
            };

        var chart = new google.visualization.BubbleChart(
            document.getElementById('heatmapChart'));
        chart.draw(data, options);
    }

    // --- severity distribution (column chart) ---
    function drawSeverity() {
        var data = google.visualization.arrayToDataTable([
            ['Severity', 'Number of Risks', {role: 'style'}],
            ['Low', ${lowCount}, '#28a745'],
            ['Medium', ${mediumCount}, '#ffa500'],
            ['High', ${highCount}, '#dc3545']
        ]);

        var options = {
            title: 'Risk Severity Distribution',
            legend: {position: 'none'},
            chartArea: {width: '70%', height: '70%'},
            vAxis: {
                title: 'Number of Risks',
                minValue: 0,
                format: '0'
            },
            hAxis: {title: 'Severity Band'}
        };

        var chart = new google.visualization.ColumnChart(
            document.getElementById('severityChart'));
        chart.draw(data, options);
    }

    // --- status breakdown (donut chart) ---
    function drawStatus() {
        var data = google.visualization.arrayToDataTable([
            ['Status', 'Count'],
            ['New', ${statusNew}],
            ['Under Review', ${statusUnderReview}],
            ['Mitigating', ${statusMitigating}],
            ['Resolved', ${statusResolved}],
            ['Closed', ${statusClosed}]
        ]);

        var options = {
            title: 'Risk Status Breakdown',
            pieHole: 0.4,
            colors: ['#6c757d', '#007bff', '#ffa500',
                '#28a745', '#343a40'],
            legend: {position: 'bottom'},
            chartArea: {width: '80%', height: '75%'}
        };

        var chart = new google.visualization.PieChart(
            document.getElementById('statusChart'));
        chart.draw(data, options);
    }

    // --- risks by category (horizontal bar chart) ---
    function drawCategory() {
        var labels = ${categoryLabels};
        var values = ${categoryValues};

        var dataArray = [['Category', 'Number of Risks',
            {role: 'style'}]];
        var colours = ['#007bff', '#6610f2', '#17a2b8',
            '#fd7e14', '#20c997', '#e83e8c',
            '#6c757d', '#28a745'];

        labels.forEach(function (label, i) {
            dataArray.push([
                label,
                values[i],
                colours[i % colours.length]
            ]);
        });

        var data = google.visualization.arrayToDataTable(dataArray);

        var options = {
            title: 'Risks by Category',
            legend: {position: 'none'},
            chartArea: {width: '60%', height: '80%'},
            hAxis: {
                title: 'Number of Risks',
                minValue: 0,
                format: '0'
            },
            vAxis: {title: 'Category'}
        };

        var chart = new google.visualization.BarChart(
            document.getElementById('categoryChart'));
        chart.draw(data, options);
    }

    // --- risks over time (line chart) ---
    function drawTimeline() {
        var labels = ${monthLabels};
        var values = ${monthValues};

        var dataArray = [['Month', 'Risks Submitted']];
        labels.forEach(function (label, i) {
            dataArray.push([label, values[i]]);
        });

        var data = google.visualization.arrayToDataTable(dataArray);

        var options = {
            title: 'Risks Submitted Over Time',
            legend: {position: 'none'},
            chartArea: {width: '75%', height: '70%'},
            vAxis: {
                title: 'Risks Submitted',
                minValue: 0,
                format: '0'
            },
            hAxis: {title: 'Month'},
            colors: ['#027459'],
            lineWidth: 2,
            pointSize: 5,
            curveType: 'function'
        };

        var chart = new google.visualization.LineChart(
            document.getElementById('timelineChart'));
        chart.draw(data, options);
    }

    // --- AI analysis ---
    function getCsrfToken() {
        var match = document.cookie.match(
            /XSRF-TOKEN=([^;]+)/);
        return match ? decodeURIComponent(match[1]) : '';
    }

    function generateAnalysis() {
        var output = document.getElementById('aiOutput');
        output.innerHTML =
            '<p class="ai-loading">Generating AI analysis... ' +
            'this may take a moment.</p>';

        var summary = '${analysisSummary}';


        fetch('${pageContext.request.contextPath}/analytics/ai-analysis', {
            method: 'POST',
            headers: {
                'Content-Type':
                    'application/x-www-form-urlencoded',
                'X-XSRF-TOKEN': getCsrfToken()
            },
            body: 'summary=' + encodeURIComponent(summary)
            })
                .then(function (res) { return res.text(); })
                .then(function (text) {
                    output.innerHTML =
                        '<div class="ai-output">' + text + '</div>';
                })
                .catch(function () {
                    output.innerHTML =
                    '<p style="color:red;">Failed to generate ' +
                    'analysis. Please try again.</p>';
            });
    }


</script>
</c:if>

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
