package com.defei.lps.dao;

import com.defei.lps.entity.Warehouse;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarehouseMapper {
    //添加
    int deleteByPrimaryKey(Integer id);

    //添加
    int insertSelective(Warehouse record);

    //修改
    int updateByPrimaryKeySelective(Warehouse record);

    //根据id查询
    Warehouse selectByPrimaryKey(Integer id);
    //根据线路id查询
    List<Warehouse> selectByRouteid(@Param("routeid")int routeId);
    //根据仓库名称查询
    Warehouse selectByWarehousename(@Param("warehousename")String warehouseName);
    //根据仓库编号查询
    Warehouse selectByWarehousenumber(@Param("warehousenumber")String warehouseNumber);
    //条件分页查询
    public List<Warehouse> selectLimitByCondition(
            @Param("warehousename") String warehouseName,
            @Param("warehousenumber") String warehouseNumber,
            @Param("province") String province,
            @Param("city") String city,
            @Param("district") String district,
            @Param("index") int index
    );
    //条件分页查询的总数量
    int selectCountByCondition(
            @Param("warehousename") String warehouseName,
            @Param("warehousenumber") String warehouseNumber,
            @Param("province") String province,
            @Param("city") String city,
            @Param("district") String district
    );
    //查询所有
    List<Warehouse> selectAll();
}