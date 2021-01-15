package com.defei.lps.controller;

import com.defei.lps.result.Result;
import com.defei.lps.service.WarehouseService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class WarehouseController {
    @Autowired
    private WarehouseService warehouseService;
    //--------------------------------页面--------------------------------
    //跳转到工厂(客户)管理页面
    @RequestMapping(value = "toWarehouse", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("warehouse")
    public String toWarehouse() {
        return "warehouse";
    }

    //条件分页查询
    @RequestMapping(value = "warehouse", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("warehouse")
    @ResponseBody
    public Result warehouse(String warehouseName, String warehouseNumber,String province,String city,String district, int currentPage) {
        return warehouseService.findAll(warehouseName,warehouseNumber,province,city,district,currentPage);
    }

    //添加
    @RequestMapping(value = "warehouseAdd", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("warehouseAdd")
    @ResponseBody
    public Result add(String warehouseName, String warehouseNumber,String describes,String contact,String phone,String province,String city,String district,String address,String longitude,String latitude) {
        return warehouseService.add(warehouseName, warehouseNumber,describes,contact,phone,province,city,district,address,longitude,latitude);
    }

    //删除
    @RequestMapping(value = "warehouseDelete", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("warehouseDelete")
    @ResponseBody
    public Result delete(int id) {
        return warehouseService.delete(id);
    }

    //修改
    @RequestMapping(value = "warehouseUpdate", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("warehouseUpdate")
    @ResponseBody
    public Result update(int id,String warehouseName,String warehouseNumber,String describes,String contact,String phone,String province,String city,String district,String address,String longitude,String latitude) {
        return warehouseService.update(id,warehouseName,warehouseNumber,describes,contact,phone,province,city,district,address,longitude,latitude);
    }

    //根据id查询
    @RequestMapping(value = "warehouseById", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public Result warehouseById(int id){
        return warehouseService.warehouseById(id);
    }

    //查询所有的中转仓，并以Ztree结构返回
    @RequestMapping(value = "warehouseZtree", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public Result warehouseZtree(){
        return warehouseService.warehouseZtree();
    }

    //查询当前账号能看到的所有中转仓
    @RequestMapping(value = "currentWarehouse", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public Result currentWarehouse(){
        return warehouseService.currentWarehouse();
    }

    //查询所有
    @RequestMapping(value = "warehouseAll", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public Result warehouseAll() {
        return warehouseService.warehouseAll();
    }
}
