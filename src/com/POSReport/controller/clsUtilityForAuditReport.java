/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.POSReport.controller;

import com.POSReport.controller.comparator.clsOperatorComparator;
import com.POSReport.controller.comparator.clsCounterComparator;
import com.POSReport.controller.comparator.clsWaiterWiseSalesComparator;
import com.POSGlobal.controller.clsAdvOrderItemDtl;
import com.POSGlobal.controller.clsBillDtl;
import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsItemDtlForTax;
import com.POSGlobal.controller.clsOperatorDtl;
import com.POSGlobal.controller.clsPlaceOrderDtl;
import com.POSGlobal.controller.clsSettelementOptions;
import com.POSGlobal.controller.clsTaxCalculationDtls;
import com.POSGlobal.controller.clsVoidBillDtl;
import java.awt.Dimension;
import java.io.InputStream;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.swing.JRViewer;

public class clsUtilityForAuditReport
{

    private String reportName;
    private HashMap hm;

    public clsUtilityForAuditReport()
    {
    }

    public clsUtilityForAuditReport(String reportName, HashMap hm)
    {
        this.reportName = reportName;
        this.hm = hm;
    }

    public void funGuestCreditReportForJasper()
    {
        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();

            StringBuilder sqlLiveBuilder = new StringBuilder();
            StringBuilder sqlQBuilder = new StringBuilder();

            sqlLiveBuilder.append("select a.strBillNo,a.strItemCode,a.strItemName,a.dblRate,a.dblQuantity,a.dblAmount,date(a.dteBillDate) "
                    + ",h.strPosName,d.strSettelmentType,a.strKOTNo,b.strPOSCode,b.strRemarks,ifnull(e.strTableName,'') as strTableName"
                    + ",f.strCustomerName,ifnull(g.strWShortName,'') as strWShortName "
                    + "from tblbilldtl a "
                    + "left outer join tblbillhd b on a.strBillNo=b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate) "
                    + "left outer join tblbillsettlementdtl c on a.strBillNo=c.strBillNo and date(a.dteBillDate)=date(c.dteBillDate) "
                    + "left outer join tblsettelmenthd d on c.strSettlementCode=d.strSettelmentCode "
                    + "left outer join tbltablemaster e on b.strTableNo=e.strTableNo "
                    + "left outer join tblcustomermaster f on c.strCustomerCode=f.strCustomerCode "
                    + "left outer join tblwaitermaster g on b.strWaiterNo=g.strWaiterNo "
                    + "left outer join tblposmaster h on b.strPOSCode=h.strPosCode "
                    + "where date(a.dteBillDate) Between '" + fromDate + "' and '" + toDate + "'  "
                    + "and d.strSettelmentType='Credit' ");

            sqlQBuilder.append("select a.strBillNo,a.strItemCode,a.strItemName,a.dblRate,a.dblQuantity,a.dblAmount,date(a.dteBillDate) "
                    + ",h.strPosName,d.strSettelmentType,a.strKOTNo,b.strPOSCode,b.strRemarks,ifnull(e.strTableName,'') as strTableName"
                    + ",f.strCustomerName,ifnull(g.strWShortName,'') as strWShortName "
                    + "from tblqbilldtl a "
                    + "left outer join tblqbillhd b on a.strBillNo=b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate) "
                    + "left outer join tblqbillsettlementdtl c on a.strBillNo=c.strBillNo  and date(a.dteBillDate)=date(c.dteBillDate) "
                    + "left outer join tblsettelmenthd d on c.strSettlementCode=d.strSettelmentCode "
                    + "left outer join tbltablemaster e on b.strTableNo=e.strTableNo "
                    + "left outer join tblcustomermaster f on c.strCustomerCode=f.strCustomerCode "
                    + "left outer join tblwaitermaster g on b.strWaiterNo=g.strWaiterNo "
                    + "left outer join tblposmaster h on b.strPOSCode=h.strPosCode "
                    + "where date(a.dteBillDate) Between '" + fromDate + "' and '" + toDate + "'  "
                    + "and d.strSettelmentType='Credit' ");

            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlLiveBuilder.append("and b.strPOSCode='" + posCode + "' ");
                sqlQBuilder.append("and b.strPOSCode='" + posCode + "' ");
            }

            sqlLiveBuilder.append("group by b.strPOSCode,a.strBillNo,a.strKOTNo,a.strItemCode "
                    + "order by b.strPOSCode,a.strBillNo,a.strKOTNo,a.strItemCode ");
            sqlQBuilder.append("group by b.strPOSCode,a.strBillNo,a.strKOTNo,a.strItemCode "
                    + "order by b.strPOSCode,a.strBillNo,a.strKOTNo,a.strItemCode ");

            List<clsBillDtl> listOfGuestCreditData = new ArrayList<>();

            //live
            ResultSet rsData = clsGlobalVarClass.dbMysql.executeResultSet(sqlLiveBuilder.toString());
            while (rsData.next())
            {
                clsBillDtl obj = new clsBillDtl();

                obj.setStrBillNo(rsData.getString(1));
                obj.setStrItemCode(rsData.getString(2));
                obj.setStrItemName(rsData.getString(3));
                obj.setDblRate(rsData.getDouble(4));
                obj.setDblQuantity(rsData.getDouble(5));
                obj.setDblAmount(rsData.getDouble(6));
                obj.setDteBillDate(rsData.getString(7));
                obj.setStrPosName(rsData.getString(8));
                // obj.setStrSettlementType(rsData.getString(9));
                obj.setStrKOTNo(rsData.getString(10));
                obj.setStrPOSCode(rsData.getString(11));
                obj.setStrRemarks(rsData.getString(12));
                obj.setStrTableName(rsData.getString(13));
                obj.setStrCustomerName(rsData.getString(14));
                obj.setStrWShortName(rsData.getString(15));

                listOfGuestCreditData.add(obj);
            }
            rsData.close();;
            //Q
            rsData = clsGlobalVarClass.dbMysql.executeResultSet(sqlQBuilder.toString());
            while (rsData.next())
            {
                clsBillDtl obj = new clsBillDtl();

                obj.setStrBillNo(rsData.getString(1));
                obj.setStrItemCode(rsData.getString(2));
                obj.setStrItemName(rsData.getString(3));
                obj.setDblRate(rsData.getDouble(4));
                obj.setDblQuantity(rsData.getDouble(5));
                obj.setDblAmount(rsData.getDouble(6));
                obj.setDteBillDate(rsData.getString(7));
                obj.setStrPosName(rsData.getString(8));
                // obj.setStrSettlementType(rsData.getString(9));
                obj.setStrKOTNo(rsData.getString(10));
                obj.setStrPOSCode(rsData.getString(11));
                obj.setStrRemarks(rsData.getString(12));
                obj.setStrTableName(rsData.getString(13));
                obj.setStrCustomerName(rsData.getString(14));
                obj.setStrWShortName(rsData.getString(15));

                listOfGuestCreditData.add(obj);
            }
            rsData.close();
            //call for view report
            funViewJasperReportForBeanCollectionDataSource(is, hm, listOfGuestCreditData);
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
            if (pages.size()==0)
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

