/**
 * User: sofn
 * Date: 13-9-13 下午5:26
 * Description: 分页工具
 */
/**
 * 创建分页导航栏  注意要引入bootstrap.css 自己编写pageclick方法
 * @param counts 总条数
 * @param current 当前页数
 * @param itemnum 分页栏显示的数目
 * @param pagesize 每页的条数
 * @param pageclick 相应的方法
 * @returns {string}
 */
function createPagination(counts, current, itemnum, pagesize, pageclick) {
    current = parseInt(current);
    var total = (counts % pagesize) == 0 ? (counts / pagesize) : (parseInt(counts / pagesize) + 1);
    var begin = Math.max(1, current - parseInt(itemnum / 2));
    var end = Math.min(begin + (itemnum - 1), total);
    begin = Math.min(begin, end - itemnum + 1);
    begin = begin < 1 ? 1 : begin;
    total = total < 1 ? 1 : total;
    end = end < begin ? begin : end;
    var content = '<ul class="pagination">';
    var arr = ["首页", "上一页", "下一页", "尾页"];
    if (current != 1) {
        content += '<li><a onclick="' + pageclick + '(1);">' + arr[0] + '</a></li>' +
            '<li><a onclick="' + pageclick + '(' + (parseInt(current) - 1) + ');">' + arr[1] + '</a></li>';
    } else {
        content += '<li class="disabled"><a href="#">' + arr[0] + '</a></li>' +
            '<li class="disabled"><a href="#">' + arr[1] + '</a></li>';
    }
    for (var item = begin; item <= end; item++) {
        if (item == current) {
            content += '<li class="active"><a href="#">' + item + '</a></li>';
        } else {
            content += '<li><a onclick="' + pageclick + '(' + item + ');">' + item + '</a></li>';
        }
    }
    if (current != total) {
        content += '<li><a onclick="' + pageclick + '(' + (current + 1) + ');">' + arr[2] + '</a></li>' +
            '<li><a onclick="' + pageclick + '(' + total + ');">' + arr[3] + '</a></li>';
    } else {
        content += '<li class="disabled"><a href="#">' + arr[2] + '</a></li>' +
            '<li class="disabled"><a href="#">' + arr[3] + '</a></li>';
    }
    content += '<li class="disabled"><a href="#">共' + total + '页</a></li></ul>';

    return content;
}
