package com.POSReport.controller;

import com.POSReport.controller.comparator.clsItemConsumptionComparator;
import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsItemWiseConsumption;
import com.POSGlobal.controller.clsPosConfigFile;
import com.POSGlobal.controller.clsUtility;
import com.POSGlobal.controller.clsUtility2;
import com.POSGlobal.view.frmShowTextFile;
import com.POSReport.controller.comparator.clsItemConsumptionMonthWiseComparator;
import java.awt.Desktop;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import jdk.nashorn.internal.parser.DateParser;
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

public class clsItemWiseConsumptionReport
{

    DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();
    private clsUtility2 objUtility2 = new clsUtility2();

    public void funItemWiseConsumptionReport(String reportType, HashMap hm, String dayEnd)
    {
	try
	{
	    InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptItemWiseConsumptionReport1.jasper");

	    String fromDate = hm.get("fromDate").toString();
	    String toDate = hm.get("toDate").toString();
	    String posCode = hm.get("posCode").toString();
	    String shiftNo = hm.get("shiftNo").toString();
	    String posName = hm.get("posName").toString();
	    String groupCode = hm.get("GroupCode").toString();
	    String groupName = hm.get("GroupName").toString();
	    String costCenterCode = hm.get("costCenterCode").toString();
	    String costCenterName = hm.get("costCenterName").toString();
	    String printZeroAmountModi = hm.get("PrintZeroAmountModi").toString();
	    String currency = hm.get("currency").toString();
	    String costCenterCd = "", costCenterNm = "";

	    int sqlNo = 0;
	    StringBuilder sbSql = new StringBuilder();
	    StringBuilder sbSqlMod = new StringBuilder();
	    StringBuilder sbFilters = new StringBuilder();
	    ResultSet rsSalesMod;
	    Map<String, clsItemWiseConsumption> hmItemWiseConsumption = new HashMap<String, clsItemWiseConsumption>();

	    // Code for Sales Qty for bill detail and bill modifier live & q data
	    // for Sales Qty for bill detail live data  
	    String amount = "SUM(b.dblamount)";
	    String rate = "b.dblRate";
	    String discAmt = "SUM(b.dblDiscountAmt)";
	    if(currency.equalsIgnoreCase("USD"))
	    {
		amount = "SUM(b.dblamount)/a.dblUSDConverionRate";
		rate = "b.dblRate/a.dblUSDConverionRate";
		discAmt = "SUM(b.dblDiscountAmt)/a.dblUSDConverionRate";
	    }
	    sbSql.setLength(0);
	    
	    sbSql.append("SELECT b.stritemcode,upper(b.stritemname), SUM(b.dblQuantity), "+amount+","+rate+", e.strposname,"+discAmt+",g.strSubGroupName,h.strGroupName,a.strBillNo,f.strExternalCode "
		    + ",i.strCostCenterCode,j.strCostCenterName "
		    + "FROM tblbillhd a,tblbilldtl b, tblposmaster e,tblitemmaster f,tblsubgrouphd g,tblgrouphd h,tblmenuitempricingdtl i,tblcostcentermaster j "
		    + "WHERE a.strBillNo=b.strBillNo  "
		    + "AND DATE(a.dteBillDate)= DATE(b.dteBillDate)  "
		    + "AND a.strPOSCode=e.strPosCode  "
		    + "AND b.strItemCode=f.strItemCode  "
		    + "AND f.strSubGroupCode=g.strSubGroupCode  "
		    + "AND g.strGroupCode=h.strGroupCode  "
		    + "and b.strItemCode=i.strItemCode "
		    + "and (a.strPOSCode=i.strPosCode or i.strPosCode='All') "
		    + " and i.strHourlyPricing='NO' ");
	    if (clsGlobalVarClass.gAreaWisePricing.equalsIgnoreCase("Y"))
	    {
		sbSql.append("and (a.strAreaCode=i.strAreaCode ) ");
	    }

	    sbSql.append("and i.strCostCenterCode=j.strCostCenterCode "
		    + "AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' ");

	    if (!posCode.equals("All"))
	    {
		sbSql.append(" and a.strPOSCode = '" + posCode + "' ");
	    }

	    if (!groupCode.equalsIgnoreCase("All"))
	    {
		sbSql.append(" and h.strGroupCode = '" + groupCode + "' ");
	    }
	    if (!costCenterCode.equalsIgnoreCase("All"))
	    {
		sbSql.append(" and j.strCostCenterCode = '" + costCenterCode + "' ");
	    }

	    if (clsGlobalVarClass.gEnableShiftYN)
	    {
		if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
		{
		    sbSql.append(" and a.intShiftCode = '" + shiftNo + "' ");
		}
	    }
	    sbSql.append(" group by b.strItemCode,a.strBillNo "
		    + "  order by j.strCostCenterCode,b.strItemName");
	    ResultSet rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
	    while (rsSales.next())
	    {
		clsItemWiseConsumption objItemWiseConsumption = null;
		if (null != hmItemWiseConsumption.get(rsSales.getString(1) + "!" + rsSales.getString(2)))
		{
		    objItemWiseConsumption = hmItemWiseConsumption.get(rsSales.getString(1) + "!" + rsSales.getString(2));
		    objItemWiseConsumption.setSaleQty(objItemWiseConsumption.getSaleQty() + rsSales.getDouble(3));
		    objItemWiseConsumption.setSaleAmt(objItemWiseConsumption.getSaleAmt() + (rsSales.getDouble(4) - rsSales.getDouble(7)));
		    objItemWiseConsumption.setSubTotal(objItemWiseConsumption.getSubTotal() + rsSales.getDouble(4));
		    //objItemWiseConsumption.setTotalQty(objItemWiseConsumption.getTotalQty() + rsSales.getDouble(3));
		}
		else
		{
		    sqlNo++;
		    objItemWiseConsumption = new clsItemWiseConsumption();
		    objItemWiseConsumption.setItemCode(rsSales.getString(1));
		    objItemWiseConsumption.setItemName(rsSales.getString(2));
		    objItemWiseConsumption.setSubGroupName(rsSales.getString(8));
		    objItemWiseConsumption.setGroupName(rsSales.getString(9));
		    objItemWiseConsumption.setSaleQty(rsSales.getDouble(3));
		    objItemWiseConsumption.setComplimentaryQty(0);
		    objItemWiseConsumption.setNcQty(0);
		    objItemWiseConsumption.setSubTotal(rsSales.getDouble(4));
		    objItemWiseConsumption.setDiscAmt(rsSales.getDouble(7));
		    objItemWiseConsumption.setSaleAmt(rsSales.getDouble(4) - rsSales.getDouble(7));
		    objItemWiseConsumption.setPOSName(rsSales.getString(6));
		    objItemWiseConsumption.setPromoQty(0);
		    objItemWiseConsumption.setSeqNo(sqlNo);
		    objItemWiseConsumption.setCostCenterCode(rsSales.getString(12));
		    objItemWiseConsumption.setCostCenterName(rsSales.getString(13));
		    costCenterCd = rsSales.getString(12);
		    costCenterNm = rsSales.getString(13);
		    objItemWiseConsumption.setExternalCode(rsSales.getString(11));
		    double totalRowQty = rsSales.getDouble(3) + 0 + 0 + 0;
		    //objItemWiseConsumption.setTotalQty(totalRowQty);
		    objItemWiseConsumption.setTotalQty(0);
		    objItemWiseConsumption.setItemRate(rsSales.getDouble(5));
		}
		if (null != objItemWiseConsumption)
		{
		    hmItemWiseConsumption.put(rsSales.getString(1) + "!" + rsSales.getString(2), objItemWiseConsumption);
		}
		sbSqlMod.setLength(0);
		String dblDiscAmount = "b.dblDiscAmt";
		if(currency.equalsIgnoreCase("USD"))
		{    
		dblDiscAmount = "b.dblDiscAmt/a.dblUSDConverionRate";
		}
		if (printZeroAmountModi.equalsIgnoreCase("Yes"))
		{
		    //for Sales Qty for bill modifier live data 

		    sbSqlMod.append("select b.strItemCode,upper(b.strModifierName),b.dblQuantity,"+amount+","+rate+""
			    + " ,e.strposname,"+dblDiscAmount+",g.strSubGroupName,h.strGroupName,a.strBillNo,f.strExternalCode "
			    + " from tblbillhd a,tblbillmodifierdtl b, tblbillsettlementdtl c,tblsettelmenthd d,tblposmaster e"
			    + " ,tblitemmaster f,tblsubgrouphd g,tblgrouphd h "
			    + " where a.strBillNo=b.strBillNo "
			    + " and date(a.dteBillDate)=date(b.dteBillDate) "
			    + " and a.strBillNo=c.strBillNo "
			    + " and date(a.dteBillDate)=date(c.dteBillDate) "
			    + " and c.strSettlementCode=d.strSettelmentCode "
			    + " and a.strPOSCode=e.strPosCode "
			    + " and left(b.strItemCode,7)=f.strItemCode "
			    + " and f.strSubGroupCode=g.strSubGroupCode "
			    + " and g.strGroupCode=h.strGroupCode "
			    + " and d.strSettelmentType!='Complementary' "
			    + " and left(b.strItemCode,7)='" + rsSales.getString(1) + "' and a.strBillNo='" + rsSales.getString(10) + "' "
			    + " and date(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
			    + " group by b.strItemCode,b.strModifierName ");
		}
		else
		{
		    sbSqlMod.append("select b.strItemCode,upper(b.strModifierName),b.dblQuantity,"+amount+","+rate+""
			    + " ,e.strposname,"+dblDiscAmount+",g.strSubGroupName,h.strGroupName,a.strBillNo,f.strExternalCode "
			    + " from tblbillhd a,tblbillmodifierdtl b, tblbillsettlementdtl c,tblsettelmenthd d,tblposmaster e"
			    + " ,tblitemmaster f,tblsubgrouphd g,tblgrouphd h "
			    + " where a.strBillNo=b.strBillNo "
			    + " and date(a.dteBillDate)=date(b.dteBillDate) "
			    + " and a.strBillNo=c.strBillNo "
			    + " and date(a.dteBillDate)=date(c.dteBillDate) "
			    + " and c.strSettlementCode=d.strSettelmentCode "
			    + " and a.strPOSCode=e.strPosCode "
			    + " and left(b.strItemCode,7)=f.strItemCode "
			    + " and f.strSubGroupCode=g.strSubGroupCode "
			    + " and g.strGroupCode=h.strGroupCode "
			    + " and d.strSettelmentType!='Complementary' "
			    + " and left(b.strItemCode,7)='" + rsSales.getString(1) + "' and a.strBillNo='" + rsSales.getString(10) + "' "
			    + " and date(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' AND b.dblamount>0"
			    + " group by b.strItemCode,b.strModifierName ");
		}

		rsSalesMod = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlMod.toString());
		while (rsSalesMod.next())
		{
		    // clsItemWiseConsumption objItemWiseConsumption=null;
		    if (null != hmItemWiseConsumption.get(rsSalesMod.getString(1) + "!" + rsSalesMod.getString(2)))
		    {
			objItemWiseConsumption = hmItemWiseConsumption.get(rsSalesMod.getString(1) + "!" + rsSalesMod.getString(2));
			objItemWiseConsumption.setSaleQty(objItemWiseConsumption.getSaleQty() + rsSalesMod.getDouble(3));
			objItemWiseConsumption.setSaleAmt(objItemWiseConsumption.getSaleAmt() + ((rsSalesMod.getDouble(4)) - rsSalesMod.getDouble(7)));
			objItemWiseConsumption.setSubTotal(objItemWiseConsumption.getSubTotal() + rsSalesMod.getDouble(4));
			//objItemWiseConsumption.setTotalQty(objItemWiseConsumption.getTotalQty() + rsSalesMod.getDouble(3));
		    }
		    else
		    {
			sqlNo++;
			objItemWiseConsumption = new clsItemWiseConsumption();
			objItemWiseConsumption.setItemCode(rsSalesMod.getString(1));
			objItemWiseConsumption.setItemName(rsSalesMod.getString(2));
			objItemWiseConsumption.setSubGroupName(rsSalesMod.getString(8));
			objItemWiseConsumption.setGroupName(rsSalesMod.getString(9));
			objItemWiseConsumption.setSaleQty(rsSalesMod.getDouble(3));
			objItemWiseConsumption.setComplimentaryQty(0);
			objItemWiseConsumption.setNcQty(0);
			objItemWiseConsumption.setSubTotal(rsSalesMod.getDouble(4));
			objItemWiseConsumption.setDiscAmt(rsSalesMod.getDouble(7));
			objItemWiseConsumption.setSaleAmt(rsSalesMod.getDouble(4) - rsSalesMod.getDouble(7));
			objItemWiseConsumption.setPOSName(rsSalesMod.getString(6));
			objItemWiseConsumption.setPromoQty(0);
			objItemWiseConsumption.setSeqNo(sqlNo);
			objItemWiseConsumption.setCostCenterCode(costCenterCd);
			objItemWiseConsumption.setCostCenterName(costCenterNm);
			objItemWiseConsumption.setExternalCode(rsSalesMod.getString(11));
			double totalRowQty = rsSalesMod.getDouble(3) + 0 + 0 + 0;
			//objItemWiseConsumption.setTotalQty(totalRowQty);
			objItemWiseConsumption.setTotalQty(0);
			objItemWiseConsumption.setItemRate(rsSalesMod.getDouble(5));
		    }
		    if (null != objItemWiseConsumption)
		    {
			hmItemWiseConsumption.put(rsSalesMod.getString(1) + "!" + rsSalesMod.getString(2), objItemWiseConsumption);
		    }

		}
		rsSalesMod.close();

	    }
	    rsSales.close();

	    // for Sales Qty for bill detail q data 
	    sbSql.setLength(0);
	    sbSql.append("SELECT b.stritemcode,upper(b.stritemname), SUM(b.dblQuantity), "+amount+","+rate+", e.strposname,"+discAmt+",g.strSubGroupName,h.strGroupName,a.strBillNo,f.strExternalCode "
		    + ",i.strCostCenterCode,j.strCostCenterName "
		    + "FROM tblqbillhd a,tblqbilldtl b, tblposmaster e,tblitemmaster f,tblsubgrouphd g,tblgrouphd h,tblmenuitempricingdtl i,tblcostcentermaster j "
		    + "WHERE a.strBillNo=b.strBillNo  "
		    + "AND DATE(a.dteBillDate)= DATE(b.dteBillDate)  "
		    + "AND a.strPOSCode=e.strPosCode  "
		    + "AND b.strItemCode=f.strItemCode  "
		    + "AND f.strSubGroupCode=g.strSubGroupCode  "
		    + "AND g.strGroupCode=h.strGroupCode  "
		    + "and b.strItemCode=i.strItemCode "
		    + "and (a.strPOSCode=i.strPosCode or i.strPosCode='All') "
		    + " and i.strHourlyPricing='NO' ");
	    if (clsGlobalVarClass.gAreaWisePricing.equalsIgnoreCase("Y"))
	    {
		sbSql.append("and (a.strAreaCode=i.strAreaCode ) ");
	    }
	    sbSql.append("and i.strCostCenterCode=j.strCostCenterCode "
		    + "AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' ");

	    if (!posCode.equals("All"))
	    {
		sbSql.append(" and a.strPOSCode = '" + posCode + "' ");
	    }

	    if (!groupCode.equalsIgnoreCase("All"))
	    {
		sbSql.append(" and h.strGroupCode = '" + groupCode + "' ");
	    }

	    if (!costCenterCode.equalsIgnoreCase("All"))
	    {
		sbSql.append(" and j.strCostCenterCode = '" + costCenterCode + "' ");
	    }

	    if (clsGlobalVarClass.gEnableShiftYN)
	    {
		if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
		{
		    sbSql.append(" and a.intShiftCode = '" + shiftNo + "' ");
		}
	    }
	    sbSql.append(" group by b.strItemCode order by j.strCostCenterCode,b.strItemName");

	    rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
	    while (rsSales.next())
	    {
		clsItemWiseConsumption objItemWiseConsumption = null;
		if (null != hmItemWiseConsumption.get(rsSales.getString(1) + "!" + rsSales.getString(2)))
		{
		    objItemWiseConsumption = hmItemWiseConsumption.get(rsSales.getString(1) + "!" + rsSales.getString(2));
		    objItemWiseConsumption.setSaleQty(objItemWiseConsumption.getSaleQty() + rsSales.getDouble(3));
		    objItemWiseConsumption.setSaleAmt(objItemWiseConsumption.getSaleAmt() + (rsSales.getDouble(4) - rsSales.getDouble(7)));
		    objItemWiseConsumption.setSubTotal(objItemWiseConsumption.getSubTotal() + rsSales.getDouble(4));
		    //objItemWiseConsumption.setTotalQty(objItemWiseConsumption.getTotalQty() + rsSales.getDouble(3));
		}
		else
		{
		    sqlNo++;
		    objItemWiseConsumption = new clsItemWiseConsumption();
		    objItemWiseConsumption.setItemCode(rsSales.getString(1));
		    objItemWiseConsumption.setItemName(rsSales.getString(2));
		    objItemWiseConsumption.setSubGroupName(rsSales.getString(8));
		    objItemWiseConsumption.setGroupName(rsSales.getString(9));
		    objItemWiseConsumption.setSaleQty(rsSales.getDouble(3));
		    objItemWiseConsumption.setComplimentaryQty(0);
		    objItemWiseConsumption.setNcQty(0);
		    objItemWiseConsumption.setSubTotal(rsSales.getDouble(4));
		    objItemWiseConsumption.setDiscAmt(rsSales.getDouble(7));
		    objItemWiseConsumption.setSaleAmt(rsSales.getDouble(4) - rsSales.getDouble(7));
		    objItemWiseConsumption.setPOSName(rsSales.getString(6));
		    objItemWiseConsumption.setPromoQty(0);
		    objItemWiseConsumption.setSeqNo(sqlNo);
		    objItemWiseConsumption.setCostCenterCode(rsSales.getString(12));
		    objItemWiseConsumption.setCostCenterName(rsSales.getString(13));
		    costCenterCd = rsSales.getString(12);
		    costCenterNm = rsSales.getString(13);
		    objItemWiseConsumption.setExternalCode(rsSales.getString(11));
		    double totalRowQty = rsSales.getDouble(3) + 0 + 0 + 0;
		    //objItemWiseConsumption.setTotalQty(totalRowQty);
		    objItemWiseConsumption.setTotalQty(0);
		    objItemWiseConsumption.setItemRate(rsSales.getDouble(5));
		}
		if (null != objItemWiseConsumption)
		{
		    hmItemWiseConsumption.put(rsSales.getString(1) + "!" + rsSales.getString(2), objItemWiseConsumption);
		}
		sbSqlMod.setLength(0);
		String dblDiscAmount = "b.dblDiscAmt";
		if(currency.equalsIgnoreCase("USD"))
		{    
		dblDiscAmount = "b.dblDiscAmt/a.dblUSDConverionRate";
		}
		if (printZeroAmountModi.equalsIgnoreCase("Yes"))//Tjs brew works dont want modifiers details
		{
		    // Code for Sales Qty for modifier live & q data
		    
		    sbSqlMod.append("select b.strItemCode,upper(b.strModifierName),b.dblQuantity,"+amount+","+rate+""
			    + " ,e.strposname,"+dblDiscAmount+",g.strSubGroupName,h.strGroupName,a.strBillNo,f.strExternalCode"
			    + " from tblqbillhd a,tblqbillmodifierdtl b, tblqbillsettlementdtl c,tblsettelmenthd d,tblposmaster e "
			    + " ,tblitemmaster f,tblsubgrouphd g,tblgrouphd h "
			    + " where a.strBillNo=b.strBillNo "
			    + " and date(a.dteBillDate)=date(b.dteBillDate) "
			    + " and a.strBillNo=c.strBillNo "
			    + " and date(a.dteBillDate)=date(c.dteBillDate) "
			    + " and c.strSettlementCode=d.strSettelmentCode "
			    + " and a.strPOSCode=e.strPosCode "
			    + " and left(b.strItemCode,7)=f.strItemCode "
			    + " and f.strSubGroupCode=g.strSubGroupCode "
			    + " and g.strGroupCode=h.strGroupCode "
			    + " and d.strSettelmentType!='Complementary' "
			    + " and left(b.strItemCode,7)='" + rsSales.getString(1) + "' "
			    + " and a.strBillNo='" + rsSales.getString(10) + "' "
			    + " and date(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
			    + " group by b.strItemCode,b.strModifierName ");
		}
		else
		{
		    sbSqlMod.append("select b.strItemCode,upper(b.strModifierName),b.dblQuantity,"+amount+","+rate+""
			    + " ,e.strposname,"+dblDiscAmount+",g.strSubGroupName,h.strGroupName,a.strBillNo,f.strExternalCode"
			    + " from tblqbillhd a,tblqbillmodifierdtl b, tblqbillsettlementdtl c,tblsettelmenthd d,tblposmaster e "
			    + " ,tblitemmaster f,tblsubgrouphd g,tblgrouphd h "
			    + " where a.strBillNo=b.strBillNo "
			    + " and date(a.dteBillDate)=date(b.dteBillDate) "
			    + " and a.strBillNo=c.strBillNo "
			    + " and date(a.dteBillDate)=date(c.dteBillDate) "
			    + " and c.strSettlementCode=d.strSettelmentCode "
			    + " and a.strPOSCode=e.strPosCode "
			    + " and left(b.strItemCode,7)=f.strItemCode "
			    + " and f.strSubGroupCode=g.strSubGroupCode "
			    + " and g.strGroupCode=h.strGroupCode "
			    + " and d.strSettelmentType!='Complementary' "
			    + " and left(b.strItemCode,7)='" + rsSales.getString(1) + "' "
			    + " and a.strBillNo='" + rsSales.getString(10) + "' "
			    + " and date(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' And  b.dblamount >0"
			    + " group by b.strItemCode,b.strModifierName ");
		}
		sbSqlMod.append(sbFilters);

		rsSalesMod = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlMod.toString());
		while (rsSalesMod.next())
		{

		    if (null != hmItemWiseConsumption.get(rsSalesMod.getString(1) + "!" + rsSalesMod.getString(2)))
		    {
			objItemWiseConsumption = hmItemWiseConsumption.get(rsSalesMod.getString(1) + "!" + rsSalesMod.getString(2));
			objItemWiseConsumption.setSaleQty(objItemWiseConsumption.getSaleQty() + rsSalesMod.getDouble(3));
			objItemWiseConsumption.setSaleAmt(objItemWiseConsumption.getSaleAmt() + (rsSalesMod.getDouble(4) - rsSalesMod.getDouble(7)));
			objItemWiseConsumption.setSubTotal(objItemWiseConsumption.getSubTotal() + rsSalesMod.getDouble(4));
			//objItemWiseConsumption.setTotalQty(objItemWiseConsumption.getTotalQty() + rsSalesMod.getDouble(3));
		    }
		    else
		    {
			sqlNo++;
			objItemWiseConsumption = new clsItemWiseConsumption();
			objItemWiseConsumption.setItemCode(rsSalesMod.getString(1));
			objItemWiseConsumption.setItemName(rsSalesMod.getString(2));
			objItemWiseConsumption.setSubGroupName(rsSalesMod.getString(8));
			objItemWiseConsumption.setGroupName(rsSalesMod.getString(9));
			objItemWiseConsumption.setSaleQty(rsSalesMod.getDouble(3));
			objItemWiseConsumption.setComplimentaryQty(0);
			objItemWiseConsumption.setNcQty(0);
			objItemWiseConsumption.setSubTotal(rsSalesMod.getDouble(4));
			objItemWiseConsumption.setDiscAmt(rsSalesMod.getDouble(7));
			objItemWiseConsumption.setSaleAmt(rsSalesMod.getDouble(4) - rsSalesMod.getDouble(7));
			objItemWiseConsumption.setPOSName(rsSalesMod.getString(6));
			objItemWiseConsumption.setPromoQty(0);
			objItemWiseConsumption.setSeqNo(sqlNo);
			objItemWiseConsumption.setCostCenterCode(costCenterCd);
			objItemWiseConsumption.setCostCenterName(costCenterNm);
			objItemWiseConsumption.setExternalCode(rsSalesMod.getString(11));
			double totalRowQty = rsSalesMod.getDouble(3) + 0 + 0 + 0;
			//objItemWiseConsumption.setTotalQty(totalRowQty);
			objItemWiseConsumption.setTotalQty(0);
			objItemWiseConsumption.setItemRate(rsSalesMod.getDouble(5));
		    }
		    if (null != objItemWiseConsumption)
		    {
			hmItemWiseConsumption.put(rsSalesMod.getString(1) + "!" + rsSalesMod.getString(2), objItemWiseConsumption);
		    }
		}
		rsSalesMod.close();

	    }
	    rsSales.close();

