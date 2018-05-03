/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.POSReport.controller;

import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsManagerReportBean;
import com.POSGlobal.controller.clsSalesFlashColumns;
import com.POSGlobal.controller.clsTaxCalculationDtls;
import com.POSGlobal.controller.clsUtility;
import com.POSGlobal.controller.clsUtility2;
import com.POSReport.controller.comparator.clsSalesFlashComparator;
import com.POSReport.view.frmBillWiseSettlementSalesSummaryFlash;
import com.toedter.calendar.JDateChooser;
import java.awt.Desktop;
import java.io.File;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Ajim
 */
public class clsBillRegisterReport
{

    private final DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();
    private clsUtility objUtility;
    private clsUtility2 objUtility2;
    private StringBuilder sb = new StringBuilder();
    private PrintWriter pw;
    private File file;
    private double totalDiscAmt;
    private double totalSettleAmt;
    private double totalRoundOffAmt;
    private double totalTaxAmt;
    private double totalTipAmt;
    private int totalBills;

    public clsBillRegisterReport()
    {
	try
	{
	    objUtility = new clsUtility();
	    objUtility2 = new clsUtility2();

	    objUtility2.funCreateTempFolder();

	    String filePath = System.getProperty("user.dir");
	    file = new File(filePath + File.separator + "Temp" + File.separator + "Bill Register.txt");
	    pw = new PrintWriter(file);
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}

    }

