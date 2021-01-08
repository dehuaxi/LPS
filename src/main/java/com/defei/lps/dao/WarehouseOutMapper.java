package com.defei.lps.dao;

import com.defei.lps.entity.WarehouseEntry;
import com.defei.lps.entity.WarehouseOut;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface WarehouseOutMapper {
    int deleteByPrimaryKey(Integer id);

    int insertSelective(WarehouseOut record);
    void insertBatch(@Param("list") List<WarehouseOut> list);

    WarehouseOut selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(WarehouseOut record);
    //条件分页查询
    public List<WarehouseOut> selectLimitByCondition(
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
    //根据装载方案编号查询
    List<WarehouseOut> selectByBillnumber(@Param("billnumber") String billNumber);
}