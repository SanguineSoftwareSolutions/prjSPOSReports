/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.POSReport.view;

import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsPosConfigFile;
import com.POSGlobal.controller.clsUtility;
import com.POSGlobal.view.frmOkPopUp;
import com.POSReport.controller.clsAPCReport;
import com.POSReport.controller.comparator.clsBillComparator;
import com.POSReport.controller.clsBillItemDtlBean;
import com.POSReport.controller.clsKOTAnalysisBean;
import com.POSReport.controller.comparator.clsWaiterWiseAPCComparator;
import com.POSReport.controller.comparator.clsWaiterWiseSalesComparator;
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

/**
 *
 * @author sss11
 */
public class frmAPC extends javax.swing.JFrame
{

    String fromDate, toDate, imagePath;
    private clsUtility objUtility;
    private StringBuilder sb = new StringBuilder();
    List<clsAPCReport> listOfDtl = new LinkedList<clsAPCReport>();
    HashMap hm = new HashMap();
    double dinningAmt = 0.00;

    /**
     * this Function is used for Component initialization
     */
    public frmAPC()
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

	objUtility = new clsUtility();
	clsPosConfigFile pc = new clsPosConfigFile();
	imagePath = System.getProperty("user.dir");
	imagePath = imagePath + File.separator + "ReportImage";
	fillComboBox();
	fillWaiterWiseComboBox();
	funSetFormToInDateChosser();

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

