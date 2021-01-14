
//-------------------------------页面加载执行--------------------------------------
$(document).ready(function () {
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

    //加载区域
    $.ajax({
        url: 'currentArea',
        type: 'post',
        dataType:'json',
        success: function (data) {
            if (data.code==0) {
                for(var i=0;i<data.data.length;i++){
                    $("#select_startName").append("<option>"+data.data[i].areaname+"</option>")
                }
            } else {
                alert(data.msg);
            }
        },
        error:function(jqXHR, textStatus, errorThrown){
            var status = jqXHR.status;//404,500等
            var text = jqXHR.statusText;//404对应的Not found,500对应的error
            alert("加载出发地失败：" + status + "  " + text);
        }
    });

    //加载工厂
    $.ajax({
        url: 'currentFactory',
        type: 'post',
        dataType:'json',
        success: function (data) {
            if (data.code==0) {
                for(var i=0;i<data.data.length;i++){
                    $("#select_endName").append("<option>"+data.data[i].factoryname+"</option>")
                }
                //加载中转仓
                $.ajax({
                    url: 'currentWarehouse',
                    type: 'post',
                    dataType:'json',
                    success: function (datas) {
                        if (datas.code==0) {
                            for(var g=0;g<datas.data.length;g++){
                                $("#select_endName").append("<option>"+datas.data[g].warehousename+"</option>")
                            }
                        } else {
                        }
                    },
                    error:function(jqXHR, textStatus, errorThrown){
                        var status = jqXHR.status;//404,500等
                        var text = jqXHR.statusText;//404对应的Not found,500对应的error
                        alert("加载目的地失败：" + status + "  " + text);
                    }
                });
            } else {
                alert(data.msg);
            }
        },
        error:function(jqXHR, textStatus, errorThrown){
            var status = jqXHR.status;//404,500等
            var text = jqXHR.statusText;//404对应的Not found,500对应的error
            alert("加载目的地失败：" + status + "  " + text);
        }
    });

    laydate.render({
        elem: '#input_takeDate',//指定元素
        type: 'date'//日期
    });

    //全选按钮
    $("#checkAll").click(function(){
        if($(this).is(':checked')){
            $("#table_data").find("input").each(function(){
                $(this).prop("checked",true);
            })
        }else{
            $("#table_data").find("input").each(function(){
                $(this).prop("checked",false);
            })
        }
    })

    //查询
    findByLimit(1);

})

//下载
function downloads(){
    var planNumber=$("#input_planNumber").val();
    var supplierCode=$("#input_supplierCode").val();
    var supplierName=$("#input_supplierName").val();
    var routeId=$("#select_routeId").val();
    var takeDate=$("#input_takeDate").val();
    var startName=$("#select_startName").val();
    var endName=$("#select_endName").val();
    var userName=$("#input_userName").val();
    if(startName==""){
        alert("必须选择出发地");
    }else if(endName==""){
        alert("必须选择目的地");
    }else {
        window.open("planTakeDownload?planNumber="+planNumber+"&supplierCode="+supplierCode+"&supplierName="+supplierName+"&routeId="+routeId+"&takeDate="+takeDate+"&startName="+startName+"&endName="+endName+"&userName="+userName);
    }
}

