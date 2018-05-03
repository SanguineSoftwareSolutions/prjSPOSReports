package com.POSReport.controller;

import com.POSReport.controller.comparator.clsRevenueHeadComparator;
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
import java.util.Collections;
import java.util.Comparator;
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

public class clsRevenueHeadWiseReport 
{
    DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter(); 
    public void funRevenueHeadWiseReport(String reportType, HashMap hm,String dayEnd)
    {
       try
        {
            
            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String rptType = hm.get("rptType").toString();
            String revenueHead = hm.get("revenueHead").toString();
            String posName = hm.get("posName").toString();
            String fromDateToDisplay = hm.get("fromDateToDisplay").toString();
            String toDateToDisplay = hm.get("toDateToDisplay").toString();
            
            StringBuilder sbSqlLive = new StringBuilder();
            StringBuilder sbSqlQFile = new StringBuilder();
            StringBuilder sbSqlFilters = new StringBuilder();

            sbSqlLive.setLength(0);
            sbSqlQFile.setLength(0);
            sbSqlFilters.setLength(0);

            sbSqlQFile.append("select e.strRevenueHead,d.strMenuName,a.strItemName, SUM(a.dblQuantity), SUM(a.dblAmount),a.strItemCode "
                    + "from tblqbilldtl a,tblqbillhd b ,tblmenuitempricingdtl c,tblmenuhd d,tblitemmaster e "
                    + "where a.strBillNo=b.strBillNo  "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + "and a.strItemCode=c.strItemCode "
                    + "and c.strMenuCode=d.strMenuCode "
                    + "and a.strItemCode=e.strItemCode "
                    + "and c.strPosCode=if(c.strPosCode='All','All',b.strPOSCode) "
                    + "AND (b.strAreaCode=c.strAreaCode or c.strAreaCode='A001') ");

            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sbSqlQFile.append(" and b.intShiftCode = '" + shiftNo + "' ");
                }
            }

            String sqlModQFile = "SELECT e.strRevenueHead,d.strMenuName,a.strModifierName, SUM(a.dblQuantity), SUM(a.dblAmount),a.strItemCode "
                    + "FROM tblqbillmodifierdtl a,tblqbillhd b,tblmenuitempricingdtl c,tblmenuhd d,tblitemmaster e "
                    + "WHERE a.strBillNo=b.strBillNo  "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + "AND left(a.strItemCode,7)=c.strItemCode  "
                    + "AND c.strMenuCode=d.strMenuCode  "
                    + "AND left(a.strItemCode,7)=e.strItemCode  "
                    + "AND c.strPosCode= IF(c.strPosCode='All','All',b.strPOSCode)  "
                    + "AND (b.strAreaCode=c.strAreaCode or c.strAreaCode='A001')  "
                    + "AND DATE(b.dteBillDate)  BETWEEN '" + fromDate + "' AND '" + toDate + "' ";

