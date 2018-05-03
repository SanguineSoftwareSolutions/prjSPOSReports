
package com.POSReport.controller;

import com.POSGlobal.controller.clsGlobalVarClass;
import java.awt.Dimension;
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
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.swing.JRViewer;


public class clsOpenItemWiseAuditReport
{
   public void funOpenItemWiseAuditReport(String reportType, HashMap hm, String dayEnd)
    {
        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptOpenItemWiseAuditReport.jasper");

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
            DecimalFormat decimalFormat=new DecimalFormat("0.##");

            sbSqlLive.append("select b.strItemCode,b.strItemName,ifnull(d.strTableName,''),sum(b.dblAmount+(ifnull(c.dblAmount,0))),TIME_FORMAT(b.dteBillDate,'%H:%i'),ifnull(c.strModifierName,'')\n " 
                    + "from tblbillhd a inner join tblbilldtl b on a.strBillNo = b.strBillNo\n" 
                    + "left outer join tblbillmodifierdtl c on a.strBillNo=c.strBillNo\n" 
                    + "left outer join tbltablemaster d on a.strTableNo=d.strTableNo \n" 
                    + "inner join tblitemmaster e on b.strItemCode=e.strItemCode\n" 
                    + "where e.strOpenItem='Y' and (b.dblAmount>0 or c.dblAmount>0)\n" 
                    );

            sbSqlQFile.append("select b.strItemCode,b.strItemName,ifnull(d.strTableName,''),sum(b.dblAmount+(ifnull(c.dblAmount,0))),TIME_FORMAT(b.dteBillDate,'%H:%i'),ifnull(c.strModifierName,'')\n " 
                    + "from tblqbillhd a inner join tblqbilldtl b on a.strBillNo = b.strBillNo\n" 
                    + "left outer join tblqbillmodifierdtl c on a.strBillNo=c.strBillNo\n" 
                    + "left outer join tbltablemaster d on a.strTableNo=d.strTableNo \n" 
                    + "inner join tblitemmaster e on b.strItemCode=e.strItemCode\n" 
                    + "where e.strOpenItem='Y' and (b.dblAmount>0 or c.dblAmount>0)\n" 
                    );

            sqlFilter.append("and date(a.dteBillDate ) BETWEEN  '" + fromDate + "' AND '" + toDate + "' ");
            if (!"All".equalsIgnoreCase(posCode))
            {
                sqlFilter.append("and  a.strPosCode='" + posCode + "' ");
            }
            if (clsGlobalVarClass.gEnableShiftYN)
            {
                if (clsGlobalVarClass.gEnableShiftYN && (!shiftNo.equalsIgnoreCase("All")))
                {
                    sqlFilter.append(" and a.intShiftCode = '" + shiftNo + "' ");
                }
            }

            sqlFilter.append("group by b.strItemCode,c.strModifierName,a.strTableNo");

            sbSqlLive.append(sqlFilter);
            sbSqlQFile.append(sqlFilter);

            ResultSet rsData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
            List<clsOpenItemWiseAuditBean> listOfItemWiseAudittData = new ArrayList<clsOpenItemWiseAuditBean>();
            double grossRevenue = 0;
            while (rsData.next())
            {
                clsOpenItemWiseAuditBean obj = new clsOpenItemWiseAuditBean();
                obj.setStrItemCode(rsData.getString(1));
                obj.setStrItemName(rsData.getString(2));
                obj.setStrTableName(rsData.getString(3));
                obj.setDblAmount(rsData.getDouble(4));
                obj.setDteBillDate(rsData.getString(5));
                obj.setStrModifierName(rsData.getString(6));
                
                grossRevenue += rsData.getDouble(4);
                listOfItemWiseAudittData.add(obj);
            }

            rsData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFile.toString());
            while (rsData.next())
            {
                clsOpenItemWiseAuditBean obj = new clsOpenItemWiseAuditBean();
                obj.setStrItemCode(rsData.getString(1));
                obj.setStrItemName(rsData.getString(2));
                obj.setStrTableName(rsData.getString(3));
                obj.setDblAmount(rsData.getDouble(4));
                obj.setDteBillDate(rsData.getString(5));
                obj.setStrModifierName(rsData.getString(6));
                
                grossRevenue += rsData.getDouble(4);
                listOfItemWiseAudittData.add(obj);
            }

            hm.put("grossRevenue", grossRevenue);

            //call for view report
            if (reportType.equalsIgnoreCase("A4 Size Report"))
            {
                funViewJasperReportForBeanCollectionDataSource(is, hm, listOfItemWiseAudittData);
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
}
