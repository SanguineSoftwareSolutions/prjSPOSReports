/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.POSReport.controller;

import com.POSGlobal.controller.clsBillDtl;
import com.POSGlobal.controller.clsGlobalVarClass;
import java.awt.Desktop;
import java.awt.Dimension;
import java.io.File;
import java.io.InputStream;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
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

public class clsGuestCreditReport
{
    DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();
    public void funGuestCreditReport(String reportType, HashMap hm, String dayEnd)
    {
        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptGuestCreditReport.jasper");

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();

            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String posName = hm.get("posName").toString();
            SimpleDateFormat sdfr = new SimpleDateFormat("dd-MMM-yyyy");

            StringBuilder sqlLiveBuilder = new StringBuilder();
            StringBuilder sqlQBuilder = new StringBuilder();

            sqlLiveBuilder.append("select a.strBillNo,a.strItemCode,a.strItemName,a.dblRate,a.dblQuantity,a.dblAmount,date(a.dteBillDate) "
                    + ",h.strPosName,d.strSettelmentDesc,a.strKOTNo,b.strPOSCode,b.strRemarks,ifnull(e.strTableName,'') as strTableName"
                    + ",f.strCustomerName,ifnull(g.strWShortName,'') as strWShortName,b.dblDeliveryCharges "
                    + ",a.dblDiscountAmt,a.dblTaxAmount,(a.dblAmount-a.dblDiscountAmt+a.dblTaxAmount)GrandTotal "
                    + ",f.longMobileNo,i.strReasonName "
                    + "from tblbilldtl a "
                    + "left outer join tblbillhd b on a.strBillNo=b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate) "
                    + "left outer join tblbillsettlementdtl c on a.strBillNo=c.strBillNo and date(a.dteBillDate)=date(c.dteBillDate) "
                    + "left outer join tblsettelmenthd d on c.strSettlementCode=d.strSettelmentCode "
                    + "left outer join tbltablemaster e on b.strTableNo=e.strTableNo "
                    + "left outer join tblcustomermaster f on c.strCustomerCode=f.strCustomerCode "
                    + "left outer join tblwaitermaster g on b.strWaiterNo=g.strWaiterNo "
                    + "left outer join tblposmaster h on b.strPOSCode=h.strPosCode "
                    + "left outer join tblreasonmaster i on b.strReasonCode=i.strReasonCode "
                    + "where date(a.dteBillDate) Between '" + fromDate + "' and '" + toDate + "'  "
                    + "and d.strSettelmentType='Credit' ");

            sqlQBuilder.append("select a.strBillNo,a.strItemCode,a.strItemName,a.dblRate,a.dblQuantity,a.dblAmount,date(a.dteBillDate) "
                    + ",h.strPosName,d.strSettelmentDesc,a.strKOTNo,b.strPOSCode,b.strRemarks,ifnull(e.strTableName,'') as strTableName"
                    + ",f.strCustomerName,ifnull(g.strWShortName,'') as strWShortName,b.dblDeliveryCharges "
                    + ",a.dblDiscountAmt,a.dblTaxAmount,(a.dblAmount-a.dblDiscountAmt+a.dblTaxAmount)GrandTotal "
                    + ",f.longMobileNo,i.strReasonName "
                    + "from tblqbilldtl a "
                    + "left outer join tblqbillhd b on a.strBillNo=b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate) "
                    + "left outer join tblqbillsettlementdtl c on a.strBillNo=c.strBillNo  and date(a.dteBillDate)=date(c.dteBillDate) "
                    + "left outer join tblsettelmenthd d on c.strSettlementCode=d.strSettelmentCode "
                    + "left outer join tbltablemaster e on b.strTableNo=e.strTableNo "
                    + "left outer join tblcustomermaster f on c.strCustomerCode=f.strCustomerCode "
                    + "left outer join tblwaitermaster g on b.strWaiterNo=g.strWaiterNo "
                    + "left outer join tblposmaster h on b.strPOSCode=h.strPosCode "
                    + "left outer join tblreasonmaster i on b.strReasonCode=i.strReasonCode "
                    + "where date(a.dteBillDate) Between '" + fromDate + "' and '" + toDate + "'  "
                    + "and d.strSettelmentType='Credit' ");

            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlLiveBuilder.append("and b.strPOSCode='" + posCode + "' ");
                sqlQBuilder.append("and b.strPOSCode='" + posCode + "' ");
            }

            sqlLiveBuilder.append("group by b.strPOSCode,a.strBillNo,a.strKOTNo,a.strItemCode "
                    + "order by b.strPOSCode,a.strBillNo,a.strKOTNo,a.strItemCode ");
            sqlQBuilder.append("group by b.strPOSCode,a.strBillNo,a.strKOTNo,a.strItemCode "
                    + "order by b.strPOSCode,a.strBillNo,a.strKOTNo,a.strItemCode ");

