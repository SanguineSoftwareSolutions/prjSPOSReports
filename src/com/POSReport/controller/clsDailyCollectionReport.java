package com.POSReport.controller;

import com.POSReport.controller.comparator.clsBillComparator;
import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsPosConfigFile;
import com.POSGlobal.controller.clsVoidBillDtl;
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

public class clsDailyCollectionReport
{
    DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter(); 
    public void funDailyCollectionReport(String reportType, HashMap hm, String dayEnd)
    {
        try
        {

            InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptDailyCollectionReport.jasper");

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String posName = hm.get("posName").toString();
            String fromDateToDisplay = hm.get("fromDateToDisplay").toString();
            String toDateToDisplay = hm.get("toDateToDisplay").toString();

            Map mapMultiSettleBills = new HashMap();

            StringBuilder sqlBuilder = new StringBuilder();
            //DecimalFormat gDecimalFormat = new DecimalFormat("0.00");

            //live
            sqlBuilder.setLength(0);
            sqlBuilder.append("SELECT a.strBillNo, DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y') AS dteBillDate,b.strPosName "
                    + ", IFNULL(d.strSettelmentDesc,'') AS strSettelmentMode,a.dblDiscountAmt,a.dblTaxAmt "
                    + ", SUM(c.dblSettlementAmt) AS dblSettlementAmt,a.dblSubTotal,a.strSettelmentMode,intBillSeriesPaxNo "
                    + ",ifnull(e.strTableName,''),a.strUserEdited,ifnull(f.strCustomerName,'') "
                    + "FROM tblbillhd a "
                    + "join tblposmaster b on a.strPOSCode=b.strPOSCode  "
                    + "join tblbillsettlementdtl c on a.strBillNo=c.strBillNo AND DATE(a.dteBillDate)= DATE(c.dteBillDate) AND a.strClientCode=c.strClientCode "
                    + "join tblsettelmenthd d on c.strSettlementCode=d.strSettelmentCode  "
                    + "left outer join tbltablemaster e on a.strTableNo=e.strTableNo "
                    + "left outer join tblcustomermaster f on a.strCustomerCode=f.strCustomerCode "
                    + "where date(a.dteBillDate) between '" + fromDate + "' and  '" + toDate + "'  ");
            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and a.strPOSCode='" + posCode + "' ");
            }
            if (!shiftNo.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and a.intShiftCode='" + shiftNo + "'  ");
            }
            sqlBuilder.append("GROUP BY a.strClientCode, DATE(a.dteBillDate),a.strBillNo,d.strSettelmentCode "
                    + "ORDER BY d.strSettelmentCode ");

            ResultSet rsData = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
            List<clsBillItemDtlBean> listOfBillData = new ArrayList<clsBillItemDtlBean>();
            while (rsData.next())
            {
                String key = rsData.getString(1) + "!" + rsData.getString(2);

                clsBillItemDtlBean obj = new clsBillItemDtlBean();
                if (mapMultiSettleBills.containsKey(key))//billNo
                {
                    obj.setStrBillNo(rsData.getString(1));
                    obj.setDteBillDate(rsData.getString(2));
                    obj.setStrPosName(rsData.getString(3));
                    obj.setStrSettelmentMode(rsData.getString(4));
                    obj.setDblDiscountAmt(0.00);
                    obj.setDblTaxAmt(0.00);
                    obj.setDblSettlementAmt(rsData.getDouble(7));
                    obj.setDblSubTotal(0.00);
                    obj.setIntBillSeriesPaxNo(0);
                }
                else
                {
                    obj.setStrBillNo(rsData.getString(1));
                    obj.setDteBillDate(rsData.getString(2));
                    obj.setStrPosName(rsData.getString(3));
                    obj.setStrSettelmentMode(rsData.getString(4));
                    obj.setDblDiscountAmt(rsData.getDouble(5));
                    obj.setDblTaxAmt(rsData.getDouble(6));
                    obj.setDblSettlementAmt(rsData.getDouble(7));
                    obj.setDblSubTotal(rsData.getDouble(8));
                    obj.setIntBillSeriesPaxNo(rsData.getInt(10));
                    obj.setStrItemCode(rsData.getString(11));
                    obj.setStrDiscType(rsData.getString(12));
                    obj.setStrItemName(rsData.getString(13));

                    if (obj.getDblSubTotal() > 0)
                    {
                        obj.setDblDiscountPer((obj.getDblDiscountAmt() / obj.getDblSubTotal()) * 100);
                    }
                }
                listOfBillData.add(obj);

                if (rsData.getString(9).equalsIgnoreCase("MultiSettle"))
                {
                    mapMultiSettleBills.put(key, rsData.getString(1));
                }
            }

            //QFile
            sqlBuilder.setLength(0);
            sqlBuilder.append("SELECT a.strBillNo, DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y') AS dteBillDate,b.strPosName "
                    + ", IFNULL(d.strSettelmentDesc,'') AS strSettelmentMode,a.dblDiscountAmt,a.dblTaxAmt "
                    + ", SUM(c.dblSettlementAmt) AS dblSettlementAmt,a.dblSubTotal,a.strSettelmentMode,intBillSeriesPaxNo "
                    + ",ifnull(e.strTableName,''),a.strUserEdited,ifnull(f.strCustomerName,'') "
                    + "FROM tblqbillhd a "
                    + "join tblposmaster b on a.strPOSCode=b.strPOSCode  "
                    + "join tblqbillsettlementdtl c on a.strBillNo=c.strBillNo AND DATE(a.dteBillDate)= DATE(c.dteBillDate) AND a.strClientCode=c.strClientCode "
                    + "join tblsettelmenthd d on c.strSettlementCode=d.strSettelmentCode  "
                    + "left outer join tbltablemaster e on a.strTableNo=e.strTableNo "
                    + "left outer join tblcustomermaster f on a.strCustomerCode=f.strCustomerCode "
                    + "where date(a.dteBillDate) between '" + fromDate + "' and  '" + toDate + "'  ");
            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and a.strPOSCode='" + posCode + "' ");
            }
            if (!shiftNo.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and a.intShiftCode='" + shiftNo + "'  ");
            }
            sqlBuilder.append("GROUP BY a.strClientCode, DATE(a.dteBillDate),a.strBillNo,d.strSettelmentCode "
                    + "ORDER BY d.strSettelmentCode ");

