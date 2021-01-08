package com.defei.lps.controller;

import com.defei.lps.result.Result;
import com.defei.lps.service.SupplierService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

@Controller
public class SupplierController {
    @Autowired
    private SupplierService supplierService;
    //--------------------------------供应商页面--------------------------------
    //跳转到页面
    @RequestMapping(value = "toSupplier", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("supplier")
    public String toSupplier() {
        return "supplier";
    }

    //条件分页查询
    @RequestMapping(value = "supplier", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("supplier")
    @ResponseBody
    public Result findAll(String supplierCode, String supplierName,String province,String city,String district,int areaId,int factoryId, int currentPage) {
        return supplierService.findAll(supplierCode,supplierName,province,city,district,areaId,factoryId,currentPage);
    }

    //添加
    @RequestMapping(value = "supplierAdd", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("supplierAdd")
    @ResponseBody
    public Result add(String supplierCode, String supplierName,String abbreviation,String contact,String phone,String province,String city,String district,String address,int areaId,int factoryId,String longitude,String latitude,String transitDay) {
        return supplierService.add(supplierCode, supplierName,abbreviation,contact,phone,province,city,district,address,areaId,factoryId,longitude,latitude,transitDay);
    }

    //批量添加
    @RequestMapping(value = "supplierAddUpload", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("supplierAdd")
    @ResponseBody
    public Result upload(@RequestParam(value="file",required=false) MultipartFile excelFile) {
        return supplierService.upload(excelFile);
    }

    //删除
    @RequestMapping(value = "supplierDelete", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("supplierDelete")
    @ResponseBody
    public Result delete(int id) {
        return supplierService.delete(id);
    }

    //修改
    @RequestMapping(value = "supplierUpdate", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("supplierUpdate")
    @ResponseBody
    public Result update(int id,String abbreviation,String contact,String phone,String province,String city,String district,String address,String longitude,String latitude,String transitDay) {
        return supplierService.update(id,abbreviation,contact,phone,province,city,district,address,longitude,latitude,transitDay);
    }

    //根据id查询
    @RequestMapping(value = "supplierById", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public Result supplierById(int id){
        return supplierService.supplierById(id);
    }

    //下载
    @RequestMapping(value = "supplierDownload", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("supplierDownload")
    public void supplierDownload(String supplierCode, String supplierName,String province, String city, String district, int areaId, int factoryId, HttpServletResponse response) {
        supplierService.supplierDownload(supplierCode,supplierName,province,city,district,areaId,factoryId,response);
    }

    //批量上传的模板下载
    @RequestMapping(value = "supplierModalDownload", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("supplierAdd")
    public void supplierModalDownload(HttpServletResponse response) {
        supplierService.modelDownload(response);
    }

    //根据工厂id查询
    @RequestMapping(value = "supplierByFactoryid", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public Result supplierByFactoryid(int factoryId){
        return supplierService.supplierByFactoryid(factoryId);
    }

    //根据供应商名称查询不重复的供应商
    @RequestMapping(value = "supplierLikeName", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public Result supplierLikeName(String supplierName){
        return supplierService.supplierLikeName(supplierName);
    }
}
