package com.POSReport.controller;

import com.POSReport.controller.comparator.clsOperatorComparator;
import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsOperatorDtl;
import com.POSGlobal.controller.clsPosConfigFile;
import com.POSGlobal.controller.clsTaxCalculationDtls;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

public class clsAuditorsReport
{
 DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();
    public void funAuditorsReport(String reportType, HashMap hm, String dayEnd)
    {
        try
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/POSReport/reports/rptAuditorReport.jasper");

            String fromDate = hm.get("fromDate").toString();
            String toDate = hm.get("toDate").toString();
            String posCode = hm.get("posCode").toString();
            String shiftNo = hm.get("shiftNo").toString();
            String posName = hm.get("posName").toString();
            String fromDateToDisplay = hm.get("fromDateToDisplay").toString();
            String toDateToDisplay = hm.get("toDateToDisplay").toString();

            String sqlLive = "", sqlQFile = "";
            StringBuilder sbSqlDisLive = new StringBuilder();
            StringBuilder sbSqlQDisFile = new StringBuilder();
            StringBuilder sbSqlDisFilters = new StringBuilder();
            List<clsOperatorDtl> listOperatorDtl = new ArrayList<>();

            sbSqlDisLive.setLength(0);
            sbSqlQDisFile.setLength(0);
            sbSqlDisFilters.setLength(0);

            String MinBillNo = "";
            String MaxBillNo = "";
            String TotalDiscount = "";
            String sql = "select min(a.strBillNo),max(a.strBillNo),sum(a.dblDiscountAmt)\n"
                    + "from vqbillhd  a \n"
                    + "where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' \n"
                    + "Order By a.strBillNo";
            ResultSet rsAuditorReport = clsGlobalVarClass.dbMysql.executeResultSet(sql);
            while (rsAuditorReport.next())
            {
                MinBillNo = rsAuditorReport.getString(1);
                MaxBillNo = rsAuditorReport.getString(2);
                TotalDiscount = rsAuditorReport.getString(3);
            }
            rsAuditorReport.close();
            hm.put("minBillNo", MinBillNo);
            hm.put("maxBillNo", MaxBillNo);
            hm.put("totalDiscount", TotalDiscount);

            sqlLive = " SELECT a.strBillNo, IFNULL(d.strSettelmentDesc,'ND') AS payMode, IFNULL(a.dblSubTotal,0.00) AS subTotal\n"
                    + ", a.dblTaxAmt,a.dblDiscountAmt, IFNULL(c.dblSettlementAmt,0.00) AS settleAmt\n"
                    + ", IFNULL(e.strCustomerName,'') AS CustomerName,(a.dblSubTotal-a.dblDiscountAmt)NetTotal\n"
                    + ",d.dblThirdPartyComission,d.strComissionType,d.strComissionOn,a.intBillSeriesPaxNo "
                    + " from tblbillhd  a "
                    + " left outer join tblbillsettlementdtl c on a.strBillNo=c.strBillNo  and date(a.dteBillDate)=date(c.dteBillDate) "
                    + " left outer join tblsettelmenthd d on c.strSettlementCode=d.strSettelmentCode "
                    + " left outer join tblcustomermaster e on a.strCustomerCode=e.strCustomerCode "
                    + " where date(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
                    + " and d.strSettelmentType!='Complementary'";

            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlLive += " and a.strPOSCode= '" + posCode + "' ";
            }
            if (!shiftNo.equalsIgnoreCase("All"))
            {
                sqlLive += " and a.intShiftCode= '" + shiftNo + "' ";
            }

            sqlLive += " Order By d.strSettelmentDesc";

