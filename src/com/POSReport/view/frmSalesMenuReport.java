/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.POSReport.view;

import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsManagerReportBean;
import com.POSGlobal.controller.clsPosConfigFile;
import com.POSGlobal.controller.clsSalesFlashReport;
import com.POSGlobal.controller.clsSettelementOptions;
import com.POSGlobal.controller.clsUtility;
import com.POSGlobal.view.frmOkPopUp;
import com.POSGlobal.view.frmShowTextFile;
import com.POSReport.controller.clsAdvanceOrderReport;
import com.POSReport.controller.clsAuditorsReport;
import com.POSReport.controller.comparator.clsBillComparator;
import com.POSReport.controller.clsBillItemDtlBean;
import com.POSReport.controller.clsBillRegisterReport;
import com.POSReport.controller.clsBillWiseSalesReport;
import com.POSReport.controller.clsBlindSettlementWiseReport;
import com.POSReport.controller.clsComplimentaryBillReport;
import com.POSReport.controller.clsConsolidatedDiscountReport;
import com.POSReport.controller.clsCounterWiseReport;
import com.POSReport.controller.clsCreaditBillOutstandingReport;
import com.POSReport.controller.clsCreditReport;
import com.POSReport.controller.clsCustomerLedgerReport;
import com.POSReport.controller.clsDailyCollectionReport;
import com.POSReport.controller.clsDailySalesReport;
import com.POSReport.controller.clsDebtorsAsOnReport;
import com.POSReport.controller.clsDeliveryBoyWiseCashTakenReport;
import com.POSReport.controller.clsDeliveryboyIncentivesReport;
import com.POSReport.controller.clsDiscountWiseReport;
import com.POSReport.controller.clsGroupSubGroupWiseReport;
import com.POSReport.controller.clsGroupWiseReport;
import com.POSReport.controller.clsGuestCreditReport;
import com.POSReport.controller.clsItemMasterListingReport;
import com.POSReport.controller.clsItemWiseConsumptionReport;
import com.POSReport.controller.clsItemWiseSalesReport;
import com.POSReport.controller.clsMenuHeadWiseSalesReport;
import com.POSReport.controller.clsNonChargableKOTReport;
import com.POSReport.controller.clsNonSellingItemsReport;
import com.POSReport.controller.clsOpenItemWiseAuditReport;
import com.POSReport.controller.clsOperatorWiseReport;
import com.POSReport.controller.clsOrderAnalysisReport;
import com.POSReport.controller.clsPaymentReceiptReport;
import com.POSReport.controller.clsPlacedOrderReport;
import com.POSReport.controller.clsPostingReport;
import com.POSReport.controller.clsRevenueHeadWiseReport;
import com.POSReport.controller.clsSettlementWiseReport;
import com.POSReport.controller.clsSubGroupWiseReport;
import com.POSReport.controller.clsSubGroupWiseSummaryReport;
import com.POSReport.controller.clsTableWisePax;
import com.POSReport.controller.clsTaxBreakupSummaryReport;
import com.POSReport.controller.clsTaxWiseSalesReport;
import com.POSReport.controller.clsUnusedCardBalanceReport;
import com.POSReport.controller.clsUtilityForAuditReport;
import com.POSReport.controller.clsVoidBillReport;
import com.POSReport.controller.clsVoidKOTReport;
import com.POSReport.controller.clsWaiterWiseIncentiveSalesReport;
import com.POSReport.controller.clsWaiterWiseItemReport;
import com.POSReport.controller.clsWaiterWiseItemWiseIncentivesSummaryReport;
import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;

public class frmSalesMenuReport extends javax.swing.JFrame
{

    public String fromDate, toDate, discount, total, imagePath;
    DefaultTableModel dm, totalDm;
    private String salesReportName;
    private String reportName, userCode, dpCode = "";
    private String settleCode = "";
    private clsUtility objUtility;
    private StringBuilder sb = new StringBuilder();
    private final String gDecimalFormatString = clsGlobalVarClass.funGetGlobalDecimalFormatString();
    private HashMap<String, String> mapCustomerNameCode;

    public frmSalesMenuReport()
    {
	//initComponents();
    }

