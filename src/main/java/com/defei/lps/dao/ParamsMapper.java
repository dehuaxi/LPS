package com.defei.lps.dao;

import com.defei.lps.entity.Params;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParamsMapper {
    int deleteByPrimaryKey(Integer id);

    int insertSelective(Params record);

    Params selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Params record);

    //根据参数名称查询
    Params selectByName(@Param("paramname")String paramName);
    //查询所有
    List<Params> selectAll();
}