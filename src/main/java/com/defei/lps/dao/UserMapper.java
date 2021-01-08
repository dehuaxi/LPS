package com.defei.lps.dao;

import com.defei.lps.entity.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository()
public interface UserMapper {
    //添加
    public int insertSelective(User user);

    //根据id删除
    public void deleteByPrimaryKey(@Param("id") int id);

    //修改
    public int updateByPrimaryKeySelective(User user);
    //根据角色名称修改角色名称
    public void updateRolenameByRolename(@Param("rolename")String roleName,
                                         @Param("oldrolename")String oldRoleName);

    //根据id查询
    public User selectByPrimaryKey(@Param("id") Integer id);
    //条件分页查询
    public List<User> selectLimitByCondition(
            @Param("username") String userName,
            @Param("rolename") String roleName,
            @Param("index") int index
    );
    //条件分页查询的总数量
    int selectCountByCondition(
            @Param("username") String userName,
            @Param("rolename") String roleName
    );
    //根据用户名查询,在myRealm中用到
    public User selectByUserName(@Param("username") String userName);
    //根据角色名称查询。角色的删除、修改时用到
    public List<User> selectByRoleName(
            @Param("rolename") String roleName);
}