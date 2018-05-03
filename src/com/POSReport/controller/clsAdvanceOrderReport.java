package com.POSReport.controller;

import com.POSGlobal.controller.clsAdvOrderItemDtl;
import com.POSGlobal.controller.clsGlobalVarClass;
import java.awt.Dimension;
import java.io.InputStream;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.swing.JRViewer;

public class clsAdvanceOrderReport 
{
    public void funAdvanceOrderReport(String reportType, HashMap hm)
    {
        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptAdvanceOrderReport.jasper");

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String orderType = hm.get("orderType").toString();

            List<clsAdvOrderItemDtl> listOdAdvOrderItemDtl = new ArrayList<>();
            StringBuilder sqlBuilder = new StringBuilder();
            //for live data
            sqlBuilder.setLength(0);
            sqlBuilder.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y') as dteAdvBookingDate,DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y') as  dteOrderFor,ifnull(f.strCustomerName,'NA') as strCustomerName ,f.longMobileNo "
                    + ",b.strItemCode,b.strItemName,d.strCharCode,ifnull(d.strCharName,'') as strCharName,ifnull(c.strCharValues,'') as strCharValues, "
                    + "sum(b.dblQuantity) as dblQuantity ,sum(b.dblAmount)/sum(b.dblQuantity) as dblRate,sum(b.dblAmount) as dblAmount,sum(b.dblWeight) as dblWeight, "
                    + "e.dblAdvDeposite "
                    + "from tbladvbookbillhd a "
                    + "inner join tbladvbookbilldtl b on a.strAdvBookingNo=b.strAdvBookingNo and a.strClientCode=b.strClientCode "
                    + "left outer join tbladvbookbillchardtl c on a.strAdvBookingNo=c.strAdvBookingNo and b.strItemCode=c.strItemCode and a.strClientCode=c.strClientCode "
                    + "left outer join tblcharactersticsmaster d on c.strCharCode=d.strCharCode and c.strClientCode=d.strClientCode "
                    + "left outer join tbladvancereceipthd e on a.strAdvBookingNo=e.strAdvBookingNo and a.strClientCode=e.strClientCode "
                    + "left outer join tblcustomermaster f on a.strCustomerCode=f.strCustomerCode and a.strClientCode=f.strClientCode "
                    + "where date(a.dteOrderFor) between '" + fromDate + "' and '" + toDate + "' ");
            if (!posCode.equals("All"))
            {
                sqlBuilder.append(" and a.strPOSCode='" + posCode + "'  ");
            }
            if (!orderType.equalsIgnoreCase("All") && orderType.equalsIgnoreCase("Advance Order"))
            {
                sqlBuilder.append(" and a.strUrgentOrder='N'  ");
            }
            else if (!orderType.equalsIgnoreCase("All") && orderType.equalsIgnoreCase("Urgent Order"))
            {
                sqlBuilder.append(" and a.strUrgentOrder='Y'  ");
            }
            sqlBuilder.append("group by a.strAdvBookingNo,b.strItemCode,c.strCharCode,c.strCharValues "
                    + "order by a.strAdvBookingNo,b.strItemCode,c.strCharCode,c.strCharValues ");

            ResultSet rsAdvOrderDtl = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());

            // listOdAdvOrderItemDtl=funGetAdvOrderList(rsAdvOrderDtl,listOdAdvOrderItemDtl);
            String itemCode = "", advOrderNO = "";
            clsAdvOrderItemDtl objAdvOrderItemDtl = null;
            boolean flag = false;
            double totalAdvAmt = 0.00;
            while (rsAdvOrderDtl.next())
            {
                if (itemCode.equals(rsAdvOrderDtl.getString("strItemCode")) && advOrderNO.equals(rsAdvOrderDtl.getString("strAdvBookingNo")))
                {
                    objAdvOrderItemDtl.setStrCharNameValuePair(objAdvOrderItemDtl.getStrCharNameValuePair() + "  " + rsAdvOrderDtl.getString("strCharName") + "-->" + rsAdvOrderDtl.getString("strCharValues"));
                }
                else
                {
                    objAdvOrderItemDtl = new clsAdvOrderItemDtl();

                    if (advOrderNO == "")
                    {
                        totalAdvAmt += rsAdvOrderDtl.getDouble("dblAdvDeposite");
                    }
                    else if (advOrderNO.equals(rsAdvOrderDtl.getString("strAdvBookingNo")))
                    {
                    }
                    else if (!advOrderNO.equals(rsAdvOrderDtl.getString("strAdvBookingNo")))
                    {
                        totalAdvAmt += rsAdvOrderDtl.getDouble("dblAdvDeposite");
                    }
                    itemCode = rsAdvOrderDtl.getString("strItemCode");
                    advOrderNO = rsAdvOrderDtl.getString("strAdvBookingNo");

                    objAdvOrderItemDtl.setStrAdvBookingNo(rsAdvOrderDtl.getString("strAdvBookingNo"));
                    objAdvOrderItemDtl.setDteAdvBookingDate(rsAdvOrderDtl.getString("dteAdvBookingDate"));
                    objAdvOrderItemDtl.setDteOrderFor(rsAdvOrderDtl.getString("dteOrderFor"));
                    objAdvOrderItemDtl.setStrCustomerName(rsAdvOrderDtl.getString("strCustomerName"));
                    objAdvOrderItemDtl.setLongMobileNo(rsAdvOrderDtl.getString("longMobileNo"));
                    objAdvOrderItemDtl.setStrItemCode(rsAdvOrderDtl.getString("strItemCode"));
                    objAdvOrderItemDtl.setStrItemName(rsAdvOrderDtl.getString("strItemName"));
                    objAdvOrderItemDtl.setStrCharCode(rsAdvOrderDtl.getString("strCharCode"));
                    objAdvOrderItemDtl.setStrCharName(rsAdvOrderDtl.getString("strCharName"));
                    objAdvOrderItemDtl.setStrCharValues(rsAdvOrderDtl.getString("strCharValues"));
                    objAdvOrderItemDtl.setDblQuantity(rsAdvOrderDtl.getDouble("dblQuantity"));
                    objAdvOrderItemDtl.setDblRate(rsAdvOrderDtl.getDouble("dblRate"));
                    objAdvOrderItemDtl.setDblAmount(rsAdvOrderDtl.getDouble("dblAmount"));
                    objAdvOrderItemDtl.setDblWeight(rsAdvOrderDtl.getDouble("dblWeight"));
                    objAdvOrderItemDtl.setDblAdvDeposite(rsAdvOrderDtl.getDouble("dblAdvDeposite"));
                    objAdvOrderItemDtl.setDblTotalAmount(rsAdvOrderDtl.getDouble("dblAmount"));
                    objAdvOrderItemDtl.setStrCharNameValuePair(rsAdvOrderDtl.getString("strCharName") + "-->" + rsAdvOrderDtl.getString("strCharValues"));

                    listOdAdvOrderItemDtl.add(objAdvOrderItemDtl);
                }
            }

