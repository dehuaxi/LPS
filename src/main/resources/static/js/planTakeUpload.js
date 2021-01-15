
//-------------------------------页面加载执行--------------------------------------
$(document).ready(function () {
    //加载车型信息、计划信息
    loadPlan();
    //加载承运商
    loadCarrier();
    //加载司机信息
    $.ajax({
        url: 'driverAll',
        type: 'post',
        dataType:'json',
        success: function (data) {
            if(data.code==0){
                if(data.data==null){
                    alert("没有司机信息，请添加后再试");
                    window.close();
                }else {
                    for(var i=0;i<data.data.length;i++){
                        $("#select_driver").append("<option>"+data.data[i].phone+":"+data.data[i].name+"</option>")
                    }
                }
            } else {
                alert(data.msg);
                window.close();
            }
        },
        error:function(jqXHR, textStatus, errorThrown){
            var status = jqXHR.status;//404,500等
            var text = jqXHR.statusText;//404对应的Not found,500对应的error
            alert("加载司机信息失败：" + status + "  " + text);
            window.close();
        }
    });
})
//----------------加载承运商------------------
function loadCarrier(){
    $.ajax({
        url:'allCarrier',
        dataType:'json',
        type:'post',
        success:function (data){
            if(data.code==0){
                for(var i=0;data.data.length;i++){
                    $("#select_carrierName").append("<option>"+data.data[i].carriername+"</option>")
                }
            }else {
                alert(data.msg);
                window.close();
            }
        },
        error:function(jqXHR, textStatus, errorThrown){
            var status = jqXHR.status;//404,500等
            var text = jqXHR.statusText;//404对应的Not found,500对应的error
            alert("加载承运商信息失败：" + status + "  " + text);
            window.close();
        }
    })
}

//----------------选择司机----------------
function chooseDriver(){
    var info=$("#select_driver").val();
    if(info==""){
        $("#input_driver").val("");
        $("#input_driver").removeAttr("disabled");
        $("#input_driver").focus();
        $("#input_phone").val("");
        $("#input_phone").removeAttr("disabled");
    }else {
        var name=info.split(":")[1];
        var phone=info.split(":")[0];
        $("#input_driver").val(name);
        $("#input_phone").val(phone);
        $("#input_driver").attr("disabled","disabled");
        $("#input_phone").attr("disabled","disabled");
    }
}

//----------------加载计划信息和车辆信息------------------
function loadPlan(){
    var planNumber=$("#planNumber").html();
    if(planNumber==""){
        alert("无取货计划，无法绑定");
        window.close();
    }else {
        $("#table_data").html("");
        $("#div_loading").css("display","block");
        $.ajax({
            url: 'planTakeByNumber',
            type: 'post',
            data:{'planNumber':planNumber},
            dataType:'json',
            success: function (data) {
                if(data.code==0){
                    if(data.data==null){
                        //已经全部拼拆完成
                        alert("该计划没有具体内容，无法绑定");
                        window.close();
                    }else {
                        //加载车型信息
                        var carType=data.data[0].cartype;
                        $("#carType").html(carType);
                        $("#input_highLength").val(data.data[0].highlength);
                        $("#input_highHeight").val(data.data[0].highheight);
                        $("#input_lowLength").val(data.data[0].lowlength);
                        $("#input_lowHeight").val(data.data[0].lowheight);
                        $("#input_carWidth").val(data.data[0].carwidth);
                        for(var i=0;i<data.data.length;i++){
                            var goodcode="";
                            var goodname="";
                            var suppliercode="";
                            var suppliername="";
                            var oneBoxCount=0;
                            var goodid="";
                            if(data.data[i].good!=null){
                                goodcode=data.data[i].good.goodcode;
                                goodname=data.data[i].good.goodname;
                                goodid=data.data[i].good.id;
                                oneBoxCount=data.data[i].good.oneboxcount;
                                if(data.data[i].good.supplier!=null){
                                    suppliercode=data.data[i].good.supplier.suppliercode;
                                    suppliername=data.data[i].good.supplier.suppliername;
                                }
                            }
                            //没有重复的，就需要新增一行
                            var str="<tr onclick='showGeely(this)'><td>" + goodid +"</td>"+
                                "<td>" + goodcode +"</td>"+
                                "<td>" + goodname +"</td>"+
                                "<td>" + suppliercode +"</td>"+
                                "<td>" + suppliername +"</td>"+
                                "<td>" + data.data[i].boxcount +"</td>"+
                                "<td>" + data.data[i].count +"</td></tr>";
                            $("#table_data").append(str);
                        }
                        //根据车型加载车辆
                        $.ajax({
                            url:'carByCartype',
                            dataType:'json',
                            type:'post',
                            data:{'carTypeName':carType},
                            success:function (datas){
                                if(datas.code==0){
                                    for(var g=0;g<datas.data.length;g++){
                                        $("#select_carNumber").append("<option>"+datas.data[g].carnumber+"</option>")
                                    }
                                }else {
                                    alert(datas.msg);
                                }
                            },
                            error:function(jqXHR, textStatus, errorThrown){
                                var status = jqXHR.status;//404,500等
                                var text = jqXHR.statusText;//404对应的Not found,500对应的error
                                alert("加载"+carType+"车型相关车辆信息失败：" + status + "  " + text);
                            }
                        })
                    }
                } else {
                    alert(data.msg);
                    window.close();
                }
                $("#div_loading").css("display", "none");
            },
            error:function(jqXHR, textStatus, errorThrown){
                var status = jqXHR.status;//404,500等
                var text = jqXHR.statusText;//404对应的Not found,500对应的error
                alert("加载计划信息失败：" + status + "  " + text);
                window.close();
            }
        });
    }
}

