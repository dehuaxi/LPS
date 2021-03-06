
//-------------------------------页面加载执行--------------------------------------
$(document).ready(function () {
    //加载所有线路上物料信息以及方案中物料信息
    findAll();

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

//选择车型。如果有选择的，则改变车型后，用新的车型的地板高重新计算一遍所有选择的在库记录
function chooseCarType(){
    var carTypeName=$("#select_carTypeName").val();
    var lastCarType=$("#carType").html();
    //如果有选择的在库记录，就无法变动
    if($("#table_data2 tr").length>0){
        alert("有选中的计划，无法变更车型");
        $("#select_carTypeName").val(lastCarType);
    }else {
        //如果没有在库记录，选择了空
        if(carTypeName==""){
            $("#input_highLength").val("0");
            $("#input_highHeight").val("0");
            $("#input_lowLength").val("0");
            $("#input_lowHeight").val("0");
            $("#input_carWidth").val("0");
            $("#totalHighLength").html("0");
            $("#totalLowLength").html("0");
            $("#carType").html(carTypeName);
        }else {
            //根据车型后台查询
            $.ajax({
                url:'carTypeByName',
                type:'post',
                data:{'name':carTypeName},
                dataType:'json',
                success:function (data){
                    if(data.code==0){
                        $("#totalHighLength").html("0");
                        $("#totalLowLength").html(parseFloat(data.data.lowlength)/parseFloat("1000"));
                        $("#totalHighLength").html(parseFloat(data.data.highlength)/parseFloat("1000"));
                        $("#carType").html(carTypeName);
                        var newlLowHeight=data.data.lowheight;
                        $("#input_highLength").val(data.data.highlength);
                        $("#input_highHeight").val(data.data.highheight);
                        $("#input_lowLength").val(data.data.lowlength);
                        $("#input_lowHeight").val(newlLowHeight);
                        $("#input_carWidth").val(data.data.carwidth);
                    }else {
                        alert(data.msg);
                        $("#select_carTypeName").val(lastCarType);
                    }
                },
                error:function(jqXHR, textStatus, errorThrown){
                    var status = jqXHR.status;//404,500等
                    var text = jqXHR.statusText;//404对应的Not found,500对应的error
                    alert("加载车型信息失败：" + status + "  " + text);
                    $("#select_carTypeName").val(lastCarType);
                }
            })
        }
    }
}

//根据方案编号查询线路上所有在库物料、方案中被选中的在库物料
function findAll() {
    var billNumber=$("#billNumber").html();
    if(billNumber==""){
        alert("没有装载方案编号，无法出库");
        window.close();
    }else {
        //显示加载提示信息
        $("#div_loading").css("display", "block");
        //后台查询
        $.ajax({
            url: 'warehouseCacheByBillnumber',
            type: 'post',
            data: {'billNumber':billNumber},
            dataType:'json',
            success: function (data) {
                if(data.code==0){
                    //选中的物料信息
                    var takeList=data.data.takeList;
                    for(var i=0;i<takeList.length;i++){
                        var str="<tr><td style='display: none'>"+takeList[i].id+"</td>" +
                            "<td>"+takeList[i].good.goodcode+"</td>" +
                            "<td>"+takeList[i].good.goodname+"</td>" +
                            "<td>"+takeList[i].good.supplier.suppliercode+"</td>" +
                            "<td>"+takeList[i].good.supplier.suppliername+"</td>" +
                            "<td><a href='#' onclick='updateCarHeight(this)'>"+takeList[i].carheight+"</a></td>" +
                            "<td>"+takeList[i].carwidth+"</td>" +
                            "<td>"+takeList[i].good.oneboxcount+"</td>" +
                            "<td>"+takeList[i].boxcount+"</td>" +
                            "<td><a href='#' onclick='updateCount(this)'>"+takeList[i].count+"</a></td>" +
                            "<td>"+takeList[i].location+"</td>" +
                            "<td>"+takeList[i].layers+"</td>" +
                            "<td>"+takeList[i].row+"</td>" +
                            "<td>"+takeList[i].length+"</td>" +
                            "<td>"+takeList[i].volume+"</td>" +
                            "<td>"+takeList[i].weight+"</td>" +
                            "<td>"+takeList[i].side+"</td></tr>";
                        $("#table_data2").append(str);
                    }
                    //所选线路上的物料
                    var cacheList=data.data.cacheList;
                    for(var i=0;i<cacheList.length;i++){
                        //在库物料
                        var cache=cacheList[i].warehouseCache;
                        //复选框是否选中
                        var checked=cacheList[i].checked;
                        var check="";
                        if(checked){
                            check="<input type='checkbox' checked='checked' onclick='checkboxSelf(this)'>";
                        }else {
                            check="<input type='checkbox' onclick='checkboxSelf(this)'>";
                        }
                        //可选数量=数量-方案数量
                        var unsureCount=parseInt(cache.count)-parseInt(cache.plancount);
                        var str="<tr><td style='display: none'>"+cache.id+"</td>" +
                            "<td>"+check+"</td>" +
                            "<td>"+cache.good.goodcode+"</td>" +
                            "<td>"+cache.good.goodname+"</td>" +
                            "<td>"+cache.good.supplier.suppliercode+"</td>" +
                            "<td>"+cache.good.supplier.suppliername+"</td>" +
                            "<td>"+cache.geelybillnumber+"</td>" +
                            "<td>"+cache.geelycount+"</td>" +
                            "<td>"+cache.batch+"</td>" +
                            "<td>"+cache.count+"</td>" +
                            "<td>"+unsureCount+"</td>" +
                            "<td>"+cache.oneboxcount+"</td>" +
                            "<td>"+cache.packstate+"</td></tr>";
                        $("#table_data").append(str);
                    }
                    //出发地、目的地
                    $("#start").html(takeList[0].startname);
                    $("#end").html(takeList[0].endname);
                    //车型
                    var carType=takeList[0].cartype;
                    //加载车型信息
                    $.ajax({
                        url: 'carType',
                        type: 'post',
                        dataType:'json',
                        success: function (datas) {
                            if(datas.code==0){
                                if(datas.data==null){
                                    alert("没有车型信息，请添加后再试");
                                    window.close();
                                }else {
                                    for(var i=0;i<datas.data.length;i++){
                                        if(carType==datas.data[i].cartypename){
                                            $("#select_carTypeName").append("<option selected='selected'>"+datas.data[i].cartypename+"</option>");
                                            $("#totalHighLength").html(parseFloat(datas.data[i].highlength)/parseFloat("1000"));
                                            $("#totalLowLength").html(parseFloat(datas.data[i].lowlength)/parseFloat("1000"));
                                            $("#input_highLength").val(datas.data[i].highlength);
                                            $("#input_highHeight").val(datas.data[i].highheight);
                                            $("#input_lowLength").val(datas.data[i].lowlength);
                                            $("#input_lowHeight").val(datas.data[i].lowheight);
                                            $("#input_carWidth").val(datas.data[i].carwidth);
                                        }else {
                                            $("#select_carTypeName").append("<option>"+datas.data[i].cartypename+"</option>")
                                        }
                                    }
                                }
                            } else {
                                alert(datas.msg);
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
                }else {
                    alert(data.msg);
                    window.close();
                }
                //隐藏加载提示信息
                $("#div_loading").css("display", "none");
            },
            error:function(jqXHR, textStatus, errorThrown){
                var status = jqXHR.status;//404,500等
                var text = jqXHR.statusText;//404对应的Not found,500对应的error
                alert("查询失败：" + status + "  " + text);
                window.close();
            }
        });
    }
}

//选择在库记录操作
function checkboxSelf(check){
    //获取选择的缺件计划的id
    var warehouseCacheId=$(check).parent().parent().find("td:eq(0)").text();
    //可选数量
    var unsureCount=$(check).parent().parent().find("td:eq(10)").text();
    //获取当前选择状态
    if($(check).is(':checked')){
        //当前操作时选中复选框，那么就要获取结果放入取货计划
        var carType=$("#select_carTypeName").val();
        var lowHeight=$("#input_lowHeight").val();
        var carWidth=$("#input_carWidth").val();
        //正整数
        var str=/^[1-9]{1}[0-9]{0,10}$/;
        if(carType==""){
            alert("请选择车型");
            $(check).prop("checked",false);
        }else if(!str.test(lowHeight)){
            alert("请填入低板高度(mm)");
            $("#input_lowHeight").focus();
            $(check).prop("checked",false);
        }else if(!str.test(carWidth)){
            alert("请填入车宽(mm)");
            $("#input_carWidth").focus();
            $(check).prop("checked",false);
        }else {
            //收容数
            var oneBoxCount=$(check).parent().parent().find("td:eq(11)").text();
            $("#chooseModal_warehouseCacheId").val(warehouseCacheId);
            $("#chooseModal_unsureCount").val(unsureCount);
            $("#chooseModal_oneBoxCount").val(oneBoxCount);
            $("#chooseModal_count").val("");
            $("#chooseModal_boxCount").val("");
            $("#chooseModal_odd").val("");
            //打开模态框
            $("#chooseModal").modal("show");
            $("#chooseModal").on("shown.bs.modal",function (){
                $("#chooseModal_count").focus();
            })
        }
    }else {
        var billNumber=$("#billNumber").html();
        //当前操作是取消选择，就把物料从方案中去掉
        $.ajax({
            url: 'warehouseTakeDelete',
            type: 'post',
            data: {'warehouseCacheId':warehouseCacheId,'billNumber':billNumber},
            dataType:'json',
            success: function (data) {
                if(data.code==0){
                    var length=$("#table_data2 tr").length;
                    alert(length);
                    if(length>0){
                        var chooseCount=0;
                        $("#table_data2 tr").each(function (){
                            var id=$(this).find("td:eq(0)").text();
                            if(id==warehouseCacheId){
                                chooseCount=$(this).find("td:eq(9)").text();
                                $(this).remove();
                            }
                        })
                        infoSum();
                        //修改在库记录的可选数量
                        unsureCount=parseInt(unsureCount)+parseInt(chooseCount);
                        $(check).parent().parent().find("td:eq(10)").html(unsureCount);
                    }else {
                        alert("装载方案无内容，请重新生成");
                        window.close();
                    }
                }else {
                    alert(data.msg);
                    //如果出错，不可以取消选中
                    $(check).prop("checked",true);
                }
                //隐藏加载提示信息
                $("#div_loading").css("display", "none");
            },
            error:function(jqXHR, textStatus, errorThrown){
                var status = jqXHR.status;//404,500等
                var text = jqXHR.statusText;//404对应的Not found,500对应的error
                alert("操作失败：" + status + "  " + text);
                //如果出错，不可以取消选中
                $(check).prop("checked",true);
                $("#div_loading").css("display","none");
            }
        });

    }
}

//填入数量模态框中的取消按钮
function chooseCancel(){
    var warehouseCacheId=$("#chooseModal_warehouseCacheId").val();
    $("#table_data tr").each(function () {
        var id=$(this).find("td:eq(0)").text();
        if(warehouseCacheId==id){
            var inputList=$(this).find("input");
            for(var i=0;i<inputList.length;i++){
                $(inputList[i]).prop("checked",false);
            }
        }
    })
    $("#chooseModal").modal("hide");
}

//选择缺件计划，填入取货数量后，计算取货数量的相关信息
function choose(){
    //数量
    var count=$("#chooseModal_count").val();
    //未确认数量
    var unsureCount=$("#chooseModal_unsureCount").val();
    var str=/^[1-9]{1}[0-9]{0,10}$/;
    if(!str.test(count)){
        $("#chooseModal_count").focus();
    }else if(parseInt(count)>parseInt(unsureCount)){
        alert("填入的取货数量不可大于最大可选数量");
        $("#chooseModal_count").focus();
    }else {
        //在库id
        var warehouseCacheId=$("#chooseModal_warehouseCacheId").val();
        var lowHeight=$("#input_lowHeight").val();
        var carWidth=$("#input_carWidth").val();
        //数量
        var chooseCount=$("#chooseModal_count").val();
        //收容数
        var oneBoxCount=$("#chooseModal_oneBoxCount").val();
        var boxCount=$("#chooseModal_boxCount").val();
        var billNumber=$("#billNumber");
        //显示加载提示信息
        $("#div_loading").css("display", "block");
        //后台查询
        $.ajax({
            url: 'warehouseTakeAdd',
            type: 'post',
            data: {
                'billNumber':billNumber,
                'id':warehouseCacheId,
                'chooseCount':chooseCount,
                'lowHeight':lowHeight,
                'carWidth':carWidth},
            dataType:'json',
            success: function (data) {
                if(data.code==0){
                    //加载当前选中的在库记录到下面拼载表中
                    var str="<tr><td style='display: none'>"+warehouseCacheId+"</td>"+
                        "<td>"+data.data.good.goodcode+"</td>"+
                        "<td>"+data.data.good.goodname+"</td>"+
                        "<td>"+data.data.good.supplier.suppliercode+"</td>"+
                        "<td>"+data.data.good.supplier.suppliername+"</td>"+
                        "<td title='点击车高使用高板高度重新计算长度'><a href='#' onclick='updateCarHeight(this)'>"+lowHeight+"</a></td>"+
                        "<td>"+carWidth+"</td>"+
                        "<td>"+oneBoxCount+"</td>"+
                        "<td>"+boxCount+"</td>"+
                        "<td title='点击数量修改缺件计划'><a href='#' onclick='toUpdateCount(this)'>"+chooseCount+"</a></td>"+
                        "<td>"+data.data.location+"</td>"+
                        "<td>"+data.data.layers+"</td>"+
                        "<td>"+data.data.row+"</td>"+
                        "<td>"+data.data.length+"</td>"+
                        "<td>"+data.data.volume+"</td>"+
                        "<td>"+data.data.weight+"</td>"+
                        "<td>"+data.data.side+"</td></tr>";
                    $("#table_data2").prepend(str);
                    //统计总和
                    infoSum();
                    //更改在库记录的可选数量
                    $("#table_data tr").each(function (){
                        var id=$(this).find("td:eq(0)").text();
                        if(id==warehouseCacheId){
                            var oldCount=$(this).find("td:eq(10)").text();
                            $(this).find("td:eq(10)").html(parseInt(oldCount)-parseInt(chooseCount));
                        }
                    })
                    $("#chooseModal").modal("hide");
                }else {
                    alert(data.msg);
                    //如果出错，不可以被选中
                    $("#table_data tr").each(function () {
                        var id=$(this).find("td:eq(0)").text();
                        if(warehouseCacheId==id){
                            var inputList=$(this).find("input");
                            for(var i=0;i<inputList.length;i++){
                                $(inputList[i]).prop("checked",false);
                            }
                        }
                    })
                }
                //隐藏加载提示信息
                $("#div_loading").css("display", "none");
            },
            error:function(jqXHR, textStatus, errorThrown){
                var status = jqXHR.status;//404,500等
                var text = jqXHR.statusText;//404对应的Not found,500对应的error
                alert("操作失败：" + status + "  " + text);
                //如果出错，不可以被选中
                $("#table_data tr").each(function () {
                    var id=$(this).find("td:eq(0)").text();
                    if(warehouseCacheId==id){
                        var inputList=$(this).find("input");
                        for(var i=0;i<inputList.length;i++){
                            $(inputList[i]).prop("checked",false);
                        }
                    }
                })
                $("#div_loading").css("display","none");
            }
        });
    }
}

//-------------------------修改被选中的数量---------------------------
function toUpdateCount(a){
    var warehouseCacheId=a.parentNode.parentNode.childNodes[0].innerHTML;
    var oneBoxCount=a.parentNode.parentNode.childNodes[7].innerHTML;
    var date=$("#input_takeDate").val();
    var lowHeight=$("#input_lowHeight").val();
    var carWidth=$("#input_carWidth").val();
    if(date==""){
        alert("请选择取货时间");
    }else if(lowHeight==""){
        alert("请填入低板高(mm)");
        $("#input_lowHeight").focus();
    }else if(carWidth==""){
        alert("请填入车宽(mm)");
        $("#input_carWidth").focus();
    }else {
        var count=$(a).parent().text();
        //获取最大可选数量
        var unsureCount=0;
        $("#table_data tr").each(function (){
            var id=$(this).find("td:eq(0)").text();
            if(id==warehouseCacheId){
                var oldCount=$(this).find("td:eq(10)").text();
                unsureCount=parseInt(count)+parseInt(oldCount);
            }
        })
        //打开模态框
        $("#updateCountModal_warehouseCacheId").val(warehouseCacheId);
        $("#updateCountModal_unsureCount").val(unsureCount);
        $("#updateCountModal_count").val(count);
        $("#updateCountModal_oneBoxCount").val(oneBoxCount);
        //箱数
        var boxCount=0;
        if(count%oneBoxCount!=0){
            boxCount=Math.ceil(count/oneBoxCount);
            $("#updateCountModal_odd").val(count%oneBoxCount);
        }else {
            boxCount=count/oneBoxCount;
            $("#updateCountModal_odd").val(0);
        }
        $("#updateCountModal_boxCount").val(boxCount);
        $("#updateCountModal").modal("show");
        $("#updateCountModal").on("shown.bs.modal",function (){
            $("#updateCountModal_count").focus();
        })
    }
}
function updateCount(){
    var warehouseCacheId=$("#updateCountModal_warehouseCacheId").val();
    var unsureCount=$("#updateCountModal_unsureCount").val();
    var oneBoxCount=$("#updateCountModal_oneBoxCount").val();
    var count=$("#updateCountModal_count").val();
    //修改数量时，应该拿记录中的车高计算
    var lowHeight=0;
    $("#table_data2 tr").each(function (){
        var id=$(this).find("td:eq(0)").text();
        if(id==warehouseCacheId){
            lowHeight=$(this).find("td:eq(5)").text();
        }
    })
    var carWidth=$("#input_carWidth").val();
    //正整数
    var str=/^[1-9]{1}[0-9]{0,10}$/;
    if(!str.test(count)){
        $("#updateCountModal_count").focus();
    }else if(parseInt(count)>parseInt(unsureCount)){
        alert("填入的数量不可大于最大可选数量");
        $("#updateCountModal_count").focus();
    }else {
        var boxCount=0;
        if(parseInt(count)%parseInt(oneBoxCount)==0){
            boxCount=count/oneBoxCount;
        }else {
            boxCount=Math.ceil(count/oneBoxCount);
        }
        //显示加载提示信息
        $("#div_loading").css("display", "block");
        //后台查询
        $.ajax({
            url: 'warehouseCacheCalculate',
            type: 'post',
            data: {
                'id':warehouseCacheId,
                'chooseCount':count,
                'lowHeight':lowHeight,
                'carWidth':carWidth},
            dataType:'json',
            success: function (data) {
                if(data.code==0){
                    //加载当前选中的在库记录到下面拼载表中
                    $("#table_data2 tr").each(function (){
                        var id=$(this).find("td:eq(0)").text();
                        if(id==warehouseCacheId){
                            $(this).find("td:eq(8)").html(boxCount);
                            $(this).find("td:eq(9)").html("<a href='#' onclick='toUpdateCount(this)'>"+count+"</a>");
                            $(this).find("td:eq(10)").html(data.data.location);
                            $(this).find("td:eq(11)").html(data.data.layers);
                            $(this).find("td:eq(12)").html(data.data.row);
                            $(this).find("td:eq(13)").html(data.data.length);
                            $(this).find("td:eq(14)").html(data.data.volume);
                            $(this).find("td:eq(15)").html(data.data.weight);
                            $(this).find("td:eq(16)").html(data.data.side);
                        }
                    })
                    //统计总和
                    infoSum();
                    //更改在库记录的可选数量
                    $("#table_data tr").each(function (){
                        var id=$(this).find("td:eq(0)").text();
                        if(id==warehouseCacheId){
                            $(this).find("td:eq(10)").html(parseInt(unsureCount)-parseInt(count));
                        }
                    })
                    $("#updateCountModal").modal("hide");
                }else {
                    alert(data.msg);
                }
                //隐藏加载提示信息
                $("#div_loading").css("display", "none");
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

//------------------------修改被选中的车身高度----------------------
function updateCarHeight(a){
    var warehouseCacheId=a.parentNode.parentNode.childNodes[0].innerHTML;
    var count=$(a.parentNode.parentNode.childNodes[9]).text();
    var date=$("#input_takeDate").val();
    var carHeight=$(a.parentNode.parentNode.childNodes[5]).text();
    var highHeight=$("#input_highHeight").val();
    var carWidth=$("#input_carWidth").val();
    //正整数
    var str=/^[1-9]{1}[0-9]{0,10}$/;
    if(date==""){
        alert("请选择取货时间");
    }else if(highHeight==""){
        alert("请填入高板高(mm)");
        $("#input_highHeight").focus();
    }else if(!str.test(highHeight)){
        alert("高板高(mm)必须为正整数");
        $("#input_highHeight").focus();
    }else if(highHeight==carHeight){
        //填入的高板高和当前车高一样，无需任何操作
    }else if(carWidth==""){
        alert("请填入车宽(mm)");
        $("#input_carWidth").focus();
    }else {
        //显示加载提示信息
        $("#div_loading").css("display", "block");
        //后台查询
        $.ajax({
            url: 'warehouseCacheCalculate',
            type: 'post',
            data: {
                'id':warehouseCacheId,
                'chooseCount':count,
                'lowHeight':highHeight,
                'carWidth':carWidth},
            dataType:'json',
            success: function (data) {
                if(data.code==0){
                    //加载当前选中的在库记录到下面拼载表中
                    $("#table_data2 tr").each(function (){
                        var id=$(this).find("td:eq(0)").text();
                        if(id==warehouseCacheId){
                            $(this).find("td:eq(5)").html("<a href='#' onclick='updateCarHeight(this)'>"+highHeight+"</a>");
                            $(this).find("td:eq(10)").html(data.data.location);
                            $(this).find("td:eq(11)").html(data.data.layers);
                            $(this).find("td:eq(12)").html(data.data.row);
                            $(this).find("td:eq(13)").html(data.data.length);
                            $(this).find("td:eq(14)").html(data.data.volume);
                            $(this).find("td:eq(15)").html(data.data.weight);
                            $(this).find("td:eq(16)").html(data.data.side);
                        }
                    })
                    //统计总和
                    infoSum();
                }else {
                    alert(data.msg);
                }
                //隐藏加载提示信息
                $("#div_loading").css("display", "none");
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

//计算长度、体积、重量之和
function infoSum(){
    var lowHeight=$("#input_lowHeight").val();
    var lowLength=0;
    var highLength=0;
    var volume=0;
    var weight=0;
    $("#table_data2 tr").each(function () {
        var high=$(this).find("td:eq(5)").text();
        var length=$(this).find("td:eq(13)").text();
        if(high==lowHeight){
            lowLength=parseFloat(lowLength)+parseFloat(length);
        }else {
            highLength=parseFloat(highLength)+parseFloat(length);
        }
        volume=parseFloat(volume)+parseFloat($(this).find("td:eq(14)").text());
        weight=parseFloat(weight)+parseFloat($(this).find("td:eq(15)").text());
    })
    $("#span_highLengthTotal").html(highLength.toFixed(2));
    var totalHighLength=$("#totalHighLength").html();
    if(parseFloat(highLength.toFixed(2))>parseFloat(totalHighLength)){
        $("#span_highLengthTotal").css("color","red");
    }else {
        $("#span_highLengthTotal").removeAttr("style");
    }
    $("#span_lowLengthTotal").html(lowLength.toFixed(2));
    var totalLowLength=$("#totalLowLength").html();
    if(parseFloat(lowLength.toFixed(2))>parseFloat(totalLowLength)){
        $("#span_lowLengthTotal").css("color","red");
    }else {
        $("#span_lowLengthTotal").removeAttr("style");
    }
    $("#span_volume").html(volume.toFixed(2));
    $("#span_weight").html(weight.toFixed(2));
}

//-----------------------生成在途运输单------------------
function planTakeAdd(){
    var startId=$("#select_startId").val();
    var endId=$("#select_endId").val();
    var endType=$("#select_endType").val();
    var date=$("#input_takeDate").val();
    var carType=$("#select_carTypeName").val();
    var highLength=$("#input_highLength").val();
    var highHeight=$("#input_highHeight").val();
    var lowLength=$("#input_lowLength").val();
    var lowHeight=$("#input_lowHeight").val();
    var carWidth=$("#input_carWidth").val();
    var infos="";
    //缺件计划信息:计划id,车高mm,数量,箱数,长度,体积,重量
    $("#table_data2 tr").each(function (){
        var id=$(this).find("td:eq(0)").text();
        var carHeight=$(this).find("td:eq(5)").text();
        var count=$(this).find("td:eq(9)").text();
        var boxCount=$(this).find("td:eq(8)").text();
        var length=$(this).find("td:eq(13)").text();
        var volume=$(this).find("td:eq(14)").text();
        var weight=$(this).find("td:eq(15)").text();
        infos+=";"+id+","+carHeight+","+count+","+boxCount+","+length+","+volume+","+weight;
    })
    if(infos!=""){
        var tips=confirm("是否确定生成取货计划？");
        if(tips){
            infos=infos.substring(1);
            $("#div_loading").css("display", "block");
            //后台
            $.ajax({
                url: 'warehouseTakeAdd',
                type: 'post',
                data: {
                    'startId':startId,
                    'endId':endId,
                    'endType':endType,
                    'date':date,
                    'carType':carType,
                    'highLength':highLength,
                    'highHeight':highHeight,
                    'lowLength':lowLength,
                    'lowHeight':lowHeight,
                    'carWidth':carWidth,
                    'goodInfos':infos},
                dataType:'json',
                success: function (data) {
                    if(data.code==0){
                        findAll();
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
