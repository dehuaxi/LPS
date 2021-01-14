package com.defei.lps.dao;

import com.defei.lps.entity.PlanCache;
import com.defei.lps.entity.PlanTake;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanTakeMapper {
    int deleteByPrimaryKey(Integer id);
    void deleteByPlannumber(@Param("plannumber")String planNumber);

    int insertSelective(PlanTake record);
    //批量添加
    void insertBatch(@Param("list")List<PlanTake> list);

    PlanTake selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(PlanTake record);

    //根据计划编号查询
    List<PlanTake> selectByPlannumber(@Param("plannumber")String planNumber);
    //根据计划编号查询,并以物料id分组，把数量求和
    List<PlanTake> selectGroupByPlannumber(@Param("plannumber")String planNumber);
    //条件分页查询，并以计划编号分组
    List<PlanTake> selectLimitByCondition(@Param("plannumber")String planNumber,
                                          @Param("suppliercode")String supplierCode,
                                          @Param("suppliername")String supplierName,
                                          @Param("routeid")int routeId,
                                          @Param("date")String date,
                                          @Param("startname")String startName,
                                          @Param("endname")String endName,
                                          @Param("username")String userName,
                                          @Param("index")int index);
    //条件分页查询数量
    int selectCountByCondition(@Param("plannumber")String planNumber,
                               @Param("suppliercode")String supplierCode,
                               @Param("suppliername")String supplierName,
                               @Param("routeid")int routeId,
                               @Param("date")String date,
                               @Param("startname")String startName,
                               @Param("endname")String endName,
                               @Param("username")String userName);
    //条件查询
    List<PlanTake> selectByCondition(@Param("plannumber")String planNumber,
                                      @Param("suppliercode")String supplierCode,
                                      @Param("suppliername")String supplierName,
                                      @Param("routeid")int routeId,
                                      @Param("date")String date,
                                      @Param("startname")String startName,
                                      @Param("endname")String endName,
                                      @Param("username")String userName);
    //根据计划编号、物料id查询
    List<PlanTake> selectByPlannumberAndGoodid(@Param("plannumber")String planNumber,
                                               @Param("goodid")int goodId);
    //根据在途缺件计划id查询，并以取货计划编号分组
    List<PlanTake> selectGroupPlannumberByPlancacheid(@Param("plancacheid")int planCacheId);
    //根据缺件计划id查询
    List<PlanTake> selectByPlancacheid(@Param("plancacheid")int planCacheId);
}