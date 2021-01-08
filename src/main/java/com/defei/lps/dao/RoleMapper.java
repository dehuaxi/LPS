package com.defei.lps.dao;

import com.defei.lps.entity.Role;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleMapper {
    //根据id删除
    int deleteByPrimaryKey(Integer id);

    //添加
    int insertSelective(Role record);

    //修改
    int updateByPrimaryKeySelective(Role record);

    //通过id查询
    Role selectByPrimaryKey(Integer id);
    //根据角色名称查询
    Role selectByRolename(@Param("rolename")String roleName);
    //条件分页查询,除了系统管理员角色systemManager和传入的排除角色
    List<Role> selectLimitByCondition(
            @Param("rolename") String routeName,
            @Param("removerolename") String removeRoleName,
            @Param("index") int index
    );
    //条件分页查询的总数量
    int selectCountByCondition(
            @Param("rolename") String routeName,
            @Param("removerolename") String removeRoleName
    );
    //查询所有记录，除了系统管理员角色systemManager
    List<Role> selectAll();
}