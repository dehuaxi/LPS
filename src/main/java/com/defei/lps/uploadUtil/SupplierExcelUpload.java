package com.defei.lps.uploadUtil;

import com.defei.lps.dao.AreaMapper;
import com.defei.lps.dao.FactoryMapper;
import com.defei.lps.dao.RouteMapper;
import com.defei.lps.dao.SupplierMapper;
import com.defei.lps.entity.Area;
import com.defei.lps.entity.Factory;
import com.defei.lps.entity.Route;
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
import java.util.ArrayList;
import java.util.List;


/**
 *使用EXCEL进行上传，对每个记录进行验证合法性
 * @author 高德飞
 */
@Component//加上此注解，让spring容器启动时就加载该类
public class SupplierExcelUpload {
    @Autowired
    private SupplierMapper supplierMapper;
    @Autowired
    private FactoryMapper factoryMapper;
    @Autowired
    private AreaMapper areaMapper;
    @Autowired
    private RouteMapper routeMapper;

    //静态初始化本类
    private static SupplierExcelUpload supplierExcelUpload;
    //在方法上加上注解@PostConstruct,这样方法就会在bean初始化之后被spring容器执行
    @PostConstruct
    public void init(){
        //声明的静态类=this
        supplierExcelUpload=this;
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
    public List<Supplier> getList(MultipartFile file) {
    	//定义集合
        List<Supplier> list = new ArrayList<>();
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
            //1.供应商编号
            int supplierCodeIndex=-1;
            //2.供应商名称
            int supplierNameIndex=-1;
            //3.联系人
            int contactIndex=-1;
            //4.电话
            int phoneIndex=-1;
            //5.省
            int provinceIndex=-1;
            //6.市
            int cityIndex=-1;
            //区县
            int districtIndex=-1;
            //地址
            int addressIndex=-1;
            //所属工厂
            int factoryNameIndex=-1;
            //所属区域名称
            int areaNameIndex=-1;
            //经度
            int longitudeIndex=-1;
            //纬度
            int latitudeIndex=-1;
            for(int g=0;g<headRow.getPhysicalNumberOfCells();g++){
                if(headRow.getCell(g).getStringCellValue().equals("供应商编号")){
                    supplierCodeIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("供应商名称")){
                    supplierNameIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("联系人")){
                    contactIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("电话")){
                    phoneIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("省")){
                    provinceIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("市")){
                    cityIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("区(县)")){
                    districtIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("详细地址")){
                    addressIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("经度")){
                    longitudeIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("纬度")){
                    latitudeIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("所属区域")){
                    areaNameIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("所属工厂")){
                    factoryNameIndex=g;
                }
            }
            if(supplierCodeIndex==-1){
                message="EXCEL中没有“供应商编号”列";
                return null;
            }else if(supplierNameIndex==-1){
                message="EXCEL中没有“供应商名称”列";
                return null;
            }else if(contactIndex==-1){
                message="EXCEL中没有“联系人”列";
                return null;
            }else if(phoneIndex==-1){
                message="EXCEL中没有“电话”列";
                return null;
            }else if(provinceIndex==-1){
                message="EXCEL中没有“省”列";
                return null;
            }else if(cityIndex==-1){
                message="EXCEL中没有“市”列";
                return null;
            }else if(districtIndex==-1){
                message="EXCEL中没有“区(县)”列";
                return null;
            }else if(addressIndex==-1){
                message="EXCEL中没有“详细地址”列";
                return null;
            }else if(longitudeIndex==-1){
                message="EXCEL中没有“经度”列";
                return null;
            }else if(latitudeIndex==-1){
                message="EXCEL中没有“纬度”列";
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
                        //1.供应商编号
                        String supplierCode="";
                        Cell supplierCodeCell=row.getCell(supplierCodeIndex);
                        if(supplierCodeCell!=null) {
                            if (supplierCodeCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                supplierCode = supplierCodeCell.getStringCellValue().replace("\\s", "");
                            } else if(supplierCodeCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){ }else {
                                result+=";第"+(i+1)+"行，“供应商编号”必须为文本格式";
                                allisRight=false;
                            }
                        }

                        //2.供应商名称
                        String supplierName="";
                        Cell supplierNameCell=row.getCell(supplierNameIndex);
                        if(supplierNameCell!=null) {
                            if (supplierNameCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                supplierName = supplierNameCell.getStringCellValue().replace(" ", "");
                            } else if(supplierNameCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){ }else {
                                result+=";第"+(i+1)+"行，“供应商名称”必须为文本格式";
                                allisRight=false;
                            }
                        }

                        //3.联系人
                        String contact="";
                        Cell contactCell=row.getCell(contactIndex);
                        if(contactCell!=null) {
                            if (contactCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                contact = contactCell.getStringCellValue().replace(" ", "");
                            } else if(contactCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){ }else {
                                result+=";第"+(i+1)+"行，“联系人”必须为文本格式";
                                allisRight=false;
                            }
                        }

                        //4.电话
                        String phone="";
                        Cell phoneCell=row.getCell(phoneIndex);
                        if(phoneCell!=null) {
                            if (phoneCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                phone = phoneCell.getStringCellValue().replace(" ", "");
                            } else if(phoneCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){ }else {
                                result+=";第"+(i+1)+"行，“电话”必须为文本格式";
                                allisRight=false;
                            }
                        }

                        //5.省
                        String province="";
                        Cell provinceCell=row.getCell(provinceIndex);
                        if(provinceCell!=null) {
                            if (provinceCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                province = provinceCell.getStringCellValue().replace(" ", "");
                            } else if(provinceCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){ }else {
                                result+=";第"+(i+1)+"行，“省”必须为文本格式";
                                allisRight=false;
                            }
                        }

                        //6.市
                        String city="";
                        Cell cityCell=row.getCell(cityIndex);
                        if(cityCell!=null) {
                            if (cityCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                city = cityCell.getStringCellValue().replace(" ", "");
                            } else if(cityCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){ }else {
                                result+=";第"+(i+1)+"行，“市”必须为文本格式";
                                allisRight=false;
                            }
                        }

                        //7.区县
                        String district="";
                        Cell districtCell=row.getCell(districtIndex);
                        if(districtCell!=null) {
                            if (districtCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                district = districtCell.getStringCellValue().replace(" ", "");
                            } else if(districtCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){ }else {
                                result+=";第"+(i+1)+"行，“区(县)”必须为文本格式";
                                allisRight=false;
                            }
                        }

                        //8.详细地址
                        String address="";
                        Cell addressCell=row.getCell(addressIndex);
                        if(addressCell!=null) {
                            if (addressCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                address = addressCell.getStringCellValue().replace(" ", "");
                            } else if(addressCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){ }else {
                                result+=";第"+(i+1)+"行，“详细地址”必须为文本格式";
                                allisRight=false;
                            }
                        }

                        //9.工厂名称
                        String factoryName="";
                        Cell factoryNameCell=row.getCell(factoryNameIndex);
                        if(factoryNameCell!=null) {
                            if (factoryNameCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                factoryName = factoryNameCell.getStringCellValue().replace(" ", "");
                            } else if(factoryNameCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){ }else {
                                result+=";第"+(i+1)+"行，“所属工厂”必须为文本格式";
                                allisRight=false;
                            }
                        }

                        //10.区域名称
                        String areaName="";
                        Cell areaNameNameCell=row.getCell(areaNameIndex);
                        if(areaNameNameCell!=null) {
                            if (areaNameNameCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                areaName = areaNameNameCell.getStringCellValue().replace(" ", "");
                            } else if(areaNameNameCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){ }else {
                                result+=";第"+(i+1)+"行，“所属区域”必须为文本格式";
                                allisRight=false;
                            }
                        }

                        //11.经度
                        String longitude="";
                        Cell longitudeCell=row.getCell(longitudeIndex);
                        if(longitudeCell!=null) {
                            if (longitudeCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                longitude = longitudeCell.getStringCellValue().replace(" ", "");
                            } else if(longitudeCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){ }else  {
                                result+=";第"+(i+1)+"行，“经度”必须为文本格式";
                                allisRight=false;
                            }
                        }

                        //12.纬度
                        String latitude="";
                        Cell latitudeCell=row.getCell(latitudeIndex);
                        if(latitudeCell!=null) {
                            if (latitudeCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                latitude = latitudeCell.getStringCellValue().replace(" ", "");
                            } else if(latitudeCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){ }else  {
                                result+=";第"+(i+1)+"行，“纬度”必须为文本格式";
                                allisRight=false;
                            }
                        }

                        if(supplierCode.equals("")&&supplierName.equals("")&&contact.equals("")&&phone.equals("")&&province.equals("")&&city.equals("")&&district.equals("")&&address.equals("")&&factoryName.equals("")&&areaName.equals("")&&longitude.equals("")&&latitude.equals("")){
                            continue;
                        }else {
                            //供应商编号
                            if(!supplierCode.matches("^[0-9A-Z]{1,6}$")){
                                result+=";第"+(i+1)+"行，“供应商编号”必须为1-6位的数字、大写字母";
                                allisRight=false;
                            }
                            //供应商名称
                            if(!supplierName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_*()（）-]{1,50}$")){
                                result+=";第"+(i+1)+"行，“供应商名称”只能是1-50位的数字、大小写字母、汉字、特殊字符(@#_*()（）-)";
                                allisRight=false;
                            }
                            //联系人
                            if(!contact.matches("^[a-zA-Z\\u4e00-\\u9fa5]{0,10}$")){
                                result+=";第"+(i+1)+"行，“联系人”必须为1-10位的汉字、大小写字母";
                                allisRight=false;
                            }
                            //电话
                            if(!phone.matches("^[0-9-]{0,15}$")){
                                result+=";第"+(i+1)+"行，“电话”必须为手机号或者是包含区号的座机号";
                                allisRight=false;
                            }
                            //省
                            if(!province.matches("^[\\u4e00-\\u9fa5]{1,10}$")){
                                result+=";第"+(i+1)+"行，“电话”必须为1-10位的汉字";
                                allisRight=false;
                            }
                            //市
                            if(!city.matches("^[\\u4e00-\\u9fa5]{1,10}$")){
                                result+=";第"+(i+1)+"行，“市”必须为1-10位的汉字";
                                allisRight=false;
                            }
                            //区县
                            if(!district.matches("^[\\u4e00-\\u9fa5]{1,10}$")){
                                result+=";第"+(i+1)+"行，“区(县)”必须为1-10位的汉字";
                                allisRight=false;
                            }
                            //详细地址
                            if(!address.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_-]{1,100}$")){
                                result+=";第"+(i+1)+"行，“详细地址”必须为1-100位的数字、大小写字母、汉字、特殊字符(@#_-)";
                                allisRight=false;
                            }
                            //验证区域是否存在
                            Area area=supplierExcelUpload.areaMapper.selectByAreaname(areaName);
                            if(area==null){
                                result+=";第"+(i+1)+"行，“所属区域”的值在系统中找不到";
                                allisRight=false;
                            }
                            //验证工厂是否存在
                            Factory factory=supplierExcelUpload.factoryMapper.selectByFactoryname(factoryName);
                            if(factory==null){
                                result+=";第"+(i+1)+"行，“所属工厂”的值在系统中找不到";
                                allisRight=false;
                            }
                            //获取线路
                            Route route=supplierExcelUpload.routeMapper.selectByFactoryidAndAreaid(factory.getId(),area.getId());
                            if(route==null){
                                result+=";第"+(i+1)+"行，“所属区域”和“所属工厂”没有生成线路";
                                allisRight=false;
                            }
                            //经度
                            if(!longitude.equals("")){
                                if(!longitude.matches("^(([\\+ \\-]?([1-9]{1}[0-9]{0,2})|([0]{1})))([.]\\d{0,6})?$")){
                                    result+=";第"+(i+1)+"行，“经度”必须为-180到180之间的最多保留6位小数的数字";
                                    allisRight=false;
                                }
                            }
                            //纬度
                            if(!latitude.equals("")){
                                if(!latitude.matches("^(([\\+ \\-]?([1-9]{1}[0-9]{0,2})|([0]{1})))([.]\\d{0,6})?$")){
                                    result+=";第"+(i+1)+"行，“纬度”必须为-180到180之间的最多保留6位小数的数字";
                                    allisRight=false;
                                }
                            }
                            //所有都有值，则放入集合
                            if(allisRight){
                                Supplier supplier=new Supplier();
                                supplier.setSuppliercode(supplierCode);
                                supplier.setSuppliername(supplierName);
                                supplier.setContact(contact);
                                supplier.setPhone(phone);
                                supplier.setProvince(province);
                                supplier.setCity(city);
                                supplier.setDistrict(district);
                                supplier.setAddress(address);
                                supplier.setRoute(route);
                                supplier.setLongitude(longitude);
                                supplier.setLatitude(latitude);
                                list.add(supplier);
                            }
                        }
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
