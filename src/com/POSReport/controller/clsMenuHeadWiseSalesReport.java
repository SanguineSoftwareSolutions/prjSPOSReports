/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.POSReport.controller;

import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsPosConfigFile;
import java.awt.Desktop;
import java.awt.Dimension;
import java.io.File;
import java.io.InputStream;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.swing.JRViewer;

/**
 *
 * @author Harry
 */
public class clsMenuHeadWiseSalesReport 
{
    DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();
    public void funGenerateMenuHeadWiseReport(String reportType, HashMap hm,String dayEnd)
    {
        try
        {
            String reportName = "com/POSReport/reports/rptMenuHeadWiseSalesReport.jasper";
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);
              
            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String posName = hm.get("posName").toString();
            String fromDateToDisplay = hm.get("fromDateToDisplay").toString();
            String toDateToDisplay = hm.get("toDateToDisplay").toString();
            
            StringBuilder sbSqlLive = new StringBuilder();
            StringBuilder sbSqlQFile = new StringBuilder();
            StringBuilder sbSqlFilters = new StringBuilder();

            sbSqlLive.setLength(0);
            sbSqlQFile.setLength(0);
            sbSqlFilters.setLength(0);

            sbSqlQFile.append("SELECT  ifnull(d.strMenuCode,'ND'),ifnull(e.strMenuName,'ND'), sum(a.dblQuantity),\n"
                    + "sum(a.dblAmount)-sum(a.dblDiscountAmt),f.strPosName,'" + clsGlobalVarClass.gUserCode + "',sum(a.dblRate),sum(a.dblAmount) ,sum(a.dblDiscountAmt) "
                    + "FROM tblqbilldtl a\n"
                    + "left outer join tblqbillhd b on a.strBillNo=b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate) \n"
                    + "left outer join tblposmaster f on b.strposcode=f.strposcode "
                    + "left outer join tblmenuitempricingdtl d on a.strItemCode = d.strItemCode "
                    + " and b.strposcode =d.strposcode ");
            if (clsGlobalVarClass.gAreaWisePricing.equals("Y"))
            {
                sbSqlQFile.append("and b.strAreaCode= d.strAreaCode ");
            }
            sbSqlQFile.append("left outer join tblmenuhd e on d.strMenuCode= e.strMenuCode");
            sbSqlQFile.append(" where date( b.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
                    + " and a.strClientCode=b.strClientCode ");

            sbSqlLive.append("SELECT ifnull(d.strMenuCode,'ND'),ifnull(e.strMenuName,'ND'), sum(a.dblQuantity),\n"
                    + " sum(a.dblAmount)-sum(a.dblDiscountAmt),f.strPosName,'" + clsGlobalVarClass.gUserCode + "',sum(a.dblRate) ,sum(a.dblAmount),sum(a.dblDiscountAmt) "
                    + " FROM tblbilldtl a\n"
                    + " left outer join tblbillhd b on a.strBillNo=b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate) \n"
                    + " left outer join tblposmaster f on b.strposcode=f.strposcode "
                    + " left outer join tblmenuitempricingdtl d on a.strItemCode = d.strItemCode "
                    + " and b.strposcode =d.strposcode ");
            if (clsGlobalVarClass.gAreaWisePricing.equals("Y"))
            {
                sbSqlLive.append("and b.strAreaCode= d.strAreaCode ");
            }
            sbSqlLive.append("left outer join tblmenuhd e on d.strMenuCode= e.strMenuCode");
            sbSqlLive.append(" where date( b.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
                    + " and a.strClientCode=b.strClientCode ");

            String sqlModLive = "SELECT  ifnull(d.strMenuCode,'ND'),ifnull(e.strMenuName,'ND'), sum(a.dblQuantity),\n"
                    + "sum(a.dblAmount)-sum(a.dblDiscAmt),f.strPosName,'" + clsGlobalVarClass.gUserCode + "',sum(a.dblRate),sum(a.dblAmount),sum(a.dblDiscAmt) "
                    + "FROM tblbillmodifierdtl a\n"
                    + "left outer join tblbillhd b on a.strBillNo=b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate) \n"
                    + "left outer join tblposmaster f on b.strposcode=f.strposcode "
                    + "left outer join tblmenuitempricingdtl d on LEFT(a.strItemCode,7)= d.strItemCode "
                    + " and b.strposcode =d.strposcode ";

            if (clsGlobalVarClass.gAreaWisePricing.equals("Y"))
            {
                sqlModLive += "and b.strAreaCode= d.strAreaCode ";
            }
            sqlModLive += "left outer join tblmenuhd e on d.strMenuCode= e.strMenuCode";
            sqlModLive += " where date( b.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' and a.dblAmount>0 "
                    + " and a.strClientCode=b.strClientCode ";

            String sqlModQFile = "SELECT  ifnull(d.strMenuCode,'ND'),ifnull(e.strMenuName,'ND'), sum(a.dblQuantity),\n"
                    + "sum(a.dblAmount)-sum(a.dblDiscAmt),f.strPosName,'" + clsGlobalVarClass.gUserCode + "',sum(a.dblRate),sum(a.dblAmount),sum(a.dblDiscAmt) "
                    + "FROM tblqbillmodifierdtl a\n"
                    + "left outer join tblqbillhd b on a.strBillNo=b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate) \n"
                    + "left outer join tblposmaster f on b.strposcode=f.strposcode "
                    + "left outer join tblmenuitempricingdtl d on LEFT(a.strItemCode,7)= d.strItemCode "
                    + " and b.strposcode =d.strposcode ";

            if (clsGlobalVarClass.gAreaWisePricing.equals("Y"))
            {
                sqlModQFile += "and b.strAreaCode= d.strAreaCode ";
            }
            sqlModQFile += "left outer join tblmenuhd e on d.strMenuCode= e.strMenuCode";
            sqlModQFile += " where date( b.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' and a.dblAmount>0 "
                    + " and a.strClientCode=b.strClientCode ";

            if (!posCode.equals("All"))
            {
                sbSqlFilters.append(" AND b.strPOSCode = '" + posCode + "' ");
            }
            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sbSqlFilters.append(" AND b.intShiftCode = '" + shiftNo + "' ");
                }
            }
            sbSqlFilters.append(" Group by b.strPoscode, d.strMenuCode,e.strMenuName");

            sbSqlLive.append(sbSqlFilters);
            sbSqlQFile.append(sbSqlFilters);
            sqlModLive = sqlModLive + " " + sbSqlFilters.toString();
            sqlModQFile = sqlModQFile + " " + sbSqlFilters.toString();

            Map<String, clsGenericBean> mapMenuDtl = new HashMap<String, clsGenericBean>();

            //live data
            ResultSet rsData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
            funGetMenuHeadMap(mapMenuDtl, rsData);
            //live modifiers
            rsData = clsGlobalVarClass.dbMysql.executeResultSet(sqlModLive.toString());
            funGetMenuHeadMap(mapMenuDtl, rsData);
            //Q data
            rsData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFile.toString());
            funGetMenuHeadMap(mapMenuDtl, rsData);
            //live modifiers
            rsData = clsGlobalVarClass.dbMysql.executeResultSet(sqlModQFile.toString());
            funGetMenuHeadMap(mapMenuDtl, rsData);
            //convert to list
            Collection<clsGenericBean> listOfMenuHead = mapMenuDtl.values();

            //call for view report
            if(reportType.equalsIgnoreCase("A4 Size Report"))
            {
            funViewJasperReportForBeanCollectionDataSource(is, hm, listOfMenuHead);
            }
            if(reportType.equalsIgnoreCase("Excel Report"))
            {
                DecimalFormat decFormat = new DecimalFormat("0");
               // DecimalFormat decFormatFor2Decimal = new DecimalFormat("0.00");
                
                int i = 1;
                Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
                List<String> arrListTotal = new ArrayList<String>();
                List<String> arrHeaderList = new ArrayList<String>();
                double totalQty = 0;
                double totalAmount = 0;
                double subTotal = 0;
                double discountTotal = 0;
                for (clsGenericBean objBean : listOfMenuHead)
                {
                    List<String> arrListItem = new ArrayList<String>();
                    arrListItem.add(objBean.getStrName());
                    arrListItem.add(objBean.getStrPOSName());
                    arrListItem.add(String.valueOf(decFormat.format(objBean.getDblQty())));
                    arrListItem.add(String.valueOf(gDecimalFormat.format(objBean.getDblAmt())));
                    arrListItem.add(String.valueOf(gDecimalFormat.format(objBean.getDblDiscAmt())));
                    arrListItem.add(String.valueOf(gDecimalFormat.format(objBean.getDblSubTotal())));

                    totalQty = totalQty + Double.parseDouble(String.valueOf(objBean.getDblQty()));
                    totalAmount = totalAmount + Double.parseDouble(String.valueOf(objBean.getDblAmt()));
                    subTotal = subTotal + Double.parseDouble(String.valueOf(objBean.getDblSubTotal()));
                    discountTotal = discountTotal + Double.parseDouble(String.valueOf(objBean.getDblDiscAmt()));
                    mapExcelItemDtl.put(i, arrListItem);
                    i++;
                }

                arrListTotal.add(String.valueOf(decFormat.format(totalQty)) + "#" + "3");
                arrListTotal.add(String.valueOf(gDecimalFormat.format(totalAmount)) + "#" + "4");
                arrListTotal.add(String.valueOf(gDecimalFormat.format(discountTotal)) + "#" + "5");
                arrListTotal.add(String.valueOf(gDecimalFormat.format(subTotal)) + "#" + "6");

                arrHeaderList.add("Serial No");
                arrHeaderList.add("MenuName");
                arrHeaderList.add("POSName");
                arrHeaderList.add("Qty");
                arrHeaderList.add("Sub Total");
                arrHeaderList.add("Discount");
                arrHeaderList.add("Net Total");

                List<String> arrparameterList = new ArrayList<String>();
                arrparameterList.add("MenuHeadWise Report");
                arrparameterList.add("POS" + " : " + posName);
                arrparameterList.add("FromDate" + " : " + fromDateToDisplay);
                arrparameterList.add("ToDate" + " : " + toDateToDisplay);
                arrparameterList.add(" ");
                arrparameterList.add(" ");
                if (clsGlobalVarClass.gEnableShiftYN)
                {
                    if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                    {
                        arrparameterList.add("Shift No " + " : " + shiftNo.toString());
                    }
                    else
                    {
                        arrparameterList.add("Shift No " + " : " + shiftNo.toString());
                    }
                }

                funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "menuWiseExcelSheet",dayEnd);
            }
            
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }    
    }
    
     public void funCreateExcelSheet(List<String> parameterList, List<String> headerList, Map<Integer, List<String>> map, List<String> totalList, String fileName,String dayEnd)
    {
        String filePath = System.getProperty("user.dir");
        File file = new File(filePath +File.separator+ "Reports"+File.separator+ fileName + ".xls");
        try
        {
            WritableWorkbook workbook1 = Workbook.createWorkbook(file);
            WritableSheet sheet1 = workbook1.createSheet("First Sheet", 0);
            WritableFont cellFont = new WritableFont(WritableFont.COURIER, 14);
            cellFont.setBoldStyle(WritableFont.BOLD);
            WritableCellFormat cellFormat = new WritableCellFormat(cellFont);
            WritableFont headerCellFont = new WritableFont(WritableFont.TIMES, 10);
            headerCellFont.setBoldStyle(WritableFont.BOLD);
            WritableCellFormat headerCell = new WritableCellFormat(headerCellFont);

            for (int j = 0; j <= parameterList.size(); j++)
            {
                Label l0 = new Label(2, 0, parameterList.get(0), cellFormat);
                Label l1 = new Label(0, 2, parameterList.get(1), headerCell);
                Label l2 = new Label(1, 2, parameterList.get(2), headerCell);
                Label l3 = new Label(2, 2, parameterList.get(3), headerCell);
                Label l4 = new Label(0, 3, parameterList.get(4), headerCell);
                Label l5 = new Label(1, 3, parameterList.get(5), headerCell);

                sheet1.addCell(l0);
                sheet1.addCell(l1);
                sheet1.addCell(l2);
                sheet1.addCell(l3);
                sheet1.addCell(l4);
                sheet1.addCell(l5);
            }

            for (int j = 0; j < headerList.size(); j++)
            {
                Label lblHeader = new Label(j, 5, headerList.get(j), headerCell);
                sheet1.addCell(lblHeader);
            }

            int i = 7;
            for (Map.Entry<Integer, List<String>> entry : map.entrySet())
            {
                Label lbl0 = new Label(0, i, entry.getKey().toString());
                List<String> nameList = map.get(entry.getKey());
                for (int j = 0; j < nameList.size(); j++)
                {
                    int colIndex = j + 1;
                    Label lblData = new Label(colIndex, i, nameList.get(j));
                    sheet1.addCell(lblData);
                    sheet1.setColumnView(i, 15);
                }
                sheet1.addCell(lbl0);
                i++;
            }

            for (int j = 0; j < totalList.size(); j++)
            {
                String[] l0 = new String[10];
                for (int c = 0; c < totalList.size(); c++)
                {
                    l0 = totalList.get(c).split("#");
                    int pos = Integer.parseInt(l0[1]);
                    Label lable0 = new Label(pos, i + 1, l0[0], headerCell);
                    sheet1.addCell(lable0);
                }
                Label labelTotal = new Label(0, i + 1, "TOTAL:", headerCell);
                sheet1.addCell(labelTotal);
            }
            workbook1.write();
            workbook1.close();

            if(!dayEnd.equalsIgnoreCase("Yes"))
           {
            Desktop dt = Desktop.getDesktop();
            dt.open(file);
           }

        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(null, ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    private void funViewJasperReportForBeanCollectionDataSource(InputStream is, HashMap hm, Collection listOfBillData)
    {
        try
        {
            JRBeanCollectionDataSource beanCollectionDataSource = new JRBeanCollectionDataSource(listOfBillData);
            JasperPrint print = JasperFillManager.fillReport(is, hm, beanCollectionDataSource);
            List<JRPrintPage> pages = print.getPages();
            if (pages.size()==0)
            {
               JOptionPane.showMessageDialog(null, "Data not present for selected dates!!!"); 
            }
            else
            {  
            JRViewer viewer = new JRViewer(print);
            JFrame jf = new JFrame();
            jf.getContentPane().add(viewer);
            jf.validate();
            jf.setVisible(true);
            jf.setSize(new Dimension(850, 750));
            }

        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            if (e.getMessage().startsWith("Byte data not found at"))
            {
                JOptionPane.showMessageDialog(null, "Report Image Not Found!!!\nPlease Check Property Setup Report Image.", "Error Code: RIMG-1", JOptionPane.ERROR_MESSAGE);
            }
            e.printStackTrace();
        }
    }
    
    private void funGetMenuHeadMap(Map<String, clsGenericBean> mapMenuDtl, ResultSet rsData)
    {
        try
        {
            while (rsData.next())
            {
                if (mapMenuDtl.containsKey(rsData.getString(1)))
                {
                    clsGenericBean obj = mapMenuDtl.get(rsData.getString(1));

                    obj.setDblQty(obj.getDblQty() + rsData.getDouble(3));
                    obj.setDblAmt(obj.getDblAmt() + rsData.getDouble(8));
                    obj.setDblSubTotal(obj.getDblSubTotal() + rsData.getDouble(4));
                    obj.setDblDiscAmt(obj.getDblDiscAmt() + rsData.getDouble(9));
                    obj.setStrPOSName(obj.getStrPOSName().toString());
                    mapMenuDtl.put(rsData.getString(1), obj);
                }
                else
                {
                    clsGenericBean obj = new clsGenericBean();

                    obj.setStrCode(rsData.getString(1));
                    obj.setStrName(rsData.getString(2));
                    obj.setDblQty(rsData.getDouble(3));
                    obj.setDblAmt(rsData.getDouble(8));
                    obj.setDblSubTotal(rsData.getDouble(4));
                    obj.setDblDiscAmt(rsData.getDouble(9));
                    obj.setStrPOSName(rsData.getString(5));
                    mapMenuDtl.put(rsData.getString(1), obj);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
