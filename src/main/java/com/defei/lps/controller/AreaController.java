package com.defei.lps.controller;

import com.defei.lps.result.Result;
import com.defei.lps.service.AreaService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AreaController {
    @Autowired
    private AreaService areaService;
    //--------------------------------工厂(客户)管理页面--------------------------------
    //跳转到工厂(客户)管理页面
    @RequestMapping(value = "toArea", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("area")
    public String toArea() {
        return "area";
    }

    //条件分页查询
    @RequestMapping(value = "area", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("area")
    @ResponseBody
    public Result findAll(String areaName, String areaNumber, int currentPage) {
        return areaService.findAll(areaName,areaNumber,currentPage);
    }

    //添加
    @RequestMapping(value = "areaAdd", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("areaAdd")
    @ResponseBody
    public Result add(String areaName, String areaNumber, String describes) {
        return areaService.add(areaName, areaNumber,describes);
    }

    //删除
    @RequestMapping(value = "areaDelete", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("areaDelete")
    @ResponseBody
    public Result delete(int id) {
        return areaService.delete(id);
    }

    //修改
    @RequestMapping(value = "areaUpdate", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("areaUpdate")
    @ResponseBody
    public Result update(int id, String areaName, String areaNumber, String describes) {
        return areaService.update(id,areaName,areaNumber,describes);
    }

    //查询所有区域
    @RequestMapping(value = "allArea", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public Result allArea(){
        return areaService.allArea();
    }

    //查询当前账号能看到的区域
    @RequestMapping(value = "currentArea", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public Result currentArea(){
        return areaService.currentArea();
    }

}
