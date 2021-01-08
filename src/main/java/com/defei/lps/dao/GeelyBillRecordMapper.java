package com.defei.lps.dao;

import com.defei.lps.entity.GeelyBillRecord;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeelyBillRecordMapper {
    int deleteByPrimaryKey(Integer id);

    int insertSelective(GeelyBillRecord record);

    GeelyBillRecord selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(GeelyBillRecord record);

    //根据物料id、PD单号查询
    GeelyBillRecord selectByGoodidAndBillnumber(@Param("goodid")int goodId,
                                                @Param("billnumber")String billNumber);

    //条件分页查询
    public List<GeelyBillRecord> selectLimitByCondition(
            @Param("goodcode") String goodCode,
            @Param("goodname") String goodName,
            @Param("suppliercode") String supplierCode,
            @Param("suppliername") String supplierName,
            @Param("billnumber") String billNumber,
            @Param("batch") String planDate,
            @Param("needbind") String needBind,
            @Param("bindbillnumber") String bindBillNumber,
            @Param("uploaddate") String uploadDate,
            @Param("receivedatestart") String receiveDateStart,
            @Param("receivedateend") String receiveDateEnd,
            @Param("index") int index
    );
    //条件分页查询的总数量
    int selectCountByCondition(
            @Param("goodcode") String goodCode,
            @Param("goodname") String goodName,
            @Param("suppliercode") String supplierCode,
            @Param("suppliername") String supplierName,
            @Param("billnumber") String billNumber,
            @Param("batch") String planDate,
            @Param("needbind") String needBind,
            @Param("bindbillnumber") String bindBillNumber,
            @Param("uploaddate") String uploadDate,
            @Param("receivedatestart") String receiveDateStart,
            @Param("receivedateend") String receiveDateEnd
    );
    //条件分页查询
    public List<GeelyBillRecord> selectByCondition(
            @Param("goodcode") String goodCode,
            @Param("goodname") String goodName,
            @Param("suppliercode") String supplierCode,
            @Param("suppliername") String supplierName,
            @Param("billnumber") String billNumber,
            @Param("batch") String planDate,
            @Param("needbind") String needBind,
            @Param("bindbillnumber") String bindBillNumber,
            @Param("uploaddate") String uploadDate,
            @Param("receivedatestart") String receiveDateStart,
            @Param("receivedateend") String receiveDateEnd
    );
    //查询需要绑定的数量
    int selectCountNeedBind();
    //根据PD单号查询
    List<GeelyBillRecord> selectByBillnumber(@Param("billnumber") String billNumber);
}