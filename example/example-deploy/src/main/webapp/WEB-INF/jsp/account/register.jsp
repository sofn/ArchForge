<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<html>
<head>
    <link href="${ctx}/static/jquery-validation/1.14.0/validate.css" type="text/css" rel="stylesheet"/>
    <script src="${ctx}/static/jquery-validation/1.14.0/jquery.validate.min.js" type="text/javascript"></script>
    <script src="${ctx}/static/jquery-validation/1.14.0/messages_bs_zh.js" type="text/javascript"></script>
    <script src="${ctx}/static/jquery-cookie/1.4.1/jquery.cookie.min.js" type="text/javascript"></script>
    <title>用户注册</title>
</head>
<body>
<form id="inputForm" class="form-horizontal">
    <div id="msg"></div>
    <fieldset>
        <legend>
            <small>用户注册</small>
        </legend>
        <div class="control-group">
            <label for="username" class="control-label">用户名:</label>

            <div class="controls">
                <input type="text" id="username" name="username" class="input-large required"/>
            </div>
        </div>
        <div class="control-group">
            <label for="password" class="control-label">密码:</label>

            <div class="controls">
                <input type="password" id="password" name="password" class="input-large required"/>
            </div>
        </div>
        <div class="control-group">
            <label for="confirmPassword" class="control-label">确认密码:</label>

            <div class="controls">
                <input type="password" id="confirmPassword" name="confirmPassword" class="input-large required"
                       equalTo="#password"/>
            </div>
        </div>
        <br/>

        <div class="form-actions">
            <input id="submit_btn" class="btn btn-primary" type="submit" value="提交"/>&nbsp;
            <input id="cancel_btn" class="btn" type="button" value="返回" onclick="history.back()"/>
        </div>
    </fieldset>
</form>
<script>
    $(document).ready(function () {
        $.cookie('AUTH_COOKIE', null, {expires: -1, path: '/'});
        //聚焦第一个输入框
        $("#username").focus();
        //为inputForm注册validate函数
        $("#inputForm").validate({
            submitHandler: function () {
                var username = $("#username").val();
                var password = $("#password").val();
                $.ajax({
                    //提交数据的类型 POST GET
                    type: "POST",
                    //提交的网址
                    url: "${ctx}/users/register",
                    //提交的数据
                    data: {"username": username, "password": password},
                    //返回数据的格式
                    datatype: "json",
                    //成功返回之后调用的函数
                    success: function () {
                        $("#msg").attr("class", "text-warning").html("注册成功,即将跳转");
                        setInterval(function () {
                            location.href = "${ctx}/web/login";
                        }, 3000);
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
