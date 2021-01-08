package com.defei.lps.controller;

import com.defei.lps.result.Result;
import com.defei.lps.service.WarehouseOutService;
import com.defei.lps.service.WarehouseTakeService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class WarehouseTakeController {
    @Autowired
    private WarehouseTakeService warehouseTakeService;
    //--------------------------------中转仓装载方案页面--------------------------------
    //跳转到页面
    @RequestMapping(value = "toWarehouseTake", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("warehouseTake")
    public String toWarehouseTake() {
        return "warehouseTake";
    }

    //条件分页查询
    @RequestMapping(value = "warehouseTake", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("warehouseTake")
    @ResponseBody
    public Result warehouseOut(String billNumber,String carTypeName,String startName,String endName,String userName, int currentPage) {
        return warehouseTakeService.findAll(billNumber,carTypeName,startName,endName,userName,currentPage);
    }

    //根据方案编号查询详情
    @RequestMapping(value = "warehouseTakeDetail", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("warehouseTake")
    @ResponseBody
    public Result warehouseTakeDetail(String billNumber){
        return warehouseTakeService.detail(billNumber);
    }

    //----------------------------方案修改出库页面------------------------------
    //根据在库物料记录id、方案编号，把该在库物料从方案中去掉
    @RequestMapping(value = "warehouseTakeDelete", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("warehouseOutAdd")
    @ResponseBody
    public Result warehouseTakeDelete(int warehouseCacheId,String billNumber){
        return warehouseTakeService.warehouseTakeDelete(warehouseCacheId,billNumber);
    }

    //根据选择的在库记录id、数量、车型信息来计算物料的长、体积、重量,返回前端
    @RequestMapping(value = "warehouseTakeAdd", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("warehouseOutAdd")
    @ResponseBody
    public Result warehouseTakeAdd(String billNumber,int id,int chooseCount,int lowHeight,int carWidth){
        return warehouseTakeService.warehouseTakeAdd(billNumber,id, chooseCount,lowHeight,carWidth);
    }

}
