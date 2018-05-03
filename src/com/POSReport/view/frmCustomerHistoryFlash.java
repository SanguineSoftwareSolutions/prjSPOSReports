package com.POSReport.view;

import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsPosConfigFile;
import com.POSGlobal.controller.clsSalesFlashColumns;
import com.POSGlobal.controller.clsSalesFlashReport;
import com.POSGlobal.controller.clsUtility;
import com.POSGlobal.view.frmOkPopUp;
import com.POSGlobal.view.frmSearchFormDialog;
import com.POSReport.controller.clsBillItemDtlBean;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class frmCustomerHistoryFlash extends javax.swing.JFrame
{

    private clsUtility objUtility;
    StringBuilder sb = new StringBuilder();
    private String fromDate, toDate, reportName;
    private Object[] records, ob1;
    private String custCode;
    private String dateFilter;
    private int navigate;
    private String exportFormName, rDate, ExportReportPath, sql;
    private java.util.Vector vSalesReportExcelColLength;
    private DefaultTableModel dm, dm1, dmSales;
    private BigDecimal totalAmount, temp, temp1, Disc;

    JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
    private DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();
    private SimpleDateFormat ddMMyyyyDateFormate;

    /**
     * Creates new form frmCustomerHistoryFlashReport
     */
    public frmCustomerHistoryFlash()
    {

	initComponents();

	this.setLocationRelativeTo(null);

	funSetLookAndFeel();

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
	    navigate = 0;

	    clsPosConfigFile pc = new clsPosConfigFile();
	    ExportReportPath = clsPosConfigFile.exportReportPath;
	    rDate = clsGlobalVarClass.gPOSDateToDisplay;

	    objUtility = new clsUtility();
	    tctCustomerCode.setEditable(false);
	    tctCustomerCode.setEnabled(false);

	    dteFromDate.setDate(objUtility.funGetDateToSetCalenderDate());
	    dteToDate.setDate(objUtility.funGetDateToSetCalenderDate());

	    ddMMyyyyDateFormate = new SimpleDateFormat("dd-MM-yyyy");

	    if (clsGlobalVarClass.gShowOnlyLoginPOSReports)
	    {
		cmbPosCode.addItem(clsGlobalVarClass.gPOSName + " " + clsGlobalVarClass.gPOSCode);
	    }
	    else
	    {
		cmbPosCode.addItem("All");
		sb.setLength(0);
		sb.append("select strPosName,strPosCode from tblposmaster");
		ResultSet rs1 = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
		while (rs1.next())
		{
		    cmbPosCode.addItem(rs1.getString(1)+" "+rs1.getString(2));
		}
		rs1.close();
	    }

	    dm = new DefaultTableModel();
	    dm.addColumn("Customer Name");
	    dm.addColumn("No. Of Bills");
	    dm.addColumn("Sales Amount");

	    vSalesReportExcelColLength = new java.util.Vector();
	    vSalesReportExcelColLength.add("6#Left"); //date
	    vSalesReportExcelColLength.add("15#Left"); //no of bills
	    vSalesReportExcelColLength.add("6#Right"); //subtotal
	    vSalesReportExcelColLength.add("6#Right"); //disc
	    vSalesReportExcelColLength.add("6#Right"); //tax
	    vSalesReportExcelColLength.add("6#Right"); //salestotal

	    int selectedIndex = panelDeliveryCharges.getSelectedIndex();
	    System.out.println("Default Index:" + selectedIndex);

	    if (panelDeliveryCharges.getSelectedIndex() == 0)
	    {

		btnExport.setEnabled(false);
	    }

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

    /*
     * To set the Customer Name in Search by the Help 
     * and set the Customer Code into variable
     */
    void funSetCustomerData(Object[] data)
    {
	tctCustomerCode.setText(data[0].toString());
	txtCustomerName.setText(data[1].toString());
	//custCode = data[0].toString();

    }

    /*
     * Reset the Fields
     */
    private void funResetField()
    {
	try
	{

	    lblDate.setText(objUtility.funGetDateInString());

	    dteFromDate.setDate(objUtility.funGetDateToSetCalenderDate());
	    dteToDate.setDate(objUtility.funGetDateToSetCalenderDate());
	    tctCustomerCode.setText("");
	    txtCustomerName.setText("");
	    custCode = null;
	    cmbPosCode.setSelectedItem("All");
	    cmbReportType.setSelectedItem("Customer Wise");
	    txtAmount.setText("");
	    dm.setRowCount(0);
	    dm1.setRowCount(0);
	    dmSales.setRowCount(0);
	    txtAmount.setEditable(true);
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
        jPanel1 = new javax.swing.JPanel();
        dteFromDate = new com.toedter.calendar.JDateChooser();
        dteToDate = new com.toedter.calendar.JDateChooser();
        lblposcode = new javax.swing.JLabel();
        lblToDate = new javax.swing.JLabel();
        btnExport = new javax.swing.JButton();
        btnExecute = new javax.swing.JButton();
        lblFromDate = new javax.swing.JLabel();
        cmbPosCode = new javax.swing.JComboBox();
        panelDeliveryCharges = new javax.swing.JTabbedPane();
        panelOfficeAddress = new javax.swing.JPanel();
        lblcustCode = new javax.swing.JLabel();
        lblReportType = new javax.swing.JLabel();
        cmbReportType = new javax.swing.JComboBox();
        txtCustomerName = new javax.swing.JTextField();
        tctCustomerCode = new javax.swing.JTextField();
        pnlSalesData = new javax.swing.JScrollPane();
        tblCustomerWise = new javax.swing.JTable();
        pnlSalesTotal6 = new javax.swing.JScrollPane();
        tblTotal6 = new javax.swing.JTable();
        panelTemporaryAddress = new javax.swing.JPanel();
        lblAmount = new javax.swing.JLabel();
        cmbAmount = new javax.swing.JComboBox();
        txtAmount = new javax.swing.JTextField();
        pnlSalesData1 = new javax.swing.JScrollPane();
        tblTopSpenders = new javax.swing.JTable();
        pnlSalesTotal4 = new javax.swing.JScrollPane();
        tblTotal4 = new javax.swing.JTable();
        pnlSalesTotal5 = new javax.swing.JScrollPane();
        tblTotal5 = new javax.swing.JTable();
        panelFormBody = new javax.swing.JPanel();
        pnlSalesData2 = new javax.swing.JScrollPane();
        tblNonTopSpenders = new javax.swing.JTable();
        btnCancel = new javax.swing.JButton();
        btnCancel1 = new javax.swing.JButton();

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
        pnlheader.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
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
        lblformName.setText(" Customer History Flash");
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

        pnlBackGround.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        pnlBackGround.setOpaque(false);
        pnlBackGround.setLayout(new java.awt.GridBagLayout());

        pnlMain.setBackground(new java.awt.Color(255, 255, 255));
        pnlMain.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, new java.awt.Color(204, 204, 204), new java.awt.Color(204, 204, 204), new java.awt.Color(153, 153, 153), new java.awt.Color(153, 153, 153)));
        pnlMain.setMinimumSize(new java.awt.Dimension(800, 570));
        pnlMain.setOpaque(false);

        dteFromDate.setBackground(new java.awt.Color(51, 102, 255));
        dteFromDate.setToolTipText("Select From Date");
        dteFromDate.setPreferredSize(new java.awt.Dimension(119, 35));

        dteToDate.setBackground(new java.awt.Color(51, 102, 255));
        dteToDate.setToolTipText("Select To Date");
        dteToDate.setPreferredSize(new java.awt.Dimension(119, 35));

        lblposcode.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblposcode.setText("POS Name");

        lblToDate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblToDate.setText("To Date");

        btnExport.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnExport.setForeground(new java.awt.Color(255, 255, 255));
        btnExport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnExport.setText("Export");
        btnExport.setToolTipText("Export File");
        btnExport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExport.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnExportMouseClicked(evt);
            }
        });
        btnExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportActionPerformed(evt);
            }
        });

        btnExecute.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnExecute.setForeground(new java.awt.Color(255, 255, 255));
        btnExecute.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnExecute.setText("Execute");
        btnExecute.setToolTipText("Execute Report");
        btnExecute.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExecute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExecuteActionPerformed(evt);
            }
        });

        lblFromDate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblFromDate.setText("From Date");

        cmbPosCode.setToolTipText("Select POS");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(lblposcode)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbPosCode, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(dteFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(dteToDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 43, Short.MAX_VALUE)
                .addComponent(btnExecute, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnExport, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(32, 32, 32))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dteToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnExecute, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnExport, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblposcode, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cmbPosCode, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(lblFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(dteFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(10, 10, 10))
        );

        panelDeliveryCharges.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                panelDeliveryChargesMouseClicked(evt);
            }
        });

        panelOfficeAddress.setOpaque(false);
        panelOfficeAddress.setLayout(null);

        lblcustCode.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblcustCode.setText("Cust Code");
        panelOfficeAddress.add(lblcustCode);
        lblcustCode.setBounds(10, 10, 56, 30);

        lblReportType.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblReportType.setText("Report Type");
        panelOfficeAddress.add(lblReportType);
        lblReportType.setBounds(580, 10, 90, 30);

        cmbReportType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Bill Wise", "Item Wise" }));
        cmbReportType.setToolTipText("Select Type");
        cmbReportType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbReportTypeActionPerformed(evt);
            }
        });
        panelOfficeAddress.add(cmbReportType);
        cmbReportType.setBounds(670, 10, 120, 30);

        txtCustomerName.setEditable(false);
        txtCustomerName.setToolTipText("Enter Name");
        txtCustomerName.setBorder(null);
        txtCustomerName.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                txtCustomerNameMouseClicked(evt);
            }
        });
        panelOfficeAddress.add(txtCustomerName);
        txtCustomerName.setBounds(210, 10, 280, 30);

        tctCustomerCode.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tctCustomerCodeMouseClicked(evt);
            }
        });
        panelOfficeAddress.add(tctCustomerCode);
        tctCustomerCode.setBounds(90, 10, 110, 30);

        tblCustomerWise.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Customer Name", "No. Of Bills", "Sales Amount"
            }
        ));
        tblCustomerWise.setFillsViewportHeight(true);
        tblCustomerWise.setRowHeight(25);
        tblCustomerWise.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblCustomerWiseMouseClicked(evt);
            }
        });
        pnlSalesData.setViewportView(tblCustomerWise);

        panelOfficeAddress.add(pnlSalesData);
        pnlSalesData.setBounds(0, 50, 840, 340);

        tblTotal6.setFont(new java.awt.Font("DejaVu Sans", 1, 14)); // NOI18N
        tblTotal6.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "", "SubTotal", "", "Disc", "Tax Total", "Sales Amount", "Tip Amount"
            }
        ));
        tblTotal6.setRowHeight(25);
        pnlSalesTotal6.setViewportView(tblTotal6);

        panelOfficeAddress.add(pnlSalesTotal6);
        pnlSalesTotal6.setBounds(0, 390, 840, 70);

        panelDeliveryCharges.addTab("Customer Wise", panelOfficeAddress);

        panelTemporaryAddress.setOpaque(false);

        lblAmount.setText("Amount");

        cmbAmount.setModel(new javax.swing.DefaultComboBoxModel(new String[] { ">=", "<=", "=" }));

        txtAmount.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtAmountKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtAmountKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtAmountKeyTyped(evt);
            }
        });

        tblTopSpenders.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Mobile Number", "Customer Name", "No.of Bills", "Sales Amount"
            }
        ));
        tblTopSpenders.setFillsViewportHeight(true);
        tblTopSpenders.setRowHeight(25);
        tblTopSpenders.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblTopSpendersMouseClicked(evt);
            }
        });
        pnlSalesData1.setViewportView(tblTopSpenders);

        tblTotal4.setFont(new java.awt.Font("DejaVu Sans", 1, 14)); // NOI18N
        tblTotal4.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tblTotal4.setRowHeight(25);
        pnlSalesTotal4.setViewportView(tblTotal4);

        tblTotal5.setFont(new java.awt.Font("DejaVu Sans", 1, 14)); // NOI18N
        tblTotal5.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "", "Sales Amount"
            }
        ));
        tblTotal5.setRowHeight(25);
        pnlSalesTotal5.setViewportView(tblTotal5);

        javax.swing.GroupLayout panelTemporaryAddressLayout = new javax.swing.GroupLayout(panelTemporaryAddress);
        panelTemporaryAddress.setLayout(panelTemporaryAddressLayout);
        panelTemporaryAddressLayout.setHorizontalGroup(
            panelTemporaryAddressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTemporaryAddressLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblAmount)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cmbAmount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(txtAmount, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(632, Short.MAX_VALUE))
            .addGroup(panelTemporaryAddressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(pnlSalesData1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 840, Short.MAX_VALUE))
            .addGroup(panelTemporaryAddressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelTemporaryAddressLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(pnlSalesTotal4, javax.swing.GroupLayout.PREFERRED_SIZE, 798, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
            .addGroup(panelTemporaryAddressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(pnlSalesTotal5, javax.swing.GroupLayout.DEFAULT_SIZE, 840, Short.MAX_VALUE))
        );
        panelTemporaryAddressLayout.setVerticalGroup(
            panelTemporaryAddressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTemporaryAddressLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelTemporaryAddressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblAmount, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbAmount, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtAmount, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(420, Short.MAX_VALUE))
            .addGroup(panelTemporaryAddressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelTemporaryAddressLayout.createSequentialGroup()
                    .addContainerGap(50, Short.MAX_VALUE)
                    .addComponent(pnlSalesData1, javax.swing.GroupLayout.PREFERRED_SIZE, 337, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(74, 74, 74)))
            .addGroup(panelTemporaryAddressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelTemporaryAddressLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(pnlSalesTotal4, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
            .addGroup(panelTemporaryAddressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelTemporaryAddressLayout.createSequentialGroup()
                    .addGap(0, 388, Short.MAX_VALUE)
                    .addComponent(pnlSalesTotal5, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        panelDeliveryCharges.addTab("Top Spenders", panelTemporaryAddress);

        panelFormBody.setBackground(new java.awt.Color(255, 255, 255));
        panelFormBody.setOpaque(false);
        panelFormBody.setPreferredSize(new java.awt.Dimension(610, 600));
        panelFormBody.setLayout(null);

        tblNonTopSpenders.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Mobile Number", "Customer Name", "Last Transaction Date"
            }
        ));
        tblNonTopSpenders.setFillsViewportHeight(true);
        tblNonTopSpenders.setRowHeight(25);
        tblNonTopSpenders.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblNonTopSpendersMouseClicked(evt);
            }
        });
        pnlSalesData2.setViewportView(tblNonTopSpenders);

        panelFormBody.add(pnlSalesData2);
        pnlSalesData2.setBounds(0, 0, 840, 460);

        panelDeliveryCharges.addTab("Non-Spenders", panelFormBody);

        btnCancel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnCancel.setForeground(new java.awt.Color(255, 255, 255));
        btnCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCommonBtn1.png"))); // NOI18N
        btnCancel.setText("Reset");
        btnCancel.setToolTipText("Close Customer Master Form");
        btnCancel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnCancel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnCancelMouseClicked(evt);
            }
        });
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        btnCancel1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnCancel1.setForeground(new java.awt.Color(255, 255, 255));
        btnCancel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCommonBtn1.png"))); // NOI18N
        btnCancel1.setText("CLOSE");
        btnCancel1.setToolTipText("Close Customer Master Form");
        btnCancel1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnCancel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnCancel1MouseClicked(evt);
            }
        });
        btnCancel1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancel1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlMainLayout = new javax.swing.GroupLayout(pnlMain);
        pnlMain.setLayout(pnlMainLayout);
        pnlMainLayout.setHorizontalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMainLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnCancel1, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnlMainLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(panelDeliveryCharges)
                    .addContainerGap()))
        );
        pnlMainLayout.setVerticalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 509, Short.MAX_VALUE)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancel1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
            .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnlMainLayout.createSequentialGroup()
                    .addGap(59, 59, 59)
                    .addComponent(panelDeliveryCharges, javax.swing.GroupLayout.PREFERRED_SIZE, 489, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(64, Short.MAX_VALUE)))
        );

        pnlBackGround.add(pnlMain, new java.awt.GridBagConstraints());

        getContentPane().add(pnlBackGround, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void lblProductNameMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblProductNameMouseClicked
    {//GEN-HEADEREND:event_lblProductNameMouseClicked

    }//GEN-LAST:event_lblProductNameMouseClicked

    private void lblformNameMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblformNameMouseClicked
    {//GEN-HEADEREND:event_lblformNameMouseClicked

    }//GEN-LAST:event_lblformNameMouseClicked

    private void lblPosNameMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblPosNameMouseClicked
    {//GEN-HEADEREND:event_lblPosNameMouseClicked

    }//GEN-LAST:event_lblPosNameMouseClicked

    private void lblUserCodeMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblUserCodeMouseClicked
    {//GEN-HEADEREND:event_lblUserCodeMouseClicked

    }//GEN-LAST:event_lblUserCodeMouseClicked

    private void lblDateMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblDateMouseClicked
    {//GEN-HEADEREND:event_lblDateMouseClicked

    }//GEN-LAST:event_lblDateMouseClicked

    private void lblHOSignMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblHOSignMouseClicked
    {//GEN-HEADEREND:event_lblHOSignMouseClicked

    }//GEN-LAST:event_lblHOSignMouseClicked

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("BillWiseSettlementSalesSummaryFlash");
    }//GEN-LAST:event_formWindowClosed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("BillWiseSettlementSalesSummaryFlash");
    }//GEN-LAST:event_formWindowClosing

    private void btnExportMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnExportMouseClicked

    }//GEN-LAST:event_btnExportMouseClicked

    private void btnExecuteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExecuteActionPerformed
	// TODO add your handling code here:

	funSalesReportButton7Clicked();
    }//GEN-LAST:event_btnExecuteActionPerformed

    private void cmbReportTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbReportTypeActionPerformed
	// TODO add your handling code here:


    }//GEN-LAST:event_cmbReportTypeActionPerformed

    private void txtCustomerNameMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txtCustomerNameMouseClicked
	// TODO add your handling code here:
    }//GEN-LAST:event_txtCustomerNameMouseClicked

    private void btnCancelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCancelMouseClicked
	// TODO add your handling code here:
    }//GEN-LAST:event_btnCancelMouseClicked

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
	funResetField();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnCancel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCancel1MouseClicked
	clsGlobalVarClass.hmActiveForms.remove("Customer History Flash Report");
	dispose();
    }//GEN-LAST:event_btnCancel1MouseClicked

    private void btnCancel1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancel1ActionPerformed
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("Customer History Flash Report");
	funResetLookAndFeel();
	dispose();
    }//GEN-LAST:event_btnCancel1ActionPerformed

    private void tctCustomerCodeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tctCustomerCodeMouseClicked
	objUtility.funCallForSearchForm("CustomerMaster");
	new frmSearchFormDialog(this, true).setVisible(true);
	if (clsGlobalVarClass.gSearchItemClicked)
	{

	    Object[] data = clsGlobalVarClass.gArrListSearchData.toArray();
	    funSetCustomerData(data);
	    clsGlobalVarClass.gSearchItemClicked = false;
	}
	btnExport.setEnabled(false);
	if (tctCustomerCode.getText().trim().length() == 0)
	{
	    funCustomerWiseBillSales();
	}
	else
	{
	    if (cmbReportType.getSelectedItem().toString().equalsIgnoreCase("Item Wise"))
	    {
		if (tctCustomerCode.getText().trim().length() == 0)
		{
		    JOptionPane.showMessageDialog(this, "Please Select Customer");
		    return;
		}
		funCustomerWiseItemSales();
	    }
	    else
	    {
		if (tctCustomerCode.getText().trim().length() == 0)
		{
		    JOptionPane.showMessageDialog(this, "Please Select Customer");
		    return;
		}
		funCustomerWiseBillSales();
	    }
	}
    }//GEN-LAST:event_tctCustomerCodeMouseClicked

    private void tblCustomerWiseMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblCustomerWiseMouseClicked


    }//GEN-LAST:event_tblCustomerWiseMouseClicked

    private void tblTopSpendersMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblTopSpendersMouseClicked
	// TODO add your handling code here:
    }//GEN-LAST:event_tblTopSpendersMouseClicked

    private void tblNonTopSpendersMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblNonTopSpendersMouseClicked
	// TODO add your handling code here:
    }//GEN-LAST:event_tblNonTopSpendersMouseClicked

    private void txtAmountKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtAmountKeyPressed
	// TODO add your handling code here:
	txtAmount.setHorizontalAlignment(JTextField.RIGHT);
	if ((evt.getKeyChar() >= '0') && (evt.getKeyChar() <= '9') || (evt.getKeyCode() == 10))
	{
	    txtAmount.setEditable(true);
	}
	if (evt.getKeyCode() == 10)
	{
	    if (!txtAmount.getText().toString().isEmpty())
	    {
		//JOptionPane.showMessageDialog(this,"You can now execute");
		funTopSpenderWiseSales();
	    }
	}


    }//GEN-LAST:event_txtAmountKeyPressed

    private void txtAmountKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtAmountKeyTyped
	// TODO add your handling code here:

    }//GEN-LAST:event_txtAmountKeyTyped

    private void btnExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportActionPerformed
	// TODO add your handling code here:
	try
	{
	    File theDir = new File(ExportReportPath);
	    File file = new File(ExportReportPath + "/" + exportFormName + rDate + ".xls");

	    if (panelDeliveryCharges.getSelectedIndex() == 0)
	    {
		if (!theDir.exists())
		{
		    theDir.mkdir();
		    funExportFile(tblCustomerWise, file);
		    //sendMail();
		}
		else
		{
		    funExportFile(tblCustomerWise, file);
		    //sendMail();
		}
	    }
	    if (panelDeliveryCharges.getSelectedIndex() == 1)
	    {
		if (!theDir.exists())
		{
		    theDir.mkdir();
		    funExportFile(tblTopSpenders, file);
		    //sendMail();
		}
		else
		{
		    funExportFile(tblTopSpenders, file);
		    //sendMail();
		}
	    }
	    if (panelDeliveryCharges.getSelectedIndex() == 2)
	    {
		if (!theDir.exists())
		{
		    theDir.mkdir();
		    funExportFile(tblNonTopSpenders, file);
		    //sendMail();
		}
		else
		{
		    funExportFile(tblNonTopSpenders, file);
		    //sendMail();
		}
	    }
	}
	catch (Exception ex)
	{
	    ex.printStackTrace();
	}
    }//GEN-LAST:event_btnExportActionPerformed

    private void txtAmountKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtAmountKeyReleased
	// TODO add your handling code here:
	txtAmount.setHorizontalAlignment(JTextField.RIGHT);
	if ((evt.getKeyChar() >= '0') && (evt.getKeyChar() <= '9') || (evt.getKeyCode() == 10))
	{
	    txtAmount.setEditable(true);
	}
	else if (evt.getKeyCode() == 10)
	{
	    if (!txtAmount.getText().toString().isEmpty())
	    {
		//JOptionPane.showMessageDialog(this,"You can now execute");
		funTopSpenderWiseSales();
	    }
	}
	else
	{
	    txtAmount.setEditable(false);
	    JOptionPane.showMessageDialog(this, "Enter Only numeric values(0-9)");
	}


    }//GEN-LAST:event_txtAmountKeyReleased

    private void panelDeliveryChargesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panelDeliveryChargesMouseClicked
	// TODO add your handling code here:
	if (panelDeliveryCharges.getSelectedIndex() == 1)
	{

	    btnExport.setEnabled(true);
	}
	else if (panelDeliveryCharges.getSelectedIndex() == 2)
	{

	    btnExport.setEnabled(true);
	}
	else
	{
	    if (tctCustomerCode.getText().length() == 0)
	    {
		btnExport.setEnabled(true);
	    }
	    else
	    {
		btnExport.setEnabled(false);
	    }
	}
    }//GEN-LAST:event_panelDeliveryChargesMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnCancel1;
    private javax.swing.JButton btnExecute;
    private javax.swing.JButton btnExport;
    private javax.swing.JComboBox cmbAmount;
    private javax.swing.JComboBox cmbPosCode;
    private javax.swing.JComboBox cmbReportType;
    private com.toedter.calendar.JDateChooser dteFromDate;
    private com.toedter.calendar.JDateChooser dteToDate;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblAmount;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblFromDate;
    private javax.swing.JLabel lblHOSign;
    private javax.swing.JLabel lblModuleName;
    private javax.swing.JLabel lblPosName;
    private javax.swing.JLabel lblProductName;
    private javax.swing.JLabel lblReportType;
    private javax.swing.JLabel lblToDate;
    private javax.swing.JLabel lblUserCode;
    private javax.swing.JLabel lblcustCode;
    private javax.swing.JLabel lblformName;
    private javax.swing.JLabel lblposcode;
    private javax.swing.JTabbedPane panelDeliveryCharges;
    private javax.swing.JPanel panelFormBody;
    private javax.swing.JPanel panelOfficeAddress;
    private javax.swing.JPanel panelTemporaryAddress;
    private javax.swing.JPanel pnlBackGround;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JScrollPane pnlSalesData;
    private javax.swing.JScrollPane pnlSalesData1;
    private javax.swing.JScrollPane pnlSalesData2;
    private javax.swing.JScrollPane pnlSalesTotal4;
    private javax.swing.JScrollPane pnlSalesTotal5;
    private javax.swing.JScrollPane pnlSalesTotal6;
    private javax.swing.JPanel pnlheader;
    private javax.swing.JTable tblCustomerWise;
    private javax.swing.JTable tblNonTopSpenders;
    private javax.swing.JTable tblTopSpenders;
    private javax.swing.JTable tblTotal4;
    private javax.swing.JTable tblTotal5;
    private javax.swing.JTable tblTotal6;
    private javax.swing.JTextField tctCustomerCode;
    private javax.swing.JTextField txtAmount;
    private javax.swing.JTextField txtCustomerName;
    // End of variables declaration//GEN-END:variables

    /**
     * fun get From Date
     *
     * @return
     * @throws Exception
     */
    private String funGetFromDate() throws Exception
    {
	String fromDate = null;
	java.util.Date dt1 = new java.util.Date();
	dt1 = dteFromDate.getDate();
	int d = dt1.getDate();
	int m = dt1.getMonth() + 1;
	int y = dt1.getYear() + 1900;
	fromDate = y + "-" + m + "-" + d;
	return fromDate;
    }

    /**
     * Get To Date
     *
     * @return
     * @throws Exception
     */
    private String funGetToDate() throws Exception
    {
	String toDate = null;
	Date dt2 = dteToDate.getDate();
	int d = dt2.getDate();
	int m = dt2.getMonth() + 1;
	int y = dt2.getYear() + 1900;
	toDate = y + "-" + m + "-" + d;
	return toDate;
    }

    /**
     * get Selected Pos Code
     *
     * @return
     * @throws Exception
     */
    private String funGetSelectedPosCode() throws Exception
    {
	String pos = null;

	String posCode = cmbPosCode.getSelectedItem().toString();
	StringBuilder sb = new StringBuilder(posCode);
	int len = posCode.length();
	int lastInd = sb.lastIndexOf(" ");
	pos = sb.substring(lastInd + 1, len).toString();

	return pos;
    }

    private void funSalesReportButton7Clicked()
    {
	if (panelDeliveryCharges.getSelectedIndex() == 0)
	{
//        if (navigate == 0)
//        {

	    exportFormName = "CustomerWiseSales";
	    if (tctCustomerCode.getText().trim().length() == 0)
	    {
		btnExport.setEnabled(true);
		funCustomerWiseBillSales();
	    }
	    else
	    {
		btnExport.setEnabled(false);
		if (cmbReportType.getSelectedItem().toString().equalsIgnoreCase("Item Wise"))
		{
		    if (tctCustomerCode.getText().trim().length() == 0)
		    {
			JOptionPane.showMessageDialog(this, "Please Select Customer");
			return;
		    }
		    funCustomerWiseItemSales();
		}
		else
		{
		    if (tctCustomerCode.getText().trim().length() == 0)
		    {
			JOptionPane.showMessageDialog(this, "Please Select Customer");
			return;
		    }
		    funCustomerWiseBillSales();
		}
	    }

	}
	if (panelDeliveryCharges.getSelectedIndex() == 1)
	{
	    exportFormName = "TopSpenders";
	    funTopSpenderWiseSales();
	    //funTopSpenderWiseSales();
	}
	if (panelDeliveryCharges.getSelectedIndex() == 2)
	{
	    exportFormName = "NonSpenders";
	    funNonSpenderWiseSales();
	    //funTopSpenderWiseSales();
	}
    }

    /**
     * Customer Wise Item Sales
     */
    private void funCustomerWiseItemSales()
    {
	StringBuilder sbSqlLiveBill = new StringBuilder();
	StringBuilder sbSqlQFileBill = new StringBuilder();
	StringBuilder sbSqlFilters = new StringBuilder();
	try
	{
	    reportName = "Customer Wise Item Sales";
	    sql = "";
	    exportFormName = "CustomerWiseItemSales";
	    fromDate = funGetFromDate();
	    toDate = funGetToDate();

	    String DateFrom = null, field = null, DateTo = null;
	    if (funGetFromDate() != null)
	    {
		DateFrom = fromDate;
		field = "dteBillDate";
	    }
	    else
	    {
		DateFrom = fromDate;
		field = "date(dteBillDate)";
	    }
	    if (funGetToDate() != null)
	    {
		DateTo = toDate;
	    }
	    else
	    {
		DateTo = toDate;
	    }

	    dm = new DefaultTableModel()
	    {
		@Override
		public boolean isCellEditable(int row, int column)
		{
		    return false;
		}
	    };

	    dm.addColumn("Bill No");
	    dm.addColumn("Bill Date");
	    // dm.addColumn("Customer Code");
	    //dm.addColumn("Customer Name");
	    dm.addColumn("Item Name");
	    dm.addColumn("Quantity");
	    dm.addColumn("Amount");

	    vSalesReportExcelColLength = new java.util.Vector();
	    vSalesReportExcelColLength.add("6#Left");
	    vSalesReportExcelColLength.add("6#Left");
	    vSalesReportExcelColLength.add("6#Left");
	    vSalesReportExcelColLength.add("10#Left");
	    vSalesReportExcelColLength.add("10#Left");

	    dm1 = new DefaultTableModel();

	    dm1.addColumn("");
	    dm1.addColumn("Sales Amount");
	    tblTotal6.setModel(dm1);

	    records = new Object[5];
	    String pos = funGetSelectedPosCode();

	    sbSqlLiveBill.setLength(0);
	    sbSqlQFileBill.setLength(0);
	    sbSqlFilters.setLength(0);

	    sbSqlLiveBill.append("select a.strBillNo,date(a.dteBillDate)"
		    + ",c.strCustomerCode,c.strCustomerName,d.strItemName"
		    + ",TRUNCATE(sum(b.dblQuantity),0),sum(b.dblAmount),'" + clsGlobalVarClass.gUserCode + "' "
		    + "from tblbillhd a,tblbilldtl b,tblcustomermaster c,tblitemmaster d "
		    + "where a.strBillNo=b.strBillNo and a.strClientCode=b.strClientCode and a.strCustomerCode=c.strCustomerCode "
		    + "and b.strItemCode=d.strItemCode and a.strCustomerCode='" + tctCustomerCode.getText() + "'"
		    + "and date(a.dteBillDate) between '" + DateFrom + "' and '" + DateTo + "'");

	    sbSqlQFileBill.append("select a.strBillNo,date(a.dteBillDate)"
		    + ",c.strCustomerCode,c.strCustomerName,d.strItemName"
		    + ",TRUNCATE(sum(b.dblQuantity),0),sum(b.dblAmount),'" + clsGlobalVarClass.gUserCode + "' "
		    + "from tblqbillhd a,tblqbilldtl b,tblcustomermaster c,tblitemmaster d "
		    + "where a.strBillNo=b.strBillNo and a.strClientCode=b.strClientCode and a.strCustomerCode=c.strCustomerCode "
		    + "and b.strItemCode=d.strItemCode and a.strCustomerCode='" + tctCustomerCode.getText() + "'"
		    + "and date(a.dteBillDate) between '" + DateFrom + "' and '" + DateTo + "'");

	    if (!pos.equals("All"))
	    {
		sbSqlFilters.append(" and a.strPOSCode='" + pos + "' ");
	    }

	    sbSqlFilters.append(" group by d.strItemName");

	    boolean flgRecords = false;
	    double qty = 0, amount = 0;
	    double totalAmt = 0, totalAmt1 = 0;

	    sbSqlLiveBill.append(sbSqlFilters);
	    sbSqlQFileBill.append(sbSqlFilters);
	    //System.out.println(sbSqlLiveBill);
	    //System.out.println(sbSqlQFileBill);

//            clsSalesFlashReport obj = new clsSalesFlashReport();
//            obj.funProcessSalesFlashReport(sbSqlLiveBill.toString(), sbSqlQFileBill.toString(), "CustWiseItemSales");
//
//            sql = "select * from tbltempsalesflash1 where strUser='" + clsGlobalVarClass.gUserCode + "'";
//            ResultSet rsCustomerWise = clsGlobalVarClass.dbMysql.executeResultSet(sql);
//            while (rsCustomerWise.next())
//            {
//                flgRecords = true;
//             
	    List<clsBillItemDtlBean> listOfBillData = new ArrayList<clsBillItemDtlBean>();
	    Map mapMultiSettleBills = new HashMap();

	    ResultSet rsData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLiveBill.toString());
	    while (rsData.next())
	    {
		clsBillItemDtlBean obj = new clsBillItemDtlBean();
		obj.setStrBillNo(rsData.getString(1));
		obj.setDteBillDate(rsData.getString(2));
		obj.setStrCustomerCode(rsData.getString(3));
		obj.setStrCustomerName(rsData.getString(4));
		obj.setStrItemName(rsData.getString(5));
		obj.setDblQuantity(Double.parseDouble(rsData.getString(6)));
		obj.setDblAmount(rsData.getDouble(7));

		listOfBillData.add(obj);
		totalAmt += obj.getDblAmount(); // Grand Total     
	    }
	    ResultSet rsData1 = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFileBill.toString());
	    while (rsData1.next())
	    {
		clsBillItemDtlBean obj = new clsBillItemDtlBean();

		obj.setStrBillNo(rsData1.getString(1));
		obj.setDteBillDate(rsData1.getString(2));
		obj.setStrCustomerCode(rsData1.getString(3));
		obj.setStrCustomerName(rsData1.getString(4));
		obj.setStrItemName(rsData1.getString(5));
		obj.setDblQuantity(Double.parseDouble(rsData1.getString(6)));
		obj.setDblAmount(rsData1.getDouble(7));
		listOfBillData.add(obj);
		totalAmt += obj.getDblGrandTotal(); // Grand Total       
	    }

	    for (int i = 0; i < listOfBillData.size(); i++)
	    {
		clsBillItemDtlBean obj = listOfBillData.get(i);
		records[0] = obj.getStrBillNo();
		String tempBillDate = obj.getDteBillDate();
		String[] spDate = tempBillDate.split("-");
		records[1] = spDate[2] + "-" + spDate[1] + "-" + spDate[0];//Bill Date
		records[2] = obj.getStrItemName();//Item Name
		records[3] = obj.getDblQuantity();//Qty
		Double totDblAmt = obj.getDblAmount();
		records[4] = gDecimalFormat.format(totDblAmt);//Amountxcxc
		totalAmt1 += totDblAmt;
		dm.addRow(records);
	    }
	    rsData.close();
	    rsData1.close();

	    Object[] ob1
		    =
		    {
			"Total", gDecimalFormat.format(totalAmt1)
		    };
	    dm1.addRow(ob1);

	    tblTotal6.setModel(dm1);
	    tblTotal6.setRowHeight(40);

	    tblCustomerWise.setModel(dm);
	    tblCustomerWise.setRowHeight(25);

	    DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
	    rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
	    DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
	    centerRenderer.setHorizontalAlignment(JLabel.CENTER);

	    tblCustomerWise.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
	    tblCustomerWise.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
	    tblCustomerWise.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

	    tblCustomerWise.getColumnModel().getColumn(0).setPreferredWidth(100);
	    tblCustomerWise.getColumnModel().getColumn(1).setPreferredWidth(100);
	    tblCustomerWise.getColumnModel().getColumn(2).setPreferredWidth(200);

	    tblTotal6.setSize(400, 400);
	    DefaultTableCellRenderer rightRenderer1 = new DefaultTableCellRenderer();
	    rightRenderer1.setHorizontalAlignment(JLabel.RIGHT);
	    tblTotal6.getColumnModel().getColumn(1).setCellRenderer(rightRenderer1);
	    tblTotal6.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    tblTotal6.getColumnModel().getColumn(0).setPreferredWidth(400);
	    tblTotal6.getColumnModel().getColumn(1).setPreferredWidth(435);
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	finally
	{
	    sbSqlLiveBill = null;
	    sbSqlQFileBill = null;
	    sbSqlFilters = null;
	}
    }

