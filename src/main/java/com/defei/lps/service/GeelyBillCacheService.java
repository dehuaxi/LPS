package com.defei.lps.service;

import com.defei.lps.result.Result;
import org.springframework.web.multipart.MultipartFile;

public interface GeelyBillCacheService {
    //上传PD单
    public Result upload(MultipartFile[] files,String supplierCode,int factoryId);
    //条件分页查询
    public Result findAll(String goodCode,String goodName,String supplierCode,String supplierName,String billNumber,String urgent,int routeId,int factoryId,String uploadDate,int currentPage);
    //根据id删除
    public Result delete(int id);
    //根据计划id查询出对应的PD单
    public Result billCacheByPlancacheid(int planCacheId);
    //根据计划id、PD单记录id进行绑定操作
    public Result billCacheUpdate(int planCacheId,String billCacheIds);
    //根据扫描的PD单获取所有在途记录
    public Result billCacheByBillnumber(String billNumber);
    //扫描PD单编号，获取PD单内容明细
    public Result geelyBillCacheDetail(String geelyBillNumber);
    //在途吉利单转为完结吉利单
    public Result geelyBillRecordAdd(int geelyBillCacheId,int count,String remarks);
}