            List<clsBillDtl> listOfGuestCreditData = new ArrayList<>();

            //live
            ResultSet rsData = clsGlobalVarClass.dbMysql.executeResultSet(sqlLiveBuilder.toString());
            while (rsData.next())
            {
                clsBillDtl obj = new clsBillDtl();
                String dteBillDate = "";
                String billDate = rsData.getString(7);
                String dateParts[] = billDate.split("-");
                dteBillDate = dateParts[2] + "-" + dateParts[1] + "-" + dateParts[0];

                obj.setStrBillNo(rsData.getString(1));
                obj.setStrItemCode(rsData.getString(2));
                obj.setStrItemName(rsData.getString(3));
                obj.setDblRate(rsData.getDouble(4));
                obj.setDblQuantity(rsData.getDouble(5));
                obj.setDblAmount(rsData.getDouble(6));
                obj.setDteBillDate(dteBillDate);
                obj.setStrPosName(rsData.getString(8));
                obj.setStrSettlementName(rsData.getString(9));
                obj.setStrKOTNo(rsData.getString(10));
                obj.setStrPOSCode(rsData.getString(11));
                obj.setStrRemarks(rsData.getString(12));
                obj.setStrTableName(rsData.getString(13));
                obj.setStrCustomerName(rsData.getString(14));
                obj.setStrWShortName(rsData.getString(15));
                obj.setDblDelCharges(rsData.getDouble(16));
                obj.setDblDiscountAmt(rsData.getDouble(17));//disc
                obj.setDblTaxAmount(rsData.getDouble(18));//tax
                obj.setDblBillAmt(rsData.getDouble(19));//grandtotal
                obj.setLongMobileNo(rsData.getLong(20));
                obj.setStrReasonName(rsData.getString(21));

                listOfGuestCreditData.add(obj);
            }
            rsData.close();;
            //Q
            rsData = clsGlobalVarClass.dbMysql.executeResultSet(sqlQBuilder.toString());
            while (rsData.next())
            {
                clsBillDtl obj = new clsBillDtl();
                String dteBillDate = "";
                String billDate = rsData.getString(7);
                String dateParts[] = billDate.split("-");
                dteBillDate = dateParts[2] + "-" + dateParts[1] + "-" + dateParts[0];

                obj.setStrBillNo(rsData.getString(1));
                obj.setStrItemCode(rsData.getString(2));
                obj.setStrItemName(rsData.getString(3));
                obj.setDblRate(rsData.getDouble(4));
                obj.setDblQuantity(rsData.getDouble(5));
                obj.setDblAmount(rsData.getDouble(6));
                obj.setDteBillDate(dteBillDate);
                obj.setStrPosName(rsData.getString(8));
                obj.setStrSettlementName(rsData.getString(9));
                obj.setStrKOTNo(rsData.getString(10));
                obj.setStrPOSCode(rsData.getString(11));
                obj.setStrRemarks(rsData.getString(12));
                obj.setStrTableName(rsData.getString(13));
                obj.setStrCustomerName(rsData.getString(14));
                obj.setStrWShortName(rsData.getString(15));
                obj.setDblDelCharges(rsData.getDouble(16));
                obj.setDblDiscountAmt(rsData.getDouble(17));//disc
                obj.setDblTaxAmount(rsData.getDouble(18));//tax
                obj.setDblBillAmt(rsData.getDouble(19));//grandtotal
                obj.setLongMobileNo(rsData.getLong(20));
                obj.setStrReasonName(rsData.getString(21));

                listOfGuestCreditData.add(obj);
            }
            rsData.close();
            //call for view report
            if (reportType.equalsIgnoreCase("A4 Size Report"))
            {
                funViewJasperReportForBeanCollectionDataSource(is, hm, listOfGuestCreditData);
            }
            if (reportType.equalsIgnoreCase("Excel Report"))
            {
                Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
                List<String> arrListTotal = new ArrayList<String>();
                List<String> arrHeaderList = new ArrayList<String>();
                double totalQty = 0;
                double totalAmt = 0;
                double totalDis = 0;
                double totalDelCharges = 0, totalDisc = 0, totalTax = 0, grandTotal = 0;

                int i = 1;
                String prevBillNo = "";
                for (clsBillDtl objtemp : listOfGuestCreditData)
                {
                    List<String> arrListItem = new ArrayList<String>();
                    if (prevBillNo.equalsIgnoreCase(objtemp.getStrBillNo()))
                    {
                        arrListItem.add("");
                        arrListItem.add("");
                        arrListItem.add("");
                        arrListItem.add("");
                        arrListItem.add(objtemp.getStrItemName());
                        arrListItem.add(objtemp.getStrKOTNo());
                        arrListItem.add(String.valueOf(objtemp.getDblQuantity()));
                        arrListItem.add(String.valueOf(gDecimalFormat.format(objtemp.getDblAmount())));
                        arrListItem.add(String.valueOf(gDecimalFormat.format(objtemp.getDblDiscountAmt())));
                        arrListItem.add(String.valueOf(gDecimalFormat.format(objtemp.getDblTaxAmount())));
                        arrListItem.add(String.valueOf(gDecimalFormat.format(objtemp.getDblDelCharges())));
                        arrListItem.add(String.valueOf(gDecimalFormat.format(objtemp.getDblBillAmt())));
                        arrListItem.add("");
                        arrListItem.add("");
                        prevBillNo = objtemp.getStrBillNo();
                    }
                    else
                    {
                        arrListItem.add(objtemp.getStrBillNo());
                        arrListItem.add(objtemp.getDteBillDate());
                        arrListItem.add(objtemp.getStrCustomerName());
                        arrListItem.add(String.valueOf(objtemp.getLongMobileNo()));
                        arrListItem.add(objtemp.getStrItemName());
                        arrListItem.add(objtemp.getStrKOTNo());
                        arrListItem.add(String.valueOf(objtemp.getDblQuantity()));
                        arrListItem.add(String.valueOf(gDecimalFormat.format(objtemp.getDblAmount())));
                        arrListItem.add(String.valueOf(gDecimalFormat.format(objtemp.getDblDiscountAmt())));
                        arrListItem.add(String.valueOf(gDecimalFormat.format(objtemp.getDblTaxAmount())));
                        arrListItem.add(String.valueOf(gDecimalFormat.format(objtemp.getDblDelCharges())));
                        arrListItem.add(String.valueOf(gDecimalFormat.format(objtemp.getDblBillAmt())));
                        arrListItem.add(String.valueOf(objtemp.getStrRemarks()));
                        arrListItem.add(String.valueOf(objtemp.getStrReasonName()));
                        prevBillNo = objtemp.getStrBillNo();
                    }
                    totalQty = totalQty + objtemp.getDblQuantity();
                    totalAmt = totalAmt + objtemp.getDblAmount();
                    totalDelCharges = totalDelCharges + objtemp.getDblDelCharges();
                    totalDisc = totalDisc + objtemp.getDblDiscountAmt();
                    totalTax = totalTax + objtemp.getDblTaxAmount();
                    grandTotal = grandTotal + objtemp.getDblBillAmt();

                    mapExcelItemDtl.put(i, arrListItem);

                    i++;
                }
                arrListTotal.add(String.valueOf(Math.rint(totalQty)) + "#" + "7");
                arrListTotal.add(String.valueOf(gDecimalFormat.format(totalAmt)) + "#" + "8");
                arrListTotal.add(String.valueOf(gDecimalFormat.format(totalDis)) + "#" + "9");
                arrListTotal.add(String.valueOf(gDecimalFormat.format(totalTax)) + "#" + "10");
                arrListTotal.add(String.valueOf(gDecimalFormat.format(totalDelCharges)) + "#" + "11");
                arrListTotal.add(String.valueOf(gDecimalFormat.format(grandTotal)) + "#" + "12");

                arrHeaderList.add("Serial No");
                arrHeaderList.add("Bill No");
                arrHeaderList.add("Bill Date");
                arrHeaderList.add("Customer Name");
                arrHeaderList.add("Mobile No");
                arrHeaderList.add("Item Name");
                arrHeaderList.add("KOT No");
                arrHeaderList.add("Qty");
                arrHeaderList.add("Sub Total");
                arrHeaderList.add("Discount");
                arrHeaderList.add("Tax");
                arrHeaderList.add("Del. Charges");
                arrHeaderList.add("Grand Total");
                arrHeaderList.add("Remark");
                arrHeaderList.add("Reason");

                List<String> arrparameterList = new ArrayList<String>();
                arrparameterList.add("Guest Credit Report");
                arrparameterList.add("POS" + " : " + posName);
                arrparameterList.add("FromDate" + " : " + fromDate);
                arrparameterList.add("ToDate" + " : " + toDate);
                arrparameterList.add(" ");
                arrparameterList.add(" ");
                if (clsGlobalVarClass.gEnableShiftYN)
                {
                    if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.toString().equalsIgnoreCase("All")))
                    {
                        arrparameterList.add("Shift No " + " : " + shiftNo.toString());
                    }
                    else
                    {
                        arrparameterList.add("Shift No " + " : " + shiftNo.toString());
                    }
                }

                funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "GuestCreditExcelSheet", dayEnd);
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
