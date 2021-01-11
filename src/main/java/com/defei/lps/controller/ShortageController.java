package com.defei.lps.controller;

import com.defei.lps.result.Result;
import com.defei.lps.service.ShortageService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

@Controller
public class ShortageController {
    @Autowired
    private ShortageService shortageService;
    //跳转到页面
    @RequestMapping(value = "toShortage", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("shortage")
    public String toShortage() {
        return "shortage";
    }

    //条件分页查询
    @RequestMapping(value = "shortage", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("shortage")
    @ResponseBody
    public Result findAll(String goodCode, String goodName, String supplierCode, String supplierName, int factoryId,int routeId, String dateStart,String dateEnd,int currentPage) {
        return shortageService.findAll(goodCode,goodName,supplierCode,supplierName,factoryId,routeId,dateStart,dateEnd,currentPage);
    }

    //批量添加
    @RequestMapping(value = "shortageAddUpload", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("shortageAdd")
    @ResponseBody
    public Result upload(@RequestParam(value="file",required=false) MultipartFile excelFile,int factoryId) {
        return shortageService.upload(excelFile,factoryId);
    }

    //批量上传的模板下载
    @RequestMapping(value = "shortageModalDownload", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("shortageAdd")
    public void shortageModalDownload(HttpServletResponse response) {
        shortageService.modelDownload(response);
    }

    //查询当天到最大日期的日期集合
    @RequestMapping(value = "shortageDateList", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public Result shortageDateList() {
        return shortageService.shortageDateList();
    }
}
