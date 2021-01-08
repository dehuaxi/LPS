
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
    //加载查询栏、添加模态框的区域
    $.ajax({
        url: 'allArea',
        type: 'post',
        dataType:'json',
        success: function (data) {
            if(data.date===null){
                alert("没有区域信息，请添加");
            } else {
                for(var i=0;i<data.data.length;i++){
                    $("#select_areaId").append("<option value='"+data.data[i].id+"'>"+data.data[i].areaname+"</option>")
                    $("#add_areaId").append("<option value='"+data.data[i].id+"'>"+data.data[i].areaname+"</option>")
                }
            }
        },
        error:function(jqXHR, textStatus, errorThrown){
            var status = jqXHR.status;//404,500等
            var text = jqXHR.statusText;//404对应的Not found,500对应的error
            alert("加载区域信息失败：" + status + "  " + text);
        }
    });
    //加载查询栏的省
    loadProvince("select_province","");
    //查询
    findByLimit(1);
})

//----------------------------------------添加-----------------------------------
//去添加函数,打开添加角色的模态框
function toAdd() {
    //清除输入框内容
    $("#add_supplierName").val("");//名称
    $("#add_supplierCode").val("");//编号
    $("#add_abbreviation").val("");//编号
    $("#add_contact").val("");//联系人
    $("#add_phone").val("");//电话
    //加载省的下拉框
    loadProvince("add_province","");
    //市下拉框清空
    $("#add_city").html("");
    //县区下拉框清空
    $("#add_district").html("");
    $("#add_address").html("");//详细地址
    $("#add_transitDay").val("");
    ///打开模态框
    $("#addModal").modal("show");
    //模态框打开事件
    $("#addModal").on("shown.bs.modal", function () {
        $("#add_supplierName").focus();
    })
}

