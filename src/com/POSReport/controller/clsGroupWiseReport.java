package com.POSReport.controller;

import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsGroupSubGroupWiseSales;
import com.POSGlobal.controller.clsPosConfigFile;
import com.POSReport.controller.comparator.clsGroupSubGroupComparator;
import com.POSReport.controller.comparator.clsGroupSubGroupWiseSalesComparator;
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

public class clsGroupWiseReport
{

    private Map<String, List<Map<String, clsGroupSubGroupWiseSales>>> mapPOSDtlForGroupSubGroup;
    DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();

    public void funGroupWiseReport(String reportType, HashMap hm, String dayEnd)
    {
	try
	{
	    InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptGroupWiseSalesReport.jasper");

	    String fromDate = hm.get("fromDate").toString();
	    String toDate = hm.get("toDate").toString();
	    String posCode = hm.get("posCode").toString();
	    String shiftNo = hm.get("shiftNo").toString();
	    String posName = hm.get("posName").toString();
	    String subGroup = hm.get("subGroup").toString();
	    String subGroupCode = "";
	    String subGroupName = "";
	    if (subGroup.equalsIgnoreCase("All"))
	    {
		subGroupCode = "All";
		subGroupName = "All";
	    }
	    else
	    {
		subGroupCode = subGroup.split("                                         ")[1];
		subGroupName = subGroup.split("                                         ")[0];
	    }

	    String fromDateToDisplay = hm.get("fromDateToDisplay").toString();
	    String toDateToDisplay = hm.get("toDateToDisplay").toString();
	    String currency = hm.get("currency").toString();

	    StringBuilder sbSqlLive = new StringBuilder();
	    StringBuilder sbSqlQFile = new StringBuilder();
	    StringBuilder sbSqlFilters = new StringBuilder();
	    mapPOSDtlForGroupSubGroup = new LinkedHashMap<>();
	    List<clsGroupSubGroupItemBean> listOfGroupWiseSales = new ArrayList<clsGroupSubGroupItemBean>();
	    List<clsGroupSubGroupWiseSales> listOfGroupWise = new ArrayList<clsGroupSubGroupWiseSales>();
	    sbSqlLive.setLength(0);
	    sbSqlQFile.setLength(0);
	    sbSqlFilters.setLength(0);
	    String subtotalAmount = "sum( b.dblAmount)-sum(b.dblDiscountAmt)";
	    String netTotalAmount="sum( b.dblAmount)-sum(b.dblDiscountAmt)+sum(b.dblTaxAmount)";
	    String rate = "b.dblRate";
	    String amount = "sum(b.dblAmount)";
	    String discountAmount = "sum(b.dblDiscountAmt)";
	    if(currency.equalsIgnoreCase("USD"))
	    {
		subtotalAmount = "(sum( b.dblAmount)-sum(b.dblDiscountAmt))/a.dblUSDConverionRate";
		netTotalAmount="(sum( b.dblAmount)-sum(b.dblDiscountAmt)+sum(b.dblTaxAmount))/a.dblUSDConverionRate";
		rate = "b.dblRate/a.dblUSDConverionRate";
		amount = "sum(b.dblAmount)/a.dblUSDConverionRate";
		discountAmount = "sum(b.dblDiscountAmt)/a.dblUSDConverionRate";
	    }	
	    
	    sbSqlQFile.append("SELECT c.strGroupCode,c.strGroupName,sum( b.dblQuantity)"
		    + ","+subtotalAmount+" "
		    + ",f.strPosName, '" + clsGlobalVarClass.gUserCode + "',"+rate+" ,"+amount+","+discountAmount+",a.strPOSCode,"
		    + ""+netTotalAmount+"  "
		    + "FROM tblqbillhd a,tblqbilldtl b,tblgrouphd c,tblsubgrouphd d"
		    + ",tblitemmaster e,tblposmaster f "
		    + "where a.strBillNo=b.strBillNo "
		    + " and date(a.dteBillDate)=date(b.dteBillDate) "
		    + " and a.strPOSCode=f.strPOSCode  "
		    + " and a.strClientCode=b.strClientCode "
		    + "and b.strItemCode=e.strItemCode "
		    + "and c.strGroupCode=d.strGroupCode and d.strSubGroupCode=e.strSubGroupCode ");

	    sbSqlLive.append("SELECT c.strGroupCode,c.strGroupName,sum( b.dblQuantity)"
		    + ","+subtotalAmount+" "
		    + ",f.strPosName, '" + clsGlobalVarClass.gUserCode + "',"+rate+" ,"+amount+","+discountAmount+",a.strPOSCode,"
		    + " "+netTotalAmount+"  "
		    + "FROM tblbillhd a,tblbilldtl b,tblgrouphd c,tblsubgrouphd d"
		    + ",tblitemmaster e,tblposmaster f "
		    + "where a.strBillNo=b.strBillNo "
		    + " and date(a.dteBillDate)=date(b.dteBillDate) "
		    + " and a.strPOSCode=f.strPOSCode  "
		    + " and a.strClientCode=b.strClientCode   "
		    + "and b.strItemCode=e.strItemCode "
		    + "and c.strGroupCode=d.strGroupCode "
		    + " and d.strSubGroupCode=e.strSubGroupCode ");

	    String subtotalAmt = "sum(b.dblAmount)-sum(b.dblDiscAmt)";
	    String rateAmt = "b.dblRate";
	    String amt = "sum(b.dblAmount)";
	    String discountAmt = "sum(b.dblDiscAmt)";
	    if(currency.equalsIgnoreCase("USD"))
	    {
		subtotalAmt = "(sum(b.dblAmount)-sum(b.dblDiscAmt))/a.dblUSDConverionRate";
		rateAmt = "b.dblRate/a.dblUSDConverionRate";
		amt = "sum(b.dblAmount)/a.dblUSDConverionRate";
		discountAmt = "sum(b.dblDiscAmt)/a.dblUSDConverionRate";
	    }	
	    
	    String sqlModLive = "select c.strGroupCode,c.strGroupName"
		    + ",sum(b.dblQuantity),"+subtotalAmt+",f.strPOSName"
		    + ",'" + clsGlobalVarClass.gUserCode + "','0' ,"+amt+","+discountAmt+",a.strPOSCode,"
		    + " "+subtotalAmt+"  "
		    + " from tblbillmodifierdtl b,tblbillhd a,tblposmaster f,tblitemmaster d"
		    + ",tblsubgrouphd e,tblgrouphd c "
		    + " where a.strBillNo=b.strBillNo "
		    + " and date(a.dteBillDate)=date(b.dteBillDate) "
		    + " and a.strPOSCode=f.strPosCode  "
		    + " and a.strClientCode=b.strClientCode  "
		    + " and LEFT(b.strItemCode,7)=d.strItemCode "
		    + " and d.strSubGroupCode=e.strSubGroupCode "
		    + " and e.strGroupCode=c.strGroupCode "
		    + " and b.dblamount>0 ";

	    String sqlModQFile = "select c.strGroupCode,c.strGroupName"
		    + ",sum(b.dblQuantity),sum(b.dblAmount)-sum(b.dblDiscAmt),f.strPOSName"
		    + ",'" + clsGlobalVarClass.gUserCode + "','0' ,"+amt+","+discountAmt+",a.strPOSCode,"
		    + " "+subtotalAmt+" "
		    + " from tblqbillmodifierdtl b,tblqbillhd a,tblposmaster f,tblitemmaster d"
		    + ",tblsubgrouphd e,tblgrouphd c "
		    + " where a.strBillNo=b.strBillNo "
		    + " and date(a.dteBillDate)=date(b.dteBillDate) "
		    + " and a.strPOSCode=f.strPosCode   "
		    + " and a.strClientCode=b.strClientCode   "
		    + " and LEFT(b.strItemCode,7)=d.strItemCode "
		    + " and d.strSubGroupCode=e.strSubGroupCode "
		    + " and e.strGroupCode=c.strGroupCode "
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
	    sbSqlFilters.append(" Group BY c.strGroupCode, c.strGroupName, a.strPoscode ");

	    sbSqlLive.append(sbSqlFilters);
	    sbSqlQFile.append(sbSqlFilters);

	    sqlModLive += " " + sbSqlFilters;
	    sqlModQFile += " " + sbSqlFilters;

	    ResultSet rsGroupWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
	    funGenerateGroupWiseSales(rsGroupWiseSales);
	    rsGroupWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlModLive);
	    funGenerateGroupWiseSales(rsGroupWiseSales);
	    rsGroupWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFile.toString());
	    funGenerateGroupWiseSales(rsGroupWiseSales);
	    rsGroupWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlModQFile);
	    funGenerateGroupWiseSales(rsGroupWiseSales);

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
	    Collections.sort(listOfGroupWise, new clsGroupSubGroupWiseSalesComparator(groupNameComparator)
	    );

	    //call for view report
	    if (reportType.equalsIgnoreCase("A4 Size Report"))
	    {
		funViewJasperReportForBeanCollectionDataSource(is, hm, listOfGroupWise);
	    }
	    if (reportType.equalsIgnoreCase("Excel Report"))
	    {
		int i = 1;
		Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
		List<String> arrListTotal = new ArrayList<String>();
		List<String> arrHeaderList = new ArrayList<String>();
		double totalQty = 0;
		double totalAmount = 0;
		double subTotal = 0;
		double discountTotal = 0;
		DecimalFormat decFormat = new DecimalFormat("0");
		//DecimalFormat gDecimalFormat = new DecimalFormat("0.00");
		for (clsGroupSubGroupWiseSales objBean : listOfGroupWise)
		{
		    List<String> arrListItem = new ArrayList<String>();
		    arrListItem.add(objBean.getGroupName());
		    arrListItem.add(objBean.getPosName());
		    arrListItem.add(String.valueOf(decFormat.format(objBean.getQty())));
		    arrListItem.add(String.valueOf(gDecimalFormat.format(objBean.getSubTotal())));
		    arrListItem.add(String.valueOf(gDecimalFormat.format(objBean.getDiscAmt())));
		    arrListItem.add(String.valueOf(gDecimalFormat.format(objBean.getSalesAmt())));

		    totalQty = totalQty + objBean.getQty();
		    subTotal = subTotal + objBean.getSubTotal();
		    discountTotal = discountTotal + objBean.getDiscAmt();
		    totalAmount = totalAmount + objBean.getSalesAmt();
		    mapExcelItemDtl.put(i, arrListItem);
		    i++;
		}
		arrListTotal.add(String.valueOf(decFormat.format(totalQty)) + "#" + "3");
		arrListTotal.add(String.valueOf(gDecimalFormat.format(subTotal)) + "#" + "4");
		arrListTotal.add(String.valueOf(gDecimalFormat.format(discountTotal)) + "#" + "5");
		arrListTotal.add(String.valueOf(gDecimalFormat.format(totalAmount)) + "#" + "6");

		arrHeaderList.add("Serial No");
		arrHeaderList.add("GroupName");
		arrHeaderList.add("POSName");
		arrHeaderList.add("Qty");
		arrHeaderList.add("Sub Total");
		arrHeaderList.add("Discount");
		arrHeaderList.add("Net Total");

		List<String> arrparameterList = new ArrayList<String>();
		arrparameterList.add("Group Wise Report");
		arrparameterList.add("POS" + " : " + posName);
		arrparameterList.add("FromDate" + " : " + fromDateToDisplay);
		arrparameterList.add("ToDate" + " : " + toDateToDisplay);
		arrparameterList.add("SubGroup Name" + " : " + subGroupName);
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

		funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "groupWiseExcelSheet", dayEnd);
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funGenerateGroupWiseSales(ResultSet rsGroupWiseSales)
    {
	try
	{
	    boolean flgRecords = false;
	    while (rsGroupWiseSales.next())
	    {
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

    public void funAreaWiseGroupWiseSalesReport(String reportType, HashMap hm, String dayEnd)
    {
	try
	{
	    InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptAreaWiseGroupWiseSalesReport.jasper");

	    String fromDate = hm.get("fromDate").toString();
	    String toDate = hm.get("toDate").toString();
	    String posCode = hm.get("posCode").toString();
	    String shiftNo = hm.get("shiftNo").toString();
	    String posName = hm.get("posName").toString();
	    String areaName = hm.get("areaName").toString();

	    String fromDateToDisplay = hm.get("fromDateToDisplay").toString();
	    String toDateToDisplay = hm.get("toDateToDisplay").toString();

	    StringBuilder sbSqlLive = new StringBuilder();
	    StringBuilder sbSqlQFile = new StringBuilder();
	    StringBuilder sbSqlFilters = new StringBuilder();
	    mapPOSDtlForGroupSubGroup = new LinkedHashMap<>();
	    List<clsGroupSubGroupItemBean> listOfGroupWiseSales = new ArrayList<clsGroupSubGroupItemBean>();
	    List<clsGroupSubGroupWiseSales> listOfGroupWise = new ArrayList<clsGroupSubGroupWiseSales>();
	    sbSqlLive.setLength(0);
	    sbSqlQFile.setLength(0);
	    sbSqlFilters.setLength(0);

	    sbSqlQFile.append("SELECT c.strGroupCode,c.strGroupName,sum( b.dblQuantity)"
		    + " ,sum( b.dblAmount)-sum(b.dblDiscountAmt) "
		    + " ,f.strPosName, '" + clsGlobalVarClass.gUserCode + "',b.dblRate ,sum(b.dblAmount),sum(b.dblDiscountAmt),a.strPOSCode,"
		    + " sum( b.dblAmount)-sum(b.dblDiscountAmt)+sum(b.dblTaxAmount),a.strAreaCode,g.strAreaName  "
		    + " FROM tblqbillhd a,tblqbilldtl b,tblgrouphd c,tblsubgrouphd d"
		    + " ,tblitemmaster e,tblposmaster f,tblareamaster g "
		    + " where a.strBillNo=b.strBillNo "
		    + " and date(a.dteBillDate)=date(b.dteBillDate) "
		    + " and a.strPOSCode=f.strPOSCode  "
		    + " and a.strClientCode=b.strClientCode "
		    + " and b.strItemCode=e.strItemCode "
		    + " and c.strGroupCode=d.strGroupCode "
		    + " and d.strSubGroupCode=e.strSubGroupCode "
		    + " and a.strAreaCode=g.strAreaCode ");

	    sbSqlLive.append("SELECT c.strGroupCode,c.strGroupName,sum( b.dblQuantity)"
		    + " ,sum( b.dblAmount)-sum(b.dblDiscountAmt) "
		    + " ,f.strPosName, '" + clsGlobalVarClass.gUserCode + "',b.dblRate ,sum(b.dblAmount),sum(b.dblDiscountAmt),a.strPOSCode,"
		    + " sum( b.dblAmount)-sum(b.dblDiscountAmt)+sum(b.dblTaxAmount),a.strAreaCode,g.strAreaName  "
		    + " FROM tblbillhd a,tblbilldtl b,tblgrouphd c,tblsubgrouphd d"
		    + " ,tblitemmaster e,tblposmaster f,tblareamaster g "
		    + " where a.strBillNo=b.strBillNo "
		    + " and date(a.dteBillDate)=date(b.dteBillDate) "
		    + " and a.strPOSCode=f.strPOSCode  "
		    + " and a.strClientCode=b.strClientCode   "
		    + " and b.strItemCode=e.strItemCode "
		    + " and c.strGroupCode=d.strGroupCode "
		    + " and d.strSubGroupCode=e.strSubGroupCode "
		    + " and a.strAreaCode=g.strAreaCode ");

	    String sqlModLive = "select c.strGroupCode,c.strGroupName"
		    + " ,sum(b.dblQuantity),sum(b.dblAmount)-sum(b.dblDiscAmt),f.strPOSName"
		    + " ,'" + clsGlobalVarClass.gUserCode + "','0' ,sum(b.dblAmount),sum(b.dblDiscAmt),a.strPOSCode,"
		    + " sum(b.dblAmount)-sum(b.dblDiscAmt),a.strAreaCode,g.strAreaName  "
		    + " from tblbillmodifierdtl b,tblbillhd a,tblposmaster f,tblitemmaster d"
		    + " ,tblsubgrouphd e,tblgrouphd c,tblareamaster g "
		    + " where a.strBillNo=b.strBillNo "
		    + " and date(a.dteBillDate)=date(b.dteBillDate) "
		    + " and a.strPOSCode=f.strPosCode  "
		    + " and a.strClientCode=b.strClientCode  "
		    + " and LEFT(b.strItemCode,7)=d.strItemCode "
		    + " and d.strSubGroupCode=e.strSubGroupCode "
		    + " and e.strGroupCode=c.strGroupCode "
		    + " and a.strAreaCode=g.strAreaCode "
		    + " and b.dblamount>0 ";

	    String sqlModQFile = "select c.strGroupCode,c.strGroupName"
		    + ",sum(b.dblQuantity),sum(b.dblAmount)-sum(b.dblDiscAmt),f.strPOSName"
		    + ",'" + clsGlobalVarClass.gUserCode + "','0' ,sum(b.dblAmount),sum(b.dblDiscAmt),a.strPOSCode,"
		    + " sum(b.dblAmount)-sum(b.dblDiscAmt),a.strAreaCode,g.strAreaName "
		    + " from tblqbillmodifierdtl b,tblqbillhd a,tblposmaster f,tblitemmaster d"
		    + ",tblsubgrouphd e,tblgrouphd c,tblareamaster g "
		    + " where a.strBillNo=b.strBillNo "
		    + " and date(a.dteBillDate)=date(b.dteBillDate) "
		    + " and a.strPOSCode=f.strPosCode   "
		    + " and a.strClientCode=b.strClientCode   "
		    + " and LEFT(b.strItemCode,7)=d.strItemCode "
		    + " and d.strSubGroupCode=e.strSubGroupCode "
		    + " and e.strGroupCode=c.strGroupCode "
		    + " and a.strAreaCode=g.strAreaCode "
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

	    if (!areaName.equalsIgnoreCase("All"))
	    {
		ResultSet rsAreaCode = clsGlobalVarClass.dbMysql.executeResultSet("select a.strAreaCode,a.strAreaName "
			+ "from tblareamaster a  "
			+ "where a.strAreaName='" + areaName + "' ");
		if (rsAreaCode.next())
		{
		    sbSqlFilters.append("AND a.strAreaCode='" + rsAreaCode.getString(1) + "' ");
		}
		rsAreaCode.close();
	    }
	    sbSqlFilters.append(" GROUP BY a.strAreaCode,c.strGroupCode, c.strGroupName, a.strPoscode "
		    + " order BY g.strAreaName,c.strGroupName ");

	    sbSqlLive.append(sbSqlFilters);
	    sbSqlQFile.append(sbSqlFilters);

	    sqlModLive += " " + sbSqlFilters;
	    sqlModQFile += " " + sbSqlFilters;

	    Map<String, Map<String, clsGroupSubGroupWiseSales>> mapAreaWiseGroupSale = new HashMap<>();

	    ResultSet rsGroupWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
	    while (rsGroupWiseSales.next())
	    {
		String area = rsGroupWiseSales.getString(13);
		String group = rsGroupWiseSales.getString(2);

		if (mapAreaWiseGroupSale.containsKey(area))
		{
		    Map<String, clsGroupSubGroupWiseSales> mapGroup = mapAreaWiseGroupSale.get(area);
		    if (mapGroup.containsKey(group))
		    {
			clsGroupSubGroupWiseSales objGroupCodeDtl = mapGroup.get(group);

			objGroupCodeDtl.setQty(objGroupCodeDtl.getQty() + rsGroupWiseSales.getDouble(3));
			objGroupCodeDtl.setSubTotal(objGroupCodeDtl.getSubTotal() + rsGroupWiseSales.getDouble(8));
			objGroupCodeDtl.setDiscAmt(objGroupCodeDtl.getDiscAmt() + rsGroupWiseSales.getDouble(9));
			objGroupCodeDtl.setSalesAmt(objGroupCodeDtl.getSalesAmt() + rsGroupWiseSales.getDouble(4));

		    }
		    else
		    {
			clsGroupSubGroupWiseSales objGroupCodeDtl = new clsGroupSubGroupWiseSales(rsGroupWiseSales.getString(1), rsGroupWiseSales.getString(2), rsGroupWiseSales.getString(5), rsGroupWiseSales.getDouble(3), rsGroupWiseSales.getDouble(8), rsGroupWiseSales.getDouble(4), rsGroupWiseSales.getDouble(9), rsGroupWiseSales.getDouble(11));
			objGroupCodeDtl.setStrAreaName(area);

			mapGroup.put(group, objGroupCodeDtl);
		    }

		}
		else
		{
		    clsGroupSubGroupWiseSales objGroupCodeDtl = new clsGroupSubGroupWiseSales(rsGroupWiseSales.getString(1), rsGroupWiseSales.getString(2), rsGroupWiseSales.getString(5), rsGroupWiseSales.getDouble(3), rsGroupWiseSales.getDouble(8), rsGroupWiseSales.getDouble(4), rsGroupWiseSales.getDouble(9), rsGroupWiseSales.getDouble(11));
		    objGroupCodeDtl.setStrAreaName(area);

		    Map<String, clsGroupSubGroupWiseSales> mapGroup = new HashMap<>();
		    mapGroup.put(group, objGroupCodeDtl);

		    mapAreaWiseGroupSale.put(area, mapGroup);

		}

	    }
	    rsGroupWiseSales.close();

	    rsGroupWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlModLive.toString());
	    while (rsGroupWiseSales.next())
	    {
		String area = rsGroupWiseSales.getString(13);
		String group = rsGroupWiseSales.getString(2);

		if (mapAreaWiseGroupSale.containsKey(area))
		{
		    Map<String, clsGroupSubGroupWiseSales> mapGroup = mapAreaWiseGroupSale.get(area);
		    if (mapGroup.containsKey(group))
		    {
			clsGroupSubGroupWiseSales objGroupCodeDtl = mapGroup.get(group);

			objGroupCodeDtl.setQty(objGroupCodeDtl.getQty() + rsGroupWiseSales.getDouble(3));
			objGroupCodeDtl.setSubTotal(objGroupCodeDtl.getSubTotal() + rsGroupWiseSales.getDouble(8));
			objGroupCodeDtl.setDiscAmt(objGroupCodeDtl.getDiscAmt() + rsGroupWiseSales.getDouble(9));
			objGroupCodeDtl.setSalesAmt(objGroupCodeDtl.getSalesAmt() + rsGroupWiseSales.getDouble(4));

		    }
		    else
		    {
			clsGroupSubGroupWiseSales objGroupCodeDtl = new clsGroupSubGroupWiseSales(rsGroupWiseSales.getString(1), rsGroupWiseSales.getString(2), rsGroupWiseSales.getString(5), rsGroupWiseSales.getDouble(3), rsGroupWiseSales.getDouble(8), rsGroupWiseSales.getDouble(4), rsGroupWiseSales.getDouble(9), rsGroupWiseSales.getDouble(11));
			objGroupCodeDtl.setStrAreaName(area);

			mapGroup.put(group, objGroupCodeDtl);
		    }

		}
		else
		{
		    clsGroupSubGroupWiseSales objGroupCodeDtl = new clsGroupSubGroupWiseSales(rsGroupWiseSales.getString(1), rsGroupWiseSales.getString(2), rsGroupWiseSales.getString(5), rsGroupWiseSales.getDouble(3), rsGroupWiseSales.getDouble(8), rsGroupWiseSales.getDouble(4), rsGroupWiseSales.getDouble(9), rsGroupWiseSales.getDouble(11));
		    objGroupCodeDtl.setStrAreaName(area);

		    Map<String, clsGroupSubGroupWiseSales> mapGroup = new HashMap<>();
		    mapGroup.put(group, objGroupCodeDtl);

		    mapAreaWiseGroupSale.put(area, mapGroup);

		}

	    }
	    rsGroupWiseSales.close();

	    rsGroupWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFile.toString());
	    while (rsGroupWiseSales.next())
	    {
		String area = rsGroupWiseSales.getString(13);
		String group = rsGroupWiseSales.getString(2);

		if (mapAreaWiseGroupSale.containsKey(area))
		{
		    Map<String, clsGroupSubGroupWiseSales> mapGroup = mapAreaWiseGroupSale.get(area);
		    if (mapGroup.containsKey(group))
		    {
			clsGroupSubGroupWiseSales objGroupCodeDtl = mapGroup.get(group);

			objGroupCodeDtl.setQty(objGroupCodeDtl.getQty() + rsGroupWiseSales.getDouble(3));
			objGroupCodeDtl.setSubTotal(objGroupCodeDtl.getSubTotal() + rsGroupWiseSales.getDouble(8));
			objGroupCodeDtl.setDiscAmt(objGroupCodeDtl.getDiscAmt() + rsGroupWiseSales.getDouble(9));
			objGroupCodeDtl.setSalesAmt(objGroupCodeDtl.getSalesAmt() + rsGroupWiseSales.getDouble(4));

		    }
		    else
		    {
			clsGroupSubGroupWiseSales objGroupCodeDtl = new clsGroupSubGroupWiseSales(rsGroupWiseSales.getString(1), rsGroupWiseSales.getString(2), rsGroupWiseSales.getString(5), rsGroupWiseSales.getDouble(3), rsGroupWiseSales.getDouble(8), rsGroupWiseSales.getDouble(4), rsGroupWiseSales.getDouble(9), rsGroupWiseSales.getDouble(11));
			objGroupCodeDtl.setStrAreaName(area);

			mapGroup.put(group, objGroupCodeDtl);
		    }

		}
		else
		{
		    clsGroupSubGroupWiseSales objGroupCodeDtl = new clsGroupSubGroupWiseSales(rsGroupWiseSales.getString(1), rsGroupWiseSales.getString(2), rsGroupWiseSales.getString(5), rsGroupWiseSales.getDouble(3), rsGroupWiseSales.getDouble(8), rsGroupWiseSales.getDouble(4), rsGroupWiseSales.getDouble(9), rsGroupWiseSales.getDouble(11));
		    objGroupCodeDtl.setStrAreaName(area);

		    Map<String, clsGroupSubGroupWiseSales> mapGroup = new HashMap<>();
		    mapGroup.put(group, objGroupCodeDtl);

		    mapAreaWiseGroupSale.put(area, mapGroup);

		}

	    }
	    rsGroupWiseSales.close();

	    rsGroupWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlModQFile.toString());
	    while (rsGroupWiseSales.next())
	    {
		String area = rsGroupWiseSales.getString(13);
		String group = rsGroupWiseSales.getString(2);

		if (mapAreaWiseGroupSale.containsKey(area))
		{
		    Map<String, clsGroupSubGroupWiseSales> mapGroup = mapAreaWiseGroupSale.get(area);
		    if (mapGroup.containsKey(group))
		    {
			clsGroupSubGroupWiseSales objGroupCodeDtl = mapGroup.get(group);

			objGroupCodeDtl.setQty(objGroupCodeDtl.getQty() + rsGroupWiseSales.getDouble(3));
			objGroupCodeDtl.setSubTotal(objGroupCodeDtl.getSubTotal() + rsGroupWiseSales.getDouble(8));
			objGroupCodeDtl.setDiscAmt(objGroupCodeDtl.getDiscAmt() + rsGroupWiseSales.getDouble(9));
			objGroupCodeDtl.setSalesAmt(objGroupCodeDtl.getSalesAmt() + rsGroupWiseSales.getDouble(4));

		    }
		    else
		    {
			clsGroupSubGroupWiseSales objGroupCodeDtl = new clsGroupSubGroupWiseSales(rsGroupWiseSales.getString(1), rsGroupWiseSales.getString(2), rsGroupWiseSales.getString(5), rsGroupWiseSales.getDouble(3), rsGroupWiseSales.getDouble(8), rsGroupWiseSales.getDouble(4), rsGroupWiseSales.getDouble(9), rsGroupWiseSales.getDouble(11));
			objGroupCodeDtl.setStrAreaName(area);

			mapGroup.put(group, objGroupCodeDtl);
		    }

		}
		else
		{
		    clsGroupSubGroupWiseSales objGroupCodeDtl = new clsGroupSubGroupWiseSales(rsGroupWiseSales.getString(1), rsGroupWiseSales.getString(2), rsGroupWiseSales.getString(5), rsGroupWiseSales.getDouble(3), rsGroupWiseSales.getDouble(8), rsGroupWiseSales.getDouble(4), rsGroupWiseSales.getDouble(9), rsGroupWiseSales.getDouble(11));
		    objGroupCodeDtl.setStrAreaName(area);

		    Map<String, clsGroupSubGroupWiseSales> mapGroup = new HashMap<>();
		    mapGroup.put(group, objGroupCodeDtl);

		    mapAreaWiseGroupSale.put(area, mapGroup);

		}

	    }
	    rsGroupWiseSales.close();

	    Comparator<clsGroupSubGroupWiseSales> areaNameComparator = new Comparator<clsGroupSubGroupWiseSales>()
	    {

		@Override
		public int compare(clsGroupSubGroupWiseSales o1, clsGroupSubGroupWiseSales o2)
		{
		    return o1.getStrAreaName().compareToIgnoreCase(o2.getStrAreaName());
		}
	    };

	    Comparator<clsGroupSubGroupWiseSales> groupNameComparator = new Comparator<clsGroupSubGroupWiseSales>()
	    {

		@Override
		public int compare(clsGroupSubGroupWiseSales o1, clsGroupSubGroupWiseSales o2)
		{
		    return o1.getGroupName().compareToIgnoreCase(o2.getGroupName());
		}
	    };

	    listOfGroupWise.clear();
	    for (Map<String, clsGroupSubGroupWiseSales> mapGroup : mapAreaWiseGroupSale.values())
	    {
		for (clsGroupSubGroupWiseSales objBean : mapGroup.values())
		{
		    listOfGroupWise.add(objBean);
		}
	    }

	    Collections.sort(listOfGroupWise, new clsGroupSubGroupWiseSalesComparator(areaNameComparator, groupNameComparator));

	    //call for view report
	    if (reportType.equalsIgnoreCase("A4 Size Report"))
	    {
		funViewJasperReportForBeanCollectionDataSource(is, hm, listOfGroupWise);
	    }
	    if (reportType.equalsIgnoreCase("Excel Report"))
	    {
		int i = 1;
		Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
		List<String> arrListTotal = new ArrayList<String>();
		List<String> arrHeaderList = new ArrayList<String>();
		double totalQty = 0;
		double totalAmount = 0;
		double subTotal = 0;
		double discountTotal = 0;
		DecimalFormat decFormat = new DecimalFormat("0");
		//DecimalFormat gDecimalFormat = new DecimalFormat("0.00");
		for (clsGroupSubGroupWiseSales objBean : listOfGroupWise)
		{
		    List<String> arrListItem = new ArrayList<String>();
		    arrListItem.add(objBean.getGroupName());
		    arrListItem.add(objBean.getPosName());
		    arrListItem.add(String.valueOf(decFormat.format(objBean.getQty())));
		    arrListItem.add(String.valueOf(gDecimalFormat.format(objBean.getSubTotal())));
		    arrListItem.add(String.valueOf(gDecimalFormat.format(objBean.getDiscAmt())));
		    arrListItem.add(String.valueOf(gDecimalFormat.format(objBean.getSalesAmt())));

		    totalQty = totalQty + objBean.getQty();
		    subTotal = subTotal + objBean.getSubTotal();
		    discountTotal = discountTotal + objBean.getDiscAmt();
		    totalAmount = totalAmount + objBean.getSalesAmt();
		    mapExcelItemDtl.put(i, arrListItem);
		    i++;
		}
		arrListTotal.add(String.valueOf(decFormat.format(totalQty)) + "#" + "3");
		arrListTotal.add(String.valueOf(gDecimalFormat.format(subTotal)) + "#" + "4");
		arrListTotal.add(String.valueOf(gDecimalFormat.format(discountTotal)) + "#" + "5");
		arrListTotal.add(String.valueOf(gDecimalFormat.format(totalAmount)) + "#" + "6");

		arrHeaderList.add("Serial No");
		arrHeaderList.add("GroupName");
		arrHeaderList.add("POSName");
		arrHeaderList.add("Qty");
		arrHeaderList.add("Sub Total");
		arrHeaderList.add("Discount");
		arrHeaderList.add("Net Total");

		List<String> arrparameterList = new ArrayList<String>();
		arrparameterList.add("Group Wise Report");
		arrparameterList.add("POS" + " : " + posName);
		arrparameterList.add("FromDate" + " : " + fromDateToDisplay);
		arrparameterList.add("ToDate" + " : " + toDateToDisplay);
		arrparameterList.add("Area Name" + " : " + areaName);
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

		funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "groupWiseExcelSheet", dayEnd);
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

}
