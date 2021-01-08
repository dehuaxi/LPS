
//-------------------------------页面加载执行--------------------------------------
$(document).ready(function () {
    $("#supplierModal").modal("show");
    $("#supplierModal").on("shown.bs.modal",function () {
        $("#input_supplier").focus();
    })

    //供应商名称输入框键盘事件
    $("#input_supplier").keyup(function (event) {
        findSupplier();
    })

    //全选按钮
    $("#allCheckBox").click(function(){
        if($(this).is(':checked')){
            $("#table_billCache").find("input").each(function(){
                $(this).prop("checked",true);
            })
        }else{
            $("#table_billCache").find("input").each(function(){
                $(this).prop("checked",false);
            })
        }
    })

    //加载上传模态框的工厂
    $.ajax({
        url: 'currentFactory',
        type: 'post',
        dataType:'json',
        success: function (data) {
            if (data.code==0) {
                for(var i=0;i<data.data.length;i++){
                    $("#select_factoryId").append("<option value='"+data.data[i].id+"'>"+data.data[i].factoryname+"</option>")
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
})

//-----------------------获取供应商信息-----------------------------
//获取供应商信息
function findSupplier(){
    var supplierName=$("#input_supplier").val().replace(" ","").replace("'","");
    if(supplierName==""){
        $("#ul_supplier").css("display","none");
        $("#ul_supplier").html("");
        $("#input_supplier").focus();
    }else{
        //清除列表内容
        $("#ul_supplier").html("");
        $.ajax({
            url:'supplierLikeName',
            type:'post',
            data:{'supplierName':supplierName},
            dataType:'json',
            success:function(data){
                if(data.code==0){
                    if(data.data!=null){
                        for(var k=0;k<data.data.length;k++){
                            $("#ul_supplier").append("<li><a href='#' onclick='chooseSupplier(this)'>"+data.data[k].suppliercode+":"+data.data[k].suppliername+"</a></li>");
                        }
                        $("#ul_supplier").css("display","block");
                    }else{
                        $("#ul_supplier").css("display","none");
                        $("#input_supplier").focus();
                    }
                }else {
                    alert(data.msg);
                }
            },
            error:function(jqXHR, textStatus, errorThrown){
                var status = jqXHR.status;//404,500等
                var text = jqXHR.statusText;//404对应的Not found,500对应的error
                alert("查询失败：" + status + "  " + text);
                $("#ul_supplier").css("display","none");
                $("#input_supplier").focus();
            }
        });
    }
}

//选择供应商
function chooseSupplier(a) {
    //显示加载提示信息
    $("#div_loading").css("display", "block");
    var s=a.innerHTML;
    var supplierName=s.split(":")[1];
    var supplierCode=s.split(":")[0];
    $("#span_supplierCode").html(supplierCode);
    $("#span_supplierName").html(supplierName);
    //加载供应商对应的未取货的计划
    $.ajax({
        url:'untakePlanCacheBySupplierCode',
        type:'post',
        data:{'supplierCode':supplierCode},
        dataType:'json',
        success:function(data){
            if(data.code==0){
                //加载未取货计划
                for(var i=0;i<data.data.length;i++){
                    var goodcode="";
                    var goodname="";
                    var factoryname="";
                    var routename="";
                    if(data.data[i].good!=null){
                        goodcode= data.data[i].good.goodcode;
                        goodname=data.data[i].good.goodname;
                        if(data.data[i].good.factory!=null){
                            factoryname=data.data[i].good.factory.factoryname;
                        }
                    }
                    if(data.data[i].route!=null){
                        routename=data.data[i].route.routename;
                    }
                    var str= "<tr><td style='display: none'>" + data.data[i].id + "</td>"+
                        "<td>" + goodcode +"</td>"+
                        "<td>" + goodname +"</td>"+
                        "<td>" + data.data[i].count +"</td>"+
                        "<td>" + data.data[i].takecount +"</td>"+
                        "<td>" + data.data[i].date +"</td>"+
                        "<td>" + routename +"</td>"+
                        "<td>" + factoryname +"</td><td><button type='button' class='btn btn-xs btn-warning' onclick='loadBill(this)'>选择PD单</button></td></tr>";
                    $("#table_data").append(str);
                }
                $("#supplierModal").modal("hide");
                $("#div_loading").css("display", "none");
            }else {
                alert(data.msg);
                window.location.reload();
            }
        },
        error:function(jqXHR, textStatus, errorThrown){
            var status = jqXHR.status;//404,500等
            var text = jqXHR.statusText;//404对应的Not found,500对应的error
            alert("查询失败：" + status + "  " + text);
            window.location.reload();
        }
    });
}

//点击表格中复选框所在的单元格
function chooseSelf(td) {
    var checkbox=$(td).find("input");
    if($(checkbox).is(":checked")){
        $(checkbox).prop("checked",false);
    }else {
        $(checkbox).prop("checked",true);
    }
}

//点击复选框自己
function checkboxSelf(checkbox) {
    if($(checkbox).is(":checked")){
        $(checkbox).prop("checked",false);
    }else {
        $(checkbox).prop("checked",true);
    }
}

//加载计划对应的PD单
function loadBill(td) {
    //获取计划id
    var planCacheId=$(td.parentNode.parentNode.childNodes[0]).text();
    //根据计划id获取未绑定计划的PD单记录
    $("#div_loading").css("display", "block");
    $("#table_billCache").html("");
    $.ajax({
        url:'billCacheByPlancacheid',
        type:'post',
        data:{'planCacheId':planCacheId},
        dataType:'json',
        success:function(data){
            if(data.code==0){
                $("#input_planCacheId").val(planCacheId);
                //加载未取货计划
                for(var i=0;i<data.data.length;i++){
                    var goodcode="";
                    var goodname="";
                    if(data.data[i].good!=null){
                        goodcode= data.data[i].good.goodcode;
                        goodname=data.data[i].good.goodname;
                    }
                    var str= "<tr><td style='display: none'>" + data.data[i].id + "</td>"+
                        "</td><td onclick='chooseSelf(this)'><input type='checkbox' name='sort' onclick='checkboxSelf(this)'>"+
                        "<td>" + data.data[i].billnumber +"</td>"+
                        "<td>" + goodcode +"</td>"+
                        "<td>" + goodname +"</td>"+
                        "<td>" + data.data[i].count +"</td>"+
                        "<td>" + data.data[i].batch +"</td></tr>";
                    $("#table_billCache").append(str);
                }
                $("#billModal").modal("show");
            }else {
                alert(data.msg);
            }
            $("#div_loading").css("display", "none");
        },
        error:function(jqXHR, textStatus, errorThrown){
            var status = jqXHR.status;//404,500等
            var text = jqXHR.statusText;//404对应的Not found,500对应的error
            alert("查询失败：" + status + "  " + text);
            $("#div_loading").css("display", "none");
        }
    });
}

//选择PD单后，绑定计划
function bind() {
    //获取所有被选中的PD单记录
    var billCacheIds="";
    var checkBoxList=document.getElementsByName("sort");
    for(k in checkBoxList){
        if(checkBoxList[k].checked){
            billCacheIds+=","+checkBoxList[k].parentNode.parentNode.childNodes[0].innerHTML;
        }
    }
    if(billCacheIds!=""){
        $("#div_loading").css("display", "block");
        billCacheIds=billCacheIds.substring(1);
        var planCacheId=$("#input_planCacheId").val();
        $.ajax({
            url:'billCacheUpdate',
            type:'post',
            data:{'planCacheId':planCacheId,'billCacheIds':billCacheIds},
            dataType:'json',
            success:function(data){
                if(data.code==0){
                    //加载供应商对应的未取货的计划
                    $("#table_data").html("");
                    var supplierCode=$("#span_supplierCode").html();
                    $.ajax({
                        url:'untakePlanCacheBySupplierCode',
                        type:'post',
                        data:{'supplierCode':supplierCode},
                        dataType:'json',
                        success:function(data){
                            if(data.code==0){
                                //加载未取货计划
                                for(var i=0;i<data.data.length;i++){
                                    var goodcode="";
                                    var goodname="";
                                    var factoryname="";
                                    var routename="";
                                    if(data.data[i].good!=null){
                                        goodcode= data.data[i].good.goodcode;
                                        goodname=data.data[i].good.goodname;
                                        if(data.data[i].good.factory!=null){
                                            factoryname=data.data[i].good.factory.factoryname;
                                        }
                                    }
                                    if(data.data[i].route!=null){
                                        routename=data.data[i].route.routename;
                                    }
                                    var str= "<tr><td style='display: none'>" + data.data[i].id + "</td>"+
                                        "<td>" + goodcode +"</td>"+
                                        "<td>" + goodname +"</td>"+
                                        "<td>" + data.data[i].count +"</td>"+
                                        "<td>" + data.data[i].takecount +"</td>"+
                                        "<td>" + data.data[i].date +"</td>"+
                                        "<td>" + routename +"</td>"+
                                        "<td>" + factoryname +"</td><td><button type='button' class='btn btn-xs btn-warning' onclick='loadBill(this)'>选择PD单</button></td></tr>";
                                    $("#table_data").append(str);
                                }
                            }else {
                                alert(data.msg);
                            }
                        },
                        error:function(jqXHR, textStatus, errorThrown){
                            var status = jqXHR.status;//404,500等
                            var text = jqXHR.statusText;//404对应的Not found,500对应的error
                            alert("查询失败：" + status + "  " + text);
                        }
                    });
                    //隐藏pd单模态框
                    $("#billModal").modal("hide");
                }else {
                    alert(data.msg);
                }
                $("#div_loading").css("display", "none");
            },
            error:function(jqXHR, textStatus, errorThrown){
                var status = jqXHR.status;//404,500等
                var text = jqXHR.statusText;//404对应的Not found,500对应的error
                alert("绑定失败：" + status + "  " + text);
                $("#div_loading").css("display", "none");
            }
        });
    }
}
//------------------------------------PD单上传-----------------------------
function toUpload() {
    $("#uploadFile").val("");
    $("#table_upload").html("");
    $("#uploadModal").modal("show");
}

function upload() {
//获取选择的文件值
    var file = $("#uploadFile").val();
    var factoryId=$("#select_factoryId").val();
    if (file == "") {
        alert("选择上传的文件")
    }else if(factoryId==0){
        alert("选择工厂")
    }else {
        //显示加载提示信息
        $("#div_loading").css("display", "block");
        //获取表单中的文件
        var formData = new FormData($('#uploadForm')[0]);
        formData.append("factoryId",factoryId);
        var supplierCode=$("#span_supplierCode").html();
        formData.append("supplierCode",supplierCode);
        //清空表格
        $("#table_upload").html("");
        $("#uploadFile").val("");
        //ajax请求
        $.ajax({
            url: "billCacheAdd",//后台的接口地址
            type: "post",//post请求方式
            data: formData,//参数
            cache: false,//无缓存
            processData: false,//必须false才会避开jQuery对 formdata 的默认处理
            contentType: false,//必须false才会自动加上正确的Content-Type
            success: function (data) {
                if(data.code==0){
                    //结果放入表格
                    for (var i = 0; i < data.data.length; i++) {
                        //如果code0表示成功，字体用绿色，如果为1表示失败，字体用红色
                        var str="";
                        if(data.data[i].code==0){
                            str="<tr style='color: #00B83F'>";
                        }else{
                            str="<tr style='color: #ff0e11'>";
                        }
                        str+="<td>"+data.data[i].fileName+"</td>"+
                            "<td>"+data.data[i].message+"</td>"+
                            "<td>"+data.data[i].time+"</td></tr>";
                        $("#table_upload").append(str);
                    }
                }else {
                    alert(data.msg);
                }
                //隐藏加载提示信息
                $("#div_loading").css("display", "none");
            },
            error: function (jqXHR, textStatus, errorThrown) {
                var status = jqXHR.status;//404,500等
                var text = jqXHR.statusText;//404对应的Not found,500对应的error
                alert("上传失败：" + status + "  " + text);
                //隐藏加载提示
                $("#div_loading").css("display", "none");
            }
        });
    }
}
