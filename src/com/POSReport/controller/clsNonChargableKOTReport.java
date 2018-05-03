package com.POSReport.controller;

import com.POSGlobal.controller.clsBillDtl;
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

public class clsNonChargableKOTReport 
{
    DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();
    public void funNonChargableKOTReport(String reportType, HashMap hm,String dayEnd)
    {
        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptNonChargableKOTReport.jasper");

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String reasonCode = hm.get("reasonCode").toString();
            String posName = hm.get("posName").toString();
            String fromDateToDisplay = hm.get("fromDateToDisplay").toString();
            String toDateToDisplay = hm.get("toDateToDisplay").toString();
            
            StringBuilder sqlBuilder = new StringBuilder();

            //live
            sqlBuilder.setLength(0);
            sqlBuilder.append("select a.strKOTNo, DATE_FORMAT(a.dteNCKOTDate,'%d-%m-%Y %H:%i'), a.strTableNo, b.strReasonName,d.strPosName,\n"
                    + "a.strRemark,  a.strItemCode, c.strItemName, a.dblQuantity, a.dblRate, a.dblQuantity * a.dblRate as Amount\n"
                    + ",e.strTableName\n"
                    + "from tblnonchargablekot a, tblreasonmaster b, tblitemmaster c,tblposmaster d,tbltablemaster e\n"
                    + "where  a.strReasonCode = b.strReasonCode \n"
                    + "and a.strTableNo=e.strTableNo \n"
                    + "and a.strItemCode = c.strItemCode  and a.strPosCode=d.strPOSCode\n"
                    + "and date(a.dteNCKOTDate) between '" + fromDate + "' and  '" + toDate + "'\n ");
            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and a.strPOSCode='" + posCode + "' ");
            }
            if (!reasonCode.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and a.strReasonCode='" + reasonCode + "'  ");
            }

            ResultSet rsData = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
            List<clsBillDtl> listOfNCKOTData = new ArrayList<clsBillDtl>();
            while (rsData.next())
            {
                clsBillDtl obj = new clsBillDtl();
                obj.setStrKOTNo(rsData.getString(1));
                obj.setDteNCKOTDate(rsData.getString(2));
                obj.setStrTableNo(rsData.getString(3));
                obj.setStrReasonName(rsData.getString(4));
                obj.setStrPosName(rsData.getString(5));
                obj.setStrRemarks(rsData.getString(6));
                obj.setStrItemCode(rsData.getString(7));
                obj.setStrItemName(rsData.getString(8));
                obj.setDblQuantity(rsData.getDouble(9));
                obj.setDblRate(rsData.getDouble(10));
                obj.setDblAmount(rsData.getDouble(11));
                obj.setStrTableName(rsData.getString(12));
                listOfNCKOTData.add(obj);
            }
            //call for view report
            if(reportType.equalsIgnoreCase("A4 Size Report"))
            {
            funViewJasperReportForBeanCollectionDataSource(is, hm, listOfNCKOTData);
            }
            if(reportType.equalsIgnoreCase("Excel Report"))
            {
                Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
                List<String> arrListTotal = new ArrayList<String>();
                List<String> arrHeaderList = new ArrayList<String>();

                //DecimalFormat gDecimalFormat = new DecimalFormat("#.##");
                DecimalFormat decimalFormat = new DecimalFormat("#");
                double totalQty = 0;
                double totalAmount = 0;
                int i = 1;
                
                for(clsBillDtl objBillDtl:listOfNCKOTData)
                {
                    List<String> arrListItem = new ArrayList<String>();
                    arrListItem.add(objBillDtl.getStrKOTNo());
                    arrListItem.add(objBillDtl.getDteNCKOTDate());
                    arrListItem.add(objBillDtl.getStrPosName());
                    arrListItem.add(objBillDtl.getStrTableName());
                    arrListItem.add(objBillDtl.getStrItemName());
                    arrListItem.add(String.valueOf(decimalFormat.format(objBillDtl.getDblQuantity())));//qty
                    arrListItem.add(String.valueOf(gDecimalFormat.format(objBillDtl.getDblAmount())));//amt
                    arrListItem.add(objBillDtl.getStrReasonName());
                    arrListItem.add(objBillDtl.getStrRemarks());

                    totalQty = totalQty + objBillDtl.getDblQuantity();
                    totalAmount = totalAmount + objBillDtl.getDblAmount();
                    mapExcelItemDtl.put(i, arrListItem);
                    i++;
                }

                arrListTotal.add(String.valueOf(decimalFormat.format(totalQty)) + "#" + "6");
                arrListTotal.add(String.valueOf(gDecimalFormat.format(totalAmount) + "#" + "7"));

                arrHeaderList.add("Serial No");
                arrHeaderList.add("KOT NO.");
                arrHeaderList.add("NCKOT Date");
                arrHeaderList.add("POS Name");
                arrHeaderList.add("Table No");
                arrHeaderList.add("Rate");
                arrHeaderList.add("Qty");
                arrHeaderList.add("Amount");
                arrHeaderList.add("Reason");
                arrHeaderList.add("Remarks");

                List<String> arrparameterList = new ArrayList<String>();
                arrparameterList.add("Non Chargable KOT Report");
                arrparameterList.add("POS" + " : " + posName);
                arrparameterList.add("FromDate" + " : " + fromDateToDisplay);
                arrparameterList.add("ToDate" + " : " + toDateToDisplay);
                arrparameterList.add("Reason" + " : " + reasonCode);
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

                funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "NCKOTExcelSheet",dayEnd);
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
}
