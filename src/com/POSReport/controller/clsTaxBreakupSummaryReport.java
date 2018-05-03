package com.POSReport.controller;

import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsPosConfigFile;
import com.POSGlobal.controller.clsTaxCalculationDtls;
import com.POSGlobal.controller.clsUtility;
import java.awt.Desktop;
import java.awt.Dimension;
import java.io.File;
import java.io.InputStream;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
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
import net.sf.jasperreports.swing.JRViewer;

public class clsTaxBreakupSummaryReport
{

    private clsUtility objUtility;
    DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();
    public void funTaxBreakupSummaryReport(String reportType, HashMap hm, String dayEnd)
    {
        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptTaxBreakupSummaryReport.jasper");

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String posName = hm.get("posName").toString();
            String fromDateToDisplay = hm.get("fromDateToDisplay").toString();
            String toDateToDisplay = hm.get("toDateToDisplay").toString();

            Map<String, clsTaxCalculationDtls> mapTaxDtl = new HashMap<>();
            StringBuilder sqlTaxBuilder = new StringBuilder();
            StringBuilder sqlMenuBreakupBuilder = new StringBuilder();
            //DecimalFormat gDecimalFormat = new DecimalFormat("0.00");

            //live tax
            sqlTaxBuilder.setLength(0);
            sqlTaxBuilder.append("SELECT b.strTaxCode,c.strTaxDesc,sum(b.dblTaxableAmount) as dblTaxableAmount,sum(b.dblTaxAmount) as dblTaxAmount "
                    + "FROM tblBillHd a "
                    + "INNER JOIN tblBillTaxDtl b ON a.strBillNo = b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate) "
                    + "INNER JOIN tblTaxHd c ON b.strTaxCode = c.strTaxCode "
                    + "LEFT OUTER JOIN tblposmaster d ON a.strposcode=d.strposcode "
                    + "where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlTaxBuilder.append("and a.strPOSCode='" + posCode + "'  ");
            }
            if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
            {
                sqlTaxBuilder.append("and a.intShiftCode='" + shiftNo + "' ");
            }
            sqlTaxBuilder.append("group by c.strTaxCode,c.strTaxDesc ");

            ResultSet rsTaxDtl = clsGlobalVarClass.dbMysql.executeResultSet(sqlTaxBuilder.toString());
            while (rsTaxDtl.next())
            {
                if (mapTaxDtl.containsKey(rsTaxDtl.getString(1)))//taxCode
                {
                    clsTaxCalculationDtls obj = mapTaxDtl.get(rsTaxDtl.getString(1));
                    obj.setTaxableAmount(obj.getTaxableAmount() + rsTaxDtl.getDouble(3));
                    obj.setTaxAmount(obj.getTaxAmount() + rsTaxDtl.getDouble(4));
                }
                else
                {
                    clsTaxCalculationDtls obj = new clsTaxCalculationDtls();

                    obj.setTaxCode(rsTaxDtl.getString(1));
                    obj.setTaxName(rsTaxDtl.getString(2));
                    obj.setTaxableAmount(rsTaxDtl.getDouble(3));
                    obj.setTaxAmount(rsTaxDtl.getDouble(4));

                    mapTaxDtl.put(rsTaxDtl.getString(1), obj);

                }
            }
            //Q tax
            sqlTaxBuilder.setLength(0);
            sqlTaxBuilder.append("SELECT b.strTaxCode,c.strTaxDesc,sum(b.dblTaxableAmount) as dblTaxableAmount,sum(b.dblTaxAmount) as dblTaxAmount "
                    + "FROM tblqBillHd a "
                    + "INNER JOIN tblqBillTaxDtl b ON a.strBillNo = b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate) "
                    + "INNER JOIN tblTaxHd c ON b.strTaxCode = c.strTaxCode "
                    + "LEFT OUTER JOIN tblposmaster d ON a.strposcode=d.strposcode "
                    + "where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlTaxBuilder.append("and a.strPOSCode='" + posCode + "'  ");
            }
            if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
            {
                sqlTaxBuilder.append("and a.intShiftCode='" + shiftNo + "' ");
            }
            sqlTaxBuilder.append("group by c.strTaxCode,c.strTaxDesc ");

            rsTaxDtl = clsGlobalVarClass.dbMysql.executeResultSet(sqlTaxBuilder.toString());
            while (rsTaxDtl.next())
            {
                if (mapTaxDtl.containsKey(rsTaxDtl.getString(1)))//taxCode
                {
                    clsTaxCalculationDtls obj = mapTaxDtl.get(rsTaxDtl.getString(1));
                    obj.setTaxableAmount(obj.getTaxableAmount() + rsTaxDtl.getDouble(3));
                    obj.setTaxAmount(obj.getTaxAmount() + rsTaxDtl.getDouble(4));
                }
                else
                {
                    clsTaxCalculationDtls obj = new clsTaxCalculationDtls();

                    obj.setTaxCode(rsTaxDtl.getString(1));
                    obj.setTaxName(rsTaxDtl.getString(2));
                    obj.setTaxableAmount(rsTaxDtl.getDouble(3));
                    obj.setTaxAmount(rsTaxDtl.getDouble(4));

                    mapTaxDtl.put(rsTaxDtl.getString(1), obj);

                }
            }
            List<clsTaxCalculationDtls> listOfTaxDtl = new LinkedList<>();
            for (clsTaxCalculationDtls objTaxDtl : mapTaxDtl.values())
            {
                listOfTaxDtl.add(objTaxDtl);
            }
            Comparator<clsTaxCalculationDtls> taxNameComparator = new Comparator<clsTaxCalculationDtls>()
            {

                @Override
                public int compare(clsTaxCalculationDtls o1, clsTaxCalculationDtls o2)
                {
                    return o1.getTaxName().compareToIgnoreCase(o2.getTaxName());
                }
            };

            Collections.sort(listOfTaxDtl, taxNameComparator);

            hm.put("listOfTaxDtl", listOfTaxDtl);

            Map<String, clsTaxCalculationDtls> mapMenuBreakupDtl = new HashMap<>();

//            //live menuBreakup
//            sqlMenuBreakupBuilder.setLength(0);
//            sqlMenuBreakupBuilder.append("select d.strItemCode,d.strItemName,sum(d.dblamount)"
//                    + " from tblbillhd a  "
//                    + " left Outer join tblbilltaxdtl b on a.strBillNo=b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate) "
//                    + " left outer join tbltaxhd c on b.strTaxCode=c.strTaxCode "
//                    + " left outer join tblbilldtl d on a.strBillNo=d.strBillNo "
//                    + " where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
//                    + " and b.dblTaxableAmount IS NOT NULL ");
//            if (!posCode.equalsIgnoreCase("All"))
//            {
//                sqlMenuBreakupBuilder.append(" and a.strPOSCode='" + posCode + "' ");
//            }
//            if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
//            {
//                sqlMenuBreakupBuilder.append(" and a.intShiftCode='" + shiftNo + "' ");
//            }
//            sqlMenuBreakupBuilder.append(" group by d.strItemCode,d.strItemName order by d.strItemName ");
//
//            ResultSet rsMenuBreakupDtl = clsGlobalVarClass.dbMysql.executeResultSet(sqlMenuBreakupBuilder.toString());
//            while (rsMenuBreakupDtl.next())
//            {
//                if (mapMenuBreakupDtl.containsKey(rsMenuBreakupDtl.getString(1)))//itemName
//                {
//                    clsTaxCalculationDtls obj = mapTaxDtl.get(rsMenuBreakupDtl.getString(1));
//                    obj.setTaxAmount(obj.getTaxAmount() + rsMenuBreakupDtl.getDouble(3));
//                }
//                else
//                {
//                    clsTaxCalculationDtls obj = new clsTaxCalculationDtls();
//                    obj.setTaxCode(rsMenuBreakupDtl.getString(1));
//                    obj.setTaxName(rsMenuBreakupDtl.getString(2));
//                    obj.setTaxAmount(rsMenuBreakupDtl.getDouble(3));
//                    mapMenuBreakupDtl.put(rsMenuBreakupDtl.getString(1), obj);
//
//                }
//            }
//            rsMenuBreakupDtl.close();
//            //Q menuBreakup
//            sqlMenuBreakupBuilder.setLength(0);
//            sqlMenuBreakupBuilder.append("select d.strItemCode,d.strItemName,sum(d.dblamount)"
//                    + " from tblqbillhd a  "
//                    + " left Outer join tblqbilltaxdtl b on a.strBillNo=b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate) "
//                    + " left outer join tbltaxhd c on b.strTaxCode=c.strTaxCode "
//                    + " left outer join tblqbilldtl d on a.strBillNo=d.strBillNo "
//                    + " where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
//                    + " and b.dblTaxableAmount IS NOT NULL ");
//            if (!posCode.equalsIgnoreCase("All"))
//            {
//                sqlMenuBreakupBuilder.append(" and a.strPOSCode='" + posCode + "' ");
//            }
//            if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
//            {
//                sqlMenuBreakupBuilder.append(" and a.intShiftCode='" + shiftNo + "' ");
//            }
//            sqlMenuBreakupBuilder.append(" group by d.strItemCode,d.strItemName order by d.strItemName ");
//
//            rsMenuBreakupDtl = clsGlobalVarClass.dbMysql.executeResultSet(sqlMenuBreakupBuilder.toString());
//            while (rsMenuBreakupDtl.next())
//            {
//                if (mapMenuBreakupDtl.containsKey(rsMenuBreakupDtl.getString(1)))//itemName
//                {
//                    clsTaxCalculationDtls obj = mapMenuBreakupDtl.get(rsMenuBreakupDtl.getString(1));
//                    obj.setTaxAmount(obj.getTaxAmount() + rsMenuBreakupDtl.getDouble(3));
//                }
//                else
//                {
//                    clsTaxCalculationDtls obj = new clsTaxCalculationDtls();
//                    obj.setTaxCode(rsMenuBreakupDtl.getString(1));
//                    obj.setTaxName(rsMenuBreakupDtl.getString(2));
//                    obj.setTaxAmount(rsMenuBreakupDtl.getDouble(3));
//                    mapMenuBreakupDtl.put(rsMenuBreakupDtl.getString(1), obj);
//
//                }
//            }
//            rsMenuBreakupDtl.close();
            List<clsTaxCalculationDtls> listOfMenuHeadBreakupDtl = new LinkedList<>();
            for (clsTaxCalculationDtls objMenuDtl : mapMenuBreakupDtl.values())
            {
                listOfMenuHeadBreakupDtl.add(objMenuDtl);
            }
            hm.put("listOfMenuBreakupDtl", listOfMenuHeadBreakupDtl);

            //call for view report
            if (reportType.equalsIgnoreCase("A4 Size Report"))
            {
                if (listOfTaxDtl.size() > 0)
                {
                    funViewJasperReportForJDBCConnectionDataSource(is, hm, null);
                }
                else
                {
                    JOptionPane.showMessageDialog(null, "Data not present for selected dates!!!");
                }
            }
            if (reportType.equalsIgnoreCase("Excel Report"))
            {
                int i = 1;
                Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
                List<String> arrListTotal = new ArrayList<String>();
                List<String> arrHeaderList = new ArrayList<String>();
                double totalTaxableAmount = 0;
                double totalTaxAmt = 0;
                for (int cnt = 0; cnt < listOfTaxDtl.size(); cnt++)
                {
                    clsTaxCalculationDtls objTax = listOfTaxDtl.get(cnt);
                    List<String> arrListItem = new ArrayList<String>();
                    arrListItem.add(objTax.getTaxName());
                    arrListItem.add(gDecimalFormat.format(objTax.getTaxableAmount()));
                    arrListItem.add(gDecimalFormat.format(objTax.getTaxAmount()));
                    totalTaxAmt = totalTaxAmt + objTax.getTaxAmount();
                    totalTaxableAmount = totalTaxableAmount + objTax.getTaxableAmount();
                    mapExcelItemDtl.put(i, arrListItem);
                    i++;
                }

                //arrListTotal.add(String.valueOf(Math.rint(totalTaxableAmount)) + "#" + "2");
                arrListTotal.add(gDecimalFormat.format(totalTaxAmt) + "#" + "3");

                arrHeaderList.add("Serial No");
                arrHeaderList.add("Tax Desc");
                arrHeaderList.add("Taxable Amt");
                arrHeaderList.add("Tax Amt");

                List<String> arrparameterList = new ArrayList<String>();
                arrparameterList.add("Tax Breakup Summary Report");
                arrparameterList.add("POS : " + posName);
                arrparameterList.add("FromDate : " + fromDateToDisplay);
                arrparameterList.add("ToDate : " + toDateToDisplay);
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
                //objUtility.funCreateExcelSheet(arrparameterList,arrHeaderList,mapExcelItemDtl, arrListTotal,"taxBreakupExcelSheet");

                String filePath = System.getProperty("user.dir");
                File file = new File(filePath + File.separator + "Reports" + File.separator + "taxBreakupExcelSheet" + ".xls");

                WritableWorkbook workbook1 = Workbook.createWorkbook(file);
                WritableSheet sheet1 = workbook1.createSheet("First Sheet", 0);
                WritableFont cellFont = new WritableFont(WritableFont.COURIER, 14);
                cellFont.setBoldStyle(WritableFont.BOLD);
                WritableCellFormat cellFormat = new WritableCellFormat(cellFont);
                WritableFont cellFont1 = new WritableFont(WritableFont.COURIER, 12);
                cellFont1.setBoldStyle(WritableFont.BOLD);
                WritableCellFormat cellFormat1 = new WritableCellFormat(cellFont1);
                WritableFont headerCellFont = new WritableFont(WritableFont.TIMES, 10);
                headerCellFont.setBoldStyle(WritableFont.BOLD);
                WritableCellFormat headerCell = new WritableCellFormat(headerCellFont);

                for (int j = 0; j <= arrparameterList.size(); j++)
                {
                    Label l0 = new Label(1, 0, arrparameterList.get(0), cellFormat);
                    Label l1 = new Label(0, 2, arrparameterList.get(1), headerCell);
                    Label l2 = new Label(1, 2, arrparameterList.get(2), headerCell);
                    Label l3 = new Label(2, 2, arrparameterList.get(3), headerCell);
                    Label l4 = new Label(0, 3, arrparameterList.get(4), headerCell);
                    Label l5 = new Label(1, 3, arrparameterList.get(5), headerCell);

                    sheet1.addCell(l0);
                    sheet1.addCell(l1);
                    sheet1.addCell(l2);
                    sheet1.addCell(l3);
                    sheet1.addCell(l4);
                    sheet1.addCell(l5);
                }

                Label labelTaxBreakup = new Label(0, 5, "Tax Breakup Summary", cellFormat1);
                sheet1.addCell(labelTaxBreakup);
                sheet1.setColumnView(5, 15);

                for (int j = 0; j <= arrHeaderList.size(); j++)
                {
                    Label l0 = new Label(0, 7, arrHeaderList.get(0), headerCell);
                    Label l1 = new Label(1, 7, arrHeaderList.get(1), headerCell);
                    Label l2 = new Label(2, 7, arrHeaderList.get(2), headerCell);
                    Label l3 = new Label(3, 7, arrHeaderList.get(3), headerCell);

                    sheet1.addCell(l0);
                    sheet1.addCell(l1);
                    sheet1.addCell(l2);
                    sheet1.addCell(l3);

                }

                i = 9;
                for (Map.Entry<Integer, List<String>> entry : mapExcelItemDtl.entrySet())
                {
                    Label lbl0 = new Label(0, i, entry.getKey().toString());
                    List<String> nameList = mapExcelItemDtl.get(entry.getKey());
                    for (int j = 0; j <= nameList.size(); j++)
                    {
                        Label lbl1 = new Label(1, i, nameList.get(0));
                        Label lbl2 = new Label(2, i, nameList.get(1));
                        Label lbl3 = new Label(3, i, nameList.get(2));

                        sheet1.addCell(lbl1);
                        sheet1.addCell(lbl2);
                        sheet1.addCell(lbl3);
                        sheet1.setColumnView(i, 15);
                    }
                    sheet1.addCell(lbl0);
                    i++;
                }

                for (int j = 0; j < arrListTotal.size(); j++)
                {
                    String[] l0 = new String[10];
                    for (int c = 0; c < arrListTotal.size(); c++)
                    {
                        l0 = arrListTotal.get(c).split("#");
                        int position = Integer.parseInt(l0[1]);
                        Label lable0 = new Label(position, i + 1, l0[0], headerCell);
                        sheet1.addCell(lable0);
                    }
                    Label labelTotal = new Label(0, i + 1, "TOTAL:", headerCell);
                    sheet1.addCell(labelTotal);
                }

                // Menu Head Wise Tax Break up        
                i += 4;
                mapExcelItemDtl.clear();
                arrListTotal.clear();
                arrHeaderList.clear();

                workbook1.write();
                workbook1.close();

                if (!dayEnd.equalsIgnoreCase("Yes"))
                {
                    Desktop dt = Desktop.getDesktop();
                    dt.open(file);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void funViewJasperReportForJDBCConnectionDataSource(InputStream is, HashMap hm, List list)
    {
        try
        {
            JasperPrint print = JasperFillManager.fillReport(is, hm, clsGlobalVarClass.conJasper);
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
}
