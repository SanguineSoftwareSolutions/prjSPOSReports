/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.POSReport.view;

import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsUtility;
import com.POSGlobal.view.frmOkPopUp;
import com.POSReport.controller.clsBillItemDtlBean;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.swing.JRViewer;

/**
 *
 * @author sss11
 */
public class frmPromotionFlash extends javax.swing.JFrame
{

    public String fromDate, toDate, discount, total, ImagePath;
    DefaultTableModel dm, totalDm;

    int flag;
    private String reportName;
    private clsUtility objUtility;
    private HashMap<String, String> mapPromotions;
    private StringBuilder sb = new StringBuilder();
    private DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();

    /**
     * init Components
     *
     * @param evt
     */
    public frmPromotionFlash()
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

	    dteFromDate.setDate(objUtility.funGetDateToSetCalenderDate());
	    dteToDate.setDate(objUtility.funGetDateToSetCalenderDate());

	    ImagePath = System.getProperty("user.dir");
	    ImagePath = ImagePath + File.separator + "ReportImage";

	    ResultSet rs = null;
	    if (clsGlobalVarClass.gShowOnlyLoginPOSReports)
	    {
		cmbPosCode.addItem(clsGlobalVarClass.gPOSName + "                                         " + clsGlobalVarClass.gPOSCode);
	    }
	    else
	    {
		sb.setLength(0);
		sb.append("select strPosName,strPosCode from tblposmaster");
		rs = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
		cmbPosCode.addItem("All");
		while (rs.next())
		{
		    cmbPosCode.addItem(rs.getString(1) + "                                         " + rs.getString(2));
		}
		rs.close();
	    }

