
//-------------------------------页面加载执行--------------------------------------
$(document).ready(function () {
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
                        $("#select_carType").append("<option>"+data.data[i].cartypename+"</option>")
                    }
                    //加载计划信息
                    loadPlan();
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

    //选中计划后，弹出填入数量的模态框中填入数量输入框的按键事件，自动计算箱数
    $("#chooseModal_count").keyup(function (e){
        var count=$("#chooseModal_count").val();
        //未确认数量
        var unsureCount=$("#chooseModal_unsureCount").val();
        var str=/^[1-9]{1}[0-9]{0,10}$/;
        if(!str.test(count)){
            $("#chooseModal_count").val("");
            $("#chooseModal_count").focus();
            $("#chooseModal_boxCount").val("");
            $("#chooseModal_odd").val("");
        }else if(parseInt(count)>parseInt(unsureCount)){
            alert("填入的取货数量不可大于可选数量");
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

    //选中计划后，弹出填入数量的模态框中填入箱数输入框的按键事件，自动计算数量
    $("#chooseModal_boxCount").keyup(function (e){
        var boxCount=$("#chooseModal_boxCount").val();
        //未确认数量
        var unsureBoxCount=$("#chooseModal_unsureBoxCount").val();
        var str=/^[1-9]{1}[0-9]{0,10}$/;
        if(!str.test(boxCount)){
            $("#chooseModal_boxCount").val("");
            $("#chooseModal_boxCount").focus();
            $("#chooseModal_count").val("");
            $("#chooseModal_odd").val("");
        }else if(parseInt(boxCount)>parseInt(unsureBoxCount)){
            alert("填入的箱数不可大于可选箱数");
            $("#chooseModal_boxCount").focus();
        }else {
            var oneBoxCount=$("#chooseModal_oneBoxCount").val();
            var count=oneBoxCount*boxCount;
            $("#chooseModal_odd").val("0");
            $("#chooseModal_count").val(count);
        }
    })

    //修改数量的模态框中填入数量输入框按键事件，自动计算箱数
    $("#updateCountModal_count").keyup(function (e){
        var count=$("#updateCountModal_count").val();
        var str=/^[1-9]{1}[0-9]{0,10}$/;
        if(!str.test(count)){
            $("#updateCountModal_count").val("");
            $("#updateCountModal_count").focus();
            $("#updateCountModal_boxCount").val("");
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

    //修改箱数的模态框中填入数量输入框按键事件，自动计算数量
    $("#updateBoxCountModal_boxCount").keyup(function (e){
        var boxCount=$("#updateBoxCountModal_boxCount").val();
        var str=/^[1-9]{1}[0-9]{0,10}$/;
        if(!str.test(boxCount)){
            $("#updateBoxCountModal_boxCount").val("");
            $("#updateBoxCountModal_boxCount").focus();
            $("#updateBoxCountModal_count").val("");
        }else{
            var oneBoxCount=$("#updateBoxCountModal_oneBoxCount").val();
            var count=oneBoxCount*boxCount
            $("#updateBoxCountModal_count").val(count);
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

//加载计划信息
function loadPlan(){
    var planNumbers=$("#planNumbers").html();
    if(planNumbers==""){
        alert("无取货计划需要拼拆操作");
        window.close();
    }else {
        $("#span_highLengthTotal").html("");
        $("#span_lowLengthTotal").html("");
        $("#span_volume").html("");
        $("#span_weight").html("");
        $("#table_data").html("");
        $("#table_data2").html("");
        $("#div_loading").css("display","block");
        $.ajax({
            url: 'planTakeByNumbers',
            type: 'post',
            data:{'planNumbers':planNumbers},
            dataType:'json',
            success: function (data) {
                if(data.code==0){
                    if(data.data==null){
                        //已经全部拼拆完成
                        alert("所有取货计划拼拆完成");
                        window.close();
                    }else {
                        for(var i=0;i<data.data.length;i++){
                            var goodcode="";
                            var goodname="";
                            var suppliercode="";
                            var suppliername="";
                            var oneBoxCount="";
                            if(data.data[i].good!=null){
                                goodcode=data.data[i].good.goodcode;
                                goodname=data.data[i].good.goodname;
                                oneBoxCount=data.data[i].good.oneboxcount;
                                if(data.data[i].good.supplier!=null){
                                    suppliercode=data.data[i].good.supplier.suppliercode;
                                    suppliername=data.data[i].good.supplier.suppliername;
                                }
                            }
                            var str="<tr><td style='display: none'>"+data.data[i].id+"</td>"+
                                "<td><input type='checkbox' name='sort' onclick='checkboxSelf(this)'></td>"+
                                "<td>" + (i+1) +"</td>"+
                                "<td>" + data.data[i].plannumber +"</td>"+
                                "<td>" + goodcode +"</td>"+
                                "<td>" + goodname +"</td>"+
                                "<td>" + suppliercode +"</td>"+
                                "<td>" + suppliername +"</td>"+
                                "<td>" + oneBoxCount +"</td>"+
                                "<td>" + data.data[i].count +"</td>"+
                                "<td>" + data.data[i].boxcount +"</td>"+
                                "<td>" + data.data[i].cartype +"</td>"+
                                "<td>" + data.data[i].length +"</td>"+
                                "<td>" + data.data[i].volume +"</td>"+
                                "<td>" + data.data[i].weight +"</td>"+
                                "<td>" + data.data[i].carheight +"</td>"+
                                "<td>" + data.data[i].carwidth +"</td></tr>";
                            $("#table_data").append(str);
                        }
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

//选择车型
function chooseCarType(){
    var carTypeName=$("#select_carType").val();
    var lastCarType=$("#carType").html();
    //如果有计划，就无法变动
    if($("#table_data2 tr").length>0){
        alert("有选中的计划，无法变更车型");
        $("#select_carType").val(lastCarType);
    }else {
        //如果没有计划，选择了空
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
                        $("#select_carType").val(lastCarType);
                    }
                },
                error:function(jqXHR, textStatus, errorThrown){
                    var status = jqXHR.status;//404,500等
                    var text = jqXHR.statusText;//404对应的Not found,500对应的error
                    alert("加载车型信息失败：" + status + "  " + text);
                    $("#select_carType").val(lastCarType);
                }
            })
        }
    }
}

//选择缺件计划操作
function checkboxSelf(check){
    var id=$(check).parent().parent().find("td:eq(0)").text();
    //获取当前选择状态
    if($(check).is(':checked')){
        //物料编号
        var goodCode=$(check).parent().parent().find("td:eq(4)").text();
        //物料名称
        var goodName=$(check).parent().parent().find("td:eq(5)").text();
        //供应商编号
        var supplierCode=$(check).parent().parent().find("td:eq(6)").text();
        //供应商名称
        var supplierName=$(check).parent().parent().find("td:eq(7)").text();
        //数量
        var count=$(check).parent().parent().find("td:eq(9)").text();
        //箱数
        var boxCount=$(check).parent().parent().find("td:eq(10)").text();
        //收容数
        var oneBoxCount=$(check).parent().parent().find("td:eq(8)").text();
        //序号
        var sort=$(check).parent().parent().find("td:eq(2)").text();
        //看是否选择车型
        var carType=$("#select_carType").val();
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
            $("#chooseModal_sort").val(sort);
            $("#chooseModal_goodCode").val(goodCode);
            $("#chooseModal_goodName").val(goodName);
            $("#chooseModal_supplierCode").val(supplierCode);
            $("#chooseModal_supplierName").val(supplierName);
            $("#chooseModal_unsureCount").val(count);
            $("#chooseModal_count").val(count);
            $("#chooseModal_unsureBoxCount").val(boxCount);
            $("#chooseModal_boxCount").val(boxCount);
            $("#chooseModal_oneBoxCount").val(oneBoxCount);
            $("#chooseModal_odd").val(0);
            $("#chooseModal_id").val(id);
            $("#chooseModal").modal("show");
            $("#chooseModal").on("shown.bs.modal",function (){
                $("#chooseModal_count").focus();
            })
        }
    }else {
        //去掉装载表中行
        $("#table_data2 tr").each(function () {
            var td2id=$(this).find("td:eq(0)").text();
            if(td2id==id){
                //从下面拼载表中去掉该计划
                $(this).remove();
            }
        })
        //3.统计总和
        infoSum();
    }
}

//选择缺件计划，填入取货数量后，计算取货数量的相关信息
function choose(){
    var goodCode=$("#chooseModal_goodCode").val();
    var goodName=$("#chooseModal_goodName").val();
    var supplierCode=$("#chooseModal_supplierCode").val();
    var supplierName=$("#chooseModal_supplierName").val();
    var count=$("#chooseModal_count").val();
    var boxCount=$("#chooseModal_boxCount").val();
    var oneBoxCount=$("#chooseModal_oneBoxCount").val();
    var id=$("#chooseModal_id").val();
    var lowHeight=$("#input_lowHeight").val();
    var carWidth=$("#input_carWidth").val();
    var sort=$("#chooseModal_sort").val();
    var str=/^[1-9]{1}[0-9]{0,10}$/;
    if(!str.test(count)){
        $("#chooseModal_count").focus();
    }else if(!str.test(boxCount)){
        $("#chooseModal_boxCount").focus();
    }else {
        //显示加载提示信息
        $("#div_loading").css("display", "block");
        //后台查询
        $.ajax({
            url: 'planTakeCalculate',
            type: 'post',
            data: {
                'id':id,
                'count':count,
                'boxCount':boxCount,
                'lowHeight':lowHeight,
                'carWidth':carWidth},
            dataType:'json',
            success: function (data) {
                if(data.code==0){
                    var str="<tr><td style='display: none'>"+id+"</td>"+
                        "<td>"+sort+"</td>"+
                        "<td>"+goodCode+"</td>"+
                        "<td>"+goodName+"</td>"+
                        "<td>"+supplierCode+"</td>"+
                        "<td>"+supplierName+"</td>"+
                        "<td title='点击车高使用高板高度重新计算长度'><a href='#' onclick='updateCarHeight(this)'>"+lowHeight+"</a></td>"+
                        "<td>"+carWidth+"</td>"+
                        "<td>"+oneBoxCount+"</td>"+
                        "<td title='点击箱数修改缺件计划'><a href='#' onclick='toUpdateBoxcount(this)'>"+boxCount+"</a></td>"+
                        "<td title='点击数量修改缺件计划'><a href='#' onclick='toUpdateCount(this)'>"+count+"</a></td>"+
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
                    //隐藏模态框
                    $("#chooseModal").modal("hide");
                }else {
                    alert(data.msg);
                    //如果出错，不可以被选中
                    $("#table_data tr").each(function () {
                        var tdid=$(this).find("td:eq(0)").text();
                        if(id==tdid){
                            var inputList=$(this).find("input");
                            $(inputList).prop("checked",false);
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
                    var tdid=$(this).find("td:eq(0)").text();
                    if(id==tdid){
                        var inputList=$(this).find("input");
                        $(inputList).prop("checked",false);
                    }
                })
                $("#div_loading").css("display","none");
            }
        });
    }
}
//---------------------修改被选中的计划的箱数--------------------------
function toUpdateBoxcount(a){
    var id=a.parentNode.parentNode.childNodes[0].innerHTML;
    var oneBoxCount=$(a).parent().parent().find("td:eq(8)").text();
    var count=$(a).parent().parent().find("td:eq(10)").text();
    var lowHeight=$("#input_lowHeight").val();
    var carWidth=$("#input_carWidth").val();
    if(lowHeight==""){
        alert("请填入低板高(mm)");
        $("#input_lowHeight").focus();
    }else if(carWidth==""){
        alert("请填入车宽(mm)");
        $("#input_carWidth").focus();
    }else {
        var boxCount=$(a).parent().text();
        //打开模态框
        $("#updateBoxCountModal_id").val(id);
        $("#updateBoxCountModal_oneBoxCount").val(oneBoxCount);
        $("#updateBoxCountModal_boxCount").val(boxCount);
        $("#updateBoxCountModal_count").val(count);
        $("#updateBoxCountModal").modal("show");
        $("#updateBoxCountModal").on("shown.bs.modal",function (){
            $("#updateBoxCountModal_boxCount").focus();
        })
    }
}
function updateBoxcount(){
    var id=$("#updateBoxCountModal_id").val();
    var boxCount=$("#updateBoxCountModal_boxCount").val();
    var count=$("#updateBoxCountModal_count").val();
    var lowHeight=$("#input_lowHeight").val();
    var carWidth=$("#input_carWidth").val();
    //正整数
    var str=/^[1-9]{1}[0-9]{0,10}$/;
    if(!str.test(boxCount)){
        $("#updateBoxCountModal_boxCount").focus();
    }else {
        //后台重新计算
        $("#div_loading").css("display", "block");
        //后台查询
        $.ajax({
            url: 'planTakeCalculate',
            type: 'post',
            data: {
                'id':id,
                'count':count,
                'boxCount':boxCount,
                'lowHeight':lowHeight,
                'carWidth':carWidth},
            dataType:'json',
            success: function (data) {
                if(data.code==0){
                    //更新装载信息
                    $("#table_data2 tr").each(function () {
                        var tdid=$(this).find("td:eq(0)").text();
                        if(tdid==id){
                            //箱数
                            $($(this).find("td:eq(9)")).html("<a href='#' onclick='toUpdateBoxcount(this)'>"+boxCount+"</a>");
                            //数量
                            $($(this).find("td:eq(10)")).html("<a href='#' onclick='toUpdateCount(this)'>"+count+"</a>");
                            //占位数
                            $($(this).find("td:eq(11)")).html(data.data.location);
                            //层数
                            $($(this).find("td:eq(12)")).html(data.data.layers);
                            //排数
                            $($(this).find("td:eq(13)")).html(data.data.row);
                            //长度
                            $($(this).find("td:eq(14)")).html(data.data.length);
                            //体积
                            $($(this).find("td:eq(15)")).html(data.data.volume);
                            //重量
                            $($(this).find("td:eq(16)")).html(data.data.weight);
                        }
                    })
                    //统计总和
                    infoSum();
                    //隐藏模态框
                    $("#updateBoxCountModal").modal("hide");
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

//-------------------------修改被选中计划的数量---------------------------
function toUpdateCount(a){
    var id=a.parentNode.parentNode.childNodes[0].innerHTML;
    var oneBoxCount=$(a).parent().parent().find("td:eq(8)").text();
    var boxCount=$(a).parent().parent().find("td:eq(9)").text();
    var lowHeight=$("#input_lowHeight").val();
    var carWidth=$("#input_carWidth").val();
    if(lowHeight==""){
        alert("请填入低板高(mm)");
        $("#input_lowHeight").focus();
    }else if(carWidth==""){
        alert("请填入车宽(mm)");
        $("#input_carWidth").focus();
    }else {
        var count=$(a).parent().text();
        //打开模态框
        $("#updateCountModal_id").val(id);
        $("#updateCountModal_count").val(count);
        $("#updateCountModal_boxCount").val(boxCount);
        $("#updateCountModal_oneBoxCount").val(oneBoxCount);
        $("#updateCountModal").modal("show");
        $("#updateCountModal").on("shown.bs.modal",function (){
            $("#updateCountModal_count").focus();
        })
    }
}
function updateCount(){
    var id=$("#updateCountModal_id").val();
    var count=$("#updateCountModal_count").val();
    var boxCount=$("#updateCountModal_boxCount").val();
    var lowHeight=$("#input_lowHeight").val();
    var carWidth=$("#input_carWidth").val();
    //正整数
    var str=/^[1-9]{1}[0-9]{0,10}$/;
    if(!str.test(count)){
        $("#updateCountModal_count").focus();
    }else {
        //后台重新计算
        $("#div_loading").css("display", "block");
        //后台查询
        $.ajax({
            url: 'planTakeCalculate',
            type: 'post',
            data: {
                'id':id,
                'count':count,
                'boxCount':boxCount,
                'lowHeight':lowHeight,
                'carWidth':carWidth},
            dataType:'json',
            success: function (data) {
                if(data.code==0){
                    //更新装载信息
                    $("#table_data2 tr").each(function () {
                        var tdid=$(this).find("td:eq(0)").text();
                        if(tdid==id){
                            //箱数
                            $($(this).find("td:eq(9)")).html("<a href='#' onclick='toUpdateBoxcount(this)'>"+boxCount+"</a>");
                            //数量
                            $($(this).find("td:eq(10)")).html("<a href='#' onclick='toUpdateCount(this)'>"+count+"</a>");
                            //占位数
                            $($(this).find("td:eq(11)")).html(data.data.location);
                            //层数
                            $($(this).find("td:eq(12)")).html(data.data.layers);
                            //排数
                            $($(this).find("td:eq(13)")).html(data.data.row);
                            //长度
                            $($(this).find("td:eq(14)")).html(data.data.length);
                            //体积
                            $($(this).find("td:eq(15)")).html(data.data.volume);
                            //重量
                            $($(this).find("td:eq(16)")).html(data.data.weight);
                        }
                    })
                    //统计总和
                    infoSum();
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

//------------------------修改被选中计划的车身高度----------------------
function updateCarHeight(a){
    var id=a.parentNode.parentNode.childNodes[0].innerHTML;
    var count=$(a.parentNode.parentNode.childNodes[10]).text();
    var boxCount=$(a.parentNode.parentNode.childNodes[9]).text();
    var carHeight=$(a.parentNode.parentNode.childNodes[6]).text();
    var highHeight=$("#input_highHeight").val();
    var carWidth=$("#input_carWidth").val();
    //正整数
    var str=/^[1-9]{1}[0-9]{0,10}$/;
    if(highHeight==""){
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
        //后台修改
        $("#div_loading").css("display", "block");
        //后台查询
        $.ajax({
            url: 'planTakeCalculate',
            type: 'post',
            data: {
                'id':id,
                'count':count,
                'boxCount':boxCount,
                'lowHeight':highHeight,
                'carWidth':carWidth},
            dataType:'json',
            success: function (data) {
                if(data.code==0){
                    //更新装载信息
                    $("#table_data2 tr").each(function () {
                        var tdid=$(this).find("td:eq(0)").text();
                        if(id==tdid){
                            //车高
                            $($(this).find("td:eq(6)")).html("<a href='#' onclick='updateCarHeight(this)'>"+highHeight+"</a>");
                            //车宽
                            $($(this).find("td:eq(7)")).html(carWidth);
                            //占位数
                            $($(this).find("td:eq(11)")).html(data.data.location);
                            //层数
                            $($(this).find("td:eq(12)")).html(data.data.layers);
                            //排数
                            $($(this).find("td:eq(13)")).html(data.data.row);
                            //长度
                            $($(this).find("td:eq(14)")).html(data.data.length);
                            //体积
                            $($(this).find("td:eq(15)")).html(data.data.volume);
                            //重量
                            $($(this).find("td:eq(16)")).html(data.data.weight);
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
                alert("修改失败：" + status + "  " + text);
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
        var high=$(this).find("td:eq(6)").text();
        var length=$(this).find("td:eq(14)").text();
        if(high==lowHeight){
            lowLength=parseFloat(lowLength)+parseFloat(length);
        }else {
            highLength=parseFloat(highLength)+parseFloat(length);
        }
        volume=parseFloat(volume)+parseFloat($(this).find("td:eq(15)").text());
        weight=parseFloat(weight)+parseFloat($(this).find("td:eq(16)").text());
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

//-----------------------生成取货计划------------------
function planTakeAdd(){
    var carType=$("#select_carType").val();
    var highLength=$("#input_highLength").val();
    var highHeight=$("#input_highHeight").val();
    var lowLength=$("#input_lowLength").val();
    var lowHeight=$("#input_lowHeight").val();
    var carWidth=$("#input_carWidth").val();
    var infos="";
    //缺件计划信息:计划id,车高mm,数量,箱数,长度,体积,重量
    $("#table_data2 tr").each(function (){
        var id=$(this).find("td:eq(0)").text();
        var carHeight=$(this).find("td:eq(6)").text();
        var count=$(this).find("td:eq(10)").text();
        var boxCount=$(this).find("td:eq(9)").text();
        var length=$(this).find("td:eq(14)").text();
        var volume=$(this).find("td:eq(15)").text();
        var weight=$(this).find("td:eq(16)").text();
        infos+=";"+id+","+carHeight+","+count+","+boxCount+","+length+","+volume+","+weight;
    })
    if(infos!=""){
        var tips=confirm("是否确定生成取货计划？");
        if(tips){
            infos=infos.substring(1);
            $("#div_loading").css("display", "block");
            //后台
            $.ajax({
                url: 'planTakeAddRepeat',
                type: 'post',
                data: {
                    'carType':carType,
                    'highLength':highLength,
                    'highHeight':highHeight,
                    'lowLength':lowLength,
                    'lowHeight':lowHeight,
                    'carWidth':carWidth,
                    'planTakeInfos':infos},
                dataType:'json',
                success: function (data) {
                    if(data.code==0){
                        loadPlan();
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
