/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
import java.util.logging.Level;
import java.util.logging.Logger;
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
import jxl.write.WriteException;

public class frmDayEndFlash extends javax.swing.JFrame
{

    private String fromDate;
    private String toDate;
    private DefaultTableModel dm, totalDm;
    private clsUtility objUtility;
    private DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();

    public frmDayEndFlash()
    {
	/**
	 * this Function is used for Component initialization
	 */
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

	this.setLocationRelativeTo(null);
	funFillPOSCmbBox();

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
     * ]
     * this function is used POS Code ComboBoxs
     */
    private void funFillPOSCmbBox()
    {
	try
	{
	    objUtility = new clsUtility();

	    if (clsGlobalVarClass.gShowOnlyLoginPOSReports)
	    {
		cmbPosCode.addItem(clsGlobalVarClass.gPOSName + " " + clsGlobalVarClass.gPOSCode);
	    }
	    else
	    {
		cmbPosCode.addItem("All");
		String sql = "select strPosName,strPosCode from tblposmaster";
		ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(sql);
		while (rs.next())
		{
		    cmbPosCode.addItem(rs.getString(1) + " " + rs.getString(2));
		}
		rs.close();
	    }

	    dteFromDate.setDate(objUtility.funGetDateToSetCalenderDate());
	    dteToDate.setDate(objUtility.funGetDateToSetCalenderDate());
	    funFillDayEndTable();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}

    }

