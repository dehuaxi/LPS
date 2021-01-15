
//-------------------------------页面加载执行--------------------------------------
$(document).ready(function () {
    //加载承运商
    loadCarrier();
    //加载车型
    loadCarType();
    //查询
    findByLimit(1);
})

function loadCarrier(){
    $.ajax({
        url:'allCarrier',
        dataType:'json',
        type:'post',
        success:function (data){
            if(data.code==0){
                for(var i=0;data.data.length;i++){
                    $("#select_carrierId").append("<option value='"+data.data[i].id+"'>"+data.data[i].carriername+"</option>");
                    $("#add_carrierId").append("<option value='"+data.data[i].id+"'>"+data.data[i].carriername+"</option>");
                }
            }else {
                alert(data.msg);
            }
        },
        error:function(jqXHR, textStatus, errorThrown){
            var status = jqXHR.status;//404,500等
            var text = jqXHR.statusText;//404对应的Not found,500对应的error
            alert("加载承运商信息失败：" + status + "  " + text);
        }
    })
}

function loadCarType(){
    $.ajax({
        url:'carType',
        dataType:'json',
        type:'post',
        success:function (data){
            if(data.code==0){
                for(var i=0;data.data.length;i++){
                    $("#select_carTypeId").append("<option value='"+data.data[i].id+"'>"+data.data[i].cartypename+"</option>");
                    $("#add_carTypeId").append("<option value='"+data.data[i].id+"'>"+data.data[i].cartypename+"</option>");
                }
            }else {
                alert(data.msg);
            }
        },
        error:function(jqXHR, textStatus, errorThrown){
            var status = jqXHR.status;//404,500等
            var text = jqXHR.statusText;//404对应的Not found,500对应的error
            alert("加载车型信息失败：" + status + "  " + text);
        }
    })
}
//----------------------------------------添加-----------------------------------
//去添加函数,打开添加角色的模态框
function toAdd() {
    //清除输入框内容
    $("#add_carNumber").val("");
    $("#add_carrierId").val("0");
    $("#add_carTypeId").val("0");
    $("#add_highLength").val("");
    $("#add_highHeight").val("");
    $("#add_lowLength").val("");
    $("#add_lowHeight").val("");
    $("#add_carWidth").val("");
    //打开模态框
    $("#addModal").modal("show");
    //模态框打开事件
    $("#addModal").on("shown.bs.modal", function () {
        $("#add_carNumber").focus();
    })
}

