package com.POSReport.controller;

import com.POSReport.controller.comparator.clsCreditBillReportComparator;
import com.POSGlobal.controller.clsBillDtl;
import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsUtility2;
import java.awt.Desktop;
import java.awt.Dimension;
import java.io.File;
import java.io.InputStream;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.swing.JRViewer;

public class clsPaymentReceiptReport
{

    DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();
    private clsUtility2 objUtility2 = new clsUtility2();

    public void funPaymentReceiptReport(String reportType, HashMap hm, String dayEnd)
    {
	try
	{
	    InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptPaymentReceiptReport.jasper");

	    String fromDate = hm.get("fromDate").toString();
	    String toDate = hm.get("toDate").toString();
	    String posCode = hm.get("posCode").toString();
	    String shiftNo = hm.get("shiftNo").toString();
	    String posName = hm.get("posName").toString();

	    String fromDateToDisplay = hm.get("fromDateToDisplay").toString();
	    String toDateToDisplay = hm.get("toDateToDisplay").toString();

	    DateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
	    Date date = new Date();
	    String printedDate = sdf.format(date);

	    boolean isDayEndHappend = objUtility2.isDayEndHappened(toDate);
	    if (!isDayEndHappend)
	    {
		hm.put("isDayEndHappend", "DAY END NOT DONE.");
	    }
	    hm.put("printedDate", printedDate);
	    hm.put("pageFooterMessage", "END OF Date Wise Payment Receipt Report");
	    List<clsBillDtl> listOfCreditBillReport = new ArrayList<clsBillDtl>();

	    DateFormat dteDate = new SimpleDateFormat("dd-MMM-yyyy");
	    String sbSqlFilters = "", sbSqlFilters1 = "";

	    String sbSqlLive = "select a.strCustomerCode, a.strCustomerName, CreditAmt as  Outstanding,receiptNo,dteReceiptDate,settlement\n"
		    + "from \n"
		    + "(\n"
		    + "\n"
		    + "SELECT a.strCustomerCode,d.strCustomerName, \n"
		    + "DATE_FORMAT(DATE(a.dteBillDate),'%d-%b-%Y')dteBillDate\n"
		    + "FROM tblbillhd a,tblbillsettlementdtl b,tblsettelmenthd c,tblcustomermaster d\n"
		    + "WHERE a.strBillNo=b.strBillNo AND b.strSettlementCode=c.strSettelmentCode \n"
		    + "AND DATE(a.dtebilldate)= DATE(b.dtebilldate) AND a.strClientCode=b.strClientCode \n"
		    + "AND c.strSettelmentType='Credit' AND a.strCustomerCode=d.strCustomerCode \n"
		    + "AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "'\n"
		    + "GROUP BY a.strCustomerCode) as a,\n"
		    + "\n"
		    + "(SELECT b.strReceiptNo as receiptNo, DATE_FORMAT(DATE(b.dteReceiptDate),'%d-%b-%Y')dteReceiptDate,b.strSettlementName as settlement, \n"
		    + "SUM(b.dblReceiptAmt) as CreditAmt,c.strCustomerName,a.strCustomerCode\n"
		    + "FROM tblbillhd a,tblqcreditbillreceipthd b,tblcustomermaster c\n"
		    + "WHERE a.strBillNo=b.strBillNo AND DATE(a.dteBillDate)= DATE(b.dteBillDate) \n"
		    + "AND a.strClientCode=b.strClientCode AND a.strCustomerCode=c.strCustomerCode \n"
		    + "AND DATE(b.dteReceiptDate) BETWEEN '" + fromDate + "' AND '" + toDate + "'\n"
		    + "GROUP BY b.strReceiptNo) as b\n"
		    + "\n"
		    + "where a.strCustomerCode = b.strCustomerCode \n";

	    String sbSqlQFile = "select a.strCustomerCode, a.strCustomerName, CreditAmt as  Outstanding,receiptNo,dteReceiptDate,settlement\n"
		    + "from \n"
		    + "(\n"
		    + "\n"
		    + "SELECT a.strCustomerCode,d.strCustomerName, \n"
		    + "DATE_FORMAT(DATE(a.dteBillDate),'%d-%b-%Y')dteBillDate\n"
		    + "FROM tblqbillhd a,tblqbillsettlementdtl b,tblsettelmenthd c,tblcustomermaster d\n"
		    + "WHERE a.strBillNo=b.strBillNo AND b.strSettlementCode=c.strSettelmentCode \n"
		    + "AND DATE(a.dtebilldate)= DATE(b.dtebilldate) AND a.strClientCode=b.strClientCode \n"
		    + "AND c.strSettelmentType='Credit' AND a.strCustomerCode=d.strCustomerCode \n"
		    + "AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "'\n"
		    + "GROUP BY a.strCustomerCode) as a,\n"
		    + "\n"
		    + "(SELECT b.strReceiptNo as receiptNo, DATE_FORMAT(DATE(b.dteReceiptDate),'%d-%b-%Y')dteReceiptDate,b.strSettlementName as settlement, \n"
		    + "SUM(b.dblReceiptAmt) as CreditAmt,c.strCustomerName,a.strCustomerCode\n"
		    + "FROM tblqbillhd a,tblqcreditbillreceipthd b,tblcustomermaster c\n"
		    + "WHERE a.strBillNo=b.strBillNo AND DATE(a.dteBillDate)= DATE(b.dteBillDate) \n"
		    + "AND a.strClientCode=b.strClientCode AND a.strCustomerCode=c.strCustomerCode \n"
		    + "AND DATE(b.dteReceiptDate) BETWEEN '" + fromDate + "' AND '" + toDate + "'\n"
		    + "GROUP BY b.strReceiptNo) as b\n"
		    + "\n"
		    + "where a.strCustomerCode = b.strCustomerCode \n";
	    if (!posCode.equalsIgnoreCase("All"))
	    {
		sbSqlFilters1 = "and a.strPOSCode='" + posCode + "'";
	    }

	    sbSqlFilters1 = sbSqlFilters1 + " Order by a.strCustomerName ";

	    sbSqlLive += " " + sbSqlFilters1;
	    sbSqlQFile += " " + sbSqlFilters1;

	    ResultSet rsLiveData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive);

	    while (rsLiveData.next())
	    {

		clsBillDtl objBean = new clsBillDtl();
		objBean.setStrCustomerCode(rsLiveData.getString(1));
		objBean.setStrCustomerName(rsLiveData.getString(2));
		objBean.setDblAmount(rsLiveData.getDouble(3));   //Receipt Amount
		objBean.setStrReceiptNo(rsLiveData.getString(4));  //Receipt No
		objBean.setDteReceiptDate(rsLiveData.getString(5));
		objBean.setStrSettlementName(rsLiveData.getString(6));
		if (!rsLiveData.getString(4).equalsIgnoreCase(""))
		{
		    listOfCreditBillReport.add(objBean);
		}
	    }
	    rsLiveData.close();

	    ResultSet rsQfileModData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFile);
	    while (rsQfileModData.next())
	    {
		clsBillDtl objBean = new clsBillDtl();
		objBean.setStrCustomerCode(rsQfileModData.getString(1));
		objBean.setStrCustomerName(rsQfileModData.getString(2));
		objBean.setDblAmount(rsQfileModData.getDouble(3));   //Receipt Amount
		objBean.setStrReceiptNo(rsQfileModData.getString(4));  //Receipt No
		objBean.setDteReceiptDate(rsQfileModData.getString(5));
		objBean.setStrSettlementName(rsQfileModData.getString(6));
		if (!rsQfileModData.getString(4).equalsIgnoreCase(""))
		{
		    listOfCreditBillReport.add(objBean);
		}
	    }
	    rsQfileModData.close();

