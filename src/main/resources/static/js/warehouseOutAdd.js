
//-------------------------------页面加载执行--------------------------------------
$(document).ready(function () {
    //显示选择线路类型的模态框
    $("#typeModal").modal("show");
    //加载出发地中转仓
    $.ajax({
        url: 'currentWarehouse',
        type: 'post',
        dataType:'json',
        success: function (data) {
            if(data.code==0){
                for(var i=0;i<data.data.length;i++){
                    $("#select_startId").append("<option value='"+data.data[i].id+"'>"+data.data[i].warehousename+"</option>")
                }
            } else {
                alert(data.msg);
                window.close();
            }
        },
        error:function(jqXHR, textStatus, errorThrown){
            var status = jqXHR.status;//404,500等
            var text = jqXHR.statusText;//404对应的Not found,500对应的error
            alert("加载出发地中转仓信息失败：" + status + "  " + text);
            window.close();
        }
    });
    //加载车型
    $.ajax({
        url: 'carType',
        type: 'post',
        dataType:'json',
        success: function (data) {
            if(data.code==0){
                if(data.data==null){
                    alert("没有车型信息，请添加后再试");
                    window.close();
                }else {
                    for(var i=0;i<data.data.length;i++){
                        $("#select_carTypeName").append("<option>"+data.data[i].cartypename+"</option>")
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
            alert("加载车型信息失败：" + status + "  " + text);
            window.close();
        }
    });
    //加载承运商
    $.ajax({
        url: 'allCarrier',
        type: 'post',
        dataType:'json',
        success: function (data) {
            if(data.code==0){
                for(var i=0;i<data.data.length;i++){
                    $("#select_carrierName").append("<option>"+data.data[i].carriername+"</option>")
                }
            } else {
                alert(data.msg);
                window.close();
            }
        },
        error:function(jqXHR, textStatus, errorThrown){
            var status = jqXHR.status;//404,500等
            var text = jqXHR.statusText;//404对应的Not found,500对应的error
            alert("加载承运商失败：" + status + "  " + text);
            window.close();
        }
    });

    //选中在库记录后，弹出填入数量的模态框中填入数量输入框的按键事件，自动计算箱数
    $("#chooseModal_count").keyup(function (e){
        var count=$("#chooseModal_count").val();
        //最大可选数量
        var unsureCount=$("#chooseModal_unsureCount").val();
        var str=/^[1-9]{1}[0-9]{0,10}$/;
        if(!str.test(count)){
            $("#chooseModal_count").val("");
            $("#chooseModal_count").focus();
        }else if(parseInt(count)>parseInt(unsureCount)){
            alert("填入的数量不可大于最大可选数量");
            $("#chooseModal_count").focus();
        }else {
            var oneBoxCount=$("#chooseModal_oneBoxCount").val();
            var boxCount=0;
            if(parseInt(count)%parseInt(oneBoxCount)==0){
                boxCount=parseInt(count)/parseInt(oneBoxCount);
                $("#chooseModal_odd").val("0");
            }else {
                //向上取整
                boxCount=Math.ceil(count / oneBoxCount);
                $("#chooseModal_odd").val(count%oneBoxCount);
            }
            $("#chooseModal_boxCount").val(boxCount);
        }
    })

    //修改数量的模态框中填入数量输入框按键事件，自动计算箱数
    $("#updateCountModal_count").keyup(function (e){
        var count=$("#updateCountModal_count").val();
        //最大可选数量
        var unsureCount=$("#chooseModal_unsureCount").val();
        var str=/^[1-9]{1}[0-9]{0,10}$/;
        if(!str.test(count)){
            $("#updateCountModal_count").val("");
            $("#updateCountModal_count").focus();
        }else if(parseInt(count)>parseInt(unsureCount)){
            alert("填入的数量不可大于最大可选数量");
            $("#updateCountModal_count").focus();
        }else{
            var oneBoxCount=$("#updateCountModal_oneBoxCount").val();
            var boxCount=0;
            if(count%oneBoxCount==0){
                boxCount=count/oneBoxCount;
                $("#updateCountModal_odd").val("0");
            }else {
                //向上取整
                boxCount=Math.ceil(count / oneBoxCount);
                $("#updateCountModal_odd").val(count%oneBoxCount);
            }
            $("#updateCountModal_boxCount").val(boxCount);
        }
    })

    //高板长输入框按键事件，防止选中计划后修改值
    $("#input_highLength").keydown(function (e){
        if($("#table_data2 tr").length>0){
            alert("有选中的计划，无法修改");
            return false;
        }
    })
    //高板高输入框按键事件，防止选中计划后修改值
    $("#input_highHeight").keydown(function (e){
        if($("#table_data2 tr").length>0){
            alert("有选中的计划，无法修改");
            return false;
        }
    })
    //低板长输入框按键事件，防止选中计划后修改值
    $("#input_lowLength").keydown(function (e){
        if($("#table_data2 tr").length>0){
            alert("有选中的计划，无法修改");
            return false;
        }
    })
    //低板高输入框按键事件，防止选中计划后修改值
    $("#input_lowHeight").keydown(function (e){
        if($("#table_data2 tr").length>0){
            alert("有选中的计划，无法修改");
            return false;
        }
    })
    //车宽输入框按键事件，防止选中计划后修改值
    $("#input_carWidth").keydown(function (e){
        if($("#table_data2 tr").length>0){
            alert("有选中的计划，无法修改");
            return false;
        }
    })
})

//选择目的地类型，加载目的地
function chooseType(){
    var routeType=$("#select_routeType").val();
    $("#select_endId").html("");
    $("#select_endId").append("<option value='0'>请选择目的地中转仓/工厂</option>")
    if(routeType=="中转仓-工厂"){
        //加载工厂
        $.ajax({
            url: 'currentFactory',
            type: 'post',
            dataType:'json',
            success: function (data) {
                if (data.code==0) {
                    for(var i=0;i<data.data.length;i++){
                        $("#select_endId").append("<option value='"+data.data[i].id+"'>"+data.data[i].factoryname+"</option>")
                    }
                } else {
                    alert(data.msg);
                }
            },
            error:function(jqXHR, textStatus, errorThrown){
                var status = jqXHR.status;//404,500等
                var text = jqXHR.statusText;//404对应的Not found,500对应的error
                alert("加载目的地工厂信息失败：" + status + "  " + text);
            }
        });
    }else if(routeType=="中转仓-中转仓"){
        $("#type").html("中转仓-中转仓");
        //加载中转仓
        $.ajax({
            url: 'currentWarehouse',
            type: 'post',
            dataType:'json',
            success: function (data) {
                if (data.code==0) {
                    for(var i=0;i<data.data.length;i++){
                        $("#select_endId").append("<option value='"+data.data[i].id+"'>"+data.data[i].warehousename+"</option>")
                    }
                } else {
                    alert(data.msg);
                }
            },
            error:function(jqXHR, textStatus, errorThrown){
                var status = jqXHR.status;//404,500等
                var text = jqXHR.statusText;//404对应的Not found,500对应的error
                alert("加载目的地中转仓信息失败：" + status + "  " + text);
            }
        });
    }
}
function sureType(){
    var routeType=$("#select_routeType").val();
    var startId=$("#select_startId").val();
    var endId=$("#select_endId").val();
    if(routeType==""){
        alert("请选择线路类型");
    }else if(startId==0){
        alert("请选择出发地");
    }else if(endId==0){
        alert("请选择目的地");
    }else {
        $("#route").html($("#select_startId option:selected").text()+" ->-> "+$("#select_endId option:selected").text());
        $("#typeModal").modal("hide");
    }
}

//根据条件查询在库记录
function findWarehouseCache(){
    $("#table_data").html("");
    var goodCode=$("#input_goodCode").val();
    var goodName=$("#input_goodName").val();
    var supplierCode=$("#input_supplierCode").val();
    var supplierName=$("#input_supplierName").val();
    var geelyBillNumber=$("#input_geelyBillNumber").val();
    var packState=$("#select_packState").val();
    var warehouseId=$("#select_warehouseId").val();
    $.ajax({
        url:'warehouseCacheByCondition',
        type:'post',
        data:{'goodCode':goodCode,
            'goodName':goodName,
            'supplierCode':supplierCode,
            'supplierName':supplierName,
            'geelyBillNumber':geelyBillNumber,
            'packState':packState,
            'warehouseId':warehouseId},
        dataType:'json',
        success:function (data){
            if(data.code==0){
                for(var i=0;i<data.data.length;i++){
                    var str="<tr><td style='display: none'>"+data.data[i].id+"</td>" +
                        "<td><a href='#' onclick='chooseWarehouseCache(this)'>"+data.data[i].good.goodcode+"</a></td>" +
                        "<td>"+data.data[i].good.goodname+"</td>" +
                        "<td>"+data.data[i].good.supplier.suppliercode+"</td>" +
                        "<td>"+data.data[i].good.supplier.suppliername+"</td>" +
                        "<td>"+data.data[i].geelybillnumber+"</td>" +
                        "<td>"+data.data[i].geelycount+"</td>" +
                        "<td>"+data.data[i].batch+"</td>" +
                        "<td>"+data.data[i].count+"</td>" +
                        "<td>"+data.data[i].oneboxcount+"</td>" +
                        "<td>"+data.data[i].packstate+"</td></tr>";
                    $("#table_data").append(str);
                }
            }else {
                alert(data.msg);
            }
        },
        error:function(jqXHR, textStatus, errorThrown){
            var status = jqXHR.status;//404,500等
            var text = jqXHR.statusText;//404对应的Not found,500对应的error
            alert("加载信息失败：" + status + "  " + text);
        }
    })
}

//选择车型
function chooseCarType(){
    var carTypeName=$("#select_carTypeName").val();
    //如果没有在库记录，选择了空
    if(carTypeName==""){
        $("#input_highLength").val("0");
        $("#input_highHeight").val("0");
        $("#input_lowLength").val("0");
        $("#input_lowHeight").val("0");
        $("#input_carWidth").val("0");
    }else {
        //根据车型后台查询
        $.ajax({
            url:'carTypeByName',
            type:'post',
            data:{'name':carTypeName},
            dataType:'json',
            success:function (data){
                if(data.code==0){
                    $("#input_highLength").val(data.data.highlength);
                    $("#input_highHeight").val(data.data.highheight);
                    $("#input_lowLength").val(data.data.lowlength);
                    $("#input_lowHeight").val(data.data.lowheight);
                    $("#input_carWidth").val(data.data.carwidth);
                    //根据车型信息加载车辆信息
                    $.ajax({
                        url:'carByCartype',
                        type:'post',
                        data:{'carTypeName':carTypeName},
                        dataType:'json',
                        success:function (datas){
                            if(datas.code==0){
                                for(var i=0;i<datas.data.length;i++){
                                    $("#select_car").append("<option>"+datas.data[i].carnumber+"</option>");
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
}

//选择车辆
function chooseCar(){
    var carNumber=$("#select_car").val();
    if(carNumber==""){
        //司机、手机
        $("#input_driver").val("");
        $("#input_phone").val("");
        //车辆尺寸根据车型查询
        $.ajax({
            url:'carTypeByName',
            type:'post',
            data:{'name':carTypeName},
            dataType:'json',
            success:function (data){
                if(data.code==0){
                    $("#input_highLength").val(data.data.highlength);
                    $("#input_highHeight").val(data.data.highheight);
                    $("#input_lowLength").val(data.data.lowlength);
                    $("#input_lowHeight").val(data.data.lowheight);
                    $("#input_carWidth").val(data.data.carwidth);
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
    }else {
        //根据车辆车牌号查询
        $.ajax({
            url:'carByCarnumber',
            type:'post',
            data:{'carNumber':carNumber},
            dataType:'json',
            success:function (datas){
                if(datas.code==0){
                    //加载司机、手机号
                    $("#input_driver").val(datas.data.driver);
                    $("#input_phone").val(datas.data.phone);
                    $("#input_highLength").val(datas.data.highlength);
                    $("#input_highHeight").val(datas.data.highheight);
                    $("#input_lowLength").val(datas.data.lowlength);
                    $("#input_lowHeight").val(datas.data.lowheight);
                    $("#input_carWidth").val(datas.data.carwidth);
                }else {
                    alert(data.msg);
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

//点击物料编号，弹出填入出库数量的模态框
function chooseWarehouseCache(a){
    //获取选择的缺件计划的id
    var warehouseCacheId=$(a).parent().parent().find("td:eq(0)").text();
    var goodCode=$(a).text();
    var goodName=$(a).parent().parent().find("td:eq(2)").text();
    var supplierCode=$(a).parent().parent().find("td:eq(3)").text();
    var supplierName=$(a).parent().parent().find("td:eq(4)").text();
    var geelyBillNumber=$(a).parent().parent().find("td:eq(5)").text();
    var batch=$(a).parent().parent().find("td:eq(7)").text();
    var useCount=$(a).parent().parent().find("td:eq(8)").text();
    var oneBoxCount=$(a).parent().parent().find("td:eq(9)").text();
    $("#chooseModal_warehouseCacheId").val(warehouseCacheId);
    $("#chooseModal_goodCode").val(goodCode);
    $("#chooseModal_goodName").val(goodName);
    $("#chooseModal_supplierCode").val(supplierCode);
    $("#chooseModal_supplierName").val(supplierName);
    $("#chooseModal_geelyBillNumber").val(geelyBillNumber);
    $("#chooseModal_batch").val(batch);
    $("#chooseModal_useCount").val(useCount);
    $("#chooseModal_count").val("");
    $("#chooseModal_oneBoxCount").val(oneBoxCount);
    $("#chooseModal").modal("show");
    $("#chooseModal").on("shown.bs.modal",function (){
        $("#chooseModal_count").focus();
    })
}

//填入出库数量后，点击确认
function choose(){
    var warehouseCacheId=$("#chooseModal_warehouseCacheId").val();
    var goodCode=$("#chooseModal_goodCode").val();
    var goodName=$("#chooseModal_goodName").val();
    var supplierCode=$("#chooseModal_supplierCode").val();
    var supplierName=$("#chooseModal_supplierName").val();
    var geelyBillNumber=$("#chooseModal_geelyBillNumber").val();
    var batch=$("#chooseModal_batch").val();
    var useCount=$("#chooseModal_useCount").val();
    var count=$("#chooseModal_count").val();
    var oneBoxCount=$("#chooseModal_oneBoxCount").val();
    if(parseInt(count)>parseInt(useCount)){
        alert("出库数量不可大于库存数量"+useCount);
        $("#chooseModal_count").val("");
        $("#chooseModal_count").focus();
    }else{
        var isHas=false;
        $("#table_data2 tr").each(function (){
            var id=$(this).parent().parent().find("td:eq(0)").text();
            var geelyBillNumber2=$(this).parent().parent().find("td:eq(5)").text();
            var batch2=$(this).parent().parent().find("td:eq(6)").text();
            if(id==warehouseCacheId&&geelyBillNumber2==geelyBillNumber&&batch2==batch){
                isHas=true;
            }
        })
        if(isHas){
            alert("已经选中过该在库记录，不可重复选择");
            $("#chooseModal").modal("hide");
        }else {
            //出库箱数
            var boxCount=0;
            if(count%oneBoxCount!=0){
                boxCount=Math.ceil(count/oneBoxCount);
            }else {
                boxCount=count/oneBoxCount;
            }
            var str="<tr><td style='display: none'>"+warehouseCacheId+"</td>" +
                "<td>"+goodCode+"</td>" +
                "<td>"+goodName+"</td>" +
                "<td>"+supplierCode+"</td>" +
                "<td>"+supplierName+"</td>" +
                "<td>"+geelyBillNumber+"</td>" +
                "<td>"+batch+"</td>" +
                "<td>"+oneBoxCount+"</td>" +
                "<td>"+count+"</td>" +
                "<td>"+boxCount+"</td><td><button type='button' class='btn btn-xs btn-danger' onclick='chooseRemove(this)'>取消</button></td></tr>";
            $("#table_data2").prepend(str);
            $("#chooseModal").modal("hide");
        }
    }
}

//从出库表中移除选中的在库物料
function chooseRemove(btn){
    $(btn.parentNode.parentNode).remove()
}

//-----------------------生成在途运输单------------------
function createTransportBill(){
    var startId=$("#select_startId").val();
    var endId=$("#select_endId").val();
    var routeType=$("#select_routeType").val();
    var carTypeName=$("#select_carTypeName").val();
    var carNumber=$("#input_carNumber").val().replace(/\s*/g,"");
    var driver=$("#input_driver").val().replace(/\s*/g,"");
    var phone=$("#input_phone").val().replace(/\s*/g,"");
    var highLength=$("#input_highLength").val().replace(/\s*/g,"");
    var highHeight=$("#input_highHeight").val().replace(/\s*/g,"");
    var lowLength=$("#input_lowLength").val().replace(/\s*/g,"");
    var lowHeight=$("#input_lowHeight").val().replace(/\s*/g,"");
    var carWidth=$("#input_carWidth").val().replace(/\s*/g,"");
    var carrierName=$("#select_carrierName").val();
    var money=$("#input_money").val().replace(/\s*/g,"");
    var remarks=$("#input_remarks").val().replace(/\s*/g,"");
    var isHasCarNumber=false;
    if(carTypeName==""){
        alert("请选择车型");
    }else if(carNumber==""){
        carNumber=$("#select_car").val();
        if(carNumber==""){
            alert("必须选择车辆，如果车辆不在系统中请手动填入车牌号");
            $("#select_car").focus();
        }else {
            isHasCarNumber=true;
        }
    }else {
        isHasCarNumber=true;
    }
    if(isHasCarNumber){
        var str=/^[1-9]{1}[0-9]{0,10}$/;
        //有车牌号才往下看
        var carTest=/^([京津晋冀蒙辽吉黑沪苏浙皖闽赣鲁豫鄂湘粤桂琼渝川贵云藏陕甘青宁新][ABCDEFGHJKLMNPQRSTUVWXY][1-9DF][1-9ABCDEFGHJKLMNPQRSTUVWXYZ]\d{3}[1-9DF]|[京津晋冀蒙辽吉黑沪苏浙皖闽赣鲁豫鄂湘粤桂琼渝川贵云藏陕甘青宁新][ABCDEFGHJKLMNPQRSTUVWXY][\dABCDEFGHJKLNMxPQRSTUVWXYZ]{5})$/;
        if(!carTest.test(carNumber)){
            alert("车牌号格式错误");
            $("#input_carNumber").focus();
        }else if(driver==""){
            alert("司机姓名必填");
            $("#input_driver").focus();
        }else if(!/^[0-9]{11}$/.test(phone)){
            alert("手机格式错误");
            $("#input_phone").focus();
        }else if(!str.test(lowLength)){
            alert("低板长(mm)必须为正整数");
            $("#input_lowLength").focus();
        }else if(!str.test(lowHeight)){
            alert("低板高(mm)必须为正整数");
            $("#input_lowHeight").focus();
        }else if(!str.test(carWidth)){
            alert("车宽(mm)必须为正整数");
            $("#input_carWidth").focus();
        }else if(carrierName==""){
            alert("请选择承运商");
        }else {
            var isContinue=false;
            if(money!=""){
                var str1=/^\d+(.\d{1,2})?$/;
                if(!str1.test(money)){
                    alert("配送费只能是最多保留2位小数点的数字");
                    $("#input_money").focus();
                }else if(remarks==""){
                    alert("一旦填入配送费，请填入备注说明");
                    $("#input_remarks").focus();
                }else {
                    isContinue=true;
                }
            }else {
                isContinue=true;
            }
            //判断配送费是否填入，如果填入是否符合格式。
            if(isContinue){
                //可以继续往下看
                var goodInfo="";
                //物料信息:在库记录id,出库数量;在库记录id,出库数量;...
                $("#table_data2 tr").each(function (){
                    var id=$(this).find("td:eq(0)").text();
                    var count=$(this).find("td:eq(8)").text();
                    goodInfo+=";"+id+","+count;
                })
                if(goodInfo!=""){
                    var tips=confirm("确定出库并生成运输单？");
                    if(tips){
                        goodInfo=goodInfo.substring(1);
                        $("#div_loading").css("display", "block");
                        //后台
                        $.ajax({
                            url: 'warehouseOutAdd',
                            type: 'post',
                            data: {
                                'startId':startId,
                                'endId':endId,
                                'routeType':routeType,
                                'carTypeName':carTypeName,
                                'carNumber':carNumber,
                                'driver':driver,
                                'phone':phone,
                                'highLength':highLength,
                                'highHeight':highHeight,
                                'lowLength':lowLength,
                                'lowHeight':lowHeight,
                                'carWidth':carWidth,
                                'carrierName':carrierName,
                                'money':money,
                                'remarks':remarks,
                                'goodInfo':goodInfo},
                            dataType:'json',
                            success: function (data) {
                                if(data.code==0){
                                    var tips=confirm("是否打印运输单"+data.data+"?");
                                    if(tips){
                                        window.open("transportBillCacheBillPrint?billNumber="+data.data);
                                    }
                                    window.location.reload();
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
    }
}
