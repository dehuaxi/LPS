
//-------------------------------页面加载执行--------------------------------------
$(document).ready(function () {
    //加载查询栏的工厂
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

    //查询
    findByLimit(1);

})

//-----------------------获取供应商信息-----------------------------
//根据工厂id加载供应商信息下拉框。参数1：工厂下拉框id  参数2：供应商下拉框的id  参数3：需要被选中的供应商id
function loadSupplier(factorySelectId,supplierSelectId,supplierId){
    var factoryId=$("#"+factorySelectId).val();
    $("#"+supplierSelectId).html("");
    $("#"+supplierSelectId).append("<option value='0'>选择供应商</option>")
    if(factoryId!=0){
        $.ajax({
            url:'supplierByFactoryid',
            type:'post',
            data:{'factoryId':factoryId},
            dataType:'json',
            success:function(data){
                if(data.data!=null){
                    for(var k=0;k<data.data.length;k++){
                        if(supplierId==data.data.id){
                            $("#"+supplierSelectId).append("<option value='"+data.data[k].id+"' selected='selected'>"+data.data[k].suppliercode+":"+data.data[k].suppliername+"</option>");
                        }else {
                            $("#"+supplierSelectId).append("<option value='"+data.data[k].id+"'>"+data.data[k].suppliercode+":"+data.data[k].suppliername+"</option>");
                        }
                    }
                }
            },
            error:function(jqXHR, textStatus, errorThrown){
                var status = jqXHR.status;//404,500等
                var text = jqXHR.statusText;//404对应的Not found,500对应的error
                alert("加载供应商失败：" + status + "  " + text);
            }
        });
    }
}

