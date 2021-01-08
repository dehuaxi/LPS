package com.defei.lps.controller;

import com.alibaba.fastjson.JSONObject;
import com.defei.lps.result.Result;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * controller层全局异常处理类
 * 用于处理controller层的异常
 *
 * @author Administrator
 */
@ControllerAdvice
public class GlobalExceptionHandler {
/*
    *//**
     * 登录认证异常
     *
     * @param request
     * @param response
     * @return
     *//*
    @ExceptionHandler({ UnauthenticatedException.class, AuthenticationException.class })
    public String authenticationException(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("未登录异常处理");
        String header = ((HttpServletRequest) request).getHeader("X-Requested-With");
        if ("XMLHttpRequest".equalsIgnoreCase(header)) {
            System.out.println("ajax未登录");
            Result result=new Result();
            result.setCode(-2);
            result.setMsg("未登录");
            writeJson(result, response);
        }
        //普通请求，直接跳转到登录页
        return "login";
    }*/

    /**
     * 权限异常
     *
     * @param request
     * @param response
     * @return
     */
    @ExceptionHandler({ UnauthorizedException.class, AuthorizationException.class })
    public String authorizationException(HttpServletRequest request, HttpServletResponse response) {
        String header = ((HttpServletRequest) request).getHeader("X-Requested-With");
        if ("XMLHttpRequest".equalsIgnoreCase(header)) {
            Result result=new Result();
            result.setCode(-1);
            result.setMsg("无权限");
            writeJson(result, response);
        }
        //普通请求，直接跳转到无权限提示页
        return "notPermission";
    }

    private void writeJson(Result map, HttpServletResponse response) {
        PrintWriter out = null;
        try {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=utf-8");
            out = response.getWriter();
            out.write(JSONObject.toJSONString(map));
        } catch (IOException e) {
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
}
