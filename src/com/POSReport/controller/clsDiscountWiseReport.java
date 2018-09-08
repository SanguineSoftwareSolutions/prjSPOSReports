package com.POSReport.controller;

import com.POSReport.controller.comparator.clsDiscountComparator;
import com.POSGlobal.controller.clsBillDtl;
import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsGroupSubGroupWiseSales;
import com.POSGlobal.controller.clsPosConfigFile;
import com.POSReport.controller.comparator.clsBillComparator;
import com.POSReport.controller.comparator.clsGroupSubGroupComparator;
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

public class clsDiscountWiseReport
{
    DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();
    public void funGenerateDsicountWiseReport(String reportType, HashMap hm, String dayEnd)
    {
        try
        {
            String reportName = "";

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String posName = hm.get("posName").toString();
            String type = hm.get("rptType").toString();
	    String currency = hm.get("currency").toString();
            String fromDateToDisplay = hm.get("fromDateToDisplay").toString();
            String toDateToDisplay = hm.get("toDateToDisplay").toString();
            

            Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
            List<String> arrListTotal = new ArrayList<String>();
            List<String> arrHeaderList = new ArrayList<String>();
            double totalDis = 0,totalDiscValue=0;
            double totalAmount = 0,netRevenue=0.0;
            double totalDisOnAmount = 0;
	    
	    if (type.equalsIgnoreCase("Summary"))
            {
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptBillDiscountReport.jasper");
                List<clsBillItemDtlBean> listOfBillItemDtl = new ArrayList<>();
                StringBuilder sbSqlLiveDisc = new StringBuilder();
                StringBuilder sbSqlQFileDisc = new StringBuilder();

                sbSqlLiveDisc.setLength(0);
		String discAmt="b.dblDiscAmt";
		String discOnAmt = "b.dblDiscOnAmt";
		if(currency.equalsIgnoreCase("USD"))
		{
		    discAmt="(b.dblDiscAmt)/a.dblUSDConverionRate";
		    discOnAmt="(b.dblDiscOnAmt)/a.dblUSDConverionRate";
		}
                sbSqlLiveDisc.append("select d.strPosName,date(a.dteBillDate),a.strBillNo,b.dblDiscPer,"+discAmt+","+discOnAmt+",b.strDiscOnType,b.strDiscOnValue "
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
                sbSqlQFileDisc.append("select d.strPosName,date(a.dteBillDate),a.strBillNo,b.dblDiscPer,"+discAmt+","+discOnAmt+",b.strDiscOnType,b.strDiscOnValue "
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
                if (reportType.equalsIgnoreCase("A4 Size Report"))
                {
                    funViewJasperReportForBeanCollectionDataSource(is, hm, listOfBillItemDtl);
                }
                if (reportType.equalsIgnoreCase("Excel Report"))
                {
                    int i = 1;
                   // DecimalFormat gDecimalFormat = new DecimalFormat("0.00");
                    for (clsBillItemDtlBean objBean : listOfBillItemDtl)
                    {
                        List<String> arrListItem = new ArrayList<String>();

                        arrListItem.add(objBean.getStrBillNo());
                        arrListItem.add(objBean.getDteBillDate());
                        arrListItem.add(objBean.getStrPosName());
                        arrListItem.add("" + gDecimalFormat.format(objBean.getDblSubTotal()));
                        arrListItem.add("" + gDecimalFormat.format(objBean.getDblDiscountAmt()));
                        arrListItem.add("" + gDecimalFormat.format(objBean.getDblBillDiscPer()));
                        arrListItem.add("" + objBean.getDblDiscountPer());
                        arrListItem.add(objBean.getStrItemCode());
                        arrListItem.add(objBean.getStrItemName());

                        totalAmount = totalAmount + objBean.getDblSubTotal();
                        totalDis = totalDis + objBean.getDblDiscountAmt();
                        totalDisOnAmount = totalDisOnAmount + objBean.getDblBillDiscPer();
                        mapExcelItemDtl.put(i, arrListItem);
                        i++;
                    }
                    arrListTotal.add(String.valueOf(gDecimalFormat.format(totalDis)) + "#" + "5");
                    arrListTotal.add(String.valueOf(gDecimalFormat.format(totalDisOnAmount)) + "#" + "6");

                    arrHeaderList.add("Serial No");
                    arrHeaderList.add("Bill No");
                    arrHeaderList.add("BillDate");
                    arrHeaderList.add("POS Name");
                    arrHeaderList.add("Amount");
                    arrHeaderList.add("Discount");
                    arrHeaderList.add("Discount On Amt");
                    arrHeaderList.add("Dis Per");
                    arrHeaderList.add("Reason");
                    arrHeaderList.add("Remark");

                    List<String> arrparameterList = new ArrayList<String>();
                    arrparameterList.add("Discount Wise Report");
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

                    funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "discountWiseSummaryExcelSheetReport", dayEnd);
                }

            }
            else//detail
            {

                InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptBillDiscountDetailReport.jasper");
                StringBuilder sqlModiBuilder = new StringBuilder();
                StringBuilder sqlItemBuilder = new StringBuilder();
                List<clsBillItemDtlBean> listOfBillItemDtl = new ArrayList<>();

                sqlItemBuilder.setLength(0);
		String subtotal = "a.dblSubTotal";
		String grandtotal = "a.dblGrandTotal";
		String amount = "sum(b.dblAmount)";
		String dblDiscAmt = "sum(b.dblDiscountAmt)";
		if(currency.equalsIgnoreCase("USD"))
		{
		    subtotal="(a.dblSubTotal)/a.dblUSDConverionRate";
		    grandtotal = "(a.dblGrandTotal)/a.dblUSDConverionRate";
		    amount = "(sum(b.dblAmount))/a.dblUSDConverionRate";
		    dblDiscAmt = "sum(b.dblDiscountAmt)/a.dblUSDConverionRate";
		}
                sqlItemBuilder.append("select a.strBillNo,DATE_FORMAT(a.dteBillDate,'%d-%m-%Y') as dteBillDate,c.strPosName,"+subtotal+","+grandtotal+" "
                        + ",b.strItemCode,b.strItemName,b.dblQuantity,"+amount+","+dblDiscAmt+",b.dblDiscountPer,a.dblDiscountPer as dblBillDiscPer  "
                        + ",ifnull(d.strReasonName,'')strReasonName,ifnull(a.strDiscountRemark,'')strDiscountRemark "
                        + ",e.strUserEdited "
                        + "from tblbillhd a "
                        + "inner join  tblbilldtl b on a.strBillNo=b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate) "
                        + "LEFT OUTER JOIN tblbilldiscdtl e ON e.strBillNo=a.strBillNo AND DATE(a.dteBillDate)= DATE(e.dteBillDate) "
                        + "inner join tblposmaster c on a.strPOSCode=c.strPOSCode "
                        + "left JOIN  tblreasonmaster d on d.strReasonCode=e.strDiscReasonCode "
                        + "where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "'  "
                        + "and b.dblDiscountPer>0 ");
                if (!posCode.equalsIgnoreCase("All"))
                {
                    sqlItemBuilder.append(" and a.strPOSCode='" + posCode + "' ");
                    sqlItemBuilder.append(" and a.strPOSCode='" + posCode + "' ");
                }

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
                    objBean.setStrReasonName(rsLiveDisc.getString(13));
                    objBean.setStrDiscountRemark(rsLiveDisc.getString(14));
                    objBean.setStrSettelmentMode(rsLiveDisc.getString(15)); //UserEdited

                    listOfBillItemDtl.add(objBean);
                }
                rsLiveDisc.close();
                //live modifiers
                sqlModiBuilder.setLength(0);
		
                sqlModiBuilder.append("select a.strBillNo,DATE_FORMAT(a.dteBillDate,'%d-%m-%Y') as dteBillDate,c.strPosName,"+subtotal+","+grandtotal+" "
                        + ",b.strItemCode,b.strModifierName,b.dblQuantity,"+amount+","+dblDiscAmt+",b.dblDiscPer,a.dblDiscountPer as dblBillDiscPer "
                        + " ,ifnull(d.strReasonName,'')strReasonName,ifnull(a.strDiscountRemark,'')strDiscountRemark "
                        + ",e.strUserEdited "
                        + "from tblbillhd a "
                        + "inner join  tblbillmodifierdtl b on a.strBillNo=b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate) "
                        + "LEFT OUTER JOIN tblbilldiscdtl e ON e.strBillNo=a.strBillNo AND DATE(a.dteBillDate)= DATE(e.dteBillDate) "
                        + "inner join tblposmaster c on a.strPOSCode=c.strPOSCode "
                        + "left JOIN  tblreasonmaster d on d.strReasonCode=e.strDiscReasonCode "
                        + "where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "'  "
                        + "and b.dblDiscPer>0 ");
                if (!posCode.equalsIgnoreCase("All"))
                {
                    sqlModiBuilder.append(" and a.strPOSCode='" + posCode + "' ");
                    sqlModiBuilder.append(" and a.strPOSCode='" + posCode + "' ");
                }

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
                    objBean.setStrReasonName(rsLiveDisc.getString(13));
                    objBean.setStrDiscountRemark(rsLiveDisc.getString(14));
                    objBean.setStrSettelmentMode(rsLiveDisc.getString(15)); //UserEdited
                     
                    listOfBillItemDtl.add(objBean);
                }
                rsLiveDisc.close();

                //QFile
                sqlItemBuilder.setLength(0);
                sqlItemBuilder.append("select a.strBillNo,DATE_FORMAT(a.dteBillDate,'%d-%m-%Y') as dteBillDate,c.strPosName,"+subtotal+","+grandtotal+""
                        + ",b.strItemCode,b.strItemName,b.dblQuantity,"+amount+","+dblDiscAmt+",b.dblDiscountPer,a.dblDiscountPer as dblBillDiscPer "
                        + ",ifnull(d.strReasonName,'')strReasonName,ifnull(a.strDiscountRemark,'')strDiscountRemark "
                        + ",e.strUserEdited "
                        + "from tblqbillhd a "
                        + "inner join  tblqbilldtl b on a.strBillNo=b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate) "
                        + "LEFT OUTER JOIN tblqbilldiscdtl e ON e.strBillNo=a.strBillNo AND DATE(a.dteBillDate)= DATE(e.dteBillDate) "
                        + "inner join tblposmaster c on a.strPOSCode=c.strPOSCode "
                        + "left JOIN  tblreasonmaster d on d.strReasonCode=e.strDiscReasonCode "
                        + "where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "'  "
                        + "and b.dblDiscountPer>0 ");
                if (!posCode.equalsIgnoreCase("All"))
                {
                    sqlItemBuilder.append(" and a.strPOSCode='" + posCode + "' ");
                    sqlItemBuilder.append(" and a.strPOSCode='" + posCode + "' ");
                }

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
                    objBean.setStrReasonName(rsQDisc.getString(13));
                    objBean.setStrDiscountRemark(rsQDisc.getString(14));
                    objBean.setStrSettelmentMode(rsQDisc.getString(15)); //UserEdited
                    listOfBillItemDtl.add(objBean);
                }
                rsQDisc.close();
                //QFile modifiers
                sqlModiBuilder.setLength(0);
                sqlModiBuilder.append("select a.strBillNo,DATE_FORMAT(a.dteBillDate,'%d-%m-%Y') as dteBillDate,c.strPosName,"+subtotal+","+grandtotal+" "
                        + ",b.strItemCode,b.strModifierName,b.dblQuantity,"+amount+","+dblDiscAmt+",b.dblDiscPer,a.dblDiscountPer as dblBillDiscPer "
                        + ",ifnull(d.strReasonName,'')strReasonName,ifnull(a.strDiscountRemark,'')strDiscountRemark "
                        + ",e.strUserEdited "
                        + "from tblqbillhd a "
                        + "inner join  tblqbillmodifierdtl b on a.strBillNo=b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate) "
                        + "LEFT OUTER JOIN tblqbilldiscdtl e ON e.strBillNo=a.strBillNo AND DATE(a.dteBillDate)= DATE(e.dteBillDate) "
                        + "inner join tblposmaster c on a.strPOSCode=c.strPOSCode "
                        + "left JOIN  tblreasonmaster d on d.strReasonCode=e.strDiscReasonCode  "
                        + "where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "'  "
                        + "and b.dblDiscPer>0 ");
                if (!posCode.equalsIgnoreCase("All"))
                {
                    sqlModiBuilder.append(" and a.strPOSCode='" + posCode + "' ");
                    sqlModiBuilder.append(" and a.strPOSCode='" + posCode + "' ");
                }
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
                    objBean.setStrReasonName(rsQDisc.getString(13));
                    objBean.setStrDiscountRemark(rsQDisc.getString(14));
                    objBean.setStrSettelmentMode(rsQDisc.getString(15)); //UserEdited
                    listOfBillItemDtl.add(objBean);
                }
                rsQDisc.close();

