/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.POSReport.controller;

import com.POSReport.controller.comparator.clsRevenueHeadComparator;
import com.POSReport.controller.comparator.clsBillComparator;
import com.POSReport.controller.comparator.clsBillComplimentaryComparator;
import com.POSReport.controller.comparator.clsItemConsumptionComparator;
import com.POSReport.controller.comparator.clsWaiterWiseSalesComparator;
import com.POSReport.controller.comparator.clsCreditBillReportComparator;
import com.POSReport.controller.comparator.clsGroupSubGroupComparator;
import com.POSGlobal.controller.clsBillDtl;
import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsGroupSubGroupWiseSales;
import com.POSGlobal.controller.clsItemWiseConsumption;
import com.POSGlobal.controller.clsSalesFlashReport;
import com.POSGlobal.controller.clsTaxCalculationDtls;
import com.POSGlobal.controller.clsVoidBillDtl;
import com.lowagie.text.pdf.PdfName;
import java.awt.Dimension;
import java.io.InputStream;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.swing.JRViewer;

public class clsUtilityForSalesReport
{

    private String reportName;
    private HashMap hm;
    private Map<String, List<Map<String, clsGroupSubGroupWiseSales>>> mapPOSDtlForGroupSubGroup;

    public clsUtilityForSalesReport()
    {
    }

    public clsUtilityForSalesReport(String reportName, HashMap hm)
    {
        this.reportName = reportName;
        this.hm = hm;
    }

    public void funBillWiseSalesReportForJasper()
    {
        try
        {

            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();

            Map mapMultiSettleBills = new HashMap();

            StringBuilder sqlBuilder = new StringBuilder();
            //live
            sqlBuilder.setLength(0);
            sqlBuilder.append("select a.strBillNo,DATE_FORMAT(date(a.dteBillDate),'%d-%m-%Y') as dteBillDate ,b.strPosName, "
                    + "ifnull(d.strSettelmentDesc,'') as strSettelmentMode,a.dblDiscountAmt,a.dblTaxAmt  "
                    + ",sum(c.dblSettlementAmt) as dblSettlementAmt,a.dblSubTotal,a.strSettelmentMode,intBillSeriesPaxNo "
                    + "from  tblbillhd a,tblposmaster b,tblbillsettlementdtl c,tblsettelmenthd d "
                    + "where date(a.dteBillDate) between '" + fromDate + "' and  '" + toDate + "' "
                    + "and a.strPOSCode=b.strPOSCode "
                    + "and a.strBillNo=c.strBillNo "
                    + "and c.strSettlementCode=d.strSettelmentCode "
                    + "and date(a.dteBillDate)=date(c.dteBillDate) "
                    + "and a.strClientCode=c.strClientCode ");
            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and a.strPOSCode='" + posCode + "' ");
            }
            if (!shiftNo.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and a.intShiftCode='" + shiftNo + "'  ");
            }
            sqlBuilder.append("GROUP BY a.strClientCode,date(a.dteBillDate),a.strBillNo,d.strSettelmentCode "
                    + "ORDER BY a.strBillNo ASC ");

            ResultSet rsData = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
            List<clsBillItemDtlBean> listOfBillData = new ArrayList<clsBillItemDtlBean>();
            while (rsData.next())
            {
                String key = rsData.getString(1) + "!" + rsData.getString(2);

                clsBillItemDtlBean obj = new clsBillItemDtlBean();
                if (mapMultiSettleBills.containsKey(key))//billNo
                {
                    obj.setStrBillNo(rsData.getString(1));
                    obj.setDteBillDate(rsData.getString(2));
                    obj.setStrPosName(rsData.getString(3));
                    obj.setStrSettelmentMode(rsData.getString(4));
                    obj.setDblDiscountAmt(0.00);
                    obj.setDblTaxAmt(0.00);
                    obj.setDblSettlementAmt(rsData.getDouble(7));
                    obj.setDblSubTotal(0.00);
                    obj.setIntBillSeriesPaxNo(0);
                }
                else
                {
                    obj.setStrBillNo(rsData.getString(1));
                    obj.setDteBillDate(rsData.getString(2));
                    obj.setStrPosName(rsData.getString(3));
                    obj.setStrSettelmentMode(rsData.getString(4));
                    obj.setDblDiscountAmt(rsData.getDouble(5));
                    obj.setDblTaxAmt(rsData.getDouble(6));
                    obj.setDblSettlementAmt(rsData.getDouble(7));
                    obj.setDblSubTotal(rsData.getDouble(8));
                    obj.setIntBillSeriesPaxNo(rsData.getInt(10));
                }
                listOfBillData.add(obj);

                if (rsData.getString(9).equalsIgnoreCase("MultiSettle"))
                {
                    mapMultiSettleBills.put(key, rsData.getString(1));
                }
            }

            //QFile
            sqlBuilder.setLength(0);
            sqlBuilder.append("select a.strBillNo,DATE_FORMAT(date(a.dteBillDate),'%d-%m-%Y') as dteBillDate ,b.strPosName, "
                    + "ifnull(d.strSettelmentDesc,'') as strSettelmentMode,a.dblDiscountAmt,a.dblTaxAmt   "
                    + ",sum(c.dblSettlementAmt) as dblSettlementAmt,a.dblSubTotal,a.strSettelmentMode,intBillSeriesPaxNo "
                    + "from  tblqbillhd a,tblposmaster b,tblqbillsettlementdtl c,tblsettelmenthd d "
                    + "where date(a.dteBillDate) between '" + fromDate + "' and  '" + toDate + "' "
                    + "and a.strPOSCode=b.strPOSCode "
                    + "and a.strBillNo=c.strBillNo "
                    + "and c.strSettlementCode=d.strSettelmentCode "
                    + "and date(a.dteBillDate)=date(c.dteBillDate) "
                    + "and a.strClientCode=c.strClientCode ");
            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and a.strPOSCode='" + posCode + "' ");
            }
            if (!shiftNo.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and a.intShiftCode='" + shiftNo + "'  ");
            }
            sqlBuilder.append("GROUP BY a.strClientCode,date(a.dteBillDate),a.strBillNo,d.strSettelmentCode "
                    + "ORDER BY a.strBillNo ASC ");

            rsData = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
            while (rsData.next())
            {
                String key = rsData.getString(1) + "!" + rsData.getString(2);

                clsBillItemDtlBean obj = new clsBillItemDtlBean();
                if (mapMultiSettleBills.containsKey(key))//billNo
                {
                    obj.setStrBillNo(rsData.getString(1));
                    obj.setDteBillDate(rsData.getString(2));
                    obj.setStrPosName(rsData.getString(3));
                    obj.setStrSettelmentMode(rsData.getString(4));
                    obj.setDblDiscountAmt(0.00);
                    obj.setDblTaxAmt(0.00);
                    obj.setDblSettlementAmt(rsData.getDouble(7));
                    obj.setDblSubTotal(0.00);
                    obj.setIntBillSeriesPaxNo(0);
                }
                else
                {
                    obj.setStrBillNo(rsData.getString(1));
                    obj.setDteBillDate(rsData.getString(2));
                    obj.setStrPosName(rsData.getString(3));
                    obj.setStrSettelmentMode(rsData.getString(4));
                    obj.setDblDiscountAmt(rsData.getDouble(5));
                    obj.setDblTaxAmt(rsData.getDouble(6));
                    obj.setDblSettlementAmt(rsData.getDouble(7));
                    obj.setDblSubTotal(rsData.getDouble(8));
                    obj.setIntBillSeriesPaxNo(rsData.getInt(10));
                }
                listOfBillData.add(obj);

                if (rsData.getString(9).equalsIgnoreCase("MultiSettle"))
                {
                    mapMultiSettleBills.put(key, rsData.getString(1));
                }
            }

