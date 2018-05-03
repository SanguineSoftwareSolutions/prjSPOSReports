/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.POSReport.controller;

import com.POSGlobal.controller.clsBillDtl;
import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsSalesFlashReport;
import java.awt.Desktop;
import java.io.File;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/**
 *
 * @author Sanguine
 */
public class clsCustomerWiseSales
{

    private String strCustomerCode;
    private String strCustomerName;
    private String strCustomerNo;
    private int noOfBills;
    private double dblSalesAmt;
    private String strDOB;

    public void funGenerateCustomerWiseSalesExcelReport(String reportType, HashMap hm, String dayEnd, String type)
    {
        try
        {
            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String posName = hm.get("posName").toString();
            String groupCode = hm.get("groupCode").toString();
            String subGroupCode = hm.get("subGroupCode").toString();

            StringBuilder sqlBuilder = new StringBuilder();
            List<clsBillDtl> listOfCustomerrWiseItemSales = new ArrayList<>();
            StringBuilder sbSqlLiveBill = new StringBuilder();
            StringBuilder sbSqlQFileBill = new StringBuilder();
            StringBuilder sbSqlFilters = new StringBuilder();

            sbSqlLiveBill.setLength(0);
            sbSqlQFileBill.setLength(0);
            sbSqlFilters.setLength(0);

            sbSqlLiveBill.append("select ifnull(b.strCustomerCode,'ND'),ifnull(b.strCustomerName,'ND')"
                    + ",ifnull(count(a.strBillNo),'0'),ifnull(sum(a.dblGrandTotal),'0.00')"
                    + ",'" + clsGlobalVarClass.gUserCode + "',b.longMobileNo,b.dteDOB "
                    + "from tblbillhd a,tblcustomermaster b "
                    + "where a.strCustomerCode=b.strCustomerCode "
                    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
                    + "and a.strPOSCode='" + posCode + "' "
                    + "group by b.strCustomerCode ");

            sbSqlQFileBill.append("select ifnull(b.strCustomerCode,'ND'),ifnull(b.strCustomerName,'ND')"
                    + ",ifnull(count(a.strBillNo),'0'),ifnull(sum(a.dblGrandTotal),'0.00')"
                    + ",'" + clsGlobalVarClass.gUserCode + "',b.longMobileNo,b.dteDOB "
                    + "from tblqbillhd a,tblcustomermaster b "
                    + "where a.strCustomerCode=b.strCustomerCode "
                    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
                    + "and a.strPOSCode='" + posCode + "' "
                    + "group by b.strCustomerCode ");

            clsSalesFlashReport obj = new clsSalesFlashReport();
            obj.funProcessSalesFlashReport(sbSqlLiveBill.toString(), sbSqlQFileBill.toString(), "CustWiseBillSales");

            double billCount = 0;
            double grandTotal = 0;
            String sql = "select a.strbillno,a.dtebilldate,sum(a.tmebilltime),sum(a.strtablename),a.strposcode,a.strpaymode  "
                    + "from tbltempsalesflash1 a "
                    + "where strUser='" + clsGlobalVarClass.gUserCode + "' "
                    + "group by a.strbillno ";
            ResultSet rsCustomerWise = clsGlobalVarClass.dbMysql.executeResultSet(sql);
            Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
            List<String> arrListTotal = new ArrayList<String>();
            List<String> arrHeaderList = new ArrayList<String>();

            DecimalFormat decimalFormat2Decimal = new DecimalFormat("0.00");
            DecimalFormat decimalFormat1Decimal = new DecimalFormat("0.0");
            int i = 1;
            while (rsCustomerWise.next())
            {
                List<String> arrListItem = new ArrayList<String>();
                arrListItem.add(rsCustomerWise.getString(2));
                arrListItem.add(rsCustomerWise.getString(5));
                arrListItem.add(rsCustomerWise.getString(6));
                arrListItem.add(decimalFormat1Decimal.format(rsCustomerWise.getDouble(3)));
                arrListItem.add(decimalFormat2Decimal.format(rsCustomerWise.getDouble(4)));

                billCount += rsCustomerWise.getDouble(3);
                grandTotal += rsCustomerWise.getDouble(4);

                mapExcelItemDtl.put(i, arrListItem);
            }
            rsCustomerWise.close();

            arrHeaderList.add("Serial No");
            arrHeaderList.add("Customer");
            arrHeaderList.add("Mobile No");
            arrHeaderList.add("Date Of Birth");
            arrHeaderList.add("No Of Bills");
            arrHeaderList.add("Sales Amount");

            arrListTotal.add(decimalFormat1Decimal.format(billCount) + "#" + "4");
            arrListTotal.add(decimalFormat2Decimal.format(grandTotal) + "#" + "5");

            List<String> arrparameterList = new ArrayList<String>();
            arrparameterList.add("Customer Wise Sales Report");
            arrparameterList.add("POS" + " : " + posName);
            arrparameterList.add("FromDate" + " : " + fromDate);
            arrparameterList.add("ToDate" + " : " + toDate);
            arrparameterList.add("");
            arrparameterList.add("  ");

            funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "CustomerWiseSalesExcelSheet", dayEnd);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public void funCreateExcelSheet(List<String> parameterList, List<String> headerList, Map<Integer, List<String>> map, List<String> totalList, String fileName, String dayEnd)
    {
        String filePath = System.getProperty("user.dir");
        File file = new File(filePath + File.separator + "Reports" + File.separator + fileName + ".xls");
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

//            if (!dayEnd.equalsIgnoreCase("Yes"))
//            {
//                Desktop dt = Desktop.getDesktop();
//                dt.open(file);
//            }

        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(null, ex.getMessage());
            ex.printStackTrace();
        }
    }
}
