package com.POSReport.view;

import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsPosConfigFile;
import com.POSGlobal.controller.clsUtility;
import com.POSGlobal.controller.clsUtility2;
import com.POSGlobal.view.frmMultiPOSSelection;
import com.POSPrinting.clsVoidKOTAuditingGenerator;
import com.POSPrinting.clsVoidKOTGenerator;
import com.POSReport.controller.clsBillItemDtlBean;
import com.POSReport.controller.clsKOTAnalysisBean;
import com.POSReport.controller.clsWaiterAnalysisBean;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
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
import jxl.write.WriteException;



public class frmAuditFlash extends javax.swing.JFrame
{

    private String fromDate, toDate;
    DefaultTableModel dm, totalDm;
    private String exportFormName, ExportReportPath, reportName;
    private clsUtility objUtility;
    private Set selectedPOSCodeSet;
    public int navigate;
    StringBuilder sb = new StringBuilder();
    private DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();
    private DecimalFormat decFormatForQty = new DecimalFormat("0");

    public frmAuditFlash()
    {
	initComponents();

	funSetLookAndFeel();
	selectedPOSCodeSet = new HashSet();
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
	    ExportReportPath = clsPosConfigFile.exportReportPath;
	    exportFormName = "Modifed Bill";
	    funFillComboBox();

	    dteFromDate.setDate(objUtility.funGetDateToSetCalenderDate());
	    dteToDate.setDate(objUtility.funGetDateToSetCalenderDate());

	    funModifiedBillFlash();

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
     * this function is used Filling POS Code,Reason code User Code ComboBoxs
     */
    private void funFillComboBox()
    {
	ResultSet objResultSet = null;
	try
	{
	    if (clsGlobalVarClass.gShowOnlyLoginPOSReports)
	    {
		cmbPosCode.addItem(clsGlobalVarClass.gPOSName + " " + clsGlobalVarClass.gPOSCode);

	    }
	    else
	    {
		cmbPosCode.addItem("All");
		cmbPosCode.addItem("Multiple");
		String sql = "select strPosName,strPosCode from tblposmaster";
		objResultSet = clsGlobalVarClass.dbMysql.executeResultSet(sql);
		while (objResultSet.next())
		{
		    cmbPosCode.addItem(objResultSet.getString(1) + " " + objResultSet.getString(2));
		}
		objResultSet.close();

	    }

	    cmbUser.addItem("All");
	    objResultSet = clsGlobalVarClass.dbMysql.executeResultSet("select strUserName,strUserCode from tbluserhd");
	    while (objResultSet.next())
	    {
		cmbUser.addItem(objResultSet.getString(1) + "                         " + objResultSet.getString(2));
	    }
	    objResultSet.close();

	    cmbReason.addItem("All");
	    objResultSet = clsGlobalVarClass.dbMysql.executeResultSet("select strReasonName,strreasonCode from tblreasonmaster");
	    while (objResultSet.next())
	    {
		cmbReason.addItem(objResultSet.getString(1) + "                                                                                                                         " + objResultSet.getString(2));
	    }
	    objResultSet.close();

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    /**
     * this Function is used for get Selected POS Code
     *
     * @return
     */
    /**
     * this Function is used for get user Code
     *
     * @return
     */
    private String funGetSelectedUserCode(String user)
    {
	//String temp = cmbUser.getSelectedItem().toString();
	StringBuilder sb = new StringBuilder(user);
	int len = user.length();
	int lastInd = sb.lastIndexOf(" ");
	String userCode = sb.substring(lastInd + 1, len).toString();
	return userCode;
    }

    /**
     * this Function is used for get Reason Code
     *
     * @return
     */
    private String funGetSelectedReasonCode(String reason)
    {
	//String temp = cmbReason.getSelectedItem().toString();
	StringBuilder sb = new StringBuilder(reason);
	int len = reason.length();
	int lastInd = sb.lastIndexOf(" ");
	String ResonCode = sb.substring(lastInd + 1, len).toString();
	return ResonCode;
    }

    /**
     * this Function is used for Audit Report
     *
     * @return
     */
    private void funTimeAuditReport()
    {
	StringBuilder sbSql = new StringBuilder();
	try
	{
	    tblTotal.setVisible(false);
	    exportFormName = "Time Audit";
	    fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());

	    toDate = objUtility.funGetFromToDate(dteToDate.getDate());
	    String userCode = funGetSelectedUserCode(cmbUser.getSelectedItem().toString());
	    String POSCode = objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString().trim());
	    DefaultTableModel dmOpenTableColumn = new DefaultTableModel()
	    {
		@Override
		public boolean isCellEditable(int row, int column)
		{
		    //all cells false
		    return false;
		}
	    };
	    DefaultTableModel dm2 = new DefaultTableModel()
	    {
		@Override
		public boolean isCellEditable(int row, int column)
		{
		    //all cells false
		    return false;
		}
	    };
	    dmOpenTableColumn.getDataVector().removeAllElements();
	    tblAuditFlash.updateUI();

	    dmOpenTableColumn.addColumn("Bill No");
	    dmOpenTableColumn.addColumn("Bill Date");
	    dmOpenTableColumn.addColumn("KOT Time");
	    dmOpenTableColumn.addColumn("Bill Time");
	    dmOpenTableColumn.addColumn("Settle Time");
	    dmOpenTableColumn.addColumn("Difference");
	    dmOpenTableColumn.addColumn("User Created");
	    dmOpenTableColumn.addColumn("User Edited");
	    dmOpenTableColumn.addColumn("Remarks");

	    sbSql.setLength(0);
	    sbSql.append("SELECT a.strbillno, DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y') AS BillDate "
		    + ",TIME_FORMAT(TIME(a.dteBillDate),'%h:%i') AS BillTime, TIME_FORMAT(TIME(b.dteBillDate),'%h:%i') AS KOTTime "
		    + ",TIME_FORMAT(TIME(a.dteSettleDate),'%h:%i')SettleTime, DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y') "
		    + ",DATE_FORMAT(DATE(a.dteSettleDate),'%d-%m-%Y')SettleDate,a.strUserCreated,a.strUserEdited, IFNULL(a.strRemarks,'') "
		    + ",SEC_TO_TIME(TIMESTAMPDIFF(second,a.dteBillDate,a.dteSettleDate)) AS diffInBillnSettled  "
		    + "from tblbillhd a, tblbilldtl b where a.strBillNo=b.strBillNo ");

	    if (!"All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()))
	    {
		sbSql.append(" and a.strUserCreated='" + userCode + "' and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + ""
			+ " and Date(a.dteDateCreated) between '" + fromDate + "' and '" + toDate + "'");
	    }
	    else if (!"All".equals(cmbPosCode.getSelectedItem()) && "All".equals(cmbUser.getSelectedItem()))
	    {
		sbSql.append(" and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " and Date(a.dteDateCreated) between '" + fromDate + "' and '" + toDate + "'");
	    }
	    else if ("All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()))
	    {
		sbSql.append(" and a.strUserCreated='" + userCode + "' and Date(a.dteDateCreated) between '" + fromDate + "' and '" + toDate + "'");
	    }
	    else
	    {
		sbSql.append(" and Date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "'");
	    }
	    sbSql.append(" group by a.strBillNo");

	    ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
	    while (rs.next())
	    {
		//String time = funTimeDiff(rs.getString(6), rs.getString(7));
		Object[] row =
		{
		    rs.getString(1), rs.getString(2), rs.getString(4), rs.getString(3), rs.getString(5), rs.getString(11), rs.getString(8), rs.getString(9), rs.getString(10)
		};
		dmOpenTableColumn.addRow(row);
	    }

	    sbSql.setLength(0);
	    sbSql.append("SELECT a.strbillno, DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y') AS BillDate "
		    + ",TIME_FORMAT(TIME(a.dteBillDate),'%h:%i') AS BillTime, TIME_FORMAT(TIME(b.dteBillDate),'%h:%i') AS KOTTime "
		    + ",TIME_FORMAT(TIME(a.dteSettleDate),'%h:%i')SettleTime, DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y') "
		    + ",DATE_FORMAT(DATE(a.dteSettleDate),'%d-%m-%Y')SettleDate,a.strUserCreated,a.strUserEdited, IFNULL(a.strRemarks,'') "
		    + ",if(a.dteBillDate>a.dteSettleDate,SEC_TO_TIME(TIMESTAMPDIFF(SECOND,a.dteSettleDate,a.dteBillDate)),SEC_TO_TIME(TIMESTAMPDIFF(SECOND,a.dteBillDate,a.dteSettleDate)))diffInBillnSettled  "
		    + "from tblqbillhd a, tblqbilldtl b where a.strBillNo=b.strBillNo ");

	    if (!"All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()))
	    {
		sbSql.append(" and a.strUserCreated='" + userCode + "' and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " and Date(a.dteDateCreated) between '" + fromDate + "' and '" + toDate + "'");
	    }
	    else if (!"All".equals(cmbPosCode.getSelectedItem()) && "All".equals(cmbUser.getSelectedItem()))
	    {
		sbSql.append(" and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " and Date(a.dteDateCreated) between '" + fromDate + "' and '" + toDate + "'");
	    }
	    else if ("All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()))
	    {
		sbSql.append(" and a.strUserCreated='" + userCode + "' and Date(a.dteDateCreated) between '" + fromDate + "' and '" + toDate + "'");
	    }
	    else
	    {
		sbSql.append(" and Date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "'");
	    }
	    sbSql.append(" group by a.strBillNo");

	    rs = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
	    while (rs.next())
	    {
		//String time = funTimeDiff(rs.getString(6), rs.getString(7));
		Object[] row =
		{
		    rs.getString(1), rs.getString(2), rs.getString(4), rs.getString(3), rs.getString(5), rs.getString(11), rs.getString(8), rs.getString(9), rs.getString(10)
		};
		dmOpenTableColumn.addRow(row);
	    }

	    tblAuditFlash.setModel(dmOpenTableColumn);
	    Object[] total =
	    {
		" ", " "
	    };
	    dm2.addRow(total);
	    tblTotal.setModel(dm2);

	    DefaultTableCellRenderer OpenrightRenderer = new DefaultTableCellRenderer();
	    OpenrightRenderer.setHorizontalAlignment(JLabel.RIGHT);
	    tblAuditFlash.getColumnModel().getColumn(0).setPreferredWidth(40);
	    tblAuditFlash.getColumnModel().getColumn(1).setPreferredWidth(40);
	    tblAuditFlash.getColumnModel().getColumn(2).setPreferredWidth(40);
	    tblAuditFlash.getColumnModel().getColumn(3).setPreferredWidth(60);
	    tblAuditFlash.getColumnModel().getColumn(4).setPreferredWidth(60);
	    tblAuditFlash.getColumnModel().getColumn(5).setPreferredWidth(30);
	    tblAuditFlash.getColumnModel().getColumn(6).setPreferredWidth(40);
	    tblAuditFlash.getColumnModel().getColumn(7).setPreferredWidth(40);

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	finally
	{
	    sbSql = null;
	}
    }

    /**
     * this Function is used for get Kot Wise Report
     *
     * @return
     */
    public void funKOTWise()
    {

	if (cmbType.getSelectedItem().toString().equalsIgnoreCase("Summary"))
	{
	    funSummaryKOTAnalysis();
	}
	else//detail
	{
	    //funDetailKOTAnalysisOld();

	    funDetailKOTAnalysis();
	}
    }

    /**
     * this Function is used for Moved KOT
     *
     * @return
     */
    private void funMovedKOTFlash()
    {
	StringBuilder sbSql = new StringBuilder();
	try
	{
	    reportName = "KOT";
	    tblTotal.setVisible(true);
	    exportFormName = "Moved Kot";
	    fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());

	    toDate = objUtility.funGetFromToDate(dteToDate.getDate());
	    String userCode = funGetSelectedUserCode(cmbUser.getSelectedItem().toString());
	    String POSCode = objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString().trim());
	    String reasonCode = funGetSelectedReasonCode(cmbReason.getSelectedItem().toString());
	    DefaultTableModel dm1 = new DefaultTableModel()
	    {
		@Override
		public boolean isCellEditable(int row, int column)
		{
		    //all cells false
		    return false;
		}
	    };
	    dm1.getDataVector().removeAllElements();
	    DefaultTableModel dm2 = new DefaultTableModel()
	    {
		@Override
		public boolean isCellEditable(int row, int column)
		{
		    //all cells false
		    return false;
		}
	    };
	    dm2.getDataVector().removeAllElements();
	    dm2.addColumn("");
	    dm2.addColumn("");
	    dm2.addColumn("");
	    dm2.addColumn("");

	    tblAuditFlash.updateUI();
	    tblTotal.updateUI();
	    dm1.addColumn("POS");
	    dm1.addColumn("Table");
	    dm1.addColumn("Waiter");
	    dm1.addColumn("KOT No");
	    dm1.addColumn("Item Name");
	    dm1.addColumn("Pax");
	    dm1.addColumn("Qty");
	    dm1.addColumn("Amount");
	    dm1.addColumn("Reason");
	    dm1.addColumn("User Created");
	    dm1.addColumn("Date Created");
	    dm1.addColumn("Remarks");
	    double sumQty = 0.00, sumTotalAmt = 0.00, pax = 0.00;

	    sbSql.setLength(0);
	    sbSql.append("select d.strPOSName,e.strTableName,b.strWShortName,a.strKOTNo "
		    + " ,a.strItemName,a.intPaxNo,a.dblItemQuantity,a.dblAmount,c.strReasonName "
		    + " ,a.strUserCreated,DATE_FORMAT(a.dteDateCreated,'%d-%m-%Y'),ifnull(a.strRemark,'') "
		    + " from tblvoidkot a left outer join tblwaitermaster b on a.strWaiterNo=b.strWaiterNo "
		    + " ,tblreasonmaster c,tblposmaster d,tbltablemaster e "
		    + " where a.strreasonCode=c.strreasonCode and a.strPOSCode=d.strPOSCode "
		    + " and a.strTableNo=e.strTableNo and a.strType='MVKot' ");

	    if (!"Summary".equals(cmbType.getSelectedItem()))
	    {
		if (!"All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append(" and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " and a.strUserCreated='" + userCode + "' "
			    + "and Date(a.dteDateCreated) between '" + fromDate + "' and '" + toDate + "' "
			    + "and a.strreasonCode='" + reasonCode + "'");
		}
		else if (!"All".equals(cmbPosCode.getSelectedItem()) && "All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append(" and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " and Date(a.dteDateCreated) between '"
			    + fromDate + "' and '" + toDate + "' and a.strreasonCode='" + reasonCode + "'");
		}
		else if ("All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append(" and a.strUserCreated='" + userCode + "' and Date(a.dteDateCreated) between '"
			    + fromDate + "' and '" + toDate + "' and a.strreasonCode='" + reasonCode + "'");
		}
		else if (!"All".equals(cmbPosCode.getSelectedItem()) && "All".equals(cmbUser.getSelectedItem()) && "All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append(" and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " and Date(a.dteDateCreated) between '"
			    + fromDate + "' and '" + toDate + "'");
		}
		else if ("All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append(" and a.strUserCreated='" + userCode + "' and Date(a.dteDateCreated) between '"
			    + fromDate + "' and '" + toDate + "' and a.strreasonCode='" + reasonCode + "'");
		}
		else if ("All".equals(cmbPosCode.getSelectedItem()) && "All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append(" and a.strreasonCode='" + reasonCode + "' and Date(a.dteDateCreated) between '"
			    + fromDate + "' and '" + toDate + "'");
		}
		else
		{
		    sbSql.append(" and Date(a.dteDateCreated) between '" + fromDate + "' and '" + toDate + "'");
		}

		ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
		while (rs.next())
		{
		    Object[] row =
		    {
			rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7), gDecimalFormat.format(rs.getDouble(8)), rs.getString(9), rs.getString(10), rs.getString(11), rs.getString(12)
		    };
		    dm1.addRow(row);
		    pax = pax + rs.getDouble(6);
		    sumQty = sumQty + rs.getDouble(7);
		    sumTotalAmt = sumTotalAmt + rs.getDouble(8);
		}
		rs.close();

		Object[] total =
		{
		    "Total", decFormatForQty.format(pax), decFormatForQty.format(sumQty), gDecimalFormat.format(sumTotalAmt)
		};
		dm2.addRow(total);
		tblAuditFlash.setModel(dm1);
		tblTotal.setModel(dm2);
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
		tblAuditFlash.getColumnModel().getColumn(0).setPreferredWidth(40);
		tblAuditFlash.getColumnModel().getColumn(1).setPreferredWidth(55);
		tblAuditFlash.getColumnModel().getColumn(3).setPreferredWidth(65);
		tblAuditFlash.getColumnModel().getColumn(5).setPreferredWidth(100);
		tblAuditFlash.getColumnModel().getColumn(4).setPreferredWidth(30);
		tblAuditFlash.getColumnModel().getColumn(6).setPreferredWidth(40);
		tblAuditFlash.getColumnModel().getColumn(7).setPreferredWidth(50);
		tblAuditFlash.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
		tblAuditFlash.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);
		tblAuditFlash.getColumnModel().getColumn(7).setCellRenderer(rightRenderer);

		DefaultTableCellRenderer rightRenderer1 = new DefaultTableCellRenderer();
		rightRenderer1.setHorizontalAlignment(JLabel.RIGHT);
		tblTotal.getColumnModel().getColumn(1).setCellRenderer(rightRenderer1);
		tblTotal.getColumnModel().getColumn(2).setCellRenderer(rightRenderer1);
		tblTotal.getColumnModel().getColumn(3).setCellRenderer(rightRenderer1);
		tblTotal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tblTotal.getColumnModel().getColumn(0).setPreferredWidth(240);
		tblTotal.getColumnModel().getColumn(1).setPreferredWidth(84);
		tblTotal.getColumnModel().getColumn(2).setPreferredWidth(130);
		tblTotal.getColumnModel().getColumn(3).setPreferredWidth(90);
		tblTotal.getColumnModel().getColumn(4).setPreferredWidth(230);

	    }
	    else
	    {
		dm1 = new DefaultTableModel()
		{
		    @Override
		    public boolean isCellEditable(int row, int column)
		    {
			//all cells false
			return false;
		    }
		};
		dm1.getDataVector().removeAllElements();
		dm2 = new DefaultTableModel()
		{
		    @Override
		    public boolean isCellEditable(int row, int column)
		    {
			//all cells false
			return false;
		    }
		};
		dm2.getDataVector().removeAllElements();
		dm2.addColumn("");
		dm2.addColumn("");
		dm2.addColumn("");
		dm2.addColumn("");
		tblAuditFlash.updateUI();
		tblTotal.updateUI();
		dm1.addColumn("POS");
		dm1.addColumn("Table");
		dm1.addColumn("Waiter");
		dm1.addColumn("KOT No");
		dm1.addColumn("Pax");
		dm1.addColumn("Amount");
		dm1.addColumn("Reason");
		dm1.addColumn("User Created");
		dm1.addColumn("Date Created");
		sumQty = 0.00;
		sumTotalAmt = 0.00;
		pax = 0.00;

		sbSql.setLength(0);
		sbSql.append("select d.strPOSName,e.strTableName,b.strWShortName,a.strKOTNo,a.intPaxNo,"
			+ " sum(a.dblAmount),c.strReasonName,a.strUserCreated,DATE_FORMAT(a.dteDateCreated,'%d-%m-%Y') "
			+ " from tblvoidkot a left outer join tblwaitermaster b on a.strWaiterNo=b.strWaiterNo "
			+ ",tblreasonmaster c,tblposmaster d,tbltablemaster e "
			+ " where a.strreasonCode=c.strreasonCode "
			+ " and a.strPOSCode=d.strPOSCode and a.strTableNo=e.strTableNo "
			+ " and a.strType='MVKot' ");

		if (!"All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append(" and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " and a.strUserCreated='" + userCode + "' "
			    + "and Date(a.dteDateCreated) between '" + fromDate + "' and '" + toDate + "' "
			    + "and a.strreasonCode='" + reasonCode + "'");
		}
		else if (!"All".equals(cmbPosCode.getSelectedItem()) && "All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append(" and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " and Date(a.dteDateCreated) between '"
			    + fromDate + "' and '" + toDate + "' and a.strreasonCode='" + reasonCode + "'");
		}
		else if ("All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append(" and a.strUserCreated='" + userCode + "' and Date(a.dteDateCreated) between '"
			    + fromDate + "' and '" + toDate + "' and a.strreasonCode='" + reasonCode + "'");
		}
		else if (!"All".equals(cmbPosCode.getSelectedItem()) && "All".equals(cmbUser.getSelectedItem()) && "All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append(" and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " and Date(a.dteDateCreated) between '"
			    + fromDate + "' and '" + toDate + "'");
		}
		else if ("All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append(" and a.strUserCreated='" + userCode + "' and Date(a.dteDateCreated) between '"
			    + fromDate + "' and '" + toDate + "' and a.strreasonCode='" + reasonCode + "'");
		}
		else if ("All".equals(cmbPosCode.getSelectedItem()) && "All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append(" and a.strreasonCode='" + reasonCode + "' and Date(a.dteDateCreated) between '"
			    + fromDate + "' and '" + toDate + "'");
		}
		else
		{
		    sbSql.append(" and Date(a.dteDateCreated) between '" + fromDate + "' and '" + toDate + "'");
		}
		sbSql.append(" Group By a.strPOSCode,a.strTableNo,b.strWShortName,a.strKOTNo,a.intPaxNo,"
			+ "c.strReasonName,a.strUserCreated");
		//System.out.println(sbSql.toString());
		ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
		while (rs.next())
		{
		    Object[] row =
		    {
			rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
			rs.getString(5), gDecimalFormat.format(rs.getDouble(6)), rs.getString(7), rs.getString(8), rs.getString(9)
		    };
		    dm1.addRow(row);
		    pax = pax + rs.getDouble(5);
		    sumTotalAmt = sumTotalAmt + rs.getDouble(6);
		}
		rs.close();

		Object[] total =
		{
		    "Total", decFormatForQty.format(pax), gDecimalFormat.format(sumTotalAmt), " "
		};
		dm2.addRow(total);
		tblAuditFlash.setModel(dm1);
		tblTotal.setModel(dm2);
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
		tblAuditFlash.getColumnModel().getColumn(0).setPreferredWidth(30);
		tblAuditFlash.getColumnModel().getColumn(1).setPreferredWidth(35);
		tblAuditFlash.getColumnModel().getColumn(2).setPreferredWidth(55);
		tblAuditFlash.getColumnModel().getColumn(3).setPreferredWidth(45);
		tblAuditFlash.getColumnModel().getColumn(4).setPreferredWidth(20);
		tblAuditFlash.getColumnModel().getColumn(5).setPreferredWidth(40);
		tblAuditFlash.getColumnModel().getColumn(6).setPreferredWidth(100);
		tblAuditFlash.getColumnModel().getColumn(7).setPreferredWidth(50);
		tblAuditFlash.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
		tblAuditFlash.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);

		DefaultTableCellRenderer rightRenderer1 = new DefaultTableCellRenderer();
		rightRenderer1.setHorizontalAlignment(JLabel.RIGHT);
		tblTotal.getColumnModel().getColumn(1).setCellRenderer(rightRenderer1);
		tblTotal.getColumnModel().getColumn(2).setCellRenderer(rightRenderer1);
		tblTotal.getColumnModel().getColumn(3).setCellRenderer(rightRenderer1);
		tblTotal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tblTotal.getColumnModel().getColumn(0).setPreferredWidth(310);
		tblTotal.getColumnModel().getColumn(1).setPreferredWidth(60);
		tblTotal.getColumnModel().getColumn(2).setPreferredWidth(80);
		tblTotal.getColumnModel().getColumn(3).setPreferredWidth(330);
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	finally
	{
	    sbSql = null;
	}
    }

    /**
     * Export File
     *
     * @param table
     * @param file
     */
    void ExportFile(JTable table, File file)
    {
	try
	{
	    WritableWorkbook workbook1 = Workbook.createWorkbook(file);
	    WritableSheet sheet1 = workbook1.createSheet("First Sheet", 0);
	    TableModel model = table.getModel();
	    sheet1.addCell(new Label(3, 0, "Audit Flash Report "));
	    //sheet1.addCell(new Label(0,2,"gsdfg "));
	    for (int i = 0; i < model.getColumnCount() + 1; i++)
	    {
		Label column = new Label(i, 1, model.getColumnName(i));
		sheet1.addCell(column);
	    }
	    int i = 0, j = 0;
	    int k = 0;
	    //System.out.println(model.getRowCount());
	    for (i = 1; i < model.getRowCount() + 1; i++)
	    {
		for (j = 0; j < model.getColumnCount(); j++)
		{
		    String value = "";
		    if (model.getValueAt(k, j) == null)
		    {
			value = "";
		    }
		    else
		    {
			value = model.getValueAt(k, j).toString();
		    }
		    Label row = new Label(j, i + 1, value);
		    sheet1.addCell(row);
		}
		k++;
	    }
	    addLastOfExportReport(workbook1);
	    workbook1.write();
	    workbook1.close();
	    Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + ExportReportPath + "/" + exportFormName + objUtility.funGetDateInString() + ".xls");
	    //sendMail();
	}
	catch (FileNotFoundException ex)
	{
	    JOptionPane.showMessageDialog(this, "<html>Please check file export path. or <br>Please close open files.</html>");
	    ex.printStackTrace();
	}
	catch (Exception ex)
	{
	    ex.printStackTrace();
	}
    }

    /**
     *
     * Add last Export File Modifed Bill Voided Bill Line Void Void Kot
     *
     * @param workbook1
     */
    private void addLastOfExportReport(WritableWorkbook workbook1)
    {
	try
	{
	    int i = 0, j = 0, LastIndexReport = 0;
	    if (exportFormName.equals("Modifed Bill"))
	    {
		LastIndexReport = 4;
	    }
	    else if (exportFormName.equals("Voided Bill"))
	    {
		LastIndexReport = 3;
	    }
	    else if (exportFormName.equals("Line Void"))
	    {
		LastIndexReport = 3;
	    }
	    else if (exportFormName.equals("Void Kot"))
	    {
		if (cmbType.getSelectedItem().toString().equals("Detail"))
		{
		    LastIndexReport = 4;
		}
		else
		{
		    LastIndexReport = 3;
		}
	    }
	    else if (exportFormName.equals("Moved Kot"))
	    {
		if (cmbType.getSelectedItem().toString().equals("Detail"))
		{
		    LastIndexReport = 4;
		}
		else
		{
		    LastIndexReport = 3;
		}
	    }
	    else if (exportFormName.trim().equalsIgnoreCase("Voided Advance Order Bill"))
	    {
		LastIndexReport = 3;
	    }

	    WritableSheet sheet2 = workbook1.getSheet(0);
	    int r = sheet2.getRows();
	    System.out.println("Total Row\t" + r);
	    System.out.println("tblTotal.getRowCount== " + tblTotal.getRowCount() + "");
	    for (i = r; i < tblTotal.getRowCount() + r; i++)
	    {
		for (j = 0; j < tblTotal.getColumnCount(); j++)
		{
		    System.out.println("j=" + j + "\t(LastIndexReport+j)=\t" + (LastIndexReport + j) + "\n" + "i+1\t" + (i + 1) + "\n" + "tblTotal.getValueAt(0, " + j + ")\t" + tblTotal.getValueAt(0, j).toString());
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
	catch (IndexOutOfBoundsException | WriteException e)
	{
	    e.printStackTrace();
	}
    }

    /**
     * This Function is used To Calculate time difference
     *
     * @param fromDate
     * @param toDate
     * @return
     */
    private String funTimeDiff(String fromDate, String toDate)
    {
	String time = "";
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	Date d1 = null;
	Date d2 = null;

	Date dt = new Date();

	try
	{
	    fromDate = fromDate.split("-")[2] + "-" + fromDate.split("-")[1] + "-" + fromDate.split("-")[0];
	    toDate = toDate.split("-")[2] + "-" + toDate.split("-")[1] + "-" + toDate.split("-")[0];

	    fromDate += " " + dt.getHours() + ":" + dt.getMinutes() + ":" + dt.getSeconds();
	    toDate += " " + dt.getHours() + ":" + dt.getMinutes() + ":" + dt.getSeconds();

	    d1 = format.parse(fromDate);
	    d2 = format.parse(toDate);
	    long diff = d2.getTime() - d1.getTime();
	    long diffSeconds = diff / 1000 % 60;
	    long diffMinutes = diff / (60 * 1000) % 60;
	    long diffHours = diff / (60 * 60 * 1000) % 24;
	    long diffDays = diff / (24 * 60 * 60 * 1000);

	    time = diffHours + ":" + diffMinutes + ":" + diffSeconds;

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	finally
	{
	    return time;
	}
    }

    /**
     * This Function is used TO File Writing Purpose
     *
     * @throws DocumentException
     * @throws FileNotFoundException
     */
    public void Expoer() throws DocumentException, FileNotFoundException
    {
	Document document = new Document();
	PdfWriter writer;
	writer = PdfWriter.getInstance(document, new FileOutputStream("c:\\my_jtable_shapes.pdf"));
	document.open();
	PdfPTable tab = new PdfPTable(2);
	PdfContentByte cb = writer.getDirectContent();
	PdfTemplate tp = cb.createTemplate(500, 500);
	Graphics2D g2;
	g2 = tp.createGraphicsShapes(500, 500);
	tblAuditFlash.print(g2);
	g2.dispose();
	cb.addTemplate(tp, 30, 300);
	document.add(tab);
	document.close();
    }

    private void funTableRowClicked(java.awt.event.MouseEvent evt)
    {
	if (reportName.equals("Bill"))
	{
	    int row = tblAuditFlash.getSelectedRow();
	    String posCode = tblAuditFlash.getValueAt(row, 0).toString();
	    String billNo = tblAuditFlash.getValueAt(row, 2).toString();
	    String billDate = tblAuditFlash.getValueAt(row, 3).toString();
	    billDate = billDate.split("-")[2] + "-" + billDate.split("-")[1] + "-" + billDate.split("-")[0];
	    try
	    {
		if (evt.getClickCount() == 2)
		{

//                    clsUtility objUtility = new clsUtility();
//                    objUtility.funPrintBill(billNo, "Void", billDate, posCode, "print");
//                    
		    clsUtility2 objUtility2 = new clsUtility2();
		    objUtility2.funPrintBillForAuditing(billNo, "", "Void", billDate, posCode, "print");

		}
	    }
	    catch (Exception e)
	    {
		e.printStackTrace();
	    }
	    finally
	    {
	    }
	}
	else if (reportName.equals("KOT"))
	{
	    int row = tblAuditFlash.getSelectedRow();
	    String KOTNo = tblAuditFlash.getValueAt(row, 3).toString();

	    try
	    {
		if (evt.getClickCount() == 2)
		{
		    sb.setLength(0);
		    sb.append("select a.strTableNo,c.strCostCenterCode "
			    + "from tblvoidkot a,tblmenuitempricingdtl b,tblcostcentermaster c "
			    + "where a.strItemCode=b.strItemCode and b.strCostCenterCode=c.strCostCenterCode "
			    + "and a.strKOTNo='" + KOTNo + "' "
			    + "and b.strPOSCode='" + clsGlobalVarClass.gPOSCode + "' "
			    + "group by c.strCostCenterCode");
		    ResultSet rsPrint = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
		    if (rsPrint.next())
		    {
			funPrintVoidKOT(KOTNo, rsPrint.getString(1), rsPrint.getString(2));
		    }
		    rsPrint.close();
		}
	    }
	    catch (Exception e)
	    {
		e.printStackTrace();
	    }
	    finally
	    {
	    }
	}
    }

    /**
     * get Selected Pos Code
     *
     * @return
     * @throws Exception
     */
    private String funGetSelectedPosCode() throws Exception
    {
	String pos = null;

	String posCode = cmbPosCode.getSelectedItem().toString();
	if (posCode.equalsIgnoreCase("Multiple"))
	{
	    pos = posCode;
	}
	else
	{
	    StringBuilder sb = new StringBuilder(posCode);
	    int len = posCode.length();
	    int lastInd = sb.lastIndexOf(" ");
	    pos = sb.substring(lastInd + 1, len).toString();
	}

	return pos;
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
        };  ;
        pnlMain = new javax.swing.JPanel();
        pnlsalesGrid = new javax.swing.JScrollPane();
        tblAuditFlash = new javax.swing.JTable();
        btnModifiedBill = new javax.swing.JButton();
        btnVoidedBill = new javax.swing.JButton();
        btnLineVoid = new javax.swing.JButton();
        lblposCode = new javax.swing.JLabel();
        cmbPosCode = new javax.swing.JComboBox();
        lblFromDate = new javax.swing.JLabel();
        dteFromDate = new com.toedter.calendar.JDateChooser();
        lblToDate = new javax.swing.JLabel();
        dteToDate = new com.toedter.calendar.JDateChooser();
        pnlGridHeader = new javax.swing.JScrollPane();
        tblTotal = new javax.swing.JTable();
        lbluser = new javax.swing.JLabel();
        cmbUser = new javax.swing.JComboBox();
        btnclose = new javax.swing.JButton();
        cmbType = new javax.swing.JComboBox();
        lbltype = new javax.swing.JLabel();
        btnVoidKot = new javax.swing.JButton();
        lblReason = new javax.swing.JLabel();
        cmbReason = new javax.swing.JComboBox();
        btnExport = new javax.swing.JButton();
        btnTimeAudit = new javax.swing.JButton();
        btnKOTAnalysis = new javax.swing.JButton();
        btnVoidedAdvOrder = new javax.swing.JButton();
        btnMovedKot = new javax.swing.JButton();
        lblSorting = new javax.swing.JLabel();
        cmbSorting = new javax.swing.JComboBox();
        cmbSorting1 = new javax.swing.JComboBox();
        btnNext = new javax.swing.JButton();
        btnPrevious = new javax.swing.JButton();

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
        lblfromName.setText("-Audit Flash");
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
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                lblHOSignMouseClicked(evt);
            }
        });
        pnlheader.add(lblHOSign);

        getContentPane().add(pnlheader, java.awt.BorderLayout.PAGE_START);

        pnlBackGround.setOpaque(false);
        pnlBackGround.setLayout(new java.awt.GridBagLayout());

        pnlMain.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, new java.awt.Color(204, 204, 204), new java.awt.Color(204, 204, 204), new java.awt.Color(153, 153, 153), new java.awt.Color(153, 153, 153)));
        pnlMain.setMinimumSize(new java.awt.Dimension(800, 570));
        pnlMain.setOpaque(false);

        tblAuditFlash.setModel(new javax.swing.table.DefaultTableModel(
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
        tblAuditFlash.setRowHeight(30);
        tblAuditFlash.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                tblAuditFlashMouseClicked(evt);
            }
        });
        pnlsalesGrid.setViewportView(tblAuditFlash);

        btnModifiedBill.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnModifiedBill.setForeground(new java.awt.Color(255, 255, 255));
        btnModifiedBill.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnModifiedBill.setText("<html>Modified <br>Bill</html>");
        btnModifiedBill.setToolTipText("View Modified Bill");
        btnModifiedBill.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnModifiedBill.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn2.png"))); // NOI18N
        btnModifiedBill.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnModifiedBillActionPerformed(evt);
            }
        });

        btnVoidedBill.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnVoidedBill.setForeground(new java.awt.Color(255, 255, 255));
        btnVoidedBill.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnVoidedBill.setText("<html>Voided <br>Bill</html>");
        btnVoidedBill.setToolTipText("view Voided Bill");
        btnVoidedBill.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnVoidedBill.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn2.png"))); // NOI18N
        btnVoidedBill.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnVoidedBillMouseClicked(evt);
            }
        });

        btnLineVoid.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnLineVoid.setForeground(new java.awt.Color(255, 255, 255));
        btnLineVoid.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnLineVoid.setText("<html>Line<br>Voids</html>");
        btnLineVoid.setToolTipText("view Line Voids");
        btnLineVoid.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnLineVoid.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn2.png"))); // NOI18N
        btnLineVoid.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnLineVoidMouseClicked(evt);
            }
        });
        btnLineVoid.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnLineVoidActionPerformed(evt);
            }
        });

        lblposCode.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblposCode.setText("POS Name");

        cmbPosCode.setToolTipText("Select  POS");
        cmbPosCode.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                cmbPosCodeActionPerformed(evt);
            }
        });

        lblFromDate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblFromDate.setText("From Date");

        dteFromDate.setToolTipText("Select From Date");
        dteFromDate.setPreferredSize(new java.awt.Dimension(119, 35));

        lblToDate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblToDate.setText("To Date");

        dteToDate.setToolTipText("Select To Date");
        dteToDate.setPreferredSize(new java.awt.Dimension(119, 35));

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
        pnlGridHeader.setViewportView(tblTotal);

        lbluser.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lbluser.setText("User");

        cmbUser.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        cmbUser.setToolTipText("Select User Name");

        btnclose.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnclose.setForeground(new java.awt.Color(255, 255, 255));
        btnclose.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnclose.setText("Close");
        btnclose.setToolTipText("Close Window");
        btnclose.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnclose.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn2.png"))); // NOI18N
        btnclose.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btncloseMouseClicked(evt);
            }
        });
        btnclose.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btncloseActionPerformed(evt);
            }
        });

        cmbType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Summary", "Detail" }));
        cmbType.setToolTipText("Select  View Type");

        lbltype.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lbltype.setText("Type");

        btnVoidKot.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnVoidKot.setForeground(new java.awt.Color(255, 255, 255));
        btnVoidKot.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnVoidKot.setText("<html>Voided<br>KOT </html>");
        btnVoidKot.setToolTipText("View Voided KOT");
        btnVoidKot.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnVoidKot.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn2.png"))); // NOI18N
        btnVoidKot.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnVoidKotMouseClicked(evt);
            }
        });

        lblReason.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblReason.setText("Reason");

        cmbReason.setToolTipText("Select Reason");

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
        btnExport.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnExportActionPerformed(evt);
            }
        });

        btnTimeAudit.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnTimeAudit.setForeground(new java.awt.Color(255, 255, 255));
        btnTimeAudit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnTimeAudit.setText("<html>Time<br>Audit</html>");
        btnTimeAudit.setToolTipText("View Time Audit");
        btnTimeAudit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnTimeAudit.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn2.png"))); // NOI18N
        btnTimeAudit.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnTimeAuditMouseClicked(evt);
            }
        });
        btnTimeAudit.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnTimeAuditActionPerformed(evt);
            }
        });

        btnKOTAnalysis.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnKOTAnalysis.setForeground(new java.awt.Color(255, 255, 255));
        btnKOTAnalysis.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnKOTAnalysis.setText("<html>KOT<br>Analysis</html>");
        btnKOTAnalysis.setToolTipText("View Kot Analysis");
        btnKOTAnalysis.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnKOTAnalysis.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn2.png"))); // NOI18N
        btnKOTAnalysis.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnKOTAnalysisMouseClicked(evt);
            }
        });
        btnKOTAnalysis.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnKOTAnalysisActionPerformed(evt);
            }
        });

        btnVoidedAdvOrder.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnVoidedAdvOrder.setForeground(new java.awt.Color(255, 255, 255));
        btnVoidedAdvOrder.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnVoidedAdvOrder.setText("<html>Voided<br>Advance<br>Order</html>");
        btnVoidedAdvOrder.setToolTipText("view Voided Bill");
        btnVoidedAdvOrder.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnVoidedAdvOrder.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn2.png"))); // NOI18N
        btnVoidedAdvOrder.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnVoidedAdvOrderMouseClicked(evt);
            }
        });

        btnMovedKot.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnMovedKot.setForeground(new java.awt.Color(255, 255, 255));
        btnMovedKot.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnMovedKot.setText("<html>Moved<br>KOT </html>");
        btnMovedKot.setToolTipText("View Voided KOT");
        btnMovedKot.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnMovedKot.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn2.png"))); // NOI18N
        btnMovedKot.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnMovedKotMouseClicked(evt);
            }
        });
        btnMovedKot.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnMovedKotActionPerformed(evt);
            }
        });

        lblSorting.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblSorting.setText("Sort");

        cmbSorting.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "BILL", "AMOUNT" }));
        cmbSorting.setToolTipText("Select Reason");

        cmbSorting1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ALL", "Full VOID", "ITEM VOID" }));
        cmbSorting1.setToolTipText("Select Reason");

        btnNext.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        btnNext.setForeground(new java.awt.Color(255, 255, 255));
        btnNext.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCommonBtnLong1.png"))); // NOI18N
        btnNext.setText(">>");
        btnNext.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnNext.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCommonBtnLong2.png"))); // NOI18N
        btnNext.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnNextMouseClicked(evt);
            }
        });

        btnPrevious.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        btnPrevious.setForeground(new java.awt.Color(255, 255, 255));
        btnPrevious.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCommonBtnLong1.png"))); // NOI18N
        btnPrevious.setText("<<");
        btnPrevious.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnPrevious.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCommonBtnLong2.png"))); // NOI18N
        btnPrevious.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnPreviousMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout pnlMainLayout = new javax.swing.GroupLayout(pnlMain);
        pnlMain.setLayout(pnlMainLayout);
        pnlMainLayout.setHorizontalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMainLayout.createSequentialGroup()
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addComponent(btnPrevious, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnModifiedBill, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnVoidedBill, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnVoidedAdvOrder, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnLineVoid, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnVoidKot, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnTimeAudit, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnKOTAnalysis, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnMovedKot, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnNext, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnclose, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(pnlGridHeader, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(pnlMainLayout.createSequentialGroup()
                                .addComponent(lbltype, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cmbSorting1, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblReason, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cmbReason, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(cmbType, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(lblSorting, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(cmbSorting, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(40, 40, 40)
                                .addComponent(btnExport, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(pnlsalesGrid))
                        .addGap(1, 1, 1)))
                .addGap(3, 3, 3))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMainLayout.createSequentialGroup()
                .addComponent(lblposCode)
                .addGap(2, 2, 2)
                .addComponent(cmbPosCode, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(lbluser, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(cmbUser, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblFromDate)
                .addGap(3, 3, 3)
                .addComponent(dteFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addGap(50, 50, 50)
                        .addComponent(dteToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        pnlMainLayout.setVerticalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMainLayout.createSequentialGroup()
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblposCode, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbPosCode, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbluser, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbUser, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dteFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(lblToDate, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(dteToDate, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lblReason, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lbltype, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(cmbSorting1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(cmbReason, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(cmbType, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(cmbSorting, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblSorting, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnExport, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlsalesGrid, javax.swing.GroupLayout.DEFAULT_SIZE, 366, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlGridHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnModifiedBill, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnVoidedBill, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnKOTAnalysis, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnVoidKot, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnLineVoid, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnTimeAudit, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnMovedKot, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnclose, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnNext, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnPrevious, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnVoidedAdvOrder, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );

        pnlBackGround.add(pnlMain, new java.awt.GridBagConstraints());

        getContentPane().add(pnlBackGround, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents
     /**
     * this Function is used for Call Modified Bill
     *
     * @return
     */
    private void btnModifiedBillActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnModifiedBillActionPerformed
	// TODO add your handling code here:
	funButtonAction(btnModifiedBill);
    }//GEN-LAST:event_btnModifiedBillActionPerformed

    /**
     * this Function is used for Call Voided Bill
     *
     * @return
     */
    private void btnVoidedBillMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnVoidedBillMouseClicked
	// TODO add your handling code here:
	funVoidedBillFlash();
    }//GEN-LAST:event_btnVoidedBillMouseClicked
    /**
     * this Function is used for get Line Void Bill
     *
     * @return
     */
    private void btnLineVoidMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnLineVoidMouseClicked
	// TODO add your handling code here:
	funLineVoidFlash();
    }//GEN-LAST:event_btnLineVoidMouseClicked

    private void btnLineVoidActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLineVoidActionPerformed
	// TODO add your handling code here:
    }//GEN-LAST:event_btnLineVoidActionPerformed
    /**
     * this Function is used for get Close Window
     *
     * @return
     */
    private void btncloseMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btncloseMouseClicked
	funResetLookAndFeel();
	dispose();
	clsGlobalVarClass.hmActiveForms.remove("Audit Flash");
    }//GEN-LAST:event_btncloseMouseClicked
    /**
     * this Function is used for Void Kot
     *
     * @return
     */
    private void btnVoidKotMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnVoidKotMouseClicked
	// TODO add your handling code here:
	funVoidKOTFlash();
    }//GEN-LAST:event_btnVoidKotMouseClicked
    /**
     * this Function is used for get Export Bill
     *
     * @return
     */
    private void btnExportMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnExportMouseClicked
	try
	{

	    File theDir = new File(ExportReportPath);
	    File file = new File(ExportReportPath + File.separator + exportFormName + objUtility.funGetDateInString() + ".xls");
	    // if the directory does not exist, create it
	    if (!theDir.exists())
	    {
		theDir.mkdir();
		ExportFile(tblAuditFlash, file);
	    }
	    else
	    {
		ExportFile(tblAuditFlash, file);
	    }
	}
	catch (Exception ex)
	{
	    Logger.getLogger(frmAuditFlash.class.getName()).log(Level.SEVERE, null, ex);
	}
    }//GEN-LAST:event_btnExportMouseClicked

    private void btnExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportActionPerformed
	// TODO add your handling code here:
    }//GEN-LAST:event_btnExportActionPerformed
    /**
     * this Function is used for Time Audit Report
     *
     * @return
     */
    private void btnTimeAuditMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnTimeAuditMouseClicked
	// TODO add your handling code here:
	funTimeAuditReport();

    }//GEN-LAST:event_btnTimeAuditMouseClicked

    private void btnKOTAnalysisMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnKOTAnalysisMouseClicked
	// TODO add your handling code here:
    }//GEN-LAST:event_btnKOTAnalysisMouseClicked

    /**
     * this Function is used for Kot Wise Bill
     *
     * @return
     */
    private void btnKOTAnalysisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKOTAnalysisActionPerformed
	// TODO add your handling code here:
	funKOTWise();
    }//GEN-LAST:event_btnKOTAnalysisActionPerformed

    private void btnVoidedAdvOrderMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnVoidedAdvOrderMouseClicked

	funVoidedAdvanceOrderBill();
    }//GEN-LAST:event_btnVoidedAdvOrderMouseClicked

    private void tblAuditFlashMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblAuditFlashMouseClicked
	// TODO add your handling code here:
	funTableRowClicked(evt);
    }//GEN-LAST:event_tblAuditFlashMouseClicked

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

    private void btncloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btncloseActionPerformed
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("Audit Flash");
    }//GEN-LAST:event_btncloseActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("Audit Flash");
    }//GEN-LAST:event_formWindowClosed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("Audit Flash");
    }//GEN-LAST:event_formWindowClosing

    private void btnMovedKotMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnMovedKotMouseClicked
	// TODO add your handling code here:
	funMovedKOTFlash();
    }//GEN-LAST:event_btnMovedKotMouseClicked

    private void btnMovedKotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMovedKotActionPerformed
	// TODO add your handling code here:
    }//GEN-LAST:event_btnMovedKotActionPerformed

    private void cmbPosCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbPosCodeActionPerformed
	// TODO add your handling code here:

	try
	{
	    String posCode = funGetSelectedPosCode();

	    if (posCode.equalsIgnoreCase("Multiple"))
	    {
		frmMultiPOSSelection objMultiPOSSelection = new frmMultiPOSSelection(this);
		selectedPOSCodeSet = objMultiPOSSelection.funGetSelectedPOSCode();

	    }
	    else
	    {
		selectedPOSCodeSet.clear();
		selectedPOSCodeSet.add(posCode);
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}

    }//GEN-LAST:event_cmbPosCodeActionPerformed

    private void btnTimeAuditActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnTimeAuditActionPerformed
    {//GEN-HEADEREND:event_btnTimeAuditActionPerformed
	// TODO add your handling code here:
    }//GEN-LAST:event_btnTimeAuditActionPerformed

    private void btnPreviousMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_btnPreviousMouseClicked
    {//GEN-HEADEREND:event_btnPreviousMouseClicked
	funPreviousButtonClicked();
    }//GEN-LAST:event_btnPreviousMouseClicked

    private void btnNextMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_btnNextMouseClicked
    {//GEN-HEADEREND:event_btnNextMouseClicked
	funNextButtonClicked();
    }//GEN-LAST:event_btnNextMouseClicked

    /**
     * method Print Bill
     *
     * @param billno
     * @param poscode
     */
    private void funPrintVoidKOT(String KOTNo, String tableNo, String costCenterCode)
    {
	try
	{

	    clsVoidKOTAuditingGenerator objVoidKOTAuditingGenerator = new clsVoidKOTAuditingGenerator();
	    objVoidKOTAuditingGenerator.funGenerateVoidKOT(tableNo, KOTNo, "Reprint", costCenterCode, null);

	}
	catch (Exception ex)
	{
	    StackTraceElement[] st = ex.getStackTrace();
	    ex.printStackTrace();
	}
	finally
	{
	}
    }

    /**
     * method Print Bill
     *
     * @param billno
     * @param poscode
     */
    /*
     * private void funPrintBill(String billno,String poscode, String billDate)
     * { try { clsTextFileGeneratorForPrinting obj = new
     * clsTextFileGeneratorForPrinting();
     * if(clsGlobalVarClass.gBillFormatType.equalsIgnoreCase("Format 1")) {
     * obj.funGenerateTextFileBillPrinting(billno,"reprint","sales
     * report","sale",billDate,"abc"); } else
     * if(clsGlobalVarClass.gBillFormatType.equalsIgnoreCase("Format 2")) {
     * obj.funGenerateTextFileBillPrintingForFormat2(billno,"reprint","sales
     * report","sale",billDate); } else
     * if(clsGlobalVarClass.gBillFormatType.equalsIgnoreCase("Format 3")) {
     * obj.funGenerateTextFileBillPrintingForFormat3(billno,"reprint","sales
     * report","sale",billDate); } else
     * if(clsGlobalVarClass.gBillFormatType.equalsIgnoreCase("Format 4")) {
     * obj.funGenerateTextFileBillPrintingForFormat4(billno,"reprint","sales
     * report","sale",billDate); } else
     * if(clsGlobalVarClass.gBillFormatType.equalsIgnoreCase("Format 5")) {
     * obj.funGenerateTextFileBillPrintingForFormat5(billno,"reprint","sales
     * report","sale",billDate); } else
     * if(clsGlobalVarClass.gBillFormatType.equalsIgnoreCase("Format 6")) {
     * obj.funGenerateTextFileBillPrintingForFormat6(billno,"reprint","sales
     * report","sale",billDate); } } catch (Exception ex) { StackTraceElement[]
     * st = ex.getStackTrace(); ex.printStackTrace(); } finally { } }
     */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnExport;
    private javax.swing.JButton btnKOTAnalysis;
    private javax.swing.JButton btnLineVoid;
    private javax.swing.JButton btnModifiedBill;
    private javax.swing.JButton btnMovedKot;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnPrevious;
    private javax.swing.JButton btnTimeAudit;
    private javax.swing.JButton btnVoidKot;
    private javax.swing.JButton btnVoidedAdvOrder;
    private javax.swing.JButton btnVoidedBill;
    private javax.swing.JButton btnclose;
    private javax.swing.JComboBox cmbPosCode;
    private javax.swing.JComboBox cmbReason;
    private javax.swing.JComboBox cmbSorting;
    private javax.swing.JComboBox cmbSorting1;
    private javax.swing.JComboBox cmbType;
    private javax.swing.JComboBox cmbUser;
    private com.toedter.calendar.JDateChooser dteFromDate;
    private com.toedter.calendar.JDateChooser dteToDate;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblFromDate;
    private javax.swing.JLabel lblHOSign;
    private javax.swing.JLabel lblModuleName;
    private javax.swing.JLabel lblPosName;
    private javax.swing.JLabel lblProductName;
    private javax.swing.JLabel lblReason;
    private javax.swing.JLabel lblSorting;
    private javax.swing.JLabel lblToDate;
    private javax.swing.JLabel lblUserCode;
    private javax.swing.JLabel lblfromName;
    private javax.swing.JLabel lblposCode;
    private javax.swing.JLabel lbltype;
    private javax.swing.JLabel lbluser;
    private javax.swing.JPanel pnlBackGround;
    private javax.swing.JScrollPane pnlGridHeader;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JPanel pnlheader;
    private javax.swing.JScrollPane pnlsalesGrid;
    private javax.swing.JTable tblAuditFlash;
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
	    java.util.logging.Logger.getLogger(frmAuditFlash.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	}
	catch (InstantiationException ex)
	{
	    java.util.logging.Logger.getLogger(frmAuditFlash.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	}
	catch (IllegalAccessException ex)
	{
	    java.util.logging.Logger.getLogger(frmAuditFlash.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	}
	catch (javax.swing.UnsupportedLookAndFeelException ex)
	{
	    java.util.logging.Logger.getLogger(frmAuditFlash.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	}
    }

    private void funDetailKOTAnalysisOld()
    {
	StringBuilder sbSqlLive = new StringBuilder();
	StringBuilder sbSqlQFile = new StringBuilder();
	StringBuilder sbFilters = new StringBuilder();
	try
	{
	    tblTotal.setVisible(true);
	    exportFormName = "KOTAnalysis";
	    double totalQuantity = 0.0;
	    fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());

	    toDate = objUtility.funGetFromToDate(dteToDate.getDate());

	    DefaultTableModel dmTableColoum = new DefaultTableModel()
	    {
		@Override
		public boolean isCellEditable(int row, int column)
		{
		    //all cells false
		    return false;
		}
	    };

	    dmTableColoum.addColumn("Bill No");
	    dmTableColoum.addColumn("Date");
	    dmTableColoum.addColumn("KOT No.");
	    dmTableColoum.addColumn("Qty");
	    dmTableColoum.addColumn("Item Name");
	    dmTableColoum.addColumn("Waiter No.");
	    dmTableColoum.addColumn("Table No.");

	    DefaultTableModel dmTotal = new DefaultTableModel();
	    dmTotal.addColumn("Total");
	    dmTotal.addColumn("");

	    Object[] records = new Object[11];
	    String userCode = funGetSelectedUserCode(cmbUser.getSelectedItem().toString());
	    String POSCode = objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString().trim());

	    sbSqlLive.setLength(0);
	    sbSqlQFile.setLength(0);

	    sbSqlLive.append(" select b.strBillNo,DATE_FORMAT(date(b.dteBillDate),'%d-%m-%Y') ,b.strKOTNo, b.dblQuantity, b.strItemName,d.strWShortName"
		    + ",c.strTableName "
		    + "from tblbillhd a, tblbilldtl b, tbltablemaster c, tblwaitermaster d "
		    + "where a.strBillNo=b.strBillNo and a.strTableNo=c.strTableNo and b.strWaiterNo=d.strWaiterNo ");

	    sbSqlQFile.append(" select b.strBillNo,DATE_FORMAT(date(b.dteBillDate) ,'%d-%m-%Y'),b.strKOTNo, b.dblQuantity, b.strItemName,d.strWShortName"
		    + ",c.strTableName "
		    + "from tblqbillhd a, tblqbilldtl b, tbltablemaster c, tblwaitermaster d "
		    + "where a.strBillNo=b.strBillNo and a.strTableNo=c.strTableNo and b.strWaiterNo=d.strWaiterNo ");

	    if (!"All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()))
	    {
		sbFilters.append(" and a.strUserCreated='" + userCode + "' and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " and Date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "'");
	    }
	    else if (!"All".equals(cmbPosCode.getSelectedItem()) && "All".equals(cmbUser.getSelectedItem()))
	    {
		sbFilters.append(" and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " "
			+ "and Date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "'");
	    }
	    else if ("All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()))
	    {
		sbFilters.append(" and a.strUserCreated='" + userCode + "' "
			+ "and Date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "'");
	    }
	    else
	    {
		sbFilters.append(" and Date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "'");
	    }

	    sbSqlLive.append(sbFilters);
	    sbSqlQFile.append(sbFilters);

	    sbSqlLive.append("group by a.strBillNo,b.strKOTNo,b.strItemCode "
		    + "order by a.strBillNo,b.strKOTNo,b.strItemCode");
	    sbSqlQFile.append("group by a.strBillNo,b.strKOTNo,b.strItemCode "
		    + "order by a.strBillNo,b.strKOTNo,b.strItemCode");

	    ResultSet rsKOTAnalysis = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
	    while (rsKOTAnalysis.next())
	    {
		records[0] = rsKOTAnalysis.getString(1);
		String tempBillDate = rsKOTAnalysis.getString(2);
		String[] spDate = tempBillDate.split("-");
		records[1] = spDate[2] + "-" + spDate[1] + "-" + spDate[0];
		records[2] = rsKOTAnalysis.getString(3);
		records[3] = rsKOTAnalysis.getString(4);
		records[4] = rsKOTAnalysis.getString(5);
		records[5] = rsKOTAnalysis.getString(6);
		records[6] = rsKOTAnalysis.getString(7);
		totalQuantity += Double.parseDouble(records[3].toString());
		dmTableColoum.addRow(records);
	    }

	    rsKOTAnalysis = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFile.toString());
	    while (rsKOTAnalysis.next())
	    {
		records[0] = rsKOTAnalysis.getString(1);
		String tempBillDate = rsKOTAnalysis.getString(2);
		String[] spDate = tempBillDate.split("-");
		records[1] = spDate[0] + "-" + spDate[1] + "-" + spDate[2];
		records[2] = rsKOTAnalysis.getString(3);
		records[3] = rsKOTAnalysis.getString(4);
		records[4] = rsKOTAnalysis.getString(5);
		records[5] = rsKOTAnalysis.getString(6);
		records[6] = rsKOTAnalysis.getString(7);
		totalQuantity += Double.parseDouble(records[3].toString());
		dmTableColoum.addRow(records);
	    }

	    Object[] arrObjTotals =
	    {
		"Total", totalQuantity
	    };
	    dmTotal.addRow(arrObjTotals);

	    tblTotal.setModel(dmTotal);
	    tblAuditFlash.setModel(dmTableColoum);
	    tblAuditFlash.setRowHeight(25);
	    tblTotal.setRowHeight(25);
	    DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
	    rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
	    DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
	    centerRenderer.setHorizontalAlignment(JLabel.CENTER);
	    tblAuditFlash.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
	    tblAuditFlash.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);

	    tblAuditFlash.getColumnModel().getColumn(0).setPreferredWidth(50);
	    tblAuditFlash.getColumnModel().getColumn(1).setPreferredWidth(50);
	    tblAuditFlash.getColumnModel().getColumn(2).setPreferredWidth(50);
	    tblAuditFlash.getColumnModel().getColumn(3).setPreferredWidth(50);
	    tblAuditFlash.getColumnModel().getColumn(4).setPreferredWidth(180);
	    tblAuditFlash.getColumnModel().getColumn(5).setPreferredWidth(30);
	    tblAuditFlash.getColumnModel().getColumn(6).setPreferredWidth(30);

	    tblTotal.setSize(400, 400);
	    DefaultTableCellRenderer rightRenderer1 = new DefaultTableCellRenderer();
	    rightRenderer1.setHorizontalAlignment(JLabel.RIGHT);
	    tblTotal.getColumnModel().getColumn(1).setCellRenderer(rightRenderer1);

	    tblTotal.getColumnModel().getColumn(0).setPreferredWidth(400);
	    tblTotal.getColumnModel().getColumn(1).setPreferredWidth(70);

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	finally
	{
	    sbSqlLive = null;
	    sbSqlQFile = null;
	    sbFilters = null;
	}
    }

    private void funSummaryKOTAnalysis()
    {
	StringBuilder sbSqlLive = new StringBuilder();
	StringBuilder sbSqlQFile = new StringBuilder();
	StringBuilder sbFilters = new StringBuilder();
	try
	{
	    tblTotal.setVisible(true);
	    exportFormName = "Summary KOT Analysis";
	    reportName = "Summary KOT Analysis";;
	    double totalQuantity = 0.0;
	    fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());

	    toDate = objUtility.funGetFromToDate(dteToDate.getDate());

	    DefaultTableModel dmTableColoum = new DefaultTableModel()
	    {
		@Override
		public boolean isCellEditable(int row, int column)
		{
		    //all cells false
		    return false;
		}
	    };

	    dmTableColoum.addColumn("KOT");
	    dmTableColoum.addColumn("Opearation");
	    dmTableColoum.addColumn("Date");
	    dmTableColoum.addColumn("Time");
	    dmTableColoum.addColumn("Bill No.");
	    dmTableColoum.addColumn("Table");
	    dmTableColoum.addColumn("Waiter");
	    dmTableColoum.addColumn("Reason");
	    dmTableColoum.addColumn("Remarks");

	    DefaultTableModel dmTotal = new DefaultTableModel()
	    {
		@Override
		public boolean isCellEditable(int row, int column)
		{
		    //all cells false
		    return false;
		}
	    };
	    dmTotal.addColumn("");
	    dmTotal.addColumn("");
	    dmTotal.addColumn("");
	    dmTotal.addColumn("");
	    dmTotal.addColumn("");
	    dmTotal.addColumn("");
	    dmTotal.addColumn("");
	    dmTotal.addColumn("");
	    dmTotal.addColumn("");

	    Object[] records = new Object[11];
	    String userCode = funGetSelectedUserCode(cmbUser.getSelectedItem().toString());
	    String reasonCode = funGetSelectedReasonCode(cmbReason.getSelectedItem().toString());
	    String posCode = objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString().trim());

	    int noOfKOTs = 0;
	    List<clsKOTAnalysisBean> listOfKOTAnalysis = new LinkedList<clsKOTAnalysisBean>();

	    sbSqlLive.setLength(0);
	    sbSqlQFile.setLength(0);
	    sbFilters.setLength(0);
	    String operation = "Billed KOT";

	    if (!"All".equals(cmbPosCode.getSelectedItem().toString()))
	    {
		sbFilters.append("and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " ");
	    }
	    if (!"All".equals(cmbUser.getSelectedItem().toString()))
	    {
		sbFilters.append(" and a.strUserCreated='" + userCode + "' ");
	    }
	    if (cmbReason.getSelectedItem().toString() != "All")
	    {
		sbFilters.append(" and a.strReasonCode='" + reasonCode + "' ");
	    }

	    //live billed KOTs
	    sbSqlLive.append("select if(b.strKOTNo='','DirectBiller',b.strKOTNo)strKOTNo "
		    + ",DATE_FORMAT(date(b.dteBillDate),'%d-%m-%Y') dteKOTDate,TIME_FORMAT(time(b.dteBillDate),'%h:%i')tmeKOTTime "
		    + ",a.strBillNo,a.strTableNo,c.strTableName,b.strWaiterNo,if(d.strWShortName='','ShortName',d.strWShortName)strWShortName "
		    + "from tblbillhd a,tblbilldtl b,tbltablemaster c,tblwaitermaster d "
		    + "where a.strBillNo=b.strBillNo  "
		    + "and date(a.dteBillDate)=date(b.dteBillDate) "
		    + "and a.strTableNo=c.strTableNo "
		    + "and b.strWaiterNo=d.strWaiterNo "
		    + "and LENGTH(b.strKOTNo)>0 "
		    + "and Date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");

	    sbSqlLive.append(sbFilters);

	    sbSqlLive.append("group by a.strBillNo,b.strKOTNo "
		    + "order by a.strBillNo,b.strKOTNo");

	    ResultSet rsBilledKOTs = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
	    while (rsBilledKOTs.next())
	    {
		clsKOTAnalysisBean objKOTAnalysisBean = new clsKOTAnalysisBean();

		objKOTAnalysisBean.setStrKOTNo(rsBilledKOTs.getString(1));//kotNO
		objKOTAnalysisBean.setStrOperationType(operation);//operation
		objKOTAnalysisBean.setDteKOTDate(rsBilledKOTs.getString(2));//date
		objKOTAnalysisBean.setTmeKOTTime(rsBilledKOTs.getString(3));//time
		objKOTAnalysisBean.setStrBillNo(rsBilledKOTs.getString(4));//billNo
		objKOTAnalysisBean.setStrTableNo(rsBilledKOTs.getString(5));//tableNO
		objKOTAnalysisBean.setStrTableName(rsBilledKOTs.getString(6));//tableName
		objKOTAnalysisBean.setStrWaiterNo(rsBilledKOTs.getString(7));//waiterNo
		objKOTAnalysisBean.setStrWaiterName(rsBilledKOTs.getString(8));//waiterName
		objKOTAnalysisBean.setStrReasonName("");//reason
		objKOTAnalysisBean.setStrRemarks("");//remarks   

		listOfKOTAnalysis.add(objKOTAnalysisBean);
	    }
	    rsBilledKOTs.close();

	    //Q billed KOTs
	    sbSqlQFile.append("select if(b.strKOTNo='','DirectBiller',b.strKOTNo)strKOTNo "
		    + ",DATE_FORMAT(date(b.dteBillDate),'%d-%m-%Y') dteKOTDate,TIME_FORMAT(time(b.dteBillDate),'%h:%i')tmeKOTTime "
		    + ",a.strBillNo,a.strTableNo,c.strTableName,b.strWaiterNo,if(d.strWShortName='','ShortName',d.strWShortName)strWShortName "
		    + "from tblqbillhd a,tblqbilldtl b,tbltablemaster c,tblwaitermaster d "
		    + "where a.strBillNo=b.strBillNo  "
		    + "and date(a.dteBillDate)=date(b.dteBillDate) "
		    + "and a.strTableNo=c.strTableNo "
		    + "and b.strWaiterNo=d.strWaiterNo "
		    + "and LENGTH(b.strKOTNo)>0 "
		    + "and Date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "'  ");
	    sbSqlQFile.append(sbFilters);
	    sbSqlQFile.append("group by a.strBillNo,b.strKOTNo "
		    + "order by a.strBillNo,b.strKOTNo");

	    rsBilledKOTs = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFile.toString());
	    while (rsBilledKOTs.next())
	    {
		clsKOTAnalysisBean objKOTAnalysisBean = new clsKOTAnalysisBean();

		objKOTAnalysisBean.setStrKOTNo(rsBilledKOTs.getString(1));//kotNO
		objKOTAnalysisBean.setStrOperationType(operation);//operation
		objKOTAnalysisBean.setDteKOTDate(rsBilledKOTs.getString(2));//date
		objKOTAnalysisBean.setTmeKOTTime(rsBilledKOTs.getString(3));//time
		objKOTAnalysisBean.setStrBillNo(rsBilledKOTs.getString(4));//billNo
		objKOTAnalysisBean.setStrTableNo(rsBilledKOTs.getString(5));//tableNO
		objKOTAnalysisBean.setStrTableName(rsBilledKOTs.getString(6));//tableName
		objKOTAnalysisBean.setStrWaiterNo(rsBilledKOTs.getString(7));//waiterNo
		objKOTAnalysisBean.setStrWaiterName(rsBilledKOTs.getString(8));//waiterName
		objKOTAnalysisBean.setStrReasonName("");//reason
		objKOTAnalysisBean.setStrRemarks("");//remarks   

		listOfKOTAnalysis.add(objKOTAnalysisBean);
	    }
	    rsBilledKOTs.close();

	    //voided billed KOTs
	    sbSqlLive.setLength(0);
	    sbSqlQFile.setLength(0);
	    sbFilters.setLength(0);

	    if (!"All".equals(cmbPosCode.getSelectedItem().toString()))
	    {
		sbFilters.append("and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " ");
	    }
	    if (!"All".equals(cmbUser.getSelectedItem().toString()))
	    {
		sbFilters.append(" and a.strUserCreated='" + userCode + "' ");
	    }
	    if (cmbReason.getSelectedItem().toString() != "All")
	    {
		sbFilters.append(" and a.strReasonCode='" + reasonCode + "' ");
	    }
	    sbSqlLive.append("select if(b.strKOTNo='','DirectBiller',b.strKOTNo)strKOTNo,b.strTransType "
		    + ",DATE_FORMAT(date(b.dteBillDate),'%d-%m-%Y') dteKOTDate,TIME_FORMAT(time(b.dteBillDate),'%h:%i')tmeKOTTime "
		    + ",a.strBillNo,a.strTableNo,c.strTableName,b.strWaiterNo,if(d.strWShortName='','ShortName',d.strWShortName)strWShortName "
		    + ",b.strReasonName,b.strRemarks "
		    + "from tblvoidbillhd a,tblvoidbilldtl b,tbltablemaster c,tblwaitermaster d "
		    + "where a.strBillNo=b.strBillNo  "
		    + "and date(a.dteBillDate)=date(b.dteBillDate) "
		    + "and a.strTableNo=c.strTableNo "
		    + "and a.strWaiterNo=d.strWaiterNo "
		    + "and LENGTH(b.strKOTNo)>2 "
		    + "and Date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    sbSqlLive.append(sbFilters);
	    sbSqlLive.append("group by a.strBillNo,b.strKOTNo "
		    + "order by a.strBillNo,b.strKOTNo");
	    ResultSet rsVoidedBilledKOTs = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
	    while (rsVoidedBilledKOTs.next())
	    {
		clsKOTAnalysisBean objKOTAnalysisBean = new clsKOTAnalysisBean();

		operation = rsVoidedBilledKOTs.getString(2);
		if (rsVoidedBilledKOTs.getString(2).equalsIgnoreCase("VB"))
		{
		    operation = "Void Bill";
		}
		if (rsVoidedBilledKOTs.getString(2).equalsIgnoreCase("USBill"))
		{
		    operation = "Unseetled Bill";
		}
		if (rsVoidedBilledKOTs.getString(2).equalsIgnoreCase("MB"))
		{
		    operation = "Modified Bill";
		}

		objKOTAnalysisBean.setStrKOTNo(rsVoidedBilledKOTs.getString(1));//kotNO
		objKOTAnalysisBean.setStrOperationType(operation);//operation
		objKOTAnalysisBean.setDteKOTDate(rsVoidedBilledKOTs.getString(3));//date
		objKOTAnalysisBean.setTmeKOTTime(rsVoidedBilledKOTs.getString(4));//time
		objKOTAnalysisBean.setStrBillNo(rsVoidedBilledKOTs.getString(5));//billNo
		objKOTAnalysisBean.setStrTableNo(rsVoidedBilledKOTs.getString(6));//tableNO
		objKOTAnalysisBean.setStrTableName(rsVoidedBilledKOTs.getString(7));//tableName
		objKOTAnalysisBean.setStrWaiterNo(rsVoidedBilledKOTs.getString(8));//waiterNo
		objKOTAnalysisBean.setStrWaiterName(rsVoidedBilledKOTs.getString(9));//waiterName
		objKOTAnalysisBean.setStrReasonName(rsVoidedBilledKOTs.getString(10));//reason
		objKOTAnalysisBean.setStrRemarks(rsVoidedBilledKOTs.getString(11));//remarks   

		listOfKOTAnalysis.add(objKOTAnalysisBean);
	    }
	    rsVoidedBilledKOTs.close();

	    //line voided KOTs
	    sbSqlLive.setLength(0);
	    sbSqlQFile.setLength(0);
	    sbFilters.setLength(0);
	    operation = "Line Void";

	    if (!"All".equals(cmbPosCode.getSelectedItem().toString()))
	    {
		sbFilters.append("and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " ");
	    }
	    if (!"All".equals(cmbUser.getSelectedItem().toString()))
	    {
		sbFilters.append(" and a.strUserCreated='" + userCode + "' ");
	    }

	    sbSqlLive.append("select if(a.strKOTNo='','DirectBiller',a.strKOTNo)strKOTNo,'Line Void' strOperationType "
		    + ",DATE_FORMAT(date(a.dteDateCreated),'%d-%m-%Y') dteKOTDate,TIME_FORMAT(time(a.dteDateCreated),'%h:%i')tmeKOTTime "
		    + "from tbllinevoid a "
		    + "where LENGTH(a.strKOTNo)>2 "
		    + "and Date(a.dteDateCreated) between '" + fromDate + "' and '" + toDate + "' ");
	    sbSqlLive.append(sbFilters);
	    sbSqlLive.append("group by a.strKOTNo "
		    + "order by a.strKOTNo");
	    ResultSet rsLineVoidedKOTs = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
	    while (rsLineVoidedKOTs.next())
	    {
		clsKOTAnalysisBean objKOTAnalysisBean = new clsKOTAnalysisBean();

		objKOTAnalysisBean.setStrKOTNo(rsLineVoidedKOTs.getString(1));//kotNO
		objKOTAnalysisBean.setStrOperationType(operation);//operation
		objKOTAnalysisBean.setDteKOTDate(rsLineVoidedKOTs.getString(3));//date
		objKOTAnalysisBean.setTmeKOTTime(rsLineVoidedKOTs.getString(4));//time
		objKOTAnalysisBean.setStrBillNo("");//billNo
		objKOTAnalysisBean.setStrTableNo("");//tableNO
		objKOTAnalysisBean.setStrTableName("");//tableName
		objKOTAnalysisBean.setStrWaiterNo("");//waiterNo
		objKOTAnalysisBean.setStrWaiterName("");//waiterName
		objKOTAnalysisBean.setStrReasonName("");//reason
		objKOTAnalysisBean.setStrRemarks("");//remarks   

		listOfKOTAnalysis.add(objKOTAnalysisBean);
	    }
	    rsLineVoidedKOTs.close();

	    //voided KOTs
	    sbSqlLive.setLength(0);
	    sbSqlQFile.setLength(0);
	    sbFilters.setLength(0);
	    operation = "Void KOT";

	    if (!"All".equals(cmbPosCode.getSelectedItem().toString()))
	    {
		sbFilters.append("and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " ");
	    }
	    if (!"All".equals(cmbUser.getSelectedItem().toString()))
	    {
		sbFilters.append(" and a.strUserCreated='" + userCode + "' ");
	    }
	    if (cmbReason.getSelectedItem().toString() != "All")
	    {
		sbFilters.append(" and a.strReasonCode='" + reasonCode + "' ");
	    }
	    sbSqlLive.append("select if(a.strKOTNo='','DirectBiller',a.strKOTNo)strKOTNo,a.strType strOperationType "
		    + ",DATE_FORMAT(date(a.dteDateCreated),'%d-%m-%Y') dteKOTDate,TIME_FORMAT(time(a.dteDateCreated),'%h:%i')tmeKOTTime "
		    + ",b.strTableName,c.strWShortName,d.strReasonName,a.strRemark "
		    + "from tblvoidkot a,tbltablemaster b,tblwaitermaster c,tblreasonmaster d "
		    + "where a.strTableNo=b.strTableNo  "
		    + "and a.strWaiterNo=c.strWaiterNo "
		    + "and a.strReasonCode=d.strReasonCode "
		    + "and LENGTH(a.strKOTNo)>2 "
		    + "and Date(a.dteDateCreated) between '" + fromDate + "' and '" + toDate + "' ");
	    sbSqlLive.append(sbFilters);
	    sbSqlLive.append("group by a.strKOTNo,a.strType "
		    + "order by a.strKOTNo");
	    ResultSet rsVoidedKOT = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
	    while (rsVoidedKOT.next())
	    {
		clsKOTAnalysisBean objKOTAnalysisBean = new clsKOTAnalysisBean();

		objKOTAnalysisBean.setStrKOTNo(rsVoidedKOT.getString(1));//kotNO
		if (rsVoidedKOT.getString(2).equalsIgnoreCase("VKot"))
		{
		    operation = "Void KOT";
		}
		else if (rsVoidedKOT.getString(2).equalsIgnoreCase("MVKot"))
		{
		    operation = "Move KOT";
		}
		else
		{
		    operation = "Void KOT";
		}
		objKOTAnalysisBean.setStrOperationType(operation);//operation
		objKOTAnalysisBean.setDteKOTDate(rsVoidedKOT.getString(3));//date
		objKOTAnalysisBean.setTmeKOTTime(rsVoidedKOT.getString(4));//time
		objKOTAnalysisBean.setStrBillNo("");//billNo
		objKOTAnalysisBean.setStrTableNo("");//tableNO
		objKOTAnalysisBean.setStrTableName(rsVoidedKOT.getString(5));//tableName
		objKOTAnalysisBean.setStrWaiterNo("");//waiterNo
		objKOTAnalysisBean.setStrWaiterName(rsVoidedKOT.getString(6));//waiterName
		objKOTAnalysisBean.setStrReasonName(rsVoidedKOT.getString(7));//reason
		objKOTAnalysisBean.setStrRemarks(rsVoidedKOT.getString(8));//remarks   

		listOfKOTAnalysis.add(objKOTAnalysisBean);
	    }
	    rsVoidedKOT.close();

	    //NC KOTs
	    sbSqlLive.setLength(0);
	    sbSqlQFile.setLength(0);
	    sbFilters.setLength(0);
	    operation = "NC KOT";

	    if (!"All".equals(cmbPosCode.getSelectedItem().toString()))
	    {
		sbFilters.append("and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " ");
	    }
	    if (!"All".equals(cmbUser.getSelectedItem().toString()))
	    {
		sbFilters.append(" and a.strUserCreated='" + userCode + "' ");
	    }
	    if (cmbReason.getSelectedItem().toString() != "All")
	    {
		sbFilters.append(" and a.strReasonCode='" + reasonCode + "' ");
	    }

	    sbSqlLive.append("select if(a.strKOTNo='','DirectBiller',a.strKOTNo)strKOTNo,'NC KOT' strOperationType "
		    + ",DATE_FORMAT(date(a.dteNCKOTDate),'%d-%m-%Y') dteKOTDate,TIME_FORMAT(time(a.dteNCKOTDate),'%h:%i')tmeKOTTime "
		    + ",a.strTableNo,b.strTableName,c.strReasonCode,c.strReasonName,a.strRemark "
		    + "from tblnonchargablekot a,tbltablemaster b,tblreasonmaster c "
		    + "where LENGTH(a.strKOTNo)>2 "
		    + "and a.strTableNo=b.strTableNo "
		    + "and a.strReasonCode=c.strReasonCode "
		    + "and Date(a.dteNCKOTDate) between '" + fromDate + "' and '" + toDate + "' ");
	    sbSqlLive.append(sbFilters);
	    sbSqlLive.append("group by a.strKOTNo "
		    + "order by a.strKOTNo");
	    ResultSet rsNCKOTs = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
	    while (rsNCKOTs.next())
	    {
		clsKOTAnalysisBean objKOTAnalysisBean = new clsKOTAnalysisBean();

		objKOTAnalysisBean.setStrKOTNo(rsNCKOTs.getString(1));//kotNO
		objKOTAnalysisBean.setStrOperationType(operation);//operation
		objKOTAnalysisBean.setDteKOTDate(rsNCKOTs.getString(3));//date
		objKOTAnalysisBean.setTmeKOTTime(rsNCKOTs.getString(4));//time
		objKOTAnalysisBean.setStrBillNo("");//billNo
		objKOTAnalysisBean.setStrTableNo("");//tableNO
		objKOTAnalysisBean.setStrTableName(rsNCKOTs.getString(6));//tableName
		objKOTAnalysisBean.setStrWaiterNo("");//waiterNo
		objKOTAnalysisBean.setStrWaiterName("");//waiterName
		objKOTAnalysisBean.setStrReasonName(rsNCKOTs.getString(8));//reason
		objKOTAnalysisBean.setStrRemarks(rsNCKOTs.getString(9));//remarks   

		listOfKOTAnalysis.add(objKOTAnalysisBean);
	    }
	    rsNCKOTs.close();

	    //sorting
	    Comparator<clsKOTAnalysisBean> kotComaparator = new Comparator<clsKOTAnalysisBean>()
	    {

		@Override
		public int compare(clsKOTAnalysisBean o1, clsKOTAnalysisBean o2)
		{
		    return o1.getStrKOTNo().compareToIgnoreCase(o2.getStrKOTNo());
		}
	    };
	    Collections.sort(listOfKOTAnalysis, kotComaparator);
	    //sorting//

	    //fill table data
	    for (clsKOTAnalysisBean objKOTAnalysisBean : listOfKOTAnalysis)
	    {
		Object row[] =
		{
		    objKOTAnalysisBean.getStrKOTNo(), objKOTAnalysisBean.getStrOperationType(), objKOTAnalysisBean.getDteKOTDate(), objKOTAnalysisBean.getTmeKOTTime(), objKOTAnalysisBean.getStrBillNo(), objKOTAnalysisBean.getStrTableName(), objKOTAnalysisBean.getStrWaiterName(), objKOTAnalysisBean.getStrReasonName(), objKOTAnalysisBean.getStrRemarks()
		};

		dmTableColoum.addRow(row);
		noOfKOTs++;
	    }

	    Object totalRow[] =
	    {
		"No. Of KOTs", "", "", "", "", "", "", "", noOfKOTs++
	    };
	    dmTotal.addRow(totalRow);

	    tblTotal.setModel(dmTotal);
	    tblAuditFlash.setModel(dmTableColoum);
	    tblAuditFlash.setRowHeight(25);
	    tblTotal.setRowHeight(25);

	    tblAuditFlash.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
	    tblTotal.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

	    DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
	    leftRenderer.setHorizontalAlignment(JLabel.LEFT);
	    DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
	    rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
	    DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
	    centerRenderer.setHorizontalAlignment(JLabel.CENTER);

//            tblAuditFlash.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
//            tblAuditFlash.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
	    tblAuditFlash.getColumnModel().getColumn(0).setPreferredWidth(50);//kotNO
	    tblAuditFlash.getColumnModel().getColumn(1).setPreferredWidth(50);//operation
	    tblAuditFlash.getColumnModel().getColumn(2).setPreferredWidth(50);//date
	    tblAuditFlash.getColumnModel().getColumn(3).setPreferredWidth(50);//time
	    tblAuditFlash.getColumnModel().getColumn(4).setPreferredWidth(50);//billNo
	    tblAuditFlash.getColumnModel().getColumn(5).setPreferredWidth(50);//tableName
	    tblAuditFlash.getColumnModel().getColumn(6).setPreferredWidth(50);//waiterName
	    tblAuditFlash.getColumnModel().getColumn(7).setPreferredWidth(50);//reason
	    tblAuditFlash.getColumnModel().getColumn(8).setPreferredWidth(70);//remarks  

	    //tblTotal.setSize(400, 400);
	    DefaultTableCellRenderer rightRenderer1 = new DefaultTableCellRenderer();
	    rightRenderer1.setHorizontalAlignment(JLabel.RIGHT);
	    tblTotal.getColumnModel().getColumn(1).setCellRenderer(rightRenderer1);

	    tblTotal.getColumnModel().getColumn(0).setPreferredWidth(200);//kotNO
	    tblTotal.getColumnModel().getColumn(1).setPreferredWidth(50);//operation
	    tblTotal.getColumnModel().getColumn(2).setPreferredWidth(50);//date
	    tblTotal.getColumnModel().getColumn(3).setPreferredWidth(50);//time
	    tblTotal.getColumnModel().getColumn(4).setPreferredWidth(50);//billNo
	    tblTotal.getColumnModel().getColumn(5).setPreferredWidth(50);//tableName
	    tblTotal.getColumnModel().getColumn(6).setPreferredWidth(50);//waiterName
	    tblTotal.getColumnModel().getColumn(7).setPreferredWidth(50);//reason
	    tblTotal.getColumnModel().getColumn(8).setPreferredWidth(50);//remarks  
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}

	finally
	{
	    sbSqlLive = null;
	    sbSqlQFile = null;
	    sbFilters = null;
	}
    }

    private void funDetailKOTAnalysis()
    {
	StringBuilder sbSqlLive = new StringBuilder();
	StringBuilder sbSqlQFile = new StringBuilder();
	StringBuilder sbFilters = new StringBuilder();
	try
	{
	    tblTotal.setVisible(true);
	    exportFormName = "Detail KOT Analysis";
	    reportName = "Detail KOT Analysis";;
	    double totalQuantity = 0.0;
	    fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());

	    toDate = objUtility.funGetFromToDate(dteToDate.getDate());

	    DefaultTableModel dmTableColoum = new DefaultTableModel()
	    {
		@Override
		public boolean isCellEditable(int row, int column)
		{
		    //all cells false
		    return false;
		}
	    };

	    dmTableColoum.addColumn("KOT");
	    dmTableColoum.addColumn("Opearation");
	    dmTableColoum.addColumn("Date");
	    dmTableColoum.addColumn("Time");
	    dmTableColoum.addColumn("Bill No.");
	    dmTableColoum.addColumn("Item");
	    dmTableColoum.addColumn("Qty");
	    dmTableColoum.addColumn("Table");
	    dmTableColoum.addColumn("Waiter");
	    dmTableColoum.addColumn("Reason");
	    dmTableColoum.addColumn("Remarks");

	    DefaultTableModel dmTotal = new DefaultTableModel()
	    {
		@Override
		public boolean isCellEditable(int row, int column)
		{
		    //all cells false
		    return false;
		}
	    };
	    dmTotal.addColumn("");
	    dmTotal.addColumn("");
	    dmTotal.addColumn("");
	    dmTotal.addColumn("");
	    dmTotal.addColumn("");
	    dmTotal.addColumn("");
	    dmTotal.addColumn("");
	    dmTotal.addColumn("");
	    dmTotal.addColumn("");
	    dmTotal.addColumn("");
	    dmTotal.addColumn("");

	    String userCode = funGetSelectedUserCode(cmbUser.getSelectedItem().toString());
	    String reasonCode = funGetSelectedReasonCode(cmbReason.getSelectedItem().toString());
	    String posCode = objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString().trim());

	    List<clsKOTAnalysisBean> listOfKOTAnalysis = new LinkedList<clsKOTAnalysisBean>();

	    sbSqlLive.setLength(0);
	    sbSqlQFile.setLength(0);
	    sbFilters.setLength(0);
	    String operation = "Billed KOT";

	    if (!"All".equals(cmbPosCode.getSelectedItem().toString()))
	    {
		sbFilters.append("and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " ");
	    }
	    if (!"All".equals(cmbUser.getSelectedItem().toString()))
	    {
		sbFilters.append(" and a.strUserCreated='" + userCode + "' ");
	    }
	    if (cmbReason.getSelectedItem().toString() != "All")
	    {
		sbFilters.append(" and a.strReasonCode='" + reasonCode + "' ");
	    }

	    //live billed KOTs
	    sbSqlLive.append("select if(b.strKOTNo='','DirectBiller',b.strKOTNo)strKOTNo "
		    + ",DATE_FORMAT(date(b.dteBillDate),'%d-%m-%Y') dteKOTDate,TIME_FORMAT(time(b.dteBillDate),'%h:%i')tmeKOTTime "
		    + ",a.strBillNo,a.strTableNo,c.strTableName,b.strWaiterNo,if(d.strWShortName='','ShortName',d.strWShortName)strWShortName"
		    + ",b.strItemCode,b.strItemName,sum(b.dblQuantity) "
		    + "from tblbillhd a,tblbilldtl b,tbltablemaster c,tblwaitermaster d "
		    + "where a.strBillNo=b.strBillNo  "
		    + "and date(a.dteBillDate)=date(b.dteBillDate) "
		    + "and a.strTableNo=c.strTableNo "
		    + "and b.strWaiterNo=d.strWaiterNo "
		    + "and LENGTH(b.strKOTNo)>0 "
		    + "and Date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");

	    sbSqlLive.append(sbFilters);

	    sbSqlLive.append("group by a.strBillNo,b.strKOTNo,b.strItemCode "
		    + "order by a.strBillNo,b.strKOTNo");

	    ResultSet rsBilledKOTs = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
	    while (rsBilledKOTs.next())
	    {
		clsKOTAnalysisBean objKOTAnalysisBean = new clsKOTAnalysisBean();

		objKOTAnalysisBean.setStrKOTNo(rsBilledKOTs.getString(1));//kotNO
		objKOTAnalysisBean.setStrOperationType(operation);//operation
		objKOTAnalysisBean.setDteKOTDate(rsBilledKOTs.getString(2));//date
		objKOTAnalysisBean.setTmeKOTTime(rsBilledKOTs.getString(3));//time
		objKOTAnalysisBean.setStrBillNo(rsBilledKOTs.getString(4));//billNo
		objKOTAnalysisBean.setStrTableNo(rsBilledKOTs.getString(5));//tableNO
		objKOTAnalysisBean.setStrTableName(rsBilledKOTs.getString(6));//tableName
		objKOTAnalysisBean.setStrWaiterNo(rsBilledKOTs.getString(7));//waiterNo
		objKOTAnalysisBean.setStrWaiterName(rsBilledKOTs.getString(8));//waiterName
		objKOTAnalysisBean.setStrReasonName("");//reason
		objKOTAnalysisBean.setStrRemarks("");//remarks   
		objKOTAnalysisBean.setStrItemCode(rsBilledKOTs.getString(9));//itemCode   
		objKOTAnalysisBean.setStrItemName(rsBilledKOTs.getString(10));//itemName   
		objKOTAnalysisBean.setDblQty(rsBilledKOTs.getDouble(11));//itemQty   

		listOfKOTAnalysis.add(objKOTAnalysisBean);
	    }
	    rsBilledKOTs.close();

	    //Q billed KOTs
	    sbSqlQFile.append("select if(b.strKOTNo='','DirectBiller',b.strKOTNo)strKOTNo "
		    + ",DATE_FORMAT(date(b.dteBillDate),'%d-%m-%Y') dteKOTDate,TIME_FORMAT(time(b.dteBillDate),'%h:%i')tmeKOTTime "
		    + ",a.strBillNo,a.strTableNo,c.strTableName,b.strWaiterNo,if(d.strWShortName='','ShortName',d.strWShortName)strWShortName"
		    + ",b.strItemCode,b.strItemName,sum(b.dblQuantity) "
		    + "from tblqbillhd a,tblqbilldtl b,tbltablemaster c,tblwaitermaster d "
		    + "where a.strBillNo=b.strBillNo  "
		    + "and date(a.dteBillDate)=date(b.dteBillDate) "
		    + "and a.strTableNo=c.strTableNo "
		    + "and b.strWaiterNo=d.strWaiterNo "
		    + "and LENGTH(b.strKOTNo)>0 "
		    + "and Date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "'  ");
	    sbSqlQFile.append(sbFilters);
	    sbSqlQFile.append("group by a.strBillNo,b.strKOTNo,b.strItemCode "
		    + "order by a.strBillNo,b.strKOTNo");

	    rsBilledKOTs = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFile.toString());
	    while (rsBilledKOTs.next())
	    {
		clsKOTAnalysisBean objKOTAnalysisBean = new clsKOTAnalysisBean();

		objKOTAnalysisBean.setStrKOTNo(rsBilledKOTs.getString(1));//kotNO
		objKOTAnalysisBean.setStrOperationType(operation);//operation
		objKOTAnalysisBean.setDteKOTDate(rsBilledKOTs.getString(2));//date
		objKOTAnalysisBean.setTmeKOTTime(rsBilledKOTs.getString(3));//time
		objKOTAnalysisBean.setStrBillNo(rsBilledKOTs.getString(4));//billNo
		objKOTAnalysisBean.setStrTableNo(rsBilledKOTs.getString(5));//tableNO
		objKOTAnalysisBean.setStrTableName(rsBilledKOTs.getString(6));//tableName
		objKOTAnalysisBean.setStrWaiterNo(rsBilledKOTs.getString(7));//waiterNo
		objKOTAnalysisBean.setStrWaiterName(rsBilledKOTs.getString(8));//waiterName
		objKOTAnalysisBean.setStrReasonName("");//reason
		objKOTAnalysisBean.setStrRemarks("");//remarks 
		objKOTAnalysisBean.setStrItemCode(rsBilledKOTs.getString(9));//itemCode   
		objKOTAnalysisBean.setStrItemName(rsBilledKOTs.getString(10));//itemName   
		objKOTAnalysisBean.setDblQty(rsBilledKOTs.getDouble(11));//itemQty 

		listOfKOTAnalysis.add(objKOTAnalysisBean);
	    }
	    rsBilledKOTs.close();

	    //voided billed KOTs
	    sbSqlLive.setLength(0);
	    sbSqlQFile.setLength(0);
	    sbFilters.setLength(0);

	    if (!"All".equals(cmbPosCode.getSelectedItem().toString()))
	    {
		sbFilters.append("and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " ");
	    }
	    if (!"All".equals(cmbUser.getSelectedItem().toString()))
	    {
		sbFilters.append(" and a.strUserCreated='" + userCode + "' ");
	    }
	    if (cmbReason.getSelectedItem().toString() != "All")
	    {
		sbFilters.append(" and a.strReasonCode='" + reasonCode + "' ");
	    }
	    sbSqlLive.append("select if(b.strKOTNo='','DirectBiller',b.strKOTNo)strKOTNo,b.strTransType "
		    + ",DATE_FORMAT(date(b.dteBillDate),'%d-%m-%Y') dteKOTDate,TIME_FORMAT(time(b.dteBillDate),'%h:%i')tmeKOTTime "
		    + ",a.strBillNo,a.strTableNo,c.strTableName,b.strWaiterNo,if(d.strWShortName='','ShortName',d.strWShortName)strWShortName "
		    + ",b.strReasonName,b.strRemarks"
		    + ",b.strItemCode,b.strItemName,sum(b.intQuantity) "
		    + "from tblvoidbillhd a,tblvoidbilldtl b,tbltablemaster c,tblwaitermaster d "
		    + "where a.strBillNo=b.strBillNo  "
		    + "and date(a.dteBillDate)=date(b.dteBillDate) "
		    + "and a.strTableNo=c.strTableNo "
		    + "and a.strWaiterNo=d.strWaiterNo "
		    + "and LENGTH(b.strKOTNo)>2 "
		    + "and Date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    sbSqlLive.append(sbFilters);
	    sbSqlLive.append("group by a.strBillNo,b.strKOTNo,b.strItemCode "
		    + "order by a.strBillNo,b.strKOTNo");
	    ResultSet rsVoidedBilledKOTs = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
	    while (rsVoidedBilledKOTs.next())
	    {
		clsKOTAnalysisBean objKOTAnalysisBean = new clsKOTAnalysisBean();

		operation = rsVoidedBilledKOTs.getString(2);
		if (rsVoidedBilledKOTs.getString(2).equalsIgnoreCase("VB"))
		{
		    operation = "Void Bill";
		}
		if (rsVoidedBilledKOTs.getString(2).equalsIgnoreCase("USBill"))
		{
		    operation = "Unseetled Bill";
		}
		if (rsVoidedBilledKOTs.getString(2).equalsIgnoreCase("MB"))
		{
		    operation = "Modified Bill";
		}

		objKOTAnalysisBean.setStrKOTNo(rsVoidedBilledKOTs.getString(1));//kotNO
		objKOTAnalysisBean.setStrOperationType(operation);//operation
		objKOTAnalysisBean.setDteKOTDate(rsVoidedBilledKOTs.getString(3));//date
		objKOTAnalysisBean.setTmeKOTTime(rsVoidedBilledKOTs.getString(4));//time
		objKOTAnalysisBean.setStrBillNo(rsVoidedBilledKOTs.getString(5));//billNo
		objKOTAnalysisBean.setStrTableNo(rsVoidedBilledKOTs.getString(6));//tableNO
		objKOTAnalysisBean.setStrTableName(rsVoidedBilledKOTs.getString(7));//tableName
		objKOTAnalysisBean.setStrWaiterNo(rsVoidedBilledKOTs.getString(8));//waiterNo
		objKOTAnalysisBean.setStrWaiterName(rsVoidedBilledKOTs.getString(9));//waiterName
		objKOTAnalysisBean.setStrReasonName(rsVoidedBilledKOTs.getString(10));//reason
		objKOTAnalysisBean.setStrRemarks(rsVoidedBilledKOTs.getString(11));//remarks  
		objKOTAnalysisBean.setStrItemCode(rsVoidedBilledKOTs.getString(12));//itemCode   
		objKOTAnalysisBean.setStrItemName(rsVoidedBilledKOTs.getString(13));//itemName   
		objKOTAnalysisBean.setDblQty(rsVoidedBilledKOTs.getDouble(14));//itemQty 

		listOfKOTAnalysis.add(objKOTAnalysisBean);
	    }
	    rsVoidedBilledKOTs.close();

	    //line voided KOTs
	    sbSqlLive.setLength(0);
	    sbSqlQFile.setLength(0);
	    sbFilters.setLength(0);
	    operation = "Line Void";

	    if (!"All".equals(cmbPosCode.getSelectedItem().toString()))
	    {
		sbFilters.append("and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " ");
	    }
	    if (!"All".equals(cmbUser.getSelectedItem().toString()))
	    {
		sbFilters.append(" and a.strUserCreated='" + userCode + "' ");
	    }

	    sbSqlLive.append("select if(a.strKOTNo='','DirectBiller',a.strKOTNo)strKOTNo,'Line Void' strOperationType "
		    + ",DATE_FORMAT(date(a.dteDateCreated),'%d-%m-%Y') dteKOTDate,TIME_FORMAT(time(a.dteDateCreated),'%h:%i')tmeKOTTime"
		    + ",a.strItemCode,a.strItemName,sum(a.dblItemQuantity) "
		    + "from tbllinevoid a "
		    + "where LENGTH(a.strKOTNo)>2 "
		    + "and Date(a.dteDateCreated) between '" + fromDate + "' and '" + toDate + "' ");
	    sbSqlLive.append(sbFilters);
	    sbSqlLive.append("group by a.strKOTNo,a.strItemCode "
		    + "order by a.strKOTNo");
	    ResultSet rsLineVoidedKOTs = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
	    while (rsLineVoidedKOTs.next())
	    {
		clsKOTAnalysisBean objKOTAnalysisBean = new clsKOTAnalysisBean();

		objKOTAnalysisBean.setStrKOTNo(rsLineVoidedKOTs.getString(1));//kotNO
		objKOTAnalysisBean.setStrOperationType(operation);//operation
		objKOTAnalysisBean.setDteKOTDate(rsLineVoidedKOTs.getString(3));//date
		objKOTAnalysisBean.setTmeKOTTime(rsLineVoidedKOTs.getString(4));//time
		objKOTAnalysisBean.setStrBillNo("");//billNo
		objKOTAnalysisBean.setStrTableNo("");//tableNO
		objKOTAnalysisBean.setStrTableName("");//tableName
		objKOTAnalysisBean.setStrWaiterNo("");//waiterNo
		objKOTAnalysisBean.setStrWaiterName("");//waiterName
		objKOTAnalysisBean.setStrReasonName("");//reason
		objKOTAnalysisBean.setStrRemarks("");//remarks  
		objKOTAnalysisBean.setStrItemCode(rsLineVoidedKOTs.getString(5));//itemCode   
		objKOTAnalysisBean.setStrItemName(rsLineVoidedKOTs.getString(6));//itemName   
		objKOTAnalysisBean.setDblQty(rsLineVoidedKOTs.getDouble(7));//itemQty 

		listOfKOTAnalysis.add(objKOTAnalysisBean);
	    }
	    rsLineVoidedKOTs.close();

	    //voided KOTs
	    sbSqlLive.setLength(0);
	    sbSqlQFile.setLength(0);
	    sbFilters.setLength(0);
	    operation = "Void KOT";

	    if (!"All".equals(cmbPosCode.getSelectedItem().toString()))
	    {
		sbFilters.append("and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " ");
	    }
	    if (!"All".equals(cmbUser.getSelectedItem().toString()))
	    {
		sbFilters.append(" and a.strUserCreated='" + userCode + "' ");
	    }
	    if (cmbReason.getSelectedItem().toString() != "All")
	    {
		sbFilters.append(" and a.strReasonCode='" + reasonCode + "' ");
	    }
	    sbSqlLive.append("select if(a.strKOTNo='','DirectBiller',a.strKOTNo)strKOTNo,'Void KOT' strOperationType "
		    + ",DATE_FORMAT(date(a.dteDateCreated),'%d-%m-%Y') dteKOTDate,TIME_FORMAT(time(a.dteDateCreated),'%h:%i')tmeKOTTime "
		    + ",b.strTableName,c.strWShortName,d.strReasonName,a.strRemark"
		    + ",a.strItemCode,a.strItemName,sum(a.dblItemQuantity) "
		    + "from tblvoidkot a,tbltablemaster b,tblwaitermaster c,tblreasonmaster d "
		    + "where a.strTableNo=b.strTableNo  "
		    + "and a.strWaiterNo=c.strWaiterNo "
		    + "and a.strReasonCode=d.strReasonCode "
		    + "and LENGTH(a.strKOTNo)>2 "
		    + "and Date(a.dteDateCreated) between '" + fromDate + "' and '" + toDate + "' ");
	    sbSqlLive.append(sbFilters);
	    sbSqlLive.append("group by a.strKOTNo,a.strItemCode "
		    + "order by a.strKOTNo");
	    ResultSet rsVoidedKOT = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
	    while (rsVoidedKOT.next())
	    {
		clsKOTAnalysisBean objKOTAnalysisBean = new clsKOTAnalysisBean();

		objKOTAnalysisBean.setStrKOTNo(rsVoidedKOT.getString(1));//kotNO
		objKOTAnalysisBean.setStrOperationType(operation);//operation
		objKOTAnalysisBean.setDteKOTDate(rsVoidedKOT.getString(3));//date
		objKOTAnalysisBean.setTmeKOTTime(rsVoidedKOT.getString(4));//time
		objKOTAnalysisBean.setStrBillNo("");//billNo
		objKOTAnalysisBean.setStrTableNo("");//tableNO
		objKOTAnalysisBean.setStrTableName(rsVoidedKOT.getString(5));//tableName
		objKOTAnalysisBean.setStrWaiterNo("");//waiterNo
		objKOTAnalysisBean.setStrWaiterName(rsVoidedKOT.getString(6));//waiterName
		objKOTAnalysisBean.setStrReasonName(rsVoidedKOT.getString(7));//reason
		objKOTAnalysisBean.setStrRemarks(rsVoidedKOT.getString(8));//remarks   
		objKOTAnalysisBean.setStrItemCode(rsVoidedKOT.getString(9));//itemCode   
		objKOTAnalysisBean.setStrItemName(rsVoidedKOT.getString(10));//itemName   
		objKOTAnalysisBean.setDblQty(rsVoidedKOT.getDouble(11));//itemQty 

		listOfKOTAnalysis.add(objKOTAnalysisBean);
	    }
	    rsVoidedKOT.close();

	    //NC KOTs
	    sbSqlLive.setLength(0);
	    sbSqlQFile.setLength(0);
	    sbFilters.setLength(0);
	    operation = "NC KOT";

	    if (!"All".equals(cmbPosCode.getSelectedItem().toString()))
	    {
		sbFilters.append("and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " ");
	    }
	    if (!"All".equals(cmbUser.getSelectedItem().toString()))
	    {
		sbFilters.append(" and a.strUserCreated='" + userCode + "' ");
	    }
	    if (cmbReason.getSelectedItem().toString() != "All")
	    {
		sbFilters.append(" and a.strReasonCode='" + reasonCode + "' ");
	    }

	    sbSqlLive.append("select if(a.strKOTNo='','DirectBiller',a.strKOTNo)strKOTNo,'NC KOT' strOperationType "
		    + ",DATE_FORMAT(date(a.dteNCKOTDate),'%d-%m-%Y') dteKOTDate,TIME_FORMAT(time(a.dteNCKOTDate),'%h:%i')tmeKOTTime "
		    + ",a.strTableNo,b.strTableName,c.strReasonCode,c.strReasonName,a.strRemark"
		    + ",a.strItemCode,d.strItemName,sum(a.dblQuantity) "
		    + "from tblnonchargablekot a,tbltablemaster b,tblreasonmaster c,tblitemmaster d "
		    + "where LENGTH(a.strKOTNo)>2 "
		    + "and a.strTableNo=b.strTableNo "
		    + "and a.strReasonCode=c.strReasonCode "
		    + "and a.strItemCode=d.strItemCode "
		    + "and Date(a.dteNCKOTDate) between '" + fromDate + "' and '" + toDate + "' ");
	    sbSqlLive.append(sbFilters);
	    sbSqlLive.append("group by a.strKOTNo,a.strItemCode "
		    + "order by a.strKOTNo");
	    ResultSet rsNCKOTs = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
	    while (rsNCKOTs.next())
	    {
		clsKOTAnalysisBean objKOTAnalysisBean = new clsKOTAnalysisBean();

		objKOTAnalysisBean.setStrKOTNo(rsNCKOTs.getString(1));//kotNO
		objKOTAnalysisBean.setStrOperationType(operation);//operation
		objKOTAnalysisBean.setDteKOTDate(rsNCKOTs.getString(3));//date
		objKOTAnalysisBean.setTmeKOTTime(rsNCKOTs.getString(4));//time
		objKOTAnalysisBean.setStrBillNo("");//billNo
		objKOTAnalysisBean.setStrTableNo("");//tableNO
		objKOTAnalysisBean.setStrTableName(rsNCKOTs.getString(6));//tableName
		objKOTAnalysisBean.setStrWaiterNo("");//waiterNo
		objKOTAnalysisBean.setStrWaiterName("");//waiterName
		objKOTAnalysisBean.setStrReasonName(rsNCKOTs.getString(8));//reason
		objKOTAnalysisBean.setStrRemarks(rsNCKOTs.getString(9));//remarks   
		objKOTAnalysisBean.setStrItemCode(rsNCKOTs.getString(10));//itemCode   
		objKOTAnalysisBean.setStrItemName(rsNCKOTs.getString(11));//itemName   
		objKOTAnalysisBean.setDblQty(rsNCKOTs.getDouble(12));//itemQty 

		listOfKOTAnalysis.add(objKOTAnalysisBean);
	    }
	    rsNCKOTs.close();

	    //sorting
	    Comparator<clsKOTAnalysisBean> kotComaparator = new Comparator<clsKOTAnalysisBean>()
	    {

		@Override
		public int compare(clsKOTAnalysisBean o1, clsKOTAnalysisBean o2)
		{
		    return o1.getStrKOTNo().compareToIgnoreCase(o2.getStrKOTNo());
		}
	    };
	    Collections.sort(listOfKOTAnalysis, kotComaparator);
	    //sorting//

	    //fill table data
	    for (clsKOTAnalysisBean objKOTAnalysisBean : listOfKOTAnalysis)
	    {
		Object row[] =
		{
		    objKOTAnalysisBean.getStrKOTNo(), objKOTAnalysisBean.getStrOperationType(), objKOTAnalysisBean.getDteKOTDate(), objKOTAnalysisBean.getTmeKOTTime(), objKOTAnalysisBean.getStrBillNo(), objKOTAnalysisBean.getStrItemName(), objKOTAnalysisBean.getDblQty(), objKOTAnalysisBean.getStrTableName(), objKOTAnalysisBean.getStrWaiterName(), objKOTAnalysisBean.getStrReasonName(), objKOTAnalysisBean.getStrRemarks()
		};

		dmTableColoum.addRow(row);

		totalQuantity += objKOTAnalysisBean.getDblQty();
	    }

	    Object totalRow[] =
	    {
		"Total Qty", "", "", "", "", "", "", "", "", "", totalQuantity
	    };
	    dmTotal.addRow(totalRow);

	    tblTotal.setModel(dmTotal);
	    tblAuditFlash.setModel(dmTableColoum);
	    tblAuditFlash.setRowHeight(25);
	    tblTotal.setRowHeight(25);

	    tblAuditFlash.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    tblTotal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

	    DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
	    leftRenderer.setHorizontalAlignment(JLabel.LEFT);
	    DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
	    rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
	    DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
	    centerRenderer.setHorizontalAlignment(JLabel.CENTER);

	    tblAuditFlash.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);//itemQty    

	    tblAuditFlash.getColumnModel().getColumn(0).setPreferredWidth(70);//kotNO
	    tblAuditFlash.getColumnModel().getColumn(1).setPreferredWidth(70);//operation
	    tblAuditFlash.getColumnModel().getColumn(2).setPreferredWidth(70);//date
	    tblAuditFlash.getColumnModel().getColumn(3).setPreferredWidth(50);//time
	    tblAuditFlash.getColumnModel().getColumn(4).setPreferredWidth(70);//billNo            
	    tblAuditFlash.getColumnModel().getColumn(5).setPreferredWidth(200);//itemName
	    tblAuditFlash.getColumnModel().getColumn(6).setPreferredWidth(50);//itemQty           
	    tblAuditFlash.getColumnModel().getColumn(7).setPreferredWidth(50);//tableName            
	    tblAuditFlash.getColumnModel().getColumn(8).setPreferredWidth(50);//waiterName
	    tblAuditFlash.getColumnModel().getColumn(9).setPreferredWidth(100);//reason
	    tblAuditFlash.getColumnModel().getColumn(10).setPreferredWidth(100);//remarks  

	    //tblTotal.setSize(400, 400);
	    DefaultTableCellRenderer rightRenderer1 = new DefaultTableCellRenderer();
	    rightRenderer1.setHorizontalAlignment(JLabel.RIGHT);
	    tblTotal.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);//itemQty    

	    tblTotal.getColumnModel().getColumn(0).setPreferredWidth(70);//kotNO
	    tblTotal.getColumnModel().getColumn(1).setPreferredWidth(70);//operation
	    tblTotal.getColumnModel().getColumn(2).setPreferredWidth(70);//date
	    tblTotal.getColumnModel().getColumn(3).setPreferredWidth(50);//time
	    tblTotal.getColumnModel().getColumn(4).setPreferredWidth(70);//billNo            
	    tblTotal.getColumnModel().getColumn(5).setPreferredWidth(150);//itemName
	    tblTotal.getColumnModel().getColumn(6).setPreferredWidth(50);//itemQty           
	    tblTotal.getColumnModel().getColumn(7).setPreferredWidth(50);//tableName            
	    tblTotal.getColumnModel().getColumn(8).setPreferredWidth(50);//waiterName
	    tblTotal.getColumnModel().getColumn(9).setPreferredWidth(50);//reason
	    tblTotal.getColumnModel().getColumn(10).setPreferredWidth(70);//remarks    
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}

	finally
	{
	    sbSqlLive = null;
	    sbSqlQFile = null;
	    sbFilters = null;
	}
    }

    /**
     * this Function is used for get Modify bill
     *
     * @return
     */
    private void funModifiedBillFlash()
    {

	StringBuilder sbSql = new StringBuilder();
	try
	{
	    reportName = "Bill";
	    tblTotal.setVisible(true);
	    exportFormName = "Modifed Bill";
	    fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());

	    toDate = objUtility.funGetFromToDate(dteToDate.getDate());

	    String userCode = funGetSelectedUserCode(cmbUser.getSelectedItem().toString());
	    String POSCode = objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString().trim());
	    String reasonCode = funGetSelectedReasonCode(cmbReason.getSelectedItem().toString());
	    DefaultTableModel dm1 = new DefaultTableModel()
	    {
		@Override
		public boolean isCellEditable(int row, int column)
		{
		    //all cells false
		    return false;
		}
	    };
	    dm = new DefaultTableModel()
	    {
		@Override
		public boolean isCellEditable(int row, int column)
		{
		    //all cells false
		    return false;
		}
	    };
	    dm.addColumn("Bill No");
	    dm.addColumn("Bill Date");
	    dm.addColumn("Entry Time");
	    dm.addColumn("Modify Time");
	    dm.addColumn("Bill Amt");
	    dm.addColumn("New Amt");
	    dm.addColumn("Discount");
	    dm.addColumn("User Created");
	    dm.addColumn("User Edited");
	    dm.addColumn("Reason");
	    dm.addColumn("Remarks");

	    dm1.addColumn("");
	    dm1.addColumn("");
	    dm1.addColumn("");
	    dm1.addColumn("");
	    dm1.addColumn("");
	    double sumBillAmt = 0.00, sumNewAmt = 0.00;
	    double sumQty = 0.00, discAmt = 0.0;

	    dm.getDataVector().removeAllElements();
	    dm1.getDataVector().removeAllElements();
	    tblAuditFlash.updateUI();
	    tblTotal.updateUI();

	    sbSql.setLength(0);
	    if ("Summary".equals(cmbType.getSelectedItem()))
	    {
		sbSql.append("select a.strBillNo as BillNo, DATE_FORMAT(Date(a.dteBillDate),'%d-%m-%Y') as BillDate ,"
			+ " DATE_FORMAT(Date(a.dteModifyVoidBill),'%d-%m-%Y') as ModifiedDate ,TIME_FORMAT(time(a.dteBillDate),'%h:%i') as EntryTime , "
			+ " TIME_FORMAT(time(a.dteModifyVoidBill),'%h:%i') as ModifyTime,a.dblActualAmount as BillAmt ,"
			+ " a.dblModifiedAmount as NetAmt,a.strUserCreated as UserCreated, "
			+ " a.strUserEdited as UserEdited,ifnull(b.strReasonName,'') as ReasonName,ifnull(a.strRemark,'')"
			+ " ,(a.dblActualAmount-a.dblModifiedAmount) as DiscAmt "
			+ " from tblvoidbillhd a left outer join tblreasonmaster b on a.strReasonCode=b.strReasonCode ");
		if (!"All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append(" where a.strTransType='MB' and Date(a.dteModifyVoidBill) between '"
			    + fromDate + "' and '" + toDate + "' and a.strUserCreated='" + userCode + "'"
			    + " and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + "  and a.strreasonCode='" + reasonCode + "' "
			    + "group by a.strBillNo,a.dteModifyVoidBill");
		}
		else if (!"All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()) && "All".equals(cmbUser.getSelectedItem()))
		{
		    sbSql.append(" where a.strTransType='MB' and Date(a.dteModifyVoidBill) between '"
			    + fromDate + "' and '" + toDate + "' and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + "  and  "
			    + "a.strreasonCode='" + reasonCode + "' group by a.strBillNo,a.dteModifyVoidBill");
		}
		else if ("All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append(" where a.strTransType='MB' and Date(a.dteModifyVoidBill) between '"
			    + fromDate + "' and '" + toDate + "' and a.strUserCreated='" + userCode + "'  "
			    + "and a.strreasonCode='" + reasonCode + "' "
			    + "group by a.strBillNo,a.dteModifyVoidBill");
		}
		else if (!"All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()) && "All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append(" where a.strTransType='MB' and Date(a.dteModifyVoidBill) between '"
			    + fromDate + "' and '" + toDate + "' and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " "
			    + "and a.strUserCreated='" + userCode + "' "
			    + "group by a.strBillNo,a.dteModifyVoidBill");
		}
		else if ("All".equals(cmbPosCode.getSelectedItem()) && "All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append(" where a.strTransType='MB' and Date(a.dteModifyVoidBill) between '"
			    + fromDate + "' and '" + toDate + "' and a.strreasonCode='" + reasonCode + "' "
			    + "group by a.strBillNo,a.dteModifyVoidBill");
		}
		else if ("All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()) && "All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append(" where a.strTransType='MB' and Date(a.dteModifyVoidBill) between '"
			    + fromDate + "' and '" + toDate + "' and a.strUserCreated='" + userCode + "' "
			    + "group by a.strBillNo,a.dteModifyVoidBill");
		}
		else if (!"All".equals(cmbPosCode.getSelectedItem()) && "All".equals(cmbUser.getSelectedItem()) && "All".equals(cmbReason.getSelectedItem()))
		{

		    sbSql.append(" where a.strTransType='MB' and Date(a.dteModifyVoidBill) between '"
			    + fromDate + "' and '" + toDate + "' and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " "
			    + "group by a.strBillNo,a.dteModifyVoidBill");
		}
		else
		{
		    sbSql.append(" where a.strTransType='MB' and Date(a.dteModifyVoidBill) between '"
			    + fromDate + "' and '" + toDate + "' group by a.strBillNo,a.dteModifyVoidBill");
		}
		if ("Item Void".equalsIgnoreCase(cmbSorting1.getSelectedItem().toString()))
		{
		    sbSql.append(" and a.strVoidBillType ='" + cmbSorting1.getSelectedItem().toString() + "' ");
		}
		else if ("Full Void".equalsIgnoreCase(cmbSorting1.getSelectedItem().toString()))
		{
		    sbSql.append(" and a.strVoidBillType ='Bill Void' ");
		}
		else
		{
		    sbSql.append(" and (a.strVoidBillType = 'Bill Void' or a.strVoidBillType = 'ITEM VOID' ) ");
		}
		if (cmbSorting.getSelectedItem().equals("BILL"))
		{
		    sbSql.append(" order by a.strBillNo");
		}
		else
		{
		    sbSql.append(" order by a.dblActualAmount");
		}
		ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
		while (rs.next())
		{
		    Object[] row =
		    {
			rs.getString(1), rs.getString(2), rs.getString(4), rs.getString(5), gDecimalFormat.format(rs.getDouble(6)), gDecimalFormat.format(rs.getDouble(7)), gDecimalFormat.format(rs.getDouble(12)), rs.getString(8), rs.getString(9), rs.getString(10), rs.getString(11)
		    };
		    dm.addRow(row);
		    sumBillAmt = sumBillAmt + rs.getDouble(6);
		    sumNewAmt = sumNewAmt + rs.getDouble(7);
		    discAmt += rs.getDouble(12);
		}
		rs.close();
		Object[] total =
		{
		    "Total", gDecimalFormat.format(sumBillAmt), gDecimalFormat.format(sumNewAmt), gDecimalFormat.format(discAmt), " "
		};
		dm1.addRow(total);
		tblAuditFlash.setModel(dm);
		tblTotal.setModel(dm1);
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
		tblAuditFlash.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
		tblAuditFlash.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
		tblAuditFlash.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);
		DefaultTableCellRenderer rightRenderer1 = new DefaultTableCellRenderer();
		rightRenderer1.setHorizontalAlignment(JLabel.RIGHT);
		tblTotal.getColumnModel().getColumn(1).setCellRenderer(rightRenderer1);
		tblTotal.getColumnModel().getColumn(2).setCellRenderer(rightRenderer1);
		tblTotal.getColumnModel().getColumn(3).setCellRenderer(rightRenderer1);
		tblTotal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tblTotal.getColumnModel().getColumn(0).setPreferredWidth(370);
		tblTotal.getColumnModel().getColumn(1).setPreferredWidth(84);
		tblTotal.getColumnModel().getColumn(2).setPreferredWidth(113);
		tblTotal.getColumnModel().getColumn(3).setPreferredWidth(113);
		tblTotal.getColumnModel().getColumn(4).setPreferredWidth(100);
	    }
	    else
	    {
		dm = new DefaultTableModel()
		{
		    @Override
		    public boolean isCellEditable(int row, int column)
		    {
			//all cells false
			return false;
		    }
		};
		dm.addColumn("Bill No");
		dm.addColumn("Bill Date");
		//dm.addColumn("Modified Date");
		dm.addColumn("Entry Time");
		dm.addColumn("Modify Time");
		dm.addColumn("Item Name");
		dm.addColumn("Qty");
		dm.addColumn("Amount");
		dm.addColumn("User Created");
		dm.addColumn("User Edited");
		dm.addColumn("Remarks");

		dm1 = new DefaultTableModel()
		{
		    @Override
		    public boolean isCellEditable(int row, int column)
		    {
			//all cells false
			return false;
		    }
		};
		dm1.addColumn("");
		dm1.addColumn("");
		dm1.addColumn("");
		dm1.addColumn("");
		dm1.addColumn("");
		dm.getDataVector().removeAllElements();
		dm1.getDataVector().removeAllElements();
		tblAuditFlash.updateUI();
		tblTotal.updateUI();
		ResultSet rs = null;

		sbSql.setLength(0);
		sbSql.append("select a.strBillNo,DATE_FORMAT(date(b.dteBillDate),'%d-%m-%Y') as BillDate,DATE_FORMAT(Date(a.dteModifyVoidBill),'%d-%m-%Y') as ModifiedDate,"
			+ "Time(a.dteBillDate) EntryTime,Time(a.dteModifyVoidBill) ModifiedTime,a.strItemName,a.intQuantity,sum(a.dblAmount) as Amount,"
			+ "b.strUserCreated as Usercreated,b.strUserEdited as UserEdited,ifnull(b.strRemark,'') "
			+ " from tblvoidbilldtl a, tblvoidbillhd b ");

		if (!"All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append("where a.strBillNo=b.strBillNo and a.strTransType='MB' and Date(a.dteModifyVoidBill) between '" + fromDate + "' and '" + toDate + "' "
			    + "and  " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " and "
			    + "a.strUserCreated='" + userCode + "' and a.strreasonCode='" + reasonCode + "' "
			    + "group by a.strItemName,a.strBillNo ");
		}
		else if ("All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append("where a.strBillNo=b.strBillNo and a.strTransType='MB' and Date(a.dteModifyVoidBill) between '" + fromDate + "' and '" + toDate + "' and  a.strUserCreated='" + userCode + "' and a.strreasonCode='" + reasonCode + "' "
			    + "group by a.strItemName,a.strBillNo ");
		}
		else if (!"All".equals(cmbPosCode.getSelectedItem()) && "All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append("where a.strBillNo=b.strBillNo and a.strTransType='MB' and Date(a.dteModifyVoidBill) between '" + fromDate + "' and '" + toDate + "'"
			    + " and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " and a.strreasonCode='" + reasonCode + "' "
			    + "group by a.strItemName,a.strBillNo ");
		}
		else if (!"All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()) && "All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append("where a.strBillNo=b.strBillNo and a.strTransType='MB' "
			    + "and Date(a.dteModifyVoidBill) between '" + fromDate + "' and '" + toDate + "' "
			    + "and  " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " and a.strUserCreated='" + userCode + "' "
			    + "group by a.strItemName,a.strBillNo ");
		}
		else if (!"All".equals(cmbPosCode.getSelectedItem()) && "All".equals(cmbUser.getSelectedItem()) && "All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append("where a.strBillNo=b.strBillNo and a.strTransType='MB' "
			    + "and Date(a.dteModifyVoidBill) between '" + fromDate + "' and '" + toDate + "' "
			    + "and  " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + "  "
			    + "group by a.strItemName,a.strBillNo ");
		}
		else if ("All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()) && "All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append("where a.strBillNo=b.strBillNo and a.strTransType='MB' "
			    + "and Date(a.dteModifyVoidBill) between '" + fromDate + "' and '" + toDate + "' "
			    + "and  a.strUserCreated='" + userCode + "' "
			    + "group by a.strItemName,a.strBillNo ");
		}
		else if ("All".equals(cmbPosCode.getSelectedItem()) && "All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append("where a.strBillNo=b.strBillNo and a.strTransType='MB' "
			    + "and Date(a.dteModifyVoidBill) between '" + fromDate + "' and '" + toDate + "' "
			    + "and  a.strreasonCode='" + reasonCode + "' "
			    + "group by a.strItemName,a.strBillNo ");
		}
		else
		{
		    sbSql.append("where a.strBillNo=b.strBillNo and a.strTransType='MB' "
			    + "and Date(a.dteModifyVoidBill) between '" + fromDate + "' and '" + toDate + "' "
			    + "group by a.strItemName,a.strBillNo ");
		}
		if ("Item Void".equalsIgnoreCase(cmbSorting1.getSelectedItem().toString()))
		{
		    sbSql.append("and b.strVoidBillType ='" + cmbSorting1.getSelectedItem().toString() + "' ");
		}
		else if ("Full Void".equalsIgnoreCase(cmbSorting1.getSelectedItem().toString()))
		{
		    sbSql.append("and b.strVoidBillType ='Bill Void' ");
		}
		else
		{
		    sbSql.append("and (b.strVoidBillType = 'Bill Void' or b.strVoidBillType = 'ITEM VOID') ");
		}
		if (cmbSorting.getSelectedItem().equals("BILL"))
		{
		    sbSql.append(" order by a.strBillNo asc");
		}
		else
		{
		    sbSql.append(" order by sum(a.dblAmount) asc");
		}

		rs = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
		while (rs.next())
		{
		    Object[] row =
		    {
			rs.getString(1), rs.getString(2), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7), gDecimalFormat.format(rs.getDouble(8)), rs.getString(9), rs.getString(10)
		    };
		    dm.addRow(row);
		    sumQty = sumQty + rs.getDouble(7);
		    sumBillAmt = sumBillAmt + rs.getDouble(8);
		}
		rs.close();
		Object[] total =
		{
		    "Total", gDecimalFormat.format(sumQty), gDecimalFormat.format(sumBillAmt), "", ""
		};
		dm1.addRow(total);
		tblAuditFlash.setModel(dm);
		tblTotal.setModel(dm1);
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
		tblAuditFlash.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
		tblAuditFlash.getColumnModel().getColumn(5).setPreferredWidth(30);
		tblAuditFlash.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);
		DefaultTableCellRenderer rightRenderer1 = new DefaultTableCellRenderer();
		rightRenderer1.setHorizontalAlignment(JLabel.RIGHT);
		tblTotal.getColumnModel().getColumn(1).setCellRenderer(rightRenderer1);
		tblTotal.getColumnModel().getColumn(2).setCellRenderer(rightRenderer1);
		tblTotal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tblTotal.getColumnModel().getColumn(0).setPreferredWidth(400);
		tblTotal.getColumnModel().getColumn(1).setPreferredWidth(84);
		tblTotal.getColumnModel().getColumn(2).setPreferredWidth(85);
		tblTotal.getColumnModel().getColumn(3).setPreferredWidth(135);
		tblTotal.getColumnModel().getColumn(4).setPreferredWidth(70);
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	finally
	{
	    sbSql = null;
	}
    }

