package com.defei.lps.service;

import com.defei.lps.result.Result;

public interface PlanTakeRecordService {
    //条件分页查询
    public Result findAll(String goodCode, String goodName, String supplierCode, String supplierName, String date,String carTypeName,String overDate, int currentPage);

}