function add() {
    //获取参数
    var supplierName=$("#add_supplierName").val();//名称
    var supplierCode=$("#add_supplierCode").val();//编号
    var abbreviation=$("#add_abbreviation").val();
    var contact=$("#add_contact").val();//联系人
    var phone=$("#add_phone").val();//电话
    var province=$("#add_province").val();
    var city=$("#add_city").val();
    var district=$("#add_district").val();
    var address=$("#add_address").val();
    var longitude=$("#add_longitude").val();
    var latitude=$("#add_latitude").val();
    var factoryId=$("#add_factoryId").val();
    var areaId=$("#add_areaId").val();
    var transitDay=$("#add_transitDay").val();
    var str1=/^\d{1,2}(.[5]{1})?$/;
    //判断条件
    if (supplierName == "") {
        $("#add_supplierName").focus();
    } else if (supplierCode == "") {
        $("#add_supplierCode").focus();
    }else if (abbreviation == "") {
        $("#add_abbreviation").focus();
    } else if(province==""){
        alert("请选择省")
    }else if(city==""){
        alert("请选择市")
    }else if(district==""){
        alert("请选择区(县)")
    }else if(address==""){
        $("#add_address").focus();
    }else if(factoryId=="0"){
        alert("请选择工厂")
    }else if(areaId=="0"){
        alert("请选择区域")
    }else if(!str1.test(transitDay)){
        $("#add_transitDay").focus();
    }else{
        $("#div_loading").css("display","block");
        $.ajax({
            url: 'supplierAdd',
            type: 'post',
            data: {'supplierName':supplierName,
                'supplierCode':supplierCode,
                'abbreviation':abbreviation,
                'contact':contact,
                'phone':phone,
                'province':province,
                'city':city,
                'district':district,
                'address':address,
                'longitude':longitude,
                'latitude':latitude,
                'factoryId':factoryId,
                'areaId':areaId,
                'transitDay':transitDay},
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
    //获取表单中的文件
    var fileData = new FormData($('#uploadForm')[0]);
    if (file != "") {//上传不为空，则ajax上传
        //隐藏加载提示
        $("#div_loading").css("display", "block");
        //清空提示信息
        $("#uploadResult").html("");
        $("#uploadFile").val("");
        $.ajax({
            url: "supplierAddUpload",
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
        url: 'supplierById',
        type: 'post',
        dataType:'json',
        data:{'id':id},
        success: function (data) {
            if (data.code==0) {
                $("#update_id").val(id);
                $("#update_supplierCode").val(data.data.suppliercode);
                $("#update_supplierName").val(data.data.suppliername);
                $("#update_abbreviation").val(data.data.abbreviation);
                $("#update_contact").val(data.data.contact);
                $("#update_phone").val(data.data.phone);
                loadProvince("update_province",data.data.province);
                loadCity("update_city","update_province",data.data.city);
                loadDistrict("update_district","update_city","update_province",data.data.district);
                $("#update_address").val(data.data.address);
                $("#update_longitude").val(data.data.longitude);
                $("#update_latitude").val(data.data.latitude);
                $("#update_transitDay").val(data.data.transitday);
                //打开模态框
                $("#updateModal").modal("show");
                //模态框打开事件
                $("#updateModal").on("shown.bs.modal", function () {
                    //名称输入框获取焦点
                    $("#update_supplierName").focus();
                })
            } else {
                alert(data.msg);
            }
        },
        error:function(jqXHR, textStatus, errorThrown){
            var status = jqXHR.status;//404,500等
            var text = jqXHR.statusText;//404对应的Not found,500对应的error
            alert("加载供应商信息失败：" + status + "  " + text);
        }
    });
}

//修改按钮
function update() {
    //获取参数
    var id = $("#update_id").val();//id
    var abbreviation=$("#update_abbreviation").val();
    var contact =$("#update_contact").val();
    var phone=$("#update_phone").val();
    var province=$("#update_province").val();
    var city=$("#update_city").val();
    var district=$("#update_district").val();
    var address=$("#update_address").val();
    var longitude=$("#update_longitude").val();
    var latitude=$("#update_latitude").val();
    var transitDay=$("#update_transitDay").val();
    var str= /^(([\+ \-]?([1-9]{1}[0-9]{0,2})|([0]{1})))([.]\d{0,6})?$/;
    var str1=/^\d{1,2}(.[5]{1})?$/;
    if (province == "") {
        alert("请选择省");
    }else if (abbreviation == "") {
        $("#update_abbreviation").focus();
    }  else if (city == "") {
        alert("请选择市");
    } else if (district == "") {
        alert("请选择区县");
    } else if (address == "") {
        $("#update_address").focus();
    } else if (!str.test(longitude)) {
        $("#update_longitude").focus();
    } else if (!str.test(latitude)) {
        $("#update_latitude").focus();
    } else if(!str1.test(transitDay)){
        alert(transitDay)
        $("#update_transitDay").focus();
    }else {
        $("#div_loading").css("display","block");
        //提交后台
        $.ajax({
            url: 'supplierUpdate',
            type: 'post',
            data: {'id': id,
                'abbreviation':abbreviation,
                'contact': contact,
                'phone': phone,
                'province': province,
                'city': city,
                'district': district,
                'address': address,
                'longitude': longitude,
                'latitude': latitude,
                'transitDay':transitDay},
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

//----------------------------------------删除-----------------------------------
function toDelete(a) {
    //获取角色id
    var id = a.parentNode.parentNode.childNodes[0].innerHTML;
    var tips = confirm("确定删除吗？");
    if (tips == true) {
        $("#div_loading").css("display","block");
        //ajax删除
        $.ajax({
            url: 'supplierDelete',
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
    var supplierCode=$("#input_supplierCode").val();
    var supplierName=$("#input_supplierName").val();
    var province=$("#select_province").val();
    var city=$("#select_city").val();
    var district=$("#select_district").val();
    var factoryId=$("#select_factoryId").val();
    var areaId=$("#select_areaId").val();
    if(factoryId=="0"){
        alert("必须选择工厂")
    }else {
        window.open("supplierDownload?supplierCode="+supplierCode+"&supplierName="+supplierName+"&province="+province+"&city="+city+"&district="+district+"&factoryId="+factoryId+"&areaId="+areaId);
    }
}

//------------------------------------分页查询收货记录-------------------------------
function findByLimit(currentPage) {
    //显示加载提示信息
    $("#div_loading").css("display", "block");
    //获取参数
    var supplierCode=$("#input_supplierCode").val();
    var supplierName=$("#input_supplierName").val();
    var province=$("#select_province").val();
    var city=$("#select_city").val();
    var district=$("#select_district").val();
    var factoryId=$("#select_factoryId").val();
    var areaId=$("#select_areaId").val();
    //先清除旧数据
    $("#table_data").html("");
    //后台查询
    $.ajax({
        url: 'supplier',
        type: 'post',
        data: {
            'supplierCode':supplierCode,
            'supplierName':supplierName,
            'province':province,
            'city':city,
            'district':district,
            'factoryId':factoryId,
            'areaId':areaId,
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
                        var areaname="";
                        var factoryname="";
                        if(record[i].route!=null){
                            if(record[i].route.area!=null){
                                areaname=record[i].route.area.areaname;
                            }
                            if(record[i].route.factory!=null){
                                factoryname=record[i].route.factory.factoryname;
                            }
                        }
                        var str = "<tr><td style='display: none'>" + record[i].id + "</td>"+
                            "<td>" + record[i].suppliercode +"</td>"+
                            "<td>" + record[i].suppliername +"</td>"+
                            "<td>" + record[i].abbreviation +"</td>"+
                            "<td>" + record[i].contact +"</td>"+
                            "<td>" + record[i].phone +"</td>"+
                            "<td>" + record[i].province +"</td>"+
                            "<td>" + record[i].city +"</td>"+
                            "<td>" + record[i].district +"</td>"+
                            "<td>" + record[i].address +"</td>"+
                            "<td>" + record[i].longitude +"</td>"+
                            "<td>" + record[i].latitude +"</td>"+
                            "<td>" + record[i].transitday +"</td>"+
                            "<td>" + areaname +"</td>"+
                            "<td>" + factoryname +"</td>"+
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
