package com.defei.lps.service;

import com.defei.lps.result.Result;

import javax.servlet.http.HttpServletResponse;

public interface FactoryService {
    //添加
    public Result add(String factoryName, String factoryNumber,String describes,String province,String city,String district,String address,String longitude,String latitude);
    //根据id修改
    public Result update(int id,String factoryName, String factoryNumber,String describes,String province,String city,String district,String address,String longitude,String latitude);
    //根据id删除
    public Result delete(int id);
    //条件分页查询
    public Result findAll(String factoryName, String factoryNumber,String province,String city,String district,int currentPage);
    //查询当前用户拥有的所有工厂编号
    public Result currentFactory();
    //根据id查询
    public Result factoryById(int id);
    //下载
    public void factoryDownload(String factoryName, String factoryNumber,String province,String city,String district, HttpServletResponse response);
}
