package com.defei.lps.service;

import com.defei.lps.result.Result;

public interface WarehouseOutService {
    //条件分页查询
    public Result findAll(String goodCode, String goodName, String supplierCode, String supplierName, String billNumber, String geelyBillNumber, int warehouseId, String date, int currentPage);

    //装载方案确认后，出库操作
    public Result add(String billNumber, String carNumber, String driver, String phone, String carTypeName, int highLength, int highHeight, int lowLength, int lowHeight, int carWidth, String carrierName, String money, String remarks);

    //生成出库记录、生成运输单
    public Result warehouseOutAdd(int startId,int endId,String routeType,String carTypeName, String carNumber, String driver, String phone, int highLength, int highHeight, int lowLength, int lowHeight, int carWidth, String carrierName, String money, String remarks, String goodInfo);
}
