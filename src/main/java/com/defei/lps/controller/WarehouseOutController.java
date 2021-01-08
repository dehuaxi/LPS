package com.defei.lps.controller;

import com.defei.lps.result.Result;
import com.defei.lps.service.WarehouseOutService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class WarehouseOutController {
    @Autowired
    private WarehouseOutService warehouseOutService;
    //--------------------------------页面--------------------------------
    //跳转到页面
    @RequestMapping(value = "toWarehouseOut", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("warehouseOut")
    public String toWarehouseOut() {
        return "warehouseOut";
    }

    //条件分页查询
    @RequestMapping(value = "warehouseOut", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("warehouseOut")
    @ResponseBody
    public Result warehouseOut(String goodCode, String goodName,String supplierCode,String supplierName,String billNumber,String geelyBillNumber,int warehouseId,String date, int currentPage) {
        return warehouseOutService.findAll(goodCode,goodName,supplierCode,supplierName,billNumber,geelyBillNumber,warehouseId,date,currentPage);
    }

    //------------------------------出库生成运输的页面--------------------------------
    //跳转到出库生成运输单页面
    @RequestMapping(value = "toWarehouseOutAdd", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("warehouseOutAdd")
    public String toWarehouseOutAdd() {
        return "warehouseOutAdd";
    }

    //生成出库记录、生成运输单
    @RequestMapping(value = "warehouseOutAdd", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("warehouseOutAdd")
    @ResponseBody
    public Result warehouseOutAdd(int startId,int endId,String routeType,String carTypeName,String carNumber,String driver, String phone,int highLength, int highHeight, int lowLength, int lowHeight, int carWidth,String carrierName, String money, String remarks,String goodInfo) {
        return warehouseOutService.warehouseOutAdd(startId,endId,routeType,carTypeName,carNumber,driver,phone,highLength,highHeight,lowLength,lowHeight,carWidth,carrierName,money,remarks,goodInfo);
    }

//------------------------------出库修改装载方案绑定车辆信息页面----------------------------
    //跳转到出库装载方案调整页面
    @RequestMapping(value = "toWarehouseTakeDelete", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("warehouseTakeDelete")
    public String toWarehouseTakeDelete(String billNumber, Model model) {
        model.addAttribute("billNumber",billNumber);
        return "warehouseTakeDelete";
    }

    //添加出库记录
    @RequestMapping(value = "warehouseOutAdd2", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("toWarehouseTakeDelete")
    @ResponseBody
    public Result add(String billNumber, String carNumber, String driver, String phone, String carTypeName, int highLength, int highHeight, int lowLength, int lowHeight, int carWidth, String carrierName, String money, String remarks) {
        return warehouseOutService.add(billNumber, carNumber,driver,phone,carTypeName,highLength,highHeight,lowLength,lowHeight,carWidth,carrierName,money,remarks);
    }

}
