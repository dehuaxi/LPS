package com.defei.lps.serviceImp;

import com.defei.lps.dao.PlanHandleRecordMapper;
import com.defei.lps.entity.PlanHandleRecord;
import com.defei.lps.result.Result;
import com.defei.lps.result.ResultUtil;
import com.defei.lps.service.PlanHandleRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PlanHandleRecordServiceImp implements PlanHandleRecordService {
    @Autowired
    private PlanHandleRecordMapper planHandleRecordMapper;

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
    public Result findAll(String goodCode, String goodName, String supplierCode, String supplierName, String date, int currentPage) {
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
        planHandleRecordMapper.deleteOneYearAgo();
        //查询起始下标
        int index=(currentPage-1)*30;
        //分页条件查询
        List<PlanHandleRecord> list= planHandleRecordMapper.selectLimitByCondition(goodCode,goodName,supplierCode,supplierName ,date,index);
        if(!list.isEmpty()) {
            //总数量
            int totalCount= planHandleRecordMapper.selectCountByCondition(goodCode,goodName,supplierCode,supplierName ,date);
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
