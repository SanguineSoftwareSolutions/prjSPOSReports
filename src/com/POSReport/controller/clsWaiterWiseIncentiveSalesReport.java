package com.POSReport.controller;

import com.POSReport.controller.comparator.clsWaiterWiseSalesComparator;
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

public class clsWaiterWiseIncentiveSalesReport
{
    
    DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter(); 
    public void funGenerateWaiterWiseIncentivesWiseReport(String reportType, HashMap hm, String dayEnd)
    {
        try
        {
            String reportName = "";

            String type = hm.get("rptType").toString();
            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String posName = hm.get("posName").toString();
            
            if (type.equals("Summary"))
            {
                type = "Summary";
                funWaiterWiseIncentivesSummaryReport(reportType, hm, dayEnd);
            }
            else
            {
                type = "Detail";
                funWaiterWiseIncentivesDetailReport(reportType, hm, dayEnd);
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void funWaiterWiseIncentivesSummaryReport(String reportType, HashMap hm, String dayEnd)
    {
        try
        {
            Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
            List<String> arrListTotal = new ArrayList<String>();
            List<String> arrHeaderList = new ArrayList<String>();
            double totalAmount = 0;
            double totalQty = 0;
            double totalIncentiveAmt = 0;
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptWaiterWiseIncentivesSummaryReport.jasper");
            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String posName = hm.get("posName").toString();
            String fromDateToDisplay = hm.get("fromDateToDisplay").toString();
            String toDateToDisplay = hm.get("toDateToDisplay").toString();

            StringBuilder sqlBuilder = new StringBuilder();
            List<clsBillDtl> listOfWaiterWiseItemSales = new ArrayList<>();

            //Q Data
            sqlBuilder.setLength(0);
            sqlBuilder.append("select e.strWaiterNo,ifnull(e.strWShortName,'ND')strWShortName,sum(b.dblQuantity)dblQuantity,sum(b.dblAmount)dblAmount,"
                    + "round(sum(b.dblAmount)*(d.strIncentives/100),2)dblIncentives,a.strBillNo,d.strIncentives "
                    + "from tblqbillhd a,tblqbilldtl b,tblitemmaster c,tblsubgrouphd d,tblwaitermaster e "
                    + "where date(a.dtebilldate) between '" + fromDate + "' and '" + toDate + "' "
                    + "and a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + "and b.strItemCode=c.strItemCode "
                    + "and c.strSubGroupCode=d.strSubGroupCode "
                    + "and a.strWaiterNo=e.strWaiterNo ");
            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and a.strPOSCode='" + posCode + "' ");
            }
            if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
            {
                sqlBuilder.append("and a.intShiftCode='" + shiftNo + "' ");
            }

            sqlBuilder.append("group by e.strWaiterNo "
                    + "order by e.strWFullName ");

            ResultSet rsWaiterWiseItemSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
            while (rsWaiterWiseItemSales.next())
            {
                clsBillDtl obj = new clsBillDtl();

                obj.setStrWaiterNo(rsWaiterWiseItemSales.getString(1));
                obj.setStrWShortName(rsWaiterWiseItemSales.getString(2));
                obj.setDblQuantity(rsWaiterWiseItemSales.getDouble(3));
                obj.setDblAmount(rsWaiterWiseItemSales.getDouble(4));
                obj.setDblIncentive(rsWaiterWiseItemSales.getDouble(5));
                obj.setStrBillNo(rsWaiterWiseItemSales.getString(6));
                obj.setDblIncentivePer(rsWaiterWiseItemSales.getDouble(7));

                listOfWaiterWiseItemSales.add(obj);
            }
            rsWaiterWiseItemSales.close();

            //Live Data
            sqlBuilder.setLength(0);
            sqlBuilder.append("select e.strWaiterNo,ifnull(e.strWShortName,'ND')strWShortName,sum(b.dblQuantity)dblQuantity,sum(b.dblAmount)dblAmount,"
                    + "round(sum(b.dblAmount)*(d.strIncentives/100),2)dblIncentives,a.strBillNo,d.strIncentives "
                    + "from tblbillhd a,tblbilldtl b,tblitemmaster c,tblsubgrouphd d,tblwaitermaster e "
                    + "where date(a.dtebilldate) between '" + fromDate + "' and '" + toDate + "' "
                    + "and a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + "and b.strItemCode=c.strItemCode "
                    + "and c.strSubGroupCode=d.strSubGroupCode "
                    + "and a.strWaiterNo=e.strWaiterNo ");
            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and a.strPOSCode='" + posCode + "' ");
            }
            if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
            {
                sqlBuilder.append("and a.intShiftCode='" + shiftNo + "' ");
            }

            sqlBuilder.append("group by e.strWaiterNo "
                    + "order by e.strWFullName ");

            rsWaiterWiseItemSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
            while (rsWaiterWiseItemSales.next())
            {
                clsBillDtl obj = new clsBillDtl();

                obj.setStrWaiterNo(rsWaiterWiseItemSales.getString(1));
                obj.setStrWShortName(rsWaiterWiseItemSales.getString(2));
                obj.setDblQuantity(rsWaiterWiseItemSales.getDouble(3));
                obj.setDblAmount(rsWaiterWiseItemSales.getDouble(4));
                obj.setDblIncentive(rsWaiterWiseItemSales.getDouble(5));
                obj.setStrBillNo(rsWaiterWiseItemSales.getString(6));
                obj.setDblIncentivePer(rsWaiterWiseItemSales.getDouble(7));

                listOfWaiterWiseItemSales.add(obj);
            }
            rsWaiterWiseItemSales.close();

            Comparator<clsBillDtl> waiterCodeComparator = new Comparator<clsBillDtl>()
            {

                @Override
                public int compare(clsBillDtl o1, clsBillDtl o2)
                {
                    return o1.getStrWShortName().compareTo(o2.getStrWShortName());
                }
            };

            Collections.sort(listOfWaiterWiseItemSales, new clsWaiterWiseSalesComparator(waiterCodeComparator));
            //call for view report
            if (reportType.equalsIgnoreCase("A4 Size Report"))
            {
                funViewJasperReportForBeanCollectionDataSource(is, hm, listOfWaiterWiseItemSales);
            }
            //DecimalFormat gDecimalFormat = new DecimalFormat("0.00");
            DecimalFormat decFormat = new DecimalFormat("0");
            if (reportType.equalsIgnoreCase("Excel Report"))
            {
                int i = 1;
                
                for (clsBillDtl objBean : listOfWaiterWiseItemSales)
                {
                    List<String> arrListItem = new ArrayList<String>();
                    arrListItem.add(objBean.getStrBillNo());
                    arrListItem.add(objBean.getStrWaiterNo());
                    arrListItem.add(objBean.getStrWShortName());
                    arrListItem.add(decFormat.format(objBean.getDblQuantity()));
                    arrListItem.add(gDecimalFormat.format(objBean.getDblAmount()));
                    arrListItem.add(gDecimalFormat.format(objBean.getDblIncentive()));
                    arrListItem.add(" ");

                    totalAmount = totalAmount + objBean.getDblAmount();
                    totalIncentiveAmt = totalIncentiveAmt + objBean.getDblIncentive();
                    totalQty = totalQty + objBean.getDblQuantity();
                    mapExcelItemDtl.put(i, arrListItem);

                    i++;
                }
                arrListTotal.add(decFormat.format(totalQty) + "#" + "4");
                arrListTotal.add(gDecimalFormat.format(totalAmount) + "#" + "5");
                arrListTotal.add(gDecimalFormat.format(totalIncentiveAmt) + "#" + "6");

                arrHeaderList.add("Serial No");
                arrHeaderList.add("Bill No");
                arrHeaderList.add("Waiter Code");
                arrHeaderList.add("Waiter Name");
                arrHeaderList.add("Quantity");
                arrHeaderList.add("Amount");
                arrHeaderList.add("Incentive Amt");
                arrHeaderList.add(" ");

                List<String> arrparameterList = new ArrayList<String>();
                arrparameterList.add("WaiterWise Incentives Summary Report");
                arrparameterList.add("POS" + " : " + posName);
                arrparameterList.add("FromDate" + " : " + fromDateToDisplay);
                arrparameterList.add("ToDate" + " : " + toDateToDisplay);
                arrparameterList.add("");
                arrparameterList.add("  ");
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

                funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "waiterIncentivesSummaryExcelSheet", dayEnd);

            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void funWaiterWiseIncentivesDetailReport(String reportType, HashMap hm, String dayEnd)
    {
        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptWaiterWiseIncentivesDetailsReport.jasper");
            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String posName = hm.get("posName").toString();
            String fromDateToDisplay = hm.get("fromDateToDisplay").toString();
            String toDateToDisplay = hm.get("toDateToDisplay").toString();

            StringBuilder sqlBuilder = new StringBuilder();
            List<clsBillDtl> listOfWaiterWiseItemSales = new ArrayList<>();
            Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
            double totalAmount = 0;
            double totalQty = 0;
            double totalIncentiveAmt = 0;
            List<String> arrListTotal = new ArrayList<String>();
            List<String> arrHeaderList = new ArrayList<String>();

            //Q Data
            sqlBuilder.setLength(0);
            sqlBuilder.append("select e.strWaiterNo,ifnull(e.strWShortName,'ND')strWShortName,d.strSubGroupCode,d.strSubGroupName,a.strBillNo "
                    + ",sum(b.dblQuantity)dblQuantity,sum(b.dblAmount)dblAmount, "
                    + "round(sum(b.dblAmount)*(d.strIncentives/100),2)dblIncentives,round(d.strIncentives,2) as strIncentivePer,a.strBillNo "
                    + "from tblqbillhd a,tblqbilldtl b,tblitemmaster c,tblsubgrouphd d,tblwaitermaster e "
                    + "where date(a.dtebilldate) between '" + fromDate + "' and '" + toDate + "' "
                    + "and a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + "and b.strItemCode=c.strItemCode "
                    + "and c.strSubGroupCode=d.strSubGroupCode "
                    + "and a.strWaiterNo=e.strWaiterNo ");
            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and a.strPOSCode='" + posCode + "' ");
            }
            if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
            {
                sqlBuilder.append("and a.intShiftCode='" + shiftNo + "' ");
            }

            sqlBuilder.append("group by e.strWaiterNo,a.strBillNo "
                    + "order by e.strWFullName,a.strBillNo ");

            ResultSet rsWaiterWiseItemSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
            while (rsWaiterWiseItemSales.next())
            {
                clsBillDtl obj = new clsBillDtl();

                obj.setStrWaiterNo(rsWaiterWiseItemSales.getString(1));
                obj.setStrWShortName(rsWaiterWiseItemSales.getString(2));
                obj.setStrSubGroupCode(rsWaiterWiseItemSales.getString(3));
                obj.setStrSubGroupName(rsWaiterWiseItemSales.getString(4));
                obj.setStrBillNo(rsWaiterWiseItemSales.getString(5));
                obj.setDblQuantity(rsWaiterWiseItemSales.getDouble(6));
                obj.setDblAmount(rsWaiterWiseItemSales.getDouble(7));
                obj.setDblIncentive(rsWaiterWiseItemSales.getDouble(8));
                obj.setDblIncentivePer(rsWaiterWiseItemSales.getDouble(9));
                obj.setStrBillNo(rsWaiterWiseItemSales.getString(10));

                listOfWaiterWiseItemSales.add(obj);
            }
            rsWaiterWiseItemSales.close();

            //Live Data
            sqlBuilder.setLength(0);
            sqlBuilder.append("select e.strWaiterNo,ifnull(e.strWShortName,'ND')strWShortName,d.strSubGroupCode,d.strSubGroupName,a.strBillNo "
                    + ",sum(b.dblQuantity)dblQuantity,sum(b.dblAmount)dblAmount, "
                    + "round(sum(b.dblAmount)*(d.strIncentives/100),2)dblIncentives,round(d.strIncentives,2) as strIncentivePer,a.strBillNo "
                    + "from tblbillhd a,tblbilldtl b,tblitemmaster c,tblsubgrouphd d,tblwaitermaster e "
                    + "where date(a.dtebilldate) between '" + fromDate + "' and '" + toDate + "' "
                    + "and a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + "and b.strItemCode=c.strItemCode "
                    + "and c.strSubGroupCode=d.strSubGroupCode "
                    + "and a.strWaiterNo=e.strWaiterNo ");
            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and a.strPOSCode='" + posCode + "' ");
            }
            if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
            {
                sqlBuilder.append("and a.intShiftCode='" + shiftNo + "' ");
            }

            sqlBuilder.append("group by e.strWaiterNo,a.strBillNo "
                    + "order by e.strWFullName,a.strBillNo ");

            rsWaiterWiseItemSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
            while (rsWaiterWiseItemSales.next())
            {
                clsBillDtl obj = new clsBillDtl();

                obj.setStrWaiterNo(rsWaiterWiseItemSales.getString(1));
                obj.setStrWShortName(rsWaiterWiseItemSales.getString(2));
                obj.setStrSubGroupCode(rsWaiterWiseItemSales.getString(3));
                obj.setStrSubGroupName(rsWaiterWiseItemSales.getString(4));
                obj.setStrBillNo(rsWaiterWiseItemSales.getString(5));
                obj.setDblQuantity(rsWaiterWiseItemSales.getDouble(6));
                obj.setDblAmount(rsWaiterWiseItemSales.getDouble(7));
                obj.setDblIncentive(rsWaiterWiseItemSales.getDouble(8));
                obj.setDblIncentivePer(rsWaiterWiseItemSales.getDouble(9));
                obj.setStrBillNo(rsWaiterWiseItemSales.getString(10));

                listOfWaiterWiseItemSales.add(obj);
            }
            rsWaiterWiseItemSales.close();

            Comparator<clsBillDtl> waiterCodeComparator = new Comparator<clsBillDtl>()
            {

                @Override
                public int compare(clsBillDtl o1, clsBillDtl o2)
                {
                    return o1.getStrWShortName().compareTo(o2.getStrWShortName());
                }
            };

            Comparator<clsBillDtl> billNoComparator = new Comparator<clsBillDtl>()
            {

                @Override
                public int compare(clsBillDtl o1, clsBillDtl o2)
                {
                    return o1.getStrBillNo().compareTo(o2.getStrBillNo());
                }
            };

            Comparator<clsBillDtl> subGroupCodeComparator = new Comparator<clsBillDtl>()
            {

                @Override
                public int compare(clsBillDtl o1, clsBillDtl o2)
                {
                    return o1.getStrSubGroupCode().compareTo(o2.getStrSubGroupCode());
                }
            };

            Collections.sort(listOfWaiterWiseItemSales, new clsWaiterWiseSalesComparator(waiterCodeComparator, billNoComparator, subGroupCodeComparator));

            //call for view report
            if (reportType.equalsIgnoreCase("A4 Size Report"))
            {
                funViewJasperReportForBeanCollectionDataSource(is, hm, listOfWaiterWiseItemSales);
            }
            //DecimalFormat gDecimalFormat = new DecimalFormat("0.00");
            DecimalFormat decimalFormat = new DecimalFormat("0");
            if (reportType.equalsIgnoreCase("Excel Report"))
            {
                int i = 1;
                for (clsBillDtl objBean : listOfWaiterWiseItemSales)
                {
                    List<String> arrListItem = new ArrayList<String>();
                    arrListItem.add(objBean.getStrBillNo());
                    arrListItem.add(objBean.getStrWShortName());
                    arrListItem.add(objBean.getStrSubGroupName());
                    arrListItem.add(decimalFormat.format(objBean.getDblQuantity()));
                    arrListItem.add(gDecimalFormat.format(objBean.getDblAmount()));
                    arrListItem.add(gDecimalFormat.format(objBean.getDblIncentive()));
                    arrListItem.add(" ");

                    totalAmount = totalAmount + objBean.getDblAmount();
                    totalIncentiveAmt = totalIncentiveAmt + objBean.getDblIncentive();
                    totalQty = totalQty + objBean.getDblQuantity();

                    mapExcelItemDtl.put(i, arrListItem);

                    i++;
                }

                arrListTotal.add(decimalFormat.format(totalQty) + "#" + "4");
                arrListTotal.add(gDecimalFormat.format(totalAmount) + "#" + "5");
                arrListTotal.add(gDecimalFormat.format(totalIncentiveAmt) + "#" + "6");

                arrHeaderList.add("Serial No");
                arrHeaderList.add("Bill No");
                arrHeaderList.add("Waiter Name");
                arrHeaderList.add("SubGroup Name");
                arrHeaderList.add("Qty");
                arrHeaderList.add("Amount");
                arrHeaderList.add("Incentive Amt");
                arrHeaderList.add(" ");

                List<String> arrparameterList = new ArrayList<String>();
                arrparameterList.add("WaiterWise Incentives Details Report");
                arrparameterList.add("POS" + " : " + posName);
                arrparameterList.add("FromDate" + " : " + fromDateToDisplay);
                arrparameterList.add("ToDate" + " : " + toDateToDisplay);
                arrparameterList.add(" ");
                arrparameterList.add(" ");

                funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "waiterIncentivesDtlExcelSheet", dayEnd);

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
