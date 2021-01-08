package com.defei.lps.dao;

import com.defei.lps.entity.Car;
import com.defei.lps.entity.Factory;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarMapper {
    int deleteByPrimaryKey(Integer id);

    int insertSelective(Car record);

    Car selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Car record);
    //根据车牌查询
    Car selectByCarNumber(@Param("carnumber")String carNumber);
    //条件分页查询
    public List<Car> selectLimitByCondition(
            @Param("carnumber") String carNumber,
            @Param("carrierid") int carrierId,
            @Param("cartypeid") int carTypeId,
            @Param("index") int index
    );
    //条件分页查询的总数量
    int selectCountByCondition(
            @Param("carnumber") String carNumber,
            @Param("carrierid") int carrierId,
            @Param("cartypeid") int carTypeId
    );
    //根据车型id查询
    List<Car> selectByCartypeid(@Param("cartypeid") int carTypeId);
}