            //Bill detail data
            sqlBuilder.setLength(0);
            sqlBuilder.append("SELECT a.strBillNo "
                    + ",DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y') AS BillDate, DATE_FORMAT(DATE(a.dteModifyVoidBill),'%d-%m-%Y') AS VoidedDate "
                    + ",TIME(a.dteBillDate) AS EntryTime, TIME(a.dteModifyVoidBill) VoidedTime, a.dblModifiedAmount AS BillAmount "
                    + ",a.strReasonName AS Reason,a.strUserEdited AS UserEdited,a.strUserCreated,a.strRemark "
                    + " from tblvoidbillhd a,tblvoidbilldtl b "
                    + " where a.strBillNo=b.strBillNo "
                    + " and b.strTransType='VB' "
                    + " and a.strTransType='VB' "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " and Date(a.dteModifyVoidBill)  Between '" + fromDate + "' and '" + toDate + "' ");
            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and a.strPosCode='" + posCode + "' ");
            }
            if (!shiftNo.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and a.intShiftCode='" + shiftNo + "'  ");
            }
            sqlBuilder.append(" group by a.dteBillDate,a.strBillNo ");

            ResultSet rsVoidData = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
            List<clsVoidBillDtl> listOfVoidBillData = new ArrayList<clsVoidBillDtl>();
            while (rsVoidData.next())
            {
                clsVoidBillDtl objVoidBill = new clsVoidBillDtl();
                objVoidBill.setStrBillNo(rsVoidData.getString(1));          //BillNo
                objVoidBill.setDteBillDate(rsVoidData.getString(2));        //Bill Date
                objVoidBill.setStrWaiterNo(rsVoidData.getString(3));        //Voided Date
                objVoidBill.setStrTableNo(rsVoidData.getString(4));         //Entry Time
                objVoidBill.setStrSettlementCode(rsVoidData.getString(5));  //Voided Time
                objVoidBill.setDblAmount(rsVoidData.getDouble(6));          //Bill Amount
                objVoidBill.setStrReasonName(rsVoidData.getString(7));      //Reason
                objVoidBill.setStrClientCode(rsVoidData.getString(8));      //User Edited
                objVoidBill.setStrUserCreated(rsVoidData.getString(9));     //User Created
                objVoidBill.setStrRemarks(rsVoidData.getString(10));         //Remarks   

                listOfVoidBillData.add(objVoidBill);
            }
            rsVoidData.close();

            Comparator<clsBillItemDtlBean> billDateComparator = new Comparator<clsBillItemDtlBean>()
            {

                @Override
                public int compare(clsBillItemDtlBean o1, clsBillItemDtlBean o2)
                {
                    return o2.getDteBillDate().compareToIgnoreCase(o1.getDteBillDate());
                }
            };

            Comparator<clsBillItemDtlBean> billNoComparator = new Comparator<clsBillItemDtlBean>()
            {

                @Override
                public int compare(clsBillItemDtlBean o1, clsBillItemDtlBean o2)
                {
                    return o1.getStrBillNo().compareToIgnoreCase(o2.getStrBillNo());
                }
            };

            Collections.sort(listOfBillData, new clsBillComparator(
                    billDateComparator, billNoComparator
            ));

            hm.put("listOfVoidBillData", listOfVoidBillData);
            //call for view report
            funViewJasperReportForBeanCollectionDataSource(is, hm, listOfBillData);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void funViewJasperReportForBeanCollectionDataSource(InputStream is, InputStream is2, HashMap hm, Collection listOfBillData, Collection listOfVoidBillData)
    {
        try
        {
            JasperPrint jp1 = JasperFillManager.fillReport(is, hm, new JRBeanCollectionDataSource(listOfBillData));
            JasperPrint jp2 = JasperFillManager.fillReport(is2, hm, new JRBeanCollectionDataSource(listOfVoidBillData));

            List pages = jp2.getPages();
            for (int j = 0; j < pages.size(); j++)
            {
                JRPrintPage object = (JRPrintPage) pages.get(j);
                jp1.addPage(object);
            }

            JRViewer viewer = new JRViewer(jp1);
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

    public void funItemWiseSalesReportForJasper()
    {
        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();

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

            String sqlQFile = "select a.strItemCode,a.strItemName,c.strPOSName"
                    + ",sum(a.dblQuantity),sum(a.dblTaxAmount)\n"
                    + ",sum(a.dblAmount),sum(a.dblAmount)-sum(a.dblDiscountAmt),sum(a.dblDiscountAmt),DATE_FORMAT(date(a.dteBillDate),'%d-%m-%Y'),'" + clsGlobalVarClass.gUserCode + "'\n"
                    + "from tblqbilldtl a,tblqbillhd b,tblposmaster c\n"
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
            sqlQFile = sqlQFile + " " + sqlFilters + "  GROUP BY a.strItemCode,a.strItemName ";

            sqlModLive = sqlModLive + " " + sqlFilters + "  GROUP BY a.strItemCode,a.strModifierName ";
            sqlModQFile = sqlModQFile + " " + sqlFilters + "  GROUP BY a.strItemCode,a.strModifierName ";

            ResultSet rsData = clsGlobalVarClass.dbMysql.executeResultSet(sqlLive.toString());
            List<clsBillItemDtlBean> listOfItemData = new ArrayList<clsBillItemDtlBean>();
            while (rsData.next())
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

                listOfItemData.add(obj);
            }

            rsData = clsGlobalVarClass.dbMysql.executeResultSet(sqlQFile.toString());
            while (rsData.next())
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

                listOfItemData.add(obj);
            }

            rsData = clsGlobalVarClass.dbMysql.executeResultSet(sqlModLive.toString());
            while (rsData.next())
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

                listOfItemData.add(obj);
            }

            rsData = clsGlobalVarClass.dbMysql.executeResultSet(sqlModQFile.toString());
            while (rsData.next())
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

                listOfItemData.add(obj);
            }

            Comparator<clsBillItemDtlBean> itemCodeComparator = new Comparator<clsBillItemDtlBean>()
            {

                @Override
                public int compare(clsBillItemDtlBean o1, clsBillItemDtlBean o2)
                {
                    return o1.getStrItemCode().substring(0, 7).compareToIgnoreCase(o2.getStrItemCode().substring(0, 7));
                }
            };
            Collections.sort(listOfItemData, itemCodeComparator);

            //call for view report
            funViewJasperReportForBeanCollectionDataSource(is, hm, listOfItemData);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public void funMenuHeadWiseSalesReportForJasper()
    {

        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();

            StringBuilder sbSqlLive = new StringBuilder();
            StringBuilder sbSqlQFile = new StringBuilder();
            StringBuilder sbSqlFilters = new StringBuilder();

            sbSqlLive.setLength(0);
            sbSqlQFile.setLength(0);
            sbSqlFilters.setLength(0);

            sbSqlQFile.append("SELECT  ifnull(d.strMenuCode,'ND'),ifnull(e.strMenuName,'ND'), sum(a.dblQuantity),\n"
                    + "sum(a.dblAmount)-sum(a.dblDiscountAmt),f.strPosName,'" + clsGlobalVarClass.gUserCode + "',sum(a.dblRate),sum(a.dblAmount) ,sum(a.dblDiscountAmt) "
                    + "FROM tblqbilldtl a\n"
                    + "left outer join tblqbillhd b on a.strBillNo=b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate) \n"
                    + "left outer join tblposmaster f on b.strposcode=f.strposcode "
                    + "left outer join tblmenuitempricingdtl d on a.strItemCode = d.strItemCode "
                    + " and b.strposcode =d.strposcode ");
            if (clsGlobalVarClass.gAreaWisePricing.equals("Y"))
            {
                sbSqlQFile.append("and b.strAreaCode= d.strAreaCode ");
            }
            sbSqlQFile.append("left outer join tblmenuhd e on d.strMenuCode= e.strMenuCode");
            sbSqlQFile.append(" where date( b.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
                    + " and a.strClientCode=b.strClientCode ");

            sbSqlLive.append("SELECT ifnull(d.strMenuCode,'ND'),ifnull(e.strMenuName,'ND'), sum(a.dblQuantity),\n"
                    + " sum(a.dblAmount)-sum(a.dblDiscountAmt),f.strPosName,'" + clsGlobalVarClass.gUserCode + "',sum(a.dblRate) ,sum(a.dblAmount),sum(a.dblDiscountAmt) "
                    + " FROM tblbilldtl a\n"
                    + " left outer join tblbillhd b on a.strBillNo=b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate) \n"
                    + " left outer join tblposmaster f on b.strposcode=f.strposcode "
                    + " left outer join tblmenuitempricingdtl d on a.strItemCode = d.strItemCode "
                    + " and b.strposcode =d.strposcode ");
            if (clsGlobalVarClass.gAreaWisePricing.equals("Y"))
            {
                sbSqlLive.append("and b.strAreaCode= d.strAreaCode ");
            }
            sbSqlLive.append("left outer join tblmenuhd e on d.strMenuCode= e.strMenuCode");
            sbSqlLive.append(" where date( b.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
                    + " and a.strClientCode=b.strClientCode ");

            String sqlModLive = "SELECT  ifnull(d.strMenuCode,'ND'),ifnull(e.strMenuName,'ND'), sum(a.dblQuantity),\n"
                    + "sum(a.dblAmount)-sum(a.dblDiscAmt),f.strPosName,'" + clsGlobalVarClass.gUserCode + "',sum(a.dblRate),sum(a.dblAmount),sum(a.dblDiscAmt) "
                    + "FROM tblbillmodifierdtl a\n"
                    + "left outer join tblbillhd b on a.strBillNo=b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate) \n"
                    + "left outer join tblposmaster f on b.strposcode=f.strposcode "
                    + "left outer join tblmenuitempricingdtl d on LEFT(a.strItemCode,7)= d.strItemCode "
                    + " and b.strposcode =d.strposcode ";

            if (clsGlobalVarClass.gAreaWisePricing.equals("Y"))
            {
                sqlModLive += "and b.strAreaCode= d.strAreaCode ";
            }
            sqlModLive += "left outer join tblmenuhd e on d.strMenuCode= e.strMenuCode";
            sqlModLive += " where date( b.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' and a.dblAmount>0 "
                    + " and a.strClientCode=b.strClientCode ";

            String sqlModQFile = "SELECT  ifnull(d.strMenuCode,'ND'),ifnull(e.strMenuName,'ND'), sum(a.dblQuantity),\n"
                    + "sum(a.dblAmount)-sum(a.dblDiscAmt),f.strPosName,'" + clsGlobalVarClass.gUserCode + "',sum(a.dblRate),sum(a.dblAmount),sum(a.dblDiscAmt) "
                    + "FROM tblqbillmodifierdtl a\n"
                    + "left outer join tblqbillhd b on a.strBillNo=b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate) \n"
                    + "left outer join tblposmaster f on b.strposcode=f.strposcode "
                    + "left outer join tblmenuitempricingdtl d on LEFT(a.strItemCode,7)= d.strItemCode "
                    + " and b.strposcode =d.strposcode ";

            if (clsGlobalVarClass.gAreaWisePricing.equals("Y"))
            {
                sqlModQFile += "and b.strAreaCode= d.strAreaCode ";
            }
            sqlModQFile += "left outer join tblmenuhd e on d.strMenuCode= e.strMenuCode";
            sqlModQFile += " where date( b.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' and a.dblAmount>0 "
                    + " and a.strClientCode=b.strClientCode ";

            if (!posCode.equals("All"))
            {
                sbSqlFilters.append(" AND b.strPOSCode = '" + posCode + "' ");
            }
            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sbSqlFilters.append(" AND b.intShiftCode = '" + shiftNo + "' ");
                }
            }
            sbSqlFilters.append(" Group by b.strPoscode, d.strMenuCode,e.strMenuName");

            sbSqlLive.append(sbSqlFilters);
            sbSqlQFile.append(sbSqlFilters);
            sqlModLive = sqlModLive + " " + sbSqlFilters.toString();
            sqlModQFile = sqlModQFile + " " + sbSqlFilters.toString();

            Map<String, clsGenericBean> mapMenuDtl = new HashMap<String, clsGenericBean>();

            //live data
            ResultSet rsData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
            funGetMenuHeadMap(mapMenuDtl, rsData);
            //live modifiers
            rsData = clsGlobalVarClass.dbMysql.executeResultSet(sqlModLive.toString());
            funGetMenuHeadMap(mapMenuDtl, rsData);
            //Q data
            rsData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFile.toString());
            funGetMenuHeadMap(mapMenuDtl, rsData);
            //live modifiers
            rsData = clsGlobalVarClass.dbMysql.executeResultSet(sqlModQFile.toString());
            funGetMenuHeadMap(mapMenuDtl, rsData);
            //convert to list
            Collection<clsGenericBean> listOfMenuHead = mapMenuDtl.values();

            //call for view report
            funViewJasperReportForBeanCollectionDataSource(is, hm, listOfMenuHead);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void funGetMenuHeadMap(Map<String, clsGenericBean> mapMenuDtl, ResultSet rsData)
    {
        try
        {
            while (rsData.next())
            {
                if (mapMenuDtl.containsKey(rsData.getString(1)))
                {
                    clsGenericBean obj = mapMenuDtl.get(rsData.getString(1));

                    obj.setDblQty(obj.getDblQty() + rsData.getDouble(3));
                    obj.setDblAmt(obj.getDblAmt() + rsData.getDouble(8));
                    obj.setDblSubTotal(obj.getDblSubTotal() + rsData.getDouble(4));
                    obj.setDblDiscAmt(obj.getDblDiscAmt() + rsData.getDouble(9));

                    mapMenuDtl.put(rsData.getString(1), obj);
                }
                else
                {
                    clsGenericBean obj = new clsGenericBean();

                    obj.setStrCode(rsData.getString(1));
                    obj.setStrName(rsData.getString(2));
                    obj.setDblQty(rsData.getDouble(3));
                    obj.setDblAmt(rsData.getDouble(8));
                    obj.setDblSubTotal(rsData.getDouble(4));
                    obj.setDblDiscAmt(rsData.getDouble(9));

                    mapMenuDtl.put(rsData.getString(1), obj);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void funComplimentarySettlementReportForJasper()
    {

        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String reportType = hm.get("reportType").toString();
            String reasonCode = hm.get("reasonCode").toString();
            //System.out.println(hm);

            Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
            StringBuilder sbSqlLive = new StringBuilder();
            StringBuilder sbSqlQBill = new StringBuilder();
            StringBuilder sqlLiveModifierBuilder = new StringBuilder();
            StringBuilder sqlQModifierBuilder = new StringBuilder();

            List<clsBillDtl> listOfCompliItemDtl = new ArrayList<>();

            sbSqlLive.setLength(0);
            sbSqlQBill.setLength(0);
            sqlLiveModifierBuilder.setLength(0);
            sqlQModifierBuilder.setLength(0);
            if (reportType.equalsIgnoreCase("Group Wise"))
            {
                sbSqlLive.setLength(0);
                sbSqlQBill.setLength(0);
                sqlLiveModifierBuilder.setLength(0);
                sqlQModifierBuilder.setLength(0);

                //live data
                sbSqlLive.append("SELECT e.strPosName,h.strGroupCode,h.strGroupName,b.strItemCode,b.strItemName,b.dblRate, SUM(b.dblQuantity) AS dblQnty, IFNULL((b.dblRate* SUM(b.dblQuantity)),0) AS dblAmount "
                        + "FROM tblbillhd a,tblbillcomplementrydtl b,tblposmaster e,tblitemmaster f,tblsubgrouphd g,tblgrouphd h "
                        + "WHERE a.strBillNo = b.strBillNo  "
                        + "AND DATE(a.dteBillDate) =date(b.dteBillDate)  "
                        + "AND a.strPOSCode=e.strPosCode  "
                        + "AND b.strItemCode=f.strItemCode  "
                        + "AND f.strSubGroupCode=g.strSubGroupCode  "
                        + "AND g.strGroupCode=h.strGroupCode  "
                );

                //live modifiers
                sqlLiveModifierBuilder.append(" select e.strPosName,h.strGroupCode,h.strGroupName,b.strItemCode,b.strModifierName,b.dblRate,sum(b.dblQuantity),ifnull((b.dblRate*sum(b.dblQuantity)),0) as dblAmount"
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
                sbSqlQBill.append("SELECT e.strPosName,h.strGroupCode,h.strGroupName,b.strItemCode,b.strItemName,b.dblRate, SUM(b.dblQuantity) AS dblQnty, IFNULL((b.dblRate* SUM(b.dblQuantity)),0) AS dblAmount "
                        + "FROM tblqbillhd a,tblqbillcomplementrydtl b,tblposmaster e,tblitemmaster f,tblsubgrouphd g,tblgrouphd h "
                        + "WHERE a.strBillNo = b.strBillNo  "
                        + "AND DATE(a.dteBillDate) =date(b.dteBillDate)  "
                        + "AND a.strPOSCode=e.strPosCode  "
                        + "AND b.strItemCode=f.strItemCode  "
                        + "AND f.strSubGroupCode=g.strSubGroupCode  "
                        + "AND g.strGroupCode=h.strGroupCode  ");

                //Q modifiers
                sqlQModifierBuilder.append("select e.strPosName,h.strGroupCode,h.strGroupName,b.strItemCode,b.strModifierName,b.dblRate,sum(b.dblQuantity),ifnull((b.dblRate*sum(b.dblQuantity)),0) as dblAmount"
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
                //live modi
//                ResultSet rsSqlMod = clsGlobalVarClass.dbMysql.executeResultSet(sqlLiveModifierBuilder.toString());
//                while (rsSqlMod.next())
//                {
//                    clsBillDtl objItemModiDtl = new clsBillDtl();
//
//                    objItemModiDtl.setStrPosName(rsSqlMod.getString(1));
//                    objItemModiDtl.setStrGroupCode(rsSqlMod.getString(2));
//                    objItemModiDtl.setStrGroupName(rsSqlMod.getString(3));
//                    objItemModiDtl.setStrItemCode(rsSqlMod.getString(4));
//                    objItemModiDtl.setStrItemName(rsSqlMod.getString(5));
//                    objItemModiDtl.setDblRate(rsSqlMod.getDouble(6));
//                    objItemModiDtl.setDblQuantity(rsSqlMod.getDouble(7));
//                    objItemModiDtl.setDblAmount(rsSqlMod.getDouble(8));
//
//                    listOfCompliItemDtl.add(objItemModiDtl);
//                }
//                rsSqlMod.close();

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
                //Q modi
//                rsSqlMod = clsGlobalVarClass.dbMysql.executeResultSet(sqlQModifierBuilder.toString());
//                while (rsSqlMod.next())
//                {
//                    clsBillDtl objItemModiDtl = new clsBillDtl();
//
//                    objItemModiDtl.setStrPosName(rsSqlMod.getString(1));
//                    objItemModiDtl.setStrGroupCode(rsSqlMod.getString(2));
//                    objItemModiDtl.setStrGroupName(rsSqlMod.getString(3));
//                    objItemModiDtl.setStrItemCode(rsSqlMod.getString(4));
//                    objItemModiDtl.setStrItemName(rsSqlMod.getString(5));
//                    objItemModiDtl.setDblRate(rsSqlMod.getDouble(6));
//                    objItemModiDtl.setDblQuantity(rsSqlMod.getDouble(7));
//                    objItemModiDtl.setDblAmount(rsSqlMod.getDouble(8));
//
//                    listOfCompliItemDtl.add(objItemModiDtl);
//                }
//                rsSqlMod.close();

                Comparator<clsBillDtl> groupNameComparator = new Comparator<clsBillDtl>()
                {

                    @Override
                    public int compare(clsBillDtl o1, clsBillDtl o2)
                    {
                        return o1.getStrGroupName().compareToIgnoreCase(o2.getStrGroupName());
                    }
                };
                Comparator<clsBillDtl> posNameComparator = new Comparator<clsBillDtl>()
                {

                    @Override
                    public int compare(clsBillDtl o1, clsBillDtl o2)
                    {
                        return o1.getStrPosName().compareToIgnoreCase(o2.getStrPosName());
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
                        posNameComparator, groupNameComparator, itemCodeComparator
                ));

                //call for view report
                funViewJasperReportForBeanCollectionDataSource(is, hm, listOfCompliItemDtl);
            }
            else if (reportType.equalsIgnoreCase("Detail"))
            {
                sbSqlLive.setLength(0);
                sbSqlQBill.setLength(0);
                sqlLiveModifierBuilder.setLength(0);
                sqlQModifierBuilder.setLength(0);

                //live data              
                sbSqlLive.append("SELECT IFNULL(a.strBillNo,''), DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y') AS dteBillDate, IFNULL(b.strItemName,'') "
                        + ",b.dblQuantity,b.dblRate,(b.dblQuantity*b.dblRate) AS dblAmount, IFNULL(f.strPosName,'') "
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
                sqlLiveModifierBuilder.append("select ifnull(a.strBillNo,''),DATE_FORMAT(date(a.dteBillDate),'%d-%m-%Y') as dteBillDate,b.strModifierName, b.dblQuantity, b.dblRate,(b.dblQuantity*b.dblRate) as dblAmount"
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
                        + ",b.dblQuantity,b.dblRate,(b.dblQuantity*b.dblRate) AS dblAmount, IFNULL(f.strPosName,'') "
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
                sqlQModifierBuilder.append("select ifnull(a.strBillNo,''),DATE_FORMAT(date(a.dteBillDate),'%d-%m-%Y') as dteBillDate,b.strModifierName, b.dblQuantity, b.dblRate,ifnull(b.dblQuantity*b.dblRate,0) as dblAmount,ifnull(f.strPosName,''),ifnull(g.strWShortName,'NA') as strWShortName,ifnull(e.strReasonName,'') as strReasonName, a.strRemarks,ifnull(i.strGroupName,'') as strGroupName,\n"
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
                //live modi
//                ResultSet rsSqlMod = clsGlobalVarClass.dbMysql.executeResultSet(sqlLiveModifierBuilder.toString());
//                while (rsSqlMod.next())
//                {
//                    clsBillDtl objItemModiDtl = new clsBillDtl();
//
//                    objItemModiDtl.setStrBillNo(rsSqlMod.getString(1));
//                    objItemModiDtl.setDteBillDate(rsSqlMod.getString(2));
//                    objItemModiDtl.setStrItemName(rsSqlMod.getString(3));
//                    objItemModiDtl.setDblQuantity(0);//itemQty
//                    objItemModiDtl.setDblModQuantity(rsSqlMod.getDouble(4));//modifierQty
//                    objItemModiDtl.setDblRate(rsSqlMod.getDouble(5));
//                    objItemModiDtl.setDblAmount(rsSqlMod.getDouble(6));
//                    objItemModiDtl.setStrPosName(rsSqlMod.getString(7));
//                    objItemModiDtl.setStrWShortName(rsSqlMod.getString(8));
//                    objItemModiDtl.setStrReasonName(rsSqlMod.getString(9));
//                    objItemModiDtl.setStrRemarks(rsSqlMod.getString(10));
//                    objItemModiDtl.setStrGroupName(rsSqlMod.getString(11));
//                    objItemModiDtl.setStrKOTNo(rsSqlMod.getString(12));
//                    objItemModiDtl.setStrPOSCode(rsSqlMod.getString(13));
//                    objItemModiDtl.setStrTableName(rsSqlMod.getString(14));
//                    objItemModiDtl.setStrItemCode(rsSqlMod.getString(15));
//
//                    listOfCompliItemDtl.add(objItemModiDtl);
//                }
//                rsSqlMod.close();

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
                //Q modi
//                rsSqlMod = clsGlobalVarClass.dbMysql.executeResultSet(sqlQModifierBuilder.toString());
//                while (rsSqlMod.next())
//                {
//                    clsBillDtl objItemModiDtl = new clsBillDtl();
//
//                    objItemModiDtl.setStrBillNo(rsSqlMod.getString(1));
//                    objItemModiDtl.setDteBillDate(rsSqlMod.getString(2));
//                    objItemModiDtl.setStrItemName(rsSqlMod.getString(3));
//                    objItemModiDtl.setDblQuantity(0);//itemQty
//                    objItemModiDtl.setDblModQuantity(rsSqlMod.getDouble(4));//modifierQty
//                    objItemModiDtl.setDblRate(rsSqlMod.getDouble(5));
//                    objItemModiDtl.setDblAmount(rsSqlMod.getDouble(6));
//                    objItemModiDtl.setStrPosName(rsSqlMod.getString(7));
//                    objItemModiDtl.setStrWShortName(rsSqlMod.getString(8));
//                    objItemModiDtl.setStrReasonName(rsSqlMod.getString(9));
//                    objItemModiDtl.setStrRemarks(rsSqlMod.getString(10));
//                    objItemModiDtl.setStrGroupName(rsSqlMod.getString(11));
//                    objItemModiDtl.setStrKOTNo(rsSqlMod.getString(12));
//                    objItemModiDtl.setStrPOSCode(rsSqlMod.getString(13));
//                    objItemModiDtl.setStrTableName(rsSqlMod.getString(14));
//                    objItemModiDtl.setStrItemCode(rsSqlMod.getString(15));
//
//                    listOfCompliItemDtl.add(objItemModiDtl);
//                }
//                rsSqlMod.close();

                Comparator<clsBillDtl> posNameComparator = new Comparator<clsBillDtl>()
                {

                    @Override
                    public int compare(clsBillDtl o1, clsBillDtl o2)
                    {
                        return o1.getStrPosName().compareToIgnoreCase(o2.getStrPosName());
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
                        posNameComparator, billNoComparator, kotNoComparator, itemCodeComparator
                ));

                //call for view report
                funViewJasperReportForBeanCollectionDataSource(is, hm, listOfCompliItemDtl);
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
                //live modi
//                ResultSet rsSqlMod = clsGlobalVarClass.dbMysql.executeResultSet(sqlLiveModifierBuilder.toString());
//                while (rsSqlMod.next())
//                {
//                    clsBillDtl objItemModiDtl = new clsBillDtl();
//
//                    objItemModiDtl.setStrBillNo(rsSqlMod.getString(1));
//                    objItemModiDtl.setDteBillDate(rsSqlMod.getString(2));
//                    objItemModiDtl.setDblAmount(rsSqlMod.getDouble(3));
//                    objItemModiDtl.setStrPosName(rsSqlMod.getString(4));
//                    objItemModiDtl.setStrWShortName(rsSqlMod.getString(5));
//                    objItemModiDtl.setStrReasonName(rsSqlMod.getString(6));
//                    objItemModiDtl.setStrRemarks(rsSqlMod.getString(7));
//
//                    listOfCompliItemDtl.add(objItemModiDtl);
//                }
//                rsSqlMod.close();

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
                //Q modi
//                rsSqlMod = clsGlobalVarClass.dbMysql.executeResultSet(sqlQModifierBuilder.toString());
//                while (rsSqlMod.next())
//                {
//                    clsBillDtl objItemModiDtl = new clsBillDtl();
//
//                    objItemModiDtl.setStrBillNo(rsSqlMod.getString(1));
//                    objItemModiDtl.setDteBillDate(rsSqlMod.getString(2));
//                    objItemModiDtl.setDblAmount(rsSqlMod.getDouble(3));
//                    objItemModiDtl.setStrPosName(rsSqlMod.getString(4));
//                    objItemModiDtl.setStrWShortName(rsSqlMod.getString(5));
//                    objItemModiDtl.setStrReasonName(rsSqlMod.getString(6));
//                    objItemModiDtl.setStrRemarks(rsSqlMod.getString(7));
//
//                    listOfCompliItemDtl.add(objItemModiDtl);
//                }
//                rsSqlMod.close();

                Comparator<clsBillDtl> posNameComparator = new Comparator<clsBillDtl>()
                {

                    @Override
                    public int compare(clsBillDtl o1, clsBillDtl o2)
                    {
                        return o1.getStrPosName().compareToIgnoreCase(o2.getStrPosName());
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
                        posNameComparator, billNoComparator
                ));

                //call for view report
                funViewJasperReportForBeanCollectionDataSource(is, hm, listOfCompliItemDtl);

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

    public void funGroupWiseReportForJasper()
    {
        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String subGroupCode = hm.get("subGroupCode").toString();

            StringBuilder sbSqlLive = new StringBuilder();
            StringBuilder sbSqlQFile = new StringBuilder();
            StringBuilder sbSqlFilters = new StringBuilder();
            mapPOSDtlForGroupSubGroup = new LinkedHashMap<>();
            List<clsGroupSubGroupItemBean> listOfGroupWiseSales = new ArrayList<clsGroupSubGroupItemBean>();
            List<clsGroupSubGroupWiseSales> listOfGroupWise = new ArrayList<clsGroupSubGroupWiseSales>();
            sbSqlLive.setLength(0);
            sbSqlQFile.setLength(0);
            sbSqlFilters.setLength(0);

            sbSqlQFile.append("SELECT c.strGroupCode,c.strGroupName,sum( b.dblQuantity)"
                    + ",sum( b.dblAmount)-sum(b.dblDiscountAmt) "
                    + ",f.strPosName, '" + clsGlobalVarClass.gUserCode + "',b.dblRate ,sum(b.dblAmount),sum(b.dblDiscountAmt),a.strPOSCode,"
                    + "sum( b.dblAmount)-sum(b.dblDiscountAmt)+sum(b.dblTaxAmount)  "
                    + "FROM tblqbillhd a,tblqbilldtl b,tblgrouphd c,tblsubgrouphd d"
                    + ",tblitemmaster e,tblposmaster f "
                    + "where a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " and a.strPOSCode=f.strPOSCode  "
                    + " and a.strClientCode=b.strClientCode "
                    + "and b.strItemCode=e.strItemCode "
                    + "and c.strGroupCode=d.strGroupCode and d.strSubGroupCode=e.strSubGroupCode ");

            sbSqlLive.append("SELECT c.strGroupCode,c.strGroupName,sum( b.dblQuantity)"
                    + ",sum( b.dblAmount)-sum(b.dblDiscountAmt) "
                    + ",f.strPosName, '" + clsGlobalVarClass.gUserCode + "',b.dblRate ,sum(b.dblAmount),sum(b.dblDiscountAmt),a.strPOSCode,"
                    + " sum( b.dblAmount)-sum(b.dblDiscountAmt)+sum(b.dblTaxAmount)  "
                    + "FROM tblbillhd a,tblbilldtl b,tblgrouphd c,tblsubgrouphd d"
                    + ",tblitemmaster e,tblposmaster f "
                    + "where a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " and a.strPOSCode=f.strPOSCode  "
                    + " and a.strClientCode=b.strClientCode   "
                    + "and b.strItemCode=e.strItemCode "
                    + "and c.strGroupCode=d.strGroupCode "
                    + " and d.strSubGroupCode=e.strSubGroupCode ");

            String sqlModLive = "select c.strGroupCode,c.strGroupName"
                    + ",sum(b.dblQuantity),sum(b.dblAmount)-sum(b.dblDiscAmt),f.strPOSName"
                    + ",'" + clsGlobalVarClass.gUserCode + "','0' ,sum(b.dblAmount),sum(b.dblDiscAmt),a.strPOSCode,"
                    + " sum(b.dblAmount)-sum(b.dblDiscAmt)  "
                    + " from tblbillmodifierdtl b,tblbillhd a,tblposmaster f,tblitemmaster d"
                    + ",tblsubgrouphd e,tblgrouphd c "
                    + " where a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " and a.strPOSCode=f.strPosCode  "
                    + " and a.strClientCode=b.strClientCode  "
                    + " and LEFT(b.strItemCode,7)=d.strItemCode "
                    + " and d.strSubGroupCode=e.strSubGroupCode "
                    + " and e.strGroupCode=c.strGroupCode "
                    + " and b.dblamount>0 ";

            String sqlModQFile = "select c.strGroupCode,c.strGroupName"
                    + ",sum(b.dblQuantity),sum(b.dblAmount)-sum(b.dblDiscAmt),f.strPOSName"
                    + ",'" + clsGlobalVarClass.gUserCode + "','0' ,sum(b.dblAmount),sum(b.dblDiscAmt),a.strPOSCode,"
                    + " sum(b.dblAmount)-sum(b.dblDiscAmt) "
                    + " from tblqbillmodifierdtl b,tblqbillhd a,tblposmaster f,tblitemmaster d"
                    + ",tblsubgrouphd e,tblgrouphd c "
                    + " where a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " and a.strPOSCode=f.strPosCode   "
                    + " and a.strClientCode=b.strClientCode   "
                    + " and LEFT(b.strItemCode,7)=d.strItemCode "
                    + " and d.strSubGroupCode=e.strSubGroupCode "
                    + " and e.strGroupCode=c.strGroupCode "
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

            if (!subGroupCode.equalsIgnoreCase("All"))
            {
                sbSqlFilters.append("AND d.strSubGroupCode='" + subGroupCode + "' ");
            }
            sbSqlFilters.append(" Group BY c.strGroupCode, c.strGroupName, a.strPoscode ");

            sbSqlLive.append(sbSqlFilters);
            sbSqlQFile.append(sbSqlFilters);

            sqlModLive += " " + sbSqlFilters;
            sqlModQFile += " " + sbSqlFilters;

            ResultSet rsGroupWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
            funGenerateGroupWiseSales(rsGroupWiseSales);
            rsGroupWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlModLive);
            funGenerateGroupWiseSales(rsGroupWiseSales);
            rsGroupWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFile.toString());
            funGenerateGroupWiseSales(rsGroupWiseSales);
            rsGroupWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlModQFile);
            funGenerateGroupWiseSales(rsGroupWiseSales);

            Iterator<Map.Entry<String, List<Map<String, clsGroupSubGroupWiseSales>>>> it = mapPOSDtlForGroupSubGroup.entrySet().iterator();
            while (it.hasNext())
            {
                Map.Entry<String, List<Map<String, clsGroupSubGroupWiseSales>>> entry = it.next();
                String posCode1 = entry.getKey();
                List<Map<String, clsGroupSubGroupWiseSales>> listOfGroup = entry.getValue();
                for (int i = 0; i < listOfGroup.size(); i++)
                {
                    clsGroupSubGroupWiseSales objGroupDtl = listOfGroup.get(i).entrySet().iterator().next().getValue();

                    Object[] arrObjRows =
                    {
                        objGroupDtl.getGroupName(), objGroupDtl.getPosName(), objGroupDtl.getQty(), objGroupDtl.getSalesAmt(), objGroupDtl.getSubTotal(), objGroupDtl.getDiscAmt()
                    };
                    listOfGroupWise.add(objGroupDtl);
                }
            }

            /*
             * ResultSet rsLiveData =
             * clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
             * while (rsLiveData.next()) { clsGroupSubGroupItemBean objBean =
             * new clsGroupSubGroupItemBean();
             * objBean.setStrItemCode(rsLiveData.getString(1)); //Group Code
             * objBean.setStrGroupName(rsLiveData.getString(2)); //Group Name
             * objBean.setDblQuantity(rsLiveData.getDouble(3)); //Qty
             * objBean.setDblSubTotal(rsLiveData.getDouble(4)); //sub total
             * objBean.setDblAmount(rsLiveData.getDouble(8)); //amt-disAmt
             * objBean.setDblDisAmt(rsLiveData.getDouble(9)); //dis amt
             * objBean.setStrPOSName(rsLiveData.getString(5)); //POS Name
             *
             * listOfGroupWiseSales.add(objBean); } rsLiveData.close();
             *
             * ResultSet rsLiveModData =
             * clsGlobalVarClass.dbMysql.executeResultSet(sqlModLive.toString());
             * while (rsLiveModData.next()) { clsGroupSubGroupItemBean objBean =
             * new clsGroupSubGroupItemBean();
             * objBean.setStrItemCode(rsLiveModData.getString(1)); //Group Code
             * objBean.setStrGroupName(rsLiveModData.getString(2)); //Group Name
             * objBean.setDblQuantity(rsLiveModData.getDouble(3)); //Qty
             * objBean.setDblSubTotal(rsLiveModData.getDouble(4)); //sub total
             * objBean.setDblAmount(rsLiveModData.getDouble(8)); //amt-disAmt
             * objBean.setDblDisAmt(rsLiveModData.getDouble(9)); //dis amt
             * objBean.setStrPOSName(rsLiveModData.getString(5)); //POS Name
             *
             * listOfGroupWiseSales.add(objBean); } rsLiveModData.close();
             *
             * ResultSet rsQfileData =
             * clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFile.toString());
             * while (rsQfileData.next()) { clsGroupSubGroupItemBean objBean =
             * new clsGroupSubGroupItemBean();
             * objBean.setStrItemCode(rsQfileData.getString(1)); //Group Code
             * objBean.setStrGroupName(rsQfileData.getString(2)); //Group Name
             * objBean.setDblQuantity(rsQfileData.getDouble(3)); //Qty
             * objBean.setDblSubTotal(rsQfileData.getDouble(4)); //sub total
             * objBean.setDblAmount(rsQfileData.getDouble(8)); //amt-disAmt
             * objBean.setDblDisAmt(rsQfileData.getDouble(9)); //dis amt
             * objBean.setStrPOSName(rsQfileData.getString(5)); //POS Name
             *
             * listOfGroupWiseSales.add(objBean); } rsQfileData.close();
             *
             * ResultSet rsQfileModData =
             * clsGlobalVarClass.dbMysql.executeResultSet(sqlModQFile.toString());
             * while (rsQfileModData.next()) { clsGroupSubGroupItemBean objBean
             * = new clsGroupSubGroupItemBean();
             * objBean.setStrItemCode(rsQfileModData.getString(1)); //Group Code
             * objBean.setStrGroupName(rsQfileModData.getString(2)); //Group
             * Name objBean.setDblQuantity(rsQfileModData.getDouble(3)); //Qty
             * objBean.setDblSubTotal(rsQfileModData.getDouble(4)); //sub total
             * objBean.setDblAmount(rsQfileModData.getDouble(8)); //amt-disAmt
             * objBean.setDblDisAmt(rsQfileModData.getDouble(9)); //dis amt
             * objBean.setStrPOSName(rsQfileModData.getString(5)); //POS Name
             *
             * listOfGroupWiseSales.add(objBean); } rsQfileModData.close();
             *
             * Comparator<clsGroupSubGroupItemBean> groupComparator = new
             * Comparator<clsGroupSubGroupItemBean>() {
             *
             * @Override public int compare(clsGroupSubGroupItemBean o1,
             * clsGroupSubGroupItemBean o2) { return
             * o1.getStrGroupName().compareToIgnoreCase(o2.getStrGroupName()); }
             * };
             *
             * Collections.sort(listOfGroupWiseSales, groupComparator);
             */
            //call for view report
            funViewJasperReportForBeanCollectionDataSource(is, hm, listOfGroupWise);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void funSubGroupWiseReportForJasper()
    {

        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();

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
            funViewJasperReportForBeanCollectionDataSource(is, hm, listOfGroupSubGroupWiseSales);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void funGroupSubGroupReportForJasper()
    {

        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String groupCode = hm.get("groupCode").toString();
            String subGroupCode = hm.get("subGroupCode").toString();

            StringBuilder sqlBuilder = new StringBuilder();
            List<clsGroupSubGroupItemBean> listOfGroupSubGroupWiseSales = new ArrayList<clsGroupSubGroupItemBean>();

            sqlBuilder.setLength(0);
            sqlBuilder.append("TRUNCATE tbltempsalesflash1");
            clsGlobalVarClass.dbMysql.execute(sqlBuilder.toString());
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
                clsGroupSubGroupItemBean obj = new clsGroupSubGroupItemBean();
                obj.setStrItemName(rs.getString(1));
                obj.setStrSubGroupName(rs.getString(2));
                obj.setStrGroupName(rs.getString(3));
                obj.setDblQuantity(rs.getDouble(4));
                obj.setDblAmount(rs.getDouble(5));
                obj.setStrItemCode(rs.getString(6));

                listOfGroupSubGroupWiseSales.add(obj);
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
                clsGroupSubGroupItemBean obj = new clsGroupSubGroupItemBean();
                obj.setStrItemName(rs.getString(1));
                obj.setStrSubGroupName(rs.getString(2));
                obj.setStrGroupName(rs.getString(3));
                obj.setDblQuantity(rs.getDouble(4));
                obj.setDblAmount(rs.getDouble(5));
                obj.setStrItemCode(rs.getString(6));

                listOfGroupSubGroupWiseSales.add(obj);
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
                clsGroupSubGroupItemBean obj = new clsGroupSubGroupItemBean();
                obj.setStrItemName(rs.getString(1));
                obj.setStrSubGroupName(rs.getString(2));
                obj.setStrGroupName(rs.getString(3));
                obj.setDblQuantity(rs.getDouble(4));
                obj.setDblAmount(rs.getDouble(5));
                obj.setStrItemCode(rs.getString(6));

                listOfGroupSubGroupWiseSales.add(obj);
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
                clsGroupSubGroupItemBean obj = new clsGroupSubGroupItemBean();
                obj.setStrItemName(rs.getString(1));
                obj.setStrSubGroupName(rs.getString(2));
                obj.setStrGroupName(rs.getString(3));
                obj.setDblQuantity(rs.getDouble(4));
                obj.setDblAmount(rs.getDouble(5));
                obj.setStrItemCode(rs.getString(6));

                listOfGroupSubGroupWiseSales.add(obj);
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

            Collections.sort(listOfGroupSubGroupWiseSales, new clsGroupSubGroupComparator(
                    groupComparator,
                    subGroupComparator,
                    codeComparator)
            );
            //call for view report
            funViewJasperReportForBeanCollectionDataSource(is, hm, listOfGroupSubGroupWiseSales);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void funSettlementWiseReportForJasper()
    {

        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();

            StringBuilder sbSqlLive = new StringBuilder();
            StringBuilder sbSqlQFile = new StringBuilder();
            StringBuilder sqlFilter = new StringBuilder();

            sbSqlLive.append("SELECT a.strPosCode,c.strSettelmentDesc,sum(b.dblSettlementAmt),d.strposname "
                    + "FROM tblbillhd a, tblbillsettlementdtl b, tblsettelmenthd c ,tblposmaster d "
                    + "Where a.strBillNo = b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " and a.strClientCode=b.strClientCode "
                    + "and a.strposcode=d.strposcode "
                    + "and b.strSettlementCode = c.strSettelmentCode "
                    + "");

            sbSqlQFile.append("SELECT a.strPosCode,c.strSettelmentDesc,sum(b.dblSettlementAmt),d.strposname "
                    + "FROM tblqbillhd a, tblqbillsettlementdtl b, tblsettelmenthd c ,tblposmaster d "
                    + "Where a.strBillNo = b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " and a.strClientCode=b.strClientCode "
                    + "and a.strposcode=d.strposcode "
                    + "and b.strSettlementCode = c.strSettelmentCode "
                    + "");

            sqlFilter.append("and date(a.dteBillDate ) BETWEEN  '" + fromDate + "' AND '" + toDate + "' ");
            if (!"All".equalsIgnoreCase(posCode))
            {
                sqlFilter.append("and  a.strPosCode='" + posCode + "' ");
            }
            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sqlFilter.append(" and a.intShiftCode = '" + shiftNo + "' ");
                }
            }

            sqlFilter.append("GROUP BY c.strSettelmentDesc, a.strPosCode");

            sbSqlLive.append(sqlFilter);
            sbSqlQFile.append(sqlFilter);

            ResultSet rsData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
            List<clsBillItemDtlBean> listOfSettlementData = new ArrayList<clsBillItemDtlBean>();
            while (rsData.next())
            {
                clsBillItemDtlBean obj = new clsBillItemDtlBean();

                obj.setStrSettelmentMode(rsData.getString(2));
                obj.setDblSettlementAmt(rsData.getDouble(3));
                obj.setStrPosName(rsData.getString(4));
                listOfSettlementData.add(obj);
            }

            rsData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFile.toString());
            while (rsData.next())
            {
                clsBillItemDtlBean obj = new clsBillItemDtlBean();
                obj.setStrSettelmentMode(rsData.getString(2));
                obj.setDblSettlementAmt(rsData.getDouble(3));
                obj.setStrPosName(rsData.getString(4));
                listOfSettlementData.add(obj);
            }

            //call for view report
            funViewJasperReportForBeanCollectionDataSource(is, hm, listOfSettlementData);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void funTaxWiseSalesReportForJasper()
    {

        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();

            StringBuilder sqlBuilder = new StringBuilder();
            StringBuilder sqlQBuilder = new StringBuilder();
            //live
            sqlBuilder.setLength(0);
            sqlBuilder.append("SELECT a.strBillNo,DATE_FORMAT(a.dteBillDate,'%d-%m-%Y') as dteBillDate, b.strTaxCode, c.strTaxDesc, a.strPOSCode, b.dblTaxableAmount, b.dblTaxAmount, a.dblGrandTotal,d.strposname\n"
                    + "FROM tblBillHd a\n"
                    + "INNER JOIN tblBillTaxDtl b ON a.strBillNo = b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate) \n"
                    + "INNER JOIN tblTaxHd c ON b.strTaxCode = c.strTaxCode\n"
                    + "LEFT OUTER\n"
                    + "JOIN tblposmaster d ON a.strposcode=d.strposcode\n"
                    + "WHERE DATE(a.dteBillDate) BETWEEN '" + fromDate + "' and  '" + toDate + "' ");
            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and a.strPOSCode='" + posCode + "' ");
            }
            if (!shiftNo.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and a.intShiftCode='" + shiftNo + "'  ");
            }

            ResultSet rsData = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
            List<clsTaxCalculationDtls> listOfTaxData = new ArrayList<clsTaxCalculationDtls>();
            while (rsData.next())
            {
                clsTaxCalculationDtls obj = new clsTaxCalculationDtls();
                obj.setStrBillNo(rsData.getString(1));
                obj.setDteBillDate(rsData.getString(2));
                obj.setTaxCode(rsData.getString(3));
                obj.setStrTaxDesc(rsData.getString(4));
                obj.setStrPOSCode(rsData.getString(5));
                obj.setTaxableAmount(rsData.getDouble(6));
                obj.setTaxAmount(rsData.getDouble(7));
                obj.setDblGrandTotal(rsData.getDouble(8));
                obj.setStrPOSName(rsData.getString(9));

                listOfTaxData.add(obj);
            }

            sqlQBuilder.setLength(0);
            sqlQBuilder.append("SELECT a.strBillNo,DATE_FORMAT(a.dteBillDate,'%d-%m-%Y') as dteBillDate, b.strTaxCode, c.strTaxDesc, a.strPOSCode, b.dblTaxableAmount, b.dblTaxAmount, a.dblGrandTotal,d.strposname\n"
                    + "FROM tblqBillHd a\n"
                    + "INNER JOIN tblqBillTaxDtl b ON a.strBillNo = b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate) \n"
                    + "INNER JOIN tblTaxHd c ON b.strTaxCode = c.strTaxCode\n"
                    + "LEFT OUTER\n"
                    + "JOIN tblposmaster d ON a.strposcode=d.strposcode\n"
                    + "WHERE DATE(a.dteBillDate) BETWEEN '" + fromDate + "' and  '" + toDate + "' ");
            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlQBuilder.append("and a.strPOSCode='" + posCode + "' ");
            }
            if (!shiftNo.equalsIgnoreCase("All"))
            {
                sqlQBuilder.append("and a.intShiftCode='" + shiftNo + "'  ");
            }

            rsData = clsGlobalVarClass.dbMysql.executeResultSet(sqlQBuilder.toString());
            while (rsData.next())
            {
                clsTaxCalculationDtls obj = new clsTaxCalculationDtls();
                obj.setStrBillNo(rsData.getString(1));
                obj.setDteBillDate(rsData.getString(2));
                obj.setTaxCode(rsData.getString(3));
                obj.setStrTaxDesc(rsData.getString(4));
                obj.setStrPOSCode(rsData.getString(5));
                obj.setTaxableAmount(rsData.getDouble(6));
                obj.setTaxAmount(rsData.getDouble(7));
                obj.setDblGrandTotal(rsData.getDouble(8));
                obj.setStrPOSName(rsData.getString(9));

                listOfTaxData.add(obj);
            }
            //call for view report
            funViewJasperReportForBeanCollectionDataSource(is, hm, listOfTaxData);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void funWaiterWiseItemReportForJasper()
    {

        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String waiterCode = hm.get("waiterCode").toString();

            StringBuilder sqlBuilder = new StringBuilder();
            List<clsBillDtl> listOfWaiterWiseItemSales = new ArrayList<>();

            //Q Data
            sqlBuilder.setLength(0);
            sqlBuilder.append("select b.strItemCode,b.strItemName,b.dblRate,sum(b.dblQuantity),sum(b.dblAmount),c.strWaiterNo,c.strWShortName "
                    + "from tblqbillhd a,tblqbilldtl b,tblwaitermaster c "
                    + "where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
                    + "and a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + "and a.strWaiterNo=c.strWaiterNo ");
            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and a.strPOSCode='" + posCode + "' ");
            }
            if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
            {
                sqlBuilder.append("and a.intShiftCode='" + shiftNo + "' ");
            }
            if (!waiterCode.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and c.strWaiterNo='" + waiterCode + "' ");
            }
            sqlBuilder.append("group by c.strWaiterNo,b.strItemCode "
                    + "order by c.strWaiterNo,b.strItemCode ");

            ResultSet rsWaiterWiseItemSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
            while (rsWaiterWiseItemSales.next())
            {
                clsBillDtl obj = new clsBillDtl();

                obj.setStrItemCode(rsWaiterWiseItemSales.getString(1));
                obj.setStrItemName(rsWaiterWiseItemSales.getString(2));
                obj.setDblRate(rsWaiterWiseItemSales.getDouble(3));
                obj.setDblQuantity(rsWaiterWiseItemSales.getDouble(4));
                obj.setDblAmount(rsWaiterWiseItemSales.getDouble(5));
                obj.setStrWaiterNo(rsWaiterWiseItemSales.getString(6));
                obj.setStrWShortName(rsWaiterWiseItemSales.getString(7));

                listOfWaiterWiseItemSales.add(obj);
            }
            rsWaiterWiseItemSales.close();

            //Live Data
            sqlBuilder.setLength(0);
            sqlBuilder.append("select b.strItemCode,b.strItemName,b.dblRate,sum(b.dblQuantity),sum(b.dblAmount),c.strWaiterNo,c.strWShortName "
                    + "from tblbillhd a,tblbilldtl b,tblwaitermaster c "
                    + "where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
                    + "and a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + "and a.strWaiterNo=c.strWaiterNo ");
            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and a.strPOSCode='" + posCode + "' ");
            }
            if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
            {
                sqlBuilder.append("and a.intShiftCode='" + shiftNo + "' ");
            }
            if (!waiterCode.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and c.strWaiterNo='" + waiterCode + "' ");
            }
            sqlBuilder.append("group by c.strWaiterNo,b.strItemCode "
                    + "order by c.strWaiterNo,b.strItemCode ");

            rsWaiterWiseItemSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
            while (rsWaiterWiseItemSales.next())
            {
                clsBillDtl obj = new clsBillDtl();

                obj.setStrItemCode(rsWaiterWiseItemSales.getString(1));
                obj.setStrItemName(rsWaiterWiseItemSales.getString(2));
                obj.setDblRate(rsWaiterWiseItemSales.getDouble(3));
                obj.setDblQuantity(rsWaiterWiseItemSales.getDouble(4));
                obj.setDblAmount(rsWaiterWiseItemSales.getDouble(5));
                obj.setStrWaiterNo(rsWaiterWiseItemSales.getString(6));
                obj.setStrWShortName(rsWaiterWiseItemSales.getString(7));

                listOfWaiterWiseItemSales.add(obj);
            }
            rsWaiterWiseItemSales.close();

            Comparator<clsBillDtl> waiterCodeComparator = new Comparator<clsBillDtl>()
            {

                @Override
                public int compare(clsBillDtl o1, clsBillDtl o2)
                {
                    return o1.getStrWaiterNo().compareTo(o2.getStrWaiterNo());
                }
            };

            Comparator<clsBillDtl> itemCodeCodeComparator = new Comparator<clsBillDtl>()
            {
                @Override
                public int compare(clsBillDtl o1, clsBillDtl o2)
                {
                    return o1.getStrItemCode().substring(0, 7).compareTo(o2.getStrItemCode().substring(0, 7));
                }
            };
            Collections.sort(listOfWaiterWiseItemSales, new clsWaiterWiseSalesComparator(waiterCodeComparator, itemCodeCodeComparator));
            //call for view report
            funViewJasperReportForBeanCollectionDataSource(is, hm, listOfWaiterWiseItemSales);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void funDeliveryboyIncentivesReportForJasper()
    {
        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String reportType = hm.get("reportType").toString();
            String dpName = hm.get("DPName").toString();
            String dpCode = hm.get("DPCode").toString();

            if (reportType.equalsIgnoreCase("Summary"))
            {
                StringBuilder sqlBuilder = new StringBuilder();
                List<clsBillDtl> listOfDelBoyIncentives = new ArrayList<>();

                //Q Data
                sqlBuilder.setLength(0);
                sqlBuilder.append("select e.strDPCode,e.strDPName,sum(c.dblSubTotal)dblSubTotal,sum(b.dblDBIncentives) dblDBIncentives "
                        + "from tblhomedelivery a,tblhomedeldtl b,tblqbillhd c "
                        + ",tblareawisedelboywisecharges d,tbldeliverypersonmaster e,tblqadvbookbillhd f "
                        + ",tblcustomermaster g "
                        + "where date(c.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
                        + "and a.strBillNo=b.strBillNo  "
                        + " and date(a.dteDate)=date(b.dteBillDate) "
                        + "and a.strBillNo=c.strBillNo"
                        + " and date(a.dteDate)=date(c.dteBillDate) "
                        + "and b.strDPCode= d.strDeliveryBoyCode "
                        + "and d.strDeliveryBoyCode=e.strDPCode "
                        + "and c.strAdvBookingNo=f.strAdvBookingNo "
                        + "and c.strCustomerCode=g.strCustomerCode "
                        + "and g.strBuldingCode=d.strCustAreaCode ");
                if (!posCode.equalsIgnoreCase("All"))
                {
                    sqlBuilder.append("and c.strPOSCode='" + posCode + "' ");
                }
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sqlBuilder.append("and c.intShiftCode='" + shiftNo + "' ");
                }
                if (!dpCode.equalsIgnoreCase("All"))
                {
                    sqlBuilder.append("and b.strDPCode='" + dpCode + "' ");
                }
                sqlBuilder.append("group by e.strDPCode "
                        + "order by e.strDPCode ");

                ResultSet rsWaiterWiseItemSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
                while (rsWaiterWiseItemSales.next())
                {
                    clsBillDtl obj = new clsBillDtl();

                    obj.setStrDelBoyCode(rsWaiterWiseItemSales.getString(1));
                    obj.setStrDelBoyName(rsWaiterWiseItemSales.getString(2));
                    obj.setDblAmount(rsWaiterWiseItemSales.getDouble(3));
                    obj.setDblIncentive(rsWaiterWiseItemSales.getDouble(4));

                    listOfDelBoyIncentives.add(obj);
                }
                rsWaiterWiseItemSales.close();

                //Live Data
                sqlBuilder.setLength(0);
                sqlBuilder.append("select e.strDPCode,e.strDPName,sum(c.dblSubTotal)dblSubTotal,sum(b.dblDBIncentives) dblDBIncentives "
                        + "from tblhomedelivery a,tblhomedeldtl b,tblbillhd c "
                        + ",tblareawisedelboywisecharges d,tbldeliverypersonmaster e,tbladvbookbillhd f "
                        + ",tblcustomermaster g "
                        + "where date(c.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
                        + "and a.strBillNo=b.strBillNo  "
                        + "and a.strBillNo=c.strBillNo "
                        + "and b.strDPCode= d.strDeliveryBoyCode "
                        + "and d.strDeliveryBoyCode=e.strDPCode "
                        + "and c.strAdvBookingNo=f.strAdvBookingNo "
                        + "and c.strCustomerCode=g.strCustomerCode "
                        + "and g.strBuldingCode=d.strCustAreaCode ");
                if (!posCode.equalsIgnoreCase("All"))
                {
                    sqlBuilder.append("and c.strPOSCode='" + posCode + "' ");
                }
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sqlBuilder.append("and c.intShiftCode='" + shiftNo + "' ");
                }
                if (!dpCode.equalsIgnoreCase("All"))
                {
                    sqlBuilder.append("and b.strDPCode='" + dpCode + "' ");
                }
                sqlBuilder.append("group by e.strDPCode "
                        + "order by e.strDPCode ");

                rsWaiterWiseItemSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
                while (rsWaiterWiseItemSales.next())
                {
                    clsBillDtl obj = new clsBillDtl();

                    obj.setStrDelBoyCode(rsWaiterWiseItemSales.getString(1));
                    obj.setStrDelBoyName(rsWaiterWiseItemSales.getString(2));
                    obj.setDblAmount(rsWaiterWiseItemSales.getDouble(3));
                    obj.setDblIncentive(rsWaiterWiseItemSales.getDouble(4));

                    listOfDelBoyIncentives.add(obj);
                }
                rsWaiterWiseItemSales.close();

                Comparator<clsBillDtl> delBoyCodeComparator = new Comparator<clsBillDtl>()
                {

                    @Override
                    public int compare(clsBillDtl o1, clsBillDtl o2)
                    {
                        return o1.getStrDelBoyCode().compareTo(o2.getStrDelBoyCode());
                    }
                };

                Collections.sort(listOfDelBoyIncentives, new clsWaiterWiseSalesComparator(delBoyCodeComparator));
                //call for view report
                funViewJasperReportForBeanCollectionDataSource(is, hm, listOfDelBoyIncentives);
            }
            else
            {
                StringBuilder sqlBuilder = new StringBuilder();
                List<clsBillDtl> listOfDelBoyIncentives = new ArrayList<>();

                //Q Data
                sqlBuilder.setLength(0);
                sqlBuilder.append("select e.strDPCode,e.strDPName,a.strBillNo,date(c.dteBillDate) as dteBillDate,TIME_FORMAT(time(c.dteBillDate),\"%r\") as tmeBillTime "
                        + ",ifnull(h.strBuildingName,'') as strArea,ifnull(date(c.dteSettleDate),'') as dteSettleDate "
                        + ",ifnull(TIME_FORMAT(time(c.dteSettleDate),\"%r\"),'') as tmeSettleTime,sum(b.dblDBIncentives)dblDBIncentives "
                        + "from tblhomedelivery a,tblhomedeldtl b,tblqbillhd c "
                        + ",tblareawisedelboywisecharges d,tbldeliverypersonmaster e "
                        + ",tblcustomermaster g "
                        + "left outer join tblbuildingmaster h on g.strBuldingCode=h.strBuildingCode "
                        + "where date(c.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
                        + "and a.strBillNo=b.strBillNo   "
                        + "and a.strBillNo=c.strBillNo  "
                        + "and b.strDPCode= d.strDeliveryBoyCode "
                        + "and d.strDeliveryBoyCode=e.strDPCode  "
                        + "and c.strCustomerCode=g.strCustomerCode "
                        + "and g.strBuldingCode=d.strCustAreaCode ");
                if (!posCode.equalsIgnoreCase("All"))
                {
                    sqlBuilder.append("and c.strPOSCode='" + posCode + "' ");
                }
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sqlBuilder.append("and c.intShiftCode='" + shiftNo + "' ");
                }
                if (!dpCode.equalsIgnoreCase("All"))
                {
                    sqlBuilder.append("and b.strDPCode='" + dpCode + "' ");
                }
                sqlBuilder.append("group by e.strDPCode,c.strBillNo "
                        + "order by e.strDPCode,c.strBillNo ");

                ResultSet rsWaiterWiseItemSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
                while (rsWaiterWiseItemSales.next())
                {
                    clsBillDtl obj = new clsBillDtl();

                    obj.setStrDelBoyCode(rsWaiterWiseItemSales.getString(1));
                    obj.setStrDelBoyName(rsWaiterWiseItemSales.getString(2));
                    obj.setStrBillNo(rsWaiterWiseItemSales.getString(3));

                    obj.setDteBillDate(rsWaiterWiseItemSales.getString(4));
                    obj.setTmeBillTime(rsWaiterWiseItemSales.getString(5));
                    obj.setStrArea(rsWaiterWiseItemSales.getString(6));

                    obj.setDteBillSettleDate(rsWaiterWiseItemSales.getString(7));
                    obj.setTmeBillTime(rsWaiterWiseItemSales.getString(8));
                    obj.setDblIncentive(rsWaiterWiseItemSales.getDouble(9));

                    listOfDelBoyIncentives.add(obj);
                }
                rsWaiterWiseItemSales.close();

                //Live Data
                sqlBuilder.setLength(0);
                sqlBuilder.append("select e.strDPCode,e.strDPName,a.strBillNo,date(c.dteBillDate) as dteBillDate,TIME_FORMAT(time(c.dteBillDate),\"%r\") as tmeBillTime "
                        + ",ifnull(h.strBuildingName,'') as strArea,ifnull(date(c.dteSettleDate),'') as dteSettleDate "
                        + ",ifnull(TIME_FORMAT(time(c.dteSettleDate),\"%r\"),'') as tmeSettleTime,sum(b.dblDBIncentives)dblDBIncentives "
                        + "from tblhomedelivery a,tblhomedeldtl b,tblbillhd c "
                        + ",tblareawisedelboywisecharges d,tbldeliverypersonmaster e "
                        + ",tblcustomermaster g "
                        + "left outer join tblbuildingmaster h on g.strBuldingCode=h.strBuildingCode "
                        + "where date(c.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
                        + "and a.strBillNo=b.strBillNo   "
                        + "and a.strBillNo=c.strBillNo  "
                        + "and b.strDPCode= d.strDeliveryBoyCode "
                        + "and d.strDeliveryBoyCode=e.strDPCode  "
                        + "and c.strCustomerCode=g.strCustomerCode "
                        + "and g.strBuldingCode=d.strCustAreaCode ");
                if (!posCode.equalsIgnoreCase("All"))
                {
                    sqlBuilder.append("and c.strPOSCode='" + posCode + "' ");
                }
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sqlBuilder.append("and c.intShiftCode='" + shiftNo + "' ");
                }
                if (!dpCode.equalsIgnoreCase("All"))
                {
                    sqlBuilder.append("and b.strDPCode='" + dpCode + "' ");
                }
                sqlBuilder.append("group by e.strDPCode,c.strBillNo "
                        + "order by e.strDPCode,c.strBillNo ");

                rsWaiterWiseItemSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
                while (rsWaiterWiseItemSales.next())
                {
                    clsBillDtl obj = new clsBillDtl();

                    obj.setStrDelBoyCode(rsWaiterWiseItemSales.getString(1));
                    obj.setStrDelBoyName(rsWaiterWiseItemSales.getString(2));
                    obj.setStrBillNo(rsWaiterWiseItemSales.getString(3));

                    obj.setDteBillDate(rsWaiterWiseItemSales.getString(4));
                    obj.setTmeBillTime(rsWaiterWiseItemSales.getString(5));
                    obj.setStrArea(rsWaiterWiseItemSales.getString(6));

                    obj.setDteBillSettleDate(rsWaiterWiseItemSales.getString(7));
                    obj.setTmeBillTime(rsWaiterWiseItemSales.getString(8));
                    obj.setDblIncentive(rsWaiterWiseItemSales.getDouble(9));

                    listOfDelBoyIncentives.add(obj);
                }
                rsWaiterWiseItemSales.close();

                Comparator<clsBillDtl> delBoyCodeComparator = new Comparator<clsBillDtl>()
                {

                    @Override
                    public int compare(clsBillDtl o1, clsBillDtl o2)
                    {
                        return o1.getStrDelBoyCode().compareTo(o2.getStrDelBoyCode());
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

                Collections.sort(listOfDelBoyIncentives, new clsWaiterWiseSalesComparator(delBoyCodeComparator, billNoComparator));
                //call for view report
                funViewJasperReportForBeanCollectionDataSource(is, hm, listOfDelBoyIncentives);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void funDailyCollectionReportForJasper()
    {

        try
        {

            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();

            Map mapMultiSettleBills = new HashMap();

            StringBuilder sqlBuilder = new StringBuilder();
            //live
            sqlBuilder.setLength(0);
            sqlBuilder.append("select a.strBillNo,DATE_FORMAT(date(a.dteBillDate),'%d-%m-%Y') as dteBillDate ,b.strPosName, "
                    + "ifnull(d.strSettelmentDesc,'') as strSettelmentMode,a.dblDiscountAmt,a.dblTaxAmt  "
                    + ",sum(c.dblSettlementAmt) as dblSettlementAmt,a.dblSubTotal,a.strSettelmentMode,intBillSeriesPaxNo "
                    + "from  tblbillhd a,tblposmaster b,tblbillsettlementdtl c,tblsettelmenthd d "
                    + "where date(a.dteBillDate) between '" + fromDate + "' and  '" + toDate + "' "
                    + "and a.strPOSCode=b.strPOSCode "
                    + "and a.strBillNo=c.strBillNo "
                    + "and c.strSettlementCode=d.strSettelmentCode "
                    + "and date(a.dteBillDate)=date(c.dteBillDate) "
                    + "and a.strClientCode=c.strClientCode ");
            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and a.strPOSCode='" + posCode + "' ");
            }
            if (!shiftNo.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and a.intShiftCode='" + shiftNo + "'  ");
            }
            sqlBuilder.append("GROUP BY a.strClientCode,date(a.dteBillDate),a.strBillNo,d.strSettelmentCode "
                    + "ORDER BY a.strBillNo ASC ");

            ResultSet rsData = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
            List<clsBillItemDtlBean> listOfBillData = new ArrayList<clsBillItemDtlBean>();
            while (rsData.next())
            {
                String key = rsData.getString(1) + "!" + rsData.getString(2);

                clsBillItemDtlBean obj = new clsBillItemDtlBean();
                if (mapMultiSettleBills.containsKey(key))//billNo
                {
                    obj.setStrBillNo(rsData.getString(1));
                    obj.setDteBillDate(rsData.getString(2));
                    obj.setStrPosName(rsData.getString(3));
                    obj.setStrSettelmentMode(rsData.getString(4));
                    obj.setDblDiscountAmt(0.00);
                    obj.setDblTaxAmt(0.00);
                    obj.setDblSettlementAmt(rsData.getDouble(7));
                    obj.setDblSubTotal(0.00);
                    obj.setIntBillSeriesPaxNo(0);
                }
                else
                {
                    obj.setStrBillNo(rsData.getString(1));
                    obj.setDteBillDate(rsData.getString(2));
                    obj.setStrPosName(rsData.getString(3));
                    obj.setStrSettelmentMode(rsData.getString(4));
                    obj.setDblDiscountAmt(rsData.getDouble(5));
                    obj.setDblTaxAmt(rsData.getDouble(6));
                    obj.setDblSettlementAmt(rsData.getDouble(7));
                    obj.setDblSubTotal(rsData.getDouble(8));
                    obj.setIntBillSeriesPaxNo(rsData.getInt(10));
                }
                listOfBillData.add(obj);

                if (rsData.getString(9).equalsIgnoreCase("MultiSettle"))
                {
                    mapMultiSettleBills.put(key, rsData.getString(1));
                }
            }

            //QFile
            sqlBuilder.setLength(0);
            sqlBuilder.append("select a.strBillNo,DATE_FORMAT(date(a.dteBillDate),'%d-%m-%Y') as dteBillDate ,b.strPosName, "
                    + "ifnull(d.strSettelmentDesc,'') as strSettelmentMode,a.dblDiscountAmt,a.dblTaxAmt   "
                    + ",sum(c.dblSettlementAmt) as dblSettlementAmt,a.dblSubTotal,a.strSettelmentMode,intBillSeriesPaxNo "
                    + "from  tblqbillhd a,tblposmaster b,tblqbillsettlementdtl c,tblsettelmenthd d "
                    + "where date(a.dteBillDate) between '" + fromDate + "' and  '" + toDate + "' "
                    + "and a.strPOSCode=b.strPOSCode "
                    + "and a.strBillNo=c.strBillNo "
                    + "and c.strSettlementCode=d.strSettelmentCode "
                    + "and date(a.dteBillDate)=date(c.dteBillDate) "
                    + "and a.strClientCode=c.strClientCode ");
            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and a.strPOSCode='" + posCode + "' ");
            }
            if (!shiftNo.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and a.intShiftCode='" + shiftNo + "'  ");
            }
            sqlBuilder.append("GROUP BY a.strClientCode,date(a.dteBillDate),a.strBillNo,d.strSettelmentCode "
                    + "ORDER BY a.strBillNo ASC ");

            rsData = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
            while (rsData.next())
            {
                String key = rsData.getString(1) + "!" + rsData.getString(2);

                clsBillItemDtlBean obj = new clsBillItemDtlBean();
                if (mapMultiSettleBills.containsKey(key))//billNo
                {
                    obj.setStrBillNo(rsData.getString(1));
                    obj.setDteBillDate(rsData.getString(2));
                    obj.setStrPosName(rsData.getString(3));
                    obj.setStrSettelmentMode(rsData.getString(4));
                    obj.setDblDiscountAmt(0.00);
                    obj.setDblTaxAmt(0.00);
                    obj.setDblSettlementAmt(rsData.getDouble(7));
                    obj.setDblSubTotal(0.00);
                    obj.setIntBillSeriesPaxNo(0);
                }
                else
                {
                    obj.setStrBillNo(rsData.getString(1));
                    obj.setDteBillDate(rsData.getString(2));
                    obj.setStrPosName(rsData.getString(3));
                    obj.setStrSettelmentMode(rsData.getString(4));
                    obj.setDblDiscountAmt(rsData.getDouble(5));
                    obj.setDblTaxAmt(rsData.getDouble(6));
                    obj.setDblSettlementAmt(rsData.getDouble(7));
                    obj.setDblSubTotal(rsData.getDouble(8));
                    obj.setIntBillSeriesPaxNo(rsData.getInt(10));
                }
                listOfBillData.add(obj);

                if (rsData.getString(9).equalsIgnoreCase("MultiSettle"))
                {
                    mapMultiSettleBills.put(key, rsData.getString(1));
                }
            }

            //Bill detail data
            sqlBuilder.setLength(0);
            sqlBuilder.append("SELECT a.strBillNo "
                    + ",DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y') AS BillDate, DATE_FORMAT(DATE(a.dteModifyVoidBill),'%d-%m-%Y') AS VoidedDate "
                    + ",TIME(a.dteBillDate) AS EntryTime, TIME(a.dteModifyVoidBill) VoidedTime, a.dblModifiedAmount AS BillAmount "
                    + ",a.strReasonName AS Reason,a.strUserEdited AS UserEdited,a.strUserCreated,a.strRemark "
                    + " from tblvoidbillhd a,tblvoidbilldtl b "
                    + " where a.strBillNo=b.strBillNo "
                    + " and b.strTransType='VB' "
                    + " and a.strTransType='VB' "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " and Date(a.dteModifyVoidBill)  Between '" + fromDate + "' and '" + toDate + "' ");
            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and a.strPosCode='" + posCode + "' ");
            }
            if (!shiftNo.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and a.intShiftCode='" + shiftNo + "'  ");
            }
            sqlBuilder.append(" group by a.dteBillDate,a.strBillNo ");

            ResultSet rsVoidData = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
            List<clsVoidBillDtl> listOfVoidBillData = new ArrayList<clsVoidBillDtl>();
            while (rsVoidData.next())
            {
                clsVoidBillDtl objVoidBill = new clsVoidBillDtl();
                objVoidBill.setStrBillNo(rsVoidData.getString(1));          //BillNo
                objVoidBill.setDteBillDate(rsVoidData.getString(2));        //Bill Date
                objVoidBill.setStrWaiterNo(rsVoidData.getString(3));        //Voided Date
                objVoidBill.setStrTableNo(rsVoidData.getString(4));         //Entry Time
                objVoidBill.setStrSettlementCode(rsVoidData.getString(5));  //Voided Time
                objVoidBill.setDblAmount(rsVoidData.getDouble(6));          //Bill Amount
                objVoidBill.setStrReasonName(rsVoidData.getString(7));      //Reason
                objVoidBill.setStrClientCode(rsVoidData.getString(8));      //User Edited
                objVoidBill.setStrUserCreated(rsVoidData.getString(9));     //User Created
                objVoidBill.setStrRemarks(rsVoidData.getString(10));         //Remarks   

                listOfVoidBillData.add(objVoidBill);
            }
            rsVoidData.close();

            Comparator<clsBillItemDtlBean> billDateComparator = new Comparator<clsBillItemDtlBean>()
            {

                @Override
                public int compare(clsBillItemDtlBean o1, clsBillItemDtlBean o2)
                {
                    return o2.getDteBillDate().compareToIgnoreCase(o1.getDteBillDate());
                }
            };

            Comparator<clsBillItemDtlBean> billNoComparator = new Comparator<clsBillItemDtlBean>()
            {

                @Override
                public int compare(clsBillItemDtlBean o1, clsBillItemDtlBean o2)
                {
                    return o1.getStrBillNo().compareToIgnoreCase(o2.getStrBillNo());
                }
            };

            Collections.sort(listOfBillData, new clsBillComparator(
                    billDateComparator, billNoComparator
            ));

            hm.put("listOfVoidBillData", listOfVoidBillData);
            //call for view report
            funViewJasperReportForBeanCollectionDataSource(is, hm, listOfBillData);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public void funDailySalesReportForJasper()
    {

        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();

            List<clsBillItemDtlBean> listOfDailySaleData = new ArrayList<clsBillItemDtlBean>();
            StringBuilder sbSqlBillWise = new StringBuilder();
            StringBuilder sbSqlBillWiseQFile = new StringBuilder();

            sbSqlBillWise.setLength(0);
            sbSqlBillWise.append("select a.strBillNo,left(a.dteBillDate,10),left(right(a.dteDateCreated,8),5) as BillTime"
                    + ",ifnull(b.strTableName,'') as TableName,f.strPOSName, ifnull(d.strSettelmentDesc,'') as payMode"
                    + ",ifnull(a.dblSubTotal,0.00),a.dblDiscountPer,a.dblDiscountAmt,a.dblTaxAmt"
                    + ",ifnull(c.dblSettlementAmt,0.00),a.strUserCreated,a.strUserEdited,a.dteDateCreated"
                    + ",a.dteDateEdited,a.strClientCode,a.strWaiterNo,a.strCustomerCode,a.dblDeliveryCharges"
                    + ",ifnull(c.strRemark,''),ifnull(e.strCustomerName ,'NA')"
                    + ",a.dblTipAmount,'" + clsGlobalVarClass.gUserCode + "',a.strDiscountRemark,'' "
                    + "from tblbillhd  a "
                    + "left outer join  tbltablemaster b on a.strTableNo=b.strTableNo "
                    + "left outer join tblposmaster f on a.strPOSCode=f.strPOSCode "
                    + "left outer join tblbillsettlementdtl c on a.strBillNo=c.strBillNo and date(a.dteBillDate)=date(c.dteBillDate) "
                    + "left outer join tblsettelmenthd d on c.strSettlementCode=d.strSettelmentCode "
                    + "left outer join tblcustomermaster e on a.strCustomerCode=e.strCustomerCode "
                    + "where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "'");

            if (!posCode.equals("All"))
            {
                sbSqlBillWise.append(" and a.strPOSCode='" + posCode + "' ");
            }
            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sbSqlBillWise.append(" AND a.intShiftCode = '" + shiftNo + "' ");
                }
            }

            sbSqlBillWise.append(" order by a.strBillNo desc");
            //System.out.println("Bill Wise Report Live Query="+sbSqlBillWise);

            sbSqlBillWiseQFile.setLength(0);
            sbSqlBillWiseQFile.append("select a.strBillNo,left(a.dteBillDate,10),left(right(a.dteDateCreated,8),5) as BillTime "
                    + ",ifnull(b.strTableName,'') as TableName,f.strPOSName, ifnull(d.strSettelmentDesc,'') as payMode "
                    + ",ifnull(a.dblSubTotal,0.00),a.dblDiscountPer,a.dblDiscountAmt,a.dblTaxAmt "
                    + ",ifnull(c.dblSettlementAmt,0.00),a.strUserCreated,a.strUserEdited,a.dteDateCreated "
                    + ",a.dteDateEdited,a.strClientCode,a.strWaiterNo,a.strCustomerCode,a.dblDeliveryCharges "
                    + ",ifnull(c.strRemark,''),ifnull(e.strCustomerName ,'NA')"
                    + ",a.dblTipAmount,'" + clsGlobalVarClass.gUserCode + "',a.strDiscountRemark,'' "
                    + "from tblqbillhd  a "
                    + "left outer join  tbltablemaster b on a.strTableNo=b.strTableNo "
                    + "left outer join tblposmaster f on a.strPOSCode=f.strPOSCode "
                    + "left outer join tblqbillsettlementdtl c on a.strBillNo=c.strBillNo and date(a.dteBillDate)=date(c.dteBillDate) "
                    + "left outer join tblsettelmenthd d on c.strSettlementCode=d.strSettelmentCode "
                    + "left outer join tblcustomermaster e on a.strCustomerCode=e.strCustomerCode "
                    + "where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "'");

            if (!posCode.equals("All"))
            {
                sbSqlBillWiseQFile.append(" and a.strPOSCode='" + posCode + "' ");
            }
            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sbSqlBillWiseQFile.append(" AND a.intShiftCode = '" + shiftNo + "' ");
                }
            }
            sbSqlBillWiseQFile.append(" order by a.strBillNo desc");

            ResultSet rsLiveData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlBillWise.toString());
            while (rsLiveData.next())
            {
                clsBillItemDtlBean obj = new clsBillItemDtlBean();
                obj.setStrBillNo(rsLiveData.getString(1));          //BillNo
                obj.setDteBillDate(rsLiveData.getString(2));        //Bill Date
                obj.setStrItemCode(rsLiveData.getString(4));        //Table Name    
                obj.setStrPosName(rsLiveData.getString(5));         //POS Name
                obj.setStrSettelmentMode(rsLiveData.getString(6));  //Settle Mode
                obj.setDblSubTotal(rsLiveData.getDouble(7));        //Sub Total
                obj.setDblDiscountPer(rsLiveData.getDouble(8));     //Disc Per
                obj.setDblDiscountAmt(rsLiveData.getDouble(9));     //Disc Amt
                obj.setDblTaxAmt(rsLiveData.getDouble(10));         //Tax Amt
                obj.setDblSettlementAmt(rsLiveData.getDouble(11));  //Settle Amt
                obj.setStrDiscType(rsLiveData.getString(12));       //User Created
                obj.setStrDiscValue(rsLiveData.getString(14));      //Date Created
                obj.setStrItemName(rsLiveData.getString(21));       //Customer Name
                obj.setDblAmount(rsLiveData.getDouble(19));         //Delivery Charges
                listOfDailySaleData.add(obj);
            }
            rsLiveData.close();

            ResultSet rsQFileData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlBillWiseQFile.toString());
            while (rsQFileData.next())
            {
                clsBillItemDtlBean obj = new clsBillItemDtlBean();
                obj.setStrBillNo(rsQFileData.getString(1));          //BillNo
                obj.setDteBillDate(rsQFileData.getString(2));        //Bill Date
                obj.setStrItemCode(rsQFileData.getString(4));        //Table Name    
                obj.setStrPosName(rsQFileData.getString(5));         //POS Name
                obj.setStrSettelmentMode(rsQFileData.getString(6));  //Settle Mode
                obj.setDblSubTotal(rsQFileData.getDouble(7));        //Sub Total
                obj.setDblDiscountPer(rsQFileData.getDouble(8));     //Disc Per
                obj.setDblDiscountAmt(rsQFileData.getDouble(9));     //Disc Amt
                obj.setDblTaxAmt(rsQFileData.getDouble(10));         //Tax Amt
                obj.setDblSettlementAmt(rsQFileData.getDouble(11));  //Settle Amt
                obj.setStrDiscType(rsQFileData.getString(12));       //User Created
                obj.setStrDiscValue(rsQFileData.getString(14));      //Date Created
                obj.setStrItemName(rsQFileData.getString(21));       //Customer Name
                obj.setDblAmount(rsQFileData.getDouble(19));         //Delivery Charges
                listOfDailySaleData.add(obj);
            }
            rsQFileData.close();
            //call for view report
            funViewJasperReportForBeanCollectionDataSource(is, hm, listOfDailySaleData);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void funSubGroupWiseSummaryReportForJasper()
    {

        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();

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
                    + " and a.strPOSCode=f.strPOSCode "
                    + "and b.strItemCode=d.strItemCode "
                    + "and c.strSubGroupCode=d.strSubGroupCode ");

            sbSqlLive.append("SELECT c.strSubGroupCode, c.strSubGroupName, sum( b.dblQuantity ) "
                    + ", sum( b.dblAmount )-sum(b.dblDiscountAmt), f.strPosName,'" + clsGlobalVarClass.gUserCode + "',b.dblRate ,sum(b.dblAmount),sum(b.dblDiscountAmt)"
                    + "from tblbillhd a,tblbilldtl b,tblsubgrouphd c,tblitemmaster d "
                    + ",tblposmaster f "
                    + "where a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " and a.strPOSCode=f.strPOSCode "
                    + "and b.strItemCode=d.strItemCode "
                    + "and c.strSubGroupCode=d.strSubGroupCode ");

            String sqlModLive = "select c.strSubGroupCode,c.strSubGroupName"
                    + ",sum(b.dblQuantity),sum(b.dblAmount),f.strPOSName"
                    + ",'" + clsGlobalVarClass.gUserCode + "','0' ,'0.00','0.00' "
                    + " from tblbillmodifierdtl b,tblbillhd a,tblposmaster f,tblitemmaster d"
                    + ",tblsubgrouphd c"
                    + " where a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " and a.strPOSCode=f.strPosCode "
                    + " and left(b.strItemCode,7)=d.strItemCode "
                    + " and d.strSubGroupCode=c.strSubGroupCode "
                    + "  ";

            String sqlModQFile = "select c.strSubGroupCode,c.strSubGroupName"
                    + ",sum(b.dblQuantity),sum(b.dblAmount),f.strPOSName"
                    + ",'" + clsGlobalVarClass.gUserCode + "','0' ,'0.00','0.00' "
                    + " from tblqbillmodifierdtl b,tblqbillhd a,tblposmaster f,tblitemmaster d"
                    + ",tblsubgrouphd c"
                    + " where a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " and a.strPOSCode=f.strPosCode "
                    + " and left(b.strItemCode,7)=d.strItemCode "
                    + " and d.strSubGroupCode=c.strSubGroupCode "
                    + "  ";

            sbSqlFilters.append(" and date( a.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' ");
            if (!posCode.equals("All"))
            {
                sbSqlFilters.append(" AND a.strPOSCode = '" + posCode + "' ");
            }
            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sbSqlFilters.append(" AND a.intShiftCode = '" + shiftNo + "' ");
                }
            }

            sbSqlFilters.append(" group by c.strSubGroupCode, c.strSubGroupName, a.strPoscode");

            sbSqlLive.append(sbSqlFilters);
            sbSqlQFile.append(sbSqlFilters);
            sqlModLive += " " + sbSqlFilters;
            sqlModQFile += " " + sbSqlFilters;

            clsSalesFlashReport obj = new clsSalesFlashReport();
            obj.funProcessSalesFlashReport(sbSqlLive.toString(), sbSqlQFile.toString(), "SubGroupWiseSales");

            String sqlInsertLiveBillSales = "insert into tbltempsalesflash "
                    + "(" + sqlModLive + ");";

            String sqlInsertQFileBillSales = "insert into tbltempsalesflash "
                    + "(" + sqlModQFile + ");";
            clsGlobalVarClass.dbMysql.execute(sqlInsertLiveBillSales);
            clsGlobalVarClass.dbMysql.execute(sqlInsertQFileBillSales);
            //call for view report
            funViewJasperReportForJDBCConnectionDataSource(is, hm, null);
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
            JRViewer viewer = new JRViewer(print);
            JFrame jf = new JFrame();
            jf.getContentPane().add(viewer);
            jf.validate();
            jf.setVisible(true);
            jf.setSize(new Dimension(850, 750));
            //jf.setLocation(300, 10);
            //jf.setLocationRelativeTo(this);

            //export to other format
            // funExportToOtherFormat(print);
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

    public void funRevenueHeadWiseReportForJasper()
    {

        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String reportType = hm.get("reportType").toString();
            String revenueHead = hm.get("revenueHead").toString();

            StringBuilder sbSqlLive = new StringBuilder();
            StringBuilder sbSqlQFile = new StringBuilder();
            StringBuilder sbSqlFilters = new StringBuilder();

            sbSqlLive.setLength(0);
            sbSqlQFile.setLength(0);
            sbSqlFilters.setLength(0);

            sbSqlQFile.append("select e.strRevenueHead,d.strMenuName,a.strItemName, SUM(a.dblQuantity), SUM(a.dblAmount),a.strItemCode "
                    + "from tblqbilldtl a,tblqbillhd b ,tblmenuitempricingdtl c,tblmenuhd d,tblitemmaster e "
                    + "where a.strBillNo=b.strBillNo  "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + "and a.strItemCode=c.strItemCode "
                    + "and c.strMenuCode=d.strMenuCode "
                    + "and a.strItemCode=e.strItemCode "
                    + "and c.strPosCode=if(c.strPosCode='All','All',b.strPOSCode) "
                    + "AND (b.strAreaCode=c.strAreaCode or c.strAreaCode='A001') ");

            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sbSqlQFile.append(" and b.intShiftCode = '" + shiftNo + "' ");
                }
            }

            String sqlModQFile = "SELECT e.strRevenueHead,d.strMenuName,a.strModifierName, SUM(a.dblQuantity), SUM(a.dblAmount),a.strItemCode "
                    + "FROM tblqbillmodifierdtl a,tblqbillhd b,tblmenuitempricingdtl c,tblmenuhd d,tblitemmaster e "
                    + "WHERE a.strBillNo=b.strBillNo  "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + "AND left(a.strItemCode,7)=c.strItemCode  "
                    + "AND c.strMenuCode=d.strMenuCode  "
                    + "AND left(a.strItemCode,7)=e.strItemCode  "
                    + "AND c.strPosCode= IF(c.strPosCode='All','All',b.strPOSCode)  "
                    + "AND (b.strAreaCode=c.strAreaCode or c.strAreaCode='A001')  "
                    + "AND DATE(b.dteBillDate)  BETWEEN '" + fromDate + "' AND '" + toDate + "' ";

            if (!posCode.equals("All"))
            {
                sqlModQFile += " and b.strPosCode='" + posCode + "'  ";
            }
            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sqlModQFile += " and b.intShiftCode = '" + shiftNo + "' ";
                }
            }

            if (!revenueHead.equalsIgnoreCase("All"))
            {
                sqlModQFile += " and e.strRevenueHead='" + revenueHead + "' ";
            }

            if (reportType.equalsIgnoreCase("Summary"))
            {
                sqlModQFile += "GROUP BY e.strRevenueHead,d.strMenuCode "
                        + "ORDER BY e.strRevenueHead,d.strMenuCode ";
            }
            else
            {
                sqlModQFile += "GROUP BY e.strRevenueHead,d.strMenuCode,a.strModifierName "
                        + "ORDER BY e.strRevenueHead,d.strMenuCode,a.strModifierName ";
            }

            sbSqlLive.append("select e.strRevenueHead,d.strMenuName,a.strItemName, SUM(a.dblQuantity), SUM(a.dblAmount),a.strItemCode "
                    + "from tblbilldtl a,tblbillhd b ,tblmenuitempricingdtl c,tblmenuhd d,tblitemmaster e "
                    + "where a.strBillNo=b.strBillNo  "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + "and a.strItemCode=c.strItemCode "
                    + "and c.strMenuCode=d.strMenuCode "
                    + "and a.strItemCode=e.strItemCode "
                    + "and c.strPosCode=if(c.strPosCode='All','All',b.strPOSCode) "
                    + "AND (b.strAreaCode=c.strAreaCode or c.strAreaCode='A001') ");

            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sbSqlLive.append(" and b.intShiftCode = '" + shiftNo + "' ");
                }
            }

            String sqlModLive = "SELECT e.strRevenueHead,d.strMenuName,a.strModifierName, SUM(a.dblQuantity), SUM(a.dblAmount),a.strItemCode "
                    + "FROM tblbillmodifierdtl a,tblbillhd b,tblmenuitempricingdtl c,tblmenuhd d,tblitemmaster e "
                    + "WHERE a.strBillNo=b.strBillNo  "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + "AND left(a.strItemCode,7)=c.strItemCode  "
                    + "AND c.strMenuCode=d.strMenuCode  "
                    + "AND left(a.strItemCode,7)=e.strItemCode  "
                    + "AND c.strPosCode= IF(c.strPosCode='All','All',b.strPOSCode)  "
                    + "AND (b.strAreaCode=c.strAreaCode or c.strAreaCode='A001')  "
                    + "AND DATE(b.dteBillDate)  BETWEEN '" + fromDate + "' AND '" + toDate + "' ";
            if (!posCode.equals("All"))
            {
                sqlModLive += " and b.strPosCode='" + posCode + "'  ";
            }
            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sqlModLive += " and b.intShiftCode = '" + shiftNo + "' ";
                }
            }

            if (!revenueHead.equalsIgnoreCase("All"))
            {
                sqlModLive += " and e.strRevenueHead='" + revenueHead + "' ";
            }

            if (reportType.equalsIgnoreCase("Summary"))
            {
                sqlModLive += "GROUP BY e.strRevenueHead,d.strMenuCode "
                        + "ORDER BY e.strRevenueHead,d.strMenuCode ";
            }
            else
            {
                sqlModLive += "GROUP BY e.strRevenueHead,d.strMenuCode,a.strModifierName "
                        + "ORDER BY e.strRevenueHead,d.strMenuCode,a.strModifierName ";
            }

            sbSqlFilters.append(" AND date(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' ");
            if (!posCode.equals("All"))
            {
                sbSqlFilters.append(" and b.strPosCode='" + posCode + "'  ");
            }

            if (!revenueHead.equalsIgnoreCase("All"))
            {
                sbSqlFilters.append(" and e.strRevenueHead='" + revenueHead + "' ");
            }

            if (reportType.equalsIgnoreCase("Summary"))
            {
                sbSqlFilters.append(" group by e.strRevenueHead,d.strMenuCode "
                        + " order by e.strRevenueHead,d.strMenuCode ");
            }
            else
            {
                sbSqlFilters.append(" group by e.strRevenueHead,d.strMenuCode,a.strItemName"
                        + " order by e.strRevenueHead,d.strMenuCode,a.strItemName");
            }
            sbSqlLive.append(sbSqlFilters);
            sbSqlQFile.append(sbSqlFilters);

            ResultSet rsData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
            List<clsRevenueBean> listOfRevenueData = new ArrayList<clsRevenueBean>();
            while (rsData.next())
            {
                clsRevenueBean obj = new clsRevenueBean();
                obj.setStrRevenueHead(rsData.getString(1));
                obj.setStrMenuName(rsData.getString(2));
                obj.setStrItemName(rsData.getString(3));
                obj.setDblQuantity(rsData.getDouble(4));
                obj.setDblAmount(rsData.getDouble(5));
                obj.setStrItemCode(rsData.getString(6));
                listOfRevenueData.add(obj);
            }

            rsData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFile.toString());
            while (rsData.next())
            {
                clsRevenueBean obj = new clsRevenueBean();
                obj.setStrRevenueHead(rsData.getString(1));
                obj.setStrMenuName(rsData.getString(2));
                obj.setStrItemName(rsData.getString(3));
                obj.setDblQuantity(rsData.getDouble(4));
                obj.setDblAmount(rsData.getDouble(5));
                obj.setStrItemCode(rsData.getString(6));
                listOfRevenueData.add(obj);
            }

            rsData = clsGlobalVarClass.dbMysql.executeResultSet(sqlModLive.toString());
            while (rsData.next())
            {
                clsRevenueBean obj = new clsRevenueBean();
                obj.setStrRevenueHead(rsData.getString(1));
                obj.setStrMenuName(rsData.getString(2));
                obj.setStrItemName(rsData.getString(3));
                obj.setDblQuantity(rsData.getDouble(4));
                obj.setDblAmount(rsData.getDouble(5));
                obj.setStrItemCode(rsData.getString(6));
                listOfRevenueData.add(obj);
            }

            rsData = clsGlobalVarClass.dbMysql.executeResultSet(sqlModQFile.toString());
            while (rsData.next())
            {
                clsRevenueBean obj = new clsRevenueBean();
                obj.setStrRevenueHead(rsData.getString(1));
                obj.setStrMenuName(rsData.getString(2));
                obj.setStrItemName(rsData.getString(3));
                obj.setDblQuantity(rsData.getDouble(4));
                obj.setDblAmount(rsData.getDouble(5));
                obj.setStrItemCode(rsData.getString(6));
                listOfRevenueData.add(obj);
            }

            Comparator<clsRevenueBean> revenueHeadNameComparator = new Comparator<clsRevenueBean>()
            {

                @Override
                public int compare(clsRevenueBean o1, clsRevenueBean o2)
                {
                    return o1.getStrRevenueHead().compareTo(o2.getStrRevenueHead());
                }
            };
            Comparator<clsRevenueBean> menuHeadNameComparator = new Comparator<clsRevenueBean>()
            {

                @Override
                public int compare(clsRevenueBean o1, clsRevenueBean o2)
                {
                    return o1.getStrMenuName().compareTo(o2.getStrMenuName());
                }
            };
            Comparator<clsRevenueBean> itemCodeComparator = new Comparator<clsRevenueBean>()
            {

                @Override
                public int compare(clsRevenueBean o1, clsRevenueBean o2)
                {
                    return o1.getStrItemCode().substring(0, 7).compareTo(o2.getStrItemCode().substring(0, 7));
                }
            };
            Collections.sort(listOfRevenueData, new clsRevenueHeadComparator(
                    revenueHeadNameComparator, menuHeadNameComparator, itemCodeComparator));

            //call for view report
            funViewJasperReportForBeanCollectionDataSource(is, hm, listOfRevenueData);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void funItemWiseConsumptionReportForJasper()
    {
        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();

            String groupCode = hm.get("GroupCode").toString();
            String groupName = hm.get("GroupName").toString();
            String printZeroAmountModi = hm.get("PrintZeroAmountModi").toString();

            int sqlNo = 0;
            StringBuilder sbSql = new StringBuilder();
            StringBuilder sbSqlMod = new StringBuilder();
            StringBuilder sbFilters = new StringBuilder();
            ResultSet rsSalesMod;
            Map<String, clsItemWiseConsumption> hmItemWiseConsumption = new HashMap<String, clsItemWiseConsumption>();

            // Code for Sales Qty for bill detail and bill modifier live & q data
            // for Sales Qty for bill detail live data  
            sbSql.setLength(0);
            /*
             * sbSql.append("select
             * b.stritemcode,b.stritemname,sum(b.dblQuantity),sum(b.dblamount),b.dblRate"
             * + "
             * ,e.strposname,b.dblDiscountAmt,g.strSubGroupName,h.strGroupName,a.strBillNo,b.dblTaxAmount
             * " + " from tblbillhd a,tblbilldtl b, tblbillsettlementdtl
             * c,tblsettelmenthd d,tblposmaster e" + " ,tblitemmaster
             * f,tblsubgrouphd g,tblgrouphd h " + " where
             * a.strBillNo=b.strBillNo and a.strBillNo=c.strBillNo and
             * c.strSettlementCode=d.strSettelmentCode " + " and
             * a.strPOSCode=e.strPosCode and b.strItemCode=f.strItemCode and
             * f.strSubGroupCode=g.strSubGroupCode " + " and
             * g.strGroupCode=h.strGroupCode and
             * d.strSettelmentType!='Complementary' " + " and
             * date(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate +
             * "' ");
             */
            sbSql.append("select b.stritemcode,b.stritemname,sum(b.dblQuantity),sum(b.dblamount),b.dblRate , "
                    + " e.strposname,b.dblDiscountAmt,g.strSubGroupName,h.strGroupName,a.strBillNo,f.strExternalCode "
                    + " from tblbillhd a,tblbilldtl b, tblposmaster e ,tblitemmaster f,tblsubgrouphd g,tblgrouphd h "
                    + " where a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " and a.strPOSCode=e.strPosCode "
                    + " and b.strItemCode=f.strItemCode "
                    + " and f.strSubGroupCode=g.strSubGroupCode  "
                    + " and g.strGroupCode=h.strGroupCode    "
                    + " and a.strBillNo NOT IN (select f.strBillNo from tblbillcomplementrydtl f ) "
                    + " and date(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' ");

            if (!posCode.equals("All"))
            {
                sbSql.append(" and a.strPOSCode = '" + posCode + "' ");
            }

            if (!groupCode.equalsIgnoreCase("All"))
            {
                sbSql.append(" and h.strGroupCode = '" + groupCode + "' ");
            }

            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sbSql.append(" and a.intShiftCode = '" + shiftNo + "' ");
                }
            }
            sbSql.append(" group by b.strItemCode order by a.strPOSCode,b.strItemName");