    public void fillWaiterWiseComboBox()
    {
	try
	{

	    cmbWaiterWise.addItem("All");
	    sb.setLength(0);
	    sb.append("select a.strWShortName,a.strWaiterNo  from tblwaitermaster a");
	    ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
	    while (rs.next())
	    {
		cmbWaiterWise.addItem(rs.getString(1) + " " + rs.getString(2));
	    }
	    rs.close();

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    /**
     * this Function is used for Set Form To Date chooser
     */
    private void funSetFormToInDateChosser()
    {

	dteFromDate.setDate(objUtility.funGetDateToSetCalenderDate());
	dteToDate.setDate(objUtility.funGetDateToSetCalenderDate());

    }

    /**
     * This Function Truncate temporary tblatvreport Table
     */
    public void funTruncateTable()
    {
	try
	{
	    clsGlobalVarClass.dbMysql.execute("truncate table tblatvreport");
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    /**
     * This Function Insert update Data In tblatvreport table
     */
    public void funInsertDataForDetailAPC() throws Exception
    {
	funTruncateTable();

	String waiter = cmbWaiterWise.getSelectedItem().toString();
	String waiterCode = "";
	if (cmbWaiterWise.getSelectedItem().toString().equalsIgnoreCase("All"))
	{
	    waiterCode = "All";
	}
	else
	{
	    waiterCode = waiter.split(" ")[1];
	}

	if ((dteToDate.getDate().getTime() - dteFromDate.getDate().getTime()) < 0)
	{
	    new frmOkPopUp(this, "Invalid date", "Error", 1).setVisible(true);
	}
	else
	{

	    fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());

	    toDate = objUtility.funGetFromToDate(dteToDate.getDate());
	}

	StringBuilder sqlLiveNonComplimentaryBuilder = new StringBuilder();
	StringBuilder sqlQNonComplimentaryBuilder = new StringBuilder();
	StringBuilder sqlLiveComplimentaryBuilder = new StringBuilder();
	StringBuilder sqlQComplimentaryBuilder = new StringBuilder();
	StringBuilder sqlFilter = new StringBuilder();

	sqlLiveNonComplimentaryBuilder.append("select a.strPOSCode ,d.strPosName,date(a.dteBillDate) as Date,a.strBillNo,"
		+ "a.dblDiscountAmt as Discount,a.dblSubTotal as subTotal,sum(intBillSeriesPaxNo), a.dblSubTotal-a.dblDiscountAmt as netTotal,"
		+ " b.dblSettlementAmt as grandTotal,'0',e.strWShortName "
		+ "from tblbillhd a,tblbillsettlementdtl b,tblsettelmenthd c,tblposmaster d,tblwaitermaster e "
		+ "where Date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		+ "and a.strPOSCode=d.strPosCode "
		+ "and a.strBillNo=b.strBillNo "
		+ "and b.strSettlementCode=c.strSettelmentCode "
		+ "and a.strOperationType='DineIn' "
		+ "and date(a.dteBillDate)=date(b.dteBillDate) "
		+ "and c.strSettelmentType<>'Complementary' "
		+ "and a.strWaiterNo = e.strWaiterNo "
		+ " and a.strSettelmentMode!='MultiSettle' ");
	if (!cmbPosCode.getSelectedItem().toString().equalsIgnoreCase("All"))
	{
	    sqlLiveNonComplimentaryBuilder.append("and a.strPOSCode='" + objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString().trim()) + "' ");
	}
	sqlLiveNonComplimentaryBuilder.append(" group by a.strPOSCode,date(a.dteBillDate),a.strBillNo ");

	sqlQNonComplimentaryBuilder.append("select a.strPOSCode ,d.strPosName,date(a.dteBillDate) as Date,a.strBillNo,"
		+ "a.dblDiscountAmt as Discount,a.dblSubTotal as subTotal,sum(intBillSeriesPaxNo), a.dblSubTotal-a.dblDiscountAmt as netTotal,"
		+ " b.dblSettlementAmt as grandTotal,'0',e.strWShortName "
		+ "from tblqbillhd a,tblqbillsettlementdtl b,tblsettelmenthd c,tblposmaster d,tblwaitermaster e "
		+ "where Date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		+ "and a.strPOSCode=d.strPosCode "
		+ "and a.strBillNo=b.strBillNo "
		+ "and b.strSettlementCode=c.strSettelmentCode "
		+ "and a.strOperationType='DineIn' "
		+ "and date(a.dteBillDate)=date(b.dteBillDate) "
		+ "and c.strSettelmentType<>'Complementary' "
		+ "and a.strWaiterNo = e.strWaiterNo "
		+ "  and a.strSettelmentMode!='MultiSettle'  ");
	if (!cmbPosCode.getSelectedItem().toString().equalsIgnoreCase("All"))
	{
	    sqlQNonComplimentaryBuilder.append("and a.strPOSCode='" + objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString().trim()) + "' ");
	}
	sqlQNonComplimentaryBuilder.append(" group by a.strPOSCode,date(a.dteBillDate),a.strBillNo ");

	sqlLiveComplimentaryBuilder.append("select a.strPOSCode ,d.strPosName,date(a.dteBillDate) as Date,a.strBillNo,"
		+ "a.dblDiscountAmt as Discount,a.dblSubTotal as subTotal,sum(intBillSeriesPaxNo), a.dblSubTotal-a.dblDiscountAmt as netTotal,"
		+ " b.dblSettlementAmt as grandTotal,'0',e.strWShortName "
		+ "from tblbillhd a,tblbillsettlementdtl b,tblsettelmenthd c,tblposmaster d,tblwaitermaster e "
		+ "where Date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		+ "and a.strPOSCode=d.strPosCode "
		+ "and a.strBillNo=b.strBillNo "
		+ "and b.strSettlementCode=c.strSettelmentCode "
		+ "and a.strOperationType='DineIn' "
		+ "and date(a.dteBillDate)=date(b.dteBillDate) "
		+ "and c.strSettelmentType='Complementary' "
		+ "and a.strWaiterNo = e.strWaiterNo ");
	if (!cmbWaiterWise.getSelectedItem().toString().equalsIgnoreCase("All"))
	{

	    sqlLiveComplimentaryBuilder.append(" and a.strWaiterNo='" + waiterCode + "'");
	}
	sqlLiveComplimentaryBuilder.append(" group by a.strPOSCode,date(a.dteBillDate),a.strBillNo ");
//                + "");
	sqlQComplimentaryBuilder.append("select a.strPOSCode ,d.strPosName,date(a.dteBillDate) as Date,a.strBillNo,"
		+ "a.dblDiscountAmt as Discount,a.dblSubTotal as subTotal,sum(intBillSeriesPaxNo), a.dblSubTotal-a.dblDiscountAmt as netTotal,"
		+ " b.dblSettlementAmt as grandTotal,'0',e.strWShortName "
		+ "from tblqbillhd a,tblqbillsettlementdtl b,tblsettelmenthd c,tblposmaster d,tblwaitermaster e "
		+ "where Date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		+ "and a.strPOSCode=d.strPosCode "
		+ "and a.strBillNo=b.strBillNo "
		+ "and b.strSettlementCode=c.strSettelmentCode "
		+ "and a.strOperationType='DineIn' "
		+ "and date(a.dteBillDate)=date(b.dteBillDate) "
		+ "and c.strSettelmentType='Complementary' "
		+ "and a.strWaiterNo = e.strWaiterNo ");
	if (!cmbWaiterWise.getSelectedItem().toString().equalsIgnoreCase("All"))
	{

	    sqlQComplimentaryBuilder.append(" and a.strWaiterNo='" + waiterCode + "'");
	}
	sqlQComplimentaryBuilder.append(" group by a.strPOSCode,date(a.dteBillDate),a.strBillNo ");

	Map<String, clsAPCReport> mapNonComplementaryBillWiseAPCReport = new HashMap<>();
	Map<String, clsAPCReport> mapComplementaryAPCReport = new HashMap<>();

	ResultSet rsNonComplementary = clsGlobalVarClass.dbMysql.executeResultSet(sqlLiveNonComplimentaryBuilder.toString());
	while (rsNonComplementary.next())
	{
	    String pos = cmbPosCode.getSelectedItem().toString();
	    String billDate = rsNonComplementary.getString(3);
	    String billNo = rsNonComplementary.getString(4);
	    String waiterName = rsNonComplementary.getString(11);

	    String key = pos + "!" + billDate + "!" + billNo + "!" + waiterName;

	    if (mapNonComplementaryBillWiseAPCReport.containsKey(key))
	    {
		clsAPCReport objAPCReport = mapNonComplementaryBillWiseAPCReport.get(key);

		objAPCReport.setGrandTotal(objAPCReport.getGrandTotal() + rsNonComplementary.getDouble(9));//net total

		mapNonComplementaryBillWiseAPCReport.put(key, objAPCReport);
	    }
	    else
	    {
		clsAPCReport objAPCReport = new clsAPCReport();

		objAPCReport.setStrPOSCode(rsNonComplementary.getString(1));//posCode
		objAPCReport.setStrPOSName(rsNonComplementary.getString(2));//posName
		objAPCReport.setDteBillDate(rsNonComplementary.getString(3));//date
		objAPCReport.setDblDiscountAmt(rsNonComplementary.getDouble(5));//discount
		objAPCReport.setDblSubTotal(rsNonComplementary.getDouble(6));//subtotal
		objAPCReport.setDblPAXNo(rsNonComplementary.getDouble(7));//PAX
		objAPCReport.setNetTotal(rsNonComplementary.getDouble(8));//net total
		objAPCReport.setGrandTotal(rsNonComplementary.getDouble(9));//grandtotal
		objAPCReport.setStrWaiterName(rsNonComplementary.getString(11));

		mapNonComplementaryBillWiseAPCReport.put(key, objAPCReport);
	    }

	}
	rsNonComplementary.close();

	ResultSet rsQNonComplementary = clsGlobalVarClass.dbMysql.executeResultSet(sqlQNonComplimentaryBuilder.toString());
	while (rsQNonComplementary.next())
	{
	    String pos = cmbPosCode.getSelectedItem().toString();
	    String billDate = rsQNonComplementary.getString(3);
	    String billNo = rsQNonComplementary.getString(4);
	    String waiterName = rsQNonComplementary.getString(11);

	    String key = pos + "!" + billDate + "!" + billNo + "!" + waiterName;

	    if (mapNonComplementaryBillWiseAPCReport.containsKey(key))
	    {
		clsAPCReport objAPCReport = mapNonComplementaryBillWiseAPCReport.get(key);

		objAPCReport.setNetTotal(objAPCReport.getNetTotal() + rsQNonComplementary.getDouble(8));//net total

		mapNonComplementaryBillWiseAPCReport.put(key, objAPCReport);
	    }
	    else
	    {
		clsAPCReport objAPCReport = new clsAPCReport();

		objAPCReport.setStrPOSCode(rsQNonComplementary.getString(1));//posCode
		objAPCReport.setStrPOSName(rsQNonComplementary.getString(2));//posName
		objAPCReport.setDteBillDate(rsQNonComplementary.getString(3));//date
		objAPCReport.setDblDiscountAmt(rsQNonComplementary.getDouble(5));//discount
		objAPCReport.setDblSubTotal(rsQNonComplementary.getDouble(6));//subtotal
		objAPCReport.setDblPAXNo(rsQNonComplementary.getDouble(7));//PAX
		objAPCReport.setNetTotal(rsQNonComplementary.getDouble(8));//net total
		objAPCReport.setGrandTotal(rsQNonComplementary.getDouble(9));//grandtotal
		objAPCReport.setStrWaiterName(rsQNonComplementary.getString(11));

		mapNonComplementaryBillWiseAPCReport.put(key, objAPCReport);
	    }

	}
	rsQNonComplementary.close();

	//for only MultiSettle bills
	sqlLiveNonComplimentaryBuilder.setLength(0);
	sqlLiveNonComplimentaryBuilder.append("SELECT a.strPOSCode,d.strPosName, DATE(a.dteBillDate) AS DATE,a.strBillNo,a.dblDiscountAmt AS Discount,a.dblSubTotal AS subTotal "
		+ ", SUM(intBillSeriesPaxNo), a.dblSubTotal-a.dblDiscountAmt AS netTotal, a.dblGrandTotal AS grandTotal,'0',e.strWShortName "
		+ "FROM tblbillhd a,tblposmaster d,tblwaitermaster e "
		+ "WHERE DATE(a.dteBillDate) BETWEEN '" + fromDate + "' and '" + toDate + "'  "
		+ "AND a.strPOSCode=d.strPosCode  "
		+ "AND a.strOperationType='DineIn'  "
		+ "AND a.strWaiterNo = e.strWaiterNo "
		+ "and a.strSettelmentMode='MultiSettle' ");
	if (!cmbPosCode.getSelectedItem().toString().equalsIgnoreCase("All"))
	{
	    sqlLiveNonComplimentaryBuilder.append("and a.strPOSCode='" + objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString().trim()) + "' ");
	}
	sqlLiveNonComplimentaryBuilder.append(" group by a.strPOSCode,date(a.dteBillDate),a.strBillNo ");
	rsNonComplementary = clsGlobalVarClass.dbMysql.executeResultSet(sqlLiveNonComplimentaryBuilder.toString());
	while (rsNonComplementary.next())
	{
	    String pos = cmbPosCode.getSelectedItem().toString();
	    String billDate = rsNonComplementary.getString(3);
	    String billNo = rsNonComplementary.getString(4);
	    String waiterName = rsNonComplementary.getString(11);

	    String key = pos + "!" + billDate + "!" + billNo + "!" + waiterName;

	    if (mapNonComplementaryBillWiseAPCReport.containsKey(key))
	    {
		clsAPCReport objAPCReport = mapNonComplementaryBillWiseAPCReport.get(key);

		objAPCReport.setGrandTotal(objAPCReport.getGrandTotal() + rsNonComplementary.getDouble(9));//net total

		mapNonComplementaryBillWiseAPCReport.put(key, objAPCReport);
	    }
	    else
	    {
		clsAPCReport objAPCReport = new clsAPCReport();

		objAPCReport.setStrPOSCode(rsNonComplementary.getString(1));//posCode
		objAPCReport.setStrPOSName(rsNonComplementary.getString(2));//posName
		objAPCReport.setDteBillDate(rsNonComplementary.getString(3));//date
		objAPCReport.setDblDiscountAmt(rsNonComplementary.getDouble(5));//discount
		objAPCReport.setDblSubTotal(rsNonComplementary.getDouble(6));//subtotal
		objAPCReport.setDblPAXNo(rsNonComplementary.getDouble(7));//PAX
		objAPCReport.setNetTotal(rsNonComplementary.getDouble(8));//net total
		objAPCReport.setGrandTotal(rsNonComplementary.getDouble(9));//grandtotal
		objAPCReport.setStrWaiterName(rsNonComplementary.getString(11));

		mapNonComplementaryBillWiseAPCReport.put(key, objAPCReport);
	    }

	}
	rsNonComplementary.close();

	//Q
	sqlQNonComplimentaryBuilder.setLength(0);
	sqlQNonComplimentaryBuilder.append("SELECT a.strPOSCode,d.strPosName, DATE(a.dteBillDate) AS DATE,a.strBillNo,a.dblDiscountAmt AS Discount,a.dblSubTotal AS subTotal "
		+ ", SUM(intBillSeriesPaxNo), a.dblSubTotal-a.dblDiscountAmt AS netTotal, a.dblGrandTotal AS grandTotal,'0',e.strWShortName "
		+ "FROM tblqbillhd a,tblposmaster d,tblwaitermaster e "
		+ "WHERE DATE(a.dteBillDate) BETWEEN '" + fromDate + "' and '" + toDate + "'  "
		+ "AND a.strPOSCode=d.strPosCode  "
		+ "AND a.strOperationType='DineIn'  "
		+ "AND a.strWaiterNo = e.strWaiterNo "
		+ "and a.strSettelmentMode='MultiSettle' ");
	if (!cmbPosCode.getSelectedItem().toString().equalsIgnoreCase("All"))
	{
	    sqlQNonComplimentaryBuilder.append("and a.strPOSCode='" + objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString().trim()) + "' ");
	}
	sqlQNonComplimentaryBuilder.append(" group by a.strPOSCode,date(a.dteBillDate),a.strBillNo ");
	rsQNonComplementary = clsGlobalVarClass.dbMysql.executeResultSet(sqlQNonComplimentaryBuilder.toString());
	while (rsQNonComplementary.next())
	{
	    String pos = cmbPosCode.getSelectedItem().toString();
	    String billDate = rsQNonComplementary.getString(3);
	    String billNo = rsQNonComplementary.getString(4);
	    String waiterName = rsQNonComplementary.getString(11);

	    String key = pos + "!" + billDate + "!" + billNo + "!" + waiterName;

	    if (mapNonComplementaryBillWiseAPCReport.containsKey(key))
	    {
		clsAPCReport objAPCReport = mapNonComplementaryBillWiseAPCReport.get(key);

		objAPCReport.setNetTotal(objAPCReport.getNetTotal() + rsQNonComplementary.getDouble(8));//net total

		mapNonComplementaryBillWiseAPCReport.put(key, objAPCReport);
	    }
	    else
	    {
		clsAPCReport objAPCReport = new clsAPCReport();

		objAPCReport.setStrPOSCode(rsQNonComplementary.getString(1));//posCode
		objAPCReport.setStrPOSName(rsQNonComplementary.getString(2));//posName
		objAPCReport.setDteBillDate(rsQNonComplementary.getString(3));//date
		objAPCReport.setDblDiscountAmt(rsQNonComplementary.getDouble(5));//discount
		objAPCReport.setDblSubTotal(rsQNonComplementary.getDouble(6));//subtotal
		objAPCReport.setDblPAXNo(rsQNonComplementary.getDouble(7));//PAX
		objAPCReport.setNetTotal(rsQNonComplementary.getDouble(8));//net total
		objAPCReport.setGrandTotal(rsQNonComplementary.getDouble(9));//grandtotal
		objAPCReport.setStrWaiterName(rsQNonComplementary.getString(11));

		mapNonComplementaryBillWiseAPCReport.put(key, objAPCReport);
	    }

	}
	rsQNonComplementary.close();

