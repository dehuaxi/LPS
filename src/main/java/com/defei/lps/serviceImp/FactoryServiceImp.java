package com.defei.lps.serviceImp;

import com.defei.lps.dao.FactoryMapper;
import com.defei.lps.dao.RouteMapper;
import com.defei.lps.dao.UserMapper;
import com.defei.lps.dao.UserRouteMapper;
import com.defei.lps.entity.Factory;
import com.defei.lps.entity.Route;
import com.defei.lps.entity.User;
import com.defei.lps.result.Result;
import com.defei.lps.result.ResultUtil;
import com.defei.lps.service.FactoryService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FactoryServiceImp implements FactoryService {
    @Autowired
    private FactoryMapper factoryMapper;
    @Autowired
    private RouteMapper routeMapper;
    @Autowired
    private UserRouteMapper userRouteMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 添加工厂信息
     * @param factoryName 工厂名称
     * @param factoryNumber 工厂编号
     * @param describes 描述
     * @return
     */
    @Override
    public synchronized Result add(String factoryName, String factoryNumber,String describes,String province,String city,String district,String address,String longitude,String latitude) {
        //参数校验
        if(!factoryName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_-]{1,10}$")){
            return ResultUtil.error(1,"工厂名称只能是1-20位的数字、大小写字母、汉字、特殊字符(@#_-)");
        }else if(!factoryNumber.matches("^[0-9A-Za-z#@_-]{1,10}$")){
            return ResultUtil.error(1,"工厂编号只能是1-9位的数字、大小写字母、特殊字符(@#_-)");
        }else if(!describes.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_-]{0,50}$")){
            return ResultUtil.error(1,"描述只能是1-50位的数字、大小写字母、汉字、特殊字符(@#_-)");
        }else if(!province.matches("^[\\u4e00-\\u9fa5]{1,10}$")){
            return ResultUtil.error(1,"省为1-10位的汉字");
        }else if(!city.matches("^[\\u4e00-\\u9fa5]{1,10}$")){
            return ResultUtil.error(1,"市为1-10位的汉字");
        }else if(!district.matches("^[\\u4e00-\\u9fa5]{1,10}$")){
            return ResultUtil.error(1,"区(县)为1-10位的汉字");
        }else if(!address.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_-]{1,100}$")){
            return ResultUtil.error(1,"省详细地址只能是1-100位的数字、大小写字母、汉字、特殊字符(@#_-)");
        }else if(!longitude.matches("^(([\\+ \\-]?([1-9]{1}[0-9]{0,2})|([0]{1})))([.]\\d{0,6})?$")){
            return ResultUtil.error(1,"经度取值范围为-180到180之间的最多保留6位小数的数字");
        }else if(!latitude.matches("^(([\\+ \\-]?([1-9]{1}[0-9]{0,2})|([0]{1})))([.]\\d{0,6})?$")){
            return ResultUtil.error(1,"纬度取值范围为-180到180之间的最多保留6位小数的数字");
        }
        //先检查工厂编号是否存
        Factory factory=factoryMapper.selectByFactorynumber(factoryNumber);
        if(factory!=null){
            return ResultUtil.error(1,"工厂编号已经存在");
        }
        //再检查工厂名称是否存在
        Factory factory1=factoryMapper.selectByFactoryname(factoryName);
        if(factory1!=null){
            return ResultUtil.error(1,"工厂名称已经存在");
        }
        //都不存在，就保存
        Factory factory2=new Factory();
        factory2.setFactoryname(factoryName);
        factory2.setFactorynumber(factoryNumber);
        factory2.setDescribes(describes);
        factory2.setProvince(province);
        factory2.setCity(city);
        factory2.setDistrict(district);
        factory2.setAddress(address);
        factory2.setLongitude(longitude);
        factory2.setLatitude(latitude);
        factoryMapper.insertSelective(factory2);
        return ResultUtil.success();
    }

    /**
     * 修改工厂信息
     * @param id
     * @param factoryName
     * @param factoryNumber
     * @param describes
     * @return
     */
    @Override
    public synchronized Result update(int id, String factoryName,String factoryNumber,String describes,String province,String city,String district,String address,String longitude,String latitude) {
        //参数校验
        if(!factoryName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_-]{1,10}$")){
            return ResultUtil.error(1,"工厂名称只能是1-20位的数字、大小写字母、汉字、特殊字符(@#_-)");
        }else if(!factoryNumber.matches("^[0-9A-Za-z#@_-]{1,10}$")){
            return ResultUtil.error(1,"工厂编号只能是1-9位的数字、大小写字母、特殊字符(@#_-)");
        }else if(!describes.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_-]{0,50}$")){
            return ResultUtil.error(1,"描述只能是1-50位的数字、大小写字母、汉字、特殊字符(@#_-)");
        }else if(!province.matches("^[\\u4e00-\\u9fa5]{1,10}$")){
            return ResultUtil.error(1,"省为1-10位的汉字");
        }else if(!city.matches("^[\\u4e00-\\u9fa5]{1,10}$")){
            return ResultUtil.error(1,"市为1-10位的汉字");
        }else if(!district.matches("^[\\u4e00-\\u9fa5]{1,10}$")){
            return ResultUtil.error(1,"区(县)为1-10位的汉字");
        }else if(!address.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_-]{1,100}$")){
            return ResultUtil.error(1,"省详细地址只能是1-100位的数字、大小写字母、汉字、特殊字符(@#_-)");
        }else if(!longitude.matches("^(([\\+ \\-]?([1-9]{1}[0-9]{0,2})|([0]{1})))([.]\\d{0,6})?$")){
            return ResultUtil.error(1,"经度取值范围为-180到180之间的最多保留6位小数的数字");
        }else if(!latitude.matches("^(([\\+ \\-]?([1-9]{1}[0-9]{0,2})|([0]{1})))([.]\\d{0,6})?$")){
            return ResultUtil.error(1,"纬度取值范围为-180到180之间的最多保留6位小数的数字");
        }
        //先检查工厂编号是否存
        Factory factory=factoryMapper.selectByPrimaryKey(id);
        if(factory==null){
            return ResultUtil.error(1,"工厂不存在，刷新页面后重试");
        }
        if(!factoryNumber.equals(factory.getFactorynumber())){
            //先检查工厂编号是否存
            Factory factory1=factoryMapper.selectByFactorynumber(factoryNumber);
            if(factory1!=null){
                return ResultUtil.error(1,"修改后的工厂编号已经存在");
            }
        }
        if(!factoryName.equals(factory.getFactoryname())){
            //再检查工厂名称是否存在
            Factory factory1=factoryMapper.selectByFactoryname(factoryName);
            if(factory1!=null){
                return ResultUtil.error(1,"修改后的工厂名称已经存在");
            }
        }
        //修改工厂信息本身
        factory.setFactoryname(factoryName);
        factory.setFactorynumber(factoryNumber);
        factory.setDescribes(describes);
        factory.setProvince(province);
        factory.setCity(city);
        factory.setDistrict(district);
        factory.setAddress(address);
        factory.setLongitude(longitude);
        factory.setLatitude(latitude);
        factoryMapper.updateByPrimaryKeySelective(factory);
        return ResultUtil.success();
    }

    /**
     * 删除工厂信息
     * @param id
     * @return
     */
    @Override
    public Result delete(int id) {
        //先检查工厂编号是否存
        Factory factory=factoryMapper.selectByPrimaryKey(id);
        if(factory==null){
            return ResultUtil.error(1,"工厂不存在，刷新页面后重试");
        }
        //------删除凡是用到了该路线的记录------
        //删除线路表。先要删除线路对应的：线路-用户关系表记录,再删除线路表
        List<Route> routes=routeMapper.selectByFactoryid(factory.getId()) ;
        for(Route route:routes){
            //删除线路-用户关系表记录
            userRouteMapper.deleteByRouteid(route.getId());
            //删除路线表记录
            routeMapper.deleteByPrimaryKey(route.getId());
        }
        //删除其他凡是用到了工厂的记录
        //TODO
        //最后删除工厂记录
        factoryMapper.deleteByPrimaryKey(id);
        return ResultUtil.success();
    }

    /**
     * 条件分页查询
     * @param factoryName
     * @param factoryNumber
     * @param currentPage
     * @return
     */
    @Override
    public Result findAll(String factoryName, String factoryNumber,String province,String city,String district, int currentPage) {
        if(!factoryName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_-]{0,10}$")){
            return ResultUtil.error(1,"工厂名称只能是1-20位的数字、大小写字母、汉字、特殊字符(@#_-)");
        }else if(!factoryNumber.matches("^[0-9A-Za-z#@_-]{0,10}$")){
            return ResultUtil.error(1,"工厂编号只能是1-9位的数字、大小写字母、特殊字符(@#_-)");
        }else if(!province.matches("^[\\u4e00-\\u9fa5]{0,10}$")){
            return ResultUtil.error(1,"省为1-10位的汉字");
        }else if(!city.matches("^[\\u4e00-\\u9fa5]{0,10}$")){
            return ResultUtil.error(1,"市为1-10位的汉字");
        }else if(!district.matches("^[\\u4e00-\\u9fa5]{0,10}$")){
            return ResultUtil.error(1,"区(县)为1-10位的汉字");
        }
        //查询起始下标
        int index=(currentPage-1)*30;
        //分页条件查询
        List<Factory> list=factoryMapper.selectLimitByCondition(factoryName,factoryNumber ,province,city,district,index);
        if(!list.isEmpty()) {
            //总数量
            int totalCount=factoryMapper.selectCountByCondition(factoryName,factoryNumber,province,city,district);
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
     * 获取当前账号能使用的所有工厂集合
     * 主要是为页面提供下拉框的数据
     * @return
     */
    @Override
    public Result currentFactory() {
        String userName= (String)SecurityUtils.getSubject().getPrincipal();
        User user=userMapper.selectByUserName(userName);
        if(user==null){
            return ResultUtil.error(1,"请刷新页面后重试");
        }
        List<Factory> factoryList=new ArrayList<>();
        //如果是超级用户，直接查询所有工厂
        if(userName.equals("furiadmin")){
            factoryList=factoryMapper.selectByCondition("","","","","");
            if(factoryList.isEmpty()){
                return ResultUtil.error(1,"系统未添加工厂信息，请先添加工厂信息");
            }
        }else {
            List<Route> routeList=routeMapper.selectByUserid(user.getId());
            if(routeList.isEmpty()){
                return ResultUtil.error(1,"当前账号没有分配工厂");
            }
            //集合存放工厂对象
            for(Route route:routeList){
                if(!factoryList.contains(route.getFactory())){
                    factoryList.add(route.getFactory());
                }
            }
        }
        return ResultUtil.success(factoryList);
    }

    /**
     * 根据id查询
     * @param id
     * @return
     */
    @Override
    public Result factoryById(int id) {
        Factory factory=factoryMapper.selectByPrimaryKey(id);
        if(factory==null){
            return ResultUtil.error(1,"工厂不存在");
        }
        return ResultUtil.success(factory);
    }

    /**
     * 下载
     * @param factoryName
     * @param factoryNumber
     * @param province
     * @param city
     * @param district
     * @param response
     */
    @Override
    public void factoryDownload(String factoryName, String factoryNumber, String province, String city, String district, HttpServletResponse response) {
        //获取结果
        List<Factory> list=factoryMapper.selectByCondition(factoryName,factoryNumber,province,city,district);
        //创建Excel工作簿对象,此处选择SXSSFWorkbook,创建的excel以.xlsx结尾，支持2007、2010及以后版本
        SXSSFWorkbook wb = new SXSSFWorkbook();
        //创建表
        Sheet sheet = wb.createSheet();
        //给sheet设置名称
        wb.setSheetName(0,"工厂信息表");
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
        goodCodeCell.setCellValue("工厂名称");
        goodCodeCell.setCellStyle(titleCellStyle);
        Cell goodNameCell = titleRow.createCell(1);
        goodNameCell.setCellValue("工厂编号");
        goodNameCell.setCellStyle(titleCellStyle);
        Cell supplierCodeCell=titleRow.createCell(2);
        supplierCodeCell.setCellValue("描述");
        supplierCodeCell.setCellStyle(titleCellStyle);
        Cell supplierNameCell = titleRow.createCell(3);
        supplierNameCell.setCellValue("省");
        supplierNameCell.setCellStyle(titleCellStyle);
        Cell boxCodeCell = titleRow.createCell(4);
        boxCodeCell.setCellValue("市");
        boxCodeCell.setCellStyle(titleCellStyle);
        Cell countCell = titleRow.createCell(5);
        countCell.setCellValue("区(县)");
        countCell.setCellStyle(titleCellStyle);
        Cell batchCell = titleRow.createCell(6);
        batchCell.setCellValue("详细地址");
        batchCell.setCellStyle(titleCellStyle);
        Cell typeCell=titleRow.createCell(7);
        typeCell.setCellValue("经度");
        typeCell.setCellStyle(titleCellStyle);
        Cell nameCell = titleRow.createCell(8);
        nameCell.setCellValue("纬度");
        nameCell.setCellStyle(titleCellStyle);

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
                //名称
                Cell cell0 = bodyRow.createCell(0);
                cell0.setCellValue(list.get(i).getFactoryname());
                cell0.setCellStyle(bodyCellStyle);
                cell0.setCellType(Cell.CELL_TYPE_STRING);
                //编号
                Cell cell1 = bodyRow.createCell(1);
                cell1.setCellValue(list.get(i).getFactorynumber());
                cell1.setCellStyle(bodyCellStyle);
                cell1.setCellType(Cell.CELL_TYPE_STRING);
                //描述
                Cell cell2 = bodyRow.createCell(2);
                cell2.setCellValue(list.get(i).getDescribes());
                cell2.setCellStyle(bodyCellStyle);
                cell2.setCellType(Cell.CELL_TYPE_STRING);
                //省
                Cell cell3 = bodyRow.createCell(3);
                cell3.setCellValue(list.get(i).getProvince());
                cell3.setCellStyle(bodyCellStyle);
                cell3.setCellType(Cell.CELL_TYPE_STRING);
                //市
                Cell cell4 = bodyRow.createCell(4);
                cell4.setCellValue(list.get(i).getCity());
                cell4.setCellStyle(bodyCellStyle);
                cell4.setCellType(Cell.CELL_TYPE_STRING);
                //区县
                Cell cell5 = bodyRow.createCell(5);
                cell5.setCellValue(list.get(i).getDistrict());
                cell5.setCellStyle(bodyCellStyle);
                cell5.setCellType(Cell.CELL_TYPE_STRING);
                //地址
                Cell cell6 = bodyRow.createCell(6);
                cell6.setCellValue(list.get(i).getAddress());
                cell6.setCellStyle(bodyCellStyle);
                cell6.setCellType(Cell.CELL_TYPE_STRING);
                //经度
                Cell cell7 = bodyRow.createCell(7);
                cell7.setCellValue(list.get(i).getLongitude());
                cell7.setCellStyle(bodyCellStyle);
                cell7.setCellType(Cell.CELL_TYPE_STRING);
                //纬度
                Cell cell9 = bodyRow.createCell(8);
                cell9.setCellValue(list.get(i).getLatitude());
                cell9.setCellStyle(bodyCellStyle);
                cell9.setCellType(Cell.CELL_TYPE_STRING);
            }
        }
        //设置响应内容编码
        response.setCharacterEncoding("UTF-8");
        //设置响应的内容格式
        response.setContentType("aplication/x-download");
        //设置文件名称
        String fileName = "工厂信息.xlsx";
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

}