//////////////////////////////////
    @SuppressWarnings("empty-statement")
    /**
     * this Function is used for get void Bill Details
     *
     * @return
     */
    private void funVoidedBillFlash()
    {

	StringBuilder sbSql = new StringBuilder();
	StringBuilder sbSqlMod = new StringBuilder();

	try
	{
	    reportName = "Bill";
	    tblTotal.setVisible(true);
	    exportFormName = "Voided Bill";
	    fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());

	    toDate = objUtility.funGetFromToDate(dteToDate.getDate());
	    List<clsBillItemDtlBean> arrListVoidBillWise = new ArrayList<clsBillItemDtlBean>();
	    String userCode = funGetSelectedUserCode(cmbUser.getSelectedItem().toString());
	    String POSCode = objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString().trim());
	    String ResonCode = funGetSelectedReasonCode(cmbReason.getSelectedItem().toString());
	    DefaultTableModel dm1 = new DefaultTableModel()
	    {
		@Override
		public boolean isCellEditable(int row, int column)
		{
		    //all cells false
		    return false;
		}
	    };
	    dm1.getDataVector().removeAllElements();
	    DefaultTableModel dm2 = new DefaultTableModel()
	    {
		@Override
		public boolean isCellEditable(int row, int column)
		{
		    //all cells false
		    return false;
		}
	    };
	    dm2.getDataVector().removeAllElements();
	    dm2.addColumn("");
	    dm2.addColumn("");
	    dm2.addColumn("");
	    //dm2.addColumn("");
	    tblAuditFlash.updateUI();
	    tblTotal.updateUI();
	    dm1.addColumn("POS");
	    dm1.addColumn("POS Name");
	    dm1.addColumn("Bill No");
	    dm1.addColumn("Bill Date");
	    dm1.addColumn("Voided Date");
	    dm1.addColumn("Entry Time");
	    dm1.addColumn("Voided Time");
	    dm1.addColumn("Item Name");
	    dm1.addColumn("Qty");
	    dm1.addColumn("Amt");
	    dm1.addColumn("User Edited");
	    dm1.addColumn("Reason");
	    dm1.addColumn("Remarks");
	    double sumQty = 0.00, sumTotalAmt = 0.00;

	    sbSql.setLength(0);
	    sbSql.append("(select a.strBillNo as strBillNo,DATE_FORMAT(Date(a.dteBillDate),'%d-%m-%Y') as BillDate,DATE_FORMAT(Date(a.dteModifyVoidBill),'%d-%m-%Y') as VoidedDate,"
		    + "TIME_FORMAT(time(a.dteBillDate),'%h:%i') As EntryTime,TIME_FORMAT(time(a.dteModifyVoidBill),'%h:%i') VoidedTime,b.strItemName,"
		    + "b.intQuantity,b.dblAmount as BillAmount,a.strReasonName as Reason,a.strUserEdited as UserEdited,ifnull(a.strRemark,'')"
		    + ",c.strPosCode,c.strPosName "
		    + "from tblvoidbillhd a,tblvoidbilldtl b,tblposmaster c "
		    + "where a.strBillNo=b.strBillNo "
		    + "and date(a.dteBillDate)=date(b.dteBillDate) "
		    + "and a.strPosCode=c.strPosCode ");
	    if (!"Summary".equals(cmbType.getSelectedItem()))
	    {
		if (!"All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append(" and b.strTransType='VB' and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " "
			    + "and a.strUserCreated='" + userCode + "' and a.strreasonCode='" + ResonCode
			    + "' and Date(a.dteModifyVoidBill) between '" + fromDate + "' and '" + toDate + "'");
		}
		else if (!"All".equals(cmbPosCode.getSelectedItem()) && "All".equals(cmbUser.getSelectedItem()) && "All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append(" and b.strTransType='VB' and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " and "
			    + "Date(a.dteModifyVoidBill) between '" + fromDate + "' and '" + toDate + "'");
		}
		else if ("All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()) && "All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append(" and b.strTransType='VB' and a.strUserCreated='" + userCode + "' "
			    + "and Date(a.dteModifyVoidBill) between '" + fromDate + "' and '" + toDate + "'");
		}
		else if ("All".equals(cmbPosCode.getSelectedItem()) && "All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append(" and b.strTransType='VB' and a.strreasonCode='" + ResonCode + "' "
			    + "and Date(a.dteModifyVoidBill) between '" + fromDate + "' and '" + toDate + "'");
		}
		else if (!"All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()) && "All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append(" and b.strTransType='VB' " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " "
			    + "and a.strUserCreated='" + userCode + "' and Date(a.dteModifyVoidBill) "
			    + "between '" + fromDate + "' and '" + toDate + "'");
		}
		else if (!"All".equals(cmbPosCode.getSelectedItem()) && "All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append(" and b.strTransType='VB' and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " "
			    + "and a.strreasonCode='" + ResonCode + "' and Date(a.dteModifyVoidBill) "
			    + "between '" + fromDate + "' and '" + toDate + "'");
		}
		else if ("All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append(" and b.strTransType='VB' and a.strUserCreated='" + userCode + "' "
			    + "and a.strreasonCode='" + ResonCode + "' and Date(a.dteModifyVoidBill)"
			    + " between '" + fromDate + "' and '" + toDate + "'");
		}
		else
		{
		    sbSql.append(" and b.strTransType='VB' and Date(a.dteModifyVoidBill) "
			    + "between '" + fromDate + "' and '" + toDate + "'");
		}
		if ("Item Void".equalsIgnoreCase(cmbSorting1.getSelectedItem().toString()))
		{
		    sbSql.append("and a.strVoidBillType ='" + cmbSorting1.getSelectedItem().toString() + "'");
		}
		else if ("Full Void".equalsIgnoreCase(cmbSorting1.getSelectedItem().toString()))
		{
		    sbSql.append("and a.strVoidBillType ='Bill Void'");
		}
		else
		{
		    sbSql.append("and (a.strVoidBillType = 'Bill Void' or a.strVoidBillType = 'ITEM VOID' )");
		}

		sbSql.append(" group by a.strBillNo,b.strItemCode)");
