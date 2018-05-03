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

/**
 *
 * @author Harry
 */
public class clsItemWiseSalesReport
{
    DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();
    public void funGenerateItemWiseReport(String reportType, HashMap hm, String dayEnd)
    {
        try
        {
            String reportName = "com/POSReport/reports/rptitemWiseSalesReport.jasper";
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String posName = hm.get("posName").toString();
            String printComplimentaryYN = hm.get("printComplimentaryYN").toString();

            String sqlFilters = "";

            String sqlLive = "select a.strItemCode,a.strItemName,c.strPOSName"
                    + ",sum(a.dblQuantity),sum(a.dblTaxAmount)\n"
                    + ",sum(a.dblAmount),sum(a.dblAmount)-sum(a.dblDiscountAmt),sum(a.dblDiscountAmt),DATE_FORMAT(date(a.dteBillDate),'%d-%m-%Y'),'" + clsGlobalVarClass.gUserCode + "'\n"
                    + "from tblbilldtl a,tblbillhd b,tblposmaster c\n"
                    + "where a.strBillNo=b.strBillNo "
                    + "AND DATE(a.dteBillDate)=DATE(b.dteBillDate) "
                    + "and b.strPOSCode=c.strPosCode "
                    + "and date( b.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
                    + " and a.strClientCode=b.strClientCode ";

            String sqlLiveCompli = "select a.strItemCode,a.strItemName,c.strPOSName"
                    + ",sum(a.dblQuantity),sum(a.dblTaxAmount)\n"
                    + ",sum(a.dblAmount),sum(a.dblAmount)-sum(a.dblDiscountAmt),sum(a.dblDiscountAmt),DATE_FORMAT(date(a.dteBillDate),'%d-%m-%Y'),'" + clsGlobalVarClass.gUserCode + "'\n"
                    + "from tblbillcomplementrydtl a,tblbillhd b,tblposmaster c\n"
                    + "where a.strBillNo=b.strBillNo "
                    + "AND DATE(a.dteBillDate)=DATE(b.dteBillDate) "
                    + "and b.strPOSCode=c.strPosCode "
                    + "and date( b.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
                    + " and a.strClientCode=b.strClientCode ";

            String sqlQFile = "select a.strItemCode,a.strItemName,c.strPOSName"
                    + ",sum(a.dblQuantity),sum(a.dblTaxAmount)\n"
                    + ",sum(a.dblAmount),sum(a.dblAmount)-sum(a.dblDiscountAmt),sum(a.dblDiscountAmt),DATE_FORMAT(date(a.dteBillDate),'%d-%m-%Y'),'" + clsGlobalVarClass.gUserCode + "'\n"
                    + "from tblqbilldtl a,tblqbillhd b,tblposmaster c\n"
                    + "where a.strBillNo=b.strBillNo "
                    + "AND DATE(a.dteBillDate)=DATE(b.dteBillDate) "
                    + "and b.strPOSCode=c.strPosCode "
                    + "and date( b.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
                    + " and a.strClientCode=b.strClientCode ";
            String sqlQCompli = "select a.strItemCode,a.strItemName,c.strPOSName"
                    + ",sum(a.dblQuantity),sum(a.dblTaxAmount)\n"
                    + ",sum(a.dblAmount),sum(a.dblAmount)-sum(a.dblDiscountAmt),sum(a.dblDiscountAmt),DATE_FORMAT(date(a.dteBillDate),'%d-%m-%Y'),'" + clsGlobalVarClass.gUserCode + "'\n"
                    + "from tblqbillcomplementrydtl a,tblqbillhd b,tblposmaster c\n"
                    + "where a.strBillNo=b.strBillNo "
                    + "AND DATE(a.dteBillDate)=DATE(b.dteBillDate) "
                    + "and b.strPOSCode=c.strPosCode "
                    + "and date( b.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
                    + " and a.strClientCode=b.strClientCode ";

            String sqlModLive = "select a.strItemCode,a.strModifierName,c.strPOSName"
                    + ",sum(a.dblQuantity),'0',sum(a.dblAmount),sum(a.dblAmount)-sum(a.dblDiscAmt),sum(a.dblDiscAmt),DATE_FORMAT(date(b.dteBillDate),'%d-%m-%Y'),'" + clsGlobalVarClass.gUserCode + "'\n"
                    + "from tblbillmodifierdtl a,tblbillhd b,tblposmaster c\n"
                    + "where a.strBillNo=b.strBillNo "
                    + "AND DATE(a.dteBillDate)=DATE(b.dteBillDate) "
                    + "and b.strPOSCode=c.strPosCode "
                    + "and a.dblamount>0 \n"
                    + "and date( b.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "'"
                    + " and a.strClientCode=b.strClientCode  ";

            String sqlModQFile = "select a.strItemCode,a.strModifierName,c.strPOSName"
                    + ",sum(a.dblQuantity),'0',sum(a.dblAmount),sum(a.dblAmount)-sum(a.dblDiscAmt),sum(a.dblDiscAmt),DATE_FORMAT(date(b.dteBillDate),'%d-%m-%Y'),'" + clsGlobalVarClass.gUserCode + "'\n"
                    + "from tblqbillmodifierdtl a,tblqbillhd b,tblposmaster c\n"
                    + "where a.strBillNo=b.strBillNo "
                    + "AND DATE(a.dteBillDate)=DATE(b.dteBillDate) "
                    + "and b.strPOSCode=c.strPosCode "
                    + "and a.dblamount>0 \n"
                    + "and date( b.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "'"
                    + "and a.strClientCode=b.strClientCode  ";

            if (!posCode.equals("All"))
            {
                sqlFilters += " AND b.strPOSCode = '" + posCode + "' ";
            }
            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sqlFilters += " AND b.intShiftCode = '" + shiftNo + "' ";
                }
            }

//                sqlFilters += " GROUP BY a.strItemCode";
            sqlLive = sqlLive + " " + sqlFilters + "  GROUP BY a.strItemCode,a.strItemName ";
            sqlLiveCompli = sqlLiveCompli + " " + sqlFilters + "  GROUP BY a.strItemCode,a.strItemName ";
            sqlQFile = sqlQFile + " " + sqlFilters + "  GROUP BY a.strItemCode,a.strItemName ";
            sqlQCompli = sqlQCompli + " " + sqlFilters + "  GROUP BY a.strItemCode,a.strItemName ";

