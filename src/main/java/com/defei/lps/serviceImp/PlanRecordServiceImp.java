package com.defei.lps.serviceImp;

import com.defei.lps.dao.PlanRecordMapper;
import com.defei.lps.entity.PlanRecorde;
import com.defei.lps.result.Result;
import com.defei.lps.result.ResultUtil;
import com.defei.lps.service.PlanRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PlanRecordServiceImp implements PlanRecordService {
    @Autowired
    private PlanRecordMapper planRecordMapper;

    /**
     * 条件分页查询
     * @param goodCode
     * @param goodName
     * @param supplierCode
     * @param supplierName
     * @param date
     * @param currentPage
     * @return
     */
    @Override
    public Result findAll(String goodCode, String goodName, String supplierCode, String supplierName, String date,String urgent,int routeId,String type,String createDate,String overDate, int currentPage) {
        //校验参数
        if(!goodCode.matches("^[0-9A-Za-z#-]{0,30}$")){
            return ResultUtil.error(1,"物料编号只能是1-30位的数字、大小写字母、特殊字符(#-)");
        }else if(!goodName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_*-]{0,100}$")){
            return ResultUtil.error(1,"物料名称只能是1-100位的数字、大小写字母、汉字、特殊字符(@#_*-)");
        }if(!supplierCode.matches("^[0-9A-Za-z-]{0,6}$")){
            return ResultUtil.error(1,"供应商编号只能是1-6位的数字、大小写字母、特殊字符(-)");
        }else if(!supplierName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_*-]{0,50}$")){
            return ResultUtil.error(1,"供应商名称只能是1-50位的数字、大小写字母、汉字、特殊字符(@#_*-)");
        }
        //删除一年之前数据
        planRecordMapper.deleteOneYearAgo();
        //查询起始下标
        int index=(currentPage-1)*30;
        //分页条件查询
        List<PlanRecorde> list= planRecordMapper.selectLimitByCondition(goodCode,goodName,supplierCode,supplierName ,date,urgent,routeId,type,createDate,overDate,index);
        if(!list.isEmpty()) {
            //总数量
            int totalCount= planRecordMapper.selectCountByCondition(goodCode,goodName,supplierCode,supplierName ,date,urgent,routeId,type,createDate,overDate);
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
}
