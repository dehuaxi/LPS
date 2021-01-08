//页面加载时就执行
$(document).ready(function () {
    //页面一加载，用户名输入框获取焦点
    $("#input_userName").focus();

    //密码输入框按enter按键时触发
    $("#input_password").keydown(function (event) {
        $("#login_result").html("");
        if (event.keyCode == 13) {
            //调用登陆函数
            login();
        }
    })
})

//登陆
function login() {
    var userName=$("#input_userName").val();
    var password=$("#input_password").val();
    var str=/[0-9A-Za-z]{6,20}/;
    if(userName==""){
        $("#input_userName").focus();
    }else if(!str.test(password)){
        $("#input_password").focus();
    }else {
        $.ajax({
            url:'login',
            data:{'userName':userName,'password':password},
            type:'post',
            dataType:'json',
            success:function (data) {
                if(data.code==0){
                    var url=data.data;
                    window.location.href=url;
                }else if(data.code==1){
                    $("#login_result").html(data.msg);
                    $("#input_password").val("");
                    $("#input_password").focus();
                }else{
                    window.close();
                }
            },
            error:function(jqXHR, textStatus, errorThrown){
                var status = jqXHR.status;//404,500等
                var text = jqXHR.statusText;//404对应的Not found,500对应的error
                alert("登陆失败：" + status + "  " + text);
            }
        })
    }
}