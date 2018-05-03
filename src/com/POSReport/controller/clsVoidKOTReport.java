package com.POSReport.controller;

import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsVoidBillDtl;
import java.awt.Desktop;
import java.awt.Dimension;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.sql.ResultSet;
import java.text.DecimalFormat;
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
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.swing.JRViewer;

public class clsVoidKOTReport
{

    DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();

    public void funVoidKOTReport(String reportType, HashMap hm, String dayEnd, String reportSubType)
    {
        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptVoidKOTReport.jasper");
            InputStream isForSubReport = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptVoidKOTSubReportForWaiterWiseVoidedKOT.jasper");

            hm.put("rptVoidKOTSubReportForWaiterWiseVoidedKOT", isForSubReport);

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String posName = hm.get("posName").toString();
            String fromDateToDisplay = hm.get("fromDateToDisplay").toString();
            String toDateToDisplay = hm.get("toDateToDisplay").toString();

            //DecimalFormat gDecimalFormat = new DecimalFormat("0.00");
            DecimalFormat decimalFormat0Decimal = new DecimalFormat("0");

            int totalKOTs = 0, totalItems = 0, totalVoidedKOTs = 0, totalVoidedItems = 0;
            StringBuilder sqlBuilder = new StringBuilder();

            sqlBuilder.setLength(0);
            sqlBuilder.append("SELECT COUNT(distinct b.strKOTNo), SUM(b.dblQuantity)\n"
                    + " FROM tblbillhd a,tblbilldtl b,tbltablemaster c,tblwaitermaster d\n"
                    + " WHERE a.strBillNo=b.strBillNo AND DATE(a.dteBillDate)= DATE(b.dteBillDate) \n"
                    + " AND a.strTableNo=c.strTableNo AND b.strWaiterNo=d.strWaiterNo AND LENGTH(b.strKOTNo)>0 \n"
                    + " AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' and '" + toDate + "'");
            ResultSet rsNoOfKotData = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());

            while (rsNoOfKotData.next())

            {
                totalKOTs = totalKOTs + rsNoOfKotData.getInt(1);
                totalItems = totalItems + rsNoOfKotData.getInt(2);

            }
            rsNoOfKotData.close();

            StringBuilder sqlQBuilder = new StringBuilder();
            sqlQBuilder.append("SELECT COUNT(distinct b.strKOTNo), SUM(b.dblQuantity)\n"
                    + " FROM tblqbillhd a,tblqbilldtl b,tbltablemaster c,tblwaitermaster d\n"
                    + " WHERE a.strBillNo=b.strBillNo AND DATE(a.dteBillDate)= DATE(b.dteBillDate) \n"
                    + " AND a.strTableNo=c.strTableNo AND b.strWaiterNo=d.strWaiterNo AND LENGTH(b.strKOTNo)>0 \n"
                    + " AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' and '" + toDate + "'");
            rsNoOfKotData = clsGlobalVarClass.dbMysql.executeResultSet(sqlQBuilder.toString());
            while (rsNoOfKotData.next())

            {

                totalKOTs = totalKOTs + rsNoOfKotData.getInt(1);
                totalItems = totalItems + rsNoOfKotData.getInt(2);

            }
            rsNoOfKotData.close();

            sqlBuilder.setLength(0);
            sqlBuilder.append("SELECT COUNT(distinct b.strKOTNo),sum(b.intQuantity)\n"
                    + "FROM tblvoidbillhd a,tblvoidbilldtl b,tbltablemaster c,tblwaitermaster d\n"
                    + "WHERE a.strBillNo=b.strBillNo AND DATE(a.dteBillDate)= DATE(b.dteBillDate) \n"
                    + "AND a.strTableNo=c.strTableNo AND a.strWaiterNo=d.strWaiterNo AND LENGTH(b.strKOTNo)>2 \n"
                    + "AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' and '" + toDate + "'\n");
            ResultSet rsVoidedBilledKOTs = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
            while (rsVoidedBilledKOTs.next())

