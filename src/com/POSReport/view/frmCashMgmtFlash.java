/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.POSReport.view;

import com.POSGlobal.controller.clsCashManagement;
import com.POSGlobal.controller.clsCashManagementDtl;
import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsPosConfigFile;
import com.POSGlobal.controller.clsUtility;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.swing.JRViewer;

public class frmCashMgmtFlash extends javax.swing.JFrame
{

    private String fromDate, toDate;
    private Object[] records;
    private String exportFormName, savePath, formName;
    private java.util.Vector vPOSCode, vReasonCode, vReasonName;
    private DecimalFormat decFormatter;
    private clsUtility objUtility;
    private DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();

    public frmCashMgmtFlash()
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

	funSetLookAndFeel();
	this.setLocationRelativeTo(null);

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

    public frmCashMgmtFlash(String rights)
    {
	initComponents();
	this.setLocationRelativeTo(null);
	try
	{
	    //btnExport.setVisible(false);
	    objUtility = new clsUtility();
	    dteFromDate.setDate(objUtility.funGetDateToSetCalenderDate());
	    dteToDate.setDate(objUtility.funGetDateToSetCalenderDate());

	    savePath = clsPosConfigFile.exportReportPath;
	    fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());
	    toDate = objUtility.funGetFromToDate(dteToDate.getDate());
	    vPOSCode = new java.util.Vector();
	    vReasonCode = new java.util.Vector();
	    vReasonName = new java.util.Vector();

	    String sql = "select strReasonCode,strReasonName from tblreasonmaster where strStkIn='Y'";
	    ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(sql);
	    while (rs.next())
	    {
		String rName = rs.getString(2);
		vReasonCode.add(rs.getString(1));
		vReasonName.add(rName);
	    }
	    rs.close();

	    if (clsGlobalVarClass.gShowOnlyLoginPOSReports)
	    {
		cmbPOSCode.addItem(clsGlobalVarClass.gPOSName);
		vPOSCode.add(clsGlobalVarClass.gPOSCode);
	    }
	    else
	    {
		sql = "select strPosName,strPosCode from tblposmaster";
		rs = clsGlobalVarClass.dbMysql.executeResultSet(sql);
		while (rs.next())
		{
		    //cmbPOSCode.addItem(rs.getString(1)+" "+rs.getString(2));
		    cmbPOSCode.addItem(rs.getString(1));
		    vPOSCode.add(rs.getString(2));
		}
	    }

	    decFormatter = new DecimalFormat("##.00");
	    funCashManagementFlashForDetail();

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    /**
     * this Function is used for billWiseReport
     */
    private void funCashManagementFlashForDetail()
    {
	try
	{

	    formName = "CashManagementReport.jasper";
	    exportFormName = "CashManagement" + objUtility.funGetDateInString();

	    fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());
	    toDate = objUtility.funGetFromToDate(dteToDate.getDate());
	    DefaultTableModel dmDataRows = new DefaultTableModel();
	    dmDataRows = new DefaultTableModel();

	    dmDataRows.setRowCount(0);

	    dmDataRows.addColumn("User");
	    dmDataRows.addColumn("Trans Type");
	    dmDataRows.addColumn("Date");
	    dmDataRows.addColumn("POS");
	    dmDataRows.addColumn("Reason");
	    dmDataRows.addColumn("Remarks");
	    dmDataRows.addColumn("Amount");

	    DefaultTableModel dmTotalsRow = new DefaultTableModel();

	    dmTotalsRow.setRowCount(0);
	    dmTotalsRow.addColumn("");
	    dmTotalsRow.addColumn("");
	    tblTotal.setModel(dmTotalsRow);
	    records = new Object[7];
	    String pos = String.valueOf(vPOSCode.elementAt(cmbPOSCode.getSelectedIndex()));

	    String rollingEntryTime = "";
	    boolean flgPostRollingSales = false;
	    Map<String, clsTempSalesAmt> hmCashSalesAmt = new HashMap<String, clsTempSalesAmt>();
	    double amount = 0;
	    StringBuilder sbSqlSale = new StringBuilder();
	    sbSqlSale.setLength(0);
	    sbSqlSale.append("select a.strTransType,time(a.dteTransDate) "
		    + " from tblcashmanagement a "
		    + " where date(a.dteTransDate) between '" + fromDate + "' and '" + toDate + "' "
		    + " and a.strAgainst='Rolling' "
		    + " and a.strPOSCode='" + pos + "' "
		    + " group by time(a.dteTransDate)");
	    ResultSet rsRollingEntry = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlSale.toString());
	    if (rsRollingEntry.next())
	    {
		flgPostRollingSales = true;
		rollingEntryTime = rsRollingEntry.getString(2);
		sbSqlSale.setLength(0);
		sbSqlSale.append("select a.strUserEdited,sum(b.dblSettlementAmt),date(a.dteBillDate),d.strPOSName "
			+ " from tblbillhd a,tblbillsettlementdtl b,tblsettelmenthd c,tblposmaster d "
			+ " where a.strBillNo=b.strBillNo and b.strSettlementCode=c.strSettelmentCode "
			+ " and a.strPOSCode=d.strPOSCode and c.strSettelmentType='Cash' "
			+ " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
			+ " and a.strPOSCode='" + pos + "' "
			+ " and time(a.dteBillDate) < '" + rsRollingEntry.getString(2) + "' "
			+ " group by a.strUserEdited");
		ResultSet rsSalesAmt = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlSale.toString());
		while (rsSalesAmt.next())
		{
		    clsTempSalesAmt objCashSales = new clsTempSalesAmt();
		    objCashSales.setUserCode(rsSalesAmt.getString(1));
		    objCashSales.setTransType("Sale");
		    objCashSales.setDate(rsSalesAmt.getString(3).split("-")[2] + "-" + rsSalesAmt.getString(3).split("-")[1] + "-" + rsSalesAmt.getString(3).split("-")[0]);
		    objCashSales.setPOS(rsSalesAmt.getString(4));
		    objCashSales.setReason("");
		    objCashSales.setRemarks("");
		    objCashSales.setAmount(Double.parseDouble(rsSalesAmt.getString(2)));
		    hmCashSalesAmt.put(rsSalesAmt.getString(1), objCashSales);
		}
		rsSalesAmt.close();

		sbSqlSale.setLength(0);
		sbSqlSale.append("select a.strUserEdited,sum(b.dblSettlementAmt),date(a.dteBillDate),d.strPOSName "
			+ " from tblqbillhd a,tblqbillsettlementdtl b,tblsettelmenthd c,tblposmaster d "
			+ " where a.strBillNo=b.strBillNo and b.strSettlementCode=c.strSettelmentCode "
			+ " and a.strPOSCode=d.strPOSCode and c.strSettelmentType='Cash' "
			+ " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
			+ " and a.strPOSCode='" + pos + "' "
			+ " and time(a.dteBillDate) < '" + rsRollingEntry.getString(2) + "' "
			+ " group by a.strUserEdited");
		rsSalesAmt = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlSale.toString());
		while (rsSalesAmt.next())
		{
		    clsTempSalesAmt objCashSales = new clsTempSalesAmt();
		    if (hmCashSalesAmt.containsKey(rsSalesAmt.getString(1)))
		    {
			objCashSales = hmCashSalesAmt.get(rsSalesAmt.getString(1));
			objCashSales.setAmount(objCashSales.getAmount() + Double.parseDouble(rsSalesAmt.getString(2)));
			hmCashSalesAmt.put(rsSalesAmt.getString(1), objCashSales);
		    }
		    else
		    {
			objCashSales.setUserCode(rsSalesAmt.getString(1));
			objCashSales.setTransType("Sale");
			objCashSales.setDate(rsSalesAmt.getString(3).split("-")[2] + "-" + rsSalesAmt.getString(3).split("-")[1] + "-" + rsSalesAmt.getString(3).split("-")[0]);
			objCashSales.setPOS(rsSalesAmt.getString(4));
			objCashSales.setReason("");
			objCashSales.setRemarks("");
			objCashSales.setAmount(Double.parseDouble(rsSalesAmt.getString(2)));
			hmCashSalesAmt.put(rsSalesAmt.getString(1), objCashSales);
		    }
		}
		rsSalesAmt.close();
	    }
	    else
	    {
		sbSqlSale.setLength(0);
		sbSqlSale.append("select a.strUserEdited,sum(b.dblSettlementAmt),date(a.dteBillDate),d.strPOSName "
			+ " from tblbillhd a,tblbillsettlementdtl b,tblsettelmenthd c,tblposmaster d "
			+ " where a.strBillNo=b.strBillNo and b.strSettlementCode=c.strSettelmentCode "
			+ " and a.strPOSCode=d.strPOSCode and c.strSettelmentType='Cash' "
			+ " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
			+ " and a.strPOSCode='" + pos + "' "
			+ " group by a.strUserEdited");
		ResultSet rsSalesAmt = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlSale.toString());
		while (rsSalesAmt.next())
		{
		    clsTempSalesAmt objCashSales = new clsTempSalesAmt();
		    objCashSales.setUserCode(rsSalesAmt.getString(1));
		    objCashSales.setTransType("Sale");
		    objCashSales.setDate(rsSalesAmt.getString(3).split("-")[2] + "-" + rsSalesAmt.getString(3).split("-")[1] + "-" + rsSalesAmt.getString(3).split("-")[0]);
		    objCashSales.setPOS(rsSalesAmt.getString(4));
		    objCashSales.setReason("");
		    objCashSales.setRemarks("");
		    objCashSales.setAmount(Double.parseDouble(rsSalesAmt.getString(2)));
		    hmCashSalesAmt.put(rsSalesAmt.getString(1), objCashSales);
		}
		rsSalesAmt.close();

		sbSqlSale.setLength(0);
		sbSqlSale.append("select a.strUserEdited,sum(b.dblSettlementAmt),date(a.dteBillDate),d.strPOSName "
			+ " from tblqbillhd a,tblqbillsettlementdtl b,tblsettelmenthd c,tblposmaster d "
			+ " where a.strBillNo=b.strBillNo and b.strSettlementCode=c.strSettelmentCode "
			+ " and a.strPOSCode=d.strPOSCode and c.strSettelmentType='Cash' "
			+ " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
			+ " and a.strPOSCode='" + pos + "' "
			+ " group by a.strUserEdited");
		rsSalesAmt = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlSale.toString());
		while (rsSalesAmt.next())
		{
		    clsTempSalesAmt objCashSales = new clsTempSalesAmt();
		    if (hmCashSalesAmt.containsKey(rsSalesAmt.getString(1)))
		    {
			objCashSales = hmCashSalesAmt.get(rsSalesAmt.getString(1));
			objCashSales.setAmount(objCashSales.getAmount() + Double.parseDouble(rsSalesAmt.getString(2)));
			hmCashSalesAmt.put(rsSalesAmt.getString(1), objCashSales);
		    }
		    else
		    {
			objCashSales.setUserCode(rsSalesAmt.getString(1));
			objCashSales.setTransType("Sale");
			objCashSales.setDate(rsSalesAmt.getString(3).split("-")[2] + "-" + rsSalesAmt.getString(3).split("-")[1] + "-" + rsSalesAmt.getString(3).split("-")[0]);
			objCashSales.setPOS(rsSalesAmt.getString(4));
			objCashSales.setReason("");
			objCashSales.setRemarks("");
			objCashSales.setAmount(Double.parseDouble(rsSalesAmt.getString(2)));
			hmCashSalesAmt.put(rsSalesAmt.getString(1), objCashSales);
		    }
		}
		rsSalesAmt.close();
	    }
	    rsRollingEntry.close();

	    for (Map.Entry<String, clsTempSalesAmt> entry : hmCashSalesAmt.entrySet())
	    {
		records[0] = entry.getValue().getUserCode();
		records[1] = entry.getValue().getTransType();
		records[2] = entry.getValue().getDate();
		records[3] = entry.getValue().getPOS();
		records[4] = entry.getValue().getReason();
		records[5] = entry.getValue().getRemarks();
		records[6] = gDecimalFormat.format(entry.getValue().getAmount());
		amount += entry.getValue().getAmount();
		dmDataRows.addRow(records);
	    }

	    sbSqlSale.setLength(0);
	    sbSqlSale.append("select a.strUserEdited,sum(a.dblAdvDeposite),a.dtReceiptDate,b.strPOSName  "
		    + " from tbladvancereceipthd a,tblposmaster b "
		    + " where a.strPOSCode=b.strPOSCode "
		    + " and a.dtReceiptDate between '" + fromDate + "' and '" + toDate + "' "
		    + " and a.strPOSCode='" + pos + "' "
		    + " group by a.strUserEdited ");
	    ResultSet rsAdvAmt = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlSale.toString());
	    while (rsAdvAmt.next())
	    {
		records[0] = rsAdvAmt.getString(1);
		records[1] = "Advance";
		records[2] = rsAdvAmt.getString(3).split("-")[2] + "-" + rsAdvAmt.getString(3).split("-")[1] + "-" + rsAdvAmt.getString(3).split("-")[0];
		records[3] = rsAdvAmt.getString(4);
		records[4] = "";
		records[5] = "";
		records[6] = gDecimalFormat.format(rsAdvAmt.getDouble(2));
		amount += rsAdvAmt.getDouble(2);
		dmDataRows.addRow(records);
	    }
	    rsAdvAmt.close();

	    sbSqlSale.setLength(0);
	    sbSqlSale.append("select a.strUserEdited,sum(a.dblAdvDeposite),a.dtReceiptDate,b.strPOSName  "
		    + " from tblqadvancereceipthd a,tblposmaster b "
		    + " where a.strPOSCode=b.strPOSCode "
		    + " and a.dtReceiptDate between '" + fromDate + "' and '" + toDate + "' "
		    + " and a.strPOSCode='" + pos + "' "
		    + " group by a.strUserEdited ");
	    rsAdvAmt = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlSale.toString());
	    while (rsAdvAmt.next())
	    {
		records[0] = rsAdvAmt.getString(1);
		records[1] = "Advance";
		records[2] = rsAdvAmt.getString(3).split("-")[2] + "-" + rsAdvAmt.getString(3).split("-")[1] + "-" + rsAdvAmt.getString(3).split("-")[0];
		records[3] = rsAdvAmt.getString(4);
		records[4] = "";
		records[5] = "";
		records[6] = gDecimalFormat.format(rsAdvAmt.getDouble(2));
		amount += rsAdvAmt.getDouble(2);
		dmDataRows.addRow(records);
	    }
	    rsAdvAmt.close();

	    if (cmbTransType.getSelectedItem().toString().equals("All"))
	    {
		sbSqlSale.setLength(0);
		sbSqlSale.append("select a.strUserEdited,a.strTransType,date(a.dteTransDate),ifnull(c.strPosName,'All')"
			+ " ,b.strReasonName,a.strRemarks,a.dblAmount "
			+ " from tblcashmanagement a inner join tblreasonmaster b on a.strReasonCode=b.strReasonCode "
			+ " left outer join tblposmaster c on a.strPOSCode=c.strPosCode "
			+ " where date(a.dteTransDate) between '" + fromDate + "' and '" + toDate + "' "
			+ " and a.strPOSCode='" + pos + "' "
			+ " order by a.strUserEdited,a.strTransType ");
	    }
	    else
	    {
		sbSqlSale.setLength(0);
		sbSqlSale.append("select a.strUserEdited,a.strTransType,date(a.dteTransDate),ifnull(c.strPosName,'All')"
			+ " ,b.strReasonName,a.strRemarks,a.dblAmount "
			+ " from tblcashmanagement a inner join tblreasonmaster b on a.strReasonCode=b.strReasonCode "
			+ " left outer join tblposmaster c on a.strPOSCode=c.strPosCode "
			+ " where date(a.dteTransDate) between '" + fromDate + "' and '" + toDate + "' "
			+ " and a.strPOSCode='" + pos + "' "
			+ " and a.strTransType='" + cmbTransType.getSelectedItem() + "' "
			+ " order by a.strUserEdited,a.strTransType ");
	    }
	    ResultSet rsCashMgmt = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlSale.toString());
	    while (rsCashMgmt.next())
	    {
		records[0] = rsCashMgmt.getString(1);
		records[1] = rsCashMgmt.getString(2);
		records[2] = rsCashMgmt.getString(3).split("-")[2] + "-" + rsCashMgmt.getString(3).split("-")[1] + "-" + rsCashMgmt.getString(3).split("-")[0];
		records[3] = rsCashMgmt.getString(4);
		records[4] = rsCashMgmt.getString(5);
		records[5] = rsCashMgmt.getString(6);
		records[6] = gDecimalFormat.format(rsCashMgmt.getDouble(7));
		String transType = rsCashMgmt.getString(2);
		if (transType.equalsIgnoreCase("Float"))
		{
		    amount += Double.parseDouble(rsCashMgmt.getString(7));
		}
		else if (transType.equalsIgnoreCase("Transfer In"))
		{
		    amount += Double.parseDouble(rsCashMgmt.getString(7));
		}
		else
		{
		    amount -= Double.parseDouble(rsCashMgmt.getString(7));
		}
		dmDataRows.addRow(records);
	    }
	    rsCashMgmt.close();

	    if (flgPostRollingSales)
	    {
		hmCashSalesAmt = new HashMap<String, clsTempSalesAmt>();
		sbSqlSale.setLength(0);
		sbSqlSale.append("select a.strUserEdited,sum(b.dblSettlementAmt),date(a.dteBillDate),d.strPOSName "
			+ " from tblbillhd a,tblbillsettlementdtl b,tblsettelmenthd c,tblposmaster d "
			+ " where a.strBillNo=b.strBillNo and b.strSettlementCode=c.strSettelmentCode "
			+ " and a.strPOSCode=d.strPOSCode and c.strSettelmentType='Cash' "
			+ " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
			+ " and a.strPOSCode='" + pos + "' "
			+ " and time(a.dteBillDate) > '" + rollingEntryTime + "' "
			+ " group by a.strUserEdited");
		ResultSet rsSalesAmt = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlSale.toString());
		while (rsSalesAmt.next())
		{
		    clsTempSalesAmt objCashSales = new clsTempSalesAmt();
		    objCashSales.setUserCode(rsSalesAmt.getString(1));
		    objCashSales.setTransType("Sale After Rolling");
		    objCashSales.setDate(rsSalesAmt.getString(3).split("-")[2] + "-" + rsSalesAmt.getString(3).split("-")[1] + "-" + rsSalesAmt.getString(3).split("-")[0]);
		    objCashSales.setPOS(rsSalesAmt.getString(4));
		    objCashSales.setReason("");
		    objCashSales.setRemarks("");
		    objCashSales.setAmount(Double.parseDouble(rsSalesAmt.getString(2)));
		    hmCashSalesAmt.put(rsSalesAmt.getString(1), objCashSales);
		}
		rsSalesAmt.close();

		sbSqlSale.setLength(0);
		sbSqlSale.append("select a.strUserEdited,sum(b.dblSettlementAmt),date(a.dteBillDate),d.strPOSName "
			+ " from tblqbillhd a,tblqbillsettlementdtl b,tblsettelmenthd c,tblposmaster d "
			+ " where a.strBillNo=b.strBillNo and b.strSettlementCode=c.strSettelmentCode "
			+ " and a.strPOSCode=d.strPOSCode and c.strSettelmentType='Cash' "
			+ " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
			+ " and a.strPOSCode='" + pos + "' "
			+ " and time(a.dteBillDate) > '" + rollingEntryTime + "' "
			+ " group by a.strUserEdited");
		rsSalesAmt = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlSale.toString());
		while (rsSalesAmt.next())
		{
		    clsTempSalesAmt objCashSales = new clsTempSalesAmt();
		    if (hmCashSalesAmt.containsKey(rsSalesAmt.getString(1)))
		    {
			objCashSales = hmCashSalesAmt.get(rsSalesAmt.getString(1));
			objCashSales.setAmount(objCashSales.getAmount() + Double.parseDouble(rsSalesAmt.getString(2)));
			hmCashSalesAmt.put(rsSalesAmt.getString(1), objCashSales);
		    }
		    else
		    {
			objCashSales.setUserCode(rsSalesAmt.getString(1));
			objCashSales.setTransType("Sale After Rolling");
			objCashSales.setDate(rsSalesAmt.getString(3).split("-")[2] + "-" + rsSalesAmt.getString(3).split("-")[1] + "-" + rsSalesAmt.getString(3).split("-")[0]);
			objCashSales.setPOS(rsSalesAmt.getString(4));
			objCashSales.setReason("");
			objCashSales.setRemarks("");
			objCashSales.setAmount(Double.parseDouble(rsSalesAmt.getString(2)));
			hmCashSalesAmt.put(rsSalesAmt.getString(1), objCashSales);
		    }
		}
		rsSalesAmt.close();

		for (Map.Entry<String, clsTempSalesAmt> entry : hmCashSalesAmt.entrySet())
		{
		    records[0] = entry.getValue().getUserCode();
		    records[1] = entry.getValue().getTransType();
		    records[2] = entry.getValue().getDate();
		    records[3] = entry.getValue().getPOS();
		    records[4] = entry.getValue().getReason();
		    records[5] = entry.getValue().getRemarks();
		    records[6] = gDecimalFormat.format(entry.getValue().getAmount());
		    amount += entry.getValue().getAmount();
		    dmDataRows.addRow(records);
		}
	    }

	    Object[] arrObjRows =
	    {
		"Total", gDecimalFormat.format(amount)
	    };
	    dmTotalsRow.addRow(arrObjRows);

	    tblTotal.setModel(dmTotalsRow);
	    tblSales.setModel(dmDataRows);
	    tblSales.setRowHeight(25);
	    tblTotal.setRowHeight(25);
	    DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
	    rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
	    tblSales.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);
	    tblSales.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    tblSales.getColumnModel().getColumn(0).setPreferredWidth(100);
	    tblSales.getColumnModel().getColumn(1).setPreferredWidth(100);
	    tblSales.getColumnModel().getColumn(2).setPreferredWidth(80);
	    tblSales.getColumnModel().getColumn(3).setPreferredWidth(140);
	    tblSales.getColumnModel().getColumn(4).setPreferredWidth(110);
	    tblSales.getColumnModel().getColumn(5).setPreferredWidth(180);
	    tblSales.getColumnModel().getColumn(6).setPreferredWidth(80);
	    tblTotal.setSize(400, 400);
	    DefaultTableCellRenderer rightRenderer1 = new DefaultTableCellRenderer();
	    rightRenderer1.setHorizontalAlignment(JLabel.RIGHT);
	    tblTotal.getColumnModel().getColumn(1).setCellRenderer(rightRenderer1);
	    tblTotal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    tblTotal.getColumnModel().getColumn(0).setPreferredWidth(710);
	    tblTotal.getColumnModel().getColumn(1).setPreferredWidth(80);

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    /**
     * bill Wise Report
     *
     * @param type
     */
    private void funCashManagementFlashForSummary()
    {
	try
	{
	    formName = "billWiseReport.jasper";
	    exportFormName = "billwise" + objUtility.funGetDateInString();

	    fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());
	    toDate = objUtility.funGetFromToDate(dteToDate.getDate());

	    DefaultTableModel dmDataRows = new DefaultTableModel();
	    dmDataRows.addColumn("User");
	    dmDataRows.addColumn("Transaction Type");
	    dmDataRows.addColumn("Date");
	    dmDataRows.addColumn("POS");
	    dmDataRows.addColumn("Amount");

	    DefaultTableModel dmTotalsRow = new DefaultTableModel();
	    dmTotalsRow.addColumn("");
	    dmTotalsRow.addColumn("");
	    records = new Object[5];
	    String pos = String.valueOf(vPOSCode.elementAt(cmbPOSCode.getSelectedIndex()));

	    StringBuilder sbSqlSale = new StringBuilder();
	    double amount = 0;
	    if (cmbTransType.getSelectedItem().toString().equals("All"))
	    {
		sbSqlSale.setLength(0);
		sbSqlSale.append("select a.strUserEdited,a.strTransType,date(a.dteTransDate),ifnull(c.strPosName,'All')"
			+ " ,sum(a.dblAmount) "
			+ " from tblcashmanagement a inner join tblreasonmaster b on a.strReasonCode=b.strReasonCode "
			+ " left outer join tblposmaster c on a.strPOSCode=c.strPosCode "
			+ " where date(a.dteTransDate) between '" + fromDate + "' and '" + toDate + "' and a.strPOSCode='" + pos + "' "
			+ " group by a.strUserEdited,a.strTransType "
			+ " order by a.strUserEdited");
	    }
	    else
	    {
		sbSqlSale.setLength(0);
		sbSqlSale.append("select a.strUserEdited,a.strTransType,date(a.dteTransDate),ifnull(c.strPosName,'All')"
			+ " ,sum(a.dblAmount) "
			+ " from tblcashmanagement a inner join tblreasonmaster b on a.strReasonCode=b.strReasonCode "
			+ " left outer join tblposmaster c on a.strPOSCode=c.strPosCode "
			+ " where date(a.dteTransDate) between '" + fromDate + "' and '" + toDate + "' and a.strPOSCode='" + pos + "' "
			+ " and a.strTransType='" + cmbTransType.getSelectedItem() + "' "
			+ " group by a.strUserEdited,a.strTransType "
			+ " order by a.strUserEdited");
	    }
	    System.out.println(sbSqlSale.toString());
	    ResultSet rsCashMgmt = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlSale.toString());
	    while (rsCashMgmt.next())
	    {
		records[0] = rsCashMgmt.getString(1);
		records[1] = rsCashMgmt.getString(2);
		records[2] = rsCashMgmt.getString(3).split("-")[2] + "-" + rsCashMgmt.getString(3).split("-")[1] + "-" + rsCashMgmt.getString(3).split("-")[0];
		records[3] = rsCashMgmt.getString(4);
		records[4] = gDecimalFormat.format(rsCashMgmt.getDouble(5));
		amount += Double.parseDouble(rsCashMgmt.getString(5));
		dmDataRows.addRow(records);
	    }
	    rsCashMgmt.close();
	    Object[] arrObjRows =
	    {
		"Total", gDecimalFormat.format(amount)
	    };
	    dmTotalsRow.addRow(arrObjRows);

	    tblSales.setModel(dmDataRows);
	    tblTotal.setModel(dmTotalsRow);

	    tblSales.setRowHeight(25);
	    tblTotal.setRowHeight(25);
	    DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
	    rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
	    tblSales.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    tblSales.getColumnModel().getColumn(0).setPreferredWidth(200);
	    tblSales.getColumnModel().getColumn(1).setPreferredWidth(200);
	    tblSales.getColumnModel().getColumn(2).setPreferredWidth(120);
	    tblSales.getColumnModel().getColumn(3).setPreferredWidth(130);
	    tblSales.getColumnModel().getColumn(4).setPreferredWidth(130);
	    tblSales.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
	    tblTotal.setSize(600, 600);

	    tblTotal.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
	    tblTotal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    tblTotal.getColumnModel().getColumn(0).setPreferredWidth(650);
	    tblTotal.getColumnModel().getColumn(1).setPreferredWidth(130);

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funExportToJasper() throws Exception
    {
	com.mysql.jdbc.Connection con = null;
	Class.forName("com.mysql.jdbc.Driver");
	con = (com.mysql.jdbc.Connection) DriverManager.getConnection("jdbc:mysql://localhost:3306/jpos", "root", "root");
	//String pdfFileName="D:/"+exportFormName+".pdf";
	String outFilenameExcel = savePath + "/" + exportFormName + ".xls";
	String pdfFileName = savePath + "/" + exportFormName + ".pdf";
	String reportName = "Report/" + formName;
	InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);
	HashMap hm = new HashMap();
	hm.put("fromDate", fromDate);
	hm.put("toDate", toDate);

	JasperPrint print = JasperFillManager.fillReport(is, hm, con);
	JasperExportManager.exportReportToPdfFile(print, pdfFileName);
	JRViewer viewer = new JRViewer(print);

	JFrame jf = new JFrame();
	jf.getContentPane().add(viewer);
	jf.validate();
	jf.setVisible(true);
	jf.setSize(new Dimension(800, 700));
	jf.setLocation(300, 10);
	//jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static Map<String, clsCashManagementDtl> funSortMapByKey(Map<String, clsCashManagementDtl> unsortMap)
    {

	// 1. Convert Map to List of Map
	List<Map.Entry<String, clsCashManagementDtl>> list = new LinkedList<Map.Entry<String, clsCashManagementDtl>>(unsortMap.entrySet());

	// 2. Sort list with Collections.sort(), provide a custom Comparator
	//    Try switch the o1 o2 position for a different order
	Collections.sort(list, new Comparator<Map.Entry<String, clsCashManagementDtl>>()
	{
	    public int compare(Map.Entry<String, clsCashManagementDtl> o1,
		    Map.Entry<String, clsCashManagementDtl> o2)
	    {
		return (o1.getKey()).compareTo(o2.getKey());
	    }
	});

	// 3. Loop the sorted list and put it into a new insertion order Map LinkedHashMap
	Map<String, clsCashManagementDtl> sortedMap = new LinkedHashMap<String, clsCashManagementDtl>();
	for (Map.Entry<String, clsCashManagementDtl> entry : list)
	{
	    sortedMap.put(entry.getKey(), entry.getValue());
	}

	/*
         //classic iterator example
         for (Iterator<Map.Entry<String, Integer>> it = list.iterator(); it.hasNext(); ) {
         Map.Entry<String, Integer> entry = it.next();
         sortedMap.put(entry.getKey(), entry.getValue());
         }*/
	return sortedMap;
    }

    private void funExportToTextFile() throws Exception
    {
	fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());
	toDate = objUtility.funGetFromToDate(dteToDate.getDate());
	String POSCode = String.valueOf(vPOSCode.elementAt(cmbPOSCode.getSelectedIndex()));

	Map<String, clsCashManagementDtl> hmCashMgmtDtlTemp = new clsCashManagement().funGetCashManagement(fromDate, toDate, POSCode);
	Map<String, clsCashManagementDtl> hmCashMgmtDtl = funSortMapByKey(hmCashMgmtDtlTemp);

	double floatAmt = 0, refundAmt = 0, saleAmt = 0, advAmt = 0, paymentsAmt = 0, transInAmt = 0, transOutAmt = 0, withdrawlAmt = 0, totalBalanceAmt = 0, totalPostRollingSalesAmt = 0, totalRollingAmt = 0;

	clsUtility objUtility = new clsUtility();
	objUtility.funCreateTempFolder();
	String filePath = System.getProperty("user.dir");
	File file = new File(filePath + File.separator + "Temp" + File.separator + "CashManagement.txt");
	PrintWriter pw = new PrintWriter(file);
	pw.println(clsGlobalVarClass.gClientName);
	if (clsGlobalVarClass.gClientAddress2.trim().length() > 0)
	{
	    pw.println(clsGlobalVarClass.gClientAddress2);
	}
	if (clsGlobalVarClass.gClientAddress3.trim().length() > 0)
	{
	    pw.println(clsGlobalVarClass.gClientAddress3);
	}
	pw.println("Report : Cash Management");
	pw.println("POS    : " + cmbPOSCode.getSelectedItem());
	pw.println("Date   : " + "  " + fromDate + " " + "To" + " " + toDate);
	pw.println();
	pw.println("------------------------------------------------------------------------------------------------------------------------------------------------");
	pw.println();
	pw.print(objUtility.funPrintTextWithAlignment("User Name", 15, "Left"));
	pw.print(objUtility.funPrintTextWithAlignment("Float|", 12, "Right"));
	pw.print(objUtility.funPrintTextWithAlignment("Sale|", 12, "Right"));
	pw.print(objUtility.funPrintTextWithAlignment("Advance|", 12, "Right"));
	//pw.print(objUtility.funPrintTextWithAlignment("Withdrawl|", 25, "Right"));
	pw.print(objUtility.funPrintTextWithAlignment("Withdrawal|", 12, "Right"));
	pw.print(objUtility.funPrintTextWithAlignment("Payments|", 12, "Right"));
	pw.print(objUtility.funPrintTextWithAlignment("Refund|", 12, "Right"));
	pw.print(objUtility.funPrintTextWithAlignment("Trans In|", 12, "Right"));
	pw.print(objUtility.funPrintTextWithAlignment("Trans Out|", 12, "Right"));
	pw.print(objUtility.funPrintTextWithAlignment("Sale After Rolling|", 20, "Right"));
	pw.print(objUtility.funPrintTextWithAlignment("Balance|", 12, "Right"));
	pw.println();
	pw.print(objUtility.funPrintTextWithAlignment("(Rolling)", 63, "Right"));
	pw.println();
	pw.println("------------------------------------------------------------------------------------------------------------------------------------------------");
	pw.println();

	for (Map.Entry<String, clsCashManagementDtl> entry : hmCashMgmtDtl.entrySet())
	{
	    pw.print(objUtility.funPrintTextWithAlignment(entry.getKey(), 15, "Left"));

	    double balanceAmt = (entry.getValue().getSaleAmt() + entry.getValue().getAdvanceAmt() + entry.getValue().getFloatAmt() + entry.getValue().getTransferInAmt()) - (entry.getValue().getWithdrawlAmt() + entry.getValue().getPaymentAmt() + entry.getValue().getRefundAmt() + entry.getValue().getTransferOutAmt());
	    floatAmt += entry.getValue().getFloatAmt();
	    saleAmt += entry.getValue().getSaleAmt();
	    advAmt += entry.getValue().getAdvanceAmt();
	    transInAmt += entry.getValue().getTransferInAmt();
	    withdrawlAmt += entry.getValue().getWithdrawlAmt();
	    totalRollingAmt += entry.getValue().getRollingAmt();
	    refundAmt += entry.getValue().getRefundAmt();
	    paymentsAmt += entry.getValue().getPaymentAmt();
	    transOutAmt += entry.getValue().getTransferOutAmt();
	    totalBalanceAmt += balanceAmt;

	    pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(entry.getValue().getFloatAmt()), 12, "Right"));
	    pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(entry.getValue().getSaleAmt()), 12, "Right"));
	    pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(entry.getValue().getAdvanceAmt()), 12, "Right"));

	    /*
             if(entry.getValue().getRollingAmt()>0)
             {
             //pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(entry.getValue().getWithdrawlAmt())+"("+String.valueOf(entry.getValue().getRollingAmt())+")", 25, "Right"));
             }
             else
             {
             pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(entry.getValue().getWithdrawlAmt()), 12, "Right"));
             }*/
	    pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(entry.getValue().getWithdrawlAmt()), 12, "Right"));

	    pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(entry.getValue().getPaymentAmt()), 12, "Right"));
	    pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(entry.getValue().getRefundAmt()), 12, "Right"));
	    pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(entry.getValue().getTransferInAmt()), 12, "Right"));
	    pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(entry.getValue().getTransferOutAmt()), 12, "Right"));

	    if (null != entry.getValue().getHmPostRollingSalesAmt())
	    {
		for (Map.Entry<String, Double> entryPostRollingSales : entry.getValue().getHmPostRollingSalesAmt().entrySet())
		{
		    totalBalanceAmt += entryPostRollingSales.getValue();
		    balanceAmt += entryPostRollingSales.getValue();
		    totalPostRollingSalesAmt += entryPostRollingSales.getValue();
		    pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(entryPostRollingSales.getValue()), 20, "Right"));
		}
	    }
	    else
	    {
		pw.print(objUtility.funPrintTextWithAlignment("0.0", 20, "Right"));
	    }
	    pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(balanceAmt), 12, "Right"));
	    pw.println();
	    pw.print(objUtility.funPrintTextWithAlignment("(" + String.valueOf(entry.getValue().getRollingAmt()) + ")", 63, "Right"));
	    pw.println();
	    pw.println();
	}

	pw.println();
	pw.println("------------------------------------------------------------------------------------------------------------------------------------------------");
	pw.println();
	pw.print(objUtility.funPrintTextWithAlignment("Totals", 15, "Left"));
	pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(floatAmt), 12, "Right"));
	pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(saleAmt), 12, "Right"));
	pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(advAmt), 12, "Right"));
	/*if(totalRollingAmt>0)
         {
         pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(withdrawlAmt)+"("+totalRollingAmt+")", 25, "Right"));
         }
         else
         {
         pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(withdrawlAmt), 25, "Right"));
         }*/
	pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(withdrawlAmt), 12, "Right"));

	pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(paymentsAmt), 12, "Right"));
	pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(refundAmt), 12, "Right"));
	pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(transInAmt), 12, "Right"));
	pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(transOutAmt), 12, "Right"));
	pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(totalPostRollingSalesAmt), 20, "Right"));
	pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(totalBalanceAmt), 12, "Right"));
	pw.println();
	pw.print(objUtility.funPrintTextWithAlignment("(" + totalRollingAmt + ")", 63, "Right"));
	pw.println();
	pw.println();
	pw.println("------------------------------------------------------------------------------------------------------------------------------------------------");
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

	Desktop dt = Desktop.getDesktop();
	dt.open(file);
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
        lblformName = new javax.swing.JLabel();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 32767));
        filler5 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        lblPosName = new javax.swing.JLabel();
        lblUserCode = new javax.swing.JLabel();
        filler6 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        lblDate = new javax.swing.JLabel();
        lblHOSign = new javax.swing.JLabel();
        pnlBackground = new JPanel()
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
        pnlCashMgtFlash = new javax.swing.JPanel();
        lblToDate = new javax.swing.JLabel();
        dteToDate = new com.toedter.calendar.JDateChooser();
        pnlgridcashmgt = new javax.swing.JScrollPane();
        tblSales = new javax.swing.JTable();
        pnlgridTotal = new javax.swing.JScrollPane();
        tblTotal = new javax.swing.JTable();
        btnExport = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();
        dteFromDate = new com.toedter.calendar.JDateChooser();
        lblfromDate = new javax.swing.JLabel();
        btnShow = new javax.swing.JButton();
        lblReportType = new javax.swing.JLabel();
        cmbTransType = new javax.swing.JComboBox();
        lblTransType = new javax.swing.JLabel();
        cmbReportType = new javax.swing.JComboBox();
        lblPOSName = new javax.swing.JLabel();
        cmbPOSCode = new javax.swing.JComboBox();

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
        lblProductName.setText("SPOS -");
        pnlheader.add(lblProductName);

        lblModuleName.setBackground(new java.awt.Color(255, 255, 255));
        lblModuleName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblModuleName.setForeground(new java.awt.Color(255, 255, 255));
        pnlheader.add(lblModuleName);

        lblformName.setBackground(new java.awt.Color(255, 255, 255));
        lblformName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblformName.setForeground(new java.awt.Color(255, 255, 255));
        lblformName.setText("-Cash ManageMent Flash");
        pnlheader.add(lblformName);
        pnlheader.add(filler4);
        pnlheader.add(filler5);

        lblPosName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblPosName.setForeground(new java.awt.Color(255, 255, 255));
        lblPosName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblPosName.setMaximumSize(new java.awt.Dimension(321, 30));
        lblPosName.setMinimumSize(new java.awt.Dimension(321, 30));
        lblPosName.setPreferredSize(new java.awt.Dimension(321, 30));
        pnlheader.add(lblPosName);

        lblUserCode.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblUserCode.setForeground(new java.awt.Color(255, 255, 255));
        lblUserCode.setMaximumSize(new java.awt.Dimension(90, 30));
        lblUserCode.setMinimumSize(new java.awt.Dimension(90, 30));
        lblUserCode.setPreferredSize(new java.awt.Dimension(90, 30));
        pnlheader.add(lblUserCode);
        pnlheader.add(filler6);

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

        pnlBackground.setLayout(new java.awt.GridBagLayout());

        pnlMain.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, new java.awt.Color(204, 204, 204), new java.awt.Color(204, 204, 204), new java.awt.Color(153, 153, 153), new java.awt.Color(153, 153, 153)));
        pnlMain.setMinimumSize(new java.awt.Dimension(800, 570));
        pnlMain.setOpaque(false);

        pnlCashMgtFlash.setBackground(new java.awt.Color(255, 255, 255));
        pnlCashMgtFlash.setOpaque(false);
        pnlCashMgtFlash.setPreferredSize(new java.awt.Dimension(800, 508));
        pnlCashMgtFlash.setLayout(null);

        lblToDate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblToDate.setText("To Date");
        pnlCashMgtFlash.add(lblToDate);
        lblToDate.setBounds(570, 10, 67, 27);

        dteToDate.setToolTipText("Select To Date");
        dteToDate.setPreferredSize(new java.awt.Dimension(119, 35));
        pnlCashMgtFlash.add(dteToDate);
        dteToDate.setBounds(640, 10, 150, 30);

        tblSales.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String []
            {
                "Transaction Type", "POS Name", "Date", "Reason", "Amount", "Remarks"
            }
        ));
        tblSales.setRowHeight(25);
        pnlgridcashmgt.setViewportView(tblSales);

        pnlCashMgtFlash.add(pnlgridcashmgt);
        pnlgridcashmgt.setBounds(0, 110, 798, 340);

        tblTotal.setFont(new java.awt.Font("DejaVu Sans", 1, 14)); // NOI18N
        tblTotal.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {
                {null, null, null, null}
            },
            new String []
            {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tblTotal.setRowHeight(25);
        pnlgridTotal.setViewportView(tblTotal);

        pnlCashMgtFlash.add(pnlgridTotal);
        pnlgridTotal.setBounds(0, 450, 798, 52);

        btnExport.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnExport.setForeground(new java.awt.Color(255, 255, 255));
        btnExport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnExport.setText("EXPORT");
        btnExport.setToolTipText("Export File");
        btnExport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExport.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn2.png"))); // NOI18N
        btnExport.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnExportActionPerformed(evt);
            }
        });
        pnlCashMgtFlash.add(btnExport);
        btnExport.setBounds(550, 520, 100, 40);

        btnClose.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnClose.setForeground(new java.awt.Color(255, 255, 255));
        btnClose.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnClose.setText("CLOSE");
        btnClose.setToolTipText("Close Window");
        btnClose.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnClose.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn2.png"))); // NOI18N
        btnClose.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnCloseMouseClicked(evt);
            }
        });
        btnClose.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnCloseActionPerformed(evt);
            }
        });
        pnlCashMgtFlash.add(btnClose);
        btnClose.setBounds(680, 520, 100, 40);

        dteFromDate.setToolTipText("Select From Date");
        dteFromDate.setPreferredSize(new java.awt.Dimension(119, 35));
        pnlCashMgtFlash.add(dteFromDate);
        dteFromDate.setBounds(390, 10, 150, 30);

        lblfromDate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblfromDate.setText("From Date");
        pnlCashMgtFlash.add(lblfromDate);
        lblfromDate.setBounds(300, 10, 88, 29);

        btnShow.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        btnShow.setForeground(new java.awt.Color(255, 255, 255));
        btnShow.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnShow.setText("SHOW");
        btnShow.setToolTipText("Show Report");
        btnShow.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnShow.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn2.png"))); // NOI18N
        btnShow.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnShowActionPerformed(evt);
            }
        });
        pnlCashMgtFlash.add(btnShow);
        btnShow.setBounds(420, 520, 100, 40);

        lblReportType.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblReportType.setText("Report Type");
        pnlCashMgtFlash.add(lblReportType);
        lblReportType.setBounds(10, 60, 90, 30);

        cmbTransType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "All", "Transfer In", "Float", "Refund", "Withdrawal", "Payments", "Transfer Out" }));
        cmbTransType.setToolTipText("Select Transaction Type ");
        pnlCashMgtFlash.add(cmbTransType);
        cmbTransType.setBounds(100, 10, 170, 30);

        lblTransType.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblTransType.setText("Trans Type");
        pnlCashMgtFlash.add(lblTransType);
        lblTransType.setBounds(10, 10, 90, 30);

        cmbReportType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Detail", "Summery" }));
        cmbReportType.setToolTipText("Select Report Type");
        pnlCashMgtFlash.add(cmbReportType);
        cmbReportType.setBounds(100, 60, 170, 30);

        lblPOSName.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblPOSName.setText("POS Name");
        pnlCashMgtFlash.add(lblPOSName);
        lblPOSName.setBounds(300, 60, 70, 30);

        cmbPOSCode.setToolTipText("Select POS");
        pnlCashMgtFlash.add(cmbPOSCode);
        cmbPOSCode.setBounds(390, 60, 150, 30);

        javax.swing.GroupLayout pnlMainLayout = new javax.swing.GroupLayout(pnlMain);
        pnlMain.setLayout(pnlMainLayout);
        pnlMainLayout.setHorizontalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 806, Short.MAX_VALUE)
            .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnlMainLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(pnlCashMgtFlash, javax.swing.GroupLayout.PREFERRED_SIZE, 806, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        pnlMainLayout.setVerticalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 570, Short.MAX_VALUE)
            .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnlMainLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(pnlCashMgtFlash, javax.swing.GroupLayout.PREFERRED_SIZE, 570, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        pnlBackground.add(pnlMain, new java.awt.GridBagConstraints());

        getContentPane().add(pnlBackground, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
	// TODO add your handling code here:
	dispose();
	clsGlobalVarClass.hmActiveForms.remove("Cash Mgmt Report");
    }//GEN-LAST:event_btnCloseActionPerformed

    /**
     * This Function is used to show Reports
     *
     * @param evt
     */
    private void btnShowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowActionPerformed
	// TODO add your handling code here:
	try
	{
	    if (cmbReportType.getSelectedItem().toString().equals("Detail"))
	    {
		funCashManagementFlashForDetail();
	    }
	    else
	    {
		funCashManagementFlashForSummary();
	    }

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }//GEN-LAST:event_btnShowActionPerformed

    private void btnExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportActionPerformed
	// TODO add your handling code here:
	try
	{
	    funExportToTextFile();
	}
	catch (Exception ex)
	{
	    ex.printStackTrace();
	}
    }//GEN-LAST:event_btnExportActionPerformed

    private void btnCloseMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCloseMouseClicked
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("Cash Mgmt Report");
    }//GEN-LAST:event_btnCloseMouseClicked

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("Cash Mgmt Report");
    }//GEN-LAST:event_formWindowClosed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("Cash Mgmt Report");
    }//GEN-LAST:event_formWindowClosing


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnExport;
    private javax.swing.JButton btnShow;
    private javax.swing.JComboBox cmbPOSCode;
    private javax.swing.JComboBox cmbReportType;
    private javax.swing.JComboBox cmbTransType;
    private com.toedter.calendar.JDateChooser dteFromDate;
    private com.toedter.calendar.JDateChooser dteToDate;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblHOSign;
    private javax.swing.JLabel lblModuleName;
    private javax.swing.JLabel lblPOSName;
    private javax.swing.JLabel lblPosName;
    private javax.swing.JLabel lblProductName;
    private javax.swing.JLabel lblReportType;
    private javax.swing.JLabel lblToDate;
    private javax.swing.JLabel lblTransType;
    private javax.swing.JLabel lblUserCode;
    private javax.swing.JLabel lblformName;
    private javax.swing.JLabel lblfromDate;
    private javax.swing.JPanel pnlBackground;
    private javax.swing.JPanel pnlCashMgtFlash;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JScrollPane pnlgridTotal;
    private javax.swing.JScrollPane pnlgridcashmgt;
    private javax.swing.JPanel pnlheader;
    private javax.swing.JTable tblSales;
    private javax.swing.JTable tblTotal;
    // End of variables declaration//GEN-END:variables

    private void funSetLookAndFeel()
    {
	try
	{
	    // Set System L&F
	    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	    SwingUtilities.updateComponentTreeUI(this);
	}
	catch (UnsupportedLookAndFeelException e)
	{
	    // handle exception
	}
	catch (ClassNotFoundException e)
	{
	    // handle exception
	}
	catch (InstantiationException e)
	{
	    // handle exception
	}
	catch (IllegalAccessException e)
	{
	    // handle exception
	}
    }

    private void funResetLookAndFeel()
    {
	try
	{
	    for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
	    {
		System.out.println("lookandfeel" + info.getName());
		if ("Nimbus".equals(info.getName()))
		{
		    javax.swing.UIManager.setLookAndFeel(info.getClassName());
		    SwingUtilities.updateComponentTreeUI(this);
		    break;
		}
	    }
	}
	catch (ClassNotFoundException ex)
	{
	}
	catch (InstantiationException ex)
	{
	}
	catch (IllegalAccessException ex)
	{
	}
	catch (javax.swing.UnsupportedLookAndFeelException ex)
	{
	}
    }

}

class clsTempSalesAmt
{

    private String userCode;

    private String transType;

    private String date;

    private String POS;

    private String reason;

    private String remarks;

    private double amount;

    public String getUserCode()
    {
	return userCode;
    }

    public void setUserCode(String userCode)
    {
	this.userCode = userCode;
    }

    public String getTransType()
    {
	return transType;
    }

    public void setTransType(String transType)
    {
	this.transType = transType;
    }

    public String getDate()
    {
	return date;
    }

    public void setDate(String date)
    {
	this.date = date;
    }

    public String getPOS()
    {
	return POS;
    }

    public void setPOS(String POS)
    {
	this.POS = POS;
    }

    public String getReason()
    {
	return reason;
    }

    public void setReason(String reason)
    {
	this.reason = reason;
    }

    public String getRemarks()
    {
	return remarks;
    }

    public void setRemarks(String remarks)
    {
	this.remarks = remarks;
    }

    public double getAmount()
    {
	return amount;
    }

    public void setAmount(double amount)
    {
	this.amount = amount;
    }

}
