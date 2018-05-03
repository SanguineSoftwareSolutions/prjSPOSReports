package com.POSReport.view;

import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsPosConfigFile;
import com.POSGlobal.controller.clsUtility;
import com.POSGlobal.view.frmOkPopUp;
import com.POSGlobal.view.frmShowTextFile;
import com.POSReport.controller.clsCostCenterBean;
import com.POSReport.controller.comparator.clsCostCenterComparator;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.swing.JRViewer;

public class frmCostCenterReport extends javax.swing.JFrame
{

    public String fromDate, toDate, imagePath;
    private String reportName;
    private clsUtility objUtility;
    private final String gDecimalFormatString = clsGlobalVarClass.funGetGlobalDecimalFormatString();

    public frmCostCenterReport()
    {
	initComponents();

	try
	{
	    Timer timer = new Timer(500, new ActionListener()
	    {
		@Override
		public void actionPerformed(ActionEvent e)
		{
		    Date date1 = new Date();
		    String newstr = String.format("%tr", date1);
		    String dateAndTime = clsGlobalVarClass.gPOSDateToDisplay + " " + newstr;
		    lblDate.setText(dateAndTime);
		}
	    });
	    timer.setRepeats(true);
	    timer.setCoalesce(true);
	    timer.setInitialDelay(0);
	    timer.start();
	    lblUserCode.setText(clsGlobalVarClass.gUserCode);
	    lblPosName.setText(clsGlobalVarClass.gPOSName);
	    lblModuleName.setText(clsGlobalVarClass.gSelectedModule);

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}

	try
	{

	    objUtility = new clsUtility();
	    dteFromDate.setDate(objUtility.funGetDateToSetCalenderDate());
	    dteToDate.setDate(objUtility.funGetDateToSetCalenderDate());
	    imagePath = System.getProperty("user.dir");
	    imagePath = imagePath + File.separator + "ReportImage";

	    String sql = null;
	    ResultSet rs = null;

	    if (clsGlobalVarClass.gShowOnlyLoginPOSReports)
	    {
		cmbPosCode.addItem(clsGlobalVarClass.gPOSName + "                                        " + clsGlobalVarClass.gPOSCode);
	    }
	    else
	    {
		cmbPosCode.addItem("All                                        All");
		cmbCostCenterName.addItem("All");
		sql = "select strPosName,strPosCode from tblposmaster";
		rs = clsGlobalVarClass.dbMysql.executeResultSet(sql);
		while (rs.next())
		{
		    cmbPosCode.addItem(rs.getString(1) + "                                        " + rs.getString(2));
		}
		rs.close();
	    }

	    sql = "select strCostCenterName,strCostCenterCode from tblcostcentermaster";
	    rs = clsGlobalVarClass.dbMysql.executeResultSet(sql);
	    while (rs.next())
	    {
		cmbCostCenterName.addItem(rs.getString(1) + "                                        " + rs.getString(2));
	    }
	    rs.close();

	    funFillShiftCombo();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}

	if (clsGlobalVarClass.gNoOfDaysReportsView != 0)
	{
	    try
	    {

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		final Date userDateRange = dateFormat.parse(clsGlobalVarClass.gPOSOnlyDateForTransaction);
		int days = userDateRange.getDate() - clsGlobalVarClass.gNoOfDaysReportsView;
		userDateRange.setDate(days);

		dteFromDate.getJCalendar().setMinSelectableDate(userDateRange);

		dteFromDate.getDateEditor().addPropertyChangeListener(new PropertyChangeListener()
		{
		    @Override
		    public void propertyChange(PropertyChangeEvent e)
		    {
			if ("date".equals(e.getPropertyName()))
			{
			    Date dateChooserValue = (Date) e.getNewValue();

			    if (clsGlobalVarClass.gNoOfDaysReportsView != 0 && dateChooserValue.before(userDateRange))
			    {
				try
				{
				    java.util.Date date = new SimpleDateFormat("dd-MM-yyyy").parse(clsGlobalVarClass.gPOSDateToDisplay);
				    dteFromDate.setDate(date);
				}
				catch (Exception ex)
				{
				    ex.printStackTrace();
				}
			    }
			}
		    }
		});
	    }
	    catch (Exception e)
	    {
		e.printStackTrace();
	    }
	}
    }

    private void funCreateTempFolder()
    {
	String filePath = System.getProperty("user.dir");
	File TextKOT = new File(filePath + File.separator + "Temp");
	if (!TextKOT.exists())
	{
	    TextKOT.mkdirs();
	}
    }

    private void funCreateJasperReport()
    {
	StringBuilder sbSqlLive = new StringBuilder();
	StringBuilder sbSqlQFile = new StringBuilder();
	StringBuilder sbSqlModLive = new StringBuilder();
	StringBuilder sbSqlModQFile = new StringBuilder();
	StringBuilder sbSqlFilters = new StringBuilder();

	if ((dteToDate.getDate().getTime() - dteFromDate.getDate().getTime()) < 0)
	{
	    new frmOkPopUp(this, "Invalid date", "Error", 1).setVisible(true);
	}
	else
	{

	    if ("Summary".equalsIgnoreCase(cmbType.getSelectedItem().toString()))
	    {
		funCostCenterWiseSummaryJasperReport();
	    }
	    else if ("Detail".equalsIgnoreCase(cmbType.getSelectedItem().toString()))
	    {
		funCostCenterWiseDetailJasperReport();
	    }
	}

    }

    private void funGenerateExcelSheetOfReport() throws Exception
    {
	Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
	List<String> arrListTotal = new ArrayList<String>();
	List<String> arrHeaderList = new ArrayList<String>();
	StringBuilder sbSqlLive = new StringBuilder();
	StringBuilder sbSqlQFile = new StringBuilder();
	StringBuilder sbSqlModLive = new StringBuilder();
	StringBuilder sbSqlModQFile = new StringBuilder();
	double totalQty = 0;
	double totalAmt = 0;
	double totalSubTotal = 0;
	double totalDiscount = 0;
	String posCode = cmbPosCode.getSelectedItem().toString();
	StringBuilder sb = new StringBuilder(posCode);
	int len = posCode.length();
	int lastInd = sb.lastIndexOf(" ");
	posCode = sb.substring(lastInd + 1, len).toString();
//        String CostCenterCode = cmbCostCenterName.getSelectedItem().toString();
//        StringBuilder sb1 = new StringBuilder(CostCenterCode);
//        int len1 = CostCenterCode.length();
//        int lastInd1 = sb1.lastIndexOf(" ");
//        String CostcenterCode = sb1.substring(lastInd1 + 1, len1).toString();

	String costCenterCode = cmbCostCenterName.getSelectedItem().toString();
	StringBuilder sb1 = new StringBuilder(costCenterCode);
	int len1 = costCenterCode.length();
	int lastInd1 = sb1.lastIndexOf(" ");
	costCenterCode = sb1.substring(lastInd1 + 1, len1).toString();
	Date dt1 = dteFromDate.getDate();
	Date dt2 = dteToDate.getDate();
	SimpleDateFormat ddmmyyyyDateFormat = new SimpleDateFormat("dd-MM-yyyy");
	String fromDateToDisplay = ddmmyyyyDateFormat.format(dt1);
	String toDateToDisplay = ddmmyyyyDateFormat.format(dt2);
	fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());
	toDate = objUtility.funGetFromToDate(dteToDate.getDate());

