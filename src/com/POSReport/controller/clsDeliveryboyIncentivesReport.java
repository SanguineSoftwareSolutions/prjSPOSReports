package com.POSReport.controller;

import com.POSReport.controller.comparator.clsWaiterWiseSalesComparator;
import com.POSGlobal.controller.clsBillDtl;
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

public class clsDeliveryboyIncentivesReport 
{
    DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();  
    public void funDeliveryboyIncentivesReport(String reportType, HashMap hm,String dayEnd)
    {
        try
        {
            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String type = hm.get("rptType").toString();
            String dpName = hm.get("DPName").toString();
            String dpCode = hm.get("DPCode").toString();
            String posName = hm.get("posName").toString();
            
            Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
            List<String> arrListTotal = new ArrayList<String>();
            List<String> arrHeaderList = new ArrayList<String>();
            double totalAmount = 0;
            double totalQty = 0;
            double totalIncentiveAmt = 0;

            if (type.equalsIgnoreCase("Summary"))
            {
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptDeliveryBoyIncentiveSummaryReport.jasper");
                StringBuilder sqlBuilder = new StringBuilder();
                List<clsBillDtl> listOfDelBoyIncentives = new ArrayList<>();

                //Q Data
                sqlBuilder.setLength(0);
                sqlBuilder.append("select e.strDPCode,e.strDPName,sum(c.dblSubTotal)dblSubTotal,sum(b.dblDBIncentives) dblDBIncentives, "
                        + "a.strBillNo,date(c.dteBillDate) as BillDate, ifnull(DATE(f.dteOrderFor),'') AS OrderDate\n" 
                        + ",g.strCustomerName "
                        + "from tblhomedelivery a,tblhomedeldtl b,tblqbillhd c left outer join tbladvbookbillhd f on c.strAdvBookingNo=f.strAdvBookingNo,"
                        + "tblareawisedelboywisecharges d,tbldeliverypersonmaster e "
                        + ",tblcustomermaster g "
                        + "where date(c.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
                        + "and a.strBillNo=b.strBillNo  "
                        + " and date(a.dteDate)=date(b.dteBillDate) "
                        + "and a.strBillNo=c.strBillNo"
                        + " and date(a.dteDate)=date(c.dteBillDate) "
                        + "and b.strDPCode= d.strDeliveryBoyCode "
                        + "and d.strDeliveryBoyCode=e.strDPCode "
                        + "and c.strCustomerCode=g.strCustomerCode "
                        + "and g.strBuldingCode=d.strCustAreaCode ");
                if (!posCode.equalsIgnoreCase("All"))
                {
                    sqlBuilder.append("and c.strPOSCode='" + posCode + "' ");
                }
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sqlBuilder.append("and c.intShiftCode='" + shiftNo + "' ");
                }
                if (!dpCode.equalsIgnoreCase("All"))
                {
                    sqlBuilder.append("and b.strDPCode='" + dpCode + "' ");
                }
                sqlBuilder.append("group by e.strDPCode "
                        + "order by e.strDPCode ");

                ResultSet rsWaiterWiseItemSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
                while (rsWaiterWiseItemSales.next())
                {
                    clsBillDtl obj = new clsBillDtl();

                    obj.setStrDelBoyCode(rsWaiterWiseItemSales.getString(1));
                    obj.setStrDelBoyName(rsWaiterWiseItemSales.getString(2));
                    obj.setDblAmount(rsWaiterWiseItemSales.getDouble(3));
                    obj.setDblIncentive(rsWaiterWiseItemSales.getDouble(4));
                    obj.setStrBillNo(rsWaiterWiseItemSales.getString(5));
                    obj.setDteBillDate(rsWaiterWiseItemSales.getString(5));
                    obj.setDteOrderDate(rsWaiterWiseItemSales.getString(7));
                    obj.setStrCustomerName(rsWaiterWiseItemSales.getString(8));
                    listOfDelBoyIncentives.add(obj);
                }
                rsWaiterWiseItemSales.close();

                //Live Data
                sqlBuilder.setLength(0);
                sqlBuilder.append("select e.strDPCode,e.strDPName,sum(c.dblSubTotal)dblSubTotal,sum(b.dblDBIncentives) dblDBIncentives, "
                        + "a.strBillNo,date(c.dteBillDate) as BillDate, ifnull(DATE(f.dteOrderFor),'') AS OrderDate\n" 
                        + ",g.strCustomerName "
                        + "from tblhomedelivery a,tblhomedeldtl b,tblbillhd c left outer join tbladvbookbillhd f on c.strAdvBookingNo=f.strAdvBookingNo,"
                        + "tblareawisedelboywisecharges d,tbldeliverypersonmaster e "
                        + ",tblcustomermaster g "
                        + "where date(c.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
                        + "and a.strBillNo=b.strBillNo  "
                        + "and a.strBillNo=c.strBillNo "
                        + "and b.strDPCode= d.strDeliveryBoyCode "
                        + "and d.strDeliveryBoyCode=e.strDPCode "
                        + "and c.strCustomerCode=g.strCustomerCode "
                        + "and g.strBuldingCode=d.strCustAreaCode ");
                if (!posCode.equalsIgnoreCase("All"))
                {
                    sqlBuilder.append("and c.strPOSCode='" + posCode + "' ");
                }
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sqlBuilder.append("and c.intShiftCode='" + shiftNo + "' ");
                }
                if (!dpCode.equalsIgnoreCase("All"))
                {
                    sqlBuilder.append("and b.strDPCode='" + dpCode + "' ");
                }
                sqlBuilder.append("group by e.strDPCode "
                        + "order by e.strDPCode ");

                rsWaiterWiseItemSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
                while (rsWaiterWiseItemSales.next())
                {
                    clsBillDtl obj = new clsBillDtl();

                    obj.setStrDelBoyCode(rsWaiterWiseItemSales.getString(1));
                    obj.setStrDelBoyName(rsWaiterWiseItemSales.getString(2));
                    obj.setDblAmount(rsWaiterWiseItemSales.getDouble(3));
                    obj.setDblIncentive(rsWaiterWiseItemSales.getDouble(4));
                    obj.setStrBillNo(rsWaiterWiseItemSales.getString(5));
                    obj.setDteBillDate(rsWaiterWiseItemSales.getString(5));
                    obj.setDteOrderDate(rsWaiterWiseItemSales.getString(7));
                    obj.setStrCustomerName(rsWaiterWiseItemSales.getString(8));

                    listOfDelBoyIncentives.add(obj);
                }
                rsWaiterWiseItemSales.close();

                Comparator<clsBillDtl> delBoyCodeComparator = new Comparator<clsBillDtl>()
                {

                    @Override
                    public int compare(clsBillDtl o1, clsBillDtl o2)
                    {
                        return o1.getStrDelBoyCode().compareTo(o2.getStrDelBoyCode());
                    }
                };

                Collections.sort(listOfDelBoyIncentives, new clsWaiterWiseSalesComparator(delBoyCodeComparator));
                //call for view report
                if(reportType.equalsIgnoreCase("A4 Size Report"))
                {
                funViewJasperReportForBeanCollectionDataSource(is, hm, listOfDelBoyIncentives);
                }
                if(reportType.equalsIgnoreCase("Excel Report"))
                {
                   int i = 1;
                for(clsBillDtl objBean:listOfDelBoyIncentives)
                {
                    List<String> arrListItem = new ArrayList<String>();
                    arrListItem.add(objBean.getStrBillNo());
                    arrListItem.add(objBean.getDteBillDate());
                    arrListItem.add(objBean.getDteOrderDate());
                    arrListItem.add(objBean.getStrCustomerName());
                    arrListItem.add(objBean.getStrDelBoyName());
                    arrListItem.add(String.valueOf(gDecimalFormat.format(objBean.getDblAmount())));
                    arrListItem.add(String.valueOf(gDecimalFormat.format(objBean.getDblIncentive())));

                    totalAmount = totalAmount + Double.parseDouble(String.valueOf(objBean.getDblAmount()));
                    totalIncentiveAmt = totalIncentiveAmt + Double.parseDouble(String.valueOf(objBean.getDblIncentive()));

                    mapExcelItemDtl.put(i, arrListItem);

                    i++;

                }

                arrListTotal.add(String.valueOf(gDecimalFormat.format(totalAmount)) + "#" + "6");
                arrListTotal.add(String.valueOf(gDecimalFormat.format(totalIncentiveAmt)) + "#" + "7");

                arrHeaderList.add("Serial No");
                arrHeaderList.add("Bill No");
                arrHeaderList.add("Bill Date");
                arrHeaderList.add("Order Date");
                arrHeaderList.add("Customer Name");
                arrHeaderList.add("DP Name");
                arrHeaderList.add("Amount");
                arrHeaderList.add("Incentive Amt");

                List<String> arrparameterList = new ArrayList<String>();
                arrparameterList.add("Delivery Boy Incentives Summary Report");
                arrparameterList.add("POS" + " : " + posName);
                arrparameterList.add("FromDate" + " : " + fromDate);
                arrparameterList.add("ToDate" + " : " + toDate);
                arrparameterList.add("DP Name" + " : " + dpName);
                arrparameterList.add(" ");

                funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "deleveryBoyIncentiveSummaryExcelSheet",dayEnd);
 
                }
            }
            else
            {
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptDeliveryBoyIncentiveDetailReport.jasper");
                StringBuilder sqlBuilder = new StringBuilder();
                List<clsBillDtl> listOfDelBoyIncentives = new ArrayList<>();

                //Q Data
                sqlBuilder.setLength(0);
                sqlBuilder.append("select e.strDPCode,e.strDPName,a.strBillNo,date(c.dteBillDate) as dteBillDate,TIME_FORMAT(time(c.dteBillDate),\"%r\") as tmeBillTime "
                        + ",ifnull(h.strBuildingName,'') as strArea,ifnull(date(c.dteSettleDate),'') as dteSettleDate "
                        + ",ifnull(TIME_FORMAT(time(c.dteSettleDate),\"%r\"),'') as tmeSettleTime,sum(b.dblDBIncentives)dblDBIncentives "
                        + "from tblhomedelivery a,tblhomedeldtl b,tblqbillhd c "
                        + ",tblareawisedelboywisecharges d,tbldeliverypersonmaster e "
                        + ",tblcustomermaster g "
                        + "left outer join tblbuildingmaster h on g.strBuldingCode=h.strBuildingCode "
                        + "where date(c.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
                        + "and a.strBillNo=b.strBillNo   "
                        + "and a.strBillNo=c.strBillNo  "
                        + "and b.strDPCode= d.strDeliveryBoyCode "
                        + "and d.strDeliveryBoyCode=e.strDPCode  "
                        + "and c.strCustomerCode=g.strCustomerCode "
                        + "and g.strBuldingCode=d.strCustAreaCode ");
                if (!posCode.equalsIgnoreCase("All"))
                {
                    sqlBuilder.append("and c.strPOSCode='" + posCode + "' ");
                }
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sqlBuilder.append("and c.intShiftCode='" + shiftNo + "' ");
                }
                if (!dpCode.equalsIgnoreCase("All"))
                {
                    sqlBuilder.append("and b.strDPCode='" + dpCode + "' ");
                }
                sqlBuilder.append("group by e.strDPCode,c.strBillNo "
                        + "order by e.strDPCode,c.strBillNo ");

                ResultSet rsWaiterWiseItemSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
                while (rsWaiterWiseItemSales.next())
                {
                    clsBillDtl obj = new clsBillDtl();

                    obj.setStrDelBoyCode(rsWaiterWiseItemSales.getString(1));
                    obj.setStrDelBoyName(rsWaiterWiseItemSales.getString(2));
                    obj.setStrBillNo(rsWaiterWiseItemSales.getString(3));

                    obj.setDteBillDate(rsWaiterWiseItemSales.getString(4));
                    obj.setTmeBillTime(rsWaiterWiseItemSales.getString(5));
                    obj.setStrArea(rsWaiterWiseItemSales.getString(6));

                    obj.setDteBillSettleDate(rsWaiterWiseItemSales.getString(7));
                    obj.setTmeBillTime(rsWaiterWiseItemSales.getString(8));
                    obj.setDblIncentive(rsWaiterWiseItemSales.getDouble(9));

                    listOfDelBoyIncentives.add(obj);
                }
                rsWaiterWiseItemSales.close();

                //Live Data
                sqlBuilder.setLength(0);
                sqlBuilder.append("select e.strDPCode,e.strDPName,a.strBillNo,date(c.dteBillDate) as dteBillDate,TIME_FORMAT(time(c.dteBillDate),\"%r\") as tmeBillTime "
                        + ",ifnull(h.strBuildingName,'') as strArea,ifnull(date(c.dteSettleDate),'') as dteSettleDate "
                        + ",ifnull(TIME_FORMAT(time(c.dteSettleDate),\"%r\"),'') as tmeSettleTime,sum(b.dblDBIncentives)dblDBIncentives "
                        + "from tblhomedelivery a,tblhomedeldtl b,tblbillhd c "
                        + ",tblareawisedelboywisecharges d,tbldeliverypersonmaster e "
                        + ",tblcustomermaster g "
                        + "left outer join tblbuildingmaster h on g.strBuldingCode=h.strBuildingCode "
                        + "where date(c.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
                        + "and a.strBillNo=b.strBillNo   "
                        + "and a.strBillNo=c.strBillNo  "
                        + "and b.strDPCode= d.strDeliveryBoyCode "
                        + "and d.strDeliveryBoyCode=e.strDPCode  "
                        + "and c.strCustomerCode=g.strCustomerCode "
                        + "and g.strBuldingCode=d.strCustAreaCode ");
                if (!posCode.equalsIgnoreCase("All"))
                {
                    sqlBuilder.append("and c.strPOSCode='" + posCode + "' ");
                }
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sqlBuilder.append("and c.intShiftCode='" + shiftNo + "' ");
                }
                if (!dpCode.equalsIgnoreCase("All"))
                {
                    sqlBuilder.append("and b.strDPCode='" + dpCode + "' ");
                }
                sqlBuilder.append("group by e.strDPCode,c.strBillNo "
                        + "order by e.strDPCode,c.strBillNo ");

                rsWaiterWiseItemSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
                while (rsWaiterWiseItemSales.next())
                {
                    clsBillDtl obj = new clsBillDtl();

                    obj.setStrDelBoyCode(rsWaiterWiseItemSales.getString(1));
                    obj.setStrDelBoyName(rsWaiterWiseItemSales.getString(2));
                    obj.setStrBillNo(rsWaiterWiseItemSales.getString(3));

                    obj.setDteBillDate(rsWaiterWiseItemSales.getString(4));
                    obj.setTmeBillTime(rsWaiterWiseItemSales.getString(5));
                    obj.setStrArea(rsWaiterWiseItemSales.getString(6));

                    obj.setDteBillSettleDate(rsWaiterWiseItemSales.getString(7));
                    obj.setTmeBillTime(rsWaiterWiseItemSales.getString(8));
                    obj.setDblIncentive(rsWaiterWiseItemSales.getDouble(9));

                    listOfDelBoyIncentives.add(obj);
                }
                rsWaiterWiseItemSales.close();

                Comparator<clsBillDtl> delBoyCodeComparator = new Comparator<clsBillDtl>()
                {

                    @Override
                    public int compare(clsBillDtl o1, clsBillDtl o2)
                    {
                        return o1.getStrDelBoyCode().compareTo(o2.getStrDelBoyCode());
                    }
                };

                Comparator<clsBillDtl> billNoComparator = new Comparator<clsBillDtl>()
                {

                    @Override
                    public int compare(clsBillDtl o1, clsBillDtl o2)
                    {
                        return o1.getStrBillNo().compareTo(o2.getStrBillNo());
                    }
                };

                Collections.sort(listOfDelBoyIncentives, new clsWaiterWiseSalesComparator(delBoyCodeComparator, billNoComparator));
                //call for view report
                if(reportType.equalsIgnoreCase("A4 Size Report"))
                {
                funViewJasperReportForBeanCollectionDataSource(is, hm, listOfDelBoyIncentives);
                }
                if(reportType.equalsIgnoreCase("Excel Report"))
                {
                    int i = 1;
                    for(clsBillDtl objBean:listOfDelBoyIncentives)
                    {
                        List<String> arrListItem = new ArrayList<String>();
                        arrListItem.add(objBean.getStrBillNo());
                        arrListItem.add(objBean.getDteBillDate());
                        arrListItem.add(objBean.getTmeBillTime());
                        arrListItem.add(objBean.getStrDelBoyName());
                        arrListItem.add(String.valueOf(gDecimalFormat.format(objBean.getDblIncentive())));
                        arrListItem.add(objBean.getStrArea());
                        arrListItem.add(objBean.getDteBillSettleDate());

                        totalIncentiveAmt = totalIncentiveAmt + Double.parseDouble(String.valueOf(objBean.getDblIncentive()));

                        mapExcelItemDtl.put(i, arrListItem);

                        i++;

                    }

                    arrListTotal.add(String.valueOf(gDecimalFormat.format(totalIncentiveAmt)) + "#" + "5");

                    arrHeaderList.add("Serial No");
                    arrHeaderList.add("Bill No");
                    arrHeaderList.add("Bill Date ");
                    arrHeaderList.add("Bill Time");
                    arrHeaderList.add("DP Name");
                    arrHeaderList.add("Amount");
                    arrHeaderList.add("Area");
                    arrHeaderList.add("Settle Date");

                    List<String> arrparameterList = new ArrayList<String>();
                    arrparameterList.add("Delivery Boy Incentives Details Report");
                    arrparameterList.add("POS" + " : " + posName);
                    arrparameterList.add("FromDate" + " : " + fromDate);
                    arrparameterList.add("ToDate" + " : " + toDate);
                    arrparameterList.add("DP Name" + " : " + dpName);
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

                    funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "deleveryBoyIncentiveDtlExcelSheet",dayEnd);

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
     
      public void funCreateExcelSheet(List<String> parameterList, List<String> headerList, Map<Integer, List<String>> map, List<String> totalList, String fileName,String dayEnd)
    {
        String filePath = System.getProperty("user.dir");
        File file = new File(filePath + File.separator+"Reports"+File.separator+ fileName + ".xls");
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
