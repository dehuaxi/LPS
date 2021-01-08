package com.defei.lps.controller;

import com.defei.lps.result.Result;
import com.defei.lps.service.GeelyBillRecordService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;

@Controller
public class GeelyBillRecordController {
    @Autowired
    private GeelyBillRecordService geelyBillRecordService;
    //--------------------------------记录页面--------------------------------
    //跳转到页面
    @RequestMapping(value = "toGeelyBillRecord", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("geelyBillRecord")
    public String toGeelyBillRecord() {
        return "geelyBillRecord";
    }

    //条件分页查询
    @RequestMapping(value = "geelyBillRecord", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("geelyBillRecord")
    @ResponseBody
    public Result findAll(String goodCode,String goodName,String supplierCode,String supplierName,String billNumber,String batch,String needBind,String bindBillNumber,String uploadDate,String receiveDateStart,String receiveDateEnd,int currentPage) {
        return geelyBillRecordService.findAll(goodCode,goodName,supplierCode,supplierName,billNumber,batch,needBind,bindBillNumber,uploadDate,receiveDateStart,receiveDateEnd,currentPage);
    }

    //下载记录
    @RequestMapping(value = "geelyBillRecordDownload", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("geelyBillRecordDownload")
    public void geelyBillRecordDownload(String goodCode,String goodName,String supplierCode,String supplierName,String billNumber,String planDate,String urgent,String routeId,String uploadDate,String receiveDateStart,String receiveDateEnd,HttpServletResponse response) {
        geelyBillRecordService.geelyBillRecordDownload(goodCode,goodName,supplierCode,supplierName,billNumber,planDate,urgent,routeId,uploadDate,receiveDateStart,receiveDateEnd,response);
    }

    //把多送的吉利单号补的吉利单绑定到实收数>单据数的吉利单据记录上
    @RequestMapping(value = "geelyBillRecordBind", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("geelyBillRecordBind")
    @ResponseBody
    public Result geelyBillRecordBind(int id,String billNumber) {
        return geelyBillRecordService.geelyBillRecordBind(id,billNumber);
    }

}
