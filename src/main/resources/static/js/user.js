
//-------------------------------页面加载执行--------------------------------------
$(document).ready(function () {
    //1.加载用户
    findByLimit(1);
    //2.加载添加模态框的线路ztree
    loadAddZtree();
})

//--------------------------加载ztree----------------
//设置ztree
var Setting = {
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
    }
};

function loadAddZtree() {
    $.ajax({
        url: 'routeGroupFactorynumber',
        type: 'post',
        dataType:'json',
        success: function (data) {
            if (data.code==0) {
                //zTree启动
                var zTree=$.fn.zTree.init($("#add_zTree"), Setting, data.data);
                //获取根节点
                var node=zTree.getNodes();
                for(var i=0;i<node.length;i++){
                    node[i].nocheck = true;
                }
                //展开全部节点
                zTree.expandAll(true);
                //zTree启动
                var zTree1=$.fn.zTree.init($("#update_zTree"), Setting, data.data);
                //获取根节点
                var node1=zTree1.getNodes();
                for(var i=0;i<node1.length;i++){
                    node1[i].nocheck = true;
                }
                //展开全部节点
                zTree1.expandAll(true);
            }else {
                alert(data.msg+"，添加、修改用户功能将无法使用");
            }
        },
        error:function(jqXHR, textStatus, errorThrown){
            var status = jqXHR.status;//404,500等
            var text = jqXHR.statusText;//404对应的Not found,500对应的error
            alert("线路加载失败：" + status + "  " + text);
        }
    });
}

//----------------------------------------添加-----------------------------------
//去添加函数,打开添加的模态框
function toAdd() {
    //清除输入框内容
    $("#add_userName").val("");//用户名
    $("#add_password").val("");//密码
    $("#add_roleName").html("");//角色
    //线路全部不选中,把所有节点变为未选中
    var zTree = $.fn.zTree.getZTreeObj("add_zTree");
    zTree.checkAllNodes(false);
    //加载角色
    $.ajax({
        url: 'currentRole',
        type: 'post',
        dataType:'json',
        success: function (data) {
            if (data.code==0) {
                $("#add_roleName").append("<option value=''>选择角色</option>");
                for(var i=0;i<data.data.length;i++){
                    $("#add_roleName").append("<option>"+data.data[i].rolename+"</option>");
                }
                //打开模态框
                $("#addModal").modal("show");
                //模态框打开事件
                $("#addModal").on("shown.bs.modal", function () {
                    $("#add_userName").focus();
                })
            }else {
                alert(data.msg);
            }
        },
        error:function(jqXHR, textStatus, errorThrown){
            var status = jqXHR.status;//404,500等
            var text = jqXHR.statusText;//404对应的Not found,500对应的error
            alert("角色加载失败：" + status + "  " + text);
        }
    });
}