    public void funCounterWiseMenuHeadWiseJasperReport()
    {

        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();

            StringBuilder sbSqlLive = new StringBuilder();
            StringBuilder sbSqlQfile = new StringBuilder();
            StringBuilder sbFilter = new StringBuilder();

            sbSqlLive.setLength(0);
            sbSqlQfile.setLength(0);
            sbFilter.setLength(0);
            sbSqlLive.append(" select ifnull(b.strCounterCode,'NA') as strCounterCode ,"
                    + " ifnull(d.strCounterName,'NA') as   strCounterName ,ifNull(e.strMenuCode,'NA') as strMenuCode,"
                    + " ifnull(e.strMenuName,'NA') as strMenuName,  b.dblRate,sum(b.dblquantity) as dblquantity ,"
                    + " sum(b.dblamount) as dblamount"
                    + " from tblbillhd a ,tblbilldtl b ,tblmenuitempricingdtl c ,tblcounterhd d,tblmenuhd e "
                    + " where a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " and b.stritemcode = c.strItemCode "
                    + " and b.strCounterCode=d.strCounterCode "
                    + " and a.strPOSCode = c.strPosCode "
                    + " and c.strMenuCode=e.strMenuCode and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");

            sbSqlQfile.append(" select ifnull(b.strCounterCode,'NA') as strCounterCode ,"
                    + " ifnull(d.strCounterName,'NA') as   strCounterName ,ifNull(e.strMenuCode,'NA') as strMenuCode,"
                    + " ifnull(e.strMenuName,'NA') as strMenuName,  b.dblRate,sum(b.dblquantity) as dblquantity ,"
                    + " sum(b.dblamount) as dblamount"
                    + " from tblqbillhd a ,tblqbilldtl b ,tblmenuitempricingdtl c ,tblcounterhd d,tblmenuhd e "
                    + " where a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " and b.stritemcode = c.strItemCode "
                    + " and b.strCounterCode=d.strCounterCode "
                    + " and a.strPOSCode = c.strPosCode "
                    + " and c.strMenuCode=e.strMenuCode and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");

            if (!posCode.equals("All"))
            {
                sbFilter.append(" AND a.strPoscode = '" + posCode + "' ");
            }
            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sbFilter.append(" and a.intShiftCode = '" + shiftNo + "' ");
                }
            }
            sbFilter.append("  and a.strAdvBookingNo ='' "
                    + " group by b.strCounterCode,d.strCounterName, e.strMenuCode,e.strMenuName "
                    + " order by d.strCounterName,e.strMenuName  ");
            sbSqlLive.append(sbFilter);
            sbSqlQfile.append(sbFilter);

            List<clsCounterDtlBean> listOfCounterWiseMenuHeadWiseData = new ArrayList<>();

            //live data
            ResultSet rsCounterData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
            while (rsCounterData.next())
            {
                clsCounterDtlBean obj = new clsCounterDtlBean();

                obj.setStrCounterCode(rsCounterData.getString(1));
                obj.setStrCounterName(rsCounterData.getString(2));
                obj.setStrMenuCode(rsCounterData.getString(3));
                obj.setStrMenuName(rsCounterData.getString(4));
                obj.setDblRate(rsCounterData.getDouble(5));
                obj.setDblQuantity(rsCounterData.getDouble(6));
                obj.setDblAmount(rsCounterData.getDouble(7));

                listOfCounterWiseMenuHeadWiseData.add(obj);
            }
            rsCounterData.close();
            //QData
            rsCounterData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQfile.toString());
            while (rsCounterData.next())
            {
                clsCounterDtlBean obj = new clsCounterDtlBean();

                obj.setStrCounterCode(rsCounterData.getString(1));
                obj.setStrCounterName(rsCounterData.getString(2));
                obj.setStrMenuCode(rsCounterData.getString(3));
                obj.setStrMenuName(rsCounterData.getString(4));
                obj.setDblRate(rsCounterData.getDouble(5));
                obj.setDblQuantity(rsCounterData.getDouble(6));
                obj.setDblAmount(rsCounterData.getDouble(7));

                listOfCounterWiseMenuHeadWiseData.add(obj);
            }
            rsCounterData.close();

            Comparator<clsCounterDtlBean> counterCodeComparator = new Comparator<clsCounterDtlBean>()
            {

                @Override
                public int compare(clsCounterDtlBean o1, clsCounterDtlBean o2)
                {
                    return o1.getStrCounterCode().compareTo(o2.getStrCounterCode());
                }
            };
            Comparator<clsCounterDtlBean> menuCodeComparator = new Comparator<clsCounterDtlBean>()
            {

                @Override
                public int compare(clsCounterDtlBean o1, clsCounterDtlBean o2)
                {
                    return o1.getStrMenuCode().compareTo(o2.getStrMenuCode());
                }
            };

            Collections.sort(listOfCounterWiseMenuHeadWiseData, new clsCounterComparator(counterCodeComparator, menuCodeComparator));

            //call for view report
            funViewJasperReportForBeanCollectionDataSource(is, hm, listOfCounterWiseMenuHeadWiseData);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void funCounterWiseGroupWiseJasperReport()
    {
        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();

            StringBuilder sbSqlLive = new StringBuilder();
            StringBuilder sbSqlQfile = new StringBuilder();
            StringBuilder sbFilter = new StringBuilder();

            sbSqlLive.setLength(0);
            sbSqlQfile.setLength(0);
            sbFilter.setLength(0);

            sbSqlLive.setLength(0);
            sbSqlQfile.setLength(0);
            sbFilter.setLength(0);

            sbSqlLive.append(" select ifnull(d.strCounterCode,'NA') as strCounterCode ,ifnull(d.strCounterName,'NA') as strCounterName , "
                    + " ifNull(h.strGroupCode,'NA') as strGroupCode,ifnull(h.strGroupName,'NA') as strGroupName,b.dblRate,sum(b.dblquantity) as dblquantity ,sum(b.dblamount) as dblamount"
                    + " from tblbillhd a ,tblbilldtl b,tblcounterhd d, tblitemmaster f,tblsubgrouphd g,tblgrouphd h "
                    + " where a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " and b.strCounterCode=d.strCounterCode "
                    + " and b.stritemcode=f.strItemCode "
                    + " and f.strSubGroupCode=g.strSubGroupCode"
                    + " and g.strGroupCode=h.strGroupCode "
                    + " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "'  ");

            sbSqlQfile.append(" select ifnull(d.strCounterCode,'NA') as strCounterCode ,ifnull(d.strCounterName,'NA') as strCounterName , "
                    + " ifNull(h.strGroupCode,'NA') as strGroupCode,ifnull(h.strGroupName,'NA') as strGroupName,b.dblRate,sum(b.dblquantity) as dblquantity ,sum(b.dblamount) as dblamount"
                    + " from tblqbillhd a ,tblqbilldtl b,tblcounterhd d, tblitemmaster f,tblsubgrouphd g,tblgrouphd h "
                    + " where a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " and b.strCounterCode=d.strCounterCode "
                    + " and b.stritemcode=f.strItemCode "
                    + " and f.strSubGroupCode=g.strSubGroupCode"
                    + " and g.strGroupCode=h.strGroupCode "
                    + " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "'  ");

            if (!posCode.equals("All"))
            {
                sbFilter.append(" AND a.strPoscode = '" + posCode + "' ");
            }
            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sbFilter.append(" and a.intShiftCode = '" + shiftNo + "' ");
                }
            }
            sbFilter.append(" and a.strAdvBookingNo ='' "
                    + " group by d.strCounterCode,d.strCounterName, h.strGroupName,g.strSubGroupCode "
                    + " order by d.strCounterName,h.strGroupName  ");

            sbSqlLive.append(sbFilter);
            sbSqlQfile.append(sbFilter);

            List<clsCounterDtlBean> listOfCounterWiseMenuHeadWiseData = new ArrayList<>();

            //live data
            ResultSet rsCounterData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
            while (rsCounterData.next())
            {
                clsCounterDtlBean obj = new clsCounterDtlBean();

                obj.setStrCounterCode(rsCounterData.getString(1));
                obj.setStrCounterName(rsCounterData.getString(2));
                obj.setStrGroupCode(rsCounterData.getString(3));
                obj.setStrGroupName(rsCounterData.getString(4));
                obj.setDblRate(rsCounterData.getDouble(5));
                obj.setDblQuantity(rsCounterData.getDouble(6));
                obj.setDblAmount(rsCounterData.getDouble(7));

                listOfCounterWiseMenuHeadWiseData.add(obj);
            }
            rsCounterData.close();
            //QData
            rsCounterData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQfile.toString());
            while (rsCounterData.next())
            {
                clsCounterDtlBean obj = new clsCounterDtlBean();

                obj.setStrCounterCode(rsCounterData.getString(1));
                obj.setStrCounterName(rsCounterData.getString(2));
                obj.setStrGroupCode(rsCounterData.getString(3));
                obj.setStrGroupName(rsCounterData.getString(4));
                obj.setDblRate(rsCounterData.getDouble(5));
                obj.setDblQuantity(rsCounterData.getDouble(6));
                obj.setDblAmount(rsCounterData.getDouble(7));

                listOfCounterWiseMenuHeadWiseData.add(obj);
            }
            rsCounterData.close();

            Comparator<clsCounterDtlBean> counterCodeComparator = new Comparator<clsCounterDtlBean>()
            {

                @Override
                public int compare(clsCounterDtlBean o1, clsCounterDtlBean o2)
                {
                    return o1.getStrCounterCode().compareTo(o2.getStrCounterCode());
                }
            };
            Comparator<clsCounterDtlBean> groupCodeComparator = new Comparator<clsCounterDtlBean>()
            {

                @Override
                public int compare(clsCounterDtlBean o1, clsCounterDtlBean o2)
                {
                    return o1.getStrGroupCode().compareToIgnoreCase(o2.getStrGroupCode());
                }
            };
            Comparator<clsCounterDtlBean> groupNameComparator = new Comparator<clsCounterDtlBean>()
            {

                @Override
                public int compare(clsCounterDtlBean o1, clsCounterDtlBean o2)
                {
                    return o1.getStrGroupName().compareToIgnoreCase(o2.getStrGroupName());
                }
            };

            Collections.sort(listOfCounterWiseMenuHeadWiseData, new clsCounterComparator(counterCodeComparator, groupCodeComparator, groupNameComparator));

            //call for view report
            funViewJasperReportForBeanCollectionDataSource(is, hm, listOfCounterWiseMenuHeadWiseData);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void funCounterWiseSubGroupWiseJasperReport()
    {

        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();

            StringBuilder sbSqlLive = new StringBuilder();
            StringBuilder sbSqlQfile = new StringBuilder();
            StringBuilder sbFilter = new StringBuilder();

            sbSqlLive.setLength(0);
            sbSqlQfile.setLength(0);
            sbFilter.setLength(0);

            sbSqlLive.setLength(0);
            sbSqlQfile.setLength(0);
            sbFilter.setLength(0);

            sbSqlLive.append(" select ifnull(d.strCounterCode,'NA') as strCounterCode ,ifnull(d.strCounterName,'NA') as strCounterName , "
                    + " e.strSubGroupCode,e.strSubGroupName, "
                    + " b.dblRate,sum(b.dblquantity) as dblquantity ,sum(b.dblamount) as dblamount"
                    + " from tblbillhd a ,tblbilldtl b,tblitemmaster c ,tblcounterhd d ,tblsubgrouphd e "
                    + " where a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " and b.strCounterCode=d.strCounterCode "
                    + " and b.stritemcode=c.strItemCode "
                    + " and c.strSubGroupCode=e.strSubGroupCode "
                    + " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "'  ");

            sbSqlQfile.append(" select ifnull(d.strCounterCode,'NA') as strCounterCode ,ifnull(d.strCounterName,'NA') as strCounterName , "
                    + " e.strSubGroupCode,e.strSubGroupName, "
                    + " b.dblRate,sum(b.dblquantity) as dblquantity ,sum(b.dblamount) as dblamount"
                    + " from tblqbillhd a ,tblqbilldtl b,tblitemmaster c ,tblcounterhd d ,tblsubgrouphd e "
                    + " where a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " and b.strCounterCode=d.strCounterCode "
                    + " and b.stritemcode=c.strItemCode "
                    + " and c.strSubGroupCode=e.strSubGroupCode "
                    + " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "'  ");

            if (!posCode.equals("All"))
            {
                sbFilter.append(" AND a.strPoscode = '" + posCode + "' ");
            }
            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sbFilter.append(" and a.intShiftCode = '" + shiftNo + "' ");
                }
            }
            sbFilter.append(" and a.strAdvBookingNo ='' "
                    + " group by d.strCounterCode,d.strCounterName, e.strSubGroupCode,e.strSubGroupName "
                    + " order by d.strCounterName,e.strSubGroupName   ");
            sbSqlLive.append(sbFilter);
            sbSqlQfile.append(sbFilter);

            List<clsCounterDtlBean> listOfSubGroupWiseCounterData = new ArrayList<>();

            //live data
            ResultSet rsCounterData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
            while (rsCounterData.next())
            {
                clsCounterDtlBean obj = new clsCounterDtlBean();

                obj.setStrCounterCode(rsCounterData.getString(1));
                obj.setStrCounterName(rsCounterData.getString(2));
                obj.setStrSubGroupCode(rsCounterData.getString(3));
                obj.setStrSubGroupName(rsCounterData.getString(4));
                obj.setDblRate(rsCounterData.getDouble(5));
                obj.setDblQuantity(rsCounterData.getDouble(6));
                obj.setDblAmount(rsCounterData.getDouble(7));

                listOfSubGroupWiseCounterData.add(obj);
            }
            rsCounterData.close();
            //QData
            rsCounterData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQfile.toString());
            while (rsCounterData.next())
            {
                clsCounterDtlBean obj = new clsCounterDtlBean();

                obj.setStrCounterCode(rsCounterData.getString(1));
                obj.setStrCounterName(rsCounterData.getString(2));
                obj.setStrSubGroupCode(rsCounterData.getString(3));
                obj.setStrSubGroupName(rsCounterData.getString(4));
                obj.setDblRate(rsCounterData.getDouble(5));
                obj.setDblQuantity(rsCounterData.getDouble(6));
                obj.setDblAmount(rsCounterData.getDouble(7));

                listOfSubGroupWiseCounterData.add(obj);
            }
            rsCounterData.close();

            Comparator<clsCounterDtlBean> counterCodeComparator = new Comparator<clsCounterDtlBean>()
            {

                @Override
                public int compare(clsCounterDtlBean o1, clsCounterDtlBean o2)
                {
                    return o1.getStrCounterCode().compareTo(o2.getStrCounterCode());
                }
            };
            Comparator<clsCounterDtlBean> subGroupCodeComparator = new Comparator<clsCounterDtlBean>()
            {

                @Override
                public int compare(clsCounterDtlBean o1, clsCounterDtlBean o2)
                {
                    return o1.getStrSubGroupCode().compareToIgnoreCase(o2.getStrSubGroupCode());
                }
            };
            Comparator<clsCounterDtlBean> subGroupNameComparator = new Comparator<clsCounterDtlBean>()
            {

                @Override
                public int compare(clsCounterDtlBean o1, clsCounterDtlBean o2)
                {
                    return o1.getStrSubGroupName().compareToIgnoreCase(o2.getStrSubGroupName());
                }
            };

            Collections.sort(listOfSubGroupWiseCounterData, new clsCounterComparator(counterCodeComparator, subGroupCodeComparator, subGroupNameComparator));

            //call for view report
            funViewJasperReportForBeanCollectionDataSource(is, hm, listOfSubGroupWiseCounterData);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void funDiscountReportForJasper()
    {

        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String reportType = hm.get("reportType").toString();

            if (reportType.equalsIgnoreCase("Summary"))
            {
                List<clsBillItemDtlBean> listOfBillItemDtl = new ArrayList<>();
                StringBuilder sbSqlLiveDisc = new StringBuilder();
                StringBuilder sbSqlQFileDisc = new StringBuilder();

                sbSqlLiveDisc.setLength(0);
                sbSqlLiveDisc.append("select d.strPosName,date(a.dteBillDate),a.strBillNo,b.dblDiscPer,b.dblDiscAmt,b.dblDiscOnAmt,b.strDiscOnType,b.strDiscOnValue "
                        + " ,c.strReasonName,b.strDiscRemarks,a.dblSubTotal,a.dblGrandTotal,b.strUserEdited "
                        + " from \n"
                        + " tblbillhd a\n"
                        + " left outer join tblbilldiscdtl b on b.strBillNo=a.strBillNo and date(a.dteBillDate)=date(b.dteBillDate) \n"
                        + " left outer join tblreasonmaster c on c.strReasonCode=b.strDiscReasonCode\n"
                        + " left outer join tblposmaster d on d.strPOSCode=a.strPOSCode\n"
                        + " where  (b.dblDiscAmt> 0.00 or b.dblDiscPer >0.0) \n"
                        + " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
                        + " and a.strClientCode=b.strClientCode ");

                sbSqlQFileDisc.setLength(0);
                sbSqlQFileDisc.append("select d.strPosName,date(a.dteBillDate),a.strBillNo,b.dblDiscPer,b.dblDiscAmt,b.dblDiscOnAmt,b.strDiscOnType,b.strDiscOnValue "
                        + " ,c.strReasonName,b.strDiscRemarks,a.dblSubTotal,a.dblGrandTotal,b.strUserEdited "
                        + " from \n"
                        + " tblqbillhd a\n"
                        + " left outer join tblqbilldiscdtl b on b.strBillNo=a.strBillNo and date(a.dteBillDate)=date(b.dteBillDate) \n"
                        + " left outer join tblreasonmaster c on c.strReasonCode=b.strDiscReasonCode\n"
                        + " left outer join tblposmaster d on d.strPOSCode=a.strPOSCode\n"
                        + " where  (b.dblDiscAmt> 0.00 or b.dblDiscPer >0.0) \n"
                        + " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
                        + " and a.strClientCode=b.strClientCode ");

                if (!posCode.equalsIgnoreCase("All"))
                {
                    sbSqlLiveDisc.append(" and a.strPOSCode='" + posCode + "' ");
                    sbSqlQFileDisc.append(" and a.strPOSCode='" + posCode + "' ");
                }

                if (clsGlobalVarClass.gEnableShiftYN)
                {
                    if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                    {
                        sbSqlLiveDisc.append(" and a.intShiftCode = '" + shiftNo + "' ");
                        sbSqlQFileDisc.append(" and a.intShiftCode = '" + shiftNo + "' ");
                    }
                }

                ResultSet rsLiveDisc = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLiveDisc.toString());
                while (rsLiveDisc.next())
                {
                    clsBillItemDtlBean objBean = new clsBillItemDtlBean();

                    objBean.setStrPosName(rsLiveDisc.getString(1));    //POSName
                    objBean.setDteBillDate(rsLiveDisc.getString(2));   //BillDate
                    objBean.setStrBillNo(rsLiveDisc.getString(3));     //BillNo
                    objBean.setDblDiscountPer(rsLiveDisc.getDouble(4));//DiscPer
                    objBean.setDblDiscountAmt(rsLiveDisc.getDouble(5)); //DiscAmt
                    objBean.setDblBillDiscPer(rsLiveDisc.getDouble(6));//DiscOnAmt
                    objBean.setStrDiscType(rsLiveDisc.getString(7));   //DiscType
                    objBean.setStrDiscValue(rsLiveDisc.getString(8));   //DiscValue
                    objBean.setStrItemCode(rsLiveDisc.getString(9));    //DiscReason
                    objBean.setStrItemName(rsLiveDisc.getString(10));   //DiscRemark
                    objBean.setDblSubTotal(rsLiveDisc.getDouble(11));   //SubTotal
                    objBean.setDblGrandTotal(rsLiveDisc.getDouble(12)); //GrandTotal
                    objBean.setStrSettelmentMode(rsLiveDisc.getString(13)); //UserEdited

                    listOfBillItemDtl.add(objBean);
                }
                rsLiveDisc.close();

                ResultSet rsQfileDisc = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFileDisc.toString());
                while (rsQfileDisc.next())
                {
                    clsBillItemDtlBean objBean = new clsBillItemDtlBean();

                    objBean.setStrPosName(rsQfileDisc.getString(1));    //POSName
                    objBean.setDteBillDate(rsQfileDisc.getString(2));   //BillDate
                    objBean.setStrBillNo(rsQfileDisc.getString(3));     //BillNo
                    objBean.setDblDiscountPer(rsQfileDisc.getDouble(4));//DiscPer
                    objBean.setDblDiscountAmt(rsQfileDisc.getDouble(5)); //DiscAmt
                    objBean.setDblBillDiscPer(rsQfileDisc.getDouble(6));//DiscOnAmt
                    objBean.setStrDiscType(rsQfileDisc.getString(7));   //DiscType
                    objBean.setStrDiscValue(rsQfileDisc.getString(8));   //DiscValue
                    objBean.setStrItemCode(rsQfileDisc.getString(9));    //DiscReason
                    objBean.setStrItemName(rsQfileDisc.getString(10));   //DiscRemark
                    objBean.setDblSubTotal(rsQfileDisc.getDouble(11));   //SubTotal
                    objBean.setDblGrandTotal(rsQfileDisc.getDouble(12)); //GrandTotal
                    objBean.setStrSettelmentMode(rsQfileDisc.getString(13)); //UserEdited

                    listOfBillItemDtl.add(objBean);
                }
                rsQfileDisc.close();

                //call for view report
                funViewJasperReportForBeanCollectionDataSource(is, hm, listOfBillItemDtl);

            }
            else//detail
            {
                StringBuilder sqlModiBuilder = new StringBuilder();
                StringBuilder sqlItemBuilder = new StringBuilder();
                List<clsBillItemDtlBean> listOfBillItemDtl = new ArrayList<>();

                sqlItemBuilder.setLength(0);
                sqlItemBuilder.append("select a.strBillNo,DATE_FORMAT(a.dteBillDate,'%d-%m-%Y') as dteBillDate,c.strPosName,a.dblSubTotal,a.dblGrandTotal "
                        + ",b.strItemCode,b.strItemName,b.dblQuantity,sum(b.dblAmount),sum(b.dblDiscountAmt),b.dblDiscountPer,a.dblDiscountPer as dblBillDiscPer  "
                        + "from tblbillhd a "
                        + "inner join  tblbilldtl b on a.strBillNo=b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate) "
                        + "inner join tblposmaster c on a.strPOSCode=c.strPOSCode "
                        + "where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "'  "
                        + "and b.dblDiscountPer>0 ");
                if (!posCode.equalsIgnoreCase("All"))
                {
                    sqlItemBuilder.append(" and a.strPOSCode='" + posCode + "' ");
                    sqlItemBuilder.append(" and a.strPOSCode='" + posCode + "' ");
                }
//                    if (clsGlobalVarClass.gEnableShiftYN)
//                    {
//                        if (clsGlobalVarClass.gEnableShiftYN && (!cmbShiftNo.getSelectedItem().toString().equalsIgnoreCase("All")))
//                        {
//                            sbSqlLiveDisc.append(" and a.intShiftCode = '" + cmbShiftNo.getSelectedItem().toString() + "' ");
//                            sbSqlQFileDisc.append(" and a.intShiftCode = '" + cmbShiftNo.getSelectedItem().toString() + "' ");
//                        }
//                    }
                sqlItemBuilder.append("group by a.strBillNo,b.strItemCode,b.strItemName "
                        + "order by a.strBillNo,b.strItemCode,b.strItemName");
                ResultSet rsLiveDisc = clsGlobalVarClass.dbMysql.executeResultSet(sqlItemBuilder.toString());
                while (rsLiveDisc.next())
                {
                    clsBillItemDtlBean objBean = new clsBillItemDtlBean();

                    objBean.setStrBillNo(rsLiveDisc.getString(1));
                    objBean.setDteBillDate(rsLiveDisc.getString(2));
                    objBean.setStrPosName(rsLiveDisc.getString(3));
                    objBean.setDblSubTotal(rsLiveDisc.getDouble(4));
                    objBean.setDblGrandTotal(rsLiveDisc.getDouble(5));
                    objBean.setStrItemCode(rsLiveDisc.getString(6));
                    objBean.setStrItemName(rsLiveDisc.getString(7));
                    objBean.setDblQuantity(rsLiveDisc.getDouble(8));
                    objBean.setDblAmount(rsLiveDisc.getDouble(9));
                    objBean.setDblDiscountAmt(rsLiveDisc.getDouble(10));
                    objBean.setDblDiscountPer(rsLiveDisc.getDouble(11));
                    objBean.setStrReasonName(reportName);

                    listOfBillItemDtl.add(objBean);
                }
                rsLiveDisc.close();
                //live modifiers
                sqlModiBuilder.setLength(0);
                sqlModiBuilder.append("select a.strBillNo,DATE_FORMAT(a.dteBillDate,'%d-%m-%Y') as dteBillDate,c.strPosName,a.dblSubTotal,a.dblGrandTotal "
                        + ",b.strItemCode,b.strModifierName,b.dblQuantity,sum(b.dblAmount),sum(b.dblDiscAmt),b.dblDiscPer,a.dblDiscountPer as dblBillDiscPer "
                        + "from tblbillhd a "
                        + "inner join  tblbillmodifierdtl b on a.strBillNo=b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate) "
                        + "inner join tblposmaster c on a.strPOSCode=c.strPOSCode "
                        + "where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "'  "
                        + "and b.dblDiscPer>0 ");
                if (!posCode.equalsIgnoreCase("All"))
                {
                    sqlModiBuilder.append(" and a.strPOSCode='" + posCode + "' ");
                    sqlModiBuilder.append(" and a.strPOSCode='" + posCode + "' ");
                }
//                    if (clsGlobalVarClass.gEnableShiftYN)
//                    {
//                        if (clsGlobalVarClass.gEnableShiftYN && (!cmbShiftNo.getSelectedItem().toString().equalsIgnoreCase("All")))
//                        {
//                            sbSqlLiveDisc.append(" and a.intShiftCode = '" + cmbShiftNo.getSelectedItem().toString() + "' ");
//                            sbSqlQFileDisc.append(" and a.intShiftCode = '" + cmbShiftNo.getSelectedItem().toString() + "' ");
//                        }
//                    }
                sqlModiBuilder.append("group by a.strBillNo,b.strItemCode,b.strModifierName "
                        + "order by a.strBillNo,b.strItemCode,b.strModifierName");
                rsLiveDisc = clsGlobalVarClass.dbMysql.executeResultSet(sqlModiBuilder.toString());
                while (rsLiveDisc.next())
                {
                    clsBillItemDtlBean objBean = new clsBillItemDtlBean();

                    objBean.setStrBillNo(rsLiveDisc.getString(1));
                    objBean.setDteBillDate(rsLiveDisc.getString(2));
                    objBean.setStrPosName(rsLiveDisc.getString(3));
                    objBean.setDblSubTotal(rsLiveDisc.getDouble(4));
                    objBean.setDblGrandTotal(rsLiveDisc.getDouble(5));
                    objBean.setStrItemCode(rsLiveDisc.getString(6));
                    objBean.setStrItemName(rsLiveDisc.getString(7));
                    objBean.setDblQuantity(rsLiveDisc.getDouble(8));
                    objBean.setDblAmount(rsLiveDisc.getDouble(9));
                    objBean.setDblDiscountAmt(rsLiveDisc.getDouble(10));
                    objBean.setDblDiscountPer(rsLiveDisc.getDouble(11));

                    listOfBillItemDtl.add(objBean);
                }
                rsLiveDisc.close();

                //QFile
                sqlItemBuilder.setLength(0);
                sqlItemBuilder.append("select a.strBillNo,DATE_FORMAT(a.dteBillDate,'%d-%m-%Y') as dteBillDate,c.strPosName,a.dblSubTotal,a.dblGrandTotal "
                        + ",b.strItemCode,b.strItemName,b.dblQuantity,sum(b.dblAmount),sum(b.dblDiscountAmt),b.dblDiscountPer,a.dblDiscountPer as dblBillDiscPer "
                        + "from tblqbillhd a "
                        + "inner join  tblqbilldtl b on a.strBillNo=b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate) "
                        + "inner join tblposmaster c on a.strPOSCode=c.strPOSCode "
                        + "where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "'  "
                        + "and b.dblDiscountPer>0 ");
                if (!posCode.equalsIgnoreCase("All"))
                {
                    sqlItemBuilder.append(" and a.strPOSCode='" + posCode + "' ");
                    sqlItemBuilder.append(" and a.strPOSCode='" + posCode + "' ");
                }
//                    if (clsGlobalVarClass.gEnableShiftYN)
//                    {
//                        if (clsGlobalVarClass.gEnableShiftYN && (!cmbShiftNo.getSelectedItem().toString().equalsIgnoreCase("All")))
//                        {
//                            sbSqlLiveDisc.append(" and a.intShiftCode = '" + cmbShiftNo.getSelectedItem().toString() + "' ");
//                            sbSqlQFileDisc.append(" and a.intShiftCode = '" + cmbShiftNo.getSelectedItem().toString() + "' ");
//                        }
//                    }
                sqlItemBuilder.append("group by a.strBillNo,b.strItemCode,b.strItemName "
                        + "order by a.strBillNo,b.strItemCode,b.strItemName");
                ResultSet rsQDisc = clsGlobalVarClass.dbMysql.executeResultSet(sqlItemBuilder.toString());
                while (rsQDisc.next())
                {
                    clsBillItemDtlBean objBean = new clsBillItemDtlBean();

                    objBean.setStrBillNo(rsQDisc.getString(1));
                    objBean.setDteBillDate(rsQDisc.getString(2));
                    objBean.setStrPosName(rsQDisc.getString(3));
                    objBean.setDblSubTotal(rsQDisc.getDouble(4));
                    objBean.setDblGrandTotal(rsQDisc.getDouble(5));
                    objBean.setStrItemCode(rsQDisc.getString(6));
                    objBean.setStrItemName(rsQDisc.getString(7));
                    objBean.setDblQuantity(rsQDisc.getDouble(8));
                    objBean.setDblAmount(rsQDisc.getDouble(9));
                    objBean.setDblDiscountAmt(rsQDisc.getDouble(10));
                    objBean.setDblDiscountPer(rsQDisc.getDouble(11));

                    listOfBillItemDtl.add(objBean);
                }
                rsQDisc.close();
                //QFile modifiers
                sqlModiBuilder.setLength(0);
                sqlModiBuilder.append("select a.strBillNo,DATE_FORMAT(a.dteBillDate,'%d-%m-%Y') as dteBillDate,c.strPosName,a.dblSubTotal,a.dblGrandTotal "
                        + ",b.strItemCode,b.strModifierName,b.dblQuantity,sum(b.dblAmount),sum(b.dblDiscAmt),b.dblDiscPer,a.dblDiscountPer as dblBillDiscPer "
                        + "from tblqbillhd a "
                        + "inner join  tblqbillmodifierdtl b on a.strBillNo=b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate) "
                        + "inner join tblposmaster c on a.strPOSCode=c.strPOSCode "
                        + "where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "'  "
                        + "and b.dblDiscPer>0 ");
                if (!posCode.equalsIgnoreCase("All"))
                {
                    sqlModiBuilder.append(" and a.strPOSCode='" + posCode + "' ");
                    sqlModiBuilder.append(" and a.strPOSCode='" + posCode + "' ");
                }
//                    if (clsGlobalVarClass.gEnableShiftYN)
//                    {
//                        if (clsGlobalVarClass.gEnableShiftYN && (!cmbShiftNo.getSelectedItem().toString().equalsIgnoreCase("All")))
//                        {
//                            sbSqlLiveDisc.append(" and a.intShiftCode = '" + cmbShiftNo.getSelectedItem().toString() + "' ");
//                            sbSqlQFileDisc.append(" and a.intShiftCode = '" + cmbShiftNo.getSelectedItem().toString() + "' ");
//                        }
//                    }
                sqlModiBuilder.append("group by a.strBillNo,b.strItemCode,b.strModifierName "
                        + "order by a.strBillNo,b.strItemCode,b.strModifierName");
                rsQDisc = clsGlobalVarClass.dbMysql.executeResultSet(sqlModiBuilder.toString());
                while (rsQDisc.next())
                {
                    clsBillItemDtlBean objBean = new clsBillItemDtlBean();

                    objBean.setStrBillNo(rsQDisc.getString(1));
                    objBean.setDteBillDate(rsQDisc.getString(2));
                    objBean.setStrPosName(rsQDisc.getString(3));
                    objBean.setDblSubTotal(rsQDisc.getDouble(4));
                    objBean.setDblGrandTotal(rsQDisc.getDouble(5));
                    objBean.setStrItemCode(rsQDisc.getString(6));
                    objBean.setStrItemName(rsQDisc.getString(7));
                    objBean.setDblQuantity(rsQDisc.getDouble(8));
                    objBean.setDblAmount(rsQDisc.getDouble(9));
                    objBean.setDblDiscountAmt(rsQDisc.getDouble(10));
                    objBean.setDblDiscountPer(rsQDisc.getDouble(11));

                    listOfBillItemDtl.add(objBean);
                }
                rsQDisc.close();

                Comparator<clsBillItemDtlBean> itemCodeComparator = new Comparator<clsBillItemDtlBean>()
                {

                    @Override
                    public int compare(clsBillItemDtlBean o1, clsBillItemDtlBean o2)
                    {
                        return o1.getStrItemCode().substring(0, 7).compareTo(o2.getStrItemCode().substring(0, 7));
                    }
                };

                Collections.sort(listOfBillItemDtl, itemCodeComparator);

                //call for view report
                funViewJasperReportForBeanCollectionDataSource(is, hm, listOfBillItemDtl);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void funNonChargableKOTReportForJasper()
    {

        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String reasonCode = hm.get("reasonCode").toString();

            StringBuilder sqlBuilder = new StringBuilder();

            //live
            sqlBuilder.setLength(0);
            sqlBuilder.append("select a.strKOTNo, a.dteNCKOTDate, a.strTableNo, b.strReasonName,d.strPosName,\n"
                    + "a.strRemark,  a.strItemCode, c.strItemName, a.dblQuantity, a.dblRate, a.dblQuantity * a.dblRate as Amount\n"
                    + ",e.strTableName\n"
                    + "from tblnonchargablekot a, tblreasonmaster b, tblitemmaster c,tblposmaster d,tbltablemaster e\n"
                    + "where  a.strReasonCode = b.strReasonCode \n"
                    + "and a.strTableNo=e.strTableNo \n"
                    + "and a.strItemCode = c.strItemCode  and a.strPosCode=d.strPOSCode\n"
                    + "and date(a.dteNCKOTDate) between '" + fromDate + "' and  '" + toDate + "'\n ");
            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and a.strPOSCode='" + posCode + "' ");
            }
            if (!reasonCode.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and a.strReasonCode='" + reasonCode + "'  ");
            }

            ResultSet rsData = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
            List<clsBillDtl> listOfNCKOTData = new ArrayList<clsBillDtl>();
            while (rsData.next())
            {
                clsBillDtl obj = new clsBillDtl();
                obj.setStrKOTNo(rsData.getString(1));
                obj.setDteNCKOTDate(rsData.getString(2));
                obj.setStrTableNo(rsData.getString(3));
                obj.setStrReasonName(rsData.getString(4));
                obj.setStrPosName(rsData.getString(5));
                obj.setStrRemarks(rsData.getString(6));
                obj.setStrItemCode(rsData.getString(7));
                obj.setStrItemName(rsData.getString(8));
                obj.setDblQuantity(rsData.getDouble(9));
                obj.setDblRate(rsData.getDouble(10));
                obj.setDblAmount(rsData.getDouble(11));
                obj.setStrTableName(rsData.getString(12));
                listOfNCKOTData.add(obj);
            }
            //call for view report
            funViewJasperReportForBeanCollectionDataSource(is, hm, listOfNCKOTData);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void funOperatorWiseReportForJasper()
    {
        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String userCode = hm.get("userCode").toString();
            String userName = hm.get("userName").toString();
            String settleCode = hm.get("settleCode").toString();

            StringBuilder sbSqlLive = new StringBuilder();
            StringBuilder sbSqlQFile = new StringBuilder();
            StringBuilder sbSqlDisLive = new StringBuilder();
            StringBuilder sbSqlQDisFile = new StringBuilder();
            StringBuilder sbSqlFilters = new StringBuilder();
            StringBuilder sbSqlDisFilters = new StringBuilder();

            List<clsOperatorDtl> listOfOperatorWiseSettlementDtl = new LinkedList<>();

            sbSqlLive.setLength(0);
            sbSqlQFile.setLength(0);
            sbSqlDisLive.setLength(0);
            sbSqlQDisFile.setLength(0);
            sbSqlFilters.setLength(0);
            sbSqlDisFilters.setLength(0);

            sbSqlLive.append(" SELECT a.strUserCode, a.strUserName, c.strPOSName,e.strSettelmentDesc "
                    + " ,sum(d.dblSettlementAmt),'SANGUINE',c.strPosCode, d.strSettlementCode "
                    + " FROM tbluserhd a "
                    + " INNER JOIN tblbillhd b ON a.strUserCode = b.strUserCreated "
                    + " inner join tblposmaster c on b.strPOSCode=c.strPOSCode "
                    + " inner join tblbillsettlementdtl d on b.strBillNo=d.strBillNo  and date(b.dteBillDate)=date(d.dteBillDate) "
                    + " inner join tblsettelmenthd e on d.strSettlementCode=e.strSettelmentCode "
                    + " WHERE date( b.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' ");

            sbSqlQFile.append(" SELECT a.strUserCode, a.strUserName, c.strPOSName,e.strSettelmentDesc "
                    + " ,sum(d.dblSettlementAmt),'SANGUINE',c.strPosCode, d.strSettlementCode "
                    + " FROM tbluserhd a "
                    + " INNER JOIN tblqbillhd b ON a.strUserCode = b.strUserCreated "
                    + " inner join tblposmaster c on b.strPOSCode=c.strPOSCode "
                    + " inner join tblqbillsettlementdtl d on b.strBillNo=d.strBillNo and date(b.dteBillDate)=date(d.dteBillDate) "
                    + " inner join tblsettelmenthd e on d.strSettlementCode=e.strSettelmentCode "
                    + " WHERE date( b.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' ");

            if (!posCode.equals("All"))
            {
                sbSqlFilters.append(" AND b.strPOSCode = '" + posCode + "' ");
            }

            if (!userCode.equals("All"))
            {
                sbSqlFilters.append("  and b.strUserCreated='" + userCode + "'");
            }

            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sbSqlFilters.append(" and b.intShiftCode = '" + shiftNo + "' ");
                }
            }
            if (settleCode != "All")
            {
                sbSqlFilters.append("  and d.strSettlementCode='" + settleCode + "'");
            }

            sbSqlFilters.append(" GROUP BY a.strUserCode, b.strPosCode, d.strSettlementCode");

            sbSqlLive.append(sbSqlFilters);
            sbSqlQFile.append(sbSqlFilters);

            Map<String, Map<String, clsOperatorDtl>> hmOperatorWiseSales = new HashMap<String, Map<String, clsOperatorDtl>>();
            Map<String, clsOperatorDtl> hmSettlementDtl = null;
            clsOperatorDtl objOperatorWiseSales = null;

            ResultSet rsOperator = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
            while (rsOperator.next())
            {
                if (hmOperatorWiseSales.containsKey(rsOperator.getString(1)))
                {
                    hmSettlementDtl = hmOperatorWiseSales.get(rsOperator.getString(1));
                    if (hmSettlementDtl.containsKey(rsOperator.getString(8)))
                    {
                        objOperatorWiseSales = hmSettlementDtl.get(rsOperator.getString(8));
                        objOperatorWiseSales.setSettleAmt(objOperatorWiseSales.getSettleAmt() + rsOperator.getDouble(5));
                    }
                    else
                    {
                        objOperatorWiseSales = new clsOperatorDtl();
                        objOperatorWiseSales.setStrUserCode(rsOperator.getString(1));
                        objOperatorWiseSales.setStrUserName(rsOperator.getString(2));
                        objOperatorWiseSales.setStrPOSName(rsOperator.getString(3));
                        objOperatorWiseSales.setStrSettlementDesc(rsOperator.getString(4));
                        objOperatorWiseSales.setSettleAmt(rsOperator.getDouble(5));
                        objOperatorWiseSales.setStrPOSCode(rsOperator.getString(7));
                        objOperatorWiseSales.setDiscountAmt(0);
                    }
                    hmSettlementDtl.put(rsOperator.getString(8), objOperatorWiseSales);
                }
                else
                {
                    objOperatorWiseSales = new clsOperatorDtl();
                    objOperatorWiseSales.setStrUserCode(rsOperator.getString(1));
                    objOperatorWiseSales.setStrUserName(rsOperator.getString(2));
                    objOperatorWiseSales.setStrPOSName(rsOperator.getString(3));
                    objOperatorWiseSales.setStrSettlementDesc(rsOperator.getString(4));
                    objOperatorWiseSales.setSettleAmt(rsOperator.getDouble(5));
                    objOperatorWiseSales.setStrPOSCode(rsOperator.getString(7));
                    objOperatorWiseSales.setDiscountAmt(0);

                    hmSettlementDtl = new HashMap<String, clsOperatorDtl>();
                    hmSettlementDtl.put(rsOperator.getString(8), objOperatorWiseSales);
                }
                hmOperatorWiseSales.put(rsOperator.getString(1), hmSettlementDtl);
            }
            rsOperator.close();

            rsOperator = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFile.toString());
            while (rsOperator.next())
            {
                if (hmOperatorWiseSales.containsKey(rsOperator.getString(1)))
                {
                    hmSettlementDtl = hmOperatorWiseSales.get(rsOperator.getString(1));
                    if (hmSettlementDtl.containsKey(rsOperator.getString(8)))
                    {
                        objOperatorWiseSales = hmSettlementDtl.get(rsOperator.getString(8));
                        objOperatorWiseSales.setSettleAmt(objOperatorWiseSales.getSettleAmt() + rsOperator.getDouble(5));
                    }
                    else
                    {
                        objOperatorWiseSales = new clsOperatorDtl();
                        objOperatorWiseSales.setStrUserCode(rsOperator.getString(1));
                        objOperatorWiseSales.setStrUserName(rsOperator.getString(2));
                        objOperatorWiseSales.setStrPOSName(rsOperator.getString(3));
                        objOperatorWiseSales.setStrSettlementDesc(rsOperator.getString(4));
                        objOperatorWiseSales.setSettleAmt(rsOperator.getDouble(5));
                        objOperatorWiseSales.setStrPOSCode(rsOperator.getString(7));
                        objOperatorWiseSales.setDiscountAmt(0);
                    }
                    hmSettlementDtl.put(rsOperator.getString(8), objOperatorWiseSales);
                }
                else
                {
                    objOperatorWiseSales = new clsOperatorDtl();
                    objOperatorWiseSales.setStrUserCode(rsOperator.getString(1));
                    objOperatorWiseSales.setStrUserName(rsOperator.getString(2));
                    objOperatorWiseSales.setStrPOSName(rsOperator.getString(3));
                    objOperatorWiseSales.setStrSettlementDesc(rsOperator.getString(4));
                    objOperatorWiseSales.setSettleAmt(rsOperator.getDouble(5));
                    objOperatorWiseSales.setStrPOSCode(rsOperator.getString(7));
                    objOperatorWiseSales.setDiscountAmt(0);

                    hmSettlementDtl = new HashMap<String, clsOperatorDtl>();
                    hmSettlementDtl.put(rsOperator.getString(8), objOperatorWiseSales);
                }
                hmOperatorWiseSales.put(rsOperator.getString(1), hmSettlementDtl);
            }
            rsOperator.close();

            sbSqlDisLive.append("SELECT a.strUserCode, a.strUserName, c.strPOSName"
                    + " ,sum(b.dblDiscountAmt),'SANGUINE',c.strPosCode "
                    + " FROM tbluserhd a "
                    + " INNER JOIN tblbillhd b ON a.strUserCode = b.strUserCreated "
                    + " inner join tblposmaster c on b.strPOSCode=c.strPOSCode "
                    + " inner join tblbillsettlementdtl d on b.strBillNo=d.strBillNo "
                    + " inner join tblsettelmenthd e on d.strSettlementCode=e.strSettelmentCode "
                    + " WHERE date( b.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' ");

            sbSqlQDisFile.append("  SELECT a.strUserCode, a.strUserName, c.strPOSName "
                    + " ,sum(b.dblDiscountAmt),'SANGUINE',c.strPosCode "
                    + " FROM tbluserhd a "
                    + " INNER JOIN tblqbillhd b ON a.strUserCode = b.strUserCreated "
                    + " inner join tblposmaster c on b.strPOSCode=c.strPOSCode "
                    + " inner join tblqbillsettlementdtl d on b.strBillNo=d.strBillNo "
                    + " inner join tblsettelmenthd e on d.strSettlementCode=e.strSettelmentCode "
                    + " WHERE date( b.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' ");

            if (!posCode.equals("All"))
            {
                sbSqlDisFilters.append(" AND b.strPOSCode = '" + posCode + "' ");
            }

            if (!userCode.equals("All"))
            {
                sbSqlDisFilters.append(" and b.strUserCreated='" + userCode.toString() + "'");
            }

            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sbSqlDisFilters.append(" and b.intShiftCode = '" + shiftNo + "' ");
                }
            }
            if (settleCode != "All")
            {
                sbSqlDisFilters.append("  and d.strSettlementCode='" + settleCode + "'");
            }
            sbSqlDisFilters.append(" GROUP BY a.strUserCode, b.strPosCode,d.strSettlementCode");

            sbSqlDisLive.append(sbSqlDisFilters);
            sbSqlQDisFile.append(sbSqlDisFilters);

            //System.out.println(sbSqlDisLive);
            //System.out.println(sbSqlQDisFile);
            ResultSet rsOperatorDis = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlDisLive.toString());
            while (rsOperatorDis.next())
            {
                if (hmOperatorWiseSales.containsKey(rsOperatorDis.getString(1)))
                {
                    hmSettlementDtl = hmOperatorWiseSales.get(rsOperatorDis.getString(1));
                    Set<String> setKeys = hmSettlementDtl.keySet();
                    for (String keys : setKeys)
                    {
                        objOperatorWiseSales = hmSettlementDtl.get(keys);
                        objOperatorWiseSales.setDiscountAmt(objOperatorWiseSales.getDiscountAmt() + rsOperatorDis.getDouble(4));
                        hmSettlementDtl.put(keys, objOperatorWiseSales);
                        break;
                    }
                    hmOperatorWiseSales.put(rsOperatorDis.getString(1), hmSettlementDtl);
                }
            }
            rsOperatorDis.close();

            rsOperatorDis = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQDisFile.toString());
            while (rsOperatorDis.next())
            {
                if (hmOperatorWiseSales.containsKey(rsOperatorDis.getString(1)))
                {
                    hmSettlementDtl = hmOperatorWiseSales.get(rsOperatorDis.getString(1));
                    Set<String> setKeys = hmSettlementDtl.keySet();
                    for (String keys : setKeys)
                    {
                        objOperatorWiseSales = hmSettlementDtl.get(keys);
                        objOperatorWiseSales.setDiscountAmt(objOperatorWiseSales.getDiscountAmt() + rsOperatorDis.getDouble(4));
                        hmSettlementDtl.put(keys, objOperatorWiseSales);
                        break;
                    }
                    hmOperatorWiseSales.put(rsOperatorDis.getString(1), hmSettlementDtl);
                }
            }
            rsOperatorDis.close();
            int i = 0;
            double discAmt = 0, totalAmt = 0;
            Object[] arrObjTableRowData = new Object[6];

            for (Map.Entry<String, Map<String, clsOperatorDtl>> entry : hmOperatorWiseSales.entrySet())
            {

                Map<String, clsOperatorDtl> hmOpSettlementDtl = entry.getValue();
                for (Map.Entry<String, clsOperatorDtl> entryOp : hmOpSettlementDtl.entrySet())
                {
                    clsOperatorDtl objOperatorDtl = entryOp.getValue();
                    listOfOperatorWiseSettlementDtl.add(objOperatorDtl);

                }
            }

            //call for view report
            funViewJasperReportForBeanCollectionDataSource(is, hm, listOfOperatorWiseSettlementDtl);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void funOrderAnalysisReportForJasper()
    {

        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateFormat.setLenient(false);

            clsOrderAnalysisBean objOrderAnalysis = null;
            Map<String, clsOrderAnalysisBean> hmOrderAnalysisData = new HashMap<String, clsOrderAnalysisBean>();
            StringBuilder sbSql = new StringBuilder();
            sbSql.setLength(0);
            sbSql.append("select c.strItemCode,c.strItemName,b.dblRate,sum(b.dblQuantity),b.strKOTNo"
                    + ",(b.dblRate*sum(b.dblQuantity)) TotalAmt,c.dblPurchaseRate,a.dblDiscountAmt "
                    + "from tblbillhd a,tblbilldtl b,tblitemmaster c "
                    + "where a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " and b.strItemCode=c.strItemCode   "
                    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
            if (!posCode.equals("All"))
            {
                sbSql.append("and a.strPOSCode='" + posCode + "' ");
            }
            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sbSql.append(" and a.intShiftCode = '" + shiftNo + "' ");
                }
            }
            sbSql.append("group by c.strItemCode order by c.strItemName; ");
            ResultSet rsOrderAnanlysis = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
            while (rsOrderAnanlysis.next())
            {
                if (null != hmOrderAnalysisData.get(rsOrderAnanlysis.getString(1)))
                {
                    objOrderAnalysis = hmOrderAnalysisData.get(rsOrderAnanlysis.getString(1));
                    objOrderAnalysis.setSaleQty((objOrderAnalysis.getSaleQty() + rsOrderAnanlysis.getDouble(4)));
                }
                else
                {
                    objOrderAnalysis = new clsOrderAnalysisBean();
                    objOrderAnalysis.setSaleQty((rsOrderAnanlysis.getDouble(4)));
                }
                objOrderAnalysis.setItemCode(rsOrderAnanlysis.getString(1));
                objOrderAnalysis.setStrItemName(rsOrderAnanlysis.getString(2));
                objOrderAnalysis.setItemSaleRate(rsOrderAnanlysis.getDouble(3));
                objOrderAnalysis.setItemPurchaseRate(rsOrderAnanlysis.getDouble(7));
                objOrderAnalysis.setKOTNo(rsOrderAnanlysis.getString(5));
                objOrderAnalysis.setDblNCQty(0);
                objOrderAnalysis.setTotalDiscountAmt(rsOrderAnanlysis.getDouble(8));
                objOrderAnalysis.setVoidKOTQty(0);
                objOrderAnalysis.setVoidQty(0);
                objOrderAnalysis.setDblCompQty(0);
                double finalQty = (objOrderAnalysis.getSaleQty() - (objOrderAnalysis.getVoidQty() + objOrderAnalysis.getVoidKOTQty()));
                objOrderAnalysis.setFinalItemQty(finalQty);
                objOrderAnalysis.setTotalAmt(objOrderAnalysis.getSaleQty() * rsOrderAnanlysis.getDouble(3));
                objOrderAnalysis.setTotalCostValue(objOrderAnalysis.getSaleQty() * rsOrderAnanlysis.getDouble(7));

                hmOrderAnalysisData.put(rsOrderAnanlysis.getString(1), objOrderAnalysis);
            }
            rsOrderAnanlysis.close();
            System.out.println(sbSql);

            sbSql.setLength(0);
            sbSql.append("select c.strItemCode,c.strItemName,b.dblRate,sum(b.dblQuantity),b.strKOTNo"
                    + ",(b.dblRate*sum(b.dblQuantity)) TotalAmt,c.dblPurchaseRate,a.dblDiscountAmt "
                    + "from tblqbillhd a,tblqbilldtl b,tblitemmaster c "
                    + "where a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " and b.strItemCode=c.strItemCode   "
                    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
            if (!posCode.equals("All"))
            {
                sbSql.append("and a.strPOSCode='" + posCode + "' ");
            }
            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sbSql.append(" and a.intShiftCode = '" + shiftNo + "' ");
                }
            }
            sbSql.append("group by c.strItemCode order by c.strItemName; ");
            rsOrderAnanlysis = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
            while (rsOrderAnanlysis.next())
            {
                if (null != hmOrderAnalysisData.get(rsOrderAnanlysis.getString(1)))
                {
                    objOrderAnalysis = hmOrderAnalysisData.get(rsOrderAnanlysis.getString(1));
                    objOrderAnalysis.setSaleQty((objOrderAnalysis.getSaleQty() + rsOrderAnanlysis.getDouble(4)));
                }
                else
                {
                    objOrderAnalysis = new clsOrderAnalysisBean();
                    objOrderAnalysis.setSaleQty((rsOrderAnanlysis.getDouble(4)));
                }
                objOrderAnalysis.setItemCode(rsOrderAnanlysis.getString(1));
                objOrderAnalysis.setStrItemName(rsOrderAnanlysis.getString(2));
                objOrderAnalysis.setItemSaleRate(rsOrderAnanlysis.getDouble(3));
                objOrderAnalysis.setItemPurchaseRate(rsOrderAnanlysis.getDouble(7));
                objOrderAnalysis.setKOTNo(rsOrderAnanlysis.getString(5));
                objOrderAnalysis.setDblNCQty(0);
                objOrderAnalysis.setTotalDiscountAmt(rsOrderAnanlysis.getDouble(8));
                objOrderAnalysis.setVoidKOTQty(0);
                objOrderAnalysis.setVoidQty(0);
                objOrderAnalysis.setDblCompQty(0);
                double finalQty = (objOrderAnalysis.getSaleQty() - (objOrderAnalysis.getVoidQty() + objOrderAnalysis.getVoidKOTQty()));
                objOrderAnalysis.setFinalItemQty(finalQty);
                objOrderAnalysis.setTotalAmt(objOrderAnalysis.getSaleQty() * rsOrderAnanlysis.getDouble(3));
                objOrderAnalysis.setTotalCostValue(objOrderAnalysis.getSaleQty() * rsOrderAnanlysis.getDouble(7));

                hmOrderAnalysisData.put(rsOrderAnanlysis.getString(1), objOrderAnalysis);
            }
            rsOrderAnanlysis.close();
            System.out.println(sbSql);

            sbSql.setLength(0);
            sbSql.append("select a.strItemCode,sum(a.dblQuantity) "
                    + "from tblnonchargablekot a "
                    + "where date(a.dteNCKOTDate) between '" + fromDate + "' and '" + toDate + "' ");
            if (!posCode.equals("All"))
            {
                sbSql.append("and a.strPOSCode='' ");
            }

            sbSql.append("group by a.strItemCode");
            rsOrderAnanlysis = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
            while (rsOrderAnanlysis.next())
            {
                if (null != hmOrderAnalysisData.get(rsOrderAnanlysis.getString(1)))
                {
                    objOrderAnalysis = hmOrderAnalysisData.get(rsOrderAnanlysis.getString(1));
                    objOrderAnalysis.setDblNCQty(rsOrderAnanlysis.getDouble(2));
                    double finalQty = (objOrderAnalysis.getSaleQty() - (objOrderAnalysis.getVoidQty() + objOrderAnalysis.getVoidKOTQty()));
                    objOrderAnalysis.setFinalItemQty(finalQty);
                    objOrderAnalysis.setTotalAmt(objOrderAnalysis.getSaleQty() * objOrderAnalysis.getItemSaleRate());
                    objOrderAnalysis.setTotalCostValue(objOrderAnalysis.getSaleQty() * objOrderAnalysis.getItemPurchaseRate());
                    hmOrderAnalysisData.put(rsOrderAnanlysis.getString(1), objOrderAnalysis);
                }
            }
            rsOrderAnanlysis.close();
            System.out.println(sbSql);

            sbSql.setLength(0);
            sbSql.append("select a.strItemCode,sum(a.intQuantity) "
                    + "from tblvoidbilldtl a "
                    + "where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
            if (!posCode.equals("All"))
            {
                sbSql.append("and a.strPOSCode='' ");
            }
            sbSql.append("group by a.strItemCode");
            rsOrderAnanlysis = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
            while (rsOrderAnanlysis.next())
            {
                if (null != hmOrderAnalysisData.get(rsOrderAnanlysis.getString(1)))
                {
                    objOrderAnalysis = hmOrderAnalysisData.get(rsOrderAnanlysis.getString(1));
                    objOrderAnalysis.setVoidQty(rsOrderAnanlysis.getDouble(2));
                    double finalQty = (objOrderAnalysis.getSaleQty() - (objOrderAnalysis.getVoidQty() + objOrderAnalysis.getVoidKOTQty()));
                    objOrderAnalysis.setFinalItemQty(finalQty);
                    objOrderAnalysis.setTotalAmt(objOrderAnalysis.getSaleQty() * objOrderAnalysis.getItemSaleRate());
                    objOrderAnalysis.setTotalCostValue(objOrderAnalysis.getSaleQty() * objOrderAnalysis.getItemPurchaseRate());
                    hmOrderAnalysisData.put(rsOrderAnanlysis.getString(1), objOrderAnalysis);
                }
            }
            rsOrderAnanlysis.close();
            System.out.println(sbSql);

            sbSql.setLength(0);
            sbSql.append("select a.strItemCode,sum(a.dblItemQuantity) "
                    + "from tblvoidkot a,tblitemmaster b "
                    + "where a.strItemCode=b.strItemCode and date(a.dteVoidedDate) between '" + fromDate + "' and '" + toDate + "' ");
            if (!posCode.equals("All"))
            {
                sbSql.append("and a.strPOSCode='' ");
            }
            sbSql.append("group by a.strItemCode");
            rsOrderAnanlysis = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
            while (rsOrderAnanlysis.next())
            {
                if (null != hmOrderAnalysisData.get(rsOrderAnanlysis.getString(1)))
                {
                    objOrderAnalysis = hmOrderAnalysisData.get(rsOrderAnanlysis.getString(1));
                    objOrderAnalysis.setVoidKOTQty(rsOrderAnanlysis.getDouble(2));
                    double finalQty = (objOrderAnalysis.getSaleQty() - (objOrderAnalysis.getVoidQty() + objOrderAnalysis.getVoidKOTQty()));
                    objOrderAnalysis.setFinalItemQty(finalQty);
                    objOrderAnalysis.setTotalAmt(objOrderAnalysis.getSaleQty() * objOrderAnalysis.getItemSaleRate());
                    objOrderAnalysis.setTotalCostValue(objOrderAnalysis.getSaleQty() * objOrderAnalysis.getItemPurchaseRate());
                    hmOrderAnalysisData.put(rsOrderAnanlysis.getString(1), objOrderAnalysis);
                }
            }
            rsOrderAnanlysis.close();
            System.out.println(sbSql);

            sbSql.setLength(0);
            sbSql.append("select b.stritemcode,sum(b.dblQuantity)  "
                    + " from tblbillhd a,tblbilldtl b, tblbillsettlementdtl c,tblsettelmenthd d,tblposmaster e  ,tblitemmaster f,tblsubgrouphd g,tblgrouphd h  "
                    + " where a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " and a.strBillNo=c.strBillNo "
                    + " and date(a.dteBillDate)=date(c.dteBillDate) "
                    + " and c.strSettlementCode=d.strSettelmentCode  "
                    + " and a.strPOSCode=e.strPosCode "
                    + " and b.strItemCode=f.strItemCode "
                    + " and f.strSubGroupCode=g.strSubGroupCode  "
                    + " and g.strGroupCode=h.strGroupCode "
                    + " and d.strSettelmentType='Complementary'  "
                    + " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");

            if (!posCode.equals("All"))
            {
                sbSql.append("and a.strPOSCode='' ");
            }
            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sbSql.append(" and a.intShiftCode = '" + shiftNo + "' ");
                }
            }
            sbSql.append("group by b.strItemCode,e.strPOSName order by a.dteBillDate ");
            rsOrderAnanlysis = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
            while (rsOrderAnanlysis.next())
            {
                if (null != hmOrderAnalysisData.get(rsOrderAnanlysis.getString(1)))
                {
                    objOrderAnalysis = hmOrderAnalysisData.get(rsOrderAnanlysis.getString(1));
                    objOrderAnalysis.setDblCompQty(rsOrderAnanlysis.getDouble(2));
                    double finalQty = (objOrderAnalysis.getSaleQty() - (objOrderAnalysis.getVoidQty() + objOrderAnalysis.getVoidKOTQty()));
                    objOrderAnalysis.setFinalItemQty(finalQty);
                    objOrderAnalysis.setTotalAmt(objOrderAnalysis.getSaleQty() * objOrderAnalysis.getItemSaleRate());
                    objOrderAnalysis.setTotalCostValue(objOrderAnalysis.getSaleQty() * objOrderAnalysis.getItemPurchaseRate());
                    hmOrderAnalysisData.put(rsOrderAnanlysis.getString(1), objOrderAnalysis);
                }
            }
            rsOrderAnanlysis.close();
            System.out.println(sbSql);

            String sql = "truncate table tblorderanalysis";
            clsGlobalVarClass.dbMysql.execute(sql);

            double totalSaleAmt = 0, totalCostValue = 0;
            List<clsOrderAnalysisBean> listOrderAnalysis = new ArrayList<clsOrderAnalysisBean>();
            for (Map.Entry<String, clsOrderAnalysisBean> entry : hmOrderAnalysisData.entrySet())
            {

                clsOrderAnalysisBean objOrderAnalysis1 = entry.getValue();
                sbSql.setLength(0);
                sbSql.append("insert into tblorderanalysis values('" + objOrderAnalysis1.getStrItemName() + "'"
                        + ",'" + objOrderAnalysis1.getItemSaleRate() + "','" + objOrderAnalysis1.getSaleQty() + "'"
                        + ",'" + objOrderAnalysis1.getDblNCQty() + "','" + objOrderAnalysis1.getVoidQty() + "'"
                        + ",'" + objOrderAnalysis1.getVoidKOTQty() + "','" + objOrderAnalysis1.getKOTNo() + "'"
                        + ",'" + objOrderAnalysis1.getItemPurchaseRate() + "','" + objOrderAnalysis1.getTotalAmt() + "'"
                        + ",'" + objOrderAnalysis1.getTotalCostValue() + "','" + objOrderAnalysis1.getTotalDiscountAmt() + "'"
                        + ",'" + clsGlobalVarClass.gUserCode + "','0','0','" + objOrderAnalysis1.getDblCompQty() + "')");
                System.out.println(objOrderAnalysis1.getItemCode() + "\t" + sbSql);
                clsGlobalVarClass.dbMysql.execute(sbSql.toString());

                totalSaleAmt += objOrderAnalysis1.getTotalAmt();
                totalCostValue += objOrderAnalysis1.getTotalCostValue();
            }

            String pattern = "###.##";
            DecimalFormat decimalFormat = new DecimalFormat(pattern);
            for (Map.Entry<String, clsOrderAnalysisBean> entry : hmOrderAnalysisData.entrySet())
            {

                clsOrderAnalysisBean objOrderAnalysis1 = entry.getValue();
                sbSql.setLength(0);
                double per = 0;
                if (totalSaleAmt > 0)
                {
                    per = Double.parseDouble(decimalFormat.format((objOrderAnalysis1.getSaleQty() / totalSaleAmt) * 100));
                    objOrderAnalysis1.setPer(per);

                }
                double costValuePer = 0;
                if (totalCostValue > 0)
                {
                    costValuePer = Double.parseDouble(decimalFormat.format((objOrderAnalysis1.getSaleQty() / totalCostValue) * 100));
                    objOrderAnalysis1.setCostValuePer(costValuePer);
                }
                sbSql.append("update tblorderanalysis set strField13='" + per + "',strField14='" + costValuePer + "' "
                        + "where strField1='" + objOrderAnalysis1.getStrItemName() + "'");
                clsGlobalVarClass.dbMysql.execute(sbSql.toString());
                listOrderAnalysis.add(objOrderAnalysis1);

            }

            //call for view report
            funViewJasperReportForBeanCollectionDataSource(is, hm, listOrderAnalysis);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
