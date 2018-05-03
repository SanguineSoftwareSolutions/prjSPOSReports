package com.POSReport.controller;

import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsPlaceOrderDtl;
import java.awt.Dimension;
import java.io.InputStream;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.swing.JRViewer;

public class clsPlacedOrderReport 
{
    private HashMap hm;
    public void funPostingReport(String reportType, HashMap hm)
    {
        try
        {
//            InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String orderType = hm.get("orderType").toString();
            String orderCode = hm.get("orderCode").toString();
            String orderName = hm.get("orderName").toString();

            String sqlFilters = "";
            int count = 1, cn = 0;

            Map<String, Integer> hmOrderCode = new HashMap<String, Integer>();
            Map<String, Integer> hmOrderCode1 = new HashMap<String, Integer>();
            Map<String, List<clsPlaceOrderDtl>> hmPlaceOrderDtl = new HashMap<String, List<clsPlaceOrderDtl>>();
            List<clsPlaceOrderDtl> listItemDtl = null;

            Map<String, Map<String, List<clsPlaceOrderDtl>>> hmOrderDtl = new HashMap<String, Map<String, List<clsPlaceOrderDtl>>>();

            String sql = "select c.strExternalCode,c.strItemName,b.strProductCode,sum(b.dblStockQty),"
                    + " sum(b.dblQty),a.strOrderCode,a.strSOCode,date(a.dteOrderDate),"
                    + " f.strGroupCode,f.strSubGroupCode ,b.strItemCode,f.strSubGroupName,date(a.dteSODate)    "
                    + " from tblplaceorderhd a,tblplaceorderdtl b,tblitemmaster c,tblitemcurrentstk e,"
                    + " tblsubgrouphd f   where a.strOrderCode=b.strOrderCode and b.strItemCode=c.strItemCode "
                    + " and c.strItemCode=e.strItemCode   and c.strSubGroupCode=f.strSubGroupCode "
                    + " and a.strCloseSO='N'  and a.strSOCode!='' "
                    + " and date(a.dteSODate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' ";
            if (!posCode.equals("All"))
            {
                sqlFilters += " AND e.strPOSCode = '" + posCode + "' ";
            }
            if (!orderType.equals("All"))
            {
                sqlFilters += " and a.strOrderType = '" + orderType + "' ";
                if (orderType.equals("Normal"))
                {
                    if (!orderName.equals("All"))
                    {
                        sqlFilters += " and a.strOrderTypeCode = '" + orderCode + "' ";
                    }
                }
            }

            sqlFilters += " GROUP BY b.strItemCode order by a.dteOrderDate";
            sql = sql + " " + sqlFilters;

            ResultSet rsOrder = clsGlobalVarClass.dbMysql.executeResultSet(sql);
            while (rsOrder.next())
            {
                cn++;
                clsPlaceOrderDtl objItemDtl = new clsPlaceOrderDtl();
                if (hmOrderDtl.containsKey(rsOrder.getString(6)))
                {
                    hmPlaceOrderDtl = hmOrderDtl.get(rsOrder.getString(6));

                    if (hmPlaceOrderDtl.containsKey(rsOrder.getString(12)))
                    {
                        listItemDtl = hmPlaceOrderDtl.get(rsOrder.getString(12));
                        objItemDtl = new clsPlaceOrderDtl();
                        objItemDtl.setSubGroupName(rsOrder.getString(12));
                        objItemDtl.setItemName(rsOrder.getString(2));
                        objItemDtl.setSaleQty(rsOrder.getString(5));
                        objItemDtl.setOrderDate(rsOrder.getString(8));
                        objItemDtl.setSoDate(rsOrder.getString(13));
                        objItemDtl.setSOCode(rsOrder.getString(7));
                    }
                    else
                    {
                        listItemDtl = new ArrayList<clsPlaceOrderDtl>();
                        objItemDtl.setSubGroupName(rsOrder.getString(12));
                        objItemDtl.setItemName(rsOrder.getString(2));
                        objItemDtl.setSaleQty(rsOrder.getString(5));
                        objItemDtl.setOrderDate(rsOrder.getString(8));
                        objItemDtl.setSoDate(rsOrder.getString(13));
                        objItemDtl.setSOCode(rsOrder.getString(7));
                    }
                    listItemDtl.add(objItemDtl);
                    hmPlaceOrderDtl.put(rsOrder.getString(12), listItemDtl);
                }
                else
                {
                    listItemDtl = new ArrayList<clsPlaceOrderDtl>();
                    objItemDtl.setSubGroupName(rsOrder.getString(12));
                    objItemDtl.setItemName(rsOrder.getString(2));
                    objItemDtl.setSaleQty(rsOrder.getString(5));
                    objItemDtl.setOrderDate(rsOrder.getString(8));
                    objItemDtl.setSoDate(rsOrder.getString(13));
                    objItemDtl.setSOCode(rsOrder.getString(7));
                    listItemDtl.add(objItemDtl);
                    hmPlaceOrderDtl = new HashMap<String, List<clsPlaceOrderDtl>>();
                    hmPlaceOrderDtl.put(rsOrder.getString(12), listItemDtl);
                }
                hmOrderDtl.put(rsOrder.getString(6), hmPlaceOrderDtl);
            }
            if (cn > 0)
            {

                if (!orderType.equals("All"))
                {
                    if (orderType.equals("Normal"))
                    {
                        funGeneratePlaceOrderJasperReport(hmOrderDtl, "Normal Order Details",reportType);
                    }
                    else if (orderType.equals("Advance"))
                    {
                        funGeneratePlaceOrderJasperReport(hmOrderDtl, "Advance Order Details",reportType);
                    }
                    else
                    {
                        funGeneratePlaceOrderJasperReport(hmOrderDtl, "Urgent Order Details",reportType);
                    }
                }
                else
                {
                    funGeneratePlaceOrderJasperReport(hmOrderDtl, "Placed Order Details",reportType);
                }
            }
            else
            {
                JOptionPane.showMessageDialog(null, "Data Not Present For Selected Dates!!!!!");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private void funGeneratePlaceOrderJasperReport(Map<String, Map<String, List<clsPlaceOrderDtl>>> hmOrderDtl, String Title,String reportType)
    {
        try
        {

            hm.put("Title", Title);
            String line = "__________";

            List<clsPlaceOrderDtl> listOfLeftSidePlaceOrderDtl = new ArrayList<clsPlaceOrderDtl>();
            List<clsPlaceOrderDtl> listOfRightSidePlaceOrderDtl = new ArrayList<clsPlaceOrderDtl>();
            List<clsPlaceOrderDtl> orderList = new ArrayList<clsPlaceOrderDtl>();
            Map<String, List<clsPlaceOrderDtl>> hmPlaceOrderDtl = new HashMap<String, List<clsPlaceOrderDtl>>();
            int c = 0;

            if (hmOrderDtl.size() > 0)
            {
                for (Map.Entry<String, Map<String, List<clsPlaceOrderDtl>>> entry : hmOrderDtl.entrySet())
                {
                    String orderCode = entry.getKey();
                    String orderDate = "", soDate = "", soCode = "";
                    double totalOrderQty = 0;
                    hmPlaceOrderDtl = entry.getValue();
                    if (c % 2 == 0)
                    {
                        clsPlaceOrderDtl objOrder = new clsPlaceOrderDtl();
                        objOrder.setSubGroupName("");
                        objOrder.setItemName("Order No :" + "  " + entry.getKey());
                        objOrder.setSaleQty("");
                        listOfLeftSidePlaceOrderDtl.add(objOrder);
                        for (Map.Entry<String, List<clsPlaceOrderDtl>> entryOrder : hmPlaceOrderDtl.entrySet())
                        {
                            orderList = entryOrder.getValue();
                            for (int i = 0; i < orderList.size(); i++)
                            {
                                objOrder = orderList.get(i);
                                orderDate = objOrder.getOrderDate();
                                soDate = objOrder.getSoDate();
                                soCode = objOrder.getSOCode();
                                break;

                            }
                        }
                        objOrder = new clsPlaceOrderDtl();
                        objOrder.setSubGroupName("");
                        objOrder.setItemName("Order Date :" + " " + orderDate);
                        objOrder.setSaleQty(" ");
                        listOfLeftSidePlaceOrderDtl.add(objOrder);
                        objOrder = new clsPlaceOrderDtl();
                        objOrder.setSubGroupName("");
                        objOrder.setItemName("SO Date :" + " " + soDate);
                        objOrder.setSaleQty(" ");
                        listOfLeftSidePlaceOrderDtl.add(objOrder);
                        objOrder = new clsPlaceOrderDtl();
                        objOrder.setSubGroupName("");
                        objOrder.setItemName("SO Code :" + " " + soCode);
                        objOrder.setSaleQty(" ");
                        listOfLeftSidePlaceOrderDtl.add(objOrder);

                        for (Map.Entry<String, List<clsPlaceOrderDtl>> entryOrder : hmPlaceOrderDtl.entrySet())
                        {
                            double totalQty = 0;
                            orderList = entryOrder.getValue();
                            objOrder = new clsPlaceOrderDtl();
                            objOrder.setSubGroupName((char) 27 + entryOrder.getKey());
                            objOrder.setItemName(entryOrder.getKey());
                            objOrder.setSaleQty("");
                            listOfLeftSidePlaceOrderDtl.add(objOrder);
                            objOrder = new clsPlaceOrderDtl();
                            objOrder.setSubGroupName("");
                            objOrder.setItemName(line + line);
                            objOrder.setSaleQty(" ");
                            listOfLeftSidePlaceOrderDtl.add(objOrder);
                            for (int i = 0; i < orderList.size(); i++)
                            {
                                objOrder = orderList.get(i);
                                totalQty = totalQty + Double.valueOf(objOrder.getSaleQty());
                                listOfLeftSidePlaceOrderDtl.add(objOrder);
                            }
                            totalOrderQty += totalQty;
                            objOrder = new clsPlaceOrderDtl();
                            objOrder.setSubGroupName((char) 27 + entryOrder.getKey());
                            objOrder.setItemName("");
                            objOrder.setSaleQty(line);
                            listOfLeftSidePlaceOrderDtl.add(objOrder);
                            objOrder = new clsPlaceOrderDtl();
                            objOrder.setSubGroupName(entryOrder.getKey());
                            objOrder.setItemName("");
                            objOrder.setSaleQty(String.valueOf(totalQty));
                            listOfLeftSidePlaceOrderDtl.add(objOrder);
                            objOrder = new clsPlaceOrderDtl();
                            objOrder.setSubGroupName("");
                            objOrder.setItemName("");
                            objOrder.setSaleQty("");
                            listOfLeftSidePlaceOrderDtl.add(objOrder);
                        }

                        objOrder = new clsPlaceOrderDtl();
                        objOrder.setSubGroupName("");
                        objOrder.setItemName("Total Order Qty :");
                        objOrder.setSaleQty(String.valueOf(totalOrderQty));
                        listOfLeftSidePlaceOrderDtl.add(objOrder);
                        objOrder = new clsPlaceOrderDtl();
                        objOrder.setSubGroupName("");
                        objOrder.setItemName("");
                        objOrder.setSaleQty(" ");
                        listOfLeftSidePlaceOrderDtl.add(objOrder);
                    }
                    else
                    {
                        clsPlaceOrderDtl objOrder = new clsPlaceOrderDtl();
                        objOrder.setSubGroupName("");
                        objOrder.setItemName("Order No :" + "  " + entry.getKey());
                        objOrder.setSaleQty("");
                        listOfRightSidePlaceOrderDtl.add(objOrder);
                        for (Map.Entry<String, List<clsPlaceOrderDtl>> entryOrder : hmPlaceOrderDtl.entrySet())
                        {
                            orderList = entryOrder.getValue();
                            for (int i = 0; i < orderList.size(); i++)
                            {
                                objOrder = orderList.get(i);
                                orderDate = objOrder.getOrderDate();
                                soDate = objOrder.getSoDate();
                                soCode = objOrder.getSOCode();
                                break;

                            }
                        }
                        objOrder = new clsPlaceOrderDtl();
                        objOrder.setSubGroupName("");
                        objOrder.setItemName("Order Date :" + " " + orderDate);
                        objOrder.setSaleQty(" ");
                        listOfRightSidePlaceOrderDtl.add(objOrder);
                        objOrder = new clsPlaceOrderDtl();
                        objOrder.setSubGroupName("");
                        objOrder.setItemName("SO Date :" + " " + soDate);
                        objOrder.setSaleQty(" ");
                        listOfRightSidePlaceOrderDtl.add(objOrder);
                        objOrder = new clsPlaceOrderDtl();
                        objOrder.setSubGroupName("");
                        objOrder.setItemName("SO Code :" + " " + soCode);
                        objOrder.setSaleQty(" ");
                        listOfRightSidePlaceOrderDtl.add(objOrder);
                        /*
                         * objOrder=new clsPlaceOrderDtl();
                         * objOrder.setSubGroupName("");
                         * objOrder.setItemName(line+line);
                         * objOrder.setSaleQty(" ");
                         * listOfRightSidePlaceOrderDtl.add(objOrder);
                         */

                        for (Map.Entry<String, List<clsPlaceOrderDtl>> entryOrder : hmPlaceOrderDtl.entrySet())
                        {
                            double totalQty = 0;
                            orderList = entryOrder.getValue();
                            objOrder = new clsPlaceOrderDtl();
                            objOrder.setSubGroupName((char) 27 + entryOrder.getKey());
                            objOrder.setItemName(entryOrder.getKey());
                            objOrder.setSaleQty("");
                            listOfRightSidePlaceOrderDtl.add(objOrder);
                            objOrder = new clsPlaceOrderDtl();
                            objOrder.setSubGroupName("");
                            objOrder.setItemName(line + line);
                            objOrder.setSaleQty(" ");
                            listOfRightSidePlaceOrderDtl.add(objOrder);
                            for (int i = 0; i < orderList.size(); i++)
                            {
                                objOrder = orderList.get(i);
                                totalQty = totalQty + Double.valueOf(objOrder.getSaleQty());
                                listOfRightSidePlaceOrderDtl.add(objOrder);
                            }
                            totalOrderQty += totalQty;
                            objOrder = new clsPlaceOrderDtl();
                            objOrder.setSubGroupName(entryOrder.getKey());
                            objOrder.setItemName("");
                            objOrder.setSaleQty(line);
                            listOfRightSidePlaceOrderDtl.add(objOrder);
                            objOrder = new clsPlaceOrderDtl();
                            objOrder.setSubGroupName(entryOrder.getKey());
                            objOrder.setItemName("");
                            objOrder.setSaleQty(String.valueOf(totalQty));
                            listOfRightSidePlaceOrderDtl.add(objOrder);
                            objOrder = new clsPlaceOrderDtl();
                            objOrder.setSubGroupName("");
                            objOrder.setItemName("");
                            objOrder.setSaleQty("");
                            listOfRightSidePlaceOrderDtl.add(objOrder);
                        }
                        objOrder = new clsPlaceOrderDtl();
                        objOrder.setSubGroupName("");
                        objOrder.setItemName("Total Order Qty :");
                        objOrder.setSaleQty(String.valueOf(totalOrderQty));
                        listOfRightSidePlaceOrderDtl.add(objOrder);
                        objOrder = new clsPlaceOrderDtl();
                        objOrder.setSubGroupName("");
                        objOrder.setItemName("");
                        objOrder.setSaleQty(" ");
                        listOfRightSidePlaceOrderDtl.add(objOrder);

                    }
                    c++;
                }
            }
            hm.put("LeftSideList", listOfLeftSidePlaceOrderDtl);
            hm.put("RightSideList", listOfRightSidePlaceOrderDtl);

            InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptplaceOrderDetailReport.jasper");
            //call for view report
            if(reportType.equalsIgnoreCase("A4 Size Report"))
            {
            funViewJasperReportForJDBCConnectionDataSource(is, hm, null);
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

}
