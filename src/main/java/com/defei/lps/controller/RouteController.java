package com.defei.lps.controller;

import com.defei.lps.result.Result;
import com.defei.lps.service.RouteService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

@Controller
public class RouteController {
    @Autowired
    private RouteService routeService;

    //跳转到线路管理页面
    @RequestMapping(value = "toRoute", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("route")
    public String toRoute() {
        return "route";
    }

    //条件分页查询
    @RequestMapping(value = "route", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("route")
    @ResponseBody
    public Result findAll(String routeName,String routeNumber,int areaId,int factoryId,int currentPage) {
        return routeService.findAll(routeName,routeNumber,areaId,factoryId,currentPage);
    }

    //添加
    @RequestMapping(value = "routeAdd", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("routeAdd")
    @ResponseBody
    public Result add(String routeName,String routeNumber,String describes,int areaId,int factoryId,String warehouse) {
        return routeService.add(routeName, routeNumber,describes,areaId,factoryId,warehouse);
    }

    //批量添加
    @RequestMapping(value = "routeAddUpload", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("routeAdd")
    @ResponseBody
    public Result upload(@RequestParam(value="file",required=false) MultipartFile excelFile) {
        return routeService.upload(excelFile);
    }

    //删除
    @RequestMapping(value = "routeDelete", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("routeDelete")
    @ResponseBody
    public Result delete(int id) {
        return routeService.delete(id);
    }

    //修改
    @RequestMapping(value = "routeUpdate", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("routeUpdate")
    @ResponseBody
    public Result update(int id,String routeName,String routeNumber,String describes,String warehouse) {
        return routeService.update(id,routeName,routeNumber,describes,warehouse);
    }

    //查询当前用户拥有的所有工厂编号
    @RequestMapping(value = "currentRoute", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public Result currentRoute(){
        return routeService.currentRoute();
    }

    //查询所有的线路，以工厂分组，返回zTree格式数据
    @RequestMapping(value = "routeGroupFactorynumber", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public Result routeGroupFactorynumber(){
        return routeService.routeGroupFactorynumber();
    }

    //根据id查询
    @RequestMapping(value = "routeById", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public Result routeById(int id){
        return routeService.routeById(id);
    }

    //下载
    @RequestMapping(value = "routeDownload", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("routeDownload")
    public void routeDownload(String routeName,String routeNumber,int areaId,int factoryId, HttpServletResponse response) {
        routeService.routeDownload(routeName,routeNumber,areaId,factoryId,response);
    }

    //批量上传的模板下载
    @RequestMapping(value = "routeModalDownload", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("routeAdd")
    public void routeModalDownload(HttpServletResponse response) {
        routeService.routeModalDownload(response);
    }
}
