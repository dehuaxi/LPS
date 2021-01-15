package com.defei.lps.service;

import com.defei.lps.result.Result;
import org.springframework.stereotype.Repository;

public interface DriverService {
    //添加
    public Result add(String driverName, String phone,String licenseNumber);
    //根据id修改
    public Result update(int id, String driverName, String phone,String licenseNumber);
    //根据id删除
    public Result delete(int id);
    //条件分页查询
    public Result findAll(String driverName, String phone,int currentPage);
    //查询所有
    public Result driverAll();
}