            sqlModLive = sqlModLive + " " + sqlFilters + "  GROUP BY a.strItemCode,a.strModifierName ";
            sqlModQFile = sqlModQFile + " " + sqlFilters + "  GROUP BY a.strItemCode,a.strModifierName ";

            Map<String, clsBillItemDtlBean> mapItemdtl = new HashMap<>();

            ResultSet rsData = clsGlobalVarClass.dbMysql.executeResultSet(sqlLive.toString());
            while (rsData.next())
            {
                String itemCode = rsData.getString(1);
                if (mapItemdtl.containsKey(itemCode))
                {
                    clsBillItemDtlBean obj = mapItemdtl.get(itemCode);

                    obj.setDblQuantity(obj.getDblQuantity() + rsData.getDouble(4));
                    obj.setDblTaxAmt(obj.getDblTaxAmt() + rsData.getDouble(5));
                    obj.setDblAmount(obj.getDblAmount() + rsData.getDouble(6));
                    obj.setDblSubTotal(obj.getDblSubTotal() + rsData.getDouble(7));
                    obj.setDblDiscountAmt(obj.getDblDiscountAmt() + rsData.getDouble(8));

                }
                else
                {
                    clsBillItemDtlBean obj = new clsBillItemDtlBean();

                    obj.setStrItemCode(rsData.getString(1));
                    obj.setStrItemName(rsData.getString(2));
                    obj.setStrPosName(rsData.getString(3));
                    obj.setDblQuantity(rsData.getDouble(4));
                    obj.setDblTaxAmt(rsData.getDouble(5));
                    obj.setDblAmount(rsData.getDouble(6));
                    obj.setDblSubTotal(rsData.getDouble(7));
                    obj.setDblDiscountAmt(rsData.getDouble(8));
                    obj.setDteBillDate(rsData.getString(9));

                    mapItemdtl.put(itemCode, obj);
                }
            }
            rsData.close();