    public void funFillTableForBillRegister(JDateChooser dteFromDate, JDateChooser dteToDate, String fromDate, String toDate, String posCode, JTable tblBillRegister, frmBillWiseSettlementSalesSummaryFlash objBillWiseSettlementSalesSummaryFlash)
    {

	try
	{

	    DecimalFormat decFormat = new DecimalFormat("0");
	    StringBuilder sbSqlBillWise = new StringBuilder();
	    StringBuilder sbSqlBillWiseQFile = new StringBuilder();

	    /**
	     * Printing logic starts
	     */
	    Date dateFromDate = dteFromDate.getDate();
	    Date dateToDate = dteToDate.getDate();
	    Date currentDate = new Date();

	    SimpleDateFormat dateFormat = new SimpleDateFormat("E dd-MMM-yyyy");

	    SimpleDateFormat dateFormat24Hrs = new SimpleDateFormat("E dd-MMM-yyyy HH:mm:ss");

	    String fromDayAndDate = dateFormat.format(dateFromDate);
	    String toDayAndDate = dateFormat.format(dateToDate);
	    String todaysDate = dateFormat24Hrs.format(currentDate);

	    boolean isDayEndHappend = objUtility2.isDayEndHappened(toDate);
	    if (!isDayEndHappend)
	    {
		pw.println();
		pw.print(objUtility.funPrintTextWithAlignment("DAY END NOT DONE.", 100, "center"));
		pw.println();
	    }

	    pw.println(clsGlobalVarClass.gClientName);
	    pw.println("Report : Bill Register");
	    pw.println("Reporting For :" + "  " + fromDayAndDate + " " + "To" + " " + toDayAndDate);
	    pw.println("Printed On :" + "  " + todaysDate);

	    int noOfColumns = 0;
	    pw.println();
	    pw.print(objUtility.funPrintTextWithAlignment("Tbl No", 10, "left"));
	    pw.print(objUtility.funPrintTextWithAlignment("Bill No", 10, "left"));
	    pw.print(objUtility.funPrintTextWithAlignment("Amount", 10, "Right"));
	    pw.print(objUtility.funPrintTextWithAlignment("Disc.", 10, "Right"));
	    noOfColumns = 40;

	    Map<String, String> mapAllTaxes = objBillWiseSettlementSalesSummaryFlash.getMapAllTaxes();
	    for (Map.Entry<String, String> taxEntry : mapAllTaxes.entrySet())
	    {

		String taxCode = taxEntry.getKey();
		String taxName = taxEntry.getValue();

		pw.print(objUtility.funPrintTextWithAlignment(taxName.toUpperCase(), 20, "Right"));

		noOfColumns = noOfColumns + 20;

	    }
	    pw.print(objUtility.funPrintTextWithAlignment("Tip", 10, "Right"));
	    pw.print(objUtility.funPrintTextWithAlignment("Adv.", 10, "Right"));
	    pw.print(objUtility.funPrintTextWithAlignment("Total", 10, "Right"));
	    pw.print(objUtility.funPrintTextWithAlignment("  Settle", 20, "left"));
	    noOfColumns = noOfColumns + 50;

	    String DASHLINE = "";
	    for (int i = 0; i < noOfColumns; i++)
	    {
		DASHLINE = DASHLINE + "-";
	    }
	    pw.println();
	    pw.print(DASHLINE);

	    pw.println();
	    pw.println("BILLS");

	    pw.println();
	    pw.println("FOOD BILLS");

	    boolean liquorBillsPrint = true;

	    ArrayList<clsSalesFlashColumns> arrTempListBillWiseSales = objBillWiseSettlementSalesSummaryFlash.getArrTempListBillWiseSales();
	    Map<String, Double> mapOfTotal = new LinkedHashMap<>();
	    mapOfTotal.put("Amount", 0.00);
	    mapOfTotal.put("Discount", 0.00);
	    for (Map.Entry<String, String> taxEntry : mapAllTaxes.entrySet())
	    {

		String taxCode = taxEntry.getKey();
		String taxName = taxEntry.getValue();

		mapOfTotal.put(taxName.toUpperCase(), 0.00);
	    }
	    mapOfTotal.put("Tip", 0.00);
	    mapOfTotal.put("Advance", 0.00);
	    mapOfTotal.put("Total", 0.00);
	    mapOfTotal.put("RoundOff", 0.00);

	    for (clsSalesFlashColumns objSalesFlashColumns : arrTempListBillWiseSales)
	    {

		if (objSalesFlashColumns.getStrField24() != null && objSalesFlashColumns.getStrField24().equalsIgnoreCase("MultiSettle"))
		{

		    pw.println();
		    pw.print(objUtility.funPrintTextWithAlignment("Part Settlement", 20, "left"));
		    //pw.print(objUtility.funPrintTextWithAlignment("0.00", 10, "Right"));
		    pw.print(objUtility.funPrintTextWithAlignment("0.00", 10, "Right"));
		    pw.print(objUtility.funPrintTextWithAlignment("0.00", 10, "Right"));
		    Map<String, clsTaxCalculationDtls> mapOfTaxesLocal = objSalesFlashColumns.getMapOfTaxes();
		    for (clsTaxCalculationDtls objTaxCalculationDtls : mapOfTaxesLocal.values())
		    {
			pw.print(objUtility.funPrintTextWithAlignment("0.00", 20, "Right"));
		    }
		    pw.print(objUtility.funPrintTextWithAlignment("0.00", 10, "Right"));
		    pw.print(objUtility.funPrintTextWithAlignment("0.00", 10, "Right"));
		    pw.print(objUtility.funPrintTextWithAlignment(gDecimalFormat.format(Double.parseDouble(objSalesFlashColumns.getStrField13())), 10, "Right"));
		    pw.print(objUtility.funPrintTextWithAlignment("  " + objSalesFlashColumns.getStrField7(), 20, "left"));

		    mapOfTotal.put("Total", mapOfTotal.get("Total") + Double.parseDouble(objSalesFlashColumns.getStrField13()));

		    continue;
		}

		if (objSalesFlashColumns.getStrField23().equalsIgnoreCase("L") && liquorBillsPrint)
		{

		    pw.println();
		    pw.print("LIQUOR BILLS");

		    liquorBillsPrint = false;
		}

		mapOfTotal.put("Amount", mapOfTotal.get("Amount") + Double.parseDouble(objSalesFlashColumns.getStrField9()));
		mapOfTotal.put("Discount", mapOfTotal.get("Discount") + Double.parseDouble(objSalesFlashColumns.getStrField11()));
		

		pw.println();
		pw.print(objUtility.funPrintTextWithAlignment(objSalesFlashColumns.getStrField4(), 10, "left"));
		pw.print(objUtility.funPrintTextWithAlignment(objSalesFlashColumns.getStrField1(), 10, "left"));
		pw.print(objUtility.funPrintTextWithAlignment(gDecimalFormat.format(Double.parseDouble(objSalesFlashColumns.getStrField9())), 10, "Right"));
		pw.print(objUtility.funPrintTextWithAlignment(gDecimalFormat.format(Double.parseDouble(objSalesFlashColumns.getStrField11())), 10, "Right"));
		Map<String, clsTaxCalculationDtls> mapOfTaxesLocal = objSalesFlashColumns.getMapOfTaxes();
		for (clsTaxCalculationDtls objTaxCalculationDtls : mapOfTaxesLocal.values())
		{
		    pw.print(objUtility.funPrintTextWithAlignment(gDecimalFormat.format(objTaxCalculationDtls.getTaxAmount()), 20, "Right"));

		    if (mapOfTotal.containsKey(objTaxCalculationDtls.getTaxName().toUpperCase()))
		    {
			mapOfTotal.put(objTaxCalculationDtls.getTaxName().toUpperCase(), mapOfTotal.get(objTaxCalculationDtls.getTaxName().toUpperCase()) + objTaxCalculationDtls.getTaxAmount());
		    }
		}
		pw.print(objUtility.funPrintTextWithAlignment(gDecimalFormat.format(Double.parseDouble(objSalesFlashColumns.getStrField15())), 10, "Right"));
		pw.print(objUtility.funPrintTextWithAlignment(gDecimalFormat.format(Double.parseDouble(objSalesFlashColumns.getStrField21())), 10, "Right"));
		pw.print(objUtility.funPrintTextWithAlignment(gDecimalFormat.format(Double.parseDouble(objSalesFlashColumns.getStrField13())), 10, "Right"));
		pw.print(objUtility.funPrintTextWithAlignment("  " + objSalesFlashColumns.getStrField7(), 20, "left"));

		mapOfTotal.put("Tip", mapOfTotal.get("Tip") + Double.parseDouble(objSalesFlashColumns.getStrField15()));
		mapOfTotal.put("Advance", mapOfTotal.get("Advance") + Double.parseDouble(objSalesFlashColumns.getStrField21()));
		mapOfTotal.put("Total", mapOfTotal.get("Total") + Double.parseDouble(objSalesFlashColumns.getStrField13()));
		mapOfTotal.put("RoundOff", mapOfTotal.get("RoundOff") + Double.parseDouble(objSalesFlashColumns.getStrField19()));

		if (Double.parseDouble(objSalesFlashColumns.getStrField11()) > 0)
		{
		    pw.println();
		    pw.print(objUtility.funPrintTextWithAlignment("Reason For Discount : " + objSalesFlashColumns.getStrField16(), 10, "left"));
		    pw.print(objUtility.funPrintTextWithAlignment("", 10, "left"));
		    pw.print(objUtility.funPrintTextWithAlignment("", 10, "Right"));
		    pw.print(objUtility.funPrintTextWithAlignment("", 10, "Right"));
		    mapOfTaxesLocal = objSalesFlashColumns.getMapOfTaxes();
		    for (clsTaxCalculationDtls objTaxCalculationDtls : mapOfTaxesLocal.values())
		    {
			pw.print(objUtility.funPrintTextWithAlignment("", 20, "Right"));
		    }
		    pw.print(objUtility.funPrintTextWithAlignment("", 10, "Right"));
		    pw.print(objUtility.funPrintTextWithAlignment("", 10, "Right"));
		    pw.print(objUtility.funPrintTextWithAlignment("", 10, "Right"));
		    pw.print(objUtility.funPrintTextWithAlignment("", 20, "left"));
		}

		if (objSalesFlashColumns.getStrSettlementType().equalsIgnoreCase("CREDIT"))
		{
		    pw.println();
		    pw.print(objUtility.funPrintTextWithAlignment("Customer : " + objSalesFlashColumns.getStrCustomerName(), 20, "left"));
		    //pw.print(objUtility.funPrintTextWithAlignment("", 10, "left"));
		    pw.print(objUtility.funPrintTextWithAlignment("", 10, "Right"));
		    pw.print(objUtility.funPrintTextWithAlignment("", 10, "Right"));
		    mapOfTaxesLocal = objSalesFlashColumns.getMapOfTaxes();
		    for (clsTaxCalculationDtls objTaxCalculationDtls : mapOfTaxesLocal.values())
		    {
			pw.print(objUtility.funPrintTextWithAlignment("", 20, "Right"));
		    }
		    pw.print(objUtility.funPrintTextWithAlignment("", 10, "Right"));
		    pw.print(objUtility.funPrintTextWithAlignment("", 10, "Right"));
		    pw.print(objUtility.funPrintTextWithAlignment("", 10, "Right"));
		    pw.print(objUtility.funPrintTextWithAlignment("", 20, "left"));
		}
	    }

	    pw.println();
	    pw.println();
	    pw.print(objUtility.funPrintTextWithAlignment("Total", 10, "left"));
	    pw.print(objUtility.funPrintTextWithAlignment("", 10, "left"));
	    pw.print(objUtility.funPrintTextWithAlignment(gDecimalFormat.format(mapOfTotal.get("Amount")), 10, "Right"));
	    pw.print(objUtility.funPrintTextWithAlignment(gDecimalFormat.format(mapOfTotal.get("Discount")), 10, "Right"));
	    double totalTax = 0;
	    for (Map.Entry<String, String> taxEntry : mapAllTaxes.entrySet())
	    {

		String taxCode = taxEntry.getKey();
		String taxName = taxEntry.getValue();

		pw.print(objUtility.funPrintTextWithAlignment(gDecimalFormat.format(mapOfTotal.get(taxName.toUpperCase())), 20, "Right"));

		totalTax += mapOfTotal.get(taxName.toUpperCase());
	    }
	    pw.print(objUtility.funPrintTextWithAlignment(gDecimalFormat.format(mapOfTotal.get("Tip")), 10, "Right"));
	    pw.print(objUtility.funPrintTextWithAlignment(gDecimalFormat.format(mapOfTotal.get("Advance")), 10, "Right"));

	    double grandTotal = mapOfTotal.get("Amount") - mapOfTotal.get("Discount") + totalTax+mapOfTotal.get("RoundOff");
	    //pw.print(objUtility.funPrintTextWithAlignment(gDecimalFormat.format(mapOfTotal.get("Total")), 10, "Right"));
	    pw.print(objUtility.funPrintTextWithAlignment(gDecimalFormat.format(grandTotal), 10, "Right"));
	    pw.print(objUtility.funPrintTextWithAlignment("  ", 20, "left"));

	    /**
	     * closing bill register dtl
	     */
	    pw.println();
	    pw.print(DASHLINE);

	    /**
	     * opening summary
	     */
	    funBillRegisterSummaryGenerator(fromDate, toDate, posCode, pw);

	    /**
	     * closing report
	     */
	    pw.println();
	    pw.println();

	    pw.flush();
	    pw.close();

	    Desktop dt = Desktop.getDesktop();
	    dt.open(file);

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private int funBillRegisterSummaryGenerator(String fromDate, String toDate, String posCode, PrintWriter p) throws Exception
    {
	StringBuilder sbSqlLiveFile = new StringBuilder();
	StringBuilder sbSqlQFile = new StringBuilder();
//	

	SimpleDateFormat dateFormat = new SimpleDateFormat("E dd-MMM-yyyy");

	SimpleDateFormat dateFormat24Hrs = new SimpleDateFormat("E dd-MMM-yyyy HH:mm:ss");

	String sqlTip = "", sqlNoOfBill = "", sqlDiscount = "";

	Map<String, Map<String, clsManagerReportBean>> mapBillWiseData = new TreeMap<String, Map<String, clsManagerReportBean>>();
	Map<String, Map<String, String>> mapBillWiseSettlementNames = new TreeMap<String, Map<String, String>>();
	Map<String, Map<String, String>> mapBillWiseTaxNames = new TreeMap<String, Map<String, String>>();
	Map<String, Map<String, String>> mapBillWiseGroupNames = new TreeMap<String, Map<String, String>>();

	int maxSettlementNameLength = 24;
	int maxGroupNameLength = 12;
	int maxTaxNameLength = 0;
	//int maxLineCount = 0;

	Map<String, Integer> mapGroupNameWithLength = new TreeMap<>();

	final String ORDERFORSUMMARY = "VSC";
	Comparator<String> taxNameSorting = new Comparator<String>()
	{
	    @Override
	    public int compare(String o1, String o2)
	    {
		return ORDERFORSUMMARY.indexOf(o1.charAt(0)) - ORDERFORSUMMARY.indexOf(o2.charAt(0));
	    }
	};

	Map<String, Integer> mapTaxNameWithLength = new TreeMap<>(taxNameSorting);

	//Map<String, clsManagerReportBean> mapDateWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();
	//Map<String, Double> mapDateWiseDiscTipRoundOffData = new TreeMap<String, Double>();
	//Map<Integer, String> mapTaxHeaders = new TreeMap<Integer, String>();
	//Map<String, Double> mapDateWiseTaxBreakupData = new TreeMap<String, Double>();
	//Map<String, clsGroupSubGroupItemBean> mapDateWiseGroupWiseData = new HashMap<String, clsGroupSubGroupItemBean>();
	int cntTax = 1;
	totalTaxAmt = 0.00;
	totalSettleAmt = 0.00;
	totalDiscAmt = 0.00;
	totalTipAmt = 0.00;
	totalRoundOffAmt = 0.00;
	totalBills = 0;

	sbSqlLiveFile.setLength(0);
	sbSqlLiveFile.append(" select a.strBillNo,c.strSettelmentCode,c.strSettelmentDesc,b.dblSettlementAmt,DATE_FORMAT(date(a.dteBillDate),'%d-%m-%Y')dteBillDate "
		+ " from tblbillhd a,tblbillsettlementdtl b,tblsettelmenthd c "
		+ " where a.strBillNo=b.strBillNo "
		+ " and date(a.dteBillDate)=date(b.dteBillDate) "
		+ " and b.strSettlementCode=c.strSettelmentCode "
		+ " and a.strClientCode=b.strClientCode "//and a.strSettelmentMode!='MultiSettle'
		+ " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		+ " and c.strSettelmentType!='Complementary' "
		+ " ");
	if (!posCode.equalsIgnoreCase("All"))
	{
	    sbSqlLiveFile.append(" and a.strPOSCode='" + posCode + "' ");
	}
	sbSqlLiveFile.append(" order BY a.strBillNo,c.strSettelmentDesc ");
	System.out.println(sbSqlLiveFile);

	ResultSet rsSettleManager = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLiveFile.toString());
	while (rsSettleManager.next())
	{

	    String strBillNo = rsSettleManager.getString(1);
	    String settlementCode = rsSettleManager.getString(2);
	    String settlementDesc = rsSettleManager.getString(3);
	    double settleAmt = rsSettleManager.getDouble(4);
	    String billDate = rsSettleManager.getString(5);

	    if (settlementDesc.length() > maxSettlementNameLength)
	    {
		maxSettlementNameLength = settlementDesc.length();
	    }

	    totalSettleAmt = totalSettleAmt + settleAmt;

	    if (mapBillWiseSettlementNames.containsKey(strBillNo))
	    {
		Map<String, String> mapSettlementNames = mapBillWiseSettlementNames.get(strBillNo);

		mapSettlementNames.put(settlementCode, settlementDesc);
	    }
	    else
	    {
		Map<String, String> mapSettlementNames = new TreeMap<>();

		mapSettlementNames.put(settlementCode, settlementDesc);

		mapBillWiseSettlementNames.put(strBillNo, mapSettlementNames);
	    }

	    if (mapBillWiseData.containsKey(strBillNo))
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = mapBillWiseData.get(strBillNo);

		//put settlement dtl
		if (mapBillWiseSettlementWiseData.containsKey(settlementCode))
		{
		    clsManagerReportBean objManagerReportBean = mapBillWiseSettlementWiseData.get(settlementCode);
		    objManagerReportBean.setDblSettlementAmt(objManagerReportBean.getDblSettlementAmt() + settleAmt);

		    mapBillWiseSettlementWiseData.put(settlementCode, objManagerReportBean);
		}
		else
		{
		    clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		    objManagerReportBean.setStrSettlementCode(settlementCode);
		    objManagerReportBean.setStrSettlementDesc(settlementDesc);
		    objManagerReportBean.setDblSettlementAmt(settleAmt);

		    mapBillWiseSettlementWiseData.put(settlementCode, objManagerReportBean);
		}
		//put total settlement dtl
		if (mapBillWiseSettlementWiseData.containsKey("TotalSettlementAmt"))
		{
		    clsManagerReportBean objManagerReportBean = mapBillWiseSettlementWiseData.get("TotalSettlementAmt");
		    objManagerReportBean.setDblSettlementAmt(objManagerReportBean.getDblSettlementAmt() + settleAmt);

		    mapBillWiseSettlementWiseData.put("TotalSettlementAmt", objManagerReportBean);
		}
		else
		{
		    clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		    objManagerReportBean.setStrSettlementCode("TotalSettlementAmt");
		    objManagerReportBean.setStrSettlementDesc("TotalSettlementAmt");
		    objManagerReportBean.setDblSettlementAmt(settleAmt);

		    mapBillWiseSettlementWiseData.put("TotalSettlementAmt", objManagerReportBean);
		}

		mapBillWiseData.put(strBillNo, mapBillWiseSettlementWiseData);
	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();

		//put settlement dtl
		clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		objManagerReportBean.setStrSettlementCode(settlementCode);
		objManagerReportBean.setStrSettlementDesc(settlementDesc);
		objManagerReportBean.setDblSettlementAmt(settleAmt);

		mapBillWiseSettlementWiseData.put(settlementCode, objManagerReportBean);

		//put total settlement dtl
		objManagerReportBean = new clsManagerReportBean();
		objManagerReportBean.setStrSettlementCode("TotalSettlementAmt");
		objManagerReportBean.setStrSettlementDesc("TotalSettlementAmt");
		objManagerReportBean.setDblSettlementAmt(settleAmt);

		mapBillWiseSettlementWiseData.put("TotalSettlementAmt", objManagerReportBean);

		mapBillWiseData.put(strBillNo, mapBillWiseSettlementWiseData);
	    }
	}
	rsSettleManager.close();

	sbSqlQFile.setLength(0);
	sbSqlQFile.append(" select a.strBillNo,c.strSettelmentCode,c.strSettelmentDesc,b.dblSettlementAmt,DATE_FORMAT(date(a.dteBillDate),'%d-%m-%Y')dteBillDate "
		+ " from tblqbillhd a,tblqbillsettlementdtl b,tblsettelmenthd c "
		+ " where a.strBillNo=b.strBillNo "
		+ " and date(a.dteBillDate)=date(b.dteBillDate) "
		+ " and b.strSettlementCode=c.strSettelmentCode "
		+ " and a.strClientCode=b.strClientCode "//and a.strSettelmentMode!='MultiSettle' 
		+ " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		+ " and c.strSettelmentType!='Complementary' ");
	if (!posCode.equalsIgnoreCase("All"))
	{
	    sbSqlQFile.append(" and a.strPOSCode='" + posCode + "' ");
	}
	sbSqlQFile.append(" order BY a.strBillNo,c.strSettelmentDesc ");
	rsSettleManager = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFile.toString());

	while (rsSettleManager.next())
	{

	    String strBillNo = rsSettleManager.getString(1);
	    String settlementCode = rsSettleManager.getString(2);
	    String settlementDesc = rsSettleManager.getString(3);
	    double settleAmt = rsSettleManager.getDouble(4);
	    String billDate = rsSettleManager.getString(5);

	    if (settlementDesc.length() > maxSettlementNameLength)
	    {
		maxSettlementNameLength = settlementDesc.length();
	    }

	    totalSettleAmt = totalSettleAmt + settleAmt;

	    if (mapBillWiseSettlementNames.containsKey(strBillNo))
	    {
		Map<String, String> mapSettlementNames = mapBillWiseSettlementNames.get(strBillNo);

		mapSettlementNames.put(settlementCode, settlementDesc);
	    }
	    else
	    {
		Map<String, String> mapSettlementNames = new TreeMap<>();

		mapSettlementNames.put(settlementCode, settlementDesc);

		mapBillWiseSettlementNames.put(strBillNo, mapSettlementNames);
	    }

	    if (mapBillWiseData.containsKey(strBillNo))
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = mapBillWiseData.get(strBillNo);

		//put settlement dtl
		if (mapBillWiseSettlementWiseData.containsKey(settlementCode))
		{
		    clsManagerReportBean objManagerReportBean = mapBillWiseSettlementWiseData.get(settlementCode);
		    objManagerReportBean.setDblSettlementAmt(objManagerReportBean.getDblSettlementAmt() + settleAmt);

		    mapBillWiseSettlementWiseData.put(settlementCode, objManagerReportBean);
		}
		else
		{
		    clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		    objManagerReportBean.setStrSettlementCode(settlementCode);
		    objManagerReportBean.setStrSettlementDesc(settlementDesc);
		    objManagerReportBean.setDblSettlementAmt(settleAmt);

		    mapBillWiseSettlementWiseData.put(settlementCode, objManagerReportBean);
		}
		//put total settlement dtl
		if (mapBillWiseSettlementWiseData.containsKey("TotalSettlementAmt"))
		{
		    clsManagerReportBean objManagerReportBean = mapBillWiseSettlementWiseData.get("TotalSettlementAmt");
		    objManagerReportBean.setDblSettlementAmt(objManagerReportBean.getDblSettlementAmt() + settleAmt);

		    mapBillWiseSettlementWiseData.put("TotalSettlementAmt", objManagerReportBean);
		}
		else
		{
		    clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		    objManagerReportBean.setStrSettlementCode("TotalSettlementAmt");
		    objManagerReportBean.setStrSettlementDesc("TotalSettlementAmt");
		    objManagerReportBean.setDblSettlementAmt(settleAmt);

		    mapBillWiseSettlementWiseData.put("TotalSettlementAmt", objManagerReportBean);
		}

		mapBillWiseData.put(strBillNo, mapBillWiseSettlementWiseData);
	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();
		//put settlement dtl

		clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		objManagerReportBean.setStrSettlementCode(settlementCode);
		objManagerReportBean.setStrSettlementDesc(settlementDesc);
		objManagerReportBean.setDblSettlementAmt(settleAmt);

		mapBillWiseSettlementWiseData.put(settlementCode, objManagerReportBean);

		//put total settlement dtl
		objManagerReportBean = new clsManagerReportBean();
		objManagerReportBean.setStrSettlementCode("TotalSettlementAmt");
		objManagerReportBean.setStrSettlementDesc("TotalSettlementAmt");
		objManagerReportBean.setDblSettlementAmt(settleAmt);

		mapBillWiseSettlementWiseData.put("TotalSettlementAmt", objManagerReportBean);

		mapBillWiseData.put(strBillNo, mapBillWiseSettlementWiseData);
	    }
	}
	rsSettleManager.close();

