package com.defei.lps.dao;

import com.defei.lps.entity.RouteWarehouse;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteWarehouseMapper {
    //删除
    int deleteByPrimaryKey(Integer id);
    //根据线路id删除
    void deleteByRouteid(@Param("routeid")int routeId);

    //添加
    int insertSelective(RouteWarehouse record);

    //修改
    int updateByPrimaryKeySelective(RouteWarehouse record);

    //根据id查询
    RouteWarehouse selectByPrimaryKey(Integer id);
    //根据线路id查询
    List<RouteWarehouse> selectByRouteid(@Param("routeid")int routeId);
}