	    // Code for Complimentary Qty for live & q bill detail and bill modifier data   
	    //for Complimentary Qty for live bill detail
	    sbSql.setLength(0);

	    sbSql.append("SELECT b.stritemcode,upper(b.stritemname), SUM(b.dblQuantity), "+amount+","+rate+",e.strposname,"+discAmt+",g.strSubGroupName,h.strGroupName,a.strBillNo,f.strExternalCode "
		    + ",i.strCostCenterCode,j.strCostCenterName "
		    + "FROM tblbillhd a,tblbillcomplementrydtl b,tblposmaster e,tblitemmaster f,tblsubgrouphd g,tblgrouphd h,tblmenuitempricingdtl i,tblcostcentermaster j "
		    + "WHERE a.strBillNo=b.strBillNo  "
		    + "AND DATE(a.dteBillDate)= DATE(b.dteBillDate)  "
		    + "AND a.strPOSCode=e.strPosCode  "
		    + "AND b.strItemCode=f.strItemCode  "
		    + "AND f.strSubGroupCode=g.strSubGroupCode  "
		    + "AND g.strGroupCode=h.strGroupCode  "
		    + "and b.strItemCode=i.strItemCode "
		    + "and (a.strPOSCode=i.strPosCode or i.strPosCode='All') "
		    + " and i.strHourlyPricing='NO' ");
	    if (clsGlobalVarClass.gAreaWisePricing.equalsIgnoreCase("Y"))
	    {
		sbSql.append("and (a.strAreaCode=i.strAreaCode ) ");
	    }
	    sbSql.append("and i.strCostCenterCode=j.strCostCenterCode "
		    + "AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' ");

	    if (!posCode.equals("All"))
	    {
		sbSql.append(" and a.strPOSCode = '" + posCode + "' ");
	    }

	    if (!groupCode.equalsIgnoreCase("All"))
	    {
		sbSql.append(" and h.strGroupCode = '" + groupCode + "' ");
	    }

	    if (!costCenterCode.equalsIgnoreCase("All"))
	    {
		sbSql.append(" and j.strCostCenterCode = '" + costCenterCode + "' ");
	    }

	    if (clsGlobalVarClass.gEnableShiftYN)
	    {
		if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
		{
		    sbSql.append(" and a.intShiftCode = '" + shiftNo + "' ");
		}
	    }
	    sbSql.append(" group by b.strItemCode order by j.strCostCenterCode,b.strItemName");
	    //System.out.println(sbSql);