	//for complementary sales        
	ResultSet rsComplementary = clsGlobalVarClass.dbMysql.executeResultSet(sqlLiveComplimentaryBuilder.toString());
	while (rsComplementary.next())
	{
	    String pos = cmbPosCode.getSelectedItem().toString();
	    String billDate = rsComplementary.getString(3);
	    String billNo = rsComplementary.getString(4);
	    String waiterName = rsComplementary.getString(11);
	    double dblPaxNo = rsComplementary.getDouble(7);

	    String key = pos + "!" + billDate + "!" + waiterName;

	    if (mapComplementaryAPCReport.containsKey(key))
	    {
		clsAPCReport objAPCReport = mapComplementaryAPCReport.get(key);

		objAPCReport.setDblPAXNo(objAPCReport.getDblPAXNo() + dblPaxNo);//PAX No

		mapComplementaryAPCReport.put(key, objAPCReport);
	    }
	    else
	    {
		clsAPCReport objAPCReport = new clsAPCReport();

		objAPCReport.setStrPOSCode(rsComplementary.getString(1));//posCode
		objAPCReport.setStrPOSName(rsComplementary.getString(2));//posName
		objAPCReport.setDteBillDate(rsComplementary.getString(3));//date
		objAPCReport.setDblDiscountAmt(rsComplementary.getDouble(5));//discount
		objAPCReport.setDblSubTotal(rsComplementary.getDouble(6));//subtotal
		objAPCReport.setDblPAXNo(dblPaxNo);//PAX
		objAPCReport.setNetTotal(rsComplementary.getDouble(8));//net total
		objAPCReport.setGrandTotal(rsComplementary.getDouble(9));//grandtotal
		objAPCReport.setStrWaiterName(rsComplementary.getString(11));

		mapComplementaryAPCReport.put(key, objAPCReport);
	    }

	}
	rsComplementary.close();

	rsComplementary = clsGlobalVarClass.dbMysql.executeResultSet(sqlQComplimentaryBuilder.toString());
	while (rsComplementary.next())
	{
	    String pos = cmbPosCode.getSelectedItem().toString();
	    String billDate = rsComplementary.getString(3);
	    String billNo = rsComplementary.getString(4);
	    String waiterName = rsComplementary.getString(11);
	    double dblPaxNo = rsComplementary.getDouble(7);

	    String key = pos + "!" + billDate + "!" + waiterName;

	    if (mapComplementaryAPCReport.containsKey(key))
	    {
		clsAPCReport objAPCReport = mapComplementaryAPCReport.get(key);

		objAPCReport.setDblPAXNo(objAPCReport.getDblPAXNo() + dblPaxNo);//PAX No

		mapComplementaryAPCReport.put(key, objAPCReport);
	    }
	    else
	    {
		clsAPCReport objAPCReport = new clsAPCReport();

		objAPCReport.setStrPOSCode(rsComplementary.getString(1));//posCode
		objAPCReport.setStrPOSName(rsComplementary.getString(2));//posName
		objAPCReport.setDteBillDate(rsComplementary.getString(3));//date
		objAPCReport.setDblDiscountAmt(rsComplementary.getDouble(5));//discount
		objAPCReport.setDblSubTotal(rsComplementary.getDouble(6));//subtotal
		objAPCReport.setDblPAXNo(dblPaxNo);//PAX
		objAPCReport.setNetTotal(rsComplementary.getDouble(8));//net total
		objAPCReport.setGrandTotal(rsComplementary.getDouble(9));//grandtotal
		objAPCReport.setStrWaiterName(rsComplementary.getString(11));

		mapComplementaryAPCReport.put(key, objAPCReport);
	    }

	}
	rsComplementary.close();

	//truncate
	clsGlobalVarClass.dbMysql.execute("truncate tblatvreport");

	Map<String, clsAPCReport> mapNonComplementaryWaiterWiseAPCReport = new HashMap<>();
	for (clsAPCReport objBillWiseAPCReport : mapNonComplementaryBillWiseAPCReport.values())
	{
	    String pos = objBillWiseAPCReport.getStrPOSCode();
	    String billDate = objBillWiseAPCReport.getDteBillDate();
	    String waiterName = objBillWiseAPCReport.getStrWaiterName();
	    double dblPaxNo = objBillWiseAPCReport.getDblPAXNo();
	    double netTotal = objBillWiseAPCReport.getNetTotal();

	    String key = pos + "!" + billDate + "!" + waiterName;

	    if (mapNonComplementaryWaiterWiseAPCReport.containsKey(key))
	    {
		clsAPCReport objWaiterWiseAPCReport = mapNonComplementaryWaiterWiseAPCReport.get(key);

		objWaiterWiseAPCReport.setNetTotal(objWaiterWiseAPCReport.getNetTotal() + netTotal);//net total
		objWaiterWiseAPCReport.setDblPAXNo(objWaiterWiseAPCReport.getDblPAXNo() + dblPaxNo);//PAX No

		mapNonComplementaryWaiterWiseAPCReport.put(key, objWaiterWiseAPCReport);
	    }
	    else
	    {
		mapNonComplementaryWaiterWiseAPCReport.put(key, objBillWiseAPCReport);
	    }
	}

