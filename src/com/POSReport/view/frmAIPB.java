/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.POSReport.view;

import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsGroupSubGroupWiseSales;
import com.POSGlobal.controller.clsPosConfigFile;
import com.POSGlobal.controller.clsUtility;
import com.POSGlobal.view.frmOkPopUp;
import com.POSReport.controller.comparator.clsGroupSubGroupWiseSalesComparator;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.swing.JRViewer;

public class frmAIPB extends javax.swing.JFrame
{

    String selectQuery1, fromDate, toDate, imagePath;
    private StringBuilder sb = new StringBuilder();
    private clsUtility objUtility;
    private final String gDecimalFormatString = clsGlobalVarClass.funGetGlobalDecimalFormatString();

    /**
     * this Function is used for Component initialization
     */
    public frmAIPB() throws ParseException
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
		    String dateAndtime = clsGlobalVarClass.gPOSDateToDisplay + " " + newstr;
		    lblDate.setText(dateAndtime);

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
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	    final Date userDateRange = dateFormat.parse(clsGlobalVarClass.gPOSDateToDisplay);
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
				funSetFormToInDateChosser();
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

	if ((dteToDate.getDate().getTime() - dteFromDate.getDate().getTime()) < 0)
	{
	    new frmOkPopUp(this, "Invalid date", "Error", 1).setVisible(true);
	}
	else
	{
	    fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());
	    toDate = objUtility.funGetFromToDate(dteToDate.getDate());

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

