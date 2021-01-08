package com.defei.lps.serviceImp;

import com.defei.lps.dao.*;
import com.defei.lps.entity.Permission;
import com.defei.lps.entity.Role;
import com.defei.lps.entity.RolePermission;
import com.defei.lps.entity.User;
import com.defei.lps.result.Result;
import com.defei.lps.result.ResultUtil;
import com.defei.lps.service.RoleService;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("roleService")
public class RoleServiceImp implements RoleService {
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private RolePermissionMapper rolePermissionMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private FactoryMapper departmentMapper;
    @Autowired
    private PermissionMapper permissionMapper;

    /**
     * 添加角色
     * @param roleName 角色名称
     * @param describes 描述
     * @param permissionName 权限名称集合，格式：权限名称,权限名称,权限名称,...
     * @return
     */
    @Override
    public synchronized Result addRole(String roleName, String describes, String permissionName) {
        //校验参数
        if(!roleName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_-]{1,20}$")){
            return ResultUtil.error(1,"角色名称只能是1-20位的数字、大小写字母、汉字、特殊字符(@#_-)");
        }else if(describes.length()>50){
            return ResultUtil.error(1,"描述总字数不可超过50");
        }else if(permissionName==null||permissionName.equals("")){
            return ResultUtil.error(1,"必须选择权限");
        }
        //验证是否存在相同角色名称的对象
        if (roleMapper.selectByRolename(roleName) != null) {
            return ResultUtil.error(1,"角色名称已存在，重新输入");
        }
        //验证选择的权限是否存在
        //根据传入的权限集合字符串获取权限名称集合
        String[] permissionNames = permissionName.split(",");
        List<Permission> permissionList=new ArrayList<>();
        for (String permission:permissionNames) {
            Permission permission1=permissionMapper.selectByPermissionname(permission);
            if(permission1==null){
                return ResultUtil.error(1,"权限名称："+permission+"不存在");
            }
            permissionList.add(permission1);
        }
        //添加角色基本信息
        Role role=new Role();
        role.setRolename(roleName);
        role.setDescribes(describes);
        roleMapper.insertSelective(role);
        int roleId=roleMapper.selectByRolename(roleName).getId();
        //添加角色权限关系表记录
        for(Permission permission:permissionList){
            RolePermission rolePermission=new RolePermission();
            rolePermission.setRoleid(roleId);
            rolePermission.setPermissionid(permission.getId());
        }
        return ResultUtil.success();
    }

    /**
     * 删除角色
     * @param id
     * @return
     */
    @Override
    public synchronized Result deleteRoleById(int id) {
        Role role=roleMapper.selectByPrimaryKey(id);
        if(role==null){
            return ResultUtil.error(1,"角色不存在，刷新页面后重试");
        }
        //先要根据角色名称查询用户表中是否有用户
        List<User> list = userMapper.selectByRoleName(role.getRolename());
        if (list.isEmpty()) {
            //1.删除角色表中的基本信息
            roleMapper.deleteByPrimaryKey(id);
            //2.根据角色id删除角色权限表中的内容
            rolePermissionMapper.deleteByRoleid(id);
            return ResultUtil.success();
        } else {
            return ResultUtil.error(1,"有" + list.size() + "个用户正在使用本角色。请先把这些用户的角色变更，再删除本角色");
        }
    }

    /**
     * 条件分页查询角色。
     * 只能查询除自己所属角色以外的不包含系统管理员角色（systemManager）的角色
     * @param roleName
     * @param currentPage
     * @return
     */
    @Override
    public Result findAll(String roleName,int currentPage) {
        String userName= (String) SecurityUtils.getSubject().getPrincipal();
        User user=userMapper.selectByUserName(userName);
        if(user==null){
            return ResultUtil.error(1,"刷新页面后重试");
        }
        //查询起始下标
        int index=(currentPage-1)*30;
        //分页条件查询,除了系统管理员角色systemManager和传入的排除角色
        List<Role> list=roleMapper.selectLimitByCondition(roleName ,user.getRolename(),index);
        if(!list.isEmpty()) {
            //总数量
            int totalCount=roleMapper.selectCountByCondition(roleName,user.getRolename());
            int totalPage=0;
            if(totalCount%30==0) {
                totalPage=totalCount/30;
            }else {
                totalPage=totalCount/30+1;
            }
            //集合内容
            Map map=new HashMap();
            map.put("currentPage",currentPage);
            map.put("totalPage",totalPage);
            map.put("totalCount",totalCount);
            map.put("list",list);
            return ResultUtil.success(map);
        }
        return ResultUtil.success();
    }

    /**
     * 修改角色
     * @param id
     * @param roleName
     * @param describes
     * @param permissionName 权限名称集合，格式：权限名称,权限名称,权限名称,...
     * @return
     */
    @Override
    public synchronized Result updateRole(int id, String roleName, String describes, String permissionName) {
        //校验参数
        if(!roleName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_-]{1,20}$")){
            return ResultUtil.error(1,"角色名称只能是1-20位的数字、大小写字母、汉字、特殊字符(@#_-)");
        }else if(describes.length()>50){
            return ResultUtil.error(1,"描述总字数不可超过50");
        }else if(permissionName==null||permissionName.equals("")){
            return ResultUtil.error(1,"必须选择权限");
        }
        //验证角色是否存在
        Role role=roleMapper.selectByPrimaryKey(id);
        if(role==null){
            return ResultUtil.error(1,"角色不存在，刷新页面后重试");
        }
        //验证选择的权限是否存在
        //根据传入的权限集合字符串获取权限名称集合
        String[] permissionNames = permissionName.split(",");
        List<Permission> permissionList=new ArrayList<>();
        for (String permission:permissionNames) {
            Permission permission1=permissionMapper.selectByPermissionname(permission);
            if(permission1==null){
                return ResultUtil.error(1,"权限名称："+permission+"不存在");
            }
            permissionList.add(permission1);
        }
        //1.看角色名称是否修改，如果修改就要修改相应的用户记录
        if(!role.getRolename().equals(roleName)){
            //看修改后的角色名称是否存在
            Role role1=roleMapper.selectByRolename(roleName);
            if(role1!=null){
                return ResultUtil.error(1,"修改后的角色名称已存在");
            }
            //如果不存在，那么修改用户表中的角色名称
            userMapper.updateRolenameByRolename(roleName,role.getRolename());
        }
        //2.修改角色信息
        role.setRolename(roleName);
        role.setDescribes(describes);
        roleMapper.updateByPrimaryKeySelective(role);
        //3.修改角色对应的角色权限关系记录
        rolePermissionMapper.deleteByRoleid(role.getId());
        for(Permission permission:permissionList){
            RolePermission rolePermission=new RolePermission();
            rolePermission.setRoleid(role.getId());
            rolePermission.setPermissionid(permission.getId());
            rolePermissionMapper.insertSelective(rolePermission);
        }
        return ResultUtil.success();
    }

    /**
     * 查询所有角色，除开系统管理员角色systemManager和自身的角色
     * @return
     */
    @Override
    public Result currentRole() {
        List<Role> roleList=roleMapper.selectAll();
        if(roleList.isEmpty()){
            return ResultUtil.error(1,"系统未创建角色，请添加角色后再试");
        }
        return ResultUtil.success(roleList);
    }


}