            rsData = clsGlobalVarClass.dbMysql.executeResultSet(sqlQFile.toString());
            while (rsData.next())
            {
                String itemCode = rsData.getString(1);
                if (mapItemdtl.containsKey(itemCode))
                {
                    clsBillItemDtlBean obj = mapItemdtl.get(itemCode);

                    obj.setDblQuantity(obj.getDblQuantity() + rsData.getDouble(4));
                    obj.setDblTaxAmt(obj.getDblTaxAmt() + rsData.getDouble(5));
                    obj.setDblAmount(obj.getDblAmount() + rsData.getDouble(6));
                    obj.setDblSubTotal(obj.getDblSubTotal() + rsData.getDouble(7));
                    obj.setDblDiscountAmt(obj.getDblDiscountAmt() + rsData.getDouble(8));

                }
                else
                {
                    clsBillItemDtlBean obj = new clsBillItemDtlBean();

                    obj.setStrItemCode(rsData.getString(1));
                    obj.setStrItemName(rsData.getString(2));
                    obj.setStrPosName(rsData.getString(3));
                    obj.setDblQuantity(rsData.getDouble(4));
                    obj.setDblTaxAmt(rsData.getDouble(5));
                    obj.setDblAmount(rsData.getDouble(6));
                    obj.setDblSubTotal(rsData.getDouble(7));
                    obj.setDblDiscountAmt(rsData.getDouble(8));
                    obj.setDteBillDate(rsData.getString(9));

                    mapItemdtl.put(itemCode, obj);
                }
            }
            rsData.close();

            rsData = clsGlobalVarClass.dbMysql.executeResultSet(sqlModLive.toString());
            while (rsData.next())
            {
                String itemCode = rsData.getString(1);
                if (mapItemdtl.containsKey(itemCode))
                {
                    clsBillItemDtlBean obj = mapItemdtl.get(itemCode);

                    obj.setDblQuantity(obj.getDblQuantity() + rsData.getDouble(4));
                    obj.setDblTaxAmt(obj.getDblTaxAmt() + rsData.getDouble(5));
                    obj.setDblAmount(obj.getDblAmount() + rsData.getDouble(6));
                    obj.setDblSubTotal(obj.getDblSubTotal() + rsData.getDouble(7));
                    obj.setDblDiscountAmt(obj.getDblDiscountAmt() + rsData.getDouble(8));

                }
                else
                {
                    clsBillItemDtlBean obj = new clsBillItemDtlBean();

                    obj.setStrItemCode(rsData.getString(1));
                    obj.setStrItemName(rsData.getString(2));
                    obj.setStrPosName(rsData.getString(3));
                    obj.setDblQuantity(rsData.getDouble(4));
                    obj.setDblTaxAmt(rsData.getDouble(5));
                    obj.setDblAmount(rsData.getDouble(6));
                    obj.setDblSubTotal(rsData.getDouble(7));
                    obj.setDblDiscountAmt(rsData.getDouble(8));
                    obj.setDteBillDate(rsData.getString(9));

                    mapItemdtl.put(itemCode, obj);
                }
            }
            rsData.close();

            rsData = clsGlobalVarClass.dbMysql.executeResultSet(sqlModQFile.toString());
            while (rsData.next())
            {
                String itemCode = rsData.getString(1);
                if (mapItemdtl.containsKey(itemCode))
                {
                    clsBillItemDtlBean obj = mapItemdtl.get(itemCode);

                    obj.setDblQuantity(obj.getDblQuantity() + rsData.getDouble(4));
                    obj.setDblTaxAmt(obj.getDblTaxAmt() + rsData.getDouble(5));
                    obj.setDblAmount(obj.getDblAmount() + rsData.getDouble(6));
                    obj.setDblSubTotal(obj.getDblSubTotal() + rsData.getDouble(7));
                    obj.setDblDiscountAmt(obj.getDblDiscountAmt() + rsData.getDouble(8));

                }
                else
                {
                    clsBillItemDtlBean obj = new clsBillItemDtlBean();

                    obj.setStrItemCode(rsData.getString(1));
                    obj.setStrItemName(rsData.getString(2));
                    obj.setStrPosName(rsData.getString(3));
                    obj.setDblQuantity(rsData.getDouble(4));
                    obj.setDblTaxAmt(rsData.getDouble(5));
                    obj.setDblAmount(rsData.getDouble(6));
                    obj.setDblSubTotal(rsData.getDouble(7));
                    obj.setDblDiscountAmt(rsData.getDouble(8));
                    obj.setDteBillDate(rsData.getString(9));

                    mapItemdtl.put(itemCode, obj);
                }
            }
            rsData.close();