            rsData = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
            while (rsData.next())
            {
                String key = rsData.getString(1) + "!" + rsData.getString(2);

                clsBillItemDtlBean obj = new clsBillItemDtlBean();
                if (mapMultiSettleBills.containsKey(key))//billNo
                {
                    obj.setStrBillNo(rsData.getString(1));
                    obj.setDteBillDate(rsData.getString(2));
                    obj.setStrPosName(rsData.getString(3));
                    obj.setStrSettelmentMode(rsData.getString(4));
                    obj.setDblDiscountAmt(0.00);
                    obj.setDblTaxAmt(0.00);
                    obj.setDblSettlementAmt(rsData.getDouble(7));
                    obj.setDblSubTotal(0.00);
                    obj.setIntBillSeriesPaxNo(0);
                }
                else
                {
                    obj.setStrBillNo(rsData.getString(1));
                    obj.setDteBillDate(rsData.getString(2));
                    obj.setStrPosName(rsData.getString(3));
                    obj.setStrSettelmentMode(rsData.getString(4));
                    obj.setDblDiscountAmt(rsData.getDouble(5));
                    obj.setDblTaxAmt(rsData.getDouble(6));
                    obj.setDblSettlementAmt(rsData.getDouble(7));
                    obj.setDblSubTotal(rsData.getDouble(8));
                    obj.setIntBillSeriesPaxNo(rsData.getInt(10));
                    obj.setStrItemCode(rsData.getString(11));
                    obj.setStrDiscType(rsData.getString(12));
                    obj.setStrItemName(rsData.getString(13));

                    if (obj.getDblSubTotal() > 0)
                    {
                        obj.setDblDiscountPer((obj.getDblDiscountAmt() / obj.getDblSubTotal()) * 100);
                    }
                }
                listOfBillData.add(obj);

                if (rsData.getString(9).equalsIgnoreCase("MultiSettle"))
                {
                    mapMultiSettleBills.put(key, rsData.getString(1));
                }
            }