                double totalGrossSales = 0.00;

                //to calculate gross revenue
                //live
                sqlItemBuilder.setLength(0);
		String settlementAmt = "sum(a.dblSettlementAmt)";
		if(currency.equalsIgnoreCase("USD"))
		{
		    settlementAmt = "sum(a.dblSettlementAmt) / b.dblUSDConverionRate ";
		}    
                sqlItemBuilder.append("select "+settlementAmt+" "
                        + "from tblbillsettlementdtl a,tblbillhd b,tblposmaster c "
                        + "where a.strBillNo=b.strBillNo "
                        + "and date(a.dteBillDate)=date(b.dteBillDate) "
                        + "and b.strPOSCode=c.strPosCode "
                        + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "'  ");
                if (!posCode.equalsIgnoreCase("All"))
                {
                    sqlItemBuilder.append(" and b.strPOSCode='" + posCode + "' ");
                }
                ResultSet rsLiveGross = clsGlobalVarClass.dbMysql.executeResultSet(sqlItemBuilder.toString());
                if (rsLiveGross.next())
                {
                    totalGrossSales += rsLiveGross.getDouble(1);
                }
                rsLiveGross.close();

                //q
                sqlItemBuilder.setLength(0);
                sqlItemBuilder.append("select "+settlementAmt+" "
                        + "from tblqbillsettlementdtl a,tblqbillhd b,tblposmaster c "
                        + "where a.strBillNo=b.strBillNo "
                        + "and date(a.dteBillDate)=date(b.dteBillDate) "
                        + "and b.strPOSCode=c.strPosCode "
                        + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "'  ");
                if (!posCode.equalsIgnoreCase("All"))
                {
                    sqlItemBuilder.append(" and b.strPOSCode='" + posCode + "' ");
                }
                rsLiveGross = clsGlobalVarClass.dbMysql.executeResultSet(sqlItemBuilder.toString());
                if (rsLiveGross.next())
                {
                    totalGrossSales += rsLiveGross.getDouble(1);
                }
                rsLiveGross.close();

