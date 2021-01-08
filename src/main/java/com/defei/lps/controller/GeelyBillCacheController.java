package com.defei.lps.controller;

import com.defei.lps.result.Result;
import com.defei.lps.service.GeelyBillCacheService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class GeelyBillCacheController {
    @Autowired
    private GeelyBillCacheService geelyBillCacheService;
    //--------------------------------未回执吉利单据页面--------------------------------
    //跳转到页面
    @RequestMapping(value = "toGeelyBillCache", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("geelyBillCache")
    public String toGeelyBillCache() {
        return "geelyBillCache";
    }

    //条件分页查询
    @RequestMapping(value = "geelyBillCache", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("geelyBillCache")
    @ResponseBody
    public Result findAll(String goodCode,String goodName,String supplierCode,String supplierName,String billNumber,String urgent,int routeId,int factoryId,String uploadDate,int currentPage) {
        return geelyBillCacheService.findAll(goodCode,goodName,supplierCode,supplierName,billNumber,urgent,routeId,factoryId,uploadDate,currentPage);
    }

    //根据id删除
    @RequestMapping(value = "geelyBillCacheDelete", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("geelyBillCacheDelete")
    @ResponseBody
    public Result delete(int id) {
        return geelyBillCacheService.delete(id);
    }

    //--------------------------------计划PD单绑定页面--------------------------------
    //批量添加
    @RequestMapping(value = "billCacheAdd", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("billCacheAdd")
    @ResponseBody
    public Result upload(@RequestParam(value="file",required=false) MultipartFile[] files,String supplierCode,int factoryId) {
        return geelyBillCacheService.upload(files,supplierCode,factoryId);
    }


    //根据计划id、PD单记录id进行绑定操作
    @RequestMapping(value = "billCacheUpdate", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("billCacheAdd")
    @ResponseBody
    public Result billCacheUpdate(int planCacheId,String billCacheIds) {
        return geelyBillCacheService.billCacheUpdate(planCacheId,billCacheIds);
    }

    //----------------------记录单据回执页面---------------------------
    //跳转到页面
    @RequestMapping(value = "toGeelyBillRecordAdd", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("geelyBillRecordAdd")
    public String toGeelyBillRecordAdd() {
        return "geelyBillRecordAdd";
    }

    //扫描PD单编号，获取PD单内容明细
    @RequestMapping(value = "geelyBillCacheDetail", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("geelyBillRecordAdd")
    @ResponseBody
    public Result geelyBillCacheDetail(String geelyBillNumber) {
        return geelyBillCacheService.geelyBillCacheDetail(geelyBillNumber);
    }

    //在途吉利单转为完结吉利单
    @RequestMapping(value = "geelyBillRecordAdd", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("geelyBillRecordAdd")
    @ResponseBody
    public Result geelyBillRecordAdd(int geelyBillCacheId,int count,String remarks) {
        return geelyBillCacheService.geelyBillRecordAdd(geelyBillCacheId,count,remarks);
    }
}
