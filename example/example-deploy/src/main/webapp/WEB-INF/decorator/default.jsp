<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<!DOCTYPE html>
<html>
<head>
    <title>QuickStart示例:<sitemesh:write property='title'/></title>
    <meta charset="UTF-8">
    <meta http-equiv="Content-Type" content="text/html;charset=utf-8"/>
    <meta http-equiv="Cache-Control" content="no-store"/>
    <meta http-equiv="Pragma" content="no-cache"/>
    <meta http-equiv="Expires" content="0"/>

    <link type="image/x-icon" href="${ctx}/static/images/favicon.ico" rel="shortcut icon">
    <link href="${ctx}/webjars/bootstrap/3.3.5/css/bootstrap.min.css" type="text/css" rel="stylesheet"/>
    <link href="${ctx}/static/styles/default.css" type="text/css" rel="stylesheet"/>
    <script src="${ctx}/webjars/jquery/2.1.4/jquery.min.js" type="text/javascript"></script>
    <sitemesh:write property='head'/>
</head>

<body>
<div class="container">
    <%@ include file="/WEB-INF/decorator/header.jsp" %>
    <div id="content">
        <sitemesh:write property='body'/>
    </div>
    <%@ include file="/WEB-INF/decorator/footer.jsp" %>
</div>
<script src="${ctx}/webjars/bootstrap/3.3.5/js/bootstrap.min.js" type="text/javascript"></script>
</body>
</html>