package com.POSReport.controller;

import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsPosConfigFile;
import com.POSGlobal.controller.clsUtility;
import com.POSGlobal.view.frmShowTextFile;
import java.awt.Desktop;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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

public class clsSettlementWiseReport
{
    DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();
    public void funSettlementWiseReport(String reportType, HashMap hm, String dayEnd)
    {
        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptSettelementWiseSalesReport.jasper");

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String posName = hm.get("posName").toString();
            String fromDateToDisplay = hm.get("fromDateToDisplay").toString();
            String toDateToDisplay = hm.get("toDateToDisplay").toString();

            StringBuilder sbSqlLive = new StringBuilder();
            StringBuilder sbSqlQFile = new StringBuilder();
            StringBuilder sqlFilter = new StringBuilder();
            //DecimalFormat gDecimalFormat = new DecimalFormat("0.00");
            DecimalFormat decimalFormat0Dec = new DecimalFormat("0");

            sbSqlLive.append("select ifnull(c.strPosCode,'All'),a.strSettelmentDesc, ifnull(SUM(b.dblSettlementAmt),0.00) "
                    + ",ifnull(d.strposname,'All'), if(c.strPOSCode is null,0,COUNT(*)) "
                    + "from tblsettelmenthd a "
                    + "left outer join tblbillsettlementdtl b on a.strSettelmentCode=b.strSettlementCode and date(b.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
                    + "left outer join tblbillhd c on b.strBillNo=c.strBillNo and date(b.dteBillDate)=date(c.dteBillDate) "
                    + "left outer join tblposmaster d on c.strPOSCode=d.strPosCode ");

            sbSqlQFile.append("select ifnull(c.strPosCode,'All'),a.strSettelmentDesc, ifnull(SUM(b.dblSettlementAmt),0.00) "
                    + ",ifnull(d.strposname,'All'), if(c.strPOSCode is null,0,COUNT(*)) "
                    + "from tblsettelmenthd a "
                    + "left outer join tblqbillsettlementdtl b on a.strSettelmentCode=b.strSettlementCode and date(b.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
                    + "left outer join tblqbillhd c on b.strBillNo=c.strBillNo and date(b.dteBillDate)=date(c.dteBillDate) "
                    + "left outer join tblposmaster d on c.strPOSCode=d.strPosCode ");

            sqlFilter.append(" where a.strSettelmentType!='Complementary' "
                    + "and a.strApplicable='Yes' ");

            if (!"All".equalsIgnoreCase(posCode))
            {
                sqlFilter.append("and  c.strPosCode='" + posCode + "' ");
            }
            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sqlFilter.append(" and c.intShiftCode = '" + shiftNo + "' ");
                }
            }

            sqlFilter.append("group by a.strSettelmentCode "
                    + "order by b.dblSettlementAmt desc ");

            sbSqlLive.append(sqlFilter);
            sbSqlQFile.append(sqlFilter);

            ResultSet rsData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());

            Map<String, clsBillItemDtlBean> mapSettlementModes = new HashMap<>();

            double grossRevenue = 0;
            while (rsData.next())
            {
                String settlementName = rsData.getString(2);
                if (mapSettlementModes.containsKey(settlementName))
                {
                    clsBillItemDtlBean obj = mapSettlementModes.get(settlementName);

                    obj.setDblSettlementAmt(obj.getDblSettlementAmt() + rsData.getDouble(3));
                    obj.setNoOfBills(obj.getNoOfBills() + rsData.getInt(5));

                }
                else
                {
                    clsBillItemDtlBean obj = new clsBillItemDtlBean();
                    obj.setStrPosCode(rsData.getString(1));
                    obj.setStrSettelmentMode(settlementName);
                    obj.setDblSettlementAmt(rsData.getDouble(3));
                    obj.setStrPosName(rsData.getString(4));
                    obj.setNoOfBills(rsData.getInt(5));

                    mapSettlementModes.put(settlementName, obj);

                }

                grossRevenue += rsData.getDouble(3);

            }

            rsData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFile.toString());
            while (rsData.next())
            {
                String settlementName = rsData.getString(2);
                if (mapSettlementModes.containsKey(settlementName))
                {
                    clsBillItemDtlBean obj = mapSettlementModes.get(settlementName);

                    obj.setDblSettlementAmt(obj.getDblSettlementAmt() + rsData.getDouble(3));
                    obj.setNoOfBills(obj.getNoOfBills() + rsData.getInt(5));

                }
                else
                {
                    clsBillItemDtlBean obj = new clsBillItemDtlBean();
                    obj.setStrPosCode(rsData.getString(1));
                    obj.setStrSettelmentMode(settlementName);
                    obj.setDblSettlementAmt(rsData.getDouble(3));
                    obj.setStrPosName(rsData.getString(4));
                    obj.setNoOfBills(rsData.getInt(5));

                    mapSettlementModes.put(settlementName, obj);

                }

                grossRevenue += rsData.getDouble(3);
            }

            hm.put("grossRevenue", grossRevenue);

            List<clsBillItemDtlBean> listOfSettlementData = new ArrayList<clsBillItemDtlBean>();

            for (clsBillItemDtlBean objDtlBean : mapSettlementModes.values())
            {
                listOfSettlementData.add(objDtlBean);
            }
            Comparator<clsBillItemDtlBean> amtComparator = new Comparator<clsBillItemDtlBean>()
            {

                @Override
                public int compare(clsBillItemDtlBean o1, clsBillItemDtlBean o2)
                {
                    if (o1.getDblSettlementAmt() == o2.getDblSettlementAmt())
                    {
                        return 0;
                    }
                    else if (o1.getDblSettlementAmt() > o2.getDblSettlementAmt())
                    {
                        return -1;
                    }
                    else
                    {
                        return 1;
                    }
                }
            };

            Collections.sort(listOfSettlementData, amtComparator);

            //call for view report
            if (reportType.equalsIgnoreCase("A4 Size Report"))
            {
                funViewJasperReportForBeanCollectionDataSource(is, hm, listOfSettlementData);
            }
            if (reportType.equalsIgnoreCase("Excel Report"))
            {
                Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
                List<String> arrListTotal = new ArrayList<String>();
                List<String> arrHeaderList = new ArrayList<String>();
                double totalSettlementAmt = 0, noOfBills = 0;
                int i = 1;
                for (clsBillItemDtlBean objBean : listOfSettlementData)
                {
                    List<String> arrListItem = new ArrayList<String>();

                    arrListItem.add(objBean.getStrSettelmentMode());
                    arrListItem.add(gDecimalFormat.format(objBean.getDblSettlementAmt()));
                    arrListItem.add(decimalFormat0Dec.format(objBean.getNoOfBills()));
                    arrListItem.add(String.valueOf(Math.rint(objBean.getDblSettlementAmt() / grossRevenue) * 100));

                    totalSettlementAmt = totalSettlementAmt + objBean.getDblSettlementAmt();
                    noOfBills = noOfBills + objBean.getNoOfBills();

                    mapExcelItemDtl.put(i, arrListItem);
                    i++;
                }
                arrListTotal.add(gDecimalFormat.format(totalSettlementAmt) + "#" + "2");
                arrListTotal.add(decimalFormat0Dec.format(noOfBills) + "#" + "3");

                arrHeaderList.add("Serial No");
                arrHeaderList.add("Settlement");
                arrHeaderList.add("Gross Amount");
                arrHeaderList.add("No.Of Bills");
                arrHeaderList.add("%To Gross Revenue");

                List<String> arrparameterList = new ArrayList<String>();
                arrparameterList.add("Settlement Wise Report");
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

                funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "settlemntWiseExcelSheet", dayEnd);
            }
            if (reportType.equalsIgnoreCase("Text File-40 Column Report")){

                funCreateTempFolder();
                String filePath = System.getProperty("user.dir");
                File file = new File(filePath + File.separator + "Temp" + File.separator + "Temp_SettlementWiseSalesTextReport.txt");

                int count = funGenerateSettlementWiseTextReport(file, posName, fromDate, toDate,shiftNo, listOfSettlementData);
                if (count > 0)
                {
                    funShowTextFile(file, "Settlement Wise Sales");
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

    public int funGenerateSettlementWiseTextReport(File file, String pos, String fDate, String tDate,String shiftNo,List<clsBillItemDtlBean> list)
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
//            funPrintBlankLines(clsGlobalVarClass.gClientName, pw);
//            funPrintBlankLines(clsGlobalVarClass.gClientAddress1, pw);
//            if (clsGlobalVarClass.gClientAddress2.trim().length() > 0)
//            {
//                funPrintBlankLines(clsGlobalVarClass.gClientAddress2, pw);
//            }
//            if (clsGlobalVarClass.gClientAddress3.trim().length() > 0)
//            {
//                funPrintBlankLines(clsGlobalVarClass.gClientAddress3, pw);
//            }
            pw.println();
            funPrintBlankLines("Settlement Wise Sales Report", pw);
            pw.println();
            pw.println();
            pw.println("POS  :" + pos);
            pw.println("Client Name  :" + clsGlobalVarClass.gClientName);
            pw.println("Shift No  :" + shiftNo);
            pw.println("From :" + fromDate + "  To :" + toDate);
            pw.println("---------------------------------------");
            //pw.println(dashedLine);
            pw.println("Settlement Name        Amount");
            pw.println("---------------------------------------");

            double Total = 0;
            for (clsBillItemDtlBean objBean : list)
            {
                pw.print(funPrintTextWithAlignment1("Left",objBean.getStrSettelmentMode(), 20, pw));
                pw.print(funPrintTextWithAlignment1("Right",gDecimalFormat.format(objBean.getDblSettlementAmt()), 20, pw));
 
                Total=Total+objBean.getDblSettlementAmt();
                pw.println();
                count++;
            }

            pw.println("---------------------------------------");
            
            pw.print(funPrintTextWithAlignment1("Left","TOTAL :", 20, pw));
            pw.print(funPrintTextWithAlignment1("Right",gDecimalFormat.format(Total), 20, pw));
            pw.println();
            pw.println("---------------------------------------");

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

        pw.print(textToPrint);
        return 1;
    }
     

    private String funPrintTextWithAlignment1(String alignment, String text, int totalLength, PrintWriter pw)
    {
        StringBuilder sbText = new StringBuilder();
        if (alignment.equalsIgnoreCase("Center"))
        {
            int textLength = text.length();
            int totalSpace = (totalLength - textLength) / 2;

            for (int i = 0; i < totalSpace; i++)
            {
                sbText.append(" ");
            }
            sbText.append(text);
        }
        else if (alignment.equalsIgnoreCase("Left"))
        {
            sbText.setLength(0);
            int textLength = text.length();
            int totalSpace = (totalLength - textLength);
            sbText.append(text);
            for (int i = 0; i < totalSpace; i++)
            {
                sbText.append(" ");
            }
        }
        else
        {
            sbText.setLength(0);
            int textLength = text.length();
            int totalSpace = (totalLength - textLength);
            for (int i = 0; i < totalSpace; i++)
            {
                sbText.append(" ");
            }
            sbText.append(text);
        }

        return sbText.toString();
    }

}