    /**
     * this Function is used Fill Table
     */
    private void funFillDayEndTable()
    {
	StringBuilder sbSql = new StringBuilder();
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
	    dm.addColumn("POS");
	    dm.addColumn("POS Date");
	    dm.addColumn("HD Amt");
	    dm.addColumn("Dining Amt");
	    dm.addColumn("Take Away");
	    dm.addColumn("Total Sale");
	    dm.addColumn("Float");
	    dm.addColumn("Cash");
	    dm.addColumn("Advance");
	    dm.addColumn("Transfer In");
	    dm.addColumn("Total Receipt");
	    dm.addColumn("Pay");
	    dm.addColumn("With Drawal");
	    dm.addColumn("Tranf Out");
	    dm.addColumn("Refund");
	    dm.addColumn("Total Pay");
	    dm.addColumn("Cash In Hand");
	    dm.addColumn("No Of Bill");
	    dm.addColumn("No Of Voided Bill");
	    dm.addColumn("No Of Modify Bill");
	    dm.addColumn("Stock Adj No");
	    dm.addColumn("Excise Bill Gen");
	    dm.addColumn("Net Sale");
	    dm.addColumn("Gross Sale");
	    dm.addColumn("APC");

	    double sumtSale = 0.00, sumFloat = 0.00, sumCash = 0.00, sumAdvance = 0.00, sumTransferIn = 0.00, sumTotalReceipt = 0.00;
	    double sumPay = 0.00, sumWithDrawal = 0.00, sumTransferOut = 0.00, sumTotalPay = 0.00, sumCashInhand = 0.00;
	    double sumHdAmt = 0.00, SumDining = 0.00, sumTaleAway = 0.00, sumNoOfBill = 0.00, SumNoOfVoidedBill = 0.00, SumNoOfModifyBill = 0.00, sumRefund = 0.00;
	    double sumNetSale = 0.00, sumGrossSale = 0.00, sumAPC = 0.00;
	    totalDm = new DefaultTableModel()
	    {
		@Override
		public boolean isCellEditable(int row, int column)
		{
		    //all cells false
		    return false;
		}
	    };
	    for (int i = 0; i < 25; i++)
	    {
		totalDm.addColumn("");
	    }
	    sbSql.append("select b.strPOSName,DATE_FORMAT(date(a.dtePOSDate),'%d-%m-%Y'),dblHDAmt,dblDiningAmt,dblTakeAway,dblTotalSale,dblFloat"
		    + ",dblCash,dblAdvance,dblTransferIn,dblTotalReceipt,dblPayments,dblWithdrawal,dblTransferOut,dblRefund"
		    + ",dblTotalPay,dblCashInHand,dblNoOfBill,dblNoOfVoidedBill,dblNoOfModifyBill,strWSStockAdjustmentNo,strExciseBillGeneration "
		    + " ,a.dblNetSale,a.dblGrossSale,a.dblAPC"
		    + " from tbldayendprocess a,tblposmaster b where a.strPOSCode=b.strPOSCode ");

	    if ("All".equals(cmbPosCode.getSelectedItem()))
	    {
		sbSql.append(" and date(a.dtePOSDate) between '" + fromDate + "' and '" + toDate + "' ");
	    }
	    else
	    {
		String temp = cmbPosCode.getSelectedItem().toString();
		StringBuilder sb = new StringBuilder(temp);
		int len = temp.length();
		int lastInd = sb.lastIndexOf(" ");
		String POSCode = sb.substring(lastInd + 1, len).toString();
		sbSql.append(" and a.strPOSCode='" + POSCode + "' and date(a.dtePOSDate) between '" + fromDate + "' and '" + toDate + "'");
	    }
	    ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
	    DecimalFormat decFormat = new DecimalFormat("0");

	    while (rs.next())
	    {
		Object[] row =
		{
		    rs.getString(1), rs.getString(2), gDecimalFormat.format(rs.getDouble(3)), gDecimalFormat.format(rs.getDouble(4)), gDecimalFormat.format(rs.getDouble(5)), gDecimalFormat.format(rs.getDouble(6)), gDecimalFormat.format(rs.getDouble(7)), gDecimalFormat.format(rs.getDouble(8)), gDecimalFormat.format(rs.getDouble(9)), gDecimalFormat.format(rs.getDouble(10)), gDecimalFormat.format(rs.getDouble(11)), gDecimalFormat.format(rs.getDouble(12)), gDecimalFormat.format(rs.getDouble(13)), gDecimalFormat.format(rs.getDouble(14)), gDecimalFormat.format(rs.getDouble(15)), gDecimalFormat.format(rs.getDouble(16)), gDecimalFormat.format(rs.getDouble(17)), decFormat.format(rs.getDouble(18)), decFormat.format(rs.getDouble(19)), decFormat.format(rs.getDouble(20)), rs.getString(21), rs.getString(22), gDecimalFormat.format(rs.getDouble(23)), gDecimalFormat.format(rs.getDouble(24)), gDecimalFormat.format(rs.getDouble(25))
		};

		sumtSale = sumtSale + rs.getDouble("dblTotalSale");
		sumFloat = sumFloat + rs.getDouble("dblFloat");
		sumCash = sumCash + rs.getDouble("dblCash");
		sumAdvance = sumAdvance + rs.getDouble("dblAdvance");
		sumTransferIn = sumTransferIn + rs.getDouble("dblTransferIn");
		sumTotalReceipt = sumTotalReceipt + rs.getDouble("dblTotalReceipt");
		sumPay = sumPay + rs.getDouble("dblPayments");
		sumWithDrawal += rs.getDouble("dblWithdrawal");
		sumTransferOut += rs.getDouble("dblTransferOut");
		sumTotalPay += rs.getDouble("dblTotalPay");
		sumCashInhand += rs.getDouble("dblCashInHand");
		sumHdAmt += rs.getDouble("dblHDAmt");
		SumDining += rs.getDouble("dblDiningAmt");
		sumTaleAway += rs.getDouble("dblTakeAway");
		sumNoOfBill += rs.getDouble("dblNoOfBill");
		SumNoOfVoidedBill += rs.getDouble("dblNoOfVoidedBill");
		SumNoOfModifyBill += rs.getDouble("dblNoOfModifyBill");
		sumRefund += rs.getDouble("dblRefund");
		sumNetSale += rs.getDouble("dblNetSale");
		sumGrossSale += rs.getDouble("dblGrossSale");
		sumAPC += rs.getDouble("dblAPC");
		dm.addRow(row);
	    }
	    tblDayEnd.setModel(dm);
	    tblDayEnd.setAutoscrolls(true);
	    Object[] totalSumrow =

	    {

		"Total", "", gDecimalFormat.format(sumHdAmt), gDecimalFormat.format(SumDining), gDecimalFormat.format(sumTaleAway), gDecimalFormat.format(sumtSale), gDecimalFormat.format(sumFloat), gDecimalFormat.format(sumCash), gDecimalFormat.format(sumAdvance), gDecimalFormat.format(sumTransferIn), gDecimalFormat.format(sumTotalReceipt), gDecimalFormat.format(sumPay), gDecimalFormat.format(sumWithDrawal), gDecimalFormat.format(sumTransferOut), gDecimalFormat.format(sumRefund), gDecimalFormat.format(sumTotalPay), gDecimalFormat.format(sumCashInhand), decFormat.format(sumNoOfBill), decFormat.format(SumNoOfVoidedBill), decFormat.format(SumNoOfModifyBill), "", "", gDecimalFormat.format(sumNetSale), gDecimalFormat.format(sumGrossSale), gDecimalFormat.format(sumAPC)
	    };
	    totalDm.addRow(totalSumrow);
	    tblTotal.setModel(totalDm);
	    tblTotal.setAutoscrolls(true);
	    DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
	    rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
	    for (int i = 2; i < 20; i++)
	    {
		tblDayEnd.getColumnModel().getColumn(i).setCellRenderer(rightRenderer);
		tblTotal.getColumnModel().getColumn(i).setCellRenderer(rightRenderer);
	    }
	    tblTotal.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);

