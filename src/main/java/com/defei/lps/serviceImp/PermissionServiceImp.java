package com.defei.lps.serviceImp;

import com.defei.lps.dao.PermissionMapper;
import com.defei.lps.dao.RoleMapper;
import com.defei.lps.dao.RolePermissionMapper;
import com.defei.lps.dao.UserMapper;
import com.defei.lps.entity.Permission;
import com.defei.lps.entity.Role;
import com.defei.lps.entity.RolePermission;
import com.defei.lps.entity.User;
import com.defei.lps.result.Result;
import com.defei.lps.result.ResultUtil;
import com.defei.lps.service.PermissionService;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PermissionServiceImp implements PermissionService {
    @Autowired
    private PermissionMapper permissionMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    /**
     * 添加权限，同时给系统管理员systemManager添加该权限
     * @param url
     * @param permissionName
     * @param pid
     * @return
     */
    @Override
    public synchronized Result addPermission(String url, String permissionName, int pid) {
        //校验参数
        if(!permissionName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_-]{1,20}$")){
            return ResultUtil.error(1,"权限名称只能是1-20位的数字、大小写字母、汉字、特殊字符(@#_-)");
        }if(!url.matches("^[0-9A-Za-z]{1,50}$")){
            return ResultUtil.error(1,"权限url只能是1-50位的数字、大小写字母");
        }
        if(pid!=0){
            Permission permission=permissionMapper.selectByPrimaryKey(pid);
            if(permission==null){
                return ResultUtil.error(1,"父权限不存在，无法添加权限");
            }
        }
        //验证权限是否存在
        Permission permission=permissionMapper.selectByPermissionname(permissionName);
        if(permission!=null){
            return ResultUtil.error(1,"权限已存在");
        }
        //验证url+权限名称的组合是否存在
        Permission permission2=permissionMapper.selectByUrlAndPermissionname(url,permissionName);
        if(permission2!=null){
            return ResultUtil.error(1,"URL+权限名称组合已存在");
        }
        //添加权限信息
        Permission permission1=new Permission();
        permission1.setPermissionname(permissionName);
        permission1.setUrl(url);
        permission1.setPid(pid);
        permissionMapper.insertSelective(permission1);
        //添加权限给系统管理员角色
        Role role=roleMapper.selectByRolename("systemManager");
        if(role==null){
            return ResultUtil.error(1,"系统管理员角色:systemManager不存在，请添加后重试");
        }
        //查询刚刚添加的权限
        Permission permission3=permissionMapper.selectByPermissionname(permissionName);
        //添加权限角色关系
        RolePermission rolePermission=new RolePermission();
        rolePermission.setPermissionid(permission3.getId());
        rolePermission.setRoleid(role.getId());
        rolePermissionMapper.insertSelective(rolePermission);
        return ResultUtil.success();
    }

    /**
     * 删除权限
     * 根据权限名称删除，删除当前权限及对应的角色权限表记录，
     * 删除当前权限的所有层级的子权限及对应的角色权限表记录
     * @param permissionName
     * @return
     */
    @Override
    public synchronized Result deletePermission(String permissionName) {
        Permission permission=permissionMapper.selectByPermissionname(permissionName);
        if(permission==null){
            return ResultUtil.error(1,"权限不存在，刷新页面后重试");
        }
        //递归获取当前权限的所有层级的子权限
        List<Permission> childPermissions=permissionMapper.selectChildById(permission.getId());
        //删除所有子权限的权限信息、角色权限关系表信息
        for(Permission permission1:childPermissions){
            permissionMapper.deleteByPrimaryKey(permission1.getId());
            rolePermissionMapper.deleteByPermissionid(permission1.getId());
        }
        //根据权限id删除角色权限关系表内容
        rolePermissionMapper.deleteByPermissionid(permission.getId());
        //删除权限信息
        permissionMapper.deleteByPrimaryKey(permission.getId());
        return ResultUtil.success();
    }

    /**
     * 修改权限
     * @param url
     * @param permissionName
     * @return
     */
    @Override
    public synchronized Result updatePermission(int id,String url, String permissionName) {
        //校验参数
        if(!permissionName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_-]{1,20}$")){
            return ResultUtil.error(1,"权限名称只能是1-20位的数字、大小写字母、汉字、特殊字符(@#_-)");
        }if(!url.matches("^[0-9A-Za-z]{1,50}$")){
            return ResultUtil.error(1,"权限url只能是1-50位的数字、大小写字母");
        }
        //验证权限是否存在
        Permission permission=permissionMapper.selectByPrimaryKey(id);
        if(permission==null){
            return ResultUtil.error(1,"权限不存在，刷新页面后重试");
        }
        //看修改后权限名称是否重复
        if(!permission.getPermissionname().equals(permissionName)){
            Permission permission1=permissionMapper.selectByPermissionname(permissionName);
            if(permission1!=null){
                return ResultUtil.error(1,"修改后的权限名称已存在");
            }
        }
        //修改权限信息
        permission.setUrl(url);
        permission.setPermissionname(permissionName);
        permissionMapper.updateByPrimaryKeySelective(permission);
        return ResultUtil.success();
    }

    /**
     * 查询当前登录用户的所有权限,以zTree需要的数据格式返回
     * zTree的数据格式：[{id:1,pid:0,name:"aaaa"},{id:2,pid:0,name:"bbbb"},...]
     * @return
     */
    @Override
    public Result permissionZTree() {
        String userName= (String) SecurityUtils.getSubject().getPrincipal();
        User user=userMapper.selectByUserName(userName);
        if(user==null){
            return ResultUtil.error(1,"请刷新页面后重试");
        }
        //根据用户所属的角色名称查询角色id
        Role role=roleMapper.selectByRolename(user.getRolename());
        if(role==null){
            return ResultUtil.error(1,"当前用户分配的角色名称不存在，请重新分配角色后再试");
        }
        //根据角色id查询权限
        List<Permission> permissionList=permissionMapper.selectByRoleid(role.getId());
        if(permissionList.isEmpty()){
            return ResultUtil.error(1,"当前用户分配的角色无任何权限");
        }
        List<Map<String,Object>> list=new ArrayList<>();
        for(Permission permission:permissionList){
            Map<String,Object> map=new HashMap<>();
            map.put("id",permission.getId());
            map.put("pid",permission.getPid());
            map.put("name",permission.getPermissionname());
            list.add(map);
        }
        return ResultUtil.success(list);
    }

    /**
     * 根据角色的id查询角色对应的权限
     * 主要在角色管理页面使用
     * 以zTree需要的数据格式返回
     * zTree的数据格式：[{id:1,pid:0,name:"aaaa"},{id:2,pid:0,name:"bbbb"},...]
     * @param roleId
     * @return
     */
    @Override
    public Result permissionByRoleid(int roleId) {
        //根据角色id查询权限
        List<Permission> permissionList=permissionMapper.selectByRoleid(roleId);
        if(permissionList.isEmpty()){
            return ResultUtil.error(1,"角色无任何权限");
        }
        List<Map<String,Object>> list=new ArrayList<>();
        for(Permission permission:permissionList){
            Map<String,Object> map=new HashMap<>();
            map.put("id",permission.getId());
            map.put("pid",permission.getPid());
            map.put("name",permission.getPermissionname());
            list.add(map);
        }
        return ResultUtil.success(list);
    }

    /**
     * 根据权限名称查询
     * 权限管理页面使用，当点击zTree节点进行修改时，需要根据权限名称获取信息
     * @param permissionName
     * @return
     */
    @Override
    public Result permissionByName(String permissionName) {
        Permission permission=permissionMapper.selectByPermissionname(permissionName);
        if(permission==null){
            return ResultUtil.error(1,"权限不存在");
        }
        return ResultUtil.success(permission);
    }

}