	/**
	 * live taxes
	 */
	String sqlTax = "SELECT a.strBillNo,ifnull(c.strTaxCode,'VAT'),ifnull(c.strTaxDesc,'VAT'),ifnull(SUM(b.dblTaxAmount),0) "
		+ " from tblbillhd a "
		+ " left outer join tblbilltaxdtl b on a.strBillNo=b.strBillNo AND DATE(a.dteBillDate)= DATE(b.dteBillDate) AND a.strClientCode=b.strClientCode "
		+ " left outer join tbltaxhd c on b.strTaxCode=c.strTaxCode "
		+ " where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ";
	if (!posCode.equalsIgnoreCase("All"))
	{
	    sqlTax += " and a.strPOSCode='" + posCode + "' ";
	}
	sqlTax += " group by a.strBillNo,c.strTaxCode";
	ResultSet rsTaxDtl1 = clsGlobalVarClass.dbMysql.executeResultSet(sqlTax);
	while (rsTaxDtl1.next())
	{
	    String strBillNo = rsTaxDtl1.getString(1);
	    String taxCode = rsTaxDtl1.getString(2);
	    String taxDesc = rsTaxDtl1.getString(3);
	    double taxAmt = rsTaxDtl1.getDouble(4);

	    mapTaxNameWithLength.put(taxDesc, taxDesc.length());
	    if (taxDesc.length() > maxTaxNameLength)
	    {
		maxTaxNameLength = taxDesc.length();
	    }

	    totalTaxAmt = totalTaxAmt + taxAmt;

	    if (mapBillWiseTaxNames.containsKey(strBillNo))
	    {
		Map<String, String> mapTaxNames = mapBillWiseTaxNames.get(strBillNo);

		mapTaxNames.put(taxCode, taxDesc);
	    }
	    else
	    {
		Map<String, String> mapTaxNames = new TreeMap<>();

		mapTaxNames.put(taxCode, taxDesc);

		mapBillWiseTaxNames.put(strBillNo, mapTaxNames);
	    }

	    if (mapBillWiseData.containsKey(strBillNo))
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = mapBillWiseData.get(strBillNo);

		//put tax dtl
		if (mapBillWiseSettlementWiseData.containsKey(taxCode))
		{
		    clsManagerReportBean objManagerReportBean = mapBillWiseSettlementWiseData.get(taxCode);
		    objManagerReportBean.setDblSettlementAmt(objManagerReportBean.getDblTaxAmt() + taxAmt);

		    mapBillWiseSettlementWiseData.put(taxCode, objManagerReportBean);
		}
		else
		{
		    clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		    objManagerReportBean.setStrTaxCode(taxCode);
		    objManagerReportBean.setStrTaxDesc(taxDesc);
		    objManagerReportBean.setDblTaxAmt(taxAmt);

		    mapBillWiseSettlementWiseData.put(taxCode, objManagerReportBean);
		}

		//put total tax dtl
		if (mapBillWiseSettlementWiseData.containsKey("TotalTaxAmt"))
		{
		    clsManagerReportBean objManagerReportBean = mapBillWiseSettlementWiseData.get("TotalTaxAmt");
		    objManagerReportBean.setDblTaxAmt(objManagerReportBean.getDblTaxAmt() + taxAmt);

		    mapBillWiseSettlementWiseData.put("TotalTaxAmt", objManagerReportBean);
		}
		else
		{
		    clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		    objManagerReportBean.setStrTaxCode("TotalTaxAmt");
		    objManagerReportBean.setStrTaxDesc("TotalTaxAmt");
		    objManagerReportBean.setDblTaxAmt(taxAmt);

		    mapBillWiseSettlementWiseData.put("TotalTaxAmt", objManagerReportBean);
		}

		mapBillWiseData.put(strBillNo, mapBillWiseSettlementWiseData);
	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();

		clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		objManagerReportBean.setStrTaxCode(taxCode);
		objManagerReportBean.setStrTaxDesc(taxDesc);
		objManagerReportBean.setDblTaxAmt(taxAmt);

		//put total tax dtl
		objManagerReportBean = new clsManagerReportBean();
		objManagerReportBean.setStrTaxCode("TotalTaxAmt");
		objManagerReportBean.setStrTaxDesc("TotalTaxAmt");
		objManagerReportBean.setDblTaxAmt(taxAmt);

		mapBillWiseSettlementWiseData.put("TotalTaxAmt", objManagerReportBean);

		mapBillWiseSettlementWiseData.put(taxCode, objManagerReportBean);

		mapBillWiseData.put(strBillNo, mapBillWiseSettlementWiseData);
	    }
	}
	rsTaxDtl1.close();

	/**
	 * Q taxes
	 */
	sqlTax = "SELECT a.strBillNo,ifnull(c.strTaxCode,'VAT'),ifnull(c.strTaxDesc,'VAT'),ifnull(SUM(b.dblTaxAmount),0) "
		+ " from tblqbillhd a "
		+ " left outer join tblqbilltaxdtl b on a.strBillNo=b.strBillNo AND DATE(a.dteBillDate)= DATE(b.dteBillDate) AND a.strClientCode=b.strClientCode "
		+ " left outer join tbltaxhd c on b.strTaxCode=c.strTaxCode "
		+ " where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ";
	if (!posCode.equalsIgnoreCase("All"))
	{
	    sqlTax += " and a.strPOSCode='" + posCode + "' ";
	}
	sqlTax += " group by a.strBillNo,c.strTaxCode";
	rsTaxDtl1 = clsGlobalVarClass.dbMysql.executeResultSet(sqlTax);
	while (rsTaxDtl1.next())
	{
	    String strBillNo = rsTaxDtl1.getString(1);
	    String taxCode = rsTaxDtl1.getString(2);
	    String taxDesc = rsTaxDtl1.getString(3);
	    double taxAmt = rsTaxDtl1.getDouble(4);

	    if (taxDesc.length() > maxTaxNameLength)
	    {
		maxTaxNameLength = taxDesc.length();
	    }

	    mapTaxNameWithLength.put(taxDesc, taxDesc.length());

	    totalTaxAmt = totalTaxAmt + taxAmt;

	    if (mapBillWiseTaxNames.containsKey(strBillNo))
	    {
		Map<String, String> mapTaxNames = mapBillWiseTaxNames.get(strBillNo);

		mapTaxNames.put(taxCode, taxDesc);
	    }
	    else
	    {
		Map<String, String> mapTaxNames = new TreeMap<>();

		mapTaxNames.put(taxCode, taxDesc);

		mapBillWiseTaxNames.put(strBillNo, mapTaxNames);
	    }

	    if (mapBillWiseData.containsKey(strBillNo))
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = mapBillWiseData.get(strBillNo);

		//put tax dtl
		if (mapBillWiseSettlementWiseData.containsKey(taxCode))
		{
		    clsManagerReportBean objManagerReportBean = mapBillWiseSettlementWiseData.get(taxCode);
		    objManagerReportBean.setDblSettlementAmt(objManagerReportBean.getDblTaxAmt() + taxAmt);

		    mapBillWiseSettlementWiseData.put(taxCode, objManagerReportBean);
		}
		else
		{
		    clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		    objManagerReportBean.setStrTaxCode(taxCode);
		    objManagerReportBean.setStrTaxDesc(taxDesc);
		    objManagerReportBean.setDblTaxAmt(taxAmt);

		    mapBillWiseSettlementWiseData.put(taxCode, objManagerReportBean);
		}

		//put total tax dtl
		if (mapBillWiseSettlementWiseData.containsKey("TotalTaxAmt"))
		{
		    clsManagerReportBean objManagerReportBean = mapBillWiseSettlementWiseData.get("TotalTaxAmt");
		    objManagerReportBean.setDblTaxAmt(objManagerReportBean.getDblTaxAmt() + taxAmt);

		    mapBillWiseSettlementWiseData.put("TotalTaxAmt", objManagerReportBean);
		}
		else
		{
		    clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		    objManagerReportBean.setStrTaxCode("TotalTaxAmt");
		    objManagerReportBean.setStrTaxDesc("TotalTaxAmt");
		    objManagerReportBean.setDblTaxAmt(taxAmt);

		    mapBillWiseSettlementWiseData.put("TotalTaxAmt", objManagerReportBean);
		}

		mapBillWiseData.put(strBillNo, mapBillWiseSettlementWiseData);
	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();

		clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		objManagerReportBean.setStrTaxCode(taxCode);
		objManagerReportBean.setStrTaxDesc(taxDesc);
		objManagerReportBean.setDblTaxAmt(taxAmt);

		objManagerReportBean = new clsManagerReportBean();
		objManagerReportBean.setStrTaxCode("TotalTaxAmt");
		objManagerReportBean.setStrTaxDesc("TotalTaxAmt");
		objManagerReportBean.setDblTaxAmt(taxAmt);

		mapBillWiseSettlementWiseData.put("TotalTaxAmt", objManagerReportBean);

		mapBillWiseSettlementWiseData.put(taxCode, objManagerReportBean);

		mapBillWiseData.put(strBillNo, mapBillWiseSettlementWiseData);
	    }
	}
	rsTaxDtl1.close();

	maxTaxNameLength = maxTaxNameLength + 1;

	//set discount,roundoff,tip
	sbSqlLiveFile.setLength(0);
	sbSqlLiveFile.append(" SELECT sum(a.dblDiscountAmt),sum(a.dblRoundOff),sum(a.dblTipAmount),a.strBillNo "
		+ " from tblbillhd a  "
		+ " where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		+ "  ");
	if (!posCode.equalsIgnoreCase("All"))
	{
	    sbSqlLiveFile.append(" and a.strPOSCode='" + posCode + "' ");
	}
	sbSqlLiveFile.append(" group by a.strBillNo ");
	System.out.println(sbSqlLiveFile);

	rsSettleManager = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLiveFile.toString());
	while (rsSettleManager.next())
	{
	    double discAmt = rsSettleManager.getDouble(1);//discAmt
	    double roundOffAmt = rsSettleManager.getDouble(2);//roundOff
	    double tipAmt = rsSettleManager.getDouble(3);//tipAmt
	    //int noOfBills = rsSettleManager.getInt(4);//bill count
	    totalDiscAmt = totalDiscAmt + discAmt;
	    totalRoundOffAmt = totalRoundOffAmt + roundOffAmt;//roundOff
	    totalTipAmt = totalTipAmt + tipAmt;//tipAmt
	    //totalBills = totalBills + noOfBills;//bill count
	    String strBillNo = rsSettleManager.getString(4);//billDate

	    //discount
	    if (mapBillWiseData.containsKey(strBillNo))
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = mapBillWiseData.get(strBillNo);
		if (mapBillWiseSettlementWiseData.containsKey("DiscAmt"))
		{
		    clsManagerReportBean objDiscAmt = mapBillWiseSettlementWiseData.get("DiscAmt");
		    objDiscAmt.setDblDiscAmt(objDiscAmt.getDblDiscAmt() + discAmt);
		}
		else
		{
		    clsManagerReportBean objDiscAmt = new clsManagerReportBean();
		    objDiscAmt.setDblDiscAmt(discAmt);

		    mapBillWiseSettlementWiseData.put("DiscAmt", objDiscAmt);
		}

	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();

		clsManagerReportBean objDiscAmt = new clsManagerReportBean();
		objDiscAmt.setDblDiscAmt(discAmt);

		mapBillWiseSettlementWiseData.put("DiscAmt", objDiscAmt);

		mapBillWiseData.put(strBillNo, mapBillWiseSettlementWiseData);
	    }

	    //roundoff
	    if (mapBillWiseData.containsKey(strBillNo))
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = mapBillWiseData.get(strBillNo);
		if (mapBillWiseSettlementWiseData.containsKey("RoundOffAmt"))
		{
		    clsManagerReportBean objRoundOffAmt = mapBillWiseSettlementWiseData.get("RoundOffAmt");
		    objRoundOffAmt.setDblRoundOffAmt(objRoundOffAmt.getDblRoundOffAmt() + roundOffAmt);
		}
		else
		{
		    clsManagerReportBean objRoundOffAmt = new clsManagerReportBean();
		    objRoundOffAmt.setDblRoundOffAmt(roundOffAmt);

		    mapBillWiseSettlementWiseData.put("RoundOffAmt", objRoundOffAmt);
		}

	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();

		clsManagerReportBean objRoundOffAmt = new clsManagerReportBean();
		objRoundOffAmt.setDblRoundOffAmt(roundOffAmt);

		mapBillWiseSettlementWiseData.put("RoundOffAmt", objRoundOffAmt);

		mapBillWiseData.put(strBillNo, mapBillWiseSettlementWiseData);
	    }

	    //tip
	    if (mapBillWiseData.containsKey(strBillNo))
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = mapBillWiseData.get(strBillNo);
		if (mapBillWiseSettlementWiseData.containsKey("TipAmt"))
		{
		    clsManagerReportBean objTipAmt = mapBillWiseSettlementWiseData.get("TipAmt");
		    objTipAmt.setDblTipAmt(objTipAmt.getDblTipAmt() + tipAmt);
		}
		else
		{
		    clsManagerReportBean objTipAmt = new clsManagerReportBean();
		    objTipAmt.setDblTipAmt(tipAmt);

		    mapBillWiseSettlementWiseData.put("TipAmt", objTipAmt);
		}

	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();

		clsManagerReportBean objTipAmt = new clsManagerReportBean();
		objTipAmt.setDblTipAmt(tipAmt);

		mapBillWiseSettlementWiseData.put("TipAmt", objTipAmt);

		mapBillWiseData.put(strBillNo, mapBillWiseSettlementWiseData);
	    }
	}
	rsSettleManager.close();

	sbSqlQFile.setLength(0);
	sbSqlQFile.append(" SELECT sum(a.dblDiscountAmt),sum(a.dblRoundOff),sum(a.dblTipAmount),a.strBillNo "
		+ " from tblqbillhd a"
		+ " where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		+ "  ");
	if (!posCode.equalsIgnoreCase("All"))
	{
	    sbSqlQFile.append(" and a.strPOSCode='" + posCode + "' ");
	}
	sbSqlQFile.append(" group by a.strBillNo ");
	System.out.println(sbSqlQFile);

	rsSettleManager = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFile.toString());
	while (rsSettleManager.next())
	{
	    double discAmt = rsSettleManager.getDouble(1);//discAmt
	    double roundOffAmt = rsSettleManager.getDouble(2);//roundOff
	    double tipAmt = rsSettleManager.getDouble(3);//tipAmt
	    // int noOfBills = rsSettleManager.getInt(4);//bill count
	    totalDiscAmt = totalDiscAmt + discAmt;
	    totalRoundOffAmt = totalRoundOffAmt + roundOffAmt;//roundOff
	    totalTipAmt = totalTipAmt + tipAmt;//tipAmt
	    //  totalBills = totalBills + noOfBills;//bill count
	    String strBillNo = rsSettleManager.getString(4);//billDate

	    //discount
	    if (mapBillWiseData.containsKey(strBillNo))
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = mapBillWiseData.get(strBillNo);
		if (mapBillWiseSettlementWiseData.containsKey("DiscAmt"))
		{
		    clsManagerReportBean objDiscAmt = mapBillWiseSettlementWiseData.get("DiscAmt");
		    objDiscAmt.setDblDiscAmt(objDiscAmt.getDblDiscAmt() + discAmt);
		}
		else
		{
		    clsManagerReportBean objDiscAmt = new clsManagerReportBean();
		    objDiscAmt.setDblDiscAmt(discAmt);

		    mapBillWiseSettlementWiseData.put("DiscAmt", objDiscAmt);
		}

	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();

		clsManagerReportBean objDiscAmt = new clsManagerReportBean();
		objDiscAmt.setDblDiscAmt(discAmt);

		mapBillWiseSettlementWiseData.put("DiscAmt", objDiscAmt);

		mapBillWiseData.put(strBillNo, mapBillWiseSettlementWiseData);
	    }

	    //roundoff
	    if (mapBillWiseData.containsKey(strBillNo))
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = mapBillWiseData.get(strBillNo);
		if (mapBillWiseSettlementWiseData.containsKey("RoundOffAmt"))
		{
		    clsManagerReportBean objRoundOffAmt = mapBillWiseSettlementWiseData.get("RoundOffAmt");
		    objRoundOffAmt.setDblRoundOffAmt(objRoundOffAmt.getDblRoundOffAmt() + roundOffAmt);
		}
		else
		{
		    clsManagerReportBean objRoundOffAmt = new clsManagerReportBean();
		    objRoundOffAmt.setDblRoundOffAmt(roundOffAmt);

		    mapBillWiseSettlementWiseData.put("RoundOffAmt", objRoundOffAmt);
		}

	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();

		clsManagerReportBean objRoundOffAmt = new clsManagerReportBean();
		objRoundOffAmt.setDblRoundOffAmt(roundOffAmt);

		mapBillWiseSettlementWiseData.put("RoundOffAmt", objRoundOffAmt);

		mapBillWiseData.put(strBillNo, mapBillWiseSettlementWiseData);
	    }

	    //tip
	    if (mapBillWiseData.containsKey(strBillNo))
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = mapBillWiseData.get(strBillNo);
		if (mapBillWiseSettlementWiseData.containsKey("TipAmt"))
		{
		    clsManagerReportBean objTipAmt = mapBillWiseSettlementWiseData.get("TipAmt");
		    objTipAmt.setDblTipAmt(objTipAmt.getDblTipAmt() + tipAmt);
		}
		else
		{
		    clsManagerReportBean objTipAmt = new clsManagerReportBean();
		    objTipAmt.setDblTipAmt(tipAmt);

		    mapBillWiseSettlementWiseData.put("TipAmt", objTipAmt);
		}

	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapDateWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();

		clsManagerReportBean objTipAmt = new clsManagerReportBean();
		objTipAmt.setDblTipAmt(tipAmt);

		mapDateWiseSettlementWiseData.put("TipAmt", objTipAmt);

		mapBillWiseData.put(strBillNo, mapDateWiseSettlementWiseData);
	    }
	}
	rsSettleManager.close();

	/**
	 * fill live date wise group wise data
	 */
	StringBuilder sqlGroupData = new StringBuilder();

	sqlGroupData.setLength(0);
	sqlGroupData.append("select  a.strBillNo,e.strGroupCode,e.strGroupName,sum(b.dblAmount)SubTotal,sum(b.dblDiscountAmt)Discount,sum(b.dblAmount)-sum(b.dblDiscountAmt)NetTotal "
		+ "from tblbillhd a,tblbilldtl b,tblitemmaster c,tblsubgrouphd d,tblgrouphd e "
		+ "where a.strBillNo=b.strBillNo "
		+ "AND DATE(a.dteBillDate)= DATE(b.dteBillDate) "
		+ "and b.strItemCode=c.strItemCode "
		+ "and c.strSubGroupCode=d.strSubGroupCode "
		+ "and d.strGroupCode=e.strGroupCode "
		+ "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	if (!posCode.equalsIgnoreCase("All"))
	{
	    sqlGroupData.append(" and a.strPOSCode='" + posCode + "' ");
	}
	sqlGroupData.append("group by  a.strBillNo,e.strGroupCode ");
	ResultSet rsGroupsData = clsGlobalVarClass.dbMysql.executeResultSet(sqlGroupData.toString());
	while (rsGroupsData.next())
	{
	    String strBillNo = rsGroupsData.getString(1);//date
	    String groupCode = rsGroupsData.getString(2);//groupCode
	    String groupName = rsGroupsData.getString(3);//groupName
	    double subTotal = rsGroupsData.getDouble(4); //subTotal
	    double discount = rsGroupsData.getDouble(5); //discount
	    double netTotal = rsGroupsData.getDouble(6); //netTotal

	    if (groupName.length() > maxGroupNameLength)
	    {
		maxGroupNameLength = groupName.length();
	    }
	    mapGroupNameWithLength.put(groupName, groupName.length());

	    if (mapBillWiseGroupNames.containsKey(strBillNo))
	    {
		Map<String, String> mapGroupNames = mapBillWiseGroupNames.get(strBillNo);

		mapGroupNames.put(groupCode, groupName);
	    }
	    else
	    {
		Map<String, String> mapGroupNames = new TreeMap<>();

		mapGroupNames.put(groupCode, groupName);

		mapBillWiseGroupNames.put(strBillNo, mapGroupNames);
	    }

	    if (mapBillWiseData.containsKey(strBillNo))
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = mapBillWiseData.get(strBillNo);

		if (mapBillWiseSettlementWiseData.containsKey(groupCode))
		{
		    clsManagerReportBean objGroupDtl = mapBillWiseSettlementWiseData.get(groupCode);

		    objGroupDtl.setDblSubTotal(objGroupDtl.getDblSubTotal() + subTotal);
		    objGroupDtl.setDblDisAmt(objGroupDtl.getDblDisAmt() + discount);
		    objGroupDtl.setDblNetTotal(objGroupDtl.getDblNetTotal() + netTotal);
		}
		else
		{
		    clsManagerReportBean objGroupDtl = new clsManagerReportBean();
		    objGroupDtl.setStrGroupCode(groupCode);
		    objGroupDtl.setStrGroupName(groupName);
		    objGroupDtl.setDblSubTotal(subTotal);
		    objGroupDtl.setDblDisAmt(discount);
		    objGroupDtl.setDblNetTotal(netTotal);

		    mapBillWiseSettlementWiseData.put(groupCode, objGroupDtl);
		}

		//put total settlement dtl
		if (mapBillWiseSettlementWiseData.containsKey("TotalGroupAmt"))
		{
		    clsManagerReportBean objManagerReportBean = mapBillWiseSettlementWiseData.get("TotalGroupAmt");
		    objManagerReportBean.setDblSubTotal(objManagerReportBean.getDblSubTotal() + subTotal);
		    objManagerReportBean.setDblNetTotal(objManagerReportBean.getDblNetTotal() + netTotal);

		    mapBillWiseSettlementWiseData.put("TotalGroupAmt", objManagerReportBean);
		}
		else
		{
		    clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		    objManagerReportBean.setStrGroupCode("TotalGroupAmt");
		    objManagerReportBean.setStrGroupName("TotalGroupAmt");
		    objManagerReportBean.setDblSubTotal(subTotal);
		    objManagerReportBean.setDblNetTotal(netTotal);

		    mapBillWiseSettlementWiseData.put("TotalGroupAmt", objManagerReportBean);
		}
	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();

		clsManagerReportBean objGroupDtl = new clsManagerReportBean();
		objGroupDtl.setStrGroupCode(groupCode);
		objGroupDtl.setStrGroupName(groupName);
		objGroupDtl.setDblSubTotal(subTotal);
		objGroupDtl.setDblDisAmt(discount);
		objGroupDtl.setDblNetTotal(netTotal);

		//put total settlement dtl
		clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		objManagerReportBean.setStrGroupCode("TotalGroupAmt");
		objManagerReportBean.setStrGroupName("TotalGroupAmt");
		objManagerReportBean.setDblSubTotal(subTotal);
		objManagerReportBean.setDblNetTotal(netTotal);

		mapBillWiseSettlementWiseData.put("TotalGroupAmt", objManagerReportBean);

		mapBillWiseSettlementWiseData.put(groupCode, objGroupDtl);

		mapBillWiseData.put(strBillNo, mapBillWiseSettlementWiseData);
	    }
	}
	rsGroupsData.close();

	/**
	 * fill live modifiers date wise group wise data
	 */
	sqlGroupData.setLength(0);
	sqlGroupData.append("select a.strBillNo,e.strGroupCode,e.strGroupName,sum(b.dblAmount)SubTotal,sum(b.dblDiscAmt)Discount,sum(b.dblAmount)-sum(b.dblDiscAmt)NetTotal "
		+ "from tblbillhd a,tblbillmodifierdtl b,tblitemmaster c,tblsubgrouphd d,tblgrouphd e "
		+ "where a.strBillNo=b.strBillNo "
		+ "AND DATE(a.dteBillDate)= DATE(b.dteBillDate) "
		+ "and left(b.strItemCode,7)=c.strItemCode "
		+ "and c.strSubGroupCode=d.strSubGroupCode "
		+ "and d.strGroupCode=e.strGroupCode "
		+ "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	if (!posCode.equalsIgnoreCase("All"))
	{
	    sqlGroupData.append(" and a.strPOSCode='" + posCode + "' ");
	}
	sqlGroupData.append("group by a.strBillNo,e.strGroupCode ");
	rsGroupsData = clsGlobalVarClass.dbMysql.executeResultSet(sqlGroupData.toString());
	while (rsGroupsData.next())
	{
	    String strBillNo = rsGroupsData.getString(1);//date
	    String groupCode = rsGroupsData.getString(2);//groupCode
	    String groupName = rsGroupsData.getString(3);//groupName
	    double subTotal = rsGroupsData.getDouble(4); //subTotal
	    double discount = rsGroupsData.getDouble(5); //discount
	    double netTotal = rsGroupsData.getDouble(6); //netTotal

	    if (groupName.length() > maxGroupNameLength)
	    {
		maxGroupNameLength = groupName.length();
	    }
	    mapGroupNameWithLength.put(groupName, groupName.length());

	    if (mapBillWiseGroupNames.containsKey(strBillNo))
	    {
		Map<String, String> mapGroupNames = mapBillWiseGroupNames.get(strBillNo);

		mapGroupNames.put(groupCode, groupName);
	    }
	    else
	    {
		Map<String, String> mapGroupNames = new TreeMap<>();

		mapGroupNames.put(groupCode, groupName);

		mapBillWiseGroupNames.put(strBillNo, mapGroupNames);
	    }

	    if (mapBillWiseData.containsKey(strBillNo))
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = mapBillWiseData.get(strBillNo);

		if (mapBillWiseSettlementWiseData.containsKey(groupCode))
		{
		    clsManagerReportBean objGroupDtl = mapBillWiseSettlementWiseData.get(groupCode);

		    objGroupDtl.setDblSubTotal(objGroupDtl.getDblSubTotal() + subTotal);
		    objGroupDtl.setDblDisAmt(objGroupDtl.getDblDisAmt() + discount);
		    objGroupDtl.setDblNetTotal(objGroupDtl.getDblNetTotal() + netTotal);
		}
		else
		{
		    clsManagerReportBean objGroupDtl = new clsManagerReportBean();
		    objGroupDtl.setStrGroupCode(groupCode);
		    objGroupDtl.setStrGroupName(groupName);
		    objGroupDtl.setDblSubTotal(subTotal);
		    objGroupDtl.setDblDisAmt(discount);
		    objGroupDtl.setDblNetTotal(netTotal);

		    mapBillWiseSettlementWiseData.put(groupCode, objGroupDtl);
		}

		//put total settlement dtl
		if (mapBillWiseSettlementWiseData.containsKey("TotalGroupAmt"))
		{
		    clsManagerReportBean objManagerReportBean = mapBillWiseSettlementWiseData.get("TotalGroupAmt");
		    objManagerReportBean.setDblSubTotal(objManagerReportBean.getDblSubTotal() + subTotal);
		    objManagerReportBean.setDblNetTotal(objManagerReportBean.getDblNetTotal() + netTotal);

		    mapBillWiseSettlementWiseData.put("TotalGroupAmt", objManagerReportBean);
		}
		else
		{
		    clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		    objManagerReportBean.setStrGroupCode("TotalGroupAmt");
		    objManagerReportBean.setStrGroupName("TotalGroupAmt");
		    objManagerReportBean.setDblSubTotal(subTotal);
		    objManagerReportBean.setDblNetTotal(netTotal);

		    mapBillWiseSettlementWiseData.put("TotalGroupAmt", objManagerReportBean);
		}
	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();

		clsManagerReportBean objGroupDtl = new clsManagerReportBean();
		objGroupDtl.setStrGroupCode(groupCode);
		objGroupDtl.setStrGroupName(groupName);
		objGroupDtl.setDblSubTotal(subTotal);
		objGroupDtl.setDblDisAmt(discount);
		objGroupDtl.setDblNetTotal(netTotal);

		//put total settlement dtl
		clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		objManagerReportBean.setStrGroupCode("TotalGroupAmt");
		objManagerReportBean.setStrGroupName("TotalGroupAmt");
		objManagerReportBean.setDblSubTotal(subTotal);
		objManagerReportBean.setDblNetTotal(netTotal);

		mapBillWiseSettlementWiseData.put("TotalGroupAmt", objManagerReportBean);

		mapBillWiseSettlementWiseData.put(groupCode, objGroupDtl);

		mapBillWiseData.put(strBillNo, mapBillWiseSettlementWiseData);
	    }
	}
	rsGroupsData.close();

	/**
	 * fill Q date wise group wise data
	 */
	sqlGroupData.setLength(0);
	sqlGroupData.append("select a.strBillNo,e.strGroupCode,e.strGroupName,sum(b.dblAmount)SubTotal,sum(b.dblDiscountAmt)Discount,sum(b.dblAmount)-sum(b.dblDiscountAmt)NetTotal "
		+ "from tblqbillhd a,tblqbilldtl b,tblitemmaster c,tblsubgrouphd d,tblgrouphd e "
		+ "where a.strBillNo=b.strBillNo "
		+ "AND DATE(a.dteBillDate)= DATE(b.dteBillDate) "
		+ "and b.strItemCode=c.strItemCode "
		+ "and c.strSubGroupCode=d.strSubGroupCode "
		+ "and d.strGroupCode=e.strGroupCode "
		+ "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	if (!posCode.equalsIgnoreCase("All"))
	{
	    sqlGroupData.append(" and a.strPOSCode='" + posCode + "' ");
	}
	sqlGroupData.append("group by a.strBillNo,e.strGroupCode ");
	rsGroupsData = clsGlobalVarClass.dbMysql.executeResultSet(sqlGroupData.toString());
	while (rsGroupsData.next())
	{
	    String strBillNo = rsGroupsData.getString(1);//date
	    String groupCode = rsGroupsData.getString(2);//groupCode
	    String groupName = rsGroupsData.getString(3);//groupName
	    double subTotal = rsGroupsData.getDouble(4); //subTotal
	    double discount = rsGroupsData.getDouble(5); //discount
	    double netTotal = rsGroupsData.getDouble(6); //netTotal

	    if (groupName.length() > maxGroupNameLength)
	    {
		maxGroupNameLength = groupName.length();
	    }
	    mapGroupNameWithLength.put(groupName, groupName.length());

	    if (mapBillWiseGroupNames.containsKey(strBillNo))
	    {
		Map<String, String> mapGroupNames = mapBillWiseGroupNames.get(strBillNo);

		mapGroupNames.put(groupCode, groupName);
	    }
	    else
	    {
		Map<String, String> mapGroupNames = new TreeMap<>();

		mapGroupNames.put(groupCode, groupName);

		mapBillWiseGroupNames.put(strBillNo, mapGroupNames);
	    }

	    if (mapBillWiseData.containsKey(strBillNo))
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = mapBillWiseData.get(strBillNo);

		if (mapBillWiseSettlementWiseData.containsKey(groupCode))
		{
		    clsManagerReportBean objGroupDtl = mapBillWiseSettlementWiseData.get(groupCode);

		    objGroupDtl.setDblSubTotal(objGroupDtl.getDblSubTotal() + subTotal);
		    objGroupDtl.setDblDisAmt(objGroupDtl.getDblDisAmt() + discount);
		    objGroupDtl.setDblNetTotal(objGroupDtl.getDblNetTotal() + netTotal);
		}
		else
		{
		    clsManagerReportBean objGroupDtl = new clsManagerReportBean();
		    objGroupDtl.setStrGroupCode(groupCode);
		    objGroupDtl.setStrGroupName(groupName);
		    objGroupDtl.setDblSubTotal(subTotal);
		    objGroupDtl.setDblDisAmt(discount);
		    objGroupDtl.setDblNetTotal(netTotal);

		    mapBillWiseSettlementWiseData.put(groupCode, objGroupDtl);
		}

		//put total settlement dtl
		if (mapBillWiseSettlementWiseData.containsKey("TotalGroupAmt"))
		{
		    clsManagerReportBean objManagerReportBean = mapBillWiseSettlementWiseData.get("TotalGroupAmt");
		    objManagerReportBean.setDblSubTotal(objManagerReportBean.getDblSubTotal() + subTotal);
		    objManagerReportBean.setDblNetTotal(objManagerReportBean.getDblNetTotal() + netTotal);

		    mapBillWiseSettlementWiseData.put("TotalGroupAmt", objManagerReportBean);
		}
		else
		{
		    clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		    objManagerReportBean.setStrGroupCode("TotalGroupAmt");
		    objManagerReportBean.setStrGroupName("TotalGroupAmt");
		    objManagerReportBean.setDblSubTotal(subTotal);
		    objManagerReportBean.setDblNetTotal(netTotal);

		    mapBillWiseSettlementWiseData.put("TotalGroupAmt", objManagerReportBean);
		}
	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();

		clsManagerReportBean objGroupDtl = new clsManagerReportBean();
		objGroupDtl.setStrGroupCode(groupCode);
		objGroupDtl.setStrGroupName(groupName);
		objGroupDtl.setDblSubTotal(subTotal);
		objGroupDtl.setDblDisAmt(discount);
		objGroupDtl.setDblNetTotal(netTotal);

		//put total settlement dtl
		clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		objManagerReportBean.setStrGroupCode("TotalGroupAmt");
		objManagerReportBean.setStrGroupName("TotalGroupAmt");
		objManagerReportBean.setDblSubTotal(subTotal);
		objManagerReportBean.setDblNetTotal(netTotal);

		mapBillWiseSettlementWiseData.put("TotalGroupAmt", objManagerReportBean);

		mapBillWiseSettlementWiseData.put(groupCode, objGroupDtl);

		mapBillWiseData.put(strBillNo, mapBillWiseSettlementWiseData);
	    }
	}
	rsGroupsData.close();

	/**
	 * fill Q modifiers date wise group wise data
	 */
	sqlGroupData.setLength(0);
	sqlGroupData.append("select a.strBillNo,e.strGroupCode,e.strGroupName,sum(b.dblAmount)SubTotal,sum(b.dblDiscAmt)Discount,sum(b.dblAmount)-sum(b.dblDiscAmt)NetTotal "
		+ "from tblqbillhd a,tblqbillmodifierdtl b,tblitemmaster c,tblsubgrouphd d,tblgrouphd e "
		+ "where a.strBillNo=b.strBillNo "
		+ "AND DATE(a.dteBillDate)= DATE(b.dteBillDate) "
		+ "and left(b.strItemCode,7)=c.strItemCode "
		+ "and c.strSubGroupCode=d.strSubGroupCode "
		+ "and d.strGroupCode=e.strGroupCode "
		+ "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	if (!posCode.equalsIgnoreCase("All"))
	{
	    sqlGroupData.append(" and a.strPOSCode='" + posCode + "' ");
	}
	sqlGroupData.append("group by a.strBillNo,e.strGroupCode ");
	rsGroupsData = clsGlobalVarClass.dbMysql.executeResultSet(sqlGroupData.toString());
	while (rsGroupsData.next())
	{
	    String strBillNo = rsGroupsData.getString(1);//date
	    String groupCode = rsGroupsData.getString(2);//groupCode
	    String groupName = rsGroupsData.getString(3);//groupName
	    double subTotal = rsGroupsData.getDouble(4); //subTotal
	    double discount = rsGroupsData.getDouble(5); //discount
	    double netTotal = rsGroupsData.getDouble(6); //netTotal

	    if (groupName.length() > maxGroupNameLength)
	    {
		maxGroupNameLength = groupName.length();
	    }
	    mapGroupNameWithLength.put(groupName, groupName.length());

	    if (mapBillWiseGroupNames.containsKey(strBillNo))
	    {
		Map<String, String> mapGroupNames = mapBillWiseGroupNames.get(strBillNo);

		mapGroupNames.put(groupCode, groupName);
	    }
	    else
	    {
		Map<String, String> mapGroupNames = new TreeMap<>();

		mapGroupNames.put(groupCode, groupName);

		mapBillWiseGroupNames.put(strBillNo, mapGroupNames);
	    }

	    if (mapBillWiseData.containsKey(strBillNo))
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = mapBillWiseData.get(strBillNo);

		if (mapBillWiseSettlementWiseData.containsKey(groupCode))
		{
		    clsManagerReportBean objGroupDtl = mapBillWiseSettlementWiseData.get(groupCode);

		    objGroupDtl.setDblSubTotal(objGroupDtl.getDblSubTotal() + subTotal);
		    objGroupDtl.setDblDisAmt(objGroupDtl.getDblDisAmt() + discount);
		    objGroupDtl.setDblNetTotal(objGroupDtl.getDblNetTotal() + netTotal);
		}
		else
		{
		    clsManagerReportBean objGroupDtl = new clsManagerReportBean();
		    objGroupDtl.setStrGroupCode(groupCode);
		    objGroupDtl.setStrGroupName(groupName);
		    objGroupDtl.setDblSubTotal(subTotal);
		    objGroupDtl.setDblDisAmt(discount);
		    objGroupDtl.setDblNetTotal(netTotal);

		    mapBillWiseSettlementWiseData.put(groupCode, objGroupDtl);
		}

		//put total settlement dtl
		if (mapBillWiseSettlementWiseData.containsKey("TotalGroupAmt"))
		{
		    clsManagerReportBean objManagerReportBean = mapBillWiseSettlementWiseData.get("TotalGroupAmt");
		    objManagerReportBean.setDblSubTotal(objManagerReportBean.getDblSubTotal() + subTotal);
		    objManagerReportBean.setDblNetTotal(objManagerReportBean.getDblNetTotal() + netTotal);

		    mapBillWiseSettlementWiseData.put("TotalGroupAmt", objManagerReportBean);
		}
		else
		{
		    clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		    objManagerReportBean.setStrGroupCode("TotalGroupAmt");
		    objManagerReportBean.setStrGroupName("TotalGroupAmt");
		    objManagerReportBean.setDblSubTotal(subTotal);
		    objManagerReportBean.setDblNetTotal(netTotal);

		    mapBillWiseSettlementWiseData.put("TotalGroupAmt", objManagerReportBean);
		}
	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();

		clsManagerReportBean objGroupDtl = new clsManagerReportBean();
		objGroupDtl.setStrGroupCode(groupCode);
		objGroupDtl.setStrGroupName(groupName);
		objGroupDtl.setDblSubTotal(subTotal);
		objGroupDtl.setDblDisAmt(discount);
		objGroupDtl.setDblNetTotal(netTotal);

		//put total settlement dtl
		clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		objManagerReportBean.setStrGroupCode("TotalGroupAmt");
		objManagerReportBean.setStrGroupName("TotalGroupAmt");
		objManagerReportBean.setDblSubTotal(subTotal);
		objManagerReportBean.setDblNetTotal(netTotal);

		mapBillWiseSettlementWiseData.put("TotalGroupAmt", objManagerReportBean);

		mapBillWiseSettlementWiseData.put(groupCode, objGroupDtl);

		mapBillWiseData.put(strBillNo, mapBillWiseSettlementWiseData);
	    }
	}
	rsGroupsData.close();

	/**
	 * new logic for gross sales
	 */
	final String CASHCARDGUESTCREDIT = "CASH GUEST CREDIT CARD  Total";

	Comparator<String> settlementSorting = new Comparator<String>()
	{
	    @Override
	    public int compare(String o1, String o2)
	    {

		return CASHCARDGUESTCREDIT.indexOf(o1) - CASHCARDGUESTCREDIT.indexOf(o2);

	    }
	};
	Map<String, Map<String, Double>> mapSettelemtWiseGroupBreakup = new TreeMap<>(settlementSorting);

	Map<String, Map<String, Double>> mapSettelemtWiseTaxBreakup = new TreeMap<>();
	/**
	 * new logic for gross sales
	 */

	if (mapBillWiseData.size() > 0)
	{
	    for (Map.Entry<String, Map<String, clsManagerReportBean>> entrySet : mapBillWiseData.entrySet())
	    {
		String strBillNo = entrySet.getKey();
		Map<String, clsManagerReportBean> mapBillWiseGroupTaxSettlementData = entrySet.getValue();

		clsManagerReportBean objTotalSettlementAmt = mapBillWiseGroupTaxSettlementData.get("TotalSettlementAmt");
		double totalSettlementAmt = 0;
		if (objTotalSettlementAmt != null)
		{
		    totalSettlementAmt = objTotalSettlementAmt.getDblSettlementAmt();
		}
		clsManagerReportBean objTotalTaxAmt = mapBillWiseGroupTaxSettlementData.get("TotalTaxAmt");
		double totalTaxAmt = 0;
		if (objTotalTaxAmt != null)
		{
		    totalTaxAmt = objTotalTaxAmt.getDblTaxAmt();
		}

		clsManagerReportBean objTotalGroupAmt = mapBillWiseGroupTaxSettlementData.get("TotalGroupAmt");
		//double totalGroupSubTotal = objTotalGroupAmt.getDblSubTotal();
		double totalGroupNetTotal = 0;
		if (objTotalGroupAmt != null)
		{
		    totalGroupNetTotal = objTotalGroupAmt.getDblNetTotal();
		}

		String labelSettlement = "SETTLEMENT          |";

		String horizontalTotalLabel = "  TOTALS   |";

		Map<String, String> mapBillWiseTaxeNames = mapBillWiseTaxNames.get(strBillNo);

		Iterator<String> ketIterator = mapBillWiseTaxeNames.keySet().iterator();
		while (ketIterator.hasNext())
		{
		    String taxCode = ketIterator.next();
		    if (taxCode == null || taxCode.isEmpty())
		    {
			ketIterator.remove();
		    }
		}

		if (mapBillWiseTaxeNames != null)
		{
		    if (mapBillWiseGroupNames.containsKey(strBillNo))
		    {
			Map<String, String> mapGroupNames = mapBillWiseGroupNames.get(strBillNo);
			for (Map.Entry<String, String> entryGroupNames : mapGroupNames.entrySet())
			{

			    String groupCode = entryGroupNames.getKey();
			    String groupName = entryGroupNames.getValue();
			    if (groupName.length() > maxGroupNameLength)
			    {
				maxGroupNameLength = groupName.length();
			    }

			    clsManagerReportBean objGroupDtl = mapBillWiseGroupTaxSettlementData.get(groupCode);
			    //double groupSubTotal = objGroupDtl.getDblSubTotal();
			    double groupNetTotal = objGroupDtl.getDblNetTotal();

			    if (mapBillWiseTaxeNames != null)
			    {
				for (String taxDesc : mapBillWiseTaxeNames.values())
				{
				    String labelTaxDesc = taxDesc + "|";
				}
			    }

			    Map<String, String> mapSettlementNames = mapBillWiseSettlementNames.get(strBillNo);

			    if (mapSettlementNames != null)
			    {
				for (Map.Entry<String, String> entrySettlements : mapSettlementNames.entrySet())
				{
				    String settlementCode = entrySettlements.getKey();
				    String settlementName = entrySettlements.getValue();

				    double horizontalTotalAmt = 0.00;

				    clsManagerReportBean objSettlementDtl = mapBillWiseGroupTaxSettlementData.get(settlementCode);

				    double groupSubTotalForThisSettlement = 0.00;
				    if (totalSettlementAmt > 0)
				    {
					groupSubTotalForThisSettlement = (groupNetTotal / totalSettlementAmt) * objSettlementDtl.getDblSettlementAmt();
				    }
				    horizontalTotalAmt += groupSubTotalForThisSettlement;

				    //new added for groups
				    if (mapSettelemtWiseGroupBreakup.containsKey(settlementName))
				    {
					Map<String, Double> mapGroupBreakup = mapSettelemtWiseGroupBreakup.get(settlementName);
					if (mapGroupBreakup.containsKey(groupName))
					{
					    mapGroupBreakup.put(groupName, mapGroupBreakup.get(groupName) + groupSubTotalForThisSettlement);

					    mapSettelemtWiseGroupBreakup.put(settlementName, mapGroupBreakup);
					}
					else
					{
					    mapGroupBreakup.put(groupName, groupSubTotalForThisSettlement);

					    mapSettelemtWiseGroupBreakup.put(settlementName, mapGroupBreakup);
					}
				    }
				    else
				    {
					Map<String, Double> mapGroupBreakup = new TreeMap<String, Double>();

					mapGroupBreakup.put(groupName, groupSubTotalForThisSettlement);

					mapSettelemtWiseGroupBreakup.put(settlementName, mapGroupBreakup);
				    }

				    //new added for groups total
				    String totalSettlement = "Total";
				    if (mapSettelemtWiseGroupBreakup.containsKey(totalSettlement))
				    {
					Map<String, Double> mapGroupBreakup = mapSettelemtWiseGroupBreakup.get(totalSettlement);
					if (mapGroupBreakup.containsKey(groupName))
					{
					    mapGroupBreakup.put(groupName, mapGroupBreakup.get(groupName) + groupSubTotalForThisSettlement);

					    mapSettelemtWiseGroupBreakup.put(totalSettlement, mapGroupBreakup);
					}
					else
					{
					    mapGroupBreakup.put(groupName, groupSubTotalForThisSettlement);

					    mapSettelemtWiseGroupBreakup.put(totalSettlement, mapGroupBreakup);
					}
				    }
				    else
				    {
					Map<String, Double> mapGroupBreakup = new TreeMap<String, Double>();

					mapGroupBreakup.put(groupName, groupSubTotalForThisSettlement);

					mapSettelemtWiseGroupBreakup.put(totalSettlement, mapGroupBreakup);
				    }

				    if (mapBillWiseTaxeNames != null)
				    {
					for (Map.Entry<String, String> entryTaxNames : mapBillWiseTaxeNames.entrySet())
					{
					    String taxCode = entryTaxNames.getKey();
					    String taxName = entryTaxNames.getValue();

					    String labelTaxDesc = taxName + "|";

					    clsManagerReportBean objTaxDtl = mapBillWiseGroupTaxSettlementData.get(taxCode);
					    double taxAmt = objTaxDtl.getDblTaxAmt();

					    double taxWiseGroupTotal = funGetTaxWiseGroupTotal(strBillNo, taxCode, mapBillWiseGroupTaxSettlementData);

					    double taxAmtForThisTax = 0.00;
					    boolean isApplicable = isApplicableTaxOnGroup(taxCode, groupCode);

					    if (taxWiseGroupTotal > 0 && isApplicable)
					    {
						taxAmtForThisTax = (taxAmt / taxWiseGroupTotal) * groupSubTotalForThisSettlement;
					    }
					    horizontalTotalAmt += taxAmtForThisTax;

					    //new added for taxes
					    String key = settlementName + "!" + groupName + "!" + taxName;
					    if (mapSettelemtWiseTaxBreakup.containsKey(settlementName))
					    {
						Map<String, Double> mapTaxBreakup = mapSettelemtWiseTaxBreakup.get(settlementName);
						if (mapTaxBreakup.containsKey(key))
						{
						    mapTaxBreakup.put(key, mapTaxBreakup.get(key) + taxAmtForThisTax);

						    mapSettelemtWiseTaxBreakup.put(settlementName, mapTaxBreakup);
						}
						else
						{
						    mapTaxBreakup.put(key, taxAmtForThisTax);

						    mapSettelemtWiseTaxBreakup.put(settlementName, mapTaxBreakup);
						}
					    }
					    else
					    {
						Map<String, Double> mapTaxBreakup = new TreeMap<String, Double>();

						mapTaxBreakup.put(key, taxAmtForThisTax);

						mapSettelemtWiseTaxBreakup.put(settlementName, mapTaxBreakup);
					    }

					    //new added for total taxes
					    String totalKey = totalSettlement + "!" + groupName + "!" + taxName;
					    if (mapSettelemtWiseTaxBreakup.containsKey(totalSettlement))
					    {
						Map<String, Double> mapTaxBreakup = mapSettelemtWiseTaxBreakup.get(totalSettlement);
						if (mapTaxBreakup.containsKey(totalKey))
						{
						    mapTaxBreakup.put(totalKey, mapTaxBreakup.get(totalKey) + taxAmtForThisTax);

						    mapSettelemtWiseTaxBreakup.put(totalSettlement, mapTaxBreakup);
						}
						else
						{
						    mapTaxBreakup.put(totalKey, taxAmtForThisTax);

						    mapSettelemtWiseTaxBreakup.put(totalSettlement, mapTaxBreakup);
						}
					    }
					    else
					    {
						Map<String, Double> mapTaxBreakup = new TreeMap<String, Double>();

						mapTaxBreakup.put(totalKey, taxAmtForThisTax);

						mapSettelemtWiseTaxBreakup.put(totalSettlement, mapTaxBreakup);
					    }

					}
				    }
				}
			    }
			    if (mapBillWiseTaxeNames != null)
			    {
				for (Map.Entry<String, String> entryTaxNames : mapBillWiseTaxeNames.entrySet())
				{
				    String taxCode = entryTaxNames.getKey();
				    String taxName = entryTaxNames.getValue();

				    String labelTaxDesc = taxName + "|";
				    double taxAmt = 0.00;

				    boolean isApplicable = isApplicableTaxOnGroup(taxCode, groupCode);
				    if (isApplicable)
				    {
					double taxWiseGroupTotal = funGetTaxWiseGroupTotal(strBillNo, taxCode, mapBillWiseGroupTaxSettlementData);
					clsManagerReportBean objTaxDtl = mapBillWiseGroupTaxSettlementData.get(taxCode);
					double totalTaxAmtForGroup = objTaxDtl.getDblTaxAmt();

					if (taxWiseGroupTotal > 0)
					{
					    taxAmt = (totalTaxAmtForGroup / taxWiseGroupTotal) * groupNetTotal;
					}
				    }
				}
			    }
			}
		    }
		    else
		    {
			continue;
		    }
		}
		double BillTotal = totalGroupNetTotal;
		if (mapBillWiseTaxeNames != null)
		{
		    for (Map.Entry<String, String> entryTaxNames : mapBillWiseTaxeNames.entrySet())
		    {
			String taxCode = entryTaxNames.getKey();
			String taxName = entryTaxNames.getValue();

			String labelTaxDesc = "  " + taxName + "|";

			clsManagerReportBean objTaxDtl = mapBillWiseGroupTaxSettlementData.get(taxCode);
			double totalTaxAmtForGroup = objTaxDtl.getDblTaxAmt();
			BillTotal += totalTaxAmtForGroup;
		    }
		}
	    }
	}
	

	if ("Total After Tip Amount".length() > maxSettlementNameLength)
	{
	    maxSettlementNameLength = "Total After Tip Amount".length();
	}

	Iterator<String> keyIterator = mapTaxNameWithLength.keySet().iterator();
	while (keyIterator.hasNext())
	{
	    String taxCode = keyIterator.next();
	    if (taxCode == null || taxCode.isEmpty())
	    {
		keyIterator.remove();
	    }
	}
	double finalNetTotal = 0.00, finalSettlementTotal = 0.00;
	Map<String, Double> mapFinalTaxTotal = new TreeMap<String, Double>();
	mapFinalTaxTotal.put("Total", 0.00);

	/**
	 * Due to the round off issues in final settlement amount which is
	 * calculated by percentage calculation on taxes and groups giving
	 * difference in round off so this logic
	 */
	StringBuilder sbSqlLive = new StringBuilder();
	StringBuilder sqlFilter = new StringBuilder();

	Map<String, clsBillItemDtlBean> mapSettlementModes = new HashMap<>();
	double grossRevenue = 0;

	sbSqlLive.setLength(0);
	sbSqlLive.append("select ifnull(c.strPosCode,'All'),a.strSettelmentDesc, ifnull(SUM(b.dblSettlementAmt),0.00) "
		+ ",ifnull(d.strposname,'All'), if(c.strPOSCode is null,0,COUNT(*)) "
		+ "from tblsettelmenthd a "
		+ "left outer join tblbillsettlementdtl b on a.strSettelmentCode=b.strSettlementCode and date(b.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
		+ "left outer join tblbillhd c on b.strBillNo=c.strBillNo and date(b.dteBillDate)=date(c.dteBillDate) "
		+ "left outer join tblposmaster d on c.strPOSCode=d.strPosCode ");

	sbSqlQFile.setLength(0);
	sbSqlQFile.append("select ifnull(c.strPosCode,'All'),a.strSettelmentDesc, ifnull(SUM(b.dblSettlementAmt),0.00) "
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
	sqlFilter.append("group by a.strSettelmentCode "
		+ "order by b.dblSettlementAmt desc ");

	sbSqlLive.append(sqlFilter);
	sbSqlQFile.append(sqlFilter);

	ResultSet rsData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
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

	for (Map.Entry<String, Map<String, Double>> settlementEntry : mapSettelemtWiseGroupBreakup.entrySet())
	{
	    String settlementName = settlementEntry.getKey();

	    double settlementWiseNetTotal = 0.00;
	    Map<String, Double> mapSettlementWiseTaxTotal = new TreeMap<String, Double>();
	    mapSettlementWiseTaxTotal.put("Total", 0.00);

	    Map<String, Double> mapGroupBreakup = settlementEntry.getValue();

	    String settlementNameForPrinting = settlementName;
	    for (int i = settlementName.length(); i < maxSettlementNameLength; i++)
	    {
		settlementNameForPrinting += " ";
	    }
	    for (Map.Entry<String, Integer> groupEntry : mapGroupNameWithLength.entrySet())
	    {
		double total = 0;
		String groupName = groupEntry.getKey();
		int groupNameLength = groupEntry.getValue();

		double groupNetTotal = 0.00;
		if (mapGroupBreakup.containsKey(groupName))
		{
		    groupNetTotal = mapGroupBreakup.get(groupName);
		}

		total = total + groupNetTotal;
		if (!settlementName.equalsIgnoreCase("Total"))
		{
		    settlementWiseNetTotal += groupNetTotal;
		    finalNetTotal += groupNetTotal;
		}

		String emptySettlementNameForPrinting = "";
		for (int i = emptySettlementNameForPrinting.length(); i < maxSettlementNameLength; i++)
		{
		    emptySettlementNameForPrinting += " ";
		}

		String groupNameForPrinting = groupName;
		for (int i = groupName.length(); i < maxGroupNameLength; i++)
		{
		    groupNameForPrinting += " ";
		}

		if (mapSettelemtWiseTaxBreakup.containsKey(settlementName))
		{
		    Map<String, Double> mapTaxBreakup = mapSettelemtWiseTaxBreakup.get(settlementName);

		    for (Map.Entry<String, Integer> taxEntry : mapTaxNameWithLength.entrySet())
		    {
			String taxName = taxEntry.getKey();
			String key = settlementName + "!" + groupName + "!" + taxName;

			double taxAmt = 0.00;
			if (mapTaxBreakup.containsKey(key))
			{
			    taxAmt = mapTaxBreakup.get(key);
			}

			total = total + taxAmt;

			if (mapSettlementWiseTaxTotal.containsKey(taxName))
			{
			    mapSettlementWiseTaxTotal.put(taxName, mapSettlementWiseTaxTotal.get(taxName) + taxAmt);
			}
			else
			{
			    mapSettlementWiseTaxTotal.put(taxName, taxAmt);
			}

			if (!settlementName.equalsIgnoreCase("Total"))
			{
			    if (mapFinalTaxTotal.containsKey(taxName))
			    {
				mapFinalTaxTotal.put(taxName, mapFinalTaxTotal.get(taxName) + taxAmt);
			    }
			    else
			    {
				mapFinalTaxTotal.put(taxName, taxAmt);
			    }
			}
		    }

		}
		else
		{
		    continue;
		}
	    }
	    if (!settlementName.equalsIgnoreCase("Total"))
	    {
		double finalSettlementWiseTotal = settlementWiseNetTotal;

		String settlementTotalForPrinting = settlementName;
		for (int i = settlementTotalForPrinting.length(); i < maxSettlementNameLength; i++)
		{
		    settlementTotalForPrinting += " ";
		}

		String emptyGroupNameForPrinting = "";
		for (int i = emptyGroupNameForPrinting.length(); i < maxGroupNameLength; i++)
		{
		    emptyGroupNameForPrinting += " ";
		}

		pw.println();
		pw.print(objUtility.funPrintTextWithAlignment(settlementName, 20, "left"));
		//pw.print(objUtility.funPrintTextWithAlignment(emptyGroupNameForPrinting, 10, "left"));
		pw.print(objUtility.funPrintTextWithAlignment(gDecimalFormat.format(settlementWiseNetTotal), 10, "right"));
		pw.print(objUtility.funPrintTextWithAlignment(gDecimalFormat.format(0.00), 10, "right"));
		for (Map.Entry<String, Integer> taxEntry : mapTaxNameWithLength.entrySet())
		{
		    String taxName = taxEntry.getKey();
		    if (taxName.equalsIgnoreCase("VAT"))
		    {
			continue;
		    }
		    double settlementWiseTaxAmt = 0.00;
		    if (mapSettlementWiseTaxTotal.containsKey(taxName))
		    {
			settlementWiseTaxAmt = mapSettlementWiseTaxTotal.get(taxName);
		    }
		    finalSettlementWiseTotal += settlementWiseTaxAmt;
		    pw.print(objUtility.funPrintTextWithAlignment(gDecimalFormat.format(settlementWiseTaxAmt), 20, "right"));
		}
		pw.print(objUtility.funPrintTextWithAlignment(gDecimalFormat.format(0.00), 10, "right"));
		pw.print(objUtility.funPrintTextWithAlignment(gDecimalFormat.format(0.00), 10, "right"));

		double roundOffForThisSettlement = (totalRoundOffAmt / totalSettleAmt) * finalSettlementWiseTotal;
		finalSettlementWiseTotal = finalSettlementWiseTotal + roundOffForThisSettlement;

		double settlementAmount = mapSettlementModes.get(settlementName).getDblSettlementAmt();

		pw.print(objUtility.funPrintTextWithAlignment(gDecimalFormat.format(settlementAmount), 10, "right"));
		pw.println();

	    }
	}
	//print grand total
	double finalTotalSettlementAmounr = finalNetTotal;

	String totalForPrinting = "Total";
	for (int i = totalForPrinting.length(); i < maxSettlementNameLength; i++)
	{
	    totalForPrinting += " ";
	}

	String emptyGroupNameForPrinting = "";
	for (int i = emptyGroupNameForPrinting.length(); i < maxGroupNameLength; i++)
	{
	    emptyGroupNameForPrinting += " ";
	}

	String emptyNetTotalForPrinting = "";
	for (int i = emptyNetTotalForPrinting.length(); i < maxGroupNameLength; i++)
	{
	    emptyNetTotalForPrinting += " ";
	}

	String emptyTaxNameForPrinting = "";
	for (int i = emptyTaxNameForPrinting.length(); i < maxTaxNameLength; i++)
	{
	    emptyTaxNameForPrinting += " ";
	}

	String roundOffForPrinting = "Round Off";
	for (int i = "Round Off".length(); i < maxSettlementNameLength; i++)
	{
	    roundOffForPrinting += " ";
	}

	String tipForPrinting = "Tip Amount";
	for (int i = "Tip Amount".length(); i < maxSettlementNameLength; i++)
	{
	    tipForPrinting += " ";
	}

	String totalAfterTipForPrinting = "Total After Tip Amount";
	for (int i = totalAfterTipForPrinting.length(); i < maxSettlementNameLength; i++)
	{
	    totalAfterTipForPrinting += " ";
	}

	int noOfColumns = 120;
	String DASHLINE = "";
	for (int i = 0; i < noOfColumns; i++)
	{
	    DASHLINE = DASHLINE + "-";
	}
	pw.println();
	pw.print(DASHLINE);

	pw.println();
	pw.print(objUtility.funPrintTextWithAlignment("Total Sale", 20, "left"));
	//pw.print(objUtility.funPrintTextWithAlignment(emptyGroupNameForPrinting, 10, "right"));
	pw.print(objUtility.funPrintTextWithAlignment(gDecimalFormat.format(finalNetTotal), 10, "right"));
	pw.print(objUtility.funPrintTextWithAlignment(gDecimalFormat.format(0.00), 10, "right"));
	for (Map.Entry<String, Integer> taxEntry : mapTaxNameWithLength.entrySet())
	{
	    String taxName = taxEntry.getKey();
	    if (taxName.equalsIgnoreCase("VAT"))
	    {
		continue;
	    }
	    double settlementWiseTaxAmt = 0.00;
	    if (mapFinalTaxTotal.containsKey(taxName))
	    {
		settlementWiseTaxAmt = mapFinalTaxTotal.get(taxName);
	    }
	    finalTotalSettlementAmounr += settlementWiseTaxAmt;
	    pw.print(objUtility.funPrintTextWithAlignment(gDecimalFormat.format(settlementWiseTaxAmt), 20, "right"));
	}
	pw.print(objUtility.funPrintTextWithAlignment(gDecimalFormat.format(0.00), 10, "right"));
	pw.print(objUtility.funPrintTextWithAlignment(gDecimalFormat.format(0.00), 10, "right"));
	pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(Math.rint(finalTotalSettlementAmounr+totalRoundOffAmt)), 10, "right"));

	pw.println();
	pw.print(DASHLINE);

	
