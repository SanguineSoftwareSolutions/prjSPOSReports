/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.POSReport.controller;

import com.POSReport.controller.comparator.clsBillComplimentaryComparator;
import com.POSGlobal.controller.clsBillDtl;
import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsPosConfigFile;
import com.POSGlobal.controller.clsSalesFlashColumns;
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

public class clsComplimentaryBillReport
{
    DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();
    public void funGenerateComplimentaryBillReport(String reportType, HashMap hm, String dayEnd)
    {
        Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
        List<String> arrListTotal = new ArrayList<String>();
        List<String> arrHeaderList = new ArrayList<String>();
       // DecimalFormat decimalFormat = new DecimalFormat("0.00");
        double totalQty = 0;
        double totalAmount = 0;
        try
        {

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String posName = hm.get("posName").toString();
            String type = hm.get("rptType").toString();
            String reasonCode = hm.get("reasonCode").toString().trim();
            String reasonName = hm.get("reasonName").toString().trim();
            String fromDateToDisplay = hm.get("fromDateToDisplay").toString();
            String toDateToDisplay = hm.get("toDateToDisplay").toString();
            String reportName = "";
            if (type.equals("Detail"))
            {
                type = "Detail";
                reportName = "com/POSReport/reports/rptComplimentarySettlementReport.jasper";
            }
            else if (type.equals("Summary"))
            {
                type = "Summary";
                reportName = "com/POSReport/reports/rptComplimentorySummaryReport.jasper";
            }
            else
            {
                type = "Group Wise";
                reportName = "com/POSReport/reports/rptComplimentaryGroupWaiseReport.jasper";
            }
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

            StringBuilder sbSqlLive = new StringBuilder();
            StringBuilder sbSqlQBill = new StringBuilder();
            StringBuilder sqlLiveModifierBuilder = new StringBuilder();
            StringBuilder sqlQModifierBuilder = new StringBuilder();

            List<clsBillDtl> listOfCompliItemDtl = new ArrayList<>();

            sbSqlLive.setLength(0);
            sbSqlQBill.setLength(0);
            sqlLiveModifierBuilder.setLength(0);
            sqlQModifierBuilder.setLength(0);
            if (type.equalsIgnoreCase("Group Wise"))
            {
                sbSqlLive.setLength(0);
                sbSqlQBill.setLength(0);
                sqlLiveModifierBuilder.setLength(0);
                sqlQModifierBuilder.setLength(0);

                //live data
                sbSqlLive.append("SELECT e.strPosName,h.strGroupCode,h.strGroupName,b.strItemCode,b.strItemName,b.dblRate"
                        + ", SUM(b.dblQuantity) AS dblQnty,SUM(b.dblRate* b.dblQuantity) AS dblAmount "
                        + "FROM tblbillhd a,tblbillcomplementrydtl b,tblposmaster e,tblitemmaster f,tblsubgrouphd g,tblgrouphd h "
                        + "WHERE a.strBillNo = b.strBillNo  "
                        + "AND DATE(a.dteBillDate) =date(b.dteBillDate)  "
                        + "AND a.strPOSCode=e.strPosCode  "
                        + "AND b.strItemCode=f.strItemCode  "
                        + "AND f.strSubGroupCode=g.strSubGroupCode  "
                        + "AND g.strGroupCode=h.strGroupCode  "
                );

                //live modifiers
                sqlLiveModifierBuilder.append(" select e.strPosName,h.strGroupCode,h.strGroupName,b.strItemCode,b.strModifierName"
                        + ",b.dblRate,sum(b.dblQuantity),SUM(b.dblRate*b.dblQuantity) as dblAmount"
                        + " from tblbillhd a,tblbillmodifierdtl b,tblbillsettlementdtl c,tblsettelmenthd d,tblposmaster e "
                        + " ,tblitemmaster f,tblsubgrouphd g,tblgrouphd h"
                        + " where a.strBillNo = b.strBillNo "
                        + " and  a.strBillNo = c.strBillNo "
                        + " and c.strSettlementCode = d.strSettelmentCode "
                        + " and  a.strPOSCode=e.strPosCode  "
                        + " and left(b.strItemCode,7)=f.strItemCode"
                        + " and f.strSubGroupCode=g.strSubGroupCode"
                        + " and g.strGroupCode=h.strGroupCode"
                        + " and d.strSettelmentType='Complementary' ");

                //Q data
                sbSqlQBill.append("SELECT e.strPosName,h.strGroupCode,h.strGroupName,b.strItemCode,b.strItemName,b.dblRate"
                        + ", SUM(b.dblQuantity) AS dblQnty,SUM(b.dblRate* b.dblQuantity) AS dblAmount "
                        + "FROM tblqbillhd a,tblqbillcomplementrydtl b,tblposmaster e,tblitemmaster f,tblsubgrouphd g,tblgrouphd h "
                        + "WHERE a.strBillNo = b.strBillNo  "
                        + "AND DATE(a.dteBillDate) =date(b.dteBillDate)  "
                        + "AND a.strPOSCode=e.strPosCode  "
                        + "AND b.strItemCode=f.strItemCode  "
                        + "AND f.strSubGroupCode=g.strSubGroupCode  "
                        + "AND g.strGroupCode=h.strGroupCode  ");

                //Q modifiers
                sqlQModifierBuilder.append("select e.strPosName,h.strGroupCode,h.strGroupName,b.strItemCode,b.strModifierName"
                        + ",b.dblRate,sum(b.dblQuantity),SUM(b.dblRate*b.dblQuantity) as dblAmount"
                        + " from tblqbillhd a,tblqbillmodifierdtl b,tblqbillsettlementdtl c,tblsettelmenthd d,tblposmaster e \n"
                        + " ,tblitemmaster f,tblsubgrouphd g,tblgrouphd h"
                        + " where a.strBillNo = b.strBillNo "
                        + " and  a.strBillNo = c.strBillNo "
                        + " and c.strSettlementCode = d.strSettelmentCode "
                        + " and  a.strPOSCode=e.strPosCode  "
                        + " and left(b.strItemCode,7)=f.strItemCode"
                        + " and f.strSubGroupCode=g.strSubGroupCode"
                        + " and g.strGroupCode=h.strGroupCode"
                        + " and d.strSettelmentType='Complementary'");

                if (!posCode.equals("All"))
                {
                    sbSqlLive.append(" AND a.strPOSCode = '" + posCode + "' ");
                    sbSqlQBill.append(" AND a.strPOSCode = '" + posCode + "' ");
                    sqlLiveModifierBuilder.append(" AND a.strPOSCode = '" + posCode + "' ");
                    sqlQModifierBuilder.append(" AND a.strPOSCode = '" + posCode + "' ");
                }
                if (!reasonCode.equals("All"))
                {
                    sbSqlLive.append(" and a.strReasonCode='" + reasonCode + "' ");
                    sbSqlQBill.append(" and a.strReasonCode='" + reasonCode + "' ");
                    sqlLiveModifierBuilder.append(" and a.strReasonCode='" + reasonCode + "' ");
                    sqlQModifierBuilder.append(" and a.strReasonCode='" + reasonCode + "' ");
                }
                if (clsGlobalVarClass.gEnableShiftYN)
                {
                    if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                    {
                        sbSqlLive.append(" and a.intShiftCode = '" + shiftNo + "' ");
                        sbSqlQBill.append(" and a.intShiftCode = '" + shiftNo + "' ");
                        sqlLiveModifierBuilder.append(" and a.intShiftCode = '" + shiftNo + "' ");
                        sqlQModifierBuilder.append(" and a.intShiftCode = '" + shiftNo + "' ");
                    }
                }
                sbSqlLive.append(" and date(a.dteBillDate) Between '" + fromDate + "' and '" + toDate + "' "
                        + " group by h.strGroupCode,b.strItemCode"
                        + " order by h.strGroupCode,b.strItemCode;");
                sbSqlQBill.append(" and date(a.dteBillDate) Between '" + fromDate + "' and '" + toDate + "' "
                        + " group by h.strGroupCode,b.strItemCode"
                        + " order by h.strGroupCode,b.strItemCode;");
                sqlLiveModifierBuilder.append(" and date(a.dteBillDate) Between '" + fromDate + "' and '" + toDate + "' "
                        + " group by h.strGroupCode,b.strItemCode,b.strModifierName"
                        + " order by h.strGroupCode,b.strItemCode;");
                sqlQModifierBuilder.append(" and date(a.dteBillDate) Between '" + fromDate + "' and '" + toDate + "' "
                        + " group by h.strGroupCode,b.strItemCode,b.strModifierName"
                        + " order by h.strGroupCode,b.strItemCode;");

                //live data
                ResultSet rsSql = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
                while (rsSql.next())
                {
                    clsBillDtl objItemDtl = new clsBillDtl();

                    objItemDtl.setStrPosName(rsSql.getString(1));
                    objItemDtl.setStrGroupCode(rsSql.getString(2));
                    objItemDtl.setStrGroupName(rsSql.getString(3));
                    objItemDtl.setStrItemCode(rsSql.getString(4));
                    objItemDtl.setStrItemName(rsSql.getString(5));
                    objItemDtl.setDblRate(rsSql.getDouble(6));
                    objItemDtl.setDblQuantity(rsSql.getDouble(7));
                    objItemDtl.setDblAmount(rsSql.getDouble(8));

                    listOfCompliItemDtl.add(objItemDtl);
                }
                rsSql.close();
               

                //QFile
                rsSql = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQBill.toString());
                while (rsSql.next())
                {
                    clsBillDtl objItemDtl = new clsBillDtl();

                    objItemDtl.setStrPosName(rsSql.getString(1));
                    objItemDtl.setStrGroupCode(rsSql.getString(2));
                    objItemDtl.setStrGroupName(rsSql.getString(3));
                    objItemDtl.setStrItemCode(rsSql.getString(4));
                    objItemDtl.setStrItemName(rsSql.getString(5));
                    objItemDtl.setDblRate(rsSql.getDouble(6));
                    objItemDtl.setDblQuantity(rsSql.getDouble(7));
                    objItemDtl.setDblAmount(rsSql.getDouble(8));

                    listOfCompliItemDtl.add(objItemDtl);

                }
                rsSql.close();
               

                Comparator<clsBillDtl> posNameComparator = new Comparator<clsBillDtl>()
                {

                    @Override
                    public int compare(clsBillDtl o1, clsBillDtl o2)
                    {
                        return o1.getStrPosName().compareToIgnoreCase(o2.getStrPosName());
                    }
                };
                Comparator<clsBillDtl> groupNameComparator = new Comparator<clsBillDtl>()
                {

                    @Override
                    public int compare(clsBillDtl o1, clsBillDtl o2)
                    {
                        return o1.getStrGroupName().compareToIgnoreCase(o2.getStrGroupName());
                    }
                };

                Comparator<clsBillDtl> itemNameComparator = new Comparator<clsBillDtl>()
                {

                    @Override
                    public int compare(clsBillDtl o1, clsBillDtl o2)
                    {
                        return o1.getStrItemName().compareToIgnoreCase(o2.getStrItemName());
                    }
                };

                Collections.sort(listOfCompliItemDtl, new clsBillComplimentaryComparator(
                        posNameComparator, groupNameComparator, itemNameComparator
                ));

                //call for view report
                if (reportType.equalsIgnoreCase("A4 Size Report"))
                {
                    funViewJasperReportForBeanCollectionDataSource(is, hm, listOfCompliItemDtl);
                }

                if (reportType.equalsIgnoreCase("Excel Report"))
                {
                    int i = 1;
                    DecimalFormat decFormat = new DecimalFormat("0");
                    //DecimalFormat decFormatFor2Decimal = new DecimalFormat("0.00");
                    for (clsBillDtl objBean : listOfCompliItemDtl)
                    {
                        List<String> arrListItem = new ArrayList<String>();
                        arrListItem.add(objBean.getStrPosName());
                        arrListItem.add(objBean.getStrGroupCode());
                        arrListItem.add(objBean.getStrGroupName());
                        arrListItem.add(objBean.getStrItemCode());
                        arrListItem.add(objBean.getStrItemName());
                        arrListItem.add(String.valueOf(gDecimalFormat.format(objBean.getDblRate())));
                        arrListItem.add(String.valueOf(decFormat.format(objBean.getDblQuantity())));
                        arrListItem.add(String.valueOf(gDecimalFormat.format(objBean.getDblAmount())));

                        totalQty = totalQty + Double.parseDouble(String.valueOf(objBean.getDblQuantity()));
                        totalAmount = totalAmount + Double.parseDouble(String.valueOf(objBean.getDblAmount()));
                        mapExcelItemDtl.put(i, arrListItem);
                        i++;
                    }

                    arrListTotal.add(String.valueOf(decFormat.format((totalQty))) + "#" + "7");
                    arrListTotal.add(String.valueOf(gDecimalFormat.format((totalAmount))) + "#" + "8");

                    arrHeaderList.add("Serial No");
                    arrHeaderList.add("POS");
                    arrHeaderList.add("Group Code");
                    arrHeaderList.add("Group Name");
                    arrHeaderList.add("Item Code");
                    arrHeaderList.add("Item Name");

                    arrHeaderList.add("Rate");
                    arrHeaderList.add("Qty");
                    arrHeaderList.add("Amount");

                    List<String> arrparameterList = new ArrayList<String>();
                    arrparameterList.add("Complimentary SettlementWise Group Wise Report");
                    arrparameterList.add("POS" + " : " + posName);
                    arrparameterList.add("FromDate" + " : " + fromDateToDisplay);
                    arrparameterList.add("ToDate" + " : " + toDateToDisplay);
                    arrparameterList.add("Reason" + " : " + reasonName);
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
                    funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "ComplimentaryWiseGroupWiseExcelSheetReport",dayEnd);

                }
            }
            else if (type.equalsIgnoreCase("Detail"))
            {
                sbSqlLive.setLength(0);
                sbSqlQBill.setLength(0);
                sqlLiveModifierBuilder.setLength(0);
                sqlQModifierBuilder.setLength(0);

                //live data              
                sbSqlLive.append("SELECT IFNULL(a.strBillNo,''), DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y') AS dteBillDate, IFNULL(b.strItemName,'') "
                        + ",sum(b.dblQuantity),b.dblRate,sum(b.dblQuantity*b.dblRate) AS dblAmount, IFNULL(f.strPosName,'') "
                        + ", IFNULL(g.strWShortName,'NA') AS strWShortName, IFNULL(e.strReasonName,''), IFNULL(a.strRemarks,'') "
                        + ", IFNULL(i.strGroupName,'') AS strGroupName, IFNULL(b.strKOTNo,'') "
                        + ",a.strPOSCode, IFNULL(h.strTableName,'') AS strTableName, IFNULL(b.strItemCode,'        ') "
                        + "FROM tblbillhd a "
                        + "Inner JOIN tblbillcomplementrydtl b ON a.strBillNo = b.strBillNo "
                        + "left outer JOIN tblreasonmaster e ON a.strReasonCode = e.strReasonCode "
                        + "LEFT OUTER "
                        + "JOIN tblposmaster f ON a.strPOSCode=f.strPosCode "
                        + "LEFT OUTER  "
                        + "JOIN tblwaitermaster g ON a.strWaiterNo=g.strWaiterNo "
                        + "LEFT OUTER "
                        + "JOIN tbltablemaster h ON a.strTableNo=h.strTableNo "
                        + "LEFT OUTER "
                        + "JOIN tblitemcurrentstk i ON b.strItemCode=i.strItemCode "
                        + "where  date(a.dteBillDate) Between '" + fromDate + "' and '" + toDate + "' ");

                //live modifiers
                sqlLiveModifierBuilder.append("select ifnull(a.strBillNo,''),DATE_FORMAT(date(a.dteBillDate),'%d-%m-%Y') as dteBillDate,b.strModifierName, sum(b.dblQuantity), b.dblRate,sum(b.dblQuantity*b.dblRate) as dblAmount"
                        + " ,ifnull(f.strPosName,''),ifnull(g.strWShortName,'NA') as strWShortName, ifnull(e.strReasonName,'') as strReasonName, a.strRemarks,ifnull(i.strGroupName,'') as strGroupName, "
                        + " ifnull(j.strKOTNo,''),a.strPOSCode,ifnull(h.strTableName,'') as strTableName,ifnull(b.strItemCode,'        ')  "
                        + " from tblbillhd a"
                        + " INNER JOIN  tblbillmodifierdtl b on a.strBillNo = b.strBillNo"
                        + " left outer join  tblbillsettlementdtl c on a.strBillNo = c.strBillNo"
                        + " left outer join  tblsettelmenthd d on c.strSettlementCode = d.strSettelmentCode "
                        + " left outer join tblreasonmaster e on  a.strReasonCode = e.strReasonCode "
                        + " left outer join tblposmaster f on a.strPOSCode=f.strPosCode "
                        + " left outer join tblwaitermaster g on a.strWaiterNo=g.strWaiterNo"
                        + " left outer join tbltablemaster h on  a.strTableNo=h.strTableNo"
                        + " left outer join tblitemcurrentstk i on left(b.strItemCode,7)=i.strItemCode"
                        + " left outer join  tblbilldtl j on b.strBillNo = j.strBillNo  "
                        + " where d.strSettelmentType = 'Complementary' ");

                //Q data
                sbSqlQBill.append("SELECT IFNULL(a.strBillNo,''), DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y') AS dteBillDate, IFNULL(b.strItemName,'') "
                        + ",sum(b.dblQuantity),b.dblRate,sum(b.dblQuantity*b.dblRate) AS dblAmount, IFNULL(f.strPosName,'') "
                        + ", IFNULL(g.strWShortName,'NA') AS strWShortName, IFNULL(e.strReasonName,''), IFNULL(a.strRemarks,'') "
                        + ", IFNULL(i.strGroupName,'') AS strGroupName, IFNULL(b.strKOTNo,'') "
                        + ",a.strPOSCode, IFNULL(h.strTableName,'') AS strTableName, IFNULL(b.strItemCode,'        ') "
                        + "FROM tblqbillhd a "
                        + "INNER JOIN tblqbillcomplementrydtl b ON a.strBillNo = b.strBillNo "
                        + "left outer JOIN tblreasonmaster e ON a.strReasonCode = e.strReasonCode "
                        + "LEFT OUTER "
                        + "JOIN tblposmaster f ON a.strPOSCode=f.strPosCode "
                        + "LEFT OUTER  "
                        + "JOIN tblwaitermaster g ON a.strWaiterNo=g.strWaiterNo "
                        + "LEFT OUTER "
                        + "JOIN tbltablemaster h ON a.strTableNo=h.strTableNo "
                        + "LEFT OUTER "
                        + "JOIN tblitemcurrentstk i ON b.strItemCode=i.strItemCode "
                        + "where  date(a.dteBillDate) Between '" + fromDate + "' and '" + toDate + "' ");

                //Q modifiers
                sqlQModifierBuilder.append("select ifnull(a.strBillNo,''),DATE_FORMAT(date(a.dteBillDate),'%d-%m-%Y') as dteBillDate,b.strModifierName,sum(b.dblQuantity), b.dblRate,sum(b.dblQuantity*b.dblRate,0) as dblAmount,ifnull(f.strPosName,''),ifnull(g.strWShortName,'NA') as strWShortName,ifnull(e.strReasonName,'') as strReasonName, a.strRemarks,ifnull(i.strGroupName,'') as strGroupName,\n"
                        + "ifnull(j.strKOTNo,''),a.strPOSCode,ifnull(h.strTableName,'') as strTableName,ifnull(b.strItemCode,'        ')  "
                        + " from tblqbillhd a"
                        + " INNER JOIN  tblqbillmodifierdtl b on a.strBillNo = b.strBillNo"
                        + " left outer join  tblqbillsettlementdtl c on a.strBillNo = c.strBillNo"
                        + " left outer join  tblsettelmenthd d on c.strSettlementCode = d.strSettelmentCode "
                        + " left outer join tblreasonmaster e on  a.strReasonCode = e.strReasonCode "
                        + " left outer join tblposmaster f on a.strPOSCode=f.strPosCode "
                        + " left outer join tblwaitermaster g on a.strWaiterNo=g.strWaiterNo"
                        + " left outer join tbltablemaster h on  a.strTableNo=h.strTableNo"
                        + " left outer join tblitemcurrentstk i on left(b.strItemCode,7)=i.strItemCode"
                        + " left outer join  tblqbilldtl j on b.strBillNo = j.strBillNo  "
                        + " where d.strSettelmentType = 'Complementary' ");

                if (!posCode.equals("All"))
                {
                    sbSqlLive.append(" AND a.strPOSCode = '" + posCode + "' ");
                    sbSqlQBill.append(" AND a.strPOSCode = '" + posCode + "' ");
                    sqlLiveModifierBuilder.append(" AND a.strPOSCode = '" + posCode + "' ");
                    sqlQModifierBuilder.append(" AND a.strPOSCode = '" + posCode + "' ");
                }
                if (!reasonCode.equals("All"))
                {
                    sbSqlLive.append(" and a.strReasonCode='" + reasonCode + "' ");
                    sbSqlQBill.append(" and a.strReasonCode='" + reasonCode + "' ");
                    sqlLiveModifierBuilder.append(" and a.strReasonCode='" + reasonCode + "' ");
                    sqlQModifierBuilder.append(" and a.strReasonCode='" + reasonCode + "' ");
                }
                if (clsGlobalVarClass.gEnableShiftYN)
                {
                    if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                    {
                        sbSqlLive.append(" and a.intShiftCode = '" + shiftNo + "' ");
                        sbSqlQBill.append(" and a.intShiftCode = '" + shiftNo + "' ");
                        sqlLiveModifierBuilder.append(" and a.intShiftCode = '" + shiftNo + "' ");
                        sqlQModifierBuilder.append(" and a.intShiftCode = '" + shiftNo + "' ");
                    }
                }
                sbSqlLive.append("  "
                        + " group by a.strPOSCode,a.strBillNo,b.strKOTNo,b.strItemCode "
                        + " order by a.strPOSCode,a.strBillNo,b.strKOTNo,b.strItemCode ");
                sbSqlQBill.append("  "
                        + " group by a.strPOSCode,a.strBillNo,b.strKOTNo,b.strItemCode "
                        + " order by a.strPOSCode,a.strBillNo,b.strKOTNo,b.strItemCode ");
                sqlLiveModifierBuilder.append(" and date(a.dteBillDate) Between '" + fromDate + "' and '" + toDate + "'  "
                        + " group by a.strPOSCode,a.strBillNo,left(b.strItemCode,7),b.strModifierName "
                        + " order by a.strPOSCode,a.strBillNo,left(b.strItemCode,7),b.strModifierName ");
                sqlQModifierBuilder.append(" and date(a.dteBillDate) Between '" + fromDate + "' and '" + toDate + "'  "
                        + " group by a.strPOSCode,a.strBillNo,left(b.strItemCode,7),b.strModifierName "
                        + " order by a.strPOSCode,a.strBillNo,left(b.strItemCode,7),b.strModifierName ");

                //live data
                ResultSet rsSql = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
                while (rsSql.next())
                {
                    clsBillDtl objItemDtl = new clsBillDtl();

                    objItemDtl.setStrBillNo(rsSql.getString(1));
                    objItemDtl.setDteBillDate(rsSql.getString(2));
                    objItemDtl.setStrItemName(rsSql.getString(3));
                    objItemDtl.setDblQuantity(rsSql.getDouble(4));//itemQty
                    objItemDtl.setDblModQuantity(0);//modifierQty
                    objItemDtl.setDblRate(rsSql.getDouble(5));
                    objItemDtl.setDblAmount(rsSql.getDouble(6));
                    objItemDtl.setStrPosName(rsSql.getString(7));
                    objItemDtl.setStrWShortName(rsSql.getString(8));
                    objItemDtl.setStrReasonName(rsSql.getString(9));
                    objItemDtl.setStrRemarks(rsSql.getString(10));
                    objItemDtl.setStrGroupName(rsSql.getString(11));
                    objItemDtl.setStrKOTNo(rsSql.getString(12));
                    objItemDtl.setStrPOSCode(rsSql.getString(13));
                    objItemDtl.setStrTableName(rsSql.getString(14));
                    objItemDtl.setStrItemCode(rsSql.getString(15));

                    listOfCompliItemDtl.add(objItemDtl);
                }
                rsSql.close();
               

                //QFile
                rsSql = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQBill.toString());
                while (rsSql.next())
                {
                    clsBillDtl objItemDtl = new clsBillDtl();

                    objItemDtl.setStrBillNo(rsSql.getString(1));
                    objItemDtl.setDteBillDate(rsSql.getString(2));
                    objItemDtl.setStrItemName(rsSql.getString(3));
                    objItemDtl.setDblQuantity(rsSql.getDouble(4));//itemQty
                    objItemDtl.setDblModQuantity(0);//modifierQty
                    objItemDtl.setDblRate(rsSql.getDouble(5));
                    objItemDtl.setDblAmount(rsSql.getDouble(6));
                    objItemDtl.setStrPosName(rsSql.getString(7));
                    objItemDtl.setStrWShortName(rsSql.getString(8));
                    objItemDtl.setStrReasonName(rsSql.getString(9));
                    objItemDtl.setStrRemarks(rsSql.getString(10));
                    objItemDtl.setStrGroupName(rsSql.getString(11));
                    objItemDtl.setStrKOTNo(rsSql.getString(12));
                    objItemDtl.setStrPOSCode(rsSql.getString(13));
                    objItemDtl.setStrTableName(rsSql.getString(14));
                    objItemDtl.setStrItemCode(rsSql.getString(15));

                    listOfCompliItemDtl.add(objItemDtl);

                }
                rsSql.close();
               

