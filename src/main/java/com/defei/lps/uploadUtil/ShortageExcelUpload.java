package com.defei.lps.uploadUtil;

import com.defei.lps.dao.FactoryMapper;
import com.defei.lps.dao.GoodMapper;
import com.defei.lps.dao.ShortageMapper;
import com.defei.lps.dao.SupplierMapper;
import com.defei.lps.entity.Factory;
import com.defei.lps.entity.Good;
import com.defei.lps.entity.Shortage;
import com.defei.lps.entity.Supplier;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * 缺件报表上传。
 *使用EXCEL进行上传，对每个记录进行验证合法性
 * @author 高德飞
 */
@Component//加上此注解，让spring容器启动时就加载该类
public class ShortageExcelUpload {
    @Autowired
    private SupplierMapper supplierMapper;
    @Autowired
    private FactoryMapper factoryMapper;
    @Autowired
    private ShortageMapper shortageMapper;
    @Autowired
    private GoodMapper goodMapper;

    //静态初始化本类
    private static ShortageExcelUpload shortageExcelUpload;
    //在方法上加上注解@PostConstruct,这样方法就会在bean初始化之后被spring容器执行
    @PostConstruct
    public void init(){
        //声明的静态类=this
        shortageExcelUpload=this;
    }

    private String message;

    //获取错误信息
    public String getMessage() {
        return message;
    }

