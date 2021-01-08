package com.defei.lps.controller;

import com.defei.lps.result.Result;
import com.defei.lps.service.GeelyBillCacheService;
import com.defei.lps.service.TransportBillCacheService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TransportBillCacheController {
    @Autowired
    private TransportBillCacheService transportBillCacheService;
    //--------------------------------运输单明细页面--------------------------------
    //跳转到页面
    @RequestMapping(value = "toTransportBillCache", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("transportBillCache")
    public String toTransportBillCache() {
        return "transportBillCache";
    }

    //运输单明细条件分页查询
    @RequestMapping(value = "transportBillCache", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("transportBillCache")
    @ResponseBody
    public Result findAll(String goodCode,String goodName,String supplierCode,String supplierName,String billNumber,String geelyBillNumber,String dateStart,String dateEnd,String carNumber,String carTypeName,String carrierName,int currentPage) {
        return transportBillCacheService.findAll(goodCode,goodName,supplierCode,supplierName,billNumber,geelyBillNumber,dateStart,dateEnd,carNumber,carTypeName,carrierName,currentPage);
    }

    //--------------------------------运输单页面--------------------------------
    //跳转到页面
    @RequestMapping(value = "toTransportBillCacheBill", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("transportBillCacheBill")
    public String toTransportBillCacheBill() {
        return "transportBillCacheBill";
    }

    //运输单条件分页查询
    @RequestMapping(value = "transportBillCacheBill", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("transportBillCacheBill")
    @ResponseBody
    public Result findAllByBillnumber(String billNumber,String geelyBillNumber,String dateStart,String dateEnd,String carNumber,String carTypeName,String carrierName,int currentPage) {
        return transportBillCacheService.findAllByBillnumber(billNumber,geelyBillNumber,dateStart,dateEnd,carNumber,carTypeName,carrierName,currentPage);
    }

    //根据运输单号获取明细
    @RequestMapping(value = "transportBillCacheBillDetail", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("transportBillCacheBillDetail")
    @ResponseBody
    public Result findDetailByBillnumber(String billNumber) {
        return transportBillCacheService.findDetailByBillnumber(billNumber);
    }

    //根据运输单号，打印运输单.跳转到打印页面，把运输单号带进去
    @RequestMapping(value = "transportBillCacheBillPrint", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("transportBillCacheBillPrint")
    public String transportBillCacheBillPrint(String billNumber, Model model) {
        model.addAttribute("billNumber",billNumber);
        return "printTransportBill";
    }

    //运输单确认到货
    @RequestMapping(value = "transportBillRecordAdd", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("transportBillRecordAdd")
    @ResponseBody
    public Result transportBillRecordAdd(String billNumber) {
        return transportBillCacheService.transportBillRecordAdd(billNumber);
    }

    //------------------------------------取货计划上传并绑定PD单页面---------------------------------
    //生成PD单绑定取货计划时，生成运输单
    @RequestMapping(value = "transportBillCacheAdd", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("planTakeUpload")
    @ResponseBody
    public Result addPlan(String planNumber,String carNumber,String driver,String phone,String carTypeName,int highLength,int highHeight,int lowLength,int lowHeight,int carWidth,String carrierName,String money,String remarks,String geelyRealInfo) {
        return transportBillCacheService.addPlan(planNumber,carNumber,driver,phone,carTypeName,highLength,highHeight,lowLength,lowHeight,carWidth,carrierName,money,remarks,geelyRealInfo);
    }

    //------------------------------------中转仓拼车页面---------------------------------
    //中转仓选择物料进行拼车，生成运输单
    @RequestMapping(value = "transportBillCacheAddStock", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("planTakeUpload")
    @ResponseBody
    public Result addBillStock(String carNumber,String driver,String phone,String carTypeName,int highLength,int highHeight,int lowLength,int lowHeight,int carWidth,String carrierName,String money,String remarks,String geelyBillInfo) {
        return transportBillCacheService.addBillStock(carNumber,driver,phone,carTypeName,highLength,highHeight,lowLength,lowHeight,carWidth,carrierName,money,remarks,geelyBillInfo);
    }

    //--------------------------中转仓入库操作页面--------------------------
    //根据传入的在途运输单单号、想要入库的中转仓id，来进行判断，是否可以入库，如果可以就返回运输单详情
    @RequestMapping(value = "transportBillCacheBillDetail2", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("warehouseEntryAdd")
    @ResponseBody
    public Result transportBillCacheBillDetail2(String billNumber,int warehouseId) {
        return transportBillCacheService.transportBillCacheBillDetail2(billNumber,warehouseId);
    }
}