                Comparator<clsBillItemDtlBean> billDateComparator = new Comparator<clsBillItemDtlBean>()
                {

                    @Override
                    public int compare(clsBillItemDtlBean o1, clsBillItemDtlBean o2)
                    {
                        return o1.getDteBillDate().compareTo(o2.getDteBillDate());
                    }
                };

                Comparator<clsBillItemDtlBean> billNoComparator = new Comparator<clsBillItemDtlBean>()
                {

                    @Override
                    public int compare(clsBillItemDtlBean o1, clsBillItemDtlBean o2)
                    {
                        return o1.getStrBillNo().compareTo(o2.getStrBillNo());
                    }
                };

                Comparator<clsBillItemDtlBean> itemCodeComparator = new Comparator<clsBillItemDtlBean>()
                {

                    @Override
                    public int compare(clsBillItemDtlBean o1, clsBillItemDtlBean o2)
                    {
                        return o1.getStrItemCode().substring(0, 7).compareTo(o2.getStrItemCode().substring(0, 7));
                    }
                };

                Collections.sort(listOfBillItemDtl, new clsDiscountComparator(
                        billDateComparator, billNoComparator, itemCodeComparator
                ));

                //for group wise sales
                StringBuilder sbSqlLive = new StringBuilder();
                StringBuilder sbSqlQFile = new StringBuilder();
                StringBuilder sbSqlFilters = new StringBuilder();