	    Comparator<clsBillDtl> dateComparator = new Comparator<clsBillDtl>()
	    {

		@Override
		public int compare(clsBillDtl o1, clsBillDtl o2)
		{
		    return o1.getDteReceiptDate().compareToIgnoreCase(o2.getDteReceiptDate());
		}
	    };

	    Collections.sort(listOfCreditBillReport, new clsCreditBillReportComparator(dateComparator));

	    //DecimalFormat gDecimalFormat = new DecimalFormat("0.00");
	    //call for view report
	    if (reportType.equalsIgnoreCase("A4 Size Report"))
	    {
		funViewJasperReportForBeanCollectionDataSource(is, hm, listOfCreditBillReport);
	    }

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funViewJasperReportForBeanCollectionDataSource(InputStream is, HashMap hm, Collection listOfBillData)
    {
	try
	{
	    JRBeanCollectionDataSource beanCollectionDataSource = new JRBeanCollectionDataSource(listOfBillData);
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
	    }

	}
	catch (Exception e)
	{
	    System.out.println(e.getMessage());
	    if (e.getMessage().startsWith("Byte data not found at"))
	    {
		JOptionPane.showMessageDialog(null, "Report Image Not Found!!!\nPlease Check Property Setup Report Image.", "Error Code: RIMG-1", JOptionPane.ERROR_MESSAGE);
	    }
	    e.printStackTrace();
	}
    }

}
