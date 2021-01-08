
//-------------------------------页面加载执行--------------------------------------
$(document).ready(function () {
    //2.加载权限
    findAll(1);
})

//-----------------------------------ztree-----------------------------------
//设置ztree
var setting = {
    view: {
        showLine: true,//是否显示节点之间的连线
        showIcon: false,//是否显示节点上的icon图标
        dblClickExpand: true,//是否允许双击父节点时收缩或展开子节点
        addHoverDom: addHoverDom,//用于当鼠标移动到节点上时，显示用户自定义控件
        removeHoverDom: removeHoverDom,//用于当鼠标移出节点时，隐藏用户自定义控件
        txtSelectedEnable: true,//设置 zTree 是否允许可以选择 zTree DOM 内的文本。
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
        enable: false,
    },
    edit:{
        enable:true,
        showRemoveBtn: true,
        removeTitle: "删除权限及其子权限",
        showRenameBtn: true,
        renameTitle: "修改权限信息"
    },
    callback:{
        beforeRemove:deletePermission,//点击删除节点按钮时触发
        beforeEditName: toEditPermission,//点击修改按钮后进入修改编辑前出发
    }
};

//-----------------显示、隐藏自定的添加按钮-----------------------
//鼠标放到节点上时显示自定义按钮
function addHoverDom(treeId, treeNode) {
    var aObj = $("#" + treeNode.tId + "_a");
    if ($("#diyBtn_"+treeNode.id).length>0) return;
    var editStr = "<span id='diyBtn_space_" +treeNode.id+ "'></span>"
        + "<button type='button' style='background-color: #00B83F;color: white;border:0px solid' id='diyBtn_" + treeNode.id
        + "' title='添加子权限' onfocus='this.blur();'>+</button><span id='diyBtn_space2_" +treeNode.id+ "'></span><button type='button' style='background-color: #085bb8;color: white;border:0px solid' id='diyBtn2_" + treeNode.id
    + "' title='添加同级权限' onfocus='this.blur();'>+</button>";
    aObj.append(editStr);
    //添加子权限按钮
    var btn = $("#diyBtn_"+treeNode.id);
    if (btn){
        btn.bind("click", function() {
            //先看是否有权限
            if (addData == false) {
                alert("没有添加权限");
            } else {
                $("#add_url").val("");
                $("#add_permissionName").val("");
                //根据权限获取父节点
                $.ajax({
                    url: 'permissionByName',
                    type: 'post',
                    data: {'permissionName': treeNode.name},
                    success: function (data) {
                        $("#div_loading").css("display", "none");
                        if (data.code == 0) {
                            //添加子权限时，父id为当前权限的id
                            $("#add_pid").val(data.data.id);
                            $("#add_titel").html("添加子权限");
                            //显示添加模态框
                            $("#addModal").modal("show");
                            $("#addModal").on("shown.bs.modal", function () {
                                $("#add_url").focus();
                            });
                        } else {
                            alert(data.msg);
                        }
                    },
                    error: function (jqXHR, textStatus, errorThrown) {
                        var status = jqXHR.status;//404,500等
                        var text = jqXHR.statusText;//404对应的Not found,500对应的error
                        alert("获取权限信息失败：" + status + "  " + text);
                        $("#div_loading").css("display", "none");
                    }
                });
            }
        })
    }
    //添加同级权限按钮
    var btn2 = $("#diyBtn2_"+treeNode.id);
    if (btn2){
        btn2.bind("click", function() {
            //先看是否有权限
            if (addData == false) {
                alert("没有添加权限");
            } else {
                $("#add_url2").val("");
                $("#add_permissionName2").val("");
                //根据权限获取父节点
                $.ajax({
                    url: 'permissionByName',
                    type: 'post',
                    data: {'permissionName': treeNode.name},
                    success: function (data) {
                        $("#div_loading").css("display", "none");
                        if (data.code == 0) {
                            //添加同级权限时，父id为当前权限的pid
                            $("#add_pid").val(data.data.pid);
                            $("#add_titel").html("添加同级权限");
                            //显示添加模态框
                            $("#addModal").modal("show");
                            $("#addModal").on("shown.bs.modal", function () {
                                $("#add_url").focus();
                            });
                        } else {
                            alert(data.msg);
                        }
                    },
                    error: function (jqXHR, textStatus, errorThrown) {
                        var status = jqXHR.status;//404,500等
                        var text = jqXHR.statusText;//404对应的Not found,500对应的error
                        alert("获取权限信息失败：" + status + "  " + text);
                        $("#div_loading").css("display", "none");
                    }
                });
            }
        })
    }
}

