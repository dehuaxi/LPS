package com.defei.lps.util;

import com.defei.lps.dao.PlanCacheMapper;
import com.defei.lps.dao.ShortageMapper;
import com.defei.lps.entity.Good;
import com.defei.lps.entity.PlanCache;
import com.defei.lps.entity.Shortage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 专门计算物料的缺件计划的类
 * @author 高德飞
 * @create 2021-01-13 16:20
 */
@Component//加上此注解，让spring容器启动时就加载该类
public class PlanCacheCreateUtil {
    @Autowired
    private PlanCacheMapper planCacheMapper;
    @Autowired
    private ShortageMapper shortageMapper;

    //静态初始化本类
    private static PlanCacheCreateUtil planCacheCreateUtil;
    //在方法上加上注解@PostConstruct,这样方法就会在bean初始化之后被spring容器执行

    @PostConstruct
    public void init(){
        //声明的静态类=this
        planCacheCreateUtil=this;
    }

    /**
     * 创建缺件计划
     */
    public synchronized static void createPlanCacheList(List<Good> goodList){
        List<PlanCache> newPlan=new ArrayList<>();
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat simpleDateFormat1=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //当前日期
        Date now=new Date();
        //今天的日期
        String today=simpleDateFormat.format(now);
        for(Good good:goodList){
            //删除所有的该物料的未确认的计划
            planCacheCreateUtil.planCacheMapper.deleteByGoodidAndState(good.getId(),"未确认");
            //验证：如果该物料上传的缺件报表记录中从今天开始未来几天的所有缺件需求和结存值都是0，则该物料不计算缺件计划
            List<Shortage> shortages=planCacheCreateUtil.shortageMapper.selectByGoodidAndDatestartAndDateend(good.getId(),today,"");
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
                List<PlanCache> planCacheList=planCacheCreateUtil.planCacheMapper.selectByGoodidAndReceivedate(shortages.get(i).getGood().getId(),shortages.get(i).getDate());
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
                    if(shortages.get(i).getStock()<=good.getTriggerstock()){
                        //结存小于等于了物料拉动库存，生成缺件计划
                        PlanCache planCache=new PlanCache();
                        planCache.setGood(good);
                        //先计算最少送货数量：从到货日期后运输周期内天数的需求数量之和，再加上物料最小拉动库存
                        int minCount=0;
                        for(int r=0;r<transitDay;r++){
                            //如果下标还在缺件报表集合内，那么就把需求加起来
                            if((i+r)<shortages.size()){
                                minCount+=shortages.get(i+r).getNeedcount();
                            }
                        }
                        //加上物料最小拉动库存
                        minCount+=good.getTriggerstock();
                        //修正，箱数向上取整
                        if(minCount%good.getOneboxcount()!=0){
                            minCount=(minCount/good.getOneboxcount()+1)*good.getOneboxcount();
                        }
                        //最大取货数量
                        int maxCount=0;
                        //获取前一天的到货后结存
                        int yestodayPlanStock=shortages.get(i-1).getStock();
                        if(yestodayPlanStock<0){
                            yestodayPlanStock=0;
                        }
                        if((minCount+yestodayPlanStock+shortages.get(i-1).getNeedcount())<good.getMaxstock()){
                            maxCount=good.getMaxstock()-shortages.get(i-1).getNeedcount()-yestodayPlanStock;
                            //箱数向下取整
                            if(maxCount%good.getOneboxcount()!=0){
                                maxCount=(maxCount/good.getOneboxcount()-1)*good.getOneboxcount();
                            }
                        }else {
                            maxCount=minCount;
                        }
                        planCache.setCount(maxCount);
                        planCache.setMaxcount(maxCount);
                        planCache.setMincount(minCount);
                        planCache.setSurecount(0);
                        planCache.setTakecount(0);
                        planCache.setReceivecount(0);
                        planCache.setBoxcount(maxCount/good.getOneboxcount());
                        //当前日期的前一天是到后日期，在从到货日期往回推运输周期天
                        planCache.setDate(shortages.get((i-1-transitDay)).getDate());
                        planCache.setReceivedate(shortages.get(i-1).getDate());
                        planCache.setState("未确认");
                        planCache.setType("系统");
                        planCache.setUrgent("正常");
                        planCache.setRemarks("");
                        planCache.setCreatetime(now);
                        //看是否有发货日期、到货日期一样的在途缺件计划
                        PlanCache oldPlanCache=planCacheCreateUtil.planCacheMapper.selectByGoodidAndDateAndReceivedate(good.getId(),shortages.get((i-1-transitDay)).getDate(),shortages.get(i-1).getDate());
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
                                planCacheCreateUtil.planCacheMapper.updateByPrimaryKeySelective(oldPlanCache);
                            }
                        }else {
                            newPlan.add(planCache);
                        }
                        //生成缺件计划后，再拿生成的缺件计划的最大数量更新后面的缺件报表集合的结存，看后面更新后的结存还有没有小于物料最小拉动库存的
                        //那么第二个计划的最小取货数量就是到货日期之后所有天数的需求数之和
                        //初始结存=昨天的结存+第一个计划的最大到后数量
                        int startCount=maxCount+yestodayPlanStock;
                        for(int g=i;g<shortages.size();g++){
                            int currentStock=startCount-shortages.get(g).getNeedcount();
                            shortages.get(g).setStock(currentStock);
                            startCount=currentStock;
                        }
                        //用更新结存后的集合查看结存是否小于物料拉动库存
                        for(int g=i;g<shortages.size();g++){
                            if(shortages.get(g).getStock()<=good.getTriggerstock()){
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
                                //加上物料最小拉动库存
                                minCount2+=good.getTriggerstock();
                                //修正，箱数向上取整
                                if(minCount2%good.getOneboxcount()!=0){
                                    minCount2=(minCount2/good.getOneboxcount()+1)*good.getOneboxcount();
                                }
                                //最大取货数量
                                int maxCount2=0;
                                //获取前一天的到货后结存
                                int yestodayPlanStock2=shortages.get(g-1).getStock();
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
                                planCache2.setDate(shortages.get((g-1-transitDay)).getDate());
                                planCache2.setReceivedate(shortages.get(g-1).getDate());
                                planCache2.setState("未确认");
                                planCache2.setType("系统");
                                planCache2.setUrgent("正常");
                                planCache2.setRemarks("");
                                planCache2.setCreatetime(now);
                                //看是否有发货日期、到货日期一样的在途缺件计划
                                PlanCache oldPlanCache2=planCacheCreateUtil.planCacheMapper.selectByGoodidAndDateAndReceivedate(good.getId(),shortages.get((i-1-transitDay)).getDate(),shortages.get(i-1).getDate());
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
                                        planCacheCreateUtil.planCacheMapper.updateByPrimaryKeySelective(oldPlanCache2);
                                    }
                                }else {
                                    newPlan.add(planCache2);
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
                    //如果当天结存>物料最小拉动库存,继续循环
                    continue;
                } else if (new BigDecimal(shortageList.get(i).getStock()).compareTo(new BigDecimal(good.getTriggerstock())) == 0) {
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
                    //如果当天结存<物料最小拉动库存，看是否已经有到货日期，如果没有，那么前一天就是到货日期
                    if (receiveDate.equals("")) {
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
                    //如果当天结存<物料最小拉动库存,再看当天结存是否<当天的需求，如果是，则需求总数的累加值=当天需求数量，否则需求总数的累加值=物料最小拉动库存-当天结存
                    if (new BigDecimal(shortageList.get(i).getStock()).compareTo(new BigDecimal(shortageList.get(i).getNeedcount())) != 1) {
                        //当天结存<=当天需求，需求总数的累加值=当天需求数量
                        totalNeed += shortageList.get(i).getNeedcount();
                    } else {
                        //当天结存>当天需求，需求总数的累加值=物料最小拉动库存-当天结存
                        totalNeed += good.getTriggerstock() - shortageList.get(i).getStock();
                    }
                }
            }
            //如果有紧急计划的取货数量，说明有紧急计划
            if(totalNeed>0){
                //取货数量向上取整
                //修正，箱数向上取整
                if(totalNeed%good.getOneboxcount()!=0){
                    totalNeed=(totalNeed/good.getOneboxcount()+1)*good.getOneboxcount();
                }
                //生成紧急计划，如果有发货日期、收货日期一样的在途缺件计划，就合并
                PlanCache oldPlanCache=planCacheCreateUtil.planCacheMapper.selectByGoodidAndDateAndReceivedate(good.getId(),sendDate,receiveDate);
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
                        planCacheCreateUtil.planCacheMapper.updateByPrimaryKeySelective(oldPlanCache);
                    }
                }else {
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
            planCacheCreateUtil.planCacheMapper.insertBatch(newPlan);
        }
    }

    /**
     * 修改单个物料对应的缺件计划
     */
    public synchronized static void createPlanCache(Good good) {
        List<PlanCache> newPlan = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //当前日期
        Date now = new Date();
        //今天的日期
        String today = simpleDateFormat.format(now);
        //删除所有的该物料的未确认的计划
        planCacheCreateUtil.planCacheMapper.deleteByGoodidAndState(good.getId(), "未确认");
        //验证：如果该物料上传的缺件报表记录中从今天开始未来几天的所有缺件需求和结存值都是0，则该物料不计算缺件计划
        List<Shortage> shortages = planCacheCreateUtil.shortageMapper.selectByGoodidAndDatestartAndDateend(good.getId(), today, "");
        //是否能创建缺件计划，默认位否
        boolean canCreate = false;
        //判断每一天的需求数量是否为0，如果每天的需求数量都为0，那么无需计算缺件计划
        for (Shortage shortage1 : shortages) {
            if (shortage1.getNeedcount() != 0) {
                //需求数量不为0，跳出循环，需要创建计划
                canCreate = true;
            }
        }
        if (canCreate == false) {
            today=null;
            now=null;
            newPlan=null;
            simpleDateFormat=null;
            simpleDateFormat1=null;
            //无需创建缺件计划,设置集合为空
            shortages = null;
        }else {
            //获取当前循环物料的配送周期天数
            int transitDay = new BigDecimal(good.getSupplier().getTransitday()).intValue();
            if (good.getSupplier().getTransitday().contains(".")) {
                //如果配送天数值含有.号，就说明是有小数的，那么统一给配送周期向上取整
                transitDay++;
            }
            //（一）：先把从今天开始的缺件报表集合，查询每天是否有到货的在途缺件计划。如果有就把到货数量更新到缺件报表集合的结存中，
            //更新方式：到货当日结存+到货数量就是到货当日的新结存，然后用新结存去减去后面每天的需求，得到每日的新结存
            for (int i = 0; i < shortages.size(); i++) {
                //获取每日的是否有到货的在途计划
                List<PlanCache> planCacheList = planCacheCreateUtil.planCacheMapper.selectByGoodidAndReceivedate(shortages.get(i).getGood().getId(), shortages.get(i).getDate());
                if (!planCacheList.isEmpty()) {
                    //更新结存1：先看到货在途缺件计划集合中是否有特急的计划，如果有，就先用特急的计划修改缺件报表集合的结存：到货当天的新结存=当天结存+到货数量
                    int nextCount = 0;//非特急在途缺件计划的数量
                    for (PlanCache planCache : planCacheList) {
                        if (planCache.getUrgent().equals("特急")) {
                            //到货当日的新结存
                            int newStock = planCache.getCount() + shortages.get(i).getStock();
                            //修改到货当日的结存
                            shortages.get(i).setStock(newStock);
                            //更新以后每天的结存
                            for (int k = 1; k <= (shortages.size() - 1 - i); k++) {
                                //修改结存
                                shortages.get(i + k).setStock(newStock - shortages.get(i + k).getNeedcount());
                                //更新初始结存
                                newStock = newStock - shortages.get(i + k).getNeedcount();
                            }
                        } else {
                            nextCount++;
                        }
                    }
                    //更新结存2：更新非特急的在途缺件计划到货后得结存
                    if (nextCount > 0) {
                        //当特急在途缺件计划更新缺件报表集合的结存完毕后，再用非特急的在途缺件计划更新缺件报表集合的结存：如果到货当天结存为负数，那么到货当天的结存不变更，用到货数量做初始结存更新到货第二天及以后的每日结存
                        for (PlanCache planCache : planCacheList) {
                            if (!planCache.getUrgent().equals("特急")) {
                                //更新结存的初始结存值。如果到货当天结存<0，则初始结存值=到货数量,且不需要修改到货当天的结存
                                int newStock = planCache.getCount();
                                if (shortages.get(i).getStock() > 0) {
                                    //如果到货当天结存>0，则初始结存值=到货数量+到货当天结存
                                    newStock = shortages.get(i).getStock() + planCache.getCount();
                                }
                                //更新以后每天的结存
                                for (int k = 1; k <= (shortages.size() - 1 - i); k++) {
                                    //修改结存
                                    shortages.get(i + k).setStock(newStock - shortages.get(i + k).getNeedcount());
                                    //更新初始结存
                                    newStock = newStock - shortages.get(i + k).getNeedcount();
                                }
                            }
                        }
                    }
                }
            }
            //（二）：用更新完后的缺件报表集合来判断是否生成缺件计划。
            //第1：先判断是否有第一个正常缺件计划。第一个正常缺件计划标准：以上传的第二天为发货日期，往后推运输周期天数得到到货日期，看到货日期后一天的结存是否小于等于物料拉动库存
            //自定义：该物料第一个正常计划的应该到货日期在缺件集合中的下标.默认为运输周期天数+1
            int index = transitDay + 1;
            //如果第一个正常计划的到货日期第二天的下标不超过缺件集合最后一个记录的下标，那么就可以创建第一个正常计划
            if ((transitDay + 2) <= (shortages.size() - 1)) {
                //自定义：看第一个正常计划到货后的第二天的结存是否小于拉动库存，如果小于拉动库存就产生第一个正常计划，如果不小就继续往后看结存是否小于最小拉动库存
                for (int i = (transitDay + 2); i < shortages.size(); i++) {
                    if (shortages.get(i).getStock() <= good.getTriggerstock()) {
                        //结存小于等于了物料拉动库存，生成缺件计划
                        PlanCache planCache = new PlanCache();
                        planCache.setGood(good);
                        //先计算最少送货数量：从到货日期后运输周期内天数的需求数量之和，再加上物料最小拉动库存
                        int minCount = 0;
                        for (int r = 0; r < transitDay; r++) {
                            //如果下标还在缺件报表集合内，那么就把需求加起来
                            if ((i + r) < shortages.size()) {
                                minCount += shortages.get(i + r).getNeedcount();
                            }
                        }
                        //加上物料最小拉动库存
                        minCount += good.getTriggerstock();
                        //修正，箱数向上取整
                        if (minCount % good.getOneboxcount() != 0) {
                            minCount = (minCount / good.getOneboxcount() + 1) * good.getOneboxcount();
                        }
                        //最大取货数量
                        int maxCount = 0;
                        //获取前一天的到货后结存
                        int yestodayPlanStock = shortages.get(i - 1).getStock();
                        if (yestodayPlanStock < 0) {
                            yestodayPlanStock = 0;
                        }
                        if ((minCount + yestodayPlanStock + shortages.get(i - 1).getNeedcount()) < good.getMaxstock()) {
                            maxCount = good.getMaxstock() - shortages.get(i - 1).getNeedcount() - yestodayPlanStock;
                            //箱数向下取整
                            if (maxCount % good.getOneboxcount() != 0) {
                                maxCount = (maxCount / good.getOneboxcount() - 1) * good.getOneboxcount();
                            }
                        } else {
                            maxCount = minCount;
                        }
                        planCache.setCount(maxCount);
                        planCache.setMaxcount(maxCount);
                        planCache.setMincount(minCount);
                        planCache.setSurecount(0);
                        planCache.setTakecount(0);
                        planCache.setReceivecount(0);
                        planCache.setBoxcount(maxCount / good.getOneboxcount());
                        //当前日期的前一天是到后日期，在从到货日期往回推运输周期天
                        planCache.setDate(shortages.get((i - 1 - transitDay)).getDate());
                        planCache.setReceivedate(shortages.get(i - 1).getDate());
                        planCache.setState("未确认");
                        planCache.setType("系统");
                        planCache.setUrgent("正常");
                        planCache.setRemarks("");
                        planCache.setCreatetime(now);
                        //看是否有发货日期、到货日期一样的在途缺件计划
                        PlanCache oldPlanCache = planCacheCreateUtil.planCacheMapper.selectByGoodidAndDateAndReceivedate(good.getId(), shortages.get((i - 1 - transitDay)).getDate(), shortages.get(i - 1).getDate());
                        if (oldPlanCache != null) {
                            planCache = null;
                            if (oldPlanCache.getMincount() < minCount) {
                                StringBuffer a = new StringBuffer("最小数量由");
                                a.append(oldPlanCache.getMincount());
                                a.append("变为");
                                a.append(minCount);
                                //如果原计划最小数量<了新计划的最小数量，那么修改原计划的最小数量
                                oldPlanCache.setMincount(minCount);
                                if (minCount >= oldPlanCache.getMaxcount()) {
                                    a.append(";最大数量由");
                                    a.append(oldPlanCache.getMaxcount());
                                    a.append("变为");
                                    a.append(maxCount);
                                    oldPlanCache.setMaxcount(maxCount);
                                }
                                oldPlanCache.setRemarks(a.toString());
                                planCacheCreateUtil.planCacheMapper.updateByPrimaryKeySelective(oldPlanCache);
                            }
                        } else {
                            newPlan.add(planCache);
                        }
                        //生成缺件计划后，再拿生成的缺件计划的最大数量更新后面的缺件报表集合的结存，看后面更新后的结存还有没有小于物料最小拉动库存的
                        //那么第二个计划的最小取货数量就是到货日期之后所有天数的需求数之和
                        //初始结存=昨天的结存+第一个计划的最大到后数量
                        int startCount = maxCount + yestodayPlanStock;
                        for (int g = i; g < shortages.size(); g++) {
                            int currentStock = startCount - shortages.get(g).getNeedcount();
                            shortages.get(g).setStock(currentStock);
                            startCount = currentStock;
                        }
                        //用更新结存后的集合查看结存是否小于物料拉动库存
                        for (int g = i; g < shortages.size(); g++) {
                            if (shortages.get(g).getStock() <= good.getTriggerstock()) {
                                //生成第二个正常缺件计划，其最小取货数量就是到货后所有天的需求之和，再加上物料最小拉动库存
                                PlanCache planCache2 = new PlanCache();
                                planCache2.setGood(good);
                                //先计算最少送货数量：到货后所有天的需求之和，再加上物料最小拉动库存
                                int minCount2 = 0;
                                for (int r = 0; r < (shortages.size() - g); r++) {
                                    //如果下标还在缺件报表集合内，那么就把需求加起来
                                    if ((g + r) < shortages.size()) {
                                        minCount2 += shortages.get(g + r).getNeedcount();
                                    }
                                }
                                //加上物料最小拉动库存
                                minCount2 += good.getTriggerstock();
                                //修正，箱数向上取整
                                if (minCount2 % good.getOneboxcount() != 0) {
                                    minCount2 = (minCount2 / good.getOneboxcount() + 1) * good.getOneboxcount();
                                }
                                //最大取货数量
                                int maxCount2 = 0;
                                //获取前一天的到货后结存
                                int yestodayPlanStock2 = shortages.get(g - 1).getStock();
                                if (yestodayPlanStock2 < 0) {
                                    yestodayPlanStock2 = 0;
                                }
                                if ((minCount2 + yestodayPlanStock2 + shortages.get(g - 1).getNeedcount()) < good.getMaxstock()) {
                                    maxCount2 = good.getMaxstock() - shortages.get(g - 1).getNeedcount() - yestodayPlanStock2;
                                    //箱数向下取整
                                    if (maxCount2 % good.getOneboxcount() != 0) {
                                        maxCount2 = (maxCount2 / good.getOneboxcount() - 1) * good.getOneboxcount();
                                    }
                                } else {
                                    maxCount2 = minCount2;
                                }
                                planCache2.setCount(maxCount2);
                                planCache2.setMaxcount(maxCount2);
                                planCache2.setMincount(minCount2);
                                planCache2.setSurecount(0);
                                planCache2.setTakecount(0);
                                planCache2.setReceivecount(0);
                                planCache2.setBoxcount(maxCount2 / good.getOneboxcount());
                                //当前日期的前一天是到后日期，在从到货日期往回推运输周期天
                                planCache2.setDate(shortages.get((g - 1 - transitDay)).getDate());
                                planCache2.setReceivedate(shortages.get(g - 1).getDate());
                                planCache2.setState("未确认");
                                planCache2.setType("系统");
                                planCache2.setUrgent("正常");
                                planCache2.setRemarks("");
                                planCache2.setCreatetime(now);
                                //看是否有发货日期、到货日期一样的在途缺件计划
                                PlanCache oldPlanCache2 = planCacheCreateUtil.planCacheMapper.selectByGoodidAndDateAndReceivedate(good.getId(), shortages.get((i - 1 - transitDay)).getDate(), shortages.get(i - 1).getDate());
                                if (oldPlanCache2 != null) {
                                    planCache2 = null;
                                    if (oldPlanCache2.getMincount() < minCount2) {
                                        StringBuffer a = new StringBuffer("最小数量由");
                                        a.append(oldPlanCache2.getMincount());
                                        a.append("变为");
                                        a.append(minCount2);
                                        //如果原计划最小数量<了新计划的最小数量，那么修改原计划的最小数量
                                        oldPlanCache2.setMincount(minCount2);
                                        if (minCount2 >= oldPlanCache2.getMaxcount()) {
                                            a.append(";最大数量由");
                                            a.append(oldPlanCache2.getMaxcount());
                                            a.append("变为");
                                            a.append(maxCount2);
                                            oldPlanCache2.setMaxcount(maxCount2);
                                        }
                                        oldPlanCache2.setRemarks(a.toString());
                                        planCacheCreateUtil.planCacheMapper.updateByPrimaryKeySelective(oldPlanCache2);
                                    }
                                } else {
                                    newPlan.add(planCache2);
                                }
                            }
                        }
                    }
                }
            } else {
                //无需生成第一个正常缺件计划，那么说明第一个正常缺件计划的到货日期等于或大于了缺件报表中最大日期,也就是说整个缺件报表集合都在第一个正常缺件计划到货日期之内，那么需要看这个集合内每天的结存是否有小于物料最小拉动库存的
                index = shortages.size() - 1;
            }
            //循环今天到第一个正常计划的到货日期下标，看是否有紧急计划
            List<Shortage> shortageList = new ArrayList<>();
            for (int i = 0; i <= index; i++) {
                shortageList.add(shortages.get(i));
            }
            //循环需要查看是否生成紧急计划的缺件报表集合shortageList
            //紧急计划总的需求数量
            int totalNeed = 0;
            //紧急计划的到货日期
            String receiveDate = "";
            //紧急计划的发货日期
            String sendDate = "";
            //紧急程度
            String urgent = "紧急";
            for (int i = 0; i < shortageList.size(); i++) {
                if (new BigDecimal(shortageList.get(i).getStock()).compareTo(new BigDecimal(good.getTriggerstock())) == 1) {
                    //如果当天结存>物料最小拉动库存,继续循环
                    continue;
                } else if (new BigDecimal(shortageList.get(i).getStock()).compareTo(new BigDecimal(good.getTriggerstock())) == 0) {
                    //如果当天结存==物料最小拉动库存，看是否已经有到货日期，如果没有，那么当天就是到货日期
                    if (receiveDate.equals("")) {
                        receiveDate = shortageList.get(i).getDate();
                        //计算发货日期
                        if ((i - transitDay) <= 0) {
                            sendDate = shortageList.get(0).getDate();
                            urgent = "特急";
                        } else {
                            sendDate = shortageList.get((i - transitDay)).getDate();
                        }
                    }
                } else if (new BigDecimal(shortageList.get(i).getStock()).compareTo(new BigDecimal(good.getTriggerstock())) == -1) {
                    //如果当天结存<物料最小拉动库存，看是否已经有到货日期，如果没有，那么前一天就是到货日期
                    if (receiveDate.equals("")) {
                        receiveDate = shortageList.get(i).getDate();
                        //计算发货日期
                        if ((i - transitDay) <= 0) {
                            sendDate = shortageList.get(0).getDate();
                            urgent = "特急";
                        } else {
                            sendDate = shortageList.get((i - transitDay)).getDate();
                        }
                    }
                    //如果当天结存<物料最小拉动库存,再看当天结存是否<当天的需求，如果是，则需求总数的累加值=当天需求数量，否则需求总数的累加值=物料最小拉动库存-当天结存
                    if (new BigDecimal(shortageList.get(i).getStock()).compareTo(new BigDecimal(shortageList.get(i).getNeedcount())) != 1) {
                        //当天结存<=当天需求，需求总数的累加值=当天需求数量
                        totalNeed += shortageList.get(i).getNeedcount();
                    } else {
                        //当天结存>当天需求，需求总数的累加值=物料最小拉动库存-当天结存
                        totalNeed += good.getTriggerstock() - shortageList.get(i).getStock();
                    }
                }
            }
            //如果有紧急计划的取货数量，说明有紧急计划
            if (totalNeed > 0) {
                //取货数量向上取整
                //修正，箱数向上取整
                if (totalNeed % good.getOneboxcount() != 0) {
                    totalNeed = (totalNeed / good.getOneboxcount() + 1) * good.getOneboxcount();
                }
                //生成紧急计划，如果有发货日期、收货日期一样的在途缺件计划，就合并
                PlanCache oldPlanCache = planCacheCreateUtil.planCacheMapper.selectByGoodidAndDateAndReceivedate(good.getId(), sendDate, receiveDate);
                if (oldPlanCache != null) {
                    //看数量是否一致，如果一样，就不保存
                    if (oldPlanCache.getCount() < totalNeed) {
                        {
                            //原计划数量小于了当前生成的数量，增加备注，修改数量
                            int oldCount = oldPlanCache.getCount();
                            oldPlanCache.setCount(totalNeed);
                            oldPlanCache.setMincount(totalNeed);
                            oldPlanCache.setMaxcount(totalNeed);
                            oldPlanCache.setRemarks("于" + simpleDateFormat1.format(now) + "更新计划，从原数量" + oldCount + "增加至" + totalNeed + "，请尽快补足差额" + (totalNeed - oldCount));
                            planCacheCreateUtil.planCacheMapper.updateByPrimaryKeySelective(oldPlanCache);
                        }
                    } else {
                        PlanCache planCache = new PlanCache();
                        planCache.setGood(good);
                        planCache.setCount(totalNeed);
                        planCache.setMaxcount(totalNeed);
                        planCache.setMincount(totalNeed);
                        planCache.setSurecount(0);
                        planCache.setTakecount(0);
                        planCache.setReceivecount(0);
                        planCache.setBoxcount(totalNeed / good.getOneboxcount());
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
                //批量添加新计划
                if(!newPlan.isEmpty()){
                    planCacheCreateUtil.planCacheMapper.insertBatch(newPlan);
                }
            }
        }
    }
}
