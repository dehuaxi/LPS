package com.defei.lps.service;

import com.defei.lps.result.Result;
import org.springframework.web.multipart.MultipartFile;

public interface TransportBillCacheService {
    //PD单上传页面，生成运输单
    public Result addPlan(String planNumber,String carNumber,String driver,String phone,String carTypeName,int highLength,int highHeight,int lowLength,int lowHeight,int carWidth,String carrierName,String money,String remarks,String geelyRealInfo);
    //中转仓在库拼车，生成运输单
    public Result addBillStock(String carNumber,String driver,String phone,String carTypeName,int highLength,int highHeight,int lowLength,int lowHeight,int carWidth,String carrierName,String money,String remarks,String geelyBillInfo);
    //运输单明细条件分页查询
    public Result findAll(String goodCode,String goodName,String supplierCode,String supplierName,String billNumber,String geelyBillNumber,String dateStart,String dateEnd,String carNumber,String carTypeName,String carrierName,int currentPage);
    //按照运输单号汇总条件分页查询
    public Result findAllByBillnumber(String billNumber,String geelyBillNumber,String dateStart,String dateEnd,String carNumber,String carTypeName,String carrierName,int currentPage);
    //据运输单号获取明细
    public Result findDetailByBillnumber(String billNumber);
    //根据传入的在途运输单单号、想要入库的中转仓id，来进行判断，是否可以入库，如果可以就返回运输单详情
    public Result transportBillCacheBillDetail2(String billNumber,int warehouseId);
    //运输单确认到达目的地操作
    public Result transportBillRecordAdd(String billNumber);
}
