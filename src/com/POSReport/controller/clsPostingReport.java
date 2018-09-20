package com.POSReport.controller;

import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsPosConfigFile;
import com.POSGlobal.controller.clsSettelementOptions;
import java.awt.Desktop;
import java.awt.Dimension;
import java.io.File;
import java.io.InputStream;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import jxl.Workbook;
import jxl.format.Alignment;
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

public class clsPostingReport
{

    DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();
    Map<String, List<clsSettelementOptions>> hmSalesSettleData = new HashMap<String, List<clsSettelementOptions>>();
    List<clsSettelementOptions> arrListSettleData = null;
    List<clsSettelementOptions> arrListCreditSettleData = null;
    Map<String, List<clsSettelementOptions>> hmSalesGroupWiseSaleData = new HashMap<String, List<clsSettelementOptions>>();
    List<clsSettelementOptions> arrListGroupwiseSaleData = null;
    Map<String, Double> mapDineIn = new HashMap<>();
    Map<String, Double> mapTakeAway = new HashMap<>();
    Map<String, Double> mapHomeDel = new HashMap<>();
    Map<String, List<clsSettelementOptions>> hmSalesTaxWiseSaleData = new HashMap<String, List<clsSettelementOptions>>();
    List<clsSettelementOptions> arrListTaxwiseSaleData = null;
    public void funPostingReport(String reportType, HashMap hm)
    {
	try
	{
	    InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptPostingReport.jasper");

	    String fromDate = hm.get("fromDate").toString();
	    String toDate = hm.get("toDate").toString();
	    String posCode = hm.get("posCode").toString();
	    String shiftNo = hm.get("shiftNo").toString();
	    String posName = hm.get("posName").toString();
	    String fromDateToDisplay = hm.get("fromDateToDisplay").toString();
	    String toDateToDisplay = hm.get("toDateToDisplay").toString();
	    String currency = hm.get("currency").toString();
	    int count = 0;
	    ////For Settlement details of Live and Q data
	    StringBuilder sqlQData = new StringBuilder();
	    StringBuilder sqlModQData = new StringBuilder();
	    StringBuilder sqlLiveData = new StringBuilder();
	    StringBuilder sqlModLiveData = new StringBuilder();
	    String settlementAmt="sum(b.dblSettlementAmt)+sum(a.dblTipAmount)";
	    if(currency.equalsIgnoreCase("USD"))
	    {
		settlementAmt="(sum(b.dblSettlementAmt)+sum(a.dblTipAmount))/a.dblUSDConverionRate";
	    }	
	    
	    sqlQData.append(" select c.strSettelmentDesc,c.strSettelmentType,"+settlementAmt+" "
		    + " from tblqbillhd a,tblqbillsettlementdtl b,tblsettelmenthd c "
		    + " where a.strBillNo=b.strBillNo "
		    + " and date(a.dteBillDate)=date(b.dteBillDate) "
		    + " and b.strSettlementCode=c.strSettelmentCode  "
		    + " and c.strSettelmentType!='Complementary' "
		    + " and c.strSettelmentType!='Credit' "
		    + " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    if (!posCode.equals("All"))
	    {
		sqlQData.append(" and a.strPOSCode='" + posCode + "' ");
	    }
	    sqlQData.append(" group by c.strSettelmentCode ");
	    funSettlementWiseQLiveData(sqlQData);
	    
	    sqlLiveData.append(" select c.strSettelmentDesc,c.strSettelmentType,"+settlementAmt+" "
		    + " from tblbillhd a,tblbillsettlementdtl b,tblsettelmenthd c "
		    + " where a.strBillNo=b.strBillNo "
		    + " and date(a.dteBillDate)=date(b.dteBillDate) "
		    + " and b.strSettlementCode=c.strSettelmentCode  "
		    + " and c.strSettelmentType!='Complementary' "
		    + " and c.strSettelmentType!='Credit' "
		    + " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    if (!posCode.equals("All"))
	    {
		sqlLiveData.append(" and a.strPOSCode='" + posCode + "' ");
	    }
	    sqlLiveData.append(" group by c.strSettelmentCode ");
	    funSettlementWiseQLiveData(sqlLiveData);
	
	    sqlQData.setLength(0);
	    sqlQData.append(" select ifnull(d.strCustomerName,'NA'),c.strSettelmentType,"+settlementAmt+" "
		    + ",a.strKOTToBillNote "
		    + " from tblqbillhd a "
		    + " inner join tblqbillsettlementdtl b on a.strBillNo=b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate) "
		    + " left outer join tblcustomermaster d on b.strCustomerCode=d.strCustomerCode "
		    + " inner join tblsettelmenthd c on b.strSettlementCode=c.strSettelmentCode "
		    + " where c.strSettelmentType='Credit' "
		    + " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    if (!posCode.equals("All"))
	    {
		sqlQData.append(" and a.strPOSCode='" + posCode + "' ");
	    }
	    sqlQData.append(" group by d.strCustomerCode order by d.strCustomerName ");
	    funSettlementQLiveData(sqlQData);
	    
	    sqlLiveData.setLength(0);
	    sqlLiveData.append(" select ifnull(d.strCustomerName,'NA'),c.strSettelmentType,"+settlementAmt+" "
		    + ",a.strKOTToBillNote "
		    + " from tblbillhd a "
		    + " inner join tblbillsettlementdtl b on a.strBillNo=b.strBillNo and date(a.dteBillDate)=date(b.dteBillDate) "
		    + " left outer join tblcustomermaster d on b.strCustomerCode=d.strCustomerCode "
		    + " inner join tblsettelmenthd c on b.strSettlementCode=c.strSettelmentCode "
		    + " where c.strSettelmentType='Credit' "
		    + " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    if (!posCode.equals("All"))
	    {
		sqlLiveData.append(" and a.strPOSCode='" + posCode + "' ");
	    }
	    sqlLiveData.append(" group by d.strCustomerCode order by d.strCustomerName ");
	    funSettlementQLiveData(sqlLiveData);
	    
	    //For Discount details of Live and Q data
	    String discAmt="sum(b.dblDiscAmt)";
	    if(currency.equalsIgnoreCase("USD"))
	    {
		discAmt="(sum(b.dblDiscAmt))/a.dblUSDConverionRate";
	    }	
	    sqlQData.setLength(0);
	    sqlQData.append(" select "+discAmt+",a.strBillNo  from tblqbillhd a,tblqbilldiscdtl b "
		    + " where a.strBillNo=b.strBillNo "
		    + " and date(a.dteBillDate)=date(b.dteBillDate) "
		    + " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    if (!posCode.equals("All"))
	    {

		sqlQData.append(" and a.strPOSCode='" + posCode + "' ");
	    }
	    sqlQData.append(" group by a.strBillNo");

	    double finalDisAmt = 0;
	    ResultSet rsDisQData = clsGlobalVarClass.dbMysql.executeResultSet(sqlQData.toString());
	    while (rsDisQData.next())
	    {
		finalDisAmt = finalDisAmt + rsDisQData.getDouble(1);
	    }
	    rsDisQData.close();
	    
	    
	    sqlLiveData.setLength(0);
	    sqlLiveData.append(" select "+discAmt+",a.strBillNo  from tblbillhd a,tblbilldiscdtl b "
		    + " where a.strBillNo=b.strBillNo "
		    + " and date(a.dteBillDate)=date(b.dteBillDate) "
		    + " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    if (!posCode.equals("All"))
	    {

		sqlLiveData.append(" and a.strPOSCode='" + posCode + "' ");
	    }
	    sqlLiveData.append(" group by a.strBillNo");

	    ResultSet rsDisLiveData = clsGlobalVarClass.dbMysql.executeResultSet(sqlLiveData.toString());
	    while (rsDisLiveData.next())
	    {
		finalDisAmt = finalDisAmt + rsDisLiveData.getDouble(1);
	    }
	    rsDisLiveData.close();

	    //For groupwise sala data
	    sqlQData.setLength(0);
	    sqlModQData.setLength(0);
	    String groupWiseAmt="SUM(b.dblAmount)-sum(b.dblDiscountAmt)";
	    if(currency.equalsIgnoreCase("USD"))
	    {
		groupWiseAmt="(SUM(b.dblAmount)-sum(b.dblDiscountAmt))/a.dblUSDConverionRate";
	    }	
	    sqlQData.append(" select e.strGroupName,"+groupWiseAmt+",e.strGroupCode,a.strOperationType "
		    + " from tblqbillhd a,tblqbilldtl b,tblitemmaster c,tblsubgrouphd d,tblgrouphd e "
		    + " where a.strBillNo=b.strBillNo "
		    + " and date(a.dteBillDate)=date(b.dteBillDate) "
		    + " and b.strItemCode=c.strItemCode "
		    + " and c.strSubGroupCode=d.strSubGroupCode "
		    + " and d.strGroupCode=e.strGroupCode "
		    + " and a.strClientCode=b.strClientCode  "
		    + " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    String groupWiseModAmt="SUM(b.dblAmount)-sum(b.dblDiscAmt)";
	    if(currency.equalsIgnoreCase("USD"))
	    {
		groupWiseModAmt="(SUM(b.dblAmount)-sum(b.dblDiscAmt))/a.dblUSDConverionRate";
	    }	
	    sqlModQData.append(" select e.strGroupName,"+groupWiseModAmt+",e.strGroupCode,a.strOperationType "
		    + " from tblqbillhd a,tblqbillmodifierdtl b,tblitemmaster c,tblsubgrouphd d,tblgrouphd e "
		    + " where a.strBillNo=b.strBillNo "
		    + " and date(a.dteBillDate)=date(b.dteBillDate) "
		    + " and left(b.strItemCode,7)=c.strItemCode "
		    + " and c.strSubGroupCode=d.strSubGroupCode "
		    + " and d.strGroupCode=e.strGroupCode "
		    + "and a.strClientCode=b.strClientCode "
		    + " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");

	    if (!posCode.equals("All"))
	    {
		sqlQData.append(" and a.strPOSCode='" + posCode + "' ");
		sqlModQData.append(" and a.strPOSCode='" + posCode + "' ");
	    }
	    sqlQData.append(" group by a.strOperationType,e.strGroupCode");
	    sqlModQData.append(" group by a.strOperationType,e.strGroupCode");

	    funGroupWiseQLiveData(sqlQData);
	    funGroupWiseQLiveModData(sqlModQData);
	    
	    sqlLiveData.setLength(0);
	    sqlModLiveData.setLength(0);

	    sqlLiveData.append(" select e.strGroupName,"+groupWiseAmt+",e.strGroupCode,a.strOperationType "
		    + " from tblbillhd a,tblbilldtl b,tblitemmaster c,tblsubgrouphd d,tblgrouphd e "
		    + " where a.strBillNo=b.strBillNo "
		    + " and date(a.dteBillDate)=date(b.dteBillDate) "
		    + " and b.strItemCode=c.strItemCode "
		    + " and c.strSubGroupCode=d.strSubGroupCode "
		    + " and d.strGroupCode=e.strGroupCode "
		    + " and a.strClientCode=b.strClientCode  "
		    + " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");

	    sqlModLiveData.append(" select e.strGroupName,"+groupWiseModAmt+",e.strGroupCode,a.strOperationType "
		    + " from tblbillhd a,tblbillmodifierdtl b,tblitemmaster c,tblsubgrouphd d,tblgrouphd e "
		    + " where a.strBillNo=b.strBillNo "
		    + " and date(a.dteBillDate)=date(b.dteBillDate) "
		    + " and left(b.strItemCode,7)=c.strItemCode "
		    + " and c.strSubGroupCode=d.strSubGroupCode "
		    + " and d.strGroupCode=e.strGroupCode "
		    + "and a.strClientCode=b.strClientCode "
		    + " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");

	    if (!posCode.equals("All"))
	    {
		sqlLiveData.append(" and a.strPOSCode='" + posCode + "' ");
		sqlModLiveData.append(" and a.strPOSCode='" + posCode + "' ");
	    }
	    sqlLiveData.append(" group by a.strOperationType,e.strGroupCode");
	    sqlModLiveData.append(" group by a.strOperationType,e.strGroupCode");

	    funGroupWiseQLiveData(sqlLiveData);
	    funGroupWiseQLiveModData(sqlModLiveData);
	    
	    //For taxwise data details;
	    sqlQData.setLength(0);
	    String taxAmt="sum(b.dblTaxAmount)";
	    String taxableAmt="sum(b.dblTaxableAmount)";
	    if(currency.equalsIgnoreCase("USD"))
	    {
		taxAmt="(sum(b.dblTaxAmount))/a.dblUSDConverionRate";
		taxableAmt="(sum(b.dblTaxAmount))/a.dblUSDConverionRate";
	    }	
	    sqlQData.append(" select c.strTaxCode,c.strTaxDesc,"+taxAmt+","+taxableAmt+" "
		    + " from tblqbillhd a,tblqbilltaxdtl b,tbltaxhd c "
		    + " where a.strBillNo=b.strBillNo "
		    + " and date(a.dteBillDate)=date(b.dteBillDate) "
		    + "and b.strTaxCode=c.strTaxCode "
		    + " and a.strClientCode=b.strClientCode "
		    + " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    if (!posCode.equals("All"))
	    {
		sqlQData.append(" and a.strPOSCode='" + posCode + "' ");
	    }
	    sqlQData.append(" group by b.strTaxCode order by c.strTaxOnTax");

	    funTaxWiseQLiveData(sqlQData);
	    
	    sqlLiveData.setLength(0);

	    sqlLiveData.append(" select c.strTaxCode,c.strTaxDesc,"+taxAmt+","+taxableAmt+" "
		    + " from tblbillhd a,tblbilltaxdtl b,tbltaxhd c "
		    + " where a.strBillNo=b.strBillNo "
		    + " and date(a.dteBillDate)=date(b.dteBillDate) "
		    + "and b.strTaxCode=c.strTaxCode "
		    + " and a.strClientCode=b.strClientCode "
		    + " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    if (!posCode.equals("All"))
	    {
		sqlLiveData.append(" and a.strPOSCode='" + posCode + "' ");
	    }
	    sqlLiveData.append(" group by b.strTaxCode order by c.strTaxOnTax");

	    funTaxWiseQLiveData(sqlLiveData);

	    sqlQData.setLength(0);
	    String tipAmt="sum(a.dblTipAmount)";
	    if(currency.equalsIgnoreCase("USD"))
	    {
		tipAmt="(sum(a.dblTipAmount))/a.dblUSDConverionRate";
	    }
	    sqlQData.append(" select "+tipAmt+" from tblqbillhd a "
		    + " where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    if (!posCode.equals("All"))
	    {
		sqlQData.append(" and a.strPOSCode='" + posCode + "' ");
	    }
	    sqlQData.append(" group by a.strBillNo");

	    double finalTipAmt = 0;

	    ResultSet rsTipQData = clsGlobalVarClass.dbMysql.executeResultSet(sqlQData.toString());
	    while (rsTipQData.next())
	    {
		finalTipAmt = finalTipAmt + rsTipQData.getDouble(1);
	    }
	    rsTipQData.close();
	    
	    sqlLiveData.setLength(0);

	    sqlLiveData.append(" select "+tipAmt+" from tblbillhd a "
		    + " where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    if (!posCode.equals("All"))
	    {
		sqlQData.append(" and a.strPOSCode='" + posCode + "' ");
	    }
	    sqlLiveData.append(" group by a.strBillNo");
	    ResultSet rsTipLiveData = clsGlobalVarClass.dbMysql.executeResultSet(sqlLiveData.toString());
	    while (rsTipLiveData.next())
	    {
		finalTipAmt = finalTipAmt + rsTipLiveData.getDouble(1);
	    }
	    rsTipLiveData.close();

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

	    //   totalDebitAmt += finalDisAmt;
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
	    double finalDebitAmount = 0, finalCreditAmount = 0;

	    //For Settlement Detail
	    double finalSettleAmt = 0;

	    List<clsSettelementOptions> listOfSettlement = new ArrayList<>();

	    if (hmSalesSettleData.containsKey("Credit"))
	    {
		clsSettelementOptions objSettle = new clsSettelementOptions();
		objSettle.setDblSettlementAmt(-1.0);
		objSettle.setStrSettelmentDesc("Credit");
		objSettle.setStrSettelmentType("Credit");

		listOfSettlement.add(objSettle);
	    }

	    for (Map.Entry<String, List<clsSettelementOptions>> entry : hmSalesSettleData.entrySet())
	    {
		count++;
		double totalSaleAmt = 0;
		List<clsSettelementOptions> listOfSettleDataDtl = entry.getValue();

		for (int j = 0; j < listOfSettleDataDtl.size(); j++)
		{
		    clsSettelementOptions objSettle = listOfSettleDataDtl.get(j);
//                    if (objSettle.getStrSettelmentType().equals("Credit"))
//                    {
//                        objSettle = new clsSettelementOptions();
//                        objSettle.setStrSettelmentDesc("Credit");
//                        listOfSettlement.add(objSettle);
//                    }
//
//                    objSettle = listOfSettleDataDtl.get(j);
		    listOfSettlement.add(objSettle);
		    totalSaleAmt += objSettle.getDblSettlementAmt();
		}
		finalSettleAmt += totalSaleAmt;
	    }

	    finalDebitAmount = finalDebitAmount + finalSettleAmt;
	    //For Discount
	    if (roundOff < 0)
	    {
		finalDebitAmount += roundOff;
	    }

	    //For Groupwise sale data
	    double finalGroupSaleAmt = 0;

	    List<clsSettelementOptions> listOfGroupWiseSales = new ArrayList<>();

	    for (Map.Entry<String, List<clsSettelementOptions>> entry : hmSalesGroupWiseSaleData.entrySet())
	    {
		double totalSaleAmt = 0;
		List<clsSettelementOptions> listOfGroupwiseSaleDataDtl = entry.getValue();
		for (int j = 0; j < listOfGroupwiseSaleDataDtl.size(); j++)
		{
		    clsSettelementOptions objSettle = listOfGroupwiseSaleDataDtl.get(j);
		    listOfGroupWiseSales.add(objSettle);
		}
		finalGroupSaleAmt += totalSaleAmt;
	    }

	    finalCreditAmount = finalCreditAmount + finalGroupSaleAmt;
	    //For Taxwise detial data

	    double finalTaxAmt = 0;
	    List<clsSettelementOptions> listOfTaxWiseSales = new ArrayList<>();
	    for (Map.Entry<String, List<clsSettelementOptions>> entry : hmSalesTaxWiseSaleData.entrySet())
	    {
		double totalSaleAmt = 0;
		List<clsSettelementOptions> listOfTaxwiseSaleDataDtl = entry.getValue();
		for (int j = 0; j < listOfTaxwiseSaleDataDtl.size(); j++)
		{
		    clsSettelementOptions objSettle = listOfTaxwiseSaleDataDtl.get(j);
		    totalSaleAmt += objSettle.getDblSettlementAmt();
		    listOfTaxWiseSales.add(objSettle);

		}
		finalTaxAmt += totalSaleAmt;
	    }

	    finalCreditAmount = finalCreditAmount + finalTaxAmt;

	    finalCreditAmount = finalCreditAmount + finalTipAmt;

	    double finalRoundOff = 0;

	    finalCreditAmount = finalCreditAmount + finalRoundOff;

	    if (roundOff > 0)
	    {

		finalCreditAmount += roundOff;
	    }

	    List<clsSettelementOptions> listOfGroupWiseSalesForDineIn = new ArrayList<>();
	    List<clsSettelementOptions> listOfGroupWiseSalesForTakeAway = new ArrayList<>();
	    List<clsSettelementOptions> listOfGroupWiseSalesForHomeDel = new ArrayList<>();

	    Iterator<Map.Entry<String, Double>> itDineIn = mapDineIn.entrySet().iterator();
	    while (itDineIn.hasNext())
	    {
		Map.Entry<String, Double> entry = itDineIn.next();
		String group = entry.getKey();
		double amount = entry.getValue();
		clsSettelementOptions objSett = new clsSettelementOptions();
		objSett.setStrSettelmentDesc(group);
		objSett.setDblSettlementAmt(amount);

		listOfGroupWiseSalesForDineIn.add(objSett);
	    }

	    Iterator<Map.Entry<String, Double>> itTakeAway = mapTakeAway.entrySet().iterator();
	    while (itTakeAway.hasNext())
	    {
		Map.Entry<String, Double> entry = itTakeAway.next();
		String group = entry.getKey();
		double amount = entry.getValue();
		clsSettelementOptions objSett = new clsSettelementOptions();
		objSett.setStrSettelmentDesc(group);
		objSett.setDblSettlementAmt(amount);

		listOfGroupWiseSalesForTakeAway.add(objSett);
	    }

	    Iterator<Map.Entry<String, Double>> itHomeDel = mapHomeDel.entrySet().iterator();
	    while (itHomeDel.hasNext())
	    {
		Map.Entry<String, Double> entry = itHomeDel.next();
		String group = entry.getKey();
		double amount = entry.getValue();
		clsSettelementOptions objSett = new clsSettelementOptions();
		objSett.setStrSettelmentDesc(group);
		objSett.setDblSettlementAmt(amount);

		listOfGroupWiseSalesForHomeDel.add(objSett);
	    }

	    hm.put("listOfSettlement", listOfSettlement);
	    hm.put("listOfGroupWiseSales", listOfGroupWiseSales);
	    hm.put("listOfTaxWiseSales", listOfTaxWiseSales);
	    hm.put("finalDisAmt", finalDisAmt);
	    hm.put("finalTipAmt", finalTipAmt);
	    hm.put("listOfGroupWiseSalesForDineIn", listOfGroupWiseSalesForDineIn);
	    hm.put("listOfGroupWiseSalesForTakeAway", listOfGroupWiseSalesForTakeAway);
	    hm.put("listOfGroupWiseSalesForHomeDel", listOfGroupWiseSalesForHomeDel);

	    double debitRoundOff = 0.00, creaditRoundOff = 0.00;
	    if (roundOff < 0)
	    {
		debitRoundOff = (-1) * roundOff;
		totalDebitAmt = totalDebitAmt + debitRoundOff;
	    }
	    else
	    {
		creaditRoundOff = roundOff;
		totalCreditAmt = totalCreditAmt + creaditRoundOff;
	    }
	    hm.put("finalRoundOff", creaditRoundOff);
	    hm.put("totalDebitAmt", totalDebitAmt);
	    hm.put("totalCreditAmt", totalCreditAmt);
	    hm.put("debitRoundOff", debitRoundOff);

	    //call for view report
	    if (reportType.equalsIgnoreCase("A4 Size Report"))
	    {
		funViewJasperReportForBeanCollectionDataSource(is, hm, listOfSettlement);
	    }
	    if (reportType.equalsIgnoreCase("Excel Report"))
	    {
		try
		{
		    totalDebitAmt = 0;
		    totalCreditAmt = 0;
		    //DecimalFormat gDecimalFormat = new DecimalFormat("0.00");
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

		    //For Settlement Detail
		    listOfSettlement = new ArrayList<>();
		    for (Map.Entry<String, List<clsSettelementOptions>> entry : hmSalesSettleData.entrySet())
		    {
			count++;
			double totalSaleAmt = 0;
			List<clsSettelementOptions> listOfSettleDataDtl = entry.getValue();
			for (int j = 0; j < listOfSettleDataDtl.size(); j++)
			{
			    clsSettelementOptions objSettle = listOfSettleDataDtl.get(j);
			    listOfSettlement.add(objSettle);
			    totalSaleAmt += objSettle.getDblSettlementAmt();
			}
			finalSettleAmt += totalSaleAmt;
		    }
		    finalDebitAmount = finalDebitAmount + finalSettleAmt;

		    //For Discount
		    if (roundOff < 0)
		    {
			finalDebitAmount += roundOff;
		    }

		    //For Groupwise sale data
		    listOfGroupWiseSales = new ArrayList<>();
		    for (Map.Entry<String, List<clsSettelementOptions>> entry : hmSalesGroupWiseSaleData.entrySet())
		    {
			double totalSaleAmt = 0;
			List<clsSettelementOptions> listOfGroupwiseSaleDataDtl = entry.getValue();
			for (int j = 0; j < listOfGroupwiseSaleDataDtl.size(); j++)
			{
			    clsSettelementOptions objSettle = listOfGroupwiseSaleDataDtl.get(j);
			    listOfGroupWiseSales.add(objSettle);
			}
			finalGroupSaleAmt += totalSaleAmt;
		    }

		    finalCreditAmount = finalCreditAmount + finalGroupSaleAmt;
		    //For Taxwise detial data

		    listOfTaxWiseSales = new ArrayList<>();
		    for (Map.Entry<String, List<clsSettelementOptions>> entry : hmSalesTaxWiseSaleData.entrySet())
		    {
			double totalSaleAmt = 0;
			List<clsSettelementOptions> listOfTaxwiseSaleDataDtl = entry.getValue();
			for (int j = 0; j < listOfTaxwiseSaleDataDtl.size(); j++)
			{
			    clsSettelementOptions objSettle = listOfTaxwiseSaleDataDtl.get(j);
			    totalSaleAmt += objSettle.getDblSettlementAmt();
			    listOfTaxWiseSales.add(objSettle);
			}
			finalTaxAmt += totalSaleAmt;
		    }

		    finalCreditAmount = finalCreditAmount + finalTaxAmt;
		    finalCreditAmount = finalCreditAmount + finalTipAmt;

		    finalCreditAmount = finalCreditAmount + finalRoundOff;
		    if (roundOff > 0)
		    {
			finalCreditAmount += roundOff;
		    }
		    listOfGroupWiseSalesForDineIn = new ArrayList<>();
		    listOfGroupWiseSalesForTakeAway = new ArrayList<>();
		    listOfGroupWiseSalesForHomeDel = new ArrayList<>();

		    itDineIn = mapDineIn.entrySet().iterator();
		    while (itDineIn.hasNext())
		    {
			Map.Entry<String, Double> entry = itDineIn.next();
			String group = entry.getKey();
			double amount = entry.getValue();
			clsSettelementOptions objSett = new clsSettelementOptions();
			objSett.setStrSettelmentDesc(group);
			objSett.setDblSettlementAmt(amount);
			listOfGroupWiseSalesForDineIn.add(objSett);
		    }

		    itTakeAway = mapTakeAway.entrySet().iterator();
		    while (itTakeAway.hasNext())
		    {
			Map.Entry<String, Double> entry = itTakeAway.next();
			String group = entry.getKey();
			double amount = entry.getValue();
			clsSettelementOptions objSett = new clsSettelementOptions();
			objSett.setStrSettelmentDesc(group);
			objSett.setDblSettlementAmt(amount);
			listOfGroupWiseSalesForTakeAway.add(objSett);
		    }

		    itHomeDel = mapHomeDel.entrySet().iterator();
		    while (itHomeDel.hasNext())
		    {
			Map.Entry<String, Double> entry = itHomeDel.next();
			String group = entry.getKey();
			double amount = entry.getValue();
			clsSettelementOptions objSett = new clsSettelementOptions();
			objSett.setStrSettelmentDesc(group);
			objSett.setDblSettlementAmt(amount);
			listOfGroupWiseSalesForHomeDel.add(objSett);
		    }

		    List<String> arrparameterList = new ArrayList<String>();
		    arrparameterList.add("Posting Report");
		    arrparameterList.add("Client Name" + " : " + clsGlobalVarClass.gClientName);
		    arrparameterList.add("POS" + " : " + posName);
		    arrparameterList.add("FromDate" + " : " + fromDateToDisplay + "  " + "ToDate" + " : " + toDateToDisplay);
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

		    File file = new File(clsPosConfigFile.exportReportPath + File.separator + "postingExcelSheet.xls");
//                if(!file.mkdir()){
//                    file.mkdirs();
//                }
		    WritableWorkbook workbook1 = Workbook.createWorkbook(file);
		    WritableSheet sheet1 = workbook1.createSheet("First Sheet", 0);
		    WritableFont cellFont = new WritableFont(WritableFont.COURIER, 14);
		    cellFont.setBoldStyle(WritableFont.BOLD);
		    WritableCellFormat cellFormat = new WritableCellFormat(cellFont);
		    cellFormat.setAlignment(Alignment.CENTRE);
		    WritableFont cellFont1 = new WritableFont(WritableFont.COURIER, 12);
		    cellFont1.setBoldStyle(WritableFont.BOLD);
		    WritableCellFormat cellFormat1 = new WritableCellFormat(cellFont1);
		    WritableFont headerCellFont = new WritableFont(WritableFont.TIMES, 10);
		    headerCellFont.setBoldStyle(WritableFont.BOLD);
		    WritableCellFormat headerCell = new WritableCellFormat(headerCellFont);
		    headerCell.setAlignment(Alignment.CENTRE);
		    WritableCellFormat leftHeaderCell = new WritableCellFormat(headerCellFont);
		    leftHeaderCell.setAlignment(Alignment.LEFT);
		    WritableCellFormat rightHeaderCell = new WritableCellFormat(headerCellFont);
		    rightHeaderCell.setAlignment(Alignment.RIGHT);

		    WritableFont NormalTextCellFont = new WritableFont(WritableFont.TIMES, 10);
		    headerCellFont.setBoldStyle(WritableFont.NO_BOLD);
		    WritableCellFormat normalTextCell = new WritableCellFormat(NormalTextCellFont);
		    headerCell.setAlignment(Alignment.CENTRE);
		    WritableCellFormat leftnormalTextCell = new WritableCellFormat(NormalTextCellFont);
		    leftnormalTextCell.setAlignment(Alignment.LEFT);
		    WritableCellFormat rightnormalTextCell = new WritableCellFormat(NormalTextCellFont);
		    rightnormalTextCell.setAlignment(Alignment.RIGHT);

		    WritableFont cellHeaderFont = new WritableFont(WritableFont.TIMES, 12);
		    cellHeaderFont.setBoldStyle(WritableFont.BOLD);
		    WritableCellFormat leftCellFormat = new WritableCellFormat(cellHeaderFont);
		    leftCellFormat.setAlignment(Alignment.LEFT);
		    WritableCellFormat rightCellFormat = new WritableCellFormat(cellHeaderFont);
		    rightCellFormat.setAlignment(Alignment.RIGHT);

		    int lineNo = 0;
		    sheet1.setColumnView(0, 30);
		    sheet1.setColumnView(1, 30);
		    sheet1.setColumnView(2, 30);
		    sheet1.setColumnView(3, 30);
		    sheet1.setColumnView(4, 30);

		    for (int j = 0; j <= arrparameterList.size(); j++)
		    {
			Label l0 = new Label(2, lineNo, arrparameterList.get(0), cellFormat);
			Label l1 = new Label(2, lineNo + 2, arrparameterList.get(1), leftHeaderCell);
			Label l2 = new Label(2, lineNo + 3, arrparameterList.get(2), leftHeaderCell);
			Label l3 = new Label(2, lineNo + 4, arrparameterList.get(3), leftHeaderCell);

			sheet1.addCell(l0);
			sheet1.addCell(l1);
			sheet1.addCell(l2);
			sheet1.addCell(l3);
			if (clsGlobalVarClass.gEnableShiftYN)
			{
			    Label l4 = new Label(2, lineNo + 5, arrparameterList.get(4), leftHeaderCell);
			    sheet1.addCell(l4);
			}
		    }
		    lineNo += 7;
		    String line = "___________________";

		    Label labelline1 = new Label(0, lineNo, line, cellFormat);
		    Label labelline2 = new Label(1, lineNo, line, cellFormat);
		    Label labelline3 = new Label(2, lineNo, line, cellFormat);
		    Label labelline4 = new Label(3, lineNo, line, cellFormat);
		    Label labelline5 = new Label(4, lineNo, line, cellFormat);
		    sheet1.addCell(labelline1);
		    sheet1.addCell(labelline2);
		    sheet1.addCell(labelline3);
		    sheet1.addCell(labelline4);
		    sheet1.addCell(labelline5);

		    lineNo += 2;
		    int lineNoForCreditDetail = lineNo;
		    Label labelParticular = new Label(0, lineNo, "Particulars", leftCellFormat);
		    Label labelDebit = new Label(1, lineNo, "Debit", rightCellFormat);
		    sheet1.addCell(labelParticular);
		    sheet1.addCell(labelDebit);
		    // sheet1.setColumnView(5, 15);

		    lineNo += 2;
		    double FinaltotalSettleAmt = 0;
		    for (int j = 0; j < listOfSettlement.size(); j++)
		    {
			clsSettelementOptions objSettle = listOfSettlement.get(j);
			if (objSettle.getStrSettelmentType().equals("Credit"))
			{
			    Label labelSettleType = new Label(0, lineNo, objSettle.getStrSettelmentType(), leftnormalTextCell);
			    sheet1.addCell(labelSettleType);
			    lineNo++;
			    Label labelSettleDesc = new Label(0, lineNo, objSettle.getStrSettelmentDesc(), leftnormalTextCell);
			    Label labelSettleAmt = new Label(1, lineNo, String.valueOf(gDecimalFormat.format(objSettle.getDblSettlementAmt())), rightnormalTextCell);
			    lineNo++;
			    FinaltotalSettleAmt += objSettle.getDblSettlementAmt();
			    sheet1.addCell(labelSettleDesc);
			    sheet1.addCell(labelSettleAmt);
			}
			else
			{
			    Label labelSettleDesc = new Label(0, lineNo, objSettle.getStrSettelmentDesc(), leftnormalTextCell);
			    Label labelSettleAmt = new Label(1, lineNo, String.valueOf(gDecimalFormat.format(objSettle.getDblSettlementAmt())), rightnormalTextCell);
			    lineNo++;
			    FinaltotalSettleAmt += objSettle.getDblSettlementAmt();
			    sheet1.addCell(labelSettleDesc);
			    sheet1.addCell(labelSettleAmt);
			}

		    }
		    lineNo += 1;

		    String formatted1 = gDecimalFormat.format(FinaltotalSettleAmt);
		    Label labelTotal = new Label(0, lineNo, "Total", leftCellFormat);
		    Label labelSettleAmt = new Label(1, lineNo, formatted1, rightCellFormat);
		    sheet1.addCell(labelTotal);
		    sheet1.addCell(labelSettleAmt);

		    if (listOfGroupWiseSalesForDineIn.size() > 0)
		    {
			Label labelDineIn = new Label(3, lineNoForCreditDetail, "DineIn", leftCellFormat);
			Label labelCredit = new Label(4, lineNoForCreditDetail, "Credit", rightCellFormat);
			sheet1.addCell(labelDineIn);
			sheet1.addCell(labelCredit);

			lineNoForCreditDetail += 2;
			double FinaltotalDineCreditAmt = 0;
			for (int j = 0; j < listOfGroupWiseSalesForDineIn.size(); j++)
			{
			    clsSettelementOptions objSettle = listOfGroupWiseSalesForDineIn.get(j);
			    Label labelGroupDesc = new Label(3, lineNoForCreditDetail, objSettle.getStrSettelmentDesc(), leftnormalTextCell);
			    Label labelGroupDineInAmt = new Label(4, lineNoForCreditDetail, String.valueOf(gDecimalFormat.format(objSettle.getDblSettlementAmt())), rightnormalTextCell);
			    lineNoForCreditDetail++;
			    FinaltotalDineCreditAmt += objSettle.getDblSettlementAmt();
			    sheet1.addCell(labelGroupDesc);
			    sheet1.addCell(labelGroupDineInAmt);
			}
			lineNoForCreditDetail += 1;

			String formatted = gDecimalFormat.format(FinaltotalDineCreditAmt);
			Label labelGroupDineInTotal = new Label(3, lineNoForCreditDetail, "DineIn Total", leftCellFormat);
			Label labelGroupDineInAmt = new Label(4, lineNoForCreditDetail, formatted, rightCellFormat);
			sheet1.addCell(labelGroupDineInTotal);
			sheet1.addCell(labelGroupDineInAmt);
		    }

		    if (listOfGroupWiseSalesForTakeAway.size() > 0)
		    {
			lineNoForCreditDetail += 2;
			Label labelTakeAway = new Label(3, lineNoForCreditDetail, "TakeAway", leftCellFormat);
			Label labelCredit = new Label(4, lineNoForCreditDetail, "Credit", rightCellFormat);
			sheet1.addCell(labelTakeAway);
			sheet1.addCell(labelCredit);

			lineNoForCreditDetail += 2;
			double FinaltotalTakeAwayCreditAmt = 0;
			for (int j = 0; j < listOfGroupWiseSalesForTakeAway.size(); j++)
			{
			    clsSettelementOptions objSettle = listOfGroupWiseSalesForTakeAway.get(j);
			    Label labelGroupDesc = new Label(3, lineNoForCreditDetail, objSettle.getStrSettelmentDesc(), leftnormalTextCell);
			    Label labelGroupDineInAmt = new Label(4, lineNoForCreditDetail, String.valueOf(gDecimalFormat.format(objSettle.getDblSettlementAmt())), rightnormalTextCell);
			    lineNoForCreditDetail++;
			    FinaltotalTakeAwayCreditAmt += objSettle.getDblSettlementAmt();
			    sheet1.addCell(labelGroupDesc);
			    sheet1.addCell(labelGroupDineInAmt);
			}
			lineNoForCreditDetail += 1;

			String formatted = gDecimalFormat.format(FinaltotalTakeAwayCreditAmt);
			Label labelGroupTakeAwayTotal = new Label(3, lineNoForCreditDetail, "TakeAway Total", leftCellFormat);
			Label labelGroupTakeAwayAmt = new Label(4, lineNoForCreditDetail, formatted, rightCellFormat);
			sheet1.addCell(labelGroupTakeAwayTotal);
			sheet1.addCell(labelGroupTakeAwayAmt);
		    }

		    if (listOfGroupWiseSalesForHomeDel.size() > 0)
		    {
			lineNoForCreditDetail += 2;
			Label labelhd = new Label(3, lineNoForCreditDetail, "HomeDelivery", leftCellFormat);
			Label labelCredit = new Label(4, lineNoForCreditDetail, "Credit", rightCellFormat);
			sheet1.addCell(labelhd);
			sheet1.addCell(labelCredit);

			lineNoForCreditDetail += 2;
			double FinaltotalHomeDeliveryCreditAmt = 0;
			for (int j = 0; j < listOfGroupWiseSalesForHomeDel.size(); j++)
			{
			    clsSettelementOptions objSettle = listOfGroupWiseSalesForHomeDel.get(j);
			    Label labelGroupDesc = new Label(3, lineNoForCreditDetail, objSettle.getStrSettelmentDesc(), leftnormalTextCell);
			    Label labelGroupDineInAmt = new Label(4, lineNoForCreditDetail, String.valueOf(gDecimalFormat.format(objSettle.getDblSettlementAmt())), rightnormalTextCell);
			    lineNoForCreditDetail++;
			    FinaltotalHomeDeliveryCreditAmt += objSettle.getDblSettlementAmt();
			    sheet1.addCell(labelGroupDesc);
			    sheet1.addCell(labelGroupDineInAmt);
			}
			lineNoForCreditDetail += 1;

			String formatted = gDecimalFormat.format(FinaltotalHomeDeliveryCreditAmt);
			Label labelGroupTakeAwayTotal = new Label(3, lineNoForCreditDetail, "HomeDelivery Total", leftCellFormat);
			Label labelGroupTakeAwayAmt = new Label(4, lineNoForCreditDetail, formatted, rightCellFormat);
			sheet1.addCell(labelGroupTakeAwayTotal);
			sheet1.addCell(labelGroupTakeAwayAmt);
		    }

		    if (listOfTaxWiseSales.size() > 0)
		    {
			lineNoForCreditDetail += 2;
			Label labelTAX = new Label(3, lineNoForCreditDetail, "Other", leftCellFormat);
			sheet1.addCell(labelTAX);

			lineNoForCreditDetail += 2;
			double FinaltotalOtherCreditAmt = 0;
			for (int j = 0; j < listOfTaxWiseSales.size(); j++)
			{
			    clsSettelementOptions objSettle = listOfTaxWiseSales.get(j);
			    Label labelGroupDesc = new Label(3, lineNoForCreditDetail, objSettle.getStrSettelmentDesc(), leftnormalTextCell);
			    Label labelGroupDineInAmt = new Label(4, lineNoForCreditDetail, String.valueOf(gDecimalFormat.format(objSettle.getDblSettlementAmt())), rightnormalTextCell);
			    lineNoForCreditDetail++;
			    FinaltotalOtherCreditAmt += objSettle.getDblSettlementAmt();
			    sheet1.addCell(labelGroupDesc);
			    sheet1.addCell(labelGroupDineInAmt);
			}
			lineNoForCreditDetail += 1;

			String formatted = gDecimalFormat.format(FinaltotalOtherCreditAmt);
			Label labelGroupTakeAwayTotal = new Label(3, lineNoForCreditDetail, "Total", leftCellFormat);
			Label labelGroupTakeAwayAmt = new Label(4, lineNoForCreditDetail, formatted, rightCellFormat);
			sheet1.addCell(labelGroupTakeAwayTotal);
			sheet1.addCell(labelGroupTakeAwayAmt);
		    }

		    if (finalTipAmt > 0)
		    {
			lineNoForCreditDetail += 2;
			Label labelTip = new Label(3, lineNoForCreditDetail, "Tip Amount", leftnormalTextCell);

			String formatted = gDecimalFormat.format(finalTipAmt);
			Label labelCredit = new Label(4, lineNoForCreditDetail, formatted, rightnormalTextCell);
			sheet1.addCell(labelTip);
			sheet1.addCell(labelCredit);
		    }

		    lineNo = lineNoForCreditDetail;

		    if (roundOff < 0)
		    {
			debitRoundOff = (-1) * roundOff;
			totalDebitAmt = totalDebitAmt + debitRoundOff;

			String formatted = gDecimalFormat.format(debitRoundOff);
			Label labelRoundOff = new Label(0, lineNo, "Round Off", leftnormalTextCell);
			Label labelAmt = new Label(1, lineNo, formatted, rightnormalTextCell);
			sheet1.addCell(labelRoundOff);
			sheet1.addCell(labelAmt);
		    }
		    else
		    {
			lineNo += 2;
			creaditRoundOff = roundOff;
			totalCreditAmt = totalCreditAmt + creaditRoundOff;

			String formatted = gDecimalFormat.format(creaditRoundOff);
			Label labelRoundOff = new Label(3, lineNo, "Round Off", leftnormalTextCell);
			Label labelAmt = new Label(4, lineNo, formatted, rightnormalTextCell);
			sheet1.addCell(labelRoundOff);
			sheet1.addCell(labelAmt);
		    }
		    lineNo += 1;
		    labelline1 = new Label(0, lineNo, line, cellFormat);
		    labelline2 = new Label(1, lineNo, line, cellFormat);
		    labelline3 = new Label(2, lineNo, line, cellFormat);
		    labelline4 = new Label(3, lineNo, line, cellFormat);
		    labelline5 = new Label(4, lineNo, line, cellFormat);
		    sheet1.addCell(labelline1);
		    sheet1.addCell(labelline2);
		    sheet1.addCell(labelline3);
		    sheet1.addCell(labelline4);
		    sheet1.addCell(labelline5);
		    lineNo += 2;
		    if (totalDebitAmt > 0)
		    {

			String formatted = gDecimalFormat.format(totalDebitAmt);
			Label labelGrandTotal = new Label(0, lineNo, "GRAND TOTAL :", leftCellFormat);
			Label labelCredit = new Label(1, lineNo, formatted, rightCellFormat);
			sheet1.addCell(labelGrandTotal);
			sheet1.addCell(labelCredit);
		    }

		    if (totalCreditAmt > 0)
		    {

			String formatted = gDecimalFormat.format(totalCreditAmt);
			Label labelCredit = new Label(4, lineNo, formatted, rightCellFormat);
			sheet1.addCell(labelCredit);
		    }

		    lineNo += 1;
		    labelline1 = new Label(0, lineNo, line, cellFormat);
		    labelline2 = new Label(1, lineNo, line, cellFormat);
		    labelline3 = new Label(2, lineNo, line, cellFormat);
		    labelline4 = new Label(3, lineNo, line, cellFormat);
		    labelline5 = new Label(4, lineNo, line, cellFormat);
		    sheet1.addCell(labelline1);
		    sheet1.addCell(labelline2);
		    sheet1.addCell(labelline3);
		    sheet1.addCell(labelline4);
		    sheet1.addCell(labelline5);

		    workbook1.write();
		    workbook1.close();
		    Desktop dt = Desktop.getDesktop();
		    dt.open(file);
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

    private void funViewJasperReportForBeanCollectionDataSource(InputStream is, HashMap hm, Collection listOfBillData)
    {
	try
	{
	    List list = new ArrayList();
	    list.add(1);
	    JRBeanCollectionDataSource beanCollectionDataSource = new JRBeanCollectionDataSource(list);
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
    
    public void funSettlementWiseQLiveData(StringBuilder sqlData) throws Exception
    {
	    ResultSet rsSettlementWiseData = clsGlobalVarClass.dbMysql.executeResultSet(sqlData.toString());
	    while (rsSettlementWiseData.next())
	    {
		clsSettelementOptions objSettle = new clsSettelementOptions();
		if (hmSalesSettleData.containsKey(rsSettlementWiseData.getString(2)))
		{
		    arrListSettleData = hmSalesSettleData.get(rsSettlementWiseData.getString(2));
		    for (int j = 0; j < arrListSettleData.size(); j++)
		    {
			objSettle = arrListSettleData.get(j);
			if (objSettle.getStrSettelmentDesc().equals(rsSettlementWiseData.getString(1)))
			{
			    arrListSettleData.remove(objSettle);
			    double settleAmt = objSettle.getDblSettlementAmt();
			    objSettle.setDblSettlementAmt(settleAmt + rsSettlementWiseData.getDouble(3));
			}
			else
			{
			    objSettle = new clsSettelementOptions();
			    objSettle.setDblSettlementAmt(rsSettlementWiseData.getDouble(3));
			    objSettle.setStrSettelmentDesc(rsSettlementWiseData.getString(1));
			    objSettle.setStrSettelmentType(rsSettlementWiseData.getString(2));
			}
		    }
		}
		else
		{
		    arrListSettleData = new ArrayList<clsSettelementOptions>();
		    objSettle.setStrSettelmentDesc(rsSettlementWiseData.getString(1));
		    objSettle.setStrSettelmentType(rsSettlementWiseData.getString(2));
		    objSettle.setDblSettlementAmt(rsSettlementWiseData.getDouble(3));
		}
		arrListSettleData.add(objSettle);
		hmSalesSettleData.put(rsSettlementWiseData.getString(2), arrListSettleData);
	    }
	    rsSettlementWiseData.close();
    }   
    
    public void funSettlementQLiveData(StringBuilder sqlData) throws Exception
    {
	String key = "Credit";
	ResultSet rsSettlementWiseData = clsGlobalVarClass.dbMysql.executeResultSet(sqlData.toString());
	    while (rsSettlementWiseData.next())
	    {
		String settDesc = rsSettlementWiseData.getString(1);
		String billNote = rsSettlementWiseData.getString(4);

		String settNameKey = settDesc;
		if (billNote.trim().length() > 0)
		{
		    settNameKey = settNameKey + "-" + billNote;
		}

		clsSettelementOptions objSettle = new clsSettelementOptions();
		if (hmSalesSettleData.containsKey(key))
		{
		    arrListCreditSettleData = hmSalesSettleData.get(key);
		    for (int j = 0; j < arrListCreditSettleData.size(); j++)
		    {
			objSettle = arrListCreditSettleData.get(j);
			if (objSettle.getStrSettelmentDesc().equals(settNameKey))
			{
			    arrListCreditSettleData.remove(objSettle);
			    double settleAmt = objSettle.getDblSettlementAmt();
			    objSettle.setDblSettlementAmt(settleAmt + rsSettlementWiseData.getDouble(3));
			}
			else
			{
			    objSettle = new clsSettelementOptions();
			    objSettle.setDblSettlementAmt(rsSettlementWiseData.getDouble(3));
			    objSettle.setStrSettelmentDesc(settNameKey);
			    objSettle.setStrSettelmentType(rsSettlementWiseData.getString(2));
			}
		    }
		}
		else
		{
		    arrListCreditSettleData = new ArrayList<clsSettelementOptions>();
		    objSettle.setStrSettelmentDesc(settNameKey);
		    objSettle.setStrSettelmentType(rsSettlementWiseData.getString(2));
		    objSettle.setDblSettlementAmt(rsSettlementWiseData.getDouble(3));
		}
		arrListCreditSettleData.add(objSettle);
		hmSalesSettleData.put(key, arrListCreditSettleData);
	    }
	    rsSettlementWiseData.close();
    }
    
    public void funGroupWiseQLiveData(StringBuilder sqlData) throws Exception
    {
	    ResultSet rsGroupwiseSaleData = clsGlobalVarClass.dbMysql.executeResultSet(sqlData.toString());

	    while (rsGroupwiseSaleData.next())
	    {

		clsSettelementOptions objDineIn = new clsSettelementOptions();
		objDineIn.setDblSettlementAmt(rsGroupwiseSaleData.getDouble(2));
		objDineIn.setStrSettelmentDesc(rsGroupwiseSaleData.getString(1));
		objDineIn.setStrSettelmentType(rsGroupwiseSaleData.getString(3));
		objDineIn.setStrRemark(rsGroupwiseSaleData.getString(4));

		if (rsGroupwiseSaleData.getString(4).equalsIgnoreCase("DineIn") || rsGroupwiseSaleData.getString(4).equalsIgnoreCase("DirectBiller"))
		{
		    if (mapDineIn.containsKey(rsGroupwiseSaleData.getString(1)))
		    {
			mapDineIn.put(rsGroupwiseSaleData.getString(1), mapDineIn.get(rsGroupwiseSaleData.getString(1)) + rsGroupwiseSaleData.getDouble(2));
		    }
		    else
		    {
			mapDineIn.put(rsGroupwiseSaleData.getString(1), rsGroupwiseSaleData.getDouble(2));
		    }
		}
		else if (rsGroupwiseSaleData.getString(4).equalsIgnoreCase("TakeAway"))
		{
		    if (mapTakeAway.containsKey(rsGroupwiseSaleData.getString(1)))
		    {
			mapTakeAway.put(rsGroupwiseSaleData.getString(1), mapTakeAway.get(rsGroupwiseSaleData.getString(1)) + rsGroupwiseSaleData.getDouble(2));
		    }
		    else
		    {
			mapTakeAway.put(rsGroupwiseSaleData.getString(1), rsGroupwiseSaleData.getDouble(2));
		    }
		}
		else if (rsGroupwiseSaleData.getString(4).equalsIgnoreCase("HomeDelivery"))
		{
		    if (mapHomeDel.containsKey(rsGroupwiseSaleData.getString(1)))
		    {
			mapHomeDel.put(rsGroupwiseSaleData.getString(1), mapHomeDel.get(rsGroupwiseSaleData.getString(1)) + rsGroupwiseSaleData.getDouble(2));
		    }
		    else
		    {
			mapHomeDel.put(rsGroupwiseSaleData.getString(1), rsGroupwiseSaleData.getDouble(2));
		    }
		}

		clsSettelementOptions objSettle = new clsSettelementOptions();
		if (hmSalesGroupWiseSaleData.containsKey(rsGroupwiseSaleData.getString(3)))
		{
		    arrListGroupwiseSaleData = hmSalesGroupWiseSaleData.get(rsGroupwiseSaleData.getString(3));
		    for (int j = 0; j < arrListGroupwiseSaleData.size(); j++)
		    {
			objSettle = arrListGroupwiseSaleData.get(j);
			if (objSettle.getStrSettelmentDesc().equals(rsGroupwiseSaleData.getString(1)))
			{
			    arrListGroupwiseSaleData.remove(objSettle);
			    double settleAmt = objSettle.getDblSettlementAmt();
			    objSettle.setDblSettlementAmt(settleAmt + rsGroupwiseSaleData.getDouble(2));
			}
			else
			{
			    objSettle = new clsSettelementOptions();
			    objSettle.setDblSettlementAmt(rsGroupwiseSaleData.getDouble(2));
			    objSettle.setStrSettelmentDesc(rsGroupwiseSaleData.getString(1));
			    objSettle.setStrSettelmentType(rsGroupwiseSaleData.getString(3));
			    objSettle.setStrRemark(rsGroupwiseSaleData.getString(4));
			}
		    }
		}
		else
		{
		    arrListGroupwiseSaleData = new ArrayList<clsSettelementOptions>();
		    objSettle.setStrSettelmentDesc(rsGroupwiseSaleData.getString(1));
		    objSettle.setStrSettelmentType(rsGroupwiseSaleData.getString(3));
		    objSettle.setDblSettlementAmt(rsGroupwiseSaleData.getDouble(2));
		    objSettle.setStrRemark(rsGroupwiseSaleData.getString(4));

		}
		arrListGroupwiseSaleData.add(objSettle);
		hmSalesGroupWiseSaleData.put(rsGroupwiseSaleData.getString(3), arrListGroupwiseSaleData);
	    }
	    rsGroupwiseSaleData.close();
    }
    
    public void funGroupWiseQLiveModData(StringBuilder sqlModData) throws Exception
    {
	ResultSet rsGroupwiseSaleModData = clsGlobalVarClass.dbMysql.executeResultSet(sqlModData.toString());
	    while (rsGroupwiseSaleModData.next())
	    {

		if (rsGroupwiseSaleModData.getString(4).equalsIgnoreCase("DineIn") || rsGroupwiseSaleModData.getString(4).equalsIgnoreCase("DirectBiller"))
		{
		    if (mapDineIn.containsKey(rsGroupwiseSaleModData.getString(1)))
		    {
			mapDineIn.put(rsGroupwiseSaleModData.getString(1), mapDineIn.get(rsGroupwiseSaleModData.getString(1)) + rsGroupwiseSaleModData.getDouble(2));
		    }
		    else
		    {
			mapDineIn.put(rsGroupwiseSaleModData.getString(1), rsGroupwiseSaleModData.getDouble(2));
		    }
		}
		else if (rsGroupwiseSaleModData.getString(4).equalsIgnoreCase("TakeAway"))
		{
		    if (mapTakeAway.containsKey(rsGroupwiseSaleModData.getString(1)))
		    {
			mapTakeAway.put(rsGroupwiseSaleModData.getString(1), mapTakeAway.get(rsGroupwiseSaleModData.getString(1)) + rsGroupwiseSaleModData.getDouble(2));
		    }
		    else
		    {
			mapTakeAway.put(rsGroupwiseSaleModData.getString(1), rsGroupwiseSaleModData.getDouble(2));
		    }
		}
		else if (rsGroupwiseSaleModData.getString(4).equalsIgnoreCase("HomeDelivery"))
		{
		    if (mapHomeDel.containsKey(rsGroupwiseSaleModData.getString(1)))
		    {
			mapHomeDel.put(rsGroupwiseSaleModData.getString(1), mapHomeDel.get(rsGroupwiseSaleModData.getString(1)) + rsGroupwiseSaleModData.getDouble(2));
		    }
		    else
		    {
			mapHomeDel.put(rsGroupwiseSaleModData.getString(1), rsGroupwiseSaleModData.getDouble(2));
		    }
		}

		clsSettelementOptions objSettle = new clsSettelementOptions();
		if (hmSalesGroupWiseSaleData.containsKey(rsGroupwiseSaleModData.getString(3)))
		{
		    arrListGroupwiseSaleData = hmSalesGroupWiseSaleData.get(rsGroupwiseSaleModData.getString(3));
		    for (int j = 0; j < arrListGroupwiseSaleData.size(); j++)
		    {
			objSettle = arrListGroupwiseSaleData.get(j);
			if (objSettle.getStrSettelmentDesc().equals(rsGroupwiseSaleModData.getString(1)))
			{
			    arrListGroupwiseSaleData.remove(objSettle);
			    double settleAmt = objSettle.getDblSettlementAmt();
			    objSettle.setDblSettlementAmt(settleAmt + rsGroupwiseSaleModData.getDouble(2));
			}
			else
			{
			    objSettle = new clsSettelementOptions();
			    objSettle.setDblSettlementAmt(rsGroupwiseSaleModData.getDouble(2));
			    objSettle.setStrSettelmentDesc(rsGroupwiseSaleModData.getString(1));
			    objSettle.setStrSettelmentType(rsGroupwiseSaleModData.getString(3));
			    objSettle.setStrRemark(rsGroupwiseSaleModData.getString(4));
			}
		    }
		}
		else
		{
		    arrListGroupwiseSaleData = new ArrayList<clsSettelementOptions>();
		    objSettle.setStrSettelmentDesc(rsGroupwiseSaleModData.getString(1));
		    objSettle.setStrSettelmentType(rsGroupwiseSaleModData.getString(3));
		    objSettle.setDblSettlementAmt(rsGroupwiseSaleModData.getDouble(2));
		    objSettle.setStrRemark(rsGroupwiseSaleModData.getString(4));

		}
		arrListGroupwiseSaleData.add(objSettle);
		hmSalesGroupWiseSaleData.put(rsGroupwiseSaleModData.getString(3), arrListGroupwiseSaleData);
	    }
	    rsGroupwiseSaleModData.close();
    }	
    
    public void funTaxWiseQLiveData(StringBuilder sqlData) throws Exception
    {
	ResultSet rsTaxwiseSaleData = clsGlobalVarClass.dbMysql.executeResultSet(sqlData.toString());
	    while (rsTaxwiseSaleData.next())
	    {
		clsSettelementOptions objSettle = new clsSettelementOptions();
		if (hmSalesTaxWiseSaleData.containsKey(rsTaxwiseSaleData.getString(1)))
		{
		    arrListTaxwiseSaleData = hmSalesTaxWiseSaleData.get(rsTaxwiseSaleData.getString(1));
		    for (int j = 0; j < arrListTaxwiseSaleData.size(); j++)
		    {
			objSettle = arrListTaxwiseSaleData.get(j);
			if (objSettle.getStrSettelmentDesc().equals(rsTaxwiseSaleData.getString(2)))
			{
			    arrListTaxwiseSaleData.remove(objSettle);
			    double settleAmt = objSettle.getDblSettlementAmt();
			    objSettle.setDblSettlementAmt(settleAmt + rsTaxwiseSaleData.getDouble(3));
			}
			else
			{
			    objSettle = new clsSettelementOptions();
			    objSettle.setDblSettlementAmt(rsTaxwiseSaleData.getDouble(3));
			    objSettle.setStrSettelmentDesc(rsTaxwiseSaleData.getString(2));
			    objSettle.setStrSettelmentType(rsTaxwiseSaleData.getString(1));
			}
		    }
		}
		else
		{
		    arrListTaxwiseSaleData = new ArrayList<clsSettelementOptions>();
		    objSettle.setStrSettelmentDesc(rsTaxwiseSaleData.getString(2));
		    objSettle.setStrSettelmentType(rsTaxwiseSaleData.getString(1));
		    objSettle.setDblSettlementAmt(rsTaxwiseSaleData.getDouble(3));

		}
		arrListTaxwiseSaleData.add(objSettle);
		hmSalesTaxWiseSaleData.put(rsTaxwiseSaleData.getString(1), arrListTaxwiseSaleData);
	    }
	    rsTaxwiseSaleData.close();
    }	       
}