	for (clsAPCReport objAPCReport : mapNonComplementaryWaiterWiseAPCReport.values())
	{
	    //insert non complimentary sales
	    clsGlobalVarClass.dbMysql.execute("Insert into tblatvreport "
		    + "(strPosCode,strPosName,dteDate,dblDiningAmt,dblDiningNoBill,dblHDNoBill,strWaiterName) "
		    + "values('" + objAPCReport.getStrPOSCode() + "','" + objAPCReport.getStrPOSName() + "','" + objAPCReport.getDteBillDate() + "'"
		    + ",'" + objAPCReport.getNetTotal() + "','" + objAPCReport.getDblPAXNo() + "','0','" + objAPCReport.getStrWaiterName() + "') ");
	}

	//complimenary
	for (clsAPCReport objCompliAPC : mapComplementaryAPCReport.values())
	{

	    //insert non complimentary sales
	    clsGlobalVarClass.dbMysql.execute("Insert into tblatvreport "
		    + "(strPosCode,strPosName,dteDate,dblDiningAmt,dblDiningNoBill,dblHDNoBill,strWaiterName,dblDiningAvg) "
		    + "values('" + objCompliAPC.getStrPOSCode() + "','" + objCompliAPC.getStrPOSName() + "','" + objCompliAPC.getDteBillDate() + "'"
		    + ",'0.00','0','" + objCompliAPC.getDblPAXNo() + "','" + objCompliAPC.getStrWaiterName() + "','0.00') ");
	}

	clsGlobalVarClass.dbMysql.execute("update tblatvreport set dblDiningAvg=  dblDiningAmt/dblDiningNoBill");
	clsGlobalVarClass.dbMysql.execute("update tblatvreport  "
		+ "set dblDiningAvg=0 "
		+ "where dblDiningAvg is null;");

	StringBuilder sqlTempTbl = new StringBuilder();

	sqlTempTbl.append("SELECT\n"
		+ "     a.`strPosCode` AS strPosCode,\n"
		+ "     DATE_FORMAT(date(a.`dteDate`),'%d-%m-%Y') AS dteDate, "
		+ "     sum(a.`dblDiningAmt`) AS dblDiningAmt,\n"
		+ "     sum(a.`dblDiningNoBill`) AS dblDiningNoBill,\n"
		+ "     sum(a.`dblDiningAvg`) AS dblDiningAvg,\n"
		+ "     sum(a.`dblHDAmt`) AS dblHDAmt,\n"
		+ "     sum(a.`dblHDNoBill`) AS dblHDNoBill,\n"
		+ "     a.`dblHdAvg` AS dblHdAvg,\n"
		+ "     a.`dblTAAmt` AS dblTAAmt,\n"
		+ "     a.`dblTANoBill` AS dblTANoBill,\n"
		+ "     a.`dblTAAvg` AS dblTAAvg,    \n"
		+ "     a.`strPosName` AS strPosName,\n"
		+ "    a.`strWaiterName` AS strWaiterName\n"
		+ " FROM\n"
		+ "     `tblatvreport` a");

	String waiterName = "";
	if (cmbWaiterWise.getSelectedItem().toString().equalsIgnoreCase("All"))
	{
	    waiterCode = "All";
	}
	else
	{
	    waiterName = waiter.split(" ")[0];
	    waiterCode = waiter.split(" ")[1];
	}

	if (!cmbWaiterWise.getSelectedItem().toString().equalsIgnoreCase("All"))
	{
	    sqlTempTbl.append(" where a.strWaiterName = '" + waiterName + "'");

	}
	sqlTempTbl.append(" group by a.strPosCode,a.dteDate,a.strWaiterName ");
	ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(sqlTempTbl.toString());
	listOfDtl.clear();
	while (rs.next())
	{
	    clsAPCReport objAPCReport = new clsAPCReport();

	    objAPCReport.setStrPOSCode(rs.getString(1));//posCode 
	    objAPCReport.setDteBillDate(rs.getString(2));//posDate 
	    objAPCReport.setNetTotal(rs.getDouble(3));//dinningAmount 
	    objAPCReport.setDblDiningNoBill(rs.getDouble(4));//dinningNoBill 
	    objAPCReport.setDblHDNoBill(rs.getDouble(7));//dinningAvg
	    objAPCReport.setDblDiningAvg(rs.getDouble(5));//hdAmt
	    objAPCReport.setStrPOSName(rs.getString(12));//posName
	    objAPCReport.setStrWaiterName(rs.getString(13));//waiterName
	    dinningAmt = dinningAmt + rs.getDouble(3);

	    listOfDtl.add(objAPCReport);

	}
	rs.close();

	Comparator<clsAPCReport> posComparator = new Comparator<clsAPCReport>()
	{

	    @Override
	    public int compare(clsAPCReport o1, clsAPCReport o2)
	    {
		return o2.getStrPOSName().compareToIgnoreCase(o1.getStrPOSName());
	    }
	};

	Comparator<clsAPCReport> dateComparator = new Comparator<clsAPCReport>()
	{

	    @Override
	    public int compare(clsAPCReport o1, clsAPCReport o2)
	    {
		return o1.getDteBillDate().compareToIgnoreCase(o2.getDteBillDate());
	    }
	};
	Comparator<clsAPCReport> waiterComparator = new Comparator<clsAPCReport>()
	{

	    @Override
	    public int compare(clsAPCReport o1, clsAPCReport o2)
	    {
		return o2.getStrWaiterName().compareToIgnoreCase(o1.getStrWaiterName());
	    }
	};

