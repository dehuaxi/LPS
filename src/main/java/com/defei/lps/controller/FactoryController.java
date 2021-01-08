package com.defei.lps.controller;

import com.defei.lps.result.Result;
import com.defei.lps.service.FactoryService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;

@Controller
public class FactoryController {
    @Autowired
    private FactoryService factoryService;
    //--------------------------------工厂(客户)管理页面--------------------------------
    //跳转到工厂(客户)管理页面
    @RequestMapping(value = "toFactory", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("factory")
    public String toFactory() {
        return "factory";
    }

    //条件分页查询
    @RequestMapping(value = "factory", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("factory")
    @ResponseBody
    public Result factory(String factoryName, String factoryNumber,String province,String city,String district, int currentPage) {
        return factoryService.findAll(factoryName,factoryNumber,province,city,district,currentPage);
    }

    //添加
    @RequestMapping(value = "factoryAdd", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("factoryAdd")
    @ResponseBody
    public Result add(String factoryName, String factoryNumber,String describes,String province,String city,String district,String address,String longitude,String latitude) {
        return factoryService.add(factoryName, factoryNumber,describes,province,city,district,address,longitude,latitude);
    }

    //删除
    @RequestMapping(value = "factoryDelete", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("factoryDelete")
    @ResponseBody
    public Result delete(int id) {
        return factoryService.delete(id);
    }

    //修改
    @RequestMapping(value = "factoryUpdate", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("factoryUpdate")
    @ResponseBody
    public Result update(int id,String factoryName,String factoryNumber,String describes,String province,String city,String district,String address,String longitude,String latitude) {
        return factoryService.update(id,factoryName,factoryNumber,describes,province,city,district,address,longitude,latitude);
    }

    //查询当前用户拥有的所有工厂编号
    @RequestMapping(value = "currentFactory", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public Result currentFactory(){
        return factoryService.currentFactory();
    }

    //根据id查询
    @RequestMapping(value = "factoryById", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public Result factoryById(int id){
        return factoryService.factoryById(id);
    }

    //下载
    @RequestMapping(value = "factoryDownload", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("factoryDownload")
    public void factoryDownload(String factoryName, String factoryNumber,String province,String city,String district, HttpServletResponse response) {
        factoryService.factoryDownload(factoryName,factoryNumber,province,city,district,response);
    }
}
