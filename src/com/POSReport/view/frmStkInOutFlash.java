/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.POSReport.view;

import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsUtility;
import com.POSGlobal.view.frmOkPopUp;
import com.POSGlobal.view.frmSearchFormDialog;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class frmStkInOutFlash extends javax.swing.JFrame
{

    String strReasonName, sql, strTaxIndicator;
    DefaultTableModel dm, totalDm;
    java.util.Vector vResonCode;

    double sumAmount;
    private Map hmPOS, hmReason;
    private String textSearchTypeCode = "";
    clsUtility objUtility;
    private String supplierCode;
    private String supplierName;
    private DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();

    public frmStkInOutFlash()
    {
	initComponents();
	try
	{
	    Timer timer = new Timer(500, new ActionListener()
	    {
		@Override
		public void actionPerformed(ActionEvent e)
		{
		    Date date1 = new Date();
		    String newstr = String.format("%tr", date1);
		    String dateAndTime = clsGlobalVarClass.gPOSDateToDisplay + " " + newstr;
		    lblDate.setText(dateAndTime);
		}
	    });
	    timer.setRepeats(true);
	    timer.setCoalesce(true);
	    timer.setInitialDelay(0);
	    timer.start();
	    lblUserCode.setText(clsGlobalVarClass.gUserCode);
	    lblPosName.setText(clsGlobalVarClass.gPOSName);
	    lblModuleName.setText(clsGlobalVarClass.gSelectedModule);

	    funSetLookAndFeel();
	    this.setLocationRelativeTo(null);

	    objUtility = new clsUtility();

	    hmPOS = new HashMap<String, String>();
	    hmReason = new HashMap<String, String>();
	    txtTransTypeCode.setEnabled(false);
	    txtTransTypeCode.setEditable(false);
	    dteFromDate.setDate(objUtility.funGetDateToSetCalenderDate());
	    dteToDate.setDate(objUtility.funGetDateToSetCalenderDate());
	    funFillComboBox();

	    funFillReasonCombo();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	if (clsGlobalVarClass.gNoOfDaysReportsView != 0)
	{
	    try
	    {

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		final Date userDateRange = dateFormat.parse(clsGlobalVarClass.gPOSOnlyDateForTransaction);
		int days = userDateRange.getDate() - clsGlobalVarClass.gNoOfDaysReportsView;
		userDateRange.setDate(days);

		dteFromDate.getJCalendar().setMinSelectableDate(userDateRange);

		dteFromDate.getDateEditor().addPropertyChangeListener(new PropertyChangeListener()
		{
		    @Override
		    public void propertyChange(PropertyChangeEvent e)
		    {
			if ("date".equals(e.getPropertyName()))
			{
			    Date dateChooserValue = (Date) e.getNewValue();

			    if (clsGlobalVarClass.gNoOfDaysReportsView != 0 && dateChooserValue.before(userDateRange))
			    {
				try
				{
				    java.util.Date date = new SimpleDateFormat("dd-MM-yyyy").parse(clsGlobalVarClass.gPOSDateToDisplay);
				    dteFromDate.setDate(date);
				}
				catch (Exception ex)
				{
				    ex.printStackTrace();
				}
			    }
			}
		    }
		});
	    }
	    catch (Exception e)
	    {
		e.printStackTrace();
	    }
	}
    }

    /**
     * this function is used Filling POS Code ComboBoxs
     */
    private void funFillComboBox() throws Exception
    {
	cmbTransType.addItem("Stock In");
	cmbTransType.addItem("Stock Out");

	cmbSearchType.addItem("Item wise");
	cmbSearchType.addItem("MenuHead wise");
	cmbSearchType.addItem("SubGroup wise");
	cmbSearchType.addItem("Group wise");

	if (clsGlobalVarClass.gShowOnlyLoginPOSReports)
	{
	    hmPOS.put(clsGlobalVarClass.gPOSName, clsGlobalVarClass.gPOSCode);
	}
	else
	{
	    sql = "select strPosCode,strPosName from tblposmaster";
	    ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(sql);
	    hmPOS.put("All", "All");
	    while (rs.next())
	    {
		hmPOS.put(rs.getString(2), rs.getString(1));
	    }
	    rs.close();
	}

	Set setPOS = hmPOS.keySet();
	Iterator itrPOS = setPOS.iterator();
	cmbPOSCode.removeAllItems();
	//cmbPOSCode.addItem("All");
	while (itrPOS.hasNext())
	{
	    cmbPOSCode.addItem(itrPOS.next());
	}
    }

    private void funFillReasonCombo() throws Exception
    {
	hmReason.clear();
	hmReason.put("All", "All");
	sql = "select strReasonCode,strReasonName from tblreasonmaster where strStkIn='Y' or strStkOut='Y'";
	ResultSet rsReason = clsGlobalVarClass.dbMysql.executeResultSet(sql);
	cmbReason.addItem("All");
	while (rsReason.next())
	{
	    hmReason.put(rsReason.getString(2), rsReason.getString(1));
	    cmbReason.addItem(rsReason.getString(2));
	}
	rsReason.close();
    }

    //Get date into yyyy-mm-dd format
    private String funGetCalenderDate(Date objDate)
    {
	return (objDate.getYear() + 1900) + "-" + (objDate.getMonth() + 1) + "-" + (objDate.getDate());
    }

    //Get date into yyyy-mm-dd format
    private String funGetCalenderDateInPOSDateFormat(Date objDate)
    {
	return (objDate.getDate()) + "-" + (objDate.getMonth() + 1) + "-" + (objDate.getYear() + 1900);
    }

    /**
     * ]
     * this function is used Filling table
     */
    private void funFillStockTable(String tableName, String fieldName, String strCode) throws Exception
    {
	StringBuilder sbSql = new StringBuilder();

	dm = new DefaultTableModel()
	{
	    @Override
	    public boolean isCellEditable(int row, int column)
	    {
		//all cells false
		return false;
	    }
	};
	String pos = cmbPOSCode.getSelectedItem().toString().toString();
	String POSCode = hmPOS.get(cmbPOSCode.getSelectedItem().toString()).toString();
	String fromDate = funGetCalenderDate(dteFromDate.getDate());
	String toDate = funGetCalenderDate(dteToDate.getDate());
//        String reason = cmbReason.getSelectedItem().toString().toString();
	String reasonCode = hmReason.get(cmbReason.getSelectedItem().toString()).toString();

	if (txtSupplierName.getText().trim().length() > 0)
	{

	}
	else
	{
	    supplierCode = "";
	    supplierName = "";
	}

	lblBillDate.setText("");
	lblBillNo.setText("");

	dm.addColumn("Item Name");
	dm.addColumn("Qty");
	dm.addColumn("Purchase Rate");
	dm.addColumn("Amount");
	dm.addColumn("Tax");

	totalDm = new DefaultTableModel()
	{
	    @Override
	    public boolean isCellEditable(int row, int column)
	    {
		//all cells false
		return false;
	    }
	};
	totalDm.getDataVector().removeAllElements();
	totalDm.addColumn("");
	totalDm.addColumn("");
	totalDm.addColumn("");
	totalDm.addColumn("");
	totalDm.addColumn("");
	totalDm.addColumn("");
	tblTotal.updateUI();
	double dblAmount = 0.00, dblPurchaseRate = 0.00;
	double sumPurchaseRate = 0.00, dblPenalty = 0.00, sumPenalty = 0.00;
	sumAmount = 0.00;
	int sumQty = 0;

	sbSql.setLength(0);
	if (cmbTransType.getSelectedItem().equals("Stock Out"))
	{
	    dm.addColumn("StockOut No");
	    dm.addColumn("Bill No");
	    dm.addColumn("StockOut Date");
	    dm.addColumn("POS Name");
	    dm.addColumn("Reason");

	    if (cmbSearchType.getSelectedItem().equals("Item wise"))
	    {
		sbSql.append(" SELECT c.strItemName,sum(b.dblQuantity),b.dblPurchaseRate,sum(b.dblAmount) "
			+ ",a.strStkOutCode,a.dteStkOutDate,d.strPosName,g.strReasonName,a.strPurchaseBillNo "
			+ ",e.strPOCode,e.strSupplierCode,f.strSupplierName,sum(a.dblTaxAmt) "
			+ "FROM tblstkouthd a  "
			+ "join tblstkoutdtl b on a.strStkOutCode=b.strStkOutCode  "
			+ "join tblitemmaster c on c.strItemCode=b.strItemCode  "
			+ "join tblposmaster d on a.strPOSCode=d.strPosCode "
			+ "left join tblpurchaseorderhd e on a.strPurchaseBillNo=e.strPOCode "
			+ "left join tblsuppliermaster f on e.strSupplierCode=f.strSupplierCode "
			+ "left join tblreasonmaster g on a.strReasonCode=g.strReasonCode "
			+ " where date( a.dteStkOutDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' ");
		if (!pos.equals("All"))
		{
		    sbSql.append("  and a.strPOSCode='" + POSCode + "'  ");
		}
		if (!txtTransTypeCode.getText().isEmpty())
		{
		    sbSql.append(" and a.strStkOutCode='" + txtTransTypeCode.getText().trim() + "'");
		}
		if (!txtSearchType.getText().isEmpty())
		{
		    sbSql.append(" and b.strItemCode='" + textSearchTypeCode + "'");
		}
		if (!reasonCode.equals("All"))
		{
		    sbSql.append("  and a.strReasonCode='" + reasonCode + "'  ");
		}
		if (!supplierCode.isEmpty())
		{
		    sbSql.append("  and a.strSupplierCode='" + supplierCode + "'  ");
		}

		sbSql.append(" group by b.strItemCode,date( a.dteStkOutDate ),a.strPOSCOde,a.strStkOutCode "
			+ " order by date( a.dteStkOutDate ),c.strItemName asc ");
	    }
	    else if (cmbSearchType.getSelectedItem().equals("MenuHead wise"))
	    {
		sbSql.append(" select e.strMenuName ,sum(b.dblQuantity),b.dblPurchaseRate,sum(b.dblAmount),a.strStkOutCode"
			+ ",a.dteStkOutDate,f.strPosName,g.strReasonName,e.strMenuCode,sum(a.dblTaxAmt) "
			+ " from tblstkouthd a,tblstkoutdtl b,tblitemmaster c,tblmenuitempricingdtl d ,tblmenuhd e,tblposmaster f "
			+ ",tblreasonmaster g "
			+ " where b.strItemCode=d.strItemCode and d.strMenuCode=e.strMenuCode "
			+ " and a.strStkOutCode=b.strStkOutCode and c.strItemCode=b.strItemCode and a.strPOSCode=d.strPosCode "
			+ " and a.strPOSCode=f.strPosCode "
			+ " and date( a.dteStkOutDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
			+ " and a.strReasonCode=g.strReasonCode ");
		if (!pos.equals("All"))
		{
		    sbSql.append("  and a.strPOSCode='" + POSCode + "'  ");
		}
		if (!txtTransTypeCode.getText().isEmpty())
		{
		    sbSql.append(" and a.strStkOutCode='" + txtTransTypeCode.getText().trim() + "'");
		}
		if (!txtSearchType.getText().isEmpty())
		{
		    sbSql.append(" and e.strMenuCode='" + textSearchTypeCode + "'");
		}
		if (!reasonCode.equals("All"))
		{
		    sbSql.append("  and a.strReasonCode='" + reasonCode + "'  ");
		}
		sbSql.append(" group by e.strMenuName,date(a.dteStkOutDate),a.strPOSCOde,a.strStkOutCode "
			+ " order by date( a.dteStkOutDate ),e.strMenuName asc ");
	    }
	    else if (cmbSearchType.getSelectedItem().equals("SubGroup wise"))
	    {
		sbSql.append(" select d.strSubGroupName ,sum(b.dblQuantity),b.dblPurchaseRate,sum(b.dblAmount)"
			+ ",a.strStkOutCode,a.dteStkOutDate,e.strPosName,f.strReasonName,d.strSubGroupCode,sum(a.dblTaxAmt) "
			+ " from tblstkouthd a,tblstkoutdtl b,tblitemmaster c,tblsubgrouphd d,tblposmaster e "
			+ ",tblreasonmaster f"
			+ " where c.strSubGroupCode=d.strSubGroupCode "
			+ " and a.strStkOutCode=b.strStkOutCode and c.strItemCode=b.strItemCode and a.strPOSCode=e.strPosCode"
			+ " and date( a.dteStkOutDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
			+ "and a.strReasonCode=f.strReasonCode ");
		if (!pos.equals("All"))
		{
		    sbSql.append("  and a.strPOSCode='" + POSCode + "'  ");
		}
		if (!txtTransTypeCode.getText().isEmpty())
		{
		    sbSql.append(" and a.strStkOutCode='" + txtTransTypeCode.getText().trim() + "'");
		}
		if (!txtSearchType.getText().isEmpty())
		{
		    sbSql.append(" and d.strSubGroupCode='" + textSearchTypeCode + "'");
		}
		if (!reasonCode.equals("All"))
		{
		    sbSql.append("  and a.strReasonCode='" + reasonCode + "'  ");
		}
		sbSql.append(" group by d.strSubGroupName,date(a.dteStkOutDate),a.strPOSCOde,a.strStkOutCode "
			+ " order by date( a.dteStkOutDate ),d.strSubGroupName asc ");
	    }
	    else if (cmbSearchType.getSelectedItem().equals("Group wise"))
	    {
		sbSql.append(" select e.strGroupName,sum(b.dblQuantity),b.dblPurchaseRate,sum(b.dblAmount),a.strStkOutCode"
			+ ",a.dteStkOutDate,f.strPosName,g.strReasonName,e.strGroupCode,sum(a.dblTaxAmt) "
			+ " from tblstkouthd a,tblstkoutdtl b,tblitemmaster c,tblsubgrouphd d,tblgrouphd e,tblposmaster f "
			+ ",tblreasonmaster g"
			+ " where c.strSubGroupCode=d.strSubGroupCode and d.strGroupCode=e.strGroupCode "
			+ " and a.strStkOutCode=b.strStkOutCode and c.strItemCode=b.strItemCode and a.strPOSCode=f.strPosCode"
			+ " and date( a.dteStkOutDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
			+ " and a.strReasonCode=g.strReasonCode ");
		if (!pos.equals("All"))
		{
		    sbSql.append("  and a.strPOSCode='" + POSCode + "'  ");
		}
		if (!txtTransTypeCode.getText().isEmpty())
		{
		    sbSql.append(" and a.strStkOutCode='" + txtTransTypeCode.getText().trim() + "'");
		}
		if (!txtSearchType.getText().isEmpty())
		{
		    sbSql.append(" and e.strGroupCode='" + textSearchTypeCode + "'");
		}
		if (!reasonCode.equals("All"))
		{
		    sbSql.append("  and a.strReasonCode='" + reasonCode + "'  ");
		}
		sbSql.append(" group by e.strGroupName,date(a.dteStkOutDate),a.strPOSCOde,a.strStkOutCode "
			+ " order by date( a.dteStkOutDate ),e.strGroupName asc ");
	    }
	}
	else
	{
	    dm.addColumn("StockIn No.");
	    dm.addColumn("Bill No.");
	    dm.addColumn("StockIn Date");
	    dm.addColumn("POS Name");
	    dm.addColumn("Reason");

	    if (cmbSearchType.getSelectedItem().equals("Item wise"))
	    {
		sbSql.append(" SELECT c.strItemName,b.dblQuantity,b.dblPurchaseRate,b.dblAmount,a.strStkInCode,a.dteStkInDate"
			+ ",d.strPosName,g.strReasonName,a.strPurchaseBillNo "
			+ ",e.strPOCode,e.strSupplierCode,f.strSupplierName,sum(a.dblTaxAmt) "
			+ "FROM tblstkinhd a "
			+ "join tblstkindtl b on a.strStkInCode=b.strStkInCode "
			+ "join tblitemmaster c on c.strItemCode=b.strItemCode  "
			+ "join tblposmaster d on a.strPOSCode=d.strPosCode  "
			+ "left join tblpurchaseorderhd e on a.strPurchaseBillNo=e.strPOCode "
			+ "left join tblsuppliermaster f on e.strSupplierCode=f.strSupplierCode "
			+ "left join tblreasonmaster g on a.strReasonCode=g.strReasonCode "
			+ " where date( a.dteStkInDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
			+ "and a.strReasonCode=g.strReasonCode ");
		if (!pos.equals("All"))
		{
		    sbSql.append("  and a.strPOSCode='" + POSCode + "'  ");
		}
		if (!txtTransTypeCode.getText().isEmpty())
		{
		    sbSql.append(" and a.strStkInCode='" + txtTransTypeCode.getText().trim() + "'");
		}
		if (!txtSearchType.getText().isEmpty())
		{
		    sbSql.append(" and b.strItemCode='" + textSearchTypeCode + "'");
		}
		if (!reasonCode.equals("All"))
		{
		    sbSql.append("  and a.strReasonCode='" + reasonCode + "'  ");
		}
		if (!supplierCode.isEmpty())
		{
		    sbSql.append("  and a.strSupplierCode='" + supplierCode + "'  ");
		}
		sbSql.append(" group by date(a.dteStkInDate),a.strStkInCode,a.strPOSCode,b.strItemCode "
			+ "ORDER BY DATE(a.dteStkInDate),c.strItemName ASC ");
	    }
	    else if (cmbSearchType.getSelectedItem().equals("MenuHead wise"))
	    {
		sbSql.append(" select e.strMenuName,sum(b.dblQuantity),b.dblPurchaseRate,sum(b.dblAmount),a.strStkInCode"
			+ ",a.dteStkInDate,f.strPosName,g.strReasonName,e.strMenuCode,sum(a.dblTaxAmt) "
			+ " from tblstkinhd a,tblstkindtl b,tblitemmaster c,tblmenuitempricingdtl d ,tblmenuhd e,tblposmaster f"
			+ ",tblreasonmaster g "
			+ " where b.strItemCode=d.strItemCode and d.strMenuCode=e.strMenuCode "
			+ " and a.strStkInCode=b.strStkInCode and c.strItemCode=b.strItemCode and a.strPOSCode=d.strPosCode "
			+ " and a.strPOSCode=f.strPosCode "
			+ " and date( a.dteStkInDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
			+ "and a.strReasonCode=g.strReasonCode ");
		if (!pos.equals("All"))
		{
		    sbSql.append(" and a.strPOSCode='" + POSCode + "'  ");
		}
		if (!txtTransTypeCode.getText().isEmpty())
		{
		    sbSql.append(" and a.strStkInCode='" + txtTransTypeCode.getText().trim() + "'");
		}
		if (!txtSearchType.getText().isEmpty())
		{
		    sbSql.append(" and e.strMenuCode='" + textSearchTypeCode + "'");
		}
		if (!reasonCode.equals("All"))
		{
		    sbSql.append("  and a.strReasonCode='" + reasonCode + "'  ");
		}
		sbSql.append(" group by e.strMenuName,date(a.dteStkInDate),a.strPOSCOde,a.strStkInCode "
			+ " order by date( a.dteStkInDate ),e.strMenuName asc ");
	    }
	    else if (cmbSearchType.getSelectedItem().equals("SubGroup wise"))
	    {
		sbSql.append(" select d.strSubGroupName,sum(b.dblQuantity),b.dblPurchaseRate,sum(b.dblAmount),a.strStkInCode"
			+ ",a.dteStkInDate,e.strPosName,f.strReasonName,d.strSubGroupCode,sum(a.dblTaxAmt) "
			+ " from tblstkinhd a,tblstkindtl b,tblitemmaster c,tblsubgrouphd d,tblposmaster e,tblreasonmaster f  "
			+ " where c.strSubGroupCode=d.strSubGroupCode "
			+ " and a.strStkInCode=b.strStkInCode and c.strItemCode=b.strItemCode and a.strPOSCode=e.strPosCode "
			+ " and date( a.dteStkInDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
			+ "and a.strReasonCode=f.strReasonCode ");
		if (!pos.equals("All"))
		{
		    sbSql.append("  and a.strPOSCode='" + POSCode + "'  ");
		}
		if (!txtTransTypeCode.getText().isEmpty())
		{
		    sbSql.append(" and a.strStkInCode='" + txtTransTypeCode.getText().trim() + "'");
		}
		if (!txtSearchType.getText().isEmpty())
		{
		    sbSql.append(" and c.strSubGroupCode='" + textSearchTypeCode + "'");
		}
		if (!reasonCode.equals("All"))
		{
		    sbSql.append("  and a.strReasonCode='" + reasonCode + "'  ");
		}
		sbSql.append(" group by d.strSubGroupName,date(a.dteStkInDate),a.strPOSCOde,a.strStkInCode "
			+ " order by date( a.dteStkInDate ),d.strSubGroupName asc ");
	    }
	    else if (cmbSearchType.getSelectedItem().equals("Group wise"))
	    {
		sbSql.append(" select e.strGroupName,sum(b.dblQuantity),b.dblPurchaseRate,sum(b.dblAmount),a.strStkInCode"
			+ ",a.dteStkInDate,f.strPosName,g.strReasonName,e.strGroupCode,sum(a.dblTaxAmt) "
			+ " from tblstkinhd a,tblstkindtl b,tblitemmaster c,tblsubgrouphd d,tblgrouphd e,tblposmaster f "
			+ ",tblreasonmaster g "
			+ " where c.strSubGroupCode=d.strSubGroupCode and d.strGroupCode=e.strGroupCode "
			+ " and a.strStkInCode=b.strStkInCode and c.strItemCode=b.strItemCode and a.strPOSCode=f.strPosCode  "
			+ " and date( a.dteStkInDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
			+ "and a.strReasonCode=g.strReasonCode ");
		if (!pos.equals("All"))
		{
		    sbSql.append("  and a.strPOSCode='" + POSCode + "'  ");
		}
		if (!txtTransTypeCode.getText().isEmpty())
		{
		    sbSql.append(" and a.strStkInCode='" + txtTransTypeCode.getText().trim() + "'");
		}
		if (!txtSearchType.getText().isEmpty())
		{
		    sbSql.append(" and e.strGroupCode='" + textSearchTypeCode + "'");
		}
		if (!reasonCode.equals("All"))
		{
		    sbSql.append("  and a.strReasonCode='" + reasonCode + "'  ");
		}
		sbSql.append(" group by e.strGroupName,date(a.dteStkInDate),a.strPOSCOde,a.strStkInCode "
			+ " order by date( a.dteStkInDate ),e.strGroupName asc ");
	    }
	}

	System.out.println(cmbTransType.getSelectedItem() + "=" + sbSql.toString());
	ResultSet itemInfo = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
	double totalTaxAmt = 0.00;
	while (itemInfo.next())
	{
	    dblPurchaseRate = itemInfo.getDouble(3);
	    dblAmount = itemInfo.getDouble(4);
	    sumQty = sumQty + itemInfo.getInt(2);
	    sumPurchaseRate = sumPurchaseRate + dblPurchaseRate;
	    sumAmount = sumAmount + dblAmount;
	    dblPenalty = dblPurchaseRate * 0.2;
	    sumPenalty = sumPenalty + dblPenalty;
	    double taxAmt = 0;
	    if (cmbSearchType.getSelectedItem().equals("Item wise"))
	    {
		taxAmt = itemInfo.getDouble(13);
	    }
	    else
	    {
		taxAmt = itemInfo.getDouble(10);
	    }

	    totalTaxAmt += taxAmt;

//            if (cmbSearchType.getSelectedItem().equals("Item wise"))
//            {
//                Object[] row =
//                {
//                    itemInfo.getString(1), itemInfo.getInt(2), formt.format(dblPurchaseRate), formt.format(dblAmount), itemInfo.getString(5), itemInfo.getString(8), funGetCalenderDateInPOSDateFormat(itemInfo.getDate(6)), itemInfo.getString(7)
//                };
//                dm.addRow(row);
//            }
//            else
//            {
//                Object[] row =
//                {
//                    itemInfo.getString(1), itemInfo.getInt(2), formt.format(dblPurchaseRate), formt.format(dblAmount), itemInfo.getString(5), funGetCalenderDateInPOSDateFormat(itemInfo.getDate(6)), itemInfo.getString(7)
//                };
//                dm.addRow(row);
//            }
	    if (cmbSearchType.getSelectedItem().equals("Item wise"))
	    {
		Object[] row =
		{
		    itemInfo.getString(1), itemInfo.getInt(2), gDecimalFormat.format(dblPurchaseRate), gDecimalFormat.format(dblAmount), gDecimalFormat.format(taxAmt), itemInfo.getString(5), itemInfo.getString(9), funGetCalenderDateInPOSDateFormat(itemInfo.getDate(6)), itemInfo.getString(7), itemInfo.getString(8)
		};
		dm.addRow(row);
	    }
	    else
	    {
		Object[] row =
		{
		    itemInfo.getString(1), itemInfo.getInt(2), gDecimalFormat.format(dblPurchaseRate), gDecimalFormat.format(dblAmount), gDecimalFormat.format(taxAmt), itemInfo.getString(5), "", funGetCalenderDateInPOSDateFormat(itemInfo.getDate(6)), itemInfo.getString(7), itemInfo.getString(8)
		};
		dm.addRow(row);
	    }
	}
	tblCreditedStock.setModel(dm);
	Object[] total =
	{
	    "Total", sumQty, gDecimalFormat.format(sumPurchaseRate), gDecimalFormat.format(sumAmount), gDecimalFormat.format(totalTaxAmt)
	};
	totalDm.addRow(total);
	tblTotal.setModel(totalDm);
	DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
	rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
	tblCreditedStock.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
	tblCreditedStock.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
	tblCreditedStock.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
	tblCreditedStock.getColumnModel().getColumn(0).setPreferredWidth(250);
	tblCreditedStock.getColumnModel().getColumn(1).setPreferredWidth(40);
	tblCreditedStock.getColumnModel().getColumn(2).setPreferredWidth(70);
	tblCreditedStock.getColumnModel().getColumn(3).setPreferredWidth(70);
	tblCreditedStock.getColumnModel().getColumn(4).setPreferredWidth(70);
	tblCreditedStock.getColumnModel().getColumn(4).setPreferredWidth(70);

	DefaultTableCellRenderer rightRenderer1 = new DefaultTableCellRenderer();
	rightRenderer1.setHorizontalAlignment(JLabel.RIGHT);
	tblTotal.getColumnModel().getColumn(1).setCellRenderer(rightRenderer1);
	tblTotal.getColumnModel().getColumn(2).setCellRenderer(rightRenderer1);
	tblTotal.getColumnModel().getColumn(3).setCellRenderer(rightRenderer1);

	tblTotal.getColumnModel().getColumn(0).setPreferredWidth(250);
	tblTotal.getColumnModel().getColumn(1).setPreferredWidth(40);
	tblTotal.getColumnModel().getColumn(2).setPreferredWidth(70);
	tblTotal.getColumnModel().getColumn(3).setPreferredWidth(70);
	tblTotal.getColumnModel().getColumn(4).setPreferredWidth(70);

	sbSql = null;

	/*
        
        
         if(strCode.isEmpty() && (!txtSearchType.getText().isEmpty()))
         {
         lblBillDate.setText("");
         lblBillNo.setText("");
            
         dm.addColumn("Item Name");
         dm.addColumn("Qty");
         dm.addColumn("Purchase Rate");
         dm.addColumn("Amount");
           
           
         totalDm=new DefaultTableModel()
         {
         @Override
         public boolean isCellEditable(int row, int column)
         {
         //all cells false
         return false;
         }
         };
         totalDm.getDataVector().removeAllElements();
         totalDm.addColumn("");
         totalDm.addColumn("");
         totalDm.addColumn("");
         totalDm.addColumn("");
         totalDm.addColumn("");
         totalDm.addColumn("");
         tblTotal.updateUI();
         double dblAmount=0.00,dblPurchaseRate=0.00,dblTaxableAmt=0.00;
         double sumPurchaseRate=0.00,sumTaxableAmt=0.00,dblPenalty=0.00,sumPenalty=0.00;
         sumAmount=0.00;
         int sumQty=0;
            
            
         if(cmbTransType.getSelectedItem().equals("Stock Out"))
         {
         dm.addColumn("StockOut No");
         dm.addColumn("StockOut Date");
         dm.addColumn("POS Name");
                
         if(cmbSearchType.getSelectedItem().equals("Item wise"))
         {
         selectQuery= " select c.strItemName,b.dblQuantity,b.dblPurchaseRate,b.dblAmount,a.strStkOutCode,a.dteStkOutDate,d.strPosName "
         + " from tblstkouthd a,tblstkoutdtl b,tblitemmaster c,tblposmaster d "
         + " where a.strStkOutCode=b.strStkOutCode and c.strItemCode=b.strItemCode and a.strPOSCode=d.strPosCode"
         + " and b.strItemCode='"+textSearchTypeCode+"' "
         + " and date( a.dteStkOutDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' ";
         if (!pos.equals("All"))
         {
         selectQuery += "  and a.strPOSCode='"+POSCode+"'  ";
         }
         selectQuery+= " order by c.strItemName asc ";
         }
         else if(cmbSearchType.getSelectedItem().equals("MenuHead wise"))
         {
         selectQuery=" select e.strMenuName ,sum(b.dblQuantity),b.dblPurchaseRate,sum(b.dblAmount),a.strStkOutCode,a.dteStkOutDate,f.strPosName,e.strMenuCode "
         + " from tblstkouthd a,tblstkoutdtl b,tblitemmaster c,tblmenuitempricingdtl d ,tblmenuhd e,tblposmaster f "
         + " where b.strItemCode=d.strItemCode and d.strMenuCode=e.strMenuCode "
         + " and a.strStkOutCode=b.strStkOutCode and c.strItemCode=b.strItemCode and a.strPOSCode=d.strPosCode "
         + " and a.strPOSCode=f.strPosCode and e.strMenuCode='"+textSearchTypeCode+"'"
         + " and date( a.dteStkOutDate ) BETWEEN '"+fromDate+"' AND '"+toDate+"' " ;
         if (!pos.equals("All"))
         {
         selectQuery += "  and a.strPOSCode='"+POSCode+"'  ";
         }
         selectQuery+= " group by e.strMenuName,date(a.dteStkOutDate) order by e.strMenuName asc ";
         }
         else if(cmbSearchType.getSelectedItem().equals("SubGroup wise")) 
         {
         selectQuery= " select d.strSubGroupName ,sum(b.dblQuantity),b.dblPurchaseRate,sum(b.dblAmount),a.strStkOutCode,a.dteStkOutDate,e.strPosName,d.strSubGroupCode "
         + " from tblstkouthd a,tblstkoutdtl b,tblitemmaster c,tblsubgrouphd d,tblposmaster e "
         + " where c.strSubGroupCode=d.strSubGroupCode "
         + " and a.strStkOutCode=b.strStkOutCode and c.strItemCode=b.strItemCode and a.strPOSCode=e.strPosCode"
         + " and c.strSubGroupCode='"+textSearchTypeCode+"'"
         + " and date( a.dteStkOutDate ) BETWEEN '"+fromDate+"' AND '"+toDate+"' ";
         if (!pos.equals("All"))
         {
         selectQuery += "  and a.strPOSCode='"+POSCode+"'  ";
         }
         selectQuery+= " group by d.strSubGroupName,date(a.dteStkOutDate) order by d.strSubGroupName asc ";
         }                
         else if(cmbSearchType.getSelectedItem().equals("Group wise"))
         {
         selectQuery = " select e.strGroupName,sum(b.dblQuantity),b.dblPurchaseRate,sum(b.dblAmount),a.strStkOutCode,a.dteStkOutDate,f.strPosName,e.strGroupCode "
         + " from tblstkouthd a,tblstkoutdtl b,tblitemmaster c,tblsubgrouphd d,tblgrouphd e,tblposmaster f "
         + " where c.strSubGroupCode=d.strSubGroupCode and d.strGroupCode=e.strGroupCode "
         + " and a.strStkOutCode=b.strStkOutCode and c.strItemCode=b.strItemCode and a.strPOSCode=f.strPosCode"
         + " and e.strGroupCode='"+textSearchTypeCode+"' "
         + " and date( a.dteStkOutDate ) BETWEEN '"+fromDate+"' AND '"+toDate+"' ";
         if (!pos.equals("All"))
         {
         selectQuery += "  and a.strPOSCode='"+POSCode+"'  ";
         }
         selectQuery+= " group by e.strGroupName,date(a.dteStkOutDate) order by e.strGroupName asc ";
         }
         }
         else
         {
         dm.addColumn("StockIn No.");
         dm.addColumn("StockIn Date");
         dm.addColumn("POS Name");
                
         if(cmbSearchType.getSelectedItem().equals("Item wise"))
         {
         selectQuery= " select c.strItemName,b.dblQuantity,b.dblPurchaseRate,b.dblAmount,a.strStkInCode,a.dteStkInDate,d.strPosName "
         + " from  tblstkinhd a,tblstkindtl b,tblitemmaster c,tblposmaster d "
         + " where a.strStkInCode=b.strStkInCode and c.strItemCode=b.strItemCode and a.strPOSCode=d.strPosCode "
         + " and b.strItemCode='"+textSearchTypeCode+"' "
         + " and date( a.dteStkInDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' ";
         if (!pos.equals("All"))
         {
         selectQuery += "  and a.strPOSCode='"+POSCode+"'  ";
         }
         selectQuery+= " order by c.strItemName asc ";
         }
         else if(cmbSearchType.getSelectedItem().equals("MenuHead wise"))
         {
         selectQuery=" select e.strMenuName,sum(b.dblQuantity),b.dblPurchaseRate,sum(b.dblAmount),a.strStkInCode,a.dteStkInDate,f.strPosName,e.strMenuCode "
         + " from tblstkinhd a,tblstkindtl b,tblitemmaster c,tblmenuitempricingdtl d ,tblmenuhd e,tblposmaster f "
         + " where b.strItemCode=d.strItemCode and d.strMenuCode=e.strMenuCode "
         + " and a.strStkInCode=b.strStkInCode and c.strItemCode=b.strItemCode and a.strPOSCode=d.strPosCode "
         + " and a.strPOSCode=f.strPosCode and e.strMenuCode='"+textSearchTypeCode+"' "
         + " and date( a.dteStkInDate ) BETWEEN '"+fromDate+"' AND '"+toDate+"' ";
         if (!pos.equals("All"))
         {
         selectQuery += "  and a.strPOSCode='"+POSCode+"'  ";
         }
         selectQuery+= " group by e.strMenuName,date(a.dteStkInDate) order by e.strMenuName asc ";
         }                
         else if(cmbSearchType.getSelectedItem().equals("SubGroup wise")) 
         {
         selectQuery= " select d.strSubGroupName,sum(b.dblQuantity),b.dblPurchaseRate,sum(b.dblAmount),a.strStkInCode,a.dteStkInDate,e.strPosName,d.strSubGroupCode "
         + " from tblstkinhd a,tblstkindtl b,tblitemmaster c,tblsubgrouphd d,tblposmaster e  "
         + " where c.strSubGroupCode=d.strSubGroupCode "
         + " and a.strStkInCode=b.strStkInCode and c.strItemCode=b.strItemCode and a.strPOSCode=e.strPosCode "
         + " and c.strSubGroupCode='"+textSearchTypeCode+"' "
         + " and date( a.dteStkInDate ) BETWEEN '"+fromDate+"' AND '"+toDate+"' ";
         if (!pos.equals("All"))
         {
         selectQuery += "  and a.strPOSCode='"+POSCode+"'  ";
         }
         selectQuery+= " group by d.strSubGroupName,date(a.dteStkInDate) order by d.strSubGroupName asc ";
         }
         else if(cmbSearchType.getSelectedItem().equals("Group wise"))
         {
         selectQuery = " select e.strGroupName,sum(b.dblQuantity),b.dblPurchaseRate,sum(b.dblAmount),a.strStkInCode,a.dteStkInDate,f.strPosName,e.strGroupCode "
         + " from tblstkinhd a,tblstkindtl b,tblitemmaster c,tblsubgrouphd d,tblgrouphd e,tblposmaster f "
         + " where c.strSubGroupCode=d.strSubGroupCode and d.strGroupCode=e.strGroupCode "
         + " and a.strStkInCode=b.strStkInCode and c.strItemCode=b.strItemCode and a.strPOSCode=f.strPosCode  "
         + " and e.strGroupCode='"+textSearchTypeCode+"' "
         + " and date( a.dteStkInDate ) BETWEEN '"+fromDate+"' AND '"+toDate+"' ";
         if (!pos.equals("All"))
         {
         selectQuery += "  and a.strPOSCode='"+POSCode+"'  ";
         }
         selectQuery+= " group by e.strGroupName,date(a.dteStkInDate) order by e.strGroupName asc ";
         }
         }
         
         System.out.println(cmbTransType.getSelectedItem()+"="+selectQuery);
         ResultSet itemInfo=clsGlobalVarClass.dbMysql.executeResultSet(selectQuery);
         while(itemInfo.next())
         {               
         dblPurchaseRate=itemInfo.getDouble(3);
         dblAmount=itemInfo.getDouble(4);
         sumQty=sumQty+itemInfo.getInt(2);
         sumPurchaseRate=sumPurchaseRate+dblPurchaseRate;
         sumAmount=sumAmount+dblAmount;
         dblPenalty=dblPurchaseRate*0.2;
         sumPenalty=sumPenalty+dblPenalty;
                
         Object[] row={itemInfo.getString(1),itemInfo.getInt(2)
         ,formt.format(dblPurchaseRate),formt.format(dblAmount),itemInfo.getString(5),funGetCalenderDateInPOSDateFormat(itemInfo.getDate(6)),itemInfo.getString(7)};
         dm.addRow(row);
         }
         tblCreditedStock.setModel(dm);
         Object[] total={"Total",sumQty,formt.format(sumPurchaseRate),formt.format(sumAmount)};
         totalDm.addRow(total);
         tblTotal.setModel(totalDm);
         DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
         rightRenderer.setHorizontalAlignment( JLabel.RIGHT );
         tblCreditedStock.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
         tblCreditedStock.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
         tblCreditedStock.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
         tblCreditedStock.getColumnModel().getColumn(0).setPreferredWidth(250);
         tblCreditedStock.getColumnModel().getColumn(1).setPreferredWidth(40);
         tblCreditedStock.getColumnModel().getColumn(2).setPreferredWidth(70);
         tblCreditedStock.getColumnModel().getColumn(3).setPreferredWidth(70);
         tblCreditedStock.getColumnModel().getColumn(4).setPreferredWidth(70);
         tblCreditedStock.getColumnModel().getColumn(4).setPreferredWidth(70);

         DefaultTableCellRenderer rightRenderer1 = new DefaultTableCellRenderer();
         rightRenderer1.setHorizontalAlignment( JLabel.RIGHT );
         tblTotal.getColumnModel().getColumn(1).setCellRenderer(rightRenderer1);
         tblTotal.getColumnModel().getColumn(2).setCellRenderer(rightRenderer1);
         tblTotal.getColumnModel().getColumn(3).setCellRenderer(rightRenderer1);

         tblTotal.getColumnModel().getColumn(0).setPreferredWidth(250);
         tblTotal.getColumnModel().getColumn(1).setPreferredWidth(40);
         tblTotal.getColumnModel().getColumn(2).setPreferredWidth(70);
         tblTotal.getColumnModel().getColumn(3).setPreferredWidth(70);
         tblTotal.getColumnModel().getColumn(4).setPreferredWidth(70);
            
         }
         else if((!strCode.isEmpty()) && (!txtSearchType.getText().isEmpty()))
         {
         dm.addColumn("Item Name");
         dm.addColumn("Qty");
         dm.addColumn("Purchase Rate");
         dm.addColumn("Amount");
         dm.addColumn("POS Name");
         totalDm=new DefaultTableModel()
         {
         @Override
         public boolean isCellEditable(int row, int column)
         {
         //all cells false
         return false;
         }
         };
         totalDm.getDataVector().removeAllElements();
         totalDm.addColumn("");
         totalDm.addColumn("");
         totalDm.addColumn("");
         totalDm.addColumn("");
         totalDm.addColumn("");
         tblTotal.updateUI();
         double dblAmount=0.00,dblPurchaseRate=0.00,dblTaxableAmt=0.00;
         double sumPurchaseRate=0.00,sumTaxableAmt=0.00,dblPenalty=0.00,sumPenalty=0.00;
         sumAmount=0.00;
         int sumQty=0;

         if(cmbTransType.getSelectedItem().equals("Stock Out"))
         {
         if(cmbSearchType.getSelectedItem().equals("Item wise"))
         {
                     
         selectQuery=" select c.strItemName,b.dblQuantity,b.dblPurchaseRate,b.dblAmount,a.dteStkOutDate,a.strPurchaseBillNo,d.strPosName "
         + " from tblstkouthd a ,"+tableName+" b,tblitemmaster c,tblposmaster d "
         + " where c.strItemCode=b.strItemCode and a.strStkOutCode=b.strStkOutCode and a.strPOSCode=d.strPosCode "
         + " and b.strItemCode='"+textSearchTypeCode+"' and a."+fieldName+"='"+strCode+"' "
         + " and date( a.dteStkOutDate ) BETWEEN '"+fromDate+"' AND '"+toDate+"' ";
         if (!pos.equals("All"))
         {
         selectQuery += "  and a.strPOSCode='"+POSCode+"'  ";
         }
                             
         selectQuery+= " order by c.strItemName asc ";
                    
         }
                
         else if(cmbSearchType.getSelectedItem().equals("MenuHead wise"))
         {
         selectQuery="  select e.strMenuName,sum(b.dblQuantity),b.dblPurchaseRate,sum(b.dblAmount),a.dteStkOutDate,a.strPurchaseBillNo,f.strPosName,e.strMenuCode "
         + " from tblstkouthd a ,"+tableName+" b,tblitemmaster c,tblmenuitempricingdtl d ,tblmenuhd e,tblposmaster f "
         + " where b.strItemCode=d.strItemCode and d.strMenuCode=e.strMenuCode "
         + " and a.strStkOutCode=b.strStkOutCode and c.strItemCode=b.strItemCode and a.strPOSCode=d.strPosCode "
         + " and a.strPOSCode=f.strPosCode and a."+fieldName+"='"+strCode+"' and e.strMenuCode='"+textSearchTypeCode+"'"
         + " and date( a.dteStkOutDate ) BETWEEN '"+fromDate+"' AND '"+toDate+"' ";
         if (!pos.equals("All"))
         {
         selectQuery += "  and a.strPOSCode='"+POSCode+"'  ";
         }
                             
         selectQuery+= " group by e.strMenuName,date(a.dteStkOutDate) order by e.strMenuName asc ";
                    
         }
                
         else if(cmbSearchType.getSelectedItem().equals("SubGroup wise")) 
         {
         selectQuery= " select d.strSubGroupName,sum(b.dblQuantity),b.dblPurchaseRate,sum(b.dblAmount),a.dteStkOutDate,a.strPurchaseBillNo,e.strPosName,d.strSubGroupCode "
         + " from tblstkouthd a ,"+tableName+" b,tblitemmaster c,tblsubgrouphd d,tblposmaster e "
         + " where c.strSubGroupCode=d.strSubGroupCode "
         + " and a.strStkOutCode=b.strStkOutCode and c.strItemCode=b.strItemCode "
         + " and c.strItemCode=b.strItemCode and c.strItemCode=b.strItemCode and a.strPOSCode=e.strPosCode "
         + " and c.strSubGroupCode='"+textSearchTypeCode+"' and a."+fieldName+"='"+strCode+"' "
         + " and date( a.dteStkOutDate ) BETWEEN '"+fromDate+"' AND '"+toDate+"' ";
         if (!pos.equals("All"))
         {
         selectQuery += "  and a.strPOSCode='"+POSCode+"'  ";
         }
                             
         selectQuery+= " group by d.strSubGroupName,date(a.dteStkOutDate) order by d.strSubGroupName asc ";
                     
         }
                
         else if(cmbSearchType.getSelectedItem().equals("Group wise"))
         {
         selectQuery = " select e.strGroupName,b.dblQuantity,b.dblPurchaseRate,b.dblAmount,a.dteStkOutDate,a.strPurchaseBillNo,f.strPosName,e.strGroupCode "
         + " from tblstkouthd a ,"+tableName+" b,tblitemmaster c,tblsubgrouphd d,tblgrouphd e,tblposmaster f "
         + " where c.strSubGroupCode=d.strSubGroupCode and d.strGroupCode=e.strGroupCode "
         + " and a.strStkOutCode=b.strStkOutCode and c.strItemCode=b.strItemCode and a.strPOSCode=f.strPosCode "
         + " and c.strItemCode=b.strItemCode and e.strGroupCode='"+textSearchTypeCode+"' "
         + " and a."+fieldName+"='"+strCode+"' "
         + " and date( a.dteStkOutDate ) BETWEEN '"+fromDate+"' AND '"+toDate+"' ";
         if (!pos.equals("All"))
         {
         selectQuery += "  and a.strPOSCode='"+POSCode+"'  ";
         }
                             
         selectQuery+= " group by e.strGroupName,date( a.dteStkOutDate ) order by e.strGroupName asc ";
                     
         } 
          
         }
         else
         {
         if(cmbSearchType.getSelectedItem().equals("Item wise"))
         {
                     
         selectQuery= " select c.strItemName,b.dblQuantity,b.dblPurchaseRate,b.dblAmount,a.dteStkInDate,a.strPurchaseBillNo,d.strPosName "
         + " from tblstkinhd a ,"+tableName+" b,tblitemmaster c,tblposmaster d "
         + " where c.strItemCode=b.strItemCode and a.strStkInCode=b.strStkInCode and a.strPOSCode=d.strPosCode "
         + " and b.strItemCode='"+textSearchTypeCode+"' and a."+fieldName+"='"+strCode+"' "
         + " and date( a.dteStkInDate ) BETWEEN '"+fromDate+"' AND '"+toDate+"' ";
         if (!pos.equals("All"))
         {
         selectQuery += "  and a.strPOSCode='"+POSCode+"'  ";
         }
                             
         selectQuery+= " order by c.strItemName asc ";
         }
                
         else if(cmbSearchType.getSelectedItem().equals("MenuHead wise"))
         {
         selectQuery="  select e.strMenuName,sum(b.dblQuantity),b.dblPurchaseRate,sum(b.dblAmount),a.dteStkInDate,a.strPurchaseBillNo,f.strPosName,e.strMenuCode "
         + " from tblstkinhd a ,"+tableName+" b,tblitemmaster c,tblmenuitempricingdtl d ,tblmenuhd e,tblposmaster f "
         + " where b.strItemCode=d.strItemCode and d.strMenuCode=e.strMenuCode and a.strPOSCode=d.strPosCode "
         + " and a.strStkInCode=b.strStkInCode and c.strItemCode=b.strItemCode and a.strPOSCode=f.strPosCode "
         + " and c.strItemCode=b.strItemCode and e.strMenuCode='"+textSearchTypeCode+"'"
         + " and a."+fieldName+"='"+strCode+"' "
         + " and date( a.dteStkInDate ) BETWEEN '"+fromDate+"' AND '"+toDate+"' ";
         if (!pos.equals("All"))
         {
         selectQuery += "  and a.strPOSCode='"+POSCode+"'  ";
         }
                             
         selectQuery+= " group by e.strMenuName,date( a.dteStkInDate ) order by e.strMenuName asc ";
                    
         }
                
         else if(cmbSearchType.getSelectedItem().equals("SubGroup wise")) 
         {
         selectQuery= " select d.strSubGroupName,sum(b.dblQuantity),b.dblPurchaseRate,sum(b.dblAmount),a.dteStkInDate,a.strPurchaseBillNo,e.strPosName,d.strSubGroupCode "
         + " from tblstkinhd a ,"+tableName+" b,tblitemmaster c,tblsubgrouphd d,tblposmaster e "
         + " where c.strSubGroupCode=d.strSubGroupCode "
         + " and a.strStkInCode=b.strStkInCode and c.strItemCode=b.strItemCode and a.strPOSCode=e.strPosCode  "
         + " and c.strItemCode=b.strItemCode and c.strSubGroupCode='"+textSearchTypeCode+"' "
         + " and date( a.dteStkInDate ) BETWEEN '"+fromDate+"' AND '"+toDate+"' ";
         if (!pos.equals("All"))
         {
         selectQuery += "  and a.strPOSCode='"+POSCode+"'  ";
         }
                             
         selectQuery+= " group by d.strSubGroupName,date( a.dteStkInDate ) order by d.strSubGroupName asc ";
         }
                
         else if(cmbSearchType.getSelectedItem().equals("Group wise"))
         {
         selectQuery = " select e.strGroupName,sum(b.dblQuantity),b.dblPurchaseRate,sum(b.dblAmount),a.dteStkInDate,a.strPurchaseBillNo,f.strPosName,e.strGroupCode"
         + " from tblstkinhd a ,"+tableName+" b,tblitemmaster c,tblsubgrouphd d,tblgrouphd e,tblposmaster f  "
         + " where c.strSubGroupCode=d.strSubGroupCode and d.strGroupCode=e.strGroupCode "
         + " and a.strStkInCode=b.strStkInCode and c.strItemCode=b.strItemCode and a.strPOSCode=f.strPosCode "
         + " and c.strItemCode=b.strItemCode and e.strGroupCode='"+textSearchTypeCode+"' "
         + " and a."+fieldName+"='"+strCode+"'  "
         + " and date( a.dteStkInDate ) BETWEEN '"+fromDate+"' AND '"+toDate+"' ";
         if (!pos.equals("All"))
         {
         selectQuery += "  and a.strPOSCode='"+POSCode+"'  ";
         }
                             
         selectQuery+= " group by e.strGroupName,date(a.dteStkInDate) order by e.strGroupName asc ";
         }
         }
            
         System.out.println(cmbTransType.getSelectedItem()+"="+selectQuery);
         ResultSet itemInfo=clsGlobalVarClass.dbMysql.executeResultSet(selectQuery);
         while(itemInfo.next())
         {
               
         dblPurchaseRate=itemInfo.getDouble(3);
         dblAmount=itemInfo.getDouble(4);
         sumQty=sumQty+itemInfo.getInt(2);
         sumPurchaseRate=sumPurchaseRate+dblPurchaseRate;
         sumAmount=sumAmount+dblAmount;
         dblPenalty=dblPurchaseRate*0.2;
         sumPenalty=sumPenalty+dblPenalty;

               
         Object[] row={itemInfo.getString(1),itemInfo.getInt(2)
         ,formt.format(dblPurchaseRate),formt.format(dblAmount),itemInfo.getString(7)};
         dm.addRow(row);
              
         lblBillDate.setText(funGetCalenderDateInPOSDateFormat(itemInfo.getDate(5)));
         lblBillNo.setText(itemInfo.getString(6));
         }
         tblCreditedStock.setModel(dm);
         Object[] total={"Total",sumQty,formt.format(sumPurchaseRate),formt.format(sumAmount)};
         totalDm.addRow(total);
         tblTotal.setModel(totalDm);
         DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
         rightRenderer.setHorizontalAlignment( JLabel.RIGHT );
         tblCreditedStock.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
         tblCreditedStock.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
         tblCreditedStock.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
         tblCreditedStock.getColumnModel().getColumn(0).setPreferredWidth(250);
         tblCreditedStock.getColumnModel().getColumn(1).setPreferredWidth(40);
         tblCreditedStock.getColumnModel().getColumn(2).setPreferredWidth(70);
         tblCreditedStock.getColumnModel().getColumn(3).setPreferredWidth(70);
         tblCreditedStock.getColumnModel().getColumn(4).setPreferredWidth(70);

         DefaultTableCellRenderer rightRenderer1 = new DefaultTableCellRenderer();
         rightRenderer1.setHorizontalAlignment( JLabel.RIGHT );
         tblTotal.getColumnModel().getColumn(1).setCellRenderer(rightRenderer1);
         tblTotal.getColumnModel().getColumn(2).setCellRenderer(rightRenderer1);
         tblTotal.getColumnModel().getColumn(3).setCellRenderer(rightRenderer1);

         tblTotal.getColumnModel().getColumn(0).setPreferredWidth(250);
         tblTotal.getColumnModel().getColumn(1).setPreferredWidth(40);
         tblTotal.getColumnModel().getColumn(2).setPreferredWidth(70);
         tblTotal.getColumnModel().getColumn(3).setPreferredWidth(70);
         tblTotal.getColumnModel().getColumn(4).setPreferredWidth(70);
         }
        
         else if((strCode.isEmpty()) && (txtSearchType.getText().isEmpty()))
         { 
         lblBillDate.setText("");
         lblBillNo.setText("");
            
         dm.addColumn("Item Name");
         dm.addColumn("Qty");
         dm.addColumn("Purchase Rate");
         dm.addColumn("Amount");
           
         // dm.addColumn("Tax");
         totalDm=new DefaultTableModel()
         {
         @Override
         public boolean isCellEditable(int row, int column)
         {
         //all cells false
         return false;
         }
         };
         totalDm.getDataVector().removeAllElements();
         totalDm.addColumn("");
         totalDm.addColumn("");
         totalDm.addColumn("");
         totalDm.addColumn("");
         totalDm.addColumn("");
         totalDm.addColumn("");
         tblTotal.updateUI();
         double dblAmount=0.00,dblPurchaseRate=0.00,dblTaxableAmt=0.00;
         double sumPurchaseRate=0.00,sumTaxableAmt=0.00,dblPenalty=0.00,sumPenalty=0.00;
         sumAmount=0.00;
         int sumQty=0;

         if(cmbTransType.getSelectedItem().equals("Stock Out"))
         {
         dm.addColumn("StockOut No.");
         dm.addColumn("StockOut Date");
         dm.addColumn("POS Name");
         if(cmbSearchType.getSelectedItem().equals("Item wise"))
         {
         selectQuery=" select c.strItemName,b.dblQuantity,b.dblPurchaseRate,b.dblAmount,a.strStkOutCode,a.dteStkOutDate,a.strPurchaseBillNo,d.strPosName "
         + " from tblstkouthd a ,"+tableName+" b,tblitemmaster c,tblposmaster d  "
         + " where c.strItemCode=b.strItemCode and a.strStkOutCode=b.strStkOutCode and a.strPOSCode=d.strPosCode "
         + " and date( a.dteStkOutDate ) BETWEEN '"+fromDate+"' AND '"+toDate+"' ";
         if (!pos.equals("All"))
         {
         selectQuery += "  and a.strPOSCode='"+POSCode+"'  ";
         }
                             
         selectQuery+= " order by c.strItemName asc ";
         }     
                
         else if(cmbSearchType.getSelectedItem().equals("MenuHead wise"))
         {
         selectQuery="  select e.strMenuName,sum(b.dblQuantity),b.dblPurchaseRate,sum(b.dblAmount),a.strStkOutCode,a.dteStkOutDate,a.strPurchaseBillNo,f.strPosName,e.strMenuCode "
         + " from tblstkouthd a ,"+tableName+" b,tblitemmaster c,tblmenuitempricingdtl d ,tblmenuhd e,tblposmaster f  "
         + " where b.strItemCode=d.strItemCode and d.strMenuCode=e.strMenuCode "
         + " and a.strStkOutCode=b.strStkOutCode and  c.strItemCode=b.strItemCode and a.strPOSCode=f.strPosCode  "
         + " and date( a.dteStkOutDate ) BETWEEN '"+fromDate+"' AND '"+toDate+"' ";
         if (!pos.equals("All"))
         {
         selectQuery += "  and a.strPOSCode='"+POSCode+"'  ";
         }
                             
         selectQuery+= " group by e.strMenuName,date( a.dteStkOutDate ) order by e.strMenuName asc ";
                    
         }
                
         else if(cmbSearchType.getSelectedItem().equals("SubGroup wise")) 
         {
         selectQuery= " select d.strSubGroupName,sum(b.dblQuantity),b.dblPurchaseRate,sum(b.dblAmount),a.strStkOutCode,a.dteStkOutDate,a.strPurchaseBillNo,e.strPosName,d.strSubGroupCode "
         + " from tblstkouthd a ,"+tableName+" b,tblitemmaster c,tblsubgrouphd d,tblposmaster e "
         + " where c.strSubGroupCode=d.strSubGroupCode "
         + " and a.strStkOutCode=b.strStkOutCode and c.strItemCode=b.strItemCode and a.strPOSCode=e.strPosCode "
         + " and date( a.dteStkOutDate ) BETWEEN '"+fromDate+"' AND '"+toDate+"' ";
         if (!pos.equals("All"))
         {
         selectQuery += "  and a.strPOSCode='"+POSCode+"'  ";
         }
                             
         selectQuery+= " group by d.strSubGroupName,date( a.dteStkOutDate ) order by d.strSubGroupName asc ";
                     
         }
                
         else if(cmbSearchType.getSelectedItem().equals("Group wise"))
         {
         selectQuery = " select e.strGroupName,sum(b.dblQuantity),b.dblPurchaseRate,sum(b.dblAmount),a.strStkOutCode,a.dteStkOutDate,a.strPurchaseBillNo,f.strPosName,e.strGroupCode "
         + " from tblstkouthd a ,"+tableName+" b,tblitemmaster c,tblsubgrouphd d,tblgrouphd e,tblposmaster f "
         + " where c.strSubGroupCode=d.strSubGroupCode and d.strGroupCode=e.strGroupCode "
         + " and a.strStkOutCode=b.strStkOutCode and c.strItemCode=b.strItemCode and a.strPOSCode=f.strPosCode "
         + " and date( a.dteStkOutDate ) BETWEEN '"+fromDate+"' AND '"+toDate+"' ";
         if (!pos.equals("All"))
         {
         selectQuery += "  and a.strPOSCode='"+POSCode+"'  ";
         }
                             
         selectQuery+= " group by e.strGroupName,date( a.dteStkOutDate ) order by e.strGroupName asc ";
                     
         } 
          
         }
         else
         {
         dm.addColumn("StockIn No.");
         dm.addColumn("StockIn Date");
         dm.addColumn("POS Name");
         if(cmbSearchType.getSelectedItem().equals("Item wise"))
         {
                     
         selectQuery= " select c.strItemName,b.dblQuantity,b.dblPurchaseRate,b.dblAmount,a.strStkInCode,a.dteStkInDate,a.strPurchaseBillNo,d.strPosName"
         + " from tblstkinhd a ,"+tableName+" b,tblitemmaster c,tblposmaster d "
         + " where c.strItemCode=b.strItemCode and a.strStkInCode=b.strStkInCode and a.strPOSCode=d.strPosCode  "
         + " and date( a.dteStkInDate ) BETWEEN '"+fromDate+"' AND '"+toDate+"' ";
         if (!pos.equals("All"))
         {
         selectQuery += "  and a.strPOSCode='"+POSCode+"'  ";
         }
                             
         selectQuery+= " order by c.strItemName asc ";
                    
         }
                
         else if(cmbSearchType.getSelectedItem().equals("MenuHead wise"))
         {
         selectQuery="  select e.strMenuName,sum(b.dblQuantity),b.dblPurchaseRate,sum(b.dblAmount),a.strStkInCode,a.dteStkInDate,a.strPurchaseBillNo,f.strPosName,e.strMenuCode "
         + " from tblstkinhd a ,"+tableName+" b,tblitemmaster c,tblmenuitempricingdtl d ,tblmenuhd e,tblposmaster f "
         + " where b.strItemCode=d.strItemCode and d.strMenuCode=e.strMenuCode and a.strPOSCode=d.strPosCode "
         + " and a.strStkInCode=b.strStkInCode and c.strItemCode=b.strItemCode and a.strPOSCode=f.strPosCode "
         + " and date( a.dteStkInDate ) BETWEEN '"+fromDate+"' AND '"+toDate+"' ";
         if (!pos.equals("All"))
         {
         selectQuery += "  and a.strPOSCode='"+POSCode+"'  ";
         }
                             
         selectQuery+= " group by e.strMenuName,date( a.dteStkInDate ) order by e.strMenuName asc ";
         }
                
         else if(cmbSearchType.getSelectedItem().equals("SubGroup wise")) 
         {
         selectQuery= " select d.strSubGroupName,sum(b.dblQuantity),b.dblPurchaseRate,sum(b.dblAmount),a.strStkInCode,a.dteStkInDate,a.strPurchaseBillNo,e.strPosName,d.strSubGroupCode"
         + " from tblstkinhd a ,"+tableName+" b,tblitemmaster c,tblsubgrouphd d,tblposmaster e  "
         + " where c.strSubGroupCode=d.strSubGroupCode "
         + " and a.strStkInCode=b.strStkInCode and c.strItemCode=b.strItemCode and a.strPOSCode=e.strPosCode "
         + " and date( a.dteStkInDate ) BETWEEN '"+fromDate+"' AND '"+toDate+"' ";
         if (!pos.equals("All"))
         {
         selectQuery += "  and a.strPOSCode='"+POSCode+"'  ";
         }
                             
         selectQuery+= " group by d.strSubGroupName,date( a.dteStkInDate ) order by d.strSubGroupName asc ";
                     
         }
                
         else if(cmbSearchType.getSelectedItem().equals("Group wise"))
         {
         selectQuery = " select e.strGroupName,sum(b.dblQuantity),b.dblPurchaseRate,sum(b.dblAmount),a.strStkInCode,a.dteStkInDate,a.strPurchaseBillNo,f.strPosName,e.strGroupCode"
         + " from tblstkinhd a ,"+tableName+" b,tblitemmaster c,tblsubgrouphd d,tblgrouphd e,tblposmaster f "
         + " where c.strSubGroupCode=d.strSubGroupCode and d.strGroupCode=e.strGroupCode "
         + " and a.strStkInCode=b.strStkInCode and c.strItemCode=b.strItemCode and a.strPOSCode=f.strPosCode  "
         + " and date( a.dteStkInDate ) BETWEEN '"+fromDate+"' AND '"+toDate+"' ";
         if (!pos.equals("All"))
         {
         selectQuery += "  and a.strPOSCode='"+POSCode+"'  ";
         }
                             
         selectQuery+= " group by e.strGroupName,date( a.dteStkInDate ) order by e.strGroupName asc ";
                     
         } 
                 
         }
            
         System.out.println(cmbTransType.getSelectedItem()+"="+selectQuery);
         ResultSet itemInfo=clsGlobalVarClass.dbMysql.executeResultSet(selectQuery);
         while(itemInfo.next())
         {
               
         dblPurchaseRate=itemInfo.getDouble(3);
         dblAmount=itemInfo.getDouble(4);
         sumQty=sumQty+itemInfo.getInt(2);
         sumPurchaseRate=sumPurchaseRate+dblPurchaseRate;
         sumAmount=sumAmount+dblAmount;
         dblPenalty=dblPurchaseRate*0.2;
         sumPenalty=sumPenalty+dblPenalty;

               
         Object[] row={itemInfo.getString(1),itemInfo.getInt(2)
         ,formt.format(dblPurchaseRate),formt.format(dblAmount),itemInfo.getString(5),funGetCalenderDateInPOSDateFormat(itemInfo.getDate(6)),itemInfo.getString(8)};
         dm.addRow(row);
              
               
         }
         tblCreditedStock.setModel(dm);
         Object[] total={"Total",sumQty,formt.format(sumPurchaseRate),formt.format(sumAmount)};
         totalDm.addRow(total);
         tblTotal.setModel(totalDm);
         DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
         rightRenderer.setHorizontalAlignment( JLabel.RIGHT );
         tblCreditedStock.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
         tblCreditedStock.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
         tblCreditedStock.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
         tblCreditedStock.getColumnModel().getColumn(0).setPreferredWidth(250);
         tblCreditedStock.getColumnModel().getColumn(1).setPreferredWidth(40);
         tblCreditedStock.getColumnModel().getColumn(2).setPreferredWidth(70);
         tblCreditedStock.getColumnModel().getColumn(3).setPreferredWidth(70);
         tblCreditedStock.getColumnModel().getColumn(4).setPreferredWidth(70);
         tblCreditedStock.getColumnModel().getColumn(5).setPreferredWidth(70);

         DefaultTableCellRenderer rightRenderer1 = new DefaultTableCellRenderer();
         rightRenderer1.setHorizontalAlignment( JLabel.RIGHT );
         tblTotal.getColumnModel().getColumn(1).setCellRenderer(rightRenderer1);
         tblTotal.getColumnModel().getColumn(2).setCellRenderer(rightRenderer1);
         tblTotal.getColumnModel().getColumn(3).setCellRenderer(rightRenderer1);

         tblTotal.getColumnModel().getColumn(0).setPreferredWidth(250);
         tblTotal.getColumnModel().getColumn(1).setPreferredWidth(40);
         tblTotal.getColumnModel().getColumn(2).setPreferredWidth(70);
         tblTotal.getColumnModel().getColumn(3).setPreferredWidth(70);
         tblTotal.getColumnModel().getColumn(4).setPreferredWidth(70);
         tblTotal.getColumnModel().getColumn(5).setPreferredWidth(70);           
         }
         else if((!strCode.isEmpty()) && (txtSearchType.getText().isEmpty()))
         {
         dm.addColumn("Item Name");
         dm.addColumn("Qty");
         dm.addColumn("Purchase Rate");
         dm.addColumn("Amount");
         dm.addColumn("POS Name");
         // dm.addColumn("Tax");
         totalDm=new DefaultTableModel()
         {
         @Override
         public boolean isCellEditable(int row, int column)
         {
         //all cells false
         return false;
         }
         };
         totalDm.getDataVector().removeAllElements();
         totalDm.addColumn("");
         totalDm.addColumn("");
         totalDm.addColumn("");
         totalDm.addColumn("");
         totalDm.addColumn("");
         tblTotal.updateUI();
         double dblAmount=0.00,dblPurchaseRate=0.00,dblTaxableAmt=0.00;
         double sumPurchaseRate=0.00,sumTaxableAmt=0.00,dblPenalty=0.00,sumPenalty=0.00;
         sumAmount=0.00;
         int sumQty=0;

         if(cmbTransType.getSelectedItem().equals("Stock Out"))
         {
         if(cmbSearchType.getSelectedItem().equals("Item wise"))
         {
                     
         selectQuery=" select c.strItemName,b.dblQuantity,b.dblPurchaseRate,b.dblAmount,a.dteStkOutDate,a.strPurchaseBillNo,d.strPosName "
         + " from tblstkouthd a ,"+tableName+" b,tblitemmaster c,tblposmaster d "
         + " where c.strItemCode=b.strItemCode and a.strStkOutCode=b.strStkOutCode and a.strPOSCode=d.strPosCode "
         + " and a."+fieldName+"='"+strCode+"' "
         + " and date( a.dteStkOutDate ) BETWEEN '"+fromDate+"' AND '"+toDate+"' ";
         if (!pos.equals("All"))
         {
         selectQuery += "  and a.strPOSCode='"+POSCode+"'  ";
         }
                             
         selectQuery+= " order by c.strItemName asc ";
                    
         }
                
         else if(cmbSearchType.getSelectedItem().equals("MenuHead wise"))
         {
         selectQuery="  select e.strMenuName,sum(b.dblQuantity),b.dblPurchaseRate,sum(b.dblAmount),a.dteStkOutDate,a.strPurchaseBillNo,f.strPosName ,e.strMenuCode "
         + " from tblstkouthd a ,"+tableName+" b,tblitemmaster c,tblmenuitempricingdtl d ,tblmenuhd e,tblposmaster f "
         + " where b.strItemCode=d.strItemCode and d.strMenuCode=e.strMenuCode and a.strPOSCode=d.strPosCode "
         + " and a.strStkOutCode=b.strStkOutCode and c.strItemCode=b.strItemCode  and a.strPOSCode=f.strPosCode "
         + " and c.strItemCode=b.strItemCode "
         + " and a."+fieldName+"='"+strCode+"' "
         + " and date( a.dteStkOutDate ) BETWEEN '"+fromDate+"' AND '"+toDate+"' ";
         if (!pos.equals("All"))
         {
         selectQuery += "  and a.strPOSCode='"+POSCode+"'  ";
         }
                             
         selectQuery+= " group by e.strMenuName,date( a.dteStkOutDate ) order by e.strMenuName asc ";
         }
                
         else if(cmbSearchType.getSelectedItem().equals("SubGroup wise")) 
         {
         selectQuery= " select d.strSubGroupName,sum(b.dblQuantity),b.dblPurchaseRate,sum(b.dblAmount),a.dteStkOutDate,a.strPurchaseBillNo,e.strPosName,d.strSubGroupCode "
         + " from tblstkouthd a ,"+tableName+" b,tblitemmaster c,tblsubgrouphd d,tblposmaster e  "
         + " where c.strSubGroupCode=d.strSubGroupCode "
         + " and a.strStkOutCode=b.strStkOutCode and c.strItemCode=b.strItemCode and a.strPOSCode=e.strPosCode " 
         + " and c.strItemCode=b.strItemCode  "
         + " and a."+fieldName+"='"+strCode+"' "
         + " and date( a.dteStkOutDate ) BETWEEN '"+fromDate+"' AND '"+toDate+"' ";
         if (!pos.equals("All"))
         {
         selectQuery += "  and a.strPOSCode='"+POSCode+"'  ";
         }
                             
         selectQuery+= " group by d.strSubGroupName,date( a.dteStkOutDate ) order by d.strSubGroupName asc ";
         }
                
         else if(cmbSearchType.getSelectedItem().equals("Group wise"))
         {
         selectQuery = " select e.strGroupName,sum(b.dblQuantity),b.dblPurchaseRate,sum(b.dblAmount),a.dteStkOutDate,a.strPurchaseBillNo,f.strPosName,e.strGroupCode "
         + " from tblstkouthd a ,"+tableName+" b,tblitemmaster c,tblsubgrouphd d,tblgrouphd e,tblposmaster f "
         + " where c.strSubGroupCode=d.strSubGroupCode and d.strGroupCode=e.strGroupCode "
         + " and a.strStkOutCode=b.strStkOutCode and c.strItemCode=b.strItemCode and a.strPOSCode=f.strPosCode "
         + " and c.strItemCode=b.strItemCode "
         + " and a."+fieldName+"='"+strCode+"' "
         + " and date( a.dteStkOutDate ) BETWEEN '"+fromDate+"' AND '"+toDate+"' ";
         if (!pos.equals("All"))
         {
         selectQuery += "  and a.strPOSCode='"+POSCode+"'  ";
         }
                             
         selectQuery+= " group by e.strGroupName,date(a.dteStkOutDate) order by c.strItemName asc ";
                     
         } 
          
         }
         else
         {
         if(cmbSearchType.getSelectedItem().equals("Item wise"))
         {
                     
         selectQuery= " select c.strItemName,b.dblQuantity,b.dblPurchaseRate,b.dblAmount,a.dteStkInDate,a.strPurchaseBillNo,d.strPosName "
         + " from tblstkinhd a ,"+tableName+" b,tblitemmaster c,tblposmaster d "
         + " where c.strItemCode=b.strItemCode and a.strStkInCode=b.strStkInCode and a.strPOSCode=d.strPosCode  "
         + " and a."+fieldName+"='"+strCode+"' "
         + " and date( a.dteStkInDate ) BETWEEN '"+fromDate+"' AND '"+toDate+"' ";
         if (!pos.equals("All"))
         {
         selectQuery += "  and a.strPOSCode='"+POSCode+"'  ";
         }
                             
         selectQuery+= " order by c.strItemName asc ";
                    
         }
                
         else if(cmbSearchType.getSelectedItem().equals("MenuHead wise"))
         {
         selectQuery="  select e.strMenuName,sum(b.dblQuantity),b.dblPurchaseRate,sum(b.dblAmount),a.dteStkInDate,a.strPurchaseBillNo,f.strPosName,e.strMenuCode "
         + " from tblstkinhd a ,"+tableName+" b,tblitemmaster c,tblmenuitempricingdtl d ,tblmenuhd e,tblposmaster f "
         + " where b.strItemCode=d.strItemCode and d.strMenuCode=e.strMenuCode and a.strPOSCode=d.strPosCode "
         + " and a.strStkInCode=b.strStkInCode and c.strItemCode=b.strItemCode and a.strPOSCode=f.strPosCode  "
         + " and c.strItemCode=b.strItemCode  "
         + " and a."+fieldName+"='"+strCode+"' "
         + " and date( a.dteStkInDate ) BETWEEN '"+fromDate+"' AND '"+toDate+"' ";
         if (!pos.equals("All"))
         {
         selectQuery += "  and a.strPOSCode='"+POSCode+"'  ";
         }
                             
         selectQuery+= " group by e.strMenuName,date(a.dteStkInDate) order by e.strMenuName asc ";
                    
         }
                
         else if(cmbSearchType.getSelectedItem().equals("SubGroup wise")) 
         {
         selectQuery= " select d.strSubGroupName,sum(b.dblQuantity),b.dblPurchaseRate,sum(b.dblAmount),a.dteStkInDate,a.strPurchaseBillNo,e.strPosName,d.strSubGroupCode"
         + " from tblstkinhd a ,"+tableName+" b,tblitemmaster c,tblsubgrouphd d,tblposmaster e "
         + " where c.strSubGroupCode=d.strSubGroupCode "
         + " and a.strStkInCode=b.strStkInCode and c.strItemCode=b.strItemCode and a.strPOSCode=e.strPosCode "
         + " and c.strItemCode=b.strItemCode  "
         + " and a."+fieldName+"='"+strCode+"' "
         + " and date( a.dteStkInDate ) BETWEEN '"+fromDate+"' AND '"+toDate+"' ";
         if (!pos.equals("All"))
         {
         selectQuery += "  and a.strPOSCode='"+POSCode+"'  ";
         }
                             
         selectQuery+= " group by d.strSubGroupName,date(a.dteStkInDate) order by d.strSubGroupName asc ";
                     
         }
                
         else if(cmbSearchType.getSelectedItem().equals("Group wise"))
         {
         selectQuery = " select e.strGroupName,sum(b.dblQuantity),b.dblPurchaseRate,sum(b.dblAmount),a.dteStkInDate,a.strPurchaseBillNo,f.strPosName,e.strGroupCode"
         + " from tblstkinhd a ,"+tableName+" b,tblitemmaster c,tblsubgrouphd d,tblgrouphd e,tblposmaster f "
         + " where c.strSubGroupCode=d.strSubGroupCode and d.strGroupCode=e.strGroupCode "
         + " and a.strStkInCode=b.strStkInCode and c.strItemCode=b.strItemCode and a.strPOSCode=f.strPosCode "
         + " and c.strItemCode=b.strItemCode  "
         + " and a."+fieldName+"='"+strCode+"' "
         + " and date( a.dteStkInDate ) BETWEEN '"+fromDate+"' AND '"+toDate+"' ";
         if (!pos.equals("All"))
         {
         selectQuery += "  and a.strPOSCode='"+POSCode+"'  ";
         }
                             
         selectQuery+= " group by e.strGroupName,date(a.dteStkInDate) order by e.strGroupName asc ";
                     
         } 
                 
         }
            
         System.out.println(cmbTransType.getSelectedItem()+"="+selectQuery);
         ResultSet itemInfo=clsGlobalVarClass.dbMysql.executeResultSet(selectQuery);
         while(itemInfo.next())
         {
               
         dblPurchaseRate=itemInfo.getDouble(3);
         dblAmount=itemInfo.getDouble(4);
         sumQty=sumQty+itemInfo.getInt(2);
         sumPurchaseRate=sumPurchaseRate+dblPurchaseRate;
         sumAmount=sumAmount+dblAmount;
         dblPenalty=dblPurchaseRate*0.2;
         sumPenalty=sumPenalty+dblPenalty;

               
         Object[] row={itemInfo.getString(1),itemInfo.getInt(2)
         ,formt.format(dblPurchaseRate),formt.format(dblAmount),itemInfo.getString(7)};
         dm.addRow(row);
              
         lblBillDate.setText(funGetCalenderDateInPOSDateFormat(itemInfo.getDate(5)));
         lblBillNo.setText(itemInfo.getString(6));
         }
         tblCreditedStock.setModel(dm);
         Object[] total={"Total",sumQty,formt.format(sumPurchaseRate),formt.format(sumAmount)};
         totalDm.addRow(total);
         tblTotal.setModel(totalDm);
         DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
         rightRenderer.setHorizontalAlignment( JLabel.RIGHT );
         tblCreditedStock.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
         tblCreditedStock.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
         tblCreditedStock.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
         tblCreditedStock.getColumnModel().getColumn(0).setPreferredWidth(250);
         tblCreditedStock.getColumnModel().getColumn(1).setPreferredWidth(40);
         tblCreditedStock.getColumnModel().getColumn(2).setPreferredWidth(70);
         tblCreditedStock.getColumnModel().getColumn(3).setPreferredWidth(70);
         tblCreditedStock.getColumnModel().getColumn(4).setPreferredWidth(70);
         //tblCreditedStock.getColumnModel().getColumn(3).setPreferredWidth(70);

         DefaultTableCellRenderer rightRenderer1 = new DefaultTableCellRenderer();
         rightRenderer1.setHorizontalAlignment( JLabel.RIGHT );
         tblTotal.getColumnModel().getColumn(1).setCellRenderer(rightRenderer1);
         tblTotal.getColumnModel().getColumn(2).setCellRenderer(rightRenderer1);
         tblTotal.getColumnModel().getColumn(3).setCellRenderer(rightRenderer1);

         tblTotal.getColumnModel().getColumn(0).setPreferredWidth(250);
         tblTotal.getColumnModel().getColumn(1).setPreferredWidth(40);
         tblTotal.getColumnModel().getColumn(2).setPreferredWidth(70);
         tblTotal.getColumnModel().getColumn(3).setPreferredWidth(70);
         tblTotal.getColumnModel().getColumn(4).setPreferredWidth(70);
           
         }*/
    }

    /**
     * this Function is used for fill tax table
     *
     */
    /* private void funFillTaxTable() throws Exception
     {
     dm=new DefaultTableModel()
     {
     @Override
     public boolean isCellEditable(int row, int column)
     {
     //all cells false
     return false;
     }
     };
     dm.addColumn("Tax Name");
     dm.addColumn("Taxable Amt");
     dm.addColumn("Tax Amt");
     totalDm=new DefaultTableModel()
     {
     @Override
     public boolean isCellEditable(int row, int column)
     {
     //all cells false
     return false;
     }
     };
     totalDm.getDataVector().removeAllElements();
     totalDm.addColumn("");
     totalDm.addColumn("");
     totalDm.addColumn("");
     tblTotal.updateUI();
     String strTaxCode=null,strTaxName=null;
     double dblTaxableAmt=0.00,dblTaxAmt=0.00,dblTotalTaxableAmt=0.00,dblTotalTaxAmt=0.00,dblTaxPercent=0.00;
     double dblTaxable1=0.00,dblTaxable2=0.00;

     sql="select b.strTaxDesc,a.strTaxCode,sum(a.dblTaxableAmt),sum(a.dblTaxAmt),b.dblPercent,b.strTaxIndicator "
     + "from tblstocktaxdtl a,tbltaxhd b where a.strTransactionId='"+txtTransTypeCode.getText()+"' "
     + "and a.strTaxCode=b.strTaxCode group by a.strTaxCode";
     ResultSet taxSet=clsGlobalVarClass.dbMysql.executeResultSet(sql);

     while(taxSet.next())
     {
     strTaxName=taxSet.getString(1);
     strTaxCode=taxSet.getString(2);
     dblTaxableAmt=Double.parseDouble(taxSet.getString(3));
     dblTaxAmt=Double.parseDouble(taxSet.getString(4));
     dblTaxPercent=Double.parseDouble(taxSet.getString(5));
     dblTotalTaxableAmt=dblTotalTaxableAmt+dblTaxableAmt;

     sql="select strTaxOnTax,strTaxOnTaxCode from tbltaxhd where strTaxCode='"+strTaxCode+"'";
     //System.out.println(sql);
     ResultSet taxSet1=clsGlobalVarClass.dbMysql.executeResultSet(sql);
     taxSet1.next();
     if(taxSet1.getString(1).equals("Yes"))
     {
     String totCode=taxSet1.getString(2);
     sql="select dblPercent from tbltaxhd where strTaxCode='"+totCode+"'";
     //System.out.println("TOT="+sql);
     ResultSet taxSet2=clsGlobalVarClass.dbMysql.executeResultSet(sql);
     taxSet2.next();
     double totPer=Double.parseDouble(taxSet2.getString(1));
     dblTaxable1=dblTaxableAmt*(totPer/100);
     dblTaxable2=(dblTaxable1+dblTaxableAmt)*(dblTaxPercent/100);
     dblTotalTaxAmt=dblTotalTaxAmt+dblTaxable2;
     }

     if(taxSet.getString(6).length()>0)
     {
     Object[] row={strTaxName,formt.format(dblTaxableAmt),formt.format(dblTaxable2)};
     dm.addRow(row);
     }
     else
     {
     dblTotalTaxAmt=dblTotalTaxAmt+dblTaxAmt;
     Object[] row={strTaxName,formt.format(dblTaxableAmt),formt.format(dblTaxAmt)};
     dm.addRow(row);
     }
     }
     lblTotalTax.setText(formt.format(dblTotalTaxAmt));
     lblGrandTotal.setText(formt.format(dblTotalTaxAmt+sumAmount));
     tblTax.setModel(dm);
     Object[] total={"Total",formt.format(sumAmount),formt.format(dblTotalTaxAmt)};
     totalDm.addRow(total);
     tblTaxTotal.setModel(totalDm);
     DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
     rightRenderer.setHorizontalAlignment( JLabel.RIGHT );
     tblTax.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
     tblTax.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
     tblTax.getColumnModel().getColumn(0).setPreferredWidth(50);
     tblTax.getColumnModel().getColumn(1).setPreferredWidth(50);
     tblTax.getColumnModel().getColumn(2).setPreferredWidth(50);

     DefaultTableCellRenderer rightRenderer1 = new DefaultTableCellRenderer();
     rightRenderer1.setHorizontalAlignment( JLabel.RIGHT );
     tblTaxTotal.getColumnModel().getColumn(1).setCellRenderer(rightRenderer1);
     tblTaxTotal.getColumnModel().getColumn(2).setCellRenderer(rightRenderer1);
     tblTaxTotal.getColumnModel().getColumn(0).setPreferredWidth(50);
     tblTaxTotal.getColumnModel().getColumn(1).setPreferredWidth(50);
     tblTaxTotal.getColumnModel().getColumn(2).setPreferredWidth(50);
     }
    
     */
    /**
     * Export
     */
    private void funExportToExcel()
    {
	try
	{
	    Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
	    List<String> arrListTotal = new ArrayList<String>();
	    List<String> arrHeaderList = new ArrayList<String>();

	    String pos = cmbPOSCode.getSelectedItem().toString().toString();
	    String reason = cmbReason.getSelectedItem().toString().toString();
	    String reasonCode = hmReason.get(cmbReason.getSelectedItem().toString()).toString();
	    String POSCode = hmPOS.get(cmbPOSCode.getSelectedItem().toString()).toString();
	    String fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());

	    String toDate = objUtility.funGetFromToDate(dteToDate.getDate());
	    StringBuilder sbSql = new StringBuilder();

	    sbSql.setLength(0);
	    if (cmbTransType.getSelectedItem().equals("Stock Out"))
	    {

		if (cmbSearchType.getSelectedItem().equals("Item wise"))
		{
		    sbSql.append(" SELECT c.strItemName,sum(b.dblQuantity),b.dblPurchaseRate,sum(b.dblAmount) "
			    + ",a.strStkOutCode,a.dteStkOutDate,d.strPosName,g.strReasonName,a.strPurchaseBillNo "
			    + ",e.strPOCode,e.strSupplierCode,f.strSupplierName,sum(a.dblTaxAmt) "
			    + "FROM tblstkouthd a  "
			    + "join tblstkoutdtl b on a.strStkOutCode=b.strStkOutCode  "
			    + "join tblitemmaster c on c.strItemCode=b.strItemCode  "
			    + "join tblposmaster d on a.strPOSCode=d.strPosCode "
			    + "left join tblpurchaseorderhd e on a.strPurchaseBillNo=e.strPOCode "
			    + "left join tblsuppliermaster f on e.strSupplierCode=f.strSupplierCode "
			    + "left join tblreasonmaster g on a.strReasonCode=g.strReasonCode "
			    + " where date( a.dteStkOutDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' ");
		    if (!pos.equals("All"))
		    {
			sbSql.append("  and a.strPOSCode='" + POSCode + "'  ");
		    }
		    if (!txtTransTypeCode.getText().isEmpty())
		    {
			sbSql.append(" and a.strStkOutCode='" + txtTransTypeCode.getText().trim() + "'");
		    }
		    if (!txtSearchType.getText().isEmpty())
		    {
			sbSql.append(" and b.strItemCode='" + textSearchTypeCode + "'");
		    }
		    if (!reasonCode.equals("All"))
		    {
			sbSql.append("  and a.strReasonCode='" + reasonCode + "'  ");
		    }
		    if (!supplierCode.isEmpty())
		    {
			sbSql.append("  and a.strSupplierCode='" + supplierCode + "'  ");
		    }

		    sbSql.append(" group by b.strItemCode,date( a.dteStkOutDate ),a.strPOSCOde,a.strStkOutCode "
			    + " order by date( a.dteStkOutDate ),c.strItemName asc ");
		}
		else if (cmbSearchType.getSelectedItem().equals("MenuHead wise"))
		{
		    sbSql.append(" select e.strMenuName ,sum(b.dblQuantity),b.dblPurchaseRate,sum(b.dblAmount),a.strStkOutCode"
			    + ",a.dteStkOutDate,f.strPosName,g.strReasonName,e.strMenuCode,sum(a.dblTaxAmt) "
			    + " from tblstkouthd a,tblstkoutdtl b,tblitemmaster c,tblmenuitempricingdtl d ,tblmenuhd e,tblposmaster f "
			    + ",tblreasonmaster g "
			    + " where b.strItemCode=d.strItemCode and d.strMenuCode=e.strMenuCode "
			    + " and a.strStkOutCode=b.strStkOutCode and c.strItemCode=b.strItemCode and a.strPOSCode=d.strPosCode "
			    + " and a.strPOSCode=f.strPosCode "
			    + " and date( a.dteStkOutDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
			    + " and a.strReasonCode=g.strReasonCode ");
		    if (!pos.equals("All"))
		    {
			sbSql.append("  and a.strPOSCode='" + POSCode + "'  ");
		    }
		    if (!txtTransTypeCode.getText().isEmpty())
		    {
			sbSql.append(" and a.strStkOutCode='" + txtTransTypeCode.getText().trim() + "'");
		    }
		    if (!txtSearchType.getText().isEmpty())
		    {
			sbSql.append(" and e.strMenuCode='" + textSearchTypeCode + "'");
		    }
		    if (!reasonCode.equals("All"))
		    {
			sbSql.append("  and a.strReasonCode='" + reasonCode + "'  ");
		    }
		    sbSql.append(" group by e.strMenuName,date(a.dteStkOutDate),a.strPOSCOde,a.strStkOutCode "
			    + " order by date( a.dteStkOutDate ),e.strMenuName asc ");
		}
		else if (cmbSearchType.getSelectedItem().equals("SubGroup wise"))
		{
		    sbSql.append(" select d.strSubGroupName ,sum(b.dblQuantity),b.dblPurchaseRate,sum(b.dblAmount)"
			    + ",a.strStkOutCode,a.dteStkOutDate,e.strPosName,f.strReasonName,d.strSubGroupCode,sum(a.dblTaxAmt) "
			    + " from tblstkouthd a,tblstkoutdtl b,tblitemmaster c,tblsubgrouphd d,tblposmaster e "
			    + ",tblreasonmaster f"
			    + " where c.strSubGroupCode=d.strSubGroupCode "
			    + " and a.strStkOutCode=b.strStkOutCode and c.strItemCode=b.strItemCode and a.strPOSCode=e.strPosCode"
			    + " and date( a.dteStkOutDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
			    + "and a.strReasonCode=f.strReasonCode ");
		    if (!pos.equals("All"))
		    {
			sbSql.append("  and a.strPOSCode='" + POSCode + "'  ");
		    }
		    if (!txtTransTypeCode.getText().isEmpty())
		    {
			sbSql.append(" and a.strStkOutCode='" + txtTransTypeCode.getText().trim() + "'");
		    }
		    if (!txtSearchType.getText().isEmpty())
		    {
			sbSql.append(" and d.strSubGroupCode='" + textSearchTypeCode + "'");
		    }
		    if (!reasonCode.equals("All"))
		    {
			sbSql.append("  and a.strReasonCode='" + reasonCode + "'  ");
		    }
		    sbSql.append(" group by d.strSubGroupName,date(a.dteStkOutDate),a.strPOSCOde,a.strStkOutCode "
			    + " order by date( a.dteStkOutDate ),d.strSubGroupName asc ");
		}
		else if (cmbSearchType.getSelectedItem().equals("Group wise"))
		{
		    sbSql.append(" select e.strGroupName,sum(b.dblQuantity),b.dblPurchaseRate,sum(b.dblAmount),a.strStkOutCode"
			    + ",a.dteStkOutDate,f.strPosName,g.strReasonName,e.strGroupCode,sum(a.dblTaxAmt) "
			    + " from tblstkouthd a,tblstkoutdtl b,tblitemmaster c,tblsubgrouphd d,tblgrouphd e,tblposmaster f "
			    + ",tblreasonmaster g"
			    + " where c.strSubGroupCode=d.strSubGroupCode and d.strGroupCode=e.strGroupCode "
			    + " and a.strStkOutCode=b.strStkOutCode and c.strItemCode=b.strItemCode and a.strPOSCode=f.strPosCode"
			    + " and date( a.dteStkOutDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
			    + " and a.strReasonCode=g.strReasonCode ");
		    if (!pos.equals("All"))
		    {
			sbSql.append("  and a.strPOSCode='" + POSCode + "'  ");
		    }
		    if (!txtTransTypeCode.getText().isEmpty())
		    {
			sbSql.append(" and a.strStkOutCode='" + txtTransTypeCode.getText().trim() + "'");
		    }
		    if (!txtSearchType.getText().isEmpty())
		    {
			sbSql.append(" and e.strGroupCode='" + textSearchTypeCode + "'");
		    }
		    if (!reasonCode.equals("All"))
		    {
			sbSql.append("  and a.strReasonCode='" + reasonCode + "'  ");
		    }
		    sbSql.append(" group by e.strGroupName,date(a.dteStkOutDate),a.strPOSCOde,a.strStkOutCode "
			    + " order by date( a.dteStkOutDate ),e.strGroupName asc ");
		}
	    }
	    else
	    {

		if (cmbSearchType.getSelectedItem().equals("Item wise"))
		{
		    sbSql.append(" SELECT c.strItemName,b.dblQuantity,b.dblPurchaseRate,b.dblAmount,a.strStkInCode,a.dteStkInDate"
			    + ",d.strPosName,g.strReasonName,a.strPurchaseBillNo "
			    + ",e.strPOCode,e.strSupplierCode,f.strSupplierName,sum(a.dblTaxAmt) "
			    + "FROM tblstkinhd a "
			    + "join tblstkindtl b on a.strStkInCode=b.strStkInCode "
			    + "join tblitemmaster c on c.strItemCode=b.strItemCode  "
			    + "join tblposmaster d on a.strPOSCode=d.strPosCode  "
			    + "left join tblpurchaseorderhd e on a.strPurchaseBillNo=e.strPOCode "
			    + "left join tblsuppliermaster f on e.strSupplierCode=f.strSupplierCode "
			    + "left join tblreasonmaster g on a.strReasonCode=g.strReasonCode "
			    + " where date( a.dteStkInDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
			    + "and a.strReasonCode=g.strReasonCode ");
		    if (!pos.equals("All"))
		    {
			sbSql.append("  and a.strPOSCode='" + POSCode + "'  ");
		    }
		    if (!txtTransTypeCode.getText().isEmpty())
		    {
			sbSql.append(" and a.strStkInCode='" + txtTransTypeCode.getText().trim() + "'");
		    }
		    if (!txtSearchType.getText().isEmpty())
		    {
			sbSql.append(" and b.strItemCode='" + textSearchTypeCode + "'");
		    }
		    if (!reasonCode.equals("All"))
		    {
			sbSql.append("  and a.strReasonCode='" + reasonCode + "'  ");
		    }
		    if (!supplierCode.isEmpty())
		    {
			sbSql.append("  and a.strSupplierCode='" + supplierCode + "'  ");
		    }
		    sbSql.append(" group by date(a.dteStkInDate),a.strStkInCode,a.strPOSCode,b.strItemCode "
			    + "ORDER BY DATE(a.dteStkInDate),c.strItemName ASC ");
		}
		else if (cmbSearchType.getSelectedItem().equals("MenuHead wise"))
		{
		    sbSql.append(" select e.strMenuName,sum(b.dblQuantity),b.dblPurchaseRate,sum(b.dblAmount),a.strStkInCode"
			    + ",a.dteStkInDate,f.strPosName,g.strReasonName,e.strMenuCode,sum(a.dblTaxAmt) "
			    + " from tblstkinhd a,tblstkindtl b,tblitemmaster c,tblmenuitempricingdtl d ,tblmenuhd e,tblposmaster f"
			    + ",tblreasonmaster g "
			    + " where b.strItemCode=d.strItemCode and d.strMenuCode=e.strMenuCode "
			    + " and a.strStkInCode=b.strStkInCode and c.strItemCode=b.strItemCode and a.strPOSCode=d.strPosCode "
			    + " and a.strPOSCode=f.strPosCode "
			    + " and date( a.dteStkInDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
			    + "and a.strReasonCode=g.strReasonCode ");
		    if (!pos.equals("All"))
		    {
			sbSql.append(" and a.strPOSCode='" + POSCode + "'  ");
		    }
		    if (!txtTransTypeCode.getText().isEmpty())
		    {
			sbSql.append(" and a.strStkInCode='" + txtTransTypeCode.getText().trim() + "'");
		    }
		    if (!txtSearchType.getText().isEmpty())
		    {
			sbSql.append(" and e.strMenuCode='" + textSearchTypeCode + "'");
		    }
		    if (!reasonCode.equals("All"))
		    {
			sbSql.append("  and a.strReasonCode='" + reasonCode + "'  ");
		    }
		    sbSql.append(" group by e.strMenuName,date(a.dteStkInDate),a.strPOSCOde,a.strStkInCode "
			    + " order by date( a.dteStkInDate ),e.strMenuName asc ");
		}
		else if (cmbSearchType.getSelectedItem().equals("SubGroup wise"))
		{
		    sbSql.append(" select d.strSubGroupName,sum(b.dblQuantity),b.dblPurchaseRate,sum(b.dblAmount),a.strStkInCode"
			    + ",a.dteStkInDate,e.strPosName,f.strReasonName,d.strSubGroupCode,sum(a.dblTaxAmt) "
			    + " from tblstkinhd a,tblstkindtl b,tblitemmaster c,tblsubgrouphd d,tblposmaster e,tblreasonmaster f  "
			    + " where c.strSubGroupCode=d.strSubGroupCode "
			    + " and a.strStkInCode=b.strStkInCode and c.strItemCode=b.strItemCode and a.strPOSCode=e.strPosCode "
			    + " and date( a.dteStkInDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
			    + "and a.strReasonCode=f.strReasonCode ");
		    if (!pos.equals("All"))
		    {
			sbSql.append("  and a.strPOSCode='" + POSCode + "'  ");
		    }
		    if (!txtTransTypeCode.getText().isEmpty())
		    {
			sbSql.append(" and a.strStkInCode='" + txtTransTypeCode.getText().trim() + "'");
		    }
		    if (!txtSearchType.getText().isEmpty())
		    {
			sbSql.append(" and c.strSubGroupCode='" + textSearchTypeCode + "'");
		    }
		    if (!reasonCode.equals("All"))
		    {
			sbSql.append("  and a.strReasonCode='" + reasonCode + "'  ");
		    }
		    sbSql.append(" group by d.strSubGroupName,date(a.dteStkInDate),a.strPOSCOde,a.strStkInCode "
			    + " order by date( a.dteStkInDate ),d.strSubGroupName asc ");
		}
		else if (cmbSearchType.getSelectedItem().equals("Group wise"))
		{
		    sbSql.append(" select e.strGroupName,sum(b.dblQuantity),b.dblPurchaseRate,sum(b.dblAmount),a.strStkInCode"
			    + ",a.dteStkInDate,f.strPosName,g.strReasonName,e.strGroupCode,sum(a.dblTaxAmt) "
			    + " from tblstkinhd a,tblstkindtl b,tblitemmaster c,tblsubgrouphd d,tblgrouphd e,tblposmaster f "
			    + ",tblreasonmaster g "
			    + " where c.strSubGroupCode=d.strSubGroupCode and d.strGroupCode=e.strGroupCode "
			    + " and a.strStkInCode=b.strStkInCode and c.strItemCode=b.strItemCode and a.strPOSCode=f.strPosCode  "
			    + " and date( a.dteStkInDate ) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
			    + "and a.strReasonCode=g.strReasonCode ");
		    if (!pos.equals("All"))
		    {
			sbSql.append("  and a.strPOSCode='" + POSCode + "'  ");
		    }
		    if (!txtTransTypeCode.getText().isEmpty())
		    {
			sbSql.append(" and a.strStkInCode='" + txtTransTypeCode.getText().trim() + "'");
		    }
		    if (!txtSearchType.getText().isEmpty())
		    {
			sbSql.append(" and e.strGroupCode='" + textSearchTypeCode + "'");
		    }
		    if (!reasonCode.equals("All"))
		    {
			sbSql.append("  and a.strReasonCode='" + reasonCode + "'  ");
		    }
		    sbSql.append(" group by e.strGroupName,date(a.dteStkInDate),a.strPOSCOde,a.strStkInCode "
			    + " order by date( a.dteStkInDate ),e.strGroupName asc ");
		}
	    }

	    double totalQty = 0, totalAmt = 0, totalTaxAmt = 0;
	    int i = 1;
	    ResultSet rsStokInOutExport = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
	    while (rsStokInOutExport.next())
	    {
		List<String> arrListItem = new ArrayList<String>();
		arrListItem.add(rsStokInOutExport.getString(1));
		arrListItem.add(rsStokInOutExport.getString(2));
		arrListItem.add(rsStokInOutExport.getString(3));
		arrListItem.add(rsStokInOutExport.getString(4));
		double taxAmt = 0;
		if (cmbSearchType.getSelectedItem().equals("Item wise"))
		{
		    taxAmt = rsStokInOutExport.getDouble(13);
		}
		else
		{
		    taxAmt = rsStokInOutExport.getDouble(10);
		}
		arrListItem.add(String.valueOf(taxAmt));

		arrListItem.add(rsStokInOutExport.getString(5));
		if (cmbSearchType.getSelectedItem().equals("Item wise"))
		{
		    arrListItem.add(rsStokInOutExport.getString(9));
		}

		totalTaxAmt += taxAmt;
		if (cmbSearchType.getSelectedItem().equals("Item wise"))
		{
		    arrListItem.add(rsStokInOutExport.getString(6));
		}
		else
		{
		    arrListItem.add("");
		    arrListItem.add(rsStokInOutExport.getString(6));
		}
		arrListItem.add(rsStokInOutExport.getString(7));
		arrListItem.add(rsStokInOutExport.getString(8));

		totalQty = totalQty + rsStokInOutExport.getDouble(2);
		totalAmt = totalAmt + rsStokInOutExport.getDouble(4);
		mapExcelItemDtl.put(i, arrListItem);
		i++;
	    }
	    rsStokInOutExport.close();

	    arrListTotal.add(String.valueOf(Math.rint(totalQty) + "#" + "2"));
	    arrListTotal.add(String.valueOf(Math.rint(totalAmt)) + "#" + "4");
	    arrListTotal.add(String.valueOf(Math.rint(totalTaxAmt)) + "#" + "5");

	    arrHeaderList.add("Serial No");
	    arrHeaderList.add("Item Name");
	    arrHeaderList.add("Qty");
	    arrHeaderList.add("Purchase Rate");
	    arrHeaderList.add("Amount");
	    arrHeaderList.add("Tax");
	    if (cmbTransType.getSelectedItem().equals("Stock Out"))
	    {
		arrHeaderList.add("Stock Out No");
		arrHeaderList.add("BillNo");
		arrHeaderList.add("Stock Out Date");
	    }
	    else
	    {
		arrHeaderList.add("Stock In No");
		arrHeaderList.add("BillNo");
		arrHeaderList.add("Stock In Date");
	    }
	    arrHeaderList.add("POS Name");
	    arrHeaderList.add("Reason");
	    List<String> arrparameterList = new ArrayList<String>();
	    arrparameterList.add("Stock In Out Flash");
	    arrparameterList.add("POS" + " : " + pos);
	    arrparameterList.add("FromDate" + " : " + fromDate);
	    arrparameterList.add("ToDate" + " : " + toDate);
	    arrparameterList.add(" ");
	    arrparameterList.add(" ");

	    objUtility.funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "StockInOutExcelSheet");

	    /*
             java.util.Date dt1=new java.util.Date();
             int day=dt1.getDate();
             int month=dt1.getMonth()+1;
             int year=dt1.getYear()+1900;
             String dte=day+"-"+month+"-"+year;
             File f=new  File("D:\\result"+dte+".xls");
            
             TableModel model = tblCreditedStock.getModel();
             FileWriter out = new FileWriter(f);
             for(int i=0; i < model.getColumnCount(); i++) 
             {
             out.write(model.getColumnName(i) + "\t");
             }
             out.write("\n");
             for(int i=0; i< model.getRowCount(); i++) 
             {
             for(int j=0; j < model.getColumnCount(); j++)
             {
             out.write(model.getValueAt(i,j).toString()+"\t");
             }
             out.write("\n");
             }
             out.close();*/
	    new frmOkPopUp(this, "Export successfully", "success", 1).setVisible(true);

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funOpenStockInSearchHelp()
    {
	clsUtility obj = new clsUtility();
	obj.funCallForSearchForm("StockIn");
	new frmSearchFormDialog(this, true).setVisible(true);
	if (clsGlobalVarClass.gSearchItemClicked)
	{
	    Object[] data = clsGlobalVarClass.gArrListSearchData.toArray();
	    txtTransTypeCode.setText(data[0].toString());
	    clsGlobalVarClass.gSearchItemClicked = false;
	    DefaultTableModel dmStkGrid = (DefaultTableModel) tblCreditedStock.getModel();
	    dmStkGrid.setRowCount(0);
	    tblCreditedStock.setModel(dmStkGrid);

	    DefaultTableModel dmTotal = (DefaultTableModel) tblTotal.getModel();
	    dmTotal.setRowCount(0);
	    tblTotal.setModel(dmTotal);
	}
    }

    private void funOpenStockOutSearchHelp()
    {
	clsUtility obj = new clsUtility();
	obj.funCallForSearchForm("StockOut");
	new frmSearchFormDialog(this, true).setVisible(true);
	if (clsGlobalVarClass.gSearchItemClicked)
	{
	    Object[] data = clsGlobalVarClass.gArrListSearchData.toArray();
	    txtTransTypeCode.setText(data[0].toString());
	    clsGlobalVarClass.gSearchItemClicked = false;
	}
    }

    private void funShowButtonClicked()
    {
	try
	{
	    /* if(!txtTransTypeCode.getText().equals(""))
             {
             if(cmbTransType.getSelectedItem().equals("Stock Out"))
             {
             funFillStockTable("tblstkoutdtl","strStkOutCode",txtTransTypeCode.getText());
             funFillTaxTable();
             }
             if(cmbTransType.getSelectedItem().equals("Stock In"))
             {
             funFillStockTable("tblstkindtl","strStkInCode",txtTransTypeCode.getText());
             funFillTaxTable();
             }
             }
             else
             {
             new frmOkPopUp(this, "Please Select Code", "Warning", 1).setVisible(true);
             }
	     */

	    if (cmbTransType.getSelectedItem().equals("Stock Out"))
	    {
		funFillStockTable("tblstkoutdtl", "strStkOutCode", txtTransTypeCode.getText());
		//funFillTaxTable();
	    }
	    if (cmbTransType.getSelectedItem().equals("Stock In"))
	    {
		funFillStockTable("tblstkindtl", "strStkInCode", txtTransTypeCode.getText());
		//funFillTaxTable();
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funSetLookAndFeel()
    {
	try
	{
	    // Set System L&F
	    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	    SwingUtilities.updateComponentTreeUI(this);
	}
	catch (UnsupportedLookAndFeelException e)
	{
	    // handle exception
	}
	catch (ClassNotFoundException e)
	{
	    // handle exception
	}
	catch (InstantiationException e)
	{
	    // handle exception
	}
	catch (IllegalAccessException e)
	{
	    // handle exception
	}
    }

    private void funResetLookAndFeel()
    {
	try
	{
	    for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
	    {
		System.out.println("lookandfeel" + info.getName());
		if ("Nimbus".equals(info.getName()))
		{
		    javax.swing.UIManager.setLookAndFeel(info.getClassName());
		    SwingUtilities.updateComponentTreeUI(this);
		    break;
		}
	    }
	}
	catch (ClassNotFoundException ex)
	{

	}
	catch (InstantiationException ex)
	{

	}
	catch (IllegalAccessException ex)
	{

	}
	catch (javax.swing.UnsupportedLookAndFeelException ex)
	{

	}
    }

    //Function to reset fields
    private void funResetFields()
    {
	try

	{

	    DefaultTableModel dm = new DefaultTableModel();
	    dm.addColumn("Item Name");
	    dm.addColumn("Qty");
	    dm.addColumn("Purchase Rate");
	    dm.addColumn("Amount");
	    tblCreditedStock.setModel(dm);
	    // funFillReasonCombo();
	    totalDm = new DefaultTableModel();
	    totalDm.addColumn("");
	    totalDm.addColumn("");
	    totalDm.addColumn("");
	    totalDm.addColumn("");
	    totalDm.addColumn("");
	    tblTotal.setModel(totalDm);

	    DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
	    rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
	    tblCreditedStock.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
	    tblCreditedStock.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
	    tblCreditedStock.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
	    tblCreditedStock.getColumnModel().getColumn(0).setPreferredWidth(250);
	    tblCreditedStock.getColumnModel().getColumn(1).setPreferredWidth(40);
	    tblCreditedStock.getColumnModel().getColumn(2).setPreferredWidth(70);
	    tblCreditedStock.getColumnModel().getColumn(3).setPreferredWidth(70);
	    tblCreditedStock.setShowHorizontalLines(true);

	    DefaultTableCellRenderer rightRenderer1 = new DefaultTableCellRenderer();
	    rightRenderer1.setHorizontalAlignment(JLabel.RIGHT);
	    tblTotal.getColumnModel().getColumn(1).setCellRenderer(rightRenderer1);
	    tblTotal.getColumnModel().getColumn(2).setCellRenderer(rightRenderer1);
	    tblTotal.getColumnModel().getColumn(3).setCellRenderer(rightRenderer1);

	    tblTotal.getColumnModel().getColumn(0).setPreferredWidth(250);
	    tblTotal.getColumnModel().getColumn(1).setPreferredWidth(40);
	    tblTotal.getColumnModel().getColumn(2).setPreferredWidth(70);
	    tblTotal.getColumnModel().getColumn(3).setPreferredWidth(70);
	    tblTotal.setShowHorizontalLines(true);

	    txtTransTypeCode.setText("");
	    // textSearchTypeCode=" ";
	    txtSearchType.setText("");
	    lblBillDate.setText("");
	    lblBillNo.setText("");
	    lblReasonName.setText("");
	    txtSupplierName.setText("");
	    supplierCode = "";
	    supplierName = "";
	    java.util.Date date = new SimpleDateFormat("dd-MM-yyyy").parse(clsGlobalVarClass.gPOSDateToDisplay);
	    dteFromDate.setDate(date);
	    dteToDate.setDate(date);

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    //Function to call search form for selected type
    private void funSearchTypeTextFieldClicked()
    {

	if (cmbSearchType.getSelectedItem().equals("Item wise"))
	{
	    objUtility.funCallForSearchForm("MenuItem");
	}
	else if (cmbSearchType.getSelectedItem().equals("MenuHead wise"))
	{
	    objUtility.funCallForSearchForm("Menu");
	}
	else if (cmbSearchType.getSelectedItem().equals("SubGroup wise"))
	{
	    objUtility.funCallForSearchForm("SubGroup");
	}
	else if (cmbSearchType.getSelectedItem().equals("Group wise"))
	{
	    objUtility.funCallForSearchForm("Group");
	}

	new frmSearchFormDialog(this, true).setVisible(true);
	if (clsGlobalVarClass.gSearchItemClicked)
	{
	    Object[] data = clsGlobalVarClass.gArrListSearchData.toArray();
	    //funSetData(data);
	    textSearchTypeCode = data[0].toString();
	    txtSearchType.setText(data[1].toString());
	    clsGlobalVarClass.gSearchItemClicked = false;
	}
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        pnlheader = new javax.swing.JPanel();
        lblProductName = new javax.swing.JLabel();
        lblModuleName = new javax.swing.JLabel();
        lblfromName = new javax.swing.JLabel();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 32767));
        filler5 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        lblPosName = new javax.swing.JLabel();
        lblUserCode = new javax.swing.JLabel();
        filler6 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        lblDate = new javax.swing.JLabel();
        lblHOSign = new javax.swing.JLabel();
        pnlBackGround = new JPanel()
        {
            public void paintComponent(Graphics g)
            {
                Image img = Toolkit.getDefaultToolkit().getImage(
                    getClass().getResource("/com/POSReport/images/imgBGJPOS.png"));
                g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), this);
            }
        };  ;
        pnlMain = new javax.swing.JPanel();
        pnlStockInOut = new javax.swing.JPanel();
        lblToDate = new javax.swing.JLabel();
        dteToDate = new com.toedter.calendar.JDateChooser();
        pnlStock = new javax.swing.JScrollPane();
        tblCreditedStock = new javax.swing.JTable();
        pnltotal = new javax.swing.JScrollPane();
        tblTotal = new javax.swing.JTable();
        btnExport = new javax.swing.JButton();
        btnBack = new javax.swing.JButton();
        dteFromDate = new com.toedter.calendar.JDateChooser();
        lblFromDate = new javax.swing.JLabel();
        btnShow = new javax.swing.JButton();
        cmbTransType = new javax.swing.JComboBox();
        lblPOSName = new javax.swing.JLabel();
        cmbPOSCode = new javax.swing.JComboBox();
        txtTransTypeCode = new javax.swing.JTextField();
        lblBillDate = new javax.swing.JLabel();
        lblTotalTax = new javax.swing.JLabel();
        lblGrandTotal = new javax.swing.JLabel();
        lblReasonName = new javax.swing.JLabel();
        btnReset = new javax.swing.JButton();
        lblTransType1 = new javax.swing.JLabel();
        cmbSearchType = new javax.swing.JComboBox();
        txtSearchType = new javax.swing.JTextField();
        lblBillNo = new javax.swing.JLabel();
        lblSupplierName = new javax.swing.JLabel();
        cmbReason = new javax.swing.JComboBox();
        lblReason = new javax.swing.JLabel();
        txtSupplierName = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setExtendedState(MAXIMIZED_BOTH);
        setMinimumSize(new java.awt.Dimension(800, 600));
        setUndecorated(true);
        addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosed(java.awt.event.WindowEvent evt)
            {
                formWindowClosed(evt);
            }
            public void windowClosing(java.awt.event.WindowEvent evt)
            {
                formWindowClosing(evt);
            }
        });

        pnlheader.setBackground(new java.awt.Color(69, 164, 238));
        pnlheader.setLayout(new javax.swing.BoxLayout(pnlheader, javax.swing.BoxLayout.LINE_AXIS));

        lblProductName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblProductName.setForeground(new java.awt.Color(255, 255, 255));
        lblProductName.setText("SPOS - ");
        lblProductName.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                lblProductNameMouseClicked(evt);
            }
        });
        pnlheader.add(lblProductName);

        lblModuleName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblModuleName.setForeground(new java.awt.Color(255, 255, 255));
        pnlheader.add(lblModuleName);

        lblfromName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblfromName.setForeground(new java.awt.Color(255, 255, 255));
        lblfromName.setText("-Stock In/Out Flash");
        lblfromName.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                lblfromNameMouseClicked(evt);
            }
        });
        pnlheader.add(lblfromName);
        pnlheader.add(filler4);
        pnlheader.add(filler5);

        lblPosName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblPosName.setForeground(new java.awt.Color(255, 255, 255));
        lblPosName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblPosName.setMaximumSize(new java.awt.Dimension(321, 30));
        lblPosName.setMinimumSize(new java.awt.Dimension(321, 30));
        lblPosName.setPreferredSize(new java.awt.Dimension(321, 30));
        lblPosName.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                lblPosNameMouseClicked(evt);
            }
        });
        pnlheader.add(lblPosName);

        lblUserCode.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblUserCode.setForeground(new java.awt.Color(255, 255, 255));
        lblUserCode.setMaximumSize(new java.awt.Dimension(90, 30));
        lblUserCode.setMinimumSize(new java.awt.Dimension(90, 30));
        lblUserCode.setPreferredSize(new java.awt.Dimension(71, 30));
        lblUserCode.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                lblUserCodeMouseClicked(evt);
            }
        });
        pnlheader.add(lblUserCode);
        pnlheader.add(filler6);

        lblDate.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblDate.setForeground(new java.awt.Color(255, 255, 255));
        lblDate.setMaximumSize(new java.awt.Dimension(192, 30));
        lblDate.setMinimumSize(new java.awt.Dimension(192, 30));
        lblDate.setPreferredSize(new java.awt.Dimension(192, 30));
        lblDate.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                lblDateMouseClicked(evt);
            }
        });
        pnlheader.add(lblDate);

        lblHOSign.setMaximumSize(new java.awt.Dimension(34, 30));
        lblHOSign.setMinimumSize(new java.awt.Dimension(34, 30));
        lblHOSign.setPreferredSize(new java.awt.Dimension(34, 30));
        lblHOSign.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                lblHOSignMouseClicked(evt);
            }
        });
        pnlheader.add(lblHOSign);

        getContentPane().add(pnlheader, java.awt.BorderLayout.PAGE_START);

        pnlBackGround.setLayout(new java.awt.GridBagLayout());

        pnlMain.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, new java.awt.Color(204, 204, 204), new java.awt.Color(204, 204, 204), new java.awt.Color(153, 153, 153), new java.awt.Color(153, 153, 153)));
        pnlMain.setMinimumSize(new java.awt.Dimension(800, 570));
        pnlMain.setOpaque(false);

        pnlStockInOut.setBackground(new java.awt.Color(255, 255, 255));
        pnlStockInOut.setOpaque(false);
        pnlStockInOut.setPreferredSize(new java.awt.Dimension(800, 508));
        pnlStockInOut.setLayout(null);

        lblToDate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblToDate.setText("To Date");
        pnlStockInOut.add(lblToDate);
        lblToDate.setBounds(590, 10, 50, 27);

        dteToDate.setToolTipText("Select To Date ");
        dteToDate.setPreferredSize(new java.awt.Dimension(119, 35));
        pnlStockInOut.add(dteToDate);
        dteToDate.setBounds(640, 10, 150, 30);

        tblCreditedStock.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String []
            {
                "", "", "", "", "", ""
            }
        ));
        tblCreditedStock.setRowHeight(25);
        pnlStock.setViewportView(tblCreditedStock);

        pnlStockInOut.add(pnlStock);
        pnlStock.setBounds(0, 130, 800, 350);

        tblTotal.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        tblTotal.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {
                {null, null, null, null}
            },
            new String []
            {
                "", "", "", ""
            }
        ));
        tblTotal.setRowHeight(25);
        pnltotal.setViewportView(tblTotal);

        pnlStockInOut.add(pnltotal);
        pnltotal.setBounds(0, 480, 800, 40);

        btnExport.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnExport.setForeground(new java.awt.Color(255, 255, 255));
        btnExport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnExport.setText("EXPORT");
        btnExport.setToolTipText("Export File");
        btnExport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExport.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn2.png"))); // NOI18N
        btnExport.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnExportMouseClicked(evt);
            }
        });
        pnlStockInOut.add(btnExport);
        btnExport.setBounds(130, 520, 100, 40);

        btnBack.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnBack.setForeground(new java.awt.Color(255, 255, 255));
        btnBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnBack.setText("CLOSE");
        btnBack.setToolTipText("Close Window");
        btnBack.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnBack.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn2.png"))); // NOI18N
        btnBack.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnBackMouseClicked(evt);
            }
        });
        btnBack.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnBackActionPerformed(evt);
            }
        });
        pnlStockInOut.add(btnBack);
        btnBack.setBounds(260, 520, 100, 40);

        dteFromDate.setToolTipText("Select From Date ");
        dteFromDate.setPreferredSize(new java.awt.Dimension(119, 35));
        pnlStockInOut.add(dteFromDate);
        dteFromDate.setBounds(430, 10, 150, 30);

        lblFromDate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblFromDate.setText("From Date");
        pnlStockInOut.add(lblFromDate);
        lblFromDate.setBounds(360, 10, 70, 29);

        btnShow.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        btnShow.setForeground(new java.awt.Color(255, 255, 255));
        btnShow.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnShow.setText("SHOW");
        btnShow.setToolTipText("View Report");
        btnShow.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnShow.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn2.png"))); // NOI18N
        btnShow.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnShowMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt)
            {
                btnShowMouseEntered(evt);
            }
        });
        pnlStockInOut.add(btnShow);
        btnShow.setBounds(0, 520, 100, 40);

        cmbTransType.setToolTipText("Select In/Out");
        pnlStockInOut.add(cmbTransType);
        cmbTransType.setBounds(10, 50, 100, 30);

        lblPOSName.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblPOSName.setText("POS Name");
        pnlStockInOut.add(lblPOSName);
        lblPOSName.setBounds(10, 10, 80, 30);

        cmbPOSCode.setToolTipText("Select POS");
        pnlStockInOut.add(cmbPOSCode);
        cmbPOSCode.setBounds(90, 10, 240, 30);

        txtTransTypeCode.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                txtTransTypeCodeMouseClicked(evt);
            }
        });
        txtTransTypeCode.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                txtTransTypeCodeActionPerformed(evt);
            }
        });
        pnlStockInOut.add(txtTransTypeCode);
        txtTransTypeCode.setBounds(120, 50, 130, 30);
        pnlStockInOut.add(lblBillDate);
        lblBillDate.setBounds(260, 50, 90, 30);

        lblTotalTax.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        lblTotalTax.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        pnlStockInOut.add(lblTotalTax);
        lblTotalTax.setBounds(690, 444, 100, 40);

        lblGrandTotal.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        lblGrandTotal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        pnlStockInOut.add(lblGrandTotal);
        lblGrandTotal.setBounds(690, 520, 100, 30);
        pnlStockInOut.add(lblReasonName);
        lblReasonName.setBounds(500, 520, 110, 30);

        btnReset.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnReset.setForeground(new java.awt.Color(255, 255, 255));
        btnReset.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnReset.setText("RESET");
        btnReset.setToolTipText("Close Window");
        btnReset.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnReset.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn2.png"))); // NOI18N
        btnReset.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnResetMouseClicked(evt);
            }
        });
        btnReset.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnResetActionPerformed(evt);
            }
        });
        pnlStockInOut.add(btnReset);
        btnReset.setBounds(390, 520, 100, 40);

        lblTransType1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblTransType1.setText("Type");
        pnlStockInOut.add(lblTransType1);
        lblTransType1.setBounds(470, 50, 40, 30);

        cmbSearchType.setToolTipText("Select In/Out");
        pnlStockInOut.add(cmbSearchType);
        cmbSearchType.setBounds(510, 50, 100, 30);

        txtSearchType.setBackground(new java.awt.Color(224, 224, 224));
        txtSearchType.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                txtSearchTypeMouseClicked(evt);
            }
        });
        txtSearchType.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                txtSearchTypeActionPerformed(evt);
            }
        });
        pnlStockInOut.add(txtSearchType);
        txtSearchType.setBounds(620, 50, 170, 30);
        pnlStockInOut.add(lblBillNo);
        lblBillNo.setBounds(360, 50, 90, 30);

        lblSupplierName.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblSupplierName.setText("Supplier  :");
        pnlStockInOut.add(lblSupplierName);
        lblSupplierName.setBounds(440, 90, 70, 30);

        pnlStockInOut.add(cmbReason);
        cmbReason.setBounds(120, 90, 310, 30);

        lblReason.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblReason.setText("Reason      :");
        pnlStockInOut.add(lblReason);
        lblReason.setBounds(10, 90, 80, 30);

        txtSupplierName.setBackground(new java.awt.Color(224, 224, 224));
        txtSupplierName.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                txtSupplierNameMouseClicked(evt);
            }
        });
        txtSupplierName.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                txtSupplierNameActionPerformed(evt);
            }
        });
        pnlStockInOut.add(txtSupplierName);
        txtSupplierName.setBounds(510, 90, 280, 30);

        javax.swing.GroupLayout pnlMainLayout = new javax.swing.GroupLayout(pnlMain);
        pnlMain.setLayout(pnlMainLayout);
        pnlMainLayout.setHorizontalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlStockInOut, javax.swing.GroupLayout.DEFAULT_SIZE, 796, Short.MAX_VALUE)
        );
        pnlMainLayout.setVerticalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMainLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(pnlStockInOut, javax.swing.GroupLayout.PREFERRED_SIZE, 570, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pnlBackGround.add(pnlMain, new java.awt.GridBagConstraints());

        getContentPane().add(pnlBackGround, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnExportMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnExportMouseClicked
	// TODO add your handling code here:
	funExportToExcel();
    }//GEN-LAST:event_btnExportMouseClicked
    /**
     * Close Window
     *
     * @param evt
     */
    private void btnBackMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnBackMouseClicked
	funResetLookAndFeel();
	dispose();
	clsGlobalVarClass.hmActiveForms.remove("Stock In Out Flash");
    }//GEN-LAST:event_btnBackMouseClicked

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("Stock In Out Flash");
    }//GEN-LAST:event_btnBackActionPerformed

    private void btnShowMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnShowMouseClicked
	// TODO add your handling code here:
	funShowButtonClicked();
    }//GEN-LAST:event_btnShowMouseClicked

    private void txtTransTypeCodeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txtTransTypeCodeMouseClicked
	// TODO add your handling code here:
	if (cmbTransType.getSelectedItem().toString().equals("Stock In"))
	{
	    funOpenStockInSearchHelp();
	}
	else if (cmbTransType.getSelectedItem().toString().equals("Stock Out"))
	{
	    funOpenStockOutSearchHelp();
	}
    }//GEN-LAST:event_txtTransTypeCodeMouseClicked

    private void lblProductNameMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblProductNameMouseClicked
    {//GEN-HEADEREND:event_lblProductNameMouseClicked
	// TODO add your handling code here:
    }//GEN-LAST:event_lblProductNameMouseClicked

    private void lblfromNameMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblfromNameMouseClicked
    {//GEN-HEADEREND:event_lblfromNameMouseClicked
	// TODO add your handling code here:
    }//GEN-LAST:event_lblfromNameMouseClicked

    private void lblPosNameMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblPosNameMouseClicked
    {//GEN-HEADEREND:event_lblPosNameMouseClicked
	// TODO add your handling code here:
    }//GEN-LAST:event_lblPosNameMouseClicked

    private void lblUserCodeMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblUserCodeMouseClicked
    {//GEN-HEADEREND:event_lblUserCodeMouseClicked
	// TODO add your handling code here:
    }//GEN-LAST:event_lblUserCodeMouseClicked

    private void lblDateMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblDateMouseClicked
    {//GEN-HEADEREND:event_lblDateMouseClicked
	// TODO add your handling code here:
    }//GEN-LAST:event_lblDateMouseClicked

    private void lblHOSignMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblHOSignMouseClicked
    {//GEN-HEADEREND:event_lblHOSignMouseClicked
	// TODO add your handling code here:
    }//GEN-LAST:event_lblHOSignMouseClicked

    private void btnShowMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnShowMouseEntered
	// TODO add your handling code here:
    }//GEN-LAST:event_btnShowMouseEntered

    private void btnResetMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnResetMouseClicked
	// TODO add your handling code here:
	funResetFields();
    }//GEN-LAST:event_btnResetMouseClicked

    private void btnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetActionPerformed
	// TODO add your handling code here:
    }//GEN-LAST:event_btnResetActionPerformed

    private void txtTransTypeCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTransTypeCodeActionPerformed
	// TODO add your handling code here:
    }//GEN-LAST:event_txtTransTypeCodeActionPerformed

    private void txtSearchTypeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txtSearchTypeMouseClicked
	// TODO add your handling code here:
	funSearchTypeTextFieldClicked();

    }//GEN-LAST:event_txtSearchTypeMouseClicked

    private void txtSearchTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchTypeActionPerformed
	// TODO add your handling code here:
    }//GEN-LAST:event_txtSearchTypeActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("Stock In Out Flash");
    }//GEN-LAST:event_formWindowClosed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("Stock In Out Flash");
    }//GEN-LAST:event_formWindowClosing

    private void txtSupplierNameMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_txtSupplierNameMouseClicked
    {//GEN-HEADEREND:event_txtSupplierNameMouseClicked
	try
	{
	    //Search for cost center to update
	    //btnNew.setText("UPDATE");
	    clsUtility obj = new clsUtility();
	    obj.funCallForSearchForm("SupplierMaster");
	    new frmSearchFormDialog(this, true).setVisible(true);
	    if (clsGlobalVarClass.gSearchItemClicked)
	    {

		Object[] data = clsGlobalVarClass.gArrListSearchData.toArray();

		supplierCode = data[0].toString();
		supplierName = data[1].toString();
		txtSupplierName.setText(supplierName);

		clsGlobalVarClass.gSearchItemClicked = false;
	    }
	}
	catch (Exception e)
	{
	    objUtility.funWriteErrorLog(e);
	    e.printStackTrace();
	}
    }//GEN-LAST:event_txtSupplierNameMouseClicked

    private void txtSupplierNameActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_txtSupplierNameActionPerformed
    {//GEN-HEADEREND:event_txtSupplierNameActionPerformed
	// TODO add your handling code here:
    }//GEN-LAST:event_txtSupplierNameActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnExport;
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnShow;
    private javax.swing.JComboBox cmbPOSCode;
    private javax.swing.JComboBox cmbReason;
    private javax.swing.JComboBox cmbSearchType;
    private javax.swing.JComboBox cmbTransType;
    private com.toedter.calendar.JDateChooser dteFromDate;
    private com.toedter.calendar.JDateChooser dteToDate;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.JLabel lblBillDate;
    private javax.swing.JLabel lblBillNo;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblFromDate;
    private javax.swing.JLabel lblGrandTotal;
    private javax.swing.JLabel lblHOSign;
    private javax.swing.JLabel lblModuleName;
    private javax.swing.JLabel lblPOSName;
    private javax.swing.JLabel lblPosName;
    private javax.swing.JLabel lblProductName;
    private javax.swing.JLabel lblReason;
    private javax.swing.JLabel lblReasonName;
    private javax.swing.JLabel lblSupplierName;
    private javax.swing.JLabel lblToDate;
    private javax.swing.JLabel lblTotalTax;
    private javax.swing.JLabel lblTransType1;
    private javax.swing.JLabel lblUserCode;
    private javax.swing.JLabel lblfromName;
    private javax.swing.JPanel pnlBackGround;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JScrollPane pnlStock;
    private javax.swing.JPanel pnlStockInOut;
    private javax.swing.JPanel pnlheader;
    private javax.swing.JScrollPane pnltotal;
    private javax.swing.JTable tblCreditedStock;
    private javax.swing.JTable tblTotal;
    private javax.swing.JTextField txtSearchType;
    private javax.swing.JTextField txtSupplierName;
    private javax.swing.JTextField txtTransTypeCode;
    // End of variables declaration//GEN-END:variables

}
