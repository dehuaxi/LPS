package com.defei.lps.service;

import com.defei.lps.result.Result;

public interface AreaService {
    //添加
    public Result add(String areaName, String areaNumber, String describes);
    //根据id修改
    public Result update(int id, String areaName, String areaNumber, String describes);
    //根据id删除
    public Result delete(int id);
    //条件分页查询
    public Result findAll(String areaName, String areaNumber, int currentPage);
    //查询所有区域
    public Result allArea();
    //查询当前账号能看到的区域
    public Result currentArea();
}