//    /**
//     * Customer Wise Bill Sales
//     */
//    private void funCustomerWiseBillSales() {
//        StringBuilder sbSqlLiveBill = new StringBuilder();
//        StringBuilder sbSqlQFileBill = new StringBuilder();
//        StringBuilder sbSqlFilters = new StringBuilder();
//        try {
//            reportName = "Customer Wise Bill Sales";
//            sql = "";
//            exportFormName = "CustomerWiseBillSales";
//            fromDate = funGetFromDate();
//            toDate = funGetToDate();
//
//            String DateFrom = null, field = null, DateTo = null;
//            if (funGetFromDate() != null) {
//                DateFrom = fromDate;
//                field = "dteBillDate";
//            } else {
//                DateFrom = fromDate;
//                field = "date(dteBillDate)";
//            }
//            if (funGetToDate() != null) {
//                DateTo = toDate;
//            } else {
//                DateTo = toDate;
//            }
//
//            dm = new DefaultTableModel() {
//                @Override
//                public boolean isCellEditable(int row, int column) {
//                    //all cells false
//                    return false;
//                }
//            };
//
////            dm.addColumn("Customer Code");
////            dm.addColumn("Customer Name");
//            dm.addColumn("Bill No");
//            dm.addColumn("Sales Amount");
//
//            vSalesReportExcelColLength = new java.util.Vector();
//            vSalesReportExcelColLength.add("6#Left");
//            vSalesReportExcelColLength.add("6#Left");
////            vSalesReportExcelColLength.add("6#Left");
////            vSalesReportExcelColLength.add("10#Left");
//
//            DefaultTableModel dm1 = new DefaultTableModel();
//
//            dm1.addColumn("");
//            dm1.addColumn("Sales Amount");
//            tblTotal6.setModel(dm1);
//
//            records = new Object[2];
//            String pos = funGetSelectedPosCode();
//
//            sbSqlLiveBill.setLength(0);
//            sbSqlQFileBill.setLength(0);
//            sbSqlFilters.setLength(0);
//
//            sbSqlLiveBill.append("select b.strCustomerCode,b.strCustomerName "
//                    + " ,a.strBillNo,sum(a.dblGrandTotal),'" + clsGlobalVarClass.gUserCode + "' "
//                    + " from tblbillhd a,tblcustomermaster b "
//                    + " where a.strCustomerCode=b.strCustomerCode and a.strCustomerCode='" + tctCustomerCode.getText().trim() + "' "
//                    + " and date(a.dteBillDate) between '" + DateFrom + "' and '" + DateTo + "'");
//
//            sbSqlQFileBill.append("select b.strCustomerCode,b.strCustomerName "
//                    + " ,a.strBillNo,sum(a.dblGrandTotal),'" + clsGlobalVarClass.gUserCode + "' "
//                    + " from tblqbillhd a,tblcustomermaster b "
//                    + " where a.strCustomerCode=b.strCustomerCode and a.strCustomerCode='" + tctCustomerCode.getText().trim() + "' "
//                    + " and date(a.dteBillDate) between '" + DateFrom + "' and '" + DateTo + "'");
//
//            if (!pos.equals("All")) {
//                sbSqlFilters.append(" and a.strPOSCode='" + pos + "' ");
//            }
//
//            sbSqlFilters.append(" group by a.strBillNo");
//
//            boolean flgRecords = false;
//            double grandTotal = 0;
//            double totalSettleAmt = 0;
//
//            sbSqlLiveBill.append(sbSqlFilters);
//            sbSqlQFileBill.append(sbSqlFilters);
//            //System.out.println(sbSqlLiveBill);
//            //System.out.println(sbSqlQFileBill);
//
////            clsSalesFlashReport obj = new clsSalesFlashReport();
////            obj.funProcessSalesFlashReport(sbSqlLiveBill.toString(), sbSqlQFileBill.toString(), "CustWiseBillSales");
////
////            sql = "select * from tbltempsalesflash1 where strUser='" + clsGlobalVarClass.gUserCode + "'";
////            ResultSet rsCustomerWise = clsGlobalVarClass.dbMysql.executeResultSet(sql);
////            while (rsCustomerWise.next())
////            {
////                flgRecords = true;
//            //records[0] = rsCustomerWise.getString(1);//Cust Code
//            // records[1] = rsCustomerWise.getString(2);//Cust Name
//            List<clsBillItemDtlBean> listOfBillData = new ArrayList<clsBillItemDtlBean>();
//            Map mapMultiSettleBills = new HashMap();
//
//            ResultSet rsData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLiveBill.toString());
//            while (rsData.next()) {
//                clsBillItemDtlBean obj = new clsBillItemDtlBean();
//
//                obj.setStrCustomerCode(rsData.getString(1));
//                obj.setStrCustomerName(rsData.getString(2));
//                obj.setStrBillNo(rsData.getString(3));
//                obj.setDblGrandTotal(rsData.getDouble(4));
//                listOfBillData.add(obj);
//                totalSettleAmt += obj.getDblGrandTotal(); // Grand Total     
//            }
//            ResultSet rsData1 = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFileBill.toString());
//            while (rsData1.next()) {
//                clsBillItemDtlBean obj = new clsBillItemDtlBean();
//
//                obj.setStrCustomerCode(rsData.getString(1));
//                obj.setStrCustomerName(rsData.getString(2));
//                obj.setStrBillNo(rsData.getString(3));
//                obj.setDblGrandTotal(rsData.getDouble(4));
//
//                listOfBillData.add(obj);
//                totalSettleAmt += obj.getDblGrandTotal(); // Grand Total       
//            }
//
//            for (int i = 0; i < listOfBillData.size(); i++) {
//                clsBillItemDtlBean obj = listOfBillData.get(i);
//                records[0] = obj.getStrBillNo();
////                records[1]=obj.getStrCustomerName();
////                records[2]=obj.getStrBillNo();
//                records[1] = obj.getDblGrandTotal();
//                //records[2]=obj.getDteBillDate();
//
//                dm.addRow(records);
//            }
//            rsData.close();
//            rsData1.close();
//
//            Object[] ob1
//                    = {
//                        "Total", decimalFormat.format(totalSettleAmt)
//                    };
//            dm1.addRow(ob1);
//
//            tblTotal6.setModel(dm1);
//            tblTotal6.setRowHeight(40);
//
//            tblCustomerWise.setModel(dm);
//            tblCustomerWise.setRowHeight(25);
//
////            DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
////            rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
////            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
////            centerRenderer.setHorizontalAlignment(JLabel.CENTER);
////
////            tblCustomerWise.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
////            tblCustomerWise.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
////            tblCustomerWise.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
////            tblCustomerWise.getColumnModel().getColumn(0).setPreferredWidth(200);
////            tblCustomerWise.getColumnModel().getColumn(1).setPreferredWidth(220);
////            tblCustomerWise.getColumnModel().getColumn(2).setPreferredWidth(196);
////            tblCustomerWise.getColumnModel().getColumn(3).setPreferredWidth(196);
//            DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
//            rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
//            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
//            centerRenderer.setHorizontalAlignment(JLabel.CENTER);
//
//            tblCustomerWise.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
////            tblCustomerWise.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
////            tblCustomerWise.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
//            tblCustomerWise.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
//
//            tblCustomerWise.getColumnModel().getColumn(0).setPreferredWidth(200);
//
//            tblTotal6.setSize(400, 400);
//            DefaultTableCellRenderer rightRenderer1 = new DefaultTableCellRenderer();
//            rightRenderer1.setHorizontalAlignment(JLabel.RIGHT);
//            tblTotal6.getColumnModel().getColumn(1).setCellRenderer(rightRenderer1);
//            tblTotal6.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//            tblTotal6.getColumnModel().getColumn(0).setPreferredWidth(400);
//            tblTotal6.getColumnModel().getColumn(1).setPreferredWidth(430);
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            sbSqlLiveBill = null;
//            sbSqlQFileBill = null;
//            sbSqlFilters = null;
//        }
//    }
    /**
     * Customer Wise Sales
     */
    private void funCustomerWiseBillSales()
    {
	StringBuilder sbSqlLiveBill = new StringBuilder();
	StringBuilder sbSqlQFileBill = new StringBuilder();
	StringBuilder sbSqlFilters = new StringBuilder();
	try
	{
	    reportName = "Customer Wise Sales";
	    sql = "";
	    exportFormName = "CustomerWiseSales";
	    fromDate = funGetFromDate();
	    toDate = funGetToDate();

	    String custCode = tctCustomerCode.getText();

	    String DateFrom = null, DateTo = null;
	    if (funGetFromDate() != null)
	    {
		DateFrom = fromDate;
	    }
	    else
	    {
		DateFrom = fromDate;
	    }
	    if (funGetToDate() != null)
	    {
		DateTo = toDate;
	    }
	    else
	    {
		DateTo = toDate;
	    }

	    records = new Object[15];
	    String pos = funGetSelectedPosCode();

	    sbSqlLiveBill.setLength(0);
	    sbSqlQFileBill.setLength(0);
	    sbSqlFilters.setLength(0);
	    if (!custCode.equalsIgnoreCase(""))
	    {
		dmSales = new DefaultTableModel();

		dmSales.addColumn("Bill No");
		dmSales.addColumn("Bill Date");
		dmSales.addColumn("Bill Time");
		dmSales.addColumn("POS Name");
		dmSales.addColumn("Pay Mode");
		dmSales.addColumn("Sub Total");
		dmSales.addColumn("Disc %");
		dmSales.addColumn("Disc Amt");
		dmSales.addColumn("TAX Amt");
		dmSales.addColumn("Sales Amount");
		dmSales.addColumn("Remarks");
		dmSales.addColumn("Tip");
		dmSales.addColumn("Discount Remarks");
		dmSales.addColumn("Reason");

		vSalesReportExcelColLength = new java.util.Vector();
		vSalesReportExcelColLength.add("6#Left");
		vSalesReportExcelColLength.add("6#Left");
		vSalesReportExcelColLength.add("6#Left");
		vSalesReportExcelColLength.add("6#Left");
		vSalesReportExcelColLength.add("6#Left");
		vSalesReportExcelColLength.add("6#Left");
		vSalesReportExcelColLength.add("6#Left");
		vSalesReportExcelColLength.add("6#Left");
		vSalesReportExcelColLength.add("6#Left");
		vSalesReportExcelColLength.add("6#Left");
		vSalesReportExcelColLength.add("6#Left");
		vSalesReportExcelColLength.add("6#Left");
		vSalesReportExcelColLength.add("6#Left");
		vSalesReportExcelColLength.add("6#Left");
		vSalesReportExcelColLength.add("6#Left");

		dm1 = new DefaultTableModel();
		dm1.addColumn("");
		dm1.addColumn("SubTotal");
		dm1.addColumn("");
		dm1.addColumn("Disc");
		dm1.addColumn("Tax Total");
		dm1.addColumn("Sales Amount");
		dm1.addColumn("Tip Amount");
		tblTotal6.setModel(dm1);

		sbSqlLiveBill.append("select a.strBillNo,DATE_FORMAT(a.dteBillDate, '%d-%m-%y'),left(right(a.dteDateCreated,8),5) as BillTime"
			+ " ,f.strPOSName"
			+ ", ifnull(d.strSettelmentDesc,'') as payMode"
			+ " ,ifnull(a.dblSubTotal,0.00),IFNULL(a.dblDiscountPer,0), IFNULL(a.dblDiscountAmt,0.00),a.dblTaxAmt"
			+ " ,ifnull(c.dblSettlementAmt,0.00)"
			+ " ,ifnull(c.strRemark,'')"
			+ " ,a.dblTipAmount,a.strDiscountRemark,ifnull(h.strReasonName ,'NA') "
			+ " from tblbillhd  a "
			+ " left outer join tblposmaster f on a.strPOSCode=f.strPOSCode "
			+ " left outer join tblbillsettlementdtl c on a.strBillNo=c.strBillNo and a.strClientCode=c.strClientCode "
			+ " left outer join tblsettelmenthd d on c.strSettlementCode=d.strSettelmentCode "
			+ " left outer join tblcustomermaster e on a.strCustomerCode=e.strCustomerCode "
			+ " left outer join tblreasonmaster h on a.strReasonCode=h.strReasonCode "
			+ " where date(a.dteBillDate) between '" + DateFrom + "' and '" + DateTo + "'"
			+ " AND a.strCustomerCode='" + custCode + "'");

		sbSqlQFileBill.append("select a.strBillNo,DATE_FORMAT(a.dteBillDate, '%d-%m-%y'),left(right(a.dteDateCreated,8),5) as BillTime"
			+ " ,f.strPOSName"
			+ ", ifnull(d.strSettelmentDesc,'') as payMode"
			+ " ,ifnull(a.dblSubTotal,0.00),IFNULL(a.dblDiscountPer,0), IFNULL(a.dblDiscountAmt,0.00),a.dblTaxAmt"
			+ " ,ifnull(c.dblSettlementAmt,0.00)"
			+ " ,ifnull(c.strRemark,'')"
			+ " ,a.dblTipAmount,a.strDiscountRemark,ifnull(h.strReasonName ,'NA') "
			+ " from tblqbillhd a "
			+ " left outer join tblposmaster f on a.strPOSCode=f.strPOSCode "
			+ " left outer join tblqbillsettlementdtl c on a.strBillNo=c.strBillNo and a.strClientCode=c.strClientCode "
			+ " left outer join tblsettelmenthd d on c.strSettlementCode=d.strSettelmentCode "
			+ " left outer join tblcustomermaster e on a.strCustomerCode=e.strCustomerCode "
			+ " left outer join tblreasonmaster h on a.strReasonCode=h.strReasonCode "
			+ " where date(a.dteBillDate) between '" + DateFrom + "' and '" + DateTo + "'"
			+ " AND a.strCustomerCode='" + custCode + "'");

		if (!pos.equals("All"))
		{
		    sbSqlFilters.append(" and a.strPOSCode='" + pos + "' ");
		}

		// sbSqlFilters.append(" GROUP BY b.strCustomerCode");
		sbSqlFilters.append(" order by a.strBillNo desc ");
		boolean flgRecords = false;
		double grandTotal = 0;
		double totalDiscAmt = 0, totalSubTotal = 0, totalTaxAmt = 0, totalSettleAmt = 0, totalTipAmt = 0;

		sbSqlLiveBill.append(sbSqlFilters);
		sbSqlQFileBill.append(sbSqlFilters);

		List<clsBillItemDtlBean> listOfBillData = new ArrayList<clsBillItemDtlBean>();
		Map mapMultiSettleBills = new HashMap();

		ResultSet rsData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLiveBill.toString());
		while (rsData.next())
		{
		    clsBillItemDtlBean obj = new clsBillItemDtlBean();

		    obj.setStrBillNo(rsData.getString(1));
		    obj.setDteBillDate(rsData.getString(2));
		    obj.setDteDateCreated(rsData.getString(3));
		    obj.setStrPosName(rsData.getString(4));
		    obj.setStrSettelmentDesc(rsData.getString(5));
		    obj.setDblSubTotal(rsData.getDouble(6));
		    obj.setDblDiscountPer(rsData.getDouble(7));
		    obj.setDblDiscountAmt(rsData.getDouble(8));
		    obj.setDblTaxAmt(rsData.getDouble(9));
		    obj.setDblSettlementAmt(rsData.getDouble(10));
		    obj.setStrRemark(rsData.getString(11));
		    obj.setDblTipAmount(rsData.getDouble(12));
		    obj.setStrDiscountRemark(rsData.getString(13));
		    obj.setStrReasonName(rsData.getString(14));

		    listOfBillData.add(obj);

		    totalDiscAmt += obj.getDblDiscountAmt();
		    totalSubTotal += obj.getDblSubTotal();
		    totalTaxAmt += obj.getDblTaxAmt();
		    totalSettleAmt += obj.getDblSettlementAmt(); // Grand Total                
		    totalTipAmt += obj.getDblTipAmount(); // tip Amt     

		}
		rsData.close();

		ResultSet rsData1 = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFileBill.toString());
		while (rsData1.next())
		{
		    clsBillItemDtlBean obj = new clsBillItemDtlBean();

		    obj.setStrBillNo(rsData1.getString(1));
		    obj.setDteBillDate(rsData1.getString(2));
		    obj.setDteDateCreated(rsData1.getString(3));
		    obj.setStrPosName(rsData1.getString(4));
		    obj.setStrSettelmentDesc(rsData1.getString(5));
		    obj.setDblSubTotal(rsData1.getDouble(6));
		    obj.setDblDiscountPer(rsData1.getDouble(7));
		    obj.setDblDiscountAmt(rsData1.getDouble(8));
		    obj.setDblTaxAmt(rsData1.getDouble(9));
		    obj.setDblSettlementAmt(rsData1.getDouble(10));
		    obj.setStrRemark(rsData1.getString(11));
		    obj.setDblTipAmount(rsData1.getDouble(12));
		    obj.setStrDiscountRemark(rsData1.getString(13));
		    obj.setStrReasonName(rsData1.getString(14));

		    listOfBillData.add(obj);

		    totalDiscAmt += obj.getDblDiscountAmt();
		    totalSubTotal += obj.getDblSubTotal();
		    totalTaxAmt += obj.getDblTaxAmt();
		    totalSettleAmt += obj.getDblSettlementAmt(); // Grand Total                
		    totalTipAmt += obj.getDblTipAmount(); // tip Amt     
		}
		rsData1.close();
		for (int i = 0; i < listOfBillData.size(); i++)
		{
		    clsBillItemDtlBean obj = listOfBillData.get(i);

		    records[0] = obj.getStrBillNo();
		    records[1] = obj.getDteBillDate();
		    records[2] = obj.getDteDateCreated();
		    records[3] = obj.getStrPosName();
		    records[4] = obj.getStrSettelmentDesc();
		    records[5] = obj.getDblSubTotal();
		    records[6] = obj.getDblDiscountPer();
		    records[7] = obj.getDblDiscountAmt();
		    records[8] = obj.getDblTaxAmt();
		    records[9] = obj.getDblSettlementAmt();
		    records[10] = obj.getStrRemark();
		    records[11] = obj.getDblTipAmount();
		    records[12] = obj.getStrDiscountRemark();
		    records[13] = obj.getStrReasonName();

		    dmSales.addRow(records);
		}

		if (!flgRecords)
		{
		    dm.setRowCount(0);

		}

		Object[] ob1
			=
			{
			    "Total", gDecimalFormat.format(totalSubTotal), "", gDecimalFormat.format(totalDiscAmt), gDecimalFormat.format(totalTaxAmt), gDecimalFormat.format(totalSettleAmt), gDecimalFormat.format(totalTipAmt)
			};
		dm1.addRow(ob1);

		tblCustomerWise.setSize(400, 400);
		tblCustomerWise.setRowHeight(25);
		tblCustomerWise.setModel(dmSales);
		tblTotal6.setModel(dm1);
		tblTotal6.setRowHeight(40);

		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);

		tblCustomerWise.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
		tblCustomerWise.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
		tblCustomerWise.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);
		tblCustomerWise.getColumnModel().getColumn(7).setCellRenderer(rightRenderer);
		tblCustomerWise.getColumnModel().getColumn(8).setCellRenderer(rightRenderer);
		tblCustomerWise.getColumnModel().getColumn(9).setCellRenderer(rightRenderer);

		tblCustomerWise.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		tblCustomerWise.getColumnModel().getColumn(0).setPreferredWidth(80);
		tblCustomerWise.getColumnModel().getColumn(1).setPreferredWidth(80);
		tblCustomerWise.getColumnModel().getColumn(2).setPreferredWidth(60);
		tblCustomerWise.getColumnModel().getColumn(3).setPreferredWidth(100);
		tblCustomerWise.getColumnModel().getColumn(4).setPreferredWidth(70);
		tblCustomerWise.getColumnModel().getColumn(5).setPreferredWidth(60);
		tblCustomerWise.getColumnModel().getColumn(6).setPreferredWidth(75);
		tblCustomerWise.getColumnModel().getColumn(7).setPreferredWidth(60);
		tblCustomerWise.getColumnModel().getColumn(8).setPreferredWidth(75);
		tblCustomerWise.getColumnModel().getColumn(9).setPreferredWidth(100);
		tblCustomerWise.getColumnModel().getColumn(10).setPreferredWidth(100);
		tblCustomerWise.getColumnModel().getColumn(11).setPreferredWidth(60);
		tblCustomerWise.getColumnModel().getColumn(12).setPreferredWidth(120);
		tblCustomerWise.getColumnModel().getColumn(13).setPreferredWidth(100);

		tblTotal6.setSize(400, 400);
		DefaultTableCellRenderer rightRenderer1 = new DefaultTableCellRenderer();
		rightRenderer1.setHorizontalAlignment(JLabel.RIGHT);
		tblTotal6.getColumnModel().getColumn(1).setCellRenderer(rightRenderer1);
		tblTotal6.getColumnModel().getColumn(3).setCellRenderer(rightRenderer1);
		tblTotal6.getColumnModel().getColumn(4).setCellRenderer(rightRenderer1);
		tblTotal6.getColumnModel().getColumn(5).setCellRenderer(rightRenderer1);
		tblTotal6.getColumnModel().getColumn(6).setCellRenderer(rightRenderer1);
		tblTotal6.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tblTotal6.getColumnModel().getColumn(0).setPreferredWidth(260);
		tblTotal6.getColumnModel().getColumn(1).setPreferredWidth(100);
		tblTotal6.getColumnModel().getColumn(3).setPreferredWidth(100);
		tblTotal6.getColumnModel().getColumn(4).setPreferredWidth(100);
		tblTotal6.getColumnModel().getColumn(5).setPreferredWidth(100);
		tblTotal6.getColumnModel().getColumn(6).setPreferredWidth(100);

	    }
	    else
	    {
		dmSales = new DefaultTableModel();

		dmSales.addColumn("Bill Number");
		dmSales.addColumn("Date");
		dmSales.addColumn("Time");
		dmSales.addColumn("Bill Amount");
		dmSales.addColumn("Contact No");
		dmSales.addColumn("Name");

		vSalesReportExcelColLength = new java.util.Vector();
		vSalesReportExcelColLength.add("6#Left");
		vSalesReportExcelColLength.add("6#Left");
		vSalesReportExcelColLength.add("6#Left");
		vSalesReportExcelColLength.add("6#Left");
		vSalesReportExcelColLength.add("6#Left");
		vSalesReportExcelColLength.add("6#Left");

		dm1 = new DefaultTableModel();
		dm1.addColumn("");
		dm1.addColumn("");
		dm1.addColumn("");
		dm1.addColumn("");
		dm1.addColumn("");
		dm1.addColumn("");
		tblTotal6.setModel(dm1);

		sbSqlLiveBill.append("select a.strBillNo,DATE_FORMAT(a.dteBillDate, '%d-%m-%y'),TIME_FORMAT(time(a.dteDateCreated),'%h:%i:%s') as BillTime"
			+ ",ifnull(a.dblGrandTotal,0),ifnull(e.longMobileNo,0),ifnull(e.strCustomerName,'') "
			+ " from tblbillhd  a "
			+ " ,tblposmaster f  "
			+ " , tblbillsettlementdtl c  "
			+ " , tblsettelmenthd d   "
			+ " , tblcustomermaster e   "			
			+ " where date(a.dteBillDate) between '" + DateFrom + "' and '" + DateTo + "' "
			+ "and  a.strPOSCode=f.strPOSCode "
			+ "and a.strBillNo=c.strBillNo and a.strClientCode=c.strClientCode  "
			+ "and c.strSettlementCode=d.strSettelmentCode "
			+ " and a.strCustomerCode=e.strCustomerCode ");

		sbSqlQFileBill.append("select a.strBillNo,DATE_FORMAT(a.dteBillDate, '%d-%m-%y'),TIME_FORMAT(time(a.dteDateCreated),'%h:%i:%s') as BillTime"
			+ ",ifnull(a.dblGrandTotal,0),ifnull(e.longMobileNo,0),ifnull(e.strCustomerName,'') "
			+ " from tblqbillhd  a "
			+ " ,tblposmaster f  "
			+ " , tblqbillsettlementdtl c  "
			+ " , tblsettelmenthd d   "
			+ " , tblcustomermaster e   "		
			+ " where date(a.dteBillDate) between '" + DateFrom + "' and '" + DateTo + "' "
			+ "and  a.strPOSCode=f.strPOSCode "
			+ "and a.strBillNo=c.strBillNo and a.strClientCode=c.strClientCode  "
			+ "and c.strSettlementCode=d.strSettelmentCode "
			+ " and a.strCustomerCode=e.strCustomerCode ");

		if (!pos.equals("All"))
		{
		    sbSqlFilters.append(" and a.strPOSCode='" + pos + "' ");
		}

		// sbSqlFilters.append(" GROUP BY b.strCustomerCode");
		sbSqlFilters.append(" order by a.strBillNo desc ");
		boolean flgRecords = false;
		double grandTotal = 0;
		double totalDiscAmt = 0, totalSubTotal = 0, totalTaxAmt = 0, totalSettleAmt = 0, totalTipAmt = 0;

		sbSqlLiveBill.append(sbSqlFilters);
		sbSqlQFileBill.append(sbSqlFilters);

		List<clsBillItemDtlBean> listOfBillData = new ArrayList<clsBillItemDtlBean>();
		Map mapMultiSettleBills = new HashMap();
		double totGrandTotal = 0.0;
		ResultSet rsData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLiveBill.toString());
		while (rsData.next())
		{
		    clsBillItemDtlBean obj = new clsBillItemDtlBean();

		    obj.setStrBillNo(rsData.getString(1));
		    obj.setDteBillDate(rsData.getString(2));
		    obj.setDteDateCreated(rsData.getString(3));
		    obj.setDblGrandTotal(Double.parseDouble(rsData.getString(4)));
		    obj.setLongMobileNo(rsData.getString(5));
		    obj.setStrCustomerName(rsData.getString(6));
		    listOfBillData.add(obj);
		    totGrandTotal += obj.getDblGrandTotal();
		}
		rsData.close();

		ResultSet rsData1 = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFileBill.toString());
		while (rsData1.next())
		{
		    clsBillItemDtlBean obj = new clsBillItemDtlBean();

		    obj.setStrBillNo(rsData1.getString(1));
		    obj.setDteBillDate(rsData1.getString(2));
		    obj.setDteDateCreated(rsData1.getString(3));
		    obj.setDblGrandTotal(Double.parseDouble(rsData1.getString(4)));
		    obj.setLongMobileNo(rsData1.getString(5));
		    obj.setStrCustomerName(rsData1.getString(6));
		    listOfBillData.add(obj);
		    totGrandTotal += obj.getDblGrandTotal();
		}
		rsData1.close();
		for (int i = 0; i < listOfBillData.size(); i++)
		{
		    clsBillItemDtlBean obj = listOfBillData.get(i);

		    records[0] = obj.getStrBillNo();
		    records[1] = obj.getDteBillDate();
		    records[2] = obj.getDteDateCreated();
		    records[3] = obj.getDblGrandTotal() + "  ";
		    records[4] = "  " + obj.getLongMobileNo();
		    records[5] = obj.getStrCustomerName();

		    dmSales.addRow(records);
		}

		if (!flgRecords)
		{
		    dm.setRowCount(0);

		}

		Object[] ob1
			=
			{
			    "Total", "", "", gDecimalFormat.format(totGrandTotal), "", ""
			};
		dm1.addRow(ob1);
	    }
	    tblCustomerWise.setSize(400, 400);
	    tblCustomerWise.setRowHeight(25);
	    tblCustomerWise.setModel(dmSales);
	    tblTotal6.setModel(dm1);
	    tblTotal6.setRowHeight(40);

	    DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
	    rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
	    DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
	    centerRenderer.setHorizontalAlignment(JLabel.CENTER);

	    tblCustomerWise.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);

	    tblCustomerWise.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

	    tblCustomerWise.getColumnModel().getColumn(0).setPreferredWidth(110);
	    tblCustomerWise.getColumnModel().getColumn(1).setPreferredWidth(110);
	    tblCustomerWise.getColumnModel().getColumn(2).setPreferredWidth(110);
	    tblCustomerWise.getColumnModel().getColumn(3).setPreferredWidth(150);
	    tblCustomerWise.getColumnModel().getColumn(4).setPreferredWidth(150);
	    tblCustomerWise.getColumnModel().getColumn(5).setPreferredWidth(210);

	    tblTotal6.setSize(400, 400);
	    DefaultTableCellRenderer rightRenderer1 = new DefaultTableCellRenderer();
	    rightRenderer1.setHorizontalAlignment(JLabel.RIGHT);
	    tblTotal6.getColumnModel().getColumn(3).setCellRenderer(rightRenderer1);
	    tblTotal6.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    tblTotal6.getColumnModel().getColumn(0).setPreferredWidth(110);
	    tblTotal6.getColumnModel().getColumn(1).setPreferredWidth(110);
	    tblTotal6.getColumnModel().getColumn(2).setPreferredWidth(110);
	    tblTotal6.getColumnModel().getColumn(3).setPreferredWidth(150);
	    tblTotal6.getColumnModel().getColumn(4).setPreferredWidth(150);
	    tblTotal6.getColumnModel().getColumn(5).setPreferredWidth(210);

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	finally
	{
	    sbSqlLiveBill = null;
	    sbSqlQFileBill = null;
	    sbSqlFilters = null;
	}
    }

    /**
     * Top Spender Details
     */
    private void funTopSpenderWiseSales()
    {
	StringBuilder sbSqlLiveBill = new StringBuilder();
	StringBuilder sbSqlQFileBill = new StringBuilder();
	StringBuilder sbSqlFilters = new StringBuilder();
	try
	{
	    reportName = "Top Spender Wise Sales";
	    sql = "";
	    exportFormName = "TopSpenderWiseSales";
	    fromDate = funGetFromDate();
	    toDate = funGetToDate();

	    String DateFrom = null, DateTo = null;
	    Double amt = 0.0d;
	    if (funGetFromDate() != null)
	    {
		DateFrom = fromDate;
	    }
	    else
	    {
		DateFrom = fromDate;
	    }
	    if (funGetToDate() != null)
	    {
		DateTo = toDate;
	    }
	    else
	    {
		DateTo = toDate;
	    }
	    amt = Double.parseDouble(txtAmount.getText());
	    String valueOfAmt = cmbAmount.getSelectedItem().toString();
	    dmSales = new DefaultTableModel();
	    dmSales.addColumn("Mobile Number");
	    dmSales.addColumn("Customer Name");
	    dmSales.addColumn("No Of Bills");
	    dmSales.addColumn("Sales Amount");

	    vSalesReportExcelColLength = new java.util.Vector();
	    vSalesReportExcelColLength.add("6#Left");
	    vSalesReportExcelColLength.add("6#Left");
	    vSalesReportExcelColLength.add("6#Left");
	    vSalesReportExcelColLength.add("6#Left");

	    dm1 = new DefaultTableModel();

	    dm1.addColumn("");
	    dm1.addColumn("Sales Amount");
	    tblTotal5.setModel(dm1);

	    records = new Object[4];
	    String pos = funGetSelectedPosCode();

	    sbSqlLiveBill.setLength(0);
	    sbSqlQFileBill.setLength(0);
	    sbSqlFilters.setLength(0);

	    sbSqlLiveBill.append("select longMobileNo,ifnull(b.strCustomerName,'ND')"
		    + ",ifnull(count(a.strBillNo),'0'),ifnull(sum(a.dblGrandTotal),'0.00'),'" + clsGlobalVarClass.gUserCode + "' "
		    + "from tblbillhd a,tblcustomermaster b "
		    + "where a.strCustomerCode=b.strCustomerCode "
		    + "and date(a.dteBillDate) between '" + DateFrom + "' and '" + DateTo + "'"
		    + "and a.dblGrandTotal " + valueOfAmt + " '" + amt + "'");

	    sbSqlQFileBill.append("select longMobileNo,ifnull(b.strCustomerName,'ND')"
		    + ",ifnull(count(a.strBillNo),'0'),ifnull(sum(a.dblGrandTotal),'0.00'),'" + clsGlobalVarClass.gUserCode + "' "
		    + "from tblqbillhd a,tblcustomermaster b "
		    + "where a.strCustomerCode=b.strCustomerCode "
		    + "and date(a.dteBillDate) between '" + DateFrom + "' and '" + DateTo + "'"
		    + "and a.dblGrandTotal " + valueOfAmt + " '" + amt + "'");

	    if (!pos.equals("All"))
	    {
		sbSqlFilters.append(" and a.strPOSCode='" + pos + "' ");
	    }

	    sbSqlFilters.append(" GROUP BY a.strBillNo");
	    sbSqlFilters.append(" order by a.strBillNo desc");
	    boolean flgRecords = false;
	    double grandTotal = 0;
	    double totalSettleAmt = 0;
	    sbSqlLiveBill.append(sbSqlFilters);
	    sbSqlQFileBill.append(sbSqlFilters);

	    //System.out.println(sbSqlLiveBill);
	    //System.out.println(sbSqlQFileBill);
//            clsSalesFlashReport obj = new clsSalesFlashReport();
//            obj.funProcessSalesFlashReport(sbSqlLiveBill.toString(), sbSqlQFileBill.toString(), "CustWiseBillSales");
//
//            int billCount = 0;
//            sql = "select * from tbltempsalesflash1 where strUser='" + clsGlobalVarClass.gUserCode + "'";
//            ResultSet rsCustomerWise = clsGlobalVarClass.dbMysql.executeResultSet(sql);
//            while (rsCustomerWise.next())
//            {
//                flgRecords = true;
	    List<clsBillItemDtlBean> listOfBillData = new ArrayList<clsBillItemDtlBean>();
	    Map mapMultiSettleBills = new HashMap();

	    ResultSet rsData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLiveBill.toString());
	    while (rsData.next())
	    {
		clsBillItemDtlBean obj = new clsBillItemDtlBean();

		obj.setLongMobileNo(rsData.getString(1));
		obj.setStrCustomerName(rsData.getString(2));
		obj.setStrBillNo(rsData.getString(3));
		obj.setDblGrandTotal(rsData.getDouble(4));
		listOfBillData.add(obj);
		totalSettleAmt += obj.getDblGrandTotal(); // Grand Total     
	    }
	    ResultSet rsData1 = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFileBill.toString());
	    while (rsData1.next())
	    {
		clsBillItemDtlBean obj = new clsBillItemDtlBean();

		obj.setLongMobileNo(rsData1.getString(1));
		obj.setStrCustomerName(rsData1.getString(2));
		obj.setStrBillNo(rsData1.getString(3));
		obj.setDblGrandTotal(rsData1.getDouble(4));

		listOfBillData.add(obj);
		totalSettleAmt += obj.getDblGrandTotal(); // Grand Total       
	    }

	    Collections.sort(listOfBillData, new Comparator<clsBillItemDtlBean>()
	    {

		@Override
		public int compare(clsBillItemDtlBean o1, clsBillItemDtlBean o2)
		{
		    return ((int) o2.getDblGrandTotal() - (int) o1.getDblGrandTotal());
		}
	    });

	    for (int i = 0; i < listOfBillData.size(); i++)
	    {
		clsBillItemDtlBean obj = listOfBillData.get(i);
		records[0] = obj.getLongMobileNo();
		records[1] = obj.getStrCustomerName();
		records[2] = obj.getStrBillNo();
		records[3] = obj.getDblGrandTotal();
		//records[2]=obj.getDteBillDate();

		dmSales.addRow(records);
	    }
	    rsData.close();
	    rsData1.close();

