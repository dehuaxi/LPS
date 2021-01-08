package com.defei.lps.dao;

import com.defei.lps.entity.PlanHandleRecord;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanHandleRecordMapper {
    int deleteByPrimaryKey(Integer id);
    //删除一年之前数据
    void deleteOneYearAgo();

    int insertSelective(PlanHandleRecord record);

    PlanHandleRecord selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(PlanHandleRecord record);

    //条件分页查询
    public List<PlanHandleRecord> selectLimitByCondition(
            @Param("goodcode") String goodCode,
            @Param("goodname") String goodName,
            @Param("suppliercode") String supplierCode,
            @Param("suppliername") String supplierName,
            @Param("date") String date,
            @Param("index") int index
    );
    //条件分页查询的总数量
    int selectCountByCondition(
            @Param("goodcode") String goodCode,
            @Param("goodname") String goodName,
            @Param("suppliercode") String supplierCode,
            @Param("suppliername") String supplierName,
            @Param("date") String date
    );
}