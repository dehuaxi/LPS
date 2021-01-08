
//-------------------------------页面加载执行--------------------------------------
$(document).ready(function () {
    //查询
    findAll();
})

//----------------------------------------添加-----------------------------------
//去添加函数,打开添加角色的模态框
function toAdd() {
    //清除输入框内容
    $("#add_carTypeName").val("");//名称
    $("#add_highLength").val("");
    $("#add_highHeight").val("");
    $("#add_lowLength").val("");
    $("#add_lowHeight").val("");
    $("#add_carWidth").val("");
    $("#add_carWeight").val("");
    $("#add_carVolume").val("");
    //打开模态框
    $("#addModal").modal("show");
    //模态框打开事件
    $("#addModal").on("shown.bs.modal", function () {
        $("#add_carTypeName").focus();
    })
}

function add() {
    //获取参数
    var carTypeName=$("#add_carTypeName").val();//名称
    var highLength=$("#add_highLength").val();
    var highHeight=$("#add_highHeight").val();
    var lowLength=$("#add_lowLength").val();
    var lowHeight=$("#add_lowHeight").val();
    var carWidth=$("#add_carWidth").val();
    var carWeight=$("#add_carWeight").val();
    var carVolume=$("#add_carVolume").val();
    //正整数
    var str=/[1-9]{1}[0-9]{0,10}/;
    //2位小数点
    var str2=/(([1-9]{1}[0-9]{0,6})|([0]{1}))([.]\d{1,2})?/;
    //判断条件
    if (carTypeName == "") {
        $("#add_carTypeName").focus();
    } else if (!str.test(highLength)) {
        $("#add_highLength").focus();
    }else if (!str.test(highHeight)) {
        $("#add_highHeight").focus();
    }else if (!str.test(lowLength)) {
        $("#add_lowLength").focus();
    }else if (!str.test(lowHeight)) {
        $("#add_lowHeight").focus();
    } else if (!str.test(carWidth)) {
        $("#add_carWidth").focus();
    } else if (!str2.test(carWeight)) {
        $("#add_carWeight").focus();
    } else if (!str2.test(carVolume)) {
        $("#add_carVolume").focus();
    } else {
        $("#div_loading").css("display","block");
        //提交后台
        $.ajax({
            url: 'carTypeAdd',
            type: 'post',
            data: {'carTypeName': carTypeName,
                'highLength': highLength,
                'highHeight': highHeight,
                'lowLength': lowLength,
                'lowHeight': lowHeight,
                'carWidth': carWidth,
                'carWeight': carWeight,
                'carVolume': carVolume},
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

//----------------------------------------修改-----------------------------------
//去修改函数
function toUpdate(a) {
    //获取所在的行所有单元格
    var td = a.parentNode.parentNode.childNodes;
    //获取参数
    var id = td[0].innerHTML;//1.id
    var carTypeName = td[1].innerHTML;//2.名称
    var highLength=td[2].innerHTML;
    var highHeight=td[3].innerHTML;
    var lowLength=td[4].innerHTML;
    var lowHeight=td[5].innerHTML;
    var carWidth = td[6].innerHTML;
    var carWeight=td[7].innerHTML;
    var carVolume=td[8].innerHTML;
    //把参数放入输入框
    $("#update_id").val(id);
    $("#update_carTypeName").val(carTypeName);
    $("#update_highLength").val(highLength);
    $("#update_highHeight").val(highHeight);
    $("#update_lowLength").val(lowLength);
    $("#update_lowHeight").val(lowHeight);
    $("#update_carWidth").val(carWidth);
    $("#update_carWeight").val(carWeight);
    $("#update_carVolume").val(carVolume);
    //打开模态框
    $("#updateModal").modal("show");
    //模态框打开事件
    $("#updateModal").on("shown.bs.modal", function () {
        //角色名称输入框获取焦点
        $("#update_carTypeName").focus();
    })
}

//修改按钮
function update() {
    //获取参数
    var id = $("#update_id").val();//id
    var carTypeName=$("#update_carTypeName").val();
    var highLength=$("#update_highLength").val();
    var highHeight=$("#update_highHeight").val();
    var lowLength=$("#update_lowLength").val();
    var lowHeight=$("#update_lowHeight").val();
    var carWidth=$("#update_carWidth").val();
    var carWeight=$("#update_carWeight").val();
    var carVolume=$("#update_carVolume").val();
    //正整数
    var str=/[1-9]{1}[0-9]{0,10}/;
    //2位小数点
    var str2=/(([1-9]{1}[0-9]{0,6})|([0]{1}))([.]\d{1,2})?/;
    //判断条件
    if (carTypeName == "") {
        $("#add_carTypeName").focus();
    } else if (!str.test(highLength)) {
        $("#add_highLength").focus();
    } else if (!str.test(highHeight)) {
        $("#add_highHeight").focus();
    }else if (!str.test(lowLength)) {
        $("#add_lowLength").focus();
    }else if (!str.test(lowHeight)) {
        $("#add_lowHeight").focus();
    }else if (!str.test(carWidth)) {
        $("#add_carWidth").focus();
    } else if (!str2.test(carWeight)) {
        $("#add_carWeight").focus();
    } else if (!str2.test(carVolume)) {
        $("#add_carVolume").focus();
    } else {
        $("#div_loading").css("display","block");
        //提交后台
        $.ajax({
            url: 'carTypeUpdate',
            type: 'post',
            data: {'id': id,
                'highLength': highLength,
                'highHeight': highHeight,
                'lowLength': lowLength,
                'lowHeight': lowHeight,
                'carWidth': carWidth,
                'carWeight': carWeight,
                'carVolume': carVolume},
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
            url: 'carTypeDelete',
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
        url: 'carType',
        type: 'post',
        dataType:'json',
        success: function (data) {
            //返回值不为空，有内容
            if (data.data != null) {
                //把页数信息放入页面
                var datas=data.data;
                //五、把数据填入页面
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
                        "</td><td>" + record[i].cartypename +
                        "</td><td>" + record[i].highlength +
                        "</td><td>" + record[i].highheight +
                        "</td><td>" + record[i].lowlength +
                        "</td><td>" + record[i].lowheight +
                        "</td><td>" + record[i].carwidth +
                        "</td><td>" + record[i].carweight +
                        "</td><td>" + record[i].carvolume +
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
