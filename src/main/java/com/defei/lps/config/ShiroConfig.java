package com.defei.lps.config;

import at.pollux.thymeleaf.shiro.dialect.ShiroDialect;
import com.defei.lps.realm.MyRealm;
import com.defei.lps.util.ShiroSessionFilter;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * shiro配置类
 */
@Configuration
public class ShiroConfig {

    //引入自定义的realm类
    @Bean
    public Realm realm() {
        return new MyRealm();
    }

    //配置shiro session 的一个管理器
    @Bean
    public DefaultWebSessionManager getDefaultWebSessionManager(){
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        // 设置session过期时间,单位毫秒。此处设置为8小时=60*60*8*1000
        sessionManager.setGlobalSessionTimeout(28800000);
        //不让JSESSIONID显示在URL中
        sessionManager.setSessionIdUrlRewritingEnabled(false);
        return sessionManager;
    }

    //引入securityManager 安全管理器，并把自定义的realm类放入管理器
    @Bean
    public SecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        //配置自定义的realm
        securityManager.setRealm(realm());
        //配置session管理器
        securityManager.setSessionManager(getDefaultWebSessionManager());
        return securityManager;
    }

    /**
     * 以下2个函数功能是启用shiro注解
     * @return
     */
    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator proxyCreator = new DefaultAdvisorAutoProxyCreator();
        proxyCreator.setProxyTargetClass(true);
        return proxyCreator;
    }
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }

    /*
    @Bean
    public static DefaultAdvisorAutoProxyCreator getDefaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator creator = new DefaultAdvisorAutoProxyCreator();
        /**
         * setUsePrefix(false)用于解决一个奇怪的bug。在引入spring aop的情况下。
         * 在@Controller注解的类的方法中加入@RequiresRole注解，会导致该方法无法映射请求，导致返回404。
         * 加入这项配置能解决这个bug
         */
        /*creator.setUsePrefix(true);
        return creator;
    }*/

    //Filter工厂，设置对应的过滤条件和跳转条件
    @Bean
    public ShiroFilterFactoryBean shirFilter(SecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        // 必须设置 SecurityManager
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        // setLoginUrl 如果不设置值，默认会自动寻找Web工程根目录下的"/login.jsp"页面 或 "/login" 映射
        shiroFilterFactoryBean.setLoginUrl("/");
        // 设置无权限时跳转的 url;
        shiroFilterFactoryBean.setUnauthorizedUrl("/notPermission.html");

        Map<String, Filter> filters=shiroFilterFactoryBean.getFilters();
        filters.put("session",new ShiroSessionFilter());
        shiroFilterFactoryBean.setFilters(filters);
        //设置拦截器,设置拦截的url链
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
        //游客，开发权限
        //filterChainDefinitionMap.put("/guest/**", "anon");
        //用户，需要角色权限 “user”
        //filterChainDefinitionMap.put("/user/**", "roles[user]");
        //管理员，需要角色权限 “admin”
        //filterChainDefinitionMap.put("/admin/**", "roles[admin]");
        //开放登陆接口
        filterChainDefinitionMap.put("/login", "anon");
        filterChainDefinitionMap.put("/", "anon");
        filterChainDefinitionMap.put("/loginWeiXin", "anon");
        //开放静态资源
        filterChainDefinitionMap.put("/js/**", "anon");
        filterChainDefinitionMap.put("/bootstrap/**", "anon");
        filterChainDefinitionMap.put("/css/**", "anon");
        filterChainDefinitionMap.put("/image/**", "anon");
        filterChainDefinitionMap.put("/laydate/**", "anon");
        filterChainDefinitionMap.put("/music/**", "anon");
        filterChainDefinitionMap.put("/zTree/**", "anon");
        //其余接口一律拦截
        //主要这行代码必须放在所有权限设置的最后，不然会导致所有 url 都被拦截
        filterChainDefinitionMap.put("/**", "session,authc");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return shiroFilterFactoryBean;
    }

    //Thymeleaf模板html页面引入shiro标签
    @Bean
    public ShiroDialect shiroDialect() {
        return new ShiroDialect();
    }

}
