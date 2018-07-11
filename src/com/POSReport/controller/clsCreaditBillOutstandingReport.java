package com.POSReport.controller;

import com.POSReport.controller.comparator.clsCreditBillReportComparator;
import com.POSGlobal.controller.clsBillDtl;
import com.POSGlobal.controller.clsGlobalVarClass;
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

public class clsCreaditBillOutstandingReport
{

    DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();

    public void funCreaditBillReport(String reportType, HashMap hm, String dayEnd)
    {
	try
	{
	    String summaryDetailType = hm.get("SummaryDetailType").toString();

	    if (summaryDetailType.equalsIgnoreCase("Detail"))
	    {
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptCreditBillReport.jasper");

		String fromDate = hm.get("fromDate").toString();
		String toDate = hm.get("toDate").toString();
		String posCode = hm.get("posCode").toString();
		String shiftNo = hm.get("shiftNo").toString();
		String posName = hm.get("posName").toString();

		String fromDateToDisplay = hm.get("fromDateToDisplay").toString();
		String toDateToDisplay = hm.get("toDateToDisplay").toString();

		List<clsBillDtl> listOfCreditBillReport = new ArrayList<clsBillDtl>();

		String sbSqlFilters = "", sbSqlFilters1 = "";

		String sbSqlLive = "SELECT a.strPOSCode,a.strCustomerCode,d.strCustomerName,a.strBillNo,DATE_FORMAT(date( a.dteBillDate),'%d-%m-%Y')dteBillDate ,a.strClientCode, SUM(b.dblSettlementAmt)"
			+ " ,d.longMobileNo,a.strRemarks "
			+ " FROM tblbillhd a,tblbillsettlementdtl b,tblsettelmenthd c,tblcustomermaster d"
			+ " WHERE a.strBillNo=b.strBillNo "
			+ " AND b.strSettlementCode=c.strSettelmentCode "
			+ " and date(a.dtebilldate)=date(b.dtebilldate) "
			+ " and a.strClientCode=b.strClientCode "
			+ " AND c.strSettelmentType='Credit' "
			+ " and a.strCustomerCode=d.strCustomerCode "
			+ " and date( a.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' ";

		String sbSqlQFile = "SELECT a.strPOSCode,a.strCustomerCode,d.strCustomerName,a.strBillNo,DATE_FORMAT(date(a.dteBillDate),'%d-%m-%Y')dteBillDate ,a.strClientCode, SUM(b.dblSettlementAmt)"
			+ " ,d.longMobileNo,a.strRemarks "
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

		String sqlModLive = "SELECT a.strPOSCode,b.strBillNo,b.strReceiptNo,DATE_FORMAT(date(b.dteReceiptDate),'%d-%m-%Y'),b.strSettlementName, SUM(b.dblReceiptAmt),b.strChequeNo,b.strBankName,b.strRemarks,c.strCustomerName,a.strCustomerCode,a.strClientCode"
			+ " ,c.longMobileNo,a.strRemarks "
			+ " from tblbillhd a,tblqcreditbillreceipthd b,tblcustomermaster c "
			+ " where a.strBillNo=b.strBillNo "
			+ " and date(a.dteBillDate)=date(b.dteBillDate) "
			+ " and a.strClientCode=b.strClientCode "
			+ " AND a.strCustomerCode=c.strCustomerCode "
			+ " and date( a.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "'";

		String sqlModQFile = "SELECT a.strPOSCode,b.strBillNo,b.strReceiptNo,DATE_FORMAT(date(b.dteReceiptDate),'%d-%m-%Y'),b.strSettlementName, SUM(b.dblReceiptAmt),b.strChequeNo,b.strBankName,b.strRemarks,c.strCustomerName,a.strCustomerCode,a.strClientCode"
			+ " ,c.longMobileNo,a.strRemarks "
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
		    objBean.setLongMobileNo(rsLiveData.getLong(8));
		    objBean.setStrRemarks(rsLiveData.getString(9));
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
		    objBean.setLongMobileNo(rsQfileData.getLong(8));
		    objBean.setStrRemarks(rsQfileData.getString(9));
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
		    objBean.setLongMobileNo(rsLiveModData.getLong(13));
		    objBean.setStrRemarks(rsLiveModData.getString(14));
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
		    objBean.setLongMobileNo(rsQfileModData.getLong(13));
		    objBean.setStrRemarks(rsQfileModData.getString(14));
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

		Comparator<clsBillDtl> remarkComparator = new Comparator<clsBillDtl>()
		{

		    @Override
		    public int compare(clsBillDtl o1, clsBillDtl o2)
		    {
			return o1.getStrRemarks().compareToIgnoreCase(o2.getStrRemarks());
		    }
		};

		Collections.sort(listOfCreditBillReport, new clsCreditBillReportComparator(
			customerComparator,
			billComparator,
			receiptComparator,
			remarkComparator
		)
		);

		//DecimalFormat gDecimalFormat = new DecimalFormat("0.00");
		//call for view report
		if (reportType.equalsIgnoreCase("A4 Size Report"))
		{
		    funViewJasperReportForBeanCollectionDataSource(is, hm, listOfCreditBillReport);
		}
		if (reportType.equalsIgnoreCase("Excel Report"))
		{
		    if (listOfCreditBillReport.size() > 0)
		    {
			Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
			List<String> arrListTotal = new ArrayList<String>();
			List<String> arrHeaderList = new ArrayList<String>();
			double totalReceiptAmount = 0, totBillAmt = 0, totBalanceAmt = 0, billAmount = 0;
			double totalQty = 0;
			int i = 1;
			double balanceAmount = 0, balAmount = 0;
			for (clsBillDtl objBean : listOfCreditBillReport)
			{
			    List<String> arrListItem = new ArrayList<String>();
			    arrListItem.add(objBean.getStrBillNo());
			    arrListItem.add(objBean.getDteBillDate());
			    arrListItem.add(gDecimalFormat.format(objBean.getDblBillAmt()));
			    arrListItem.add(String.valueOf(objBean.getLongMobileNo()));
			    arrListItem.add(objBean.getStrRemarks());
			    arrListItem.add(objBean.getStrReceiptNo());
			    arrListItem.add(objBean.getDteReceiptDate());
			    arrListItem.add(objBean.getStrCustomerName());
			    arrListItem.add(objBean.getStrCustomerCode());
			    arrListItem.add(gDecimalFormat.format(objBean.getDblAmount()));
			    arrListItem.add(objBean.getStrSettlementName());
			    arrListItem.add(objBean.getStrChequeNo());
			    arrListItem.add(objBean.getStrBankName());
			    arrListItem.add(objBean.getStrRemark());

			    totalReceiptAmount = totalReceiptAmount + objBean.getDblAmount();
			    totBillAmt = totBillAmt + objBean.getDblBillAmt();

			    if (i == 1)
			    {
				balanceAmount = totBillAmt - totalReceiptAmount;
			    }
			    else
			    {
				balanceAmount = totBillAmt - totalReceiptAmount;
			    }

			    totBalanceAmt = totBillAmt + balanceAmount;
			    arrListItem.add(String.valueOf(balanceAmount));
			    mapExcelItemDtl.put(i, arrListItem);
			    i++;

			}
			arrListTotal.add(gDecimalFormat.format(totalReceiptAmount) + "#" + "10");
			arrListTotal.add(gDecimalFormat.format(totBillAmt) + "#" + "3");
			arrListTotal.add(gDecimalFormat.format(balanceAmount) + "#" + "15");

			arrHeaderList.add("Serial No");
			arrHeaderList.add("Bill No");
			arrHeaderList.add("Bill Date");
			arrHeaderList.add("Bill Amount");
			arrHeaderList.add("Mobile No");
			arrHeaderList.add("Remarks");
			arrHeaderList.add("Receipt No");
			arrHeaderList.add("Receipt Date");
			arrHeaderList.add("Customer Name");
			arrHeaderList.add("Customer Code");
			arrHeaderList.add("Receipr Amount");
			arrHeaderList.add("Settlement Name");
			arrHeaderList.add("Cheque No");
			arrHeaderList.add("Bank Name");
			arrHeaderList.add("Remark");
			arrHeaderList.add("Balance Amount");

			List<String> arrparameterList = new ArrayList<String>();
			arrparameterList.add("Credit Bill Outstanding Report");
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
			funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "creditBillOutstandingExcelSheet", dayEnd);

		    }
		    else
		    {
			JOptionPane.showMessageDialog(null, "Data not present for selected dates!!!");
		    }
		}
	    }
	    else//summary
	    {
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptCreditBillOutstandingSummaryReport.jasper");

		String fromDate = hm.get("fromDate").toString();
		String toDate = hm.get("toDate").toString();
		String posCode = hm.get("posCode").toString();
		String shiftNo = hm.get("shiftNo").toString();
		String posName = hm.get("posName").toString();

		String fromDateToDisplay = hm.get("fromDateToDisplay").toString();
		String toDateToDisplay = hm.get("toDateToDisplay").toString();

		List<clsBillDtl> listOfCreditBillReport = new ArrayList<clsBillDtl>();

		Map<String, clsBillDtl> mapCustomerData = new HashMap<>();

		String sbSqlFilters = "", sbSqlFilters1 = "";

		String sbSqlLive = "SELECT a.strPOSCode,a.strCustomerCode,d.strCustomerName,a.strBillNo,DATE_FORMAT(date( a.dteBillDate),'%d-%m-%Y')dteBillDate ,a.strClientCode, SUM(b.dblSettlementAmt)"
			+ " ,d.longMobileNo,a.strRemarks "
			+ " FROM tblbillhd a,tblbillsettlementdtl b,tblsettelmenthd c,tblcustomermaster d"
			+ " WHERE a.strBillNo=b.strBillNo "
			+ " AND b.strSettlementCode=c.strSettelmentCode "
			+ " and date(a.dtebilldate)=date(b.dtebilldate) "
			+ " and a.strClientCode=b.strClientCode "
			+ " AND c.strSettelmentType='Credit' "
			+ " and a.strCustomerCode=d.strCustomerCode "
			+ " and date( a.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' ";

		String sbSqlQFile = "SELECT a.strPOSCode,a.strCustomerCode,d.strCustomerName,a.strBillNo,DATE_FORMAT(date(a.dteBillDate),'%d-%m-%Y')dteBillDate ,a.strClientCode, SUM(b.dblSettlementAmt)"
			+ " ,d.longMobileNo,a.strRemarks "
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
		sbSqlFilters1 = " GROUP BY a.strCustomerCode ";

		String sqlModLive = "SELECT a.strPOSCode,b.strBillNo,b.strReceiptNo,DATE_FORMAT(date(b.dteReceiptDate),'%d-%m-%Y'),b.strSettlementName, SUM(b.dblReceiptAmt),b.strChequeNo,b.strBankName,b.strRemarks,c.strCustomerName,a.strCustomerCode,a.strClientCode"
			+ " ,c.longMobileNo,a.strRemarks "
			+ " from tblbillhd a,tblqcreditbillreceipthd b,tblcustomermaster c "
			+ " where a.strBillNo=b.strBillNo "
			+ " and date(a.dteBillDate)=date(b.dteBillDate) "
			+ " and a.strClientCode=b.strClientCode "
			+ " AND a.strCustomerCode=c.strCustomerCode "
			+ " and date( a.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "'";

		String sqlModQFile = "SELECT a.strPOSCode,b.strBillNo,b.strReceiptNo,DATE_FORMAT(date(b.dteReceiptDate),'%d-%m-%Y'),b.strSettlementName, SUM(b.dblReceiptAmt),b.strChequeNo,b.strBankName,b.strRemarks,c.strCustomerName,a.strCustomerCode,a.strClientCode"
			+ " ,c.longMobileNo,a.strRemarks "
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
		sbSqlFilters = " group by a.strCustomerCode ";

		sbSqlLive += " " + sbSqlFilters1;
		sbSqlQFile += " " + sbSqlFilters1;
		sqlModLive += " " + sbSqlFilters;
		sqlModQFile += " " + sbSqlFilters;
		double balanceAmt = 0.00, tottalCreditAmt = 0.00, tottalReceivedAmt = 0.00;
		ResultSet rsLiveData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive);
		while (rsLiveData.next())
		{
		    String custCode = rsLiveData.getString(2);
		    String custName = rsLiveData.getString(3);

		    tottalCreditAmt = +rsLiveData.getDouble(7);
		    balanceAmt = +tottalCreditAmt;

		    if (mapCustomerData.containsKey(custCode))
		    {
			clsBillDtl objBean = mapCustomerData.get(custCode);

			objBean.setDblBillAmt(objBean.getDblBillAmt() + rsLiveData.getDouble(7));
		    }
		    else
		    {
			clsBillDtl objBean = new clsBillDtl();
			objBean.setStrCustomerName(rsLiveData.getString(3));   //Customer Name
			objBean.setStrCustomerCode(rsLiveData.getString(2));

			objBean.setDblBillAmt(rsLiveData.getDouble(7));
			objBean.setLongMobileNo(rsLiveData.getLong(8));

			mapCustomerData.put(custCode, objBean);
		    }

		}
		rsLiveData.close();

		ResultSet rsQfileData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFile);
		while (rsQfileData.next())
		{
		    String custCode = rsQfileData.getString(2);
		    String custName = rsQfileData.getString(3);

		    tottalCreditAmt = +rsQfileData.getDouble(7);

		    if (mapCustomerData.containsKey(custCode))
		    {
			clsBillDtl objBean = mapCustomerData.get(custCode);

			objBean.setDblBillAmt(objBean.getDblBillAmt() + rsQfileData.getDouble(7));
		    }
		    else
		    {
			clsBillDtl objBean = new clsBillDtl();
			objBean.setStrCustomerName(rsQfileData.getString(3));   //Customer Name
			objBean.setStrCustomerCode(rsQfileData.getString(2));

			objBean.setDblBillAmt(rsQfileData.getDouble(7));
			objBean.setLongMobileNo(rsQfileData.getLong(8));

			mapCustomerData.put(custCode, objBean);
		    }

		}
		rsQfileData.close();

