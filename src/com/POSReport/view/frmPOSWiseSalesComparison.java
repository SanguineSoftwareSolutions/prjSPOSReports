/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.POSReport.view;

import com.POSGlobal.controller.clsBillItemDtl;
import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsGroupSubGroupWiseSales;
import com.POSGlobal.controller.clsPosConfigFile;
import com.POSGlobal.controller.clsUtility;
import com.POSGlobal.view.frmOkPopUp;

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
import java.util.Iterator;
import java.util.LinkedHashMap;
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
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

public class frmPOSWiseSalesComparison extends javax.swing.JFrame
{

    String selectQuery;
    private String userCode;
    private String fromDate;
    private String toDate;
    DefaultTableModel dm, totalDm;
    Map hmPOS;
    private Object[] records;
    private Map<String, Map<String, clsBillItemDtl>> mapPOSItemDtl;
    private Map<String, List<Map<String, clsGroupSubGroupWiseSales>>> mapPOSDtlForGroupSubGroup;
    private Map<String, Map<String, clsBillItemDtl>> mapPOSMenuHeadDtl;
    private String exportFormName, ExportReportPath;
    private java.util.Vector vSalesReportExcelColLength;
    private String rDate, reportName;
    private clsUtility objUtility;
    private Map<String, String> mapDocCode;
    private Map<String, Double> mapPOSDocSales;