//添加用户
function add() {
    //获取参数
    var userName = $("#add_userName").val();//名称
    var password = $("#add_password").val();//密码
    var roleName = $("#add_roleName").val();//角色名称
    //获取页面中ztree对象，参数是ul标签的id
    var zTree = $.fn.zTree.getZTreeObj("add_zTree");
    //获取所有被选中的节点
    var nodes = zTree.getCheckedNodes(true);
    //线路
    var route="";
    for (var i = 0; i < nodes.length; i++) {
        route += ',' + nodes[i].name;
    }
    if (route.length > 0) {
        //如果有被勾选的节点，去掉最前面的,号
        route = route.substring(1);
    }
    if(userName==""){
        $("#add_userName").focus();
    }else if(!/^[0-9a-zA-Z]{6,20}$/.test(password)){
        $("#add_password").focus();
    }else if(roleName==""){
        alert("请选择角色");
    }else if(route==""){
        alert("请给用户分配线路")
    }else {
        $("#div_loading").css("display","block");
        //提交后台
        $.ajax({
            url: 'userAdd',
            type: 'post',
            data: {'userName': userName, 'password': password, 'roleName': roleName,'routeNames':route},
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
    //根据用户id查询用户信息，并加载角色选择项、线路信息
    $.ajax({
        url: 'userById',
        type: 'post',
        data: {'id': id},
        dataType:'json',
        success: function (data) {
            if (data.code==0) {
                var roleName=data.data.user.rolename;
                //加载用户信息
                $("#update_id").val(data.data.user.id);
                $("#update_userName").val(data.data.user.username);
                //线路信息
                var routes=data.data.route;
                //获取zTree
                var zTree= $.fn.zTree.getZTreeObj("update_zTree");
                //所有节点不选中
                zTree.checkAllNodes(false);
                //获取zTree的所有的节点的简单array集合
                var array = zTree.transformToArray(zTree.getNodes());
                for (var i = 0; i < routes.length; i++) {
                    //循环所有节点
                    for (var k = 0; k < array.length; k++) {
                        if (array[k].name == routes[i].routename) {
                            //如果节点名相同，钩选这个节点,并且不影响子节点的勾选状态
                            zTree.checkNode(array[k], true, false);
                        }
                    }
                }
                //加载角色
                $("#update_roleName").html("");
                $.ajax({
                    url: 'currentRole',
                    type: 'post',
                    dataType:'json',
                    success: function (data) {
                        if (data.code==0) {
                            $("#update_roleName").append("<option value=''>选择角色</option>");
                            for(var i=0;i<data.data.length;i++){
                                if(data.data[i].rolename==roleName){
                                    $("#update_roleName").append("<option selected='selected'>"+data.data[i].rolename+"</option>")
                                }else {
                                    $("#update_roleName").append("<option>"+data.data[i].rolename+"</option>");
                                }
                            }
                            //打开模态框
                            $("#updateModal").modal("show");
                            //模态框打开事件
                            $("#updateModal").on("shown.bs.modal", function () {
                                $("#update_password").focus();
                            })
                        }else {
                            alert(data.msg);
                        }
                    },
                    error:function(jqXHR, textStatus, errorThrown){
                        var status = jqXHR.status;//404,500等
                        var text = jqXHR.statusText;//404对应的Not found,500对应的error
                        alert("角色加载失败：" + status + "  " + text);
                    }
                });
            }else {
                alert(data.msg);
            }
        },
        error:function(jqXHR, textStatus, errorThrown){
            var status = jqXHR.status;//404,500等
            var text = jqXHR.statusText;//404对应的Not found,500对应的error
            alert("用户信息获取失败：" + status + "  " + text+"无法修改");
        }
    });
}

//修改按钮
function update() {
    //获取参数
    var id = $("#update_id").val();//id
    var roleName = $("#update_roleName").val();//角色名称
    var password=$("#update_password").val();//密码
    //获取页面中ztree对象，参数是ul标签的id
    var zTree = $.fn.zTree.getZTreeObj("update_zTree");
    //获取所有被选中的节点
    var nodes = zTree.getCheckedNodes(true);
    //线路
    var route="";
    for (var i = 0; i < nodes.length; i++) {
        route += ',' + nodes[i].name;
    }
    if (route.length > 0) {
        //如果有被勾选的节点，去掉最前面的,号
        route = route.substring(1);
    }
    if(roleName==""){
        alert("请选择角色");
    }else if(route==""){
        alert("请为用户分配线路");
    }else {
        $("#div_loading").css("display","block");
        //提交后台
        $.ajax({
            url: 'userUpdate',
            type: 'post',
            data: {'id': id, 'roleName': roleName,'password':password, 'routeNames': route},
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
            url: 'userDelete',
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
    var userName=$("#input_userName").val();
    var roleName=$("#input_roleName").val();
    //先清除旧数据
    $("#table_data").html("");
    //后台查询
    $.ajax({
        url: 'user',
        type: 'post',
        data: {
            'userName':userName,
            'roleName': roleName,
            'currentPage':currentPage
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
                        "</td><td>" + record[i].username +
                        "</td><td>" + record[i].rolename +
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
