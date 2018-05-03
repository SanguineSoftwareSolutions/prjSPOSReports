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

public class clsSubGroupWiseReport
{
    DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();
    public void funSubGroupWiseReport(String reportType, HashMap hm, String dayEnd)
    {
        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptSubGroupWiseSalesReport.jasper");

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String posName = hm.get("posName").toString();
            String fromDateToDisplay = hm.get("fromDateToDisplay").toString();
            String toDateToDisplay = hm.get("toDateToDisplay").toString();

           // DecimalFormat gDecimalFormat = new DecimalFormat("0.00");
            DecimalFormat decimalFormat = new DecimalFormat("0");

            List<clsGroupSubGroupItemBean> listOfGroupSubGroupWiseSales = new ArrayList<clsGroupSubGroupItemBean>();

            StringBuilder sbSqlLive = new StringBuilder();
            StringBuilder sbSqlQFile = new StringBuilder();
            StringBuilder sbSqlFilters = new StringBuilder();

            sbSqlLive.setLength(0);
            sbSqlQFile.setLength(0);
            sbSqlFilters.setLength(0);

            sbSqlQFile.append("SELECT c.strSubGroupCode, c.strSubGroupName, sum( b.dblQuantity ) "
                    + ", sum( b.dblAmount )-sum(b.dblDiscountAmt), f.strPosName,'" + clsGlobalVarClass.gUserCode + "',b.dblRate ,sum(b.dblAmount),sum(b.dblDiscountAmt)"
                    + "from tblqbillhd a,tblqbilldtl b,tblsubgrouphd c,tblitemmaster d "
                    + ",tblposmaster f "
                    + "where a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " and a.strPOSCode=f.strPOSCode  "
                    + " and a.strClientCode=b.strClientCode   "
                    + "and b.strItemCode=d.strItemCode "
                    + "and c.strSubGroupCode=d.strSubGroupCode ");

            sbSqlLive.append("SELECT c.strSubGroupCode, c.strSubGroupName, sum( b.dblQuantity ) "
                    + ", sum( b.dblAmount )-sum(b.dblDiscountAmt), f.strPosName,'" + clsGlobalVarClass.gUserCode + "',b.dblRate ,sum(b.dblAmount),sum(b.dblDiscountAmt)"
                    + "from tblbillhd a,tblbilldtl b,tblsubgrouphd c,tblitemmaster d "
                    + ",tblposmaster f "
                    + "where a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " and a.strPOSCode=f.strPOSCode "
                    + " and a.strClientCode=b.strClientCode   "
                    + "and b.strItemCode=d.strItemCode "
                    + "and c.strSubGroupCode=d.strSubGroupCode ");

            String sqlModLive = "select c.strSubGroupCode,c.strSubGroupName"
                    + ",sum(b.dblQuantity),sum(b.dblAmount)-sum(b.dblDiscAmt),f.strPOSName"
                    + ",'" + clsGlobalVarClass.gUserCode + "','0' ,sum(b.dblAmount),sum(b.dblDiscAmt) "
                    + " from tblbillmodifierdtl b,tblbillhd a,tblposmaster f,tblitemmaster d"
                    + ",tblsubgrouphd c"
                    + " where a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " and a.strPOSCode=f.strPosCode  "
                    + " and a.strClientCode=b.strClientCode  "
                    + " and LEFT(b.strItemCode,7)=d.strItemCode "
                    + " and d.strSubGroupCode=c.strSubGroupCode "
                    + " and b.dblamount>0 ";

            String sqlModQFile = "select c.strSubGroupCode,c.strSubGroupName"
                    + ",sum(b.dblQuantity),sum(b.dblAmount)-sum(b.dblDiscAmt),f.strPOSName"
                    + ",'" + clsGlobalVarClass.gUserCode + "','0' ,sum(b.dblAmount),sum(b.dblDiscAmt) "
                    + " from tblqbillmodifierdtl b,tblqbillhd a,tblposmaster f,tblitemmaster d"
                    + ",tblsubgrouphd c"
                    + " where a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " and a.strPOSCode=f.strPosCode "
                    + " and a.strClientCode=b.strClientCode  "
                    + " and LEFT(b.strItemCode,7)=d.strItemCode "
                    + " and d.strSubGroupCode=c.strSubGroupCode "
                    + " and b.dblamount>0 ";

            sbSqlFilters.append(" and date( a.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' ");
            if (!posCode.equals("All"))
            {
                sbSqlFilters.append(" AND a.strPOSCode = '" + posCode + "' ");
            }
            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sbSqlFilters.append(" and a.intShiftCode = '" + shiftNo + "' ");
                }
            }
            sbSqlFilters.append(" group by c.strSubGroupCode, c.strSubGroupName, a.strPoscode");

            sbSqlLive.append(sbSqlFilters);
            sbSqlQFile.append(sbSqlFilters);
            sqlModLive += " " + sbSqlFilters;
            sqlModQFile += " " + sbSqlFilters;

            ResultSet rsLiveData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
            while (rsLiveData.next())
            {
                clsGroupSubGroupItemBean objBean = new clsGroupSubGroupItemBean();
                objBean.setStrItemCode(rsLiveData.getString(1));   //SubGroup Code
                objBean.setStrSubGroupName(rsLiveData.getString(2));  //SubGroup Name
                objBean.setDblQuantity(rsLiveData.getDouble(3));   //Qty
                objBean.setDblSubTotal(rsLiveData.getDouble(4));   //sub total
                objBean.setDblAmount(rsLiveData.getDouble(8));     //amt-disAmt
                objBean.setDblDisAmt(rsLiveData.getDouble(9));     //dis amt
                objBean.setStrPOSName(rsLiveData.getString(5));    //POS Name

                listOfGroupSubGroupWiseSales.add(objBean);
            }
            rsLiveData.close();

            ResultSet rsLiveModData = clsGlobalVarClass.dbMysql.executeResultSet(sqlModLive.toString());
            while (rsLiveModData.next())
            {
                clsGroupSubGroupItemBean objBean = new clsGroupSubGroupItemBean();
                objBean.setStrItemCode(rsLiveModData.getString(1));   //SubGroup Code
                objBean.setStrSubGroupName(rsLiveModData.getString(2));  //SubGroup Name
                objBean.setDblQuantity(rsLiveModData.getDouble(3));   //Qty
                objBean.setDblSubTotal(rsLiveModData.getDouble(4));   //sub total
                objBean.setDblAmount(rsLiveModData.getDouble(8));     //amt-disAmt
                objBean.setDblDisAmt(rsLiveModData.getDouble(9));     //dis amt
                objBean.setStrPOSName(rsLiveModData.getString(5));    //POS Name

                listOfGroupSubGroupWiseSales.add(objBean);
            }
            rsLiveModData.close();

            ResultSet rsQfileData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFile.toString());
            while (rsQfileData.next())
            {
                clsGroupSubGroupItemBean objBean = new clsGroupSubGroupItemBean();
                objBean.setStrItemCode(rsQfileData.getString(1));   //SubGroup Code
                objBean.setStrSubGroupName(rsQfileData.getString(2));  //SubGroup Name
                objBean.setDblQuantity(rsQfileData.getDouble(3));   //Qty
                objBean.setDblSubTotal(rsQfileData.getDouble(4));   //sub total
                objBean.setDblAmount(rsQfileData.getDouble(8));     //amt-disAmt
                objBean.setDblDisAmt(rsQfileData.getDouble(9));     //dis amt
                objBean.setStrPOSName(rsQfileData.getString(5));    //POS Name

                listOfGroupSubGroupWiseSales.add(objBean);
            }
            rsQfileData.close();

            ResultSet rsQfileModData = clsGlobalVarClass.dbMysql.executeResultSet(sqlModQFile.toString());
            while (rsQfileModData.next())
            {
                clsGroupSubGroupItemBean objBean = new clsGroupSubGroupItemBean();
                objBean.setStrItemCode(rsQfileModData.getString(1));    //SubGroup Code
                objBean.setStrSubGroupName(rsQfileModData.getString(2));//SubGroup Name
                objBean.setDblQuantity(rsQfileModData.getDouble(3));   //Qty
                objBean.setDblSubTotal(rsQfileModData.getDouble(4));   //sub total
                objBean.setDblAmount(rsQfileModData.getDouble(8));     //amt-disAmt
                objBean.setDblDisAmt(rsQfileModData.getDouble(9));     //dis amt
                objBean.setStrPOSName(rsQfileModData.getString(5));    //POS Name

                listOfGroupSubGroupWiseSales.add(objBean);
            }
            rsQfileModData.close();

            Comparator<clsGroupSubGroupItemBean> subGroupNameComparator = new Comparator<clsGroupSubGroupItemBean>()
            {

                @Override
                public int compare(clsGroupSubGroupItemBean o1, clsGroupSubGroupItemBean o2)
                {
                    return o1.getStrSubGroupName().compareToIgnoreCase(o2.getStrSubGroupName());
                }
            };

            Collections.sort(listOfGroupSubGroupWiseSales, subGroupNameComparator);

            //call for view report
            if (reportType.equalsIgnoreCase("A4 Size Report"))
            {
                funViewJasperReportForBeanCollectionDataSource(is, hm, listOfGroupSubGroupWiseSales);
            }
            if (reportType.equalsIgnoreCase("Excel Report"))
            {
                Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
                List<String> arrListTotal = new ArrayList<String>();
                List<String> arrHeaderList = new ArrayList<String>();
                double totalQty = 0;
                double totalAmount = 0;
                double subTotal = 0;
                double discountTotal = 0;

                int i = 1;
                for (clsGroupSubGroupItemBean objBean : listOfGroupSubGroupWiseSales)
                {
                    List<String> arrListItem = new ArrayList<String>();
                    arrListItem.add(objBean.getStrSubGroupName());
                    arrListItem.add(objBean.getStrPOSName());
                    arrListItem.add(String.valueOf(decimalFormat.format(objBean.getDblQuantity())));
                    arrListItem.add(gDecimalFormat.format(objBean.getDblSubTotal()));
                    arrListItem.add(gDecimalFormat.format(objBean.getDblDisAmt()));
                    arrListItem.add(gDecimalFormat.format(objBean.getDblAmount()));

                    totalQty = totalQty + objBean.getDblQuantity();
                    subTotal = subTotal + objBean.getDblSubTotal();
                    discountTotal = discountTotal + objBean.getDblDisAmt();
                    totalAmount = totalAmount + objBean.getDblAmount();
                    mapExcelItemDtl.put(i, arrListItem);
                    i++;
                }

                arrListTotal.add(String.valueOf(decimalFormat.format(totalQty)) + "#" + "3");
                arrListTotal.add(gDecimalFormat.format(subTotal) + "#" + "4");
                arrListTotal.add(gDecimalFormat.format(discountTotal) + "#" + "5");
                arrListTotal.add(gDecimalFormat.format(totalAmount) + "#" + "6");

                arrHeaderList.add("Serial No");
                arrHeaderList.add("SubGroup Name");
                arrHeaderList.add("POSName");
                arrHeaderList.add("Qty");
                arrHeaderList.add("SubTotal");
                arrHeaderList.add("Discount");
                arrHeaderList.add("Net Total");

                List<String> arrparameterList = new ArrayList<String>();
                arrparameterList.add("SubGroup Wise Report");
                arrparameterList.add("POS" + " : " + posName);
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

                funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "subGroupWiseExcelSheet", dayEnd);
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
