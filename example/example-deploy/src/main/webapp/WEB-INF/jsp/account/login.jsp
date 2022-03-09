<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<html>
<head>
    <link href="${ctx}/static/jquery-validation/1.14.0/validate.css" type="text/css" rel="stylesheet"/>
    <script src="${ctx}/static/jquery-validation/1.14.0/jquery.validate.min.js" type="text/javascript"></script>
    <script src="${ctx}/static/jquery-validation/1.14.0/messages_bs_zh.js" type="text/javascript"></script>
    <script src="${ctx}/static/jquery-cookie/1.4.1/jquery.cookie.min.js" type="text/javascript"></script>
    <title>登录页</title>
</head>

<body>
<form id="loginForm" class="form-horizontal">
    <div id="msg"></div>
    <div class="control-group">
        <label for="username" class="control-label">名称:</label>

        <div class="controls">
            <input type="text" id="username" name="username" class="input-medium required"/>
        </div>
    </div>
    <div class="control-group">
        <label for="password" class="control-label">密码:</label>

        <div class="controls">
            <input type="password" id="password" name="password" class="input-medium required"/>
        </div>
    </div>

    <div class="control-group">
        <div class="controls">
            <br/>
            <input id="submit_btn" class="btn btn-primary" type="submit" value="登录"/>
            <a class="btn" href="${ctx}/web/register">注册</a>
        </div>
    </div>
</form>

<script>
    $(document).ready(function () {
        $.cookie('AUTH_COOKIE', null, {expires: -1, path: '/'});
        //聚焦第一个输入框
        $("#username").focus();
        //为inputForm注册validate函数
        $("#loginForm").validate({
            submitHandler: function () {
                var username = $("#username").val();
                var password = $("#password").val();
                var rememberMe = $("#rememberMe").val() == "on" ? "true" : "false";
                $.ajax({
                    //提交数据的类型 POST GET
                    type: "POST",
                    //提交的网址
                    url: "${ctx}/users/login",
                    //提交的数据
                    data: {"username": username, "password": password, "cookie": "true"},
                    //返回数据的格式
                    datatype: "json",
                    //成功返回之后调用的函数
                    success: function (data) {
                        if (data && data.result) {
                            $.cookie('AUTH_COOKIE', data.result.cookie, {expires: 1, path: '/'});
                        }
                        location.href = "${ctx}/web/task";
                    },
                    //调用出错执行的函数
                    error: function (data) {
                        var errorMsg = "网络错误请重试";
                        var json = data.responseJSON;
                        if (json && json.result) {
                            errorMsg = json.result.error_zh_CN;
                        }
                        $("#msg").attr("class", "text-warning alert alert-error").html(errorMsg);
                    }
                });
            }
        });
    });
</script>
</body>
</html>
