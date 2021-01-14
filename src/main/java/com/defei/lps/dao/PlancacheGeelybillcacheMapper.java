package com.defei.lps.dao;

import com.defei.lps.entity.PlancacheGeelybillcache;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlancacheGeelybillcacheMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(PlancacheGeelybillcache record);

    int insertSelective(PlancacheGeelybillcache record);

    PlancacheGeelybillcache selectByPrimaryKey(Integer id);
    //根据在途吉利单id查询
    List<PlancacheGeelybillcache> selectByGeelybillcacheid(@Param("geelybillcacheid")int geelybillcacheid);
    //根据计划id、传入的吉利单据id查询非传入的吉利单据id的记录
    List<PlancacheGeelybillcache> selectExcludeGeelybillcacheidByPlancacheid(
            @Param("plancacheid")int plancacheId,
            @Param("geelybillcacheid")int geelybillcacheId);
    //根据缺件计划id、在途吉利单据记录id查询
    PlancacheGeelybillcache selectByPlancacheidAndGeelybillcacheid(
            @Param("plancacheid")int plancacheId,
            @Param("geelybillcacheid")int geelybillcacheId
    );

    int updateByPrimaryKeySelective(PlancacheGeelybillcache record);

    int updateByPrimaryKey(PlancacheGeelybillcache record);
}