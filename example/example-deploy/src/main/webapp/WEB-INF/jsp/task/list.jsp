<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<html>
<head>
    <script language="javascript" src="${ctx}/static/javascripts/pageutil.js"></script>
    <title>任务管理</title>
</head>
<body>
<div id="msg"></div>
<table id="contentTable" class="table table-striped table-bordered table-condensed">
    <thead>
    <tr>
        <th>任务</th>
        <th>管理</th>
    </tr>
    </thead>
    <tbody id="tasks"></tbody>
</table>

<div id="pageing"></div>

<div><a class="btn btn-primary" href="${ctx}/web/task/save">创建任务</a></div>
<script lang="javascript">
    var page = 1;
    var pagesize = 10;
    var tasksDom = $("#tasks");
    function createTaskDom(id, title) {
        var new_task = $("<tr id='task_" + id + "'>");
        var td1 = $("<td>");
        td1.appendTo(new_task);
        $("<a href=\"${ctx}/web/task/save?id=" + id + "\">" + title + "</a>").appendTo(td1);
        var td2 = $("<td>");
        td2.appendTo(new_task);
        $("<a onclick=\"del_task(" + id + ")\">删除</a>").appendTo(td2);
        return new_task;
    }

    pageclick(1);

    function pageclick(to_page) {
        tasksDom.html(null);
        $.ajax({
            url: '/task/list',
            type: 'GET',
            data: {
                page: to_page,
                page_size: pagesize
            },
            success: function (json) {
                page = to_page;
                var result = json.result;
                for (var i = 0; i < result.content.length; i++) {
                    var task = result.content[i];
                    createTaskDom(task.id, task.title).appendTo(tasksDom);
                }
                var pageDom = $("#pageing");
                if (pageDom.length > 0) {
                    pageDom.html(createPagination(result.totalElements, to_page, 5, pagesize, "pageclick"));
                }
            },
            error: function () {
                $("#msg").attr("class", "alert alert-success").html('<button data-dismiss="alert" class="close">×</button>服务器错误')
            }
        });
    }

    function del_task(id) {
        $.ajax({
            url: '/task/delete',
            type: 'POST',
            data: {id: id, _method: "DELETE"},
            success: function (json) {
                if (json.result) {
                    $("#task_" + id).remove();
                    $("#msg").attr("class", "alert alert-success").html('<button data-dismiss="alert" class="close">×</button>删除成功')
                }
            },
            error: function () {
                $("#msg").attr("class", "alert alert-success").html('<button data-dismiss="alert" class="close">×</button>服务器错误')
            }
        });
    }
</script>
</body>
</html>