	Collections.sort(listOfDtl, new clsWaiterWiseAPCComparator(posComparator, dateComparator, waiterComparator));

    }

    /**
     * *
     * This Function Calls Reports
     */
    public void CallReportForDetailAPC() throws Exception
    {
	DecimalFormat df2 = new DecimalFormat("00.00000");
	if ((dteToDate.getDate().getTime() - dteFromDate.getDate().getTime()) < 0)
	{
	    new frmOkPopUp(this, "Invalid date", "Error", 1).setVisible(true);
	}
	else
	{

	    fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());

	    toDate = objUtility.funGetFromToDate(dteToDate.getDate());

	    try
	    {
		com.mysql.jdbc.Connection con = null;
		try
		{
		    Class.forName("com.mysql.jdbc.Driver");
		    //con = (com.mysql.jdbc.Connection) DriverManager.getConnection("jdbc:mysql://localhost:3306/jpos", "root", "root");
		}
		catch (Exception e)
		{
		    e.printStackTrace();
		}

		String reportName = "com/POSReport/reports/rptAPCDetail.jasper";

		InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

		String pos = cmbPosCode.getSelectedItem().toString();
		String posCode = "All";
		String posName = "All";
		if (pos.equalsIgnoreCase("All"))
		{
		    posCode = "All";
		    posName = "All";
		}
		else
		{
		    int lastIndex = pos.lastIndexOf(" ");
		    posName = pos.substring(0, lastIndex);
		    posCode = pos.substring(lastIndex + 1, pos.length());
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
		System.out.println("imagePath=" + imagePath);

		hm.put("FromDate", fromDate);
		hm.put("ToDate", toDate);
		hm.put("strUserName", clsGlobalVarClass.gUserName.toUpperCase());
		hm.put("strImagePath", imagePath);

		SimpleDateFormat ddmmyyyyDateFormat = new SimpleDateFormat("dd-MM-yyyy");
		Date fDate = dteFromDate.getDate();
		Date tDate = dteToDate.getDate();
		String fromDateToDisplay = ddmmyyyyDateFormat.format(fDate);
		String toDateToDisplay = ddmmyyyyDateFormat.format(tDate);

		hm.put("fromDateToDisplay", fromDateToDisplay);
		hm.put("toDateToDisplay", toDateToDisplay);
		String shiftNo = "All", shiftCode = "All";

		hm.put("shiftNo", shiftNo);
		hm.put("shiftCode", shiftCode);
		hm.put("posName", cmbPosCode.getSelectedItem().toString());
		if (cmbWaiterWise.getSelectedItem().toString().equalsIgnoreCase("Yes"))
		{
		    hm.put("waiter", "Waiter Name");
		}

		hm.put("clientCode", clsGlobalVarClass.gClientCode);
		hm.put("clientName", clsGlobalVarClass.gClientName);
		hm.put("address1", clsGlobalVarClass.gClientAddress1);
		hm.put("address3", clsGlobalVarClass.gClientAddress3);
		hm.put("dinningAmt", dinningAmt);

		hm.put("decimalFormaterForDoubleValue", "0.00");
		StringBuilder decimalFormatBuilderForDoubleValue = new StringBuilder("0");
		for (int i = 0; i < clsGlobalVarClass.gNoOfDecimalPlace; i++)
		{
		    if (i == 0)
		    {
			decimalFormatBuilderForDoubleValue.append(".0");
		    }
		    else
		    {
			decimalFormatBuilderForDoubleValue.append("0");
		    }
		}
		hm.put("decimalFormaterForDoubleValue", decimalFormatBuilderForDoubleValue.toString());
		hm.put("decimalFormaterForIntegerValue", "0");

		JRBeanCollectionDataSource beanCollectionDataSource = new JRBeanCollectionDataSource(listOfDtl);
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
	    catch (Exception e)
	    {
		if (e.getMessage().startsWith("Byte data not found at"))
		{
		    JOptionPane.showMessageDialog(null, "Report Image Not Found!!!\nPlease Check Property Setup Report Image.", "Error Code: RIMG-1", JOptionPane.ERROR_MESSAGE);
		}
		e.printStackTrace();
	    }
	}

    }

    private void funGenerateExcelSheetOfReport() throws Exception
    {
	Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
	List<String> arrListTotal = new ArrayList<String>();
	List<String> arrHeaderList = new ArrayList<String>();
	fromDate = dteFromDate.getDate().getDate() + "-" + (dteFromDate.getDate().getMonth() + 1) + "-" + (dteFromDate.getDate().getYear() + 1900);
	toDate = dteToDate.getDate().getDate() + "-" + (dteToDate.getDate().getMonth() + 1) + "-" + (dteToDate.getDate().getYear() + 1900);
	String[] posName = cmbPosCode.getSelectedItem().toString().split(" ");
	double totalDiningAmt = 0;
	double totalDiningAvg = 0;
	double totalDiningNoOfBill = 0;
	double totalCompliPAX = 0;
	DecimalFormat decimalFormat2Decimal = new DecimalFormat("0.00");
	DecimalFormat decimalFormat0Decimal = new DecimalFormat("0");
	SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

	sb.setLength(0);
	sb.append(" SELECT tblatvreport.`strPosCode` AS tblatvreport_strPosCode, "
		+ " DATE_FORMAT(tblatvreport.`dteDate`,'%d-%m-%Y') AS tblatvreport_dteDate,  "
		+ " ifnull(tblatvreport.`dblDiningAmt`,00) AS tblatvreport_dblDiningAmt, "
		+ " ifnull( tblatvreport.`dblDiningNoBill`,00) AS tblatvreport_dblDiningNoBill,  "
		+ " ifnull(tblatvreport.`dblDiningAvg`,00) AS tblatvreport_dblDiningAvg, "
		+ " ifnull( tblatvreport.`dblHDAmt`,00) AS tblatvreport_dblHDAmt, "
		+ " ifnull( tblatvreport.`dblHDNoBill`,00) AS tblatvreport_dblHDNoBill, "
		+ " ifnull( tblatvreport.`dblHdAvg`,00) AS tblatvreport_dblHdAvg, "
		+ " ifnull(tblatvreport.`dblTAAmt`,00) AS tblatvreport_dblTAAmt, "
		+ " ifnull(tblatvreport.`dblTANoBill`,00) AS tblatvreport_dblTANoBill, "
		+ " ifnull(tblatvreport.`dblTAAvg`,00) AS tblatvreport_dblTAAvg, "
		+ " tblatvreport.`strPosName` AS tblatvreport_strPosName, "
		+ " tblatvreport.`strWaiterName` AS tblatvreport_strWaiterName "
		+ "  FROM `tblatvreport` tblatvreport "
		+ "order by tblatvreport_dteDate asc");

	ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
	int i = 1;
	while (rs.next())
	{
	    List<String> arrListItem = new ArrayList<String>();
	    arrListItem.add(rs.getString(1));
	    arrListItem.add(rs.getString(12));
	    arrListItem.add(rs.getString(2));
	    arrListItem.add(decimalFormat0Decimal.format(rs.getDouble(4)));
	    arrListItem.add(decimalFormat0Decimal.format(rs.getDouble(7)));
	    arrListItem.add(rs.getString(3));
	    arrListItem.add(rs.getString(5));
	    if (cmbWaiterWise.getSelectedItem().toString().equalsIgnoreCase("Yes"))
	    {
		arrListItem.add(rs.getString(13));
	    }
	    totalDiningAmt = totalDiningAmt + Double.parseDouble(rs.getString(3));
	    totalDiningAvg = totalDiningAvg + Double.parseDouble(rs.getString(5));
	    totalDiningNoOfBill = totalDiningNoOfBill + Double.parseDouble(rs.getString(4));
	    totalCompliPAX = totalCompliPAX + Double.parseDouble(rs.getString(7));

	    mapExcelItemDtl.put(i, arrListItem);

	    i++;

	}

	arrListTotal.add(decimalFormat0Decimal.format(totalDiningNoOfBill) + "#" + "4");
	arrListTotal.add(decimalFormat0Decimal.format(totalCompliPAX) + "#" + "5");
	arrListTotal.add(decimalFormat2Decimal.format(totalDiningAmt) + "#" + "6");
	arrListTotal.add("" + "#" + "7");

	arrHeaderList.add("Serial No");
	arrHeaderList.add("POS Code");
	arrHeaderList.add("POS Name");
	arrHeaderList.add("BillDate");
	arrHeaderList.add("Sale PAX");
	arrHeaderList.add("Complimentary PAX");
	arrHeaderList.add("Dining Amt");
	arrHeaderList.add("Dining Avg");
	if (cmbWaiterWise.getSelectedItem().toString().equalsIgnoreCase("Yes"))
	{
	    arrHeaderList.add("Waiter Name");
	}

	String fromDateToDisplay = dateFormat.format(dteFromDate.getDate());
	String toDateToDisplay = dateFormat.format(dteToDate.getDate());

	List<String> arrparameterList = new ArrayList<String>();
	arrparameterList.add("Average Per Cover Report");
	arrparameterList.add("POS" + " : " + posName[0]);
	arrparameterList.add("From Date" + " : " + fromDateToDisplay);
	arrparameterList.add("To Date" + " : " + toDateToDisplay);
	arrparameterList.add(" ");
	arrparameterList.add(" ");

	objUtility.funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "APCExcelSheet");

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
        pnlbackground = new JPanel()
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
        pnlAPC = new javax.swing.JPanel();
        lblposCode = new javax.swing.JLabel();
        cmbPosCode = new javax.swing.JComboBox();
        lblFromDate = new javax.swing.JLabel();
        dteFromDate = new com.toedter.calendar.JDateChooser();
        dteToDate = new com.toedter.calendar.JDateChooser();
        lblToDate = new javax.swing.JLabel();
        lblPosWise = new javax.swing.JLabel();
        cmbPosWise = new javax.swing.JComboBox();
        lblDateWise = new javax.swing.JLabel();
        cmbDateWise = new javax.swing.JComboBox();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        lblAPC = new javax.swing.JLabel();
        lblReportType = new javax.swing.JLabel();
        cmbReportType = new javax.swing.JComboBox();
        cmbWaiterWise = new javax.swing.JComboBox();
        lblWaiterWise = new javax.swing.JLabel();
        lblReportType1 = new javax.swing.JLabel();
        cmbReportMode = new javax.swing.JComboBox();

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

        lblModuleName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblModuleName.setForeground(new java.awt.Color(255, 255, 255));
        pnlheader.add(lblModuleName);

        lblformName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblformName.setForeground(new java.awt.Color(255, 255, 255));
        lblformName.setText("-APC");
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

        pnlbackground.setLayout(new java.awt.GridBagLayout());

        pnlMain.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, new java.awt.Color(204, 204, 204), new java.awt.Color(204, 204, 204), new java.awt.Color(153, 153, 153), new java.awt.Color(153, 153, 153)));
        pnlMain.setMinimumSize(new java.awt.Dimension(800, 570));
        pnlMain.setOpaque(false);

        pnlAPC.setOpaque(false);
        pnlAPC.setLayout(null);

        lblposCode.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblposCode.setText("POS Name :");
        pnlAPC.add(lblposCode);
        lblposCode.setBounds(250, 70, 90, 30);

        cmbPosCode.setToolTipText("Select POS");
        cmbPosCode.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                cmbPosCodeActionPerformed(evt);
            }
        });
        pnlAPC.add(cmbPosCode);
        cmbPosCode.setBounds(340, 70, 150, 30);

        lblFromDate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblFromDate.setText("From Date :");
        pnlAPC.add(lblFromDate);
        lblFromDate.setBounds(250, 120, 90, 29);

        dteFromDate.setToolTipText("Select From Date");
        dteFromDate.setPreferredSize(new java.awt.Dimension(119, 35));
        dteFromDate.addHierarchyListener(new java.awt.event.HierarchyListener()
        {
            public void hierarchyChanged(java.awt.event.HierarchyEvent evt)
            {
                dteFromDateHierarchyChanged(evt);
            }
        });
        dteFromDate.addPropertyChangeListener(new java.beans.PropertyChangeListener()
        {
            public void propertyChange(java.beans.PropertyChangeEvent evt)
            {
                dteFromDatePropertyChange(evt);
            }
        });
        pnlAPC.add(dteFromDate);
        dteFromDate.setBounds(340, 120, 150, 30);

        dteToDate.setToolTipText("Select To Date");
        dteToDate.setPreferredSize(new java.awt.Dimension(119, 35));
        dteToDate.addHierarchyListener(new java.awt.event.HierarchyListener()
        {
            public void hierarchyChanged(java.awt.event.HierarchyEvent evt)
            {
                dteToDateHierarchyChanged(evt);
            }
        });
        dteToDate.addPropertyChangeListener(new java.beans.PropertyChangeListener()
        {
            public void propertyChange(java.beans.PropertyChangeEvent evt)
            {
                dteToDatePropertyChange(evt);
            }
        });
        pnlAPC.add(dteToDate);
        dteToDate.setBounds(340, 170, 150, 30);

        lblToDate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblToDate.setText("To Date :");
        pnlAPC.add(lblToDate);
        lblToDate.setBounds(250, 170, 90, 30);

        lblPosWise.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblPosWise.setText("POS Wise :");
        pnlAPC.add(lblPosWise);
        lblPosWise.setBounds(250, 220, 88, 33);

        cmbPosWise.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        cmbPosWise.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "NO", "YES" }));
        cmbPosWise.setToolTipText("Select POS Wise");
        pnlAPC.add(cmbPosWise);
        cmbPosWise.setBounds(340, 220, 150, 33);

        lblDateWise.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblDateWise.setText("Date Wise :");
        pnlAPC.add(lblDateWise);
        lblDateWise.setBounds(250, 270, 86, 33);

        cmbDateWise.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        cmbDateWise.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "NO", "YES" }));
        cmbDateWise.setToolTipText("Select Date Wise");
        pnlAPC.add(cmbDateWise);
        cmbDateWise.setBounds(340, 270, 150, 33);

        jButton1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        jButton1.setText("VIEW");
        jButton1.setToolTipText("View Report");
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn2.png"))); // NOI18N
        jButton1.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                jButton1MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt)
            {
                jButton1MouseEntered(evt);
            }
        });
        pnlAPC.add(jButton1);
        jButton1.setBounds(510, 500, 96, 41);

        jButton2.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton2.setForeground(new java.awt.Color(255, 255, 255));
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        jButton2.setText("CLOSE");
        jButton2.setToolTipText("Close Window");
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn2.png"))); // NOI18N
        jButton2.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                jButton2MouseClicked(evt);
            }
        });
        jButton2.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton2ActionPerformed(evt);
            }
        });
        pnlAPC.add(jButton2);
        jButton2.setBounds(650, 500, 97, 41);

        lblAPC.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        lblAPC.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblAPC.setText("Average Per Cover Report");
        lblAPC.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        pnlAPC.add(lblAPC);
        lblAPC.setBounds(220, 10, 330, 30);

        lblReportType.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblReportType.setText("Report Type :");
        pnlAPC.add(lblReportType);
        lblReportType.setBounds(250, 370, 86, 33);

        cmbReportType.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        cmbReportType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "A4 Size Report", "Excel Report" }));
        cmbReportType.setToolTipText("Select Date Wise");
        pnlAPC.add(cmbReportType);
        cmbReportType.setBounds(340, 370, 150, 33);

        cmbWaiterWise.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        cmbWaiterWise.setToolTipText("Select WaiterWise");
        pnlAPC.add(cmbWaiterWise);
        cmbWaiterWise.setBounds(340, 320, 150, 33);

        lblWaiterWise.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblWaiterWise.setText("Waiter Wise :");
        pnlAPC.add(lblWaiterWise);
        lblWaiterWise.setBounds(250, 320, 86, 33);

        lblReportType1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblReportType1.setText("Report Mode :");
        pnlAPC.add(lblReportType1);
        lblReportType1.setBounds(250, 430, 86, 33);

        cmbReportMode.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        cmbReportMode.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Summary", "Detail" }));
        cmbReportMode.setToolTipText("Select Date Wise");
        pnlAPC.add(cmbReportMode);
        cmbReportMode.setBounds(340, 430, 150, 33);

        javax.swing.GroupLayout pnlMainLayout = new javax.swing.GroupLayout(pnlMain);
        pnlMain.setLayout(pnlMainLayout);
        pnlMainLayout.setHorizontalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMainLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlAPC, javax.swing.GroupLayout.DEFAULT_SIZE, 796, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlMainLayout.setVerticalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlAPC, javax.swing.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE)
        );

        pnlbackground.add(pnlMain, new java.awt.GridBagConstraints());

        getContentPane().add(pnlbackground, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void dteFromDateHierarchyChanged(java.awt.event.HierarchyEvent evt) {//GEN-FIRST:event_dteFromDateHierarchyChanged
	// TODO add your handling code here:

	//FromDate();
    }//GEN-LAST:event_dteFromDateHierarchyChanged

    private void dteFromDatePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_dteFromDatePropertyChange
	// TODO add your handling code here:
	//FromDate();
    }//GEN-LAST:event_dteFromDatePropertyChange

    private void dteToDateHierarchyChanged(java.awt.event.HierarchyEvent evt) {//GEN-FIRST:event_dteToDateHierarchyChanged
	// TODO add your handling code here:
	//ToDate();
    }//GEN-LAST:event_dteToDateHierarchyChanged

    private void dteToDatePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_dteToDatePropertyChange
	// TODO add your handling code here:
	//ToDate();
    }//GEN-LAST:event_dteToDatePropertyChange
    /**
     * *
     * This Function Is Used Insert Data And Call Reports
     *
     * @param evt
     */
    private void jButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseClicked
	// TODO add your handling code here:

	try
	{
	    if (cmbReportMode.getSelectedItem().toString().equalsIgnoreCase("Summary"))//old format
	    {
		if (cmbReportType.getSelectedItem().toString().equalsIgnoreCase("A4 Size Report"))
		{
		    funInsertDataForSummary();
		    CallReportForSummary();
		}
		else
		{
		    funInsertDataForSummary();
		    funGenerateExcelSheetOfReport();
		}
	    }
	    else//detail new format
	    {
		if (cmbReportType.getSelectedItem().toString().equalsIgnoreCase("A4 Size Report"))
		{
		    funInsertDataForDetailAPC();
		    CallReportForDetailAPC();
		}
		else
		{
		    funInsertDataForDetailAPC();
		    funGenerateExcelSheetOfReport();
		}
	    }

	    listOfDtl.clear();
	    dinningAmt = 0.00;

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }//GEN-LAST:event_jButton1MouseClicked

    private void jButton2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton2MouseClicked
	// TODO add your handling code here:
	dispose();
	clsGlobalVarClass.hmActiveForms.remove("AvgPerCover");
    }//GEN-LAST:event_jButton2MouseClicked

    private void cmbPosCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbPosCodeActionPerformed
	// TODO add your handling code here:

    }//GEN-LAST:event_cmbPosCodeActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("AvgPerCover");
    }//GEN-LAST:event_jButton2ActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("AvgPerCover");
    }//GEN-LAST:event_formWindowClosed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("AvgPerCover");
    }//GEN-LAST:event_formWindowClosing

    private void jButton1MouseEntered(java.awt.event.MouseEvent evt)//GEN-FIRST:event_jButton1MouseEntered
    {//GEN-HEADEREND:event_jButton1MouseEntered
	// TODO add your handling code here:
    }//GEN-LAST:event_jButton1MouseEntered

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
	    java.util.logging.Logger.getLogger(frmAPC.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	}
	catch (InstantiationException ex)
	{
	    java.util.logging.Logger.getLogger(frmAPC.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	}
	catch (IllegalAccessException ex)
	{
	    java.util.logging.Logger.getLogger(frmAPC.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	}
	catch (javax.swing.UnsupportedLookAndFeelException ex)
	{
	    java.util.logging.Logger.getLogger(frmAPC.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	}
	//</editor-fold>
	//</editor-fold>

	/* Create and display the form */
	java.awt.EventQueue.invokeLater(new Runnable()
	{
	    public void run()
	    {
		new frmAPC().setVisible(true);
	    }
	});
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cmbDateWise;
    private javax.swing.JComboBox cmbPosCode;
    private javax.swing.JComboBox cmbPosWise;
    private javax.swing.JComboBox cmbReportMode;
    private javax.swing.JComboBox cmbReportType;
    private javax.swing.JComboBox cmbWaiterWise;
    private com.toedter.calendar.JDateChooser dteFromDate;
    private com.toedter.calendar.JDateChooser dteToDate;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel lblAPC;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblDateWise;
    private javax.swing.JLabel lblFromDate;
    private javax.swing.JLabel lblHOSign;
    private javax.swing.JLabel lblModuleName;
    private javax.swing.JLabel lblPosName;
    private javax.swing.JLabel lblPosWise;
    private javax.swing.JLabel lblProductName;
    private javax.swing.JLabel lblReportType;
    private javax.swing.JLabel lblReportType1;
    private javax.swing.JLabel lblToDate;
    private javax.swing.JLabel lblUserCode;
    private javax.swing.JLabel lblWaiterWise;
    private javax.swing.JLabel lblformName;
    private javax.swing.JLabel lblposCode;
    private javax.swing.JPanel pnlAPC;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JPanel pnlbackground;
    private javax.swing.JPanel pnlheader;
    // End of variables declaration//GEN-END:variables
  /* This Function Insert update Data In tblatvreport table
    *
    String posCode = "a.strPOSCode";
        String posName = "d.strPosName";
        if (cmbPosCode.getSelectedItem().toString().equalsIgnoreCase("All") && cmbPosWise.getSelectedItem().toString().equalsIgnoreCase("No"))
        {
            posCode = "'All'";
            posName = "'All'";
        }
     */

    public void funInsertDataForSummary() throws Exception
    {

	funTruncateTable();

	if ((dteToDate.getDate().getTime() - dteFromDate.getDate().getTime()) < 0)
	{
	    new frmOkPopUp(this, "Invalid date", "Error", 1).setVisible(true);
	}
	else
	{

	    fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());

	    toDate = objUtility.funGetFromToDate(dteToDate.getDate());
	}

	StringBuilder sqlNonComplimentaryBuilder = new StringBuilder();
	StringBuilder sqlComplimentaryBuilder = new StringBuilder();
	StringBuilder sqlFilter = new StringBuilder();

	String posCode = "a.strPOSCode";
	String posName = "d.strPosName";
	if (cmbPosCode.getSelectedItem().toString().equalsIgnoreCase("All") && cmbPosWise.getSelectedItem().toString().equalsIgnoreCase("No"))
	{
	    posCode = "'All'";
	    posName = "'All'";
	}

	//for not multi settle 
	sqlNonComplimentaryBuilder.append("select " + posCode + " ," + posName + ",date(a.dteBillDate) as Date,(sum(a.dblGrandTotal) ) as DiningAmt,sum(intBillSeriesPaxNo),'0' "
		+ "from vqbillhd a,vqbillsettlementdtl b,tblsettelmenthd c,tblposmaster d "
		+ "where Date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		+ "and a.strPOSCode=d.strPosCode "
		+ "and a.strBillNo=b.strBillNo "
		+ "and b.strSettlementCode=c.strSettelmentCode "
		+ "and a.strOperationType='DineIn' "
		+ "and date(a.dteBillDate)=date(b.dteBillDate) "
		+ "and c.strSettelmentType<>'Complementary' "
		+ "and a.strSettelmentMode!='MultiSettle'  ");

	sqlComplimentaryBuilder.append("select " + posCode + " ," + posName + ",date(a.dteBillDate)  as Date,(sum(a.dblGrandTotal) ) as DiningAmt,sum(intBillSeriesPaxNo),'0' "
		+ "from vqbillhd a,vqbillsettlementdtl b,tblsettelmenthd c,tblposmaster d "
		+ "where Date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		+ "and a.strPOSCode=d.strPosCode "
		+ "and a.strBillNo=b.strBillNo "
		+ "and b.strSettlementCode=c.strSettelmentCode "
		+ "and a.strOperationType='DineIn' "
		+ "and date(a.dteBillDate)=date(b.dteBillDate) "
		+ "and c.strSettelmentType='Complementary' "
		+ "");

	if (!cmbPosCode.getSelectedItem().toString().equalsIgnoreCase("All"))
	{
	    sqlFilter.append("and a.strPOSCode='" + objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString().trim()) + "' ");
	}

	if (cmbPosWise.getSelectedItem().toString().equalsIgnoreCase("Yes") || cmbDateWise.getSelectedItem().toString().equalsIgnoreCase("Yes"))
	{
	    if (cmbPosWise.getSelectedItem().toString().equalsIgnoreCase("Yes") && cmbDateWise.getSelectedItem().toString().equalsIgnoreCase("Yes"))
	    {
		sqlFilter.append("group by a.strPOSCode,date(a.dteBillDate) ");
		sqlFilter.append("order by a.strPOSCode,date(a.dteBillDate) ");
	    }
	    else if (cmbPosWise.getSelectedItem().toString().equalsIgnoreCase("Yes"))
	    {
		sqlFilter.append("group by a.strPOSCode ");
		sqlFilter.append("order by a.strPOSCode ");
	    }
	    else if (cmbDateWise.getSelectedItem().toString().equalsIgnoreCase("Yes"))
	    {
		sqlFilter.append("group by date(a.dteBillDate) ");
		sqlFilter.append("order by date(a.dteBillDate) ");
	    }
	}
	else
	{
	    sqlFilter.append(" ");
	}

	sqlNonComplimentaryBuilder.append(sqlFilter);
	sqlComplimentaryBuilder.append(sqlFilter);

	Map<String, clsAPCReport> mapAPCReport = new HashMap<>();
	ResultSet rsNonComplementary = clsGlobalVarClass.dbMysql.executeResultSet(sqlNonComplimentaryBuilder.toString());
	while (rsNonComplementary.next())
	{
	    String key = "";
	    if (cmbPosWise.getSelectedItem().toString().equalsIgnoreCase("Yes") || cmbDateWise.getSelectedItem().toString().equalsIgnoreCase("Yes"))
	    {
		if (cmbPosWise.getSelectedItem().toString().equalsIgnoreCase("Yes") && cmbDateWise.getSelectedItem().toString().equalsIgnoreCase("Yes"))
		{
		    key = rsNonComplementary.getString(1) + "!" + rsNonComplementary.getString(3);//posCode+date
		}
		else if (cmbPosWise.getSelectedItem().toString().equalsIgnoreCase("Yes"))
		{
		    key = rsNonComplementary.getString(1);//posCode
		}
		else if (cmbDateWise.getSelectedItem().toString().equalsIgnoreCase("Yes"))
		{
		    key = rsNonComplementary.getString(3);//date
		}
	    }
	    else
	    {
		sqlFilter.append(" ");
	    }

	    if (mapAPCReport.containsKey(key))
	    {
		clsAPCReport objAPCReport = mapAPCReport.get(key);

		objAPCReport.setDblDiningAmt(objAPCReport.getDblDiningAmt() + rsNonComplementary.getDouble(4));//dining amt
		objAPCReport.setDblPAXNo(objAPCReport.getDblPAXNo() + rsNonComplementary.getDouble(5));//PAX

		mapAPCReport.put(key, objAPCReport);
	    }
	    else
	    {
		clsAPCReport objAPCReport = new clsAPCReport();

		objAPCReport.setStrPOSCode(rsNonComplementary.getString(1));//posCode
		objAPCReport.setStrPOSName(rsNonComplementary.getString(2));//posName
		objAPCReport.setDteBillDate(rsNonComplementary.getString(3));//date
		objAPCReport.setDblDiningAmt(rsNonComplementary.getDouble(4));//dining amt
		objAPCReport.setDblPAXNo(rsNonComplementary.getDouble(5));//PAX

		mapAPCReport.put(key, objAPCReport);
	    }

	}
	rsNonComplementary.close();

	//for multi settle 
	sqlNonComplimentaryBuilder.setLength(0);
	sqlNonComplimentaryBuilder.append("select " + posCode + " ," + posName + ",date(a.dteBillDate) as Date,(sum(a.dblGrandTotal) ) as DiningAmt,sum(intBillSeriesPaxNo),'0' "
		+ "from vqbillhd a,tblposmaster d "
		+ "where Date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		+ "and a.strPOSCode=d.strPosCode "
		+ "and a.strOperationType='DineIn' "
		+ "and a.strSettelmentMode='MultiSettle'  ");
	sqlNonComplimentaryBuilder.append(sqlFilter);

	rsNonComplementary = clsGlobalVarClass.dbMysql.executeResultSet(sqlNonComplimentaryBuilder.toString());
	while (rsNonComplementary.next())
	{
	    String key = "";
	    if (cmbPosWise.getSelectedItem().toString().equalsIgnoreCase("Yes") || cmbDateWise.getSelectedItem().toString().equalsIgnoreCase("Yes"))
	    {
		if (cmbPosWise.getSelectedItem().toString().equalsIgnoreCase("Yes") && cmbDateWise.getSelectedItem().toString().equalsIgnoreCase("Yes"))
		{
		    key = rsNonComplementary.getString(1) + "!" + rsNonComplementary.getString(3);//posCode+date
		}
		else if (cmbPosWise.getSelectedItem().toString().equalsIgnoreCase("Yes"))
		{
		    key = rsNonComplementary.getString(1);//posCode
		}
		else if (cmbDateWise.getSelectedItem().toString().equalsIgnoreCase("Yes"))
		{
		    key = rsNonComplementary.getString(3);//date
		}
	    }
	    else
	    {
		sqlFilter.append(" ");
	    }

	    if (mapAPCReport.containsKey(key))
	    {
		clsAPCReport objAPCReport = mapAPCReport.get(key);

		objAPCReport.setDblDiningAmt(objAPCReport.getDblDiningAmt() + rsNonComplementary.getDouble(4));//dining amt
		objAPCReport.setDblPAXNo(objAPCReport.getDblPAXNo() + rsNonComplementary.getDouble(5));//PAX

		mapAPCReport.put(key, objAPCReport);
	    }
	    else
	    {
		clsAPCReport objAPCReport = new clsAPCReport();

		objAPCReport.setStrPOSCode(rsNonComplementary.getString(1));//posCode
		objAPCReport.setStrPOSName(rsNonComplementary.getString(2));//posName
		objAPCReport.setDteBillDate(rsNonComplementary.getString(3));//date
		objAPCReport.setDblDiningAmt(rsNonComplementary.getDouble(4));//dining amt
		objAPCReport.setDblPAXNo(rsNonComplementary.getDouble(5));//PAX

		mapAPCReport.put(key, objAPCReport);
	    }

	}
	rsNonComplementary.close();

	//truncate
	clsGlobalVarClass.dbMysql.execute("truncate tblatvreport");
	//insert non complimentary sales

	for (clsAPCReport objAPCReport : mapAPCReport.values())
	{
	    //insert non complimentary sales
	    clsGlobalVarClass.dbMysql.execute("Insert into tblatvreport "
		    + "(strPosCode,strPosName,dteDate,dblDiningAmt,dblDiningNoBill,dblHDNoBill) "
		    + "values('" + objAPCReport.getStrPOSCode() + "','" + objAPCReport.getStrPOSName() + "','" + objAPCReport.getDteBillDate() + "','" + objAPCReport.getDblDiningAmt() + "','" + objAPCReport.getDblPAXNo() + "','0') ");
	}

	clsGlobalVarClass.dbMysql.execute("update tblatvreport set dblDiningAvg=  dblDiningAmt/dblDiningNoBill");
	//complimenary
	ResultSet rsComplimentarySales = clsGlobalVarClass.dbMysql.executeResultSet(sqlComplimentaryBuilder.toString());
	while (rsComplimentarySales.next())
	{
	    clsGlobalVarClass.dbMysql.execute("update tblatvreport set dblHDNoBill='" + rsComplimentarySales.getString(5) + "' "
		    + " where strPosCode='" + rsComplimentarySales.getString(1) + "' and dteDate='" + rsComplimentarySales.getString(3) + "'  ");
	}
	rsComplimentarySales.close();
    }

    /**
     * *
     * This Function Calls Reports
     */
    public void CallReportForSummary() throws Exception
    {

	if ((dteToDate.getDate().getTime() - dteFromDate.getDate().getTime()) < 0)
	{
	    new frmOkPopUp(this, "Invalid date", "Error", 1).setVisible(true);
	}
	else
	{

	    fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());

	    toDate = objUtility.funGetFromToDate(dteToDate.getDate());

	    try
	    {
		com.mysql.jdbc.Connection con = null;
		try
		{
		    Class.forName("com.mysql.jdbc.Driver");
		    //con = (com.mysql.jdbc.Connection) DriverManager.getConnection("jdbc:mysql://localhost:3306/jpos", "root", "root");
		}
		catch (Exception e)
		{
		    e.printStackTrace();
		}

		String reportName = "com/POSReport/reports/rptAPCSummary.jasper";

		InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

		String pos = cmbPosCode.getSelectedItem().toString();
		String posCode = "All";
		String posName = "All";
		if (pos.equalsIgnoreCase("All"))
		{
		    posCode = "All";
		    posName = "All";
		}
		else
		{
		    int lastIndex = pos.lastIndexOf(" ");
		    posName = pos.substring(0, lastIndex);
		    posCode = pos.substring(lastIndex + 1, pos.length());
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
		System.out.println("imagePath=" + imagePath);

		HashMap hm = new HashMap();
		hm.put("FromDate", fromDate);
		hm.put("ToDate", toDate);
		hm.put("strUserName", clsGlobalVarClass.gUserName.toUpperCase());
		hm.put("strImagePath", imagePath);

		SimpleDateFormat ddmmyyyyDateFormat = new SimpleDateFormat("dd-MM-yyyy");
		Date fDate = dteFromDate.getDate();
		Date tDate = dteToDate.getDate();
		String fromDateToDisplay = ddmmyyyyDateFormat.format(fDate);
		String toDateToDisplay = ddmmyyyyDateFormat.format(tDate);

		hm.put("fromDateToDisplay", fromDateToDisplay);
		hm.put("toDateToDisplay", toDateToDisplay);
		String shiftNo = "All", shiftCode = "All";

		hm.put("shiftNo", shiftNo);
		hm.put("shiftCode", shiftCode);
		hm.put("posName", cmbPosCode.getSelectedItem().toString());
		hm.put("dateWise", "No");
		if (cmbDateWise.getSelectedItem().toString().equalsIgnoreCase("Yes"))
		{
		    hm.put("dateWise", "Yes");
		}
		hm.put("decimalFormaterForDoubleValue", "0.00");
		StringBuilder decimalFormatBuilderForDoubleValue = new StringBuilder("0");
		for (int i = 0; i < clsGlobalVarClass.gNoOfDecimalPlace; i++)
		{
		    if (i == 0)
		    {
			decimalFormatBuilderForDoubleValue.append(".0");
		    }
		    else
		    {
			decimalFormatBuilderForDoubleValue.append("0");
		    }
		}
		hm.put("decimalFormaterForDoubleValue", decimalFormatBuilderForDoubleValue.toString());
		hm.put("decimalFormaterForIntegerValue", "0");

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
	    catch (Exception e)
	    {
		if (e.getMessage().startsWith("Byte data not found at"))
		{
		    JOptionPane.showMessageDialog(null, "Report Image Not Found!!!\nPlease Check Property Setup Report Image.", "Error Code: RIMG-1", JOptionPane.ERROR_MESSAGE);
		}
		e.printStackTrace();
	    }
	}

    }
}