	    ResultSet rsComplimentary = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
	    while (rsComplimentary.next())
	    {
		clsItemWiseConsumption objItemWiseConsumption = null;
		if (null != hmItemWiseConsumption.get(rsComplimentary.getString(1) + "!" + rsComplimentary.getString(2)))
		{
		    objItemWiseConsumption = hmItemWiseConsumption.get(rsComplimentary.getString(1) + "!" + rsComplimentary.getString(2));
		    objItemWiseConsumption.setComplimentaryQty(objItemWiseConsumption.getComplimentaryQty() + rsComplimentary.getDouble(3));

		    objItemWiseConsumption.setSaleQty(objItemWiseConsumption.getSaleQty() - rsComplimentary.getDouble(3));

		}
		else
		{
		    sqlNo++;
		    objItemWiseConsumption = new clsItemWiseConsumption();
		    objItemWiseConsumption.setItemCode(rsComplimentary.getString(1));
		    objItemWiseConsumption.setItemName(rsComplimentary.getString(2));
		    objItemWiseConsumption.setSubGroupName(rsComplimentary.getString(8));
		    objItemWiseConsumption.setGroupName(rsComplimentary.getString(9));
		    objItemWiseConsumption.setComplimentaryQty(rsComplimentary.getDouble(3));
		    objItemWiseConsumption.setSaleQty(0);
		    objItemWiseConsumption.setNcQty(0);

		    objItemWiseConsumption.setPOSName(rsComplimentary.getString(6));
		    objItemWiseConsumption.setPromoQty(0);
		    objItemWiseConsumption.setSeqNo(sqlNo);
		    objItemWiseConsumption.setCostCenterCode(rsComplimentary.getString(12));
		    objItemWiseConsumption.setCostCenterName(rsComplimentary.getString(13));
		    costCenterCd = rsComplimentary.getString(12);
		    costCenterNm = rsComplimentary.getString(13);
		    objItemWiseConsumption.setExternalCode(rsComplimentary.getString(11));
		    double totalRowQty = rsComplimentary.getDouble(3) + 0 + 0 + 0;
		    //objItemWiseConsumption.setTotalQty(totalRowQty);
		    objItemWiseConsumption.setTotalQty(0);
		    objItemWiseConsumption.setItemRate(rsComplimentary.getDouble(5));
		    ///System.out.println("New= " + rsComplimentary.getString(1) + objItemWiseConsumption.getComplimentaryQty());
		}
		if (null != objItemWiseConsumption)
		{
		    hmItemWiseConsumption.put(rsComplimentary.getString(1) + "!" + rsComplimentary.getString(2), objItemWiseConsumption);
		}

		sbSqlMod.setLength(0);
		String dblDiscAmount = "b.dblDiscAmt";
		if(currency.equalsIgnoreCase("USD"))
		{    
		dblDiscAmount = "b.dblDiscAmt/a.dblUSDConverionRate";
		}
		if (printZeroAmountModi.equalsIgnoreCase("Yes"))//Tjs brew works dont want modifiers details
		{
		    //for Complimentary Qty for live bill modifier

		    sbSqlMod.append("select b.strItemCode,upper(b.strModifierName),b.dblQuantity,"+amount+","+rate+""
			    + " ,e.strposname,"+dblDiscAmount+",g.strSubGroupName,h.strGroupName,a.strBillNo,f.strExternalCode "
			    + " from tblbillhd a,tblbillmodifierdtl b, tblbillsettlementdtl c,tblsettelmenthd d,tblposmaster e "
			    + " ,tblitemmaster f,tblsubgrouphd g,tblgrouphd h "
			    + " where a.strBillNo=b.strBillNo "
			    + " and date(a.dteBillDate)=date(b.dteBillDate) "
			    + " and a.strBillNo=c.strBillNo  "
			    + " and date(a.dteBillDate)=date(c.dteBillDate) "
			    + " and c.strSettlementCode=d.strSettelmentCode "
			    + " and a.strPOSCode=e.strPosCode "
			    + " and left(b.strItemCode,7)=f.strItemCode "
			    + " and f.strSubGroupCode=g.strSubGroupCode "
			    + " and g.strGroupCode=h.strGroupCode "
			    + " and d.strSettelmentType='Complementary' "
			    + " and left(b.strItemCode,7)='" + rsComplimentary.getString(1) + "' "
			    + " and a.strBillNo='" + rsComplimentary.getString(10) + "' "
			    + " and date(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
			    + " group by b.strItemCode,b.strModifierName ");
		}
		else
		{
		    sbSqlMod.append("select b.strItemCode,upper(b.strModifierName),b.dblQuantity,"+amount+","+rate+""
			    + " ,e.strposname,"+dblDiscAmount+",g.strSubGroupName,h.strGroupName,a.strBillNo,f.strExternalCode "
			    + " from tblbillhd a,tblbillmodifierdtl b, tblbillsettlementdtl c,tblsettelmenthd d,tblposmaster e "
			    + " ,tblitemmaster f,tblsubgrouphd g,tblgrouphd h "
			    + " where a.strBillNo=b.strBillNo "
			    + " and date(a.dteBillDate)=date(b.dteBillDate) "
			    + " and a.strBillNo=c.strBillNo "
			    + " and date(a.dteBillDate)=date(c.dteBillDate) "
			    + " and c.strSettlementCode=d.strSettelmentCode "
			    + " and a.strPOSCode=e.strPosCode "
			    + " and left(b.strItemCode,7)=f.strItemCode "
			    + " and f.strSubGroupCode=g.strSubGroupCode "
			    + " and g.strGroupCode=h.strGroupCode "
			    + " and d.strSettelmentType='Complementary' "
			    + " and left(b.strItemCode,7)='" + rsComplimentary.getString(1) + "' "
			    + " and a.strBillNo='" + rsComplimentary.getString(10) + "' "
			    + " and date(a.dteBillDate) BETWEEN '" + fromDate + "' "
			    + " AND '" + toDate + "' AND  b.dblamount >0"
			    + " group by b.strItemCode,b.strModifierName ");
		}
		sbSqlMod.append(sbFilters);
		//System.out.println(sbSqlMod);

		ResultSet rsModComplimentary = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlMod.toString());
		while (rsModComplimentary.next())
		{
		    if (null != hmItemWiseConsumption.get(rsModComplimentary.getString(1) + "!" + rsModComplimentary.getString(2)))
		    {
			objItemWiseConsumption = hmItemWiseConsumption.get(rsModComplimentary.getString(1) + "!" + rsModComplimentary.getString(2));
			objItemWiseConsumption.setComplimentaryQty(objItemWiseConsumption.getComplimentaryQty() + rsModComplimentary.getDouble(3));

		    }
		    else
		    {
			sqlNo++;
			objItemWiseConsumption = new clsItemWiseConsumption();
			objItemWiseConsumption.setItemCode(rsModComplimentary.getString(1));
			objItemWiseConsumption.setItemName(rsModComplimentary.getString(2));
			objItemWiseConsumption.setSubGroupName(rsModComplimentary.getString(8));
			objItemWiseConsumption.setGroupName(rsModComplimentary.getString(9));
			objItemWiseConsumption.setComplimentaryQty(rsModComplimentary.getDouble(3));
			objItemWiseConsumption.setSaleQty(0);
			objItemWiseConsumption.setNcQty(0);

			objItemWiseConsumption.setPOSName(rsModComplimentary.getString(6));
			objItemWiseConsumption.setSeqNo(sqlNo);
			objItemWiseConsumption.setPromoQty(0);
			objItemWiseConsumption.setCostCenterCode(costCenterCd);
			objItemWiseConsumption.setCostCenterName(costCenterNm);
			objItemWiseConsumption.setExternalCode(rsModComplimentary.getString(11));
			//System.out.println("New= " + rsModComplimentary.getString(1) + objItemWiseConsumption.getComplimentaryQty());
			double totalRowQty = rsModComplimentary.getDouble(3) + 0 + 0 + 0;
			//objItemWiseConsumption.setTotalQty(totalRowQty);
			objItemWiseConsumption.setTotalQty(0);
			objItemWiseConsumption.setItemRate(rsModComplimentary.getDouble(5));
		    }
		    if (null != objItemWiseConsumption)
		    {
			hmItemWiseConsumption.put(rsModComplimentary.getString(1) + "!" + rsModComplimentary.getString(2), objItemWiseConsumption);
		    }
		}
		rsModComplimentary.close();

	    }
	    rsComplimentary.close();

	    //for Complimentary Qty for q bill details
	    sbSql.setLength(0);

	    sbSql.append("SELECT b.stritemcode,upper(b.stritemname), SUM(b.dblQuantity),"+amount+","+rate+",e.strposname,"+discAmt+""
		    + ",g.strSubGroupName,h.strGroupName,a.strBillNo,f.strExternalCode "
		    + ",i.strCostCenterCode,j.strCostCenterName "
		    + "FROM tblqbillhd a,tblqbillcomplementrydtl b,tblposmaster e,tblitemmaster f,tblsubgrouphd g,tblgrouphd h,tblmenuitempricingdtl i,tblcostcentermaster j "
		    + "WHERE a.strBillNo=b.strBillNo  "
		    + "AND DATE(a.dteBillDate)= DATE(b.dteBillDate)  "
		    + "AND a.strPOSCode=e.strPosCode  "
		    + "AND b.strItemCode=f.strItemCode  "
		    + "AND f.strSubGroupCode=g.strSubGroupCode  "
		    + "AND g.strGroupCode=h.strGroupCode  "
		    + "and b.strItemCode=i.strItemCode "
		    + "and (a.strPOSCode=i.strPosCode or i.strPosCode='All') "
		    + " and i.strHourlyPricing='NO' ");
	    if (clsGlobalVarClass.gAreaWisePricing.equalsIgnoreCase("Y"))
	    {
		sbSql.append("and (a.strAreaCode=i.strAreaCode ) ");
	    }
	    sbSql.append("and i.strCostCenterCode=j.strCostCenterCode "
		    + "AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' ");
	    if (!posCode.equals("All"))
	    {
		sbSql.append(" and a.strPOSCode = '" + posCode + "' ");
	    }

	    if (!groupCode.equalsIgnoreCase("All"))
	    {
		sbSql.append(" and h.strGroupCode = '" + groupCode + "' ");
	    }

	    if (!costCenterCode.equalsIgnoreCase("All"))
	    {
		sbSql.append(" and j.strCostCenterCode = '" + costCenterCode + "' ");
	    }

	    if (clsGlobalVarClass.gEnableShiftYN)
	    {
		if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
		{
		    sbSql.append(" and a.intShiftCode = '" + shiftNo + "' ");
		}
	    }
	    sbSql.append(" group by b.strItemCode order by j.strCostCenterCode,b.strItemName");
	    //System.out.println(sbSql);

	    rsComplimentary = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
	    while (rsComplimentary.next())
	    {
		clsItemWiseConsumption objItemWiseConsumption = null;
		if (null != hmItemWiseConsumption.get(rsComplimentary.getString(1) + "!" + rsComplimentary.getString(2)))
		{
		    objItemWiseConsumption = hmItemWiseConsumption.get(rsComplimentary.getString(1) + "!" + rsComplimentary.getString(2));
		    objItemWiseConsumption.setComplimentaryQty(objItemWiseConsumption.getComplimentaryQty() + rsComplimentary.getDouble(3));

		    objItemWiseConsumption.setSaleQty(objItemWiseConsumption.getSaleQty() - rsComplimentary.getDouble(3));

		}
		else
		{
		    sqlNo++;
		    objItemWiseConsumption = new clsItemWiseConsumption();
		    objItemWiseConsumption.setItemCode(rsComplimentary.getString(1));
		    objItemWiseConsumption.setItemName(rsComplimentary.getString(2));
		    objItemWiseConsumption.setSubGroupName(rsComplimentary.getString(8));
		    objItemWiseConsumption.setGroupName(rsComplimentary.getString(9));
		    objItemWiseConsumption.setComplimentaryQty(rsComplimentary.getDouble(3));
		    objItemWiseConsumption.setSaleQty(0);
		    objItemWiseConsumption.setNcQty(0);

		    objItemWiseConsumption.setPOSName(rsComplimentary.getString(6));
		    objItemWiseConsumption.setPromoQty(0);
		    objItemWiseConsumption.setSeqNo(sqlNo);
		    objItemWiseConsumption.setCostCenterCode(rsComplimentary.getString(12));
		    objItemWiseConsumption.setCostCenterName(rsComplimentary.getString(13));
		    costCenterCd = rsComplimentary.getString(12);
		    costCenterNm = rsComplimentary.getString(13);
		    objItemWiseConsumption.setExternalCode(rsComplimentary.getString(11));
		    double totalRowQty = rsComplimentary.getDouble(3) + 0 + 0 + 0;
		    //objItemWiseConsumption.setTotalQty(totalRowQty);
		    objItemWiseConsumption.setTotalQty(0);
		    objItemWiseConsumption.setItemRate(rsComplimentary.getDouble(5));
		}
		if (null != objItemWiseConsumption)
		{
		    hmItemWiseConsumption.put(rsComplimentary.getString(1) + "!" + rsComplimentary.getString(2), objItemWiseConsumption);
		}

		sbSqlMod.setLength(0);
		String dblDiscAmount = "b.dblDiscAmt";
		if(currency.equalsIgnoreCase("USD"))
		{    
		dblDiscAmount = "b.dblDiscAmt/a.dblUSDConverionRate";
		}
		if (printZeroAmountModi.equalsIgnoreCase("Yes"))//Tjs brew works dont want modifiers details
		{
		    //for Complimentary Qty for q bill modifier 

		    sbSqlMod.append("select b.strItemCode,upper(b.strModifierName),b.dblQuantity,"+amount+","+rate+""
			    + " ,e.strposname,"+dblDiscAmount+",g.strSubGroupName,h.strGroupName,a.strBillNo,f.strExternalCode"
			    + " from tblqbillhd a,tblqbillmodifierdtl b, tblqbillsettlementdtl c,tblsettelmenthd d,tblposmaster e "
			    + " ,tblitemmaster f,tblsubgrouphd g,tblgrouphd h "
			    + " where a.strBillNo=b.strBillNo "
			    + " and date(a.dteBillDate)=date(b.dteBillDate) "
			    + " and a.strBillNo=c.strBillNo "
			    + " and date(a.dteBillDate)=date(c.dteBillDate) "
			    + " and c.strSettlementCode=d.strSettelmentCode "
			    + " and a.strPOSCode=e.strPosCode "
			    + " and left(b.strItemCode,7)=f.strItemCode "
			    + " and f.strSubGroupCode=g.strSubGroupCode "
			    + " and g.strGroupCode=h.strGroupCode "
			    + " and d.strSettelmentType='Complementary' "
			    + " and left(b.strItemCode,7)='" + rsComplimentary.getString(1) + "' "
			    + " and a.strBillNo='" + rsComplimentary.getString(10) + "' "
			    + " and date(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
			    + " group by b.strItemCode,b.strModifierName ");
		}
		else
		{
		    sbSqlMod.append("select b.strItemCode,upper(b.strModifierName),b.dblQuantity,"+amount+","+rate+""
			    + " ,e.strposname,"+dblDiscAmount+",g.strSubGroupName,h.strGroupName,a.strBillNo,f.strExternalCode"
			    + " from tblqbillhd a,tblqbillmodifierdtl b, tblqbillsettlementdtl c,tblsettelmenthd d,tblposmaster e "
			    + " ,tblitemmaster f,tblsubgrouphd g,tblgrouphd h "
			    + " where a.strBillNo=b.strBillNo "
			    + " and date(a.dteBillDate)=date(b.dteBillDate) "
			    + " and a.strBillNo=c.strBillNo "
			    + " and date(a.dteBillDate)=date(c.dteBillDate) "
			    + " and c.strSettlementCode=d.strSettelmentCode "
			    + " and a.strPOSCode=e.strPosCode "
			    + " and left(b.strItemCode,7)=f.strItemCode "
			    + " and f.strSubGroupCode=g.strSubGroupCode "
			    + " and g.strGroupCode=h.strGroupCode "
			    + " and d.strSettelmentType='Complementary' "
			    + " and left(b.strItemCode,7)='" + rsComplimentary.getString(1) + "' "
			    + " and a.strBillNo='" + rsComplimentary.getString(10) + "' "
			    + " and date(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
			    + " AND  b.dblamount >0"
			    + " group by b.strItemCode,b.strModifierName ");
		}
		sbSqlMod.append(sbFilters);
		//System.out.println(sbSqlMod);

		ResultSet rsModComplimentary = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlMod.toString());
		while (rsModComplimentary.next())
		{

		    if (null != hmItemWiseConsumption.get(rsModComplimentary.getString(1) + "!" + rsModComplimentary.getString(2)))
		    {
			objItemWiseConsumption = hmItemWiseConsumption.get(rsModComplimentary.getString(1) + "!" + rsModComplimentary.getString(2));
			objItemWiseConsumption.setComplimentaryQty(objItemWiseConsumption.getComplimentaryQty() + rsModComplimentary.getDouble(3));

		    }
		    else
		    {
			sqlNo++;
			objItemWiseConsumption = new clsItemWiseConsumption();
			objItemWiseConsumption.setItemCode(rsModComplimentary.getString(1));
			objItemWiseConsumption.setItemName(rsModComplimentary.getString(2));
			objItemWiseConsumption.setSubGroupName(rsModComplimentary.getString(8));
			objItemWiseConsumption.setGroupName(rsModComplimentary.getString(9));
			objItemWiseConsumption.setComplimentaryQty(rsModComplimentary.getDouble(3));
			objItemWiseConsumption.setSaleQty(0);
			objItemWiseConsumption.setNcQty(0);

			objItemWiseConsumption.setPOSName(rsModComplimentary.getString(6));
			objItemWiseConsumption.setPromoQty(0);
			objItemWiseConsumption.setSeqNo(sqlNo);
			objItemWiseConsumption.setCostCenterCode(costCenterCd);
			objItemWiseConsumption.setCostCenterName(costCenterNm);
			objItemWiseConsumption.setExternalCode(rsModComplimentary.getString(11));
			double totalRowQty = rsModComplimentary.getDouble(3) + 0 + 0 + 0;
			//objItemWiseConsumption.setTotalQty(totalRowQty);
			objItemWiseConsumption.setTotalQty(0);
			objItemWiseConsumption.setItemRate(rsModComplimentary.getDouble(5));
		    }
		    if (null != objItemWiseConsumption)
		    {
			hmItemWiseConsumption.put(rsModComplimentary.getString(1) + "!" + rsModComplimentary.getString(2), objItemWiseConsumption);
		    }
		}
		rsModComplimentary.close();

	    }
	    rsComplimentary.close();

	    // Code for NC Qty    
	    sbSql.setLength(0);
	    sbSql.append("SELECT a.stritemcode,upper(b.stritemname), SUM(a.dblQuantity), SUM(a.dblQuantity*a.dblRate),a.dblRate, c.strposname,0 AS DiscAmt,d.strSubGroupName,e.strGroupName,b.strExternalCode "
		    + ",i.strCostCenterCode,j.strCostCenterName "
		    + "FROM tblnonchargablekot a, tblitemmaster b, tblposmaster c,tblsubgrouphd d,tblgrouphd e,tblmenuitempricingdtl i,tblcostcentermaster j "
		    + "WHERE LEFT(a.strItemCode,7)=b.strItemCode  "
		    + "AND a.strPOSCode=c.strPosCode  "
		    + "AND b.strSubGroupCode=d.strSubGroupCode  "
		    + "AND d.strGroupCode=e.strGroupCode  "
		    + "and a.strItemCode=i.strItemCode "
		    + "and (a.strPOSCode=i.strPosCode or i.strPosCode='All') "
		    + "and i.strCostCenterCode=j.strCostCenterCode "
		    + "AND DATE(a.dteNCKOTDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
		    + " and i.strHourlyPricing='NO' ");
	    if (!posCode.equals("All"))
	    {
		sbSql.append(" AND a.strPOSCode = '" + posCode + "' ");
	    }

	    if (!groupCode.equalsIgnoreCase("All"))
	    {
		sbSql.append(" and e.strGroupCode = '" + groupCode + "' ");
	    }

	    if (!costCenterCode.equalsIgnoreCase("All"))
	    {
		sbSql.append(" and j.strCostCenterCode = '" + costCenterCode + "' ");
	    }

	    sbSql.append(" group by a.strItemCode order by j.strCostCenterCode,b.strItemName");
	    //System.out.println(sbSql);

	    ResultSet rsNCKOT = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
	    while (rsNCKOT.next())
	    {
		clsItemWiseConsumption objItemWiseConsumption = null;
		if (null != hmItemWiseConsumption.get(rsNCKOT.getString(1) + "!" + rsNCKOT.getString(2)))
		{
		    objItemWiseConsumption = hmItemWiseConsumption.get(rsNCKOT.getString(1) + "!" + rsNCKOT.getString(2));
		    objItemWiseConsumption.setNcQty(objItemWiseConsumption.getNcQty() + rsNCKOT.getDouble(3));

		}
		else
		{
		    sqlNo++;
		    objItemWiseConsumption = new clsItemWiseConsumption();
		    objItemWiseConsumption.setItemCode(rsNCKOT.getString(1));
		    objItemWiseConsumption.setItemName(rsNCKOT.getString(2));
		    objItemWiseConsumption.setSubGroupName(rsNCKOT.getString(8));
		    objItemWiseConsumption.setGroupName(rsNCKOT.getString(9));
		    objItemWiseConsumption.setNcQty(rsNCKOT.getDouble(3));
		    objItemWiseConsumption.setSaleQty(0);
		    objItemWiseConsumption.setComplimentaryQty(0);

		    objItemWiseConsumption.setPOSName(rsNCKOT.getString(6));
		    objItemWiseConsumption.setPromoQty(0);
		    objItemWiseConsumption.setSeqNo(sqlNo);
		    objItemWiseConsumption.setCostCenterCode(rsNCKOT.getString(11));
		    objItemWiseConsumption.setCostCenterName(rsNCKOT.getString(12));
		    costCenterCd = rsNCKOT.getString(11);
		    costCenterNm = rsNCKOT.getString(12);
		    objItemWiseConsumption.setExternalCode(rsNCKOT.getString(10));
		    double totalRowQty = rsNCKOT.getDouble(3) + 0 + 0 + 0;
		    //objItemWiseConsumption.setTotalQty(totalRowQty);
		    objItemWiseConsumption.setTotalQty(0);
		    objItemWiseConsumption.setItemRate(rsNCKOT.getDouble(5));
		}
		if (null != objItemWiseConsumption)
		{
		    hmItemWiseConsumption.put(rsNCKOT.getString(1) + "!" + rsNCKOT.getString(2), objItemWiseConsumption);
		}
	    }
	    rsNCKOT.close();

	    // Code for promotion Qty for Q
	    sbSql.setLength(0);
	    sbSql.append("SELECT b.strItemCode,upper(c.strItemName), SUM(b.dblQuantity), "+amount+","+rate+",f.strPosName,0,d.strSubGroupName,e.strGroupName,c.strExternalCode "
		    + ",i.strCostCenterCode,j.strCostCenterName "
		    + "FROM tblqbillhd a,tblqbillpromotiondtl b,tblitemmaster c,tblsubgrouphd d,tblgrouphd e,tblposmaster f,tblmenuitempricingdtl i,tblcostcentermaster j "
		    + "WHERE a.strBillNo=b.strBillNo  "
		    + "AND DATE(a.dteBillDate)= DATE(b.dteBillDate)  "
		    + "AND b.strItemCode=c.strItemCode  "
		    + "AND c.strSubGroupCode=d.strSubGroupCode  "
		    + "AND d.strGroupCode=e.strGroupCode  "
		    + "AND a.strPOSCode=f.strPosCode  "
		    + "and b.strItemCode=i.strItemCode "
		    + "and (a.strPOSCode=i.strPosCode or i.strPosCode='All') "
		    + " and i.strHourlyPricing='NO' ");
	    if (clsGlobalVarClass.gAreaWisePricing.equalsIgnoreCase("Y"))
	    {
		sbSql.append("and (a.strAreaCode=i.strAreaCode ) ");
	    }
	    sbSql.append("and i.strCostCenterCode=j.strCostCenterCode "
		    + "AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' ");
	    if (!posCode.equals("All"))
	    {
		sbSql.append(" AND a.strPOSCode = '" + posCode + "' ");
	    }

	    if (!groupCode.equalsIgnoreCase("All"))
	    {
		sbSql.append(" and e.strGroupCode = '" + groupCode + "' ");
	    }

	    if (!costCenterCode.equalsIgnoreCase("All"))
	    {
		sbSql.append(" and j.strCostCenterCode = '" + costCenterCode + "' ");
	    }

	    if (clsGlobalVarClass.gEnableShiftYN)
	    {
		if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
		{
		    sbSql.append(" and a.intShiftCode = '" + shiftNo + "' ");
		}
	    }
	    sbSql.append(" group by b.strItemCode  order by j.strCostCenterCode,c.strItemName");
	    //System.out.println(sbSql);

	    rsSalesMod = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
	    while (rsSalesMod.next())
	    {
		clsItemWiseConsumption objItemWiseConsumption = null;
		if (null != hmItemWiseConsumption.get(rsSalesMod.getString(1) + "!" + rsSalesMod.getString(2)))
		{
		    objItemWiseConsumption = hmItemWiseConsumption.get(rsSalesMod.getString(1) + "!" + rsSalesMod.getString(2));
		    double saleQty = objItemWiseConsumption.getSaleQty();
		    if (saleQty > 0)
		    {
			objItemWiseConsumption.setSaleQty(objItemWiseConsumption.getSaleQty() - rsSalesMod.getDouble(3));
			objItemWiseConsumption.setTotalQty(objItemWiseConsumption.getTotalQty() - rsSalesMod.getDouble(3));
		    }

		    objItemWiseConsumption.setPromoQty(objItemWiseConsumption.getPromoQty() + rsSalesMod.getDouble(3));
		    double qty = objItemWiseConsumption.getTotalQty();
		    //objItemWiseConsumption.setTotalQty(qty + objItemWiseConsumption.getPromoQty());
		}
		else
		{
		    sqlNo++;
		    objItemWiseConsumption = new clsItemWiseConsumption();
		    objItemWiseConsumption.setItemCode(rsSalesMod.getString(1));
		    objItemWiseConsumption.setItemName(rsSalesMod.getString(2));
		    objItemWiseConsumption.setSubGroupName(rsSalesMod.getString(8));
		    objItemWiseConsumption.setGroupName(rsSalesMod.getString(9));
		    objItemWiseConsumption.setNcQty(0);
		    objItemWiseConsumption.setPromoQty(rsSalesMod.getDouble(3));
		    objItemWiseConsumption.setSaleQty(0);
		    objItemWiseConsumption.setComplimentaryQty(0);

		    objItemWiseConsumption.setPOSName(rsSalesMod.getString(6));
		    objItemWiseConsumption.setSeqNo(sqlNo);
		    objItemWiseConsumption.setCostCenterCode(rsSalesMod.getString(11));
		    objItemWiseConsumption.setCostCenterName(rsSalesMod.getString(12));
		    objItemWiseConsumption.setExternalCode(rsSalesMod.getString(10));
		    double totalRowQty = rsSalesMod.getDouble(3) + 0 + 0 + 0;
		    //objItemWiseConsumption.setTotalQty(totalRowQty);
		    objItemWiseConsumption.setTotalQty(0);
		    objItemWiseConsumption.setItemRate(rsSalesMod.getDouble(5));
		}
		if (null != objItemWiseConsumption)
		{
		    hmItemWiseConsumption.put(rsSalesMod.getString(1) + "!" + rsSalesMod.getString(2), objItemWiseConsumption);
		}
	    }
	    rsSalesMod.close();

	    // Code for promotion Qty for live
	    sbSql.setLength(0);
	    sbSql.append("SELECT b.strItemCode,upper(c.strItemName), SUM(b.dblQuantity), "+amount+","+rate+",f.strPosName,0,d.strSubGroupName,e.strGroupName,c.strExternalCode "
		    + ",i.strCostCenterCode,j.strCostCenterName "
		    + "FROM tblbillhd a,tblbillpromotiondtl b,tblitemmaster c,tblsubgrouphd d,tblgrouphd e,tblposmaster f,tblmenuitempricingdtl i,tblcostcentermaster j "
		    + "WHERE a.strBillNo=b.strBillNo  "
		    + "AND DATE(a.dteBillDate)= DATE(b.dteBillDate)  "
		    + "AND b.strItemCode=c.strItemCode  "
		    + "AND c.strSubGroupCode=d.strSubGroupCode  "
		    + "AND d.strGroupCode=e.strGroupCode  "
		    + "AND a.strPOSCode=f.strPosCode  "
		    + "and b.strItemCode=i.strItemCode "
		    + "and (a.strPOSCode=i.strPosCode or i.strPosCode='All') "
		    + " and i.strHourlyPricing='NO' ");
	    if (clsGlobalVarClass.gAreaWisePricing.equalsIgnoreCase("Y"))
	    {
		sbSql.append("and (a.strAreaCode=i.strAreaCode ) ");
	    }
	    sbSql.append("and i.strCostCenterCode=j.strCostCenterCode "
		    + "AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "'");

	    if (!posCode.equals("All"))
	    {
		sbSql.append(" AND a.strPOSCode = '" + posCode + "' ");
	    }

	    if (!groupCode.equalsIgnoreCase("All"))
	    {
		sbSql.append(" and e.strGroupCode = '" + groupCode + "' ");
	    }

	    if (!costCenterCode.equalsIgnoreCase("All"))
	    {
		sbSql.append(" and j.strCostCenterCode = '" + costCenterCode + "' ");
	    }

	    if (clsGlobalVarClass.gEnableShiftYN)
	    {
		if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
		{
		    sbSql.append(" and a.intShiftCode = '" + shiftNo + "' ");
		}
	    }
	    sbSql.append(" group by b.strItemCode order by j.strCostCenterCode,c.strItemName");
	    //System.out.println(sbSql);

	    rsSalesMod = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
	    while (rsSalesMod.next())
	    {
		clsItemWiseConsumption objItemWiseConsumption = null;
		if (null != hmItemWiseConsumption.get(rsSalesMod.getString(1) + "!" + rsSalesMod.getString(2)))
		{
		    objItemWiseConsumption = hmItemWiseConsumption.get(rsSalesMod.getString(1) + "!" + rsSalesMod.getString(2));
		    double saleQty = objItemWiseConsumption.getSaleQty();
		    if (saleQty > 0)
		    {
			objItemWiseConsumption.setSaleQty(objItemWiseConsumption.getSaleQty() - rsSalesMod.getDouble(3));
			objItemWiseConsumption.setTotalQty(objItemWiseConsumption.getTotalQty() - rsSalesMod.getDouble(3));
		    }

		    objItemWiseConsumption.setPromoQty(objItemWiseConsumption.getPromoQty() + rsSalesMod.getDouble(3));
		    double qty = objItemWiseConsumption.getTotalQty();
		    //objItemWiseConsumption.setTotalQty(qty + objItemWiseConsumption.getPromoQty());
		}
		else
		{
		    sqlNo++;
		    objItemWiseConsumption = new clsItemWiseConsumption();
		    objItemWiseConsumption.setItemCode(rsSalesMod.getString(1));
		    objItemWiseConsumption.setItemName(rsSalesMod.getString(2));
		    objItemWiseConsumption.setSubGroupName(rsSalesMod.getString(8));
		    objItemWiseConsumption.setGroupName(rsSalesMod.getString(9));
		    objItemWiseConsumption.setNcQty(0);
		    objItemWiseConsumption.setPromoQty(rsSalesMod.getDouble(3));
		    objItemWiseConsumption.setSaleQty(0);
		    objItemWiseConsumption.setComplimentaryQty(0);

		    objItemWiseConsumption.setPOSName(rsSalesMod.getString(6));
		    objItemWiseConsumption.setSeqNo(sqlNo);
		    objItemWiseConsumption.setCostCenterCode(rsSalesMod.getString(11));
		    objItemWiseConsumption.setCostCenterName(rsSalesMod.getString(12));
		    objItemWiseConsumption.setExternalCode(rsSalesMod.getString(10));
		    double totalRowQty = rsSalesMod.getDouble(3) + 0 + 0 + 0;
		    //objItemWiseConsumption.setTotalQty(totalRowQty);
		    objItemWiseConsumption.setTotalQty(0);
		    objItemWiseConsumption.setItemRate(rsSalesMod.getDouble(5));
		}
		if (null != objItemWiseConsumption)
		{
		    hmItemWiseConsumption.put(rsSalesMod.getString(1) + "!" + rsSalesMod.getString(2), objItemWiseConsumption);
		}
	    }
	    rsSalesMod.close();

	    List<clsItemWiseConsumption> list = new ArrayList<clsItemWiseConsumption>();
	    for (Map.Entry<String, clsItemWiseConsumption> entry : hmItemWiseConsumption.entrySet())
	    {
		clsItemWiseConsumption objItemComp = entry.getValue();
		double totalRowQty = objItemComp.getSaleQty() + objItemComp.getComplimentaryQty() + objItemComp.getNcQty() + objItemComp.getPromoQty();
		objItemComp.setTotalQty(totalRowQty);
		list.add(objItemComp);
	    }

	    //sort list 
	    // Collections.sort(list, clsItemWiseConsumption.comparatorItemConsumptionColumnDtl);
	    Comparator<clsItemWiseConsumption> posNameComparator = new Comparator<clsItemWiseConsumption>()
	    {

		@Override
		public int compare(clsItemWiseConsumption o1, clsItemWiseConsumption o2)
		{
		    return o1.getPOSName().compareToIgnoreCase(o2.getPOSName());
		}
	    };
	    Comparator<clsItemWiseConsumption> costCenterNameComparator = new Comparator<clsItemWiseConsumption>()
	    {

		@Override
		public int compare(clsItemWiseConsumption o1, clsItemWiseConsumption o2)
		{
		    return o1.getCostCenterName().compareToIgnoreCase(o2.getCostCenterName());
		}
	    };

	    Comparator<clsItemWiseConsumption> groupNameComparator = new Comparator<clsItemWiseConsumption>()
	    {

		@Override
		public int compare(clsItemWiseConsumption o1, clsItemWiseConsumption o2)
		{
		    return o1.getGroupName().compareToIgnoreCase(o2.getGroupName());
		}
	    };

	    Comparator<clsItemWiseConsumption> subGroupNameComparator = new Comparator<clsItemWiseConsumption>()
	    {

		@Override
		public int compare(clsItemWiseConsumption o1, clsItemWiseConsumption o2)
		{
		    return o1.getSubGroupName().compareToIgnoreCase(o2.getSubGroupName());
		}
	    };

	    Comparator<clsItemWiseConsumption> itemCodeComparator = new Comparator<clsItemWiseConsumption>()
	    {

		@Override
		public int compare(clsItemWiseConsumption o1, clsItemWiseConsumption o2)
		{
		    return o1.getItemName().compareToIgnoreCase(o2.getItemName());
		}
	    };

	    Comparator<clsItemWiseConsumption> seqNoComparator = new Comparator<clsItemWiseConsumption>()
	    {

		@Override
		public int compare(clsItemWiseConsumption o1, clsItemWiseConsumption o2)
		{
		    int seqNo1 = o1.getSeqNo();
		    int seqNo2 = o2.getSeqNo();

		    if (seqNo1 == seqNo2)
		    {
			return 0;
		    }
		    else if (seqNo1 > seqNo2)
		    {
			return 1;
		    }
		    else
		    {
			return -1;
		    }
		}
	    };

	    Collections.sort(list, new clsItemConsumptionComparator(posNameComparator, costCenterNameComparator, groupNameComparator, subGroupNameComparator, itemCodeComparator
	    ));

	    //System.out.println(list);
	    //call for view report
	    if (reportType.equalsIgnoreCase("A4 Size Report"))
	    {
		funViewJasperReportForBeanCollectionDataSource(is, hm, list);
	    }
	    if (reportType.equalsIgnoreCase("Excel Report"))
	    {
		Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
		List<String> arrListTotal = new ArrayList<String>();
		List<String> arrHeaderList = new ArrayList<String>();
		double totalSaleQty = 0, totalComplimentaryQty = 0, totalNCQty = 0, totalSaleAmt = 0, totalSubTotal = 0, totalPromoQty = 0, totalDiscAmt = 0, totalQty = 0;
		double totNetTotal = 0, totDiscPer = 0, discPer = 0;
		DecimalFormat decFormat = new DecimalFormat("0");
		//DecimalFormat gDecimalFormat = new DecimalFormat("0.00");
		int i = 1;
		for (clsItemWiseConsumption objItemComp : list)
		{
		    List<String> arrListItem = new ArrayList<String>();
		    arrListItem.add(objItemComp.getGroupName());
		    arrListItem.add(objItemComp.getSubGroupName());
		    arrListItem.add(objItemComp.getItemCode());
		    arrListItem.add(objItemComp.getExternalCode());
		    arrListItem.add(objItemComp.getItemName());
		    arrListItem.add(objItemComp.getPOSName());
		    arrListItem.add(gDecimalFormat.format(objItemComp.getItemRate()));
		    arrListItem.add(String.valueOf(decFormat.format(objItemComp.getSaleQty())));
		    arrListItem.add(String.valueOf(decFormat.format(objItemComp.getComplimentaryQty())));
		    arrListItem.add(String.valueOf(decFormat.format(objItemComp.getNcQty())));
		    arrListItem.add(String.valueOf(decFormat.format(objItemComp.getPromoQty())));

		    double totalRowQty = objItemComp.getSaleQty() + objItemComp.getComplimentaryQty() + objItemComp.getNcQty() + objItemComp.getPromoQty();
		    objItemComp.setTotalQty(totalRowQty);
		    arrListItem.add(String.valueOf(decFormat.format(objItemComp.getTotalQty())));
		    arrListItem.add(String.valueOf(gDecimalFormat.format(objItemComp.getSubTotal())));
		    // arrListItem.add(String.valueOf(objItemComp.getSaleAmt()));
		    if (objItemComp.getSubTotal() > 0)
		    {
			arrListItem.add(String.valueOf(Math.rint((objItemComp.getDiscAmt() / objItemComp.getSubTotal()) * 100)));
		    }
		    else
		    {
			arrListItem.add(String.valueOf(0.00));
		    }
		    arrListItem.add(String.valueOf(gDecimalFormat.format(objItemComp.getDiscAmt())));
		    double netTotal = objItemComp.getSubTotal() - objItemComp.getDiscAmt();
		    arrListItem.add(String.valueOf(gDecimalFormat.format(netTotal)));
		    totalQty += objItemComp.getTotalQty();
		    totalSaleQty += objItemComp.getSaleQty();
		    totalComplimentaryQty += objItemComp.getComplimentaryQty();
		    totalNCQty += objItemComp.getNcQty();
//                    totalSaleAmt += objItemComp.getSaleAmt();
		    totalSubTotal += objItemComp.getSubTotal();
		    totalPromoQty += objItemComp.getPromoQty();
		    totalDiscAmt += objItemComp.getDiscAmt();

		    if (objItemComp.getSubTotal() != 0)
		    {

			discPer = objItemComp.getDiscAmt() / objItemComp.getSubTotal() * 100;
			totDiscPer = totDiscPer + discPer;
		    }
//                    totNetTotal += totalSubTotal - totalDiscAmt;
		    mapExcelItemDtl.put(i, arrListItem);
		    i++;
		}
		arrListTotal.add("" + "#" + "7");
		arrListTotal.add(String.valueOf(decFormat.format(totalSaleQty)) + "#" + "8");
		arrListTotal.add(String.valueOf(decFormat.format(totalComplimentaryQty)) + "#" + "9");
		arrListTotal.add(String.valueOf(decFormat.format(totalNCQty)) + "#" + "10");
		arrListTotal.add(String.valueOf(decFormat.format(totalPromoQty)) + "#" + "11");
		arrListTotal.add(String.valueOf(decFormat.format(totalQty)) + "#" + "12");
		arrListTotal.add(String.valueOf(gDecimalFormat.format(totalSubTotal)) + "#" + "13");
		arrListTotal.add(String.valueOf(Math.rint(totDiscPer)) + "#" + "14");
		arrListTotal.add(String.valueOf(gDecimalFormat.format(totalDiscAmt)) + "#" + "15");
		arrListTotal.add(String.valueOf(gDecimalFormat.format(totalSubTotal - totalDiscAmt)) + "#" + "16");

		arrHeaderList.add("Sr. No.");
		arrHeaderList.add("Group");
		arrHeaderList.add("Sub Group");
		arrHeaderList.add("Item Code");
		arrHeaderList.add("External Code");
		arrHeaderList.add("Item Name");
		arrHeaderList.add("POS");
		arrHeaderList.add("Sale Rate");
		arrHeaderList.add("Sale Qty");
		arrHeaderList.add("Complimentary Qty");
		arrHeaderList.add("NC Qty");
		arrHeaderList.add("Promo Qty");
		arrHeaderList.add("Total");
		arrHeaderList.add("SubTotal");
		arrHeaderList.add("Discount(%)");
		arrHeaderList.add("Discount Amount");
		arrHeaderList.add("Net Total");

		List<String> arrparameterList = new ArrayList<String>();
		arrparameterList.add("Item Consumption Report");
		arrparameterList.add("POS" + " : " + posName);
		arrparameterList.add("Group Name" + " : " + groupName);
		arrparameterList.add("Print Modifiers" + " : " + printZeroAmountModi);
		arrparameterList.add("Cost Center Name" + " : " + costCenterName);
		arrparameterList.add("FromDate" + " : " + fromDate);
		arrparameterList.add("ToDate" + " : " + toDate);
		arrparameterList.add(" ");
		arrparameterList.add(" ");
		if (clsGlobalVarClass.gEnableShiftYN)
		{
		    if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
		    {
			arrparameterList.add("Shift No " + " : " + shiftNo);
		    }
		    else
		    {
			arrparameterList.add("Shift No " + " : " + shiftNo);
		    }
		}
		funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "ItemConsumptionExcelSheet", dayEnd);
	    }
	    if (reportType.equalsIgnoreCase("Text File-40 Column Report"))
	    {

		funCreateTempFolder();
		String filePath = System.getProperty("user.dir");
		File file = new File(filePath + File.separator + "Temp" + File.separator + "Temp_ItemWiseConsumptionTextReport.txt");

		int count = funGenerateItemWiseConsumptionTextReport(file, posName, fromDate, toDate, groupName, printZeroAmountModi, costCenterName, list);
		if (count > 0)
		{
		    funShowTextFile(file, "Item Wise Consumption");
		}

	    }
	    if (reportType.equalsIgnoreCase("Text"))
	    {
		funCreateTempFolder();
		String filePath = System.getProperty("user.dir");
		File file = new File(filePath + File.separator + "Temp" + File.separator + "Temp_ItemWiseConsumptionTextReport.txt");

		int count = funGenerateItemWiseConsumptionTextReport(file, posName, fromDate, toDate, groupName, printZeroAmountModi, costCenterName, list);
		if (count > 0)
		{
		    funShowTextFile(file, "Item Wise Consumption");
		}
	    }

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

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
		data = data + line + "\n";
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

    public void funCreateTempFolder()
    {
	try
	{
	    String filePath = System.getProperty("user.dir");
	    File file = new File(filePath + "/Temp");
	    if (!file.exists())
	    {
		file.mkdirs();
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    public int funGenerateItemWiseConsumptionTextReport(File file, String pos, String fDate, String tDate, String groupName, String printModifiers, String costCenterName, List<clsItemWiseConsumption> list)
    {
	int count = 0;
	String dashedLine = "---------------------";
	try
	{
	    String[] dateFromDt = fDate.split("-");
	    String fromDate = dateFromDt[2] + "-" + dateFromDt[1] + "-" + dateFromDt[0];

	    String[] dateToDt = tDate.split("-");
	    String toDate = dateToDt[2] + "-" + dateToDt[1] + "-" + dateToDt[0]; // 004

	    PrintWriter pw = new PrintWriter(file);
	    funPrintBlankLines(clsGlobalVarClass.gClientName, pw);

	    pw.println();
	    funPrintBlankLines("ItemWise Consumption Report", pw);
	    pw.println();
	    pw.println();
	    pw.println("POS  :" + pos);
	    pw.println("Group Name  :" + groupName);
	    pw.println("Print Modifiers  :" + printModifiers);
	    pw.println("Cost Center Name  :" + costCenterName);
	    pw.println("From :" + fromDate + "  To :" + toDate);

	    pw.println(dashedLine);
	    pw.println("Item Name");
	    pw.println("   Sale Qty  Comp Qty");
	    pw.println(dashedLine);

	    double totalSaleQty = 0, totalCompQty = 0, totalSubTotal = 0;
	    for (clsItemWiseConsumption objItemComp : list)
	    {
		pw.println(objItemComp.getItemName());
		funPrintTextWithAlignment("right", String.valueOf(objItemComp.getSaleQty()), 9, pw);

		funPrintTextWithAlignment("right", String.valueOf(objItemComp.getComplimentaryQty() + objItemComp.getPromoQty()), 9, pw);

		//funPrintTextWithAlignment("right", "   " + String.valueOf(objItemComp.getSubTotal()), 20, pw);
		pw.println();
		count++;

		totalSaleQty = totalSaleQty + objItemComp.getSaleQty();
		totalCompQty = totalCompQty + objItemComp.getComplimentaryQty();
		totalSubTotal = totalSubTotal + objItemComp.getSubTotal();

	    }

	    pw.println(dashedLine);
	    pw.println("GRAND TOTAL :");
	    funPrintTextWithAlignment("right", String.valueOf(gDecimalFormat.format(totalSaleQty)), 9, pw);
	    funPrintTextWithAlignment("right", String.valueOf(gDecimalFormat.format(totalCompQty)), 9, pw);
	    //funPrintTextWithAlignment("right", "   " + String.valueOf(Math.rint(totalSubTotal)), 20, pw);
	    pw.println();
	    pw.println(dashedLine);

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

    private int funPrintTextWithAlignment(String align, String textToPrint, int totalLength, PrintWriter pw)
    {
	int len = totalLength - textToPrint.length();
	for (int cnt = 0; cnt < len; cnt++)
	{
	    pw.print(" ");
	}

	DecimalFormat decFormat = new DecimalFormat("######.00");
	pw.print(new DecimalFormat("######0.00").format(Double.parseDouble(textToPrint)));
	return 1;
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
		JOptionPane.showMessageDialog(null, "Report Image Not Found!!! Please Check Property Setup Report Image.", "Error Code: RIMG-1", JOptionPane.ERROR_MESSAGE);
	    }
	    e.printStackTrace();
	}
    }

    public void funCreateExcelSheet(List<String> parameterList, List<String> headerList, Map<Integer, List<String>> map, List<String> totalList, String fileName, String dayEnd)
    {
	String filePath = System.getProperty("user.dir");
	File file = new File(filePath + File.separator + "Reports" + File.separator + fileName + ".xls");
	try
	{
	    WritableWorkbook workbook1 = Workbook.createWorkbook(file);
	    WritableSheet sheet1 = workbook1.createSheet("First Sheet", 0);
	    WritableFont cellFont = new WritableFont(WritableFont.COURIER, 14);
	    cellFont.setBoldStyle(WritableFont.BOLD);
	    WritableCellFormat cellFormat = new WritableCellFormat(cellFont);
	    WritableFont headerCellFont = new WritableFont(WritableFont.TIMES, 10);
	    headerCellFont.setBoldStyle(WritableFont.BOLD);
	    WritableCellFormat headerCell = new WritableCellFormat(headerCellFont);

	    for (int j = 0; j <= parameterList.size(); j++)
	    {
		Label l0 = new Label(2, 0, parameterList.get(0), cellFormat);
		Label l1 = new Label(0, 2, parameterList.get(1), headerCell);
		Label l2 = new Label(1, 2, parameterList.get(2), headerCell);
		Label l3 = new Label(2, 2, parameterList.get(3), headerCell);
		Label l4 = new Label(0, 3, parameterList.get(4), headerCell);
		Label l5 = new Label(1, 3, parameterList.get(5), headerCell);
		Label l6 = new Label(2, 3, parameterList.get(6), headerCell);
		sheet1.addCell(l0);
		sheet1.addCell(l1);
		sheet1.addCell(l2);
		sheet1.addCell(l3);
		sheet1.addCell(l4);
		sheet1.addCell(l5);
		sheet1.addCell(l6);
	    }

	    for (int j = 0; j < headerList.size(); j++)
	    {
		Label lblHeader = new Label(j, 5, headerList.get(j), headerCell);
		sheet1.addCell(lblHeader);
	    }

	    int i = 7;
	    for (Map.Entry<Integer, List<String>> entry : map.entrySet())
	    {
		Label lbl0 = new Label(0, i, entry.getKey().toString());
		List<String> nameList = map.get(entry.getKey());
		for (int j = 0; j < nameList.size(); j++)
		{
		    int colIndex = j + 1;
		    Label lblData = new Label(colIndex, i, nameList.get(j));
		    sheet1.addCell(lblData);
		    sheet1.setColumnView(i, 15);
		}
		sheet1.addCell(lbl0);
		i++;
	    }

	    for (int j = 0; j < totalList.size(); j++)
	    {
		String[] l0 = new String[10];
		for (int c = 0; c < totalList.size(); c++)
		{
		    l0 = totalList.get(c).split("#");
		    int pos = Integer.parseInt(l0[1]);
		    Label lable0 = new Label(pos, i + 1, l0[0], headerCell);
		    sheet1.addCell(lable0);
		}
		Label labelTotal = new Label(0, i + 1, "TOTAL:", headerCell);
		sheet1.addCell(labelTotal);
	    }
	    workbook1.write();
	    workbook1.close();

	    if (!dayEnd.equalsIgnoreCase("Yes"))
	    {
		Desktop dt = Desktop.getDesktop();
		dt.open(file);
	    }
	}
	catch (Exception ex)
	{
	    JOptionPane.showMessageDialog(null, ex.getMessage());
	    ex.printStackTrace();
	}
    }

    public void funItemWiseConsumptionMenuHead(String reportType, HashMap hm, String dayEnd)
    {

	try
	{
	    InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptItemWiseConsumptionReport2.jasper");
	    String fromDate = hm.get("fromDate").toString();
	    String toDate = hm.get("toDate").toString();
	    String posCode = hm.get("posCode").toString();
	    String shiftNo = hm.get("shiftNo").toString();
	    String posName = hm.get("posName").toString();
	    String groupCode = hm.get("GroupCode").toString();
	    String groupName = hm.get("GroupName").toString();
	    String costCenterCode = hm.get("costCenterCode").toString();
	    String printZeroAmountModi = hm.get("PrintZeroAmountModi").toString();
	    String currency = hm.get("currency").toString();
	    String costCenterCd = "", costCenterNm = "";

	    boolean isDayEndHappend = objUtility2.isDayEndHappened(toDate);
	    if (!isDayEndHappend)
	    {
		hm.put("isDayEndHappend", "DAY END NOT DONE.");
	    }

	    SimpleDateFormat parser = new SimpleDateFormat("dd-MM-yyyy");
	    Date dateFrom = parser.parse(hm.get("fromDateToDisplay").toString());
	    Date dateTo = parser.parse(hm.get("toDateToDisplay").toString());
	    hm.put("fromDateToDisplay", new SimpleDateFormat("dd-MMM-yyyy").format(dateFrom));
	    hm.put("toDateToDisplay", new SimpleDateFormat("dd-MMM-yyyy").format(dateTo));

	    int sqlNo = 0;
	    StringBuilder sbSql = new StringBuilder();
	    StringBuilder sbSqlMod = new StringBuilder();
	    StringBuilder sbFilters = new StringBuilder();
	    ResultSet rsSalesMod;
	    Map<String, clsItemWiseConsumption> hmItemWiseConsumption = new HashMap<String, clsItemWiseConsumption>();
	    Map<String, Double> hmMenuHdAmt = new HashMap<String, Double>();
	    // Code for Sales Qty for bill detail and bill modifier live & q data
	    // for Sales Qty for bill detail live data  
	    sbSql.setLength(0);
	    
	    String regularAmt = "a.RegularAmt";
	    String compAmt = "b.CompAmt";
	    String dblAmount = "SUM(b.dblamount)";
	    if(currency.equalsIgnoreCase("USD"))
	    {
		dblAmount = "SUM(b.dblamount)/a.dblUSDConverionRate";
	    }	
	   
	    sbSql.append(" select  a.strMenuName, a.stritemcode, upper(a.itemName),a.RegularQty-IFNULL(b.CompQty,0)RegularQty, "+regularAmt+",  "
		    + "ifnull(b.CompQty,0), ifnull("+compAmt+",0) ,a.strBillNo from  "
		    + "(SELECT k.strMenuName, b.stritemcode, upper(b.stritemname) itemName, SUM(b.dblQuantity) RegularQty, "+dblAmount+" RegularAmt,a.strBillNo,a.dblUSDConverionRate dblUSDConverionRate "
		    + "  FROM tblbillhd a,tblbilldtl b, tblposmaster e,tblitemmaster f,tblsubgrouphd g,tblgrouphd h,tblmenuitempricingdtl i,tblcostcentermaster j, tblmenuhd k "
		    + "  WHERE a.strBillNo=b.strBillNo "
		    + "  AND DATE(a.dteBillDate)= DATE(b.dteBillDate)   "
		    + "  AND a.strPOSCode=e.strPosCode "
		    + "  AND b.strItemCode=f.strItemCode "
		    + "  AND f.strSubGroupCode=g.strSubGroupCode "
		    + "  AND g.strGroupCode=h.strGroupCode "
		    + "  and b.strItemCode=i.strItemCode "
		    + "  and (a.strPOSCode=i.strPosCode or i.strPosCode='All') "
		    + "  and i.strCostCenterCode=j.strCostCenterCode "
		    + "  and i.strMenuCode = k.strMenuCode "
		    + " and i.strHourlyPricing='NO' ");
//		    + "  and b.dblAmount <> 0  ");
	    if (!costCenterCode.equalsIgnoreCase("All"))
	    {
		sbSql.append(" and j.strCostCenterCode = '" + costCenterCode + "' ");
	    }
	    sbSql.append(" and DATE(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		    + "group by k.strMenuName, b.strItemCode, b.stritemname) a left outer join   "
		    + " "
		    + "  "
		    + "(SELECT k.strMenuName, b.stritemcode, upper(b.stritemname), SUM(b.dblQuantity) CompQty, "+dblAmount+" CompAmt ,a.strBillNo,a.dblUSDConverionRate dblUSDConverionRate "
		    + "  FROM tblbillhd a,tblbilldtl b, tblposmaster e,tblitemmaster f,tblsubgrouphd g,tblgrouphd h,tblmenuitempricingdtl i,tblcostcentermaster j, tblmenuhd k "
		    + "  WHERE a.strBillNo=b.strBillNo "
		    + "  AND DATE(a.dteBillDate)= DATE(b.dteBillDate)   "
		    + "  AND a.strPOSCode=e.strPosCode "
		    + "  AND b.strItemCode=f.strItemCode "
		    + "  AND f.strSubGroupCode=g.strSubGroupCode "
		    + "  AND g.strGroupCode=h.strGroupCode "
		    + "  and b.strItemCode=i.strItemCode "
		    + "  and (a.strPOSCode=i.strPosCode or i.strPosCode='All') "
		    + "  and i.strCostCenterCode=j.strCostCenterCode "
		    + "  and i.strMenuCode = k.strMenuCode "
		    + "  and b.dblAmount = 0  "
		    + " and i.strHourlyPricing='NO' ");
	    if (!costCenterCode.equalsIgnoreCase("All"))
	    {
		sbSql.append(" and j.strCostCenterCode = '" + costCenterCode + "' ");
	    }
	    sbSql.append("  and DATE(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		    + "group by k.strMenuName, b.strItemCode, b.stritemname) b "
		    + "on a.strItemCode = b.strItemCode "
		    + " "
		    + "Order by a.strMenuName, a.itemName ");
	    ResultSet rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
	    while (rsSales.next())
	    {
		//a.strMenuName, a.stritemcode,itemName,
		//4 a.RegularQty, a.RegularAmt,6 b.CompQty,CompAmt,
		clsItemWiseConsumption objItemWiseConsumption = null;
		if (null != hmItemWiseConsumption.get(rsSales.getString(2) + "!" + rsSales.getString(3)))
		{
		    objItemWiseConsumption = hmItemWiseConsumption.get(rsSales.getString(2) + "!" + rsSales.getString(3));
		    objItemWiseConsumption.setSaleQty(objItemWiseConsumption.getSaleQty() + rsSales.getDouble(4));
		    objItemWiseConsumption.setSaleAmt(objItemWiseConsumption.getSaleAmt() + (rsSales.getDouble(5)));
		    //objItemWiseConsumption.setSubTotal(objItemWiseConsumption.getSubTotal() + rsSales.getDouble(4));
		    //objItemWiseConsumption.setTotalQty(objItemWiseConsumption.getTotalQty() + rsSales.getDouble(3));
		}
		else
		{
		    sqlNo++;
		    objItemWiseConsumption = new clsItemWiseConsumption();
		    objItemWiseConsumption.setMenuHead(rsSales.getString(1));
		    objItemWiseConsumption.setItemCode(rsSales.getString(2));
		    objItemWiseConsumption.setItemName(rsSales.getString(3));
		    objItemWiseConsumption.setSaleQty(rsSales.getDouble(4));
		    objItemWiseConsumption.setComplimentaryQty(rsSales.getDouble(6));
		    objItemWiseConsumption.setNcQty(0);
		    objItemWiseConsumption.setSubTotal(rsSales.getDouble(5));
		    objItemWiseConsumption.setSeqNo(sqlNo);
		}
		if (null != objItemWiseConsumption)
		{
		    hmItemWiseConsumption.put(rsSales.getString(2) + "!" + rsSales.getString(3), objItemWiseConsumption);
		    if (null != hmMenuHdAmt.get(rsSales.getString(1)))//check menu h
		    {
			double subTot = hmMenuHdAmt.get(rsSales.getString(1));
			hmMenuHdAmt.put(rsSales.getString(1), objItemWiseConsumption.getSubTotal() + subTot);
		    }
		    else
		    {
			hmMenuHdAmt.put(rsSales.getString(1), objItemWiseConsumption.getSubTotal());
		    }

		}
	    }
	    rsSales.close();

	    // for Sales Qty for bill detail q data 
	    sbSql.setLength(0);

	    sbSql.append(" select  a.strMenuName, a.stritemcode, upper(a.itemName),a.RegularQty-IFNULL(b.CompQty,0)RegularQty, "+regularAmt+",  "
		    + "ifnull(b.CompQty,0), ifnull("+compAmt+",0) ,a.strBillNo from  "
		    + "(SELECT k.strMenuName, b.stritemcode, upper(b.stritemname) itemName, SUM(b.dblQuantity) RegularQty, "+dblAmount+" RegularAmt,a.strBillNo,a.dblUSDConverionRate dblUSDConverionRate "
		    + "  FROM tblqbillhd a,tblqbilldtl b, tblposmaster e,tblitemmaster f,tblsubgrouphd g,tblgrouphd h,tblmenuitempricingdtl i,tblcostcentermaster j, tblmenuhd k "
		    + "  WHERE a.strBillNo=b.strBillNo "
		    + "  AND DATE(a.dteBillDate)= DATE(b.dteBillDate)   "
		    + "  AND a.strPOSCode=e.strPosCode "
		    + "  AND b.strItemCode=f.strItemCode "
		    + "  AND f.strSubGroupCode=g.strSubGroupCode "
		    + "  AND g.strGroupCode=h.strGroupCode "
		    + "  and b.strItemCode=i.strItemCode "
		    + "  and (a.strPOSCode=i.strPosCode or i.strPosCode='All') "
		    + "  and i.strCostCenterCode=j.strCostCenterCode "
		    + "  and i.strMenuCode = k.strMenuCode "
		    + " and i.strHourlyPricing='NO'  ");
//		    + "  and b.dblAmount <> 0  ");
	    if (!costCenterCode.equalsIgnoreCase("All"))
	    {
		sbSql.append(" and j.strCostCenterCode = '" + costCenterCode + "' ");
	    }
	    sbSql.append(" and DATE(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		    + "group by k.strMenuName, b.strItemCode, b.stritemname) a left outer join   "
		    + "  "
		    + "(SELECT k.strMenuName, b.stritemcode, upper(b.stritemname), SUM(b.dblQuantity) CompQty, "+dblAmount+" CompAmt ,a.strBillNo,a.dblUSDConverionRate dblUSDConverionRate "
		    + "  FROM tblqbillhd a,tblqbilldtl b, tblposmaster e,tblitemmaster f,tblsubgrouphd g,tblgrouphd h,tblmenuitempricingdtl i,tblcostcentermaster j, tblmenuhd k "
		    + "  WHERE a.strBillNo=b.strBillNo "
		    + "  AND DATE(a.dteBillDate)= DATE(b.dteBillDate)   "
		    + "  AND a.strPOSCode=e.strPosCode "
		    + "  AND b.strItemCode=f.strItemCode "
		    + "  AND f.strSubGroupCode=g.strSubGroupCode "
		    + "  AND g.strGroupCode=h.strGroupCode "
		    + "  and b.strItemCode=i.strItemCode "
		    + "  and (a.strPOSCode=i.strPosCode or i.strPosCode='All') "
		    + "  and i.strCostCenterCode=j.strCostCenterCode "
		    + "  and i.strMenuCode = k.strMenuCode "
		    + "  and b.dblAmount = 0  "
		    + " and i.strHourlyPricing='NO' ");
	    if (!costCenterCode.equalsIgnoreCase("All"))
	    {
		sbSql.append(" and j.strCostCenterCode = '" + costCenterCode + "' ");
	    }
	    sbSql.append("  and DATE(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		    + "group by k.strMenuName, b.strItemCode, b.stritemname) b "
		    + "on a.strItemCode = b.strItemCode "
		    + "Order by a.strMenuName, a.itemName ");
	    rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
	    while (rsSales.next())
	    {
		clsItemWiseConsumption objItemWiseConsumption = null;
		if (null != hmItemWiseConsumption.get(rsSales.getString(1) + "!" + rsSales.getString(2)))
		{
		    objItemWiseConsumption = hmItemWiseConsumption.get(rsSales.getString(2) + "!" + rsSales.getString(3));
		    objItemWiseConsumption.setSaleQty(objItemWiseConsumption.getSaleQty() + rsSales.getDouble(4));
		    objItemWiseConsumption.setSaleAmt(objItemWiseConsumption.getSaleAmt() + (rsSales.getDouble(5)));

		}
		else
		{
		    sqlNo++;
		    objItemWiseConsumption = new clsItemWiseConsumption();
		    objItemWiseConsumption.setMenuHead(rsSales.getString(1));
		    objItemWiseConsumption.setItemCode(rsSales.getString(2));
		    objItemWiseConsumption.setItemName(rsSales.getString(3));
		    objItemWiseConsumption.setSaleQty(rsSales.getDouble(4));
		    objItemWiseConsumption.setComplimentaryQty(rsSales.getDouble(6));
		    objItemWiseConsumption.setNcQty(0);
		    objItemWiseConsumption.setSubTotal(rsSales.getDouble(5));
		    objItemWiseConsumption.setSeqNo(sqlNo);
		}
		if (null != objItemWiseConsumption)
		{
		    hmItemWiseConsumption.put(rsSales.getString(2) + "!" + rsSales.getString(3), objItemWiseConsumption);
		    if (null != hmMenuHdAmt.get(rsSales.getString(1)))//check menu h
		    {
			double subTot = hmMenuHdAmt.get(rsSales.getString(1));
			hmMenuHdAmt.put(rsSales.getString(1), objItemWiseConsumption.getSubTotal() + subTot);
		    }
		    else
		    {
			hmMenuHdAmt.put(rsSales.getString(1), objItemWiseConsumption.getSubTotal());
		    }
		}
	    }
	    rsSales.close();

	    //live modifiers
	    String amount = "b.dblamount";
	    String rate = "b.dblRate";
	    String discAmt = "b.dblDiscAmt";
	    if(currency.equalsIgnoreCase("USD"))
	    {
		amount = "b.dblamount/a.dblUSDConverionRate";
		rate = "b.dblRate/a.dblUSDConverionRate";
		discAmt = "b.dblDiscAmt/a.dblUSDConverionRate";
	    }	
	    
	    sbSqlMod.setLength(0);
	    // Code for Sales Qty for modifier live & q data
	    sbSqlMod.append("SELECT b.strItemCode, UPPER(b.strModifierName),b.dblQuantity,"+amount+","+rate+",e.strposname,"+discAmt+",g.strSubGroupName,h.strGroupName,a.strBillNo,f.strExternalCode\n"
		    + ",j.strMenuCode,j.strMenuName\n"
		    + "FROM tblbillhd a,tblbillmodifierdtl b,tblposmaster e,tblitemmaster f,tblsubgrouphd g\n"
		    + ",tblgrouphd h,tblmenuitempricingdtl i,tblmenuhd j\n"
		    + "WHERE a.strBillNo=b.strBillNo \n"
		    + "AND DATE(a.dteBillDate)= DATE(b.dteBillDate) \n"
		    + "AND a.strPOSCode=e.strPosCode \n"
		    + "AND LEFT(b.strItemCode,7)=f.strItemCode \n"
		    + "AND f.strSubGroupCode=g.strSubGroupCode \n"
		    + "AND g.strGroupCode=h.strGroupCode \n"
		    + "AND LEFT(b.strItemCode,7)=i.strItemCode \n"
		    + "and i.strMenuCode=j.strMenuCode\n"
		    + "and (a.strPOSCode=e.strPosCode or i.strPosCode='All')\n"
		    + "AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "'\n"
		    + "  and i.strHourlyPricing='NO' ");
	    
	    
	     if (!costCenterCode.equalsIgnoreCase("All"))
	    {
		sbSqlMod.append(" and i.strCostCenterCode = '" + costCenterCode + "' ");
	    }
	    if (printZeroAmountModi.equalsIgnoreCase("Yes"))//Tjs brew works dont want modifiers details
	    {
		sbSqlMod.append(" GROUP BY b.strItemCode,b.strModifierName ");
	    }
	    else
	    {
		sbSqlMod.append(" And  b.dblamount >0 ");
		sbSqlMod.append(" GROUP BY b.strItemCode,b.strModifierName ");
	    }
	    sbSqlMod.append(sbFilters);

	    rsSalesMod = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlMod.toString());
	    while (rsSalesMod.next())
	    {
		clsItemWiseConsumption objItemWiseConsumption = null;
		if (null != hmItemWiseConsumption.get(rsSalesMod.getString(1) + "!" + rsSalesMod.getString(2)))
		{
		    objItemWiseConsumption = hmItemWiseConsumption.get(rsSalesMod.getString(1) + "!" + rsSalesMod.getString(2));
		    objItemWiseConsumption.setSaleQty(objItemWiseConsumption.getSaleQty() + rsSalesMod.getDouble(3));
		    objItemWiseConsumption.setSaleAmt(objItemWiseConsumption.getSaleAmt() + (rsSalesMod.getDouble(4) - rsSalesMod.getDouble(7)));
		    objItemWiseConsumption.setSubTotal(objItemWiseConsumption.getSubTotal() + rsSalesMod.getDouble(4));
		    //objItemWiseConsumption.setTotalQty(objItemWiseConsumption.getTotalQty() + rsSalesMod.getDouble(3));
		}
		else
		{
		    sqlNo++;
		    objItemWiseConsumption = new clsItemWiseConsumption();

		    objItemWiseConsumption.setItemCode(rsSalesMod.getString(1));
		    objItemWiseConsumption.setItemName(rsSalesMod.getString(2));
		    objItemWiseConsumption.setSubGroupName(rsSalesMod.getString(8));
		    objItemWiseConsumption.setGroupName(rsSalesMod.getString(9));
		    objItemWiseConsumption.setSaleQty(rsSalesMod.getDouble(3));
		    objItemWiseConsumption.setComplimentaryQty(0);
		    objItemWiseConsumption.setMenuHead(rsSalesMod.getString(13));
		    objItemWiseConsumption.setNcQty(0);
		    objItemWiseConsumption.setSubTotal(rsSalesMod.getDouble(4));
		    double totalRowQty = rsSalesMod.getDouble(3) + 0 + 0 + 0;
		    //objItemWiseConsumption.setTotalQty(totalRowQty);

		}
		if (null != objItemWiseConsumption)
		{
		    hmItemWiseConsumption.put(rsSalesMod.getString(1) + "!" + rsSalesMod.getString(2), objItemWiseConsumption);
		    if (null != hmMenuHdAmt.get(rsSalesMod.getString(13)))//check menu h
		    {
			double subTot = hmMenuHdAmt.get(rsSalesMod.getString(13));
			hmMenuHdAmt.put(rsSalesMod.getString(13), objItemWiseConsumption.getSubTotal() + subTot);
		    }
		    else
		    {
			hmMenuHdAmt.put(rsSalesMod.getString(13), objItemWiseConsumption.getSubTotal());
		    }
		}
	    }
	    rsSalesMod.close();
	    
	    sbSqlMod.setLength(0);
	    // Code for Sales Qty for modifier live & q data
	    sbSqlMod.append("SELECT b.strItemCode, UPPER(b.strModifierName),b.dblQuantity,"+amount+","+rate+",e.strposname,"+discAmt+",g.strSubGroupName,h.strGroupName,a.strBillNo,f.strExternalCode\n"
		    + ",j.strMenuCode,j.strMenuName\n"
		    + "FROM tblqbillhd a,tblqbillmodifierdtl b,tblposmaster e,tblitemmaster f,tblsubgrouphd g\n"
		    + ",tblgrouphd h,tblmenuitempricingdtl i,tblmenuhd j\n"
		    + "WHERE a.strBillNo=b.strBillNo \n"
		    + "AND DATE(a.dteBillDate)= DATE(b.dteBillDate) \n"
		    + "AND a.strPOSCode=e.strPosCode \n"
		    + "AND LEFT(b.strItemCode,7)=f.strItemCode \n"
		    + "AND f.strSubGroupCode=g.strSubGroupCode \n"
		    + "AND g.strGroupCode=h.strGroupCode \n"
		    + "AND LEFT(b.strItemCode,7)=i.strItemCode \n"
		    + "and i.strMenuCode=j.strMenuCode\n"
		    + "and (a.strPOSCode=e.strPosCode or i.strPosCode='All')\n"
		    + "AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "'\n"
		    + "  and i.strHourlyPricing='NO' ");
	    if (!costCenterCode.equalsIgnoreCase("All"))
	    {
		sbSqlMod.append(" and i.strCostCenterCode = '" + costCenterCode + "' ");
	    }
	    if (printZeroAmountModi.equalsIgnoreCase("Yes"))//Tjs brew works dont want modifiers details
	    {
		sbSqlMod.append(" GROUP BY b.strItemCode,b.strModifierName ");
	    }
	    else
	    {
		sbSqlMod.append(" And  b.dblamount >0 ");
		sbSqlMod.append(" GROUP BY b.strItemCode,b.strModifierName ");
	    }
	    sbSqlMod.append(sbFilters);

	    rsSalesMod = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlMod.toString());
	    while (rsSalesMod.next())
	    {
		clsItemWiseConsumption objItemWiseConsumption = null;
		if (null != hmItemWiseConsumption.get(rsSalesMod.getString(1) + "!" + rsSalesMod.getString(2)))
		{
		    objItemWiseConsumption = hmItemWiseConsumption.get(rsSalesMod.getString(1) + "!" + rsSalesMod.getString(2));
		    objItemWiseConsumption.setSaleQty(objItemWiseConsumption.getSaleQty() + rsSalesMod.getDouble(3));
		    objItemWiseConsumption.setSaleAmt(objItemWiseConsumption.getSaleAmt() + (rsSalesMod.getDouble(4) - rsSalesMod.getDouble(7)));
		    objItemWiseConsumption.setSubTotal(objItemWiseConsumption.getSubTotal() + rsSalesMod.getDouble(4));
		    //objItemWiseConsumption.setTotalQty(objItemWiseConsumption.getTotalQty() + rsSalesMod.getDouble(3));
		}
		else
		{
		    sqlNo++;
		    objItemWiseConsumption = new clsItemWiseConsumption();

		    objItemWiseConsumption.setItemCode(rsSalesMod.getString(1));
		    objItemWiseConsumption.setItemName(rsSalesMod.getString(2));
		    objItemWiseConsumption.setSubGroupName(rsSalesMod.getString(8));
		    objItemWiseConsumption.setGroupName(rsSalesMod.getString(9));
		    objItemWiseConsumption.setSaleQty(rsSalesMod.getDouble(3));
		    objItemWiseConsumption.setComplimentaryQty(0);
		    objItemWiseConsumption.setMenuHead(rsSalesMod.getString(13));
		    objItemWiseConsumption.setNcQty(0);
		    objItemWiseConsumption.setSubTotal(rsSalesMod.getDouble(4));
		    double totalRowQty = rsSalesMod.getDouble(3) + 0 + 0 + 0;
		    //objItemWiseConsumption.setTotalQty(totalRowQty);

		}
		if (null != objItemWiseConsumption)
		{
		    hmItemWiseConsumption.put(rsSalesMod.getString(1) + "!" + rsSalesMod.getString(2), objItemWiseConsumption);
		    if (null != hmMenuHdAmt.get(rsSalesMod.getString(13)))//check menu h
		    {
			double subTot = hmMenuHdAmt.get(rsSalesMod.getString(13));
			hmMenuHdAmt.put(rsSalesMod.getString(13), objItemWiseConsumption.getSubTotal() + subTot);
		    }
		    else
		    {
			hmMenuHdAmt.put(rsSalesMod.getString(13), objItemWiseConsumption.getSubTotal());
		    }
		}
	    }
	    rsSalesMod.close();

	    double totalSaleAmt = 0;
	    for (Map.Entry<String, Double> entry : hmMenuHdAmt.entrySet())
	    {
		totalSaleAmt = totalSaleAmt + entry.getValue();
	    }

	    List<clsItemWiseConsumption> list = new ArrayList<clsItemWiseConsumption>();
	    for (Map.Entry<String, clsItemWiseConsumption> entry : hmItemWiseConsumption.entrySet())
	    {
		clsItemWiseConsumption objItemComp = entry.getValue();
		double menuTot = hmMenuHdAmt.get(objItemComp.getMenuHead());
		objItemComp.setMenuHeadPer((objItemComp.getSubTotal() / menuTot) * 100);
		objItemComp.setSubTotalPer((objItemComp.getSubTotal() / totalSaleAmt) * 100);
		list.add(objItemComp);
	    }

	    Comparator<clsItemWiseConsumption> menuHeadComparator = new Comparator<clsItemWiseConsumption>()
	    {

		@Override
		public int compare(clsItemWiseConsumption o1, clsItemWiseConsumption o2)
		{
		    return o1.getMenuHead().compareToIgnoreCase(o2.getMenuHead());
		}
	    };

	    Comparator<clsItemWiseConsumption> itemCodeComparator = new Comparator<clsItemWiseConsumption>()
	    {

		@Override
		public int compare(clsItemWiseConsumption o1, clsItemWiseConsumption o2)
		{
		    return o1.getItemName().compareToIgnoreCase(o2.getItemName());
		}
	    };

	    Collections.sort(list, new clsItemConsumptionComparator(menuHeadComparator, itemCodeComparator
	    ));

	    //System.out.println(list);
	    //call for view report
	    if (reportType.equalsIgnoreCase("A4 Size Report"))
	    {
		funViewJasperReportForBeanCollectionDataSource(is, hm, list);
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    public void funItemWiseConsumptionReportCostCenter(String reportType, HashMap hm, String dayEnd)
    {
	try
	{
	    InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptItemWiseConsumptionReportCostCenter.jasper");
	    String fromDate = hm.get("fromDate").toString();
	    String toDate = hm.get("toDate").toString();
	    String posCode = hm.get("posCode").toString();
	    String shiftNo = hm.get("shiftNo").toString();
	    String posName = hm.get("posName").toString();
	    String groupCode = hm.get("GroupCode").toString();
	    String groupName = hm.get("GroupName").toString();
	    String costCenterCode = hm.get("costCenterCode").toString();
	    String printZeroAmountModi = hm.get("PrintZeroAmountModi").toString();
	    String currency = hm.get("currency").toString();
	    String costCenterCd = "", costCenterNm = "";

	    boolean isDayEndHappend = objUtility2.isDayEndHappened(toDate);
	    if (!isDayEndHappend)
	    {
		hm.put("isDayEndHappend", "DAY END NOT DONE.");
	    }

	    SimpleDateFormat parser = new SimpleDateFormat("dd-MM-yyyy");
	    Date dateFrom = parser.parse(hm.get("fromDateToDisplay").toString());
	    Date dateTo = parser.parse(hm.get("toDateToDisplay").toString());
	    hm.put("fromDateToDisplay", new SimpleDateFormat("dd-MMM-yyyy").format(dateFrom));
	    hm.put("toDateToDisplay", new SimpleDateFormat("dd-MMM-yyyy").format(dateTo));

	    int sqlNo = 0;
	    StringBuilder sbSql = new StringBuilder();
	    StringBuilder sbSqlMod = new StringBuilder();
	    StringBuilder sbFilters = new StringBuilder();
	    ResultSet rsSalesMod;
	    Map<String, clsItemWiseConsumption> hmItemWiseConsumption = new HashMap<String, clsItemWiseConsumption>();
	    Map<String, Double> hmMenuHdAmt = new HashMap<String, Double>();
	    // Code for Sales Qty for bill detail and bill modifier live & q data
	    // for Sales Qty for bill detail live data  
	    sbSql.setLength(0);
	    String regularAmt = "a.RegularAmt";
	    String compAmt = "b.CompAmt";
	    String dblAmount = "SUM(b.dblamount)";
	    if(currency.equalsIgnoreCase("USD"))
	    {
		dblAmount = "SUM(b.dblamount)/a.dblUSDConverionRate";
	    }	
	    
	    sbSql.append(" select  a.strCostCenterName, a.stritemcode, upper(a.itemName),a.RegularQty-IFNULL(b.CompQty,0)RegularQty, "+regularAmt+",  "
		    + "ifnull(b.CompQty,0), ifnull("+compAmt+",0) ,a.strBillNo,a.strMenuName from  "
		    + "(SELECT j.strCostCenterName, b.stritemcode, upper(b.stritemname) itemName, SUM(b.dblQuantity) RegularQty, "+dblAmount+" RegularAmt,a.strBillNo ,k.strMenuName,a.dblUSDConverionRate dblUSDConverionRate"
		    + "  FROM tblbillhd a,tblbilldtl b, tblposmaster e,tblitemmaster f,tblsubgrouphd g,tblgrouphd h,tblmenuitempricingdtl i,tblcostcentermaster j, tblmenuhd k "
		    + "  WHERE a.strBillNo=b.strBillNo "
		    + "  AND DATE(a.dteBillDate)= DATE(b.dteBillDate)   "
		    + "  AND a.strPOSCode=e.strPosCode "
		    + "  AND b.strItemCode=f.strItemCode "
		    + "  AND f.strSubGroupCode=g.strSubGroupCode "
		    + "  AND g.strGroupCode=h.strGroupCode "
		    + "  and b.strItemCode=i.strItemCode "
		    + "  and (a.strPOSCode=i.strPosCode or i.strPosCode='All') "
		    + "  and i.strCostCenterCode=j.strCostCenterCode "
		    + "  and i.strMenuCode = k.strMenuCode "
		    + "  and i.strHourlyPricing='NO' ");
//		    + "  and b.dblAmount <> 0  ");
	    if (!costCenterCode.equalsIgnoreCase("All"))
	    {
		sbSql.append(" and j.strCostCenterCode = '" + costCenterCode + "' ");
	    }

	    sbSql.append("  and DATE(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		    + "group by j.strCostCenterName, b.strItemCode, b.stritemname) a "
		    + " left outer join   "
		    + "(SELECT j.strCostCenterName, b.stritemcode, upper(b.stritemname), SUM(b.dblQuantity) CompQty, "+dblAmount+" CompAmt ,a.strBillNo ,k.strMenuName,a.dblUSDConverionRate dblUSDConverionRate"
		    + "  FROM tblbillhd a,tblbilldtl b, tblposmaster e,tblitemmaster f,tblsubgrouphd g,tblgrouphd h,tblmenuitempricingdtl i,tblcostcentermaster j, tblmenuhd k "
		    + "  WHERE a.strBillNo=b.strBillNo "
		    + "  AND DATE(a.dteBillDate)= DATE(b.dteBillDate)   "
		    + "  AND a.strPOSCode=e.strPosCode "
		    + "  AND b.strItemCode=f.strItemCode "
		    + "  AND f.strSubGroupCode=g.strSubGroupCode "
		    + "  AND g.strGroupCode=h.strGroupCode "
		    + "  and b.strItemCode=i.strItemCode "
		    + "  and (a.strPOSCode=i.strPosCode or i.strPosCode='All') "
		    + "  and i.strCostCenterCode=j.strCostCenterCode "
		    + "  and i.strMenuCode = k.strMenuCode "
		    + "  and b.dblAmount = 0  "
		    + " and i.strHourlyPricing='NO' ");
	    if (!costCenterCode.equalsIgnoreCase("All"))
	    {
		sbSql.append(" and j.strCostCenterCode = '" + costCenterCode + "' ");
	    }
	    sbSql.append(" and DATE(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		    + "group by j.strCostCenterName, b.strItemCode, b.stritemname) b "
		    + "on a.strItemCode = b.strItemCode "
		    + " "
		    + "Order by a.strCostCenterName, a.itemName ");
	    ResultSet rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
	    while (rsSales.next())
	    {
		clsItemWiseConsumption objItemWiseConsumption = null;
		if (null != hmItemWiseConsumption.get(rsSales.getString(2) + "!" + rsSales.getString(3)))
		{
		    objItemWiseConsumption = hmItemWiseConsumption.get(rsSales.getString(2) + "!" + rsSales.getString(3));
		    objItemWiseConsumption.setSaleQty(objItemWiseConsumption.getSaleQty() + rsSales.getDouble(4));
		    objItemWiseConsumption.setSaleAmt(objItemWiseConsumption.getSaleAmt() + (rsSales.getDouble(5)));
		    //objItemWiseConsumption.setSubTotal(objItemWiseConsumption.getSubTotal() + rsSales.getDouble(4));
		    //objItemWiseConsumption.setTotalQty(objItemWiseConsumption.getTotalQty() + rsSales.getDouble(3));
		}
		else
		{
		    sqlNo++;
		    objItemWiseConsumption = new clsItemWiseConsumption();
		    objItemWiseConsumption.setCostCenterName(rsSales.getString(1));
		    objItemWiseConsumption.setItemCode(rsSales.getString(2));
		    objItemWiseConsumption.setItemName(rsSales.getString(3));
		    objItemWiseConsumption.setSaleQty(rsSales.getDouble(4));
		    objItemWiseConsumption.setComplimentaryQty(rsSales.getDouble(6));
		    objItemWiseConsumption.setComplimentaryAmt(rsSales.getDouble(7));
		    objItemWiseConsumption.setNcQty(0);
		    objItemWiseConsumption.setSubTotal(rsSales.getDouble(5));
		    objItemWiseConsumption.setSeqNo(sqlNo);
		}
		if (null != objItemWiseConsumption)
		{
		    hmItemWiseConsumption.put(rsSales.getString(2) + "!" + rsSales.getString(3), objItemWiseConsumption);
		    if (null != hmMenuHdAmt.get(rsSales.getString(1)))//check menu h
		    {
			double subTot = hmMenuHdAmt.get(rsSales.getString(1));
			hmMenuHdAmt.put(rsSales.getString(1), objItemWiseConsumption.getSubTotal() + subTot);
		    }
		    else
		    {
			hmMenuHdAmt.put(rsSales.getString(1), objItemWiseConsumption.getSubTotal());
		    }

		}
	    }
	    rsSales.close();

	    //live modifiers
	    String amount = "b.dblamount";
	    String rate="b.dblRate";
	    String discAmt = "b.dblDiscAmt";
	    if(currency.equalsIgnoreCase("USD"))
	    {
		amount = "b.dblamount/a.dblUSDConverionRate";
		rate="b.dblRate/a.dblUSDConverionRate";
		discAmt = "b.dblDiscAmt/a.dblUSDConverionRate";
	    }	
	    
	    sbSqlMod.setLength(0);
	    // Code for Sales Qty for modifier live & q data
	    sbSqlMod.append("SELECT b.strItemCode, UPPER(b.strModifierName),b.dblQuantity,"+amount+","+rate+",e.strposname,"+discAmt+",g.strSubGroupName,h.strGroupName,a.strBillNo,f.strExternalCode\n"
		    + ",j.strMenuCode,j.strMenuName,k.strCostCenterName "
		    + "FROM tblbillhd a,tblbillmodifierdtl b,tblposmaster e,tblitemmaster f,tblsubgrouphd g\n"
		    + ",tblgrouphd h,tblmenuitempricingdtl i,tblmenuhd j,tblcostcentermaster k "
		    + "WHERE a.strBillNo=b.strBillNo \n"
		    + "AND DATE(a.dteBillDate)= DATE(b.dteBillDate) \n"
		    + "AND a.strPOSCode=e.strPosCode \n"
		    + "AND LEFT(b.strItemCode,7)=f.strItemCode \n"
		    + "AND f.strSubGroupCode=g.strSubGroupCode \n"
		    + "AND g.strGroupCode=h.strGroupCode \n"
		    + "AND LEFT(b.strItemCode,7)=i.strItemCode \n"
		    + "and i.strMenuCode=j.strMenuCode\n"
		    + "and (a.strPOSCode=e.strPosCode or i.strPosCode='All') "
		    + "and i.strCostCenterCode=k.strCostCenterCode "
		    + "AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "'\n"
		    + "  and i.strHourlyPricing='NO'  ");
	    if (printZeroAmountModi.equalsIgnoreCase("Yes"))//Tjs brew works dont want modifiers details
	    {
		sbSqlMod.append(" GROUP BY b.strItemCode,b.strModifierName ");
	    }
	    else
	    {
		sbSqlMod.append(" And  b.dblamount >0 ");
		sbSqlMod.append(" GROUP BY b.strItemCode,b.strModifierName ");
	    }
	    sbSqlMod.append(sbFilters);

	    rsSalesMod = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlMod.toString());
	    while (rsSalesMod.next())
	    {
		clsItemWiseConsumption objItemWiseConsumption = null;
		if (null != hmItemWiseConsumption.get(rsSalesMod.getString(1) + "!" + rsSalesMod.getString(2)))
		{
		    objItemWiseConsumption = hmItemWiseConsumption.get(rsSalesMod.getString(1) + "!" + rsSalesMod.getString(2));
		    objItemWiseConsumption.setSaleQty(objItemWiseConsumption.getSaleQty() + rsSalesMod.getDouble(3));
		    objItemWiseConsumption.setSaleAmt(objItemWiseConsumption.getSaleAmt() + (rsSalesMod.getDouble(4) - rsSalesMod.getDouble(7)));
		    objItemWiseConsumption.setSubTotal(objItemWiseConsumption.getSubTotal() + rsSalesMod.getDouble(4));
		    //objItemWiseConsumption.setTotalQty(objItemWiseConsumption.getTotalQty() + rsSalesMod.getDouble(3));
		}
		else
		{
		    sqlNo++;
		    objItemWiseConsumption = new clsItemWiseConsumption();

		    objItemWiseConsumption.setItemCode(rsSalesMod.getString(1));
		    objItemWiseConsumption.setItemName(rsSalesMod.getString(2));
		    objItemWiseConsumption.setSubGroupName(rsSalesMod.getString(8));
		    objItemWiseConsumption.setGroupName(rsSalesMod.getString(9));
		    objItemWiseConsumption.setSaleQty(rsSalesMod.getDouble(3));
		    objItemWiseConsumption.setComplimentaryQty(0);
		    objItemWiseConsumption.setMenuHead(rsSalesMod.getString(13));
		    objItemWiseConsumption.setCostCenterName(rsSalesMod.getString(14));
		    objItemWiseConsumption.setNcQty(0);
		    objItemWiseConsumption.setSubTotal(rsSalesMod.getDouble(4));
		    double totalRowQty = rsSalesMod.getDouble(3) + 0 + 0 + 0;
		    //objItemWiseConsumption.setTotalQty(totalRowQty);

		}
		if (null != objItemWiseConsumption)
		{
		    hmItemWiseConsumption.put(rsSalesMod.getString(1) + "!" + rsSalesMod.getString(2), objItemWiseConsumption);
		    if (null != hmMenuHdAmt.get(rsSalesMod.getString(14)))//check menu h
		    {
			double subTot = hmMenuHdAmt.get(rsSalesMod.getString(14));
			hmMenuHdAmt.put(rsSalesMod.getString(14), objItemWiseConsumption.getSubTotal() + subTot);
		    }
		    else
		    {
			hmMenuHdAmt.put(rsSalesMod.getString(14), objItemWiseConsumption.getSubTotal());
		    }
		}
	    }
	    rsSalesMod.close();

	    // for Sales Qty for bill detail q data 
	    sbSql.setLength(0);
	    sbSql.append(" select  a.strCostCenterName, a.stritemcode, upper(a.itemName),a.RegularQty-IFNULL(b.CompQty,0)RegularQty, "+regularAmt+",  "
		    + "ifnull(b.CompQty,0), ifnull("+compAmt+",0) ,a.strBillNo,a.strMenuName from  "
		    + "(SELECT j.strCostCenterName, b.stritemcode, upper(b.stritemname) itemName, SUM(b.dblQuantity) RegularQty, "+dblAmount+" RegularAmt,a.strBillNo ,k.strMenuName,a.dblUSDConverionRate dblUSDConverionRate"
		    + "  FROM tblqbillhd a,tblqbilldtl b, tblposmaster e,tblitemmaster f,tblsubgrouphd g,tblgrouphd h,tblmenuitempricingdtl i,tblcostcentermaster j, tblmenuhd k "
		    + "  WHERE a.strBillNo=b.strBillNo "
		    + "  AND DATE(a.dteBillDate)= DATE(b.dteBillDate)   "
		    + "  AND a.strPOSCode=e.strPosCode "
		    + "  AND b.strItemCode=f.strItemCode "
		    + "  AND f.strSubGroupCode=g.strSubGroupCode "
		    + "  AND g.strGroupCode=h.strGroupCode "
		    + "  and b.strItemCode=i.strItemCode "
		    + "  and (a.strPOSCode=i.strPosCode or i.strPosCode='All') "
		    + "  and i.strCostCenterCode=j.strCostCenterCode "
		    + "  and i.strMenuCode = k.strMenuCode "
		    + "  and i.strHourlyPricing='NO' ");
