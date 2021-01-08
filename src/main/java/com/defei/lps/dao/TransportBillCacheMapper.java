package com.defei.lps.dao;

import com.defei.lps.entity.PlanCache;
import com.defei.lps.entity.TransportBillCache;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransportBillCacheMapper {
    int deleteByPrimaryKey(Integer id);
    //批量删除
    void deleteBatch(@Param("list")List<TransportBillCache> list);

    int insertSelective(TransportBillCache record);
    void insertBatch(@Param("list")List<TransportBillCache> list);

    TransportBillCache selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(TransportBillCache record);

    //条件分页查询
    public List<TransportBillCache> selectLimitByCondition(
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
    //以运输单分组，条件分页查询
    public List<TransportBillCache> selectBillLimitByCondition(
            @Param("billnumber") String billNumber,
            @Param("geelybillnumber") String geelyBillNumber,
            @Param("dateStart") String dateStart,
            @Param("dateEnd") String dateEnd,
            @Param("carnumber") String carNumber,
            @Param("cartypename") String carTypeName,
            @Param("carriername") String carrierName,
            @Param("index") int index
    );
    //以运输单分组，条件分页查询的总数量
    int selectBillCountByCondition(
            @Param("billnumber") String billNumber,
            @Param("geelybillnumber") String geelyBillNumber,
            @Param("dateStart") String dateStart,
            @Param("dateEnd") String dateEnd,
            @Param("carnumber") String carNumber,
            @Param("cartypename") String carTypeName,
            @Param("carriername") String carrierName
    );
    //根据运输单查询
    List<TransportBillCache> selectByBillnumber(@Param("billnumber") String billNumber);
}