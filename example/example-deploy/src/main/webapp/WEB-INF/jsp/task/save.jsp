<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<html>
<head>
    <link href="${ctx}/static/jquery-validation/1.14.0/validate.css" type="text/css" rel="stylesheet"/>
    <script src="${ctx}/static/jquery-validation/1.14.0/jquery.validate.min.js" type="text/javascript"></script>
    <script src="${ctx}/static/jquery-validation/1.14.0/messages_bs_zh.js" type="text/javascript"></script>
    <title>任务管理</title>
</head>

<body>
<form id="inputForm" class="form-horizontal">
    <input type="hidden" id="task_id" name="title" value="${task.id}"/>

    <div id="msg"></div>
    <fieldset>
        <legend>
            <small>管理任务</small>
        </legend>
        <div class="control-group">
            <label for="task_title" class="control-label">任务名称:</label>

            <div class="controls">
                <input type="text" id="task_title" name="title" value="${task.title}" class="input-large required"
                       minlength="3"/>
            </div>
        </div>
        <div class="control-group">
            <label for="description" class="control-label">任务描述:</label>

            <div class="controls">
                <textarea id="description" name="description"
                          class="input-large required">${task.description}</textarea>
            </div>
        </div>
        <div class="form-actions">
            <input id="submit_btn" class="btn btn-primary" type="submit" value="提交"/>&nbsp;
            <input id="cancel_btn" class="btn" type="button" value="返回" onclick="history.back()"/>
        </div>
    </fieldset>
</form>
<script>
    $(document).ready(function () {
        //聚焦第一个输入框
        $("#task_title").focus();
        //为inputForm注册validate函数
        $("#inputForm").validate({
            submitHandler: function () {
                var id = $("#task_id").val() || 0;
                var title = $("#task_title").val();
                var desc = $("#description").val();
                $.ajax({
                    //提交数据的类型 POST GET
                    type: "POST",
                    //提交的网址
                    url: "${ctx}/task/save",
                    //提交的数据
                    data: {id: id, title: title, desc: desc},
                    cache: false,
                    crossDomain: true,
                    dataType: 'json',
                    xhrFields: {
                        withCredentials: true
                    },
                    success: function () {
                        $("#msg").attr("class", "text-warning alert alert-error").html("创建成功");
                        setInterval(function () {
                            location.href = "${ctx}/web/task";
                        }, 1000);
                    },
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