	    tblDayEnd.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    tblDayEnd.getColumnModel().getColumn(0).setPreferredWidth(100);
	    tblDayEnd.getColumnModel().getColumn(1).setPreferredWidth(65);
	    tblDayEnd.getColumnModel().getColumn(2).setPreferredWidth(50);
	    tblDayEnd.getColumnModel().getColumn(3).setPreferredWidth(70);
	    tblDayEnd.getColumnModel().getColumn(4).setPreferredWidth(70);
	    tblDayEnd.getColumnModel().getColumn(5).setPreferredWidth(80);
	    tblDayEnd.getColumnModel().getColumn(6).setPreferredWidth(50);
	    tblDayEnd.getColumnModel().getColumn(7).setPreferredWidth(80);
	    tblDayEnd.getColumnModel().getColumn(8).setPreferredWidth(80);
	    tblDayEnd.getColumnModel().getColumn(9).setPreferredWidth(100);
	    tblDayEnd.getColumnModel().getColumn(10).setPreferredWidth(70);
	    tblDayEnd.getColumnModel().getColumn(11).setPreferredWidth(105);
	    tblDayEnd.getColumnModel().getColumn(12).setPreferredWidth(70);
	    tblDayEnd.getColumnModel().getColumn(13).setPreferredWidth(70);
	    tblDayEnd.getColumnModel().getColumn(14).setPreferredWidth(70);
	    tblDayEnd.getColumnModel().getColumn(15).setPreferredWidth(110);
	    tblDayEnd.getColumnModel().getColumn(16).setPreferredWidth(70);
	    tblDayEnd.getColumnModel().getColumn(17).setPreferredWidth(50);
	    tblDayEnd.getColumnModel().getColumn(18).setPreferredWidth(100);
	    tblDayEnd.getColumnModel().getColumn(19).setPreferredWidth(100);
	    tblDayEnd.getColumnModel().getColumn(20).setPreferredWidth(100);
	    tblDayEnd.getColumnModel().getColumn(21).setPreferredWidth(100);

