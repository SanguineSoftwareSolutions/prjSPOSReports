/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.POSReport.controller;

import com.POSReport.controller.comparator.clsGroupSubGroupComparator;
import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsPosConfigFile;
import com.POSGlobal.controller.clsUtility;
import com.POSGlobal.controller.clsUtility2;
import com.POSGlobal.view.frmShowTextFile;
import java.awt.Desktop;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.PrintWriter;
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

public class clsGroupSubGroupWiseReport
{
    DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();
    public void funGenerateGroupSubGroupWiseReport(String reportType, HashMap hm, String dayEnd)
    {
       

        try
        {
            String reportName = "com/POSReport/reports/rptGroupSubGroupWiseReport.jasper";
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String groupCode = hm.get("groupCode").toString();
            String subGroupCode = hm.get("subGroupCode").toString();
            String posName = hm.get("posName").toString();
            String fromDateToDisplay = hm.get("fromDateToDisplay").toString();
            String toDateToDisplay = hm.get("toDateToDisplay").toString();

            StringBuilder sqlBuilder = new StringBuilder();

            String groupName = "", subGroupName = "";
            if (groupCode.equalsIgnoreCase("All"))
            {
                groupName = "All";
            }
            else
            {
                sqlBuilder.setLength(0);
                sqlBuilder.append("select strGroupName from tblgrouphd where strGroupCode='" + groupCode + "'");
                ResultSet rsGroupName = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
                rsGroupName.next();
                groupName = rsGroupName.getString(1);
                rsGroupName.close();
            }

            if (subGroupCode.equalsIgnoreCase("All"))
            {
                subGroupName = "All";
            }
            else
            {
                sqlBuilder.setLength(0);
                sqlBuilder.append("select strSubGroupName from tblsubgrouphd where strSubGroupCode='" + subGroupCode + "'");
                ResultSet rsSubGroupName = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
                rsSubGroupName.next();
                subGroupName = rsSubGroupName.getString(1);
                rsSubGroupName.close();
            }

            sqlBuilder.setLength(0);
            sqlBuilder.append("TRUNCATE tbltempsalesflash1");
            clsGlobalVarClass.dbMysql.execute(sqlBuilder.toString());

            Map<String, clsGroupSubGroupItemBean> mapItemDtl = new HashMap<>();
            List<clsGroupSubGroupItemBean> listOfGroupSubGroupWiseSales = new ArrayList<clsGroupSubGroupItemBean>();
            clsGroupSubGroupItemBean obj = null;

            //QFile
            sqlBuilder.setLength(0);
            sqlBuilder.append("select b.strItemName,d.strSubGroupName,e.strGroupName ,ifnull(sum(b.dblQuantity),0) as Quantity "
                    + ",ifnull(sum(b.dblAmount),0) as Amount,b.strItemCode "
                    + "from tblqbillhd a "
                    + "left outer join tblqbilldtl b on a.strBillNo=b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate) "
                    + "left outer join tblitemmaster c on b.strItemCode=c.strItemCode "
                    + "left outer join tblsubgrouphd d on c.strSubGroupCode=d.strSubGroupCode "
                    + "left outer join tblgrouphd e on d.strGroupCode=e.strGroupCode "
                    + "where  date(a.dteBillDate)  between '" + fromDate + "' and '" + toDate + "' "
                    + "and a.strPoscode=if('" + posCode + "'='All', a.strPoscode,'" + posCode + "') "
                    + "and e.strGroupCode=if('" + groupCode + "'='All',e.strGroupCode,'" + groupCode + "') "
                    + "and d.strSubGroupCode=if('" + subGroupCode + "'='All',d.strSubGroupCode,'" + subGroupCode + "') "
                    + "and a.intShiftCode=if('" + shiftNo + "'='All',a.intShiftCode,'" + shiftNo + "') "
                    + "Group By e.strGroupName ,d.strSubGroupName,b.strItemCode,b.strItemName "
                    + "order By e.strGroupName ,d.strSubGroupName,b.strItemCode,b.strItemName");
            ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
            while (rs.next())
            {
                String itemCode = rs.getString(6);

                if (mapItemDtl.containsKey(itemCode))
                {
                    obj = mapItemDtl.get(itemCode);

                    obj.setDblQuantity(obj.getDblQuantity() + rs.getDouble(4));
                    obj.setDblAmount(obj.getDblAmount() + rs.getDouble(5));

                    mapItemDtl.put(itemCode, obj);
                }
                else
                {
                    obj = new clsGroupSubGroupItemBean();
                    obj.setStrItemName(rs.getString(1));
                    obj.setStrSubGroupName(rs.getString(2));
                    obj.setStrGroupName(rs.getString(3));
                    obj.setDblQuantity(rs.getDouble(4));
                    obj.setDblAmount(rs.getDouble(5));
                    obj.setStrItemCode(rs.getString(6));

                    mapItemDtl.put(itemCode, obj);
                }

            }
            rs.close();

            //QFile modifiers
            sqlBuilder.setLength(0);
            sqlBuilder.append("select f.strModifierName,d.strSubGroupName,e.strGroupName ,ifnull(sum(f.dblQuantity),0) as Quantity "
                    + ",ifnull(sum(f.dblAmount),0) as Amount,f.strItemCode "
                    + "from tblqbillhd a "
                    + "left outer join tblqbilldtl b on a.strBillNo=b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate) "
                    + "left outer join tblqbillmodifierdtl f on b.strBillNo=f.strBillNo  and date(a.dteBillDate)=date(f.dteBillDate) "
                    + "left outer join tblitemmaster c on b.strItemCode=c.strItemCode "
                    + "left outer join tblsubgrouphd d on c.strSubGroupCode=d.strSubGroupCode "
                    + "left outer join tblgrouphd e on d.strGroupCode=e.strGroupCode "
                    + "where  date(a.dteBillDate)  between '" + fromDate + "' and '" + toDate + "' "
                    + "and a.strPoscode=if('" + posCode + "'='All', a.strPoscode,'" + posCode + "') "
                    + "and e.strGroupCode=if('" + groupCode + "'='All',e.strGroupCode,'" + groupCode + "') "
                    + "and d.strSubGroupCode=if('" + subGroupCode + "'='All',d.strSubGroupCode,'" + subGroupCode + "') "
                    + "and a.intShiftCode=if('" + shiftNo + "'='All',a.intShiftCode,'" + shiftNo + "') "
                    + "and b.strItemCode=left(f.strItemCode,7) "
                    + "and f.dblAmount>0 "
                    + "Group By e.strGroupName ,d.strSubGroupName,f.strItemCode,f.strModifierName "
                    + "order By e.strGroupName ,d.strSubGroupName,f.strItemCode,f.strModifierName");
            rs = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
            while (rs.next())
            {
                String itemCode = rs.getString(6);

                if (mapItemDtl.containsKey(itemCode))
                {
                    obj = mapItemDtl.get(itemCode);

                    obj.setDblQuantity(obj.getDblQuantity() + rs.getDouble(4));
                    obj.setDblAmount(obj.getDblAmount() + rs.getDouble(5));

                    mapItemDtl.put(itemCode, obj);
                }
                else
                {
                    obj = new clsGroupSubGroupItemBean();
                    obj.setStrItemName(rs.getString(1));
                    obj.setStrSubGroupName(rs.getString(2));
                    obj.setStrGroupName(rs.getString(3));
                    obj.setDblQuantity(rs.getDouble(4));
                    obj.setDblAmount(rs.getDouble(5));
                    obj.setStrItemCode(rs.getString(6));

                    mapItemDtl.put(itemCode, obj);
                }
            }
            rs.close();
            //Live
            sqlBuilder.setLength(0);
            sqlBuilder.append("select b.strItemName,d.strSubGroupName,e.strGroupName ,ifnull(sum(b.dblQuantity),0) as Quantity "
                    + ",ifnull(sum(b.dblAmount),0) as Amount,b.strItemCode "
                    + "from tblbillhd a "
                    + "left outer join tblbilldtl b on a.strBillNo=b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate) "
                    + "left outer join tblitemmaster c on b.strItemCode=c.strItemCode "
                    + "left outer join tblsubgrouphd d on c.strSubGroupCode=d.strSubGroupCode "
                    + "left outer join tblgrouphd e on d.strGroupCode=e.strGroupCode "
                    + "where  date(a.dteBillDate)  between '" + fromDate + "' and '" + toDate + "' "
                    + "and a.strPoscode=if('" + posCode + "'='All', a.strPoscode,'" + posCode + "') "
                    + "and e.strGroupCode=if('" + groupCode + "'='All',e.strGroupCode,'" + groupCode + "') "
                    + "and d.strSubGroupCode=if('" + subGroupCode + "'='All',d.strSubGroupCode,'" + subGroupCode + "') "
                    + "and a.intShiftCode=if('" + shiftNo + "'='All',a.intShiftCode,'" + shiftNo + "') "
                    + "Group By e.strGroupName ,d.strSubGroupName,b.strItemCode,b.strItemName "
                    + "order By e.strGroupName ,d.strSubGroupName,b.strItemCode,b.strItemName");
            rs = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
            while (rs.next())
            {
                String itemCode = rs.getString(6);

                if (mapItemDtl.containsKey(itemCode))
                {
                    obj = mapItemDtl.get(itemCode);

                    obj.setDblQuantity(obj.getDblQuantity() + rs.getDouble(4));
                    obj.setDblAmount(obj.getDblAmount() + rs.getDouble(5));

                    mapItemDtl.put(itemCode, obj);
                }
                else
                {
                    obj = new clsGroupSubGroupItemBean();
                    obj.setStrItemName(rs.getString(1));
                    obj.setStrSubGroupName(rs.getString(2));
                    obj.setStrGroupName(rs.getString(3));
                    obj.setDblQuantity(rs.getDouble(4));
                    obj.setDblAmount(rs.getDouble(5));
                    obj.setStrItemCode(rs.getString(6));

                    mapItemDtl.put(itemCode, obj);
                }
            }
            rs.close();
            //Live modifiers
            sqlBuilder.setLength(0);
            sqlBuilder.append("select f.strModifierName,d.strSubGroupName,e.strGroupName ,ifnull(sum(f.dblQuantity),0) as Quantity "
                    + ",ifnull(sum(f.dblAmount),0) as Amount,f.strItemCode "
                    + "from tblbillhd a "
                    + "left outer join tblbilldtl b on a.strBillNo=b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate) "
                    + "left outer join tblbillmodifierdtl f on b.strBillNo=f.strBillNo  and date(a.dteBillDate)=date(f.dteBillDate) "
                    + "left outer join tblitemmaster c on b.strItemCode=c.strItemCode "
                    + "left outer join tblsubgrouphd d on c.strSubGroupCode=d.strSubGroupCode "
                    + "left outer join tblgrouphd e on d.strGroupCode=e.strGroupCode "
                    + "where  date(a.dteBillDate)  between '" + fromDate + "' and '" + toDate + "' "
                    + "and a.strPoscode=if('" + posCode + "'='All', a.strPoscode,'" + posCode + "') "
                    + "and e.strGroupCode=if('" + groupCode + "'='All',e.strGroupCode,'" + groupCode + "') "
                    + "and d.strSubGroupCode=if('" + subGroupCode + "'='All',d.strSubGroupCode,'" + subGroupCode + "') "
                    + "and a.intShiftCode=if('" + shiftNo + "'='All',a.intShiftCode,'" + shiftNo + "') "
                    + "and b.strItemCode=left(f.strItemCode,7) "
                    + "and f.dblAmount>0 "
                    + "Group By e.strGroupName ,d.strSubGroupName,f.strItemCode,f.strModifierName "
                    + "order By e.strGroupName ,d.strSubGroupName,f.strItemCode,f.strModifierName");
            rs = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
            while (rs.next())
            {
                String itemCode = rs.getString(6);

                if (mapItemDtl.containsKey(itemCode))
                {
                    obj = mapItemDtl.get(itemCode);

                    obj.setDblQuantity(obj.getDblQuantity() + rs.getDouble(4));
                    obj.setDblAmount(obj.getDblAmount() + rs.getDouble(5));

                    mapItemDtl.put(itemCode, obj);
                }
                else
                {
                    obj = new clsGroupSubGroupItemBean();
                    obj.setStrItemName(rs.getString(1));
                    obj.setStrSubGroupName(rs.getString(2));
                    obj.setStrGroupName(rs.getString(3));
                    obj.setDblQuantity(rs.getDouble(4));
                    obj.setDblAmount(rs.getDouble(5));
                    obj.setStrItemCode(rs.getString(6));

                    mapItemDtl.put(itemCode, obj);
                }
            }
            rs.close();

            Comparator<clsGroupSubGroupItemBean> groupComparator = new Comparator<clsGroupSubGroupItemBean>()
            {

                @Override
                public int compare(clsGroupSubGroupItemBean o1, clsGroupSubGroupItemBean o2)
                {
                    return o1.getStrGroupName().compareToIgnoreCase(o2.getStrGroupName());
                }
            };

            Comparator<clsGroupSubGroupItemBean> subGroupComparator = new Comparator<clsGroupSubGroupItemBean>()
            {

                @Override
                public int compare(clsGroupSubGroupItemBean o1, clsGroupSubGroupItemBean o2)
                {
                    return o1.getStrSubGroupName().compareToIgnoreCase(o2.getStrSubGroupName());
                }
            };

            Comparator<clsGroupSubGroupItemBean> codeComparator = new Comparator<clsGroupSubGroupItemBean>()
            {

                @Override
                public int compare(clsGroupSubGroupItemBean o1, clsGroupSubGroupItemBean o2)
                {
                    return o1.getStrItemCode().substring(0, 7).compareToIgnoreCase(o2.getStrItemCode().substring(0, 7));
                }
            };

            listOfGroupSubGroupWiseSales.addAll(mapItemDtl.values());

            Collections.sort(listOfGroupSubGroupWiseSales, new clsGroupSubGroupComparator(
                    groupComparator,
                    subGroupComparator,
                    codeComparator)
            );
            //call for view report
            if (reportType.equalsIgnoreCase("A4 Size Report"))
            {
                funViewJasperReportForBeanCollectionDataSource(is, hm, listOfGroupSubGroupWiseSales);
            }
            if (reportType.equalsIgnoreCase("Excel Report"))
            {
                double totalQty = 0;
                double totalAmount = 0;
                int i = 1;
                Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
                List<String> arrListTotal = new ArrayList<String>();
                List<String> arrHeaderList = new ArrayList<String>();
                //DecimalFormat gDecimalFormat = new DecimalFormat("0.00");
                DecimalFormat decFormat = new DecimalFormat("0");
                for (clsGroupSubGroupItemBean objtemp : listOfGroupSubGroupWiseSales)
                {
                    listOfGroupSubGroupWiseSales.get(0);
                    List<String> arrListItem = new ArrayList<String>();
                    arrListItem.add(objtemp.getStrItemName());//itemName
                    arrListItem.add(objtemp.getStrSubGroupName());//subGroup
                    arrListItem.add(objtemp.getStrGroupName());//group
                    //arrListItem.add(rs.getString(7));//date
                    arrListItem.add(String.valueOf(decFormat.format(objtemp.getDblQuantity())));//qty
                    arrListItem.add(String.valueOf(gDecimalFormat.format(objtemp.getDblAmount())));//amount
                    arrListItem.add(" ");

                    totalQty = totalQty + Double.parseDouble(String.valueOf(decFormat.format(objtemp.getDblQuantity())));
                    totalAmount = totalAmount + Double.parseDouble(String.valueOf(gDecimalFormat.format(objtemp.getDblAmount())));
                    mapExcelItemDtl.put(i, arrListItem);
                    i++;
                }
                arrListTotal.add(String.valueOf(decFormat.format(totalQty)) + "#" + "4");
                arrListTotal.add(String.valueOf(gDecimalFormat.format(totalAmount)) + "#" + "5");

                arrHeaderList.add("Serial No");
                arrHeaderList.add("ItemName");
                arrHeaderList.add("SubGroup Name");
                arrHeaderList.add("Group Name");
                //arrHeaderList.add("BillDate");
                arrHeaderList.add("Qty");
                arrHeaderList.add("Amount");
                arrHeaderList.add(" ");

                List<String> arrparameterList = new ArrayList<String>();
                arrparameterList.add("Group-SubGroup Wise Report");
                arrparameterList.add("POS" + " : " + posName);
                arrparameterList.add("FromDate" + " : " + fromDateToDisplay);
                arrparameterList.add("ToDate" + " : " + toDateToDisplay);
                arrparameterList.add("Group Name" + " : " + groupName);
                arrparameterList.add("SubGroup Name" + " : " + subGroupName);
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
                funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "subgroupGroupWiseExcelSheet", dayEnd);
            }

            if (reportType.equalsIgnoreCase("Text File-40 Column Report"))
            {
                funCreateTempFolder();
                String filePath = System.getProperty("user.dir");
                File file = new File(filePath + "/Temp/Temp_GroupSubGroupWiseReport.txt");

                int count = 0;
                PrintWriter pw = new PrintWriter(file);
                funPrintBlankLines(clsGlobalVarClass.gClientName, pw);
                funPrintBlankLines(clsGlobalVarClass.gClientAddress1, pw);
                if (clsGlobalVarClass.gClientAddress2.trim().length() > 0)
                {
                    funPrintBlankLines(clsGlobalVarClass.gClientAddress2, pw);
                }
                if (clsGlobalVarClass.gClientAddress3.trim().length() > 0)
                {
                    funPrintBlankLines(clsGlobalVarClass.gClientAddress3, pw);
                }
                funPrintBlankLines("Group Sub-GroupWise Report", pw);

                pw.println();
                pw.println("POS  :" + posName);
                pw.println("From :" + fromDate + "  To :" + toDate);
                pw.println("---------------------------------------");
                pw.println("ITEM NAME                            ");
                pw.println("       Rate         Qty        Amount ");
                pw.println("--------------------------------------");

                int cnt = 0, cnt1 = 0;
                double grandTotal = 0.00, quantityTotal = 0.00, GroupTotal = 0.00, subGroupTotal = 0.00;

                int i = 0;
                String prevGroupName = "", GroupName = "", SubGroupName = "";
                String prevSubGroupName = "";
                for (clsGroupSubGroupItemBean objtemp : listOfGroupSubGroupWiseSales)
                {

                    count++;

                    if (!SubGroupName.equals(objtemp.getStrSubGroupName()))
                    {
                        if (i != 0)
                        {
                            pw.println("---------------------------------------");
                            funPrintTextValueWithAlignment("left", String.valueOf(SubGroupName + " TOTAL"), 27, pw);
                            funPrintTextWithAlignment("right", gDecimalFormat.format(subGroupTotal), 10, pw);
                            pw.println();
                            pw.println("---------------------------------------");
                            pw.println();
                            subGroupTotal = 0.00;
                        }

                    }

                    if (!GroupName.equals(objtemp.getStrGroupName()))
                    {
                        if (i != 0)
                        {
                            pw.println("---------------------------------------");
                            pw.print(GroupName + " TOTAL");
                            funPrintTextWithAlignment("right", gDecimalFormat.format(GroupTotal), 15, pw);
                            pw.println();
                            pw.println("---------------------------------------");
                            pw.println();
                            GroupTotal = 0.00;
                        }

                    }
                    if (!GroupName.equals(objtemp.getStrGroupName()))
                    {
                        pw.println();
                        pw.println("GROUP :" + objtemp.getStrGroupName());
                        funDrawUnderLine("GROUP :" + objtemp.getStrGroupName(), pw);

                    }
                    if (!SubGroupName.equals(objtemp.getStrSubGroupName()))
                    {

//                }
                        pw.println();
                        pw.println(objtemp.getStrSubGroupName());
                        funDrawUnderLine(objtemp.getStrSubGroupName(), pw);

                    }
                    pw.println(objtemp.getStrItemName());
                    funPrintTextWithAlignment("right", gDecimalFormat.format(objtemp.getDblSubTotal()), 12, pw);
                    funPrintTextWithAlignment("right", gDecimalFormat.format(objtemp.getDblQuantity()), 12, pw);
                    funPrintTextWithAlignment("right", gDecimalFormat.format(objtemp.getDblAmount()), 13, pw);
                    pw.println();

                    quantityTotal += objtemp.getDblQuantity();
                    grandTotal += objtemp.getDblAmount();
                    GroupTotal += objtemp.getDblAmount();
                    subGroupTotal += objtemp.getDblAmount();
                    cnt1++;
                    GroupName = objtemp.getStrGroupName();

                    SubGroupName = objtemp.getStrSubGroupName();

                    i++;

                }

                pw.println("---------------------------------------");
                funPrintTextValueWithAlignment("left", String.valueOf(SubGroupName + " TOTAL"), 27, pw);
                funPrintTextWithAlignment("right", gDecimalFormat.format(subGroupTotal), 10, pw);
                pw.println();
                pw.println("---------------------------------------");
                pw.println();

                pw.println("---------------------------------------");
                pw.print(GroupName + " TOTAL");
                funPrintTextWithAlignment("right", gDecimalFormat.format(GroupTotal), 15, pw);
                pw.println();
                pw.println("---------------------------------------");
                pw.println();

                pw.println("---------------------------------------");
                pw.print("GRAND TOTAL");
                funPrintTextWithAlignment("right", gDecimalFormat.format(quantityTotal), 13, pw);
                funPrintTextWithAlignment("right", gDecimalFormat.format(grandTotal), 12, pw);
                pw.println();
                pw.println("---------------------------------------");

                pw.println();
                pw.println();
                if ("linux".equalsIgnoreCase(clsPosConfigFile.gPrintOS))
                {
                    pw.println("V");//Linux
                }
                else if ("windows".equalsIgnoreCase(clsPosConfigFile.gPrintOS))
                {
                    if ("Inbuild".equalsIgnoreCase(clsPosConfigFile.gPrinterType))
                    {
                        pw.println("V");
                    }
                    else
                    {
                        pw.println("m");//windows
                    }
                }

                pw.flush();
                pw.close();

                if (count > 0)
                {
                    funShowTextFile(file, "Text Sales Report");
                }

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

    /**
     * Print blank Lines
     *
     * @param textToPrint
     * @param pw
     * @return
     */
    private int funPrintBlankLines(String textToPrint, PrintWriter pw)
    {
        pw.println();
        int len = 40 - textToPrint.length();
        len = len / 2;
        for (int cnt = 0; cnt < len; cnt++)
        {
            pw.print(" ");
        }
        pw.print(textToPrint);
        return len;
    }

    /**
     * Print Text Value With Alignment
     *
     * @param align
     * @param textToPrint
     * @param totalLength
     * @param pw
     * @return
     */
    private int funPrintTextValueWithAlignment(String align, String textToPrint, int totalLength, PrintWriter pw)
    {
        pw.print(String.format("%-27s", textToPrint));
        return 1;
    }

    /**
     * Print Text With Alignment
     *
     * @param align
     * @param textToPrint
     * @param totalLength
     * @param pw
     * @return
     */
    private int funPrintTextWithAlignment(String align, String textToPrint, int totalLength, PrintWriter pw)
    {
        int len = totalLength - textToPrint.length();
        for (int cnt = 0; cnt < len; cnt++)
        {
            pw.print(" ");
        }

        DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();
        pw.print(gDecimalFormat.format(Double.parseDouble(textToPrint)));
        return 1;
    }

    /**
     * Draw Under Line
     *
     * @param textToPrint
     * @param pw
     * @return
     */
    private int funDrawUnderLine(String textToPrint, PrintWriter pw)
    {
        for (int cnt = 0; cnt < textToPrint.length(); cnt++)
        {
            pw.print("-");
        }
        pw.println();
        return 1;
    }

    public void funCreateTempFolder()
    {
        try
        {
            String filePath = System.getProperty("user.dir");
            File file = new File(filePath + "/Temp");
            if (!file.exists())
            {
                file.mkdirs();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Show text file Method
     *
     * @param file
     * @param reportName
     */
    private void funShowTextFile(File file, String reportName)
    {
        try
        {
            String data = "";
            FileReader fread = new FileReader(file);
            BufferedReader KOTIn = new BufferedReader(fread);

            String line = "";
            while ((line = KOTIn.readLine()) != null)
            {
                data = data + line + "\n";
            }
            //printing to bill printer
            String billPrinterName = clsGlobalVarClass.gBillPrintPrinterPort;

            new frmShowTextFile(data, reportName, file, billPrinterName).setVisible(true);
            fread.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