    /**
     * 读取EXCEL内容
     *
     * @param file 传入的文件
     * @return Shortage集合
     */
    public List<Shortage> getList(MultipartFile file, int factoryId) {
        //验证工厂是否存在
        Factory factory=shortageExcelUpload.factoryMapper.selectByPrimaryKey(factoryId);
        if(factory==null){
            message="工厂不存在，刷新页面后重试";
            return null;
        }
    	//定义集合
        List<Shortage> list = new ArrayList<>();
        //获取文件流
        InputStream is = null;
		try {
            is = file.getInputStream();
            //获取工作簿
            Workbook wb=null;
			//获取文件名
	        String fileName = file.getOriginalFilename();
			if (fileName.matches("^.+\\.(?i)(xls)$")) {
	        	wb = new HSSFWorkbook(is);
	        }else if (fileName.matches("^.+\\.(?i)(xlsx)$")) {
	        	wb = new XSSFWorkbook(is);
	        }else {
                message="不是excel文件";
                list=null;
                return list;
            }

            //得到第一个shell
            Sheet sheet = wb.getSheetAt(0);
            //得到EXCEL数据的总行数
            int totalRows = sheet.getPhysicalNumberOfRows();
            //获取第一行表头
            Row headRow=sheet.getRow(0);
            //从表头中找到需要的参数所在的列下标
            //1.物料编号
            int goodCodeIndex=-1;
            //2.供应商编号
            int supplierCodeIndex=-1;
            //3.当天日期的需求列
            int todayNeedIndex=-1;
            //4.当天日期的结存列
            int todayStockIndex=-1;
            //5.昨天结存列
            int yestodayStockIndex=-1;
            //验证1：检查是否有这4个基本列
            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("M月d日");
            Date now=new Date();
            String today=simpleDateFormat.format(now);
            for(int g=0;g<headRow.getPhysicalNumberOfCells();g++){
                if(headRow.getCell(g).getStringCellValue().equals("物料编号")){
                    goodCodeIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("供应商编号")){
                    supplierCodeIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals(today+"需求")){
                    todayNeedIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals(today+"结存")){
                    todayStockIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("前日结存")){
                    yestodayStockIndex=g;
                }
            }
            if(goodCodeIndex==-1){
                message="EXCEL中没有“物料编号”列";
                return null;
            }else if(supplierCodeIndex==-1){
                message="EXCEL中没有“供应商编号”列";
                return null;
            }else if(todayNeedIndex==-1){
                message="EXCEL中没有“"+today+"需求”列";
                return null;
            }else if(todayStockIndex==-1){
                message="EXCEL中没有“"+today+"结存”列";
                return null;
            }else if(yestodayStockIndex==-1){
                message="EXCEL中没有“前日结存”列";
                return null;
            }
            //验证2：看是否有排产计划列
            if(todayStockIndex>=headRow.getPhysicalNumberOfCells()){
                message="EXCEL中没有未来几天的排产计划";
                return null;
            }
            //验证3：看后面的每个日期的列表表头是否正确。看当天结存后面的日期列表
            List<String> dateList=new ArrayList<>();
            String headResult="";
            for(int r=todayStockIndex+1;r<headRow.getPhysicalNumberOfCells();r++){
                if(headRow.getCell(r).getCellType()==Cell.CELL_TYPE_STRING){
                    String head=headRow.getCell(r).getStringCellValue().replace(" ","");
                    if(!head.equals("")){
                        if(head.contains("日")){
                            if(!dateList.contains(head.split("日")[0])){
                                dateList.add(head.split("日")[0]);
                            }
                        }else {
                            headResult+=";EXCEL中第"+(r+1)+"列表头不是正确的内容";
                        }
                    }else {
                        headResult+=";EXCEL中第"+(r+1)+"列表头不能填入空格";
                    }
                }else if(headRow.getCell(r).getCellType()!=Cell.CELL_TYPE_BLANK){
                    headResult+=";EXCEL中第"+(r+1)+"列表头必须为文本格式";
                }
            }
            if(!headResult.equals("")){
                message=headResult.substring(1);
                return null;
            }
            //如果后面的日期列表为空，那么返回提示
            if(dateList.isEmpty()){
                message="EXCEL中没有未来几天的排产计划";
                return null;
            }
            //检验4：每个日期的需求列和结存列是否存在
            List<Map<String,Object>> dateList1=new ArrayList<>();
            for(String date:dateList){
                Map<String,Object> map=new HashMap<>();
                map.put("date",date);
                int needIndex=0;//当前循环日期的需求列的下标
                int stockIndex=0;//当前循环日期的结存列的下标
                int needTimes=0;//当前循环日期的需求列的出现的次数
                int stockTimes=0;//当前循环日期的结存列出现的次数
                for(int r=todayStockIndex+1;r<headRow.getPhysicalNumberOfCells();r++){
                    if(headRow.getCell(r).getStringCellValue().replace(" ","").equals(date+"日需求")){
                        needTimes++;
                        needIndex=r;
                    }
                    if(headRow.getCell(r).getStringCellValue().replace(" ","").equals(date+"日结存")){
                        stockTimes++;
                        stockIndex=r;
                    }
                }
                if(needTimes==0){
                    headResult+=";EXCEL中“"+date+"日需求”列不存在";
                }else if(needTimes>1){
                    headResult+=";EXCEL中“"+date+"日需求”列过多";
                }else {
                    //保存当前循环日期的需求列的下标
                    map.put("needIndex",needIndex);
                }
                if(stockTimes==0){
                    headResult+=";EXCEL中“"+date+"日结存”列不存在";
                }else if(stockTimes>1){
                    headResult+=";EXCEL“中"+date+"日结存”列过多";
                }else {
                    //保存当前循环日期的结存列的下标
                    map.put("stockIndex",stockIndex);
                }
                dateList1.add(map);
            }
            if(!headResult.equals("")){
                message=headResult.substring(1);
                return null;
            }
            //表头验证完毕，开始取值
            if(totalRows>=2) {
                //格式化数字，保留所有整数
                DecimalFormat df = new DecimalFormat("#");
                SimpleDateFormat simpleDateFormat1=new SimpleDateFormat("yyyy-MM-dd");
                String todayDate=simpleDateFormat1.format(now);
                String result="";
                //循环表格，每行循环，取出每行中每列的数据，放入物料信息集合
                for (int i = 1; i < totalRows; i++) {
                    //获取行
                    Row row=sheet.getRow(i);
                    //行不为空时才读取该行数据
                    if(row!=null){
                        //先判断这一行所有列都是否为空
                        boolean allIsNull=true;
                        for(int g=0;g<row.getPhysicalNumberOfCells();g++){
                            if(row.getCell(g).getCellType()==Cell.CELL_TYPE_NUMERIC){
                                allIsNull=false;
                                break;
                            }else if(row.getCell(g).getCellType()==Cell.CELL_TYPE_STRING){
                                if(!row.getCell(g).getStringCellValue().replace(" ","").equals("")){
                                    allIsNull=false;
                                    break;
                                }
                            }else if(row.getCell(g).getCellType()!=Cell.CELL_TYPE_BLANK){
                                allIsNull=false;
                                break;
                            }
                        }
                        if(allIsNull==false){
                            //NumberFormat nf = NumberFormat.getInstance();
                            //如果一旦行中某一个个列有值，那么就需要获取当前行的数据
                            //1.物料编号
                            String goodCode="";
                            Cell goodCodeCell=row.getCell(goodCodeIndex);
                            if(goodCodeCell!=null) {
                                if (goodCodeCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                    //获取单元格内容,并去掉空格
                                    goodCode = goodCodeCell.getStringCellValue().replace("\\s", "");
                                } else if(goodCodeCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){
                                    result+=";第"+(i+1)+"行，“物料编号”不能为空";
                                    continue;
                                }else if(goodCodeCell.getCellType()==HSSFCell.CELL_TYPE_NUMERIC){
                                    goodCode=df.format(goodCodeCell.getNumericCellValue()).replace(",","");
                                }else {
                                    result+=";第"+(i+1)+"行，“物料编号”必须为文本或常规格式";
                                    continue;
                                }
                            }
                            //2.供应商编号
                            String supplierCode="";
                            Cell supplierCodeCell=row.getCell(supplierCodeIndex);
                            if(supplierCodeCell!=null) {
                                if (supplierCodeCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                    //获取单元格内容,并去掉空格
                                    supplierCode = supplierCodeCell.getStringCellValue().replace(" ", "");
                                } else if(supplierCodeCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){
                                    result+=";第"+(i+1)+"行，“供应商编号”不能为空";
                                    continue;
                                }else if(supplierCodeCell.getCellType()==HSSFCell.CELL_TYPE_NUMERIC){
                                    supplierCode=df.format(supplierCodeCell.getNumericCellValue()).replace(",","");
                                }else {
                                    result+=";第"+(i+1)+"行，“供应商编号”必须为文本或常规格式";
                                    continue;
                                }
                            }
                            //验证供应商是否存在
                            Supplier supplier=shortageExcelUpload.supplierMapper.selectBySuppliercodeAndFactoryid(supplierCode,factoryId);
                            if(supplier==null){
                                //如果供应商不存在，说明不是本系统涉及到的供应商，直接跳过读取下一行
                                continue;
                            }
                            //验证供应商的物料是否存在
                            Good good=shortageExcelUpload.goodMapper.selectByGoodcodeAndSupplierid(goodCode,supplier.getId());
                            if(good==null){
                                result+=";第"+(i+1)+"行供应商"+supplierCode+"的物料"+goodCode+"不存在，请核对";
                                continue;
                            }

                            //3.当天需求
                            String todayNeed="";
                            Cell todayNeedCell=row.getCell(todayNeedIndex);
                            if(todayNeedCell!=null) {
                                if (todayNeedCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                    //获取单元格内容,并去掉空格
                                    todayNeed = todayNeedCell.getStringCellValue().replace(" ", "");
                                    if(!todayNeed.matches("^[0-9]{1,11}$")){
                                        result+=";第"+(i+1)+"行，“"+today+"需求”必须填入数字";
                                        continue;
                                    }
                                } else if(todayNeedCell.getCellType()==HSSFCell.CELL_TYPE_NUMERIC){
                                    todayNeed=String.valueOf(df.format(todayNeedCell.getNumericCellValue()));
                                }else {
                                    result+=";第"+(i+1)+"行，“"+today+"需求”必须填入数字";
                                    continue;
                                }
                            }
                            //4.当天结存
                            String todayStock="";
                            Cell todayStockCell=row.getCell(todayStockIndex);
                            if(todayStockCell!=null) {
                                if (todayStockCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                    //获取单元格内容,并去掉空格
                                    todayStock = todayStockCell.getStringCellValue().replace(" ", "");
                                    if(!todayStock.matches("^[+-]{0,1}[0-9]{1,10}$")){
                                        result+=";第"+(i+1)+"行，“"+today+"结存”必须填入数字";
                                        continue;
                                    }
                                } else if(todayStockCell.getCellType()==HSSFCell.CELL_TYPE_NUMERIC){
                                    todayStock=String.valueOf(df.format(todayStockCell.getNumericCellValue()));
                                }else {
                                    result+=";第"+(i+1)+"行，“"+today+"结存”必须填入数字";
                                    continue;
                                }
                            }
                            //5.前一天结存
                            String yestodayStock="";
                            Cell yestodayStockCell=row.getCell(yestodayStockIndex);
                            if(yestodayStockCell!=null) {
                                if (yestodayStockCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                    //获取单元格内容,并去掉空格
                                    yestodayStock = yestodayStockCell.getStringCellValue().replace(" ", "");
                                    if(!yestodayStock.matches("^[+-]{0,1}[0-9]{1,10}$")){
                                        result+=";第"+(i+1)+"行，“前日结存”必须填入数字";
                                        continue;
                                    }
                                } else if(yestodayStockCell.getCellType()==HSSFCell.CELL_TYPE_NUMERIC){
                                    yestodayStock=String.valueOf(df.format(yestodayStockCell.getNumericCellValue()));
                                }else {
                                    result+=";第"+(i+1)+"行，“前日结存”必须填入数字";
                                    continue;
                                }
                            }
                            //保存当天的记录
                            Shortage shortage=new Shortage();
                            shortage.setGood(good);
                            shortage.setNeedcount(Integer.parseInt(todayNeed));
                            shortage.setLastneedcount(Integer.parseInt(todayNeed));
                            shortage.setStock(Integer.parseInt(todayStock));
                            shortage.setLaststock(Integer.parseInt(todayStock));
                            shortage.setDate(todayDate);
                            list.add(shortage);
                            //保存前一天记录
                            //获取前一天的日期.如果系统是首次使用，那么必然没有前一天的缺件报表记录，就无需添加前一天的缺件报表记录
                            List<Shortage> shortageList=shortageExcelUpload.shortageMapper.selectByGoodidAndDatestartAndDateend(good.getId(),"",todayDate);
                            if(!shortageList.isEmpty()){
                                //去掉结束日期的记录
                                shortageList.remove(shortageList.size()-1);
                                if(!shortageList.isEmpty()){
                                    //如果去掉结束日期的记录后，集合仍然有值，那么最后一个记录的日期就是前一天的日期
                                    Shortage shortage2=new Shortage();
                                    shortage2.setId(shortageList.get(shortageList.size()-1).getId());
                                    shortage2.setStock(Integer.parseInt(yestodayStock));
                                    list.add(shortage2);
                                }
                            }
                            //保存后面几天的需求结存记录
                            for(Map<String,Object> map:dateList1){
                                //结存
                                String stock="";
                                Cell stockCell=row.getCell((int) map.get("stockIndex"));
                                if(stockCell!=null){
                                    if (stockCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                        //获取单元格内容,并去掉空格
                                        if(stockCell.getStringCellValue().replace(" ", "").matches("^[+-]{0,1}[0-9]{1,10}$")){
                                            stock=stockCell.getStringCellValue().replace(" ", "");
                                        }else {
                                            result+=";第"+(i+1)+"行，“"+map.get("date")+"日结存”必须填入数字";
                                            continue;
                                        }
                                    } else if(stockCell.getCellType()==HSSFCell.CELL_TYPE_NUMERIC){
                                        stock=df.format(stockCell.getNumericCellValue());
                                    }else {
                                        result+=";第"+(i+1)+"行，“"+map.get("date")+"日结存”必须填入数字";
                                        continue;
                                    }
                                }else {
                                    result+=";第"+(i+1)+"行，“"+map.get("date")+"日结存”必须填入数字";
                                    continue;
                                }
                                //需求
                                String need="";
                                Cell needCell=row.getCell((int) map.get("needIndex"));
                                if(needCell!=null){
                                    if (needCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                        //获取单元格内容,并去掉空格
                                        if(needCell.getStringCellValue().replace(" ", "").matches("^[0-9]{1,11}$")){
                                            need=needCell.getStringCellValue().replace(" ", "");
                                        }else{
                                            result+=";第"+(i+1)+"行，“"+map.get("date")+"日需求”必须填入数字";
                                            continue;
                                        }
                                    } else if(needCell.getCellType()==HSSFCell.CELL_TYPE_NUMERIC){
                                        need=df.format(needCell.getNumericCellValue());
                                    }else {
                                        result+=";第"+(i+1)+"行，“"+map.get("date")+"日需求”必须填入数字";
                                        continue;
                                    }
                                }else {
                                    result+=";第"+(i+1)+"行，“"+map.get("date")+"日需求”必须填入数字";
                                    continue;
                                }
                                //只有日期对应的需求为数字、结存为数字才能保存缺件记录
                                if(!stock.equals("")&&!need.equals("")){
                                    //日期
                                    String date=(String)map.get("date");
                                    String month=date.split("月")[0];
                                    if(month.length()==1){
                                        month="0"+month;
                                    }
                                    String day=date.split("月")[1];
                                    if(day.length()==1){
                                        day="0"+day;
                                    }
                                    date=todayDate.substring(0,5)+month+"-"+day;
                                    if(Integer.parseInt(month)<Integer.parseInt(todayDate.split("-")[1])){
                                        date=(Integer.parseInt(todayDate.split("-")[0])+1)+"-"+month+"-"+day;
                                    }
                                    Shortage shortage1=new Shortage();
                                    shortage1.setGood(good);
                                    shortage1.setNeedcount(Integer.parseInt(need));
                                    shortage1.setLastneedcount(Integer.parseInt(need));
                                    shortage1.setStock(Integer.parseInt(stock));
                                    shortage1.setLaststock(Integer.parseInt(stock));
                                    shortage1.setDate(date);
                                    list.add(shortage1);
                                }
                            }//循环保存后几天的缺件记录完毕
                        }//行中只要有一个单元格不为空则需要读取单元格内容完毕
                    }//行不为空判断
                }//行循环结束
                if(!result.equals("")) {
                    message=result.substring(1);
                    list=null;
                    return list;
                }else {
                    //没有提示情况下，如果集合仍为空，那么说明没有数据
                    if(list.isEmpty()) {
                        message="EXCEL中没有需要的数据";
                        list=null;
                        return list;
                    }
                }
                return list;
            }else {
                message="EXCEL中没有数据";
                list=null;
                return list;
            }
		} catch (Exception e) {
			e.printStackTrace();
			message="文件读取错误";
			list=null;
			return list;
		}finally {
            if(is!=null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    message="关闭文件流错误";
                    list=null;
                    return list;
                }
            }
        }
    }
}
