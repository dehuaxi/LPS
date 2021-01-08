package com.defei.lps.uploadUtil;

import com.defei.lps.dao.AreaMapper;
import com.defei.lps.dao.FactoryMapper;
import com.defei.lps.dao.WarehouseMapper;
import com.defei.lps.entity.Area;
import com.defei.lps.entity.Factory;
import com.defei.lps.entity.Route;
import com.defei.lps.entity.Warehouse;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *使用EXCEL进行上传，对每个记录进行验证合法性
 * @author 高德飞
 */
@Component//加上此注解，让spring容器启动时就加载该类
public class RouteExcelUpload {
    @Autowired
    private FactoryMapper factoryMapper;
    @Autowired
    private AreaMapper areaMapper;
    @Autowired
    private WarehouseMapper warehouseMapper;

    //静态初始化本类
    private static RouteExcelUpload routeExcelUpload;
    //在方法上加上注解@PostConstruct,这样方法就会在bean初始化之后被spring容器执行
    @PostConstruct
    public void init(){
        //声明的静态类=this
        routeExcelUpload=this;
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
     * @return 未检查排序单集合
     */
    public List<Map<String,Object>> getList(MultipartFile file) {
    	//定义集合
        List<Map<String,Object>> list = new ArrayList<>();
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
            //1.线路名称
            int routeNameIndex=-1;
            //2.线路编号
            int routeNumberIndex=-1;
            //3.描述
            int describesIndex=-1;
            //4.出发区域
            int areaNameIndex=-1;
            //5.终点工厂
            int factoryNameIndex=-1;
            //6.途径中转仓
            int warehouseIndex=-1;

            for(int g=0;g<headRow.getPhysicalNumberOfCells();g++){
                if(headRow.getCell(g).getStringCellValue().equals("线路名称")){
                    routeNameIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("线路编号")){
                    routeNumberIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("描述")){
                    describesIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("出发区域")){
                    areaNameIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("终点工厂")){
                    factoryNameIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("途径中转仓")){
                    warehouseIndex=g;
                }
            }
            if(routeNameIndex==-1){
                message="EXCEL中没有“线路名称”列";
                return null;
            }else if(routeNumberIndex==-1){
                message="EXCEL中没有“线路编号”列";
                return null;
            }else if(describesIndex==-1){
                message="EXCEL中没有“描述”列";
                return null;
            }else if(areaNameIndex==-1){
                message="EXCEL中没有“出发区域”列";
                return null;
            }else if(factoryNameIndex==-1){
                message="EXCEL中没有“终点工厂”列";
                return null;
            }else if(warehouseIndex==-1){
                message="EXCEL中没有“途径中转仓”列";
                return null;
            }
            if(totalRows>=2) {
                String result="";
                //循环表格，每行循环，取出每行中每列的数据，放入物料信息集合
                for (int i = 1; i < totalRows; i++) {
                    //获取行
                    Row row=sheet.getRow(i);
                    //行不为空时才读取该行数据
                    if(row!=null){
                        boolean allisRight=true;//默认所有列的值都合法
                        //1.名称
                        String routeName="";
                        Cell routeNameCell=row.getCell(routeNameIndex);
                        if(routeNameCell!=null) {
                            if (routeNameCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                routeName = routeNameCell.getStringCellValue().replace(" ", "");
                            } else if(routeNameCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){ }else {
                                result+=";第"+(i+1)+"行，“线路名称”必须为文本格式";
                                allisRight=false;
                            }
                        }

                        //2.线路编号
                        String routeNumber="";
                        Cell routeNumberCell=row.getCell(routeNumberIndex);
                        if(routeNumberCell!=null) {
                            if (routeNumberCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                routeNumber = routeNumberCell.getStringCellValue().replace(" ", "");
                            } else if(routeNumberCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){ }else {
                                result+=";第"+(i+1)+"行，“线路编号”必须为文本格式";
                                allisRight=false;
                            }
                        }

                        //3.描述
                        String describes="";
                        Cell describesCell=row.getCell(describesIndex);
                        if(describesCell!=null) {
                            if (describesCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                describes = describesCell.getStringCellValue().replace(" ", "");
                            } else if(describesCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){ }else {
                                result+=";第"+(i+1)+"行，“描述”必须为文本格式";
                                allisRight=false;
                            }
                        }

                        //4.工厂名称
                        String factoryName="";
                        Cell factoryNameCell=row.getCell(factoryNameIndex);
                        if(factoryNameCell!=null) {
                            if (factoryNameCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                factoryName = factoryNameCell.getStringCellValue().replace(" ", "");
                            } else if(factoryNameCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){ }else {
                                result+=";第"+(i+1)+"行，“终点工厂”必须为文本格式";
                                allisRight=false;
                            }
                        }

                        //5.区域名称
                        String areaName="";
                        Cell areaNameNameCell=row.getCell(areaNameIndex);
                        if(areaNameNameCell!=null) {
                            if (areaNameNameCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                areaName = areaNameNameCell.getStringCellValue().replace(" ", "");
                            } else if(areaNameNameCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){ }else {
                                result+=";第"+(i+1)+"行，“出发区域”必须为文本格式且不能为空";
                                allisRight=false;
                            }
                        }

                        //6.途径中转仓
                        String warehouses="";
                        Cell warehousesCell=row.getCell(warehouseIndex);
                        if(warehousesCell!=null) {
                            if (warehousesCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                warehouses = warehousesCell.getStringCellValue().replace(" ", "");
                            } else if(warehousesCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){ }else {
                                result+=";第"+(i+1)+"行，“途径中转仓”必须为文本格式";
                                allisRight=false;
                            }
                        }

                        //所有的单元格都为空，则跳过该行
                        if(routeName.equals("")&&routeNumber.equals("")&&describes.equals("")&&areaName.equals("")&&factoryName.equals("")&&warehouses.equals("")){
                            continue;
                        }else {
                            //只要一行中有一个字段不为空，就要验证所有字段的值的合法性
                            //线路名称
                            if(!routeName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5@#_-]{1,20}$")){
                                result+=";第"+(i+1)+"行，“线路名称”必须为1-20位的数字、大小写字母、汉字、特殊字符(@#_-)";
                                allisRight=false;
                            }
                            //线路编号
                            if(!routeNumber.matches("^[0-9A-Za-z@#_-]{1,9}$")){
                                result+=";第"+(i+1)+"行，“线路编号”只能是1-9位的数字、大小写字母、特殊字符(@#_-)";
                                allisRight=false;
                            }
                            //描述
                            if(!describes.matches("^[0-9A-Za-z\\u4e00-\\u9fa5@#_-]{0,50}$")){
                                result+=";第"+(i+1)+"行，“描述”必须为1-50位的数字、大小写字母、汉字、特殊字符(@#_-)";
                                allisRight=false;
                            }
                            //验证工厂是否存在
                            Factory factory=routeExcelUpload.factoryMapper.selectByFactoryname(factoryName);
                            if(factory==null){
                                result+=";第"+(i+1)+"行，“终点工厂”的值在系统中找不到";
                                allisRight=false;
                            }
                            //验证区域是否存在
                            Area area=routeExcelUpload.areaMapper.selectByAreaname(areaName);
                            if(area==null){
                                result+=";第"+(i+1)+"行，“出发区域”的值在系统中找不到";
                                allisRight=false;
                            }
                            //验证中转仓是否存在
                            List<Warehouse> warehouseList=new ArrayList<>();
                            if(!warehouses.equals("")){
                                if(warehouses.contains(",")){
                                    String[] warehouseNameList=warehouses.split(",");
                                    String noexitName="";
                                    for(String name:warehouseNameList){
                                        if(!name.equals("")){
                                            Warehouse warehouse=routeExcelUpload.warehouseMapper.selectByWarehousename(name);
                                            if(warehouse==null){
                                                noexitName+="、"+name;
                                            }else {
                                                warehouseList.add(warehouse);
                                            }
                                        }
                                    }
                                    if(!noexitName.equals("")){
                                        //不存在的中转仓名称集合不为空，那么就表示有中转仓不存在，返回提示
                                        result+=";第"+(i+1)+"行，“途径中转仓”的值中以下中转仓在系统中找不到："+noexitName.substring(1);
                                        allisRight=false;
                                    }
                                }else {
                                    Warehouse warehouse=routeExcelUpload.warehouseMapper.selectByWarehousename(warehouses);
                                    if(warehouse==null){
                                        result+=";第"+(i+1)+"行，“途径中转仓”的值在系统中找不到";
                                        allisRight=false;
                                    }else {
                                        warehouseList.add(warehouse);
                                    }
                                }
                            }
                            //所有都有值，则放入集合
                            if(allisRight){
                                Map<String,Object> map=new HashMap<>();
                                //线路信息
                                Route route=new Route();
                                route.setRoutename(routeName);
                                route.setRoutenumber(routeNumber);
                                route.setDescribes(describes);
                                route.setArea(area);
                                route.setFactory(factory);
                                map.put("route",route);
                                //线路对应的中转仓信息
                                map.put("warehouse",warehouseList);
                                list.add(map);
                            }
                        }//行内所有列的值都不为空判断结束
                    }//行不为空判断
                }//行循环结束
                if(!result.equals("")) {
                    message=result.substring(1);
                    return null;
                }else {
                    //没有提示情况下，如果集合仍为空，那么说明没有数据
                    if(list.isEmpty()) {
                        message="EXCEL中没有需要的数据";
                        return null;
                    }
                }
            }else {
                list=null;
                message="EXCEL中没有数据";
            }
            //返回供应商对象集合
            return list;
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
