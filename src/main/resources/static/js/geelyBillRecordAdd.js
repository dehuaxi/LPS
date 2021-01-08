
//-------------------------------页面加载执行--------------------------------------
$(document).ready(function () {
    $("#input_billNumber").focus();

    //输入框键盘事件
    $("#input_billNumber").keyup(function (event) {
        if(event.keyCode==13){
            loadGeelyBillCache();
        }
    })
})

//加载PD单内的所有在途记录
function loadGeelyBillCache() {
    var billNumber = $("#input_billNumber").val();
    $("#div_loading").css("display", "block");
    $("#input_billNumber").val("");
    $("#result").html("");
    $("#table_data").html("");
    $.ajax({
        url:'geelyBillCacheDetail',
        type:'post',
        data:{'geelyBillNumber':billNumber},
        dataType:'json',
        success:function(data){
            if(data.code==0){
                document.getElementById("voice_scanner").play();
                var supplierCode="";
                var supplierName="";
                if(data.data[0].good!=null){
                    if(data.data[0].good.supplier!=null){
                        supplierCode=data.data[0].good.supplier.suppliercode;
                        supplierName=data.data[0].good.supplier.suppliername;
                    }
                }
                $("#result").html("<span style='color: green'>单号："+data.data[0].billnumber+"&emsp;&emsp;供应商编号："+supplierCode+"&emsp;&emsp;供应商名称："+supplierName+"</span>");
                //加载
                for(var i=0;i<data.data.length;i++){
                    var goodname="";
                    var goodcode="";
                    var oneBoxCount="";
                    if(data.data[i].good!=null){
                        goodname=data.data[i].good.goodname;
                        goodcode=data.data[i].good.goodcode;
                        oneBoxCount=data.data[i].good.oneboxcount;
                    }
                    var btn="<td></td>";
                    if(data.data[i].plandate!=""){
                        btn="<td><button type='button' class='btn btn-xs btn-warning' onclick='receive(this)'>回执</button></td>";
                    }
                    var str= "<tr title='点击实收数量可修改实收数量和备注'><td style='display: none'>" + data.data[i].id + "</td>"+
                        "<td>" + goodcode +"</td>"+
                        "<td>" + goodname +"</td>"+
                        "<td>" + oneBoxCount +"</td>"+
                        "<td>" + data.data[i].count +"</td>"+
                        "<td>" + data.data[i].batch +"</td>"+
                        "<td><a href='#' onclick='toUpdateCount(this)'>" + data.data[i].count +"</a></td><td></td>"+btn+"</tr>";
                    $("#table_data").append(str);
                }
            }else {
                document.getElementById("voice_error").play();
                $("#result").html("<span style='color: red'>"+data.msg+"</span>");
            }
            $("#input_billNumber").focus();
            $("#div_loading").css("display", "none");
        },
        error:function(jqXHR, textStatus, errorThrown){
            var status = jqXHR.status;//404,500等
            var text = jqXHR.statusText;//404对应的Not found,500对应的error
            document.getElementById("voice_error").play();
            $("#result").html("<span style='color: red'>查询失败：" + status + "  " + text+"</span>");
            $("#input_billNumber").focus();
            $("#div_loading").css("display", "none");
        }
    });
}

//修改实收数量
function toUpdateCount(a) {
    var id=$(a.parentNode.parentNode).find("td:eq(0)").text();
    var count=$(a).text();
    var maxCount=$(a.parentNode.parentNode).find("td:eq(4)").text();
    var remarks=$(a.parentNode.parentNode).find("td:eq(7)").text();
    $("#updateModal_id").val(id);
    $("#updateModal_maxCount").val(maxCount);
    $("#updateModal_count").val(count);
    $("#updateModal_remarks").val(remarks);
    $("#updateModal").modal("show");
    $("#updateModal").on("shown.bs.modal",function (){
        $("#updateModal_count").focus();
    })
}
function updateCount() {
    var id=$("#updateModal_id").val();
    var count=$("#updateModal_count").val();
    var maxCount=$("#updateModal_maxCount").val();
    var remarks=$("#updateModal_remarks").val();
    var isNext=false;
    if(!/^[1-9]{1}[0-9]{0,10}$/.test(count)){
        alert("请填入正整数");
        $("#updateModal_count").focus();
    }else if(parseInt(count)>parseInt(maxCount)){
        if(remarks==""){
            alert("请填入备注");
            $("#updateModal_remarks").focus();
        }else {
            isNext=true;
        }
    }else if(parseInt(count)<parseInt(maxCount)){
        if(remarks==""){
            alert("请填入备注");
            $("#updateModal_remarks").focus();
        }else {
            isNext=true;
        }
    }else {
        isNext=true;
    }
    if(isNext){
        $("#table_data tr").each(function (){
            if($(this).find("td:eq(0)").text()==id){
                $(this).find("td:eq(6)").html("<a href='#' onclick='toUpdateCount(this)'>"+count+"</a>");
                $(this).find("td:eq(7)").html(remarks);
            }
        })
        $("#updateModal").modal("hide");
    }
}

function receive(btn){
    var id=$(btn.parentNode.parentNode).find("td:eq(0)").text();
    var count=$(btn.parentNode.parentNode).find("td:eq(6)").text();
    var maxCount=$(btn.parentNode.parentNode).find("td:eq(4)").text();
    var remarks=$(btn.parentNode.parentNode).find("td:eq(7)").text();
    var isCan=false;
    if(!/^[1-9]{1}[0-9]{0,10}$/.test(count)){
        alert("数实收量请填入数字");
    }else if(parseInt(count)>parseInt(maxCount)){
        if(remarks==""){
            alert("请填入备注");
        }else {
            isCan=true;
        }
    }else if(parseInt(count)<parseInt(maxCount)){
        if(remarks==""){
            alert("请填入备注");
        }else {
            isCan=true;
        }
    }else{
        isCan=true;
    }
    if(isCan){
        $("#div_loading").css("display", "block");
        $.ajax({
            url:'geelyBillRecordAdd',
            type:'post',
            data:{'geelyBillCacheId':id,'count':count,'remarks':remarks},
            dataType:'json',
            success:function(data){
                if(data.code==0){
                    document.getElementById("voice_success").play();
                    //删除行
                    $("#table_data tr").each(function(){
                        var tdId=$(this).find("td").eq(0);
                        if($(tdId).text()==id){
                            $(this).remove();
                        }
                    });
                    //如果全单回执完毕，那么就清楚上面得内容
                    if( $("#table_data").find("tr").length<1){
                        $("#result").html("");
                    }
                }else {
                    alert(data.msg);
                }
                $("#input_billNumber").focus();
                $("#div_loading").css("display", "none");
            },
            error:function(jqXHR, textStatus, errorThrown){
                var status = jqXHR.status;//404,500等
                var text = jqXHR.statusText;//404对应的Not found,500对应的error
                alert("回执失败：" + status + "  " + text);
                $("#input_billNumber").focus();
                $("#div_loading").css("display", "none");
            }
        });
    }
}