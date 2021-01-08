package com.defei.lps.controller;

import com.defei.lps.result.Result;
import com.defei.lps.service.WarehouseCacheService;
import com.defei.lps.service.WarehouseEntryService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class WarehouseCacheController {
    @Autowired
    private WarehouseCacheService warehouseCacheService;
    //--------------------------------在库页面--------------------------------
    //跳转到在库页面
    @RequestMapping(value = "toWarehouseCache", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("warehouseCache")
    public String toWarehouseCache() {
        return "warehouseCache";
    }

    //条件分页查询
    @RequestMapping(value = "warehouseCache", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("warehouseCache")
    @ResponseBody
    public Result warehouseCache(String goodCode, String goodName,String supplierCode,String supplierName,String geelyBillNumber,String packState,int warehouseId,String date, int currentPage) {
        return warehouseCacheService.findAll(goodCode,goodName,supplierCode,supplierName,geelyBillNumber,packState,warehouseId,date,currentPage);
    }

    //-------------------------装载方案页面-------------------------------
    //跳转到在库装载方案页面
    @RequestMapping(value = "toWarehouseTakeAdd", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("warehouseTakeAdd")
    public String toWarehouseTakeAdd() {
        return "warehouseTakeAdd";
    }

    //根据选择的出发地、目的地、目的地类型获取该线路上所有的在库物料信息
    @RequestMapping(value = "warehouseCacheByRoute", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("warehouseTakeAdd")
    @ResponseBody
    public Result warehouseCacheByRoute(int startId,int endId,String endType){
        return warehouseCacheService.warehouseCacheByRoute(startId, endId,endType);
    }

    //根据选择的在库记录id、数量、车型信息来计算物料的长、体积、重量,返回前端
    @RequestMapping(value = "warehouseCacheCalculate", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("warehouseTakeAdd")
    @ResponseBody
    public Result warehouseCacheCalculate(int id,int chooseCount,int lowHeight,int carWidth){
        return warehouseCacheService.warehouseCacheCalculate(id, chooseCount,lowHeight,carWidth);
    }

    //生成装载方案。该方案在出库确认前是可以调整修改的
    @RequestMapping(value = "warehouseTakeCreate", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("warehouseTakeAdd")
    @ResponseBody
    public Result warehouseTakeCreate(int startId,int endId,String endType,String date,String carType,int highLength,int highHeight,int lowLength,int lowHeight,int carWidth,String goodInfos){
        return warehouseCacheService.warehouseTakeAdd(startId, endId,endType,date,carType,highLength,highHeight,lowLength,lowHeight,carWidth,goodInfos);
    }
    //------------------------------------出库装载方案绑定车辆信息页面------------------------
    //根据装载方案编号，查询装载方案详情，查询装载方案对应的线路的所有在库记录
    @RequestMapping(value = "warehouseCacheByBillnumber", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("warehouseOutAdd")
    @ResponseBody
    public Result warehouseCacheByBillnumber(String billNumber){
        return warehouseCacheService.warehouseCacheByBillnumber(billNumber);
    }

    //翻包
    @RequestMapping(value = "warehouseCachePack", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("warehouseCachePack")
    @ResponseBody
    public Result pack(int id,int packCount){
        return warehouseCacheService.pack(id,packCount);
    }

    //根据返空计划单号查询出所有包含在返空计划单中的在库的物料
    @RequestMapping(value = "warehouseCacheByReturnBillNumber", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("returnBillOut")
    @ResponseBody
    public Result findByReturnBillNumber(String returnBillNumber){
        return warehouseCacheService.findByReturnBillNumber(returnBillNumber);
    }
    //----------------------生成出库运输单页面--------------------------
    //根据条件查询中转仓在库记录
    @RequestMapping(value = "warehouseCacheByCondition", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("warehouseOutAdd")
    @ResponseBody
    public Result warehouseCacheByCondition(String goodCode,String goodName,String supplierCode,String supplierName,String geelyBillNumber,String packState,int warehouseId){
        return warehouseCacheService.warehouseCacheByCondition(goodCode,goodName,supplierCode,supplierName,geelyBillNumber,packState,warehouseId);
    }
}
