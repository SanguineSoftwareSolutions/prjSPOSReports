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

public class clsDailySalesReport 
{
    DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter(); 
    public void funDailyCollectionReport(String reportType, HashMap hm,String dayEnd)
    {
        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptDailySalesReport.jasper");
            //DecimalFormat gDecimalFormat = new DecimalFormat("0.00");
            String fromDate = hm.get("fromDate").toString();
            String fromDateToDisplay = hm.get("fromDateToDisplay").toString();
            String toDateToDisplay = hm.get("toDateToDisplay").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String posName = hm.get("posName").toString();

            List<clsBillItemDtlBean> listOfDailySaleData = new ArrayList<clsBillItemDtlBean>();
            StringBuilder sbSqlBillWise = new StringBuilder();
            StringBuilder sbSqlBillWiseQFile = new StringBuilder();

            sbSqlBillWise.setLength(0);
            sbSqlBillWise.append("select a.strBillNo,left(a.dteBillDate,10),left(right(a.dteDateCreated,8),5) as BillTime"
                    + ",ifnull(b.strTableName,'') as TableName,f.strPOSName, ifnull(d.strSettelmentDesc,'') as payMode"
                    + ",ifnull(a.dblSubTotal,0.00),a.dblDiscountPer,a.dblDiscountAmt,a.dblTaxAmt"
                    + ",ifnull(c.dblSettlementAmt,0.00),a.strUserCreated,a.strUserEdited,a.dteDateCreated"
                    + ",a.dteDateEdited,a.strClientCode,a.strWaiterNo,a.strCustomerCode,a.dblDeliveryCharges"
                    + ",ifnull(c.strRemark,''),ifnull(e.strCustomerName ,'NA')"
                    + ",a.dblTipAmount,'" + clsGlobalVarClass.gUserCode + "',a.strDiscountRemark,'' "
                    + "from tblbillhd  a "
                    + "left outer join  tbltablemaster b on a.strTableNo=b.strTableNo "
                    + "left outer join tblposmaster f on a.strPOSCode=f.strPOSCode "
                    + "left outer join tblbillsettlementdtl c on a.strBillNo=c.strBillNo and date(a.dteBillDate)=date(c.dteBillDate) "
                    + "left outer join tblsettelmenthd d on c.strSettlementCode=d.strSettelmentCode "
                    + "left outer join tblcustomermaster e on a.strCustomerCode=e.strCustomerCode "
                    + "where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "'");

            if (!posCode.equals("All"))
            {
                sbSqlBillWise.append(" and a.strPOSCode='" + posCode + "' ");
            }
            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sbSqlBillWise.append(" AND a.intShiftCode = '" + shiftNo + "' ");
                }
            }

            sbSqlBillWise.append(" order by a.strBillNo desc");
            //System.out.println("Bill Wise Report Live Query="+sbSqlBillWise);