	    sb.setLength(0);
	    sb.append("select strPromoName,strPromoCode from tblPromotionMaster");
	    rs = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());

	    mapPromotions = new HashMap<String, String>();
	    cmbPromotions.addItem("All");
	    mapPromotions.put("All", "All");
	    while (rs.next())
	    {
		cmbPromotions.addItem(rs.getString(1));
		mapPromotions.put(rs.getString(1), rs.getString(2));
	    }
	    
	    if(clsGlobalVarClass.gUSDConvertionRate==0.0)
	    {
		cmbCurrency.setVisible(false);
		lblCurrency.setVisible(false);
	    }
	    else
	    {
		cmbCurrency.setVisible(true);
		lblCurrency.setVisible(true);
	    }
	    rs.close();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}

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

    private void funFillShiftCombo()
    {
	try
	{

	    if (clsGlobalVarClass.gEnableShiftYN)
	    {
		lblShiftNo.setText("Shift                :    ");
		lblShiftNo.setVisible(true);
		cmbShiftNo.setVisible(true);

		sb.setLength(0);
		if (cmbPosCode.getSelectedItem().toString().equalsIgnoreCase("All"))
		{
		    sb.append("select max(a.intShiftCode) from tblshiftmaster a group by a.intShiftCode ");
		}
		else
		{
		    sb.append("select a.intShiftCode from tblshiftmaster a where a.strPOSCode='" + objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString()) + "' ");
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
     * this Method is used for generate Jasper Report
     *
     * @return
     */
    private void funGenerateJasperReport()
    {
	try
	{

	    DefaultTableModel dm1 = new DefaultTableModel();
	    dm1.addColumn("");
	    dm1.addColumn("");
	    dm1.addColumn("");
	    dm1.addColumn("");
	    Date dt1 = dteFromDate.getDate();
	    Date dt2 = dteToDate.getDate();
	    SimpleDateFormat ddmmyyyyDateFormat = new SimpleDateFormat("dd-MM-yyyy");
	    String fromDateToDisplay = ddmmyyyyDateFormat.format(dt1);
	    String toDateToDisplay = ddmmyyyyDateFormat.format(dt2);

	    if ((dt2.getTime() - dt1.getTime()) < 0)
	    {
		new frmOkPopUp(this, "Invalid date", "Error", 1).setVisible(true);
	    }
	    else
	    {
		List<clsBillItemDtlBean> listOfPromotionBillData = new ArrayList<clsBillItemDtlBean>();
		fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());

		toDate = objUtility.funGetFromToDate(dteToDate.getDate());
		String pos = objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString());

		String promoName = cmbPromotions.getSelectedItem().toString();
		String promoCode = mapPromotions.get(promoName);

		StringBuilder sqlLiveData = new StringBuilder();
		StringBuilder sqlQData = new StringBuilder();
		String amount="sum(a.dblQuantity*a.dblRate) AS Amount";
		if(cmbCurrency.getSelectedItem().toString().equalsIgnoreCase("USD"))
		{
		    amount="sum(a.dblQuantity*a.dblRate)/b.dblUSDConverionRate AS Amount";
		}
		if (cmbSelectType.getSelectedItem().toString().equalsIgnoreCase("Summary"))
		{
		    	
		    
		    sqlLiveData.append("SELECT sum(a.dblQuantity) AS Qty,"+amount+",c.strPromoName\n"
			    + "FROM tblbillpromotiondtl a,tblbillhd b,tblpromotionmaster c\n"
			    + "WHERE a.strBillNo=b.strBillNo and a.strPromotionCode=c.strPromoCode \n"
			    + "AND DATE(b.dteBillDate) BETWEEN '" + fromDate + "' and '" + toDate + "' ");

		    sqlQData.append("SELECT sum(a.dblQuantity) AS Qty,"+amount+",c.strPromoName \n"
			    + "FROM tblqbillpromotiondtl a,tblqbillhd b,tblpromotionmaster c\n"
			    + "WHERE a.strBillNo=b.strBillNo and a.strPromotionCode=c.strPromoCode \n "
			    + "AND DATE(b.dteBillDate) BETWEEN '" + fromDate + "' and '" + toDate + "' ");

		    if (!cmbPromotions.getSelectedItem().toString().equalsIgnoreCase("All"))
		    {
			sqlLiveData.append(" and a.strPromotionCode='" + promoCode + "' ");
			sqlQData.append(" and a.strPromotionCode='" + promoCode + "' ");
		    }
		    else
		    {
			sqlLiveData.append(" group by a.strPromotionCode ");
			sqlQData.append(" group by a.strPromotionCode ");
		    }
		    if (!cmbPosCode.getSelectedItem().toString().equalsIgnoreCase("All"))
		    {
			sqlLiveData.append(" and b.strPOSCode='" + pos + "' ");
			sqlQData.append(" and b.strPOSCode='" + pos + "' ");
		    }

		    if (clsGlobalVarClass.gEnableShiftYN)
		    {
			if (clsGlobalVarClass.gEnableShiftYN && (!cmbShiftNo.getSelectedItem().toString().equalsIgnoreCase("All")))
			{
			    sqlLiveData.append(" and b.intShiftCode ='" + cmbShiftNo.getSelectedItem().toString() + "' ");
			    sqlQData.append(" and b.intShiftCode ='" + cmbShiftNo.getSelectedItem().toString() + "' ");
			}
		    }

		    /*clsGlobalVarClass.dbMysql.execute("truncate tbltempsalesflash1");
                 String insertSql = "insert into tbltempsalesflash1(strbillno,dtebilldate,tmebilltime,strtablename,strposcode,strpaymode)";
                 clsGlobalVarClass.dbMysql.execute(insertSql + " (" + sqlLiveData + ")  ");
                 clsGlobalVarClass.dbMysql.execute(insertSql + " (" + sqlQData + ")  ");
		     */
		    ResultSet rsLiveData = clsGlobalVarClass.dbMysql.executeResultSet(sqlLiveData.toString());

		    while (rsLiveData.next())
		    {
			clsBillItemDtlBean obj = new clsBillItemDtlBean();
			if (rsLiveData.getString(3) != null)
			{
			    obj.setStrItemName(rsLiveData.getString(3));
			    obj.setDblQuantity(rsLiveData.getDouble(1));
			    obj.setDblAmount(rsLiveData.getDouble(2));
			    listOfPromotionBillData.add(obj);
			}
		    }
		    rsLiveData.close();

		    ResultSet rsQfileData = clsGlobalVarClass.dbMysql.executeResultSet(sqlQData.toString());
		    while (rsQfileData.next())
		    {
			clsBillItemDtlBean obj = new clsBillItemDtlBean();
			if (rsQfileData.getString(3) != null)
			{
			    obj.setStrItemName(rsQfileData.getString(3));
			    obj.setDblQuantity(rsQfileData.getDouble(1));
			    obj.setDblAmount(rsQfileData.getDouble(2));
			    listOfPromotionBillData.add(obj);
			}
		    }
		    rsLiveData.close();

		    if (pos.equals("All"))
		    {
			reportName = "com/POSReport/reports/rptPromotionSummaryFlash.jasper";
			pos = "All";
		    }
		    else
		    {
			reportName = "com/POSReport/reports/rptPromotionSummaryFlash.jasper";
		    }

		}
		else
		{

		    sqlLiveData.append(" select a.strBillNo as Billno,DATE_FORMAT(b.dteBillDate,'%d-%m-%y') as BillDate,a.strItemCode as ItemCode,c.strItemName as ItemName, "
			    + " a.dblQuantity as Qty,"+amount+" "
			    + " from tblbillpromotiondtl a,tblbillhd b,tblitemmaster c "
			    + " where a.strBillNo=b.strBillNo and a.strItemCode=c.strItemCode"
			    + " and date(b.dteBillDate) between '" + fromDate + "' and '" + toDate + "'  ");

		    sqlQData.append(" select a.strBillNo as Billno,DATE_FORMAT(b.dteBillDate,'%d-%m-%y') as BillDate,a.strItemCode as ItemCode,c.strItemName as ItemName, "
			    + " a.dblQuantity as Qty,"+amount+" "
			    + " from tblqbillpromotiondtl a,tblqbillhd b,tblitemmaster c "
			    + " where a.strBillNo=b.strBillNo and a.strItemCode=c.strItemCode "
			    + " and date(b.dteBillDate) between '" + fromDate + "' and '" + toDate + "'  ");

		    if (!cmbPromotions.getSelectedItem().toString().equalsIgnoreCase("All"))
		    {
			sqlLiveData.append(" and a.strPromotionCode='" + promoCode + "' ");
			sqlQData.append(" and a.strPromotionCode='" + promoCode + "' ");
		    }
		    if (!cmbPosCode.getSelectedItem().toString().equalsIgnoreCase("All"))
		    {
			sqlLiveData.append(" and b.strPOSCode='" + pos + "' ");
			sqlQData.append(" and b.strPOSCode='" + pos + "' ");
		    }

		    if (clsGlobalVarClass.gEnableShiftYN)
		    {
			if (clsGlobalVarClass.gEnableShiftYN && (!cmbShiftNo.getSelectedItem().toString().equalsIgnoreCase("All")))
			{
			    sqlLiveData.append(" and b.intShiftCode ='" + cmbShiftNo.getSelectedItem().toString() + "' ");
			    sqlQData.append(" and b.intShiftCode ='" + cmbShiftNo.getSelectedItem().toString() + "' ");
			}
		    }

		    /*clsGlobalVarClass.dbMysql.execute("truncate tbltempsalesflash1");
                 String insertSql = "insert into tbltempsalesflash1(strbillno,dtebilldate,tmebilltime,strtablename,strposcode,strpaymode)";
                 clsGlobalVarClass.dbMysql.execute(insertSql + " (" + sqlLiveData + ")  ");
                 clsGlobalVarClass.dbMysql.execute(insertSql + " (" + sqlQData + ")  ");
		     */
		    ResultSet rsLiveData = clsGlobalVarClass.dbMysql.executeResultSet(sqlLiveData.toString());

		    while (rsLiveData.next())
		    {
			clsBillItemDtlBean obj = new clsBillItemDtlBean();
			obj.setStrBillNo(rsLiveData.getString(1));
			obj.setDteBillDate(rsLiveData.getString(2));
			obj.setStrItemCode(rsLiveData.getString(3));
			obj.setStrItemName(rsLiveData.getString(4));
			obj.setDblQuantity(rsLiveData.getDouble(5));
			obj.setDblAmount(rsLiveData.getDouble(6));
			listOfPromotionBillData.add(obj);
		    }
		    rsLiveData.close();

		    ResultSet rsQfileData = clsGlobalVarClass.dbMysql.executeResultSet(sqlQData.toString());
		    while (rsQfileData.next())
		    {
			clsBillItemDtlBean obj = new clsBillItemDtlBean();
			obj.setStrBillNo(rsQfileData.getString(1));
			obj.setDteBillDate(rsQfileData.getString(2));
			obj.setStrItemCode(rsQfileData.getString(3));
			obj.setStrItemName(rsQfileData.getString(4));
			obj.setDblQuantity(rsQfileData.getDouble(5));
			obj.setDblAmount(rsQfileData.getDouble(6));
			listOfPromotionBillData.add(obj);
		    }
		    rsLiveData.close();

		    if (pos.equals("All"))
		    {
			reportName = "com/POSReport/reports/rptPromotionFlash.jasper";
			pos = "All";
		    }
		    else
		    {
			reportName = "com/POSReport/reports/rptPromotionFlash.jasper";
		    }
		}
		try
		{
		    String posName = cmbPosCode.getSelectedItem().toString();
		    String posCode = objUtility.funGetPOSCodeFromPOSName(posName);

		    ImagePath = System.getProperty("user.dir");
		    ImagePath = ImagePath + File.separator + "ReportImage";
		    if (posCode.equalsIgnoreCase("All"))
		    {
			ImagePath = ImagePath + File.separator + "imgClientImage.jpg";
		    }
		    else
		    {
			ImagePath = ImagePath + File.separator + "img" + posCode + ".jpg";
		    }
		    System.out.println("imagePath=" + ImagePath);

		    InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);
		    HashMap hm = new HashMap();
		    hm.put("posName", cmbPosCode.getSelectedItem());
		    hm.put("fromDate", fromDateToDisplay);
		    hm.put("toDate", toDateToDisplay);
		    hm.put("userName", clsGlobalVarClass.gUserName);
		    hm.put("posCode", pos);
		    hm.put("strPMCode", promoCode);
		    hm.put("strPMName", promoName);
		    hm.put("imagePath", ImagePath);

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

		    JRBeanCollectionDataSource beanCollectionDataSource = new JRBeanCollectionDataSource(listOfPromotionBillData);
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
		    e.printStackTrace();
		}

	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();

	}
    }

    private void funGenerateExcelSheetOfReport() throws Exception
    {
	Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
	List<String> arrListTotal = new ArrayList<String>();
	List<String> arrHeaderList = new ArrayList<String>();
	double totalQty = 0;
	double totalAmount = 0;
	Date dt1 = dteFromDate.getDate();
	Date dt2 = dteToDate.getDate();
	SimpleDateFormat ddmmyyyyDateFormat = new SimpleDateFormat("dd-MM-yyyy");
	String fromDateToDisplay = ddmmyyyyDateFormat.format(dt1);
	String toDateToDisplay = ddmmyyyyDateFormat.format(dt2);

	fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());

	toDate = objUtility.funGetFromToDate(dteToDate.getDate());

	String promoName = cmbPromotions.getSelectedItem().toString();
	String promoCode = mapPromotions.get(promoName);

	StringBuilder sqlLiveData = new StringBuilder();
	StringBuilder sqlQData = new StringBuilder();
	List<clsBillItemDtlBean> listOfPromotionBillData = new ArrayList<clsBillItemDtlBean>();
	String amount="sum(a.dblQuantity*a.dblRate) AS Amount";
	if(cmbCurrency.getSelectedItem().toString().equalsIgnoreCase("USD"))
	{
	    amount="sum(a.dblQuantity*a.dblRate)/b.dblUSDConverionRate AS Amount";
	}
	if (cmbSelectType.getSelectedItem().toString().equalsIgnoreCase("Summary"))
	{
	    sqlLiveData.append("SELECT sum(a.dblQuantity) AS Qty,"+amount+",c.strPromoName\n"
		    + "FROM tblbillpromotiondtl a,tblbillhd b,tblpromotionmaster c\n"
		    + "WHERE a.strBillNo=b.strBillNo and a.strPromotionCode=c.strPromoCode \n"
		    + "AND DATE(b.dteBillDate) BETWEEN '" + fromDate + "' and '" + toDate + "' ");

	    sqlQData.append("SELECT sum(a.dblQuantity) AS Qty,"+amount+",c.strPromoName \n"
		    + "FROM tblqbillpromotiondtl a,tblqbillhd b,tblpromotionmaster c\n"
		    + "WHERE a.strBillNo=b.strBillNo and a.strPromotionCode=c.strPromoCode \n "
		    + "AND DATE(b.dteBillDate) BETWEEN '" + fromDate + "' and '" + toDate + "' ");

	    if (!cmbPromotions.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlLiveData.append(" and a.strPromotionCode='" + promoCode + "' ");
		sqlQData.append(" and a.strPromotionCode='" + promoCode + "' ");
	    }
	    else
	    {
		sqlLiveData.append(" group by a.strPromotionCode ");
		sqlQData.append(" group by a.strPromotionCode ");
	    }
	    if (!cmbPosCode.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlLiveData.append(" and b.strPOSCode='" + objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString()) + "' ");
		sqlQData.append(" and b.strPOSCode='" + objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString()) + "' ");
	    }

	    ResultSet rsLiveData = clsGlobalVarClass.dbMysql.executeResultSet(sqlLiveData.toString());

	    while (rsLiveData.next())
	    {
		clsBillItemDtlBean obj = new clsBillItemDtlBean();
		if (rsLiveData.getString(3) != null)
		{
		    obj.setStrItemName(rsLiveData.getString(3));
		    obj.setDblQuantity(rsLiveData.getDouble(1));
		    obj.setDblAmount(rsLiveData.getDouble(2));
		    listOfPromotionBillData.add(obj);
		}
	    }
	    rsLiveData.close();

	    ResultSet rsQfileData = clsGlobalVarClass.dbMysql.executeResultSet(sqlQData.toString());
	    while (rsQfileData.next())
	    {
		clsBillItemDtlBean obj = new clsBillItemDtlBean();
		if (rsQfileData.getString(3) != null)
		{
		    obj.setStrItemName(rsQfileData.getString(3));
		    obj.setDblQuantity(rsQfileData.getDouble(1));
		    obj.setDblAmount(rsQfileData.getDouble(2));
		    listOfPromotionBillData.add(obj);
		}
	    }
	    rsLiveData.close();
	    int i = 1;

	    DecimalFormat decFormat = new DecimalFormat("0");
	    for (clsBillItemDtlBean objBean : listOfPromotionBillData)
	    {
		List<String> arrListItem = new ArrayList<String>();
		arrListItem.add(objBean.getStrItemName());
		arrListItem.add(String.valueOf(decFormat.format(objBean.getDblQuantity())));
		arrListItem.add(String.valueOf(gDecimalFormat.format(objBean.getDblAmount())));
		arrListItem.add(" ");

		totalQty = totalQty + Double.parseDouble(String.valueOf(objBean.getDblQuantity()));
		totalAmount = totalAmount + Double.parseDouble(String.valueOf(objBean.getDblAmount()));

		mapExcelItemDtl.put(i, arrListItem);

		i++;

	    }

	    arrListTotal.add(String.valueOf(decFormat.format(totalQty)) + "#" + "2");
	    arrListTotal.add(String.valueOf(gDecimalFormat.format(totalAmount)) + "#" + "3");

	    arrHeaderList.add("Serial No");
	    arrHeaderList.add("Promotion Name");
	    arrHeaderList.add("Qty");
	    arrHeaderList.add("Amount");
	    arrHeaderList.add(" ");

	    List<String> arrparameterList = new ArrayList<String>();
	    arrparameterList.add("Promotion Summary Report");
	    arrparameterList.add("POS" + " : " + cmbPosCode.getSelectedItem().toString());
	    arrparameterList.add("FromDate" + " : " + fromDateToDisplay);
	    arrparameterList.add("ToDate" + " : " + toDateToDisplay);
	    arrparameterList.add("Promotion" + " : " + cmbPromotions.getSelectedItem().toString());
	    arrparameterList.add(" ");

	    objUtility.funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "promotionFlashExcelSheet");

	}
	else
	{

	    sqlLiveData.append(" select a.strBillNo as Billno,DATE_FORMAT(b.dteBillDate,'%d-%m-%y') as BillDate,a.strItemCode as ItemCode,c.strItemName as ItemName, "
		    + " a.dblQuantity as Qty,"+amount+" "
		    + " from tblbillpromotiondtl a,tblbillhd b,tblitemmaster c "
		    + " where a.strBillNo=b.strBillNo and a.strItemCode=c.strItemCode"
		    + " and date(b.dteBillDate) between '" + fromDate + "' and '" + toDate + "'  ");

	    sqlQData.append(" select a.strBillNo as Billno,DATE_FORMAT(b.dteBillDate,'%d-%m-%y') as BillDate,a.strItemCode as ItemCode,c.strItemName as ItemName, "
		    + " a.dblQuantity as Qty,"+amount+" "
		    + " from tblqbillpromotiondtl a,tblqbillhd b,tblitemmaster c "
		    + " where a.strBillNo=b.strBillNo and a.strItemCode=c.strItemCode "
		    + " and date(b.dteBillDate) between '" + fromDate + "' and '" + toDate + "'  ");

	    if (!cmbPromotions.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlLiveData.append(" and a.strPromotionCode='" + promoCode + "' ");
		sqlQData.append(" and a.strPromotionCode='" + promoCode + "' ");
	    }
	    if (!cmbPosCode.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlLiveData.append(" and b.strPOSCode='" + objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString()) + "' ");
		sqlQData.append(" and b.strPOSCode='" + objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString()) + "' ");
	    }
	    clsGlobalVarClass.dbMysql.execute("truncate tbltempsalesflash1");
	    String insertSql = "insert into tbltempsalesflash1(strbillno,dtebilldate,tmebilltime,strtablename,strposcode,strpaymode)";
	    clsGlobalVarClass.dbMysql.execute(insertSql + " (" + sqlLiveData + ")  ");
	    clsGlobalVarClass.dbMysql.execute(insertSql + " (" + sqlQData + ")  ");

	    String sql = " select a.strbillno  as Billno,a.dtebilldate  as BillDate,a.tmebilltime  as ItemCode,a.strtablename  as ItemName, "
		    + " a.strposcode as Qty,a.strpaymode as Amount "
		    + " from tbltempsalesflash1 a ";

	    ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(sql);
	    int i = 1;

	    DecimalFormat decFormat = new DecimalFormat("0");
	    while (rs.next())
	    {
		List<String> arrListItem = new ArrayList<String>();
		arrListItem.add(rs.getString(1));
		arrListItem.add(rs.getString(2));
		arrListItem.add(rs.getString(3));
		arrListItem.add(rs.getString(4));
		arrListItem.add(String.valueOf(decFormat.format(rs.getDouble(5))));
		arrListItem.add(String.valueOf(gDecimalFormat.format(rs.getDouble(6))));
		arrListItem.add(" ");

		totalQty = totalQty + Double.parseDouble(rs.getString(5));
		totalAmount = totalAmount + Double.parseDouble(rs.getString(6));

		mapExcelItemDtl.put(i, arrListItem);

		i++;

	    }

	    arrListTotal.add(String.valueOf(decFormat.format(totalQty)) + "#" + "5");
	    arrListTotal.add(String.valueOf(gDecimalFormat.format(totalAmount)) + "#" + "6");

	    arrHeaderList.add("Serial No");
	    arrHeaderList.add("Bill No");
	    arrHeaderList.add("BillDate");
	    arrHeaderList.add("Item Code");
	    arrHeaderList.add("Item Name");
	    arrHeaderList.add("Qty");
	    arrHeaderList.add("Amount");
	    arrHeaderList.add(" ");

	    List<String> arrparameterList = new ArrayList<String>();
	    arrparameterList.add("Promotion Report");
	    arrparameterList.add("POS" + " : " + cmbPosCode.getSelectedItem().toString());
	    arrparameterList.add("FromDate" + " : " + fromDateToDisplay);
	    arrparameterList.add("ToDate" + " : " + toDateToDisplay);
	    arrparameterList.add("Promotion" + " : " + cmbPromotions.getSelectedItem().toString());
	    arrparameterList.add(" ");

	    objUtility.funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "promotionFlashExcelSheet");

	}

    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

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
        lblPromotionFlash = new javax.swing.JLabel();
        lblPOSName = new javax.swing.JLabel();
        lblPromotionCode = new javax.swing.JLabel();
        dteFromDate = new com.toedter.calendar.JDateChooser();
        dteToDate = new com.toedter.calendar.JDateChooser();
        lblFromDate = new javax.swing.JLabel();
        lblToDate = new javax.swing.JLabel();
        btnSubmit = new javax.swing.JButton();
        btnReset = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        cmbPromotions = new javax.swing.JComboBox();
        cmbPosCode = new javax.swing.JComboBox();
        lblReportType = new javax.swing.JLabel();
        cmbReportType = new javax.swing.JComboBox();
        lblShiftNo = new javax.swing.JLabel();
        cmbShiftNo = new javax.swing.JComboBox();
        lblSelectType = new javax.swing.JLabel();
        cmbSelectType = new javax.swing.JComboBox();
        lblCurrency = new javax.swing.JLabel();
        cmbCurrency = new javax.swing.JComboBox();

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
        lblProductName.setText("SPOS -  ");
        lblProductName.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                lblProductNameMouseClicked(evt);
            }
        });
        pnlHeader.add(lblProductName);

        lblModuleName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblModuleName.setForeground(new java.awt.Color(255, 255, 255));
        pnlHeader.add(lblModuleName);

        lblformName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblformName.setForeground(new java.awt.Color(255, 255, 255));
        lblformName.setText("- Promotion Flash Report");
        lblformName.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                lblformNameMouseClicked(evt);
            }
        });
        pnlHeader.add(lblformName);
        pnlHeader.add(filler4);
        pnlHeader.add(filler5);

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
        pnlHeader.add(lblPosName);
        pnlHeader.add(filler6);

        lblUserCode.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblUserCode.setForeground(new java.awt.Color(255, 255, 255));
        lblUserCode.setMaximumSize(new java.awt.Dimension(90, 30));
        lblUserCode.setMinimumSize(new java.awt.Dimension(90, 30));
        lblUserCode.setPreferredSize(new java.awt.Dimension(71, 30));
        lblUserCode.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                lblUserCodeMouseClicked(evt);
            }
        });
        pnlHeader.add(lblUserCode);

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
        pnlHeader.add(lblDate);

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
        pnlHeader.add(lblHOSign);

        getContentPane().add(pnlHeader, java.awt.BorderLayout.PAGE_START);

        pnlBackground.setLayout(new java.awt.GridBagLayout());

        pnlMain.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, new java.awt.Color(204, 204, 204), new java.awt.Color(204, 204, 204), new java.awt.Color(153, 153, 153), new java.awt.Color(153, 153, 153)));
        pnlMain.setMinimumSize(new java.awt.Dimension(800, 570));
        pnlMain.setOpaque(false);

        lblPromotionFlash.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        lblPromotionFlash.setText("Promotion Flash Report");

        lblPOSName.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblPOSName.setText("POS Name                 :");

        lblPromotionCode.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblPromotionCode.setText("Promotion Code          :");

        dteFromDate.setToolTipText("Select From Date");

        dteToDate.setToolTipText("Select To Date");

        lblFromDate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblFromDate.setText("From Date                  :");

        lblToDate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblToDate.setText("To Date                     : ");

        btnSubmit.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnSubmit.setForeground(new java.awt.Color(255, 255, 255));
        btnSubmit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnSubmit.setText("VIEW");
        btnSubmit.setToolTipText("View Report");
        btnSubmit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSubmit.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn2.png"))); // NOI18N
        btnSubmit.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnSubmitMouseClicked(evt);
            }
        });

        btnReset.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnReset.setForeground(new java.awt.Color(255, 255, 255));
        btnReset.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnReset.setText("RESET");
        btnReset.setToolTipText("Reset Window");
        btnReset.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnReset.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn2.png"))); // NOI18N
        btnReset.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnResetActionPerformed(evt);
            }
        });

        jButton1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        jButton1.setText("CLOSE");
        jButton1.setToolTipText("Close Window");
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn2.png"))); // NOI18N
        jButton1.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                jButton1MouseClicked(evt);
            }
        });
        jButton1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton1ActionPerformed(evt);
            }
        });

        cmbPromotions.setBackground(new java.awt.Color(51, 102, 255));
        cmbPromotions.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        cmbPromotions.setForeground(new java.awt.Color(255, 255, 255));
        cmbPromotions.setToolTipText("Select User ");
        cmbPromotions.setMaximumSize(new java.awt.Dimension(230, 30));
        cmbPromotions.setMinimumSize(new java.awt.Dimension(230, 30));

        cmbPosCode.setBackground(new java.awt.Color(51, 102, 255));
        cmbPosCode.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        cmbPosCode.setForeground(new java.awt.Color(255, 255, 255));
        cmbPosCode.setToolTipText("Select User ");
        cmbPosCode.setMaximumSize(new java.awt.Dimension(230, 30));
        cmbPosCode.setMinimumSize(new java.awt.Dimension(230, 30));
        cmbPosCode.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                cmbPosCodeActionPerformed(evt);
            }
        });

        lblReportType.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblReportType.setText("Report Type               :");

        cmbReportType.setBackground(new java.awt.Color(51, 102, 255));
        cmbReportType.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        cmbReportType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "A4 Size Report", "Excel Report" }));
        cmbReportType.setToolTipText("Select User ");
        cmbReportType.setMaximumSize(new java.awt.Dimension(230, 30));
        cmbReportType.setMinimumSize(new java.awt.Dimension(230, 30));

        lblShiftNo.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblShiftNo.setText("Shift No                     :");

        cmbShiftNo.setBackground(new java.awt.Color(51, 102, 255));
        cmbShiftNo.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        cmbShiftNo.setToolTipText("Select User ");
        cmbShiftNo.setMaximumSize(new java.awt.Dimension(230, 30));
        cmbShiftNo.setMinimumSize(new java.awt.Dimension(230, 30));

        lblSelectType.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblSelectType.setText("Select Type                :");

        cmbSelectType.setBackground(new java.awt.Color(51, 102, 255));
        cmbSelectType.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        cmbSelectType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Summary", "Detail" }));
        cmbSelectType.setToolTipText("Select User ");
        cmbSelectType.setMaximumSize(new java.awt.Dimension(230, 30));
        cmbSelectType.setMinimumSize(new java.awt.Dimension(230, 30));

        lblCurrency.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblCurrency.setText("Currency                    :");

        cmbCurrency.setBackground(new java.awt.Color(51, 102, 255));
        cmbCurrency.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        cmbCurrency.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "BASE", "USD" }));
        cmbCurrency.setToolTipText("Select User ");
        cmbCurrency.setMaximumSize(new java.awt.Dimension(230, 30));
        cmbCurrency.setMinimumSize(new java.awt.Dimension(230, 30));

        javax.swing.GroupLayout pnlMainLayout = new javax.swing.GroupLayout(pnlMain);
        pnlMain.setLayout(pnlMainLayout);
        pnlMainLayout.setHorizontalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addGap(198, 198, 198)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(pnlMainLayout.createSequentialGroup()
                                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblShiftNo, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(dteToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(cmbShiftNo, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(lblReportType, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(pnlMainLayout.createSequentialGroup()
                                    .addGap(184, 184, 184)
                                    .addComponent(cmbReportType, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addContainerGap(254, Short.MAX_VALUE))
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlMainLayout.createSequentialGroup()
                                .addComponent(lblFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(dteFromDate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(pnlMainLayout.createSequentialGroup()
                                .addComponent(btnSubmit, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(26, 26, 26)
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlMainLayout.createSequentialGroup()
                                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlMainLayout.createSequentialGroup()
                                        .addGap(76, 76, 76)
                                        .addComponent(lblPromotionFlash, javax.swing.GroupLayout.PREFERRED_SIZE, 266, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlMainLayout.createSequentialGroup()
                                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(lblPOSName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(lblPromotionCode, javax.swing.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE))
                                        .addGap(18, 18, 18)
                                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(cmbPosCode, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(cmbPromotions, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addGap(254, 254, 254))
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblSelectType, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblCurrency))
                        .addGap(18, 18, 18)
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cmbCurrency, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cmbSelectType, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );

        pnlMainLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {lblCurrency, lblReportType, lblSelectType, lblShiftNo, lblToDate});

        pnlMainLayout.setVerticalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addComponent(lblPromotionFlash, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblPOSName, javax.swing.GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE)
                    .addComponent(cmbPosCode, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblPromotionCode, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbPromotions, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dteFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblToDate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(dteToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblReportType, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbReportType, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(lblShiftNo, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(cmbShiftNo, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(7, 7, 7)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSelectType, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbSelectType, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCurrency, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbCurrency, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(31, 31, 31)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSubmit, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(49, 49, 49))
        );

        pnlBackground.add(pnlMain, new java.awt.GridBagConstraints());

        getContentPane().add(pnlBackground, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetActionPerformed

	funResetButtonClicked();
    }//GEN-LAST:event_btnResetActionPerformed

    /**
     * submit Button
     *
     * @param evt
     */
    private void btnSubmitMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnSubmitMouseClicked

	try
	{
	    if (cmbReportType.getSelectedItem().toString().equalsIgnoreCase("A4 Size Report"))
	    {
		funGenerateJasperReport();
	    }
	    else
	    {
		funGenerateExcelSheetOfReport();
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}

    }//GEN-LAST:event_btnSubmitMouseClicked
    /**
     * Close Window
     *
     * @param evt
     */
    private void jButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseClicked
	// TODO add your handling code here:
	dispose();
	clsGlobalVarClass.hmActiveForms.remove("Promotion Flash");

    }//GEN-LAST:event_jButton1MouseClicked

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("Promotion Flash");
    }//GEN-LAST:event_jButton1ActionPerformed

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

    private void lblHOSignMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblHOSignMouseClicked
    {//GEN-HEADEREND:event_lblHOSignMouseClicked
	// TODO add your handling code here:
	objUtility = new clsUtility();
    }//GEN-LAST:event_lblHOSignMouseClicked

    private void cmbPosCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbPosCodeActionPerformed
	// TODO add your handling code here:
	funFillShiftCombo();
    }//GEN-LAST:event_cmbPosCodeActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("Promotion Flash");
    }//GEN-LAST:event_formWindowClosed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("Promotion Flash");
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
	    java.util.logging.Logger.getLogger(frmPromotionFlash.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	}
	catch (InstantiationException ex)
	{
	    java.util.logging.Logger.getLogger(frmPromotionFlash.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	}
	catch (IllegalAccessException ex)
	{
	    java.util.logging.Logger.getLogger(frmPromotionFlash.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	}
	catch (javax.swing.UnsupportedLookAndFeelException ex)
	{
	    java.util.logging.Logger.getLogger(frmPromotionFlash.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	}
	//</editor-fold>
	//</editor-fold>

	/* Create and display the form */
	java.awt.EventQueue.invokeLater(new Runnable()
	{
	    public void run()
	    {
		new frmPromotionFlash().setVisible(true);
	    }
	});
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnSubmit;
    private javax.swing.JComboBox cmbCurrency;
    private javax.swing.JComboBox cmbPosCode;
    private javax.swing.JComboBox cmbPromotions;
    private javax.swing.JComboBox cmbReportType;
    private javax.swing.JComboBox cmbSelectType;
    private javax.swing.JComboBox cmbShiftNo;
    private com.toedter.calendar.JDateChooser dteFromDate;
    private com.toedter.calendar.JDateChooser dteToDate;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel lblCurrency;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblFromDate;
    private javax.swing.JLabel lblHOSign;
    private javax.swing.JLabel lblModuleName;
    private javax.swing.JLabel lblPOSName;
    private javax.swing.JLabel lblPosName;
    private javax.swing.JLabel lblProductName;
    private javax.swing.JLabel lblPromotionCode;
    private javax.swing.JLabel lblPromotionFlash;
    private javax.swing.JLabel lblReportType;
    private javax.swing.JLabel lblSelectType;
    private javax.swing.JLabel lblShiftNo;
    private javax.swing.JLabel lblToDate;
    private javax.swing.JLabel lblUserCode;
    private javax.swing.JLabel lblformName;
    private javax.swing.JPanel pnlBackground;
    private javax.swing.JPanel pnlHeader;
    private javax.swing.JPanel pnlMain;
    // End of variables declaration//GEN-END:variables

    private void funResetButtonClicked()
    {
	cmbPosCode.setSelectedItem("All");
	cmbPromotions.setSelectedItem("All");
	cmbReportType.setSelectedIndex(0);
	if (cmbShiftNo.isVisible())
	{
	    cmbShiftNo.setSelectedIndex(0);
	}
	dteFromDate.setDate(objUtility.funGetDateToSetCalenderDate());
	dteToDate.setDate(objUtility.funGetDateToSetCalenderDate());
    }

}
