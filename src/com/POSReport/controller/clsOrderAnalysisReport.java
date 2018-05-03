package com.POSReport.controller;

import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsPosConfigFile;
import java.awt.Desktop;
import java.awt.Dimension;
import java.io.File;
import java.io.InputStream;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
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

public class clsOrderAnalysisReport
{
    DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();
    public void funOrderAnalysisReport(String reportType, HashMap hm, String dayEnd)
    {
        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptOrderAnalysisReport.jasper");

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String posName = hm.get("posName").toString();
            String fromDateToDisplay = hm.get("fromDateToDisplay").toString();
            String toDateToDisplay = hm.get("toDateToDisplay").toString();

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateFormat.setLenient(false);

            clsOrderAnalysisBean objOrderAnalysis = null;
            Map<String, clsOrderAnalysisBean> hmOrderAnalysisData = new HashMap<String, clsOrderAnalysisBean>();
            StringBuilder sbSql = new StringBuilder();
            sbSql.setLength(0);
            sbSql.append("select c.strItemCode,c.strItemName,b.dblRate,sum(b.dblQuantity),b.strKOTNo"
                    + ",sum(b.dblRate*b.dblQuantity) TotalAmt,c.dblPurchaseRate,SUM(b.dblDiscountAmt) "
                    + "from tblbillhd a,tblbilldtl b,tblitemmaster c "
                    + "where a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " and b.strItemCode=c.strItemCode   "
                    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
            if (!posCode.equals("All"))
            {
                sbSql.append("and a.strPOSCode='" + posCode + "' ");
            }
            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sbSql.append(" and a.intShiftCode = '" + shiftNo + "' ");
                }
            }
            sbSql.append("group by c.strItemCode order by c.strItemName; ");
            ResultSet rsOrderAnanlysis = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
            while (rsOrderAnanlysis.next())
            {
                if (null != hmOrderAnalysisData.get(rsOrderAnanlysis.getString(1)))
                {
                    objOrderAnalysis = hmOrderAnalysisData.get(rsOrderAnanlysis.getString(1));
                    objOrderAnalysis.setSaleQty((objOrderAnalysis.getSaleQty() + rsOrderAnanlysis.getDouble(4)));
                }
                else
                {
                    objOrderAnalysis = new clsOrderAnalysisBean();
                    objOrderAnalysis.setSaleQty((rsOrderAnanlysis.getDouble(4)));
                }
                objOrderAnalysis.setItemCode(rsOrderAnanlysis.getString(1));
                objOrderAnalysis.setStrItemName(rsOrderAnanlysis.getString(2));
                objOrderAnalysis.setItemSaleRate(rsOrderAnanlysis.getDouble(3));
                objOrderAnalysis.setItemPurchaseRate(rsOrderAnanlysis.getDouble(7));
                objOrderAnalysis.setKOTNo(rsOrderAnanlysis.getString(5));
                objOrderAnalysis.setDblNCQty(0);
                objOrderAnalysis.setTotalDiscountAmt(rsOrderAnanlysis.getDouble(8));
                objOrderAnalysis.setVoidKOTQty(0);
                objOrderAnalysis.setVoidQty(0);
                objOrderAnalysis.setDblCompQty(0);
                double finalQty = (objOrderAnalysis.getSaleQty() - (objOrderAnalysis.getVoidQty() + objOrderAnalysis.getVoidKOTQty()));
                objOrderAnalysis.setFinalItemQty(finalQty);
                objOrderAnalysis.setTotalAmt(objOrderAnalysis.getSaleQty() * rsOrderAnanlysis.getDouble(3));
                objOrderAnalysis.setTotalCostValue(objOrderAnalysis.getSaleQty() * rsOrderAnanlysis.getDouble(7));

                hmOrderAnalysisData.put(rsOrderAnanlysis.getString(1), objOrderAnalysis);
            }
            rsOrderAnanlysis.close();
            System.out.println(sbSql);

