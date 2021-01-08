package com.defei.lps.serviceImp;

import com.defei.lps.dao.*;
import com.defei.lps.entity.Role;
import com.defei.lps.entity.Route;
import com.defei.lps.entity.User;
import com.defei.lps.entity.UserRoute;
import com.defei.lps.result.Result;
import com.defei.lps.result.ResultUtil;
import com.defei.lps.service.UserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImp implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private FactoryMapper factoryMapper;
    @Autowired
    private RouteMapper routeMapper;
    @Autowired
    private UserRouteMapper userRouteMapper;

    /**
     * 添加用户
     * @param userName 用户名
     * @param password 密码
     * @param roleName 角色名称
     * @param routeNames 线路名称集合，格式：线路名称,线路名称...
     * @return
     */
    @Override
    @Transactional
    public synchronized Result addUser(String userName, String password, String roleName, String routeNames) {
        //先做参数的合法性校验
        if(!userName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5]{2,10}$")){
            return ResultUtil.error(1,"用户名只能是2-10位的数字、大小写字母、汉字");
        }else if(!password.matches("^[0-9A-Za-z]{6,20}$")){
            return ResultUtil.error(1,"密码只能是6-20位的数字、大小写字母");
        }
        //判断角色名称是否存在
        Role role=roleMapper.selectByRolename(roleName);
        if(role==null){
            return ResultUtil.error(1,"角色名称不存在，刷新页面后重新再试");
        }
        //判断传入的线路名称是否存在
        String[] list=routeNames.split(",");
        //定义线路对象集合，记录所有的对象
            List<Route> routeList=new ArrayList<>();
        for(String routeName:list){
            Route route=routeMapper.selectByRoutename(routeName);
            if(route==null){
                return ResultUtil.error(1,"线路名称"+routeName+"不存在，刷新页面后重新再试");
            }else {
                routeList.add(route);
            }
        }
        //验证完毕：先根据用户名查询，看是否已经存在，存在就返回提示
        User user=userMapper.selectByUserName(userName);
        if (user!=null) {
            return ResultUtil.error(1,"用户名已存在") ;
        }
        //保存用户基本信息
        User user1=new User();
        user1.setUsername(userName);
        user1.setPassword(password);
        user1.setRolename(roleName);
        userMapper.insertSelective(user1);
        int userId=userMapper.selectByUserName(userName).getId();
        //保存用户线路关系
        for(Route route:routeList){
            UserRoute userRoute=new UserRoute();
            userRoute.setUserid(userId);
            userRoute.setRouteid(route.getId());
            userRouteMapper.insertSelective(userRoute);
        }
        return ResultUtil.success();
    }

    /**
     * 根据id删除用户
     * @param id
     */
    @Override
    public Result deleteUserById(int id) {
        User user=userMapper.selectByPrimaryKey(id);
        if(user==null){
            return ResultUtil.error(1,"用户不存在，刷新页面后再试");
        }
        //删除用户对应的用户线路关系记录
        userRouteMapper.deleteByUserid(id);
        //删除用户信息
        userMapper.deleteByPrimaryKey(id);
        return ResultUtil.success();
    }

    /**
     * 修改账号
     * @param id
     * @param password 密码
     * @param roleName 角色名称
     * @param routeNames 线路名称集合，格式：线路名称,线路名称...
     * @return
     */
    @Override
    public synchronized Result updateUser(int id,String password, String roleName, String routeNames) {
        //先做参数的合法性校验
        if(!password.equals("")&&!password.matches("^[0-9A-Za-z]{6,20}$")){
            return ResultUtil.error(1,"密码只能是6-20位的数字、大小写字母");
        }
        //判断角色名称是否存在
        Role role=roleMapper.selectByRolename(roleName);
        if(role==null){
            return ResultUtil.error(1,"角色名称不存在，刷新页面后重新再试");
        }
        //判断传入的线路名称是否存在
        String[] list=routeNames.split(",");
        //定义线路对象集合，记录所有的对象
        List<Route> routeList=new ArrayList<>();
        for(String routeName:list){
            Route route=routeMapper.selectByRoutename(routeName);
            if(route==null){
                return ResultUtil.error(1,"线路名称"+routeName+"不存在，刷新页面后重新再试");
            }else {
                routeList.add(route);
            }
        }
        //根据id查询用户
        User user=userMapper.selectByPrimaryKey(id);
        if(user==null){
            return ResultUtil.error(1,"用户不存在，刷新页面后重试");
        }
        //修改用户路线关系记录
        userRouteMapper.deleteByUserid(user.getId());
        for(Route route:routeList){
            UserRoute userRoute=new UserRoute();
            userRoute.setUserid(user.getId());
            userRoute.setRouteid(route.getId());
            userRouteMapper.insertSelective(userRoute);
        }
        //修改用户信息
        if(!password.equals("")){
            user.setPassword(password);
        }
        user.setRolename(roleName);
        userMapper.updateByPrimaryKeySelective(user);
        return ResultUtil.success();
    }

    /**
     * 条件分页查询用户
     * @param userName 用户名称
     * @return
     */
    @Override
    public Result findAll(String userName,String roleName,int currentPage) {
        //查询起始下标
        int index=(currentPage-1)*30;
        //分页条件查询
        List<User> list=userMapper.selectLimitByCondition(userName,roleName ,index);
        if(!list.isEmpty()) {
            //总数量
            int totalCount=userMapper.selectCountByCondition(userName,roleName);
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
     * 用户自己重置密码
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return
     */
    @Override
    public synchronized Result updatePassword(String oldPassword,String newPassword) {
        //获取当前登录的用户名称
        Subject subject=SecurityUtils.getSubject();
        String currentUserName = (String) subject.getPrincipal();
        //根据用户名查询
        User user=userMapper.selectByUserName(currentUserName);
        if(user==null){
            return ResultUtil.error(1,"修改失败，刷新页面后重试");
        }
        //旧密码与数据库的密码进行对比
        if(oldPassword.equals(user.getPassword())){
            //旧密码一致，才修改新密码
            user.setPassword(newPassword);
            userMapper.updateByPrimaryKeySelective(user);
            return ResultUtil.success();
        }else {
            return ResultUtil.error(1,"旧密码不正确，请重试");
        }
    }

    /**
     * 根据用户id获取用户信息和用户的线路信息
     * @param id
     * @return
     */
    @Override
    public Result userById(int id) {
        User user=userMapper.selectByPrimaryKey(id);
        if(user==null){
            return ResultUtil.error(1,"用户不存在，刷新页面后重试");
        }
        //获取用户线路
        List<Route> routeList=routeMapper.selectByUserid(id);
        Map<String,Object> map=new HashMap<>();
        map.put("user",user);
        if(routeList.isEmpty()){
            map.put("route",null);
        }else {
            map.put("route",routeList);
        }
        return ResultUtil.success(map);
    }

}
