
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

    laydate.render({
        elem: '#input_date',//指定元素
        type: 'date'//日期
    });

    laydate.render({
        elem: '#add_date',//指定元素
        type: 'date'//日期
    });

    laydate.render({
        elem: '#update_date',//指定元素
        type: 'date'//日期
    });

    //查询
    findByLimit(1);

    //添加模态框中输入物料名称的按键事件
    $("#add_findGoodName").keyup(function (event) {
        loadGood();
    })
})


//----------------------------------------删除-----------------------------------
function toDelete(a) {
    //获取角色id
    var id = a.parentNode.parentNode.childNodes[0].innerHTML;
    var tips = confirm("确定删除吗？");
    if (tips == true) {
        $("#div_loading").css("display","block");
        //ajax删除
        $.ajax({
            url: 'planCacheDelete',
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

function findByLimit(currentPage) {
    //显示加载提示信息
    $("#div_loading").css("display", "block");
    //获取参数
    var goodCode=$("#input_goodCode").val();
    var goodName=$("#input_goodName").val();
    var supplierCode=$("#input_supplierCode").val();
    var supplierName=$("#input_supplierName").val();
    var routeId=$("#select_routeId").val();
    var factoryId=$("#select_factoryId").val();
    var date=$("#input_date").val();
    var state=$("#select_state").val();
    var type=$("#select_type").val();
    var urgent=$("#select_urgent").val();
    //先清除旧数据
    $("#table_data").html("");
    //后台查询
    $.ajax({
        url: 'planCache',
        type: 'post',
        data: {
            'goodCode':goodCode,
            'goodName':goodName,
            'supplierCode':supplierCode,
            'supplierName':supplierName,
            'routeId':routeId,
            'factoryId':factoryId,
            'date':date,
            'state':state,
            'type':type,
            'urgent':urgent,
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
                        deleteBtn="<td><button type='button' class='btn btn-danger btn-xs' onclick='toDelete(this)'>删除</button></td>";
                    }
                    for (var i = 0; i < record.length; i++) {
                        var background="";
                        if(record[i].state=="未取货"){
                            background="rgb(127,152,238)";
                        }else if(record[i].state=="在途"){
                            background="rgb(137,239,152)";
                        }
                        var color="";
                        if(record[i].urgent=="特急"){
                            color="rgb(245,3,51)";
                        }else if(record[i].urgent=="紧急"){
                            color="rgb(255,192,3)";
                        }
                        var goodcode="";
                        var goodname="";
                        var suppliercode="";
                        var suppliername="";
                        var factoryname="";
                        var routename="";
                        var transitDay="";
                        if(record[i].good!=null){
                            goodcode= record[i].good.goodcode;
                            goodname=record[i].good.goodname;
                            if(record[i].good.supplier!=null){
                                suppliercode=record[i].good.supplier.suppliercode;
                                suppliername=record[i].good.supplier.suppliername;
                                transitDay=record[i].good.supplier.transitday;
                                if(record[i].good.supplier.route!=null){
                                    routename=record[i].good.supplier.route.routename;
                                    if(record[i].good.supplier.route.factory!=null){
                                        factoryname=record[i].good.supplier.route.factory.factoryname;
                                    }
                                }
                            }
                        }
                        var str= "<tr style='background-color: "+background+";color: "+color+"'><td style='display: none'>" + record[i].id + "</td>"+
                            "<td>" + goodcode +"</td>"+
                            "<td>" + goodname +"</td>"+
                            "<td>" + suppliercode +"</td>"+
                            "<td>" + suppliername +"</td>"+
                            "<td>" + record[i].boxcount +"</td>"+
                            "<td>" + record[i].count +"</td>"+
                            "<td>" + record[i].maxcount +"</td>"+
                            "<td>" + record[i].mincount +"</td>"+
                            "<td>" + record[i].surecount +"</td>"+
                            "<td>" + record[i].takecount +"</td>"+
                            "<td>" + record[i].receivecount +"</td>"+
                            "<td>" + record[i].date +"</td>"+
                            "<td>" + record[i].receivedate +"</td>"+
                            "<td>" + transitDay +"</td>"+
                            "<td>" + record[i].state +"</td>"+
                            "<td>" + record[i].type +"</td>"+
                            "<td>" + record[i].urgent +"</td>"+
                            "<td>" + timeFormat(record[i].createtime) +"</td>"+
                            "<td>" + routename +"</td>"+
                            "<td>" + factoryname +"</td>"+
                            "<td>" + record[i].remarks +"</td>"+
                            deleteBtn+"</tr>";
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