//查看详细情况
function details(a){
    $("#table_detail").html("");
    var planNumber=$(a).parent().text();
    $("#div_loading").css("display", "block");
    $.ajax({
        url: 'planTakeDetail',
        type: 'post',
        data: {
            'planNumber':planNumber},
        dataType:'json',
        success: function (data) {
            if(data.code==0){
                $("#detail_planNumber").html(planNumber);
                $("#detail_carType").html(data.data[0].cartype);
                $("#detail_startName").html(data.data[0].startname);
                $("#detail_endName").html(data.data[0].endname);
                var deleteBtn="";
                if(deleteData){
                    deleteBtn="<td><button type='button' class='btn btn-xs btn-danger' onclick='deletePlanTake(this)'>移出计划</button></td>"
                }
                for(var i=0;i<data.data.length;i++){
                    var goodcode="";
                    var goodname="";
                    var suppliercode="";
                    var suppliername="";
                    if(data.data[i].good!=null){
                        goodcode=data.data[i].good.goodcode;
                        goodname=data.data[i].good.goodname;
                        if(data.data[i].good.supplier!=null){
                            suppliercode=data.data[i].good.supplier.suppliercode;
                            suppliername=data.data[i].good.supplier.suppliername;
                        }
                    }
                    var str="<tr><td style='display: none'>"+data.data[i].id+"</td>" +
                        "<td>"+goodcode+"</td>" +
                        "<td>"+goodname+"</td>" +
                        "<td>"+suppliercode+"</td>" +
                        "<td>"+suppliername+"</td>" +
                        "<td>"+data.data[i].count+"</td>" +
                        "<td>"+data.data[i].boxcount+"</td>" +
                        "<td>"+data.data[i].length+"</td>" +
                        "<td>"+data.data[i].volume+"</td>" +
                        "<td>"+data.data[i].weight+"</td>" +
                        "<td>"+data.data[i].carheight+"</td>" +deleteBtn+"</tr>";
                    $("#table_detail").append(str);
                }
                $("#detailModal").modal("show");
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

//删除取货计划
function deletePlanTake(btn){
    var id=$(btn).parent().parent().find("td:eq(0)").text();
    $("#div_loading").css("display", "block");
    $.ajax({
        url: 'planTakeDelete',
        type: 'post',
        data: {'id':id},
        dataType:'json',
        success: function (data) {
            if(data.code==0){
                var planNumber=$("#detail_planNumber").html();
                $("#table_detail").html("");
                $.ajax({
                    url: 'planTakeDetail',
                    type: 'post',
                    data: {
                        'planNumber':planNumber},
                    dataType:'json',
                    success: function (data) {
                        if(data.code==0){
                            var deleteBtn="";
                            if(deleteData){
                                deleteBtn="<td><button type='button' class='btn btn-xs btn-danger' onclick='deletePlanTake(this)'>移出计划</button></td>"
                            }
                            for(var i=0;i<data.data.length;i++){
                                var goodcode="";
                                var goodname="";
                                var suppliercode="";
                                var suppliername="";
                                if(data.data[i].good!=null){
                                    goodcode=data.data[i].good.goodcode;
                                    goodname=data.data[i].good.goodname;
                                    if(data.data[i].good.supplier!=null){
                                        suppliercode=data.data[i].good.supplier.suppliercode;
                                        suppliername=data.data[i].good.supplier.suppliername;
                                    }
                                }
                                var str="<tr><td style='display: none'>"+data.data[i].id+"</td>" +
                                    "<td>"+goodcode+"</td>" +
                                    "<td>"+goodname+"</td>" +
                                    "<td>"+suppliercode+"</td>" +
                                    "<td>"+suppliername+"</td>" +
                                    "<td>"+data.data[i].count+"</td>" +
                                    "<td>"+data.data[i].boxcount+"</td>" +
                                    "<td>"+data.data[i].length+"</td>" +
                                    "<td>"+data.data[i].volume+"</td>" +
                                    "<td>"+data.data[i].weight+"</td>" +
                                    "<td>"+data.data[i].carheight+"</td>" +deleteBtn+"</tr>";
                                $("#table_detail").append(str);
                            }
                        }else {
                            //计划不存在，就隐藏详细模态框，刷新页面
                            $("#detailModal").modal("hide");
                            var currentPage=$("#span_currentPage").html();
                            findByLimit(currentPage);
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
            }else {
                alert(data.msg);
            }
            //隐藏加载提示信息
            $("#div_loading").css("display", "none");
        },
        error:function(jqXHR, textStatus, errorThrown){
            var status = jqXHR.status;//404,500等
            var text = jqXHR.statusText;//404对应的Not found,500对应的error
            alert("删除失败：" + status + "  " + text);
            $("#div_loading").css("display","none");
        }
    });
}
//拼拆取货计划
function toUpdatePlanTake(){
    //获取需要拼拆的计划编号
    var planNumbers="";
    var checkBoxList=document.getElementsByName("sort");
    for(k in checkBoxList){
        if(checkBoxList[k].checked){
            planNumbers+=","+$(checkBoxList[k]).parent().parent().find("td:eq(1)").text();
        }
    }
    if(planNumbers!=""){
        //后台提交
        window.open("toPlanTakeUpdate?planNumbers="+planNumbers.substring(1));
    }
}

//取货绑定吉利单号
function toUploadPlanTake(){
    //获取需要拼拆的计划编号
    var planNumber="";
    var checkBoxList=document.getElementsByName("sort");
    var count=0;
    for(k in checkBoxList){
        if(checkBoxList[k].checked){
            count++;
            planNumber=$(checkBoxList[k]).parent().parent().find("td:eq(1)").text();
        }
    }
    if(count==1){
        //后台提交
        window.open("toPlanTakeUpload?planNumber="+planNumber);
    }else if(count>1){
        alert("只能选择一个取货计划进行绑定");
    }
}

//------------------------------------分页查询收货记录-------------------------------
//时间格式化
function timeFormat(time) {
    var date = new Date(time);//时间戳为10位需*1000，时间戳为13位的话不需乘1000
    Y = date.getFullYear() + '-';
    M = (date.getMonth()+1 < 10 ? '0'+(date.getMonth()+1) : date.getMonth()+1) + '-';
    D = date.getDate() < 10 ? '0'+date.getDate()+ ' ' : date.getDate() + ' ';
    h = date.getHours() <10 ? '0'+date.getHours()+ ':': date.getHours()+ ':';
    m = date.getMinutes() <10 ? '0'+date.getMinutes()+ ':' : date.getMinutes()+ ':';
    s = date.getSeconds() <10 ? '0'+date.getSeconds() : date.getSeconds();
    return Y+M+D+h+m+s;
}

function chooseSelf(td) {
    var checkbox=$(td).find("input");
    if($(checkbox).is(":checked")){
        $(checkbox).prop("checked",false);
    }else {
        $(checkbox).prop("checked",true);
    }
}
function checkboxSelf(checkbox) {
    if($(checkbox).is(":checked")){
        $(checkbox).prop("checked",false);
    }else {
        $(checkbox).prop("checked",true);
    }
}

function findByLimit(currentPage) {
    //显示加载提示信息
    $("#div_loading").css("display", "block");
    //获取参数
    var planNumber=$("#input_planNumber").val();
    var supplierCode=$("#input_supplierCode").val();
    var supplierName=$("#input_supplierName").val();
    var routeId=$("#select_routeId").val();
    var takeDate=$("#input_takeDate").val();
    var startName=$("#select_startName").val();
    var endName=$("#select_endName").val();
    var userName=$("#input_userName").val();
    //先清除旧数据
    $("#table_data").html("");
    //后台查询
    $.ajax({
        url: 'planTake',
        type: 'post',
        data: {
            'planNumber':planNumber,
            'supplierCode':supplierCode,
            'supplierName':supplierName,
            'routeId':routeId,
            'date':takeDate,
            'startName':startName,
            'endName':endName,
            'userName':userName,
            'currentPage': currentPage},
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
                    for (var i = 0; i < record.length; i++) {
                        var str= "<tr><td onclick='chooseSelf(this)'><input type='checkbox' name='sort' onclick='checkboxSelf(this)'></td>"+
                            "<td><a href='#' onclick='details(this)'>" + record[i].plannumber +"</a></td>"+
                            "<td>" + record[i].count +"</td>"+
                            "<td>" + record[i].boxcount +"</td>"+
                            "<td>" + record[i].length +"</td>"+
                            "<td>" + record[i].volume +"</td>"+
                            "<td>" + record[i].weight +"</td>"+
                            "<td>" + record[i].date +"</td>"+
                            "<td>" + record[i].cartype +"</td>"+
                            "<td>" + record[i].startname +"</td>"+
                            "<td>" + record[i].startnumber +"</td>"+
                            "<td>" + record[i].endname +"</td>"+
                            "<td>" + record[i].endnumber +"</td>"+
                            "<td>" + record[i].routetype +"</td>"+
                            "<td>" + timeFormat(record[i].createtime) +"</td>"+
                            "<td>" + record[i].username +"</td></tr>";
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
