package com.POSReport.controller;

import com.POSGlobal.controller.clsBillDtl;
import com.POSGlobal.controller.clsBillHd;
import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsPosConfigFile;
import java.awt.Desktop;
import java.awt.Dimension;
import java.io.File;
import java.io.InputStream;
import java.sql.ResultSet;
import java.text.DecimalFormat;
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

public class clsTableWisePax
{
    DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter(); 
    public void funItemWiseConsumptionReport(String reportType, HashMap hm)
    {
        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptTableWisePaxReport.jasper");

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String posName = hm.get("posName").toString();
            String fromDateToDisplay = hm.get("fromDateToDisplay").toString();
            String toDateToDisplay = hm.get("toDateToDisplay").toString();

            Map<String, List<String>> mapTablePaxList = new HashMap<String, List<String>>();
            List<String> arrListTablePax = null;
            List<clsBillDtl> arrListPaxData = null;
            int totalPax = 0;

            int position = 0;

            String sqlFilters = "";

            String sqlLive = "select b.strTableNo,b.strTableName,sum(a.intBillSeriesPaxNo) "
                    + " from tblbillhd a,tbltablemaster b "
                    + " where a.strTableNo=b.strTableNo "
                    + " and date( a.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
                    + " and a.strClientCode=b.strClientCode ";

            String sqlQFile = "select b.strTableNo,b.strTableName,sum(a.intBillSeriesPaxNo) "
                    + " from tblqbillhd a,tbltablemaster b "
                    + " where a.strTableNo=b.strTableNo "
                    + " and date( a.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
                    + " and a.strClientCode=b.strClientCode ";

            if (!posCode.equals("All"))
            {
                sqlFilters += " and a.strPOSCode = '" + posCode + "' ";
            }

            sqlFilters += " group by b.strTableNo,a.strPOSCode   ";

            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sqlFilters += " and a.intShiftCode = '" + shiftNo + "' ";
                }
            }

            sqlLive = sqlLive + " " + sqlFilters;
            sqlQFile = sqlQFile + " " + sqlFilters;

            ResultSet rsLiveTablePax = clsGlobalVarClass.dbMysql.executeResultSet(sqlLive);
            while (rsLiveTablePax.next())
            {
                int totalPaxNo = 0;
                if (null != mapTablePaxList.get(rsLiveTablePax.getString(1)))
                {
                    String existPax = "";
                    arrListTablePax = mapTablePaxList.get(rsLiveTablePax.getString(1));
                    for (int i = 0; i < arrListTablePax.size(); i++)
                    {
                        String[] param = arrListTablePax.get(i).split("#");
                        existPax = param[2];
                        int pax = Integer.parseInt(rsLiveTablePax.getString(3));
                        totalPaxNo += Integer.parseInt(existPax) + pax;
                    }
                    arrListTablePax.remove(rsLiveTablePax.getString(1) + "#" + rsLiveTablePax.getString(2) + "#" + existPax);
                    arrListTablePax.add(rsLiveTablePax.getString(1) + "#" + rsLiveTablePax.getString(2) + "#" + totalPaxNo);
                    mapTablePaxList.remove(rsLiveTablePax.getString(1));
                }
                else
                {
                    arrListTablePax = new ArrayList<String>();
                    arrListTablePax.add(rsLiveTablePax.getString(1) + "#" + rsLiveTablePax.getString(2) + "#" + rsLiveTablePax.getString(3));

                }

                if (null != arrListTablePax)
                {
                    mapTablePaxList.put(rsLiveTablePax.getString(1), arrListTablePax);
                }
            }
            rsLiveTablePax.close();

            ResultSet rsQTablePax = clsGlobalVarClass.dbMysql.executeResultSet(sqlQFile);
            while (rsQTablePax.next())
            {
                int totalPaxNo = 0;
                if (null != mapTablePaxList.get(rsQTablePax.getString(1)))
                {
                    String existPax = "";
                    arrListTablePax = mapTablePaxList.get(rsQTablePax.getString(1));
                    for (int i = 0; i < arrListTablePax.size(); i++)
                    {
                        String[] param = arrListTablePax.get(i).split("#");
                        existPax = param[2];
                        int pax = Integer.parseInt(rsQTablePax.getString(3));
                        totalPaxNo += Integer.parseInt(existPax) + pax;
                    }
                    arrListTablePax.remove(rsQTablePax.getString(1) + "#" + rsQTablePax.getString(2) + "#" + existPax);
                    arrListTablePax.add(rsQTablePax.getString(1) + "#" + rsQTablePax.getString(2) + "#" + totalPaxNo);
                    mapTablePaxList.remove(rsQTablePax.getString(1));
                }
                else
                {
                    arrListTablePax = new ArrayList<String>();
                    arrListTablePax.add(rsQTablePax.getString(1) + "#" + rsQTablePax.getString(2) + "#" + rsQTablePax.getString(3));

                }

                if (null != arrListTablePax)
                {
                    mapTablePaxList.put(rsQTablePax.getString(1), arrListTablePax);
                }
            }
            rsQTablePax.close();

            if (mapTablePaxList.size() > 0)
            {
                arrListPaxData = new ArrayList<clsBillDtl>();

                for (Map.Entry<String, List<String>> entry : mapTablePaxList.entrySet())
                {
                    List<String> listOfTablePax = entry.getValue();
                    for (int i = 0; i < listOfTablePax.size(); i++)
                    {

                        String[] tablePaxData = listOfTablePax.get(i).split("#");
                        clsBillDtl objPaxData = new clsBillDtl();
                        objPaxData.setStrBillNo(entry.getKey());
                        objPaxData.setStrTableName(tablePaxData[1]);
                        objPaxData.setIntPAXBillSeriesNo(Integer.parseInt(tablePaxData[2]));
                        arrListPaxData.add(objPaxData);
                    }

                }
            }

            //call for view report
            if (reportType.equalsIgnoreCase("A4 Size Report"))
            {
                funViewJasperReportForBeanCollectionDataSource(is, hm, arrListPaxData);
            }
            if (reportType.equalsIgnoreCase("Excel Report"))
            {

                Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
                List<String> arrListTotal = new ArrayList<String>();
                List<String> arrHeaderList = new ArrayList<String>();
                DecimalFormat decFormat = new DecimalFormat("0");
                int i = 1;
                for (clsBillDtl objBean : arrListPaxData)
                {
                    List<String> arrListItem = new ArrayList<String>();
                    //arrListItem.add(rs.getString(1));
                    arrListItem.add(objBean.getStrTableName());
                    arrListItem.add(String.valueOf(decFormat.format(objBean.getIntPAXBillSeriesNo())));

                    totalPax = totalPax + objBean.getIntPAXBillSeriesNo();
                    mapExcelItemDtl.put(i, arrListItem);
                    i++;
                }

                arrListTotal.add(String.valueOf(decFormat.format(totalPax) + "#" + "2"));

                arrHeaderList.add("Serial No");
                arrHeaderList.add("Table");
                arrHeaderList.add("Pax ");

                List<String> arrparameterList = new ArrayList<String>();
                arrparameterList.add("Table Wise Pax Report");
                arrparameterList.add("POS" + " : " + posName);
                arrparameterList.add("FromDate" + " : " + fromDateToDisplay);
                arrparameterList.add("ToDate" + " : " + toDateToDisplay);
                arrparameterList.add(" ");
                arrparameterList.add(" ");

                funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "tableWisePaxExcelSheet");
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

    public void funCreateExcelSheet(List<String> parameterList, List<String> headerList, Map<Integer, List<String>> map, List<String> totalList, String fileName)
    {
        File file = new File(clsPosConfigFile.exportReportPath + File.separator + fileName + ".xls");
        if(!file.mkdirs()){
            file.mkdir();
        }
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

            Desktop dt = Desktop.getDesktop();
            dt.open(file);

        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(null, ex.getMessage());
            ex.printStackTrace();
        }
    }
}
