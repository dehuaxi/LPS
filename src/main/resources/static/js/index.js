//iframe在页面设置高度
function changeFrameHeight() {
    //设置calss名为main的div的高度为浏览器高度-导航栏高度
    $("div.main").css("height", (window.innerHeight - $("#navbar").height() - 4));
}

//当窗口大小改变时执行函数
window.onresize = function () {
    changeFrameHeight();
}
//页面加载时，就执行函数，设置iframe的高度
$(document).ready(function () {
    //设置iframe的高度
    changeFrameHeight();
})


//点击修改个人信息时执行
function loadSelf() {
    $("#input_oldpassword").val("");
    $("#input_newpassword").val("");
    $("#input_newpassword2").val("");
    //打开模态框
    $("#updateSelfModal").modal("show");
    $("#updateSelfModal").on("shown.bs.modal",function () {
        $("#input_oldpassword").focus();
    });
}

//修改自身信息
function updateSelf() {
    //获取手机号并验证
    var oldpassword = $("#input_oldpassword").val().replace(" ","");
    var newpassword=$("#input_newpassword").val().replace(" ","");
    var newpassword2=$("#input_newpassword2").val().replace(" ","");
    var str=/^[0-9A-Za-z]{6,20}$/;
    if(!str.test(oldpassword)){
        $("#input_oldpassword").focus();
    }else if(!str.test(newpassword)){
        $("#input_newpassword").focus();
    }else if(!str.test(newpassword2)){
        $("#input_newpassword2").focus();
    }else if(newpassword!=newpassword2){
        alert("2次输入的新密码不一致");
        $("#input_newpassword2").focus();
    }else {
        var key = $("#key").html();
        var oldpasswordEncode=aesEncode(oldpassword, key);
        var newpasswordEncode = aesEncode(newpassword, key);
        $.ajax({
            url: 'updatePassword',
            type: 'post',
            data: {'oldPassword': oldpasswordEncode, 'newPassword': newpasswordEncode},
            success: function (data) {
                if (data== "") {
                    alert("修改成功，请重新登陆");
                    location = "logOut";
                } else {
                    alert(data);
                }
            },
            error: function (jqXHR, textStatus, errorThrown) {
                var status = jqXHR.status;//404,500等
                var text = jqXHR.statusText;//404对应的Not found,500对应的error
                alert("修改失败：" + status + "  " + text);
            }
        });
    }
}
