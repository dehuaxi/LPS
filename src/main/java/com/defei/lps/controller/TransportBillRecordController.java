package com.defei.lps.controller;

import com.defei.lps.result.Result;
import com.defei.lps.service.TransportBillCacheService;
import com.defei.lps.service.TransportBillRecordService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TransportBillRecordController {
    @Autowired
    private TransportBillRecordService transportBillRecordService;
    //--------------------------------完结运输单明细页面--------------------------------
    //跳转到页面
    @RequestMapping(value = "toTransportBillRecord", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("transportBillRecord")
    public String toTransportBillRecord() {
        return "transportBillRecord";
    }

    //运输单明细条件分页查询
    @RequestMapping(value = "transportBillRecord", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("transportBillRecord")
    @ResponseBody
    public Result findAll(String goodCode,String goodName,String supplierCode,String supplierName,String billNumber,String geelyBillNumber,String dateStart,String dateEnd,String carNumber,String carTypeName,String carrierName,int currentPage) {
        return transportBillRecordService.findAll(goodCode,goodName,supplierCode,supplierName,billNumber,geelyBillNumber,dateStart,dateEnd,carNumber,carTypeName,carrierName,currentPage);
    }

}