	DecimalFormat decFormat = new DecimalFormat("0");
	DecimalFormat decFormatFor2Decimal = new DecimalFormat("0.00");
	if ("Detail".equals(cmbType.getSelectedItem()))
	{
	    sbSqlQFile.setLength(0);
	    sbSqlQFile.append("SELECT ifnull(f.strCostCenterName,''),a.strItemName,sum(a.dblQuantity), sum(a.dblAmount)"
		    + " ,b.strPOSCode,'" + clsGlobalVarClass.gUserCode + "',ifnull(d.strPriceMonday,0.00)"
		    + " ,sum(a.dblAmount)-sum(a.dblDiscountAmt),sum(a.dblDiscountAmt)\n"
		    + " FROM tblqbilldtl a inner join tblqbillhd b on a.strBillNo=b.strBillNo \n"
		    + " inner join tblmenuitempricingdtl d on a.strItemCode = d.strItemCode "
		    + " and (b.strposcode =d.strposcode  or d.strPosCode='All') and d.strHourlyPricing='NO' ");
	    if (clsGlobalVarClass.gAreaWisePricing.equals("Y"))
	    {
		sbSqlQFile.append("and b.strAreaCode= d.strAreaCode ");
	    }
	    sbSqlQFile.append(" inner join tblcostcentermaster f on d.strCostCenterCode=f.strCostCenterCode "
		    + " where date(b.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    if (!costCenterCode.equalsIgnoreCase("All"))
	    {
		sbSqlQFile.append(" and d.strCostCenterCode='" + costCenterCode + "' ");
	    }
	    if (!posCode.equals("All"))
	    {
		sbSqlQFile.append(" and b.strPOSCode='" + posCode + "' ");
	    }

	    sbSqlLive.setLength(0);
	    sbSqlLive.append("SELECT ifnull(f.strCostCenterName,''),a.strItemName,sum(a.dblQuantity), sum(a.dblAmount)"
		    + " ,b.strPOSCode,'" + clsGlobalVarClass.gUserCode + "',ifnull(d.strPriceMonday,0.00)"
		    + " ,sum(a.dblAmount)-sum(a.dblDiscountAmt),sum(a.dblDiscountAmt)\n"
		    + " FROM tblbilldtl a inner join tblbillhd b on a.strBillNo=b.strBillNo \n"
		    + " inner join tblmenuitempricingdtl d on a.strItemCode = d.strItemCode "
		    + " and (b.strposcode =d.strposcode  or d.strPosCode='All') and d.strHourlyPricing='NO' ");
	    if (clsGlobalVarClass.gAreaWisePricing.equals("Y"))
	    {
		sbSqlLive.append("and b.strAreaCode= d.strAreaCode ");
	    }
	    sbSqlLive.append(" inner join tblcostcentermaster f on d.strCostCenterCode=f.strCostCenterCode "
		    + " where date(b.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    if (!costCenterCode.equalsIgnoreCase("All"))
	    {
		sbSqlLive.append(" and d.strCostCenterCode='" + costCenterCode + "' ");
	    }
	    if (!posCode.equals("All"))
	    {
		sbSqlLive.append(" and b.strPOSCode='" + posCode + "' ");
		sbSqlQFile.append(" and b.strPOSCode='" + posCode + "' ");
	    }

	    sbSqlLive.append(" Group by f.strCostCenterCode,b.strPoscode, a.strItemCode,a.strItemName");
	    sbSqlQFile.append(" Group by f.strCostCenterCode,b.strPoscode, a.strItemCode,a.strItemName");
	    System.out.println("detail live=" + sbSqlQFile);
	    System.out.println("detail Q=" + sbSqlLive);

	    sbSqlModLive.setLength(0);
	    sbSqlModLive.append("SELECT ifnull(f.strCostCenterName,''),a.strModifierName,sum(a.dblQuantity)"
		    + " ,sum(a.dblAmount),b.strPOSCode,'" + clsGlobalVarClass.gUserCode + "',ifnull(d.strPriceMonday,0.00)"
		    + " ,sum(a.dblAmount)-sum(a.dblDiscAmt),sum(a.dblDiscAmt) "
		    + " FROM tblbillmodifierdtl a inner join tblbillhd b on a.strBillNo=b.strBillNo "
		    + " inner join tblmenuitempricingdtl d on LEFT(a.strItemCode,7)  = d.strItemCode "
		    + " and (b.strposcode =d.strposcode  or d.strPosCode='All') and d.strHourlyPricing='NO' ");
	    if (clsGlobalVarClass.gAreaWisePricing.equals("Y"))
	    {
		sbSqlModLive.append("and b.strAreaCode= d.strAreaCode ");
	    }
	    sbSqlModLive.append(" inner join tblcostcentermaster f on d.strCostCenterCode=f.strCostCenterCode "
		    + " where date(b.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		    + " and a.dblAmount>0 ");
	    if (!costCenterCode.equalsIgnoreCase("All"))
	    {
		sbSqlModLive.append(" and d.strCostCenterCode='" + costCenterCode + "' ");
	    }

	    sbSqlModQFile.setLength(0);
	    sbSqlModQFile.append("SELECT ifnull(f.strCostCenterName,''),a.strModifierName,sum(a.dblQuantity) "
		    + " ,sum(a.dblAmount),b.strPOSCode,'" + clsGlobalVarClass.gUserCode + "',ifnull(d.strPriceMonday,0.00)"
		    + " ,sum(a.dblAmount)-sum(a.dblDiscAmt),sum(a.dblDiscAmt) "
		    + " FROM tblqbillmodifierdtl a inner join tblqbillhd b on a.strBillNo=b.strBillNo "
		    + " inner join tblmenuitempricingdtl d on LEFT(a.strItemCode,7)  = d.strItemCode "
		    + " and (b.strposcode =d.strposcode  or d.strPosCode='All') and d.strHourlyPricing='NO' ");
	    if (clsGlobalVarClass.gAreaWisePricing.equals("Y"))
	    {
		sbSqlModQFile.append(" and b.strAreaCode= d.strAreaCode ");
	    }
	    sbSqlModQFile.append(" inner join tblcostcentermaster f on d.strCostCenterCode=f.strCostCenterCode "
		    + " where date(b.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		    + " and a.dblAmount>0 ");
	    if (!costCenterCode.equalsIgnoreCase("All"))
	    {
		sbSqlModQFile.append(" and d.strCostCenterCode='" + costCenterCode + "' ");
	    }
	    if (!posCode.equals("All"))
	    {
		sbSqlModQFile.append(" and b.strPOSCode='" + posCode + "' ");
		sbSqlModLive.append(" and b.strPOSCode='" + posCode + "' ");
	    }

	    sbSqlModQFile.append(" Group by f.strCostCenterCode,b.strPoscode, a.strItemCode, d.strItemName");
	    sbSqlModLive.append(" Group by f.strCostCenterCode,b.strPoscode, a.strItemCode, d.strItemName");

	    //Non chargable kots
	    StringBuilder sqlNonChargableKOts = new StringBuilder();

	    sqlNonChargableKOts.append("select ifnull(c.strCostCenterName,''),b.strItemName,sum(a.dblQuantity),0.00 as dblAmount,a.strPOSCode,'user',a.dblRate,0.00 as dblAmt_dblDisc,0.00 as dblDisc "
		    + "from tblnonchargablekot a,tblmenuitempricingdtl b,tblcostcentermaster c "
		    + "where date(a.dteNCKOTDate) between '" + fromDate + "' and '" + toDate + "' "
		    + "and a.strItemCode=b.strItemCode "
		    + "and (a.strposcode =b.strposcode  or b.strPosCode='All') "
		    + "and b.strCostCenterCode=c.strCostCenterCode and b.strHourlyPricing='NO' ");
	    if (!costCenterCode.equalsIgnoreCase("All"))
	    {
		sqlNonChargableKOts.append(" and b.strCostCenterCode='" + costCenterCode + "' ");
	    }
	    if (!posCode.equals("All"))
	    {
		sqlNonChargableKOts.append(" and a.strPOSCode='" + posCode + "' ");
	    }
	    sqlNonChargableKOts.append("group by a.strItemCode,b.strItemName,c.strCostCenterCode,c.strCostCenterName ");
	    sqlNonChargableKOts.append("order by a.strItemCode,b.strItemName,c.strCostCenterCode,c.strCostCenterName ");

	    //System.out.println(sqlModQFile);
	    clsGlobalVarClass.dbMysql.execute("truncate table tbltempsalesflash");
	    String insertTempSalesFlash = "insert into tbltempsalesflash(strcode,strname,dblquantity,dblamount,strposcode,struser,dblRate,dblsubtotal,dbldiscamt) ";
	    clsGlobalVarClass.dbMysql.execute(insertTempSalesFlash + "(" + sbSqlLive + ")");
	    clsGlobalVarClass.dbMysql.execute(insertTempSalesFlash + "(" + sbSqlQFile + ")");
	    clsGlobalVarClass.dbMysql.execute(insertTempSalesFlash + "(" + sbSqlModLive + ")");
	    clsGlobalVarClass.dbMysql.execute(insertTempSalesFlash + "(" + sbSqlModQFile + ")");
	    //insert non chargable kots sales
	    clsGlobalVarClass.dbMysql.execute(insertTempSalesFlash + "(" + sqlNonChargableKOts + ")");

	    String sql = " SELECT a.strcode, a.strname, b.strPosName, sum(a.dblquantity),sum(a.dblsubtotal)"
		    + " ,sum(a.dbldiscamt),sum(a.dblamount)  "
		    + " FROM tbltempsalesflash a,tblposmaster b "
		    + " where a.strposcode=b.strPosCode group by a.strcode, a.strname, a.strposcode ";
	    ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(sql);
	    int i = 1;
	    while (rs.next())
	    {
		List<String> arrListItem = new ArrayList<String>();
		arrListItem.add(rs.getString(1));
		arrListItem.add(rs.getString(2));
		arrListItem.add(rs.getString(3));
		arrListItem.add(decFormat.format(rs.getDouble(4)));
		arrListItem.add(decFormatFor2Decimal.format(rs.getDouble(7))); //Sub Total
		arrListItem.add(decFormatFor2Decimal.format(rs.getDouble(6))); // Dis Amt
		arrListItem.add(decFormatFor2Decimal.format(rs.getDouble(5))); //Net Total

		totalQty = totalQty + rs.getDouble(4);
		totalSubTotal = totalSubTotal + rs.getDouble(7); //Sub Total
		totalDiscount = totalDiscount + rs.getDouble(6); //Dis Amt
		totalAmt = totalAmt + rs.getDouble(5); //Net Total
		mapExcelItemDtl.put(i, arrListItem);
		i++;
	    }
	    rs.close();

	    arrListTotal.add(String.valueOf(decFormat.format(totalQty)) + "#" + "4");
	    arrListTotal.add(String.valueOf(decFormatFor2Decimal.format(totalSubTotal)) + "#" + "5"); //Sub Total
	    arrListTotal.add(String.valueOf(decFormatFor2Decimal.format(totalDiscount)) + "#" + "6"); //Dis Amt
	    arrListTotal.add(String.valueOf(decFormatFor2Decimal.format(totalAmt)) + "#" + "7");     // Net Total

	    arrHeaderList.add("Serial No");
	    arrHeaderList.add("Cost Center Name");
	    arrHeaderList.add("Item Name");
	    arrHeaderList.add("POS Name");
	    arrHeaderList.add("Qty");
	    arrHeaderList.add("SubTotal");
	    arrHeaderList.add("Discount");
	    arrHeaderList.add("Net Total");

	    List<String> arrparameterList = new ArrayList<String>();
	    arrparameterList.add("Cost Center Details Report");
	    arrparameterList.add("POS" + " : " + cmbPosCode.getSelectedItem().toString());
	    arrparameterList.add("FromDate" + " : " + fromDateToDisplay);
	    arrparameterList.add("ToDate" + " : " + toDateToDisplay);
	    arrparameterList.add("Cost Center" + " : " + cmbCostCenterName.getSelectedItem().toString());
	    arrparameterList.add(" ");

	    objUtility.funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "costCenterDetailsExcelSheet");
	}
	else
	{
	    funGenerateCostCenterWiseSummaryExcelSheetReport();
	}

	sbSqlLive = null;
	sbSqlQFile = null;
	sbSqlModLive = null;
	sbSqlModQFile = null;
    }

    private void funGenerateCostCenterWiseSummaryExcelSheetReport() throws Exception
    {
	Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
	List<String> arrListTotal = new ArrayList<String>();
	List<String> arrHeaderList = new ArrayList<String>();
	double totalQty = 0;
	double totalAmt = 0;
	double totalSubTotal = 0;
	double totalDiscount = 0;
	fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());

	toDate = objUtility.funGetFromToDate(dteToDate.getDate());
	Date dt1 = dteFromDate.getDate();
	Date dt2 = dteToDate.getDate();
	SimpleDateFormat ddmmyyyyDateFormat = new SimpleDateFormat("dd-MM-yyyy");
	String fromDateToDisplay = ddmmyyyyDateFormat.format(dt1);
	String toDateToDisplay = ddmmyyyyDateFormat.format(dt2);

	String posCode = cmbPosCode.getSelectedItem().toString();

	StringBuilder sb = new StringBuilder(posCode);
	int len = posCode.length();
	int lastInd = sb.lastIndexOf(" ");
	posCode = sb.substring(lastInd + 1, len).toString();
	String posName = sb.substring(0, lastInd - 1).toString();

	String costCenterCode = cmbCostCenterName.getSelectedItem().toString();
	StringBuilder sb1 = new StringBuilder(costCenterCode);
	int len1 = costCenterCode.length();
	int lastInd1 = sb1.lastIndexOf(" ");
	costCenterCode = sb1.substring(lastInd1 + 1, len1).toString();

	StringBuilder sbSqlLive = new StringBuilder();
	StringBuilder sbSqlQFile = new StringBuilder();
	StringBuilder sbSqlModLive = new StringBuilder();
	StringBuilder sbSqlModQFile = new StringBuilder();
	StringBuilder sbSqlFilters = new StringBuilder();

	try
	{
	    sbSqlLive.setLength(0);
	    sbSqlQFile.setLength(0);
	    sbSqlFilters.setLength(0);

	    // Live Sql
	    sbSqlLive.append("SELECT e.strPosCode,e.strPOSName,ifnull(a.strCostCenterCode,'ND')strCostCenterCode,ifnull(a.strCostCenterName,'ND')strCostCenterName "
		    + ",sum(c.dblAmount)dblSubTotal,sum(c.dblDiscountAmt) dblDiscountAmt,sum( c.dblAmount )-sum(c.dblDiscountAmt)dblSalesAmount "
		    + " from tblbilldtl c,tblbillhd d,tblposmaster e ,tblmenuitempricingdtl b,tblcostcentermaster a "
		    + " where date( d.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
		    + " and c.strBillNo = d.strBillNo and d.strPOSCode = e.strPOSCode  and b.strItemCode = c.strItemCode "
		    + " and (b.strposcode =d.strposcode  or b.strPosCode='All') and a.strCostCenterCode = b.strCostCenterCode "
		    + " and c.strClientCode=d.strClientCode and b.strHourlyPricing='NO' ");
	    if (clsGlobalVarClass.gAreaWisePricing.equals("Y"))
	    {
		sbSqlLive.append(" and d.strAreaCode=b.strAreaCode ");
	    }

	    // QFile Sql    
	    sbSqlQFile.append("SELECT e.strPosCode,e.strPOSName,ifnull(a.strCostCenterCode,'ND')strCostCenterCode,ifnull(a.strCostCenterName,'ND')strCostCenterName "
		    + ",sum(c.dblAmount)dblSubTotal,sum(c.dblDiscountAmt) dblDiscountAmt,sum( c.dblAmount )-sum(c.dblDiscountAmt)dblSalesAmount "
		    + " from tblqbilldtl c,tblqbillhd d,tblposmaster e ,tblmenuitempricingdtl b,tblcostcentermaster a "
		    + " where date( d.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
		    + " and c.strBillNo = d.strBillNo "
		    + " and d.strPOSCode = e.strPOSCode "
		    + " and b.strItemCode = c.strItemCode "
		    + " and (b.strposcode =d.strposcode  or b.strPosCode='All') "
		    + " and a.strCostCenterCode = b.strCostCenterCode "
		    + " and c.strClientCode=d.strClientCode and b.strHourlyPricing='NO' ");
	    if (clsGlobalVarClass.gAreaWisePricing.equals("Y"))
	    {
		sbSqlQFile.append(" and d.strAreaCode=b.strAreaCode ");
	    }

	    String sqlModLive = "SELECT e.strPosCode,e.strPOSName,ifnull(a.strCostCenterCode,'ND')strCostCenterCode,ifnull(a.strCostCenterName,'ND')strCostCenterName "
		    + ",sum(c.dblAmount)dblSubTotal,sum(c.dblDiscAmt) dblDiscountAmt,sum( c.dblAmount )-sum(c.dblDiscAmt)dblSalesAmount  "
		    + " from tblbillmodifierdtl c,tblbillhd d,tblposmaster e ,tblmenuitempricingdtl b,tblcostcentermaster a "
		    + " where date( d.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
		    + " and c.strBillNo = d.strBillNo "
		    + " and d.strPOSCode = e.strPOSCode "
		    + " and b.strItemCode = left(c.strItemCode,7) "
		    + " and (b.strposcode =d.strposcode  or b.strPosCode='All') "
		    + " and a.strCostCenterCode = b.strCostCenterCode "
		    + " and c.dblAmount>0 "
		    + " and c.strClientCode=d.strClientCode and b.strHourlyPricing='NO' ";
	    if (clsGlobalVarClass.gAreaWisePricing.equals("Y"))
	    {
		sqlModLive += " and d.strAreaCode=b.strAreaCode ";
	    }

	    String sqlModQFile = "SELECT e.strPosCode,e.strPOSName,ifnull(a.strCostCenterCode,'ND')strCostCenterCode,ifnull(a.strCostCenterName,'ND')strCostCenterName "
		    + ",sum(c.dblAmount)dblSubTotal,sum(c.dblDiscAmt) dblDiscountAmt,sum( c.dblAmount )-sum(c.dblDiscAmt)dblSalesAmount  "
		    + " from tblqbillmodifierdtl c,tblqbillhd d,tblposmaster e ,tblmenuitempricingdtl b,tblcostcentermaster a "
		    + " where date( d.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
		    + " and c.strBillNo = d.strBillNo "
		    + " and d.strPOSCode = e.strPOSCode "
		    + " and b.strItemCode = left(c.strItemCode,7) "
		    + " and (b.strposcode =d.strposcode  or b.strPosCode='All') "
		    + " and a.strCostCenterCode = b.strCostCenterCode "
		    + " and c.dblAmount>0 "
		    + " and c.strClientCode=d.strClientCode and b.strHourlyPricing='NO' ";
	    if (clsGlobalVarClass.gAreaWisePricing.equals("Y"))
	    {
		sqlModQFile += " and d.strAreaCode=b.strAreaCode ";
	    }

	    if (!posCode.equals("All"))
	    {
		sbSqlFilters.append(" AND b.strPOSCode = '" + posCode + "' ");
	    }
	    if (clsGlobalVarClass.gEnableShiftYN && (!cmbShiftNo.getSelectedItem().toString().equalsIgnoreCase("All")))
	    {
		sbSqlFilters.append(" and b.intShiftCode ='" + cmbShiftNo.getSelectedItem().toString() + "' ");
	    }
	    if (!costCenterCode.equalsIgnoreCase("All"))
	    {
		sbSqlFilters.append(" and a.strCostCenterCode='" + costCenterCode + "' ");
	    }
	    sbSqlFilters.append("GROUP BY e.strPOSName,b.strCostCenterCode,a.strCostCenterName "
		    + "order BY e.strPOSName,b.strCostCenterCode,a.strCostCenterName ");

	    sbSqlLive.append(sbSqlFilters);
	    sbSqlQFile.append(sbSqlFilters);
	    sqlModLive = sqlModLive + " " + sbSqlFilters.toString();
	    sqlModQFile = sqlModQFile + " " + sbSqlFilters.toString();

	    List<clsCostCenterBean> listOfCostCenterDtl = new LinkedList<>();

	    //live data
	    ResultSet rsCostCenterData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
	    while (rsCostCenterData.next())
	    {
		clsCostCenterBean obj = new clsCostCenterBean();

		obj.setStrPOSCode(rsCostCenterData.getString(1));//posCode
		obj.setStrPOSName(rsCostCenterData.getString(2));//posName
		obj.setStrCostCenterCode(rsCostCenterData.getString(3));//costCenterCode
		obj.setStrCostCenterName(rsCostCenterData.getString(4));//costCenterName
		obj.setDblSubTotal(rsCostCenterData.getDouble(5));//subTotal
		obj.setDblDiscAmount(rsCostCenterData.getDouble(6));//discount
		obj.setDblSalesAmount(rsCostCenterData.getDouble(7));//salesAmount

		listOfCostCenterDtl.add(obj);

	    }
	    rsCostCenterData.close();
	    //live modifier data
	    rsCostCenterData = clsGlobalVarClass.dbMysql.executeResultSet(sqlModLive.toString());
	    while (rsCostCenterData.next())
	    {
		clsCostCenterBean obj = new clsCostCenterBean();

		obj.setStrPOSCode(rsCostCenterData.getString(1));//posCode
		obj.setStrPOSName(rsCostCenterData.getString(2));//posName
		obj.setStrCostCenterCode(rsCostCenterData.getString(3));//costCenterCode
		obj.setStrCostCenterName(rsCostCenterData.getString(4));//costCenterName
		obj.setDblSubTotal(rsCostCenterData.getDouble(5));//subTotal
		obj.setDblDiscAmount(rsCostCenterData.getDouble(6));//discount
		obj.setDblSalesAmount(rsCostCenterData.getDouble(7));//salesAmount

		listOfCostCenterDtl.add(obj);

	    }
	    rsCostCenterData.close();

	    //Q data
	    rsCostCenterData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFile.toString());
	    while (rsCostCenterData.next())
	    {
		clsCostCenterBean obj = new clsCostCenterBean();

		obj.setStrPOSCode(rsCostCenterData.getString(1));//posCode
		obj.setStrPOSName(rsCostCenterData.getString(2));//posName
		obj.setStrCostCenterCode(rsCostCenterData.getString(3));//costCenterCode
		obj.setStrCostCenterName(rsCostCenterData.getString(4));//costCenterName
		obj.setDblSubTotal(rsCostCenterData.getDouble(5));//subTotal
		obj.setDblDiscAmount(rsCostCenterData.getDouble(6));//discount
		obj.setDblSalesAmount(rsCostCenterData.getDouble(7));//salesAmount

		listOfCostCenterDtl.add(obj);

	    }
	    rsCostCenterData.close();
	    //Q modifier data
	    rsCostCenterData = clsGlobalVarClass.dbMysql.executeResultSet(sqlModQFile.toString());
	    while (rsCostCenterData.next())
	    {
		clsCostCenterBean obj = new clsCostCenterBean();

		obj.setStrPOSCode(rsCostCenterData.getString(1));//posCode
		obj.setStrPOSName(rsCostCenterData.getString(2));//posName
		obj.setStrCostCenterCode(rsCostCenterData.getString(3));//costCenterCode
		obj.setStrCostCenterName(rsCostCenterData.getString(4));//costCenterName
		obj.setDblSubTotal(rsCostCenterData.getDouble(5));//subTotal
		obj.setDblDiscAmount(rsCostCenterData.getDouble(6));//discount
		obj.setDblSalesAmount(rsCostCenterData.getDouble(7));//salesAmount

		listOfCostCenterDtl.add(obj);

	    }
	    rsCostCenterData.close();

	    Comparator<clsCostCenterBean> posCodeComparator = new Comparator<clsCostCenterBean>()
	    {

		@Override
		public int compare(clsCostCenterBean o1, clsCostCenterBean o2)
		{
		    return o1.getStrPOSCode().compareTo(o2.getStrPOSCode());
		}
	    };
	    Comparator<clsCostCenterBean> costCenterCodeComparator = new Comparator<clsCostCenterBean>()
	    {

		@Override
		public int compare(clsCostCenterBean o1, clsCostCenterBean o2)
		{
		    return o1.getStrCostCenterCode().compareTo(o2.getStrCostCenterCode());
		}
	    };

	    Collections.sort(listOfCostCenterDtl, new clsCostCenterComparator(posCodeComparator, costCenterCodeComparator));

	    int i = 1;
	    int newKey = 0;
	    DecimalFormat decFormatfor2Decimal = new DecimalFormat("0.00");
	    for (int cnt = 0; cnt < listOfCostCenterDtl.size(); cnt++)
	    {
		boolean flgFound = false;
		clsCostCenterBean obj = listOfCostCenterDtl.get(cnt);
		List<String> arrListItem = new ArrayList<String>();
		if (mapExcelItemDtl.size() > 0)
		{
		    for (Map.Entry<Integer, List<String>> entry : mapExcelItemDtl.entrySet())
		    {
			List<String> list = mapExcelItemDtl.get(entry.getKey());
			newKey = entry.getKey();
			if (list.get(0).equals(obj.getStrCostCenterName()) && list.get(1).equals(obj.getStrPOSName()))
			{
			    arrListItem.add(obj.getStrCostCenterName());//costCenterName
			    arrListItem.add(obj.getStrPOSName());//posName
			    double subAmt = obj.getDblSubTotal() + Double.parseDouble(list.get(2));
			    double disAmt = obj.getDblDiscAmount() + Double.parseDouble(list.get(3));
			    double saleAmt = obj.getDblSalesAmount() + Double.parseDouble(list.get(4));
			    arrListItem.add(String.valueOf(decFormatfor2Decimal.format((subAmt))));//subTotal
			    arrListItem.add(String.valueOf(decFormatfor2Decimal.format((disAmt))));//discount
			    arrListItem.add(String.valueOf(decFormatfor2Decimal.format((saleAmt))));//salesAmount  
			    flgFound = true;
			}
		    }
		}
		if (flgFound)
		{
		    mapExcelItemDtl.remove(newKey);
		    mapExcelItemDtl.put(newKey, arrListItem);
		}
		else
		{
		    arrListItem.add(obj.getStrCostCenterName());//costCenterName
		    arrListItem.add(obj.getStrPOSName());//posName
		    arrListItem.add(String.valueOf(decFormatfor2Decimal.format((obj.getDblSubTotal()))));//subTotal
		    arrListItem.add(String.valueOf(decFormatfor2Decimal.format((obj.getDblDiscAmount()))));//discount
		    arrListItem.add(String.valueOf(decFormatfor2Decimal.format((obj.getDblSalesAmount()))));//salesAmount
		    mapExcelItemDtl.put(i, arrListItem);
		    i++;
		}
		totalSubTotal = totalSubTotal + obj.getDblSubTotal();
		totalDiscount = totalDiscount + obj.getDblDiscAmount();
		totalAmt = totalAmt + obj.getDblSalesAmount();

	    }
	    arrListTotal.add(String.valueOf(decFormatfor2Decimal.format((totalSubTotal))) + "#" + "3");
	    arrListTotal.add(String.valueOf(decFormatfor2Decimal.format((totalDiscount))) + "#" + "4");
	    arrListTotal.add(String.valueOf(decFormatfor2Decimal.format((totalAmt))) + "#" + "5");

	    arrHeaderList.add("Serial No");
	    arrHeaderList.add("Cost Center Name");
	    arrHeaderList.add("POS Name");
	    arrHeaderList.add("SubTotal");
	    arrHeaderList.add("Discount");
	    arrHeaderList.add("Grand Total");

	    List<String> arrparameterList = new ArrayList<String>();
	    arrparameterList.add("Cost Center Summary Report");
	    arrparameterList.add("POS" + " : " + posName);
	    arrparameterList.add("FromDate" + " : " + fromDateToDisplay);
	    arrparameterList.add("ToDate" + " : " + toDateToDisplay);
	    arrparameterList.add("Cost Center" + " : " + cmbCostCenterName.getSelectedItem().toString());
	    arrparameterList.add(" ");

	    objUtility.funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "costCenterSummaryExcelSheet");

	}

