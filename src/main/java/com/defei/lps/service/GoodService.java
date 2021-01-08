package com.defei.lps.service;

import com.defei.lps.result.Result;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

public interface GoodService {
    //添加
    public Result add(String goodCode, String goodName, int factoryId, int supplierId, int oneBoxCount, int binCount,  int oneCarCount, int maxStock, int triggerStock, int quotaRatio, String boxType, int boxLength, int boxWidth, int boxHeight, int packBoxLength, int packBoxWidth, int packBoxHeight, String packBoxWeight,String boxWeight,int returnRatio,int oneTrayBoxCount,int oneTrayLayersCount,int trayRatio,int trayLength,int trayWidth,int trayHeight,String packRemarks,String receiver);
    //批量上传，添加新物料，修改旧物料
    public Result upload(MultipartFile excelFile);
    //根据id修改
    public Result update(int id, String goodCode, String goodName, int factoryId, int supplierId, int oneBoxCount, int binCount, int oneCarCount, int maxStock, int triggerStock, int quotaRatio, String boxType, int boxLength, int boxWidth, int boxHeight,int packBoxLength, int packBoxWidth, int packBoxHeight, String packBoxWeight, String boxWeight,int returnRatio,int oneTrayBoxCount,int oneTrayLayersCount,int trayRatio,int trayLength,int trayWidth,int trayHeight,String packRemarks,String receiver);
    //根据id删除
    public Result delete(int id);
    //条件分页查询
    public Result findAll(String goodCode, String goodName, String supplierCode, String supplierName, String boxType, int factoryId, int currentPage);
    //根据id查询
    public Result goodById(int id);
    //下载
    public void download(String goodCode, String goodName, String supplierCode, String supplierName, String boxType,int factoryId, HttpServletResponse response);
    //批量上传的模板下载
    public void modelDownload(HttpServletResponse response);
    //根据工厂id和物料名称查询
    public Result goodLikeNameAndFactoryId(int factoryId,String goodName);
}