            sbSqlBillWiseQFile.setLength(0);
            sbSqlBillWiseQFile.append("select a.strBillNo,left(a.dteBillDate,10),left(right(a.dteDateCreated,8),5) as BillTime "
                    + ",ifnull(b.strTableName,'') as TableName,f.strPOSName, ifnull(d.strSettelmentDesc,'') as payMode "
                    + ",ifnull(a.dblSubTotal,0.00),a.dblDiscountPer,a.dblDiscountAmt,a.dblTaxAmt "
                    + ",ifnull(c.dblSettlementAmt,0.00),a.strUserCreated,a.strUserEdited,a.dteDateCreated "
                    + ",a.dteDateEdited,a.strClientCode,a.strWaiterNo,a.strCustomerCode,a.dblDeliveryCharges "
                    + ",ifnull(c.strRemark,''),ifnull(e.strCustomerName ,'NA')"
                    + ",a.dblTipAmount,'" + clsGlobalVarClass.gUserCode + "',a.strDiscountRemark,'' "
                    + "from tblqbillhd  a "
                    + "left outer join  tbltablemaster b on a.strTableNo=b.strTableNo "
                    + "left outer join tblposmaster f on a.strPOSCode=f.strPOSCode "
                    + "left outer join tblqbillsettlementdtl c on a.strBillNo=c.strBillNo and date(a.dteBillDate)=date(c.dteBillDate) "
                    + "left outer join tblsettelmenthd d on c.strSettlementCode=d.strSettelmentCode "
                    + "left outer join tblcustomermaster e on a.strCustomerCode=e.strCustomerCode "
                    + "where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "'");

            if (!posCode.equals("All"))
            {
                sbSqlBillWiseQFile.append(" and a.strPOSCode='" + posCode + "' ");
            }
            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sbSqlBillWiseQFile.append(" AND a.intShiftCode = '" + shiftNo + "' ");
                }
            }
            sbSqlBillWiseQFile.append(" order by a.strBillNo desc");

            ResultSet rsLiveData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlBillWise.toString());
            while (rsLiveData.next())
            {
                clsBillItemDtlBean obj = new clsBillItemDtlBean();
                obj.setStrBillNo(rsLiveData.getString(1));          //BillNo
                obj.setDteBillDate(rsLiveData.getString(2));        //Bill Date
                obj.setStrItemCode(rsLiveData.getString(4));        //Table Name    
                obj.setStrPosName(rsLiveData.getString(5));         //POS Name
                obj.setStrSettelmentMode(rsLiveData.getString(6));  //Settle Mode
                obj.setDblSubTotal(rsLiveData.getDouble(7));        //Sub Total
                obj.setDblDiscountPer(rsLiveData.getDouble(8));     //Disc Per
                obj.setDblDiscountAmt(rsLiveData.getDouble(9));     //Disc Amt
                obj.setDblTaxAmt(rsLiveData.getDouble(10));         //Tax Amt
                obj.setDblSettlementAmt(rsLiveData.getDouble(11));  //Settle Amt
                obj.setStrDiscType(rsLiveData.getString(12));       //User Created
                obj.setStrDiscValue(rsLiveData.getString(14));      //Date Created
                obj.setStrItemName(rsLiveData.getString(21));       //Customer Name
                obj.setDblAmount(rsLiveData.getDouble(19));         //Delivery Charges
                listOfDailySaleData.add(obj);
            }
            rsLiveData.close();

            ResultSet rsQFileData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlBillWiseQFile.toString());
            while (rsQFileData.next())
            {
                clsBillItemDtlBean obj = new clsBillItemDtlBean();
                obj.setStrBillNo(rsQFileData.getString(1));          //BillNo
                obj.setDteBillDate(rsQFileData.getString(2));        //Bill Date
                obj.setStrItemCode(rsQFileData.getString(4));        //Table Name    
                obj.setStrPosName(rsQFileData.getString(5));         //POS Name
                obj.setStrSettelmentMode(rsQFileData.getString(6));  //Settle Mode
                obj.setDblSubTotal(rsQFileData.getDouble(7));        //Sub Total
                obj.setDblDiscountPer(rsQFileData.getDouble(8));     //Disc Per
                obj.setDblDiscountAmt(rsQFileData.getDouble(9));     //Disc Amt
                obj.setDblTaxAmt(rsQFileData.getDouble(10));         //Tax Amt
                obj.setDblSettlementAmt(rsQFileData.getDouble(11));  //Settle Amt
                obj.setStrDiscType(rsQFileData.getString(12));       //User Created
                obj.setStrDiscValue(rsQFileData.getString(14));      //Date Created
                obj.setStrItemName(rsQFileData.getString(21));       //Customer Name
                obj.setDblAmount(rsQFileData.getDouble(19));         //Delivery Charges
                listOfDailySaleData.add(obj);
            }
            rsQFileData.close();
            //call for view report
            if(reportType.equalsIgnoreCase("A4 Size Report"))
            {
            funViewJasperReportForBeanCollectionDataSource(is, hm, listOfDailySaleData);
            }
            if(reportType.equalsIgnoreCase("Excel Report"))
            {
                
                Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
                List<String> arrListTotal = new ArrayList<String>();
                List<String> arrHeaderList = new ArrayList<String>();
                double totalSettleAmt = 0;
                double totalTax = 0;
                double totalsubTotal = 0;
                double totalDisAmt = 0,totalDisPer=0;
                int i = 1;
                for(clsBillItemDtlBean objBean:listOfDailySaleData)
                {
                    List<String> arrListItem = new ArrayList<String>();
                    arrListItem.add(objBean.getStrBillNo());
                    arrListItem.add(objBean.getStrItemCode());
                    arrListItem.add(String.valueOf(gDecimalFormat.format(objBean.getDblSubTotal())));
                    arrListItem.add(String.valueOf(Math.rint(objBean.getDblDiscountPer())));
                    arrListItem.add(String.valueOf(gDecimalFormat.format(objBean.getDblDiscountAmt())));
                    arrListItem.add(String.valueOf(gDecimalFormat.format(objBean.getDblTaxAmt())));
                    arrListItem.add(String.valueOf(gDecimalFormat.format(objBean.getDblSettlementAmt())));
                    arrListItem.add(objBean.getStrDiscType());
                    arrListItem.add(objBean.getStrItemName());
                    arrListItem.add(objBean.getStrSettelmentMode());
                    
                    //totalsubTotal = totalsubTotal + Double.parseDouble(String.valueOf(objBean.getDblSubTotal()));
                    totalDisPer = totalDisPer + Double.parseDouble(String.valueOf(objBean.getDblDiscountPer()));
                    totalDisAmt = totalDisAmt + Double.parseDouble(String.valueOf(objBean.getDblDiscountAmt()));
                    totalTax = totalTax + Double.parseDouble(String.valueOf(objBean.getDblTaxAmt()));
                    totalSettleAmt = totalSettleAmt + Double.parseDouble(String.valueOf(objBean.getDblSettlementAmt()));
                    mapExcelItemDtl.put(i, arrListItem);
                    i++;
                }
                arrListTotal.add(String.valueOf((Math.rint(totalDisPer))) + "#" + "4");
                arrListTotal.add(String.valueOf(gDecimalFormat.format(Math.rint(totalDisAmt))) + "#" + "5");
                arrListTotal.add(String.valueOf(gDecimalFormat.format(Math.rint(totalTax))) + "#" + "6");
                arrListTotal.add(String.valueOf(gDecimalFormat.format(Math.rint(totalSettleAmt))) + "#" + "7");

                arrHeaderList.add("Serial No");
                arrHeaderList.add("Bill No");
                arrHeaderList.add("Table");
                arrHeaderList.add("Taxable Amt");
                arrHeaderList.add("Disc %");
                arrHeaderList.add("Disc Amt");
                arrHeaderList.add("Tax Amt");
                arrHeaderList.add("Bill Amt");
                arrHeaderList.add("User");
                arrHeaderList.add("Customer Name");
                arrHeaderList.add("Settlement Name");
                
               
               

                List<String> arrparameterList = new ArrayList<String>();
                arrparameterList.add("Daily Sales Report");
                arrparameterList.add("POS" + " : " + posName );
                arrparameterList.add("FromDate" + " : " + fromDateToDisplay);
                arrparameterList.add("ToDate" + " : " + toDateToDisplay);
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

                funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "dailySalesReportExcelSheet",dayEnd);

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
        File file = new File(filePath + File.separator+"Reports"+File.separator + fileName + ".xls");
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
