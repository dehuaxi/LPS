package com.defei.lps.dao;

import com.defei.lps.entity.TransportBillCache;
import com.defei.lps.entity.TransportBillRecord;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransportBillRecordMapper {
    int deleteByPrimaryKey(Integer id);

    int insertSelective(TransportBillRecord record);
    void insertBatch(@Param("list")List<TransportBillRecord> list);

    TransportBillRecord selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(TransportBillRecord record);
    //条件分页查询
    public List<TransportBillRecord> selectLimitByCondition(
            @Param("goodcode") String goodCode,
            @Param("goodname") String goodName,
            @Param("suppliercode") String supplierCode,
            @Param("suppliername") String supplierName,
            @Param("billnumber") String billNumber,
            @Param("geelybillnumber") String geelyBillNumber,
            @Param("dateStart") String dateStart,
            @Param("dateEnd") String dateEnd,
            @Param("carnumber") String carNumber,
            @Param("cartypename") String carTypeName,
            @Param("carriername") String carrierName,
            @Param("index") int index
    );
    //条件分页查询的总数量
    int selectCountByCondition(
            @Param("goodcode") String goodCode,
            @Param("goodname") String goodName,
            @Param("suppliercode") String supplierCode,
            @Param("suppliername") String supplierName,
            @Param("billnumber") String billNumber,
            @Param("geelybillnumber") String geelyBillNumber,
            @Param("dateStart") String dateStart,
            @Param("dateEnd") String dateEnd,
            @Param("carnumber") String carNumber,
            @Param("cartypename") String carTypeName,
            @Param("carriername") String carrierName
    );
}