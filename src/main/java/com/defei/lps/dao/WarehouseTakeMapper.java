package com.defei.lps.dao;

import com.defei.lps.entity.WarehouseOut;
import com.defei.lps.entity.WarehouseTake;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarehouseTakeMapper {
    int deleteByPrimaryKey(Integer id);
    //批量删除
    void deteteBatch(@Param("list")List<WarehouseTake> list);
    //根据编号删除
    void deleteByBillnumber(@Param("billnumber")String billNumber);
    //根据方案编号、物料id、批次删除
    void deleteByBillnumberAndGoodidAndBatch(
            @Param("billnumber")String billNumber,
            @Param("goodid") int goodId,
            @Param("batch")String batch
    );

    int insertSelective(WarehouseTake record);

    WarehouseTake selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(WarehouseTake record);

    //条件分页查询
    public List<WarehouseTake> selectBillLimitByCondition(
            @Param("billnumber") String billNumber,
            @Param("cartype") String carTypeName,
            @Param("startname") String startName,
            @Param("endname") String endName,
            @Param("username") String userName,
            @Param("index") int index
    );
    //条件分页查询的总数量
    int selectBillCountByCondition(
            @Param("billnumber") String billNumber,
            @Param("cartype") String carTypeName,
            @Param("startname") String startName,
            @Param("endname") String endName,
            @Param("username") String userName
    );
    //根据装载方案编号查询
    List<WarehouseTake> selectByBillnumber(@Param("billnumber") String billNumber);
    //根据装载方案编号查询,并以物料id分组，把数量求和
    List<WarehouseTake> selectGroupGoodidByBillnumber(@Param("billnumber")String billNumber);
    //根据方案编号、物料id、批次查询
    WarehouseTake selectByBillnumberAndGoodidAndBatch(
            @Param("billnumber")String billNumber,
            @Param("goodid") int goodId,
            @Param("batch")String batch
    );
}