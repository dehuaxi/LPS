package com.defei.lps.service;

import com.defei.lps.result.Result;

public interface WarehouseCacheService {
    //条件分页查询
    public Result findAll(String goodCode, String goodName,String supplierCode,String supplierName,String geelyBillNumber,String packState,int warehouseId,String date, int currentPage);
    //根据选择的出发地、目的地、目的地类型获取该线路上所有的在库物料信息
    public Result warehouseCacheByRoute(int startId,int endId,String endType);
    //根据选择的在库记录id、数量、车型信息来计算物料的长、体积、重量
    public Result warehouseCacheCalculate(int id,int chooseCount,int lowHeight,int carWidth);
    //生成装载方案即运输单。该方案在出库确认前是可以调整修改的
    public Result warehouseTakeAdd(int startId,int endId,String endType,String date,String carType,int highLength,int highHeight,int lowLength,int lowHeight,int carWidth,String goodInfos);
    //根据装载方案编号，查询装载方案详情，查询装载方案对应的线路的所有在库记录
    public Result warehouseCacheByBillnumber(String billNumber);
    //翻包
    public Result pack(int id,int packCount);
    //根据返空计划单号查询出所有包含在返空计划单中的在库的物料
    public Result findByReturnBillNumber(String returnBillNumber);
    //返空单中物料确认出库数量后，确认出库操作
    public Result returnOut(String returnBillNumber,String goodInfos);
    //根据条件查询中转仓在库记录
    public Result warehouseCacheByCondition(String goodCode,String goodName,String supplierCode,String supplierName,String geelyBillNumber,String packState,int warehouseId);
}