	catch (Exception ex)
	{
	    ex.printStackTrace();
	}

	finally
	{
	    sbSqlLive = null;
	    sbSqlQFile = null;
	    sbSqlModLive = null;
	    sbSqlModQFile = null;
	    sbSqlFilters = null;
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
     * Draw Under Line
     *
     * @param textToPrint
     * @param pw
     * @return
     */
    private int funDrawUnderLine(String textToPrint, PrintWriter pw)
    {
	for (int cnt = 0; cnt < textToPrint.length(); cnt++)
	{
	    pw.print("-");
	}
	pw.println();
	return 1;
    }

    /**
     * Get Calender Date
     *
     * @throws Exception
     */
    private String funGetCalenderDate(String type)
    {
	String date = "";
	if (type.equalsIgnoreCase("From"))
	{
	    date = dteFromDate.getDate().getDate() + "-" + (dteFromDate.getDate().getMonth() + 1) + "-" + (dteFromDate.getDate().getYear() + 1900);
	}
	else
	{
	    date = dteToDate.getDate().getDate() + "-" + (dteToDate.getDate().getMonth() + 1) + "-" + (dteToDate.getDate().getYear() + 1900);
	}
	return date;
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
	pw.print(decFormat.format(Double.parseDouble(textToPrint)));
	return 1;
    }

    /**
     * Print Text Value With Alignment
     *
     * @param align
     * @param textToPrint
     * @param totalLength
     * @param pw
     * @return
     */
    private int funPrintTextValueWithAlignment(String align, String textToPrint, int totalLength, PrintWriter pw)
    {

	pw.print(String.format("%-27s", textToPrint));
	return 1;
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
	    new frmShowTextFile(data, reportName, file, clsGlobalVarClass.gBillPrintPrinterPort).setVisible(true);
	    fread.close();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funCostCenterWiseTextReport() throws Exception
    {
	int count = 0;
	funCreateTempFolder();
	String filePath = System.getProperty("user.dir");
	File file = new File(filePath + File.separator + "Temp" + File.separator + "Temp_ItemWiseReport.txt");
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
	funPrintBlankLines("Item Wise Report", pw);

	pw.println();
	pw.println("POS  :" + cmbPosCode.getSelectedItem().toString());
	pw.println("From :" + funGetCalenderDate("From") + "  To :" + funGetCalenderDate("To"));

	pw.println("---------------------------------------");
	pw.println("Cost Center Name");
	funDrawUnderLine("Cost Center Name", pw);
	pw.println("Menu Item Name");
	pw.println("       Rate         Qty        Amount");
	pw.println("---------------------------------------");

	int cnt = 0;
	String costCenterName = "";
	double grandTotal = 0.00, quantityTotal = 0.00, costCenterTotal = 0.00;
	String fromDate = (dteFromDate.getDate().getYear() + 1900) + "-" + (dteFromDate.getDate().getMonth() + 1) + "-" + (dteFromDate.getDate().getDate());
	String toDate = (dteToDate.getDate().getYear() + 1900) + "-" + (dteToDate.getDate().getMonth() + 1) + "-" + (dteToDate.getDate().getDate());

	StringBuilder sbSqlLive = new StringBuilder();
	StringBuilder sbSqlQFile = new StringBuilder();
	StringBuilder sbSqlModLive = new StringBuilder();
	StringBuilder sbSqlModQFile = new StringBuilder();

	String posCode = cmbPosCode.getSelectedItem().toString();
	StringBuilder sb = new StringBuilder(posCode);
	int len = posCode.length();
	int lastInd = sb.lastIndexOf(" ");
	String pos = sb.substring(lastInd + 1, len).toString();
	String CostCenterCode = cmbCostCenterName.getSelectedItem().toString();
	StringBuilder sb1 = new StringBuilder(CostCenterCode);
	int len1 = CostCenterCode.length();
	int lastInd1 = sb1.lastIndexOf(" ");
	String CostcenterCode = sb1.substring(lastInd1 + 1, len1).toString();
	fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());

	toDate = objUtility.funGetFromToDate(dteToDate.getDate());

	sbSqlQFile.setLength(0);
	sbSqlQFile.append("SELECT ifnull(f.strCostCenterName,''),a.strItemName,sum(a.dblQuantity),sum(a.dblAmount)-sum(a.dblDiscountAmt) "
		+ " ,b.strPOSCode,'" + clsGlobalVarClass.gUserCode + "',ifnull(d.strPriceMonday,0.00) "
		+ " FROM tblqbilldtl a inner join tblqbillhd b on a.strBillNo=b.strBillNo "
		+ " inner join tblmenuitempricingdtl d on a.strItemCode = d.strItemCode "
		+ " and (b.strposcode =d.strposcode  or d.strPosCode='All') and d.strHourlyPricing='NO' ");
	if (clsGlobalVarClass.gAreaWisePricing.equals("Y"))
	{
	    sbSqlQFile.append("and b.strAreaCode= d.strAreaCode ");
	}
	sbSqlQFile.append(" inner join tblcostcentermaster f on d.strCostCenterCode=f.strCostCenterCode "
		+ " where date(b.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	if (!CostCenterCode.equalsIgnoreCase("All"))
	{
	    sbSqlQFile.append(" and d.strCostCenterCode='" + CostcenterCode + "' ");
	}
	if (!pos.equals("All"))
	{
	    sbSqlQFile.append(" and b.strPOSCode='" + pos + "' ");
	}

	sbSqlLive.setLength(0);
	sbSqlLive.append("SELECT ifnull(f.strCostCenterName,''),a.strItemName,sum(a.dblQuantity),sum(a.dblAmount)-sum(a.dblDiscountAmt) "
		+ " ,b.strPOSCode,'" + clsGlobalVarClass.gUserCode + "',ifnull(d.strPriceMonday,0.00) "
		+ " FROM tblbilldtl a inner join tblbillhd b on a.strBillNo=b.strBillNo "
		+ " inner join tblmenuitempricingdtl d on a.strItemCode = d.strItemCode "
		+ " and (b.strposcode =d.strposcode  or d.strPosCode='All') and d.strHourlyPricing='NO' ");
	if (clsGlobalVarClass.gAreaWisePricing.equals("Y"))
	{
	    sbSqlLive.append("and b.strAreaCode= d.strAreaCode ");
	}
	sbSqlLive.append(" inner join tblcostcentermaster f on d.strCostCenterCode=f.strCostCenterCode "
		+ " where date(b.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	if (!CostCenterCode.equalsIgnoreCase("All"))
	{
	    sbSqlLive.append(" and d.strCostCenterCode='" + CostcenterCode + "' ");
	}
	if (!pos.equals("All"))
	{
	    sbSqlLive.append(" and b.strPOSCode='" + pos + "' ");
	}

	sbSqlModQFile.setLength(0);
	sbSqlModQFile.append("SELECT ifnull(f.strCostCenterName,''),a.strModifierName,sum(a.dblQuantity),sum(a.dblAmount)-sum(a.dblDiscAmt) "
		+ " ,b.strPOSCode,'" + clsGlobalVarClass.gUserCode + "',ifnull(d.strPriceMonday,0.00) "
		+ " FROM tblqbillmodifierdtl a inner join tblqbillhd b on a.strBillNo=b.strBillNo "
		+ " inner join tblmenuitempricingdtl d on left(a.strItemCode,7) = d.strItemCode "
		+ " and (b.strposcode =d.strposcode  or d.strPosCode='All') and d.strHourlyPricing='NO' ");
	if (clsGlobalVarClass.gAreaWisePricing.equals("Y"))
	{
	    sbSqlModQFile.append("and b.strAreaCode= d.strAreaCode ");
	}
	sbSqlModQFile.append(" inner join tblcostcentermaster f on d.strCostCenterCode=f.strCostCenterCode "
		+ " where date(b.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		+ " and a.dblAmount>0 ");
	if (!CostCenterCode.equalsIgnoreCase("All"))
	{
	    sbSqlModQFile.append(" and d.strCostCenterCode='" + CostcenterCode + "' ");
	}
	if (!pos.equals("All"))
	{
	    sbSqlModQFile.append(" and b.strPOSCode='" + pos + "' ");
	}

	sbSqlModLive.setLength(0);
	sbSqlModLive.append("SELECT ifnull(f.strCostCenterName,''),a.strModifierName,sum(a.dblQuantity),sum(a.dblAmount)-sum(a.dblDiscAmt) "
		+ " ,b.strPOSCode,'" + clsGlobalVarClass.gUserCode + "',ifnull(d.strPriceMonday,0.00) "
		+ " FROM tblbillmodifierdtl a inner join tblbillhd b on a.strBillNo=b.strBillNo "
		+ " inner join tblmenuitempricingdtl d on left(a.strItemCode,7) = d.strItemCode "
		+ " and (b.strposcode =d.strposcode  or d.strPosCode='All') and d.strHourlyPricing='NO' ");
	if (clsGlobalVarClass.gAreaWisePricing.equals("Y"))
	{
	    sbSqlModLive.append("and b.strAreaCode= d.strAreaCode ");
	}
	sbSqlModLive.append(" inner join tblcostcentermaster f on d.strCostCenterCode=f.strCostCenterCode "
		+ " where date(b.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		+ " and a.dblAmount>0 ");
	if (!CostCenterCode.equalsIgnoreCase("All"))
	{
	    sbSqlModLive.append(" and d.strCostCenterCode='" + CostcenterCode + "' ");
	}
	if (!pos.equals("All"))
	{
	    sbSqlModLive.append(" and b.strPOSCode='" + pos + "' ");
	}

	sbSqlLive.append(" Group by f.strCostCenterCode,b.strPoscode, a.strItemCode,a.strItemName");
	sbSqlQFile.append(" Group by f.strCostCenterCode,b.strPoscode, a.strItemCode,a.strItemName");

	sbSqlModLive.append(" Group by f.strCostCenterCode,b.strPoscode, a.strItemCode,a.strModifierName");
	sbSqlModQFile.append(" Group by f.strCostCenterCode,b.strPoscode, a.strItemCode,a.strModifierName");

	clsGlobalVarClass.dbMysql.execute("truncate table tbltempsalesflash");
	String insertTempSalesFlash = "insert into tbltempsalesflash(strcode,strname,dblquantity,dblamount,strposcode,struser,dblRate)";

	clsGlobalVarClass.dbMysql.execute(insertTempSalesFlash + "(" + sbSqlQFile + ")");
	clsGlobalVarClass.dbMysql.execute(insertTempSalesFlash + "(" + sbSqlLive + ")");

	clsGlobalVarClass.dbMysql.execute(insertTempSalesFlash + "(" + sbSqlModLive + ")");
	clsGlobalVarClass.dbMysql.execute(insertTempSalesFlash + "(" + sbSqlModQFile + ")");

	String sql = "SELECT strname,dblRate,sum(dblquantity),sum(dblamount),strcode\n"
		+ " FROM tbltempsalesflash "
		+ " group by strcode,strname";
	System.out.println(sql);
	ResultSet rsItemWise = clsGlobalVarClass.dbMysql.executeResultSet(sql);
	while (rsItemWise.next())
	{
	    count++;
	    if (!costCenterName.equals(rsItemWise.getString(5)))
	    {
		if (cnt > 0)
		{
		    pw.println("---------------------------------------");
		    pw.print(costCenterName + " TOTAL");
		    funPrintTextWithAlignment("right", String.valueOf(costCenterTotal), 26, pw);
		    pw.println();
		    pw.println("---------------------------------------");
		    pw.println();
		}
		pw.println();
		pw.println(rsItemWise.getString(5));
		funDrawUnderLine(rsItemWise.getString(5), pw);
		costCenterName = rsItemWise.getString(5);
		costCenterTotal = 0.00;
	    }

	    pw.println(rsItemWise.getString(1));
	    funPrintTextWithAlignment("right", rsItemWise.getString(2), 12, pw);
	    funPrintTextWithAlignment("right", rsItemWise.getString(3), 12, pw);
	    funPrintTextWithAlignment("right", rsItemWise.getString(4), 13, pw);
	    pw.println();
	    quantityTotal += Double.parseDouble(rsItemWise.getString(3));
	    grandTotal += Double.parseDouble(rsItemWise.getString(4));
	    costCenterTotal += Double.parseDouble(rsItemWise.getString(4));
	    cnt++;

	    if (rsItemWise.isLast())
	    {
		pw.println("---------------------------------------");
		pw.print(costCenterName + " TOTAL");
		funPrintTextWithAlignment("right", String.valueOf(costCenterTotal), 26, pw);
		pw.println();
	    }
	}
	rsItemWise.close();

	pw.println("---------------------------------------");
	pw.print("GRAND TOTAL");
	funPrintTextWithAlignment("right", String.valueOf((quantityTotal)), 13, pw);
	funPrintTextWithAlignment("right", String.valueOf((grandTotal)), 11, pw);
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

    private void funFillShiftCombo()
    {
	try
	{
	    String posCode = cmbPosCode.getSelectedItem().toString();
	    StringBuilder sb = new StringBuilder(posCode);
	    int len = posCode.length();
	    int lastInd = sb.lastIndexOf(" ");
	    String pos = sb.substring(lastInd + 1, len).toString();

	    if (clsGlobalVarClass.gEnableShiftYN)
	    {
		lblShiftNo.setText("Shift                :    ");
		lblShiftNo.setVisible(true);
		cmbShiftNo.setVisible(true);

		StringBuilder sqlShift = new StringBuilder();
		if (posCode.equalsIgnoreCase("All"))
		{
		    sqlShift.append("select max(a.intShiftCode) from tblshiftmaster a group by a.intShiftCode ");
		}
		else
		{
		    sqlShift.append("select a.intShiftCode from tblshiftmaster a where a.strPOSCode='" + pos + "' ");
		}
		ResultSet rsShifts = clsGlobalVarClass.dbMysql.executeResultSet(sqlShift.toString());
		cmbShiftNo.removeAllItems();

		cmbShiftNo.addItem("All");

		while (rsShifts.next())
		{

		    cmbShiftNo.addItem(rsShifts.getString(1));
		}

	    }
	    else
	    {
		lblShiftNo.setVisible(false);
		cmbShiftNo.setVisible(false);
		lblShiftNo.setText("");

	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        pnlheader = new javax.swing.JPanel();
        lblProductName = new javax.swing.JLabel();
        lblModuleName = new javax.swing.JLabel();
        lblfromName = new javax.swing.JLabel();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 32767));
        filler5 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        lblPosName = new javax.swing.JLabel();
        filler6 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        lblUserCode = new javax.swing.JLabel();
        lblDate = new javax.swing.JLabel();
        lblHOSign = new javax.swing.JLabel();
        pnlBackGround = new JPanel()
        {
            public void paintComponent(Graphics g)
            {
                Image img = Toolkit.getDefaultToolkit().getImage(
                    getClass().getResource("/com/POSReport/images/imgBGJPOS.png"));
                g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), this);
            }
        };

        ;
        pnlMain = new javax.swing.JPanel();
        lblCostCenterReport = new javax.swing.JLabel();
        cmbPosCode = new javax.swing.JComboBox();
        lblposName = new javax.swing.JLabel();
        lblCostCenterName = new javax.swing.JLabel();
        cmbCostCenterName = new javax.swing.JComboBox();
        cmbType = new javax.swing.JComboBox();
        dteFromDate = new com.toedter.calendar.JDateChooser();
        dteToDate = new com.toedter.calendar.JDateChooser();
        lblToDate = new javax.swing.JLabel();
        lblFromDate = new javax.swing.JLabel();
        lbltype = new javax.swing.JLabel();
        btnBack = new javax.swing.JButton();
        btnView = new javax.swing.JButton();
        lblReportType = new javax.swing.JLabel();
        cmbReportType = new javax.swing.JComboBox();
        lblShiftNo = new javax.swing.JLabel();
        cmbShiftNo = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setExtendedState(MAXIMIZED_BOTH);
        setMinimumSize(new java.awt.Dimension(800, 600));
        setUndecorated(true);
        addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosed(java.awt.event.WindowEvent evt)
            {
                formWindowClosed(evt);
            }
            public void windowClosing(java.awt.event.WindowEvent evt)
            {
                formWindowClosing(evt);
            }
        });

        pnlheader.setBackground(new java.awt.Color(69, 164, 238));
        pnlheader.setLayout(new javax.swing.BoxLayout(pnlheader, javax.swing.BoxLayout.LINE_AXIS));

        lblProductName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblProductName.setForeground(new java.awt.Color(255, 255, 255));
        lblProductName.setText("SPOS - ");
        pnlheader.add(lblProductName);

        lblModuleName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblModuleName.setForeground(new java.awt.Color(255, 255, 255));
        pnlheader.add(lblModuleName);

        lblfromName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblfromName.setForeground(new java.awt.Color(255, 255, 255));
        lblfromName.setText("-Cost Center Report");
        pnlheader.add(lblfromName);
        pnlheader.add(filler4);
        pnlheader.add(filler5);

        lblPosName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblPosName.setForeground(new java.awt.Color(255, 255, 255));
        lblPosName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblPosName.setMaximumSize(new java.awt.Dimension(321, 30));
        lblPosName.setMinimumSize(new java.awt.Dimension(321, 30));
        lblPosName.setPreferredSize(new java.awt.Dimension(321, 30));
        pnlheader.add(lblPosName);
        pnlheader.add(filler6);

        lblUserCode.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblUserCode.setForeground(new java.awt.Color(255, 255, 255));
        lblUserCode.setMaximumSize(new java.awt.Dimension(90, 30));
        lblUserCode.setMinimumSize(new java.awt.Dimension(90, 30));
        lblUserCode.setPreferredSize(new java.awt.Dimension(90, 30));
        pnlheader.add(lblUserCode);

        lblDate.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblDate.setForeground(new java.awt.Color(255, 255, 255));
        lblDate.setMaximumSize(new java.awt.Dimension(192, 30));
        lblDate.setMinimumSize(new java.awt.Dimension(192, 30));
        lblDate.setPreferredSize(new java.awt.Dimension(192, 30));
        pnlheader.add(lblDate);

        lblHOSign.setMaximumSize(new java.awt.Dimension(34, 30));
        lblHOSign.setMinimumSize(new java.awt.Dimension(34, 30));
        lblHOSign.setPreferredSize(new java.awt.Dimension(34, 30));
        pnlheader.add(lblHOSign);

        getContentPane().add(pnlheader, java.awt.BorderLayout.PAGE_START);

        pnlBackGround.setOpaque(false);
        pnlBackGround.setLayout(new java.awt.GridBagLayout());

        pnlMain.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, new java.awt.Color(204, 204, 204), new java.awt.Color(204, 204, 204), new java.awt.Color(153, 153, 153), new java.awt.Color(153, 153, 153)));
        pnlMain.setMinimumSize(new java.awt.Dimension(800, 570));
        pnlMain.setOpaque(false);

        lblCostCenterReport.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        lblCostCenterReport.setText("Cost Center Report");

        cmbPosCode.setBackground(new java.awt.Color(51, 102, 255));
        cmbPosCode.setForeground(new java.awt.Color(255, 255, 255));
        cmbPosCode.setToolTipText("Select POS");
        cmbPosCode.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                cmbPosCodeActionPerformed(evt);
            }
        });

        lblposName.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblposName.setText("POS Name             :");

        lblCostCenterName.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblCostCenterName.setText("Cost Center Name :");

        cmbCostCenterName.setBackground(new java.awt.Color(51, 102, 255));
        cmbCostCenterName.setForeground(new java.awt.Color(255, 255, 255));
        cmbCostCenterName.setToolTipText("Select Cost Center");
        cmbCostCenterName.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                cmbCostCenterNameActionPerformed(evt);
            }
        });

        cmbType.setBackground(new java.awt.Color(51, 102, 255));
        cmbType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Summary", "Detail" }));
        cmbType.setToolTipText("Select  View Type");
        cmbType.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                cmbTypeActionPerformed(evt);
            }
        });

        dteFromDate.setToolTipText("Select From Date");

        dteToDate.setToolTipText("Select To Date");

        lblToDate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblToDate.setText("To Date                 :");

        lblFromDate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblFromDate.setText("From Date             :");

        lbltype.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lbltype.setText("Type                    :");

        btnBack.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnBack.setForeground(new java.awt.Color(255, 255, 255));
        btnBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnBack.setText("CLOSE");
        btnBack.setToolTipText("Close Window");
        btnBack.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnBack.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn2.png"))); // NOI18N
        btnBack.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnBackMouseClicked(evt);
            }
        });
        btnBack.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnBackActionPerformed(evt);
            }
        });

        btnView.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnView.setForeground(new java.awt.Color(255, 255, 255));
        btnView.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnView.setText("VIEW");
        btnView.setToolTipText("View Report");
        btnView.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnView.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn2.png"))); // NOI18N
        btnView.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnViewMouseClicked(evt);
            }
        });
        btnView.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnViewActionPerformed(evt);
            }
        });

        lblReportType.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblReportType.setText("Report Type           :");

        cmbReportType.setBackground(new java.awt.Color(51, 102, 255));
        cmbReportType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "A4 Size Report", "Text File 40 Col", "Excel Report" }));
        cmbReportType.setToolTipText("Select POS");

        lblShiftNo.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblShiftNo.setText("Shift No                 :");

        cmbShiftNo.setBackground(new java.awt.Color(51, 102, 255));
        cmbShiftNo.setToolTipText("Select POS");

        javax.swing.GroupLayout pnlMainLayout = new javax.swing.GroupLayout(pnlMain);
        pnlMain.setLayout(pnlMainLayout);
        pnlMainLayout.setHorizontalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMainLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(btnView, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(42, 42, 42)
                .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26))
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addGap(240, 240, 240)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addComponent(lblposName, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(cmbPosCode, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addComponent(lblCostCenterName, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(cmbCostCenterName, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addComponent(lbltype, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(cmbType, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addComponent(lblFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(dteFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addComponent(lblToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(dteToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(pnlMainLayout.createSequentialGroup()
                            .addComponent(lblShiftNo, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(cmbShiftNo, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlMainLayout.createSequentialGroup()
                            .addComponent(lblReportType, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(cmbReportType, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(lblCostCenterReport, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(256, Short.MAX_VALUE))
        );
        pnlMainLayout.setVerticalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(lblCostCenterReport, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(60, 60, 60)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblposName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbPosCode, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblCostCenterName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbCostCenterName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lbltype, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbType, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dteFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dteToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(29, 29, 29)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblReportType, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbReportType, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblShiftNo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbShiftNo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnView, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(43, 43, 43))
        );

        pnlBackGround.add(pnlMain, new java.awt.GridBagConstraints());

        getContentPane().add(pnlBackGround, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cmbCostCenterNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbCostCenterNameActionPerformed
	// TODO add your handling code here:
    }//GEN-LAST:event_cmbCostCenterNameActionPerformed

    private void cmbTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbTypeActionPerformed
	// TODO add your handling code here:
    }//GEN-LAST:event_cmbTypeActionPerformed
    /**
     * Close Window
     *
     * @param evt
     */
    private void btnBackMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnBackMouseClicked
	// TODO add your handling code here:
	dispose();
	clsGlobalVarClass.hmActiveForms.remove("Cost Centre Report");
    }//GEN-LAST:event_btnBackMouseClicked
    /**
     * This function is used View Cost Center Sales Wise Report
     *
     * @param evt
     */
    private void btnViewMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnViewMouseClicked

	try
	{
	    if (cmbReportType.getSelectedItem().toString().equalsIgnoreCase("A4 Size Report"))
	    {
		funCreateJasperReport();
	    }

	    else if (cmbReportType.getSelectedItem().toString().startsWith("Text File"))
	    {
		funCostCenterWiseTextReport();
	    }
	    else
	    {
		funGenerateExcelSheetOfReport();
	    }

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}


    }//GEN-LAST:event_btnViewMouseClicked

    private void btnViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnViewActionPerformed
	// TODO add your handling code here:
    }//GEN-LAST:event_btnViewActionPerformed

    private void cmbPosCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbPosCodeActionPerformed
	// TODO add your handling code here:
	funFillShiftCombo();
    }//GEN-LAST:event_cmbPosCodeActionPerformed

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("Cost Centre Report");
    }//GEN-LAST:event_btnBackActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("Cost Centre Report");
    }//GEN-LAST:event_formWindowClosed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("Cost Centre Report");
    }//GEN-LAST:event_formWindowClosing

    /**
     * @param args the command line arguments
     */
    public static void main(String args[])
    {
	/* Set the Nimbus look and feel */
	//<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
	/* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
	 */
	try
	{
	    for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
	    {
		if ("Nimbus".equals(info.getName()))
		{
		    javax.swing.UIManager.setLookAndFeel(info.getClassName());
		    break;
		}
	    }
	}
	catch (ClassNotFoundException ex)
	{
	    java.util.logging.Logger.getLogger(frmCostCenterReport.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	}
	catch (InstantiationException ex)
	{
	    java.util.logging.Logger.getLogger(frmCostCenterReport.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	}
	catch (IllegalAccessException ex)
	{
	    java.util.logging.Logger.getLogger(frmCostCenterReport.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	}
	catch (javax.swing.UnsupportedLookAndFeelException ex)
	{
	    java.util.logging.Logger.getLogger(frmCostCenterReport.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	}
	//</editor-fold>
	//</editor-fold>

	/* Create and display the form */
	java.awt.EventQueue.invokeLater(new Runnable()
	{
	    public void run()
	    {
		new frmCostCenterReport().setVisible(true);
	    }
	});
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnView;
    private javax.swing.JComboBox cmbCostCenterName;
    private javax.swing.JComboBox cmbPosCode;
    private javax.swing.JComboBox cmbReportType;
    private javax.swing.JComboBox cmbShiftNo;
    private javax.swing.JComboBox cmbType;
    private com.toedter.calendar.JDateChooser dteFromDate;
    private com.toedter.calendar.JDateChooser dteToDate;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.JLabel lblCostCenterName;
    private javax.swing.JLabel lblCostCenterReport;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblFromDate;
    private javax.swing.JLabel lblHOSign;
    private javax.swing.JLabel lblModuleName;
    private javax.swing.JLabel lblPosName;
    private javax.swing.JLabel lblProductName;
    private javax.swing.JLabel lblReportType;
    private javax.swing.JLabel lblShiftNo;
    private javax.swing.JLabel lblToDate;
    private javax.swing.JLabel lblUserCode;
    private javax.swing.JLabel lblfromName;
    private javax.swing.JLabel lblposName;
    private javax.swing.JLabel lbltype;
    private javax.swing.JPanel pnlBackGround;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JPanel pnlheader;
    // End of variables declaration//GEN-END:variables

    private void funCostCenterWiseSummaryJasperReport()
    {
	fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());

	toDate = objUtility.funGetFromToDate(dteToDate.getDate());
	String posCode = cmbPosCode.getSelectedItem().toString();

	StringBuilder sb = new StringBuilder(posCode);
	int len = posCode.length();
	int lastInd = sb.lastIndexOf(" ");
	posCode = sb.substring(lastInd + 1, len).toString();
	String posName = sb.substring(0, lastInd - 1).toString();

	String costCenterCode = cmbCostCenterName.getSelectedItem().toString();
	StringBuilder sb1 = new StringBuilder(costCenterCode);
	int len1 = costCenterCode.length();
	int lastInd1 = sb1.lastIndexOf(" ");
	costCenterCode = sb1.substring(lastInd1 + 1, len1).toString();

	StringBuilder sbSqlLive = new StringBuilder();
	StringBuilder sbSqlQFile = new StringBuilder();

	StringBuilder sbSqlFilters = new StringBuilder();

	reportName = "com/POSReport/reports/rptCostCenterWiseSummaryReport1.jasper";//summary

	try
	{
	    sbSqlLive.setLength(0);
	    sbSqlQFile.setLength(0);
	    sbSqlFilters.setLength(0);

	    // Live Sql
	    sbSqlLive.append("SELECT e.strPosCode,e.strPOSName,ifnull(a.strCostCenterCode,'ND')strCostCenterCode,ifnull(a.strCostCenterName,'ND')strCostCenterName "
		    + ",sum(c.dblAmount)dblSubTotal,sum(c.dblDiscountAmt) dblDiscountAmt,sum( c.dblAmount )-sum(c.dblDiscountAmt)dblSalesAmount "
		    + " from tblbilldtl c,tblbillhd d,tblposmaster e ,tblmenuitempricingdtl b,tblcostcentermaster a "
		    + " where date( d.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
		    + " and c.strBillNo = d.strBillNo and d.strPOSCode = e.strPOSCode and b.strItemCode = c.strItemCode "
		    + " and (b.strposcode =d.strposcode  or b.strPosCode='All') and a.strCostCenterCode = b.strCostCenterCode "
		    + " and c.strClientCode=d.strClientCode and b.strHourlyPricing='NO' ");
	    if (clsGlobalVarClass.gAreaWisePricing.equals("Y"))
	    {
		sbSqlLive.append(" and d.strAreaCode=b.strAreaCode ");
	    }

	    // QFile Sql    
	    sbSqlQFile.append("SELECT e.strPosCode,e.strPOSName,ifnull(a.strCostCenterCode,'ND')strCostCenterCode,ifnull(a.strCostCenterName,'ND')strCostCenterName "
		    + ",sum(c.dblAmount)dblSubTotal,sum(c.dblDiscountAmt) dblDiscountAmt,sum( c.dblAmount )-sum(c.dblDiscountAmt)dblSalesAmount "
		    + " from tblqbilldtl c,tblqbillhd d,tblposmaster e ,tblmenuitempricingdtl b,tblcostcentermaster a "
		    + " where date( d.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
		    + " and c.strBillNo = d.strBillNo "
		    + " and d.strPOSCode = e.strPOSCode "
		    + " and b.strItemCode = c.strItemCode "
		    + " and (b.strposcode =d.strposcode  or b.strPosCode='All') "
		    + " and a.strCostCenterCode = b.strCostCenterCode "
		    + " and c.strClientCode=d.strClientCode and b.strHourlyPricing='NO' ");
	    if (clsGlobalVarClass.gAreaWisePricing.equals("Y"))
	    {
		sbSqlQFile.append(" and d.strAreaCode=b.strAreaCode ");
	    }

	    String sqlModLive = "SELECT e.strPosCode,e.strPOSName,ifnull(a.strCostCenterCode,'ND')strCostCenterCode,ifnull(a.strCostCenterName,'ND')strCostCenterName "
		    + ",sum(c.dblAmount)dblSubTotal,sum(c.dblDiscAmt) dblDiscountAmt,sum( c.dblAmount )-sum(c.dblDiscAmt)dblSalesAmount  "
		    + " from tblbillmodifierdtl c,tblbillhd d,tblposmaster e ,tblmenuitempricingdtl b,tblcostcentermaster a "
		    + " where date( d.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
		    + " and c.strBillNo = d.strBillNo "
		    + " and d.strPOSCode = e.strPOSCode "
		    + " and b.strItemCode = left(c.strItemCode,7) "
		    + " and (b.strposcode =d.strposcode  or b.strPosCode='All') "
		    + " and a.strCostCenterCode = b.strCostCenterCode "
		    + " and c.dblAmount>0 "
		    + " and c.strClientCode=d.strClientCode and b.strHourlyPricing='NO' ";
	    if (clsGlobalVarClass.gAreaWisePricing.equals("Y"))
	    {
		sqlModLive += " and d.strAreaCode=b.strAreaCode ";
	    }

	    String sqlModQFile = "SELECT e.strPosCode,e.strPOSName,ifnull(a.strCostCenterCode,'ND')strCostCenterCode,ifnull(a.strCostCenterName,'ND')strCostCenterName "
		    + ",sum(c.dblAmount)dblSubTotal,sum(c.dblDiscAmt) dblDiscountAmt,sum( c.dblAmount )-sum(c.dblDiscAmt)dblSalesAmount  "
		    + " from tblqbillmodifierdtl c,tblqbillhd d,tblposmaster e ,tblmenuitempricingdtl b,tblcostcentermaster a "
		    + " where date( d.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
		    + " and c.strBillNo = d.strBillNo "
		    + " and d.strPOSCode = e.strPOSCode "
		    + " and b.strItemCode = left(c.strItemCode,7) "
		    + " and (b.strposcode =d.strposcode  or b.strPosCode='All') "
		    + " and a.strCostCenterCode = b.strCostCenterCode "
		    + " and c.dblAmount>0 "
		    + " and c.strClientCode=d.strClientCode and b.strHourlyPricing='NO' ";
	    if (clsGlobalVarClass.gAreaWisePricing.equals("Y"))
	    {
		sqlModQFile += " and d.strAreaCode=b.strAreaCode ";
	    }

	    if (!posCode.equals("All"))
	    {
		sbSqlFilters.append(" AND d.strPOSCode = '" + posCode + "' ");
	    }
	    if (clsGlobalVarClass.gEnableShiftYN && (!cmbShiftNo.getSelectedItem().toString().equalsIgnoreCase("All")))
	    {
		sbSqlFilters.append(" and b.intShiftCode ='" + cmbShiftNo.getSelectedItem().toString() + "' ");
	    }
	    if (!costCenterCode.equalsIgnoreCase("All"))
	    {
		sbSqlFilters.append(" and a.strCostCenterCode='" + costCenterCode + "' ");
	    }
	    sbSqlFilters.append("GROUP BY e.strPOSName,b.strCostCenterCode,a.strCostCenterName "
		    + "order BY e.strPOSName,b.strCostCenterCode,a.strCostCenterName ");

	    sbSqlLive.append(sbSqlFilters);
	    sbSqlQFile.append(sbSqlFilters);
	    sqlModLive = sqlModLive + " " + sbSqlFilters.toString();
	    sqlModQFile = sqlModQFile + " " + sbSqlFilters.toString();

	    List<clsCostCenterBean> listOfCostCenterDtl = new LinkedList<>();

	    //live data
	    ResultSet rsCostCenterData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
	    while (rsCostCenterData.next())
	    {
		clsCostCenterBean obj = new clsCostCenterBean();

		obj.setStrPOSCode(rsCostCenterData.getString(1));//posCode
		obj.setStrPOSName(rsCostCenterData.getString(2));//posName
		obj.setStrCostCenterCode(rsCostCenterData.getString(3));//costCenterCode
		obj.setStrCostCenterName(rsCostCenterData.getString(4));//costCenterName
		obj.setDblSubTotal(rsCostCenterData.getDouble(5));//subTotal
		obj.setDblDiscAmount(rsCostCenterData.getDouble(6));//discount
		obj.setDblSalesAmount(rsCostCenterData.getDouble(7));//salesAmount

		listOfCostCenterDtl.add(obj);

	    }
	    rsCostCenterData.close();
	    //live modifier data
	    rsCostCenterData = clsGlobalVarClass.dbMysql.executeResultSet(sqlModLive.toString());
	    while (rsCostCenterData.next())
	    {
		clsCostCenterBean obj = new clsCostCenterBean();

		obj.setStrPOSCode(rsCostCenterData.getString(1));//posCode
		obj.setStrPOSName(rsCostCenterData.getString(2));//posName
		obj.setStrCostCenterCode(rsCostCenterData.getString(3));//costCenterCode
		obj.setStrCostCenterName(rsCostCenterData.getString(4));//costCenterName
		obj.setDblSubTotal(rsCostCenterData.getDouble(5));//subTotal
		obj.setDblDiscAmount(rsCostCenterData.getDouble(6));//discount
		obj.setDblSalesAmount(rsCostCenterData.getDouble(7));//salesAmount

		listOfCostCenterDtl.add(obj);

	    }
	    rsCostCenterData.close();

	    //Q data
	    rsCostCenterData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFile.toString());
	    while (rsCostCenterData.next())
	    {
		clsCostCenterBean obj = new clsCostCenterBean();

		obj.setStrPOSCode(rsCostCenterData.getString(1));//posCode
		obj.setStrPOSName(rsCostCenterData.getString(2));//posName
		obj.setStrCostCenterCode(rsCostCenterData.getString(3));//costCenterCode
		obj.setStrCostCenterName(rsCostCenterData.getString(4));//costCenterName
		obj.setDblSubTotal(rsCostCenterData.getDouble(5));//subTotal
		obj.setDblDiscAmount(rsCostCenterData.getDouble(6));//discount
		obj.setDblSalesAmount(rsCostCenterData.getDouble(7));//salesAmount

		listOfCostCenterDtl.add(obj);

	    }
	    rsCostCenterData.close();
	    //Q modifier data
	    rsCostCenterData = clsGlobalVarClass.dbMysql.executeResultSet(sqlModQFile.toString());
	    while (rsCostCenterData.next())
	    {
		clsCostCenterBean obj = new clsCostCenterBean();

		obj.setStrPOSCode(rsCostCenterData.getString(1));//posCode
		obj.setStrPOSName(rsCostCenterData.getString(2));//posName
		obj.setStrCostCenterCode(rsCostCenterData.getString(3));//costCenterCode
		obj.setStrCostCenterName(rsCostCenterData.getString(4));//costCenterName
		obj.setDblSubTotal(rsCostCenterData.getDouble(5));//subTotal
		obj.setDblDiscAmount(rsCostCenterData.getDouble(6));//discount
		obj.setDblSalesAmount(rsCostCenterData.getDouble(7));//salesAmount

		listOfCostCenterDtl.add(obj);

	    }
	    rsCostCenterData.close();

	    Comparator<clsCostCenterBean> posCodeComparator = new Comparator<clsCostCenterBean>()
	    {

		@Override
		public int compare(clsCostCenterBean o1, clsCostCenterBean o2)
		{
		    return o1.getStrPOSCode().compareTo(o2.getStrPOSCode());
		}
	    };
	    Comparator<clsCostCenterBean> costCenterCodeComparator = new Comparator<clsCostCenterBean>()
	    {

		@Override
		public int compare(clsCostCenterBean o1, clsCostCenterBean o2)
		{
		    return o1.getStrCostCenterCode().compareTo(o2.getStrCostCenterCode());
		}
	    };

	    Collections.sort(listOfCostCenterDtl, new clsCostCenterComparator(posCodeComparator, costCenterCodeComparator));

	    InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);
	    posName = cmbPosCode.getSelectedItem().toString();
	    posCode = objUtility.funGetPOSCodeFromPOSName(posName);
	    imagePath = System.getProperty("user.dir");
	    imagePath = imagePath + File.separator + "ReportImage";
	    if (posCode.equalsIgnoreCase("All"))
	    {
		imagePath = imagePath + File.separator + "imgClientImage.jpg";
	    }
	    else
	    {
		imagePath = imagePath + File.separator + "img" + posCode + ".jpg";
	    }
	    System.out.println("imagePath=" + imagePath);

	    HashMap hm = new HashMap();

	    hm.put("posName", posName);
	    hm.put("CostCenterCode", costCenterCode);
	    hm.put("dtefromDate", fromDate);
	    hm.put("dtetoDate", toDate);
	    hm.put("userName", clsGlobalVarClass.gUserName);
	    hm.put("posCode", posCode);
	    hm.put("clientName", clsGlobalVarClass.gClientName);
	    hm.put("imagePath", imagePath);
	    hm.put("decimalFormaterForDoubleValue", gDecimalFormatString);
	    hm.put("decimalFormaterForIntegerValue", "0");

	    SimpleDateFormat ddmmyyyyDateFormat = new SimpleDateFormat("dd-MM-yyyy");
	    Date fDate = dteFromDate.getDate();
	    Date tDate = dteToDate.getDate();
	    String fromDateToDisplay = ddmmyyyyDateFormat.format(fDate);
	    String toDateToDisplay = ddmmyyyyDateFormat.format(tDate);

	    hm.put("fromDateToDisplay", fromDateToDisplay);
	    hm.put("toDateToDisplay", toDateToDisplay);

	    String shiftNo = "All", shiftCode = "All";
	    if (clsGlobalVarClass.gEnableShiftYN)
	    {
		if (clsGlobalVarClass.gEnableShiftYN && (!cmbShiftNo.getSelectedItem().toString().equalsIgnoreCase("All")))
		{
		    shiftNo = cmbShiftNo.getSelectedItem().toString();
		    shiftCode = cmbShiftNo.getSelectedItem().toString();
		}
		else
		{
		    shiftNo = cmbShiftNo.getSelectedItem().toString();
		    shiftCode = cmbShiftNo.getSelectedItem().toString();
		}
	    }
	    hm.put("shiftNo", shiftNo);
	    hm.put("shiftCode", shiftCode);

	    JRBeanCollectionDataSource beanCollectionDataSource = new JRBeanCollectionDataSource(listOfCostCenterDtl);

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
		//jf.setLocation(300, 10);
		jf.setLocationRelativeTo(this);
	    }
	}

	catch (Exception ex)
	{
	    ex.printStackTrace();
	}

	finally
	{
	    sbSqlLive = null;
	    sbSqlQFile = null;

	    sbSqlFilters = null;
	}
    }

    private void funCostCenterWiseDetailJasperReport()
    {
	Date objDate = dteFromDate.getDate();
	fromDate = (objDate.getYear() + 1900) + "-" + (objDate.getMonth() + 1) + "-" + objDate.getDate();

	objDate = dteToDate.getDate();
	toDate = (objDate.getYear() + 1900) + "-" + (objDate.getMonth() + 1) + "-" + objDate.getDate();

	String posCode = cmbPosCode.getSelectedItem().toString();

	StringBuilder sb = new StringBuilder(posCode);
	int len = posCode.length();
	int lastInd = sb.lastIndexOf(" ");
	posCode = sb.substring(lastInd + 1, len).toString();
	String posName = sb.substring(0, lastInd - 1).toString();

	String costCenterCode = cmbCostCenterName.getSelectedItem().toString();
	StringBuilder sb1 = new StringBuilder(costCenterCode);
	int len1 = costCenterCode.length();
	int lastInd1 = sb1.lastIndexOf(" ");
	costCenterCode = sb1.substring(lastInd1 + 1, len1).toString();

	StringBuilder sbSqlLive = new StringBuilder();
	StringBuilder sbSqlQFile = new StringBuilder();
	StringBuilder sbSqlModLive = new StringBuilder();
	StringBuilder sbSqlModQFile = new StringBuilder();

	try
	{
	    sbSqlQFile.setLength(0);
	    sbSqlQFile.append("SELECT ifnull(f.strCostCenterName,''),a.strItemName,sum(a.dblQuantity), sum(a.dblAmount)"
		    + " ,b.strPOSCode,'" + clsGlobalVarClass.gUserCode + "',ifnull(d.strPriceMonday,0.00)"
		    + " ,sum(a.dblAmount)-sum(a.dblDiscountAmt),sum(a.dblDiscountAmt)\n"
		    + " FROM tblqbilldtl a inner join tblqbillhd b on a.strBillNo=b.strBillNo \n"
		    + " inner join tblmenuitempricingdtl d on a.strItemCode = d.strItemCode "
		    + " and (b.strposcode =d.strposcode  or d.strPosCode='All') and d.strHourlyPricing='NO' ");
	    if (clsGlobalVarClass.gAreaWisePricing.equals("Y"))
	    {
		sbSqlQFile.append("and b.strAreaCode= d.strAreaCode ");
	    }
	    sbSqlQFile.append(" inner join tblcostcentermaster f on d.strCostCenterCode=f.strCostCenterCode "
		    + " where date(b.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    if (!costCenterCode.equalsIgnoreCase("All"))
	    {
		sbSqlQFile.append(" and d.strCostCenterCode='" + costCenterCode + "' ");
	    }
	    if (!posCode.equals("All"))
	    {
		sbSqlQFile.append(" and b.strPOSCode='" + posCode + "' ");
	    }

	    sbSqlLive.setLength(0);
	    sbSqlLive.append("SELECT ifnull(f.strCostCenterName,''),a.strItemName,sum(a.dblQuantity), sum(a.dblAmount)"
		    + " ,b.strPOSCode,'" + clsGlobalVarClass.gUserCode + "',ifnull(d.strPriceMonday,0.00)"
		    + " ,sum(a.dblAmount)-sum(a.dblDiscountAmt),sum(a.dblDiscountAmt)\n"
		    + " FROM tblbilldtl a inner join tblbillhd b on a.strBillNo=b.strBillNo \n"
		    + " inner join tblmenuitempricingdtl d on a.strItemCode = d.strItemCode "
		    + " and (b.strposcode =d.strposcode  or d.strPosCode='All') and d.strHourlyPricing='NO' ");
	    if (clsGlobalVarClass.gAreaWisePricing.equals("Y"))
	    {
		sbSqlLive.append("and b.strAreaCode= d.strAreaCode ");
	    }
	    sbSqlLive.append(" inner join tblcostcentermaster f on d.strCostCenterCode=f.strCostCenterCode "
		    + " where date(b.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    if (!costCenterCode.equalsIgnoreCase("All"))
	    {
		sbSqlLive.append(" and d.strCostCenterCode='" + costCenterCode + "' ");
	    }
	    if (!posCode.equals("All"))
	    {
		sbSqlLive.append(" and b.strPOSCode='" + posCode + "' ");
		sbSqlQFile.append(" and b.strPOSCode='" + posCode + "' ");
	    }

	    sbSqlLive.append(" Group by f.strCostCenterCode,b.strPoscode, a.strItemCode,a.strItemName");
	    sbSqlQFile.append(" Group by f.strCostCenterCode,b.strPoscode, a.strItemCode,a.strItemName");
	    System.out.println("detail live=" + sbSqlQFile);
	    System.out.println("detail Q=" + sbSqlLive);

	    sbSqlModLive.setLength(0);
	    sbSqlModLive.append("SELECT ifnull(f.strCostCenterName,''),a.strModifierName,sum(a.dblQuantity)"
		    + " ,sum(a.dblAmount),b.strPOSCode,'" + clsGlobalVarClass.gUserCode + "',ifnull(d.strPriceMonday,0.00)"
		    + " ,sum(a.dblAmount)-sum(a.dblDiscAmt),sum(a.dblDiscAmt) "
		    + " FROM tblbillmodifierdtl a inner join tblbillhd b on a.strBillNo=b.strBillNo "
		    + " inner join tblmenuitempricingdtl d on LEFT(a.strItemCode,7)  = d.strItemCode "
		    + " and (b.strposcode =d.strposcode  or d.strPosCode='All') and d.strHourlyPricing='NO' ");
	    if (clsGlobalVarClass.gAreaWisePricing.equals("Y"))
	    {
		sbSqlModLive.append("and b.strAreaCode= d.strAreaCode ");
	    }
	    sbSqlModLive.append(" inner join tblcostcentermaster f on d.strCostCenterCode=f.strCostCenterCode "
		    + " where date(b.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		    + " and a.dblAmount>0 ");
	    if (!costCenterCode.equalsIgnoreCase("All"))
	    {
		sbSqlModLive.append(" and d.strCostCenterCode='" + costCenterCode + "' ");
	    }

	    sbSqlModQFile.setLength(0);
	    sbSqlModQFile.append("SELECT ifnull(f.strCostCenterName,''),a.strModifierName,sum(a.dblQuantity) "
		    + " ,sum(a.dblAmount),b.strPOSCode,'" + clsGlobalVarClass.gUserCode + "',ifnull(d.strPriceMonday,0.00)"
		    + " ,sum(a.dblAmount)-sum(a.dblDiscAmt),sum(a.dblDiscAmt) "
		    + " FROM tblqbillmodifierdtl a inner join tblqbillhd b on a.strBillNo=b.strBillNo "
		    + " inner join tblmenuitempricingdtl d on LEFT(a.strItemCode,7)  = d.strItemCode "
		    + " and (b.strposcode =d.strposcode  or d.strPosCode='All') and d.strHourlyPricing='NO' ");
	    if (clsGlobalVarClass.gAreaWisePricing.equals("Y"))
	    {
		sbSqlModQFile.append(" and b.strAreaCode= d.strAreaCode ");
	    }
	    sbSqlModQFile.append(" inner join tblcostcentermaster f on d.strCostCenterCode=f.strCostCenterCode "
		    + " where date(b.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		    + " and a.dblAmount>0 ");
	    if (!costCenterCode.equalsIgnoreCase("All"))
	    {
		sbSqlModQFile.append(" and d.strCostCenterCode='" + costCenterCode + "' ");
	    }
	    if (!posCode.equals("All"))
	    {
		sbSqlModQFile.append(" and b.strPOSCode='" + posCode + "' ");
		sbSqlModLive.append(" and b.strPOSCode='" + posCode + "' ");
	    }

	    sbSqlModQFile.append(" Group by f.strCostCenterCode,b.strPoscode, a.strItemCode, d.strItemName");
	    sbSqlModLive.append(" Group by f.strCostCenterCode,b.strPoscode, a.strItemCode, d.strItemName");

	    //Non chargable kots
	    StringBuilder sqlNonChargableKOts = new StringBuilder();

	    sqlNonChargableKOts.append("select ifnull(c.strCostCenterName,''),b.strItemName,sum(a.dblQuantity),0.00 as dblAmount,a.strPOSCode,'user',a.dblRate,0.00 as dblAmt_dblDisc,0.00 as dblDisc "
		    + "from tblnonchargablekot a,tblmenuitempricingdtl b,tblcostcentermaster c "
		    + "where date(a.dteNCKOTDate) between '" + fromDate + "' and '" + toDate + "' "
		    + "and a.strItemCode=b.strItemCode "
		    + "and (a.strposcode =b.strposcode  or b.strPosCode='All') "
		    + "and b.strCostCenterCode=c.strCostCenterCode and b.strHourlyPricing='NO' ");
	    if (!costCenterCode.equalsIgnoreCase("All"))
	    {
		sqlNonChargableKOts.append(" and b.strCostCenterCode='" + costCenterCode + "' ");
	    }
	    if (!posCode.equals("All"))
	    {
		sqlNonChargableKOts.append(" and a.strPOSCode='" + posCode + "' ");
	    }
	    sqlNonChargableKOts.append("group by a.strItemCode,b.strItemName,c.strCostCenterCode,c.strCostCenterName ");
	    sqlNonChargableKOts.append("order by a.strItemCode,b.strItemName,c.strCostCenterCode,c.strCostCenterName ");

	    //System.out.println(sqlModQFile);
	    clsGlobalVarClass.dbMysql.execute("truncate table tbltempsalesflash");
	    String insertTempSalesFlash = "insert into tbltempsalesflash(strcode,strname,dblquantity,dblamount,strposcode,struser,dblRate,dblsubtotal,dbldiscamt) ";
	    clsGlobalVarClass.dbMysql.execute(insertTempSalesFlash + "(" + sbSqlLive + ")");
	    clsGlobalVarClass.dbMysql.execute(insertTempSalesFlash + "(" + sbSqlQFile + ")");
	    clsGlobalVarClass.dbMysql.execute(insertTempSalesFlash + "(" + sbSqlModLive + ")");
	    clsGlobalVarClass.dbMysql.execute(insertTempSalesFlash + "(" + sbSqlModQFile + ")");
	    //insert non chargable kots sales
	    clsGlobalVarClass.dbMysql.execute(insertTempSalesFlash + "(" + sqlNonChargableKOts + ")");

	    reportName = "com/POSReport/reports/rptCostCenterSalesWiseDetailReport1.jasper";//detail

	    posName = cmbPosCode.getSelectedItem().toString();
	    posCode = objUtility.funGetPOSCodeFromPOSName(posName);
	    imagePath = System.getProperty("user.dir");
	    imagePath = imagePath + File.separator + "ReportImage";
	    if (posCode.equalsIgnoreCase("All"))
	    {
		imagePath = imagePath + File.separator + "imgClientImage.jpg";
	    }
	    else
	    {
		imagePath = imagePath + File.separator + "img" + posCode + ".jpg";
	    }
	    System.out.println("imagePath=" + imagePath);
	    InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);
	    HashMap hm = new HashMap();

	    hm.put("posName", posName);
	    hm.put("CostCenterCode", costCenterCode);
	    hm.put("dtefromDate", fromDate);
	    hm.put("dtetoDate", toDate);
	    hm.put("userName", clsGlobalVarClass.gUserName);
	    hm.put("posCode", posCode);
	    hm.put("clientName", clsGlobalVarClass.gClientName);
	    hm.put("imagePath", imagePath);
	    hm.put("decimalFormaterForDoubleValue", gDecimalFormatString);
	    hm.put("decimalFormaterForIntegerValue", "0");

	    SimpleDateFormat ddmmyyyyDateFormat = new SimpleDateFormat("dd-MM-yyyy");
	    Date fDate = dteFromDate.getDate();
	    Date tDate = dteToDate.getDate();
	    String fromDateToDisplay = ddmmyyyyDateFormat.format(fDate);
	    String toDateToDisplay = ddmmyyyyDateFormat.format(tDate);

	    hm.put("fromDateToDisplay", fromDateToDisplay);
	    hm.put("toDateToDisplay", toDateToDisplay);

	    String shiftNo = "All", shiftCode = "All";
	    if (clsGlobalVarClass.gEnableShiftYN)
	    {
		if (clsGlobalVarClass.gEnableShiftYN && (!cmbShiftNo.getSelectedItem().toString().equalsIgnoreCase("All")))
		{
		    shiftNo = cmbShiftNo.getSelectedItem().toString();
		    shiftCode = cmbShiftNo.getSelectedItem().toString();
		}
		else
		{
		    shiftNo = cmbShiftNo.getSelectedItem().toString();
		    shiftCode = cmbShiftNo.getSelectedItem().toString();
		}
	    }
	    hm.put("shiftNo", shiftNo);
	    hm.put("shiftCode", shiftCode);
	    JasperPrint print = JasperFillManager.fillReport(is, hm, clsGlobalVarClass.conJasper);
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
		//jf.setLocation(300, 10);
		jf.setLocationRelativeTo(this);
	    }
	}
	catch (Exception ex)
	{
	    ex.printStackTrace();
	}
	finally
	{
	    sbSqlLive = null;
	    sbSqlQFile = null;
	    sbSqlModLive = null;
	    sbSqlModQFile = null;

	}
    }
}
