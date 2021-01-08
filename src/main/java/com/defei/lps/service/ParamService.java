package com.defei.lps.service;

import com.defei.lps.result.Result;

public interface ParamService {
    //添加
    public Result add(String paramName,String paramValue,String paramType,String describes);
    //根据id修改
    public Result update(int id,String paramValue,String paramType,String describes);
    //根据id删除
    public Result delete(int id);
    //条件分页查询
    public Result findAll();
}
