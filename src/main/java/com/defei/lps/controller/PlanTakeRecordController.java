package com.defei.lps.controller;

import com.defei.lps.result.Result;
import com.defei.lps.service.PlanRecordService;
import com.defei.lps.service.PlanTakeRecordService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PlanTakeRecordController {
    @Autowired
    private PlanTakeRecordService planTakeRecordService;

    //跳转到页面
    @RequestMapping(value = "toPlanTakeRecord", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("planTakeRecord")
    public String toPlanTakeRecord() {
        return "planTakeRecord";
    }

    //条件分页查询
    @RequestMapping(value = "planTakeRecord", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("planTakeRecord")
    @ResponseBody
    public Result planTakeRecord(String goodCode, String goodName, String supplierCode, String supplierName, String date,String carTypeName,String overDate, int currentPage) {
        return planTakeRecordService.findAll(goodCode,goodName,supplierCode,supplierName,date,carTypeName,overDate,currentPage);
    }

}
