package com.defei.lps.service;

import com.defei.lps.result.Result;

public interface PlanCacheService {
    //手动添加计划
    public Result add(int goodId, int count, String date,String remarks);
    //修改
    public Result update(int id, int count,String remarks);
    //修改缺件计划的最大取货数量
    public Result planCacheUpdateMaxcount(int planCacheId, int maxCount);
    //根据id删除未确认的计划
    public Result delete(int id);
    //条件分页查询
    public Result findAll(String goodCode,String goodName,String supplierCode, String supplierName, int routeId, int factoryId, String date,String state,String type,String urgent,int currentPage);
    //根据id查询
    public Result planCacheUpdate(int id);
    //根据供应商编号查询所有的未取货计划
    public Result untakePlanCacheBySupplierCode(String supplierCode);
    //生成总取货计划页面，根据出发地id和目的地id查询所有未确认缺件计划
    public Result planCacheByRoute(int startId,int endId,String endType);
    //选框选中后，根据计划Id查询计划详情
    public Result planCacheDetail(int planCacheId);
    //页面选择某个缺件计划的复选框后，弹出填入数量的模态框，之后点击确认时调用此函数
    public Result planCacheChoose(int planCacheId, String takeDate,int chooseCount,int lowHeight,int carWidth);
    //取消选择某个缺件计划后，根据计划id重新加载该物料的缺件记录、缺件计划信息
    public Result planCacheChooseCancel(int planCacheId);
    //传入缺件报表记录的日期、物料id，修改该日期中该物料的缺件报表记录的结存数量
    public Result updateShortageByGoodAndDate(int goodId,int stock,String date);
}
