package com.POSReport.view;

import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsPosConfigFile;
import com.POSGlobal.controller.clsUtility;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class frmSalesSummaryFlash extends javax.swing.JFrame
{

    private StringBuilder sb = new StringBuilder();
    private String fromDate;
    private String toDate, rDate, reportName;
    DefaultTableModel dm, totalDm;
    private String exportFormName, ExportReportPath;
    private java.util.Vector vSalesReportExcelColLength;
    private clsUtility objUtility;
    private DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();

    public frmSalesSummaryFlash()
    {
	/**
	 * this Function is used for Component initialization
	 */
	objUtility = new clsUtility();
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
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	funSetLookAndFeel();

	reportName = "SalesSummaryFlash";
	exportFormName = "SalesSummaryFlash";
	this.setLocationRelativeTo(null);
	funFillPaymentModeCombo();
	funFillComboBox();
	rDate = clsGlobalVarClass.gPOSDateToDisplay;
	ExportReportPath = clsPosConfigFile.exportReportPath;

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
     * this function is used POS Code ComboBoxs
     */
    private void funFillComboBox()
    {
	try
	{

	    if (clsGlobalVarClass.gShowOnlyLoginPOSReports)
	    {
		cmbPosCode.addItem(clsGlobalVarClass.gPOSName + " " + clsGlobalVarClass.gPOSCode);
	    }
	    else
	    {
		cmbPosCode.addItem("All");
		sb.setLength(0);
		sb.append("select strPosName,strPosCode from tblposmaster");
		ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
		while (rs.next())
		{
		    cmbPosCode.addItem(rs.getString(1) + " " + rs.getString(2));
		}
		rs.close();
	    }

	    dteFromDate.setDate(objUtility.funGetDateToSetCalenderDate());
	    dteToDate.setDate(objUtility.funGetDateToSetCalenderDate());
	    funFillDataGridForDaily();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    /**
     * this Function is used Fill Table
     */
    private void funFillDataGridForDaily()
    {
	try
	{
	    fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());

	    toDate = objUtility.funGetFromToDate(dteToDate.getDate());

	    dm = new DefaultTableModel()
	    {
		@Override
		public boolean isCellEditable(int row, int column)
		{
		    //all cells false
		    return false;
		}
	    };

	    totalDm = new DefaultTableModel()
	    {
		@Override
		public boolean isCellEditable(int row, int column)
		{
		    //all cells false
		    return false;
		}
	    };
	    totalDm.addColumn("Total");

	    String pos = objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString().trim());

	    int cntArrLen = 1;
	    sb.setLength(0);
	    sb.append("select strSettelmentDesc from tblsettelmenthd "
		    + "order by strSettelmentDesc");
	    dm.addColumn("POS");
	    dm.addColumn("POS Name");
	    dm.addColumn("POS Date");

	    ResultSet rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
	    while (rsSales.next())
	    {
		dm.addColumn(rsSales.getString(1));
		totalDm.addColumn("");
		cntArrLen++;
	    }
	    rsSales.close();

	    if (cmbPaymentMode.getSelectedIndex() == 0)
	    {
		sb.setLength(0);
		sb.append("select a.strPOSCode,c.strPosName,date(a.dteBillDate)"
			+ ",sum(a.dblSettlementAmt),sum(a.dblGrandTotal) "
			+ " from vqbillhdsettlementdtl a,tblsettelmenthd b,tblposmaster c "
			+ " where a.strSettlementCode=b.strSettelmentCode "
			+ " and a.strPOSCode=c.strPosCode"
			+ " and date(dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
		if (!cmbPosCode.getSelectedItem().toString().equals("All"))
		{
		    sb.append(" and a.strPOSCode='" + pos + "' ");
		}
		sb.append(" group by a.strPOSCode,date(a.dteBillDate) order by date(a.dteBillDate);");
	    }
	    else
	    {
		String payCode = cmbPaymentMode.getSelectedItem().toString().split("!")[1];
		sb.append("select a.strPOSCode,c.strPosName,date(a.dteBillDate)"
			+ ",sum(a.dblSettlementAmt),sum(a.dblGrandTotal) "
			+ " from vqbillhdsettlementdtl a,tblsettelmenthd b,tblposmaster c "
			+ " where a.strSettlementCode=b.strSettelmentCode "
			+ " and a.strPOSCode=c.strPosCode"
			+ " and date(dteBillDate) between '" + fromDate + "' and '" + toDate + "' and b.strSettelmentCode='" + payCode + "' ");
		if (!cmbPosCode.getSelectedItem().toString().equals("All"))
		{
		    sb.append(" and a.strPOSCode='" + pos + "' ");
		}
		sb.append(" group by a.strPOSCode,date(a.dteBillDate) order by date(a.dteBillDate);");
	    }

	    cntArrLen = cntArrLen + 2;
	    rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
	    while (rsSales.next())
	    {
		Object[] arrObjRecords = new Object[cntArrLen];
		arrObjRecords[0] = rsSales.getString(1);
		arrObjRecords[1] = rsSales.getString(2);
		arrObjRecords[2] = rsSales.getString(3);
		for (int cnt = 3; cnt < cntArrLen; cnt++)
		{
		    arrObjRecords[cnt] = "0.00";
		}
		dm.addRow(arrObjRecords);
	    }
	    rsSales.close();

	    cntArrLen = cntArrLen - 2;
	    Object[] arrObjRecords1 = new Object[cntArrLen];
	    arrObjRecords1[0] = "Total";
	    for (int cnt = 1; cnt < cntArrLen; cnt++)
	    {
		arrObjRecords1[cnt] = "0.00";
	    }
	    totalDm.addRow(arrObjRecords1);
	    DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
	    rightRenderer.setHorizontalAlignment(JLabel.RIGHT);

	    tblTotal.setRowHeight(30);
	    tblTotal.setModel(totalDm);
	    tblTotal.setAutoscrolls(true);
	    tblTotal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    tblTotal.getColumnModel().getColumn(0).setPreferredWidth(370);
	    for (int cnt = 1; cnt < tblTotal.getColumnCount(); cnt++)
	    {
		tblTotal.getColumnModel().getColumn(cnt).setPreferredWidth(100);
		tblTotal.getColumnModel().getColumn(cnt).setCellRenderer(rightRenderer);
	    }

	    tblDailyMonthlyWiseSales.setModel(dm);
	    tblDailyMonthlyWiseSales.setAutoscrolls(true);
	    tblDailyMonthlyWiseSales.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    tblDailyMonthlyWiseSales.getColumnModel().getColumn(0).setPreferredWidth(70);
	    tblDailyMonthlyWiseSales.getColumnModel().getColumn(1).setPreferredWidth(200);
	    tblDailyMonthlyWiseSales.getColumnModel().getColumn(2).setPreferredWidth(100);
	    for (int cnt = 3; cnt < tblDailyMonthlyWiseSales.getColumnCount(); cnt++)
	    {
		tblDailyMonthlyWiseSales.getColumnModel().getColumn(cnt).setPreferredWidth(100);
		tblDailyMonthlyWiseSales.getColumnModel().getColumn(cnt).setCellRenderer(rightRenderer);
	    }

	    for (int cnt = 0; cnt < tblDailyMonthlyWiseSales.getRowCount(); cnt++)
	    {
		String billDate = tblDailyMonthlyWiseSales.getValueAt(cnt, 2).toString();
		String posCode = tblDailyMonthlyWiseSales.getValueAt(cnt, 0).toString();
		sb.setLength(0);
		sb.append("select a.strPOSCode,date(a.dteBillDate)"
			+ ",b.strSettelmentDesc,sum(a.dblSettlementAmt),sum(a.dblSettlementAmt) "
			+ " from vqbillhdsettlementdtl a,tblsettelmenthd b "
			+ " where a.strSettlementCode=b.strSettelmentCode "
			+ " and date(dteBillDate) = '" + billDate + "' and a.strPOSCode='" + posCode + "' "
			+ " group by a.strPOSCode,date(a.dteBillDate),b.strSettelmentDesc "
			+ " order by a.strPOSCode,date(a.dteBillDate),b.strSettelmentDesc;");
		rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());

		while (rsSales.next())
		{
		    for (int cntCol = 3; cntCol < tblDailyMonthlyWiseSales.getColumnCount(); cntCol++)
		    {
			if (rsSales.getString(3).equals(tblDailyMonthlyWiseSales.getColumnName(cntCol)))
			{
			    tblDailyMonthlyWiseSales.setValueAt(gDecimalFormat.format(rsSales.getDouble(5)), cnt, cntCol);
			    //System.out.println("Date="+rsSales.getString(2)+"\tAmt="+rsSales.getDouble(5));
			    break;
			}
		    }
		}
		rsSales.close();
	    }

	    double settleAmt = 0;
	    for (int cntCol = 3; cntCol < tblDailyMonthlyWiseSales.getColumnCount(); cntCol++)
	    {
		settleAmt = 0;
		for (int cntRow = 0; cntRow < tblDailyMonthlyWiseSales.getRowCount(); cntRow++)
		{
		    if (null != tblDailyMonthlyWiseSales.getValueAt(cntRow, cntCol).toString())
		    {
			settleAmt += Double.parseDouble(tblDailyMonthlyWiseSales.getValueAt(cntRow, cntCol).toString());
		    }
		}
		tblTotal.setValueAt(gDecimalFormat.format(settleAmt), 0, cntCol - 2);
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funFillDataGridForMonthly()
    {
	try
	{
	    fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());

	    toDate = objUtility.funGetFromToDate(dteToDate.getDate());

	    dm = new DefaultTableModel()
	    {
		@Override
		public boolean isCellEditable(int row, int column)
		{
		    //all cells false
		    return false;
		}
	    };

	    totalDm = new DefaultTableModel()
	    {
		@Override
		public boolean isCellEditable(int row, int column)
		{
		    //all cells false
		    return false;
		}
	    };

	    String pos = objUtility.funGetPOSCodeFromPOSName(cmbPosCode.getSelectedItem().toString().trim());

	    sb.setLength(0);
	    if (cmbPaymentMode.getSelectedIndex() == 0)
	    {
		sb.append("select strSettelmentDesc from tblsettelmenthd order by strSettelmentDesc");
	    }
	    else
	    {
		String payName = cmbPaymentMode.getSelectedItem().toString().split("!")[0];
		sb.append("select strSettelmentDesc from tblsettelmenthd where strSettelmentDesc='" + payName + "' order by strSettelmentDesc");
	    }
	    dm.addColumn("POS");
	    dm.addColumn("POS Name");
	    dm.addColumn("Month");
	    dm.addColumn("Year");
	    totalDm.addColumn("Total");
	    totalDm.addColumn("");

	    ResultSet rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
	    int cntArrLen = 1;
	    while (rsSales.next())
	    {
		dm.addColumn(rsSales.getString(1));
		totalDm.addColumn("");
		cntArrLen++;
	    }
	    rsSales.close();

	    if (cmbPaymentMode.getSelectedIndex() == 0)
	    {
		sb.setLength(0);
		sb.append("select a.strPOSCode,c.strPOSName,monthname(date(a.dteBillDate)),year(date(a.dteBillDate)) "
			+ " from vqbillhdsettlementdtl a,tblsettelmenthd b,tblposmaster c "
			+ " where a.strSettlementCode=b.strSettelmentCode "
			+ " and a.strPOSCode=c.strPOSCode "
			+ " and date(dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
		if (!cmbPosCode.getSelectedItem().toString().trim().equals("All"))
		{
		    sb.append(" and a.strPOSCode='" + pos + "' ");
		}
		sb.append("  group by a.strPOSCode,month(date(dteBillDate))"
			+ " order by a.strPOSCode,month(date(dteBillDate)) ");

	    }
	    else
	    {
		String payCode = cmbPaymentMode.getSelectedItem().toString().split("!")[1];

		sb.append("select a.strPOSCode,c.strPOSName,monthname(date(a.dteBillDate)),year(date(a.dteBillDate)) "
			+ " from vqbillhdsettlementdtl a,tblsettelmenthd b,tblposmaster c "
			+ " where a.strSettlementCode=b.strSettelmentCode "
			+ " and a.strPOSCode=c.strPOSCode "
			+ " and date(dteBillDate) between '" + fromDate + "' and '" + toDate + "' and b.strSettelmentCode='" + payCode + "' ");
		if (!cmbPosCode.getSelectedItem().toString().trim().equals("All"))
		{
		    sb.append(" and a.strPOSCode='" + pos + "' ");
		}
		sb.append("  group by a.strPOSCode,month(date(dteBillDate))"
			+ " order by a.strPOSCode,month(date(dteBillDate)) ");
	    }

	    cntArrLen = cntArrLen + 2;
	    rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
	    while (rsSales.next())
	    {
		Object[] arrObjRecords = new Object[cntArrLen];
		arrObjRecords[0] = rsSales.getString(1);
		arrObjRecords[1] = rsSales.getString(2);
		arrObjRecords[2] = rsSales.getString(3);
		arrObjRecords[3] = rsSales.getString(4);
		for (int cnt = 4; cnt < cntArrLen; cnt++)
		{
		    arrObjRecords[cnt] = "0.00";
		}
		dm.addRow(arrObjRecords);
	    }
	    rsSales.close();

	    cntArrLen = cntArrLen - 2;
	    Object[] arrObjRecords1 = new Object[cntArrLen];
	    arrObjRecords1[0] = "Total";
	    for (int cnt = 1; cnt < cntArrLen; cnt++)
	    {
		arrObjRecords1[cnt] = "0.00";
	    }
	    totalDm.addRow(arrObjRecords1);

	    DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
	    rightRenderer.setHorizontalAlignment(JLabel.RIGHT);

	    tblTotal.setModel(totalDm);
	    if (cmbPaymentMode.getSelectedIndex() > 0)
	    {
		tblTotal.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
	    }
	    else
	    {
		tblTotal.setAutoscrolls(true);
		tblTotal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    }

	    tblTotal.getColumnModel().getColumn(0).setPreferredWidth(370);
	    for (int cnt = 1; cnt < tblTotal.getColumnCount(); cnt++)
	    {
		tblTotal.getColumnModel().getColumn(cnt).setPreferredWidth(100);
		tblTotal.getColumnModel().getColumn(cnt).setCellRenderer(rightRenderer);
	    }

	    tblDailyMonthlyWiseSales.setModel(dm);
	    if (cmbPaymentMode.getSelectedIndex() > 0)
	    {
		tblDailyMonthlyWiseSales.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
	    }
	    else
	    {
		tblDailyMonthlyWiseSales.setAutoscrolls(true);
		tblDailyMonthlyWiseSales.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    }

	    //tblDailyMonthlyWiseSales.getColumnModel().getColumn(0).setPreferredWidth(70);
	    tblDailyMonthlyWiseSales.getColumnModel().getColumn(1).setPreferredWidth(200);
	    tblDailyMonthlyWiseSales.getColumnModel().getColumn(2).setPreferredWidth(100);
	    tblDailyMonthlyWiseSales.getColumnModel().getColumn(3).setPreferredWidth(100);
	    for (int cnt = 4; cnt < tblDailyMonthlyWiseSales.getColumnCount(); cnt++)
	    {
		tblDailyMonthlyWiseSales.getColumnModel().getColumn(cnt).setPreferredWidth(100);
		tblDailyMonthlyWiseSales.getColumnModel().getColumn(cnt).setCellRenderer(rightRenderer);
	    }

	    for (int cnt = 0; cnt < tblDailyMonthlyWiseSales.getRowCount(); cnt++)
	    {
		String billMonth = tblDailyMonthlyWiseSales.getValueAt(cnt, 2).toString();
		String billYear = tblDailyMonthlyWiseSales.getValueAt(cnt, 3).toString();
		String posCode = tblDailyMonthlyWiseSales.getValueAt(cnt, 0).toString();
		sb.setLength(0);
		sb.append("select a.strPOSCode,date(a.dteBillDate)"
			+ ",b.strSettelmentDesc,sum(a.dblSettlementAmt),sum(a.dblGrandTotal) "
			+ " from vqbillhdsettlementdtl a,tblsettelmenthd b "
			+ " where a.strSettlementCode=b.strSettelmentCode "
			+ " and monthname(date(dteBillDate)) ='" + billMonth + "' and Year(dteBillDate)='" + billYear + "' "
			+ " and a.strPOSCode='" + posCode + "' "
			+ " group by a.strPOSCode,month(date(dteBillDate)),b.strSettelmentDesc  "
			+ " order by a.strPOSCode,month(date(dteBillDate)),b.strSettelmentDesc ;");
		//System.out.println(sqlTransRecords);
		rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());

		while (rsSales.next())
		{
		    for (int cntCol = 4; cntCol < tblDailyMonthlyWiseSales.getColumnCount(); cntCol++)
		    {
			if (rsSales.getString(3).equals(tblDailyMonthlyWiseSales.getColumnName(cntCol)))
			{
			    tblDailyMonthlyWiseSales.setValueAt(gDecimalFormat.format(rsSales.getDouble(5)), cnt, cntCol);
			    //System.out.println("Date="+rsSales.getString(2)+"\tAmt="+rsSales.getDouble(5));
			    break;
			}
		    }
		}
		rsSales.close();
	    }

	    double settleAmt = 0;
	    for (int cntCol = 4; cntCol < tblDailyMonthlyWiseSales.getColumnCount(); cntCol++)
	    {
		settleAmt = 0;
		for (int cntRow = 0; cntRow < tblDailyMonthlyWiseSales.getRowCount(); cntRow++)
		{
		    if (null != tblDailyMonthlyWiseSales.getValueAt(cntRow, cntCol))
		    {
			settleAmt += Double.parseDouble(tblDailyMonthlyWiseSales.getValueAt(cntRow, cntCol).toString());
		    }
		}
		tblTotal.setValueAt(gDecimalFormat.format(settleAmt), 0, cntCol - 2);
	    }
	    tblDailyMonthlyWiseSales.removeColumn(tblDailyMonthlyWiseSales.getColumnModel().getColumn(0));
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    /* private void funExportButtonPressed()
     {
     try
     {
     String exportFormName="";
     if(cmbReportType.getSelectedItem().toString().trim().equals("Daily"))
     {
     exportFormName="";
     }
     else
     {
     exportFormName="";
     }
     File theDir = new File(clsPosConfigFile.exportReportPath);
     File file=new File(clsPosConfigFile.exportReportPath+"/"+exportFormName+rDate+".xls");
     if (!theDir.exists())
     {
     theDir.mkdir();
     funExportFile(tblDailyMonthlyWiseSales,file);
     }
     else
     {
     funExportFile(tblDailyMonthlyWiseSales,file);
     }
     }
     catch (Exception ex)
     {
     ex.printStackTrace();
     }
     }
     */
    private void funExportFile(JTable table, File file)
    {
	try
	{
	    WritableWorkbook workbook1 = Workbook.createWorkbook(file);
	    WritableSheet sheet1 = workbook1.createSheet("First Sheet", 0);
	    TableModel model = table.getModel();
	    sheet1.addCell(new Label(0, 0, reportName));
	    Date dt1 = dteFromDate.getDate();
	    Date dt2 = dteToDate.getDate();
	    SimpleDateFormat ddmmyyyyDateFormat = new SimpleDateFormat("dd-MM-yyyy");
	    String fromDateToDisplay = ddmmyyyyDateFormat.format(dt1);
	    String toDateToDisplay = ddmmyyyyDateFormat.format(dt2);

	    sheet1.addCell(new Label(0, 1, "From Date:"));
	    sheet1.addCell(new Label(1, 1, fromDateToDisplay));
	    sheet1.addCell(new Label(2, 1, "To Date:"));
	    sheet1.addCell(new Label(3, 1, toDateToDisplay));
	    vSalesReportExcelColLength = new java.util.Vector();
	    for (int i = 0; i < model.getColumnCount(); i++)
	    {
		vSalesReportExcelColLength.add("10#Left");
	    }

	    for (int i = 0; i < model.getColumnCount(); i++)
	    {
		Label column = new Label(i, 3, model.getColumnName(i));
		int colLen = Integer.parseInt(vSalesReportExcelColLength.elementAt(i).toString().split("#")[0]);
		sheet1.setColumnView(i, model.getColumnName(i).toString().length() + colLen);
		sheet1.addCell(column);
	    }
	    int i = 0, j = 0;
	    int k = 0;

	    for (i = 3; i < model.getRowCount() + 3; i++)
	    {
		for (j = 0; j < model.getColumnCount(); j++)
		{
		    //System.out.println(model.getValueAt(k, j).toString()+"\tcol="+j);
		    int colLen = Integer.parseInt(vSalesReportExcelColLength.elementAt(j).toString().split("#")[0]);

		    String data = "";
		    if (null != model.getValueAt(k, j))
		    {
			data = model.getValueAt(k, j).toString();
		    }
		    Label row = new Label(j, i + 1, data);
		    sheet1.setColumnView(j, data.length() + colLen);
		    sheet1.addCell(row);
		}
		k++;
	    }
	    funAddLastOfExportReport(workbook1);
	    workbook1.write();
	    workbook1.close();
	    Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + ExportReportPath + "/" + exportFormName + rDate + ".xls");
	    //sendMail();
	}
	catch (FileNotFoundException ex)
	{
	    JOptionPane.showMessageDialog(this, "File Not Found Invalid File Path!!!");
	    ex.printStackTrace();

	}
	catch (Exception ex)
	{
	    ex.printStackTrace();
	}
    }

    private void funAddLastOfExportReport(WritableWorkbook workbook1)
    {
	try
	{
	    int i = 0, j = 0, LastIndexReport = 2;
	    if (exportFormName.equals("SalesSummary"))
	    {
		LastIndexReport = 5;
	    }

	    WritableSheet sheet2 = workbook1.getSheet(0);
	    int r = sheet2.getRows();
	    for (i = r; i < tblTotal.getRowCount() + r; i++)
	    {
		for (j = 0; j < tblTotal.getColumnCount(); j++)
		{
		    Label row = new Label(LastIndexReport + j, i + 1, tblTotal.getValueAt(0, j).toString());
		    sheet2.addCell(row);
		}
	    }
	    WritableSheet sheet3 = workbook1.getSheet(0);
	    r = sheet3.getRows();
	    Formatter fmt = new Formatter();
	    Calendar cal = Calendar.getInstance();
	    fmt.format("%tr", cal);
	    Label row = new Label(1, r + 1, " Created On : " + clsGlobalVarClass.gPOSDateToDisplay + " At : " + fmt + " By : " + clsGlobalVarClass.gUserCode + " ");
	    sheet2.addCell(row);
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funFillPaymentModeCombo()
    {
	try
	{
	    cmbPaymentMode.addItem("All                                              !All");
	    sb.setLength(0);
	    sb.append("select strSettelmentDesc,strSettelmentCode from tblsettelmenthd "
		    + "order by strSettelmentDesc");
	    ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
	    while (rs.next())
	    {
		cmbPaymentMode.addItem(rs.getString(1) + "                                              !" + rs.getString(2));
	    }
	    rs.close();
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlheader = new javax.swing.JPanel();
        lblProductName = new javax.swing.JLabel();
        lblModuleName = new javax.swing.JLabel();
        lblformName = new javax.swing.JLabel();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 32767));
        filler5 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        lblPosName = new javax.swing.JLabel();
        filler6 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        lblUserCode = new javax.swing.JLabel();
        lblDate = new javax.swing.JLabel();
        lblHOSign = new javax.swing.JLabel();
        pnlBackGround = new JPanel() {
            public void paintComponent(Graphics g) {
                Image img = Toolkit.getDefaultToolkit().getImage(
                    getClass().getResource("/com/POSReport/images/imgBGJPOS.png"));
                g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), this);
            }
        };  ;
        pnlMain = new javax.swing.JPanel();
        lblPosCode = new javax.swing.JLabel();
        cmbPosCode = new javax.swing.JComboBox();
        lblFromDate = new javax.swing.JLabel();
        dteFromDate = new com.toedter.calendar.JDateChooser();
        lblToDate = new javax.swing.JLabel();
        dteToDate = new com.toedter.calendar.JDateChooser();
        pnlDayEnd = new javax.swing.JScrollPane();
        tblDailyMonthlyWiseSales = new javax.swing.JTable();
        btnExecute = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();
        pnltotal = new javax.swing.JScrollPane();
        tblTotal = new javax.swing.JTable();
        lblReportType = new javax.swing.JLabel();
        cmbReportType = new javax.swing.JComboBox();
        btnExport = new javax.swing.JButton();
        lblPaymentMode = new javax.swing.JLabel();
        cmbPaymentMode = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setExtendedState(MAXIMIZED_BOTH);
        setMinimumSize(new java.awt.Dimension(800, 600));
        setUndecorated(true);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        pnlheader.setBackground(new java.awt.Color(69, 164, 238));
        pnlheader.setLayout(new javax.swing.BoxLayout(pnlheader, javax.swing.BoxLayout.LINE_AXIS));

        lblProductName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblProductName.setForeground(new java.awt.Color(255, 255, 255));
        lblProductName.setText("SPOS - ");
        lblProductName.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblProductNameMouseClicked(evt);
            }
        });
        pnlheader.add(lblProductName);

        lblModuleName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblModuleName.setForeground(new java.awt.Color(255, 255, 255));
        pnlheader.add(lblModuleName);

        lblformName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblformName.setForeground(new java.awt.Color(255, 255, 255));
        lblformName.setText("-Sales Summary Flash");
        lblformName.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblformNameMouseClicked(evt);
            }
        });
        pnlheader.add(lblformName);
        pnlheader.add(filler4);
        pnlheader.add(filler5);

        lblPosName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblPosName.setForeground(new java.awt.Color(255, 255, 255));
        lblPosName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblPosName.setMaximumSize(new java.awt.Dimension(321, 30));
        lblPosName.setMinimumSize(new java.awt.Dimension(321, 30));
        lblPosName.setPreferredSize(new java.awt.Dimension(321, 30));
        lblPosName.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblPosNameMouseClicked(evt);
            }
        });
        pnlheader.add(lblPosName);
        pnlheader.add(filler6);

        lblUserCode.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblUserCode.setForeground(new java.awt.Color(255, 255, 255));
        lblUserCode.setMaximumSize(new java.awt.Dimension(90, 30));
        lblUserCode.setMinimumSize(new java.awt.Dimension(90, 30));
        lblUserCode.setPreferredSize(new java.awt.Dimension(90, 30));
        lblUserCode.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblUserCodeMouseClicked(evt);
            }
        });
        pnlheader.add(lblUserCode);

        lblDate.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblDate.setForeground(new java.awt.Color(255, 255, 255));
        lblDate.setMaximumSize(new java.awt.Dimension(192, 30));
        lblDate.setMinimumSize(new java.awt.Dimension(192, 30));
        lblDate.setPreferredSize(new java.awt.Dimension(192, 30));
        lblDate.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblDateMouseClicked(evt);
            }
        });
        pnlheader.add(lblDate);

        lblHOSign.setMaximumSize(new java.awt.Dimension(34, 30));
        lblHOSign.setMinimumSize(new java.awt.Dimension(34, 30));
        lblHOSign.setPreferredSize(new java.awt.Dimension(34, 30));
        lblHOSign.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblHOSignMouseClicked(evt);
            }
        });
        pnlheader.add(lblHOSign);

        getContentPane().add(pnlheader, java.awt.BorderLayout.PAGE_START);

        pnlBackGround.setOpaque(false);
        pnlBackGround.setLayout(new java.awt.GridBagLayout());

        pnlMain.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, new java.awt.Color(204, 204, 204), new java.awt.Color(204, 204, 204), new java.awt.Color(153, 153, 153), new java.awt.Color(153, 153, 153)));
        pnlMain.setMinimumSize(new java.awt.Dimension(800, 570));
        pnlMain.setOpaque(false);

        lblPosCode.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblPosCode.setText("POS Name");

        cmbPosCode.setToolTipText("Select POS");

        lblFromDate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblFromDate.setText("From Date");

        dteFromDate.setToolTipText("Select From Date");
        dteFromDate.setPreferredSize(new java.awt.Dimension(119, 35));

        lblToDate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblToDate.setText("To Date");

        dteToDate.setToolTipText("Select To Date");
        dteToDate.setPreferredSize(new java.awt.Dimension(119, 35));

        tblDailyMonthlyWiseSales.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tblDailyMonthlyWiseSales.setRowHeight(25);
        pnlDayEnd.setViewportView(tblDailyMonthlyWiseSales);

        btnExecute.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnExecute.setForeground(new java.awt.Color(255, 255, 255));
        btnExecute.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnExecute.setText("Execute");
        btnExecute.setToolTipText("Execute");
        btnExecute.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExecute.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn2.png"))); // NOI18N
        btnExecute.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnExecuteMouseClicked(evt);
            }
        });

        btnClose.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnClose.setForeground(new java.awt.Color(255, 255, 255));
        btnClose.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnClose.setText("Close");
        btnClose.setToolTipText("Close Window");
        btnClose.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnClose.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn2.png"))); // NOI18N
        btnClose.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnCloseMouseClicked(evt);
            }
        });
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        tblTotal.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        tblTotal.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tblTotal.setRowHeight(30);
        pnltotal.setViewportView(tblTotal);

        lblReportType.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblReportType.setText("Report Type");

        cmbReportType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Daily", "Monthly" }));
        cmbReportType.setToolTipText("Select POS");

        btnExport.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnExport.setForeground(new java.awt.Color(255, 255, 255));
        btnExport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnExport.setText("Export");
        btnExport.setToolTipText("Close Window");
        btnExport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExport.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn2.png"))); // NOI18N
        btnExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportActionPerformed(evt);
            }
        });

        lblPaymentMode.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblPaymentMode.setText("Payment Mode :");

        cmbPaymentMode.setToolTipText("Select Payment Mode");

        javax.swing.GroupLayout pnlMainLayout = new javax.swing.GroupLayout(pnlMain);
        pnlMain.setLayout(pnlMainLayout);
        pnlMainLayout.setHorizontalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMainLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addComponent(lblPosCode)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbPosCode, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblReportType)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbReportType, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblFromDate)
                        .addGap(3, 3, 3)
                        .addComponent(dteFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlMainLayout.createSequentialGroup()
                                .addGap(50, 50, 50)
                                .addComponent(dteToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(lblToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(pnltotal)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlMainLayout.createSequentialGroup()
                        .addComponent(lblPaymentMode)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbPaymentMode, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(344, 344, 344)
                        .addComponent(btnExecute, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnExport, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(pnlDayEnd)))
        );
        pnlMainLayout.setVerticalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblReportType, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cmbReportType, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblPosCode, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cmbPosCode, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(lblFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(dteFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(dteToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblPaymentMode, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cmbPaymentMode, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnExecute, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnExport, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlDayEnd, javax.swing.GroupLayout.PREFERRED_SIZE, 425, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(pnltotal, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pnlBackGround.add(pnlMain, new java.awt.GridBagConstraints());

        getContentPane().add(pnlBackGround, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents
/**
     * Execute Function for view Reports
     *
     * @param evt
     */
    private void btnExecuteMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnExecuteMouseClicked
	// TODO add your handling code here:
	if (cmbReportType.getSelectedItem().toString().trim().equals("Daily"))
	{
	    funFillDataGridForDaily();
	}
	else
	{
	    funFillDataGridForMonthly();
	}
    }//GEN-LAST:event_btnExecuteMouseClicked
    /**
     * Close Windows
     *
     * @param evt
     */
    private void btnCloseMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCloseMouseClicked
	funResetLookAndFeel();
	dispose();
	clsGlobalVarClass.hmActiveForms.remove("Sales Summary Flash");
    }//GEN-LAST:event_btnCloseMouseClicked

    private void btnExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportActionPerformed
	// TODO add your handling code here:

	try
	{
	    File theDir = new File(ExportReportPath);
	    File file = new File(ExportReportPath + "/" + exportFormName + rDate + ".xls");
	    if (!theDir.exists())
	    {
		theDir.mkdir();
		funExportFile(tblDailyMonthlyWiseSales, file);
		//sendMail();
	    }
	    else
	    {
		funExportFile(tblDailyMonthlyWiseSales, file);
		//sendMail();
	    }
	}
	catch (Exception ex)
	{
	    ex.printStackTrace();
	}

	//funExportButtonPressed();
    }//GEN-LAST:event_btnExportActionPerformed

    private void lblProductNameMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblProductNameMouseClicked
    {//GEN-HEADEREND:event_lblProductNameMouseClicked
	// TODO add your handling code here:
    }//GEN-LAST:event_lblProductNameMouseClicked

    private void lblformNameMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblformNameMouseClicked
    {//GEN-HEADEREND:event_lblformNameMouseClicked
	// TODO add your handling code here:
    }//GEN-LAST:event_lblformNameMouseClicked

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

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("Sales Summary Flash");
    }//GEN-LAST:event_btnCloseActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("Sales Summary Flash");
    }//GEN-LAST:event_formWindowClosed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("Sales Summary Flash");
    }//GEN-LAST:event_formWindowClosing


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnExecute;
    private javax.swing.JButton btnExport;
    private javax.swing.JComboBox cmbPaymentMode;
    private javax.swing.JComboBox cmbPosCode;
    private javax.swing.JComboBox cmbReportType;
    private com.toedter.calendar.JDateChooser dteFromDate;
    private com.toedter.calendar.JDateChooser dteToDate;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblFromDate;
    private javax.swing.JLabel lblHOSign;
    private javax.swing.JLabel lblModuleName;
    private javax.swing.JLabel lblPaymentMode;
    private javax.swing.JLabel lblPosCode;
    private javax.swing.JLabel lblPosName;
    private javax.swing.JLabel lblProductName;
    private javax.swing.JLabel lblReportType;
    private javax.swing.JLabel lblToDate;
    private javax.swing.JLabel lblUserCode;
    private javax.swing.JLabel lblformName;
    private javax.swing.JPanel pnlBackGround;
    private javax.swing.JScrollPane pnlDayEnd;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JPanel pnlheader;
    private javax.swing.JScrollPane pnltotal;
    private javax.swing.JTable tblDailyMonthlyWiseSales;
    private javax.swing.JTable tblTotal;
    // End of variables declaration//GEN-END:variables

}
