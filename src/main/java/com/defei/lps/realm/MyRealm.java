package com.defei.lps.realm;

import com.defei.lps.dao.PermissionMapper;
import com.defei.lps.dao.UserMapper;
import com.defei.lps.entity.User;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

public class MyRealm extends AuthorizingRealm {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PermissionMapper permissionMapper;
    //@Autowired
    //private SessionDAO sessionDAO;

    //给用户授权
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection pc) {
        //获取用户对象
        String userName = (String) SecurityUtils.getSubject().getPrincipal();
        User user = userMapper.selectByUserName(userName);
        //定义一个权限对象(框架自有的)，用于记录用户的所有权限信息
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        //当用户对象存在时，把用户的所有权限获取到，并放入上面定义的权限对象中
        if (user != null) {
            //获取角色
            String role = user.getRolename();
            //1.把角色放入权限对象中
            info.addRole(role);
            //通过角色id获取权限的url集合
            Set<String> usrls = permissionMapper.selectUrlByRolename(role);
            //把权限放入权限对象中
            info.setStringPermissions(usrls);
            //返回权限对象
            return info;
        }
        return null;
    }

    //认证用户。主要用来登陆
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token)
            throws AuthenticationException {
        //获取shiro内置对象UsernamePasswordToken
        UsernamePasswordToken t = (UsernamePasswordToken) token;
        //通过用户名查询用户对象
        String userName=t.getUsername();
        User user = userMapper.selectByUserName(userName);
        if (user == null) {
            return null;
        }
        //获取所有在线用户
        /*Collection<Session> sessions = sessionDAO.getActiveSessions();
        //踢出相同账户
        for (Session session : sessions) {
            //如果是同浏览器就不踢出
            if (SecurityUtils.getSubject().getSession().getId().equals(session.getId())) {
                break;
            } else {
                //不是同一个浏览器，判断是否重复登陆
                if (t.getUsername().equals(String.valueOf(session.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY)))) {
                    session.setTimeout(0);// 设置session立即失效，即将其踢出系统
                    break;
                }
            }
        }*/
        //创建shiro内置的SimpleAuthenticationInfo对象，来认证用户。参数1为得到的用户名，参数2位数据库查询的密码，参数三是本类
        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(user.getUsername(), user.getPassword(), getName());
        //把验证结果返回
        return info;
    }

}
