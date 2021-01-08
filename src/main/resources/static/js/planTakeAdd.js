
//-------------------------------页面加载执行--------------------------------------
$(document).ready(function () {
    $("#typeModal").modal("show");
    //加载区域
    $.ajax({
        url: 'currentArea',
        type: 'post',
        dataType:'json',
        success: function (data) {
            if(data.code==0){
                for(var i=0;i<data.data.length;i++){
                    $("#select_startId").append("<option value='"+data.data[i].id+"'>"+data.data[i].areaname+"</option>")
                }
            } else {
                alert(data.msg);
                window.close();
            }
        },
        error:function(jqXHR, textStatus, errorThrown){
            var status = jqXHR.status;//404,500等
            var text = jqXHR.statusText;//404对应的Not found,500对应的error
            alert("加载出发地区域信息失败：" + status + "  " + text);
            window.close();
        }
    });

    //加载日期插件，并防止选择计划后修改日期
    laydate.render({
        elem: '#input_takeDate',//指定元素
        type: 'date',//日期+时间
        change: function(value, date){ //监听日期被切换
            if($("#table_data2 tr").length>0){
                alert("有选中的计划，无法变更取货日期");
                $("body").click();
            }
        },
        trigger: "click",
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

    //选中计划后，弹出填入数量的模态框中填入数量输入框的按键事件，自动计算箱数
    $("#chooseModal_count").keyup(function (e){
        var count=$("#chooseModal_count").val();
        //未确认数量
        var unsureCount=$("#chooseModal_unsureCount").val();
        var str=/^[1-9]{1}[0-9]{0,10}$/;
        if(!str.test(count)){
            $("#chooseModal_count").val("");
            $("#chooseModal_count").focus();
        }else if(parseInt(count)>parseInt(unsureCount)){
            alert("填入的取货数量不可大于未确认数量");
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
        var str=/^[1-9]{1}[0-9]{0,10}$/;
        if(!str.test(count)){
            $("#updateCountModal_count").val("");
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

//选择车型
function chooseCarType(){
    var carTypeName=$("#select_carTypeName").val();
    var lastCarType=$("#carType").html();
    var date=$("#input_takeDate").val();
    if(date==""){
        alert("请先选择取货时间");
        $("#select_carTypeName").val(lastCarType);
    }else {
        //如果有计划，就无法变动
        if($("#table_data2 tr").length>0){
            alert("有选中的计划，无法变更车型");
            $("#select_carTypeName").val(lastCarType);
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
}

//选择目的地类型，加载目的地
function chooseType(){
    var endType=$("#select_endType").val();
    if(endType=="工厂"){
        $("#type").html("区域-工厂");
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
                    $("#typeModal").modal("hide");
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
    }
    if(endType=="中转仓"){
        $("#type").html("区域-中转仓");
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
                    $("#typeModal").modal("hide");
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

//查询线路上所有缺件计划
function findAll() {
    //先清表格除旧数据
    $("#table_data").html("");
    $("#table_head").html("");
    $("#table_data2").html("");
    //清除装载信息
    $("#span_volume").html("");
    $("#span_weight").html("");
    $("#span_highLengthTotal").html("");
    $("#span_lowLengthTotal").html("");
    //清除取货日期
    $("#input_takeDate").val("");
    //清除车型信息
    $("#select_carTypeName").val("");
    $("#input_highLength").val("0");
    $("#input_highHeight").val("0");
    $("#input_lowLength").val("0");
    $("#input_lowHeight").val("0");
    $("#input_carWidth").val("0");
    $("#totalHighLength").html("0");
    $("#totalLowLength").html("0");
    $("#carType").html("");
    //获取参数
    var startId=$("#select_startId").val();
    var endId=$("#select_endId").val();
    var endType=$("#select_endType").val();
    if(startId!="0"&&endId!="0"){
        //选择开始地，目的地选择，请求后台
        //显示加载提示信息
        $("#div_loading").css("display", "block");
        //后台查询
        $.ajax({
            url: 'planCacheByRoute',
            type: 'post',
            data: {
                'startId':startId,
                'endId':endId,
                'endType':endType},
            dataType:'json',
            success: function (data) {
                if(data.code==0){
                    var dateList=data.data.dateList;
                    var head="<tr><th style='display: none'>id</th><th>物料编号</th><th>物料名称</th><th>供应商编号</th><th>供应商名称</th><th>所属工厂</th><th>上次计划</th>";
                    for(var g=0;g<dateList.length;g++){
                        head+="<th>"+dateList[g]+"</th>"
                    }
                    head+="</tr>";
                    $("#table_head").append(head);
                    var list=data.data.list;
                    //填入物料信息、计划、缺件信息
                    for (var i = 0; i < list.length; i++) {
                        var good=list[i].good;
                        var suppliercode="";
                        var suppliername="";
                        var factoryname="";
                        if(good.supplier!=null){
                            suppliercode=good.supplier.suppliercode;
                            suppliername=good.supplier.suppliername;
                            if(good.supplier.route!=null){
                                if(good.supplier.route.factory!=null){
                                    factoryname=good.supplier.route.factory.factoryname;
                                }
                            }
                        }
                        //上次计划
                        var lastPlan=list[i].lastPlan;
                        var lastPlanStr="";
                        if(lastPlan!=null){
                            lastPlanStr="取货日期:"+lastPlan.date+"<br>取货数量:"+lastPlan.count+"<br>到货日期:"+lastPlan.receivedate;
                        }
                        var str= "<tr><td style='display: none'>"+good.id+"</td>" +
                            "<td title='点击物料编号添加缺件计划'><a href='#' onclick='toAddPlan(this)'>" + good.goodcode + "</a></td>"+
                            "<td>" + good.goodname +"</td>"+
                            "<td>" + suppliercode +"</td>"+
                            "<td>" + suppliername +"</td>"+
                            "<td>" + factoryname +"</td>"+
                            "<td>" + lastPlanStr +"</td>";
                        //填入计划、缺件信息
                        var planList=list[i].planList;
                        for(var k=0;k<planList.length;k++){
                            //缺件信息
                            var shortage=planList[k].shortage;
                            if(shortage==null){
                                str+="<td></td>";
                            }else {
                                //缺件计划信息
                                var plan=planList[k].plan;
                                //是否显示红色
                                var shortageRed=planList[k].shortageRed;
                                //看是否有缺件计划信息
                                if(plan==null){
                                    //看是否是红色字体
                                    if(shortageRed){
                                        //无缺件信息，红色字体
                                        str+="<td style='color: red'>需求:"+shortage.needcount+" 结存:<a href='#' onclick='toUpdateStock(this)'>"+shortage.stock+"</a> 上次结存:"+shortage.laststock+"</td>";
                                    }else {
                                        //无缺件信息，不是红色字体
                                        str+="<td>需求:"+shortage.needcount+" 结存:<a href='#' onclick='toUpdateStock(this)'>"+shortage.stock+"</a> 上次结存:"+shortage.laststock+"</td>";
                                    }
                                }else {
                                    //缺件计划是否显示复选框
                                    var checkbox=planList[k].checkbox;
                                    //复选框
                                    var sort="";
                                    if(checkbox){
                                        //有复选框
                                        sort="<input type='checkbox' name='sort' onclick='checkboxSelf(this)'>";
                                    }
                                    //看当前单元格的背景色
                                    var bc="";
                                    if(plan.state=="未确认"){
                                        bc="rgba(164,158,145,0.61)";
                                    }else if(plan.state=="未取货"){
                                        bc="rgba(238,182,41,0.29)";
                                    }else if(plan.state=="在途"){
                                        bc="rgba(151,201,233,0.36)";
                                    }else {
                                        bc="#ffffff";
                                    }
                                    //看是否是红色字体
                                    if(shortageRed){
                                        //有缺件信息，红色字体
                                        str+="<td style='color:red;background-color: "+bc+"'>需求:"+shortage.needcount+" 结存:<a href='#' onclick='toUpdateStock(this)'>"+shortage.stock+"</a> 上次结存:"+shortage.laststock+"<br>"+sort+" ("+plan.id+")数量:"+plan.count+" 箱数:"+plan.boxcount+" MAX:"+plan.maxcount+" MIN:"+plan.mincount+" 确认数量:"+plan.surecount+" 收容数:"+good.oneboxcount+"</td>";
                                    }else {
                                        //有缺件信息，不是红色字体
                                        str+="<td style='background-color: "+bc+"'>需求:"+shortage.needcount+" 结存:<a href='#' onclick='toUpdateStock(this)'>"+shortage.stock+"</a> 上次结存:"+shortage.laststock+"<br>"+sort+" ("+plan.id+")数量:"+plan.count+" 箱数:"+plan.boxcount+" MAX:"+plan.maxcount+" MIN:"+plan.mincount+" 确认数量:"+plan.surecount+" 收容数:"+good.oneboxcount+"</td>";
                                    }
                                }
                            }
                        }
                        str+="</tr>";
                        $("#table_data").append(str);
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
                alert("查询失败：" + status + "  " + text);
                $("#div_loading").css("display","none");
            }
        });
    }
}

//选择缺件计划操作
function checkboxSelf(check){
    var text=$(check).parent().text();
    //获取选择的缺件计划的id
    var planCacheId=text.split("(")[1].split(")")[0];
    //物料id
    var goodId=$(check).parent().parent().find("td:eq(0)").text();
    //获取当前选择状态
    if($(check).is(':checked')){
        //当前操作时选中复选框，那么就要获取结果放入取货计划
        //1.看是否选择了取货日期
        var takeDate=$("#input_takeDate").val();
        //2.看是否选择了车型
        var carType=$("#select_carTypeName").val();
        //3.看是否有低板高、车宽的值大于0
        var lowHeight=$("#input_lowHeight").val();
        var carWidth=$("#input_carWidth").val();
        //正整数
        var str=/^[1-9]{1}[0-9]{0,10}$/;
        if(takeDate==""){
            alert("请先选择取货时间");
            $(check).prop("checked",false);
        }else if(carType==""){
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
            //根据计划Id查询详情
            $.ajax({
                url: 'planCacheDetail',
                type: 'post',
                data: {
                    'planCacheId':planCacheId},
                dataType:'json',
                success: function (data) {
                    if(data.code==0){
                        //填入数据
                        $("#chooseModal_planCacheId").val(planCacheId);
                        $("#chooseModal_goodId").val(goodId);
                        $("#chooseModal_unsureCount").val(parseInt(data.data.count)-parseInt(data.data.surecount));
                        $("#chooseModal_oneBoxCount").val(data.data.good.oneboxcount);
                        //清空
                        $("#chooseModal_count").val("");
                        $("#chooseModal_boxCount").val("");
                        $("#chooseModal_odd").val("");
                        //打开填入数量的模态框
                        $("#chooseModal").modal("show");
                        $("#chooseModal").on("shown.bs.modal",function (){
                            $("#chooseModal_count").focus();
                        })
                    }else {
                        alert(data.msg);
                        $(check).prop("checked",false);
                    }
                },
                error:function(jqXHR, textStatus, errorThrown){
                    var status = jqXHR.status;//404,500等
                    var text = jqXHR.statusText;//404对应的Not found,500对应的error
                    alert("操作失败：" + status + "  " + text);
                    //如果出错，不可以被选中
                    $(check).prop("checked",false);
                }
            });
        }
    }else {
        //当前操作是取消选择，不允许通过选择复选框的形式取消，只能是点击下面取货计划表中的删除按钮取消，所以此处把复选框选中
        $(check).prop("checked",true);
    }
}

//填入数量模态框中的取消按钮
function chooseCancel(){
    var goodId=$("#chooseModal_goodId").val();
    $("#table_data tr").each(function () {
        var id=$(this).find("td:eq(0)").text();
        if(goodId==id){
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
    var chooseCount=$("#chooseModal_count").val();
    //未确认数量
    var unsureCount=$("#chooseModal_unsureCount").val();
    var str=/^[1-9]{1}[0-9]{0,10}$/;
    if(!str.test(chooseCount)){
        $("#chooseModal_count").focus();
    }else if(parseInt(chooseCount)>parseInt(unsureCount)){
        alert("填入的取货数量不可大于未确认数量");
        $("#chooseModal_count").focus();
    }else {
        //计划id
        var planCacheId=$("#chooseModal_planCacheId").val();
        var lowHeight=$("#input_lowHeight").val();
        var carWidth=$("#input_carWidth").val();
        var takeDate=$("#input_takeDate").val();
        //显示加载提示信息
        $("#div_loading").css("display", "block");
        //后台查询
        $.ajax({
            url: 'planCacheChoose',
            type: 'post',
            data: {
                'planCacheId':planCacheId,
                'takeDate':takeDate,
                'chooseCount':chooseCount,
                'lowHeight':lowHeight,
                'carWidth':carWidth},
            dataType:'json',
            success: function (data) {
                if(data.code==0){
                    //加载当前选中的计划到下面拼载表中
                    var goodname="";
                    var goodcode="";
                    var suppliercode="";
                    var suppliername="";
                    var oneBoxCount="";
                    if(data.data.good!=null){
                        goodname=data.data.good.goodname;
                        goodcode=data.data.good.goodcode;
                        oneBoxCount=data.data.good.oneboxcount;
                        if(data.data.good.supplier!=null){
                            suppliercode=data.data.good.supplier.suppliercode;
                            suppliername=data.data.good.supplier.suppliername;
                        }
                    }
                    //计算箱数
                    var boxCount=0;
                    if(chooseCount%oneBoxCount==0){
                        boxCount=chooseCount/oneBoxCount;
                    }else {
                        boxCount=Math.ceil(chooseCount/oneBoxCount);
                    }
                    var str="<tr><td style='display: none'>"+planCacheId+"</td>"+
                        "<td>"+goodcode+"</td>"+
                        "<td>"+goodname+"</td>"+
                        "<td>"+suppliercode+"</td>"+
                        "<td>"+suppliername+"</td>"+
                        "<td title='点击车高使用高板高度重新计算长度'><a href='#' onclick='updateCarHeight(this)'>"+lowHeight+"</a></td>"+
                        "<td>"+carWidth+"</td>"+
                        "<td>"+oneBoxCount+"</td>"+
                        "<td>"+boxCount+"</td>"+
                        "<td title='点击数量修改取货计划'><a href='#' onclick='toUpdateCount(this)'>"+chooseCount+"</a></td>"+
                        "<td>"+data.data.location+"</td>"+
                        "<td>"+data.data.layers+"</td>"+
                        "<td>"+data.data.row+"</td>"+
                        "<td>"+data.data.length+"</td>"+
                        "<td>"+data.data.volume+"</td>"+
                        "<td>"+data.data.weight+"</td>"+
                        "<td>"+data.data.side+"</td>"+
                        "<td><button type='button' class='btn btn-xs btn-danger' onclick='chooseDelete(this)'>取消</button></td></tr>";
                    $("#table_data2").prepend(str);
                    //统计总和
                    infoSum();
                    //重新加载物料的缺件信息、缺件计划信息
                    var planList=data.data.planList;
                    //获取行
                    var tr=null;
                    $("#table_data tr").each(function (){
                        if($(this).find("td:eq(0)").text()==data.data.good.id){
                            tr=$(this);
                        }
                    })
                    for(var k=0;k<planList.length;k++){
                        //获取当前单元格的下标
                        var index=parseInt(k)+7;
                        //单元格内容
                        var str1="";
                        //先移除本单元格的样式
                        $(tr).find("td:eq('"+index+"')").removeAttr("style");
                        //缺件信息
                        var shortage=planList[k].shortage;
                        if(shortage!=null){
                            str1+="需求:"+shortage.needcount+" 结存:<a href='#' onclick='toUpdateStock(this)'>"+shortage.stock+"</a> 上次结存:"+shortage.laststock;
                        }
                        //缺件计划是否显示复选框
                        var checkbox=planList[k].checkbox;
                        if(checkbox){
                            //是否选择复选框
                            var checked=planList[k].checked;
                            if(checked){
                                str1+="<br><input type='checkbox' name='sort' onclick='checkboxSelf(this)' checked='checked'>";
                            }else {
                                str1+="<br><input type='checkbox' name='sort' onclick='checkboxSelf(this)'>";
                            }
                        }
                        //缺件计划信息
                        var plan=planList[k].plan;
                        if(plan!=null){
                            str1+=" ("+plan.id+")数量:"+plan.count+" 箱数:"+plan.boxcount+" MAX:"+plan.maxcount+" MIN:"+plan.mincount+" 确认数量:"+plan.surecount+" 收容数:"+oneBoxCount;
                            //有缺件计划才生成背景色
                            if(plan.state=="未确认"){
                                $(tr).find("td:eq('"+index+"')").css("background-color","rgba(164,158,145,0.61)");
                            }else if(plan.state=="未取货"){
                                $(tr).find("td:eq('"+index+"')").css("background-color","rgba(238,182,41,0.29)");
                            }else if(plan.state=="在途"){
                                $(tr).find("td:eq('"+index+"')").css("background-color","rgba(151,201,233,0.36)");
                            }
                        }
                        //是否显示红色
                        var shortageRed=planList[k].shortageRed;
                        if(shortageRed){
                            $(tr).find("td:eq('"+index+"')").css("color","red");
                        }
                        $(tr).find("td:eq('"+index+"')").html(str1);
                    }
                    $("#chooseModal").modal("hide");
                }else {
                    alert(data.msg);
                    //如果出错，不可以被选中
                    var goodId=$("#chooseModal_goodId").val();
                    $("#table_data tr").each(function () {
                        var id=$(this).find("td:eq(0)").text();
                        if(goodId==id){
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
                var goodId=$("#chooseModal_goodId").val();
                $("#table_data tr").each(function () {
                    var id=$(this).find("td:eq(0)").text();
                    if(goodId==id){
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

//取消选择的计划
function chooseDelete(btn){
    var planCacheId=$(btn).parent().parent().find("td:eq(0)").text();
    //显示加载提示信息
    $("#div_loading").css("display", "block");
    //后台查询
    $.ajax({
        url: 'planCacheChooseCancel',
        type: 'post',
        data: {'planCacheId':planCacheId},
        dataType:'json',
        success: function (data) {
            if(data.code==0){
                $(btn.parentNode.parentNode).remove();
                infoSum();
                //重新加载物料的缺件信息、缺件计划信息
                var planList=data.data.planList;
                var goodId=data.data.goodId;
                //获取行
                var tr=null;
                $("#table_data tr").each(function (){
                    if($(this).find("td:eq(0)").text()==goodId){
                        tr=$(this);
                    }
                })
                for(var k=0;k<planList.length;k++){
                    //获取当前单元格的下标
                    var index=parseInt(k)+7;
                    //单元格内容
                    var str1="";
                    //先移除本单元格的样式
                    $(tr).find("td:eq('"+index+"')").removeAttr("style");
                    //缺件信息
                    var shortage=planList[k].shortage;
                    if(shortage!=null){
                        str1+="需求:"+shortage.needcount+" 结存:<a href='#' onclick='toUpdateStock(this)'>"+shortage.stock+"</a> 上次结存:"+shortage.laststock;
                    }
                    //缺件计划是否显示复选框
                    var checkbox=planList[k].checkbox;
                    if(checkbox){
                        str1+="<br><input type='checkbox' name='sort' onclick='checkboxSelf(this)'>";
                    }
                    //缺件计划信息
                    var plan=planList[k].plan;
                    if(plan!=null){
                        str1+=" ("+plan.id+")数量:"+plan.count+" 箱数:"+plan.boxcount+" MAX:"+plan.maxcount+" MIN:"+plan.mincount+" 确认数量:"+plan.surecount+" 收容数:"+plan.good.oneboxcount;
                        //有缺件计划才生成背景色
                        if(plan.state=="未确认"){
                            $(tr).find("td:eq('"+index+"')").css("background-color","rgba(164,158,145,0.61)");
                        }else if(plan.state=="未取货"){
                            $(tr).find("td:eq('"+index+"')").css("background-color","rgba(238,182,41,0.29)");
                        }else if(plan.state=="在途"){
                            $(tr).find("td:eq('"+index+"')").css("background-color","rgba(151,201,233,0.36)");
                        }
                    }
                    //是否显示红色
                    var shortageRed=planList[k].shortageRed;
                    if(shortageRed){
                        $(tr).find("td:eq('"+index+"')").css("color","red");
                    }
                    $(tr).find("td:eq('"+index+"')").html(str1);
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
            alert("操作失败：" + status + "  " + text);
            $("#div_loading").css("display","none");
        }
    });
}

//-------------------------修改被选中计划的数量---------------------------
function toUpdateCount(a){
    var planCacheId=a.parentNode.parentNode.childNodes[0].innerHTML;
    var count=$(a).text();
    //根据计划Id查询详情
    $.ajax({
        url: 'planCacheDetail',
        type: 'post',
        data: {
            'planCacheId':planCacheId},
        dataType:'json',
        success: function (data) {
            if(data.code==0){
                //填入数据
                $("#updateCountModal_planCacheId").val(planCacheId);
                $("#updateCountModal_count").val(count);
                $("#updateCountModal_oneBoxCount").val(data.data.good.oneboxcount);
                var boxCount=0;
                if(count%data.data.good.oneboxcount!=0){
                    boxCount=Math.ceil(count/data.data.good.oneboxcount);
                    $("#updateCountModal_odd").val(count%data.data.good.oneboxcount);
                }else {
                    boxCount=count/data.data.good.oneboxcount;
                    $("#updateCountModal_odd").val(0);
                }
                $("#updateCountModal_boxCount").val(boxCount);
                $("#updateCountModal_unsureCount").val(parseInt(data.data.count)-parseInt(data.data.surecount));
                //打开填入数量的模态框
                $("#updateCountModal").modal("show");
                $("#updateCountModal").on("shown.bs.modal",function (){
                    $("#updateCountModal_count").focus();
                })
            }else {
                alert(data.msg);
            }
        },
        error:function(jqXHR, textStatus, errorThrown){
            var status = jqXHR.status;//404,500等
            var text = jqXHR.statusText;//404对应的Not found,500对应的error
            alert("操作失败：" + status + "  " + text);
        }
    });
}
function updateCount(){
    //数量
    var chooseCount=$("#updateCountModal_count").val();
    //未确认数量
    var unsureCount=$("#updateCountModal_unsureCount").val();
    var str=/^[1-9]{1}[0-9]{0,10}$/;
    if(!str.test(chooseCount)){
        $("#chooseModal_count").focus();
    }else if(parseInt(chooseCount)>parseInt(unsureCount)){
        alert("填入的取货数量不可大于未确认数量");
        $("#chooseModal_count").focus();
    }else {
        //计划id
        var planCacheId=$("#chooseModal_planCacheId").val();
        var lowHeight=$("#input_lowHeight").val();
        var carWidth=$("#input_carWidth").val();
        var takeDate=$("#input_takeDate").val();
        //显示加载提示信息
        $("#div_loading").css("display", "block");
        //后台查询
        $.ajax({
            url: 'planCacheChoose',
            type: 'post',
            data: {
                'planCacheId':planCacheId,
                'takeDate':takeDate,
                'chooseCount':chooseCount,
                'lowHeight':lowHeight,
                'carWidth':carWidth},
            dataType:'json',
            success: function (data) {
                if(data.code==0){
                    //加载当前选中的计划到下面拼载表中
                    var oneBoxCount=data.data.good.oneboxcount;
                    //计算箱数
                    var boxCount=0;
                    if(chooseCount%oneBoxCount==0){
                        boxCount=chooseCount/oneBoxCount;
                    }else {
                        boxCount=Math.ceil(chooseCount/oneBoxCount);
                    }
                    $("#table_data2 tr").each(function (){
                        if($(this).find("td:eq(0)").text()==planCacheId){
                            //更新这一行取货数量、长、重量、体积数据
                            $(this).find("td:eq(8)").html(boxCount);
                            $(this).find("td:eq(9)").html("<a href='#' onclick='toUpdateCount(this)'>"+chooseCount+"</a>");
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
                    //重新加载物料的缺件信息、缺件计划信息
                    var planList=data.data.planList;
                    //获取行
                    var tr=null;
                    $("#table_data tr").each(function (){
                        if($(this).find("td:eq(0)").text()==data.data.good.id){
                            tr=$(this);
                        }
                    })
                    for(var k=0;k<planList.length;k++){
                        //获取当前单元格的下标
                        var index=parseInt(k)+7;
                        //单元格内容
                        var str1="";
                        //先移除本单元格的样式
                        $(tr).find("td:eq('"+index+"')").removeAttr("style");
                        //缺件信息
                        var shortage=planList[k].shortage;
                        if(shortage!=null){
                            str1+="需求:"+shortage.needcount+" 结存:<a href='#' onclick='toUpdateStock(this)'>"+shortage.stock+"</a> 上次结存:"+shortage.laststock;
                        }
                        //缺件计划是否显示复选框
                        var checkbox=planList[k].checkbox;
                        if(checkbox){
                            //是否选择复选框
                            var checked=planList[k].checked;
                            if(checked){
                                str1+="<br><input type='checkbox' name='sort' onclick='checkboxSelf(this)' checked='checked'>";
                            }else {
                                str1+="<br><input type='checkbox' name='sort' onclick='checkboxSelf(this)'>";
                            }
                        }
                        //缺件计划信息
                        var plan=planList[k].plan;
                        if(plan!=null){
                            str1+=" ("+plan.id+")数量:"+plan.count+" 箱数:"+plan.boxcount+" MAX:"+plan.maxcount+" MIN:"+plan.mincount+" 确认数量:"+plan.surecount+" 收容数:"+oneBoxCount;
                            //有缺件计划才生成背景色
                            if(plan.state=="未确认"){
                                $(tr).find("td:eq('"+index+"')").css("background-color","rgba(164,158,145,0.61)");
                            }else if(plan.state=="未取货"){
                                $(tr).find("td:eq('"+index+"')").css("background-color","rgba(238,182,41,0.29)");
                            }else if(plan.state=="在途"){
                                $(tr).find("td:eq('"+index+"')").css("background-color","rgba(151,201,233,0.36)");
                            }
                        }
                        //是否显示红色
                        var shortageRed=planList[k].shortageRed;
                        if(shortageRed){
                            $(tr).find("td:eq('"+index+"')").css("color","red");
                        }
                        $(tr).find("td:eq('"+index+"')").html(str1);
                    }
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
    var currentHeight=$(a).text();
    var highHeight=$("#input_highHeight").val();
    if(currentHeight!=highHeight){
        //数量
        var chooseCount=$(a).parent().parent().find("td:eq(9)").text();
        //计划id
        var planCacheId=a.parentNode.parentNode.childNodes[0].innerHTML;
        var carWidth=$("#input_carWidth").val();
        var takeDate=$("#input_takeDate").val();
        //显示加载提示信息
        $("#div_loading").css("display", "block");
        //后台查询
        $.ajax({
            url: 'planCacheChoose',
            type: 'post',
            data: {
                'planCacheId':planCacheId,
                'takeDate':takeDate,
                'chooseCount':chooseCount,
                'lowHeight':highHeight,
                'carWidth':carWidth},
            dataType:'json',
            success: function (data) {
                if(data.code==0){
                    $("#table_data2 tr").each(function (){
                        if($(this).find("td:eq(0)").text()==planCacheId){
                            //更新这一行长、重量、体积数据
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

//-----------------添加缺件计划---------------------
function toAddPlan(a){
    var goodId=$(a.parentNode.parentNode).find("td:eq(0)").text();
    var goodCode=$(a.parentNode.parentNode).find("td:eq(1)").text();
    var goodName=$(a.parentNode.parentNode).find("td:eq(2)").text();
    var supplierCode=$(a.parentNode.parentNode).find("td:eq(3)").text();
    var supplierName=$(a.parentNode.parentNode).find("td:eq(4)").text();
    $("#addModal_count").val("");
    $("#addModal_remarks").val("");
    var date=$("#input_takeDate").val();
    if(date==""){
        alert("请选择取货时间")
    }else {
        $("#addModal_goodId").val(goodId);
        $("#addModal_goodCode").val(goodCode);
        $("#addModal_goodName").val(goodName);
        $("#addModal_supplierCode").val(supplierCode);
        $("#addModal_supplierName").val(supplierName);
        $("#addModal_date").val(date);
        $("#addModal").modal("show");
        $("#addModal").on("shown.bs.modal",function (){
            $("#addModal_count").focus();
        })
    }
}

function addPlanCache(){
    var goodId=$("#addModal_goodId").val();
    var count=$("#addModal_count").val();
    var date=$("#addModal_date").val();
    var remarks=$("#addModal_remarks").val();
    //正整数
    var str=/^[1-9]{1}[0-9]{0,10}$/;
    if(!str.test(count)){
        $("#addModal_count").focus();
    }else if(remarks==""){
        $("#addModal_remarks").focus();
    }else {
        $("#div_loading").css("display","block");
        //后台添加
        $.ajax({
            url: 'planCacheAdd',
            type: 'post',
            data: {
                'goodId':goodId,
                'count':count,
                'date':date,
                'remarks':remarks},
            dataType:'json',
            success: function (data) {
                if(data.code==0){
                    //更新缺件信息、缺件计划信息
                    var planList=data.data;
                    //加载更新后的计划到表中
                    $("#table_data tr").each(function () {
                        var id=$(this).find("td:eq(0)").text();
                        if(id==goodId){
                            for (var i = 0; i < planList.length; i++) {
                                var tdindex=i+7;
                                var td=$(this).find("td:eq("+tdindex+")");
                                //缺件计划
                                var plan=planList[i].plan;
                                //缺件信息
                                var shortage=planList[i].shortage;
                                //是否标红
                                var red=planList[i].shortageRed;
                                //清空td内容
                                $(td).html("");
                                //清空td的背景色
                                $(td).removeAttr("style");
                                if(shortage!=null){
                                    //缺件信息不为空才填入信息
                                    if(plan==null){
                                        //无计划，只填缺件信息
                                        $(td).html("需求:"+shortage.needcount+" 结存:<a href='#' onclick='toUpdateStock(this)'>"+shortage.stock+"</a> 上次结存:"+shortage.laststock);
                                    }else {
                                        //有计划，填入缺件、计划信息
                                        var unsureCount=parseInt(plan.count)-parseInt(plan.surecount);
                                        var unsureBoxCount=0;
                                        if(unsureCount%plan.good.oneboxcount==0){
                                            unsureBoxCount=unsureCount/plan.good.oneboxcount;
                                        }else {
                                            unsureBoxCount=Math.ceil(unsureCount/plan.good.oneboxcount);
                                        }
                                        //根据计划在途显示背景色
                                        if(plan.state=="未确认"){
                                            $(td).html("需求:"+shortage.needcount+" 结存:<a href='#' onclick='toUpdateStock(this)'>"+shortage.stock+"</a> 上次结存:"+shortage.laststock+"<br><input type='checkbox' name='sort' onclick='checkboxSelf(this)'> ("+plan.id+")数量:"+plan.count+" 箱数:"+plan.boxcount+" 未确认数量:"+unsureCount+" 未确认箱数:"+unsureBoxCount+" 收容数:"+plan.good.oneboxcount);
                                            $(td).css("background-color","rgba(164,158,145,0.61)");
                                        }else if(plan.state=="未取货"){
                                            $(td).html("需求:"+shortage.needcount+" 结存:<a href='#' onclick='toUpdateStock(this)'>"+shortage.stock+"</a> 上次结存:"+shortage.laststock+"<br>("+plan.id+")数量:"+plan.count+" 箱数:"+plan.boxcount);
                                            $(td).css("background-color","rgba(238,182,41,0.29)");
                                        }else if(plan.state=="在途"){
                                            $(td).html("需求:"+shortage.needcount+" 结存:<a href='#' onclick='toUpdateStock(this)'>"+shortage.stock+"</a> 上次结存:"+shortage.laststock+"<br>("+plan.id+")数量:"+plan.count+" 箱数:"+plan.boxcount);
                                            $(td).css("background-color","rgba(151,201,233,0.36)");
                                        }
                                    }
                                    //看是否设置为红色字体
                                    if(red){
                                        $(td).css("color","red");
                                    }
                                }
                            }
                        }
                    })
                    $("#addModal").modal("hide");
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

//---------------------修改缺件结存-----------------
function toUpdateStock(a){
    //物料id
    var goodId=a.parentNode.parentNode.childNodes[0].innerHTML;
    var stock=$(a).text();
    //计划日期
    var col=$(a.parentNode).prevAll().length;
    var date=$("#table_head").find("tr:eq(0)").find("th:eq("+col+")").text();
    //看该物料是否有计划被选中
    var checks=$(a.parentNode.parentNode).find("input");
    var isCheck=false;
    for(var i=0;i<checks.length;i++){
        if($(checks[i]).is(':checked')){
            isCheck=true;
            break;
        }
    }
    if(isCheck){
        alert("有计划被选中，无法修改结存");
    }else {
        //未被选中，可以修改
        $("#updateStockModal_goodId").val(goodId);
        $("#updateStockModal_date").val(date);
        $("#updateStockModal_stock").val(stock);
        $("#updateStockModal").modal("show");
        $("#updateStockModal").on("shown.bs.modal",function (){
            $("#updateStockModal_stock").focus();
        })
    }
}

function updateStock(){
    var goodId=$("#updateStockModal_goodId").val();
    var date=$("#updateStockModal_date").val();
    var stock=$("#updateStockModal_stock").val();
    var str=/^[+-]?\d*$/;
    if(!str.test(stock)){
        $("#updateStockModal_stock").focus();
    }else {
        $("#div_loading").css("display","block");
        //后台查询
        $.ajax({
            url: 'updateShortageByGoodAndDate',
            type: 'post',
            data: {
                'goodId':goodId,
                'stock':stock,
                'date':date},
            dataType:'json',
            success: function (data) {
                if(data.code==0){
                    //更新缺件信息、缺件计划信息
                    var planList=data.data.planList;
                    //加载更新后的计划到表中
                    $("#table_data tr").each(function () {
                        var id=$(this).find("td:eq(0)").text();
                        if(id==goodId){
                            for(var k=0;k<planList.length;k++){
                                var tdindex=k+7;
                                var td=$(this).find("td:eq("+tdindex+")");
                                //缺件信息
                                var shortage=planList[k].shortage;
                                if(shortage==null){
                                    $(td).html("");
                                }else {
                                    //缺件计划信息
                                    var plan=planList[k].plan;
                                    //是否显示红色
                                    var shortageRed=planList[k].shortageRed;
                                    //去掉单元格样式
                                    $(td).removeAttr("style");
                                    //看是否有缺件计划信息
                                    if(plan==null){
                                        $(td).html("需求:"+shortage.needcount+" 结存:<a href='#' onclick='toUpdateStock(this)'>"+shortage.stock+"</a> 上次结存:"+shortage.laststock);
                                        //看是否是红色字体
                                        if(shortageRed){
                                            $(td).css("color","red");
                                        }
                                    }else {
                                        //缺件计划是否显示复选框
                                        var checkbox=planList[k].checkbox;
                                        //复选框
                                        var sort="";
                                        if(checkbox){
                                            //有复选框
                                            sort="<input type='checkbox' name='sort' onclick='checkboxSelf(this)'>";
                                        }
                                        $(td).html("需求:"+shortage.needcount+" 结存:<a href='#' onclick='toUpdateStock(this)'>"+shortage.stock+"</a> 上次结存:"+shortage.laststock+"<br>"+sort+" ("+plan.id+")数量:"+plan.count+" 箱数:"+plan.boxcount+" MAX:"+plan.maxcount+" MIN:"+plan.mincount+" 确认数量:"+plan.surecount+" 收容数:"+good.oneboxcount);
                                        //看当前单元格的背景色
                                        if(plan.state=="未确认"){
                                            $(td).css("background-color","rgba(164,158,145,0.61)");
                                        }else if(plan.state=="未取货"){
                                            $(td).css("background-color","rgba(238,182,41,0.29)");
                                        }else if(plan.state=="在途"){
                                            $(td).css("background-color","rgba(151,201,233,0.36)");
                                        }
                                        //看是否是红色字体
                                        if(shortageRed){
                                            $(td).css("color","red");
                                        }
                                    }
                                }
                            }
                        }
                    })
                    $("#updateStockModal").modal("hide");
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

//-----------------------生成取货计划------------------
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
                url: 'planTakeAdd',
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
                    'planCacheInfos':infos},
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
