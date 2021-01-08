
//-------------------------------页面加载执行--------------------------------------
$(document).ready(function () {
    //查询
    findByLimit(1);
})

//----------------------------------------添加-----------------------------------
//去添加函数,打开添加角色的模态框
function toAdd() {
    //清除输入框内容
    $("#add_areaName").val("");//名称
    $("#add_describes").val("");//描述
    $("#add_areaNumber").val("");//编号
    //打开模态框
    $("#addModal").modal("show");
    //模态框打开事件
    $("#addModal").on("shown.bs.modal", function () {
        $("#add_areaName").focus();
    })
}

function add() {
    //获取参数
    var areaName = $("#add_areaName").val();//角色名称
    var areaNumber = $("#add_areaNumber").val();//编号
    var describes=$("#add_describes").val();//描述
    //判断条件
    if (areaName == "") {
        $("#add_areaName").focus();
    } else if (areaNumber == "") {
        $("#add_areaNumber").focus();
    } else {
        $("#div_loading").css("display","block");
        //提交后台
        $.ajax({
            url: 'areaAdd',
            type: 'post',
            data: {'areaName': areaName, 'areaNumber': areaNumber, 'describes': describes},
            success: function (data) {
                if (data.code==0) {
                    var currentPage=$("#span_currentPage").html();
                    if(!/^[0-9]{1,10}$/.test(currentPage)){
                        currentPage=1;
                    }
                    findByLimit(currentPage);
                    $("#addModal").modal("hide");
                } else {
                    alert(data.msg);
                }
                $("#div_loading").css("display","none");
            },
            error:function(jqXHR, textStatus, errorThrown){
                var status = jqXHR.status;//404,500等
                var text = jqXHR.statusText;//404对应的Not found,500对应的error
                alert("操作失败：" + status + "  " + text);
                $("#div_loading").css("display","none");
            }
        });
    }
}

//----------------------------------------修改角色-----------------------------------
//去修改函数
function toUpdate(a) {
    //获取所在的行所有单元格
    var td = a.parentNode.parentNode.childNodes;
    //获取参数
    var id = td[0].innerHTML;//1.id
    var areaName = td[1].innerHTML;//2.名称
    var areaNumber=td[2].innerHTML;//编号
    var describes = td[3].innerHTML;//3.描述
    //把参数放入输入框
    $("#update_id").val(id);
    $("#update_areaName").val(areaName);
    $("#update_areaNumber").val(areaNumber);
    $("#update_describes").val(describes);
    //打开模态框
    $("#updateModal").modal("show");
    //模态框打开事件
    $("#updateModal").on("shown.bs.modal", function () {
        //角色名称输入框获取焦点
        $("#update_areaName").focus();
    })
}

//修改按钮
function update() {
    //获取参数
    var id = $("#update_id").val();//id
    var areaName = $("#update_areaName").val();//名称
    var areaNumber = $("#update_areaNumber").val();//编号
    var describes =$("#update_describes").val();
    if (areaName == "") {
        $("#update_areaName").focus();
    } else if (areaNumber == "") {
        $("#update_areaNumber").focus();
    } else {
        $("#div_loading").css("display","block");
        //提交后台
        $.ajax({
            url: 'areaUpdate',
            type: 'post',
            data: {'id': id, 'areaName': areaName, 'areaNumber': areaNumber, 'describes': describes},
            success: function (data) {
                if (data.code==0) {
                    var currentPage=$("#span_currentPage").html();
                    findByLimit(currentPage);
                    $("#updateModal").modal("hide");
                } else {
                    alert(data.msg);
                }
                $("#div_loading").css("display","none");
            },
            error:function(jqXHR, textStatus, errorThrown){
                var status = jqXHR.status;//404,500等
                var text = jqXHR.statusText;//404对应的Not found,500对应的error
                alert("修改失败：" + status + "  " + text);
                $("#div_loading").css("display","none");
            }
        });
    }
}

