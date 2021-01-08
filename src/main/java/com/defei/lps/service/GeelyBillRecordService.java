package com.defei.lps.service;

import com.defei.lps.result.Result;

import javax.servlet.http.HttpServletResponse;

public interface GeelyBillRecordService {
    //条件分页查询
    public Result findAll(String goodCode, String goodName, String supplierCode, String supplierName, String billNumber, String batch,String needBind,String bindBillNumber, String uploadDate,String receiveDateStart,String receiveDateEnd, int currentPage);
    //下载
    public void geelyBillRecordDownload(String goodCode, String goodName, String supplierCode, String supplierName, String billNumber, String batch,String needBind,String bindBillNumber, String uploadDate, String receiveDateStart, String receiveDateEnd, HttpServletResponse response);
    //把多送的吉利单号补的吉利单绑定到实收数>单据数的吉利单据记录上
    public Result geelyBillRecordBind(int id,String billNumber);
}