//                if(cmbSorting.getSelectedItem().equals("BILL"))
//                {
//                    sbSql.append(" order by a.strBillNo");
//                }
//                else{
//                    sbSql.append(" order by b.dblAmount");
//                }

		sbSqlMod.setLength(0);
		sbSqlMod.append("(select a.strBillNo as strBillNo,DATE_FORMAT(Date(a.dteBillDate),'%d-%m-%Y') as BillDate,DATE_FORMAT(Date(a.dteModifyVoidBill),'%d-%m-%Y') as VoidedDate,"
			+ "TIME_FORMAT(time(a.dteBillDate),'%h:%i')  As EntryTime,TIME_FORMAT(time(a.dteModifyVoidBill),'%h:%i') VoidedTime,b.strModifierName,"
			+ "b.dblQuantity,b.dblAmount as BillAmount,a.strReasonName as Reason,a.strUserEdited as UserEdited,ifnull(a.strRemark,'')"
			+ ",c.strPosCode,c.strPosName "
			+ "from tblvoidbillhd a,tblvoidmodifierdtl b,tblposmaster c "
			+ "where a.strBillNo=b.strBillNo "
			+ "and date(a.dteBillDate)=date(b.dteBillDate) "
			+ "and a.strPosCode=c.strPosCode ");

		if (!"Summary".equals(cmbType.getSelectedItem()))
		{
		    if (!"All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		    {
			sbSqlMod.append(" and a.strTransType='VB' and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " "
				+ "and a.strUserCreated='" + userCode + "' and a.strreasonCode='" + ResonCode
				+ "' and Date(a.dteModifyVoidBill) between '" + fromDate + "' and '" + toDate + "'");
		    }
		    else if (!"All".equals(cmbPosCode.getSelectedItem()) && "All".equals(cmbUser.getSelectedItem()) && "All".equals(cmbReason.getSelectedItem()))
		    {
			sbSqlMod.append(" and a.strTransType='VB' and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " and "
				+ "Date(a.dteModifyVoidBill) between '" + fromDate + "' and '" + toDate + "'");
		    }
		    else if ("All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()) && "All".equals(cmbReason.getSelectedItem()))
		    {
			sbSqlMod.append(" and a.strTransType='VB' and a.strUserCreated='" + userCode + "' "
				+ "and Date(a.dteModifyVoidBill) between '" + fromDate + "' and '" + toDate + "'");
		    }
		    else if ("All".equals(cmbPosCode.getSelectedItem()) && "All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		    {
			sbSqlMod.append(" and a.strTransType='VB' and a.strreasonCode='" + ResonCode + "' "
				+ "and Date(a.dteModifyVoidBill) between '" + fromDate + "' and '" + toDate + "'");
		    }
		    else if (!"All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()) && "All".equals(cmbReason.getSelectedItem()))
		    {
			sbSqlMod.append(" and a.strTransType='VB' and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " "
				+ "and a.strUserCreated='" + userCode + "' and Date(a.dteModifyVoidBill) "
				+ "between '" + fromDate + "' and '" + toDate + "'");
		    }
		    else if (!"All".equals(cmbPosCode.getSelectedItem()) && "All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		    {
			sbSqlMod.append(" and a.strTransType='VB' and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " "
				+ "and a.strreasonCode='" + ResonCode + "' and Date(a.dteModifyVoidBill) "
				+ "between '" + fromDate + "' and '" + toDate + "'");
		    }
		    else if ("All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		    {
			sbSqlMod.append(" and a.strTransType='VB' and a.strUserCreated='" + userCode + "' "
				+ "and a.strreasonCode='" + ResonCode + "' and Date(a.dteModifyVoidBill)"
				+ " between '" + fromDate + "' and '" + toDate + "'");
		    }
		    else
		    {
			sbSqlMod.append(" and a.strTransType='VB' and Date(a.dteModifyVoidBill) "
				+ "between '" + fromDate + "' and '" + toDate + "'");
		    }
		    if ("Item Void".equalsIgnoreCase(cmbSorting1.getSelectedItem().toString()))
		    {
			sbSqlMod.append("and a.strVoidBillType ='" + cmbSorting1.getSelectedItem().toString() + "'");
		    }
		    else if ("Full Void".equalsIgnoreCase(cmbSorting1.getSelectedItem().toString()))
		    {
			sbSqlMod.append("and a.strVoidBillType ='Bill Void'");
		    }
		    else
		    {
			sbSqlMod.append("and (a.strVoidBillType = 'Bill Void' or a.strVoidBillType = 'ITEM VOID' )");
		    }

		    sbSqlMod.append(" group by a.strBillNo,b.strModifierCode)");
		    if (cmbSorting.getSelectedItem().equals("BILL"))
		    {
			sbSqlMod.append(" order by strBillNo");
		    }
		    else
		    {
			sbSqlMod.append(" order by BillAmount");
		    }
		}

		String sql = sbSql.toString() + " union " + sbSqlMod.toString();
		//System.out.println(sql);
		ResultSet rsVoidBillDetail = clsGlobalVarClass.dbMysql.executeResultSet(sql);
		while (rsVoidBillDetail.next())
		{
		    Object[] row =
		    {
			rsVoidBillDetail.getString(12), rsVoidBillDetail.getString(13), rsVoidBillDetail.getString(1), rsVoidBillDetail.getString(2), rsVoidBillDetail.getString(3),
			rsVoidBillDetail.getString(4), rsVoidBillDetail.getString(5), rsVoidBillDetail.getString(6),
			rsVoidBillDetail.getString(7), rsVoidBillDetail.getString(8), rsVoidBillDetail.getString(9),
			gDecimalFormat.format(rsVoidBillDetail.getDouble(10))
		    };
		    dm1.addRow(row);
		    sumQty = sumQty + rsVoidBillDetail.getDouble(7);
		    sumTotalAmt = sumTotalAmt + rsVoidBillDetail.getDouble(8);
		}
		rsVoidBillDetail.close();
		Object[] total =
		{
		    "Total", gDecimalFormat.format(sumQty), gDecimalFormat.format(sumTotalAmt)
		};
		dm2.addRow(total);
		tblAuditFlash.setModel(dm1);
		tblTotal.setModel(dm2);
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(JLabel.RIGHT);

		tblAuditFlash.getColumnModel().getColumn(0).setPreferredWidth(100);
		tblAuditFlash.getColumnModel().getColumn(7).setPreferredWidth(100);
		tblAuditFlash.getColumnModel().getColumn(8).setPreferredWidth(40);
		tblAuditFlash.getColumnModel().getColumn(8).setCellRenderer(rightRenderer);
		tblAuditFlash.getColumnModel().getColumn(9).setCellRenderer(rightRenderer);
		DefaultTableCellRenderer rightRenderer1 = new DefaultTableCellRenderer();
		rightRenderer1.setHorizontalAlignment(JLabel.RIGHT);
		tblTotal.getColumnModel().getColumn(1).setCellRenderer(rightRenderer1);
		tblTotal.getColumnModel().getColumn(2).setCellRenderer(rightRenderer1);
		tblTotal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		tblTotal.getColumnModel().getColumn(0).setPreferredWidth(460);
		tblTotal.getColumnModel().getColumn(1).setPreferredWidth(84);
		tblTotal.getColumnModel().getColumn(2).setPreferredWidth(95);
	    }
	    else
	    {
		dm1 = new DefaultTableModel()
		{
		    @Override
		    public boolean isCellEditable(int row, int column)
		    {
			return false;
		    }
		};
		dm1.getDataVector().removeAllElements();
		dm2 = new DefaultTableModel()
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
		tblAuditFlash.updateUI();
		tblTotal.updateUI();

		dm1.addColumn("POS");
		dm1.addColumn("POS Name");
		dm1.addColumn("Bill No");
		dm1.addColumn("Bill Date");
		dm1.addColumn("Voided Date");
		dm1.addColumn("Entry Time");
		dm1.addColumn("Voided Time");
		dm1.addColumn("Amt");
		dm1.addColumn("User Edited");
		dm1.addColumn("Reason");
		dm1.addColumn("Remarks");

		sbSql.setLength(0);
		sbSql.append("select a.strBillNo,DATE_FORMAT(Date(a.dteBillDate),'%d-%m-%Y') as BillDate,DATE_FORMAT(Date(a.dteModifyVoidBill),'%d-%m-%Y') as VoidedDate,"
			+ "TIME_FORMAT(time(a.dteBillDate),'%h:%i')  As EntryTime,TIME_FORMAT(time(a.dteModifyVoidBill),'%h:%i') VoidedTime, a.dblModifiedAmount,"
			+ "a.strUserEdited as UserEdited, a.strReasonName as Reason,ifnull(a.strRemark,'')"
			+ ",b.strPosCode,b.strPosName "
			+ " from tblvoidbillhd a,tblposmaster b "
			+ " where a.strTransType='VB'  "
			+ " and a.strPosCode=b.strPosCode ");
		if (!"All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append(" and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " and "
			    + "strUserCreated='" + userCode + "' and strreasonCode='" + ResonCode + "' and "
			    + "Date(a.dteModifyVoidBill) between '" + fromDate + "' and '" + toDate + "' ");
		}
		else if (!"All".equals(cmbPosCode.getSelectedItem()) && "All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append("  and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " and  "
			    + "strreasonCode='" + ResonCode + "' and Date(a.dteModifyVoidBill) between '"
			    + fromDate + "' and '" + toDate + "' ");
		}
		else if ("All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append("  and a.strUserCreated='" + userCode + "' "
			    + "and strreasonCode='" + ResonCode + "' and Date(a.dteModifyVoidBill) "
			    + "between '" + fromDate + "' and '" + toDate + "' ");
		}
		else if (!"All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()) && "All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append("  and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " "
			    + "and strUserCreated='" + userCode + "'  and Date(a.dteModifyVoidBill) "
			    + "between '" + fromDate + "' and '" + toDate + "' ");
		}
		else if ("All".equals(cmbPosCode.getSelectedItem()) && "All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append("  and strreasonCode='" + ResonCode + "' "
			    + "and Date(a.dteModifyVoidBill) between '" + fromDate + "' and '" + toDate + "' ");
		}
		else if (!"All".equals(cmbPosCode.getSelectedItem()) && "All".equals(cmbUser.getSelectedItem()) && "All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append("  and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " and "
			    + "Date(a.dteModifyVoidBill) between '" + fromDate + "' and '" + toDate + "' ");
		}
		else if ("All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()) && "All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append("  and strUserCreated='" + userCode + "' and "
			    + "Date(a.dteModifyVoidBill) between '" + fromDate + "' and '" + toDate + "' ");
		}
		else
		{
		    sbSql.append("  and Date(a.dteModifyVoidBill) between '" + fromDate
			    + "' and '" + toDate + "' ");
		}
		if ("Item Void".equalsIgnoreCase(cmbSorting1.getSelectedItem().toString()))
		{
		    sbSql.append("and a.strVoidBillType ='" + cmbSorting1.getSelectedItem().toString() + "'");
		}
		else if ("Full Void".equalsIgnoreCase(cmbSorting1.getSelectedItem().toString()))
		{
		    sbSql.append("and a.strVoidBillType ='Bill Void'");
		}
		else
		{
		    sbSql.append("and (a.strVoidBillType = 'Bill Void' or a.strVoidBillType = 'ITEM VOID' )");
		}
		sbSql.append(" group by a.strBillNo,date(a.dteBillDate) ");
		if (cmbSorting.getSelectedItem().equals("BILL"))
		{
		    sbSql.append(" order by a.strBillNo");

		    //System.out.println(sbSql.toString());
		    ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
		    while (rs.next())
		    {
			double amountTemp = Double.parseDouble(rs.getString(6));
			String billNo = rs.getString(1);
			String sql = "Select count(*) from tblvoidmodifierdtl where strBillNo='" + billNo + "' ";
			ResultSet rs2 = clsGlobalVarClass.dbMysql.executeResultSet(sql);
			rs2.next();
			int count = rs2.getInt(1);
			rs2.close();
			if (count > 0)
			{
			    sql = "select ROUND(SUM(dblAmount))from tblvoidmodifierdtl where strBillNo ='" + billNo + "' ";
			    rs2 = clsGlobalVarClass.dbMysql.executeResultSet(sql);
			    rs2.next();
			    Double temp = rs2.getDouble(1);
			    rs2.close();
			    amountTemp = amountTemp + temp;
			}
			Object[] row =
			{
			    rs.getString(10), rs.getString(11), rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), gDecimalFormat.format(amountTemp), rs.getString(7), rs.getString(8), rs.getString(9)

			};
			dm1.addRow(row);

			sumTotalAmt = sumTotalAmt + amountTemp;

		    }

		}
		else
		{

		    ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
		    while (rs.next())
		    {
			clsBillItemDtlBean obj = new clsBillItemDtlBean();
			double amountTemp = Double.parseDouble(rs.getString(6));
			String billNo = rs.getString(1);
			//obj.setDblAmount(amountTemp);
			//obj.setStrBillNo(billNo);
			String sql = "Select count(*) from tblvoidmodifierdtl where strBillNo='" + billNo + "' ";
			ResultSet rs2 = clsGlobalVarClass.dbMysql.executeResultSet(sql);
			rs2.next();
			int count = rs2.getInt(1);
			rs2.close();
			if (count > 0)
			{
			    sql = "select ROUND(SUM(dblAmount))from tblvoidmodifierdtl where strBillNo ='" + billNo + "' ";
			    rs2 = clsGlobalVarClass.dbMysql.executeResultSet(sql);
			    rs2.next();
			    Double temp = rs2.getDouble(1);
			    rs2.close();
			    amountTemp = amountTemp + temp;
			    //obj.setDblAmount(amountTemp);
			}
			obj.setStrPosCode(rs.getString(10));
			obj.setStrPosName(rs.getString(11));
			obj.setStrBillNo(rs.getString(1));
			obj.setDteBillDate(rs.getString(2));
			obj.setDteVoidedDate(rs.getString(3));
			obj.setStrEntryTime(rs.getString(4));//
			obj.setStrVoidedTime(rs.getString(5));
			obj.setDblAmountTemp(amountTemp);
			obj.setStrUserEdited(rs.getString(7));
			obj.setStrReasonName(rs.getString(8));
			obj.setStrRemark(rs.getString(9));

			arrListVoidBillWise.add(obj);
			sumTotalAmt = sumTotalAmt + amountTemp;

		    }

		}
		Comparator<clsBillItemDtlBean> compareBillItem = new Comparator<clsBillItemDtlBean>()
		{

		    @Override
		    public int compare(clsBillItemDtlBean o1, clsBillItemDtlBean o2)
		    {
			double dblAmount = o1.getDblAmountTemp();
			double dblAmount2 = o2.getDblAmountTemp();

			if (dblAmount == dblAmount2)
			{
			    return 0;
			}
			else if (dblAmount > dblAmount2)
			{
			    return 1;
			}
			else
			{
			    return -1;
			}

		    }
		};
		Collections.sort(arrListVoidBillWise, compareBillItem);
		for (clsBillItemDtlBean obj : arrListVoidBillWise)
		{
		    Object[] row =
		    {
			obj.getStrPosCode(),
			obj.getStrPosName(),
			obj.getStrBillNo(),
			obj.getDteBillDate(),
			obj.getDteVoidedDate(),
			obj.getStrEntryTime(),
			obj.getStrVoidedTime(),
			gDecimalFormat.format(obj.getDblAmountTemp()),
			obj.getStrUserEdited(),
			obj.getStrReasonName(),
			obj.getStrRemark()
		    };
		    dm1.addRow(row);
		}

		Object[] total =
		{
		    "Total", "", gDecimalFormat.format(sumTotalAmt)
		};
		dm2.addRow(total);
		tblAuditFlash.setModel(dm1);
		tblTotal.setModel(dm2);
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
		tblAuditFlash.getColumnModel().getColumn(0).setPreferredWidth(0);
		tblAuditFlash.getColumnModel().getColumn(7).setPreferredWidth(40);
		tblAuditFlash.getColumnModel().getColumn(8).setPreferredWidth(50);
		tblAuditFlash.getColumnModel().getColumn(7).setCellRenderer(rightRenderer);

		DefaultTableCellRenderer rightRenderer1 = new DefaultTableCellRenderer();
		rightRenderer1.setHorizontalAlignment(JLabel.RIGHT);
		tblTotal.getColumnModel().getColumn(1).setCellRenderer(rightRenderer1);
		tblTotal.getColumnModel().getColumn(2).setCellRenderer(rightRenderer1);
		tblTotal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tblTotal.getColumnModel().getColumn(0).setPreferredWidth(460);
		tblTotal.getColumnModel().getColumn(1).setPreferredWidth(200);
		tblTotal.getColumnModel().getColumn(2).setPreferredWidth(95);
		//tblTotal.getColumnModel().getColumn(3).setPreferredWidth(130);
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	finally
	{
	    sbSql = null;
	    sbSqlMod = null;
	}
    }