    public frmPOSWiseSalesComparison()
    {
	/**
	 * this Function is used for Component initialization
	 */
	initComponents();
	objUtility = new clsUtility();
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
	objUtility = new clsUtility();
	this.setLocationRelativeTo(null);
	userCode = clsGlobalVarClass.gUserCode;
	hmPOS = new HashMap<String, String>();
	fillCmbBox();
	reportName = "POSWiseSalesComarison";
	exportFormName = "POSWiseSalesComarison";
	rDate = clsGlobalVarClass.gPOSDateToDisplay;
	ExportReportPath = clsPosConfigFile.exportReportPath;
	vSalesReportExcelColLength = new java.util.Vector();
	vSalesReportExcelColLength.add("10#Left"); //ItemCode
	vSalesReportExcelColLength.add("30#Left"); //ItemName
	vSalesReportExcelColLength.add("18#Left"); //Sales Amt
	vSalesReportExcelColLength.add("10#Left");
	vSalesReportExcelColLength.add("10#Left");
	vSalesReportExcelColLength.add("10#Left");
	vSalesReportExcelColLength.add("18#Left");
	vSalesReportExcelColLength.add("10#Left");

	btnExecute.setVisible(true);
	jButton1.setVisible(true);
	btnClose.setVisible(true);

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

    /**
     * ]
     * this function is used POS Code ComboBoxs
     */
    public void fillCmbBox()
    {
	dteFromDate.setDate(objUtility.funGetDateToSetCalenderDate());
	dteToDate.setDate(objUtility.funGetDateToSetCalenderDate());
	funInvokeReport();

    }

    /**
     * this Function is used For POS Wise Sub Group Comparison
     */
    private void funFillDataGridForSubGroup()
    {
	try
	{
	    double saleAmt = 0;
	    mapPOSDtlForGroupSubGroup = new LinkedHashMap<>();
	    records = new Object[5];
	    StringBuilder sbSqlLive = new StringBuilder();
	    StringBuilder sbSqlQFile = new StringBuilder();
	    StringBuilder sbSqlFilters = new StringBuilder();
	    hmPOS.clear();
	    fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());

	    toDate = objUtility.funGetFromToDate(dteToDate.getDate());

	    dm = new DefaultTableModel()
	    {
		@Override
		public boolean isCellEditable(int row, int column)
		{
		    //all cells false
		    return false;
		}
	    };

	    totalDm = new DefaultTableModel()
	    {
		@Override
		public boolean isCellEditable(int row, int column)
		{
		    //all cells false
		    return false;
		}
	    };
	    dm.addColumn("Sub Group Code");
	    dm.addColumn("Sub Group Name");

	    totalDm.addColumn("Total");
	    totalDm.addColumn("");

	    int cntArrLen = 1, cntPOS = 0;
	    String sqlSettlement = "select strPOSCode,strPOSName from tblposmaster "
		    + "order by strPOSName";
	    ResultSet rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlSettlement);
	    while (rsSales.next())
	    {
		hmPOS.put(rsSales.getString(2), rsSales.getString(1));
		//dm.addColumn(rsSales.getString(1));
		dm.addColumn(rsSales.getString(2));
		totalDm.addColumn("");
		cntArrLen++;
		cntPOS++;
	    }
	    rsSales.close();

	    //dm.addColumn("POSName");
	    dm.addColumn("Total");
	    totalDm.addColumn("");

	    tblPOSWiseSales.setModel(dm);
	    tblTotal.setModel(totalDm);

	    if ((dteToDate.getDate().getTime() - dteFromDate.getDate().getTime()) < 0)
	    {
		new frmOkPopUp(null, "Invalid date", "Error", 1).setVisible(true);
	    }
	    else
	    {
		sbSqlLive.setLength(0);
		sbSqlQFile.setLength(0);
		sbSqlFilters.setLength(0);

		sbSqlLive.append(" SELECT c.strSubGroupCode, c.strSubGroupName, sum( b.dblQuantity )  , sum( b.dblAmount )-sum(b.dblDiscountAmt), f.strPosName,'SANGUINE',b.dblRate , "
			+ " sum(b.dblAmount),sum(b.dblDiscountAmt),a.strPOSCode "
			+ " from tblbillhd a,tblbilldtl b,tblsubgrouphd c,tblitemmaster d  ,tblposmaster f  "
			+ " where a.strBillNo=b.strBillNo and a.strPOSCode=f.strPOSCode  "
			+ " and b.strItemCode=d.strItemCode  and c.strSubGroupCode=d.strSubGroupCode  "
			+ " and date( a.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
			+ " group by c.strSubGroupCode, c.strSubGroupName, a.strPoscode   ");

		sbSqlQFile.append(" SELECT c.strSubGroupCode, c.strSubGroupName, sum( b.dblQuantity )  , "
			+ " sum( b.dblAmount )-sum(b.dblDiscountAmt), f.strPosName,'SANGUINE',b.dblRate ,sum(b.dblAmount),sum(b.dblDiscountAmt),a.strPOSCode "
			+ " from tblqbillhd a,tblqbilldtl b,tblsubgrouphd c,tblitemmaster d  ,tblposmaster f  "
			+ " where a.strBillNo=b.strBillNo and a.strPOSCode=f.strPOSCode  "
			+ " and b.strItemCode=d.strItemCode  and c.strSubGroupCode=d.strSubGroupCode "
			+ " and date( a.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
			+ " group by c.strSubGroupCode, c.strSubGroupName, a.strPoscode   ");

		String sqlModLive = " select c.strSubGroupCode,c.strSubGroupName,sum(b.dblQuantity),"
			+ " sum(b.dblAmount)-sum(b.dblDiscAmt),f.strPOSName,'SANGUINE','0' ,sum(b.dblAmount),sum(b.dblDiscAmt),a.strPOSCode  "
			+ " from tblbillmodifierdtl b,tblbillhd a,tblposmaster f,tblitemmaster d,tblsubgrouphd c "
			+ " where a.strBillNo=b.strBillNo and a.strPOSCode=f.strPosCode  and LEFT(b.strItemCode,7)=d.strItemCode  "
			+ " and d.strSubGroupCode=c.strSubGroupCode  and b.dblamount>0  "
			+ " and date( a.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "'   "
			+ " group by c.strSubGroupCode, c.strSubGroupName, a.strPoscode  ";

		String sqlModQfile = " select c.strSubGroupCode,c.strSubGroupName,sum(b.dblQuantity),sum(b.dblAmount)-sum(b.dblDiscAmt),"
			+ " f.strPOSName,'SANGUINE','0' ,sum(b.dblAmount),sum(b.dblDiscAmt),a.strPOSCode  "
			+ " from tblqbillmodifierdtl b,tblqbillhd a,tblposmaster f,tblitemmaster d,tblsubgrouphd c "
			+ " where a.strBillNo=b.strBillNo and a.strPOSCode=f.strPosCode  and LEFT(b.strItemCode,7)=d.strItemCode  "
			+ " and d.strSubGroupCode=c.strSubGroupCode  and b.dblamount>0  "
			+ " and date( a.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "'  "
			+ " group by c.strSubGroupCode, c.strSubGroupName, a.strPoscode   ";

		ResultSet rsGroupWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
		funGenerateGroupWiseSales(rsGroupWiseSales);
		rsGroupWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFile.toString());
		funGenerateGroupWiseSales(rsGroupWiseSales);
		rsGroupWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlModLive);
		funGenerateGroupWiseSales(rsGroupWiseSales);
		rsGroupWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlModQfile);
		funGenerateGroupWiseSales(rsGroupWiseSales);

		DecimalFormat decFormatFor2Decimal = new DecimalFormat("0.00");
		Iterator<Map.Entry<String, String>> docIterator = mapDocCode.entrySet().iterator();
		while (docIterator.hasNext())
		{
		    Map.Entry entry = docIterator.next();
		    String docCode = entry.getKey().toString();
		    String docName = entry.getValue().toString();

		    int colSize = tblPOSWiseSales.getColumnCount();
		    int lastCol = colSize - 1;
		    Object[] row = new Object[colSize];

		    int i = 2;
		    row[0] = docCode.toString();
		    row[1] = docName.toString();

		    double posWiseTotalSales = 0.00;
		    for (int col = i; col < colSize; col++)
		    {
			double sales = 0.00;
			if (mapPOSDocSales.containsKey(tblPOSWiseSales.getColumnName(col).toString() + "!" + docCode))
			{
			    sales = Double.parseDouble(mapPOSDocSales.get(tblPOSWiseSales.getColumnName(col).toString() + "!" + docCode).toString());

			    row[i] = String.valueOf(decFormatFor2Decimal.format(sales));

			    posWiseTotalSales += sales;
			    i++;
			}
			else
			{
			    row[i] = "0.0";

			    posWiseTotalSales += 0.00;
			    i++;
			}
		    }
		    row[lastCol] = String.valueOf(decFormatFor2Decimal.format(posWiseTotalSales));
		    dm.addRow(row);
		}

		tblPOSWiseSales.setRowHeight(25);
		tblTotal.setRowHeight(40);

		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(JLabel.RIGHT);

		int i = 2;
		for (int col = i; col < tblPOSWiseSales.getColumnCount(); col++)
		{
		    tblPOSWiseSales.getColumnModel().getColumn(col).setCellRenderer(rightRenderer);
		    tblTotal.getColumnModel().getColumn(col).setCellRenderer(rightRenderer);
		}

		tblPOSWiseSales.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tblPOSWiseSales.getColumnModel().getColumn(0).setPreferredWidth(100);
		tblPOSWiseSales.getColumnModel().getColumn(1).setPreferredWidth(200);

		tblTotal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tblTotal.getColumnModel().getColumn(0).setPreferredWidth(100);
		tblTotal.getColumnModel().getColumn(1).setPreferredWidth(200);
		for (int col = i; col < tblPOSWiseSales.getColumnCount(); col++)
		{
		    tblPOSWiseSales.getColumnModel().getColumn(col).setPreferredWidth(120);
		    tblTotal.getColumnModel().getColumn(col).setPreferredWidth(120);
		}

		Object totalRow[] = new Object[tblPOSWiseSales.getColumnCount()];

		totalRow[0] = "Totals";
		totalRow[1] = "";
		for (int col = i; col < tblPOSWiseSales.getColumnCount(); col++, i++)
		{
		    double colTotalSales = 0.0;
		    for (int r = 0; r < tblPOSWiseSales.getRowCount(); r++)
		    {
			//System.out.println("r,c->" + r + "," + col);
			colTotalSales += Double.parseDouble(tblPOSWiseSales.getValueAt(r, col).toString());
		    }
		    totalRow[i] = String.valueOf(decFormatFor2Decimal.format(colTotalSales));
		}
		totalDm.addRow(totalRow);

	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    /**
     * this Function is used For POS Wise Item Comparison
     */
    private void funFillDataGridForItem()
    {
	try
	{
	    double saleAmt = 0;

	    mapPOSItemDtl = new LinkedHashMap<>();
	    records = new Object[5];
	    StringBuilder sbSqlLive = new StringBuilder();
	    StringBuilder sbSqlQFile = new StringBuilder();
	    StringBuilder sbSqlFilters = new StringBuilder();
	    hmPOS.clear();
	    fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());

	    toDate = objUtility.funGetFromToDate(dteToDate.getDate());

	    dm = new DefaultTableModel()
	    {
		@Override
		public boolean isCellEditable(int row, int column)
		{
		    //all cells false
		    return false;
		}
	    };

	    totalDm = new DefaultTableModel()
	    {
		@Override
		public boolean isCellEditable(int row, int column)
		{
		    //all cells false
		    return false;
		}
	    };

	    dm.addColumn("ItemCode");
	    dm.addColumn("Item Name");

	    totalDm.addColumn("Total");
	    totalDm.addColumn("");
	    DecimalFormat decFormatFor2Decimal = new DecimalFormat("0.00");
	    int cntArrLen = 1, cntPOS = 0;
	    String sqlSettlement = "select strPOSCode,strPOSName from tblposmaster "
		    + "order by strPOSName";
	    ResultSet rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlSettlement);
	    while (rsSales.next())
	    {
		hmPOS.put(rsSales.getString(2), rsSales.getString(1));
		//dm.addColumn(rsSales.getString(1));
		dm.addColumn(rsSales.getString(2));
		totalDm.addColumn("");
		cntArrLen++;
		cntPOS++;
	    }
	    rsSales.close();

	    //dm.addColumn("POSName");
	    dm.addColumn("Total");
	    totalDm.addColumn("");

	    tblPOSWiseSales.setModel(dm);
	    tblTotal.setModel(totalDm);

	    if ((dteToDate.getDate().getTime() - dteFromDate.getDate().getTime()) < 0)
	    {
		new frmOkPopUp(null, "Invalid date", "Error", 1).setVisible(true);
	    }
	    else
	    {
		sbSqlLive.setLength(0);
		sbSqlQFile.setLength(0);
		sbSqlFilters.setLength(0);

		sbSqlLive.append("  select a.strItemCode,a.strItemName,c.strPOSName,sum(a.dblQuantity),sum(a.dblTaxAmount) "
			+ "  ,sum(a.dblAmount)-sum(a.dblDiscountAmt),'SANGUINE' ,sum(a.dblAmount), "
			+ " sum(a.dblDiscountAmt),DATE_FORMAT(date(b.dteBillDate),'%d-%m-%Y'),b.strPOSCode "
			+ " from tblbilldtl a,tblbillhd b,tblposmaster c "
			+ " where a.strBillNo=b.strBillNo and b.strPOSCode=c.strPosCode "
			+ " and date( b.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "'   "
			+ " group by a.strItemCode,c.strPOSName  order by b.dteBillDate   ");

		sbSqlQFile.append(" select a.strItemCode,a.strItemName,c.strPOSName,sum(a.dblQuantity),sum(a.dblTaxAmount) "
			+ " ,sum(a.dblAmount)-sum(a.dblDiscountAmt),'SANGUINE' ,sum(a.dblAmount), "
			+ " sum(a.dblDiscountAmt),DATE_FORMAT(date(b.dteBillDate),'%d-%m-%Y'),b.strPOSCode "
			+ " from tblqbilldtl a,tblqbillhd b,tblposmaster c "
			+ " where a.strBillNo=b.strBillNo and b.strPOSCode=c.strPosCode "
			+ " and date( b.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "'   "
			+ " group by a.strItemCode,c.strPOSName  order by b.dteBillDate   ");

		String sqlModLive = " select a.strItemCode,a.strModifierName,c.strPOSName,sum(a.dblQuantity),'0.0', "
			+ " sum(a.dblAmount)-sum(a.dblDiscAmt),'SANGUINE' ,sum(a.dblAmount), "
			+ " sum(a.dblDiscAmt),DATE_FORMAT(date(b.dteBillDate),'%d-%m-%Y'),b.strPOSCode  "
			+ " from tblbillmodifierdtl a,tblbillhd b,tblposmaster c  "
			+ " where a.strBillNo=b.strBillNo and b.strPOSCode=c.strPosCode  "
			+ " and a.dblamount>0  "
			+ " and date( b.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "'   "
			+ " group by a.strItemCode,c.strPOSName  order by b.dteBillDate  ";

		String sqlModQfile = " select a.strItemCode,a.strModifierName,c.strPOSName,sum(a.dblQuantity),'0', "
			+ " sum(a.dblAmount)-sum(a.dblDiscAmt),'SANGUINE' ,sum(a.dblAmount), "
			+ " sum(a.dblDiscAmt),DATE_FORMAT(date(b.dteBillDate),'%d-%m-%Y'),b.strPOSCode "
			+ " from tblqbillmodifierdtl a,tblqbillhd b,tblposmaster c,tblitemmaster d "
			+ " where a.strBillNo=b.strBillNo and b.strPOSCode=c.strPosCode "
			+ " and a.strItemCode=d.strItemCode and a.dblamount>0 "
			+ " and date( b.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "'   "
			+ " group by a.strItemCode,c.strPOSName  order by b.dteBillDate  ";

		ResultSet rsItemWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
		funGenerateItemWiseSales(rsItemWiseSales);
		rsItemWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFile.toString());
		funGenerateItemWiseSales(rsItemWiseSales);
		rsItemWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlModLive);
		funGenerateItemWiseSales(rsItemWiseSales);
		rsItemWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlModQfile);
		funGenerateItemWiseSales(rsItemWiseSales);

		Iterator<Map.Entry<String, String>> docIterator = mapDocCode.entrySet().iterator();
		while (docIterator.hasNext())
		{
		    Map.Entry entry = docIterator.next();
		    String docCode = entry.getKey().toString();
		    String docName = entry.getValue().toString();

		    int colSize = tblPOSWiseSales.getColumnCount();
		    int lastCol = colSize - 1;
		    Object[] row = new Object[colSize];

		    int i = 2;
		    row[0] = docCode.toString();
		    row[1] = docName.toString();

		    double posWiseTotalSales = 0.00;
		    for (int col = i; col < colSize; col++)
		    {
			double sales = 0.00;
			if (mapPOSDocSales.containsKey(tblPOSWiseSales.getColumnName(col).toString() + "!" + docCode))
			{
			    sales = Double.parseDouble(mapPOSDocSales.get(tblPOSWiseSales.getColumnName(col).toString() + "!" + docCode).toString());

			    row[i] = String.valueOf(decFormatFor2Decimal.format(sales));

			    posWiseTotalSales += sales;
			    i++;
			}
			else
			{
			    row[i] = "0.0";

			    posWiseTotalSales += 0.00;
			    i++;
			}
		    }
		    row[lastCol] = String.valueOf(decFormatFor2Decimal.format(posWiseTotalSales));
		    dm.addRow(row);
		}

		tblPOSWiseSales.setRowHeight(25);
		tblTotal.setRowHeight(40);

		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(JLabel.RIGHT);

		int i = 2;
		for (int col = i; col < tblPOSWiseSales.getColumnCount(); col++)
		{
		    tblPOSWiseSales.getColumnModel().getColumn(col).setCellRenderer(rightRenderer);
		    tblTotal.getColumnModel().getColumn(col).setCellRenderer(rightRenderer);
		}

		tblPOSWiseSales.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tblPOSWiseSales.getColumnModel().getColumn(0).setPreferredWidth(100);
		tblPOSWiseSales.getColumnModel().getColumn(1).setPreferredWidth(200);

		tblTotal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tblTotal.getColumnModel().getColumn(0).setPreferredWidth(100);
		tblTotal.getColumnModel().getColumn(1).setPreferredWidth(200);
		for (int col = i; col < tblPOSWiseSales.getColumnCount(); col++)
		{
		    tblPOSWiseSales.getColumnModel().getColumn(col).setPreferredWidth(120);
		    tblTotal.getColumnModel().getColumn(col).setPreferredWidth(120);
		}

		Object totalRow[] = new Object[tblPOSWiseSales.getColumnCount()];

		totalRow[0] = "Totals";
		totalRow[1] = "";
		for (int col = i; col < tblPOSWiseSales.getColumnCount(); col++, i++)
		{
		    double colTotalSales = 0.0;
		    for (int r = 0; r < tblPOSWiseSales.getRowCount(); r++)
		    {
			//System.out.println("r,c->" + r + "," + col);
			colTotalSales += Double.parseDouble(tblPOSWiseSales.getValueAt(r, col).toString());
		    }
		    totalRow[i] = String.valueOf(decFormatFor2Decimal.format(colTotalSales));
		}
		totalDm.addRow(totalRow);
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    /**
     * this Function is used For POS Wise Group Comparison
     */
    private void funFillDataGridForGroup()
    {
	try
	{
	    double saleAmt = 0;
	    mapPOSDtlForGroupSubGroup = new LinkedHashMap<>();
	    records = new Object[5];
	    StringBuilder sbSqlLive = new StringBuilder();
	    StringBuilder sbSqlQFile = new StringBuilder();
	    StringBuilder sbSqlFilters = new StringBuilder();
	    hmPOS.clear();
	    fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());

	    toDate = objUtility.funGetFromToDate(dteToDate.getDate());

	    dm = new DefaultTableModel()
	    {
		@Override
		public boolean isCellEditable(int row, int column)
		{
		    //all cells false
		    return false;
		}
	    };

	    totalDm = new DefaultTableModel()
	    {
		@Override
		public boolean isCellEditable(int row, int column)
		{
		    //all cells false
		    return false;
		}
	    };
	    dm.addColumn("Group Code");
	    dm.addColumn("Group Name");

	    totalDm.addColumn("Total");
	    totalDm.addColumn("");

	    int cntArrLen = 1, cntPOS = 0;
	    String sqlSettlement = "select strPOSCode,strPOSName from tblposmaster "
		    + "order by strPOSName";
	    ResultSet rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlSettlement);
	    while (rsSales.next())
	    {
		hmPOS.put(rsSales.getString(2), rsSales.getString(1));
		//dm.addColumn(rsSales.getString(1));
		dm.addColumn(rsSales.getString(2));
		totalDm.addColumn("");
		cntArrLen++;
		cntPOS++;
	    }
	    rsSales.close();

	    //dm.addColumn("POSName");
	    dm.addColumn("Total");
	    totalDm.addColumn("");

	    tblPOSWiseSales.setModel(dm);
	    tblTotal.setModel(totalDm);
	    DecimalFormat decFormatFor2Decimal = new DecimalFormat("0.00");
	    if ((dteToDate.getDate().getTime() - dteFromDate.getDate().getTime()) < 0)
	    {
		new frmOkPopUp(null, "Invalid date", "Error", 1).setVisible(true);
	    }
	    else
	    {
		sbSqlLive.setLength(0);
		sbSqlQFile.setLength(0);
		sbSqlFilters.setLength(0);

		sbSqlLive.append(" SELECT c.strGroupCode,c.strGroupName,sum( b.dblQuantity),"
			+ " sum( b.dblAmount)-sum(b.dblDiscountAmt) ,f.strPosName, 'SANGUINE',b.dblRate ,sum(b.dblAmount),sum(b.dblDiscountAmt),a.strPOSCode "
			+ " FROM tblbillhd a,tblbilldtl b,tblgrouphd c,tblsubgrouphd d,tblitemmaster e,tblposmaster f "
			+ " where a.strBillNo=b.strBillNo and a.strPOSCode=f.strPOSCode and b.strItemCode=e.strItemCode and c.strGroupCode=d.strGroupCode "
			+ " and d.strSubGroupCode=e.strSubGroupCode "
			+ " and date( a.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
			+ " GROUP BY c.strGroupCode, c.strGroupName, a.strPoscode  ");

		sbSqlQFile.append(" SELECT c.strGroupCode,c.strGroupName,sum( b.dblQuantity),"
			+ " sum( b.dblAmount)-sum(b.dblDiscountAmt) ,f.strPosName, 'SANGUINE',b.dblRate ,sum(b.dblAmount),sum(b.dblDiscountAmt),a.strPOSCode  "
			+ " FROM tblqbillhd a,tblqbilldtl b,tblgrouphd c,tblsubgrouphd d,tblitemmaster e,tblposmaster f "
			+ " where a.strBillNo=b.strBillNo and a.strPOSCode=f.strPOSCode and b.strItemCode=e.strItemCode and c.strGroupCode=d.strGroupCode "
			+ " and d.strSubGroupCode=e.strSubGroupCode  "
			+ " and date( a.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
			+ " GROUP BY c.strGroupCode, c.strGroupName, a.strPoscode   ");

		String sqlModLive = " select c.strGroupCode,c.strGroupName,sum(b.dblQuantity),sum(b.dblAmount)-sum(b.dblDiscAmt),f.strPOSName,'SANGUINE','0' ,"
			+ " sum(b.dblAmount),sum(b.dblDiscAmt),a.strPOSCode  "
			+ " from tblbillmodifierdtl b,tblbillhd a,tblposmaster f,tblitemmaster d,tblsubgrouphd e,tblgrouphd c  "
			+ " where a.strBillNo=b.strBillNo and a.strPOSCode=f.strPosCode  "
			+ " and LEFT(b.strItemCode,7)=d.strItemCode  and d.strSubGroupCode=e.strSubGroupCode "
			+ " and e.strGroupCode=c.strGroupCode  and b.dblamount>0  "
			+ " and date( a.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
			+ " GROUP BY c.strGroupCode, c.strGroupName, a.strPoscode  ";

		String sqlModQfile = " select c.strGroupCode,c.strGroupName,sum(b.dblQuantity),sum(b.dblAmount)-sum(b.dblDiscAmt),f.strPOSName,'SANGUINE','0' , "
			+ " sum(b.dblAmount),sum(b.dblDiscAmt),a.strPOSCode  "
			+ " from tblqbillmodifierdtl b,tblqbillhd a,tblposmaster f,tblitemmaster d,tblsubgrouphd e,tblgrouphd c  "
			+ " where a.strBillNo=b.strBillNo and a.strPOSCode=f.strPosCode  and LEFT(b.strItemCode,7)=d.strItemCode  "
			+ " and d.strSubGroupCode=e.strSubGroupCode and e.strGroupCode=c.strGroupCode  and b.dblamount>0  "
			+ " and date( a.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "'   "
			+ " GROUP BY c.strGroupCode, c.strGroupName, a.strPoscode  ";

		ResultSet rsGroupWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
		funGenerateGroupWiseSales(rsGroupWiseSales);
		rsGroupWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFile.toString());
		funGenerateGroupWiseSales(rsGroupWiseSales);
		rsGroupWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlModLive);
		funGenerateGroupWiseSales(rsGroupWiseSales);
		rsGroupWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlModQfile);
		funGenerateGroupWiseSales(rsGroupWiseSales);

		Iterator<Map.Entry<String, String>> docIterator = mapDocCode.entrySet().iterator();
		while (docIterator.hasNext())
		{
		    Map.Entry entry = docIterator.next();
		    String docCode = entry.getKey().toString();
		    String docName = entry.getValue().toString();

		    int colSize = tblPOSWiseSales.getColumnCount();
		    int lastCol = colSize - 1;
		    Object[] row = new Object[colSize];

		    int i = 2;
		    row[0] = docCode.toString();
		    row[1] = docName.toString();

		    double posWiseTotalSales = 0.00;
		    for (int col = i; col < colSize; col++)
		    {
			double sales = 0.00;
			if (mapPOSDocSales.containsKey(tblPOSWiseSales.getColumnName(col).toString() + "!" + docCode))
			{
			    sales = Double.parseDouble(mapPOSDocSales.get(tblPOSWiseSales.getColumnName(col).toString() + "!" + docCode).toString());

			    row[i] = String.valueOf(decFormatFor2Decimal.format(sales));

			    posWiseTotalSales += sales;
			    i++;
			}
			else
			{
			    row[i] = "0.0";

			    posWiseTotalSales += 0.00;
			    i++;
			}
		    }
		    row[lastCol] = String.valueOf(decFormatFor2Decimal.format(posWiseTotalSales));
		    dm.addRow(row);
		}

		tblPOSWiseSales.setRowHeight(25);
		tblTotal.setRowHeight(40);

		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(JLabel.RIGHT);

		int i = 2;
		for (int col = i; col < tblPOSWiseSales.getColumnCount(); col++)
		{
		    tblPOSWiseSales.getColumnModel().getColumn(col).setCellRenderer(rightRenderer);
		    tblTotal.getColumnModel().getColumn(col).setCellRenderer(rightRenderer);
		}

		tblPOSWiseSales.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tblPOSWiseSales.getColumnModel().getColumn(0).setPreferredWidth(100);
		tblPOSWiseSales.getColumnModel().getColumn(1).setPreferredWidth(200);

		tblTotal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tblTotal.getColumnModel().getColumn(0).setPreferredWidth(100);
		tblTotal.getColumnModel().getColumn(1).setPreferredWidth(200);
		for (int col = i; col < tblPOSWiseSales.getColumnCount(); col++)
		{
		    tblPOSWiseSales.getColumnModel().getColumn(col).setPreferredWidth(120);
		    tblTotal.getColumnModel().getColumn(col).setPreferredWidth(120);
		}

		Object totalRow[] = new Object[tblPOSWiseSales.getColumnCount()];

		totalRow[0] = "Totals";
		totalRow[1] = "";
		for (int col = i; col < tblPOSWiseSales.getColumnCount(); col++, i++)
		{
		    double colTotalSales = 0.0;
		    for (int r = 0; r < tblPOSWiseSales.getRowCount(); r++)
		    {
			//System.out.println("r,c->" + r + "," + col);
			colTotalSales += Double.parseDouble(tblPOSWiseSales.getValueAt(r, col).toString());
		    }
		    totalRow[i] = String.valueOf(decFormatFor2Decimal.format(colTotalSales));
		}
		totalDm.addRow(totalRow);

	    }
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

	    while (rsGroupWiseSales.next())
	    {
		String subGroupCode = rsGroupWiseSales.getString(1);
		String subGroupName = rsGroupWiseSales.getString(2);
		String posName = rsGroupWiseSales.getString(5);
		double salesAmt = rsGroupWiseSales.getDouble(8);
		String posCode = rsGroupWiseSales.getString(10);

		mapDocCode.put(subGroupCode, subGroupName);
		if (mapPOSDocSales.containsKey(posName + "!" + subGroupCode))
		{
		    double oldSalesAmt = mapPOSDocSales.get(posName + "!" + subGroupCode);
		    mapPOSDocSales.put(posName + "!" + subGroupCode, oldSalesAmt + salesAmt);
		}
		else
		{
		    mapPOSDocSales.put(posName + "!" + subGroupCode, salesAmt);
		}
	    }
	    rsGroupWiseSales.close();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funGenerateMenuHeadWiseSales(ResultSet rsMenuHeadWiseSales)
    {
	try
	{
	    while (rsMenuHeadWiseSales.next())
	    {
		String posCode = rsMenuHeadWiseSales.getString(10);//posCode
		String posName = rsMenuHeadWiseSales.getString(5);//posName
		String menuCode = rsMenuHeadWiseSales.getString(1);//menuCode
		String menuName = rsMenuHeadWiseSales.getString(2);//menuName                
		double qty = rsMenuHeadWiseSales.getDouble(3);//qty
		double salesAmt = rsMenuHeadWiseSales.getDouble(8);//salesAmt
		double subTotal = rsMenuHeadWiseSales.getDouble(4);//subTotal
		double discAmt = rsMenuHeadWiseSales.getDouble(9);//disc                 

		mapDocCode.put(menuCode, menuName);
		if (mapPOSDocSales.containsKey(posName + "!" + menuCode))
		{
		    double oldSalesAmt = mapPOSDocSales.get(posName + "!" + menuCode);
		    mapPOSDocSales.put(posName + "!" + menuCode, oldSalesAmt + salesAmt);
		}
		else
		{
		    mapPOSDocSales.put(posName + "!" + menuCode, salesAmt);
		}

	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funGenerateItemWiseSales(ResultSet rsItemWiseSales)
    {
	try
	{
	    while (rsItemWiseSales.next())
	    {
		String itemCode = rsItemWiseSales.getString(1);//itemCode
		String itemName = rsItemWiseSales.getString(2);//itemName
		String posName = rsItemWiseSales.getString(3);//posName
		double qty = rsItemWiseSales.getDouble(4);//qty
		double salesAmt = rsItemWiseSales.getDouble(8);//salesAmount
		double subTotal = rsItemWiseSales.getDouble(6);//sunTotal
		double discAmt = rsItemWiseSales.getDouble(9);//discount
		String date = rsItemWiseSales.getString(10);//date
		String posCode = rsItemWiseSales.getString(11);//posCode

		mapDocCode.put(itemCode, itemName);
		if (mapPOSDocSales.containsKey(posName + "!" + itemCode))
		{
		    double oldSalesAmt = mapPOSDocSales.get(posName + "!" + itemCode);
		    mapPOSDocSales.put(posName + "!" + itemCode, oldSalesAmt + salesAmt);
		}
		else
		{
		    mapPOSDocSales.put(posName + "!" + itemCode, salesAmt);
		}

		if (mapPOSItemDtl.containsKey(posCode))
		{
		    Map<String, clsBillItemDtl> mapItemDtl = mapPOSItemDtl.get(posCode);
		    if (mapItemDtl.containsKey(itemCode))
		    {
			clsBillItemDtl objItemDtl = mapItemDtl.get(itemCode);
			objItemDtl.setQuantity(objItemDtl.getQuantity() + qty);
			objItemDtl.setAmount(objItemDtl.getAmount() + salesAmt);
			objItemDtl.setSubTotal(objItemDtl.getSubTotal() + subTotal);
			objItemDtl.setDiscountAmount(objItemDtl.getDiscountAmount() + discAmt);
		    }
		    else
		    {
			clsBillItemDtl objItemDtl = new clsBillItemDtl(date, itemCode, itemName, qty, salesAmt, discAmt, posName, subTotal);
			mapItemDtl.put(itemCode, objItemDtl);
		    }
		}
		else
		{
		    Map<String, clsBillItemDtl> mapItemDtl = new LinkedHashMap<>();
		    clsBillItemDtl objItemDtl = new clsBillItemDtl(date, itemCode, itemName, qty, salesAmt, discAmt, posName, subTotal);
		    mapItemDtl.put(itemCode, objItemDtl);
		    mapPOSItemDtl.put(posCode, mapItemDtl);
		}
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    /**
     * this Function is used For POS Wise Group Comparison
     */
    private void funFillDataGridForMenuHead()
    {
	try
	{
	    double saleAmt = 0;

	    mapPOSMenuHeadDtl = new LinkedHashMap<>();
	    records = new Object[5];
	    StringBuilder sbSqlLive = new StringBuilder();
	    StringBuilder sbSqlQFile = new StringBuilder();
	    StringBuilder sbSqlFilters = new StringBuilder();
	    hmPOS.clear();
	    fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());

	    toDate = objUtility.funGetFromToDate(dteToDate.getDate());
	    DecimalFormat decFormatFor2Decimal = new DecimalFormat("0.00");

	    dm = new DefaultTableModel()
	    {
		@Override
		public boolean isCellEditable(int row, int column)
		{
		    //all cells false
		    return false;
		}
	    };

	    totalDm = new DefaultTableModel()
	    {
		@Override
		public boolean isCellEditable(int row, int column)
		{
		    //all cells false
		    return false;
		}
	    };
	    dm.addColumn("Menu Head Code");
	    dm.addColumn("Menu Head Name");

	    totalDm.addColumn("Total");
	    totalDm.addColumn("");

	    int cntArrLen = 1, cntPOS = 0;
	    String sqlSettlement = "select strPOSCode,strPOSName from tblposmaster "
		    + "order by strPOSName";
	    ResultSet rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlSettlement);
	    while (rsSales.next())
	    {
		hmPOS.put(rsSales.getString(2), rsSales.getString(1));
		//dm.addColumn(rsSales.getString(1));
		dm.addColumn(rsSales.getString(2));
		totalDm.addColumn("");
		cntArrLen++;
		cntPOS++;
	    }
	    rsSales.close();

	    //dm.addColumn("POSName");
	    dm.addColumn("Total");
	    totalDm.addColumn("");

	    tblPOSWiseSales.setModel(dm);
	    tblTotal.setModel(totalDm);

	    if ((dteToDate.getDate().getTime() - dteFromDate.getDate().getTime()) < 0)
	    {
		new frmOkPopUp(null, "Invalid date", "Error", 1).setVisible(true);
	    }
	    else
	    {
		sbSqlLive.setLength(0);
		sbSqlQFile.setLength(0);
		sbSqlFilters.setLength(0);

		sbSqlLive.append(" SELECT ifnull(d.strMenuCode,'ND'),ifnull(e.strMenuName,'ND'), sum(a.dblQuantity), "
			+ " sum(a.dblAmount)-sum(a.dblDiscountAmt),f.strPosName,'SANGUINE',a.dblRate  ,sum(a.dblAmount),sum(a.dblDiscountAmt),b.strPOSCode   "
			+ " FROM tblbilldtl a "
			+ " left outer join tblbillhd b on a.strBillNo=b.strBillNo "
			+ " left outer join tblposmaster f on b.strposcode=f.strposcode  "
			+ " left outer join tblmenuitempricingdtl d on a.strItemCode = d.strItemCode  "
			+ " and b.strposcode =d.strposcode and b.strAreaCode= d.strAreaCode "
			+ " left outer join tblmenuhd e on d.strMenuCode= e.strMenuCode "
			+ " where date( b.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "'  "
			+ " Group by b.strPoscode, d.strMenuCode,e.strMenuName "
			+ " order by b.strPoscode, d.strMenuCode,e.strMenuName   ");

		sbSqlQFile.append(" SELECT  ifnull(d.strMenuCode,'ND'),ifnull(e.strMenuName,'ND'), sum(a.dblQuantity), "
			+ " sum(a.dblAmount)-sum(a.dblDiscountAmt),f.strPosName,'SANGUINE',a.dblRate ,sum(a.dblAmount),sum(a.dblDiscountAmt),b.strPOSCode  "
			+ " FROM tblqbilldtl a "
			+ " left outer join tblqbillhd b on a.strBillNo=b.strBillNo "
			+ " left outer join tblposmaster f on b.strposcode=f.strposcode "
			+ " left outer join tblmenuitempricingdtl d on a.strItemCode = d.strItemCode  "
			+ " and b.strposcode =d.strposcode and b.strAreaCode= d.strAreaCode "
			+ " left outer join tblmenuhd e on d.strMenuCode= e.strMenuCode "
			+ " where date( b.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "'  "
			+ " Group by b.strPoscode, d.strMenuCode,e.strMenuName "
			+ " order by b.strPoscode, d.strMenuCode,e.strMenuName   ");

		String sqlModLive = " SELECT  ifnull(d.strMenuCode,'ND'),ifnull(e.strMenuName,'ND'), sum(a.dblQuantity), "
			+ " sum(a.dblAmount)-sum(a.dblDiscAmt),f.strPosName,'SANGUINE',a.dblRate ,sum(a.dblAmount),sum(a.dblDiscAmt),b.strPOSCode "
			+ " FROM tblbillmodifierdtl a "
			+ " left outer join tblbillhd b on a.strBillNo=b.strBillNo "
			+ " left outer join tblposmaster f on b.strposcode=f.strposcode "
			+ " left outer join tblmenuitempricingdtl d on LEFT(a.strItemCode,7)= d.strItemCode  "
			+ " and b.strposcode =d.strposcode and b.strAreaCode= d.strAreaCode "
			+ " left outer join tblmenuhd e on d.strMenuCode= e.strMenuCode "
			+ " where date( b.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
			+ " and a.dblAmount>0   "
			+ " Group by b.strPoscode, d.strMenuCode,e.strMenuName "
			+ " order by b.strPoscode, d.strMenuCode,e.strMenuName  ";

		String sqlModQfile = " SELECT  ifnull(d.strMenuCode,'ND'),ifnull(e.strMenuName,'ND'), sum(a.dblQuantity), "
			+ " sum(a.dblAmount)-sum(a.dblDiscAmt),f.strPosName,'SANGUINE',a.dblRate ,sum(a.dblAmount),sum(a.dblDiscAmt),b.strPOSCode  "
			+ " FROM tblqbillmodifierdtl a "
			+ " left outer join tblqbillhd b on a.strBillNo=b.strBillNo "
			+ " left outer join tblposmaster f on b.strposcode=f.strposcode "
			+ " left outer join tblmenuitempricingdtl d on LEFT(a.strItemCode,7)= d.strItemCode "
			+ " and b.strposcode =d.strposcode and b.strAreaCode= d.strAreaCode "
			+ " left outer join tblmenuhd e on d.strMenuCode= e.strMenuCode"
			+ " where date( b.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
			+ " and a.dblAmount>0    "
			+ " Group by b.strPoscode, d.strMenuCode,e.strMenuName "
			+ " order by b.strPoscode, d.strMenuCode,e.strMenuName  ";

		ResultSet rsMenuHeadWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
		funGenerateMenuHeadWiseSales(rsMenuHeadWiseSales);
		rsMenuHeadWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFile.toString());
		funGenerateMenuHeadWiseSales(rsMenuHeadWiseSales);
		rsMenuHeadWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlModLive);
		funGenerateMenuHeadWiseSales(rsMenuHeadWiseSales);
		rsMenuHeadWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlModQfile);
		funGenerateMenuHeadWiseSales(rsMenuHeadWiseSales);

		Iterator<Map.Entry<String, String>> docIterator = mapDocCode.entrySet().iterator();
		while (docIterator.hasNext())
		{
		    Map.Entry entry = docIterator.next();
		    String docCode = entry.getKey().toString();
		    String docName = entry.getValue().toString();

		    int colSize = tblPOSWiseSales.getColumnCount();
		    int lastCol = colSize - 1;
		    Object[] row = new Object[colSize];

		    int i = 2;
		    row[0] = docCode.toString();
		    row[1] = docName.toString();

		    double posWiseTotalSales = 0.00;
		    for (int col = i; col < colSize; col++)
		    {
			double sales = 0.00;
			if (mapPOSDocSales.containsKey(tblPOSWiseSales.getColumnName(col).toString() + "!" + docCode))
			{
			    sales = Double.parseDouble(mapPOSDocSales.get(tblPOSWiseSales.getColumnName(col).toString() + "!" + docCode).toString());

			    row[i] = String.valueOf(decFormatFor2Decimal.format(sales));

			    posWiseTotalSales += sales;
			    i++;
			}
			else
			{
			    row[i] = "0.0";

			    posWiseTotalSales += 0.00;
			    i++;
			}
		    }
		    row[lastCol] = String.valueOf(decFormatFor2Decimal.format(posWiseTotalSales));
		    dm.addRow(row);
		}

		tblPOSWiseSales.setRowHeight(25);
		tblTotal.setRowHeight(40);

		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(JLabel.RIGHT);

		int i = 2;
		for (int col = i; col < tblPOSWiseSales.getColumnCount(); col++)
		{
		    tblPOSWiseSales.getColumnModel().getColumn(col).setCellRenderer(rightRenderer);
		    tblTotal.getColumnModel().getColumn(col).setCellRenderer(rightRenderer);
		}

		tblPOSWiseSales.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tblPOSWiseSales.getColumnModel().getColumn(0).setPreferredWidth(100);
		tblPOSWiseSales.getColumnModel().getColumn(1).setPreferredWidth(200);

		tblTotal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tblTotal.getColumnModel().getColumn(0).setPreferredWidth(100);
		tblTotal.getColumnModel().getColumn(1).setPreferredWidth(200);
		for (int col = i; col < tblPOSWiseSales.getColumnCount(); col++)
		{
		    tblPOSWiseSales.getColumnModel().getColumn(col).setPreferredWidth(120);
		    tblTotal.getColumnModel().getColumn(col).setPreferredWidth(120);
		}

		Object totalRow[] = new Object[tblPOSWiseSales.getColumnCount()];

		totalRow[0] = "Totals";
		totalRow[1] = "";
		for (int col = i; col < tblPOSWiseSales.getColumnCount(); col++, i++)
		{
		    double colTotalSales = 0.0;
		    for (int r = 0; r < tblPOSWiseSales.getRowCount(); r++)
		    {
			//System.out.println("r,c->" + r + "," + col);
			colTotalSales += Double.parseDouble(tblPOSWiseSales.getValueAt(r, col).toString());
		    }
		    totalRow[i] = String.valueOf(decFormatFor2Decimal.format(colTotalSales));
		}
		totalDm.addRow(totalRow);
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funInvokeReport()
    {

	mapDocCode = new HashMap<String, String>();
	mapPOSDocSales = new HashMap<String, Double>();

	//Item Wise, SubGroup Wise, Group Wise, Menu Head Wise
	if (cmbReportType.getSelectedItem().toString().trim().equals("Item Wise"))
	{
	    funFillDataGridForItem();
	}
	else if (cmbReportType.getSelectedItem().toString().trim().equals("Group Wise"))
	{
	    funFillDataGridForGroup();
	}
	else if (cmbReportType.getSelectedItem().toString().trim().equals("SubGroup Wise"))
	{
	    funFillDataGridForSubGroup();
	}
	else if (cmbReportType.getSelectedItem().toString().trim().equals("Menu Head Wise"))
	{
	    funFillDataGridForMenuHead();
	}
	else if (cmbReportType.getSelectedItem().toString().trim().equals("POS Wise"))
	{
	    funFillDataGridForPOSWiseSales();
	}
    }

    private void funExportFile(JTable table, File file)
    {
	try
	{
	    WritableWorkbook workbook1 = Workbook.createWorkbook(file);
	    WritableSheet sheet1 = workbook1.createSheet("First Sheet", 0);
	    TableModel model = table.getModel();
	    Date dt1 = dteFromDate.getDate();
	    Date dt2 = dteToDate.getDate();
	    SimpleDateFormat ddmmyyyyDateFormat = new SimpleDateFormat("dd-MM-yyyy");
	    String fromDateToDisplay = ddmmyyyyDateFormat.format(dt1);
	    String toDateToDisplay = ddmmyyyyDateFormat.format(dt2);

	    sheet1.addCell(new Label(0, 0, reportName));
	    sheet1.addCell(new Label(0, 1, "From Date:"));
	    sheet1.addCell(new Label(1, 1, fromDateToDisplay));
	    sheet1.addCell(new Label(2, 1, "To Date:"));
	    sheet1.addCell(new Label(3, 1, toDateToDisplay));

	    for (int i = 0; i < model.getColumnCount(); i++)
	    {
		Label column = new Label(i, 3, model.getColumnName(i));
		int colLen = Integer.parseInt(vSalesReportExcelColLength.elementAt(i).toString().split("#")[0]);
		sheet1.setColumnView(i, model.getColumnName(i).toString().length() + colLen);
		sheet1.addCell(column);
	    }
	    int i = 0, j = 0;
	    int k = 0;

	    for (i = 3; i < model.getRowCount() + 3; i++)
	    {
		for (j = 0; j < model.getColumnCount(); j++)
		{
		    //System.out.println(model.getValueAt(k, j).toString()+"\tcol="+j);
		    int colLen = Integer.parseInt(vSalesReportExcelColLength.elementAt(j).toString().split("#")[0]);
		    Label row = new Label(j, i + 1, model.getValueAt(k, j).toString());
		    sheet1.setColumnView(j, model.getValueAt(k, j).toString().length() + colLen);
		    sheet1.addCell(row);
		}
		k++;
	    }
	    funAddLastOfExportReport(workbook1);
	    workbook1.write();
	    workbook1.close();
	    Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + ExportReportPath + "/" + exportFormName + rDate + ".xls");
	    //sendMail();
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

    private void funAddLastOfExportReport(WritableWorkbook workbook1)
    {
	try
	{
	    int i = 0, j = 0, LastIndexReport = 1;
	    WritableSheet sheet2 = workbook1.getSheet(0);
	    int r = sheet2.getRows();

	    for (j = 0; j < tblTotal.getColumnCount(); j++)
	    {
		Label row = new Label(LastIndexReport + j + 1, r + 1, tblTotal.getColumnName(j).toString());
		sheet2.addCell(row);
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
        };  ;
        pnlMain = new javax.swing.JPanel();
        lblFromDate = new javax.swing.JLabel();
        btnClose = new javax.swing.JButton();
        dteFromDate = new com.toedter.calendar.JDateChooser();
        btnExecute = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        lblToDate = new javax.swing.JLabel();
        dteToDate = new com.toedter.calendar.JDateChooser();
        lblReportType = new javax.swing.JLabel();
        cmbReportType = new javax.swing.JComboBox();
        pnlDayEnd = new javax.swing.JScrollPane();
        tblPOSWiseSales = new javax.swing.JTable();
        pnltotal = new javax.swing.JScrollPane();
        tblTotal = new javax.swing.JTable();
        pnlGraph = new javax.swing.JPanel();
        lblGraph = new javax.swing.JLabel();
        chkGraph = new javax.swing.JCheckBox();

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

        lblformName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblformName.setForeground(new java.awt.Color(255, 255, 255));
        lblformName.setText("-POS Wise Sales Comparison");
        lblformName.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                lblformNameMouseClicked(evt);
            }
        });
        pnlheader.add(lblformName);
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
        pnlheader.add(filler6);

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
            public void mouseEntered(java.awt.event.MouseEvent evt)
            {
                lblHOSignMouseEntered(evt);
            }
        });
        pnlheader.add(lblHOSign);

        getContentPane().add(pnlheader, java.awt.BorderLayout.PAGE_START);

        pnlBackGround.setOpaque(false);
        pnlBackGround.setLayout(new java.awt.GridBagLayout());

        pnlMain.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, new java.awt.Color(204, 204, 204), new java.awt.Color(204, 204, 204), new java.awt.Color(153, 153, 153), new java.awt.Color(153, 153, 153)));
        pnlMain.setMinimumSize(new java.awt.Dimension(800, 570));
        pnlMain.setOpaque(false);

        lblFromDate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblFromDate.setText("From Date");

        btnClose.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnClose.setForeground(new java.awt.Color(255, 255, 255));
        btnClose.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnClose.setText("Close");
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

        dteFromDate.setToolTipText("Select From Date");
        dteFromDate.setPreferredSize(new java.awt.Dimension(119, 35));

        btnExecute.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnExecute.setForeground(new java.awt.Color(255, 255, 255));
        btnExecute.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnExecute.setText("Execute");
        btnExecute.setToolTipText("Execute");
        btnExecute.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExecute.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn2.png"))); // NOI18N
        btnExecute.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnExecuteMouseClicked(evt);
            }
        });

        jButton1.setBackground(new java.awt.Color(255, 255, 255));
        jButton1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        jButton1.setText("Export");
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn2.png"))); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton1ActionPerformed(evt);
            }
        });

        lblToDate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblToDate.setText("To Date");

        dteToDate.setToolTipText("Select To Date");
        dteToDate.setPreferredSize(new java.awt.Dimension(119, 35));

        lblReportType.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblReportType.setText("Report Type");

        cmbReportType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item Wise", "Group Wise", "SubGroup Wise", "Menu Head Wise", "POS Wise" }));
        cmbReportType.setToolTipText("Select POS");

        tblPOSWiseSales.setModel(new javax.swing.table.DefaultTableModel(
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
        ));
        tblPOSWiseSales.setRowHeight(25);
        pnlDayEnd.setViewportView(tblPOSWiseSales);

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
        ));
        tblTotal.setRowHeight(30);
        pnltotal.setViewportView(tblTotal);

        lblGraph.setText("jLabel1");

        javax.swing.GroupLayout pnlGraphLayout = new javax.swing.GroupLayout(pnlGraph);
        pnlGraph.setLayout(pnlGraphLayout);
        pnlGraphLayout.setHorizontalGroup(
            pnlGraphLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 876, Short.MAX_VALUE)
            .addGroup(pnlGraphLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnlGraphLayout.createSequentialGroup()
                    .addComponent(lblGraph, javax.swing.GroupLayout.DEFAULT_SIZE, 866, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        pnlGraphLayout.setVerticalGroup(
            pnlGraphLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 523, Short.MAX_VALUE)
            .addGroup(pnlGraphLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlGraphLayout.createSequentialGroup()
                    .addComponent(lblGraph, javax.swing.GroupLayout.DEFAULT_SIZE, 512, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        chkGraph.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        chkGraph.setText("Chart");
        chkGraph.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                chkGraphMouseClicked(evt);
            }
        });
        chkGraph.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                chkGraphActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlMainLayout = new javax.swing.GroupLayout(pnlMain);
        pnlMain.setLayout(pnlMainLayout);
        pnlMainLayout.setHorizontalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMainLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(pnlDayEnd)
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addComponent(lblReportType, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbReportType, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(chkGraph, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dteFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblToDate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(dteToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnExecute, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addComponent(pnltotal)))
            .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(pnlGraph, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlMainLayout.setVerticalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dteToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblReportType, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cmbReportType, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(chkGraph))
                    .addComponent(dteFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnExecute, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlDayEnd, javax.swing.GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnltotal, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMainLayout.createSequentialGroup()
                    .addGap(0, 52, Short.MAX_VALUE)
                    .addComponent(pnlGraph, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        pnlBackGround.add(pnlMain, new java.awt.GridBagConstraints());

        getContentPane().add(pnlBackGround, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents
/**
     * Execute Function for view Reports
     *
     * @param evt
     */
    private void btnExecuteMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnExecuteMouseClicked
	// TODO add your handling code here:
	funInvokeReport();

    }//GEN-LAST:event_btnExecuteMouseClicked

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
	// TODO add your handling code here:
	try
	{
	    File theDir = new File(ExportReportPath);
	    File file = new File(ExportReportPath + "/" + exportFormName + rDate + ".xls");
	    if (!theDir.exists())
	    {
		theDir.mkdir();
		funExportFile(tblPOSWiseSales, file);
		//sendMail();
	    }
	    else
	    {
		funExportFile(tblPOSWiseSales, file);
		//sendMail();
	    }
	}
	catch (Exception ex)
	{
	    ex.printStackTrace();
	}

    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * Close Windows
     *
     * @param evt
     */
    private void btnCloseMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCloseMouseClicked
	funResetLookAndFeel();
	dispose();
	clsGlobalVarClass.hmActiveForms.remove("POS Wise Sales");
    }//GEN-LAST:event_btnCloseMouseClicked

    private void chkGraphMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_chkGraphMouseClicked
	if (chkGraph.isSelected() == false)
	{
	    pnlDayEnd.setVisible(true);
	    pnltotal.setVisible(true);
	    pnlGraph.setVisible(false);
	}
    }//GEN-LAST:event_chkGraphMouseClicked

    private void chkGraphActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkGraphActionPerformed
	// TODO add your handling code here:
    }//GEN-LAST:event_chkGraphActionPerformed

    private void lblProductNameMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblProductNameMouseClicked
    {//GEN-HEADEREND:event_lblProductNameMouseClicked
	// TODO add your handling code here:
	objUtility = new clsUtility();
    }//GEN-LAST:event_lblProductNameMouseClicked

    private void lblformNameMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblformNameMouseClicked
    {//GEN-HEADEREND:event_lblformNameMouseClicked
	// TODO add your handling code here:
	objUtility = new clsUtility();
    }//GEN-LAST:event_lblformNameMouseClicked

    private void lblPosNameMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblPosNameMouseClicked
    {//GEN-HEADEREND:event_lblPosNameMouseClicked
	// TODO add your handling code here:
	objUtility = new clsUtility();
    }//GEN-LAST:event_lblPosNameMouseClicked

    private void lblUserCodeMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblUserCodeMouseClicked
    {//GEN-HEADEREND:event_lblUserCodeMouseClicked
	// TODO add your handling code here:
	objUtility = new clsUtility();
    }//GEN-LAST:event_lblUserCodeMouseClicked

    private void lblDateMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblDateMouseClicked
    {//GEN-HEADEREND:event_lblDateMouseClicked
	// TODO add your handling code here:
	objUtility = new clsUtility();
    }//GEN-LAST:event_lblDateMouseClicked

    private void lblHOSignMouseEntered(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblHOSignMouseEntered
    {//GEN-HEADEREND:event_lblHOSignMouseEntered
	// TODO add your handling code here:
	objUtility = new clsUtility();
    }//GEN-LAST:event_lblHOSignMouseEntered

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("POS Wise Sales");
    }//GEN-LAST:event_btnCloseActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("POS Wise Sales");
    }//GEN-LAST:event_formWindowClosed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("POS Wise Sales");
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
	    java.util.logging.Logger.getLogger(frmPOSWiseSalesComparison.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	}
	catch (InstantiationException ex)
	{
	    java.util.logging.Logger.getLogger(frmPOSWiseSalesComparison.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	}
	catch (IllegalAccessException ex)
	{
	    java.util.logging.Logger.getLogger(frmPOSWiseSalesComparison.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	}
	catch (javax.swing.UnsupportedLookAndFeelException ex)
	{
	    java.util.logging.Logger.getLogger(frmPOSWiseSalesComparison.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	}
	//</editor-fold>
	//</editor-fold>

	/* Create and display the form */
	java.awt.EventQueue.invokeLater(new Runnable()
	{
	    public void run()
	    {
		new frmPOSWiseSalesComparison().setVisible(true);
	    }
	});
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnExecute;
    private javax.swing.JCheckBox chkGraph;
    private javax.swing.JComboBox cmbReportType;
    private com.toedter.calendar.JDateChooser dteFromDate;
    private com.toedter.calendar.JDateChooser dteToDate;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblFromDate;
    private javax.swing.JLabel lblGraph;
    private javax.swing.JLabel lblHOSign;
    private javax.swing.JLabel lblModuleName;
    private javax.swing.JLabel lblPosName;
    private javax.swing.JLabel lblProductName;
    private javax.swing.JLabel lblReportType;
    private javax.swing.JLabel lblToDate;
    private javax.swing.JLabel lblUserCode;
    private javax.swing.JLabel lblformName;
    private javax.swing.JPanel pnlBackGround;
    private javax.swing.JScrollPane pnlDayEnd;
    private javax.swing.JPanel pnlGraph;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JPanel pnlheader;
    private javax.swing.JScrollPane pnltotal;
    private javax.swing.JTable tblPOSWiseSales;
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

    /*private void funGenerateGraph(ArrayList<String> arrListGraphData)
     {
     try
     {

     funCreateTempFolder();
     String filePath = System.getProperty("user.dir");
     File fileGraphPath = new File(filePath + "/Temp/Temp_Graph.jpeg");
     DefaultCategoryDataset dataset = null;
     dataset = new DefaultCategoryDataset();
     JFreeChart barChart = null;

     for (int i = 0; i < arrListGraphData.size(); i++)
     {
     String data = arrListGraphData.get(i);
     double amt = Double.parseDouble(data.split("#")[2]);
     String itemName = data.split("#")[1];
     String POSName = data.split("#")[0];
     if (amt > 0)
     {
     // dataset.addValue(amt, POSName, itemName);
     }
     barChart = ChartFactory.createBarChart("", "", "Amount", dataset, PlotOrientation.VERTICAL, true, true, false);
     }
     int width = 900; // Width of the image 

     int height = 480; // Height of the image 

     File BarChart = new File("Graph");
     ChartUtilities.saveChartAsJPEG(BarChart, barChart, width, height);
     }
     catch (Exception e)
     {
     e.printStackTrace();
     }
     }

     private void funCreateTempFolder()
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
     }*/
    private void funFillDataGridForPOSWiseSales()
    {
	try
	{
	    double saleAmt = 0;

	    records = new Object[2];
	    StringBuilder sbSqlLive = new StringBuilder();
	    StringBuilder sbSqlQFile = new StringBuilder();
	    StringBuilder sbSqlFilters = new StringBuilder();
	    hmPOS.clear();
	    fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());

	    toDate = objUtility.funGetFromToDate(dteToDate.getDate());
	    DecimalFormat decFormatFor2Decimal = new DecimalFormat("0.00");

	    dm = new DefaultTableModel()
	    {
		@Override
		public boolean isCellEditable(int row, int column)
		{
		    //all cells false
		    return false;
		}
	    };

	    totalDm = new DefaultTableModel()
	    {
		@Override
		public boolean isCellEditable(int row, int column)
		{
		    //all cells false
		    return false;
		}
	    };
	    dm.addColumn("POS");
	    dm.addColumn("Sales");

	    totalDm.addColumn("Totals");
	    totalDm.addColumn("");

	    tblPOSWiseSales.setModel(dm);
	    tblTotal.setModel(totalDm);

	    if ((dteToDate.getDate().getTime() - dteFromDate.getDate().getTime()) < 0)
	    {
		new frmOkPopUp(null, "Invalid date", "Error", 1).setVisible(true);
	    }
	    else
	    {
		sbSqlLive.setLength(0);
		sbSqlQFile.setLength(0);
		sbSqlFilters.setLength(0);

		sbSqlLive.append("select  b.strPosCode,b.strPosName,sum(a.dblGrandTotal) "
			+ "from tblbillhd a,tblposmaster b "
			+ "where a.strPOSCode=b.strPosCode "
			+ "and  date(a.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
			+ "group by b.strPosCode,b.strPosName "
			+ "order by b.strPosCode,b.strPosName;   ");

		sbSqlQFile.append("select  b.strPosCode,b.strPosName,sum(a.dblGrandTotal) "
			+ "from tblqbillhd a,tblposmaster b "
			+ "where a.strPOSCode=b.strPosCode "
			+ "and  date(a.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
			+ "group by b.strPosCode,b.strPosName "
			+ "order by b.strPosCode,b.strPosName;   ");

		ResultSet rsPOSWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
		funGeneratePOSWiseSales(rsPOSWiseSales);
		rsPOSWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFile.toString());
		funGeneratePOSWiseSales(rsPOSWiseSales);

		double totalSale = 0.00;
		Iterator<Map.Entry<String, Double>> itPOSSales = mapPOSDocSales.entrySet().iterator();
		while (itPOSSales.hasNext())
		{
		    Map.Entry<String, Double> entry = itPOSSales.next();
		    String posName = entry.getKey();
		    double sale = entry.getValue();

		    totalSale += sale;

		    Object[] row = new Object[2];
		    row[0] = posName;
		    row[1] = String.valueOf(decFormatFor2Decimal.format(sale));

		    dm.addRow(row);
		}

		Object[] row = new Object[2];
		row[0] = "Totals";
		row[1] = String.valueOf(totalSale);

		totalDm.addRow(row);

		tblPOSWiseSales.setRowHeight(25);
		tblTotal.setRowHeight(40);

		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(JLabel.RIGHT);

		tblPOSWiseSales.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
		tblTotal.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);

//                tblPOSWiseSales.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//                tblPOSWiseSales.getColumnModel().getColumn(0).setPreferredWidth(300);
//                tblPOSWiseSales.getColumnModel().getColumn(1).setPreferredWidth(200);
//
//                tblTotal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//                tblTotal.getColumnModel().getColumn(0).setPreferredWidth(300);
//                tblTotal.getColumnModel().getColumn(1).setPreferredWidth(200);
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funGeneratePOSWiseSales(ResultSet rsPOSWiseSales)
    {

	try
	{
	    while (rsPOSWiseSales.next())
	    {
		String posCode = rsPOSWiseSales.getString(1);//posCode
		String posName = rsPOSWiseSales.getString(2);//posName                                
		double salesAmt = rsPOSWiseSales.getDouble(3);//salesAmt

		mapDocCode.put(posCode, posName);
		if (mapPOSDocSales.containsKey(posName))
		{
		    double oldSalesAmt = mapPOSDocSales.get(posName);
		    mapPOSDocSales.put(posName, oldSalesAmt + salesAmt);
		}
		else
		{
		    mapPOSDocSales.put(posName, salesAmt);
		}

	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }
}
