package com.defei.lps.service;

import com.defei.lps.result.Result;

public interface WarehouseTakeService {
    //条件分页查询，以装载方案编号分组
    public Result findAll(String billNumber,String carTypeName,String startName,String endName,String userName, int currentPage);
    //获取装载方案详情
    public Result detail(String billNumber);
    //根据在库物料记录id、方案编号，把该在库物料从方案中去掉
    public Result warehouseTakeDelete(int warehouseCacheId,String billNumber);
    //根据选择的在库记录id、数量、车型信息来计算物料的长、体积、重量,返回前端
    public Result warehouseTakeAdd(String billNumber,int id,int chooseCount,int lowHeight,int carWidth);
}
