package com.defei.lps.serviceImp;

import com.defei.lps.dao.ParamsMapper;
import com.defei.lps.entity.Params;
import com.defei.lps.result.Result;
import com.defei.lps.result.ResultUtil;
import com.defei.lps.service.ParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 高德飞
 * @create 2020-12-02 13:24
 */
@Service
public class ParamServiceImp implements ParamService {
    @Autowired
    private ParamsMapper paramsMapper;

    @Override
    public Result add(String paramName, String paramValue, String paramType, String describes) {
        Params params = paramsMapper.selectByName(paramName);
        if(params ==null){
            Params params1 =new Params();
            params1.setParamname(paramName);
            params1.setParamtype(paramType);
            params1.setParamvalue(paramValue);
            params1.setDescribes(describes);
            paramsMapper.insertSelective(params1);
            return ResultUtil.success();
        }else {
            return ResultUtil.error(1,"参数名已存在");
        }
    }

    @Override
    public Result update(int id, String paramValue, String paramType, String describes) {
        Params params = paramsMapper.selectByPrimaryKey(id);
        if(params !=null){
            params.setParamvalue(paramValue);
            params.setParamtype(paramType);
            params.setDescribes(describes);
            paramsMapper.updateByPrimaryKeySelective(params);
        }
        return ResultUtil.success();
    }

    @Override
    public Result delete(int id) {
        paramsMapper.deleteByPrimaryKey(id);
        return ResultUtil.success();
    }

    @Override
    public Result findAll() {
        List<Params> list= paramsMapper.selectAll();
        if(list.isEmpty()){
            return ResultUtil.success();
        }
        return ResultUtil.success(list);
    }
}
