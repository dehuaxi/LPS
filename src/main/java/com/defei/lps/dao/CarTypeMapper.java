package com.defei.lps.dao;

import com.defei.lps.entity.CarType;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarTypeMapper {
    int deleteByPrimaryKey(Integer id);

    int insertSelective(CarType record);

    CarType selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(CarType record);

    List<CarType> selectAll();
    //根据车型名称查询
    CarType selectByName(@Param("cartypename")String carTypeName);
}