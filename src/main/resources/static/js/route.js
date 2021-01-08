
//-------------------------------页面加载执行--------------------------------------
$(document).ready(function () {
    //加载查询栏、添加模态框的工厂
    $.ajax({
        url: 'currentFactory',
        type: 'post',
        dataType:'json',
        success: function (data) {
            if (data.code==0) {
                for(var i=0;i<data.data.length;i++){
                    $("#select_factoryId").append("<option value='"+data.data[i].id+"'>"+data.data[i].factoryname+"</option>")
                    $("#add_factoryId").append("<option value='"+data.data[i].id+"'>"+data.data[i].factoryname+"</option>")
                }
            } else {
                alert(data.msg);
            }
        },
        error:function(jqXHR, textStatus, errorThrown){
            var status = jqXHR.status;//404,500等
            var text = jqXHR.statusText;//404对应的Not found,500对应的error
            alert("加载工厂信息失败：" + status + "  " + text);
        }
    });
    //加载查询栏、添加模态框的区域
    $.ajax({
        url: 'allArea',
        type: 'post',
        dataType:'json',
        success: function (data) {
            if(data.date===null){
                alert("没有区域信息，请添加");
            } else {
                for(var i=0;i<data.data.length;i++){
                    $("#select_areaId").append("<option value='"+data.data[i].id+"'>"+data.data[i].areaname+"</option>")
                    $("#add_areaId").append("<option value='"+data.data[i].id+"'>"+data.data[i].areaname+"</option>")
                }
            }
        },
        error:function(jqXHR, textStatus, errorThrown){
            var status = jqXHR.status;//404,500等
            var text = jqXHR.statusText;//404对应的Not found,500对应的error
            alert("加载区域信息失败：" + status + "  " + text);
        }
    });
    //查询
    findByLimit(1);
    //加载中转仓列表
    $.ajax({
        url: 'warehouseZtree',
        type: 'post',
        dataType:'json',
        data:{},
        success: function (data) {
            if (data.code==0) {
                //zTree启动
                zTree=$.fn.zTree.init($("#add_zTree"), setting, data.data);
                zTree=$.fn.zTree.init($("#update_zTree"), setting, data.data);
            }else {
                alert(data.msg);
            }
        },
        error:function(jqXHR, textStatus, errorThrown){
            var status = jqXHR.status;//404,500等
            var text = jqXHR.statusText;//404对应的Not found,500对应的error
            alert("中转仓加载失败：" + status + "  " + text);
        }
    });
})

//-----------------------------------ztree-----------------------------------
//设置ztree
var setting = {
    view: {
        showLine: false,
        showIcon: false,
        dblClickExpand: false,
    },
    data: {
        simpleData: {
            enable: true,
            idKey:'id',
            pIdKey:'pid',
            rootPId:null
        }
    },
    check: {
        enable: true,
    },
    callback: {
        onCheck: zTreeOnCheck,
    }
};
//选中时
function zTreeOnCheck(event, treeId, treeNode) {
    var warehouse=treeNode.name;
    if(treeId=="add_zTree"){
        var value=$("#add_warehouse").val();
        if(value==""){
            $("#add_warehouse").val(warehouse);
        }else {
            var list=value.split(",");
            if(treeNode.checked){
                //选中，那么添加选中的值
                var str1="";
                for(var i=0;i<list.length;i++){
                    if(list[i]!=warehouse){
                        str1+=","+list[i];
                    }
                }
                if(str1!=""){
                    str1=str1.substring(1);
                    str1=str1+","+warehouse;
                }else {
                    str1=warehouse;
                }
                $("#add_warehouse").val(str1);
            }else {
                //未被选中，那么从已存在的值中去掉
                var str1="";
                for(var i=0;i<list.length;i++){
                    if(list[i]!=warehouse){
                        str1+=","+list[i];
                    }
                }
                if(str1!=""){
                    str1=str1.substring(1);
                }
                $("#add_warehouse").val(str1);
            }
            if(value.indexOf(warehouse)<0){
                $("#add_warehouse").val(value+","+warehouse);
            }
        }
    }
    if(treeId=="update_zTree"){
        var value=$("#update_warehouse").val();
        if(value==""){
            $("#update_warehouse").val(warehouse);
        }else {
            var list=value.split(",");
            if(treeNode.checked){
                //选中，那么添加选中的值
                var str1="";
                for(var i=0;i<list.length;i++){
                    if(list[i]!=warehouse){
                        str1+=","+list[i];
                    }
                }
                if(str1!=""){
                    str1=str1.substring(1);
                    str1=str1+","+warehouse;
                }else {
                    str1=warehouse;
                }
                $("#update_warehouse").val(str1);
            }else {
                //未被选中，那么从已存在的值中去掉
                var str1="";
                for(var i=0;i<list.length;i++){
                    if(list[i]!=warehouse){
                        str1+=","+list[i];
                    }
                }
                if(str1!=""){
                    str1=str1.substring(1);
                }
                $("#update_warehouse").val(str1);
            }
        }
    }
};
//----------------------------------------添加-----------------------------------
//去添加函数,打开添加角色的模态框
function toAdd() {
    //清除输入框内容
    $("#add_routeName").val("");//名称
    $("#add_routeNumber").val("");//编号
    $("#add_describes").val("");//描述
    $("#add_warehouse").val("");//中转仓
    //获取zTree对象
    var zTree = $.fn.zTree.getZTreeObj("add_zTree");
    //把所有节点变为未选中
    zTree.checkAllNodes(false);
    ///打开模态框
    $("#addModal").modal("show");
    //模态框打开事件
    $("#addModal").on("shown.bs.modal", function () {
        $("#add_routeName").focus();
    })
}

