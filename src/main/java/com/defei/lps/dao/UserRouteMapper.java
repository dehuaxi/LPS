package com.defei.lps.dao;

import com.defei.lps.entity.UserRoute;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRouteMapper {
    //删除
    int deleteByPrimaryKey(Integer id);
    //根据路线id删除
    void deleteByRouteid(@Param("routeid")int routeId);
    //根据用户id删除
    void deleteByUserid(@Param("userid")int userId);

    //添加
    int insertSelective(UserRoute record);

    //修改
    int updateByPrimaryKeySelective(UserRoute record);

    //根据id查询
    UserRoute selectByPrimaryKey(Integer id);
}