            {

                totalKOTs = totalKOTs + rsVoidedBilledKOTs.getInt(1);
                totalItems = totalItems + rsVoidedBilledKOTs.getInt(2);

            }
            rsVoidedBilledKOTs.close();

            sqlBuilder.setLength(0);
            sqlBuilder.append("SELECT COUNT(distinct a.strKOTNo),sum(a.dblItemQuantity)\n"
                    + "FROM tbllinevoid a\n"
                    + "WHERE LENGTH(a.strKOTNo)>2 AND DATE(a.dteDateCreated) BETWEEN '" + fromDate + "' and '" + toDate + "'\n"
            );
            ResultSet rsLineVoidedKOTs = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
            while (rsLineVoidedKOTs.next())

            {

                totalKOTs = totalKOTs + rsLineVoidedKOTs.getInt(1);
                totalItems = totalItems + rsLineVoidedKOTs.getInt(2);

            }
            rsLineVoidedKOTs.close();

            sqlBuilder.setLength(0);
            sqlBuilder.append("SELECT COUNT(distinct a.strKOTNo),sum(a.dblItemQuantity) "
                    + "FROM tblvoidkot a,tbltablemaster b,tblwaitermaster c,tblreasonmaster d "
                    + "WHERE a.strTableNo=b.strTableNo AND a.strWaiterNo=c.strWaiterNo "
                    + " AND a.strReasonCode=d.strReasonCode  "
                    + "AND LENGTH(a.strKOTNo)>2 "
                    + " and LEFT(a.strItemName,3)!='-->' "
                    + " AND DATE(a.dteDateCreated) BETWEEN '" + fromDate + "' and '" + toDate + "'  ");
            if (reportSubType.equalsIgnoreCase("All"))
            {
                sqlBuilder.append(" and a.strType!='MVKot' ");
            }

            ResultSet rsVoidedKOT = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
            while (rsVoidedKOT.next())
            {

                totalKOTs = totalKOTs + rsVoidedKOT.getInt(1);
                totalItems = totalItems + rsVoidedKOT.getInt(2);

            }
            rsVoidedKOT.close();

            sqlBuilder.setLength(0);
            sqlBuilder.append("SELECT COUNT(distinct a.strKOTNo),sum(a.dblQuantity) "
                    + "FROM tblnonchargablekot a,tbltablemaster b,tblreasonmaster c "
                    + "WHERE LENGTH(a.strKOTNo)>2 AND a.strTableNo=b.strTableNo AND a.strReasonCode=c.strReasonCode  "
                    + "AND DATE(a.dteNCKOTDate) BETWEEN '" + fromDate + "' and '" + toDate + "' "
                    + " "
            );
            ResultSet rsNCKOTs = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
            while (rsNCKOTs.next())

            {

                totalKOTs = totalKOTs + rsNCKOTs.getInt(1);
                totalItems = totalItems + rsNCKOTs.getInt(2);

            }
            rsNCKOTs.close();