//                 //for q data
            sqlBuilder.setLength(0);
            sqlBuilder.append("select a.strAdvBookingNo,DATE_FORMAT(a.dteAdvBookingDate,'%d-%m-%Y') as dteAdvBookingDate,DATE_FORMAT(a.dteOrderFor,'%d-%m-%Y') as  dteOrderFor,ifnull(f.strCustomerName,'NA') as strCustomerName ,f.longMobileNo "
                    + ",b.strItemCode,b.strItemName,d.strCharCode,ifnull(d.strCharName,'') as strCharName,ifnull(c.strCharValues,'') as strCharValues, "
                    + "sum(b.dblQuantity) as dblQuantity ,sum(b.dblAmount)/sum(b.dblQuantity) as dblRate,sum(b.dblAmount) as dblAmount,sum(b.dblWeight) as dblWeight, "
                    + "e.dblAdvDeposite "
                    + "from tblqadvbookbillhd a "
                    + "inner join tblqadvbookbilldtl b on a.strAdvBookingNo=b.strAdvBookingNo and a.strClientCode=b.strClientCode "
                    + "left outer join tblqadvbookbillchardtl c on a.strAdvBookingNo=c.strAdvBookingNo and b.strItemCode=c.strItemCode and a.strClientCode=c.strClientCode "
                    + "left outer join tblcharactersticsmaster d on c.strCharCode=d.strCharCode and c.strClientCode=d.strClientCode "
                    + "left outer join tblqadvancereceipthd e on a.strAdvBookingNo=e.strAdvBookingNo and a.strClientCode=e.strClientCode "
                    + "left outer join tblcustomermaster f on a.strCustomerCode=f.strCustomerCode and a.strClientCode=f.strClientCode "
                    + "where date(a.dteOrderFor) between '" + fromDate + "' and '" + toDate + "' ");
            if (!posCode.equals("All"))
            {
                sqlBuilder.append(" and a.strPOSCode='" + posCode + "'  ");
            }
            if (!orderType.equalsIgnoreCase("All") && orderType.equalsIgnoreCase("Advance Order"))
            {
                sqlBuilder.append(" and a.strUrgentOrder='N'  ");
            }
            else if (!orderType.equalsIgnoreCase("All") && orderType.equalsIgnoreCase("Urgent Order"))
            {
                sqlBuilder.append(" and a.strUrgentOrder='Y'  ");
            }
            sqlBuilder.append("group by a.strAdvBookingNo,b.strItemCode,c.strCharCode,c.strCharValues "
                    + "order by a.strAdvBookingNo,b.strItemCode,c.strCharCode,c.strCharValues ");

            rsAdvOrderDtl = clsGlobalVarClass.dbMysql.executeResultSet(sqlBuilder.toString());

            //listOdAdvOrderItemDtl=funGetAdvOrderList(rsAdvOrderDtl,listOdAdvOrderItemDtl);
            itemCode = "";
            advOrderNO = "";
            while (rsAdvOrderDtl.next())
            {
                if (itemCode.equals(rsAdvOrderDtl.getString("strItemCode")) && advOrderNO.equals(rsAdvOrderDtl.getString("strAdvBookingNo")))
                {
                    objAdvOrderItemDtl.setStrCharNameValuePair(objAdvOrderItemDtl.getStrCharNameValuePair() + "  " + rsAdvOrderDtl.getString("strCharName") + "-->" + rsAdvOrderDtl.getString("strCharValues"));
                }
                else
                {
                    objAdvOrderItemDtl = new clsAdvOrderItemDtl();
                    if (advOrderNO == "")
                    {
                        totalAdvAmt += rsAdvOrderDtl.getDouble("dblAdvDeposite");
                    }
                    else if (advOrderNO.equals(rsAdvOrderDtl.getString("strAdvBookingNo")))
                    {
                    }
                    else if (!advOrderNO.equals(rsAdvOrderDtl.getString("strAdvBookingNo")))
                    {
                        totalAdvAmt += rsAdvOrderDtl.getDouble("dblAdvDeposite");
                    }
                    itemCode = rsAdvOrderDtl.getString("strItemCode");
                    advOrderNO = rsAdvOrderDtl.getString("strAdvBookingNo");

                    objAdvOrderItemDtl.setStrAdvBookingNo(rsAdvOrderDtl.getString("strAdvBookingNo"));
                    objAdvOrderItemDtl.setDteAdvBookingDate(rsAdvOrderDtl.getString("dteAdvBookingDate"));
                    objAdvOrderItemDtl.setDteOrderFor(rsAdvOrderDtl.getString("dteOrderFor"));
                    objAdvOrderItemDtl.setStrCustomerName(rsAdvOrderDtl.getString("strCustomerName"));
                    objAdvOrderItemDtl.setLongMobileNo(rsAdvOrderDtl.getString("longMobileNo"));
                    objAdvOrderItemDtl.setStrItemCode(rsAdvOrderDtl.getString("strItemCode"));
                    objAdvOrderItemDtl.setStrItemName(rsAdvOrderDtl.getString("strItemName"));
                    objAdvOrderItemDtl.setStrCharCode(rsAdvOrderDtl.getString("strCharCode"));
                    objAdvOrderItemDtl.setStrCharName(rsAdvOrderDtl.getString("strCharName"));
                    objAdvOrderItemDtl.setStrCharValues(rsAdvOrderDtl.getString("strCharValues"));
                    objAdvOrderItemDtl.setDblQuantity(rsAdvOrderDtl.getDouble("dblQuantity"));
                    objAdvOrderItemDtl.setDblRate(rsAdvOrderDtl.getDouble("dblRate"));
                    objAdvOrderItemDtl.setDblAmount(rsAdvOrderDtl.getDouble("dblAmount"));
                    objAdvOrderItemDtl.setDblWeight(rsAdvOrderDtl.getDouble("dblWeight"));
                    objAdvOrderItemDtl.setDblAdvDeposite(rsAdvOrderDtl.getDouble("dblAdvDeposite"));
                    objAdvOrderItemDtl.setDblTotalAmount(rsAdvOrderDtl.getDouble("dblAmount"));
                    objAdvOrderItemDtl.setStrCharNameValuePair(rsAdvOrderDtl.getString("strCharName") + "-->" + rsAdvOrderDtl.getString("strCharValues"));

                    listOdAdvOrderItemDtl.add(objAdvOrderItemDtl);
                }

            }
            //call for view report
            if(reportType.equalsIgnoreCase("A4 Size Report"))
            {
            funViewJasperReportForBeanCollectionDataSource(is, hm, listOdAdvOrderItemDtl);
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