            sqlQFile = " SELECT a.strBillNo, IFNULL(d.strSettelmentDesc,'ND') AS payMode, IFNULL(a.dblSubTotal,0.00) AS subTotal\n"
                    + ", a.dblTaxAmt,a.dblDiscountAmt, IFNULL(c.dblSettlementAmt,0.00) AS settleAmt\n"
                    + ", IFNULL(e.strCustomerName,'') AS CustomerName,(a.dblSubTotal-a.dblDiscountAmt)NetTotal\n"
                    + ",d.dblThirdPartyComission,d.strComissionType,d.strComissionOn,a.intBillSeriesPaxNo "
                    + " from tblqbillhd  a "
                    + " left outer join tblqbillsettlementdtl c on a.strBillNo=c.strBillNo  and date(a.dteBillDate)=date(c.dteBillDate) "
                    + " left outer join tblsettelmenthd d on c.strSettlementCode=d.strSettelmentCode "
                    + " left outer join tblcustomermaster e on a.strCustomerCode=e.strCustomerCode "
                    + " where date(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
                    + " and d.strSettelmentType!='Complementary' ";

            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlQFile += " and a.strPOSCode= '" + posCode + "' ";
            }
            if (!shiftNo.equalsIgnoreCase("All"))
            {
                sqlQFile += " and a.intShiftCode= '" + shiftNo + "' ";
            }

            sqlQFile += " Order By d.strSettelmentDesc";

            Map<String, Map<String, clsOperatorDtl>> hmOperatorWiseSales = new HashMap<String, Map<String, clsOperatorDtl>>();
            Map<String, clsOperatorDtl> hmSettlementDtl = null;
            clsOperatorDtl objOperatorWiseSales = null;

            ResultSet rsOperator = clsGlobalVarClass.dbMysql.executeResultSet(sqlLive);
            while (rsOperator.next())
            {
                if (hmOperatorWiseSales.containsKey(rsOperator.getString(1)))
                {
                    hmSettlementDtl = hmOperatorWiseSales.get(rsOperator.getString(1));
                    if (hmSettlementDtl.containsKey(rsOperator.getString(2)))
                    {
                        objOperatorWiseSales = hmSettlementDtl.get(rsOperator.getString(2));
                        objOperatorWiseSales.setSettleAmt(objOperatorWiseSales.getSettleAmt() + rsOperator.getDouble(6));
                        objOperatorWiseSales.setDblGrossAmt(objOperatorWiseSales.getDblGrossAmt() + rsOperator.getDouble(6));

                        double comissionOn = 0.00;
                        if (objOperatorWiseSales.getStrComissionOn().equalsIgnoreCase("Net Amount"))
                        {
                            comissionOn = objOperatorWiseSales.getDblNetTotal();
                        }
                        else if (objOperatorWiseSales.getStrComissionOn().equalsIgnoreCase("Gross Amount"))
                        {
                            comissionOn = objOperatorWiseSales.getDblGrossAmt();
                        }
                        else if (objOperatorWiseSales.getStrComissionOn().equalsIgnoreCase("No. Of PAX"))
                        {
                            comissionOn = objOperatorWiseSales.getIntBillSeriesPaxNo();
                        }

                        double comission = 0.00;
                        if (objOperatorWiseSales.getStrComissionType().equalsIgnoreCase("Per"))
                        {
                            comission = (objOperatorWiseSales.getDblThirdPartyComission() / 100) * comissionOn;
                        }
                        else//amt
                        {
                            comission = objOperatorWiseSales.getDblThirdPartyComission();
                        }

                        objOperatorWiseSales.setDblComission(comission);
                    }
                    else
                    {
                        objOperatorWiseSales = new clsOperatorDtl();
                        objOperatorWiseSales.setStrUserCode(rsOperator.getString(1));
                        objOperatorWiseSales.setStrSettlementDesc(rsOperator.getString(2));
                        objOperatorWiseSales.setStrUserName(rsOperator.getString(7));
                        objOperatorWiseSales.setDblSubTotal(0);
                        objOperatorWiseSales.setDblTaxAmt(0);
                        objOperatorWiseSales.setDiscountAmt(0);
                        objOperatorWiseSales.setSettleAmt(rsOperator.getDouble(6));
                        objOperatorWiseSales.setDblGrossAmt(rsOperator.getDouble(6));
                        objOperatorWiseSales.setDblNetTotal(rsOperator.getDouble(8));

                        objOperatorWiseSales.setDblThirdPartyComission(rsOperator.getDouble(9));
                        objOperatorWiseSales.setStrComissionType(rsOperator.getString(10));
                        objOperatorWiseSales.setStrComissionOn(rsOperator.getString(11));
                        objOperatorWiseSales.setIntBillSeriesPaxNo(rsOperator.getInt(12));

                        double comissionOn = 0.00;
                        if (objOperatorWiseSales.getStrComissionOn().equalsIgnoreCase("Net Amount"))
                        {
                            comissionOn = objOperatorWiseSales.getDblNetTotal();
                        }
                        else if (objOperatorWiseSales.getStrComissionOn().equalsIgnoreCase("Gross Amount"))
                        {
                            comissionOn = objOperatorWiseSales.getDblGrossAmt();
                        }
                        else if (objOperatorWiseSales.getStrComissionOn().equalsIgnoreCase("No. Of PAX"))
                        {
                            comissionOn = objOperatorWiseSales.getIntBillSeriesPaxNo();
                        }

                        double comission = 0.00;
                        if (objOperatorWiseSales.getStrComissionType().equalsIgnoreCase("Per"))
                        {
                            comission = (objOperatorWiseSales.getDblThirdPartyComission() / 100) * comissionOn;
                        }
                        else//amt
                        {
                            comission = objOperatorWiseSales.getDblThirdPartyComission();
                        }

                        objOperatorWiseSales.setDblComission(comission);

                    }
                    hmSettlementDtl.put(rsOperator.getString(2), objOperatorWiseSales);
                }
                else
                {

                    objOperatorWiseSales = new clsOperatorDtl();
                    objOperatorWiseSales.setStrUserCode(rsOperator.getString(1));
                    objOperatorWiseSales.setStrSettlementDesc(rsOperator.getString(2));
                    objOperatorWiseSales.setStrUserName(rsOperator.getString(7));
                    objOperatorWiseSales.setDblSubTotal(0);
                    objOperatorWiseSales.setDblTaxAmt(0);
                    objOperatorWiseSales.setDiscountAmt(0);
                    objOperatorWiseSales.setSettleAmt(rsOperator.getDouble(6));
                    objOperatorWiseSales.setDblGrossAmt(rsOperator.getDouble(6));
                    objOperatorWiseSales.setDblNetTotal(rsOperator.getDouble(8));

                    objOperatorWiseSales.setDblThirdPartyComission(rsOperator.getDouble(9));
                    objOperatorWiseSales.setStrComissionType(rsOperator.getString(10));
                    objOperatorWiseSales.setStrComissionOn(rsOperator.getString(11));
                    objOperatorWiseSales.setIntBillSeriesPaxNo(rsOperator.getInt(12));

                    double comissionOn = 0.00;
                    if (objOperatorWiseSales.getStrComissionOn().equalsIgnoreCase("Net Amount"))
                    {
                        comissionOn = objOperatorWiseSales.getDblNetTotal();
                    }
                    else if (objOperatorWiseSales.getStrComissionOn().equalsIgnoreCase("Gross Amount"))
                    {
                        comissionOn = objOperatorWiseSales.getDblGrossAmt();
                    }
                    else if (objOperatorWiseSales.getStrComissionOn().equalsIgnoreCase("No. Of PAX"))
                    {
                        comissionOn = objOperatorWiseSales.getIntBillSeriesPaxNo();
                    }

                    double comission = 0.00;
                    if (objOperatorWiseSales.getStrComissionType().equalsIgnoreCase("Per"))
                    {
                        comission = (objOperatorWiseSales.getDblThirdPartyComission() / 100) * comissionOn;
                    }
                    else//amt
                    {
                        comission = objOperatorWiseSales.getDblThirdPartyComission();
                    }

                    objOperatorWiseSales.setDblComission(comission);

                    hmSettlementDtl = new HashMap<String, clsOperatorDtl>();
                    hmSettlementDtl.put(rsOperator.getString(2), objOperatorWiseSales);
                }
                hmOperatorWiseSales.put(rsOperator.getString(1), hmSettlementDtl);
            }
            rsOperator.close();

            rsOperator = clsGlobalVarClass.dbMysql.executeResultSet(sqlQFile);
            while (rsOperator.next())
            {
                if (hmOperatorWiseSales.containsKey(rsOperator.getString(1)))
                {
                    hmSettlementDtl = hmOperatorWiseSales.get(rsOperator.getString(1));
                    if (hmSettlementDtl.containsKey(rsOperator.getString(2)))
                    {
                        objOperatorWiseSales = hmSettlementDtl.get(rsOperator.getString(2));
                        objOperatorWiseSales.setSettleAmt(objOperatorWiseSales.getSettleAmt() + rsOperator.getDouble(6));
                        objOperatorWiseSales.setDblGrossAmt(objOperatorWiseSales.getDblGrossAmt() + rsOperator.getDouble(6));

                        double comissionOn = 0.00;
                        if (objOperatorWiseSales.getStrComissionOn().equalsIgnoreCase("Net Amount"))
                        {
                            comissionOn = objOperatorWiseSales.getDblNetTotal();
                        }
                        else if (objOperatorWiseSales.getStrComissionOn().equalsIgnoreCase("Gross Amount"))
                        {
                            comissionOn = objOperatorWiseSales.getDblGrossAmt();
                        }
                        else if (objOperatorWiseSales.getStrComissionOn().equalsIgnoreCase("No. Of PAX"))
                        {
                            comissionOn = objOperatorWiseSales.getIntBillSeriesPaxNo();
                        }

                        double comission = 0.00;
                        if (objOperatorWiseSales.getStrComissionType().equalsIgnoreCase("Per"))
                        {
                            comission = (objOperatorWiseSales.getDblThirdPartyComission() / 100) * comissionOn;
                        }
                        else//amt
                        {
                            comission = objOperatorWiseSales.getDblThirdPartyComission();
                        }

                        objOperatorWiseSales.setDblComission(comission);
                    }
                    else
                    {
                        objOperatorWiseSales = new clsOperatorDtl();
                        objOperatorWiseSales.setStrUserCode(rsOperator.getString(1));
                        objOperatorWiseSales.setStrSettlementDesc(rsOperator.getString(2));
                        objOperatorWiseSales.setStrUserName(rsOperator.getString(7));
                        objOperatorWiseSales.setDblSubTotal(0);
                        objOperatorWiseSales.setDblTaxAmt(0);
                        objOperatorWiseSales.setDiscountAmt(0);
                        objOperatorWiseSales.setSettleAmt(rsOperator.getDouble(6));
                        objOperatorWiseSales.setDblGrossAmt(rsOperator.getDouble(6));
                        objOperatorWiseSales.setDblNetTotal(rsOperator.getDouble(8));

                        objOperatorWiseSales.setDblThirdPartyComission(rsOperator.getDouble(9));
                        objOperatorWiseSales.setStrComissionType(rsOperator.getString(10));
                        objOperatorWiseSales.setStrComissionOn(rsOperator.getString(11));
                        objOperatorWiseSales.setIntBillSeriesPaxNo(rsOperator.getInt(12));

                        double comissionOn = 0.00;
                        if (objOperatorWiseSales.getStrComissionOn().equalsIgnoreCase("Net Amount"))
                        {
                            comissionOn = objOperatorWiseSales.getDblNetTotal();
                        }
                        else if (objOperatorWiseSales.getStrComissionOn().equalsIgnoreCase("Gross Amount"))
                        {
                            comissionOn = objOperatorWiseSales.getDblGrossAmt();
                        }
                        else if (objOperatorWiseSales.getStrComissionOn().equalsIgnoreCase("No. Of PAX"))
                        {
                            comissionOn = objOperatorWiseSales.getIntBillSeriesPaxNo();
                        }

                        double comission = 0.00;
                        if (objOperatorWiseSales.getStrComissionType().equalsIgnoreCase("Per"))
                        {
                            comission = (objOperatorWiseSales.getDblThirdPartyComission() / 100) * comissionOn;
                        }
                        else//amt
                        {
                            comission = objOperatorWiseSales.getDblThirdPartyComission();
                        }

                        objOperatorWiseSales.setDblComission(comission);

                    }
                    hmSettlementDtl.put(rsOperator.getString(2), objOperatorWiseSales);
                }
                else
                {

                    objOperatorWiseSales = new clsOperatorDtl();
                    objOperatorWiseSales.setStrUserCode(rsOperator.getString(1));
                    objOperatorWiseSales.setStrSettlementDesc(rsOperator.getString(2));
                    objOperatorWiseSales.setStrUserName(rsOperator.getString(7));
                    objOperatorWiseSales.setDblSubTotal(0);
                    objOperatorWiseSales.setDblTaxAmt(0);
                    objOperatorWiseSales.setDiscountAmt(0);
                    objOperatorWiseSales.setSettleAmt(rsOperator.getDouble(6));
                    objOperatorWiseSales.setDblGrossAmt(rsOperator.getDouble(6));
                    objOperatorWiseSales.setDblNetTotal(rsOperator.getDouble(8));

                    objOperatorWiseSales.setDblThirdPartyComission(rsOperator.getDouble(9));
                    objOperatorWiseSales.setStrComissionType(rsOperator.getString(10));
                    objOperatorWiseSales.setStrComissionOn(rsOperator.getString(11));
                    objOperatorWiseSales.setIntBillSeriesPaxNo(rsOperator.getInt(12));

                    double comissionOn = 0.00;
                    if (objOperatorWiseSales.getStrComissionOn().equalsIgnoreCase("Net Amount"))
                    {
                        comissionOn = objOperatorWiseSales.getDblNetTotal();
                    }
                    else if (objOperatorWiseSales.getStrComissionOn().equalsIgnoreCase("Gross Amount"))
                    {
                        comissionOn = objOperatorWiseSales.getDblGrossAmt();
                    }
                    else if (objOperatorWiseSales.getStrComissionOn().equalsIgnoreCase("No. Of PAX"))
                    {
                        comissionOn = objOperatorWiseSales.getIntBillSeriesPaxNo();
                    }

                    double comission = 0.00;
                    if (objOperatorWiseSales.getStrComissionType().equalsIgnoreCase("Per"))
                    {
                        comission = (objOperatorWiseSales.getDblThirdPartyComission() / 100) * comissionOn;
                    }
                    else//amt
                    {
                        comission = objOperatorWiseSales.getDblThirdPartyComission();
                    }

                    objOperatorWiseSales.setDblComission(comission);

                    hmSettlementDtl = new HashMap<String, clsOperatorDtl>();
                    hmSettlementDtl.put(rsOperator.getString(2), objOperatorWiseSales);
                }
                hmOperatorWiseSales.put(rsOperator.getString(1), hmSettlementDtl);
            }
            rsOperator.close();

            sbSqlDisLive.append("SELECT b.strBillNo, b.strPOSCode, c.strPOSName "
                    + ",sum(b.dblSubTotal),sum(b.dblDiscountAmt),sum(b.dblTaxAmt),'SANGUINE' "
                    + " FROM tblbillhd b "
                    + " inner join tblposmaster c on b.strPOSCode=c.strPOSCode  "
                    + " WHERE date( b.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' ");

            sbSqlQDisFile.append(" SELECT b.strBillNo, b.strPOSCode, c.strPOSName"
                    + ",sum(b.dblSubTotal),sum(b.dblDiscountAmt),sum(b.dblTaxAmt),'SANGUINE' "
                    + " FROM tblqbillhd b "
                    + " inner join tblposmaster c on b.strPOSCode=c.strPOSCode "
                    + " WHERE date( b.dteBillDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' ");

            if (!posCode.equalsIgnoreCase("All"))
            {
                sbSqlDisFilters.append(" AND b.strPOSCode = '" + posCode + "' ");
            }
            if (!shiftNo.equalsIgnoreCase("All"))
            {
                sbSqlDisFilters.append(" AND b.intShiftCode = '" + shiftNo + "' ");
            }

            sbSqlDisFilters.append(" GROUP BY b.strBillNo, b.strPosCode");

            sbSqlDisLive.append(sbSqlDisFilters);
            sbSqlQDisFile.append(sbSqlDisFilters);

            double dis = 0;

            ResultSet rsOperatorDis = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlDisLive.toString());
            while (rsOperatorDis.next())
            {
                if (hmOperatorWiseSales.containsKey(rsOperatorDis.getString(1)))
                {
                    hmSettlementDtl = hmOperatorWiseSales.get(rsOperatorDis.getString(1));
                    Set<String> setKeys = hmSettlementDtl.keySet();
                    for (String keys : setKeys)
                    {
                        objOperatorWiseSales = hmSettlementDtl.get(keys);
                        objOperatorWiseSales.setDblSubTotal(objOperatorWiseSales.getDblSubTotal() + rsOperatorDis.getDouble(4));
                        objOperatorWiseSales.setDblTaxAmt(objOperatorWiseSales.getDblTaxAmt() + rsOperatorDis.getDouble(6));
                        dis = objOperatorWiseSales.getDiscountAmt();
                        objOperatorWiseSales.setDiscountAmt(dis + rsOperatorDis.getDouble(5));
                        hmSettlementDtl.put(keys, objOperatorWiseSales);
                        break;
                    }
                    hmOperatorWiseSales.put(rsOperatorDis.getString(1), hmSettlementDtl);
                }
            }
            rsOperatorDis.close();

            rsOperatorDis = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQDisFile.toString());
            while (rsOperatorDis.next())
            {
                if (hmOperatorWiseSales.containsKey(rsOperatorDis.getString(1)))
                {
                    hmSettlementDtl = hmOperatorWiseSales.get(rsOperatorDis.getString(1));
                    Set<String> setKeys = hmSettlementDtl.keySet();
                    for (String keys : setKeys)
                    {
                        objOperatorWiseSales = hmSettlementDtl.get(keys);
                        objOperatorWiseSales.setDblSubTotal(objOperatorWiseSales.getDblSubTotal() + rsOperatorDis.getDouble(4));
                        objOperatorWiseSales.setDblTaxAmt(objOperatorWiseSales.getDblTaxAmt() + rsOperatorDis.getDouble(6));
                        dis = objOperatorWiseSales.getDiscountAmt();
                        objOperatorWiseSales.setDiscountAmt(dis + rsOperatorDis.getDouble(5));
                        hmSettlementDtl.put(keys, objOperatorWiseSales);
                        break;
                    }
                    hmOperatorWiseSales.put(rsOperatorDis.getString(1), hmSettlementDtl);
                }
            }
            rsOperatorDis.close();

            for (Map.Entry<String, Map<String, clsOperatorDtl>> entry : hmOperatorWiseSales.entrySet())
            {
                Map<String, clsOperatorDtl> hmOpSettlementDtl = entry.getValue();
                for (Map.Entry<String, clsOperatorDtl> entryOp : hmOpSettlementDtl.entrySet())
                {
                    clsOperatorDtl objOperatorDtl = entryOp.getValue();
                    listOperatorDtl.add(objOperatorDtl);
                }
            }

            sqlLive = " select ifnull(d.strSettelmentDesc,'') as payMode "
                    + " ,sum(c.dblSettlementAmt) "
                    + " from tblbillhd a "
                    + " left outer join tblbillsettlementdtl c on a.strBillNo=c.strBillNo and date(a.dteBillDate)=date(c.dteBillDate) "
                    + " left outer join tblsettelmenthd d on c.strSettlementCode=d.strSettelmentCode "
                    + " where date(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
                    + " and d.strSettelmentType!='Complementary' ";

            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlLive += " and a.strPOSCode= '" + posCode + "' ";
            }
            if (!shiftNo.equalsIgnoreCase("All"))
            {
                sqlLive += " and a.intShiftCode= '" + shiftNo + "' ";
            }

            sqlLive += " Group By d.strSettelmentDesc ";

            sqlQFile = " select ifnull(d.strSettelmentDesc,'') as payMode "
                    + " ,sum(c.dblSettlementAmt) "
                    + " from tblqbillhd a "
                    + " left outer join tblqbillsettlementdtl c on a.strBillNo=c.strBillNo and date(a.dteBillDate)=date(c.dteBillDate) "
                    + " left outer join tblsettelmenthd d on c.strSettlementCode=d.strSettelmentCode "
                    + " where date(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "'"
                    + " and d.strSettelmentType!='Complementary' ";

            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlQFile += " and a.strPOSCode= '" + posCode + "' ";
            }
            if (!shiftNo.equalsIgnoreCase("All"))
            {
                sqlQFile += " and a.intShiftCode= '" + shiftNo + "' ";
            }

            sqlQFile += " Group By d.strSettelmentDesc ";

            int previousListIndex = 0;
            List<clsOperatorDtl> listSettleDetail = new ArrayList<>();
            rsOperator = clsGlobalVarClass.dbMysql.executeResultSet(sqlLive);
            while (rsOperator.next())
            {
                boolean flgFound = false;
                if (!rsOperator.getString(1).isEmpty())
                {
                    objOperatorWiseSales = new clsOperatorDtl();
                    if (listSettleDetail.size() > 0)
                    {
                        for (int cnt = 0; cnt < listSettleDetail.size(); cnt++)
                        {
                            clsOperatorDtl objPreviousList = listSettleDetail.get(cnt);
                            if (objPreviousList.getStrSettlementDesc().equals(rsOperator.getString(1)))
                            {
                                double settleAmount = objPreviousList.getSettleAmt() + rsOperator.getDouble(2);
                                objOperatorWiseSales.setStrSettlementDesc(rsOperator.getString(1));
                                objOperatorWiseSales.setSettleAmt(settleAmount);
                                flgFound = true;
                                previousListIndex = cnt;
                            }
                        }

                    }
                    if (flgFound)
                    {
                        listSettleDetail.remove(previousListIndex);
                        listSettleDetail.add(objOperatorWiseSales);
                    }
                    else
                    {
                        objOperatorWiseSales.setStrSettlementDesc(rsOperator.getString(1));
                        objOperatorWiseSales.setSettleAmt(rsOperator.getDouble(2));
                        listSettleDetail.add(objOperatorWiseSales);
                    }
                }

            }
            rsOperator.close();

            rsOperator = clsGlobalVarClass.dbMysql.executeResultSet(sqlQFile);
            while (rsOperator.next())
            {
                boolean flgFound = false;
                if (!rsOperator.getString(1).isEmpty())
                {
                    objOperatorWiseSales = new clsOperatorDtl();
                    if (listSettleDetail.size() > 0)
                    {
                        for (int cnt = 0; cnt < listSettleDetail.size(); cnt++)
                        {
                            clsOperatorDtl objPreviousList = listSettleDetail.get(cnt);
                            if (objPreviousList.getStrSettlementDesc().equals(rsOperator.getString(1)))
                            {
                                double settleAmount = objPreviousList.getSettleAmt() + rsOperator.getDouble(2);
                                objOperatorWiseSales.setStrSettlementDesc(rsOperator.getString(1));
                                objOperatorWiseSales.setSettleAmt(settleAmount);
                                flgFound = true;
                                previousListIndex = cnt;

                            }
                        }

                    }
                    if (flgFound)
                    {
                        listSettleDetail.remove(previousListIndex);
                        listSettleDetail.add(objOperatorWiseSales);
                    }
                    else
                    {
                        objOperatorWiseSales.setStrSettlementDesc(rsOperator.getString(1));
                        objOperatorWiseSales.setSettleAmt(rsOperator.getDouble(2));
                        listSettleDetail.add(objOperatorWiseSales);
                    }
                }
            }
            rsOperator.close();

            sqlLive = " select ifnull(a.strBillNo,''),a.strReasonName,a.strRemark,a.strUserCreated as CreatedUser,a.dblActualAmount,a.strUserEdited as VoidedUser "
                    + " from tblvoidbillhd a "
                    + " where date(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
                    + " and strTransType='VB' ";

            if (!posCode.equalsIgnoreCase("All"))
            {
                sqlLive += " and a.strPosCode= '" + posCode + "' ";
            }
            if (!shiftNo.equalsIgnoreCase("All"))
            {
                sqlLive += " and a.intShiftCode= '" + shiftNo + "' ";
            }

            sqlLive += " Order By a.strBillNo ";

            List<clsOperatorDtl> listVoidBillDetail = new ArrayList<>();
            rsOperator = clsGlobalVarClass.dbMysql.executeResultSet(sqlLive);
            while (rsOperator.next())
            {
                objOperatorWiseSales = new clsOperatorDtl();
                objOperatorWiseSales.setStrUser(rsOperator.getString(1));
                objOperatorWiseSales.setStrUserName(rsOperator.getString(4));//created user
                objOperatorWiseSales.setReason(rsOperator.getString(2));
                objOperatorWiseSales.setRemark(rsOperator.getString(3));
                objOperatorWiseSales.setDblBillAmount(Double.parseDouble(rsOperator.getString(5)));
                objOperatorWiseSales.setStrVoidedUser(rsOperator.getString(6));//voided user

                listVoidBillDetail.add(objOperatorWiseSales);
            }
            rsOperator.close();

            Comparator<clsOperatorDtl> settleModeComparator = new Comparator<clsOperatorDtl>()
            {

                @Override
                public int compare(clsOperatorDtl o1, clsOperatorDtl o2)
                {
                    return o1.getStrSettlementDesc().compareTo(o2.getStrSettlementDesc());
                }
            };
            Comparator<clsOperatorDtl> billWiseComparator = new Comparator<clsOperatorDtl>()
            {

                @Override
                public int compare(clsOperatorDtl o1, clsOperatorDtl o2)
                {
                    return o1.getStrUserCode().compareTo(o2.getStrUserCode());
                }
            };

            Collections.sort(listOperatorDtl, new clsOperatorComparator(settleModeComparator, billWiseComparator));

            hm.put("listOfOperatorDtl", listOperatorDtl);
            hm.put("listOfBillSettleDtl", listSettleDetail);
            hm.put("listOfVoidBillDtl", listVoidBillDetail);

            //call for view report
            if (reportType.equalsIgnoreCase("A4 Size Report"))
            {
                funViewJasperReportForBeanCollectionDataSource(is, hm, listOperatorDtl);
            }
            if (reportType.equalsIgnoreCase("Excel Report"))
            {
                Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
                List<String> arrListTotal = new ArrayList<String>();
                List<String> arrHeaderList = new ArrayList<String>();
                double discAmt = 0, grandAmt = 0, subTotalAmt = 0, taxAmt = 0;
                double netTotal = 0, comissionAmt = 0;

                int i = 1;

                double totalTaxableAmount = 0;
                double totalTaxAmt = 0;
                //DecimalFormat gDecimalFormat = new DecimalFormat("0.00");
                for (int cnt = 0; cnt < listOperatorDtl.size(); cnt++)
                {
                    clsOperatorDtl objOperatorDtl = listOperatorDtl.get(cnt);
                    List<String> arrListItem = new ArrayList<String>();
                    arrListItem.add(objOperatorDtl.getStrUserCode()); //BillNo
                    arrListItem.add(objOperatorDtl.getStrSettlementDesc()); //payMode
                    arrListItem.add(String.valueOf(gDecimalFormat.format(objOperatorDtl.getDblSubTotal())));       //subTotal
                    arrListItem.add(String.valueOf(gDecimalFormat.format(objOperatorDtl.getDblTaxAmt())));       //taxAmt
                    arrListItem.add(String.valueOf(gDecimalFormat.format(objOperatorDtl.getDiscountAmt())));//disAmt
                    arrListItem.add(String.valueOf(gDecimalFormat.format(objOperatorDtl.getDblNetTotal())));//netTotal
                    arrListItem.add(String.valueOf(gDecimalFormat.format(objOperatorDtl.getSettleAmt())));//GrandTotal
                    arrListItem.add(String.valueOf(gDecimalFormat.format(objOperatorDtl.getDblThirdPartyComission())));//comission
                    arrListItem.add(objOperatorDtl.getStrComissionType());//comission type
                    arrListItem.add(objOperatorDtl.getStrComissionOn());//comission on
                    arrListItem.add(String.valueOf(gDecimalFormat.format(objOperatorDtl.getDblComission())));//comission Amt                                       
                    arrListItem.add(objOperatorDtl.getStrUserName());//CustomerName

                    discAmt = discAmt + objOperatorDtl.getDiscountAmt();
                    taxAmt = taxAmt + objOperatorDtl.getDblTaxAmt();
                    subTotalAmt = subTotalAmt + objOperatorDtl.getDblSubTotal();

                    netTotal = netTotal + objOperatorDtl.getDblNetTotal();
                    comissionAmt = comissionAmt + objOperatorDtl.getDblComission();

                    grandAmt = grandAmt + objOperatorDtl.getSettleAmt();

                    mapExcelItemDtl.put(i, arrListItem);

                    i++;
                }

                arrListTotal.add(String.valueOf(gDecimalFormat.format(subTotalAmt)) + "#" + "3");
                arrListTotal.add(String.valueOf(gDecimalFormat.format((taxAmt))) + "#" + "4");
                arrListTotal.add(String.valueOf(gDecimalFormat.format((discAmt))) + "#" + "5");
                arrListTotal.add(String.valueOf(gDecimalFormat.format((netTotal))) + "#" + "6");
                arrListTotal.add(String.valueOf(gDecimalFormat.format((grandAmt))) + "#" + "7");
                arrListTotal.add("" + "#" + "8");//comi%
                arrListTotal.add(String.valueOf(gDecimalFormat.format((comissionAmt))) + "#" + "11");

                arrHeaderList.add("Serial No");
                arrHeaderList.add("Bill No");
                arrHeaderList.add("Pay Mode");
                arrHeaderList.add("Subtotal");
                arrHeaderList.add("Tax Amt");
                arrHeaderList.add("Discount Amt");
                arrHeaderList.add("Net Total");
                arrHeaderList.add("Grand Total");
                arrHeaderList.add("Comission");
                arrHeaderList.add("Comission Type");
                arrHeaderList.add("Comission On ");
                arrHeaderList.add("Comission Amt");
                arrHeaderList.add("Customer Name");

                List<String> arrparameterList = new ArrayList<String>();
                arrparameterList.add("Auditor Report");
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
                //objUtility.funCreateExcelSheet(arrparameterList,arrHeaderList,mapExcelItemDtl, arrListTotal,"taxBreakupExcelSheet");

                String filePath = System.getProperty("user.dir");
                File file = new File(filePath + File.separator + "Reports" + File.separator + "audiorExcelSheetReport" + ".xls");
                WritableWorkbook workbook1 = Workbook.createWorkbook(file);
                WritableSheet sheet1 = workbook1.createSheet("First Sheet", 0);
                WritableFont cellFont = new WritableFont(WritableFont.COURIER, 14);
                cellFont.setBoldStyle(WritableFont.BOLD);
                WritableCellFormat cellFormat = new WritableCellFormat(cellFont);
                WritableFont cellFont1 = new WritableFont(WritableFont.COURIER, 12);
                cellFont1.setBoldStyle(WritableFont.BOLD);
                WritableCellFormat cellFormat1 = new WritableCellFormat(cellFont1);
                WritableFont headerCellFont = new WritableFont(WritableFont.TIMES, 10);
                headerCellFont.setBoldStyle(WritableFont.BOLD);
                WritableCellFormat headerCell = new WritableCellFormat(headerCellFont);

                for (int j = 0; j <= arrparameterList.size(); j++)
                {
                    Label l0 = new Label(1, 0, arrparameterList.get(0), cellFormat);
                    Label l1 = new Label(0, 2, arrparameterList.get(1), headerCell);
                    Label l2 = new Label(1, 2, arrparameterList.get(2), headerCell);
                    Label l3 = new Label(2, 2, arrparameterList.get(3), headerCell);
                    Label l4 = new Label(0, 3, arrparameterList.get(4), headerCell);
                    Label l5 = new Label(1, 3, arrparameterList.get(5), headerCell);

                    sheet1.addCell(l0);
                    sheet1.addCell(l1);
                    sheet1.addCell(l2);
                    sheet1.addCell(l3);
                    sheet1.addCell(l4);
                    sheet1.addCell(l5);
                }

                Label labelAuditor = new Label(0, 5, "Auditor", cellFormat1);
                sheet1.addCell(labelAuditor);
                sheet1.setColumnView(5, 15);

                for (int j = 0; j <= arrHeaderList.size(); j++)
                {
                    Label l0 = new Label(0, 7, arrHeaderList.get(0), headerCell);
                    Label l1 = new Label(1, 7, arrHeaderList.get(1), headerCell);
                    Label l2 = new Label(2, 7, arrHeaderList.get(2), headerCell);
                    Label l3 = new Label(3, 7, arrHeaderList.get(3), headerCell);
                    Label l4 = new Label(4, 7, arrHeaderList.get(4), headerCell);
                    Label l5 = new Label(5, 7, arrHeaderList.get(5), headerCell);
                    Label l6 = new Label(6, 7, arrHeaderList.get(6), headerCell);
                    Label l7 = new Label(7, 7, arrHeaderList.get(7), headerCell);
                    Label l8 = new Label(8, 7, arrHeaderList.get(8), headerCell);
                    Label l9 = new Label(9, 7, arrHeaderList.get(9), headerCell);
                    Label l10 = new Label(10, 7, arrHeaderList.get(10), headerCell);
                    Label l11 = new Label(11, 7, arrHeaderList.get(11), headerCell);
                    Label l12 = new Label(12, 7, arrHeaderList.get(12), headerCell);

                    sheet1.addCell(l0);
                    sheet1.addCell(l1);
                    sheet1.addCell(l2);
                    sheet1.addCell(l3);
                    sheet1.addCell(l4);
                    sheet1.addCell(l5);
                    sheet1.addCell(l6);
                    sheet1.addCell(l7);
                    sheet1.addCell(l8);
                    sheet1.addCell(l9);
                    sheet1.addCell(l10);
                    sheet1.addCell(l11);
                    sheet1.addCell(l12);

                }

                i = 9;
                for (Map.Entry<Integer, List<String>> entry : mapExcelItemDtl.entrySet())
                {
                    Label lbl0 = new Label(0, i, entry.getKey().toString());
                    List<String> nameList = mapExcelItemDtl.get(entry.getKey());
                    for (int j = 0; j <= nameList.size(); j++)
                    {
                        Label lbl1 = new Label(1, i, nameList.get(0));
                        Label lbl2 = new Label(2, i, nameList.get(1));
                        Label lbl3 = new Label(3, i, nameList.get(2));
                        Label lbl4 = new Label(4, i, nameList.get(3));
                        Label lbl5 = new Label(5, i, nameList.get(4));
                        Label lbl6 = new Label(6, i, nameList.get(5));
                        Label lbl7 = new Label(7, i, nameList.get(6));

                        Label lbl8 = new Label(8, i, nameList.get(7));
                        Label lbl9 = new Label(9, i, nameList.get(8));
                        Label lbl10 = new Label(10, i, nameList.get(9));
                        Label lbl11 = new Label(11, i, nameList.get(10));
                        Label lbl12 = new Label(12, i, nameList.get(11));

                        sheet1.addCell(lbl1);
                        sheet1.addCell(lbl2);
                        sheet1.addCell(lbl3);
                        sheet1.addCell(lbl4);
                        sheet1.addCell(lbl5);
                        sheet1.addCell(lbl6);
                        sheet1.addCell(lbl7);
                        sheet1.addCell(lbl8);
                        sheet1.addCell(lbl9);
                        sheet1.addCell(lbl10);

                        sheet1.addCell(lbl11);
                        sheet1.addCell(lbl12);

                        sheet1.setColumnView(i, 15);
                    }
                    sheet1.addCell(lbl0);
                    i++;
                }

                for (int j = 0; j < arrListTotal.size(); j++)
                {
                    String[] l0 = new String[10];
                    for (int c = 0; c < arrListTotal.size(); c++)
                    {
                        l0 = arrListTotal.get(c).split("#");
                        int position = Integer.parseInt(l0[1]);
                        Label lable0 = new Label(position, i + 1, l0[0], headerCell);
//                        Label lable1 = new Label(position, i + 1, l0[1], headerCell);
//                        Label lable2 = new Label(position, i + 1, l0[2], headerCell);
//                        Label lable3 = new Label(position, i + 1, l0[3], headerCell);

                        sheet1.addCell(lable0);
//                        sheet1.addCell(lable1);
//                        sheet1.addCell(lable2);
//                        sheet1.addCell(lable3);

                    }
                    Label labelTotal = new Label(0, i + 1, "TOTAL:", headerCell);
                    sheet1.addCell(labelTotal);
                }

                // Menu Head Wise Tax Break up        
                i += 4;
                mapExcelItemDtl.clear();
                arrListTotal.clear();
                arrHeaderList.clear();
                arrHeaderList.add("");
                arrHeaderList.add("Voided Bill");
                arrHeaderList.add("Created User");
                arrHeaderList.add("Voided User");
                arrHeaderList.add("Reason");
                arrHeaderList.add("Remark");
                arrHeaderList.add("Bill Amount");
                int h = i - 1;
                Label labelVoidedBill = new Label(0, h, "", cellFormat1);
                sheet1.addCell(labelVoidedBill);
                sheet1.setColumnView(h, 15);
                for (int j = 0; j <= arrHeaderList.size(); j++)
                {
                    Label l0 = new Label(0, i, arrHeaderList.get(0), headerCell);
                    Label l1 = new Label(1, i, arrHeaderList.get(1), headerCell);
                    Label l2 = new Label(2, i, arrHeaderList.get(2), headerCell);
                    Label l3 = new Label(3, i, arrHeaderList.get(3), headerCell);
                    Label l4 = new Label(4, i, arrHeaderList.get(4), headerCell);
                    Label l5 = new Label(5, i, arrHeaderList.get(5), headerCell);
                    Label l6 = new Label(5, i, arrHeaderList.get(6), headerCell);

                    sheet1.addCell(l0);
                    sheet1.addCell(l1);
                    sheet1.addCell(l2);
                    sheet1.addCell(l3);
                    sheet1.addCell(l4);
                    sheet1.addCell(l5);
                    sheet1.addCell(l6);
                }

                i = i + 1;
                int count = 1;
                
                for (int cnt = 0; cnt < listVoidBillDetail.size(); cnt++)
                {
                    clsOperatorDtl objOperator = listVoidBillDetail.get(cnt);
                    List<String> arrListItem = new ArrayList<String>();
                    arrListItem.add(objOperator.getStrUser());
                    arrListItem.add(objOperator.getStrUserName());
                    arrListItem.add(objOperator.getStrVoidedUser());
                    arrListItem.add(objOperator.getReason());
                    arrListItem.add(objOperator.getRemark());
                    arrListItem.add(String.valueOf(gDecimalFormat.format(objOperator.getDblBillAmount())));
                    mapExcelItemDtl.put(count, arrListItem);
                    count++;
                }
                for (Map.Entry<Integer, List<String>> entry : mapExcelItemDtl.entrySet())
                {
                    Label lbl0 = new Label(0, i, entry.getKey().toString());
                    List<String> nameList = mapExcelItemDtl.get(entry.getKey());
                    for (int j = 0; j <= nameList.size(); j++)
                    {
                        Label lbl1 = new Label(1, i, nameList.get(0));
                        Label lbl2 = new Label(2, i, nameList.get(1));
                        Label lbl3 = new Label(3, i, nameList.get(2));
                        Label lbl4 = new Label(4, i, nameList.get(3));
                        Label lbl5 = new Label(5, i, nameList.get(4));
                        Label lbl6 = new Label(6, i, nameList.get(5));
                        sheet1.addCell(lbl1);
                        sheet1.addCell(lbl2);
                        sheet1.addCell(lbl3);
                        sheet1.addCell(lbl4);
                        sheet1.addCell(lbl5);
                        sheet1.addCell(lbl6);
                        sheet1.setColumnView(i, 15);
                    }
                    sheet1.addCell(lbl0);
                    i++;
                }

                arrHeaderList.add("Pay Mode Description");
                arrHeaderList.add("Settle Amt");

                int k = i + 1;
                Label labelPayModeCollection = new Label(0, k, "Pay Mode Wise Collection", cellFormat1);
                sheet1.addCell(labelPayModeCollection);
                sheet1.setColumnView(k, 15);

                i = k + 1;
                for (int j = 0; j <= arrHeaderList.size(); j++)
                {
                    Label l0 = new Label(0, i, arrHeaderList.get(0), headerCell);
                    Label l1 = new Label(1, i, arrHeaderList.get(1), headerCell);

                    sheet1.addCell(l0);
                    sheet1.addCell(l1);
                }
                mapExcelItemDtl.clear();
                arrListTotal.clear();
                count = 1;
                double grandTotal = 0.0;
                for (int j = 0; j < listSettleDetail.size(); j++)
                {
                    clsOperatorDtl objOperator = listSettleDetail.get(j);
                    List<String> arrListItem = new ArrayList<String>();
                    arrListItem.add(objOperator.getStrSettlementDesc());
                    arrListItem.add(String.valueOf(gDecimalFormat.format(objOperator.getSettleAmt())));
                    grandTotal = grandTotal + objOperator.getSettleAmt();

                    mapExcelItemDtl.put(count, arrListItem);
                    count++;
                }
                arrListTotal.add(String.valueOf(gDecimalFormat.format((grandTotal))) + "#" + "2");

                for (Map.Entry<Integer, List<String>> entry : mapExcelItemDtl.entrySet())
                {
                    Label lbl0 = new Label(0, i, entry.getKey().toString());
                    List<String> nameList = mapExcelItemDtl.get(entry.getKey());
                    for (int j = 0; j <= nameList.size(); j++)
                    {
                        Label lbl1 = new Label(1, i, nameList.get(0));
                        Label lbl2 = new Label(2, i, nameList.get(1));

                        sheet1.addCell(lbl1);
                        sheet1.addCell(lbl2);

                        sheet1.setColumnView(i, 15);
                    }
                    sheet1.addCell(lbl0);
                    i++;
                }

                for (int j = 0; j < arrListTotal.size(); j++)
                {
                    String[] l0 = new String[10];
                    for (int c = 0; c < arrListTotal.size(); c++)
                    {
                        l0 = arrListTotal.get(c).split("#");
                        int position = Integer.parseInt(l0[1]);
                        Label lable0 = new Label(position, i + 1, l0[0], headerCell);
                        sheet1.addCell(lable0);
                    }
                    Label labelTotal = new Label(0, i + 1, "Grand Total:", headerCell);
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