//	List<clsBillItemDtlBean> listOfSettlementData = new ArrayList<clsBillItemDtlBean>();
//
//	for (clsBillItemDtlBean objDtlBean : mapSettlementModes.values())
//	{
//	    listOfSettlementData.add(objDtlBean);
//	}
//	final String CASHCARDGUESTCREDITLIST = "CASH GUEST CREDIT CARD  Total";
//
//	Comparator<clsBillItemDtlBean> settlementSortingList = new Comparator<clsBillItemDtlBean>()
//	{
//	    @Override
//	    public int compare(clsBillItemDtlBean o1, clsBillItemDtlBean o2)
//	    {
//
//		return CASHCARDGUESTCREDIT.indexOf(o1.getStrSettelmentMode()) - CASHCARDGUESTCREDIT.indexOf(o2.getStrSettelmentMode());
//
//	    }
//	};
//
//	Collections.sort(listOfSettlementData, settlementSortingList);
//
//	double totalCash = 0;
//	int i = 0;
//	for (clsBillItemDtlBean objSettlement : listOfSettlementData)
//	{
//
//	    if (objSettlement.getStrSettelmentMode().equalsIgnoreCase("CASH"))
//	    {
//		totalCash = objSettlement.getDblSettlementAmt();
//	    }
//
//	    pw.println();
//	    pw.print(objUtility.funPrintTextWithAlignment(objSettlement.getStrSettelmentMode(), 20, "left"));
//	    pw.print(objUtility.funPrintTextWithAlignment(gDecimalFormat.format(objSettlement.getDblSettlementAmt()), 10, "right"));
//
//	    if (i == 0)
//	    {
//		pw.print(objUtility.funPrintTextWithAlignment("     Pending Bills".toUpperCase(), 25, "left"));
//		pw.print(objUtility.funPrintTextWithAlignment(gDecimalFormat.format(0.00), 10, "right"));
//	    }
//	    if (i == 1)
//	    {
//		pw.print(objUtility.funPrintTextWithAlignment("     Discount".toUpperCase(), 25, "left"));
//		pw.print(objUtility.funPrintTextWithAlignment(gDecimalFormat.format(totalDiscAmt), 10, "right"));
//	    }
//	    if (i == 2)
//	    {
//		pw.print(objUtility.funPrintTextWithAlignment("     Tip".toUpperCase(), 25, "left"));
//		pw.print(objUtility.funPrintTextWithAlignment(gDecimalFormat.format(totalTipAmt), 10, "right"));
//	    }
//	    i++;
//	}
//
//	pw.println();
//	pw.print(objUtility.funPrintTextWithAlignment("Advance".toUpperCase(), 20, "left"));
//	pw.print(objUtility.funPrintTextWithAlignment(gDecimalFormat.format(0.00), 10, "right"));
//
//	pw.print(objUtility.funPrintTextWithAlignment("     SGST+CGST".toUpperCase(), 25, "left"));
//	pw.print(objUtility.funPrintTextWithAlignment(gDecimalFormat.format(totalTaxAmt), 10, "right"));
//
//	pw.println();
//	pw.print(objUtility.funPrintTextWithAlignment("", 20, "left"));
//	pw.print(objUtility.funPrintTextWithAlignment("", 10, "right"));
//
//	pw.print(objUtility.funPrintTextWithAlignment("     Total Service Charge".toUpperCase(), 25, "left"));
//	pw.print(objUtility.funPrintTextWithAlignment(gDecimalFormat.format(0.00), 10, "right"));
//
//	pw.println();
//	pw.print(objUtility.funPrintTextWithAlignment("", 20, "left"));
//	pw.print(objUtility.funPrintTextWithAlignment("", 10, "right"));
//
//	pw.print(objUtility.funPrintTextWithAlignment("     Excess".toUpperCase(), 25, "left"));
//	pw.print(objUtility.funPrintTextWithAlignment(gDecimalFormat.format(0.00), 10, "right"));
//
//	pw.println();
//	pw.print(objUtility.funPrintTextWithAlignment("Pending Bills Rec.".toUpperCase(), 20, "left"));
//	pw.print(objUtility.funPrintTextWithAlignment("0.00", 10, "right"));