//------------------选择车辆信息------------------
function chooseCarNumber(){
    var carNumber=$("#select_carNumber").val();
    if(carNumber==""){
        $("#input_highLength").val(0);
        $("#input_highLength").removeAttr("disabled");
        $("#input_highHeight").val(0);
        $("#input_highHeight").removeAttr("disabled");
        $("#input_lowLength").val(0);
        $("#input_lowLength").removeAttr("disabled");
        $("#input_lowHeight").val(0);
        $("#input_lowHeight").removeAttr("disabled");
        $("#input_carWidth").val(0);
        $("#input_carWidth").removeAttr("disabled");
        $("#input_carNumber").val("");
        $("#input_carNumber").removeAttr("disabled");
        $("#select_carrierName").val("");
        $("#select_carrierName").removeAttr("disabled");
    }else {
        //根据车牌号查询车辆信息
        $.ajax({
            url:'carByCarnumber',
            dataType:'json',
            type:'post',
            data:{'carNumber':carNumber},
            success:function (datas){
                if(datas.code==0){
                    $("#input_highLength").val(datas.data.highlength);
                    $("#input_highLength").attr("disabled","disabled");
                    $("#input_highHeight").val(datas.data.highheight);
                    $("#input_highHeight").attr("disabled","disabled");
                    $("#input_lowLength").val(datas.data.lowlength);
                    $("#input_lowLength").attr("disabled","disabled");
                    $("#input_lowHeight").val(datas.data.lowheight);
                    $("#input_lowHeight").attr("disabled","disabled");
                    $("#input_carWidth").val(datas.data.carwidth);
                    $("#input_carWidth").attr("disabled","disabled");
                    $("#input_carNumber").val(datas.data.carnumber);
                    $("#input_carNumber").attr("disabled","disabled");
                    $("#select_carrierName").val(datas.data.carrier.carriername);
                    $("#select_carrierName").attr("disabled","disabled");
                }else {
                    alert(datas.msg);
                }
            },
            error:function(jqXHR, textStatus, errorThrown){
                var status = jqXHR.status;//404,500等
                var text = jqXHR.statusText;//404对应的Not found,500对应的error
                alert("加载车辆信息失败：" + status + "  " + text);
            }
        })
    }
}