//		    + "  and b.dblAmount <> 0  ");
	    if (!costCenterCode.equalsIgnoreCase("All"))
	    {
		sbSql.append(" and j.strCostCenterCode = '" + costCenterCode + "' ");
	    }

	    sbSql.append("  and DATE(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		    + "group by j.strCostCenterName, b.strItemCode, b.stritemname) a "
		    + " left outer join   "
		    + "(SELECT j.strCostCenterName, b.stritemcode, upper(b.stritemname), SUM(b.dblQuantity) CompQty, "+dblAmount+" CompAmt ,a.strBillNo ,k.strMenuName,a.dblUSDConverionRate dblUSDConverionRate"
		    + "  FROM tblqbillhd a,tblqbilldtl b, tblposmaster e,tblitemmaster f,tblsubgrouphd g,tblgrouphd h,tblmenuitempricingdtl i,tblcostcentermaster j, tblmenuhd k "
		    + "  WHERE a.strBillNo=b.strBillNo "
		    + "  AND DATE(a.dteBillDate)= DATE(b.dteBillDate)   "
		    + "  AND a.strPOSCode=e.strPosCode "
		    + "  AND b.strItemCode=f.strItemCode "
		    + "  AND f.strSubGroupCode=g.strSubGroupCode "
		    + "  AND g.strGroupCode=h.strGroupCode "
		    + "  and b.strItemCode=i.strItemCode "
		    + "  and (a.strPOSCode=i.strPosCode or i.strPosCode='All') "
		    + "  and i.strCostCenterCode=j.strCostCenterCode "
		    + "  and i.strMenuCode = k.strMenuCode "
		    + "  and b.dblAmount = 0  "
		    + "  and i.strHourlyPricing='NO'  ");
	    if (!costCenterCode.equalsIgnoreCase("All"))
	    {
		sbSql.append(" and j.strCostCenterCode = '" + costCenterCode + "' ");
	    }
	    sbSql.append(" and DATE(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		    + "group by j.strCostCenterName, b.strItemCode, b.stritemname) b "
		    + "on a.strItemCode = b.strItemCode "
		    + " "
		    + "Order by a.strCostCenterName, a.itemName ");
	    rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
	    while (rsSales.next())
	    {
		clsItemWiseConsumption objItemWiseConsumption = null;
		if (null != hmItemWiseConsumption.get(rsSales.getString(1) + "!" + rsSales.getString(2)))
		{
		    objItemWiseConsumption = hmItemWiseConsumption.get(rsSales.getString(2) + "!" + rsSales.getString(3));
		    objItemWiseConsumption.setSaleQty(objItemWiseConsumption.getSaleQty() + rsSales.getDouble(4));
		    objItemWiseConsumption.setSaleAmt(objItemWiseConsumption.getSaleAmt() + (rsSales.getDouble(5)));

		}
		else
		{
		    sqlNo++;
		    objItemWiseConsumption = new clsItemWiseConsumption();
		    objItemWiseConsumption.setCostCenterName(rsSales.getString(1));
		    objItemWiseConsumption.setItemCode(rsSales.getString(2));
		    objItemWiseConsumption.setItemName(rsSales.getString(3));
		    objItemWiseConsumption.setSaleQty(rsSales.getDouble(4));
		    objItemWiseConsumption.setComplimentaryQty(rsSales.getDouble(6));
		    objItemWiseConsumption.setComplimentaryAmt(rsSales.getDouble(7));
		    objItemWiseConsumption.setNcQty(0);
		    objItemWiseConsumption.setSubTotal(rsSales.getDouble(5));
		    objItemWiseConsumption.setSeqNo(sqlNo);
		}
		if (null != objItemWiseConsumption)
		{
		    hmItemWiseConsumption.put(rsSales.getString(2) + "!" + rsSales.getString(3), objItemWiseConsumption);
		    if (null != hmMenuHdAmt.get(rsSales.getString(1)))//check menu h
		    {
			double subTot = hmMenuHdAmt.get(rsSales.getString(1));
			hmMenuHdAmt.put(rsSales.getString(1), objItemWiseConsumption.getSubTotal() + subTot);
		    }
		    else
		    {
			hmMenuHdAmt.put(rsSales.getString(1), objItemWiseConsumption.getSubTotal());
		    }
		}
	    }
	    rsSales.close();

	    //Q modifiers
	    sbSqlMod.setLength(0);
	    // Code for Sales Qty for modifier live & q data
	    sbSqlMod.append("SELECT b.strItemCode, UPPER(b.strModifierName),b.dblQuantity,"+amount+","+rate+",e.strposname,"+discAmt+",g.strSubGroupName,h.strGroupName,a.strBillNo,f.strExternalCode\n"
		    + ",j.strMenuCode,j.strMenuName,k.strCostCenterName "
		    + "FROM tblqbillhd a,tblqbillmodifierdtl b,tblposmaster e,tblitemmaster f,tblsubgrouphd g\n"
		    + ",tblgrouphd h,tblmenuitempricingdtl i,tblmenuhd j,tblcostcentermaster k "
		    + "WHERE a.strBillNo=b.strBillNo \n"
		    + "AND DATE(a.dteBillDate)= DATE(b.dteBillDate) \n"
		    + "AND a.strPOSCode=e.strPosCode \n"
		    + "AND LEFT(b.strItemCode,7)=f.strItemCode \n"
		    + "AND f.strSubGroupCode=g.strSubGroupCode \n"
		    + "AND g.strGroupCode=h.strGroupCode \n"
		    + "AND LEFT(b.strItemCode,7)=i.strItemCode \n"
		    + "and i.strMenuCode=j.strMenuCode\n"
		    + "and (a.strPOSCode=e.strPosCode or i.strPosCode='All') "
		    + "and i.strCostCenterCode=k.strCostCenterCode "
		    + "AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
		    + "  and i.strHourlyPricing='NO'  "
		    + " ");
	    if (printZeroAmountModi.equalsIgnoreCase("Yes"))//Tjs brew works dont want modifiers details
	    {
		sbSqlMod.append(" GROUP BY b.strItemCode,b.strModifierName ");
	    }
	    else
	    {
		sbSqlMod.append(" And  b.dblamount >0 ");
		sbSqlMod.append(" GROUP BY b.strItemCode,b.strModifierName ");
	    }
	    sbSqlMod.append(sbFilters);

	    rsSalesMod = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlMod.toString());
	    while (rsSalesMod.next())
	    {
		clsItemWiseConsumption objItemWiseConsumption = null;
		if (null != hmItemWiseConsumption.get(rsSalesMod.getString(1) + "!" + rsSalesMod.getString(2)))
		{
		    objItemWiseConsumption = hmItemWiseConsumption.get(rsSalesMod.getString(1) + "!" + rsSalesMod.getString(2));
		    objItemWiseConsumption.setSaleQty(objItemWiseConsumption.getSaleQty() + rsSalesMod.getDouble(3));
		    objItemWiseConsumption.setSaleAmt(objItemWiseConsumption.getSaleAmt() + (rsSalesMod.getDouble(4) - rsSalesMod.getDouble(7)));
		    objItemWiseConsumption.setSubTotal(objItemWiseConsumption.getSubTotal() + rsSalesMod.getDouble(4));
		    //objItemWiseConsumption.setTotalQty(objItemWiseConsumption.getTotalQty() + rsSalesMod.getDouble(3));
		}
		else
		{
		    sqlNo++;
		    objItemWiseConsumption = new clsItemWiseConsumption();

		    objItemWiseConsumption.setItemCode(rsSalesMod.getString(1));
		    objItemWiseConsumption.setItemName(rsSalesMod.getString(2));
		    objItemWiseConsumption.setSubGroupName(rsSalesMod.getString(8));
		    objItemWiseConsumption.setGroupName(rsSalesMod.getString(9));
		    objItemWiseConsumption.setSaleQty(rsSalesMod.getDouble(3));
		    objItemWiseConsumption.setComplimentaryQty(0);
		    objItemWiseConsumption.setMenuHead(rsSalesMod.getString(13));
		    objItemWiseConsumption.setCostCenterName(rsSalesMod.getString(14));
		    objItemWiseConsumption.setNcQty(0);
		    objItemWiseConsumption.setSubTotal(rsSalesMod.getDouble(4));
		    double totalRowQty = rsSalesMod.getDouble(3) + 0 + 0 + 0;
		    //objItemWiseConsumption.setTotalQty(totalRowQty);

		}
		if (null != objItemWiseConsumption)
		{
		    hmItemWiseConsumption.put(rsSalesMod.getString(1) + "!" + rsSalesMod.getString(2), objItemWiseConsumption);
		    if (null != hmMenuHdAmt.get(rsSalesMod.getString(14)))//check menu h
		    {
			double subTot = hmMenuHdAmt.get(rsSalesMod.getString(14));
			hmMenuHdAmt.put(rsSalesMod.getString(14), objItemWiseConsumption.getSubTotal() + subTot);
		    }
		    else
		    {
			hmMenuHdAmt.put(rsSalesMod.getString(14), objItemWiseConsumption.getSubTotal());
		    }
		}
	    }
	    rsSalesMod.close();

	    double totalSaleAmt = 0;

	    List<clsItemWiseConsumption> list = new ArrayList<clsItemWiseConsumption>();
	    for (Map.Entry<String, clsItemWiseConsumption> entry : hmItemWiseConsumption.entrySet())
	    {
		clsItemWiseConsumption objItemComp = entry.getValue();
		list.add(objItemComp);
	    }

	    Comparator<clsItemWiseConsumption> costCenterComparator = new Comparator<clsItemWiseConsumption>()
	    {

		@Override
		public int compare(clsItemWiseConsumption o1, clsItemWiseConsumption o2)
		{
		    return o1.getCostCenterName().compareToIgnoreCase(o2.getCostCenterName());
		}
	    };

	    Comparator<clsItemWiseConsumption> itemCodeComparator = new Comparator<clsItemWiseConsumption>()
	    {

		@Override
		public int compare(clsItemWiseConsumption o1, clsItemWiseConsumption o2)
		{
		    return o1.getItemName().compareToIgnoreCase(o2.getItemName());
		}
	    };

	    Collections.sort(list, new clsItemConsumptionComparator(costCenterComparator, itemCodeComparator
	    ));

	    //System.out.println(list);
	    //call for view report
	    if (reportType.equalsIgnoreCase("A4 Size Report"))
	    {
		funViewJasperReportForBeanCollectionDataSource(is, hm, list);
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }
    
       public void funItemWiseConsumptionMonthWise(String reportType, HashMap hm, String dayEnd)
    {

	try
	{
	    InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptItemConsumptionMonthWiseReport1.jasper");
	    String fromDate = hm.get("fromDate").toString();
	    String toDate = hm.get("toDate").toString();
	    String posCode = hm.get("posCode").toString();
	    String shiftNo = hm.get("shiftNo").toString();
	    String posName = hm.get("posName").toString();
	    String groupCode = hm.get("GroupCode").toString();
	    String groupName = hm.get("GroupName").toString();
	    String costCenterCode = hm.get("costCenterCode").toString();
	    String printZeroAmountModi = hm.get("PrintZeroAmountModi").toString();
	    String currency = hm.get("currency").toString();
	    String costCenterCd = "", costCenterNm = "";

	    boolean isDayEndHappend = objUtility2.isDayEndHappened(toDate);
	    if (!isDayEndHappend)
	    {
		hm.put("isDayEndHappend", "DAY END NOT DONE.");
	    }

	    SimpleDateFormat parser = new SimpleDateFormat("dd-MM-yyyy");
	    Date dateFrom = parser.parse(hm.get("fromDateToDisplay").toString());
	    Date dateTo = parser.parse(hm.get("toDateToDisplay").toString());
	    hm.put("fromDateToDisplay", new SimpleDateFormat("dd-MM-yyyy").format(dateFrom));
	    hm.put("toDateToDisplay", new SimpleDateFormat("dd-MM-yyyy").format(dateTo));

	    int sqlNo = 0;
	    StringBuilder sbSql = new StringBuilder();
	    StringBuilder sbSqlMod = new StringBuilder();
	    StringBuilder sbFilters = new StringBuilder();
	    ResultSet rsSalesMod;
	    Map<String, Map<String,clsItemConsumptionMonthWiseBean>> hmItemWiseConsumption = new TreeMap<String, Map<String,clsItemConsumptionMonthWiseBean>>();
	    Map<String, clsItemConsumptionMonthWiseBean> hmItemWise = new HashMap<String, clsItemConsumptionMonthWiseBean>();
	    
	    List<clsItemConsumptionMonthWiseBean> objItemConsumptionMonthWise = new ArrayList<clsItemConsumptionMonthWiseBean>();

	    String regularAmt = "a.RegularAmt";
	    String compAmt = "b.CompAmt";
	    String dblAmount = "SUM(b.dblamount)";
	    if(currency.equalsIgnoreCase("USD"))
	    {
		dblAmount = "SUM(b.dblamount)/a.dblUSDConverionRate";
	    }	
	    
	   sbSql.setLength(0); 
	   sbSql.append("SELECT b.stritemcode, UPPER(b.stritemname) itemName, SUM(b.dblQuantity) RegularQty, "+dblAmount+" RegularAmt\n" 
		+ " ,DATE_FORMAT(a.dteBillDate,'%M') AS dteDate, DATE_FORMAT(a.dteBillDate,'%m') AS monthNo,k.strMenuName\n" 
		+ " FROM tblqbillhd a,tblqbilldtl b, tblposmaster e,tblitemmaster f,tblsubgrouphd g,tblgrouphd h,tblmenuitempricingdtl i,tblcostcentermaster j, tblmenuhd k\n" 
		+ " WHERE a.strBillNo=b.strBillNo AND DATE(a.dteBillDate)= DATE(b.dteBillDate) AND a.strPOSCode=e.strPosCode\n" 
		+ " AND b.strItemCode=f.strItemCode AND f.strSubGroupCode=g.strSubGroupCode AND g.strGroupCode=h.strGroupCode\n" 
		+ " AND b.strItemCode=i.strItemCode AND (a.strPOSCode=i.strPosCode OR i.strPosCode='All') \n" 
		+ " AND i.strCostCenterCode=j.strCostCenterCode AND i.strMenuCode = k.strMenuCode \n" 
		+ " AND i.strHourlyPricing='NO' AND DATE(a.dteBillDate) BETWEEN '"+fromDate+"' AND '"+toDate+"'");
	    if (!costCenterCode.equalsIgnoreCase("All"))
	    {
		sbSql.append(" and j.strCostCenterCode = '" + costCenterCode + "' ");
	    }
	    sbSql.append( "GROUP BY dteDate, b.strItemCode, b.stritemname,k.strMenuName\n" 
		+ " ORDER BY monthNo,k.strMenuName, b.stritemname");
	    ResultSet rsLiveData = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
	    objItemConsumptionMonthWise = new ArrayList<clsItemConsumptionMonthWiseBean>();
	    hmItemWise = new HashMap<String, clsItemConsumptionMonthWiseBean>();
	    String prevMonth ="";
	    while(rsLiveData.next())
	    {
		if(!rsLiveData.getString(6).equalsIgnoreCase(prevMonth))
		{    
		    hmItemWise = new HashMap<String, clsItemConsumptionMonthWiseBean>();
		}
		   
		clsItemConsumptionMonthWiseBean objItemWiseConsumption = null;
		if(hmItemWiseConsumption.containsKey(rsLiveData.getString(6)))
		{
		    objItemWiseConsumption = new clsItemConsumptionMonthWiseBean();
		    objItemWiseConsumption.setStrItemName(rsLiveData.getString(2)); //item name
		    objItemWiseConsumption.setQty1(rsLiveData.getDouble(3)); // qty
		    objItemWiseConsumption.setStrMonth1(rsLiveData.getString(6));//month name
		    objItemWiseConsumption.setDblAmount(rsLiveData.getDouble(4)); //amount
		    objItemWiseConsumption.setStrItemCode(rsLiveData.getString(1)); //item code
		    objItemWiseConsumption.setStrMenuHeadName(rsLiveData.getString(7)); //menu head name
		}   
		else
		{
		    objItemWiseConsumption = new clsItemConsumptionMonthWiseBean();
		    objItemWiseConsumption.setStrItemName(rsLiveData.getString(2)); //item name
		    objItemWiseConsumption.setQty1(rsLiveData.getDouble(3)); // qty
		    objItemWiseConsumption.setStrMonth1(rsLiveData.getString(6));//month name
		    objItemWiseConsumption.setDblAmount(rsLiveData.getDouble(4)); //amount
		    objItemWiseConsumption.setStrItemCode(rsLiveData.getString(1)); //item code
		    objItemWiseConsumption.setStrMenuHeadName(rsLiveData.getString(7)); //menu head name
		} 
		
		hmItemWise.put(rsLiveData.getString(1), objItemWiseConsumption);
		hmItemWiseConsumption.put(rsLiveData.getString(6),hmItemWise);
		prevMonth = rsLiveData.getString(6);
	    }
	    
	    sbSql.setLength(0);
	    sbSql.append("SELECT b.stritemcode, UPPER(b.stritemname) itemName, SUM(b.dblQuantity) RegularQty, "+dblAmount+" RegularAmt \n" 
		+ " ,DATE_FORMAT(a.dteBillDate,'%M') AS dteDate, DATE_FORMAT(a.dteBillDate,'%m') AS monthNo,k.strMenuName\n" 
		+ " FROM tblbillhd a,tblbilldtl b, tblposmaster e,tblitemmaster f,tblsubgrouphd g,tblgrouphd h,tblmenuitempricingdtl i,tblcostcentermaster j, tblmenuhd k\n" 
		+ " WHERE a.strBillNo=b.strBillNo AND DATE(a.dteBillDate)= DATE(b.dteBillDate) AND a.strPOSCode=e.strPosCode\n" 
		+ " AND b.strItemCode=f.strItemCode AND f.strSubGroupCode=g.strSubGroupCode AND g.strGroupCode=h.strGroupCode\n" 
		+ " AND b.strItemCode=i.strItemCode AND (a.strPOSCode=i.strPosCode OR i.strPosCode='All') \n" 
		+ " AND i.strCostCenterCode=j.strCostCenterCode AND i.strMenuCode = k.strMenuCode \n" 
		+ " AND i.strHourlyPricing='NO' AND DATE(a.dteBillDate) BETWEEN '"+fromDate+"' AND '"+toDate+"'");
	    if (!costCenterCode.equalsIgnoreCase("All"))
	    {
		sbSql.append(" and j.strCostCenterCode = '" + costCenterCode + "' ");
	    }
	    sbSql.append( "GROUP BY dteDate, b.strItemCode, b.stritemname,k.strMenuName\n" 
		+ " ORDER BY monthNo,k.strMenuName, b.stritemname");
	    rsLiveData = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
	    objItemConsumptionMonthWise = new ArrayList<clsItemConsumptionMonthWiseBean>();
	    while(rsLiveData.next())
	    {
		if(!rsLiveData.getString(6).equalsIgnoreCase(prevMonth))
		{    
		    hmItemWise = new HashMap<String, clsItemConsumptionMonthWiseBean>();
		}
		clsItemConsumptionMonthWiseBean objItemWiseConsumption = null;
		if(hmItemWiseConsumption.containsKey(rsLiveData.getString(6)))
		{
		    hmItemWise=hmItemWiseConsumption.get(rsLiveData.getString(6));
		    if(hmItemWise.containsKey(rsLiveData.getString(1))){
			objItemWiseConsumption=hmItemWise.get(rsLiveData.getString(1));
			objItemWiseConsumption.setQty1(objItemWiseConsumption.getQty1()+rsLiveData.getDouble(3)); // qty
			objItemWiseConsumption.setDblAmount(objItemWiseConsumption.getDblAmount()+rsLiveData.getDouble(4)); // amount
		    }
		    else
		    {	
			objItemWiseConsumption = new clsItemConsumptionMonthWiseBean();
			objItemWiseConsumption.setStrItemName(rsLiveData.getString(2)); //item name
			objItemWiseConsumption.setQty1(rsLiveData.getDouble(3)); // qty
			objItemWiseConsumption.setStrMonth1(rsLiveData.getString(6));//month name
			objItemWiseConsumption.setDblAmount(rsLiveData.getDouble(4)); //amount
			objItemWiseConsumption.setStrItemCode(rsLiveData.getString(1)); //item code
			objItemWiseConsumption.setStrMenuHeadName(rsLiveData.getString(7)); //menu head name
		    }
		}   
		else
		{
		    objItemWiseConsumption = new clsItemConsumptionMonthWiseBean();
		    objItemWiseConsumption.setStrItemName(rsLiveData.getString(2)); //item name
		    objItemWiseConsumption.setQty1(rsLiveData.getDouble(3)); // qty
		    objItemWiseConsumption.setStrMonth1(rsLiveData.getString(6));//month name
		    objItemWiseConsumption.setDblAmount(rsLiveData.getDouble(4)); //amount
		    objItemWiseConsumption.setStrItemCode(rsLiveData.getString(1)); //item code
		    objItemWiseConsumption.setStrMenuHeadName(rsLiveData.getString(7)); //menu head name
		} 
		hmItemWise.put(rsLiveData.getString(1), objItemWiseConsumption);
		hmItemWiseConsumption.put(rsLiveData.getString(6),hmItemWise);
		prevMonth = rsLiveData.getString(6);
	    }
	    if (printZeroAmountModi.equalsIgnoreCase("Yes"))
	    {
	    //live modifiers
	    String amount = "b.dblamount";
	    String rate = "b.dblRate";
	    String discAmt = "b.dblDiscAmt";
	    if(currency.equalsIgnoreCase("USD"))
	    {
		amount = "b.dblamount/a.dblUSDConverionRate";
		rate = "b.dblRate/a.dblUSDConverionRate";
		discAmt = "b.dblDiscAmt/a.dblUSDConverionRate";
	    }	
	    
	    sbSqlMod.setLength(0);
	    // Code for Sales Qty for modifier live & q data
	    sbSqlMod.append("SELECT b.strItemCode, UPPER(b.strModifierName),b.dblQuantity,"+amount+",\n" 
		    + "DATE_FORMAT(a.dteBillDate,'%M') AS dteDate, DATE_FORMAT(a.dteBillDate,'%m') AS monthNo,j.strMenuName\n" 
		    + "FROM tblqbillhd a,tblqbillmodifierdtl b,tblposmaster e,tblitemmaster f,tblsubgrouphd g\n" 
		    + ",tblgrouphd h,tblmenuitempricingdtl i,tblmenuhd j\n" 
		    + "WHERE a.strBillNo=b.strBillNo \n" 
		    + "AND DATE(a.dteBillDate)= DATE(b.dteBillDate) \n" 
		    + "AND a.strPOSCode=e.strPosCode \n" 
		    + "AND LEFT(b.strItemCode,7)=f.strItemCode \n" 
		    + "AND f.strSubGroupCode=g.strSubGroupCode \n" 
		    + "AND g.strGroupCode=h.strGroupCode \n" 
		    + "AND LEFT(b.strItemCode,7)=i.strItemCode \n" 
		    + "and i.strMenuCode=j.strMenuCode\n" 
		    + "and (a.strPOSCode=e.strPosCode or i.strPosCode='All')\n" 
		    + "AND DATE(a.dteBillDate) BETWEEN '"+fromDate+"' AND '"+toDate+"'\n" 
		    + "and i.strHourlyPricing='NO' ");
	    if (!costCenterCode.equalsIgnoreCase("All"))
	    {
		sbSqlMod.append(" and i.strCostCenterCode = '" + costCenterCode + "' ");
	    }
	    if (printZeroAmountModi.equalsIgnoreCase("Yes"))//Tjs brew works dont want modifiers details
	    {
		sbSqlMod.append(" GROUP BY dteDate,b.strItemCode,b.strModifierName");
	    }
	    else
	    {
		sbSqlMod.append(" And  b.dblamount >0 ");
		sbSqlMod.append("GROUP BY dteDate,b.strItemCode,b.strModifierName ");
	    }
	    
	    rsLiveData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlMod.toString());
	    hmItemWise = new HashMap<String, clsItemConsumptionMonthWiseBean>();
	    objItemConsumptionMonthWise = new ArrayList<clsItemConsumptionMonthWiseBean>(); 
	    while(rsLiveData.next())
	    {
		if(!rsLiveData.getString(6).equalsIgnoreCase(prevMonth))
		{    
		    hmItemWise = new HashMap<String, clsItemConsumptionMonthWiseBean>();
		}
		
		clsItemConsumptionMonthWiseBean objItemWiseConsumption = null;
		if(hmItemWiseConsumption.containsKey(rsLiveData.getString(6)))
		{
		    hmItemWise=hmItemWiseConsumption.get(rsLiveData.getString(6));
		    if(hmItemWise.containsKey(rsLiveData.getString(1))){
			objItemWiseConsumption=hmItemWise.get(rsLiveData.getString(1));
			objItemWiseConsumption.setQty1(objItemWiseConsumption.getQty1()+rsLiveData.getDouble(3)); // qty
			objItemWiseConsumption.setDblAmount(objItemWiseConsumption.getDblAmount()+rsLiveData.getDouble(4)); // amount
		    }
		    else
		    {	
			objItemWiseConsumption = new clsItemConsumptionMonthWiseBean();
			objItemWiseConsumption.setStrItemName(rsLiveData.getString(2)); //item name
			objItemWiseConsumption.setQty1(rsLiveData.getDouble(3)); // qty
			objItemWiseConsumption.setStrMonth1(rsLiveData.getString(6));//month name
			objItemWiseConsumption.setDblAmount(rsLiveData.getDouble(4)); //amount
			objItemWiseConsumption.setStrItemCode(rsLiveData.getString(1)); //item code
			objItemWiseConsumption.setStrMenuHeadName(rsLiveData.getString(7)); //menu head name
		    }
		}     
		else
		{
		    objItemWiseConsumption = new clsItemConsumptionMonthWiseBean();
		    objItemWiseConsumption.setStrItemName(rsLiveData.getString(2)); //item name
		    objItemWiseConsumption.setQty1(rsLiveData.getDouble(3)); // qty
		    objItemWiseConsumption.setStrMonth1(rsLiveData.getString(6));//month name
		    objItemWiseConsumption.setDblAmount(rsLiveData.getDouble(4)); //amount
		    objItemWiseConsumption.setStrItemCode(rsLiveData.getString(1)); //item code
		    objItemWiseConsumption.setStrMenuHeadName(rsLiveData.getString(7)); //menu head name
		} 
		hmItemWise.put(rsLiveData.getString(1), objItemWiseConsumption);
		hmItemWiseConsumption.put(rsLiveData.getString(6),hmItemWise);
		prevMonth = rsLiveData.getString(6);
	    }
	    
	     sbSqlMod.setLength(0);
	    // Code for Sales Qty for modifier live & q data
	    sbSqlMod.append("SELECT b.strItemCode, UPPER(b.strModifierName),b.dblQuantity,"+amount+",\n" 
		    + "DATE_FORMAT(a.dteBillDate,'%M') AS dteDate, DATE_FORMAT(a.dteBillDate,'%m') AS monthNo,j.strMenuName\n" 
		    + "FROM tblbillhd a,tblbillmodifierdtl b,tblposmaster e,tblitemmaster f,tblsubgrouphd g\n" 
		    + ",tblgrouphd h,tblmenuitempricingdtl i,tblmenuhd j\n" 
		    + "WHERE a.strBillNo=b.strBillNo \n" 
		    + "AND DATE(a.dteBillDate)= DATE(b.dteBillDate) \n" 
		    + "AND a.strPOSCode=e.strPosCode \n" 
		    + "AND LEFT(b.strItemCode,7)=f.strItemCode \n" 
		    + "AND f.strSubGroupCode=g.strSubGroupCode \n" 
		    + "AND g.strGroupCode=h.strGroupCode \n" 
		    + "AND LEFT(b.strItemCode,7)=i.strItemCode \n" 
		    + "and i.strMenuCode=j.strMenuCode\n" 
		    + "and (a.strPOSCode=e.strPosCode or i.strPosCode='All')\n" 
		    + "AND DATE(a.dteBillDate) BETWEEN '"+fromDate+"' AND '"+toDate+"'\n" 
		    + "and i.strHourlyPricing='NO' ");
	    if (!costCenterCode.equalsIgnoreCase("All"))
	    {
		sbSqlMod.append(" and i.strCostCenterCode = '" + costCenterCode + "' ");
	    }
	    if (printZeroAmountModi.equalsIgnoreCase("Yes"))//Tjs brew works dont want modifiers details
	    {
		sbSqlMod.append(" GROUP BY dteDate,b.strItemCode,b.strModifierName");
	    }
	    else
	    {
		sbSqlMod.append(" And  b.dblamount >0 ");
		sbSqlMod.append("GROUP BY dteDate,b.strItemCode,b.strModifierName ");
	    }

	    rsLiveData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlMod.toString());
	    objItemConsumptionMonthWise = new ArrayList<clsItemConsumptionMonthWiseBean>();
	    while(rsLiveData.next())
	    {
		clsItemConsumptionMonthWiseBean objItemWiseConsumption = null;
		if(hmItemWiseConsumption.containsKey(rsLiveData.getString(6)))
		{
		    hmItemWise=hmItemWiseConsumption.get(rsLiveData.getString(6));
		    if(hmItemWise.containsKey(rsLiveData.getString(1))){
			objItemWiseConsumption=hmItemWise.get(rsLiveData.getString(1));
			objItemWiseConsumption.setQty1(objItemWiseConsumption.getQty1()+rsLiveData.getDouble(3)); // qty
			objItemWiseConsumption.setDblAmount(objItemWiseConsumption.getDblAmount()+rsLiveData.getDouble(4)); // amount
		    }
		    else
		    {	
			objItemWiseConsumption = new clsItemConsumptionMonthWiseBean();
			objItemWiseConsumption.setStrItemName(rsLiveData.getString(2)); //item name
			objItemWiseConsumption.setQty1(rsLiveData.getDouble(3)); // qty
			objItemWiseConsumption.setStrMonth1(rsLiveData.getString(6));//month name
			objItemWiseConsumption.setDblAmount(rsLiveData.getDouble(4)); //amount
			objItemWiseConsumption.setStrItemCode(rsLiveData.getString(1)); //item code
			objItemWiseConsumption.setStrMenuHeadName(rsLiveData.getString(7)); //menu head name
		    }
		}     
		else
		{
		    objItemWiseConsumption = new clsItemConsumptionMonthWiseBean();
		    objItemWiseConsumption.setStrItemName(rsLiveData.getString(2)); //item name
		    objItemWiseConsumption.setQty1(rsLiveData.getDouble(3)); // qty
		    objItemWiseConsumption.setStrMonth1(rsLiveData.getString(6));//month name
		    objItemWiseConsumption.setDblAmount(rsLiveData.getDouble(4)); //amount
		    objItemWiseConsumption.setStrItemCode(rsLiveData.getString(1)); //item code
		    objItemWiseConsumption.setStrMenuHeadName(rsLiveData.getString(7)); //menu head name
		} 
		hmItemWise.put(rsLiveData.getString(1), objItemWiseConsumption);
		hmItemWiseConsumption.put(rsLiveData.getString(6),hmItemWise);
		prevMonth = rsLiveData.getString(6);
		
	    }
	    }
	    int i=1;
	    String itemCode = "";
	    ArrayList<String> listMonthsName=new ArrayList<>();
	    Map<String,clsItemConsumptionMonthWiseBean> objRetMap = new HashMap<String,clsItemConsumptionMonthWiseBean>(); 
	    List<clsItemConsumptionMonthWiseBean> list = new ArrayList<clsItemConsumptionMonthWiseBean>();
	    for (Map.Entry<String, Map<String,clsItemConsumptionMonthWiseBean>> entry : hmItemWiseConsumption.entrySet())
            {
		String monthName = entry.getKey();
		listMonthsName.add(monthName);
                Map<String,clsItemConsumptionMonthWiseBean> hmItemDtl = entry.getValue();
                for (Map.Entry<String, clsItemConsumptionMonthWiseBean> entryDtl : hmItemDtl.entrySet())
                {
		    clsItemConsumptionMonthWiseBean objBean = entryDtl.getValue();
		    clsItemConsumptionMonthWiseBean objItemConsumptionMonthWiseBean = new clsItemConsumptionMonthWiseBean();
		    objItemConsumptionMonthWiseBean.setStrItemName(objBean.getStrItemName());
		    itemCode = objBean.getStrItemCode();
		    double totalQty = 0.0,dblTotAmt=0;
		    objItemConsumptionMonthWiseBean.setStrItemCode(itemCode);
		    objItemConsumptionMonthWiseBean.setDblAmount(objBean.getDblAmount());
		    objItemConsumptionMonthWiseBean.setStrMenuHeadName(objBean.getStrMenuHeadName());
		    if(i==1)
		    {
			objItemConsumptionMonthWiseBean.setQty1(objBean.getQty1());
			objItemConsumptionMonthWiseBean.setQty2(0);
			objItemConsumptionMonthWiseBean.setQty3(0);
			objItemConsumptionMonthWiseBean.setQty4(0);
			objItemConsumptionMonthWiseBean.setQty5(0);
			objItemConsumptionMonthWiseBean.setQty6(0);
			objItemConsumptionMonthWiseBean.setStrMonth1(objBean.getStrMonth1());
			objItemConsumptionMonthWiseBean.setStrMonth2("");
			objItemConsumptionMonthWiseBean.setStrMonth3("");
			objItemConsumptionMonthWiseBean.setStrMonth4("");
			objItemConsumptionMonthWiseBean.setStrMonth5("");
			objItemConsumptionMonthWiseBean.setStrMonth6("");
		    }	
		    if(i==2)
		    {
			if(objRetMap.containsKey(itemCode)){
			    totalQty=objBean.getQty1();
			    dblTotAmt=objBean.getDblAmount();
			    objItemConsumptionMonthWiseBean=objRetMap.get(itemCode);
			    objItemConsumptionMonthWiseBean.setQty2(totalQty+objItemConsumptionMonthWiseBean.getQty2());
			    objItemConsumptionMonthWiseBean.setDblAmount(objItemConsumptionMonthWiseBean.getDblAmount()+dblTotAmt);
			    objItemConsumptionMonthWiseBean.setStrMonth2(objBean.getStrMonth1());
			}else{
			    objItemConsumptionMonthWiseBean.setQty2(objBean.getQty1());
			    objItemConsumptionMonthWiseBean.setQty1(0);
			    objItemConsumptionMonthWiseBean.setQty3(0);
			    objItemConsumptionMonthWiseBean.setQty4(0);
			    objItemConsumptionMonthWiseBean.setQty5(0);
			    objItemConsumptionMonthWiseBean.setQty6(0); 
			    objItemConsumptionMonthWiseBean.setStrMonth2(objBean.getStrMonth1());
			}
			
			
		    }
		    if(i==3)
		    {
			if(objRetMap.containsKey(itemCode)){
			    totalQty=objBean.getQty1();
			    dblTotAmt=objBean.getDblAmount();
			    objItemConsumptionMonthWiseBean=objRetMap.get(itemCode);
			    objItemConsumptionMonthWiseBean.setQty3(totalQty+objItemConsumptionMonthWiseBean.getQty3());
			    objItemConsumptionMonthWiseBean.setDblAmount(objItemConsumptionMonthWiseBean.getDblAmount()+dblTotAmt);
			    objItemConsumptionMonthWiseBean.setStrMonth3(objBean.getStrMonth1());
			}else{
			    objItemConsumptionMonthWiseBean.setQty3(objBean.getQty1());
			    objItemConsumptionMonthWiseBean.setQty2(0);
			    objItemConsumptionMonthWiseBean.setQty1(0);
			    objItemConsumptionMonthWiseBean.setQty4(0);
			    objItemConsumptionMonthWiseBean.setQty5(0);
			    objItemConsumptionMonthWiseBean.setQty6(0);
			    objItemConsumptionMonthWiseBean.setStrMonth3(objBean.getStrMonth1());
			    }
		    }
		    if(i==4)
		    {
			if(objRetMap.containsKey(itemCode)){
			    totalQty=objBean.getQty1();
			    dblTotAmt=objBean.getDblAmount();
			    objItemConsumptionMonthWiseBean=objRetMap.get(itemCode);
			    objItemConsumptionMonthWiseBean.setQty4(totalQty+objItemConsumptionMonthWiseBean.getQty4());
			    objItemConsumptionMonthWiseBean.setDblAmount(objItemConsumptionMonthWiseBean.getDblAmount()+dblTotAmt);
			    objItemConsumptionMonthWiseBean.setStrMonth4(objBean.getStrMonth1());
			}else{
			    objItemConsumptionMonthWiseBean.setQty4(objBean.getQty1());
			    objItemConsumptionMonthWiseBean.setQty2(0);
			    objItemConsumptionMonthWiseBean.setQty3(0);
			    objItemConsumptionMonthWiseBean.setQty1(0);
			    objItemConsumptionMonthWiseBean.setQty5(0);
			    objItemConsumptionMonthWiseBean.setQty6(0);
			    objItemConsumptionMonthWiseBean.setStrMonth4(objBean.getStrMonth1());	 		
			}
						
			
		    }
		    if(i==5)
		    {
			if(objRetMap.containsKey(itemCode)){
			    totalQty=objBean.getQty1();
			    dblTotAmt=objBean.getDblAmount();
			    objItemConsumptionMonthWiseBean=objRetMap.get(itemCode);
			    objItemConsumptionMonthWiseBean.setQty5(totalQty+objItemConsumptionMonthWiseBean.getQty5());
			    objItemConsumptionMonthWiseBean.setDblAmount(objItemConsumptionMonthWiseBean.getDblAmount()+dblTotAmt);
			    objItemConsumptionMonthWiseBean.setStrMonth5(objBean.getStrMonth1());
			}else{
        		    objItemConsumptionMonthWiseBean.setQty5(objBean.getQty1());
			    objItemConsumptionMonthWiseBean.setQty2(0);
			    objItemConsumptionMonthWiseBean.setQty3(0);
			    objItemConsumptionMonthWiseBean.setQty4(0);
			    objItemConsumptionMonthWiseBean.setQty1(0);
			    objItemConsumptionMonthWiseBean.setQty6(0);
			    objItemConsumptionMonthWiseBean.setStrMonth5(objBean.getStrMonth1());
			}
			
		    }
		    if(i==6)
		    {
			if(objRetMap.containsKey(itemCode)){
			    totalQty=objBean.getQty1();
			    dblTotAmt=objBean.getDblAmount();
			    objItemConsumptionMonthWiseBean=objRetMap.get(itemCode);
			    objItemConsumptionMonthWiseBean.setQty6(totalQty+objItemConsumptionMonthWiseBean.getQty6());
			    objItemConsumptionMonthWiseBean.setDblAmount(objItemConsumptionMonthWiseBean.getDblAmount()+dblTotAmt);
			    objItemConsumptionMonthWiseBean.setStrMonth6(objBean.getStrMonth1());
			}else{
			    objItemConsumptionMonthWiseBean.setQty6(objBean.getQty1());
			    objItemConsumptionMonthWiseBean.setQty2(0);
			    objItemConsumptionMonthWiseBean.setQty3(0);
			    objItemConsumptionMonthWiseBean.setQty4(0);
			    objItemConsumptionMonthWiseBean.setQty5(0);
			    objItemConsumptionMonthWiseBean.setQty1(0);
			    objItemConsumptionMonthWiseBean.setStrMonth6(objBean.getStrMonth1());
			}
			
		    
		    }  
		   objRetMap.put(itemCode,objItemConsumptionMonthWiseBean);
                  
                }
		i++;
            }
	    for (Map.Entry<String, clsItemConsumptionMonthWiseBean> entry : objRetMap.entrySet())
	    {
		clsItemConsumptionMonthWiseBean objItemComp = entry.getValue();
		list.add(objItemComp);
	    }
	    Map<String,String> mapMonthNames = new HashMap<String,String>();
	    mapMonthNames.put("01","January");
	    mapMonthNames.put("02","February");
	    mapMonthNames.put("03","March");
	    mapMonthNames.put("04","April");
	    mapMonthNames.put("05","May");
	    mapMonthNames.put("06","June");
	    mapMonthNames.put("07","July");
	    mapMonthNames.put("08","August");
	    mapMonthNames.put("09","September");
	    mapMonthNames.put("10","October");
	    mapMonthNames.put("11","November");
	    mapMonthNames.put("12","December");
	    String monthName = "";
	    for(int j=0;j<listMonthsName.size();j++)
	    {
		int k = j;
		k = k+1;
		if(mapMonthNames.containsKey(listMonthsName.get(j)))
		{
		   monthName =  mapMonthNames.get(listMonthsName.get(j));
		}    
		hm.put("month"+k,monthName );
	    }	
	    
	    Comparator<clsItemConsumptionMonthWiseBean> menuCodeComparator = new Comparator<clsItemConsumptionMonthWiseBean>()
	    {

		@Override
		public int compare(clsItemConsumptionMonthWiseBean o1, clsItemConsumptionMonthWiseBean o2)
		{
		    return o1.getStrMenuHeadName().compareToIgnoreCase(o2.getStrMenuHeadName());
		}
	    };
	    Comparator<clsItemConsumptionMonthWiseBean> itemCodeComparator = new Comparator<clsItemConsumptionMonthWiseBean>()
	    {

		@Override
		public int compare(clsItemConsumptionMonthWiseBean o1, clsItemConsumptionMonthWiseBean o2)
		{
		    return o1.getStrItemName().compareToIgnoreCase(o2.getStrItemName());
		}
	    };
	    
	    

	    Collections.sort(list, new clsItemConsumptionMonthWiseComparator(menuCodeComparator,itemCodeComparator));
	    if (reportType.equalsIgnoreCase("A4 Size Report"))
	    {
		funViewJasperReportForBeanCollectionDataSource(is, hm, list);
	    }
	    
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }


}