//鼠标从节点移除时，去掉自定义的按钮
function removeHoverDom(treeId, treeNode) {
    $("#diyBtn_"+treeNode.id).unbind().remove();
    $("#diyBtn2_"+treeNode.id).unbind().remove();
    $("#diyBtn_space_" +treeNode.id).unbind().remove();
    $("#diyBtn_space2_" +treeNode.id).unbind().remove();
};

//------------------添加------------------
function add() {
    var pid=$("#add_pid").val();
    var url=$("#add_url").val();
    var permissionName=$("#add_permissionName").val();
    if(url==""){
        $("#add_url").focus();
    }else if(permissionName==""){
        $("#add_permissionName").focus();
    }else {
        $("#div_loading").css("display", "block");
        $.ajax({
            url: 'permissionAdd',
            type: 'post',
            data: {'permissionName': permissionName,'url':url,'pid':pid},
            success: function (data) {
                $("#div_loading").css("display", "none");
                if (data.code == 0) {
                    window.location.reload();
                } else {
                    alert(data.msg);
                }
            },
            error: function (jqXHR, textStatus, errorThrown) {
                var status = jqXHR.status;//404,500等
                var text = jqXHR.statusText;//404对应的Not found,500对应的error
                alert("添加失败：" + status + "  " + text);
                $("#div_loading").css("display", "none");
            }
        });
    }
}
//----------------------删除--------------------
//删除权限
function deletePermission(treeId, treeNode) {
    //判断是否有删除的权限
    if(deleteData==false){
        alert("无删除权限");
        return false;
    }else {
        //有删除权限，则后台删除
        $("#div_loading").css("display","block");
        //ajax删除
        $.ajax({
            url: 'permissionDelete',
            type: 'post',
            data: {'permissionName': treeNode.name},
            success: function (data) {
                $("#div_loading").css("display","none");
                if (data.code==0) {
                    //删除成功后。返回真，更新ztree树
                    return true;
                } else {
                    alert(data.msg);
                    return false;
                }
            },
            error:function(jqXHR, textStatus, errorThrown){
                var status = jqXHR.status;//404,500等
                var text = jqXHR.statusText;//404对应的Not found,500对应的error
                alert("删除失败：" + status + "  " + text);
                $("#div_loading").css("display","none");
                return false;
            }
        });
    }
}

//-----------------------修改--------------------
//准备修改
function toEditPermission(treeId, treeNode) {
    //看是否有修改权限
    if(updateData==false){
        alert("无修改权限");
        return false;
    }else {
        //有修改权限，则根据权限名称查询权限信息，放入修改模态框
        $.ajax({
            url: 'permissionByName',
            type: 'post',
            data: {'permissionName': treeNode.name},
            success: function (data) {
                $("#div_loading").css("display","none");
                if (data.code==0) {
                    $("#update_id").val(data.data.id);
                    $("#update_url").val(data.data.url);
                    $("#update_permissionName").val(data.data.permissionname);
                    //打开编辑模态框
                    $("#updateModal").modal("show");
                    $("#updateModal").on("shown.bs.modal",function () {
                        $("#update_url").focus();
                    });
                    //有信息，则返回真，运行编辑节点
                    return true;
                } else {
                    //有错误，则返回false,不允许编辑节点
                    alert(data.msg);
                    return false;
                }
            },
            error:function(jqXHR, textStatus, errorThrown){
                var status = jqXHR.status;//404,500等
                var text = jqXHR.statusText;//404对应的Not found,500对应的error
                alert("获取权限信息失败：" + status + "  " + text);
                $("#div_loading").css("display","none");
                return false;
            }
        });
    }
}

//修改传入后台
function updatePermission() {
    var id=$("#update_id").val();
    var url=$("#update_url").val();
    var permissionName=$("#update_permissionName").val();
    if(url==""){
        $("#update_url").focus();
    }else if(permissionName==""){
        $("#update_permissionName").focus()
    }else {
        $.ajax({
            url: 'permissionUpdate',
            type: 'post',
            data: {'id':id,'url':url,'permissionName': permissionName},
            success: function (data) {
                $("#div_loading").css("display","none");
                if (data.code==0) {
                    window.location.reload();
                } else {
                    alert(data.msg);
                }
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

//------------------------------------查询记录-------------------------------
function findAll() {
    //显示加载提示信息
    $("#div_loading").css("display", "block");
    //先清除旧数据
    $("#ur_zTree").html("");
    //后台查询
    $.ajax({
        url: 'permissionZTree',
        type: 'post',
        dataType:'json',
        success: function (data) {
            if(data.code==0){
                zTree=$.fn.zTree.init($("#ur_zTree"), setting, data.data);
                //展开全部节点
                zTree.expandAll(true);
            }else {
                alert(data.msg);
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

