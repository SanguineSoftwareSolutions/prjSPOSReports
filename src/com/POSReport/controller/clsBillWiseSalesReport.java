/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.POSReport.controller;

import com.POSReport.controller.comparator.clsBillComparator;
import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsPosConfigFile;
import com.POSGlobal.controller.clsVoidBillDtl;
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

/**
 *
 * @author Manisha
 */
public class clsBillWiseSalesReport
{

    DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();

    public void funGenerateBillWiseReport(String reportType, HashMap hm, String dayEnd)
    {
	try
	{
	    String reportName = "com/POSReport/reports/rptBillWiseSalesReport.jasper";
	    InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

	    String fromDate = hm.get("fromDate").toString();
	    String toDate = hm.get("toDate").toString();
	    String posCode = hm.get("posCode").toString();
	    String shiftNo = hm.get("shiftNo").toString();
	    String posName = hm.get("posName").toString();
	    String fromDateToDisplay = hm.get("fromDateToDisplay").toString();
	    String toDateToDisplay = hm.get("toDateToDisplay").toString();
	    String currency = hm.get("currency").toString();
	    Map mapMultiSettleBills = new HashMap();

	    StringBuilder sqlBuilder = new StringBuilder();
	    //live
	    sqlBuilder.setLength(0);
	    sqlBuilder.append("select a.strBillNo,DATE_FORMAT(date(a.dteBillDate),'%d-%m-%Y') as dteBillDate ,b.strPosName, "
		    + "ifnull(d.strSettelmentDesc,'') as strSettelmentMode,");
	    if(currency.equalsIgnoreCase("USD"))
	    {
		sqlBuilder.append("a.dblDiscountAmt/a.dblUSDConverionRate,a.dblTaxAmt/a.dblUSDConverionRate,"
			+ "sum(c.dblSettlementAmt)/a.dblUSDConverionRate as dblSettlementAmt,a.dblSubTotal/a.dblUSDConverionRate");
	    }	
	    else
	    {
		sqlBuilder.append("a.dblDiscountAmt,a.dblTaxAmt,sum(c.dblSettlementAmt) as dblSettlementAmt,a.dblSubTotal");
	    }	
	    sqlBuilder.append(",a.strSettelmentMode,intBillSeriesPaxNo "
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
	    sqlBuilder.append("GROUP BY b.strPosName,a.strClientCode,date(a.dteBillDate),a.strBillNo,d.strSettelmentCode "
		    + "ORDER BY a.strSettelmentMode,a.strBillNo ASC ");

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
		    + "ifnull(d.strSettelmentDesc,'') as strSettelmentMode,");
	    if(currency.equalsIgnoreCase("USD"))
	    {
		sqlBuilder.append("a.dblDiscountAmt/a.dblUSDConverionRate,a.dblTaxAmt/a.dblUSDConverionRate,"
			+ "sum(c.dblSettlementAmt)/a.dblUSDConverionRate as dblSettlementAmt,a.dblSubTotal/a.dblUSDConverionRate");
	    }	
	    else
	    {
		sqlBuilder.append("a.dblDiscountAmt,a.dblTaxAmt,sum(c.dblSettlementAmt) as dblSettlementAmt,a.dblSubTotal");
	    }	    
	    sqlBuilder.append(",a.strSettelmentMode,intBillSeriesPaxNo "
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
	    sqlBuilder.append("GROUP BY b.strPosName,a.strClientCode,date(a.dteBillDate),a.strBillNo,d.strSettelmentCode "
		    + "ORDER BY a.strSettelmentMode,a.strBillNo ASC ");

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
	    String roundOffAmount = "sum(a.dblRoundOff)dblRoundOff";
	    if(currency.equalsIgnoreCase("USD"))
	    {
		roundOffAmount = "sum(a.dblRoundOff)/a.dblUSDConverionRate dblRoundOff";
	    }	
	    
	    StringBuilder sqlRoundOff = new StringBuilder("select sum(b.dblRoundOff) "
		    + "from "
		    + "(select "+roundOffAmount+" "
		    + "from tblbillhd a "
		    + "where date(a.dteBillDate) between '" + fromDate + "' and  '" + toDate + "'  ");
	    if (!posCode.equalsIgnoreCase("All"))
	    {
		sqlRoundOff.append("and a.strPOSCode='" + posCode + "' ");
	    }
	    if (!shiftNo.equalsIgnoreCase("All"))
	    {
		sqlRoundOff.append("and a.intShiftCode='" + shiftNo + "'  ");
	    }
	    sqlRoundOff.append("union  "
		    + "select "+roundOffAmount+" "
		    + "from tblqbillhd a "
		    + "where date(a.dteBillDate) between '" + fromDate + "' and  '" + toDate + "'  ");
	    if (!posCode.equalsIgnoreCase("All"))
	    {
		sqlRoundOff.append("and a.strPOSCode='" + posCode + "' ");
	    }
	    if (!shiftNo.equalsIgnoreCase("All"))
	    {
		sqlRoundOff.append("and a.intShiftCode='" + shiftNo + "'  ");
	    }
	    sqlRoundOff.append(") b ");

	    double roundOff = 0.00;
	    ResultSet rsRoundOff = clsGlobalVarClass.dbMysql.executeResultSet(sqlRoundOff.toString());
	    if (rsRoundOff.next())
	    {
		roundOff = rsRoundOff.getDouble(1);

	    }
	    rsRoundOff.close();

	    hm.put("RoundOff", roundOff);

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

	    Comparator<clsBillItemDtlBean> posNameComparator = new Comparator<clsBillItemDtlBean>()
	    {

		@Override
		public int compare(clsBillItemDtlBean o1, clsBillItemDtlBean o2)
		{
		    return o1.getStrPosName().compareToIgnoreCase(o2.getStrPosName());
		}
	    };

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
		    posNameComparator, billDateComparator, billNoComparator
	    ));
	    	    	    	    
	    hm.put("listOfVoidBillData", listOfVoidBillData);
	    
	    
	    StringBuilder sbSqlLive = new StringBuilder();
            StringBuilder sbSqlQFile = new StringBuilder();
            StringBuilder sqlFilter = new StringBuilder();
            //DecimalFormat gDecimalFormat = new DecimalFormat("0.00");
            DecimalFormat decimalFormat0Dec = new DecimalFormat("0");

	    String settlementAmt = "SUM(b.dblSettlementAmt) ";
	    if(currency.equalsIgnoreCase("USD"))
	    {
		 settlementAmt = " SUM(b.dblSettlementAmt)/c.dblUSDConverionRate ";
	    }	
            sbSqlLive.append("select ifnull(c.strPosCode,'All'),a.strSettelmentDesc, ifnull("+settlementAmt+",0.00) "
                    + ",ifnull(d.strposname,'All'), if(c.strPOSCode is null,0,COUNT(*)) "
                    + "from tblsettelmenthd a "
                    + "left outer join tblbillsettlementdtl b on a.strSettelmentCode=b.strSettlementCode and date(b.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
                    + "left outer join tblbillhd c on b.strBillNo=c.strBillNo and date(b.dteBillDate)=date(c.dteBillDate) "
                    + "left outer join tblposmaster d on c.strPOSCode=d.strPosCode ");

            sbSqlQFile.append("select ifnull(c.strPosCode,'All'),a.strSettelmentDesc, ifnull("+settlementAmt+",0.00) "
                    + ",ifnull(d.strposname,'All'), if(c.strPOSCode is null,0,COUNT(*)) "
                    + "from tblsettelmenthd a "
                    + "left outer join tblqbillsettlementdtl b on a.strSettelmentCode=b.strSettlementCode and date(b.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
                    + "left outer join tblqbillhd c on b.strBillNo=c.strBillNo and date(b.dteBillDate)=date(c.dteBillDate) "
                    + "left outer join tblposmaster d on c.strPOSCode=d.strPosCode ");

            sqlFilter.append(" where a.strSettelmentType!='Complementary' "
                    + "and a.strApplicable='Yes' ");

            if (!"All".equalsIgnoreCase(posCode))
            {
                sqlFilter.append("and  c.strPosCode='" + posCode + "' ");
            }
            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sqlFilter.append(" and c.intShiftCode = '" + shiftNo + "' ");
                }
            }

            sqlFilter.append("group by a.strSettelmentCode "
                    + "order by b.dblSettlementAmt desc ");

            sbSqlLive.append(sqlFilter);
            sbSqlQFile.append(sqlFilter);

            rsData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());

            Map<String, clsBillItemDtlBean> mapSettlementModes = new HashMap<>();

            double grossRevenue = 0;
            while (rsData.next())
            {
                String settlementName = rsData.getString(2);
                if (mapSettlementModes.containsKey(settlementName))
                {
                    clsBillItemDtlBean obj = mapSettlementModes.get(settlementName);

                    obj.setDblSettlementAmt(obj.getDblSettlementAmt() + rsData.getDouble(3));
                    obj.setNoOfBills(obj.getNoOfBills() + rsData.getInt(5));

                }
                else
                {
                    clsBillItemDtlBean obj = new clsBillItemDtlBean();
                    obj.setStrPosCode(rsData.getString(1));
                    obj.setStrSettelmentMode(settlementName);
                    obj.setDblSettlementAmt(rsData.getDouble(3));
                    obj.setStrPosName(rsData.getString(4));
                    obj.setNoOfBills(rsData.getInt(5));

                    mapSettlementModes.put(settlementName, obj);

                }

                grossRevenue += rsData.getDouble(3);

            }

            rsData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFile.toString());
            while (rsData.next())
            {
                String settlementName = rsData.getString(2);
                if (mapSettlementModes.containsKey(settlementName))
                {
                    clsBillItemDtlBean obj = mapSettlementModes.get(settlementName);

                    obj.setDblSettlementAmt(obj.getDblSettlementAmt() + rsData.getDouble(3));
                    obj.setNoOfBills(obj.getNoOfBills() + rsData.getInt(5));

                }
                else
                {
                    clsBillItemDtlBean obj = new clsBillItemDtlBean();
                    obj.setStrPosCode(rsData.getString(1));
                    obj.setStrSettelmentMode(settlementName);
                    obj.setDblSettlementAmt(rsData.getDouble(3));
                    obj.setStrPosName(rsData.getString(4));
                    obj.setNoOfBills(rsData.getInt(5));

                    mapSettlementModes.put(settlementName, obj);

                }

                grossRevenue += rsData.getDouble(3);
            }

            hm.put("grossRevenue", grossRevenue);

            List<clsBillItemDtlBean> listOfSettlementData = new ArrayList<clsBillItemDtlBean>();

            for (clsBillItemDtlBean objDtlBean : mapSettlementModes.values())
            {
                listOfSettlementData.add(objDtlBean);
            }
            Comparator<clsBillItemDtlBean> amtComparator = new Comparator<clsBillItemDtlBean>()
            {

                @Override
                public int compare(clsBillItemDtlBean o1, clsBillItemDtlBean o2)
                {
                    if (o1.getDblSettlementAmt() == o2.getDblSettlementAmt())
                    {
                        return 0;
                    }
                    else if (o1.getDblSettlementAmt() > o2.getDblSettlementAmt())
                    {
                        return -1;
                    }
                    else
                    {
                        return 1;
                    }
                }
            };

            Collections.sort(listOfSettlementData, amtComparator);
	    hm.put("listOfSettlement", listOfSettlementData);
	    
	    
	    
	    
	    //call for view report
	    if (reportType.equalsIgnoreCase("A4 Size Report"))
	    {
		funViewJasperReportForBeanCollectionDataSource(is, hm, listOfBillData);
	    }
	    if (reportType.equalsIgnoreCase("Excel Report"))
	    {
		double totalSubTotal = 0;
		double totalTaxAmt = 0;
		double totalGrandAmount = 0;
		double discountTotal = 0;
		int i = 1;
		//DecimalFormat decFormatFor2Decimal = new DecimalFormat("0.00");
		Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
		List<String> arrListTotal = new ArrayList<String>();
		List<String> arrHeaderList = new ArrayList<String>();
		for (clsBillItemDtlBean objBean : listOfBillData)
		{
		    List<String> arrListItem = new ArrayList<String>();

		    String key = objBean.getStrBillNo() + "!" + objBean.getDblSettlementAmt();
		    if (mapMultiSettleBills.containsKey(key))//BillNo
		    {

			arrListItem.add(objBean.getStrBillNo());
			arrListItem.add(objBean.getDteBillDate());
			arrListItem.add(objBean.getStrPosName());
			arrListItem.add(objBean.getStrSettelmentDesc());
			arrListItem.add("0.00");//subTotal/8
			arrListItem.add("0.00");//discAmt/6
			arrListItem.add("0.00");//tax/5
			arrListItem.add(String.valueOf(objBean.getDblSettlementAmt()));//setlementAmt /4               

			totalTaxAmt = totalTaxAmt + Double.parseDouble("0.00");
			totalGrandAmount = totalGrandAmount + Double.parseDouble(String.valueOf(objBean.getDblSettlementAmt()));//settleAmt
			discountTotal = discountTotal + Double.parseDouble("0.00");
			totalSubTotal = totalSubTotal + Double.parseDouble("0.00");

		    }
		    else
		    {
			arrListItem.add(objBean.getStrBillNo());
			arrListItem.add(objBean.getDteBillDate());
			arrListItem.add(objBean.getStrPosName());
			arrListItem.add(objBean.getStrSettelmentMode());
			arrListItem.add(String.valueOf(gDecimalFormat.format(objBean.getDblSubTotal())));
			arrListItem.add(String.valueOf(gDecimalFormat.format(objBean.getDblDiscountAmt())));
			arrListItem.add(String.valueOf(gDecimalFormat.format(objBean.getDblTaxAmt())));
			arrListItem.add(String.valueOf(gDecimalFormat.format(objBean.getDblSettlementAmt())));

			totalTaxAmt = totalTaxAmt + Double.parseDouble(String.valueOf(objBean.getDblTaxAmt()));
			totalGrandAmount = totalGrandAmount + Double.parseDouble(String.valueOf(objBean.getDblSettlementAmt()));
			discountTotal = discountTotal + Double.parseDouble(String.valueOf(objBean.getDblDiscountAmt()));
			totalSubTotal = totalSubTotal + Double.parseDouble(String.valueOf(objBean.getDblSubTotal()));//subTotal
		    }

		    mapExcelItemDtl.put(i, arrListItem);
		    i++;
		}
		arrListTotal.add(String.valueOf(gDecimalFormat.format(roundOff) + "#" + "4"));
		arrListTotal.add(String.valueOf(gDecimalFormat.format(totalSubTotal) + "#" + "5"));
		arrListTotal.add(String.valueOf(gDecimalFormat.format(discountTotal)) + "#" + "6");
		arrListTotal.add(String.valueOf(gDecimalFormat.format(totalTaxAmt)) + "#" + "7");
		arrListTotal.add(String.valueOf(gDecimalFormat.format(totalGrandAmount) + "#" + "8"));

		arrHeaderList.add("Serial No");
		arrHeaderList.add("Bill No");
		arrHeaderList.add("Bill Date");
		arrHeaderList.add("POS Name");
		arrHeaderList.add("Settle Mode");
		arrHeaderList.add("Sub Total");
		arrHeaderList.add("Discount");
		arrHeaderList.add("Tax Amt");
		arrHeaderList.add("Grand Total");

		List<String> arrparameterList = new ArrayList<String>();
		arrparameterList.add("Bill Wise Report");
		arrparameterList.add("POS" + " : " + posName);
		arrparameterList.add("From Date" + " : " + fromDateToDisplay);
		arrparameterList.add("To Date" + " : " + toDateToDisplay);
		arrparameterList.add(" ");
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
		funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "billWiseExcelSheet", dayEnd);
	    }

	    if (reportType.equalsIgnoreCase("Text File-40 Column Report"))
	    {
		int count = 0;

		try
		{
		    funCreateTempFolder();
		    String filePath = System.getProperty("user.dir");
		    File file = new File(filePath + "/Temp/Temp_BillReport.txt");
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
		    funPrintBlankLines("Bill Wise Report", pw);

		    pw.println();
		    pw.println("POS  :" + posName);
		    pw.println("From :" + fromDate + "  To :" + toDate);

		    pw.println("---------------------------------------");
		    pw.println("Bill No       Bill Date         Total");
		    pw.println("---------------------------------------");

		    double grandTotal = 0.00;

		    for (clsBillItemDtlBean objBillDtl : listOfBillData)
		    {
			count++;
			pw.print(objBillDtl.getStrBillNo() + "      " + objBillDtl.getDteBillDate());
			funPrintTextWithAlignment("right", String.valueOf(objBillDtl.getDblSettlementAmt()), 13, pw);
			pw.println();
			grandTotal += objBillDtl.getDblSettlementAmt();
		    }

		    pw.println("---------------------------------------");
		    pw.print("Total                  ");
		    funPrintTextWithAlignment("right", String.valueOf((grandTotal)), 13, pw);
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
		catch (Exception e)
		{
		    e.printStackTrace();
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

	DecimalFormat decFormat = new DecimalFormat("######.00");
	pw.print(new DecimalFormat("######0.00").format(Double.parseDouble(textToPrint)));
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