//-----------------上传PD单--------------------
function toUpload(){
    $("#uploadResult").html("");
    $("#uploadFile").val("");
    $("#uploadModal").modal("show");
}
function upload(){
//获取选择的文件值
    var file = $("#uploadFile").val();
    if (file != "") {
        //获取表单中的文件
        var formData = new FormData($('#uploadForm')[0]);
        var planNumber=$("#planNumber").html();
        formData.append("planNumber",planNumber);
        //显示加载提示信息
        $("#div_loading").css("display","block");
        //清空表格
        $("#uploadResult").html("");
        $("#uploadFile").val("");
        $("#table_data2").html("");
        //ajax请求
        $.ajax({
            url: "planTakeUpload",//后台的接口地址
            type: "post",//post请求方式
            data: formData,//参数
            cache: false,//无缓存
            processData: false,//必须false才会避开jQuery对 formdata 的默认处理
            contentType: false,//必须false才会自动加上正确的Content-Type
            success: function (data) {
                if(data.code==0){
                    for(var i=0;i<data.data.length;i++){
                        var billList=data.data[i];
                        var goodcode="";
                        var goodname="";
                        var suppliercode="";
                        var suppliername="";
                        var goodid="";
                        if(billList.length>1){
                            if(billList[0].good!=null){
                                goodcode=billList[0].good.goodcode;
                                goodname=billList[0].good.goodname;
                                goodid=billList[0].good.id;
                                if(billList[0].good.supplier!=null){
                                    suppliercode=billList[0].good.supplier.suppliercode;
                                    suppliername=billList[0].good.supplier.suppliername;
                                }
                            }
                            for(var k=0;k<billList.length;k++){
                                //填入表格
                                var str="<tr><td>"+goodid+"</td>" +
                                    "<td>"+billList[k].billnumber+"</td>" +
                                    "<td>"+goodcode+"</td>" +
                                    "<td>"+goodname+"</td>" +
                                    "<td>"+suppliercode+"</td>" +
                                    "<td>"+suppliername+"</td>" +
                                    "<td>"+billList[k].count+"</td>" +
                                    "<td>"+billList[k].batch+"</td>" +
                                    "<td onclick='toUpdateRealcount(this)'></td></tr>";
                                $("#table_data2").append(str);
                            }
                        }else if(billList.length==1){
                            if(billList[0].good!=null){
                                goodcode=billList[0].good.goodcode;
                                goodname=billList[0].good.goodname;
                                goodid=billList[0].good.id;
                                if(billList[0].good.supplier!=null){
                                    suppliercode=billList[0].good.supplier.suppliercode;
                                    suppliername=billList[0].good.supplier.suppliername;
                                }
                            }
                            var realCount=billList[0].count;
                            var color="";
                            //根据物料id获取该物料需要取货数量
                            $("#table_data tr").each(function (){
                                var id=$(this).find("td:eq(0)").text();
                                var needCount=$(this).find("td:eq(6)").text();
                                if(id==goodid){
                                    if(realCount!=needCount){
                                        color="red";
                                    }
                                }
                            })
                            //填入表格
                            var str="<tr><td>"+goodid+"</td>" +
                                "<td>"+billList[0].billnumber+"</td>" +
                                "<td>"+goodcode+"</td>" +
                                "<td>"+goodname+"</td>" +
                                "<td>"+suppliercode+"</td>" +
                                "<td>"+suppliername+"</td>" +
                                "<td>"+realCount+"</td>" +
                                "<td>"+billList[0].batch+"</td>" +
                                "<td style='color: "+color+"' onclick='toUpdateRealcount(this)'>"+realCount+"</td></tr>";
                            $("#table_data2").append(str);
                        }
                    }
                    $("#uploadModal").modal("hide");
                }else {
                    $("#uploadResult").html(data.msg);
                }
                //隐藏加载提示信息
                $("#div_loading").css("display","none");
            },
            error: function (jqXHR, textStatus, errorThrown) {
                var status = jqXHR.status;//404,500等
                var text = jqXHR.statusText;//404对应的Not found,500对应的error
                alert("上传失败：" + status + "  " + text);
                //隐藏加载提示
                $("#div_loading").css("display","none");
            }
        });
    }
}