/////////////////////////////////////////
    private void funVoidedAdvanceOrderBill()
    {

	StringBuilder sbSql = new StringBuilder();
	StringBuilder sbSqlMod = new StringBuilder();
	List<clsBillItemDtlBean> arrListVoidAdvOrder = new ArrayList<clsBillItemDtlBean>();
	try
	{
	    tblTotal.setVisible(true);
	    exportFormName = "Voided Advance Order Bill";
	    fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());

	    toDate = objUtility.funGetFromToDate(dteToDate.getDate());
	    String userCode = funGetSelectedUserCode(cmbUser.getSelectedItem().toString());
	    String POSCode = objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString().trim());
	    String ResonCode = funGetSelectedReasonCode(cmbReason.getSelectedItem().toString());
	    DefaultTableModel dm1 = new DefaultTableModel()
	    {
		@Override
		public boolean isCellEditable(int row, int column)
		{
		    //all cells false
		    return false;
		}
	    };
	    dm1.getDataVector().removeAllElements();
	    DefaultTableModel dm2 = new DefaultTableModel()
	    {
		@Override
		public boolean isCellEditable(int row, int column)
		{
		    //all cells false
		    return false;
		}
	    };
	    dm2.getDataVector().removeAllElements();
	    dm2.addColumn("");
	    dm2.addColumn("");
	    dm2.addColumn("");
	    //dm2.addColumn("");
	    tblAuditFlash.updateUI();
	    tblTotal.updateUI();
	    dm1.addColumn("Bill No");
	    dm1.addColumn("Bill Date");
	    dm1.addColumn("Voided Date");
	    dm1.addColumn("Entry Time");
	    dm1.addColumn("Voided Time");
	    dm1.addColumn("Item Name");
	    dm1.addColumn("Qty");
	    dm1.addColumn("Amt");
	    dm1.addColumn("User Edited");
	    dm1.addColumn("Reason");
	    dm1.addColumn("Remarks");
	    double sumQty = 0.00, sumTotalAmt = 0.00;

	    sbSql.setLength(0);
	    sbSql.append("select a.strBillNo,DATE_FORMAT(Date(a.dteBillDate) ,'%d-%m-%Y') as BillDate,DATE_FORMAT(Date(a.dteModifyVoidBill) ,'%d-%m-%Y') as VoidedDate,"
		    + "TIME_FORMAT(time(a.dteBillDate),'%h:%i') As EntryTime,TIME_FORMAT(time(a.dteModifyVoidBill),'%h:%i') VoidedTime,b.strItemName,"
		    + "b.intQuantity,b.dblAmount as BillAmount,a.strReasonName as Reason,a.strUserEdited as UserEdited,ifnull(a.strRemark,'') "
		    + " from tblvoidbillhd a,tblvoidbilldtl b where a.strBillNo=b.strBillNo");
	    if (!"Summary".equals(cmbType.getSelectedItem()))
	    {
		if (!"All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append(" and b.strTransType='AOVB' and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " "
			    + "and a.strUserCreated='" + userCode + "' and a.strreasonCode='" + ResonCode
			    + "' and Date(a.dteModifyVoidBill) between '" + fromDate + "' and '" + toDate + "'");
		}
		else if (!"All".equals(cmbPosCode.getSelectedItem()) && "All".equals(cmbUser.getSelectedItem()) && "All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append(" and b.strTransType='AOVB' and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " and "
			    + "Date(a.dteModifyVoidBill) between '" + fromDate + "' and '" + toDate + "'");
		}
		else if ("All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()) && "All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append(" and b.strTransType='AOVB' and a.strUserCreated='" + userCode + "' "
			    + "and Date(a.dteModifyVoidBill) between '" + fromDate + "' and '" + toDate + "'");
		}
		else if ("All".equals(cmbPosCode.getSelectedItem()) && "All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append(" and b.strTransType='AOVB' and a.strreasonCode='" + ResonCode + "' "
			    + "and Date(a.dteModifyVoidBill) between '" + fromDate + "' and '" + toDate + "'");
		}
		else if (!"All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()) && "All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append(" and b.strTransType='AOVB' and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " "
			    + "and a.strUserCreated='" + userCode + "' and Date(a.dteModifyVoidBill) "
			    + "between '" + fromDate + "' and '" + toDate + "'");
		}
		else if (!"All".equals(cmbPosCode.getSelectedItem()) && "All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append(" and b.strTransType='AOVB' and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " "
			    + "and a.strreasonCode='" + ResonCode + "' and Date(a.dteModifyVoidBill) "
			    + "between '" + fromDate + "' and '" + toDate + "'");
		}
		else if ("All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append(" and b.strTransType='AOVB' and a.strUserCreated='" + userCode + "' "
			    + "and a.strreasonCode='" + ResonCode + "' and Date(a.dteModifyVoidBill)"
			    + " between '" + fromDate + "' and '" + toDate + "'");
		}
		else
		{
		    sbSql.append(" and b.strTransType='AOVB' and Date(a.dteModifyVoidBill) "
			    + "between '" + fromDate + "' and '" + toDate + "'");
		}

		sbSql.append(" group by a.strBillNo,b.strItemCode");
