package com.POSReport.view;

import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsPosConfigFile;
import com.POSGlobal.controller.clsUtility;
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
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
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
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class frmManagerSummaryFlash extends javax.swing.JFrame
{

    private StringBuilder sb = new StringBuilder();
    DefaultTableModel dmManagerSummaryFlash, totalDm;
    private java.util.Vector vSalesReportExcelColLength;
    private clsUtility objUtility;
    private DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();

    /**
     * this Function is used for Component initialization
     */
    public frmManagerSummaryFlash()
    {
	objUtility = new clsUtility();
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

	    funFillComboBox();

	    funManagerSummaryFlash();

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

    /**
     * this function is used POS Code ComboBoxs
     */
    private void funFillComboBox() throws Exception
    {
	if (clsGlobalVarClass.gShowOnlyLoginPOSReports)
	{
	    cmbPosCode.addItem(clsGlobalVarClass.gPOSName + " " + clsGlobalVarClass.gPOSCode);
	}
	else
	{
	    cmbPosCode.addItem("All");
	    sb.setLength(0);
	    sb.append("select strPosName,strPosCode from tblposmaster");
	    ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
	    while (rs.next())
	    {
		cmbPosCode.addItem(rs.getString(1) + " " + rs.getString(2));
	    }
	    rs.close();
	}

	dteFromDate.setDate(objUtility.funGetDateToSetCalenderDate());
	dteToDate.setDate(objUtility.funGetDateToSetCalenderDate());
    }

    /**
     * this Function is used Fill Table
     */
    private void funManagerSummaryFlash() throws Exception
    {
	String fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());
	String toDate = objUtility.funGetFromToDate(dteToDate.getDate());
	dmManagerSummaryFlash = new DefaultTableModel()
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
	String POSCode = objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString().trim());

	lblCashMagntBalAmt.setText(gDecimalFormat.format(0.00));
	String sqlTip = "", sqlNoOfBill = "", sqlDiscount = "";
	Map<Integer, String> mapTaxHeaders = new TreeMap<Integer, String>();
	int cntTax = 1;
	String sqlTax = "select c.strTaxCode "
		+ " from tblbillhd a,tblbilltaxdtl b,tbltaxhd c "
		+ " where a.strBillNo=b.strBillNo and b.strTaxCode=c.strTaxCode "
		+ " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "'"
		+ " and a.strClientCode=b.strClientCode ";
	if (!POSCode.equalsIgnoreCase("All"))
	{
	    sqlTax += " and a.strPOSCode='" + POSCode + "' ";
	}
	sqlTax += " group by c.strTaxCode";
	ResultSet rsTaxDtl1 = clsGlobalVarClass.dbMysql.executeResultSet(sqlTax);
	while (rsTaxDtl1.next())
	{
	    mapTaxHeaders.put(cntTax, rsTaxDtl1.getString(1));
	    cntTax++;
	}
	rsTaxDtl1.close();

	sqlTax = "select c.strTaxCode "
		+ " from tblqbillhd a,tblqbilltaxdtl b,tbltaxhd c "
		+ " where a.strBillNo=b.strBillNo and b.strTaxCode=c.strTaxCode "
		+ " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		+ " and a.strClientCode=b.strClientCode ";
	if (!POSCode.equalsIgnoreCase("All"))
	{
	    sqlTax += " and a.strPOSCode='" + POSCode + "' ";
	}
	sqlTax += " group by c.strTaxCode";
	rsTaxDtl1 = clsGlobalVarClass.dbMysql.executeResultSet(sqlTax);
	while (rsTaxDtl1.next())
	{
	    if (!mapTaxHeaders.containsValue(rsTaxDtl1.getString(1)))
	    {
		mapTaxHeaders.put(cntTax, rsTaxDtl1.getString(1));
		cntTax++;
	    }
	}
	rsTaxDtl1.close();

	Map<String, Map<String, Double>> hmSettlementWiseData = new HashMap<String, Map<String, Double>>();

	StringBuilder sbSqlLiveFile = new StringBuilder();
	StringBuilder sbSqlQFile = new StringBuilder();
	sbSqlLiveFile.setLength(0);
	sbSqlQFile.setLength(0);

	sbSqlLiveFile.append(" select c.strSettelmentCode,c.strSettelmentDesc,sum(b.dblSettlementAmt),c.strSettelmentType "
		+ " ,sum(a.dblSubTotal) "
		+ " from tblbillhd a,tblbillsettlementdtl b,tblsettelmenthd c "
		+ " where a.strBillNo=b.strBillNo and b.strSettlementCode=c.strSettelmentCode "
		+ " and a.strSettelmentMode!='MultiSettle' and a.strClientCode=b.strClientCode "
		+ " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		+ " and c.strSettelmentType!='Complementary' ");
	if (!POSCode.equalsIgnoreCase("All"))
	{
	    sbSqlLiveFile.append(" and a.strPOSCode='" + POSCode + "' ");
	}
	sbSqlLiveFile.append(" group by c.strSettelmentDesc ");
	System.out.println(sbSqlLiveFile);

	ResultSet rsSettleManager = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLiveFile.toString());
	while (rsSettleManager.next())
	{
	    //int cnt=1;
	    double taxAmt = 0, tipAmt = 0;
	    String settleKey = rsSettleManager.getString(2);
	    double settleAmt = rsSettleManager.getDouble(3);
	    double subTotal = rsSettleManager.getDouble(5);
	    Map<String, Double> hmSettlementWiseDtlData = new HashMap<String, Double>();

	    sqlTax = " select d.strSettlementCode,sum(b.dblTaxAmount),c.strTaxCode,c.strTaxDesc "
		    + " from tblbillhd a,tblbilltaxdtl b,tbltaxhd c,tblbillsettlementdtl d "
		    + " where a.strBillNo=b.strBillNo and a.strBillNo=d.strBillNo and a.strClientCode=b.strClientCode "
		    + " and a.strClientCode=d.strClientCode and b.strTaxCode=c.strTaxCode "
		    + " and a.strSettelmentMode!='MultiSettle' and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		    + " and d.strSettlementCode='" + rsSettleManager.getString(1) + "' ";
	    if (!POSCode.equalsIgnoreCase("All"))
	    {
		sqlTax += " and a.strPOSCode='" + POSCode + "' ";
	    }
	    sqlTax += " group by c.strTaxCode ";
	    ResultSet rsTaxManager = clsGlobalVarClass.dbMysql.executeResultSet(sqlTax);
	    while (rsTaxManager.next())
	    {
		taxAmt += rsTaxManager.getDouble(2);
		if (hmSettlementWiseData.containsKey(settleKey))
		{
		    hmSettlementWiseDtlData.put(rsTaxManager.getString(3), hmSettlementWiseDtlData.get(rsTaxManager.getString(3)) + rsTaxManager.getDouble(2));
		}
		else
		{
		    hmSettlementWiseDtlData.put(rsTaxManager.getString(3), rsTaxManager.getDouble(2));
		}
		//mapTaxHeaders.put(cnt,rsTaxManager.getString(3));
		//cnt++;
	    }
	    rsTaxManager.close();

	    boolean flgTip = false;
	    sqlTip = " select c.strSettelmentCode,c.strSettelmentDesc,sum(a.dblTipAmount),sum(a.dblRoundOff) "
		    + " from tblbillhd a,tblbillsettlementdtl b,tblsettelmenthd c "
		    + " where a.strBillNo=b.strBillNo and b.strSettlementCode=c.strSettelmentCode "
		    + " and a.strSettelmentMode!='MultiSettle' and a.strClientCode=b.strClientCode "
		    + " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		    + " and c.strSettelmentDesc='" + rsSettleManager.getString(2) + "' ";
	    if (!POSCode.equalsIgnoreCase("All"))
	    {
		sqlTip += " and a.strPOSCode='" + POSCode + "' ";
	    }
	    sqlTip += " group by c.strSettelmentDesc";
	    ResultSet rsTip = clsGlobalVarClass.dbMysql.executeResultSet(sqlTip);
	    while (rsTip.next())
	    {
		tipAmt = rsTip.getDouble(3);
		double roundOff = rsTip.getDouble(4);;
		if (hmSettlementWiseData.containsKey(settleKey))
		{
		    hmSettlementWiseDtlData.put("Tip Amt", hmSettlementWiseDtlData.get("Tip Amt") + tipAmt);
		}
		else
		{
		    hmSettlementWiseDtlData.put("Tip Amt", tipAmt);
		}

		if (hmSettlementWiseData.containsKey(settleKey))
		{
		    hmSettlementWiseDtlData.put("RoundOff", hmSettlementWiseDtlData.get("RoundOff") + roundOff);
		}
		else
		{
		    hmSettlementWiseDtlData.put("RoundOff", roundOff);
		}

		flgTip = true;
	    }
	    rsTip.close();
	    if (!flgTip)
	    {
		hmSettlementWiseDtlData.put("Tip Amt", 0.00);
	    }

	   
	    boolean flgDiscount = false;
	    sqlDiscount = "select c.strSettlementCode,sum(b.dblDiscAmt) "
		    + " from tblbillhd a,tblbilldiscdtl b,tblbillsettlementdtl c,tblsettelmenthd d "
		    + " where a.strBillNo=b.strBillNo and a.strBillNo=c.strBillNo and c.strSettlementCode=d.strSettelmentCode "
		    + " and a.strSettelmentMode!='MultiSettle' and a.strClientCode=b.strClientCode and a.strClientCode=c.strClientCode "
		    + " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		    + " and c.strSettlementCode='" + rsSettleManager.getString(1) + "' ";
	    if (!POSCode.equalsIgnoreCase("All"))
	    {
		sqlDiscount += " and a.strPOSCode='" + POSCode + "' ";
	    }
	    sqlDiscount += " group by d.strSettelmentDesc";
	    ResultSet rsDiscount = clsGlobalVarClass.dbMysql.executeResultSet(sqlDiscount);
	    while (rsDiscount.next())
	    {
		if (hmSettlementWiseData.containsKey(settleKey))
		{
		    hmSettlementWiseDtlData.put("Discount", hmSettlementWiseDtlData.get("Discount") + rsDiscount.getDouble(2));
		}
		else
		{
		    hmSettlementWiseDtlData.put("Discount", rsDiscount.getDouble(2));
		}
		flgDiscount = true;
	    }
	    rsDiscount.close();
	    if (!flgDiscount)
	    {
		hmSettlementWiseDtlData.put("Discount", 0.00);
	    }

	    //settleAmt-=taxAmt;
	    if (hmSettlementWiseData.containsKey(settleKey))
	    {
		hmSettlementWiseDtlData.put("SettleAmt", hmSettlementWiseDtlData.get("SettleAmt") + subTotal);
	    }
	    else
	    {
		hmSettlementWiseDtlData.put("SettleAmt", subTotal);
	    }

	    double totalHorizontalAmt = settleAmt;
	    hmSettlementWiseDtlData.put("TotalHorizontalAmt", totalHorizontalAmt);

	    hmSettlementWiseData.put(settleKey, hmSettlementWiseDtlData);
	}
	rsSettleManager.close();

	sbSqlQFile.append(" select c.strSettelmentCode,c.strSettelmentDesc,sum(b.dblSettlementAmt),c.strSettelmentType "
		+ " ,sum(a.dblSubTotal) "
		+ " from tblqbillhd a,tblqbillsettlementdtl b,tblsettelmenthd c "
		+ " where a.strBillNo=b.strBillNo and b.strSettlementCode=c.strSettelmentCode "
		+ " and a.strSettelmentMode!='MultiSettle' and a.strClientCode=b.strClientCode "
		+ " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		+ " and c.strSettelmentType!='Complementary' ");
	if (!POSCode.equalsIgnoreCase("All"))
	{
	    sbSqlQFile.append(" and a.strPOSCode='" + POSCode + "' ");
	}
	sbSqlQFile.append(" group by c.strSettelmentDesc ");
	rsSettleManager = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFile.toString());

	while (rsSettleManager.next())
	{
	    //int cnt=1;
	    double taxAmt = 0, tipAmt = 0, roundOff = 0;
	    String settleKey = rsSettleManager.getString(2);
	    double settleAmt = rsSettleManager.getDouble(3);
	    double subTotal = rsSettleManager.getDouble(5);
	    Map<String, Double> hmSettlementWiseDtlData = new HashMap<String, Double>();

	    if (hmSettlementWiseData.containsKey(settleKey))
	    {
		hmSettlementWiseDtlData = hmSettlementWiseData.get(settleKey);
	    }

	    sqlTax = " select d.strSettlementCode,sum(b.dblTaxAmount),c.strTaxCode,c.strTaxDesc "
		    + " from tblqbillhd a,tblqbilltaxdtl b,tbltaxhd c,tblqbillsettlementdtl d,tblsettelmenthd e "
		    + " where a.strBillNo=b.strBillNo and a.strBillNo=d.strBillNo and d.strSettlementCode=e.strSettelmentCode "
		    + " and b.strTaxCode=c.strTaxCode and a.strSettelmentMode!='MultiSettle' "
		    + " and a.strClientCode=b.strClientCode and a.strClientCode=d.strClientCode "
		    + " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		    + " and e.strSettelmentDesc='" + rsSettleManager.getString(2) + "' ";
	    if (!POSCode.equalsIgnoreCase("All"))
	    {
		sqlTax += " and a.strPOSCode='" + POSCode + "' ";
	    }
	    sqlTax += " group by c.strTaxCode ";
	    ResultSet rsTaxManager = clsGlobalVarClass.dbMysql.executeResultSet(sqlTax);
	    while (rsTaxManager.next())
	    {
		taxAmt += rsTaxManager.getDouble(2);
		if (hmSettlementWiseData.containsKey(settleKey))
		{
		    if (hmSettlementWiseDtlData.containsKey(rsTaxManager.getString(3)))
		    {
			hmSettlementWiseDtlData.put(rsTaxManager.getString(3), hmSettlementWiseDtlData.get(rsTaxManager.getString(3)) + rsTaxManager.getDouble(2));
		    }
		    else
		    {
			hmSettlementWiseDtlData.put(rsTaxManager.getString(3), rsTaxManager.getDouble(2));
		    }
		}
		else
		{
		    hmSettlementWiseDtlData.put(rsTaxManager.getString(3), rsTaxManager.getDouble(2));
		}
		//mapTaxHeaders.put(cnt,rsTaxManager.getString(3));
		//cnt++;
	    }
	    rsTaxManager.close();

	    boolean flgTip = false;
	    sqlTip = " select c.strSettelmentCode,c.strSettelmentDesc,sum(a.dblTipAmount),sum(a.dblRoundOff)  "
		    + " from tblqbillhd a,tblqbillsettlementdtl b,tblsettelmenthd c "
		    + " where a.strBillNo=b.strBillNo and b.strSettlementCode=c.strSettelmentCode "
		    + " and a.strSettelmentMode!='MultiSettle' and a.strClientCode=b.strClientCode "
		    + " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		    + " and c.strSettelmentDesc='" + rsSettleManager.getString(2) + "' ";
	    if (!POSCode.equalsIgnoreCase("All"))
	    {
		sqlTip += " and a.strPOSCode='" + POSCode + "' ";
	    }
	    sqlTip += " group by c.strSettelmentDesc";
	    ResultSet rsTip = clsGlobalVarClass.dbMysql.executeResultSet(sqlTip);
	    while (rsTip.next())
	    {
		tipAmt = rsTip.getDouble(3);
		roundOff = rsTip.getDouble(4);;
		if (hmSettlementWiseData.containsKey(settleKey))
		{
		    if (hmSettlementWiseDtlData.containsKey("Tip Amt"))
		    {
			hmSettlementWiseDtlData.put("Tip Amt", hmSettlementWiseDtlData.get("Tip Amt") + tipAmt);
		    }
		    else
		    {
			hmSettlementWiseDtlData.put("Tip Amt", tipAmt);
		    }
		}
		else
		{
		    hmSettlementWiseDtlData.put("Tip Amt", tipAmt);
		}

		if (hmSettlementWiseData.containsKey(settleKey))
		{
		    hmSettlementWiseDtlData.put("RoundOff", hmSettlementWiseDtlData.get("RoundOff") + roundOff);
		}
		else
		{
		    hmSettlementWiseDtlData.put("RoundOff", roundOff);
		}

		flgTip = true;
	    }
	    rsTip.close();
	    if (!flgTip)
	    {
		if (!hmSettlementWiseDtlData.containsKey("Tip Amt"))
		{
		    hmSettlementWiseDtlData.put("Tip Amt", 0.00);
		}
	    }

	    boolean flgDiscount = false;
	    sqlDiscount = "select c.strSettlementCode,sum(b.dblDiscAmt) "
		    + " from tblqbillhd a,tblqbilldiscdtl b,tblqbillsettlementdtl c,tblsettelmenthd d "
		    + " where a.strBillNo=b.strBillNo and a.strBillNo=c.strBillNo and c.strSettlementCode=d.strSettelmentCode "
		    + " and a.strSettelmentMode!='MultiSettle' and a.strClientCode=b.strClientCode and a.strClientCode=c.strClientCode "
		    + " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		    + " and d.strSettelmentDesc='" + rsSettleManager.getString(2) + "' ";
	    if (!POSCode.equalsIgnoreCase("All"))
	    {
		sqlDiscount += " and a.strPOSCode='" + POSCode + "' ";
	    }
	    sqlDiscount += " group by d.strSettelmentDesc";
	    ResultSet rsDiscount = clsGlobalVarClass.dbMysql.executeResultSet(sqlDiscount);
	    while (rsDiscount.next())
	    {
		if (hmSettlementWiseData.containsKey(settleKey))
		{
		    if (hmSettlementWiseDtlData.containsKey("Discount"))
		    {
			hmSettlementWiseDtlData.put("Discount", hmSettlementWiseDtlData.get("Discount") + rsDiscount.getDouble(2));
		    }
		    else
		    {
			hmSettlementWiseDtlData.put("Discount", rsDiscount.getDouble(2));
		    }
		}
		else
		{
		    hmSettlementWiseDtlData.put("Discount", rsDiscount.getDouble(2));
		}
		flgDiscount = true;
	    }
	    rsDiscount.close();
	    if (!flgDiscount)
	    {
		if (!hmSettlementWiseDtlData.containsKey("Discount"))
		{
		    hmSettlementWiseDtlData.put("Discount", 0.00);
		}
	    }

	    //settleAmt-=taxAmt;
	    if (hmSettlementWiseData.containsKey(settleKey))
	    {
		if (hmSettlementWiseDtlData.containsKey("SettleAmt"))
		{
		    hmSettlementWiseDtlData.put("SettleAmt", hmSettlementWiseDtlData.get("SettleAmt") + subTotal);
		}
		else
		{
		    hmSettlementWiseDtlData.put("SettleAmt", subTotal);
		}
	    }
	    else
	    {
		hmSettlementWiseDtlData.put("SettleAmt", subTotal);
	    }

	    double totalHorizontalAmt = settleAmt;
	    if (hmSettlementWiseData.containsKey(settleKey))
	    {
		if (hmSettlementWiseDtlData.containsKey("TotalHorizontalAmt"))
		{
		    hmSettlementWiseDtlData.put("TotalHorizontalAmt", hmSettlementWiseDtlData.get("TotalHorizontalAmt") + totalHorizontalAmt);
		}
		else
		{
		    hmSettlementWiseDtlData.put("TotalHorizontalAmt", totalHorizontalAmt);
		}
	    }
	    else
	    {
		hmSettlementWiseDtlData.put("TotalHorizontalAmt", totalHorizontalAmt);
	    }
	    hmSettlementWiseData.put(settleKey, hmSettlementWiseDtlData);
	}
	rsSettleManager.close();


	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	StringBuilder sbSql = new StringBuilder();
	Map<String, Double> hmSettlementPer = new HashMap<String, Double>();

	int billCount = 0;
	String sqlMultiSettledBills = "select a.strBillNo,a.dblGrandTotal,a.strClientCode "
		+ " from tblbillhd a "
		+ " where a.strSettelmentMode='MultiSettle' and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ";
	if (!POSCode.equalsIgnoreCase("All"))
	{
	    sqlMultiSettledBills += " and a.strPOSCode='" + POSCode + "' ";
	}
	sqlMultiSettledBills += " order by a.strBillNo; ";
	ResultSet rsMultiSettledBills = clsGlobalVarClass.dbMysql.executeResultSet(sqlMultiSettledBills);
	while (rsMultiSettledBills.next())
	{
	    billCount++;
	    Map<String, Double> hmSettlementWiseDtlData = new HashMap<String, Double>();

	    hmSettlementPer.clear();
	    String sqlBillDtl = "select c.strSettelmentDesc,b.dblSettlementAmt,a.dblGrandTotal,a.dblSubTotal"
		    + " ,b.strSettlementCode "
		    + " from tblbillhd a,tblbillsettlementdtl b,tblsettelmenthd c "
		    + " where a.strBillNo=b.strBillNo and b.strSettlementCode=c.strSettelmentCode "
		    + " and a.strBillNo='" + rsMultiSettledBills.getString(1) + "' and a.strClientCode=b.strClientCode "
		    + " and a.strClientCode='" + rsMultiSettledBills.getString(3) + "' ";
	    ResultSet rsSettleAmt = clsGlobalVarClass.dbMysql.executeResultSet(sqlBillDtl);
	    while (rsSettleAmt.next())
	    {
		double settlementAmt = rsSettleAmt.getDouble(2);
		double grandTotal = rsSettleAmt.getDouble(3);
		double subTotal = rsSettleAmt.getDouble(4);
		double totalPer = (settlementAmt / grandTotal) * 100;
		hmSettlementPer.put(rsSettleAmt.getString(1), totalPer);

		double settleTypwWiseSubTotal = subTotal * (totalPer / 100);
		if (hmSettlementWiseData.containsKey(rsSettleAmt.getString(1)))
		{
		    hmSettlementWiseDtlData = hmSettlementWiseData.get(rsSettleAmt.getString(1));
		    hmSettlementWiseDtlData.put("SettleAmt", hmSettlementWiseDtlData.get("SettleAmt") + settleTypwWiseSubTotal);
		}
		else
		{
		    hmSettlementWiseDtlData = new HashMap<String, Double>();
		    hmSettlementWiseDtlData.put("SettleAmt", settleTypwWiseSubTotal);
		}
	    }
	    rsSettleAmt.close();

	    for (Map.Entry<String, Double> entryPer : hmSettlementPer.entrySet())
	    {
		sbSql.setLength(0);
		sbSql.append("select b.strTaxCode,sum(b.dblTaxAmount),d.strSettelmentType "
			+ " from tblbillhd a,tblbilltaxdtl b,tblbillsettlementdtl c,tblsettelmenthd d "
			+ " where a.strBillNo=b.strBillNo and a.strBillNo=c.strBillNo "
			+ " and a.strClientCode=b.strClientCode and a.strClientCode=c.strClientCode "
			+ " and c.strSettlementCode=d.strSettelmentCode and a.strBillNo='" + rsMultiSettledBills.getString(1) + "' "
			+ " and d.strSettelmentDesc='" + entryPer.getKey() + "' "
			+ " and a.strClientCode='" + rsMultiSettledBills.getString(3) + "' ");
		if (!POSCode.equalsIgnoreCase("All"))
		{
		    sbSql.append(" and a.strPOSCode='" + POSCode + "' ");
		}
		sbSql.append(" group by b.strTaxCode");
		ResultSet rsTax = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
		while (rsTax.next())
		{
		    double taxAmt = rsTax.getDouble(2) * (entryPer.getValue() / 100);
		    if (hmSettlementWiseData.containsKey(entryPer.getKey()))
		    {
			hmSettlementWiseDtlData = hmSettlementWiseData.get(entryPer.getKey());

			if (hmSettlementWiseDtlData.containsKey(rsTax.getString(1)))
			{
			    hmSettlementWiseDtlData.put(rsTax.getString(1), hmSettlementWiseDtlData.get(rsTax.getString(1)) + taxAmt);
			}
			else
			{
			    hmSettlementWiseDtlData.put(rsTax.getString(1), taxAmt);
			}
		    }
		    else
		    {
			hmSettlementWiseDtlData.put(rsTax.getString(1), taxAmt);
		    }
		    //mapTaxHeaders.put(cnt,rsTax.getString(1));
		    //cnt++;
		}
		rsTax.close();

		boolean flgTip = false;
		sbSql.setLength(0);
		sbSql.append("select a.dblTipAmount from tblbillhd a "
			+ " where a.strBillNo='" + rsMultiSettledBills.getString(1) + "' "
			+ " and a.strClientCode='" + rsMultiSettledBills.getString(3) + "' ");
		if (!POSCode.equalsIgnoreCase("All"))
		{
		    sbSql.append(" and a.strPOSCode='" + POSCode + "' ");
		}
		ResultSet rsTip = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
		while (rsTip.next())
		{
		    double tipAmt = rsTip.getDouble(1) * (entryPer.getValue() / 100);
		    if (hmSettlementWiseData.containsKey(entryPer.getKey()))
		    {
			hmSettlementWiseDtlData = hmSettlementWiseData.get(entryPer.getKey());
			hmSettlementWiseDtlData.put("Tip Amt", hmSettlementWiseDtlData.get("Tip Amt") + tipAmt);
		    }
		    else
		    {
			hmSettlementWiseDtlData.put("Tip Amt", tipAmt);
		    }
		    flgTip = true;
		}
		rsTip.close();
		if (!flgTip)
		{
		    if (!hmSettlementWiseDtlData.containsKey("Tip Amt"))
		    {
			hmSettlementWiseDtlData.put("Tip Amt", 0.00);
		    }
		}

		boolean flgDiscount = false;
		sbSql.setLength(0);
		sbSql.append("select a.dblDiscountAmt from tblbillhd a "
			+ " where a.strBillNo='" + rsMultiSettledBills.getString(1) + "' "
			+ " and a.strClientCode='" + rsMultiSettledBills.getString(3) + "' ");
		if (!POSCode.equalsIgnoreCase("All"))
		{
		    sbSql.append(" and a.strPOSCode='" + POSCode + "' ");
		}
		ResultSet rsDiscount = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
		while (rsDiscount.next())
		{
		    double discAmt = rsDiscount.getDouble(1) * (entryPer.getValue() / 100);
		    if (hmSettlementWiseData.containsKey(entryPer.getKey()))
		    {
			hmSettlementWiseDtlData = hmSettlementWiseData.get(entryPer.getKey());
			hmSettlementWiseDtlData.put("Discount", hmSettlementWiseDtlData.get("Discount") + discAmt);
		    }
		    else
		    {
			hmSettlementWiseDtlData.put("Discount", discAmt);
		    }
		    flgDiscount = true;
		}
		rsDiscount.close();

		if (!flgDiscount)
		{
		    if (!hmSettlementWiseDtlData.containsKey("Discount"))
		    {
			hmSettlementWiseDtlData.put("Discount", 0.00);
		    }
		}
		//hmSettlementWiseDtlData.put("NoOfBills",1.00);

		hmSettlementWiseData.put(entryPer.getKey(), hmSettlementWiseDtlData);
	    }
	}
	rsMultiSettledBills.close();

	sqlMultiSettledBills = "select a.strBillNo,a.dblGrandTotal,a.strClientCode "
		+ " from tblqbillhd a "
		+ " where a.strSettelmentMode='MultiSettle' and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ";
	if (!POSCode.equalsIgnoreCase("All"))
	{
	    sqlMultiSettledBills += " and a.strPOSCode='" + POSCode + "' ";
	}
	sqlMultiSettledBills += " order by a.strBillNo; ";
	rsMultiSettledBills = clsGlobalVarClass.dbMysql.executeResultSet(sqlMultiSettledBills);
	while (rsMultiSettledBills.next())
	{
	    billCount++;
	    Map<String, Double> hmSettlementWiseDtlData = new HashMap<String, Double>();

	    hmSettlementPer.clear();
	    String sqlBillDtl = "select c.strSettelmentDesc,b.dblSettlementAmt,a.dblGrandTotal,a.dblSubTotal,b.strSettlementCode "
		    + " from tblqbillhd a,tblqbillsettlementdtl b,tblsettelmenthd c "
		    + " where a.strBillNo=b.strBillNo and b.strSettlementCode=c.strSettelmentCode "
		    + " and a.strClientCode=b.strClientCode and a.strBillNo='" + rsMultiSettledBills.getString(1) + "' "
		    + " and a.strClientCode='" + rsMultiSettledBills.getString(3) + "' ";
	    if (!POSCode.equalsIgnoreCase("All"))
	    {
		sqlBillDtl += " and a.strPOSCode='" + POSCode + "' ";
	    }
	    ResultSet rsSettleAmt = clsGlobalVarClass.dbMysql.executeResultSet(sqlBillDtl);
	    while (rsSettleAmt.next())
	    {
		double settlementAmt = rsSettleAmt.getDouble(2);
		double grandTotal = rsSettleAmt.getDouble(3);
		double subTotal = rsSettleAmt.getDouble(4);
		double totalPer = (settlementAmt / grandTotal) * 100;
		hmSettlementPer.put(rsSettleAmt.getString(1), totalPer);

		double settleTypwWiseSubTotal = subTotal * (totalPer / 100);
		if (hmSettlementWiseData.containsKey(rsSettleAmt.getString(1)))
		{
		    hmSettlementWiseDtlData = hmSettlementWiseData.get(rsSettleAmt.getString(1));
		    hmSettlementWiseDtlData.put("SettleAmt", hmSettlementWiseDtlData.get("SettleAmt") + settleTypwWiseSubTotal);
		}
		else
		{
		    hmSettlementWiseDtlData.put("SettleAmt", settleTypwWiseSubTotal);
		}
	    }
	    rsSettleAmt.close();

	    for (Map.Entry<String, Double> entryPer : hmSettlementPer.entrySet())
	    {
		sbSql.setLength(0);
		sbSql.append("select b.strTaxCode,sum(b.dblTaxAmount),d.strSettelmentType "
			+ " from tblqbillhd a,tblqbilltaxdtl b,tblqbillsettlementdtl c,tblsettelmenthd d "
			+ " where a.strBillNo=b.strBillNo and a.strBillNo=c.strBillNo "
			+ " and a.strClientCode=b.strClientCode and a.strClientCode=c.strClientCode "
			+ " and c.strSettlementCode=d.strSettelmentCode and a.strBillNo='" + rsMultiSettledBills.getString(1) + "' "
			+ " and d.strSettelmentDesc='" + entryPer.getKey() + "' and a.strClientCode='" + rsMultiSettledBills.getString(3) + "' ");
		if (!POSCode.equalsIgnoreCase("All"))
		{
		    sbSql.append(" and a.strPOSCode='" + POSCode + "' ");
		}
		sbSql.append(" group by b.strTaxCode");
		ResultSet rsTax = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
		while (rsTax.next())
		{
		    double taxAmt = rsTax.getDouble(2) * (entryPer.getValue() / 100);
		    if (hmSettlementWiseData.containsKey(entryPer.getKey()))
		    {
			hmSettlementWiseDtlData = hmSettlementWiseData.get(entryPer.getKey());
			if (hmSettlementWiseDtlData.containsKey(rsTax.getString(1)))
			{
			    hmSettlementWiseDtlData.put(rsTax.getString(1), hmSettlementWiseDtlData.get(rsTax.getString(1)) + taxAmt);
			}
			else
			{
			    hmSettlementWiseDtlData.put(rsTax.getString(1), taxAmt);
			}
		    }
		    else
		    {
			hmSettlementWiseDtlData.put(rsTax.getString(1), taxAmt);
		    }
		    //mapTaxHeaders.put(cnt,rsTax.getString(1));
		    //cnt++;
		}
		rsTax.close();

		boolean flgTip = false;
		sbSql.setLength(0);
		sbSql.append("select a.dblTipAmount from tblqbillhd a "
			+ " where a.strBillNo='" + rsMultiSettledBills.getString(1) + "' "
			+ " and a.strClientCode='" + rsMultiSettledBills.getString(3) + "' ");
		if (!POSCode.equalsIgnoreCase("All"))
		{
		    sbSql.append(" and a.strPOSCode='" + POSCode + "' ");
		}
		ResultSet rsTip = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
		while (rsTip.next())
		{
		    double tipAmt = rsTip.getDouble(1) * (entryPer.getValue() / 100);
		    if (hmSettlementWiseData.containsKey(entryPer.getKey()))
		    {
			hmSettlementWiseDtlData = hmSettlementWiseData.get(entryPer.getKey());
			hmSettlementWiseDtlData.put("Tip Amt", hmSettlementWiseDtlData.get("Tip Amt") + tipAmt);
		    }
		    else
		    {
			hmSettlementWiseDtlData.put("Tip Amt", tipAmt);
		    }
		    flgTip = true;
		}
		rsTip.close();
		if (!flgTip)
		{
		    if (!hmSettlementWiseDtlData.containsKey("Tip Amt"))
		    {
			hmSettlementWiseDtlData.put("Tip Amt", 0.00);
		    }
		}

		boolean flgDiscount = false;
		sbSql.setLength(0);
		sbSql.append("select a.dblDiscountAmt from tblqbillhd a "
			+ " where a.strBillNo='" + rsMultiSettledBills.getString(1) + "' "
			+ " and a.strClientCode='" + rsMultiSettledBills.getString(3) + "' ");
		if (!POSCode.equalsIgnoreCase("All"))
		{
		    sbSql.append(" and a.strPOSCode='" + POSCode + "' ");
		}
		ResultSet rsDiscount = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
		while (rsDiscount.next())
		{
		    double discAmt = rsDiscount.getDouble(1) * (entryPer.getValue() / 100);
		    if (hmSettlementWiseData.containsKey(entryPer.getKey()))
		    {
			hmSettlementWiseDtlData = hmSettlementWiseData.get(entryPer.getKey());
			hmSettlementWiseDtlData.put("Discount", hmSettlementWiseDtlData.get("Discount") + discAmt);
		    }
		    else
		    {
			hmSettlementWiseDtlData.put("Discount", discAmt);
		    }
		    flgDiscount = true;
		}
		rsDiscount.close();

		if (!flgDiscount)
		{
		    if (!hmSettlementWiseDtlData.containsKey("Discount"))
		    {
			hmSettlementWiseDtlData.put("Discount", 0.00);
		    }
		}
		//hmSettlementWiseDtlData.put("NoOfBills",1.00);

		hmSettlementWiseData.put(entryPer.getKey(), hmSettlementWiseDtlData);
	    }
	}
	rsMultiSettledBills.close();

	if (hmSettlementWiseData.size() > 0)
	{
	    clsUtility objUtility = new clsUtility();
	    int cnt = 7;

	    Map<String, Double> mapVerticalTotal = new HashMap<String, Double>();
	    //Map<Integer ,String> mapSequence=new TreeMap<Integer,String>();
	    double settleAmt = 0, roundOff = 0, tipAmt = 0, discountAmt = 0;
	    //int noOfBills=0;
	    double totalVertSettleAmt = 0, totalVertTipAmt = 0, totalVertTotalAmt = 0, totalVerNoOfBill = 0, totalVerRndOff = 0, totalVertDiscAmt = 0;

	    dmManagerSummaryFlash.addColumn("Settlement Name");
	    dmManagerSummaryFlash.addColumn("Settlement Amt");

	    for (Map.Entry<Integer, String> entry : mapTaxHeaders.entrySet())
	    {
		String sql = "select strTaxDesc,strTaxShortName from tbltaxhd where strTaxCode='" + entry.getValue() + "' ";
		ResultSet rsTaxDtl = clsGlobalVarClass.dbMysql.executeResultSet(sql);
		while (rsTaxDtl.next())
		{
		    cnt++;
		    dmManagerSummaryFlash.addColumn(rsTaxDtl.getString(1));
		}
	    }
	    dmManagerSummaryFlash.addColumn("Discount");
	    dmManagerSummaryFlash.addColumn("Rnd.Off");
	    dmManagerSummaryFlash.addColumn("Total");
	    dmManagerSummaryFlash.addColumn("Tip");
	    //dmManagerSummaryFlash.addColumn("No Of Bills");

	    for (Map.Entry<String, Map<String, Double>> entry : hmSettlementWiseData.entrySet())
	    {
		Vector vRows = new Vector();
		Map<String, Double> hmDtlData = entry.getValue();
		//settleAmt=Math.rint(entry.getValue().get("SettleAmt"));
		settleAmt = entry.getValue().get("SettleAmt");
		String settleType = entry.getKey();
		roundOff = 0;

		vRows.addElement(settleType);
		vRows.addElement(String.valueOf(gDecimalFormat.format(settleAmt)));

		double totalTaxAmt = 0;
		for (Map.Entry<Integer, String> entryTaxCode : mapTaxHeaders.entrySet())
		{
		    if (hmDtlData.containsKey(entryTaxCode.getValue()))
		    {
			//double taxAmt=Math.rint(hmDtlData.get(entryTaxCode.getValue()));
			double taxAmt = hmDtlData.get(entryTaxCode.getValue());
			vRows.addElement(String.valueOf(gDecimalFormat.format(taxAmt)));
			totalTaxAmt += taxAmt;

			if (mapVerticalTotal.containsKey(entryTaxCode.getValue()))
			{
			    double tempTaxAmt = mapVerticalTotal.get(entryTaxCode.getValue());
			    mapVerticalTotal.put(entryTaxCode.getValue(), taxAmt + tempTaxAmt);
			}
			else
			{
			    mapVerticalTotal.put(entryTaxCode.getValue(), taxAmt);
			}
			//mapSequence.put(count,entryTaxCode.getValue());
		    }
		    else
		    {
			vRows.addElement("0.00");
		    }
		}

		if (hmDtlData.containsKey("Discount"))
		{
		    //discountAmt=Math.rint(hmDtlData.get("Discount"));
		    discountAmt = hmDtlData.get("Discount");
		}
		tipAmt = hmDtlData.get("Tip Amt");
		roundOff = hmDtlData.get("RoundOff");

		//int noOfBills = hmDtlData.get("NoOfBills").intValue();

		totalVertDiscAmt += discountAmt;
		totalVertSettleAmt += settleAmt;
		totalVertTipAmt += tipAmt;
		//totalVerNoOfBill += noOfBills;
		totalVerRndOff += roundOff;
		//double totalHorizontalAmt=hmDtlData.get("TotalHorizontalAmt");

		double totalHorizontalAmt = (settleAmt + totalTaxAmt) - discountAmt;
		totalVertTotalAmt += totalHorizontalAmt;

		vRows.addElement(String.valueOf(gDecimalFormat.format(discountAmt)));
		vRows.addElement(String.valueOf(gDecimalFormat.format(roundOff)));
		vRows.addElement(String.valueOf(gDecimalFormat.format(totalHorizontalAmt)));
		vRows.addElement(String.valueOf(gDecimalFormat.format(tipAmt)));
		//vRows.addElement(String.valueOf(gDecimalFormat.format(noOfBills)));

		dmManagerSummaryFlash.addRow(vRows);
	    }

	    totalVerNoOfBill += billCount;
	    mapVerticalTotal.put("SettlementTotal", totalVertSettleAmt);
	    mapVerticalTotal.put("TotalAmount", totalVertTotalAmt);
	    mapVerticalTotal.put("DiscountTotal", totalVertDiscAmt);
	    mapVerticalTotal.put("TipTotal", totalVertTipAmt);
	    mapVerticalTotal.put("TotalNoOfBill", totalVerNoOfBill);
	    mapVerticalTotal.put("TotalRndOff", totalVerRndOff);

	    totalDm.setRowCount(0);
	    totalDm.addColumn("");
	    totalDm.addColumn("");
	    for (Map.Entry<Integer, String> entry : mapTaxHeaders.entrySet())
	    {
		totalDm.addColumn("");
	    }
	    totalDm.addColumn("");
	    totalDm.addColumn("");
	    totalDm.addColumn("");
	    totalDm.addColumn("");
	    //totalDm.addColumn("");

	    Vector totalRow = new Vector();

	    totalRow.add("Totals");
	    totalRow.add(String.valueOf(gDecimalFormat.format(mapVerticalTotal.get("SettlementTotal"))));
	    for (Map.Entry<Integer, String> entry : mapTaxHeaders.entrySet())
	    {
		double taxVal = 0;
		System.out.println(entry.getKey() + "   " + entry.getValue());
		if (mapVerticalTotal.containsKey(entry.getValue()))
		{
		    taxVal = mapVerticalTotal.get(entry.getValue());
		}
		totalRow.add(String.valueOf(gDecimalFormat.format(taxVal)));
	    }
	    totalRow.add(String.valueOf(gDecimalFormat.format(mapVerticalTotal.get("DiscountTotal"))));
	    totalRow.add(String.valueOf(gDecimalFormat.format(mapVerticalTotal.get("TotalRndOff"))));
	    totalRow.add(String.valueOf(gDecimalFormat.format(mapVerticalTotal.get("TotalAmount") + totalVerRndOff)));
	    totalRow.add(String.valueOf(gDecimalFormat.format(mapVerticalTotal.get("TipTotal"))));
	   // totalRow.add(String.valueOf(gDecimalFormat.format(mapVerticalTotal.get("TotalNoOfBill"))));

	    totalDm.addRow(totalRow);

	    double unbilledAmt = 0;
	    String sql = "select sum(dblAmount) from tblitemrtemp ";
	    if (!POSCode.equalsIgnoreCase("All"))
	    {
		sql += " where strPOSCode='" + POSCode + "' ";
	    }
	    ResultSet rsUnbilledAmt = clsGlobalVarClass.dbMysql.executeResultSet(sql);
	    if (rsUnbilledAmt.next())
	    {
		unbilledAmt = rsUnbilledAmt.getDouble(1);
	    }
	    rsUnbilledAmt.close();
	    double totalNetAmt = totalVertSettleAmt + unbilledAmt;
	    lblUnbilledOrderAmt.setText(gDecimalFormat.format(unbilledAmt));
	    lblNetTotalAmt.setText(gDecimalFormat.format(totalNetAmt));

	    tblManagerSummaryFlash.setModel(dmManagerSummaryFlash);
	    tblTotal.setModel(totalDm);

	    tblManagerSummaryFlash.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    tblTotal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

	    DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
	    rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
	    for (int counter = 1; counter < tblManagerSummaryFlash.getColumnCount(); counter++)
	    {
		tblManagerSummaryFlash.getColumnModel().getColumn(counter).setCellRenderer(rightRenderer);
		tblTotal.getColumnModel().getColumn(counter).setCellRenderer(rightRenderer);
	    }

	    // Code to show cash management.
	    sql = "select strTransType,sum(dblAmount) "
		    + " from tblcashmanagement "
		    + " where date(dteTransDate) between '" + fromDate + "' and '" + toDate + "' ";
	    if (!POSCode.equalsIgnoreCase("All"))
	    {
		sql += " and strPOSCode='" + POSCode + "' ";
	    }
	    sql += " group by strTransType ";
	    ResultSet rsCashMgmt = clsGlobalVarClass.dbMysql.executeResultSet(sql);
	    double dblCashMagntBalAmt = 0.00;
	    while (rsCashMgmt.next())
	    {
		if (rsCashMgmt.getString(1).equalsIgnoreCase("Float"))
		{
		    lblFloatAmt.setText(gDecimalFormat.format(rsCashMgmt.getDouble(2)));
		    dblCashMagntBalAmt += rsCashMgmt.getDouble(2);
		}
		else if (rsCashMgmt.getString(1).equalsIgnoreCase("Transfer In"))
		{
		    lblTransferInAmt.setText(gDecimalFormat.format(rsCashMgmt.getDouble(2)));
		    dblCashMagntBalAmt += rsCashMgmt.getDouble(2);
		}
		else if (rsCashMgmt.getString(1).equalsIgnoreCase("Refund"))
		{
		    lblRefundAmt.setText(gDecimalFormat.format(rsCashMgmt.getDouble(2)));
		    dblCashMagntBalAmt -= rsCashMgmt.getDouble(2);
		}
		else if (rsCashMgmt.getString(1).equalsIgnoreCase("Withdrawal"))
		{
		    lblWithdrawlAmt.setText(gDecimalFormat.format(rsCashMgmt.getDouble(2)));
		    dblCashMagntBalAmt -= rsCashMgmt.getDouble(2);
		}
		else if (rsCashMgmt.getString(1).equalsIgnoreCase("Payments"))
		{
		    lblPaymentsAmt.setText(gDecimalFormat.format(rsCashMgmt.getDouble(2)));
		    dblCashMagntBalAmt -= rsCashMgmt.getDouble(2);
		}
		else if (rsCashMgmt.getString(1).equalsIgnoreCase("Transfer Out"))
		{
		    lblTransferOutAmt.setText(gDecimalFormat.format(rsCashMgmt.getDouble(2)));
		    dblCashMagntBalAmt -= rsCashMgmt.getDouble(2);
		}
	    }
	    rsCashMgmt.close();

	    double dblCashTotal = 0.00;
	    //live
	    String sqlCashSettlementAmt = " select sum(dblSettlementAmt) "
		    + " from tblbillhd a,tblbillsettlementdtl b,tblsettelmenthd c "
		    + " where a.strBillNo=b.strBillNo and b.strSettlementCode=c.strSettelmentCode "
		    + " and a.strClientCode=b.strClientCode and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		    + " and c.strSettelmentType='Cash' ";
	    if (!POSCode.equalsIgnoreCase("All"))
	    {
		sqlCashSettlementAmt += " and a.strPOSCode='" + POSCode + "' ";
	    }
	    ResultSet rsCashSettlementAmt = clsGlobalVarClass.dbMysql.executeResultSet(sqlCashSettlementAmt);
	    if (rsCashSettlementAmt.next())
	    {
		dblCashTotal += rsCashSettlementAmt.getDouble(1);
		dblCashMagntBalAmt += rsCashSettlementAmt.getDouble(1);
	    }
	    rsCashSettlementAmt.close();
	    //q
	    sqlCashSettlementAmt = " select sum(dblSettlementAmt) "
		    + " from tblqbillhd a,tblqbillsettlementdtl b,tblsettelmenthd c "
		    + " where a.strBillNo=b.strBillNo and b.strSettlementCode=c.strSettelmentCode "
		    + " and a.strClientCode=b.strClientCode and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		    + " and c.strSettelmentType='Cash' ";
	    if (!POSCode.equalsIgnoreCase("All"))
	    {
		sqlCashSettlementAmt += " and a.strPOSCode='" + POSCode + "' ";
	    }
	    rsCashSettlementAmt = clsGlobalVarClass.dbMysql.executeResultSet(sqlCashSettlementAmt);
	    if (rsCashSettlementAmt.next())
	    {
		dblCashTotal += rsCashSettlementAmt.getDouble(1);
		dblCashMagntBalAmt += rsCashSettlementAmt.getDouble(1);
	    }
	    rsCashSettlementAmt.close();

	    lblCashAmt.setText(gDecimalFormat.format(dblCashTotal));

	    //set balance amount
	    lblCashMagntBalAmt.setText(gDecimalFormat.format(dblCashMagntBalAmt));

	    String sqlComplimentarySettlementAmt = " select sum(dblSettlementAmt) "
		    + " from tblqbillhd a,tblqbillsettlementdtl b,tblsettelmenthd c "
		    + " where a.strBillNo=b.strBillNo and b.strSettlementCode=c.strSettelmentCode "
		    + " and a.strClientCode=b.strClientCode and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		    + " and c.strSettelmentType='Complementary' ";
	    if (!POSCode.equalsIgnoreCase("All"))
	    {
		sqlComplimentarySettlementAmt += " and a.strPOSCode='" + POSCode + "' ";
	    }
	    ResultSet rsCompSettlementAmt = clsGlobalVarClass.dbMysql.executeResultSet(sqlComplimentarySettlementAmt);
	    if (rsCompSettlementAmt.next())
	    {
		lblCompAmt.setText(gDecimalFormat.format(rsCompSettlementAmt.getDouble(1)));
	    }
	    rsCompSettlementAmt.close();

	    String sqlNCKOTAmt = "select sum(dblAmount) "
		    + "from tblitemrtemp where strNCKotYN='Y' ";
	    if (!POSCode.equalsIgnoreCase("All"))
	    {
		sqlNCKOTAmt += " and strPOSCode='" + POSCode + "' ";
	    }
	    ResultSet rsNCKOTAmt = clsGlobalVarClass.dbMysql.executeResultSet(sqlNCKOTAmt);
	    if (rsNCKOTAmt.next())
	    {
		lblNCKOTAmt.setText(gDecimalFormat.format(rsNCKOTAmt.getDouble(1)));
	    }
	    rsNCKOTAmt.close();

	}

	sbSqlQFile = null;

	tblManagerSummaryFlash.getColumnModel().getColumn(0).setPreferredWidth(110);//settName
	tblManagerSummaryFlash.getColumnModel().getColumn(1).setPreferredWidth(110);//settAmt
	tblManagerSummaryFlash.getColumnModel().getColumn(2).setPreferredWidth(150);//
	tblManagerSummaryFlash.getColumnModel().getColumn(3).setPreferredWidth(150);
	tblManagerSummaryFlash.getColumnModel().getColumn(4).setPreferredWidth(75);
	tblManagerSummaryFlash.getColumnModel().getColumn(5).setPreferredWidth(55);
	tblManagerSummaryFlash.getColumnModel().getColumn(6).setPreferredWidth(150);
	tblManagerSummaryFlash.getColumnModel().getColumn(7).setPreferredWidth(70);
	tblManagerSummaryFlash.getColumnModel().getColumn(8).setPreferredWidth(75);

	tblTotal.getColumnModel().getColumn(0).setPreferredWidth(110);
	tblTotal.getColumnModel().getColumn(1).setPreferredWidth(110);
	tblTotal.getColumnModel().getColumn(2).setPreferredWidth(150);
	tblTotal.getColumnModel().getColumn(3).setPreferredWidth(150);
	tblTotal.getColumnModel().getColumn(4).setPreferredWidth(75);
	tblTotal.getColumnModel().getColumn(5).setPreferredWidth(55);
	tblTotal.getColumnModel().getColumn(6).setPreferredWidth(150);
	tblTotal.getColumnModel().getColumn(7).setPreferredWidth(70);
	tblTotal.getColumnModel().getColumn(8).setPreferredWidth(75);

    }

    private void funExportButtonClicked() throws Exception
    {
	File theDir = new File(clsPosConfigFile.exportReportPath);
	File file = new File(clsPosConfigFile.exportReportPath + File.separator + "ManagerSummaryFlash" + clsGlobalVarClass.gPOSDateToDisplay + ".xls");
	if (!theDir.exists())
	{
	    theDir.mkdir();
	    funExportFile(tblManagerSummaryFlash, file);
	}
	else
	{
	    funExportFile(tblManagerSummaryFlash, file);
	}
    }

    private void funExportFile(JTable table, File file) throws Exception
    {
	WritableWorkbook workbook1 = Workbook.createWorkbook(file);
	WritableSheet sheet1 = workbook1.createSheet("First Sheet", 0);
	TableModel model = table.getModel();
	sheet1.addCell(new Label(0, 0, "ManagerSummaryFlash"));

	vSalesReportExcelColLength = new java.util.Vector();
	for (int i = 0; i < model.getColumnCount(); i++)
	{
	    vSalesReportExcelColLength.add("10#Left");
	}

	for (int i = 0; i < model.getColumnCount(); i++)
	{
	    Label column = new Label(i, 1, model.getColumnName(i));
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

		String data = "";
		if (null != model.getValueAt(k, j))
		{
		    data = model.getValueAt(k, j).toString();
		}
		Label row = new Label(j, i + 1, data);		
		sheet1.setColumnView(j, data.length() + colLen);
		sheet1.addCell(row);
	    }
	    k++;
	}
	funAddLastOfExportReport(workbook1);
	workbook1.write();
	workbook1.close();
	Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + clsPosConfigFile.exportReportPath + "/" + "ManagerSummaryFlash" + clsGlobalVarClass.gPOSDateToDisplay + ".xls");

    }

    private void funAddLastOfExportReport(WritableWorkbook workbook1)
    {
	try
	{
	    int i = 0, j = 0, LastIndexReport = 2;
	    LastIndexReport = 0;
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

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
        pnlBackGround = new JPanel() {
            public void paintComponent(Graphics g) {
                Image img = Toolkit.getDefaultToolkit().getImage(
                    getClass().getResource("/com/POSReport/images/imgBGJPOS.png"));
                g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), this);
            }
        };  ;
        pnlMain = new javax.swing.JPanel();
        lblPosCode = new javax.swing.JLabel();
        cmbPosCode = new javax.swing.JComboBox();
        lblFromDate = new javax.swing.JLabel();
        dteFromDate = new com.toedter.calendar.JDateChooser();
        lblToDate = new javax.swing.JLabel();
        dteToDate = new com.toedter.calendar.JDateChooser();
        pnlDayEnd = new javax.swing.JScrollPane();
        tblManagerSummaryFlash = new javax.swing.JTable();
        btnClose = new javax.swing.JButton();
        pnltotal = new javax.swing.JScrollPane();
        tblTotal = new javax.swing.JTable();
        btnExport = new javax.swing.JButton();
        lblUnbilledOrders = new javax.swing.JLabel();
        lblUnbilledOrderAmt = new javax.swing.JLabel();
        lblNetTotalAmt = new javax.swing.JLabel();
        lblNetTotal = new javax.swing.JLabel();
        lblFloat = new javax.swing.JLabel();
        lblTransferIn = new javax.swing.JLabel();
        lblCash = new javax.swing.JLabel();
        lblRefund = new javax.swing.JLabel();
        lblWithdrawl = new javax.swing.JLabel();
        lblTransferOut = new javax.swing.JLabel();
        lblPayments = new javax.swing.JLabel();
        lblComplimentary = new javax.swing.JLabel();
        lblNCKOT = new javax.swing.JLabel();
        lblFloatAmt = new javax.swing.JLabel();
        lblTransferInAmt = new javax.swing.JLabel();
        lblCashAmt = new javax.swing.JLabel();
        lblRefundAmt = new javax.swing.JLabel();
        lblWithdrawlAmt = new javax.swing.JLabel();
        lblTransferOutAmt = new javax.swing.JLabel();
        lblPaymentsAmt = new javax.swing.JLabel();
        lblCompAmt = new javax.swing.JLabel();
        lblNCKOTAmt = new javax.swing.JLabel();
        lblCashMagtBalance = new javax.swing.JLabel();
        lblCashMagntBalAmt = new javax.swing.JLabel();
        btnExecute = new javax.swing.JButton();
        btnExport1 = new javax.swing.JButton();
        btnClose1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setExtendedState(MAXIMIZED_BOTH);
        setMinimumSize(new java.awt.Dimension(800, 600));
        setUndecorated(true);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        pnlheader.setBackground(new java.awt.Color(69, 164, 238));
        pnlheader.setLayout(new javax.swing.BoxLayout(pnlheader, javax.swing.BoxLayout.LINE_AXIS));

        lblProductName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblProductName.setForeground(new java.awt.Color(255, 255, 255));
        lblProductName.setText("SPOS - ");
        lblProductName.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblProductNameMouseClicked(evt);
            }
        });
        pnlheader.add(lblProductName);

        lblModuleName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblModuleName.setForeground(new java.awt.Color(255, 255, 255));
        pnlheader.add(lblModuleName);

        lblformName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblformName.setForeground(new java.awt.Color(255, 255, 255));
        lblformName.setText("-Manager Summary Flash");
        lblformName.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
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
        lblPosName.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
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
        lblUserCode.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblUserCodeMouseClicked(evt);
            }
        });
        pnlheader.add(lblUserCode);

        lblDate.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblDate.setForeground(new java.awt.Color(255, 255, 255));
        lblDate.setMaximumSize(new java.awt.Dimension(192, 30));
        lblDate.setMinimumSize(new java.awt.Dimension(192, 30));
        lblDate.setPreferredSize(new java.awt.Dimension(192, 30));
        lblDate.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblDateMouseClicked(evt);
            }
        });
        pnlheader.add(lblDate);

        lblHOSign.setMaximumSize(new java.awt.Dimension(34, 30));
        lblHOSign.setMinimumSize(new java.awt.Dimension(34, 30));
        lblHOSign.setPreferredSize(new java.awt.Dimension(34, 30));
        lblHOSign.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblHOSignMouseClicked(evt);
            }
        });
        pnlheader.add(lblHOSign);

        getContentPane().add(pnlheader, java.awt.BorderLayout.PAGE_START);

        pnlBackGround.setOpaque(false);
        pnlBackGround.setLayout(new java.awt.GridBagLayout());

        pnlMain.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, new java.awt.Color(204, 204, 204), new java.awt.Color(204, 204, 204), new java.awt.Color(153, 153, 153), new java.awt.Color(153, 153, 153)));
        pnlMain.setMaximumSize(new java.awt.Dimension(800, 570));
        pnlMain.setMinimumSize(new java.awt.Dimension(800, 570));
        pnlMain.setOpaque(false);
        pnlMain.setPreferredSize(new java.awt.Dimension(800, 570));

        lblPosCode.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblPosCode.setText("POS Name");

        cmbPosCode.setToolTipText("Select POS");

        lblFromDate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblFromDate.setText("From Date");

        dteFromDate.setToolTipText("Select From Date");
        dteFromDate.setPreferredSize(new java.awt.Dimension(119, 35));

        lblToDate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblToDate.setText("To Date");

        dteToDate.setToolTipText("Select To Date");
        dteToDate.setPreferredSize(new java.awt.Dimension(119, 35));

        tblManagerSummaryFlash.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        tblManagerSummaryFlash.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tblManagerSummaryFlash.setRowHeight(25);
        pnlDayEnd.setViewportView(tblManagerSummaryFlash);

        btnClose.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnClose.setForeground(new java.awt.Color(255, 255, 255));
        btnClose.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnClose.setText("Close");
        btnClose.setToolTipText("Close Window");
        btnClose.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnClose.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn2.png"))); // NOI18N
        btnClose.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnCloseMouseClicked(evt);
            }
        });
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        tblTotal.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        tblTotal.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tblTotal.setRowHeight(30);
        pnltotal.setViewportView(tblTotal);

        btnExport.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnExport.setForeground(new java.awt.Color(255, 255, 255));
        btnExport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnExport.setText("Export");
        btnExport.setToolTipText("Close Window");
        btnExport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExport.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn2.png"))); // NOI18N
        btnExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportActionPerformed(evt);
            }
        });

        lblUnbilledOrders.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lblUnbilledOrders.setText("Unbilled Orders ");

        lblUnbilledOrderAmt.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lblUnbilledOrderAmt.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblUnbilledOrderAmt.setText("0.00");

        lblNetTotalAmt.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblNetTotalAmt.setForeground(new java.awt.Color(0, 51, 255));
        lblNetTotalAmt.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblNetTotalAmt.setText("0.00");

        lblNetTotal.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblNetTotal.setForeground(new java.awt.Color(0, 51, 255));
        lblNetTotal.setText("Net Total ");

        lblFloat.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lblFloat.setText("FLOAT");

        lblTransferIn.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lblTransferIn.setText("TRANSFER IN");

        lblCash.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lblCash.setText("CASH SALE");

        lblRefund.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lblRefund.setText("REFUND");

        lblWithdrawl.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lblWithdrawl.setText("WITHDRAWL");

        lblTransferOut.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lblTransferOut.setText("TRANSFER OUT");

        lblPayments.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lblPayments.setText("PAYMENTS");

        lblComplimentary.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lblComplimentary.setText("COMPLIMENTARY");

        lblNCKOT.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lblNCKOT.setText("NC KOT");

        lblFloatAmt.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lblFloatAmt.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblFloatAmt.setText("0.00");

        lblTransferInAmt.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lblTransferInAmt.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTransferInAmt.setText("0.00");

        lblCashAmt.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lblCashAmt.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblCashAmt.setText("0.00");

        lblRefundAmt.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lblRefundAmt.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblRefundAmt.setText("0.00");

        lblWithdrawlAmt.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lblWithdrawlAmt.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblWithdrawlAmt.setText("0.00");

        lblTransferOutAmt.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lblTransferOutAmt.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTransferOutAmt.setText("0.00");

        lblPaymentsAmt.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lblPaymentsAmt.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblPaymentsAmt.setText("0.00");

        lblCompAmt.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lblCompAmt.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblCompAmt.setText("0.00");

        lblNCKOTAmt.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lblNCKOTAmt.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblNCKOTAmt.setText("0.00");

        lblCashMagtBalance.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblCashMagtBalance.setForeground(new java.awt.Color(0, 51, 255));
        lblCashMagtBalance.setText("BALANCE");

        lblCashMagntBalAmt.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblCashMagntBalAmt.setForeground(new java.awt.Color(0, 51, 255));
        lblCashMagntBalAmt.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblCashMagntBalAmt.setText("0.00");

        btnExecute.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnExecute.setForeground(new java.awt.Color(255, 255, 255));
        btnExecute.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnExecute.setText("Execute");
        btnExecute.setToolTipText("Execute");
        btnExecute.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExecute.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn2.png"))); // NOI18N
        btnExecute.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnExecuteMouseClicked(evt);
            }
        });

        btnExport1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnExport1.setForeground(new java.awt.Color(255, 255, 255));
        btnExport1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnExport1.setText("Export");
        btnExport1.setToolTipText("Close Window");
        btnExport1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExport1.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn2.png"))); // NOI18N
        btnExport1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnExport1MouseClicked(evt);
            }
        });
        btnExport1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExport1ActionPerformed(evt);
            }
        });

        btnClose1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnClose1.setForeground(new java.awt.Color(255, 255, 255));
        btnClose1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnClose1.setText("Close");
        btnClose1.setToolTipText("Close Window");
        btnClose1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnClose1.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn2.png"))); // NOI18N
        btnClose1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnClose1MouseClicked(evt);
            }
        });
        btnClose1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClose1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlMainLayout = new javax.swing.GroupLayout(pnlMain);
        pnlMain.setLayout(pnlMainLayout);
        pnlMainLayout.setHorizontalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMainLayout.createSequentialGroup()
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pnltotal, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 795, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlMainLayout.createSequentialGroup()
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlMainLayout.createSequentialGroup()
                                .addComponent(lblPosCode)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cmbPosCode, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblFromDate)
                                .addGap(3, 3, 3)
                                .addComponent(dteFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dteToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnExecute, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnExport1, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnClose1, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(pnlDayEnd, javax.swing.GroupLayout.PREFERRED_SIZE, 795, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(36, 36, 36)
                        .addComponent(btnExport, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblComplimentary, javax.swing.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
                            .addComponent(lblNCKOT, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblCompAmt, javax.swing.GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE)
                            .addComponent(lblNCKOTAmt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblUnbilledOrders, javax.swing.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
                            .addComponent(lblNetTotal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(lblNetTotalAmt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblUnbilledOrderAmt, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMainLayout.createSequentialGroup()
                                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(pnlMainLayout.createSequentialGroup()
                                        .addComponent(lblCashMagtBalance, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))
                                    .addGroup(pnlMainLayout.createSequentialGroup()
                                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(lblFloat, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(lblTransferIn, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(lblCash, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(6, 6, 6)))
                                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(lblFloatAmt, javax.swing.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE)
                                    .addComponent(lblTransferInAmt, javax.swing.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE)
                                    .addComponent(lblCashAmt, javax.swing.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE)
                                    .addComponent(lblCashMagntBalAmt, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMainLayout.createSequentialGroup()
                                .addComponent(lblRefund, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblRefundAmt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMainLayout.createSequentialGroup()
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblWithdrawl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblTransferOut, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblPayments, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblPaymentsAmt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblTransferOutAmt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblWithdrawlAmt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(235, 235, 235))
        );

        pnlMainLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {lblCashAmt, lblFloatAmt, lblPaymentsAmt, lblRefundAmt, lblTransferInAmt, lblTransferOutAmt, lblWithdrawlAmt});

        pnlMainLayout.setVerticalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnExport, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(21, 21, 21))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMainLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMainLayout.createSequentialGroup()
                                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblPosCode, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(cmbPosCode, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(lblFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(dteFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(dteToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(8, 8, 8))
                            .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(btnClose1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                .addComponent(btnExport1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                .addComponent(btnExecute, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addComponent(pnlDayEnd, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnltotal, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblUnbilledOrders, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblUnbilledOrderAmt, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblNetTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblNetTotalAmt, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblComplimentary, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblCompAmt, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblNCKOT, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblNCKOTAmt, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lblFloat, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblFloatAmt, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(pnlMainLayout.createSequentialGroup()
                                .addGap(27, 27, 27)
                                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(lblTransferIn, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblTransferInAmt, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(lblCash, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblCashAmt, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblRefund, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblRefundAmt, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblWithdrawl, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblWithdrawlAmt, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblTransferOut, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblTransferOutAmt, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblPayments, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblPaymentsAmt, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lblCashMagtBalance, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblCashMagntBalAmt, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)))))
        );

        pnlBackGround.add(pnlMain, new java.awt.GridBagConstraints());

        getContentPane().add(pnlBackGround, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Close Windows
     *
     * @param evt
     */
    private void btnCloseMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCloseMouseClicked
	funResetLookAndFeel();
	dispose();
	clsGlobalVarClass.hmActiveForms.remove("Manager Summary Flash");
    }//GEN-LAST:event_btnCloseMouseClicked

    private void btnExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportActionPerformed
	// TODO add your handling code here:

	try
	{
	    funExportButtonClicked();
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

	//funExportButtonPressed();
    }//GEN-LAST:event_btnExportActionPerformed

    private void lblProductNameMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblProductNameMouseClicked
    {//GEN-HEADEREND:event_lblProductNameMouseClicked
	// TODO add your handling code here:
    }//GEN-LAST:event_lblProductNameMouseClicked

    private void lblformNameMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblformNameMouseClicked
    {//GEN-HEADEREND:event_lblformNameMouseClicked
	// TODO add your handling code here:
    }//GEN-LAST:event_lblformNameMouseClicked

    private void lblPosNameMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblPosNameMouseClicked
    {//GEN-HEADEREND:event_lblPosNameMouseClicked
	// TODO add your handling code here:
    }//GEN-LAST:event_lblPosNameMouseClicked

    private void lblUserCodeMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblUserCodeMouseClicked
    {//GEN-HEADEREND:event_lblUserCodeMouseClicked
	// TODO add your handling code here:
    }//GEN-LAST:event_lblUserCodeMouseClicked

    private void lblDateMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblDateMouseClicked
    {//GEN-HEADEREND:event_lblDateMouseClicked
	// TODO add your handling code here:
    }//GEN-LAST:event_lblDateMouseClicked

    private void lblHOSignMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblHOSignMouseClicked
    {//GEN-HEADEREND:event_lblHOSignMouseClicked
	// TODO add your handling code here:
    }//GEN-LAST:event_lblHOSignMouseClicked

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed

    }//GEN-LAST:event_btnCloseActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
	dispose();
	funResetLookAndFeel();
	clsGlobalVarClass.hmActiveForms.remove("Manager Summary Flash");
    }//GEN-LAST:event_formWindowClosed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
	dispose();
	funResetLookAndFeel();
	clsGlobalVarClass.hmActiveForms.remove("Manager Summary Flash");
    }//GEN-LAST:event_formWindowClosing

    private void btnExport1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnExport1ActionPerformed
    {//GEN-HEADEREND:event_btnExport1ActionPerformed
	// TODO add your handling code here:
    }//GEN-LAST:event_btnExport1ActionPerformed

    private void btnClose1MouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_btnClose1MouseClicked
    {//GEN-HEADEREND:event_btnClose1MouseClicked
	dispose();
	funResetLookAndFeel();
	clsGlobalVarClass.hmActiveForms.remove("Manager Summary Flash");
    }//GEN-LAST:event_btnClose1MouseClicked

    private void btnClose1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnClose1ActionPerformed
    {//GEN-HEADEREND:event_btnClose1ActionPerformed
	// TODO add your handling code here:
    }//GEN-LAST:event_btnClose1ActionPerformed

    private void btnExecuteMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_btnExecuteMouseClicked
    {//GEN-HEADEREND:event_btnExecuteMouseClicked
	try
	{
	    funManagerSummaryFlash();
	}
	catch (Exception ex)
	{
	    ex.printStackTrace();
	}
    }//GEN-LAST:event_btnExecuteMouseClicked

    private void btnExport1MouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_btnExport1MouseClicked
    {//GEN-HEADEREND:event_btnExport1MouseClicked

	try
	{
	    funExportButtonClicked();
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
    }//GEN-LAST:event_btnExport1MouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnClose1;
    private javax.swing.JButton btnExecute;
    private javax.swing.JButton btnExport;
    private javax.swing.JButton btnExport1;
    private javax.swing.JComboBox cmbPosCode;
    private com.toedter.calendar.JDateChooser dteFromDate;
    private com.toedter.calendar.JDateChooser dteToDate;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.JLabel lblCash;
    private javax.swing.JLabel lblCashAmt;
    private javax.swing.JLabel lblCashMagntBalAmt;
    private javax.swing.JLabel lblCashMagtBalance;
    private javax.swing.JLabel lblCompAmt;
    private javax.swing.JLabel lblComplimentary;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblFloat;
    private javax.swing.JLabel lblFloatAmt;
    private javax.swing.JLabel lblFromDate;
    private javax.swing.JLabel lblHOSign;
    private javax.swing.JLabel lblModuleName;
    private javax.swing.JLabel lblNCKOT;
    private javax.swing.JLabel lblNCKOTAmt;
    private javax.swing.JLabel lblNetTotal;
    private javax.swing.JLabel lblNetTotalAmt;
    private javax.swing.JLabel lblPayments;
    private javax.swing.JLabel lblPaymentsAmt;
    private javax.swing.JLabel lblPosCode;
    private javax.swing.JLabel lblPosName;
    private javax.swing.JLabel lblProductName;
    private javax.swing.JLabel lblRefund;
    private javax.swing.JLabel lblRefundAmt;
    private javax.swing.JLabel lblToDate;
    private javax.swing.JLabel lblTransferIn;
    private javax.swing.JLabel lblTransferInAmt;
    private javax.swing.JLabel lblTransferOut;
    private javax.swing.JLabel lblTransferOutAmt;
    private javax.swing.JLabel lblUnbilledOrderAmt;
    private javax.swing.JLabel lblUnbilledOrders;
    private javax.swing.JLabel lblUserCode;
    private javax.swing.JLabel lblWithdrawl;
    private javax.swing.JLabel lblWithdrawlAmt;
    private javax.swing.JLabel lblformName;
    private javax.swing.JPanel pnlBackGround;
    private javax.swing.JScrollPane pnlDayEnd;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JPanel pnlheader;
    private javax.swing.JScrollPane pnltotal;
    private javax.swing.JTable tblManagerSummaryFlash;
    private javax.swing.JTable tblTotal;
    // End of variables declaration//GEN-END:variables

}