//----------------------------------------添加-----------------------------------
//去添加函数,打开添加的模态框
function toAdd() {
    //清除输入框内容
    $("#add_goodName").val("");//名称
    $("#add_goodCode").val("");//编号
    //加载添加模态框的工厂
    $("#add_factoryId").html("");
    $("#add_factoryId").append("<option value='0'>选择工厂</option>");
    $.ajax({
        url: 'currentFactory',
        type: 'post',
        dataType:'json',
        success: function (data) {
            if (data.code==0) {
                for(var i=0;i<data.data.length;i++){
                    $("#add_factoryId").append("<option value='"+data.data[i].id+"'>"+data.data[i].factoryname+"</option>")
                }
                $("#add_supplierId").html("");
                $("#add_supplierId").append("<option value='0'>选择供应商</option>")
                $("#add_oneBoxCount").val("");
                $("#add_binCount").val("");
                $("#add_oneCarCount").val("");
                $("#add_maxStock").val("");
                $("#add_triggerStock").val("");
                $("#add_quotaRatio").val("");
                $("#add_boxType").val("");
                $("#add_boxLength").val("");
                $("#add_boxWidth").val("");
                $("#add_boxHeight").val("");
                $("#add_packboxLength").val("");
                $("#add_packboxWidth").val("");
                $("#add_packboxHeight").val("");
                $("#add_packboxWeight").val("");
                $("#add_boxWeight").val("");
                $("#add_returnRatio").val("");
                $("#add_oneTrayBoxCount").val("");
                $("#add_oneTrayLayersCount").val("");
                $("#add_trayRatio").val("");
                $("#add_trayLength").val("");
                $("#add_trayWidth").val("");
                $("#add_trayHeight").val("");
                $("#add_packRemarks").val("");
                $("#add_receiver").val("");
                ///打开模态框
                $("#addModal").modal("show");
                //模态框打开事件
                $("#addModal").on("shown.bs.modal", function () {
                    $("#add_goodCode").focus();
                })
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
}

function add() {
    //获取参数
    var goodName=$("#add_goodName").val();//名称
    var goodCode=$("#add_goodCode").val();//编号
    var supplierId=$("#add_supplierId").val();//名称
    var factoryId=$("#add_factoryId").val();
    var oneBoxCount=$("#add_oneBoxCount").val();
    var binCount=$("#add_binCount").val();
    var oneCarCount=$("#add_oneCarCount").val();
    var maxStock=$("#add_maxStock").val();
    var triggerStock=$("#add_triggerStock").val();
    var quotaRatio=$("#add_quotaRatio").val();
    var boxType=$("#add_boxType").val();
    var boxLength=$("#add_boxLength").val();
    var boxWidth=$("#add_boxWidth").val();
    var boxHeight=$("#add_boxHeight").val();
    var packBoxLength=$("#add_packBoxLength").val();
    var packBoxWidth=$("#add_packBoxWidth").val();
    var packBoxHeight=$("#add_packBoxHeight").val();
    var packBoxWeight=$("#add_packBoxWeight").val();
    var boxWeight=$("#add_boxWeight").val();
    var returnRatio=$("#add_returnRatio").val();
    var oneTrayBoxCount=$("#add_oneTrayBoxCount").val();
    var oneTrayLayersCount=$("#add_oneTrayLayersCount").val();
    var trayRatio=$("#add_trayRatio").val();
    var trayLength=$("#add_trayLength").val();
    var trayWidth=$("#add_trayWidth").val();
    var trayHeight=$("#add_trayHeight").val();
    var packRemarks=$("#add_packRemarks").val();
    var receiver=$("#add_receiver").val();
    //正整数
    var str=/[1-9]{1}[0-9]{0,10}/;
    //正整数或0
    var str1=/([1-9]{1}[0-9]{0,10})|0/;
    //2位小数点
    var str2=/(([1-9]{1}[0-9]{0,6})|([0]{1}))([.]\d{1,2})?/;
    //判断条件
    if (goodName == "") {
        $("#add_goodName").focus();
    }else if (goodCode == "") {
        $("#add_goodCode").focus();
    }else if(factoryId=="0"){
        alert("请选择工厂")
    }else if (supplierId == "0") {
        alert("请选择供应商")
    }else if(!str.test(oneBoxCount)){
        $("#add_oneBoxCount").focus();
    }else if(!str1.test(binCount)){
        $("#add_binCount").focus();
    }else if(!str.test(oneCarCount)){
        $("#add_oneCarCount").focus();
    }else if(!str.test(maxStock)){
        $("#add_maxStock").focus();
    }else if(!str.test(triggerStock)){
        $("#add_triggerStock").focus();
    }else if(!str1.test(quotaRatio)){
        $("#add_quotaRatio").focus();
    }else if(boxType==""){
        $("#add_boxType").focus();
    }else if(!str.test(boxLength)){
        $("#add_boxLength").focus();
    }else if(!str.test(boxWidth)){
        $("#add_boxWidth").focus();
    }else if(!str.test(boxHeight)){
        $("#add_boxHeight").focus();
    }else if(!str1.test(packBoxLength)){
        $("#add_packBoxLength").focus();
    }else if(!str1.test(packBoxWidth)){
        $("#add_packBoxWidth").focus();
    }else if(!str1.test(packBoxHeight)){
        $("#add_packBoxHeight").focus();
    }else if(!str2.test(packBoxWeight)){
        $("#add_packBoxWeight").focus();
    }else if(!str2.test(boxWeight)){
        $("#add_boxWeight").focus();
    }else if(parseFloat(boxWeight)==0){
        $("#add_boxWeight").focus();
    }else if(!str1.test(returnRatio)){
        $("#add_returnRatio").focus();
    }else if(!str1.test(oneTrayBoxCount)){
        $("#add_oneTrayBoxCount").focus();
    }else if(!str1.test(oneTrayLayersCount)){
        $("#add_oneTrayLayersCount").focus();
    }else if(!str1.test(trayRatio)){
        $("#add_trayRatio").focus();
    }else if(!str1.test(trayLength)){
        $("#add_trayLength").focus();
    }else if(!str1.test(trayWidth)){
        $("#add_trayWidth").focus();
    }else if(!str1.test(trayHeight)){
        $("#add_trayHeight").focus();
    }else if(receiver==""){
        $("#add_receiver").focus();
    }else{
        var a=false;
        if(oneTrayBoxCount=="0"){
            if(oneTrayLayersCount!="0"){
                alert("当单托箱数为0时，单托层数必须为0");
                $("#add_oneTrayLayersCount").focus();
            }else if(trayLength!="0"){
                alert("当单托箱数为0时，托盘长必须为0");
                $("#add_trayLength").focus();
            }else if(trayWidth!="0"){
                alert("当单托箱数为0时，托盘宽必须为0");
                $("#add_trayWidth").focus();
            }else if(trayHeight!="0"){
                alert("当单托箱数为0时，托盘高必须为0");
                $("#add_trayHeight").focus();
            }else {
                a=true;
            }
        }else {
            if(oneTrayLayersCount=="0"){
                alert("当单托箱数不为0时，单托层数也不能为0");
                $("#add_oneTrayLayersCount").focus();
            }else if(trayLength=="0"){
                alert("当单托箱数不为0时，托盘长不能为0");
                $("#add_trayLength").focus();
            }else if(trayWidth=="0"){
                alert("当单托箱数不为0时，托盘宽不能为0");
                $("#add_trayWidth").focus();
            }else if(trayHeight=="0"){
                alert("当单托箱数不为0时，托盘高不能为0");
                $("#add_trayHeight").focus();
            }else{
                a=true;
            }
        }
        if(a){
            $("#div_loading").css("display","block");
            $.ajax({
                url: 'goodAdd',
                type: 'post',
                data: {
                    'goodName':goodName,
                    'goodCode':goodCode,
                    'supplierId':supplierId,
                    'factoryId':factoryId,
                    'oneBoxCount':oneBoxCount,
                    'binCount':binCount,
                    'oneCarCount':oneCarCount,
                    'maxStock':maxStock,
                    'triggerStock':triggerStock,
                    'quotaRatio':quotaRatio,
                    'boxType':boxType,
                    'boxLength':boxLength,
                    'boxWidth':boxWidth,
                    'boxHeight':boxHeight,
                    'packBoxLength':packBoxLength,
                    'packBoxWidth':packBoxWidth,
                    'packBoxHeight':packBoxHeight,
                    'packBoxWeight':packBoxWeight,
                    'boxWeight':boxWeight,
                    'returnRatio':returnRatio,
                    'oneTrayBoxCount':oneTrayBoxCount,
                    'oneTrayLayersCount':oneTrayLayersCount,
                    'trayRatio':trayRatio,
                    'trayLength':trayLength,
                    'trayWidth':trayWidth,
                    'trayHeight':trayHeight,
                    'packRemarks':packRemarks,
                    'receiver':receiver},
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
}

//----------------------------------------批量添加/修改-----------------------------------
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
            url: "goodAddUpload",
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
        url: 'goodById',
        type: 'post',
        dataType:'json',
        data:{'id':id},
        success: function (data) {
            if (data.code==0) {
                $("#update_id").val(id);
                $("#update_goodCode").val(data.data.goodcode);
                $("#update_goodName").val(data.data.goodname);
                $("#update_oneBoxCount").val(data.data.oneboxcount);
                $("#update_binCount").val(data.data.bincount);
                $("#update_oneCarCount").val(data.data.onecarcount);
                $("#update_maxStock").val(data.data.maxstock);
                $("#update_triggerStock").val(data.data.triggerstock);
                $("#update_quotaRatio").val(data.data.quotaratio);
                $("#update_boxType").val(data.data.boxtype);
                $("#update_boxLength").val(data.data.boxlength);
                $("#update_boxWidth").val(data.data.boxwidth);
                $("#update_boxHeight").val(data.data.boxheight);
                $("#update_packBoxLength").val(data.data.packboxlength);
                $("#update_packBoxWidth").val(data.data.packboxwidth);
                $("#update_packBoxHeight").val(data.data.packboxheight);
                $("#update_packBoxWeight").val(data.data.packboxweight);
                $("#update_boxWeight").val(data.data.boxweight);
                $("#update_returnRatio").val(data.data.returnratio);
                $("#update_oneTrayBoxCount").val(data.data.onetrayboxcount);
                $("#update_oneTrayLayersCount").val(data.data.onetraylayerscount);
                $("#update_trayRatio").val(data.data.trayratio);
                $("#update_trayLength").val(data.data.traylength);
                $("#update_trayWidth").val(data.data.traywidth);
                $("#update_trayHeight").val(data.data.trayheight);
                $("#update_packRemarks").val(data.data.packremarks);
                $("#update_receiver").val(data.data.receiver);
                //供应商id
                var supplierId=data.data.supplier.id;
                //加载工厂
                var factoryId=data.data.supplier.route.factory.id;
                $("#update_factoryId").html("");
                $("#update_factoryId").append("<option value='0'>选择工厂</option>")
                $.ajax({
                    url: 'currentFactory',
                    type: 'post',
                    dataType:'json',
                    success: function (data) {
                        if (data.code==0) {
                            for(var i=0;i<data.data.length;i++){
                                if(factoryId==data.data[i].id){
                                    $("#update_factoryId").append("<option value='"+data.data[i].id+"' selected='selected'>"+data.data[i].factoryname+"</option>")
                                }else {
                                    $("#update_factoryId").append("<option value='"+data.data[i].id+"'>"+data.data[i].factoryname+"</option>")
                                }
                            }
                            //加载供应商信息
                            $("#update_supplierId").html("");
                            $("#update_supplierId").append("<option value='0'>选择供应商</option>")
                            $.ajax({
                                url:'supplierByFactoryid',
                                type:'post',
                                data:{'factoryId':factoryId},
                                dataType:'json',
                                success:function(data){
                                    if(data.data!=null){
                                        for(var k=0;k<data.data.length;k++){
                                            if(supplierId==data.data[k].id){
                                                $("#update_supplierId").append("<option value='"+data.data[k].id+"' selected='selected'>"+data.data[k].suppliercode+":"+data.data[k].suppliername+"</option>");
                                            }else {
                                                $("#update_supplierId").append("<option value='"+data.data[k].id+"'>"+data.data[k].suppliercode+":"+data.data[k].suppliername+"</option>");
                                            }
                                        }
                                    }
                                    //打开模态框
                                    $("#updateModal").modal("show");
                                    //模态框打开事件
                                    $("#updateModal").on("shown.bs.modal", function () {
                                        //角色名称输入框获取焦点
                                        $("#update_goodCode").focus();
                                    })
                                },
                                error:function(jqXHR, textStatus, errorThrown){
                                    var status = jqXHR.status;//404,500等
                                    var text = jqXHR.statusText;//404对应的Not found,500对应的error
                                    alert("加载供应商失败：" + status + "  " + text);
                                }
                            });
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
            } else {
                alert(data.msg);
            }
        },
        error:function(jqXHR, textStatus, errorThrown){
            var status = jqXHR.status;//404,500等
            var text = jqXHR.statusText;//404对应的Not found,500对应的error
            alert("加载物料信息失败：" + status + "  " + text);
        }
    });
}

//修改按钮
function update() {
    //获取参数
    var id = $("#update_id").val();//id
    var goodName=$("#update_goodName").val();
    var goodCode=$("#update_goodCode").val();
    var supplierId = $("#update_supplierId").val();
    var factoryId=$("#update_factoryId").val();
    var oneBoxCount=$("#update_oneBoxCount").val();
    var binCount=$("#update_binCount").val();
    var oneCarCount=$("#update_oneCarCount").val();
    var maxStock=$("#update_maxStock").val();
    var triggerStock=$("#update_triggerStock").val();
    var quotaRatio=$("#update_quotaRatio").val();
    var boxType=$("#update_boxType").val();
    var boxLength=$("#update_boxLength").val();
    var boxWidth=$("#update_boxWidth").val();
    var boxHeight=$("#update_boxHeight").val();
    var packBoxLength=$("#update_packBoxLength").val();
    var packBoxWidth=$("#update_packBoxWidth").val();
    var packBoxHeight=$("#update_packBoxHeight").val();
    var packBoxWeight=$("#update_packBoxWeight").val();
    var boxWeight=$("#update_boxWeight").val();
    var returnRatio=$("#update_returnRatio").val();
    var oneTrayBoxCount=$("#update_oneTrayBoxCount").val();
    var oneTrayLayersCount=$("#update_oneTrayLayersCount").val();
    var trayRatio=$("#update_trayRatio").val();
    var trayLength=$("#update_trayLength").val();
    var trayWidth=$("#update_trayWidth").val();
    var trayHeight=$("#update_trayHeight").val();
    var packRemarks=$("#update_packRemarks").val();
    var receiver=$("#update_receiver").val();
    //正整数
    var str=/[1-9]{1}[0-9]{0,10}/;
    //正整数或0
    var str1=/([1-9]{1}[0-9]{0,10})|0/;
    //2位小数点
    var str2=/(([1-9]{1}[0-9]{0,6})|([0]{1}))([.]\d{1,2})?/;
    if (goodName == "") {
        $("#update_goodName").focus();
    } else if (goodCode == "") {
        $("#update_goodCode").focus();
    }else if(supplierId=="0"){
        alert("请选择供应商")
    }else if(factoryId=="0"){
        alert("请选择工厂")
    }else if(!str.test(oneBoxCount)){
        $("#update_oneBoxCount").focus();
    }else if(!str1.test(binCount)){
        $("#update_binCount").focus();
    }else if(!str.test(oneCarCount)){
        $("#update_oneCarCount").focus();
    }else if(!str.test(maxStock)){
        $("#update_maxStock").focus();
    }else if(!str.test(triggerStock)){
        $("#update_triggerStock").focus();
    }else if(!str1.test(quotaRatio)){
        $("#update_quotaRatio").focus();
    }else if(boxType==""){
        $("#update_boxType").focus();
    }else if(!str.test(boxLength)){
        $("#update_boxLength").focus();
    }else if(!str.test(boxWidth)){
        $("#update_boxWidth").focus();
    }else if(!str.test(boxHeight)){
        $("#update_boxHeight").focus();
    }else if(!str1.test(packBoxLength)){
        $("#update_packBoxLength").focus();
    }else if(!str1.test(packBoxWidth)){
        $("#update_packBoxWidth").focus();
    }else if(!str1.test(packBoxHeight)){
        $("#update_packBoxHeight").focus();
    }else if(!str2.test(packBoxWeight)){
        $("#update_packBoxWidth").focus();
    }else if(!str2.test(boxWeight)){
        $("#update_boxWidth").focus();
    }else if(parseFloat(boxWeight)==0){
        $("#update_boxWidth").focus();
    }else if(!str1.test(returnRatio)){
        $("#update_returnRatio").focus();
    }else if(!str1.test(oneTrayBoxCount)){
        $("#update_oneTrayBoxCount").focus();
    }else if(!str1.test(oneTrayLayersCount)){
        $("#update_oneTrayLayersCount").focus();
    }else if(!str1.test(trayRatio)){
        $("#update_trayRatio").focus();
    }else if(!str1.test(trayLength)){
        $("#add_trayLength").focus();
    }else if(!str1.test(trayWidth)){
        $("#add_trayWidth").focus();
    }else if(!str1.test(trayHeight)){
        $("#add_trayHeight").focus();
    }else if(receiver==""){
        $("#update_receiver").focus();
    }else{
        var a=false;
        if(oneTrayBoxCount=="0"){
            if(oneTrayLayersCount!="0"){
                alert("当单托箱数为0时，单托层数必须为0");
                $("#add_oneTrayLayersCount").focus();
            }else if(trayLength!="0"){
                alert("当单托箱数为0时，托盘长必须为0");
                $("#add_trayLength").focus();
            }else if(trayWidth!="0"){
                alert("当单托箱数为0时，托盘宽必须为0");
                $("#add_trayWidth").focus();
            }else if(trayHeight!="0"){
                alert("当单托箱数为0时，托盘高必须为0");
                $("#add_trayHeight").focus();
            }else {
                a=true;
            }
        }else {
            if(oneTrayLayersCount=="0"){
                alert("当单托箱数不为0时，单托层数也不能为0");
                $("#add_oneTrayLayersCount").focus();
            }else if(trayLength=="0"){
                alert("当单托箱数不为0时，托盘长不能为0");
                $("#add_trayLength").focus();
            }else if(trayWidth=="0"){
                alert("当单托箱数不为0时，托盘宽不能为0");
                $("#add_trayWidth").focus();
            }else if(trayHeight=="0"){
                alert("当单托箱数不为0时，托盘高不能为0");
                $("#add_trayHeight").focus();
            }else{
                a=true;
            }
        }
        if(a){
            $("#div_loading").css("display","block");
            //提交后台
            $.ajax({
                url: 'goodUpdate',
                type: 'post',
                data: {'id': id,
                    'goodName':goodName,
                    'goodCode':goodCode,
                    'supplierId':supplierId,
                    'factoryId':factoryId,
                    'oneBoxCount':oneBoxCount,
                    'binCount':binCount,
                    'oneCarCount':oneCarCount,
                    'maxStock':maxStock,
                    'triggerStock':triggerStock,
                    'quotaRatio':quotaRatio,
                    'boxType':boxType,
                    'boxLength':boxLength,
                    'boxWidth':boxWidth,
                    'boxHeight':boxHeight,
                    'packBoxLength':packBoxLength,
                    'packBoxWidth':packBoxWidth,
                    'packBoxHeight':packBoxHeight,
                    'packBoxWeight':packBoxWeight,
                    'boxWeight':boxWeight,
                    'returnRatio':returnRatio,
                    'oneTrayBoxCount':oneTrayBoxCount,
                    'oneTrayLayersCount':oneTrayLayersCount,
                    'trayRatio':trayRatio,
                    'trayLength':trayLength,
                    'trayWidth':trayWidth,
                    'trayHeight':trayHeight,
                    'packRemarks':packRemarks,
                    'receiver':receiver},
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
            url: 'goodDelete',
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
    var goodCode=$("#input_goodCode").val();
    var goodName=$("#input_goodName").val();
    var supplierCode=$("#input_supplierCode").val();
    var supplierName=$("#input_supplierName").val();
    var boxType=$("#input_boxType").val();
    var factoryId=$("#select_factoryId").val();
    if(factoryId=="0"){
        alert("必须选择工厂")
    }else {
        window.open("goodDownload?supplierCode="+supplierCode+"&supplierName="+supplierName+"&goodCode="+goodCode+"&goodName="+goodName+"&factoryId="+factoryId+"&boxType="+boxType);
    }
}

//------------------------------------分页查询收货记录-------------------------------
function findByLimit(currentPage) {
    //显示加载提示信息
    $("#div_loading").css("display", "block");
    //获取参数
    var goodCode=$("#input_goodCode").val();
    var goodName=$("#input_goodName").val();
    var supplierCode=$("#input_supplierCode").val();
    var supplierName=$("#input_supplierName").val();
    var boxType=$("#input_boxType").val();
    var factoryId=$("#select_factoryId").val();
    //先清除旧数据
    $("#table_data").html("");
    //后台查询
    $.ajax({
        url: 'good',
        type: 'post',
        data: {
            'goodName':goodName,
            'goodCode':goodCode,
            'supplierName':supplierName,
            'supplierCode':supplierCode,
            'factoryId':factoryId,
            'boxType':boxType,
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
                        var suppliercode="";
                        var suppliername="";
                        var factoryName="";
                        if(record[i].supplier!=null){
                            suppliercode=record[i].supplier.suppliercode;
                            suppliername=record[i].supplier.suppliername;
                            if(record[i].supplier.route!=null){
                                if(record[i].supplier.route.factory!=null){
                                    factoryName=record[i].supplier.route.factory.factoryname;
                                }
                            }
                        }
                        var str = "<tr><td style='display: none'>" + record[i].id + "</td>"+
                            "<td>" + record[i].goodcode +"</td>"+
                            "<td>" + record[i].goodname +"</td>"+
                            "<td>" + suppliercode +"</td>"+
                            "<td>" + suppliername +"</td>"+
                            "<td>" + factoryName +"</td>"+
                            "<td>" + record[i].oneboxcount +"</td>"+
                            "<td>" + record[i].bincount +"</td>"+
                            "<td>" + record[i].onecarcount +"</td>"+
                            "<td>" + record[i].maxstock +"</td>"+
                            "<td>" + record[i].triggerstock +"</td>"+
                            "<td>" + record[i].quotaratio +"</td>"+
                            "<td>" + record[i].boxtype +"</td>"+
                            "<td>" + record[i].boxlength +"</td>"+
                            "<td>" + record[i].boxwidth +"</td>"+
                            "<td>" + record[i].boxheight +"</td>"+
                            "<td>" + record[i].packboxlength +"</td>"+
                            "<td>" + record[i].packboxwidth +"</td>"+
                            "<td>" + record[i].packboxheight +"</td>"+
                            "<td>" + record[i].packboxweight +"</td>"+
                            "<td>" + record[i].boxweight +"</td>"+
                            "<td>" + record[i].returnratio +"</td>"+
                            "<td>" + record[i].onetrayboxcount +"</td>"+
                            "<td>" + record[i].onetraylayerscount +"</td>"+
                            "<td>" + record[i].trayratio +"</td>"+
                            "<td>" + record[i].traylength +"</td>"+
                            "<td>" + record[i].traywidth +"</td>"+
                            "<td>" + record[i].trayheight +"</td>"+
                            "<td>" + record[i].packremarks +"</td>"+
                            "<td>" + record[i].receiver +"</td>"+
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
