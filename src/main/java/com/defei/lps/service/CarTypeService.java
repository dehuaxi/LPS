package com.defei.lps.service;

import com.defei.lps.result.Result;

public interface CarTypeService {
    //添加
    public Result add(String carTypeName, int highLength,int highHeight,int lowLength,int lowHeight, int carWidth,String carWeight,String carVolume);
    //根据id修改
    public Result update(int id, int highLength,int highHeight,int lowLength,int lowHeight, int carWidth,String carWeight,String carVolume);
    //根据id删除
    public Result delete(int id);
    //条件分页查询
    public Result findAll();
    //根据车型名称查询
    public Result carTypeByName(String name);
}
