/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.POSReport.view;

import com.POSGlobal.controller.clsExportDocument;
import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsPosConfigFile;
import com.POSGlobal.controller.clsUtility;
import com.POSGlobal.view.frmSearchFormDialog;
import com.POSPrinting.Utility.clsPrintingUtility;
import com.itextpdf.text.Document;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;

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

import javax.swing.JFrame;
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
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.swing.JRViewer;

public class frmDebitCardFlashReports extends javax.swing.JFrame
{

    private String DateFrom = "", DateTo = "", customerType, rechargeType;

    DefaultTableModel dm, totalDm;
    private String exportFormName, ExportReportPath, sql, customerCode;
    private static String emailMsgTxt = "", reportType, reportName;
    private static String emailSubjectTxt = "";
    private static String emailFromAddress = "";
    private static String[] emailList = new String[10];
    private String attachments[] = null;
    private Map<String, String> hmCustomerType;
    private clsUtility objUtility;
    private StringBuilder sb = new StringBuilder();
    private DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();

    public frmDebitCardFlashReports()
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

	try
	{
	    objUtility = new clsUtility();
	    btnExport.setVisible(true);

	    clsPosConfigFile pc = new clsPosConfigFile();
	    ExportReportPath = clsPosConfigFile.exportReportPath;
	    lblUserCode.setText(clsGlobalVarClass.gUserCode);
	    lblPosName.setText(clsGlobalVarClass.gPOSName);
	    lblDate.setText(clsGlobalVarClass.gPOSDateToDisplay);
	    lblModuleName.setText(clsGlobalVarClass.gSelectedModule);
	    fillComboBox();
	    dteFromDate.setDate(objUtility.funGetDateToSetCalenderDate());
	    dteToDate.setDate(objUtility.funGetDateToSetCalenderDate());

	    hmCustomerType = new HashMap<String, String>();
	    sql = "select strCustTypeCode,strCustType from tblcustomertypemaster";
	    ResultSet rsCustTypeData = clsGlobalVarClass.dbMysql.executeResultSet(sql);
	    while (rsCustTypeData.next())
	    {
		hmCustomerType.put(rsCustTypeData.getString(2), rsCustTypeData.getString(1));
	    }
	    customerCode = "All";
	    rsCustTypeData.close();
	    //funFillCustTypeCombo();
	    //funCustomerWiseFlashReport();
	    funDebitCardConsumptionReport();

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

    private String funGetCalanderDate(String type)
    {
	String date = "";
	if (type.equals("From"))
	{
	    date = (dteFromDate.getDate().getYear() + 1900) + "-" + (dteFromDate.getDate().getMonth() + 1) + "-" + dteFromDate.getDate().getDate();
	}
	else
	{
	    date = (dteToDate.getDate().getYear() + 1900) + "-" + (dteToDate.getDate().getMonth() + 1) + "-" + dteToDate.getDate().getDate();
	}
	return date;
    }

    /**
     * This Function is used for Customer Wise Flash Report
     */
    private void funDebitCardStatusReport()
    {

	StringBuilder sbSql = new StringBuilder();

	try
	{
	    exportFormName = "DebitCardCustomerWiseReport";

	    totalDm = new DefaultTableModel();
	    totalDm.addColumn("");
	    totalDm.addColumn("");
	    totalDm.addColumn("");
	    totalDm.addColumn("");
	    totalDm.addColumn("");

	    dm = new DefaultTableModel();
	    dm.addColumn("CardNo");
	    dm.addColumn("CustomerName");
	    dm.addColumn("Recharge Amt");
	    dm.addColumn("Refund Amt");
	    dm.addColumn("Redeem Amt");
	    dm.addColumn("Balance Amt");
	    tblDebitCardFlash.setModel(dm);

	    String fromDate = funGetCalanderDate("From");
	    String toDate = funGetCalanderDate("To");

	    Map<String, clsDebitCardReportDtl> hmDebitCardDtl = new HashMap<String, clsDebitCardReportDtl>();

	    /*
             * String sql="select a.strCardNo,ifnull(b.strCustomerName,'') " + "
             * from tbldebitcardmaster a left outer join tblcustomermaster b on
             * a.strCustomerCode=b.strCustomerCode " + " group by a.strCardNo";
             * ResultSet
             * rsDebitCardDtl=clsGlobalVarClass.dbMysql.executeResultSet(sql);
             * while(rsDebitCardDtl.next()) { clsDebitCardReportDtl
             * objDebitCardDtl=new clsDebitCardReportDtl();
             * objDebitCardDtl.setStrCardNo(rsDebitCardDtl.getString(1));
             * objDebitCardDtl.setStrCustomerName(rsDebitCardDtl.getString(2));
             * objDebitCardDtl.setDblRechargeAmt(0);
             * objDebitCardDtl.setDblRedeemAmt(0);
             * objDebitCardDtl.setDblRefundAmt(0);
             * hmDebitCardDtl.put(rsDebitCardDtl.getString(1),objDebitCardDtl);
             * }
             rsDebitCardDtl.close();
	     */
	    sql = "select a.strCardNo,ifnull(sum(b.dblRechargeAmount),0.00) "
		    + " from tbldebitcardmaster a left outer join tbldebitcardrecharge b on a.strCardString=b.strCardString "
		    + " where date(b.dteDateCreated) between '" + fromDate + "' and '" + toDate + "' and b.strTransferBalance='N' "
		    + " group by a.strCardNo";
	    ResultSet rsDebitCardRechargeDtl = clsGlobalVarClass.dbMysql.executeResultSet(sql);
	    while (rsDebitCardRechargeDtl.next())
	    {
		if (hmDebitCardDtl.containsKey(rsDebitCardRechargeDtl.getString(1)))
		{
		    clsDebitCardReportDtl objDebitCardDtl = hmDebitCardDtl.get(rsDebitCardRechargeDtl.getString(1));
		    objDebitCardDtl.setDblRechargeAmt(objDebitCardDtl.getDblRechargeAmt() + rsDebitCardRechargeDtl.getDouble(2));
		    hmDebitCardDtl.put(rsDebitCardRechargeDtl.getString(1), objDebitCardDtl);
		}
		else
		{
		    clsDebitCardReportDtl objDebitCardDtl = new clsDebitCardReportDtl();
		    objDebitCardDtl.setStrCardNo(rsDebitCardRechargeDtl.getString(1));
		    objDebitCardDtl.setStrCustomerName("");
		    objDebitCardDtl.setDblRechargeAmt(rsDebitCardRechargeDtl.getDouble(2));
		    objDebitCardDtl.setDblRedeemAmt(0);
		    objDebitCardDtl.setDblRefundAmt(0);
		    hmDebitCardDtl.put(rsDebitCardRechargeDtl.getString(1), objDebitCardDtl);
		}
	    }
	    rsDebitCardRechargeDtl.close();

	    sql = "select a.strCardNo,ifnull(sum(b.dblRefundAmt),0.00) "
		    + " from tbldebitcardmaster a left outer join tbldebitcardrefundamt b on a.strCardString=b.strCardString "
		    + " where date(b.dteDateCreated) between '" + fromDate + "' and '" + toDate + "' "
		    + " group by a.strCardNo";
	    ResultSet rsDebitCardRefundDtl = clsGlobalVarClass.dbMysql.executeResultSet(sql);
	    while (rsDebitCardRefundDtl.next())
	    {
		if (hmDebitCardDtl.containsKey(rsDebitCardRefundDtl.getString(1)))
		{
		    clsDebitCardReportDtl objDebitCardDtl = hmDebitCardDtl.get(rsDebitCardRefundDtl.getString(1));
		    objDebitCardDtl.setDblRefundAmt(objDebitCardDtl.getDblRefundAmt() + rsDebitCardRefundDtl.getDouble(2));
		    hmDebitCardDtl.put(rsDebitCardRefundDtl.getString(1), objDebitCardDtl);
		}
		else
		{
		    clsDebitCardReportDtl objDebitCardDtl = new clsDebitCardReportDtl();
		    objDebitCardDtl.setStrCardNo(rsDebitCardRefundDtl.getString(1));
		    objDebitCardDtl.setStrCustomerName("");
		    objDebitCardDtl.setDblRechargeAmt(0);
		    objDebitCardDtl.setDblRedeemAmt(0);
		    objDebitCardDtl.setDblRefundAmt(rsDebitCardRefundDtl.getDouble(2));
		    hmDebitCardDtl.put(rsDebitCardRefundDtl.getString(1), objDebitCardDtl);
		}
	    }
	    rsDebitCardRefundDtl.close();

	    sql = "select b.strCardNo,ifnull(sum(b.dblTransactionAmt),0.00) "
		    + " from tbldebitcardbilldetails b "
		    + " where date(b.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		    + " group by b.strCardNo";
	    ResultSet rsDebitCardRedeemDtl = clsGlobalVarClass.dbMysql.executeResultSet(sql);
	    while (rsDebitCardRedeemDtl.next())
	    {
		if (hmDebitCardDtl.containsKey(rsDebitCardRedeemDtl.getString(1)))
		{
		    clsDebitCardReportDtl objDebitCardDtl = hmDebitCardDtl.get(rsDebitCardRedeemDtl.getString(1));
		    objDebitCardDtl.setDblRedeemAmt(objDebitCardDtl.getDblRedeemAmt() + rsDebitCardRedeemDtl.getDouble(2));
		    hmDebitCardDtl.put(rsDebitCardRedeemDtl.getString(1), objDebitCardDtl);
		}
		else
		{
		    clsDebitCardReportDtl objDebitCardDtl = new clsDebitCardReportDtl();
		    objDebitCardDtl.setStrCardNo(rsDebitCardRedeemDtl.getString(1));
		    objDebitCardDtl.setStrCustomerName("");
		    objDebitCardDtl.setDblRechargeAmt(0);
		    objDebitCardDtl.setDblRedeemAmt(rsDebitCardRedeemDtl.getDouble(2));
		    objDebitCardDtl.setDblRefundAmt(0);
		    hmDebitCardDtl.put(rsDebitCardRedeemDtl.getString(1), objDebitCardDtl);
		}
	    }
	    rsDebitCardRedeemDtl.close();

	    double totalBalance = 0, balanceAmt = 0;
	    double totalRechargeAmt = 0, totalRefundAmt = 0, totalRedeemAmt = 0;
	    for (Map.Entry<String, clsDebitCardReportDtl> entry : hmDebitCardDtl.entrySet())
	    {
		clsDebitCardReportDtl objDebitCardDtl = entry.getValue();
		balanceAmt = objDebitCardDtl.getDblRechargeAmt() - (objDebitCardDtl.getDblRefundAmt() + objDebitCardDtl.getDblRedeemAmt());
		//if(balanceAmt>0)
		//{
		Object[] ob =
		{
		    objDebitCardDtl.getStrCardNo(), objDebitCardDtl.getStrCustomerName(), gDecimalFormat.format(objDebitCardDtl.getDblRechargeAmt()), gDecimalFormat.format(objDebitCardDtl.getDblRefundAmt()), gDecimalFormat.format(objDebitCardDtl.getDblRedeemAmt()), gDecimalFormat.format(balanceAmt)
		};
		totalBalance += balanceAmt;
		totalRechargeAmt += objDebitCardDtl.getDblRechargeAmt();
		totalRefundAmt += objDebitCardDtl.getDblRefundAmt();
		totalRedeemAmt += objDebitCardDtl.getDblRedeemAmt();
		dm.addRow(ob);
		//}
	    }
	    tblDebitCardFlash.setModel(dm);
	    Object[] total =
	    {
		"Total", gDecimalFormat.format(totalRechargeAmt), gDecimalFormat.format(totalRefundAmt), gDecimalFormat.format(totalRedeemAmt), gDecimalFormat.format(totalBalance)
	    };
	    totalDm.addRow(total);
	    tblTotal.setModel(totalDm);

	    DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
	    rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
	    tblDebitCardFlash.getColumnModel().getColumn(0).setPreferredWidth(100);
	    tblDebitCardFlash.getColumnModel().getColumn(1).setPreferredWidth(200);
	    tblDebitCardFlash.getColumnModel().getColumn(2).setPreferredWidth(100);
	    tblDebitCardFlash.getColumnModel().getColumn(3).setPreferredWidth(100);
	    tblDebitCardFlash.getColumnModel().getColumn(4).setPreferredWidth(100);
	    tblDebitCardFlash.getColumnModel().getColumn(5).setPreferredWidth(100);

	    tblDebitCardFlash.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
	    tblDebitCardFlash.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
	    tblDebitCardFlash.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
	    tblDebitCardFlash.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);

	    tblTotal.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
	    tblTotal.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
	    tblTotal.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
	    tblTotal.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
	    tblTotal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    tblTotal.getColumnModel().getColumn(0).setPreferredWidth(400);
	    tblTotal.getColumnModel().getColumn(1).setPreferredWidth(100);
	    tblTotal.getColumnModel().getColumn(2).setPreferredWidth(100);
	    tblTotal.getColumnModel().getColumn(3).setPreferredWidth(100);
	    tblTotal.getColumnModel().getColumn(4).setPreferredWidth(80);

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
     * This Function is used for Consumption Flash Report
     */
    private void funDebitCardConsumptionReport()
    {

	StringBuilder sbSql = new StringBuilder();

	try
	{

	    totalDm = new DefaultTableModel();
	    totalDm.addColumn("");
	    totalDm.addColumn("");

	    dm = new DefaultTableModel();
	    dm.addColumn("POS");
	    dm.addColumn("Bill No");
	    dm.addColumn("Card No");
	    dm.addColumn("Customer Name");
	    dm.addColumn("Bill Date");
	    dm.addColumn("Bill Time");
	    dm.addColumn("Bill Amount");
	    tblDebitCardFlash.setModel(dm);

	    String fromDate = funGetCalanderDate("From");
	    String toDate = funGetCalanderDate("To");

	    double totalBalance = 0;
	    sbSql.append(" select c.strPosName,b.strBillNo,a.strCardNo,ifnull(d.strCustomerName,''), "
		    + " date(b.dteBillDate),time(b.dteBillDate),b.dblTransactionAmt "
		    + " from tbldebitcardmaster a left outer join tblcustomermaster d on a.strCustomerCode=d.strCustomerCode, "
		    + " tbldebitcardbilldetails b,tblposmaster c "
		    + " where a.strCardNo=b.strCardNo and b.strPOSCode=c.strPosCode "
		    + " and date(b.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");

	    if (!cmbPosCode.getSelectedItem().toString().equals("All"))
	    {
		sbSql.append(" and b.strPOSCode='" + objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString().trim()) + "' ");
	    }
	    sbSql.append(" group by d.strCustomerName,b.strBillNo ");

	    System.out.println(sbSql.toString());
	    ResultSet rsCustomerData = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
	    while (rsCustomerData.next())
	    {

		Object[] ob =
		{
		    rsCustomerData.getString(1), rsCustomerData.getString(2), rsCustomerData.getString(3),
		    rsCustomerData.getString(4), rsCustomerData.getString(5), rsCustomerData.getString(6), gDecimalFormat.format(rsCustomerData.getString(7))
		};
		totalBalance += rsCustomerData.getDouble(7);
		dm.addRow(ob);
	    }
	    rsCustomerData.close();
	    tblDebitCardFlash.setModel(dm);

	    Object[] total =
	    {
		"Total", gDecimalFormat.format(totalBalance)
	    };
	    totalDm.addRow(total);
	    tblTotal.setModel(totalDm);

	    DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
	    rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
	    tblDebitCardFlash.getColumnModel().getColumn(0).setPreferredWidth(200);
	    tblDebitCardFlash.getColumnModel().getColumn(1).setPreferredWidth(100);
	    tblDebitCardFlash.getColumnModel().getColumn(2).setPreferredWidth(100);
	    tblDebitCardFlash.getColumnModel().getColumn(3).setPreferredWidth(200);
	    tblDebitCardFlash.getColumnModel().getColumn(4).setPreferredWidth(100);
	    tblDebitCardFlash.getColumnModel().getColumn(5).setPreferredWidth(100);
	    tblDebitCardFlash.getColumnModel().getColumn(6).setPreferredWidth(100);
	    tblDebitCardFlash.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);

	    tblTotal.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
	    tblTotal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    tblTotal.getColumnModel().getColumn(0).setPreferredWidth(700);

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
     * This Function is used for Unused Card Balance Report
     */
    private void funShowUnusedCardBalance()
    {

	StringBuilder sbSql = new StringBuilder();

	try
	{

	    totalDm = new DefaultTableModel();
	    totalDm.addColumn("");
	    totalDm.addColumn("");

	    dm = new DefaultTableModel();
	    dm.addColumn("Card No");
	    dm.addColumn("POS Date");
	    dm.addColumn("User");
	    dm.addColumn("Card Amt");
	    tblDebitCardFlash.setModel(dm);

	    String fromDate = funGetCalanderDate("From");
	    String toDate = funGetCalanderDate("To");
	    double totalBalance = 0;
	    sbSql.append(" select strCardNo,date(dtePOSDate),strUserCreated,sum(dblCardAmt) "
		    + " from tbldebitcardrevenue "
		    + " where date(dtePOSDate) between '" + fromDate + "' and '" + toDate + "' "
		    + " group by strCardNo "
		    + " order by date(dtePOSDate)");
	    System.out.println(sbSql.toString());
	    ResultSet rsUnusedCardBal = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
	    while (rsUnusedCardBal.next())
	    {

		Object[] ob =
		{
		    rsUnusedCardBal.getString(1), rsUnusedCardBal.getString(2), rsUnusedCardBal.getString(3), gDecimalFormat.format(rsUnusedCardBal.getString(4))
		};
		totalBalance += rsUnusedCardBal.getDouble(4);
		dm.addRow(ob);
	    }
	    rsUnusedCardBal.close();
	    tblDebitCardFlash.setModel(dm);

	    Object[] total =
	    {
		"Total", gDecimalFormat.format(totalBalance)
	    };
	    totalDm.addRow(total);
	    tblTotal.setModel(totalDm);

	    DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
	    rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
	    tblDebitCardFlash.getColumnModel().getColumn(0).setPreferredWidth(200);
	    tblDebitCardFlash.getColumnModel().getColumn(1).setPreferredWidth(200);
	    tblDebitCardFlash.getColumnModel().getColumn(2).setPreferredWidth(300);
	    tblDebitCardFlash.getColumnModel().getColumn(3).setPreferredWidth(200);
	    tblDebitCardFlash.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);

	    tblTotal.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
	    tblTotal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    tblTotal.getColumnModel().getColumn(0).setPreferredWidth(700);

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
     * This Function is used for Recharge Details
     */
    private void funDebitCardRechargeDtlReport()
    {

	StringBuilder sbSql = new StringBuilder();

	try
	{

	    totalDm = new DefaultTableModel();
	    totalDm.addColumn("");
	    totalDm.addColumn("");

	    dm = new DefaultTableModel();
	    dm.addColumn("POS");
	    dm.addColumn("Recharge No");
	    dm.addColumn("Card No");
	    dm.addColumn("Customer Name");
	    dm.addColumn("Recharge Date");
	    dm.addColumn("Recharge Time");
	    dm.addColumn("Amount");
	    dm.addColumn("User");
	    dm.addColumn("User Name");
	    tblDebitCardFlash.setModel(dm);

	    String fromDate = funGetCalanderDate("From");
	    String toDate = funGetCalanderDate("To");
	    double totalRechargeAmt = 0;
	    sbSql.append("select d.strPosName,c.intRechargeNo,a.strCardNo,ifnull(b.strCustomerName,'')"
		    + " ,date(c.dteDateCreated),time(c.dteDateCreated),c.dblRechargeAmount"
		    + " ,ifnull(e.strUserCode,'NA'),ifnull(e.strUserName,'NA') "
		    + " from tbldebitcardmaster a left outer join tblcustomermaster b on a.strCustomerCode=b.strCustomerCode "
		    + " left outer join tbldebitcardrecharge c on a.strCardString=c.strCardString "
		    + " left outer join tblposmaster d on c.strPOSCode=d.strPosCode "
		    + " left outer join tbluserhd e on c.strUserCreated=e.strUserCode "
		    + " where date(c.dteDateCreated) between '" + fromDate + "' and '" + toDate + "' AND c.strTransferBalance<>'Y' ");

	    if (!cmbPosCode.getSelectedItem().toString().equals("All"))
	    {
		sbSql.append(" and c.strPOSCode='" + objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString().trim()) + "' ");
	    }
	    System.out.println(sbSql.toString());
	    ResultSet rsRechargeDtl = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
	    while (rsRechargeDtl.next())
	    {
		double rechargeAmt = rsRechargeDtl.getDouble(7);
		Object[] ob =
		{
		    rsRechargeDtl.getString(1), rsRechargeDtl.getString(2), rsRechargeDtl.getString(3), rsRechargeDtl.getString(4), rsRechargeDtl.getString(5), rsRechargeDtl.getString(6), gDecimalFormat.format(rechargeAmt), rsRechargeDtl.getString(8), rsRechargeDtl.getString(9)
		};
		//totalRechargeAmt+=rsRechargeDtl.getDouble(7);
		totalRechargeAmt += rechargeAmt;
		dm.addRow(ob);
	    }
	    rsRechargeDtl.close();
	    tblDebitCardFlash.setModel(dm);

	    Object[] total =
	    {
		"Total", gDecimalFormat.format(totalRechargeAmt)
	    };
	    totalDm.addRow(total);
	    tblTotal.setModel(totalDm);

	    DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
	    rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
	    tblDebitCardFlash.getColumnModel().getColumn(0).setPreferredWidth(80);
	    tblDebitCardFlash.getColumnModel().getColumn(1).setPreferredWidth(80);
	    tblDebitCardFlash.getColumnModel().getColumn(2).setPreferredWidth(80);
	    tblDebitCardFlash.getColumnModel().getColumn(3).setPreferredWidth(180);
	    tblDebitCardFlash.getColumnModel().getColumn(4).setPreferredWidth(80);
	    tblDebitCardFlash.getColumnModel().getColumn(5).setPreferredWidth(80);
	    tblDebitCardFlash.getColumnModel().getColumn(6).setPreferredWidth(70);
	    tblDebitCardFlash.getColumnModel().getColumn(7).setPreferredWidth(70);

	    tblDebitCardFlash.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);

	    tblTotal.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
	    tblTotal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    tblTotal.getColumnModel().getColumn(0).setPreferredWidth(700);

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
     * This Function is used for Refund Details
     */
    private void funDebitCardRefundDtlReport()
    {

	StringBuilder sbSql = new StringBuilder();

	try
	{

	    String fromDate = funGetCalanderDate("From");
	    String toDate = funGetCalanderDate("To");

	    totalDm = new DefaultTableModel();
	    totalDm.addColumn("");
	    totalDm.addColumn("");

	    dm = new DefaultTableModel();
	    dm.addColumn("POS");
	    dm.addColumn("Refund No");
	    dm.addColumn("Card No");
	    dm.addColumn("Customer Name");
	    dm.addColumn("Refund Date");
	    dm.addColumn("Refund Time");
	    dm.addColumn("Amount");
	    tblDebitCardFlash.setModel(dm);

	    double totalRefundAmt = 0;
	    sbSql.append("select d.strPosName,c.strRefundNo,a.strCardNo,ifnull(b.strCustomerName,'') "
		    + ",date(c.dteDateCreated),time(c.dteDateCreated),c.dblRefundAmt "
		    + " from tbldebitcardmaster a left outer join tblcustomermaster b on a.strCustomerCode=b.strCustomerCode "
		    + " left outer join tbldebitcardrefundamt c on a.strCardString=c.strCardString "
		    + " inner join tblposmaster d on c.strPOSCode=d.strPosCode ");

	    if (!cmbPosCode.getSelectedItem().toString().equals("All"))
	    {
		sbSql.append(" and c.strPOSCode='" + objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString().trim()) + "' ");
	    }
	    sbSql.append(" where date(c.dteDateCreated) between '" + fromDate + "' and '" + toDate + "' ");

	    System.out.println(sbSql.toString());
	    ResultSet rsRefundDtl = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
	    while (rsRefundDtl.next())
	    {

		Object[] ob =
		{
		    rsRefundDtl.getString(1), rsRefundDtl.getString(2), rsRefundDtl.getString(3),
		    rsRefundDtl.getString(4), rsRefundDtl.getString(5), rsRefundDtl.getString(6), gDecimalFormat.format(rsRefundDtl.getString(7))
		};
		totalRefundAmt += rsRefundDtl.getDouble(7);
		dm.addRow(ob);
	    }
	    rsRefundDtl.close();
	    tblDebitCardFlash.setModel(dm);

	    Object[] total =
	    {
		"Total", gDecimalFormat.format(totalRefundAmt)
	    };
	    totalDm.addRow(total);
	    tblTotal.setModel(totalDm);

	    DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
	    rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
	    tblDebitCardFlash.getColumnModel().getColumn(0).setPreferredWidth(100);
	    tblDebitCardFlash.getColumnModel().getColumn(1).setPreferredWidth(100);
	    tblDebitCardFlash.getColumnModel().getColumn(2).setPreferredWidth(100);
	    tblDebitCardFlash.getColumnModel().getColumn(3).setPreferredWidth(200);
	    tblDebitCardFlash.getColumnModel().getColumn(4).setPreferredWidth(100);
	    tblDebitCardFlash.getColumnModel().getColumn(5).setPreferredWidth(100);
	    tblDebitCardFlash.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);

	    tblTotal.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
	    tblTotal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    tblTotal.getColumnModel().getColumn(0).setPreferredWidth(700);

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
     * ]
     * this function is used Filling POS Code ComboBoxs
     */
    public void fillComboBox()
    {
	try
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

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    /**
     * this Function is used for get Selected POS Code
     */
    /*
     * private void setFormToInDateChosser() {
     *
     * dteFromDate.setDate(clsGlobalVarClass.gCalendarDate);
     * dteToDate.setDate(clsGlobalVarClass.gCalendarDate); }
     *
     *
     * private String getFromDate() { String FromDate = null; try {
     * java.util.Date dt1 = new java.util.Date(); dt1 = dteFromDate.getDate();
     * int d = dt1.getDate(); int m = dt1.getMonth() + 1; int y = dt1.getYear()
     * + 1900; FromDate = y + "-" + m + "-" + d; } catch (Exception e) {
     * e.printStackTrace(); } finally { return FromDate; } }
     *
     *
     * private String getToDate() { String Todate = null; try { Date dt2 =
     * dteToDate.getDate(); int d = dt2.getDate(); int m = dt2.getMonth() + 1;
     * int y = dt2.getYear() + 1900; Todate = y + "-" + m + "-" + d; } catch
     * (Exception e) { e.printStackTrace(); } finally { return Todate; }
     }
     */
    /**
     * this function is used Export Files
     *
     * @param table
     * @param file
     */
    public void funExportFile(JTable table, File file)
    {
	try
	{
	    WritableWorkbook workbook1 = Workbook.createWorkbook(file);
	    WritableSheet sheet1 = workbook1.createSheet("First Sheet", 0);
	    TableModel model = table.getModel();
	    sheet1.addCell(new Label(3, 0, "DSS Java Pos Debit Card Transaction / Status Details "));
	    for (int i = 0; i < model.getColumnCount() + 1; i++)
	    {
		Label column = new Label(i, 1, model.getColumnName(i));
		sheet1.addCell(column);
	    }
	    int i = 0, j = 0;
	    int k = 0;
	    for (i = 1; i < model.getRowCount() + 1; i++)
	    {
		for (j = 0; j < model.getColumnCount(); j++)
		{
		    Label row = new Label(j, i + 1, model.getValueAt(k, j).toString());
		    sheet1.addCell(row);
		}
		k++;
	    }
	    funAddLastOfExportReport(workbook1);
	    workbook1.write();
	    workbook1.close();
	    Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + ExportReportPath + "/" + exportFormName + objUtility.funGetDateInString() + ".xls");
	}
	catch (FileNotFoundException ex)
	{
	    JOptionPane.showMessageDialog(this, "File is already opened please close ");
	    ex.printStackTrace();
	}
	catch (Exception ex)
	{
	    ex.printStackTrace();
	}
    }

    /**
     * This function is used Last Export Report
     *
     * @param workbook1
     */
    private void funAddLastOfExportReport(WritableWorkbook workbook1)
    {
	try
	{
	    int i = 0, j = 0, LastIndexReport = 0;
	    switch (exportFormName)
	    {
		case "DebitCardCunsumptionReport":
		    LastIndexReport = 1;
		    break;
		case "DebitCardRechargeDetails":
		    LastIndexReport = 3;
		    break;
		case "DebitCardStatusDetails":
		    LastIndexReport = 6;
		    break;
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
	    Label row = new Label(1, r + 1, " Created On : " + clsGlobalVarClass.getCurrentDateTime() + " At : " + fmt + " By : " + clsGlobalVarClass.gUserCode + " ");
	    sheet2.addCell(row);
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    /**
     * this function is used for Export file To PDF file
     *
     * @param table
     * @param fileName
     */
    public void funExportToPDF(JTable table, String fileName)
    {
	try
	{
	    Document my_pdf_report = new Document();
	    PdfWriter.getInstance(my_pdf_report, new FileOutputStream(ExportReportPath + "/" + exportFormName + objUtility.funGetDateInString() + ".pdf"));
	    my_pdf_report.open();

	    int colCount = table.getColumnCount();
	    int rowCount = table.getRowCount();
	    PdfPTable my_report_table = new PdfPTable(colCount);
	    PdfPCell table_cell;

	    for (int i = 0; i < colCount; i++)
	    {
		table_cell = new PdfPCell(new Phrase(table.getColumnName(i)));
		my_report_table.addCell(table_cell);
		my_report_table.setWidthPercentage(115.00f);
	    }
	    for (int rowCnt = 0; rowCnt < rowCount; rowCnt++)
	    {
		for (int colCnt = 0; colCnt < colCount; colCnt++)
		{
		    table_cell = new PdfPCell(new Phrase(table.getValueAt(rowCnt, colCnt).toString()));
		    my_report_table.addCell(table_cell);
		    my_report_table.setWidthPercentage(115.00f);
		}
	    }
	    my_pdf_report.addTitle("Recharge Report");
	    my_pdf_report.add(my_report_table);
	    my_pdf_report.close();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    /**
     * this function is used print Report Document
     */
    public void funPrintReport()
    {
	try
	{
	    System.out.println("Type=" + reportType);
	    if (reportType.equalsIgnoreCase("RechargeDetails"))
	    {
		reportName = "Report/rptDebitcardRechargeDetails.jasper";
	    }
	    System.out.println("Name=" + reportName);
	    //JasperReport jasperReport = JasperCompileManager.compileReport("Report/rptDebitCardRechargeDetails.jrxml");
	    InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);
	    HashMap hm = new HashMap();
	    hm.put("posName", clsGlobalVarClass.gPOSName);
	    hm.put("fromDate", DateFrom);
	    hm.put("toDate", DateTo);
	    hm.put("customerType", customerType);
	    hm.put("rechargeType", rechargeType);
	    System.out.println(is);
	    JasperPrint print = JasperFillManager.fillReport(is, hm, clsGlobalVarClass.conJasper);
	    JRViewer viewer = new JRViewer(print);
	    JFrame jf = new JFrame();
	    jf.getContentPane().add(viewer);
	    jf.validate();
	    jf.setVisible(true);
	    jf.setSize(new Dimension(850, 750));
	    jf.setLocationRelativeTo(this);

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    /**
     * this function set customer Data
     *
     * @param data
     */
    private void funSetCustomerData(Object[] data)
    {
	txtCustCode.setText(data[1].toString());
	customerCode = clsGlobalVarClass.gSearchedItem;
    }

    private void funExportToExcel()
    {
	File theDir = new File(ExportReportPath);
	File file = new File(ExportReportPath + "/" + exportFormName + objUtility.funGetDateInString() + ".xls");
	if (!theDir.exists())// if the directory does not exist, create it
	{
	    theDir.mkdir();
	    //funExportToPDF(tblDebitCardFlash,"path");
	    funExportFile(tblDebitCardFlash, file);
	}
	else
	{
	    funExportFile(tblDebitCardFlash, file);
	    //funExportToPDF(tblDebitCardFlash,"path");
	}
    }

    private void funExportReports()
    {
	try
	{
	    if (cmbExportType.getSelectedItem().toString().equalsIgnoreCase("Excel"))
	    {
		funExportToExcel();
	    }
	    else
	    {
		clsExportDocument exportDocument = new clsExportDocument();
		exportDocument.funExportToPDF(tblDebitCardFlash, tblTotal, exportFormName);
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funOpenDebitCardDtlPrint(String cardNo)
    {
	List<List<String>> arrListShowCardDtl = new ArrayList<List<String>>();;
	List<String> arrListRecharge = null;
	List<String> arrListRedeem = null;
	List<String> arrListRefund = null;
	List<String> arrListOpenKOT = null;
	List<String> arrListUnsettleBill = null;
	List<String> arrListTransferAmt = null;

	double totalRechargeAmt = 0;
	double totalRedeemAmt = 0;
	double totalRefundAmt = 0;
	double totalOpenKOTAmt = 0;
	double totalUnsettleAmt = 0;
	double totalTransferAmt = 0;
	double totalCardRevenue = 0;

	String fromDate = funGetCalanderDate("From");
	String toDate = funGetCalanderDate("To");

	String sql = "";
	try
	{
	    String cardString = "";
	    sql = "select a.strCardString,ifnull(b.strCustomerName,'') "
		    + "from tbldebitcardmaster a left outer join tblcustomermaster b "
		    + "on a.strCustomerCode=b.strCustomerCode "
		    + "where a.strCardNo='" + cardNo + "';";
	    ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(sql);
	    if (rs.next())
	    {
		cardString = rs.getString(1);
	    }
	    rs.close();

	    StringBuilder sbSqlRecharge = new StringBuilder();
	    StringBuilder sbSqlRedeem = new StringBuilder();
	    StringBuilder sbSqlRefund = new StringBuilder();
	    StringBuilder sbSqlOpenKot = new StringBuilder();
	    StringBuilder sbSqlUnsettleBill = new StringBuilder();
	    StringBuilder sbSqlTransferedAmt = new StringBuilder();
	    StringBuilder sbSqlRevenueAmt = new StringBuilder();

	    sbSqlRecharge.setLength(0);
	    sbSqlRecharge.append("select b.strPosName,a.intRechargeNo,date(a.dteDateCreated),time(a.dteDateCreated),'Recharge',a.strUserCreated,a.dblRechargeAmount "
		    + " from tbldebitcardrecharge a,tblposmaster b "
		    + " where date(a.dteDateCreated ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
		    + " and a.strPOSCode=b.strPosCode and a.strCardString='" + cardString + "' ");

	    if (!cmbPosCode.getSelectedItem().toString().equals("All"))
	    {
		sbSqlRecharge.append("  and b.strPOSCode='" + objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString().trim()) + "' ");
	    }
	    ResultSet rsRechargeDetails = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlRecharge.toString());
	    while (rsRechargeDetails.next())
	    {
		totalRechargeAmt += rsRechargeDetails.getDouble(7);
		arrListRecharge = new ArrayList<String>();
		Object[] row =
		{
		    rsRechargeDetails.getString(1), rsRechargeDetails.getString(2), rsRechargeDetails.getString(3), rsRechargeDetails.getString(4), rsRechargeDetails.getString(5), rsRechargeDetails.getString(6), rsRechargeDetails.getString(7)
		};

		arrListRecharge.add(rsRechargeDetails.getString(1));
		arrListRecharge.add(rsRechargeDetails.getString(2));
		arrListRecharge.add(rsRechargeDetails.getString(3));
		arrListRecharge.add(rsRechargeDetails.getString(4));
		arrListRecharge.add(rsRechargeDetails.getString(5));
		arrListRecharge.add(rsRechargeDetails.getString(6));
		arrListRecharge.add(rsRechargeDetails.getString(7));
	    }
	    rsRechargeDetails.close();
	    arrListShowCardDtl.add(arrListRecharge);

	    sbSqlRedeem.append("select c.strPosName,a.strBillNo,date(a.dteBillDate),time(a.dteBillDate),'Redeem',e.strUserCreated,a.dblTransactionAmt "
		    + "from tbldebitcardbilldetails a,tbldebitcardmaster b,tblposmaster c,tblbillsettlementdtl d,tblbillhd e "
		    + "where date(e.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
		    + "and a.strCardNo=b.strCardNo and a.strPOSCode=c.strPosCode "
		    + "and a.strBillNo=d.strBillNo and d.strBillNo=e.strBillNo "
		    + "and b.strCardNo='" + cardNo + "'  ");

	    if (!cmbPosCode.getSelectedItem().toString().equals("All"))
	    {
		sbSqlRedeem.append("  and c.strPOSCode='" + objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString().trim()) + "' ");
	    }
	    ResultSet rsRedeemDetails = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlRedeem.toString());
	    while (rsRedeemDetails.next())
	    {
		totalRedeemAmt += rsRedeemDetails.getDouble(7);
		arrListRedeem = new ArrayList<String>();
		Object[] row =
		{
		    rsRedeemDetails.getString(1), rsRedeemDetails.getString(2), rsRedeemDetails.getString(3), rsRedeemDetails.getString(4), rsRedeemDetails.getString(5), rsRedeemDetails.getString(6), rsRedeemDetails.getString(7)
		};
		arrListRedeem.add(rsRedeemDetails.getString(1));
		arrListRedeem.add(rsRedeemDetails.getString(2));
		arrListRedeem.add(rsRedeemDetails.getString(3));
		arrListRedeem.add(rsRedeemDetails.getString(4));
		arrListRedeem.add(rsRedeemDetails.getString(5));
		arrListRedeem.add(rsRedeemDetails.getString(6));
		arrListRedeem.add(rsRedeemDetails.getString(7));
	    }
	    rsRedeemDetails.close();
	    arrListShowCardDtl.add(arrListRedeem);

	    sbSqlRedeem.setLength(0);
	    sbSqlRedeem.append("select c.strPosName,a.strBillNo,date(a.dteBillDate),time(a.dteBillDate),'Redeem',e.strUserCreated,a.dblTransactionAmt "
		    + "from tbldebitcardbilldetails a,tbldebitcardmaster b,tblposmaster c,tblqbillsettlementdtl d,tblqbillhd e "
		    + "where date(e.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
		    + "and a.strCardNo=b.strCardNo and a.strPOSCode=c.strPosCode "
		    + "and a.strBillNo=d.strBillNo and d.strBillNo=e.strBillNo "
		    + "and b.strCardNo='" + cardNo + "'  ");

	    if (!cmbPosCode.getSelectedItem().toString().equals("All"))
	    {
		sbSqlRedeem.append("  and c.strPOSCode='" + objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString().trim()) + "' ");
	    }
	    rsRedeemDetails = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlRedeem.toString());
	    while (rsRedeemDetails.next())
	    {
		totalRedeemAmt += rsRedeemDetails.getDouble(7);
		arrListRedeem = new ArrayList<String>();
		Object[] row =
		{
		    rsRedeemDetails.getString(1), rsRedeemDetails.getString(2), rsRedeemDetails.getString(3), rsRedeemDetails.getString(4), rsRedeemDetails.getString(5), rsRedeemDetails.getString(6), rsRedeemDetails.getString(7)
		};
		arrListRedeem.add(rsRedeemDetails.getString(1));
		arrListRedeem.add(rsRedeemDetails.getString(2));
		arrListRedeem.add(rsRedeemDetails.getString(3));
		arrListRedeem.add(rsRedeemDetails.getString(4));
		arrListRedeem.add(rsRedeemDetails.getString(5));
		arrListRedeem.add(rsRedeemDetails.getString(6));
		arrListRedeem.add(rsRedeemDetails.getString(7));
	    }
	    rsRedeemDetails.close();
	    arrListShowCardDtl.add(arrListRedeem);

	    // sbSql.setLength(0);
	    sbSqlRefund.append("select b.strPosName,a.strRefundNo,date(a.dteDateCreated),time(a.dteDateCreated),'Refund'"
		    + ",a.strUserCreated,a.dblRefundAmt "
		    + " from tbldebitcardrefundamt a,tblposmaster b "
		    + " where date(a.dteDateCreated ) BETWEEN '" + fromDate + "' AND '" + toDate + "'  "
		    + " and a.strPOSCode=b.strPosCode and a.strCardString='" + cardString + "'  ");

	    if (!cmbPosCode.getSelectedItem().toString().equals("All"))
	    {
		sbSqlRefund.append("  and b.strPOSCode='" + objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString().trim()) + "' ");
	    }

	    ResultSet rsRefundDetails = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlRefund.toString());
	    while (rsRefundDetails.next())
	    {
		totalRefundAmt += rsRefundDetails.getDouble(7);
		arrListRefund = new ArrayList<String>();
		Object[] row =
		{
		    rsRefundDetails.getString(1), rsRefundDetails.getString(2), rsRefundDetails.getString(3), rsRefundDetails.getString(4), rsRefundDetails.getString(5), rsRefundDetails.getString(6), rsRefundDetails.getString(7)
		};
		arrListRefund.add(rsRefundDetails.getString(1));
		arrListRefund.add(rsRefundDetails.getString(2));
		arrListRefund.add(rsRefundDetails.getString(3));
		arrListRefund.add(rsRefundDetails.getString(4));
		arrListRefund.add(rsRefundDetails.getString(5));
		arrListRefund.add(rsRefundDetails.getString(6));
		arrListRefund.add(rsRefundDetails.getString(7));
	    }
	    rsRefundDetails.close();
	    arrListShowCardDtl.add(arrListRefund);

	    // sbSql.setLength(0);
	    sbSqlOpenKot.append(" select b.strPosName,a.strKOTNo,date(a.dteDateCreated),time(a.dteDateCreated), "
		    + " 'Open KOT',a.strUserCreated,sum(a.dblAmount),a.dblTaxAmt "
		    + " from tblitemrtemp a,tblposmaster b"
		    + " where date(a.dteDateCreated ) BETWEEN '" + fromDate + "' AND '" + toDate + "'  "
		    + " and a.strPOSCode=b.strPosCode and a.strCardNo='" + cardNo + "' "
		    + " and a.strPrintYN='Y' and a.strNCKotYN='N' ");
	    if (!cmbPosCode.getSelectedItem().toString().equals("All"))
	    {
		sbSqlOpenKot.append("  and b.strPOSCode='" + objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString().trim()) + "' ");
	    }
	    sbSqlOpenKot.append("  group by a.strKOTNo ");

	    ResultSet rsOpenKOT = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlOpenKot.toString());
	    while (rsOpenKOT.next())
	    {
		double openKOTAmt = (rsOpenKOT.getDouble(7) + rsOpenKOT.getDouble(8));
		totalOpenKOTAmt += openKOTAmt;
		arrListOpenKOT = new ArrayList<String>();

		arrListOpenKOT.add(rsOpenKOT.getString(1));
		arrListOpenKOT.add(rsOpenKOT.getString(2));
		arrListOpenKOT.add(rsOpenKOT.getString(3));
		arrListOpenKOT.add(rsOpenKOT.getString(4));
		arrListOpenKOT.add(rsOpenKOT.getString(5));
		arrListOpenKOT.add(rsOpenKOT.getString(6));
		arrListOpenKOT.add(rsOpenKOT.getString(7));
	    }
	    rsOpenKOT.close();
	    arrListShowCardDtl.add(arrListOpenKOT);

	    sbSqlUnsettleBill.append("  select c.strPosName,a.strBillNo,date(a.dteBillDate),time(a.dteBillDate),'Unsettle Bill', "
		    + "  a.strUserCreated ,a.dblGrandTotal from tblbillhd a left outer join tbltablemaster b  "
		    + "  on a.strTableNo=b.strTableNo ,tblposmaster c  "
		    + " where date(a.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
		    + " and a.strPOSCode=c.strPosCode and a.strCardNo='" + cardNo + "' "
		    + " and a.strBillNo not in (select strBillNo from tblbillsettlementdtl)");

	    if (!cmbPosCode.getSelectedItem().toString().equals("All"))
	    {
		sbSqlUnsettleBill.append("  and c.strPOSCode='" + objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString().trim()) + "' ");
	    }
	    ResultSet rsUnsettleBill = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlUnsettleBill.toString());
	    while (rsUnsettleBill.next())
	    {
		totalUnsettleAmt += rsUnsettleBill.getDouble(7);
		arrListUnsettleBill = new ArrayList<String>();
		arrListUnsettleBill.add(rsUnsettleBill.getString(1));
		arrListUnsettleBill.add(rsUnsettleBill.getString(2));
		arrListUnsettleBill.add(rsUnsettleBill.getString(3));
		arrListUnsettleBill.add(rsUnsettleBill.getString(4));
		arrListUnsettleBill.add(rsUnsettleBill.getString(5));
		arrListUnsettleBill.add(rsUnsettleBill.getString(6));
		arrListUnsettleBill.add(rsUnsettleBill.getString(7));
	    }
	    rsUnsettleBill.close();
	    arrListShowCardDtl.add(arrListUnsettleBill);

	    sbSqlTransferedAmt.append("  select c.strPosName,b.strCardNo,date(b.dteDateCreated),time(b.dteDateCreated)"
		    + ",'Balance Transfer',b.strUserCreated,a.dblRechargeAmt "
		    + " from tbldcrechargesettlementdtl a , tbldebitcardrecharge b,tblposmaster c  "
		    + " where date(b.dteDateCreated) BETWEEN '" + fromDate + "' AND '" + toDate + "'  "
		    + " and a.dblRechargeAmt=b.dblRechargeAmount and a.strRechargeNo=b.intRechargeNo "
		    + " and b.strPOSCode=c.strPosCode and a.strCardNo='" + cardNo + "' and a.strType='Debit Card' ");

	    if (!cmbPosCode.getSelectedItem().toString().equals("All"))
	    {
		sbSqlTransferedAmt.append("  and b.strPOSCode='" + objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString().trim()) + "' ");
	    }
	    ResultSet rsTransferAmt = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlTransferedAmt.toString());
	    while (rsTransferAmt.next())
	    {
		totalTransferAmt += rsTransferAmt.getDouble(7);
		arrListTransferAmt = new ArrayList<String>();
		Object[] row =
		{
		    rsTransferAmt.getString(1), rsTransferAmt.getString(2), rsTransferAmt.getString(3), rsTransferAmt.getString(4), rsTransferAmt.getString(5), rsTransferAmt.getString(6), rsTransferAmt.getString(7)
		};
		arrListTransferAmt.add(rsTransferAmt.getString(1));
		arrListTransferAmt.add(rsTransferAmt.getString(2));
		arrListTransferAmt.add(rsTransferAmt.getString(3));
		arrListTransferAmt.add(rsTransferAmt.getString(4));
		arrListTransferAmt.add(rsTransferAmt.getString(5));
		arrListTransferAmt.add(rsTransferAmt.getString(6));
		arrListTransferAmt.add(rsTransferAmt.getString(7));
	    }
	    rsTransferAmt.close();
	    arrListShowCardDtl.add(arrListTransferAmt);

	    sbSqlRevenueAmt.setLength(0);
	    sbSqlRevenueAmt.append("select b.strPosName,'',date(a.dtePOSDate),time(a.dtePOSDate),'Card Revenue'"
		    + ",a.strUserCreated,a.dblCardAmt "
		    + " from tbldebitcardrevenue a,tblposmaster b "
		    + " where date(a.dtePOSDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
		    + " and a.strPOSCode=b.strPosCode and a.strCardNo='" + cardNo + "' ");

	    if (!cmbPosCode.getSelectedItem().toString().equals("All"))
	    {
		sbSqlRevenueAmt.append("  and b.strPOSCode='" + objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString().trim()) + "' ");
	    }
	    System.out.println(sbSqlRevenueAmt);

	    List<String> arrListCardRevenue = new ArrayList<String>();
	    ResultSet rsCardRevenue = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlRevenueAmt.toString());
	    while (rsCardRevenue.next())
	    {
		totalCardRevenue += rsCardRevenue.getDouble(7);
		Object[] row =
		{
		    rsCardRevenue.getString(1), rsCardRevenue.getString(2), rsCardRevenue.getString(3), rsCardRevenue.getString(4), rsCardRevenue.getString(5), rsCardRevenue.getString(6), rsCardRevenue.getString(7)
		};
		arrListCardRevenue.add(rsCardRevenue.getString(1));
		arrListCardRevenue.add(rsCardRevenue.getString(2));
		arrListCardRevenue.add(rsCardRevenue.getString(3));
		arrListCardRevenue.add(rsCardRevenue.getString(4));
		arrListCardRevenue.add(rsCardRevenue.getString(5));
		arrListCardRevenue.add(rsCardRevenue.getString(6));
		arrListCardRevenue.add(rsCardRevenue.getString(7));
	    }
	    rsCardRevenue.close();
	    arrListShowCardDtl.add(arrListCardRevenue);

	    double totalAmt = totalRechargeAmt - (totalRedeemAmt + totalRefundAmt + totalOpenKOTAmt + totalUnsettleAmt + totalTransferAmt + totalCardRevenue);

	    funPrintShowCardDtlTextfile(arrListShowCardDtl, "", totalAmt, cardNo);

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
	    File Text_CardDtl = new File(filePath + "/Temp");
	    if (!Text_CardDtl.exists())
	    {
		Text_CardDtl.mkdirs();
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    public void funPrintShowCardDtlTextfile(List<List<String>> arrListShowCardDtlTemp, String custName, double totalAmount, String cardNo)
    {
	clsUtility objUtility = new clsUtility();
	try
	{
	    double totalBalance = 0;
	    funCreateTempFolder();
	    String filPath = System.getProperty("user.dir");
	    File textFile = new File(filPath + "/Temp/Temp_Card_Dtl.txt");
	    if (!textFile.exists())
	    {
		textFile.createNewFile();
	    }
	    PrintWriter pw = new PrintWriter(textFile);
	    pw.println(objUtility.funPrintTextWithAlignment("Card Transaction Detail", 40, "Center"));
	    pw.println(" ");
	    pw.print(objUtility.funPrintTextWithAlignment(clsGlobalVarClass.gPOSName, 40, "Center"));
	    pw.println(" ");
	    pw.print(objUtility.funPrintTextWithAlignment(clsGlobalVarClass.gClientName, 40, "Center"));
	    pw.println(" ");
	    pw.println(" ");
	    pw.print(objUtility.funPrintTextWithAlignment("Date", 8, "Left"));
	    pw.print(objUtility.funPrintTextWithAlignment(":", 4, "Left"));
	    pw.print(objUtility.funPrintTextWithAlignment(clsGlobalVarClass.gPOSDate, 28, "Left"));
	    pw.println(" ");
	    pw.print(objUtility.funPrintTextWithAlignment("Card No", 8, "Left"));
	    pw.print(objUtility.funPrintTextWithAlignment(":", 4, "Left"));
	    pw.print(objUtility.funPrintTextWithAlignment(cardNo, 28, "Left"));
	    pw.println(" ");

	    pw.print(objUtility.funPrintTextWithAlignment("Customer Name", 15, "Left"));
	    pw.print(objUtility.funPrintTextWithAlignment(":", 4, "Left"));
	    pw.print(objUtility.funPrintTextWithAlignment(custName, 21, "Left"));
	    pw.println(" ");

	    pw.println("----------------------------------------");
	    pw.print(objUtility.funPrintTextWithAlignment("POS ", 8, "Left"));
	    pw.println(" ");
	    pw.print(objUtility.funPrintTextWithAlignment("", 6, "Left"));
	    pw.print(objUtility.funPrintTextWithAlignment("Trans No", 10, "Left"));
	    pw.print(objUtility.funPrintTextWithAlignment("Date", 12, "Left"));
	    pw.print(objUtility.funPrintTextWithAlignment("Type", 5, "Left"));
	    pw.print(objUtility.funPrintTextWithAlignment("Amt", 7, "RIGHT"));
	    pw.println(" ");
	    pw.println("----------------------------------------");
	    for (int cnt = 0; cnt < arrListShowCardDtlTemp.size(); cnt++)
	    {
		List<String> items = arrListShowCardDtlTemp.get(cnt);
		if (null != items)
		{
		    String amt = items.get(6);
		    Double rechargeAmt = Double.valueOf(amt);
		    pw.print(objUtility.funPrintTextWithAlignment(items.get(0), 8, "Left"));
		    pw.println(" ");
		    pw.print(objUtility.funPrintTextWithAlignment("", 6, "Left"));
		    pw.print(objUtility.funPrintTextWithAlignment(items.get(1), 10, "Left"));
		    pw.print(objUtility.funPrintTextWithAlignment(items.get(2), 12, "Left"));
		    if (items.get(3).equals("Recharge"))
		    {
			pw.print(objUtility.funPrintTextWithAlignment("RC", 5, "Left"));
		    }
		    else if (items.get(3).equals("Redeem"))
		    {
			pw.print(objUtility.funPrintTextWithAlignment("RD", 5, "Left"));
		    }
		    else
		    {
			pw.print(objUtility.funPrintTextWithAlignment("RF", 5, "Left"));
		    }

		    pw.print(objUtility.funPrintTextWithAlignment("" + Math.rint(rechargeAmt), 7, "RIGHT"));
		    pw.println(" ");
		}
	    }

	    pw.println(" ");
	    pw.println("----------------------------------------");
	    pw.println(" ");
	    int row = tblTotal.getSelectedRow();
	    pw.print(objUtility.funPrintTextWithAlignment("", 27, "Left"));
	    pw.print(objUtility.funPrintTextWithAlignment("Total", 6, "Left"));
	    pw.print(objUtility.funPrintTextWithAlignment("" + totalAmount, 7, "RIGHT"));
	    pw.flush();
	    pw.close();

	    clsPrintingUtility objPrintingUtility = new clsPrintingUtility();
	    if (clsGlobalVarClass.gShowBill)
	    {
		objPrintingUtility.funShowTextFile(textFile, "", "");
	    }
	    objUtility.funPrintReportToPrinter(clsGlobalVarClass.gBillPrintPrinterPort, filPath);
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
        };  ;
        pnlMain = new javax.swing.JPanel();
        pnlData = new javax.swing.JTabbedPane();
        pnlCardDetails = new javax.swing.JPanel();
        pnlDetails = new javax.swing.JScrollPane();
        tblDebitCardFlash = new javax.swing.JTable();
        pnltotal = new javax.swing.JScrollPane();
        tblTotal = new javax.swing.JTable();
        btnExport = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();
        btnConsumptionReport = new javax.swing.JButton();
        btnDebitCardStatus = new javax.swing.JButton();
        btnRechargeDetails = new javax.swing.JButton();
        cmbPosCode = new javax.swing.JComboBox();
        lblPOSName = new javax.swing.JLabel();
        txtCustCode = new javax.swing.JTextField();
        lblCustName = new javax.swing.JLabel();
        btnUnusedCardBal = new javax.swing.JButton();
        btnRefundDetails1 = new javax.swing.JButton();
        dteToDate = new com.toedter.calendar.JDateChooser();
        dteFromDate = new com.toedter.calendar.JDateChooser();
        lblFromDate = new javax.swing.JLabel();
        lblToDate = new javax.swing.JLabel();
        cmbExportType = new javax.swing.JComboBox();
        btnUserWiseRechargeDtl = new javax.swing.JButton();

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
        lblProductName.setText("SPOS -  ");
        pnlheader.add(lblProductName);

        lblModuleName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblModuleName.setForeground(new java.awt.Color(255, 255, 255));
        pnlheader.add(lblModuleName);

        lblfromName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblfromName.setForeground(new java.awt.Color(255, 255, 255));
        lblfromName.setText("Debit Card Flash");
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

        pnlBackGround.setLayout(new java.awt.GridBagLayout());

        pnlMain.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, new java.awt.Color(204, 204, 204), new java.awt.Color(204, 204, 204), new java.awt.Color(153, 153, 153), new java.awt.Color(153, 153, 153)));
        pnlMain.setMinimumSize(new java.awt.Dimension(800, 570));
        pnlMain.setOpaque(false);

        pnlCardDetails.setBackground(new java.awt.Color(255, 255, 255));
        pnlCardDetails.setOpaque(false);
        pnlCardDetails.setPreferredSize(new java.awt.Dimension(800, 508));
        pnlCardDetails.setLayout(null);

        tblDebitCardFlash.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String []
            {
                "Bill No", "Date", "Bill Time", "POS Code", "Debit Card No", "Amount", "Grand Total"
            }
        )
        {
            boolean[] canEdit = new boolean []
            {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return canEdit [columnIndex];
            }
        });
        tblDebitCardFlash.setRowHeight(25);
        tblDebitCardFlash.getTableHeader().setReorderingAllowed(false);
        tblDebitCardFlash.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                tblDebitCardFlashMouseClicked(evt);
            }
        });
        pnlDetails.setViewportView(tblDebitCardFlash);

        pnlCardDetails.add(pnlDetails);
        pnlDetails.setBounds(0, 79, 790, 330);

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
        pnltotal.setViewportView(tblTotal);

        pnlCardDetails.add(pnltotal);
        pnltotal.setBounds(0, 410, 790, 50);

        btnExport.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnExport.setForeground(new java.awt.Color(255, 255, 255));
        btnExport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnExport.setText("EXPORT");
        btnExport.setToolTipText("Export file");
        btnExport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExport.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn2.png"))); // NOI18N
        btnExport.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnExportMouseClicked(evt);
            }
        });
        pnlCardDetails.add(btnExport);
        btnExport.setBounds(620, 40, 80, 30);

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
        pnlCardDetails.add(btnClose);
        btnClose.setBounds(710, 40, 80, 30);

        btnConsumptionReport.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        btnConsumptionReport.setForeground(new java.awt.Color(255, 255, 255));
        btnConsumptionReport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgConsumptionreport.png"))); // NOI18N
        btnConsumptionReport.setToolTipText("View Report");
        btnConsumptionReport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnConsumptionReport.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnConsumptionReportActionPerformed(evt);
            }
        });
        pnlCardDetails.add(btnConsumptionReport);
        btnConsumptionReport.setBounds(0, 460, 80, 80);

        btnDebitCardStatus.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        btnDebitCardStatus.setForeground(new java.awt.Color(255, 255, 255));
        btnDebitCardStatus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgDebCardStatus.png"))); // NOI18N
        btnDebitCardStatus.setToolTipText("View Report");
        btnDebitCardStatus.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnDebitCardStatus.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnDebitCardStatusMouseClicked(evt);
            }
        });
        pnlCardDetails.add(btnDebitCardStatus);
        btnDebitCardStatus.setBounds(270, 460, 80, 80);

        btnRechargeDetails.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        btnRechargeDetails.setForeground(new java.awt.Color(255, 255, 255));
        btnRechargeDetails.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgRechargeDetail.png"))); // NOI18N
        btnRechargeDetails.setToolTipText("View Report");
        btnRechargeDetails.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnRechargeDetails.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnRechargeDetailsMouseClicked(evt);
            }
        });
        btnRechargeDetails.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnRechargeDetailsActionPerformed(evt);
            }
        });
        pnlCardDetails.add(btnRechargeDetails);
        btnRechargeDetails.setBounds(90, 460, 80, 80);

        cmbPosCode.setToolTipText("Select POS");
        pnlCardDetails.add(cmbPosCode);
        cmbPosCode.setBounds(80, 0, 190, 30);

        lblPOSName.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblPOSName.setText("POS Name :");
        pnlCardDetails.add(lblPOSName);
        lblPOSName.setBounds(0, 0, 70, 30);

        txtCustCode.setToolTipText("Select Customer Date");
        txtCustCode.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                txtCustCodeMouseClicked(evt);
            }
        });
        pnlCardDetails.add(txtCustCode);
        txtCustCode.setBounds(80, 40, 190, 30);

        lblCustName.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblCustName.setText("Cust Name :");
        pnlCardDetails.add(lblCustName);
        lblCustName.setBounds(0, 40, 70, 30);

        btnUnusedCardBal.setBackground(new java.awt.Color(255, 255, 255));
        btnUnusedCardBal.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        btnUnusedCardBal.setText("<html>Unused<br> Card<br> Balance</html>");
        btnUnusedCardBal.setToolTipText("View Report");
        btnUnusedCardBal.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnUnusedCardBal.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnUnusedCardBalMouseClicked(evt);
            }
        });
        btnUnusedCardBal.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnUnusedCardBalActionPerformed(evt);
            }
        });
        pnlCardDetails.add(btnUnusedCardBal);
        btnUnusedCardBal.setBounds(360, 460, 80, 80);

        btnRefundDetails1.setBackground(new java.awt.Color(255, 255, 255));
        btnRefundDetails1.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        btnRefundDetails1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgRefundDetails.png"))); // NOI18N
        btnRefundDetails1.setToolTipText("View Report");
        btnRefundDetails1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnRefundDetails1.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnRefundDetails1MouseClicked(evt);
            }
        });
        btnRefundDetails1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnRefundDetails1ActionPerformed(evt);
            }
        });
        pnlCardDetails.add(btnRefundDetails1);
        btnRefundDetails1.setBounds(180, 460, 80, 80);

        dteToDate.setToolTipText("Select From Date");
        dteToDate.setPreferredSize(new java.awt.Dimension(119, 35));
        pnlCardDetails.add(dteToDate);
        dteToDate.setBounds(670, 0, 119, 30);

        dteFromDate.setToolTipText("Select From Date");
        dteFromDate.setPreferredSize(new java.awt.Dimension(119, 35));
        pnlCardDetails.add(dteFromDate);
        dteFromDate.setBounds(410, 0, 119, 30);

        lblFromDate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblFromDate.setText("From Date :");
        pnlCardDetails.add(lblFromDate);
        lblFromDate.setBounds(340, 0, 70, 30);

        lblToDate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblToDate.setText("To Date    :");
        pnlCardDetails.add(lblToDate);
        lblToDate.setBounds(600, 0, 70, 30);

        cmbExportType.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        cmbExportType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Excel", "PDF" }));
        pnlCardDetails.add(cmbExportType);
        cmbExportType.setBounds(480, 40, 130, 30);

        btnUserWiseRechargeDtl.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        btnUserWiseRechargeDtl.setForeground(new java.awt.Color(255, 255, 255));
        btnUserWiseRechargeDtl.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgUserWiseRechargeDetail.png"))); // NOI18N
        btnUserWiseRechargeDtl.setToolTipText("View Report");
        btnUserWiseRechargeDtl.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnUserWiseRechargeDtl.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnUserWiseRechargeDtlMouseClicked(evt);
            }
        });
        btnUserWiseRechargeDtl.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnUserWiseRechargeDtlActionPerformed(evt);
            }
        });
        pnlCardDetails.add(btnUserWiseRechargeDtl);
        btnUserWiseRechargeDtl.setBounds(450, 460, 80, 80);

        pnlData.addTab("Data", pnlCardDetails);

        javax.swing.GroupLayout pnlMainLayout = new javax.swing.GroupLayout(pnlMain);
        pnlMain.setLayout(pnlMainLayout);
        pnlMainLayout.setHorizontalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 796, Short.MAX_VALUE)
            .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnlMainLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(pnlData, javax.swing.GroupLayout.PREFERRED_SIZE, 800, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        pnlMainLayout.setVerticalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 620, Short.MAX_VALUE)
            .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnlMainLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(pnlData, javax.swing.GroupLayout.PREFERRED_SIZE, 620, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        pnlBackGround.add(pnlMain, new java.awt.GridBagConstraints());

        getContentPane().add(pnlBackGround, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * this function is used Export File in System
     *
     * @param evt
     */
    private void btnExportMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnExportMouseClicked
	// TODO add your handling code here:
	funExportReports();
    }//GEN-LAST:event_btnExportMouseClicked
    /**
     * Close Window
     *
     * @param evt
     */
    private void btnCloseMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCloseMouseClicked
	funResetLookAndFeel();
	dispose();
	clsGlobalVarClass.hmActiveForms.remove("DebitCardFlashReports");
    }//GEN-LAST:event_btnCloseMouseClicked

    private void btnDebitCardStatusMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnDebitCardStatusMouseClicked
	// TODO add your handling code here:
	exportFormName = "Debit Card Status Details";
	funDebitCardStatusReport();
    }//GEN-LAST:event_btnDebitCardStatusMouseClicked
    /**
     * 8
     * this function is used for Recharge Details Report
     *
     * @param evt
     */
    private void btnRechargeDetailsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnRechargeDetailsMouseClicked
	// TODO add your handling code here:

    }//GEN-LAST:event_btnRechargeDetailsMouseClicked

    /**
     * // * this Function search Customer From Customer Master
     *
     * @param evt
     */
    private void txtCustCodeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txtCustCodeMouseClicked
	// TODO add your handling code here:
	objUtility.funCallForSearchForm("CustomerMaster");
	new frmSearchFormDialog(this, true).setVisible(true);
	if (clsGlobalVarClass.gSearchItemClicked)
	{
	    Object[] data = clsGlobalVarClass.gArrListSearchData.toArray();
	    funSetCustomerData(data);
	    clsGlobalVarClass.gSearchItemClicked = false;
	}
    }//GEN-LAST:event_txtCustCodeMouseClicked

    private void btnRechargeDetailsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRechargeDetailsActionPerformed
	// TODO add your handling code here:
	reportType = "RechargeDetails";
	exportFormName = "Debit Card Recharge Details";
	funDebitCardRechargeDtlReport();
    }//GEN-LAST:event_btnRechargeDetailsActionPerformed

    private void btnConsumptionReportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConsumptionReportActionPerformed
	// TODO add your handling code here:
	exportFormName = "Debit Card Cunsumption Report";
	funDebitCardConsumptionReport();
    }//GEN-LAST:event_btnConsumptionReportActionPerformed

    private void btnUnusedCardBalMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnUnusedCardBalMouseClicked
	// TODO add your handling code here:
    }//GEN-LAST:event_btnUnusedCardBalMouseClicked

    private void btnUnusedCardBalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUnusedCardBalActionPerformed
	// TODO add your handling code here:
	reportType = "UnusedCardBalance";
	exportFormName = "Debit Card Unused Balance";
	funShowUnusedCardBalance();
    }//GEN-LAST:event_btnUnusedCardBalActionPerformed

    private void btnRefundDetails1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnRefundDetails1MouseClicked
	// TODO add your handling code here:
	funDebitCardRefundDtlReport();
    }//GEN-LAST:event_btnRefundDetails1MouseClicked

    private void btnRefundDetails1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefundDetails1ActionPerformed
	// TODO add your handling code here:
    }//GEN-LAST:event_btnRefundDetails1ActionPerformed

    private void tblDebitCardFlashMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblDebitCardFlashMouseClicked
	// TODO add your handling code here:
	if (reportType.equals("UnusedCardBalance"))
	{
	    int row = tblDebitCardFlash.getSelectedRow();
	    funOpenDebitCardDtlPrint(tblDebitCardFlash.getValueAt(row, 0).toString());
	}
    }//GEN-LAST:event_tblDebitCardFlashMouseClicked

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("DebitCardFlashReports");
    }//GEN-LAST:event_btnCloseActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("DebitCardFlashReports");
    }//GEN-LAST:event_formWindowClosed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("DebitCardFlashReports");
    }//GEN-LAST:event_formWindowClosing

    private void btnUserWiseRechargeDtlMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_btnUserWiseRechargeDtlMouseClicked
    {//GEN-HEADEREND:event_btnUserWiseRechargeDtlMouseClicked
	// TODO add your handling code here:
    }//GEN-LAST:event_btnUserWiseRechargeDtlMouseClicked

    private void btnUserWiseRechargeDtlActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnUserWiseRechargeDtlActionPerformed
    {//GEN-HEADEREND:event_btnUserWiseRechargeDtlActionPerformed
	reportType = "UserWiseRechargeDetails";
	exportFormName = "User Wise Debit Card Recharge Details";
	funUserWiseDebitCardRechargeDtlReport();
    }//GEN-LAST:event_btnUserWiseRechargeDtlActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnConsumptionReport;
    private javax.swing.JButton btnDebitCardStatus;
    private javax.swing.JButton btnExport;
    private javax.swing.JButton btnRechargeDetails;
    private javax.swing.JButton btnRefundDetails1;
    private javax.swing.JButton btnUnusedCardBal;
    private javax.swing.JButton btnUserWiseRechargeDtl;
    private javax.swing.JComboBox cmbExportType;
    private javax.swing.JComboBox cmbPosCode;
    private com.toedter.calendar.JDateChooser dteFromDate;
    private com.toedter.calendar.JDateChooser dteToDate;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.JLabel lblCustName;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblFromDate;
    private javax.swing.JLabel lblHOSign;
    private javax.swing.JLabel lblModuleName;
    private javax.swing.JLabel lblPOSName;
    private javax.swing.JLabel lblPosName;
    private javax.swing.JLabel lblProductName;
    private javax.swing.JLabel lblToDate;
    private javax.swing.JLabel lblUserCode;
    private javax.swing.JLabel lblfromName;
    private javax.swing.JPanel pnlBackGround;
    private javax.swing.JPanel pnlCardDetails;
    private javax.swing.JTabbedPane pnlData;
    private javax.swing.JScrollPane pnlDetails;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JPanel pnlheader;
    private javax.swing.JScrollPane pnltotal;
    private javax.swing.JTable tblDebitCardFlash;
    private javax.swing.JTable tblTotal;
    private javax.swing.JTextField txtCustCode;
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

    private void funUserWiseDebitCardRechargeDtlReport()
    {
	StringBuilder sbSql = new StringBuilder();
	try
	{
	    totalDm = new javax.swing.table.DefaultTableModel()
	    {
		boolean[] canEdit = new boolean[]
		{
		    false, false, false, false
		};

		public boolean isCellEditable(int rowIndex, int columnIndex)
		{
		    return canEdit[columnIndex];
		}
	    };
	    tblTotal.setRowHeight(30);
	    totalDm.addColumn("");
	    totalDm.addColumn("");
	    totalDm.addColumn("");
	    totalDm.addColumn("");

	    dm = new javax.swing.table.DefaultTableModel()
	    {
		boolean[] canEdit = new boolean[]
		{
		    false, false, false, false
		};

		public boolean isCellEditable(int rowIndex, int columnIndex)
		{
		    return canEdit[columnIndex];
		}
	    };
	    tblDebitCardFlash.setRowHeight(25);

	    dm.addColumn("POS");
	    dm.addColumn("USER");
	    dm.addColumn("SETTLEMENT");
	    dm.addColumn("RECHARGE AMOUNT");
	    tblDebitCardFlash.setModel(dm);
	    String fromDate = funGetCalanderDate("From");
	    String toDate = funGetCalanderDate("To");
	    double totalRechargeAmt = 0;
	    sbSql.append("select d.strPosName,ifnull(e.strUserName,'NA'),g.strSettelmentDesc, sum(f.dblRechargeAmt) "
		    + "from tbldebitcardmaster a "
		    + "left outer join tblcustomermaster b on a.strCustomerCode=b.strCustomerCode  "
		    + "left outer join tbldebitcardrecharge c on a.strCardString=c.strCardString  "
		    + "left outer join tblposmaster d on c.strPOSCode=d.strPosCode  "
		    + "left outer join tbluserhd e on c.strUserCreated=e.strUserCode "
		    + "left outer join tbldcrechargesettlementdtl f on c.intRechargeNo=f.strRechargeNo "
		    + "left outer join tblsettelmenthd g on f.strSettlementCode =g.strSettelmentCode "
		    + "where date(c.dteDateCreated) between '" + fromDate + "' and '" + toDate + "' "
		    + "AND c.strTransferBalance<>'Y' ");
	    if (!cmbPosCode.getSelectedItem().toString().equals("All"))
	    {
		sbSql.append(" and c.strPOSCode='" + objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString().trim()) + "' ");
	    }
	    sbSql.append("group by d.strPosCode,e.strUserName,g.strSettelmentDesc "
		    + "order by d.strPosCode,e.strUserName,g.strSettelmentDesc");
	    System.out.println(sbSql.toString());
	    ResultSet rsRechargeDtl = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
	    while (rsRechargeDtl.next())
	    {
		double rechargeAmt = rsRechargeDtl.getDouble(4);
		Object[] ob =
		{
		    rsRechargeDtl.getString(1), rsRechargeDtl.getString(2), rsRechargeDtl.getString(3), gDecimalFormat.format(rsRechargeDtl.getString(4))
		};
		//totalRechargeAmt+=rsRechargeDtl.getDouble(7);
		totalRechargeAmt += rechargeAmt;
		dm.addRow(ob);
	    }
	    rsRechargeDtl.close();
	    tblDebitCardFlash.setModel(dm);

	    Object[] total =
	    {
		"TOTAL", "", "", gDecimalFormat.format(totalRechargeAmt)
	    };
	    totalDm.addRow(total);
	    tblTotal.setModel(totalDm);

	    DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
	    rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
//            tblDebitCardFlash.getColumnModel().getColumn(0).setPreferredWidth(80);
//            tblDebitCardFlash.getColumnModel().getColumn(1).setPreferredWidth(80);
//            tblDebitCardFlash.getColumnModel().getColumn(2).setPreferredWidth(80);
//            tblDebitCardFlash.getColumnModel().getColumn(3).setPreferredWidth(70);
//            tblDebitCardFlash.getColumnModel().getColumn(4).setPreferredWidth(50);
//            tblDebitCardFlash.getColumnModel().getColumn(5).setPreferredWidth(100);
//            tblDebitCardFlash.getColumnModel().getColumn(6).setPreferredWidth(80);
//            tblDebitCardFlash.getColumnModel().getColumn(7).setPreferredWidth(50);
//            tblDebitCardFlash.getColumnModel().getColumn(8).setPreferredWidth(80);

	    tblDebitCardFlash.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
	    //tblDebitCardFlash.getColumnModel().getColumn(0).setPreferredWidth(700);

//            tblTotal.getColumnModel().getColumn(0).setPreferredWidth(80);
//            tblTotal.getColumnModel().getColumn(1).setPreferredWidth(80);
//            tblTotal.getColumnModel().getColumn(2).setPreferredWidth(80);
//            tblTotal.getColumnModel().getColumn(3).setPreferredWidth(70);
//            tblTotal.getColumnModel().getColumn(4).setPreferredWidth(50);
//            tblTotal.getColumnModel().getColumn(5).setPreferredWidth(100);
//            tblTotal.getColumnModel().getColumn(6).setPreferredWidth(80);
//            tblTotal.getColumnModel().getColumn(7).setPreferredWidth(50);
//            tblTotal.getColumnModel().getColumn(8).setPreferredWidth(190);
	    tblTotal.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
	    tblTotal.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

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
}
