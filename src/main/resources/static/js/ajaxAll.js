//全局的ajax访问拦截，ajax完毕无论成功还是失败，都要进行这个处理
$.ajaxSetup({
    contentType: "application/x-www-form-urlencoded;charset=utf-8",
    complete: function(xhr) {
        //session过期，则跳转到登录页面
        if(xhr.responseJSON.code == -2){
            alert(xhr.responseJSON.msg);
            window.location.href=xhr.responseJSON.data;
        }
        //如果是没权限，则提示
        if(xhr.responseJSON.code == -1){
            alert(xhr.responseJSON.msg);
        }
    }
});