	    tblTotal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    tblTotal.getColumnModel().getColumn(0).setPreferredWidth(100);
	    tblTotal.getColumnModel().getColumn(1).setPreferredWidth(65);
	    tblTotal.getColumnModel().getColumn(2).setPreferredWidth(50);
	    tblTotal.getColumnModel().getColumn(3).setPreferredWidth(70);
	    tblTotal.getColumnModel().getColumn(4).setPreferredWidth(70);
	    tblTotal.getColumnModel().getColumn(5).setPreferredWidth(80);
	    tblTotal.getColumnModel().getColumn(6).setPreferredWidth(50);
	    tblTotal.getColumnModel().getColumn(7).setPreferredWidth(80);
	    tblTotal.getColumnModel().getColumn(8).setPreferredWidth(80);
	    tblTotal.getColumnModel().getColumn(9).setPreferredWidth(100);
	    tblTotal.getColumnModel().getColumn(10).setPreferredWidth(70);
	    tblTotal.getColumnModel().getColumn(11).setPreferredWidth(105);
	    tblTotal.getColumnModel().getColumn(12).setPreferredWidth(70);
	    tblTotal.getColumnModel().getColumn(13).setPreferredWidth(70);
	    tblTotal.getColumnModel().getColumn(14).setPreferredWidth(70);
	    tblTotal.getColumnModel().getColumn(15).setPreferredWidth(110);
	    tblTotal.getColumnModel().getColumn(16).setPreferredWidth(70);
	    tblTotal.getColumnModel().getColumn(17).setPreferredWidth(50);
	    tblTotal.getColumnModel().getColumn(18).setPreferredWidth(100);
	    tblTotal.getColumnModel().getColumn(19).setPreferredWidth(100);
	    tblTotal.getColumnModel().getColumn(20).setPreferredWidth(100);
	    tblTotal.getColumnModel().getColumn(21).setPreferredWidth(100);
	    tblTotal.getColumnModel().getColumn(22).setPreferredWidth(100);
	    tblTotal.getColumnModel().getColumn(23).setPreferredWidth(100);
	    tblTotal.getColumnModel().getColumn(24).setPreferredWidth(100);

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	finally
	{
	    sbSql = null;
	}
    }

    private void funExportReportToExcel()
    {
	try
	{

	    File theDir = new File(clsPosConfigFile.exportReportPath);
	    File file = new File(clsPosConfigFile.exportReportPath + File.separator + "Day End Report " + objUtility.funGetDateInString() + ".xls");
	    // if the directory does not exist, create it
	    if (!theDir.exists())
	    {
		theDir.mkdir();
		funExportFile(tblDayEnd, file);
	    }
	    else
	    {
		funExportFile(tblDayEnd, file);
	    }
	}
	catch (Exception ex)
	{
	    ex.printStackTrace();
	}
    }

    /**
     * Export File
     *
     * @param table
     * @param file
     */
    private void funExportFile(JTable table, File file)
    {
	try
	{
	    WritableWorkbook workbook1 = Workbook.createWorkbook(file);
	    WritableSheet sheet1 = workbook1.createSheet("First Sheet", 0);
	    TableModel model = table.getModel();
	    sheet1.addCell(new Label(3, 0, "DSS Java Pos Audit Flash Report "));
	    //sheet1.addCell(new Label(0,2,"gsdfg "));
	    for (int i = 0; i < model.getColumnCount() + 1; i++)
	    {
		Label column = new Label(i, 1, model.getColumnName(i));
		sheet1.addCell(column);
	    }
	    int i = 0, j = 0;
	    int k = 0;
	    //System.out.println(model.getRowCount());
	    for (i = 1; i < model.getRowCount() + 1; i++)
	    {
		for (j = 0; j < model.getColumnCount(); j++)
		{
		    Label row = new Label(j, i + 1, model.getValueAt(k, j).toString());
		    sheet1.addCell(row);
		}
		k++;
	    }
	    funWriteDataToExcelFile(workbook1);
	    workbook1.write();
	    workbook1.close();

	    Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + clsPosConfigFile.exportReportPath + File.separator + "Day End Report " + objUtility.funGetDateInString() + ".xls");
	    //sendMail();
	}
	catch (FileNotFoundException ex)
	{
	    JOptionPane.showMessageDialog(this, "File is already opened please close ");
	    ex.printStackTrace();
	}
	catch (Exception ex)
	{
	    ex.printStackTrace();
	}
    }

    /**
     *
     * Add last Export File Modifed Bill Voided Bill Line Void Void Kot
     *
     * @param workbook1
     */
    private void funWriteDataToExcelFile(WritableWorkbook workbook1)
    {
	try
	{
	    int i = 0, j = 0, lastIndexReport = 0;
	    WritableSheet sheet2 = workbook1.getSheet(0);
	    int r = sheet2.getRows();
	    for (i = r; i < tblTotal.getRowCount() + r; i++)
	    {
		for (j = 0; j < tblTotal.getColumnCount(); j++)
		{
		    Label row = new Label(lastIndexReport + j, i + 1, tblTotal.getValueAt(0, j).toString());
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
	catch (IndexOutOfBoundsException | WriteException e)
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
    private void initComponents()
    {

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
        lblPosCode = new javax.swing.JLabel();
        cmbPosCode = new javax.swing.JComboBox();
        lblFromDate = new javax.swing.JLabel();
        dteFromDate = new com.toedter.calendar.JDateChooser();
        lblToDate = new javax.swing.JLabel();
        dteToDate = new com.toedter.calendar.JDateChooser();
        pnlDayEnd = new javax.swing.JScrollPane();
        tblDayEnd = new javax.swing.JTable();
        btnExecute = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();
        pnltotal = new javax.swing.JScrollPane();
        tblTotal = new javax.swing.JTable();
        btnExport = new javax.swing.JButton();

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

        lblformName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblformName.setForeground(new java.awt.Color(255, 255, 255));
        lblformName.setText("-Day End Flash");
        lblformName.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
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
        lblPosName.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
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
        lblUserCode.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                lblUserCodeMouseClicked(evt);
            }
        });
        pnlheader.add(lblUserCode);

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

        pnlBackGround.setOpaque(false);
        pnlBackGround.setLayout(new java.awt.GridBagLayout());

        pnlMain.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, new java.awt.Color(204, 204, 204), new java.awt.Color(204, 204, 204), new java.awt.Color(153, 153, 153), new java.awt.Color(153, 153, 153)));
        pnlMain.setMinimumSize(new java.awt.Dimension(800, 570));
        pnlMain.setOpaque(false);

        lblPosCode.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblPosCode.setText("POS");

        cmbPosCode.setToolTipText("Select POS");

        lblFromDate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblFromDate.setText("From Date");

        dteFromDate.setToolTipText("Select From Date");
        dteFromDate.setPreferredSize(new java.awt.Dimension(119, 35));

        lblToDate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblToDate.setText("To Date");

        dteToDate.setToolTipText("Select To Date");
        dteToDate.setPreferredSize(new java.awt.Dimension(119, 35));

        tblDayEnd.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String []
            {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tblDayEnd.setRowHeight(25);
        pnlDayEnd.setViewportView(tblDayEnd);

        btnExecute.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnExecute.setForeground(new java.awt.Color(255, 255, 255));
        btnExecute.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnExecute.setText("Execute");
        btnExecute.setToolTipText("Execute");
        btnExecute.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExecute.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn2.png"))); // NOI18N
        btnExecute.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
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
        btnClose.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnCloseMouseClicked(evt);
            }
        });
        btnClose.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnCloseActionPerformed(evt);
            }
        });

        tblTotal.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        tblTotal.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {
                {null, null, null, null}
            },
            new String []
            {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tblTotal.setRowHeight(30);
        pnltotal.setViewportView(tblTotal);

        btnExport.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnExport.setForeground(new java.awt.Color(255, 255, 255));
        btnExport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnExport.setText("Export");
        btnExport.setToolTipText("Close Window");
        btnExport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExport.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn2.png"))); // NOI18N
        btnExport.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnExportMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout pnlMainLayout = new javax.swing.GroupLayout(pnlMain);
        pnlMain.setLayout(pnlMainLayout);
        pnlMainLayout.setHorizontalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMainLayout.createSequentialGroup()
                .addGap(0, 15, Short.MAX_VALUE)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnltotal, javax.swing.GroupLayout.PREFERRED_SIZE, 800, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlMainLayout.createSequentialGroup()
                            .addComponent(lblPosCode)
                            .addGap(2, 2, 2)
                            .addComponent(cmbPosCode, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(lblFromDate)
                            .addGap(3, 3, 3)
                            .addComponent(dteFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(pnlMainLayout.createSequentialGroup()
                                    .addGap(50, 50, 50)
                                    .addComponent(dteToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(lblToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(btnExecute, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(btnExport, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(pnlDayEnd, javax.swing.GroupLayout.PREFERRED_SIZE, 795, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        pnlMainLayout.setVerticalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblPosCode, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cmbPosCode, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnExecute, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnExport, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(lblFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(dteFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(dteToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(12, 12, 12)
                .addComponent(pnlDayEnd, javax.swing.GroupLayout.PREFERRED_SIZE, 420, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7)
                .addComponent(pnltotal, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE)
                .addContainerGap())
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
	funFillDayEndTable();
    }//GEN-LAST:event_btnExecuteMouseClicked
    /**
     * Close Windows
     *
     * @param evt
     */
    private void btnCloseMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCloseMouseClicked
	// TODO add your handling code here:
	funResetLookAndFeel();
	dispose();
	clsGlobalVarClass.hmActiveForms.remove("Day End Flash");
    }//GEN-LAST:event_btnCloseMouseClicked

    private void lblProductNameMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblProductNameMouseClicked
    {//GEN-HEADEREND:event_lblProductNameMouseClicked
	// TODO add your handling code here:
	objUtility.funMinimizeWindow();
    }//GEN-LAST:event_lblProductNameMouseClicked

    private void lblformNameMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblformNameMouseClicked
    {//GEN-HEADEREND:event_lblformNameMouseClicked
	// TODO add your handling code here:
	objUtility.funMinimizeWindow();
    }//GEN-LAST:event_lblformNameMouseClicked

    private void lblPosNameMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblPosNameMouseClicked
    {//GEN-HEADEREND:event_lblPosNameMouseClicked
	// TODO add your handling code here:
	objUtility.funMinimizeWindow();
    }//GEN-LAST:event_lblPosNameMouseClicked

    private void lblUserCodeMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblUserCodeMouseClicked
    {//GEN-HEADEREND:event_lblUserCodeMouseClicked
	// TODO add your handling code here:
	objUtility.funMinimizeWindow();
    }//GEN-LAST:event_lblUserCodeMouseClicked

    private void lblDateMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblDateMouseClicked
    {//GEN-HEADEREND:event_lblDateMouseClicked
	// TODO add your handling code here:
	objUtility.funMinimizeWindow();
    }//GEN-LAST:event_lblDateMouseClicked

    private void lblHOSignMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblHOSignMouseClicked
    {//GEN-HEADEREND:event_lblHOSignMouseClicked
	// TODO add your handling code here:
	objUtility.funMinimizeWindow();
    }//GEN-LAST:event_lblHOSignMouseClicked

    private void btnExportMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnExportMouseClicked
	// TODO add your handling code here:
	funExportReportToExcel();
    }//GEN-LAST:event_btnExportMouseClicked

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("Day End Flash");
    }//GEN-LAST:event_btnCloseActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("Day End Flash");
    }//GEN-LAST:event_formWindowClosed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("Day End Flash");
    }//GEN-LAST:event_formWindowClosing


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnExecute;
    private javax.swing.JButton btnExport;
    private javax.swing.JComboBox cmbPosCode;
    private com.toedter.calendar.JDateChooser dteFromDate;
    private com.toedter.calendar.JDateChooser dteToDate;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblFromDate;
    private javax.swing.JLabel lblHOSign;
    private javax.swing.JLabel lblModuleName;
    private javax.swing.JLabel lblPosCode;
    private javax.swing.JLabel lblPosName;
    private javax.swing.JLabel lblProductName;
    private javax.swing.JLabel lblToDate;
    private javax.swing.JLabel lblUserCode;
    private javax.swing.JLabel lblformName;
    private javax.swing.JPanel pnlBackGround;
    private javax.swing.JScrollPane pnlDayEnd;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JPanel pnlheader;
    private javax.swing.JScrollPane pnltotal;
    private javax.swing.JTable tblDayEnd;
    private javax.swing.JTable tblTotal;
    // End of variables declaration//GEN-END:variables

}
