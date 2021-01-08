package com.defei.lps.dao;

import com.defei.lps.entity.GeelyBillCache;
import com.defei.lps.entity.Route;
import com.defei.lps.entity.WarehouseCache;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarehouseCacheMapper {
    int deleteByPrimaryKey(Integer id);
    //批量删除
    void deleteBatch(@Param("list")List<WarehouseCache> list);

    int insertSelective(WarehouseCache record);

    WarehouseCache selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(WarehouseCache record);
    //批量更新数量、方案数量
    int updateBatch(@Param("list")List<WarehouseCache> list);

    //根据Pd单、物料id、批次、收容数查询
    WarehouseCache selectByGeelybillnumberAndGoodidAndBatchAndOneboxcount(
            @Param("geelybillnumber") String geelyBillNumber,
            @Param("goodid") int goodId,
            @Param("oneboxcount") int oneBoxCount,
            @Param("batch") String batch);
    //条件分页查询
    public List<WarehouseCache> selectLimitByCondition(
            @Param("goodcode") String goodCode,
            @Param("goodname") String goodName,
            @Param("suppliercode") String supplierCode,
            @Param("suppliername") String supplierName,
            @Param("geelybillnumber") String geelyBillNumber,
            @Param("packstate") String packState,
            @Param("warehouseid") int warehouseId,
            @Param("date") String date,
            @Param("index") int index
    );
    //条件分页查询的总数量
    int selectCountByCondition(
            @Param("goodcode") String goodCode,
            @Param("goodname") String goodName,
            @Param("suppliercode") String supplierCode,
            @Param("suppliername") String supplierName,
            @Param("geelybillnumber") String geelyBillNumber,
            @Param("packstate") String packState,
            @Param("warehouseid") int warehouseId,
            @Param("date") String date
    );
    //条件查询
    public List<WarehouseCache> selectByCondition(
            @Param("goodcode") String goodCode,
            @Param("goodname") String goodName,
            @Param("suppliercode") String supplierCode,
            @Param("suppliername") String supplierName,
            @Param("geelybillnumber") String geelyBillNumber,
            @Param("packstate") String packState,
            @Param("warehouseid") int warehouseId,
            @Param("date") String date
    );
    //根据线路集合查询
    List<WarehouseCache> selectByRouteids(@Param("routeList")List<Route> list);
}