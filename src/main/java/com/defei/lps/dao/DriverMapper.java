package com.defei.lps.dao;

import com.defei.lps.entity.Carrier;
import com.defei.lps.entity.Driver;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Driver record);

    int insertSelective(Driver record);

    Driver selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Driver record);

    int updateByPrimaryKey(Driver record);

    //根据手机号查询
    Driver selectByPhone(@Param("phone")String phone);
    //条件分页查询
    public List<Driver> selectLimitByCondition(
            @Param("name") String driverName,
            @Param("phone") String phone,
            @Param("index") int index
    );
    //条件分页查询的总数量
    int selectCountByCondition(
            @Param("name") String driverName,
            @Param("phone") String phone
    );
    //查询所有
    List<Driver> selectAll();
}