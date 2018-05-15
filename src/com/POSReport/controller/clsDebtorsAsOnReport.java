package com.POSReport.controller;

import com.POSReport.controller.comparator.clsCreditBillReportComparator;
import com.POSGlobal.controller.clsBillDtl;
import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsOperatorDtl;
import com.POSGlobal.controller.clsUtility2;
import java.awt.Desktop;
import java.awt.Dimension;
import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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

public class clsDebtorsAsOnReport
{

    DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();
    private clsUtility2 objUtility2 = new clsUtility2();

    public void funDebtorsAsOnReport(String reportType, HashMap hm, String dayEnd)
    {
	try
	{
	    InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptDebtorAsOn.jasper");

	    String fromDate = hm.get("fromDate").toString();
	    String toDate = hm.get("toDate").toString();
	    String posCode = hm.get("posCode").toString();
	    String shiftNo = hm.get("shiftNo").toString();
	    String fromDt = hm.get("fromDateToDisplay").toString();
	    String toDt = hm.get("toDateToDisplay").toString();
	    String fromDateToDisplay = fromDt.replace('-', '/');
	    String toDateToDisplay = toDt.replace('-', '/');
	    hm.put("dateToDisplay", toDateToDisplay);

	    boolean isDayEndHappend = objUtility2.isDayEndHappened(toDate);
	    if (!isDayEndHappend)
	    {
		hm.put("isDayEndHappend", "DAY END NOT DONE.");
	    }

	    List<clsBillDtl> listOfCreditBillReport = new ArrayList<clsBillDtl>();

	    String sbSqlFilters = "";
	    String sbSqlLive = "SELECT a.strCustomerCode, a.strCustomerName,if(CreditAmt is null,DebitAmt,DebitAmt - CreditAmt) Outstanding\n"
		    + "FROM \n"
		    + "(\n"
		    + "SELECT a.strPOSCode,a.strCustomerCode,d.strCustomerName, SUM(b.dblSettlementAmt) DebitAmt\n"
		    + "FROM tblbillhd a,tblbillsettlementdtl b,tblsettelmenthd c,tblcustomermaster d\n"
		    + "WHERE a.strBillNo=b.strBillNo AND b.strSettlementCode=c.strSettelmentCode \n"
		    + "AND DATE(a.dtebilldate)= DATE(b.dtebilldate) AND a.strClientCode=b.strClientCode \n"
		    + "AND c.strSettelmentType='Credit' AND a.strCustomerCode=d.strCustomerCode \n"
		    + "AND DATE(a.dteBillDate) <= '" + toDate + "'\n"
		    + "GROUP BY a.strCustomerCode) AS a\n"
		    + "left outer join  \n"
		    + "(\n"
		    + "SELECT c.strCustomerCode, ifnull(SUM(b.dblReceiptAmt),0) CreditAmt\n"
		    + "FROM tblbillhd a,tblqcreditbillreceipthd b,tblcustomermaster c\n"
		    + "WHERE a.strBillNo=b.strBillNo AND DATE(a.dteBillDate)= DATE(b.dteBillDate)\n"
		    + " AND a.strClientCode=b.strClientCode AND a.strCustomerCode=c.strCustomerCode \n"
		    + " AND DATE(a.dteBillDate) <= '" + toDate + "'\n"
		    + "GROUP BY c.strCustomerCode) AS b\n"
		    + "on a.strCustomerCode = b.strCustomerCode\n";

	    String sbSqlQFile = "SELECT a.strCustomerCode, a.strCustomerName,if(CreditAmt is null,DebitAmt,DebitAmt - CreditAmt) Outstanding\n"
		    + "FROM \n"
		    + "(\n"
		    + "SELECT a.strPOSCode,a.strCustomerCode,d.strCustomerName, SUM(b.dblSettlementAmt) DebitAmt\n"
		    + "FROM tblqbillhd a,tblqbillsettlementdtl b,tblsettelmenthd c,tblcustomermaster d\n"
		    + "WHERE a.strBillNo=b.strBillNo AND b.strSettlementCode=c.strSettelmentCode \n"
		    + "AND DATE(a.dtebilldate)= DATE(b.dtebilldate) AND a.strClientCode=b.strClientCode \n"
		    + "AND c.strSettelmentType='Credit' AND a.strCustomerCode=d.strCustomerCode \n"
		    + "AND DATE(a.dteBillDate) <= '" + toDate + "'\n"
		    + "GROUP BY a.strCustomerCode) AS a\n"
		    + "left outer join  \n"
		    + "(\n"
		    + "SELECT c.strCustomerCode, ifnull(SUM(b.dblReceiptAmt),0) CreditAmt\n"
		    + "FROM tblqbillhd a,tblqcreditbillreceipthd b,tblcustomermaster c\n"
		    + "WHERE a.strBillNo=b.strBillNo AND DATE(a.dteBillDate)= DATE(b.dteBillDate)\n"
		    + " AND a.strClientCode=b.strClientCode AND a.strCustomerCode=c.strCustomerCode \n"
		    + " AND DATE(a.dteBillDate) <= '" + toDate + "'\n"
		    + "GROUP BY c.strCustomerCode) AS b\n"
		    + "on a.strCustomerCode = b.strCustomerCode\n";
	    sbSqlFilters = " Order by a.strCustomerName";

	    sbSqlLive += " " + sbSqlFilters;
	    sbSqlQFile += " " + sbSqlFilters;
	    Map<String, clsBillDtl> hmCustomerDtl = new HashMap<String, clsBillDtl>();
	    clsBillDtl objBean = null;
	    ResultSet rsLiveData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive);

	    while (rsLiveData.next())
	    {
		if (hmCustomerDtl.containsKey(rsLiveData.getString(1)))
		{
		    objBean = hmCustomerDtl.get(rsLiveData.getString(1));
		    objBean.setDblBalanceAmt(objBean.getDblBalanceAmt() + Double.parseDouble(rsLiveData.getString(3)));

		}
		else
		{
		    objBean = new clsBillDtl();
		    objBean.setStrCustomerCode(rsLiveData.getString(1));
		    objBean.setStrCustomerName(rsLiveData.getString(2));   //Customer Name
		    objBean.setDblBalanceAmt(Double.parseDouble(rsLiveData.getString(3)));

		    hmCustomerDtl.put(rsLiveData.getString(1), objBean);
		}

	    }
	    rsLiveData.close();

	    ResultSet rsQfileData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFile);
	    while (rsQfileData.next())
	    {
		if (hmCustomerDtl.containsKey(rsQfileData.getString(1)))
		{
		    objBean = hmCustomerDtl.get(rsQfileData.getString(1));
		    objBean.setDblBalanceAmt(objBean.getDblBalanceAmt() + Double.parseDouble(rsQfileData.getString(3)));

		}
		else
		{
		    objBean = new clsBillDtl();
		    objBean.setStrCustomerCode(rsQfileData.getString(1));
		    objBean.setStrCustomerName(rsQfileData.getString(2));   //Customer Name
		    objBean.setDblBalanceAmt(Double.parseDouble(rsQfileData.getString(3)));

		    hmCustomerDtl.put(rsQfileData.getString(1), objBean);
		}

	    }
	    rsQfileData.close();

	    for (Map.Entry<String, clsBillDtl> entryOp : hmCustomerDtl.entrySet())
	    {
		clsBillDtl objBillDtl = entryOp.getValue();
		if(objBillDtl.getDblBalanceAmt()>0){
		    listOfCreditBillReport.add(objBillDtl);
		}
	    }

	    Comparator<clsBillDtl> customerComparator = new Comparator<clsBillDtl>()
	    {

		@Override
		public int compare(clsBillDtl o1, clsBillDtl o2)
		{
		    return o1.getStrCustomerCode().compareToIgnoreCase(o2.getStrCustomerCode());
		}
	    };

	    Collections.sort(listOfCreditBillReport, new clsCreditBillReportComparator(customerComparator));
	    
	    
	    
	    
	    hm.put("listOfCreditBillReport", listOfCreditBillReport);
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
