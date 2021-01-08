package com.defei.lps.serviceImp;

import com.defei.lps.dao.*;
import com.defei.lps.entity.*;
import com.defei.lps.result.Result;
import com.defei.lps.result.ResultUtil;
import com.defei.lps.service.WarehouseCacheService;
import org.apache.shiro.SecurityUtils;
import org.omg.CORBA.MARSHAL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author 高德飞
 * @create 2020-12-17 13:29
 */
@Service
public class WarehouseCacheServiceImp implements WarehouseCacheService {
    @Autowired
    private WarehouseCacheMapper warehouseCacheMapper;
    @Autowired
    private WarehouseMapper warehouseMapper;
    @Autowired
    private FactoryMapper factoryMapper;
    @Autowired
    private WarehouseTakeMapper warehouseTakeMapper;
    @Autowired
    private RouteMapper routeMapper;
    @Autowired
    private RouteWarehouseMapper routeWarehouseMapper;

    /**
     * 条件分页查询
     * @param goodCode
     * @param goodName
     * @param supplierCode
     * @param supplierName
     * @param geelyBillNumber
     * @param packState
     * @param warehouseId
     * @param date
     * @param currentPage
     * @return
     */
    @Override
    public Result findAll(String goodCode, String goodName,String supplierCode,String supplierName,String geelyBillNumber,String packState,int warehouseId,String date, int currentPage) {
        //校验参数
        if(!goodCode.matches("^[0-9A-Za-z#-]{0,30}$")){
            return ResultUtil.error(1,"物料编号只能是1-30位的数字、大小写字母、特殊字符(#-)");
        }else if(!goodName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_*-]{0,100}$")){
            return ResultUtil.error(1,"物料名称只能是1-100位的数字、大小写字母、汉字、特殊字符(@#_*-)");
        }else if(!supplierCode.matches("^[0-9A-Z]{0,6}$")){
            return ResultUtil.error(1,"供应商编号只能是1-6位的数字、大写字母");
        }else if(!supplierName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_*-]{0,50}$")){
            return ResultUtil.error(1,"供应商名称只能是1-50位的数字、大小写字母、汉字、特殊字符(@#_*-)");
        }else if(!geelyBillNumber.matches("^[0-9A-Za-z]{0,20}$")){
            return ResultUtil.error(1,"吉利单号只能是1-20位的数字、大小写字母");
        }
        //查询起始下标
        int index=(currentPage-1)*30;
        //分页条件查询
        List<WarehouseCache> list=warehouseCacheMapper.selectLimitByCondition(goodCode,goodName,supplierCode,supplierName ,geelyBillNumber,packState,warehouseId,date,index);
        if(!list.isEmpty()) {
            //总数量
            int totalCount=warehouseCacheMapper.selectCountByCondition(goodCode,goodName,supplierCode,supplierName ,geelyBillNumber,packState,warehouseId,date);
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
     * 根据选择的出发地、目的地、目的地类型获取该线路上所有的在库物料信息
     * @param startId
     * @param endId
     * @param endType
     * @return
     */
    @Override
    public Result warehouseCacheByRoute(int startId, int endId, String endType) {
        //出发地是中转仓
        Warehouse warehouse=warehouseMapper.selectByPrimaryKey(startId);
        if(warehouse==null){
            return ResultUtil.error(1,"出发地中转仓不存在，刷新后再试");
        }
        List<Route> routeList=new ArrayList<>();
        if(endType.equals("工厂")){
            Factory factory=factoryMapper.selectByPrimaryKey(endId);
            if(factory==null){
                return ResultUtil.error(1,"目的地工厂不存在，刷新后再试");
            }
            List<Route> routes=routeMapper.selectByFactoryidAndWarehouseid(endId,startId);
            if(routes.isEmpty()){
                return ResultUtil.error(1,"所选出发地中转仓和目的地工厂之间未创建线路");
            }
            //看所选中转仓是否是该线路的最后一个中转仓
            for(Route route:routes){
                //根据线路查询线路中转仓关系记录，并以中转仓在线路中的顺序号升序排序
                List<RouteWarehouse> routeWarehouseList=routeWarehouseMapper.selectByRouteid(route.getId());
                if(!routeWarehouseList.isEmpty()){
                    //获取最后一个中转仓，看是否就是当前选择的出发地中转仓
                    if(routeWarehouseList.get(routeWarehouseList.size()-1).getWarehouseid()==startId){
                        routeList.add(route);
                    }
                }
            }
        }else {
            Warehouse warehouse2=warehouseMapper.selectByPrimaryKey(endId);
            if(warehouse2==null){
                return ResultUtil.error(1,"目的地中转仓不存在，刷新后再试");
            }
            //查询线路集合
            List<Route> routeList1=routeMapper.selectByStartWarehouseidAndEndWarehouseid(startId,endId);
            if(routeList1.isEmpty()){
                return ResultUtil.error(1,"出发地中转仓和目的地中转仓之间未创建线路");
            }
            //判断这个线路中出发地中转仓和目的地中转仓中间是否还有中转仓，如果没有才能用这个线路去查询
            for(Route route:routeList1){
                //根据线路查询线路中转仓关系记录，并以中转仓在线路中的顺序号升序排序
                List<RouteWarehouse> routeWarehouseList=routeWarehouseMapper.selectByRouteid(route.getId());
                if(!routeWarehouseList.isEmpty()){
                    //看当前线路的出发地中转仓和目的地中转仓是否挨着
                    for(int o=0;o<routeWarehouseList.size();o++){
                        if(String.valueOf(routeWarehouseList.get(o).getWarehouseid()).equals(String.valueOf(startId))){
                            //看下一个记录的中转仓id是否和目的地id一样
                            if(String.valueOf(routeWarehouseList.get(o+1).getWarehouseid()).equals(String.valueOf(endId))){
                                routeList.add(route);
                            }
                        }
                    }
                }
            }
        }
        if(routeList.size()<=0){
            return ResultUtil.error(1,"所选出发地和目的地之间没有分配线路");
        }
        //根据线路id集合查询该线路集合内的中转仓在库记录，以线路排序，再以供应商排序
        List<WarehouseCache> warehouseCacheList=warehouseCacheMapper.selectByRouteids(routeList);
        if(warehouseCacheList.isEmpty()){
            return ResultUtil.error(1,"所选线路上没有中转仓在库记录");
        }
        //返回在库记录，线路集合
        Map<String,Object> map=new HashMap<>();
        map.put("warehouseCacheList",warehouseCacheList);
        map.put("routeList",routeList);
        return ResultUtil.success(map);
    }

    /**
     * 根据选择的在库记录id、数量、车型信息来计算物料的长、体积、重量,返回前端
     * @param id
     * @param chooseCount
     * @param lowHeight
     * @param carWidth
     * @return
     */
    @Override
    public Result warehouseCacheCalculate(int id, int chooseCount, int lowHeight, int carWidth) {
        WarehouseCache warehouseCache=warehouseCacheMapper.selectByPrimaryKey(id);
        if(warehouseCache==null){
            return ResultUtil.error(1,"选择的在库记录不存在，刷新页面后重试");
        }
        Good good=warehouseCache.getGood();
        //计算所选的物料的箱数
        int boxCount=0;
        if(chooseCount%warehouseCache.getOneboxcount()!=0){
            boxCount=chooseCount/warehouseCache.getOneboxcount()+1;
        }else {
            boxCount=chooseCount/warehouseCache.getOneboxcount();
        }

        Map<String,Object> map=new HashMap<>();
        map.put("good",good);
        //看该物料是托盘件还是非托盘件，分别计算层数、物料占的位置数、放几排、占车长米数、是用箱子或托盘的长宽哪一边作为靠车宽摆放边
        if(good.getOnetrayboxcount()==0){
            //非托盘件
            //堆叠层数=车厢高度（mm）/单箱高度（mm）
            int layers=lowHeight/good.getBoxheight();
            if(layers==0){
                return ResultUtil.error(1,"车高不可低于"+good.getBoxheight()+"，否则该物料无法装车");
            }
            //层数
            map.put("layers",layers);
            //所占位置数量，即一个箱子占的一个格子。位置数量=箱数/堆叠层数
            BigDecimal location=new BigDecimal(boxCount).divide(new BigDecimal(layers),2,BigDecimal.ROUND_HALF_UP);
            map.put("location",location);
            //此处分为4种情况。：1.如果车身宽度除箱子长度能够除尽，那么就用箱子长度作为排序宽度，进行摆放箱子
            //2.如果车身宽度除箱子长度不能够除尽，但是除箱子宽度能除尽，那么就用箱子长度作为排序宽度，进行摆放箱子
            //3.如果车身宽度除箱子长度和宽度都除不尽，看是否能够用车身宽度除以(箱子长+箱子宽)能不能除尽，如果可以那么就用箱子长+宽之和作为排序宽度，进行摆放箱子
            //4.如果前三种情况都除不尽，那么就看谁摆放后剩余的车身宽度最小，就用哪种方案摆放
            if(carWidth%good.getBoxlength()==0){
                //就用箱子长来做标准摆放。那么排数=位置数量/(车身宽度/箱子长度)
                BigDecimal row=location.divide(new BigDecimal(carWidth/good.getBoxlength()),2,BigDecimal.ROUND_HALF_UP);
                map.put("row",row);
                //米数=排数*箱子宽度
                BigDecimal length=row.multiply(new BigDecimal(good.getBoxwidth())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                map.put("length",length);
                map.put("side","箱长"+good.getBoxlength());
            }else if(carWidth%good.getBoxwidth()==0){
                //就用箱子宽来做标准摆放。那么排数=位置数量/(车身宽度/箱子宽度)
                BigDecimal row=location.divide(new BigDecimal(carWidth/good.getBoxwidth()),2,BigDecimal.ROUND_HALF_UP);
                map.put("row",row);
                //米数=排数*箱子长度
                BigDecimal length=row.multiply(new BigDecimal(good.getBoxlength())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                map.put("length",length);
                map.put("side","箱宽"+good.getBoxwidth());
            }else if(carWidth%(good.getBoxlength()+good.getBoxwidth())==0){
                //就用箱子长+宽来做标准摆放。那么排数=位置数量/(车身宽度/(箱子长+宽))
                BigDecimal row=location.divide(new BigDecimal(carWidth/(good.getBoxlength()+good.getBoxwidth())),2,BigDecimal.ROUND_HALF_UP);
                map.put("row",row);
                map.put("side","箱长+宽("+good.getBoxlength()+"+"+good.getBoxwidth()+")");
                //米数要以拼装的最长的一边为准。
                if(row.intValue()<=1){
                    //如果只有一排，那么米数就是箱子的长
                    map.put("length",new BigDecimal(good.getBoxlength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                }else {
                    //如果有多排，看哪种方式最合适
                    for(int i=1;i<location.intValue();i++){
                        //如果以托盘长为计算标准，从1开始逐步计算，看i*箱子长-（位置数量(向上取整)-i）*箱子宽的绝对值<箱子长，那么这个时候就是最优拼载方案
                        int cha=i*good.getBoxlength()-(location.intValue()+1-i)*good.getBoxwidth();
                        if(cha>-good.getBoxlength()||cha<good.getBoxwidth()){
                            //此时就是最优拼载方案，看哪边的总长大，哪边就是该物料的米数
                            if(cha>=0){
                                map.put("length",new BigDecimal(i*good.getBoxlength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                            }else {
                                map.put("length",new BigDecimal((location.intValue()+1-i)*good.getBoxwidth()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                            }
                        }
                    }
                }
            }else {
                //箱子长度作为计算宽度时，剩余的车身空闲宽度
                int lengthLast=carWidth-(carWidth/good.getBoxlength())*good.getBoxlength();
                //托盘宽度作为计算宽度时，剩余的车身空闲宽度
                int widthLast=carWidth-(carWidth/good.getBoxwidth())*good.getBoxwidth();
                if((good.getBoxlength()+good.getBoxwidth())>carWidth){
                    //如果箱长+宽>车宽，直接放弃方案3，只需要比较方案1、2哪个剩余宽度小就用哪个方案。剩余宽度一样，箱长度优先
                    if(lengthLast<=widthLast){
                        //就用箱子长来做标准摆放。那么排数=位置数量/(车身宽度/箱子长度)
                        BigDecimal row=location.divide(new BigDecimal(carWidth/good.getBoxlength()),2,BigDecimal.ROUND_HALF_UP);
                        map.put("row",row);
                        //米数=排数*箱子宽度
                        BigDecimal length=row.multiply(new BigDecimal(good.getBoxwidth())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                        map.put("length",length);
                        map.put("side","箱长"+good.getBoxlength());
                    }else {
                        //就用箱子宽来做标准摆放。那么排数=位置数量/(车身宽度/箱子宽度)
                        BigDecimal row=location.divide(new BigDecimal(carWidth/good.getBoxwidth()),2,BigDecimal.ROUND_HALF_UP);
                        map.put("row",row);
                        //米数=排数*箱子长度/1000
                        BigDecimal length=row.multiply(new BigDecimal(good.getBoxlength())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                        map.put("length",length);
                        map.put("side","箱宽"+good.getBoxwidth());
                    }
                }else {
                    //托盘长+宽作为计算宽度时，剩余的车身空闲宽度
                    int sumLast=carWidth-(carWidth/(good.getBoxlength()+good.getBoxwidth()))*(good.getBoxlength()+good.getBoxwidth());
                    if(lengthLast<=widthLast){
                        if(widthLast<=sumLast){
                            //长<=宽,且宽<=和，那么就用长。那么排数=位置数量/(车身宽度/箱子长度)
                            BigDecimal row=location.divide(new BigDecimal(carWidth/good.getBoxlength()),2,BigDecimal.ROUND_HALF_UP);
                            map.put("row",row);
                            //米数=排数*箱子宽度
                            BigDecimal length=row.multiply(new BigDecimal(good.getBoxwidth())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                            map.put("length",length);
                            map.put("side","箱长"+good.getBoxlength());
                        }else {
                            //长<=宽,且宽>和
                            if(lengthLast<=sumLast){
                                //长<=宽,且宽>和,且长<=和，用长。那么排数=位置数量/(车身宽度/箱子长度)
                                BigDecimal row=location.divide(new BigDecimal(carWidth/good.getBoxlength()),2,BigDecimal.ROUND_HALF_UP);
                                map.put("row",row);
                                //米数=排数*箱子宽度
                                BigDecimal length=row.multiply(new BigDecimal(good.getBoxwidth())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                                map.put("length",length);
                                map.put("side","箱长"+good.getBoxlength());
                            }else {
                                //长<=宽,且宽>和,且长>和，用和，就用箱子长+宽来做标准摆放。那么排数=位置数量/(车身宽度/(箱子长+宽))
                                BigDecimal row=location.divide(new BigDecimal(carWidth/(good.getBoxlength()+good.getBoxwidth())),2,BigDecimal.ROUND_HALF_UP);
                                map.put("row",row);
                                map.put("side","箱长+宽("+good.getBoxlength()+"+"+good.getBoxwidth()+")");
                                //米数要以拼装的最长的一边为准。
                                if(row.intValue()<=1){
                                    //如果只有一排，那么米数就是箱子的长
                                    map.put("length",new BigDecimal(good.getBoxlength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                                }else {
                                    //如果有多排，看哪种方式最合适
                                    for(int i=1;i<location.intValue();i++){
                                        //如果以托盘长为计算标准，从1开始逐步计算，看i*箱子长-（位置数量(向上取整)-i）*箱子宽的绝对值<箱子长，那么这个时候就是最优拼载方案
                                        int cha=i*good.getBoxlength()-(location.intValue()+1-i)*good.getBoxwidth();
                                        if(cha>-good.getBoxlength()||cha<good.getBoxwidth()){
                                            //此时就是最优拼载方案，看哪边的总长大，哪边就是该物料的米数
                                            if(cha>=0){
                                                map.put("length",new BigDecimal(i*good.getBoxlength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                                            }else {
                                                map.put("length",new BigDecimal((location.intValue()+1-i)*good.getBoxwidth()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }else {
                        if(sumLast<widthLast){
                            //长>宽，且宽>和，用和，就用箱子长+宽来做标准摆放。那么排数=位置数量/(车身宽度/(箱子长+宽))
                            BigDecimal row=location.divide(new BigDecimal(carWidth/(good.getBoxlength()+good.getBoxwidth())),2,BigDecimal.ROUND_HALF_UP);
                            map.put("row",row);
                            map.put("side","箱长+宽("+good.getBoxlength()+"+"+good.getBoxwidth()+")");
                            //米数要以拼装的最长的一边为准。
                            if(row.intValue()<=1){
                                //如果只有一排，那么米数就是箱子的长
                                map.put("length",new BigDecimal(good.getBoxlength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                            }else {
                                //如果有多排，看哪种方式最合适
                                for(int i=1;i<location.intValue();i++){
                                    //如果以托盘长为计算标准，从1开始逐步计算，看i*箱子长-（位置数量(向上取整)-i）*箱子宽的绝对值<箱子长，那么这个时候就是最优拼载方案
                                    int cha=i*good.getBoxlength()-(location.intValue()+1-i)*good.getBoxwidth();
                                    if(cha>-good.getBoxlength()||cha<good.getBoxwidth()){
                                        //此时就是最优拼载方案，看哪边的总长大，哪边就是该物料的米数
                                        if(cha>=0){
                                            map.put("length",new BigDecimal(i*good.getBoxlength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                                        }else {
                                            map.put("length",new BigDecimal((location.intValue()+1-i)*good.getBoxwidth()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                                        }
                                    }
                                }
                            }
                        }else {
                            //长>宽，且宽<=和
                            if(lengthLast<=sumLast){
                                //长>宽，且宽<=和,且长<=和，用宽
                                BigDecimal row=location.divide(new BigDecimal(carWidth/good.getBoxwidth()),2,BigDecimal.ROUND_HALF_UP);
                                map.put("row",row);
                                //米数=排数*箱子长度
                                BigDecimal length=row.multiply(new BigDecimal(good.getBoxlength())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                                map.put("length",length);
                                map.put("side","箱宽"+good.getBoxwidth());
                            }else {
                                //长>宽，且宽<=和,且长>和，用宽
                                BigDecimal row=location.divide(new BigDecimal(carWidth/good.getBoxwidth()),2,BigDecimal.ROUND_HALF_UP);
                                map.put("row",row);
                                //米数=排数*箱子长度
                                BigDecimal length=row.multiply(new BigDecimal(good.getBoxlength())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                                map.put("length",length);
                                map.put("side","箱宽"+good.getBoxwidth());
                            }
                        }
                    }
                }
            }
        }else {
            //托盘件
            //托盘数量=箱数/单托箱数
            BigDecimal trayCount=new BigDecimal(boxCount).divide(new BigDecimal(good.getOnetrayboxcount()),2,BigDecimal.ROUND_HALF_UP);
            //单托高度（mm）=物料箱高度（mm）*单托层数+托盘高度（mm）
            int trayHeight=good.getTrayheight()+good.getBoxheight()*good.getOnetraylayerscount();
            //堆叠层数=车厢高度（mm）/托盘高度（mm）
            int layers=lowHeight/trayHeight;
            if(layers==0){
                return ResultUtil.error(1,"车高不可低于"+trayHeight+"，否则该物料无法装车");
            }
            map.put("layers",layers);
            //所占位置数量，即一个托盘占的一个格子。位置数量=托盘数量/堆叠层数
            BigDecimal location=trayCount.divide(new BigDecimal(layers),2,BigDecimal.ROUND_HALF_UP);
            map.put("location",location);
            //此处分为4种情况。：1.如果车身宽度除托盘长度能够除尽，那么就用托盘长度作为排序宽度，进行摆放托盘
            //2.如果车身宽度除托盘长度不能够除尽，但是除托盘宽度能除尽，那么就用托盘长度作为排序宽度，进行摆放托盘
            //3.如果车身宽度除托盘长度和宽度都除不尽，看是否能够用车身宽度除以(托盘长+托盘宽)能不能除尽，如果可以那么就用托盘长+宽之和作为排序宽度，进行摆放托盘
            //4.如果前三种情况都除不尽，那么就看谁摆放后剩余的车身宽度最小，就用哪种方案摆放
            if(carWidth%good.getTraylength()==0){
                //就用托盘长来做标准摆放。那么排数=位置数量/(车身宽度/托盘长度)
                BigDecimal row=location.divide(new BigDecimal(carWidth/good.getTraylength()),2,BigDecimal.ROUND_HALF_UP);
                map.put("row",row);
                //米数=排数*托盘宽度
                BigDecimal length=row.multiply(new BigDecimal(good.getTraywidth())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                map.put("length",length);
                map.put("side","托盘长"+good.getTraylength());
            }else if(carWidth%good.getTraywidth()==0){
                //就用托盘宽来做标准摆放。那么排数=位置数量/(车身宽度/托盘宽度)
                BigDecimal row=location.divide(new BigDecimal(carWidth/good.getTraywidth()),2,BigDecimal.ROUND_HALF_UP);
                map.put("row",row);
                //米数=排数*托盘长度
                BigDecimal length=row.multiply(new BigDecimal(good.getTraylength())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                map.put("length",length);
                map.put("side","托盘宽"+good.getTraywidth());
            }else if(carWidth%(good.getTraywidth()+good.getTraylength())==0){
                //就用托盘长+宽来做标准摆放。那么排数=位置数量/(车身宽度/(托盘长+宽))
                BigDecimal row=location.divide(new BigDecimal(carWidth/(good.getTraywidth()+good.getTraylength())),2,BigDecimal.ROUND_HALF_UP);
                map.put("row",row);
                map.put("side","托盘长+宽("+good.getTraylength()+"+"+good.getTraywidth()+")");
                //米数要以拼装的最长的一边为准。
                if(row.intValue()<=1){
                    //如果只有一排，那么米数就是托盘的长
                    map.put("length",new BigDecimal(good.getTraylength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                }else {
                    //如果有多排，看哪种方式最合适
                    for(int i=1;i<location.intValue();i++){
                        //如果以托盘长为计算标准，从1开始逐步计算，看i*托盘长-（位置数量(向上取整)-i）*托盘宽的绝对值<托盘长，那么这个时候就是最优拼载方案
                        int cha=i*good.getTraylength()-(location.intValue()+1-i)*good.getTraywidth();
                        if(cha>-good.getTraylength()||cha<good.getTraylength()){
                            //此时就是最优拼载方案，看哪边的总长大，哪边就是该物料的米数
                            if(cha>=0){
                                map.put("length",new BigDecimal(i*good.getTraylength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                            }else {
                                map.put("length",new BigDecimal((location.intValue()+1-i)*good.getTraywidth()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                            }
                        }
                    }
                }
            }else {
                //托盘长度作为计算宽度时，剩余的车身空闲宽度
                int lengthLast=carWidth-(carWidth/good.getTraylength())*good.getTraylength();
                //托盘宽度作为计算宽度时，剩余的车身空闲宽度
                int widthLast=carWidth-(carWidth/good.getTraywidth())*good.getTraywidth();
                //托盘长+宽作为计算宽度时，剩余的车身空闲宽度。如果长+宽超过了车宽则直接放弃
                if((good.getTraywidth()+good.getTraylength())>carWidth){
                    //只需要考虑长和宽2种情况，如果长和宽都满足，那么以长优先
                    if(lengthLast<=widthLast){
                        //长做摆放
                        BigDecimal row=location.divide(new BigDecimal(carWidth/good.getTraylength()),2,BigDecimal.ROUND_HALF_UP);
                        map.put("row",row);
                        //米数=排数*托盘宽度
                        BigDecimal length=row.multiply(new BigDecimal(good.getTraywidth())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                        map.put("length",length);
                        map.put("side","托盘长"+good.getTraylength());
                    }else {
                        //用托盘宽来做标准摆放。那么排数=位置数量/(车身宽度/托盘宽度)
                        BigDecimal row=location.divide(new BigDecimal(carWidth/good.getTraywidth()),2,BigDecimal.ROUND_HALF_UP);
                        map.put("row",row);
                        //米数=排数*托盘长度
                        BigDecimal length=row.multiply(new BigDecimal(good.getTraylength())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                        map.put("length",length);
                        map.put("side","托盘宽"+good.getTraywidth());
                    }
                }else{
                    //如果长+宽<车宽，那么就用3种方式判断
                    int sumLast=carWidth-(carWidth/(good.getTraywidth()+good.getTraylength()))*(good.getTraywidth()+good.getTraylength());
                    System.out.println("托盘件:3种都不行，托盘长+宽摆放剩余宽度"+sumLast);
                    if(lengthLast<=widthLast){
                        if(widthLast<=sumLast){
                            //长<=宽,且宽<=和，那么就用长。那么排数=位置数量/(车身宽度/托盘长度)
                            BigDecimal row=location.divide(new BigDecimal(carWidth/good.getTraylength()),2,BigDecimal.ROUND_HALF_UP);
                            map.put("row",row);
                            //米数=排数*托盘宽度
                            BigDecimal length=row.multiply(new BigDecimal(good.getTraywidth())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                            map.put("length",length);
                            map.put("side","托盘长"+good.getTraylength());
                        }else {
                            //长<=宽,且宽>和
                            if(lengthLast<=sumLast){
                                //长<=宽,且宽>和,且长<=和，用长。那么排数=位置数量/(车身宽度/托盘长度)
                                BigDecimal row=location.divide(new BigDecimal(carWidth/good.getTraylength()),2,BigDecimal.ROUND_HALF_UP);
                                map.put("row",row);
                                //米数=排数*托盘宽度
                                BigDecimal length=row.multiply(new BigDecimal(good.getTraywidth())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                                map.put("length",length);
                                map.put("side","托盘长"+good.getTraylength());
                            }else {
                                //长<=宽,且宽>和,且长>和，用和，就用托盘长+宽来做标准摆放。那么排数=位置数量/(车身宽度/(托盘长+宽))
                                BigDecimal row=location.divide(new BigDecimal(carWidth/(good.getTraywidth()+good.getTraylength())),2,BigDecimal.ROUND_HALF_UP);
                                map.put("row",row);
                                map.put("side","托盘长+宽("+good.getTraylength()+"+"+good.getTraywidth()+")");
                                //米数要以拼装的最长的一边为准。
                                if(row.intValue()<=1){
                                    //如果只有一排，那么米数就是托盘的长
                                    map.put("length",new BigDecimal(good.getTraylength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                                }else {
                                    //如果有多排，看哪种方式最合适
                                    for(int i=1;i<location.intValue();i++){
                                        //如果以托盘长为计算标准，从1开始逐步计算，看i*托盘长-（位置数量(向上取整)-i）*托盘宽的绝对值<托盘长，那么这个时候就是最优拼载方案
                                        int cha=i*good.getTraylength()-(location.intValue()+1-i)*good.getTraywidth();
                                        if(cha>-good.getTraylength()||cha<good.getTraylength()){
                                            //此时就是最优拼载方案，看哪边的总长大，哪边就是该物料的米数
                                            if(cha>=0){
                                                map.put("length",new BigDecimal(i*good.getTraylength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                                            }else {
                                                map.put("length",new BigDecimal((location.intValue()+1-i)*good.getTraywidth()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }else {
                        if(sumLast<widthLast){
                            //长>宽，且宽>和，用和，就用托盘长+宽来做标准摆放。那么排数=位置数量/(车身宽度/(托盘长+宽))
                            BigDecimal row=location.divide(new BigDecimal(carWidth/(good.getTraywidth()+good.getTraylength())),2,BigDecimal.ROUND_HALF_UP);
                            map.put("row",row);
                            map.put("side","托盘长+宽("+good.getTraylength()+"+"+good.getTraywidth()+")");
                            //米数要以拼装的最长的一边为准。
                            if(row.intValue()<=1){
                                //如果只有一排，那么米数就是托盘的长
                                map.put("length",new BigDecimal(good.getTraylength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                            }else {
                                //如果有多排，看哪种方式最合适
                                for(int i=1;i<location.intValue();i++){
                                    //如果以托盘长为计算标准，从1开始逐步计算，看i*托盘长-（位置数量(向上取整)-i）*托盘宽的绝对值<托盘长，那么这个时候就是最优拼载方案
                                    int cha=i*good.getTraylength()-(location.intValue()+1-i)*good.getTraywidth();
                                    if(cha>-good.getTraylength()||cha<good.getTraylength()){
                                        //此时就是最优拼载方案，看哪边的总长大，哪边就是该物料的米数
                                        if(cha>=0){
                                            map.put("length",new BigDecimal(i*good.getTraylength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                                        }else {
                                            map.put("length",new BigDecimal((location.intValue()+1-i)*good.getTraywidth()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                                        }
                                    }
                                }
                            }
                        }else {
                            //长>宽，且宽<=和
                            if(lengthLast<=sumLast){
                                //长>宽，且宽<=和,且长<=和，用宽
                                //就用托盘宽来做标准摆放。那么排数=位置数量/(车身宽度/托盘宽度)
                                BigDecimal row=location.divide(new BigDecimal(carWidth/good.getTraywidth()),2,BigDecimal.ROUND_HALF_UP);
                                map.put("row",row);
                                //米数=排数*托盘长度
                                BigDecimal length=row.multiply(new BigDecimal(good.getTraylength())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                                map.put("length",length);
                                map.put("side","托盘宽"+good.getTraywidth());
                            }else {
                                //长>宽，且宽<=和,且长>和，用宽
                                //就用托盘宽来做标准摆放。那么排数=位置数量/(车身宽度/托盘宽度)
                                BigDecimal row=location.divide(new BigDecimal(carWidth/good.getTraywidth()),2,BigDecimal.ROUND_HALF_UP);
                                map.put("row",row);
                                //米数=排数*托盘长度
                                BigDecimal length=row.multiply(new BigDecimal(good.getTraylength())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                                map.put("length",length);
                                map.put("side","托盘宽"+good.getTraywidth());
                            }
                        }
                    }
                }
            }
        }
        //计算物料的总体积=物料体积+托盘体积
        //托盘体积系数
        BigDecimal trayRatio=new BigDecimal((good.getTrayratio()+100)).divide(new BigDecimal("100"));
        //物料总体积=箱数*箱长*箱宽*箱高/1000000000*托盘体积系数
        BigDecimal volume=new BigDecimal(good.getBoxlength()).multiply(new BigDecimal(good.getBoxwidth())).multiply(new BigDecimal(good.getBoxheight())).multiply(new BigDecimal(boxCount)).divide(new BigDecimal("1000000000"),2,BigDecimal.ROUND_HALF_UP).multiply(trayRatio);
        map.put("volume",volume);
        //计算物料的总重量t=单箱重量kg*箱数/1000
        BigDecimal weight=good.getBoxweight().multiply(new BigDecimal(boxCount)).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
        map.put("weight",weight);
        return ResultUtil.success(map);
    }

    /**
     * 中转仓生成装载计划
     * @param startId 出发地id
     * @param endId 目的地id
     * @param endType 目的地类型  1.中转仓 2.工厂
     * @param carType 车型
     * @param highLength 高板长
     * @param highHeight 高板高
     * @param lowLength 低板长
     * @param lowHeight 低板高
     * @param carWidth 车宽
     * @param goodInfos 在库记录信息集合，格式：在库记录id,车高mm,数量,箱数,长度,体积,重量;在库记录id,车高mm,数量,箱数,长度,体积,重量;...
     * @return
     */
    @Override
    public synchronized Result warehouseTakeAdd(int startId, int endId, String endType,String date, String carType, int highLength, int highHeight, int lowLength, int lowHeight, int carWidth, String goodInfos) {
        //验证出发中转仓是否存在。出发地是中转仓
        Warehouse warehouse=warehouseMapper.selectByPrimaryKey(startId);
        if(warehouse==null){
            return ResultUtil.error(1,"出发地中转仓不存在，刷新后再试");
        }
        String endName="";
        String endNumber="";
        String routeType="";
        //验证目的地是否存在，验证出发地和目的地之间是否分配了线路
        if(endType.equals("工厂")){
            Factory factory=factoryMapper.selectByPrimaryKey(endId);
            if(factory==null){
                return ResultUtil.error(1,"工厂不存在，刷新后再试");
            }
            endName=factory.getFactoryname();
            endNumber=factory.getFactorynumber();
            routeType="中转仓-工厂";
        }else {
            Warehouse warehouse1=warehouseMapper.selectByPrimaryKey(endId);
            if(warehouse1==null){
                return ResultUtil.error(1,"目的地中转仓不存在，刷新后再试");
            }
            endName=warehouse1.getWarehousename();
            endNumber=warehouse1.getWarehousenumber();
            routeType="中转仓-中转仓";
        }
        //获取方案内容,并验证每个中转仓在库记录是否都存在
        String[] infos=goodInfos.split(";");
        List<Map<String,Object>> infoList=new ArrayList<>();
        for(String info:infos){
            Map<String,Object> map=new HashMap<>();
            //获取在库记录
            WarehouseCache warehouseCache=warehouseCacheMapper.selectByPrimaryKey(Integer.parseInt(info.split(",")[0]));
            if(warehouseCache==null){
                return ResultUtil.error(1,"有选中的中转仓在库记录不存在，刷新页面后再试");
            }else {
                map.put("warehouseCache",warehouseCache);
                //车高
                map.put("carHeight",Integer.parseInt(info.split(",")[1]));
                //数量
                map.put("count",Integer.parseInt(info.split(",")[2]));
                //验证填入数量是否大于可分配数量
                int canUseCount=warehouseCache.getCount()-warehouseCache.getPlancount();
                if(Integer.parseInt(info.split(",")[2])>canUseCount){
                    return ResultUtil.error(1,"吉利单据"+warehouseCache.getGeelybillnumber()+"中物料"+warehouseCache.getGood().getGoodcode()+"填入的计划数量大于了可用数量"+canUseCount);
                }
                //箱数
                map.put("boxCount",Integer.parseInt(info.split(",")[3]));
                //长度m
                map.put("length",info.split(",")[4]);
                //体积立方米
                map.put("volume",info.split(",")[5]);
                //重量t
                map.put("weight",info.split(",")[6]);
                infoList.add(map);
            }
        }
        //生成计划编号：出发地编号+目的地编号+"-"+年4位+2位月+2位日+时分秒6位+3位毫秒
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Date now=new Date();
        String billNumber=warehouse.getWarehousenumber()+endNumber+"-"+simpleDateFormat.format(now);
        //获取当前账号
        String userName= (String) SecurityUtils.getSubject().getPrincipal();
        //添加中转仓装载方案、修改原缺记录的生成方案数量
        for(Map map:infoList){
            WarehouseTake warehouseTake=new WarehouseTake();
            warehouseTake.setBillnumber(billNumber);
            WarehouseCache warehouseCache=(WarehouseCache)map.get("warehouseCache");
            warehouseTake.setGood(warehouseCache.getGood());
            warehouseTake.setGeelybillnumber(warehouseCache.getGeelybillnumber());
            warehouseTake.setGeelycount(warehouseCache.getGeelycount());
            warehouseTake.setBatch(warehouseCache.getBatch());
            warehouseTake.setCount((Integer)map.get("count"));
            warehouseTake.setBoxcount((Integer)map.get("boxCount"));
            warehouseTake.setLength(new BigDecimal((String)map.get("length")));
            warehouseTake.setVolume(new BigDecimal((String)map.get("volume")));
            warehouseTake.setWeight(new BigDecimal((String)map.get("weight")));
            warehouseTake.setStartname(warehouse.getWarehousename());
            warehouseTake.setStartnumber(warehouse.getWarehousenumber());
            warehouseTake.setEndname(endName);
            warehouseTake.setEndnumber(endNumber);
            warehouseTake.setRoutetype(routeType);
            warehouseTake.setCartype(carType);
            warehouseTake.setHighlength(highLength);
            warehouseTake.setHighheight(highHeight);
            warehouseTake.setLowlength(lowLength);
            warehouseTake.setLowheight(lowHeight);
            warehouseTake.setCarheight((Integer)map.get("carHeight"));
            warehouseTake.setCarwidth(carWidth);
            warehouseTake.setUsername(userName);
            warehouseTake.setCreatetime(now);
            warehouseTakeMapper.insertSelective(warehouseTake);
            //修改在库记录的已分配方案数量
            //新的已分配方案数量=原已分配数量+数量
            warehouseCache.setPlancount(warehouseCache.getPlancount()+(Integer)map.get("count"));
            warehouseCacheMapper.updateByPrimaryKeySelective(warehouseCache);
        }
        return ResultUtil.success();
    }

    /**
     * 根据装载方案编号，查询装载方案详情，查询装载方案对应的线路的所有在库记录
     * @param billNumber
     * @return
     */
    @Override
    public Result warehouseCacheByBillnumber(String billNumber) {
        //1.先根据装载方案编号查询出装载方案
        List<WarehouseTake> warehouseTakeList=warehouseTakeMapper.selectByBillnumber(billNumber);
        if(warehouseTakeList.isEmpty()){
            return ResultUtil.error(1,"装载方案不存在");
        }
        //2.根据装载方案的出发地目的地查询这个线路上所有在库物料
        List<Route> routeList=new ArrayList<>();
        //获取出发地id
        int startId=0;
        Warehouse warehouse=warehouseMapper.selectByWarehousenumber(warehouseTakeList.get(0).getStartnumber());
        if(warehouse!=null){
            startId=warehouse.getId();
        }else {
            return ResultUtil.error(1,"出发地中转仓不存在");
        }
        String endType=warehouseTakeList.get(0).getRoutetype().split("-")[1];
        if(endType.equals("工厂")){
            Factory factory=factoryMapper.selectByFactorynumber(warehouseTakeList.get(0).getEndnumber());
            if(factory==null){
                return ResultUtil.error(1,"目的地工厂不存在");
            }
            List<Route> routes=routeMapper.selectByFactoryidAndWarehouseid(factory.getId(),startId);
            if(routes.isEmpty()){
                return ResultUtil.error(1,"所选出发地中转仓和目的地工厂之间未创建线路");
            }
            //看所选中转仓是否是该线路的最后一个中转仓
            for(Route route:routes){
                //根据线路查询线路中转仓关系记录，并以中转仓在线路中的顺序号升序排序
                List<RouteWarehouse> routeWarehouseList=routeWarehouseMapper.selectByRouteid(route.getId());
                if(!routeWarehouseList.isEmpty()){
                    //获取最后一个中转仓，看是否就是当前选择的出发地中转仓
                    if(routeWarehouseList.get(routeWarehouseList.size()-1).getWarehouseid()==startId){
                        routeList.add(route);
                    }
                }
            }
        }else {
            Warehouse warehouse2=warehouseMapper.selectByWarehousenumber(warehouseTakeList.get(0).getEndnumber());
            if(warehouse2==null){
                return ResultUtil.error(1,"目的地中转仓不存在");
            }
            //查询线路集合
            List<Route> routeList1=routeMapper.selectByStartWarehouseidAndEndWarehouseid(startId,warehouse2.getId());
            if(routeList1.isEmpty()){
                return ResultUtil.error(1,"出发地中转仓和目的地中转仓之间未创建线路");
            }
            //判断这个线路中出发地中转仓和目的地中转仓中间是否还有中转仓，如果没有才能用这个线路去查询
            for(Route route:routeList1){
                //根据线路查询线路中转仓关系记录，并以中转仓在线路中的顺序号升序排序
                List<RouteWarehouse> routeWarehouseList=routeWarehouseMapper.selectByRouteid(route.getId());
                if(!routeWarehouseList.isEmpty()){
                    //看当前线路的出发地中转仓和目的地中转仓是否挨着
                    for(int o=0;o<routeWarehouseList.size();o++){
                        if(String.valueOf(routeWarehouseList.get(o).getWarehouseid()).equals(String.valueOf(startId))){
                            //看下一个记录的中转仓id是否和目的地id一样
                            if(String.valueOf(routeWarehouseList.get(o+1).getWarehouseid()).equals(String.valueOf(warehouse2.getId()))){
                                routeList.add(route);
                            }
                        }
                    }
                }
            }
        }
        if(routeList.size()<=0){
            return ResultUtil.error(1,"所选出发地和目的地之间没有分配线路");
        }
        //根据线路id集合查询该线路集合内的中转仓在库记录，以线路排序，再以供应商排序
        List<WarehouseCache> warehouseCacheList=warehouseCacheMapper.selectByRouteids(routeList);
        if(warehouseCacheList.isEmpty()){
            return ResultUtil.error(1,"所选线路上没有中转仓在库记录");
        }
        //自定义：选择的装载方案记录个数
        int size=0;
        //在库记录集合
        List<Map<String,Object>> list=new ArrayList<>();
        for(WarehouseCache warehouseCache:warehouseCacheList){
            Map<String,Object> map=new HashMap<>();
            map.put("warehouseCache",warehouseCache);
            //当匹配数量<装载方案数量时，才判断是否一样，如果等于或大于了说名判断完了
            if(size<warehouseTakeList.size()){
                //看选择的和在库的是否是一样的：物料id、吉利单号、批次一样
                boolean isa=false;
                for(WarehouseTake warehouseTake:warehouseTakeList){
                    if(warehouseTake.getGood().getId().equals(warehouseCache.getGood().getId())&&warehouseCache.getGeelybillnumber().equals(warehouseTake.getGeelybillnumber())&&warehouseCache.getBatch().equals(warehouseTake.getBatch())){
                        isa=true;
                        size++;
                    }
                }
                if(isa){
                    map.put("checked",true);
                }else {
                    map.put("checked",false);
                }
            }else {
                map.put("checked",false);
            }
            list.add(map);
        }
        //返回页面
        Map<String,Object> map=new HashMap<>();
        map.put("takeList",warehouseTakeList);
        map.put("cacheList",list);
        return ResultUtil.success(map);
    }

    /**
     * 中转仓翻包
     * @param id 中转仓在库记录id
     * @param packCount 要翻包的总数量
     * @return
     */
    @Override
    public Result pack(int id, int packCount) {
        if(packCount<=0){
            return ResultUtil.error(1,"翻包数量必须为大于0的整数");
        }
        //验证1.中转仓在库记录是否存在
        WarehouseCache warehouseCache=warehouseCacheMapper.selectByPrimaryKey(id);
        if(warehouseCache==null){
            return ResultUtil.error(1,"在库记录不存在，无法翻包");
        }
        //验证2.中转仓在库记录是否是已经翻包后的记录
        if(warehouseCache.getPackstate().equals("已翻包")){
            return ResultUtil.error(1,"翻包过，无法二次翻包");
        }
        //验证完毕，翻包操作：看翻包总数量是否为原数量,如果不是则添加一条中转仓在库记录。否则还需要删除原数量，但是保留入库时间
        WarehouseCache warehouseCache1=new WarehouseCache();
        warehouseCache1.setGood(warehouseCache.getGood());
        warehouseCache1.setGeelybillnumber(warehouseCache.getGeelybillnumber());
        warehouseCache1.setGeelycount(warehouseCache.getGeelycount());
        warehouseCache1.setBatch(warehouseCache.getBatch());
        warehouseCache1.setCount(packCount);
        //收容数为物料的上线工装数
        warehouseCache1.setOneboxcount(warehouseCache.getGood().getBincount());
        //箱数
        int boxCount=0;
        if(packCount%warehouseCache.getGood().getBincount()==0){
            boxCount=packCount/warehouseCache.getGood().getBincount();
        }else {
            boxCount=packCount/warehouseCache.getGood().getBincount()+1;
        }
        warehouseCache1.setBoxcount(boxCount);
        warehouseCache1.setPackstate("已翻包");
        warehouseCache1.setCreatetime(warehouseCache.getCreatetime());
        warehouseCache1.setWarehouse(warehouseCache.getWarehouse());
        if(packCount==warehouseCache.getCount()){
            //删除原在库记录
            warehouseCacheMapper.deleteByPrimaryKey(id);
        }
        return ResultUtil.success();
    }

    @Override
    public Result findByReturnBillNumber(String returnBillNumber) {
        return null;
    }

    @Override
    public Result returnOut(String returnBillNumber, String goodInfos) {
        return null;
    }

    /**
     * 生成出库运输单页面，根据条件查询中转仓在库记录
     * @param goodCode
     * @param goodName
     * @param supplierCode
     * @param supplierName
     * @param geelyBillNumber
     * @return
     */
    @Override
    public Result warehouseCacheByCondition(String goodCode, String goodName, String supplierCode, String supplierName, String geelyBillNumber,String packState,int warehouseId) {
        List<WarehouseCache> warehouseCacheList=warehouseCacheMapper.selectByCondition(goodCode,goodName,supplierCode,supplierName,geelyBillNumber,packState,warehouseId,"");
        if(warehouseCacheList.isEmpty()){
            return ResultUtil.error(1,"无查询结果");
        }
        return ResultUtil.success(warehouseCacheList);
    }
}