//----------------------------------------删除角色-----------------------------------
function toDelete(a) {
    //获取角色id
    var id = a.parentNode.parentNode.childNodes[0].innerHTML;
    var tips = confirm("确定删除吗？");
    if (tips == true) {
        $("#div_loading").css("display","block");
        //ajax删除
        $.ajax({
            url: 'areaDelete',
            type: 'post',
            data: {'id': id},
            success: function (data) {
                if (data.code==0) {
                    var currentPage=$("#span_currentPage").html();
                    findByLimit(currentPage);
                } else {
                    alert(data.msg);
                }
                $("#div_loading").css("display","none");
            },
            error:function(jqXHR, textStatus, errorThrown){
                var status = jqXHR.status;//404,500等
                var text = jqXHR.statusText;//404对应的Not found,500对应的error
                alert("删除失败：" + status + "  " + text);
                $("#div_loading").css("display","none");
            }
        });
    }
}

//------------------------------------分页查询收货记录-------------------------------
function findByLimit(currentPage) {
    //显示加载提示信息
    $("#div_loading").css("display", "block");
    //获取参数
    var areaName=$("#input_areaName").val();
    var areaNumber=$("#input_areaNumber").val();
    //先清除旧数据
    $("#table_data").html("");
    //后台查询
    $.ajax({
        url: 'area',
        type: 'post',
        data: {
            'areaName':areaName,
            'areaNumber':areaNumber,
            'currentPage': currentPage
        },
        dataType:'json',
        success: function (data) {
            //返回值不为空，有内容
            if (data.data != null) {
                //把页数信息放入页面
                var datas=data.data;
                //获取当前页,并将值设置到div中
                var currentPage = datas.currentPage;
                $("#span_currentPage").html(currentPage);
                //获取总页数,并将值设置到div中
                var totalPage = datas.totalPage;
                $("#span_totalPage").html(totalPage);
                //总数据条数放入div中
                var dataCount = datas.totalCount;
                $("#span_dataCount").html(dataCount);
                //三、把分页按钮修改内容
                pageButtonChange(currentPage, totalPage);
                //四、把分页信息显示
                $("#div_page").css("display", "block");
                //五、把数据填入页面
                var record = datas.list;
                var deleteBtn="";
                if(deleteData){
                    deleteBtn="<button type='button' class='btn btn-danger btn-xs' onclick='toDelete(this)'>删除</button>";
                }
                var updateBtn="";
                if(updateData){
                    updateBtn=" <button type='button' class='btn btn-warning btn-xs' onclick='toUpdate(this)'>修改</button>";
                }
                var td="";
                if(deleteData==false&&updateData==false){
                    td="";
                }else {
                    td="<td>"+updateBtn+deleteBtn+"</td>";
                }
                for (var i = 0; i < record.length; i++) {
                    var str = "<tr><td style='display: none'>" + record[i].id +
                        "</td><td>" + record[i].areaname +
                        "</td><td>" + record[i].areanumber +
                        "</td><td>" + record[i].describes +
                        "</td>" + td+"</tr>";
                    $("#table_data").append(str);
                }
            } else {
                //隐藏分页信息
                $("#div_page").css("display", "none");
            }
            //隐藏加载提示信息
            $("#div_loading").css("display", "none");
        },
        error:function(jqXHR, textStatus, errorThrown){
            var status = jqXHR.status;//404,500等
            var text = jqXHR.statusText;//404对应的Not found,500对应的error
            alert("查询失败：" + status + "  " + text);
            $("#div_loading").css("display","none");
        }
    });
}

