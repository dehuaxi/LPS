package com.defei.lps.dao;

import com.defei.lps.entity.Factory;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FactoryMapper {
    int deleteByPrimaryKey(Integer id);

    int insertSelective(Factory record);

    int updateByPrimaryKeySelective(Factory record);

    //根据id查询
    Factory selectByPrimaryKey(Integer id);
    //根据工厂编号查询
    Factory selectByFactorynumber(@Param("factorynumber")String factoryNumber);
    //根据工厂名称查询
    Factory selectByFactoryname(@Param("factoryname")String factoryName);
    //条件分页查询
    public List<Factory> selectLimitByCondition(
            @Param("factoryname") String factoryName,
            @Param("factorynumber") String factoryNumber,
            @Param("province") String province,
            @Param("city") String city,
            @Param("district") String district,
            @Param("index") int index
    );
    //条件分页查询的总数量
    int selectCountByCondition(
            @Param("factoryname") String factoryName,
            @Param("factorynumber") String factoryNumber,
            @Param("province") String province,
            @Param("city") String city,
            @Param("district") String district
    );
    //条件查询
    public List<Factory> selectByCondition(
            @Param("factoryname") String factoryName,
            @Param("factorynumber") String factoryNumber,
            @Param("province") String province,
            @Param("city") String city,
            @Param("district") String district
    );
}