//                billCount += Integer.parseInt(records[1].toString());
//                grandTotal += Double.parseDouble(records[2].toString());
	    if (!flgRecords)
	    {
		dm.setRowCount(0);

	    }

	    Object[] ob1
		    =
		    {
			"Total", gDecimalFormat.format(totalSettleAmt)
		    };
	    dm1.addRow(ob1);

	    tblTopSpenders.setSize(400, 400);
	    tblTopSpenders.setRowHeight(25);
	    tblTopSpenders.setModel(dmSales);
	    tblTotal5.setModel(dm1);
	    tblTotal5.setRowHeight(40);

	    DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
	    rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
	    DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
	    centerRenderer.setHorizontalAlignment(JLabel.CENTER);

	    tblTopSpenders.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
	    tblTopSpenders.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
	    tblTopSpenders.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

	    tblTopSpenders.getColumnModel().getColumn(0).setPreferredWidth(90);
	    tblTopSpenders.getColumnModel().getColumn(1).setPreferredWidth(200);

	    tblTotal5.setSize(400, 400);
	    DefaultTableCellRenderer rightRenderer1 = new DefaultTableCellRenderer();
	    rightRenderer1.setHorizontalAlignment(JLabel.RIGHT);
	    tblTotal5.getColumnModel().getColumn(1).setCellRenderer(rightRenderer1);
	    tblTotal5.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    tblTotal5.getColumnModel().getColumn(0).setPreferredWidth(400);
	    tblTotal5.getColumnModel().getColumn(1).setPreferredWidth(434);

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	finally
	{
	    sbSqlLiveBill = null;
	    sbSqlQFileBill = null;
	    sbSqlFilters = null;
	}
    }

    /**
     * Non-Spender Details
     */
    private void funNonSpenderWiseSales()
    {
	StringBuilder sbSqlLiveBill = new StringBuilder();
	StringBuilder sbSqlQFileBill = new StringBuilder();
	StringBuilder sbSqlFilters = new StringBuilder();
	try
	{
	    reportName = "Non Spender Wise Sales";
	    sql = "";
	    exportFormName = "NonSpenderWiseSales";
	    fromDate = funGetFromDate();
	    toDate = funGetToDate();

	    String DateFrom = null, DateTo = null;
	    Double amt = 0.0d;
	    if (funGetFromDate() != null)
	    {
		DateFrom = fromDate;
	    }
	    else
	    {
		DateFrom = fromDate;
	    }
	    if (funGetToDate() != null)
	    {
		DateTo = toDate;
	    }
	    else
	    {
		DateTo = toDate;
	    }
	    // amt=Double.parseDouble(txtAmount.getText());
	    //String valueOfAmt=cmbAmount.getSelectedItem().toString();
	    dmSales = new DefaultTableModel();
	    dmSales.addColumn("Mobile Number");
	    dmSales.addColumn("Customer Name");
	    dmSales.addColumn("Last Transaction Date");
//            dmSales.addColumn("Last Transaction Date");

	    vSalesReportExcelColLength = new java.util.Vector();
	    vSalesReportExcelColLength.add("6#Left");
	    vSalesReportExcelColLength.add("6#Left");
	    vSalesReportExcelColLength.add("6#Left");
	    vSalesReportExcelColLength.add("6#Left");

	    records = new Object[3];
	    String pos = funGetSelectedPosCode();

	    sbSqlLiveBill.setLength(0);
	    sbSqlQFileBill.setLength(0);
	    sbSqlFilters.setLength(0);

//            sbSqlLiveBill.append("select longMobileNo,ifnull(b.strCustomerName,'ND')"
//                    + ",ifnull(count(a.strBillNo),'0'),ifnull(sum(a.dblGrandTotal),'0.00'),'" + clsGlobalVarClass.gUserCode + "' "
//                    + "from tblbillhd a,tblcustomermaster b "
//                    + "where a.strCustomerCode=b.strCustomerCode "
//                    + "and date(a.dteBillDate) between '" + DateFrom + "' and '" + DateTo + "'"
//                    + "and a.dblGrandTotal=0.00");
	    sbSqlLiveBill.append("SELECT longMobileNo, IFNULL(b.strCustomerName,'ND'), IFNULL(COUNT(a.strBillNo),'0'), IFNULL(SUM(a.dblGrandTotal),'0.00')"
		    + ",max(DATE_FORMAT(a.dteBillDate, '%d-%m-%y'))\n"
		    + "FROM tblbillhd a,tblcustomermaster b\n"
		    + "WHERE a.strCustomerCode=b.strCustomerCode "
		    + "AND DATE(a.dteBillDate) BETWEEN '" + DateFrom + "' AND '" + DateTo + "' "
		    + "AND a.dblGrandTotal=0.00");

//            sbSqlQFileBill.append("select longMobileNo,ifnull(b.strCustomerName,'ND')"
//                    + ",ifnull(count(a.strBillNo),'0'),ifnull(sum(a.dblGrandTotal),'0.00'),'" + clsGlobalVarClass.gUserCode + "' "
//                    + "from tblqbillhd a,tblcustomermaster b "
//                    + "where a.strCustomerCode=b.strCustomerCode "
//                    + "and date(a.dteBillDate) between '" + DateFrom + "' and '" + DateTo + "'"
//                    + "and a.dblGrandTotal='0.00'");
	    sbSqlQFileBill.append("SELECT longMobileNo, IFNULL(b.strCustomerName,'ND'), IFNULL(COUNT(a.strBillNo),'0'), IFNULL(SUM(a.dblGrandTotal),'0.00')"
		    + ",max(DATE_FORMAT(a.dteBillDate, '%d-%m-%y'))\n"
		    + "FROM tblqbillhd a,tblcustomermaster b\n"
		    + "WHERE a.strCustomerCode=b.strCustomerCode "
		    + "AND DATE(a.dteBillDate) BETWEEN '" + DateFrom + "' AND '" + DateTo + "' "
		    + "AND a.dblGrandTotal=0.00");

	    if (!pos.equals("All"))
	    {
		sbSqlFilters.append(" and a.strPOSCode='" + pos + "' ");
	    }

	    sbSqlFilters.append(" GROUP BY b.strCustomerCode");
	    sbSqlFilters.append(" order by DATE(a.dteBillDate) desc");
	    boolean flgRecords = false;
	    double grandTotal = 0;

	    sbSqlLiveBill.append(sbSqlFilters);
	    sbSqlQFileBill.append(sbSqlFilters);

	    //System.out.println(sbSqlLiveBill);
	    //System.out.println(sbSqlQFileBill);
//            clsSalesFlashReport obj = new clsSalesFlashReport();
//            obj.funProcessSalesFlashReport(sbSqlLiveBill.toString(), sbSqlQFileBill.toString(), "CustWiseBillSales");
//
//            int billCount = 0;
//            sql = "select * from tbltempsalesflash1 where strUser='" + clsGlobalVarClass.gUserCode + "'";
//            ResultSet rsCustomerWise = clsGlobalVarClass.dbMysql.executeResultSet(sql);
//            while (rsCustomerWise.next())
//            {
//                flgRecords = true;
	    List<clsBillItemDtlBean> listOfBillData = new ArrayList<clsBillItemDtlBean>();
	    Map mapMultiSettleBills = new HashMap();

	    ResultSet rsData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLiveBill.toString());
	    while (rsData.next())
	    {
		clsBillItemDtlBean obj = new clsBillItemDtlBean();

		obj.setLongMobileNo(rsData.getString(1));
		obj.setStrCustomerName(rsData.getString(2));
		obj.setStrBillNo(rsData.getString(3));
		obj.setDblGrandTotal(rsData.getDouble(4));
		obj.setDteBillDate(rsData.getString(5));

		listOfBillData.add(obj);
	    }
	    ResultSet rsData1 = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFileBill.toString());
	    while (rsData1.next())
	    {
		clsBillItemDtlBean obj = new clsBillItemDtlBean();

		obj.setLongMobileNo(rsData1.getString(1));
		obj.setStrCustomerName(rsData1.getString(2));
		obj.setStrBillNo(rsData1.getString(3));
		obj.setDblGrandTotal(rsData1.getDouble(4));
		obj.setDteBillDate(rsData1.getString(5));

		listOfBillData.add(obj);
	    }

	    for (int i = 0; i < listOfBillData.size(); i++)
	    {
		clsBillItemDtlBean obj = listOfBillData.get(i);
		records[0] = obj.getLongMobileNo();
		records[1] = obj.getStrCustomerName();
		//records[2]=obj.getStrBillNo();
		//records[3]=obj.getDblGrandTotal();
		records[2] = obj.getDteBillDate();

		dmSales.addRow(records);
	    }
	    rsData.close();
	    rsData1.close();

	    if (!flgRecords)
	    {
		dm.setRowCount(0);

	    }

	    tblNonTopSpenders.setSize(400, 400);
	    tblNonTopSpenders.setRowHeight(25);
	    tblNonTopSpenders.setModel(dmSales);

	    DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
	    rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
	    DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
	    centerRenderer.setHorizontalAlignment(JLabel.CENTER);

	    tblNonTopSpenders.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
	    //tblNonTopSpenders.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
	    tblNonTopSpenders.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

