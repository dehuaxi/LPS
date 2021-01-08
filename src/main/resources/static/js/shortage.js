
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
    //加载查询栏的线路
    $.ajax({
        url: 'currentRoute',
        type: 'post',
        dataType:'json',
        success: function (data) {
            if (data.code==0) {
                for(var i=0;i<data.data.length;i++){
                    $("#select_routeId").append("<option value='"+data.data[i].id+"'>"+data.data[i].routename+"</option>")
                }
            } else {
                alert(data.msg);
            }
        },
        error:function(jqXHR, textStatus, errorThrown){
            var status = jqXHR.status;//404,500等
            var text = jqXHR.statusText;//404对应的Not found,500对应的error
            alert("加载线路信息失败：" + status + "  " + text);
        }
    });
    //查询
    findByLimit(1);

    laydate.render({
        elem: '#input_dateStart',//指定元素
        type: 'date'//日期
    });

    laydate.render({
        elem: '#input_dateEnd',//指定元素
        type: 'date'//日期
    });
})


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
    if (file != "") {//上传不为空，则ajax上传
        var factoryId=$("#add_factoryId").val();
        if(factoryId==0){
            alert("请选择工厂");
        }else {
            //获取表单中的文件
            var fileData = new FormData($('#uploadForm')[0]);
            fileData.append("factoryId",factoryId);
            //隐藏加载提示
            $("#div_loading").css("display", "block");
            //清空提示信息
            $("#uploadResult").html("");
            $("#uploadFile").val("");
            $.ajax({
                url: "shortageAddUpload",
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
    var factoryId=$("#select_factoryId").val();
    var routeId=$("#select_routeId").val();
    var dateStart=$("#input_dateStart").val();
    var dateEnd=$("#input_dateEnd").val();
    //先清除旧数据
    $("#table_data").html("");
    $("#table_head").html("");
    //后台查询
    $.ajax({
        url: 'shortage',
        type: 'post',
        data: {
            'goodCode':goodCode,
            'goodName':goodName,
            'supplierCode':supplierCode,
            'supplierName':supplierName,
            'factoryId':factoryId,
            'routeId':routeId,
            'dateStart':dateStart,
            'dateEnd':dateEnd,
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
                    //5.1填入表头
                    var dateList=datas.dateList;
                    if(dateList==null){
                        $("#table_head").append("<tr><th>物料编号</th><th>物料名称</th><th>供应商编号</th><th>供应商名称</th><th>所属工厂</th></tr>");
                    }else {
                        var headStr="<tr><th>物料编号</th><th>物料名称</th><th>供应商编号</th><th>供应商名称</th><th>所属工厂</th>";
                        for(var r=0;r<dateList.length;r++){
                            var date=dateList[r];
                            var month=date.split("-")[1];
                            if(month.substring(0,1)=="0"){
                                month=month.substring(1);
                            }
                            var day=date.split("-")[2];
                            if(day.substring(0,1)=="0"){
                                day=day.substring(1);
                            }
                            var headDate=month+"月"+day+"日";
                            headStr+="<th>"+headDate+"需求</th><th>"+headDate+"结存</th>";
                        }
                        headStr+="</tr>";
                        $("#table_head").append(headStr);
                    }
                    //5.2表格内容
                    var goodList = datas.list;
                    for (var i = 0; i < goodList.length; i++) {
                        var goodcode="";
                        var goodname="";
                        var suppliercode="";
                        var suppliername="";
                        var factoryname="";
                        if(goodList[i].good!=null){
                            goodcode= goodList[i].good.goodcode;
                            goodname=goodList[i].good.goodname;
                            if(goodList[i].good.supplier!=null){
                                suppliercode=goodList[i].good.supplier.suppliercode;
                                suppliername=goodList[i].good.supplier.suppliername;
                                if(goodList[i].good.supplier.route!=null){
                                    if(goodList[i].good.supplier.route.factory!=null){
                                        factoryname=goodList[i].good.supplier.route.factory.factoryname;
                                    }
                                }
                            }
                        }
                        var str = "<tr><td>" + goodcode +"</td>"+
                            "<td>" + goodname +"</td>"+
                            "<td>" + suppliercode +"</td>"+
                            "<td>" + suppliername +"</td>"+
                            "<td>" + factoryname +"</td>";
                        var shortageList=goodList[i].shortageList;
                        if(shortageList!=null){
                            //填入需求结存行
                            for (var g = 0; g < shortageList.length; g++) {
                                if(shortageList[g].needCount==""&&shortageList[g].stock==""){
                                    str+="<td></td><td></td>";
                                }else{
                                    var stockColor="";
                                    if(parseInt(shortageList[g].stock)<0){
                                        stockColor="red";
                                    }
                                    if(shortageList[g].planState=="未确认"){
                                        str+="<td style='background-color: rgba(164,158,145,0.61)' title='"+shortageList[g].planCount+"'>"+shortageList[g].needCount+"</td><td style='background-color: rgba(164,158,145,0.61);color: "+stockColor+"' title='"+shortageList[g].planCount+"'>"+shortageList[g].stock+"</td>";
                                    }else if(shortageList[g].planState=="未取货"){
                                        str+="<td style='background-color: rgba(238,182,41,0.29)' title='"+shortageList[g].planCount+"'>"+shortageList[g].needCount+"</td><td style='background-color: rgba(238,182,41,0.29);color: "+stockColor+"' title='"+shortageList[g].planCount+"'>"+shortageList[g].stock+"</td>";
                                    }else if(shortageList[g].planState=="在途"){
                                        str+="<td style='background-color: rgba(151,201,233,0.36)' title='"+shortageList[g].planCount+"'>"+shortageList[g].needCount+"</td><td style='background-color: rgba(151,201,233,0.36);color: "+stockColor+"' title='"+shortageList[g].planCount+"'>"+shortageList[g].stock+"</td>";
                                    }else if(shortageList[g].planState=="完成"){
                                        str+="<td style='background-color: rgba(12,181,40,0.75)' title='"+shortageList[g].planCount+"'>"+shortageList[g].needCount+"</td><td style='background-color: rgba(12,181,40,0.75);color: "+stockColor+"' title='"+shortageList[g].planCount+"'>"+shortageList[g].stock+"</td>";
                                    }else {
                                        str+="<td>"+shortageList[g].needCount+"</td><td style='color: "+stockColor+"'>"+shortageList[g].stock+"</td>";
                                    }
                                }
                            }
                        }
                        str +="</tr>";
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
