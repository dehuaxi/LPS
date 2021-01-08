package com.defei.lps.service;

import com.defei.lps.result.Result;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

public interface RouteService {
    //添加
    public Result add(String routeName,String routeNumber,String describes,int areaId,int factoryId,String warehouse);
    //批量上传
    public Result upload(MultipartFile excelFile);
    //根据id修改
    public Result update(int id,String routeName,String routeNumber,String describes,String warehouse);
    //删除
    public Result delete(int id);
    //分页条件查询
    public Result findAll(String routeName,String routeNumber,int areaId,int factoryId,int currentPage);
    //获取当前账号能够查看的所有线路
    public Result currentRoute();
    //查询所有的线路，以工厂分组，返回zTree格式数据
    public Result routeGroupFactorynumber();
    //根据id查询
    public Result routeById(int id);
    //下载
    public void routeDownload(String routeName,String routeNumber,int areaId,int factoryId, HttpServletResponse response);
    //批量上传的模板下载
    public void routeModalDownload(HttpServletResponse response);
}