                Comparator<clsBillDtl> posNameComparator = new Comparator<clsBillDtl>()
                {

                    @Override
                    public int compare(clsBillDtl o1, clsBillDtl o2)
                    {
                        return o1.getStrPosName().compareToIgnoreCase(o2.getStrPosName());
                    }
                };

                Comparator<clsBillDtl> billDateComparator = new Comparator<clsBillDtl>()
                {

                    @Override
                    public int compare(clsBillDtl o1, clsBillDtl o2)
                    {
                        return o1.getDteBillDate().compareToIgnoreCase(o2.getDteBillDate());
                    }
                };
                Comparator<clsBillDtl> billNoComparator = new Comparator<clsBillDtl>()
                {

                    @Override
                    public int compare(clsBillDtl o1, clsBillDtl o2)
                    {
                        return o1.getStrBillNo().compareToIgnoreCase(o2.getStrBillNo());
                    }
                };
                Comparator<clsBillDtl> kotNoComparator = new Comparator<clsBillDtl>()
                {

                    @Override
                    public int compare(clsBillDtl o1, clsBillDtl o2)
                    {
                        return o1.getStrKOTNo().compareToIgnoreCase(o2.getStrKOTNo());
                    }
                };
                Comparator<clsBillDtl> itemCodeComparator = new Comparator<clsBillDtl>()
                {

                    @Override
                    public int compare(clsBillDtl o1, clsBillDtl o2)
                    {
                        return o1.getStrItemCode().substring(0, 7).compareToIgnoreCase(o2.getStrItemCode().substring(0, 7));
                    }
                };