                sbSqlLive.setLength(0);
                sbSqlQFile.setLength(0);
                sbSqlFilters.setLength(0);
		String subtotalAmt = "sum( b.dblAmount)-sum(b.dblDiscountAmt)";
		String modiSubtotalAmt = "sum(b.dblAmount)-sum(b.dblDiscAmt)";
		String grandTotalAmt = "sum( b.dblAmount)-sum(b.dblDiscountAmt)+sum(b.dblTaxAmount)";
		String dblAmount = "sum(b.dblAmount)";
		String dblDiscountAmt="sum(b.dblDiscountAmt)";
		String modifDiscAmt = "sum(b.dblDiscAmt)";
		if(currency.equalsIgnoreCase("USD"))
		{
		    subtotalAmt = "(sum( b.dblAmount)-sum(b.dblDiscountAmt))/a.dblUSDConverionRate";
		    grandTotalAmt = "(sum( b.dblAmount)-sum(b.dblDiscountAmt)+sum(b.dblTaxAmount))/a.dblUSDConverionRate";
		    dblAmount = "sum(b.dblAmount)/a.dblUSDConverionRate";
		    dblDiscountAmt="sum(b.dblDiscountAmt)/a.dblUSDConverionRate";
		    modiSubtotalAmt = "(sum(b.dblAmount)-sum(b.dblDiscAmt))/a.dblUSDConverionRate";
		    modifDiscAmt = "sum(b.dblDiscAmt)/a.dblUSDConverionRate";
		}    
                sbSqlQFile.append("SELECT c.strGroupCode,c.strGroupName,sum( b.dblQuantity)"
                        + ","+subtotalAmt+" "
                        + ",f.strPosName, '" + clsGlobalVarClass.gUserCode + "',b.dblRate ,"+dblAmount+","+dblDiscountAmt+",a.strPOSCode,"
                        + ""+grandTotalAmt+"  "
                        + "FROM tblqbillhd a,tblqbilldtl b,tblgrouphd c,tblsubgrouphd d"
                        + ",tblitemmaster e,tblposmaster f "
                        + "where a.strBillNo=b.strBillNo "
                        + " and date(a.dteBillDate)=date(b.dteBillDate) "
                        + " and a.strPOSCode=f.strPOSCode  "
                        + " and a.strClientCode=b.strClientCode "
                        + "and b.strItemCode=e.strItemCode "
                        + "and c.strGroupCode=d.strGroupCode and d.strSubGroupCode=e.strSubGroupCode ");