/*
    public void funVoidBillReportForJasper()
    {

        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String reportType = hm.get("reportType").toString();

            if (reportType.equalsIgnoreCase("Summary"))
            {
                StringBuilder sqlBuilder = new StringBuilder();

                //Bill detail data
                sqlBuilder.setLength(0);
                sqlBuilder.append("select a.strBillNo,Date(a.dteBillDate) as BillDate,Date(a.dteModifyVoidBill) as VoidedDate, "
                        + " Time(a.dteBillDate) As EntryTime,Time(a.dteModifyVoidBill) VoidedTime, "
                        + " a.dblModifiedAmount as BillAmount,a.strReasonName as Reason,a.strUserEdited as UserEdited "
                        + " ,a.strUserCreated,a.strRemark "
                        + " from tblvoidbillhd a,tblvoidbilldtl b "
                        + " where a.strBillNo=b.strBillNo "
                        + " and date(a.dteBillDate)=date(b.dteBillDate) "
                        + " and b.strTransType='VB' "
                        + " and a.strTransType='VB' "
                        + " and Date(a.dteModifyVoidBill)  Between '" + fromDate + "' and '" + toDate + "' ");
                if (!posCode.equalsIgnoreCase("All"))
                {
                    sqlBuilder.append("and a.strPosCode='" + posCode + "' ");
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

                //Bill Modifier data
                sqlBuilder.setLength(0);
                sqlBuilder.append("select a.strBillNo,Date(a.dteBillDate) as BillDate,Date(a.dteModifyVoidBill) as VoidedDate "
                        + " ,Time(a.dteBillDate) As EntryTime,Time(a.dteModifyVoidBill) VoidedTime "
                        + " ,b.dblAmount as BillAmount,a.strReasonName as Reason,a.strUserEdited as UserEdited "
                        + " ,a.strUserCreated,b.strRemarks "
                        + " from tblvoidbillhd a, tblvoidmodifierdtl b "
                        + " where a.strBillNo=b.strBillNo "
                        + " and date(a.dteBillDate)=date(b.dteBillDate) "
                        + " and a.strTransType='VB' "
                        + " and Date(a.dteModifyVoidBill) Between '" + fromDate + "' and '" + toDate + "' ");
                if (!posCode.equalsIgnoreCase("All"))
                {
                    sqlBuilder.append(" and a.strPosCode='" + posCode + "' ");
                }
                if (!shiftNo.equalsIgnoreCase("All"))
                {
                    sqlBuilder.append(" and a.intShiftCode='" + shiftNo + "'  ");
                }
                sqlBuilder.append(" group by a.strBillNo  ");

                rsVoidData = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
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
                //call for view report
                funViewJasperReportForBeanCollectionDataSource(is, hm, listOfVoidBillData);
            }
            else
            {
                StringBuilder sqlBuilder = new StringBuilder();

                //Bill detail data
                sqlBuilder.setLength(0);
                sqlBuilder.append("select a.strBillNo,Date(a.dteBillDate) as BillDate,Date(a.dteModifyVoidBill) as VoidedDate, "
                        + " Time(a.dteBillDate) As EntryTime,Time(a.dteModifyVoidBill) VoidedTime,b.strItemName, "
                        + " sum(b.intQuantity),sum(b.dblAmount) as BillAmount,b.strReasonName as Reason, "
                        + " a.strUserEdited as UserEdited, a.strUserCreated,b.strRemarks "
                        + " from tblvoidbillhd a,tblvoidbilldtl b"
                        + " where a.strBillNo=b.strBillNo "
                        + " and date(a.dteBillDate)=date(b.dteBillDate) "
                        + " and b.strTransType='VB' "
                        + " and a.strTransType='VB'  "
                        + " and Date(a.dteModifyVoidBill) Between '" + fromDate + "' and '" + toDate + "' ");
                if (!posCode.equalsIgnoreCase("All"))
                {
                    sqlBuilder.append("and a.strPosCode='" + posCode + "' ");
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
                    objVoidBill.setStrClientCode(rsVoidData.getString(10));      //User Edited
                    objVoidBill.setStrUserCreated(rsVoidData.getString(11));     //User Created
                    objVoidBill.setStrRemarks(rsVoidData.getString(12));         //Remarks   

                    listOfVoidBillData.add(objVoidBill);
                }
                rsVoidData.close();

                //Bill Modifier data
                sqlBuilder.setLength(0);
                sqlBuilder.append("select a.strBillNo,Date(a.dteBillDate) as BillDate,Date(a.dteModifyVoidBill) as VoidedDate, "
                        + " Time(a.dteBillDate) As EntryTime,Time(a.dteModifyVoidBill) VoidedTime,b.strModifierName, "
                        + " sum(b.dblQuantity),sum(b.dblAmount) as BillAmount,ifnull(c.strReasonName,'NA') as Reason, "
                        + " a.strUserEdited as UserEdited, a.strUserCreated,b.strRemarks "
                        + " from tblvoidbillhd a,tblvoidmodifierdtl b "
                        + " left outer join tblreasonmaster c on b.strReasonCode=c.strReasonCode "
                        + " where a.strBillNo=b.strBillNo "
                        + " and date(a.dteBillDate)=date(b.dteBillDate) "
                        + " and a.strTransType='VB' "
                        + " and Date(a.dteModifyVoidBill) Between '" + fromDate + "' and '" + toDate + "' ");
                if (!posCode.equalsIgnoreCase("All"))
                {
                    sqlBuilder.append(" and a.strPosCode='" + posCode + "' ");
                }
                if (!shiftNo.equalsIgnoreCase("All"))
                {
                    sqlBuilder.append(" and a.intShiftCode='" + shiftNo + "'  ");
                }
                sqlBuilder.append(" group by a.strBillNo,b.strModifierCode  ");

                rsVoidData = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
                while (rsVoidData.next())
                {
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
                    objVoidBill.setStrClientCode(rsVoidData.getString(10));      //User Edited
                    objVoidBill.setStrUserCreated(rsVoidData.getString(11));     //User Created
                    objVoidBill.setStrRemarks(rsVoidData.getString(12));         //Remarks   

                    listOfVoidBillData.add(objVoidBill);
                }
                rsVoidData.close();

                //call for view report
                funViewJasperReportForBeanCollectionDataSource(is, hm, listOfVoidBillData);

            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    */
    
    public void funVoidBillReportForJasper()
    {

        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String reportType = hm.get("reportType").toString();

            if (reportType.equalsIgnoreCase("Summary"))
            {
                StringBuilder sqlBuilder = new StringBuilder();

                //Bill detail data
                sqlBuilder.setLength(0);
                sqlBuilder.append("select a.strBillNo,Date(a.dteBillDate) as BillDate,Date(a.dteModifyVoidBill) as VoidedDate, "
                        + " Time(a.dteBillDate) As EntryTime,Time(a.dteModifyVoidBill) VoidedTime, "
                        + " a.dblModifiedAmount as BillAmount,a.strReasonName as Reason,a.strUserEdited as UserEdited "
                        + " ,a.strUserCreated,a.strRemark,a.strVoidBillType "
                        + " from tblvoidbillhd a,tblvoidbilldtl b "
                        + " where a.strBillNo=b.strBillNo "
                        + " and date(a.dteBillDate)=date(b.dteBillDate) "
                        + " and b.strTransType='VB' "
                        + " and a.strTransType='VB' "
                        + " and Date(a.dteModifyVoidBill)  Between '" + fromDate + "' and '" + toDate + "' ");
                if (!posCode.equalsIgnoreCase("All"))
                {
                    sqlBuilder.append("and a.strPosCode='" + posCode + "' ");
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
                    objVoidBill.setStrVoidBillType(rsVoidData.getString(11));         //Void Bill Type
                    
                    listOfVoidBillData.add(objVoidBill);
                }
                rsVoidData.close();

                //Bill Modifier data
                sqlBuilder.setLength(0);
                sqlBuilder.append("select a.strBillNo,Date(a.dteBillDate) as BillDate,Date(a.dteModifyVoidBill) as VoidedDate "
                        + " ,Time(a.dteBillDate) As EntryTime,Time(a.dteModifyVoidBill) VoidedTime "
                        + " ,b.dblAmount as BillAmount,a.strReasonName as Reason,a.strUserEdited as UserEdited "
                        + " ,a.strUserCreated,b.strRemarks,a.strVoidBillType "
                        + " from tblvoidbillhd a, tblvoidmodifierdtl b "
                        + " where a.strBillNo=b.strBillNo "
                        + " and date(a.dteBillDate)=date(b.dteBillDate) "
                        + " and a.strTransType='VB' "
                        + " and Date(a.dteModifyVoidBill) Between '" + fromDate + "' and '" + toDate + "' ");
                if (!posCode.equalsIgnoreCase("All"))
                {
                    sqlBuilder.append(" and a.strPosCode='" + posCode + "' ");
                }
                if (!shiftNo.equalsIgnoreCase("All"))
                {
                    sqlBuilder.append(" and a.intShiftCode='" + shiftNo + "'  ");
                }
                sqlBuilder.append(" group by a.strBillNo  ");

                rsVoidData = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
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
                    objVoidBill.setStrVoidBillType(rsVoidData.getString(11));         //Void Bill Type
                    
                    listOfVoidBillData.add(objVoidBill);
                }
                rsVoidData.close();
                //call for view report
                funViewJasperReportForBeanCollectionDataSource(is, hm, listOfVoidBillData);
            }
            else
            {
                StringBuilder sqlBuilder = new StringBuilder();

                //Bill detail data
                sqlBuilder.setLength(0);
                sqlBuilder.append("select a.strBillNo,Date(a.dteBillDate) as BillDate,Date(a.dteModifyVoidBill) as VoidedDate, "
                        + " Time(a.dteBillDate) As EntryTime,Time(a.dteModifyVoidBill) VoidedTime,b.strItemName, "
                        + " sum(b.intQuantity),sum(b.dblAmount) as BillAmount,b.strReasonName as Reason, "
                        + " a.strUserEdited as UserEdited, a.strUserCreated,b.strRemarks,a.strVoidBillType "
                        + " from tblvoidbillhd a,tblvoidbilldtl b"
                        + " where a.strBillNo=b.strBillNo "
                        + " and date(a.dteBillDate)=date(b.dteBillDate) "
                        + " and b.strTransType='VB' "
                        + " and a.strTransType='VB'  "
                        + " and Date(a.dteModifyVoidBill) Between '" + fromDate + "' and '" + toDate + "' ");
                if (!posCode.equalsIgnoreCase("All"))
                {
                    sqlBuilder.append("and a.strPosCode='" + posCode + "' ");
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
                    objVoidBill.setStrClientCode(rsVoidData.getString(10));      //User Edited
                    objVoidBill.setStrUserCreated(rsVoidData.getString(11));     //User Created
                    objVoidBill.setStrRemarks(rsVoidData.getString(12));         //Remarks   
                    objVoidBill.setStrVoidBillType(rsVoidData.getString(13));         //Void Bill Type
                    
                    listOfVoidBillData.add(objVoidBill);
                }
                rsVoidData.close();

                //Bill Modifier data
                sqlBuilder.setLength(0);
                sqlBuilder.append("select a.strBillNo,Date(a.dteBillDate) as BillDate,Date(a.dteModifyVoidBill) as VoidedDate, "
                        + " Time(a.dteBillDate) As EntryTime,Time(a.dteModifyVoidBill) VoidedTime,b.strModifierName, "
                        + " sum(b.dblQuantity),sum(b.dblAmount) as BillAmount,ifnull(c.strReasonName,'NA') as Reason, "
                        + " a.strUserEdited as UserEdited, a.strUserCreated,b.strRemarks,a.strVoidBillType "
                        + " from tblvoidbillhd a,tblvoidmodifierdtl b "
                        + " left outer join tblreasonmaster c on b.strReasonCode=c.strReasonCode "
                        + " where a.strBillNo=b.strBillNo "
                        + " and date(a.dteBillDate)=date(b.dteBillDate) "
                        + " and a.strTransType='VB' "
                        + " and Date(a.dteModifyVoidBill) Between '" + fromDate + "' and '" + toDate + "' ");
                if (!posCode.equalsIgnoreCase("All"))
                {
                    sqlBuilder.append(" and a.strPosCode='" + posCode + "' ");
                }
                if (!shiftNo.equalsIgnoreCase("All"))
                {
                    sqlBuilder.append(" and a.intShiftCode='" + shiftNo + "'  ");
                }
                sqlBuilder.append(" group by a.strBillNo,b.strModifierCode  ");

                rsVoidData = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
                while (rsVoidData.next())
                {
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
                    objVoidBill.setStrClientCode(rsVoidData.getString(10));      //User Edited
                    objVoidBill.setStrUserCreated(rsVoidData.getString(11));     //User Created
                    objVoidBill.setStrRemarks(rsVoidData.getString(12));         //Remarks   
                    objVoidBill.setStrVoidBillType(rsVoidData.getString(11));         //Void Bill Type
                    
                    listOfVoidBillData.add(objVoidBill);
                }
                rsVoidData.close();

                //call for view report
                funViewJasperReportForBeanCollectionDataSource(is, hm, listOfVoidBillData);

            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void funTaxBreakupSummaryReportForJasper()
    {

        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();

            Map<String, clsTaxCalculationDtls> mapTaxDtl = new HashMap<>();
            StringBuilder sqlTaxBuilder = new StringBuilder();
            StringBuilder sqlMenuBreakupBuilder = new StringBuilder();
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
            hm.put("listOfTaxDtl", listOfTaxDtl);

            Map<String, clsTaxCalculationDtls> mapMenuBreakupDtl = new HashMap<>();

            //live menuBreakup
            sqlMenuBreakupBuilder.setLength(0);
            sqlMenuBreakupBuilder.append("select d.strItemCode,d.strItemName,sum(d.dblamount)"
                    + " from tblbillhd a  "
                    + " left Outer join tblbilltaxdtl b on a.strBillNo=b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " left outer join tbltaxhd c on b.strTaxCode=c.strTaxCode "
                    + " left outer join tblbilldtl d on a.strBillNo=d.strBillNo "
                    + " where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
                    + " and b.dblTaxableAmount IS NOT NULL ");
            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlMenuBreakupBuilder.append(" and a.strPOSCode='" + posCode + "' ");
            }
            if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
            {
                sqlMenuBreakupBuilder.append(" and a.intShiftCode='" + shiftNo + "' ");
            }
            sqlMenuBreakupBuilder.append(" group by d.strItemCode,d.strItemName order by d.strItemName ");

            ResultSet rsMenuBreakupDtl = clsGlobalVarClass.dbMysql.executeResultSet(sqlMenuBreakupBuilder.toString());
            while (rsMenuBreakupDtl.next())
            {
                if (mapMenuBreakupDtl.containsKey(rsMenuBreakupDtl.getString(1)))//itemName
                {
                    clsTaxCalculationDtls obj = mapTaxDtl.get(rsMenuBreakupDtl.getString(1));
                    obj.setTaxAmount(obj.getTaxAmount() + rsMenuBreakupDtl.getDouble(3));
                }
                else
                {
                    clsTaxCalculationDtls obj = new clsTaxCalculationDtls();
                    obj.setTaxCode(rsMenuBreakupDtl.getString(1));
                    obj.setTaxName(rsMenuBreakupDtl.getString(2));
                    obj.setTaxAmount(rsMenuBreakupDtl.getDouble(3));
                    mapMenuBreakupDtl.put(rsMenuBreakupDtl.getString(1), obj);

                }
            }
            rsMenuBreakupDtl.close();
            //Q menuBreakup
            sqlMenuBreakupBuilder.setLength(0);
            sqlMenuBreakupBuilder.append("select d.strItemCode,d.strItemName,sum(d.dblamount)"
                    + " from tblqbillhd a  "
                    + " left Outer join tblqbilltaxdtl b on a.strBillNo=b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " left outer join tbltaxhd c on b.strTaxCode=c.strTaxCode "
                    + " left outer join tblqbilldtl d on a.strBillNo=d.strBillNo "
                    + " where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
                    + " and b.dblTaxableAmount IS NOT NULL ");
            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlMenuBreakupBuilder.append(" and a.strPOSCode='" + posCode + "' ");
            }
            if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
            {
                sqlMenuBreakupBuilder.append(" and a.intShiftCode='" + shiftNo + "' ");
            }
            sqlMenuBreakupBuilder.append(" group by d.strItemCode,d.strItemName order by d.strItemName ");

            rsMenuBreakupDtl = clsGlobalVarClass.dbMysql.executeResultSet(sqlMenuBreakupBuilder.toString());
            while (rsMenuBreakupDtl.next())
            {
                if (mapMenuBreakupDtl.containsKey(rsMenuBreakupDtl.getString(1)))//itemName
                {
                    clsTaxCalculationDtls obj = mapMenuBreakupDtl.get(rsMenuBreakupDtl.getString(1));
                    obj.setTaxAmount(obj.getTaxAmount() + rsMenuBreakupDtl.getDouble(3));
                }
                else
                {
                    clsTaxCalculationDtls obj = new clsTaxCalculationDtls();
                    obj.setTaxCode(rsMenuBreakupDtl.getString(1));
                    obj.setTaxName(rsMenuBreakupDtl.getString(2));
                    obj.setTaxAmount(rsMenuBreakupDtl.getDouble(3));
                    mapMenuBreakupDtl.put(rsMenuBreakupDtl.getString(1), obj);

                }
            }
            rsMenuBreakupDtl.close();

            List<clsTaxCalculationDtls> listOfMenuHeadBreakupDtl = new LinkedList<>();
            for (clsTaxCalculationDtls objMenuDtl : mapMenuBreakupDtl.values())
            {
                listOfMenuHeadBreakupDtl.add(objMenuDtl);
            }
            hm.put("listOfMenuBreakupDtl", listOfMenuHeadBreakupDtl);

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

    public void funAuditorsReportForJasper()
    {

        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();

            String sqlLive = "", sqlQFile = "";
            StringBuilder sbSqlDisLive = new StringBuilder();
            StringBuilder sbSqlQDisFile = new StringBuilder();
            StringBuilder sbSqlDisFilters = new StringBuilder();
            List<clsOperatorDtl> listOperatorDtl = new ArrayList<>();

            sbSqlDisLive.setLength(0);
            sbSqlQDisFile.setLength(0);
            sbSqlDisFilters.setLength(0);

            String MinBillNo = "";
            String MaxBillNo = "";
            String TotalDiscount = "";
            String sql = "select min(a.strBillNo),max(a.strBillNo),sum(a.dblDiscountAmt)\n"
                    + "from vqbillhd  a \n"
                    + "where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' \n"
                    + "Order By a.strBillNo";
            ResultSet rsAuditorReport = clsGlobalVarClass.dbMysql.executeResultSet(sql);
            while (rsAuditorReport.next())
            {
                MinBillNo = rsAuditorReport.getString(1);
                MaxBillNo = rsAuditorReport.getString(2);
                TotalDiscount = rsAuditorReport.getString(3);
            }
            rsAuditorReport.close();
            hm.put("minBillNo", MinBillNo);
            hm.put("maxBillNo", MaxBillNo);
            hm.put("totalDiscount", TotalDiscount);

            sqlLive = " select a.strBillNo,ifnull(d.strSettelmentDesc,'ND') as payMode,ifnull(a.dblSubTotal,0.00) as subTotal,"
                    + " a.dblTaxAmt,a.dblDiscountAmt,ifnull(c.dblSettlementAmt,0.00) as settleAmt, "
                    + " ifnull(e.strCustomerName,'') as CustomerName "
                    + " from tblbillhd  a "
                    + " left outer join tblbillsettlementdtl c on a.strBillNo=c.strBillNo  and date(a.dteBillDate)=date(c.dteBillDate) "
                    + " left outer join tblsettelmenthd d on c.strSettlementCode=d.strSettelmentCode "
                    + " left outer join tblcustomermaster e on a.strCustomerCode=e.strCustomerCode "
                    + " where date(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' ";

            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlLive += " and a.strPOSCode= '" + posCode + "' ";
            }
            if (!shiftNo.equalsIgnoreCase("All"))
            {
                sqlLive += " and a.intShiftCode= '" + shiftNo + "' ";
            }

            sqlLive += " Order By d.strSettelmentDesc";

            sqlQFile = " select a.strBillNo,ifnull(d.strSettelmentDesc,'ND') as payMode,ifnull(a.dblSubTotal,0.00) as subTotal,"
                    + " a.dblTaxAmt,a.dblDiscountAmt,ifnull(c.dblSettlementAmt,0.00) as settleAmt, "
                    + " ifnull(e.strCustomerName,'') as CustomerName "
                    + " from tblqbillhd  a "
                    + " left outer join tblqbillsettlementdtl c on a.strBillNo=c.strBillNo  and date(a.dteBillDate)=date(c.dteBillDate) "
                    + " left outer join tblsettelmenthd d on c.strSettlementCode=d.strSettelmentCode "
                    + " left outer join tblcustomermaster e on a.strCustomerCode=e.strCustomerCode "
                    + " where date(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' ";

            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlQFile += " and a.strPOSCode= '" + posCode + "' ";
            }
            if (!shiftNo.equalsIgnoreCase("All"))
            {
                sqlQFile += " and a.intShiftCode= '" + shiftNo + "' ";
            }

            sqlQFile += " Order By d.strSettelmentDesc";

            Map<String, Map<String, clsOperatorDtl>> hmOperatorWiseSales = new HashMap<String, Map<String, clsOperatorDtl>>();
            Map<String, clsOperatorDtl> hmSettlementDtl = null;
            clsOperatorDtl objOperatorWiseSales = null;

            ResultSet rsOperator = clsGlobalVarClass.dbMysql.executeResultSet(sqlLive);
            while (rsOperator.next())
            {
                if (hmOperatorWiseSales.containsKey(rsOperator.getString(1)))
                {
                    hmSettlementDtl = hmOperatorWiseSales.get(rsOperator.getString(1));
                    if (hmSettlementDtl.containsKey(rsOperator.getString(2)))
                    {
                        objOperatorWiseSales = hmSettlementDtl.get(rsOperator.getString(2));
                        objOperatorWiseSales.setSettleAmt(objOperatorWiseSales.getSettleAmt() + rsOperator.getDouble(6));
                    }
                    else
                    {
                        objOperatorWiseSales = new clsOperatorDtl();
                        objOperatorWiseSales.setStrUserCode(rsOperator.getString(1));
                        objOperatorWiseSales.setStrSettlementDesc(rsOperator.getString(2));
                        objOperatorWiseSales.setStrUserName(rsOperator.getString(7));
                        objOperatorWiseSales.setStrPOSName(String.valueOf(0));
                        objOperatorWiseSales.setStrPOSCode(String.valueOf(0));
                        objOperatorWiseSales.setDiscountAmt(0);
                        objOperatorWiseSales.setSettleAmt(rsOperator.getDouble(6));
                    }
                    hmSettlementDtl.put(rsOperator.getString(2), objOperatorWiseSales);
                }
                else
                {

                    objOperatorWiseSales = new clsOperatorDtl();
                    objOperatorWiseSales.setStrUserCode(rsOperator.getString(1));
                    objOperatorWiseSales.setStrSettlementDesc(rsOperator.getString(2));
                    objOperatorWiseSales.setStrUserName(rsOperator.getString(7));
                    objOperatorWiseSales.setStrPOSName(String.valueOf(0));
                    objOperatorWiseSales.setStrPOSCode(String.valueOf(0));
                    objOperatorWiseSales.setDiscountAmt(0);
                    objOperatorWiseSales.setSettleAmt(rsOperator.getDouble(6));

                    hmSettlementDtl = new HashMap<String, clsOperatorDtl>();
                    hmSettlementDtl.put(rsOperator.getString(2), objOperatorWiseSales);
                }
                hmOperatorWiseSales.put(rsOperator.getString(1), hmSettlementDtl);
            }
            rsOperator.close();

            rsOperator = clsGlobalVarClass.dbMysql.executeResultSet(sqlQFile);
            while (rsOperator.next())
            {
                if (hmOperatorWiseSales.containsKey(rsOperator.getString(1)))
                {
                    hmSettlementDtl = hmOperatorWiseSales.get(rsOperator.getString(1));
                    if (hmSettlementDtl.containsKey(rsOperator.getString(2)))
                    {
                        objOperatorWiseSales = hmSettlementDtl.get(rsOperator.getString(2));
                        objOperatorWiseSales.setSettleAmt(objOperatorWiseSales.getSettleAmt() + rsOperator.getDouble(6));
                    }
                    else
                    {
                        objOperatorWiseSales = new clsOperatorDtl();
                        objOperatorWiseSales.setStrUserCode(rsOperator.getString(1));
                        objOperatorWiseSales.setStrSettlementDesc(rsOperator.getString(2));
                        objOperatorWiseSales.setStrUserName(rsOperator.getString(7));
                        objOperatorWiseSales.setStrPOSName(String.valueOf(0));
                        objOperatorWiseSales.setStrPOSCode(String.valueOf(0));
                        objOperatorWiseSales.setDiscountAmt(0);
                        objOperatorWiseSales.setSettleAmt(rsOperator.getDouble(6));
                    }
                    hmSettlementDtl.put(rsOperator.getString(2), objOperatorWiseSales);
                }
                else
                {

                    objOperatorWiseSales = new clsOperatorDtl();
                    objOperatorWiseSales.setStrUserCode(rsOperator.getString(1));
                    objOperatorWiseSales.setStrSettlementDesc(rsOperator.getString(2));
                    objOperatorWiseSales.setStrUserName(rsOperator.getString(7));
                    objOperatorWiseSales.setStrPOSName(String.valueOf(0));
                    objOperatorWiseSales.setStrPOSCode(String.valueOf(0));
                    objOperatorWiseSales.setDiscountAmt(0);
                    objOperatorWiseSales.setSettleAmt(rsOperator.getDouble(6));

                    hmSettlementDtl = new HashMap<String, clsOperatorDtl>();
                    hmSettlementDtl.put(rsOperator.getString(2), objOperatorWiseSales);
                }
                hmOperatorWiseSales.put(rsOperator.getString(1), hmSettlementDtl);
            }
            rsOperator.close();

            sbSqlDisLive.append("SELECT b.strBillNo, b.strPOSCode, c.strPOSName "
                    + ",sum(b.dblSubTotal),sum(b.dblDiscountAmt),sum(b.dblTaxAmt),'SANGUINE' "
                    + " FROM tblbillhd b "
                    + " inner join tblposmaster c on b.strPOSCode=c.strPOSCode  "
                    + " WHERE date( b.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' ");

            sbSqlQDisFile.append(" SELECT b.strBillNo, b.strPOSCode, c.strPOSName"
                    + ",sum(b.dblSubTotal),sum(b.dblDiscountAmt),sum(b.dblTaxAmt),'SANGUINE' "
                    + " FROM tblqbillhd b "
                    + " inner join tblposmaster c on b.strPOSCode=c.strPOSCode "
                    + " WHERE date( b.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' ");

            if (!posCode.equalsIgnoreCase("All"))
            {
                sbSqlDisFilters.append(" AND b.strPOSCode = '" + posCode + "' ");
            }
            if (!shiftNo.equalsIgnoreCase("All"))
            {
                sbSqlDisFilters.append(" AND b.intShiftCode = '" + shiftNo + "' ");
            }

            sbSqlDisFilters.append(" GROUP BY b.strBillNo, b.strPosCode");

            sbSqlDisLive.append(sbSqlDisFilters);
            sbSqlQDisFile.append(sbSqlDisFilters);

            double dis = 0;

            ResultSet rsOperatorDis = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlDisLive.toString());
            while (rsOperatorDis.next())
            {
                if (hmOperatorWiseSales.containsKey(rsOperatorDis.getString(1)))
                {
                    hmSettlementDtl = hmOperatorWiseSales.get(rsOperatorDis.getString(1));
                    Set<String> setKeys = hmSettlementDtl.keySet();
                    for (String keys : setKeys)
                    {
                        objOperatorWiseSales = hmSettlementDtl.get(keys);
                        objOperatorWiseSales.setStrPOSName(String.valueOf(objOperatorWiseSales.getStrPOSName() + rsOperatorDis.getDouble(4)));
                        objOperatorWiseSales.setStrPOSCode(String.valueOf(objOperatorWiseSales.getStrPOSCode() + rsOperatorDis.getDouble(6)));
                        dis = objOperatorWiseSales.getDiscountAmt();
                        objOperatorWiseSales.setDiscountAmt(dis + rsOperatorDis.getDouble(5));
                        hmSettlementDtl.put(keys, objOperatorWiseSales);
                        break;
                    }
                    hmOperatorWiseSales.put(rsOperatorDis.getString(1), hmSettlementDtl);
                }
            }
            rsOperatorDis.close();

            rsOperatorDis = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQDisFile.toString());
            while (rsOperatorDis.next())
            {
                if (hmOperatorWiseSales.containsKey(rsOperatorDis.getString(1)))
                {
                    hmSettlementDtl = hmOperatorWiseSales.get(rsOperatorDis.getString(1));
                    Set<String> setKeys = hmSettlementDtl.keySet();
                    for (String keys : setKeys)
                    {
                        objOperatorWiseSales = hmSettlementDtl.get(keys);
                        objOperatorWiseSales.setStrPOSName(String.valueOf(objOperatorWiseSales.getStrPOSName() + rsOperatorDis.getDouble(4)));
                        objOperatorWiseSales.setStrPOSCode(String.valueOf(objOperatorWiseSales.getStrPOSCode() + rsOperatorDis.getDouble(6)));
                        dis = objOperatorWiseSales.getDiscountAmt();
                        objOperatorWiseSales.setDiscountAmt(dis + rsOperatorDis.getDouble(5));
                        hmSettlementDtl.put(keys, objOperatorWiseSales);
                        break;
                    }
                    hmOperatorWiseSales.put(rsOperatorDis.getString(1), hmSettlementDtl);
                }
            }
            rsOperatorDis.close();
            int i = 0;
            for (Map.Entry<String, Map<String, clsOperatorDtl>> entry : hmOperatorWiseSales.entrySet())
            {
                Map<String, clsOperatorDtl> hmOpSettlementDtl = entry.getValue();
                for (Map.Entry<String, clsOperatorDtl> entryOp : hmOpSettlementDtl.entrySet())
                {
                    clsOperatorDtl objOperatorDtl = entryOp.getValue();
                    listOperatorDtl.add(objOperatorDtl);
                }
            }

            sqlLive = " select ifnull(d.strSettelmentDesc,'') as payMode "
                    + " ,sum(c.dblSettlementAmt) "
                    + " from tblbillhd a "
                    + " left outer join tblbillsettlementdtl c on a.strBillNo=c.strBillNo and date(a.dteBillDate)=date(c.dteBillDate) "
                    + " left outer join tblsettelmenthd d on c.strSettlementCode=d.strSettelmentCode "
                    + " where date(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' ";

            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlLive += " and a.strPOSCode= '" + posCode + "' ";
            }
            if (!shiftNo.equalsIgnoreCase("All"))
            {
                sqlLive += " and a.intShiftCode= '" + shiftNo + "' ";
            }

            sqlLive += " Group By d.strSettelmentDesc ";

            sqlQFile = " select ifnull(d.strSettelmentDesc,'') as payMode "
                    + " ,sum(c.dblSettlementAmt) "
                    + " from tblqbillhd a "
                    + " left outer join tblqbillsettlementdtl c on a.strBillNo=c.strBillNo and date(a.dteBillDate)=date(c.dteBillDate) "
                    + " left outer join tblsettelmenthd d on c.strSettlementCode=d.strSettelmentCode "
                    + " where date(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' ";

            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlQFile += " and a.strPOSCode= '" + posCode + "' ";
            }
            if (!shiftNo.equalsIgnoreCase("All"))
            {
                sqlQFile += " and a.intShiftCode= '" + shiftNo + "' ";
            }

            sqlQFile += " Group By d.strSettelmentDesc ";

            int previousListIndex = 0;
            List<clsOperatorDtl> listSettleDetail = new ArrayList<>();
            rsOperator = clsGlobalVarClass.dbMysql.executeResultSet(sqlLive);
            while (rsOperator.next())
            {
                boolean flgFound = false;
                if (!rsOperator.getString(1).isEmpty())
                {
                    objOperatorWiseSales = new clsOperatorDtl();
                    if (listSettleDetail.size() > 0)
                    {
                        for (int cnt = 0; cnt < listSettleDetail.size(); cnt++)
                        {
                            clsOperatorDtl objPreviousList = listSettleDetail.get(cnt);
                            if (objPreviousList.getStrSettlementDesc().equals(rsOperator.getString(1)))
                            {
                                double settleAmount = objPreviousList.getSettleAmt() + rsOperator.getDouble(2);
                                objOperatorWiseSales.setStrSettlementDesc(rsOperator.getString(1));
                                objOperatorWiseSales.setSettleAmt(settleAmount);
                                flgFound = true;
                                previousListIndex = cnt;
                            }
                        }

                    }
                    if (flgFound)
                    {
                        listSettleDetail.remove(previousListIndex);
                        listSettleDetail.add(objOperatorWiseSales);
                    }
                    else
                    {
                        objOperatorWiseSales.setStrSettlementDesc(rsOperator.getString(1));
                        objOperatorWiseSales.setSettleAmt(rsOperator.getDouble(2));
                        listSettleDetail.add(objOperatorWiseSales);
                    }
                }

            }
            rsOperator.close();

            rsOperator = clsGlobalVarClass.dbMysql.executeResultSet(sqlQFile);
            while (rsOperator.next())
            {
                boolean flgFound = false;
                if (!rsOperator.getString(1).isEmpty())
                {
                    objOperatorWiseSales = new clsOperatorDtl();
                    if (listSettleDetail.size() > 0)
                    {
                        for (int cnt = 0; cnt < listSettleDetail.size(); cnt++)
                        {
                            clsOperatorDtl objPreviousList = listSettleDetail.get(cnt);
                            if (objPreviousList.getStrSettlementDesc().equals(rsOperator.getString(1)))
                            {
                                double settleAmount = objPreviousList.getSettleAmt() + rsOperator.getDouble(2);
                                objOperatorWiseSales.setStrSettlementDesc(rsOperator.getString(1));
                                objOperatorWiseSales.setSettleAmt(settleAmount);
                                flgFound = true;
                                previousListIndex = cnt;

                            }
                        }

                    }
                    if (flgFound)
                    {
                        listSettleDetail.remove(previousListIndex);
                        listSettleDetail.add(objOperatorWiseSales);
                    }
                    else
                    {
                        objOperatorWiseSales.setStrSettlementDesc(rsOperator.getString(1));
                        objOperatorWiseSales.setSettleAmt(rsOperator.getDouble(2));
                        listSettleDetail.add(objOperatorWiseSales);
                    }
                }
            }
            rsOperator.close();

            sqlLive = " select ifnull(a.strBillNo,'') "
                    + " from tblvoidbillhd a "
                    + " where date(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' ";

            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlLive += " and a.strPosCode= '" + posCode + "' ";
            }
            if (!shiftNo.equalsIgnoreCase("All"))
            {
                sqlLive += " and a.intShiftCode= '" + shiftNo + "' ";
            }

            sqlLive += " Order By a.strBillNo ";

            List<clsOperatorDtl> listVoidBillDetail = new ArrayList<>();
            rsOperator = clsGlobalVarClass.dbMysql.executeResultSet(sqlLive);
            while (rsOperator.next())
            {
                objOperatorWiseSales = new clsOperatorDtl();
                objOperatorWiseSales.setStrUser(rsOperator.getString(1));
                listVoidBillDetail.add(objOperatorWiseSales);
            }
            rsOperator.close();

            Comparator<clsOperatorDtl> settleModeComparator = new Comparator<clsOperatorDtl>()
            {

                @Override
                public int compare(clsOperatorDtl o1, clsOperatorDtl o2)
                {
                    return o1.getStrSettlementDesc().compareTo(o2.getStrSettlementDesc());
                }
            };
            Comparator<clsOperatorDtl> billWiseComparator = new Comparator<clsOperatorDtl>()
            {

                @Override
                public int compare(clsOperatorDtl o1, clsOperatorDtl o2)
                {
                    return o1.getStrUserCode().compareTo(o2.getStrUserCode());
                }
            };

            Collections.sort(listOperatorDtl, new clsOperatorComparator(settleModeComparator, billWiseComparator));

            hm.put("listOfOperatorDtl", listOperatorDtl);
            hm.put("listOfBillSettleDtl", listSettleDetail);
            hm.put("listOfVoidBillDtl", listVoidBillDetail);

            List<List<clsOperatorDtl>> listData = new ArrayList<>();
            listData.add(listOperatorDtl);

            //call for view report
            funViewJasperReportForBeanCollectionDataSource(is, hm, listData);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void funWaiterWiseIncentivesReportForJasper()
    {

        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String reportType = hm.get("reportType").toString();

            if (reportType.equalsIgnoreCase("Summary"))
            {
                StringBuilder sqlBuilder = new StringBuilder();
                List<clsBillDtl> listOfWaiterWiseItemSales = new ArrayList<>();

                //Q Data
                sqlBuilder.setLength(0);
                sqlBuilder.append("select e.strWaiterNo,ifnull(e.strWShortName,'ND')strWShortName,sum(b.dblQuantity)dblQuantity,sum(b.dblAmount)dblAmount,"
                        + "round(sum(b.dblAmount)*(d.strIncentives/100),2)dblIncentives "
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
                        + "order by e.strWaiterNo ");

                ResultSet rsWaiterWiseItemSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
                while (rsWaiterWiseItemSales.next())
                {
                    clsBillDtl obj = new clsBillDtl();

                    obj.setStrWaiterNo(rsWaiterWiseItemSales.getString(1));
                    obj.setStrWShortName(rsWaiterWiseItemSales.getString(2));
                    obj.setDblQuantity(rsWaiterWiseItemSales.getDouble(3));
                    obj.setDblAmount(rsWaiterWiseItemSales.getDouble(4));
                    obj.setDblIncentive(rsWaiterWiseItemSales.getDouble(5));

                    listOfWaiterWiseItemSales.add(obj);
                }
                rsWaiterWiseItemSales.close();

                //Live Data
                sqlBuilder.setLength(0);
                sqlBuilder.append("select e.strWaiterNo,ifnull(e.strWShortName,'ND')strWShortName,sum(b.dblQuantity)dblQuantity,sum(b.dblAmount)dblAmount,"
                        + "round(sum(b.dblAmount)*(d.strIncentives/100),2)dblIncentives "
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
                        + "order by e.strWaiterNo ");

                rsWaiterWiseItemSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
                while (rsWaiterWiseItemSales.next())
                {
                    clsBillDtl obj = new clsBillDtl();

                    obj.setStrWaiterNo(rsWaiterWiseItemSales.getString(1));
                    obj.setStrWShortName(rsWaiterWiseItemSales.getString(2));
                    obj.setDblQuantity(rsWaiterWiseItemSales.getDouble(3));
                    obj.setDblAmount(rsWaiterWiseItemSales.getDouble(4));
                    obj.setDblIncentive(rsWaiterWiseItemSales.getDouble(5));

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

                Collections.sort(listOfWaiterWiseItemSales, new clsWaiterWiseSalesComparator(waiterCodeComparator));
                //call for view report
                funViewJasperReportForBeanCollectionDataSource(is, hm, listOfWaiterWiseItemSales);
            }
            else
            {
                StringBuilder sqlBuilder = new StringBuilder();
                List<clsBillDtl> listOfWaiterWiseItemSales = new ArrayList<>();

                //Q Data
                sqlBuilder.setLength(0);
                sqlBuilder.append("select e.strWaiterNo,ifnull(e.strWShortName,'ND')strWShortName,d.strSubGroupCode,d.strSubGroupName,a.strBillNo "
                        + ",sum(b.dblQuantity)dblQuantity,sum(b.dblAmount)dblAmount, "
                        + "round(sum(b.dblAmount)*(d.strIncentives/100),2)dblIncentives,round(d.strIncentives,2) as strIncentivePer "
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
                        + "order by e.strWaiterNo,a.strBillNo ");

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

                    listOfWaiterWiseItemSales.add(obj);
                }
                rsWaiterWiseItemSales.close();

                //Live Data
                sqlBuilder.setLength(0);
                sqlBuilder.append("select e.strWaiterNo,ifnull(e.strWShortName,'ND')strWShortName,d.strSubGroupCode,d.strSubGroupName,a.strBillNo "
                        + ",sum(b.dblQuantity)dblQuantity,sum(b.dblAmount)dblAmount, "
                        + "round(sum(b.dblAmount)*(d.strIncentives/100),2)dblIncentives,round(d.strIncentives,2) as strIncentivePer "
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
                        + "order by e.strWaiterNo,a.strBillNo ");

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
                funViewJasperReportForBeanCollectionDataSource(is, hm, listOfWaiterWiseItemSales);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

   /* public void funVoidKOTReportForJasper()
    {
        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();

            StringBuilder sqlBuilder = new StringBuilder();

            //KOT data
            sqlBuilder.setLength(0);
            sqlBuilder.append("select a.strItemCode,a.strItemName,ifnull(d.strTableName,''),"
                    + " (a.dblAmount/a.dblItemQuantity) as dblRate,sum(a.dblItemQuantity)as dblItemQuantity,sum(a.dblAmount) as dblAmount "
                    + " ,a.strRemark,a.strKOTNo,a.strPosCode,b.strPosName,a.strUserCreated ,a.dteVoidedDate,ifnull(c.strReasonName,'')"
                    + ",ifnull(e.strWShortName,'')  "
                    + " from tblvoidkot a "
                    + " left outer join tblposmaster b on a.strPOSCode=b.strPosCode "
                    + " left outer join tblreasonmaster c on a.strReasonCode=c.strReasonCode "
                    + " left outer join tbltablemaster d on a.strTableNo=d.strTableNo"
                    + " left outer join tblwaitermaster e on a.strWaiterNo=e.strWaiterNo "
                    + " where date(a.dteVoidedDate) Between '" + fromDate + "' and '" + toDate + "' ");
            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and a.strPosCode='" + posCode + "' ");
            }
            sqlBuilder.append(" group by a.strposcode,a.strusercreated,a.strkotno,a.strItemCode "
                    + " order by a.strposcode,a.strusercreated,a.strkotno,a.strItemCode ");

            ResultSet rsVoidData = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
            List<clsVoidBillDtl> listOfVoidKOTData = new ArrayList<clsVoidBillDtl>();
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

                listOfVoidKOTData.add(objVoidBill);
            }
            rsVoidData.close();
            //call for view report
            funViewJasperReportForBeanCollectionDataSource(is, hm, listOfVoidKOTData);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
  */  
    
    
    
    public void funVoidKOTReportForJasper()
    {
        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();

            StringBuilder sqlBuilder = new StringBuilder();

            //KOT data
            sqlBuilder.setLength(0);
            sqlBuilder.append("select a.strItemCode,a.strItemName,ifnull(d.strTableName,''),"
                    + " (a.dblAmount/a.dblItemQuantity) as dblRate,sum(a.dblItemQuantity)as dblItemQuantity,sum(a.dblAmount) as dblAmount "
                    + " ,a.strRemark,a.strKOTNo,a.strPosCode,b.strPosName,a.strUserCreated ,a.dteVoidedDate,ifnull(c.strReasonName,'')"
                    + ",ifnull(e.strWShortName,''),a.strVoidBillType  "
                    + " from tblvoidkot a "
                    + " left outer join tblposmaster b on a.strPOSCode=b.strPosCode "
                    + " left outer join tblreasonmaster c on a.strReasonCode=c.strReasonCode "
                    + " left outer join tbltablemaster d on a.strTableNo=d.strTableNo"
                    + " left outer join tblwaitermaster e on a.strWaiterNo=e.strWaiterNo "
                    + " where date(a.dteVoidedDate) Between '" + fromDate + "' and '" + toDate + "' ");
            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and a.strPosCode='" + posCode + "' ");
            }
            sqlBuilder.append(" group by a.strposcode,a.strusercreated,a.strkotno,a.strItemCode "
                    + " order by a.strposcode,a.strusercreated,a.strkotno,a.strItemCode ");

            ResultSet rsVoidData = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
            List<clsVoidBillDtl> listOfVoidKOTData = new ArrayList<clsVoidBillDtl>();
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
                
                listOfVoidKOTData.add(objVoidBill);
            }
            rsVoidData.close();
            //call for view report
            funViewJasperReportForBeanCollectionDataSource(is, hm, listOfVoidKOTData);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void funUnusedCardBalanceReportForJasper()
    {

        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();

            StringBuilder sqlBuilder = new StringBuilder();
            List<clsGenericBean> listOfUnUsedBalance = new ArrayList<>();

            //Q Data
            sqlBuilder.setLength(0);
            sqlBuilder.append("select DATE_FORMAT(date(a.dtePOSDate),'%d-%m-%Y'),ifnull(sum(a.dblCardAmt),0.00) as dblUnUsedBalance "
                    + "from tbldebitcardrevenue a "
                    + "where date(a.dtePOSDate) between '" + fromDate + "' and '" + toDate + "' "
                    + "group by a.dtePOSDate "
                    + "order by a.dtePOSDate ");
            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and a.strPOSCode='" + posCode + "' ");
            }
//                if (clsGlobalVarClass.gEnableShiftYN && (!cmbShiftNo.getSelectedItem().toString().equalsIgnoreCase("All")))
//                {
//                    sqlBuilder.append("and c.intShiftCode='" + shiftNo + "' ");
//                }                

            ResultSet rsWaiterWiseItemSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
            while (rsWaiterWiseItemSales.next())
            {
                clsGenericBean obj = new clsGenericBean();
                obj.setDtePOSDate(rsWaiterWiseItemSales.getString(1));
                obj.setDblUnUsedBalance(rsWaiterWiseItemSales.getDouble(2));

                listOfUnUsedBalance.add(obj);
            }
            rsWaiterWiseItemSales.close();
            //call for view report
            funViewJasperReportForBeanCollectionDataSource(is, hm, listOfUnUsedBalance);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void funPostingReportForJasper()
    {
        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();

            int count = 0;
            ////For Settlement details of Live and Q data
            StringBuilder sqlQData = new StringBuilder();
            StringBuilder sqlModQData = new StringBuilder();

            sqlQData.append(" select c.strSettelmentDesc,c.strSettelmentType,sum(b.dblSettlementAmt)+sum(a.dblTipAmount) "
                    + " from tblqbillhd a,tblqbillsettlementdtl b,tblsettelmenthd c "
                    + " where a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " and b.strSettlementCode=c.strSettelmentCode  "
                    + " and c.strSettelmentType!='Complementary' "
                    + " and c.strSettelmentType!='Credit' "
                    + " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
            if (!posCode.equals("All"))
            {
                sqlQData.append(" and a.strPOSCode='" + posCode + "' ");
            }
            sqlQData.append(" group by c.strSettelmentCode ");

            Map<String, List<clsSettelementOptions>> hmSalesSettleData = new HashMap<String, List<clsSettelementOptions>>();
            List<clsSettelementOptions> arrListSettleData = null;

            ResultSet rsSettlementWiseQData = clsGlobalVarClass.dbMysql.executeResultSet(sqlQData.toString());
            while (rsSettlementWiseQData.next())
            {
                clsSettelementOptions objSettle = new clsSettelementOptions();
                if (hmSalesSettleData.containsKey(rsSettlementWiseQData.getString(2)))
                {
                    arrListSettleData = hmSalesSettleData.get(rsSettlementWiseQData.getString(2));
                    for (int j = 0; j < arrListSettleData.size(); j++)
                    {
                        objSettle = arrListSettleData.get(j);
                        if (objSettle.getStrSettelmentDesc().equals(rsSettlementWiseQData.getString(1)))
                        {
                            arrListSettleData.remove(objSettle);
                            double settleAmt = objSettle.getDblSettlementAmt();
                            objSettle.setDblSettlementAmt(settleAmt + rsSettlementWiseQData.getDouble(3));
                        }
                        else
                        {
                            objSettle = new clsSettelementOptions();
                            objSettle.setDblSettlementAmt(rsSettlementWiseQData.getDouble(3));
                            objSettle.setStrSettelmentDesc(rsSettlementWiseQData.getString(1));
                            objSettle.setStrSettelmentType(rsSettlementWiseQData.getString(2));
                        }
                    }
                }
                else
                {
                    arrListSettleData = new ArrayList<clsSettelementOptions>();
                    objSettle.setStrSettelmentDesc(rsSettlementWiseQData.getString(1));
                    objSettle.setStrSettelmentType(rsSettlementWiseQData.getString(2));
                    objSettle.setDblSettlementAmt(rsSettlementWiseQData.getDouble(3));
                }
                arrListSettleData.add(objSettle);
                hmSalesSettleData.put(rsSettlementWiseQData.getString(2), arrListSettleData);
            }
            rsSettlementWiseQData.close();

            List<clsSettelementOptions> arrListCreditSettleData = null;
            sqlQData.setLength(0);
            sqlQData.append(" select ifnull(d.strCustomerName,'NA'),c.strSettelmentType,sum(b.dblSettlementAmt)+sum(a.dblTipAmount) "
                    + " from tblqbillhd a "
                    + " inner join tblqbillsettlementdtl b on a.strBillNo=b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " left outer join tblcustomermaster d on b.strCustomerCode=d.strCustomerCode "
                    + " inner join tblsettelmenthd c on b.strSettlementCode=c.strSettelmentCode "
                    + " where c.strSettelmentType='Credit' "
                    + " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
            if (!posCode.equals("All"))
            {
                sqlQData.append(" and a.strPOSCode='" + posCode + "' ");
            }
            sqlQData.append(" group by d.strCustomerCode order by d.strCustomerName ");
            String key = "Credit";
            rsSettlementWiseQData = clsGlobalVarClass.dbMysql.executeResultSet(sqlQData.toString());
            while (rsSettlementWiseQData.next())
            {
                clsSettelementOptions objSettle = new clsSettelementOptions();
                if (hmSalesSettleData.containsKey(key))
                {
                    arrListCreditSettleData = hmSalesSettleData.get(key);
                    for (int j = 0; j < arrListCreditSettleData.size(); j++)
                    {
                        objSettle = arrListCreditSettleData.get(j);
                        if (objSettle.getStrSettelmentDesc().equals(rsSettlementWiseQData.getString(1)))
                        {
                            arrListCreditSettleData.remove(objSettle);
                            double settleAmt = objSettle.getDblSettlementAmt();
                            objSettle.setDblSettlementAmt(settleAmt + rsSettlementWiseQData.getDouble(3));
                        }
                        else
                        {
                            objSettle = new clsSettelementOptions();
                            objSettle.setDblSettlementAmt(rsSettlementWiseQData.getDouble(3));
                            objSettle.setStrSettelmentDesc(rsSettlementWiseQData.getString(1));
                            objSettle.setStrSettelmentType(rsSettlementWiseQData.getString(2));
                        }
                    }
                }
                else
                {
                    arrListCreditSettleData = new ArrayList<clsSettelementOptions>();
                    objSettle.setStrSettelmentDesc(rsSettlementWiseQData.getString(1));
                    objSettle.setStrSettelmentType(rsSettlementWiseQData.getString(2));
                    objSettle.setDblSettlementAmt(rsSettlementWiseQData.getDouble(3));
                }
                arrListCreditSettleData.add(objSettle);
                hmSalesSettleData.put(key, arrListCreditSettleData);
            }
            rsSettlementWiseQData.close();

            //For Discount details of Live and Q data
            sqlQData.setLength(0);
            sqlQData.append(" select sum(b.dblDiscAmt),a.strBillNo  from tblqbillhd a,tblqbilldiscdtl b "
                    + " where a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
            if (!posCode.equals("All"))
            {

                sqlQData.append(" and a.strPOSCode='" + posCode + "' ");
            }
            sqlQData.append(" group by a.strBillNo");

            double finalDisAmt = 0;
            ResultSet rsDisQData = clsGlobalVarClass.dbMysql.executeResultSet(sqlQData.toString());
            while (rsDisQData.next())
            {
                finalDisAmt = finalDisAmt + rsDisQData.getDouble(1);
            }
            rsDisQData.close();

            //For groupwise sala data
            sqlQData.setLength(0);
            sqlModQData.setLength(0);

            sqlQData.append(" select e.strGroupName,SUM(b.dblAmount)-sum(b.dblDiscountAmt),e.strGroupCode,a.strOperationType "
                    + " from tblqbillhd a,tblqbilldtl b,tblitemmaster c,tblsubgrouphd d,tblgrouphd e "
                    + " where a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " and b.strItemCode=c.strItemCode "
                    + " and c.strSubGroupCode=d.strSubGroupCode "
                    + " and d.strGroupCode=e.strGroupCode "
                    + " and a.strClientCode=b.strClientCode  "
                    + " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");

            sqlModQData.append(" select e.strGroupName,SUM(b.dblAmount)-sum(b.dblDiscAmt),e.strGroupCode,a.strOperationType "
                    + " from tblqbillhd a,tblqbillmodifierdtl b,tblitemmaster c,tblsubgrouphd d,tblgrouphd e "
                    + " where a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " and left(b.strItemCode,7)=c.strItemCode "
                    + " and c.strSubGroupCode=d.strSubGroupCode "
                    + " and d.strGroupCode=e.strGroupCode "
                    + "and a.strClientCode=b.strClientCode "
                    + " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");

            if (!posCode.equals("All"))
            {
                sqlQData.append(" and a.strPOSCode='" + posCode + "' ");
                sqlModQData.append(" and a.strPOSCode='" + posCode + "' ");
            }
            sqlQData.append(" group by a.strOperationType,e.strGroupCode");
            sqlModQData.append(" group by a.strOperationType,e.strGroupCode");

            Map<String, List<clsSettelementOptions>> hmSalesGroupWiseSaleData = new HashMap<String, List<clsSettelementOptions>>();
            List<clsSettelementOptions> arrListGroupwiseSaleData = null;

            Map<String, Double> mapDineIn = new HashMap<>();
            Map<String, Double> mapTakeAway = new HashMap<>();
            Map<String, Double> mapHomeDel = new HashMap<>();

            ResultSet rsGroupwiseSaleQData = clsGlobalVarClass.dbMysql.executeResultSet(sqlQData.toString());

            while (rsGroupwiseSaleQData.next())
            {

                clsSettelementOptions objDineIn = new clsSettelementOptions();
                objDineIn.setDblSettlementAmt(rsGroupwiseSaleQData.getDouble(2));
                objDineIn.setStrSettelmentDesc(rsGroupwiseSaleQData.getString(1));
                objDineIn.setStrSettelmentType(rsGroupwiseSaleQData.getString(3));
                objDineIn.setStrRemark(rsGroupwiseSaleQData.getString(4));

                if (rsGroupwiseSaleQData.getString(4).equalsIgnoreCase("DineIn") || rsGroupwiseSaleQData.getString(4).equalsIgnoreCase("DirectBiller"))
                {
                    if (mapDineIn.containsKey(rsGroupwiseSaleQData.getString(1)))
                    {
                        mapDineIn.put(rsGroupwiseSaleQData.getString(1), mapDineIn.get(rsGroupwiseSaleQData.getString(1)) + rsGroupwiseSaleQData.getDouble(2));
                    }
                    else
                    {
                        mapDineIn.put(rsGroupwiseSaleQData.getString(1), rsGroupwiseSaleQData.getDouble(2));
                    }
                }
                else if (rsGroupwiseSaleQData.getString(4).equalsIgnoreCase("TakeAway"))
                {
                    if (mapTakeAway.containsKey(rsGroupwiseSaleQData.getString(1)))
                    {
                        mapTakeAway.put(rsGroupwiseSaleQData.getString(1), mapTakeAway.get(rsGroupwiseSaleQData.getString(1)) + rsGroupwiseSaleQData.getDouble(2));
                    }
                    else
                    {
                        mapTakeAway.put(rsGroupwiseSaleQData.getString(1), rsGroupwiseSaleQData.getDouble(2));
                    }
                }
                else if (rsGroupwiseSaleQData.getString(4).equalsIgnoreCase("HomeDelivery"))
                {
                    if (mapHomeDel.containsKey(rsGroupwiseSaleQData.getString(1)))
                    {
                        mapHomeDel.put(rsGroupwiseSaleQData.getString(1), mapHomeDel.get(rsGroupwiseSaleQData.getString(1)) + rsGroupwiseSaleQData.getDouble(2));
                    }
                    else
                    {
                        mapHomeDel.put(rsGroupwiseSaleQData.getString(1), rsGroupwiseSaleQData.getDouble(2));
                    }
                }

                clsSettelementOptions objSettle = new clsSettelementOptions();
                if (hmSalesGroupWiseSaleData.containsKey(rsGroupwiseSaleQData.getString(3)))
                {
                    arrListGroupwiseSaleData = hmSalesGroupWiseSaleData.get(rsGroupwiseSaleQData.getString(3));
                    for (int j = 0; j < arrListGroupwiseSaleData.size(); j++)
                    {
                        objSettle = arrListGroupwiseSaleData.get(j);
                        if (objSettle.getStrSettelmentDesc().equals(rsGroupwiseSaleQData.getString(1)))
                        {
                            arrListGroupwiseSaleData.remove(objSettle);
                            double settleAmt = objSettle.getDblSettlementAmt();
                            objSettle.setDblSettlementAmt(settleAmt + rsGroupwiseSaleQData.getDouble(2));
                        }
                        else
                        {
                            objSettle = new clsSettelementOptions();
                            objSettle.setDblSettlementAmt(rsGroupwiseSaleQData.getDouble(2));
                            objSettle.setStrSettelmentDesc(rsGroupwiseSaleQData.getString(1));
                            objSettle.setStrSettelmentType(rsGroupwiseSaleQData.getString(3));
                            objSettle.setStrRemark(rsGroupwiseSaleQData.getString(4));
                        }
                    }
                }
                else
                {
                    arrListGroupwiseSaleData = new ArrayList<clsSettelementOptions>();
                    objSettle.setStrSettelmentDesc(rsGroupwiseSaleQData.getString(1));
                    objSettle.setStrSettelmentType(rsGroupwiseSaleQData.getString(3));
                    objSettle.setDblSettlementAmt(rsGroupwiseSaleQData.getDouble(2));
                    objSettle.setStrRemark(rsGroupwiseSaleQData.getString(4));

                }
                arrListGroupwiseSaleData.add(objSettle);
                hmSalesGroupWiseSaleData.put(rsGroupwiseSaleQData.getString(3), arrListGroupwiseSaleData);
            }
            rsGroupwiseSaleQData.close();

            ResultSet rsGroupwiseSaleModQData = clsGlobalVarClass.dbMysql.executeResultSet(sqlModQData.toString());
            while (rsGroupwiseSaleModQData.next())
            {

                if (rsGroupwiseSaleModQData.getString(4).equalsIgnoreCase("DineIn") || rsGroupwiseSaleModQData.getString(4).equalsIgnoreCase("DirectBiller"))
                {
                    if (mapDineIn.containsKey(rsGroupwiseSaleModQData.getString(1)))
                    {
                        mapDineIn.put(rsGroupwiseSaleModQData.getString(1), mapDineIn.get(rsGroupwiseSaleModQData.getString(1)) + rsGroupwiseSaleModQData.getDouble(2));
                    }
                    else
                    {
                        mapDineIn.put(rsGroupwiseSaleModQData.getString(1), rsGroupwiseSaleModQData.getDouble(2));
                    }
                }
                else if (rsGroupwiseSaleModQData.getString(4).equalsIgnoreCase("TakeAway"))
                {
                    if (mapTakeAway.containsKey(rsGroupwiseSaleModQData.getString(1)))
                    {
                        mapTakeAway.put(rsGroupwiseSaleModQData.getString(1), mapTakeAway.get(rsGroupwiseSaleModQData.getString(1)) + rsGroupwiseSaleModQData.getDouble(2));
                    }
                    else
                    {
                        mapTakeAway.put(rsGroupwiseSaleModQData.getString(1), rsGroupwiseSaleModQData.getDouble(2));
                    }
                }
                else if (rsGroupwiseSaleModQData.getString(4).equalsIgnoreCase("HomeDelivery"))
                {
                    if (mapHomeDel.containsKey(rsGroupwiseSaleModQData.getString(1)))
                    {
                        mapHomeDel.put(rsGroupwiseSaleModQData.getString(1), mapHomeDel.get(rsGroupwiseSaleModQData.getString(1)) + rsGroupwiseSaleModQData.getDouble(2));
                    }
                    else
                    {
                        mapHomeDel.put(rsGroupwiseSaleModQData.getString(1), rsGroupwiseSaleModQData.getDouble(2));
                    }
                }

                clsSettelementOptions objSettle = new clsSettelementOptions();
                if (hmSalesGroupWiseSaleData.containsKey(rsGroupwiseSaleModQData.getString(3)))
                {
                    arrListGroupwiseSaleData = hmSalesGroupWiseSaleData.get(rsGroupwiseSaleModQData.getString(3));
                    for (int j = 0; j < arrListGroupwiseSaleData.size(); j++)
                    {
                        objSettle = arrListGroupwiseSaleData.get(j);
                        if (objSettle.getStrSettelmentDesc().equals(rsGroupwiseSaleModQData.getString(1)))
                        {
                            arrListGroupwiseSaleData.remove(objSettle);
                            double settleAmt = objSettle.getDblSettlementAmt();
                            objSettle.setDblSettlementAmt(settleAmt + rsGroupwiseSaleModQData.getDouble(2));
                        }
                        else
                        {
                            objSettle = new clsSettelementOptions();
                            objSettle.setDblSettlementAmt(rsGroupwiseSaleModQData.getDouble(2));
                            objSettle.setStrSettelmentDesc(rsGroupwiseSaleModQData.getString(1));
                            objSettle.setStrSettelmentType(rsGroupwiseSaleModQData.getString(3));
                            objSettle.setStrRemark(rsGroupwiseSaleModQData.getString(4));
                        }
                    }
                }
                else
                {
                    arrListGroupwiseSaleData = new ArrayList<clsSettelementOptions>();
                    objSettle.setStrSettelmentDesc(rsGroupwiseSaleModQData.getString(1));
                    objSettle.setStrSettelmentType(rsGroupwiseSaleModQData.getString(3));
                    objSettle.setDblSettlementAmt(rsGroupwiseSaleModQData.getDouble(2));
                    objSettle.setStrRemark(rsGroupwiseSaleModQData.getString(4));

                }
                arrListGroupwiseSaleData.add(objSettle);
                hmSalesGroupWiseSaleData.put(rsGroupwiseSaleModQData.getString(3), arrListGroupwiseSaleData);
            }
            rsGroupwiseSaleModQData.close();

            //For taxwise data details;
            sqlQData.setLength(0);

            sqlQData.append(" select c.strTaxCode,c.strTaxDesc,sum(b.dblTaxAmount),sum(b.dblTaxableAmount) "
                    + " from tblqbillhd a,tblqbilltaxdtl b,tbltaxhd c "
                    + " where a.strBillNo=b.strBillNo and b.strTaxCode=c.strTaxCode "
                    + " and a.strClientCode=b.strClientCode "
                    + " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
            if (!posCode.equals("All"))
            {
                sqlQData.append(" and a.strPOSCode='" + posCode + "' ");
            }
            sqlQData.append(" group by b.strTaxCode order by c.strTaxOnTax");

            Map<String, List<clsSettelementOptions>> hmSalesTaxWiseSaleData = new HashMap<String, List<clsSettelementOptions>>();
            List<clsSettelementOptions> arrListTaxwiseSaleData = null;

            ResultSet rsTaxwiseSaleQData = clsGlobalVarClass.dbMysql.executeResultSet(sqlQData.toString());
            while (rsTaxwiseSaleQData.next())
            {
                clsSettelementOptions objSettle = new clsSettelementOptions();
                if (hmSalesTaxWiseSaleData.containsKey(rsTaxwiseSaleQData.getString(1)))
                {
                    arrListTaxwiseSaleData = hmSalesTaxWiseSaleData.get(rsTaxwiseSaleQData.getString(1));
                    for (int j = 0; j < arrListTaxwiseSaleData.size(); j++)
                    {
                        objSettle = arrListTaxwiseSaleData.get(j);
                        if (objSettle.getStrSettelmentDesc().equals(rsTaxwiseSaleQData.getString(2)))
                        {
                            arrListTaxwiseSaleData.remove(objSettle);
                            double settleAmt = objSettle.getDblSettlementAmt();
                            objSettle.setDblSettlementAmt(settleAmt + rsTaxwiseSaleQData.getDouble(3));
                        }
                        else
                        {
                            objSettle = new clsSettelementOptions();
                            objSettle.setDblSettlementAmt(rsTaxwiseSaleQData.getDouble(3));
                            objSettle.setStrSettelmentDesc(rsTaxwiseSaleQData.getString(2));
                            objSettle.setStrSettelmentType(rsTaxwiseSaleQData.getString(1));
                        }
                    }
                }
                else
                {
                    arrListTaxwiseSaleData = new ArrayList<clsSettelementOptions>();
                    objSettle.setStrSettelmentDesc(rsTaxwiseSaleQData.getString(2));
                    objSettle.setStrSettelmentType(rsTaxwiseSaleQData.getString(1));
                    objSettle.setDblSettlementAmt(rsTaxwiseSaleQData.getDouble(3));

                }
                arrListTaxwiseSaleData.add(objSettle);
                hmSalesTaxWiseSaleData.put(rsTaxwiseSaleQData.getString(1), arrListTaxwiseSaleData);
            }
            rsTaxwiseSaleQData.close();

            sqlQData.setLength(0);

            sqlQData.append(" select sum(a.dblTipAmount) from tblqbillhd a "
                    + " where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
            if (!posCode.equals("All"))
            {
                sqlQData.append(" and a.strPOSCode='" + posCode + "' ");
            }
            sqlQData.append(" group by a.strBillNo");

            double finalTipAmt = 0;

            ResultSet rsTipQData = clsGlobalVarClass.dbMysql.executeResultSet(sqlQData.toString());
            while (rsTipQData.next())
            {
                finalTipAmt = finalTipAmt + rsTipQData.getDouble(1);
            }
            rsTipQData.close();

            double totalDebitAmt = 0, totalCreditAmt = 0;
            for (Map.Entry<String, List<clsSettelementOptions>> entry : hmSalesSettleData.entrySet())
            {
                double totalSaleAmt = 0;
                List<clsSettelementOptions> listOfSettleDataDtl = entry.getValue();
                for (int j = 0; j < listOfSettleDataDtl.size(); j++)
                {
                    clsSettelementOptions objSettle = listOfSettleDataDtl.get(j);
                    totalSaleAmt += objSettle.getDblSettlementAmt();
                }
                totalDebitAmt += totalSaleAmt;
            }

            //   totalDebitAmt += finalDisAmt;
            for (Map.Entry<String, List<clsSettelementOptions>> entry : hmSalesGroupWiseSaleData.entrySet())
            {
                double totalSaleAmt = 0;
                List<clsSettelementOptions> listOfGroupwiseSaleDataDtl = entry.getValue();
                for (int j = 0; j < listOfGroupwiseSaleDataDtl.size(); j++)
                {
                    clsSettelementOptions objSettle = listOfGroupwiseSaleDataDtl.get(j);
                    totalSaleAmt += objSettle.getDblSettlementAmt();
                }
                totalCreditAmt += totalSaleAmt;
            }
            for (Map.Entry<String, List<clsSettelementOptions>> entry : hmSalesTaxWiseSaleData.entrySet())
            {
                double totalSaleAmt = 0;
                List<clsSettelementOptions> listOfTaxwiseSaleDataDtl = entry.getValue();
                for (int j = 0; j < listOfTaxwiseSaleDataDtl.size(); j++)
                {
                    clsSettelementOptions objSettle = listOfTaxwiseSaleDataDtl.get(j);
                    totalSaleAmt += objSettle.getDblSettlementAmt();
                }
                totalCreditAmt += totalSaleAmt;
            }
            totalCreditAmt += finalTipAmt;

            double roundOff = totalDebitAmt - totalCreditAmt;
            double finalDebitAmount = 0, finalCreditAmount = 0;

            //For Settlement Detail
            double finalSettleAmt = 0;

            List<clsSettelementOptions> listOfSettlement = new ArrayList<>();
            for (Map.Entry<String, List<clsSettelementOptions>> entry : hmSalesSettleData.entrySet())
            {
                count++;
                double totalSaleAmt = 0;
                List<clsSettelementOptions> listOfSettleDataDtl = entry.getValue();

                for (int j = 0; j < listOfSettleDataDtl.size(); j++)
                {
                    clsSettelementOptions objSettle = listOfSettleDataDtl.get(j);
                    if (objSettle.getStrSettelmentType().equals("Credit"))
                    {
                        objSettle = new clsSettelementOptions();
                        objSettle.setStrSettelmentDesc("Credit");
                        listOfSettlement.add(objSettle);
                    }

                    objSettle = listOfSettleDataDtl.get(j);
                    listOfSettlement.add(objSettle);
                    totalSaleAmt += objSettle.getDblSettlementAmt();
                }
                finalSettleAmt += totalSaleAmt;
            }

            finalDebitAmount = finalDebitAmount + finalSettleAmt;
            //For Discount
            if (roundOff < 0)
            {
                finalDebitAmount += roundOff;
            }

            //For Groupwise sale data
            double finalGroupSaleAmt = 0;

            List<clsSettelementOptions> listOfGroupWiseSales = new ArrayList<>();

            for (Map.Entry<String, List<clsSettelementOptions>> entry : hmSalesGroupWiseSaleData.entrySet())
            {
                double totalSaleAmt = 0;
                List<clsSettelementOptions> listOfGroupwiseSaleDataDtl = entry.getValue();
                for (int j = 0; j < listOfGroupwiseSaleDataDtl.size(); j++)
                {
                    clsSettelementOptions objSettle = listOfGroupwiseSaleDataDtl.get(j);
                    listOfGroupWiseSales.add(objSettle);
                }
                finalGroupSaleAmt += totalSaleAmt;
            }

            finalCreditAmount = finalCreditAmount + finalGroupSaleAmt;
            //For Taxwise detial data

            double finalTaxAmt = 0;
            List<clsSettelementOptions> listOfTaxWiseSales = new ArrayList<>();
            for (Map.Entry<String, List<clsSettelementOptions>> entry : hmSalesTaxWiseSaleData.entrySet())
            {
                double totalSaleAmt = 0;
                List<clsSettelementOptions> listOfTaxwiseSaleDataDtl = entry.getValue();
                for (int j = 0; j < listOfTaxwiseSaleDataDtl.size(); j++)
                {
                    clsSettelementOptions objSettle = listOfTaxwiseSaleDataDtl.get(j);
                    totalSaleAmt += objSettle.getDblSettlementAmt();
                    listOfTaxWiseSales.add(objSettle);

                }
                finalTaxAmt += totalSaleAmt;
            }

            finalCreditAmount = finalCreditAmount + finalTaxAmt;

            finalCreditAmount = finalCreditAmount + finalTipAmt;

            double finalRoundOff = 0;

            finalCreditAmount = finalCreditAmount + finalRoundOff;

            if (roundOff > 0)
            {

                finalCreditAmount += roundOff;
            }

            List<clsSettelementOptions> listOfGroupWiseSalesForDineIn = new ArrayList<>();
            List<clsSettelementOptions> listOfGroupWiseSalesForTakeAway = new ArrayList<>();
            List<clsSettelementOptions> listOfGroupWiseSalesForHomeDel = new ArrayList<>();

            Iterator<Map.Entry<String, Double>> itDineIn = mapDineIn.entrySet().iterator();
            while (itDineIn.hasNext())
            {
                Map.Entry<String, Double> entry = itDineIn.next();
                String group = entry.getKey();
                double amount = entry.getValue();
                clsSettelementOptions objSett = new clsSettelementOptions();
                objSett.setStrSettelmentDesc(group);
                objSett.setDblSettlementAmt(amount);

                listOfGroupWiseSalesForDineIn.add(objSett);
            }

            Iterator<Map.Entry<String, Double>> itTakeAway = mapTakeAway.entrySet().iterator();
            while (itTakeAway.hasNext())
            {
                Map.Entry<String, Double> entry = itTakeAway.next();
                String group = entry.getKey();
                double amount = entry.getValue();
                clsSettelementOptions objSett = new clsSettelementOptions();
                objSett.setStrSettelmentDesc(group);
                objSett.setDblSettlementAmt(amount);

                listOfGroupWiseSalesForTakeAway.add(objSett);
            }

            Iterator<Map.Entry<String, Double>> itHomeDel = mapHomeDel.entrySet().iterator();
            while (itHomeDel.hasNext())
            {
                Map.Entry<String, Double> entry = itHomeDel.next();
                String group = entry.getKey();
                double amount = entry.getValue();
                clsSettelementOptions objSett = new clsSettelementOptions();
                objSett.setStrSettelmentDesc(group);
                objSett.setDblSettlementAmt(amount);

                listOfGroupWiseSalesForHomeDel.add(objSett);
            }

            hm.put("listOfSettlement", listOfSettlement);
            hm.put("listOfGroupWiseSales", listOfGroupWiseSales);
            hm.put("listOfTaxWiseSales", listOfTaxWiseSales);
            hm.put("finalDisAmt", finalDisAmt);
            hm.put("finalTipAmt", finalTipAmt);
            hm.put("listOfGroupWiseSalesForDineIn", listOfGroupWiseSalesForDineIn);
            hm.put("listOfGroupWiseSalesForTakeAway", listOfGroupWiseSalesForTakeAway);
            hm.put("listOfGroupWiseSalesForHomeDel", listOfGroupWiseSalesForHomeDel);

            double debitRoundOff = 0.00, creaditRoundOff = 0.00;
            if (roundOff < 0)
            {
                debitRoundOff = (-1) * roundOff;
                totalDebitAmt = totalDebitAmt + debitRoundOff;
            }
            else
            {
                creaditRoundOff = roundOff;
                totalCreditAmt = totalCreditAmt + creaditRoundOff;
            }
            hm.put("finalRoundOff", creaditRoundOff);
            hm.put("totalDebitAmt", totalDebitAmt);
            hm.put("totalCreditAmt", totalCreditAmt);
            hm.put("debitRoundOff", debitRoundOff);

            List<List<clsSettelementOptions>> listData = new ArrayList<>();
            listData.add(listOfSettlement);

            //call for view report
            funViewJasperReportForBeanCollectionDataSource(is, hm, listData);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void funWaiterWiseItemWiseIncentivesSummaryJasperReport()
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
            List<clsBillDtl> listOfWaiterWiseItemSales = new ArrayList<>();

            //Live Data
            sqlBuilder.setLength(0);
            sqlBuilder.append("SELECT d.strWShortName,b.strItemName,sum(b.dblAmount),c.dblIncentiveValue "
                    + " ,IF(c.strIncentiveType='Amt', (c.dblIncentiveValue)*sum(b.dblQuantity), (c.dblIncentiveValue/100)*sum(b.dblAmount)) as amount, "
                    + " e.strPosName,e.strPosCode,b.strItemCode,d.strWaiterNo,c.strIncentiveType,sum(b.dblQuantity)  "
                    + " FROM tblbillhd a,tblbilldtl b,tblposwiseitemwiseincentives c,tblwaitermaster d,tblposmaster e,tblitemmaster f,tblsubgrouphd g,tblgrouphd h "
                    + " where a.strBillNo=b.strBillNo "
                    + " and b.strItemCode=c.strItemCode "
                    + " and b.strWaiterNo=d.strWaiterNo "
                    + " and a.strPOSCode=e.strPosCode "
                    + " and a.strPOSCode=c.strPOSCode "
                    + " and c.dblIncentiveValue>0 "
                    + " and b.strItemCode=f.strItemCode "
                    + " and f.strSubGroupCode=g.strSubGroupCode "
                    + " and g.strGroupCode=h.strGroupCode "
                    + " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and a.strPOSCode='" + posCode + "' ");
            }
            if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
            {
                sqlBuilder.append("and a.intShiftCode='" + shiftNo + "' ");
            }
            if(!groupCode.equalsIgnoreCase("All"))
            {
                sqlBuilder.append(" and h.strGroupCode='"+groupCode+"' ");
            }
            if(!subGroupCode.equalsIgnoreCase("All"))
            {
                sqlBuilder.append(" and g.strSubGroupCode='"+subGroupCode+"' ");
            }

            sqlBuilder.append("and a.strBillNo not in (select u.strBillNo "
                    + " from tblbillhd v,tblbillsettlementdtl u,tblsettelmenthd w "
                    + " where v.strBillNo=u.strBillNo and u.strSettlementCode=w.strSettelmentCode "
                    + " and w.strSettelmentType='Complementary' and date(v.dteBillDate) between '" + fromDate + "' and '" + toDate + "')");

            sqlBuilder.append(" group by b.strWaiterNo,c.strPOSCode,b.strItemCode ");
            sqlBuilder.append(" order by e.strPosName,b.strWaiterNo,b.strItemName ");

            ResultSet rsWaiterWiseItemSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
            while (rsWaiterWiseItemSales.next())
            {
                clsBillDtl obj = new clsBillDtl();

                obj.setStrWShortName(rsWaiterWiseItemSales.getString(1));
                obj.setStrItemName(rsWaiterWiseItemSales.getString(2));
                obj.setStrItemCode(rsWaiterWiseItemSales.getString(8));
                obj.setDblAmount(rsWaiterWiseItemSales.getDouble(3));
                obj.setDblIncentivePer(rsWaiterWiseItemSales.getDouble(4));
                obj.setDblIncentive(rsWaiterWiseItemSales.getDouble(5));
                obj.setStrPosName(rsWaiterWiseItemSales.getString(6));
                obj.setStrPOSCode(rsWaiterWiseItemSales.getString(7));
                obj.setStrWaiterNo(rsWaiterWiseItemSales.getString(9));
                obj.setStrRemarks(rsWaiterWiseItemSales.getString(10));
                obj.setDblQuantity(rsWaiterWiseItemSales.getDouble(11));

                listOfWaiterWiseItemSales.add(obj);
            }
            rsWaiterWiseItemSales.close();

            //Q Data
             sqlBuilder.setLength(0);
            sqlBuilder.append("SELECT d.strWShortName,b.strItemName,sum(b.dblAmount),c.dblIncentiveValue "
                    + " ,IF(c.strIncentiveType='Amt', (c.dblIncentiveValue)*sum(b.dblQuantity), (c.dblIncentiveValue/100)*sum(b.dblAmount)) as amount, "
                    + " e.strPosName,e.strPosCode,b.strItemCode,d.strWaiterNo,c.strIncentiveType,sum(b.dblQuantity)  "
                    + " FROM tblqbillhd a,tblqbilldtl b,tblposwiseitemwiseincentives c,tblwaitermaster d,tblposmaster e,tblitemmaster f,tblsubgrouphd g,tblgrouphd h "
                    + " where a.strBillNo=b.strBillNo "
                    + " and b.strItemCode=c.strItemCode "
                    + " and b.strWaiterNo=d.strWaiterNo "
                    + " and a.strPOSCode=e.strPosCode "
                    + " and a.strPOSCode=c.strPOSCode "
                    + " and c.dblIncentiveValue>0 "
                    + " and b.strItemCode=f.strItemCode "
                    + " and f.strSubGroupCode=g.strSubGroupCode "
                    + " and g.strGroupCode=h.strGroupCode "
                    + " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and a.strPOSCode='" + posCode + "' ");
            }
            if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
            {
                sqlBuilder.append("and a.intShiftCode='" + shiftNo + "' ");
            }
            if(!groupCode.equalsIgnoreCase("All"))
            {
                sqlBuilder.append(" and h.strGroupCode='"+groupCode+"' ");
            }
            if(!subGroupCode.equalsIgnoreCase("All"))
            {
                sqlBuilder.append(" and g.strSubGroupCode='"+subGroupCode+"' ");
            }

            sqlBuilder.append("and a.strBillNo not in (select u.strBillNo "
                    + " from tblqbillhd v,tblqbillsettlementdtl u,tblsettelmenthd w "
                    + " where v.strBillNo=u.strBillNo and u.strSettlementCode=w.strSettelmentCode "
                    + " and w.strSettelmentType='Complementary' and date(v.dteBillDate) between '" + fromDate + "' and '" + toDate + "')");

            sqlBuilder.append(" group by b.strWaiterNo,c.strPOSCode,b.strItemCode ");
            sqlBuilder.append(" order by e.strPosName,b.strWaiterNo,b.strItemName ");

            rsWaiterWiseItemSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
            while (rsWaiterWiseItemSales.next())
            {
                clsBillDtl obj = new clsBillDtl();

                obj.setStrWShortName(rsWaiterWiseItemSales.getString(1));
                obj.setStrItemName(rsWaiterWiseItemSales.getString(2));
                obj.setStrItemCode(rsWaiterWiseItemSales.getString(8));
                obj.setDblAmount(rsWaiterWiseItemSales.getDouble(3));
                obj.setDblIncentivePer(rsWaiterWiseItemSales.getDouble(4));
                obj.setDblIncentive(rsWaiterWiseItemSales.getDouble(5));
                obj.setStrPosName(rsWaiterWiseItemSales.getString(6));
                obj.setStrPOSCode(rsWaiterWiseItemSales.getString(7));
                obj.setStrWaiterNo(rsWaiterWiseItemSales.getString(9));
                obj.setStrRemarks(rsWaiterWiseItemSales.getString(10));
                obj.setDblQuantity(rsWaiterWiseItemSales.getDouble(11));

                listOfWaiterWiseItemSales.add(obj);
            }
            rsWaiterWiseItemSales.close();

            Comparator<clsBillDtl> waiterCodeComparator = new Comparator<clsBillDtl>()
            {

                @Override
                public int compare(clsBillDtl o1, clsBillDtl o2)
                {
                    return o1.getStrItemCode().compareTo(o2.getStrItemCode());
                }
            };

            Collections.sort(listOfWaiterWiseItemSales, new clsWaiterWiseSalesComparator(waiterCodeComparator));
            //call for view report
            funViewJasperReportForBeanCollectionDataSource(is, hm, listOfWaiterWiseItemSales);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void funVoidAdvanceOrderReportForJasper()
    {
        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String orderType = hm.get("orderType").toString();

            List<clsAdvOrderItemDtl> listOdAdvOrderItemDtl = new ArrayList<>();
            StringBuilder sqlBuilder = new StringBuilder();

            sqlBuilder.setLength(0);
            sqlBuilder.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y') as dteAdvBookingDate,DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y') as  dteOrderFor,ifnull(f.strCustomerName,'NA') as strCustomerName ,f.longMobileNo "
                    + ",b.strItemCode,b.strItemName,d.strCharCode,ifnull(d.strCharName,'') as strCharName,ifnull(c.strCharValues,'') as strCharValues, "
                    + "sum(b.dblQuantity) as dblQuantity ,sum(b.dblAmount)/sum(b.dblQuantity) as dblRate,sum(b.dblAmount) as dblAmount,sum(b.dblWeight) as dblWeight, "
                    + "e.dblAdvDeposite,g.strReasonName,a.strRemark,DATE_FORMAT(a.dteDateCreated,'%d-%m-%Y') as dteVoidedDate "
                    + "from tblvoidadvbookbillhd a "
                    + "left outer join tblvoidadvbookbilldtl b on a.strAdvBookingNo=b.strAdvBookingNo and a.strClientCode=b.strClientCode "
                    + "left outer join tblvoidadvbookbillchardtl c on a.strAdvBookingNo=c.strAdvBookingNo and b.strItemCode=c.strItemCode and a.strClientCode=c.strClientCode "
                    + "left outer join tblcharactersticsmaster d on c.strCharCode=d.strCharCode and c.strClientCode=d.strClientCode "
                    + "left outer join tblvoidadvancereceipthd e on a.strAdvBookingNo=e.strAdvBookingNo and a.strClientCode=e.strClientCode "
                    + "left outer join tblcustomermaster f on a.strCustomerCode=f.strCustomerCode and a.strClientCode=f.strClientCode "
                    + "left outer join tblreasonmaster g on a.strReasonCode=g.strReasonCode and a.strClientCode=g.strClientCode "
                    + "where date(a.dteOrderFor) between '" + fromDate + "' and '" + toDate + "' ");
            if (!posCode.equals("All"))
            {
                sqlBuilder.append(" and a.strPOSCode='" + posCode + "'  ");
            }
            if (!orderType.equalsIgnoreCase("All") && orderType.equalsIgnoreCase("Advance Order"))
            {
                sqlBuilder.append(" and a.strUrgentOrder='N'  ");
            }
            else if (!orderType.equalsIgnoreCase("All") && orderType.equalsIgnoreCase("Urgent Order"))
            {
                sqlBuilder.append(" and a.strUrgentOrder='Y'  ");
            }
            sqlBuilder.append("group by a.strAdvBookingNo,b.strItemCode,c.strCharCode,c.strCharValues "
                    + "order by a.strAdvBookingNo,b.strItemCode,c.strCharCode,c.strCharValues ");

            ResultSet rsAdvOrderDtl = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());

            String itemCode = "", advOrderNO = "";
            clsAdvOrderItemDtl objAdvOrderItemDtl = null;
            while (rsAdvOrderDtl.next())
            {
                if (itemCode.equals(rsAdvOrderDtl.getString("strItemCode")) && advOrderNO.equals(rsAdvOrderDtl.getString("strAdvBookingNo")))
                {
                    objAdvOrderItemDtl.setStrCharNameValuePair(objAdvOrderItemDtl.getStrCharNameValuePair() + "  " + rsAdvOrderDtl.getString("strCharName") + "-->" + rsAdvOrderDtl.getString("strCharValues"));
                }
                else
                {
                    objAdvOrderItemDtl = new clsAdvOrderItemDtl();

                    itemCode = rsAdvOrderDtl.getString("strItemCode");
                    advOrderNO = rsAdvOrderDtl.getString("strAdvBookingNo");

                    objAdvOrderItemDtl.setStrAdvBookingNo(rsAdvOrderDtl.getString("strAdvBookingNo"));
                    objAdvOrderItemDtl.setDteAdvBookingDate(rsAdvOrderDtl.getString("dteAdvBookingDate"));
                    objAdvOrderItemDtl.setDteOrderFor(rsAdvOrderDtl.getString("dteOrderFor"));
                    objAdvOrderItemDtl.setStrCustomerName(rsAdvOrderDtl.getString("strCustomerName"));
                    objAdvOrderItemDtl.setLongMobileNo(rsAdvOrderDtl.getString("longMobileNo"));
                    objAdvOrderItemDtl.setStrItemCode(rsAdvOrderDtl.getString("strItemCode"));
                    objAdvOrderItemDtl.setStrItemName(rsAdvOrderDtl.getString("strItemName"));
                    objAdvOrderItemDtl.setStrCharCode(rsAdvOrderDtl.getString("strCharCode"));
                    objAdvOrderItemDtl.setStrCharName(rsAdvOrderDtl.getString("strCharName"));
                    objAdvOrderItemDtl.setStrCharValues(rsAdvOrderDtl.getString("strCharValues"));
                    objAdvOrderItemDtl.setDblQuantity(rsAdvOrderDtl.getDouble("dblQuantity"));
                    objAdvOrderItemDtl.setDblRate(rsAdvOrderDtl.getDouble("dblRate"));
                    objAdvOrderItemDtl.setDblAmount(rsAdvOrderDtl.getDouble("dblAmount"));
                    objAdvOrderItemDtl.setDblWeight(rsAdvOrderDtl.getDouble("dblWeight"));
                    objAdvOrderItemDtl.setDblTotalAmount(rsAdvOrderDtl.getDouble("dblAmount"));
                    objAdvOrderItemDtl.setStrCharNameValuePair(rsAdvOrderDtl.getString("strCharName") + "-->" + rsAdvOrderDtl.getString("strCharValues"));
                    objAdvOrderItemDtl.setStrReasonCode(rsAdvOrderDtl.getString("strReasonName"));
                    objAdvOrderItemDtl.setStrRemarks(rsAdvOrderDtl.getString("strRemark"));
                    objAdvOrderItemDtl.setDteVoidedDate(rsAdvOrderDtl.getString("dteVoidedDate"));
                    objAdvOrderItemDtl.setDblAdvDeposite(rsAdvOrderDtl.getDouble("dblAdvDeposite"));

                    listOdAdvOrderItemDtl.add(objAdvOrderItemDtl);
                }
            }
            //call for view report
            funViewJasperReportForBeanCollectionDataSource(is, hm, listOdAdvOrderItemDtl);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void funAdvanceOrderReportForJasper()
    {

        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String orderType = hm.get("orderType").toString();

            List<clsAdvOrderItemDtl> listOdAdvOrderItemDtl = new ArrayList<>();
            StringBuilder sqlBuilder = new StringBuilder();
            //for live data
            sqlBuilder.setLength(0);
            sqlBuilder.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y') as dteAdvBookingDate,DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y') as  dteOrderFor,ifnull(f.strCustomerName,'NA') as strCustomerName ,f.longMobileNo "
                    + ",b.strItemCode,b.strItemName,d.strCharCode,ifnull(d.strCharName,'') as strCharName,ifnull(c.strCharValues,'') as strCharValues, "
                    + "sum(b.dblQuantity) as dblQuantity ,sum(b.dblAmount)/sum(b.dblQuantity) as dblRate,sum(b.dblAmount) as dblAmount,sum(b.dblWeight) as dblWeight, "
                    + "e.dblAdvDeposite "
                    + "from tbladvbookbillhd a "
                    + "inner join tbladvbookbilldtl b on a.strAdvBookingNo=b.strAdvBookingNo and a.strClientCode=b.strClientCode "
                    + "left outer join tbladvbookbillchardtl c on a.strAdvBookingNo=c.strAdvBookingNo and b.strItemCode=c.strItemCode and a.strClientCode=c.strClientCode "
                    + "left outer join tblcharactersticsmaster d on c.strCharCode=d.strCharCode and c.strClientCode=d.strClientCode "
                    + "left outer join tbladvancereceipthd e on a.strAdvBookingNo=e.strAdvBookingNo and a.strClientCode=e.strClientCode "
                    + "left outer join tblcustomermaster f on a.strCustomerCode=f.strCustomerCode and a.strClientCode=f.strClientCode "
                    + "where date(a.dteOrderFor) between '" + fromDate + "' and '" + toDate + "' ");
            if (!posCode.equals("All"))
            {
                sqlBuilder.append(" and a.strPOSCode='" + posCode + "'  ");
            }
            if (!orderType.equalsIgnoreCase("All") && orderType.equalsIgnoreCase("Advance Order"))
            {
                sqlBuilder.append(" and a.strUrgentOrder='N'  ");
            }
            else if (!orderType.equalsIgnoreCase("All") && orderType.equalsIgnoreCase("Urgent Order"))
            {
                sqlBuilder.append(" and a.strUrgentOrder='Y'  ");
            }
            sqlBuilder.append("group by a.strAdvBookingNo,b.strItemCode,c.strCharCode,c.strCharValues "
                    + "order by a.strAdvBookingNo,b.strItemCode,c.strCharCode,c.strCharValues ");

            ResultSet rsAdvOrderDtl = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());

            // listOdAdvOrderItemDtl=funGetAdvOrderList(rsAdvOrderDtl,listOdAdvOrderItemDtl);
            String itemCode = "", advOrderNO = "";
            clsAdvOrderItemDtl objAdvOrderItemDtl = null;
            boolean flag = false;
            double totalAdvAmt = 0.00;
            while (rsAdvOrderDtl.next())
            {
                if (itemCode.equals(rsAdvOrderDtl.getString("strItemCode")) && advOrderNO.equals(rsAdvOrderDtl.getString("strAdvBookingNo")))
                {
                    objAdvOrderItemDtl.setStrCharNameValuePair(objAdvOrderItemDtl.getStrCharNameValuePair() + "  " + rsAdvOrderDtl.getString("strCharName") + "-->" + rsAdvOrderDtl.getString("strCharValues"));
                }
                else
                {
                    objAdvOrderItemDtl = new clsAdvOrderItemDtl();

                    if (advOrderNO == "")
                    {
                        totalAdvAmt += rsAdvOrderDtl.getDouble("dblAdvDeposite");
                    }
                    else if (advOrderNO.equals(rsAdvOrderDtl.getString("strAdvBookingNo")))
                    {
                    }
                    else if (!advOrderNO.equals(rsAdvOrderDtl.getString("strAdvBookingNo")))
                    {
                        totalAdvAmt += rsAdvOrderDtl.getDouble("dblAdvDeposite");
                    }
                    itemCode = rsAdvOrderDtl.getString("strItemCode");
                    advOrderNO = rsAdvOrderDtl.getString("strAdvBookingNo");

                    objAdvOrderItemDtl.setStrAdvBookingNo(rsAdvOrderDtl.getString("strAdvBookingNo"));
                    objAdvOrderItemDtl.setDteAdvBookingDate(rsAdvOrderDtl.getString("dteAdvBookingDate"));
                    objAdvOrderItemDtl.setDteOrderFor(rsAdvOrderDtl.getString("dteOrderFor"));
                    objAdvOrderItemDtl.setStrCustomerName(rsAdvOrderDtl.getString("strCustomerName"));
                    objAdvOrderItemDtl.setLongMobileNo(rsAdvOrderDtl.getString("longMobileNo"));
                    objAdvOrderItemDtl.setStrItemCode(rsAdvOrderDtl.getString("strItemCode"));
                    objAdvOrderItemDtl.setStrItemName(rsAdvOrderDtl.getString("strItemName"));
                    objAdvOrderItemDtl.setStrCharCode(rsAdvOrderDtl.getString("strCharCode"));
                    objAdvOrderItemDtl.setStrCharName(rsAdvOrderDtl.getString("strCharName"));
                    objAdvOrderItemDtl.setStrCharValues(rsAdvOrderDtl.getString("strCharValues"));
                    objAdvOrderItemDtl.setDblQuantity(rsAdvOrderDtl.getDouble("dblQuantity"));
                    objAdvOrderItemDtl.setDblRate(rsAdvOrderDtl.getDouble("dblRate"));
                    objAdvOrderItemDtl.setDblAmount(rsAdvOrderDtl.getDouble("dblAmount"));
                    objAdvOrderItemDtl.setDblWeight(rsAdvOrderDtl.getDouble("dblWeight"));
                    objAdvOrderItemDtl.setDblAdvDeposite(rsAdvOrderDtl.getDouble("dblAdvDeposite"));
                    objAdvOrderItemDtl.setDblTotalAmount(rsAdvOrderDtl.getDouble("dblAmount"));
                    objAdvOrderItemDtl.setStrCharNameValuePair(rsAdvOrderDtl.getString("strCharName") + "-->" + rsAdvOrderDtl.getString("strCharValues"));

                    listOdAdvOrderItemDtl.add(objAdvOrderItemDtl);
                }
            }

//                 //for q data
            sqlBuilder.setLength(0);
            sqlBuilder.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y') as dteAdvBookingDate,DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y') as  dteOrderFor,ifnull(f.strCustomerName,'NA') as strCustomerName ,f.longMobileNo "
                    + ",b.strItemCode,b.strItemName,d.strCharCode,ifnull(d.strCharName,'') as strCharName,ifnull(c.strCharValues,'') as strCharValues, "
                    + "sum(b.dblQuantity) as dblQuantity ,sum(b.dblAmount)/sum(b.dblQuantity) as dblRate,sum(b.dblAmount) as dblAmount,sum(b.dblWeight) as dblWeight, "
                    + "e.dblAdvDeposite "
                    + "from tblqadvbookbillhd a "
                    + "inner join tblqadvbookbilldtl b on a.strAdvBookingNo=b.strAdvBookingNo and a.strClientCode=b.strClientCode "
                    + "left outer join tblqadvbookbillchardtl c on a.strAdvBookingNo=c.strAdvBookingNo and b.strItemCode=c.strItemCode and a.strClientCode=c.strClientCode "
                    + "left outer join tblcharactersticsmaster d on c.strCharCode=d.strCharCode and c.strClientCode=d.strClientCode "
                    + "left outer join tblqadvancereceipthd e on a.strAdvBookingNo=e.strAdvBookingNo and a.strClientCode=e.strClientCode "
                    + "left outer join tblcustomermaster f on a.strCustomerCode=f.strCustomerCode and a.strClientCode=f.strClientCode "
                    + "where date(a.dteOrderFor) between '" + fromDate + "' and '" + toDate + "' ");
            if (!posCode.equals("All"))
            {
                sqlBuilder.append(" and a.strPOSCode='" + posCode + "'  ");
            }
            if (!orderType.equalsIgnoreCase("All") && orderType.equalsIgnoreCase("Advance Order"))
            {
                sqlBuilder.append(" and a.strUrgentOrder='N'  ");
            }
            else if (!orderType.equalsIgnoreCase("All") && orderType.equalsIgnoreCase("Urgent Order"))
            {
                sqlBuilder.append(" and a.strUrgentOrder='Y'  ");
            }
            sqlBuilder.append("group by a.strAdvBookingNo,b.strItemCode,c.strCharCode,c.strCharValues "
                    + "order by a.strAdvBookingNo,b.strItemCode,c.strCharCode,c.strCharValues ");

            rsAdvOrderDtl = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());

            //listOdAdvOrderItemDtl=funGetAdvOrderList(rsAdvOrderDtl,listOdAdvOrderItemDtl);
            itemCode = "";
            advOrderNO = "";
            while (rsAdvOrderDtl.next())
            {
                if (itemCode.equals(rsAdvOrderDtl.getString("strItemCode")) && advOrderNO.equals(rsAdvOrderDtl.getString("strAdvBookingNo")))
                {
                    objAdvOrderItemDtl.setStrCharNameValuePair(objAdvOrderItemDtl.getStrCharNameValuePair() + "  " + rsAdvOrderDtl.getString("strCharName") + "-->" + rsAdvOrderDtl.getString("strCharValues"));
                }
                else
                {
                    objAdvOrderItemDtl = new clsAdvOrderItemDtl();
                    if (advOrderNO == "")
                    {
                        totalAdvAmt += rsAdvOrderDtl.getDouble("dblAdvDeposite");
                    }
                    else if (advOrderNO.equals(rsAdvOrderDtl.getString("strAdvBookingNo")))
                    {
                    }
                    else if (!advOrderNO.equals(rsAdvOrderDtl.getString("strAdvBookingNo")))
                    {
                        totalAdvAmt += rsAdvOrderDtl.getDouble("dblAdvDeposite");
                    }
                    itemCode = rsAdvOrderDtl.getString("strItemCode");
                    advOrderNO = rsAdvOrderDtl.getString("strAdvBookingNo");

                    objAdvOrderItemDtl.setStrAdvBookingNo(rsAdvOrderDtl.getString("strAdvBookingNo"));
                    objAdvOrderItemDtl.setDteAdvBookingDate(rsAdvOrderDtl.getString("dteAdvBookingDate"));
                    objAdvOrderItemDtl.setDteOrderFor(rsAdvOrderDtl.getString("dteOrderFor"));
                    objAdvOrderItemDtl.setStrCustomerName(rsAdvOrderDtl.getString("strCustomerName"));
                    objAdvOrderItemDtl.setLongMobileNo(rsAdvOrderDtl.getString("longMobileNo"));
                    objAdvOrderItemDtl.setStrItemCode(rsAdvOrderDtl.getString("strItemCode"));
                    objAdvOrderItemDtl.setStrItemName(rsAdvOrderDtl.getString("strItemName"));
                    objAdvOrderItemDtl.setStrCharCode(rsAdvOrderDtl.getString("strCharCode"));
                    objAdvOrderItemDtl.setStrCharName(rsAdvOrderDtl.getString("strCharName"));
                    objAdvOrderItemDtl.setStrCharValues(rsAdvOrderDtl.getString("strCharValues"));
                    objAdvOrderItemDtl.setDblQuantity(rsAdvOrderDtl.getDouble("dblQuantity"));
                    objAdvOrderItemDtl.setDblRate(rsAdvOrderDtl.getDouble("dblRate"));
                    objAdvOrderItemDtl.setDblAmount(rsAdvOrderDtl.getDouble("dblAmount"));
                    objAdvOrderItemDtl.setDblWeight(rsAdvOrderDtl.getDouble("dblWeight"));
                    objAdvOrderItemDtl.setDblAdvDeposite(rsAdvOrderDtl.getDouble("dblAdvDeposite"));
                    objAdvOrderItemDtl.setDblTotalAmount(rsAdvOrderDtl.getDouble("dblAmount"));
                    objAdvOrderItemDtl.setStrCharNameValuePair(rsAdvOrderDtl.getString("strCharName") + "-->" + rsAdvOrderDtl.getString("strCharValues"));

                    listOdAdvOrderItemDtl.add(objAdvOrderItemDtl);
                }

            }
            //call for view report
            funViewJasperReportForBeanCollectionDataSource(is, hm, listOdAdvOrderItemDtl);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void funPlacedOrderReportForJasper()
    {

        try
        {
//            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String orderType = hm.get("orderType").toString();
            String orderCode = hm.get("orderCode").toString();
            String orderName = hm.get("orderName").toString();

            String sqlFilters = "";
            int count = 1, cn = 0;

            Map<String, Integer> hmOrderCode = new HashMap<String, Integer>();
            Map<String, Integer> hmOrderCode1 = new HashMap<String, Integer>();
            Map<String, List<clsPlaceOrderDtl>> hmPlaceOrderDtl = new HashMap<String, List<clsPlaceOrderDtl>>();
            List<clsPlaceOrderDtl> listItemDtl = null;

            Map<String, Map<String, List<clsPlaceOrderDtl>>> hmOrderDtl = new HashMap<String, Map<String, List<clsPlaceOrderDtl>>>();

            String sql = "select c.strExternalCode,c.strItemName,b.strProductCode,sum(b.dblStockQty),"
                    + " sum(b.dblQty),a.strOrderCode,a.strSOCode,date(a.dteOrderDate),"
                    + " f.strGroupCode,f.strSubGroupCode ,b.strItemCode,f.strSubGroupName,date(a.dteSODate)    "
                    + " from tblplaceorderhd a,tblplaceorderdtl b,tblitemmaster c,tblitemcurrentstk e,"
                    + " tblsubgrouphd f   where a.strOrderCode=b.strOrderCode and b.strItemCode=c.strItemCode "
                    + " and c.strItemCode=e.strItemCode   and c.strSubGroupCode=f.strSubGroupCode "
                    + " and a.strCloseSO='N'  and a.strSOCode!='' "
                    + " and date(a.dteSODate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' ";
            if (!posCode.equals("All"))
            {
                sqlFilters += " AND e.strPOSCode = '" + posCode + "' ";
            }
            if (!orderType.equals("All"))
            {
                sqlFilters += " and a.strOrderType = '" + orderType + "' ";
                if (orderType.equals("Normal"))
                {
                    if (!orderName.equals("All"))
                    {
                        sqlFilters += " and a.strOrderTypeCode = '" + orderCode + "' ";
                    }
                }
            }

            sqlFilters += " GROUP BY b.strItemCode order by a.dteOrderDate";
            sql = sql + " " + sqlFilters;

            ResultSet rsOrder = clsGlobalVarClass.dbMysql.executeResultSet(sql);
            while (rsOrder.next())
            {
                cn++;
                clsPlaceOrderDtl objItemDtl = new clsPlaceOrderDtl();
                if (hmOrderDtl.containsKey(rsOrder.getString(6)))
                {
                    hmPlaceOrderDtl = hmOrderDtl.get(rsOrder.getString(6));

                    if (hmPlaceOrderDtl.containsKey(rsOrder.getString(12)))
                    {
                        listItemDtl = hmPlaceOrderDtl.get(rsOrder.getString(12));
                        objItemDtl = new clsPlaceOrderDtl();
                        objItemDtl.setSubGroupName(rsOrder.getString(12));
                        objItemDtl.setItemName(rsOrder.getString(2));
                        objItemDtl.setSaleQty(rsOrder.getString(5));
                        objItemDtl.setOrderDate(rsOrder.getString(8));
                        objItemDtl.setSoDate(rsOrder.getString(13));
                        objItemDtl.setSOCode(rsOrder.getString(7));
                    }
                    else
                    {
                        listItemDtl = new ArrayList<clsPlaceOrderDtl>();
                        objItemDtl.setSubGroupName(rsOrder.getString(12));
                        objItemDtl.setItemName(rsOrder.getString(2));
                        objItemDtl.setSaleQty(rsOrder.getString(5));
                        objItemDtl.setOrderDate(rsOrder.getString(8));
                        objItemDtl.setSoDate(rsOrder.getString(13));
                        objItemDtl.setSOCode(rsOrder.getString(7));
                    }
                    listItemDtl.add(objItemDtl);
                    hmPlaceOrderDtl.put(rsOrder.getString(12), listItemDtl);
                }
                else
                {
                    listItemDtl = new ArrayList<clsPlaceOrderDtl>();
                    objItemDtl.setSubGroupName(rsOrder.getString(12));
                    objItemDtl.setItemName(rsOrder.getString(2));
                    objItemDtl.setSaleQty(rsOrder.getString(5));
                    objItemDtl.setOrderDate(rsOrder.getString(8));
                    objItemDtl.setSoDate(rsOrder.getString(13));
                    objItemDtl.setSOCode(rsOrder.getString(7));
                    listItemDtl.add(objItemDtl);
                    hmPlaceOrderDtl = new HashMap<String, List<clsPlaceOrderDtl>>();
                    hmPlaceOrderDtl.put(rsOrder.getString(12), listItemDtl);
                }
                hmOrderDtl.put(rsOrder.getString(6), hmPlaceOrderDtl);
            }
            if (cn > 0)
            {

                if (!orderType.equals("All"))
                {
                    if (orderType.equals("Normal"))
                    {
                        funGeneratePlaceOrderJasperReport(hmOrderDtl, "Normal Order Details");
                    }
                    else if (orderType.equals("Advance"))
                    {
                        funGeneratePlaceOrderJasperReport(hmOrderDtl, "Advance Order Details");
                    }
                    else
                    {
                        funGeneratePlaceOrderJasperReport(hmOrderDtl, "Urgent Order Details");
                    }
                }
                else
                {
                    funGeneratePlaceOrderJasperReport(hmOrderDtl, "Placed Order Details");
                }
            }
            else
            {
                JOptionPane.showMessageDialog(null, "No Record Found!!!!!");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void funGeneratePlaceOrderJasperReport(Map<String, Map<String, List<clsPlaceOrderDtl>>> hmOrderDtl, String Title)
    {
        try
        {

            hm.put("Title", Title);
            String line = "__________";

            List<clsPlaceOrderDtl> listOfLeftSidePlaceOrderDtl = new ArrayList<clsPlaceOrderDtl>();
            List<clsPlaceOrderDtl> listOfRightSidePlaceOrderDtl = new ArrayList<clsPlaceOrderDtl>();
            List<clsPlaceOrderDtl> orderList = new ArrayList<clsPlaceOrderDtl>();
            Map<String, List<clsPlaceOrderDtl>> hmPlaceOrderDtl = new HashMap<String, List<clsPlaceOrderDtl>>();
            int c = 0;

            if (hmOrderDtl.size() > 0)
            {
                for (Map.Entry<String, Map<String, List<clsPlaceOrderDtl>>> entry : hmOrderDtl.entrySet())
                {
                    String orderCode = entry.getKey();
                    String orderDate = "", soDate = "", soCode = "";
                    double totalOrderQty = 0;
                    hmPlaceOrderDtl = entry.getValue();
                    if (c % 2 == 0)
                    {
                        clsPlaceOrderDtl objOrder = new clsPlaceOrderDtl();
                        objOrder.setSubGroupName("");
                        objOrder.setItemName("Order No :" + "  " + entry.getKey());
                        objOrder.setSaleQty("");
                        listOfLeftSidePlaceOrderDtl.add(objOrder);
                        for (Map.Entry<String, List<clsPlaceOrderDtl>> entryOrder : hmPlaceOrderDtl.entrySet())
                        {
                            orderList = entryOrder.getValue();
                            for (int i = 0; i < orderList.size(); i++)
                            {
                                objOrder = orderList.get(i);
                                orderDate = objOrder.getOrderDate();
                                soDate = objOrder.getSoDate();
                                soCode = objOrder.getSOCode();
                                break;

                            }
                        }
                        objOrder = new clsPlaceOrderDtl();
                        objOrder.setSubGroupName("");
                        objOrder.setItemName("Order Date :" + " " + orderDate);
                        objOrder.setSaleQty(" ");
                        listOfLeftSidePlaceOrderDtl.add(objOrder);
                        objOrder = new clsPlaceOrderDtl();
                        objOrder.setSubGroupName("");
                        objOrder.setItemName("SO Date :" + " " + soDate);
                        objOrder.setSaleQty(" ");
                        listOfLeftSidePlaceOrderDtl.add(objOrder);
                        objOrder = new clsPlaceOrderDtl();
                        objOrder.setSubGroupName("");
                        objOrder.setItemName("SO Code :" + " " + soCode);
                        objOrder.setSaleQty(" ");
                        listOfLeftSidePlaceOrderDtl.add(objOrder);

                        for (Map.Entry<String, List<clsPlaceOrderDtl>> entryOrder : hmPlaceOrderDtl.entrySet())
                        {
                            double totalQty = 0;
                            orderList = entryOrder.getValue();
                            objOrder = new clsPlaceOrderDtl();
                            objOrder.setSubGroupName((char) 27 + entryOrder.getKey());
                            objOrder.setItemName(entryOrder.getKey());
                            objOrder.setSaleQty("");
                            listOfLeftSidePlaceOrderDtl.add(objOrder);
                            objOrder = new clsPlaceOrderDtl();
                            objOrder.setSubGroupName("");
                            objOrder.setItemName(line + line);
                            objOrder.setSaleQty(" ");
                            listOfLeftSidePlaceOrderDtl.add(objOrder);
                            for (int i = 0; i < orderList.size(); i++)
                            {
                                objOrder = orderList.get(i);
                                totalQty = totalQty + Double.valueOf(objOrder.getSaleQty());
                                listOfLeftSidePlaceOrderDtl.add(objOrder);
                            }
                            totalOrderQty += totalQty;
                            objOrder = new clsPlaceOrderDtl();
                            objOrder.setSubGroupName((char) 27 + entryOrder.getKey());
                            objOrder.setItemName("");
                            objOrder.setSaleQty(line);
                            listOfLeftSidePlaceOrderDtl.add(objOrder);
                            objOrder = new clsPlaceOrderDtl();
                            objOrder.setSubGroupName(entryOrder.getKey());
                            objOrder.setItemName("");
                            objOrder.setSaleQty(String.valueOf(totalQty));
                            listOfLeftSidePlaceOrderDtl.add(objOrder);
                            objOrder = new clsPlaceOrderDtl();
                            objOrder.setSubGroupName("");
                            objOrder.setItemName("");
                            objOrder.setSaleQty("");
                            listOfLeftSidePlaceOrderDtl.add(objOrder);
                        }

                        objOrder = new clsPlaceOrderDtl();
                        objOrder.setSubGroupName("");
                        objOrder.setItemName("Total Order Qty :");
                        objOrder.setSaleQty(String.valueOf(totalOrderQty));
                        listOfLeftSidePlaceOrderDtl.add(objOrder);
                        objOrder = new clsPlaceOrderDtl();
                        objOrder.setSubGroupName("");
                        objOrder.setItemName("");
                        objOrder.setSaleQty(" ");
                        listOfLeftSidePlaceOrderDtl.add(objOrder);
                    }
                    else
                    {
                        clsPlaceOrderDtl objOrder = new clsPlaceOrderDtl();
                        objOrder.setSubGroupName("");
                        objOrder.setItemName("Order No :" + "  " + entry.getKey());
                        objOrder.setSaleQty("");
                        listOfRightSidePlaceOrderDtl.add(objOrder);
                        for (Map.Entry<String, List<clsPlaceOrderDtl>> entryOrder : hmPlaceOrderDtl.entrySet())
                        {
                            orderList = entryOrder.getValue();
                            for (int i = 0; i < orderList.size(); i++)
                            {
                                objOrder = orderList.get(i);
                                orderDate = objOrder.getOrderDate();
                                soDate = objOrder.getSoDate();
                                soCode = objOrder.getSOCode();
                                break;

                            }
                        }
                        objOrder = new clsPlaceOrderDtl();
                        objOrder.setSubGroupName("");
                        objOrder.setItemName("Order Date :" + " " + orderDate);
                        objOrder.setSaleQty(" ");
                        listOfRightSidePlaceOrderDtl.add(objOrder);
                        objOrder = new clsPlaceOrderDtl();
                        objOrder.setSubGroupName("");
                        objOrder.setItemName("SO Date :" + " " + soDate);
                        objOrder.setSaleQty(" ");
                        listOfRightSidePlaceOrderDtl.add(objOrder);
                        objOrder = new clsPlaceOrderDtl();
                        objOrder.setSubGroupName("");
                        objOrder.setItemName("SO Code :" + " " + soCode);
                        objOrder.setSaleQty(" ");
                        listOfRightSidePlaceOrderDtl.add(objOrder);
                        /*
                         * objOrder=new clsPlaceOrderDtl();
                         * objOrder.setSubGroupName("");
                         * objOrder.setItemName(line+line);
                         * objOrder.setSaleQty(" ");
                         * listOfRightSidePlaceOrderDtl.add(objOrder);
                         */

                        for (Map.Entry<String, List<clsPlaceOrderDtl>> entryOrder : hmPlaceOrderDtl.entrySet())
                        {
                            double totalQty = 0;
                            orderList = entryOrder.getValue();
                            objOrder = new clsPlaceOrderDtl();
                            objOrder.setSubGroupName((char) 27 + entryOrder.getKey());
                            objOrder.setItemName(entryOrder.getKey());
                            objOrder.setSaleQty("");
                            listOfRightSidePlaceOrderDtl.add(objOrder);
                            objOrder = new clsPlaceOrderDtl();
                            objOrder.setSubGroupName("");
                            objOrder.setItemName(line + line);
                            objOrder.setSaleQty(" ");
                            listOfRightSidePlaceOrderDtl.add(objOrder);
                            for (int i = 0; i < orderList.size(); i++)
                            {
                                objOrder = orderList.get(i);
                                totalQty = totalQty + Double.valueOf(objOrder.getSaleQty());
                                listOfRightSidePlaceOrderDtl.add(objOrder);
                            }
                            totalOrderQty += totalQty;
                            objOrder = new clsPlaceOrderDtl();
                            objOrder.setSubGroupName(entryOrder.getKey());
                            objOrder.setItemName("");
                            objOrder.setSaleQty(line);
                            listOfRightSidePlaceOrderDtl.add(objOrder);
                            objOrder = new clsPlaceOrderDtl();
                            objOrder.setSubGroupName(entryOrder.getKey());
                            objOrder.setItemName("");
                            objOrder.setSaleQty(String.valueOf(totalQty));
                            listOfRightSidePlaceOrderDtl.add(objOrder);
                            objOrder = new clsPlaceOrderDtl();
                            objOrder.setSubGroupName("");
                            objOrder.setItemName("");
                            objOrder.setSaleQty("");
                            listOfRightSidePlaceOrderDtl.add(objOrder);
                        }
                        objOrder = new clsPlaceOrderDtl();
                        objOrder.setSubGroupName("");
                        objOrder.setItemName("Total Order Qty :");
                        objOrder.setSaleQty(String.valueOf(totalOrderQty));
                        listOfRightSidePlaceOrderDtl.add(objOrder);
                        objOrder = new clsPlaceOrderDtl();
                        objOrder.setSubGroupName("");
                        objOrder.setItemName("");
                        objOrder.setSaleQty(" ");
                        listOfRightSidePlaceOrderDtl.add(objOrder);

                    }
                    c++;
                }
            }
            hm.put("LeftSideList", listOfLeftSidePlaceOrderDtl);
            hm.put("RightSideList", listOfRightSidePlaceOrderDtl);

            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);
            //call for view report
            funViewJasperReportForJDBCConnectionDataSource(is, hm, null);

        }

        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void funItemMasterListingReport()
    {
        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();

            List<clsBillDtl> listOfItemMasterListing = new ArrayList<>();
            StringBuilder sqlBuilder = new StringBuilder();

            sqlBuilder.setLength(0);
            sqlBuilder.append("select a.strItemCode,a.strItemName,b.strSubGroupName,c.strGroupName,a.strTaxIndicator  "
                    + "from tblitemmaster a,tblsubgrouphd b,tblgrouphd c "
                    + "where a.strSubGroupCode=b.strSubGroupCode "
                    + "and b.strGroupCode=c.strGroupCode "
                    + "group by c.strGroupCode,b.strSubGroupCode,a.strItemCode,a.strItemName "
                    + "order by c.strGroupCode,b.strSubGroupCode,a.strItemCode,a.strItemName ");

            ResultSet rsItemMaster = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());

            clsBillDtl objItemDtl = null;
            while (rsItemMaster.next())
            {
                objItemDtl = new clsBillDtl();

                objItemDtl.setStrItemCode(rsItemMaster.getString(1));
                objItemDtl.setStrItemName(rsItemMaster.getString(2));
                objItemDtl.setStrSubGroupName(rsItemMaster.getString(3));
                objItemDtl.setStrGroupName(rsItemMaster.getString(4));
                objItemDtl.setStrTaxIndicator(rsItemMaster.getString(5));

                listOfItemMasterListing.add(objItemDtl);
            }
            rsItemMaster.close();
            //call for view report
            funViewJasperReportForBeanCollectionDataSource(is, hm, listOfItemMasterListing);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