//点击取货计划行，显示对应的PD单行
function showGeely(tr){
    //移除当前表格每行的样式
    $("#table_data tr").each(function (){
        $(this).removeAttr("style");
    })
    var goodId=$(tr).find("td:eq(0)").text();
    //获取当前行的背景色属性
    var currentBc=$(tr).css("background-color");
    if(currentBc.indexOf("#")<0){
        //rgb转化为#
        var bcl=currentBc.match(/^rgb\((\d+),\s*(\d+),\s*(\d+)\)$/);
        var b1=("0" + parseInt(bcl[1]).toString(16)).slice(-2);
        var b2=("0" + parseInt(bcl[2]).toString(16)).slice(-2);
        var b3=("0" + parseInt(bcl[3]).toString(16)).slice(-2);
        currentBc="#"+b1+b2+b3
    }
    if(currentBc!="#579df3"){
        $(tr).css("background-color","#579df3");
        $(tr).css("color","#ffffff");
        $("#table_data2 tr").each(function (){
            var tdGoodId=$(this).find("td:eq(0)").text();
            if(tdGoodId==goodId){
                $(this).css("background-color","#579df3");
                $(this).css("color","#ffffff");
            }else {
                $(this).removeAttr("style");
            }
        })
    }else {
        $(tr).removeAttr("style");
        $("#table_data2 tr").each(function (){
            var tdGoodId=$(this).find("td:eq(0)").text();
            if(tdGoodId==goodId){
                $(this).removeAttr("style");
            }
        })
    }
}

//修改实收数量
function toUpdateRealcount(td){
    var goodId=$(td).parent().find("td:eq(0)").text();
    var geelyBillNumber=$(td).parent().find("td:eq(1)").text();
    var realCount=$(td).text();
    $("#updateRealcountModal_goodId").val(goodId);
    $("#updateRealcountModal_geelyBillNumber").val(geelyBillNumber);
    $("#updateRealcountModal_realCount").val(realCount);
    $("#updateRealcountModal").modal("show");
    $("#updateRealcountModal").on("shown.bs.modal",function (){
        $("#updateRealcountModal_realCount").focus();
    });
}
function updateRealcount(){
    var goodId=$("#updateRealcountModal_goodId").val();
    var geelyBillNumber=$("#updateRealcountModal_geelyBillNumber").val();
    var realCount=$("#updateRealcountModal_realCount").val();
    var str=/^[1-9]{1}[0-9]{0,10}$/;
    if(!str.test(realCount)){
        $("#updateRealcountModal_realCount").focus();
    }else{
        //修改
        $("#table_data2 tr").each(function (){
            var goodTd=$(this).find("td:eq(0)").text();
            var billNumberTd=$(this).find("td:eq(1)").text();
            if(goodTd==goodId&&geelyBillNumber==billNumberTd){
                $(this).find("td:eq(8)").html(realCount);
                var td=$(this).find("td:eq(8)");
                //看修改后数量是否和需求一样
                $("#table_data tr").each(function (){
                    var id=$(this).find("td:eq(0)").text();
                    var needCount=$(this).find("td:eq(6)").text();
                    if(id==goodId){
                        //先移除所有样式
                        $(td).removeAttr("style");
                        if(realCount!=needCount){
                            //如果修改后的值和需求不一样，变红色
                            $(td).css("color","red");
                        }
                    }
                })
            }
        })
        $("#updateRealcountModal").modal("hide");
    }
}

