package com.defei.lps.dao;

import com.defei.lps.entity.GeelyBillCache;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeelyBillCacheMapper {
    int deleteByPrimaryKey(Integer id);

    int insertSelective(GeelyBillCache record);
    //批量添加
    void insertBatch(@Param("list")List<GeelyBillCache> list);

    GeelyBillCache selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(GeelyBillCache record);

    //根据物料id查询未绑定计划的PD单记录
    List<GeelyBillCache> selectUnbindByGoodid(@Param("goodid")int goodId);
    //根据物料id、单号查询
    GeelyBillCache selectByGoodidAndBillnumber(@Param("goodid")int goodId,
                                               @Param("billnumber")String billNumber);
    //条件分页查询
    public List<GeelyBillCache> selectLimitByCondition(
            @Param("goodcode") String goodCode,
            @Param("goodname") String goodName,
            @Param("suppliercode") String supplierCode,
            @Param("suppliername") String supplierName,
            @Param("billnumber") String billNumber,
            @Param("urgent") String urgent,
            @Param("routeid") int routeId,
            @Param("factoryid") int factoryId,
            @Param("uploaddate") String uploadDate,
            @Param("index") int index
    );
    //条件分页查询的总数量
    int selectCountByCondition(
            @Param("goodcode") String goodCode,
            @Param("goodname") String goodName,
            @Param("suppliercode") String supplierCode,
            @Param("suppliername") String supplierName,
            @Param("billnumber") String billNumber,
            @Param("urgent") String urgent,
            @Param("routeid") int routeId,
            @Param("factoryid") int factoryId,
            @Param("uploaddate") String uploadDate
    );
    //根据PD单号查询
    List<GeelyBillCache> selectByBillnumber(@Param("billnumber") String billNumber);
}