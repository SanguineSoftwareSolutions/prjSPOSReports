package com.POSReport.controller;

import com.POSReport.controller.comparator.clsWaiterWiseSalesComparator;
import com.POSGlobal.controller.clsBillDtl;
import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsGroupSubGroupWiseSales;
import com.POSReport.controller.comparator.clsGroupSubGroupWiseSalesComparator;
import com.POSReport.controller.comparator.clsWaiterWiseAPCComparator;
import java.awt.Desktop;
import java.awt.Dimension;
import java.io.File;
import java.io.InputStream;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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

public class clsWaiterWiseItemWiseIncentivesSummaryReport
{

    DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();

    public void funWaiterWiseItemWiseIncentivesSummaryReport(String reportType, HashMap hm, String dayEnd, String type)
    {
	try
	{
	    InputStream is = null;
	    if (type.equalsIgnoreCase("Item Wise"))
	    {
		is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptItemWiseIncentivesReport.jasper");
	    }
	    else if (type.equalsIgnoreCase("Summary"))
	    {
		is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptWaiterWiseItemWiseIncentivesSummaryWiseReport.jasper");
	    }
	    else
	    {
		is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptWaiterWiseItemWiseIncentivesReport.jasper");
	    }

	    InputStream rptWaiterWiseItemWiseIncSubReportForGroupWiseSales = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptWaiterWiseItemWiseIncSubReportForGroupWiseSales.jasper");
	    hm.put("rptWaiterWiseItemWiseIncSubReportForGroupWiseSales", rptWaiterWiseItemWiseIncSubReportForGroupWiseSales);

	    InputStream rptWaiterWiseItemWiseIncSubReportForWaiterWiseSales = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptWaiterWiseItemWiseIncSubReportForWaiterWiseSales.jasper");
	    hm.put("rptWaiterWiseItemWiseIncSubReportForWaiterWiseSales", rptWaiterWiseItemWiseIncSubReportForWaiterWiseSales);

	    InputStream rptWaiterWiseItemWiseIncSubReportForAPC = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptWaiterWiseItemWiseIncSubReportForAPC.jasper");
	    hm.put("rptWaiterWiseItemWiseIncSubReportForAPC", rptWaiterWiseItemWiseIncSubReportForAPC);

	    String fromDate = hm.get("fromDate").toString();
	    String toDate = hm.get("toDate").toString();
	    String posCode = hm.get("posCode").toString();
	    String shiftNo = hm.get("shiftNo").toString();
	    String posName = hm.get("posName").toString();
	    String groupCode = hm.get("groupCode").toString();
	    String subGroupCode = hm.get("subGroupCode").toString();

	    if (type.equalsIgnoreCase("Item Wise"))
	    {
		funCallItemWiseIncentiveReport(reportType, hm, dayEnd, type);
	    }
	    else
	    {
		StringBuilder sqlBuilder = new StringBuilder();
		List<clsBillDtl> listOfWaiterWiseItemSales = new ArrayList<>();

		String waiterShortName = " '' ";
		if (!type.equalsIgnoreCase("Item Wise"))
		{
		    waiterShortName = " d.strWShortName ";
		}
		String waiterShortNo = " '' ";
		if (!type.equalsIgnoreCase("Item Wise"))
		{
		    waiterShortNo = " d.strWaiterNo ";
		}

		//Live Data
		sqlBuilder.setLength(0);
		sqlBuilder.append("SELECT " + waiterShortName + ",b.strItemName,sum(b.dblAmount),c.dblIncentiveValue "
			+ " ,IF(c.strIncentiveType='Amt', (c.dblIncentiveValue)*sum(b.dblQuantity), (c.dblIncentiveValue/100)*sum(b.dblAmount)) as amount, "
			+ " e.strPosName,e.strPosCode,b.strItemCode," + waiterShortNo + ",c.strIncentiveType,sum(b.dblQuantity)  "
			+ " FROM tblbillhd a,tblbilldtl b,tblposwiseitemwiseincentives c ");
		if (!type.equalsIgnoreCase("Item Wise"))
		{
		    sqlBuilder.append(",tblwaitermaster d ");
		}
		sqlBuilder.append(",tblposmaster e,tblitemmaster f,tblsubgrouphd g,tblgrouphd h "
			+ " where a.strBillNo=b.strBillNo "
			+ " and b.strItemCode=c.strItemCode ");
		if (!type.equalsIgnoreCase("Item Wise"))
		{
		    sqlBuilder.append(" and b.strWaiterNo=d.strWaiterNo ");
		}
		sqlBuilder.append(" and a.strPOSCode=e.strPosCode "
			+ " and a.strPOSCode=c.strPOSCode "
			+ " and c.dblIncentiveValue>0 "
			+ " and b.strItemCode=f.strItemCode "
			+ " and f.strSubGroupCode=g.strSubGroupCode "
			+ " and g.strGroupCode=h.strGroupCode "
			+ " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
		if (!posCode.equalsIgnoreCase("All"))
		{
		    sqlBuilder.append("and a.strPOSCode='" + posCode + "' ");
		}
		if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
		{
		    sqlBuilder.append("and a.intShiftCode='" + shiftNo + "' ");
		}
		if (!groupCode.equalsIgnoreCase("All"))
		{
		    sqlBuilder.append(" and h.strGroupCode='" + groupCode + "' ");
		}
		if (!subGroupCode.equalsIgnoreCase("All"))
		{
		    sqlBuilder.append(" and g.strSubGroupCode='" + subGroupCode + "' ");
		}

		sqlBuilder.append("and a.strBillNo not in (select u.strBillNo "
			+ " from tblbillhd v,tblbillsettlementdtl u,tblsettelmenthd w "
			+ " where v.strBillNo=u.strBillNo and u.strSettlementCode=w.strSettelmentCode "
			+ " and w.strSettelmentType='Complementary' and date(v.dteBillDate) between '" + fromDate + "' and '" + toDate + "')");
		if (type.equalsIgnoreCase("Item Wise"))
		{
		    sqlBuilder.append(" group by b.strItemCode ");
		    sqlBuilder.append(" order by b.strItemName ");
		}
		else if (type.equalsIgnoreCase("Summary"))
		{
		    sqlBuilder.append(" group by b.strWaiterNo");
		    sqlBuilder.append(" order by d.strWShortName ");
		}
		else
		{
		    sqlBuilder.append(" group by b.strWaiterNo,c.strPOSCode,b.strItemCode ");
		    sqlBuilder.append(" order by e.strPosName,d.strWShortName,b.strItemName ");
		}
		ResultSet rsWaiterWiseItemSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
		while (rsWaiterWiseItemSales.next())
		{
		    clsBillDtl obj = new clsBillDtl();

		    obj.setStrWShortName(rsWaiterWiseItemSales.getString(1));
		    obj.setStrItemName(rsWaiterWiseItemSales.getString(2));
		    obj.setStrItemCode(rsWaiterWiseItemSales.getString(8));
		    obj.setDblAmount(rsWaiterWiseItemSales.getDouble(3));
		    obj.setDblIncentivePer(rsWaiterWiseItemSales.getDouble(4));
		    obj.setDblIncentive(rsWaiterWiseItemSales.getDouble(5));
		    obj.setStrPosName(rsWaiterWiseItemSales.getString(6));
		    obj.setStrPOSCode(rsWaiterWiseItemSales.getString(7));
		    obj.setStrWaiterNo(rsWaiterWiseItemSales.getString(9));
		    obj.setStrRemarks(rsWaiterWiseItemSales.getString(10));
		    obj.setDblQuantity(rsWaiterWiseItemSales.getDouble(11));

		    listOfWaiterWiseItemSales.add(obj);
		}
		rsWaiterWiseItemSales.close();

		//Q Data
		sqlBuilder.setLength(0);
		sqlBuilder.append("SELECT " + waiterShortName + ",b.strItemName,sum(b.dblAmount),c.dblIncentiveValue "
			+ " ,IF(c.strIncentiveType='Amt', (c.dblIncentiveValue)*sum(b.dblQuantity), (c.dblIncentiveValue/100)*sum(b.dblAmount)) as amount, "
			+ " e.strPosName,e.strPosCode,b.strItemCode," + waiterShortNo + ",c.strIncentiveType,sum(b.dblQuantity)  "
			+ " FROM tblqbillhd a,tblqbilldtl b,tblposwiseitemwiseincentives c ");
		if (!type.equalsIgnoreCase("Item Wise"))
		{
		    sqlBuilder.append(",tblwaitermaster d ");
		}
		sqlBuilder.append(",tblposmaster e,tblitemmaster f,tblsubgrouphd g,tblgrouphd h "
			+ " where a.strBillNo=b.strBillNo "
			+ " and b.strItemCode=c.strItemCode ");
		if (!type.equalsIgnoreCase("Item Wise"))
		{
		    sqlBuilder.append(" and b.strWaiterNo=d.strWaiterNo ");
		}
		sqlBuilder.append(" and a.strPOSCode=e.strPosCode "
			+ " and a.strPOSCode=c.strPOSCode "
			+ " and c.dblIncentiveValue>0 "
			+ " and b.strItemCode=f.strItemCode "
			+ " and f.strSubGroupCode=g.strSubGroupCode "
			+ " and g.strGroupCode=h.strGroupCode "
			+ " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
		if (!posCode.equalsIgnoreCase("All"))
		{
		    sqlBuilder.append("and a.strPOSCode='" + posCode + "' ");
		}
		if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
		{
		    sqlBuilder.append("and a.intShiftCode='" + shiftNo + "' ");
		}
		if (!groupCode.equalsIgnoreCase("All"))
		{
		    sqlBuilder.append(" and h.strGroupCode='" + groupCode + "' ");
		}
		if (!subGroupCode.equalsIgnoreCase("All"))
		{
		    sqlBuilder.append(" and g.strSubGroupCode='" + subGroupCode + "' ");
		}

		sqlBuilder.append("and a.strBillNo not in (select u.strBillNo "
			+ " from tblqbillhd v,tblqbillsettlementdtl u,tblsettelmenthd w "
			+ " where v.strBillNo=u.strBillNo and u.strSettlementCode=w.strSettelmentCode "
			+ " and w.strSettelmentType='Complementary' and date(v.dteBillDate) between '" + fromDate + "' and '" + toDate + "')");
		if (type.equalsIgnoreCase("Item Wise"))
		{
		    sqlBuilder.append(" group by b.strItemCode ");
		    sqlBuilder.append(" order by b.strItemName ");
		}
		else if (type.equalsIgnoreCase("Summary"))
		{
		    sqlBuilder.append(" group by b.strWaiterNo");
		    sqlBuilder.append(" order by d.strWShortName ");
		}
		else
		{
		    sqlBuilder.append(" group by b.strWaiterNo,c.strPOSCode,b.strItemCode ");
		    sqlBuilder.append(" order by e.strPosName,d.strWShortName,b.strItemName ");
		}
		rsWaiterWiseItemSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
		while (rsWaiterWiseItemSales.next())
		{
		    clsBillDtl obj = new clsBillDtl();

		    obj.setStrWShortName(rsWaiterWiseItemSales.getString(1));
		    obj.setStrItemName(rsWaiterWiseItemSales.getString(2));
		    obj.setStrItemCode(rsWaiterWiseItemSales.getString(8));
		    obj.setDblAmount(rsWaiterWiseItemSales.getDouble(3));
		    obj.setDblIncentivePer(rsWaiterWiseItemSales.getDouble(4));
		    obj.setDblIncentive(rsWaiterWiseItemSales.getDouble(5));
		    obj.setStrPosName(rsWaiterWiseItemSales.getString(6));
		    obj.setStrPOSCode(rsWaiterWiseItemSales.getString(7));
		    obj.setStrWaiterNo(rsWaiterWiseItemSales.getString(9));
		    obj.setStrRemarks(rsWaiterWiseItemSales.getString(10));
		    obj.setDblQuantity(rsWaiterWiseItemSales.getDouble(11));

		    listOfWaiterWiseItemSales.add(obj);
		}
		rsWaiterWiseItemSales.close();

		Comparator<clsBillDtl> waiterCodeComparator = new Comparator<clsBillDtl>()
		{

		    @Override
		    public int compare(clsBillDtl o1, clsBillDtl o2)
		    {
			return o1.getStrWShortName().compareTo(o2.getStrWShortName());
		    }
		};

		Collections.sort(listOfWaiterWiseItemSales, new clsWaiterWiseSalesComparator(waiterCodeComparator));

		/**
		 * start Group wise sales
		 */
		StringBuilder sbSqlLive = new StringBuilder();
		StringBuilder sbSqlQFile = new StringBuilder();
		StringBuilder sbSqlFilters = new StringBuilder();

		Map<String, List<Map<String, clsGroupSubGroupWiseSales>>> mapPOSDtlForGroupSubGroup = new HashMap<>();
		Map<String, Map<String, clsGroupSubGroupWiseSales>> mapWaiterWiseGroupSales = new HashMap<>();
		List<clsGroupSubGroupItemBean> listOfGroupWiseSales = new ArrayList<clsGroupSubGroupItemBean>();
		List<clsGroupSubGroupWiseSales> listOfGroupWise = new ArrayList<clsGroupSubGroupWiseSales>();

		sbSqlLive.setLength(0);
		sbSqlQFile.setLength(0);
		sbSqlFilters.setLength(0);

		sbSqlQFile.append("SELECT c.strGroupCode,c.strGroupName,sum( b.dblQuantity)"
			+ ",sum( b.dblAmount)-sum(b.dblDiscountAmt) "
			+ ",f.strPosName, '" + clsGlobalVarClass.gUserCode + "',b.dblRate ,sum(b.dblAmount),sum(b.dblDiscountAmt),a.strPOSCode,"
			+ "sum( b.dblAmount)-sum(b.dblDiscountAmt)+sum(b.dblTaxAmount),g.strWShortName  "
			+ "FROM tblqbillhd a,tblqbilldtl b,tblgrouphd c,tblsubgrouphd d"
			+ ",tblitemmaster e,tblposmaster f,tblwaitermaster g "
			+ "where a.strBillNo=b.strBillNo "
			+ " and date(a.dteBillDate)=date(b.dteBillDate) "
			+ " and a.strPOSCode=f.strPOSCode  "
			+ " and a.strClientCode=b.strClientCode "
			+ "and b.strItemCode=e.strItemCode "
			+ "and c.strGroupCode=d.strGroupCode "
			+ "and d.strSubGroupCode=e.strSubGroupCode "
			+ "and a.strWaiterNo=g.strWaiterNo ");

		sbSqlLive.append("SELECT c.strGroupCode,c.strGroupName,sum( b.dblQuantity)"
			+ ",sum( b.dblAmount)-sum(b.dblDiscountAmt) "
			+ ",f.strPosName, '" + clsGlobalVarClass.gUserCode + "',b.dblRate ,sum(b.dblAmount),sum(b.dblDiscountAmt),a.strPOSCode,"
			+ " sum( b.dblAmount)-sum(b.dblDiscountAmt)+sum(b.dblTaxAmount),g.strWShortName  "
			+ "FROM tblbillhd a,tblbilldtl b,tblgrouphd c,tblsubgrouphd d"
			+ ",tblitemmaster e,tblposmaster f,tblwaitermaster g "
			+ "where a.strBillNo=b.strBillNo "
			+ " and date(a.dteBillDate)=date(b.dteBillDate) "
			+ " and a.strPOSCode=f.strPOSCode  "
			+ " and a.strClientCode=b.strClientCode   "
			+ "and b.strItemCode=e.strItemCode "
			+ "and c.strGroupCode=d.strGroupCode "
			+ " and d.strSubGroupCode=e.strSubGroupCode "
			+ "and a.strWaiterNo=g.strWaiterNo ");

		String sqlModLive = "select c.strGroupCode,c.strGroupName"
			+ ",sum(b.dblQuantity),sum(b.dblAmount)-sum(b.dblDiscAmt),f.strPOSName"
			+ ",'" + clsGlobalVarClass.gUserCode + "','0' ,sum(b.dblAmount),sum(b.dblDiscAmt),a.strPOSCode,"
			+ " sum(b.dblAmount)-sum(b.dblDiscAmt),g.strWShortName  "
			+ " from tblbillmodifierdtl b,tblbillhd a,tblposmaster f,tblitemmaster d"
			+ ",tblsubgrouphd e,tblgrouphd c,tblwaitermaster g "
			+ " where a.strBillNo=b.strBillNo "
			+ " and date(a.dteBillDate)=date(b.dteBillDate) "
			+ " and a.strPOSCode=f.strPosCode  "
			+ " and a.strClientCode=b.strClientCode  "
			+ " and LEFT(b.strItemCode,7)=d.strItemCode "
			+ " and d.strSubGroupCode=e.strSubGroupCode "
			+ " and e.strGroupCode=c.strGroupCode "
			+ "and a.strWaiterNo=g.strWaiterNo "
			+ " and b.dblamount>0 ";

		String sqlModQFile = "select c.strGroupCode,c.strGroupName"
			+ ",sum(b.dblQuantity),sum(b.dblAmount)-sum(b.dblDiscAmt),f.strPOSName"
			+ ",'" + clsGlobalVarClass.gUserCode + "','0' ,sum(b.dblAmount),sum(b.dblDiscAmt),a.strPOSCode,"
			+ " sum(b.dblAmount)-sum(b.dblDiscAmt),g.strWShortName "
			+ " from tblqbillmodifierdtl b,tblqbillhd a,tblposmaster f,tblitemmaster d"
			+ ",tblsubgrouphd e,tblgrouphd c,tblwaitermaster g "
			+ " where a.strBillNo=b.strBillNo "
			+ " and date(a.dteBillDate)=date(b.dteBillDate) "
			+ " and a.strPOSCode=f.strPosCode   "
			+ " and a.strClientCode=b.strClientCode   "
			+ " and LEFT(b.strItemCode,7)=d.strItemCode "
			+ " and d.strSubGroupCode=e.strSubGroupCode "
			+ " and e.strGroupCode=c.strGroupCode "
			+ "and a.strWaiterNo=g.strWaiterNo "
			+ " and b.dblamount>0 ";

		sbSqlFilters.append(" and date( a.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' ");
		if (!posCode.equals("All"))
		{
		    sbSqlFilters.append(" AND a.strPOSCode = '" + posCode + "' ");
		}

		if (clsGlobalVarClass.gEnableShiftYN)
		{
		    if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
		    {
			sbSqlFilters.append(" and a.intShiftCode = '" + shiftNo + "' ");
		    }
		}

		if (!subGroupCode.equalsIgnoreCase("All"))
		{
		    sbSqlFilters.append("AND d.strSubGroupCode='" + subGroupCode + "' ");
		}
		sbSqlFilters.append(" GROUP BY c.strGroupCode, c.strGroupName,g.strWaiterNo "
			+ "order BY c.strGroupCode, c.strGroupName,g.strWaiterNo ");

		sbSqlLive.append(sbSqlFilters);
		sbSqlQFile.append(sbSqlFilters);

		sqlModLive += " " + sbSqlFilters;
		sqlModQFile += " " + sbSqlFilters;

		ResultSet rsGroupWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
		funGenerateGroupWiseSales(rsGroupWiseSales, mapPOSDtlForGroupSubGroup, mapWaiterWiseGroupSales);
		rsGroupWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlModLive);
		funGenerateGroupWiseSales(rsGroupWiseSales, mapPOSDtlForGroupSubGroup, mapWaiterWiseGroupSales);
		rsGroupWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFile.toString());
		funGenerateGroupWiseSales(rsGroupWiseSales, mapPOSDtlForGroupSubGroup, mapWaiterWiseGroupSales);
		rsGroupWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlModQFile);
		funGenerateGroupWiseSales(rsGroupWiseSales, mapPOSDtlForGroupSubGroup, mapWaiterWiseGroupSales);

		double totalNetTotal = 0.00;
		Iterator<Map.Entry<String, List<Map<String, clsGroupSubGroupWiseSales>>>> it = mapPOSDtlForGroupSubGroup.entrySet().iterator();
		while (it.hasNext())
		{
		    Map.Entry<String, List<Map<String, clsGroupSubGroupWiseSales>>> entry = it.next();
		    String posCode1 = entry.getKey();
		    List<Map<String, clsGroupSubGroupWiseSales>> listOfGroup = entry.getValue();
		    for (int i = 0; i < listOfGroup.size(); i++)
		    {
			clsGroupSubGroupWiseSales objGroupDtl = listOfGroup.get(i).entrySet().iterator().next().getValue();

			Object[] arrObjRows =
			{
			    objGroupDtl.getGroupName(), objGroupDtl.getPosName(), objGroupDtl.getQty(), objGroupDtl.getSalesAmt(), objGroupDtl.getSubTotal(), objGroupDtl.getDiscAmt()
			};

			listOfGroupWise.add(objGroupDtl);
			totalNetTotal = totalNetTotal + objGroupDtl.getSalesAmt();
		    }
		}

		Map<String, Double> mapGroupWiseSales = new HashMap<String, Double>();
		for (clsGroupSubGroupWiseSales objGroupWiseSales : listOfGroupWise)
		{
		    double perToNetTotal = 0.00;
		    if (objGroupWiseSales.getSalesAmt() > 0)
		    {
			perToNetTotal = (objGroupWiseSales.getSalesAmt() / totalNetTotal) * 100;
			objGroupWiseSales.setDblPerToNetTotal(perToNetTotal);
		    }
		    if (mapGroupWiseSales.containsKey(objGroupWiseSales.getGroupName()))
		    {
			mapGroupWiseSales.put(objGroupWiseSales.getGroupName(), mapGroupWiseSales.get(objGroupWiseSales.getGroupName()) + objGroupWiseSales.getSalesAmt());
		    }
		    else
		    {
			mapGroupWiseSales.put(objGroupWiseSales.getGroupName(), objGroupWiseSales.getSalesAmt());
		    }
		}

		Comparator<clsGroupSubGroupWiseSales> groupNameComparator = new Comparator<clsGroupSubGroupWiseSales>()
		{

		    @Override
		    public int compare(clsGroupSubGroupWiseSales o1, clsGroupSubGroupWiseSales o2)
		    {
			return o1.getGroupName().compareToIgnoreCase(o2.getGroupName());
		    }
		};

		Comparator<clsGroupSubGroupWiseSales> waiterNameComparator = new Comparator<clsGroupSubGroupWiseSales>()
		{

		    @Override
		    public int compare(clsGroupSubGroupWiseSales o1, clsGroupSubGroupWiseSales o2)
		    {
			return o1.getStrWaiterShortName().compareToIgnoreCase(o2.getStrWaiterShortName());
		    }
		};

		Collections.sort(listOfGroupWise, new clsGroupSubGroupWiseSalesComparator(groupNameComparator)
		);

		hm.put("listOfGroupSales", listOfGroupWise);

		List<clsGroupSubGroupWiseSales> listOfWaiterWiseGroupSales = new ArrayList<>();
		for (Map<String, clsGroupSubGroupWiseSales> mapGroupSales : mapWaiterWiseGroupSales.values())
		{
		    for (clsGroupSubGroupWiseSales objGroupWiseSales : mapGroupSales.values())
		    {
			double waiterWiseGroupSalePer = 0.00;
			if (mapGroupWiseSales.containsKey(objGroupWiseSales.getGroupName()))
			{
			    double totalGroupNameSale = mapGroupWiseSales.get(objGroupWiseSales.getGroupName());
			    objGroupWiseSales.setDblTotalNetTotal(totalGroupNameSale);
			    if (totalGroupNameSale > 0)
			    {
				waiterWiseGroupSalePer = (objGroupWiseSales.getSalesAmt() / totalGroupNameSale) * 100;
			    }
			}
			objGroupWiseSales.setDblPerToNetTotal(waiterWiseGroupSalePer);

			listOfWaiterWiseGroupSales.add(objGroupWiseSales);
		    }
		}

		Collections.sort(listOfWaiterWiseGroupSales, new clsGroupSubGroupWiseSalesComparator(waiterNameComparator, groupNameComparator)
		);
		hm.put("listOfWaiterWiseGroupSales", listOfWaiterWiseGroupSales);

		/**
		 * End Group wise sales
		 */
		/**
		 * start of APC
		 */
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
		if (!posCode.equals("All"))
		{
		    sqlLiveNonComplimentaryBuilder.append("and a.strPOSCode='" + posCode + "' ");
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
		if (!posCode.equals("All"))
		{
		    sqlQNonComplimentaryBuilder.append("and a.strPOSCode='" + posCode + "' ");
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
		sqlLiveComplimentaryBuilder.append(" group by a.strPOSCode,date(a.dteBillDate),a.strBillNo ");

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

		sqlQComplimentaryBuilder.append(" group by a.strPOSCode,date(a.dteBillDate),a.strBillNo ");

		Map<String, clsAPCReport> mapNonComplementaryBillWiseAPCReport = new HashMap<>();
		Map<String, clsAPCReport> mapComplementaryAPCReport = new HashMap<>();

		ResultSet rsNonComplementary = clsGlobalVarClass.dbMysql.executeResultSet(sqlLiveNonComplimentaryBuilder.toString());
		while (rsNonComplementary.next())
		{
		    String pos = posName;
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
		    String pos = posName;
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
		if (!posCode.equals("All"))
		{
		    sqlLiveNonComplimentaryBuilder.append("and a.strPOSCode='" + posCode + "' ");
		}
		sqlLiveNonComplimentaryBuilder.append(" group by a.strPOSCode,date(a.dteBillDate),a.strBillNo ");
		rsNonComplementary = clsGlobalVarClass.dbMysql.executeResultSet(sqlLiveNonComplimentaryBuilder.toString());
		while (rsNonComplementary.next())
		{
		    String pos = posName;
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
		if (!posCode.equals("All"))
		{
		    sqlQNonComplimentaryBuilder.append("and a.strPOSCode='" + posCode + "' ");
		}
		sqlQNonComplimentaryBuilder.append(" group by a.strPOSCode,date(a.dteBillDate),a.strBillNo ");
		rsQNonComplementary = clsGlobalVarClass.dbMysql.executeResultSet(sqlQNonComplimentaryBuilder.toString());
		while (rsQNonComplementary.next())
		{
		    String pos = posName;
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
		    String pos = posName;
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
		    String pos = posName;
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
		double dinningAmt = 0.00;
		sqlTempTbl.setLength(0);
		sqlTempTbl.append("SELECT "
			+ " sum(a.`dblDiningAmt`) AS dblDiningAmt "
			+ " FROM"
			+ " `tblatvreport` a");
		ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(sqlTempTbl.toString());
		if (rs.next())
		{
		    dinningAmt = rs.getDouble(1);
		}
		rs.close();

		sqlTempTbl.setLength(0);
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

		sqlTempTbl.append(" group by a.strWaiterName ");
		rs = clsGlobalVarClass.dbMysql.executeResultSet(sqlTempTbl.toString());
		List<clsAPCReport> listOfWaiterWiseAPC = new LinkedList<clsAPCReport>();

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

		    double apcPer = (objAPCReport.getNetTotal() / dinningAmt) * 100;
		    objAPCReport.setDblAPCPer(apcPer);

		    listOfWaiterWiseAPC.add(objAPCReport);

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

		Collections.sort(listOfWaiterWiseAPC, new clsWaiterWiseAPCComparator(posComparator, dateComparator, waiterComparator));

		hm.put("dinningAmt", dinningAmt);
		hm.put("listOfWaiterWiseAPC", listOfWaiterWiseAPC);
		/**
		 * End of APC
		 */
		//call for view report
		if (reportType.equalsIgnoreCase("A4 Size Report"))
		{
		    funViewJasperReportForBeanCollectionDataSource(is, hm, listOfWaiterWiseItemSales);
		}
//            DecimalFormat gDecimalFormat = new DecimalFormat("0.00");
		DecimalFormat decimalFormat1Decimal = new DecimalFormat("0.0");
		if (reportType.equalsIgnoreCase("Excel Report"))
		{
		    Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
		    List<String> arrListTotal = new ArrayList<String>();
		    List<String> arrHeaderList = new ArrayList<String>();
		    double totalAmount = 0;
		    double totalQty = 0;
		    double totalIncentiveAmt = 0;
		    int i = 1;

		    for (clsBillDtl objBean : listOfWaiterWiseItemSales)
		    {
			List<String> arrListItem = new ArrayList<String>();

			if (type.equalsIgnoreCase("Summary"))
			{
			    arrListItem.add(objBean.getStrWShortName());
			    arrListItem.add(decimalFormat1Decimal.format(objBean.getDblQuantity()));
			    arrListItem.add(gDecimalFormat.format(objBean.getDblAmount()));
			    arrListItem.add(gDecimalFormat.format(objBean.getDblIncentive()));
			    arrListItem.add(" ");
			}
			else
			{
			    arrListItem.add(objBean.getStrWShortName());
			    arrListItem.add(objBean.getStrItemName());
			    arrListItem.add(objBean.getStrPosName());
			    arrListItem.add(decimalFormat1Decimal.format(objBean.getDblQuantity()));
			    arrListItem.add(objBean.getStrRemarks());
			    arrListItem.add(gDecimalFormat.format(objBean.getDblAmount()));
			    arrListItem.add(gDecimalFormat.format(objBean.getDblIncentivePer()));
			    arrListItem.add(gDecimalFormat.format(objBean.getDblIncentive()));
			    arrListItem.add(" ");
			}
			totalAmount = totalAmount + objBean.getDblAmount();
			totalIncentiveAmt = totalIncentiveAmt + objBean.getDblIncentive();
			totalQty = totalQty + objBean.getDblQuantity();
			mapExcelItemDtl.put(i, arrListItem);

			i++;
		    }
		    if (type.equalsIgnoreCase("Summary"))
		    {
			arrListTotal.add(decimalFormat1Decimal.format(totalQty) + "#" + "2");
			arrListTotal.add(gDecimalFormat.format(totalAmount) + "#" + "3");
			arrListTotal.add(gDecimalFormat.format(totalIncentiveAmt) + "#" + "4");
			arrHeaderList.add("Serial No");
			arrHeaderList.add("Waiter Name");
			arrHeaderList.add("Qty");
			arrHeaderList.add("Amount");
			arrHeaderList.add("Incentive Amt");
			arrHeaderList.add(" ");
		    }
		    else
		    {
			arrListTotal.add(decimalFormat1Decimal.format(totalQty) + "#" + "4");
			arrListTotal.add(gDecimalFormat.format(totalAmount) + "#" + "6");
			arrListTotal.add(gDecimalFormat.format(totalIncentiveAmt) + "#" + "8");
			arrHeaderList.add("Serial No");
			arrHeaderList.add("Waiter Name");
			arrHeaderList.add("Item Name");
			arrHeaderList.add("POS Name");
			arrHeaderList.add("Qty");
			arrHeaderList.add("Incentive Type");
			arrHeaderList.add("Amount");
			arrHeaderList.add("value");
			arrHeaderList.add("Incentive Amt");
			arrHeaderList.add(" ");
		    }
		    List<String> arrparameterList = new ArrayList<String>();
		    arrparameterList.add("Waiter Wise Item Wise Incentives Report");
		    arrparameterList.add("POS" + " : " + posName);
		    arrparameterList.add("FromDate" + " : " + fromDate);
		    arrparameterList.add("ToDate" + " : " + toDate);
		    arrparameterList.add("");
		    arrparameterList.add("  ");
		    if (clsGlobalVarClass.gEnableShiftYN)
		    {
			if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
			{
			    arrparameterList.add("Shift No " + " : " + shiftNo.toString());
			}
			else
			{
			    arrparameterList.add("Shift No " + " : " + shiftNo.toString());
			}
		    }

		    funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "waiterWiseItemWiseIncentivesExcelSheet", dayEnd);
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

		sheet1.addCell(l0);
		sheet1.addCell(l1);
		sheet1.addCell(l2);
		sheet1.addCell(l3);
		sheet1.addCell(l4);
		sheet1.addCell(l5);
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

    private void funGenerateGroupWiseSales(ResultSet rsGroupWiseSales, Map<String, List<Map<String, clsGroupSubGroupWiseSales>>> mapPOSDtlForGroupSubGroup, Map<String, Map<String, clsGroupSubGroupWiseSales>> mapWaiterWiseGroupSales)
    {
	try
	{
	    boolean flgRecords = false;
	    while (rsGroupWiseSales.next())
	    {
		String groupName = rsGroupWiseSales.getString(2);
		String waiterShortName = rsGroupWiseSales.getString(12);
		double netTotal = rsGroupWiseSales.getDouble(4);

		flgRecords = true;
		if (mapPOSDtlForGroupSubGroup.containsKey(rsGroupWiseSales.getString(10)))//posCode
		{
		    String posCode = rsGroupWiseSales.getString(10);
		    String groupCode = rsGroupWiseSales.getString(1);
		    List<Map<String, clsGroupSubGroupWiseSales>> listOfGroup = mapPOSDtlForGroupSubGroup.get(posCode);
		    boolean isGroupExists = false;
		    int groupIndex = 0;
		    for (int i = 0; i < listOfGroup.size(); i++)
		    {
			if (listOfGroup.get(i).containsKey(groupCode))
			{
			    isGroupExists = true;
			    groupIndex = i;
			    break;
			}
		    }
		    if (isGroupExists)
		    {
			Map<String, clsGroupSubGroupWiseSales> mapGroupCodeDtl = listOfGroup.get(groupIndex);
			clsGroupSubGroupWiseSales objGroupCodeDtl = mapGroupCodeDtl.get(groupCode);
			objGroupCodeDtl.setGroupCode(rsGroupWiseSales.getString(1));
			objGroupCodeDtl.setGroupName(rsGroupWiseSales.getString(2));
			objGroupCodeDtl.setPosName(rsGroupWiseSales.getString(5));
			objGroupCodeDtl.setQty(objGroupCodeDtl.getQty() + rsGroupWiseSales.getDouble(3));
			objGroupCodeDtl.setSubTotal(objGroupCodeDtl.getSubTotal() + rsGroupWiseSales.getDouble(8));
			objGroupCodeDtl.setSalesAmt(objGroupCodeDtl.getSalesAmt() + rsGroupWiseSales.getDouble(4));
			objGroupCodeDtl.setDiscAmt(objGroupCodeDtl.getDiscAmt() + rsGroupWiseSales.getDouble(9));
			objGroupCodeDtl.setGrandTotal(objGroupCodeDtl.getGrandTotal() + rsGroupWiseSales.getDouble(11));
		    }
		    else
		    {
			Map<String, clsGroupSubGroupWiseSales> mapGroupCodeDtl = new LinkedHashMap<>();
			clsGroupSubGroupWiseSales objGroupCodeDtl = new clsGroupSubGroupWiseSales(
				rsGroupWiseSales.getString(1), rsGroupWiseSales.getString(2), rsGroupWiseSales.getString(5), rsGroupWiseSales.getDouble(3), rsGroupWiseSales.getDouble(8), rsGroupWiseSales.getDouble(4), rsGroupWiseSales.getDouble(9), rsGroupWiseSales.getDouble(11));
			mapGroupCodeDtl.put(rsGroupWiseSales.getString(1), objGroupCodeDtl);
			listOfGroup.add(mapGroupCodeDtl);
		    }
		}
		else
		{
		    List<Map<String, clsGroupSubGroupWiseSales>> listOfGroupDtl = new ArrayList<>();
		    Map<String, clsGroupSubGroupWiseSales> mapGroupCodeDtl = new LinkedHashMap<>();
		    clsGroupSubGroupWiseSales objGroupCodeDtl = new clsGroupSubGroupWiseSales(
			    rsGroupWiseSales.getString(1), rsGroupWiseSales.getString(2), rsGroupWiseSales.getString(5), rsGroupWiseSales.getDouble(3), rsGroupWiseSales.getDouble(8), rsGroupWiseSales.getDouble(4), rsGroupWiseSales.getDouble(9), rsGroupWiseSales.getDouble(11));
		    mapGroupCodeDtl.put(rsGroupWiseSales.getString(1), objGroupCodeDtl);
		    listOfGroupDtl.add(mapGroupCodeDtl);
		    mapPOSDtlForGroupSubGroup.put(rsGroupWiseSales.getString(10), listOfGroupDtl);
		}

		if (mapWaiterWiseGroupSales.containsKey(waiterShortName))
		{
		    Map<String, clsGroupSubGroupWiseSales> mapGroupSales = mapWaiterWiseGroupSales.get(waiterShortName);
		    if (mapGroupSales.containsKey(groupName))
		    {
			clsGroupSubGroupWiseSales objGroupCodeDtl = mapGroupSales.get(groupName);

			objGroupCodeDtl.setSalesAmt(objGroupCodeDtl.getSalesAmt() + netTotal);

			mapGroupSales.put(groupName, objGroupCodeDtl);

			mapWaiterWiseGroupSales.put(waiterShortName, mapGroupSales);
		    }
		    else
		    {
			clsGroupSubGroupWiseSales objGroupCodeDtl = new clsGroupSubGroupWiseSales();
			objGroupCodeDtl.setStrWaiterShortName(waiterShortName);
			objGroupCodeDtl.setGroupName(groupName);
			objGroupCodeDtl.setSalesAmt(netTotal);

			mapGroupSales.put(groupName, objGroupCodeDtl);

			mapWaiterWiseGroupSales.put(waiterShortName, mapGroupSales);
		    }
		}
		else
		{
		    Map<String, clsGroupSubGroupWiseSales> mapGroupSales = new HashMap<>();

		    clsGroupSubGroupWiseSales objGroupCodeDtl = new clsGroupSubGroupWiseSales();
		    objGroupCodeDtl.setStrWaiterShortName(waiterShortName);
		    objGroupCodeDtl.setGroupName(groupName);
		    objGroupCodeDtl.setSalesAmt(netTotal);

		    mapGroupSales.put(groupName, objGroupCodeDtl);

		    mapWaiterWiseGroupSales.put(waiterShortName, mapGroupSales);
		}
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funCallItemWiseIncentiveReport(String reportType, HashMap hm, String dayEnd, String type)
    {
	try
	{

	    InputStream is = null;

	    is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptItemWiseIncentivesReport.jasper");

	    String fromDate = hm.get("fromDate").toString();
	    String toDate = hm.get("toDate").toString();
	    String posCode = hm.get("posCode").toString();
	    String shiftNo = hm.get("shiftNo").toString();
	    String posName = hm.get("posName").toString();
	    String groupCode = hm.get("groupCode").toString();
	    String subGroupCode = hm.get("subGroupCode").toString();

	    StringBuilder sqlBuilder = new StringBuilder();
	    List<clsBillDtl> listOfItemWiseIncentives = new ArrayList<>();
	    Map<String, clsBillDtl> mapItem = new HashMap<>();

	    String waiterShortName = " '' ";
	    if (!type.equalsIgnoreCase("Item Wise"))
	    {
		waiterShortName = " d.strWShortName ";
	    }
	    String waiterShortNo = " '' ";
	    if (!type.equalsIgnoreCase("Item Wise"))
	    {
		waiterShortNo = " d.strWaiterNo ";
	    }

	    //Live Data
	    sqlBuilder.setLength(0);
	    sqlBuilder.append("SELECT " + waiterShortName + ",b.strItemName,sum(b.dblAmount),c.dblIncentiveValue "
		    + " ,IF(c.strIncentiveType='Amt', (c.dblIncentiveValue)*sum(b.dblQuantity), (c.dblIncentiveValue/100)*sum(b.dblAmount)) as amount, "
		    + " e.strPosName,e.strPosCode,b.strItemCode," + waiterShortNo + ",c.strIncentiveType,sum(b.dblQuantity)  "
		    + " FROM tblbillhd a,tblbilldtl b,tblposwiseitemwiseincentives c ");
	    if (!type.equalsIgnoreCase("Item Wise"))
	    {
		sqlBuilder.append(",tblwaitermaster d ");
	    }
	    sqlBuilder.append(",tblposmaster e,tblitemmaster f,tblsubgrouphd g,tblgrouphd h "
		    + " where a.strBillNo=b.strBillNo "
		    + " and b.strItemCode=c.strItemCode ");
	    if (!type.equalsIgnoreCase("Item Wise"))
	    {
		sqlBuilder.append(" and b.strWaiterNo=d.strWaiterNo ");
	    }
	    sqlBuilder.append(" and a.strPOSCode=e.strPosCode "
		    + " and a.strPOSCode=c.strPOSCode "
		    + " and c.dblIncentiveValue>0 "
		    + " and b.strItemCode=f.strItemCode "
		    + " and f.strSubGroupCode=g.strSubGroupCode "
		    + " and g.strGroupCode=h.strGroupCode "
		    + " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    if (!posCode.equalsIgnoreCase("All"))
	    {
		sqlBuilder.append("and a.strPOSCode='" + posCode + "' ");
	    }
	    if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
	    {
		sqlBuilder.append("and a.intShiftCode='" + shiftNo + "' ");
	    }
	    if (!groupCode.equalsIgnoreCase("All"))
	    {
		sqlBuilder.append(" and h.strGroupCode='" + groupCode + "' ");
	    }
	    if (!subGroupCode.equalsIgnoreCase("All"))
	    {
		sqlBuilder.append(" and g.strSubGroupCode='" + subGroupCode + "' ");
	    }

	    sqlBuilder.append("and a.strBillNo not in (select u.strBillNo "
		    + " from tblbillhd v,tblbillsettlementdtl u,tblsettelmenthd w "
		    + " where v.strBillNo=u.strBillNo and u.strSettlementCode=w.strSettelmentCode "
		    + " and w.strSettelmentType='Complementary' and date(v.dteBillDate) between '" + fromDate + "' and '" + toDate + "')");
	    if (type.equalsIgnoreCase("Item Wise"))
	    {
		sqlBuilder.append(" group by b.strItemCode ");
		sqlBuilder.append(" order by b.strItemName ");
	    }
	    else if (type.equalsIgnoreCase("Summary"))
	    {
		sqlBuilder.append(" group by b.strWaiterNo");
		sqlBuilder.append(" order by d.strWShortName ");
	    }
	    else
	    {
		sqlBuilder.append(" group by b.strWaiterNo,c.strPOSCode,b.strItemCode ");
		sqlBuilder.append(" order by e.strPosName,d.strWShortName,b.strItemName ");
	    }
	    ResultSet rsWaiterWiseItemSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
	    while (rsWaiterWiseItemSales.next())
	    {
		String itemCode = rsWaiterWiseItemSales.getString(8);
		if (mapItem.containsKey(itemCode))
		{
		    clsBillDtl obj = mapItem.get(itemCode);

		    obj.setDblQuantity(obj.getDblQuantity() + rsWaiterWiseItemSales.getDouble(11));
		    obj.setDblAmount(obj.getDblAmount() + rsWaiterWiseItemSales.getDouble(3));

		    obj.setDblIncentive(obj.getDblIncentive() + rsWaiterWiseItemSales.getDouble(5));
		}
		else
		{
		    clsBillDtl obj = new clsBillDtl();

		    obj.setStrWShortName(rsWaiterWiseItemSales.getString(1));
		    obj.setStrItemName(rsWaiterWiseItemSales.getString(2));
		    obj.setStrItemCode(rsWaiterWiseItemSales.getString(8));
		    obj.setDblAmount(rsWaiterWiseItemSales.getDouble(3));
		    obj.setDblIncentivePer(rsWaiterWiseItemSales.getDouble(4));
		    obj.setDblIncentive(rsWaiterWiseItemSales.getDouble(5));
		    obj.setStrPosName(rsWaiterWiseItemSales.getString(6));
		    obj.setStrPOSCode(rsWaiterWiseItemSales.getString(7));
		    obj.setStrWaiterNo(rsWaiterWiseItemSales.getString(9));
		    obj.setStrRemarks(rsWaiterWiseItemSales.getString(10));
		    obj.setDblQuantity(rsWaiterWiseItemSales.getDouble(11));

		    mapItem.put(itemCode, obj);
		}
	    }
	    rsWaiterWiseItemSales.close();

	    //Q Data
	    sqlBuilder.setLength(0);
	    sqlBuilder.append("SELECT " + waiterShortName + ",b.strItemName,sum(b.dblAmount),c.dblIncentiveValue "
		    + " ,IF(c.strIncentiveType='Amt', (c.dblIncentiveValue)*sum(b.dblQuantity), (c.dblIncentiveValue/100)*sum(b.dblAmount)) as amount, "
		    + " e.strPosName,e.strPosCode,b.strItemCode," + waiterShortNo + ",c.strIncentiveType,sum(b.dblQuantity)  "
		    + " FROM tblqbillhd a,tblqbilldtl b,tblposwiseitemwiseincentives c ");
	    if (!type.equalsIgnoreCase("Item Wise"))
	    {
		sqlBuilder.append(",tblwaitermaster d ");
	    }
	    sqlBuilder.append(",tblposmaster e,tblitemmaster f,tblsubgrouphd g,tblgrouphd h "
		    + " where a.strBillNo=b.strBillNo "
		    + " and b.strItemCode=c.strItemCode ");
	    if (!type.equalsIgnoreCase("Item Wise"))
	    {
		sqlBuilder.append(" and b.strWaiterNo=d.strWaiterNo ");
	    }
	    sqlBuilder.append(" and a.strPOSCode=e.strPosCode "
		    + " and a.strPOSCode=c.strPOSCode "
		    + " and c.dblIncentiveValue>0 "
		    + " and b.strItemCode=f.strItemCode "
		    + " and f.strSubGroupCode=g.strSubGroupCode "
		    + " and g.strGroupCode=h.strGroupCode "
		    + " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    if (!posCode.equalsIgnoreCase("All"))
	    {
		sqlBuilder.append("and a.strPOSCode='" + posCode + "' ");
	    }
	    if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
	    {
		sqlBuilder.append("and a.intShiftCode='" + shiftNo + "' ");
	    }
	    if (!groupCode.equalsIgnoreCase("All"))
	    {
		sqlBuilder.append(" and h.strGroupCode='" + groupCode + "' ");
	    }
	    if (!subGroupCode.equalsIgnoreCase("All"))
	    {
		sqlBuilder.append(" and g.strSubGroupCode='" + subGroupCode + "' ");
	    }

	    sqlBuilder.append("and a.strBillNo not in (select u.strBillNo "
		    + " from tblqbillhd v,tblqbillsettlementdtl u,tblsettelmenthd w "
		    + " where v.strBillNo=u.strBillNo and u.strSettlementCode=w.strSettelmentCode "
		    + " and w.strSettelmentType='Complementary' and date(v.dteBillDate) between '" + fromDate + "' and '" + toDate + "')");
	    if (type.equalsIgnoreCase("Item Wise"))
	    {
		sqlBuilder.append(" group by b.strItemCode ");
		sqlBuilder.append(" order by b.strItemName ");
	    }
	    else if (type.equalsIgnoreCase("Summary"))
	    {
		sqlBuilder.append(" group by b.strWaiterNo");
		sqlBuilder.append(" order by d.strWShortName ");
	    }
	    else
	    {
		sqlBuilder.append(" group by b.strWaiterNo,c.strPOSCode,b.strItemCode ");
		sqlBuilder.append(" order by e.strPosName,d.strWShortName,b.strItemName ");
	    }
	    rsWaiterWiseItemSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
	    while (rsWaiterWiseItemSales.next())
	    {
		String itemCode = rsWaiterWiseItemSales.getString(8);
		if (mapItem.containsKey(itemCode))
		{
		    clsBillDtl obj = mapItem.get(itemCode);

		    obj.setDblQuantity(obj.getDblQuantity() + rsWaiterWiseItemSales.getDouble(11));
		    obj.setDblAmount(obj.getDblAmount() + rsWaiterWiseItemSales.getDouble(3));

		    obj.setDblIncentive(obj.getDblIncentive() + rsWaiterWiseItemSales.getDouble(5));
		}
		else
		{
		    clsBillDtl obj = new clsBillDtl();

		    obj.setStrWShortName(rsWaiterWiseItemSales.getString(1));
		    obj.setStrItemName(rsWaiterWiseItemSales.getString(2));
		    obj.setStrItemCode(rsWaiterWiseItemSales.getString(8));
		    obj.setDblAmount(rsWaiterWiseItemSales.getDouble(3));
		    obj.setDblIncentivePer(rsWaiterWiseItemSales.getDouble(4));
		    obj.setDblIncentive(rsWaiterWiseItemSales.getDouble(5));
		    obj.setStrPosName(rsWaiterWiseItemSales.getString(6));
		    obj.setStrPOSCode(rsWaiterWiseItemSales.getString(7));
		    obj.setStrWaiterNo(rsWaiterWiseItemSales.getString(9));
		    obj.setStrRemarks(rsWaiterWiseItemSales.getString(10));
		    obj.setDblQuantity(rsWaiterWiseItemSales.getDouble(11));

		    mapItem.put(itemCode, obj);
		}
	    }
	    rsWaiterWiseItemSales.close();

	    Comparator<clsBillDtl> itemNameComparator = new Comparator<clsBillDtl>()
	    {

		@Override
		public int compare(clsBillDtl o1, clsBillDtl o2)
		{
		    return o1.getStrItemName().compareTo(o2.getStrItemName());
		}
	    };

	    for (clsBillDtl objBillDtl : mapItem.values())
	    {
		listOfItemWiseIncentives.add(objBillDtl);
	    }

	    Collections.sort(listOfItemWiseIncentives, new clsWaiterWiseSalesComparator(itemNameComparator));

	    funViewJasperReportForBeanCollectionDataSource(is, hm, listOfItemWiseIncentives);

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

}