//            tblNonTopSpenders.getColumnModel().getColumn(0).setPreferredWidth(900);
	    tblNonTopSpenders.getColumnModel().getColumn(1).setPreferredWidth(200);
	    tblNonTopSpenders.getColumnModel().getColumn(0).setPreferredWidth(100);

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	finally
	{
	    sbSqlLiveBill = null;
	    sbSqlQFileBill = null;
	    sbSqlFilters = null;
	}
    }

    /**
     * Export Files
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
	    WritableFont cellFont = new WritableFont(WritableFont.COURIER, 14);
	    cellFont.setBoldStyle(WritableFont.BOLD);
	    WritableCellFormat cellFormat = new WritableCellFormat(cellFont);
	    WritableFont headerCellFont = new WritableFont(WritableFont.TIMES, 10);
	    headerCellFont.setBoldStyle(WritableFont.BOLD);
	    WritableCellFormat headerCell = new WritableCellFormat(headerCellFont);
	    TableModel model = table.getModel();
	    sheet1.addCell(new Label(0, 0, reportName));

	    String POSName = cmbPosCode.getSelectedItem().toString();
	    String strFromDate = ddMMyyyyDateFormate.format(dteFromDate.getDate());
	    String strToDate = ddMMyyyyDateFormate.format(dteToDate.getDate());

	    String customerName = txtCustomerName.getText();

	    Label l0 = new Label(1, 2, POSName, headerCell);
	    Label l1 = new Label(4, 2, strFromDate, headerCell);
	    Label l2 = new Label(7, 2, strToDate, headerCell);
	    Label l00 = new Label(0, 2, "POSName:", headerCell);
	    Label l01 = new Label(3, 2, "From Date:", headerCell);
	    Label l02 = new Label(6, 2, "To Date:", headerCell);

	    sheet1.addCell(l00);
	    sheet1.addCell(l01);
	    sheet1.addCell(l02);
	    sheet1.addCell(l0);
	    sheet1.addCell(l1);
	    sheet1.addCell(l2);
	    if (panelDeliveryCharges.getSelectedIndex() == 0)
	    {
		Label c00 = new Label(0, 1, "Customer Name:", headerCell);
		Label c0 = new Label(1, 1, customerName, headerCell);
		sheet1.addCell(c0);
		sheet1.addCell(c00);
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
			Label row = new Label(j, i + 1, model.getValueAt(k, j).toString());
			sheet1.setColumnView(j, model.getValueAt(k, j).toString().length() + colLen);
			sheet1.addCell(row);
		    }
		    k++;
		}
	    }
	    if (panelDeliveryCharges.getSelectedIndex() == 1)
	    {

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
			Label row = new Label(j, i + 1, model.getValueAt(k, j).toString());
			sheet1.setColumnView(j, model.getValueAt(k, j).toString().length() + colLen);
			sheet1.addCell(row);
		    }
		    k++;
		}
	    }
	    if (panelDeliveryCharges.getSelectedIndex() == 2)
	    {
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
			Label row = new Label(j, i + 1, model.getValueAt(k, j).toString());
			sheet1.setColumnView(j, model.getValueAt(k, j).toString().length() + colLen);
			sheet1.addCell(row);
		    }
		    k++;
		}
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

    /**
     * Add Last Of Export Report
     *
     * @param workbook1
     */
    private void funAddLastOfExportReport(WritableWorkbook workbook1)
    {
	try
	{
	    int i = 0, j = 0, LastIndexReport = 0;

	    if (exportFormName.equals("Top Spender Wise"))
	    {
		LastIndexReport = 0;
	    }
	    else if (exportFormName.equals("Non Spender Wise"))
	    {
		LastIndexReport = 1;
	    }

	    WritableSheet sheet2 = workbook1.getSheet(0);
	    int r = sheet2.getRows();
	    //System.out.println(r);
	    //System.out.println("Row Cnt="+tblTotal.getRowCount());
	    //System.out.println("Col Cnt="+tblTotal.getColumnCount());

	    if (panelDeliveryCharges.getSelectedIndex() == 0)
	    {
		for (i = r; i < tblTotal6.getRowCount() + r; i++)
		{
		    for (j = 0; j < tblTotal6.getColumnCount(); j++)
		    {
			Label row = new Label(LastIndexReport + j, i + 1, tblTotal6.getValueAt(0, j).toString());
			sheet2.addCell(row);
		    }
		}
	    }

	    if (panelDeliveryCharges.getSelectedIndex() == 1)
	    {
		for (i = r; i < tblTotal5.getRowCount() + r; i++)
		{
		    for (j = 0; j < tblTotal5.getColumnCount(); j++)
		    {
			Label row = new Label(LastIndexReport + j, i + 1, tblTotal5.getValueAt(0, j).toString());
			sheet2.addCell(row);
		    }
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
}