            /**
             * substract compli qty
             */
            if (printComplimentaryYN.equalsIgnoreCase("No"))
            {
                hm.put("Note", "Note:Report does not include complimentary quantities.");

                rsData = clsGlobalVarClass.dbMysql.executeResultSet(sqlLiveCompli.toString());
                while (rsData.next())
                {
                    String itemCode = rsData.getString(1);
                    if (mapItemdtl.containsKey(itemCode))
                    {
                        clsBillItemDtlBean obj = mapItemdtl.get(itemCode);

                        obj.setDblQuantity(obj.getDblQuantity() - rsData.getDouble(4));
                    }
                }
                rsData.close();

                rsData = clsGlobalVarClass.dbMysql.executeResultSet(sqlQCompli.toString());
                while (rsData.next())
                {
                    String itemCode = rsData.getString(1);
                    if (mapItemdtl.containsKey(itemCode))
                    {
                        clsBillItemDtlBean obj = mapItemdtl.get(itemCode);

                        obj.setDblQuantity(obj.getDblQuantity() - rsData.getDouble(4));
                    }
                }
                rsData.close();
            }
            else
            {
                hm.put("Note", "Note:Report contains complimentary quantities.");
            }

            Comparator<clsBillItemDtlBean> itemCodeComparator = new Comparator<clsBillItemDtlBean>()
            {

                @Override
                public int compare(clsBillItemDtlBean o1, clsBillItemDtlBean o2)
                {
                    return o1.getStrItemCode().substring(0, 7).compareToIgnoreCase(o2.getStrItemCode().substring(0, 7));
                }
            };

            List<clsBillItemDtlBean> listOfItemData = new ArrayList<clsBillItemDtlBean>();
            for (clsBillItemDtlBean objItemDtlBean : mapItemdtl.values())
            {
                listOfItemData.add(objItemDtlBean);
            }

            Collections.sort(listOfItemData, itemCodeComparator);

            //call for view report
            if (reportType.equalsIgnoreCase("A4 Size Report"))
            {
                funViewJasperReportForBeanCollectionDataSource(is, hm, listOfItemData);
            }
            if (reportType.equalsIgnoreCase("Excel Report"))
            {
                double totalQty = 0;
                double totalAmount = 0;
                double subTotal = 0;
                double discountTotal = 0;
                Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
                List<String> arrListTotal = new ArrayList<String>();
                List<String> arrHeaderList = new ArrayList<String>();
                int i = 1;
                for (int cnt = 0; cnt < listOfItemData.size(); cnt++)
                {
                    List<String> arrListItem = new ArrayList<String>();
                    clsBillItemDtlBean obj = listOfItemData.get(cnt);

                    arrListItem.add(obj.getStrItemName());
                    arrListItem.add(obj.getStrPosName());
                    arrListItem.add(obj.getDteBillDate());
                    arrListItem.add("" + obj.getDblQuantity());
                    arrListItem.add("" +gDecimalFormat.format(obj.getDblAmount()));
                    arrListItem.add("" +gDecimalFormat.format( obj.getDblDiscountAmt()));
                    arrListItem.add("" +gDecimalFormat.format( obj.getDblSubTotal()));

                    totalQty = totalQty + obj.getDblQuantity();
                    totalAmount = totalAmount + obj.getDblAmount();
                    subTotal = subTotal + obj.getDblSubTotal();
                    discountTotal = discountTotal + obj.getDblDiscountAmt();
                    mapExcelItemDtl.put(i, arrListItem);
                    i++;

                }

                arrListTotal.add(String.valueOf(totalQty) + "#" + "4");
                arrListTotal.add(String.valueOf(gDecimalFormat.format(totalAmount)) + "#" + "5");
                arrListTotal.add(String.valueOf(gDecimalFormat.format(subTotal)) + "#" + "7");
                arrListTotal.add(String.valueOf(gDecimalFormat.format(discountTotal)) + "#" + "6");

                arrHeaderList.add("Serial No");
                arrHeaderList.add("ItemName");
                arrHeaderList.add("POSName");
                arrHeaderList.add("BillDate");
                arrHeaderList.add("Qty");
                arrHeaderList.add("Sub Total"); //Sub Total
                arrHeaderList.add("Discount");
                arrHeaderList.add("Net Total"); //Net Total

                List<String> arrparameterList = new ArrayList<String>();
                arrparameterList.add("ItemWise Report");
                arrparameterList.add("POS" + " : " + posName);
                arrparameterList.add("FromDate" + " : " + fromDate);
                arrparameterList.add("ToDate" + " : " + toDate);
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
                funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "itemWiseExcelSheet", dayEnd);
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
