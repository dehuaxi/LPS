package com.defei.lps.serviceImp;

import com.defei.lps.dao.AreaMapper;
import com.defei.lps.dao.RouteMapper;
import com.defei.lps.dao.UserMapper;
import com.defei.lps.entity.Area;
import com.defei.lps.entity.Route;
import com.defei.lps.entity.User;
import com.defei.lps.result.Result;
import com.defei.lps.result.ResultUtil;
import com.defei.lps.service.AreaService;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AreaServiceImp implements AreaService {
    @Autowired
    private AreaMapper areaMapper;
    @Autowired
    private RouteMapper routeMapper;
    @Autowired
    private UserMapper userMapper;


    /**
     * 添加区域
     * @param areaName
     * @param areaNumber
     * @param describes
     * @return
     */
    @Override
    public Result add(String areaName, String areaNumber, String describes) {
        //校验参数
        if(!areaName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_-]{1,10}$")){
            return ResultUtil.error(1,"区域名称只能是1-20位的数字、大小写字母、汉字、特殊字符(@#_-)");
        }else if(!areaNumber.matches("^[0-9A-Za-z#@_-]{1,10}$")){
            return ResultUtil.error(1,"区域编号只能是1-9位的数字、大小写字母、特殊字符(@#_-)");
        }else if(describes.length()>50){
            return ResultUtil.error(1,"描述总字数不可超过50");
        }
        //验证区域编号是否重复
        Area area=areaMapper.selectByAreanumber(areaNumber);
        if(area!=null){
            return ResultUtil.error(1,"区域编号已经存在");
        }
        //再检查区域名称是否存在
        Area area1=areaMapper.selectByAreaname(areaName);
        if(area1!=null){
            return ResultUtil.error(1,"区域名称已经存在");
        }
        //都不存在，就保存
        Area area2=new Area();
        area2.setAreaname(areaName);
        area2.setAreanumber(areaNumber);
        area2.setDescribes(describes);
        areaMapper.insertSelective(area2);
        return ResultUtil.success();
    }

    /**
     * 修改
     */
    @Override
    public Result update(int id, String areaName, String areaNumber, String describes) {
        //校验参数
        if(!areaName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_-]{1,10}$")){
            return ResultUtil.error(1,"区域名称只能是1-20位的数字、大小写字母、汉字、特殊字符(@#_-)");
        }else if(!areaNumber.matches("^[0-9A-Za-z#@_-]{1,10}$")){
            return ResultUtil.error(1,"区域编号只能是1-9位的数字、大小写字母、特殊字符(@#_-)");
        }else if(describes.length()>50){
            return ResultUtil.error(1,"描述总字数不可超过50");
        }
        Area area=areaMapper.selectByPrimaryKey(id);
        if(area==null){
            return ResultUtil.error(1,"区域不存在，刷新页面后重试");
        }
        //验证区域编号是否重复
        if(!area.getAreanumber().equals(areaNumber)){
            Area area1=areaMapper.selectByAreanumber(areaNumber);
            if(area1!=null){
                return ResultUtil.error(1,"修改后的区域编号已经存在");
            }
        }
        //再检查区域名称是否存在
        if(!area.getAreaname().equals(areaName)){
            Area area1=areaMapper.selectByAreaname(areaName);
            if(area1!=null){
                return ResultUtil.error(1,"修改后的区域名称已经存在");
            }
        }
        //修改凡是用到了区域编号的地方
        if(!areaNumber.equals(area.getAreanumber())){

        }
        //修改区域信息本身
        area.setAreaname(areaName);
        area.setAreanumber(areaNumber);
        area.setDescribes(describes);
        areaMapper.updateByPrimaryKeySelective(area);
        return ResultUtil.success();
    }

    /**
     * 删除
     * @param id
     * @return
     */
    @Override
    public Result delete(int id) {
        Area area=areaMapper.selectByPrimaryKey(id);
        if(area==null){
            return ResultUtil.error(1,"区域不存在，刷新页面后重试");
        }
        //看线路表中有没有线路在使用该区域，如果有就提示无法删除
        List<Route> routeList=routeMapper.selectByAreaid(area.getId());
        if(routeList.isEmpty()){
            //删除区域信息
            areaMapper.deleteByPrimaryKey(id);
            return ResultUtil.success();
        }else {
            String result="";
            for(Route route:routeList){
                result+="、"+route.getRoutenumber();
            }
            result=result.substring(1);
            return ResultUtil.error(1,"有"+result+"共计"+routeList.size()+"条线路在使用本区域，无法删除");
        }
    }

    /**
     * 条件分页查询
     * @param areaName
     * @param areaNumber
     * @param currentPage
     * @return
     */
    @Override
    public Result findAll(String areaName, String areaNumber, int currentPage) {
        //校验参数
        if(!areaName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_-]{0,10}$")){
            return ResultUtil.error(1,"区域名称只能是1-20位的数字、大小写字母、汉字、特殊字符(@#_-)");
        }else if(!areaNumber.matches("^[0-9A-Za-z#@_-]{0,10}$")){
            return ResultUtil.error(1,"区域编号只能是1-9位的数字、大小写字母、特殊字符(@#_-)");
        }
        //查询起始下标
        int index=(currentPage-1)*30;
        //分页条件查询
        List<Area> list=areaMapper.selectLimitByCondition(areaName,areaNumber ,index);
        if(!list.isEmpty()) {
            //总数量
            int totalCount=areaMapper.selectCountByCondition(areaName,areaNumber);
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
     * 查询所有区域
     * @return
     */
    @Override
    public Result allArea() {
        //定义集合存放返回页面的区域信息
        List<Area> areaList=areaMapper.selectAll();
        if(!areaList.isEmpty()){
            return ResultUtil.success(areaList);
        }
        return ResultUtil.success();
    }

    /**
     * 询当前账号能看到的区域
     * @return
     */
    @Override
    public Result currentArea() {
        String userName=(String)SecurityUtils.getSubject().getPrincipal();
        User user=userMapper.selectByUserName(userName);
        if(user==null){
            return ResultUtil.error(1,"未登录，请登陆后再试");
        }
        List<Route> routeList=routeMapper.selectByUserid(user.getId());
        if(routeList.isEmpty()){
            return ResultUtil.error(1,"当前账号未分配线路");
        }
        //根据线路查询所有区域
        List<Area> areaList=new ArrayList<>();
        for(Route route:routeList){
            List<Area> areaList1=areaMapper.selectByRouteid(route.getId());
            if(!areaList1.isEmpty()){
                areaList.addAll(areaList1);
            }
        }
        //去重复
        List<Area> areaList1=areaList.stream().distinct().collect(Collectors.toList());
        return ResultUtil.success(areaList1);
    }
}
