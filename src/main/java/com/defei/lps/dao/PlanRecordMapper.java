package com.defei.lps.dao;

import com.defei.lps.entity.PlanRecorde;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanRecordMapper {
    //根据id删除
    int deleteByPrimaryKey(Integer id);
    //删除一年之前数据
    void deleteOneYearAgo();

    //添加
    int insertSelective(PlanRecorde record);

    //修改
    int updateByPrimaryKeySelective(PlanRecorde record);

    //根据id查询
    PlanRecorde selectByPrimaryKey(Integer id);
    //根据物料id查询,并以日期降序
    List<PlanRecorde> selectByGoodid(@Param("goodid") int goodId);
    //根据物料id和计划日期查询
    PlanRecorde selectByGoodidAndDate(@Param("goodid") int goodId,
                                    @Param("date") String date);
    //条件分页查询
    public List<PlanRecorde> selectLimitByCondition(
            @Param("goodcode") String goodCode,
            @Param("goodname") String goodName,
            @Param("suppliername") String supplierName,
            @Param("suppliercode") String supplierCode,
            @Param("date") String date,
            @Param("urgent") String urgent,
            @Param("routeid") int routeId,
            @Param("type") String type,
            @Param("createtime") String createTime,
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
            @Param("urgent") String urgent,
            @Param("routeid") int routeId,
            @Param("type") String type,
            @Param("createtime") String createTime,
            @Param("overtime") String overTime
    );
}