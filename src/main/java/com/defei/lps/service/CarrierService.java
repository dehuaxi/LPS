package com.defei.lps.service;

import com.defei.lps.result.Result;

public interface CarrierService {
    //添加
    public Result add(String carrierName, String carrierNumber, String contact,String phone,String address);
    //根据id修改
    public Result update(int id,String contact,String phone,String address);
    //根据id删除
    public Result delete(int id);
    //条件分页查询
    public Result findAll(String carrierName, String carrierNumber, int currentPage);
    //查询所有
    public Result allCarrier();
}