//            System.out.println(sbSql);

            ResultSet rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
            while (rsSales.next())
            {
                clsItemWiseConsumption objItemWiseConsumption = null;
                if (null != hmItemWiseConsumption.get(rsSales.getString(1) + "!" + rsSales.getString(2)))
                {
                    objItemWiseConsumption = hmItemWiseConsumption.get(rsSales.getString(1) + "!" + rsSales.getString(2));
                    objItemWiseConsumption.setSaleQty(objItemWiseConsumption.getSaleQty() + rsSales.getDouble(3));
                    objItemWiseConsumption.setSaleAmt(objItemWiseConsumption.getSaleAmt() + (rsSales.getDouble(4) - rsSales.getDouble(7)));
                    objItemWiseConsumption.setSubTotal(objItemWiseConsumption.getSubTotal() + rsSales.getDouble(4));
                    //objItemWiseConsumption.setTotalQty(objItemWiseConsumption.getTotalQty() + rsSales.getDouble(3));
                }
                else
                {
                    sqlNo++;
                    objItemWiseConsumption = new clsItemWiseConsumption();
                    objItemWiseConsumption.setItemCode(rsSales.getString(1));
                    objItemWiseConsumption.setItemName(rsSales.getString(2));
                    objItemWiseConsumption.setSubGroupName(rsSales.getString(8));
                    objItemWiseConsumption.setGroupName(rsSales.getString(9));
                    objItemWiseConsumption.setSaleQty(rsSales.getDouble(3));
                    objItemWiseConsumption.setComplimentaryQty(0);
                    objItemWiseConsumption.setNcQty(0);
                    objItemWiseConsumption.setSubTotal(rsSales.getDouble(4));
                    objItemWiseConsumption.setDiscAmt(rsSales.getDouble(7));
                    objItemWiseConsumption.setSaleAmt(rsSales.getDouble(4) - rsSales.getDouble(7));
                    objItemWiseConsumption.setPOSName(rsSales.getString(6));
                    objItemWiseConsumption.setPromoQty(0);
                    objItemWiseConsumption.setSeqNo(sqlNo);
                    objItemWiseConsumption.setExternalCode(rsSales.getString(11));
                    double totalRowQty = rsSales.getDouble(3) + 0 + 0 + 0;
                    //objItemWiseConsumption.setTotalQty(totalRowQty);
                    objItemWiseConsumption.setTotalQty(0);

                }
                if (null != objItemWiseConsumption)
                {
                    hmItemWiseConsumption.put(rsSales.getString(1) + "!" + rsSales.getString(2), objItemWiseConsumption);
                }
                sbSqlMod.setLength(0);
                if (printZeroAmountModi.equalsIgnoreCase("Yes"))
                {
                    //for Sales Qty for bill modifier live data 

                    sbSqlMod.append("select b.strItemCode,b.strModifierName,sum(b.dblQuantity),sum(b.dblamount),b.dblRate"
                            + " ,e.strposname,b.dblDiscAmt,g.strSubGroupName,h.strGroupName,a.strBillNo,f.strExternalCode "
                            + " from tblbillhd a,tblbillmodifierdtl b, tblbillsettlementdtl c,tblsettelmenthd d,tblposmaster e"
                            + " ,tblitemmaster f,tblsubgrouphd g,tblgrouphd h "
                            + " where a.strBillNo=b.strBillNo "
                            + " and date(a.dteBillDate)=date(b.dteBillDate) "
                            + " and a.strBillNo=c.strBillNo "
                            + " and date(a.dteBillDate)=date(c.dteBillDate) "
                            + " and c.strSettlementCode=d.strSettelmentCode "
                            + " and a.strPOSCode=e.strPosCode "
                            + " and left(b.strItemCode,7)=f.strItemCode "
                            + " and f.strSubGroupCode=g.strSubGroupCode "
                            + " and g.strGroupCode=h.strGroupCode "
                            + " and d.strSettelmentType!='Complementary' "
                            + " and left(b.strItemCode,7)='" + rsSales.getString(1) + "' and a.strBillNo='" + rsSales.getString(10) + "' "
                            + " and date(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
                            + " group by b.strItemCode,b.strModifierName ");
                }
                else
                {
                    sbSqlMod.append("select b.strItemCode,b.strModifierName,sum(b.dblQuantity),sum(b.dblamount),b.dblRate"
                            + " ,e.strposname,b.dblDiscAmt,g.strSubGroupName,h.strGroupName,a.strBillNo,f.strExternalCode "
                            + " from tblbillhd a,tblbillmodifierdtl b, tblbillsettlementdtl c,tblsettelmenthd d,tblposmaster e"
                            + " ,tblitemmaster f,tblsubgrouphd g,tblgrouphd h "
                            + " where a.strBillNo=b.strBillNo "
                            + " and date(a.dteBillDate)=date(b.dteBillDate) "
                            + " and a.strBillNo=c.strBillNo "
                            + " and date(a.dteBillDate)=date(c.dteBillDate) "
                            + " and c.strSettlementCode=d.strSettelmentCode "
                            + " and a.strPOSCode=e.strPosCode "
                            + " and left(b.strItemCode,7)=f.strItemCode "
                            + " and f.strSubGroupCode=g.strSubGroupCode "
                            + " and g.strGroupCode=h.strGroupCode "
                            + " and d.strSettelmentType!='Complementary' "
                            + " and left(b.strItemCode,7)='" + rsSales.getString(1) + "' and a.strBillNo='" + rsSales.getString(10) + "' "
                            + " and date(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' AND b.dblamount>0"
                            + " group by b.strItemCode,b.strModifierName ");
                }
                //System.out.println(sbSqlMod);
                rsSalesMod = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlMod.toString());
                while (rsSalesMod.next())
                {
                    // clsItemWiseConsumption objItemWiseConsumption=null;
                    if (null != hmItemWiseConsumption.get(rsSalesMod.getString(1) + "!" + rsSalesMod.getString(2)))
                    {
                        objItemWiseConsumption = hmItemWiseConsumption.get(rsSalesMod.getString(1) + "!" + rsSalesMod.getString(2));
                        objItemWiseConsumption.setSaleQty(objItemWiseConsumption.getSaleQty() + rsSalesMod.getDouble(3));
                        objItemWiseConsumption.setSaleAmt(objItemWiseConsumption.getSaleAmt() + ((rsSalesMod.getDouble(4)) - rsSalesMod.getDouble(7)));
                        objItemWiseConsumption.setSubTotal(objItemWiseConsumption.getSubTotal() + rsSalesMod.getDouble(4));
                        //objItemWiseConsumption.setTotalQty(objItemWiseConsumption.getTotalQty() + rsSalesMod.getDouble(3));
                    }
                    else
                    {
                        sqlNo++;
                        objItemWiseConsumption = new clsItemWiseConsumption();
                        objItemWiseConsumption.setItemCode(rsSalesMod.getString(1));
                        objItemWiseConsumption.setItemName(rsSalesMod.getString(2));
                        objItemWiseConsumption.setSubGroupName(rsSalesMod.getString(8));
                        objItemWiseConsumption.setGroupName(rsSalesMod.getString(9));
                        objItemWiseConsumption.setSaleQty(rsSalesMod.getDouble(3));
                        objItemWiseConsumption.setComplimentaryQty(0);
                        objItemWiseConsumption.setNcQty(0);
                        objItemWiseConsumption.setSubTotal(rsSalesMod.getDouble(4));
                        objItemWiseConsumption.setDiscAmt(rsSalesMod.getDouble(7));
                        objItemWiseConsumption.setSaleAmt(rsSalesMod.getDouble(4) - rsSalesMod.getDouble(7));
                        objItemWiseConsumption.setPOSName(rsSalesMod.getString(6));
                        objItemWiseConsumption.setPromoQty(0);
                        objItemWiseConsumption.setSeqNo(sqlNo);
                        objItemWiseConsumption.setExternalCode(rsSalesMod.getString(11));
                        double totalRowQty = rsSalesMod.getDouble(3) + 0 + 0 + 0;
                        //objItemWiseConsumption.setTotalQty(totalRowQty);
                        objItemWiseConsumption.setTotalQty(0);

                    }
                    if (null != objItemWiseConsumption)
                    {
                        hmItemWiseConsumption.put(rsSalesMod.getString(1) + "!" + rsSalesMod.getString(2), objItemWiseConsumption);
                    }

                }
                rsSalesMod.close();

            }
            rsSales.close();

            // for Sales Qty for bill detail q data 
            sbSql.setLength(0);
            /*
             * sbSql.append("select
             * b.stritemcode,b.stritemname,sum(b.dblQuantity),sum(b.dblamount),b.dblRate"
             * + "
             * ,e.strposname,b.dblDiscountAmt,g.strSubGroupName,h.strGroupName,a.strBillNo,b.dblTaxAmount
             * " + " from tblqbillhd a,tblqbilldtl b, tblqbillsettlementdtl
             * c,tblsettelmenthd d,tblposmaster e " + " ,tblitemmaster
             * f,tblsubgrouphd g,tblgrouphd h " + " where
             * a.strBillNo=b.strBillNo and a.strBillNo=c.strBillNo and
             * c.strSettlementCode=d.strSettelmentCode " + " and
             * a.strPOSCode=e.strPosCode and b.strItemCode=f.strItemCode and
             * f.strSubGroupCode=g.strSubGroupCode " + " and
             * g.strGroupCode=h.strGroupCode and
             * d.strSettelmentType!='Complementary' " + " and
             * date(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate +
             * "' ");
             */
            sbSql.append("select b.stritemcode,b.stritemname,sum(b.dblQuantity),sum(b.dblamount),b.dblRate , "
                    + " e.strposname,b.dblDiscountAmt,g.strSubGroupName,h.strGroupName,a.strBillNo,f.strExternalCode "
                    + " from tblqbillhd a,tblqbilldtl b, tblposmaster e ,tblitemmaster f,tblsubgrouphd g,tblgrouphd h "
                    + " where a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " and a.strPOSCode=e.strPosCode "
                    + " and b.strItemCode=f.strItemCode "
                    + " and f.strSubGroupCode=g.strSubGroupCode  "
                    + " and g.strGroupCode=h.strGroupCode    "
                    + " and date(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
                    + " and a.strBillNo NOT IN (select f.strBillNo from tblqbillcomplementrydtl f ) ");
            if (!posCode.equals("All"))
            {
                sbSql.append(" and a.strPOSCode = '" + posCode + "' ");
            }

            if (!groupCode.equalsIgnoreCase("All"))
            {
                sbSql.append(" and h.strGroupCode = '" + groupCode + "' ");
            }

            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sbSql.append(" and a.intShiftCode = '" + shiftNo + "' ");
                }
            }
            sbSql.append(" group by b.strItemCode order by a.strPOSCode,b.strItemName");
            //System.out.println(sbSql);

            rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
            while (rsSales.next())
            {
                clsItemWiseConsumption objItemWiseConsumption = null;
                if (null != hmItemWiseConsumption.get(rsSales.getString(1) + "!" + rsSales.getString(2)))
                {
                    objItemWiseConsumption = hmItemWiseConsumption.get(rsSales.getString(1) + "!" + rsSales.getString(2));
                    objItemWiseConsumption.setSaleQty(objItemWiseConsumption.getSaleQty() + rsSales.getDouble(3));
                    objItemWiseConsumption.setSaleAmt(objItemWiseConsumption.getSaleAmt() + (rsSales.getDouble(4) - rsSales.getDouble(7)));
                    objItemWiseConsumption.setSubTotal(objItemWiseConsumption.getSubTotal() + rsSales.getDouble(4));
                    //objItemWiseConsumption.setTotalQty(objItemWiseConsumption.getTotalQty() + rsSales.getDouble(3));
                }
                else
                {
                    sqlNo++;
                    objItemWiseConsumption = new clsItemWiseConsumption();
                    objItemWiseConsumption.setItemCode(rsSales.getString(1));
                    objItemWiseConsumption.setItemName(rsSales.getString(2));
                    objItemWiseConsumption.setSubGroupName(rsSales.getString(8));
                    objItemWiseConsumption.setGroupName(rsSales.getString(9));
                    objItemWiseConsumption.setSaleQty(rsSales.getDouble(3));
                    objItemWiseConsumption.setComplimentaryQty(0);
                    objItemWiseConsumption.setNcQty(0);
                    objItemWiseConsumption.setSubTotal(rsSales.getDouble(4));
                    objItemWiseConsumption.setDiscAmt(rsSales.getDouble(7));
                    objItemWiseConsumption.setSaleAmt(rsSales.getDouble(4) - rsSales.getDouble(7));
                    objItemWiseConsumption.setPOSName(rsSales.getString(6));
                    objItemWiseConsumption.setPromoQty(0);
                    objItemWiseConsumption.setSeqNo(sqlNo);
                    objItemWiseConsumption.setExternalCode(rsSales.getString(11));
                    double totalRowQty = rsSales.getDouble(3) + 0 + 0 + 0;
                    //objItemWiseConsumption.setTotalQty(totalRowQty);
                    objItemWiseConsumption.setTotalQty(0);

                }
                if (null != objItemWiseConsumption)
                {
                    hmItemWiseConsumption.put(rsSales.getString(1) + "!" + rsSales.getString(2), objItemWiseConsumption);
                }
                sbSqlMod.setLength(0);
                if (printZeroAmountModi.equalsIgnoreCase("Yes"))//Tjs brew works dont want modifiers details
                {
                    // Code for Sales Qty for modifier live & q data

                    sbSqlMod.append("select b.strItemCode,b.strModifierName,sum(b.dblQuantity),sum(b.dblamount),b.dblRate"
                            + " ,e.strposname,b.dblDiscAmt,g.strSubGroupName,h.strGroupName,a.strBillNo,f.strExternalCode"
                            + " from tblqbillhd a,tblqbillmodifierdtl b, tblqbillsettlementdtl c,tblsettelmenthd d,tblposmaster e "
                            + " ,tblitemmaster f,tblsubgrouphd g,tblgrouphd h "
                            + " where a.strBillNo=b.strBillNo "
                            + " and date(a.dteBillDate)=date(b.dteBillDate) "
                            + " and a.strBillNo=c.strBillNo "
                            + " and date(a.dteBillDate)=date(c.dteBillDate) "
                            + " and c.strSettlementCode=d.strSettelmentCode "
                            + " and a.strPOSCode=e.strPosCode "
                            + " and left(b.strItemCode,7)=f.strItemCode "
                            + " and f.strSubGroupCode=g.strSubGroupCode "
                            + " and g.strGroupCode=h.strGroupCode "
                            + " and d.strSettelmentType!='Complementary' "
                            + " and left(b.strItemCode,7)='" + rsSales.getString(1) + "' "
                            + " and a.strBillNo='" + rsSales.getString(10) + "' "
                            + " and date(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
                            + " group by b.strItemCode,b.strModifierName ");
                }
                else
                {
                    sbSqlMod.append("select b.strItemCode,b.strModifierName,sum(b.dblQuantity),sum(b.dblamount),b.dblRate"
                            + " ,e.strposname,b.dblDiscAmt,g.strSubGroupName,h.strGroupName,a.strBillNo,f.strExternalCode"
                            + " from tblqbillhd a,tblqbillmodifierdtl b, tblqbillsettlementdtl c,tblsettelmenthd d,tblposmaster e "
                            + " ,tblitemmaster f,tblsubgrouphd g,tblgrouphd h "
                            + " where a.strBillNo=b.strBillNo "
                            + " and date(a.dteBillDate)=date(b.dteBillDate) "
                            + " and a.strBillNo=c.strBillNo "
                            + " and date(a.dteBillDate)=date(c.dteBillDate) "
                            + " and c.strSettlementCode=d.strSettelmentCode "
                            + " and a.strPOSCode=e.strPosCode "
                            + " and left(b.strItemCode,7)=f.strItemCode "
                            + " and f.strSubGroupCode=g.strSubGroupCode "
                            + " and g.strGroupCode=h.strGroupCode "
                            + " and d.strSettelmentType!='Complementary' "
                            + " and left(b.strItemCode,7)='" + rsSales.getString(1) + "' "
                            + " and a.strBillNo='" + rsSales.getString(10) + "' "
                            + " and date(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' And  b.dblamount >0"
                            + " group by b.strItemCode,b.strModifierName ");
                }
                sbSqlMod.append(sbFilters);
                //System.out.println(sbSqlMod);

                rsSalesMod = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlMod.toString());
                while (rsSalesMod.next())
                {

                    if (null != hmItemWiseConsumption.get(rsSalesMod.getString(1) + "!" + rsSalesMod.getString(2)))
                    {
                        objItemWiseConsumption = hmItemWiseConsumption.get(rsSalesMod.getString(1) + "!" + rsSalesMod.getString(2));
                        objItemWiseConsumption.setSaleQty(objItemWiseConsumption.getSaleQty() + rsSalesMod.getDouble(3));
                        objItemWiseConsumption.setSaleAmt(objItemWiseConsumption.getSaleAmt() + (rsSalesMod.getDouble(4) - rsSalesMod.getDouble(7)));
                        objItemWiseConsumption.setSubTotal(objItemWiseConsumption.getSubTotal() + rsSalesMod.getDouble(4));
                        //objItemWiseConsumption.setTotalQty(objItemWiseConsumption.getTotalQty() + rsSalesMod.getDouble(3));
                    }
                    else
                    {
                        sqlNo++;
                        objItemWiseConsumption = new clsItemWiseConsumption();
                        objItemWiseConsumption.setItemCode(rsSalesMod.getString(1));
                        objItemWiseConsumption.setItemName(rsSalesMod.getString(2));
                        objItemWiseConsumption.setSubGroupName(rsSalesMod.getString(8));
                        objItemWiseConsumption.setGroupName(rsSalesMod.getString(9));
                        objItemWiseConsumption.setSaleQty(rsSalesMod.getDouble(3));
                        objItemWiseConsumption.setComplimentaryQty(0);
                        objItemWiseConsumption.setNcQty(0);
                        objItemWiseConsumption.setSubTotal(rsSalesMod.getDouble(4));
                        objItemWiseConsumption.setDiscAmt(rsSalesMod.getDouble(7));
                        objItemWiseConsumption.setSaleAmt(rsSalesMod.getDouble(4) - rsSalesMod.getDouble(7));
                        objItemWiseConsumption.setPOSName(rsSalesMod.getString(6));
                        objItemWiseConsumption.setPromoQty(0);
                        objItemWiseConsumption.setSeqNo(sqlNo);
                        objItemWiseConsumption.setExternalCode(rsSalesMod.getString(11));
                        double totalRowQty = rsSalesMod.getDouble(3) + 0 + 0 + 0;
                        //objItemWiseConsumption.setTotalQty(totalRowQty);
                        objItemWiseConsumption.setTotalQty(0);
                    }
                    if (null != objItemWiseConsumption)
                    {
                        hmItemWiseConsumption.put(rsSalesMod.getString(1) + "!" + rsSalesMod.getString(2), objItemWiseConsumption);
                    }
                }
                rsSalesMod.close();

            }
            rsSales.close();

            // Code for Complimentary Qty for live & q bill detail and bill modifier data   
            //for Complimentary Qty for live bill detail
            sbSql.setLength(0);
            sbSql.append("select b.stritemcode,b.stritemname,sum(b.dblQuantity),sum(b.dblamount),b.dblRate"
                    + " ,e.strposname,b.dblDiscountAmt,g.strSubGroupName,h.strGroupName,a.strBillNo,f.strExternalCode "
                    + " from tblbillhd a,tblbilldtl b, tblbillsettlementdtl c,tblsettelmenthd d,tblposmaster e "
                    + " ,tblitemmaster f,tblsubgrouphd g,tblgrouphd h "
                    + " where a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " and a.strBillNo=c.strBillNo "
                    + " and date(a.dteBillDate)=date(c.dteBillDate) "
                    + " and c.strSettlementCode=d.strSettelmentCode "
                    + " and a.strPOSCode=e.strPosCode "
                    + " and b.strItemCode=f.strItemCode "
                    + " and f.strSubGroupCode=g.strSubGroupCode "
                    + " and g.strGroupCode=h.strGroupCode "
                    + " and d.strSettelmentType='Complementary' "
                    + " and date(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' ");
            if (!posCode.equals("All"))
            {
                sbSql.append(" and a.strPOSCode = '" + posCode + "' ");
            }

            if (!groupCode.equalsIgnoreCase("All"))
            {
                sbSql.append(" and h.strGroupCode = '" + groupCode + "' ");
            }

            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sbSql.append(" and a.intShiftCode = '" + shiftNo + "' ");
                }
            }
            sbSql.append(" group by b.strItemCode order by a.strPOSCode,b.strItemName");
            //System.out.println(sbSql);

            ResultSet rsComplimentary = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
            while (rsComplimentary.next())
            {
                clsItemWiseConsumption objItemWiseConsumption = null;
                if (null != hmItemWiseConsumption.get(rsComplimentary.getString(1) + "!" + rsComplimentary.getString(2)))
                {
                    objItemWiseConsumption = hmItemWiseConsumption.get(rsComplimentary.getString(1) + "!" + rsComplimentary.getString(2));
                    objItemWiseConsumption.setComplimentaryQty(objItemWiseConsumption.getComplimentaryQty() + rsComplimentary.getDouble(3));
                    objItemWiseConsumption.setSaleAmt(objItemWiseConsumption.getSaleAmt() + (rsComplimentary.getDouble(4) - rsComplimentary.getDouble(7)));
                    objItemWiseConsumption.setSubTotal(objItemWiseConsumption.getSubTotal() + rsComplimentary.getDouble(4));
                    //System.out.println("Old= " + rsComplimentary.getString(1) + objItemWiseConsumption.getComplimentaryQty());
                    //objItemWiseConsumption.setTotalQty(objItemWiseConsumption.getTotalQty() + rsComplimentary.getDouble(3));
                }
                else
                {
                    sqlNo++;
                    objItemWiseConsumption = new clsItemWiseConsumption();
                    objItemWiseConsumption.setItemCode(rsComplimentary.getString(1));
                    objItemWiseConsumption.setItemName(rsComplimentary.getString(2));
                    objItemWiseConsumption.setSubGroupName(rsComplimentary.getString(8));
                    objItemWiseConsumption.setGroupName(rsComplimentary.getString(9));
                    objItemWiseConsumption.setComplimentaryQty(rsComplimentary.getDouble(3));
                    objItemWiseConsumption.setSaleQty(0);
                    objItemWiseConsumption.setNcQty(0);
                    objItemWiseConsumption.setSubTotal(rsComplimentary.getDouble(4));
                    objItemWiseConsumption.setDiscAmt(rsComplimentary.getDouble(7));
                    objItemWiseConsumption.setSaleAmt(rsComplimentary.getDouble(4) - rsComplimentary.getDouble(7));
                    objItemWiseConsumption.setPOSName(rsComplimentary.getString(6));
                    objItemWiseConsumption.setPromoQty(0);
                    objItemWiseConsumption.setSeqNo(sqlNo);
                    objItemWiseConsumption.setExternalCode(rsComplimentary.getString(11));
                    double totalRowQty = rsComplimentary.getDouble(3) + 0 + 0 + 0;
                    //objItemWiseConsumption.setTotalQty(totalRowQty);
                    objItemWiseConsumption.setTotalQty(0);
                    ///System.out.println("New= " + rsComplimentary.getString(1) + objItemWiseConsumption.getComplimentaryQty());
                }
                if (null != objItemWiseConsumption)
                {
                    hmItemWiseConsumption.put(rsComplimentary.getString(1) + "!" + rsComplimentary.getString(2), objItemWiseConsumption);
                }

                sbSqlMod.setLength(0);
                if (printZeroAmountModi.equalsIgnoreCase("Yes"))//Tjs brew works dont want modifiers details
                {
                    //for Complimentary Qty for live bill modifier

                    sbSqlMod.append("select b.strItemCode,b.strModifierName,sum(b.dblQuantity),sum(b.dblamount),b.dblRate"
                            + " ,e.strposname,b.dblDiscAmt,g.strSubGroupName,h.strGroupName,a.strBillNo,f.strExternalCode "
                            + " from tblbillhd a,tblbillmodifierdtl b, tblbillsettlementdtl c,tblsettelmenthd d,tblposmaster e "
                            + " ,tblitemmaster f,tblsubgrouphd g,tblgrouphd h "
                            + " where a.strBillNo=b.strBillNo "
                            + " and date(a.dteBillDate)=date(b.dteBillDate) "
                            + " and a.strBillNo=c.strBillNo  "
                            + " and date(a.dteBillDate)=date(c.dteBillDate) "
                            + " and c.strSettlementCode=d.strSettelmentCode "
                            + " and a.strPOSCode=e.strPosCode "
                            + " and left(b.strItemCode,7)=f.strItemCode "
                            + " and f.strSubGroupCode=g.strSubGroupCode "
                            + " and g.strGroupCode=h.strGroupCode "
                            + " and d.strSettelmentType='Complementary' "
                            + " and left(b.strItemCode,7)='" + rsComplimentary.getString(1) + "' "
                            + " and a.strBillNo='" + rsComplimentary.getString(10) + "' "
                            + " and date(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
                            + " group by b.strItemCode,b.strModifierName ");
                }
                else
                {
                    sbSqlMod.append("select b.strItemCode,b.strModifierName,sum(b.dblQuantity),sum(b.dblamount),b.dblRate"
                            + " ,e.strposname,b.dblDiscAmt,g.strSubGroupName,h.strGroupName,a.strBillNo,f.strExternalCode "
                            + " from tblbillhd a,tblbillmodifierdtl b, tblbillsettlementdtl c,tblsettelmenthd d,tblposmaster e "
                            + " ,tblitemmaster f,tblsubgrouphd g,tblgrouphd h "
                            + " where a.strBillNo=b.strBillNo "
                            + " and date(a.dteBillDate)=date(b.dteBillDate) "
                            + " and a.strBillNo=c.strBillNo "
                            + " and date(a.dteBillDate)=date(c.dteBillDate) "
                            + " and c.strSettlementCode=d.strSettelmentCode "
                            + " and a.strPOSCode=e.strPosCode "
                            + " and left(b.strItemCode,7)=f.strItemCode "
                            + " and f.strSubGroupCode=g.strSubGroupCode "
                            + " and g.strGroupCode=h.strGroupCode "
                            + " and d.strSettelmentType='Complementary' "
                            + " and left(b.strItemCode,7)='" + rsComplimentary.getString(1) + "' "
                            + " and a.strBillNo='" + rsComplimentary.getString(10) + "' "
                            + " and date(a.dteBillDate) BETWEEN '" + fromDate + "' "
                            + " AND '" + toDate + "' AND  b.dblamount >0"
                            + " group by b.strItemCode,b.strModifierName ");
                }
                sbSqlMod.append(sbFilters);
                //System.out.println(sbSqlMod);

                ResultSet rsModComplimentary = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlMod.toString());
                while (rsModComplimentary.next())
                {
                    if (null != hmItemWiseConsumption.get(rsModComplimentary.getString(1) + "!" + rsModComplimentary.getString(2)))
                    {
                        objItemWiseConsumption = hmItemWiseConsumption.get(rsModComplimentary.getString(1) + "!" + rsModComplimentary.getString(2));
                        objItemWiseConsumption.setComplimentaryQty(objItemWiseConsumption.getComplimentaryQty() + rsModComplimentary.getDouble(3));
                        objItemWiseConsumption.setSaleAmt(objItemWiseConsumption.getSaleAmt() + (rsModComplimentary.getDouble(4) - rsModComplimentary.getDouble(7)));
                        objItemWiseConsumption.setSubTotal(objItemWiseConsumption.getSubTotal() + rsModComplimentary.getDouble(4));
                        //System.out.println("Old= " + rsModComplimentary.getString(1) + objItemWiseConsumption.getComplimentaryQty());
                        //objItemWiseConsumption.setTotalQty(objItemWiseConsumption.getTotalQty() + rsModComplimentary.getDouble(3));
                    }
                    else
                    {
                        sqlNo++;
                        objItemWiseConsumption = new clsItemWiseConsumption();
                        objItemWiseConsumption.setItemCode(rsModComplimentary.getString(1));
                        objItemWiseConsumption.setItemName(rsModComplimentary.getString(2));
                        objItemWiseConsumption.setSubGroupName(rsModComplimentary.getString(8));
                        objItemWiseConsumption.setGroupName(rsModComplimentary.getString(9));
                        objItemWiseConsumption.setComplimentaryQty(rsModComplimentary.getDouble(3));
                        objItemWiseConsumption.setSaleQty(0);
                        objItemWiseConsumption.setNcQty(0);
                        objItemWiseConsumption.setSubTotal(rsModComplimentary.getDouble(4));
                        objItemWiseConsumption.setDiscAmt(rsModComplimentary.getDouble(7));
                        objItemWiseConsumption.setSaleAmt(rsModComplimentary.getDouble(4) - rsModComplimentary.getDouble(7));
                        objItemWiseConsumption.setPOSName(rsModComplimentary.getString(6));
                        objItemWiseConsumption.setSeqNo(sqlNo);
                        objItemWiseConsumption.setPromoQty(0);
                        objItemWiseConsumption.setExternalCode(rsModComplimentary.getString(11));
                        //System.out.println("New= " + rsModComplimentary.getString(1) + objItemWiseConsumption.getComplimentaryQty());
                        double totalRowQty = rsModComplimentary.getDouble(3) + 0 + 0 + 0;
                        //objItemWiseConsumption.setTotalQty(totalRowQty);
                        objItemWiseConsumption.setTotalQty(0);
                    }
                    if (null != objItemWiseConsumption)
                    {
                        hmItemWiseConsumption.put(rsModComplimentary.getString(1) + "!" + rsModComplimentary.getString(2), objItemWiseConsumption);
                    }
                }
                rsModComplimentary.close();

            }
            rsComplimentary.close();

            //for Complimentary Qty for q bill details
            sbSql.setLength(0);
            sbSql.append("select b.stritemcode,b.stritemname,sum(b.dblQuantity),sum(b.dblamount),b.dblRate"
                    + " ,e.strposname,b.dblDiscountAmt,g.strSubGroupName,h.strGroupName,a.strBillNo,f.strExternalCode  "
                    + " from tblqbillhd a,tblqbilldtl b, tblqbillsettlementdtl c,tblsettelmenthd d,tblposmaster e "
                    + " ,tblitemmaster f,tblsubgrouphd g,tblgrouphd h "
                    + " where a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " and a.strBillNo=c.strBillNo "
                    + " and date(a.dteBillDate)=date(c.dteBillDate) "
                    + " and c.strSettlementCode=d.strSettelmentCode "
                    + " and a.strPOSCode=e.strPosCode "
                    + " and b.strItemCode=f.strItemCode "
                    + " and f.strSubGroupCode=g.strSubGroupCode "
                    + " and g.strGroupCode=h.strGroupCode "
                    + " and d.strSettelmentType='Complementary' "
                    + " and date(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' ");
            if (!posCode.equals("All"))
            {
                sbSql.append(" and a.strPOSCode = '" + posCode + "' ");
            }

            if (!groupCode.equalsIgnoreCase("All"))
            {
                sbSql.append(" and h.strGroupCode = '" + groupCode + "' ");
            }
            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sbSql.append(" and a.intShiftCode = '" + shiftNo + "' ");
                }
            }
            sbSql.append(" group by b.strItemCode order by a.strPOSCode,b.strItemName");
            //System.out.println(sbSql);

            rsComplimentary = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
            while (rsComplimentary.next())
            {
                clsItemWiseConsumption objItemWiseConsumption = null;
                if (null != hmItemWiseConsumption.get(rsComplimentary.getString(1) + "!" + rsComplimentary.getString(2)))
                {
                    objItemWiseConsumption = hmItemWiseConsumption.get(rsComplimentary.getString(1) + "!" + rsComplimentary.getString(2));
                    objItemWiseConsumption.setComplimentaryQty(objItemWiseConsumption.getComplimentaryQty() + rsComplimentary.getDouble(3));
                    objItemWiseConsumption.setSaleAmt(objItemWiseConsumption.getSaleAmt() + (rsComplimentary.getDouble(4) - rsComplimentary.getDouble(7)));
                    objItemWiseConsumption.setSubTotal(objItemWiseConsumption.getSubTotal() + rsComplimentary.getDouble(4));
                    //objItemWiseConsumption.setTotalQty(objItemWiseConsumption.getTotalQty() + rsComplimentary.getDouble(3));
                }
                else
                {
                    sqlNo++;
                    objItemWiseConsumption = new clsItemWiseConsumption();
                    objItemWiseConsumption.setItemCode(rsComplimentary.getString(1));
                    objItemWiseConsumption.setItemName(rsComplimentary.getString(2));
                    objItemWiseConsumption.setSubGroupName(rsComplimentary.getString(8));
                    objItemWiseConsumption.setGroupName(rsComplimentary.getString(9));
                    objItemWiseConsumption.setComplimentaryQty(rsComplimentary.getDouble(3));
                    objItemWiseConsumption.setSaleQty(0);
                    objItemWiseConsumption.setNcQty(0);
                    objItemWiseConsumption.setSubTotal(rsComplimentary.getDouble(4));
                    objItemWiseConsumption.setDiscAmt(rsComplimentary.getDouble(7));
                    objItemWiseConsumption.setSaleAmt(rsComplimentary.getDouble(4) - rsComplimentary.getDouble(7));
                    objItemWiseConsumption.setPOSName(rsComplimentary.getString(6));
                    objItemWiseConsumption.setPromoQty(0);
                    objItemWiseConsumption.setSeqNo(sqlNo);
                    objItemWiseConsumption.setExternalCode(rsComplimentary.getString(11));
                    double totalRowQty = rsComplimentary.getDouble(3) + 0 + 0 + 0;
                    //objItemWiseConsumption.setTotalQty(totalRowQty);
                    objItemWiseConsumption.setTotalQty(0);
                }
                if (null != objItemWiseConsumption)
                {
                    hmItemWiseConsumption.put(rsComplimentary.getString(1) + "!" + rsComplimentary.getString(2), objItemWiseConsumption);
                }

                sbSqlMod.setLength(0);
                if (printZeroAmountModi.equalsIgnoreCase("Yes"))//Tjs brew works dont want modifiers details
                {
                    //for Complimentary Qty for q bill modifier 

                    sbSqlMod.append("select b.strItemCode,b.strModifierName,sum(b.dblQuantity),sum(b.dblamount),b.dblRate"
                            + " ,e.strposname,b.dblDiscAmt,g.strSubGroupName,h.strGroupName,a.strBillNo,f.strExternalCode"
                            + " from tblqbillhd a,tblqbillmodifierdtl b, tblqbillsettlementdtl c,tblsettelmenthd d,tblposmaster e "
                            + " ,tblitemmaster f,tblsubgrouphd g,tblgrouphd h "
                            + " where a.strBillNo=b.strBillNo "
                            + " and date(a.dteBillDate)=date(b.dteBillDate) "
                            + " and a.strBillNo=c.strBillNo "
                            + " and date(a.dteBillDate)=date(c.dteBillDate) "
                            + " and c.strSettlementCode=d.strSettelmentCode "
                            + " and a.strPOSCode=e.strPosCode "
                            + " and left(b.strItemCode,7)=f.strItemCode "
                            + " and f.strSubGroupCode=g.strSubGroupCode "
                            + " and g.strGroupCode=h.strGroupCode "
                            + " and d.strSettelmentType='Complementary' "
                            + " and left(b.strItemCode,7)='" + rsComplimentary.getString(1) + "' "
                            + " and a.strBillNo='" + rsComplimentary.getString(10) + "' "
                            + " and date(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
                            + " group by b.strItemCode,b.strModifierName ");
                }
                else
                {
                    sbSqlMod.append("select b.strItemCode,b.strModifierName,sum(b.dblQuantity),sum(b.dblamount),b.dblRate"
                            + " ,e.strposname,b.dblDiscAmt,g.strSubGroupName,h.strGroupName,a.strBillNo,f.strExternalCode"
                            + " from tblqbillhd a,tblqbillmodifierdtl b, tblqbillsettlementdtl c,tblsettelmenthd d,tblposmaster e "
                            + " ,tblitemmaster f,tblsubgrouphd g,tblgrouphd h "
                            + " where a.strBillNo=b.strBillNo "
                            + " and date(a.dteBillDate)=date(b.dteBillDate) "
                            + " and a.strBillNo=c.strBillNo "
                            + " and date(a.dteBillDate)=date(c.dteBillDate) "
                            + " and c.strSettlementCode=d.strSettelmentCode "
                            + " and a.strPOSCode=e.strPosCode "
                            + " and left(b.strItemCode,7)=f.strItemCode "
                            + " and f.strSubGroupCode=g.strSubGroupCode "
                            + " and g.strGroupCode=h.strGroupCode "
                            + " and d.strSettelmentType='Complementary' "
                            + " and left(b.strItemCode,7)='" + rsComplimentary.getString(1) + "' "
                            + " and a.strBillNo='" + rsComplimentary.getString(10) + "' "
                            + " and date(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
                            + " AND  b.dblamount >0"
                            + " group by b.strItemCode,b.strModifierName ");
                }
                sbSqlMod.append(sbFilters);
                //System.out.println(sbSqlMod);

                ResultSet rsModComplimentary = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlMod.toString());
                while (rsModComplimentary.next())
                {

                    if (null != hmItemWiseConsumption.get(rsModComplimentary.getString(1) + "!" + rsModComplimentary.getString(2)))
                    {
                        objItemWiseConsumption = hmItemWiseConsumption.get(rsModComplimentary.getString(1) + "!" + rsModComplimentary.getString(2));
                        objItemWiseConsumption.setComplimentaryQty(objItemWiseConsumption.getComplimentaryQty() + rsModComplimentary.getDouble(3));
                        objItemWiseConsumption.setSaleAmt(objItemWiseConsumption.getSaleAmt() + (rsModComplimentary.getDouble(4) - rsModComplimentary.getDouble(7)));
                        objItemWiseConsumption.setSubTotal(objItemWiseConsumption.getSubTotal() + rsModComplimentary.getDouble(4));
                        //objItemWiseConsumption.setTotalQty(objItemWiseConsumption.getTotalQty() + rsModComplimentary.getDouble(3));
                    }
                    else
                    {
                        sqlNo++;
                        objItemWiseConsumption = new clsItemWiseConsumption();
                        objItemWiseConsumption.setItemCode(rsModComplimentary.getString(1));
                        objItemWiseConsumption.setItemName(rsModComplimentary.getString(2));
                        objItemWiseConsumption.setSubGroupName(rsModComplimentary.getString(8));
                        objItemWiseConsumption.setGroupName(rsModComplimentary.getString(9));
                        objItemWiseConsumption.setComplimentaryQty(rsModComplimentary.getDouble(3));
                        objItemWiseConsumption.setSaleQty(0);
                        objItemWiseConsumption.setNcQty(0);
                        objItemWiseConsumption.setSubTotal(rsModComplimentary.getDouble(4));
                        objItemWiseConsumption.setDiscAmt(rsModComplimentary.getDouble(7));
                        objItemWiseConsumption.setSaleAmt(rsModComplimentary.getDouble(4) - rsModComplimentary.getDouble(7));
                        objItemWiseConsumption.setPOSName(rsModComplimentary.getString(6));
                        objItemWiseConsumption.setPromoQty(0);
                        objItemWiseConsumption.setSeqNo(sqlNo);
                        objItemWiseConsumption.setExternalCode(rsModComplimentary.getString(11));
                        double totalRowQty = rsModComplimentary.getDouble(3) + 0 + 0 + 0;
                        //objItemWiseConsumption.setTotalQty(totalRowQty);
                        objItemWiseConsumption.setTotalQty(0);
                    }
                    if (null != objItemWiseConsumption)
                    {
                        hmItemWiseConsumption.put(rsModComplimentary.getString(1) + "!" + rsModComplimentary.getString(2), objItemWiseConsumption);
                    }
                }
                rsModComplimentary.close();

            }
            rsComplimentary.close();

            // Code for NC Qty    
            sbSql.setLength(0);
            sbSql.append("select a.stritemcode,b.stritemname,sum(a.dblQuantity),sum(a.dblQuantity*a.dblRate)"
                    + ",a.dblRate, c.strposname,0 as DiscAmt,d.strSubGroupName,e.strGroupName ,b.strExternalCode"
                    + " from tblnonchargablekot a, tblitemmaster b, tblposmaster c,tblsubgrouphd d,tblgrouphd e "
                    + " where left(a.strItemCode,7)=b.strItemCode "
                    + " and a.strPOSCode=c.strPosCode "
                    + " and b.strSubGroupCode=d.strSubGroupCode "
                    + " and d.strGroupCode=e.strGroupCode "
                    + " and date(a.dteNCKOTDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' ");
            if (!posCode.equals("All"))
            {
                sbSql.append(" AND a.strPOSCode = '" + posCode + "' ");
            }

            if (!groupCode.equalsIgnoreCase("All"))
            {
                sbSql.append(" and e.strGroupCode = '" + groupCode + "' ");
            }

            sbSql.append(" group by a.strItemCode order by a.strPOSCode,b.strItemName");
            //System.out.println(sbSql);

            ResultSet rsNCKOT = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
            while (rsNCKOT.next())
            {
                clsItemWiseConsumption objItemWiseConsumption = null;
                if (null != hmItemWiseConsumption.get(rsNCKOT.getString(1) + "!" + rsNCKOT.getString(2)))
                {
                    objItemWiseConsumption = hmItemWiseConsumption.get(rsNCKOT.getString(1) + "!" + rsNCKOT.getString(2));
                    objItemWiseConsumption.setNcQty(objItemWiseConsumption.getNcQty() + rsNCKOT.getDouble(3));
                    objItemWiseConsumption.setSaleAmt(objItemWiseConsumption.getSaleAmt());
                    objItemWiseConsumption.setSubTotal(objItemWiseConsumption.getSubTotal());
                    //objItemWiseConsumption.setTotalQty(objItemWiseConsumption.getTotalQty() + rsNCKOT.getDouble(3));
                }
                else
                {
                    sqlNo++;
                    objItemWiseConsumption = new clsItemWiseConsumption();
                    objItemWiseConsumption.setItemCode(rsNCKOT.getString(1));
                    objItemWiseConsumption.setItemName(rsNCKOT.getString(2));
                    objItemWiseConsumption.setSubGroupName(rsNCKOT.getString(8));
                    objItemWiseConsumption.setGroupName(rsNCKOT.getString(9));
                    objItemWiseConsumption.setNcQty(rsNCKOT.getDouble(3));
                    objItemWiseConsumption.setSaleQty(0);
                    objItemWiseConsumption.setComplimentaryQty(0);
                    //   objItemWiseConsumption.setSubTotal(rsNCKOT.getDouble(4));
                    objItemWiseConsumption.setSubTotal(0);
                    objItemWiseConsumption.setDiscAmt(rsNCKOT.getDouble(7));
                    //   objItemWiseConsumption.setSaleAmt(rsNCKOT.getDouble(4) - rsNCKOT.getDouble(7));
                    objItemWiseConsumption.setSaleAmt(0);
                    objItemWiseConsumption.setPOSName(rsNCKOT.getString(6));
                    objItemWiseConsumption.setPromoQty(0);
                    objItemWiseConsumption.setSeqNo(sqlNo);
                    objItemWiseConsumption.setExternalCode(rsNCKOT.getString(10));
                    double totalRowQty = rsNCKOT.getDouble(3) + 0 + 0 + 0;
                    //objItemWiseConsumption.setTotalQty(totalRowQty);
                    objItemWiseConsumption.setTotalQty(0);
                }
                if (null != objItemWiseConsumption)
                {
                    hmItemWiseConsumption.put(rsNCKOT.getString(1) + "!" + rsNCKOT.getString(2), objItemWiseConsumption);
                }
            }
            rsNCKOT.close();

            // Code for promotion Qty for Q
            sbSql.setLength(0);
            sbSql.append("select b.strItemCode,c.strItemName,sum(b.dblQuantity),sum(b.dblAmount),b.dblRate"
                    + " ,f.strPosName,0,d.strSubGroupName,e.strGroupName,c.strExternalCode "
                    + " from tblqbillhd a,tblqbillpromotiondtl b,tblitemmaster c,tblsubgrouphd d,tblgrouphd e,tblposmaster f "
                    + " where a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " and b.strItemCode=c.strItemCode "
                    + " and c.strSubGroupCode=d.strSubGroupCode "
                    + " and d.strGroupCode=e.strGroupCode "
                    + " and a.strPOSCode=f.strPosCode  "
                    + " and DATE(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' ");
            if (!posCode.equals("All"))
            {
                sbSql.append(" AND a.strPOSCode = '" + posCode + "' ");
            }

            if (!groupCode.equalsIgnoreCase("All"))
            {
                sbSql.append(" and e.strGroupCode = '" + groupCode + "' ");
            }

            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sbSql.append(" and a.intShiftCode = '" + shiftNo + "' ");
                }
            }
            sbSql.append(" group by b.strItemCode,a.strBillNo  order by a.strPOSCode,c.strItemName");
            //System.out.println(sbSql);

            rsSalesMod = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
            while (rsSalesMod.next())
            {
                clsItemWiseConsumption objItemWiseConsumption = null;
                if (null != hmItemWiseConsumption.get(rsSalesMod.getString(1) + "!" + rsSalesMod.getString(2)))
                {
                    objItemWiseConsumption = hmItemWiseConsumption.get(rsSalesMod.getString(1) + "!" + rsSalesMod.getString(2));
                    double saleQty = objItemWiseConsumption.getSaleQty();
                    if (saleQty > 0)
                    {
                        objItemWiseConsumption.setSaleQty(objItemWiseConsumption.getSaleQty() - rsSalesMod.getDouble(3));
                        objItemWiseConsumption.setTotalQty(objItemWiseConsumption.getTotalQty() - rsSalesMod.getDouble(3));
                    }

                    objItemWiseConsumption.setPromoQty(objItemWiseConsumption.getPromoQty() + rsSalesMod.getDouble(3));
                    double qty = objItemWiseConsumption.getTotalQty();
                    //objItemWiseConsumption.setTotalQty(qty + objItemWiseConsumption.getPromoQty());
                }
                else
                {
                    sqlNo++;
                    objItemWiseConsumption = new clsItemWiseConsumption();
                    objItemWiseConsumption.setItemCode(rsSalesMod.getString(1));
                    objItemWiseConsumption.setItemName(rsSalesMod.getString(2));
                    objItemWiseConsumption.setSubGroupName(rsSalesMod.getString(8));
                    objItemWiseConsumption.setGroupName(rsSalesMod.getString(9));
                    objItemWiseConsumption.setNcQty(0);
                    objItemWiseConsumption.setPromoQty(rsSalesMod.getDouble(3));
                    objItemWiseConsumption.setSaleQty(0);
                    objItemWiseConsumption.setComplimentaryQty(0);
                    objItemWiseConsumption.setSubTotal(rsSalesMod.getDouble(4));
                    objItemWiseConsumption.setDiscAmt(rsSalesMod.getDouble(7));
                    objItemWiseConsumption.setSaleAmt(rsSalesMod.getDouble(4) - rsSalesMod.getDouble(7));
                    objItemWiseConsumption.setPOSName(rsSalesMod.getString(6));
                    objItemWiseConsumption.setSeqNo(sqlNo);
                    objItemWiseConsumption.setExternalCode(rsSalesMod.getString(10));
                    double totalRowQty = rsSalesMod.getDouble(3) + 0 + 0 + 0;
                    //objItemWiseConsumption.setTotalQty(totalRowQty);
                    objItemWiseConsumption.setTotalQty(0);
                }
                if (null != objItemWiseConsumption)
                {
                    hmItemWiseConsumption.put(rsSalesMod.getString(1) + "!" + rsSalesMod.getString(2), objItemWiseConsumption);
                }
            }
            rsSalesMod.close();

            // Code for promotion Qty for live
            sbSql.setLength(0);
            sbSql.append("select b.strItemCode,c.strItemName,sum(b.dblQuantity),sum(b.dblAmount),b.dblRate"
                    + " ,f.strPosName,0,d.strSubGroupName,e.strGroupName,c.strExternalCode "
                    + " from tblbillhd a,tblbillpromotiondtl b,tblitemmaster c,tblsubgrouphd d,tblgrouphd e,tblposmaster f "
                    + " where a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " and b.strItemCode=c.strItemCode "
                    + " and c.strSubGroupCode=d.strSubGroupCode "
                    + " and d.strGroupCode=e.strGroupCode "
                    + " and a.strPOSCode=f.strPosCode  "
                    + " and DATE(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' ");
            if (!posCode.equals("All"))
            {
                sbSql.append(" AND a.strPOSCode = '" + posCode + "' ");
            }

            if (!groupCode.equalsIgnoreCase("All"))
            {
                sbSql.append(" and e.strGroupCode = '" + groupCode + "' ");
            }

            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sbSql.append(" and a.intShiftCode = '" + shiftNo + "' ");
                }
            }
            sbSql.append(" group by b.strItemCode order by a.strPOSCode,c.strItemName");
            //System.out.println(sbSql);

            rsSalesMod = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
            while (rsSalesMod.next())
            {
                clsItemWiseConsumption objItemWiseConsumption = null;
                if (null != hmItemWiseConsumption.get(rsSalesMod.getString(1) + "!" + rsSalesMod.getString(2)))
                {
                    objItemWiseConsumption = hmItemWiseConsumption.get(rsSalesMod.getString(1) + "!" + rsSalesMod.getString(2));
                    double saleQty = objItemWiseConsumption.getSaleQty();
                    if (saleQty > 0)
                    {
                        objItemWiseConsumption.setSaleQty(objItemWiseConsumption.getSaleQty() - rsSalesMod.getDouble(3));
                        objItemWiseConsumption.setTotalQty(objItemWiseConsumption.getTotalQty() - rsSalesMod.getDouble(3));
                    }

                    objItemWiseConsumption.setPromoQty(objItemWiseConsumption.getPromoQty() + rsSalesMod.getDouble(3));
                    double qty = objItemWiseConsumption.getTotalQty();
                    //objItemWiseConsumption.setTotalQty(qty + objItemWiseConsumption.getPromoQty());
                }
                else
                {
                    sqlNo++;
                    objItemWiseConsumption = new clsItemWiseConsumption();
                    objItemWiseConsumption.setItemCode(rsSalesMod.getString(1));
                    objItemWiseConsumption.setItemName(rsSalesMod.getString(2));
                    objItemWiseConsumption.setSubGroupName(rsSalesMod.getString(8));
                    objItemWiseConsumption.setGroupName(rsSalesMod.getString(9));
                    objItemWiseConsumption.setNcQty(0);
                    objItemWiseConsumption.setPromoQty(rsSalesMod.getDouble(3));
                    objItemWiseConsumption.setSaleQty(0);
                    objItemWiseConsumption.setComplimentaryQty(0);
                    objItemWiseConsumption.setSubTotal(rsSalesMod.getDouble(4));
                    objItemWiseConsumption.setDiscAmt(rsSalesMod.getDouble(7));
                    objItemWiseConsumption.setSaleAmt(rsSalesMod.getDouble(4) - rsSalesMod.getDouble(7));
                    objItemWiseConsumption.setPOSName(rsSalesMod.getString(6));
                    objItemWiseConsumption.setSeqNo(sqlNo);
                    objItemWiseConsumption.setExternalCode(rsSalesMod.getString(10));
                    double totalRowQty = rsSalesMod.getDouble(3) + 0 + 0 + 0;
                    //objItemWiseConsumption.setTotalQty(totalRowQty);
                    objItemWiseConsumption.setTotalQty(0);
                }
                if (null != objItemWiseConsumption)
                {
                    hmItemWiseConsumption.put(rsSalesMod.getString(1) + "!" + rsSalesMod.getString(2), objItemWiseConsumption);
                }
            }
            rsSalesMod.close();

            List<clsItemWiseConsumption> list = new ArrayList<clsItemWiseConsumption>();
            for (Map.Entry<String, clsItemWiseConsumption> entry : hmItemWiseConsumption.entrySet())
            {
                clsItemWiseConsumption objItemComp = entry.getValue();
                double totalRowQty = objItemComp.getSaleQty() + objItemComp.getComplimentaryQty() + objItemComp.getNcQty() + objItemComp.getPromoQty();
                objItemComp.setTotalQty(totalRowQty);
                list.add(objItemComp);
            }

            //sort list 
            // Collections.sort(list, clsItemWiseConsumption.comparatorItemConsumptionColumnDtl);
            Comparator<clsItemWiseConsumption> groupNameComparator = new Comparator<clsItemWiseConsumption>()
            {

                @Override
                public int compare(clsItemWiseConsumption o1, clsItemWiseConsumption o2)
                {
                    return o1.getGroupName().compareToIgnoreCase(o2.getGroupName());
                }
            };
            Comparator<clsItemWiseConsumption> posNameComparator = new Comparator<clsItemWiseConsumption>()
            {

                @Override
                public int compare(clsItemWiseConsumption o1, clsItemWiseConsumption o2)
                {
                    return o1.getPOSName().compareToIgnoreCase(o2.getPOSName());
                }
            };

            Comparator<clsItemWiseConsumption> itemCodeComparator = new Comparator<clsItemWiseConsumption>()
            {

                @Override
                public int compare(clsItemWiseConsumption o1, clsItemWiseConsumption o2)
                {
                    return o1.getItemCode().substring(0, 7).compareToIgnoreCase(o2.getItemCode().substring(0, 7));
                }
            };

            Comparator<clsItemWiseConsumption> seqNoComparator = new Comparator<clsItemWiseConsumption>()
            {

                @Override
                public int compare(clsItemWiseConsumption o1, clsItemWiseConsumption o2)
                {
                    int seqNo1 = o1.getSeqNo();
                    int seqNo2 = o2.getSeqNo();

                    if (seqNo1 == seqNo2)
                    {
                        return 0;
                    }
                    else if (seqNo1 > seqNo2)
                    {
                        return 1;
                    }
                    else
                    {
                        return -1;
                    }
                }
            };

            Collections.sort(list, new clsItemConsumptionComparator(
                    posNameComparator, groupNameComparator, itemCodeComparator, seqNoComparator
            ));

            //call for view report
            funViewJasperReportForBeanCollectionDataSource(is, hm, list);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void funTableWisePaxReportForJasper()
    {

        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();

            Map<String, List<String>> mapTablePaxList = new HashMap<String, List<String>>();
            List<String> arrListTablePax = null;
            List<clsBillDtl> arrListPaxData = null;
            int totalPax = 0;

            int position = 0;

            String sqlFilters = "";

            String sqlLive = "select b.strTableNo,b.strTableName,sum(a.intPaxNo) "
                    + " from tblbillhd a,tbltablemaster b "
                    + " where a.strTableNo=b.strTableNo "
                    + " and date( a.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
                    + " and a.strClientCode=b.strClientCode ";

            String sqlQFile = "select b.strTableNo,b.strTableName,sum(a.intPaxNo) "
                    + " from tblqbillhd a,tbltablemaster b "
                    + " where a.strTableNo=b.strTableNo "
                    + " and date( a.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
                    + " and a.strClientCode=b.strClientCode ";

            if (!posCode.equals("All"))
            {
                sqlFilters += " and a.strPOSCode = '" + posCode + "' ";
            }

            sqlFilters += " group by b.strTableNo,a.strPOSCode   ";

            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sqlFilters += " and a.intShiftCode = '" + shiftNo + "' ";
                }
            }

            sqlLive = sqlLive + " " + sqlFilters;
            sqlQFile = sqlQFile + " " + sqlFilters;

            ResultSet rsLiveTablePax = clsGlobalVarClass.dbMysql.executeResultSet(sqlLive);
            while (rsLiveTablePax.next())
            {
                int totalPaxNo = 0;
                if (null != mapTablePaxList.get(rsLiveTablePax.getString(1)))
                {
                    String existPax = "";
                    arrListTablePax = mapTablePaxList.get(rsLiveTablePax.getString(1));
                    for (int i = 0; i < arrListTablePax.size(); i++)
                    {
                        String[] param = arrListTablePax.get(i).split("#");
                        existPax = param[2];
                        int pax = Integer.parseInt(rsLiveTablePax.getString(3));
                        totalPaxNo += Integer.parseInt(existPax) + pax;
                    }
                    arrListTablePax.remove(rsLiveTablePax.getString(1) + "#" + rsLiveTablePax.getString(2) + "#" + existPax);
                    arrListTablePax.add(rsLiveTablePax.getString(1) + "#" + rsLiveTablePax.getString(2) + "#" + totalPaxNo);
                    mapTablePaxList.remove(rsLiveTablePax.getString(1));
                }
                else
                {
                    arrListTablePax = new ArrayList<String>();
                    arrListTablePax.add(rsLiveTablePax.getString(1) + "#" + rsLiveTablePax.getString(2) + "#" + rsLiveTablePax.getString(3));

                }

                if (null != arrListTablePax)
                {
                    mapTablePaxList.put(rsLiveTablePax.getString(1), arrListTablePax);
                }
            }
            rsLiveTablePax.close();

            ResultSet rsQTablePax = clsGlobalVarClass.dbMysql.executeResultSet(sqlQFile);
            while (rsQTablePax.next())
            {
                int totalPaxNo = 0;
                if (null != mapTablePaxList.get(rsQTablePax.getString(1)))
                {
                    String existPax = "";
                    arrListTablePax = mapTablePaxList.get(rsQTablePax.getString(1));
                    for (int i = 0; i < arrListTablePax.size(); i++)
                    {
                        String[] param = arrListTablePax.get(i).split("#");
                        existPax = param[2];
                        int pax = Integer.parseInt(rsQTablePax.getString(3));
                        totalPaxNo += Integer.parseInt(existPax) + pax;
                    }
                    arrListTablePax.remove(rsQTablePax.getString(1) + "#" + rsQTablePax.getString(2) + "#" + existPax);
                    arrListTablePax.add(rsQTablePax.getString(1) + "#" + rsQTablePax.getString(2) + "#" + totalPaxNo);
                    mapTablePaxList.remove(rsQTablePax.getString(1));
                }
                else
                {
                    arrListTablePax = new ArrayList<String>();
                    arrListTablePax.add(rsQTablePax.getString(1) + "#" + rsQTablePax.getString(2) + "#" + rsQTablePax.getString(3));

                }

                if (null != arrListTablePax)
                {
                    mapTablePaxList.put(rsQTablePax.getString(1), arrListTablePax);
                }
            }
            rsQTablePax.close();

            if (mapTablePaxList.size() > 0)
            {
                arrListPaxData = new ArrayList<clsBillDtl>();

                for (Map.Entry<String, List<String>> entry : mapTablePaxList.entrySet())
                {
                    List<String> listOfTablePax = entry.getValue();
                    for (int i = 0; i < listOfTablePax.size(); i++)
                    {

                        String[] tablePaxData = listOfTablePax.get(i).split("#");
                        clsBillDtl objPaxData = new clsBillDtl();
                        objPaxData.setStrBillNo(entry.getKey());
                        objPaxData.setStrTableName(tablePaxData[1]);
                        objPaxData.setDblQuantity(Double.valueOf(tablePaxData[2]));
                        arrListPaxData.add(objPaxData);
                    }

                }
            }

            //call for view report
            funViewJasperReportForBeanCollectionDataSource(is, hm, arrListPaxData);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void funGenerateGroupWiseSales(ResultSet rsGroupWiseSales)
    {
        try
        {
            boolean flgRecords = false;
            while (rsGroupWiseSales.next())
            {
                flgRecords = true;
                if (mapPOSDtlForGroupSubGroup.containsKey(rsGroupWiseSales.getString(10)))//posCode
                {
                    String posCode = rsGroupWiseSales.getString(10);
                    String groupCode = rsGroupWiseSales.getString(1);
                    List<Map<String, clsGroupSubGroupWiseSales>> listOfGroup = mapPOSDtlForGroupSubGroup.get(posCode);
                    boolean isGroupExists = false;
                    int groupIndex = 0;
                    for (int i = 0; i < listOfGroup.size(); i++)
                    {
                        if (listOfGroup.get(i).containsKey(groupCode))
                        {
                            isGroupExists = true;
                            groupIndex = i;
                            break;
                        }
                    }
                    if (isGroupExists)
                    {
                        Map<String, clsGroupSubGroupWiseSales> mapGroupCodeDtl = listOfGroup.get(groupIndex);
                        clsGroupSubGroupWiseSales objGroupCodeDtl = mapGroupCodeDtl.get(groupCode);
                        objGroupCodeDtl.setGroupCode(rsGroupWiseSales.getString(1));
                        objGroupCodeDtl.setGroupName(rsGroupWiseSales.getString(2));
                        objGroupCodeDtl.setPosName(rsGroupWiseSales.getString(5));
                        objGroupCodeDtl.setQty(objGroupCodeDtl.getQty() + rsGroupWiseSales.getDouble(3));
                        objGroupCodeDtl.setSubTotal(objGroupCodeDtl.getSubTotal() + rsGroupWiseSales.getDouble(4));
                        objGroupCodeDtl.setSalesAmt(objGroupCodeDtl.getSalesAmt() + rsGroupWiseSales.getDouble(8));
                        objGroupCodeDtl.setDiscAmt(objGroupCodeDtl.getDiscAmt() + rsGroupWiseSales.getDouble(9));
                        objGroupCodeDtl.setGrandTotal(objGroupCodeDtl.getGrandTotal() + rsGroupWiseSales.getDouble(11));
                    }
                    else
                    {
                        Map<String, clsGroupSubGroupWiseSales> mapGroupCodeDtl = new LinkedHashMap<>();
                        clsGroupSubGroupWiseSales objGroupCodeDtl = new clsGroupSubGroupWiseSales(
                                rsGroupWiseSales.getString(1), rsGroupWiseSales.getString(2), rsGroupWiseSales.getString(5), rsGroupWiseSales.getDouble(3), rsGroupWiseSales.getDouble(4), rsGroupWiseSales.getDouble(8), rsGroupWiseSales.getDouble(9), rsGroupWiseSales.getDouble(11));
                        mapGroupCodeDtl.put(rsGroupWiseSales.getString(1), objGroupCodeDtl);
                        listOfGroup.add(mapGroupCodeDtl);
                    }
                }
                else
                {
                    List<Map<String, clsGroupSubGroupWiseSales>> listOfGroupDtl = new ArrayList<>();
                    Map<String, clsGroupSubGroupWiseSales> mapGroupCodeDtl = new LinkedHashMap<>();
                    clsGroupSubGroupWiseSales objGroupCodeDtl = new clsGroupSubGroupWiseSales(
                            rsGroupWiseSales.getString(1), rsGroupWiseSales.getString(2), rsGroupWiseSales.getString(5), rsGroupWiseSales.getDouble(3), rsGroupWiseSales.getDouble(4), rsGroupWiseSales.getDouble(8), rsGroupWiseSales.getDouble(9), rsGroupWiseSales.getDouble(11));
                    mapGroupCodeDtl.put(rsGroupWiseSales.getString(1), objGroupCodeDtl);
                    listOfGroupDtl.add(mapGroupCodeDtl);
                    mapPOSDtlForGroupSubGroup.put(rsGroupWiseSales.getString(10), listOfGroupDtl);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void funDeliveryBoyWiseCashTakenReportForJasper()
    {
        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();

            String dpName = hm.get("DPName").toString();
            String dpCode = hm.get("DPCode").toString();

            Map<String, List<clsBillDtl>> mapDeliBoyWiseBillDtl = new HashMap<>();

            StringBuilder sqlBuilder = new StringBuilder();

            //Q Data
            sqlBuilder.setLength(0);
            sqlBuilder.append("select a.strBillNo,DATE_FORMAT(date(a.dteBillDate),'%d-%m-%Y')dteBillDate,c.strDPCode,d.strDPName "
                    + ",a.dblGrandTotal,b.dblLooseCashAmt,a.strUserCreated,e.strCustomerCode,e.strCustomerName,ifnull(e.strBuildingName,'') strBuildingName "
                    + "from tblqbillhd a,tblhomedelivery b,tblhomedeldtl c,tbldeliverypersonmaster d,tblcustomermaster e "
                    + "where a.strBillNo=b.strBillNo "
                    + "and b.strBillNo=c.strBillNo "
                    + "and a.strCustomerCode=e.strCustomerCode "
                    + "and c.strDPCode=d.strDPCode "
                    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");

            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and a.strPOSCode='" + posCode + "' ");
            }
            if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
            {
                sqlBuilder.append("and a.intShiftCode='" + shiftNo + "' ");
            }
            if (!dpCode.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and d.strDPCode='" + dpCode + "' ");
            }

            ResultSet rsWaiterWiseItemSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
            while (rsWaiterWiseItemSales.next())
            {
                String billNo = rsWaiterWiseItemSales.getString(1);//billNo
                String deliveryBoyName = rsWaiterWiseItemSales.getString(4);//delivery boy name               

                if (mapDeliBoyWiseBillDtl.containsKey(billNo))
                {
                    List<clsBillDtl> listOfDelBoyWiseCashTaken = mapDeliBoyWiseBillDtl.get(billNo);

                    clsBillDtl obj = new clsBillDtl();

                    obj.setStrBillNo(billNo);
                    obj.setDteBillDate(rsWaiterWiseItemSales.getString(2));
                    obj.setStrDelBoyCode(rsWaiterWiseItemSales.getString(3));
                    obj.setStrDelBoyName(rsWaiterWiseItemSales.getString(4));
                    obj.setDblAmount(0.00);
                    obj.setDblCashTakenAmt(rsWaiterWiseItemSales.getDouble(6));
                    obj.setStrUserCreated(rsWaiterWiseItemSales.getString(7));
                    obj.setStrCustomerCode(rsWaiterWiseItemSales.getString(8));
                    obj.setStrCustomerName(rsWaiterWiseItemSales.getString(9));
                    obj.setStrArea(rsWaiterWiseItemSales.getString(10));

                    listOfDelBoyWiseCashTaken.add(obj);
                }
                else
                {
                    List<clsBillDtl> listOfDelBoyWiseCashTaken = new ArrayList<>();

                    clsBillDtl obj = new clsBillDtl();

                    obj.setStrBillNo(billNo);
                    obj.setDteBillDate(rsWaiterWiseItemSales.getString(2));
                    obj.setStrDelBoyCode(rsWaiterWiseItemSales.getString(3));
                    obj.setStrDelBoyName(rsWaiterWiseItemSales.getString(4));
                    obj.setDblAmount(rsWaiterWiseItemSales.getDouble(5));
                    obj.setDblCashTakenAmt(rsWaiterWiseItemSales.getDouble(6));
                    obj.setStrUserCreated(rsWaiterWiseItemSales.getString(7));
                    obj.setStrCustomerCode(rsWaiterWiseItemSales.getString(8));
                    obj.setStrCustomerName(rsWaiterWiseItemSales.getString(9));
                    obj.setStrArea(rsWaiterWiseItemSales.getString(10));

                    listOfDelBoyWiseCashTaken.add(obj);

                    mapDeliBoyWiseBillDtl.put(billNo, listOfDelBoyWiseCashTaken);
                }
            }
            rsWaiterWiseItemSales.close();

            //Live Data
            sqlBuilder.setLength(0);
            sqlBuilder.append("select a.strBillNo,DATE_FORMAT(date(a.dteBillDate),'%d-%m-%Y')dteBillDate,c.strDPCode,d.strDPName "
                    + ",a.dblGrandTotal,b.dblLooseCashAmt,a.strUserCreated,e.strCustomerCode,e.strCustomerName,ifnull(e.strBuildingName,'') strBuildingName "
                    + "from tblbillhd a,tblhomedelivery b,tblhomedeldtl c,tbldeliverypersonmaster d,tblcustomermaster e "
                    + "where a.strBillNo=b.strBillNo "
                    + "and b.strBillNo=c.strBillNo "
                    + "and a.strCustomerCode=e.strCustomerCode "
                    + "and c.strDPCode=d.strDPCode "
                    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");

            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and a.strPOSCode='" + posCode + "' ");
            }
            if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
            {
                sqlBuilder.append("and a.intShiftCode='" + shiftNo + "' ");
            }
            if (!dpCode.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and d.strDPCode='" + dpCode + "' ");
            }

            rsWaiterWiseItemSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
            while (rsWaiterWiseItemSales.next())
            {
                String billNo = rsWaiterWiseItemSales.getString(1);//billNo
                String deliveryBoyName = rsWaiterWiseItemSales.getString(4);//delivery boy name               

                if (mapDeliBoyWiseBillDtl.containsKey(billNo))
                {
                    List<clsBillDtl> listOfDelBoyWiseCashTaken = mapDeliBoyWiseBillDtl.get(billNo);

                    clsBillDtl obj = new clsBillDtl();

                    obj.setStrBillNo(billNo);
                    obj.setDteBillDate(rsWaiterWiseItemSales.getString(2));
                    obj.setStrDelBoyCode(rsWaiterWiseItemSales.getString(3));
                    obj.setStrDelBoyName(rsWaiterWiseItemSales.getString(4));
                    obj.setDblAmount(0.00);
                    obj.setDblCashTakenAmt(rsWaiterWiseItemSales.getDouble(6));
                    obj.setStrUserCreated(rsWaiterWiseItemSales.getString(7));
                    obj.setStrCustomerCode(rsWaiterWiseItemSales.getString(8));
                    obj.setStrCustomerName(rsWaiterWiseItemSales.getString(9));
                    obj.setStrArea(rsWaiterWiseItemSales.getString(10));

                    listOfDelBoyWiseCashTaken.add(obj);
                }
                else
                {
                    List<clsBillDtl> listOfDelBoyWiseCashTaken = new ArrayList<>();

                    clsBillDtl obj = new clsBillDtl();

                    obj.setStrBillNo(billNo);
                    obj.setDteBillDate(rsWaiterWiseItemSales.getString(2));
                    obj.setStrDelBoyCode(rsWaiterWiseItemSales.getString(3));
                    obj.setStrDelBoyName(rsWaiterWiseItemSales.getString(4));
                    obj.setDblAmount(rsWaiterWiseItemSales.getDouble(5));
                    obj.setDblCashTakenAmt(rsWaiterWiseItemSales.getDouble(6));
                    obj.setStrUserCreated(rsWaiterWiseItemSales.getString(7));
                    obj.setStrCustomerCode(rsWaiterWiseItemSales.getString(8));
                    obj.setStrCustomerName(rsWaiterWiseItemSales.getString(9));
                    obj.setStrArea(rsWaiterWiseItemSales.getString(10));

                    listOfDelBoyWiseCashTaken.add(obj);

                    mapDeliBoyWiseBillDtl.put(billNo, listOfDelBoyWiseCashTaken);
                }
            }
            rsWaiterWiseItemSales.close();

            Comparator<clsBillDtl> delBoyCodeComparator = new Comparator<clsBillDtl>()
            {

                @Override
                public int compare(clsBillDtl o1, clsBillDtl o2)
                {
                    return o1.getStrDelBoyName().compareTo(o2.getStrDelBoyName());
                }
            };

            List<clsBillDtl> listOfDelBoyWiseCashTaken = new LinkedList();

            for (List<clsBillDtl> listOfDelBoyWiseCashTakenTemp : mapDeliBoyWiseBillDtl.values())
            {
                listOfDelBoyWiseCashTaken.addAll(listOfDelBoyWiseCashTakenTemp);
            }

            Collections.sort(listOfDelBoyWiseCashTaken, new clsWaiterWiseSalesComparator(delBoyCodeComparator));
            //call for view report
            funViewJasperReportForBeanCollectionDataSource(is, hm, listOfDelBoyWiseCashTaken);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void funCreaditBillReportForJasper()
    {

        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();

            List<clsBillDtl> listOfCreditBillReport = new ArrayList<clsBillDtl>();

            String sbSqlFilters = "", sbSqlFilters1 = "";

            String sbSqlLive = "SELECT a.strPOSCode,a.strCustomerCode,d.strCustomerName,a.strBillNo,DATE_FORMAT(date(a.dteBillDate),'%d-%m-%Y')dteBillDate ,a.strClientCode, SUM(b.dblSettlementAmt)"
                    + " FROM tblbillhd a,tblbillsettlementdtl b,tblsettelmenthd c,tblcustomermaster d"
                    + " WHERE a.strBillNo=b.strBillNo "
                    + " AND b.strSettlementCode=c.strSettelmentCode "
                    + " and date(a.dtebilldate)=date(b.dtebilldate) "
                    + " and a.strClientCode=b.strClientCode "
                    + " AND c.strSettelmentType='Credit' "
                    + " and a.strCustomerCode=d.strCustomerCode "
                    + " and date( a.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' ";

            String sbSqlQFile = "SELECT a.strPOSCode,a.strCustomerCode,d.strCustomerName,a.strBillNo,DATE_FORMAT(date(a.dteBillDate),'%d-%m-%Y')dteBillDate ,a.strClientCode, SUM(b.dblSettlementAmt)"
                    + " FROM tblqbillhd a,tblqbillsettlementdtl b,tblsettelmenthd c,tblcustomermaster d"
                    + " WHERE a.strBillNo=b.strBillNo "
                    + " AND b.strSettlementCode=c.strSettelmentCode "
                    + " and date(a.dtebilldate)=date(b.dtebilldate) "
                    + " and a.strClientCode=b.strClientCode "
                    + " AND c.strSettelmentType='Credit' "
                    + " and a.strCustomerCode=d.strCustomerCode"
                    + " and date( a.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' ";

            if (!posCode.equals("All"))
            {
                sbSqlFilters1 = " AND a.strPOSCode = '" + posCode + "' ";
            }
            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sbSqlFilters1 = " and a.intShiftCode = '" + shiftNo + "' ";
                }
            }
            sbSqlFilters1 = " GROUP BY a.strCustomerCode,a.strBillNo ";

            String sqlModLive = "SELECT a.strPOSCode,b.strBillNo,b.strReceiptNo,b.dteReceiptDate,b.strSettlementName, SUM(b.dblReceiptAmt),b.strChequeNo,b.strBankName,b.strRemarks,c.strCustomerName,a.strCustomerCode,a.strClientCode"
                    + " from tblbillhd a,tblqcreditbillreceipthd b,tblcustomermaster c "
                    + " where a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " and a.strClientCode=b.strClientCode "
                    + " AND a.strCustomerCode=c.strCustomerCode "
                    + " and date( a.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "'";

            String sqlModQFile = "SELECT a.strPOSCode,b.strBillNo,b.strReceiptNo,b.dteReceiptDate,b.strSettlementName, SUM(b.dblReceiptAmt),b.strChequeNo,b.strBankName,b.strRemarks,c.strCustomerName,a.strCustomerCode,a.strClientCode"
                    + " from tblqbillhd a,tblqcreditbillreceipthd b,tblcustomermaster c  "
                    + " where a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " and a.strClientCode=b.strClientCode "
                    + " AND a.strCustomerCode=c.strCustomerCode "
                    + " and date( a.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "'";

            if (!posCode.equals("All"))
            {
                sbSqlFilters = " AND a.strPOSCode = '" + posCode + "' ";
            }
            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sbSqlFilters = " and a.intShiftCode = '" + shiftNo + "' ";
                }
            }
            sbSqlFilters = " group by b.strBillNo,b.strReceiptNo ";

            sbSqlLive += " " + sbSqlFilters1;
            sbSqlQFile += " " + sbSqlFilters1;
            sqlModLive += " " + sbSqlFilters;
            sqlModQFile += " " + sbSqlFilters;
            double balanceAmt = 0.00, totalAmt = 0.00;
            ResultSet rsLiveData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive);

            while (rsLiveData.next())
            {
                clsBillDtl objBean = new clsBillDtl();
                objBean.setStrCustomerName(rsLiveData.getString(3));   //Customer Name
                objBean.setStrCustomerCode(rsLiveData.getString(2));
                objBean.setStrReceiptNo("");  //Receipt No
                objBean.setStrBillNo(rsLiveData.getString(4));  //Bill No
                objBean.setDteBillDate(rsLiveData.getString(5));   //Bill Date
                objBean.setDblBillAmt(rsLiveData.getDouble(7));
                totalAmt = rsLiveData.getDouble(7);
                balanceAmt = totalAmt;
                objBean.setDblBalanceAmt(0.00);

                listOfCreditBillReport.add(objBean);

            }
            rsLiveData.close();

            ResultSet rsQfileData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFile);
            while (rsQfileData.next())
            {
                clsBillDtl objBean = new clsBillDtl();
                objBean.setStrCustomerName(rsQfileData.getString(3));   //Customer Name
                objBean.setStrCustomerCode(rsQfileData.getString(2));
                objBean.setStrBillNo(rsQfileData.getString(4));  //Bill No
                objBean.setDteBillDate(rsQfileData.getString(5));   //Bill Date
                objBean.setDblBillAmt(rsQfileData.getDouble(7));
                totalAmt = rsQfileData.getDouble(7);
                balanceAmt = totalAmt;
                objBean.setDblBalanceAmt(0.00);
                objBean.setStrReceiptNo("");  //Receipt No

                listOfCreditBillReport.add(objBean);

            }
            rsQfileData.close();

            double receiptAmt = 0.00;
            ResultSet rsLiveModData = clsGlobalVarClass.dbMysql.executeResultSet(sqlModLive);

            while (rsLiveModData.next())
            {
                clsBillDtl objBean = new clsBillDtl();
                objBean.setStrBillNo(rsLiveModData.getString(2));   //Bill No
                objBean.setStrReceiptNo(rsLiveModData.getString(3));  //Receipt No
                objBean.setDteReceiptDate(rsLiveModData.getString(4));
                objBean.setStrCustomerName(rsLiveModData.getString(10));
                objBean.setStrCustomerCode(rsLiveModData.getString(11));
                objBean.setDblAmount(rsLiveModData.getDouble(6));   //Receipt Amount
                objBean.setStrSettlementName(rsLiveModData.getString(5));
                objBean.setStrChequeNo(rsLiveModData.getString(7));
                objBean.setStrBankName(rsLiveModData.getString(8));
                objBean.setStrRemark(rsLiveModData.getString(9));
                receiptAmt = rsLiveModData.getDouble(6);
                balanceAmt = balanceAmt - receiptAmt;
                objBean.setDblBalanceAmt(balanceAmt);
                if (!rsLiveModData.getString(3).equalsIgnoreCase(""))
                {
                    listOfCreditBillReport.add(objBean);
                }
            }
            rsLiveModData.close();

            ResultSet rsQfileModData = clsGlobalVarClass.dbMysql.executeResultSet(sqlModQFile);
            while (rsQfileModData.next())
            {
                clsBillDtl objBean = new clsBillDtl();
                objBean.setStrBillNo(rsQfileModData.getString(2));   //Bill No
                objBean.setStrReceiptNo(rsQfileModData.getString(3));  //Receipt No
                objBean.setDteReceiptDate(rsQfileModData.getString(4));
                objBean.setStrCustomerName(rsQfileModData.getString(10));
                objBean.setStrCustomerCode(rsQfileModData.getString(11));
                objBean.setDblAmount(rsQfileModData.getDouble(6));   //Receipt Amount
                objBean.setStrSettlementName(rsQfileModData.getString(5));
                objBean.setStrChequeNo(rsQfileModData.getString(7));
                objBean.setStrBankName(rsQfileModData.getString(8));
                objBean.setStrRemark(rsQfileModData.getString(9));
                receiptAmt = rsQfileModData.getDouble(6);
                balanceAmt = balanceAmt - receiptAmt;
                objBean.setDblBalanceAmt(balanceAmt);
                if (!rsQfileModData.getString(3).equalsIgnoreCase(""))
                {
                    listOfCreditBillReport.add(objBean);
                }
            }
            rsQfileModData.close();

            Comparator<clsBillDtl> customerComparator = new Comparator<clsBillDtl>()
            {

                @Override
                public int compare(clsBillDtl o1, clsBillDtl o2)
                {
                    return o1.getStrCustomerCode().compareToIgnoreCase(o2.getStrCustomerCode());
                }
            };
            Comparator<clsBillDtl> billComparator = new Comparator<clsBillDtl>()
            {

                @Override
                public int compare(clsBillDtl o1, clsBillDtl o2)
                {
                    return o1.getStrBillNo().compareToIgnoreCase(o2.getStrBillNo());
                }
            };

            Comparator<clsBillDtl> receiptComparator = new Comparator<clsBillDtl>()
            {

                @Override
                public int compare(clsBillDtl o1, clsBillDtl o2)
                {
                    return o1.getStrReceiptNo().compareToIgnoreCase(o2.getStrReceiptNo());
                }
            };

            Collections.sort(listOfCreditBillReport, new clsCreditBillReportComparator(
                    customerComparator,
                    billComparator,
                    receiptComparator)
            );

            //call for view report
            funViewJasperReportForBeanCollectionDataSource(is, hm, listOfCreditBillReport);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
