/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.POSReport.view;

import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsPosConfigFile;
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
import java.io.File;
import java.io.FileNotFoundException;

import java.sql.ResultSet;
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

/**
 *
 * @author sss11
 */
public class frmLoyaltyPointReport extends javax.swing.JFrame
{

    DefaultTableModel dm, dm1;
    private boolean editFlag;

    private String custCode;
    private Double dblTotalAmount, dblTotalPoints, dblTotalValue;
    clsUtility objUtility;
    StringBuilder sb = new StringBuilder();

    /**
     * Creates new form NewJFrame
     */
    public frmLoyaltyPointReport()
    {

	initComponents();
	objUtility = new clsUtility();
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

	/**
	 * this method is used for Component initialization
	 */
	funSetLookAndFeel();
	objUtility = new clsUtility();

	custCode = "";

	dteFromDate.setDate(objUtility.funGetDateToSetCalenderDate());
	dteToDate.setDate(objUtility.funGetDateToSetCalenderDate());
	editFlag = true;

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
     * this method is used for get report info in table format
     */
    private void funTableWise()
    {
	try
	{

	    dblTotalAmount = new Double("0.00");
	    dblTotalPoints = new Double("0.00");
	    dblTotalValue = new Double("0.00");

	    dm = new DefaultTableModel();
	    dm.addColumn("Bill No");
	    dm.addColumn("Customer Name");
	    dm.addColumn("Bill Date");
	    dm.addColumn("Amount ");
	    dm.addColumn("Points ");
	    dm.addColumn("Value ");

	    dm1 = new DefaultTableModel();
	    dm1.addColumn("");
	    dm1.addColumn("");
	    dm1.addColumn("");
	    dm1.addColumn("");
	    dm1.addColumn("");
	    dm1.addColumn("");

	    Object[] ob = new Object[6];
	    sb.setLength(0);
	    sb.append("select a.strBillNo,c.strCustomerName,date(b.dteBillDate),b.dblGrandTotal"
		    + ",a.dblPoints,a.dblValue "
		    + "from tblcrmpoints a, tblbillhd b,tblcustomermaster c "
		    + "where a.strBillNo=b.strBillNo and b.strCustomerCode=c.strCustomerCode ");
	    if (custCode.trim().length() > 0)
	    {
		sb.append("and  c.strCustomerCode ='" + custCode + "' ");
	    }
	    sb.append(" and date(b.dteBillDate) BETWEEN '" + objUtility.funGetDateInString() + "' and '" + objUtility.funGetDateInString() + "'");
	    ResultSet saleSet = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
	    while (saleSet.next())
	    {
		ob[0] = saleSet.getString(1);//billno
		ob[1] = saleSet.getString(2);//Customer Name
		ob[2] = saleSet.getString(3);//bill Date
		ob[3] = saleSet.getString(4);//Amount
		ob[4] = saleSet.getString(5);//Points
		ob[5] = saleSet.getString(6);//Value

		dblTotalAmount = dblTotalAmount + new Double(saleSet.getString(4));
		dblTotalPoints = dblTotalPoints + new Double(saleSet.getString(5));
		dblTotalValue = dblTotalValue + new Double(saleSet.getString(6));

		if (!ob[5].equals(0))
		{
		    dm.addRow(ob);
		}
	    }
	    Object[] ob1 =
	    {
		"TOTAL", "", "", Math.rint(dblTotalAmount), Math.rint(dblTotalPoints), Math.rint(dblTotalValue)
	    };
	    dm1.addRow(ob1);
	    tblLoyalityPoints.setModel(dm);
	    tblTotal.setModel(dm1);

	    DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
	    rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
	    DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
	    leftRenderer.setHorizontalAlignment(JLabel.LEFT);

	    tblLoyalityPoints.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);
	    tblLoyalityPoints.getColumnModel().getColumn(1).setCellRenderer(leftRenderer);
	    tblLoyalityPoints.getColumnModel().getColumn(2).setCellRenderer(leftRenderer);
	    tblLoyalityPoints.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
	    tblLoyalityPoints.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
	    tblLoyalityPoints.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);

	    tblLoyalityPoints.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    tblLoyalityPoints.getColumnModel().getColumn(0).setPreferredWidth(100);
	    tblLoyalityPoints.getColumnModel().getColumn(1).setPreferredWidth(310);
	    tblLoyalityPoints.getColumnModel().getColumn(2).setPreferredWidth(90);
	    tblLoyalityPoints.getColumnModel().getColumn(3).setPreferredWidth(80);
	    tblLoyalityPoints.getColumnModel().getColumn(4).setPreferredWidth(90);
	    tblLoyalityPoints.getColumnModel().getColumn(5).setPreferredWidth(105);
	    custCode = "";

	    DefaultTableCellRenderer rightRenderer1 = new DefaultTableCellRenderer();
	    rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
	    DefaultTableCellRenderer leftRenderer1 = new DefaultTableCellRenderer();
	    leftRenderer.setHorizontalAlignment(JLabel.LEFT);

	    tblTotal.getColumnModel().getColumn(0).setCellRenderer(leftRenderer1);
	    tblTotal.getColumnModel().getColumn(1).setCellRenderer(rightRenderer1);
	    tblTotal.getColumnModel().getColumn(2).setCellRenderer(rightRenderer1);
	    tblTotal.getColumnModel().getColumn(3).setCellRenderer(rightRenderer1);

	    tblTotal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    tblTotal.getColumnModel().getColumn(0).setPreferredWidth(100);
	    tblTotal.getColumnModel().getColumn(1).setPreferredWidth(310);
	    tblTotal.getColumnModel().getColumn(2).setPreferredWidth(90);
	    tblTotal.getColumnModel().getColumn(3).setPreferredWidth(80);
	    tblTotal.getColumnModel().getColumn(4).setPreferredWidth(90);
	    tblTotal.getColumnModel().getColumn(5).setPreferredWidth(105);

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    /**
     * this method is used to Export File
     *
     * @param table
     * @param file
     */

    void funExportFile(JTable table, File file)
    {
	try
	{
	    WritableWorkbook workbook1 = Workbook.createWorkbook(file);
	    WritableSheet sheet1 = workbook1.createSheet("First Sheet", 0);
	    TableModel model = table.getModel();
	    sheet1.addCell(new Label(3, 0, "Loyality Points Report"));
	    for (int i = 0; i < model.getColumnCount() + 1; i++)
	    {
		Label column = new Label(i, 1, model.getColumnName(i));
		sheet1.setColumnView(i, model.getColumnName(i).toString().length() + 10);
		sheet1.addCell(column);
	    }
	    int i = 0, j = 0;
	    int k = 0;

	    for (i = 3; i < model.getRowCount() + 3; i++)
	    {
		for (j = 0; j < model.getColumnCount(); j++)
		{
		    Label row = new Label(j, i + 1, model.getValueAt(k, j).toString());
		    sheet1.setColumnView(j, model.getValueAt(k, j).toString().length() + 10);
		    sheet1.addCell(row);
		}
		k++;
	    }
	    funAddLastOfExportReport(workbook1);
	    workbook1.write();
	    workbook1.close();
	    Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + clsPosConfigFile.exportReportPath + "/LoyalityPointsReport.xls");
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
     * this method add last export file Report
     *
     * @param workbook1
     */
    private void funAddLastOfExportReport(WritableWorkbook workbook1)
    {
	try
	{
	    int i = 0, j = 0, LastIndexReport = 0;
	    LastIndexReport = 0;
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

    /**
     * using this method execute export file process
     */

    private void funExportButtonPressed()
    {
	try
	{
	    File theDir = new File(clsPosConfigFile.exportReportPath);

	    File file = new File(clsPosConfigFile.exportReportPath + "/LoyalityPointsReport" + objUtility.funGetDateInString() + ".xls");
	    if (!theDir.exists())
	    {
		theDir.mkdir();
		//toExcel(tblSales,file);
		funExportFile(tblLoyalityPoints, file);
		//sendMail();
	    }
	    else
	    {
		//toExcel(tblSales,file);
		funExportFile(tblLoyalityPoints, file);
		//sendMail();
	    }
	}
	catch (Exception ex)
	{
	    ex.printStackTrace();
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
        pnlBackground = new JPanel() {
            public void paintComponent(Graphics g) {
                Image img = Toolkit.getDefaultToolkit().getImage(
                    getClass().getResource("/com/POSReport/images/imgBGJPOS.png"));
                g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), this);
            }
        };  ;
        pnlMain = new javax.swing.JPanel();
        lblFromDate = new javax.swing.JLabel();
        dteFromDate = new com.toedter.calendar.JDateChooser();
        lblToDate = new javax.swing.JLabel();
        dteToDate = new com.toedter.calendar.JDateChooser();
        pnlLoyalityPoints = new javax.swing.JScrollPane();
        tblLoyalityPoints = new javax.swing.JTable();
        btnClose = new javax.swing.JButton();
        btnView = new javax.swing.JButton();
        pnltotal = new javax.swing.JScrollPane();
        tblTotal = new javax.swing.JTable();
        lblCustName = new javax.swing.JLabel();
        txtCustomerName = new javax.swing.JTextField();
        btnExport = new javax.swing.JButton();

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
        lblProductName.setText("SPOS-");
        pnlheader.add(lblProductName);

        lblModuleName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblModuleName.setForeground(new java.awt.Color(255, 255, 255));
        pnlheader.add(lblModuleName);

        lblformName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblformName.setForeground(new java.awt.Color(255, 255, 255));
        lblformName.setText("-Loyalty Point of Sales");
        pnlheader.add(lblformName);
        pnlheader.add(filler4);
        pnlheader.add(filler5);

        lblPosName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblPosName.setForeground(new java.awt.Color(255, 255, 255));
        lblPosName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblPosName.setMaximumSize(new java.awt.Dimension(321, 30));
        lblPosName.setMinimumSize(new java.awt.Dimension(321, 30));
        lblPosName.setPreferredSize(new java.awt.Dimension(321, 30));
        pnlheader.add(lblPosName);
        pnlheader.add(filler6);

        lblUserCode.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblUserCode.setForeground(new java.awt.Color(255, 255, 255));
        lblUserCode.setMaximumSize(new java.awt.Dimension(90, 30));
        lblUserCode.setMinimumSize(new java.awt.Dimension(90, 30));
        lblUserCode.setPreferredSize(new java.awt.Dimension(90, 30));
        pnlheader.add(lblUserCode);

        lblDate.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblDate.setForeground(new java.awt.Color(255, 255, 255));
        lblDate.setMaximumSize(new java.awt.Dimension(192, 30));
        lblDate.setMinimumSize(new java.awt.Dimension(192, 30));
        lblDate.setPreferredSize(new java.awt.Dimension(192, 30));
        pnlheader.add(lblDate);

        lblHOSign.setMaximumSize(new java.awt.Dimension(34, 30));
        lblHOSign.setMinimumSize(new java.awt.Dimension(34, 30));
        lblHOSign.setPreferredSize(new java.awt.Dimension(34, 30));
        pnlheader.add(lblHOSign);

        getContentPane().add(pnlheader, java.awt.BorderLayout.PAGE_START);

        pnlBackground.setLayout(new java.awt.GridBagLayout());

        pnlMain.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, new java.awt.Color(204, 204, 204), new java.awt.Color(204, 204, 204), new java.awt.Color(153, 153, 153), new java.awt.Color(153, 153, 153)));
        pnlMain.setMinimumSize(new java.awt.Dimension(800, 570));
        pnlMain.setOpaque(false);

        lblFromDate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblFromDate.setText("From Date :");

        dteFromDate.setToolTipText("Select From Date");
        dteFromDate.setPreferredSize(new java.awt.Dimension(119, 35));
        dteFromDate.addHierarchyListener(new java.awt.event.HierarchyListener() {
            public void hierarchyChanged(java.awt.event.HierarchyEvent evt) {
                dteFromDateHierarchyChanged(evt);
            }
        });
        dteFromDate.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                dteFromDatePropertyChange(evt);
            }
        });

        lblToDate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblToDate.setText("To Date :");

        dteToDate.setToolTipText("Select To Date");
        dteToDate.setPreferredSize(new java.awt.Dimension(119, 35));
        dteToDate.addHierarchyListener(new java.awt.event.HierarchyListener() {
            public void hierarchyChanged(java.awt.event.HierarchyEvent evt) {
                dteToDateHierarchyChanged(evt);
            }
        });
        dteToDate.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                dteToDatePropertyChange(evt);
            }
        });

        tblLoyalityPoints.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        tblLoyalityPoints.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Bill No", "Customer Name", "Bill Date", "Bill Amount", "Points", "Value"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblLoyalityPoints.setMaximumSize(new java.awt.Dimension(2147483647, 100));
        tblLoyalityPoints.setMinimumSize(new java.awt.Dimension(105, 100));
        tblLoyalityPoints.setName(""); // NOI18N
        pnlLoyalityPoints.setViewportView(tblLoyalityPoints);

        btnClose.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnClose.setForeground(new java.awt.Color(255, 255, 255));
        btnClose.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnClose.setText("CLOSE");
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

        btnView.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnView.setForeground(new java.awt.Color(255, 255, 255));
        btnView.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnView.setText("VIEW");
        btnView.setToolTipText("View Report");
        btnView.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnView.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnViewMouseClicked(evt);
            }
        });

        tblTotal.setFont(new java.awt.Font("DejaVu Sans", 1, 14)); // NOI18N
        tblTotal.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "", "Total Amounts", "Total Points", "Total Value"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblTotal.setRowHeight(25);
        pnltotal.setViewportView(tblTotal);

        lblCustName.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblCustName.setText("Cust Name");

        txtCustomerName.setToolTipText("Select Customer");
        txtCustomerName.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                txtCustomerNameMouseClicked(evt);
            }
        });

        btnExport.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnExport.setForeground(new java.awt.Color(255, 255, 255));
        btnExport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnExport.setText("EXPORT");
        btnExport.setToolTipText("Export");
        btnExport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExport.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnExportMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout pnlMainLayout = new javax.swing.GroupLayout(pnlMain);
        pnlMain.setLayout(pnlMainLayout);
        pnlMainLayout.setHorizontalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlLoyalityPoints)
            .addComponent(pnltotal)
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dteFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnView, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnExport, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addComponent(lblToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dteToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblCustName)
                        .addGap(4, 4, 4)
                        .addComponent(txtCustomerName, javax.swing.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pnlMainLayout.setVerticalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(txtCustomerName)
                        .addGap(2, 2, 2))
                    .addComponent(lblFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dteFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dteToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCustName, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, Short.MAX_VALUE)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnView, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addComponent(btnExport, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(pnlLoyalityPoints, javax.swing.GroupLayout.PREFERRED_SIZE, 418, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnltotal, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pnlBackground.add(pnlMain, new java.awt.GridBagConstraints());

        getContentPane().add(pnlBackground, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void dteFromDateHierarchyChanged(java.awt.event.HierarchyEvent evt) {//GEN-FIRST:event_dteFromDateHierarchyChanged

    }//GEN-LAST:event_dteFromDateHierarchyChanged

    private void dteFromDatePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_dteFromDatePropertyChange

    }//GEN-LAST:event_dteFromDatePropertyChange

    private void dteToDateHierarchyChanged(java.awt.event.HierarchyEvent evt) {//GEN-FIRST:event_dteToDateHierarchyChanged

    }//GEN-LAST:event_dteToDateHierarchyChanged

    private void dteToDatePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_dteToDatePropertyChange

    }//GEN-LAST:event_dteToDatePropertyChange
    /**
     * Close window
     *
     * @param evt
     */
    private void btnCloseMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCloseMouseClicked
	funResetLookAndFeel();
	dispose();
	clsGlobalVarClass.hmActiveForms.remove("Loyalty Point Report");
    }//GEN-LAST:event_btnCloseMouseClicked

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("Loyalty Point Report");
    }//GEN-LAST:event_btnCloseActionPerformed
    /**
     * view report
     *
     * @param evt
     */
    private void btnViewMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnViewMouseClicked
	// TODO add your handling code here:
	funTableWise();

    }//GEN-LAST:event_btnViewMouseClicked
    /**
     * Set Data
     *
     * @param data
     */
    void setData1(Object[] data)
    {
	txtCustomerName.setText(data[1].toString());
	custCode = data[0].toString();
    }

    /**
     * customer Name
     *
     * @param evt
     */
    private void txtCustomerNameMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txtCustomerNameMouseClicked
	// TODO add your handling code here:
	try
	{
	    if (editFlag == true)
	    {

		//btnNew.setText("UPDATE");//UpdateD
		objUtility.funCallForSearchForm("CustomerMaster");
		new frmSearchFormDialog(this, true).setVisible(true);
		if (clsGlobalVarClass.gSearchItemClicked)
		{

		    Object[] data = clsGlobalVarClass.gArrListSearchData.toArray();
		    setData1(data);
		    clsGlobalVarClass.gSearchItemClicked = false;
		}

	    }
	    else
	    {
		new frmOkPopUp(this, "You dont have edit permission", "Error", 0).setVisible(true);
	    }

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }//GEN-LAST:event_txtCustomerNameMouseClicked
    /**
     * Export Button
     *
     * @param evt
     */
    private void btnExportMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnExportMouseClicked
	// TODO add your handling code here:
	funExportButtonPressed();
    }//GEN-LAST:event_btnExportMouseClicked

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("Loyalty Point Report");
    }//GEN-LAST:event_formWindowClosed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("Loyalty Point Report");
    }//GEN-LAST:event_formWindowClosing

    /**
     * @param args the command line arguments
     */
    public static void main(String args[])
    {
	/* Set the Nimbus look and feel */
	//<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
	/* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
	 */
	try
	{
	    for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
	    {
		if ("Nimbus".equals(info.getName()))
		{
		    javax.swing.UIManager.setLookAndFeel(info.getClassName());
		    break;
		}
	    }
	}
	catch (ClassNotFoundException ex)
	{
	    java.util.logging.Logger.getLogger(frmLoyaltyPointReport.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	}
	catch (InstantiationException ex)
	{
	    java.util.logging.Logger.getLogger(frmLoyaltyPointReport.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	}
	catch (IllegalAccessException ex)
	{
	    java.util.logging.Logger.getLogger(frmLoyaltyPointReport.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	}
	catch (javax.swing.UnsupportedLookAndFeelException ex)
	{
	    java.util.logging.Logger.getLogger(frmLoyaltyPointReport.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	}
	//</editor-fold>
	//</editor-fold>

	/* Create and display the form */
	java.awt.EventQueue.invokeLater(new Runnable()
	{
	    public void run()
	    {
		new frmLoyaltyPointReport().setVisible(true);
	    }
	});
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnExport;
    private javax.swing.JButton btnView;
    private com.toedter.calendar.JDateChooser dteFromDate;
    private com.toedter.calendar.JDateChooser dteToDate;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.JLabel lblCustName;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblFromDate;
    private javax.swing.JLabel lblHOSign;
    private javax.swing.JLabel lblModuleName;
    private javax.swing.JLabel lblPosName;
    private javax.swing.JLabel lblProductName;
    private javax.swing.JLabel lblToDate;
    private javax.swing.JLabel lblUserCode;
    private javax.swing.JLabel lblformName;
    private javax.swing.JPanel pnlBackground;
    private javax.swing.JScrollPane pnlLoyalityPoints;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JPanel pnlheader;
    private javax.swing.JScrollPane pnltotal;
    private javax.swing.JTable tblLoyalityPoints;
    private javax.swing.JTable tblTotal;
    private javax.swing.JTextField txtCustomerName;
    // End of variables declaration//GEN-END:variables

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
}
