package com.defei.lps.serviceImp;

import com.defei.lps.dao.*;
import com.defei.lps.entity.*;
import com.defei.lps.result.Result;
import com.defei.lps.result.ResultUtil;
import com.defei.lps.service.ShortageService;
import com.defei.lps.uploadUtil.ShortageExcelUpload;
import com.defei.lps.util.DateUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ShortageServiceImp implements ShortageService {
    @Autowired
    private ShortageMapper shortageMapper;
    @Autowired
    private FactoryMapper factoryMapper;
    @Autowired
    private RouteMapper routeMapper;
    @Autowired
    private GoodMapper goodMapper;
    @Autowired
    private PlanCacheMapper planCacheMapper;
    @Autowired
    private PlanRecordMapper planRecordMapper;

    /**
     * 上传缺件报表 1.新增或修改缺件记录 2.修改或新增未确认的计划
     * @param excelFile
     * @return
     */
    @Override
    @Transactional
    public synchronized Result upload(MultipartFile excelFile,int factoryId) {
        ShortageExcelUpload se=new ShortageExcelUpload();
        List<Shortage> list=se.getList(excelFile,factoryId);
        if(list==null){
            return ResultUtil.error(1,se.getMessage());
        }
        //循环集合，保存或者修改缺件记录
        int updateCount=0;
        int newCount=0;
        //1.保存缺件信息。记录上传的物料信息集合
        List<Good> goodList=new ArrayList<>();
        for(Shortage shortage:list){
            if(shortage.getId()!=null){
                //说明是前一天的记录，只需要修改结存信息
                Shortage shortage1=shortageMapper.selectByPrimaryKey(shortage.getId());
                shortage1.setLaststock(shortage1.getStock());
                shortage1.setStock(shortage.getStock());
                shortageMapper.updateByPrimaryKeySelective(shortage1);
            }else {
                //1.修改或者新增缺件记录。因为物料是分了工厂的，所以根据物料id查询结果就相当于根据工厂查询了。
                Shortage shortage1=shortageMapper.selectByGoodidAndDate(shortage.getGood().getId(),shortage.getDate());
                if(shortage1==null){
                    shortageMapper.insertSelective(shortage);
                    newCount++;
                }else {
                    //如果已经存在，修改记录。把已经存在的需求数量和结存改为上次结存和上次需求
                    shortage1.setLastneedcount(shortage1.getNeedcount());
                    shortage1.setLaststock(shortage1.getStock());
                    shortage1.setStock(shortage.getStock());
                    shortage1.setNeedcount(shortage.getNeedcount());
                    shortageMapper.updateByPrimaryKeySelective(shortage1);
                    updateCount++;
                }
                if(!goodList.contains(shortage.getGood())){
                    goodList.add(shortage.getGood());
                }
            }
        }
        //2.新增缺件计划记录
        List<PlanCache> newPlan=new ArrayList<>();
        //日期格式化
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat simpleDateFormat1=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //当前日期
        Date now=new Date();
        //今天的日期
        String today=simpleDateFormat.format(now);
        //循环上传的缺件报表中的物料，给每个物料生成缺件计划
        /*for(Good good:goodList){
            //删除所有的该物料的未确认的计划
            planCacheMapper.deleteByGoodidAndState(good.getId(),"未确认");
            //验证：如果该物料上传的缺件报表记录中从今天开始未来几天的所有缺件需求和结存值都是0，则该物料不计算缺件计划
            List<Shortage> shortages=shortageMapper.selectByGoodidAndDatestartAndDateend(good.getId(),today,"");
            //是否能创建缺件计划，默认位否
            boolean canCreate=false;
            //判断每一天的需求数量是否为0，如果每天的需求数量都为0，那么无需计算缺件计划
            for(Shortage shortage1:shortages){
                if(shortage1.getNeedcount()!=0){
                    //需求数量不为0，跳出循环，需要创建计划
                    canCreate=true;
                }
            }
            if(canCreate==false){
                shortages=null;
                //无需创建缺件计划，直接循环下一个物料
                continue;
            }
            //获取当前循环物料的配送周期天数
            int transitDay=new BigDecimal(good.getSupplier().getTransitday()).intValue();
            if(good.getSupplier().getTransitday().contains(".")){
                //如果配送天数值含有.号，就说明是有小数的，那么统一给配送周期向上取整
                transitDay++;
            }
            //看是否有发货日期大于今天的在途计划，有就需要给今天生成的计划添加备注
            String remarks="";
            List<PlanCache> planCacheList=planCacheMapper.selectGreaterReceiveateByGoodidAndExcludeState(good.getId(),"未确认",today);
            if(!planCacheList.isEmpty()){
                remarks="有"+planCacheList.size()+"个已确认计划的发货日期大于"+today+",请和厂家沟通取消";
            }
            //第1：先判断是否有第一个正常缺件计划。第一个正常缺件计划标准：以上传的第二天为发货日期，往后推运输周期天数得到到货日期，看到货日期后一天的结存是否小于等于物料拉动库存
            //自定义：该物料第一个正常计划的应该到货日期在缺件集合中的下标.默认为运输周期天数+1
            int index=transitDay+1;
            System.out.println("当前物料的最大库存："+good.getMaxstock()+",最小库存："+good.getTriggerstock()+",收容数："+good.getOneboxcount());
            //如果第一个正常计划的到货日期第二天的下标不超过缺件集合最后一个记录的下标，那么就可以技术正常计划
            if((transitDay+2)<=(shortages.size()-1)){
                System.out.println("-----可以生成正常计划-----");
                //自定义：前一个正常缺件计划到货后结存.由于前面没有正常缺件计划，所以就默认为第一个正常缺件计划到货当天的上传的缺件报表记录的结存，以此来判断第一个正常缺件计划是否应该生成
                int planStock=shortages.get(transitDay+1).getStock();
                //自定义：前一次正常缺件计划的到货数量,以计划的最大取货数量为准。由于前面没有，所以默认为0
                int planCount=0;
                //从第一个正常缺件计划到货日期后一天开始循环，生成正常缺件计划
                for(int i=(transitDay+2);i<shortages.size();i++){
                    //当前循环的收货后结存=前一个计划到货数量-当前循环的需求数量
                    int currentStock=planStock-shortages.get(i).getNeedcount();
                    System.out.println("当前循环的到货后结存："+currentStock);
                    if(currentStock<=good.getTriggerstock()){
                        System.out.println("到货后结存<拉动库存："+good.getTriggerstock()+",生成计划");
                        //生成正常缺件计划
                        //最小取货数量。到货日期后一天数的补足拉动库存数+扣除到货日期后一天剩余的运输周期天数的缺件报表记录的需求数
                        int minCount=0;
                        for(int g=0;g<transitDay;g++){
                            Shortage shortage=shortages.get(i+g);
                            if(shortage!=null){
                                minCount+=shortage.getNeedcount();
                            }
                        }
                        System.out.println("最小取货数量："+minCount);
                        //修正，箱数向上取整
                        if(minCount%good.getOneboxcount()!=0){
                            minCount=(minCount/good.getOneboxcount()+1)*good.getOneboxcount();
                        }
                        System.out.println("修正最小取货数量："+minCount);
                        //最大取货数量
                        int maxCount=0;
                        //获取前一天的到货后结存
                        int yestodayPlanStock=planStock;
                        System.out.println("原昨天到货后结存："+yestodayPlanStock);
                        if(yestodayPlanStock<0){
                            yestodayPlanStock=0;
                        }
                        System.out.println("修正后昨天到货后结存："+yestodayPlanStock);
                        if((minCount+yestodayPlanStock+shortages.get(i-1).getNeedcount())<good.getMaxstock()){
                            System.out.println("头一天的需求数量:"+shortages.get(i-1).getNeedcount());
                            maxCount=good.getMaxstock()-shortages.get(i-1).getNeedcount()-yestodayPlanStock;
                            System.out.println("有额外数量时：最大取货数量："+maxCount);
                            //箱数向下取整
                            if(maxCount%good.getOneboxcount()!=0){
                                maxCount=(maxCount/good.getOneboxcount()-1)*good.getOneboxcount();
                            }
                            System.out.println("有额外数量时：修正后最大数量："+maxCount);
                        }else {
                            maxCount=minCount;
                            System.out.println("无额外数量时：修正最大取货数量："+maxCount);
                        }
                        //更新前一次计划到货数量
                        planCount=maxCount;
                        //更新前一次正常缺件计划的到货后结存。
                        planStock=currentStock+maxCount;
                        //添加本次生成的正常缺件计划
                        PlanCache newPlanCache=new PlanCache();
                        newPlanCache.setGood(good);
                        newPlanCache.setCount(maxCount);
                        newPlanCache.setMaxcount(maxCount);
                        newPlanCache.setMincount(minCount);
                        newPlanCache.setSurecount(0);
                        newPlanCache.setTakecount(0);
                        newPlanCache.setReceivecount(0);
                        //由于前面的总取货数量已经做过收容数取整，所以直接得到箱数
                        newPlanCache.setBoxcount(maxCount/good.getOneboxcount());
                        //发货日期:从当前日期回推运输周期+1天
                        newPlanCache.setDate(shortages.get(i-1-transitDay).getDate());
                        //到货日期为当前的前一天
                        newPlanCache.setReceivedate(shortages.get(i-1).getDate());
                        newPlanCache.setState("未确认");
                        newPlanCache.setType("系统");
                        newPlanCache.setUrgent("否");
                        newPlanCache.setRemarks(remarks);
                        newPlanCache.setCreatetime(now);
                        newPlan.add(newPlanCache);
                    }
                }
            }else {
                System.out.println("-----无法生成正常计划-----");
                //无需生成第一个正常缺件计划，那么说明第一个正常缺件集合到货日期大于了缺件报表中最大日期
                if(index>(shortages.size()-1)){
                    index=shortages.size()-1;
                }
            }
            //循环今天到第一个正常计划的到货日期下标，看是否有紧急计划
            List<Shortage> shortageList=new ArrayList<>();
            for(int i=0;i<=index;i++){
                shortageList.add(shortages.get(i));
            }
            //自定义：前一天的收货后的结存。默认为第一天的结存+第一天的需求
            int yestodayStock=shortages.get(0).getNeedcount()+shortages.get(0).getStock();
            for(int i=0;i<=index;i++){
                //当天结存
                int currentStock=yestodayStock-shortages.get(i).getNeedcount();
                System.out.println("紧急拉动：当天结存："+currentStock);
                //如果当天结存<=物料拉动库存，就生成计划
                if(currentStock<=good.getTriggerstock()){
                    System.out.println("生成紧急拉动计划");
                    //生成紧急计划
                    //发货日期
                    String sendDate="";
                    //到货日期
                    String receiveDate="";
                    //把期间所有到货的数量更新到缺件报表集合中，找到更新后结存最小的记录。
                    for(int t=0;t<index;t++){
                        PlanCache planCache1=planCacheMapper.selectByGoodidAndReceivedate(good.getId(),shortages.get(t).getDate());
                        if(planCache1!=null){
                            //更新后面的结存
                            for(int e=t+1;e<shortageList.size();e++){
                                shortageList.get(e).setStock(shortageList.get(e).getStock()+planCache1.getCount());
                            }
                        }
                    }
                    //找到更新后的集合中结存最小的记录,结存升序排序
                    List<Shortage> sortList=shortageList.stream().sorted(Comparator.comparing(Shortage::getStock)).collect(Collectors.toList());
                    //最小取货数量=拉动库存-最小结存记录的结存
                    int minCount=good.getTriggerstock()-sortList.get(0).getStock();
                    System.out.println("最小取货数量："+minCount);
                    //最小取货数量，箱数向上取整。如果是没有零头箱，那么总箱数之上再加上一箱。
                    if(minCount%good.getOneboxcount()!=0){
                        minCount=(minCount/good.getOneboxcount()+1)*good.getOneboxcount();
                    }else {
                        minCount=minCount+good.getOneboxcount();
                    }
                    //推算到货日期就为当前循环的前一天
                    if((i-1)<=0){
                        //到货日期为上传当天或当天之前，那么紧急计划，发货日期和到货日期都为上传当天
                        sendDate=today;
                        receiveDate=today;
                    }else {
                        //到货日期在集合中，那么获取到货日期
                        receiveDate=shortages.get(i-1).getDate();
                        //回推发货日期
                        if((i-1-transitDay)<=0){
                            sendDate=today;
                        }else {
                            sendDate=shortages.get(i-1-transitDay).getDate();
                        }
                    }
                    //看生成的紧急计划是否有在途计划于其发货日期、到货日期都一样，如果有，就把紧急计划合并到在途之中。并把在途计划改为紧急
                    PlanCache planCache=planCacheMapper.selectByGoodidAndDateAndReceivedate(good.getId(),sendDate,receiveDate);
                    if(planCache!=null){
                        planCache.setUrgent("是");
                        planCache.setRemarks(today+"新增"+minCount+"的紧急需求，请及时配送");
                        //修改最小取货数量
                        planCache.setMincount(planCache.getMincount()+minCount);
                        //如果原计划的最大取货数量<修改后最小取货数量，则修改最大取货数量
                        if(planCache.getMaxcount()<(planCache.getMincount()+minCount)){
                            planCache.setMaxcount(planCache.getMincount()+minCount);
                        }
                        //修改取货数量
                        planCache.setCount(planCache.getCount()+minCount);
                        planCacheMapper.updateByPrimaryKeySelective(planCache);
                    }else {
                        //没有发货日期和到货日期都一样的在途记录，那么就添加本次生成的紧急缺件计划
                        PlanCache newPlanCache=new PlanCache();
                        newPlanCache.setGood(good);
                        newPlanCache.setCount(minCount);
                        //紧急拉动计划的最大取货数量=最小取货数量。因为这个计划是补第一个正常计划到货期间不足的数量，不能送太多
                        newPlanCache.setMaxcount(minCount);
                        newPlanCache.setMincount(minCount);
                        newPlanCache.setSurecount(0);
                        newPlanCache.setTakecount(0);
                        newPlanCache.setReceivecount(0);
                        //由于前面的总取货数量已经做过收容数取整，所以直接得到箱数
                        newPlanCache.setBoxcount(minCount/good.getOneboxcount());
                        newPlanCache.setDate(sendDate);
                        newPlanCache.setReceivedate(receiveDate);
                        newPlanCache.setState("未确认");
                        newPlanCache.setType("系统");
                        newPlanCache.setUrgent("是");
                        newPlanCache.setRemarks(remarks);
                        newPlanCache.setCreatetime(now);
                        newPlan.add(newPlanCache);
                    }
                    //产生了一条紧急拉动，那么就跳出循环
                    break;
                }else {
                    //查询当天是否有到货，如果有到货，就把到货数量加到当天结存中，然后更新前一天收货后结存
                    PlanCache planCache1=planCacheMapper.selectByGoodidAndReceivedate(good.getId(),shortages.get(i).getDate());
                    if(planCache1!=null){
                        yestodayStock=currentStock+planCache1.getCount();
                    }else{
                        yestodayStock=currentStock;
                    }
                }
            }
        }*/
        for(Good good:goodList){
            //删除所有的该物料的未确认的计划
            planCacheMapper.deleteByGoodidAndState(good.getId(),"未确认");
            //验证：如果该物料上传的缺件报表记录中从今天开始未来几天的所有缺件需求和结存值都是0，则该物料不计算缺件计划
            List<Shortage> shortages=shortageMapper.selectByGoodidAndDatestartAndDateend(good.getId(),today,"");
            //是否能创建缺件计划，默认位否
            boolean canCreate=false;
            //判断每一天的需求数量是否为0，如果每天的需求数量都为0，那么无需计算缺件计划
            for(Shortage shortage1:shortages){
                if(shortage1.getNeedcount()!=0){
                    //需求数量不为0，跳出循环，需要创建计划
                    canCreate=true;
                }
            }
            if(canCreate==false){
                shortages=null;
                //无需创建缺件计划，直接循环下一个物料
                continue;
            }
            //获取当前循环物料的配送周期天数
            int transitDay=new BigDecimal(good.getSupplier().getTransitday()).intValue();
            if(good.getSupplier().getTransitday().contains(".")){
                //如果配送天数值含有.号，就说明是有小数的，那么统一给配送周期向上取整
                transitDay++;
            }
            //（一）：先把从今天开始的缺件报表集合，查询每天是否有到货的在途缺件计划。如果有就把到货数量更新到缺件报表集合的结存中，
            //更新方式：到货当日结存+到货数量就是到货当日的新结存，然后用新结存去减去后面每天的需求，得到每日的新结存
            for(int i=0;i< shortages.size();i++){
                //获取每日的是否有到货的在途计划
                List<PlanCache> planCacheList=planCacheMapper.selectByGoodidAndReceivedate(shortages.get(i).getGood().getId(),shortages.get(i).getDate());
                if(!planCacheList.isEmpty()){
                    //更新结存1：先看到货在途缺件计划集合中是否有特急的计划，如果有，就先用特急的计划修改缺件报表集合的结存：到货当天的新结存=当天结存+到货数量
                    int nextCount=0;//非特急在途缺件计划的数量
                    for(PlanCache planCache:planCacheList){
                        if(planCache.getUrgent().equals("特急")){
                            //到货当日的新结存
                            int newStock=planCache.getCount()+shortages.get(i).getStock();
                            //修改到货当日的结存
                            shortages.get(i).setStock(newStock);
                            //更新以后每天的结存
                            for(int k=1;k<=(shortages.size()-1-i);k++){
                                //修改结存
                                shortages.get(i+k).setStock(newStock-shortages.get(i+k).getNeedcount());
                                //更新初始结存
                                newStock=newStock-shortages.get(i+k).getNeedcount();
                            }
                        }else {
                            nextCount++;
                        }
                    }
                    //更新结存2：更新非特急的在途缺件计划到货后得结存
                    if(nextCount>0){
                        //当特急在途缺件计划更新缺件报表集合的结存完毕后，再用非特急的在途缺件计划更新缺件报表集合的结存：如果到货当天结存为负数，那么到货当天的结存不变更，用到货数量做初始结存更新到货第二天及以后的每日结存
                        for(PlanCache planCache:planCacheList){
                            if(!planCache.getUrgent().equals("特急")){
                                //更新结存的初始结存值。如果到货当天结存<0，则初始结存值=到货数量,且不需要修改到货当天的结存
                                int newStock=planCache.getCount();
                                if(shortages.get(i).getStock()>0){
                                    //如果到货当天结存>0，则初始结存值=到货数量+到货当天结存
                                    newStock=shortages.get(i).getStock()+planCache.getCount();
                                }
                                //更新以后每天的结存
                                for(int k=1;k<=(shortages.size()-1-i);k++){
                                    //修改结存
                                    shortages.get(i+k).setStock(newStock-shortages.get(i+k).getNeedcount());
                                    //更新初始结存
                                    newStock=newStock-shortages.get(i+k).getNeedcount();
                                }
                            }
                        }
                    }
                }
            }
            //（二）：用更新完后的缺件报表集合来判断是否生成缺件计划。
            //第1：先判断是否有第一个正常缺件计划。第一个正常缺件计划标准：以上传的第二天为发货日期，往后推运输周期天数得到到货日期，看到货日期后一天的结存是否小于等于物料拉动库存
            //自定义：该物料第一个正常计划的应该到货日期在缺件集合中的下标.默认为运输周期天数+1
            int index=transitDay+1;
            //如果第一个正常计划的到货日期第二天的下标不超过缺件集合最后一个记录的下标，那么就可以创建第一个正常计划
            if((transitDay+2)<=(shortages.size()-1)){
                //自定义：看第一个正常计划到货后的第二天的结存是否小于拉动库存，如果小于拉动库存就产生第一个正常计划，如果不小就继续往后看结存是否小于最小拉动库存
                for(int i=(transitDay+2);i<shortages.size();i++){
                    //验证：先看当前的日期的需求是否大于0，如果当前需求不大于0，那么就无需再去看结存是否小于等于物料最小拉动库存，无需计算缺件计划
                    if(new BigDecimal(shortages.get(i).getNeedcount()).compareTo(BigDecimal.ZERO)==1){
                        //当前的需求大于0了，再看结存是否小于等于物料最小拉动库存，如果是，就必然能生成第一个正常缺件计划
                        if(shortages.get(i).getStock()<=good.getTriggerstock()){
                            System.out.println("第一个正常计划-----"+shortages.get(i).getDate()+"结存"+shortages.get(i).getStock()+"小于等于了拉动库存"+good.getTriggerstock()+"，开始生成正常计划");
                            //结存小于等于了物料拉动库存，生成缺件计划
                            PlanCache planCache=new PlanCache();
                            planCache.setGood(good);
                            //先计算最少送货数量：从到货日期后运输周期内天数的需求数量之和，再加上物料最小拉动库存，如果是整工装，那么还要加1箱
                            int minCount=0;
                            for(int r=0;r<transitDay;r++){
                                //如果下标还在缺件报表集合内，那么就把需求加起来
                                if((i+r)<shortages.size()){
                                    minCount+=shortages.get(i+r).getNeedcount();
                                }
                            }
                            System.out.println("第一个正常计划-----运输周期天数的总需求："+minCount);
                            //加上物料最小拉动库存
                            minCount+=good.getTriggerstock();
                            System.out.println("第一个正常计划-----加上最小库存的最小取货数量："+minCount);
                            //修正，箱数向上取整.如果总数刚好是没有领头箱的，还要额外加一箱
                            minCount=(minCount/good.getOneboxcount()+1)*good.getOneboxcount();
                            System.out.println("第一个正常计划-----修正后的最小取货数量："+minCount);
                            //最大取货数量
                            int maxCount=0;
                            //获取前一天的到货后结存
                            int yestodayPlanStock=shortages.get(i-1).getStock();
                            if(yestodayPlanStock<0){
                                yestodayPlanStock=0;
                            }
                            if((minCount+yestodayPlanStock+shortages.get(i-1).getNeedcount())<good.getMaxstock()){
                                //System.out.println("第一个正常计划-----最小取货数量+到货当天结存"+yestodayPlanStock+"到货当天需求"+shortages.get(i-1).getNeedcount()+"之和小于了最大库存："+good.getMaxstock());
                                maxCount=good.getMaxstock()-shortages.get(i-1).getNeedcount()-yestodayPlanStock;
                                //箱数向下取整
                                if(maxCount%good.getOneboxcount()!=0){
                                    maxCount=(maxCount/good.getOneboxcount()-1)*good.getOneboxcount();
                                }
                            }else {
                                maxCount=minCount;
                            }
                            //System.out.println("第一个正常计划-----修正后最大取货数量："+maxCount);
                            planCache.setCount(maxCount);
                            planCache.setMaxcount(maxCount);
                            planCache.setMincount(minCount);
                            planCache.setSurecount(0);
                            planCache.setTakecount(0);
                            planCache.setReceivecount(0);
                            planCache.setBoxcount(maxCount/good.getOneboxcount());
                            //当前日期的前一天是到后日期，在从到货日期往回推运输周期天
                            //System.out.println("第一个计划-------------发货日期："+shortages.get(i-1-transitDay).getDate());
                            planCache.setDate(shortages.get((i-1-transitDay)).getDate());
                            //System.out.println("第一个计划-------------到货日期："+shortages.get(i-1).getDate());
                            planCache.setReceivedate(shortages.get(i-1).getDate());
                            planCache.setState("未确认");
                            planCache.setType("系统");
                            planCache.setUrgent("正常");
                            planCache.setRemarks("");
                            planCache.setCreatetime(now);
                            //看是否有发货日期、到货日期一样的在途缺件计划
                            PlanCache oldPlanCache=planCacheMapper.selectByGoodidAndDateAndReceivedate(good.getId(),shortages.get((i-1-transitDay)).getDate(),shortages.get(i-1).getDate());
                            //System.out.println("第一个正常计划-----看是否由发货、到货日期一样的记录:"+oldPlanCache);
                            if(oldPlanCache!=null){
                                planCache=null;
                                if(oldPlanCache.getMincount()<minCount){
                                    StringBuffer a=new StringBuffer("最小数量由");
                                    a.append(oldPlanCache.getMincount());
                                    a.append("变为");
                                    a.append(minCount);
                                    //如果原计划最小数量<了新计划的最小数量，那么修改原计划的最小数量
                                    oldPlanCache.setMincount(minCount);
                                    if(minCount>=oldPlanCache.getMaxcount()){
                                        a.append(";最大数量由");
                                        a.append(oldPlanCache.getMaxcount());
                                        a.append("变为");
                                        a.append(maxCount);
                                        oldPlanCache.setMaxcount(maxCount);
                                    }
                                    oldPlanCache.setRemarks(a.toString());
                                    planCacheMapper.updateByPrimaryKeySelective(oldPlanCache);
                                }
                            }else {
                                newPlan.add(planCache);
                            }
                            //生成缺件计划后，再拿生成的缺件计划的最大数量更新后面的缺件报表集合的结存，看后面更新后的结存还有没有小于物料最小拉动库存的
                            //那么第二个计划的最小取货数量就是到货日期之后所有天数的需求数之和
                            //初始结存=昨天的结存+第一个计划的最大到后数量
                            int startCount=maxCount+yestodayPlanStock;
                            //System.out.println("第二个正常计划的起始结存："+startCount);
                            //更新第一个计划的到货数量到缺件报表集合的结存中
                            for(int g=i;g<shortages.size();g++){
                                int currentStock=startCount-shortages.get(g).getNeedcount();
                                //System.out.println("原结存："+shortages.get(g).getStock()+"----更新了第一个计划到货数量后结存："+currentStock);
                                shortages.get(g).setStock(currentStock);
                                startCount=currentStock;
                            }
                            //用更新结存后的集合查看结存是否小于物料拉动库存
                            for(int g=i;g<shortages.size();g++){
                                //验证：先看当前的日期的需求是否大于0，如果当前需求不大于0，那么就无需再去看结存是否小于等于物料最小拉动库存，无需计算缺件计划
                                if(new BigDecimal(shortages.get(g).getNeedcount()).compareTo(BigDecimal.ZERO)==1){
                                    //System.out.println("看第一个正常计划更新后是否结存小于拉动库存："+shortages.get(g).getStock());
                                    if(shortages.get(g).getStock()<=good.getTriggerstock()){
                                        //System.out.println("第2个正常计划-----于"+shortages.get(g).getDate()+"的新结存"+shortages.get(g).getStock()+"小于了最小拉动库存"+shortages.get(g).getStock());
                                        //生成第二个正常缺件计划，其最小取货数量就是到货后所有天的需求之和，再加上物料最小拉动库存
                                        PlanCache planCache2=new PlanCache();
                                        planCache2.setGood(good);
                                        //先计算最少送货数量：到货后所有天的需求之和，再加上物料最小拉动库存
                                        int minCount2=0;
                                        for(int r=0;r<(shortages.size()-g);r++){
                                            //如果下标还在缺件报表集合内，那么就把需求加起来
                                            if((g+r)<shortages.size()){
                                                minCount2+=shortages.get(g+r).getNeedcount();
                                            }
                                        }
                                        //System.out.println("第2个计划------最小取货数量："+minCount2);
                                        //加上物料最小拉动库存
                                        minCount2+=good.getTriggerstock();
                                        //System.out.println("第2个计划------加上最小拉动库存的最小取货数量："+minCount2);
                                        //修正，箱数向上取整
                                        minCount2=(minCount2/good.getOneboxcount()+1)*good.getOneboxcount();
                                        //System.out.println("第2个计划------修正后最小取货数量："+minCount2);
                                        //最大取货数量
                                        int maxCount2=0;
                                        //获取前一天的到货后结存
                                        int yestodayPlanStock2=shortages.get(g-1).getStock();
                                        //System.out.println("第2个计划------到货当天的结存数量："+yestodayPlanStock2);
                                        if(yestodayPlanStock2<0){
                                            yestodayPlanStock2=0;
                                        }
                                        if((minCount2+yestodayPlanStock2+shortages.get(g-1).getNeedcount())<good.getMaxstock()){
                                            maxCount2=good.getMaxstock()-shortages.get(g-1).getNeedcount()-yestodayPlanStock2;
                                            //箱数向下取整
                                            if(maxCount2%good.getOneboxcount()!=0){
                                                maxCount2=(maxCount2/good.getOneboxcount()-1)*good.getOneboxcount();
                                            }
                                        }else {
                                            maxCount2=minCount2;
                                        }
                                        planCache2.setCount(maxCount2);
                                        planCache2.setMaxcount(maxCount2);
                                        planCache2.setMincount(minCount2);
                                        planCache2.setSurecount(0);
                                        planCache2.setTakecount(0);
                                        planCache2.setReceivecount(0);
                                        planCache2.setBoxcount(maxCount2/good.getOneboxcount());
                                        //当前日期的前一天是到后日期，在从到货日期往回推运输周期天
                                        //System.out.println("第2个计划-----------发货日期："+shortages.get(g-1-transitDay).getDate());
                                        planCache2.setDate(shortages.get((g-1-transitDay)).getDate());
                                        //System.out.println("第2个计划-----------到货日期："+shortages.get(g-1).getDate());
                                        planCache2.setReceivedate(shortages.get(g-1).getDate());
                                        planCache2.setState("未确认");
                                        planCache2.setType("系统");
                                        planCache2.setUrgent("正常");
                                        planCache2.setRemarks("");
                                        planCache2.setCreatetime(now);
                                        //看是否有发货日期、到货日期一样的在途缺件计划
                                        PlanCache oldPlanCache2=planCacheMapper.selectByGoodidAndDateAndReceivedate(good.getId(),shortages.get((i-1-transitDay)).getDate(),shortages.get(i-1).getDate());
                                        //System.out.println("第二个正常计划是否有重复的在途记录："+oldPlanCache2);
                                        if(oldPlanCache2!=null){
                                            planCache2=null;
                                            if(oldPlanCache2.getMincount()<minCount2){
                                                StringBuffer a=new StringBuffer("最小数量由");
                                                a.append(oldPlanCache2.getMincount());
                                                a.append("变为");
                                                a.append(minCount2);
                                                //如果原计划最小数量<了新计划的最小数量，那么修改原计划的最小数量
                                                oldPlanCache2.setMincount(minCount2);
                                                if(minCount2>=oldPlanCache2.getMaxcount()){
                                                    a.append(";最大数量由");
                                                    a.append(oldPlanCache2.getMaxcount());
                                                    a.append("变为");
                                                    a.append(maxCount2);
                                                    oldPlanCache2.setMaxcount(maxCount2);
                                                }
                                                oldPlanCache2.setRemarks(a.toString());
                                                planCacheMapper.updateByPrimaryKeySelective(oldPlanCache2);
                                            }
                                        }else {
                                            newPlan.add(planCache2);
                                        }
                                        //更新第2个计划的到货数量到缺件报表集合的结存中
                                        //初始结存=第一个计划更新后昨天的结存+第2个计划的最大到后数量
                                        int startCount2=maxCount+shortages.get(g-1).getStock();
                                        for(int h=g;h<shortages.size();h++){
                                            int currentStock=startCount2-shortages.get(h).getNeedcount();
                                            shortages.get(h).setStock(currentStock);
                                            startCount2=currentStock;
                                        }
                                    }
                                }

                            }
                        }
                    }
                }
            }else {
                //无需生成第一个正常缺件计划，那么说明第一个正常缺件计划的到货日期等于或大于了缺件报表中最大日期,也就是说整个缺件报表集合都在第一个正常缺件计划到货日期之内，那么需要看这个集合内每天的结存是否有小于物料最小拉动库存的
                index=shortages.size()-1;
            }
            //循环今天到第一个正常计划的到货日期下标，看是否有紧急计划
            List<Shortage> shortageList=new ArrayList<>();
            for(int i=0;i<=index;i++){
                shortageList.add(shortages.get(i));
            }
            //System.out.println("紧急集合："+shortageList);
            //循环需要查看是否生成紧急计划的缺件报表集合shortageList
            //紧急计划总的需求数量
            int totalNeed=0;
            //紧急计划的到货日期
            String receiveDate="";
            //紧急计划的发货日期
            String sendDate="";
            //紧急程度
            String urgent="紧急";
            for(int i=0;i<shortageList.size();i++) {
                if (new BigDecimal(shortageList.get(i).getStock()).compareTo(new BigDecimal(good.getTriggerstock())) == 1) {
                    //System.out.println("当天结存>物料最小拉动库存");
                    //如果当天结存>物料最小拉动库存,继续循环
                    continue;
                } else if (new BigDecimal(shortageList.get(i).getStock()).compareTo(new BigDecimal(good.getTriggerstock())) == 0) {
                    //System.out.println("当天结存==物料最小拉动库存");
                    //如果当天结存==物料最小拉动库存，看是否已经有到货日期，如果没有，那么当天就是到货日期
                    if (receiveDate.equals("")) {
                        receiveDate = shortageList.get(i).getDate();
                        //计算发货日期
                        if ((i - transitDay) <= 0) {
                            sendDate = shortageList.get(0).getDate();
                            urgent="特急";
                        } else {
                            sendDate = shortageList.get((i - transitDay)).getDate();
                        }
                    }
                } else if (new BigDecimal(shortageList.get(i).getStock()).compareTo(new BigDecimal(good.getTriggerstock())) == -1) {
                    //System.out.println(shortageList.get(i).getDate()+"当天结存"+shortageList.get(i).getStock()+"<物料最小拉动库存"+good.getTriggerstock());
                    //如果当天结存<物料最小拉动库存，看是否已经有到货日期，如果没有，那么前一天就是到货日期
                    if (receiveDate.equals("")) {
                        //到货日期必须是前一天,如果前一天是今天或今天之前，那么到货日期就为今天
                        if((i-1)<=0){
                            receiveDate = shortageList.get(0).getDate();
                            urgent="特急";
                        }else {
                            receiveDate = shortageList.get(i-1).getDate();
                        }
                        //计算发货日期
                        if ((i-1 - transitDay) <= 0) {
                            sendDate = shortageList.get(0).getDate();
                            urgent="特急";
                        } else {
                            sendDate = shortageList.get((i-1 - transitDay)).getDate();
                        }
                    }
                    //得到当天需求的负数=0-当天需求
                    BigDecimal fushu=BigDecimal.ZERO.subtract(new BigDecimal(shortageList.get(i).getNeedcount()));
                    //如果当天结存<物料最小拉动库存,再看当天结存是否<当天的需求的负数值，如果是，则需求总数的累加值=当天需求数量+物料最小拉动库存，否则需求总数的累加值=物料最小拉动库存-当天结存
                    if (new BigDecimal(shortageList.get(i).getStock()).compareTo(fushu) != 1) {
                        //当天结存<=当天需求的负数，需求总数的累加值=当天需求数量+物料最小拉动库存
                        totalNeed += (shortageList.get(i).getNeedcount()+good.getTriggerstock());
                        System.out.println("当天结存<=当天需求的负数:当天的累加数量："+(shortageList.get(i).getNeedcount()+good.getTriggerstock()));
                    } else {
                        //当天结存>当天需求负数，需求总数的累加值=物料最小拉动库存-当天结存
                        totalNeed += (good.getTriggerstock() - shortageList.get(i).getStock());
                        //System.out.println("当天结存>当天需求的负数:当天的累加数量："+(good.getTriggerstock() - shortageList.get(i).getStock()));
                    }
                }
            }
            //System.out.println("紧急需求数量："+totalNeed);
            //如果有紧急计划的取货数量，说明有紧急计划
            if(totalNeed>0){
                //取货数量向上取整
                //修正，箱数向上取整
                if(totalNeed%good.getOneboxcount()!=0){
                    totalNeed=(totalNeed/good.getOneboxcount()+1)*good.getOneboxcount();
                }
                //生成紧急计划，如果有发货日期、收货日期一样的在途缺件计划，就合并
                PlanCache oldPlanCache=planCacheMapper.selectByGoodidAndDateAndReceivedate(good.getId(),sendDate,receiveDate);
                //System.out.println("紧急计划是否有一样的在途记录："+oldPlanCache);
                if(oldPlanCache!=null){
                    //看数量是否一致，如果一样，就不保存
                    if(oldPlanCache.getCount()==totalNeed){
                        continue;
                    }else if(oldPlanCache.getCount()>totalNeed){
                        //原计划数量大于了当前生成的数量，继续循环
                        continue;
                    }else {
                        //原计划数量小于了当前生成的数量，增加备注，修改数量
                        int oldCount=oldPlanCache.getCount();
                        oldPlanCache.setCount(totalNeed);
                        oldPlanCache.setMincount(totalNeed);
                        oldPlanCache.setMaxcount(totalNeed);
                        oldPlanCache.setRemarks("于"+simpleDateFormat1.format(now)+"更新计划，从原数量"+oldCount+"增加至"+totalNeed+"，请尽快补足差额"+(totalNeed-oldCount));
                        planCacheMapper.updateByPrimaryKeySelective(oldPlanCache);
                    }
                }else {
                    //System.out.println("发货日期："+sendDate);
                    //System.out.println("到货日期："+receiveDate);
                    PlanCache planCache=new PlanCache();
                    planCache.setGood(good);
                    planCache.setCount(totalNeed);
                    planCache.setMaxcount(totalNeed);
                    planCache.setMincount(totalNeed);
                    planCache.setSurecount(0);
                    planCache.setTakecount(0);
                    planCache.setReceivecount(0);
                    planCache.setBoxcount(totalNeed/good.getOneboxcount());
                    //当前日期的前一天是到后日期，在从到货日期往回推运输周期天
                    planCache.setDate(sendDate);
                    planCache.setReceivedate(receiveDate);
                    planCache.setState("未确认");
                    planCache.setType("系统");
                    planCache.setUrgent(urgent);
                    planCache.setRemarks("");
                    planCache.setCreatetime(now);
                    newPlan.add(planCache);
                }
            }
        }
        //批量添加新计划
        if(!newPlan.isEmpty()){
            planCacheMapper.insertBatch(newPlan);
        }
        return ResultUtil.success("上传"+goodList.size()+"种物料的"+list.size()+"条缺件信息,新增"+newCount+"条，更新"+updateCount+"条;新增或更新"+newPlan.size()+"条未确认计划");
    }

    /**
     * 条件分页查询
     * @param goodCode
     * @param goodName
     * @param supplierCode
     * @param supplierName
     * @param factoryId 工厂id
     * @param dateStart 缺件开始日期
     * @param dateEnd 缺件结束日期
     * @param currentPage
     * @return
     */
    @Override
    public Result findAll(String goodCode, String goodName, String supplierCode, String supplierName, int factoryId,int routeId, String dateStart, String dateEnd, int currentPage) {
        //校验参数
        if(!goodCode.matches("^[0-9a-zA-Z]{0,30}$")){
            return ResultUtil.error(1,"物料编号只能是1-30位的数字、大小写字母");
        }else if(!goodName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_*-]{0,100}$")){
            return ResultUtil.error(1,"物料名称只能是1-100位的数字、大小写字母、汉字、特殊字符(@#_*-)");
        }else if(!supplierCode.matches("^[0-9A-Z]{0,6}$")){
            return ResultUtil.error(1,"供应商编号只能是1-6位的数字、大写字母");
        }else if(!supplierName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_*-]{0,50}$")){
            return ResultUtil.error(1,"供应商名称只能是1-50位的数字、大小写字母、汉字、特殊字符(@#_*-)");
        }
        if(factoryId!=0){
            Factory factory=factoryMapper.selectByPrimaryKey(factoryId);
            if(factory==null){
                return ResultUtil.error(1,"工厂不存在，刷新页面后重试");
            }
        }
        if(routeId!=0){
            Route route=routeMapper.selectByPrimaryKey(routeId);
            if(route==null){
                return ResultUtil.error(1,"线路不存在，刷新页面后重试");
            }
        }
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
        Date now=new Date();
        String today=simpleDateFormat.format(now);
        //结束日期必须大于起始日期
        if(dateStart.equals("")){
            if(!dateEnd.equals("")){
                //开始日期为空，结束日期不为空，那么结束日期必须大于当天
                try {
                    if(simpleDateFormat.parse(dateEnd).getTime()<=simpleDateFormat.parse(today).getTime()){
                        return ResultUtil.error(1,"结束日期必须大于当天日期");
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    return ResultUtil.error(1,"结束日期的值错误");
                }
            }
        }else {
            if(!dateEnd.equals("")){
                //开始日期不为空，结束日期不为空，判断结束日必须大于开始日期
                try {
                    if(simpleDateFormat.parse(dateEnd).getTime()<=simpleDateFormat.parse(dateStart).getTime()){
                        return ResultUtil.error(1,"结束日期必须大于开始日期");
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    return ResultUtil.error(1,"结束日期、开始日期的值错误");
                }
            }
        }
        //查询起始下标
        int index=(currentPage-1)*30;
        //分页条件查询物料信息
        List<Good> list=goodMapper.selectLimitByCondition(goodCode,goodName,supplierCode,supplierName ,"",factoryId,routeId,index);
        if(!list.isEmpty()) {
            //总数量
            int totalCount=goodMapper.selectCountByCondition(goodCode,goodName,supplierCode,supplierName ,"",factoryId,routeId);
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
            //内容集合，该集合包括2部分：1.物料信息  2.物料对应的传入日期之间的所有缺件信息和计划信息
            List<Map<String,Object>> resultList=new ArrayList<>();
            if(dateStart.equals("")){
                //开始日期为空，默认为当天
                dateStart= new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            }
            if(dateEnd.equals("")){
                //结束日期为空，默认为系统中存在的最大缺件日期
                Shortage shortage=shortageMapper.selectMaxDate();
                if(shortage!=null){
                    //如果没有传入结束日期，默认的最大日期也不存在，那么说明传入开始日期到默认最大日期之间没有记录
                    dateEnd=shortage.getDate();
                }
            }
            //如果传入开始日期和结束日期经过默认值校验了任何一个都没有值，那么表示没有缺件记录
            if(dateStart.equals("")||dateEnd.equals("")){
                //直接返回物料信息
                for(Good good:list){
                    Map<String,Object> map1=new HashMap<>();
                    map1.put("good",good);
                    map1.put("shortageList",null);
                    resultList.add(map1);
                }
                map.put("list",resultList);
                map.put("dateList",null);
                return ResultUtil.success(map);
            }else {
                //如果开始日期和结束日期都有值，需要验证结束日期必须大于等于开始日期
                try {
                    if(simpleDateFormat.parse(dateEnd).getTime()<simpleDateFormat.parse(dateStart).getTime()){
                        //结束日期大于开始日期，那么直接返物料信息
                        for(Good good:list){
                            Map<String,Object> map1=new HashMap<>();
                            map1.put("good",good);
                            map1.put("shortageList",null);
                            resultList.add(map1);
                        }
                        map.put("list",resultList);
                        map.put("dateList",null);
                        return ResultUtil.success(map);
                    }else {
                        //需要获取开始日期和结束日期之间的缺件信息和计划信息
                        //根据其实日期和结束日期，获取中间的每一天日期集合
                        List<String> dateList=DateUtil.getBetweenDate(dateStart,dateEnd);
                        //如果开始日期和结束日期之间有日期，则返回物料信息+每个物料对应的每个日期的需求、结存、计划数量、计划状态信息
                        for(Good good:list){
                            Map<String,Object> map1=new HashMap<>();
                            map1.put("good",good);
                            List<Map<String,Object>> shortageList=new ArrayList<>();
                            //根据日期集合查询每个物料的缺件记录以及对应的运行中计划记录
                            for(String date:dateList){
                                //缺件记录
                                Shortage shortage=shortageMapper.selectByGoodidAndDate(good.getId(),date);
                                Map<String,Object> map2=new HashMap<>();
                                if(shortage==null){
                                    map2.put("needCount","");
                                    map2.put("stock","");
                                    map2.put("planCount","");
                                    map2.put("planState","");
                                }else {
                                    map2.put("needCount",shortage.getNeedcount());
                                    map2.put("stock",shortage.getStock());
                                    //运行中的计划记录
                                    PlanCache planCache=planCacheMapper.selectByGoodidAndDate(good.getId(),date);
                                    if(planCache==null){
                                        //如果没有运行中的计划，看是否有完成的计划
                                        PlanRecorde planRecorde= planRecordMapper.selectByGoodidAndDate(good.getId(),date);
                                        if(planRecorde==null){
                                            map2.put("planCount","");
                                            map2.put("planState","");
                                        }else {
                                            map2.put("planCount","("+planRecorde.getType()+")"+planRecorde.getCount()+" "+planRecorde.getBoxcount()+"箱");
                                            map2.put("planState","完成");
                                        }
                                    }else {
                                        map2.put("planCount","("+planCache.getType()+")"+planCache.getCount()+" "+planCache.getBoxcount()+"箱");
                                        map2.put("planState",planCache.getState());
                                    }
                                }
                                shortageList.add(map2);
                            }
                            map1.put("shortageList",shortageList);
                            resultList.add(map1);
                        }
                        map.put("list",resultList);
                        map.put("dateList",dateList);
                        return ResultUtil.success(map);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    return ResultUtil.error(1,"结束日期、开始日期的值错误");
                }
            }
        }
        return ResultUtil.success();
    }

    /**
     * 缺件报表模板下载
     * @param response
     */
    @Override
    public void modelDownload(HttpServletResponse response) {
        //创建Excel工作簿对象,此处选择SXSSFWorkbook,创建的excel以.xlsx结尾，支持2007、2010及以后版本
        SXSSFWorkbook wb = new SXSSFWorkbook();
        //创建表
        Sheet sheet = wb.createSheet();
        //给sheet设置名称
        wb.setSheetName(0,"sheet1");
        //创建标题行,标题行为第一行，第一行下标为0
        Row titleRow = sheet.createRow(0);
        //给标题行的单元格设置样式和字体
        CellStyle titleCellStyle = wb.createCellStyle();//单元格样式
        titleCellStyle.setAlignment(CellStyle.ALIGN_CENTER);//水平居中
        titleCellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);//垂直居中
        Font font = wb.createFont();//字体
        font.setFontName("宋体");//设置为宋体
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);//加粗
        //把字体样式绑定到标题单元格样式中
        titleCellStyle.setFont(font);
        //给标题行创建列，第一列下标为0.给列添加内容和格式
        Cell goodCodeCell = titleRow.createCell(0);
        goodCodeCell.setCellValue("物料编号");
        goodCodeCell.setCellStyle(titleCellStyle);
        Cell goodNameCell = titleRow.createCell(1);
        goodNameCell.setCellValue("物料名称");
        goodNameCell.setCellStyle(titleCellStyle);
        Cell supplierCodeCell=titleRow.createCell(2);
        supplierCodeCell.setCellValue("供应商编号");
        supplierCodeCell.setCellStyle(titleCellStyle);
        Cell supplierNameCell = titleRow.createCell(3);
        supplierNameCell.setCellValue("供应商名称");
        supplierNameCell.setCellStyle(titleCellStyle);
        Cell yestodayCell = titleRow.createCell(4);
        yestodayCell.setCellValue("前日结存");
        yestodayCell.setCellStyle(titleCellStyle);
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("M月d日");
        Date now=new Date();
        Cell boxCodeCell = titleRow.createCell(5);
        boxCodeCell.setCellValue(simpleDateFormat.format(now)+"需求");
        boxCodeCell.setCellStyle(titleCellStyle);
        Cell countCell = titleRow.createCell(6);
        countCell.setCellValue(simpleDateFormat.format(now)+"结存");
        countCell.setCellStyle(titleCellStyle);

        //创建主体内容的单元格样式和字体
        CellStyle bodyCellStyle = wb.createCellStyle();
        bodyCellStyle.setAlignment(CellStyle.ALIGN_CENTER);//左右居中
        bodyCellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);//上下居中
        Font bodyFont = wb.createFont();//创建字体
        bodyFont.setBoldweight(Font.BOLDWEIGHT_NORMAL);//不加粗
        bodyFont.setFontName("宋体");//设置字体
        bodyFont.setColor(Font.COLOR_RED);//字体颜色为红色
        bodyCellStyle.setFont(bodyFont);//把字体放入单元格样式

        //设置响应内容编码
        response.setCharacterEncoding("UTF-8");
        //设置响应的内容格式
        response.setContentType("aplication/x-download");
        //设置文件名称
        String fileName = "缺件报表模板.xlsx";
        //对文件名进行编码格式设置，防止中文乱码
        try {
            fileName = URLEncoder.encode(fileName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //设置响应头
        response.addHeader("Content-Disposition", "attachment;filename=" + fileName);
        try {
            //得到输出流
            OutputStream os = response.getOutputStream();
            //把Excel通过输出流写出
            wb.write(os);
            //关闭流
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询当天到最大日期的日期集合.默认为5天
     * @return
     */
    @Override
    public Result shortageDateList() {
        //日期格式化
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
        //当前日期
        Date now=new Date();
        //今天的日期
        String today=simpleDateFormat.format(now);
        //查询从今天开始(包含今天)的根据日期分组并以日期升序排序的集合
        List<Shortage> shortageList=shortageMapper.selectGreatTodayDateList();
        if(shortageList.isEmpty()){
            //如果没有日期集合，就默认从今天开始共计5天
            Calendar calendar=Calendar.getInstance();
            calendar.setTime(now);
            //日期+5天
            calendar.add(Calendar.DATE,5);
            return ResultUtil.success(DateUtil.getBetweenDate(simpleDateFormat.format(now),simpleDateFormat.format(calendar.getTime())));
        }
        return ResultUtil.success(DateUtil.getBetweenDate(simpleDateFormat.format(now),shortageList.get(shortageList.size()-1).getDate()));
    }

}
