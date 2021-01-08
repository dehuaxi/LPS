package com.defei.lps.dao;

import com.defei.lps.entity.Good;
import com.defei.lps.entity.Route;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoodMapper {
    //删除
    int deleteByPrimaryKey(Integer id);

    //添加
    int insertSelective(Good record);
    //批量添加
    void insertBatch(@Param("goodList")List<Good> list);

    //修改
    int updateByPrimaryKeySelective(Good record);

    //根据id查询
    Good selectByPrimaryKey(Integer id);
    //根据工厂id和物料名称模糊查询
    List<Good> selectLikeNameAndFactoryid(@Param("goodname") String goodName,
                                          @Param("factoryid") int factoryId);
    //条件分页查询
    public List<Good> selectLimitByCondition(
            @Param("goodcode") String goodCode,
            @Param("goodname") String goodName,
            @Param("suppliername") String supplierName,
            @Param("suppliercode") String supplierCode,
            @Param("boxtype") String boxType,
            @Param("factoryid") int factoryId,
            @Param("routeid") int routeId,
            @Param("index") int index
    );
    //条件分页查询的总数量
    int selectCountByCondition(
            @Param("goodcode") String goodCode,
            @Param("goodname") String goodName,
            @Param("suppliername") String supplierName,
            @Param("suppliercode") String supplierCode,
            @Param("boxtype") String boxType,
            @Param("factoryid") int factoryId,
            @Param("routeid") int routeId
    );
    //条件查询
    public List<Good> selectByCondition(
            @Param("goodcode") String goodCode,
            @Param("goodname") String goodName,
            @Param("suppliername") String supplierName,
            @Param("suppliercode") String supplierCode,
            @Param("boxtype") String boxType,
            @Param("factoryid") int factoryId,
            @Param("routeid") int routeId
    );
    //根据物料编号、供应商id
    Good selectByGoodcodeAndSupplierid(@Param("goodcode") String goodCode,
                                        @Param("supplierid") int supplierId);
    //根据供应商id查询
    List<Good> selectBySupplierid(@Param("supplierid") int supplierId);
    //根据线路id集合查询物料
    List<Good> selectByRouteids(@Param("routeList") List<Route> routeList);
}