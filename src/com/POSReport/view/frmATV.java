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
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.swing.JRViewer;

public class frmATV extends javax.swing.JFrame
{

    String fromDate, toDate, imagePath;
    private clsUtility objUtility;
    private StringBuilder sb = new StringBuilder();

    /**
     * this Function is used for Component initialization
     */
    public frmATV()
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
	funFillComboBox();
	funSetFormToInDateChosser();
	funFillShiftCombo();

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
     * this function is used Filling POS Code ComboBoxs
     */
    private void funFillComboBox()
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

	    cmbPosWise.addItem("No");
	    cmbPosWise.addItem("Yes");
	    cmbDateWise.addItem("No");
	    cmbDateWise.addItem("Yes");
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
    private void funTruncateTable()
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
    private void funInsertData() throws Exception
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

	StringBuilder filter = new StringBuilder();

	if (!cmbPosCode.getSelectedItem().toString().equalsIgnoreCase("All"))
	{
	    filter.append(" and b.strPOSCode='" + objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString().trim()) + "' ");
	}
	if (clsGlobalVarClass.gEnableShiftYN)
	{
	    if (clsGlobalVarClass.gEnableShiftYN && (!cmbShiftNo.getSelectedItem().toString().equalsIgnoreCase("All")))
	    {
		filter.append(" and b.intShiftCode ='" + cmbShiftNo.getSelectedItem().toString() + "' ");
	    }
	}
	sb.setLength(0);
	sb.append("Insert into tblatvreport(strPosCode,strPosName,dteDate,dblDiningAmt)"
		+ " select * from ("
		+ " select  b.strPOSCode,c.strPOSName,b.dteBillDate,sum(dblQuantity) as ItemQty "
		+ " from tblBillDtl a, tblBillHd b,tblposmaster c"
		+ " Where b.strOperationType='DineIn' "
		+ " and  Date(b.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		+ " and a.strBillNo = b.strBillNo "
		+ " and b.strPOSCode=c.strPOSCode");
	sb.append(filter + " group by date(b.dteBillDate),c.strPOSCode");
	sb.append(" UNION ALL "
		+ " select  b.strPOSCode,c.strPOSName,b.dteBillDate,sum(dblQuantity) as ItemQty "
		+ " from tblqBillDtl a, tblqBillHd b,tblposmaster c"
		+ " Where b.strOperationType='DineIn' and  Date(b.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		+ " and a.strBillNo = b.strBillNo "
		+ " and b.strPOSCode=c.strPOSCode");
	sb.append(filter + " group by date(b.dteBillDate),c.strPOSCode");
	sb.append(" ) d");
	clsGlobalVarClass.dbMysql.execute(sb.toString());
	//Dine In  
	sb.setLength(0);
	sb.append("select b.dteBillDate,  b.strPOSCode, Count(*) as NoOfBills,c.strPOSName "
		+ " from tblBillHd b,tblPOSMaster c "
		+ " Where b.strOperationType='DineIn' "
		+ " and  Date(b.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		+ " and b.strPOSCode=c.strPOSCode ");
	sb.append(filter + " group by date(b.dteBillDate),c.strPOSCode");
	sb.append(" UNION ALL ");
	sb.append("select b.dteBillDate,  b.strPOSCode, Count(*) as NoOfBills,c.strPOSName "
		+ " from tblqBillHd b,tblPOSMaster c "
		+ " Where b.strOperationType='DineIn' "
		+ " and  Date(b.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		+ " and b.strPOSCode=c.strPOSCode ");
	sb.append(filter + " group by date(b.dteBillDate),c.strPOSCode");

	ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
	while (rs.next())
	{
	    sb.setLength(0);
	    sb.append("update tblatvreport set dblDiningNoBill='" + rs.getDouble(3) + "' ,strPOSName='" + rs.getString(4) + "' "
		    + "where dteDate = '" + rs.getDate(1) + "'  and strPOSCode ='" + rs.getString(2) + "'");
	    clsGlobalVarClass.dbMysql.execute(sb.toString());
	}
	//Home Delivery
	sb.setLength(0);
	sb.append("select  b.strPOSCode,b.dteBillDate,  sum(dblQuantity) as ItemQty "
		+ " from tblBillDtl a, tblBillHd b,tblPOSMaster c"
		+ " Where b.strOperationType='HomeDelivery' "
		+ " and  Date(b.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		+ " and a.strBillNo = b.strBillNo "
		+ " and b.strPOSCode=c.strPOSCode ");
	sb.append(filter + " group by date(b.dteBillDate),c.strPOSCode");
	sb.append(" UNION ALL ");
	sb.append("select  b.strPOSCode,b.dteBillDate,  sum(dblQuantity) as ItemQty "
		+ " from tblqBillDtl a, tblqBillHd b,tblPOSMaster c"
		+ " Where b.strOperationType='HomeDelivery' "
		+ " and  Date(b.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		+ " and a.strBillNo = b.strBillNo "
		+ " and b.strPOSCode=c.strPOSCode ");
	sb.append(filter + " group by date(b.dteBillDate),c.strPOSCode");

	rs = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
	while (rs.next())
	{
	    sb.setLength(0);
	    sb.append("update tblatvreport set dblHDAmt='" + rs.getDouble(3) + "' "
		    + " where dteDate = '" + rs.getDate(2) + "'  and strPOSCode ='" + rs.getString(1) + "'");
	    clsGlobalVarClass.dbMysql.execute(sb.toString());
	}
	//no of home delivery bills
	sb.setLength(0);
	sb.append("select b.dteBillDate,  b.strPOSCode, Count(*) as NoOfBills,c.strPOSName "
		+ " from tblBillHd b,tblPOSMaster c "
		+ " Where b.strOperationType='HomeDelivery' "
		+ " and b.strPOSCode=c.strPOSCode"
		+ " and  Date(b.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	sb.append(filter + " group by date(b.dteBillDate),c.strPOSCode");
	sb.append(" UNION ALL ");
	sb.append(" select b.dteBillDate,  b.strPOSCode, Count(*) as NoOfBills,c.strPOSName "
		+ " from tblqBillHd b ,tblPOSMaster c"
		+ " Where b.strOperationType='HomeDelivery'"
		+ " and b.strPOSCode=c.strPOSCode"
		+ " and  Date(b.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	sb.append(filter + " group by date(b.dteBillDate),c.strPOSCode");

	rs = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
	while (rs.next())
	{
	    sb.setLength(0);
	    sb.append("update tblatvreport set dblHDNoBill='" + rs.getDouble(3) + "' ,strPosName='" + rs.getString(4) + "' "
		    + " where dteDate = '" + rs.getDate(1) + "'  and strPOSCode ='" + rs.getString(2) + "'");
	    clsGlobalVarClass.dbMysql.execute(sb.toString());
	}
	//Take Away
	sb.setLength(0);
	sb.append("select  b.strPOSCode,c.strPOSName,b.dteBillDate,  sum(dblQuantity) as ItemQty "
		+ " from tblBillDtl a, tblBillHd b,tblPOSMaster c"
		+ " Where b.strOperationType='TakeAway' "
		+ " and  Date(b.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		+ " and a.strBillNo = b.strBillNo "
		+ " and b.strPOSCode=c.strPOSCode");
	sb.append(filter + " group by date(b.dteBillDate),c.strPOSCode");
	sb.append(" UNION ALL ");
	sb.append("select  b.strPOSCode,c.strPOSName,b.dteBillDate,  sum(dblQuantity) as ItemQty "
		+ " from tblqBillDtl a, tblqBillHd b,tblPOSMaster c"
		+ " Where b.strOperationType='TakeAway' "
		+ " and  Date(b.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		+ " and a.strBillNo = b.strBillNo "
		+ " and b.strPOSCode=c.strPOSCode");
	sb.append(filter + " group by date(b.dteBillDate),c.strPOSCode");

	rs = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
	while (rs.next())
	{
	    sb.setLength(0);
	    sb.append("update tblatvreport set dblTAAmt='" + rs.getDouble(4) + "',strPosName='" + rs.getString(2) + "' "
		    + " where dteDate = '" + rs.getDate(3) + "'  and strPOSCode ='" + rs.getString(1) + "'");
	    clsGlobalVarClass.dbMysql.execute(sb.toString());
	}
	//no  of take away bills
	sb.setLength(0);
	sb.append("select b.dteBillDate,  b.strPOSCode, Count(*) as NoOfBills,c.strPOSName "
		+ " from tblBillHd b,tblPOSMaster c"
		+ " Where b.strOperationType='TakeAway' "
		+ " and Date(b.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		+ " and b.strPOSCode=c.strPOSCode ");
	sb.append(filter + " group by date(b.dteBillDate),c.strPOSCode");
	sb.append(" UNION ALL ");
	sb.append("select b.dteBillDate,  b.strPOSCode, Count(*) as NoOfBills,c.strPOSName "
		+ " from tblqBillHd b,tblPOSMaster c"
		+ " Where b.strOperationType='TakeAway' "
		+ " and Date(b.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		+ " and b.strPOSCode=c.strPOSCode ");
	sb.append(filter + " group by date(b.dteBillDate),c.strPOSCode");

	rs = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
	while (rs.next())
	{
	    sb.setLength(0);
	    sb.append("update tblatvreport set dblTANoBill='" + rs.getDouble(3) + "' ,strPosName='" + rs.getString(4) + "'"
		    + "where dteDate = '" + rs.getDate(1) + "'  and strPOSCode ='" + rs.getString(2) + "'");
	    clsGlobalVarClass.dbMysql.execute(sb.toString());
	}
	sb.setLength(0);
	sb.append("update tblatvreport set dblDiningAvg=  dblDiningAmt/dblDiningNoBill, dblHDAvg= dblHDAmt/dblHDNoBill, dblTAAvg= dblTAAmt/dblTANoBill");
	clsGlobalVarClass.dbMysql.execute(sb.toString());

    }

    /**
     * *
     * This Function Calls Reports
     */
    private void funCallReport() throws Exception
    {

	SimpleDateFormat yyyyMMddDateFormat = new SimpleDateFormat("yyyy-MM-dd");

	SimpleDateFormat ddMMyyyyDateFormat = new SimpleDateFormat("dd-MM-yyyy");

	fromDate = yyyyMMddDateFormat.format(dteFromDate.getDate());
	toDate = yyyyMMddDateFormat.format(dteToDate.getDate());

	Date dteFromDate = yyyyMMddDateFormat.parse(fromDate);
	Date dteToDate = yyyyMMddDateFormat.parse(toDate);

	if (dteToDate.before(dteFromDate))
	{
	    new frmOkPopUp(this, "Invalid date", "Error", 1).setVisible(true);
	}
	else
	{

	    fromDate = objUtility.funGetFromToDate(dteFromDate);

	    toDate = objUtility.funGetFromToDate(dteToDate);

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

		String reportName = "com/POSReport/reports/rptAVT1.jasper";

		InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);
		HashMap hm = new HashMap();
		hm.put("FromDate", fromDate);
		hm.put("ToDate", toDate);
		hm.put("strUserName", clsGlobalVarClass.gUserName.toUpperCase());
		hm.put("strImagePath", imagePath);
		String[] fromDateParts = fromDate.split("-");
		String[] toDateParts = toDate.split("-");
		String fromDateToDisplay = fromDateParts[2] + "-" + fromDateParts[1] + "-" + fromDateParts[0];
		String toDateToDisplay = toDateParts[2] + "-" + toDateParts[1] + "-" + toDateParts[0];

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
		hm.put("posName", cmbPosCode.getSelectedItem().toString());

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
		JRViewer viewer = new JRViewer(print);
		JFrame jf = new JFrame();
		jf.getContentPane().add(viewer);
		jf.validate();
		jf.setVisible(true);
		jf.setSize(new Dimension(850, 750));
		//jf.setLocation(300, 10);
		jf.setLocationRelativeTo(this);

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
	fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());
	toDate = objUtility.funGetFromToDate(dteToDate.getDate());
	String[] posName = cmbPosCode.getSelectedItem().toString().split(" ");
	double totalDiningAmt = 0;
	double totalDiningNoOfBill = 0;
	double totalDiningAvg = 0;
	sb.setLength(0);
	sb.append(" SELECT tblatvreport.`strPosCode` AS tblatvreport_strPosCode, "
		+ " tblatvreport.`dteDate` AS tblatvreport_dteDate, "
		+ " tblatvreport.`dblDiningAmt` AS tblatvreport_dblDiningAmt, "
		+ " tblatvreport.`dblDiningNoBill` AS tblatvreport_dblDiningNoBill, "
		+ " tblatvreport.`dblDiningAvg` AS tblatvreport_dblDiningAvg, "
		+ " tblatvreport.`dblHDAmt` AS tblatvreport_dblHDAmt, "
		+ " tblatvreport.`dblHDNoBill` AS tblatvreport_dblHDNoBill, "
		+ " tblatvreport.`dblHdAvg` AS tblatvreport_dblHdAvg, "
		+ " tblatvreport.`dblTAAmt` AS tblatvreport_dblTAAmt, "
		+ " tblatvreport.`dblTANoBill` AS tblatvreport_dblTANoBill, "
		+ " tblatvreport.`dblTAAvg` AS tblatvreport_dblTAAvg, "
		+ " tblatvreport.`strPosName` AS tblatvreport_strPosName "
		+ " FROM `tblatvreport` tblatvreport ");

	ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
	int i = 1;
	while (rs.next())
	{
	    List<String> arrListItem = new ArrayList<String>();
	    arrListItem.add(rs.getString(1));
	    arrListItem.add(rs.getString(12));
	    arrListItem.add(rs.getString(2));
	    arrListItem.add(rs.getString(4));
	    arrListItem.add(rs.getString(3));
	    arrListItem.add(rs.getString(5));
	    arrListItem.add(" ");

	    totalDiningAmt = totalDiningAmt + Double.parseDouble(rs.getString(3));
	    totalDiningNoOfBill = totalDiningNoOfBill + Double.parseDouble(rs.getString(4));
	    totalDiningAvg = totalDiningAvg + Double.parseDouble(rs.getString(5));

	    mapExcelItemDtl.put(i, arrListItem);

	    i++;

	}

	arrListTotal.add(String.valueOf(Math.rint(totalDiningNoOfBill)) + "#" + "4");
	arrListTotal.add(String.valueOf(Math.rint(totalDiningAmt)) + "#" + "5");
	arrListTotal.add(String.valueOf(Math.rint(totalDiningAvg)) + "#" + "6");

	arrHeaderList.add("Serial No");
	arrHeaderList.add("POS Code");
	arrHeaderList.add("POS Name");
	arrHeaderList.add("BillDate");
	arrHeaderList.add("Dining No Of Bill");
	arrHeaderList.add("Dining Amt");
	arrHeaderList.add("Dining Avg");
	arrHeaderList.add("");

	List<String> arrparameterList = new ArrayList<String>();
	arrparameterList.add("Average Ticket Value Report");
	arrparameterList.add("POS" + " : " + posName[0]);
	arrparameterList.add("FromDate" + " : " + fromDate);
	arrparameterList.add("ToDate" + " : " + toDate);
	arrparameterList.add(" ");
	arrparameterList.add(" ");

	objUtility.funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "ATVExcelSheet");

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
        lblposname = new javax.swing.JLabel();
        cmbPosCode = new javax.swing.JComboBox();
        lblfromDate = new javax.swing.JLabel();
        dteFromDate = new com.toedter.calendar.JDateChooser();
        dteToDate = new com.toedter.calendar.JDateChooser();
        lblToDate = new javax.swing.JLabel();
        lblposWise = new javax.swing.JLabel();
        cmbPosWise = new javax.swing.JComboBox();
        lblDateWise = new javax.swing.JLabel();
        cmbDateWise = new javax.swing.JComboBox();
        btnView = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();
        lblReportName = new javax.swing.JLabel();
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

        lblProductName.setFont(new java.awt.Font("Trebuchet MS", 1, 14)); // NOI18N
        lblProductName.setForeground(new java.awt.Color(255, 255, 255));
        lblProductName.setText("SPOS - ");
        pnlheader.add(lblProductName);

        lblModuleName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblModuleName.setForeground(new java.awt.Color(255, 255, 255));
        pnlheader.add(lblModuleName);

        lblformName.setFont(new java.awt.Font("Trebuchet MS", 1, 14)); // NOI18N
        lblformName.setForeground(new java.awt.Color(255, 255, 255));
        lblformName.setText("-ATV");
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

        lblposname.setFont(new java.awt.Font("Trebuchet MS", 0, 12)); // NOI18N
        lblposname.setText("POS Name :");

        cmbPosCode.setFont(new java.awt.Font("Trebuchet MS", 0, 12)); // NOI18N
        cmbPosCode.setToolTipText("Select POS ");
        cmbPosCode.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                cmbPosCodeActionPerformed(evt);
            }
        });

        lblfromDate.setFont(new java.awt.Font("Trebuchet MS", 0, 12)); // NOI18N
        lblfromDate.setText("From Date :");

        dteFromDate.setToolTipText("Select From Date");
        dteFromDate.setFont(new java.awt.Font("Trebuchet MS", 0, 11)); // NOI18N
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

        dteToDate.setToolTipText("Select To Date");
        dteToDate.setFont(new java.awt.Font("Trebuchet MS", 0, 11)); // NOI18N
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

        lblToDate.setFont(new java.awt.Font("Trebuchet MS", 0, 12)); // NOI18N
        lblToDate.setText("To Date :");

        lblposWise.setFont(new java.awt.Font("Trebuchet MS", 0, 12)); // NOI18N
        lblposWise.setText("POS Wise :");

        cmbPosWise.setFont(new java.awt.Font("Trebuchet MS", 0, 12)); // NOI18N
        cmbPosWise.setToolTipText("Select POS Wise");

        lblDateWise.setFont(new java.awt.Font("Trebuchet MS", 0, 12)); // NOI18N
        lblDateWise.setText("Date Wise :");

        cmbDateWise.setFont(new java.awt.Font("Trebuchet MS", 0, 12)); // NOI18N
        cmbDateWise.setToolTipText("Select Date Wise");

        btnView.setFont(new java.awt.Font("Trebuchet MS", 1, 14)); // NOI18N
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

        btnClose.setFont(new java.awt.Font("Trebuchet MS", 1, 14)); // NOI18N
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

        lblReportName.setFont(new java.awt.Font("Trebuchet MS", 0, 24)); // NOI18N
        lblReportName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblReportName.setText(" Average Ticket Value");
        lblReportName.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        lblReportType.setFont(new java.awt.Font("Trebuchet MS", 0, 12)); // NOI18N
        lblReportType.setText("Report Type :");

        cmbReportType.setFont(new java.awt.Font("Trebuchet MS", 0, 12)); // NOI18N
        cmbReportType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "A4 Size Report", "Excel Report" }));
        cmbReportType.setToolTipText("Select Date Wise");

        lblShiftNo.setFont(new java.awt.Font("Trebuchet MS", 0, 12)); // NOI18N
        lblShiftNo.setText("Shift No :");

        cmbShiftNo.setFont(new java.awt.Font("Trebuchet MS", 0, 12)); // NOI18N
        cmbShiftNo.setToolTipText("Select Date Wise");

        javax.swing.GroupLayout pnlMainLayout = new javax.swing.GroupLayout(pnlMain);
        pnlMain.setLayout(pnlMainLayout);
        pnlMainLayout.setHorizontalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addContainerGap(253, Short.MAX_VALUE)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlMainLayout.createSequentialGroup()
                            .addComponent(lblfromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(dteFromDate, javax.swing.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlMainLayout.createSequentialGroup()
                            .addComponent(lblposname, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(cmbPosCode, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlMainLayout.createSequentialGroup()
                            .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(lblToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblposWise, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblDateWise, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(cmbDateWise, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(cmbPosWise, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(dteToDate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlMainLayout.createSequentialGroup()
                            .addComponent(lblReportType, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(cmbReportType, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(pnlMainLayout.createSequentialGroup()
                            .addComponent(lblShiftNo, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(cmbShiftNo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(pnlMainLayout.createSequentialGroup()
                            .addComponent(btnView, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(41, 41, 41)
                            .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(lblReportName, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(262, Short.MAX_VALUE))
        );
        pnlMainLayout.setVerticalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(lblReportName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(38, 38, 38)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblposname, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbPosCode, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblfromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dteFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dteToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblposWise, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbPosWise, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(17, 17, 17)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblDateWise, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbDateWise, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblReportType, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbReportType, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblShiftNo, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbShiftNo, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 50, Short.MAX_VALUE)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnView, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(48, 48, 48))
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
    private void btnViewMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnViewMouseClicked

	try
	{
	    if (cmbReportType.getSelectedItem().toString().equalsIgnoreCase("A4 Size Report"))
	    {
		funInsertData();
		funCallReport();
	    }
	    else
	    {
		funInsertData();
		funGenerateExcelSheetOfReport();
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }//GEN-LAST:event_btnViewMouseClicked
    /**
     * This Function is Used Close Window
     *
     * @param evt
     */
    private void btnCloseMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCloseMouseClicked
	// TODO add your handling code here:
	dispose();
	clsGlobalVarClass.hmActiveForms.remove("AvgTicketValue");
    }//GEN-LAST:event_btnCloseMouseClicked

    private void btnViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnViewActionPerformed
	// TODO add your handling code here:
    }//GEN-LAST:event_btnViewActionPerformed

    private void cmbPosCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbPosCodeActionPerformed
	// TODO add your handling code here:
	funFillShiftCombo();
    }//GEN-LAST:event_cmbPosCodeActionPerformed

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("AvgTicketValue");
    }//GEN-LAST:event_btnCloseActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("AvgTicketValue");
    }//GEN-LAST:event_formWindowClosed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("AvgTicketValue");
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
	    java.util.logging.Logger.getLogger(frmATV.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	}
	catch (InstantiationException ex)
	{
	    java.util.logging.Logger.getLogger(frmATV.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	}
	catch (IllegalAccessException ex)
	{
	    java.util.logging.Logger.getLogger(frmATV.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	}
	catch (javax.swing.UnsupportedLookAndFeelException ex)
	{
	    java.util.logging.Logger.getLogger(frmATV.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	}
	//</editor-fold>
	//</editor-fold>
	//</editor-fold>
	//</editor-fold>

	/* Create and display the form */
	java.awt.EventQueue.invokeLater(new Runnable()
	{
	    public void run()
	    {
		new frmATV().setVisible(true);
	    }
	});
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnView;
    private javax.swing.JComboBox cmbDateWise;
    private javax.swing.JComboBox cmbPosCode;
    private javax.swing.JComboBox cmbPosWise;
    private javax.swing.JComboBox cmbReportType;
    private javax.swing.JComboBox cmbShiftNo;
    private com.toedter.calendar.JDateChooser dteFromDate;
    private com.toedter.calendar.JDateChooser dteToDate;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblDateWise;
    private javax.swing.JLabel lblHOSign;
    private javax.swing.JLabel lblModuleName;
    private javax.swing.JLabel lblPosName;
    private javax.swing.JLabel lblProductName;
    private javax.swing.JLabel lblReportName;
    private javax.swing.JLabel lblReportType;
    private javax.swing.JLabel lblShiftNo;
    private javax.swing.JLabel lblToDate;
    private javax.swing.JLabel lblUserCode;
    private javax.swing.JLabel lblformName;
    private javax.swing.JLabel lblfromDate;
    private javax.swing.JLabel lblposWise;
    private javax.swing.JLabel lblposname;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JPanel pnlbackground;
    private javax.swing.JPanel pnlheader;
    // End of variables declaration//GEN-END:variables
}
