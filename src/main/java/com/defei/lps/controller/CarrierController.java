package com.defei.lps.controller;

import com.defei.lps.result.Result;
import com.defei.lps.service.CarrierService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class CarrierController {
    @Autowired
    private CarrierService carrierService;
    //--------------------------------页面--------------------------------
    //跳转到页面
    @RequestMapping(value = "toCarrier", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("carrier")
    public String toCarrier() {
        return "carrier";
    }

    //条件分页查询
    @RequestMapping(value = "carrier", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("carrier")
    @ResponseBody
    public Result findAll(String carrierName, String carrierNumber, int currentPage) {
        return carrierService.findAll(carrierName,carrierNumber,currentPage);
    }

    //添加
    @RequestMapping(value = "carrierAdd", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("carrierAdd")
    @ResponseBody
    public Result add(String carrierName, String carrierNumber, String contact,String phone,String address) {
        return carrierService.add(carrierName, carrierNumber,contact,phone,address);
    }

    //删除
    @RequestMapping(value = "carrierDelete", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("carrierDelete")
    @ResponseBody
    public Result delete(int id) {
        return carrierService.delete(id);
    }

    //修改
    @RequestMapping(value = "carrierUpdate", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("carrierUpdate")
    @ResponseBody
    public Result update(int id,String contact,String phone,String address) {
        return carrierService.update(id,contact,phone,address);
    }

    //查询所有
    @RequestMapping(value = "allCarrier", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public Result allCarrier(){
        return carrierService.allCarrier();
    }

}
