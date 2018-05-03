/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.POSReport.view;

import com.POSGlobal.controller.clsAdvBookBillHd;
import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsPosConfigFile;
import com.POSGlobal.controller.clsUtility;
import com.POSGlobal.view.frmSearchFormDialog;
import com.POSPrinting.Text.AdvReceipt.clsTextFormat1ForAdvReceipt;
import com.POSPrinting.Utility.clsPrintingUtility;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class frmAdvanceOrderFlash extends javax.swing.JFrame
{

    DefaultTableModel dm, totalDm;
    StringBuilder sb = new StringBuilder();
    private String fromDate, toDate;
    private String custCode, exportFormName;
    private Map hmAdvOrderType;
    private clsUtility objUtility;
    private String dateFilter;
    private DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();

    public frmAdvanceOrderFlash()
    {
	initComponents();
	this.setLocationRelativeTo(null);

	funSetLookAndFeel();

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
	    txtCustomerName.setEditable(false);
	    txtCustomerName.setEnabled(false);

	    dteFromDate.setDate(objUtility.funGetDateToSetCalenderDate());
	    dteToDate.setDate(objUtility.funGetDateToSetCalenderDate());

	    if (clsGlobalVarClass.gShowOnlyLoginPOSReports)
	    {
		cmbPosCode.addItem(clsGlobalVarClass.gPOSName + " " + clsGlobalVarClass.gPOSCode);
	    }
	    else
	    {
		cmbPosCode.addItem("All");
		sb.setLength(0);
		sb.append("select strPosName,strPosCode from tblposmaster");
		ResultSet rs1 = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
		while (rs1.next())
		{
		    cmbPosCode.addItem(rs1.getString(1) + " " + rs1.getString(2));
		}
		rs1.close();
	    }

	    cmbType.addItem("Bill wise");
	    cmbType.addItem("Item wise");
	    cmbType.addItem("Customer wise");
	    cmbType.addItem("Menu Head wise");
	    cmbType.addItem("Group wise");
	    funFillOrderMode();
	    funCustomerWise();

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
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    /**
     *
     * this function is used Filling Order Mode ComboBoxs
     */
    private int funFillOrderMode() throws Exception
    {

	cmbOrderMode.removeAllItems();
	cmbOrderMode.addItem("All");
	hmAdvOrderType = new HashMap<String, String>();
	hmAdvOrderType.put("All", "All");
	String sqlAdvOrderType = "select strAdvOrderTypeCode,strAdvOrderTypeName "
		+ "from tbladvanceordertypemaster where strOperational='Yes'";
	ResultSet rsAdvOrderType = clsGlobalVarClass.dbMysql.executeResultSet(sqlAdvOrderType);
	while (rsAdvOrderType.next())
	{
	    hmAdvOrderType.put(rsAdvOrderType.getString(2), rsAdvOrderType.getString(1));
	    cmbOrderMode.addItem(rsAdvOrderType.getString(2));
	}
	rsAdvOrderType.close();
	return 1;
    }

    /*
     * Filter for Select Customer To Show the Report Customer Wise
     */
    private void funCustomerWise()
    {
	Map<String, clsAdvBookBillHd> hmAdvOrderData = new HashMap<String, clsAdvBookBillHd>();
	try
	{
	    String posCode = cmbPosCode.getSelectedItem().toString();
	    StringBuilder sb = new StringBuilder(posCode);
	    int len = posCode.length();
	    int lastInd = sb.lastIndexOf(" ");
	    String pos = sb.substring(lastInd + 1, len).toString();

	    fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());
	    toDate = objUtility.funGetFromToDate(dteToDate.getDate());
	    dm = new DefaultTableModel();
	    dm.getDataVector().removeAllElements();
	    dm.addColumn("Adv Ord No");
	    dm.addColumn("Date");
	    dm.addColumn("Customer");
	    dm.addColumn("Order For");
	    dm.addColumn("Order Type");
	    dm.addColumn("Opeartion Type");
	    dm.addColumn("Disc Amt");
	    dm.addColumn("G/T Amt");
	    dm.addColumn("Adv Amt");
	    dm.addColumn("Balance");
	    dm.addColumn("Manual Adv Order No");
	    DefaultTableModel dm2 = new DefaultTableModel();
	    dm2.getDataVector().removeAllElements();
	    dm2.addColumn("");
	    dm2.addColumn("");
	    dm2.addColumn("");
	    dm2.addColumn("");
	    dm2.addColumn("");

	    tblTotal.updateUI();
	    double sumGt = 0.00, AdvAmt = 0.00;
	    double sumDisc = 0.00, sumBal = 0.00;

	    StringBuilder sbSqlForOpen = new StringBuilder(); // Adv Live only
	    StringBuilder sbSqlForBilled1 = new StringBuilder(); // Adv Live + Bill Live
	    StringBuilder sbSqlForBilled2 = new StringBuilder(); //Adv QFile + Bill Live
	    StringBuilder sbSqlForBilled3 = new StringBuilder(); //Adv QFile + Bill QFile
	    StringBuilder sbSqlForBilled11 = new StringBuilder(); // Adv Live + Bill Live
	    StringBuilder sbSqlForBilled22 = new StringBuilder(); //Adv QFile + Bill Live
	    StringBuilder sbSqlForBilled33 = new StringBuilder(); //Adv QFile + Bill QFile

	    sbSqlForOpen.setLength(0);
	    sbSqlForBilled1.setLength(0);
	    sbSqlForBilled2.setLength(0);
	    sbSqlForBilled3.setLength(0);
	    sbSqlForBilled11.setLength(0);
	    sbSqlForBilled22.setLength(0);
	    sbSqlForBilled33.setLength(0);

	    if (cmbStatus.getSelectedItem().equals("Open")) // For Open Adv Order Option
	    {
		sbSqlForOpen.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y')\n"
			+ ",a.strPOSCode,c.strCustomerName,a.dblGrandTotal,ifnull(b.dblAdvDeposite,0)\n"
			+ ",ifnull(a.dblGrandTotal-b.dblAdvDeposite,0) as Balance ,a.dblDiscountAmt\n"
			+ ",ifnull(f.strAdvOrderTypeName,''),ifnull(d.strOperationType,''),ifnull(d.strBillNo,''),'0'"
			+ ",'" + clsGlobalVarClass.gUserCode + "','" + clsGlobalVarClass.gClientCode + "'"
			+ ",a.strManualAdvOrderNo\n"
			+ "from tbladvbookbillhd a left outer join tblbillhd d on a.strAdvBookingNo=d.strAdvBookingNo \n"
			+ "left outer join tbladvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo "
			+ " left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode \n"
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			+ "where  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' \n"
			+ "and d.strBillNo is null");

		if (clsGlobalVarClass.gHOPOSType.equals("HOPOS"))
		{
		    sbSqlForOpen.setLength(0);
		    sbSqlForOpen.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y')\n"
			    + ",a.strPOSCode,c.strCustomerName,a.dblGrandTotal,ifnull(b.dblAdvDeposite,0)\n"
			    + ",ifnull(a.dblGrandTotal-b.dblAdvDeposite,0) as Balance ,a.dblDiscountAmt\n"
			    + ",ifnull(f.strAdvOrderTypeName,''),ifnull(d.strOperationType,''),ifnull(d.strBillNo,''),'0'"
			    + ",'" + clsGlobalVarClass.gUserCode + "','" + clsGlobalVarClass.gClientCode + "'"
			    + ",a.strManualAdvOrderNo\n"
			    + "from tblqadvbookbillhd a left outer join tblbillhd d on a.strAdvBookingNo=d.strAdvBookingNo \n"
			    + "left outer join tblqadvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo "
			    + " left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode \n"
			    + "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			    + "where  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' \n"
			    + "and d.strBillNo is null");
		}
	    }
	    else if (cmbStatus.getSelectedItem().equals("Settled")) // For Settled Option
	    {
		sbSqlForBilled1.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y')\n"
			+ ",a.strPOSCode,c.strCustomerName,a.dblGrandTotal,ifnull(b.dblAdvDeposite,0)\n"
			+ ",ifnull(a.dblGrandTotal-b.dblAdvDeposite,0) as Balance ,a.dblDiscountAmt\n"
			+ ",ifnull(f.strAdvOrderTypeName,''),ifnull(d.strOperationType,''),d.strBillNo,'0'"
			+ ",'" + clsGlobalVarClass.gUserCode + "','" + clsGlobalVarClass.gClientCode + "'"
			+ ",a.strManualAdvOrderNo\n"
			+ "from tbladvbookbillhd a left outer join tblbillhd d on a.strAdvBookingNo=d.strAdvBookingNo \n"
			+ "left outer join tbladvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode \n"
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			+ "where  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' \n"
			+ "and d.strBillNo is not null and LENGTH(d.strSettelmentMode)>0 ");

		sbSqlForBilled2.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y')\n"
			+ ",a.strPOSCode,c.strCustomerName,a.dblGrandTotal\n"
			+ ",ifnull(b.dblAdvDeposite,0),ifnull(a.dblGrandTotal-b.dblAdvDeposite,0) as Balance ,a.dblDiscountAmt\n"
			+ ",ifnull(f.strAdvOrderTypeName,''),ifnull(d.strOperationType,''),d.strBillNo,'0'"
			+ ",'" + clsGlobalVarClass.gUserCode + "','" + clsGlobalVarClass.gClientCode + "'"
			+ ",a.strManualAdvOrderNo\n"
			+ "from tblqadvbookbillhd a left outer join tblbillhd d on a.strAdvBookingNo=d.strAdvBookingNo \n"
			+ "left outer join tblqadvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode \n"
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			+ "where  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' \n"
			+ "and d.strBillNo is not null and LENGTH(d.strSettelmentMode)>0 ");

		sbSqlForBilled3.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y')\n"
			+ ",a.strPOSCode,c.strCustomerName,a.dblGrandTotal\n"
			+ ",ifnull(b.dblAdvDeposite,0),ifnull(a.dblGrandTotal-b.dblAdvDeposite,0) as Balance ,a.dblDiscountAmt\n"
			+ ",ifnull(f.strAdvOrderTypeName,''),ifnull(d.strOperationType,''),d.strBillNo,'0'"
			+ ",'" + clsGlobalVarClass.gUserCode + "','" + clsGlobalVarClass.gClientCode + "'"
			+ ",a.strManualAdvOrderNo\n"
			+ "from tblqadvbookbillhd a left outer join tblqbillhd d on a.strAdvBookingNo=d.strAdvBookingNo \n"
			+ "left outer join tblqadvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode \n"
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			+ "where  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' \n"
			+ "and d.strSettelmentMode is not null and LENGTH(d.strSettelmentMode)>0 ");

		if (!cmbTransType.getSelectedItem().toString().trim().equals("All"))
		{
		    sbSqlForBilled1.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		    sbSqlForBilled2.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		    sbSqlForBilled3.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		}
	    }
	    else if (cmbStatus.getSelectedItem().equals("Billed")) // For Billed Option
	    {
		sbSqlForBilled1.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y')\n"
			+ ",a.strPOSCode,c.strCustomerName,a.dblGrandTotal,ifnull(b.dblAdvDeposite,0)\n"
			+ ",ifnull(a.dblGrandTotal-b.dblAdvDeposite,0) as Balance ,a.dblDiscountAmt\n"
			+ ",ifnull(f.strAdvOrderTypeName,''),ifnull(d.strOperationType,''),d.strBillNo,'0'"
			+ ",'" + clsGlobalVarClass.gUserCode + "','" + clsGlobalVarClass.gClientCode + "'"
			+ ",a.strManualAdvOrderNo\n"
			+ "from tbladvbookbillhd a left outer join tblbillhd d on a.strAdvBookingNo=d.strAdvBookingNo \n"
			+ "left outer join tbladvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode \n"
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			+ "where Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' \n"
			+ "and d.strBillNo not in(select strBillNo from tblbillsettlementdtl) "
			+ " and d.strBillNo not in(select strBillNo from tblqbillsettlementdtl) "
			+ "and d.strBillNo is not null");

		sbSqlForBilled2.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y')\n"
			+ ",a.strPOSCode,c.strCustomerName,a.dblGrandTotal\n"
			+ ",ifnull(b.dblAdvDeposite,0),ifnull(a.dblGrandTotal-b.dblAdvDeposite,0) as Balance ,a.dblDiscountAmt\n"
			+ ",ifnull(f.strAdvOrderTypeName,''),ifnull(d.strOperationType,''),d.strBillNo,'0'"
			+ ",'" + clsGlobalVarClass.gUserCode + "','" + clsGlobalVarClass.gClientCode + "'"
			+ ",a.strManualAdvOrderNo\n"
			+ "from tblqadvbookbillhd a left outer join tblbillhd d on a.strAdvBookingNo=d.strAdvBookingNo \n"
			+ "left outer join tblqadvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode \n"
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			+ "where  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' \n"
			+ "and d.strBillNo not in(select strBillNo from tblbillsettlementdtl) "
			+ " and d.strBillNo not in(select strBillNo from tblqbillsettlementdtl) "
			+ "and d.strBillNo is not null");

		sbSqlForBilled3.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y')\n"
			+ ",a.strPOSCode,c.strCustomerName,a.dblGrandTotal\n"
			+ ",ifnull(b.dblAdvDeposite,0),ifnull(a.dblGrandTotal-b.dblAdvDeposite,0) as Balance ,a.dblDiscountAmt\n"
			+ ",ifnull(f.strAdvOrderTypeName,''),ifnull(d.strOperationType,''),d.strBillNo,'0'"
			+ ",'" + clsGlobalVarClass.gUserCode + "','" + clsGlobalVarClass.gClientCode + "'"
			+ ",a.strManualAdvOrderNo\n"
			+ "from tblqadvbookbillhd a left outer join tblqbillhd d on a.strAdvBookingNo=d.strAdvBookingNo \n"
			+ "left outer join tblqadvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode \n"
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			+ "where  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' \n"
			+ "and d.strBillNo not in(select strBillNo from tblbillsettlementdtl) "
			+ " and d.strBillNo not in(select strBillNo from tblqbillsettlementdtl) "
			+ "and d.strSettelmentMode is not null");

		if (!cmbTransType.getSelectedItem().toString().trim().equals("All"))
		{
		    sbSqlForBilled1.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		    sbSqlForBilled2.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		    sbSqlForBilled3.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		}
	    }
	    else // For Both Option
	    {
		//settled
		sbSqlForBilled1.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y')\n"
			+ ",a.strPOSCode,c.strCustomerName,a.dblGrandTotal,ifnull(b.dblAdvDeposite,0)\n"
			+ ",ifnull(a.dblGrandTotal-b.dblAdvDeposite,0) as Balance ,a.dblDiscountAmt\n"
			+ ",ifnull(f.strAdvOrderTypeName,''),ifnull(d.strOperationType,''),d.strBillNo,'0'"
			+ ",'" + clsGlobalVarClass.gUserCode + "','" + clsGlobalVarClass.gClientCode + "'"
			+ ",a.strManualAdvOrderNo\n"
			+ "from tbladvbookbillhd a left outer join tblbillhd d on a.strAdvBookingNo=d.strAdvBookingNo \n"
			+ "left outer join tbladvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode \n"
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			+ "where  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' \n"
			+ "and d.strBillNo is not null and LENGTH(d.strSettelmentMode)>0 ");

		sbSqlForBilled2.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y')\n"
			+ ",a.strPOSCode,c.strCustomerName,a.dblGrandTotal\n"
			+ ",ifnull(b.dblAdvDeposite,0),ifnull(a.dblGrandTotal-b.dblAdvDeposite,0) as Balance ,a.dblDiscountAmt\n"
			+ ",ifnull(f.strAdvOrderTypeName,''),ifnull(d.strOperationType,''),d.strBillNo,'0'"
			+ ",'" + clsGlobalVarClass.gUserCode + "','" + clsGlobalVarClass.gClientCode + "'"
			+ ",a.strManualAdvOrderNo\n"
			+ "from tblqadvbookbillhd a left outer join tblbillhd d on a.strAdvBookingNo=d.strAdvBookingNo \n"
			+ "left outer join tblqadvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode \n"
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			+ "where  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' \n"
			+ "and d.strBillNo is not null and LENGTH(d.strSettelmentMode)>0 ");

		sbSqlForBilled3.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y')\n"
			+ ",a.strPOSCode,c.strCustomerName,a.dblGrandTotal\n"
			+ ",ifnull(b.dblAdvDeposite,0),ifnull(a.dblGrandTotal-b.dblAdvDeposite,0) as Balance ,a.dblDiscountAmt\n"
			+ ",ifnull(f.strAdvOrderTypeName,''),ifnull(d.strOperationType,''),d.strBillNo,'0'"
			+ ",'" + clsGlobalVarClass.gUserCode + "','" + clsGlobalVarClass.gClientCode + "'"
			+ ",a.strManualAdvOrderNo\n"
			+ "from tblqadvbookbillhd a left outer join tblqbillhd d on a.strAdvBookingNo=d.strAdvBookingNo \n"
			+ "left outer join tblqadvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode \n"
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			+ "where Date(a.dteOrderFor) between '" + fromDate + "' and '" + toDate + "' \n"
			+ "and d.strSettelmentMode is not null and LENGTH(d.strSettelmentMode)>0 ");

		//billed
		sbSqlForBilled11.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y')\n"
			+ ",a.strPOSCode,c.strCustomerName,a.dblGrandTotal,ifnull(b.dblAdvDeposite,0)\n"
			+ ",ifnull(a.dblGrandTotal-b.dblAdvDeposite,0) as Balance ,a.dblDiscountAmt\n"
			+ ",ifnull(f.strAdvOrderTypeName,''),ifnull(d.strOperationType,''),d.strBillNo,'0'"
			+ ",'" + clsGlobalVarClass.gUserCode + "','" + clsGlobalVarClass.gClientCode + "'"
			+ ",a.strManualAdvOrderNo\n"
			+ "from tbladvbookbillhd a left outer join tblbillhd d on a.strAdvBookingNo=d.strAdvBookingNo \n"
			+ "left outer join tbladvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode \n"
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			+ "where Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' \n"
			+ "and d.strBillNo not in(select strBillNo from tblbillsettlementdtl) "
			+ " and d.strBillNo not in(select strBillNo from tblqbillsettlementdtl) "
			+ "and d.strBillNo is not null");

		sbSqlForBilled22.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y')\n"
			+ ",a.strPOSCode,c.strCustomerName,a.dblGrandTotal\n"
			+ ",ifnull(b.dblAdvDeposite,0),ifnull(a.dblGrandTotal-b.dblAdvDeposite,0) as Balance ,a.dblDiscountAmt\n"
			+ ",ifnull(f.strAdvOrderTypeName,''),ifnull(d.strOperationType,''),d.strBillNo,'0'"
			+ ",'" + clsGlobalVarClass.gUserCode + "','" + clsGlobalVarClass.gClientCode + "'"
			+ ",a.strManualAdvOrderNo\n"
			+ "from tblqadvbookbillhd a left outer join tblbillhd d on a.strAdvBookingNo=d.strAdvBookingNo \n"
			+ "left outer join tblqadvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode \n"
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			+ "where  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' \n"
			+ "and d.strBillNo not in(select strBillNo from tblbillsettlementdtl) "
			+ " and d.strBillNo not in(select strBillNo from tblqbillsettlementdtl) "
			+ "and d.strBillNo is not null");

		sbSqlForBilled33.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y')\n"
			+ ",a.strPOSCode,c.strCustomerName,a.dblGrandTotal\n"
			+ ",ifnull(b.dblAdvDeposite,0),ifnull(a.dblGrandTotal-b.dblAdvDeposite,0) as Balance ,a.dblDiscountAmt\n"
			+ ",ifnull(f.strAdvOrderTypeName,''),ifnull(d.strOperationType,''),d.strBillNo,'0'"
			+ ",'" + clsGlobalVarClass.gUserCode + "','" + clsGlobalVarClass.gClientCode + "'"
			+ ",a.strManualAdvOrderNo\n"
			+ "from tblqadvbookbillhd a left outer join tblqbillhd d on a.strAdvBookingNo=d.strAdvBookingNo \n"
			+ "left outer join tblqadvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode \n"
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			+ "where  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' \n"
			+ "and d.strBillNo not in(select strBillNo from tblbillsettlementdtl) "
			+ " and d.strBillNo not in(select strBillNo from tblqbillsettlementdtl) "
			+ "and d.strSettelmentMode is not null");

		if (!cmbTransType.getSelectedItem().toString().trim().equals("All"))
		{
		    //settled
		    sbSqlForBilled1.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		    sbSqlForBilled2.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		    sbSqlForBilled3.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		    //billed
		    sbSqlForBilled11.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		    sbSqlForBilled22.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		    sbSqlForBilled33.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		}

		//opened
		sbSqlForOpen.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y')\n"
			+ ",a.strPOSCode,c.strCustomerName,a.dblGrandTotal,ifnull(b.dblAdvDeposite,0)\n"
			+ ",ifnull(a.dblGrandTotal-b.dblAdvDeposite,0) as Balance ,a.dblDiscountAmt\n"
			+ ",ifnull(f.strAdvOrderTypeName,''),ifnull(d.strOperationType,''),ifnull(d.strBillNo,''),'0'"
			+ ",'" + clsGlobalVarClass.gUserCode + "','" + clsGlobalVarClass.gClientCode + "'"
			+ ",a.strManualAdvOrderNo\n"
			+ "from tbladvbookbillhd a left outer join tblbillhd d on a.strAdvBookingNo=d.strAdvBookingNo \n"
			+ "left outer join tbladvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode \n"
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			+ "where  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' \n"
			+ "and d.strBillNo is null");

		if (clsGlobalVarClass.gHOPOSType.equals("HOPOS"))
		{
		    sbSqlForOpen.setLength(0);
		    sbSqlForOpen.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y')\n"
			    + ",a.strPOSCode,c.strCustomerName,a.dblGrandTotal,ifnull(b.dblAdvDeposite,0)\n"
			    + ",ifnull(a.dblGrandTotal-b.dblAdvDeposite,0) as Balance ,a.dblDiscountAmt\n"
			    + ",ifnull(f.strAdvOrderTypeName,''),ifnull(d.strOperationType,''),ifnull(d.strBillNo,''),'0'"
			    + ",'" + clsGlobalVarClass.gUserCode + "','" + clsGlobalVarClass.gClientCode + "'"
			    + ",a.strManualAdvOrderNo\n"
			    + "from tblqadvbookbillhd a left outer join tblbillhd d on a.strAdvBookingNo=d.strAdvBookingNo \n"
			    + "left outer join tblqadvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo "
			    + " left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode \n"
			    + "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			    + "where  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' \n"
			    + "and d.strBillNo is null");
		}
	    }

	    if (!"All".equals(pos))
	    {
		if (sbSqlForOpen.length() > 0)
		{
		    //opened
		    sbSqlForOpen.append(" and a.strPOSCode='" + pos + "' ");
		}
		if (sbSqlForBilled1.length() > 0)
		{
		    //settled
		    sbSqlForBilled1.append(" and a.strPOSCode='" + pos + "' ");
		}
		if (sbSqlForBilled2.length() > 0)
		{
		    //settled
		    sbSqlForBilled2.append(" and a.strPOSCode='" + pos + "' ");
		}
		if (sbSqlForBilled3.length() > 0)
		{
		    //settled
		    sbSqlForBilled3.append(" and a.strPOSCode='" + pos + "' ");
		}
		if (sbSqlForBilled11.length() > 0)
		{
		    //billed
		    sbSqlForBilled11.append(" and a.strPOSCode='" + pos + "' ");
		}
		if (sbSqlForBilled22.length() > 0)
		{
		    //billed
		    sbSqlForBilled22.append(" and a.strPOSCode='" + pos + "' ");
		}
		if (sbSqlForBilled33.length() > 0)
		{
		    //billed
		    sbSqlForBilled33.append(" and a.strPOSCode='" + pos + "' ");
		}
	    }
	    if (!cmbOrderMode.getSelectedItem().toString().trim().equals("All"))
	    {
		if (hmAdvOrderType.size() > 0)
		{
		    String orderMode = cmbOrderMode.getSelectedItem().toString().trim();
		    if (sbSqlForOpen.length() > 0)
		    {
			//opened
			sbSqlForOpen.append(" and f.strAdvOrderTypeCode='" + hmAdvOrderType.get(orderMode) + "' ");
		    }
		    if (sbSqlForBilled1.length() > 0)
		    {
			//settled
			sbSqlForBilled1.append(" and f.strAdvOrderTypeCode='" + hmAdvOrderType.get(orderMode) + "' ");
		    }
		    if (sbSqlForBilled2.length() > 0)
		    {
			//settled
			sbSqlForBilled2.append(" and f.strAdvOrderTypeCode='" + hmAdvOrderType.get(orderMode) + "' ");
		    }
		    if (sbSqlForBilled3.length() > 0)
		    {
			//settled
			sbSqlForBilled3.append(" and f.strAdvOrderTypeCode='" + hmAdvOrderType.get(orderMode) + "' ");
		    }
		    if (sbSqlForBilled11.length() > 0)
		    {
			//billed
			sbSqlForBilled11.append(" and f.strAdvOrderTypeCode='" + hmAdvOrderType.get(orderMode) + "' ");
		    }
		    if (sbSqlForBilled22.length() > 0)
		    {
			//billed
			sbSqlForBilled22.append(" and f.strAdvOrderTypeCode='" + hmAdvOrderType.get(orderMode) + "' ");
		    }
		    if (sbSqlForBilled33.length() > 0)
		    {
			//billed
			sbSqlForBilled33.append(" and f.strAdvOrderTypeCode='" + hmAdvOrderType.get(orderMode) + "' ");
		    }
		}
	    }

	    boolean flgRecords = false;
	    String userCode = clsGlobalVarClass.gUserCode;
	    ResultSet rsAdvancedOrderForOpen = null;
	    ResultSet rsAdvancedOrderDataBilled1 = null;
	    ResultSet rsAdvancedOrderDataBilled2 = null;
	    ResultSet rsAdvancedOrderDataBilled3 = null;
	    ResultSet rsAdvancedOrderDataBilled11 = null;
	    ResultSet rsAdvancedOrderDataBilled22 = null;
	    ResultSet rsAdvancedOrderDataBilled33 = null;

	    if (sbSqlForOpen.length() > 0)
	    {
		rsAdvancedOrderForOpen = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlForOpen.toString());
		while (rsAdvancedOrderForOpen.next())
		{
		    flgRecords = true;

		    clsAdvBookBillHd objAdvBookColumns = new clsAdvBookBillHd();
		    objAdvBookColumns.setStrAdvBookingNo(rsAdvancedOrderForOpen.getString(1));
		    objAdvBookColumns.setDteAdvBookingDate(rsAdvancedOrderForOpen.getString(2));
		    objAdvBookColumns.setDteOrderFor(rsAdvancedOrderForOpen.getString(3));
		    objAdvBookColumns.setStrPOSCode(rsAdvancedOrderForOpen.getString(4));
		    objAdvBookColumns.setStrCustomerName(rsAdvancedOrderForOpen.getString(5));
		    objAdvBookColumns.setDblGrandTotal(rsAdvancedOrderForOpen.getDouble(6));
		    objAdvBookColumns.setDblAdvDeposite(rsAdvancedOrderForOpen.getDouble(7));
		    objAdvBookColumns.setBalance(rsAdvancedOrderForOpen.getDouble(8));
		    objAdvBookColumns.setDblDiscountAmt(rsAdvancedOrderForOpen.getDouble(9));
		    objAdvBookColumns.setStrAdvOrderTypeName(rsAdvancedOrderForOpen.getString(10));
		    objAdvBookColumns.setStrOperationType(rsAdvancedOrderForOpen.getString(11));
		    objAdvBookColumns.setBillNo(rsAdvancedOrderForOpen.getString(12));
		    objAdvBookColumns.setStrManualAdvOrderNo(rsAdvancedOrderForOpen.getString(14));

		    hmAdvOrderData.put(rsAdvancedOrderForOpen.getString(1), objAdvBookColumns);
		}
		rsAdvancedOrderForOpen.close();
	    }

	    if (sbSqlForBilled1.length() > 0)
	    {
		rsAdvancedOrderDataBilled1 = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlForBilled1.toString());
		while (rsAdvancedOrderDataBilled1.next())
		{
		    flgRecords = true;

		    clsAdvBookBillHd objAdvBookColumns = new clsAdvBookBillHd();
		    objAdvBookColumns.setStrAdvBookingNo(rsAdvancedOrderDataBilled1.getString(1));
		    objAdvBookColumns.setDteAdvBookingDate(rsAdvancedOrderDataBilled1.getString(2));
		    objAdvBookColumns.setDteOrderFor(rsAdvancedOrderDataBilled1.getString(3));
		    objAdvBookColumns.setStrPOSCode(rsAdvancedOrderDataBilled1.getString(4));
		    objAdvBookColumns.setStrCustomerName(rsAdvancedOrderDataBilled1.getString(5));
		    objAdvBookColumns.setDblGrandTotal(rsAdvancedOrderDataBilled1.getDouble(6));
		    objAdvBookColumns.setDblAdvDeposite(rsAdvancedOrderDataBilled1.getDouble(7));
		    objAdvBookColumns.setBalance(rsAdvancedOrderDataBilled1.getDouble(8));
		    objAdvBookColumns.setDblDiscountAmt(rsAdvancedOrderDataBilled1.getDouble(9));
		    objAdvBookColumns.setStrAdvOrderTypeName(rsAdvancedOrderDataBilled1.getString(10));
		    objAdvBookColumns.setStrOperationType(rsAdvancedOrderDataBilled1.getString(11));
		    objAdvBookColumns.setBillNo(rsAdvancedOrderDataBilled1.getString(12));
		    objAdvBookColumns.setStrManualAdvOrderNo(rsAdvancedOrderDataBilled1.getString(14));

		    hmAdvOrderData.put(rsAdvancedOrderDataBilled1.getString(1), objAdvBookColumns);
		}
		rsAdvancedOrderDataBilled1.close();
	    }
	    if (sbSqlForBilled2.length() > 0)
	    {
		rsAdvancedOrderDataBilled2 = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlForBilled2.toString());
		//settled
		while (rsAdvancedOrderDataBilled2.next())
		{

		    flgRecords = true;

		    clsAdvBookBillHd objAdvBookColumns = new clsAdvBookBillHd();
		    objAdvBookColumns.setStrAdvBookingNo(rsAdvancedOrderDataBilled2.getString(1));
		    objAdvBookColumns.setDteAdvBookingDate(rsAdvancedOrderDataBilled2.getString(2));
		    objAdvBookColumns.setDteOrderFor(rsAdvancedOrderDataBilled2.getString(3));
		    objAdvBookColumns.setStrPOSCode(rsAdvancedOrderDataBilled2.getString(4));
		    objAdvBookColumns.setStrCustomerName(rsAdvancedOrderDataBilled2.getString(5));
		    objAdvBookColumns.setDblGrandTotal(rsAdvancedOrderDataBilled2.getDouble(6));
		    objAdvBookColumns.setDblAdvDeposite(rsAdvancedOrderDataBilled2.getDouble(7));
		    objAdvBookColumns.setBalance(rsAdvancedOrderDataBilled2.getDouble(8));
		    objAdvBookColumns.setDblDiscountAmt(rsAdvancedOrderDataBilled2.getDouble(9));
		    objAdvBookColumns.setStrAdvOrderTypeName(rsAdvancedOrderDataBilled2.getString(10));
		    objAdvBookColumns.setStrOperationType(rsAdvancedOrderDataBilled2.getString(11));
		    objAdvBookColumns.setBillNo(rsAdvancedOrderDataBilled2.getString(12));
		    objAdvBookColumns.setStrManualAdvOrderNo(rsAdvancedOrderDataBilled2.getString(14));

		    hmAdvOrderData.put(rsAdvancedOrderDataBilled2.getString(1), objAdvBookColumns);
		}
		rsAdvancedOrderDataBilled2.close();
	    }
	    if (sbSqlForBilled3.length() > 0)
	    {
		rsAdvancedOrderDataBilled3 = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlForBilled3.toString());
		//settled
		while (rsAdvancedOrderDataBilled3.next())
		{

		    flgRecords = true;

		    clsAdvBookBillHd objAdvBookColumns = new clsAdvBookBillHd();
		    objAdvBookColumns.setStrAdvBookingNo(rsAdvancedOrderDataBilled3.getString(1));
		    objAdvBookColumns.setDteAdvBookingDate(rsAdvancedOrderDataBilled3.getString(2));
		    objAdvBookColumns.setDteOrderFor(rsAdvancedOrderDataBilled3.getString(3));
		    objAdvBookColumns.setStrPOSCode(rsAdvancedOrderDataBilled3.getString(4));
		    objAdvBookColumns.setStrCustomerName(rsAdvancedOrderDataBilled3.getString(5));
		    objAdvBookColumns.setDblGrandTotal(rsAdvancedOrderDataBilled3.getDouble(6));
		    objAdvBookColumns.setDblAdvDeposite(rsAdvancedOrderDataBilled3.getDouble(7));
		    objAdvBookColumns.setBalance(rsAdvancedOrderDataBilled3.getDouble(8));
		    objAdvBookColumns.setDblDiscountAmt(rsAdvancedOrderDataBilled3.getDouble(9));
		    objAdvBookColumns.setStrAdvOrderTypeName(rsAdvancedOrderDataBilled3.getString(10));
		    objAdvBookColumns.setStrOperationType(rsAdvancedOrderDataBilled3.getString(11));
		    objAdvBookColumns.setBillNo(rsAdvancedOrderDataBilled3.getString(12));
		    objAdvBookColumns.setStrManualAdvOrderNo(rsAdvancedOrderDataBilled3.getString(14));

		    hmAdvOrderData.put(rsAdvancedOrderDataBilled3.getString(1), objAdvBookColumns);
		}
		rsAdvancedOrderDataBilled3.close();
	    }

	    if (sbSqlForBilled11.length() > 0)
	    {
		rsAdvancedOrderDataBilled11 = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlForBilled11.toString());
		//billed
		while (rsAdvancedOrderDataBilled11.next())
		{

		    flgRecords = true;

		    clsAdvBookBillHd objAdvBookColumns = new clsAdvBookBillHd();
		    objAdvBookColumns.setStrAdvBookingNo(rsAdvancedOrderDataBilled11.getString(1));
		    objAdvBookColumns.setDteAdvBookingDate(rsAdvancedOrderDataBilled11.getString(2));
		    objAdvBookColumns.setDteOrderFor(rsAdvancedOrderDataBilled11.getString(3));
		    objAdvBookColumns.setStrPOSCode(rsAdvancedOrderDataBilled11.getString(4));
		    objAdvBookColumns.setStrCustomerName(rsAdvancedOrderDataBilled11.getString(5));
		    objAdvBookColumns.setDblGrandTotal(rsAdvancedOrderDataBilled11.getDouble(6));
		    objAdvBookColumns.setDblAdvDeposite(rsAdvancedOrderDataBilled11.getDouble(7));
		    objAdvBookColumns.setBalance(rsAdvancedOrderDataBilled11.getDouble(8));
		    objAdvBookColumns.setDblDiscountAmt(rsAdvancedOrderDataBilled11.getDouble(9));
		    objAdvBookColumns.setStrAdvOrderTypeName(rsAdvancedOrderDataBilled11.getString(10));
		    objAdvBookColumns.setStrOperationType(rsAdvancedOrderDataBilled11.getString(11));
		    objAdvBookColumns.setBillNo(rsAdvancedOrderDataBilled11.getString(12));
		    objAdvBookColumns.setStrManualAdvOrderNo(rsAdvancedOrderDataBilled11.getString(14));

		    hmAdvOrderData.put(rsAdvancedOrderDataBilled11.getString(1), objAdvBookColumns);
		}
		rsAdvancedOrderDataBilled11.close();
	    }
	    if (sbSqlForBilled22.length() > 0)
	    {
		rsAdvancedOrderDataBilled22 = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlForBilled22.toString());
		//billed
		while (rsAdvancedOrderDataBilled22.next())
		{

		    flgRecords = true;

		    clsAdvBookBillHd objAdvBookColumns = new clsAdvBookBillHd();
		    objAdvBookColumns.setStrAdvBookingNo(rsAdvancedOrderDataBilled22.getString(1));
		    objAdvBookColumns.setDteAdvBookingDate(rsAdvancedOrderDataBilled22.getString(2));
		    objAdvBookColumns.setDteOrderFor(rsAdvancedOrderDataBilled22.getString(3));
		    objAdvBookColumns.setStrPOSCode(rsAdvancedOrderDataBilled22.getString(4));
		    objAdvBookColumns.setStrCustomerName(rsAdvancedOrderDataBilled22.getString(5));
		    objAdvBookColumns.setDblGrandTotal(rsAdvancedOrderDataBilled22.getDouble(6));
		    objAdvBookColumns.setDblAdvDeposite(rsAdvancedOrderDataBilled22.getDouble(7));
		    objAdvBookColumns.setBalance(rsAdvancedOrderDataBilled22.getDouble(8));
		    objAdvBookColumns.setDblDiscountAmt(rsAdvancedOrderDataBilled22.getDouble(9));
		    objAdvBookColumns.setStrAdvOrderTypeName(rsAdvancedOrderDataBilled22.getString(10));
		    objAdvBookColumns.setStrOperationType(rsAdvancedOrderDataBilled22.getString(11));
		    objAdvBookColumns.setBillNo(rsAdvancedOrderDataBilled22.getString(12));
		    objAdvBookColumns.setStrManualAdvOrderNo(rsAdvancedOrderDataBilled22.getString(14));
		    hmAdvOrderData.put(rsAdvancedOrderDataBilled22.getString(1), objAdvBookColumns);
		}
		rsAdvancedOrderDataBilled22.close();
	    }

	    if (sbSqlForBilled33.length() > 0)
	    {
		rsAdvancedOrderDataBilled33 = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlForBilled33.toString());
		//settled
		while (rsAdvancedOrderDataBilled33.next())
		{
		    flgRecords = true;

		    clsAdvBookBillHd objAdvBookColumns = new clsAdvBookBillHd();
		    objAdvBookColumns.setStrAdvBookingNo(rsAdvancedOrderDataBilled33.getString(1));
		    objAdvBookColumns.setDteAdvBookingDate(rsAdvancedOrderDataBilled33.getString(2));
		    objAdvBookColumns.setDteOrderFor(rsAdvancedOrderDataBilled33.getString(3));
		    objAdvBookColumns.setStrPOSCode(rsAdvancedOrderDataBilled33.getString(4));
		    objAdvBookColumns.setStrCustomerName(rsAdvancedOrderDataBilled33.getString(5));
		    objAdvBookColumns.setDblGrandTotal(rsAdvancedOrderDataBilled33.getDouble(6));
		    objAdvBookColumns.setDblAdvDeposite(rsAdvancedOrderDataBilled33.getDouble(7));
		    objAdvBookColumns.setBalance(rsAdvancedOrderDataBilled33.getDouble(8));
		    objAdvBookColumns.setDblDiscountAmt(rsAdvancedOrderDataBilled33.getDouble(9));
		    objAdvBookColumns.setStrAdvOrderTypeName(rsAdvancedOrderDataBilled33.getString(10));
		    objAdvBookColumns.setStrOperationType(rsAdvancedOrderDataBilled33.getString(11));
		    objAdvBookColumns.setBillNo(rsAdvancedOrderDataBilled33.getString(12));
		    objAdvBookColumns.setStrManualAdvOrderNo(rsAdvancedOrderDataBilled33.getString(14));

		    hmAdvOrderData.put(rsAdvancedOrderDataBilled33.getString(1), objAdvBookColumns);
		}
		rsAdvancedOrderDataBilled33.close();
	    }

	    Object[] records = new Object[14];
	    for (clsAdvBookBillHd objAdvBookBillHd : hmAdvOrderData.values())
	    {

		records[0] = objAdvBookBillHd.getStrAdvBookingNo();
		records[1] = objAdvBookBillHd.getDteAdvBookingDate();
		records[2] = objAdvBookBillHd.getStrCustomerName();
		records[3] = objAdvBookBillHd.getDteOrderFor();
		records[4] = objAdvBookBillHd.getStrAdvOrderTypeName();
		records[5] = objAdvBookBillHd.getStrOperationType();
		records[6] = gDecimalFormat.format(objAdvBookBillHd.getDblDiscountAmt());
		records[7] = gDecimalFormat.format(objAdvBookBillHd.getDblGrandTotal());
		records[8] = gDecimalFormat.format(objAdvBookBillHd.getDblAdvDeposite());
		records[9] = gDecimalFormat.format(objAdvBookBillHd.getBalance());
		records[10] = objAdvBookBillHd.getStrManualAdvOrderNo();
		sumGt = sumGt + objAdvBookBillHd.getDblGrandTotal();
		AdvAmt = AdvAmt + objAdvBookBillHd.getDblAdvDeposite();
		sumBal = sumBal + objAdvBookBillHd.getBalance();
		sumDisc = sumDisc + objAdvBookBillHd.getDblDiscountAmt();

		dm.addRow(records);

	    }

	    tblAdvOrder.setModel(dm);
	    Object[] total
		    =
		    {
			"Total", gDecimalFormat.format(sumDisc), gDecimalFormat.format(sumGt), gDecimalFormat.format(AdvAmt), gDecimalFormat.format(sumBal)
		    };
	    dm2.addRow(total);
	    tblTotal.setModel(dm2);

	    DefaultTableCellRenderer rightRenderer1 = new DefaultTableCellRenderer();
	    rightRenderer1.setHorizontalAlignment(JLabel.RIGHT);

	    tblAdvOrder.getColumnModel().getColumn(6).setCellRenderer(rightRenderer1);
	    tblAdvOrder.getColumnModel().getColumn(7).setCellRenderer(rightRenderer1);
	    tblAdvOrder.getColumnModel().getColumn(8).setCellRenderer(rightRenderer1);
	    tblAdvOrder.getColumnModel().getColumn(9).setCellRenderer(rightRenderer1);
	    tblAdvOrder.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

	    tblAdvOrder.getColumnModel().getColumn(0).setPreferredWidth(80);// Adv Order No
	    tblAdvOrder.getColumnModel().getColumn(1).setPreferredWidth(80);// Adv Order Date
	    tblAdvOrder.getColumnModel().getColumn(2).setPreferredWidth(120);// Customer
	    tblAdvOrder.getColumnModel().getColumn(3).setPreferredWidth(80);// Order For
	    tblAdvOrder.getColumnModel().getColumn(4).setPreferredWidth(80);// Order type
	    tblAdvOrder.getColumnModel().getColumn(5).setPreferredWidth(80);// Operation Type
	    tblAdvOrder.getColumnModel().getColumn(6).setPreferredWidth(60);// Disc Amt
	    tblAdvOrder.getColumnModel().getColumn(7).setPreferredWidth(70);// GT Amt
	    tblAdvOrder.getColumnModel().getColumn(8).setPreferredWidth(70);// Adv Amt
	    tblAdvOrder.getColumnModel().getColumn(9).setPreferredWidth(70);// Balance
	    tblAdvOrder.getColumnModel().getColumn(10).setPreferredWidth(70);// Manual Adv Order No

	    tblTotal.getColumnModel().getColumn(1).setCellRenderer(rightRenderer1);
	    tblTotal.getColumnModel().getColumn(2).setCellRenderer(rightRenderer1);
	    tblTotal.getColumnModel().getColumn(3).setCellRenderer(rightRenderer1);
	    tblTotal.getColumnModel().getColumn(4).setCellRenderer(rightRenderer1);
	    tblTotal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    tblTotal.getColumnModel().getColumn(0).setPreferredWidth(510);
	    tblTotal.getColumnModel().getColumn(1).setPreferredWidth(60);
	    tblTotal.getColumnModel().getColumn(2).setPreferredWidth(70);
	    tblTotal.getColumnModel().getColumn(3).setPreferredWidth(70);
	    tblTotal.getColumnModel().getColumn(4).setPreferredWidth(70);
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    /*
     * Filter the Selected Item Show the Report ItemWise
     */
    private void funItemWise()
    {
	Map<String, clsAdvBookBillHd> hmAdvOrderData = new HashMap<String, clsAdvBookBillHd>();
	try
	{
	    String posCode = cmbPosCode.getSelectedItem().toString();
	    StringBuilder sb = new StringBuilder(posCode);
	    int len = posCode.length();
	    int lastInd = sb.lastIndexOf(" ");
	    String pos = sb.substring(lastInd + 1, len).toString();

	    fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());
	    toDate = objUtility.funGetFromToDate(dteToDate.getDate());
	    double sumGt = 0.00;
	    double sumQty = 0.00;
	    dm.getDataVector().removeAllElements();
	    dm = new DefaultTableModel();
	    dm.addColumn("Adv Ord No");
	    dm.addColumn("Order For");
	    dm.addColumn("Bill Date");
	    dm.addColumn("Customer Name");
	    dm.addColumn("Order Type");
	    dm.addColumn("Operation Type");
	    dm.addColumn("Item Name");
	    dm.addColumn("Qty");
	    dm.addColumn("Amount");
	    dm.addColumn("Manual Adv Order No");
	    DefaultTableModel dm2 = new DefaultTableModel();
	    dm2.getDataVector().removeAllElements();
	    dm2.addColumn("");
	    dm2.addColumn("");
	    dm2.addColumn("");
	    dm2.addColumn("");

	    StringBuilder sbSqlForOpen = new StringBuilder(); // Adv Live only
	    StringBuilder sbSqlForBilled1 = new StringBuilder(); // Adv Live + Bill Live
	    StringBuilder sbSqlForBilled2 = new StringBuilder(); //Adv QFile + Bill Live
	    StringBuilder sbSqlForBilled3 = new StringBuilder(); //Adv QFile + Bill QFile
	    StringBuilder sbSqlForBilled11 = new StringBuilder(); // Adv Live + Bill Live
	    StringBuilder sbSqlForBilled22 = new StringBuilder(); //Adv QFile + Bill Live
	    StringBuilder sbSqlForBilled33 = new StringBuilder(); //Adv QFile + Bill QFile

	    sbSqlForOpen.setLength(0);
	    sbSqlForBilled1.setLength(0);
	    sbSqlForBilled2.setLength(0);
	    sbSqlForBilled3.setLength(0);
	    sbSqlForBilled11.setLength(0);
	    sbSqlForBilled22.setLength(0);
	    sbSqlForBilled33.setLength(0);

	    if (cmbStatus.getSelectedItem().equals("Open")) // For Open Adv Order Option
	    {
		sbSqlForOpen.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y') ,DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y'),a.strPOSCode,c.strCustomerName\n"
			+ ",e.strItemName,e.dblQuantity,e.dblAmount,'',ifnull(f.strAdvOrderTypeName,'')\n"
			+ ",ifnull(d.strOperationType,''),'','0','" + clsGlobalVarClass.gUserCode + "'"
			+ ",'" + clsGlobalVarClass.gClientCode + "',a.strManualAdvOrderNo \n"
			+ "from tbladvbookbillhd a left outer join tblbillhd d on a.strAdvBookingNo=d.strAdvBookingNo \n"
			+ "left outer join tbladvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo \n"
			+ "left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode \n"
			+ "left outer join tbladvbookbilldtl e on e.strAdvBookingNo=a.strAdvBookingNo \n"
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			+ "where Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "'\n"
			+ "and d.strBillNo is null");

		if (clsGlobalVarClass.gHOPOSType.equals("HOPOS"))
		{
		    sbSqlForOpen.setLength(0);
		    sbSqlForOpen.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y'),a.strPOSCode,c.strCustomerName\n"
			    + ",e.strItemName,e.dblQuantity,e.dblAmount,'',ifnull(f.strAdvOrderTypeName,'')\n"
			    + ",ifnull(d.strOperationType,''),'','0','" + clsGlobalVarClass.gUserCode + "'"
			    + ",'" + clsGlobalVarClass.gClientCode + "',a.strManualAdvOrderNo \n"
			    + "from tblqadvbookbillhd a left outer join tblbillhd d on a.strAdvBookingNo=d.strAdvBookingNo \n"
			    + "left outer join tblqadvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo \n"
			    + "left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode \n"
			    + "left outer join tblqadvbookbilldtl e on e.strAdvBookingNo=a.strAdvBookingNo \n"
			    + "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			    + "where Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "'\n"
			    + "and d.strBillNo is null");
		}
	    }
	    else if (cmbStatus.getSelectedItem().equals("Settled")) // For Settled Option
	    {
		sbSqlForBilled1.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y'),a.strPOSCode,c.strCustomerName\n"
			+ ",e.strItemName,e.dblQuantity,e.dblAmount,'',ifnull(f.strAdvOrderTypeName,'')\n"
			+ ",ifnull(d.strOperationType,''),'','0','" + clsGlobalVarClass.gUserCode + "'"
			+ ",'" + clsGlobalVarClass.gClientCode + "',a.strManualAdvOrderNo \n"
			+ "from tbladvbookbillhd a left outer join tblbillhd d on a.strAdvBookingNo=d.strAdvBookingNo \n"
			+ "left outer join tbladvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo \n"
			+ "left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode \n"
			+ "left outer join tbladvbookbilldtl e on e.strAdvBookingNo=a.strAdvBookingNo \n"
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			+ "inner join tblbillsettlementdtl g on d.strBillNo=g.strBillNo \n"
			+ "where Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "'\n"
			+ "and d.strBillNo is not null");

		sbSqlForBilled2.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y'),a.strPOSCode,c.strCustomerName\n"
			+ ",e.strItemName,e.dblQuantity,e.dblAmount,'',ifnull(f.strAdvOrderTypeName,'')\n"
			+ ",ifnull(d.strOperationType,''),'','0','" + clsGlobalVarClass.gUserCode + "'"
			+ ",'" + clsGlobalVarClass.gClientCode + "',a.strManualAdvOrderNo \n"
			+ "from tblqadvbookbillhd a left outer join tblbillhd d on a.strAdvBookingNo=d.strAdvBookingNo \n"
			+ "left outer join tblqadvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo \n"
			+ "left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode \n"
			+ "left outer join tblqadvbookbilldtl e on e.strAdvBookingNo=a.strAdvBookingNo \n"
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			+ "inner join tblbillsettlementdtl g on d.strBillNo=g.strBillNo \n"
			+ "where Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "'");

		sbSqlForBilled3.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y'),a.strPOSCode,c.strCustomerName\n"
			+ ",e.strItemName,e.dblQuantity,e.dblAmount,'',ifnull(f.strAdvOrderTypeName,'')\n"
			+ ",ifnull(d.strOperationType,''),'','0','" + clsGlobalVarClass.gUserCode + "'"
			+ ",'" + clsGlobalVarClass.gClientCode + "',a.strManualAdvOrderNo \n"
			+ "from tblqadvbookbillhd a left outer join tblqbillhd d on a.strAdvBookingNo=d.strAdvBookingNo \n"
			+ "left outer join tblqadvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo \n"
			+ "left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode \n"
			+ "left outer join tblqadvbookbilldtl e on e.strAdvBookingNo=a.strAdvBookingNo \n"
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			+ "inner join tblqbillsettlementdtl g on d.strBillNo=g.strBillNo \n"
			+ "where Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' "
			+ "and d.strSettelmentMode is not null");

		if (!cmbTransType.getSelectedItem().toString().trim().equals("All"))
		{
		    sbSqlForBilled1.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		    sbSqlForBilled2.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		    sbSqlForBilled3.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		}
	    }
	    else if (cmbStatus.getSelectedItem().equals("Billed")) // For Billed Option
	    {
		sbSqlForBilled1.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y'),a.strPOSCode,c.strCustomerName\n"
			+ ",e.strItemName,e.dblQuantity,e.dblAmount,'',ifnull(f.strAdvOrderTypeName,'')\n"
			+ ",ifnull(d.strOperationType,''),'','0','" + clsGlobalVarClass.gUserCode + "'"
			+ ",'" + clsGlobalVarClass.gClientCode + "',a.strManualAdvOrderNo \n"
			+ "from tbladvbookbillhd a left outer join tblbillhd d on a.strAdvBookingNo=d.strAdvBookingNo \n"
			+ "left outer join tbladvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo \n"
			+ "left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode \n"
			+ "left outer join tbladvbookbilldtl e on e.strAdvBookingNo=a.strAdvBookingNo \n"
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			+ "where Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' \n"
			+ "and d.strBillNo not in(select strBillNo from tblbillsettlementdtl)\n"
			+ " and d.strBillNo not in(select strBillNo from tblqbillsettlementdtl) "
			+ "and d.strBillNo is not null");

		sbSqlForBilled2.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y'),a.strPOSCode,c.strCustomerName \n"
			+ ",e.strItemName,e.dblQuantity,e.dblAmount,'',ifnull(f.strAdvOrderTypeName,'') \n"
			+ ",ifnull(d.strOperationType,''),'','0','" + clsGlobalVarClass.gUserCode + "'"
			+ ",'" + clsGlobalVarClass.gClientCode + "',a.strManualAdvOrderNo \n"
			+ "from tblqadvbookbillhd a left outer join tblbillhd d on a.strAdvBookingNo=d.strAdvBookingNo \n"
			+ "left outer join tblqadvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo \n"
			+ "left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode \n"
			+ "left outer join tblqadvbookbilldtl e on e.strAdvBookingNo=a.strAdvBookingNo \n"
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			+ "where Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' \n"
			+ "and d.strBillNo not in(select strBillNo from tblbillsettlementdtl) "
			+ " and d.strBillNo not in(select strBillNo from tblqbillsettlementdtl) ");

		sbSqlForBilled3.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y'),a.strPOSCode,c.strCustomerName\n"
			+ ",e.strItemName,e.dblQuantity,e.dblAmount,'',ifnull(f.strAdvOrderTypeName,'')\n"
			+ ",ifnull(d.strOperationType,''),'','0','" + clsGlobalVarClass.gUserCode + "'"
			+ ",'" + clsGlobalVarClass.gClientCode + "',a.strManualAdvOrderNo \n"
			+ "from tblqadvbookbillhd a left outer join tblqbillhd d on a.strAdvBookingNo=d.strAdvBookingNo \n"
			+ "left outer join tblqadvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo \n"
			+ "left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode \n"
			+ "left outer join tblqadvbookbilldtl e on e.strAdvBookingNo=a.strAdvBookingNo \n"
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			+ "where Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' \n"
			+ "and d.strBillNo not in(select strBillNo from tblqbillsettlementdtl) \n"
			+ "and d.strSettelmentMode is not null");

		if (!cmbTransType.getSelectedItem().toString().trim().equals("All"))
		{
		    sbSqlForBilled1.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		    sbSqlForBilled2.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		    sbSqlForBilled3.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		}
	    }
	    else // For All Option
	    {
		//settled
		sbSqlForBilled1.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y'),a.strPOSCode,c.strCustomerName\n"
			+ ",e.strItemName,e.dblQuantity,e.dblAmount,'',ifnull(f.strAdvOrderTypeName,'')\n"
			+ ",ifnull(d.strOperationType,''),'','0','" + clsGlobalVarClass.gUserCode + "'"
			+ ",'" + clsGlobalVarClass.gClientCode + "',a.strManualAdvOrderNo \n"
			+ "from tbladvbookbillhd a left outer join tblbillhd d on a.strAdvBookingNo=d.strAdvBookingNo \n"
			+ "left outer join tbladvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo \n"
			+ "left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode \n"
			+ "left outer join tbladvbookbilldtl e on e.strAdvBookingNo=a.strAdvBookingNo \n"
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			+ "inner join tblbillsettlementdtl g on d.strBillNo=g.strBillNo \n"
			+ "where Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "'\n"
			+ "and d.strBillNo is not null");

		sbSqlForBilled2.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y'),a.strPOSCode,c.strCustomerName\n"
			+ ",e.strItemName,e.dblQuantity,e.dblAmount,'',ifnull(f.strAdvOrderTypeName,'')\n"
			+ ",ifnull(d.strOperationType,''),'','0','" + clsGlobalVarClass.gUserCode + "'"
			+ ",'" + clsGlobalVarClass.gClientCode + "',a.strManualAdvOrderNo \n"
			+ "from tblqadvbookbillhd a left outer join tblbillhd d on a.strAdvBookingNo=d.strAdvBookingNo \n"
			+ "left outer join tblqadvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo \n"
			+ "left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode \n"
			+ "left outer join tblqadvbookbilldtl e on e.strAdvBookingNo=a.strAdvBookingNo \n"
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			+ "inner join tblbillsettlementdtl g on d.strBillNo=g.strBillNo \n"
			+ "where Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "'");

		sbSqlForBilled3.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y'),a.strPOSCode,c.strCustomerName\n"
			+ ",e.strItemName,e.dblQuantity,e.dblAmount,'',ifnull(f.strAdvOrderTypeName,'')\n"
			+ ",ifnull(d.strOperationType,''),'','0','" + clsGlobalVarClass.gUserCode + "'"
			+ ",'" + clsGlobalVarClass.gClientCode + "',a.strManualAdvOrderNo \n"
			+ "from tblqadvbookbillhd a left outer join tblqbillhd d on a.strAdvBookingNo=d.strAdvBookingNo \n"
			+ "left outer join tblqadvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo \n"
			+ "left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode \n"
			+ "left outer join tblqadvbookbilldtl e on e.strAdvBookingNo=a.strAdvBookingNo \n"
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			+ "inner join tblqbillsettlementdtl g on d.strBillNo=g.strBillNo \n"
			+ "where Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' "
			+ "and d.strSettelmentMode is not null");

		//billed
		sbSqlForBilled11.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y'),a.strPOSCode,c.strCustomerName\n"
			+ ",e.strItemName,e.dblQuantity,e.dblAmount,'',ifnull(f.strAdvOrderTypeName,'')\n"
			+ ",ifnull(d.strOperationType,''),'','0','" + clsGlobalVarClass.gUserCode + "'"
			+ ",'" + clsGlobalVarClass.gClientCode + "',a.strManualAdvOrderNo \n"
			+ "from tbladvbookbillhd a left outer join tblbillhd d on a.strAdvBookingNo=d.strAdvBookingNo \n"
			+ "left outer join tbladvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo \n"
			+ "left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode \n"
			+ "left outer join tbladvbookbilldtl e on e.strAdvBookingNo=a.strAdvBookingNo \n"
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			+ "where Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' \n"
			+ "and d.strBillNo not in(select strBillNo from tblbillsettlementdtl)\n"
			+ " and d.strBillNo not in(select strBillNo from tblqbillsettlementdtl) "
			+ "and d.strBillNo is not null");

		sbSqlForBilled22.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y'),a.strPOSCode,c.strCustomerName \n"
			+ ",e.strItemName,e.dblQuantity,e.dblAmount,'',ifnull(f.strAdvOrderTypeName,'') \n"
			+ ",ifnull(d.strOperationType,''),'','0','" + clsGlobalVarClass.gUserCode + "'"
			+ ",'" + clsGlobalVarClass.gClientCode + "',a.strManualAdvOrderNo \n"
			+ "from tblqadvbookbillhd a left outer join tblbillhd d on a.strAdvBookingNo=d.strAdvBookingNo \n"
			+ "left outer join tblqadvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo \n"
			+ "left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode \n"
			+ "left outer join tblqadvbookbilldtl e on e.strAdvBookingNo=a.strAdvBookingNo \n"
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			+ "where Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' \n"
			+ "and d.strBillNo not in(select strBillNo from tblbillsettlementdtl) "
			+ " and d.strBillNo not in(select strBillNo from tblqbillsettlementdtl) ");

		sbSqlForBilled33.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y'),a.strPOSCode"
			+ " ,c.strCustomerName,e.strItemName,e.dblQuantity,e.dblAmount,'',ifnull(f.strAdvOrderTypeName,'')"
			+ " ,ifnull(d.strOperationType,''),'','0','" + clsGlobalVarClass.gUserCode + "'"
			+ " ,'" + clsGlobalVarClass.gClientCode + "',a.strManualAdvOrderNo "
			+ " from tblqadvbookbillhd a left outer join tblqbillhd d on a.strAdvBookingNo=d.strAdvBookingNo "
			+ " left outer join tblqadvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo "
			+ " left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode "
			+ " left outer join tblqadvbookbilldtl e on e.strAdvBookingNo=a.strAdvBookingNo "
			+ " left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode "
			+ " where Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' "
			+ " and d.strBillNo not in(select strBillNo from tblqbillsettlementdtl) "
			+ " and d.strSettelmentMode is not null");

		if (!cmbTransType.getSelectedItem().toString().trim().equals("All"))
		{
		    //settled
		    sbSqlForBilled1.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		    sbSqlForBilled2.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		    sbSqlForBilled3.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		    //billed
		    sbSqlForBilled11.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		    sbSqlForBilled22.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		    sbSqlForBilled33.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		}

		//opened
		sbSqlForOpen.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y'),a.strPOSCode,c.strCustomerName\n"
			+ ",e.strItemName,e.dblQuantity,e.dblAmount,'',ifnull(f.strAdvOrderTypeName,'')\n"
			+ ",ifnull(d.strOperationType,''),'','0','" + clsGlobalVarClass.gUserCode + "'"
			+ ",'" + clsGlobalVarClass.gClientCode + "',a.strManualAdvOrderNo\n"
			+ "from tbladvbookbillhd a left outer join tblbillhd d on a.strAdvBookingNo=d.strAdvBookingNo \n"
			+ "left outer join tbladvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo \n"
			+ "left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode \n"
			+ "left outer join tbladvbookbilldtl e on e.strAdvBookingNo=a.strAdvBookingNo \n"
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			+ "where Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "'\n"
			+ "and d.strBillNo is null");

		if (clsGlobalVarClass.gHOPOSType.equals("HOPOS"))
		{
		    sbSqlForOpen.setLength(0);
		    sbSqlForOpen.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y'),a.strPOSCode,c.strCustomerName\n"
			    + ",e.strItemName,e.dblQuantity,e.dblAmount,'',ifnull(f.strAdvOrderTypeName,'')\n"
			    + ",ifnull(d.strOperationType,''),'','0','" + clsGlobalVarClass.gUserCode + "'"
			    + ",'" + clsGlobalVarClass.gClientCode + "',a.strManualAdvOrderNo \n"
			    + "from tblqadvbookbillhd a left outer join tblbillhd d on a.strAdvBookingNo=d.strAdvBookingNo \n"
			    + "left outer join tblqadvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo \n"
			    + "left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode \n"
			    + "left outer join tblqadvbookbilldtl e on e.strAdvBookingNo=a.strAdvBookingNo \n"
			    + "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			    + "where Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "'\n"
			    + "and d.strBillNo is null");
		}
	    }

	    if (!"All".equals(pos))
	    {
		if (sbSqlForOpen.length() > 0)
		{
		    //opened
		    sbSqlForOpen.append(" and a.strPOSCode='" + pos + "' ");
		}
		if (sbSqlForBilled1.length() > 0)
		{
		    //settled
		    sbSqlForBilled1.append(" and a.strPOSCode='" + pos + "' ");
		}
		if (sbSqlForBilled2.length() > 0)
		{
		    //settled
		    sbSqlForBilled2.append(" and a.strPOSCode='" + pos + "' ");
		}
		if (sbSqlForBilled3.length() > 0)
		{
		    //settled
		    sbSqlForBilled3.append(" and a.strPOSCode='" + pos + "' ");
		}
		if (sbSqlForBilled11.length() > 0)
		{
		    //billed
		    sbSqlForBilled11.append(" and a.strPOSCode='" + pos + "' ");
		}
		if (sbSqlForBilled22.length() > 0)
		{
		    //billed
		    sbSqlForBilled22.append(" and a.strPOSCode='" + pos + "' ");
		}
		if (sbSqlForBilled33.length() > 0)
		{
		    //billed
		    sbSqlForBilled33.append(" and a.strPOSCode='" + pos + "' ");
		}
	    }
	    if (!cmbOrderMode.getSelectedItem().toString().trim().equals("All"))
	    {
		if (hmAdvOrderType.size() > 0)
		{
		    String orderMode = cmbOrderMode.getSelectedItem().toString().trim();
		    if (sbSqlForOpen.length() > 0)
		    {
			//opened
			sbSqlForOpen.append(" and f.strAdvOrderTypeCode='" + hmAdvOrderType.get(orderMode) + "' ");
		    }
		    if (sbSqlForBilled1.length() > 0)
		    {
			//settled
			sbSqlForBilled1.append(" and f.strAdvOrderTypeCode='" + hmAdvOrderType.get(orderMode) + "' ");
		    }
		    if (sbSqlForBilled2.length() > 0)
		    {
			//settled
			sbSqlForBilled2.append(" and f.strAdvOrderTypeCode='" + hmAdvOrderType.get(orderMode) + "' ");
		    }
		    if (sbSqlForBilled3.length() > 0)
		    {
			//settled
			sbSqlForBilled3.append(" and f.strAdvOrderTypeCode='" + hmAdvOrderType.get(orderMode) + "' ");
		    }
		    if (sbSqlForBilled11.length() > 0)
		    {
			//billed
			sbSqlForBilled11.append(" and f.strAdvOrderTypeCode='" + hmAdvOrderType.get(orderMode) + "' ");
		    }
		    if (sbSqlForBilled22.length() > 0)
		    {
			//billed
			sbSqlForBilled22.append(" and f.strAdvOrderTypeCode='" + hmAdvOrderType.get(orderMode) + "' ");
		    }
		    if (sbSqlForBilled33.length() > 0)
		    {
			//billed
			sbSqlForBilled33.append(" and f.strAdvOrderTypeCode='" + hmAdvOrderType.get(orderMode) + "' ");
		    }
		}
	    }

	    boolean flgRecords = false;
	    String userCode = clsGlobalVarClass.gUserCode;
	    ResultSet rsAdvancedOrderForOpen = null;
	    ResultSet rsAdvancedOrderDataBilled1 = null;
	    ResultSet rsAdvancedOrderDataBilled2 = null;
	    ResultSet rsAdvancedOrderDataBilled3 = null;
	    ResultSet rsAdvancedOrderDataBilled11 = null;
	    ResultSet rsAdvancedOrderDataBilled22 = null;
	    ResultSet rsAdvancedOrderDataBilled33 = null;

	    if (sbSqlForOpen.length() > 0)
	    {
		rsAdvancedOrderForOpen = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlForOpen.toString());
		while (rsAdvancedOrderForOpen.next())
		{
		    flgRecords = true;

		    clsAdvBookBillHd objAdvBookColumns = new clsAdvBookBillHd();
		    objAdvBookColumns.setStrAdvBookingNo(rsAdvancedOrderForOpen.getString(1));
		    objAdvBookColumns.setDteAdvBookingDate(rsAdvancedOrderForOpen.getString(2));
		    objAdvBookColumns.setDteOrderFor(rsAdvancedOrderForOpen.getString(3));
		    objAdvBookColumns.setStrPOSCode(rsAdvancedOrderForOpen.getString(4));
		    objAdvBookColumns.setStrCustomerName(rsAdvancedOrderForOpen.getString(5));
		    objAdvBookColumns.setStrItemName(rsAdvancedOrderForOpen.getString(6));
		    objAdvBookColumns.setDblQuantity(rsAdvancedOrderForOpen.getDouble(7));
		    objAdvBookColumns.setDblGrandTotal(rsAdvancedOrderForOpen.getDouble(8));
		    objAdvBookColumns.setStrAdvOrderTypeName(rsAdvancedOrderForOpen.getString(9));
		    objAdvBookColumns.setStrOperationType(rsAdvancedOrderForOpen.getString(10));
		    objAdvBookColumns.setStrManualAdvOrderNo(rsAdvancedOrderForOpen.getString(11));

		    hmAdvOrderData.put(rsAdvancedOrderForOpen.getString(1), objAdvBookColumns);
		}
		rsAdvancedOrderForOpen.close();
	    }

	    if (sbSqlForBilled1.length() > 0)
	    {
		rsAdvancedOrderDataBilled1 = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlForBilled1.toString());
		while (rsAdvancedOrderDataBilled1.next())
		{
		    flgRecords = true;

		    clsAdvBookBillHd objAdvBookColumns = new clsAdvBookBillHd();
		    objAdvBookColumns.setStrAdvBookingNo(rsAdvancedOrderForOpen.getString(1));
		    objAdvBookColumns.setDteAdvBookingDate(rsAdvancedOrderForOpen.getString(2));
		    objAdvBookColumns.setDteOrderFor(rsAdvancedOrderForOpen.getString(3));
		    objAdvBookColumns.setStrPOSCode(rsAdvancedOrderForOpen.getString(4));
		    objAdvBookColumns.setStrCustomerName(rsAdvancedOrderForOpen.getString(5));
		    objAdvBookColumns.setStrItemName(rsAdvancedOrderForOpen.getString(6));
		    objAdvBookColumns.setDblQuantity(rsAdvancedOrderForOpen.getDouble(7));
		    objAdvBookColumns.setDblGrandTotal(rsAdvancedOrderForOpen.getDouble(8));
		    objAdvBookColumns.setStrAdvOrderTypeName(rsAdvancedOrderForOpen.getString(9));
		    objAdvBookColumns.setStrOperationType(rsAdvancedOrderForOpen.getString(10));
		    objAdvBookColumns.setStrManualAdvOrderNo(rsAdvancedOrderForOpen.getString(11));

		    hmAdvOrderData.put(rsAdvancedOrderDataBilled1.getString(1), objAdvBookColumns);
		}
		rsAdvancedOrderDataBilled1.close();
	    }
	    if (sbSqlForBilled2.length() > 0)
	    {
		rsAdvancedOrderDataBilled2 = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlForBilled2.toString());
		//settled
		while (rsAdvancedOrderDataBilled2.next())
		{

		    flgRecords = true;

		    clsAdvBookBillHd objAdvBookColumns = new clsAdvBookBillHd();
		    objAdvBookColumns.setStrAdvBookingNo(rsAdvancedOrderDataBilled2.getString(1));
		    objAdvBookColumns.setDteAdvBookingDate(rsAdvancedOrderDataBilled2.getString(2));
		    objAdvBookColumns.setDteOrderFor(rsAdvancedOrderDataBilled2.getString(3));
		    objAdvBookColumns.setStrPOSCode(rsAdvancedOrderDataBilled2.getString(4));
		    objAdvBookColumns.setStrCustomerName(rsAdvancedOrderDataBilled2.getString(5));
		    objAdvBookColumns.setStrItemName(rsAdvancedOrderDataBilled2.getString(6));
		    objAdvBookColumns.setDblQuantity(rsAdvancedOrderDataBilled2.getDouble(7));
		    objAdvBookColumns.setDblGrandTotal(rsAdvancedOrderDataBilled2.getDouble(8));
		    objAdvBookColumns.setStrAdvOrderTypeName(rsAdvancedOrderDataBilled2.getString(9));
		    objAdvBookColumns.setStrOperationType(rsAdvancedOrderDataBilled2.getString(10));
		    objAdvBookColumns.setStrManualAdvOrderNo(rsAdvancedOrderDataBilled2.getString(11));

		    hmAdvOrderData.put(rsAdvancedOrderDataBilled2.getString(1), objAdvBookColumns);
		}
		rsAdvancedOrderDataBilled2.close();
	    }
	    if (sbSqlForBilled3.length() > 0)
	    {
		rsAdvancedOrderDataBilled3 = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlForBilled3.toString());
		//settled
		while (rsAdvancedOrderDataBilled3.next())
		{

		    flgRecords = true;

		    clsAdvBookBillHd objAdvBookColumns = new clsAdvBookBillHd();
		    objAdvBookColumns.setStrAdvBookingNo(rsAdvancedOrderDataBilled3.getString(1));
		    objAdvBookColumns.setDteAdvBookingDate(rsAdvancedOrderDataBilled3.getString(2));
		    objAdvBookColumns.setDteOrderFor(rsAdvancedOrderDataBilled3.getString(3));
		    objAdvBookColumns.setStrPOSCode(rsAdvancedOrderDataBilled3.getString(4));
		    objAdvBookColumns.setStrCustomerName(rsAdvancedOrderDataBilled3.getString(5));
		    objAdvBookColumns.setStrItemName(rsAdvancedOrderDataBilled3.getString(6));
		    objAdvBookColumns.setDblQuantity(rsAdvancedOrderDataBilled3.getDouble(7));
		    objAdvBookColumns.setDblGrandTotal(rsAdvancedOrderDataBilled3.getDouble(8));
		    objAdvBookColumns.setStrAdvOrderTypeName(rsAdvancedOrderDataBilled3.getString(9));
		    objAdvBookColumns.setStrOperationType(rsAdvancedOrderDataBilled3.getString(10));
		    objAdvBookColumns.setStrManualAdvOrderNo(rsAdvancedOrderDataBilled3.getString(11));

		    hmAdvOrderData.put(rsAdvancedOrderDataBilled3.getString(1), objAdvBookColumns);
		}
		rsAdvancedOrderDataBilled3.close();
	    }

	    if (sbSqlForBilled11.length() > 0)
	    {
		rsAdvancedOrderDataBilled11 = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlForBilled11.toString());
		//billed
		while (rsAdvancedOrderDataBilled11.next())
		{

		    flgRecords = true;

		    clsAdvBookBillHd objAdvBookColumns = new clsAdvBookBillHd();
		    objAdvBookColumns.setStrAdvBookingNo(rsAdvancedOrderDataBilled11.getString(1));
		    objAdvBookColumns.setDteAdvBookingDate(rsAdvancedOrderDataBilled11.getString(2));
		    objAdvBookColumns.setDteOrderFor(rsAdvancedOrderDataBilled11.getString(3));
		    objAdvBookColumns.setStrPOSCode(rsAdvancedOrderDataBilled11.getString(4));
		    objAdvBookColumns.setStrCustomerName(rsAdvancedOrderDataBilled11.getString(5));
		    objAdvBookColumns.setStrItemName(rsAdvancedOrderDataBilled11.getString(6));
		    objAdvBookColumns.setDblQuantity(rsAdvancedOrderDataBilled11.getDouble(7));
		    objAdvBookColumns.setDblGrandTotal(rsAdvancedOrderDataBilled11.getDouble(8));
		    objAdvBookColumns.setStrAdvOrderTypeName(rsAdvancedOrderDataBilled11.getString(9));
		    objAdvBookColumns.setStrOperationType(rsAdvancedOrderDataBilled11.getString(10));
		    objAdvBookColumns.setStrManualAdvOrderNo(rsAdvancedOrderDataBilled11.getString(11));

		    hmAdvOrderData.put(rsAdvancedOrderDataBilled11.getString(1), objAdvBookColumns);
		}
		rsAdvancedOrderDataBilled11.close();
	    }
	    if (sbSqlForBilled22.length() > 0)
	    {
		rsAdvancedOrderDataBilled22 = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlForBilled22.toString());
		//billed
		while (rsAdvancedOrderDataBilled22.next())
		{

		    flgRecords = true;

		    clsAdvBookBillHd objAdvBookColumns = new clsAdvBookBillHd();
		    objAdvBookColumns.setStrAdvBookingNo(rsAdvancedOrderDataBilled22.getString(1));
		    objAdvBookColumns.setDteAdvBookingDate(rsAdvancedOrderDataBilled22.getString(2));
		    objAdvBookColumns.setDteOrderFor(rsAdvancedOrderDataBilled22.getString(3));
		    objAdvBookColumns.setStrPOSCode(rsAdvancedOrderDataBilled22.getString(4));
		    objAdvBookColumns.setStrCustomerName(rsAdvancedOrderDataBilled22.getString(5));
		    objAdvBookColumns.setStrItemName(rsAdvancedOrderDataBilled22.getString(6));
		    objAdvBookColumns.setDblQuantity(rsAdvancedOrderDataBilled22.getDouble(7));
		    objAdvBookColumns.setDblGrandTotal(rsAdvancedOrderDataBilled22.getDouble(8));
		    objAdvBookColumns.setStrAdvOrderTypeName(rsAdvancedOrderDataBilled22.getString(9));
		    objAdvBookColumns.setStrOperationType(rsAdvancedOrderDataBilled22.getString(10));
		    objAdvBookColumns.setStrManualAdvOrderNo(rsAdvancedOrderDataBilled22.getString(11));

		    hmAdvOrderData.put(rsAdvancedOrderDataBilled22.getString(1), objAdvBookColumns);
		}
		rsAdvancedOrderDataBilled22.close();
	    }

	    if (sbSqlForBilled33.length() > 0)
	    {
		rsAdvancedOrderDataBilled33 = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlForBilled33.toString());
		//settled
		while (rsAdvancedOrderDataBilled33.next())
		{
		    flgRecords = true;

		    clsAdvBookBillHd objAdvBookColumns = new clsAdvBookBillHd();
		    objAdvBookColumns.setStrAdvBookingNo(rsAdvancedOrderDataBilled33.getString(1));
		    objAdvBookColumns.setDteAdvBookingDate(rsAdvancedOrderDataBilled33.getString(2));
		    objAdvBookColumns.setDteOrderFor(rsAdvancedOrderDataBilled33.getString(3));
		    objAdvBookColumns.setStrPOSCode(rsAdvancedOrderDataBilled33.getString(4));
		    objAdvBookColumns.setStrCustomerName(rsAdvancedOrderDataBilled33.getString(5));
		    objAdvBookColumns.setStrItemName(rsAdvancedOrderDataBilled33.getString(6));
		    objAdvBookColumns.setDblQuantity(rsAdvancedOrderDataBilled33.getDouble(7));
		    objAdvBookColumns.setDblGrandTotal(rsAdvancedOrderDataBilled33.getDouble(8));
		    objAdvBookColumns.setStrAdvOrderTypeName(rsAdvancedOrderDataBilled33.getString(9));
		    objAdvBookColumns.setStrOperationType(rsAdvancedOrderDataBilled33.getString(10));
		    objAdvBookColumns.setStrManualAdvOrderNo(rsAdvancedOrderDataBilled33.getString(11));

		    hmAdvOrderData.put(rsAdvancedOrderDataBilled33.getString(1), objAdvBookColumns);
		}
		rsAdvancedOrderDataBilled33.close();
	    }

	    Object[] records = new Object[14];
	    for (clsAdvBookBillHd objAdvBookBillHd : hmAdvOrderData.values())
	    {

		records[0] = objAdvBookBillHd.getStrAdvBookingNo();
		records[1] = objAdvBookBillHd.getDteAdvBookingDate();
		records[2] = objAdvBookBillHd.getDteOrderFor();
		records[3] = objAdvBookBillHd.getStrCustomerName();
		records[4] = objAdvBookBillHd.getStrAdvOrderTypeName();
		records[5] = objAdvBookBillHd.getStrOperationType();
		records[6] = objAdvBookBillHd.getStrItemName();
		records[7] = objAdvBookBillHd.getDblQuantity();
		records[8] = gDecimalFormat.format(objAdvBookBillHd.getDblGrandTotal());
		records[9] = objAdvBookBillHd.getStrManualAdvOrderNo();
		sumGt = sumGt + objAdvBookBillHd.getDblGrandTotal();
		sumQty = sumQty + objAdvBookBillHd.getDblQuantity();

		dm.addRow(records);

	    }

	    tblAdvOrder.setModel(dm);
	    Object[] total
		    =
		    {
			"Total", sumQty, gDecimalFormat.format(sumGt)
		    };
	    dm2.addRow(total);
	    tblTotal.setModel(dm2);

	    DefaultTableCellRenderer rightRenderer1 = new DefaultTableCellRenderer();
	    rightRenderer1.setHorizontalAlignment(JLabel.RIGHT);
	    tblAdvOrder.getColumnModel().getColumn(7).setCellRenderer(rightRenderer1);
	    tblAdvOrder.getColumnModel().getColumn(8).setCellRenderer(rightRenderer1);
	    tblAdvOrder.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

	    tblAdvOrder.getColumnModel().getColumn(0).setPreferredWidth(80);// Adv Order No
	    tblAdvOrder.getColumnModel().getColumn(1).setPreferredWidth(80);// Order For
	    tblAdvOrder.getColumnModel().getColumn(2).setPreferredWidth(80);// Bill Date
	    tblAdvOrder.getColumnModel().getColumn(3).setPreferredWidth(120);// Cust Name
	    tblAdvOrder.getColumnModel().getColumn(4).setPreferredWidth(80);// Order Type
	    tblAdvOrder.getColumnModel().getColumn(5).setPreferredWidth(80);// Op Type
	    tblAdvOrder.getColumnModel().getColumn(6).setPreferredWidth(120);// Item Name
	    tblAdvOrder.getColumnModel().getColumn(7).setPreferredWidth(70);// Qty
	    tblAdvOrder.getColumnModel().getColumn(8).setPreferredWidth(70);// Amt
	    tblAdvOrder.getColumnModel().getColumn(9).setPreferredWidth(70);// Manual Adv Order No

	    tblTotal.getColumnModel().getColumn(1).setCellRenderer(rightRenderer1);
	    tblTotal.getColumnModel().getColumn(2).setCellRenderer(rightRenderer1);
	    tblTotal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    tblTotal.getColumnModel().getColumn(0).setPreferredWidth(570);
	    tblTotal.getColumnModel().getColumn(1).setPreferredWidth(70);
	    tblTotal.getColumnModel().getColumn(2).setPreferredWidth(70);
	    tblTotal.getColumnModel().getColumn(3).setPreferredWidth(70);
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    /**
     * this Function is used for fun Bill Wise Report Filter the Selected Bill
     * Show the Report BillWise
     */
    private void funBillWise()
    {

	Map<String, clsAdvBookBillHd> hmAdvOrderData = new HashMap<String, clsAdvBookBillHd>();
	try
	{
	    String posCode = cmbPosCode.getSelectedItem().toString();
	    StringBuilder sb = new StringBuilder(posCode);
	    int len = posCode.length();
	    int lastInd = sb.lastIndexOf(" ");
	    String pos = sb.substring(lastInd + 1, len).toString();
	    fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());
	    toDate = objUtility.funGetFromToDate(dteToDate.getDate());
	    double sumGt = 0.00, sumAdvAmt = 0.00, sumBal = 0.00, sumSettlement = 0.00, sumSumTotal = 0.00, sumDisc = 0.00, sumTax = 0.00;
	    dm.getDataVector().removeAllElements();
	    dm = new DefaultTableModel()
	    {

		@Override
		public boolean isCellEditable(int row, int column)
		{
		    return false;
		}
	    };
	    dm.addColumn("Adv Ord No");//1
	    dm.addColumn("Adv Book Date");//2
	    dm.addColumn("Order For");//3
	    dm.addColumn("Settle Date");//4
	    dm.addColumn("Order By");//5
	    dm.addColumn("Order Type");//6
	    dm.addColumn("Operation");//7
	    dm.addColumn("POS");//8
	    dm.addColumn("Settlement Mode");//9

	    dm.addColumn("Sub Total");//10
	    dm.addColumn("Disc.");//11
	    dm.addColumn("Tax");//12
	    dm.addColumn("Grand Total");//13

	    dm.addColumn("Adv. Pay Mode");//14
	    dm.addColumn("Advance");//15
	    dm.addColumn("Balance");//16
	    dm.addColumn("Bill No");//17
	    dm.addColumn("Settlement Amount");//18
	    dm.addColumn("Manual Adv Order No");//19

	    DefaultTableModel dm2 = new DefaultTableModel()
	    {
		@Override
		public boolean isCellEditable(int row, int column)
		{
		    return false;
		}
	    };
	    dm2.getDataVector().removeAllElements();

	    dm2.addColumn("");
	    dm2.addColumn("");
	    dm2.addColumn("");
	    dm2.addColumn("");
	    dm2.addColumn("");
	    dm2.addColumn("");
	    dm2.addColumn("");
	    dm2.addColumn("");

	    StringBuilder sbSqlForOpen = new StringBuilder(); // Adv Live only
	    StringBuilder sbSqlForBilled1 = new StringBuilder(); // Adv Live + Bill Live
	    StringBuilder sbSqlForBilled2 = new StringBuilder(); //Adv QFile + Bill Live
	    StringBuilder sbSqlForBilled3 = new StringBuilder(); //Adv QFile + Bill QFile
	    StringBuilder sbSqlForBilled11 = new StringBuilder(); // Adv Live + Bill Live
	    StringBuilder sbSqlForBilled22 = new StringBuilder(); //Adv QFile + Bill Live
	    StringBuilder sbSqlForBilled33 = new StringBuilder(); //Adv QFile + Bill QFile

	    sbSqlForOpen.setLength(0);
	    sbSqlForBilled1.setLength(0);
	    sbSqlForBilled2.setLength(0);
	    sbSqlForBilled3.setLength(0);
	    sbSqlForBilled11.setLength(0);
	    sbSqlForBilled22.setLength(0);
	    sbSqlForBilled33.setLength(0);

	    if (cmbStatus.getSelectedItem().equals("Open")) // For Open Adv Order Option
	    {
		sbSqlForOpen.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y') \n"
			+ ",DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y') ,a.strPOSCode,'',a.dblSubTotal \n"
			+ ",ifnull(b.dblAdvDeposite,0)"
			+ " ,ifnull(if(b.dblAdvDeposite>a.dblGrandTotal,0,(a.dblGrandTotal-b.dblAdvDeposite)),0) as Balance "
			+ ",ifnull(c.strWShortName,''),ifnull(f.strAdvOrderTypeName,''),'',ifnull(e.strBillNo,''),0\n"
			+ ",'" + clsGlobalVarClass.gUserCode + "','" + clsGlobalVarClass.gClientCode + "' "
			+ ",a.strManualAdvOrderNo,ifnull(DATE_FORMAT(e.dteSettleDate,'%d-%m-%Y'),'') "
			+ ",ifnull(b.strSettelmentMode,'') AdvPayMode,i.strPosName  "//19
			+ ",a.dblDiscountAmt,a.dblTaxAmt,a.dblGrandTotal "//22
			+ "from tbladvbookbillhd a \n"
			+ "left outer join tbladvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo \n"
			+ "left outer join tblwaitermaster c on a.strWaiterNo=c.strWaiterNo  \n"
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			+ "left outer join tblbillhd e on a.strAdvBookingNo=e.strAdvBookingNo\n"
			+ " left outer join tblposmaster i on a.strPOSCode=i.strPosCode "
			+ " where  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' \n"
			+ "and e.strBillNo is null");

		if (clsGlobalVarClass.gHOPOSType.equals("HOPOS"))
		{
		    sbSqlForOpen.setLength(0);
		    sbSqlForOpen.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y') \n"
			    + ",DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y')  ,a.strPOSCode,'',a.dblSubTotal \n"
			    + ",ifnull(b.dblAdvDeposite,0)"
			    + " ,ifnull(if(b.dblAdvDeposite>a.dblGrandTotal,0,(a.dblGrandTotal-b.dblAdvDeposite)),0) as Balance "
			    + ",ifnull(c.strWShortName,''),ifnull(f.strAdvOrderTypeName,''),'',ifnull(e.strBillNo,''),0\n"
			    + ",'" + clsGlobalVarClass.gUserCode + "','" + clsGlobalVarClass.gClientCode + "'"
			    + ",a.strManualAdvOrderNo,ifnull(DATE_FORMAT(e.dteSettleDate,'%d-%m-%Y'),'') "
			    + ",ifnull(b.strSettelmentMode,'') AdvPayMode,i.strPosName  "//19
			    + ",a.dblDiscountAmt,a.dblTaxAmt,a.dblGrandTotal "//22
			    + "from tblqadvbookbillhd a \n"
			    + "left outer join tblqadvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo \n"
			    + "left outer join tblwaitermaster c on a.strWaiterNo=c.strWaiterNo  \n"
			    + "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			    + "left outer join tblqbillhd e on a.strAdvBookingNo=e.strAdvBookingNo\n"
			    + " left outer join tblposmaster i on a.strPOSCode=i.strPosCode "
			    + " where  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' \n"
			    + "and e.strBillNo is null");
		}
	    }
	    else if (cmbStatus.getSelectedItem().equals("Settled")) // For Settled Option
	    {
		sbSqlForBilled1.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y') \n"
			+ ",DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y')  ,a.strPOSCode,h.strSettelmentDesc,a.dblSubTotal \n"
			+ ",ifnull(b.dblAdvDeposite,0)"
			+ ",g.dblSettlementAmt as Balance "
			+ ",ifnull(c.strWShortName,''),ifnull(f.strAdvOrderTypeName,''),ifnull(d.strOperationType,'')\n"
			+ ",ifnull(d.strBillNo,'') ,ifnull(g.dblSettlementAmt,0.00) \n"
			+ ",'" + clsGlobalVarClass.gUserCode + "','" + clsGlobalVarClass.gClientCode + "'"
			+ ",a.strManualAdvOrderNo,ifnull(DATE_FORMAT(d.dteSettleDate,'%d-%m-%Y'),'') "
			+ ",ifnull(b.strSettelmentMode,'') AdvPayMode,i.strPosName  "//19
			+ ",d.dblDiscountAmt,d.dblTaxAmt"
			+ ",ifnull(round(d.dblSubTotal-d.dblDiscountAmt+d.dblTaxAmt),0)GrandTotal "//22
			+ "from tbladvbookbillhd a left outer join tbladvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo\n"
			+ "left outer join tblwaitermaster c on a.strWaiterNo=c.strWaiterNo \n"
			+ "left outer join tblbillhd d on a.strAdvBookingNo=d.strAdvBookingNo \n"
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			+ "inner join tblbillsettlementdtl g on d.strBillNo=g.strBillNo "
			+ "left outer join tblsettelmenthd h on g.strSettlementCode=h.strSettelmentCode \n"
			+ " left outer join tblposmaster i on a.strPOSCode=i.strPosCode "
			+ " where  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' \n"
			+ "and d.strSettelmentMode is not null ");

		sbSqlForBilled2.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y') \n"
			+ ",DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y')  ,a.strPOSCode,h.strSettelmentDesc,a.dblSubTotal \n"
			+ ",ifnull(b.dblAdvDeposite,0)"
			+ ",g.dblSettlementAmt as Balance "
			+ ",ifnull(c.strWShortName,''),ifnull(f.strAdvOrderTypeName,''),ifnull(d.strOperationType,'')\n"
			+ ",ifnull(d.strBillNo,'') ,ifnull(g.dblSettlementAmt,0.00) \n"
			+ ",'" + clsGlobalVarClass.gUserCode + "','" + clsGlobalVarClass.gClientCode + "'"
			+ ",a.strManualAdvOrderNo,ifnull(DATE_FORMAT(d.dteSettleDate,'%d-%m-%Y'),'') "
			+ ",ifnull(b.strSettelmentMode,'') AdvPayMode,i.strPosName  "//19
			+ ",d.dblDiscountAmt,d.dblTaxAmt"
			+ ",ifnull(round(d.dblSubTotal-d.dblDiscountAmt+d.dblTaxAmt),0)GrandTotal "//22
			+ "from tblqadvbookbillhd a left outer join tblqadvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo\n"
			+ "left outer join tblwaitermaster c on a.strWaiterNo=c.strWaiterNo \n"
			+ "left outer join tblbillhd d on a.strAdvBookingNo=d.strAdvBookingNo \n"
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			+ "inner join tblbillsettlementdtl g on d.strBillNo=g.strBillNo "
			+ "left outer join tblsettelmenthd h on g.strSettlementCode=h.strSettelmentCode \n"
			+ " left outer join tblposmaster i on a.strPOSCode=i.strPosCode "
			+ " where  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' \n"
			+ "and d.strSettelmentMode is not null ");

		sbSqlForBilled3.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y') \n"
			+ ",DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y') ,a.strPOSCode,h.strSettelmentDesc,a.dblSubTotal \n"
			+ ",ifnull(b.dblAdvDeposite,0)"
			+ ",g.dblSettlementAmt as Balance "
			+ ",ifnull(c.strWShortName,''),ifnull(f.strAdvOrderTypeName,''),ifnull(d.strOperationType,'')\n"
			+ ",ifnull(d.strBillNo,'') ,ifnull(g.dblSettlementAmt,0.00) \n"
			+ ",'" + clsGlobalVarClass.gUserCode + "','" + clsGlobalVarClass.gClientCode + "'"
			+ ",a.strManualAdvOrderNo,ifnull(DATE_FORMAT(d.dteSettleDate,'%d-%m-%Y'),'') "
			+ ",ifnull(b.strSettelmentMode,'') AdvPayMode,i.strPosName  "//19
			+ ",d.dblDiscountAmt,d.dblTaxAmt"
			+ ",ifnull(round(d.dblSubTotal-d.dblDiscountAmt+d.dblTaxAmt),0)GrandTotal "//22
			+ "from tblqadvbookbillhd a left outer join tblqadvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo\n"
			+ "left outer join tblwaitermaster c on a.strWaiterNo=c.strWaiterNo \n"
			+ "left outer join tblqbillhd d on a.strAdvBookingNo=d.strAdvBookingNo \n"
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			+ "inner join tblqbillsettlementdtl g on d.strBillNo=g.strBillNo "
			+ "left outer join tblsettelmenthd h on g.strSettlementCode=h.strSettelmentCode \n"
			+ " left outer join tblposmaster i on a.strPOSCode=i.strPosCode "
			+ " where  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' \n"
			+ "and d.strSettelmentMode is not null ");

		if (!cmbTransType.getSelectedItem().toString().trim().equals("All"))
		{
		    sbSqlForBilled1.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		    sbSqlForBilled2.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		    sbSqlForBilled3.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		}
	    }
	    else if (cmbStatus.getSelectedItem().equals("Billed")) // For Billed Option
	    {
		sbSqlForBilled1.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y') \n"
			+ ",DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y') ,a.strPOSCode,d.strSettelmentMode,a.dblSubTotal \n"
			+ ",ifnull(b.dblAdvDeposite,0)\n"
			+ ",round(d.dblSubTotal-d.dblDiscountAmt+d.dblTaxAmt-b.dblAdvDeposite)as Balance \n"
			+ ",ifnull(c.strWShortName,''),ifnull(f.strAdvOrderTypeName,''),ifnull(d.strOperationType,'')\n"
			+ ",ifnull(d.strBillNo,'') \n"
			+ ",round(d.dblSubTotal-d.dblDiscountAmt+d.dblTaxAmt-b.dblAdvDeposite)as SettleAmt\n"
			+ ",'SANGUINE','024.002',a.strManualAdvOrderNo\n"
			+ ",ifnull(DATE_FORMAT(d.dteSettleDate,'%d-%m-%Y'),'')  \n"
			+ ",ifnull(a.strSettelmentMode,'') AdvPayMode,i.strPosName  ,d.dblDiscountAmt,d.dblTaxAmt\n"
			+ ",round(a.dblSubTotal-d.dblDiscountAmt+d.dblTaxAmt)GrandTotal "//22
			+ "from tbladvbookbillhd a left outer join tbladvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo\n"
			+ "left outer join tblwaitermaster c on a.strWaiterNo=c.strWaiterNo \n"
			+ "left outer join tblbillhd d on a.strAdvBookingNo=d.strAdvBookingNo \n"
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			+ " left outer join tblposmaster i on a.strPOSCode=i.strPosCode "
			+ "where  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' \n"
			+ "and d.strSettelmentMode is not null "
			+ "and d.strBillNo not in(select strBillNo from tblbillsettlementdtl)\n"
			+ " and d.strBillNo not in(select strBillNo from tblqbillsettlementdtl) ");

		sbSqlForBilled2.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y') \n"
			+ ",DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y') ,a.strPOSCode,d.strSettelmentMode,a.dblSubTotal \n"
			+ ",ifnull(b.dblAdvDeposite,0)\n"
			+ ",round(d.dblSubTotal-d.dblDiscountAmt+d.dblTaxAmt-b.dblAdvDeposite)as Balance \n"
			+ ",ifnull(c.strWShortName,''),ifnull(f.strAdvOrderTypeName,''),ifnull(d.strOperationType,'')\n"
			+ ",ifnull(d.strBillNo,'') \n"
			+ ",round(d.dblSubTotal-d.dblDiscountAmt+d.dblTaxAmt-b.dblAdvDeposite)as SettleAmt\n"
			+ ",'SANGUINE','024.002',a.strManualAdvOrderNo\n"
			+ ",ifnull(DATE_FORMAT(d.dteSettleDate,'%d-%m-%Y'),'')  \n"
			+ ",ifnull(a.strSettelmentMode,'') AdvPayMode,i.strPosName  ,d.dblDiscountAmt,d.dblTaxAmt\n"
			+ ",round(a.dblSubTotal-d.dblDiscountAmt+d.dblTaxAmt)GrandTotal "//22
			+ "from tblqadvbookbillhd a left outer join tblqadvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo\n"
			+ "left outer join tblwaitermaster c on a.strWaiterNo=c.strWaiterNo \n"
			+ "left outer join tblbillhd d on a.strAdvBookingNo=d.strAdvBookingNo \n"
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			+ "and d.strBillNo not in(select strBillNo from tblbillsettlementdtl)\n"
			+ " left outer join tblposmaster i on a.strPOSCode=i.strPosCode "
			+ "where  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' \n"
			+ "and d.strSettelmentMode is not null "
			+ "and d.strBillNo not in(select strBillNo from tblbillsettlementdtl)\n"
			+ " and d.strBillNo not in(select strBillNo from tblqbillsettlementdtl) ");

		sbSqlForBilled3.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y') \n"
			+ ",DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y') ,a.strPOSCode,d.strSettelmentMode,a.dblSubTotal \n"
			+ ",ifnull(b.dblAdvDeposite,0)\n"
			+ ",round(d.dblSubTotal-d.dblDiscountAmt+d.dblTaxAmt-b.dblAdvDeposite)as Balance \n"
			+ ",ifnull(c.strWShortName,''),ifnull(f.strAdvOrderTypeName,''),ifnull(d.strOperationType,'')\n"
			+ ",ifnull(d.strBillNo,'') \n"
			+ ",round(d.dblSubTotal-d.dblDiscountAmt+d.dblTaxAmt-b.dblAdvDeposite)as SettleAmt\n"
			+ ",'SANGUINE','024.002',a.strManualAdvOrderNo\n"
			+ ",ifnull(DATE_FORMAT(d.dteSettleDate,'%d-%m-%Y'),'')  \n"
			+ ",ifnull(a.strSettelmentMode,'') AdvPayMode,i.strPosName  ,d.dblDiscountAmt,d.dblTaxAmt\n"
			+ ",round(a.dblSubTotal-d.dblDiscountAmt+d.dblTaxAmt)GrandTotal "//22
			+ "from tblqadvbookbillhd a left outer join tblqadvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo\n"
			+ "left outer join tblwaitermaster c on a.strWaiterNo=c.strWaiterNo \n"
			+ "left outer join tblqbillhd d on a.strAdvBookingNo=d.strAdvBookingNo \n"
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			+ "and d.strBillNo not in(select strBillNo from tblbillsettlementdtl)\n"
			+ " left outer join tblposmaster i on a.strPOSCode=i.strPosCode "
			+ " where  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' \n"
			+ "and d.strSettelmentMode is not null "
			+ "and d.strBillNo not in(select strBillNo from tblbillsettlementdtl)\n"
			+ " and d.strBillNo not in(select strBillNo from tblqbillsettlementdtl) ");
		if (!cmbTransType.getSelectedItem().toString().trim().equals("All"))
		{
		    sbSqlForBilled1.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		    sbSqlForBilled2.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		    sbSqlForBilled3.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		}
	    }
	    else // For All Option
	    {
		//settled
		sbSqlForBilled1.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y') \n"
			+ ",DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y') ,a.strPOSCode,h.strSettelmentDesc,a.dblSubTotal \n"
			+ ",ifnull(b.dblAdvDeposite,0)"
			+ ",g.dblSettlementAmt as Balance "
			+ ",ifnull(c.strWShortName,''),ifnull(f.strAdvOrderTypeName,''),ifnull(d.strOperationType,'')\n"
			+ ",ifnull(d.strBillNo,'') ,ifnull(g.dblSettlementAmt,0.00) \n"
			+ ",'" + clsGlobalVarClass.gUserCode + "','" + clsGlobalVarClass.gClientCode + "'"
			+ ",a.strManualAdvOrderNo,ifnull(DATE_FORMAT(d.dteSettleDate,'%d-%m-%Y'),'')  "
			+ ",ifnull(b.strSettelmentMode,'') AdvPayMode,i.strPosName  "//19
			+ ",d.dblDiscountAmt,d.dblTaxAmt"
			+ ",ifnull(round(d.dblSubTotal-d.dblDiscountAmt+d.dblTaxAmt),0)GrandTotal "//22
			+ "from tbladvbookbillhd a left outer join tbladvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo\n"
			+ "left outer join tblwaitermaster c on a.strWaiterNo=c.strWaiterNo \n"
			+ "left outer join tblbillhd d on a.strAdvBookingNo=d.strAdvBookingNo \n"
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			+ "inner join tblbillsettlementdtl g on d.strBillNo=g.strBillNo "
			+ "left outer join tblsettelmenthd h on g.strSettlementCode=h.strSettelmentCode \n"
			+ " left outer join tblposmaster i on a.strPOSCode=i.strPosCode "
			+ " where  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' \n"
			+ "and d.strSettelmentMode is not null ");

		sbSqlForBilled2.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y') \n"
			+ ",DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y') ,a.strPOSCode,h.strSettelmentDesc,a.dblSubTotal \n"
			+ ",ifnull(b.dblAdvDeposite,0)"
			+ ",g.dblSettlementAmt as Balance "
			+ ",ifnull(c.strWShortName,''),ifnull(f.strAdvOrderTypeName,''),ifnull(d.strOperationType,'')\n"
			+ ",ifnull(d.strBillNo,'') ,ifnull(g.dblSettlementAmt,0.00) \n"
			+ ",'" + clsGlobalVarClass.gUserCode + "','" + clsGlobalVarClass.gClientCode + "'"
			+ ",a.strManualAdvOrderNo,ifnull(DATE_FORMAT(d.dteSettleDate,'%d-%m-%Y'),'') "
			+ ",ifnull(b.strSettelmentMode,'') AdvPayMode,i.strPosName  "//19
			+ ",d.dblDiscountAmt,d.dblTaxAmt"
			+ ",ifnull(round(d.dblSubTotal-d.dblDiscountAmt+d.dblTaxAmt),0)GrandTotal "//22
			+ "from tblqadvbookbillhd a left outer join tblqadvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo\n"
			+ "left outer join tblwaitermaster c on a.strWaiterNo=c.strWaiterNo \n"
			+ "left outer join tblbillhd d on a.strAdvBookingNo=d.strAdvBookingNo \n"
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			+ "inner join tblbillsettlementdtl g on d.strBillNo=g.strBillNo "
			+ "left outer join tblsettelmenthd h on g.strSettlementCode=h.strSettelmentCode \n"
			+ " left outer join tblposmaster i on a.strPOSCode=i.strPosCode "
			+ " where  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' \n"
			+ "and d.strSettelmentMode is not null ");

		sbSqlForBilled3.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y') \n"
			+ ",DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y')  ,a.strPOSCode,h.strSettelmentDesc,a.dblSubTotal \n"
			+ ",ifnull(b.dblAdvDeposite,0)"
			+ ",g.dblSettlementAmt as Balance "
			+ ",ifnull(c.strWShortName,''),ifnull(f.strAdvOrderTypeName,''),ifnull(d.strOperationType,'')\n"
			+ ",ifnull(d.strBillNo,'') ,ifnull(g.dblSettlementAmt,0.00) \n"
			+ ",'" + clsGlobalVarClass.gUserCode + "','" + clsGlobalVarClass.gClientCode + "'"
			+ ",a.strManualAdvOrderNo,ifnull(DATE_FORMAT(d.dteSettleDate,'%d-%m-%Y'),'') "
			+ ",ifnull(b.strSettelmentMode,'') AdvPayMode,i.strPosName  "//19
			+ ",d.dblDiscountAmt,d.dblTaxAmt"
			+ ",ifnull(round(d.dblSubTotal-d.dblDiscountAmt+d.dblTaxAmt),0)GrandTotal "//22
			+ "from tblqadvbookbillhd a left outer join tblqadvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo\n"
			+ "left outer join tblwaitermaster c on a.strWaiterNo=c.strWaiterNo \n"
			+ "left outer join tblqbillhd d on a.strAdvBookingNo=d.strAdvBookingNo \n"
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			+ "inner join tblqbillsettlementdtl g on d.strBillNo=g.strBillNo "
			+ "left outer join tblsettelmenthd h on g.strSettlementCode=h.strSettelmentCode \n"
			+ " left outer join tblposmaster i on a.strPOSCode=i.strPosCode "
			+ " where  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' \n"
			+ "and d.strSettelmentMode is not null ");

		//billed
		sbSqlForBilled11.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y') \n"
			+ ",DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y') ,a.strPOSCode,d.strSettelmentMode,a.dblSubTotal \n"
			+ ",ifnull(b.dblAdvDeposite,0)\n"
			+ ",round(d.dblSubTotal-d.dblDiscountAmt+d.dblTaxAmt-b.dblAdvDeposite)as Balance \n"
			+ ",ifnull(c.strWShortName,''),ifnull(f.strAdvOrderTypeName,''),ifnull(d.strOperationType,'')\n"
			+ ",ifnull(d.strBillNo,'') \n"
			+ ",round(d.dblSubTotal-d.dblDiscountAmt+d.dblTaxAmt-b.dblAdvDeposite)as SettleAmt\n"
			+ ",'SANGUINE','024.002',a.strManualAdvOrderNo\n"
			+ ",ifnull(DATE_FORMAT(d.dteSettleDate,'%d-%m-%Y'),'')  \n"
			+ ",ifnull(a.strSettelmentMode,'') AdvPayMode,i.strPosName  ,d.dblDiscountAmt,d.dblTaxAmt\n"
			+ ",round(a.dblSubTotal-d.dblDiscountAmt+d.dblTaxAmt)GrandTotal "//22
			+ "from tbladvbookbillhd a left outer join tbladvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo\n"
			+ "left outer join tblwaitermaster c on a.strWaiterNo=c.strWaiterNo \n"
			+ "left outer join tblbillhd d on a.strAdvBookingNo=d.strAdvBookingNo \n"
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			+ " left outer join tblposmaster i on a.strPOSCode=i.strPosCode "
			+ "where  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' \n"
			+ "and d.strSettelmentMode is not null "
			+ "and d.strBillNo not in(select strBillNo from tblbillsettlementdtl)\n"
			+ " and d.strBillNo not in(select strBillNo from tblqbillsettlementdtl) ");

		sbSqlForBilled22.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y') \n"
			+ ",DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y') ,a.strPOSCode,d.strSettelmentMode,a.dblSubTotal \n"
			+ ",ifnull(b.dblAdvDeposite,0)\n"
			+ ",round(d.dblSubTotal-d.dblDiscountAmt+d.dblTaxAmt-b.dblAdvDeposite)as Balance \n"
			+ ",ifnull(c.strWShortName,''),ifnull(f.strAdvOrderTypeName,''),ifnull(d.strOperationType,'')\n"
			+ ",ifnull(d.strBillNo,'') \n"
			+ ",round(d.dblSubTotal-d.dblDiscountAmt+d.dblTaxAmt-b.dblAdvDeposite)as SettleAmt\n"
			+ ",'SANGUINE','024.002',a.strManualAdvOrderNo\n"
			+ ",ifnull(DATE_FORMAT(d.dteSettleDate,'%d-%m-%Y'),'')  \n"
			+ ",ifnull(a.strSettelmentMode,'') AdvPayMode,i.strPosName  ,d.dblDiscountAmt,d.dblTaxAmt\n"
			+ ",round(a.dblSubTotal-d.dblDiscountAmt+d.dblTaxAmt)GrandTotal "//22
			+ "from tblqadvbookbillhd a left outer join tblqadvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo\n"
			+ "left outer join tblwaitermaster c on a.strWaiterNo=c.strWaiterNo \n"
			+ "left outer join tblbillhd d on a.strAdvBookingNo=d.strAdvBookingNo \n"
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			+ "and d.strBillNo not in(select strBillNo from tblbillsettlementdtl)\n"
			+ " left outer join tblposmaster i on a.strPOSCode=i.strPosCode "
			+ "where  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' \n"
			+ "and d.strSettelmentMode is not null "
			+ "and d.strBillNo not in(select strBillNo from tblbillsettlementdtl)\n"
			+ " and d.strBillNo not in(select strBillNo from tblqbillsettlementdtl) ");

		sbSqlForBilled33.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y') \n"
			+ ",DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y') ,a.strPOSCode,d.strSettelmentMode,a.dblSubTotal \n"
			+ ",ifnull(b.dblAdvDeposite,0)\n"
			+ ",round(d.dblSubTotal-d.dblDiscountAmt+d.dblTaxAmt-b.dblAdvDeposite)as Balance \n"
			+ ",ifnull(c.strWShortName,''),ifnull(f.strAdvOrderTypeName,''),ifnull(d.strOperationType,'')\n"
			+ ",ifnull(d.strBillNo,'') \n"
			+ ",round(d.dblSubTotal-d.dblDiscountAmt+d.dblTaxAmt-b.dblAdvDeposite)as SettleAmt\n"
			+ ",'SANGUINE','024.002',a.strManualAdvOrderNo\n"
			+ ",ifnull(DATE_FORMAT(d.dteSettleDate,'%d-%m-%Y'),'')  \n"
			+ ",ifnull(a.strSettelmentMode,'') AdvPayMode,i.strPosName  ,d.dblDiscountAmt,d.dblTaxAmt\n"
			+ ",round(a.dblSubTotal-d.dblDiscountAmt+d.dblTaxAmt)GrandTotal "//22
			+ "from tblqadvbookbillhd a left outer join tblqadvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo\n"
			+ "left outer join tblwaitermaster c on a.strWaiterNo=c.strWaiterNo \n"
			+ "left outer join tblqbillhd d on a.strAdvBookingNo=d.strAdvBookingNo \n"
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			+ "and d.strBillNo not in(select strBillNo from tblbillsettlementdtl)\n"
			+ " left outer join tblposmaster i on a.strPOSCode=i.strPosCode "
			+ " where  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' \n"
			+ "and d.strSettelmentMode is not null "
			+ "and d.strBillNo not in(select strBillNo from tblbillsettlementdtl)\n"
			+ " and d.strBillNo not in(select strBillNo from tblqbillsettlementdtl) ");

		if (!cmbTransType.getSelectedItem().toString().trim().equals("All"))
		{
		    //settled
		    sbSqlForBilled1.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		    sbSqlForBilled2.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		    sbSqlForBilled3.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		    //billed
		    sbSqlForBilled11.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		    sbSqlForBilled22.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		    sbSqlForBilled33.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		}

		//opened
		sbSqlForOpen.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y') \n"
			+ ",DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y')  ,a.strPOSCode,'',a.dblSubTotal \n"
			+ ",ifnull(b.dblAdvDeposite,0)"
			+ " ,ifnull(if(b.dblAdvDeposite>a.dblGrandTotal,0,(a.dblGrandTotal-b.dblAdvDeposite)),0) as Balance "
			+ ",ifnull(c.strWShortName,''),ifnull(f.strAdvOrderTypeName,''),'',ifnull(e.strBillNo,''),0\n"
			+ ",'" + clsGlobalVarClass.gUserCode + "','" + clsGlobalVarClass.gClientCode + "'"
			+ ",a.strManualAdvOrderNo,ifnull(DATE_FORMAT(e.dteSettleDate,'%d-%m-%Y'),'') "
			+ ",ifnull(b.strSettelmentMode,'') AdvPayMode,i.strPosName  "//19
			+ ",a.dblDiscountAmt,a.dblTaxAmt,a.dblGrandTotal "//22
			+ "from tbladvbookbillhd a \n"
			+ "left outer join tbladvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo \n"
			+ "left outer join tblwaitermaster c on a.strWaiterNo=c.strWaiterNo  \n"
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			+ "left outer join tblbillhd e on a.strAdvBookingNo=e.strAdvBookingNo "
			+ " left outer join tblposmaster i on a.strPOSCode=i.strPosCode "
			+ " where  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' \n"
			+ "and e.strBillNo is null");

		if (clsGlobalVarClass.gHOPOSType.equals("HOPOS"))
		{
		    sbSqlForOpen.setLength(0);
		    sbSqlForOpen.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y') \n"
			    + ",DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y')  ,a.strPOSCode,'',a.dblSubTotal \n"
			    + ",ifnull(b.dblAdvDeposite,0)"
			    + " ,ifnull(if(b.dblAdvDeposite>a.dblGrandTotal,0,(a.dblGrandTotal-b.dblAdvDeposite)),0) as Balance "
			    + ",ifnull(c.strWShortName,''),ifnull(f.strAdvOrderTypeName,''),'',ifnull(e.strBillNo,''),0\n"
			    + ",'" + clsGlobalVarClass.gUserCode + "','" + clsGlobalVarClass.gClientCode + "'"
			    + ",a.strManualAdvOrderNo,ifnull(DATE_FORMAT(e.dteSettleDate,'%d-%m-%Y'),'') "
			    + ",ifnull(b.strSettelmentMode,'') AdvPayMode,i.strPosName  "//19
			    + ",a.dblDiscountAmt,a.dblTaxAmt,a.dblGrandTotal "//22
			    + "from tblqadvbookbillhd a \n"
			    + "left outer join tblqadvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo \n"
			    + "left outer join tblwaitermaster c on a.strWaiterNo=c.strWaiterNo  \n"
			    + "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode \n"
			    + "left outer join tblqbillhd e on a.strAdvBookingNo=e.strAdvBookingNo "
			    + " left outer join tblposmaster i on a.strPOSCode=i.strPosCode "
			    + " where  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' \n"
			    + "and e.strBillNo is null");
		}
	    }

	    if (!"All".equals(pos))
	    {
		if (sbSqlForOpen.length() > 0)
		{
		    //opened
		    sbSqlForOpen.append(" and a.strPOSCode='" + pos + "' ");
		}
		if (sbSqlForBilled1.length() > 0)
		{
		    //settled
		    sbSqlForBilled1.append(" and a.strPOSCode='" + pos + "' ");
		}
		if (sbSqlForBilled2.length() > 0)
		{
		    //settled
		    sbSqlForBilled2.append(" and a.strPOSCode='" + pos + "' ");
		}
		if (sbSqlForBilled3.length() > 0)
		{
		    //settled
		    sbSqlForBilled3.append(" and a.strPOSCode='" + pos + "' ");
		}
		if (sbSqlForBilled11.length() > 0)
		{
		    //billed
		    sbSqlForBilled11.append(" and a.strPOSCode='" + pos + "' ");
		}
		if (sbSqlForBilled22.length() > 0)
		{
		    //billed
		    sbSqlForBilled22.append(" and a.strPOSCode='" + pos + "' ");
		}
		if (sbSqlForBilled33.length() > 0)
		{
		    //billed
		    sbSqlForBilled33.append(" and a.strPOSCode='" + pos + "' ");
		}
	    }
	    if (!cmbOrderMode.getSelectedItem().toString().trim().equals("All"))
	    {
		if (hmAdvOrderType.size() > 0)
		{
		    String orderMode = cmbOrderMode.getSelectedItem().toString().trim();
		    if (sbSqlForOpen.length() > 0)
		    {
			//opened
			sbSqlForOpen.append(" and f.strAdvOrderTypeCode='" + hmAdvOrderType.get(orderMode) + "' ");
		    }
		    if (sbSqlForBilled1.length() > 0)
		    {
			//settled
			sbSqlForBilled1.append(" and f.strAdvOrderTypeCode='" + hmAdvOrderType.get(orderMode) + "' ");
		    }
		    if (sbSqlForBilled2.length() > 0)
		    {
			//settled
			sbSqlForBilled2.append(" and f.strAdvOrderTypeCode='" + hmAdvOrderType.get(orderMode) + "' ");
		    }
		    if (sbSqlForBilled3.length() > 0)
		    {
			//settled
			sbSqlForBilled3.append(" and f.strAdvOrderTypeCode='" + hmAdvOrderType.get(orderMode) + "' ");
		    }

		    if (sbSqlForBilled11.length() > 0)
		    {
			//billed
			sbSqlForBilled11.append(" and f.strAdvOrderTypeCode='" + hmAdvOrderType.get(orderMode) + "' ");
		    }
		    if (sbSqlForBilled22.length() > 0)
		    {
			//billed
			sbSqlForBilled22.append(" and f.strAdvOrderTypeCode='" + hmAdvOrderType.get(orderMode) + "' ");
		    }
		    if (sbSqlForBilled33.length() > 0)
		    {
			//billed
			sbSqlForBilled33.append(" and f.strAdvOrderTypeCode='" + hmAdvOrderType.get(orderMode) + "' ");
		    }
		}
	    }

	    ResultSet rsAdvancedOrderForOpen = null;
	    ResultSet rsAdvancedOrderDataBilled1 = null;
	    ResultSet rsAdvancedOrderDataBilled2 = null;
	    ResultSet rsAdvancedOrderDataBilled3 = null;
	    ResultSet rsAdvancedOrderDataBilled11 = null;
	    ResultSet rsAdvancedOrderDataBilled22 = null;
	    ResultSet rsAdvancedOrderDataBilled33 = null;

	    if (sbSqlForOpen.length() > 0)
	    {
		rsAdvancedOrderForOpen = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlForOpen.toString());
		while (rsAdvancedOrderForOpen.next())
		{
		    clsAdvBookBillHd objAdvBookColumns = new clsAdvBookBillHd();
		    objAdvBookColumns.setStrAdvBookingNo(rsAdvancedOrderForOpen.getString(1));
		    objAdvBookColumns.setDteAdvBookingDate(rsAdvancedOrderForOpen.getString(2));
		    objAdvBookColumns.setDteOrderFor(rsAdvancedOrderForOpen.getString(3));
		    objAdvBookColumns.setStrPOSCode(rsAdvancedOrderForOpen.getString(4));
		    objAdvBookColumns.setStrSettelmentMode(rsAdvancedOrderForOpen.getString(5));
		    objAdvBookColumns.setDblSubTotal(rsAdvancedOrderForOpen.getDouble(6));
		    objAdvBookColumns.setDblAdvDeposite(rsAdvancedOrderForOpen.getDouble(7));
		    objAdvBookColumns.setBalance(rsAdvancedOrderForOpen.getDouble(8));
		    objAdvBookColumns.setStrWShortName(rsAdvancedOrderForOpen.getString(9));
		    objAdvBookColumns.setStrAdvOrderTypeName(rsAdvancedOrderForOpen.getString(10));
		    objAdvBookColumns.setStrOperationType(rsAdvancedOrderForOpen.getString(11));
		    objAdvBookColumns.setBillNo(rsAdvancedOrderForOpen.getString(12));
		    objAdvBookColumns.setDblSettlementAmt(rsAdvancedOrderForOpen.getDouble(13));
		    objAdvBookColumns.setStrManualAdvOrderNo(rsAdvancedOrderForOpen.getString(16));
		    objAdvBookColumns.setDteSettleDate(rsAdvancedOrderForOpen.getString(17));

		    objAdvBookColumns.setStrAdvPaySettelmentMode(rsAdvancedOrderForOpen.getString(18));//AdvPaySettlementMode
		    objAdvBookColumns.setStrPOSName(rsAdvancedOrderForOpen.getString(19));//posName

		    objAdvBookColumns.setDblDiscountAmt(rsAdvancedOrderForOpen.getDouble(20));
		    objAdvBookColumns.setDblTaxAmt(rsAdvancedOrderForOpen.getDouble(21));
		    objAdvBookColumns.setDblGrandTotal(rsAdvancedOrderForOpen.getDouble(22));

		    hmAdvOrderData.put(rsAdvancedOrderForOpen.getString(1), objAdvBookColumns);
		}
		rsAdvancedOrderForOpen.close();
	    }

	    if (sbSqlForBilled1.length() > 0)
	    {
		rsAdvancedOrderDataBilled1 = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlForBilled1.toString());
		while (rsAdvancedOrderDataBilled1.next())
		{

		    clsAdvBookBillHd objAdvBookColumns = new clsAdvBookBillHd();
		    objAdvBookColumns.setStrAdvBookingNo(rsAdvancedOrderDataBilled1.getString(1));
		    objAdvBookColumns.setDteAdvBookingDate(rsAdvancedOrderDataBilled1.getString(2));
		    objAdvBookColumns.setDteOrderFor(rsAdvancedOrderDataBilled1.getString(3));
		    objAdvBookColumns.setStrPOSCode(rsAdvancedOrderDataBilled1.getString(4));
		    objAdvBookColumns.setStrSettelmentMode(rsAdvancedOrderDataBilled1.getString(5));
		    objAdvBookColumns.setDblSubTotal(rsAdvancedOrderDataBilled1.getDouble(6));
		    objAdvBookColumns.setDblAdvDeposite(rsAdvancedOrderDataBilled1.getDouble(7));
		    objAdvBookColumns.setBalance(rsAdvancedOrderDataBilled1.getDouble(8));
		    objAdvBookColumns.setStrWShortName(rsAdvancedOrderDataBilled1.getString(9));
		    objAdvBookColumns.setStrAdvOrderTypeName(rsAdvancedOrderDataBilled1.getString(10));
		    objAdvBookColumns.setStrOperationType(rsAdvancedOrderDataBilled1.getString(11));
		    objAdvBookColumns.setBillNo(rsAdvancedOrderDataBilled1.getString(12));
		    objAdvBookColumns.setDblSettlementAmt(rsAdvancedOrderDataBilled1.getDouble(13));
		    objAdvBookColumns.setStrManualAdvOrderNo(rsAdvancedOrderDataBilled1.getString(16));
		    objAdvBookColumns.setDteSettleDate(rsAdvancedOrderDataBilled1.getString(17));
		    objAdvBookColumns.setStrAdvPaySettelmentMode(rsAdvancedOrderDataBilled1.getString(18));//AdvPaySettlementMode
		    objAdvBookColumns.setStrPOSName(rsAdvancedOrderDataBilled1.getString(19));//posName

		    objAdvBookColumns.setDblDiscountAmt(rsAdvancedOrderDataBilled1.getDouble(20));
		    objAdvBookColumns.setDblTaxAmt(rsAdvancedOrderDataBilled1.getDouble(21));
		    objAdvBookColumns.setDblGrandTotal(rsAdvancedOrderDataBilled1.getDouble(22));

		    hmAdvOrderData.put(rsAdvancedOrderDataBilled1.getString(1), objAdvBookColumns);
		}
		rsAdvancedOrderDataBilled1.close();
	    }
	    if (sbSqlForBilled2.length() > 0)
	    {
		rsAdvancedOrderDataBilled2 = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlForBilled2.toString());
		//settled
		while (rsAdvancedOrderDataBilled2.next())
		{

		    clsAdvBookBillHd objAdvBookColumns = new clsAdvBookBillHd();
		    objAdvBookColumns.setStrAdvBookingNo(rsAdvancedOrderDataBilled2.getString(1));
		    objAdvBookColumns.setDteAdvBookingDate(rsAdvancedOrderDataBilled2.getString(2));
		    objAdvBookColumns.setDteOrderFor(rsAdvancedOrderDataBilled2.getString(3));
		    objAdvBookColumns.setStrPOSCode(rsAdvancedOrderDataBilled2.getString(4));
		    objAdvBookColumns.setStrSettelmentMode(rsAdvancedOrderDataBilled2.getString(5));
		    objAdvBookColumns.setDblSubTotal(rsAdvancedOrderDataBilled2.getDouble(6));
		    objAdvBookColumns.setDblAdvDeposite(rsAdvancedOrderDataBilled2.getDouble(7));
		    objAdvBookColumns.setBalance(rsAdvancedOrderDataBilled2.getDouble(8));
		    objAdvBookColumns.setStrWShortName(rsAdvancedOrderDataBilled2.getString(9));
		    objAdvBookColumns.setStrAdvOrderTypeName(rsAdvancedOrderDataBilled2.getString(10));
		    objAdvBookColumns.setStrOperationType(rsAdvancedOrderDataBilled2.getString(11));
		    objAdvBookColumns.setBillNo(rsAdvancedOrderDataBilled2.getString(12));
		    objAdvBookColumns.setDblSettlementAmt(rsAdvancedOrderDataBilled2.getDouble(13));
		    objAdvBookColumns.setStrManualAdvOrderNo(rsAdvancedOrderDataBilled2.getString(16));
		    objAdvBookColumns.setDteSettleDate(rsAdvancedOrderDataBilled2.getString(17));
		    objAdvBookColumns.setStrAdvPaySettelmentMode(rsAdvancedOrderDataBilled2.getString(18));//AdvPaySettlementMode
		    objAdvBookColumns.setStrPOSName(rsAdvancedOrderDataBilled2.getString(19));//posName

		    objAdvBookColumns.setDblDiscountAmt(rsAdvancedOrderDataBilled2.getDouble(20));
		    objAdvBookColumns.setDblTaxAmt(rsAdvancedOrderDataBilled2.getDouble(21));
		    objAdvBookColumns.setDblGrandTotal(rsAdvancedOrderDataBilled2.getDouble(22));

		    hmAdvOrderData.put(rsAdvancedOrderDataBilled2.getString(1), objAdvBookColumns);
		}
		rsAdvancedOrderDataBilled2.close();
	    }
	    if (sbSqlForBilled3.length() > 0)
	    {
		rsAdvancedOrderDataBilled3 = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlForBilled3.toString());
		//settled
		while (rsAdvancedOrderDataBilled3.next())
		{

		    clsAdvBookBillHd objAdvBookColumns = new clsAdvBookBillHd();
		    objAdvBookColumns.setStrAdvBookingNo(rsAdvancedOrderDataBilled3.getString(1));
		    objAdvBookColumns.setDteAdvBookingDate(rsAdvancedOrderDataBilled3.getString(2));
		    objAdvBookColumns.setDteOrderFor(rsAdvancedOrderDataBilled3.getString(3));
		    objAdvBookColumns.setStrPOSCode(rsAdvancedOrderDataBilled3.getString(4));
		    objAdvBookColumns.setStrSettelmentMode(rsAdvancedOrderDataBilled3.getString(5));
		    objAdvBookColumns.setDblSubTotal(rsAdvancedOrderDataBilled3.getDouble(6));
		    objAdvBookColumns.setDblAdvDeposite(rsAdvancedOrderDataBilled3.getDouble(7));
		    objAdvBookColumns.setBalance(rsAdvancedOrderDataBilled3.getDouble(8));
		    objAdvBookColumns.setStrWShortName(rsAdvancedOrderDataBilled3.getString(9));
		    objAdvBookColumns.setStrAdvOrderTypeName(rsAdvancedOrderDataBilled3.getString(10));
		    objAdvBookColumns.setStrOperationType(rsAdvancedOrderDataBilled3.getString(11));
		    objAdvBookColumns.setBillNo(rsAdvancedOrderDataBilled3.getString(12));
		    objAdvBookColumns.setDblSettlementAmt(rsAdvancedOrderDataBilled3.getDouble(13));
		    objAdvBookColumns.setStrManualAdvOrderNo(rsAdvancedOrderDataBilled3.getString(16));
		    objAdvBookColumns.setDteSettleDate(rsAdvancedOrderDataBilled3.getString(17));
		    objAdvBookColumns.setStrAdvPaySettelmentMode(rsAdvancedOrderDataBilled3.getString(18));//AdvPaySettlementMode
		    objAdvBookColumns.setStrPOSName(rsAdvancedOrderDataBilled3.getString(19));//posName

		    objAdvBookColumns.setDblDiscountAmt(rsAdvancedOrderDataBilled3.getDouble(20));
		    objAdvBookColumns.setDblTaxAmt(rsAdvancedOrderDataBilled3.getDouble(21));
		    objAdvBookColumns.setDblGrandTotal(rsAdvancedOrderDataBilled3.getDouble(22));

		    hmAdvOrderData.put(rsAdvancedOrderDataBilled3.getString(1), objAdvBookColumns);
		}
		rsAdvancedOrderDataBilled3.close();
	    }

	    if (sbSqlForBilled11.length() > 0)
	    {
		rsAdvancedOrderDataBilled11 = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlForBilled11.toString());
		//billed
		while (rsAdvancedOrderDataBilled11.next())
		{

		    clsAdvBookBillHd objAdvBookColumns = new clsAdvBookBillHd();
		    objAdvBookColumns.setStrAdvBookingNo(rsAdvancedOrderDataBilled11.getString(1));
		    objAdvBookColumns.setDteAdvBookingDate(rsAdvancedOrderDataBilled11.getString(2));
		    objAdvBookColumns.setDteOrderFor(rsAdvancedOrderDataBilled11.getString(3));
		    objAdvBookColumns.setStrPOSCode(rsAdvancedOrderDataBilled11.getString(4));
		    objAdvBookColumns.setStrSettelmentMode(rsAdvancedOrderDataBilled11.getString(5));
		    objAdvBookColumns.setDblSubTotal(rsAdvancedOrderDataBilled11.getDouble(6));
		    objAdvBookColumns.setDblAdvDeposite(rsAdvancedOrderDataBilled11.getDouble(7));
		    objAdvBookColumns.setBalance(rsAdvancedOrderDataBilled11.getDouble(8));
		    objAdvBookColumns.setStrWShortName(rsAdvancedOrderDataBilled11.getString(9));
		    objAdvBookColumns.setStrAdvOrderTypeName(rsAdvancedOrderDataBilled11.getString(10));
		    objAdvBookColumns.setStrOperationType(rsAdvancedOrderDataBilled11.getString(11));
		    objAdvBookColumns.setBillNo(rsAdvancedOrderDataBilled11.getString(12));
		    objAdvBookColumns.setDblSettlementAmt(rsAdvancedOrderDataBilled11.getDouble(13));
		    objAdvBookColumns.setStrManualAdvOrderNo(rsAdvancedOrderDataBilled11.getString(16));
		    objAdvBookColumns.setDteSettleDate(rsAdvancedOrderDataBilled11.getString(17));
		    objAdvBookColumns.setStrAdvPaySettelmentMode(rsAdvancedOrderDataBilled11.getString(18));//AdvPaySettlementMode
		    objAdvBookColumns.setStrPOSName(rsAdvancedOrderDataBilled11.getString(19));//posName

		    objAdvBookColumns.setDblDiscountAmt(rsAdvancedOrderDataBilled11.getDouble(20));
		    objAdvBookColumns.setDblTaxAmt(rsAdvancedOrderDataBilled11.getDouble(21));
		    objAdvBookColumns.setDblGrandTotal(rsAdvancedOrderDataBilled11.getDouble(22));

		    hmAdvOrderData.put(rsAdvancedOrderDataBilled11.getString(1), objAdvBookColumns);
		}
		rsAdvancedOrderDataBilled11.close();
	    }
	    if (sbSqlForBilled22.length() > 0)
	    {
		rsAdvancedOrderDataBilled22 = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlForBilled22.toString());
		//billed
		while (rsAdvancedOrderDataBilled22.next())
		{

		    clsAdvBookBillHd objAdvBookColumns = new clsAdvBookBillHd();
		    objAdvBookColumns.setStrAdvBookingNo(rsAdvancedOrderDataBilled22.getString(1));
		    objAdvBookColumns.setDteAdvBookingDate(rsAdvancedOrderDataBilled22.getString(2));
		    objAdvBookColumns.setDteOrderFor(rsAdvancedOrderDataBilled22.getString(3));
		    objAdvBookColumns.setStrPOSCode(rsAdvancedOrderDataBilled22.getString(4));
		    objAdvBookColumns.setStrSettelmentMode(rsAdvancedOrderDataBilled22.getString(5));
		    objAdvBookColumns.setDblSubTotal(rsAdvancedOrderDataBilled22.getDouble(6));
		    objAdvBookColumns.setDblAdvDeposite(rsAdvancedOrderDataBilled22.getDouble(7));
		    objAdvBookColumns.setBalance(rsAdvancedOrderDataBilled22.getDouble(8));
		    objAdvBookColumns.setStrWShortName(rsAdvancedOrderDataBilled22.getString(9));
		    objAdvBookColumns.setStrAdvOrderTypeName(rsAdvancedOrderDataBilled22.getString(10));
		    objAdvBookColumns.setStrOperationType(rsAdvancedOrderDataBilled22.getString(11));
		    objAdvBookColumns.setBillNo(rsAdvancedOrderDataBilled22.getString(12));
		    objAdvBookColumns.setDblSettlementAmt(rsAdvancedOrderDataBilled22.getDouble(13));
		    objAdvBookColumns.setStrManualAdvOrderNo(rsAdvancedOrderDataBilled22.getString(16));
		    objAdvBookColumns.setDteSettleDate(rsAdvancedOrderDataBilled22.getString(17));
		    objAdvBookColumns.setStrAdvPaySettelmentMode(rsAdvancedOrderDataBilled22.getString(18));//AdvPaySettlementMode
		    objAdvBookColumns.setStrPOSName(rsAdvancedOrderDataBilled22.getString(19));//posName

		    objAdvBookColumns.setDblDiscountAmt(rsAdvancedOrderDataBilled22.getDouble(20));
		    objAdvBookColumns.setDblTaxAmt(rsAdvancedOrderDataBilled22.getDouble(21));
		    objAdvBookColumns.setDblGrandTotal(rsAdvancedOrderDataBilled22.getDouble(22));

		    hmAdvOrderData.put(rsAdvancedOrderDataBilled22.getString(1), objAdvBookColumns);
		}
		rsAdvancedOrderDataBilled22.close();
	    }

	    if (sbSqlForBilled33.length() > 0)
	    {
		rsAdvancedOrderDataBilled33 = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlForBilled33.toString());
		//settled
		while (rsAdvancedOrderDataBilled33.next())
		{

		    clsAdvBookBillHd objAdvBookColumns = new clsAdvBookBillHd();
		    objAdvBookColumns.setStrAdvBookingNo(rsAdvancedOrderDataBilled33.getString(1));
		    objAdvBookColumns.setDteAdvBookingDate(rsAdvancedOrderDataBilled33.getString(2));
		    objAdvBookColumns.setDteOrderFor(rsAdvancedOrderDataBilled33.getString(3));
		    objAdvBookColumns.setStrPOSCode(rsAdvancedOrderDataBilled33.getString(4));
		    objAdvBookColumns.setStrSettelmentMode(rsAdvancedOrderDataBilled33.getString(5));
		    objAdvBookColumns.setDblSubTotal(rsAdvancedOrderDataBilled33.getDouble(6));
		    objAdvBookColumns.setDblAdvDeposite(rsAdvancedOrderDataBilled33.getDouble(7));
		    objAdvBookColumns.setBalance(rsAdvancedOrderDataBilled33.getDouble(8));
		    objAdvBookColumns.setStrWShortName(rsAdvancedOrderDataBilled33.getString(9));
		    objAdvBookColumns.setStrAdvOrderTypeName(rsAdvancedOrderDataBilled33.getString(10));
		    objAdvBookColumns.setStrOperationType(rsAdvancedOrderDataBilled33.getString(11));
		    objAdvBookColumns.setBillNo(rsAdvancedOrderDataBilled33.getString(12));
		    objAdvBookColumns.setDblSettlementAmt(rsAdvancedOrderDataBilled33.getDouble(13));
		    objAdvBookColumns.setStrManualAdvOrderNo(rsAdvancedOrderDataBilled33.getString(16));
		    objAdvBookColumns.setDteSettleDate(rsAdvancedOrderDataBilled33.getString(17));
		    objAdvBookColumns.setStrAdvPaySettelmentMode(rsAdvancedOrderDataBilled33.getString(18));//AdvPaySettlementMode
		    objAdvBookColumns.setStrPOSName(rsAdvancedOrderDataBilled33.getString(19));//posName

		    objAdvBookColumns.setDblDiscountAmt(rsAdvancedOrderDataBilled33.getDouble(20));
		    objAdvBookColumns.setDblTaxAmt(rsAdvancedOrderDataBilled33.getDouble(21));
		    objAdvBookColumns.setDblGrandTotal(rsAdvancedOrderDataBilled33.getDouble(22));

		    hmAdvOrderData.put(rsAdvancedOrderDataBilled33.getString(1), objAdvBookColumns);
		}
		rsAdvancedOrderDataBilled33.close();
	    }

	    Object[] records = new Object[19];
	    for (clsAdvBookBillHd objAdvBookBillHd : hmAdvOrderData.values())
	    {

		records[0] = objAdvBookBillHd.getStrAdvBookingNo();
		records[1] = objAdvBookBillHd.getDteAdvBookingDate();
		records[2] = objAdvBookBillHd.getDteOrderFor();
		records[3] = objAdvBookBillHd.getDteSettleDate();
		records[4] = objAdvBookBillHd.getStrWShortName();
		records[5] = objAdvBookBillHd.getStrAdvOrderTypeName();
		records[6] = objAdvBookBillHd.getStrOperationType();
		records[7] = objAdvBookBillHd.getStrPOSName();
		records[8] = objAdvBookBillHd.getStrSettelmentMode();
		records[9] = gDecimalFormat.format(objAdvBookBillHd.getDblSubTotal());
		records[10] = gDecimalFormat.format(objAdvBookBillHd.getDblDiscountAmt());
		records[11] = gDecimalFormat.format(objAdvBookBillHd.getDblTaxAmt());
		records[12] = gDecimalFormat.format(objAdvBookBillHd.getDblGrandTotal());
		records[13] = objAdvBookBillHd.getStrAdvPaySettelmentMode();
		records[14] = gDecimalFormat.format(objAdvBookBillHd.getDblAdvDeposite());
		records[15] = objAdvBookBillHd.getBalance();
		records[16] = gDecimalFormat.format(objAdvBookBillHd.getBillNo());
		records[17] = gDecimalFormat.format(objAdvBookBillHd.getDblSettlementAmt());
		records[18] = objAdvBookBillHd.getStrManualAdvOrderNo();

		sumSumTotal = sumSumTotal + objAdvBookBillHd.getDblSubTotal();
		sumDisc = sumDisc + objAdvBookBillHd.getDblDiscountAmt();
		sumTax = sumTax + objAdvBookBillHd.getDblTaxAmt();
		sumGt = sumGt + objAdvBookBillHd.getDblGrandTotal();
		sumAdvAmt = sumAdvAmt + objAdvBookBillHd.getDblAdvDeposite();
		sumBal = sumBal + objAdvBookBillHd.getBalance();
		sumSettlement = sumSettlement + objAdvBookBillHd.getDblSettlementAmt();

		dm.addRow(records);
	    }

	    tblAdvOrder.setModel(dm);
	    Object[] total
		    =
		    {
			"Total", gDecimalFormat.format(sumSumTotal), gDecimalFormat.format(sumDisc), gDecimalFormat.format(sumTax), gDecimalFormat.format(sumGt), gDecimalFormat.format(sumAdvAmt), gDecimalFormat.format(sumBal), gDecimalFormat.format(sumSettlement)
		    };
	    dm2.addRow(total);
	    tblTotal.setModel(dm2);

	    DefaultTableCellRenderer rightRenderer1 = new DefaultTableCellRenderer();
	    rightRenderer1.setHorizontalAlignment(JLabel.RIGHT);

	    tblAdvOrder.getColumnModel().getColumn(9).setCellRenderer(rightRenderer1);
	    tblAdvOrder.getColumnModel().getColumn(10).setCellRenderer(rightRenderer1);
	    tblAdvOrder.getColumnModel().getColumn(11).setCellRenderer(rightRenderer1);
	    tblAdvOrder.getColumnModel().getColumn(12).setCellRenderer(rightRenderer1);
	    tblAdvOrder.getColumnModel().getColumn(14).setCellRenderer(rightRenderer1);
	    tblAdvOrder.getColumnModel().getColumn(15).setCellRenderer(rightRenderer1);
	    tblAdvOrder.getColumnModel().getColumn(17).setCellRenderer(rightRenderer1);

	    tblAdvOrder.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    tblAdvOrder.getColumnModel().getColumn(0).setPreferredWidth(80);
	    tblAdvOrder.getColumnModel().getColumn(1).setPreferredWidth(90);
	    tblAdvOrder.getColumnModel().getColumn(2).setPreferredWidth(80);
	    tblAdvOrder.getColumnModel().getColumn(3).setPreferredWidth(90);
	    tblAdvOrder.getColumnModel().getColumn(4).setPreferredWidth(70);
	    tblAdvOrder.getColumnModel().getColumn(5).setPreferredWidth(75);
	    tblAdvOrder.getColumnModel().getColumn(6).setPreferredWidth(90);
	    tblAdvOrder.getColumnModel().getColumn(7).setPreferredWidth(80);
	    tblAdvOrder.getColumnModel().getColumn(8).setPreferredWidth(90);
	    tblAdvOrder.getColumnModel().getColumn(9).setPreferredWidth(70);
	    tblAdvOrder.getColumnModel().getColumn(10).setPreferredWidth(50);
	    tblAdvOrder.getColumnModel().getColumn(11).setPreferredWidth(50);
	    tblAdvOrder.getColumnModel().getColumn(12).setPreferredWidth(90);
	    tblAdvOrder.getColumnModel().getColumn(13).setPreferredWidth(90);

	    tblTotal.getColumnModel().getColumn(1).setCellRenderer(rightRenderer1);
	    tblTotal.getColumnModel().getColumn(2).setCellRenderer(rightRenderer1);
	    tblTotal.getColumnModel().getColumn(3).setCellRenderer(rightRenderer1);
	    tblTotal.getColumnModel().getColumn(4).setCellRenderer(rightRenderer1);

	    tblTotal.getColumnModel().getColumn(5).setCellRenderer(rightRenderer1);
	    tblTotal.getColumnModel().getColumn(6).setCellRenderer(rightRenderer1);
	    tblTotal.getColumnModel().getColumn(7).setCellRenderer(rightRenderer1);

	    tblTotal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    tblTotal.getColumnModel().getColumn(0).setPreferredWidth(750);
	    tblTotal.getColumnModel().getColumn(1).setPreferredWidth(70);
	    tblTotal.getColumnModel().getColumn(2).setPreferredWidth(70);
	    tblTotal.getColumnModel().getColumn(3).setPreferredWidth(70);
	    tblTotal.getColumnModel().getColumn(4).setPreferredWidth(100);
	    tblTotal.getColumnModel().getColumn(5).setPreferredWidth(100);
	    tblTotal.getColumnModel().getColumn(6).setPreferredWidth(100);
	    tblTotal.getColumnModel().getColumn(7).setPreferredWidth(100);

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funMenuHeadWise()
    {
	Map<String, clsAdvBookBillHd> hmAdvOrderData = new HashMap<String, clsAdvBookBillHd>();
	try
	{
	    String posCode = cmbPosCode.getSelectedItem().toString();
	    StringBuilder sb = new StringBuilder(posCode);
	    int len = posCode.length();
	    int lastInd = sb.lastIndexOf(" ");
	    String pos = sb.substring(lastInd + 1, len).toString();
	    java.util.Date date = dteFromDate.getDate();
	    fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());
	    toDate = objUtility.funGetFromToDate(dteToDate.getDate());
	    double sumGt = 0.00;
	    double sumQty = 0.00;
	    dm.getDataVector().removeAllElements();
	    dm = new DefaultTableModel();
	    dm.addColumn("Adv Ord No");
	    dm.addColumn("Order For");
	    dm.addColumn("Bill Date");
	    dm.addColumn("Customer Name");
	    dm.addColumn("Order Type");
	    dm.addColumn("Operation Type");
	    dm.addColumn("Menu Name");
	    dm.addColumn("Qty");
	    dm.addColumn("Amount");
	    dm.addColumn("Manul Adv Order No");
	    DefaultTableModel dm2 = new DefaultTableModel();
	    dm2.getDataVector().removeAllElements();
	    dm2.addColumn("");
	    dm2.addColumn("");
	    dm2.addColumn("");
	    dm2.addColumn("");
	    sb.setLength(0);
	    sb.append("  select a.strAdvBookingNo,DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y') as Orderfor,\n"
		    + " DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y') as BillDate,e.strCustomerName as CustomerName,\n"
		    + " ifnull(f.strAdvOrderTypeName,'') as OrderType,ifnull(d.strOperational,'') as OperationType  ,ifnull(d.strMenuName,'') as MenuName,\n"
		    + " sum(b.dblQuantity) as Qty,sum(b.dblAmount) as Amount,a.strManualAdvOrderNo \n"
		    + " from tblqadvbookbillhd a \n"
		    + " left outer join  tblqadvbookbilldtl b on a.strAdvBookingNo=b.strAdvBookingNo \n"
		    + " left outer join tblmenuitempricingdtl c on  b.strItemCode=c.strItemCode \n"
		    + " left outer join tblmenuhd d on c.strMenuCode=d.strMenuCode \n"
		    + " left outer join tblcustomermaster e on a.strCustomerCode=e.strCustomerCode \n"
		    + " left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode ");

	    if ("All".equals(pos))
	    {
		if (txtCustomerName.getText().equals(""))
		{
		    sb.append(" and   Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' ");
		    if (!cmbOrderMode.getSelectedItem().toString().trim().equals("All"))
		    {
			if (hmAdvOrderType.size() > 0)
			{
			    String orderMode = cmbOrderMode.getSelectedItem().toString().trim();
			    sb.append(" and f.strAdvOrderTypeCode='" + hmAdvOrderType.get(orderMode) + "' ");
			}
		    }
		    if (!cmbTransType.getSelectedItem().toString().trim().equals("All"))
		    {
			sb.append(" and d.strOperational='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		    }

		    sb.append(" group by a.strAdvBookingNo, d.strMenuName");
		    if (cmbDateFilter.getSelectedItem().toString().equalsIgnoreCase("Order Date"))
		    {
			sb.append(" order by a.dteOrderFor,strAdvBookingNo");
		    }
		    else
		    {
			sb.append(" order by a.dteAdvBookingDate,strAdvBookingNo");
		    }
		    ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
		    while (rs.next())
		    {
			Object[] row
				=
				{
				    rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8), gDecimalFormat.format(rs.getString(9)), rs.getString(10)
				};
			dm.addRow(row);
			sumQty = sumQty + rs.getDouble(8);
			sumGt = sumGt + rs.getDouble(9);
		    }
		}
		else
		{
		    sb.append(" and  a.strCustomerCode='" + custCode + "'  and  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' ");
		    if (!cmbOrderMode.getSelectedItem().toString().trim().equals("All"))
		    {
			if (hmAdvOrderType.size() > 0)
			{
			    String orderMode = cmbOrderMode.getSelectedItem().toString().trim();
			    sb.append(" and f.strAdvOrderTypeCode='" + hmAdvOrderType.get(orderMode) + "' ");
			}
		    }
		    if (!cmbTransType.getSelectedItem().toString().trim().equals("All"))
		    {
			sb.append(" and d.strOperational='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		    }
		    sb.append(" group by a.strAdvBookingNo, d.strMenuName");
		    if (cmbDateFilter.getSelectedItem().toString().equalsIgnoreCase("Order Date"))
		    {
			sb.append(" order by a.dteOrderFor,strAdvBookingNo");
		    }
		    else
		    {
			sb.append(" order by a.dteAdvBookingDate,strAdvBookingNo");
		    }
		    ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
		    while (rs.next())
		    {
			Object[] row
				=
				{
				    rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8), gDecimalFormat.format(rs.getString(9)), rs.getString(10)
				};
			dm.addRow(row);
			sumQty = sumQty + rs.getDouble(8);
			sumGt = sumGt + rs.getDouble(9);
		    }
		}
		tblAdvOrder.setModel(dm);
		Object[] total
			=
			{
			    "Total", "", sumQty, gDecimalFormat.format(sumGt)
			};
		dm2.addRow(total);
		tblTotal.setModel(dm2);
	    }
	    else
	    {
		if (txtCustomerName.getText().equals(""))
		{
		    sb.append(" and a.strPOSCode='" + pos + "' and  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' ");
		    if (!cmbOrderMode.getSelectedItem().toString().trim().equals("All"))
		    {
			if (hmAdvOrderType.size() > 0)
			{
			    String orderMode = cmbOrderMode.getSelectedItem().toString().trim();
			    sb.append("and f.strAdvOrderTypeCode='" + hmAdvOrderType.get(orderMode) + "' ");
			}
		    }
		    if (!cmbTransType.getSelectedItem().toString().trim().equals("All"))
		    {
			sb.append("and d.strOperational='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		    }
		    sb.append(" group by a.strAdvBookingNo, d.strMenuName");
		    if (cmbDateFilter.getSelectedItem().toString().equalsIgnoreCase("Order Date"))
		    {
			sb.append(" order by dteOrderFor,strAdvBookingNo");
		    }
		    else
		    {
			sb.append(" order by dteAdvBookingDate,strAdvBookingNo");
		    }
		    ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
		    while (rs.next())
		    {
			Object[] row
				=
				{
				    rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8), gDecimalFormat.format(rs.getString(9)), rs.getString(10)
				};
			dm.addRow(row);
			sumQty = sumQty + rs.getDouble(8);
			sumGt = sumGt + rs.getDouble(9);
		    }
		}
		else
		{
		    sb.append(" and  a.strPOSCode='" + pos + "' and a.strCustomerCode='" + custCode + "'  and  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' ");
		    if (!cmbOrderMode.getSelectedItem().toString().trim().equals("All"))
		    {
			if (hmAdvOrderType.size() > 0)
			{
			    String orderMode = cmbOrderMode.getSelectedItem().toString().trim();
			    sb.append("and f.strAdvOrderTypeCode='" + hmAdvOrderType.get(orderMode) + "' ");
			}
		    }
		    if (!cmbTransType.getSelectedItem().toString().trim().equals("All"))
		    {
			sb.append("and d.strOperational='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		    }
		    sb.append(" group by a.strAdvBookingNo, d.strMenuName");
		    if (cmbDateFilter.getSelectedItem().toString().equalsIgnoreCase("Order Date"))
		    {
			sb.append(" order by dteOrderFor,strAdvBookingNo");
		    }
		    else
		    {
			sb.append(" order by dteAdvBookingDate,strAdvBookingNo");
		    }
		    ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
		    while (rs.next())
		    {
			Object[] row
				=
				{
				    rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8), gDecimalFormat.format(rs.getString(9)), rs.getString(10)
				};
			dm.addRow(row);
			sumQty = sumQty + rs.getDouble(8);
			sumGt = sumGt + rs.getDouble(9);
		    }
		}
		tblAdvOrder.setModel(dm);
		Object[] total
			=
			{
			    "Total", sumQty, gDecimalFormat.format(sumGt)
			};
		dm2.addRow(total);
		tblTotal.setModel(dm2);
	    }

	    DefaultTableCellRenderer rightRenderer1 = new DefaultTableCellRenderer();
	    rightRenderer1.setHorizontalAlignment(JLabel.RIGHT);
	    tblAdvOrder.getColumnModel().getColumn(7).setCellRenderer(rightRenderer1);
	    tblAdvOrder.getColumnModel().getColumn(8).setCellRenderer(rightRenderer1);
	    tblAdvOrder.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

	    tblAdvOrder.getColumnModel().getColumn(0).setPreferredWidth(80);// Adv Order No
	    tblAdvOrder.getColumnModel().getColumn(1).setPreferredWidth(80);// Order For
	    tblAdvOrder.getColumnModel().getColumn(2).setPreferredWidth(80);// Bill Date
	    tblAdvOrder.getColumnModel().getColumn(3).setPreferredWidth(120);// Cust Name
	    tblAdvOrder.getColumnModel().getColumn(4).setPreferredWidth(80);// Order Type
	    tblAdvOrder.getColumnModel().getColumn(5).setPreferredWidth(80);// Op Type
	    tblAdvOrder.getColumnModel().getColumn(6).setPreferredWidth(120);// Item Name
	    tblAdvOrder.getColumnModel().getColumn(7).setPreferredWidth(70);// Qty
	    tblAdvOrder.getColumnModel().getColumn(8).setPreferredWidth(70);// Amt\
	    tblAdvOrder.getColumnModel().getColumn(9).setPreferredWidth(70);// Manual Adv Order No

	    tblTotal.getColumnModel().getColumn(1).setCellRenderer(rightRenderer1);
	    tblTotal.getColumnModel().getColumn(2).setCellRenderer(rightRenderer1);
	    tblTotal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    tblTotal.getColumnModel().getColumn(0).setPreferredWidth(570);
	    tblTotal.getColumnModel().getColumn(1).setPreferredWidth(70);
	    tblTotal.getColumnModel().getColumn(2).setPreferredWidth(70);
	    tblTotal.getColumnModel().getColumn(3).setPreferredWidth(70);
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funGroupWise()
    {
	Map<String, clsAdvBookBillHd> hmAdvOrderData = new HashMap<String, clsAdvBookBillHd>();
	try
	{
	    String posCode = cmbPosCode.getSelectedItem().toString();
	    StringBuilder sb = new StringBuilder(posCode);
	    int len = posCode.length();
	    int lastInd = sb.lastIndexOf(" ");
	    String pos = sb.substring(lastInd + 1, len).toString();
	    fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());
	    toDate = objUtility.funGetFromToDate(dteToDate.getDate());
	    double sumGt = 0.00;
	    double sumQty = 0.00;
	    dm.getDataVector().removeAllElements();
	    dm = new DefaultTableModel();
	    dm.addColumn("Adv Ord No");
	    dm.addColumn("Order For");
	    dm.addColumn("Bill Date");
	    dm.addColumn("Customer Name");
	    dm.addColumn("Order Type");
	    dm.addColumn("Operation Type");
	    dm.addColumn("Group Name");
	    dm.addColumn("Qty");
	    dm.addColumn("Amount");
	    dm.addColumn("Manual Adv Order No");
	    DefaultTableModel dm2 = new DefaultTableModel();
	    dm2.getDataVector().removeAllElements();
	    dm2.addColumn("");
	    dm2.addColumn("");
	    dm2.addColumn("");
	    dm2.addColumn("");

	    StringBuilder sbSqlForOpen = new StringBuilder(); // Adv Live only
	    StringBuilder sbSqlForBilled1 = new StringBuilder(); // Adv Live + Bill Live
	    StringBuilder sbSqlForBilled2 = new StringBuilder(); //Adv QFile + Bill Live
	    StringBuilder sbSqlForBilled3 = new StringBuilder(); //Adv QFile + Bill QFile
	    StringBuilder sbSqlForBilled11 = new StringBuilder(); // Adv Live + Bill Live
	    StringBuilder sbSqlForBilled22 = new StringBuilder(); //Adv QFile + Bill Live
	    StringBuilder sbSqlForBilled33 = new StringBuilder(); //Adv QFile + Bill QFile

	    sbSqlForOpen.setLength(0);
	    sbSqlForBilled1.setLength(0);
	    sbSqlForBilled2.setLength(0);
	    sbSqlForBilled3.setLength(0);
	    sbSqlForBilled11.setLength(0);
	    sbSqlForBilled22.setLength(0);
	    sbSqlForBilled33.setLength(0);

	    if (cmbStatus.getSelectedItem().equals("Open")) // For Open Adv Order Option
	    {
		sbSqlForOpen.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y'),a.strPOSCode"
			+ ",c.strCustomerName ,i.strGroupName,sum(e.dblQuantity),sum(e.dblAmount),''"
			+ ",ifnull(f.strAdvOrderTypeName,'') ,ifnull(d.strOperationType,''),'','0'"
			+ ",'" + clsGlobalVarClass.gUserCode + "','" + clsGlobalVarClass.gClientCode + "'"
			+ ",a.strManualAdvOrderNo "
			+ "from tbladvbookbillhd a left outer join tblbillhd d on a.strAdvBookingNo=d.strAdvBookingNo "
			+ "left outer join tbladvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo "
			+ "left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode  "
			+ "left outer join tbladvbookbilldtl e on e.strAdvBookingNo=a.strAdvBookingNo "
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode "
			+ "left outer join tblitemmaster g on e.strItemCode=g.strItemCode "
			+ "left outer join tblsubgrouphd h on  g.strSubGroupCode=h.strSubGroupCode "
			+ "left outer join tblgrouphd i on h.strGroupCode=i.strGroupCode "
			+ "where  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "'  "
			+ "and d.strBillNo is null ");

		if (clsGlobalVarClass.gHOPOSType.equalsIgnoreCase("HOPOS"))
		{
		    sbSqlForOpen.setLength(0);
		    sbSqlForOpen.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y'),a.strPOSCode"
			    + ",c.strCustomerName ,i.strGroupName,sum(e.dblQuantity),sum(e.dblAmount),''"
			    + ",ifnull(f.strAdvOrderTypeName,'') ,ifnull(d.strOperationType,''),'','0'"
			    + ",'" + clsGlobalVarClass.gUserCode + "','" + clsGlobalVarClass.gClientCode + "'"
			    + ",a.strManualAdvOrderNo "
			    + "from tblqadvbookbillhd a left outer join tblbillhd d on a.strAdvBookingNo=d.strAdvBookingNo "
			    + "left outer join tblqadvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo "
			    + "left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode  "
			    + "left outer join tblqadvbookbilldtl e on e.strAdvBookingNo=a.strAdvBookingNo "
			    + "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode "
			    + "left outer join tblitemmaster g on e.strItemCode=g.strItemCode "
			    + "left outer join tblsubgrouphd h on  g.strSubGroupCode=h.strSubGroupCode "
			    + "left outer join tblgrouphd i on h.strGroupCode=i.strGroupCode "
			    + "where  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "'  "
			    + "and d.strBillNo is null ");
		}
	    }
	    else if (cmbStatus.getSelectedItem().equals("Settled")) // For Settled Option
	    {
		sbSqlForBilled1.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y'),a.strPOSCode,c.strCustomerName "
			+ ",i.strGroupName,sum(e.dblQuantity),sum(e.dblAmount),'',ifnull(f.strAdvOrderTypeName,'') "
			+ ",ifnull(d.strOperationType,''),'','0','" + clsGlobalVarClass.gUserCode + "'"
			+ ",'" + clsGlobalVarClass.gClientCode + "',a.strManualAdvOrderNo "
			+ "from tbladvbookbillhd a left outer join tblbillhd d on a.strAdvBookingNo=d.strAdvBookingNo "
			+ " inner join tblbillsettlementdtl j on d.strBillNo=j.strBillNo "
			+ "left outer join tbladvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo  "
			+ "left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode  "
			+ "left outer join tbladvbookbilldtl e on e.strAdvBookingNo=a.strAdvBookingNo  "
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode "
			+ "left outer join tblitemmaster g on e.strItemCode=g.strItemCode "
			+ "left outer join tblsubgrouphd h on  g.strSubGroupCode=h.strSubGroupCode "
			+ "left outer join tblgrouphd i on h.strGroupCode=i.strGroupCode "
			+ "where  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' "
			+ "and d.strBillNo is not null ");

		sbSqlForBilled2.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y'),a.strPOSCode,c.strCustomerName "
			+ ",i.strGroupName,sum(e.dblQuantity),sum(e.dblAmount),'',ifnull(f.strAdvOrderTypeName,'') "
			+ ",ifnull(d.strOperationType,''),'','0','" + clsGlobalVarClass.gUserCode + "'"
			+ ",'" + clsGlobalVarClass.gClientCode + "',a.strManualAdvOrderNo "
			+ "from tblqadvbookbillhd a left outer join tblbillhd d on a.strAdvBookingNo=d.strAdvBookingNo "
			+ " inner join tblbillsettlementdtl j on d.strBillNo=j.strBillNo "
			+ "left outer join tblqadvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo "
			+ "left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode "
			+ "left outer join tblqadvbookbilldtl e on e.strAdvBookingNo=a.strAdvBookingNo "
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode "
			+ "left outer join tblitemmaster g on e.strItemCode=g.strItemCode "
			+ "left outer join tblsubgrouphd h on  g.strSubGroupCode=h.strSubGroupCode "
			+ "left outer join tblgrouphd i on h.strGroupCode=i.strGroupCode  "
			+ "where  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' ");

		sbSqlForBilled3.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y'),a.strPOSCode,c.strCustomerName "
			+ ",i.strGroupName,sum(e.dblQuantity),sum(e.dblAmount),'',ifnull(f.strAdvOrderTypeName,'') "
			+ ",ifnull(d.strOperationType,''),'','0','" + clsGlobalVarClass.gUserCode + "'"
			+ ",'" + clsGlobalVarClass.gClientCode + "',a.strManualAdvOrderNo "
			+ "from tblqadvbookbillhd a left outer join tblqbillhd d on a.strAdvBookingNo=d.strAdvBookingNo "
			+ " inner join tblbillsettlementdtl j on d.strBillNo=j.strBillNo "
			+ "left outer join tblqadvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo "
			+ "left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode "
			+ "left outer join tblqadvbookbilldtl e on e.strAdvBookingNo=a.strAdvBookingNo "
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode "
			+ "left outer join tblitemmaster g on e.strItemCode=g.strItemCode "
			+ "left outer join tblsubgrouphd h on  g.strSubGroupCode=h.strSubGroupCode "
			+ "left outer join tblgrouphd i on h.strGroupCode=i.strGroupCode  "
			+ "where  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' "
			+ "and d.strSettelmentMode is not null ");

		if (!cmbTransType.getSelectedItem().toString().trim().equals("All"))
		{
		    sbSqlForBilled1.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		    sbSqlForBilled2.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		    sbSqlForBilled3.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		}
	    }
	    else if (cmbStatus.getSelectedItem().equals("Billed")) // For Billed Option
	    {
		sbSqlForBilled1.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y'),a.strPOSCode,c.strCustomerName "
			+ ",ifnull(i.strGroupName,''),sum(e.dblQuantity),sum(e.dblAmount),'',ifnull(f.strAdvOrderTypeName,'') "
			+ ",ifnull(d.strOperationType,''),'','0','" + clsGlobalVarClass.gUserCode + "'"
			+ ",'" + clsGlobalVarClass.gClientCode + "',a.strManualAdvOrderNo "
			+ "from tbladvbookbillhd a left outer join tblbillhd d on a.strAdvBookingNo=d.strAdvBookingNo  "
			+ "left outer join tbladvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo  "
			+ "left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode  "
			+ "left outer join tbladvbookbilldtl e on e.strAdvBookingNo=a.strAdvBookingNo  "
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode "
			+ "left outer join tblitemmaster g on e.strItemCode=g.strItemCode "
			+ "left outer join tblsubgrouphd h on  g.strSubGroupCode=h.strSubGroupCode "
			+ "left outer join tblgrouphd i on h.strGroupCode=i.strGroupCode "
			+ "where  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' "
			+ "and d.strBillNo not in(select strBillNo from tblbillsettlementdtl) "
			+ " and d.strBillNo not in(select strBillNo from tblqbillsettlementdtl) "
			+ "and d.strBillNo is not null ");

		sbSqlForBilled2.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y'),a.strPOSCode,c.strCustomerName "
			+ ",ifnull(i.strGroupName,''),sum(e.dblQuantity),sum(e.dblAmount),'',ifnull(f.strAdvOrderTypeName,'') "
			+ ",ifnull(d.strOperationType,''),'','0','" + clsGlobalVarClass.gUserCode + "'"
			+ ",'" + clsGlobalVarClass.gClientCode + "',a.strManualAdvOrderNo "
			+ "from tblqadvbookbillhd a left outer join tblbillhd d on a.strAdvBookingNo=d.strAdvBookingNo "
			+ "left outer join tblqadvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo "
			+ "left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode "
			+ "left outer join tblqadvbookbilldtl e on e.strAdvBookingNo=a.strAdvBookingNo "
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode "
			+ "left outer join tblitemmaster g on e.strItemCode=g.strItemCode "
			+ "left outer join tblsubgrouphd h on  g.strSubGroupCode=h.strSubGroupCode "
			+ "left outer join tblgrouphd i on h.strGroupCode=i.strGroupCode  "
			+ "where  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' "
			+ "and d.strBillNo not in(select strBillNo from tblbillsettlementdtl) "
			+ " and d.strBillNo not in(select strBillNo from tblqbillsettlementdtl) ");

		sbSqlForBilled3.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y'),a.strPOSCode,c.strCustomerName "
			+ ",ifnull(i.strGroupName,''),sum(e.dblQuantity),sum(e.dblAmount),'',ifnull(f.strAdvOrderTypeName,'') "
			+ ",ifnull(d.strOperationType,''),'','0','" + clsGlobalVarClass.gUserCode + "'"
			+ ",'" + clsGlobalVarClass.gClientCode + "',a.strManualAdvOrderNo "
			+ "from tblqadvbookbillhd a left outer join tblqbillhd d on a.strAdvBookingNo=d.strAdvBookingNo "
			+ "left outer join tblqadvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo "
			+ "left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode "
			+ "left outer join tblqadvbookbilldtl e on e.strAdvBookingNo=a.strAdvBookingNo "
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode "
			+ "left outer join tblitemmaster g on e.strItemCode=g.strItemCode "
			+ "left outer join tblsubgrouphd h on  g.strSubGroupCode=h.strSubGroupCode "
			+ "left outer join tblgrouphd i on h.strGroupCode=i.strGroupCode  "
			+ "where  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' "
			+ "and d.strBillNo not in(select strBillNo from tblbillsettlementdtl) "
			+ " and d.strBillNo not in(select strBillNo from tblqbillsettlementdtl) "
			+ "and d.strSettelmentMode is not null ");

		if (!cmbTransType.getSelectedItem().toString().trim().equals("All"))
		{
		    sbSqlForBilled1.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		    sbSqlForBilled2.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		    sbSqlForBilled3.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		}
	    }
	    else // For Both Option
	    {
		//settled
		sbSqlForBilled1.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y'),a.strPOSCode,c.strCustomerName "
			+ ",i.strGroupName,sum(e.dblQuantity),sum(e.dblAmount),'',ifnull(f.strAdvOrderTypeName,'') "
			+ ",ifnull(d.strOperationType,''),'','0','" + clsGlobalVarClass.gUserCode + "'"
			+ ",'" + clsGlobalVarClass.gClientCode + "',a.strManualAdvOrderNo "
			+ "from tbladvbookbillhd a left outer join tblbillhd d on a.strAdvBookingNo=d.strAdvBookingNo  "
			+ " inner join tblbillsettlementdtl j on d.strBillNo=j.strBillNo "
			+ "left outer join tbladvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo  "
			+ "left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode  "
			+ "left outer join tbladvbookbilldtl e on e.strAdvBookingNo=a.strAdvBookingNo  "
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode "
			+ "left outer join tblitemmaster g on e.strItemCode=g.strItemCode "
			+ "left outer join tblsubgrouphd h on  g.strSubGroupCode=h.strSubGroupCode "
			+ "left outer join tblgrouphd i on h.strGroupCode=i.strGroupCode "
			+ "where  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' "
			+ "and d.strBillNo is not null ");

		sbSqlForBilled2.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y'),a.strPOSCode,c.strCustomerName "
			+ ",i.strGroupName,sum(e.dblQuantity),sum(e.dblAmount),'',ifnull(f.strAdvOrderTypeName,'') "
			+ ",ifnull(d.strOperationType,''),'','0','" + clsGlobalVarClass.gUserCode + "'"
			+ ",'" + clsGlobalVarClass.gClientCode + "',a.strManualAdvOrderNo "
			+ "from tblqadvbookbillhd a left outer join tblbillhd d on a.strAdvBookingNo=d.strAdvBookingNo "
			+ " inner join tblbillsettlementdtl j on d.strBillNo=j.strBillNo "
			+ "left outer join tblqadvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo "
			+ "left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode "
			+ "left outer join tblqadvbookbilldtl e on e.strAdvBookingNo=a.strAdvBookingNo "
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode "
			+ "left outer join tblitemmaster g on e.strItemCode=g.strItemCode "
			+ "left outer join tblsubgrouphd h on  g.strSubGroupCode=h.strSubGroupCode "
			+ "left outer join tblgrouphd i on h.strGroupCode=i.strGroupCode  "
			+ "where  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' ");

		sbSqlForBilled3.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y'),a.strPOSCode,c.strCustomerName "
			+ ",i.strGroupName,sum(e.dblQuantity),sum(e.dblAmount),'',ifnull(f.strAdvOrderTypeName,'') "
			+ ",ifnull(d.strOperationType,''),'','0','" + clsGlobalVarClass.gUserCode + "'"
			+ ",'" + clsGlobalVarClass.gClientCode + "',a.strManualAdvOrderNo "
			+ "from tblqadvbookbillhd a left outer join tblqbillhd d on a.strAdvBookingNo=d.strAdvBookingNo "
			+ " inner join tblbillsettlementdtl j on d.strBillNo=j.strBillNo "
			+ "left outer join tblqadvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo "
			+ "left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode "
			+ "left outer join tblqadvbookbilldtl e on e.strAdvBookingNo=a.strAdvBookingNo "
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode "
			+ "left outer join tblitemmaster g on e.strItemCode=g.strItemCode "
			+ "left outer join tblsubgrouphd h on  g.strSubGroupCode=h.strSubGroupCode "
			+ "left outer join tblgrouphd i on h.strGroupCode=i.strGroupCode  "
			+ "where  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' "
			+ "and d.strSettelmentMode is not null ");

		//billed
		sbSqlForBilled11.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y'),a.strPOSCode,c.strCustomerName "
			+ ",ifnull(i.strGroupName,''),sum(e.dblQuantity),sum(e.dblAmount),'',ifnull(f.strAdvOrderTypeName,'') "
			+ ",ifnull(d.strOperationType,''),'','0','" + clsGlobalVarClass.gUserCode + "'"
			+ ",'" + clsGlobalVarClass.gClientCode + "',a.strManualAdvOrderNo "
			+ "from tbladvbookbillhd a left outer join tblbillhd d on a.strAdvBookingNo=d.strAdvBookingNo  "
			+ "left outer join tbladvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo  "
			+ "left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode  "
			+ "left outer join tbladvbookbilldtl e on e.strAdvBookingNo=a.strAdvBookingNo  "
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode "
			+ "left outer join tblitemmaster g on e.strItemCode=g.strItemCode "
			+ "left outer join tblsubgrouphd h on  g.strSubGroupCode=h.strSubGroupCode "
			+ "left outer join tblgrouphd i on h.strGroupCode=i.strGroupCode "
			+ "where  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' "
			+ "and d.strBillNo not in(select strBillNo from tblbillsettlementdtl) "
			+ " and d.strBillNo not in(select strBillNo from tblqbillsettlementdtl) "
			+ "and d.strBillNo is not null ");

		sbSqlForBilled22.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y'),a.strPOSCode,c.strCustomerName "
			+ ",ifnull(i.strGroupName,''),sum(e.dblQuantity),sum(e.dblAmount),'',ifnull(f.strAdvOrderTypeName,'') "
			+ ",ifnull(d.strOperationType,''),'','0','" + clsGlobalVarClass.gUserCode + "'"
			+ ",'" + clsGlobalVarClass.gClientCode + "',a.strManualAdvOrderNo "
			+ "from tblqadvbookbillhd a left outer join tblbillhd d on a.strAdvBookingNo=d.strAdvBookingNo "
			+ "left outer join tblqadvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo "
			+ "left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode "
			+ "left outer join tblqadvbookbilldtl e on e.strAdvBookingNo=a.strAdvBookingNo "
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode "
			+ "left outer join tblitemmaster g on e.strItemCode=g.strItemCode "
			+ "left outer join tblsubgrouphd h on  g.strSubGroupCode=h.strSubGroupCode "
			+ "left outer join tblgrouphd i on h.strGroupCode=i.strGroupCode  "
			+ "where Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' "
			+ "and d.strBillNo not in(select strBillNo from tblbillsettlementdtl) "
			+ " and d.strBillNo not in(select strBillNo from tblqbillsettlementdtl) ");

		sbSqlForBilled33.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y'),a.strPOSCode,c.strCustomerName "
			+ ",ifnull(i.strGroupName,''),sum(e.dblQuantity),sum(e.dblAmount),'',ifnull(f.strAdvOrderTypeName,'') "
			+ ",ifnull(d.strOperationType,''),'','0','" + clsGlobalVarClass.gUserCode + "'"
			+ ",'" + clsGlobalVarClass.gClientCode + "',a.strManualAdvOrderNo "
			+ "from tblqadvbookbillhd a left outer join tblqbillhd d on a.strAdvBookingNo=d.strAdvBookingNo "
			+ "left outer join tblqadvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo "
			+ "left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode "
			+ "left outer join tblqadvbookbilldtl e on e.strAdvBookingNo=a.strAdvBookingNo "
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode "
			+ "left outer join tblitemmaster g on e.strItemCode=g.strItemCode "
			+ "left outer join tblsubgrouphd h on  g.strSubGroupCode=h.strSubGroupCode "
			+ "left outer join tblgrouphd i on h.strGroupCode=i.strGroupCode  "
			+ "where  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "' "
			+ "and d.strBillNo not in(select strBillNo from tblbillsettlementdtl) "
			+ " and d.strBillNo not in(select strBillNo from tblqbillsettlementdtl) "
			+ "and d.strSettelmentMode is not null ");

		if (!cmbTransType.getSelectedItem().toString().trim().equals("All"))
		{
		    //settled
		    sbSqlForBilled1.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		    sbSqlForBilled2.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		    sbSqlForBilled3.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		    //billed
		    sbSqlForBilled11.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		    sbSqlForBilled22.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		    sbSqlForBilled33.append(" and d.strOperationType='" + cmbTransType.getSelectedItem().toString().trim() + "' ");
		}

		//opened
		sbSqlForOpen.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y'),a.strPOSCode,c.strCustomerName "
			+ ",i.strGroupName,sum(e.dblQuantity),sum(e.dblAmount),'',ifnull(f.strAdvOrderTypeName,'') "
			+ ",ifnull(d.strOperationType,''),'','0','" + clsGlobalVarClass.gUserCode + "'"
			+ ",'" + clsGlobalVarClass.gClientCode + "',a.strManualAdvOrderNo "
			+ "from tbladvbookbillhd a left outer join tblbillhd d on a.strAdvBookingNo=d.strAdvBookingNo "
			+ "left outer join tbladvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo  "
			+ "left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode  "
			+ "left outer join tbladvbookbilldtl e on e.strAdvBookingNo=a.strAdvBookingNo "
			+ "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode "
			+ "left outer join tblitemmaster g on e.strItemCode=g.strItemCode "
			+ "left outer join tblsubgrouphd h on  g.strSubGroupCode=h.strSubGroupCode "
			+ "left outer join tblgrouphd i on h.strGroupCode=i.strGroupCode "
			+ "where  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "'  "
			+ "and d.strBillNo is null ");

		if (clsGlobalVarClass.gHOPOSType.equalsIgnoreCase("HOPOS"))
		{
		    sbSqlForOpen.setLength(0);
		    sbSqlForOpen.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y'),DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y'),a.strPOSCode"
			    + ",c.strCustomerName ,i.strGroupName,sum(e.dblQuantity),sum(e.dblAmount),''"
			    + ",ifnull(f.strAdvOrderTypeName,'') ,ifnull(d.strOperationType,''),'','0'"
			    + ",'" + clsGlobalVarClass.gUserCode + "','" + clsGlobalVarClass.gClientCode + "'"
			    + ",a.strManualAdvOrderNo "
			    + "from tblqadvbookbillhd a left outer join tblbillhd d on a.strAdvBookingNo=d.strAdvBookingNo "
			    + "left outer join tblqadvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo "
			    + "left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode  "
			    + "left outer join tblqadvbookbilldtl e on e.strAdvBookingNo=a.strAdvBookingNo "
			    + "left outer join tbladvanceordertypemaster f on a.strOrderType=f.strAdvOrderTypeCode "
			    + "left outer join tblitemmaster g on e.strItemCode=g.strItemCode "
			    + "left outer join tblsubgrouphd h on  g.strSubGroupCode=h.strSubGroupCode "
			    + "left outer join tblgrouphd i on h.strGroupCode=i.strGroupCode "
			    + "where  Date(" + dateFilter + ") between '" + fromDate + "' and '" + toDate + "'  "
			    + "and d.strBillNo is null ");
		}
	    }

	    if (!"All".equals(pos))
	    {
		if (sbSqlForOpen.length() > 0)
		{
		    //opened
		    sbSqlForOpen.append(" and a.strPOSCode='" + pos + "' ");
		}
		if (sbSqlForBilled1.length() > 0)
		{
		    //settled
		    sbSqlForBilled1.append(" and a.strPOSCode='" + pos + "' ");
		}
		if (sbSqlForBilled2.length() > 0)
		{
		    //settled
		    sbSqlForBilled2.append(" and a.strPOSCode='" + pos + "' ");
		}
		if (sbSqlForBilled3.length() > 0)
		{
		    //settled
		    sbSqlForBilled3.append(" and a.strPOSCode='" + pos + "' ");
		}
		if (sbSqlForBilled11.length() > 0)
		{
		    //billed
		    sbSqlForBilled11.append(" and a.strPOSCode='" + pos + "' ");
		}
		if (sbSqlForBilled22.length() > 0)
		{
		    //billed
		    sbSqlForBilled22.append(" and a.strPOSCode='" + pos + "' ");
		}
		if (sbSqlForBilled33.length() > 0)
		{
		    //billed
		    sbSqlForBilled33.append(" and a.strPOSCode='" + pos + "' ");
		}
	    }
	    if (!cmbOrderMode.getSelectedItem().toString().trim().equals("All"))
	    {
		if (hmAdvOrderType.size() > 0)
		{
		    String orderMode = cmbOrderMode.getSelectedItem().toString().trim();
		    if (sbSqlForOpen.length() > 0)
		    {
			//opened
			sbSqlForOpen.append(" and f.strAdvOrderTypeCode='" + hmAdvOrderType.get(orderMode) + "' ");
		    }
		    if (sbSqlForBilled1.length() > 0)
		    {
			//settled
			sbSqlForBilled1.append(" and f.strAdvOrderTypeCode='" + hmAdvOrderType.get(orderMode) + "' ");
		    }
		    if (sbSqlForBilled2.length() > 0)
		    {
			//settled
			sbSqlForBilled2.append(" and f.strAdvOrderTypeCode='" + hmAdvOrderType.get(orderMode) + "' ");
		    }
		    if (sbSqlForBilled3.length() > 0)
		    {
			//settled
			sbSqlForBilled3.append(" and f.strAdvOrderTypeCode='" + hmAdvOrderType.get(orderMode) + "' ");
		    }
		    if (sbSqlForBilled11.length() > 0)
		    {
			//billed
			sbSqlForBilled11.append(" and f.strAdvOrderTypeCode='" + hmAdvOrderType.get(orderMode) + "' ");
		    }
		    if (sbSqlForBilled22.length() > 0)
		    {
			//billed
			sbSqlForBilled22.append(" and f.strAdvOrderTypeCode='" + hmAdvOrderType.get(orderMode) + "' ");
		    }
		    if (sbSqlForBilled33.length() > 0)
		    {
			//billed
			sbSqlForBilled33.append(" and f.strAdvOrderTypeCode='" + hmAdvOrderType.get(orderMode) + "' ");
		    }
		}
	    }

	    if (sbSqlForOpen.length() > 0)
	    {
		//opened
		sbSqlForOpen.append(" group by i.strGroupCode ");
	    }
	    if (sbSqlForBilled1.length() > 0)
	    {
		//settled
		sbSqlForBilled1.append(" group by i.strGroupCode ");
	    }
	    if (sbSqlForBilled2.length() > 0)
	    {
		//settled
		sbSqlForBilled2.append(" group by i.strGroupCode ");
	    }
	    if (sbSqlForBilled3.length() > 0)
	    {
		//settled
		sbSqlForBilled3.append(" group by i.strGroupCode ");
	    }

	    if (sbSqlForBilled11.length() > 0)
	    {
		//billled
		sbSqlForBilled11.append(" group by i.strGroupCode ");
	    }
	    if (sbSqlForBilled22.length() > 0)
	    {
		//billled
		sbSqlForBilled22.append(" group by i.strGroupCode ");
	    }
	    if (sbSqlForBilled33.length() > 0)
	    {
		//billled
		sbSqlForBilled33.append(" group by i.strGroupCode ");
	    }

	    boolean flgRecords = false;
	    String userCode = clsGlobalVarClass.gUserCode;
	    List<clsAdvBookBillHd> arrListBillWiseSales = new ArrayList<clsAdvBookBillHd>();
	    ResultSet rsAdvancedOrderForOpen = null;
	    ResultSet rsAdvancedOrderDataBilled1 = null;
	    ResultSet rsAdvancedOrderDataBilled2 = null;
	    ResultSet rsAdvancedOrderDataBilled3 = null;
	    ResultSet rsAdvancedOrderDataBilled11 = null;
	    ResultSet rsAdvancedOrderDataBilled22 = null;
	    ResultSet rsAdvancedOrderDataBilled33 = null;

	    if (sbSqlForOpen.length() > 0)
	    {
		rsAdvancedOrderForOpen = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlForOpen.toString());
		while (rsAdvancedOrderForOpen.next())
		{
		    flgRecords = true;

		    clsAdvBookBillHd objAdvBookColumns = new clsAdvBookBillHd();
		    objAdvBookColumns.setStrAdvBookingNo(rsAdvancedOrderForOpen.getString(1));
		    objAdvBookColumns.setDteAdvBookingDate(rsAdvancedOrderForOpen.getString(2));
		    objAdvBookColumns.setDteOrderFor(rsAdvancedOrderForOpen.getString(3));
		    objAdvBookColumns.setStrPOSCode(rsAdvancedOrderForOpen.getString(4));
		    objAdvBookColumns.setStrCustomerName(rsAdvancedOrderForOpen.getString(5));
		    objAdvBookColumns.setStrGroupName(rsAdvancedOrderForOpen.getString(6));
		    objAdvBookColumns.setDblQuantity(rsAdvancedOrderForOpen.getDouble(7));
		    objAdvBookColumns.setDblGrandTotal(rsAdvancedOrderForOpen.getDouble(8));
		    objAdvBookColumns.setStrAdvOrderTypeName(rsAdvancedOrderForOpen.getString(9));
		    objAdvBookColumns.setStrOperationType(rsAdvancedOrderForOpen.getString(10));
		    objAdvBookColumns.setStrManualAdvOrderNo(rsAdvancedOrderForOpen.getString(11));

		    hmAdvOrderData.put(rsAdvancedOrderForOpen.getString(1), objAdvBookColumns);
		}
		rsAdvancedOrderForOpen.close();
	    }

	    if (sbSqlForBilled1.length() > 0)
	    {
		rsAdvancedOrderDataBilled1 = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlForBilled1.toString());
		while (rsAdvancedOrderDataBilled1.next())
		{
		    flgRecords = true;

		    clsAdvBookBillHd objAdvBookColumns = new clsAdvBookBillHd();
		    objAdvBookColumns.setStrAdvBookingNo(rsAdvancedOrderDataBilled1.getString(1));
		    objAdvBookColumns.setDteAdvBookingDate(rsAdvancedOrderDataBilled1.getString(2));
		    objAdvBookColumns.setDteOrderFor(rsAdvancedOrderDataBilled1.getString(3));
		    objAdvBookColumns.setStrPOSCode(rsAdvancedOrderDataBilled1.getString(4));
		    objAdvBookColumns.setStrCustomerName(rsAdvancedOrderDataBilled1.getString(5));
		    objAdvBookColumns.setStrGroupName(rsAdvancedOrderDataBilled1.getString(6));
		    objAdvBookColumns.setDblQuantity(rsAdvancedOrderDataBilled1.getDouble(7));
		    objAdvBookColumns.setDblGrandTotal(rsAdvancedOrderDataBilled1.getDouble(8));
		    objAdvBookColumns.setStrAdvOrderTypeName(rsAdvancedOrderDataBilled1.getString(9));
		    objAdvBookColumns.setStrOperationType(rsAdvancedOrderDataBilled1.getString(10));
		    objAdvBookColumns.setStrManualAdvOrderNo(rsAdvancedOrderDataBilled1.getString(11));

		    hmAdvOrderData.put(rsAdvancedOrderDataBilled1.getString(1), objAdvBookColumns);
		}
		rsAdvancedOrderDataBilled1.close();
	    }
	    if (sbSqlForBilled2.length() > 0)
	    {
		rsAdvancedOrderDataBilled2 = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlForBilled2.toString());
		//settled
		while (rsAdvancedOrderDataBilled2.next())
		{

		    flgRecords = true;

		    clsAdvBookBillHd objAdvBookColumns = new clsAdvBookBillHd();
		    objAdvBookColumns.setStrAdvBookingNo(rsAdvancedOrderDataBilled2.getString(1));
		    objAdvBookColumns.setDteAdvBookingDate(rsAdvancedOrderDataBilled2.getString(2));
		    objAdvBookColumns.setDteOrderFor(rsAdvancedOrderDataBilled2.getString(3));
		    objAdvBookColumns.setStrPOSCode(rsAdvancedOrderDataBilled2.getString(4));
		    objAdvBookColumns.setStrCustomerName(rsAdvancedOrderDataBilled2.getString(5));
		    objAdvBookColumns.setStrGroupName(rsAdvancedOrderDataBilled2.getString(6));
		    objAdvBookColumns.setDblQuantity(rsAdvancedOrderDataBilled2.getDouble(7));
		    objAdvBookColumns.setDblGrandTotal(rsAdvancedOrderDataBilled2.getDouble(8));
		    objAdvBookColumns.setStrAdvOrderTypeName(rsAdvancedOrderDataBilled2.getString(9));
		    objAdvBookColumns.setStrOperationType(rsAdvancedOrderDataBilled2.getString(10));
		    objAdvBookColumns.setStrManualAdvOrderNo(rsAdvancedOrderDataBilled2.getString(11));

		    hmAdvOrderData.put(rsAdvancedOrderDataBilled2.getString(1), objAdvBookColumns);
		}
		rsAdvancedOrderDataBilled2.close();
	    }
	    if (sbSqlForBilled3.length() > 0)
	    {
		rsAdvancedOrderDataBilled3 = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlForBilled3.toString());
		//settled
		while (rsAdvancedOrderDataBilled3.next())
		{

		    flgRecords = true;

		    clsAdvBookBillHd objAdvBookColumns = new clsAdvBookBillHd();
		    objAdvBookColumns.setStrAdvBookingNo(rsAdvancedOrderDataBilled3.getString(1));
		    objAdvBookColumns.setDteAdvBookingDate(rsAdvancedOrderDataBilled3.getString(2));
		    objAdvBookColumns.setDteOrderFor(rsAdvancedOrderDataBilled3.getString(3));
		    objAdvBookColumns.setStrPOSCode(rsAdvancedOrderDataBilled3.getString(4));
		    objAdvBookColumns.setStrCustomerName(rsAdvancedOrderDataBilled3.getString(5));
		    objAdvBookColumns.setStrGroupName(rsAdvancedOrderDataBilled3.getString(6));
		    objAdvBookColumns.setDblQuantity(rsAdvancedOrderDataBilled3.getDouble(7));
		    objAdvBookColumns.setDblGrandTotal(rsAdvancedOrderDataBilled3.getDouble(8));
		    objAdvBookColumns.setStrAdvOrderTypeName(rsAdvancedOrderDataBilled3.getString(9));
		    objAdvBookColumns.setStrOperationType(rsAdvancedOrderDataBilled3.getString(10));
		    objAdvBookColumns.setStrManualAdvOrderNo(rsAdvancedOrderDataBilled3.getString(11));

		    hmAdvOrderData.put(rsAdvancedOrderDataBilled3.getString(1), objAdvBookColumns);
		}
		rsAdvancedOrderDataBilled3.close();
	    }

	    if (sbSqlForBilled11.length() > 0)
	    {
		rsAdvancedOrderDataBilled11 = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlForBilled11.toString());
		//billed
		while (rsAdvancedOrderDataBilled11.next())
		{

		    flgRecords = true;

		    clsAdvBookBillHd objAdvBookColumns = new clsAdvBookBillHd();
		    objAdvBookColumns.setStrAdvBookingNo(rsAdvancedOrderDataBilled11.getString(1));
		    objAdvBookColumns.setDteAdvBookingDate(rsAdvancedOrderDataBilled11.getString(2));
		    objAdvBookColumns.setDteOrderFor(rsAdvancedOrderDataBilled11.getString(3));
		    objAdvBookColumns.setStrPOSCode(rsAdvancedOrderDataBilled11.getString(4));
		    objAdvBookColumns.setStrCustomerName(rsAdvancedOrderDataBilled11.getString(5));
		    objAdvBookColumns.setStrGroupName(rsAdvancedOrderDataBilled11.getString(6));
		    objAdvBookColumns.setDblQuantity(rsAdvancedOrderDataBilled11.getDouble(7));
		    objAdvBookColumns.setDblGrandTotal(rsAdvancedOrderDataBilled11.getDouble(8));
		    objAdvBookColumns.setStrAdvOrderTypeName(rsAdvancedOrderDataBilled11.getString(9));
		    objAdvBookColumns.setStrOperationType(rsAdvancedOrderDataBilled11.getString(10));
		    objAdvBookColumns.setStrManualAdvOrderNo(rsAdvancedOrderDataBilled11.getString(11));

		    hmAdvOrderData.put(rsAdvancedOrderDataBilled11.getString(1), objAdvBookColumns);
		}
		rsAdvancedOrderDataBilled11.close();
	    }
	    if (sbSqlForBilled22.length() > 0)
	    {
		rsAdvancedOrderDataBilled22 = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlForBilled22.toString());
		//billed
		while (rsAdvancedOrderDataBilled22.next())
		{

		    flgRecords = true;

		    clsAdvBookBillHd objAdvBookColumns = new clsAdvBookBillHd();
		    objAdvBookColumns.setStrAdvBookingNo(rsAdvancedOrderDataBilled22.getString(1));
		    objAdvBookColumns.setDteAdvBookingDate(rsAdvancedOrderDataBilled22.getString(2));
		    objAdvBookColumns.setDteOrderFor(rsAdvancedOrderDataBilled22.getString(3));
		    objAdvBookColumns.setStrPOSCode(rsAdvancedOrderDataBilled22.getString(4));
		    objAdvBookColumns.setStrCustomerName(rsAdvancedOrderDataBilled22.getString(5));
		    objAdvBookColumns.setStrGroupName(rsAdvancedOrderDataBilled22.getString(6));
		    objAdvBookColumns.setDblQuantity(rsAdvancedOrderDataBilled22.getDouble(7));
		    objAdvBookColumns.setDblGrandTotal(rsAdvancedOrderDataBilled22.getDouble(8));
		    objAdvBookColumns.setStrAdvOrderTypeName(rsAdvancedOrderDataBilled22.getString(9));
		    objAdvBookColumns.setStrOperationType(rsAdvancedOrderDataBilled22.getString(10));
		    objAdvBookColumns.setStrManualAdvOrderNo(rsAdvancedOrderDataBilled22.getString(11));

		    hmAdvOrderData.put(rsAdvancedOrderDataBilled22.getString(1), objAdvBookColumns);
		}
		rsAdvancedOrderDataBilled22.close();
	    }

	    if (sbSqlForBilled33.length() > 0)
	    {
		rsAdvancedOrderDataBilled33 = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlForBilled33.toString());
		//settled
		while (rsAdvancedOrderDataBilled33.next())
		{
		    flgRecords = true;

		    clsAdvBookBillHd objAdvBookColumns = new clsAdvBookBillHd();
		    objAdvBookColumns.setStrAdvBookingNo(rsAdvancedOrderDataBilled33.getString(1));
		    objAdvBookColumns.setDteAdvBookingDate(rsAdvancedOrderDataBilled33.getString(2));
		    objAdvBookColumns.setDteOrderFor(rsAdvancedOrderDataBilled33.getString(3));
		    objAdvBookColumns.setStrPOSCode(rsAdvancedOrderDataBilled33.getString(4));
		    objAdvBookColumns.setStrCustomerName(rsAdvancedOrderDataBilled33.getString(5));
		    objAdvBookColumns.setStrGroupName(rsAdvancedOrderDataBilled33.getString(6));
		    objAdvBookColumns.setDblQuantity(rsAdvancedOrderDataBilled33.getDouble(7));
		    objAdvBookColumns.setDblGrandTotal(rsAdvancedOrderDataBilled33.getDouble(8));
		    objAdvBookColumns.setStrAdvOrderTypeName(rsAdvancedOrderDataBilled33.getString(9));
		    objAdvBookColumns.setStrOperationType(rsAdvancedOrderDataBilled33.getString(10));
		    objAdvBookColumns.setStrManualAdvOrderNo(rsAdvancedOrderDataBilled33.getString(11));

		    hmAdvOrderData.put(rsAdvancedOrderDataBilled33.getString(1), objAdvBookColumns);
		}
		rsAdvancedOrderDataBilled33.close();
	    }

	    Object[] records = new Object[14];
	    for (clsAdvBookBillHd objAdvBookBillHd : hmAdvOrderData.values())
	    {

		records[0] = objAdvBookBillHd.getStrAdvBookingNo();
		records[1] = objAdvBookBillHd.getDteOrderFor();
		records[2] = objAdvBookBillHd.getDteAdvBookingDate();
		records[3] = objAdvBookBillHd.getStrCustomerName();
		records[4] = objAdvBookBillHd.getStrAdvOrderTypeName();
		records[5] = objAdvBookBillHd.getStrOperationType();
		records[6] = objAdvBookBillHd.getStrGroupName();
		records[7] = objAdvBookBillHd.getDblQuantity();
		records[8] = gDecimalFormat.format(objAdvBookBillHd.getDblGrandTotal());
		records[9] = objAdvBookBillHd.getStrManualAdvOrderNo();
		sumGt = sumGt + objAdvBookBillHd.getDblGrandTotal();
		sumQty = sumQty + objAdvBookBillHd.getDblQuantity();

		dm.addRow(records);

	    }

	    tblAdvOrder.setModel(dm);
	    Object[] total
		    =
		    {
			"Total", sumQty, gDecimalFormat.format(sumGt)
		    };
	    dm2.addRow(total);
	    tblTotal.setModel(dm2);

	    DefaultTableCellRenderer rightRenderer1 = new DefaultTableCellRenderer();
	    rightRenderer1.setHorizontalAlignment(JLabel.RIGHT);
	    tblAdvOrder.getColumnModel().getColumn(7).setCellRenderer(rightRenderer1);
	    tblAdvOrder.getColumnModel().getColumn(8).setCellRenderer(rightRenderer1);
	    tblAdvOrder.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

	    tblAdvOrder.getColumnModel().getColumn(0).setPreferredWidth(80);// Adv Order No
	    tblAdvOrder.getColumnModel().getColumn(1).setPreferredWidth(80);// Order For
	    tblAdvOrder.getColumnModel().getColumn(2).setPreferredWidth(80);// Bill Date
	    tblAdvOrder.getColumnModel().getColumn(3).setPreferredWidth(120);// Cust Name
	    tblAdvOrder.getColumnModel().getColumn(4).setPreferredWidth(80);// Order Type
	    tblAdvOrder.getColumnModel().getColumn(5).setPreferredWidth(80);// Op Type
	    tblAdvOrder.getColumnModel().getColumn(6).setPreferredWidth(120);// Group Name
	    tblAdvOrder.getColumnModel().getColumn(7).setPreferredWidth(70);// Qty
	    tblAdvOrder.getColumnModel().getColumn(8).setPreferredWidth(70);// Amt
	    tblAdvOrder.getColumnModel().getColumn(9).setPreferredWidth(70);// Manual Adv Order No

	    tblTotal.getColumnModel().getColumn(1).setCellRenderer(rightRenderer1);
	    tblTotal.getColumnModel().getColumn(2).setCellRenderer(rightRenderer1);
	    tblTotal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    tblTotal.getColumnModel().getColumn(0).setPreferredWidth(570);
	    tblTotal.getColumnModel().getColumn(1).setPreferredWidth(70);
	    tblTotal.getColumnModel().getColumn(2).setPreferredWidth(70);
	    tblTotal.getColumnModel().getColumn(3).setPreferredWidth(70);
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

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
	    java.util.logging.Logger.getLogger(frmAdvanceOrderFlash.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	}
	catch (InstantiationException ex)
	{
	    java.util.logging.Logger.getLogger(frmAdvanceOrderFlash.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	}
	catch (IllegalAccessException ex)
	{
	    java.util.logging.Logger.getLogger(frmAdvanceOrderFlash.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	}
	catch (javax.swing.UnsupportedLookAndFeelException ex)
	{
	    java.util.logging.Logger.getLogger(frmAdvanceOrderFlash.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	}
    }

    /*
     * To set the Customer Name in Search by the Help and set the Customer Code
     * into variable
     */
    void funSetCustomerData(Object[] data)
    {
	txtCustomerName.setText(data[1].toString());
	custCode = data[0].toString();
    }

    /*
     * Reset the Fields
     */
    private void funResetField()
    {
	try
	{

	    lblDate.setText(objUtility.funGetDateInString());

	    dteFromDate.setDate(objUtility.funGetDateToSetCalenderDate());
	    dteToDate.setDate(objUtility.funGetDateToSetCalenderDate());
	    txtCustomerName.setText("");
	    custCode = null;
	    cmbPosCode.setSelectedItem("All");
	    cmbType.setSelectedItem("Item wise");

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    /**
     * this Function is used for Export Files.
     */
    private void funExportButtonPressed()
    {
	try
	{
	    File theDir = new File(clsPosConfigFile.exportReportPath);
	    File file = new File(clsPosConfigFile.exportReportPath + "/" + exportFormName + objUtility.funGetDateInString() + ".xls");
	    if (!theDir.exists())
	    {
		theDir.mkdir();
		funExportFile(tblAdvOrder, file);
	    }
	    else
	    {
		funExportFile(tblAdvOrder, file);
	    }
	}
	catch (Exception ex)
	{
	    ex.printStackTrace();
	}
    }

    /**
     * this Function is used for Export files .
     */
    void funExportFile(JTable table, File file)
    {
	try
	{
	    WritableWorkbook workbook1 = Workbook.createWorkbook(file);
	    WritableSheet sheet1 = workbook1.createSheet("First Sheet", 0);
	    TableModel model = table.getModel();
	    sheet1.addCell(new Label(3, 0, "DSS Java Pos Bill Wise Report"));
	    for (int i = 0; i < model.getColumnCount() + 1; i++)
	    {
		Label column = new Label(i, 1, model.getColumnName(i));
		sheet1.setColumnView(i, model.getColumnName(i).toString().length() + 10);
		sheet1.addCell(column);
	    }
	    int i = 0, j = 0;
	    int k = 0;

	    for (i = 3; i < model.getRowCount() + 3; i++)
	    {
		for (j = 0; j < model.getColumnCount(); j++)
		{
		    Label row = new Label(j, i + 1, model.getValueAt(k, j).toString());
		    sheet1.setColumnView(j, model.getValueAt(k, j).toString().length() + 10);
		    sheet1.addCell(row);
		}
		k++;
	    }
	    funAddLastOfExportReport(workbook1);
	    workbook1.write();
	    workbook1.close();
	    Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + clsPosConfigFile.exportReportPath + "/" + exportFormName + objUtility.funGetDateInString() + ".xls");
	}
	catch (FileNotFoundException ex)
	{
	    JOptionPane.showMessageDialog(this, "File Not Found Invalid File Path!!!");
	    ex.printStackTrace();
	}
	catch (Exception ex)
	{
	    ex.printStackTrace();
	}
    }

    /**
     * this Function is used for Add Last of Export Reports files
     *
     * @param workbook1
     */
    private void funAddLastOfExportReport(WritableWorkbook workbook1)
    {
	try
	{
	    int i = 0, j = 0, LastIndexReport = 0;
	    if (exportFormName.equals("Adv Order Item wise"))
	    {
		LastIndexReport = 5;
	    }
	    else if (exportFormName.equals("Adv Order Customer wise"))
	    {
		LastIndexReport = 5;
	    }
	    else if (exportFormName.equals("Adv Order Bill wise"))
	    {
		LastIndexReport = 7;
	    }
	    WritableSheet sheet2 = workbook1.getSheet(0);
	    int r = sheet2.getRows();

	    for (i = r; i < tblTotal.getRowCount() + r; i++)
	    {
		for (j = 0; j < tblTotal.getColumnCount(); j++)
		{
		    Label row = new Label(LastIndexReport + j, i + 1, tblTotal.getValueAt(0, j).toString());
		    sheet2.addCell(row);
		}
	    }
	    WritableSheet sheet3 = workbook1.getSheet(0);
	    r = sheet3.getRows();
	    Formatter fmt = new Formatter();
	    Calendar cal = Calendar.getInstance();
	    fmt.format("%tr", cal);
	    Label row = new Label(1, r + 1, " Created On : " + clsGlobalVarClass.gPOSDateToDisplay + " At : " + fmt + " By : " + clsGlobalVarClass.gUserCode + " ");
	    sheet2.addCell(row);
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funPrintAdvOrderReceipt(int rowNo)
    {
	try
	{
	    clsTextFormat1ForAdvReceipt objAdvReceiptPrinting = new clsTextFormat1ForAdvReceipt();

	    String advOrderNo = tblAdvOrder.getValueAt(rowNo, 0).toString();

	    String sql = "select b.strReceiptNo,a.strPOSCode,c.strCustomerName,a.dteOrderFor,ifnull(d.strWaiterNo,'') "
		    + " from tbladvbookbillhd a left outer join tbladvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo "
		    + " left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode "
		    + " left outer join tblwaitermaster d on a.strWaiterNo=d.strWaiterNo "
		    + " where a.strAdvBookingNo='" + advOrderNo + "'";
	    ResultSet rsAdvOrderDtl = clsGlobalVarClass.dbMysql.executeResultSet(sql);
	    if (rsAdvOrderDtl.next())
	    {
		objAdvReceiptPrinting.funGenerateAdvReceipt(advOrderNo, rsAdvOrderDtl.getString(1), rsAdvOrderDtl.getString(2), "", rsAdvOrderDtl.getString(3), rsAdvOrderDtl.getString(4), rsAdvOrderDtl.getString(5), "AdvanceOrderFlash");
	    }
	    else
	    {
		sql = "select b.strReceiptNo,a.strPOSCode,c.strCustomerName,a.dteOrderFor,ifnull(d.strWaiterNo,'') "
			+ " from tblqadvbookbillhd a left outer join tblqadvancereceipthd b on a.strAdvBookingNo=b.strAdvBookingNo "
			+ " left outer join tblcustomermaster c on a.strCustomerCode=c.strCustomerCode "
			+ " left outer join tblwaitermaster d on a.strWaiterNo=d.strWaiterNo "
			+ " where a.strAdvBookingNo='" + advOrderNo + "'";
		rsAdvOrderDtl = clsGlobalVarClass.dbMysql.executeResultSet(sql);
		if (rsAdvOrderDtl.next())
		{
		    objAdvReceiptPrinting.funGenerateAdvReceipt(advOrderNo, rsAdvOrderDtl.getString(1), rsAdvOrderDtl.getString(2), "", rsAdvOrderDtl.getString(3), rsAdvOrderDtl.getString(4), rsAdvOrderDtl.getString(5), "AdvanceOrderFlash");
		}
		rsAdvOrderDtl.close();
	    }
	    rsAdvOrderDtl.close();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funExecuteButtonPressed()
    {
	if (cmbDateFilter.getSelectedItem().toString().equalsIgnoreCase("Order Date"))
	{
	    dateFilter = "a.dteOrderFor";
	}
	else if (cmbDateFilter.getSelectedItem().toString().equalsIgnoreCase("Booking Date"))
	{
	    dateFilter = "a.dteAdvBookingDate";
	}

	if (cmbType.getSelectedItem().equals("Item wise"))
	{
	    exportFormName = "Adv Order Item wise";
	    funItemWise();
	}
	else if (cmbType.getSelectedItem().equals("Customer wise"))
	{
	    exportFormName = "Adv Order Customer wise";
	    funCustomerWise();
	}
	else if (cmbType.getSelectedItem().equals("Bill wise"))
	{
	    exportFormName = "Adv Order Bill wise";
	    funBillWise();
	}
	else if (cmbType.getSelectedItem().equals("Menu Head wise"))
	{
	    exportFormName = "Adv Order Menu Head wise";
	    funMenuHeadWise();
	}
	else if (cmbType.getSelectedItem().toString().equalsIgnoreCase("Group Wise"))
	{
	    exportFormName = "Adv Order Group wise";
	    funGroupWise();
	}
    }

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
        };  ;
        pnlMain = new javax.swing.JPanel();
        cmbPosCode = new javax.swing.JComboBox();
        pnltotal = new javax.swing.JScrollPane();
        tblTotal = new javax.swing.JTable();
        btnClose = new javax.swing.JButton();
        dteToDate = new com.toedter.calendar.JDateChooser();
        lblposcode = new javax.swing.JLabel();
        lblType = new javax.swing.JLabel();
        txtCustomerName = new javax.swing.JTextField();
        btnReset = new javax.swing.JButton();
        cmbType = new javax.swing.JComboBox();
        btnExecute = new javax.swing.JButton();
        lblFromDate = new javax.swing.JLabel();
        lblToDate = new javax.swing.JLabel();
        pnlDetails = new javax.swing.JScrollPane();
        tblAdvOrder = new javax.swing.JTable();
        cmbStatus = new javax.swing.JComboBox();
        dteFromDate = new com.toedter.calendar.JDateChooser();
        lblcustName = new javax.swing.JLabel();
        cmbTransType = new javax.swing.JComboBox();
        lblTransType = new javax.swing.JLabel();
        cmbOrderMode = new javax.swing.JComboBox();
        lblMode = new javax.swing.JLabel();
        btnExport = new javax.swing.JButton();
        cmbDateFilter = new javax.swing.JComboBox();

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
        lblProductName.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                lblProductNameMouseClicked(evt);
            }
        });
        pnlheader.add(lblProductName);

        lblModuleName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblModuleName.setForeground(new java.awt.Color(255, 255, 255));
        pnlheader.add(lblModuleName);

        lblfromName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblfromName.setForeground(new java.awt.Color(255, 255, 255));
        lblfromName.setText("-Advance Order Flash");
        lblfromName.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                lblfromNameMouseClicked(evt);
            }
        });
        pnlheader.add(lblfromName);
        pnlheader.add(filler4);
        pnlheader.add(filler5);

        lblPosName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblPosName.setForeground(new java.awt.Color(255, 255, 255));
        lblPosName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblPosName.setMaximumSize(new java.awt.Dimension(321, 30));
        lblPosName.setMinimumSize(new java.awt.Dimension(321, 30));
        lblPosName.setPreferredSize(new java.awt.Dimension(321, 30));
        lblPosName.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                lblPosNameMouseClicked(evt);
            }
        });
        pnlheader.add(lblPosName);

        lblUserCode.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblUserCode.setForeground(new java.awt.Color(255, 255, 255));
        lblUserCode.setMaximumSize(new java.awt.Dimension(90, 30));
        lblUserCode.setMinimumSize(new java.awt.Dimension(90, 30));
        lblUserCode.setPreferredSize(new java.awt.Dimension(90, 30));
        lblUserCode.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                lblUserCodeMouseClicked(evt);
            }
        });
        pnlheader.add(lblUserCode);
        pnlheader.add(filler6);

        lblDate.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblDate.setForeground(new java.awt.Color(255, 255, 255));
        lblDate.setMaximumSize(new java.awt.Dimension(192, 30));
        lblDate.setMinimumSize(new java.awt.Dimension(192, 30));
        lblDate.setPreferredSize(new java.awt.Dimension(192, 30));
        lblDate.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                lblDateMouseClicked(evt);
            }
        });
        pnlheader.add(lblDate);

        lblHOSign.setMaximumSize(new java.awt.Dimension(34, 30));
        lblHOSign.setMinimumSize(new java.awt.Dimension(34, 30));
        lblHOSign.setPreferredSize(new java.awt.Dimension(34, 30));
        lblHOSign.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                lblHOSignMouseClicked(evt);
            }
        });
        pnlheader.add(lblHOSign);

        getContentPane().add(pnlheader, java.awt.BorderLayout.PAGE_START);

        pnlBackground.setLayout(new java.awt.GridBagLayout());

        pnlMain.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, new java.awt.Color(204, 204, 204), new java.awt.Color(204, 204, 204), new java.awt.Color(153, 153, 153), new java.awt.Color(153, 153, 153)));
        pnlMain.setMinimumSize(new java.awt.Dimension(800, 570));
        pnlMain.setOpaque(false);

        cmbPosCode.setToolTipText("Select POS");

        tblTotal.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        tblTotal.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {
                {null, null, null, null}
            },
            new String []
            {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        )
        {
            boolean[] canEdit = new boolean []
            {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return canEdit [columnIndex];
            }
        });
        tblTotal.setRowHeight(25);
        pnltotal.setViewportView(tblTotal);

        btnClose.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnClose.setForeground(new java.awt.Color(255, 255, 255));
        btnClose.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnClose.setText("Close");
        btnClose.setToolTipText("Close  Window");
        btnClose.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnClose.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnCloseActionPerformed(evt);
            }
        });

        dteToDate.setBackground(new java.awt.Color(51, 102, 255));
        dteToDate.setToolTipText("Select To Date");
        dteToDate.setPreferredSize(new java.awt.Dimension(119, 35));

        lblposcode.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblposcode.setText("POS Name");

        lblType.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblType.setText("Type");

        txtCustomerName.setToolTipText("Enter Name");
        txtCustomerName.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                txtCustomerNameMouseClicked(evt);
            }
        });

        btnReset.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnReset.setForeground(new java.awt.Color(255, 255, 255));
        btnReset.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnReset.setText("Reset ");
        btnReset.setToolTipText("Reset Form");
        btnReset.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnReset.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnResetMouseClicked(evt);
            }
        });

        cmbType.setToolTipText("Select Type");
        cmbType.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                cmbTypeActionPerformed(evt);
            }
        });

        btnExecute.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnExecute.setForeground(new java.awt.Color(255, 255, 255));
        btnExecute.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnExecute.setText("Execute");
        btnExecute.setToolTipText("Execute Report");
        btnExecute.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExecute.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnExecuteActionPerformed(evt);
            }
        });

        lblFromDate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblFromDate.setText("From Date");

        lblToDate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblToDate.setText("To Date");

        tblAdvOrder.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String []
            {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        )
        {
            boolean[] canEdit = new boolean []
            {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return canEdit [columnIndex];
            }
        });
        tblAdvOrder.setRowHeight(25);
        tblAdvOrder.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                tblAdvOrderMouseClicked(evt);
            }
        });
        pnlDetails.setViewportView(tblAdvOrder);

        cmbStatus.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        cmbStatus.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "All", "Billed", "Open", "Settled" }));
        cmbStatus.setToolTipText("Select mode");

        dteFromDate.setBackground(new java.awt.Color(51, 102, 255));
        dteFromDate.setToolTipText("Select From Date");
        dteFromDate.setPreferredSize(new java.awt.Dimension(119, 35));

        lblcustName.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblcustName.setText("Cust Name");

        cmbTransType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "All", "Dine In", "Home Delivery", "Take Away" }));
        cmbTransType.setToolTipText("Select Transaction Type");

        lblTransType.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblTransType.setText("Trans Type");

        cmbOrderMode.setToolTipText("Select Order Type");

        lblMode.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblMode.setText("Order Type");

        btnExport.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnExport.setForeground(new java.awt.Color(255, 255, 255));
        btnExport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnExport.setText("Export");
        btnExport.setToolTipText("Export File");
        btnExport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExport.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnExportMouseClicked(evt);
            }
        });

        cmbDateFilter.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Order Date", "Booking Date" }));
        cmbDateFilter.setToolTipText("Select POS");

        javax.swing.GroupLayout pnlMainLayout = new javax.swing.GroupLayout(pnlMain);
        pnlMain.setLayout(pnlMainLayout);
        pnlMainLayout.setHorizontalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addComponent(btnExecute, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(btnExport, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40)
                .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addComponent(lblposcode)
                        .addGap(4, 4, 4)
                        .addComponent(cmbPosCode, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(76, 76, 76)
                        .addComponent(cmbDateFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(27, 27, 27)
                        .addComponent(lblFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(dteFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(lblToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(dteToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addComponent(lblcustName)
                        .addGap(4, 4, 4)
                        .addComponent(txtCustomerName, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(lblType, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addComponent(cmbType, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(lblTransType)
                        .addGap(10, 10, 10)
                        .addComponent(cmbTransType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(lblMode)
                        .addGap(10, 10, 10)
                        .addComponent(cmbOrderMode, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(cmbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(pnlDetails, javax.swing.GroupLayout.PREFERRED_SIZE, 800, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pnltotal, javax.swing.GroupLayout.PREFERRED_SIZE, 800, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        pnlMainLayout.setVerticalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblposcode, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbPosCode, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbDateFilter, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dteFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dteToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblcustName, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblType, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbType, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbTransType, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbOrderMode, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtCustomerName, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblTransType, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblMode, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(5, 5, 5)
                .addComponent(pnlDetails, javax.swing.GroupLayout.PREFERRED_SIZE, 380, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(pnltotal, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnExecute, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnExport, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        pnlBackground.add(pnlMain, new java.awt.GridBagConstraints());

        getContentPane().add(pnlBackground, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents
 /**
     * *
     * Close Window
     */
    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed

	funResetLookAndFeel();
	dispose();
	clsGlobalVarClass.hmActiveForms.remove("Advance Order Flash");
    }//GEN-LAST:event_btnCloseActionPerformed
    /**
     * This Function is used Search Customer Name
     *
     * @param evt
     */
    private void txtCustomerNameMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txtCustomerNameMouseClicked
	// TODO add your handling code here:
	objUtility.funCallForSearchForm("CustomerMaster");
	new frmSearchFormDialog(this, true).setVisible(true);
	if (clsGlobalVarClass.gSearchItemClicked)
	{
	    Object[] data = clsGlobalVarClass.gArrListSearchData.toArray();
	    funSetCustomerData(data);
	    clsGlobalVarClass.gSearchItemClicked = false;
	}
    }//GEN-LAST:event_txtCustomerNameMouseClicked
    /**
     * reset Fields
     *
     * @param evt
     */
    private void btnResetMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnResetMouseClicked
	// TODO add your handling code here:
	funResetField();
    }//GEN-LAST:event_btnResetMouseClicked

    private void cmbTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbTypeActionPerformed
	// TODO add your handling code here:
    }//GEN-LAST:event_cmbTypeActionPerformed

    //This Button is used for Export Files
    private void btnExportMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnExportMouseClicked
	// TODO add your handling code here:
	funExportButtonPressed();
    }//GEN-LAST:event_btnExportMouseClicked

    private void lblProductNameMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblProductNameMouseClicked
    {//GEN-HEADEREND:event_lblProductNameMouseClicked
	// TODO add your handling code here:
	objUtility.funMinimizeWindow();
    }//GEN-LAST:event_lblProductNameMouseClicked

    private void lblfromNameMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblfromNameMouseClicked
    {//GEN-HEADEREND:event_lblfromNameMouseClicked
	// TODO add your handling code here:
	objUtility.funMinimizeWindow();
    }//GEN-LAST:event_lblfromNameMouseClicked

    private void lblPosNameMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblPosNameMouseClicked
    {//GEN-HEADEREND:event_lblPosNameMouseClicked
	// TODO add your handling code here:
	objUtility.funMinimizeWindow();
    }//GEN-LAST:event_lblPosNameMouseClicked

    private void lblUserCodeMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblUserCodeMouseClicked
    {//GEN-HEADEREND:event_lblUserCodeMouseClicked
	// TODO add your handling code here:
	objUtility.funMinimizeWindow();
    }//GEN-LAST:event_lblUserCodeMouseClicked

    private void lblDateMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblDateMouseClicked
    {//GEN-HEADEREND:event_lblDateMouseClicked
	// TODO add your handling code here:
	objUtility.funMinimizeWindow();
    }//GEN-LAST:event_lblDateMouseClicked

    private void lblHOSignMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblHOSignMouseClicked
    {//GEN-HEADEREND:event_lblHOSignMouseClicked
	// TODO add your handling code here:
	objUtility.funMinimizeWindow();
    }//GEN-LAST:event_lblHOSignMouseClicked

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("Advance Order Flash");
    }//GEN-LAST:event_formWindowClosed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("Advance Order Flash");
    }//GEN-LAST:event_formWindowClosing

    private void tblAdvOrderMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblAdvOrderMouseClicked
	// TODO add your handling code here:
	if (evt.getClickCount() == 2)
	{
	    funPrintAdvOrderReceipt(tblAdvOrder.getSelectedRow());
	}
    }//GEN-LAST:event_tblAdvOrderMouseClicked

    private void btnExecuteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExecuteActionPerformed
	// TODO add your handling code here:
	funExecuteButtonPressed();
    }//GEN-LAST:event_btnExecuteActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnExecute;
    private javax.swing.JButton btnExport;
    private javax.swing.JButton btnReset;
    private javax.swing.JComboBox cmbDateFilter;
    private javax.swing.JComboBox cmbOrderMode;
    private javax.swing.JComboBox cmbPosCode;
    private javax.swing.JComboBox cmbStatus;
    private javax.swing.JComboBox cmbTransType;
    private javax.swing.JComboBox cmbType;
    private com.toedter.calendar.JDateChooser dteFromDate;
    private com.toedter.calendar.JDateChooser dteToDate;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblFromDate;
    private javax.swing.JLabel lblHOSign;
    private javax.swing.JLabel lblMode;
    private javax.swing.JLabel lblModuleName;
    private javax.swing.JLabel lblPosName;
    private javax.swing.JLabel lblProductName;
    private javax.swing.JLabel lblToDate;
    private javax.swing.JLabel lblTransType;
    private javax.swing.JLabel lblType;
    private javax.swing.JLabel lblUserCode;
    private javax.swing.JLabel lblcustName;
    private javax.swing.JLabel lblfromName;
    private javax.swing.JLabel lblposcode;
    private javax.swing.JPanel pnlBackground;
    private javax.swing.JScrollPane pnlDetails;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JPanel pnlheader;
    private javax.swing.JScrollPane pnltotal;
    private javax.swing.JTable tblAdvOrder;
    private javax.swing.JTable tblTotal;
    private javax.swing.JTextField txtCustomerName;
    // End of variables declaration//GEN-END:variables

}
