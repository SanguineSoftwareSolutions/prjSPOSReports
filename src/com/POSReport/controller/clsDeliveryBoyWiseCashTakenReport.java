package com.POSReport.controller;

import com.POSReport.controller.comparator.clsWaiterWiseSalesComparator;
import com.POSGlobal.controller.clsBillDtl;
import com.POSGlobal.controller.clsGlobalVarClass;
import java.awt.Dimension;
import java.io.InputStream;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.swing.JRViewer;

public class clsDeliveryBoyWiseCashTakenReport 
{
    public void funDeliveryBoyWiseCashTakenReport(String reportType, HashMap hm)
    {
        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptDeliveryBoyWiseCashTaken.jasper");

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();

            String dpName = hm.get("DPName").toString();
            String dpCode = hm.get("DPCode").toString();

            Map<String, List<clsBillDtl>> mapDeliBoyWiseBillDtl = new HashMap<>();

            StringBuilder sqlBuilder = new StringBuilder();

            //Q Data
            sqlBuilder.setLength(0);
            sqlBuilder.append("select a.strBillNo,DATE_FORMAT(date(a.dteBillDate),'%d-%m-%Y')dteBillDate,c.strDPCode,d.strDPName "
                    + ",a.dblGrandTotal,b.dblLooseCashAmt,a.strUserEdited,e.strCustomerCode,e.strCustomerName,ifnull(e.strBuildingName,'') strBuildingName "
                    + "from tblqbillhd a,tblhomedelivery b,tblhomedeldtl c,tbldeliverypersonmaster d,tblcustomermaster e "
                    + "where a.strBillNo=b.strBillNo "
                    + "and b.strBillNo=c.strBillNo "
                    + "and a.strCustomerCode=e.strCustomerCode "
                    + "and c.strDPCode=d.strDPCode "
                    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");

            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and a.strPOSCode='" + posCode + "' ");
            }
            if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
            {
                sqlBuilder.append("and a.intShiftCode='" + shiftNo + "' ");
            }
            if (!dpCode.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and d.strDPCode='" + dpCode + "' ");
            }

            ResultSet rsWaiterWiseItemSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
            while (rsWaiterWiseItemSales.next())
            {
                String billNo = rsWaiterWiseItemSales.getString(1);//billNo
                String deliveryBoyName = rsWaiterWiseItemSales.getString(4);//delivery boy name               

                if (mapDeliBoyWiseBillDtl.containsKey(billNo))
                {
                    List<clsBillDtl> listOfDelBoyWiseCashTaken = mapDeliBoyWiseBillDtl.get(billNo);

                    clsBillDtl obj = new clsBillDtl();

                    obj.setStrBillNo(billNo);
                    obj.setDteBillDate(rsWaiterWiseItemSales.getString(2));
                    obj.setStrDelBoyCode(rsWaiterWiseItemSales.getString(3));
                    obj.setStrDelBoyName(rsWaiterWiseItemSales.getString(4));
                    obj.setDblAmount(0.00);
                    obj.setDblCashTakenAmt(rsWaiterWiseItemSales.getDouble(6));
                    obj.setStrUserCreated(rsWaiterWiseItemSales.getString(7));
                    obj.setStrCustomerCode(rsWaiterWiseItemSales.getString(8));
                    obj.setStrCustomerName(rsWaiterWiseItemSales.getString(9));
                    obj.setStrArea(rsWaiterWiseItemSales.getString(10));

                    listOfDelBoyWiseCashTaken.add(obj);
                }
                else
                {
                    List<clsBillDtl> listOfDelBoyWiseCashTaken = new ArrayList<>();

                    clsBillDtl obj = new clsBillDtl();

                    obj.setStrBillNo(billNo);
                    obj.setDteBillDate(rsWaiterWiseItemSales.getString(2));
                    obj.setStrDelBoyCode(rsWaiterWiseItemSales.getString(3));
                    obj.setStrDelBoyName(rsWaiterWiseItemSales.getString(4));
                    obj.setDblAmount(rsWaiterWiseItemSales.getDouble(5));
                    obj.setDblCashTakenAmt(rsWaiterWiseItemSales.getDouble(6));
                    obj.setStrUserCreated(rsWaiterWiseItemSales.getString(7));
                    obj.setStrCustomerCode(rsWaiterWiseItemSales.getString(8));
                    obj.setStrCustomerName(rsWaiterWiseItemSales.getString(9));
                    obj.setStrArea(rsWaiterWiseItemSales.getString(10));

                    listOfDelBoyWiseCashTaken.add(obj);

                    mapDeliBoyWiseBillDtl.put(billNo, listOfDelBoyWiseCashTaken);
                }
            }
            rsWaiterWiseItemSales.close();

            //Live Data
            sqlBuilder.setLength(0);
            sqlBuilder.append("select a.strBillNo,DATE_FORMAT(date(a.dteBillDate),'%d-%m-%Y')dteBillDate,c.strDPCode,d.strDPName "
                    + ",a.dblGrandTotal,b.dblLooseCashAmt,a.strUserEdited,e.strCustomerCode,e.strCustomerName,ifnull(e.strBuildingName,'') strBuildingName "
                    + "from tblbillhd a,tblhomedelivery b,tblhomedeldtl c,tbldeliverypersonmaster d,tblcustomermaster e "
                    + "where a.strBillNo=b.strBillNo "
                    + "and b.strBillNo=c.strBillNo "
                    + "and a.strCustomerCode=e.strCustomerCode "
                    + "and c.strDPCode=d.strDPCode "
                    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");

            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and a.strPOSCode='" + posCode + "' ");
            }
            if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
            {
                sqlBuilder.append("and a.intShiftCode='" + shiftNo + "' ");
            }
            if (!dpCode.equalsIgnoreCase("All"))
            {
                sqlBuilder.append("and d.strDPCode='" + dpCode + "' ");
            }

            rsWaiterWiseItemSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());
            while (rsWaiterWiseItemSales.next())
            {
                String billNo = rsWaiterWiseItemSales.getString(1);//billNo
                String deliveryBoyName = rsWaiterWiseItemSales.getString(4);//delivery boy name               

                if (mapDeliBoyWiseBillDtl.containsKey(billNo))
                {
                    List<clsBillDtl> listOfDelBoyWiseCashTaken = mapDeliBoyWiseBillDtl.get(billNo);

                    clsBillDtl obj = new clsBillDtl();

                    obj.setStrBillNo(billNo);
                    obj.setDteBillDate(rsWaiterWiseItemSales.getString(2));
                    obj.setStrDelBoyCode(rsWaiterWiseItemSales.getString(3));
                    obj.setStrDelBoyName(rsWaiterWiseItemSales.getString(4));
                    obj.setDblAmount(0.00);
                    obj.setDblCashTakenAmt(rsWaiterWiseItemSales.getDouble(6));
                    obj.setStrUserCreated(rsWaiterWiseItemSales.getString(7));
                    obj.setStrCustomerCode(rsWaiterWiseItemSales.getString(8));
                    obj.setStrCustomerName(rsWaiterWiseItemSales.getString(9));
                    obj.setStrArea(rsWaiterWiseItemSales.getString(10));

                    listOfDelBoyWiseCashTaken.add(obj);
                }
                else
                {
                    List<clsBillDtl> listOfDelBoyWiseCashTaken = new ArrayList<>();

                    clsBillDtl obj = new clsBillDtl();

                    obj.setStrBillNo(billNo);
                    obj.setDteBillDate(rsWaiterWiseItemSales.getString(2));
                    obj.setStrDelBoyCode(rsWaiterWiseItemSales.getString(3));
                    obj.setStrDelBoyName(rsWaiterWiseItemSales.getString(4));
                    obj.setDblAmount(rsWaiterWiseItemSales.getDouble(5));
                    obj.setDblCashTakenAmt(rsWaiterWiseItemSales.getDouble(6));
                    obj.setStrUserCreated(rsWaiterWiseItemSales.getString(7));
                    obj.setStrCustomerCode(rsWaiterWiseItemSales.getString(8));
                    obj.setStrCustomerName(rsWaiterWiseItemSales.getString(9));
                    obj.setStrArea(rsWaiterWiseItemSales.getString(10));

                    listOfDelBoyWiseCashTaken.add(obj);

                    mapDeliBoyWiseBillDtl.put(billNo, listOfDelBoyWiseCashTaken);
                }
            }
            rsWaiterWiseItemSales.close();

            Comparator<clsBillDtl> delBoyCodeComparator = new Comparator<clsBillDtl>()
            {

                @Override
                public int compare(clsBillDtl o1, clsBillDtl o2)
                {
                    return o1.getStrDelBoyName().compareTo(o2.getStrDelBoyName());
                }
            };

            List<clsBillDtl> listOfDelBoyWiseCashTaken = new LinkedList();

            for (List<clsBillDtl> listOfDelBoyWiseCashTakenTemp : mapDeliBoyWiseBillDtl.values())
            {
                listOfDelBoyWiseCashTaken.addAll(listOfDelBoyWiseCashTakenTemp);
            }

            Collections.sort(listOfDelBoyWiseCashTaken, new clsWaiterWiseSalesComparator(delBoyCodeComparator));
            //call for view report
            if(reportType.equalsIgnoreCase("A4 Size Report"))
            {
            funViewJasperReportForBeanCollectionDataSource(is, hm, listOfDelBoyWiseCashTaken);
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

}
