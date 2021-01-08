
//-------------------------------页面加载执行--------------------------------------
$(document).ready(function () {
    //加载中转仓
    loadWarehouse();

    $("#billNumber").keyup(function (event) {
        if(event.keyCode==13){
            getBillInfo();
        }
    })

    $("#billNumber").focus();
})
//----------------加载中转仓------------------
function loadWarehouse(){
    $.ajax({
        url:'currentWarehouse',
        dataType:'json',
        type:'post',
        success:function (data){
            if(data.code==0){
                for(var i=0;data.data.length;i++){
                    $("#warehouseId").append("<option value='"+data.data[i].id+"'>"+data.data[i].warehousename+"</option>")
                }
            }else {
                alert(data.msg);
                window.close();
            }
        },
        error:function(jqXHR, textStatus, errorThrown){
            var status = jqXHR.status;//404,500等
            var text = jqXHR.statusText;//404对应的Not found,500对应的error
            alert("加载中转仓信息失败：" + status + "  " + text);
            window.close();
        }
    })
}

//扫描或者输入运输单号按回车健后加载运输单内容
function getBillInfo(){
    $("#result").html("");
    $("#table_data").html("");
    var warehouseId=$("#warehouseId").val();
    if(warehouseId==0){
        $("#result").html("请选择要入库的中转仓");
        $("#billNumber").val("");
        $("#billNumber").focus();
    }else {
        var billNumber=$("#billNumber").val().replace(/\s*/g,"");
        if(!/^[0-9A-Za-z-]{1,30}$/.test(billNumber)){
            $("#result").html("运输单号格式不正确");
            $("#billNumber").val("");
            $("#billNumber").focus();
        }else {
            $.ajax({
                url:'transportBillCacheBillDetail2',
                dataType:'json',
                type:'post',
                data:{'billNumber':billNumber,'warehouseId':warehouseId},
                success:function (data){
                    if(data.code==0){
                        var record=data.data;
                        for(var i=0;i<record.length;i++){
                            var goodcode="";
                            var goodname="";
                            var suppliercode="";
                            var suppliername="";
                            var factoryname="";
                            if(record[i].good!=null){
                                goodcode= record[i].good.goodcode;
                                goodname=record[i].good.goodname;
                                if(record[i].good.supplier!=null){
                                    suppliercode=record[i].good.supplier.suppliercode;
                                    suppliername=record[i].good.supplier.suppliername;
                                    if(record[i].good.supplier.route.factory!=null){
                                        factoryname=record[i].good.supplier.route.factory.factoryname;
                                    }
                                }
                            }
                            var str= "<tr title='点击实收数量单元格修改实收数量'><td style='display: none'>" + record[i].id +"</td>"+
                                "<td>" + record[i].geelybillnumber +"</td>"+
                                "<td>" + goodcode +"</td>"+
                                "<td>" + goodname +"</td>"+
                                "<td>" + suppliercode +"</td>"+
                                "<td>" + suppliername +"</td>"+
                                "<td>" + record[i].geelycount +"</td>"+
                                "<td>" + record[i].count +"</td>"+
                                "<td>" + record[i].boxcount +"</td>"+
                                "<td onclick='toUpdateRealCount(this)'>"+record[i].count+"</td></tr>";
                            $("#table_data").append(str);
                        }
                        $("#p_billNumber").html(billNumber);
                    }else {
                        $("#result").html(data.msg);
                    }
                    $("#billNumber").val("");
                    $("#billNumber").focus();
                },
                error:function(jqXHR, textStatus, errorThrown){
                    var status = jqXHR.status;//404,500等
                    var text = jqXHR.statusText;//404对应的Not found,500对应的error
                    $("#result").html("加载中转仓信息失败：" + status + "  " + text);
                    $("#billNumber").val("");
                    $("#billNumber").focus();
                }
            })
        }
    }
}

//修改入库实收数量
function toUpdateRealCount(td){
    var realCount=$(td).text();
    var count=$(td).parent().find("td:eq(7)").text();
    var index=$(td).parent().index();
    $("#update_realCount").val(realCount);
    $("#update_count").val(count);
    $("#update_index").val(index);
    $("#updateModal").modal("show");
    $("#updateModal").on("shown.bs.modal",function (){
        $("#update_realCount").focus();
    })
}
function updateRealCount(td){
    var realCount=$("#update_realCount").val();
    var count=$("#update_count").val();
    var index=$("#update_index").val();
    if(!/[0-9]{1,11}/.test(realCount)){
        $("#update_realCount").focus();
    }else {
        $("#table_data tr").each(function (){
            if($(this).index()==index){
                if(count!=realCount){
                    $(this).find("td:eq(9)").html(realCount);
                    $(this).find("td:eq(9)").css("color","red");
                }else {
                    $(this).find("td:eq(9)").html(realCount);
                    $(this).find("td:eq(9)").removeAttr("style");
                }
            }
        })
        $("#updateModal").modal("hide");
    }
}

//入库
function add(){
    var billNumber=$("#p_billNumber").html();
    if(billNumber==""){
        $("#result").html("请先扫描运输单号");
        $("#billNumber").val("");
        $("#billNumber").focus();
    }else {
        var warehouseId=$("#warehouseId").val();
        //物料信息
        var goodInfos="";
        var str="";
        $("#table_data tr").each(function (){
            var id=$(this).find("td:eq(0)").text();
            var realCount=$(this).find("td:eq(9)").text();
            if(realCount==""){
                str+=";第"+(parseInt($(this).index())+parseInt("1"))+"行没有填入实收数量";
            }
            goodInfos+=";"+id+","+realCount;
        })
        if(str!=""){
            str=str.substring(0);
            var s=str.split(";");
            var ss="";
            for(var k=0;k<s.length;k++){
                ss+=s[k]+"<br>";
            }
            $("#result").html(ss);
        }else {
            goodInfos=goodInfos.substring(1);
            //都填入实收数量，传入后台
            $("#div_loading").css("display","block");
            $.ajax({
                url:'warehouseEntryAdd',
                dataType:'json',
                type:'post',
                data:{'warehouseId':warehouseId,
                    'billNumber':billNumber,
                    'goodInfos':goodInfos},
                success:function (data){
                    if(data.code==0){
                        alert("入库成功！");
                        window.location.reload();
                    }else {
                        $("#result").html(data.msg);
                    }
                    $("#div_loading").css("display","none");
                    $("#billNumber").val("");
                    $("#billNumber").focus();
                },
                error:function(jqXHR, textStatus, errorThrown){
                    var status = jqXHR.status;//404,500等
                    var text = jqXHR.statusText;//404对应的Not found,500对应的error
                    $("#result").html("入库失败：" + status + "  " + text);
                    $("#div_loading").css("display","none");
                    $("#billNumber").val("");
                    $("#billNumber").focus();
                }
            })
        }
    }
}