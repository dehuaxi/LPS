package com.defei.lps.serviceImp;

import com.defei.lps.dao.*;
import com.defei.lps.entity.*;
import com.defei.lps.result.Result;
import com.defei.lps.result.ResultUtil;
import com.defei.lps.service.SupplierService;
import com.defei.lps.uploadUtil.SupplierExcelUpload;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SupplierServiceImp implements SupplierService {
    @Autowired
    private SupplierMapper supplierMapper;
    @Autowired
    private FactoryMapper factoryMapper;
    @Autowired
    private AreaMapper areaMapper;
    @Autowired
    private GoodMapper goodMapper;
    @Autowired
    private RouteMapper routeMapper;


    /**
     * 添加供应商
     * @param supplierCode
     * @param supplierName
     * @param contact
     * @param phone
     * @param province
     * @param city
     * @param district
     * @param address
     * @param areaId 区域id
     * @param factoryId 工厂id
     * @return
     */
    @Override
    public Result add(String supplierCode, String supplierName,String abbreviation, String contact, String phone, String province, String city, String district, String address,int areaId,int factoryId,String longitude,String latitude,String transitDay) {
        //参数检查
        if(!supplierCode.matches("^[0-9A-Z]{1,6}$")){
            return ResultUtil.error(1,"供应商编号只能是1-6位的数字、大写字母");
        }else if(!supplierName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_*()。.（）-]{1,50}$")){
            return ResultUtil.error(1,"供应商名称只能是1-50位的数字、大小写字母、汉字、特殊字符(@#_*()。.（）-)");
        }else if(!abbreviation.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_*()。.（）-]{1,10}$")){
            return ResultUtil.error(1,"供应商简称只能是1-10位的数字、大小写字母、汉字、特殊字符(@#_*()。.（）-)");
        }else if(!contact.matches("^[a-zA-Z\\u4e00-\\u9fa5]{0,10}$")){
            return ResultUtil.error(1,"联系人为1-10位的汉字、大小写字母");
        }else if(!phone.matches("^[0-9-]{0,15}$")){
            return ResultUtil.error(1,"电话必须为手机号或者是包含区号的座机号");
        }else if(!province.matches("^[\\u4e00-\\u9fa5]{1,10}$")){
            return ResultUtil.error(1,"省为1-10位的汉字");
        }else if(!city.matches("^[\\u4e00-\\u9fa5]{1,10}$")){
            return ResultUtil.error(1,"市为1-10位的汉字");
        }else if(!district.matches("^[\\u4e00-\\u9fa5]{1,10}$")){
            return ResultUtil.error(1,"区县为1-10位的汉字");
        }else if(!address.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_*-]{1,100}$")){
            return ResultUtil.error(1,"区县只能是1-100位的数字、大小写字母、汉字、特殊字符(@#_*-)");
        }else if(!longitude.matches("^(([\\+ \\-]?([1-9]{1}[0-9]{0,2})|([0]{1})))([.]\\d{0,6})?$")){
            return ResultUtil.error(1,"经度取值范围为-180到180之间的最多保留6位小数的数字");
        }else if(!latitude.matches("^(([\\+ \\-]?([1-9]{1}[0-9]{0,2})|([0]{1})))([.]\\d{0,6})?$")){
            return ResultUtil.error(1,"纬度取值范围为-180到180之间的最多保留6位小数的数字");
        }else if(!transitDay.matches("^\\d{1,2}(.[5]{1})?$")){
            return ResultUtil.error(1,"运输周期是0-100之间的最多保留1位小数的数字,且小数后只能是5");
        }else {
            int b=Double.valueOf(transitDay).intValue();
            if(b<0){
                return ResultUtil.error(1,"运输周期不可<0");
            }else if(b>=100){
                return ResultUtil.error(1,"运输周期不可>=100");
            }
        }
        //验证区域是否存在
        Area area=areaMapper.selectByPrimaryKey(areaId);
        if(area==null){
            return ResultUtil.error(1,"选择的区域不存在，刷新页面后重试");
        }
        //验证工厂是否存在
        Factory factory=factoryMapper.selectByPrimaryKey(factoryId);
        if(factory==null){
            return ResultUtil.error(1,"选择的工厂不存在，刷新页面后重试");
        }
        Route route=routeMapper.selectByFactoryidAndAreaid(factoryId,areaId);
        if(route==null){
            return ResultUtil.error(1,"所选区域和工厂没有生成线路，请生成线路后在试");
        }
        //添加
        Supplier supplier=new Supplier();
        supplier.setSuppliername(supplierName);
        supplier.setSuppliercode(supplierCode);
        supplier.setAbbreviation(abbreviation);
        supplier.setContact(contact);
        supplier.setPhone(phone);
        supplier.setProvince(province);
        supplier.setCity(city);
        supplier.setDistrict(district);
        supplier.setAddress(address);
        supplier.setLongitude(longitude);
        supplier.setLatitude(latitude);
        supplier.setRoute(route);
        supplier.setTransitday(transitDay);
        supplierMapper.insertSelective(supplier);
        return ResultUtil.success();
    }

    /**
     * 批量上传添加，工厂id+供应商编号的组合是重复的，则重复的修改,不重复的添加
     * @param excelFile
     * @return
     */
    @Override
    public Result upload(MultipartFile excelFile) {
        SupplierExcelUpload se=new SupplierExcelUpload();
        List<Supplier> supplierList= se.getList(excelFile);
        if(supplierList==null){
            return ResultUtil.error(1,se.getMessage());
        }
        int count=0;
        int updateCount=0;
        for(Supplier supplier:supplierList){
            Supplier supplier1=supplierMapper.selectBySuppliercodeAndFactoryid(supplier.getSuppliercode(),supplier.getRoute().getFactory().getId());
            if(supplier1==null){
                supplierMapper.insertSelective(supplier);
                count++;
            }else {
                supplier1.setSuppliercode(supplier.getSuppliercode());
                supplier1.setSuppliername(supplier.getSuppliername());
                supplier1.setContact(supplier.getContact());
                supplier1.setPhone(supplier.getPhone());
                supplier1.setProvince(supplier.getProvince());
                supplier1.setCity(supplier.getCity());
                supplier1.setDistrict(supplier.getDistrict());
                supplier1.setAddress(supplier.getAddress());
                supplier1.setRoute(supplier.getRoute());
                supplier1.setLongitude(supplier.getLongitude());
                supplier1.setLatitude(supplier.getLatitude());
                supplier1.setTransitday(supplier.getTransitday());
                supplierMapper.updateByPrimaryKeySelective(supplier1);
                updateCount++;
            }
        }
        return ResultUtil.success("上传"+supplierList.size()+"条记录，成功新增"+count+"条，修改"+updateCount+"条");
    }

    /**
     * 修改供应商信息
     * 不可修改:编号、名称、区域、工厂，因为这样会导致关联的物料或其他信息中的线路信息没有自动更改
     * 不可修改:编号、名称，因为这样会导致PD单实际内容和系统内容不符
     * @param id
     * @param contact
     * @param phone
     * @param province
     * @param city
     * @param district
     * @param address
     * @param longitude
     * @param latitude
     * @return
     */
    @Override
    public Result update(int id,String abbreviation, String contact, String phone, String province, String city, String district, String address, String longitude,String latitude,String transitDay) {
        //参数检查
        if(!contact.matches("^[a-zA-Z\\u4e00-\\u9fa5]{0,10}$")){
            return ResultUtil.error(1,"联系人为1-10位的汉字、大小写字母");
        }else if(!abbreviation.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_*()。.（）-]{1,10}$")){
            return ResultUtil.error(1,"供应商简称只能是1-10位的数字、大小写字母、汉字、特殊字符(@#_*()。.（）-)");
        }else if(!phone.matches("^[0-9-]{0,15}$")){
            return ResultUtil.error(1,"电话必须为手机号或者是包含区号的座机号");
        }else if(!province.matches("^[\\u4e00-\\u9fa5]{1,10}$")){
            return ResultUtil.error(1,"省为1-10位的汉字");
        }else if(!city.matches("^[\\u4e00-\\u9fa5]{1,10}$")){
            return ResultUtil.error(1,"市为1-10位的汉字");
        }else if(!district.matches("^[\\u4e00-\\u9fa5]{1,10}$")){
            return ResultUtil.error(1,"区县为1-10位的汉字");
        }else if(!address.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_*-]{1,100}$")){
            return ResultUtil.error(1,"区县只能是1-100位的数字、大小写字母、汉字、特殊字符(@#_*-)");
        }else if(!longitude.matches("^(([\\+ \\-]?([1-9]{1}[0-9]{0,2})|([0]{1})))([.]\\d{0,6})?$")){
            return ResultUtil.error(1,"经度取值范围为-180到180之间的最多保留6位小数的数字");
        }else if(!latitude.matches("^(([\\+ \\-]?([1-9]{1}[0-9]{0,2})|([0]{1})))([.]\\d{0,6})?$")){
            return ResultUtil.error(1,"纬度取值范围为-180到180之间的最多保留6位小数的数字");
        }else if(!transitDay.matches("^\\d{1,2}(.[5]{1})?$")){
            return ResultUtil.error(1,"运输周期是0-100之间的最多保留1位小数的数字,且小数后只能是5");
        }else {
            int b=Double.valueOf(transitDay).intValue();
            if(b<0){
                return ResultUtil.error(1,"运输周期不可<0");
            }else if(b>=100){
                return ResultUtil.error(1,"运输周期不可>=100");
            }
        }
        //看要修改的供应商是否存在
        Supplier supplier=supplierMapper.selectByPrimaryKey(id);
        if(supplier==null){
            return ResultUtil.error(1,"供应商不存在，刷新页面后重试");
        }
        //修改
        supplier.setAbbreviation(abbreviation);
        supplier.setContact(contact);
        supplier.setPhone(phone);
        supplier.setProvince(province);
        supplier.setCity(city);
        supplier.setDistrict(district);
        supplier.setAddress(address);
        supplier.setLongitude(longitude);
        supplier.setLatitude(latitude);
        supplier.setTransitday(transitDay);
        supplierMapper.updateByPrimaryKeySelective(supplier);
        return ResultUtil.success();
    }

    /**
     * 删除供应商信息
     * @param id
     * @return
     */
    @Override
    public Result delete(int id) {
        Supplier supplier=supplierMapper.selectByPrimaryKey(id);
        if(supplier==null){
            return ResultUtil.error(1,"供应商不存在，刷新页面后重试");
        }
        //1.先删除凡是用到了供应商对应的物料id的其他记录
        List<Good> goodList=goodMapper.selectBySupplierid(id);
        if(!goodList.isEmpty()){
            //删除凡是用到了物料id的记录
            //TODO
        }
        //2.删除凡是单独用到了供应商id的记录
        //TODO
        //3.最后删除供应商信息
        supplierMapper.deleteByPrimaryKey(id);
        return ResultUtil.success();
    }

    /**
     * 条件分页查询
     * @param supplierCode
     * @param supplierName
     * @param province 省
     * @param city 市
     * @param district 县区
     * @param factoryId 工厂id
     * @param areaId 区域id
     * @param currentPage
     * @return
     */
    @Override
    public Result findAll(String supplierCode, String supplierName,String province, String city, String district, int areaId,int factoryId,int currentPage) {
        //校验参数
        if(!supplierCode.matches("^[0-9A-Z]{0,6}$")){
            return ResultUtil.error(1,"供应商编号只能是1-6位的数字、大写字母");
        }else if(!supplierName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_*-]{0,50}$")){
            return ResultUtil.error(1,"供应商名称只能是1-50位的数字、大小写字母、汉字、特殊字符(@#_*-)");
        }else if(!province.matches("^[\\u4e00-\\u9fa5]{0,10}$")){
            return ResultUtil.error(1,"省为1-10位的汉字");
        }else if(!city.matches("^[\\u4e00-\\u9fa5]{0,10}$")){
            return ResultUtil.error(1,"市为1-10位的汉字");
        }else if(!district.matches("^[\\u4e00-\\u9fa5]{0,10}$")){
            return ResultUtil.error(1,"区县为1-10位的汉字");
        }
        //查询起始下标
        int index=(currentPage-1)*30;
        //分页条件查询
        List<Supplier> list=supplierMapper.selectLimitByCondition(supplierCode,supplierName ,province,city,district,factoryId,areaId,index);
        if(!list.isEmpty()) {
            //总数量
            int totalCount=supplierMapper.selectCountByCondition(supplierCode,supplierName ,province,city,district,factoryId,areaId);
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
     * 根据id查询供应商
     * @param id
     * @return
     */
    @Override
    public Result supplierById(int id) {
        Supplier supplier=supplierMapper.selectByPrimaryKey(id);
        if(supplier==null){
            return ResultUtil.error(1,"供应商不存在");
        }
        return ResultUtil.success(supplier);
    }

    /**
     * 下载供应商信息
     * @param supplierCode
     * @param supplierName
     * @param province
     * @param city
     * @param district
     * @param areaId
     * @param factoryId
     * @param response
     */
    @Override
    public void supplierDownload(String supplierCode, String supplierName, String province, String city, String district, int areaId, int factoryId, HttpServletResponse response) {
        //获取结果
        List<Supplier> list=supplierMapper.selectByCondition(supplierCode,supplierName,province,city,district,factoryId,areaId);
        //创建Excel工作簿对象,此处选择SXSSFWorkbook,创建的excel以.xlsx结尾，支持2007、2010及以后版本
        SXSSFWorkbook wb = new SXSSFWorkbook();
        //创建表
        Sheet sheet = wb.createSheet();
        //给sheet设置名称
        wb.setSheetName(0,"供应商信息表");
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
        goodCodeCell.setCellValue("供应商编号");
        goodCodeCell.setCellStyle(titleCellStyle);
        Cell goodNameCell = titleRow.createCell(1);
        goodNameCell.setCellValue("供应商名称");
        goodNameCell.setCellStyle(titleCellStyle);
        Cell supplierCodeCell=titleRow.createCell(2);
        supplierCodeCell.setCellValue("联系人");
        supplierCodeCell.setCellStyle(titleCellStyle);
        Cell supplierNameCell = titleRow.createCell(3);
        supplierNameCell.setCellValue("电话");
        supplierNameCell.setCellStyle(titleCellStyle);
        Cell boxCodeCell = titleRow.createCell(4);
        boxCodeCell.setCellValue("省");
        boxCodeCell.setCellStyle(titleCellStyle);
        Cell countCell = titleRow.createCell(5);
        countCell.setCellValue("市");
        countCell.setCellStyle(titleCellStyle);
        Cell batchCell = titleRow.createCell(6);
        batchCell.setCellValue("区(县)");
        batchCell.setCellStyle(titleCellStyle);
        Cell typeCell=titleRow.createCell(7);
        typeCell.setCellValue("详细地址");
        typeCell.setCellStyle(titleCellStyle);
        Cell nameCell = titleRow.createCell(8);
        nameCell.setCellValue("经度");
        nameCell.setCellStyle(titleCellStyle);
        Cell timeCell=titleRow.createCell(9);
        timeCell.setCellValue("纬度");
        timeCell.setCellStyle(titleCellStyle);
        Cell time2Cell=titleRow.createCell(10);
        time2Cell.setCellValue("运输周期(天)");
        time2Cell.setCellStyle(titleCellStyle);
        Cell time3Cell=titleRow.createCell(11);
        time3Cell.setCellValue("所属区域");
        time3Cell.setCellStyle(titleCellStyle);
        Cell time4Cell=titleRow.createCell(12);
        time4Cell.setCellValue("所属工厂");
        time4Cell.setCellStyle(titleCellStyle);

        //创建主体内容的单元格样式和字体
        CellStyle bodyCellStyle = wb.createCellStyle();
        bodyCellStyle.setAlignment(CellStyle.ALIGN_CENTER);//左右居中
        bodyCellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);//上下居中
        Font bodyFont = wb.createFont();//创建字体
        bodyFont.setBoldweight(Font.BOLDWEIGHT_NORMAL);//不加粗
        bodyFont.setFontName("宋体");//设置字体
        bodyCellStyle.setFont(bodyFont);//把字体放入单元格样式
        //循环添加内容
        if (!list.isEmpty()) {
            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (int i = 0; i < list.size(); i++) {
                //每循环一次，创建一行。由于第一行是标题行，所以行下标从1开始
                Row bodyRow = sheet.createRow(i + 1);
                //给每行创建列，第一列下标为0,并给每列添加内容
                //供应商编号
                Cell cell0 = bodyRow.createCell(0);
                cell0.setCellValue(list.get(i).getSuppliercode());
                cell0.setCellStyle(bodyCellStyle);
                cell0.setCellType(Cell.CELL_TYPE_STRING);
                //供应商名称
                Cell cell1 = bodyRow.createCell(1);
                cell1.setCellValue(list.get(i).getSuppliername());
                cell1.setCellStyle(bodyCellStyle);
                cell1.setCellType(Cell.CELL_TYPE_STRING);
                //联系人
                Cell cell2 = bodyRow.createCell(2);
                cell2.setCellValue(list.get(i).getContact());
                cell2.setCellStyle(bodyCellStyle);
                cell2.setCellType(Cell.CELL_TYPE_STRING);
                //电话
                Cell cell3 = bodyRow.createCell(3);
                cell3.setCellValue(list.get(i).getPhone());
                cell3.setCellStyle(bodyCellStyle);
                cell3.setCellType(Cell.CELL_TYPE_STRING);
                //省
                Cell cell4 = bodyRow.createCell(4);
                cell4.setCellValue(list.get(i).getProvince());
                cell4.setCellStyle(bodyCellStyle);
                cell4.setCellType(Cell.CELL_TYPE_STRING);
                //市
                Cell cell5 = bodyRow.createCell(5);
                cell5.setCellValue(list.get(i).getCity());
                cell5.setCellStyle(bodyCellStyle);
                cell5.setCellType(Cell.CELL_TYPE_STRING);
                //区县
                Cell cell6 = bodyRow.createCell(6);
                cell6.setCellValue(list.get(i).getDistrict());
                cell6.setCellStyle(bodyCellStyle);
                cell6.setCellType(Cell.CELL_TYPE_STRING);
                //详细地址
                Cell cell8 = bodyRow.createCell(7);
                cell8.setCellValue(list.get(i).getAddress());
                cell8.setCellStyle(bodyCellStyle);
                cell8.setCellType(Cell.CELL_TYPE_STRING);
                //经度
                Cell cell7 = bodyRow.createCell(8);
                cell7.setCellValue(list.get(i).getLongitude());
                cell7.setCellStyle(bodyCellStyle);
                cell7.setCellType(Cell.CELL_TYPE_STRING);
                //纬度
                Cell cell9 = bodyRow.createCell(9);
                cell9.setCellValue(list.get(i).getLatitude());
                cell9.setCellStyle(bodyCellStyle);
                cell9.setCellType(Cell.CELL_TYPE_STRING);
                //运输周期
                Cell cell10 = bodyRow.createCell(10);
                cell10.setCellValue(list.get(i).getTransitday());
                cell10.setCellStyle(bodyCellStyle);
                cell10.setCellType(Cell.CELL_TYPE_STRING);
                //所属区域
                Cell cell11 = bodyRow.createCell(11);
                cell11.setCellValue(list.get(i).getRoute().getArea().getAreaname());
                cell11.setCellStyle(bodyCellStyle);
                cell11.setCellType(Cell.CELL_TYPE_STRING);
                //所属工厂
                Cell cell12 = bodyRow.createCell(12);
                cell12.setCellValue(list.get(i).getRoute().getFactory().getFactoryname());
                cell12.setCellStyle(bodyCellStyle);
                cell12.setCellType(Cell.CELL_TYPE_STRING);
            }
        }
        //设置响应内容编码
        response.setCharacterEncoding("UTF-8");
        //设置响应的内容格式
        response.setContentType("aplication/x-download");
        //设置文件名称
        String fileName = list.get(0).getRoute().getFactory().getFactoryname()+"供应商信息.xlsx";
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
     * 批量上传的模板下载
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
        goodCodeCell.setCellValue("供应商编号");
        goodCodeCell.setCellStyle(titleCellStyle);
        Cell goodNameCell = titleRow.createCell(1);
        goodNameCell.setCellValue("供应商名称");
        goodNameCell.setCellStyle(titleCellStyle);
        Cell abbreviationCell = titleRow.createCell(2);
        abbreviationCell.setCellValue("供应商简称");
        abbreviationCell.setCellStyle(titleCellStyle);
        Cell supplierCodeCell=titleRow.createCell(3);
        supplierCodeCell.setCellValue("联系人");
        supplierCodeCell.setCellStyle(titleCellStyle);
        Cell supplierNameCell = titleRow.createCell(4);
        supplierNameCell.setCellValue("电话");
        supplierNameCell.setCellStyle(titleCellStyle);
        Cell boxCodeCell = titleRow.createCell(5);
        boxCodeCell.setCellValue("省");
        boxCodeCell.setCellStyle(titleCellStyle);
        Cell countCell = titleRow.createCell(6);
        countCell.setCellValue("市");
        countCell.setCellStyle(titleCellStyle);
        Cell batchCell = titleRow.createCell(7);
        batchCell.setCellValue("区(县)");
        batchCell.setCellStyle(titleCellStyle);
        Cell typeCell=titleRow.createCell(8);
        typeCell.setCellValue("详细地址");
        typeCell.setCellStyle(titleCellStyle);
        Cell nameCell = titleRow.createCell(9);
        nameCell.setCellValue("经度");
        nameCell.setCellStyle(titleCellStyle);
        Cell timeCell=titleRow.createCell(10);
        timeCell.setCellValue("纬度");
        timeCell.setCellStyle(titleCellStyle);
        Cell time2Cell=titleRow.createCell(11);
        time2Cell.setCellValue("运输周期(天)");
        time2Cell.setCellStyle(titleCellStyle);
        Cell time3Cell=titleRow.createCell(12);
        time3Cell.setCellValue("所属区域");
        time3Cell.setCellStyle(titleCellStyle);
        Cell time4Cell=titleRow.createCell(13);
        time4Cell.setCellValue("所属工厂");
        time4Cell.setCellStyle(titleCellStyle);

        //创建主体内容的单元格样式和字体
        CellStyle bodyCellStyle = wb.createCellStyle();
        bodyCellStyle.setAlignment(CellStyle.ALIGN_CENTER);//左右居中
        bodyCellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);//上下居中
        Font bodyFont = wb.createFont();//创建字体
        bodyFont.setBoldweight(Font.BOLDWEIGHT_NORMAL);//不加粗
        bodyFont.setFontName("宋体");//设置字体
        bodyFont.setColor(Font.COLOR_RED);//字体颜色为红色
        bodyCellStyle.setFont(bodyFont);//把字体放入单元格样式
        //1.提示行内容
        Row titileRow = sheet.createRow(2);
        Cell titileCell0 = titileRow.createCell(0);
        titileCell0.setCellValue("以下列的值必须从下面的值中选择或按照以下要求填写(所有记录填完后把当前所有的红字内容清除)");
        titileCell0.setCellStyle(bodyCellStyle);
        titileCell0.setCellType(Cell.CELL_TYPE_STRING);
        //2.省、市、区格式要求
        Row addressRow = sheet.createRow(3);
        Cell addressCell0 = addressRow.createCell(0);
        addressCell0.setCellValue("省列的值格式必须为：XXX省或XX市，比如浙江省或北京市");
        addressCell0.setCellStyle(bodyCellStyle);
        addressCell0.setCellType(Cell.CELL_TYPE_STRING);

        Row addressRow1 = sheet.createRow(4);
        Cell addressCell1 = addressRow1.createCell(0);
        addressCell1.setCellValue("市列的值格式必须为：XX市或者“市辖区”，因为直辖市之下是没有市的，就只能填“市辖区”");
        addressCell1.setCellStyle(bodyCellStyle);
        addressCell1.setCellType(Cell.CELL_TYPE_STRING);

        Row addressRow2 = sheet.createRow(5);
        Cell addressCell2 = addressRow2.createCell(0);
        addressCell2.setCellValue("区(县)列的值格式必须为：XX区或XX县或XX市，填XX市是因为有些县级市的名字就为XX市，比如余姚市");
        addressCell2.setCellStyle(bodyCellStyle);
        addressCell2.setCellType(Cell.CELL_TYPE_STRING);
        //3.工厂名称行：
        Row factoryRow = sheet.createRow(6);
        Cell cell0 = factoryRow.createCell(0);
        cell0.setCellValue("所属工厂：");
        cell0.setCellStyle(bodyCellStyle);
        cell0.setCellType(Cell.CELL_TYPE_STRING);
        List<Factory> factoryList=factoryMapper.selectByCondition("","","","","");
        for(int k=0;k<factoryList.size();k++){
            Cell factoryCell= factoryRow.createCell(k+1);
            factoryCell.setCellValue(k+1+"."+factoryList.get(k).getFactoryname());
            factoryCell.setCellStyle(bodyCellStyle);
            factoryCell.setCellType(Cell.CELL_TYPE_STRING);
        }
        //4.区域名称行
        Row areaRow = sheet.createRow(7);
        Cell cell1 = areaRow.createCell(0);
        cell1.setCellValue("所属区域：");
        cell1.setCellStyle(bodyCellStyle);
        cell1.setCellType(Cell.CELL_TYPE_STRING);
        List<Area> areaList=areaMapper.selectAll();
        for(int k=0;k<areaList.size();k++){
            Cell areaCell= areaRow.createCell(k+1);
            areaCell.setCellValue(k+1+"."+areaList.get(k).getAreaname());
            areaCell.setCellStyle(bodyCellStyle);
            areaCell.setCellType(Cell.CELL_TYPE_STRING);
        }
        //设置响应内容编码
        response.setCharacterEncoding("UTF-8");
        //设置响应的内容格式
        response.setContentType("aplication/x-download");
        //设置文件名称
        String fileName = "供应商信息模板.xlsx";
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
     * 根据工厂id查询
     * 物料信息页面添加修改物料时，需要输入供应商名称，查询所有供应商
     * @param factoryId
     * @return
     */
    @Override
    public Result supplierByFactoryid(int factoryId) {
        List<Supplier> supplierList=supplierMapper.selectByCondition("","","","","",factoryId,0);
        if(supplierList.isEmpty()){
            return ResultUtil.success();
        }
        return ResultUtil.success(supplierList);
    }

    /**
     * 根据供应商名称查询不重复的供应商
     * @param supplierName
     * @return
     */
    @Override
    public Result supplierLikeName(String supplierName) {
        if(!supplierName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_*-]{1,50}$")){
            return ResultUtil.error(1,"供应商名称只能是1-50位的数字、大小写字母、汉字、特殊字符(@#_*-)");
        }
        List<Supplier> supplierList=supplierMapper.selectLikeSuppliername(supplierName);
        if(supplierList.isEmpty()){
            return ResultUtil.success();
        }
        return ResultUtil.success(supplierList);
    }
}