            sbSql.setLength(0);
            sbSql.append("select c.strItemCode,c.strItemName,b.dblRate,sum(b.dblQuantity),b.strKOTNo"
                    + ",sum(b.dblRate*b.dblQuantity) TotalAmt,c.dblPurchaseRate,SUM(b.dblDiscountAmt) "
                    + "from tblqbillhd a,tblqbilldtl b,tblitemmaster c "
                    + "where a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " and b.strItemCode=c.strItemCode   "
                    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
            if (!posCode.equals("All"))
            {
                sbSql.append("and a.strPOSCode='" + posCode + "' ");
            }
            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sbSql.append(" and a.intShiftCode = '" + shiftNo + "' ");
                }
            }
            sbSql.append("group by c.strItemCode order by c.strItemName; ");
            rsOrderAnanlysis = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
            while (rsOrderAnanlysis.next())
            {
                if (null != hmOrderAnalysisData.get(rsOrderAnanlysis.getString(1)))
                {
                    objOrderAnalysis = hmOrderAnalysisData.get(rsOrderAnanlysis.getString(1));
                    objOrderAnalysis.setSaleQty((objOrderAnalysis.getSaleQty() + rsOrderAnanlysis.getDouble(4)));
                }
                else
                {
                    objOrderAnalysis = new clsOrderAnalysisBean();
                    objOrderAnalysis.setSaleQty((rsOrderAnanlysis.getDouble(4)));
                }
                objOrderAnalysis.setItemCode(rsOrderAnanlysis.getString(1));
                objOrderAnalysis.setStrItemName(rsOrderAnanlysis.getString(2));
                objOrderAnalysis.setItemSaleRate(rsOrderAnanlysis.getDouble(3));
                objOrderAnalysis.setItemPurchaseRate(rsOrderAnanlysis.getDouble(7));
                objOrderAnalysis.setKOTNo(rsOrderAnanlysis.getString(5));
                objOrderAnalysis.setDblNCQty(0);
                objOrderAnalysis.setTotalDiscountAmt(rsOrderAnanlysis.getDouble(8));
                objOrderAnalysis.setVoidKOTQty(0);
                objOrderAnalysis.setVoidQty(0);
                objOrderAnalysis.setDblCompQty(0);
                double finalQty = (objOrderAnalysis.getSaleQty() - (objOrderAnalysis.getVoidQty() + objOrderAnalysis.getVoidKOTQty()));
                objOrderAnalysis.setFinalItemQty(finalQty);
                objOrderAnalysis.setTotalAmt(objOrderAnalysis.getSaleQty() * rsOrderAnanlysis.getDouble(3));
                objOrderAnalysis.setTotalCostValue(objOrderAnalysis.getSaleQty() * rsOrderAnanlysis.getDouble(7));

                hmOrderAnalysisData.put(rsOrderAnanlysis.getString(1), objOrderAnalysis);
            }
            rsOrderAnanlysis.close();
            System.out.println(sbSql);

            sbSql.setLength(0);
            sbSql.append("select a.strItemCode,sum(a.dblQuantity) "
                    + "from tblnonchargablekot a "
                    + "where date(a.dteNCKOTDate) between '" + fromDate + "' and '" + toDate + "' ");
            if (!posCode.equals("All"))
            {
                sbSql.append("and a.strPOSCode='' ");
            }

            sbSql.append("group by a.strItemCode");
            rsOrderAnanlysis = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
            while (rsOrderAnanlysis.next())
            {
                if (null != hmOrderAnalysisData.get(rsOrderAnanlysis.getString(1)))
                {
                    objOrderAnalysis = hmOrderAnalysisData.get(rsOrderAnanlysis.getString(1));
                    objOrderAnalysis.setDblNCQty(rsOrderAnanlysis.getDouble(2));
                    double finalQty = (objOrderAnalysis.getSaleQty() - (objOrderAnalysis.getVoidQty() + objOrderAnalysis.getVoidKOTQty()));
                    objOrderAnalysis.setFinalItemQty(finalQty);
                    objOrderAnalysis.setTotalAmt(objOrderAnalysis.getSaleQty() * objOrderAnalysis.getItemSaleRate());
                    objOrderAnalysis.setTotalCostValue(objOrderAnalysis.getSaleQty() * objOrderAnalysis.getItemPurchaseRate());
                    hmOrderAnalysisData.put(rsOrderAnanlysis.getString(1), objOrderAnalysis);
                }
            }
            rsOrderAnanlysis.close();
            System.out.println(sbSql);

            sbSql.setLength(0);
            sbSql.append("select a.strItemCode,sum(a.intQuantity) "
                    + "from tblvoidbilldtl a "
                    + "where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
            if (!posCode.equals("All"))
            {
                sbSql.append("and a.strPOSCode='' ");
            }
            sbSql.append("group by a.strItemCode");
            rsOrderAnanlysis = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
            while (rsOrderAnanlysis.next())
            {
                if (null != hmOrderAnalysisData.get(rsOrderAnanlysis.getString(1)))
                {
                    objOrderAnalysis = hmOrderAnalysisData.get(rsOrderAnanlysis.getString(1));
                    objOrderAnalysis.setVoidQty(rsOrderAnanlysis.getDouble(2));
                    double finalQty = (objOrderAnalysis.getSaleQty() - (objOrderAnalysis.getVoidQty() + objOrderAnalysis.getVoidKOTQty()));
                    objOrderAnalysis.setFinalItemQty(finalQty);
                    objOrderAnalysis.setTotalAmt(objOrderAnalysis.getSaleQty() * objOrderAnalysis.getItemSaleRate());
                    objOrderAnalysis.setTotalCostValue(objOrderAnalysis.getSaleQty() * objOrderAnalysis.getItemPurchaseRate());
                    hmOrderAnalysisData.put(rsOrderAnanlysis.getString(1), objOrderAnalysis);
                }
            }
            rsOrderAnanlysis.close();
            System.out.println(sbSql);

            sbSql.setLength(0);
            sbSql.append("select a.strItemCode,sum(a.dblItemQuantity) "
                    + "from tblvoidkot a,tblitemmaster b "
                    + "where a.strItemCode=b.strItemCode and date(a.dteVoidedDate) between '" + fromDate + "' and '" + toDate + "' ");
            if (!posCode.equals("All"))
            {
                sbSql.append("and a.strPOSCode='' ");
            }
            sbSql.append("group by a.strItemCode");
            rsOrderAnanlysis = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
            while (rsOrderAnanlysis.next())
            {
                if (null != hmOrderAnalysisData.get(rsOrderAnanlysis.getString(1)))
                {
                    objOrderAnalysis = hmOrderAnalysisData.get(rsOrderAnanlysis.getString(1));
                    objOrderAnalysis.setVoidKOTQty(rsOrderAnanlysis.getDouble(2));
                    double finalQty = (objOrderAnalysis.getSaleQty() - (objOrderAnalysis.getVoidQty() + objOrderAnalysis.getVoidKOTQty()));
                    objOrderAnalysis.setFinalItemQty(finalQty);
                    objOrderAnalysis.setTotalAmt(objOrderAnalysis.getSaleQty() * objOrderAnalysis.getItemSaleRate());
                    objOrderAnalysis.setTotalCostValue(objOrderAnalysis.getSaleQty() * objOrderAnalysis.getItemPurchaseRate());
                    hmOrderAnalysisData.put(rsOrderAnanlysis.getString(1), objOrderAnalysis);
                }
            }
            rsOrderAnanlysis.close();
            System.out.println(sbSql);

            sbSql.setLength(0);
            sbSql.append("select b.stritemcode,sum(b.dblQuantity)  "
                    + " from tblbillhd a,tblbilldtl b, tblbillsettlementdtl c,tblsettelmenthd d,tblposmaster e  ,tblitemmaster f,tblsubgrouphd g,tblgrouphd h  "
                    + " where a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " and a.strBillNo=c.strBillNo "
                    + " and date(a.dteBillDate)=date(c.dteBillDate) "
                    + " and c.strSettlementCode=d.strSettelmentCode  "
                    + " and a.strPOSCode=e.strPosCode "
                    + " and b.strItemCode=f.strItemCode "
                    + " and f.strSubGroupCode=g.strSubGroupCode  "
                    + " and g.strGroupCode=h.strGroupCode "
                    + " and d.strSettelmentType='Complementary'  "
                    + " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");

            if (!posCode.equals("All"))
            {
                sbSql.append("and a.strPOSCode='' ");
            }
            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sbSql.append(" and a.intShiftCode = '" + shiftNo + "' ");
                }
            }
            sbSql.append("group by b.strItemCode,e.strPOSName order by a.dteBillDate ");
            rsOrderAnanlysis = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
            while (rsOrderAnanlysis.next())
            {
                if (null != hmOrderAnalysisData.get(rsOrderAnanlysis.getString(1)))
                {
                    objOrderAnalysis = hmOrderAnalysisData.get(rsOrderAnanlysis.getString(1));
                    objOrderAnalysis.setDblCompQty(rsOrderAnanlysis.getDouble(2));
                    double finalQty = (objOrderAnalysis.getSaleQty() - (objOrderAnalysis.getVoidQty() + objOrderAnalysis.getVoidKOTQty()));
                    objOrderAnalysis.setFinalItemQty(finalQty);
                    objOrderAnalysis.setTotalAmt(objOrderAnalysis.getSaleQty() * objOrderAnalysis.getItemSaleRate());
                    objOrderAnalysis.setTotalCostValue(objOrderAnalysis.getSaleQty() * objOrderAnalysis.getItemPurchaseRate());
                    hmOrderAnalysisData.put(rsOrderAnanlysis.getString(1), objOrderAnalysis);
                }
            }
            rsOrderAnanlysis.close();
            System.out.println(sbSql);

            String sql = "truncate table tblorderanalysis";
            clsGlobalVarClass.dbMysql.execute(sql);

            double totalSaleAmt = 0, totalCostValue = 0;
            List<clsOrderAnalysisBean> listOrderAnalysis = new ArrayList<clsOrderAnalysisBean>();
            for (Map.Entry<String, clsOrderAnalysisBean> entry : hmOrderAnalysisData.entrySet())
            {

                clsOrderAnalysisBean objOrderAnalysis1 = entry.getValue();
                sbSql.setLength(0);
                sbSql.append("insert into tblorderanalysis values('" + objOrderAnalysis1.getStrItemName() + "'"
                        + ",'" + objOrderAnalysis1.getItemSaleRate() + "','" + objOrderAnalysis1.getSaleQty() + "'"
                        + ",'" + objOrderAnalysis1.getDblNCQty() + "','" + objOrderAnalysis1.getVoidQty() + "'"
                        + ",'" + objOrderAnalysis1.getVoidKOTQty() + "','" + objOrderAnalysis1.getKOTNo() + "'"
                        + ",'" + objOrderAnalysis1.getItemPurchaseRate() + "','" + objOrderAnalysis1.getTotalAmt() + "'"
                        + ",'" + objOrderAnalysis1.getTotalCostValue() + "','" + objOrderAnalysis1.getTotalDiscountAmt() + "'"
                        + ",'" + clsGlobalVarClass.gUserCode + "','0','0','" + objOrderAnalysis1.getDblCompQty() + "')");
                System.out.println(objOrderAnalysis1.getItemCode() + "\t" + sbSql);
                clsGlobalVarClass.dbMysql.execute(sbSql.toString());

                totalSaleAmt += objOrderAnalysis1.getTotalAmt();
                totalCostValue += objOrderAnalysis1.getTotalCostValue();
            }

            String pattern = "###.##";
            //DecimalFormat gDecimalFormat = new DecimalFormat(pattern);
            for (Map.Entry<String, clsOrderAnalysisBean> entry : hmOrderAnalysisData.entrySet())
            {

                clsOrderAnalysisBean objOrderAnalysis1 = entry.getValue();
                sbSql.setLength(0);
                double per = 0;
                if (totalSaleAmt > 0)
                {
                    per = Math.rint((objOrderAnalysis1.getTotalAmt() / totalSaleAmt) * 100);
                    objOrderAnalysis1.setPer(per);

                }
                double costValuePer = 0;
                if (totalCostValue > 0)
                {
                    costValuePer =Math.rint((objOrderAnalysis1.getSaleQty() / totalCostValue) * 100);
                    objOrderAnalysis1.setCostValuePer(costValuePer);
                }
                sbSql.append("update tblorderanalysis set strField13='" + per + "',strField14='" + costValuePer + "' "
                        + "where strField1='" + objOrderAnalysis1.getStrItemName() + "'");
                clsGlobalVarClass.dbMysql.execute(sbSql.toString());
                listOrderAnalysis.add(objOrderAnalysis1);

            }

            //call for view report
            if (reportType.equalsIgnoreCase("A4 Size Report"))
            {
                funViewJasperReportForBeanCollectionDataSource(is, hm, listOrderAnalysis);
            }
            if (reportType.equalsIgnoreCase("Excel Report"))
            {
                Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
                List<String> arrListTotal = new ArrayList<String>();
                List<String> arrHeaderList = new ArrayList<String>();
                double totalNCQty = 0;
                double totalCompQty = 0;
                double totalSaleQty = 0;
                double totalVoidQty = 0;
                double totalAmt = 0;
                double totalDis = 0;
                double totalCostVal = 0;
                double totalVoidKOTQty = 0;
                double totalPer = 0;
                int i = 1;
                DecimalFormat decFormat= new DecimalFormat("0");
                //DecimalFormat gDecimalFormat= new DecimalFormat("0.00");
                for (clsOrderAnalysisBean objBean : listOrderAnalysis)
                {
                    List<String> arrListItem = new ArrayList<String>();
                    arrListItem.add(objBean.getStrItemName());  // Item Name
                    arrListItem.add(String.valueOf(gDecimalFormat.format(objBean.getItemSaleRate())));  // Rate
                    arrListItem.add(String.valueOf(decFormat.format(objBean.getSaleQty())));  // Sale Qty
                    arrListItem.add(String.valueOf(decFormat.format(objBean.getDblNCQty())));  // NC Qty 
                    arrListItem.add(String.valueOf(decFormat.format(objBean.getVoidQty())));  // Void Qty 
                    arrListItem.add(String.valueOf(decFormat.format(objBean.getVoidKOTQty())));  // Void KOT Qty
                    arrListItem.add(String.valueOf(decFormat.format(objBean.getCompliQty())));// Comp Qty
                    arrListItem.add(String.valueOf(gDecimalFormat.format(objBean.getItemPurchaseRate())));  // Purchase Rate 
                    arrListItem.add(String.valueOf(gDecimalFormat.format(objBean.getTotalAmt())));  // Total Amt                     
                    arrListItem.add(String.valueOf(gDecimalFormat.format(objBean.getTotalDiscountAmt()))); // Total Discount                    
                    arrListItem.add(String.valueOf(Math.rint((objBean.getTotalAmt() / totalSaleAmt) * 100))); // Total Amt Per
                    arrListItem.add(String.valueOf(gDecimalFormat.format(objBean.getTotalCostValue())));  // Total Cost Value                     
                    arrListItem.add(String.valueOf(objBean.getCostValuePer())); // Total Cost Value Per                    

                    totalNCQty = totalNCQty + objBean.getDblNCQty();
                    totalSaleQty = totalSaleQty + objBean.getSaleQty();
                    totalVoidQty = totalVoidQty + objBean.getVoidQty();
                    totalVoidKOTQty = totalVoidKOTQty + objBean.getVoidKOTQty();
                    totalCompQty = totalCompQty + objBean.getCompliQty();
                    totalAmt = totalAmt + objBean.getTotalAmt();
                    totalDis = totalDis + objBean.getTotalDiscountAmt();
                    totalCostVal = totalCostVal + objBean.getTotalCostValue();

                    mapExcelItemDtl.put(i, arrListItem);
                    i++;
                }

                totalPer = (totalAmt / totalAmt) * 100;
                arrListTotal.add(String.valueOf(decFormat.format(totalSaleQty)) + "#" + "3");
                arrListTotal.add(String.valueOf(decFormat.format(totalNCQty))+ "#" + "4");
                arrListTotal.add(String.valueOf(decFormat.format(totalVoidQty)) + "#" + "5");
                arrListTotal.add(String.valueOf(decFormat.format(totalVoidKOTQty)) + "#" + "6");
                arrListTotal.add(String.valueOf(decFormat.format(totalCompQty)) + "#" + "7");
                arrListTotal.add(String.valueOf(gDecimalFormat.format(totalAmt)) + "#" + "9");
                arrListTotal.add(String.valueOf(gDecimalFormat.format(totalDis)) + "#" + "10");
                arrListTotal.add(String.valueOf(totalPer) + "#" + "11");
                arrListTotal.add(String.valueOf(gDecimalFormat.format(totalCostValue)) + "#" + "12");

                arrHeaderList.add("Serial No");
                arrHeaderList.add("Item Name");
                arrHeaderList.add("Rate");
                arrHeaderList.add("Sale Qty");
                arrHeaderList.add("NC Qty");
                arrHeaderList.add("Void Qty");
                arrHeaderList.add("Void KOT");
                arrHeaderList.add("Comp Qty");
                arrHeaderList.add("Purchase rate");
                arrHeaderList.add("Total Amount");
                arrHeaderList.add("Total Discount");
                arrHeaderList.add("% To Total");
                arrHeaderList.add("Total Cost Value");
                arrHeaderList.add("Food Cost");

                List<String> arrparameterList = new ArrayList<String>();
                arrparameterList.add("Order Analysis Report");
                arrparameterList.add("POS" + " : " + posName);
                arrparameterList.add("FromDate" + " : " + fromDateToDisplay);
                arrparameterList.add("ToDate" + " : " + toDateToDisplay);
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

                funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "orderAnalysisExcelSheet", dayEnd);

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

}
