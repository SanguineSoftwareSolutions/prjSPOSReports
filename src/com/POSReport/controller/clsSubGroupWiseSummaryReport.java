package com.POSReport.controller;

import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsGroupSubGroupWiseSales;
import com.POSGlobal.controller.clsPosConfigFile;
import com.POSGlobal.controller.clsSalesFlashReport;
import com.POSGlobal.controller.clsVoidBillDtl;
import java.awt.Desktop;
import java.awt.Dimension;
import java.io.File;
import java.io.InputStream;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
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
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.swing.JRViewer;

public class clsSubGroupWiseSummaryReport 
{
    DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter(); 
    public void funSubGroupWiseSummaryReport(String reportType, HashMap hm,String dayEnd)
    {
        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptSubGroupWiseSummaryReport.jasper");

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String posName = hm.get("posName").toString();
            String fromDateToDisplay = hm.get("fromDateToDisplay").toString();
            String toDateToDisplay = hm.get("toDateToDisplay").toString();
            String currency = hm.get("currency").toString();
            StringBuilder sbSqlLive = new StringBuilder();
            StringBuilder sql = new StringBuilder();
            StringBuilder sbSqlQFile = new StringBuilder();
            StringBuilder sbSqlFilters = new StringBuilder();

            sbSqlLive.setLength(0);
            sbSqlQFile.setLength(0);
            sbSqlFilters.setLength(0);
            sql.setLength(0);

	    String subTotalAmt = "sum( b.dblAmount )-sum(b.dblDiscountAmt)";
	    String rate = "b.dblRate";
	    String amount = "sum(b.dblAmount)"; 
	    String discAmount = "sum(b.dblDiscountAmt)";
	    if(currency.equalsIgnoreCase("USD"))
	    {
		subTotalAmt = "(sum( b.dblAmount )-sum(b.dblDiscountAmt))/a.dblUSDConverionRate";
		rate = "b.dblRate/a.dblUSDConverionRate";
		amount = "sum(b.dblAmount)/a.dblUSDConverionRate"; 
		discAmount = "sum(b.dblDiscountAmt)/a.dblUSDConverionRate";
	    }	
	    
            sbSqlQFile.append("SELECT c.strSubGroupCode, c.strSubGroupName, sum( b.dblQuantity ) "
                    + ", "+subTotalAmt+", f.strPosName,'" + clsGlobalVarClass.gUserCode + "',"
		    + " "+rate+" ,"+amount+","+discAmount+" "
                    + "from tblqbillhd a,tblqbilldtl b,tblsubgrouphd c,tblitemmaster d "
                    + ",tblposmaster f "
                    + "where a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " and a.strPOSCode=f.strPOSCode "
                    + "and b.strItemCode=d.strItemCode "
                    + "and c.strSubGroupCode=d.strSubGroupCode ");

            sbSqlLive.append("SELECT c.strSubGroupCode, c.strSubGroupName, sum( b.dblQuantity ) "
                    + ", "+subTotalAmt+", f.strPosName,'" + clsGlobalVarClass.gUserCode + "',"
		    + " "+rate+" ,"+amount+","+discAmount+" "
                    + "from tblbillhd a,tblbilldtl b,tblsubgrouphd c,tblitemmaster d "
                    + ",tblposmaster f "
                    + "where a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " and a.strPOSCode=f.strPOSCode "
                    + "and b.strItemCode=d.strItemCode "
                    + "and c.strSubGroupCode=d.strSubGroupCode ");

	    String amt = "sum(b.dblAmount)";
	    if(currency.equalsIgnoreCase("USD"))
	    {
		amt = "sum(b.dblAmount)/a.dblUSDConverionRate";
	    }	
	    
            String sqlModLive = "select c.strSubGroupCode,c.strSubGroupName"
                    + ",sum(b.dblQuantity),"+amt+",f.strPOSName"
                    + ",'" + clsGlobalVarClass.gUserCode + "','0' ,'0.00','0.00' "
                    + " from tblbillmodifierdtl b,tblbillhd a,tblposmaster f,tblitemmaster d"
                    + ",tblsubgrouphd c"
                    + " where a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " and a.strPOSCode=f.strPosCode "
                    + " and left(b.strItemCode,7)=d.strItemCode "
                    + " and d.strSubGroupCode=c.strSubGroupCode "
                    + "  ";

            String sqlModQFile = "select c.strSubGroupCode,c.strSubGroupName"
                    + ",sum(b.dblQuantity),"+amt+",f.strPOSName"
                    + ",'" + clsGlobalVarClass.gUserCode + "','0' ,'0.00','0.00' "
                    + " from tblqbillmodifierdtl b,tblqbillhd a,tblposmaster f,tblitemmaster d"
                    + ",tblsubgrouphd c"
                    + " where a.strBillNo=b.strBillNo "
                    + " and date(a.dteBillDate)=date(b.dteBillDate) "
                    + " and a.strPOSCode=f.strPosCode "
                    + " and left(b.strItemCode,7)=d.strItemCode "
                    + " and d.strSubGroupCode=c.strSubGroupCode "
                    + "  ";

            sbSqlFilters.append(" and date( a.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' ");
            if (!posCode.equals("All"))
            {
                sbSqlFilters.append(" AND a.strPOSCode = '" + posCode + "' ");
            }
            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sbSqlFilters.append(" AND a.intShiftCode = '" + shiftNo + "' ");
                }
            }

            sbSqlFilters.append(" group by c.strSubGroupCode, c.strSubGroupName, a.strPoscode");

            sbSqlLive.append(sbSqlFilters);
            sbSqlQFile.append(sbSqlFilters);
            sqlModLive += " " + sbSqlFilters;
            sqlModQFile += " " + sbSqlFilters;
            
            ResultSet rsLiveData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
            List<clsGroupSubGroupWiseSales> listOfData = new ArrayList<clsGroupSubGroupWiseSales>();
            while (rsLiveData.next())
                
            {
                clsGroupSubGroupWiseSales obj = new clsGroupSubGroupWiseSales();
                obj.setSubGroupCode(rsLiveData.getString(1)); 
                obj.setSubGroupName(rsLiveData.getString(2));
                obj.setPosName(rsLiveData.getString(5));
                obj.setQty(Double.parseDouble(rsLiveData.getString(3)));
                obj.setSubTotal(Double.parseDouble(rsLiveData.getString(8)));
                obj.setDiscAmt(Double.parseDouble(rsLiveData.getString(9)));
                obj.setDblNetTotal(Double.parseDouble(rsLiveData.getString(4)));
                listOfData.add(obj);
            }
            rsLiveData.close();
            
            ResultSet rsQData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFile.toString());
            while (rsQData.next())
                
            {
                clsGroupSubGroupWiseSales obj = new clsGroupSubGroupWiseSales();
                obj.setSubGroupCode(rsQData.getString(1)); 
                obj.setSubGroupName(rsQData.getString(2));
                obj.setPosName(rsQData.getString(5));
                obj.setQty(Double.parseDouble(rsQData.getString(3)));
                obj.setSubTotal(Double.parseDouble(rsQData.getString(8)));
                obj.setDiscAmt(Double.parseDouble(rsQData.getString(9)));
                obj.setDblNetTotal(Double.parseDouble(rsQData.getString(4)));
                listOfData.add(obj);
            }
            rsQData.close();
            
            clsSalesFlashReport obj = new clsSalesFlashReport();
            obj.funProcessSalesFlashReport(sbSqlLive.toString(), sbSqlQFile.toString(), "SubGroupWiseSales");

            String sqlInsertLiveBillSales = "insert into tbltempsalesflash "
                    + "(" + sqlModLive + ");";

            String sqlInsertQFileBillSales = "insert into tbltempsalesflash "
                    + "(" + sqlModQFile + ");";
            //clsGlobalVarClass.dbMysql.execute(sqlInsertLiveBillSales);
           // clsGlobalVarClass.dbMysql.execute(sqlInsertQFileBillSales);
            //call for view report
            if(reportType.equalsIgnoreCase("A4 Size Report"))
            {
            funViewJasperReportForJDBCConnectionDataSource(is, hm, null);
            }
            if(reportType.equalsIgnoreCase("Excel Report"))
            {
                Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
                List<String> arrListTotal = new ArrayList<String>();
                List<String> arrHeaderList = new ArrayList<String>();
                double totalAmount = 0;
                double totalQty = 0;
                double totalsubTotal = 0;
                double totalDisAmt = 0;
                int i = 1;
                DecimalFormat decFormat = new DecimalFormat("0");
                //DecimalFormat gDecimalFormat = new DecimalFormat("0.00");
                String subGroupCode="";
                for(clsGroupSubGroupWiseSales objBean:listOfData)
                {
                    List<String> arrListItem = new ArrayList<String>();
                    arrListItem.add(objBean.getSubGroupCode());
                    arrListItem.add(objBean.getSubGroupName());
                    arrListItem.add(objBean.getPosName());
                    arrListItem.add(String.valueOf(decFormat.format(objBean.getQty())));
                    arrListItem.add(String.valueOf(gDecimalFormat.format(objBean.getSubTotal())));
                    arrListItem.add(String.valueOf(gDecimalFormat.format(objBean.getDiscAmt())));
                    arrListItem.add(String.valueOf(gDecimalFormat.format(objBean.getDblNetTotal())));
                    
                      if(subGroupCode.equalsIgnoreCase(objBean.getSubGroupCode())) 
                      {
                        totalQty = totalQty + Double.parseDouble(String.valueOf(objBean.getQty()));
                        totalAmount = totalAmount + Double.parseDouble(String.valueOf(objBean.getDblNetTotal()));
                        totalsubTotal = totalsubTotal + Double.parseDouble(String.valueOf(objBean.getSubTotal()));
                        totalDisAmt = totalDisAmt + Double.parseDouble(String.valueOf(objBean.getDiscAmt()));
                      }
                    
                    else
                    {
                         totalQty = totalQty + Double.parseDouble(String.valueOf(objBean.getQty()));
                        totalAmount = totalAmount + Double.parseDouble(String.valueOf(objBean.getDblNetTotal()));
                        totalsubTotal = totalsubTotal + Double.parseDouble(String.valueOf(objBean.getSubTotal()));
                        totalDisAmt = totalDisAmt + Double.parseDouble(String.valueOf(objBean.getDiscAmt())); 
                    }    
                    
                    mapExcelItemDtl.put(i, arrListItem);

                    i++;
                    subGroupCode = objBean.getSubGroupCode();

                }
                arrListTotal.add(String.valueOf(decFormat.format(totalQty)) + "#" + "4");
                arrListTotal.add(String.valueOf(gDecimalFormat.format(totalsubTotal)) + "#" + "5");
                arrListTotal.add(String.valueOf(gDecimalFormat.format(totalDisAmt)) + "#" + "6");
                arrListTotal.add(String.valueOf(gDecimalFormat.format(totalAmount)) + "#" + "7");

                arrHeaderList.add("Serial No");
                arrHeaderList.add("SubGroup Code");
                arrHeaderList.add("SubGroup Name");
                arrHeaderList.add("POS Name");
                arrHeaderList.add("Qty");
                arrHeaderList.add("Sub Total");
                arrHeaderList.add("Discount");
                arrHeaderList.add("Net Total");

                List<String> arrparameterList = new ArrayList<String>();
                arrparameterList.add("SubGroup Summary Report");
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

                funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "subGroupSummaryExcelSheet",dayEnd);

            }
        }
        catch (Exception e)
        {
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
    
    public void funCreateExcelSheet(List<String> parameterList, List<String> headerList, Map<Integer, List<String>> map, List<String> totalList, String fileName,String dayEnd)
    {
        String filePath = System.getProperty("user.dir");
        File file = new File(filePath +File.separator+"Reports"+File.separator+ fileName + ".xls");
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

           if(!dayEnd.equalsIgnoreCase("Yes"))
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