	    String reportName = "com/POSReport/reports/rptAIPB.jasper";
	    InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);
	    HashMap hm = new HashMap();
	    hm.put("FromDate", fromDate);
	    hm.put("ToDate", toDate);
	    hm.put("strUserName", clsGlobalVarClass.gUserName.toUpperCase());
	    hm.put("strImagePath", imagePath);
	    hm.put("decimalFormaterForDoubleValue", gDecimalFormatString);
	    hm.put("decimalFormaterForIntegerValue", "0");

	    SimpleDateFormat ddmmyyyyDateFormat = new SimpleDateFormat("dd-MM-yyyy");
	    hm.put("fromDateToDisplay", ddmmyyyyDateFormat.format(dteFromDate.getDate()));
	    hm.put("toDateToDisplay", ddmmyyyyDateFormat.format(dteToDate.getDate()));
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

	    Map<String, clsGroupSubGroupWiseSales> mapGroupWiseSales = new HashMap<>();

	    StringBuilder filter = new StringBuilder();
	    StringBuilder sqlBuilder = new StringBuilder();

	    if (!cmbPosCode.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		filter.append(" and a.strPOSCode='" + objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString().trim()) + "' ");
	    }
	    if (clsGlobalVarClass.gEnableShiftYN)
	    {
		if (clsGlobalVarClass.gEnableShiftYN && (!cmbShiftNo.getSelectedItem().toString().equalsIgnoreCase("All")))
		{
		    filter.append(" and a.intShiftCode ='" + cmbShiftNo.getSelectedItem().toString() + "' ");
		}
	    }
	    filter.append(" GROUP BY  c.strGroupName "
		    + "ORDER BY  c.strGroupName  ");

	    //live sales
	    sqlBuilder.setLength(0);
	    sqlBuilder.append("SELECT c.strGroupName, SUM(b.dblQuantity),count(distinct a.strBillNo)NoOfBills "
		    + "FROM tblbillhd a,tblbilldtl b,tblgrouphd c,tblsubgrouphd d,tblitemmaster e,tblposmaster f,tblwaitermaster g "
		    + "WHERE a.strBillNo=b.strBillNo  "
		    + "AND DATE(a.dteBillDate)= DATE(b.dteBillDate)  "
		    + "AND a.strPOSCode=f.strPOSCode  "
		    + "AND a.strClientCode=b.strClientCode  "
		    + "AND b.strItemCode=e.strItemCode  "
		    + "AND c.strGroupCode=d.strGroupCode  "
		    + "AND d.strSubGroupCode=e.strSubGroupCode  "
		    + "AND a.strWaiterNo=g.strWaiterNo  "
		    + "AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' ");
	    sqlBuilder.append(filter);
	    ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
	    while (rs.next())
	    {
		String groupName = rs.getString(1);
		double qty = rs.getDouble(2);
		int noOfBills = rs.getInt(3);

		if (mapGroupWiseSales.containsKey(groupName))
		{
		    clsGroupSubGroupWiseSales objGroupWiseSales = mapGroupWiseSales.get(groupName);
		    objGroupWiseSales.setQty(objGroupWiseSales.getQty() + qty);
		    objGroupWiseSales.setIntNoOfBills(objGroupWiseSales.getIntNoOfBills() + noOfBills);

		}
		else
		{
		    clsGroupSubGroupWiseSales objGroupWiseSales = new clsGroupSubGroupWiseSales();
		    objGroupWiseSales.setGroupName(groupName);
		    objGroupWiseSales.setQty(qty);
		    objGroupWiseSales.setIntNoOfBills(noOfBills);

		    mapGroupWiseSales.put(groupName, objGroupWiseSales);
		}
	    }
	    rs.close();

	    //live modifiers sales
	    sqlBuilder.setLength(0);
	    sqlBuilder.append("SELECT c.strGroupName, SUM(b.dblQuantity),0 NoOfBills "
		    + "FROM tblbillmodifierdtl b,tblbillhd a,tblposmaster f,tblitemmaster d,tblsubgrouphd e,tblgrouphd c,tblwaitermaster g "
		    + "WHERE a.strBillNo=b.strBillNo  "
		    + "AND DATE(a.dteBillDate)= DATE(b.dteBillDate)  "
		    + "AND a.strPOSCode=f.strPosCode  "
		    + "AND a.strClientCode=b.strClientCode  "
		    + "AND LEFT(b.strItemCode,7)=d.strItemCode  "
		    + "AND d.strSubGroupCode=e.strSubGroupCode "
		    + "AND e.strGroupCode=c.strGroupCode  "
		    + "AND a.strWaiterNo=g.strWaiterNo  "
		    + "AND b.dblamount>0   "
		    + "AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' ");
	    sqlBuilder.append(filter);
	    rs = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
	    while (rs.next())
	    {
		String groupName = rs.getString(1);
		double qty = rs.getDouble(2);
		int noOfBills = rs.getInt(3);

		if (mapGroupWiseSales.containsKey(groupName))
		{
		    clsGroupSubGroupWiseSales objGroupWiseSales = mapGroupWiseSales.get(groupName);
		    objGroupWiseSales.setQty(objGroupWiseSales.getQty() + qty);
		    objGroupWiseSales.setIntNoOfBills(objGroupWiseSales.getIntNoOfBills() + noOfBills);

		}
		else
		{
		    clsGroupSubGroupWiseSales objGroupWiseSales = new clsGroupSubGroupWiseSales();
		    objGroupWiseSales.setGroupName(groupName);
		    objGroupWiseSales.setQty(qty);
		    objGroupWiseSales.setIntNoOfBills(noOfBills);

		    mapGroupWiseSales.put(groupName, objGroupWiseSales);
		}
	    }
	    rs.close();

	    //Q sales
	    sqlBuilder.setLength(0);
	    sqlBuilder.append("SELECT c.strGroupName, SUM(b.dblQuantity),count(distinct a.strBillNo)NoOfBills "
		    + "FROM tblqbillhd a,tblqbilldtl b,tblgrouphd c,tblsubgrouphd d,tblitemmaster e,tblposmaster f,tblwaitermaster g "
		    + "WHERE a.strBillNo=b.strBillNo  "
		    + "AND DATE(a.dteBillDate)= DATE(b.dteBillDate)  "
		    + "AND a.strPOSCode=f.strPOSCode  "
		    + "AND a.strClientCode=b.strClientCode  "
		    + "AND b.strItemCode=e.strItemCode  "
		    + "AND c.strGroupCode=d.strGroupCode  "
		    + "AND d.strSubGroupCode=e.strSubGroupCode  "
		    + "AND a.strWaiterNo=g.strWaiterNo  "
		    + "AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' ");
	    sqlBuilder.append(filter);
	    rs = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
	    while (rs.next())
	    {
		String groupName = rs.getString(1);
		double qty = rs.getDouble(2);
		int noOfBills = rs.getInt(3);

		if (mapGroupWiseSales.containsKey(groupName))
		{
		    clsGroupSubGroupWiseSales objGroupWiseSales = mapGroupWiseSales.get(groupName);
		    objGroupWiseSales.setQty(objGroupWiseSales.getQty() + qty);
		    objGroupWiseSales.setIntNoOfBills(objGroupWiseSales.getIntNoOfBills() + noOfBills);

		}
		else
		{
		    clsGroupSubGroupWiseSales objGroupWiseSales = new clsGroupSubGroupWiseSales();
		    objGroupWiseSales.setGroupName(groupName);
		    objGroupWiseSales.setQty(qty);
		    objGroupWiseSales.setIntNoOfBills(noOfBills);

		    mapGroupWiseSales.put(groupName, objGroupWiseSales);
		}
	    }
	    rs.close();

	    //live modifiers sales
	    sqlBuilder.setLength(0);
	    sqlBuilder.append("SELECT c.strGroupName, SUM(b.dblQuantity),0 NoOfBills "
		    + "FROM tblqbillmodifierdtl b,tblqbillhd a,tblposmaster f,tblitemmaster d,tblsubgrouphd e,tblgrouphd c,tblwaitermaster g "
		    + "WHERE a.strBillNo=b.strBillNo  "
		    + "AND DATE(a.dteBillDate)= DATE(b.dteBillDate)  "
		    + "AND a.strPOSCode=f.strPosCode  "
		    + "AND a.strClientCode=b.strClientCode  "
		    + "AND LEFT(b.strItemCode,7)=d.strItemCode  "
		    + "AND d.strSubGroupCode=e.strSubGroupCode "
		    + "AND e.strGroupCode=c.strGroupCode  "
		    + "AND a.strWaiterNo=g.strWaiterNo  "
		    + "AND b.dblamount>0   "
		    + "AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' ");
	    sqlBuilder.append(filter);
	    rs = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
	    while (rs.next())
	    {
		String groupName = rs.getString(1);
		double qty = rs.getDouble(2);
		int noOfBills = rs.getInt(3);

		if (mapGroupWiseSales.containsKey(groupName))
		{
		    clsGroupSubGroupWiseSales objGroupWiseSales = mapGroupWiseSales.get(groupName);
		    objGroupWiseSales.setQty(objGroupWiseSales.getQty() + qty);
		    objGroupWiseSales.setIntNoOfBills(objGroupWiseSales.getIntNoOfBills() + noOfBills);

		}
		else
		{
		    clsGroupSubGroupWiseSales objGroupWiseSales = new clsGroupSubGroupWiseSales();
		    objGroupWiseSales.setGroupName(groupName);
		    objGroupWiseSales.setQty(qty);
		    objGroupWiseSales.setIntNoOfBills(noOfBills);

		    mapGroupWiseSales.put(groupName, objGroupWiseSales);
		}
	    }
	    rs.close();

	    List<clsGroupSubGroupWiseSales> listOfGroupWiseSalesAvg = new ArrayList<>();
	    for (clsGroupSubGroupWiseSales objGroupWiseSales : mapGroupWiseSales.values())
	    {
		double dblGroupWiseAIPBAvg = (objGroupWiseSales.getQty() / objGroupWiseSales.getIntNoOfBills());

		objGroupWiseSales.setDblGroupWiseAIPBAvg(dblGroupWiseAIPBAvg);

		listOfGroupWiseSalesAvg.add(objGroupWiseSales);
	    }
	    Comparator<clsGroupSubGroupWiseSales> groupNameComparator = new Comparator<clsGroupSubGroupWiseSales>()
	    {

		@Override
		public int compare(clsGroupSubGroupWiseSales o1, clsGroupSubGroupWiseSales o2)
		{
		    return o1.getGroupName().compareToIgnoreCase(o2.getGroupName());
		}
	    };
	    Collections.sort(listOfGroupWiseSalesAvg, new clsGroupSubGroupWiseSalesComparator(groupNameComparator)
	    );

	    hm.put("listOfGroupWiseSalesAvg", listOfGroupWiseSalesAvg);
	    InputStream rptWaiterWiseItemWiseIncSubReportForGroupWiseSales = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptAIPBSubReportForGroupWiseSalesAvg.jasper");
	    hm.put("rptAIPBSubReportForGroupWiseSalesAvg", rptWaiterWiseItemWiseIncSubReportForGroupWiseSales);

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

    }

    private void funGenerateExcelSheetOfReport() throws Exception
    {
	Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
	List<String> arrListTotal = new ArrayList<String>();
	List<String> arrHeaderList = new ArrayList<String>();
	fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());
	toDate = objUtility.funGetFromToDate(dteToDate.getDate());
	String[] posName = cmbPosCode.getSelectedItem().toString().split(" ");
	double totalDiningNoOfItemsSold = 0;
	double totalNoOfDiningBill = 0;
	double totalDeliveryNoOfItemsSold = 0;
	double totalNoOfDeliveryBill = 0;
	double totalTakeAwayNoOfItemsSold = 0;
	double totalNoOfTakeAwayBill = 0;
	sb.setLength(0);
	sb.append("  SELECT ifnull(tblatvreport.`strPosCode`,'') AS tblatvreport_strPosCode, "
		+ " ifnull(tblatvreport.`dteDate`,'') AS tblatvreport_dteDate, "
		+ " ifnull(tblatvreport.`dblDiningAmt`,0) AS tblatvreport_dblDiningAmt, "
		+ " ifnull(tblatvreport.`dblDiningNoBill`,0) AS tblatvreport_dblDiningNoBill, "
		+ " ifnull(tblatvreport.`dblDiningAvg`,0) AS tblatvreport_dblDiningAvg, "
		+ " ifnull(tblatvreport.`dblHDAmt`,0) AS tblatvreport_dblHDAmt, "
		+ " ifnull(tblatvreport.`dblHDNoBill`,0) AS tblatvreport_dblHDNoBill, "
		+ " ifnull(tblatvreport.`dblHdAvg`,0) AS tblatvreport_dblHdAvg, "
		+ " ifnull(tblatvreport.`dblTAAmt`,0) AS tblatvreport_dblTAAmt, "
		+ " ifnull(tblatvreport.`dblTANoBill`,0) AS tblatvreport_dblTANoBill, "
		+ " ifnull(tblatvreport.`dblTAAvg`,0) AS tblatvreport_dblTAAvg, "
		+ " ifnull(tblatvreport.`strPosName`,'') AS tblatvreport_strPosName "
		+ " FROM `tblatvreport` tblatvreport  ");

	ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
	int i = 1;
	while (rs.next())
	{
	    List<String> arrListItem = new ArrayList<String>();
	    arrListItem.add(rs.getString(1));
	    arrListItem.add(rs.getString(12));
	    arrListItem.add(rs.getString(2));
	    arrListItem.add(rs.getString(3));
	    arrListItem.add(rs.getString(4));
	    arrListItem.add(rs.getString(5));
	    arrListItem.add(rs.getString(6));
	    arrListItem.add(rs.getString(7));
	    arrListItem.add(rs.getString(8));
	    arrListItem.add(rs.getString(9));
	    arrListItem.add(rs.getString(10));
	    arrListItem.add(rs.getString(11));

	    arrListItem.add(" ");

	    totalDiningNoOfItemsSold = totalDiningNoOfItemsSold + Double.parseDouble(rs.getString(3));
	    totalNoOfDiningBill = totalNoOfDiningBill + Double.parseDouble(rs.getString(4));
	    totalDeliveryNoOfItemsSold = totalDeliveryNoOfItemsSold + Double.parseDouble(rs.getString(6));
	    totalNoOfDeliveryBill = totalNoOfDeliveryBill + Double.parseDouble(rs.getString(7));
	    totalTakeAwayNoOfItemsSold = totalTakeAwayNoOfItemsSold + Double.parseDouble(rs.getString(9));
	    totalNoOfTakeAwayBill = totalNoOfTakeAwayBill + Double.parseDouble(rs.getString(10));
	    mapExcelItemDtl.put(i, arrListItem);

	    i++;
	}

	arrListTotal.add(String.valueOf(Math.rint(totalDiningNoOfItemsSold)) + "#" + "4");
	arrListTotal.add(String.valueOf(Math.rint(totalNoOfDiningBill)) + "#" + "5");
	arrListTotal.add(String.valueOf(Math.rint(totalDeliveryNoOfItemsSold)) + "#" + "7");
	arrListTotal.add(String.valueOf(Math.rint(totalNoOfDeliveryBill)) + "#" + "8");
	arrListTotal.add(String.valueOf(Math.rint(totalTakeAwayNoOfItemsSold)) + "#" + "10");
	arrListTotal.add(String.valueOf(Math.rint(totalNoOfTakeAwayBill)) + "#" + "11");

	arrHeaderList.add("Serial No");
	arrHeaderList.add("POS Code");
	arrHeaderList.add("POS Name");
	arrHeaderList.add("BillDate");
	arrHeaderList.add("Dining-No.Of Items Sold");
	arrHeaderList.add("No.Of Dining Bill");
	arrHeaderList.add("Dining Avg");
	arrHeaderList.add("Delivery No.Of Items Sold");
	arrHeaderList.add("No.Of Delivery Bill");
	arrHeaderList.add("Delivery Avg");
	arrHeaderList.add("Take Away No.Of Items Sold");
	arrHeaderList.add("No.Of Take Away Bill");
	arrHeaderList.add("Take Away Avg");

	List<String> arrparameterList = new ArrayList<String>();
	arrparameterList.add("Average Items Per Bill Report");
	arrparameterList.add("POS" + " : " + posName[0]);
	arrparameterList.add("FromDate" + " : " + fromDate);
	arrparameterList.add("ToDate" + " : " + toDate);
	arrparameterList.add(" ");
	arrparameterList.add(" ");

	objUtility.funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "AIPBExcelSheet");

    }

    private void funFillShiftCombo()
    {
	try
	{
	    String posCode = objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString());

	    if (clsGlobalVarClass.gEnableShiftYN)
	    {
		lblShiftNo.setText("Shift                :    ");
		lblShiftNo.setVisible(true);
		cmbShiftNo.setVisible(true);

		sb.setLength(0);
		if (posCode.equalsIgnoreCase("All"))
		{
		    sb.append("select max(a.intShiftCode) from tblshiftmaster a group by a.intShiftCode ");
		}
		else
		{
		    sb.append("select a.intShiftCode from tblshiftmaster a where a.strPOSCode='" + posCode + "' ");
		}
		ResultSet rsShifts = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
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
        lblformName.setText("-AIPB");
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

        cmbDateWise.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
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
        btnClose.addPropertyChangeListener(new java.beans.PropertyChangeListener()
        {
            public void propertyChange(java.beans.PropertyChangeEvent evt)
            {
                btnClosePropertyChange(evt);
            }
        });

        lblReportName.setFont(new java.awt.Font("Trebuchet MS", 0, 24)); // NOI18N
        lblReportName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblReportName.setText(" Average Items Per Bill Report");
        lblReportName.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        lblReportType.setFont(new java.awt.Font("Trebuchet MS", 0, 12)); // NOI18N
        lblReportType.setText("Report Type :");

        cmbReportType.setFont(new java.awt.Font("Trebuchet MS", 0, 12)); // NOI18N
        cmbReportType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "A4 Size Report", "Excel Report" }));
        cmbReportType.setToolTipText("Select Date Wise");

        lblShiftNo.setFont(new java.awt.Font("Trebuchet MS", 0, 12)); // NOI18N
        lblShiftNo.setText("Shift No  :");

        cmbShiftNo.setFont(new java.awt.Font("Trebuchet MS", 0, 12)); // NOI18N
        cmbShiftNo.setToolTipText("Select Date Wise");

        javax.swing.GroupLayout pnlMainLayout = new javax.swing.GroupLayout(pnlMain);
        pnlMain.setLayout(pnlMainLayout);
        pnlMainLayout.setHorizontalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMainLayout.createSequentialGroup()
                .addGap(202, 202, 202)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addComponent(lblReportName, javax.swing.GroupLayout.PREFERRED_SIZE, 337, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(17, 17, 17))
                    .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(lblReportType, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(pnlMainLayout.createSequentialGroup()
                            .addGap(37, 37, 37)
                            .addComponent(btnView, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(77, 77, 77)
                            .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(pnlMainLayout.createSequentialGroup()
                            .addComponent(lblfromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(38, 38, 38)
                            .addComponent(dteFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(pnlMainLayout.createSequentialGroup()
                            .addComponent(lblposname, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(38, 38, 38)
                            .addComponent(cmbPosCode, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(pnlMainLayout.createSequentialGroup()
                            .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(lblToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblposWise, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblDateWise, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblShiftNo, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(38, 38, 38)
                            .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(cmbPosWise, 0, 162, Short.MAX_VALUE)
                                .addComponent(dteToDate, javax.swing.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE)
                                .addComponent(cmbDateWise, 0, 162, Short.MAX_VALUE)
                                .addComponent(cmbReportType, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(cmbShiftNo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                .addContainerGap(240, Short.MAX_VALUE))
        );
        pnlMainLayout.setVerticalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(lblReportName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblposname, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbPosCode, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblfromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dteFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(21, 21, 21)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dteToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(lblposWise, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(cmbPosWise, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(17, 17, 17)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDateWise, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbDateWise, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblReportType, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cmbReportType, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE))
                .addGap(13, 13, 13)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblShiftNo, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbShiftNo, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 48, Short.MAX_VALUE)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnView, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(62, 62, 62))
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
	clsGlobalVarClass.hmActiveForms.remove("AvgItemPerBill");
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
	clsGlobalVarClass.hmActiveForms.remove("AvgItemPerBill");
    }//GEN-LAST:event_btnCloseActionPerformed

    private void btnClosePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_btnClosePropertyChange
	// TODO add your handling code here:
    }//GEN-LAST:event_btnClosePropertyChange

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("AvgItemPerBill");
    }//GEN-LAST:event_formWindowClosed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("AvgItemPerBill");
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
	    java.util.logging.Logger.getLogger(frmAIPB.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	}
	catch (InstantiationException ex)
	{
	    java.util.logging.Logger.getLogger(frmAIPB.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	}
	catch (IllegalAccessException ex)
	{
	    java.util.logging.Logger.getLogger(frmAIPB.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	}
	catch (javax.swing.UnsupportedLookAndFeelException ex)
	{
	    java.util.logging.Logger.getLogger(frmAIPB.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	}
	//</editor-fold>
	//</editor-fold>

	/* Create and display the form */
	java.awt.EventQueue.invokeLater(new Runnable()
	{
	    public void run()
	    {
		try
		{
		    new frmAIPB().setVisible(true);
		}
		catch (ParseException ex)
		{
		    Logger.getLogger(frmAIPB.class.getName()).log(Level.SEVERE, null, ex);
		}
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