		ResultSet rsLiveModData = clsGlobalVarClass.dbMysql.executeResultSet(sqlModLive);
		while (rsLiveModData.next())
		{

		    String custCode = rsLiveModData.getString(11);
		    String custName = rsLiveModData.getString(10);

		    if (mapCustomerData.containsKey(custCode))
		    {
			clsBillDtl objBean = mapCustomerData.get(custCode);

			objBean.setDblAmount(objBean.getDblAmount() + rsLiveModData.getDouble(6));   //Receipt Amount
		    }
		    else
		    {
			clsBillDtl objBean = new clsBillDtl();

			objBean.setStrCustomerName(rsLiveModData.getString(10));
			objBean.setStrCustomerCode(rsLiveModData.getString(11));
			objBean.setDblAmount(rsLiveModData.getDouble(6));   //Receipt Amount

			tottalReceivedAmt = +rsLiveModData.getDouble(6);

			mapCustomerData.put(custCode, objBean);

		    }
		}
		rsLiveModData.close();

		ResultSet rsQfileModData = clsGlobalVarClass.dbMysql.executeResultSet(sqlModQFile);
		while (rsQfileModData.next())
		{
		    String custCode = rsQfileModData.getString(11);
		    String custName = rsQfileModData.getString(10);

		    if (mapCustomerData.containsKey(custCode))
		    {
			clsBillDtl objBean = mapCustomerData.get(custCode);

			objBean.setDblAmount(objBean.getDblAmount() + rsQfileModData.getDouble(6));   //Receipt Amount
		    }
		    else
		    {
			clsBillDtl objBean = new clsBillDtl();

			objBean.setStrCustomerName(rsQfileModData.getString(10));
			objBean.setStrCustomerCode(rsQfileModData.getString(11));
			objBean.setDblAmount(rsQfileModData.getDouble(6));   //Receipt Amount

			tottalReceivedAmt = +rsQfileModData.getDouble(6);

			mapCustomerData.put(custCode, objBean);

		    }
		}
		rsQfileModData.close();

