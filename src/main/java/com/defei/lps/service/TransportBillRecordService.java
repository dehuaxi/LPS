package com.defei.lps.service;

import com.defei.lps.result.Result;

public interface TransportBillRecordService {
    //完结运输单明细条件分页查询
    public Result findAll(String goodCode,String goodName,String supplierCode,String supplierName,String billNumber,String geelyBillNumber,String dateStart,String dateEnd,String carNumber,String carTypeName,String carrierName,int currentPage);
}
