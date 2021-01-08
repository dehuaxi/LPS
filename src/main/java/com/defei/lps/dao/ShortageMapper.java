package com.defei.lps.dao;

import com.defei.lps.entity.Shortage;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShortageMapper {
    //根据id删除
    int deleteByPrimaryKey(Integer id);

    //添加
    int insertSelective(Shortage record);

    //修改
    int updateByPrimaryKeySelective(Shortage record);

    //根据id查询
    Shortage selectByPrimaryKey(Integer id);
    //根据物料id和日期查询
    Shortage selectByGoodidAndDate(@Param("goodid")int goodId,
                                   @Param("date")String date);
    //根据物料id、起始日期、结束日期查询
    List<Shortage> selectByGoodidAndDatestartAndDateend(
            @Param("goodid")int goodId,
            @Param("datestart")String dateStart,
            @Param("dateend")String dateEnd
    );
    //查询日期最大的缺件记录
    Shortage selectMaxDate();
    //查询从今天起到最大日期，并以日期分组，日期升序排序
    List<Shortage> selectGreatTodayDateList();
}