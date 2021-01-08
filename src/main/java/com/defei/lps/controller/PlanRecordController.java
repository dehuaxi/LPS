package com.defei.lps.controller;

import com.defei.lps.result.Result;
import com.defei.lps.service.PlanRecordService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PlanRecordController {
    @Autowired
    private PlanRecordService planRecordService;

    //跳转到页面
    @RequestMapping(value = "toPlanRecord", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("planRecord")
    public String toPlanRecord() {
        return "planRecord";
    }

    //条件分页查询
    @RequestMapping(value = "planRecord", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("planRecord")
    @ResponseBody
    public Result findAll(String goodCode, String goodName, String supplierCode, String supplierName, String date,String urgent,int routeId,String type,String createDate,String overDate, int currentPage) {
        return planRecordService.findAll(goodCode,goodName,supplierCode,supplierName,date,urgent,routeId,type,createDate,overDate,currentPage);
    }

}
