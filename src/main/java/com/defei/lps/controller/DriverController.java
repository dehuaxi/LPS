package com.defei.lps.controller;

import com.defei.lps.result.Result;
import com.defei.lps.service.CarrierService;
import com.defei.lps.service.DriverService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DriverController {
    @Autowired
    private DriverService driverService;
    //--------------------------------页面--------------------------------
    //跳转到页面
    @RequestMapping(value = "toDriver", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("driver")
    public String toDriver() {
        return "driver";
    }

    //条件分页查询
    @RequestMapping(value = "driver", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("driver")
    @ResponseBody
    public Result findAll(String name, String phone, int currentPage) {
        return driverService.findAll(name,phone,currentPage);
    }

    //添加
    @RequestMapping(value = "driverAdd", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("driverAdd")
    @ResponseBody
    public Result add(String name, String phone, String number) {
        return driverService.add(name, phone,number);
    }

    //删除
    @RequestMapping(value = "driverDelete", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("driverDelete")
    @ResponseBody
    public Result delete(int id) {
        return driverService.delete(id);
    }

    //修改
    @RequestMapping(value = "driverUpdate", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("driverUpdate")
    @ResponseBody
    public Result update(int id,String name,String phone,String number) {
        return driverService.update(id,name,phone,number);
    }

    //查询全部
    @RequestMapping(value = "driverAll", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public Result driverAll() {
        return driverService.driverAll();
    }

}