		Comparator<clsBillDtl> customerComparator = new Comparator<clsBillDtl>()
		{

		    @Override
		    public int compare(clsBillDtl o1, clsBillDtl o2)
		    {
			return o1.getStrCustomerName().compareToIgnoreCase(o2.getStrCustomerName());
		    }
		};

		listOfCreditBillReport.clear();

		listOfCreditBillReport.addAll(mapCustomerData.values());

		Collections.sort(listOfCreditBillReport, new clsCreditBillReportComparator(customerComparator));

		//DecimalFormat gDecimalFormat = new DecimalFormat("0.00");
		//call for view report
		if (reportType.equalsIgnoreCase("A4 Size Report"))
		{
		    funViewJasperReportForBeanCollectionDataSource(is, hm, listOfCreditBillReport);
		}
		if (reportType.equalsIgnoreCase("Excel Report"))
		{
		    if (listOfCreditBillReport.size() > 0)
		    {
			Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
			List<String> arrListTotal = new ArrayList<String>();
			List<String> arrHeaderList = new ArrayList<String>();
			double totalReceiptAmount = 0, totBillAmt = 0, totBalanceAmt = 0, billAmount = 0;
			double totalQty = 0;
			int i = 1;
			double balanceAmount = 0, balAmount = 0;
			for (clsBillDtl objBean : listOfCreditBillReport)
			{
			    List<String> arrListItem = new ArrayList<String>();
			    arrListItem.add(objBean.getStrCustomerName());
			    arrListItem.add(String.valueOf(objBean.getLongMobileNo()));
			    arrListItem.add(gDecimalFormat.format(objBean.getDblBillAmt()));
			    arrListItem.add(gDecimalFormat.format(objBean.getDblAmount()));
			    arrListItem.add(gDecimalFormat.format(objBean.getDblBillAmt() - objBean.getDblAmount()));

			    totBillAmt += objBean.getDblBillAmt();
			    totalReceiptAmount += objBean.getDblAmount();

			    mapExcelItemDtl.put(i, arrListItem);
			    i++;

			}
			totBalanceAmt = totBillAmt - totalReceiptAmount;

			arrListTotal.add(gDecimalFormat.format(totBillAmt) + "#" + "3");
			arrListTotal.add(gDecimalFormat.format(totalReceiptAmount) + "#" + "4");
			arrListTotal.add(gDecimalFormat.format(totBalanceAmt) + "#" + "5");

			arrHeaderList.add("Serial No");
			arrHeaderList.add("Customer Name");
			arrHeaderList.add("Mobile No");
			arrHeaderList.add("Credit Amount");
			arrHeaderList.add("Received Amount");
			arrHeaderList.add("Balance Amount");

			List<String> arrparameterList = new ArrayList<String>();
			arrparameterList.add("Customer Ledger Report");
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
			funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "Customer Ledger Report", dayEnd);

		    }
		    else
		    {
			JOptionPane.showMessageDialog(null, "Data not present for selected dates!!!");
		    }
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
