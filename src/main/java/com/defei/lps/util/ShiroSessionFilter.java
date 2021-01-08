package com.defei.lps.util;

import com.alibaba.fastjson.JSONObject;
import com.defei.lps.result.Result;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 拦截ajax请求，判断session是否超时失效，如果失效就跳转到登录页面
 *
 * @author Administrator
 */
public class ShiroSessionFilter extends FormAuthenticationFilter {
    /*
     * 访问拒绝函数。当请求的url没有登陆时，就会调用此函数进行拦截
     * 没有登陆的情况：
     * 1.未登陆
     * 2.登陆了但是超时了
     */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response)
            throws IOException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        //在未登录的前提下，如果是ajax请求，就跳转到登录页面。非ajax请求，由shiro框架进行重定向跳转
        if (isAjax(request)) {
            //获取跳转的登陆页面的路径
            String path = httpServletRequest.getContextPath() + "/";
            Result result=new Result();
            result.setCode(-2);
            result.setMsg("登陆超时，请重新登陆");
            result.setData(path);
            httpServletResponse.setCharacterEncoding("UTF-8");
            httpServletResponse.setContentType("application/json");
            httpServletResponse.getWriter().write(JSONObject.toJSONString(result));

            //返回false，表示本过滤器处理请求了，这个请求就不需要向下处理了。
            return false;
        }
        //返回true表示，本过滤器不处理请求，把请求向下传递
        return true;
    }

    //判断是否是ajax请求
    private boolean isAjax(ServletRequest request) {
        String header = ((HttpServletRequest) request).getHeader("X-Requested-With");
        if ("XMLHttpRequest".equalsIgnoreCase(header)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
}
