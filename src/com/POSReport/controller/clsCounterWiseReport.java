package com.POSReport.controller;

import com.POSReport.controller.comparator.clsCounterComparator;
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


public class clsCounterWiseReport 
{
    DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();
  public void funCounterWiseReport(String reportType, HashMap hm,String dayEnd)
    {
        String type = hm.get("rptType").toString();  
        String fromDate = hm.get("fromDate").toString();
        String toDate = hm.get("toDate").toString();
        String posCode = hm.get("posCode").toString();
        String shiftNo = hm.get("shiftNo").toString();
        String posName = hm.get("posName").toString();
        if (type.equals("Menu Wise"))
        {
           type = "Menu Wise";
           funCounterWiseMenuHeadWiseReport(reportType,hm,dayEnd);
        }
        else if(type.equals("Group Wise"))
        {
            type = "Group Wise";
            funCounterWiseGroupWiseReport(reportType,hm);
        }
        else
        {
            type = "Sub Group Wise";
            funCounterWiseSubGroupWiseReport(reportType,hm);
        }
        
        
    }
  
  public void funCounterWiseMenuHeadWiseReport(String reportType,HashMap hm,String dayEnd)
    {

        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptCounterWiseMenuHeadSales.jasper");

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String posName = hm.get("posName").toString();
            String type = hm.get("rptType").toString();  
            
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
            if(reportType.equalsIgnoreCase("A4 Size Report"))
            {
            funViewJasperReportForBeanCollectionDataSource(is, hm, listOfCounterWiseMenuHeadWiseData);
            }
            if(reportType.equalsIgnoreCase("Excel Report"))
            {
                Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
                List<String> arrListTotal = new ArrayList<String>();
                List<String> arrHeaderList = new ArrayList<String>();
                double totalQty = 0;
                double totalAmount = 0;
                int i = 1;
                for(clsCounterDtlBean objBean : listOfCounterWiseMenuHeadWiseData)
                {
                List<String> arrListItem = new ArrayList<String>();
                arrListItem.add(objBean.getStrCounterName());
                arrListItem.add(objBean.getStrMenuName());
                arrListItem.add(String.valueOf(gDecimalFormat.format(objBean.getDblRate())));
                arrListItem.add(String.valueOf(objBean.getDblQuantity()));
                arrListItem.add(String.valueOf(gDecimalFormat.format(objBean.getDblAmount())));
                arrListItem.add(" ");

                totalQty = totalQty + objBean.getDblQuantity();
                totalAmount = totalAmount + objBean.getDblAmount();
                mapExcelItemDtl.put(i, arrListItem);
                i++;
                }
                arrListTotal.add(String.valueOf(Math.rint(totalQty)) + "#" + "4");
        arrListTotal.add(String.valueOf(gDecimalFormat.format(totalAmount)) + "#" + "5");

        arrHeaderList.add("Serial No");
        arrHeaderList.add("Counter Name");
        if (type.equalsIgnoreCase("Group Wise"))
        {
            arrHeaderList.add("Group Name");
        }
        else if (type.equalsIgnoreCase("Sub Group Wise"))
        {
            arrHeaderList.add("SubGroup Name");
        }
        else
        {
            arrHeaderList.add("Menu Name");
        }

        arrHeaderList.add("Rate");
        arrHeaderList.add("Qty");
        arrHeaderList.add("Amount");
        arrHeaderList.add(" ");

        List<String> arrparameterList = new ArrayList<String>();
        arrparameterList.add("Counter Wise Report");
        arrparameterList.add("POS" + " : " + posName);
        arrparameterList.add("FromDate" + " : " + fromDate);
        arrparameterList.add("ToDate" + " : " + toDate);
        arrparameterList.add("Type" + " : " + type);
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

        funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "counterWiseExcelSheet",dayEnd);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
  
  public void funCounterWiseGroupWiseReport(String reportType,HashMap hm)
    {
        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptCounterWiseGroupReport.jasper");

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String posName = hm.get("posName").toString();
            String type = hm.get("rptType").toString();  
            
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
            if(reportType.equalsIgnoreCase("A4 Size Report"))
            {
            funViewJasperReportForBeanCollectionDataSource(is, hm, listOfCounterWiseMenuHeadWiseData);
            }
            if(reportType.equalsIgnoreCase("Excel Report"))
            {
               Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
                List<String> arrListTotal = new ArrayList<String>();
                List<String> arrHeaderList = new ArrayList<String>();
                double totalQty = 0;
                double totalAmount = 0;
                int i = 1;
                for(clsCounterDtlBean objBean : listOfCounterWiseMenuHeadWiseData)
                {
                List<String> arrListItem = new ArrayList<String>();
                arrListItem.add(objBean.getStrCounterName());
                arrListItem.add(objBean.getStrMenuName());
                arrListItem.add(String.valueOf(gDecimalFormat.format(objBean.getDblRate())));
                arrListItem.add(String.valueOf(objBean.getDblQuantity()));
                arrListItem.add(String.valueOf(gDecimalFormat.format(objBean.getDblAmount())));
                arrListItem.add(" ");

                totalQty = totalQty + objBean.getDblQuantity();
                totalAmount = totalAmount + objBean.getDblAmount();
                mapExcelItemDtl.put(i, arrListItem);
                i++;
                }
                arrListTotal.add(String.valueOf(Math.rint(totalQty)) + "#" + "4");
        arrListTotal.add(String.valueOf(gDecimalFormat.format(totalAmount)) + "#" + "5");

        arrHeaderList.add("Serial No");
        arrHeaderList.add("Counter Name");
        if (type.equalsIgnoreCase("Group Wise"))
        {
            arrHeaderList.add("Group Name");
        }
        else if (type.equalsIgnoreCase("Sub Group Wise"))
        {
            arrHeaderList.add("SubGroup Name");
        }
        else
        {
            arrHeaderList.add("Menu Name");
        }

        arrHeaderList.add("Rate");
        arrHeaderList.add("Qty");
        arrHeaderList.add("Amount");
        arrHeaderList.add(" ");

        List<String> arrparameterList = new ArrayList<String>();
        arrparameterList.add("Counter Wise Report");
        arrparameterList.add("POS" + " : " + posName);
        arrparameterList.add("FromDate" + " : " + fromDate);
        arrparameterList.add("ToDate" + " : " + toDate);
        arrparameterList.add("Type" + " : " + type);
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

        funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "counterWiseExcelSheet","No"); 
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
  
   public void funCounterWiseSubGroupWiseReport(String reportType,HashMap hm)
    {

        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptCounterWiseSubGroupReport.jasper");

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String posName = hm.get("posName").toString();
            String type = hm.get("rptType").toString();  
            
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
            if(reportType.equalsIgnoreCase("A4 Size Report"))
            {
            funViewJasperReportForBeanCollectionDataSource(is, hm, listOfSubGroupWiseCounterData);
            }
            if(reportType.equalsIgnoreCase("Excel Report"))
            {
                Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
                List<String> arrListTotal = new ArrayList<String>();
                List<String> arrHeaderList = new ArrayList<String>();
                double totalQty = 0;
                double totalAmount = 0;
                int i = 1;
                for(clsCounterDtlBean objBean : listOfSubGroupWiseCounterData)
                {
                List<String> arrListItem = new ArrayList<String>();
                arrListItem.add(objBean.getStrCounterName());
                arrListItem.add(objBean.getStrMenuName());
                arrListItem.add(String.valueOf(gDecimalFormat.format(objBean.getDblRate())));
                arrListItem.add(String.valueOf(objBean.getDblQuantity()));
                arrListItem.add(String.valueOf(gDecimalFormat.format(objBean.getDblAmount())));
                arrListItem.add(" ");

                totalQty = totalQty + objBean.getDblQuantity();
                totalAmount = totalAmount + objBean.getDblAmount();
                mapExcelItemDtl.put(i, arrListItem);
                i++;
                }
                arrListTotal.add(String.valueOf((totalQty)) + "#" + "4");
        arrListTotal.add(String.valueOf(gDecimalFormat.format(totalAmount)) + "#" + "5");

        arrHeaderList.add("Serial No");
        arrHeaderList.add("Counter Name");
        if (type.equalsIgnoreCase("Group Wise"))
        {
            arrHeaderList.add("Group Name");
        }
        else if (type.equalsIgnoreCase("Sub Group Wise"))
        {
            arrHeaderList.add("SubGroup Name");
        }
        else
        {
            arrHeaderList.add("Menu Name");
        }

        arrHeaderList.add("Rate");
        arrHeaderList.add("Qty");
        arrHeaderList.add("Amount");
        arrHeaderList.add(" ");

        List<String> arrparameterList = new ArrayList<String>();
        arrparameterList.add("Counter Wise Report");
        arrparameterList.add("POS" + " : " + posName);
        arrparameterList.add("FromDate" + " : " + fromDate);
        arrparameterList.add("ToDate" + " : " + toDate);
        arrparameterList.add("Type" + " : " + type);
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

        funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "counterWiseExcelSheet","No");
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
  
  public void funCreateExcelSheet(List<String> parameterList, List<String> headerList, Map<Integer, List<String>> map, List<String> totalList, String fileName,String dayEnd)
    {
        String filePath = System.getProperty("user.dir");
        File file = new File(filePath +File.separator+"Reports"+File.separator+ fileName + ".xls");
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

            if(!dayEnd.equalsIgnoreCase("Yes"))
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
