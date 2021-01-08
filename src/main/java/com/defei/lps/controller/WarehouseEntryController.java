package com.defei.lps.controller;

import com.defei.lps.result.Result;
import com.defei.lps.service.WarehouseEntryService;
import com.defei.lps.service.WarehouseService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class WarehouseEntryController {
    @Autowired
    private WarehouseEntryService warehouseEntryService;
    //--------------------------------页面--------------------------------
    //跳转到页面
    @RequestMapping(value = "toWarehouseEntry", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("warehouseEntry")
    public String toWarehouseEntry() {
        return "warehouseEntry";
    }

    //条件分页查询
    @RequestMapping(value = "warehouseEntry", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("warehouseEntry")
    @ResponseBody
    public Result warehouseEntry(String goodCode, String goodName,String supplierCode,String supplierName,String billNumber,String geelyBillNumber,int warehouseId,String date, int currentPage) {
        return warehouseEntryService.findAll(goodCode,goodName,supplierCode,supplierName,billNumber,geelyBillNumber,warehouseId,date,currentPage);
    }

    //跳转到入库页面
    @RequestMapping(value = "toEntry", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("warehouseEntryAdd")
    public String toEntry() {
        return "warehouseEntryAdd";
    }

    //添加入库记录
    @RequestMapping(value = "warehouseEntryAdd", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("warehouseEntryAdd")
    @ResponseBody
    public Result add(int warehouseId,String billNumber,String goodInfos) {
        return warehouseEntryService.add(warehouseId, billNumber,goodInfos);
    }

}
