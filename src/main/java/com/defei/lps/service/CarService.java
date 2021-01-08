package com.defei.lps.service;

import com.defei.lps.result.Result;

public interface CarService {
    //添加
    public Result add(String carNumber, int carrierId, int carTypeId,String driver,String phone,int highLength,int highHeight,int lowLength,int lowHeight,int carWidth);
    //根据id修改
    public Result update(int id, int carrierId, int carTypeId,String driver,String phone,int highLength,int highHeight,int lowLength,int lowHeight,int carWidth);
    //根据id删除
    public Result delete(int id);
    //条件分页查询
    public Result findAll(String carNumber, int carrierId, int carTypeId,int currentPage);
    //根据车型查询
    public Result findByCarType(String carTypeName);
    //根据车牌号查询
    public Result carByCarnumber(String carNumber);
}