function add() {
    //获取参数
    var carNumber=$("#add_carNumber").val();
    var carrierId=$("#add_carrierId").val();
    var carTypeId=$("#add_carTypeId").val();
    var highLength=$("#add_highLength").val();
    var highHeight=$("#add_highHeight").val();
    var lowLength=$("#add_lowLength").val();
    var lowHeight=$("#add_lowHeight").val();
    var carWidth=$("#add_carWidth").val();
    //判断条件
    var str=/^([京津晋冀蒙辽吉黑沪苏浙皖闽赣鲁豫鄂湘粤桂琼渝川贵云藏陕甘青宁新][ABCDEFGHJKLMNPQRSTUVWXY][1-9DF][1-9ABCDEFGHJKLMNPQRSTUVWXYZ]\d{3}[1-9DF]|[京津晋冀蒙辽吉黑沪苏浙皖闽赣鲁豫鄂湘粤桂琼渝川贵云藏陕甘青宁新][ABCDEFGHJKLMNPQRSTUVWXY][\dABCDEFGHJKLNMxPQRSTUVWXYZ]{5})$/;
    var str1=/^[1]{1}[0-9]{10}$/;
    var str2=/^[1-9]{1}[0-9]{0,10}$/;
    var str3=/^[0-9]{1,11}$/;
    if (!str.test(carNumber)) {
        $("#add_carNumber").focus();
    }else if(carrierId==0){
        alert("选择承运商");
    } else if(carTypeId==0){
        alert("选择车型");
    } else if(!str3.test(highLength)){
        $("#add_highLength").focus();
    }else if(!str3.test(highHeight)){
        $("#add_highHeight").focus();
    }else if(!str2.test(lowLength)){
        $("#add_lowLength").focus();
    }else if(!str2.test(lowHeight)){
        $("#add_lowHeight").focus();
    }else if(!str2.test(carWidth)){
        $("#add_carWidth").focus();
    }else {
        $("#div_loading").css("display","block");
        //提交后台
        $.ajax({
            url: 'carAdd',
            type: 'post',
            data: {'carNumber': carNumber,
                'carrierId':carrierId,
                'carTypeId':carTypeId,
                'highLength':highLength,
                'highHeight':highHeight,
                'lowLength':lowLength,
                'lowHeight':lowHeight,
                'carWidth':carWidth},
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

//----------------------------------------修改-----------------------------------
//去修改函数
function toUpdate(a) {
    //获取所在的行所有单元格
    var td = a.parentNode.parentNode.childNodes;
    var id = td[0].innerHTML;//1.id
    $("#update_id").val(id);
    var carNumber=$(td[1]).text();
    $("#update_carNumber").val(carNumber);
    var carrierName=$(td[2]).text();
    var carTypeName=$(td[3]).text();
    var highLength=$(td[4]).text();
    $("#update_highLength").val(highLength);
    var highHeight=$(td[5]).text();
    $("#update_highHeight").val(highHeight);
    var lowLength=$(td[6]).text();
    $("#update_lowLength").val(lowLength);
    var lowHeight=$(td[7]).text();
    $("#update_lowHeight").val(lowHeight);
    var carWidth=$(td[8]).text();
    $("#update_carWidth").val(carWidth);
    var isOpen=true;
    //加载承运商
    $.ajax({
        url:'allCarrier',
        dataType:'json',
        type:'post',
        success:function (data){
            if(data.code==0){
                for(var i=0;data.data.length;i++){
                    if(data.data[i].carriername==carrierName){
                        $("#update_carrierId").append("<option value='"+data.data[i].id+"' selected='selected'>"+data.data[i].carriername+"</option>")
                    }else {
                        $("#update_carrierId").append("<option value='"+data.data[i].id+"'>"+data.data[i].carriername+"</option>")
                    }
                }
            }else {
                alert(data.msg);
                isOpen=false;
            }
        },
        error:function(jqXHR, textStatus, errorThrown){
            var status = jqXHR.status;//404,500等
            var text = jqXHR.statusText;//404对应的Not found,500对应的error
            alert("加载承运商信息失败：" + status + "  " + text);
            isOpen=false;
        }
    })
    //加载车型信息
    $.ajax({
        url:'carType',
        dataType:'json',
        type:'post',
        success:function (data){
            if(data.code==0){
                for(var i=0;data.data.length;i++){
                    if(data.data[i].cartypename==carTypeName){
                        $("#update_carTypeId").append("<option value='"+data.data[i].id+"' selected='selected'>"+data.data[i].cartypename+"</option>");
                    }else {
                        $("#update_carTypeId").append("<option value='"+data.data[i].id+"'>"+data.data[i].cartypename+"</option>");
                    }
                }
            }else {
                alert(data.msg);
                isOpen=false;
            }
        },
        error:function(jqXHR, textStatus, errorThrown){
            var status = jqXHR.status;//404,500等
            var text = jqXHR.statusText;//404对应的Not found,500对应的error
            alert("加载车型信息失败：" + status + "  " + text);
            isOpen=false;
        }
    })
    if(isOpen){
        //打开模态框
        $("#updateModal").modal("show");
        //模态框打开事件
        $("#updateModal").on("shown.bs.modal", function () {
            $("#update_carNumber").focus();
        })
    }
}
//修改按钮
function update() {
    //获取参数
    var id = $("#update_id").val();//id
    var carrierId=$("#update_carrierId").val();
    var carTypeId=$("#update_carTypeId").val();
    var highLength=$("#update_highLength").val();
    var highHeight=$("#update_highHeight").val();
    var lowLength=$("#update_lowLength").val();
    var lowHeight=$("#update_lowHeight").val();
    var carWidth=$("#update_carWidth").val();
    //判断条件
    var str1=/^[1]{1}[0-9]{10}$/;
    var str2=/^[1-9]{1}[0-9]{0,10}$/;
    var str3=/^[0-9]{1,11}$/;
    if(carrierId==0){
        alert("选择承运商");
    } else if(carTypeId==0){
        alert("选择车型");
    } else if(!str3.test(highLength)){
        $("#update_highLength").focus();
    }else if(!str3.test(highHeight)){
        $("#update_highHeight").focus();
    }else if(!str2.test(lowLength)){
        $("#update_lowLength").focus();
    }else if(!str2.test(lowHeight)){
        $("#update_lowHeight").focus();
    }else if(!str2.test(carWidth)){
        $("#update_carWidth").focus();
    }else {
        $("#div_loading").css("display","block");
        //提交后台
        $.ajax({
            url: 'carUpdate',
            type: 'post',
            data: {'id': id,
                'carrierId': carrierId,
                'carTypeId': carTypeId,
                'highLength':highLength,
                'highHeight':highHeight,
                'lowLength':lowLength,
                'lowHeight':lowHeight,
                'carWidth':carWidth},
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

//----------------------------------------删除-----------------------------------
function toDelete(a) {
    //获取角色id
    var id = a.parentNode.parentNode.childNodes[0].innerHTML;
    var tips = confirm("确定删除吗？");
    if (tips == true) {
        $("#div_loading").css("display","block");
        //ajax删除
        $.ajax({
            url: 'carDelete',
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
    var carNumber=$("#input_carNumber").val();
    var carrierId=$("#select_carrierId").val();
    var carTypeId=$("#select_carTypeId").val();
    //先清除旧数据
    $("#table_data").html("");
    //后台查询
    $.ajax({
        url: 'car',
        type: 'post',
        data: {
            'carNumber':carNumber,
            'carrierId':carrierId,
            'carTypeId':carTypeId,
            'currentPage': currentPage
        },
        dataType:'json',
        success: function (data) {
            if(data.code==0){
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
                            "</td><td>" + record[i].carnumber +
                            "</td><td>" + record[i].carrier.carriername +
                            "</td><td>" + record[i].cartype.cartypename +
                            "</td><td>" + record[i].highlength +
                            "</td><td>" + record[i].highheight +
                            "</td><td>" + record[i].lowlength +
                            "</td><td>" + record[i].lowheight +
                            "</td><td>" + record[i].carwidth +
                            "</td>" + td+"</tr>";
                        $("#table_data").append(str);
                    }
                } else {
                    //隐藏分页信息
                    $("#div_page").css("display", "none");
                }
            }else {
                alert(data.msg);
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
