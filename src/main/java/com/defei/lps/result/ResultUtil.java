package com.defei.lps.result;

public class ResultUtil {
    //操作成功，有返回内容
    public static  Result success(Object object){
        Result result=new Result();
        result.setCode(0);
        result.setMsg("成功");
        result.setData(object);
        return result;
    }
    //操作成功，无任何返回内容
    public static Result success(){
       return success(null);
    }
    //操作失败，返回提示
    public static Result error(int code,String msg){
        Result result=new Result();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }
    //操作失败，返回提示以及失败的数据
    public static Result error(int code,String msg,Object object){
        Result result=new Result();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }
}
