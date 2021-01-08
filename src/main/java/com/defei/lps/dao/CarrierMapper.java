package com.defei.lps.dao;

import com.defei.lps.entity.Carrier;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarrierMapper {
    int deleteByPrimaryKey(Integer id);

    int insertSelective(Carrier record);

    Carrier selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Carrier record);
    //根据承运商名称查询
    Carrier selectByCarriername(@Param("carriername")String carrierName);
    //根据承运商编号查询
    Carrier selectByCarriernumber(@Param("carriernumber")String carrierNumber);
    //条件分页查询
    public List<Carrier> selectLimitByCondition(
            @Param("carriername") String carrierName,
            @Param("carriernumber") String carrierNumber,
            @Param("index") int index
    );
    //条件分页查询的总数量
    int selectCountByCondition(
            @Param("carriername") String carrierName,
            @Param("carriernumber") String carrierNumber
    );
    //查询所有
    List<Carrier> selectAll();
}