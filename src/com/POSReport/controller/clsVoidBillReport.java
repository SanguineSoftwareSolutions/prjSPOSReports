package com.POSReport.controller;

import com.POSReport.controller.comparator.clsVoidBillComparator;
import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsPosConfigFile;
import com.POSGlobal.controller.clsVoidBillDtl;
import java.awt.Desktop;
import java.awt.Dimension;
import java.io.File;
import java.io.InputStream;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
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

public class clsVoidBillReport
{
    DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();
    public void funVoidBillReport(String reportType, HashMap hm, String dayEnd)
    {
        try
        {
            Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
            List<String> arrListTotal = new ArrayList<String>();
            List<String> arrHeaderList = new ArrayList<String>();
            double totalAmount = 0;
            double totalQty = 0;
            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String type = hm.get("rptType").toString();
            String posName = hm.get("posName").toString();
            String reasonName = hm.get("reasonName").toString();
            String reasonCode = hm.get("reasonCode").toString();
            String fromDateToDisplay = hm.get("fromDateToDisplay").toString();
            String toDateToDisplay = hm.get("toDateToDisplay").toString();

            if (type.equalsIgnoreCase("Summary"))
            {
                StringBuilder sqlBuilder = new StringBuilder();
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptVoidBillReport.jasper");
                //Bill detail data
                sqlBuilder.setLength(0);
                sqlBuilder.append("select a.strBillNo,Date(a.dteBillDate) as BillDate,Date(a.dteModifyVoidBill) as VoidedDate, "
                        + " Time(a.dteBillDate) As EntryTime,Time(a.dteModifyVoidBill) VoidedTime, "
                        + " sum(b.dblAmount) as BillAmount,a.strReasonName as Reason,a.strUserEdited AS VoidedUser,a.strUserCreated CreatedUser,a.strRemark,a.strVoidBillType "
                        + " from tblvoidbillhd a,tblvoidbilldtl b "
                        + " where a.strBillNo=b.strBillNo "
                        + " and date(a.dteBillDate)=date(b.dteBillDate) "
                        + " and b.strTransType='VB' "
                        + " and a.strTransType='VB' "
                        + " and (a.dblModifiedAmount)>0 "
                        + " and Date(a.dteModifyVoidBill)  Between '" + fromDate + "' and '" + toDate + "' ");
                if (!posCode.equalsIgnoreCase("All"))
                {
                    sqlBuilder.append("and a.strPosCode='" + posCode + "' ");
                }
                if (!reasonName.equalsIgnoreCase("All"))
                {
                    sqlBuilder.append("and a.strReasonCode='" + reasonCode + "' ");
                }
                if (!shiftNo.equalsIgnoreCase("All"))
                {
                    sqlBuilder.append("and a.intShiftCode='" + shiftNo + "'  ");
                }
                sqlBuilder.append(" group by a.strBillNo ");

                ResultSet rsVoidData = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
                List<clsVoidBillDtl> listOfVoidBillData = new ArrayList<clsVoidBillDtl>();
                while (rsVoidData.next())
                {
                    String billDate = rsVoidData.getString(2);
                    String dateParts[] = billDate.split("-");
                    String dteBillDate = dateParts[2] + "-" + dateParts[1] + "-" + dateParts[0];

                    String voidedBillDate = rsVoidData.getString(2);
                    String dateParts1[] = voidedBillDate.split("-");
                    String dteVoidedBillDate = dateParts1[2] + "-" + dateParts1[1] + "-" + dateParts1[0];

                    clsVoidBillDtl objVoidBill = new clsVoidBillDtl();
                    objVoidBill.setStrBillNo(rsVoidData.getString(1));          //BillNo
                    objVoidBill.setDteBillDate(dteBillDate);        //Bill Date
                    objVoidBill.setStrWaiterNo(dteVoidedBillDate);        //Voided Date
                    objVoidBill.setStrTableNo(rsVoidData.getString(4));         //Entry Time
                    objVoidBill.setStrSettlementCode(rsVoidData.getString(5));  //Voided Time
                    objVoidBill.setDblAmount(rsVoidData.getDouble(6));          //Bill Amount
                    objVoidBill.setStrReasonName(rsVoidData.getString(7));      //Reason
                    objVoidBill.setStrVoidedUser(rsVoidData.getString(8));      //User voided
                    objVoidBill.setStrUserCreated(rsVoidData.getString(9));     //User Created
                    objVoidBill.setStrRemarks(rsVoidData.getString(10));         //Remarks   
                    objVoidBill.setStrVoidBillType(rsVoidData.getString(11));         //Void Bill Type

                    listOfVoidBillData.add(objVoidBill);
                }
                rsVoidData.close();

                //Bill Modifier data
                sqlBuilder.setLength(0);
                sqlBuilder.append("select a.strBillNo,Date(a.dteBillDate) as BillDate,Date(a.dteModifyVoidBill) as VoidedDate "
                        + " ,Time(a.dteBillDate) As EntryTime,Time(a.dteModifyVoidBill) VoidedTime "
                        + " ,sum(b.dblAmount) as BillAmount,a.strReasonName as Reason,a.strUserEdited AS VoidedUser,a.strUserCreated CreatedUser,b.strRemarks,a.strVoidBillType "
                        + " from tblvoidbillhd a, tblvoidmodifierdtl b "
                        + " where a.strBillNo=b.strBillNo "
                        + " and date(a.dteBillDate)=date(b.dteBillDate) "
                        + " and a.strTransType='VB' "
                        + " and (a.dblModifiedAmount)>0 "
                        + " and Date(a.dteModifyVoidBill) Between '" + fromDate + "' and '" + toDate + "' ");
                if (!posCode.equalsIgnoreCase("All"))
                {
                    sqlBuilder.append(" and a.strPosCode='" + posCode + "' ");
                }
                if (!reasonName.equalsIgnoreCase("All"))
                {
                    sqlBuilder.append("and a.strReasonCode='" + reasonCode + "' ");
                }
                if (!shiftNo.equalsIgnoreCase("All"))
                {
                    sqlBuilder.append(" and a.intShiftCode='" + shiftNo + "'  ");
                }
                sqlBuilder.append(" group by a.strBillNo  ");

                rsVoidData = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
                while (rsVoidData.next())
                {
                    String billDate = rsVoidData.getString(2);
                    String dateParts[] = billDate.split("-");
                    String dteBillDate = dateParts[2] + "-" + dateParts[1] + "-" + dateParts[0];

                    String voidedBillDate = rsVoidData.getString(2);
                    String dateParts1[] = voidedBillDate.split("-");
                    String dteVoidedBillDate = dateParts1[2] + "-" + dateParts1[1] + "-" + dateParts1[0];

                    clsVoidBillDtl objVoidBill = new clsVoidBillDtl();
                    objVoidBill.setStrBillNo(rsVoidData.getString(1));          //BillNo
                    objVoidBill.setDteBillDate(dteBillDate);        //Bill Date
                    objVoidBill.setStrWaiterNo(dteVoidedBillDate);        //Voided Date
                    objVoidBill.setStrTableNo(rsVoidData.getString(4));         //Entry Time
                    objVoidBill.setStrSettlementCode(rsVoidData.getString(5));  //Voided Time
                    objVoidBill.setDblAmount(rsVoidData.getDouble(6));          //Bill Amount
                    objVoidBill.setStrReasonName(rsVoidData.getString(7));      //Reason
                    objVoidBill.setStrVoidedUser(rsVoidData.getString(8));      //User Edited
                    objVoidBill.setStrUserCreated(rsVoidData.getString(9));     //User Created
                    objVoidBill.setStrRemarks(rsVoidData.getString(10));         //Remarks   
                    objVoidBill.setStrVoidBillType(rsVoidData.getString(11));         //Void Bill Type

                    listOfVoidBillData.add(objVoidBill);
                }
                rsVoidData.close();

                Comparator<clsVoidBillDtl> reasonNameComparator = new Comparator<clsVoidBillDtl>()
                {

                    @Override
                    public int compare(clsVoidBillDtl o1, clsVoidBillDtl o2)
                    {
                        return o1.getStrReasonName().compareToIgnoreCase(o2.getStrReasonName());
                    }
                };

                Comparator<clsVoidBillDtl> billDateComparator = new Comparator<clsVoidBillDtl>()
                {

                    @Override
                    public int compare(clsVoidBillDtl o1, clsVoidBillDtl o2)
                    {
                        return o1.getDteBillDate().compareToIgnoreCase(o2.getDteBillDate());
                    }
                };

                Comparator<clsVoidBillDtl> billNoComparator = new Comparator<clsVoidBillDtl>()
                {

                    @Override
                    public int compare(clsVoidBillDtl o1, clsVoidBillDtl o2)
                    {
                        return o1.getStrBillNo().compareToIgnoreCase(o2.getStrBillNo());
                    }
                };

                Collections.sort(listOfVoidBillData, new clsVoidBillComparator(
                        reasonNameComparator,
                        billDateComparator,
                        billNoComparator)
                );

                //call for view report
                if (reportType.equalsIgnoreCase("A4 Size Report"))
                {
                    funViewJasperReportForBeanCollectionDataSource(is, hm, listOfVoidBillData);
                }
                if (reportType.equalsIgnoreCase("Excel Report"))
                {
                    int i = 1;
                    //DecimalFormat gDecimalFormat = new DecimalFormat("0.00");
                    for (clsVoidBillDtl objBean : listOfVoidBillData)
                    {
                        List<String> arrListItem = new ArrayList<String>();
                        arrListItem.add(objBean.getStrBillNo());
                        arrListItem.add(objBean.getDteBillDate());
                        arrListItem.add(objBean.getStrWaiterNo());
                        arrListItem.add(objBean.getStrTableNo());
                        arrListItem.add(objBean.getStrSettlementCode());
                        arrListItem.add(objBean.getStrUserCreated());
                        arrListItem.add(objBean.getStrVoidedUser());
                        arrListItem.add(objBean.getStrReasonName());
                        arrListItem.add(String.valueOf(gDecimalFormat.format(objBean.getDblAmount())));
                        arrListItem.add(objBean.getStrRemarks());
                        arrListItem.add(objBean.getStrVoidBillType());

                        totalAmount = totalAmount + objBean.getDblAmount();

                        mapExcelItemDtl.put(i, arrListItem);

                        i++;

                    }

                    arrListTotal.add(String.valueOf(Math.rint(totalAmount)) + "#" + "9");

                    arrHeaderList.add("Serial No");
                    arrHeaderList.add("Bill No");
                    arrHeaderList.add("BillDate");
                    arrHeaderList.add("Voided Date");
                    arrHeaderList.add("Entry Time");
                    arrHeaderList.add("Voided Time");
                    arrHeaderList.add("Created User");
                    arrHeaderList.add("Voided User");
                    arrHeaderList.add("Reason");
                    arrHeaderList.add("Amount");
                    arrHeaderList.add("Remarks");
                    arrHeaderList.add("Void Bill Type");

                    List<String> arrparameterList = new ArrayList<String>();
                    arrparameterList.add("Void Bill Summary Report");
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

                    funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "voidBillDtlExcelSheet", dayEnd);

                }
            }
            else
            {
                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh.mm");
                StringBuilder sqlBuilder = new StringBuilder();
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptVoidBillReportDtl.jasper");
                //Bill detail data
                sqlBuilder.setLength(0);
                sqlBuilder.append("select a.strBillNo,DATE_FORMAT(a.dteBillDate,'%d-%m-%Y') as BillDate,DATE_FORMAT(a.dteModifyVoidBill,'%d-%m-%Y') as VoidedDate, "
                        + " TIME_FORMAT(a.dteBillDate, '%H:%i') AS EntryTime, TIME_FORMAT(a.dteModifyVoidBill, '%H:%i') VoidedTime,b.strItemName, "
                        + " sum(b.intQuantity),sum(b.dblAmount) as BillAmount,b.strReasonName as Reason, "
                        + " a.strUserEdited AS VoidedUser,a.strUserCreated CreatedUser,b.strRemarks,a.strVoidBillType "
                        + " from tblvoidbillhd a,tblvoidbilldtl b"
                        + " where a.strBillNo=b.strBillNo "
                        + " and date(a.dteBillDate)=date(b.dteBillDate) "
                        + " and b.strTransType='VB' "
                        + " and a.strTransType='VB'  "
                        + " and (a.dblModifiedAmount)>0 "
                        + " and Date(a.dteModifyVoidBill) Between '" + fromDate + "' and '" + toDate + "' ");
                if (!posCode.equalsIgnoreCase("All"))
                {
                    sqlBuilder.append("and a.strPosCode='" + posCode + "' ");
                }
                if (!reasonName.equalsIgnoreCase("All"))
                {
                    sqlBuilder.append("and a.strReasonCode='" + reasonCode + "' ");
                }
                if (!shiftNo.equalsIgnoreCase("All"))
                {
                    sqlBuilder.append("and a.intShiftCode='" + shiftNo + "'  ");
                }
                sqlBuilder.append("group by a.strBillNo,b.strItemCode ");

                ResultSet rsVoidData = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
                List<clsVoidBillDtl> listOfVoidBillData = new ArrayList<clsVoidBillDtl>();
                while (rsVoidData.next())
                {
//                    String billDate = rsVoidData.getString(2);
//                    String dateParts[] = billDate.split("-");
//                    String dteBillDate = dateParts[2] + "-" + dateParts[1] + "-" + dateParts[0];
//
//                    String voidedBillDate = rsVoidData.getString(2);
//                    String dateParts1[] = voidedBillDate.split("-");
//                    String dteVoidedBillDate = dateParts1[2] + "-" + dateParts1[1] + "-" + dateParts1[0];

                    clsVoidBillDtl objVoidBill = new clsVoidBillDtl();
                    objVoidBill.setStrBillNo(rsVoidData.getString(1));          //BillNo
                    objVoidBill.setDteBillDate(rsVoidData.getString(2));        //Bill Date
                    objVoidBill.setStrWaiterNo(rsVoidData.getString(3));        //Voided Date
                    objVoidBill.setStrTableNo(rsVoidData.getString(4));         //Entry Time
                    objVoidBill.setStrSettlementCode(rsVoidData.getString(5));  //Voided Time
                    objVoidBill.setStrItemName(rsVoidData.getString(6));        //ItemName
                    objVoidBill.setIntQuantity(rsVoidData.getDouble(7));        //Quantity
                    objVoidBill.setDblAmount(rsVoidData.getDouble(8));          //Bill Amount
                    objVoidBill.setStrReasonName(rsVoidData.getString(9));      //Reason
                    objVoidBill.setStrVoidedUser(rsVoidData.getString(10));      //User Edited
                    objVoidBill.setStrUserCreated(rsVoidData.getString(11));     //User Created
                    objVoidBill.setStrRemarks(rsVoidData.getString(12));         //Remarks   
                    objVoidBill.setStrVoidBillType(rsVoidData.getString(13));         //Void Bill Type

                    listOfVoidBillData.add(objVoidBill);
                }
                rsVoidData.close();

                //Bill Modifier data
                sqlBuilder.setLength(0);
                sqlBuilder.append("select a.strBillNo,DATE_FORMAT(a.dteBillDate,'%d-%m-%Y') as BillDate,DATE_FORMAT(a.dteModifyVoidBill,'%d-%m-%Y') as VoidedDate, "
                        + " TIME_FORMAT(a.dteBillDate, '%H:%i') AS EntryTime, TIME_FORMAT(a.dteModifyVoidBill, '%H:%i') VoidedTime,b.strModifierName, "
                        + " sum(b.dblQuantity),sum(b.dblAmount) as BillAmount,ifnull(c.strReasonName,'NA') as Reason, "
                        + " a.strUserEdited AS VoidedUser,a.strUserCreated CreatedUser,b.strRemarks,a.strVoidBillType "
                        + " from tblvoidbillhd a,tblvoidmodifierdtl b "
                        + " left outer join tblreasonmaster c on b.strReasonCode=c.strReasonCode "
                        + " where a.strBillNo=b.strBillNo "
                        + " and date(a.dteBillDate)=date(b.dteBillDate) "
                        + " and a.strTransType='VB' "
                        + " and (a.dblModifiedAmount)>0 "
                        + " and Date(a.dteModifyVoidBill) Between '" + fromDate + "' and '" + toDate + "' ");
                if (!posCode.equalsIgnoreCase("All"))
                {
                    sqlBuilder.append(" and a.strPosCode='" + posCode + "' ");
                }
                if (!reasonName.equalsIgnoreCase("All"))
                {
                    sqlBuilder.append("and a.strReasonCode='" + reasonCode + "' ");
                }
                if (!shiftNo.equalsIgnoreCase("All"))
                {
                    sqlBuilder.append(" and a.intShiftCode='" + shiftNo + "'  ");
                }
                sqlBuilder.append(" group by a.strBillNo,b.strModifierCode  ");

                rsVoidData = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
                while (rsVoidData.next())
                {
//                    String billDate = rsVoidData.getString(2);
//                    String dateParts[] = billDate.split("-");
//                    String dteBillDate = dateParts[2] + "-" + dateParts[1] + "-" + dateParts[0];
//
//                    String voidedBillDate = rsVoidData.getString(2);
//                    String dateParts1[] = voidedBillDate.split("-");
//                    String dteVoidedBillDate = dateParts1[2] + "-" + dateParts1[1] + "-" + dateParts1[0];

                    clsVoidBillDtl objVoidBill = new clsVoidBillDtl();
                    objVoidBill.setStrBillNo(rsVoidData.getString(1));          //BillNo
                    objVoidBill.setDteBillDate(rsVoidData.getString(2));        //Bill Date
                    objVoidBill.setStrWaiterNo(rsVoidData.getString(3));        //Voided Date
                    objVoidBill.setStrTableNo(rsVoidData.getString(4));         //Entry Time
                    objVoidBill.setStrSettlementCode(rsVoidData.getString(5));  //Voided Time
                    objVoidBill.setStrItemName(rsVoidData.getString(6));        //ItemName
                    objVoidBill.setIntQuantity(rsVoidData.getDouble(7));        //Quantity
                    objVoidBill.setDblAmount(rsVoidData.getDouble(8));          //Bill Amount
                    objVoidBill.setStrReasonName(rsVoidData.getString(9));      //Reason
                    objVoidBill.setStrVoidedUser(rsVoidData.getString(10));      //User Edited
                    objVoidBill.setStrUserCreated(rsVoidData.getString(11));     //User Created
                    objVoidBill.setStrRemarks(rsVoidData.getString(12));         //Remarks   
                    objVoidBill.setStrVoidBillType(rsVoidData.getString(11));         //Void Bill Type

                    listOfVoidBillData.add(objVoidBill);
                }
                rsVoidData.close();

                Comparator<clsVoidBillDtl> reasonNameComparator = new Comparator<clsVoidBillDtl>()
                {

                    @Override
                    public int compare(clsVoidBillDtl o1, clsVoidBillDtl o2)
                    {
                        return o1.getStrReasonName().compareToIgnoreCase(o2.getStrReasonName());
                    }
                };

                Comparator<clsVoidBillDtl> billDateComparator = new Comparator<clsVoidBillDtl>()
                {

                    @Override
                    public int compare(clsVoidBillDtl o1, clsVoidBillDtl o2)
                    {
                        return o1.getDteBillDate().compareToIgnoreCase(o2.getDteBillDate());
                    }
                };

                Comparator<clsVoidBillDtl> billNoComparator = new Comparator<clsVoidBillDtl>()
                {

                    @Override
                    public int compare(clsVoidBillDtl o1, clsVoidBillDtl o2)
                    {
                        return o1.getStrBillNo().compareToIgnoreCase(o2.getStrBillNo());
                    }
                };

                Collections.sort(listOfVoidBillData, new clsVoidBillComparator(
                        reasonNameComparator,
                        billDateComparator,
                        billNoComparator)
                );

                //call for view report
                if (reportType.equalsIgnoreCase("A4 Size Report"))
                {
                    funViewJasperReportForBeanCollectionDataSource(is, hm, listOfVoidBillData);
                }
                if (reportType.equalsIgnoreCase("Excel Report"))
                {
                    int i = 1;
                    DecimalFormat decFormat = new DecimalFormat("0");
                    //DecimalFormat gDecimalFormat = new DecimalFormat("0.00");
                    for (clsVoidBillDtl objBean : listOfVoidBillData)
                    {
                        List<String> arrListItem = new ArrayList<String>();
                        arrListItem.add(objBean.getStrBillNo());//billno
                        arrListItem.add(objBean.getDteBillDate());//bill date
                        arrListItem.add(objBean.getStrWaiterNo());//voided date
                        arrListItem.add(objBean.getStrItemName());//item name                    
                        arrListItem.add(String.valueOf(decFormat.format(objBean.getIntQuantity())));//qty
                        arrListItem.add(String.valueOf(gDecimalFormat.format(objBean.getDblAmount())));//amt
                        arrListItem.add(objBean.getStrUserCreated());//User
                        arrListItem.add(objBean.getStrVoidedUser());//User voided
                        arrListItem.add(objBean.getStrReasonName());//reason
                        arrListItem.add(objBean.getStrRemarks());//remarks
                        arrListItem.add(objBean.getStrVoidBillType());//void type

                        totalQty = totalQty + objBean.getIntQuantity();
                        totalAmount = totalAmount + objBean.getDblAmount();

                        mapExcelItemDtl.put(i, arrListItem);

                        i++;

                    }

                    arrListTotal.add(String.valueOf(decFormat.format(totalQty)) + "#" + "5");
                    arrListTotal.add(String.valueOf(gDecimalFormat.format(totalAmount)) + "#" + "6");

                    arrHeaderList.add("Serial No");
                    arrHeaderList.add("Bill No");
                    arrHeaderList.add("BillDate");
                    arrHeaderList.add("Voided Date");
                    arrHeaderList.add("Item Name");
                    arrHeaderList.add("Qty");
                    arrHeaderList.add("Amount");
                    arrHeaderList.add("Created User");
                    arrHeaderList.add("Voided User");
                    arrHeaderList.add("Remarks");
                    arrHeaderList.add("Void Bill Type");

                    List<String> arrparameterList = new ArrayList<String>();
                    arrparameterList.add("Void Bill Detail Report");
                    arrparameterList.add("POS" + " : " + posName);
                    arrparameterList.add("FromDate" + " : " + fromDateToDisplay);
                    arrparameterList.add("ToDate" + " : " + toDateToDisplay);
                    arrparameterList.add("Reason" + " : " + reasonName);
                    arrparameterList.add(" ");
                    arrparameterList.add(" ");

                    funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "voidBillDtlExcelSheet", dayEnd);

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
