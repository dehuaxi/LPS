package com.defei.lps.service;

import com.defei.lps.result.Result;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

public interface SupplierService {
    //添加
    public Result add(String supplierCode, String supplierName,String abbreviation,String contact,String phone,String province,String city,String district,String address,int areaId,int factoryId,String longitude,String latitude,String transitDay);
    //批量上传
    public Result upload(MultipartFile excelFile);
    //根据id修改
    public Result update(int id,String abbreviation,String contact,String phone,String province,String city,String district,String address,String longitude,String latitude,String transitDay);
    //根据id删除
    public Result delete(int id);
    //条件分页查询
    public Result findAll(String supplierCode, String supplierName,String province,String city,String district,int areaId,int factoryId,int currentPage);
    //根据id查询
    public Result supplierById(int id);
    //下载
    public void supplierDownload(String supplierCode, String supplierName, String province, String city, String district, int areaId, int factoryId, HttpServletResponse response);
    //批量上传的模板下载
    public void modelDownload(HttpServletResponse response);
    //根据工厂id查询
    public Result supplierByFactoryid(int factoryId);
    //根据供应商名称查询不重复的供应商
    public Result supplierLikeName(String supplierName);
}