//	pw.print(objUtility.funPrintTextWithAlignment("     Total Cash".toUpperCase(), 25, "left"));
//	pw.print(objUtility.funPrintTextWithAlignment(gDecimalFormat.format(totalCash), 10, "right"));
	pw.println();
	pw.println();
	pw.println();
	pw.println("End Of Bill Register.");

	return 1;
    }

    private double funGetTaxWiseGroupTotal(String billDate, String taxCode, Map<String, clsManagerReportBean> mapBillWiseGroupTaxSettlementData)
    {
	double taxWiseGroupTotal = 0.00;

	try
	{
	    String sql = "select distinct(b.strGroupCode),b.strGroupName,a.strTaxOnGD "
		    + "from tbltaxhd a,tbltaxongroup b "
		    + "where a.strTaxCode=b.strTaxCode "
		    + "and b.strTaxCode='" + taxCode + "' "
		    + "and b.strApplicable='true' ";
	    ResultSet rsIsApplicable = clsGlobalVarClass.dbMysql.executeResultSet(sql);
	    while (rsIsApplicable.next())
	    {
		String groupCode = rsIsApplicable.getString(1);//groupCode
		String taxOnGD = rsIsApplicable.getString(3);//taxOnGD

		if (mapBillWiseGroupTaxSettlementData.containsKey(groupCode))
		{
		    clsManagerReportBean objGroupDtl = mapBillWiseGroupTaxSettlementData.get(groupCode);
		    if (taxOnGD.equalsIgnoreCase("Gross"))
		    {
			taxWiseGroupTotal += objGroupDtl.getDblSubTotal();
		    }
		    else
		    {
			taxWiseGroupTotal += objGroupDtl.getDblNetTotal();
		    }
		}
	    }
	    rsIsApplicable.close();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	finally
	{
	    return taxWiseGroupTotal;
	}
    }

    private boolean isApplicableTaxOnGroup(String taxCode, String groupCode)
    {
	boolean isApplicable = false;
	try
	{
	    String sql = "select a.strTaxCode,a.strGroupCode,a.strGroupName,a.strApplicable "
		    + "from tbltaxongroup a "
		    + "where a.strTaxCode='" + taxCode + "' "
		    + "and a.strGroupCode='" + groupCode + "' "
		    + "and a.strApplicable='true' ";
	    ResultSet rsIsApplicable = clsGlobalVarClass.dbMysql.executeResultSet(sql);
	    if (rsIsApplicable.next())
	    {
		isApplicable = true;
	    }
	    rsIsApplicable.close();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	finally
	{
	    return isApplicable;
	}
    }

    private int funGetLineCount(String billNo, String labelSettlement, String labelGroupName, String horizontalTotalLabel, Map<String, Map<String, clsManagerReportBean>> mapBillWiseData, Map<String, Map<String, String>> mapBillWiseSettlemetNames, Map<String, Map<String, String>> mapBillWiseTaxNames)
    {

	StringBuilder stringBuilder = new StringBuilder();
	stringBuilder.append(labelSettlement);
	stringBuilder.append(labelGroupName);

	Map<String, String> map = mapBillWiseTaxNames.get(billNo);
	if (map != null)
	{
	    for (String taxDesc : map.values())
	    {
		String labelTaxDesc = taxDesc + "|";
		stringBuilder.append(labelTaxDesc);
	    }
	}
	stringBuilder.append(horizontalTotalLabel);

	return stringBuilder.length();
    }
}