    public frmSalesMenuReport(String repName)
    {
	initComponents();
	disReportName.setText(repName);

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

	    objUtility = new clsUtility();
	    salesReportName = repName;
	    dteFromDate.setDate(objUtility.funGetDateToSetCalenderDate());
	    dteToDate.setDate(objUtility.funGetDateToSetCalenderDate());
	    //imagePath = clsPosConfigFile.ReportImagePath;
	    imagePath = System.getProperty("user.dir");
	    imagePath = imagePath + File.separator + "ReportImage";
	    System.out.println("imagePath=" + imagePath);

	    if (clsGlobalVarClass.gShowOnlyLoginPOSReports)
	    {
		cmbPosCode.addItem(clsGlobalVarClass.gPOSName + "                                        " + clsGlobalVarClass.gPOSCode);
	    }
	    else
	    {
		cmbPosCode.addItem("All");
		sb.setLength(0);
		sb.append("select strPosName,strPosCode from tblposmaster");
		ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
		while (rs.next())
		{
		    cmbPosCode.addItem(rs.getString(1) + "                                        " + rs.getString(2));
		}
		rs.close();
	    }

	    panelExtraComp.setVisible(true);
	    cmbResonMaster.setVisible(false);
	    lblResonMaster.setVisible(false);
	    lblSettlementMode.setVisible(false);
	    cmbSettlementMode.setVisible(false);
	    lblUserName.setVisible(false);
	    cmbUserName.setVisible(false);
	    lblPaymentMode.setVisible(false);
	    cmbPaymentMode.setVisible(false);
	    cmbWaiterCode.setVisible(false);
	    lblWaiterName.setVisible(false);
	    lblDeliverboyName.setVisible(false);
	    cmbDeliveryboy.setVisible(false);
	    lblReportMode.setVisible(false);
	    cmbReportMode.setVisible(false);
	    lblShiftNo.setVisible(false);
	    cmbShiftNo.setVisible(false);
	    funFillShiftCombo();

	    cmbReportType.removeAllItems();
	    cmbReportType.addItem("A4 Size Report");
	    cmbReportType.addItem("Excel Report");
	    // cmbReportType.addItem("Text File-40 Column Report");

	    if (salesReportName.equals("Delivery boy Incentives Report"))
	    {
		panelExtraComp.setVisible(true);
		lblDeliverboyName.setVisible(true);
		cmbDeliveryboy.setVisible(true);
		cmbDeliveryboy.addItem("All");
		sb.setLength(0);
		sb.append("select strDPName from tbldeliverypersonmaster");
		ResultSet rsUserName = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
		while (rsUserName.next())
		{
		    cmbDeliveryboy.addItem(rsUserName.getString(1));
		}
		lblReportMode.setVisible(true);
		cmbReportMode.setVisible(true);
	    }
	    if (salesReportName.equals("Delivery Boy Wise Cash Taken"))
	    {
		panelExtraComp.setVisible(true);
		lblUserName.setText("Delivery Boy             :     ");
		lblUserName.setVisible(true);
		cmbUserName.setVisible(true);

		cmbUserName.addItem("All");
		sb.setLength(0);
		sb.append("select strDPName from tbldeliverypersonmaster");
		ResultSet rsUserName = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
		while (rsUserName.next())
		{
		    cmbUserName.addItem(rsUserName.getString(1));
		}
	    }

	    if (salesReportName.equals("Operator Wise Sales"))
	    {
		panelExtraComp.setVisible(true);
		lblUserName.setText("User Name               :    ");
		lblUserName.setVisible(true);
		cmbUserName.setVisible(true);
		lblPaymentMode.setText("Settlement Type      :    ");
		lblPaymentMode.setVisible(true);
		cmbPaymentMode.setVisible(true);

		cmbPaymentMode.addItem("All");
		cmbUserName.addItem("All");
		sb.setLength(0);
		sb.append("select strUserName from tbluserhd");
		ResultSet rsUserName = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
		while (rsUserName.next())
		{
		    cmbUserName.addItem(rsUserName.getString(1));
		}
		sb.setLength(0);
		sb.append("select strSettelmentDesc from tblsettelmenthd ");
		ResultSet rsFillcmbpayment = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
		while (rsFillcmbpayment.next())
		{
		    cmbPaymentMode.addItem(rsFillcmbpayment.getString(1));
		}
		rsFillcmbpayment.close();
	    }

	    if ((salesReportName.equals("Non Chargable KOT Report")))
	    {
		cmbReportType.removeAllItems();
		cmbReportType.addItem("A4 Size Report");
		cmbReportType.addItem("Excel Report");
		cmbReportType.addItem("Text File-40 Column Report");

		panelExtraComp.setVisible(true);
		cmbResonMaster.setVisible(true);
		lblResonMaster.setText("Reason Master          :    ");
		lblResonMaster.setVisible(true);
		cmbResonMaster.addItem("All                                         All");
		sb.setLength(0);
		sb.append("select strReasonName,strReasonCode from tblreasonmaster");
		ResultSet rsReson = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
		while (rsReson.next())
		{
		    cmbResonMaster.addItem(rsReson.getString(1) + "                                         " + rsReson.getString(2));
		}
		rsReson.close();

	    }

	    if ((salesReportName.equals("Waiter Wise Item Report")))
	    {
		panelExtraComp.setVisible(true);
		cmbWaiterCode.setVisible(true);
		lblWaiterName.setText("Waiter Name            :     ");
		lblWaiterName.setVisible(true);
		cmbWaiterCode.addItem("All");
		sb.setLength(0);
		sb.append("select strWShortName,strWaiterNo from tblwaitermaster");
		ResultSet rsWaiter = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
		while (rsWaiter.next())
		{
		    cmbWaiterCode.addItem(rsWaiter.getString(1) + "                                         " + rsWaiter.getString(2));
		}
		rsWaiter.close();
	    }

	    if ((salesReportName.equals("Waiter Wise Incentives Report")))
	    {
		lblSettlementMode.setVisible(true);
		cmbSettlementMode.setVisible(true);
		lblSettlementMode.setText("Select Type              :    ");
		panelExtraComp.setVisible(true);
		cmbSettlementMode.removeAllItems();
		cmbSettlementMode.addItem("Summary");
		cmbSettlementMode.addItem("Detail");
	    }

	    if ((salesReportName.equals("Counter Wise Sales Report")))
	    {
		cmbSettlementMode.setVisible(true);
		lblSettlementMode.setVisible(true);
		lblSettlementMode.setText("Select Type              :    ");
		cmbSettlementMode.removeAllItems();
		cmbSettlementMode.addItem("Menu Wise");
		cmbSettlementMode.addItem("Group Wise");
		cmbSettlementMode.addItem("Sub Group Wise");
		panelExtraComp.setVisible(true);
	    }
	    if ((salesReportName.equals("Group Wise Sales")))
	    {
		panelExtraComp.setVisible(true);
		cmbWaiterCode.setVisible(true);
		lblWaiterName.setText("Sub Group                 :    ");
		cmbWaiterCode.removeAllItems();
		lblWaiterName.setVisible(true);
		cmbWaiterCode.addItem("All                                         All");
		sb.setLength(0);
		sb.append("select strSubGroupName,strSubGroupCode from tblsubgrouphd");
		ResultSet rsSubGroup = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
		while (rsSubGroup.next())
		{
		    cmbWaiterCode.addItem(rsSubGroup.getString(1) + "                                         " + rsSubGroup.getString(2));
		}
		rsSubGroup.close();
	    }

	    if ((salesReportName.equals("GroupSubGroup Wise Report")))
	    {
		cmbReportType.removeAllItems();
		cmbReportType.addItem("A4 Size Report");
		cmbReportType.addItem("Excel Report");

		panelExtraComp.setVisible(true);
		//For Group Name
		lblUserName.setVisible(true);
		lblUserName.setText("Group Name              :   ");
		cmbUserName.setVisible(true);
		cmbUserName.removeAllItems();
		//For SubGroup Name
		lblSettlementMode.setVisible(true);
		lblSettlementMode.setText("SubGroup Name          :   ");
		cmbSettlementMode.setVisible(true);
		cmbSettlementMode.removeAllItems();
		sb.setLength(0);
		sb.append("select strGroupName,strGroupCode from tblgrouphd order by strGroupName asc");
		ResultSet rsSubGroup = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
		cmbUserName.addItem("All                                         All");
		while (rsSubGroup.next())
		{
		    cmbUserName.addItem(rsSubGroup.getString(1) + "                                         " + rsSubGroup.getString(2));
		}
		rsSubGroup.close();
		sb.setLength(0);
		sb.append("select strSubGroupName,strSubGroupCode from tblsubgrouphd order by strSubGroupName asc");
		rsSubGroup = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
		cmbSettlementMode.addItem("All                                         All");
		while (rsSubGroup.next())
		{
		    cmbSettlementMode.addItem(rsSubGroup.getString(1) + "                                         " + rsSubGroup.getString(2));
		}
		rsSubGroup.close();

		cmbUserName.addActionListener(new ActionListener()
		{

		    @Override
		    public void actionPerformed(ActionEvent e)
		    {
			try
			{

			    String group = cmbUserName.getSelectedItem().toString();
			    String groupCode = group.substring(group.lastIndexOf(" "), group.length()).trim();

			    sb.setLength(0);
			    sb.append("select strSubGroupName,strSubGroupCode "
				    + "from tblsubgrouphd ");
			    if (!groupCode.equalsIgnoreCase("All"))
			    {
				sb.append("where strGroupCode='" + groupCode + "' ");
			    }

			    sb.append("order by strSubGroupName asc");
			    ResultSet rsSubGroup = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
			    cmbSettlementMode.removeAllItems();
			    cmbSettlementMode.addItem("All                                         All");
			    while (rsSubGroup.next())
			    {
				cmbSettlementMode.addItem(rsSubGroup.getString(1) + "                                         " + rsSubGroup.getString(2));
			    }
			    rsSubGroup.close();

			}
			catch (Exception ex)
			{
			    ex.printStackTrace();
			}
		    }
		});
		cmbPaymentMode.setVisible(true);
		lblPaymentMode.setVisible(true);
		lblPaymentMode.setText("Select Type              :    ");
		cmbPaymentMode.removeAllItems();
		cmbPaymentMode.addItem("Summary");
		cmbPaymentMode.addItem("Detail");
		cmbPaymentMode.addActionListener(new ActionListener()
		{

		    @Override
		    public void actionPerformed(ActionEvent e)
		    {
			try
			{
			    if (cmbPaymentMode.getSelectedItem().toString().equalsIgnoreCase("Summary"))
			    {
				cmbReportType.removeAllItems();
				cmbReportType.addItem("A4 Size Report");
				cmbReportType.addItem("Excel Report");

			    }
			    else
			    {
				cmbReportType.removeAllItems();
				cmbReportType.addItem("A4 Size Report");
				cmbReportType.addItem("Excel Report");
				cmbReportType.addItem("Text File-40 Column Report");
			    }
			}
			catch (Exception ex)
			{
			    ex.printStackTrace();
			}
		    }
		});
	    }

	    if ((salesReportName.equals("Complimentary Settlement Report")))
	    {

		cmbReportType.removeAllItems();
		cmbReportType.addItem("A4 Size Report");
		cmbReportType.addItem("Excel Report");
		cmbReportType.addItem("Text File-40 Column Report");

		panelExtraComp.setVisible(true);
		cmbResonMaster.setVisible(true);
		lblResonMaster.setText("Reason Master          :    ");
		lblResonMaster.setVisible(true);
		cmbResonMaster.addItem("All                                         All");
		sb.setLength(0);
		sb.append("select strReasonName,strReasonCode from tblreasonmaster");
		ResultSet rsReson = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
		while (rsReson.next())
		{
		    cmbResonMaster.addItem(rsReson.getString(1) + "                                         " + rsReson.getString(2));
		}
		rsReson.close();

		cmbSettlementMode.setVisible(true);
		lblSettlementMode.setVisible(true);
		lblSettlementMode.setText("Select Type              :    ");
		cmbSettlementMode.removeAllItems();
		cmbSettlementMode.addItem("Summary");
		cmbSettlementMode.addItem("Detail");
		cmbSettlementMode.addItem("Group Wise");

	    }

	    if ((salesReportName.equals("Void Bill Report")))
	    {
		panelExtraComp.setVisible(true);
		cmbResonMaster.setVisible(true);
		lblResonMaster.setText("Reason Master          :     ");
		lblResonMaster.setVisible(true);
		cmbResonMaster.addItem("All                                         All");
		sb.setLength(0);
		sb.append("select strReasonName,strReasonCode from tblreasonmaster");
		ResultSet rsReson = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
		while (rsReson.next())
		{
		    cmbResonMaster.addItem(rsReson.getString(1) + "                                         " + rsReson.getString(2));
		}
		rsReson.close();
		lblReportMode.setVisible(true);
		cmbReportMode.setVisible(true);
		cmbReportMode.removeAllItems();
		cmbReportMode.addItem("Summary");
		cmbReportMode.addItem("Detail");
	    }
	    if ((salesReportName.equals("Void KOT Report")))
	    {
		panelExtraComp.setVisible(true);
		lblUserName.setVisible(true);
		cmbUserName.setVisible(true);
		lblUserName.setText("Report Sub Type      :     ");
		cmbUserName.removeAllItems();
		cmbUserName.addItem("All");
		cmbUserName.addItem("Void KOT");
		cmbUserName.addItem("Move KOT");
	    }

	    //for revenuhead wise report
	    if ((salesReportName.equals("Revenue Head Wise Item Sales")))
	    {
		panelExtraComp.setVisible(true);
		lblUserName.setVisible(true);
		lblUserName.setText("Revenue Head           :   ");
		cmbUserName.setVisible(true);
		cmbUserName.removeAllItems();
		lblSettlementMode.setVisible(true);
		lblSettlementMode.setText("Report Type           :   ");
		cmbSettlementMode.setVisible(true);
		sb.setLength(0);
		sb.append("select distinct(strRevenueHead) from tblitemmaster order by strRevenueHead; ");
		ResultSet rsSubGroup = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
		cmbUserName.addItem("All");
		while (rsSubGroup.next())
		{
		    cmbUserName.addItem(rsSubGroup.getString(1));
		}
		rsSubGroup.close();
	    }

	    if ((salesReportName.equals("UnusedCardBalanceReport")))
	    {
		disReportName.setText("Unused Card Balance Report");
	    }

	    if ((salesReportName.equals("SubGroupWiseSummaryReport")))
	    {
		disReportName.setText("SubGroup Wise Summary Report");
	    }

	    //for Placed Order Report
	    if ((salesReportName.equals("Placed Order Report")))
	    {
		panelExtraComp.setVisible(true);
		lblUserName.setVisible(true);
		lblUserName.setText("Order Type              :   ");
		cmbUserName.setVisible(true);
		cmbUserName.removeAllItems();
		cmbUserName.addItem("All");
		cmbUserName.addItem("Normal");
		cmbUserName.addItem("Advance");
		cmbUserName.addItem("Urgent");
	    }

	    //advance order jasper report
	    if ((salesReportName.equals("Advance Order Report")))
	    {
		panelExtraComp.setVisible(true);
		lblUserName.setVisible(true);
		lblUserName.setText("Order Type              :   ");
		cmbUserName.setVisible(true);
		cmbUserName.removeAllItems();
		cmbUserName.addItem("All");
		cmbUserName.addItem("Advance Order");
		cmbUserName.addItem("Urgent Order");
	    }

	    //void advance order jasper report
	    if ((salesReportName.equals("Void Advance Order Report")))
	    {
		panelExtraComp.setVisible(true);
		lblUserName.setVisible(true);
		lblUserName.setText("Order Type              :   ");
		cmbUserName.setVisible(true);
		cmbUserName.removeAllItems();
		cmbUserName.addItem("All");
		cmbUserName.addItem("Advance Order");
		cmbUserName.addItem("Urgent Order");
	    }

	    //discount report
	    if ((salesReportName.equals("Credit Bill Outstanding Report")))
	    {
		panelExtraComp.setVisible(true);
		lblUserName.setVisible(true);
		lblUserName.setText("Report Type             :    ");
		cmbUserName.setVisible(true);
		cmbUserName.removeAllItems();
		cmbUserName.addItem("Summary");
		cmbUserName.addItem("Detail");
	    }

	    //Customer Ledger
	    if ((salesReportName.equals("Customer Ledger")))
	    {
		panelExtraComp.setVisible(true);
		lblUserName.setVisible(true);
		lblUserName.setText("Customer                :     ");
		cmbUserName.setVisible(true);
		cmbUserName.removeAllItems();
		mapCustomerNameCode = new HashMap<String, String>();
		sb.setLength(0);
		sb.append("select strCustomerCode,strCustomerName "
			+ "from "
			+ "(select b.strCustomerName strCustomerName,a.strCustomerCode strCustomerCode "
			+ "from tblbillhd a,tblcustomermaster b "
			+ "where a.strCustomerCode=b.strCustomerCode "
			+ "union  "
			+ "select  b.strCustomerName strCustomerName,a.strCustomerCode strCustomerCode "
			+ "from tblqbillhd a,tblcustomermaster b "
			+ "where a.strCustomerCode=b.strCustomerCode "
			+ ") b "
			+ "order by b.strCustomerName ");
		ResultSet rsSubGroup = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
		while (rsSubGroup.next())
		{
		    cmbUserName.addItem(rsSubGroup.getString(2));

		    mapCustomerNameCode.put(rsSubGroup.getString(2), rsSubGroup.getString(1));
		}
		rsSubGroup.close();
	    }

	    //discount report
	    if ((salesReportName.equals("Discount Report")))
	    {
		panelExtraComp.setVisible(true);
		lblUserName.setVisible(true);
		lblUserName.setText("Report Type             :   ");
		cmbUserName.setVisible(true);
		cmbUserName.removeAllItems();
		cmbUserName.addItem("Summary");
		cmbUserName.addItem("Detail");
	    }

	    //consolidated discount report
	    if ((salesReportName.equals("Consolidated Discount Report")))
	    {
		panelExtraComp.setVisible(true);
		lblUserName.setVisible(true);
		lblUserName.setText("Report Type             :   ");
		cmbUserName.setVisible(true);
		cmbUserName.removeAllItems();
		cmbUserName.addItem("Consolidated Discount");

		cmbReportType.removeAllItems();
		cmbReportType.addItem("A4 Size Report");
	    }

	    if ((salesReportName.equals("Bill Wise Sales")))
	    {
		cmbReportType.removeAllItems();
		cmbReportType.addItem("A4 Size Report");
		cmbReportType.addItem("Excel Report");
		cmbReportType.addItem("Text File-40 Column Report");
	    }

	    if ((salesReportName.equals("Item Wise Sales")))
	    {
		cmbReportType.removeAllItems();
		cmbReportType.addItem("A4 Size Report");
		cmbReportType.addItem("Excel Report");
		cmbReportType.addItem("Text File-40 Column Report");

		panelExtraComp.setVisible(true);

		lblUserName.setVisible(true);
		lblUserName.setText("Complimentary Qty.      :   ");
		cmbUserName.setVisible(true);
		cmbUserName.removeAllItems();
		cmbUserName.addItem("Yes");
		cmbUserName.addItem("No");

	    }

	    if ((salesReportName.equals("Menu Head Wise")))
	    {
		cmbReportType.removeAllItems();
		cmbReportType.addItem("A4 Size Report");
		cmbReportType.addItem("Excel Report");
		cmbReportType.addItem("Text File-40 Column Report");
	    }

	    if ((salesReportName.equals("Table Wise Pax Report")))
	    {
		cmbReportType.removeAllItems();
		cmbReportType.addItem("A4 Size Report");
		cmbReportType.addItem("Excel Report");
		cmbReportType.addItem("Text File-40 Column Report");
	    }

	    if ((salesReportName.equals("Posting Report")))
	    {
		cmbReportType.removeAllItems();
		cmbReportType.addItem("A4 Size Report");
		cmbReportType.addItem("Excel Report");
		cmbReportType.addItem("Text File-40 Column Report");
	    }
	    if ((salesReportName.equals("Item Wise Consumption")))
	    {
		cmbReportType.removeAllItems();
		cmbReportType.addItem("A4 Size Report");
		cmbReportType.addItem("Excel Report");
		cmbReportType.addItem("Text File-40 Column Report");

		panelExtraComp.setVisible(true);
		//For Group Name
		lblUserName.setVisible(true);
		lblUserName.setText("Group Name              :   ");
		cmbUserName.setVisible(true);
		cmbUserName.removeAllItems();

		lblSettlementMode.setVisible(true);
		lblSettlementMode.setText("Print Modifiers              :   ");
		cmbSettlementMode.setVisible(true);
		cmbSettlementMode.removeAllItems();
		cmbSettlementMode.addItem("Yes");
		cmbSettlementMode.addItem("No");

		lblPaymentMode.setVisible(true);
		lblPaymentMode.setText("Cost Center Name         :   ");
		cmbPaymentMode.setVisible(true);
		cmbSettlementMode.removeAllItems();
		cmbSettlementMode.addItem("Yes");
		cmbSettlementMode.addItem("No");

		sb.setLength(0);
		sb.append("select strGroupName,strGroupCode from tblgrouphd order by strGroupName asc");
		ResultSet rsSubGroup = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
		cmbUserName.addItem("All                                         All");
		while (rsSubGroup.next())
		{
		    cmbUserName.addItem(rsSubGroup.getString(1) + "                                         " + rsSubGroup.getString(2));
		}
		rsSubGroup.close();

		sb.setLength(0);
		sb.append("select a.strCostCenterName,a.strCostCenterCode from tblcostcentermaster a order by a.strCostCenterName asc");
		ResultSet rsCostCenter = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
		cmbPaymentMode.addItem("All                                             All");
		while (rsCostCenter.next())
		{
		    cmbPaymentMode.addItem(rsCostCenter.getString(1) + "                                         " + rsCostCenter.getString(2));
		}
		rsCostCenter.close();

		lblResonMaster.setVisible(true);
		lblResonMaster.setText("Report By             :  ");
		cmbResonMaster.setVisible(true);
		cmbResonMaster.addItem("Menu Head");
		cmbResonMaster.addItem("POS Wise Cost Center");
		cmbResonMaster.addItem("Cost Center");

	    }

	    if (salesReportName.equalsIgnoreCase("Waiter Wise Item Wise Incentives Report"))
	    {
		cmbReportType.removeAllItems();
		cmbReportType.addItem("A4 Size Report");
		cmbReportType.addItem("Excel Report");

		panelExtraComp.setVisible(true);
		//For Group Name
		lblUserName.setVisible(true);
		lblUserName.setText("Group Name              :   ");
		cmbUserName.setVisible(true);
		cmbUserName.removeAllItems();
		//For SubGroup Name
		lblSettlementMode.setVisible(true);
		lblSettlementMode.setText("SubGroup Name          :   ");
		cmbSettlementMode.setVisible(true);
		cmbSettlementMode.removeAllItems();
		lblPaymentMode.setVisible(true);
		lblPaymentMode.setText("Select Type          :");
		cmbPaymentMode.setVisible(true);
		cmbPaymentMode.removeAllItems();
		cmbPaymentMode.addItem("Item Wise");
		cmbPaymentMode.addItem("Summary");
		cmbPaymentMode.addItem("Detail");
		sb.setLength(0);
		sb.append("select strGroupName,strGroupCode from tblgrouphd order by strGroupName asc");
		ResultSet rsSubGroup = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
		cmbUserName.addItem("All                                         All");
		while (rsSubGroup.next())
		{
		    cmbUserName.addItem(rsSubGroup.getString(1) + "                                         " + rsSubGroup.getString(2));
		}
		rsSubGroup.close();
		sb.setLength(0);
		sb.append("select strSubGroupName,strSubGroupCode from tblsubgrouphd order by strSubGroupName asc");
		rsSubGroup = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
		cmbSettlementMode.addItem("All                                         All");
		while (rsSubGroup.next())
		{
		    cmbSettlementMode.addItem(rsSubGroup.getString(1) + "                                         " + rsSubGroup.getString(2));
		}
		rsSubGroup.close();

		cmbUserName.addActionListener(new ActionListener()
		{

		    @Override
		    public void actionPerformed(ActionEvent e)
		    {
			try
			{

			    String group = cmbUserName.getSelectedItem().toString();
			    String groupCode = group.substring(group.lastIndexOf(" "), group.length()).trim();

			    sb.setLength(0);
			    sb.append("select strSubGroupName,strSubGroupCode "
				    + "from tblsubgrouphd ");
			    if (!groupCode.equalsIgnoreCase("All"))
			    {
				sb.append("where strGroupCode='" + groupCode + "' ");
			    }

			    sb.append("order by strSubGroupName asc");
			    ResultSet rsSubGroup = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
			    cmbSettlementMode.removeAllItems();
			    cmbSettlementMode.addItem("All                                         All");
			    while (rsSubGroup.next())
			    {
				cmbSettlementMode.addItem(rsSubGroup.getString(1) + "                                         " + rsSubGroup.getString(2));
			    }
			    rsSubGroup.close();

			}
			catch (Exception ex)
			{
			    ex.printStackTrace();
			}
		    }
		});
	    }

	    if ((salesReportName.equals("Settlement Wise Sales")))
	    {
		cmbReportType.removeAllItems();
		cmbReportType.addItem("A4 Size Report");
		cmbReportType.addItem("Excel Report");
		cmbReportType.addItem("Text File");
		cmbReportType.addItem("Text File-40 Column Report");
	    }

	    if ((salesReportName.equals("Blind Settlement Wise Sales")))
	    {
		cmbReportType.removeAllItems();
		cmbReportType.addItem("A4 Size Report");
		cmbReportType.addItem("Excel Report");
		cmbReportType.addItem("Text File");
	    }

	    if ((salesReportName.equals("Open Item Wise Audit Report")))
	    {
		cmbReportType.removeAllItems();
		cmbReportType.addItem("A4 Size Report");

	    }

	    if ((salesReportName.equals("Area Wise Group Wise Sales")))
	    {
		cmbReportType.removeAllItems();
		cmbReportType.addItem("A4 Size Report");

		panelExtraComp.setVisible(true);
		cmbWaiterCode.setVisible(true);
		lblWaiterName.setText("Area                        :     ");
		cmbWaiterCode.removeAllItems();
		lblWaiterName.setVisible(true);
		sb.setLength(0);
		sb.append("select a.strAreaCode,a.strAreaName from tblareamaster a");
		ResultSet rsSubGroup = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
		while (rsSubGroup.next())
		{
		    cmbWaiterCode.addItem(rsSubGroup.getString(2));
		}
		rsSubGroup.close();
		cmbWaiterCode.setSelectedItem("All");
	    }

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

    private void funFillShiftCombo()
    {
	try
	{
	    if (clsGlobalVarClass.gEnableShiftYN)
	    {
		lblShiftNo.setText("Shift                :    ");
		lblShiftNo.setVisible(true);
		cmbShiftNo.setVisible(true);
		String pos = cmbPosCode.getSelectedItem().toString();
		StringBuilder sqlShift = new StringBuilder();
		if (pos.equalsIgnoreCase("All"))
		{
		    sqlShift.append("select max(a.intShiftCode) from tblshiftmaster a group by a.intShiftCode ");
		}
		else
		{
		    sqlShift.append("select a.intShiftCode from tblshiftmaster a where a.strPOSCode='" + objUtility.funGetPOSCodeFromPOSName(pos) + "' ");
		}
		ResultSet rsShifts = clsGlobalVarClass.dbMysql.executeResultSet(sqlShift.toString());
		cmbShiftNo.removeAllItems();
		cmbShiftNo.addItem("All");
		while (rsShifts.next())
		{
		    cmbShiftNo.addItem(rsShifts.getString(1));
		}
		rsShifts.close();
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

    private void funFillOrderMaster()
    {
	try
	{
	    if (cmbUserName.getSelectedItem().equals("Normal"))
	    {
		lblSettlementMode.setVisible(true);
		cmbSettlementMode.removeAllItems();
		lblSettlementMode.setText("Order Name              :   ");
		cmbSettlementMode.setVisible(true);
		cmbSettlementMode.addItem("All                                         All");
		sb.setLength(0);
		sb.append("select a.strOrderCode,a.strOrderDesc from tblordermaster a; ");
		ResultSet rsOrder = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
		while (rsOrder.next())
		{
		    cmbSettlementMode.addItem(rsOrder.getString(2) + "                                         " + rsOrder.getString(1));
		}
		rsOrder.close();
	    }
	    else
	    {
		lblSettlementMode.setVisible(false);
		cmbSettlementMode.setVisible(false);
		lblSettlementMode.setText("");
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funBillWiseJasperReport()
    {

	String reportType = cmbReportType.getSelectedItem().toString();
	HashMap hm = funGetCommonHashMapForJasperReport();

	clsBillWiseSalesReport objBillWiseReport = new clsBillWiseSalesReport();
	objBillWiseReport.funGenerateBillWiseReport(reportType, hm, "No");

    }

    private void funItemWiseJasperReport()
    {

	String reportType = cmbReportType.getSelectedItem().toString();
	HashMap hm = funGetCommonHashMapForJasperReport();
	String printComplimentaryYN = cmbUserName.getSelectedItem().toString();
	hm.put("printComplimentaryYN", printComplimentaryYN);

	clsItemWiseSalesReport objItemWiseReport = new clsItemWiseSalesReport();
	objItemWiseReport.funGenerateItemWiseReport(reportType, hm, "No");
    }

    private void funMenuHeadWiseJasperReport()
    {

	String reportType = cmbReportType.getSelectedItem().toString();
	HashMap hm = funGetCommonHashMapForJasperReport();

	clsMenuHeadWiseSalesReport objMenuHeadWiseReport = new clsMenuHeadWiseSalesReport();
	objMenuHeadWiseReport.funGenerateMenuHeadWiseReport(reportType, hm, "No");
    }

    private int funGenerateDataForReport(String pos)
    {
	try
	{
	    StringBuilder sbSqlLive = new StringBuilder();
	    StringBuilder sbSqlQFile = new StringBuilder();
	    StringBuilder sbSqlFilters = new StringBuilder();
	    fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());

	    toDate = objUtility.funGetFromToDate(dteToDate.getDate());

	    sbSqlLive.setLength(0);
	    sbSqlQFile.setLength(0);
	    sbSqlFilters.setLength(0);

	    sbSqlQFile.append("SELECT  ifnull(d.strMenuCode,'ND'),ifnull(e.strMenuName,'ND'), sum(a.dblQuantity), "
		    + "sum(a.dblAmount)-sum(a.dblDiscountAmt),f.strPosName,'" + clsGlobalVarClass.gUserCode + "',sum(a.dblRate),sum(a.dblAmount) ,sum(a.dblDiscountAmt) "
		    + "FROM tblqbilldtl a "
		    + "left outer join tblqbillhd b on a.strBillNo=b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate)  "
		    + "left outer join tblposmaster f on b.strposcode=f.strposcode "
		    + "left outer join tblmenuitempricingdtl d on a.strItemCode = d.strItemCode "
		    + "and (b.strposcode =d.strposcode or d.strposcode='All') ");
	    if (clsGlobalVarClass.gAreaWisePricing.equals("Y"))
	    {
		sbSqlQFile.append("and b.strAreaCode= d.strAreaCode ");
	    }
	    sbSqlQFile.append("left outer join tblmenuhd e on d.strMenuCode= e.strMenuCode");
	    sbSqlQFile.append(" where date( b.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
		    + " and a.strClientCode=b.strClientCode ");

	    sbSqlLive.append("SELECT ifnull(d.strMenuCode,'ND'),ifnull(e.strMenuName,'ND'), sum(a.dblQuantity), "
		    + " sum(a.dblAmount)-sum(a.dblDiscountAmt),f.strPosName,'" + clsGlobalVarClass.gUserCode + "',sum(a.dblRate) ,sum(a.dblAmount),sum(a.dblDiscountAmt) "
		    + " FROM tblbilldtl a "
		    + " left outer join tblbillhd b on a.strBillNo=b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate)  "
		    + " left outer join tblposmaster f on b.strposcode=f.strposcode "
		    + " left outer join tblmenuitempricingdtl d on a.strItemCode = d.strItemCode "
		    + " and (b.strposcode =d.strposcode or d.strposcode='All')  ");
	    if (clsGlobalVarClass.gAreaWisePricing.equals("Y"))
	    {
		sbSqlLive.append("and b.strAreaCode= d.strAreaCode ");
	    }
	    sbSqlLive.append("left outer join tblmenuhd e on d.strMenuCode= e.strMenuCode");
	    sbSqlLive.append(" where date( b.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
		    + " and a.strClientCode=b.strClientCode ");

	    String sqlModLive = "SELECT  ifnull(d.strMenuCode,'ND'),ifnull(e.strMenuName,'ND'), sum(a.dblQuantity), "
		    + "sum(a.dblAmount)-sum(a.dblDiscAmt),f.strPosName,'" + clsGlobalVarClass.gUserCode + "',sum(a.dblRate),sum(a.dblAmount),sum(a.dblDiscAmt) "
		    + "FROM tblbillmodifierdtl a "
		    + "left outer join tblbillhd b on a.strBillNo=b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate)  "
		    + "left outer join tblposmaster f on b.strposcode=f.strposcode "
		    + "left outer join tblmenuitempricingdtl d on LEFT(a.strItemCode,7)= d.strItemCode "
		    + " and (b.strposcode =d.strposcode or d.strposcode='All')  ";

	    if (clsGlobalVarClass.gAreaWisePricing.equals("Y"))
	    {
		sqlModLive += "and b.strAreaCode= d.strAreaCode ";
	    }
	    sqlModLive += "left outer join tblmenuhd e on d.strMenuCode= e.strMenuCode";
	    sqlModLive += " where date( b.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' and a.dblAmount>0 "
		    + " and a.strClientCode=b.strClientCode ";

	    String sqlModQFile = "SELECT  ifnull(d.strMenuCode,'ND'),ifnull(e.strMenuName,'ND'), sum(a.dblQuantity), "
		    + "sum(a.dblAmount)-sum(a.dblDiscAmt),f.strPosName,'" + clsGlobalVarClass.gUserCode + "',sum(a.dblRate),sum(a.dblAmount),sum(a.dblDiscAmt) "
		    + "FROM tblqbillmodifierdtl a "
		    + "left outer join tblqbillhd b on a.strBillNo=b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate)  "
		    + "left outer join tblposmaster f on b.strposcode=f.strposcode "
		    + "left outer join tblmenuitempricingdtl d on LEFT(a.strItemCode,7)= d.strItemCode "
		    + " and (b.strposcode =d.strposcode or d.strposcode='All')  ";

	    if (clsGlobalVarClass.gAreaWisePricing.equals("Y"))
	    {
		sqlModQFile += "and b.strAreaCode= d.strAreaCode ";
	    }
	    sqlModQFile += "left outer join tblmenuhd e on d.strMenuCode= e.strMenuCode";
	    sqlModQFile += " where date( b.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' and a.dblAmount>0 "
		    + " and a.strClientCode=b.strClientCode ";

	    if (!pos.equals("All"))
	    {
		sbSqlFilters.append(" AND b.strPOSCode = '" + objUtility.funGetPOSCodeFromPOSName(pos) + "' ");
	    }
	    if (clsGlobalVarClass.gEnableShiftYN)
	    {
		if (clsGlobalVarClass.gEnableShiftYN && (!cmbShiftNo.getSelectedItem().toString().equalsIgnoreCase("All")))
		{
		    sbSqlFilters.append(" AND b.intShiftCode = '" + cmbShiftNo.getSelectedItem().toString() + "' ");
		}
	    }
	    sbSqlFilters.append(" Group by b.strPoscode, d.strMenuCode,e.strMenuName");

	    sbSqlLive.append(sbSqlFilters);
	    sbSqlQFile.append(sbSqlFilters);
	    //System.out.println(sbSqlLive);
	    //System.out.println(sbSqlQFile);

	    clsSalesFlashReport obj = new clsSalesFlashReport();
	    obj.funProcessSalesFlashReport(sbSqlLive.toString(), sbSqlQFile.toString(), "MenuHeadWiseSales");

	    sqlModLive = sqlModLive + " " + sbSqlFilters.toString();
	    sqlModQFile = sqlModQFile + " " + sbSqlFilters.toString();
	    //System.out.println(sqlModLive);
	    //System.out.println(sqlModQFile);

	    String sqlInsertLiveBillSales = "insert into tbltempsalesflash "
		    + "(" + sqlModLive + ");";
	    String sqlInsertQFileBillSales = "insert into tbltempsalesflash "
		    + "(" + sqlModQFile + ");";
	    clsGlobalVarClass.dbMysql.execute(sqlInsertLiveBillSales);
	    clsGlobalVarClass.dbMysql.execute(sqlInsertQFileBillSales);
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	finally
	{
	    return 1;
	}
    }

    private void funGroupWiseJasperReport() throws JRException
    {

	String reportType = cmbReportType.getSelectedItem().toString();
	String subGroup = cmbWaiterCode.getSelectedItem().toString();

	HashMap hm = funGetCommonHashMapForJasperReport();
	hm.put("subGroup", subGroup);

	clsGroupWiseReport objGroupWiseReport = new clsGroupWiseReport();
	objGroupWiseReport.funGroupWiseReport(reportType, hm, "No");

    }

    private void funAreaWiseGroupWiseJasperReport() throws JRException
    {

	String reportType = cmbReportType.getSelectedItem().toString();
	String areaName = cmbWaiterCode.getSelectedItem().toString();

	HashMap hm = funGetCommonHashMapForJasperReport();
	hm.put("areaName", areaName);

	clsGroupWiseReport objGroupWiseReport = new clsGroupWiseReport();
	objGroupWiseReport.funAreaWiseGroupWiseSalesReport(reportType, hm, "No");

    }

    private void funSubGroupWiseJasperReport()
    {
	String reportType = cmbReportType.getSelectedItem().toString();

	HashMap hm = funGetCommonHashMapForJasperReport();

	clsSubGroupWiseReport objSubGroupWiseReport = new clsSubGroupWiseReport();
	objSubGroupWiseReport.funSubGroupWiseReport(reportType, hm, "No");
    }

    private void funSettlementWiseJasperReport()
    {

	String reportType = cmbReportType.getSelectedItem().toString();

	HashMap hm = funGetCommonHashMapForJasperReport();

	clsSettlementWiseReport objSettlementWiseReport = new clsSettlementWiseReport();
	objSettlementWiseReport.funSettlementWiseReport(reportType, hm, "No");

    }

    private void funTaxWiseJasperReport() throws Exception
    {
	String reportType = cmbReportType.getSelectedItem().toString();
	HashMap hm = funGetCommonHashMapForJasperReport();
	clsTaxWiseSalesReport objTaxWiseSalesReport = new clsTaxWiseSalesReport();
	objTaxWiseSalesReport.funTaxWiseSalesReport(reportType, hm, "No");
    }

    private void funVoidBillJasperReport()
    {
	String reportType = cmbReportType.getSelectedItem().toString();
	String rptType = cmbReportMode.getSelectedItem().toString();
	String reasonCodeArr[] = cmbResonMaster.getSelectedItem().toString().split("                                         ");
	String reasonName = reasonCodeArr[0].trim();
	String reasonCode = reasonCodeArr[1].trim();

	HashMap hm = funGetCommonHashMapForJasperReport();
	hm.put("rptType", rptType);
	hm.put("reasonName", reasonName);
	hm.put("reasonCode", reasonCode);
	clsVoidBillReport objVoidBillReport = new clsVoidBillReport();
	objVoidBillReport.funVoidBillReport(reportType, hm, "No");
    }

    private void funDiscountJasperReport()
    {
	String reportType = "", rptType = "";
	reportType = cmbReportType.getSelectedItem().toString();
	rptType = cmbUserName.getSelectedItem().toString();

	HashMap hm = funGetCommonHashMapForJasperReport();
	hm.put("rptType", rptType);

	clsDiscountWiseReport objDiscountWiseReport = new clsDiscountWiseReport();
	objDiscountWiseReport.funGenerateDsicountWiseReport(reportType, hm, "No");
    }

    private void funConsolidatedDiscountJasperReport()
    {
	String reportType = "", rptType = "";
	reportType = cmbReportType.getSelectedItem().toString();
	rptType = cmbUserName.getSelectedItem().toString();

	HashMap hm = funGetCommonHashMapForJasperReport();
	hm.put("rptType", rptType);

	clsConsolidatedDiscountReport objConsolidatedDiscountWiseReport = new clsConsolidatedDiscountReport();
	objConsolidatedDiscountWiseReport.funGenerateConsolidatedDsicountWiseReport(reportType, hm, "No");
    }

    private void funCustomerLedgerJasperReport()
    {
	String reportType = "", rptType = "";
	reportType = cmbReportType.getSelectedItem().toString();
	String customerName = cmbUserName.getSelectedItem().toString();
	String customerCode = mapCustomerNameCode.get(customerName);

	HashMap hm = funGetCommonHashMapForJasperReport();
	hm.put("rptType", rptType);

	hm.put("CustomerCode", customerCode);
	hm.put("CustomerName", customerName);

	clsCustomerLedgerReport objCustomerLedgerReport = new clsCustomerLedgerReport();
	objCustomerLedgerReport.funCustomerLedgerReport(reportType, hm, "No");
    }

    private void funOperatorWiseJasperReport() throws Exception
    {

	String reportType = "", rptType = "";
	reportType = cmbReportType.getSelectedItem().toString();
	String userName = cmbUserName.getSelectedItem().toString();
	String sqlQuery = "select strUserCode from tblUserhd where strUserName='" + userName + "' ";
	ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(sqlQuery);

	while (rs.next())
	{
	    userCode = rs.getString(1);
	}
	rs.close();
	if (cmbUserName.getSelectedItem().equals("All"))
	{
	    userCode = "All";
	}

	String sqlsettleCode = "select strSettelmentCode from tblsettelmenthd "
		+ "where strSettelmentDesc='" + cmbPaymentMode.getSelectedItem() + "' ";
	ResultSet rssettleCode = clsGlobalVarClass.dbMysql.executeResultSet(sqlsettleCode);;
	while (rssettleCode.next())
	{
	    settleCode = rssettleCode.getString(1);
	}
	rssettleCode.close();
	if (settleCode.isEmpty())
	{
	    settleCode = "All";
	}
	if (settleCode.equals("All"))
	{
	    settleCode = "All";
	}

	HashMap hm = funGetCommonHashMapForJasperReport();
	hm.put("userCode", userCode);
	hm.put("settleCode", settleCode);

	clsOperatorWiseReport objOperatorWiseReport = new clsOperatorWiseReport();
	objOperatorWiseReport.funOperatorWiseReport(reportType, hm, "No");
    }

    private void funOperatorWise()
    {
	String reportType = "", rptType = "";
	reportType = cmbReportType.getSelectedItem().toString();
	String userName = cmbUserName.getSelectedItem().toString();
	try
	{
	    String sqlQuery = "select strUserCode from tblUserhd where strUserName='" + userName + "' ";
	    ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(sqlQuery);

	    while (rs.next())
	    {
		userCode = rs.getString(1);
	    }
	    rs.close();
	    if (cmbUserName.getSelectedItem().equals("All"))
	    {
		userCode = "All";
	    }

	    String sqlsettleCode = "select strSettelmentCode from tblsettelmenthd "
		    + "where strSettelmentType='" + cmbPaymentMode.getSelectedItem() + "' ";
	    ResultSet rssettleCode = clsGlobalVarClass.dbMysql.executeResultSet(sqlsettleCode);;
	    while (rssettleCode.next())
	    {
		settleCode = rssettleCode.getString(1);
	    }
	    rssettleCode.close();
	    if (settleCode.isEmpty())
	    {
		settleCode = "All";
	    }
	    if (settleCode.equals("All"))
	    {
		settleCode = "All";
	    }

	    HashMap hm = funGetCommonHashMapForJasperReport();
	    hm.put("userCode", userCode);
	    hm.put("settleCode", settleCode);

	    clsOperatorWiseReport objOperatorWiseReport = new clsOperatorWiseReport();
	    objOperatorWiseReport.funOperatorWiseReport(reportType, hm, "No");
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    /**
     * Generate Jasper Report
     */
    private void funGenerateJasperReport()
    {
	try
	{
	    switch (salesReportName)
	    {
		case "Bill Wise Sales":
		    funBillWiseJasperReport();
		    break;

		case "Item Wise Sales":
		    funItemWiseJasperReport();
		    break;

		case "Menu Head Wise":
		    funMenuHeadWiseJasperReport();
		    break;

		case "Complimentary Settlement Report":
		    funComplimentarySettlementReportForJasper();
		    break;

		case "Guest Credit Report":
		    funGuestCreditReportForJasper();
		    break;

		case "Counter Wise Sales Report":
		    funCounterReportForJasper();
		    break;

		case "Discount Report":
		    funDiscountJasperReport();
		    break;

		case "Group Wise Sales":
		    funGroupWiseJasperReport();
		    break;

		case "SubGroup Wise Sales":
		    funSubGroupWiseJasperReport();
		    break;

		case "GroupSubGroup Wise Report":
		    funGroupSubGroupReportForJasper();
		    break;

		case "Non Chargable KOT Report":
		    funNonChargableKOTJasperReport();
		    break;

		case "Operator Wise Sales":
		    funOperatorWiseJasperReport();
		    break;

		case "Order Analysis Report":
		    funOrderAnalysisReportForJasper();
		    break;

		case "Settlement Wise Sales":
		    funSettlementWiseJasperReport();
		    break;

		case "Tax Wise Sales":
		    funTaxWiseJasperReport();
		    break;

		case "Void Bill Report":
		    funVoidBillJasperReport();
		    break;

		case "Tax Breakup Summary Report":
		    funTaxBreakupSummaryReportForJasper();
		    break;

		case "Waiter Wise Item Report":
		    funWaiterWiseItemReportForJasper();
		    break;

		case "Auditor Report":
		    funAuditorsReportForJasper();
		    break;

		case "Waiter Wise Incentives Report":
		    funWaiterWiseIncentivesReportForJasper();
		    break;

		case "Delivery boy Incentives Report":
		    funDeliveryboyIncentivesReportForJasper();
		    break;

		case "Daily Collection Report":
		    funDailyCollectionReportForJasper();
		    break;

		case "Daily Sales Report":
		    funDailySalesReportForJasper();
		    break;

		case "Void KOT Report":
		    funVoidKOTReportForJasper();
		    break;

		case "SubGroupWiseSummaryReport":
		    funSubGroupWiseSummaryReportForJasper();
		    break;

		case "UnusedCardBalanceReport":
		    funUnusedCardBalanceReportForJasper();
		    break;

		case "Revenue Head Wise Item Sales":
		    funRevenueHeadWiseReportForJasper();
		    break;

		case "Item Wise Consumption":
		    funItemWiseConsumptionJasper();
		    break;

		case "Table Wise Pax Report":
		    funTableWisePaxJasperReport();
		    break;

		case "Posting Report":
		    funPostingReportForJasperReport();
		    break;

		case "Placed Order Report":
		    funPlacedOrderJasperReport();
		    break;

		case "Advance Order Report":
		    funCharactoristicWiseAdvanceOrderJasperReport();
		    break;

		case "Void Advance Order Report":
		    funVoidAdvanceOrderJasperReport();
		    break;

		case "Waiter Wise Item Wise Incentives Report":
		    funWaiterWiseItemWiseIncentivesSummaryJasperReport();
		    break;

		case "Item Master Listing Report":
		    funItemMasterListingReport();
		    break;

		case "Delivery Boy Wise Cash Taken":
		    funDeliveryBoyWiseCashTakenReport();
		    break;

		case "Credit Bill Outstanding Report":
		    funCreditBillReport();
		    break;

		case "Blind Settlement Wise Sales":
		    funBlindSettlementWiseJasperReport();
		    break;

		case "Open Item Wise Audit Report":
		    funOpenItemWiseAuditJasperReport();
		    break;

		case "Non Selling Items":
		    funNonSellingItems();
		    break;
		case "Debtors As On":
		    funDebtorsAsOnReport();
		    break;

		case "Payment Receipt Report":
		    funPaymentReceiptReport();
		    break;

		case "Credit Report":
		    funCreditReport();
		    break;
		case "Consolidated Discount Report":
		    funConsolidatedDiscountJasperReport();
		    break;

		case "Customer Ledger":
		    funCustomerLedgerJasperReport();
		    break;

		case "Area Wise Group Wise Sales":
		    funAreaWiseGroupWiseJasperReport();
		    break;

	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    /**
     * Generate Excel Sheet Of Report
     */
    private void funGenerateExcelSheetOfReport()
    {
	try
	{
	    switch (salesReportName)
	    {
		case "Bill Wise Sales":
		    funBillWiseExcelSheetOfReport();
		    break;

		case "Item Wise Sales":
		    funItemWiseExcelSheetOfReport();
		    break;

		case "Menu Head Wise":
		    funMenuWiseExcelSheetOfReport();
		    break;

		case "Complimentary Settlement Report":
		    funComplimentarySettlementExcelSheetOfReport();
		    break;

		case "Guest Credit Report":
		    funGuestCreditExcelSheetOfReport();
		    break;

		case "Counter Wise Sales Report":
		    funCounterReportExcelSheetOfReport();
		    break;

		case "Discount Report":
		    funDiscountExcelSheetOfReport();
		    break;

		case "Group Wise Sales":
		    funGroupWiseExcelSheetOfReport();
		    break;

		case "SubGroup Wise Sales":
		    funSubGroupWiseExcelSheetOfReport();
		    break;

		case "GroupSubGroup Wise Report":
		    funGroupSubGroupWiseExcelSheetOfReport();
		    break;

		case "Non Chargable KOT Report":
		    funNonChargableKOTExcelSheetOfReport();
		    break;

		case "Operator Wise Sales":
		    funOperatorWise();
		    break;

		case "Order Analysis Report":
		    funOrderAnalysisExcelSheetOfReport();
		    break;

		case "Settlement Wise Sales":
		    funSettlementWiseExcelSheetOfReport();
		    break;

		case "Tax Wise Sales":
		    funTaxWiseExcelSheetOfReport();
		    break;

		case "Void Bill Report":
		    funVoidBillExcelSheetOfReport();
		    break;

		case "Tax Breakup Summary Report":
		    funTaxBreakupSummaryExcelSheetOfReport();
		    break;

		case "Waiter Wise Item Report":
		    funWaiterWiseItemExcelSheetOfReport();
		    break;

		case "Auditor Report":
		    funAuditorsReportExcelSheetOfReport();
		    break;

		case "Waiter Wise Incentives Report":
		    funWaiterWiseIncentivesExcelSheetOfReport();
		    break;

		case "Delivery boy Incentives Report":
		    funDeliveryboyIncentivesExcelSheetOfReport();
		    break;

		case "Daily Collection Report":
		    funDailyCollectionExcelSheetOfReport();
		    break;

		case "Daily Sales Report":
		    funDailySalesExcelSheetOfReport();
		    break;

		case "Void KOT Report":
		    funVoidKOTExcelSheetOfReport();
		    break;

		case "SubGroupWiseSummaryReport":
		    funSubGroupWiseSummaryExcelSheetOfReport();
		    break;

		case "UnusedCardBalanceReport":
		    funUnusedCardBalanceExcelSheetOfReport();
		    break;

		case "Revenue Head Wise Item Sales":
		    funRevenueHeadWiseExcelSheetOfReport();
		    break;

		case "Item Wise Consumption":
		    funItemWiseConsumptionExcelReport();
		    break;

		case "Table Wise Pax Report":
		    funTableWisePaxExcelSheetOfReport();
		    break;

		case "Posting Report":
		    funPostingReportForExcelReport();
		    break;

		case "Item Master Listing Report":
		    funItemMasterListingExcelReport();
		    break;

		case "Waiter Wise Item Wise Incentives Report":
		    funWaiterWiseItemWiseIncentivesExcelReport();
		    break;

		case "Blind Settlement Wise Sales":
		    funBlindSettlementWiseExcelSheetOfReport();
		    break;

		case "Credit Bill Outstanding Report":
		    funCreditBillExcelReport();
		    break;

		case "Customer Ledger":
		    funCustomerLedgerJasperReport();
		    break;
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funWaiterWiseItemWiseIncentivesSummaryJasperReport()
    {

	String reportType = cmbReportType.getSelectedItem().toString();
	String type = cmbPaymentMode.getSelectedItem().toString();
	HashMap hm = funGetCommonHashMapForJasperReport();

	String groupCodeArr[] = cmbUserName.getSelectedItem().toString().split("                                         ");
	String subGroupCodeArr[] = cmbSettlementMode.getSelectedItem().toString().split("                                         ");
	String groupCode = groupCodeArr[1].trim();
	String subGroupCode = subGroupCodeArr[1].trim();

	hm.put("groupCode", groupCode);
	hm.put("subGroupCode", subGroupCode);

	clsWaiterWiseItemWiseIncentivesSummaryReport objWaiterWiseItemWiseIncentivesSummaryReport = new clsWaiterWiseItemWiseIncentivesSummaryReport();
	objWaiterWiseItemWiseIncentivesSummaryReport.funWaiterWiseItemWiseIncentivesSummaryReport(reportType, hm, "No", type);
    }

    private void funWaiterWiseIncentivesReportForJasper()
    {
	String reportType = "", rptType = "";
	reportType = cmbReportType.getSelectedItem().toString();
	rptType = cmbSettlementMode.getSelectedItem().toString();

	HashMap hm = funGetCommonHashMapForJasperReport();
	hm.put("rptType", rptType);

	clsWaiterWiseIncentiveSalesReport objComplimentaryBillReport = new clsWaiterWiseIncentiveSalesReport();
	objComplimentaryBillReport.funGenerateWaiterWiseIncentivesWiseReport(reportType, hm, "No");

    }

    private void funWaiterWiseItemReportForJasper()
    {

	String reportType = "";
	reportType = cmbReportType.getSelectedItem().toString();

	String WaiterCode = cmbWaiterCode.getSelectedItem().toString();
	StringBuilder sbWaiter = new StringBuilder(WaiterCode);
	int length = WaiterCode.length();
	int lastIndex = sbWaiter.lastIndexOf(" ");
	String waiterNo = sbWaiter.substring(lastIndex + 1, length).toString();

	HashMap hm = funGetCommonHashMapForJasperReport();
	hm.put("waiterCode", waiterNo);

	clsWaiterWiseItemReport objWaiterWiseItemReport = new clsWaiterWiseItemReport();
	objWaiterWiseItemReport.funWaiterWiseItemReport(reportType, hm, "No");

    }

    /**
     * create Jasper Report For Tax Breakup Summary
     *
     * @throws Exception
     */
    private void funTaxBreakupSummaryReportForJasper()
    {
	String reportType = cmbReportType.getSelectedItem().toString();
	HashMap hm = funGetCommonHashMapForJasperReport();

	clsTaxBreakupSummaryReport objTaxBreakupSummaryReport = new clsTaxBreakupSummaryReport();
	objTaxBreakupSummaryReport.funTaxBreakupSummaryReport(reportType, hm, "No");
    }

    /**
     * create Jasper Report For Auditors Report
     *
     * @throws Exception
     */
    private void funAuditorsReportForJasper()
    {
	try
	{
	    String reportType = cmbReportType.getSelectedItem().toString();
	    HashMap hm = funGetCommonHashMapForJasperReport();
	    clsAuditorsReport objAuditorsReport = new clsAuditorsReport();
	    objAuditorsReport.funAuditorsReport(reportType, hm, "No");

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    /**
     * create Jasper Report For Non KOT Report
     *
     * @throws Exception
     */
    private void funNonChargableKOTJasperReport() throws Exception
    {
	String reportType = "";
	reportType = cmbReportType.getSelectedItem().toString();
	String ResonMaster = cmbResonMaster.getSelectedItem().toString();
	StringBuilder sbreson = new StringBuilder(ResonMaster);
	int length = ResonMaster.length();
	int lastIndex = sbreson.lastIndexOf(" ");
	String reasonCode = sbreson.substring(lastIndex + 1, length).toString();

	HashMap hm = funGetCommonHashMapForJasperReport();
	hm.put("reasonCode", reasonCode);

	clsNonChargableKOTReport objNonChargableKOTReport = new clsNonChargableKOTReport();
	objNonChargableKOTReport.funNonChargableKOTReport(reportType, hm, "No");

    }

    private void funComplimentarySettlementReportForJasper()
    {
	String reportType = "", rptType = "";
	reportType = cmbReportType.getSelectedItem().toString();
	rptType = cmbSettlementMode.getSelectedItem().toString();

	HashMap hm = funGetCommonHashMapForJasperReport();
	hm.put("rptType", rptType);
	String reasonCode = cmbResonMaster.getSelectedItem().toString().split("                                         ")[1];
	hm.put("reasonCode", reasonCode);
	String reasonName = cmbResonMaster.getSelectedItem().toString();
	hm.put("reasonName", reasonName);

	clsComplimentaryBillReport objComplimentaryBillReport = new clsComplimentaryBillReport();
	objComplimentaryBillReport.funGenerateComplimentaryBillReport(reportType, hm, "No");

    }

    /**
     * create Jasper Report For Counter Report
     *
     * @throws Exception
     */
    private void funCounterReportForJasper()
    {
	try
	{

	    if ((dteToDate.getDate().getTime() - dteFromDate.getDate().getTime()) < 0)
	    {
		new frmOkPopUp(this, "Invalid date", "Error", 1).setVisible(true);
		return;
	    }

	    String reportType = "", rptType = "";
	    reportType = cmbReportType.getSelectedItem().toString();
	    rptType = cmbSettlementMode.getSelectedItem().toString();

	    HashMap hm = funGetCommonHashMapForJasperReport();
	    hm.put("rptType", rptType);

	    clsCounterWiseReport objCounterWiseReport = new clsCounterWiseReport();
	    objCounterWiseReport.funCounterWiseReport(reportType, hm, "No");
	}
	catch (Exception e)
	{
	    if (e.getMessage().startsWith("Byte data not found at"))
	    {
		JOptionPane.showMessageDialog(null, "Report Image Not Found!!! Please Check Property Setup Report Image.", "Error Code: RIMG-1", JOptionPane.ERROR_MESSAGE);
	    }
	    e.printStackTrace();
	}
    }

    /**
     * create Jasper Report For Group SubGroub Report
     *
     * @throws Exception
     */
    private void funGroupSubGroupReportForJasper()
    {
	String reportType = cmbReportType.getSelectedItem().toString();

	String groupCodeArr[] = cmbUserName.getSelectedItem().toString().split("                                         ");
	String subGroupCodeArr[] = cmbSettlementMode.getSelectedItem().toString().split("                                         ");
	String groupCode = groupCodeArr[1].trim();
	String subGroupCode = subGroupCodeArr[1].trim();
	String rptType = cmbPaymentMode.getSelectedItem().toString();

	HashMap hm = funGetCommonHashMapForJasperReport();
	hm.put("rptType", rptType);
	hm.put("groupCode", groupCode);
	hm.put("subGroupCode", subGroupCode);

	clsGroupSubGroupWiseReport objGroupSubGroupWiseReport = new clsGroupSubGroupWiseReport();
	objGroupSubGroupWiseReport.funGenerateGroupSubGroupWiseReport(reportType, hm, "No");
    }

    /**
     * create Jasper Report For Order Analysis Report
     *
     * @throws Exception
     */
    private void funOrderAnalysisReportForJasper()
    {

	String reportType = cmbReportType.getSelectedItem().toString();
	HashMap hm = funGetCommonHashMapForJasperReport();

	clsOrderAnalysisReport objOrderAnalysisReport = new clsOrderAnalysisReport();
	objOrderAnalysisReport.funOrderAnalysisReport(reportType, hm, "No");
    }

    private void funDeliveryboyIncentivesReportForJasper()
    {
	try
	{
	    String reportType = cmbReportType.getSelectedItem().toString();
	    String rptType = cmbReportMode.getSelectedItem().toString();

	    String dpName = cmbDeliveryboy.getSelectedItem().toString();
	    String sqlQuery = "select strDPCode from tbldeliverypersonmaster where strDPName='" + dpName + "' ";
	    ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(sqlQuery);
	    while (rs.next())
	    {
		dpCode = rs.getString(1);
	    }
	    rs.close();
	    if (cmbDeliveryboy.getSelectedItem().equals("All"))
	    {
		dpCode = "All";
	    }

	    HashMap hm = funGetCommonHashMapForJasperReport();
	    hm.put("rptType", rptType);
	    hm.put("DPName", dpName);
	    hm.put("DPCode", dpCode);

	    clsDeliveryboyIncentivesReport objDeliveryboyIncentivesReport = new clsDeliveryboyIncentivesReport();
	    objDeliveryboyIncentivesReport.funDeliveryboyIncentivesReport(reportType, hm, "No");
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funDailyCollectionReportForJasper()
    {

	String reportType = cmbReportType.getSelectedItem().toString();

	HashMap hm = funGetCommonHashMapForJasperReport();

	clsDailyCollectionReport objDailyCollectionReport = new clsDailyCollectionReport();
	objDailyCollectionReport.funDailyCollectionReport(reportType, hm, "No");

    }

    private void funDailySalesReportForJasper()
    {
	String reportType = cmbReportType.getSelectedItem().toString();

	HashMap hm = funGetCommonHashMapForJasperReport();

	clsDailySalesReport objDailySalesReport = new clsDailySalesReport();
	objDailySalesReport.funDailyCollectionReport(reportType, hm, "No");

    }

    private void funVoidKOTReportForJasper()
    {
	String reportType = cmbReportType.getSelectedItem().toString();

	HashMap hm = funGetCommonHashMapForJasperReport();

	clsVoidKOTReport objVoidKOTReport = new clsVoidKOTReport();
	objVoidKOTReport.funVoidKOTReport(reportType, hm, "No", cmbUserName.getSelectedItem().toString().trim());

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
     * Generate Text File Report
     *
     * @param reportName
     * @param reportType
     */
    private void funGenerateTextFileReport(String reportName, String reportType)
    {
	try
	{
	    if (reportName.equalsIgnoreCase("Bill Wise Sales"))
	    {
		funBillWiseTextReport(reportType);
	    }
	    else if (reportName.startsWith("Item Wise Sales"))
	    {
		funItemWiseTextReport(reportType);
	    }
	    else if (reportName.startsWith("GroupSubGroup Wise Report"))
	    {
		funGroupSubGroupWiseTextReport(reportType);
	    }
	    else if (reportName.startsWith("Non Chargable KOT Report"))
	    {
		funNonChargableKOTTextReport(reportType);
	    }
	    else if (reportName.startsWith("Complimentary Settlement Report"))
	    {
		funComplimentarySettlementTextReport(reportType);
	    }
	    else if (reportName.startsWith("Menu Head Wise"))
	    {
		funMenuHeadTextReport(reportType);
	    }
	    else if (reportName.startsWith("Table Wise Pax Report"))
	    {
		funTablePaxTextReport(reportType);
	    }
	    else if (reportName.startsWith("Posting Report"))
	    {
		funPostingTextReport(reportType);
	    }
	    else if (reportName.startsWith("Item Wise Consumption"))
	    {
		funItemWiseConsumptionTextReport(reportType);
	    }
	    else if (reportName.startsWith("Settlement Wise Sales"))
	    {
		if (cmbReportType.getSelectedItem().toString().equalsIgnoreCase("Text File-40 Column Report"))
		{
		    funSettlementWiseTextFourtyColumn();
		}
		else
		{
		    funSettlementWiseCreditTextReport(reportType);
		}

	    }
	    else if (reportName.startsWith("Blind Settlement Wise Sales"))
	    {
		funBlindSettlementWiseCreditTextReport(reportType);
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    /**
     * Print Text With Left Alignment
     *
     * @param align
     * @param textToPrint
     * @param totalLength
     * @param pw
     * @return
     */
    private int funPrintTextWithLeftAlignment(String align, String textToPrint, int totalLength, PrintWriter pw)
    {

	pw.print(String.format("%-18s", textToPrint));
	return 1;
    }

    /**
     * method Non Chargable KOT Report
     *
     * @param reportType
     * @throws Exception
     */
    public void funNonChargableKOTTextReport(String reportType) throws Exception
    {

	fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());
	toDate = objUtility.funGetFromToDate(dteToDate.getDate());
	String pos = cmbPosCode.getSelectedItem().toString();
	objUtility.funCreateTempFolder();
	String filePath = System.getProperty("user.dir");
	File file = new File(filePath + File.separator + "Temp" + File.separator + "Temp_NonChargableKOT.txt");

	int count = funGenerateNonChargableKOTTextReport(file, pos, fromDate, toDate);
	if (count > 0)
	{
	    funShowTextFile(file, "Text Sales Report");
	}
	else
	{
	    JOptionPane.showMessageDialog(this, "No Records Found");
	}
    }

    /**
     * Complimentary Settlement Text Report
     *
     * @param reportType
     * @throws Exception
     */
    public void funComplimentarySettlementTextReport(String reportType) throws Exception
    {
	String fromDate = (dteFromDate.getDate().getYear() + 1900) + "-" + (dteFromDate.getDate().getMonth() + 1) + "-" + (dteFromDate.getDate().getDate());
	String toDate = (dteToDate.getDate().getYear() + 1900) + "-" + (dteToDate.getDate().getMonth() + 1) + "-" + (dteToDate.getDate().getDate());
	String pos = cmbPosCode.getSelectedItem().toString();
	objUtility.funCreateTempFolder();
	String filePath = System.getProperty("user.dir");
	File file = new File(filePath + File.separator + "Temp" + File.separator + "Temp_ComplimentarySettlementTextReport.txt");

	int count = funGenerateComplimentarySettlementTextReport(file, pos, fromDate, toDate);
	if (count > 0)
	{
	    funShowTextFile(file, "Text Sales Report");
	}
	else
	{
	    JOptionPane.showMessageDialog(this, "No Records Found");
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

	DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();
	pw.print(gDecimalFormat.format(Double.parseDouble(textToPrint)));
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
     * Get Date
     *
     * @param format
     * @param date
     * @return
     */
    private String funGetDate(String format, String date)
    {
	String retDate = "";
	if (format.equalsIgnoreCase("dd-MM-yyyy"))
	{
	    String[] split = date.split("-");
	    retDate = split[2] + "-" + split[1] + "-" + split[0];
	}
	return retDate;
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
     * Bill Wise Text Report
     *
     * @param reportType
     * @throws Exception
     */
    public void funBillWiseTextReport(String reportType) throws Exception
    {
	String fromDate = (dteFromDate.getDate().getYear() + 1900) + "-" + (dteFromDate.getDate().getMonth() + 1) + "-" + (dteFromDate.getDate().getDate());
	String toDate = (dteToDate.getDate().getYear() + 1900) + "-" + (dteToDate.getDate().getMonth() + 1) + "-" + (dteToDate.getDate().getDate());

	String posName = cmbPosCode.getSelectedItem().toString();
	String posCode = objUtility.funGetPOSCodeFromPOSName(posName);

	objUtility.funCreateTempFolder();
	String filePath = System.getProperty("user.dir");
	File file = new File(filePath + File.separator + "Temp" + File.separator + "Temp_BillReport.txt");

	int count = funGenerateBillWiseTextReport(file, posCode, fromDate, toDate);
	if (count > 0)
	{
	    funShowTextFile(file, "Text Sales Report");
	}
	else
	{
	    JOptionPane.showMessageDialog(this, "No Records Found");
	}
    }

    /**
     * Item Wise Text Report
     *
     * @param reportType
     * @throws Exception
     */
    public void funItemWiseTextReport(String reportType) throws Exception
    {
	String fromDate = (dteFromDate.getDate().getYear() + 1900) + "-" + (dteFromDate.getDate().getMonth() + 1) + "-" + (dteFromDate.getDate().getDate());
	String toDate = (dteToDate.getDate().getYear() + 1900) + "-" + (dteToDate.getDate().getMonth() + 1) + "-" + (dteToDate.getDate().getDate());
	String pos = cmbPosCode.getSelectedItem().toString();
	objUtility.funCreateTempFolder();
	String filePath = System.getProperty("user.dir");
	File file = new File(filePath + File.separator + "Temp" + File.separator + "Temp_ItemWiseReport.txt");

	int count = funGenerateItemWiseTextReport(file, pos, fromDate, toDate);
	if (count > 0)
	{
	    funShowTextFile(file, "Text Sales Report");
	}
	else
	{
	    JOptionPane.showMessageDialog(this, "No Records Found");
	}
    }

    /**
     * Group, subGroup Wise Text Report
     *
     * @param reportType
     * @throws Exception
     */
    public void funGroupSubGroupWiseTextReport(String reportType) throws Exception
    {

	String groupCodeArr[] = cmbUserName.getSelectedItem().toString().split("                                         ");
	String subGroupCodeArr[] = cmbSettlementMode.getSelectedItem().toString().split("                                         ");
	String groupCode = groupCodeArr[1].trim();
	String subGroupCode = subGroupCodeArr[1].trim();
	String posName = cmbPosCode.getSelectedItem().toString();
	String rptType = cmbPaymentMode.getSelectedItem().toString();

	HashMap hm = funGetCommonHashMapForJasperReport();
	hm.put("rptType", rptType);
	hm.put("posName", posName);
	hm.put("groupCode", groupCode);
	hm.put("subGroupCode", subGroupCode);

	clsGroupSubGroupWiseReport objGroupSubGroupWiseReport = new clsGroupSubGroupWiseReport();
	objGroupSubGroupWiseReport.funGenerateGroupSubGroupWiseReport(reportType, hm, "No");

    }

    public int funGenerateGroupSubGroupWiseTextReport(File file, String pos, String fromDate1, String toDate1) throws Exception
    {
	int count = 0;
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
	funPrintBlankLines("Group Sub-GroupWise Report", pw);

	pw.println();
	pw.println("POS  :" + cmbPosCode.getSelectedItem().toString());
	pw.println("From :" + funGetCalenderDate("From") + "  To :" + funGetCalenderDate("To"));
	pw.println("---------------------------------------");
	pw.println("ITEM NAME                            ");
	pw.println("       Rate         Qty        Amount ");
	pw.println("--------------------------------------");

	String groupCodeArr[] = cmbUserName.getSelectedItem().toString().split("                                         ");
	String subGroupCodeArr[] = cmbSettlementMode.getSelectedItem().toString().split("                                         ");
	String groupCode = groupCodeArr[1].trim();
	String subGroupCode = subGroupCodeArr[1].trim();

	String posCode = objUtility.funGetPOSCodeFromPOSName(pos);

	int cnt = 0, cnt1 = 0;
	String GroupName = "";
	String subGroupName = "";
	double grandTotal = 0.00, quantityTotal = 0.00, GroupTotal = 0.00, subGroupTotal = 0.00;

	String sql = "select b.strItemName,c.strSubGroupName,d.strGroupName , a.dblRate "
		+ ", sum(a.dblQuantity),sum(a.dblAmount)-sum(a.dblDiscountAmt) "
		+ " from vqbillhddtl a,tblitemmaster b,tblsubgrouphd c,tblgrouphd d "
		+ "where a.strItemCode=b.strItemCode "
		+ " and b.strSubGroupCode=c.strSubGroupCode "
		+ "and c.strGroupCode=d.strGroupCode "
		+ "and date(a.dteBillDate) between '" + fromDate1 + "' and '" + toDate1 + "' "
		+ "and a.strPosCode=if('" + posCode + "'='All',a.strPosCode,'" + posCode + "') ";
	if (!groupCode.equalsIgnoreCase("All"))
	{
	    sql = sql + "and d.strGroupCode='" + groupCode + "' ";
	}
	if (!subGroupCode.equalsIgnoreCase("All"))
	{
	    sql = sql + "and c.strSubGroupCode='" + subGroupCode + "' ";
	}
	sql = sql + "Group By d.strGroupName ,c.strSubGroupName,b.strItemName "
		+ "order by d.strGroupName,c.strSubGroupName ";
	//System.out.println(sql);
	ResultSet rsGroupSubGroupWise = clsGlobalVarClass.dbMysql.executeResultSet(sql);

	while (rsGroupSubGroupWise.next())
	{
	    count++;
	    if (!GroupName.equals(rsGroupSubGroupWise.getString(3)))
	    {
		if (cnt > 0)
		{
		    pw.println("---------------------------------------");
		    funPrintTextValueWithAlignment("left", String.valueOf(GroupName + " TOTAL"), 27, pw);
		    funPrintTextWithAlignment("right", String.valueOf(GroupTotal), 10, pw);
		    pw.println();
		    pw.println("---------------------------------------");
		    pw.println();
		}
		pw.println();
		pw.println("GROUP :" + rsGroupSubGroupWise.getString(3));
		funDrawUnderLine("GROUP :" + rsGroupSubGroupWise.getString(3), pw);
		GroupName = rsGroupSubGroupWise.getString(3);
		GroupTotal = 0.00;
	    }
	    if (!subGroupName.equals(rsGroupSubGroupWise.getString(2)))
	    {
		if (cnt1 > 0)
		{
		    pw.println("---------------------------------------");
		    funPrintTextValueWithAlignment("left", String.valueOf(subGroupName + " TOTAL"), 27, pw);
		    funPrintTextWithAlignment("right", String.valueOf(subGroupTotal), 10, pw);
		    pw.println();
		    pw.println("---------------------------------------");
		    pw.println();
		}

		pw.println();
		pw.println(rsGroupSubGroupWise.getString(2));
		funDrawUnderLine(rsGroupSubGroupWise.getString(2), pw);
		subGroupName = rsGroupSubGroupWise.getString(2);
		subGroupTotal = 0.00;
	    }

	    pw.println(rsGroupSubGroupWise.getString(1));
	    funPrintTextWithAlignment("right", rsGroupSubGroupWise.getString(4), 12, pw);
	    funPrintTextWithAlignment("right", rsGroupSubGroupWise.getString(5), 12, pw);
	    funPrintTextWithAlignment("right", rsGroupSubGroupWise.getString(6), 13, pw);
	    pw.println();

	    quantityTotal += Double.parseDouble(rsGroupSubGroupWise.getString(5));
	    grandTotal += Double.parseDouble(rsGroupSubGroupWise.getString(6));
	    GroupTotal += Double.parseDouble(rsGroupSubGroupWise.getString(6));
	    subGroupTotal += Double.parseDouble(rsGroupSubGroupWise.getString(6));
	    cnt1++;
	    if (rsGroupSubGroupWise.isLast())
	    {
		pw.println("---------------------------------------");
		pw.print(subGroupName + " TOTAL");
		funPrintTextWithAlignment("right", String.valueOf(subGroupTotal), 24, pw);
		pw.println();

	    }

	    cnt++;
	    if (rsGroupSubGroupWise.isLast())
	    {
		pw.println("---------------------------------------");
		pw.print(GroupName + " TOTAL");
		funPrintTextWithAlignment("right", String.valueOf(GroupTotal), 15, pw);
		pw.println();
	    }
	}
	rsGroupSubGroupWise.close();

	pw.println("---------------------------------------");
	pw.print("GRAND TOTAL");
	funPrintTextWithAlignment("right", String.valueOf(Math.rint(quantityTotal)), 13, pw);
	funPrintTextWithAlignment("right", String.valueOf(Math.rint(grandTotal)), 12, pw);
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

	return count;
    }

    public void funMenuHeadTextReport(String reportType) throws Exception
    {

	String fromDate = (dteFromDate.getDate().getYear() + 1900) + "-" + (dteFromDate.getDate().getMonth() + 1) + "-" + (dteFromDate.getDate().getDate());
	String toDate = (dteToDate.getDate().getYear() + 1900) + "-" + (dteToDate.getDate().getMonth() + 1) + "-" + (dteToDate.getDate().getDate());
	String pos = cmbPosCode.getSelectedItem().toString();
	objUtility.funCreateTempFolder();
	String filePath = System.getProperty("user.dir");
	File file = new File(filePath + File.separator + "Temp" + File.separator + "Temp_MenuHeadReport.txt");

	int count = funGenerateMenuHeadTextReport(file, pos, fromDate, toDate);

	if (count > 0)
	{
	    funShowTextFile(file, "Text Sales Report");
	}
	else
	{
	    JOptionPane.showMessageDialog(this, "No Records Found");
	}
    }

    public void funTablePaxTextReport(String reportType) throws Exception
    {

	fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());
	toDate = objUtility.funGetFromToDate(dteToDate.getDate());
	String pos = cmbPosCode.getSelectedItem().toString();
	objUtility.funCreateTempFolder();
	String filePath = System.getProperty("user.dir");
	File file = new File(filePath + File.separator + "Temp" + File.separator + "Temp_TableWisePaxReport.txt");

	int count = funGenerateTableWisePaxTextReport(file, pos, fromDate, toDate);

	if (count > 0)
	{
	    funShowTextFile(file, "Text Sales Report");
	}
	else
	{
	    JOptionPane.showMessageDialog(this, "No Records Found");
	}
    }

    public void funPostingTextReport(String reportType) throws Exception
    {
	fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());
	toDate = objUtility.funGetFromToDate(dteToDate.getDate());
	String pos = cmbPosCode.getSelectedItem().toString();
	objUtility.funCreateTempFolder();
	String filePath = System.getProperty("user.dir");
	File file = new File(filePath + File.separator + "Temp" + File.separator + "Temp_PostingReport.txt");

	int count = funGeneratePostingReportForText(file, pos, fromDate, toDate);
	if (count > 0)
	{
	    funShowTextFile(file, "Text Posting Report");
	}
	else
	{
	    JOptionPane.showMessageDialog(this, "No Records Found");
	}
    }

    public int funGeneratePostingReportForText(File file, String pos, String fromDate, String toDate)
    {
	int count = 0;
	try
	{
	    ////For Settlement details of Live and Q data
	    StringBuilder sqlQData = new StringBuilder();
	    StringBuilder sqlModQData = new StringBuilder();

	    sqlQData.append(" select c.strSettelmentDesc,c.strSettelmentType,sum(b.dblSettlementAmt)+sum(a.dblTipAmount) "
		    + " from tblqbillhd a,tblqbillsettlementdtl b,tblsettelmenthd c "
		    + " where a.strBillNo=b.strBillNo "
		    + " and date(a.dteBillDate)=date(b.dteBillDate) "
		    + " and b.strSettlementCode=c.strSettelmentCode "
		    + " and c.strSettelmentType!='Complementary' and c.strSettelmentType!='Credit' "
		    + " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    if (!cmbPosCode.getSelectedItem().toString().equals("All"))
	    {
		sqlQData.append(" and a.strPOSCode='" + objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString()) + "' ");
	    }
	    sqlQData.append(" group by c.strSettelmentCode ");

	    Map<String, List<clsSettelementOptions>> hmSalesSettleData = new HashMap<String, List<clsSettelementOptions>>();
	    List<clsSettelementOptions> arrListSettleData = null;

	    ResultSet rsSettlementWiseQData = clsGlobalVarClass.dbMysql.executeResultSet(sqlQData.toString());
	    while (rsSettlementWiseQData.next())
	    {
		clsSettelementOptions objSettle = new clsSettelementOptions();
		if (hmSalesSettleData.containsKey(rsSettlementWiseQData.getString(2)))
		{
		    arrListSettleData = hmSalesSettleData.get(rsSettlementWiseQData.getString(2));
		    for (int j = 0; j < arrListSettleData.size(); j++)
		    {
			objSettle = arrListSettleData.get(j);
			if (objSettle.getStrSettelmentDesc().equals(rsSettlementWiseQData.getString(1)))
			{
			    arrListSettleData.remove(objSettle);
			    double settleAmt = objSettle.getDblSettlementAmt();
			    objSettle.setDblSettlementAmt(settleAmt + rsSettlementWiseQData.getDouble(3));
			}
			else
			{
			    objSettle = new clsSettelementOptions();
			    objSettle.setDblSettlementAmt(rsSettlementWiseQData.getDouble(3));
			    objSettle.setStrSettelmentDesc(rsSettlementWiseQData.getString(1));
			    objSettle.setStrSettelmentType(rsSettlementWiseQData.getString(2));
			}
		    }
		}
		else
		{
		    arrListSettleData = new ArrayList<clsSettelementOptions>();
		    objSettle.setStrSettelmentDesc(rsSettlementWiseQData.getString(1));
		    objSettle.setStrSettelmentType(rsSettlementWiseQData.getString(2));
		    objSettle.setDblSettlementAmt(rsSettlementWiseQData.getDouble(3));
		}
		arrListSettleData.add(objSettle);
		hmSalesSettleData.put(rsSettlementWiseQData.getString(2), arrListSettleData);
	    }
	    rsSettlementWiseQData.close();

	    //Map<String, Map<String,Double>> hmCreditSettleData = new HashMap<String, Map<String,Double>>();
	    //Map<String, Double> hmCreditCustData = new HashMap<String, Double>();
	    //Map<String, List<clsSettelementOptions>> hmCreditSettleData = new HashMap<String, List<clsSettelementOptions>>();
	    List<clsSettelementOptions> arrListCreditSettleData = null;

	    sqlQData.setLength(0);
	    sqlQData.append(" select ifnull(d.strCustomerName,'NA'),c.strSettelmentType,sum(b.dblSettlementAmt)+sum(a.dblTipAmount) "
		    + " from tblqbillhd a "
		    + " inner join tblqbillsettlementdtl b on a.strBillNo=b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate) "
		    + " left outer join tblcustomermaster d on b.strCustomerCode=d.strCustomerCode "
		    + " inner join tblsettelmenthd c on b.strSettlementCode=c.strSettelmentCode "
		    + " where c.strSettelmentType='Credit' "
		    + " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    if (!cmbPosCode.getSelectedItem().toString().equals("All"))
	    {
		sqlQData.append(" and a.strPOSCode='" + objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString()) + "' ");
	    }
	    sqlQData.append(" group by d.strCustomerCode order by d.strCustomerName ");
	    String key = "Credit";
	    rsSettlementWiseQData = clsGlobalVarClass.dbMysql.executeResultSet(sqlQData.toString());
	    while (rsSettlementWiseQData.next())
	    {
		clsSettelementOptions objSettle = new clsSettelementOptions();
		if (hmSalesSettleData.containsKey(key))
		{
		    arrListCreditSettleData = hmSalesSettleData.get(key);
		    for (int j = 0; j < arrListCreditSettleData.size(); j++)
		    {
			objSettle = arrListCreditSettleData.get(j);
			if (objSettle.getStrSettelmentDesc().equals(rsSettlementWiseQData.getString(1)))
			{
			    arrListCreditSettleData.remove(objSettle);
			    double settleAmt = objSettle.getDblSettlementAmt();
			    objSettle.setDblSettlementAmt(settleAmt + rsSettlementWiseQData.getDouble(3));
			}
			else
			{
			    objSettle = new clsSettelementOptions();
			    objSettle.setDblSettlementAmt(rsSettlementWiseQData.getDouble(3));
			    objSettle.setStrSettelmentDesc(rsSettlementWiseQData.getString(1));
			    objSettle.setStrSettelmentType(rsSettlementWiseQData.getString(2));
			}
		    }
		}
		else
		{
		    arrListCreditSettleData = new ArrayList<clsSettelementOptions>();
		    objSettle.setStrSettelmentDesc(rsSettlementWiseQData.getString(1));
		    objSettle.setStrSettelmentType(rsSettlementWiseQData.getString(2));
		    objSettle.setDblSettlementAmt(rsSettlementWiseQData.getDouble(3));
		}
		arrListCreditSettleData.add(objSettle);
		hmSalesSettleData.put(key, arrListCreditSettleData);
	    }
	    rsSettlementWiseQData.close();

	    //For Discount details of Live and Q data
	    //   sqlLiveData.setLength(0);
	    sqlQData.setLength(0);
	    sqlQData.append(" select sum(b.dblDiscAmt),a.strBillNo  from tblqbillhd a,tblqbilldiscdtl b "
		    + " where a.strBillNo=b.strBillNo "
		    + " and date(a.dteBillDate)=date(b.dteBillDate) "
		    + " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    if (!cmbPosCode.getSelectedItem().toString().equals("All"))
	    {
		sqlQData.append(" and a.strPOSCode='" + objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString()) + "' ");
	    }
	    sqlQData.append(" group by a.strBillNo");

	    double finalDisAmt = 0;

	    ResultSet rsDisQData = clsGlobalVarClass.dbMysql.executeResultSet(sqlQData.toString());
	    while (rsDisQData.next())
	    {
		finalDisAmt = finalDisAmt + rsDisQData.getDouble(1);
	    }
	    rsDisQData.close();

	    sqlQData.setLength(0);
	    sqlModQData.setLength(0);

	    sqlQData.append(" select e.strGroupName,sum(b.dblAmount)-sum(b.dblDiscountAmt),e.strGroupCode "
		    + " from tblqbillhd a,tblqbilldtl b,tblitemmaster c,tblsubgrouphd d,tblgrouphd e "
		    + " where a.strBillNo=b.strBillNo "
		    + " and date(a.dteBillDate)=date(b.dteBillDate) "
		    + " and b.strItemCode=c.strItemCode "
		    + " and c.strSubGroupCode=d.strSubGroupCode and d.strGroupCode=e.strGroupCode "
		    + " and a.strClientCode=b.strClientCode  "
		    + " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");

	    sqlModQData.append(" select e.strGroupName,sum(b.dblAmount)-sum(b.dblDiscAmt),e.strGroupCode "
		    + " from tblqbillhd a,tblqbillmodifierdtl b,tblitemmaster c,tblsubgrouphd d,tblgrouphd e "
		    + " where a.strBillNo=b.strBillNo "
		    + " and date(a.dteBillDate)=date(b.dteBillDate) "
		    + " and left(b.strItemCode,7)=c.strItemCode "
		    + " and c.strSubGroupCode=d.strSubGroupCode and d.strGroupCode=e.strGroupCode "
		    + "and a.strClientCode=b.strClientCode "
		    + " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");

	    if (!cmbPosCode.getSelectedItem().toString().equals("All"))
	    {
		sqlQData.append(" and a.strPOSCode='" + objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString()) + "' ");
		sqlModQData.append(" and a.strPOSCode='" + objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString()) + "' ");
	    }
	    sqlQData.append(" group by e.strGroupCode");
	    sqlModQData.append(" group by e.strGroupCode");

	    Map<String, List<clsSettelementOptions>> hmSalesGroupWiseSaleData = new HashMap<String, List<clsSettelementOptions>>();
	    List<clsSettelementOptions> arrListGroupwiseSaleData = null;

	    ResultSet rsGroupwiseSaleQData = clsGlobalVarClass.dbMysql.executeResultSet(sqlQData.toString());
	    while (rsGroupwiseSaleQData.next())
	    {
		clsSettelementOptions objSettle = new clsSettelementOptions();
		if (hmSalesGroupWiseSaleData.containsKey(rsGroupwiseSaleQData.getString(3)))
		{
		    arrListGroupwiseSaleData = hmSalesGroupWiseSaleData.get(rsGroupwiseSaleQData.getString(3));
		    for (int j = 0; j < arrListGroupwiseSaleData.size(); j++)
		    {
			objSettle = arrListGroupwiseSaleData.get(j);
			if (objSettle.getStrSettelmentDesc().equals(rsGroupwiseSaleQData.getString(1)))
			{
			    arrListGroupwiseSaleData.remove(objSettle);
			    double settleAmt = objSettle.getDblSettlementAmt();
			    objSettle.setDblSettlementAmt(settleAmt + rsGroupwiseSaleQData.getDouble(2));
			}
			else
			{
			    objSettle = new clsSettelementOptions();
			    objSettle.setDblSettlementAmt(rsGroupwiseSaleQData.getDouble(2));
			    objSettle.setStrSettelmentDesc(rsGroupwiseSaleQData.getString(1));
			    objSettle.setStrSettelmentType(rsGroupwiseSaleQData.getString(3));
			}
		    }
		}
		else
		{
		    arrListGroupwiseSaleData = new ArrayList<clsSettelementOptions>();
		    objSettle.setStrSettelmentDesc(rsGroupwiseSaleQData.getString(1));
		    objSettle.setStrSettelmentType(rsGroupwiseSaleQData.getString(3));
		    objSettle.setDblSettlementAmt(rsGroupwiseSaleQData.getDouble(2));

		}
		arrListGroupwiseSaleData.add(objSettle);
		hmSalesGroupWiseSaleData.put(rsGroupwiseSaleQData.getString(3), arrListGroupwiseSaleData);
	    }
	    rsGroupwiseSaleQData.close();

	    ResultSet rsGroupwiseSaleModQData = clsGlobalVarClass.dbMysql.executeResultSet(sqlModQData.toString());
	    while (rsGroupwiseSaleModQData.next())
	    {
		clsSettelementOptions objSettle = new clsSettelementOptions();
		if (hmSalesGroupWiseSaleData.containsKey(rsGroupwiseSaleModQData.getString(3)))
		{
		    arrListGroupwiseSaleData = hmSalesGroupWiseSaleData.get(rsGroupwiseSaleModQData.getString(3));
		    for (int j = 0; j < arrListGroupwiseSaleData.size(); j++)
		    {
			objSettle = arrListGroupwiseSaleData.get(j);
			if (objSettle.getStrSettelmentDesc().equals(rsGroupwiseSaleModQData.getString(1)))
			{
			    arrListGroupwiseSaleData.remove(objSettle);
			    double settleAmt = objSettle.getDblSettlementAmt();
			    objSettle.setDblSettlementAmt(settleAmt + rsGroupwiseSaleModQData.getDouble(2));
			}
			else
			{
			    objSettle = new clsSettelementOptions();
			    objSettle.setDblSettlementAmt(rsGroupwiseSaleModQData.getDouble(2));
			    objSettle.setStrSettelmentDesc(rsGroupwiseSaleModQData.getString(1));
			    objSettle.setStrSettelmentType(rsGroupwiseSaleModQData.getString(3));
			}
		    }
		}
		else
		{
		    arrListGroupwiseSaleData = new ArrayList<clsSettelementOptions>();
		    objSettle.setStrSettelmentDesc(rsGroupwiseSaleModQData.getString(1));
		    objSettle.setStrSettelmentType(rsGroupwiseSaleModQData.getString(3));
		    objSettle.setDblSettlementAmt(rsGroupwiseSaleModQData.getDouble(2));

		}
		arrListGroupwiseSaleData.add(objSettle);
		hmSalesGroupWiseSaleData.put(rsGroupwiseSaleModQData.getString(3), arrListGroupwiseSaleData);
	    }
	    rsGroupwiseSaleModQData.close();

	    sqlQData.setLength(0);

	    sqlQData.append(" select c.strTaxCode,c.strTaxDesc,sum(b.dblTaxAmount),sum(b.dblTaxableAmount) "
		    + " from tblqbillhd a,tblqbilltaxdtl b,tbltaxhd c "
		    + " where a.strBillNo=b.strBillNo "
		    + " and date(a.dteBillDate)=date(b.dteBillDate) "
		    + " and b.strTaxCode=c.strTaxCode "
		    + " and a.strClientCode=b.strClientCode "
		    + " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    if (!cmbPosCode.getSelectedItem().toString().equals("All"))
	    {
		//      sqlLiveData.append(" and a.strPOSCode='" + objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString()) + "' ");
		sqlQData.append(" and a.strPOSCode='" + objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString()) + "' ");
	    }

	    sqlQData.append(" group by b.strTaxCode order by c.strTaxOnTax");

	    Map<String, List<clsSettelementOptions>> hmSalesTaxWiseSaleData = new HashMap<String, List<clsSettelementOptions>>();
	    List<clsSettelementOptions> arrListTaxwiseSaleData = null;

	    ResultSet rsTaxwiseSaleQData = clsGlobalVarClass.dbMysql.executeResultSet(sqlQData.toString());
	    while (rsTaxwiseSaleQData.next())
	    {
		clsSettelementOptions objSettle = new clsSettelementOptions();
		if (hmSalesTaxWiseSaleData.containsKey(rsTaxwiseSaleQData.getString(1)))
		{
		    arrListTaxwiseSaleData = hmSalesTaxWiseSaleData.get(rsTaxwiseSaleQData.getString(1));
		    for (int j = 0; j < arrListTaxwiseSaleData.size(); j++)
		    {
			objSettle = arrListTaxwiseSaleData.get(j);
			if (objSettle.getStrSettelmentDesc().equals(rsTaxwiseSaleQData.getString(2)))
			{
			    arrListTaxwiseSaleData.remove(objSettle);
			    double settleAmt = objSettle.getDblSettlementAmt();
			    objSettle.setDblSettlementAmt(settleAmt + rsTaxwiseSaleQData.getDouble(3));
			}
			else
			{
			    objSettle = new clsSettelementOptions();
			    objSettle.setDblSettlementAmt(rsTaxwiseSaleQData.getDouble(3));
			    objSettle.setStrSettelmentDesc(rsTaxwiseSaleQData.getString(2));
			    objSettle.setStrSettelmentType(rsTaxwiseSaleQData.getString(1));
			}
		    }
		}
		else
		{
		    arrListTaxwiseSaleData = new ArrayList<clsSettelementOptions>();
		    objSettle.setStrSettelmentDesc(rsTaxwiseSaleQData.getString(2));
		    objSettle.setStrSettelmentType(rsTaxwiseSaleQData.getString(1));
		    objSettle.setDblSettlementAmt(rsTaxwiseSaleQData.getDouble(3));

		}
		arrListTaxwiseSaleData.add(objSettle);
		hmSalesTaxWiseSaleData.put(rsTaxwiseSaleQData.getString(1), arrListTaxwiseSaleData);
	    }
	    rsTaxwiseSaleQData.close();

	    //For Tip details of Live and Q data
	    sqlQData.setLength(0);

	    sqlQData.append(" select sum(a.dblTipAmount) from tblqbillhd a "
		    + " where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    if (!cmbPosCode.getSelectedItem().toString().equals("All"))
	    {
		sqlQData.append(" and a.strPOSCode='" + objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString()) + "' ");
	    }

	    sqlQData.append(" group by a.strBillNo");

	    double finalTipAmt = 0;

	    ResultSet rsTipQData = clsGlobalVarClass.dbMysql.executeResultSet(sqlQData.toString());
	    while (rsTipQData.next())
	    {
		finalTipAmt = finalTipAmt + rsTipQData.getDouble(1);
	    }
	    rsTipQData.close();

	    double totalDebitAmt = 0, totalCreditAmt = 0;
	    for (Map.Entry<String, List<clsSettelementOptions>> entry : hmSalesSettleData.entrySet())
	    {
		double totalSaleAmt = 0;
		List<clsSettelementOptions> listOfSettleDataDtl = entry.getValue();
		for (int j = 0; j < listOfSettleDataDtl.size(); j++)
		{
		    clsSettelementOptions objSettle = listOfSettleDataDtl.get(j);
		    totalSaleAmt += objSettle.getDblSettlementAmt();
		}
		totalDebitAmt += totalSaleAmt;
	    }

	    /*
             * for (Map.Entry<String, List<clsSettelementOptions>> entry :
             * hmSalesCreditSettleData.entrySet()) { double totalSaleAmt = 0;
             * List<clsSettelementOptions> listOfSettleDataDtl =
             * entry.getValue(); for (int j = 0; j < listOfSettleDataDtl.size();
             * j++) { clsSettelementOptions objSettle =
             * listOfSettleDataDtl.get(j); totalSaleAmt +=
             * objSettle.getDblSettlementAmt(); } totalDebitAmt += totalSaleAmt;
             * }
	     */
	    //    totalDebitAmt += finalDisAmt;
	    for (Map.Entry<String, List<clsSettelementOptions>> entry : hmSalesGroupWiseSaleData.entrySet())
	    {
		double totalSaleAmt = 0;
		List<clsSettelementOptions> listOfGroupwiseSaleDataDtl = entry.getValue();
		for (int j = 0; j < listOfGroupwiseSaleDataDtl.size(); j++)
		{
		    clsSettelementOptions objSettle = listOfGroupwiseSaleDataDtl.get(j);
		    totalSaleAmt += objSettle.getDblSettlementAmt();
		}
		totalCreditAmt += totalSaleAmt;
	    }
	    for (Map.Entry<String, List<clsSettelementOptions>> entry : hmSalesTaxWiseSaleData.entrySet())
	    {
		double totalSaleAmt = 0;
		List<clsSettelementOptions> listOfTaxwiseSaleDataDtl = entry.getValue();
		for (int j = 0; j < listOfTaxwiseSaleDataDtl.size(); j++)
		{
		    clsSettelementOptions objSettle = listOfTaxwiseSaleDataDtl.get(j);
		    totalSaleAmt += objSettle.getDblSettlementAmt();
		}
		totalCreditAmt += totalSaleAmt;
	    }
	    totalCreditAmt += finalTipAmt;

	    double roundOff = totalDebitAmt - totalCreditAmt;
	    double finalDeditroundOff = 0, finalCreditroundOff = 0;

	    String line = "________________________________________";
	    clsUtility objUtil = (clsUtility) objUtility.clone();
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
	    funPrintBlankLines("From :" + funGetCalenderDate("From") + "  To :" + funGetCalenderDate("To"), pw);
	    funPrintBlankLines("Posting Report", pw);
	    pw.println();
	    pw.println(line);
	    pw.println("Particulars                        Debit");
	    pw.println(line);
	    pw.println("POS  :" + cmbPosCode.getSelectedItem().toString());
	    pw.println();

	    double finalDebitAmount = 0, finalCreditAmount = 0;

	    //For Settlement Detail
	    double finalSettleAmt = 0;
	    for (Map.Entry<String, List<clsSettelementOptions>> entry : hmSalesSettleData.entrySet())
	    {
		count++;
		double totalSaleAmt = 0;
		List<clsSettelementOptions> listOfSettleDataDtl = entry.getValue();
		pw.println(objUtil.funPrintTextWithAlignment(entry.getKey(), 40, "Left"));
		pw.println();

		for (int j = 0; j < listOfSettleDataDtl.size(); j++)
		{
		    clsSettelementOptions objSettle = listOfSettleDataDtl.get(j);
		    pw.print(objUtil.funPrintTextWithAlignment(objSettle.getStrSettelmentDesc(), 32, "Left"));
		    pw.print(objUtil.funPrintTextWithAlignment(String.valueOf(Math.rint(objSettle.getDblSettlementAmt())), 8, "Right"));
		    pw.println();
		    totalSaleAmt += objSettle.getDblSettlementAmt();
		}
		pw.println(line);
		pw.print(objUtil.funPrintTextWithAlignment("Total", 32, "Left"));
		pw.print(objUtil.funPrintTextWithAlignment(String.valueOf(Math.rint(totalSaleAmt)), 8, "Right"));
		pw.println();
		pw.println();

		finalSettleAmt += totalSaleAmt;
	    }

	    pw.println(line);

	    pw.println(line);

	    pw.print(objUtil.funPrintTextWithAlignment("Total Amount ", 32, "Left"));
	    pw.print(objUtil.funPrintTextWithAlignment(String.valueOf(Math.rint(finalSettleAmt)), 8, "Right"));
	    pw.println();

	    finalDebitAmount = finalDebitAmount + finalSettleAmt;

	    pw.println(line);

	    if (roundOff < 0)
	    {
		roundOff = roundOff * (-1);
		finalDeditroundOff = roundOff;
		pw.println();
		pw.print(objUtil.funPrintTextWithAlignment("Round Off ", 32, "Left"));
		pw.print(objUtil.funPrintTextWithAlignment(String.valueOf(Math.rint(roundOff)), 8, "Right"));
		pw.println();
		finalDebitAmount += roundOff;
	    }
	    pw.print(objUtil.funPrintTextWithAlignment("POS Total ", 32, "Left"));
	    pw.print(objUtil.funPrintTextWithAlignment(String.valueOf(Math.rint(finalDebitAmount)), 8, "Right"));
	    pw.println();
	    pw.println(line);

	    pw.println();
	    pw.println(line);
	    pw.println("Particulars                       Credit");
	    pw.println(line);
	    pw.println("POS  :" + cmbPosCode.getSelectedItem().toString());
	    pw.println();

	    //For Groupwise sale data
	    double finalGroupSaleAmt = 0;
	    pw.println(objUtil.funPrintTextWithAlignment("Sale", 40, "Left"));
	    pw.println(line);
	    for (Map.Entry<String, List<clsSettelementOptions>> entry : hmSalesGroupWiseSaleData.entrySet())
	    {
		double totalSaleAmt = 0;
		List<clsSettelementOptions> listOfGroupwiseSaleDataDtl = entry.getValue();
		for (int j = 0; j < listOfGroupwiseSaleDataDtl.size(); j++)
		{
		    clsSettelementOptions objSettle = listOfGroupwiseSaleDataDtl.get(j);
		    pw.print(objUtil.funPrintTextWithAlignment(objSettle.getStrSettelmentDesc(), 32, "Left"));
		    pw.print(objUtil.funPrintTextWithAlignment(String.valueOf(Math.rint(objSettle.getDblSettlementAmt())), 8, "Right"));
		    totalSaleAmt += objSettle.getDblSettlementAmt();
		    pw.println();
		}
		finalGroupSaleAmt += totalSaleAmt;
	    }

	    pw.println(line);
	    pw.print(objUtil.funPrintTextWithAlignment("Total ", 32, "Left"));
	    pw.print(objUtil.funPrintTextWithAlignment(String.valueOf(Math.rint(finalGroupSaleAmt)), 8, "Right"));
	    pw.println();
	    pw.println(line);

	    finalCreditAmount = finalCreditAmount + finalGroupSaleAmt;
	    //For Taxwise detial data

	    double finalTaxAmt = 0;
	    pw.println(objUtil.funPrintTextWithAlignment("Tax", 40, "Left"));
	    pw.println(line);
	    for (Map.Entry<String, List<clsSettelementOptions>> entry : hmSalesTaxWiseSaleData.entrySet())
	    {
		double totalSaleAmt = 0;
		List<clsSettelementOptions> listOfTaxwiseSaleDataDtl = entry.getValue();
		for (int j = 0; j < listOfTaxwiseSaleDataDtl.size(); j++)
		{
		    clsSettelementOptions objSettle = listOfTaxwiseSaleDataDtl.get(j);
		    pw.print(objUtil.funPrintTextWithAlignment(objSettle.getStrSettelmentDesc(), 32, "Left"));
		    pw.print(objUtil.funPrintTextWithAlignment(String.valueOf(Math.rint(objSettle.getDblSettlementAmt())), 8, "Right"));
		    totalSaleAmt += objSettle.getDblSettlementAmt();
		    pw.println();
		}
		finalTaxAmt += totalSaleAmt;
	    }

	    pw.println(line);
	    pw.print(objUtil.funPrintTextWithAlignment("Total ", 32, "Left"));
	    pw.print(objUtil.funPrintTextWithAlignment(String.valueOf(Math.rint(finalTaxAmt)), 8, "Right"));
	    pw.println();
	    pw.println(line);

	    finalCreditAmount = finalCreditAmount + finalTaxAmt;
	    //For Tip
	    pw.print(objUtil.funPrintTextWithAlignment("Tip ", 32, "Left"));
	    pw.print(objUtil.funPrintTextWithAlignment(String.valueOf(Math.rint(finalTipAmt)), 8, "Right"));
	    pw.println();
	    pw.println(line);
	    finalCreditAmount = finalCreditAmount + finalTipAmt;

	    pw.println(line);

	    if (roundOff > 0)
	    {
		finalCreditroundOff = roundOff;
		pw.print(objUtil.funPrintTextWithAlignment("Round Off ", 32, "Left"));
		pw.print(objUtil.funPrintTextWithAlignment(String.valueOf(Math.rint(roundOff)), 8, "Right"));
		pw.println();
		finalCreditAmount += roundOff;
	    }

	    pw.print(objUtil.funPrintTextWithAlignment("POS Total ", 32, "Left"));
	    pw.print(objUtil.funPrintTextWithAlignment(String.valueOf(Math.rint(finalCreditAmount)), 8, "Right"));
	    pw.println();
	    pw.println(line);
	    pw.println(line);
	    pw.print(objUtil.funPrintTextWithAlignment("Debit Grand Total ", 32, "Left"));
	    pw.print(objUtil.funPrintTextWithAlignment(String.valueOf(Math.rint(finalDebitAmount)), 8, "Right"));
	    pw.println();
	    pw.println(line);
	    pw.println(line);
	    pw.print(objUtil.funPrintTextWithAlignment("Credit Grand Total ", 32, "Left"));
	    pw.print(objUtil.funPrintTextWithAlignment(String.valueOf(Math.rint(finalCreditAmount)), 8, "Right"));
	    pw.println();
	    pw.println();
	    pw.println();
	    pw.println();
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

	    funPrintReportToPrinter(clsGlobalVarClass.gBillPrintPrinterPort, file.getAbsolutePath());
	    //  funShowTextFile(file, "Posting Report");
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}

	return count;
    }

    private void funPrintReportToPrinter(String printerName, String fileName)
    {
	try
	{

	    if ("windows".equalsIgnoreCase(clsPosConfigFile.gPrintOS) && clsGlobalVarClass.gPrintType.equalsIgnoreCase("Text File"))
	    {
		PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
		DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
		int printerIndex = 0;
		PrintService printService[] = PrintServiceLookup.lookupPrintServices(flavor, pras);
		for (int i = 0; i < printService.length; i++)
		{

		    if (clsGlobalVarClass.gBillPrintPrinterPort.equalsIgnoreCase(printService[i].getName()))
		    {
			printerIndex = i;
			break;
		    }
		}
		DocPrintJob job = printService[printerIndex].createPrintJob();
		FileInputStream fis = new FileInputStream(fileName);
		DocAttributeSet das = new HashDocAttributeSet();
		Doc doc = new SimpleDoc(fis, flavor, das);
		job.print(doc, pras);
	    }
	    else if ("linux".equalsIgnoreCase(clsPosConfigFile.gPrintOS) && clsGlobalVarClass.gPrintType.equalsIgnoreCase("Text File"))
	    {
		Process process = Runtime.getRuntime().exec("lpr -P " + printerName + " " + fileName, null);
	    }

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    public int funGenerateItemWiseTextReport(File file, String pos, String fromDate, String toDate)
    {
	int count = 0;
	try
	{
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

	    String dottedLine = "------------------------------------------";

	    pw.println(dottedLine);
	    pw.println("Cost Center Name");
	    funDrawUnderLine("Cost Center Name", pw);
	    pw.println("Menu Item Name");
	    pw.println("       Rate         Qty        Amount");
	    pw.println(dottedLine);

	    int cnt = 0;
	    String costCenterName = "";
	    double grandTotal = 0.00, quantityTotal = 0.00, costCenterTotal = 0.00;

	    //KOT
	    StringBuilder sqlLiveData = new StringBuilder();
	    StringBuilder sqlQData = new StringBuilder();
	    //DB
	    StringBuilder sqlLiveDataDB = new StringBuilder();
	    StringBuilder sqlQDataDB = new StringBuilder();

	    //*****KOT******//        
	    sqlLiveData.append(" SELECT a.strItemName,ifnull(d.strPriceMonday,0.00),sum(a.dblQuantity), sum(a.dblAmount)-sum(a.dblDiscountAmt), "
		    + " ifnull(f.strCostCenterCode,''),a.strItemCode  "
		    + " FROM tblbilldtl a left outer join tblbillhd b on a.strBillNo=b.strBillNo  AND DATE(a.dteBillDate)=DATE(b.dteBillDate) "
		    + " left outer join tbltablemaster c on b.strTableNo = c.strTableNo "
		    + " left outer join tblmenuitempricingdtl d on a.strItemCode = d.strItemCode and b.strPOSCode=d.strPOSCode and c.strAreaCode= d.strAreaCode "
		    + " left outer join tblcostcentermaster f on d.strCostCenterCode=f.strCostCenterCode  "
		    + " where date(b.dteBillDate)  between '" + fromDate + "' and '" + toDate + "'  "
		    + "  ");
	    sqlQData.append(" SELECT a.strItemName,ifnull(d.strPriceMonday,0.00),sum(a.dblQuantity), sum(a.dblAmount)-sum(a.dblDiscountAmt), "
		    + " ifnull(f.strCostCenterCode,''),a.strItemCode "
		    + " FROM tblqbilldtl a left outer join tblqbillhd b on a.strBillNo=b.strBillNo AND DATE(a.dteBillDate)=DATE(b.dteBillDate) "
		    + " left outer join tbltablemaster c on b.strTableNo = c.strTableNo "
		    + " left outer join tblmenuitempricingdtl d on a.strItemCode = d.strItemCode and b.strPOSCode=d.strPOSCode  and c.strAreaCode= d.strAreaCode "
		    + " left outer join tblcostcentermaster f on d.strCostCenterCode=f.strCostCenterCode  "
		    + " where date(b.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		    + " ");
	    //*****KOT******//        

	    //******DB******//
	    sqlLiveDataDB.append("SELECT a.strItemName,ifnull(d.strPriceMonday,0.00),sum(a.dblQuantity), sum(a.dblAmount)-sum(a.dblDiscountAmt), "
		    + " ifnull(f.strCostCenterCode,''),a.strItemCode  "
		    + " FROM tblbilldtl a left outer join tblbillhd b on a.strBillNo=b.strBillNo  AND DATE(a.dteBillDate)=DATE(b.dteBillDate) "
		    + " left outer join tblmenuitempricingdtl d on a.strItemCode = d.strItemCode and b.strPOSCode=d.strPOSCode and d.strAreaCode=b.strAreaCode "
		    + " left outer join tblcostcentermaster f on d.strCostCenterCode=f.strCostCenterCode  "
		    + " where date(b.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		    + " and b.strAreaCode='" + clsGlobalVarClass.gDineInAreaForDirectBiller + "' "
		    + " and (b.strTableNo='' or b.strTableNo=null) ");
	    sqlQDataDB.append("SELECT a.strItemName,ifnull(d.strPriceMonday,0.00),sum(a.dblQuantity), sum(a.dblAmount)-sum(a.dblDiscountAmt), "
		    + " ifnull(f.strCostCenterCode,''),a.strItemCode  "
		    + " FROM tblqbilldtl a left outer join tblqbillhd b on a.strBillNo=b.strBillNo  AND DATE(a.dteBillDate)=DATE(b.dteBillDate) "
		    + " left outer join tblmenuitempricingdtl d on a.strItemCode = d.strItemCode and b.strPOSCode=d.strPOSCode and d.strAreaCode=b.strAreaCode "
		    + "  left outer join tblcostcentermaster f on d.strCostCenterCode=f.strCostCenterCode  "
		    + " where date(b.dteBillDate) between '" + fromDate + "' and '" + toDate + "'  "
		    + " and b.strAreaCode='" + clsGlobalVarClass.gDineInAreaForDirectBiller + "' "
		    + " and (b.strTableNo='' or b.strTableNo=null) ");
	    //******DB******//

	    if (!cmbPosCode.getSelectedItem().toString().equals("All"))
	    {
		//kot
		sqlLiveData.append(" and b.strPOSCode='" + objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString()) + "' ");
		sqlQData.append(" and b.strPOSCode='" + objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString()) + "' ");
		//DB
		sqlLiveDataDB.append(" and b.strPOSCode='" + objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString()) + "' ");
		sqlQDataDB.append(" and b.strPOSCode='" + objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString()) + "' ");
	    }

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
	    if (!shiftNo.equalsIgnoreCase("All"))
	    {
		sqlLiveData.append(" and b.intShiftCode='" + shiftNo + "'  ");
		sqlLiveData.append(" and b.intShiftCode='" + shiftNo + "'  ");

		sqlLiveDataDB.append(" and b.intShiftCode='" + shiftNo + "'  ");
		sqlQDataDB.append(" and b.intShiftCode='" + shiftNo + "'  ");
	    }

	    //KOT
	    sqlLiveData.append(" group by f.strCostCenterCode, a.strItemCode,a.strItemName");
	    sqlQData.append(" group by f.strCostCenterCode, a.strItemCode,a.strItemName");
	    //DB
	    sqlLiveDataDB.append(" group by f.strCostCenterCode, a.strItemCode,a.strItemName");
	    sqlQDataDB.append(" group by f.strCostCenterCode, a.strItemCode,a.strItemName");

	    clsGlobalVarClass.dbMysql.execute("truncate tbltempsalesflash1");
	    String sqlInsertData = " insert into tbltempsalesflash1(strbillno,dtebilldate,tmebilltime,strtablename,strposcode,strpaymode)";
	    //KOT
	    clsGlobalVarClass.dbMysql.execute(sqlInsertData + "(" + sqlLiveData + ")");
	    clsGlobalVarClass.dbMysql.execute(sqlInsertData + "(" + sqlQData + ")");
	    //DB
	    // clsGlobalVarClass.dbMysql.execute(sqlInsertData + "(" + sqlLiveDataDB + ")");
	    // clsGlobalVarClass.dbMysql.execute(sqlInsertData + "(" + sqlQDataDB + ")");

	    String sqlSelectData = " select strbillno,ifnull(dtebilldate,'0.00'),sum(tmebilltime),sum(strtablename),ifnull(strposcode,''),strpaymode from tbltempsalesflash1 "
		    + " group by strposcode,strpaymode,strbillno";

	    ResultSet rsItemWise = clsGlobalVarClass.dbMysql.executeResultSet(sqlSelectData);
	    while (rsItemWise.next())
	    {
		count++;
		if (!costCenterName.equals(rsItemWise.getString(5)))
		{
		    if (cnt > 0)
		    {
			pw.println(dottedLine);
			pw.print(costCenterName + " TOTAL");
			funPrintTextWithAlignment("right", String.valueOf(costCenterTotal), 26, pw);
			pw.println();
			pw.println(dottedLine);
			pw.println();
		    }
		    pw.println();
		    pw.println(rsItemWise.getString(5));
		    funDrawUnderLine(rsItemWise.getString(5), pw);
		    costCenterName = rsItemWise.getString(5);
		    costCenterTotal = 0.00;
		}

		pw.println(rsItemWise.getString(1));
		funPrintTextWithAlignment("right", rsItemWise.getString(2), 12, pw); // Rate
		funPrintTextWithAlignment("right", rsItemWise.getString(3), 12, pw); // Qty
		funPrintTextWithAlignment("right", rsItemWise.getString(4), 13, pw); // Amt
		pw.println();

		// Query To Print Modifier Items
		StringBuilder sqlLiveModifier = new StringBuilder();
		StringBuilder sqlQModifier = new StringBuilder();

		sqlLiveModifier.append("SELECT a.strModifierName,sum(a.dblQuantity),sum(a.dblAmount),a.strItemCode "
			+ "FROM tblbillmodifierdtl a, tblbillhd b "
			+ "where a.strBillNo=b.strBillNo and date(b.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
			+ "and left(a.strItemCode,7)='" + rsItemWise.getString(6) + "' ");
		sqlQModifier.append("SELECT a.strModifierName,sum(a.dblQuantity),sum(a.dblAmount),a.strItemCode "
			+ "FROM tblqbillmodifierdtl a, tblqbillhd b "
			+ "where a.strBillNo=b.strBillNo and date(b.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
			+ "and left(a.strItemCode,7)='" + rsItemWise.getString(6) + "' ");

		if (!cmbPosCode.getSelectedItem().toString().equals("All"))
		{
		    sqlLiveModifier.append(" and b.strPOSCode='" + objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString()) + "' ");
		    sqlQModifier.append(" and b.strPOSCode='" + objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString()) + "' ");
		}
		sqlLiveModifier.append(" group by a.strItemCode ");
		sqlQModifier.append(" group by a.strItemCode ");
		//System.out.println(sql);

//            System.out.println("liveModi-->"+sqlLiveModifier);
//            System.out.println("qModi-->"+sqlQModifier);
		clsGlobalVarClass.dbMysql.execute("truncate tbltempsalesflash");
		clsGlobalVarClass.dbMysql.execute("insert into tbltempsalesflash(strcode,strname,strposcode,struser)(" + sqlLiveModifier + ")");
		clsGlobalVarClass.dbMysql.execute("insert into tbltempsalesflash(strcode,strname,strposcode,struser)(" + sqlQModifier + ")");

		String sql = "select strcode,sum(strname),sum(strposcode) from tbltempsalesflash group by struser ";

		ResultSet rsModItems = clsGlobalVarClass.dbMysql.executeResultSet(sql);
		while (rsModItems.next())
		{
		    pw.println(rsModItems.getString(1));

		    double modRate = 0;
		    if (rsModItems.getDouble(3) > 0)
		    {
			modRate = Math.rint(rsModItems.getDouble(3) / rsModItems.getDouble(2));
		    }
		    funPrintTextWithAlignment("right", String.valueOf(modRate), 12, pw); // Rate
		    funPrintTextWithAlignment("right", rsModItems.getString(2), 12, pw); // Qty
		    funPrintTextWithAlignment("right", rsModItems.getString(3), 13, pw); // Amt
		    pw.println();

		    quantityTotal += Double.parseDouble(rsModItems.getString(2));
		    grandTotal += Double.parseDouble(rsModItems.getString(3));
		    costCenterTotal += Double.parseDouble(rsModItems.getString(3));
		}
		rsModItems.close();

		quantityTotal += Double.parseDouble(rsItemWise.getString(3));
		grandTotal += Double.parseDouble(rsItemWise.getString(4));
		costCenterTotal += Double.parseDouble(rsItemWise.getString(4));
		cnt++;

		if (rsItemWise.isLast())
		{
		    pw.println(dottedLine);
		    pw.print(costCenterName + " TOTAL");
		    funPrintTextWithAlignment("right", String.valueOf(costCenterTotal), 26, pw);
		    pw.println();
		}
	    }
	    rsItemWise.close();

	    pw.println(dottedLine);
	    pw.print("GRAND TOTAL");
	    funPrintTextWithAlignment("right", String.valueOf(Math.rint(quantityTotal)), 13, pw);
	    funPrintTextWithAlignment("right", String.valueOf(Math.rint(grandTotal)), 11, pw);
	    pw.println();
	    pw.println(dottedLine);
	    pw.println();
	    pw.println();
	    pw.println();
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
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}

	return count;
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
		data = data + line + " ";
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

    private void funItemWiseExcelSheetOfReport()
    {
	String reportType = cmbReportType.getSelectedItem().toString();
	String posName = cmbPosCode.getSelectedItem().toString();

	HashMap hm = funGetCommonHashMapForJasperReport();
	hm.put("posName", posName);
	String printComplimentaryYN = cmbUserName.getSelectedItem().toString();
	hm.put("printComplimentaryYN", printComplimentaryYN);

	clsItemWiseSalesReport objItemWiseSalesReport = new clsItemWiseSalesReport();
	objItemWiseSalesReport.funGenerateItemWiseReport(reportType, hm, "No");

    }

    private void funBillWiseExcelSheetOfReport() throws Exception
    {

	String reportType = cmbReportType.getSelectedItem().toString();
	String posName = cmbPosCode.getSelectedItem().toString();

	HashMap hm = funGetCommonHashMapForJasperReport();
	hm.put("posName", posName);

	clsBillWiseSalesReport objBillWiseSalesReport = new clsBillWiseSalesReport();
	objBillWiseSalesReport.funGenerateBillWiseReport(reportType, hm, "No");

    }

    private void funMenuWiseExcelSheetOfReport() throws Exception
    {
	String reportType = cmbReportType.getSelectedItem().toString();
	String posName = cmbPosCode.getSelectedItem().toString();

	HashMap hm = funGetCommonHashMapForJasperReport();
	hm.put("posName", posName);

	clsMenuHeadWiseSalesReport objMenuHeadWiseSalesReport = new clsMenuHeadWiseSalesReport();
	objMenuHeadWiseSalesReport.funGenerateMenuHeadWiseReport(reportType, hm, "No");

    }

    private void funGroupWiseExcelSheetOfReport() throws Exception
    {
	String reportType = cmbReportType.getSelectedItem().toString();
	String subGroup = cmbWaiterCode.getSelectedItem().toString();

	HashMap hm = funGetCommonHashMapForJasperReport();
	hm.put("subGroup", subGroup);

	clsGroupWiseReport objGroupWiseReport = new clsGroupWiseReport();
	objGroupWiseReport.funGroupWiseReport(reportType, hm, "No");
    }

    private void funSubGroupWiseExcelSheetOfReport() throws Exception
    {
	String reportType = cmbReportType.getSelectedItem().toString();

	HashMap hm = funGetCommonHashMapForJasperReport();

	clsSubGroupWiseReport objSubGroupWiseReport = new clsSubGroupWiseReport();
	objSubGroupWiseReport.funSubGroupWiseReport(reportType, hm, "No");
    }

    private void funComplimentarySettlementExcelSheetOfReport() throws Exception
    {
	String reportType = "", rptType = "";
	reportType = cmbReportType.getSelectedItem().toString();
	rptType = cmbSettlementMode.getSelectedItem().toString();

	HashMap hm = funGetCommonHashMapForJasperReport();
	hm.put("rptType", rptType);
	String reasonName = cmbResonMaster.getSelectedItem().toString();
	String reasonCode = cmbResonMaster.getSelectedItem().toString().split("                                         ")[1];
	hm.put("reasonCode", reasonCode);
	hm.put("reasonName", reasonName);

	clsComplimentaryBillReport objComplimentaryBillReport = new clsComplimentaryBillReport();
	objComplimentaryBillReport.funGenerateComplimentaryBillReport(reportType, hm, "No");

    }

    public void funGroupSubGroupWiseExcelSheetOfReport() throws Exception
    {
	String reportType = cmbReportType.getSelectedItem().toString();
	String groupCodeArr[] = cmbUserName.getSelectedItem().toString().split("                                         ");
	String subGroupCodeArr[] = cmbSettlementMode.getSelectedItem().toString().split("                                         ");
	String groupCode = groupCodeArr[1].trim();
	String subGroupCode = subGroupCodeArr[1].trim();
	String posName = cmbPosCode.getSelectedItem().toString();
	String rptType = cmbPaymentMode.getSelectedItem().toString();

	HashMap hm = funGetCommonHashMapForJasperReport();
	hm.put("rptType", rptType);
	hm.put("posName", posName);
	hm.put("groupCode", groupCode);
	hm.put("subGroupCode", subGroupCode);

	clsGroupSubGroupWiseReport objGroupSubGroupWiseReport = new clsGroupSubGroupWiseReport();
	objGroupSubGroupWiseReport.funGenerateGroupSubGroupWiseReport(reportType, hm, "No");

    }

    private void funCounterReportExcelSheetOfReport() throws Exception
    {
	try
	{

	    if ((dteToDate.getDate().getTime() - dteFromDate.getDate().getTime()) < 0)
	    {
		new frmOkPopUp(this, "Invalid date", "Error", 1).setVisible(true);
		return;
	    }

	    String reportType = "", rptType = "";
	    reportType = cmbReportType.getSelectedItem().toString();
	    rptType = cmbSettlementMode.getSelectedItem().toString();

	    HashMap hm = funGetCommonHashMapForJasperReport();
	    hm.put("rptType", rptType);

	    clsCounterWiseReport objCounterWiseReport = new clsCounterWiseReport();
	    objCounterWiseReport.funCounterWiseReport(reportType, hm, "No");
	}
	catch (Exception e)
	{
	    if (e.getMessage().startsWith("Byte data not found at"))
	    {
		JOptionPane.showMessageDialog(null, "Report Image Not Found!!! Please Check Property Setup Report Image.", "Error Code: RIMG-1", JOptionPane.ERROR_MESSAGE);
	    }
	    e.printStackTrace();
	}
    }

    private void funDiscountExcelSheetOfReport() throws Exception
    {
	String reportType = "", rptType = "";
	reportType = cmbReportType.getSelectedItem().toString();
	rptType = cmbUserName.getSelectedItem().toString();

	HashMap hm = funGetCommonHashMapForJasperReport();
	hm.put("rptType", rptType);

	clsDiscountWiseReport objDiscountWiseReport = new clsDiscountWiseReport();
	objDiscountWiseReport.funGenerateDsicountWiseReport(reportType, hm, "No");

    }

    private void funNonChargableKOTExcelSheetOfReport() throws Exception
    {
	String reportType = "";
	reportType = cmbReportType.getSelectedItem().toString();
	String ResonMaster = cmbResonMaster.getSelectedItem().toString();
	StringBuilder sbreson = new StringBuilder(ResonMaster);
	int length = ResonMaster.length();
	int lastIndex = sbreson.lastIndexOf(" ");
	String reasonCode = sbreson.substring(lastIndex + 1, length).toString();

	HashMap hm = funGetCommonHashMapForJasperReport();
	hm.put("reasonCode", reasonCode);

	clsNonChargableKOTReport objNonChargableKOTReport = new clsNonChargableKOTReport();
	objNonChargableKOTReport.funNonChargableKOTReport(reportType, hm, "No");
    }

    private void funSettlementWiseExcelSheetOfReport()
    {
	String reportType = cmbReportType.getSelectedItem().toString();

	HashMap hm = funGetCommonHashMapForJasperReport();

	clsSettlementWiseReport objSettlementWiseReport = new clsSettlementWiseReport();
	objSettlementWiseReport.funSettlementWiseReport(reportType, hm, "No");
    }

    private void funGuestCreditExcelSheetOfReport()
    {
	String posName = cmbPosCode.getSelectedItem().toString();
	String reportType = cmbReportType.getSelectedItem().toString();
	HashMap hm = funGetCommonHashMapForJasperReport();
	hm.put("posName", posName);
	clsGuestCreditReport objGuestCreditReport = new clsGuestCreditReport();
	objGuestCreditReport.funGuestCreditReport(reportType, hm, "No");
    }

    private void funOrderAnalysisExcelSheetOfReport()
    {
	String reportType = cmbReportType.getSelectedItem().toString();
	HashMap hm = funGetCommonHashMapForJasperReport();

	clsOrderAnalysisReport objOrderAnalysisReport = new clsOrderAnalysisReport();
	objOrderAnalysisReport.funOrderAnalysisReport(reportType, hm, "No");
    }

    private void funTaxWiseExcelSheetOfReport()
    {
	try
	{
	    String reportType = cmbReportType.getSelectedItem().toString();
	    HashMap hm = funGetCommonHashMapForJasperReport();
	    clsTaxWiseSalesReport objTaxWiseSalesReport = new clsTaxWiseSalesReport();
	    objTaxWiseSalesReport.funTaxWiseSalesReport(reportType, hm, "No");
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funWaiterWiseItemWiseIncentivesExcelReport()
    {

	String reportType = cmbReportType.getSelectedItem().toString();

	HashMap hm = funGetCommonHashMapForJasperReport();
	String type = cmbPaymentMode.getSelectedItem().toString();
	String groupCodeArr[] = cmbUserName.getSelectedItem().toString().split("                                         ");
	String subGroupCodeArr[] = cmbSettlementMode.getSelectedItem().toString().split("                                         ");
	String groupCode = groupCodeArr[1].trim();
	String subGroupCode = subGroupCodeArr[1].trim();

	hm.put("groupCode", groupCode);
	hm.put("subGroupCode", subGroupCode);

	clsWaiterWiseItemWiseIncentivesSummaryReport objWaiterWiseItemWiseIncentivesSummaryReport = new clsWaiterWiseItemWiseIncentivesSummaryReport();
	objWaiterWiseItemWiseIncentivesSummaryReport.funWaiterWiseItemWiseIncentivesSummaryReport(reportType, hm, "No", type);
    }

    private void funVoidBillExcelSheetOfReport()
    {
	String reportType = cmbReportType.getSelectedItem().toString();
	String rptType = cmbReportMode.getSelectedItem().toString();
	String reasonCodeArr[] = cmbResonMaster.getSelectedItem().toString().split("                                         ");
	String reasonName = reasonCodeArr[0].trim();
	String reasonCode = reasonCodeArr[1].trim();

	HashMap hm = funGetCommonHashMapForJasperReport();
	hm.put("rptType", rptType);
	hm.put("reasonName", reasonName);
	hm.put("reasonCode", reasonCode);
	clsVoidBillReport objVoidBillReport = new clsVoidBillReport();
	objVoidBillReport.funVoidBillReport(reportType, hm, "No");
    }

    private void funTaxBreakupSummaryExcelSheetOfReport()
    {
	String reportType = cmbReportType.getSelectedItem().toString();
	HashMap hm = funGetCommonHashMapForJasperReport();

	clsTaxBreakupSummaryReport objTaxBreakupSummaryReport = new clsTaxBreakupSummaryReport();
	objTaxBreakupSummaryReport.funTaxBreakupSummaryReport(reportType, hm, "No");
    }

    private void funWaiterWiseItemExcelSheetOfReport()
    {
	String reportType = "";
	reportType = cmbReportType.getSelectedItem().toString();

	String WaiterCode = cmbWaiterCode.getSelectedItem().toString();
	StringBuilder sbWaiter = new StringBuilder(WaiterCode);
	int length = WaiterCode.length();
	int lastIndex = sbWaiter.lastIndexOf(" ");
	String waiterNo = sbWaiter.substring(lastIndex + 1, length).toString();

	HashMap hm = funGetCommonHashMapForJasperReport();
	hm.put("waiterCode", waiterNo);

	clsWaiterWiseItemReport objWaiterWiseItemReport = new clsWaiterWiseItemReport();
	objWaiterWiseItemReport.funWaiterWiseItemReport(reportType, hm, "No");
    }

    private void funAuditorsReportExcelSheetOfReport()
    {
	String reportType = cmbReportType.getSelectedItem().toString();
	HashMap hm = funGetCommonHashMapForJasperReport();
	clsAuditorsReport objAuditorsReport = new clsAuditorsReport();
	objAuditorsReport.funAuditorsReport(reportType, hm, "No");
    }

    private void funWaiterWiseIncentivesExcelSheetOfReport()
    {
	String reportType = "", rptType = "";
	reportType = cmbReportType.getSelectedItem().toString();
	rptType = cmbSettlementMode.getSelectedItem().toString();

	HashMap hm = funGetCommonHashMapForJasperReport();
	hm.put("rptType", rptType);

	clsWaiterWiseIncentiveSalesReport objComplimentaryBillReport = new clsWaiterWiseIncentiveSalesReport();
	objComplimentaryBillReport.funGenerateWaiterWiseIncentivesWiseReport(reportType, hm, "No");

    }

    private void funDeliveryboyIncentivesExcelSheetOfReport()
    {
	try
	{
	    String reportType = cmbReportType.getSelectedItem().toString();
	    String rptType = cmbReportMode.getSelectedItem().toString();

	    String dpName = cmbDeliveryboy.getSelectedItem().toString();
	    String sqlQuery = "select strDPCode from tbldeliverypersonmaster where strDPName='" + dpName + "' ";
	    ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(sqlQuery);
	    while (rs.next())
	    {
		dpCode = rs.getString(1);
	    }
	    rs.close();
	    if (cmbDeliveryboy.getSelectedItem().equals("All"))
	    {
		dpCode = "All";
	    }

	    HashMap hm = funGetCommonHashMapForJasperReport();
	    hm.put("rptType", rptType);
	    hm.put("DPName", dpName);
	    hm.put("DPCode", dpCode);

	    clsDeliveryboyIncentivesReport objDeliveryboyIncentivesReport = new clsDeliveryboyIncentivesReport();
	    objDeliveryboyIncentivesReport.funDeliveryboyIncentivesReport(reportType, hm, "No");
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funVoidKOTExcelSheetOfReport()
    {
	try
	{
	    String reportType = cmbReportType.getSelectedItem().toString();
	    HashMap hm = funGetCommonHashMapForJasperReport();

	    clsVoidKOTReport objVoidKOTReport = new clsVoidKOTReport();
	    objVoidKOTReport.funVoidKOTReport(reportType, hm, "No", cmbUserName.getSelectedItem().toString().trim());
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funSubGroupWiseSummaryExcelSheetOfReport()
    {
	String reportType = cmbReportType.getSelectedItem().toString();
	HashMap hm = funGetCommonHashMapForJasperReport();

	clsSubGroupWiseSummaryReport objSubGroupWiseSummaryReport = new clsSubGroupWiseSummaryReport();
	objSubGroupWiseSummaryReport.funSubGroupWiseSummaryReport(reportType, hm, "No");
    }

    private void funDailyCollectionExcelSheetOfReport()
    {
	try
	{
	    String reportType = cmbReportType.getSelectedItem().toString();

	    HashMap hm = funGetCommonHashMapForJasperReport();

	    clsDailyCollectionReport objDailyCollectionReport = new clsDailyCollectionReport();
	    objDailyCollectionReport.funDailyCollectionReport(reportType, hm, "No");
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funDailySalesExcelSheetOfReport()
    {
	try
	{
	    String reportType = cmbReportType.getSelectedItem().toString();

	    HashMap hm = funGetCommonHashMapForJasperReport();

	    clsDailySalesReport objDailySalesReport = new clsDailySalesReport();
	    objDailySalesReport.funDailyCollectionReport(reportType, hm, "No");
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funUnusedCardBalanceExcelSheetOfReport()
    {
	try
	{
	    String reportType = cmbReportType.getSelectedItem().toString();

	    HashMap hm = funGetCommonHashMapForJasperReport();

	    clsUnusedCardBalanceReport objUnusedCardBalanceReport = new clsUnusedCardBalanceReport();
	    objUnusedCardBalanceReport.funUnusedCardBalanceReport(reportType, hm, "No");
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funRevenueHeadWiseExcelSheetOfReport()
    {
	try
	{
	    String reportType = cmbReportType.getSelectedItem().toString();
	    String rptType = cmbSettlementMode.getSelectedItem().toString();
	    String revenueHead = cmbUserName.getSelectedItem().toString();

	    HashMap hm = funGetCommonHashMapForJasperReport();
	    hm.put("rptType", rptType);
	    hm.put("revenueHead", revenueHead);

	    clsRevenueHeadWiseReport objRevenueHeadWiseReport = new clsRevenueHeadWiseReport();
	    objRevenueHeadWiseReport.funRevenueHeadWiseReport(reportType, hm, "No");
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funItemWiseConsumptionExcelReport()
    {

	try
	{

	    String reportType = cmbReportType.getSelectedItem().toString();

	    HashMap hm = funGetCommonHashMapForJasperReport();

	    String group = cmbUserName.getSelectedItem().toString();
	    String printModifiers = cmbSettlementMode.getSelectedItem().toString();
	    String costCenter = cmbPaymentMode.getSelectedItem().toString();
	    int lastIndex = group.lastIndexOf(" ");

	    String groupName = group.substring(0, lastIndex - 1).trim();
	    String groupCode = group.substring(lastIndex, group.length()).trim();

	    lastIndex = costCenter.lastIndexOf(" ");
	    String costCenterName = costCenter.substring(0, lastIndex - 1).trim();
	    String costCenterCode = costCenter.substring(lastIndex, costCenter.length()).trim();

	    hm.put("GroupCode", groupCode);
	    hm.put("GroupName", groupName);
	    hm.put("PrintZeroAmountModi", printModifiers);
	    hm.put("costCenterCode", costCenterCode);
	    hm.put("costCenterName", costCenterName);

	    clsItemWiseConsumptionReport objItemWiseConsumptionReport = new clsItemWiseConsumptionReport();
	    objItemWiseConsumptionReport.funItemWiseConsumptionReport(reportType, hm, "No");
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    /**
     * Item Wise Consumption Report
     */
    private void funItemWiseConsumptionJasper()
    {
	String reportType = cmbReportType.getSelectedItem().toString();

	HashMap hm = funGetCommonHashMapForJasperReport();

	String group = cmbUserName.getSelectedItem().toString();
	String printModifiers = cmbSettlementMode.getSelectedItem().toString();
	String costCenter = cmbPaymentMode.getSelectedItem().toString();
	int lastIndex = group.lastIndexOf(" ");

	String groupName = group.substring(0, lastIndex - 1).trim();
	String groupCode = group.substring(lastIndex, group.length()).trim();

	lastIndex = costCenter.lastIndexOf(" ");
	String costCenterName = costCenter.substring(0, lastIndex - 1).trim();
	String costCenterCode = costCenter.substring(lastIndex, costCenter.length()).trim();

	hm.put("GroupCode", groupCode);
	hm.put("GroupName", groupName);
	hm.put("PrintZeroAmountModi", printModifiers);
	hm.put("costCenterCode", costCenterCode);
	hm.put("costCenterName", costCenterName);
	hm.put("clientName", clsGlobalVarClass.gClientName);

	clsItemWiseConsumptionReport objItemWiseConsumptionReport = new clsItemWiseConsumptionReport();
	//
	if (cmbResonMaster.getSelectedItem().toString().equalsIgnoreCase("POS Wise Cost Center"))
	{
	    objItemWiseConsumptionReport.funItemWiseConsumptionReport(reportType, hm, "No");
	}
	else if (cmbResonMaster.getSelectedItem().toString().equalsIgnoreCase("Cost Center"))
	{
	    objItemWiseConsumptionReport.funItemWiseConsumptionReportCostCenter(reportType, hm, "No");
	}
	else
	{
	    objItemWiseConsumptionReport.funItemWiseConsumptionMenuHead(reportType, hm, "No");
	}

    }

    private void funTableWisePaxJasperReport() throws Exception
    {
	String reportType = cmbReportType.getSelectedItem().toString();

	HashMap hm = funGetCommonHashMapForJasperReport();

	clsTableWisePax objTableWisePax = new clsTableWisePax();
	objTableWisePax.funItemWiseConsumptionReport(reportType, hm);

    }

    private void funTableWisePaxExcelSheetOfReport() throws Exception
    {
	String reportType = cmbReportType.getSelectedItem().toString();

	HashMap hm = funGetCommonHashMapForJasperReport();

	clsTableWisePax objTableWisePax = new clsTableWisePax();
	objTableWisePax.funItemWiseConsumptionReport(reportType, hm);
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
        java.awt.GridBagConstraints gridBagConstraints;

        pnlHeader = new javax.swing.JPanel();
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
        };
        ;
        pnlMain = new javax.swing.JPanel();
        lblReportType = new javax.swing.JLabel();
        cmbReportType = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        dteToDate = new com.toedter.calendar.JDateChooser();
        cmbPosCode = new javax.swing.JComboBox();
        lblPOSName = new javax.swing.JLabel();
        btnView = new javax.swing.JButton();
        btnBack = new javax.swing.JButton();
        disReportName = new javax.swing.JLabel();
        dteFromDate = new com.toedter.calendar.JDateChooser();
        lblToDate = new javax.swing.JLabel();
        lblFromDate = new javax.swing.JLabel();
        panelExtraComp = new javax.swing.JPanel();
        lblUserName = new javax.swing.JLabel();
        lblSettlementMode = new javax.swing.JLabel();
        lblPaymentMode = new javax.swing.JLabel();
        cmbPaymentMode = new javax.swing.JComboBox();
        lblResonMaster = new javax.swing.JLabel();
        cmbResonMaster = new javax.swing.JComboBox();
        cmbWaiterCode = new javax.swing.JComboBox();
        lblWaiterName = new javax.swing.JLabel();
        lblDeliverboyName = new javax.swing.JLabel();
        cmbDeliveryboy = new javax.swing.JComboBox();
        lblReportMode = new javax.swing.JLabel();
        cmbReportMode = new javax.swing.JComboBox();
        cmbSettlementMode = new javax.swing.JComboBox();
        cmbUserName = new javax.swing.JComboBox();
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

        pnlHeader.setBackground(new java.awt.Color(69, 164, 238));
        pnlHeader.setLayout(new javax.swing.BoxLayout(pnlHeader, javax.swing.BoxLayout.LINE_AXIS));

        lblProductName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblProductName.setForeground(new java.awt.Color(255, 255, 255));
        lblProductName.setText("SPOS - ");
        pnlHeader.add(lblProductName);

        lblModuleName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblModuleName.setForeground(new java.awt.Color(255, 255, 255));
        pnlHeader.add(lblModuleName);

        lblformName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblformName.setForeground(new java.awt.Color(255, 255, 255));
        lblformName.setText("-Sales Report");
        pnlHeader.add(lblformName);
        pnlHeader.add(filler4);
        pnlHeader.add(filler5);

        lblPosName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblPosName.setForeground(new java.awt.Color(255, 255, 255));
        lblPosName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblPosName.setMaximumSize(new java.awt.Dimension(321, 30));
        lblPosName.setMinimumSize(new java.awt.Dimension(321, 30));
        lblPosName.setPreferredSize(new java.awt.Dimension(321, 30));
        pnlHeader.add(lblPosName);
        pnlHeader.add(filler6);

        lblUserCode.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblUserCode.setForeground(new java.awt.Color(255, 255, 255));
        lblUserCode.setMaximumSize(new java.awt.Dimension(90, 30));
        lblUserCode.setMinimumSize(new java.awt.Dimension(90, 30));
        lblUserCode.setPreferredSize(new java.awt.Dimension(90, 30));
        pnlHeader.add(lblUserCode);

        lblDate.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblDate.setForeground(new java.awt.Color(255, 255, 255));
        lblDate.setMaximumSize(new java.awt.Dimension(192, 30));
        lblDate.setMinimumSize(new java.awt.Dimension(192, 30));
        lblDate.setPreferredSize(new java.awt.Dimension(192, 30));
        pnlHeader.add(lblDate);

        lblHOSign.setMaximumSize(new java.awt.Dimension(34, 30));
        lblHOSign.setMinimumSize(new java.awt.Dimension(34, 30));
        lblHOSign.setPreferredSize(new java.awt.Dimension(34, 30));
        pnlHeader.add(lblHOSign);

        getContentPane().add(pnlHeader, java.awt.BorderLayout.PAGE_START);

        pnlBackGround.setOpaque(false);
        pnlBackGround.setLayout(new java.awt.GridBagLayout());

        pnlMain.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, new java.awt.Color(204, 204, 204), new java.awt.Color(204, 204, 204), new java.awt.Color(153, 153, 153), new java.awt.Color(153, 153, 153)));
        pnlMain.setMinimumSize(new java.awt.Dimension(800, 570));
        pnlMain.setOpaque(false);
        pnlMain.setPreferredSize(new java.awt.Dimension(800, 570));

        lblReportType.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblReportType.setText("Report Type             :");

        cmbReportType.setBackground(new java.awt.Color(51, 102, 255));
        cmbReportType.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        cmbReportType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "A4 Size Report", "Text File-40 Column Report", "Excel Report" }));
        cmbReportType.setToolTipText("Select Report Type");

        dteToDate.setToolTipText("Select To Date");

        cmbPosCode.setBackground(new java.awt.Color(51, 102, 255));
        cmbPosCode.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        cmbPosCode.setForeground(new java.awt.Color(255, 255, 255));
        cmbPosCode.setToolTipText("Select POS");
        cmbPosCode.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                cmbPosCodeActionPerformed(evt);
            }
        });

        lblPOSName.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblPOSName.setText("POS Name                :");

        btnView.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnView.setForeground(new java.awt.Color(255, 255, 255));
        btnView.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnView.setText("VIEW");
        btnView.setToolTipText("View Reports");
        btnView.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnView.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn2.png"))); // NOI18N
        btnView.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnViewMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt)
            {
                btnViewMouseEntered(evt);
            }
        });

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

        disReportName.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        disReportName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        disReportName.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        dteFromDate.setToolTipText("Select From Date");

        lblToDate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblToDate.setText("To Date                   :");

        lblFromDate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblFromDate.setText("From Date                :");

        panelExtraComp.setOpaque(false);

        lblUserName.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblUserName.setText("User Name                :");

        lblSettlementMode.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblSettlementMode.setText("Settlement Mode      :");

        lblPaymentMode.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblPaymentMode.setText("Payment Mode          :");

        cmbPaymentMode.setBackground(new java.awt.Color(51, 102, 255));

        lblResonMaster.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblResonMaster.setText("Reason Master          :");

        cmbResonMaster.setBackground(new java.awt.Color(51, 102, 255));
        cmbResonMaster.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        cmbResonMaster.setToolTipText("Select Reason");

        cmbWaiterCode.setBackground(new java.awt.Color(51, 102, 255));
        cmbWaiterCode.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        cmbWaiterCode.setToolTipText("Select Reason");

        lblWaiterName.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblWaiterName.setText("Waiter Name            :");

        lblDeliverboyName.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblDeliverboyName.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblDeliverboyName.setText("Deliveryboy Name     :");

        cmbDeliveryboy.setBackground(new java.awt.Color(51, 102, 255));
        cmbDeliveryboy.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N

        lblReportMode.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblReportMode.setText("Report Mode            :");

        cmbReportMode.setBackground(new java.awt.Color(51, 102, 255));
        cmbReportMode.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        cmbReportMode.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Summary", "Detail", "Group Wise" }));

        cmbSettlementMode.setBackground(new java.awt.Color(51, 102, 255));
        cmbSettlementMode.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        cmbSettlementMode.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Summary", "Detail", "Group Wise" }));
        cmbSettlementMode.setToolTipText("Select User ");
        cmbSettlementMode.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                cmbSettlementModeMouseClicked(evt);
            }
        });

        cmbUserName.setBackground(new java.awt.Color(51, 102, 255));
        cmbUserName.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        cmbUserName.setForeground(new java.awt.Color(255, 255, 255));
        cmbUserName.setToolTipText("Select User ");
        cmbUserName.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                cmbUserNameActionPerformed(evt);
            }
        });

        lblShiftNo.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblShiftNo.setText("Shift No                   :");

        cmbShiftNo.setBackground(new java.awt.Color(51, 102, 255));
        cmbShiftNo.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        cmbShiftNo.setToolTipText("Select Reason");

        javax.swing.GroupLayout panelExtraCompLayout = new javax.swing.GroupLayout(panelExtraComp);
        panelExtraComp.setLayout(panelExtraCompLayout);
        panelExtraCompLayout.setHorizontalGroup(
            panelExtraCompLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelExtraCompLayout.createSequentialGroup()
                .addGroup(panelExtraCompLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblSettlementMode)
                    .addComponent(lblPaymentMode)
                    .addGroup(panelExtraCompLayout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addGroup(panelExtraCompLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblReportMode, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblShiftNo, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(panelExtraCompLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(lblUserName, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE)
                        .addComponent(lblResonMaster, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(panelExtraCompLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(lblDeliverboyName, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblWaiterName, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(7, 7, 7)
                .addGroup(panelExtraCompLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cmbSettlementMode, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbUserName, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbWaiterCode, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbResonMaster, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbDeliveryboy, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbReportMode, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbPaymentMode, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbShiftNo, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        panelExtraCompLayout.setVerticalGroup(
            panelExtraCompLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelExtraCompLayout.createSequentialGroup()
                .addGroup(panelExtraCompLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblUserName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbUserName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelExtraCompLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSettlementMode, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbSettlementMode, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelExtraCompLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPaymentMode, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbPaymentMode, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(7, 7, 7)
                .addGroup(panelExtraCompLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblResonMaster, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbResonMaster, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelExtraCompLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbWaiterCode, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblWaiterName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelExtraCompLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cmbDeliveryboy)
                    .addComponent(lblDeliverboyName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelExtraCompLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblReportMode, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbReportMode, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(panelExtraCompLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblShiftNo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbShiftNo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        javax.swing.GroupLayout pnlMainLayout = new javax.swing.GroupLayout(pnlMain);
        pnlMain.setLayout(pnlMainLayout);
        pnlMainLayout.setHorizontalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addGap(311, 311, 311)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 322, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMainLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addComponent(btnView, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(60, 60, 60)
                        .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(1, 1, 1))
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(lblToDate, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblReportType, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblFromDate, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblPOSName, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlMainLayout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(dteFromDate, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(dteToDate, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(cmbReportType, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMainLayout.createSequentialGroup()
                                .addGap(19, 19, 19)
                                .addComponent(cmbPosCode, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(panelExtraComp, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(disReportName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlMainLayout.setVerticalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMainLayout.createSequentialGroup()
                .addComponent(disReportName, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblPOSName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbPosCode, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dteFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dteToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbReportType, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblReportType, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelExtraComp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnView, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        pnlBackGround.add(pnlMain, gridBagConstraints);

        getContentPane().add(pnlBackGround, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnBackMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnBackMouseClicked
	// TODO add your handling code here:
	dispose();
	clsGlobalVarClass.hmActiveForms.remove("Auditor Report");
	clsGlobalVarClass.hmActiveForms.remove("Bill Wise Report");
	clsGlobalVarClass.hmActiveForms.remove("Complimentary Settlement Report");
	clsGlobalVarClass.hmActiveForms.remove("Counter Wise Sales Report");
	clsGlobalVarClass.hmActiveForms.remove("Discount Report");
	clsGlobalVarClass.hmActiveForms.remove("Group Wise Report");
	clsGlobalVarClass.hmActiveForms.remove("SubGroupWise Report");
	clsGlobalVarClass.hmActiveForms.remove("Group-SubGroup Wise Report");
	clsGlobalVarClass.hmActiveForms.remove("Item Wise Report");
	clsGlobalVarClass.hmActiveForms.remove("Non Chargable KOT Report");
	clsGlobalVarClass.hmActiveForms.remove("OperatorWise Report");
	clsGlobalVarClass.hmActiveForms.remove("Order Analysis Report");
	clsGlobalVarClass.hmActiveForms.remove("SettlementWise Report");
	clsGlobalVarClass.hmActiveForms.remove("Tax Wise Report");
	clsGlobalVarClass.hmActiveForms.remove("Void Bill Report");
	clsGlobalVarClass.hmActiveForms.remove("Tax Breakup Summary Report");
	clsGlobalVarClass.hmActiveForms.remove("Menu Head Wise");
	clsGlobalVarClass.hmActiveForms.remove("WaiterWiseItemReport");
	clsGlobalVarClass.hmActiveForms.remove("WaiterWiseIncentivesReport");
	clsGlobalVarClass.hmActiveForms.remove("DeliveryboyIncentive");
	clsGlobalVarClass.hmActiveForms.remove("Daily Collection Report");
	clsGlobalVarClass.hmActiveForms.remove("Daily Sales Report");
	clsGlobalVarClass.hmActiveForms.remove("Void KOT Report");
	clsGlobalVarClass.hmActiveForms.remove("Guest Credit Report");
	clsGlobalVarClass.hmActiveForms.remove("SubGroupWiseSummaryReport");
	clsGlobalVarClass.hmActiveForms.remove("UnusedCardB,alanceReport");
	clsGlobalVarClass.hmActiveForms.remove("Revenue Head Wise Item Sales");
	clsGlobalVarClass.hmActiveForms.remove("Item Wise Consumption");
	clsGlobalVarClass.hmActiveForms.remove("Table Wise Pax Report");
	clsGlobalVarClass.hmActiveForms.remove("Posting Report");
	clsGlobalVarClass.hmActiveForms.remove("Placed Order Report");
	clsGlobalVarClass.hmActiveForms.remove("Open Item Wise Audit Report");
	clsGlobalVarClass.hmActiveForms.remove("Non Selling Items");
	clsGlobalVarClass.hmActiveForms.remove("Debtors As On");
	clsGlobalVarClass.hmActiveForms.remove("Payment Receipt Report");
	clsGlobalVarClass.hmActiveForms.remove("Credit Report");
	clsGlobalVarClass.hmActiveForms.remove("Consolidated Discount Report");
	clsGlobalVarClass.hmActiveForms.remove("Customer Ledger");


    }//GEN-LAST:event_btnBackMouseClicked
    /**
     * Report type Selection A4,Text
     *
     * @param evt
     */
    private void btnViewMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnViewMouseClicked

	funSalesMenuReportViewMouseClicked();

    }//GEN-LAST:event_btnViewMouseClicked


    private void cmbPosCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbPosCodeActionPerformed
	// TODO add your handling code here:
	funFillShiftCombo();
    }//GEN-LAST:event_cmbPosCodeActionPerformed

    private void cmbUserNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbUserNameActionPerformed
	// TODO add your handling code here:
	if ((salesReportName.equals("Placed Order Report")))
	{
	    funFillOrderMaster();
	}
    }//GEN-LAST:event_cmbUserNameActionPerformed

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
	// TODO add your handling code here:

	funRemoveForms();

    }//GEN-LAST:event_btnBackActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
	// TODO add your handling code here:
	funRemoveForms();
    }//GEN-LAST:event_formWindowClosed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
	// TODO add your handling code here:
	funRemoveForms();
    }//GEN-LAST:event_formWindowClosing

    private void cmbSettlementModeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cmbSettlementModeMouseClicked
	// TODO add your handling code here:
    }//GEN-LAST:event_cmbSettlementModeMouseClicked

    private void btnViewMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnViewMouseEntered
	// TODO add your handling code here:
    }//GEN-LAST:event_btnViewMouseEntered


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnView;
    private javax.swing.JComboBox cmbDeliveryboy;
    private javax.swing.JComboBox cmbPaymentMode;
    private javax.swing.JComboBox cmbPosCode;
    private javax.swing.JComboBox cmbReportMode;
    private javax.swing.JComboBox cmbReportType;
    private javax.swing.JComboBox cmbResonMaster;
    private javax.swing.JComboBox cmbSettlementMode;
    private javax.swing.JComboBox cmbShiftNo;
    private javax.swing.JComboBox cmbUserName;
    private javax.swing.JComboBox cmbWaiterCode;
    private javax.swing.JLabel disReportName;
    private com.toedter.calendar.JDateChooser dteFromDate;
    private com.toedter.calendar.JDateChooser dteToDate;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblDeliverboyName;
    private javax.swing.JLabel lblFromDate;
    private javax.swing.JLabel lblHOSign;
    private javax.swing.JLabel lblModuleName;
    private javax.swing.JLabel lblPOSName;
    private javax.swing.JLabel lblPaymentMode;
    private javax.swing.JLabel lblPosName;
    private javax.swing.JLabel lblProductName;
    private javax.swing.JLabel lblReportMode;
    private javax.swing.JLabel lblReportType;
    private javax.swing.JLabel lblResonMaster;
    private javax.swing.JLabel lblSettlementMode;
    private javax.swing.JLabel lblShiftNo;
    private javax.swing.JLabel lblToDate;
    private javax.swing.JLabel lblUserCode;
    private javax.swing.JLabel lblUserName;
    private javax.swing.JLabel lblWaiterName;
    private javax.swing.JLabel lblformName;
    private javax.swing.JPanel panelExtraComp;
    private javax.swing.JPanel pnlBackGround;
    private javax.swing.JPanel pnlHeader;
    private javax.swing.JPanel pnlMain;
    // End of variables declaration//GEN-END:variables

    private void funGuestCreditReportForJasper()
    {

	String posName = cmbPosCode.getSelectedItem().toString();
	String reportType = cmbReportType.getSelectedItem().toString();
	HashMap hm = funGetCommonHashMapForJasperReport();
	hm.put("posName", posName);
	clsGuestCreditReport objGuestCreditReport = new clsGuestCreditReport();
	objGuestCreditReport.funGuestCreditReport(reportType, hm, "No");
    }

    private void funSubGroupWiseSummaryReportForJasper()
    {

	String reportType = cmbReportType.getSelectedItem().toString();
	HashMap hm = funGetCommonHashMapForJasperReport();

	clsSubGroupWiseSummaryReport objSubGroupWiseSummaryReport = new clsSubGroupWiseSummaryReport();
	objSubGroupWiseSummaryReport.funSubGroupWiseSummaryReport(reportType, hm, "No");
    }

    private void funUnusedCardBalanceReportForJasper()
    {

	String reportType = cmbReportType.getSelectedItem().toString();

	HashMap hm = funGetCommonHashMapForJasperReport();

	clsUnusedCardBalanceReport objUnusedCardBalanceReport = new clsUnusedCardBalanceReport();
	objUnusedCardBalanceReport.funUnusedCardBalanceReport(reportType, hm, "No");

    }

    private void funRevenueHeadWiseReportForJasper()
    {

	String reportType = cmbReportType.getSelectedItem().toString();
	String rptType = cmbSettlementMode.getSelectedItem().toString();
	String revenueHead = cmbUserName.getSelectedItem().toString();

	HashMap hm = funGetCommonHashMapForJasperReport();
	hm.put("rptType", rptType);
	hm.put("revenueHead", revenueHead);

	clsRevenueHeadWiseReport objRevenueHeadWiseReport = new clsRevenueHeadWiseReport();
	objRevenueHeadWiseReport.funRevenueHeadWiseReport(reportType, hm, "No");
    }

    private void funExportToOtherFormat(JasperPrint print)
    {
	try
	{
	    String reportDestination = System.getProperty("user.dir") + File.separator + "DBBackup" + File.separator + salesReportName + clsGlobalVarClass.gPOSDateToDisplay;  //This is generated Correctly    
	    String exportFormat = "xls";

	    switch (exportFormat)
	    {
		case "xls":

		    //xls
		    JRXlsExporter exporterXLS = new JRXlsExporter();
		    exporterXLS.setParameter(JRXlsExporterParameter.JASPER_PRINT, print);
		    exporterXLS.setParameter(JRXlsExporterParameter.CHARACTER_ENCODING, "UTF-8");
		    exporterXLS.setParameter(JRXlsExporterParameter.IS_IGNORE_CELL_BACKGROUND, Boolean.TRUE);
		    exporterXLS.setParameter(JRXlsExporterParameter.IS_IGNORE_CELL_BORDER, Boolean.FALSE);
		    exporterXLS.setParameter(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, Boolean.FALSE);
		    exporterXLS.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);
		    exporterXLS.setParameter(JRXlsExporterParameter.IS_COLLAPSE_ROW_SPAN, Boolean.FALSE);
		    exporterXLS.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_COLUMNS, Boolean.TRUE);
		    exporterXLS.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
		    exporterXLS.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE, Boolean.FALSE);
		    exporterXLS.setParameter(JRXlsExporterParameter.IS_IMAGE_BORDER_FIX_ENABLED, Boolean.FALSE);
		    exporterXLS.setParameter(JRXlsExporterParameter.IS_FONT_SIZE_FIX_ENABLED, Boolean.FALSE);
		    exporterXLS.setParameter(JRXlsExporterParameter.IS_IGNORE_GRAPHICS, Boolean.TRUE);
		    exporterXLS.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, reportDestination + ".xls");

		    exporterXLS.exportReport();
		    break;

		case "xlsx":

		    //xlsx                                
		    File xlsFile = new File(reportDestination + ".xlsx");
		    JRXlsxExporter Xlsxexporter = new JRXlsxExporter();
		    Xlsxexporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
		    Xlsxexporter.setParameter(JRExporterParameter.OUTPUT_FILE, xlsFile);

		    Xlsxexporter.exportReport();//File is generated Correctly    
		    break;
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    public int funGenerateBillWiseTextReport(File file, String posCode, String fromDate, String toDate)
    {
	int count = 0;
	DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();

	try
	{
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
	    pw.println("POS  :" + cmbPosCode.getSelectedItem().toString());
	    pw.println("From :" + funGetCalenderDate("From") + "  To :" + funGetCalenderDate("To"));

	    pw.println("---------------------------------------");
	    pw.println("Bill No       Bill Date         Total");
	    pw.println("---------------------------------------");

	    double grandTotal = 0.00;

	    Map mapMultiSettleBills = new HashMap();

	    StringBuilder sqlBuilder = new StringBuilder();
	    //live
	    sqlBuilder.setLength(0);
	    sqlBuilder.append("select a.strBillNo,DATE_FORMAT(date(a.dteBillDate),'%d-%m-%Y') as dteBillDate ,b.strPosName, "
		    + "ifnull(d.strSettelmentDesc,'') as strSettelmentMode,a.dblDiscountAmt,a.dblTaxAmt  "
		    + ",sum(c.dblSettlementAmt) as dblSettlementAmt,a.dblSubTotal,a.strSettelmentMode,intBillSeriesPaxNo "
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
	    if (!shiftNo.equalsIgnoreCase("All"))
	    {
		sqlBuilder.append(" and a.intShiftCode='" + shiftNo + "'  ");
	    }

	    sqlBuilder.append("GROUP BY a.strClientCode,date(a.dteBillDate),a.strBillNo,d.strSettelmentCode "
		    + "ORDER BY a.strBillNo ASC ");

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
		    + "ifnull(d.strSettelmentDesc,'') as strSettelmentMode,a.dblDiscountAmt,a.dblTaxAmt   "
		    + ",sum(c.dblSettlementAmt) as dblSettlementAmt,a.dblSubTotal,a.strSettelmentMode,intBillSeriesPaxNo "
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
		sqlBuilder.append(" and a.intShiftCode='" + shiftNo + "'  ");
	    }

	    sqlBuilder.append("GROUP BY a.strClientCode,date(a.dteBillDate),a.strBillNo,d.strSettelmentCode "
		    + "ORDER BY a.strBillNo ASC ");

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
	    rsData.close();

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
		    billDateComparator, billNoComparator
	    ));

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
	    funPrintTextWithAlignment("right", String.valueOf(grandTotal), 13, pw);
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
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}

	return count;
    }

    public int funGenerateTableWisePaxTextReport(File file, String pos, String fromDate, String toDate)
    {
	int count = 0;
	String sqlFilter = "";
	try
	{
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
	    funPrintBlankLines("Table Wise Pax Report", pw);

	    pw.println();
	    pw.println("POS  :" + cmbPosCode.getSelectedItem().toString());
	    pw.println("From :" + funGetCalenderDate("From") + "  To :" + funGetCalenderDate("To"));

	    pw.println("----------------------------------------");
	    pw.println("Table                                Pax");
	    pw.println("----------------------------------------");

	    int cnt = 0, totalPax = 0;
	    String sqlLive = "select b.strTableNo,b.strTableName,sum(a.intBillSeriesPaxNo) "
		    + " from tblbillhd a,tbltablemaster b "
		    + " where a.strTableNo=b.strTableNo "
		    + " and date( a.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
		    + " and a.strClientCode=b.strClientCode "
		    + " group by b.strTableNo";

	    String sqlQFile = "select b.strTableNo,b.strTableName,sum(a.intBillSeriesPaxNo) "
		    + " from tblqbillhd a,tbltablemaster b "
		    + " where a.strTableNo=b.strTableNo "
		    + " and date( a.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
		    + " and a.strClientCode=b.strClientCode "
		    + " group by b.strTableNo";

	    if (!pos.equals("All"))
	    {
		sqlFilter += " and a.strPOSCode = '" + objUtility.funGetPOSCodeFromPOSName(pos) + "' ";
	    }

	    sqlLive = sqlLive + " " + sqlFilter;
	    sqlQFile = sqlQFile + " " + sqlFilter;

	    clsGlobalVarClass.dbMysql.execute("truncate table tbltempsalesflash;");
	    clsGlobalVarClass.dbMysql.execute("insert into tbltempsalesflash(strcode,strname,strposcode)(" + sqlLive + ")");
	    clsGlobalVarClass.dbMysql.execute("insert into tbltempsalesflash(strcode,strname,strposcode)(" + sqlQFile + ")");

	    String sql = "select strcode,strname,strposcode from tbltempsalesflash  ";
	    ResultSet rsTablePaxWise = clsGlobalVarClass.dbMysql.executeResultSet(sql);
	    while (rsTablePaxWise.next())
	    {
		count++;
		//pw.print(objUtility.funPrintTextWithAlignment(rsTablePaxWise.getString(1), 15, "Left"));
//                pw.print(objUtility.funPrintTextWithAlignment(rsTablePaxWise.getString(2), 20, "Left"));
//                pw.print(objUtility.funPrintTextWithAlignment(rsTablePaxWise.getString(3),30, "Right"));

		pw.print(objUtility.funPrintTextWithAlignment(rsTablePaxWise.getString(2), 15, "Left"));
		pw.print(objUtility.funPrintTextWithAlignment(" ", 18, "Left"));
		pw.print(objUtility.funPrintTextWithAlignment(rsTablePaxWise.getString(3), 7, "Right"));

		//  pw.print(rsTablePaxWise.getString(1)+"      "+rsTablePaxWise.getString(2)); // Pax
		//funPrintTextWithAlignment("right", rsTablePaxWise.getString(3), 12, pw);
		pw.println();
		totalPax = totalPax + Integer.parseInt(rsTablePaxWise.getString(3));
	    }
	    rsTablePaxWise.close();

	    pw.println("----------------------------------------");
	    pw.print(objUtility.funPrintTextWithAlignment("GRAND TOTAL", 15, "Left"));
	    pw.print(objUtility.funPrintTextWithAlignment(" ", 18, "Left"));
	    pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(Math.rint(totalPax)), 7, "Right"));
	    //   funPrintTextWithAlignment("right", String.valueOf(Math.rint(totalPax)), 7, pw);
	    pw.println();
	    pw.println("----------------------------------------");
	    pw.println();
	    pw.println();
	    pw.println();
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
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}

	return count;
    }

    public int funGenerateNonChargableKOTTextReport(File file, String pos, String fromDate, String toDate)
    {
	DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();
	int count = 0;
	try
	{
	    //Set Reason Code
	    String ResonMaster = cmbResonMaster.getSelectedItem().toString();
	    StringBuilder sbreson = new StringBuilder(ResonMaster);
	    int length = ResonMaster.length();
	    int lastIndex = sbreson.lastIndexOf(" ");
	    String Reason = sbreson.substring(lastIndex + 1, length).toString();

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
	    funPrintBlankLines("Non Chargable KOT Report", pw);

	    pw.println();
	    pw.println("POS  :" + cmbPosCode.getSelectedItem().toString());
	    pw.println("From :" + funGetCalenderDate("From") + "  To :" + funGetCalenderDate("To"));

	    pw.println("---------------------------------------");
	    pw.println("Item Name");
	    pw.println("       Rate         Qty        Amount");
	    pw.println("---------------------------------------");

	    int cnt = 0;
	    String strkotNo = "";
	    double grandTotal = 0.00, quantityTotal = 0.00, kotTotal = 0.00;
	    String sql = "select a.strKOTNo,date(a.dteNCKOTDate), a.strTableNo, b.strReasonName,d.strPosName, "
		    + "a.strRemark, a.strUserCreated, a.strItemCode, c.strItemName, a.dblQuantity, a.dblRate, a.dblQuantity * a.dblRate as Amount "
		    + "from tblnonchargablekot a, tblreasonmaster b, tblitemmaster c,tblposmaster d "
		    + "where  a.strReasonCode = b.strReasonCode and  d.strPosCode=if('" + objUtility.funGetPOSCodeFromPOSName(pos) + "'='All',d.strPOSCode,'" + objUtility.funGetPOSCodeFromPOSName(pos) + "') "
		    + "and a.strItemCode = c.strItemCode  and a.strPosCode=d.strPOSCode "
		    + "and date(a.dteNCKOTDate) between '" + fromDate + "' and '" + toDate + "' "
		    + "and a.strReasonCode =if('" + Reason + "'='ALL',a.strReasonCode,'" + Reason + "')";

//             String shiftNo = "All", shiftCode = "All";
//            if (clsGlobalVarClass.gEnableShiftYN)
//            {
//                if (clsGlobalVarClass.gEnableShiftYN && (!cmbShiftNo.getSelectedItem().toString().equalsIgnoreCase("All")))
//                {
//                    shiftNo = cmbShiftNo.getSelectedItem().toString();
//                    shiftCode = cmbShiftNo.getSelectedItem().toString();
//                }
//                else
//                {
//                    shiftNo = cmbShiftNo.getSelectedItem().toString();
//                    shiftCode = cmbShiftNo.getSelectedItem().toString();
//                }
//            }
//            if (!shiftNo.equalsIgnoreCase("All"))
//            {
//                sql=sql+" and a.intShiftCode='"+shiftNo+"' ";
//            }
	    System.out.println(sql);
	    ResultSet rsNCKot = clsGlobalVarClass.dbMysql.executeResultSet(sql);
	    while (rsNCKot.next())
	    {
		count++;
		if (!strkotNo.equals(rsNCKot.getString(1)))
		{
		    if (cnt > 0)
		    {
			pw.println("---------------------------------------");
			pw.print(strkotNo + " TOTAL");
			funPrintTextWithAlignment("right", String.valueOf(kotTotal), 22, pw);
			pw.println();
			pw.println("---------------------------------------");
			pw.println();
		    }
		    pw.println();
		    pw.println("---------------------------------------");
		    funPrintTextWithLeftAlignment("left", "KOT No: " + rsNCKot.getString(1), 20, pw);
		    funPrintTextWithLeftAlignment("right", "Bill Date: " + rsNCKot.getString(2), 20, pw);
		    pw.println();
		    funPrintTextValueWithAlignment("right", "Reason: " + rsNCKot.getString(4), 30, pw);
		    pw.println();
		    pw.println("---------------------------------------");
		    strkotNo = rsNCKot.getString(1);
		    kotTotal = 0.00;
		}

		pw.println(rsNCKot.getString(9));
		funPrintTextWithAlignment("right", gDecimalFormat.format(rsNCKot.getDouble(11)), 16, pw);
		funPrintTextWithAlignment("right", gDecimalFormat.format(rsNCKot.getDouble(10)), 14, pw);
		funPrintTextWithAlignment("right", gDecimalFormat.format(rsNCKot.getDouble(12)), 18, pw);
		pw.println();
		quantityTotal += Double.parseDouble(rsNCKot.getString(10));
		grandTotal += Double.parseDouble(rsNCKot.getString(12));
		kotTotal += Double.parseDouble(rsNCKot.getString(12));
		cnt++;

		if (rsNCKot.isLast())
		{
		    pw.println("---------------------------------------");
		    pw.print(strkotNo + " TOTAL");
		    funPrintTextWithAlignment("right", gDecimalFormat.format(kotTotal), 22, pw);
		    pw.println();
		    pw.println("---------------------------------------");
		    pw.println();
		}
	    }
	    rsNCKot.close();

	    pw.println("---------------------------------------");
	    pw.print("GRAND TOTAL");
	    funPrintTextWithAlignment("right", gDecimalFormat.format(quantityTotal), 14, pw);
	    funPrintTextWithAlignment("right", gDecimalFormat.format(grandTotal), 11, pw);
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
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}

	return count;
    }

    public int funGenerateComplimentarySettlementTextReport(File file, String pos, String fromDate, String toDate)
    {
	int count = 0;
	try
	{

	    //Set Reason Code
	    String ResonMaster = cmbResonMaster.getSelectedItem().toString();
	    StringBuilder sbreson = new StringBuilder(ResonMaster);
	    int length = ResonMaster.length();
	    int lastIndex = sbreson.lastIndexOf(" ");
	    String Reason = sbreson.substring(lastIndex + 1, length).toString();

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
	    funPrintBlankLines("Complimentary Settlement Report", pw);

	    pw.println();
	    pw.println("POS  :" + cmbPosCode.getSelectedItem().toString());
	    pw.println("From :" + funGetCalenderDate("From") + "  To :" + funGetCalenderDate("To"));

	    pw.println("---------------------------------------");
	    pw.println("Item Name");
	    pw.println("       Rate         Qty        Amount");
	    pw.println("---------------------------------------");

	    int cnt = 0;
	    String strBillNo = "";
	    double grandTotal = 0.00, quantityTotal = 0.00, BillTotal = 0.00;
	    String sql = "select a.strBillNo,date(a.dteBillDate), a.dblGrandTotal, c.dblSettlementAmt, "
		    + "a.strUserCreated, a.dteDateCreated, e.strReasonName, a.strRemarks, "
		    + "b.strItemCode, b.strItemName, b.dblQuantity, b.dblRate, b.dblAmount,  "
		    + "b.strKOTNo "
		    + "from vqbillhd a, vqbilldtl b, vqbillsettlementdtl c, tblsettelmenthd d, tblreasonmaster e "
		    + "where a.strBillNo = b.strBillNo "
		    + "and a.strBillNo = c.strBillNo "
		    + "and c.strSettlementCode = d.strSettelmentCode "
		    + "and a.strReasonCode = e.strReasonCode "
		    + "and d.strSettelmentType = 'Complementary' "
		    + "and strPOSCode=if('" + objUtility.funGetPOSCodeFromPOSName(pos) + "'='All',strPOSCode,'" + objUtility.funGetPOSCodeFromPOSName(pos) + "') "
		    + "and date(a.dteBillDate) Between '" + fromDate + "' and '" + toDate + "' "
		    + "and a.strReasonCode=if('" + Reason + "'='ALL',a.strReasonCode,'" + Reason + "')";

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
	    if (!shiftNo.equalsIgnoreCase("All"))
	    {
		sql = sql + " and a.intShiftCode='" + shiftNo + "' ";
	    }

	    System.out.println(sql);
	    ResultSet rsComplementarySettlement = clsGlobalVarClass.dbMysql.executeResultSet(sql);
	    while (rsComplementarySettlement.next())
	    {
		count++;
		if (!strBillNo.equals(rsComplementarySettlement.getString(1)))
		{
		    if (cnt > 0)
		    {
			pw.println("---------------------------------------");

			pw.println();
		    }
		    pw.println();
		    pw.println("---------------------------------------");
		    funPrintTextWithLeftAlignment("left", "Bill No: " + rsComplementarySettlement.getString(1), 20, pw);
		    funPrintTextWithLeftAlignment("right", "Bill Date:" + rsComplementarySettlement.getString(2), 20, pw);
		    pw.println();
		    funPrintTextWithLeftAlignment("left", "Reason: " + rsComplementarySettlement.getString(7), 20, pw);
		    funPrintTextWithLeftAlignment("right", "Amount:" + rsComplementarySettlement.getString(3), 20, pw);
		    pw.println();
		    pw.println("---------------------------------------");

		    strBillNo = rsComplementarySettlement.getString(1);
		    BillTotal = 0.00;
		}

		pw.println(rsComplementarySettlement.getString(10));
		funPrintTextWithAlignment("right", rsComplementarySettlement.getString(12), 12, pw);
		funPrintTextWithAlignment("right", rsComplementarySettlement.getString(11), 12, pw);
		funPrintTextWithAlignment("right", rsComplementarySettlement.getString(13), 13, pw);
		pw.println();
		quantityTotal += Double.parseDouble(rsComplementarySettlement.getString(11));
		grandTotal += Double.parseDouble(rsComplementarySettlement.getString(13));
		BillTotal += Double.parseDouble(rsComplementarySettlement.getString(13));
		cnt++;

		if (rsComplementarySettlement.isLast())
		{
		    pw.println("---------------------------------------");
//               
		    pw.println();
		}
	    }
	    rsComplementarySettlement.close();

	    pw.println("---------------------------------------");
	    pw.print("GRAND TOTAL");
	    funPrintTextWithAlignment("right", String.valueOf(Math.rint(quantityTotal)), 13, pw);
	    funPrintTextWithAlignment("right", String.valueOf(Math.rint(grandTotal)), 11, pw);
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
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}

	return count;
    }

    public int funGenerateMenuHeadTextReport(File file, String pos, String fromDate, String toDate)
    {
	int count = 0;
	try
	{
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
	    funPrintBlankLines("Menu Head Report", pw);

	    pw.println();
	    pw.println("POS  :" + cmbPosCode.getSelectedItem().toString());
	    pw.println("From :" + funGetCalenderDate("From") + "  To :" + funGetCalenderDate("To"));

	    pw.println("---------------------------------------");
	    //pw.println(" Menu Name POS      qty           Amt   ");
	    pw.println(" Menu Name          qty           Amt   ");
	    pw.println("---------------------------------------");
	    double totalQty = 0.00;
	    double grandTotal = 0.00;

	    String sqlLiveBill = "", sqlFilters = "";
	    sb.setLength(0);
	    if (clsGlobalVarClass.gAreaWisePricing.equals("Y"))
	    {

		sb.append("SELECT  ifnull(d.strMenuCode,'ND'),ifnull(e.strMenuName,'ND'), sum(a.dblQuantity), "
			+ "sum(a.dblAmount)-sum(a.dblDiscountAmt),b.strPoscode,'" + clsGlobalVarClass.gUserCode + "',a.dblRate,'0.00','0.00'  "
			+ "FROM tblqbilldtl a "
			+ "left outer join tblqbillhd b on a.strBillNo=b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate)  "
			+ "left outer join tblmenuitempricingdtl d on a.strItemCode = d.strItemCode "
			+ "and B.strAreaCode= d.strAreaCode  "
			+ "left outer join tblmenuitempricinghd e on d.strMenuCode= e.strMenuCode");

		sqlLiveBill = "SELECT ifnull(d.strMenuCode,'ND'),ifnull(e.strMenuName,'ND'), sum(a.dblQuantity), "
			+ "sum(a.dblAmount)-sum(a.dblDiscountAmt),b.strPoscode,'" + clsGlobalVarClass.gUserCode + "',a.dblRate,'0.00','0.00'  "
			+ "FROM tblbilldtl a "
			+ "left outer join tblbillhd b on a.strBillNo=b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate)  "
			+ "left outer join tblmenuitempricingdtl d on a.strItemCode = d.strItemCode "
			+ "and b.strAreaCode= d.strAreaCode  "
			+ "left outer join tblmenuitempricinghd e on d.strMenuCode= e.strMenuCode";
	    }
	    else
	    {
		sb.append("SELECT  ifnull(d.strMenuCode,'ND'),ifnull(e.strMenuName,'ND'), sum(a.dblQuantity), "
			+ "sum(a.dblAmount)-sum(a.dblDiscountAmt),b.strPoscode,'" + clsGlobalVarClass.gUserCode + "',a.dblRate,'0.00','0.00'  "
			+ "FROM tblqbilldtl a "
			+ "left outer join tblqbillhd b on a.strBillNo=b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate)  "
			+ "left outer join tblmenuitempricingdtl d on a.strItemCode = d.strItemCode "
			+ "and b.strAreaCode= d.strAreaCode  "
			+ "left outer join tblmenuitempricinghd e on d.strMenuCode= e.strMenuCode");

		sqlLiveBill = "SELECT  ifnull(d.strMenuCode,'ND'),ifnull(e.strMenuName,'ND'), sum(a.dblQuantity), "
			+ "sum(a.dblAmount)-sum(a.dblDiscountAmt),b.strPoscode,'" + clsGlobalVarClass.gUserCode + "',a.dblRate,'0.00','0.00'  "
			+ "FROM tblbilldtl a "
			+ "left outer join tblbillhd b on a.strBillNo=b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate)  "
			+ "left outer join tblmenuitempricingdtl d on a.strItemCode = d.strItemCode "
			+ "and b.strAreaCode= d.strAreaCode  "
			+ "left outer join tblmenuitempricinghd e on d.strMenuCode= e.strMenuCode";
	    }
	    if (pos.equals("All"))
	    {
		sqlFilters += "  where date( b.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' ";
	    }
	    else
	    {
		sqlFilters += "  where date( b.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' AND b.strPOSCode = '" + objUtility.funGetPOSCodeFromPOSName(pos) + "' ";
	    }
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
	    if (!shiftNo.equalsIgnoreCase("All"))
	    {
		sqlFilters += " and b.intShiftCode='" + shiftNo + "' ";
	    }

	    sqlFilters += " Group by b.strPoscode, d.strMenuCode,e.strMenuName ";

	    sb.append(sqlFilters);
	    sqlLiveBill += sqlFilters;

	    //System.out.println(selectQuery);
	    clsGlobalVarClass.dbMysql.execute("truncate table tbltempsalesflash");
	    String insertTempSalesFlash = "insert into tbltempsalesflash";
	    clsGlobalVarClass.dbMysql.execute(insertTempSalesFlash + "(" + sb.toString() + ")");
	    clsGlobalVarClass.dbMysql.execute(insertTempSalesFlash + "(" + sqlLiveBill + ")");

	    String sqlMenuHeadWise = "SELECT  strname, strposcode, sum(dblquantity), sum(dblamount) "
		    + "FROM tbltempsalesflash where strUser = '" + clsGlobalVarClass.gUserCode + "' "
		    + "group by strcode, strname, strposcode";

	    ResultSet rsMenuHeadWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlMenuHeadWise);
	    while (rsMenuHeadWiseSales.next())
	    {
		count++;
		//  pw.print(rsMenuHeadWiseSales.getString(1) + "         " + rsMenuHeadWiseSales.getString(2) );
		pw.print(rsMenuHeadWiseSales.getString(1));

		int qtyLen = 26 - (rsMenuHeadWiseSales.getString(1).length());
		funPrintTextWithAlignment("right", rsMenuHeadWiseSales.getString(3), qtyLen, pw);
		funPrintTextWithAlignment("right", rsMenuHeadWiseSales.getString(4), 13, pw);
		pw.println();
		totalQty += Double.parseDouble(rsMenuHeadWiseSales.getString(3));
		grandTotal += Double.parseDouble(rsMenuHeadWiseSales.getString(4));
	    }

	    DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();

	    pw.println("---------------------------------------");
	    pw.print("Total              " + totalQty + "    " + gDecimalFormat.format(grandTotal) + " ");
	    //funPrintTextWithAlignment("right", String.valueOf(Math.rint(grandTotal)), 13, pw);
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
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}

	return count;
    }

    private void funPostingReportForJasperReport()
    {
	String reportType = cmbReportType.getSelectedItem().toString();
	reportName = "com/POSReport/reports/rptPostingReport.jasper";
	HashMap hm = funGetCommonHashMapForJasperReport();

	clsPostingReport objPostingReport = new clsPostingReport();
	objPostingReport.funPostingReport(reportType, hm);

    }

    private void funPostingReportForExcelReport()
    {
	String reportType = cmbReportType.getSelectedItem().toString();
	reportName = "com/POSReport/reports/rptPostingReport.jasper";
	HashMap hm = funGetCommonHashMapForJasperReport();

	clsPostingReport objPostingReport = new clsPostingReport();
	objPostingReport.funPostingReport(reportType, hm);
    }

    private void funPlacedOrderJasperReport()
    {

	String reportType = cmbReportType.getSelectedItem().toString();

	String orderCode = "", orderName = "";
	String orderType = cmbUserName.getSelectedItem().toString();
	if (orderType.equals("Normal"))
	{
	    String orderMaster = cmbSettlementMode.getSelectedItem().toString();
	    orderName = orderMaster.split("                                         ")[0];
	    orderCode = orderMaster.split("                                         ")[1];
	}

	HashMap hm = funGetCommonHashMapForJasperReport();
	hm.put("orderType", orderType);
	hm.put("orderCode", orderCode);
	hm.put("orderName", orderName);

	clsPlacedOrderReport objPlacedOrderReport = new clsPlacedOrderReport();
	objPlacedOrderReport.funPostingReport(reportType, hm);

    }

    private void funCharactoristicWiseAdvanceOrderJasperReport()
    {
	String reportType = cmbReportType.getSelectedItem().toString();

	String orderType = cmbUserName.getSelectedItem().toString();

	HashMap hm = funGetCommonHashMapForJasperReport();
	hm.put("orderType", orderType);

	clsAdvanceOrderReport objAdvanceOrderReport = new clsAdvanceOrderReport();
	objAdvanceOrderReport.funAdvanceOrderReport(reportType, hm);
    }

    private void funVoidAdvanceOrderJasperReport()
    {
	reportName = "com/POSReport/reports/rptVoidAdvanceOrderReport.jasper";

	String orderType = cmbUserName.getSelectedItem().toString();

	HashMap hm = funGetCommonHashMapForJasperReport();
	hm.put("orderType", orderType);

	clsUtilityForAuditReport objUtilityForAuditReport = new clsUtilityForAuditReport(reportName, hm);
	objUtilityForAuditReport.funVoidAdvanceOrderReportForJasper();

//        String reportType = cmbReportType.getSelectedItem().toString();
//
//        String orderType = cmbUserName.getSelectedItem().toString();
//
//        HashMap hm = funGetCommonHashMapForJasperReport();
//        hm.put("orderType", orderType);
//
//        clsVoidAdvanceOrderReport objVoidAdvanceOrderReport = new clsVoidAdvanceOrderReport();
//        objVoidAdvanceOrderReport.funVoidAdvanceOrderReport(reportType,hm);
    }

    private void funRemoveForms()
    {
	clsGlobalVarClass.hmActiveForms.remove("Auditor Report");
	clsGlobalVarClass.hmActiveForms.remove("Bill Wise Report");
	clsGlobalVarClass.hmActiveForms.remove("Complimentary Settlement Report");
	clsGlobalVarClass.hmActiveForms.remove("Counter Wise Sales Report");
	clsGlobalVarClass.hmActiveForms.remove("Discount Report");
	clsGlobalVarClass.hmActiveForms.remove("Group Wise Report");
	clsGlobalVarClass.hmActiveForms.remove("SubGroupWise Report");
	clsGlobalVarClass.hmActiveForms.remove("Group-SubGroup Wise Report");
	clsGlobalVarClass.hmActiveForms.remove("Item Wise Report");
	clsGlobalVarClass.hmActiveForms.remove("Non Chargable KOT Report");
	clsGlobalVarClass.hmActiveForms.remove("OperatorWise Report");
	clsGlobalVarClass.hmActiveForms.remove("Order Analysis Report");
	clsGlobalVarClass.hmActiveForms.remove("SettlementWise Report");
	clsGlobalVarClass.hmActiveForms.remove("Tax Wise Report");
	clsGlobalVarClass.hmActiveForms.remove("Void Bill Report");
	clsGlobalVarClass.hmActiveForms.remove("Tax Breakup Summary Report");
	clsGlobalVarClass.hmActiveForms.remove("Menu Head Wise");
	clsGlobalVarClass.hmActiveForms.remove("WaiterWiseItemReport");
	clsGlobalVarClass.hmActiveForms.remove("WaiterWiseIncentivesReport");
	clsGlobalVarClass.hmActiveForms.remove("DeliveryboyIncentive");
	clsGlobalVarClass.hmActiveForms.remove("Daily Collection Report");
	clsGlobalVarClass.hmActiveForms.remove("Daily Sales Report");
	clsGlobalVarClass.hmActiveForms.remove("Void KOT Report");
	clsGlobalVarClass.hmActiveForms.remove("Guest Credit Report");
	clsGlobalVarClass.hmActiveForms.remove("SubGroupWiseSummaryReport");
	clsGlobalVarClass.hmActiveForms.remove("UnusedCardBalanceReport");
	clsGlobalVarClass.hmActiveForms.remove("Revenue Head Wise Item Sales");
	clsGlobalVarClass.hmActiveForms.remove("Item Wise Consumption");
	clsGlobalVarClass.hmActiveForms.remove("Table Wise Pax Report");
	clsGlobalVarClass.hmActiveForms.remove("Posting Report");
	clsGlobalVarClass.hmActiveForms.remove("Placed Order Report");
	clsGlobalVarClass.hmActiveForms.remove("Advance Order Report");
	clsGlobalVarClass.hmActiveForms.remove("Void Advance Order Report");
	clsGlobalVarClass.hmActiveForms.remove("Waiter Wise Item Wise Incentives Report");
	clsGlobalVarClass.hmActiveForms.remove("Item Master Listing Report");
	clsGlobalVarClass.hmActiveForms.remove("Delivery Boy Wise Cash Taken");
	clsGlobalVarClass.hmActiveForms.remove("Credit Bill Outstanding Report");
	clsGlobalVarClass.hmActiveForms.remove("Food Costing");
	clsGlobalVarClass.hmActiveForms.remove("Blind Settlement Wise Report");
	clsGlobalVarClass.hmActiveForms.remove("Open Item Wise Audit Report");
	clsGlobalVarClass.hmActiveForms.remove("Non Selling Items");
	clsGlobalVarClass.hmActiveForms.remove("Debtors As On");
	clsGlobalVarClass.hmActiveForms.remove("Payment Receipt Report");
	clsGlobalVarClass.hmActiveForms.remove("Credit Report");
	clsGlobalVarClass.hmActiveForms.remove("Consolidated Discount Report");
	clsGlobalVarClass.hmActiveForms.remove("Area Wise Group Wise Sales");

    }

    private HashMap funGetCommonHashMapForJasperReport()
    {
	HashMap hm = new HashMap();

	fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());
	toDate = objUtility.funGetFromToDate(dteToDate.getDate());
	String posName = cmbPosCode.getSelectedItem().toString();
	String posCode = objUtility.funGetPOSCodeFromPOSName(posName);

	SimpleDateFormat ddmmyyyyDateFormat = new SimpleDateFormat("dd-MM-yyyy");
	Date fDate = dteFromDate.getDate();
	Date tDate = dteToDate.getDate();

	String fromDateToDisplay = ddmmyyyyDateFormat.format(fDate);
	String toDateToDisplay = ddmmyyyyDateFormat.format(tDate);

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

	File imgFile = new File(imagePath);
	if (!imgFile.exists())
	{
	    imagePath = getClass().getResource("/com/POSReport/images/imgSanguineLogo.png").toString();
	}
	System.out.println("imagePath=" + imagePath);

	hm.put("posName", posName);
	hm.put("fromDate", fromDate);
	hm.put("toDate", toDate);
	hm.put("userName", clsGlobalVarClass.gUserName);
	hm.put("posCode", posCode);
	hm.put("userCode", userCode);
	hm.put("imagePath", imagePath);
	hm.put("clientName", clsGlobalVarClass.gClientName);
	hm.put("fromDateToDisplay", fromDateToDisplay);
	hm.put("toDateToDisplay", toDateToDisplay);
	hm.put("shiftNo", shiftNo);
	hm.put("shiftCode", shiftCode);

	hm.put("decimalFormaterForDoubleValue", gDecimalFormatString);
	hm.put("decimalFormaterForIntegerValue", "0");

	return hm;
    }

    private void funSalesMenuReportViewMouseClicked()
    {
	Date objFromDate = dteFromDate.getDate();
	Date objToDate = dteToDate.getDate();

	if ((objToDate.getTime() - objFromDate.getTime()) < 0)
	{
	    new frmOkPopUp(this, "Invalid date", "Error", 1).setVisible(true);
	    return;
	}
	else
	{
	    if (cmbReportType.getSelectedItem().toString().equalsIgnoreCase("A4 Size Report"))
	    {
		funGenerateJasperReport();
	    }
	    else if (cmbReportType.getSelectedItem().toString().startsWith("Text File"))
	    {
		funGenerateTextFileReport(salesReportName, cmbReportType.getSelectedItem().toString());
	    }
	    else if (cmbReportType.getSelectedItem().toString().startsWith("Excel Report"))
	    {
		funGenerateExcelSheetOfReport();
	    }
	}
    }

    private void funItemMasterListingReport()
    {
	String reportType = cmbReportType.getSelectedItem().toString();
	HashMap hm = funGetCommonHashMapForJasperReport();

	clsItemMasterListingReport objItemMasterListingReport = new clsItemMasterListingReport();
	objItemMasterListingReport.funItemMasterListingReport(reportType, hm);
    }

    private void funItemMasterListingExcelReport()
    {
	try
	{
	    String reportType = cmbReportType.getSelectedItem().toString();
	    HashMap hm = funGetCommonHashMapForJasperReport();

	    clsItemMasterListingReport objItemMasterListingReport = new clsItemMasterListingReport();
	    objItemMasterListingReport.funItemMasterListingReport(reportType, hm);
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}

    }

    private void funDeliveryBoyWiseCashTakenReport()
    {
	try
	{

	    String reportType = cmbReportType.getSelectedItem().toString();

	    String deliveryBoyName = cmbUserName.getSelectedItem().toString();
	    if (deliveryBoyName.equalsIgnoreCase("All"))
	    {
		dpCode = "All";
	    }
	    else
	    {
		String sqlQuery = "select strDPCode from tbldeliverypersonmaster where strDPName='" + deliveryBoyName + "' ";
		ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(sqlQuery);
		while (rs.next())
		{
		    dpCode = rs.getString(1);
		}
		rs.close();
	    }

	    HashMap hm = funGetCommonHashMapForJasperReport();
	    hm.put("DPName", deliveryBoyName);
	    hm.put("DPCode", dpCode);

	    clsDeliveryBoyWiseCashTakenReport objDeliveryBoyWiseCashTakenReport = new clsDeliveryBoyWiseCashTakenReport();
	    objDeliveryBoyWiseCashTakenReport.funDeliveryBoyWiseCashTakenReport(reportType, hm);
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funCreditBillReport()
    {
	String reportType = cmbReportType.getSelectedItem().toString();

	HashMap hm = funGetCommonHashMapForJasperReport();

	String summaryDetailType = cmbUserName.getSelectedItem().toString();

	hm.put("SummaryDetailType", summaryDetailType);

	clsCreaditBillOutstandingReport objCreaditBillReport = new clsCreaditBillOutstandingReport();
	objCreaditBillReport.funCreaditBillReport(reportType, hm, "No");
    }

    public void funItemWiseConsumptionTextReport(String reportType) throws Exception
    {

	HashMap hm = funGetCommonHashMapForJasperReport();

	String group = cmbUserName.getSelectedItem().toString();
	String printModifiers = cmbSettlementMode.getSelectedItem().toString();
	String costCenter = cmbPaymentMode.getSelectedItem().toString();
	int lastIndex = group.lastIndexOf(" ");

	String groupName = group.substring(0, lastIndex - 1).trim();
	String groupCode = group.substring(lastIndex, group.length()).trim();

	lastIndex = costCenter.lastIndexOf(" ");
	String costCenterName = costCenter.substring(0, lastIndex - 1).trim();
	String costCenterCode = costCenter.substring(lastIndex, costCenter.length()).trim();

	hm.put("GroupCode", groupCode);
	hm.put("GroupName", groupName);
	hm.put("PrintZeroAmountModi", printModifiers);
	hm.put("costCenterCode", costCenterCode);
	hm.put("costCenterName", costCenterName);

	clsItemWiseConsumptionReport objItemWiseConsumptionReport = new clsItemWiseConsumptionReport();
	objItemWiseConsumptionReport.funItemWiseConsumptionReport(reportType, hm, "No");

    }

    private int funSettlementWiseCreditTextReport(String reportType) throws Exception
    {
	//  Map<String,  Map<String, clsManagerReportBean>> mapCCData = new HashMap<String,  Map<String, clsManagerReportBean>>();
	Map<String, List<clsManagerReportBean>> hmSettlementWiseData = new HashMap<String, List<clsManagerReportBean>>();

	List<clsManagerReportBean> listSettlement = new ArrayList<clsManagerReportBean>();
	List<clsManagerReportBean> listMainSettlement = new ArrayList<clsManagerReportBean>();
	Set<String> setBillNo = new HashSet<String>();
	StringBuilder sbSqlLiveFile = new StringBuilder();
	StringBuilder sbSqlOldFile = new StringBuilder();
	StringBuilder sqlFilter = new StringBuilder();

	fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());
	toDate = objUtility.funGetFromToDate(dteToDate.getDate());
	String posName = cmbPosCode.getSelectedItem().toString();

	sbSqlLiveFile.setLength(0);
	sbSqlLiveFile.append(" select c.strSettelmentType,c.strSettelmentDesc,a.strBillNo,date(a.dteBillDate),b.dblSettlementAmt,a.dblTipAmount "
		+ ",ifnull(d.strCustomerCode,''),ifnull(d.strCustomerName,'') "
		+ " from tblbillhd a "
		+ " join tblbillsettlementdtl b on a.strBillNo=b.strBillNo  and date(a.dteBillDate)=date(b.dteBillDate) "
		+ " join tblsettelmenthd c on b.strSettlementCode=c.strSettelmentCode "
		+ " left outer join tblcustomermaster d on a.strCustomerCode=d.strCustomerCode ");
//                + " where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "'  "
//                + " group by c.strSettelmentCode,a.strBillNo "
//                + "order by c.strSettelmentType,c.strSettelmentCode,a.strBillNo ");
	sqlFilter.append("where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	if (!"All".equalsIgnoreCase(posName))
	{
	    sqlFilter.append("and  a.strPosCode='" + objUtility.funGetPOSCodeFromPOSName(posName) + "'  ");
	}
	sqlFilter.append(" group by c.strSettelmentCode,a.strBillNo ");
	sqlFilter.append("order by c.strSettelmentType,c.strSettelmentCode,a.strBillNo ");
	sbSqlLiveFile.append(sqlFilter);
	//  System.out.println(sbSqlLiveFile);
	ResultSet rsSettleLiveManager = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLiveFile.toString());
	while (rsSettleLiveManager.next())
	{
	    String settlementType = rsSettleLiveManager.getString(1);
	    String settlementDesc = rsSettleLiveManager.getString(2);
	    String strBillNo = rsSettleLiveManager.getString(3);
	    String dteBill = rsSettleLiveManager.getString(4);
	    double dblSettlementAmt = rsSettleLiveManager.getDouble(5);
	    double dblTipAmt = rsSettleLiveManager.getDouble(6);
	    String strCustCode = rsSettleLiveManager.getString(7);
	    String strCustName = rsSettleLiveManager.getString(8);

	    clsManagerReportBean objSetlementReportBean = new clsManagerReportBean();
	    objSetlementReportBean.setStrSettlementType(settlementType);
	    objSetlementReportBean.setStrSettlementDesc(settlementDesc);
	    objSetlementReportBean.setStrBillNo(strBillNo);
	    objSetlementReportBean.setDteBill(dteBill);
	    objSetlementReportBean.setDblSettlementAmt(dblSettlementAmt);
	    objSetlementReportBean.setDblTaxAmt(dblTipAmt);
	    objSetlementReportBean.setStrCustCode(strCustCode);
	    objSetlementReportBean.setStrCustName(strCustName);
	    objSetlementReportBean.setDblTipAmt(dblTipAmt);

	    listSettlement.add(objSetlementReportBean);
	    setBillNo.add(strBillNo);
	}
	sqlFilter.setLength(0);
	sbSqlOldFile.setLength(0);
	sbSqlOldFile.append(" select c.strSettelmentType,c.strSettelmentDesc,a.strBillNo,date(a.dteBillDate),b.dblSettlementAmt,a.dblTipAmount "
		+ ",ifnull(d.strCustomerCode,''),ifnull(d.strCustomerName,'') "
		+ " from tblqbillhd a "
		+ " join tblqbillsettlementdtl b on a.strBillNo=b.strBillNo  and date(a.dteBillDate)=date(b.dteBillDate) "
		+ " join tblsettelmenthd c on b.strSettlementCode=c.strSettelmentCode "
		+ " left outer join tblcustomermaster d on a.strCustomerCode=d.strCustomerCode ");
//                + " where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "'  "
//                + " group by c.strSettelmentCode,a.strBillNo "
//                + "order by c.strSettelmentType,c.strSettelmentCode,a.strBillNo ");
	sqlFilter.append("where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	if (!"All".equalsIgnoreCase(posName))
	{
	    sqlFilter.append("and  a.strPosCode='" + objUtility.funGetPOSCodeFromPOSName(posName) + "' ");
	}
	sqlFilter.append(" group by c.strSettelmentCode,a.strBillNo ");
	sqlFilter.append("order by c.strSettelmentType,c.strSettelmentCode,a.strBillNo ");
	sbSqlOldFile.append(sqlFilter);
	ResultSet rsSettleOldManager = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlOldFile.toString());
	while (rsSettleOldManager.next())
	{
	    String settlementType = rsSettleOldManager.getString(1);
	    String settlementDesc = rsSettleOldManager.getString(2);
	    String strBillNo = rsSettleOldManager.getString(3);
	    String dteBill = rsSettleOldManager.getString(4);
	    double dblSettlementAmt = rsSettleOldManager.getDouble(5);
	    double dblTipAmt = rsSettleOldManager.getDouble(6);
	    String strCustCode = rsSettleOldManager.getString(7);
	    String strCustName = rsSettleOldManager.getString(8);

	    clsManagerReportBean objSetlementReportBean = new clsManagerReportBean();
	    objSetlementReportBean.setStrSettlementType(settlementType);
	    objSetlementReportBean.setStrSettlementDesc(settlementDesc);
	    objSetlementReportBean.setStrBillNo(strBillNo);
	    objSetlementReportBean.setDteBill(dteBill);
	    objSetlementReportBean.setDblSettlementAmt(dblSettlementAmt);
	    objSetlementReportBean.setDblTaxAmt(dblTipAmt);
	    objSetlementReportBean.setStrCustCode(strCustCode);
	    objSetlementReportBean.setStrCustName(strCustName);
	    objSetlementReportBean.setDblTipAmt(dblTipAmt);

	    listSettlement.add(objSetlementReportBean);
	    setBillNo.add(strBillNo);

	}

	for (String tempbillNo : setBillNo)
	{
	    boolean flgBillSame = false;
	    List<clsManagerReportBean> listTempSettlement = new ArrayList<clsManagerReportBean>();
	    for (clsManagerReportBean objBean : listSettlement)
	    {
		if (objBean.getStrBillNo().equals(tempbillNo))
		{
		    listTempSettlement.add(objBean);
		    hmSettlementWiseData.put(tempbillNo, listTempSettlement);
		}

	    }

	    if (hmSettlementWiseData.containsKey(tempbillNo))
	    {
		List<clsManagerReportBean> listBillWiseSettlement = hmSettlementWiseData.get(tempbillNo);
		//  Collections.sort(listBillWiseSettlement, (obj1,obj2) -> obj1.getDblTipAmt()>obj2.getDblTipAmt());
		for (clsManagerReportBean objBean : listBillWiseSettlement)
		{
		    if (!flgBillSame)
		    {
			listMainSettlement.add(objBean);
			flgBillSame = true;
		    }
		    else
		    {
			objBean.setDblTipAmt(0.00);
			listMainSettlement.add(objBean);
		    }
		}
	    }
	}
	System.out.println(listMainSettlement);

	Collections.sort(listMainSettlement, new Comparator<clsManagerReportBean>()
	{

	    @Override
	    public int compare(clsManagerReportBean obj1, clsManagerReportBean obj2)
	    {
		return obj1.getStrSettlementType().compareTo(obj2.getStrSettlementType());
	    }

	});

	Set<String> setSettementType = new HashSet<String>();
	Set<String> setSettementDesc = new HashSet<String>();
	Map<String, List<clsManagerReportBean>> hmSettlementDesc = new HashMap<String, List<clsManagerReportBean>>();
	Map<String, Map<String, List<clsManagerReportBean>>> hmSettlementType = new HashMap<String, Map<String, List<clsManagerReportBean>>>();
	Map<String, List<Map<String, List<clsManagerReportBean>>>> hmSettlementTypeMain = new HashMap<String, List<Map<String, List<clsManagerReportBean>>>>();
	for (clsManagerReportBean objtempSettlement : listMainSettlement)
	{
	    setSettementType.add(objtempSettlement.getStrSettlementType());
	    setSettementDesc.add(objtempSettlement.getStrSettlementDesc());
	}

	for (String sttlmDesc : setSettementDesc)
	{
	    List<clsManagerReportBean> ltTempBean = new ArrayList<clsManagerReportBean>();
	    for (clsManagerReportBean objtempSettlement : listMainSettlement)
	    {
		if (objtempSettlement.getStrSettlementDesc().equals(sttlmDesc))
		{
		    ltTempBean.add(objtempSettlement);
		    hmSettlementDesc.put(sttlmDesc, ltTempBean);
		}

	    }
	}

	Map<String, Set<Map<String, List<clsManagerReportBean>>>> hmMainStelltype = new HashMap<String, Set<Map<String, List<clsManagerReportBean>>>>();
	for (String sttlmTyp : setSettementType)
	{

	    // List<Map<String, List<clsManagerReportBean>>> listMainSttlmType = new ArrayList<Map<String, List<clsManagerReportBean>>>();
	    Set<Map<String, List<clsManagerReportBean>>> setMainSttlmType = new HashSet<Map<String, List<clsManagerReportBean>>>();
	    for (Map.Entry<String, List<clsManagerReportBean>> entry : hmSettlementDesc.entrySet())
	    {
		List<clsManagerReportBean> ltTempBean = entry.getValue();
		clsManagerReportBean tempbean = ltTempBean.get(0);
		if (tempbean.getStrSettlementType().equals(sttlmTyp))
		{
		    Map<String, List<clsManagerReportBean>> hmtempDesc = new HashMap<String, List<clsManagerReportBean>>();
		    hmtempDesc.put(entry.getKey(), ltTempBean);
		    setMainSttlmType.add(hmtempDesc);
		    hmMainStelltype.put(sttlmTyp, setMainSttlmType);

		}

	    }

	}

	SimpleDateFormat ddmmyyyyDateFormat = new SimpleDateFormat("dd-MM-yyyy");
	Date fDate = dteFromDate.getDate();
	Date tDate = dteToDate.getDate();
	String fromDateToDisplay = ddmmyyyyDateFormat.format(fDate);
	String toDateToDisplay = ddmmyyyyDateFormat.format(tDate);
	objUtility.funCreateTempFolder();
	String filePath = System.getProperty("user.dir");
	File file = new File(filePath + File.separator + "Temp" + File.separator + "SettlmentTypeWiseReport.txt");

	int count = funGenerateSettlmentTypeWiseTextReport(file, posName, fromDateToDisplay, toDateToDisplay, listMainSettlement, setSettementType, setSettementDesc, hmSettlementType, hmMainStelltype);
	if (count > 0)
	{
	    Desktop dt = Desktop.getDesktop();
	    dt.open(file);
	    // funShowTextFile(file, "SettlmentTypeWiseReport");
	}
	else
	{
	    JOptionPane.showMessageDialog(this, "No Records Found");
	}

	return 1;
    }

    public int funGenerateSettlmentTypeWiseTextReport(File file, String pos, String fromDate, String toDate, List<clsManagerReportBean> listMainData, Set<String> setSettementType, Set<String> setSettementDesc, Map<String, Map<String, List<clsManagerReportBean>>> hmSettlementType, Map<String, Set<Map<String, List<clsManagerReportBean>>>> hmMainStelltype)
    {
	int count = 1;
	DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();

	try
	{
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
	    pw.println("Report : Settlement Type Wise Report");
	    pw.println("Reporting Time:" + "  " + fromDate + " " + "To" + " " + toDate);
	    pw.println();
	    pw.println("-------------------------------------------------------------------------------------------------------------------");
	    pw.println();
	    pw.println("Sr.No.    Slip No      Card No.       Card Holder           Expiry       Bill No      Bill Amt      Tip       Total        ");
	    pw.println();
	    pw.println("---------------------------");
	    pw.println();
	    int SrNo = 1;

	    for (Map.Entry<String, Set<Map<String, List<clsManagerReportBean>>>> entryMain : hmMainStelltype.entrySet())
	    {
		double totSttleAmt = 0.00;
		double totSttleTot = 0.00;
		pw.println(objUtility.funPrintTextWithAlignment("Settlement Type: " + entryMain.getKey() + "", 35, "Left"));
		pw.println("---------------------------");
		Set<Map<String, List<clsManagerReportBean>>> listhmDesc = entryMain.getValue();
		Iterator it = listhmDesc.iterator();
		while (it.hasNext())
		{
		    double totDescAmt = 0.00;
		    double totTot = 0.00;
		    Map<String, List<clsManagerReportBean>> hmDesc = (Map<String, List<clsManagerReportBean>>) it.next();
		    for (Map.Entry<String, List<clsManagerReportBean>> entryDesc : hmDesc.entrySet())
		    {
			pw.println(objUtility.funPrintTextWithAlignment("Settlement Desc: " + entryDesc.getKey() + "", 35, "Left"));

			pw.println("---------------------------");

			List<clsManagerReportBean> listObjTemp = entryDesc.getValue();
			for (clsManagerReportBean objtemp : listObjTemp)
			{
			    pw.print(objUtility.funPrintTextWithAlignment(SrNo + "", 10, "Left"));
			    pw.print(objUtility.funPrintTextWithAlignment("" + "", 13, "Left"));
			    pw.print(objUtility.funPrintTextWithAlignment("" + "", 15, "Left"));
			    pw.print(objUtility.funPrintTextWithAlignment(objtemp.getStrCustName() + "", 22, "Left"));
			    pw.print(objUtility.funPrintTextWithAlignment(objtemp.getDteBill() + "", 13, "Left"));
			    pw.print(objUtility.funPrintTextWithAlignment(objtemp.getStrBillNo() + "", 13, "Left"));
			    pw.print(objUtility.funPrintTextWithAlignment(gDecimalFormat.format(objtemp.getDblSettlementAmt()) + "", 13, "Right"));
			    pw.print(objUtility.funPrintTextWithAlignment(gDecimalFormat.format(objtemp.getDblTipAmt()) + "", 10, "Right"));
			    pw.print(objUtility.funPrintTextWithAlignment(gDecimalFormat.format((objtemp.getDblSettlementAmt() + objtemp.getDblTipAmt())) + "", 13, "Right"));
			    pw.println();
			    SrNo++;
			    totDescAmt += objtemp.getDblSettlementAmt();
			    totTot += (objtemp.getDblSettlementAmt() + objtemp.getDblTipAmt());

			}
			pw.println("-------------------------------------------------------------------------------------------------------------------");
			pw.print(objUtility.funPrintTextWithAlignment("Total: " + entryDesc.getKey() + "", 23, "Left"));
			pw.print(objUtility.funPrintTextWithAlignment(gDecimalFormat.format(totDescAmt) + "", 77, "Right"));
			pw.print(objUtility.funPrintTextWithAlignment(gDecimalFormat.format(totTot) + "", 22, "Right"));
			totSttleAmt += totDescAmt;
			totSttleTot += totTot;
		    }
		}
		pw.println();
		pw.println("-------------------------------------------------------------------------------------------------------------------");
		pw.println();
		pw.print(objUtility.funPrintTextWithAlignment("Total: " + entryMain.getKey() + "", 23, "Left"));
		pw.print(objUtility.funPrintTextWithAlignment(gDecimalFormat.format(totSttleAmt) + "", 77, "Right"));
		pw.print(objUtility.funPrintTextWithAlignment(gDecimalFormat.format(totSttleTot) + "", 22, "Right"));
		pw.println();
		pw.println();

	    }

	    pw.flush();
	    pw.close();

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}

	return count;
    }

    private void funBlindSettlementWiseJasperReport()
    {

	String reportType = cmbReportType.getSelectedItem().toString();

	HashMap hm = funGetCommonHashMapForJasperReport();

	clsBlindSettlementWiseReport objBlindSettlementWiseReport = new clsBlindSettlementWiseReport();
	objBlindSettlementWiseReport.funBlindSettlementWiseReport(reportType, hm, "No");

    }

    private void funBlindSettlementWiseExcelSheetOfReport()
    {
	String reportType = cmbReportType.getSelectedItem().toString();

	HashMap hm = funGetCommonHashMapForJasperReport();

	clsBlindSettlementWiseReport objBlindSettlementWiseReport = new clsBlindSettlementWiseReport();
	objBlindSettlementWiseReport.funBlindSettlementWiseReport(reportType, hm, "No");
    }

    private void funCreditBillExcelReport()
    {
	String reportType = cmbReportType.getSelectedItem().toString();

	HashMap hm = funGetCommonHashMapForJasperReport();
	String summaryDetailType = cmbUserName.getSelectedItem().toString();

	hm.put("SummaryDetailType", summaryDetailType);

	clsCreaditBillOutstandingReport objCreaditBillReport = new clsCreaditBillOutstandingReport();
	objCreaditBillReport.funCreaditBillReport(reportType, hm, "No");
    }

    private int funBlindSettlementWiseCreditTextReport(String reportType) throws Exception
    {
	//  Map<String,  Map<String, clsManagerReportBean>> mapCCData = new HashMap<String,  Map<String, clsManagerReportBean>>();
	Map<String, List<clsManagerReportBean>> hmSettlementWiseData = new HashMap<String, List<clsManagerReportBean>>();

	List<clsManagerReportBean> listSettlement = new ArrayList<clsManagerReportBean>();
	List<clsManagerReportBean> listMainSettlement = new ArrayList<clsManagerReportBean>();
	Set<String> setBillNo = new HashSet<String>();
	StringBuilder sbSqlLiveFile = new StringBuilder();
	StringBuilder sbSqlOldFile = new StringBuilder();
	StringBuilder sqlFilter = new StringBuilder();

	fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());
	toDate = objUtility.funGetFromToDate(dteToDate.getDate());
	String posName = cmbPosCode.getSelectedItem().toString();

	sbSqlLiveFile.setLength(0);
	sbSqlLiveFile.append(" select c.strSettelmentType,c.strSettelmentDesc,a.strBillNo,date(a.dteBillDate),b.dblSettlementAmt,a.dblTipAmount "
		+ ",ifnull(d.strCustomerCode,''),ifnull(d.strCustomerName,'') "
		+ " from tblbillhd a "
		+ " join tblbillsettlementdtl b on a.strBillNo=b.strBillNo  and date(a.dteBillDate)=date(b.dteBillDate) "
		+ " join tblsettelmenthd c on b.strSettlementCode=c.strSettelmentCode "
		+ " left outer join tblcustomermaster d on a.strCustomerCode=d.strCustomerCode ");
//                + " where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "'  "
//                + " group by c.strSettelmentCode,a.strBillNo "
//                + "order by c.strSettelmentType,c.strSettelmentCode,a.strBillNo ");
	sqlFilter.append("where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	if (!"All".equalsIgnoreCase(posName))
	{
	    sqlFilter.append("and  a.strPosCode='" + objUtility.funGetPOSCodeFromPOSName(posName) + "'  ");
	}
	sqlFilter.append("AND c.strSettelmentType!='Complementary' AND c.strSettelmentType!='cash' ");
	sqlFilter.append(" group by c.strSettelmentCode,a.strBillNo ");
	sqlFilter.append("order by c.strSettelmentType,c.strSettelmentCode,a.strBillNo ");
	sbSqlLiveFile.append(sqlFilter);
	//  System.out.println(sbSqlLiveFile);
	ResultSet rsSettleLiveManager = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLiveFile.toString());
	while (rsSettleLiveManager.next())
	{
	    String settlementType = rsSettleLiveManager.getString(1);
	    String settlementDesc = rsSettleLiveManager.getString(2);
	    String strBillNo = rsSettleLiveManager.getString(3);
	    String dteBill = rsSettleLiveManager.getString(4);
	    double dblSettlementAmt = rsSettleLiveManager.getDouble(5);
	    double dblTipAmt = rsSettleLiveManager.getDouble(6);
	    String strCustCode = rsSettleLiveManager.getString(7);
	    String strCustName = rsSettleLiveManager.getString(8);

	    clsManagerReportBean objSetlementReportBean = new clsManagerReportBean();
	    objSetlementReportBean.setStrSettlementType(settlementType);
	    objSetlementReportBean.setStrSettlementDesc(settlementDesc);
	    objSetlementReportBean.setStrBillNo(strBillNo);
	    objSetlementReportBean.setDteBill(dteBill);
	    objSetlementReportBean.setDblSettlementAmt(dblSettlementAmt);
	    objSetlementReportBean.setDblTaxAmt(dblTipAmt);
	    objSetlementReportBean.setStrCustCode(strCustCode);
	    objSetlementReportBean.setStrCustName(strCustName);
	    objSetlementReportBean.setDblTipAmt(dblTipAmt);

	    listSettlement.add(objSetlementReportBean);
	    setBillNo.add(strBillNo);
	}
	sqlFilter.setLength(0);
	sbSqlOldFile.setLength(0);
	sbSqlOldFile.append(" select c.strSettelmentType,c.strSettelmentDesc,a.strBillNo,date(a.dteBillDate),b.dblSettlementAmt,a.dblTipAmount "
		+ ",ifnull(d.strCustomerCode,''),ifnull(d.strCustomerName,'') "
		+ " from tblqbillhd a "
		+ " join tblqbillsettlementdtl b on a.strBillNo=b.strBillNo  and date(a.dteBillDate)=date(b.dteBillDate) "
		+ " join tblsettelmenthd c on b.strSettlementCode=c.strSettelmentCode "
		+ " left outer join tblcustomermaster d on a.strCustomerCode=d.strCustomerCode ");
//                + " where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "'  "
//                + " group by c.strSettelmentCode,a.strBillNo "
//                + "order by c.strSettelmentType,c.strSettelmentCode,a.strBillNo ");
	sqlFilter.append("where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	if (!"All".equalsIgnoreCase(posName))
	{
	    sqlFilter.append("and  a.strPosCode='" + objUtility.funGetPOSCodeFromPOSName(posName) + "' ");
	}
	sqlFilter.append(" group by c.strSettelmentCode,a.strBillNo ");
	sqlFilter.append("order by c.strSettelmentType,c.strSettelmentCode,a.strBillNo ");
	sbSqlOldFile.append(sqlFilter);
	ResultSet rsSettleOldManager = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlOldFile.toString());
	while (rsSettleOldManager.next())
	{
	    String settlementType = rsSettleOldManager.getString(1);
	    String settlementDesc = rsSettleOldManager.getString(2);
	    String strBillNo = rsSettleOldManager.getString(3);
	    String dteBill = rsSettleOldManager.getString(4);
	    double dblSettlementAmt = rsSettleOldManager.getDouble(5);
	    double dblTipAmt = rsSettleOldManager.getDouble(6);
	    String strCustCode = rsSettleOldManager.getString(7);
	    String strCustName = rsSettleOldManager.getString(8);

	    clsManagerReportBean objSetlementReportBean = new clsManagerReportBean();
	    objSetlementReportBean.setStrSettlementType(settlementType);
	    objSetlementReportBean.setStrSettlementDesc(settlementDesc);
	    objSetlementReportBean.setStrBillNo(strBillNo);
	    objSetlementReportBean.setDteBill(dteBill);
	    objSetlementReportBean.setDblSettlementAmt(dblSettlementAmt);
	    objSetlementReportBean.setDblTaxAmt(dblTipAmt);
	    objSetlementReportBean.setStrCustCode(strCustCode);
	    objSetlementReportBean.setStrCustName(strCustName);
	    objSetlementReportBean.setDblTipAmt(dblTipAmt);

	    listSettlement.add(objSetlementReportBean);
	    setBillNo.add(strBillNo);

	}

	for (String tempbillNo : setBillNo)
	{
	    boolean flgBillSame = false;
	    List<clsManagerReportBean> listTempSettlement = new ArrayList<clsManagerReportBean>();
	    for (clsManagerReportBean objBean : listSettlement)
	    {
		if (objBean.getStrBillNo().equals(tempbillNo))
		{
		    listTempSettlement.add(objBean);
		    hmSettlementWiseData.put(tempbillNo, listTempSettlement);
		}

	    }

	    if (hmSettlementWiseData.containsKey(tempbillNo))
	    {
		List<clsManagerReportBean> listBillWiseSettlement = hmSettlementWiseData.get(tempbillNo);
		//  Collections.sort(listBillWiseSettlement, (obj1,obj2) -> obj1.getDblTipAmt()>obj2.getDblTipAmt());
		for (clsManagerReportBean objBean : listBillWiseSettlement)
		{
		    if (!flgBillSame)
		    {
			listMainSettlement.add(objBean);
			flgBillSame = true;
		    }
		    else
		    {
			objBean.setDblTipAmt(0.00);
			listMainSettlement.add(objBean);
		    }
		}
	    }
	}
	System.out.println(listMainSettlement);

	Collections.sort(listMainSettlement, new Comparator<clsManagerReportBean>()
	{

	    @Override
	    public int compare(clsManagerReportBean obj1, clsManagerReportBean obj2)
	    {
		return obj1.getStrSettlementType().compareTo(obj2.getStrSettlementType());
	    }

	});

	Set<String> setSettementType = new HashSet<String>();
	Set<String> setSettementDesc = new HashSet<String>();
	Map<String, List<clsManagerReportBean>> hmSettlementDesc = new HashMap<String, List<clsManagerReportBean>>();
	Map<String, Map<String, List<clsManagerReportBean>>> hmSettlementType = new HashMap<String, Map<String, List<clsManagerReportBean>>>();
	Map<String, List<Map<String, List<clsManagerReportBean>>>> hmSettlementTypeMain = new HashMap<String, List<Map<String, List<clsManagerReportBean>>>>();
	for (clsManagerReportBean objtempSettlement : listMainSettlement)
	{
	    setSettementType.add(objtempSettlement.getStrSettlementType());
	    setSettementDesc.add(objtempSettlement.getStrSettlementDesc());
	}

	for (String sttlmDesc : setSettementDesc)
	{
	    List<clsManagerReportBean> ltTempBean = new ArrayList<clsManagerReportBean>();
	    for (clsManagerReportBean objtempSettlement : listMainSettlement)
	    {
		if (objtempSettlement.getStrSettlementDesc().equals(sttlmDesc))
		{
		    ltTempBean.add(objtempSettlement);
		    hmSettlementDesc.put(sttlmDesc, ltTempBean);
		}

	    }
	}

	Map<String, Set<Map<String, List<clsManagerReportBean>>>> hmMainStelltype = new HashMap<String, Set<Map<String, List<clsManagerReportBean>>>>();
	for (String sttlmTyp : setSettementType)
	{

	    // List<Map<String, List<clsManagerReportBean>>> listMainSttlmType = new ArrayList<Map<String, List<clsManagerReportBean>>>();
	    Set<Map<String, List<clsManagerReportBean>>> setMainSttlmType = new HashSet<Map<String, List<clsManagerReportBean>>>();
	    for (Map.Entry<String, List<clsManagerReportBean>> entry : hmSettlementDesc.entrySet())
	    {
		List<clsManagerReportBean> ltTempBean = entry.getValue();
		clsManagerReportBean tempbean = ltTempBean.get(0);
		if (tempbean.getStrSettlementType().equals(sttlmTyp))
		{
		    Map<String, List<clsManagerReportBean>> hmtempDesc = new HashMap<String, List<clsManagerReportBean>>();
		    hmtempDesc.put(entry.getKey(), ltTempBean);
		    setMainSttlmType.add(hmtempDesc);
		    hmMainStelltype.put(sttlmTyp, setMainSttlmType);

		}

	    }

	}

	SimpleDateFormat ddmmyyyyDateFormat = new SimpleDateFormat("dd-MM-yyyy");
	Date fDate = dteFromDate.getDate();
	Date tDate = dteToDate.getDate();
	String fromDateToDisplay = ddmmyyyyDateFormat.format(fDate);
	String toDateToDisplay = ddmmyyyyDateFormat.format(tDate);
	objUtility.funCreateTempFolder();
	String filePath = System.getProperty("user.dir");
	File file = new File(filePath + File.separator + "Temp" + File.separator + "BlindSettlmentTypeWiseReport.txt");

	int count = funGenerateSettlmentTypeWiseTextReport(file, posName, fromDateToDisplay, toDateToDisplay, listMainSettlement, setSettementType, setSettementDesc, hmSettlementType, hmMainStelltype);
	if (count > 0)
	{
	    Desktop dt = Desktop.getDesktop();
	    dt.open(file);
	    // funShowTextFile(file, "SettlmentTypeWiseReport");
	}
	else
	{
	    JOptionPane.showMessageDialog(this, "No Records Found");
	}

	return 1;
    }

    private int funPrintA4BlankLines(String textToPrint, PrintWriter pw, int totSpace)
    {
	pw.println();
	int len = totSpace - textToPrint.length();
	len = len / 2;
	for (int cnt = 0; cnt < len; cnt++)
	{
	    pw.print(" ");
	}
	pw.print(textToPrint);
	return len;
    }

    private void funOpenItemWiseAuditJasperReport()
    {

	String reportType = cmbReportType.getSelectedItem().toString();

	HashMap hm = funGetCommonHashMapForJasperReport();

	clsOpenItemWiseAuditReport objOpenItemWiseAuditReport = new clsOpenItemWiseAuditReport();
	objOpenItemWiseAuditReport.funOpenItemWiseAuditReport(reportType, hm, "No");

    }

    private void funNonSellingItems()
    {
	String reportType = cmbReportType.getSelectedItem().toString();
	HashMap hm = funGetCommonHashMapForJasperReport();

	clsNonSellingItemsReport objNonSellingItemsReport = new clsNonSellingItemsReport();
	objNonSellingItemsReport.funGenerateNonSellingItemReport(reportType, hm, "No");
    }

    private void funSettlementWiseTextFourtyColumn()
    {

	String reportType = cmbReportType.getSelectedItem().toString();

	HashMap hm = funGetCommonHashMapForJasperReport();

	clsSettlementWiseReport objSettlementWiseReport = new clsSettlementWiseReport();
	objSettlementWiseReport.funSettlementWiseReport(reportType, hm, "No");

    }

    private void funDebtorsAsOnReport()
    {
	String reportType = cmbReportType.getSelectedItem().toString();
	HashMap hm = funGetCommonHashMapForJasperReport();

	clsDebtorsAsOnReport objDebtorsAsOnReport = new clsDebtorsAsOnReport();
	objDebtorsAsOnReport.funDebtorsAsOnReport(reportType, hm, "No");

    }

    private void funPaymentReceiptReport()
    {
	SimpleDateFormat ddmmyyyyDateFormat = new SimpleDateFormat("dd-MMM-yyyy");
	Date fDate = dteFromDate.getDate();
	Date tDate = dteToDate.getDate();

	String reportingFromDate = ddmmyyyyDateFormat.format(fDate);
	String reportingToDate = ddmmyyyyDateFormat.format(tDate);
	String reportingdate = reportingFromDate + "  -  " + reportingToDate;
	String reportType = cmbReportType.getSelectedItem().toString();
	HashMap hm = funGetCommonHashMapForJasperReport();
	hm.put("reportingdate", reportingdate);
	clsPaymentReceiptReport objPaymentReceiptReport = new clsPaymentReceiptReport();
	objPaymentReceiptReport.funPaymentReceiptReport(reportType, hm, "No");

    }

    private void funCreditReport()
    {
	SimpleDateFormat ddmmyyyyDateFormat = new SimpleDateFormat("dd-MMM-yyyy");
	Date fDate = dteFromDate.getDate();
	Date tDate = dteToDate.getDate();

	String reportingFromDate = ddmmyyyyDateFormat.format(fDate);
	String reportingToDate = ddmmyyyyDateFormat.format(tDate);
	String reportingdate = reportingFromDate + "  -  " + reportingToDate;
	String reportType = cmbReportType.getSelectedItem().toString();
	HashMap hm = funGetCommonHashMapForJasperReport();
	hm.put("reportingdate", reportingdate);
	clsCreditReport objCreditReport = new clsCreditReport();
	objCreditReport.funCreditReport(reportType, hm, "No");

    }

}