            if (!posCode.equals("All"))
            {
                sqlModQFile += " and b.strPosCode='" + posCode + "'  ";
            }
            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sqlModQFile += " and b.intShiftCode = '" + shiftNo + "' ";
                }
            }

            if (!revenueHead.equalsIgnoreCase("All"))
            {
                sqlModQFile += " and e.strRevenueHead='" + revenueHead + "' ";
            }
            InputStream is;
            if (rptType.equalsIgnoreCase("Summary"))
            {
                is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptRevenueHeadWiseSummaryReport.jasper");
                sqlModQFile += "GROUP BY e.strRevenueHead,d.strMenuCode "
                        + "ORDER BY e.strRevenueHead,d.strMenuCode ";
            }
            else
            {
                is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptRevenueHeadWiseReport.jasper");
                sqlModQFile += "GROUP BY e.strRevenueHead,d.strMenuCode,a.strModifierName "
                        + "ORDER BY e.strRevenueHead,d.strMenuCode,a.strModifierName ";
            }

            sbSqlLive.append("select e.strRevenueHead,d.strMenuName,a.strItemName, SUM(a.dblQuantity), SUM(a.dblAmount),a.strItemCode "
                    + "from tblbilldtl a,tblbillhd b ,tblmenuitempricingdtl c,tblmenuhd d,tblitemmaster e "
                    + "where a.strBillNo=b.strBillNo  "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + "and a.strItemCode=c.strItemCode "
                    + "and c.strMenuCode=d.strMenuCode "
                    + "and a.strItemCode=e.strItemCode "
                    + "and c.strPosCode=if(c.strPosCode='All','All',b.strPOSCode) "
                    + "AND (b.strAreaCode=c.strAreaCode or c.strAreaCode='A001') ");

            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sbSqlLive.append(" and b.intShiftCode = '" + shiftNo + "' ");
                }
            }

            String sqlModLive = "SELECT e.strRevenueHead,d.strMenuName,a.strModifierName, SUM(a.dblQuantity), SUM(a.dblAmount),a.strItemCode "
                    + "FROM tblbillmodifierdtl a,tblbillhd b,tblmenuitempricingdtl c,tblmenuhd d,tblitemmaster e "
                    + "WHERE a.strBillNo=b.strBillNo  "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + "AND left(a.strItemCode,7)=c.strItemCode  "
                    + "AND c.strMenuCode=d.strMenuCode  "
                    + "AND left(a.strItemCode,7)=e.strItemCode  "
                    + "AND c.strPosCode= IF(c.strPosCode='All','All',b.strPOSCode)  "
                    + "AND (b.strAreaCode=c.strAreaCode or c.strAreaCode='A001')  "
                    + "AND DATE(b.dteBillDate)  BETWEEN '" + fromDate + "' AND '" + toDate + "' ";
            if (!posCode.equals("All"))
            {
                sqlModLive += " and b.strPosCode='" + posCode + "'  ";
            }
            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sqlModLive += " and b.intShiftCode = '" + shiftNo + "' ";
                }
            }

            if (!revenueHead.equalsIgnoreCase("All"))
            {
                sqlModLive += " and e.strRevenueHead='" + revenueHead + "' ";
            }

            if (rptType.equalsIgnoreCase("Summary"))
            {
                sqlModLive += "GROUP BY e.strRevenueHead,d.strMenuCode "
                        + "ORDER BY e.strRevenueHead,d.strMenuCode ";
            }
            else
            {
                sqlModLive += "GROUP BY e.strRevenueHead,d.strMenuCode,a.strModifierName "
                        + "ORDER BY e.strRevenueHead,d.strMenuCode,a.strModifierName ";
            }

            sbSqlFilters.append(" AND date(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' ");
            if (!posCode.equals("All"))
            {
                sbSqlFilters.append(" and b.strPosCode='" + posCode + "'  ");
            }

            if (!revenueHead.equalsIgnoreCase("All"))
            {
                sbSqlFilters.append(" and e.strRevenueHead='" + revenueHead + "' ");
            }

            if (rptType.equalsIgnoreCase("Summary"))
            {
                sbSqlFilters.append(" group by e.strRevenueHead,d.strMenuCode "
                        + " order by e.strRevenueHead,d.strMenuCode ");
            }
            else
            {
                sbSqlFilters.append(" group by e.strRevenueHead,d.strMenuCode,a.strItemName"
                        + " order by e.strRevenueHead,d.strMenuCode,a.strItemName");
            }
            sbSqlLive.append(sbSqlFilters);
            sbSqlQFile.append(sbSqlFilters);

            ResultSet rsData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
            List<clsRevenueBean> listOfRevenueData = new ArrayList<clsRevenueBean>();
            while (rsData.next())
            {
                clsRevenueBean obj = new clsRevenueBean();
                obj.setStrRevenueHead(rsData.getString(1));
                obj.setStrMenuName(rsData.getString(2));
                obj.setStrItemName(rsData.getString(3));
                obj.setDblQuantity(rsData.getDouble(4));
                obj.setDblAmount(rsData.getDouble(5));
                obj.setStrItemCode(rsData.getString(6));
                listOfRevenueData.add(obj);
            }
            rsData.close();
            
            ResultSet rsQData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFile.toString());
            while (rsQData.next())
            {
                clsRevenueBean obj = new clsRevenueBean();
                obj.setStrRevenueHead(rsQData.getString(1));
                obj.setStrMenuName(rsQData.getString(2));
                obj.setStrItemName(rsQData.getString(3));
                obj.setDblQuantity(rsQData.getDouble(4));
                obj.setDblAmount(rsQData.getDouble(5));
                obj.setStrItemCode(rsQData.getString(6));
                listOfRevenueData.add(obj);
            }
            rsQData.close();

            ResultSet rsModData = clsGlobalVarClass.dbMysql.executeResultSet(sqlModLive.toString());
            while (rsModData.next())
            {
                clsRevenueBean obj = new clsRevenueBean();
                obj.setStrRevenueHead(rsModData.getString(1));
                obj.setStrMenuName(rsModData.getString(2));
                obj.setStrItemName(rsModData.getString(3));
                obj.setDblQuantity(rsModData.getDouble(4));
                obj.setDblAmount(rsModData.getDouble(5));
                obj.setStrItemCode(rsModData.getString(6));
                listOfRevenueData.add(obj);
            }
            rsModData.close();
            
            ResultSet rsModQData = clsGlobalVarClass.dbMysql.executeResultSet(sqlModQFile.toString());
            while (rsModQData.next())
            {
                clsRevenueBean obj = new clsRevenueBean();
                obj.setStrRevenueHead(rsModQData.getString(1));
                obj.setStrMenuName(rsModQData.getString(2));
                obj.setStrItemName(rsModQData.getString(3));
                obj.setDblQuantity(rsModQData.getDouble(4));
                obj.setDblAmount(rsModQData.getDouble(5));
                obj.setStrItemCode(rsModQData.getString(6));
                listOfRevenueData.add(obj);
            }
            rsModQData.close();

            Comparator<clsRevenueBean> revenueHeadNameComparator = new Comparator<clsRevenueBean>()
            {

                @Override
                public int compare(clsRevenueBean o1, clsRevenueBean o2)
                {
                    return o1.getStrRevenueHead().compareTo(o2.getStrRevenueHead());
                }
            };
            Comparator<clsRevenueBean> menuHeadNameComparator = new Comparator<clsRevenueBean>()
            {

                @Override
                public int compare(clsRevenueBean o1, clsRevenueBean o2)
                {
                    return o1.getStrMenuName().compareTo(o2.getStrMenuName());
                }
            };
            Comparator<clsRevenueBean> itemCodeComparator = new Comparator<clsRevenueBean>()
            {

                @Override
                public int compare(clsRevenueBean o1, clsRevenueBean o2)
                {
                    return o1.getStrItemCode().substring(0, 7).compareTo(o2.getStrItemCode().substring(0, 7));
                }
            };
            Collections.sort(listOfRevenueData, new clsRevenueHeadComparator(
                    revenueHeadNameComparator, menuHeadNameComparator, itemCodeComparator));

            //call for view report
            if(reportType.equalsIgnoreCase("A4 Size Report"))
            {
            funViewJasperReportForBeanCollectionDataSource(is, hm, listOfRevenueData);
            }
            if(reportType.equalsIgnoreCase("Excel Report"))
            {
                Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
                List<String> arrListTotal = new ArrayList<String>();
                List<String> arrHeaderList = new ArrayList<String>();
                double totalQty = 0;
                double totalAmt = 0;
                DecimalFormat decFormat = new DecimalFormat("0");
                //DecimalFormat gDecimalFormat = new DecimalFormat("0.00");
                int i = 1;
                for (int cnt = 0; cnt < listOfRevenueData.size(); cnt++)
                {
                    List<String> arrListItem = new ArrayList<String>();
                    clsRevenueBean objRevenueBrean = listOfRevenueData.get(cnt);

                    if (rptType.equalsIgnoreCase("Summary"))
                    {
                        arrListItem.add(objRevenueBrean.getStrRevenueHead());
                        arrListItem.add(objRevenueBrean.getStrMenuName());
                        arrListItem.add("" + decFormat.format(objRevenueBrean.getDblQuantity()));
                        arrListItem.add("" + gDecimalFormat.format(objRevenueBrean.getDblAmount()));
                        totalQty = totalQty + objRevenueBrean.getDblQuantity();
                        totalAmt = totalAmt + objRevenueBrean.getDblAmount();
                        mapExcelItemDtl.put(i, arrListItem);
                        i++;

                    }
                    else
                    {
                        arrListItem.add(objRevenueBrean.getStrRevenueHead());
                        arrListItem.add(objRevenueBrean.getStrMenuName());
                        arrListItem.add(objRevenueBrean.getStrItemName());
                        arrListItem.add("" + decFormat.format(objRevenueBrean.getDblQuantity()));
                        arrListItem.add("" + gDecimalFormat.format(objRevenueBrean.getDblAmount()));
                        totalQty = totalQty + objRevenueBrean.getDblQuantity();
                        totalAmt = totalAmt + objRevenueBrean.getDblAmount();
                        mapExcelItemDtl.put(i, arrListItem);
                        i++;

                    }

                }

                if (rptType.equalsIgnoreCase("Summary"))
                {
                    arrListTotal.add(String.valueOf(decFormat.format(totalQty)) + "#" + "3");
                    arrListTotal.add(String.valueOf(gDecimalFormat.format(totalAmt)) + "#" + "4");
                }
                else
                {
                    arrListTotal.add(String.valueOf(decFormat.format(totalQty)) + "#" + "4");
                    arrListTotal.add(String.valueOf(gDecimalFormat.format(totalAmt)) + "#" + "5");
                }

                if (rptType.equalsIgnoreCase("Summary"))
                {
                    arrHeaderList.add("Serial No");
                    arrHeaderList.add("Revenue Head");
                    arrHeaderList.add("Menu Name");
                }
                else
                {
                    arrHeaderList.add("Item Name");
                }

                arrHeaderList.add("Qty");
                arrHeaderList.add("Amount");

                List<String> arrparameterList = new ArrayList<String>();
                arrparameterList.add("Revenue Head Wise Report");
                arrparameterList.add("POS" + " : " + posName);
                arrparameterList.add("FromDate" + " : " + fromDate);
                arrparameterList.add("ToDate" + " : " + toDate);
                arrparameterList.add(" ");
                arrparameterList.add(" ");
                if (clsGlobalVarClass.gEnableShiftYN)
                {
                    if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                    {
                        arrparameterList.add("Shift No " + " : " + shiftNo);
                    }
                    else
                    {
                        arrparameterList.add("Shift No " + " : " + shiftNo);
                    }
                }

                funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "revenueHeadWiseExcelSheet",dayEnd);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
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
    
    public void funCreateExcelSheet(List<String> parameterList, List<String> headerList, Map<Integer, List<String>> map, List<String> totalList, String fileName,String dayEnd)
    {
        String filePath = System.getProperty("user.dir");
        File file = new File(filePath +File.separator+"Reports"+File.separator+ fileName + ".xls");
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
}
