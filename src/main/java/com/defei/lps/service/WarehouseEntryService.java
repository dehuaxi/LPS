package com.defei.lps.service;

import com.defei.lps.result.Result;

public interface WarehouseEntryService {
    //条件分页查询
    public Result findAll(String goodCode, String goodName,String supplierCode,String supplierName,String billNumber,String geelyBillNumber,int warehouseId,String date, int currentPage);
    //收货操作
    public Result add(int warehouseId,String billNumber,String goodInfos);
}