                sbSqlLive.append("SELECT c.strGroupCode,c.strGroupName,sum( b.dblQuantity)"
                        + ","+subtotalAmt+" "
                        + ",f.strPosName, '" + clsGlobalVarClass.gUserCode + "',b.dblRate ,"+dblAmount+","+dblDiscountAmt+",a.strPOSCode,"
                        + " "+grandTotalAmt+"  "
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
                        + ",sum(b.dblQuantity),"+modiSubtotalAmt+",f.strPOSName"
                        + ",'" + clsGlobalVarClass.gUserCode + "','0' ,"+dblAmount+","+modifDiscAmt+",a.strPOSCode,"
                        + " "+modiSubtotalAmt+"  "
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
                        + ",sum(b.dblQuantity),"+modiSubtotalAmt+",f.strPOSName"
                        + ",'" + clsGlobalVarClass.gUserCode + "','0' ,"+dblAmount+","+modifDiscAmt+",a.strPOSCode,"
                        + " "+modiSubtotalAmt+" "
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
                sbSqlFilters.append(" Group BY c.strGroupCode ");

                sbSqlLive.append(sbSqlFilters);
                sbSqlQFile.append(sbSqlFilters);
                sqlModLive += " " + sbSqlFilters;
                sqlModQFile += " " + sbSqlFilters;

                TreeMap<String, clsGroupSubGroupWiseSales> mapGroupWiseSales = new TreeMap<String, clsGroupSubGroupWiseSales>();
                double totalDiscount = 0.00, totalNetRevenue = 0.00, totalSubTotal = 0.00;

                ResultSet rsGroupWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
                while (rsGroupWiseSales.next())
                {
                    String groupCode = rsGroupWiseSales.getString(1);
                    double netTotal = rsGroupWiseSales.getDouble(4);
                    double subTotal = rsGroupWiseSales.getDouble(8);
                    double discAmt = rsGroupWiseSales.getDouble(9);

                    totalNetRevenue += netTotal;
                    totalSubTotal += subTotal;
                    totalDiscount += discAmt;

                    if (mapGroupWiseSales.containsKey(groupCode))
                    {
                        clsGroupSubGroupWiseSales objOldGroupWiseSales = mapGroupWiseSales.get(groupCode);

                        clsGroupSubGroupWiseSales objNewGroupWiseSales = new clsGroupSubGroupWiseSales(
                                rsGroupWiseSales.getString(1), rsGroupWiseSales.getString(2), rsGroupWiseSales.getString(5), rsGroupWiseSales.getDouble(3), rsGroupWiseSales.getDouble(4), rsGroupWiseSales.getDouble(8), rsGroupWiseSales.getDouble(9), rsGroupWiseSales.getDouble(11));

                        objOldGroupWiseSales.setDblNetTotal(objOldGroupWiseSales.getDblNetTotal() + objNewGroupWiseSales.getDblNetTotal());
                        objOldGroupWiseSales.setDiscAmt(objOldGroupWiseSales.getDiscAmt() + objNewGroupWiseSales.getDiscAmt());
                        objOldGroupWiseSales.setSubTotal(objOldGroupWiseSales.getSubTotal() + objNewGroupWiseSales.getSubTotal());

                    }
                    else
                    {
                        clsGroupSubGroupWiseSales objGroupWiseSales = new clsGroupSubGroupWiseSales(
                                rsGroupWiseSales.getString(1), rsGroupWiseSales.getString(2), rsGroupWiseSales.getString(5), rsGroupWiseSales.getDouble(3), rsGroupWiseSales.getDouble(4), rsGroupWiseSales.getDouble(8), rsGroupWiseSales.getDouble(9), rsGroupWiseSales.getDouble(11));
                    }
                }
                rsGroupWiseSales.close();

                rsGroupWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlModLive);
                while (rsGroupWiseSales.next())
                {
                     String groupCode = rsGroupWiseSales.getString(1);
                    double netTotal = rsGroupWiseSales.getDouble(4);
                    double subTotal = rsGroupWiseSales.getDouble(8);
                    double discAmt = rsGroupWiseSales.getDouble(9);

                    totalNetRevenue += netTotal;
                    totalSubTotal += subTotal;
                    totalDiscount += discAmt;

                    if (mapGroupWiseSales.containsKey(groupCode))
                    {
                        clsGroupSubGroupWiseSales objOldGroupWiseSales = mapGroupWiseSales.get(groupCode);

                        clsGroupSubGroupWiseSales objNewGroupWiseSales = new clsGroupSubGroupWiseSales(
                                rsGroupWiseSales.getString(1), rsGroupWiseSales.getString(2), rsGroupWiseSales.getString(5), rsGroupWiseSales.getDouble(3), rsGroupWiseSales.getDouble(4), rsGroupWiseSales.getDouble(8), rsGroupWiseSales.getDouble(9), rsGroupWiseSales.getDouble(11));

                        objOldGroupWiseSales.setDblNetTotal(objOldGroupWiseSales.getDblNetTotal() + objNewGroupWiseSales.getDblNetTotal());
                        objOldGroupWiseSales.setDiscAmt(objOldGroupWiseSales.getDiscAmt() + objNewGroupWiseSales.getDiscAmt());
                        objOldGroupWiseSales.setSubTotal(objOldGroupWiseSales.getSubTotal() + objNewGroupWiseSales.getSubTotal());

                    }
                    else
                    {
                        clsGroupSubGroupWiseSales objGroupWiseSales = new clsGroupSubGroupWiseSales(
                                rsGroupWiseSales.getString(1), rsGroupWiseSales.getString(2), rsGroupWiseSales.getString(5), rsGroupWiseSales.getDouble(3), rsGroupWiseSales.getDouble(4), rsGroupWiseSales.getDouble(8), rsGroupWiseSales.getDouble(9), rsGroupWiseSales.getDouble(11));
                    }
                }
                rsGroupWiseSales.close();

                rsGroupWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFile.toString());
                while (rsGroupWiseSales.next())
                {
                     String groupCode = rsGroupWiseSales.getString(1);
                    double netTotal = rsGroupWiseSales.getDouble(4);
                    double subTotal = rsGroupWiseSales.getDouble(8);
                    double discAmt = rsGroupWiseSales.getDouble(9);

                    totalNetRevenue += netTotal;
                    totalSubTotal += subTotal;
                    totalDiscount += discAmt;

                    if (mapGroupWiseSales.containsKey(groupCode))
                    {
                        clsGroupSubGroupWiseSales objOldGroupWiseSales = mapGroupWiseSales.get(groupCode);

                        clsGroupSubGroupWiseSales objNewGroupWiseSales = new clsGroupSubGroupWiseSales(
                                rsGroupWiseSales.getString(1), rsGroupWiseSales.getString(2), rsGroupWiseSales.getString(5), rsGroupWiseSales.getDouble(3), rsGroupWiseSales.getDouble(4), rsGroupWiseSales.getDouble(8), rsGroupWiseSales.getDouble(9), rsGroupWiseSales.getDouble(11));

                        objOldGroupWiseSales.setDblNetTotal(objOldGroupWiseSales.getDblNetTotal() + objNewGroupWiseSales.getDblNetTotal());
                        objOldGroupWiseSales.setDiscAmt(objOldGroupWiseSales.getDiscAmt() + objNewGroupWiseSales.getDiscAmt());
                        objOldGroupWiseSales.setSubTotal(objOldGroupWiseSales.getSubTotal() + objNewGroupWiseSales.getSubTotal());

                    }
                    else
                    {
                        clsGroupSubGroupWiseSales objGroupWiseSales = new clsGroupSubGroupWiseSales(
                                rsGroupWiseSales.getString(1), rsGroupWiseSales.getString(2), rsGroupWiseSales.getString(5), rsGroupWiseSales.getDouble(3), rsGroupWiseSales.getDouble(4), rsGroupWiseSales.getDouble(8), rsGroupWiseSales.getDouble(9), rsGroupWiseSales.getDouble(11));
                    }
                }
                rsGroupWiseSales.close();
                rsGroupWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlModQFile);
                while (rsGroupWiseSales.next())
                {
                     String groupCode = rsGroupWiseSales.getString(1);
                    double netTotal = rsGroupWiseSales.getDouble(4);
                    double subTotal = rsGroupWiseSales.getDouble(8);
                    double discAmt = rsGroupWiseSales.getDouble(9);

                    totalNetRevenue += netTotal;
                    totalSubTotal += subTotal;
                    totalDiscount += discAmt;

                    if (mapGroupWiseSales.containsKey(groupCode))
                    {
                        clsGroupSubGroupWiseSales objOldGroupWiseSales = mapGroupWiseSales.get(groupCode);

                        clsGroupSubGroupWiseSales objNewGroupWiseSales = new clsGroupSubGroupWiseSales(
                                rsGroupWiseSales.getString(1), rsGroupWiseSales.getString(2), rsGroupWiseSales.getString(5), rsGroupWiseSales.getDouble(3), rsGroupWiseSales.getDouble(4), rsGroupWiseSales.getDouble(8), rsGroupWiseSales.getDouble(9), rsGroupWiseSales.getDouble(11));

                        objOldGroupWiseSales.setDblNetTotal(objOldGroupWiseSales.getDblNetTotal() + objNewGroupWiseSales.getDblNetTotal());
                        objOldGroupWiseSales.setDiscAmt(objOldGroupWiseSales.getDiscAmt() + objNewGroupWiseSales.getDiscAmt());
                        objOldGroupWiseSales.setSubTotal(objOldGroupWiseSales.getSubTotal() + objNewGroupWiseSales.getSubTotal());

                    }
                    else
                    {
                        clsGroupSubGroupWiseSales objGroupWiseSales = new clsGroupSubGroupWiseSales(
                                rsGroupWiseSales.getString(1), rsGroupWiseSales.getString(2), rsGroupWiseSales.getString(5), rsGroupWiseSales.getDouble(3), rsGroupWiseSales.getDouble(4), rsGroupWiseSales.getDouble(8), rsGroupWiseSales.getDouble(9), rsGroupWiseSales.getDouble(11));
                    }
                }
                rsGroupWiseSales.close();

                hm.put("totalGrossRevenue", totalGrossSales);
                hm.put("subTotal", totalSubTotal);
                hm.put("totalNetRevenue", totalNetRevenue);
                hm.put("totalDiscount", totalDiscount);

                //call for view report
                if (reportType.equalsIgnoreCase("A4 Size Report"))
                {
                    funViewJasperReportForBeanCollectionDataSource(is, hm, listOfBillItemDtl);
                }
                if (reportType.equalsIgnoreCase("Excel Report"))
                {
                    int i = 1;
                    //DecimalFormat gDecimalFormat = new DecimalFormat("0.00");
                    DecimalFormat decFormatterForQty = new DecimalFormat("###0");
                    for (clsBillItemDtlBean objBean : listOfBillItemDtl)
                    {
                        List<String> arrListItem = new ArrayList<String>();
                       
                        arrListItem.add(objBean.getStrBillNo());
                        arrListItem.add(objBean.getDteBillDate());
                        arrListItem.add(objBean.getStrPosName());
                        arrListItem.add(objBean.getStrItemName());
                        arrListItem.add("" + decFormatterForQty.format(objBean.getDblQuantity()));
                        arrListItem.add("" + gDecimalFormat.format(objBean.getDblAmount()));
                        arrListItem.add("" + gDecimalFormat.format(objBean.getDblDiscountAmt()));
                        arrListItem.add("" +objBean.getDblDiscountPer());

                        totalAmount = totalAmount + objBean.getDblAmount();
                        totalDis = totalDis + objBean.getDblDiscountAmt();
                        mapExcelItemDtl.put(i, arrListItem);
                        i++;
                    }
                    arrListTotal.add(String.valueOf(gDecimalFormat.format(totalAmount)) + "#" + "6");
                    arrListTotal.add(String.valueOf(gDecimalFormat.format(totalDis)) + "#" + "7");

                    arrHeaderList.add("Serial No");
                    arrHeaderList.add("Bill No");
                    arrHeaderList.add("BillDate");
                    arrHeaderList.add("POS Name");
                    arrHeaderList.add("ItemName");
                    arrHeaderList.add("Quantity");
                    arrHeaderList.add("Amount");
                    arrHeaderList.add("Discount");
                    arrHeaderList.add("Discount %");

                    List<String> arrparameterList = new ArrayList<String>();
                    arrparameterList.add("Discount Wise Report");
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

                    funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "discountWiseDetailExcelSheetReport",dayEnd);

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
