package com.defei.lps.controller;

import com.defei.lps.result.Result;
import com.defei.lps.result.ResultUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
public class LoginController {
    //日志对象
    //private static final Logger LOG = LoggerFactory.getLogger(LoginController.class);

    //设置系统的默认页面为登陆页面
    @RequestMapping(value = "/", produces = {"text/html;charset=UTF-8"})
    public String login(Model model) {
        return "login";
    }

    //登陆
    @RequestMapping(value = "login", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public Result login(String userName, String password,Model model) {
        //获取安全管理器内的主体对象
        Subject subject = SecurityUtils.getSubject();
        //创建shiro框架内置对象UsernamePasswordToken
        UsernamePasswordToken token = new UsernamePasswordToken(userName, password);
        try {
            //用户登陆
            subject.login(token);
            //把用户名存入Session以便后面使用
            Session session=SecurityUtils.getSubject().getSession();
            session.setAttribute("userName",userName);
            return ResultUtil.success("toIndex");
        } catch (IncorrectCredentialsException | UnknownAccountException e) {
            return ResultUtil.error(1,"账号或密码错误");
        }
    }

    //微信登陆。用户名和密码是明文传输进来的。
    @RequestMapping(value = "loginWeiXin", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public Result loginWeiXin(String userName, String password, HttpServletRequest request) {
        //获取安全管理器内的主体对象
        Subject subject = SecurityUtils.getSubject();
        //创建shiro框架内置对象UsernamePasswordToken
        UsernamePasswordToken token = new UsernamePasswordToken(userName, password);
        try {
            //用户登陆
            subject.login(token);
            //获取sessionID
            String sessionId=request.getSession().getId();
            //登陆成功
            return ResultUtil.success(sessionId);
        } catch (IncorrectCredentialsException | UnknownAccountException e) {
            //e.printStackTrace();
            return ResultUtil.error(1,"账号或密码错误");
        }
    }

    //登出
    @RequestMapping(value = "logOut", produces = {"text/html;charset=UTF-8"})
    public String loginOut() {
        SecurityUtils.getSubject().logout();
        //重定向到系统的默认页面
        return "redirect:/";
    }

    //跳转到主页
    @RequestMapping(value = "toIndex", produces = {"text/html;charset=UTF-8"})
    public String toIndex(Model model) {
        //获取当前session中的用户名
        Subject subject = SecurityUtils.getSubject();
        String userName=(String)subject.getSession().getAttribute("userName");
        if(userName==null||userName.equals("")){
            //重定向到系统的默认页面
            return "redirect:/";
        }
        //把用户名传入页面
        model.addAttribute("userName",userName);
        return "index";
    }

    //跳转到首页
    @RequestMapping(value = "toFirst", produces = {"text/html;charset=UTF-8"})
    public String toFirst() {
        return "test";
    }

}