            //Bill detail data
            sqlBuilder.setLength(0);
            sqlBuilder.append("SELECT a.strBillNo "
                    + ",DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y') AS BillDate, DATE_FORMAT(DATE(a.dteModifyVoidBill),'%d-%m-%Y') AS VoidedDate "
                    + ",TIME(a.dteBillDate) AS EntryTime, TIME(a.dteModifyVoidBill) VoidedTime, a.dblModifiedAmount AS BillAmount "
                    + ",a.strReasonName AS Reason,a.strUserEdited AS UserEdited,a.strUserCreated,a.strRemark "
                    + " from tblvoidbillhd a,tblvoidbilldtl b "
                    + " where a.strBillNo=b.strBillNo "
                    + " and b.strTransType='VB' "
                    + " and a.strTransType='VB' "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " and Date(a.dteModifyVoidBill)  Between '" + fromDate + "' and '" + toDate + "' ");
            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and a.strPosCode='" + posCode + "' ");
            }
            if (!shiftNo.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and a.intShiftCode='" + shiftNo + "'  ");
            }
            sqlBuilder.append(" group by a.dteBillDate,a.strBillNo ");

            ResultSet rsVoidData = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
            List<clsVoidBillDtl> listOfVoidBillData = new ArrayList<clsVoidBillDtl>();
            while (rsVoidData.next())
            {
                clsVoidBillDtl objVoidBill = new clsVoidBillDtl();
                objVoidBill.setStrBillNo(rsVoidData.getString(1));          //BillNo
                objVoidBill.setDteBillDate(rsVoidData.getString(2));        //Bill Date
                objVoidBill.setStrWaiterNo(rsVoidData.getString(3));        //Voided Date
                objVoidBill.setStrTableNo(rsVoidData.getString(4));         //Entry Time
                objVoidBill.setStrSettlementCode(rsVoidData.getString(5));  //Voided Time
                objVoidBill.setDblAmount(rsVoidData.getDouble(6));          //Bill Amount
                objVoidBill.setStrReasonName(rsVoidData.getString(7));      //Reason
                objVoidBill.setStrClientCode(rsVoidData.getString(8));      //User Edited
                objVoidBill.setStrUserCreated(rsVoidData.getString(9));     //User Created
                objVoidBill.setStrRemarks(rsVoidData.getString(10));         //Remarks   

                listOfVoidBillData.add(objVoidBill);
            }
            rsVoidData.close();

            Comparator<clsBillItemDtlBean> settlementModeComparator = new Comparator<clsBillItemDtlBean>()
            {

                @Override
                public int compare(clsBillItemDtlBean o1, clsBillItemDtlBean o2)
                {
                    return o2.getStrSettelmentMode().compareToIgnoreCase(o1.getStrSettelmentMode());
                }
            };

            Comparator<clsBillItemDtlBean> billDateComparator = new Comparator<clsBillItemDtlBean>()
            {

                @Override
                public int compare(clsBillItemDtlBean o1, clsBillItemDtlBean o2)
                {
                    return o2.getDteBillDate().compareToIgnoreCase(o1.getDteBillDate());
                }
            };

            Comparator<clsBillItemDtlBean> billNoComparator = new Comparator<clsBillItemDtlBean>()
            {

                @Override
                public int compare(clsBillItemDtlBean o1, clsBillItemDtlBean o2)
                {
                    return o1.getStrBillNo().compareToIgnoreCase(o2.getStrBillNo());
                }
            };

            Collections.sort(listOfBillData, new clsBillComparator(settlementModeComparator, billDateComparator, billNoComparator));

            hm.put("listOfVoidBillData", listOfVoidBillData);
            //call for view report
            if (reportType.equalsIgnoreCase("A4 Size Report"))
            {
                funViewJasperReportForBeanCollectionDataSource(is, hm, listOfBillData);
            }
            if (reportType.equalsIgnoreCase("Excel Report"))
            {
                Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
                List<String> arrListTotal = new ArrayList<String>();
                List<String> arrHeaderList = new ArrayList<String>();
                double totalSubTotal = 0;
                double totalTaxAmt = 0;
                double totalGrandAmount = 0;
                double discountTotal = 0;
                int i = 1;
                for (clsBillItemDtlBean objBean : listOfBillData)
                {
                    List<String> arrListItem = new ArrayList<String>();
                    arrListItem.add(objBean.getStrBillNo());
                    arrListItem.add(objBean.getStrSettelmentMode());
                    arrListItem.add(objBean.getStrItemCode());
                    arrListItem.add(gDecimalFormat.format(objBean.getDblSubTotal()));
                    arrListItem.add(String.valueOf(Math.rint(objBean.getDblDiscountPer())));
                    arrListItem.add(gDecimalFormat.format(objBean.getDblDiscountAmt()));
                    arrListItem.add(gDecimalFormat.format(objBean.getDblTaxAmt()));
                    arrListItem.add(gDecimalFormat.format(objBean.getDblSettlementAmt()));
                    arrListItem.add(objBean.getStrDiscType());
                    arrListItem.add(objBean.getStrItemName());

                    totalTaxAmt = totalTaxAmt + Double.parseDouble(gDecimalFormat.format(objBean.getDblTaxAmt()));
                    totalGrandAmount = totalGrandAmount + Double.parseDouble(gDecimalFormat.format(objBean.getDblSettlementAmt()));
                    discountTotal = discountTotal + Double.parseDouble(gDecimalFormat.format(objBean.getDblDiscountAmt()));
                    totalSubTotal = totalSubTotal + Double.parseDouble(gDecimalFormat.format(objBean.getDblSubTotal()));//subTotal
                    mapExcelItemDtl.put(i, arrListItem);
                    i++;
                }
                arrListTotal.add(gDecimalFormat.format(totalSubTotal) + "#" + "4");
                arrListTotal.add("" + "#" + "5");
                arrListTotal.add(gDecimalFormat.format(discountTotal) + "#" + "6");
                arrListTotal.add(gDecimalFormat.format(totalTaxAmt) + "#" + "7");
                arrListTotal.add(gDecimalFormat.format(totalGrandAmount) + "#" + "8");

                arrHeaderList.add("Serial No");
                arrHeaderList.add("Bill No");
                arrHeaderList.add("Settlement");
                arrHeaderList.add("Table");
                arrHeaderList.add("Taxable");
                arrHeaderList.add("Disc %");
                arrHeaderList.add("Disc Amt");
                arrHeaderList.add("Tax Amt");
                arrHeaderList.add("Bill Amt");
                arrHeaderList.add("User");
                arrHeaderList.add("Customer");

                List<String> arrparameterList = new ArrayList<String>();
                arrparameterList.add("Daily Collection Report");
                arrparameterList.add("POS" + " : " + posName);
                arrparameterList.add("From Date" + " : " + fromDateToDisplay);
                arrparameterList.add("To Date" + " : " + toDateToDisplay);
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
                funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "dailyCollectionExcelSheet", dayEnd);
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
            if (pages.size() == 0)
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

            if (!dayEnd.equalsIgnoreCase("Yes"))
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
