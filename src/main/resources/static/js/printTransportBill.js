
//-------------------------------页面加载执行--------------------------------------
$(document).ready(function () {
    //加载运输单内容
    loadCarrier();
})

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
//----------------加载运输单内容-----------------
function loadCarrier(){
    var billNumber=$("#billNumber").html();
    if(billNumber==""){
        alert("无运输单号，无法打印");
        window.close();
    }else {
        $("#div_loading").css("display","block");
        //先添加一维码
        $("#barcode").barcode(billNumber, "code128", {
            output: 'css',       //渲染方式 css/bmp/svg/canvas
            //bgColor: '#ff0000', //条码背景颜色
            //color: '#00ff00',   //条码颜色
            barWidth: 1,        //单条条码宽度
            barHeight: 50,     //单体条码高度
            //moduleSize: 100,   //条码大小
            //posX: 100000,        //条码坐标X
            //posY: 50000,         //条码坐标Y
            showHRI: false,    //是否在条码下方显示内容
            fontSize:15,//文字大小
            addQuietZone: false  //是否添加空白区（内边距）
        });
        //后台查询运输单详细情况
        $.ajax({
            url: 'transportBillCacheBillDetail',
            type: 'post',
            dataType:'json',
            data:{'billNumber':billNumber},
            success: function (data) {
                if(data.code==0){
                    var record=data.data;
                    $("#createTime").html(timeFormat(record[0].createtime));
                    $("#printTime").html(timeFormat(new Date()));
                    $("#carNumber").html(record[0].carnumber);
                    $("#dirver").html(record[0].driver);
                    $("#phone").html(record[0].phone);
                    $("#carTypeName").html(record[0].cartypename);
                    $("#startName").html(record[0].startname);
                    $("#endName").html(record[0].endname);
                    $("#carrierName").html(record[0].carriername);
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
                        var str= "<tr><td>" + goodcode +"</td>"+
                            "<td>" + goodname +"</td>"+
                            "<td>" + suppliercode +"</td>"+
                            "<td>" + suppliername +"</td>"+
                            "<td>" + record[i].geelybillnumber +"</td>"+
                            "<td>" + record[i].batch +"</td>"+
                            "<td>" + record[i].geelycount +"</td>"+
                            "<td>" + record[i].count +"</td>"+
                            "<td>" + record[i].boxcount +"</td><td></td><td></td></tr>";
                        $("#table_data").append(str);
                    }
                    $("#div_loading").css("display","none");
                    //打印
                    window.print();
                } else {
                    alert(data.msg);
                    window.close();
                }
            },
            error:function(jqXHR, textStatus, errorThrown){
                var status = jqXHR.status;//404,500等
                var text = jqXHR.statusText;//404对应的Not found,500对应的error
                alert("加载运输单信息失败：" + status + "  " + text);
                window.close();
            }
        });
    }
}

