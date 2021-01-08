package com.defei.lps.controller;

import com.defei.lps.result.Result;
import com.defei.lps.service.GoodService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

@Controller
public class GoodController {
    @Autowired
    private GoodService goodService;
    //--------------------------------页面--------------------------------
    //跳转到页面
    @RequestMapping(value = "toGood", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("good")
    public String toGood() {
        return "good";
    }

    //条件分页查询
    @RequestMapping(value = "good", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("good")
    @ResponseBody
    public Result findAll(String goodCode, String goodName, String supplierCode, String supplierName, String boxType, int factoryId, int currentPage) {
        return goodService.findAll(goodCode,goodName,supplierCode,supplierName,boxType,factoryId,currentPage);
    }

    //添加
    @RequestMapping(value = "goodAdd", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("goodAdd")
    @ResponseBody
    public Result add(String goodCode, String goodName, int factoryId, int supplierId, int oneBoxCount, int binCount,  int oneCarCount, int maxStock, int triggerStock, int quotaRatio, String boxType, int boxLength, int boxWidth, int boxHeight, int packBoxLength, int packBoxWidth, int packBoxHeight, String packBoxWeight,String boxWeight,int returnRatio,int oneTrayBoxCount,int oneTrayLayersCount,int trayRatio,int trayLength,int trayWidth,int trayHeight,String packRemarks,String receiver) {
        return goodService.add(goodCode, goodName,factoryId,supplierId,oneBoxCount,binCount,oneCarCount,maxStock,triggerStock,quotaRatio,boxType,boxLength,boxWidth,boxHeight,packBoxLength,packBoxWidth,packBoxHeight,packBoxWeight,boxWeight,returnRatio,oneTrayBoxCount,oneTrayLayersCount,trayRatio,trayLength,trayWidth,trayHeight,packRemarks,receiver);
    }

    //批量添加，添加新物料，修改旧物料
    @RequestMapping(value = "goodAddUpload", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("goodAdd")
    @ResponseBody
    public Result upload(@RequestParam(value="file",required=false) MultipartFile excelFile) {
        return goodService.upload(excelFile);
    }

    //删除
    @RequestMapping(value = "goodDelete", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("goodDelete")
    @ResponseBody
    public Result delete(int id) {
        return goodService.delete(id);
    }

    //修改
    @RequestMapping(value = "goodUpdate", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("goodUpdate")
    @ResponseBody
    public Result update(int id, String goodCode, String goodName, int factoryId, int supplierId, int oneBoxCount, int binCount, int oneCarCount, int maxStock, int triggerStock, int quotaRatio, String boxType, int boxLength, int boxWidth, int boxHeight,int packBoxLength, int packBoxWidth, int packBoxHeight, String packBoxWeight, String boxWeight,int returnRatio,int oneTrayBoxCount,int oneTrayLayersCount,int trayRatio,int trayLength,int trayWidth,int trayHeight,String packRemarks,String receiver) {
        return goodService.update(id,goodCode,goodName,factoryId,supplierId,oneBoxCount,binCount,oneCarCount,maxStock,triggerStock,quotaRatio,boxType,boxLength,boxWidth,boxHeight,packBoxLength,packBoxWidth,packBoxHeight,packBoxWeight,boxWeight,returnRatio,oneTrayBoxCount,oneTrayLayersCount,trayRatio,trayLength,trayWidth,trayHeight,packRemarks,receiver);
    }

    //根据id查询
    @RequestMapping(value = "goodById", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public Result supplierById(int id){
        return goodService.goodById(id);
    }

    //下载
    @RequestMapping(value = "goodDownload", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("goodDownload")
    public void goodDownload(String goodCode, String goodName, String supplierCode, String supplierName, String boxType,int factoryId, HttpServletResponse response) {
        goodService.download(goodCode,goodName,supplierCode,supplierName,boxType,factoryId,response);
    }

    //批量上传的模板下载
    @RequestMapping(value = "goodModalDownload", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("goodAdd")
    public void modelDownload(HttpServletResponse response) {
        goodService.modelDownload(response);
    }

    //根据工厂id和物料名称查询
    @RequestMapping(value = "goodLikeNameAndFactoryId", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public Result goodLikeNameAndFactoryId(int factoryId,String goodName){
        return goodService.goodLikeNameAndFactoryId(factoryId,goodName);
    }
}
