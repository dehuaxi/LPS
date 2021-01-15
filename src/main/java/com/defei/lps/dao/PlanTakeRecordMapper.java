package com.defei.lps.dao;

import com.defei.lps.entity.PlanRecorde;
import com.defei.lps.entity.PlanTakeRecord;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanTakeRecordMapper {
    int deleteByPrimaryKey(Integer id);
    //删除一年之前数据
    void deleteOneYearAgo();

    int insertSelective(PlanTakeRecord record);

    PlanTakeRecord selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(PlanTakeRecord record);

    //条件分页查询
    public List<PlanTakeRecord> selectLimitByCondition(
            @Param("goodcode") String goodCode,
            @Param("goodname") String goodName,
            @Param("suppliername") String supplierName,
            @Param("suppliercode") String supplierCode,
            @Param("date") String date,
            @Param("cartype") String carType,
            @Param("overtime") String overTime,
            @Param("index") int index
    );
    //条件分页查询的总数量
    int selectCountByCondition(
            @Param("goodcode") String goodCode,
            @Param("goodname") String goodName,
            @Param("suppliername") String supplierName,
            @Param("suppliercode") String supplierCode,
            @Param("date") String date,
            @Param("cartype") String carType,
            @Param("overtime") String overTime
    );
}