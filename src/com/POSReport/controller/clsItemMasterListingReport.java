package com.POSReport.controller;

import com.POSGlobal.controller.clsBillDtl;
import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsPosConfigFile;
import java.awt.Desktop;
import java.awt.Dimension;
import java.io.File;
import java.io.InputStream;
import java.sql.ResultSet;
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

public class clsItemMasterListingReport 
{
    public void funItemMasterListingReport(String reportType, HashMap hm)
    {
        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptItemMasterListingReport.jasper");

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();

            List<clsBillDtl> listOfItemMasterListing = new ArrayList<>();
            StringBuilder sqlBuilder = new StringBuilder();

            sqlBuilder.setLength(0);
            sqlBuilder.append("select a.strItemCode,a.strItemName,b.strSubGroupName,c.strGroupName,a.strTaxIndicator  "
                    + "from tblitemmaster a,tblsubgrouphd b,tblgrouphd c "
                    + "where a.strSubGroupCode=b.strSubGroupCode "
                    + "and b.strGroupCode=c.strGroupCode "
                    + "group by c.strGroupCode,b.strSubGroupCode,a.strItemCode,a.strItemName "
                    + "order by c.strGroupCode,b.strSubGroupCode,a.strItemCode,a.strItemName ");

            ResultSet rsItemMaster = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());

            clsBillDtl objItemDtl = null;
            while (rsItemMaster.next())
            {
                objItemDtl = new clsBillDtl();

                objItemDtl.setStrItemCode(rsItemMaster.getString(1));
                objItemDtl.setStrItemName(rsItemMaster.getString(2));
                objItemDtl.setStrSubGroupName(rsItemMaster.getString(3));
                objItemDtl.setStrGroupName(rsItemMaster.getString(4));
                objItemDtl.setStrTaxIndicator(rsItemMaster.getString(5));

                listOfItemMasterListing.add(objItemDtl);
            }
            rsItemMaster.close();
            //call for view report
            if(reportType.equalsIgnoreCase("A4 Size Report"))
            {
            funViewJasperReportForBeanCollectionDataSource(is, hm, listOfItemMasterListing);
            }
            if(reportType.equalsIgnoreCase("Excel Report"))
            {
                Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
                List<String> arrListTotal = new ArrayList<String>();
                List<String> arrHeaderList = new ArrayList<String>();

                int i = 1;
                for (int cnt = 0; cnt < listOfItemMasterListing.size(); cnt++)
                {
                    List<String> arrListItem = new ArrayList<String>();
                    clsBillDtl obj = listOfItemMasterListing.get(cnt);

                    arrListItem.add(obj.getStrItemCode());
                    arrListItem.add(obj.getStrItemName());
                    arrListItem.add(obj.getStrSubGroupName());
                    arrListItem.add(obj.getStrGroupName());
                    arrListItem.add(obj.getStrTaxIndicator());

                    mapExcelItemDtl.put(i, arrListItem);
                    i++;

                }

                arrHeaderList.add("Serial No");
                arrHeaderList.add("Item Code");
                arrHeaderList.add("Item Name");
                arrHeaderList.add("Sub Group");
                arrHeaderList.add("Group");
                arrHeaderList.add("Tax Indicator");

                List<String> arrparameterList = new ArrayList<String>();
                arrparameterList.add("Item Master Lising Report");
                arrparameterList.add(" ");
                arrparameterList.add(" ");
                arrparameterList.add(" ");
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
                funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "Item Master Lising Report");

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
            if (pages.size()==0)
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
