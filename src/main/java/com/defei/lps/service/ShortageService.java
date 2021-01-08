package com.defei.lps.service;

import com.defei.lps.result.Result;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

public interface ShortageService {
    //批量上传
    public Result upload(MultipartFile excelFile,int factoryId);
    //条件分页查询
    public Result findAll(String goodCode, String goodName, String supplierCode, String supplierName, int factoryId,int routeId, String dateStart,String dateEnd,int currentPage);
    //批量上传的模板下载
    public void modelDownload(HttpServletResponse response);
    //查询当天到最大日期的日期集合
    public Result shortageDateList();
}