            //KOT data
            List<clsVoidBillDtl> listOfVoidKOTData = new ArrayList<clsVoidBillDtl>();
            sqlBuilder.setLength(0);
            sqlBuilder.append("select a.strItemCode,a.strItemName,ifnull(d.strTableName,''),"
                    + " (a.dblAmount/a.dblItemQuantity) as dblRate,sum(a.dblItemQuantity)as dblItemQuantity,sum(a.dblAmount) as dblAmount "
                    + " ,a.strRemark,a.strKOTNo,a.strPosCode,b.strPosName,a.strUserCreated ,DATE_FORMAT(a.dteVoidedDate,'%d-%m-%Y %H:%i'),ifnull(c.strReasonName,'')"
                    + ",ifnull(e.strWShortName,''),if(a.strVoidBillType='N','Move KOT',a.strVoidBillType),DATE_FORMAT(a.dteDateCreated,'%d-%m-%Y %H:%i'),if(LEFT(a.strItemName,3)='-->','Y','N') isModifier  "
                    + " from tblvoidkot a "
                    + " left outer join tblposmaster b on a.strPOSCode=b.strPosCode "
                    + " left outer join tblreasonmaster c on a.strReasonCode=c.strReasonCode "
                    + " left outer join tbltablemaster d on a.strTableNo=d.strTableNo"
                    + " left outer join tblwaitermaster e on a.strWaiterNo=e.strWaiterNo "
                    + " where date(a.dteVoidedDate) Between '" + fromDate + "' and '" + toDate + "' "
                    + "  ");
            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and a.strPosCode='" + posCode + "' ");
            }

            if (reportSubType.equalsIgnoreCase("All"))
            {
                sqlBuilder.append(" and a.strType!='MVKot' ");
            }
            else if (reportSubType.equalsIgnoreCase("Void KOT"))
            {
                sqlBuilder.append(" and (a.strType='VKot' or a.strType='DVKot') ");
            }
            else if (reportSubType.equalsIgnoreCase("Move KOT"))
            {
                sqlBuilder.append(" and a.strType='MVKot' ");
            }

            sqlBuilder.append(" group by a.strposcode,a.strusercreated,a.strkotno,a.strItemCode "
                    + " having  dblAmount>if(isModifier='Y',0,-1) "
                    + " order by a.strposcode,a.strusercreated,a.strkotno,a.strItemCode ");

            ResultSet rsVoidData = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
            while (rsVoidData.next())

            {
                clsVoidBillDtl objVoidBill = new clsVoidBillDtl();
                objVoidBill.setStrItemCode(rsVoidData.getString(1));        //ItemCode
                objVoidBill.setStrItemName(rsVoidData.getString(2));        //ItemName
                objVoidBill.setStrTableNo(rsVoidData.getString(3));         //Table Name
                objVoidBill.setDblPaidAmt(rsVoidData.getDouble(4));         //Rate
                objVoidBill.setIntQuantity(rsVoidData.getDouble(5));        //Qty
                objVoidBill.setDblAmount(rsVoidData.getDouble(6));          //Amount
                objVoidBill.setStrRemarks(rsVoidData.getString(7));         //Remarks   
                objVoidBill.setStrKOTNo(rsVoidData.getString(8));           //KOT No  
                objVoidBill.setStrClientCode(rsVoidData.getString(9));      //POS Code
                objVoidBill.setStrPosCode(rsVoidData.getString(10));        //POS Name 
                objVoidBill.setStrUserCreated(rsVoidData.getString(11));    //User Created
                objVoidBill.setDteBillDate(rsVoidData.getString(12));       //Voided Date
                objVoidBill.setStrReasonName(rsVoidData.getString(13));     //Reason
                objVoidBill.setStrWaiterName(rsVoidData.getString(14));     //waiter
                objVoidBill.setStrVoidBillType(rsVoidData.getString(15));     //Void Type
                objVoidBill.setDteCreatedDate(rsVoidData.getString(16));     //kot time
                objVoidBill.setIntNoOfKot(totalKOTs);
                objVoidBill.setIntNoOfQty(totalItems);

                listOfVoidKOTData.add(objVoidBill);
            }
            rsVoidData.close();

            //which is not modifiers
            sqlBuilder.setLength(0);
            sqlBuilder.append("SELECT COUNT(distinct a.strKOTNo),sum(a.dblItemQuantity)\n"
                    + "FROM tblvoidkot a \n"
                    + "WHERE DATE(a.dteVoidedDate) BETWEEN '" + fromDate + "' and '" + toDate + "' "
                    + " and LEFT(a.strItemName,3)!='-->' "
                    + " and a.strType!='MVKot' ");

            ResultSet rsVoidedKotCount = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
            while (rsVoidedKotCount.next())
            {
                totalVoidedKOTs = rsVoidedKotCount.getInt(1);
                totalVoidedItems = rsVoidedKotCount.getInt(2);
            }
            rsVoidedKotCount.close();

            //which is modifiers but chargable
            sqlBuilder.setLength(0);
            sqlBuilder.append("SELECT COUNT(distinct a.strKOTNo),sum(a.dblItemQuantity)\n"
                    + "FROM tblvoidkot a \n"
                    + "WHERE DATE(a.dteVoidedDate) BETWEEN '" + fromDate + "' and '" + toDate + "' "
                    + " and LEFT(a.strItemName,3)='-->' "
                    + "and a.dblAmount>0 "
                    + " and a.strType!='MVKot' ");
            rsVoidedKotCount = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
            while (rsVoidedKotCount.next())
            {
//                voidedKotCount = voidedKotCount + rsVoidedKotCount.getInt(1);
                totalVoidedItems = totalVoidedItems + rsVoidedKotCount.getInt(2);
            }
            rsVoidedKotCount.close();

            double voidedKotPer = 0.0, voidedItemPer = 0.0;
            double auditTotal = totalVoidedKOTs + totalKOTs;
            if (totalKOTs != 0)
            {
                voidedKotPer = (double) totalVoidedKOTs / (double) totalKOTs * 100;
            }
            else
            {
                voidedKotPer = (double) totalVoidedKOTs;
            }
            if (totalItems != 0)
            {
                voidedItemPer = (double) totalVoidedItems / (double) totalItems * 100;
            }
            else
            {
                voidedItemPer = (double) totalVoidedKOTs;
            }
            voidedKotPer = Double.parseDouble(gDecimalFormat.format(voidedKotPer));
            voidedItemPer = Double.parseDouble(gDecimalFormat.format(voidedItemPer));
            String rptHeading = "Voided KOT Report";
            if (reportSubType.equals("Move KOT"))
            {
                rptHeading = "Moved KOT Report";
            }
            else
            {
                rptHeading = "Voided KOT Report";
            }
            hm.put("rptHeading", rptHeading);
            hm.put("noOfKot", totalKOTs);
            hm.put("voidedKotCount", totalVoidedKOTs);
            hm.put("voidedKotPer", voidedKotPer);
            hm.put("voidedItemPer", voidedItemPer);
            hm.put("voidedItemsCount", totalVoidedItems);
            hm.put("auditTotal", auditTotal);

            Map<String, Map<String, String>> mapWaiterKOTs = new HashMap<>();
            Map<String, Integer> mapWaiterKOTCount = new HashMap<>();
            for (clsVoidBillDtl objKOT : listOfVoidKOTData)
            {
                String waiterName = objKOT.getStrWaiterName();
                String kot = objKOT.getStrKOTNo();

                if (mapWaiterKOTCount.containsKey(waiterName))
                {
                    Map<String, String> map = mapWaiterKOTs.get(waiterName);

                    String key = waiterName + "!" + kot;
                    if (map.containsKey(key))
                    {
                        //ignore
                    }
                    else
                    {
                        mapWaiterKOTCount.put(waiterName, mapWaiterKOTCount.get(waiterName) + 1);

                        map.put(key, key);
                    }
                }
                else
                {
                    Map<String, String> map = new HashMap<>();

                    String key = waiterName + "!" + kot;
                    map.put(key, key);

                    mapWaiterKOTs.put(waiterName, map);

                    mapWaiterKOTCount.put(waiterName, 1);

                }

            }
            List<clsVoidBillDtl> listOfWaiterWiseKOT = new ArrayList<>();
            if (!reportSubType.equals("Move KOT"))
            {
                for (Map.Entry<String, Integer> entry : mapWaiterKOTCount.entrySet())
                {
                    String waiterName = entry.getKey();
                    int kotCount = entry.getValue();

                    clsVoidBillDtl objBillDtl = new clsVoidBillDtl();
                    objBillDtl.setStrWaiterName(waiterName);
                    objBillDtl.setIntNoOfKot(kotCount);
                    objBillDtl.setIntTotalVoidKOTs(totalVoidedKOTs);

                    double voidKOTPer = 0.00;
                    if (totalVoidedKOTs > 0)
                    {
                        voidKOTPer = (double) ((double) kotCount / (double) totalVoidedKOTs) * 100;
                    }
                    objBillDtl.setDblVoidedKOTPer(voidKOTPer);

                    listOfWaiterWiseKOT.add(objBillDtl);
                }
            }

            hm.put("listOfWaiterWiseKOT", listOfWaiterWiseKOT);

            //call for view report
            if (reportType.equalsIgnoreCase("A4 Size Report"))
            {
                if (listOfVoidKOTData.size() > 0 )
                {
                    funViewJasperReportForBeanCollectionDataSource(is, hm, listOfVoidKOTData);
                }
		else if(dayEnd.equalsIgnoreCase("NO"))
                {
                    JOptionPane.showMessageDialog(null, "Data not present for selected dates!!!");
                }
            }
            if (reportType.equalsIgnoreCase("Excel Report"))
            {
                if (listOfVoidKOTData.size() > 0)
                {
                    Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
                    List<String> arrListTotal = new ArrayList<String>();
                    List<String> arrHeaderList = new ArrayList<String>();
                    double totalAmount = 0;
                    double totalQty = 0;
                    int i = 1;
                    for (clsVoidBillDtl objBean : listOfVoidKOTData)
                    {
                        List<String> arrListItem = new ArrayList<String>();
                        arrListItem.add(objBean.getStrKOTNo());
                        //arrListItem.add(objBean.getStrPosCode());
                        arrListItem.add(objBean.getDteCreatedDate());
                        arrListItem.add(objBean.getDteBillDate());
                        arrListItem.add(objBean.getStrItemName());
                        arrListItem.add(objBean.getStrUserCreated());
                        arrListItem.add(objBean.getStrWaiterName());
                        arrListItem.add(decimalFormat0Decimal.format(objBean.getIntQuantity()));
                        arrListItem.add(gDecimalFormat.format(objBean.getDblPaidAmt()));
                        arrListItem.add(gDecimalFormat.format(objBean.getDblAmount()));
                        arrListItem.add(objBean.getStrRemarks());
                        arrListItem.add(objBean.getStrReasonName());
                        arrListItem.add(objBean.getStrVoidBillType());
                        totalQty = totalQty + Double.parseDouble(decimalFormat0Decimal.format(objBean.getIntQuantity()));
                        totalAmount = totalAmount + Double.parseDouble(gDecimalFormat.format(objBean.getDblAmount()));
                        mapExcelItemDtl.put(i, arrListItem);
                        i++;
                    }
                    arrListTotal.add(String.valueOf(decimalFormat0Decimal.format(totalQty)) + "#" + "7");
                    arrListTotal.add(String.valueOf(gDecimalFormat.format(totalAmount)) + "#" + "9");

                    arrHeaderList.add("Serial No");
                    arrHeaderList.add("KOT No");
                    //arrHeaderList.add("POS Name");
                    arrHeaderList.add("Created Date");
                    arrHeaderList.add("Voided Date");
                    arrHeaderList.add("Item");
                    arrHeaderList.add("User");
                    arrHeaderList.add("Waiter");
                    arrHeaderList.add("Qty");
                    arrHeaderList.add("Rate");
                    arrHeaderList.add("Amount");
                    arrHeaderList.add("Remark");
                    arrHeaderList.add("Reason");
                    arrHeaderList.add("Void Type");

                    List<String> arrparameterList = new ArrayList<String>();
                    if (reportSubType.equals("Move KOT"))
                    {
                        arrparameterList.add("Moved KOT Report");
                    }
                    else
                    {
                        arrparameterList.add("Voided KOT Report");
                    }

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

                    if (reportSubType.equals("Move KOT"))
                    {
                        funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "MovedKOTExcelSheet", dayEnd);
                    }
                    else
                    {
                        funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "VoidedKOTExcelSheet", dayEnd);
                    }

                }
		else if(dayEnd.equalsIgnoreCase("NO"))
                {
                    JOptionPane.showMessageDialog(null, "Data not present for selected dates!!!");
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
            JRViewer viewer = new JRViewer(print);
            JFrame jf = new JFrame();
            jf.getContentPane().add(viewer);
            jf.validate();
            jf.setVisible(true);
            jf.setSize(new Dimension(850, 750));
            //jf.setLocationRelativeTo(this);
            //export to other format xls,xlsx,pdf,etc
            //funExportToOtherFormat(print);
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
