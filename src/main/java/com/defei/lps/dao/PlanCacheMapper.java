package com.defei.lps.dao;

import com.defei.lps.entity.PlanCache;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanCacheMapper {
    //根据id删除
    int deleteByPrimaryKey(Integer id);
    //根据物料id、状态删除
    void deleteByGoodidAndState(@Param("goodid") int goodId,
                                @Param("state") String state);
    //根据物料id删除某个日期之后的所有计划
    void deleteByGoodidAndStartdate(@Param("goodid") int goodId,
                                    @Param("date") String date);
    //根据物料id、日期删除计划
    void deleteByGoodidAndDate(@Param("goodid") int goodId,
                               @Param("date") String date);

    //添加
    int insertSelective(PlanCache record);
    //批量添加
    void insertBatch(@Param("list")List<PlanCache> list);

    //修改
    int updateByPrimaryKeySelective(PlanCache record);

    //根据id查询
    PlanCache selectByPrimaryKey(Integer id);
    //根据物料id和状态查询，并以日期升序排序
    List<PlanCache> selectByGoodidAndState(@Param("goodid") int goodId,
                                           @Param("state") String state);
    //根据物料id和计划日期查询
    PlanCache selectByGoodidAndDate(@Param("goodid") int goodId,
                                    @Param("date") String date);
    //根据物料id查询,日期升序
    List<PlanCache> selectByGoodid(@Param("goodid") int goodId);
    //根据物料id、状态查询非传入状态日期最大的记录
    PlanCache selectLatelyByGoodidAndExcludeState(@Param("goodid") int goodId,
                                           @Param("state") String state);
    //根据供应商编号、状态查询取货数量不等于计划数量的未取货记录
    public List<PlanCache> selectUntakeBySuppliercodeAndState(
            @Param("suppliercode") String supplierCode,
            @Param("state") String state);
    //条件分页查询
    public List<PlanCache> selectLimitByCondition(
            @Param("goodcode") String goodCode,
            @Param("goodname") String goodName,
            @Param("suppliercode") String supplierCode,
            @Param("suppliername") String supplierName,
            @Param("date") String date,
            @Param("state") String state,
            @Param("type") String type,
            @Param("routeid") int routeId,
            @Param("factoryid") int factoryId,
            @Param("index") int index
    );
    //条件分页查询的总数量
    int selectCountByCondition(
            @Param("goodcode") String goodCode,
            @Param("goodname") String goodName,
            @Param("suppliercode") String supplierCode,
            @Param("suppliername") String supplierName,
            @Param("date") String date,
            @Param("state") String state,
            @Param("type") String type,
            @Param("routeid") int routeId,
            @Param("factoryid") int factoryId
    );
    //条件查询
    public List<PlanCache> selectByCondition(
            @Param("goodcode") String goodCode,
            @Param("goodname") String goodName,
            @Param("suppliercode") String supplierCode,
            @Param("suppliername") String supplierName,
            @Param("date") String date,
            @Param("state") String state,
            @Param("type") String type,
            @Param("routeid") int routeId,
            @Param("factoryid") int factoryId
    );
    //根据物料id集合、状态查询最早日期的记录
    PlanCache selectEarlyByGoodidsAndState(@Param("goodList") String goodidList,
                                                  @Param("state") String state);
    //根据物料id、状态、发货日期查询小于该日期的记录
    List<PlanCache> selectLessDateByGoodidAndState(@Param("goodid") int goodId,
                                                   @Param("state") String state,
                                                   @Param("date") String date);
    //根据物料id、状态、日期查询小于该日期的非传入状态的记录,取货日期升序
    List<PlanCache> selectLessDateByGoodidAndExcludeState(@Param("goodid") int goodId,
                                                   @Param("state") String state,
                                                   @Param("date") String date);
    //根据物料id、状态、起始日期(包含)，查询大于起始取货日期的所有非传入状态得记录,取货日期升序
    List<PlanCache> selectGreaterReceiveateByGoodidAndExcludeState(@Param("goodid") int goodId,
                                                          @Param("state") String state,
                                                          @Param("startdate") String startDate);
    //根据物料id、到货日期查询
    List<PlanCache> selectByGoodidAndReceivedate(
            @Param("goodid") int goodId,
            @Param("receivedate") String receiveDate
    );
    //根据物料id、到货日期、状态查询非传入状态的记录
    List<PlanCache> selectByGoodidAndReceivedateAndExcludeState(
            @Param("goodid") int goodId,
            @Param("state") String state,
            @Param("receivedate") String receiveDate
    );
    //根据物料id、发货日期、到货日期查询
    PlanCache selectByGoodidAndDateAndReceivedate(
            @Param("goodid") int goodId,
            @Param("date") String date,
            @Param("receivedate") String receiveDate
    );
}