                Collections.sort(listOfCompliItemDtl, new clsBillComplimentaryComparator(
                        posNameComparator, billDateComparator, billNoComparator, kotNoComparator, itemCodeComparator
                ));

                //call for view report
                if (reportType.equalsIgnoreCase("A4 Size Report"))
                {
                    funViewJasperReportForBeanCollectionDataSource(is, hm, listOfCompliItemDtl);
                }

                if (reportType.equalsIgnoreCase("Excel Report"))
                {
                    int i = 1;
                    DecimalFormat decFormat = new DecimalFormat("0");
                    //DecimalFormat decFormatFor2Decimal = new DecimalFormat("0.00");
                    for (clsBillDtl objBean : listOfCompliItemDtl)
                    {
                        List<String> arrListItem = new ArrayList<String>();
                        arrListItem.add(objBean.getStrBillNo());
                        arrListItem.add(objBean.getDteBillDate());
                        arrListItem.add(objBean.getStrItemName());
                        arrListItem.add(objBean.getStrGroupName());
                        arrListItem.add(String.valueOf(decFormat.format(objBean.getDblQuantity())));
                        arrListItem.add(String.valueOf(gDecimalFormat.format(objBean.getDblRate())));
                        arrListItem.add(String.valueOf(gDecimalFormat.format(objBean.getDblAmount())));
                        arrListItem.add(objBean.getStrPosName());
                        arrListItem.add(objBean.getStrWShortName());
                        arrListItem.add(objBean.getStrReasonName());
                        arrListItem.add(objBean.getStrRemarks());

                        totalQty = totalQty + Double.parseDouble(String.valueOf(decFormat.format(objBean.getDblQuantity())));
                        totalAmount = totalAmount + Double.parseDouble(String.valueOf(gDecimalFormat.format(objBean.getDblAmount())));
                        mapExcelItemDtl.put(i, arrListItem);
                        i++;
                    }
                    arrListTotal.add(String.valueOf(decFormat.format(totalQty)) + "#" + "5");
                    arrListTotal.add(String.valueOf(gDecimalFormat.format(totalAmount)) + "#" + "7");

                    arrHeaderList.add("Serial No");
                    arrHeaderList.add("Bill No");
                    arrHeaderList.add("Bill Date");
                    arrHeaderList.add("Item Name");
                    arrHeaderList.add("Group Name");
                    arrHeaderList.add("Qty");
                    arrHeaderList.add("Rate");
                    arrHeaderList.add("Amount");
                    arrHeaderList.add("POS");
                    arrHeaderList.add("Waiter");
                    arrHeaderList.add("Reason");
                    arrHeaderList.add("Remark");

                    List<String> arrparameterList = new ArrayList<String>();
                    arrparameterList.add("Complimentary SettlementWise Detail Report");
                    arrparameterList.add("POS" + " : " + posName.toString());
                    arrparameterList.add("FromDate" + " : " + fromDateToDisplay);
                    arrparameterList.add("ToDate" + " : " + toDateToDisplay);
                    arrparameterList.add("Reason" + " : " + reasonName);
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
                    funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "ComplimentaryWiseDetailExcelSheetReport",dayEnd);

                }
            }
            else//summary
            {
                //live data
                sbSqlLive.append("select ifnull(a.strBillNo,'')as strBillNo, ifnull(DATE_FORMAT(date(a.dteBillDate),'%d-%m-%Y'),'') as dteBillDate,ifnull(sum(b.dblRate*b.dblQuantity), 0) as dblAmount ,ifnull(f.strPosName,'') as strPosName,ifnull(g.strWShortName,'NA') as strWShortName, ifnull(e.strReasonName,'') as strReasonName, ifnull(a.strRemarks,'') as strRemarks  "
                        + "from tblbillhd a   "
                        + "INNER JOIN tblbillcomplementrydtl b on a.strBillNo = b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate) "
                        + "left outer join tblreasonmaster e on  a.strReasonCode = e.strReasonCode   "
                        + "left outer join tblposmaster f on a.strPOSCode=f.strPosCode   "
                        + "left outer join tblwaitermaster g on a.strWaiterNo=g.strWaiterNo  "
                        + "left outer join tbltablemaster h on  a.strTableNo=h.strTableNo  "
                        + "left outer join tblitemcurrentstk i on b.strItemCode=i.strItemCode  "
                        + "where  date(a.dteBillDate) Between '" + fromDate + "' and '" + toDate + "' ");
                //live modifiers
                sqlLiveModifierBuilder.append("select ifnull(a.strBillNo,''),ifnull(DATE_FORMAT(date(a.dteBillDate),'%d-%m-%Y'),'') as dteBillDate,ifnull(sum(b.dblQuantity*b.dblRate),0) as dblAmount ,ifnull(f.strPosName,'') as strPosName,ifnull(g.strWShortName,'NA') as strWShortName, ifnull(e.strReasonName,''), ifnull(a.strRemarks,'') as strRemarks "
                        + " from tblbillhd a"
                        + " INNER JOIN  tblbillmodifierdtl b on a.strBillNo = b.strBillNo"
                        + " left outer join  tblbillsettlementdtl c on a.strBillNo = c.strBillNo"
                        + " left outer join  tblsettelmenthd d on c.strSettlementCode = d.strSettelmentCode "
                        + " left outer join tblreasonmaster e on  a.strReasonCode = e.strReasonCode "
                        + " left outer join tblposmaster f on a.strPOSCode=f.strPosCode "
                        + " left outer join tblwaitermaster g on a.strWaiterNo=g.strWaiterNo"
                        + " left outer join tbltablemaster h on  a.strTableNo=h.strTableNo"
                        + " left outer join tblitemcurrentstk i on left(b.strItemCode,7)=i.strItemCode"
                        + " left outer join  tblbilldtl j on b.strBillNo = j.strBillNo  "
                        + " where d.strSettelmentType = 'Complementary' ");

                //Q data
                sbSqlQBill.append("select ifnull(a.strBillNo,'')as strBillNo, ifnull(DATE_FORMAT(date(a.dteBillDate),'%d-%m-%Y'),'') as dteBillDate,ifnull(sum(b.dblRate*b.dblQuantity), 0) as dblAmount ,ifnull(f.strPosName,'') as strPosName,ifnull(g.strWShortName,'NA') as strWShortName, ifnull(e.strReasonName,'') as strReasonName, ifnull(a.strRemarks,'') as strRemarks  "
                        + "from tblqbillhd a   "
                        + "INNER JOIN  tblqbillcomplementrydtl b on a.strBillNo = b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate) "
                        + "left outer join tblreasonmaster e on  a.strReasonCode = e.strReasonCode   "
                        + "left outer join tblposmaster f on a.strPOSCode=f.strPosCode   "
                        + "left outer join tblwaitermaster g on a.strWaiterNo=g.strWaiterNo  "
                        + "left outer join tbltablemaster h on  a.strTableNo=h.strTableNo  "
                        + "left outer join tblitemcurrentstk i on b.strItemCode=i.strItemCode  "
                        + "where  date(a.dteBillDate) Between '" + fromDate + "' and '" + toDate + "' ");

                //Q modifiers
                sqlQModifierBuilder.append("select ifnull(a.strBillNo,''),ifnull(DATE_FORMAT(date(a.dteBillDate),'%d-%m-%Y'),'') as dteBillDate,sum(b.dblQuantity*b.dblRate) as dblAmount ,ifnull(f.strPosName,''),ifnull(g.strWShortName,'NA') as strWShortName, e.strReasonName, a.strRemarks "
                        + " from tblqbillhd a"
                        + " INNER JOIN  tblqbillmodifierdtl b on a.strBillNo = b.strBillNo"
                        + " left outer join  tblqbillsettlementdtl c on a.strBillNo = c.strBillNo"
                        + " left outer join  tblsettelmenthd d on c.strSettlementCode = d.strSettelmentCode "
                        + " left outer join tblreasonmaster e on  a.strReasonCode = e.strReasonCode "
                        + " left outer join tblposmaster f on a.strPOSCode=f.strPosCode "
                        + " left outer join tblwaitermaster g on a.strWaiterNo=g.strWaiterNo"
                        + " left outer join tbltablemaster h on  a.strTableNo=h.strTableNo"
                        + " left outer join tblitemcurrentstk i on left(b.strItemCode,7)=i.strItemCode"
                        + " left outer join  tblqbilldtl j on b.strBillNo = j.strBillNo  "
                        + " where d.strSettelmentType = 'Complementary' ");

                if (!posCode.equals("All"))
                {
                    sbSqlLive.append(" AND a.strPOSCode = '" + posCode + "' ");
                    sbSqlQBill.append(" AND a.strPOSCode = '" + posCode + "' ");
                    sqlLiveModifierBuilder.append(" AND a.strPOSCode = '" + posCode + "' ");
                    sqlQModifierBuilder.append(" AND a.strPOSCode = '" + posCode + "' ");
                }
                if (!reasonCode.equals("All"))
                {
                    sbSqlLive.append(" and a.strReasonCode='" + reasonCode + "' ");
                    sbSqlQBill.append(" and a.strReasonCode='" + reasonCode + "' ");
                    sqlLiveModifierBuilder.append(" and a.strReasonCode='" + reasonCode + "' ");
                    sqlQModifierBuilder.append(" and a.strReasonCode='" + reasonCode + "' ");
                }
                if (clsGlobalVarClass.gEnableShiftYN)
                {
                    if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                    {
                        sbSqlLive.append(" and a.intShiftCode = '" + shiftNo + "' ");
                        sbSqlQBill.append(" and a.intShiftCode = '" + shiftNo + "' ");
                        sqlLiveModifierBuilder.append(" and a.intShiftCode = '" + shiftNo + "' ");
                        sqlQModifierBuilder.append(" and a.intShiftCode = '" + shiftNo + "' ");
                    }
                }
                sbSqlLive.append("  "
                        + " group by a.strPOSCode,a.strBillNo "
                        + " order by a.strPOSCode,a.strBillNo ");
                sbSqlQBill.append("  "
                        + " group by a.strPOSCode,a.strBillNo "
                        + " order by a.strPOSCode,a.strBillNo ");
                sqlLiveModifierBuilder.append(" and date(a.dteBillDate) Between '" + fromDate + "' and '" + toDate + "'  "
                        + " group by a.strPOSCode,a.strBillNo "
                        + " order by a.strPOSCode,a.strBillNo ");
                sqlQModifierBuilder.append(" and date(a.dteBillDate) Between '" + fromDate + "' and '" + toDate + "'  "
                        + " group by a.strPOSCode,a.strBillNo "
                        + " order by a.strPOSCode,a.strBillNo ");

                //live data
                ResultSet rsSql = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
                while (rsSql.next())
                {
                    clsBillDtl objItemDtl = new clsBillDtl();

                    objItemDtl.setStrBillNo(rsSql.getString(1));
                    objItemDtl.setDteBillDate(rsSql.getString(2));
                    objItemDtl.setDblAmount(rsSql.getDouble(3));
                    objItemDtl.setStrPosName(rsSql.getString(4));
                    objItemDtl.setStrWShortName(rsSql.getString(5));
                    objItemDtl.setStrReasonName(rsSql.getString(6));
                    objItemDtl.setStrRemarks(rsSql.getString(7));

                    listOfCompliItemDtl.add(objItemDtl);
                }
                rsSql.close();


                //QFile
                rsSql = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQBill.toString());
                while (rsSql.next())
                {
                    clsBillDtl objItemDtl = new clsBillDtl();

                    objItemDtl.setStrBillNo(rsSql.getString(1));
                    objItemDtl.setDteBillDate(rsSql.getString(2));
                    objItemDtl.setDblAmount(rsSql.getDouble(3));
                    objItemDtl.setStrPosName(rsSql.getString(4));
                    objItemDtl.setStrWShortName(rsSql.getString(5));
                    objItemDtl.setStrReasonName(rsSql.getString(6));
                    objItemDtl.setStrRemarks(rsSql.getString(7));

                    listOfCompliItemDtl.add(objItemDtl);

                }
                rsSql.close();


                Comparator<clsBillDtl> posNameComparator = new Comparator<clsBillDtl>()
                {

                    @Override
                    public int compare(clsBillDtl o1, clsBillDtl o2)
                    {
                        return o1.getStrPosName().compareToIgnoreCase(o2.getStrPosName());
                    }
                };

                Comparator<clsBillDtl> billDateComparator = new Comparator<clsBillDtl>()
                {

                    @Override
                    public int compare(clsBillDtl o1, clsBillDtl o2)
                    {
                        return o1.getDteBillDate().compareToIgnoreCase(o2.getDteBillDate());
                    }
                };
                Comparator<clsBillDtl> billNoComparator = new Comparator<clsBillDtl>()
                {

                    @Override
                    public int compare(clsBillDtl o1, clsBillDtl o2)
                    {
                        return o1.getStrBillNo().compareToIgnoreCase(o2.getStrBillNo());
                    }
                };

                Collections.sort(listOfCompliItemDtl, new clsBillComplimentaryComparator(
                        posNameComparator, billDateComparator, billNoComparator
                ));

                //call for view report
                if (reportType.equalsIgnoreCase("A4 Size Report"))
                {
                    funViewJasperReportForBeanCollectionDataSource(is, hm, listOfCompliItemDtl);
                }

                if (reportType.equalsIgnoreCase("Excel Report"))
                {

                    String sqlInsertLiveBillSales = "";
                    int count = 0;
                    int i = 1;
                    for (clsBillDtl objBean : listOfCompliItemDtl)
                    {

                        List<String> arrListItem = new ArrayList<String>();
                        arrListItem.add(objBean.getStrBillNo());
                        arrListItem.add(objBean.getDteBillDate());
                        arrListItem.add(String.valueOf(gDecimalFormat.format(objBean.getDblAmount())));
                        arrListItem.add(objBean.getStrPosName());
                        arrListItem.add(objBean.getStrWShortName());
                        arrListItem.add(objBean.getStrReasonName());
                        arrListItem.add(objBean.getStrRemarks());

                        totalAmount = totalAmount + objBean.getDblAmount();
                        mapExcelItemDtl.put(i, arrListItem);

                        i++;
                    }

                    arrListTotal.add(String.valueOf(gDecimalFormat.format(totalAmount)) + "#" + "3");

                    arrHeaderList.add("Serial No");
                    arrHeaderList.add("Bill No");
                    arrHeaderList.add("Bill Date");
                    arrHeaderList.add("Bill Amt");
                    arrHeaderList.add("POS Name");
                    arrHeaderList.add("Waiter");
                    arrHeaderList.add("Reason");
                    arrHeaderList.add("Remark");

                    List<String> arrparameterList = new ArrayList<String>();
                    arrparameterList.add("Complimentary SettlementWise Summary Report");
                    arrparameterList.add("POS" + " : " + posName);
                    arrparameterList.add("FromDate" + " : " + fromDateToDisplay);
                    arrparameterList.add("ToDate" + " : " + toDateToDisplay);
                    arrparameterList.add("Reason" + " : " + reasonName);
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
                    funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "ComplimentaryWiseSummaryExcelSheetReport", dayEnd);

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

}