function add() {
    //获取参数
    var routeName=$("#add_routeName").val();//名称
    var routeNumber=$("#add_routeNumber").val();//编号
    var describes=$("#add_describes").val();//描述
    var warehouse=$("#add_warehouse").val();//中转仓
    var factoryId=$("#add_factoryId").val();
    var areaId=$("#add_areaId").val();
    //判断条件
    if (routeName == "") {
        $("#add_routeName").focus();
    } else if (routeNumber == "") {
        $("#add_routeNumber").focus();
    }else if(factoryId=="0"){
        alert("请选择工厂")
    }else if(areaId=="0"){
        alert("请选择区域")
    }else{
        $("#div_loading").css("display","block");
        $.ajax({
            url: 'routeAdd',
            type: 'post',
            data: {'routeName':routeName,
                'routeNumber':routeNumber,
                'describes':describes,
                'areaId':areaId,
                'factoryId':factoryId,
                'warehouse':warehouse},
            dataType:'json',
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

//----------------------------------------批量添加-----------------------------------
function toUpload() {
    //清除选中的文件
    $("#uploadFile").val("");
    //清除提示文字
    $("#uploadResult").html("");
    //打开模态框
    $("#uploadModal").modal("show");
}

function upload() {
//获取文件
    var file = $("#uploadFile").val();
    //获取表单中的文件
    var fileData = new FormData($('#uploadForm')[0]);
    if (file != "") {//上传不为空，则ajax上传
        //隐藏加载提示
        $("#div_loading").css("display", "block");
        //清空提示信息
        $("#uploadResult").html("");
        $("#uploadFile").val("");
        $.ajax({
            url: "routeAddUpload",
            type: "post",
            data: fileData,
            cache: false,
            processData: false,
            contentType: false,
            success: function (data) {
                if(data.code==0){
                    alert(data.data);
                    //获取当前页
                    var currentPage = $("#span_currentPage").html();
                    if(!/^[0-9]{1,10}$/.test(currentPage)){
                        currentPage=1;
                    }
                    //调用分页查询
                    findByLimit(currentPage);
                    //关闭模态框
                    $("#uploadModal").modal("hide");
                }else{
                    var error=data.msg;
                    if(error.indexOf(";")>-1){
                        var list=error.split(";");
                        for(var i=0;i<list.length;i++){
                            $("#uploadResult").append("<span>"+list[i]+"</span><br>");
                        }
                    }else{
                        $("#uploadResult").html("<span>"+error+"</span>");
                    }
                }
                //隐藏加载提示
                $("#div_loading").css("display", "none");
            },
            error:function(jqXHR, textStatus, errorThrown){
                var status = jqXHR.status;//404,500等
                var text = jqXHR.statusText;//404对应的Not found,500对应的error
                alert("上传失败：" + status + "  " + text);
                //隐藏加载提示
                $("#div_loading").css("display", "none");
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
    $.ajax({
        url: 'routeById',
        type: 'post',
        dataType:'json',
        data:{'id':id},
        success: function (data) {
            if (data.code==0) {
                $("#update_id").val(id);
                $("#update_routeName").val(data.data.route.routename);
                $("#update_routeNumber").val(data.data.route.routenumber);
                $("#update_describes").val(data.data.route.describes);
                //加载仓库
                $("#update_warehouse").val("");
                //获取zTree对象
                var zTree = $.fn.zTree.getZTreeObj("update_zTree");
                //把所有节点变为未选中
                zTree.checkAllNodes(false);
                var warehouse=data.data.warehouse;
                if(warehouse!=""){
                    //获取zTree的所有的节点的简单array集合
                    var array = zTree.transformToArray(zTree.getNodes());
                    var wareStr="";
                    for(var h=0;h<warehouse.length;h++){
                        wareStr=","+warehouse[h].warehousename;
                        //循环所有节点
                        for (var s = 0; s < array.length; s++) {
                            if (array[s].name == warehouse[h].warehousename) {
                                //如果节点名相同，钩选这个节点,并且不影响子节点的勾选状态
                                zTree.checkNode(array[s], true, false);
                            }
                        }
                    }
                    wareStr=wareStr.substring(1);
                    $("#update_warehouse").val(wareStr);
                }
                var areaId=data.data.route.area.id;
                //打开模态框
                $("#updateModal").modal("show");
                //模态框打开事件
                $("#updateModal").on("shown.bs.modal", function () {
                    //名称输入框获取焦点
                    $("#update_routeName").focus();
                })
            } else {
                alert(data.msg);
            }
        },
        error:function(jqXHR, textStatus, errorThrown){
            var status = jqXHR.status;//404,500等
            var text = jqXHR.statusText;//404对应的Not found,500对应的error
            alert("获取线路信息失败：" + status + "  " + text);
        }
    });
}

//修改按钮
function update() {
    //获取参数
    var id = $("#update_id").val();//id
    var routeName = $("#update_routeName").val();//名称
    var routeNumber = $("#update_routeNumber").val();//编号
    var describes =$("#update_describes").val();
    var warehouse=$("#update_warehouse").val();
    if (routeName == "") {
        $("#update_routeName").focus();
    } else if (routeNumber == "") {
        $("#update_routeNumber").focus();
    } else {
        $("#div_loading").css("display","block");
        //提交后台
        $.ajax({
            url: 'routeUpdate',
            type: 'post',
            data: {'id': id,
                'routeName': routeName,
                'routeNumber': routeNumber,
                'describes': describes,
                'warehouse':warehouse},
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
            url: 'routeDelete',
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

//----------------------------------------下载-----------------------------------
function toDownload() {
    var routeNumber=$("#input_routeNumber").val();
    var routeName=$("#input_routeName").val();
    var factoryId=$("#select_factoryId").val();
    var areaId=$("#select_areaId").val();
    if(factoryId=="0"){
        alert("必须选择工厂")
    }else {
        window.open("routeDownload?routeNumber="+routeNumber+"&routeName="+routeName+"&factoryId="+factoryId+"&areaId="+areaId);
    }
}

//------------------------------------分页查询收货记录-------------------------------
function findByLimit(currentPage) {
    //显示加载提示信息
    $("#div_loading").css("display", "block");
    //获取参数
    var routeNumber=$("#input_routeNumber").val();
    var routeName=$("#input_routeName").val();
    var factoryId=$("#select_factoryId").val();
    var areaId=$("#select_areaId").val();
    //先清除旧数据
    $("#table_data").html("");
    //后台查询
    $.ajax({
        url: 'route',
        type: 'post',
        data: {
            'routeName':routeName,
            'routeNumber':routeNumber,
            'areaId':areaId,
            'factoryId':factoryId,
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
                        var warehouse="";
                        if(record[i].warehouse!=""){
                            for(var k=0;k<record[i].warehouse.length;k++){
                                warehouse+=" -> "+record[i].warehouse[k].warehousename;
                            }
                            warehouse=warehouse.substring(4);
                        }
                        var route=record[i].route;
                        var str = "<tr><td style='display: none'>" + route.id + "</td>"+
                            "<td>" + route.routename +"</td>"+
                            "<td>" + route.routenumber +"</td>"+
                            "<td>" + route.describes +"</td>"+
                            "<td>" + route.area.areaname +"</td>"+
                            "<td>" + route.factory.factoryname +"</td>"+
                            "<td>" + warehouse+"</td>"+
                            td+"</tr>";
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