//                if (cmbSorting.getSelectedItem().equals("BILL"))
//                {
//                    sbSql.append(" order by a.strBillNo");
//                }
//                else
//                {
//                    sbSql.append(" order by b.dblAmount");
//                }
		sbSqlMod.setLength(0);
		sbSqlMod.append("select a.strBillNo, DATE_FORMAT(Date(a.dteBillDate) ,'%d-%m-%Y') as BillDate,DATE_FORMAT(Date(a.dteModifyVoidBill) ,'%d-%m-%Y')  as VoidedDate,"
			+ "TIME_FORMAT(time(a.dteBillDate),'%h:%i') As EntryTime,TIME_FORMAT(time(a.dteModifyVoidBill),'%h:%i') VoidedTime,b.strModifierName,"
			+ "b.dblQuantity,b.dblAmount as BillAmount,a.strReasonName as Reason,a.strUserEdited as UserEdited,ifnull(a.strRemark,'') "
			+ "from tblvoidbillhd a,tblvoidmodifierdtl b where a.strBillNo=b.strBillNo ");

		if (!"Summary".equals(cmbType.getSelectedItem()))
		{
		    if (!"All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		    {
			sbSqlMod.append(" and a.strTransType='AOVB' and " + objUtility.funGetSelectedPOSCodeString("b.strPOSCode", selectedPOSCodeSet) + " "
				+ "and a.strUserCreated='" + userCode + "' and a.strreasonCode='" + ResonCode
				+ "' and Date(a.dteModifyVoidBill) between '" + fromDate + "' and '" + toDate + "'");
		    }
		    else if (!"All".equals(cmbPosCode.getSelectedItem()) && "All".equals(cmbUser.getSelectedItem()) && "All".equals(cmbReason.getSelectedItem()))
		    {
			sbSqlMod.append(" and a.strTransType='AOVB' and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " and "
				+ "Date(a.dteModifyVoidBill) between '" + fromDate + "' and '" + toDate + "'");
		    }
		    else if ("All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()) && "All".equals(cmbReason.getSelectedItem()))
		    {
			sbSqlMod.append(" and a.strTransType='AOVB' and a.strUserCreated='" + userCode + "' "
				+ "and Date(a.dteModifyVoidBill) between '" + fromDate + "' and '" + toDate + "'");
		    }
		    else if ("All".equals(cmbPosCode.getSelectedItem()) && "All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		    {
			sbSqlMod.append(" and a.strTransType='AOVB' and a.strreasonCode='" + ResonCode + "' "
				+ "and Date(a.dteModifyVoidBill) between '" + fromDate + "' and '" + toDate + "'");
		    }
		    else if (!"All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()) && "All".equals(cmbReason.getSelectedItem()))
		    {
			sbSqlMod.append(" and a.strTransType='AOVB' and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " "
				+ "and a.strUserCreated='" + userCode + "' and Date(a.dteModifyVoidBill) "
				+ "between '" + fromDate + "' and '" + toDate + "'");
		    }
		    else if (!"All".equals(cmbPosCode.getSelectedItem()) && "All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		    {
			sbSqlMod.append(" and a.strTransType='AOVB' and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " "
				+ "and a.strreasonCode='" + ResonCode + "' and Date(a.dteModifyVoidBill) "
				+ "between '" + fromDate + "' and '" + toDate + "'");
		    }
		    else if ("All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		    {
			sbSqlMod.append(" and a.strTransType='AOVB' and a.strUserCreated='" + userCode + "' "
				+ "and a.strreasonCode='" + ResonCode + "' and Date(a.dteModifyVoidBill)"
				+ " between '" + fromDate + "' and '" + toDate + "'");
		    }
		    else
		    {
			sbSqlMod.append(" and a.strTransType='AOVB' and Date(a.dteModifyVoidBill) "
				+ "between '" + fromDate + "' and '" + toDate + "'");
		    }

		    sbSqlMod.append(" group by a.strBillNo,b.strModifierCode");
//                    if (cmbSorting.getSelectedItem().equals("BILL"))
//                    {
//                        sbSqlMod.append(" order by a.strBillNo");
//                    }
//                    else
//                    {
//                        sbSqlMod.append(" order by b.dblAmount");
//                    }
		}

		String sql = sbSql.toString() + " union " + sbSqlMod.toString();
		//System.out.println(sql);
		ResultSet rsVoidBillDetail = clsGlobalVarClass.dbMysql.executeResultSet(sql);
		while (rsVoidBillDetail.next())
		{
		    Object[] row =
		    {
			rsVoidBillDetail.getString(1), rsVoidBillDetail.getString(2), rsVoidBillDetail.getString(3),
			rsVoidBillDetail.getString(4), rsVoidBillDetail.getString(5), rsVoidBillDetail.getString(6),
			rsVoidBillDetail.getString(7), gDecimalFormat.format(rsVoidBillDetail.getDouble(8)), rsVoidBillDetail.getString(9),
			rsVoidBillDetail.getString(10), rsVoidBillDetail.getString(11)
		    };
		    dm1.addRow(row);
		    sumQty = sumQty + rsVoidBillDetail.getDouble(7);
		    sumTotalAmt = sumTotalAmt + rsVoidBillDetail.getDouble(8);
		}
		rsVoidBillDetail.close();
		Object[] total =
		{
		    "Total", gDecimalFormat.format(sumQty), gDecimalFormat.format(sumTotalAmt)
		};
		dm2.addRow(total);
		tblAuditFlash.setModel(dm1);
		tblTotal.setModel(dm2);
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
		tblAuditFlash.getColumnModel().getColumn(5).setPreferredWidth(100);
		tblAuditFlash.getColumnModel().getColumn(6).setPreferredWidth(40);
		tblAuditFlash.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);
		tblAuditFlash.getColumnModel().getColumn(7).setCellRenderer(rightRenderer);
		DefaultTableCellRenderer rightRenderer1 = new DefaultTableCellRenderer();
		rightRenderer1.setHorizontalAlignment(JLabel.RIGHT);
		tblTotal.getColumnModel().getColumn(1).setCellRenderer(rightRenderer1);
		tblTotal.getColumnModel().getColumn(2).setCellRenderer(rightRenderer1);
		tblTotal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tblTotal.getColumnModel().getColumn(0).setPreferredWidth(460);
		tblTotal.getColumnModel().getColumn(1).setPreferredWidth(84);
		tblTotal.getColumnModel().getColumn(2).setPreferredWidth(95);
	    }
	    else
	    {
		dm1 = new DefaultTableModel()
		{
		    @Override
		    public boolean isCellEditable(int row, int column)
		    {
			//all cells false
			return false;
		    }
		};
		dm1.getDataVector().removeAllElements();
		dm2 = new DefaultTableModel()
		{
		    @Override
		    public boolean isCellEditable(int row, int column)
		    {
			//all cells false
			return false;
		    }
		};
		dm2.getDataVector().removeAllElements();
		dm2.addColumn("");
		dm2.addColumn("");
		dm2.addColumn("");
		tblAuditFlash.updateUI();
		tblTotal.updateUI();
		dm1.addColumn("Bill No");
		dm1.addColumn("Bill Date");
		dm1.addColumn("Voided Date");
		dm1.addColumn("Entry Time");
		dm1.addColumn("Voided Time");
		dm1.addColumn("Amt");
		dm1.addColumn("User Edited");
		dm1.addColumn("Reason");
		sb.setLength(0);
		sb.append("select a.strBillNo,DATE_FORMAT(Date(a.dteBillDate) ,'%d-%m-%Y')  as BillDate,DATE_FORMAT(Date(a.dteModifyVoidBill) ,'%d-%m-%Y') as VoidedDate,"
			+ "Time(a.dteBillDate) As EntryTime,Time(a.dteModifyVoidBill) VoidedTime, a.dblModifiedAmount,"
			+ "a.strUserEdited as UserEdited, a.strReasonName as Reason"
			+ " from tblvoidbillhd a ");
		if (!"All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		{
		    sb.append(" where a.strTransType='AOVB' and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " and "
			    + "strUserCreated='" + userCode + "' and strreasonCode='" + ResonCode + "' and "
			    + "Date(a.dteModifyVoidBill) between '" + fromDate + "' and '" + toDate + "' ");

		}
		else if (!"All".equals(cmbPosCode.getSelectedItem()) && "All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		{
		    sb.append(" where a.strTransType='AOVB' and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " and  "
			    + "strreasonCode='" + ResonCode + "' and Date(a.dteModifyVoidBill) between '"
			    + fromDate + "' and '" + toDate + "' ");

		}
		else if ("All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		{
		    sb.append(" where a.strTransType='AOVB' and a.strUserCreated='" + userCode + "' "
			    + "and strreasonCode='" + ResonCode + "' and Date(a.dteModifyVoidBill) "
			    + "between '" + fromDate + "' and '" + toDate + "' ");

		}
		else if (!"All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()) && "All".equals(cmbReason.getSelectedItem()))
		{
		    sb.append(" where a.strTransType='AOVB' and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " "
			    + "and strUserCreated='" + userCode + "'  and Date(a.dteModifyVoidBill) "
			    + "between '" + fromDate + "' and '" + toDate + "' ");

		}
		else if ("All".equals(cmbPosCode.getSelectedItem()) && "All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		{
		    sb.append(" where a.strTransType='AOVB' and strreasonCode='" + ResonCode + "' "
			    + "and Date(a.dteModifyVoidBill) between '" + fromDate + "' and '" + toDate + "' ");

		}
		else if (!"All".equals(cmbPosCode.getSelectedItem()) && "All".equals(cmbUser.getSelectedItem()) && "All".equals(cmbReason.getSelectedItem()))
		{
		    sb.append(" where a.strTransType='AOVB' and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " and "
			    + "Date(a.dteModifyVoidBill) between '" + fromDate + "' and '" + toDate + "' ");

		}
		else if ("All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()) && "All".equals(cmbReason.getSelectedItem()))
		{
		    sb.append(" where a.strTransType='AOVB' and strUserCreated='" + userCode + "' and "
			    + "Date(a.dteModifyVoidBill) between '" + fromDate + "' and '" + toDate + "' ");

		}
		else
		{
		    sb.append(" where a.strTransType='AOVB' and Date(a.dteModifyVoidBill) between '" + fromDate
			    + "' and '" + toDate + "' ");
		}

		sbSqlMod.append(" group by a.strBillNo");
		if (cmbSorting.getSelectedItem().equals("BILL"))
		{
		    //sbSqlMod.append(" order by a.strBillNo");
		    ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
		    while (rs.next())
		    {
			double amountTemp = Double.parseDouble(rs.getString(6));
			String billNo = rs.getString(1);
			sb.setLength(0);
			sb.append("Select count(*) from tblvoidmodifierdtl where strBillNo='" + billNo + "' ");
			ResultSet rs2 = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
			rs2.next();
			int count = rs2.getInt(1);
			rs2.close();
			if (count > 0)
			{
			    sb.setLength(0);
			    sb.append("select ROUND(SUM(dblAmount))from tblvoidmodifierdtl where strBillNo ='" + billNo + "' ");
			    rs2 = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
			    rs2.next();
			    Double temp = rs2.getDouble(1);
			    rs2.close();
			    amountTemp = amountTemp + temp;
			}
			Object[] row =
			{
			    rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), amountTemp, rs.getString(7), rs.getString(8)
			};
			dm1.addRow(row);
			sumTotalAmt = sumTotalAmt + amountTemp;

		    }
		}
		else
		{
		    ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
		    while (rs.next())
		    {
			clsBillItemDtlBean obj = new clsBillItemDtlBean();
			double amountTemp = Double.parseDouble(rs.getString(6));
			String billNo = rs.getString(1);
			sb.setLength(0);
			sb.append("Select count(*) from tblvoidmodifierdtl where strBillNo='" + billNo + "' ");
			ResultSet rs2 = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
			rs2.next();
			int count = rs2.getInt(1);
			rs2.close();
			if (count > 0)
			{
			    sb.setLength(0);
			    sb.append("select ROUND(SUM(dblAmount))from tblvoidmodifierdtl where strBillNo ='" + billNo + "' ");
			    rs2 = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
			    rs2.next();
			    Double temp = rs2.getDouble(1);
			    rs2.close();
			    amountTemp = amountTemp + temp;
			}

			obj.setStrBillNo(rs.getString(1));
			obj.setDteBillDate(rs.getString(2));
			obj.setDteVoidedDate(rs.getString(3));
			obj.setStrEntryTime(rs.getString(4));
			obj.setStrVoidedTime(rs.getString(5));
			obj.setDblModifiedAmount(amountTemp);
			obj.setStrUserEdited(rs.getString(7));
			obj.setStrReasonName(rs.getString(8));
			arrListVoidAdvOrder.add(obj);
			sumTotalAmt = sumTotalAmt + amountTemp;
		    }
		}
		Comparator<clsBillItemDtlBean> compareBillItem = new Comparator<clsBillItemDtlBean>()
		{
		    @Override
		    public int compare(clsBillItemDtlBean o1, clsBillItemDtlBean o2)
		    {
			double dblAmount = o1.getDblModifiedAmount();
			double dblAmount2 = o2.getDblModifiedAmount();

			if (dblAmount == dblAmount2)
			{
			    return 0;
			}
			else if (dblAmount > dblAmount2)
			{
			    return 1;
			}
			else
			{
			    return -1;
			}
		    }
		};
		Collections.sort(arrListVoidAdvOrder, compareBillItem);
		for (clsBillItemDtlBean obj : arrListVoidAdvOrder)
		{
		    Object[] row =
		    {
			obj.getStrBillNo(),
			obj.getDteBillDate(),
			obj.getDteVoidedDate(),
			obj.getStrEntryTime(),
			obj.getStrVoidedTime(),
			obj.getDblModifiedAmount(),
			obj.getStrUserEdited(),
			obj.getStrReasonName(),
		    };
		    dm1.addRow(row);
		    Object[] total =
		    {
			"Total", "", sumTotalAmt
		    };
		    dm2.addRow(total);
		    tblAuditFlash.setModel(dm1);
		    tblTotal.setModel(dm2);
		    DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		    rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
		    tblAuditFlash.getColumnModel().getColumn(5).setPreferredWidth(40);
		    tblAuditFlash.getColumnModel().getColumn(6).setPreferredWidth(50);
		    tblAuditFlash.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);

		    DefaultTableCellRenderer rightRenderer1 = new DefaultTableCellRenderer();
		    rightRenderer1.setHorizontalAlignment(JLabel.RIGHT);
		    tblTotal.getColumnModel().getColumn(1).setCellRenderer(rightRenderer1);
		    tblTotal.getColumnModel().getColumn(2).setCellRenderer(rightRenderer1);
		    tblTotal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		    tblTotal.getColumnModel().getColumn(0).setPreferredWidth(460);
		    tblTotal.getColumnModel().getColumn(1).setPreferredWidth(200);
		    tblTotal.getColumnModel().getColumn(2).setPreferredWidth(95);
		}
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

//////////////////////////////
    /**
     * this Function is used for Line Void
     *
     * @return
     */
    private void funLineVoidFlash()
    {

	StringBuilder sbSql = new StringBuilder();
	try
	{
	    tblTotal.setVisible(true);
	    exportFormName = "Line Void";
	    fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());

	    toDate = objUtility.funGetFromToDate(dteToDate.getDate());
	    String userCode = funGetSelectedUserCode(cmbUser.getSelectedItem().toString());
	    String POSCode = objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString().trim());
	    DefaultTableModel dm1 = new DefaultTableModel()
	    {
		@Override
		public boolean isCellEditable(int row, int column)
		{
		    //all cells false
		    return false;
		}
	    };
	    dm1.getDataVector().removeAllElements();
	    DefaultTableModel dm2 = new DefaultTableModel()
	    {
		@Override
		public boolean isCellEditable(int row, int column)
		{
		    //all cells false
		    return false;
		}
	    };
	    dm2.getDataVector().removeAllElements();
	    dm2.addColumn("");
	    dm2.addColumn("");
	    dm2.addColumn("");
	    dm2.addColumn("");
	    tblAuditFlash.updateUI();
	    tblTotal.updateUI();
	    dm1.addColumn("POS");
	    dm1.addColumn("Line Voided Date");
	    dm1.addColumn("Line Voided Time");
	    dm1.addColumn("Item Name");
	    dm1.addColumn("Qty");
	    dm1.addColumn("Amt");
	    dm1.addColumn("KOT No");
	    dm1.addColumn("User Created");

	    double sumQty = 0.00, sumTotalAmt = 0.00;

	    sbSql.setLength(0);
	    sbSql.append("select b.strPosName,DATE_FORMAT(Date(a.dteDateCreated),'%d-%m-%Y'),TIME_FORMAT(time(a.dteDateCreated),'%h:%i') "
		    + " ,a.strItemName,a.dblItemQuantity,a.dblAmount,a.strKOTNo,a.strUserCreated  "
		    + " from tbllinevoid a,tblposmaster b "
		    + " where a.strPosCode=b.strPosCode ");

	    if (!"All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()))
	    {
		sbSql.append(" and  a.strUserCreated='" + userCode + "' "
			+ "and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " and Date(a.dteDateCreated) between '" + fromDate + "' and '" + toDate + "'");
	    }
	    else if (!"All".equals(cmbPosCode.getSelectedItem()) && "All".equals(cmbUser.getSelectedItem()))
	    {
		sbSql.append(" and  " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " and Date(a.dteDateCreated) between '" + fromDate + "' and '" + toDate + "'");
	    }
	    else if ("All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()))
	    {
		sbSql.append(" and  a.strUserCreated='" + userCode + "' and Date(a.dteDateCreated) between '" + fromDate + "' and '" + toDate + "'");
	    }
	    else
	    {
		sbSql.append(" and  Date(a.dteDateCreated) between '" + fromDate + "' and '" + toDate + "'");
	    }
	    if ("Amount".equalsIgnoreCase(cmbSorting.getSelectedItem().toString()))
	    {
		sbSql.append(" order by a.dblAmount");
	    }

	    ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
	    while (rs.next())
	    {
		Object[] row =
		{
		    rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), gDecimalFormat.format(rs.getDouble(6)), rs.getString(7), rs.getString(8)
		};
		dm1.addRow(row);
		sumQty = sumQty + rs.getDouble(5);
		sumTotalAmt = sumTotalAmt + rs.getDouble(6);
	    }
	    rs.close();

	    Object[] total =
	    {
		"Total", decFormatForQty.format(sumQty), gDecimalFormat.format(sumTotalAmt), " "
	    };
	    dm2.addRow(total);
	    tblAuditFlash.setModel(dm1);
	    tblTotal.setModel(dm2);

	    DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
	    rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
	    tblAuditFlash.getColumnModel().getColumn(0).setPreferredWidth(10);
	    tblAuditFlash.getColumnModel().getColumn(1).setPreferredWidth(30);
	    tblAuditFlash.getColumnModel().getColumn(2).setPreferredWidth(60);
	    tblAuditFlash.getColumnModel().getColumn(3).setPreferredWidth(160);
	    tblAuditFlash.getColumnModel().getColumn(4).setPreferredWidth(20);
	    tblAuditFlash.getColumnModel().getColumn(5).setPreferredWidth(20);
	    tblAuditFlash.getColumnModel().getColumn(6).setPreferredWidth(50);
	    tblAuditFlash.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
	    tblAuditFlash.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
	    DefaultTableCellRenderer rightRenderer1 = new DefaultTableCellRenderer();
	    rightRenderer1.setHorizontalAlignment(JLabel.RIGHT);
	    tblTotal.getColumnModel().getColumn(1).setCellRenderer(rightRenderer1);
	    tblTotal.getColumnModel().getColumn(2).setCellRenderer(rightRenderer1);
	    tblTotal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    tblTotal.getColumnModel().getColumn(0).setPreferredWidth(280);
	    tblTotal.getColumnModel().getColumn(1).setPreferredWidth(84);
	    tblTotal.getColumnModel().getColumn(2).setPreferredWidth(80);
	    tblTotal.getColumnModel().getColumn(3).setPreferredWidth(250);
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	finally
	{
	    sbSql = null;
	}
    }
////////////////////////////////////

    /**
     * this Function is used for Void KOT
     *
     * @return
     */
    private void funVoidKOTFlash()
    {
	StringBuilder sbSql = new StringBuilder();
	try
	{
	    reportName = "KOT";
	    tblTotal.setVisible(true);
	    exportFormName = "Void Kot";
	    fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());

	    toDate = objUtility.funGetFromToDate(dteToDate.getDate());
	    String userCode = funGetSelectedUserCode(cmbUser.getSelectedItem().toString());
	    String POSCode = objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString().trim());
	    String reasonCode = funGetSelectedReasonCode(cmbReason.getSelectedItem().toString());
	    DefaultTableModel dm1 = new DefaultTableModel()
	    {
		@Override
		public boolean isCellEditable(int row, int column)
		{
		    //all cells false
		    return false;
		}
	    };
	    dm1.getDataVector().removeAllElements();
	    DefaultTableModel dm2 = new DefaultTableModel()
	    {
		@Override
		public boolean isCellEditable(int row, int column)
		{
		    //all cells false
		    return false;
		}
	    };
	    dm2.getDataVector().removeAllElements();
	    dm2.addColumn("");
	    dm2.addColumn("");
	    dm2.addColumn("");
	    dm2.addColumn("");
	    tblAuditFlash.updateUI();
	    tblTotal.updateUI();
	    dm1.addColumn("POS");
	    dm1.addColumn("Table");
	    dm1.addColumn("Waiter");
	    dm1.addColumn("KOT No");
	    dm1.addColumn("Item Name");
	    dm1.addColumn("Pax");
	    dm1.addColumn("Qty");
	    dm1.addColumn("Amount");
	    dm1.addColumn("Reason");
	    dm1.addColumn("User Created");
	    dm1.addColumn("Date Created");
	    dm1.addColumn("Remarks");
	    double sumQty = 0.00, sumTotalAmt = 0.00, pax = 0.00;

	    sbSql.setLength(0);
	    sbSql.append("select d.strPOSName,e.strTableName,b.strWShortName,a.strKOTNo "
		    + " ,a.strItemName,a.intPaxNo,a.dblItemQuantity,a.dblAmount,c.strReasonName "
		    + " ,a.strUserCreated,DATE_FORMAT(a.dteDateCreated,'%d-%m-%Y'),ifnull(a.strRemark,'') "
		    + " from tblvoidkot a left outer join tblwaitermaster b on a.strWaiterNo=b.strWaiterNo "
		    + " ,tblreasonmaster c,tblposmaster d,tbltablemaster e "
		    + " where a.strreasonCode=c.strreasonCode and a.strPOSCode=d.strPOSCode "
		    + " and a.strTableNo=e.strTableNo ");
	    if ("Item Void".equalsIgnoreCase(cmbSorting1.getSelectedItem().toString()))
	    {
		sbSql.append("and a.strVoidBillType ='" + cmbSorting1.getSelectedItem().toString() + "'");
	    }
	    else if ("Full Void".equalsIgnoreCase(cmbSorting1.getSelectedItem().toString()))
	    {
		sbSql.append("and a.strVoidBillType ='Full KOT Void'");
	    }
	    else
	    {
		sbSql.append("and (a.strVoidBillType = 'Full KOT Void' or a.strVoidBillType = 'ITEM VOID' )");
	    }
	    if (!"Summary".equals(cmbType.getSelectedItem()))
	    {
		if (!"All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append(" and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " and a.strUserCreated='" + userCode + "' "
			    + "and Date(a.dteDateCreated) between '" + fromDate + "' and '" + toDate + "' "
			    + "and a.strreasonCode='" + reasonCode + "'");
		}
		else if (!"All".equals(cmbPosCode.getSelectedItem()) && "All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append(" and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " and Date(a.dteDateCreated) between '"
			    + fromDate + "' and '" + toDate + "' and a.strreasonCode='" + reasonCode + "'");
		}
		else if ("All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append(" and a.strUserCreated='" + userCode + "' and Date(a.dteDateCreated) between '"
			    + fromDate + "' and '" + toDate + "' and a.strreasonCode='" + reasonCode + "'");
		}
		else if (!"All".equals(cmbPosCode.getSelectedItem()) && "All".equals(cmbUser.getSelectedItem()) && "All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append(" and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " and Date(a.dteDateCreated) between '"
			    + fromDate + "' and '" + toDate + "'");
		}
		else if ("All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append(" and a.strUserCreated='" + userCode + "' and Date(a.dteDateCreated) between '"
			    + fromDate + "' and '" + toDate + "' and a.strreasonCode='" + reasonCode + "'");
		}
		else if ("All".equals(cmbPosCode.getSelectedItem()) && "All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append(" and a.strreasonCode='" + reasonCode + "' and Date(a.dteDateCreated) between '"
			    + fromDate + "' and '" + toDate + "'");
		}
		else
		{
		    sbSql.append(" and Date(a.dteDateCreated) between '" + fromDate + "' and '" + toDate + "'");
		}
		if ("Amount".equalsIgnoreCase(cmbSorting.getSelectedItem().toString()))
		{
		    sbSql.append(" order by a.dblAmount");
		}

		ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
		while (rs.next())
		{
		    Object[] row =
		    {
			rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7), gDecimalFormat.format(rs.getDouble(8)), rs.getString(9), rs.getString(10), rs.getString(11), rs.getString(12)
		    };
		    dm1.addRow(row);
		    pax = pax + rs.getDouble(6);
		    sumQty = sumQty + rs.getDouble(7);
		    sumTotalAmt = sumTotalAmt + rs.getDouble(8);

		}
		Object[] total =
		{
		    "Total", decFormatForQty.format(pax), decFormatForQty.format(sumQty), gDecimalFormat.format(sumTotalAmt)
		};
		dm2.addRow(total);
		tblAuditFlash.setModel(dm1);
		tblTotal.setModel(dm2);
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
		tblAuditFlash.getColumnModel().getColumn(0).setPreferredWidth(40);
		tblAuditFlash.getColumnModel().getColumn(1).setPreferredWidth(55);
		tblAuditFlash.getColumnModel().getColumn(3).setPreferredWidth(65);
		tblAuditFlash.getColumnModel().getColumn(5).setPreferredWidth(100);
		tblAuditFlash.getColumnModel().getColumn(4).setPreferredWidth(30);
		tblAuditFlash.getColumnModel().getColumn(6).setPreferredWidth(40);
		tblAuditFlash.getColumnModel().getColumn(7).setPreferredWidth(50);
		tblAuditFlash.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
		tblAuditFlash.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);
		tblAuditFlash.getColumnModel().getColumn(7).setCellRenderer(rightRenderer);

		DefaultTableCellRenderer rightRenderer1 = new DefaultTableCellRenderer();
		rightRenderer1.setHorizontalAlignment(JLabel.RIGHT);
		tblTotal.getColumnModel().getColumn(1).setCellRenderer(rightRenderer1);
		tblTotal.getColumnModel().getColumn(2).setCellRenderer(rightRenderer1);
		tblTotal.getColumnModel().getColumn(3).setCellRenderer(rightRenderer1);
		tblTotal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tblTotal.getColumnModel().getColumn(0).setPreferredWidth(240);
		tblTotal.getColumnModel().getColumn(1).setPreferredWidth(84);
		tblTotal.getColumnModel().getColumn(2).setPreferredWidth(130);
		tblTotal.getColumnModel().getColumn(3).setPreferredWidth(90);
		tblTotal.getColumnModel().getColumn(4).setPreferredWidth(230);

	    }
	    else
	    {
		dm1 = new DefaultTableModel()
		{
		    @Override
		    public boolean isCellEditable(int row, int column)
		    {
			//all cells false
			return false;
		    }
		};
		dm1.getDataVector().removeAllElements();
		dm2 = new DefaultTableModel()
		{
		    @Override
		    public boolean isCellEditable(int row, int column)
		    {
			//all cells false
			return false;
		    }
		};
		dm2.getDataVector().removeAllElements();
		dm2.addColumn("");
		dm2.addColumn("");
		dm2.addColumn("");
		dm2.addColumn("");

		tblAuditFlash.updateUI();
		tblTotal.updateUI();
		dm1.addColumn("POS");
		dm1.addColumn("Table");
		dm1.addColumn("Waiter");
		dm1.addColumn("KOT No");
		dm1.addColumn("Pax");
		dm1.addColumn("Amount");
		dm1.addColumn("Reason");
		dm1.addColumn("User Created");
		dm1.addColumn("Date Created");
		dm1.addColumn("Remarks");
		sumQty = 0.00;
		sumTotalAmt = 0.00;
		pax = 0.00;

		sbSql.setLength(0);
		sbSql.append("select d.strPOSName,e.strTableName,b.strWShortName,a.strKOTNo,a.intPaxNo,"
			+ " sum(a.dblAmount),c.strReasonName,a.strUserCreated,DATE_FORMAT(a.dteDateCreated,'%d-%m-%Y'),ifnull(a.strRemark,'') "
			+ " from tblvoidkot a left outer join tblwaitermaster b on a.strWaiterNo=b.strWaiterNo "
			+ ",tblreasonmaster c,tblposmaster d,tbltablemaster e "
			+ " where a.strreasonCode=c.strreasonCode "
			+ " and a.strPOSCode=d.strPOSCode and a.strTableNo=e.strTableNo ");
		if ("Item Void".equalsIgnoreCase(cmbSorting1.getSelectedItem().toString()))
		{
		    sbSql.append("and a.strVoidBillType ='" + cmbSorting1.getSelectedItem().toString() + "'");
		}
		else if ("Full Void".equalsIgnoreCase(cmbSorting1.getSelectedItem().toString()))
		{
		    sbSql.append("and a.strVoidBillType ='Full KOT Void'");
		}
		else
		{
		    sbSql.append("and (a.strVoidBillType = 'Full KOT Void' or a.strVoidBillType = 'ITEM VOID' )");
		}
		if (!"All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append(" and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " and a.strUserCreated='" + userCode + "' "
			    + "and Date(a.dteDateCreated) between '" + fromDate + "' and '" + toDate + "' "
			    + "and a.strreasonCode='" + reasonCode + "'");
		}
		else if (!"All".equals(cmbPosCode.getSelectedItem()) && "All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append(" and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " and Date(a.dteDateCreated) between '"
			    + fromDate + "' and '" + toDate + "' and a.strreasonCode='" + reasonCode + "'");
		}
		else if ("All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append(" and a.strUserCreated='" + userCode + "' and Date(a.dteDateCreated) between '"
			    + fromDate + "' and '" + toDate + "' and a.strreasonCode='" + reasonCode + "'");
		}
		else if (!"All".equals(cmbPosCode.getSelectedItem()) && "All".equals(cmbUser.getSelectedItem()) && "All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append(" and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " and Date(a.dteDateCreated) between '"
			    + fromDate + "' and '" + toDate + "'");
		}
		else if ("All".equals(cmbPosCode.getSelectedItem()) && !"All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append(" and a.strUserCreated='" + userCode + "' and Date(a.dteDateCreated) between '"
			    + fromDate + "' and '" + toDate + "' and a.strreasonCode='" + reasonCode + "'");
		}
		else if ("All".equals(cmbPosCode.getSelectedItem()) && "All".equals(cmbUser.getSelectedItem()) && !"All".equals(cmbReason.getSelectedItem()))
		{
		    sbSql.append(" and a.strreasonCode='" + reasonCode + "' and Date(a.dteDateCreated) between '"
			    + fromDate + "' and '" + toDate + "'");
		}
		else
		{
		    sbSql.append(" and Date(a.dteDateCreated) between '" + fromDate + "' and '" + toDate + "'");
		}
		sbSql.append(" Group By a.strPOSCode,a.strTableNo,b.strWShortName,a.strKOTNo,a.intPaxNo,"
			+ "c.strReasonName,a.strUserCreated");
		if ("Amount".equalsIgnoreCase(cmbSorting.getSelectedItem().toString()))
		{
		    sbSql.append(" order by sum(a.dblAmount)");
		}
		//System.out.println(sbSql.toString());
		ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
		while (rs.next())
		{
		    Object[] row =
		    {
			rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
			rs.getString(5), gDecimalFormat.format(rs.getDouble(6)), rs.getString(7), rs.getString(8), rs.getString(9), rs.getString(10)
		    };
		    dm1.addRow(row);
		    pax = pax + rs.getDouble(5);
		    sumTotalAmt = sumTotalAmt + rs.getDouble(6);
		}
		Object[] total =
		{
		    "Total", decFormatForQty.format(pax), gDecimalFormat.format(sumTotalAmt), " "
		};
		dm2.addRow(total);
		tblAuditFlash.setModel(dm1);
		tblTotal.setModel(dm2);
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
		tblAuditFlash.getColumnModel().getColumn(0).setPreferredWidth(30);
		tblAuditFlash.getColumnModel().getColumn(1).setPreferredWidth(35);
		tblAuditFlash.getColumnModel().getColumn(2).setPreferredWidth(55);
		tblAuditFlash.getColumnModel().getColumn(3).setPreferredWidth(45);
		tblAuditFlash.getColumnModel().getColumn(4).setPreferredWidth(20);
		tblAuditFlash.getColumnModel().getColumn(5).setPreferredWidth(40);
		tblAuditFlash.getColumnModel().getColumn(6).setPreferredWidth(100);
		tblAuditFlash.getColumnModel().getColumn(7).setPreferredWidth(50);
		tblAuditFlash.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
		tblAuditFlash.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);

		DefaultTableCellRenderer rightRenderer1 = new DefaultTableCellRenderer();
		rightRenderer1.setHorizontalAlignment(JLabel.RIGHT);
		tblTotal.getColumnModel().getColumn(1).setCellRenderer(rightRenderer1);
		tblTotal.getColumnModel().getColumn(2).setCellRenderer(rightRenderer1);
		tblTotal.getColumnModel().getColumn(3).setCellRenderer(rightRenderer1);
		tblTotal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tblTotal.getColumnModel().getColumn(0).setPreferredWidth(310);
		tblTotal.getColumnModel().getColumn(1).setPreferredWidth(60);
		tblTotal.getColumnModel().getColumn(2).setPreferredWidth(80);
		tblTotal.getColumnModel().getColumn(3).setPreferredWidth(330);
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	finally
	{
	    sbSql = null;
	}
    }

    private void funPreviousButtonClicked()
    {
	try
	{
	    if (btnPrevious.isEnabled())
	    {
		navigate--;
	    }
	    if (navigate == 0)
	    {
		btnModifiedBill.setMnemonic('1');
		btnVoidedBill.setMnemonic('2');
		btnVoidedAdvOrder.setMnemonic('3');
		btnLineVoid.setMnemonic('4');
		btnVoidKot.setMnemonic('5');
		btnTimeAudit.setMnemonic('6');
		btnKOTAnalysis.setMnemonic('7');
		btnMovedKot.setMnemonic('8');

		btnModifiedBill.setVisible(true);
		btnModifiedBill.setText("<html>Moved<br>KOT </html>");
		btnModifiedBill.setEnabled(true);

		btnVoidedBill.setVisible(true);
		btnVoidedBill.setText("<html>Voided <br>Bill</html>");
		btnVoidedBill.setEnabled(true);

		btnVoidedAdvOrder.setVisible(true);
		btnVoidedAdvOrder.setText("<html>Voided<br>Advance<br>Order</html>");
		btnVoidedAdvOrder.setEnabled(true);

		btnLineVoid.setVisible(true);
		btnLineVoid.setText("<html>Line<br>Voids</html>");
		btnLineVoid.setEnabled(true);

		btnVoidKot.setVisible(true);
		btnVoidKot.setText("<html>Voided<br>KOT </html>");
		btnVoidKot.setEnabled(true);

		btnTimeAudit.setVisible(true);
		btnTimeAudit.setText("<html>Time<br>Audit</html>");
		btnTimeAudit.setEnabled(true);

		btnKOTAnalysis.setVisible(true);
		btnKOTAnalysis.setText("<html>KOT<br>Analysis</html>");
		btnKOTAnalysis.setEnabled(true);

		btnMovedKot.setVisible(true);
		btnMovedKot.setText("<html>Moved<br>KOT </html>");
		btnMovedKot.setEnabled(true);

		btnNext.setEnabled(true);
	    }

	    if (navigate == 1)
	    {
		btnModifiedBill.setMnemonic('9');
		btnModifiedBill.setMnemonic('a');
		btnModifiedBill.setMnemonic('b');
		btnModifiedBill.setMnemonic('c');
		btnModifiedBill.setMnemonic('d');
		btnModifiedBill.setMnemonic('e');
		btnModifiedBill.setMnemonic('f');
		btnModifiedBill.setMnemonic('g');

		btnPrevious.setEnabled(true);
		btnNext.setEnabled(false);
	    }
	}
	catch (Exception e)
	{
	    objUtility.funWriteErrorLog(e);
	    e.printStackTrace();
	}
    }

    private void funNextButtonClicked()
    {
	try
	{

	    if (btnNext.isEnabled())
	    {
		navigate++;
	    }
	    if (navigate == 0)
	    {
		btnModifiedBill.setMnemonic('1');
		btnVoidedBill.setMnemonic('2');
		btnVoidedAdvOrder.setMnemonic('3');
		btnLineVoid.setMnemonic('4');
		btnVoidKot.setMnemonic('5');
		btnTimeAudit.setMnemonic('6');
		btnKOTAnalysis.setMnemonic('7');
		btnMovedKot.setMnemonic('8');

		btnModifiedBill.setVisible(true);
		btnModifiedBill.setText("<html>Moved<br>KOT </html>");

		btnVoidedBill.setVisible(true);
		btnVoidedBill.setText("<html>Voided <br>Bill</html>");

		btnVoidedAdvOrder.setVisible(true);
		btnVoidedAdvOrder.setText("<html>Voided<br>Advance<br>Order</html>");

		btnLineVoid.setVisible(true);
		btnLineVoid.setText("<html>Line<br>Voids</html>");

		btnVoidKot.setVisible(true);
		btnVoidKot.setText("<html>Voided<br>KOT </html>");

		btnTimeAudit.setVisible(true);
		btnTimeAudit.setText("<html>Time<br>Audit</html>");

		btnKOTAnalysis.setVisible(true);
		btnKOTAnalysis.setText("<html>KOT<br>Analysis</html>");

		btnMovedKot.setVisible(true);
		btnMovedKot.setText("<html>Moved<br>KOT </html>");

		btnPrevious.setEnabled(false);

		btnNext.setEnabled(true);
	    }
	    if (navigate == 1)
	    {
		btnModifiedBill.setMnemonic('9');
		btnModifiedBill.setMnemonic('a');
		btnModifiedBill.setMnemonic('b');
		btnModifiedBill.setMnemonic('c');
		btnModifiedBill.setMnemonic('d');
		btnModifiedBill.setMnemonic('e');
		btnModifiedBill.setMnemonic('f');
		btnModifiedBill.setMnemonic('g');
		btnPrevious.setEnabled(true);

		btnModifiedBill.setVisible(true);
		btnModifiedBill.setText("<html>Waiter<br>Audit </html>");

		btnVoidedBill.setText("");
		btnVoidedBill.setEnabled(false);

		btnVoidedAdvOrder.setText("");
		btnVoidedAdvOrder.setEnabled(false);

		btnLineVoid.setText("");
		btnLineVoid.setEnabled(false);

		btnVoidKot.setText("");
		btnVoidKot.setEnabled(false);

		btnTimeAudit.setText("");
		btnTimeAudit.setEnabled(false);

		btnKOTAnalysis.setText("");
		btnKOTAnalysis.setEnabled(false);

		btnMovedKot.setText("");
		btnMovedKot.setEnabled(false);

		btnNext.setEnabled(false);
	    }
	}
	catch (Exception e)
	{
	    objUtility.funWriteErrorLog(e);
	    e.printStackTrace();
	}
    }

    private void funButtonAction(JButton objButton)
    {
	try
	{
	    String buttonName = objButton.getText();
	    switch (buttonName)
	    {
		case "<html>Modified <br>Bill</html>":
		    funModifiedBillFlash();
		    break;
		case "<html>Waiter<br>Audit </html>":
		    funWaiterWiseKOT();
		    break;

	    }
	}
	catch (Exception e)
	{
	    objUtility.funWriteErrorLog(e);
	    e.printStackTrace();
	}
    }

    public void funWaiterWiseKOT()
    {

	if (cmbType.getSelectedItem().toString().equalsIgnoreCase("Summary"))
	{
	    funSummaryWaiterWiseKOTAnalysis();
	}

    }

    private void funSummaryWaiterWiseKOTAnalysis()
    {
	StringBuilder sbSqlLive = new StringBuilder();
	StringBuilder sbSqlQFile = new StringBuilder();
	StringBuilder sbFilters = new StringBuilder();
	Map<String, clsWaiterAnalysisBean> mapWaiterWise = new HashMap();
	Map<String, String> mapKotsSave = new HashMap();
	try
	{
	    tblTotal.setVisible(true);
	    exportFormName = "Summary KOT Analysis";
	    reportName = "Summary KOT Analysis";
	    fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());
	    toDate = objUtility.funGetFromToDate(dteToDate.getDate());

	    DefaultTableModel dmTableColoum = new DefaultTableModel()
	    {
		@Override
		public boolean isCellEditable(int row, int column)
		{
		    //all cells false
		    return false;
		}
	    };

	    dmTableColoum.addColumn("Waiter Name");
	    dmTableColoum.addColumn("No Of KOT");
	    dmTableColoum.addColumn("No Of Void KOT");
	    dmTableColoum.addColumn("No Of Void KOT%");
	    dmTableColoum.addColumn("No Of Move KOT");
	    dmTableColoum.addColumn("No Of Move KOT%");

	    DefaultTableModel dmTotal = new DefaultTableModel()
	    {
		@Override
		public boolean isCellEditable(int row, int column)
		{
		    //all cells false
		    return false;
		}
	    };
	    dmTotal.addColumn("");
	    dmTotal.addColumn("");
	    dmTotal.addColumn("");
	    dmTotal.addColumn("");
	    dmTotal.addColumn("");
	    dmTotal.addColumn("");

	    String userCode = funGetSelectedUserCode(cmbUser.getSelectedItem().toString());
	    String reasonCode = funGetSelectedReasonCode(cmbReason.getSelectedItem().toString());
	    List<clsWaiterAnalysisBean> listOfWaiterAnalysis = new LinkedList<clsWaiterAnalysisBean>();

	    sbSqlLive.setLength(0);
	    sbSqlQFile.setLength(0);
	    sbFilters.setLength(0);
	    String operation = "Billed KOT";

	    if (!"All".equals(cmbPosCode.getSelectedItem().toString()))
	    {
		sbFilters.append("and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " ");
	    }
	    if (!"All".equals(cmbUser.getSelectedItem().toString()))
	    {
		sbFilters.append(" and a.strUserCreated='" + userCode + "' ");
	    }
	    if (cmbReason.getSelectedItem().toString() != "All")
	    {
		sbFilters.append(" and a.strReasonCode='" + reasonCode + "' ");
	    }

	    //live billed KOTs
	    sbSqlLive.append("SELECT d.strWaiterNo,d.strWShortName,d.strWFullName,b.strKOTNo\n"
		    + "FROM tblbillhd a,tblbilldtl b,tbltablemaster c,tblwaitermaster d\n"
		    + "WHERE a.strBillNo=b.strBillNo AND DATE(a.dteBillDate)= DATE(b.dteBillDate) AND a.strTableNo=c.strTableNo "
		    + "AND b.strWaiterNo=d.strWaiterNo AND LENGTH(b.strKOTNo)>0 AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' and '" + toDate + "'\n ");

	    sbSqlLive.append(sbFilters);

	    sbSqlLive.append("GROUP BY b.strWaiterNo,b.strKOTNo\n"
		    + "order by d.strWShortName,b.strKOTNo");

	    ResultSet rsBilledKOTs = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
	    while (rsBilledKOTs.next())
	    {
		String waiterNo = rsBilledKOTs.getString(1);
		String kotNo = rsBilledKOTs.getString(4);

		if (mapWaiterWise.containsKey(waiterNo))
		{
		    clsWaiterAnalysisBean objWaiterAnalysisBean = mapWaiterWise.get(waiterNo);
		    if (!mapKotsSave.containsKey(kotNo))
		    {
			objWaiterAnalysisBean.setNoOfKot(objWaiterAnalysisBean.getNoOfKot() + 1);
		    }
		}
		else
		{
		    clsWaiterAnalysisBean objWaiterAnalysisBean = new clsWaiterAnalysisBean();

		    objWaiterAnalysisBean.setStrWaiterNo(rsBilledKOTs.getString(1));
		    objWaiterAnalysisBean.setStrWaiterName(rsBilledKOTs.getString(3));
		    objWaiterAnalysisBean.setNoOfKot(1);
		    objWaiterAnalysisBean.setNoOfVoidKot(0);
		    objWaiterAnalysisBean.setNoOfMoveKot(0);
		    listOfWaiterAnalysis.add(objWaiterAnalysisBean);
		    mapWaiterWise.put(waiterNo, objWaiterAnalysisBean);
		    mapKotsSave.put(kotNo, kotNo);
		}
	    }
	    rsBilledKOTs.close();

	    //Q billed KOTs
	    sbSqlQFile.append("SELECT d.strWaiterNo,d.strWShortName,d.strWFullName,b.strKOTNo\n"
		    + "FROM tblqbillhd a,tblqbilldtl b,tbltablemaster c,tblwaitermaster d\n"
		    + "WHERE a.strBillNo=b.strBillNo AND DATE(a.dteBillDate)= DATE(b.dteBillDate) AND a.strTableNo=c.strTableNo "
		    + "AND b.strWaiterNo=d.strWaiterNo AND LENGTH(b.strKOTNo)>0 AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' and '" + toDate + "'\n ");

	    sbSqlQFile.append(sbFilters);
	    sbSqlQFile.append("GROUP BY b.strWaiterNo,b.strKOTNo\n"
		    + "order by d.strWShortName,b.strKOTNo");

	    rsBilledKOTs = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFile.toString());
	    while (rsBilledKOTs.next())
	    {

		String waiterNo = rsBilledKOTs.getString(1);
		String kotNo = rsBilledKOTs.getString(4);

		if (mapWaiterWise.containsKey(waiterNo))
		{
		    clsWaiterAnalysisBean objWaiterAnalysisBean = mapWaiterWise.get(waiterNo);
		    if (!mapKotsSave.containsKey(kotNo))
		    {
			objWaiterAnalysisBean.setNoOfKot(objWaiterAnalysisBean.getNoOfKot() + 1);
		    }
		}
		else
		{
		    clsWaiterAnalysisBean objWaiterAnalysisBean = new clsWaiterAnalysisBean();

		    objWaiterAnalysisBean.setStrWaiterNo(rsBilledKOTs.getString(1));
		    objWaiterAnalysisBean.setStrWaiterName(rsBilledKOTs.getString(3));
		    objWaiterAnalysisBean.setNoOfKot(1);
		    objWaiterAnalysisBean.setNoOfVoidKot(0);
		    objWaiterAnalysisBean.setNoOfMoveKot(0);
		    listOfWaiterAnalysis.add(objWaiterAnalysisBean);
		    mapWaiterWise.put(waiterNo, objWaiterAnalysisBean);
		    mapKotsSave.put(kotNo, kotNo);
		}
	    }
	    rsBilledKOTs.close();

	    //voided KOTs
	    sbSqlLive.setLength(0);
	    sbSqlQFile.setLength(0);
	    sbFilters.setLength(0);
	    operation = "Void KOT";

	    if (!"All".equals(cmbPosCode.getSelectedItem().toString()))
	    {
		sbFilters.append("and " + objUtility.funGetSelectedPOSCodeString("a.strPOSCode", selectedPOSCodeSet) + " ");
	    }
	    if (!"All".equals(cmbUser.getSelectedItem().toString()))
	    {
		sbFilters.append(" and a.strUserCreated='" + userCode + "' ");
	    }
	    if (cmbReason.getSelectedItem().toString() != "All")
	    {
		sbFilters.append(" and a.strReasonCode='" + reasonCode + "' ");
	    }

	    sbSqlLive.append("SELECT c.strWaiterNo,c.strWShortName,c.strWFullName,a.strType strOperationType,a.strKOTNo\n"
		    + "FROM tblvoidkot a,tbltablemaster b,tblwaitermaster c,tblreasonmaster d\n"
		    + "WHERE a.strTableNo=b.strTableNo AND a.strWaiterNo=c.strWaiterNo AND a.strReasonCode=d.strReasonCode \n"
		    + "AND LENGTH(a.strKOTNo)>2 AND DATE(a.dteDateCreated) BETWEEN '" + fromDate + "' and '" + toDate + "' ");

	    sbSqlLive.append(sbFilters);
	    sbSqlLive.append("group by a.strWaiterNo,a.strKOTNo "
		    + "order by c.strWFullName,a.strKOTNo");
	    ResultSet rsVoidedKOT = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
	    double noOfKot = 0.0, noOfVoidKotPer = 0.0, noOfMoveKotPer = 0.0;
	    while (rsVoidedKOT.next())
	    {
		clsWaiterAnalysisBean objWaiterAnalysisBean = new clsWaiterAnalysisBean();

		if (rsVoidedKOT.getString(4).equalsIgnoreCase("VKot"))
		{
		    operation = "Void KOT";
		}
		else if (rsVoidedKOT.getString(4).equalsIgnoreCase("MVKot"))
		{
		    operation = "Move KOT";
		}
		else
		{
		    operation = "Void KOT";
		}

		String waiterNo = rsVoidedKOT.getString(1);
		String kotNo = rsVoidedKOT.getString(5);

		if (mapWaiterWise.containsKey(waiterNo))
		{
		    objWaiterAnalysisBean = mapWaiterWise.get(waiterNo);
		    if (operation.equalsIgnoreCase("Void KOT"))
		    {
			objWaiterAnalysisBean.setNoOfVoidKot(objWaiterAnalysisBean.getNoOfVoidKot() + 1);
		    }
		    else
		    {
			objWaiterAnalysisBean.setNoOfMoveKot(objWaiterAnalysisBean.getNoOfMoveKot() + 1);
		    }
		    if (!mapKotsSave.containsKey(kotNo))
		    {
			objWaiterAnalysisBean.setNoOfKot(objWaiterAnalysisBean.getNoOfKot() + 1);
		    }
		}
		else
		{
		    objWaiterAnalysisBean.setStrWaiterNo(rsVoidedKOT.getString(1));
		    objWaiterAnalysisBean.setStrWaiterName(rsVoidedKOT.getString(3));
		    objWaiterAnalysisBean.setNoOfKot(1);
		    if (operation.equalsIgnoreCase("Void KOT"))
		    {
			objWaiterAnalysisBean.setNoOfVoidKot(1);
		    }
		    else
		    {
			objWaiterAnalysisBean.setNoOfMoveKot(1);
		    }
		    listOfWaiterAnalysis.add(objWaiterAnalysisBean);
		    mapWaiterWise.put(waiterNo, objWaiterAnalysisBean);
		    mapKotsSave.put(kotNo, kotNo);
		}
	    }
	    rsVoidedKOT.close();

	    //sorting
	    Comparator<clsWaiterAnalysisBean> kotComaparator = new Comparator<clsWaiterAnalysisBean>()
	    {

		@Override
		public int compare(clsWaiterAnalysisBean o1, clsWaiterAnalysisBean o2)
		{
		    return o1.getStrWaiterName().compareToIgnoreCase(o2.getStrWaiterName());
		}
	    };
	    Collections.sort(listOfWaiterAnalysis, kotComaparator);
	    //sorting//

	    //fill table data
	    DecimalFormat df1 = new DecimalFormat("0");
	    double totNoOfKot = 0.0, totNoOfVoidKot = 0.0, totNoOfMoveKot = 0.0;
	    for (Map.Entry<String, clsWaiterAnalysisBean> entrySet : mapWaiterWise.entrySet())
	    {
		clsWaiterAnalysisBean objWaiterAnalysisBean = entrySet.getValue();

		noOfKot = objWaiterAnalysisBean.getNoOfKot();
		totNoOfKot = totNoOfKot + objWaiterAnalysisBean.getNoOfKot();
		noOfVoidKotPer = objWaiterAnalysisBean.getNoOfVoidKot();
		totNoOfVoidKot = totNoOfVoidKot + objWaiterAnalysisBean.getNoOfVoidKot();
		noOfMoveKotPer = objWaiterAnalysisBean.getNoOfMoveKot();
		totNoOfMoveKot = totNoOfMoveKot + objWaiterAnalysisBean.getNoOfMoveKot();
		if (noOfKot > 0)
		{
		    noOfVoidKotPer = ((noOfVoidKotPer / noOfKot) * 100);
		    noOfMoveKotPer = ((noOfMoveKotPer / noOfKot) * 100);
		}

		Object row[] =
		{
		    objWaiterAnalysisBean.getStrWaiterName(), df1.format(noOfKot), df1.format(objWaiterAnalysisBean.getNoOfVoidKot()), gDecimalFormat.format(noOfVoidKotPer), df1.format(objWaiterAnalysisBean.getNoOfMoveKot()), gDecimalFormat.format(noOfMoveKotPer)
		};

		dmTableColoum.addRow(row);
	    }

	    Object totalRow[] =
	    {
		"Total", df1.format(totNoOfKot), df1.format(totNoOfVoidKot), "", df1.format(totNoOfMoveKot), ""
	    };
	    dmTotal.addRow(totalRow);

	    tblTotal.setModel(dmTotal);
	    tblAuditFlash.setModel(dmTableColoum);
	    tblAuditFlash.setRowHeight(25);
	    tblTotal.setRowHeight(25);
	    tblAuditFlash.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
	    tblTotal.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
	    DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
	    leftRenderer.setHorizontalAlignment(JLabel.LEFT);
	    DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
	    rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
	    DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
	    centerRenderer.setHorizontalAlignment(JLabel.CENTER);

	    tblAuditFlash.getColumnModel().getColumn(0).setPreferredWidth(50);//waiterName
	    tblAuditFlash.getColumnModel().getColumn(1).setPreferredWidth(50);//no of kot
	    tblAuditFlash.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
	    tblAuditFlash.getColumnModel().getColumn(2).setPreferredWidth(50);//no of voided kot
	    tblAuditFlash.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
	    tblAuditFlash.getColumnModel().getColumn(3).setPreferredWidth(50);//nvk %
	    tblAuditFlash.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
	    tblAuditFlash.getColumnModel().getColumn(4).setPreferredWidth(50);//no of moved kot
	    tblAuditFlash.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
	    tblAuditFlash.getColumnModel().getColumn(5).setPreferredWidth(50);//nmk %
	    tblAuditFlash.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);

	    //tblTotal.setSize(400, 400);
	    DefaultTableCellRenderer rightRenderer1 = new DefaultTableCellRenderer();
	    rightRenderer1.setHorizontalAlignment(JLabel.RIGHT);
	    tblTotal.getColumnModel().getColumn(1).setCellRenderer(rightRenderer1);

	    tblTotal.getColumnModel().getColumn(0).setPreferredWidth(50);
	    tblTotal.getColumnModel().getColumn(1).setPreferredWidth(50);
	    tblTotal.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
	    tblTotal.getColumnModel().getColumn(2).setPreferredWidth(50);
	    tblTotal.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
	    tblTotal.getColumnModel().getColumn(3).setPreferredWidth(50);
	    tblTotal.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
	    tblTotal.getColumnModel().getColumn(4).setPreferredWidth(50);
	    tblTotal.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
	    tblTotal.getColumnModel().getColumn(5).setPreferredWidth(50);
	    tblTotal.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}

	finally
	{
	    sbSqlLive = null;
	    sbSqlQFile = null;
	    sbFilters = null;
	}
    }

}
