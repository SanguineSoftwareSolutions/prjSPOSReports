package com.POSReport.controller;

import com.POSReport.controller.comparator.clsOperatorComparator;
import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsOperatorDtl;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
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

public class clsOperatorWiseReport
{
    DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();
    public void funOperatorWiseReport(String reportType, HashMap hm, String dayEnd)
    {
        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptOpearatorWiseSettlementReport.jasper");

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String userCode = hm.get("userCode").toString();
            String userName = hm.get("userName").toString();
            String settleCode = hm.get("settleCode").toString();
            String posName = hm.get("posName").toString();
            String fromDateToDisplay = hm.get("fromDateToDisplay").toString();
            String toDateToDisplay = hm.get("toDateToDisplay").toString();

            StringBuilder sqlBuilder = new StringBuilder();
            //DecimalFormat gDecimalFormat = new DecimalFormat("0.00");

            Map<String, List<clsOperatorDtl>> mapSettlementWiseBills = new TreeMap<String, List<clsOperatorDtl>>();
            double totalNetRevenue = 0.00;
            //for Live
            sqlBuilder.setLength(0);
            sqlBuilder.append("SELECT a.strBillNo,date(a.dteBillDate),ifnull(e.strUserCode,'SANGUINE'),a.strUserEdited,b.strPOSName "
                    + ", IFNULL(a.dblSubTotal,0.00)dblSubTotal "
                    + ", IFNULL(a.dblDiscountAmt,0.00)dblDiscountAmt,(dblSubTotal-dblDiscountAmt)dblNetTotal"
                    + ",a.dblTaxAmt, IFNULL(d.strSettelmentDesc,'') strSettelmentDesc "
                    + ", IFNULL(sum(c.dblSettlementAmt),0.00)dblSettlementAmt,a.strSettelmentMode "
                    + "FROM tblbillhd a "
                    + "LEFT OUTER JOIN tblposmaster b ON a.strPOSCode=b.strPOSCode "
                    + "LEFT OUTER JOIN tblbillsettlementdtl c ON a.strBillNo=c.strBillNo AND a.strClientCode=c.strClientCode AND DATE(a.dteBillDate)= DATE(c.dteBillDate) "
                    + "LEFT OUTER JOIN tblsettelmenthd d ON c.strSettlementCode=d.strSettelmentCode "
                    + "left outer join tbluserhd e on a.strUserEdited=e.strUserCode "
                    + "WHERE DATE(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' ");
            if (!posCode.equals("All"))
            {
                sqlBuilder.append(" AND a.strPOSCode = '" + posCode + "' ");
            }
            if (!userCode.equals("All"))
            {
                sqlBuilder.append("  and e.strUserCode='" + userCode + "'");
            }
            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sqlBuilder.append(" and a.intShiftCode = '" + shiftNo + "' ");
                }
            }
            if (settleCode != "All")
            {
                sqlBuilder.append("  and d.strSettelmentCode='" + settleCode + "'");
            }

            sqlBuilder.append("group by a.dteBillDate,a.strBillNo,c.strSettlementCode "
                    + "order by e.strUserCode,a.dteBillDate ");

            ResultSet rsSettlementWiseBills = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
            while (rsSettlementWiseBills.next())
            {
                String billNo = rsSettlementWiseBills.getString(1);
                String billDate = rsSettlementWiseBills.getString(1);
                String billNoDateKey = billNo + "!" + billDate;

                if (mapSettlementWiseBills.containsKey(billNoDateKey))
                {
                    List<clsOperatorDtl> listOfOperatorWiseSettlementDtl = mapSettlementWiseBills.get(billNoDateKey);

                    clsOperatorDtl objOperatorDtl = new clsOperatorDtl();

                    objOperatorDtl.setStrUserCode(rsSettlementWiseBills.getString(3));
                    objOperatorDtl.setStrUserName(rsSettlementWiseBills.getString(4));
                    objOperatorDtl.setStrPOSName(rsSettlementWiseBills.getString(5));
                    objOperatorDtl.setStrSettlementDesc(rsSettlementWiseBills.getString(10));
                    objOperatorDtl.setSettleAmt(rsSettlementWiseBills.getDouble(11));

                    listOfOperatorWiseSettlementDtl.add(objOperatorDtl);

                    mapSettlementWiseBills.put(billNoDateKey, listOfOperatorWiseSettlementDtl);
                }
                else
                {

                    totalNetRevenue += totalNetRevenue;
                    clsOperatorDtl objOperatorDtl = new clsOperatorDtl();

                    objOperatorDtl.setStrUserCode(rsSettlementWiseBills.getString(3));
                    objOperatorDtl.setStrUserName(rsSettlementWiseBills.getString(4));
                    objOperatorDtl.setStrPOSName(rsSettlementWiseBills.getString(5));
                    objOperatorDtl.setDblSubTotal(rsSettlementWiseBills.getDouble(6));
                    objOperatorDtl.setDiscountAmt(rsSettlementWiseBills.getDouble(7));
                    objOperatorDtl.setDblNetTotal(rsSettlementWiseBills.getDouble(8));
                    objOperatorDtl.setDblTaxAmt(rsSettlementWiseBills.getDouble(9));
                    objOperatorDtl.setStrSettlementDesc(rsSettlementWiseBills.getString(10));
                    objOperatorDtl.setSettleAmt(rsSettlementWiseBills.getDouble(11));

                    List<clsOperatorDtl> listOfOperatorWiseSettlementDtl = new LinkedList<>();
                    listOfOperatorWiseSettlementDtl.add(objOperatorDtl);

                    mapSettlementWiseBills.put(billNoDateKey, listOfOperatorWiseSettlementDtl);
                }
            }
            rsSettlementWiseBills.close();

            //For Q
            sqlBuilder.setLength(0);
            sqlBuilder.append("SELECT a.strBillNo,date(a.dteBillDate),ifnull(e.strUserCode,'SANGUINE'),a.strUserEdited,b.strPOSName "
                    + ", IFNULL(a.dblSubTotal,0.00)dblSubTotal "
                    + ", IFNULL(a.dblDiscountAmt,0.00)dblDiscountAmt,(dblSubTotal-dblDiscountAmt)dblNetTotal"
                    + ",a.dblTaxAmt, IFNULL(d.strSettelmentDesc,'') strSettelmentDesc "
                    + ", IFNULL(sum(c.dblSettlementAmt),0.00)dblSettlementAmt,a.strSettelmentMode "
                    + "FROM tblqbillhd a "
                    + "LEFT OUTER JOIN tblposmaster b ON a.strPOSCode=b.strPOSCode "
                    + "LEFT OUTER JOIN tblqbillsettlementdtl c ON a.strBillNo=c.strBillNo AND a.strClientCode=c.strClientCode AND DATE(a.dteBillDate)= DATE(c.dteBillDate) "
                    + "LEFT OUTER JOIN tblsettelmenthd d ON c.strSettlementCode=d.strSettelmentCode "
                    + "left outer join tbluserhd e on a.strUserEdited=e.strUserCode "
                    + "WHERE DATE(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' ");
            if (!posCode.equals("All"))
            {
                sqlBuilder.append(" AND a.strPOSCode = '" + posCode + "' ");
            }
            if (!userCode.equals("All"))
            {
                sqlBuilder.append("  and e.strUserCode='" + userCode + "'");
            }
            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sqlBuilder.append(" and a.intShiftCode = '" + shiftNo + "' ");
                }
            }
            if (settleCode != "All")
            {
                sqlBuilder.append("  and d.strSettelmentCode='" + settleCode + "'");
            }

            sqlBuilder.append("group by a.dteBillDate,a.strBillNo,c.strSettlementCode "
                    + "order by e.strUserCode,a.dteBillDate ");

            rsSettlementWiseBills = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
            while (rsSettlementWiseBills.next())
            {
                String billNo = rsSettlementWiseBills.getString(1);
                String billDate = rsSettlementWiseBills.getString(1);
                String billNoDateKey = billNo + "!" + billDate;

                if (mapSettlementWiseBills.containsKey(billNoDateKey))
                {
                    List<clsOperatorDtl> listOfOperatorWiseSettlementDtl = mapSettlementWiseBills.get(billNoDateKey);

                    clsOperatorDtl objOperatorDtl = new clsOperatorDtl();

                    objOperatorDtl.setStrUserCode(rsSettlementWiseBills.getString(3));
                    objOperatorDtl.setStrUserName(rsSettlementWiseBills.getString(4));
                    objOperatorDtl.setStrPOSName(rsSettlementWiseBills.getString(5));
                    objOperatorDtl.setStrSettlementDesc(rsSettlementWiseBills.getString(10));
                    objOperatorDtl.setSettleAmt(rsSettlementWiseBills.getDouble(11));

                    listOfOperatorWiseSettlementDtl.add(objOperatorDtl);

                    mapSettlementWiseBills.put(billNoDateKey, listOfOperatorWiseSettlementDtl);
                }
                else
                {
                    totalNetRevenue += totalNetRevenue;
                    clsOperatorDtl objOperatorDtl = new clsOperatorDtl();

                    objOperatorDtl.setStrUserCode(rsSettlementWiseBills.getString(3));
                    objOperatorDtl.setStrUserName(rsSettlementWiseBills.getString(4));
                    objOperatorDtl.setStrPOSName(rsSettlementWiseBills.getString(5));
                    objOperatorDtl.setDblSubTotal(rsSettlementWiseBills.getDouble(6));
                    objOperatorDtl.setDiscountAmt(rsSettlementWiseBills.getDouble(7));
                    objOperatorDtl.setDblNetTotal(rsSettlementWiseBills.getDouble(8));
                    objOperatorDtl.setDblTaxAmt(rsSettlementWiseBills.getDouble(9));
                    objOperatorDtl.setStrSettlementDesc(rsSettlementWiseBills.getString(10));
                    objOperatorDtl.setSettleAmt(rsSettlementWiseBills.getDouble(11));

                    List<clsOperatorDtl> listOfOperatorWiseSettlementDtl = new LinkedList<>();
                    listOfOperatorWiseSettlementDtl.add(objOperatorDtl);

                    mapSettlementWiseBills.put(billNoDateKey, listOfOperatorWiseSettlementDtl);
                }
            }
            rsSettlementWiseBills.close();

            Map<String, clsOperatorDtl> mapUserCodeWise = new HashMap<>();
            for (List<clsOperatorDtl> listOfSettlementWiseBill : mapSettlementWiseBills.values())
            {
                for (clsOperatorDtl objNewDtl : listOfSettlementWiseBill)
                {

                    String user = objNewDtl.getStrUserCode();
                    String settlementName = objNewDtl.getStrSettlementDesc();
                    String key = user + "!" + settlementName;
                    if (mapUserCodeWise.containsKey(key))
                    {
                        clsOperatorDtl objOldDtl = mapUserCodeWise.get(key);

                        objNewDtl.setDblSubTotal(objNewDtl.getDblSubTotal() + objOldDtl.getDblSubTotal());
                        objNewDtl.setDiscountAmt(objNewDtl.getDiscountAmt() + objOldDtl.getDiscountAmt());
                        objNewDtl.setDblNetTotal(objNewDtl.getDblNetTotal() + objOldDtl.getDblNetTotal());
                        objNewDtl.setSettleAmt(objNewDtl.getSettleAmt() + objOldDtl.getSettleAmt());

                        mapUserCodeWise.put(key, objNewDtl);
                    }
                    else
                    {
                        mapUserCodeWise.put(key, objNewDtl);
                    }
                }
            }

            totalNetRevenue = 0;
            List<clsOperatorDtl> listOfOperatorWiseSettlementDtl = new LinkedList<>();
            Map<String, clsOperatorDtl> mapUserWiseNetTotal = new HashMap<>();

            for (clsOperatorDtl objOperator : mapUserCodeWise.values())
            {
                String userCodeKey = objOperator.getStrUserCode();

                listOfOperatorWiseSettlementDtl.add(objOperator);
                totalNetRevenue = totalNetRevenue + objOperator.getDblNetTotal();

                if (mapUserWiseNetTotal.containsKey(userCodeKey))
                {
                    clsOperatorDtl objUserWiseNetTotal = mapUserWiseNetTotal.get(userCodeKey);
                    objUserWiseNetTotal.setDblNetTotal(objUserWiseNetTotal.getDblNetTotal() + objOperator.getDblNetTotal());
                }
                else
                {
                    clsOperatorDtl objUserWiseNetTotal = new  clsOperatorDtl();
                    objUserWiseNetTotal.setStrUserCode(objOperator.getStrUserCode());
                    objUserWiseNetTotal.setDblNetTotal(objOperator.getDblNetTotal());

                    mapUserWiseNetTotal.put(userCodeKey, objUserWiseNetTotal);
                }

            }

            Comparator<clsOperatorDtl> userCodeComparator = new Comparator<clsOperatorDtl>()
            {

                @Override
                public int compare(clsOperatorDtl o1, clsOperatorDtl o2)
                {
                    return o1.getStrUserCode().compareToIgnoreCase(o2.getStrUserCode());
                }
            };

            Comparator<clsOperatorDtl> settlementNameComparator = new Comparator<clsOperatorDtl>()
            {

                @Override
                public int compare(clsOperatorDtl o1, clsOperatorDtl o2)
                {
                    return o1.getStrSettlementDesc().compareToIgnoreCase(o2.getStrSettlementDesc());
                }
            };

            Collections.sort(listOfOperatorWiseSettlementDtl, new clsOperatorComparator(
                    userCodeComparator,
                    settlementNameComparator)
            );

            hm.put("totalNetRevenue", totalNetRevenue);
            //call for view report
            if (reportType.equalsIgnoreCase("A4 Size Report"))
            {
                funViewJasperReportForBeanCollectionDataSource(is, hm, listOfOperatorWiseSettlementDtl);
            }

            double subTotal = 0, discAmt = 0, netTotal = 0, SettleAmt = 0;
            if (reportType.equalsIgnoreCase("Excel Report"))
            {
                Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
                List<String> arrListTotal = new ArrayList<String>();
                List<String> arrHeaderList = new ArrayList<String>();

                int j = 1;
                for (clsOperatorDtl objOperatorDtl : listOfOperatorWiseSettlementDtl)
                {
                    List<String> arrListItem = new ArrayList<String>();
                    String userCodeKey = objOperatorDtl.getStrUserCode();

                    arrListItem.add(userCodeKey);
                    arrListItem.add(objOperatorDtl.getStrSettlementDesc());
                    arrListItem.add(gDecimalFormat.format(objOperatorDtl.getDblSubTotal()));
                    arrListItem.add(gDecimalFormat.format(objOperatorDtl.getDiscountAmt()));
                    arrListItem.add(gDecimalFormat.format(objOperatorDtl.getDblNetTotal()));
                    arrListItem.add(gDecimalFormat.format(objOperatorDtl.getSettleAmt()));

                    double userDiscPer = 0.00, netTotalPer = 0;
                    if (mapUserWiseNetTotal.containsKey(userCodeKey))
                    {
                        double userNetTotal = mapUserWiseNetTotal.get(userCodeKey).getDblNetTotal();
                        if (userNetTotal > 0)
                        {
                            userDiscPer = (objOperatorDtl.getDiscountAmt() / userNetTotal) * 100;
                        }
                    }
                    arrListItem.add(String.valueOf(Math.rint(userDiscPer)));
                    if (totalNetRevenue > 0)
                    {
                        netTotalPer = (objOperatorDtl.getDiscountAmt() / totalNetRevenue) * 100;
                    }
                    arrListItem.add(String.valueOf(Math.rint(netTotalPer)));

                    subTotal = subTotal + objOperatorDtl.getDblSubTotal();
                    discAmt = discAmt + objOperatorDtl.getDiscountAmt();
                    netTotal = netTotal + objOperatorDtl.getDblNetTotal();
                    SettleAmt = SettleAmt + objOperatorDtl.getSettleAmt();
                    mapExcelItemDtl.put(j, arrListItem);
                    j++;
                }
                arrListTotal.add(gDecimalFormat.format(subTotal) + "#" + "3");
                arrListTotal.add(gDecimalFormat.format(discAmt) + "#" + "4");
                arrListTotal.add(gDecimalFormat.format(netTotal) + "#" + "5");
                arrListTotal.add(gDecimalFormat.format(SettleAmt) + "#" + "6");

                arrHeaderList.add("Serial No");
                arrHeaderList.add("User");
                arrHeaderList.add("Settle Mode");
                arrHeaderList.add("Sub Total");
                arrHeaderList.add("Discount");
                arrHeaderList.add("Net Total");
                arrHeaderList.add("Gross Total");
                arrHeaderList.add("User Disc %");
                arrHeaderList.add("Net Revenue Disc %");

                List<String> arrparameterList = new ArrayList<String>();
                arrparameterList.add("Operator Wise Report");
                arrparameterList.add("POS" + " : " + posName);
                arrparameterList.add("FromDate" + " : " + fromDateToDisplay);
                arrparameterList.add("ToDate" + " : " + toDateToDisplay);
                arrparameterList.add("UserName" + " : " + userName);
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

                funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "operatorWiseExcelSheet", dayEnd);

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
