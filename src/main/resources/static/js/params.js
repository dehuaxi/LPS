
//-------------------------------页面加载执行--------------------------------------
$(document).ready(function () {
    //查询
    findAll();
})

//----------------------------------------添加-----------------------------------
//去添加函数,打开添加角色的模态框
function toAdd() {
    //清除输入框内容
    $("#add_paramName").val("");//名称
    $("#add_paramValue").val("");
    $("#add_paramType").val("");
    $("#add_describes").val("");
    //打开模态框
    $("#addModal").modal("show");
    //模态框打开事件
    $("#addModal").on("shown.bs.modal", function () {
        $("#add_paramName").focus();
    })
}

function add() {
    //获取参数
    var paramName=$("#add_paramName").val();//名称
    var paramValue=$("#add_paramValue").val();
    var paramType=$("#add_paramType").val();
    var describes=$("#add_describes").val();
    //正整数
    var str=/[1-9]{1}[0-9]{0,10}/;
    //2位小数点
    var str2=/(([1-9]{1}[0-9]{0,6})|([0]{1}))([.]\d{1,2})?/;
    //判断条件
    if (paramName == "") {
        $("#add_paramName").focus();
    } else if(paramValue==""){
        $("#add_paramValue").focus();
    }else {
        //是否可以传入后台
        var suer=false;
        if(paramType=="int"){
            if(!str.test(paramValue)){
                alert("填入整数");
                $("#add_paramValue").focus();
            }else {
                suer=true;
            }
        }else if(paramType=="double"){
            if(!str2.test(paramValue)){
                alert("填入最多保留2位小数点的数字");
                $("#add_paramValue").focus();
            }else {
                suer=true;
            }
        }else {
            suer=true;
        }
        if(suer){
            $("#div_loading").css("display","block");
            //提交后台
            $.ajax({
                url: 'paramsAdd',
                type: 'post',
                data: {'paramName': paramName,
                    'paramValue': paramValue,
                    'paramType': paramType,
                    'describes': describes},
                success: function (data) {
                    if (data.code==0) {
                        findAll();
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
}

//----------------------------------------修改-----------------------------------
//去修改函数
function toUpdate(a) {
    //获取所在的行所有单元格
    var td = a.parentNode.parentNode.childNodes;
    //获取参数
    var id = td[0].innerHTML;//1.id
    var paramName = td[1].innerHTML;//2.名称
    var paramValue=td[2].innerHTML;
    var paramType=td[3].innerHTML;
    var describes=td[4].innerHTML;
    //把参数放入输入框
    $("#update_id").val(id);
    $("#update_paramName").val(paramName);
    $("#update_paramValue").val(paramValue);
    $("#update_paramType").val(paramType);
    $("#update_describes").val(describes);
    //打开模态框
    $("#updateModal").modal("show");
    //模态框打开事件
    $("#updateModal").on("shown.bs.modal", function () {
        //角色名称输入框获取焦点
        $("#update_paramName").focus();
    })
}

//修改按钮
function update() {
    //获取参数
    var id = $("#update_id").val();//id
    var paramName=$("#update_paramName").val();
    var paramValue=$("#update_paramValue").val();
    var paramType=$("#update_paramType").val();
    var describes=$("#update_describes").val();
    //判断条件
    //正整数
    var str=/[1-9]{1}[0-9]{0,10}/;
    //2位小数点
    var str2=/(([1-9]{1}[0-9]{0,6})|([0]{1}))([.]\d{1,2})?/;
    //判断条件
    if (paramName == "") {
        $("#update_paramName").focus();
    } else if(paramValue==""){
        $("#update_paramName").focus();
    }else {
        //是否可以传入后台
        var suer=false;
        if(paramType=="int"){
            if(!str.test(paramValue)){
                alert("填入整数");
                $("#update_paramValue").focus();
            }else {
                suer=true;
            }
        }else if(paramType=="double"){
            if(!str2.test(paramValue)){
                alert("填入最多保留2位小数点的数字");
                $("#update_paramValue").focus();
            }else {
                suer=true;
            }
        }else {
            suer=true;
        }
        if(suer){
            $("#div_loading").css("display","block");
            //提交后台
            $.ajax({
                url: 'paramsUpdate',
                type: 'post',
                data: {'id':id,
                    'paramName': paramName,
                    'paramValue': paramValue,
                    'paramType': paramType,
                    'describes': describes},
                success: function (data) {
                    if (data.code==0) {
                        findAll();
                        $("#updateModal").modal("hide");
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
}

//----------------------------------------删除-----------------------------------
function toDelete(a) {
    //获取角色id
    var id = a.parentNode.parentNode.childNodes[0].innerHTML;
    var tips = confirm("确定删除吗？");
    if (tips == true) {
        $("#div_loading").css("display","block");
        //ajax删除
        $.ajax({
            url: 'paramsDelete',
            type: 'post',
            data: {'id': id},
            success: function (data) {
                if (data.code==0) {
                    findAll();
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

//------------------------------------查询记录-------------------------------
function findAll(currentPage) {
    //显示加载提示信息
    $("#div_loading").css("display", "block");
    //先清除旧数据
    $("#table_data").html("");
    //后台查询
    $.ajax({
        url: 'params',
        type: 'post',
        dataType:'json',
        success: function (data) {
            //返回值不为空，有内容
            if (data.data != null) {
                //把页数信息放入页面
                var datas=data.data;
                //把数据填入页面
                var record = datas;
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
                        "</td><td>" + record[i].paramname +
                        "</td><td>" + record[i].paramvalue +
                        "</td><td>" + record[i].paramtype +
                        "</td><td>" + record[i].describes +
                        "</td>" + td+"</tr>";
                    $("#table_data").append(str);
                }
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