//-----------------------生成运输单------------------
function transportBillAdd(){
    //车型信息
    var carTypeName=$("#carType").html();
    var highLength=$("#input_highLength").val();
    var highHeight=$("#input_highHeight").val();
    var lowLength=$("#input_lowLength").val();
    var lowHeight=$("#input_lowHeight").val();
    var carWidth=$("#input_carWidth").val();
    //车辆司机信息
    var carNumber=$("#input_carNumber").val();
    var driver=$("#input_driver").val();
    var phone=$("#input_phone").val();
    //承运商
    var carrierName=$("#select_carrierName").val();
    //取货计划编号
    var planNumber=$("#planNumber").html();
    //运输费
    var money=$("#input_money").val();
    var remarks=$("#input_remarks").val();
    //PD单实收信息:物料id,吉利单号,实收数量;物料id,吉利单号,实收数量...
    var infos="";
    $("#table_data2 tr").each(function (){
        var goodid=$(this).find("td:eq(0)").text();
        var geelyBillNumber=$(this).find("td:eq(1)").text();
        var batch=$(this).find("td:eq(7)").text();
        var realCount=$(this).find("td:eq(8)").text();
        infos+=";"+goodid+","+geelyBillNumber+","+batch+","+realCount;
    })
    var str=/^([京津晋冀蒙辽吉黑沪苏浙皖闽赣鲁豫鄂湘粤桂琼渝川贵云藏陕甘青宁新][ABCDEFGHJKLMNPQRSTUVWXY][1-9DF][1-9ABCDEFGHJKLMNPQRSTUVWXYZ]\d{3}[1-9DF]|[京津晋冀蒙辽吉黑沪苏浙皖闽赣鲁豫鄂湘粤桂琼渝川贵云藏陕甘青宁新][ABCDEFGHJKLMNPQRSTUVWXY][\dABCDEFGHJKLNMxPQRSTUVWXYZ]{5})$/;
    var str1=/^[1-9]{1}[0-9]{0,10}$/;
    var str2=/^[0-9]{1,11}$/;
    if(!str2.test(highLength)){
        $("#input_highLength").focus();
    }else if(!str2.test(highHeight)){
        $("#input_highHeight").focus();
    }else if(!str1.test(lowLength)){
        $("#input_lowLength").focus();
    }else if(!str1.test(lowHeight)){
        $("#input_lowHeight").focus();
    }else if(!str1.test(carWidth)){
        $("#input_carWidth").focus();
    }else if(!str.test(carNumber)){
        $("#input_carNumber").focus();
    }else if(driver==""){
        $("#input_driver").focus();
    }else if(!str1.test(phone)){
        $("#input_phone").focus();
    }else if(infos==""){
        alert("请先上传吉利单据")
    }else {
        var isAdd=false;
        if(money==""){
            isAdd=true;
        }else if(!str1.test(money)){
            $("#input_money").focus();
        }else if(remarks==""){
            //有运输费，必须填原因
            alert("填了自定义的运输费必须填备注");
            $("#input_remarks").focus();
        }else {
            isAdd=true;
        }
        if(isAdd){
            var tips=confirm("是否确定生成取货计划？");
            if(tips){
                infos=infos.substring(1);
                $("#div_loading").css("display", "block");
                //后台
                $.ajax({
                    url: 'transportBillCacheAdd',
                    type: 'post',
                    data: {
                        'planNumber':planNumber,
                        'carNumber':carNumber,
                        'driver':driver,
                        'phone':phone,
                        'carTypeName':carTypeName,
                        'highLength':highLength,
                        'highHeight':highHeight,
                        'lowLength':lowLength,
                        'lowHeight':lowHeight,
                        'carWidth':carWidth,
                        'carrierName':carrierName,
                        'money':money,
                        'remarks':remarks,
                        'geelyRealInfo':infos},
                    dataType:'json',
                    success: function (data) {
                        if(data.code==0){
                            //返回的运输单号，是否需要打印
                            var tips=confirm("是否打印运输单"+data.data);
                            if(tips){
                                window.location.href="transportBillCacheBillPrint?billNumber="+data.data;
                            }else {
                                window.close();
                            }
                        }else {
                            alert(data.msg);
                        }
                        //隐藏加载提示信息
                        $("#div_loading").css("display", "none");
                    },
                    error:function(jqXHR, textStatus, errorThrown){
                        var status = jqXHR.status;//404,500等
                        var text = jqXHR.statusText;//404对应的Not found,500对应的error
                        alert("生成失败：" + status + "  " + text);
                        $("#div_loading").css("display","none");
                    }
                });
            }
        }
    }
}
