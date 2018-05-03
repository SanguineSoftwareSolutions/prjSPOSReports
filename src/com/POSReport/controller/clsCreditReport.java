
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.POSReport.controller;

import com.POSGlobal.controller.clsBillDtl;
import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsUtility2;
import com.POSReport.controller.comparator.clsCreditBillReportComparator;
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

public class clsCreditReport
{

    private DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();
    private clsUtility2 objUtility2 = new clsUtility2();

    public void funCreditReport(String reportType, HashMap hm, String dayEnd)
    {
	try
	{
	    InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptCreditReport.jasper");

	    String fromDate = hm.get("fromDate").toString();
	    String toDate = hm.get("toDate").toString();

	    boolean isDayEndHappend = objUtility2.isDayEndHappened(toDate);
	    if (!isDayEndHappend)
	    {
		hm.put("isDayEndHappend", "DAY END NOT DONE.");
	    }

	    String posCode = hm.get("posCode").toString();
	    String shiftNo = hm.get("shiftNo").toString();
	    String posName = hm.get("posName").toString();
	    String fromDateToDisplay = hm.get("fromDateToDisplay").toString();
	    String toDateToDisplay = hm.get("toDateToDisplay").toString();

	    DateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
	    Date date = new Date();
	    String printedDate = sdf.format(date);
	    hm.put("printedDate", printedDate);
	    hm.put("pageFooterMessage", "END OF Credit Report");

	    StringBuilder sqlLiveBuilder = new StringBuilder();
	    StringBuilder sqlQBuilder = new StringBuilder();

	    sqlLiveBuilder.append("SELECT a.strBillNo,DATE_FORMAT(date(a.dteBillDate),'%d-%b-%Y') as billDate,sum(a.dblGrandTotal) billAmt,SUM(b.dblSettlementAmt) CreditAmt,d.strCustomerName\n"
		    + ",a.strCustomerCode "
		    + "FROM tblbillhd a,tblbillsettlementdtl b,tblsettelmenthd c,tblcustomermaster d\n"
		    + "WHERE a.strBillNo=b.strBillNo AND b.strSettlementCode=c.strSettelmentCode \n"
		    + "AND DATE(a.dtebilldate)= DATE(b.dtebilldate) \n"
		    + "AND a.strClientCode=b.strClientCode \n"
		    + "AND c.strSettelmentType='Credit' \n"
		    + "AND a.strCustomerCode=d.strCustomerCode \n"
		    + "AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "'\n"
	    );

	    sqlQBuilder.append("SELECT a.strBillNo,DATE_FORMAT(date(a.dteBillDate),'%d-%b-%Y') as billDate,sum(a.dblGrandTotal) billAmt,SUM(b.dblSettlementAmt) CreditAmt,d.strCustomerName\n"
		    + ",a.strCustomerCode "
		    + "FROM tblqbillhd a,tblqbillsettlementdtl b,tblsettelmenthd c,tblcustomermaster d\n"
		    + "WHERE a.strBillNo=b.strBillNo AND b.strSettlementCode=c.strSettelmentCode \n"
		    + "AND DATE(a.dtebilldate)= DATE(b.dtebilldate) \n"
		    + "AND a.strClientCode=b.strClientCode \n"
		    + "AND c.strSettelmentType='Credit' \n"
		    + "AND a.strCustomerCode=d.strCustomerCode \n"
		    + "AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "'\n"
	    );
	    if (!posCode.equalsIgnoreCase("All"))
	    {
		sqlLiveBuilder.append("and a.strPOSCode='" + posCode + "' ");
		sqlQBuilder.append("and a.strPOSCode='" + posCode + "' ");
	    }
	    sqlLiveBuilder.append(" GROUP BY a.strBillNo ORDER BY d.strCustomerName");
	    sqlQBuilder.append("GROUP BY a.strBillNo ORDER BY d.strCustomerName");

	    List<clsBillDtl> listOfGuestCreditData = new ArrayList<>();

	    //live
	    ResultSet rsData = clsGlobalVarClass.dbMysql.executeResultSet(sqlLiveBuilder.toString());
	    while (rsData.next())
	    {
		clsBillDtl obj = new clsBillDtl();

		obj.setStrBillNo(rsData.getString(1));

		obj.setDteBillDate(rsData.getString(2));
		obj.setDblBillAmt(Double.parseDouble(rsData.getString(3)));
		obj.setDblAmount(Double.parseDouble(rsData.getString(4)));
		obj.setStrCustomerName(rsData.getString(5));
		obj.setStrCustomerCode(rsData.getString(6));
		listOfGuestCreditData.add(obj);
	    }
	    rsData.close();;
	    //Q
	    rsData = clsGlobalVarClass.dbMysql.executeResultSet(sqlQBuilder.toString());
	    while (rsData.next())
	    {
		clsBillDtl obj = new clsBillDtl();

		obj.setStrBillNo(rsData.getString(1));
		obj.setDteBillDate(rsData.getString(2));
		obj.setDblBillAmt(Double.parseDouble(rsData.getString(3)));
		obj.setDblAmount(Double.parseDouble(rsData.getString(4)));
		obj.setStrCustomerName(rsData.getString(5));
		obj.setStrCustomerCode(rsData.getString(6));
		listOfGuestCreditData.add(obj);
	    }
	    rsData.close();

	    Comparator<clsBillDtl> customerComparator = new Comparator<clsBillDtl>()
	    {

		@Override
		public int compare(clsBillDtl o1, clsBillDtl o2)
		{
		    return o1.getStrCustomerCode().compareToIgnoreCase(o2.getStrCustomerCode());
		}
	    };
	    Comparator<clsBillDtl> billComparator = new Comparator<clsBillDtl>()
	    {

		@Override
		public int compare(clsBillDtl o1, clsBillDtl o2)
		{
		    return o1.getStrBillNo().compareToIgnoreCase(o2.getStrBillNo());
		}
	    };

	    Comparator<clsBillDtl> dateComparator = new Comparator<clsBillDtl>()
	    {

		@Override
		public int compare(clsBillDtl o1, clsBillDtl o2)
		{
		    return o1.getDteBillDate().compareToIgnoreCase(o2.getDteBillDate());
		}
	    };

	    Collections.sort(listOfGuestCreditData, new clsCreditBillReportComparator(
		    dateComparator,
		    customerComparator,
		    billComparator
	    )
	    );
	    //call for view report
	    if (reportType.equalsIgnoreCase("A4 Size Report"))
	    {
		funViewJasperReportForBeanCollectionDataSource(is, hm, listOfGuestCreditData);
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
