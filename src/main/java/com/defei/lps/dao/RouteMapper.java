package com.defei.lps.dao;

import com.defei.lps.entity.Route;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteMapper {
    //根据id修改
    int deleteByPrimaryKey(Integer id);

    //添加
    int insertSelective(Route record);

    //修改
    int updateByPrimaryKeySelective(Route record);

    //根据id查询
    Route selectByPrimaryKey(Integer id);
    //根据工厂id查询
    List<Route> selectByFactoryid(@Param("factoryid")int factoryId);
    //根据区域id查询
    List<Route> selectByAreaid(@Param("areaid")int areaId);
    //根据线路编号查询
    Route selectByRoutenumber(@Param("routenumber")String routeNumber);
    //根据线路名称查询
    Route selectByRoutename(@Param("routename")String routeName);
    //根据工厂id和区域id查询
    Route selectByFactoryidAndAreaid(@Param("factoryid") int factoryid,
                                     @Param("areaid") int areaid);
    //条件分页查询
    public List<Route> selectLimitByCondition(
            @Param("routename") String routeName,
            @Param("routenumber") String routeNumber,
            @Param("areaid") int areaid,
            @Param("factoryid") int factoryid,
            @Param("index") int index
    );
    //条件分页查询的总数量
    int selectCountByCondition(
            @Param("routename") String routeName,
            @Param("routenumber") String routeNumber,
            @Param("areaid") int areaid,
            @Param("factoryid") int factoryid
    );
    //条件查询
    public List<Route> selectByCondition(
            @Param("routename") String routeName,
            @Param("routenumber") String routeNumber,
            @Param("areaid") int areaid,
            @Param("factoryid") int factoryid
    );
    //根据用户id查询
    List<Route> selectByUserid(@Param("userid") int userId);
    //查询所有
    List<Route> selectAll();
    //根据区域id、中转仓id查询
    List<Route> selectByAreaidAndWarehouseid(
            @Param("areaid") int areaId,
            @Param("warehouseid") int warehouseId);
    //根据工厂id、中转仓id查询
    List<Route> selectByFactoryidAndWarehouseid(
            @Param("factoryid") int factoryId,
            @Param("warehouseid") int warehouseId);
    //根据出发地中转仓id、目的地中转仓id查询
    List<Route> selectByStartWarehouseidAndEndWarehouseid(
            @Param("startwarehouseid") int startWarehouseId,
            @Param("endwarehouseid") int endWarehouseId);
}