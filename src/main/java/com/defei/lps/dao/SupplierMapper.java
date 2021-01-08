package com.defei.lps.dao;

import com.defei.lps.entity.Supplier;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierMapper {
    //根据id删除
    int deleteByPrimaryKey(Integer id);

    //添加
    int insertSelective(Supplier record);

    //修改
    int updateByPrimaryKeySelective(Supplier record);

    //根据id查询
    Supplier selectByPrimaryKey(Integer id);
    //条件分页查询
    public List<Supplier> selectLimitByCondition(
            @Param("suppliercode") String supplierCode,
            @Param("suppliername") String supplierName,
            @Param("province") String province,
            @Param("city") String city,
            @Param("district") String district,
            @Param("factoryid") int factoryId,
            @Param("areaid") int areaId,
            @Param("index") int index
    );
    //条件分页查询的总数量
    int selectCountByCondition(
            @Param("suppliercode") String supplierCode,
            @Param("suppliername") String supplierName,
            @Param("province") String province,
            @Param("city") String city,
            @Param("district") String district,
            @Param("factoryid") int factoryId,
            @Param("areaid") int areaId
    );
    //条件查询
    public List<Supplier> selectByCondition(
            @Param("suppliercode") String supplierCode,
            @Param("suppliername") String supplierName,
            @Param("province") String province,
            @Param("city") String city,
            @Param("district") String district,
            @Param("factoryid") int factoryId,
            @Param("areaid") int areaId
    );
    //根据供应商编号、工厂id查询
    Supplier selectBySuppliercodeAndFactoryid(@Param("suppliercode") String supplierCode,
                                              @Param("factoryid") int factoryId);
    //根据供应商编号查询不重复的供应商，不论哪个工厂的
    List<Supplier> selectLikeSuppliername(@Param("suppliername") String supplierName);
}