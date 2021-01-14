package com.defei.lps.service;

import com.defei.lps.result.Result;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * @author 高德飞
 * @create 2020-12-08 16:15
 */
public interface PlanTakeService {
    //装载方案生成页面，生成方案.返回方案的编号。
    public Result add(int startId,int endId,String endType,String date,String carType,int highLength,int highHeight,int lowLength,int lowHeight,int carWidth,String planCacheInfos);
    //条件分页查询，以计划编号分组统计
    public Result findLimitByCondition(String planNumber,String supplierCode,String supplierName,int routeId,String date,String startName,String endName,String userName,int currentPage);
    //根据计划编号查询详情
    public Result findDetailByPlannumber(String planNumber);
    //根据取货计划id删除，修改对应的缺件计划的计划数量
    public Result planTakeDelete(int id);
    //下载
    public void download(String planNumber,String supplierCode,String supplierName,int routeId,String date,String startName,String endName,String userName, HttpServletResponse response);
    //根据计划编号集合，获取所有的集合详情
    public Result planTakeByNumbers(String planNumbers);
    //传入取货计划id、取货数量、箱数、车宽、车高来计算长度、体积、重量
    public Result planTakeCalculate(int id,int count,int boxCount,int lowHeight,int carWidth);
    //传入信息生成新的取货计划
    public Result planTakeAddRepeat(String carType,int highLength,int highHeight,int lowLength,int lowHeight,int carWidth,String planCacheInfos);
    //根据计划编号查询内容，同物料id合并，并根据供应商编号排序再根据物料编号排序
    public Result planTakeByNumber(String planNumber);
    //上传PD单绑定
    public Result planTakeUpload(MultipartFile[] files, String planNumber);
}
