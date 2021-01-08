package com.defei.lps.dao;

import com.defei.lps.entity.Area;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AreaMapper {
    //删除
    int deleteByPrimaryKey(Integer id);

    //新增
    int insertSelective(Area record);

    //修改
    int updateByPrimaryKeySelective(Area record);

    //根据id查询
    Area selectByPrimaryKey(Integer id);
    //根据编号查询
    Area selectByAreanumber(@Param("areanumber")String areaNumber);
    //根据名称查询
    Area selectByAreaname(@Param("areaname")String areaName);
    //条件分页查询
    public List<Area> selectLimitByCondition(
            @Param("areaname") String areaName,
            @Param("areanumber") String areaNumber,
            @Param("index") int index
    );
    //条件分页查询的总数量
    int selectCountByCondition(
            @Param("areaname") String areaName,
            @Param("areanumber") String areaNumber
    );
    //查询所有
    List<Area> selectAll();
    //根据线路id查询
    List<Area> selectByRouteid(@Param("routeid") int routeId);
}