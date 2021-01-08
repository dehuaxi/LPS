package com.defei.lps.dao;

import com.defei.lps.entity.WarehouseCache;
import com.defei.lps.entity.WarehouseEntry;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarehouseEntryMapper {
    int deleteByPrimaryKey(Integer id);

    int insertSelective(WarehouseEntry record);

    WarehouseEntry selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(WarehouseEntry record);
    //条件分页查询
    public List<WarehouseEntry> selectLimitByCondition(
            @Param("goodcode") String goodCode,
            @Param("goodname") String goodName,
            @Param("suppliercode") String supplierCode,
            @Param("suppliername") String supplierName,
            @Param("billnumber") String billNumber,
            @Param("geelybillnumber") String geelyBillNumber,
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
            @Param("billnumber") String billNumber,
            @Param("geelybillnumber") String geelyBillNumber,
            @Param("warehouseid") int warehouseId,
            @Param("date") String date
    );
    //根据运输单查询
    List<WarehouseEntry> selectByBillnumber(@Param("billnumber") String billNumber);
}