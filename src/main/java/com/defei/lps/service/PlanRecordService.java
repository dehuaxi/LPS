package com.defei.lps.service;

import com.defei.lps.result.Result;

public interface PlanRecordService {
    //条件分页查询
    public Result findAll(String goodCode, String goodName, String supplierCode, String supplierName, String date,String urgent,int routeId,String type,String createDate,String overDate, int currentPage);

}
