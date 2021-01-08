package com.defei.lps.controller;

import com.defei.lps.result.Result;
import com.defei.lps.service.PlanHandleRecordService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PlanHandleRecordController {
    @Autowired
    private PlanHandleRecordService planHandleRecordService;
    //--------------------------------供应商页面--------------------------------
    //跳转到页面
    @RequestMapping(value = "toPlanHandleRecord", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("planHandleRecord")
    public String toPlanHandleRecord() {
        return "planHandleRecord";
    }

    //条件分页查询
    @RequestMapping(value = "planHandleRecord", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("planHandleRecord")
    @ResponseBody
    public Result findAll(String goodCode,String goodName,String supplierCode, String supplierName, String date,int currentPage) {
        return planHandleRecordService.findAll(goodCode,goodName,supplierCode,supplierName,date,currentPage);
    }

}
