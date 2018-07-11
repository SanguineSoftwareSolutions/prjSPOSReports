package com.POSReport.controller;

import com.POSReport.controller.comparator.clsDiscountComparator;
import com.POSGlobal.controller.clsBillDtl;
import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsGroupSubGroupWiseSales;
import com.POSGlobal.controller.clsPosConfigFile;
import com.POSReport.controller.comparator.clsBillComparator;
import com.POSReport.controller.comparator.clsGroupSubGroupComparator;
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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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

public class clsConsolidatedDiscountReport
{

    DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();

    public void funGenerateConsolidatedDsicountWiseReport(String reportType, HashMap hm, String dayEnd)
    {
	try
	{
	    String reportName = "";

	    String fromDate = hm.get("fromDate").toString();
	    String toDate = hm.get("toDate").toString();
	    String posCode = hm.get("posCode").toString();
	    String shiftNo = hm.get("shiftNo").toString();
	    String posName = hm.get("posName").toString();
	    String type = hm.get("rptType").toString();
	    String fromDateToDisplay = hm.get("fromDateToDisplay").toString();
	    String toDateToDisplay = hm.get("toDateToDisplay").toString();

	    Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
	    List<String> arrListTotal = new ArrayList<String>();
	    List<String> arrHeaderList = new ArrayList<String>();
	    double totalDis = 0, totalDiscValue = 0;
	    double totalAmount = 0, totalSalesNetTotal = 0.0;
	    double totalDisOnAmount = 0;
	    double totalConsolidatedDiscAmt = 0.0;

	    if (type.equalsIgnoreCase("Consolidated Discount"))
	    {
		List<clsBillItemDtlBean> listOfBillItemDtl = new ArrayList<>();
		StringBuilder sbSqlLiveDisc = new StringBuilder();
		StringBuilder sbSqlQFileDisc = new StringBuilder();
		StringBuilder sqlBuilder = new StringBuilder();
		Map<String, clsBillItemDtlBean> mapReasonDtl = new HashMap<>();

		sqlBuilder.setLength(0);
		sqlBuilder.append("SELECT sum(b.dblDiscountAmt)DiscAmt,g.strReasonCode,g.strReasonName "
			+ ",sum(b.dblAmount)-sum(b.dblDiscountAmt)NetRevenue,sum(a.intBillSeriesPaxNo) "
			+ "FROM tblqbillhd a,tblqbilldtl b,tblgrouphd c,tblsubgrouphd d,tblitemmaster e,tblposmaster f,tblreasonmaster g\n"
			+ "WHERE a.strBillNo=b.strBillNo \n"
			+ "AND DATE(a.dteBillDate)= DATE(b.dteBillDate) \n"
			+ "AND a.strPOSCode=f.strPOSCode \n"
			+ "AND a.strClientCode=b.strClientCode \n"
			+ "AND b.strItemCode=e.strItemCode \n"
			+ "AND d.strSubGroupCode=e.strSubGroupCode \n"
			+ "AND c.strGroupCode=d.strGroupCode\n"
			+ "and a.strReasonCode=g.strReasonCode\n"
			+ "AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' \n");
		if (!posCode.equalsIgnoreCase("All"))
		{
		    sqlBuilder.append(" and a.strPOSCode='" + posCode + "' ");
		}
		sqlBuilder.append("GROUP BY g.strReasonName;");
		ResultSet rsLiveDisc = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
		while (rsLiveDisc.next())
		{
		    clsBillItemDtlBean objBean = new clsBillItemDtlBean();

		    String reasonCode = rsLiveDisc.getString(2);
		    if (reasonCode != null)
		    {
			if (mapReasonDtl.containsKey(reasonCode))
			{
			    objBean = mapReasonDtl.get(reasonCode);
			    objBean.setDblDiscountAmt(objBean.getDblDiscountAmt() + rsLiveDisc.getDouble(1));
			    objBean.setDblNetTotal(objBean.getDblNetTotal() + rsLiveDisc.getDouble(4));
			    objBean.setIntBillSeriesPaxNo(objBean.getIntBillSeriesPaxNo() + rsLiveDisc.getInt(5));

			    mapReasonDtl.put(reasonCode, objBean);
			}
			else
			{
			    objBean = new clsBillItemDtlBean();
			    objBean.setStrReasonName(rsLiveDisc.getString(3));
			    objBean.setDblDiscountAmt(rsLiveDisc.getDouble(1));
			    objBean.setDblNetTotal(rsLiveDisc.getDouble(4));
			    objBean.setIntBillSeriesPaxNo(rsLiveDisc.getInt(5));

			    mapReasonDtl.put(reasonCode, objBean);
			}
		    }

		}
		rsLiveDisc.close();

		sqlBuilder.setLength(0);
		sqlBuilder.append("SELECT sum(b.dblDiscAmt)DiscAmt,g.strReasonCode,g.strReasonName "
			+ ",sum(b.dblAmount)-sum(b.dblDiscAmt)NetRevenue,0 "
			+ "FROM tblqbillhd a,tblqbillmodifierdtl b,tblgrouphd c,tblsubgrouphd d,tblitemmaster e,tblposmaster f,tblreasonmaster g\n"
			+ "WHERE a.strBillNo=b.strBillNo \n"
			+ "AND DATE(a.dteBillDate)= DATE(b.dteBillDate) \n"
			+ "AND a.strPOSCode=f.strPOSCode \n"
			+ "AND a.strClientCode=b.strClientCode \n"
			+ "AND left(b.strItemCode,7)=e.strItemCode \n"
			+ "AND d.strSubGroupCode=e.strSubGroupCode \n"
			+ "AND c.strGroupCode=d.strGroupCode\n"
			+ "and a.strReasonCode=g.strReasonCode\n"
			+ "AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' \n");
		if (!posCode.equalsIgnoreCase("All"))
		{
		    sqlBuilder.append(" and a.strPOSCode='" + posCode + "' ");
		}
		sqlBuilder.append("GROUP BY g.strReasonName;");

		ResultSet rsQfileDisc = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
		while (rsQfileDisc.next())
		{
		    clsBillItemDtlBean objBean = new clsBillItemDtlBean();

		    String reasonCode = rsQfileDisc.getString(2);
		    if (reasonCode != null)
		    {
			if (mapReasonDtl.containsKey(reasonCode))
			{
			    objBean = mapReasonDtl.get(reasonCode);
			    objBean.setDblDiscountAmt(objBean.getDblDiscountAmt() + rsQfileDisc.getDouble(1));
			    objBean.setDblNetTotal(objBean.getDblNetTotal() + rsQfileDisc.getDouble(4));
			    objBean.setIntBillSeriesPaxNo(objBean.getIntBillSeriesPaxNo() + rsQfileDisc.getInt(5));

			    mapReasonDtl.put(reasonCode, objBean);
			}
			else
			{
			    objBean = new clsBillItemDtlBean();
			    objBean.setStrReasonName(rsQfileDisc.getString(3));
			    objBean.setDblDiscountAmt(rsQfileDisc.getDouble(1));
			    objBean.setDblNetTotal(rsQfileDisc.getDouble(4));
			    objBean.setIntBillSeriesPaxNo(rsQfileDisc.getInt(5));

			    mapReasonDtl.put(reasonCode, objBean);
			}
		    }
		}
		rsQfileDisc.close();

		sqlBuilder.setLength(0);
		sqlBuilder.append("SELECT sum(b.dblDiscountAmt)DiscAmt,g.strReasonCode,g.strReasonName "
			+ ",sum(b.dblAmount)-sum(b.dblDiscountAmt)NetRevenue,sum(a.intBillSeriesPaxNo) "
			+ "FROM tblbillhd a,tblbilldtl b,tblgrouphd c,tblsubgrouphd d,tblitemmaster e,tblposmaster f,tblreasonmaster g\n"
			+ "WHERE a.strBillNo=b.strBillNo \n"
			+ "AND DATE(a.dteBillDate)= DATE(b.dteBillDate) \n"
			+ "AND a.strPOSCode=f.strPOSCode \n"
			+ "AND a.strClientCode=b.strClientCode \n"
			+ "AND b.strItemCode=e.strItemCode \n"
			+ "AND d.strSubGroupCode=e.strSubGroupCode \n"
			+ "AND c.strGroupCode=d.strGroupCode\n"
			+ "and a.strReasonCode=g.strReasonCode\n"
			+ "AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' \n");
		if (!posCode.equalsIgnoreCase("All"))
		{
		    sqlBuilder.append(" and a.strPOSCode='" + posCode + "' ");
		}
		sqlBuilder.append("GROUP BY g.strReasonName;");

		rsLiveDisc = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
		while (rsLiveDisc.next())
		{
		    clsBillItemDtlBean objBean = new clsBillItemDtlBean();

		    String reasonCode = rsLiveDisc.getString(2);
		    if (reasonCode != null)
		    {
			if (mapReasonDtl.containsKey(reasonCode))
			{
			    objBean = mapReasonDtl.get(reasonCode);
			    objBean.setDblDiscountAmt(objBean.getDblDiscountAmt() + rsLiveDisc.getDouble(1));
			    objBean.setDblNetTotal(objBean.getDblNetTotal() + rsLiveDisc.getDouble(4));
			    objBean.setIntBillSeriesPaxNo(objBean.getIntBillSeriesPaxNo() + rsLiveDisc.getInt(5));

			    mapReasonDtl.put(reasonCode, objBean);
			}
			else
			{
			    objBean = new clsBillItemDtlBean();
			    objBean.setStrReasonName(rsLiveDisc.getString(3));
			    objBean.setDblDiscountAmt(rsLiveDisc.getDouble(1));
			    objBean.setDblNetTotal(rsLiveDisc.getDouble(4));
			    objBean.setIntBillSeriesPaxNo(rsLiveDisc.getInt(5));

			    mapReasonDtl.put(reasonCode, objBean);
			}
		    }

		}
		rsLiveDisc.close();

		sqlBuilder.setLength(0);
		sqlBuilder.append("SELECT sum(b.dblDiscAmt)DiscAmt,g.strReasonCode,g.strReasonName "
			+ ",sum(b.dblAmount)-sum(b.dblDiscAmt)NetRevenue,0 "
			+ "FROM tblbillhd a,tblbillmodifierdtl b,tblgrouphd c,tblsubgrouphd d,tblitemmaster e,tblposmaster f,tblreasonmaster g\n"
			+ "WHERE a.strBillNo=b.strBillNo \n"
			+ "AND DATE(a.dteBillDate)= DATE(b.dteBillDate) \n"
			+ "AND a.strPOSCode=f.strPOSCode \n"
			+ "AND a.strClientCode=b.strClientCode \n"
			+ "AND left(b.strItemCode,7)=e.strItemCode \n"
			+ "AND d.strSubGroupCode=e.strSubGroupCode \n"
			+ "AND c.strGroupCode=d.strGroupCode\n"
			+ "and a.strReasonCode=g.strReasonCode\n"
			+ "AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' \n");
		if (!posCode.equalsIgnoreCase("All"))
		{
		    sqlBuilder.append(" and a.strPOSCode='" + posCode + "' ");
		}
		sqlBuilder.append("GROUP BY g.strReasonName;");

		rsQfileDisc = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
		while (rsQfileDisc.next())
		{
		    clsBillItemDtlBean objBean = new clsBillItemDtlBean();

		    String reasonCode = rsQfileDisc.getString(2);
		    if (reasonCode != null)
		    {
			if (mapReasonDtl.containsKey(reasonCode))
			{
			    objBean = mapReasonDtl.get(reasonCode);
			    objBean.setDblDiscountAmt(objBean.getDblDiscountAmt() + rsQfileDisc.getDouble(1));
			    objBean.setDblNetTotal(objBean.getDblNetTotal() + rsQfileDisc.getDouble(4));
			    objBean.setIntBillSeriesPaxNo(objBean.getIntBillSeriesPaxNo() + rsQfileDisc.getInt(5));

			    mapReasonDtl.put(reasonCode, objBean);
			}
			else
			{
			    objBean = new clsBillItemDtlBean();
			    objBean.setStrReasonName(rsQfileDisc.getString(3));
			    objBean.setDblDiscountAmt(rsQfileDisc.getDouble(1));
			    objBean.setDblNetTotal(rsQfileDisc.getDouble(4));
			    objBean.setIntBillSeriesPaxNo(rsQfileDisc.getInt(5));

			    mapReasonDtl.put(reasonCode, objBean);
			}
		    }
		}
		rsQfileDisc.close();

		List<clsBillItemDtlBean> listOfDiscount = new ArrayList<clsBillItemDtlBean>();
		listOfDiscount.addAll(mapReasonDtl.values());

		//for complimentary 
		mapReasonDtl = new HashMap<>();
		StringBuilder sbSqlLive = new StringBuilder();
		StringBuilder sbSqlQBill = new StringBuilder();
		StringBuilder sqlLiveModifierBuilder = new StringBuilder();
		StringBuilder sqlQModifierBuilder = new StringBuilder();
		sbSqlLive.setLength(0);
		sbSqlQBill.setLength(0);
		sqlLiveModifierBuilder.setLength(0);
		sqlQModifierBuilder.setLength(0);

		//live data
		sbSqlLive.append("select sum(b.dblRate* b.dblQuantity) AS Disc,a.strReasonCode,i.strReasonName "
			+ ",SUM(b.dblRate* b.dblQuantity) AS NetRevenue,sum(a.intBillSeriesPaxNo)Pax "
			+ " FROM tblbillhd a,tblbillcomplementrydtl b,tblposmaster e,tblitemmaster f,tblsubgrouphd g,tblgrouphd h,tblreasonmaster i "
			+ " WHERE a.strBillNo = b.strBillNo  "
			+ " AND DATE(a.dteBillDate) =date(b.dteBillDate)  "
			+ " AND a.strPOSCode=e.strPosCode  "
			+ " AND b.strItemCode=f.strItemCode  "
			+ " AND f.strSubGroupCode=g.strSubGroupCode and i.strReasonCode=a.strReasonCode "
			+ " AND g.strGroupCode=h.strGroupCode  "
		);

		//Q data
		sbSqlQBill.append("select sum(b.dblRate* b.dblQuantity) AS Disc,a.strReasonCode,i.strReasonName "
			+ ",SUM(b.dblRate* b.dblQuantity) AS NetRevenue,sum(a.intBillSeriesPaxNo)Pax "
			+ " FROM tblqbillhd a,tblqbillcomplementrydtl b,tblposmaster e,tblitemmaster f,tblsubgrouphd g,tblgrouphd h,tblreasonmaster i "
			+ " WHERE a.strBillNo = b.strBillNo  "
			+ " AND DATE(a.dteBillDate) =date(b.dteBillDate)  "
			+ " AND a.strPOSCode=e.strPosCode  "
			+ " AND b.strItemCode=f.strItemCode  "
			+ " AND f.strSubGroupCode=g.strSubGroupCode and i.strReasonCode=a.strReasonCode "
			+ " AND g.strGroupCode=h.strGroupCode  ");

		if (!posCode.equals("All"))
		{
		    sbSqlLive.append(" AND a.strPOSCode = '" + posCode + "' ");
		    sbSqlQBill.append(" AND a.strPOSCode = '" + posCode + "' ");
		    sqlLiveModifierBuilder.append(" AND a.strPOSCode = '" + posCode + "' ");
		    sqlQModifierBuilder.append(" AND a.strPOSCode = '" + posCode + "' ");
		}

		if (clsGlobalVarClass.gEnableShiftYN)
		{
		    if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
		    {
			sbSqlLive.append(" and a.intShiftCode = '" + shiftNo + "' ");
			sbSqlQBill.append(" and a.intShiftCode = '" + shiftNo + "' ");
			sqlLiveModifierBuilder.append(" and a.intShiftCode = '" + shiftNo + "' ");
			sqlQModifierBuilder.append(" and a.intShiftCode = '" + shiftNo + "' ");
		    }
		}
		sbSqlLive.append(" and date(a.dteBillDate) Between '" + fromDate + "' and '" + toDate + "' "
			+ " group by a.strReasonCode"
			+ " order by a.strReasonCode;");
		sbSqlQBill.append(" and date(a.dteBillDate) Between '" + fromDate + "' and '" + toDate + "' "
			+ " group by a.strReasonCode"
			+ " order by a.strReasonCode;");
		sqlLiveModifierBuilder.append(" and date(a.dteBillDate) Between '" + fromDate + "' and '" + toDate + "' "
			+ " group by a.strReasonCode"
			+ " order by a.strReasonCode;");
		sqlQModifierBuilder.append(" and date(a.dteBillDate) Between '" + fromDate + "' and '" + toDate + "' "
			+ " group by a.strReasonCode"
			+ " order by a.strReasonCode;");

		ResultSet rsLiveCompl = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
		while (rsLiveCompl.next())
		{
		    clsBillItemDtlBean objBean = new clsBillItemDtlBean();

		    String reasonCode = rsLiveCompl.getString(2);

		    if (mapReasonDtl.containsKey(reasonCode))
		    {
			objBean = mapReasonDtl.get(reasonCode);

			objBean.setDblDiscountAmt(objBean.getDblDiscountAmt() + rsLiveCompl.getDouble(1));
			objBean.setDblNetTotal(objBean.getDblNetTotal() + rsLiveCompl.getDouble(4));
			objBean.setIntBillSeriesPaxNo(objBean.getIntBillSeriesPaxNo() + rsLiveCompl.getInt(5));

			mapReasonDtl.put(reasonCode, objBean);
		    }
		    else
		    {
			objBean = new clsBillItemDtlBean();
			objBean.setStrReasonName(rsLiveCompl.getString(3));
			objBean.setDblDiscountAmt(rsLiveCompl.getDouble(1));
			objBean.setDblNetTotal(rsLiveCompl.getDouble(4));
			objBean.setIntBillSeriesPaxNo(rsLiveCompl.getInt(5));

			mapReasonDtl.put(reasonCode, objBean);
		    }
		}
		rsLiveCompl.close();

		ResultSet rsQfileCompl = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQBill.toString());
		while (rsQfileCompl.next())
		{
		    clsBillItemDtlBean objBean = new clsBillItemDtlBean();

		    String reasonCode = rsQfileCompl.getString(2);

		    if (mapReasonDtl.containsKey(reasonCode))
		    {
			objBean = mapReasonDtl.get(reasonCode);

			objBean.setDblDiscountAmt(objBean.getDblDiscountAmt() + rsQfileCompl.getDouble(1));
			objBean.setDblNetTotal(objBean.getDblNetTotal() + rsQfileCompl.getDouble(4));
			objBean.setIntBillSeriesPaxNo(objBean.getIntBillSeriesPaxNo() + rsQfileCompl.getInt(5));

			mapReasonDtl.put(reasonCode, objBean);
		    }
		    else
		    {
			objBean = new clsBillItemDtlBean();
			objBean.setStrReasonName(rsQfileCompl.getString(3));
			objBean.setDblDiscountAmt(rsQfileCompl.getDouble(1));
			objBean.setDblNetTotal(rsQfileCompl.getDouble(4));
			objBean.setIntBillSeriesPaxNo(rsQfileCompl.getInt(5));

			mapReasonDtl.put(reasonCode, objBean);
		    }
		}
		rsQfileCompl.close();

		List<clsBillItemDtlBean> listOfComplimentaryDiscount = new ArrayList<clsBillItemDtlBean>();
		listOfComplimentaryDiscount.addAll(mapReasonDtl.values());

		//for promotion
		mapReasonDtl = new HashMap<>();
		StringBuilder sqlLiveData = new StringBuilder();
		StringBuilder sqlQData = new StringBuilder();

		sqlLiveData.append("SELECT sum(a.dblQuantity*a.dblRate) AS Disc,a.strPromotionCode,c.strPromoName "
			+ ",sum(a.dblQuantity*a.dblRate) AS NetRevenue,sum(b.intBillSeriesPaxNo)Pax "
			+ "FROM tblbillpromotiondtl a,tblbillhd b,tblpromotionmaster c\n"
			+ "WHERE a.strBillNo=b.strBillNo and a.strPromotionCode=c.strPromoCode \n"
			+ "AND DATE(b.dteBillDate) BETWEEN '" + fromDate + "' and '" + toDate + "' ");

		sqlQData.append("SELECT sum(a.dblQuantity*a.dblRate) AS Disc,a.strPromotionCode,c.strPromoName "
			+ ",sum(a.dblQuantity*a.dblRate) AS NetRevenue,sum(b.intBillSeriesPaxNo)Pax "
			+ "FROM tblqbillpromotiondtl a,tblqbillhd b,tblpromotionmaster c\n"
			+ "WHERE a.strBillNo=b.strBillNo and a.strPromotionCode=c.strPromoCode \n"
			+ "AND DATE(b.dteBillDate) BETWEEN '" + fromDate + "' and '" + toDate + "' ");
		if (!posCode.equalsIgnoreCase("All"))
		{
		    sqlLiveData.append(" and b.strPOSCode='" + posCode + "' ");
		    sqlQData.append(" and b.strPOSCode='" + posCode + "' ");
		}

		if (clsGlobalVarClass.gEnableShiftYN)
		{
		    if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
		    {
			sqlLiveData.append(" and a.intShiftCode = '" + shiftNo + "' ");
			sqlQData.append(" and a.intShiftCode = '" + shiftNo + "' ");
		    }
		}
		sqlLiveData.append(" group by a.strPromotionCode ");
		sqlQData.append(" group by a.strPromotionCode ");

		ResultSet rsLivePromotion = clsGlobalVarClass.dbMysql.executeResultSet(sqlLiveData.toString());
		while (rsLivePromotion.next())
		{
		    clsBillItemDtlBean objBean = new clsBillItemDtlBean();

		    String reasonCode = rsLivePromotion.getString(2);

		    if (mapReasonDtl.containsKey(reasonCode))
		    {
			objBean = mapReasonDtl.get(reasonCode);

			objBean.setDblDiscountAmt(objBean.getDblDiscountAmt() + rsLivePromotion.getDouble(1));
			objBean.setDblNetTotal(objBean.getDblNetTotal() + rsLivePromotion.getDouble(4));
			objBean.setIntBillSeriesPaxNo(objBean.getIntBillSeriesPaxNo() + rsLivePromotion.getInt(5));

			mapReasonDtl.put(reasonCode, objBean);
		    }
		    else
		    {
			objBean = new clsBillItemDtlBean();
			objBean.setStrReasonName(rsLivePromotion.getString(3));
			objBean.setDblDiscountAmt(rsLivePromotion.getDouble(1));
			objBean.setDblNetTotal(rsLivePromotion.getDouble(4));
			objBean.setIntBillSeriesPaxNo(rsLivePromotion.getInt(5));

			mapReasonDtl.put(reasonCode, objBean);
		    }
		}
		rsLivePromotion.close();

		ResultSet rsQFilePromotion = clsGlobalVarClass.dbMysql.executeResultSet(sqlQData.toString());
		while (rsQFilePromotion.next())
		{
		    clsBillItemDtlBean objBean = new clsBillItemDtlBean();

		    String reasonCode = rsQFilePromotion.getString(2);

		    if (mapReasonDtl.containsKey(reasonCode))
		    {
			objBean = mapReasonDtl.get(reasonCode);

			objBean.setDblDiscountAmt(objBean.getDblDiscountAmt() + rsQFilePromotion.getDouble(1));
			objBean.setDblNetTotal(objBean.getDblNetTotal() + rsQFilePromotion.getDouble(4));
			objBean.setIntBillSeriesPaxNo(objBean.getIntBillSeriesPaxNo() + rsQFilePromotion.getInt(5));

			mapReasonDtl.put(reasonCode, objBean);
		    }
		    else
		    {
			objBean = new clsBillItemDtlBean();
			objBean.setStrReasonName(rsQFilePromotion.getString(3));
			objBean.setDblDiscountAmt(rsQFilePromotion.getDouble(1));
			objBean.setDblNetTotal(rsQFilePromotion.getDouble(4));
			objBean.setIntBillSeriesPaxNo(rsQFilePromotion.getInt(5));

			mapReasonDtl.put(reasonCode, objBean);
		    }
		}
		rsQFilePromotion.close();
		List<clsBillItemDtlBean> listOfPromotionDiscount = new ArrayList<clsBillItemDtlBean>();
		listOfPromotionDiscount.addAll(mapReasonDtl.values());

		//group wise sales complimentary
		sbSqlLive.setLength(0);
		sbSqlQBill.setLength(0);

		//live data
		sbSqlLive.append("SELECT h.strGroupCode,h.strGroupName, SUM(b.dblRate* b.dblQuantity) AS Disc\n"
			+ ", SUM(b.dblRate* b.dblQuantity) AS NetRevenue,sum(a.intBillSeriesPaxNo)Pax "
			+ " FROM tblbillhd a,tblbillcomplementrydtl b,tblposmaster e,tblitemmaster f,tblsubgrouphd g,tblgrouphd h\n"
			+ " WHERE a.strBillNo = b.strBillNo AND DATE(a.dteBillDate) = DATE(b.dteBillDate) AND a.strPOSCode=e.strPosCode "
			+ " AND b.strItemCode=f.strItemCode AND f.strSubGroupCode=g.strSubGroupCode "
			+ " AND g.strGroupCode=h.strGroupCode ");

		//Q data
		sbSqlQBill.append("SELECT h.strGroupCode,h.strGroupName, SUM(b.dblRate* b.dblQuantity) AS Disc\n"
			+ ", SUM(b.dblRate* b.dblQuantity) AS NetRevenue,sum(a.intBillSeriesPaxNo)Pax "
			+ " FROM tblqbillhd a,tblqbillcomplementrydtl b,tblposmaster e,tblitemmaster f,tblsubgrouphd g,tblgrouphd h\n"
			+ " WHERE a.strBillNo = b.strBillNo AND DATE(a.dteBillDate) = DATE(b.dteBillDate) AND a.strPOSCode=e.strPosCode "
			+ " AND b.strItemCode=f.strItemCode AND f.strSubGroupCode=g.strSubGroupCode "
			+ " AND g.strGroupCode=h.strGroupCode ");
		if (!posCode.equals("All"))
		{
		    sbSqlLive.append(" AND a.strPOSCode = '" + posCode + "' ");
		    sbSqlQBill.append(" AND a.strPOSCode = '" + posCode + "' ");

		}

		if (clsGlobalVarClass.gEnableShiftYN)
		{
		    if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
		    {
			sbSqlLive.append(" and a.intShiftCode = '" + shiftNo + "' ");
			sbSqlQBill.append(" and a.intShiftCode = '" + shiftNo + "' ");

		    }
		}
		sbSqlLive.append(" and date(a.dteBillDate) Between '" + fromDate + "' and '" + toDate + "' "
			+ " group by h.strGroupCode"
			+ " order by h.strGroupCode;");
		sbSqlQBill.append(" and date(a.dteBillDate) Between '" + fromDate + "' and '" + toDate + "' "
			+ " group by h.strGroupCode"
			+ " order by h.strGroupCode;");

		mapReasonDtl = new HashMap<>();
		ResultSet rsLiveGroup = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
		while (rsLiveGroup.next())
		{
		    
		      totalConsolidatedDiscAmt+=rsLiveGroup.getDouble(3);
		      
		    clsBillItemDtlBean objBean = new clsBillItemDtlBean();

		    String groupCode = rsLiveGroup.getString(1);

		    if (mapReasonDtl.containsKey(groupCode))
		    {
			objBean = mapReasonDtl.get(groupCode);

			objBean.setDblDiscountAmt(objBean.getDblDiscountAmt() + rsLiveGroup.getDouble(3));
			objBean.setDblNetTotal(objBean.getDblNetTotal() + rsLiveGroup.getDouble(4));
			objBean.setIntBillSeriesPaxNo(objBean.getIntBillSeriesPaxNo() + rsLiveGroup.getInt(5));

			mapReasonDtl.put(groupCode, objBean);
		    }
		    else
		    {
			objBean = new clsBillItemDtlBean();
			objBean.setStrReasonName(rsLiveGroup.getString(2));
			objBean.setDblDiscountAmt(rsLiveGroup.getDouble(3));
			objBean.setDblNetTotal(rsLiveGroup.getDouble(4));
			objBean.setIntBillSeriesPaxNo(rsLiveGroup.getInt(5));

			mapReasonDtl.put(groupCode, objBean);
		    }
		}
		rsLiveGroup.close();

		ResultSet rsQFileGroup = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQBill.toString());
		while (rsQFileGroup.next())
		{
		    totalConsolidatedDiscAmt+=rsQFileGroup.getDouble(3);
		    
		    clsBillItemDtlBean objBean = new clsBillItemDtlBean();

		    String groupCode = rsQFileGroup.getString(1);

		    if (mapReasonDtl.containsKey(groupCode))
		    {
			objBean = mapReasonDtl.get(groupCode);

			objBean.setDblDiscountAmt(objBean.getDblDiscountAmt() + rsQFileGroup.getDouble(3));
			objBean.setDblNetTotal(objBean.getDblNetTotal() + rsQFileGroup.getDouble(4));
			objBean.setIntBillSeriesPaxNo(objBean.getIntBillSeriesPaxNo() + rsQFileGroup.getInt(5));

			mapReasonDtl.put(groupCode, objBean);
		    }
		    else
		    {
			objBean = new clsBillItemDtlBean();
			objBean.setStrReasonName(rsQFileGroup.getString(2));
			objBean.setDblDiscountAmt(rsQFileGroup.getDouble(3));
			objBean.setDblNetTotal(rsQFileGroup.getDouble(4));
			objBean.setIntBillSeriesPaxNo(rsQFileGroup.getInt(5));

			mapReasonDtl.put(groupCode, objBean);
		    }
		}
		rsQFileGroup.close();

		

		//group wise sales promotions
		sbSqlLive.setLength(0);
		sbSqlQBill.setLength(0);
		sqlLiveModifierBuilder.setLength(0);
		sqlQModifierBuilder.setLength(0);
		sbSqlLive.append("SELECT e.strGroupCode,e.strGroupName,sum(a.dblQuantity*a.dblRate) AS Disc\n"
			+ ",sum(a.dblQuantity*a.dblRate) AS NetRevenue,sum(b.intBillSeriesPaxNo)Pax "
			+ " FROM tblbillpromotiondtl a,tblbillhd b,tblpromotionmaster c,tblitemmaster d,tblgrouphd e,tblsubgrouphd f\n"
			+ " WHERE a.strBillNo=b.strBillNo and a.strPromotionCode=c.strPromoCode \n"
			+ " and a.strItemCode=d.strItemCode and d.strSubGroupCode=f.strSubGroupCode and f.strGroupCode=e.strGroupCode\n"
			+ " AND DATE(b.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "'\n");

		sbSqlQBill.append("SELECT e.strGroupCode,e.strGroupName,sum(a.dblQuantity*a.dblRate) AS Disc\n"
			+ ",sum(a.dblQuantity*a.dblRate) AS NetRevenue,sum(b.intBillSeriesPaxNo)Pax "
			+ " FROM tblqbillpromotiondtl a,tblqbillhd b,tblpromotionmaster c,tblitemmaster d,tblgrouphd e,tblsubgrouphd f\n"
			+ " WHERE a.strBillNo=b.strBillNo and a.strPromotionCode=c.strPromoCode \n"
			+ " and a.strItemCode=d.strItemCode and d.strSubGroupCode=f.strSubGroupCode and f.strGroupCode=e.strGroupCode\n"
			+ " AND DATE(b.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "'\n");

		if (!posCode.equalsIgnoreCase("All"))
		{
		    sbSqlLive.append(" and a.strPOSCode='" + posCode + "' ");
		    sbSqlQBill.append(" and a.strPOSCode='" + posCode + "' ");

		}

		if (clsGlobalVarClass.gEnableShiftYN)
		{
		    if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
		    {
			sbSqlLive.append(" and a.intShiftCode = '" + shiftNo + "' ");
			sbSqlQBill.append(" and a.intShiftCode = '" + shiftNo + "' ");

		    }
		}
		sbSqlLive.append(" GROUP BY e.strGroupCode, e.strGroupName");
		sbSqlQBill.append(" GROUP BY e.strGroupCode, e.strGroupName");

		rsLiveGroup = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
		while (rsLiveGroup.next())
		{
		    
		    totalConsolidatedDiscAmt+=rsLiveGroup.getDouble(3);
		    
		    clsBillItemDtlBean objBean = new clsBillItemDtlBean();

		    String groupCode = rsLiveGroup.getString(1);

		    if (mapReasonDtl.containsKey(groupCode))
		    {
			objBean = mapReasonDtl.get(groupCode);

			objBean.setDblDiscountAmt(objBean.getDblDiscountAmt() + rsLiveGroup.getDouble(3));
			objBean.setDblNetTotal(objBean.getDblNetTotal() + rsLiveGroup.getDouble(4));
			objBean.setIntBillSeriesPaxNo(objBean.getIntBillSeriesPaxNo() + rsLiveGroup.getInt(5));

			mapReasonDtl.put(groupCode, objBean);
		    }
		    else
		    {
			objBean = new clsBillItemDtlBean();
			objBean.setStrReasonName(rsLiveGroup.getString(2));
			objBean.setDblDiscountAmt(rsLiveGroup.getDouble(3));
			objBean.setDblNetTotal(rsLiveGroup.getDouble(4));
			objBean.setIntBillSeriesPaxNo(rsLiveGroup.getInt(5));

			mapReasonDtl.put(groupCode, objBean);
		    }
		}
		rsLiveGroup.close();

		rsQFileGroup = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQBill.toString());
		while (rsQFileGroup.next())
		{
		    
		    totalConsolidatedDiscAmt += rsQFileGroup.getDouble(3);
		    
		    
		    clsBillItemDtlBean objBean = new clsBillItemDtlBean();

		    String groupCode = rsQFileGroup.getString(1);

		    if (mapReasonDtl.containsKey(groupCode))
		    {
			objBean = mapReasonDtl.get(groupCode);

			objBean.setDblDiscountAmt(objBean.getDblDiscountAmt() + rsQFileGroup.getDouble(3));
			objBean.setDblNetTotal(objBean.getDblNetTotal() + rsQFileGroup.getDouble(4));
			objBean.setIntBillSeriesPaxNo(objBean.getIntBillSeriesPaxNo() + rsQFileGroup.getInt(5));

			mapReasonDtl.put(groupCode, objBean);
		    }
		    else
		    {
			objBean = new clsBillItemDtlBean();
			objBean.setStrReasonName(rsQFileGroup.getString(2));
			objBean.setDblDiscountAmt(rsQFileGroup.getDouble(3));
			objBean.setDblNetTotal(rsQFileGroup.getDouble(4));
			objBean.setIntBillSeriesPaxNo(rsQFileGroup.getInt(5));

			mapReasonDtl.put(groupCode, objBean);
		    }
		}
		rsQFileGroup.close();

		//group wise sales data
		sbSqlLive.setLength(0);
		sbSqlQBill.setLength(0);
		sqlLiveModifierBuilder.setLength(0);
		sqlQModifierBuilder.setLength(0);
		sbSqlLive.append("SELECT c.strGroupCode,c.strGroupName,SUM(b.dblDiscountAmt)Disc\n"
			+ ",sum(b.dblAmount)-sum(b.dblDiscountAmt)NetRevenue,sum(a.intBillSeriesPaxNo)Pax "
			+ " FROM tblbillhd a,tblbilldtl b,tblgrouphd c,tblsubgrouphd d,tblitemmaster e,tblposmaster f\n"
			+ " WHERE a.strBillNo=b.strBillNo AND DATE(a.dteBillDate)= DATE(b.dteBillDate) \n"
			+ " AND a.strPOSCode=f.strPOSCode AND a.strClientCode=b.strClientCode \n"
			+ " AND b.strItemCode=e.strItemCode AND d.strSubGroupCode=e.strSubGroupCode AND c.strGroupCode=d.strGroupCode\n"
			+ " AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "'\n");
		sbSqlQBill.append("SELECT c.strGroupCode,c.strGroupName,SUM(b.dblDiscountAmt)Disc\n"
			+ ",sum(b.dblAmount)-sum(b.dblDiscountAmt)NetRevenue,sum(a.intBillSeriesPaxNo)Pax "
			+ " FROM tblqbillhd a,tblqbilldtl b,tblgrouphd c,tblsubgrouphd d,tblitemmaster e,tblposmaster f\n"
			+ " WHERE a.strBillNo=b.strBillNo AND DATE(a.dteBillDate)= DATE(b.dteBillDate) \n"
			+ " AND a.strPOSCode=f.strPOSCode AND a.strClientCode=b.strClientCode \n"
			+ " AND b.strItemCode=e.strItemCode AND d.strSubGroupCode=e.strSubGroupCode AND c.strGroupCode=d.strGroupCode\n"
			+ " AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "'");

		if (!posCode.equalsIgnoreCase("All"))
		{
		    sbSqlLive.append(" and a.strPOSCode='" + posCode + "' ");
		    sbSqlQBill.append(" and a.strPOSCode='" + posCode + "' ");

		}

		if (clsGlobalVarClass.gEnableShiftYN)
		{
		    if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
		    {
			sbSqlLive.append(" and a.intShiftCode = '" + shiftNo + "' ");
			sbSqlQBill.append(" and a.intShiftCode = '" + shiftNo + "' ");

		    }
		}
		sbSqlLive.append(" GROUP BY c.strGroupCode, c.strGroupName");
		sbSqlQBill.append(" GROUP BY c.strGroupCode, c.strGroupName");

		rsLiveGroup = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
		while (rsLiveGroup.next())
		{

		    totalSalesNetTotal += rsLiveGroup.getDouble(4);
		    totalConsolidatedDiscAmt+=rsLiveGroup.getDouble(3);

		    clsBillItemDtlBean objBean = new clsBillItemDtlBean();

		    String groupCode = rsLiveGroup.getString(1);

		    if (mapReasonDtl.containsKey(groupCode))
		    {
			objBean = mapReasonDtl.get(groupCode);

			objBean.setDblDiscountAmt(objBean.getDblDiscountAmt() + rsLiveGroup.getDouble(3));
			objBean.setDblNetTotal(objBean.getDblNetTotal() + rsLiveGroup.getDouble(4));
			objBean.setIntBillSeriesPaxNo(objBean.getIntBillSeriesPaxNo() + rsLiveGroup.getInt(5));

			mapReasonDtl.put(groupCode, objBean);
		    }
		    else
		    {
			objBean = new clsBillItemDtlBean();
			objBean.setStrReasonName(rsLiveGroup.getString(2));
			objBean.setDblDiscountAmt(rsLiveGroup.getDouble(3));
			objBean.setDblNetTotal(rsLiveGroup.getDouble(4));
			objBean.setIntBillSeriesPaxNo(rsLiveGroup.getInt(5));

			mapReasonDtl.put(groupCode, objBean);
		    }
		}
		rsLiveGroup.close();

		rsQFileGroup = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQBill.toString());
		while (rsQFileGroup.next())
		{

		    totalSalesNetTotal += rsQFileGroup.getDouble(4);
		    totalConsolidatedDiscAmt+=rsQFileGroup.getDouble(3);

		    clsBillItemDtlBean objBean = new clsBillItemDtlBean();

		    String groupCode = rsQFileGroup.getString(1);

		    if (mapReasonDtl.containsKey(groupCode))
		    {
			objBean = mapReasonDtl.get(groupCode);

			objBean.setDblDiscountAmt(objBean.getDblDiscountAmt() + rsQFileGroup.getDouble(3));
			objBean.setDblNetTotal(objBean.getDblNetTotal() + rsQFileGroup.getDouble(4));
			objBean.setIntBillSeriesPaxNo(objBean.getIntBillSeriesPaxNo() + rsQFileGroup.getInt(5));

			mapReasonDtl.put(groupCode, objBean);
		    }
		    else
		    {
			objBean = new clsBillItemDtlBean();
			objBean.setStrReasonName(rsQFileGroup.getString(2));
			objBean.setDblDiscountAmt(rsQFileGroup.getDouble(3));
			objBean.setDblNetTotal(rsQFileGroup.getDouble(4));
			objBean.setIntBillSeriesPaxNo(rsQFileGroup.getInt(5));

			mapReasonDtl.put(groupCode, objBean);
		    }
		}
		rsQFileGroup.close();

		//group wise sales modifiers
		sbSqlLive.setLength(0);
		sbSqlQBill.setLength(0);
		sqlLiveModifierBuilder.setLength(0);
		sqlQModifierBuilder.setLength(0);
		sbSqlLive.append("SELECT c.strGroupCode,c.strGroupName,SUM(b.dblDiscAmt)Disc\n"
			+ ",sum(b.dblAmount)-sum(b.dblDiscAmt)NetRevenue,0 Pax\n"
			+ " FROM tblbillhd a,tblbillmodifierdtl b,tblgrouphd c,tblsubgrouphd d,tblitemmaster e,tblposmaster f\n"
			+ " WHERE a.strBillNo=b.strBillNo \n"
			+ " AND DATE(a.dteBillDate)= DATE(b.dteBillDate) \n"
			+ " AND a.strPOSCode=f.strPOSCode \n"
			+ " AND a.strClientCode=b.strClientCode \n"
			+ " AND left(b.strItemCode,7)=e.strItemCode \n"
			+ " AND d.strSubGroupCode=e.strSubGroupCode \n"
			+ " AND c.strGroupCode=d.strGroupCode "
			+ " AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "'\n");
		sbSqlQBill.append("SELECT c.strGroupCode,c.strGroupName,SUM(b.dblDiscAmt)Disc\n"
			+ ",sum(b.dblAmount)-sum(b.dblDiscAmt)NetRevenue,0 Pax\n"
			+ " FROM tblqbillhd a,tblqbillmodifierdtl b,tblgrouphd c,tblsubgrouphd d,tblitemmaster e,tblposmaster f\n"
			+ " WHERE a.strBillNo=b.strBillNo \n"
			+ " AND DATE(a.dteBillDate)= DATE(b.dteBillDate) \n"
			+ " AND a.strPOSCode=f.strPOSCode \n"
			+ " AND a.strClientCode=b.strClientCode \n"
			+ " AND left(b.strItemCode,7)=e.strItemCode \n"
			+ " AND d.strSubGroupCode=e.strSubGroupCode \n"
			+ " AND c.strGroupCode=d.strGroupCode "
			+ " AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "'\n");

		if (!posCode.equalsIgnoreCase("All"))
		{
		    sbSqlLive.append(" and a.strPOSCode='" + posCode + "' ");
		    sbSqlQBill.append(" and a.strPOSCode='" + posCode + "' ");

		}

		if (clsGlobalVarClass.gEnableShiftYN)
		{
		    if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
		    {
			sbSqlLive.append(" and a.intShiftCode = '" + shiftNo + "' ");
			sbSqlQBill.append(" and a.intShiftCode = '" + shiftNo + "' ");

		    }
		}
		sbSqlLive.append(" GROUP BY c.strGroupCode, c.strGroupName");
		sbSqlQBill.append(" GROUP BY c.strGroupCode, c.strGroupName");

		rsLiveGroup = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
		while (rsLiveGroup.next())
		{
		    totalSalesNetTotal += rsLiveGroup.getDouble(4);
		     totalConsolidatedDiscAmt+=rsLiveGroup.getDouble(3);

		    clsBillItemDtlBean objBean = new clsBillItemDtlBean();

		    String groupCode = rsLiveGroup.getString(1);

		    if (mapReasonDtl.containsKey(groupCode))
		    {
			objBean = mapReasonDtl.get(groupCode);

			objBean.setDblDiscountAmt(objBean.getDblDiscountAmt() + rsLiveGroup.getDouble(3));
			objBean.setDblNetTotal(objBean.getDblNetTotal() + rsLiveGroup.getDouble(4));
			objBean.setIntBillSeriesPaxNo(objBean.getIntBillSeriesPaxNo() + rsLiveGroup.getInt(5));

			mapReasonDtl.put(groupCode, objBean);
		    }
		    else
		    {
			objBean = new clsBillItemDtlBean();
			objBean.setStrReasonName(rsLiveGroup.getString(2));
			objBean.setDblDiscountAmt(rsLiveGroup.getDouble(3));
			objBean.setDblNetTotal(rsLiveGroup.getDouble(4));
			objBean.setIntBillSeriesPaxNo(rsLiveGroup.getInt(5));

			mapReasonDtl.put(groupCode, objBean);
		    }
		}
		rsLiveGroup.close();

		rsQFileGroup = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQBill.toString());
		while (rsQFileGroup.next())
		{
		    totalSalesNetTotal += rsQFileGroup.getDouble(4);
		     totalConsolidatedDiscAmt+=rsQFileGroup.getDouble(3);

		    clsBillItemDtlBean objBean = new clsBillItemDtlBean();

		    String groupCode = rsQFileGroup.getString(1);

		    if (mapReasonDtl.containsKey(groupCode))
		    {
			objBean = mapReasonDtl.get(groupCode);

			objBean.setDblDiscountAmt(objBean.getDblDiscountAmt() + rsQFileGroup.getDouble(3));
			objBean.setDblNetTotal(objBean.getDblNetTotal() + rsQFileGroup.getDouble(4));
			objBean.setIntBillSeriesPaxNo(objBean.getIntBillSeriesPaxNo() + rsQFileGroup.getInt(5));

			mapReasonDtl.put(groupCode, objBean);
		    }
		    else
		    {
			objBean = new clsBillItemDtlBean();
			objBean.setStrReasonName(rsQFileGroup.getString(2));
			objBean.setDblDiscountAmt(rsQFileGroup.getDouble(3));
			objBean.setDblNetTotal(rsQFileGroup.getDouble(4));
			objBean.setIntBillSeriesPaxNo(rsQFileGroup.getInt(5));

			mapReasonDtl.put(groupCode, objBean);
		    }
		}
		rsQFileGroup.close();
		
		double groupTotal = 0.0;
		List<clsBillItemDtlBean> listOfGroup = new ArrayList<clsBillItemDtlBean>();
		listOfGroup.addAll(mapReasonDtl.values());

//		StringBuilder sqlLive = new StringBuilder();
//		StringBuilder sqlQFile = new StringBuilder();
//		StringBuilder sqlModLive = new StringBuilder();
//		StringBuilder sqlModQFile = new StringBuilder();
//		sqlLive.setLength(0);
//		sqlQFile.setLength(0);
//		sqlModLive.setLength(0);
//		sqlModQFile.setLength(0);
//		sqlLive.append("SELECT SUM(b.dblAmount)- SUM(b.dblDiscountAmt) amount\n"
//			+ " FROM tblbillhd a,tblbilldtl b,tblgrouphd c,tblsubgrouphd d,tblitemmaster e,tblposmaster f\n"
//			+ " WHERE a.strBillNo=b.strBillNo AND DATE(a.dteBillDate)= DATE(b.dteBillDate) AND a.strPOSCode=f.strPOSCode \n"
//			+ " AND a.strClientCode=b.strClientCode AND b.strItemCode=e.strItemCode AND c.strGroupCode=d.strGroupCode \n"
//			+ " AND d.strSubGroupCode=e.strSubGroupCode AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "'\n");
//
//		sqlQFile.append("SELECT SUM(b.dblAmount)- SUM(b.dblDiscountAmt) amount\n"
//			+ " FROM tblqbillhd a,tblqbilldtl b,tblgrouphd c,tblsubgrouphd d,tblitemmaster e,tblposmaster f\n"
//			+ " WHERE a.strBillNo=b.strBillNo AND DATE(a.dteBillDate)= DATE(b.dteBillDate) AND a.strPOSCode=f.strPOSCode \n"
//			+ " AND a.strClientCode=b.strClientCode AND b.strItemCode=e.strItemCode AND c.strGroupCode=d.strGroupCode \n"
//			+ " AND d.strSubGroupCode=e.strSubGroupCode AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "'\n");
//
//		sqlModLive.append("select sum(b.dblAmount)-sum(b.dblDiscAmt)"
//			+ " from tblbillmodifierdtl b,tblbillhd a,tblposmaster f,tblitemmaster d"
//			+ ",tblsubgrouphd e,tblgrouphd c "
//			+ " where a.strBillNo=b.strBillNo "
//			+ " and date(a.dteBillDate)=date(b.dteBillDate) "
//			+ " and a.strPOSCode=f.strPosCode "
//			+ " and a.strClientCode=b.strClientCode "
//			+ " and LEFT(b.strItemCode,7)=d.strItemCode "
//			+ " and d.strSubGroupCode=e.strSubGroupCode and e.strGroupCode=c.strGroupCode "
//			+ " and b.dblamount>0 "
//			+ " and DATE(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' ");
//
//		sqlModQFile.append("select sum(b.dblAmount)-sum(b.dblDiscAmt)"
//			+ " from tblqbillmodifierdtl b,tblqbillhd a,tblposmaster f,tblitemmaster d"
//			+ ",tblsubgrouphd e,tblgrouphd c "
//			+ " where a.strBillNo=b.strBillNo "
//			+ " and date(a.dteBillDate)=date(b.dteBillDate) "
//			+ " and a.strPOSCode=f.strPosCode "
//			+ " and a.strClientCode=b.strClientCode "
//			+ " and LEFT(b.strItemCode,7)=d.strItemCode "
//			+ " and d.strSubGroupCode=e.strSubGroupCode "
//			+ " and e.strGroupCode=c.strGroupCode "
//			+ " and b.dblamount>0 "
//			+ " and DATE(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' ");
//
//		if (!posCode.equalsIgnoreCase("All"))
//		{
//		    sbSqlLive.append(" and a.strPOSCode='" + posCode + "' ");
//		    sbSqlQBill.append(" and a.strPOSCode='" + posCode + "' ");
//		    sqlModLive.append(" and a.strPOSCode='" + posCode + "' ");
//		    sqlModQFile.append(" and a.strPOSCode='" + posCode + "' ");
//
//		}
//
//		if (clsGlobalVarClass.gEnableShiftYN)
//		{
//		    if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
//		    {
//			sbSqlLive.append(" and a.intShiftCode = '" + shiftNo + "' ");
//			sbSqlQBill.append(" and a.intShiftCode = '" + shiftNo + "' ");
//			sqlModLive.append(" and a.strPOSCode='" + posCode + "' ");
//			sqlModQFile.append(" and a.strPOSCode='" + posCode + "' ");
//
//		    }
//		}
//		sbSqlLive.append(" GROUP BY c.strGroupCode, c.strGroupName, a.strPoscode");
//		sbSqlQBill.append(" GROUP BY c.strGroupCode, c.strGroupName, a.strPoscode");
//		double netRevenueAmt = 0.0;
//		ResultSet rsBillWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlLive.toString());
//		while (rsBillWiseSales.next())
//		{
//		    netRevenueAmt += Double.parseDouble(String.valueOf(rsBillWiseSales.getDouble(1)));
//		}
//		rsBillWiseSales.close();
//
//		rsBillWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlQFile.toString());
//		while (rsBillWiseSales.next())
//		{
//		    netRevenueAmt += Double.parseDouble(String.valueOf(rsBillWiseSales.getDouble(1)));
//		}
//		rsBillWiseSales.close();
//
//		rsBillWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlModLive.toString());
//		while (rsBillWiseSales.next())
//		{
//		    netRevenueAmt += Double.parseDouble(String.valueOf(rsBillWiseSales.getDouble(1)));
//		}
//		rsBillWiseSales.close();
//
//		rsBillWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlModQFile.toString());
//		while (rsBillWiseSales.next())
//		{
//		    netRevenueAmt += Double.parseDouble(String.valueOf(rsBillWiseSales.getDouble(1)));
//		}
//		rsBillWiseSales.close();
		double netRevenuePer = 0.0;
		netRevenuePer = totalConsolidatedDiscAmt / totalSalesNetTotal * 100;

		hm.put("listOfComplimentaryDiscount", listOfComplimentaryDiscount);
		hm.put("listOfPromotionDiscount", listOfPromotionDiscount);
		hm.put("listOfDiscount", listOfDiscount);
		hm.put("consolidateDisc", Math.rint(totalConsolidatedDiscAmt));
		hm.put("netRevenue", Math.rint(totalSalesNetTotal));
		hm.put("groupTotal", Math.rint(groupTotal));
		hm.put("listOfGroupWiseDiscount", listOfGroup);

		InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptReasonWiselDiscountReport.jasper");
		//call for view report
		if (reportType.equalsIgnoreCase("A4 Size Report"))
		{
		    funViewJasperReportForJDBCConnectionDataSource(is, hm, null);
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

    private void funViewJasperReportForJDBCConnectionDataSource(InputStream is, HashMap hm, List list)
    {
	try
	{
	    JasperPrint print = JasperFillManager.fillReport(is, hm, clsGlobalVarClass.conJasper);
	    JRViewer viewer = new JRViewer(print);
	    JFrame jf = new JFrame();
	    jf.getContentPane().add(viewer);
	    jf.validate();
	    jf.setVisible(true);
	    jf.setSize(new Dimension(850, 750));
	    //jf.setLocation(300, 10);
	    //jf.setLocationRelativeTo(this);

	    //export to other format
	    // funExportToOtherFormat(print);
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
}
