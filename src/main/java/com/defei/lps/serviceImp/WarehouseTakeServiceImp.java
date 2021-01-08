package com.defei.lps.serviceImp;

import com.defei.lps.dao.WarehouseCacheMapper;
import com.defei.lps.dao.WarehouseTakeMapper;
import com.defei.lps.entity.WarehouseCache;
import com.defei.lps.entity.WarehouseOut;
import com.defei.lps.entity.WarehouseTake;
import com.defei.lps.result.Result;
import com.defei.lps.result.ResultUtil;
import com.defei.lps.service.WarehouseTakeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 高德飞
 * @create 2021-01-04 16:57
 */
@Service
public class WarehouseTakeServiceImp implements WarehouseTakeService {
    @Autowired
    private WarehouseTakeMapper warehouseTakeMapper;
    @Autowired
    private WarehouseCacheMapper warehouseCacheMapper;

    /**
     * 条件分页查询
     * @param billNumber
     * @param carTypeName
     * @param startName
     * @param endName
     * @param userName
     * @param currentPage
     * @return
     */
    @Override
    public Result findAll(String billNumber, String carTypeName, String startName, String endName, String userName, int currentPage) {
        //校验参数
        if(!billNumber.matches("^[0-9A-Za-z#-]{0,30}$")){
            return ResultUtil.error(1,"物料编号只能是1-30位的数字、大小写字母、特殊字符(#-)");
        }else if(!userName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5]{0,10}$")){
            return ResultUtil.error(1,"账号名只能是10位的数字、大小写字母、汉字");
        }
        //查询起始下标
        int index=(currentPage-1)*30;
        //分页条件查询
        List<WarehouseTake> list=warehouseTakeMapper.selectBillLimitByCondition(billNumber,carTypeName,startName,endName,userName,index);
        if(!list.isEmpty()) {
            //总数量
            int totalCount=warehouseTakeMapper.selectBillCountByCondition(billNumber,carTypeName,startName,endName,userName);
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
     * 根据计划编号查询详情
     * @param billNumber
     * @return
     */
    @Override
    public Result detail(String billNumber) {
        List<WarehouseTake> warehouseTakeList=warehouseTakeMapper.selectByBillnumber(billNumber);
        if(warehouseTakeList.isEmpty()){
            return ResultUtil.error(1,"装载方案不存在");
        }
        return ResultUtil.success(warehouseTakeList);
    }

    /**
     * 根据在库物料记录id、方案编号，把该在库物料从方案中去掉
     * @param warehouseCacheId
     * @return
     */
    @Override
    public Result warehouseTakeDelete(int warehouseCacheId,String billNumber) {
        WarehouseCache warehouseCache=warehouseCacheMapper.selectByPrimaryKey(warehouseCacheId);
        if(warehouseCache==null){
            return ResultUtil.error(1,"选择的在库物料记录不存在");
        }
        //根据方案编号、物料id、批次查询
        WarehouseTake warehouseTake=warehouseTakeMapper.selectByBillnumberAndGoodidAndBatch(billNumber,warehouseCache.getGood().getId(),warehouseCache.getBatch());
        if(warehouseTake==null){
            return ResultUtil.error(1,"选择的装载方案记录不存在");
        }
        //修改在库记录的计划数量
        warehouseCache.setPlancount(warehouseCache.getPlancount()-warehouseTake.getCount());
        //从方案中删除：物料id一样、吉利批次一样的记录
        warehouseTakeMapper.deleteByPrimaryKey(warehouseTake.getId());
        return ResultUtil.success();
    }

    /**
     * 根据选择的在库记录id、数量、车型信息来计算物料的长、体积、重量,返回前端.把记录添加到方案
     * @param billNumber 装载方案编号
     * @param id 中转仓物料在库记录id
     * @param chooseCount 填入的数量
     * @param lowHeight 车辆低板高
     * @param carWidth 车辆车宽
     * @return
     */
    @Override
    public Result warehouseTakeAdd(String billNumber, int id, int chooseCount, int lowHeight, int carWidth) {
        WarehouseCache warehouseCache=warehouseCacheMapper.selectByPrimaryKey(id);
        if(warehouseCache==null){
            return ResultUtil.error(1,"所选的中转仓在库记录不存在");
        }
        List<WarehouseTake> warehouseTakeList=warehouseTakeMapper.selectByBillnumber(billNumber);
        if(warehouseTakeList.isEmpty()){
            return ResultUtil.error(1,"装载方案不存在");
        }
        //计算长、体积、重量

        //添加装载方案记录
        WarehouseTake warehouseTake=new WarehouseTake();
        warehouseTake.setBillnumber(billNumber);
        warehouseTake.setGood(warehouseCache.getGood());
        warehouseTake.setGeelybillnumber(warehouseCache.getGeelybillnumber());
        warehouseTake.setGeelycount(warehouseCache.getGeelycount());
        warehouseTake.setBatch(warehouseCache.getBatch());
        warehouseTake.setCount(chooseCount);
        warehouseTake.setOneboxcount(warehouseCache.getOneboxcount());
        int boxCount=0;
        if(chooseCount%warehouseCache.getOneboxcount()!=0){
            boxCount=chooseCount/warehouseCache.getOneboxcount()+1;
        }else {
            boxCount=chooseCount/warehouseCache.getOneboxcount();
        }
        warehouseTake.setBoxcount(boxCount);
        warehouseTake.setPackstate(warehouseCache.getPackstate());
        warehouseTake.setStartname(warehouseTakeList.get(0).getStartname());
        warehouseTake.setStartnumber(warehouseTakeList.get(0).getStartnumber());
        warehouseTake.setEndname(warehouseTakeList.get(0).getEndname());
        warehouseTake.setEndnumber(warehouseTakeList.get(0).getEndnumber());
        warehouseTake.setRoutetype(warehouseTakeList.get(0).getRoutetype());
        warehouseTake.setCartype(warehouseTakeList.get(0).getCartype());
        warehouseTake.setHighlength(warehouseTakeList.get(0).getHighlength());
        warehouseTake.setHighheight(warehouseTakeList.get(0).getHighheight());
        warehouseTake.setLowlength(warehouseTakeList.get(0).getLowlength());
        warehouseTake.setLowheight(warehouseTakeList.get(0).getLowheight());
        warehouseTake.setCarwidth(carWidth);
        warehouseTake.setCarheight(lowHeight);
        warehouseTake.setUsername(warehouseTakeList.get(0).getUsername());
        warehouseTake.setCreatetime(warehouseTakeList.get(0).getCreatetime());
        warehouseTake.setWarehouse(warehouseTakeList.get(0).getWarehouse());
        warehouseTakeMapper.insertSelective(warehouseTake);
        return ResultUtil.success();
    }
}