//分页按钮内容改变
function pageButtonChange(currentPage, totalPage) {
    //如果总页数大于等于3页
    if (totalPage >= 3) {
        //先设置3个按钮的选中状态为未选中
        document.getElementById("li1").className = "";
        document.getElementById("li2").className = "";
        document.getElementById("li3").className = "";
        //再根据当前页码，设置按钮上显示的值以及按钮的选中状态
        if (currentPage == 1) {//首页
            //设置第一页被选中
            document.getElementById("li1").className = "active";
            //设置按钮的值
            $("#a1").html("1");
            $("#a2").html("2");
            $("#a3").html("3");
        }
        if (currentPage == totalPage) {//尾页
            //设置尾页被选中
            document.getElementById("li3").className = "active";
            //设置按钮的值
            $("#a1").html(totalPage - 2);
            $("#a2").html(totalPage - 1);
            $("#a3").html(totalPage);
        }
        if (1 < currentPage && currentPage < totalPage) {//首页尾页之间
            //设置中间按钮被选中
            document.getElementById("li2").className = "active";
            //设置按钮的值
            $("#a1").html(currentPage - 1);
            $("#a2").html(currentPage);
            $("#a3").html(currentPage + 1);
        }
    } else {
        //设置按钮的值
        $("#a1").html("1");
        $("#a2").html("2");
        $("#a3").html("3");
        //设置多余按钮的隐藏
        var startPage = totalPage + 1;
        for (var i = startPage; i <= 3; i++) {
            var liId = "li" + i;
            document.getElementById(liId).className = "disabled";
        }
        //设置所有按钮的状态都是未被选中,当前页为选中
        for (var g = 1; g <= totalPage; g++) {
            var id = "li" + g;
            if (g == currentPage) {
                //设置被选中的按钮选中状态
                document.getElementById(id).className = "active";
            } else {
                document.getElementById(id).className = "";
            }
        }
    }
}

//分页按钮点击事件
function pageButton(a) {
    //获分页按钮的前页数
    var currentPage = parseInt(a.innerHTML);
    //获取显示的当前页数
    var showCurrentPage = parseInt($("#span_currentPage").html());
    //获取显示的总页数
    var showTotalPage = parseInt($("#span_totalPage").html());
    //如果按钮的当前页和显示的当前页一样，则不做操作
    if (currentPage != showCurrentPage) {
        //此时判断，选择的页数是否大于总页数,不做操作
        if (currentPage <= showTotalPage) {
            //调用分页条件查询函数
            findByLimit(currentPage);
        }
    }
}

//页面跳转按钮点击事件
function goPage() {
    //获取跳转页面
    var page = $("#input_goPage").val();
    //获取显示的当前页数
    var showCurrentPage = $("#span_currentPage").html();
    //获取显示的总页数
    var showTotalPage = $("#span_totalPage").html();
    //如果跳转的是当前显示页面，就不操作
    if (page != showCurrentPage && page != "") {
        //判断跳转页面和总页数的关系
        if (parseInt(page) <= parseInt(showTotalPage)) {
            //调用函数
            findByLimit(page);
        } else {
            $("#input_goPage").val("");
        }
    } else {
        $("#input_goPage").val("");
    }
}

//首页按钮
function indexPage() {
    //获取显示的当前页数
    var showCurrentPage = $("#span_currentPage").html();
    if (showCurrentPage != 1) {
        findByLimit(1);
    }
}

//尾页按钮
function lastPage() {
    //获取显示的当前页数
    var showCurrentPage = $("#span_currentPage").html();
    //获取显示的总页数
    var showTotalPage = $("#span_totalPage").html();
    if (parseInt(showCurrentPage) != parseInt(showTotalPage)) {
        findByLimit(showTotalPage);
    }
}

//上一页按钮
function previousPage() {
    //获取显示的当前页数
    var showCurrentPage = $("#span_currentPage").html();
    if (parseInt(showCurrentPage) > 1) {
        findByLimit(parseInt(showCurrentPage) - 1);
    } else {
        alert("已经是首页啦");
    }
}

//下一页按钮
function nextPage() {
    //获取显示的当前页数
    var showCurrentPage = $("#span_currentPage").html();
    //获取显示的总页数
    var showTotalPage = $("#span_totalPage").html();
    if (parseInt(showCurrentPage) < parseInt(showTotalPage)) {
        findByLimit(parseInt(showCurrentPage) + 1);
    } else {
        alert("到底啦，别翻啦");
    }
}
