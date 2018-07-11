package com.POSReport.view;

import com.POSGlobal.controller.clsBillHd;
import com.POSGlobal.controller.clsExportDocument;
import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsPosConfigFile;
import com.POSGlobal.controller.clsUtility;
import com.POSGlobal.view.frmOkPopUp;
import java.awt.Desktop;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
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

public class frmDayWiseSalesSummaryFlash extends javax.swing.JFrame
{

    private String fromDate;
    private String toDate, reportName;
    DefaultTableModel dm, totalDm;
    private String exportFormName, ExportReportPath;
    private java.util.Vector vSalesReportExcelColLength;
    private String selectedPOSCode;
    private String selectedPOSName;
    private StringBuilder sb = new StringBuilder();
    private clsUtility objUtility;
    private Map<String, String> mapSettlementNameCode;
    private Map<String, Double> mapSettlemetWiseAmt;
    private double totalSettleAmount = 0, totalGroupAmtTotal = 0, totalTaxAmount = 0, totalDiscAmtTotal = 0;
    private Map<String, String> mapAllTaxes;
    private Map<String, String> mapGroupNameCode;
    private HashMap<String, Double> mapDayGrandTotal;
    private HashMap<String, Double> mapDayNetTotal;
    private final DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();

    public frmDayWiseSalesSummaryFlash()
    {
	try
	{
	    objUtility = new clsUtility();
	    initComponents();

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

	    reportName = "DayWiseSalesSummaryFlash";
	    exportFormName = "DayWiseSalesSummaryFlash";
	    this.setLocationRelativeTo(null);
	    funFillPOSComboBox();

	    //load all POS
	    mapSettlementNameCode = new HashMap<String, String>();
	    mapGroupNameCode = new HashMap<String, String>();
	    funFillSettlementNameComboBox();
	    funFillGroupNameComboBox();

	    dteFromDate.setDate(objUtility.funGetDateToSetCalenderDate());
	    dteToDate.setDate(objUtility.funGetDateToSetCalenderDate());

	    ExportReportPath = clsPosConfigFile.exportReportPath;
	    vSalesReportExcelColLength = new java.util.Vector();
	    vSalesReportExcelColLength.add("10#Left"); //
	    vSalesReportExcelColLength.add("20#Left"); //
	    vSalesReportExcelColLength.add("10#Right"); //
	    vSalesReportExcelColLength.add("10#Right"); //
	    vSalesReportExcelColLength.add("10#Right");//
	    vSalesReportExcelColLength.add("10#Right");//
	    vSalesReportExcelColLength.add("10#Right");//
	    vSalesReportExcelColLength.add("10#Right");//
	    vSalesReportExcelColLength.add("10#Right");//
	    vSalesReportExcelColLength.add("10#Right");//
	    vSalesReportExcelColLength.add("10#Right");//
	    vSalesReportExcelColLength.add("10#Right");//
	    vSalesReportExcelColLength.add("10#Right");//
	    vSalesReportExcelColLength.add("10#Right"); //
	    vSalesReportExcelColLength.add("10#Right"); //
	    vSalesReportExcelColLength.add("10#Right");//
	    vSalesReportExcelColLength.add("10#Right");//
	    vSalesReportExcelColLength.add("10#Right");//
	    vSalesReportExcelColLength.add("10#Right");//
	    vSalesReportExcelColLength.add("10#Right");//
	    vSalesReportExcelColLength.add("10#Right");//
	    vSalesReportExcelColLength.add("10#Right");//
	    vSalesReportExcelColLength.add("10#Right");//
	    vSalesReportExcelColLength.add("10#Right");//
	    vSalesReportExcelColLength.add("10#Right");//
	    vSalesReportExcelColLength.add("10#Right");//
	    vSalesReportExcelColLength.add("10#Right"); //
	    vSalesReportExcelColLength.add("10#Right"); //
	    vSalesReportExcelColLength.add("10#Right");//
	    vSalesReportExcelColLength.add("10#Right");//
	    vSalesReportExcelColLength.add("10#Right");//
	    vSalesReportExcelColLength.add("10#Right");//
	    vSalesReportExcelColLength.add("10#Right");//
	    vSalesReportExcelColLength.add("10#Right");//
	    vSalesReportExcelColLength.add("10#Right");//
	    vSalesReportExcelColLength.add("10#Right");//
	    vSalesReportExcelColLength.add("10#Right");//

	    mapDayGrandTotal = new HashMap<String, Double>();
	    mapDayNetTotal = new HashMap<String, Double>();
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
     * this function is used POS Code ComboBoxs
     */
    private void funFillPOSComboBox()
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
		sb.append("select strPosName,strPosCode from tblposmaster order by strPosCode ");
		ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
		while (rs.next())
		{
		    cmbPosCode.addItem(rs.getString(1) + " " + rs.getString(2));
		}
		rs.close();
	    }

	}
	catch (Exception e)
	{
	    e.printStackTrace();
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
        tblDayWiseSalesSummary = new javax.swing.JTable();
        btnExecute = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();
        pnltotal = new javax.swing.JScrollPane();
        tblTotal = new javax.swing.JTable();
        btnExport = new javax.swing.JButton();
        cmbGroupWise = new javax.swing.JComboBox();
        lblPosCode1 = new javax.swing.JLabel();
        lblOperationType = new javax.swing.JLabel();
        cmbOperationType = new javax.swing.JComboBox();
        chkWithDiscount = new javax.swing.JCheckBox();
        lblSettlementName = new javax.swing.JLabel();
        cmbSettlementName = new javax.swing.JComboBox();
        lblGroupName = new javax.swing.JLabel();
        cmbGroupName = new javax.swing.JComboBox();

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
        lblformName.setText("-Day Wise Sales Summary Flash");
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
        lblPosCode.setText("POS Name :");

        cmbPosCode.setToolTipText("Select POS");

        lblFromDate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblFromDate.setText("From Date :");

        dteFromDate.setToolTipText("Select From Date");
        dteFromDate.setPreferredSize(new java.awt.Dimension(119, 35));

        lblToDate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblToDate.setText("To Date :");

        dteToDate.setToolTipText("Select To Date");
        dteToDate.setPreferredSize(new java.awt.Dimension(119, 35));

        tblDayWiseSalesSummary.setFont(new java.awt.Font("Microsoft Sans Serif", 0, 11)); // NOI18N
        tblDayWiseSalesSummary.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tblDayWiseSalesSummary.setRowHeight(25);
        pnlDayEnd.setViewportView(tblDayWiseSalesSummary);

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

        tblTotal.setFont(new java.awt.Font("Microsoft Sans Serif", 1, 12)); // NOI18N
        tblTotal.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                true, false, false, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblTotal.setRowHeight(30);
        pnltotal.setViewportView(tblTotal);

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

        cmbGroupWise.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ITEM'S GROUP WISE", "NONE" }));
        cmbGroupWise.setToolTipText("Select POS");

        lblPosCode1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblPosCode1.setText("View By :");

        lblOperationType.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblOperationType.setText("Operation Type :");

        cmbOperationType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "All", "DineIn", "DirectBiller", "HomeDelivery", "TakeAway" }));
        cmbOperationType.setToolTipText("Select POS");

        chkWithDiscount.setSelected(true);
        chkWithDiscount.setText("With Discount");
        chkWithDiscount.setOpaque(false);

        lblSettlementName.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblSettlementName.setText("Settlement Name:");

        cmbSettlementName.setToolTipText("Select POS");
        cmbSettlementName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbSettlementNameActionPerformed(evt);
            }
        });

        lblGroupName.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblGroupName.setText("Group Name:");

        cmbGroupName.setToolTipText("Select POS");
        cmbGroupName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbGroupNameActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlMainLayout = new javax.swing.GroupLayout(pnlMain);
        pnlMain.setLayout(pnlMainLayout);
        pnlMainLayout.setHorizontalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlDayEnd)
            .addComponent(pnltotal)
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addComponent(lblPosCode)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbPosCode, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblFromDate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dteFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dteToDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblPosCode1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbGroupWise, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chkWithDiscount))
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addComponent(lblSettlementName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbSettlementName, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblOperationType)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbOperationType, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblGroupName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbGroupName, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnExecute, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnExport, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        pnlMainLayout.setVerticalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblPosCode, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbPosCode, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(lblFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(dteFromDate, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                                .addComponent(lblToDate, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 27, Short.MAX_VALUE)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMainLayout.createSequentialGroup()
                                    .addComponent(dteToDate, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                                    .addGap(1, 1, 1)))
                            .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(cmbGroupWise, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblPosCode1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(chkWithDiscount, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                .addGap(10, 10, 10)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnExport, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnExecute, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblSettlementName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(cmbSettlementName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblOperationType, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(cmbOperationType, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblGroupName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(cmbGroupName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                .addComponent(pnlDayEnd, javax.swing.GroupLayout.PREFERRED_SIZE, 424, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnltotal, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pnlMainLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {cmbPosCode, lblFromDate, lblPosCode, lblToDate});

        pnlMainLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {dteFromDate, dteToDate});

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

	funResetFields();
	if (btnExecute.isEnabled())
	{
	    if (null == dteFromDate.getDate())
	    {
		new frmOkPopUp(this, "Please Enter Valid Date.", "Invalid Date", 1).setVisible(true);
		return;
	    }
	    if (null == dteToDate.getDate())
	    {
		new frmOkPopUp(this, "Please Enter Valid Date.", "Invalid Date", 1).setVisible(true);
		return;
	    }
	    if (dteFromDate.getDate().after(dteToDate.getDate()))
	    {
		new frmOkPopUp(this, "Please Enter Valid Date Range.", "Invalid Date", 1).setVisible(true);
		return;
	    }
	    btnExecute.setEnabled(false);
	    funExecuteButtonClicked();
	    btnExecute.setEnabled(true);

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
	clsGlobalVarClass.hmActiveForms.remove("DayWiseSalesSummaryFlash");
    }//GEN-LAST:event_btnCloseMouseClicked

    private void btnExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportActionPerformed

	try
	{
	    File theDir = new File(ExportReportPath);
	    File file = new File(ExportReportPath + File.separator + exportFormName + objUtility.funGetDateInString() + ".xls");
	    if (!theDir.exists())
	    {
		theDir.mkdir();
		funExportFile(tblDayWiseSalesSummary, file);
		//sendMail();
	    }
	    else
	    {
		funExportFile(tblDayWiseSalesSummary, file);
		//sendMail();
	    }
	}
	catch (Exception ex)
	{
	    ex.printStackTrace();
	}
    }//GEN-LAST:event_btnExportActionPerformed

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

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("DayWiseSalesSummaryFlash");
    }//GEN-LAST:event_btnCloseActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("DayWiseSalesSummaryFlash");
    }//GEN-LAST:event_formWindowClosed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("DayWiseSalesSummaryFlash");
    }//GEN-LAST:event_formWindowClosing

    private void cmbSettlementNameActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cmbSettlementNameActionPerformed
    {//GEN-HEADEREND:event_cmbSettlementNameActionPerformed
	// TODO add your handling code here:
    }//GEN-LAST:event_cmbSettlementNameActionPerformed

    private void cmbGroupNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbGroupNameActionPerformed
	// TODO add your handling code here:
    }//GEN-LAST:event_cmbGroupNameActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnExecute;
    private javax.swing.JButton btnExport;
    private javax.swing.JCheckBox chkWithDiscount;
    private javax.swing.JComboBox cmbGroupName;
    private javax.swing.JComboBox cmbGroupWise;
    private javax.swing.JComboBox cmbOperationType;
    private javax.swing.JComboBox cmbPosCode;
    private javax.swing.JComboBox cmbSettlementName;
    private com.toedter.calendar.JDateChooser dteFromDate;
    private com.toedter.calendar.JDateChooser dteToDate;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblFromDate;
    private javax.swing.JLabel lblGroupName;
    private javax.swing.JLabel lblHOSign;
    private javax.swing.JLabel lblModuleName;
    private javax.swing.JLabel lblOperationType;
    private javax.swing.JLabel lblPosCode;
    private javax.swing.JLabel lblPosCode1;
    private javax.swing.JLabel lblPosName;
    private javax.swing.JLabel lblProductName;
    private javax.swing.JLabel lblSettlementName;
    private javax.swing.JLabel lblToDate;
    private javax.swing.JLabel lblUserCode;
    private javax.swing.JLabel lblformName;
    private javax.swing.JPanel pnlBackGround;
    private javax.swing.JScrollPane pnlDayEnd;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JPanel pnlheader;
    private javax.swing.JScrollPane pnltotal;
    private javax.swing.JTable tblDayWiseSalesSummary;
    private javax.swing.JTable tblTotal;
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

    private void funExecuteButtonClicked()
    {
	try
	{
	    fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());

	    toDate = objUtility.funGetFromToDate(dteToDate.getDate());

	    String selectedPOS = cmbPosCode.getSelectedItem().toString();
	    if (selectedPOS.equalsIgnoreCase("All"))
	    {
		selectedPOSCode = selectedPOS;
		selectedPOSName = selectedPOS;
	    }
	    else
	    {
		int lastIndexOf = selectedPOS.lastIndexOf(" ");
		selectedPOSName = selectedPOS.substring(0, lastIndexOf - 1);
		selectedPOSCode = selectedPOS.substring(lastIndexOf + 1);
	    }

	    if (cmbGroupWise.getSelectedItem().toString().equalsIgnoreCase("ITEM'S GROUP WISE"))
	    {
		funFillTableForItemGroupWise();
	    }
	    else
	    {
		funFillTable();
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funFillTable()
    {
	try
	{
	    dm = new DefaultTableModel()
	    {
		@Override
		public boolean isCellEditable(int row, int column)
		{
		    return false;
		}
	    };

	    totalDm = new DefaultTableModel()
	    {
		@Override
		public boolean isCellEditable(int row, int column)
		{
		    return false;
		}
	    };
	    Vector<String> dateCol = new Vector<>();
	    Vector<String> posCol = new Vector<>();
	    Map<String, String> mapBillDate = new HashMap<>();
	    mapAllTaxes = new HashMap<>();
	    //fill Q Date and POS
	    sb.setLength(0);
	    sb.append("select DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y') "
		    + "from tblqbillhd a,tblqbillsettlementdtl b,tblsettelmenthd c "
		    + "where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		    + "and a.strBillNo=b.strBillNo "
		    + "and b.strSettlementCode=c.strSettelmentCode ");
	    if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append("and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ");
	    }
	    if (!cmbSettlementName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append(" and c.strSettelmentCode='" + mapSettlementNameCode.get(cmbSettlementName.getSelectedItem().toString()) + "' ");
	    }
	    sb.append("group by date(a.dteBillDate) "
		    + "order by date(a.dteBillDate); ");
	    ResultSet rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
	    while (rsSales.next())
	    {
		mapBillDate.put(rsSales.getString(1), rsSales.getString(1));
	    }
	    //fill Live Date and POS
	    sb.setLength(0);
	    sb.append("select DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y') "
		    + "from tblbillhd a,tblbillsettlementdtl b,tblsettelmenthd c "
		    + "where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		    + "and a.strBillNo=b.strBillNo "
		    + "and b.strSettlementCode=c.strSettelmentCode ");
	    if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append("and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ");
	    }
	    if (!cmbSettlementName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append(" and c.strSettelmentCode='" + mapSettlementNameCode.get(cmbSettlementName.getSelectedItem().toString()) + "' ");
	    }
	    sb.append("group by date(a.dteBillDate) "
		    + "order by date(a.dteBillDate); ");
	    rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
	    while (rsSales.next())
	    {
		mapBillDate.put(rsSales.getString(1), rsSales.getString(1));
	    }
	    Iterator<String> itBillDate = mapBillDate.keySet().iterator();
	    while (itBillDate.hasNext())
	    {
		String billDate = itBillDate.next();
		dateCol.add(billDate);
		posCol.add(selectedPOSName.toUpperCase());
	    }
	    //sorts date
	    Collections.sort(dateCol, new DateComparator());
	    dm.addColumn("DATE", dateCol);
	    dm.addColumn("POS", posCol);
	    totalDm.addColumn("TOTALS");
	    totalDm.addColumn("POS");

	    Map<String, String> mapSettlements = new HashMap<String, String>();
	    //fill Q settlement whoes amt>0    
	    sb.setLength(0);
	    sb.append("SELECT c.strSettelmentDesc "
		    + "FROM tblqbillhd a,tblqbillsettlementdtl b,tblsettelmenthd c "
		    + "WHERE a.strBillNo=b.strBillNo  "
		    + "AND b.strSettlementCode=c.strSettelmentCode  "
		    + "and b.dblSettlementAmt>0 "
		    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append("and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ");
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sb.append(" and a.strPOSCode='" + selectedPOSCode + "' ");
	    }
	    if (!cmbSettlementName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append(" and c.strSettelmentCode='" + mapSettlementNameCode.get(cmbSettlementName.getSelectedItem().toString()) + "' ");
	    }
	    sb.append("GROUP BY strSettelmentDesc "
		    + "ORDER BY strSettelmentDesc; ");
	    rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
	    while (rsSales.next())
	    {
		mapSettlements.put(rsSales.getString(1).toUpperCase(), rsSales.getString(1).toUpperCase());
	    }
	    rsSales.close();
	    //fill Live settlement whoes amt>0
	    sb.setLength(0);
	    sb.append("SELECT c.strSettelmentDesc "
		    + "FROM tblbillhd a,tblbillsettlementdtl b,tblsettelmenthd c "
		    + "WHERE a.strBillNo=b.strBillNo  "
		    + "AND b.strSettlementCode=c.strSettelmentCode  "
		    + "and b.dblSettlementAmt>0 "
		    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append("and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ");
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sb.append(" and a.strPOSCode='" + selectedPOSCode + "' ");
	    }
	    if (!cmbSettlementName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append(" and c.strSettelmentCode='" + mapSettlementNameCode.get(cmbSettlementName.getSelectedItem().toString()) + "' ");
	    }
	    sb.append("GROUP BY strSettelmentDesc "
		    + "ORDER BY strSettelmentDesc; ");
	    rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
	    while (rsSales.next())
	    {
		mapSettlements.put(rsSales.getString(1).toUpperCase(), rsSales.getString(1).toUpperCase());
	    }
	    rsSales.close();

	    Iterator<String> itSettlement = mapSettlements.keySet().iterator();
	    int cntArrLen = 0;
	    while (itSettlement.hasNext())
	    {
		String settleDisc = itSettlement.next();
		dm.addColumn(settleDisc);
		totalDm.addColumn(settleDisc);
		cntArrLen++;
	    }
	    //fill TAX
	    String taxCalType = "";
	    //fill TAX

	    ///live
	    sb.setLength(0);
	    sb.append("select distinct(a.strTaxCode),a.strTaxDesc,a.strTaxCalculation  "
		    + "from tbltaxhd a,tblbilltaxdtl b "
		    + "where a.strTaxCode=b.strTaxCode "
		    + "and date(b.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    ResultSet rsAllTaxes = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
	    while (rsAllTaxes.next())
	    {
		taxCalType = rsAllTaxes.getString(3).trim();
		mapAllTaxes.put(rsAllTaxes.getString(1), rsAllTaxes.getString(2));
	    }
	    rsAllTaxes.close();
	    ///Qfile
	    sb.setLength(0);
	    sb.append("select distinct(a.strTaxCode),a.strTaxDesc,a.strTaxCalculation  "
		    + "from tbltaxhd a,tblqbilltaxdtl b "
		    + "where a.strTaxCode=b.strTaxCode "
		    + "and date(b.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    rsAllTaxes = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
	    while (rsAllTaxes.next())
	    {
		taxCalType = rsAllTaxes.getString(3).trim();
		mapAllTaxes.put(rsAllTaxes.getString(1), rsAllTaxes.getString(2));
	    }
	    rsAllTaxes.close();

	    for (String taxDesc : mapAllTaxes.values())
	    {
		dm.addColumn(taxDesc.toUpperCase());
		totalDm.addColumn(taxDesc.toUpperCase());
		cntArrLen++;
	    }

	    cntArrLen = cntArrLen + 2;
	    Object[] arrObjRecords1 = new Object[cntArrLen];
	    arrObjRecords1[0] = "TOTALS";
	    arrObjRecords1[1] = selectedPOSName.toUpperCase();
	    for (int cnt = 2; cnt < cntArrLen; cnt++)
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

	    tblDayWiseSalesSummary.setRowHeight(25);
	    tblDayWiseSalesSummary.setModel(dm);
	    tblDayWiseSalesSummary.setAutoscrolls(true);
	    tblDayWiseSalesSummary.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

	    //fill Q Data     
	    sb.setLength(0);
	    sb.append("select DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y'),'" + selectedPOSName + "',c.strSettelmentDesc,sum(b.dblSettlementAmt) "
		    + "from tblqbillhd a,tblqbillsettlementdtl b,tblsettelmenthd c "
		    + "where a.strBillNo=b.strBillNo "
		    + "and b.strSettlementCode=c.strSettelmentCode "
		    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append("and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ");
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sb.append(" and a.strPOSCode='" + selectedPOSCode + "' ");
	    }
	    if (!cmbSettlementName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append(" and c.strSettelmentCode='" + mapSettlementNameCode.get(cmbSettlementName.getSelectedItem().toString()) + "' ");
	    }
	    sb.append("group by date(a.dteBillDate),c.strSettelmentDesc "
		    + "order by date(a.dteBillDate),c.strSettelmentDesc; ");

	    funFillSettlementData(sb);
	    sb.setLength(0);
	    sb.append("select DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y'),c.strTaxDesc,sum(b.dblTaxAmount) "
		    + "from tblqbillhd a,tblqbilltaxdtl b,tbltaxhd c,tblqbillsettlementdtl d,tblsettelmenthd e  "
		    + "where a.strBillNo=b.strBillNo "
		    + "and b.strTaxCode=c.strTaxCode "
		    + "and a.strBillNo=d.strBillNo "
		    + "and d.strSettlementCode=e.strSettelmentCode "
		    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append("and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ");
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sb.append(" and a.strPOSCode='" + selectedPOSCode + "' ");
	    }
	    if (!cmbSettlementName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append(" and e.strSettelmentCode='" + mapSettlementNameCode.get(cmbSettlementName.getSelectedItem().toString()) + "' ");
	    }
	    sb.append("group by date(a.dteBillDate),b.strTaxCode "
		    + "order by date(a.dteBillDate),b.strTaxCode; ");
	    funFillTaxData(sb);
	    //fill Q Data    

	    //fill Live Data
	    sb.setLength(0);
	    sb.append("select DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y'),'" + selectedPOSName + "',c.strSettelmentDesc,sum(b.dblSettlementAmt) "
		    + "from tblbillhd a,tblbillsettlementdtl b,tblsettelmenthd c "
		    + "where a.strBillNo=b.strBillNo "
		    + "and b.strSettlementCode=c.strSettelmentCode "
		    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append("and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ");
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sb.append(" and a.strPOSCode='" + selectedPOSCode + "' ");
	    }
	    if (!cmbSettlementName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append(" and c.strSettelmentCode='" + mapSettlementNameCode.get(cmbSettlementName.getSelectedItem().toString()) + "' ");
	    }
	    sb.append("group by date(a.dteBillDate),c.strSettelmentDesc "
		    + "order by date(a.dteBillDate),c.strSettelmentDesc; ");

	    funFillSettlementData(sb);
	    sb.setLength(0);
	    sb.append("select DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y'),c.strTaxDesc,sum(b.dblTaxAmount) "
		    + "from "
		    + "tblbillhd a,tblbilltaxdtl b,tbltaxhd c,tblbillsettlementdtl d,tblsettelmenthd e   "
		    + "where a.strBillNo=b.strBillNo "
		    + "and b.strTaxCode=c.strTaxCode "
		    + "and a.strBillNo=d.strBillNo "
		    + "and d.strSettlementCode=e.strSettelmentCode "
		    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append("and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ");
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sb.append(" and a.strPOSCode='" + selectedPOSCode + "' ");
	    }
	    if (!cmbSettlementName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append(" and e.strSettelmentCode='" + mapSettlementNameCode.get(cmbSettlementName.getSelectedItem().toString()) + "' ");
	    }
	    sb.append("group by date(a.dteBillDate),b.strTaxCode "
		    + "order by date(a.dteBillDate),b.strTaxCode; ");

	    funFillTaxData(sb);
	    //fill Live Data

	    //set null values to 0.00
	    for (int row = 0; row < tblDayWiseSalesSummary.getRowCount(); row++)
	    {
		for (int col = 0; col < tblDayWiseSalesSummary.getColumnCount(); col++)
		{
		    if (null == tblDayWiseSalesSummary.getValueAt(row, col))
		    {
			tblDayWiseSalesSummary.setValueAt("0.00", row, col);
		    }
		}
	    }
	    //set day total
	    dm.addColumn("Grand Total".toUpperCase());
	    totalDm.addColumn("Grand Total".toUpperCase());
	    Map<String, Double> mapDayGrandTotal = new HashMap<>();
	    Map<String, Double> mapDayNetTotal = new HashMap<>();
	    String sqlGrandTotal = "SELECT DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y'),sum(a.dblGrandTotal),sum(a.dblSubTotal)-sum(a.dblDiscountAmt)"
		    + "FROM tblqbillhd a "
		    + "WHERE date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ";
	    if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlGrandTotal += "and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ";
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sqlGrandTotal = sqlGrandTotal + " and a.strPOSCode='" + selectedPOSCode + "' ";
	    }

	    sqlGrandTotal = sqlGrandTotal + "GROUP BY DATE(a.dteBillDate) "
		    + "ORDER BY DATE(a.dteBillDate); ";
	    rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlGrandTotal);
	    while (rsSales.next())
	    {
		mapDayGrandTotal.put(rsSales.getString(1), rsSales.getDouble(2));
		mapDayNetTotal.put(rsSales.getString(1), rsSales.getDouble(3));
	    }

	    sqlGrandTotal = "SELECT DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y'),sum(a.dblGrandTotal),sum(a.dblSubTotal)-sum(a.dblDiscountAmt)"
		    + "FROM tblbillhd a "
		    + "where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ";
	    if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlGrandTotal += "and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ";
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sqlGrandTotal = sqlGrandTotal + " and a.strPOSCode='" + selectedPOSCode + "' ";
	    }

	    sqlGrandTotal = sqlGrandTotal + "GROUP BY DATE(a.dteBillDate) "
		    + "ORDER BY DATE(a.dteBillDate); ";
	    rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlGrandTotal);
	    while (rsSales.next())
	    {
		if (mapDayGrandTotal.containsKey(rsSales.getString(1)))
		{
		    mapDayGrandTotal.put(rsSales.getString(1), mapDayGrandTotal.get(rsSales.getString(1)) + rsSales.getDouble(2));
		    mapDayNetTotal.put(rsSales.getString(1), mapDayNetTotal.get(rsSales.getString(1)) + rsSales.getDouble(3));
		}
		else
		{
		    mapDayGrandTotal.put(rsSales.getString(1), rsSales.getDouble(2));
		    mapDayNetTotal.put(rsSales.getString(1), rsSales.getDouble(3));
		}
	    }
	    rsSales.close();
	    int lasColumn = tblDayWiseSalesSummary.getColumnCount() - 1;
	    for (int row = 0; row < tblDayWiseSalesSummary.getRowCount(); row++)
	    {
		tblDayWiseSalesSummary.setValueAt(mapDayGrandTotal.get(tblDayWiseSalesSummary.getValueAt(row, 0).toString()), row, lasColumn);
	    }

	    dm.addColumn("Net Total".toUpperCase());
	    totalDm.addColumn("Net Total".toUpperCase());
	    lasColumn = tblDayWiseSalesSummary.getColumnCount() - 1;
	    for (int row = 0; row < tblDayWiseSalesSummary.getRowCount(); row++)
	    {
		tblDayWiseSalesSummary.setValueAt(mapDayNetTotal.get(tblDayWiseSalesSummary.getValueAt(row, 0).toString()), row, lasColumn);
	    }

	    //set day total
	    // set total
	    Object[] totalRow = new Object[tblDayWiseSalesSummary.getColumnCount()];
	    totalRow[0] = "Totals".toUpperCase();
	    totalRow[1] = selectedPOSName.toUpperCase();
	    for (int col = 2; col < tblDayWiseSalesSummary.getColumnCount(); col++)
	    {
		double total = 0.00;
		for (int row = 0; row < tblDayWiseSalesSummary.getRowCount(); row++)
		{
		    if (null == tblDayWiseSalesSummary.getValueAt(row, col))
		    {
			tblDayWiseSalesSummary.setValueAt("0.00", row, col);
		    }
		    else
		    {
			total += Double.parseDouble(tblDayWiseSalesSummary.getValueAt(row, col).toString());
		    }
		}
		totalRow[col] = gDecimalFormat.format(total);
	    }
	    //
	    totalDm.setRowCount(0);
	    totalDm.addRow(totalRow);
	    // set total

	    tblTotal.getColumnModel().getColumn(0).setPreferredWidth(70);
	    tblTotal.getColumnModel().getColumn(1).setPreferredWidth(100);
	    for (int cnt = 2; cnt < tblTotal.getColumnCount(); cnt++)
	    {
		tblTotal.getColumnModel().getColumn(cnt).setPreferredWidth(100);
		tblTotal.getColumnModel().getColumn(cnt).setCellRenderer(rightRenderer);
	    }

	    tblDayWiseSalesSummary.getColumnModel().getColumn(0).setPreferredWidth(70);
	    tblDayWiseSalesSummary.getColumnModel().getColumn(1).setPreferredWidth(100);
	    for (int cnt = 2; cnt < tblDayWiseSalesSummary.getColumnCount(); cnt++)
	    {
		tblDayWiseSalesSummary.getColumnModel().getColumn(cnt).setPreferredWidth(100);
		tblDayWiseSalesSummary.getColumnModel().getColumn(cnt).setCellRenderer(rightRenderer);
	    }

	    //delete columns whoes amount <=0
	    //funDeleteColumn();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funFillSettlementData(StringBuilder sb)
    {
	try
	{
	    ResultSet rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
	    while (rsSales.next())
	    {
		totalSettleAmount += rsSales.getDouble(4);
		for (int tblRow = 0; tblRow < tblDayWiseSalesSummary.getRowCount(); tblRow++)
		{
		    if (tblDayWiseSalesSummary.getValueAt(tblRow, 0).toString().equalsIgnoreCase(rsSales.getString(1)))
		    {
			for (int tblCol = 2; tblCol < tblDayWiseSalesSummary.getColumnCount(); tblCol++)
			{
			    if (tblDayWiseSalesSummary.getColumnName(tblCol).toUpperCase().equals(rsSales.getString(3).toUpperCase()))
			    {
				if (null == tblDayWiseSalesSummary.getValueAt(tblRow, tblCol))
				{
				    tblDayWiseSalesSummary.setValueAt(gDecimalFormat.format(rsSales.getDouble(4)), tblRow, tblCol);
				}
				else
				{
				    Double value = Double.parseDouble(tblDayWiseSalesSummary.getValueAt(tblRow, tblCol).toString()) + rsSales.getDouble(4);
				    tblDayWiseSalesSummary.setValueAt(gDecimalFormat.format(value), tblRow, tblCol);
				}
				break;
			    }
			}
		    }
		    else
		    {
			continue;
		    }
		}
	    }
	    rsSales.close();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funExportFile(JTable tblDayWiseSalesSummary, File file)
    {
	try
	{
	    WritableWorkbook workbook1 = Workbook.createWorkbook(file);
	    WritableSheet sheet1 = workbook1.createSheet("First Sheet", 0);
	    TableModel model = tblDayWiseSalesSummary.getModel();
	    sheet1.addCell(new Label(0, 0, reportName));

	    for (int i = 0; i < model.getColumnCount(); i++)
	    {
		Label column = new Label(i, 1, model.getColumnName(i));
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
	    funAddLastOfExportReport(workbook1);
	    workbook1.write();
	    workbook1.close();

	    String fileName = ExportReportPath + File.separator + exportFormName + objUtility.funGetDateInString() + ".xls";
	    //Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + fileName);

	    //sendMail();
//            clsExportDocument exportDocument = new clsExportDocument();
//            exportDocument.funExportToPDF(tblDayWiseSalesSummary, tblTotal, "Day Wise Sales Summary");
	    /**
	     * to open file
	     */
	    Desktop desktop = Desktop.getDesktop();
	    desktop.open(file);

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
	    int i = 0, j = 0, LastIndexReport = 0;
	    if (exportFormName.equals("SalesSummary"))
	    {
		LastIndexReport = 5;
	    }

	    WritableSheet sheet2 = workbook1.getSheet(0);
	    int r = sheet2.getRows();
	    System.out.println(r);
	    System.out.println("Row Cnt=" + tblTotal.getRowCount());
	    System.out.println("Col Cnt=" + tblTotal.getColumnCount());
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
	    Label row = new Label(1, r + 1, " Created On : " + objUtility.funGetDateInString() + " At : " + fmt + " By : " + clsGlobalVarClass.gUserCode + " ");
	    sheet2.addCell(row);
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funFillTaxData(StringBuilder sqlTax)
    {
	try
	{
	    ResultSet rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlTax.toString());

	    while (rsSales.next())
	    {
		totalTaxAmount += rsSales.getDouble(3);
		for (int tblRow = 0; tblRow < tblDayWiseSalesSummary.getRowCount(); tblRow++)
		{
		    if (tblDayWiseSalesSummary.getValueAt(tblRow, 0).toString().equalsIgnoreCase(rsSales.getString(1)))
		    {
			for (int tblCol = 2; tblCol < tblDayWiseSalesSummary.getColumnCount(); tblCol++)
			{
			    if (tblDayWiseSalesSummary.getColumnName(tblCol).toUpperCase().equals(rsSales.getString(2).toUpperCase()))
			    {
				if (null == tblDayWiseSalesSummary.getValueAt(tblRow, tblCol))
				{
				    tblDayWiseSalesSummary.setValueAt(gDecimalFormat.format(rsSales.getDouble(3)), tblRow, tblCol);
				}
				else
				{
				    Double value = Double.parseDouble(tblDayWiseSalesSummary.getValueAt(tblRow, tblCol).toString()) + rsSales.getDouble(3);
				    tblDayWiseSalesSummary.setValueAt(gDecimalFormat.format(value), tblRow, tblCol);
				}
				break;
			    }
			    else
			    {

			    }

			}
		    }
		    else
		    {
			continue;
		    }
		}
	    }
	    rsSales.close();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funFillTableForItemGroupWise()
    {
	try
	{
	    dm = new DefaultTableModel()
	    {
		@Override
		public boolean isCellEditable(int row, int column)
		{
		    return false;
		}
	    };

	    totalDm = new DefaultTableModel()
	    {
		@Override
		public boolean isCellEditable(int row, int column)
		{
		    return false;
		}
	    };
	    Vector<String> dateCol = new Vector<>();
	    Vector<String> posCol = new Vector<>();
	    Map<String, String> mapBillDate = new HashMap<>();
	    mapSettlemetWiseAmt = new HashMap<>();
	    mapAllTaxes = new HashMap<>();

	    //fill Q Date and POS
	    sb.setLength(0);
	    sb.append("select DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y') "
		    + "from tblqbillhd a,tblqbillsettlementdtl b,tblsettelmenthd c,tblqbilldtl d ,tblitemmaster e,tblsubgrouphd f,tblgrouphd g  "
		    + "where  a.strBillNo=b.strBillNo  and a.strBillNo=d.strBillNo and b.strBillNo=d.strBillNo "
		    + "and d.strItemCode=e.strItemCode and e.strSubGroupCode=f.strSubGroupCode and f.strGroupCode=g.strGroupCode  "
		    + "and date(a.dteBillDate)=date(b.dteBillDate) "
		    + "and b.strSettlementCode=c.strSettelmentCode "
		    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append("and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ");
	    }
	    if (!cmbSettlementName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append(" and c.strSettelmentCode='" + mapSettlementNameCode.get(cmbSettlementName.getSelectedItem().toString()) + "' ");
	    }
	    if (!cmbGroupName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append(" and f.strGroupCode='" + mapGroupNameCode.get(cmbGroupName.getSelectedItem().toString()) + "' ");
	    }
	    sb.append("group by date(a.dteBillDate) "
		    + "order by date(a.dteBillDate); ");
	    ResultSet rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
	    while (rsSales.next())
	    {
		mapBillDate.put(rsSales.getString(1), rsSales.getString(1));
	    }
	    //fill Live Date and POS
	    sb.setLength(0);
	    sb.append("select DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y') "
		    + "from tblbillhd a,tblbillsettlementdtl b,tblsettelmenthd c,tblbilldtl d ,tblitemmaster e,tblsubgrouphd f,tblgrouphd g "
		    + "where  a.strBillNo=b.strBillNo  and a.strBillNo=d.strBillNo and b.strBillNo=d.strBillNo "
		    + "and d.strItemCode=e.strItemCode and e.strSubGroupCode=f.strSubGroupCode and f.strGroupCode=g.strGroupCode "
		    + "and date(a.dteBillDate)=date(b.dteBillDate) "
		    + "and b.strSettlementCode=c.strSettelmentCode "
		    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "'");
	    if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append("and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ");
	    }
	    if (!cmbSettlementName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append(" and c.strSettelmentCode='" + mapSettlementNameCode.get(cmbSettlementName.getSelectedItem().toString()) + "' ");
	    }
	    if (!cmbGroupName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append(" and f.strGroupCode='" + mapGroupNameCode.get(cmbGroupName.getSelectedItem().toString()) + "' ");
	    }
	    sb.append("group by date(a.dteBillDate) "
		    + "order by date(a.dteBillDate); ");
	    rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
	    while (rsSales.next())
	    {
		mapBillDate.put(rsSales.getString(1), rsSales.getString(1));
	    }
	    Iterator<String> itBillDate = mapBillDate.keySet().iterator();
	    while (itBillDate.hasNext())
	    {
		String billDate = itBillDate.next();
		dateCol.add(billDate);
		posCol.add(selectedPOSName.toUpperCase());
	    }
	    //sorts date
	    Collections.sort(dateCol, new DateComparator());
	    dm.addColumn("DATE", dateCol);
	    dm.addColumn("POS", posCol);
	    totalDm.addColumn("TOTALS");
	    totalDm.addColumn("POS");
	    Map<String, String> mapGroups = new HashMap<>();
	    //add group columns
	    sb.setLength(0);

	    //live groups
	    sb.setLength(0);
	    sb.append("select  g.strGroupCode,g.strGroupName "
		    + "from tblbillhd a,tblbilldtl b,tblitemmaster e "
		    + ",tblsubgrouphd f ,tblgrouphd g  "
		    + "where a.strBillNo=b.strBillNo "
		    + "and date(a.dteBillDate)=date(b.dteBillDate) "
		    + "and b.strItemCode=e.strItemCode "
		    + "and e.strSubGroupCode=f.strSubGroupCode "
		    + "and f.strGroupCode=g.strGroupCode "
//		    + "AND b.dblAmount>0  "
		    + "AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' and '" + toDate + "' ");
	    if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append("and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ");
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sb.append(" and a.strPOSCode='" + selectedPOSCode + "' ");
	    }
	    if (!cmbGroupName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append(" and g.strGroupCode='" + mapGroupNameCode.get(cmbGroupName.getSelectedItem().toString()) + "' ");
	    }

	    sb.append(" GROUP BY g.strGroupCode,g.strGroupName ");
	    ResultSet rsGroups = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
	    while (rsGroups.next())
	    {
		mapGroups.put(rsGroups.getString(2), rsGroups.getString(2));
	    }
	    rsGroups.close();

	    //Q groups
	    sb.setLength(0);
	    sb.append("select  g.strGroupCode,g.strGroupName "
		    + "from tblqbillhd a,tblqbilldtl b,tblitemmaster e "
		    + ",tblsubgrouphd f ,tblgrouphd g  "
		    + "where a.strBillNo=b.strBillNo "
		    + "and date(a.dteBillDate)=date(b.dteBillDate) "
		    + "and b.strItemCode=e.strItemCode "
		    + "and e.strSubGroupCode=f.strSubGroupCode "
		    + "and f.strGroupCode=g.strGroupCode "
//		    + "AND b.dblAmount>0  "
		    + "AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' and '" + toDate + "' ");
	    if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append("and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ");
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sb.append(" and a.strPOSCode='" + selectedPOSCode + "' ");
	    }
	    if (!cmbGroupName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append(" and g.strGroupCode='" + mapGroupNameCode.get(cmbGroupName.getSelectedItem().toString()) + "' ");
	    }

	    sb.append(" GROUP BY g.strGroupCode,g.strGroupName ");
	    rsGroups = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
	    while (rsGroups.next())
	    {
		mapGroups.put(rsGroups.getString(2), rsGroups.getString(2));
	    }
	    rsGroups.close();

	    //filling groups in table columns
	    for (Map.Entry<String, String> entryGroup : mapGroups.entrySet())
	    {
		String groupName = entryGroup.getKey();
		String groupCode = entryGroup.getValue();

		dm.addColumn(groupName.toUpperCase());
		totalDm.addColumn(groupName.toUpperCase());
	    }

	    Map<String, String> mapSettlements = new HashMap<String, String>();
	    //fill Q settlement whoes amt>0   
	    sb.setLength(0);
	    sb.append("SELECT c.strSettelmentDesc "
		    + "FROM tblqbillhd a,tblqbillsettlementdtl b,tblsettelmenthd c "
		    + "WHERE a.strBillNo=b.strBillNo  "
		    + "and date(a.dteBillDate)=date(b.dteBillDate) "
		    + "AND b.strSettlementCode=c.strSettelmentCode  "
		    + "and b.dblSettlementAmt>0 "
		    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append("and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ");
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sb.append(" and a.strPOSCode='" + selectedPOSCode + "' ");
	    }
	    if (!cmbSettlementName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append(" and c.strSettelmentCode='" + mapSettlementNameCode.get(cmbSettlementName.getSelectedItem().toString()) + "' ");
	    }
	    sb.append("GROUP BY strSettelmentDesc "
		    + "ORDER BY strSettelmentDesc; ");
	    rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
	    while (rsSales.next())
	    {
		mapSettlements.put(rsSales.getString(1).toUpperCase(), rsSales.getString(1).toUpperCase());
	    }
	    rsSales.close();
	    //fill Live settlement whoes amt>0
	    sb.setLength(0);
	    sb.append("SELECT c.strSettelmentDesc "
		    + "FROM tblbillhd a,tblbillsettlementdtl b,tblsettelmenthd c "
		    + "WHERE a.strBillNo=b.strBillNo  "
		    + "and date(a.dteBillDate)=date(b.dteBillDate) "
		    + "AND b.strSettlementCode=c.strSettelmentCode  "
		    + "and b.dblSettlementAmt>0 "
		    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append("and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ");
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sb.append(" and a.strPOSCode='" + selectedPOSCode + "' ");
	    }
	    if (!cmbSettlementName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append(" and c.strSettelmentCode='" + mapSettlementNameCode.get(cmbSettlementName.getSelectedItem().toString()) + "' ");
	    }
	    sb.append("GROUP BY strSettelmentDesc "
		    + "ORDER BY strSettelmentDesc; ");
	    rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
	    while (rsSales.next())
	    {

		mapSettlements.put(rsSales.getString(1).toUpperCase(), rsSales.getString(1).toUpperCase());
	    }
	    rsSales.close();

	    Iterator<String> itSettlement = mapSettlements.keySet().iterator();
	    int cntArrLen = 0;
	    while (itSettlement.hasNext())
	    {
		String settleDisc = itSettlement.next();
		dm.addColumn(settleDisc);
		totalDm.addColumn(settleDisc);
		cntArrLen++;
	    }

	    String taxCalType = "";
	    //fill TAX

	    ///live
	    sb.setLength(0);
	    sb.append("select distinct(a.strTaxCode),a.strTaxDesc,a.strTaxCalculation  "
		    + "from tbltaxhd a,tblbilltaxdtl b "
		    + "where a.strTaxCode=b.strTaxCode "
		    + "and date(b.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    ResultSet rsAllTaxes = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
	    while (rsAllTaxes.next())
	    {
		taxCalType = rsAllTaxes.getString(3).trim();
		mapAllTaxes.put(rsAllTaxes.getString(1), rsAllTaxes.getString(2));
	    }
	    rsAllTaxes.close();
	    ///Qfile
	    sb.setLength(0);
	    sb.append("select distinct(a.strTaxCode),a.strTaxDesc,a.strTaxCalculation  "
		    + "from tbltaxhd a,tblqbilltaxdtl b "
		    + "where a.strTaxCode=b.strTaxCode "
		    + "and date(b.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    rsAllTaxes = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
	    while (rsAllTaxes.next())
	    {
		taxCalType = rsAllTaxes.getString(3).trim();
		mapAllTaxes.put(rsAllTaxes.getString(1), rsAllTaxes.getString(2));
	    }
	    rsAllTaxes.close();

	    for (String taxDesc : mapAllTaxes.values())
	    {
		dm.addColumn(taxDesc.toUpperCase());
		totalDm.addColumn(taxDesc.toUpperCase());
		cntArrLen++;
	    }

	    cntArrLen = cntArrLen + 2;
	    Object[] arrObjRecords1 = new Object[cntArrLen];
	    arrObjRecords1[0] = "TOTALS";
	    arrObjRecords1[1] = selectedPOSName.toUpperCase();
	    for (int cnt = 2; cnt < cntArrLen; cnt++)
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

	    tblDayWiseSalesSummary.setRowHeight(25);
	    tblDayWiseSalesSummary.setModel(dm);
	    tblDayWiseSalesSummary.setAutoscrolls(true);
	    tblDayWiseSalesSummary.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

	    mapDayNetTotal.clear();

	    mapDayGrandTotal.clear();

	    String columnForSalesAmount = "sum(b.dblAmount) ";
	    if (chkWithDiscount.isSelected())
	    {
		columnForSalesAmount = "sum(b.dblAmount) ";
	    }
	    else
	    {
		columnForSalesAmount = "sum(b.dblAmount)-sum(b.dblDiscountAmt) ";
	    }
//            if (taxCalType.equalsIgnoreCase("Backward"))
//            {
//                columnForSalesAmount += " -sum(b.dblTaxAmount) ";
//            }

	    String columnForModiSalesAmount = "SUM(h.dblAmount) ";
	    if (chkWithDiscount.isSelected())
	    {
		columnForModiSalesAmount = "SUM(h.dblAmount) ";
	    }
	    else
	    {
		columnForModiSalesAmount = "SUM(h.dblAmount)-sum(h.dblDiscAmt) ";
	    }

	    //fill Q data group 
	    sb.setLength(0);
	    sb.append("select DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y'),g.strGroupName," + columnForSalesAmount + ",sum(b.dblDiscountAmt),sum(b.dblAmount)-sum(b.dblDiscountAmt) "
		    + "from tblqbillhd a,tblqbilldtl b,tblitemmaster e "
		    + ",tblsubgrouphd f ,tblgrouphd g  "
		    + "where a.strBillNo=b.strBillNo "
		    + "and date(a.dteBillDate)=date(b.dteBillDate) "
		    + "and b.strItemCode=e.strItemCode "
		    + "and e.strSubGroupCode=f.strSubGroupCode "
		    + "and f.strGroupCode=g.strGroupCode "
//		    + "AND b.dblAmount>0  "
		    + "AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' and '" + toDate + "' ");
	    if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append("and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ");
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sb.append(" and a.strPOSCode='" + selectedPOSCode + "' ");
	    }
	    if (!cmbGroupName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append(" and g.strGroupCode='" + mapGroupNameCode.get(cmbGroupName.getSelectedItem().toString()) + "' ");
	    }

	    sb.append(" GROUP BY DATE(a.dteBillDate),g.strGroupCode,g.strGroupName ");

	    funFillGroupWiseSalesData(sb);

	    //fill Q modifier data group
	    sb.setLength(0);
	    sb.append("SELECT DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y'),g.strGroupName," + columnForModiSalesAmount + ",sum(h.dblDiscAmt),sum(h.dblAmount)-sum(h.dblDiscAmt) "
		    + "FROM tblqbillhd a,tblitemmaster e,tblsubgrouphd f,tblgrouphd g,tblqbillmodifierdtl h "
		    + "WHERE a.strBillNo=h.strBillNo  "
		    + "and date(a.dteBillDate)=date(h.dteBillDate) "
		    + "AND e.strSubGroupCode=f.strSubGroupCode  "
		    + "AND f.strGroupCode=g.strGroupCode  "
		    + "and h.dblAmount>0 "
		    + "AND a.strBillNo=h.strBillNo  "
		    + "AND e.strItemCode=LEFT(h.strItemCode,7) "
		    + "AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' and '" + toDate + "' ");
	    if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append(" and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ");
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sb.append(" and a.strPOSCode='" + selectedPOSCode + "' ");
	    }
	    if (!cmbGroupName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append(" and g.strGroupCode='" + mapGroupNameCode.get(cmbGroupName.getSelectedItem().toString()) + "' ");
	    }

	    sb.append(" GROUP BY DATE(a.dteBillDate),g.strGroupCode,g.strGroupName ");

	    funFillGroupWiseSalesData(sb);

	    //fill Q Data     
	    sb.setLength(0);
	    sb.append("select DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y'),'" + selectedPOSName + "',c.strSettelmentDesc,sum(b.dblSettlementAmt) "
		    + "from tblqbillhd a,tblqbillsettlementdtl b,tblsettelmenthd c "
		    + "where a.strBillNo=b.strBillNo "
		    + "and date(a.dteBillDate)=date(b.dteBillDate) "
		    + "and b.strSettlementCode=c.strSettelmentCode "
		    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append("and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ");
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sb.append(" and a.strPOSCode='" + selectedPOSCode + "' ");
	    }
	    if (!cmbSettlementName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append(" and c.strSettelmentCode='" + mapSettlementNameCode.get(cmbSettlementName.getSelectedItem().toString()) + "' ");
	    }
	    sb.append("group by date(a.dteBillDate),c.strSettelmentDesc "
		    + "order by date(a.dteBillDate),c.strSettelmentDesc; ");

	    funFillSettlementData(sb);
	    sb.setLength(0);
	    sb.append("select DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y'),c.strTaxDesc,sum(b.dblTaxAmount) "
		    + "from "
		    + "tblqbillhd a,tblqbilltaxdtl b,tbltaxhd c "
		    + "where a.strBillNo=b.strBillNo "
		    + "and date(a.dteBillDate)=date(b.dteBillDate) "
		    + "and b.strTaxCode=c.strTaxCode "
		    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append("and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ");
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sb.append(" and a.strPOSCode='" + selectedPOSCode + "' ");
	    }

	    sb.append("group by date(a.dteBillDate),b.strTaxCode "
		    + "order by date(a.dteBillDate),b.strTaxCode; ");
	    funFillTaxData(sb);
	    //fill Q Data    

	    //fill live data group
	    sb.setLength(0);
	    sb.append("select DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y'),g.strGroupName," + columnForSalesAmount + ",sum(b.dblDiscountAmt),sum(b.dblAmount)-sum(b.dblDiscountAmt) "
		    + "from tblbillhd a,tblbilldtl b,tblitemmaster e "
		    + ",tblsubgrouphd f ,tblgrouphd g  "
		    + "where a.strBillNo=b.strBillNo "
		    + "and date(a.dteBillDate)=date(b.dteBillDate) "
		    + "and b.strItemCode=e.strItemCode "
		    + "and e.strSubGroupCode=f.strSubGroupCode "
		    + "and f.strGroupCode=g.strGroupCode "
//		    + "AND b.dblAmount>0  "
		    + "AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' and '" + toDate + "' ");
	    if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append("and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ");
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sb.append(" and a.strPOSCode='" + selectedPOSCode + "' ");
	    }

	    sb.append(" GROUP BY DATE(a.dteBillDate),g.strGroupCode,g.strGroupName ");

	    funFillGroupWiseSalesData(sb);

	    //fill live modifier data group
	    sb.setLength(0);
	    sb.append("SELECT DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y'),g.strGroupName," + columnForModiSalesAmount + ",sum(h.dblDiscAmt),sum(h.dblAmount)-sum(h.dblDiscAmt) "
		    + "FROM tblbillhd a,tblitemmaster e,tblsubgrouphd f,tblgrouphd g,tblbillmodifierdtl h "
		    + "WHERE a.strBillNo=h.strBillNo  "
		    + "and date(a.dteBillDate)=date(h.dteBillDate) "
		    + "AND e.strSubGroupCode=f.strSubGroupCode  "
		    + "AND f.strGroupCode=g.strGroupCode  "
		    + "and h.dblAmount>0 "
		    + "AND a.strBillNo=h.strBillNo  "
		    + "AND e.strItemCode=LEFT(h.strItemCode,7) "
		    + "AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' and '" + toDate + "' ");
	    if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append(" and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ");
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sb.append(" and a.strPOSCode='" + selectedPOSCode + "' ");
	    }

	    sb.append(" GROUP BY DATE(a.dteBillDate),g.strGroupCode,g.strGroupName ");

	    funFillGroupWiseSalesData(sb);

	    //fill live Data     
	    sb.setLength(0);
	    sb.append("select DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y'),'" + selectedPOSName + "',c.strSettelmentDesc,sum(b.dblSettlementAmt) "
		    + "from tblbillhd a,tblbillsettlementdtl b,tblsettelmenthd c "
		    + "where a.strBillNo=b.strBillNo "
		    + "and date(a.dteBillDate)=date(b.dteBillDate) "
		    + "and b.strSettlementCode=c.strSettelmentCode "
		    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append("and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ");
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sb.append(" and a.strPOSCode='" + selectedPOSCode + "' ");
	    }
	    if (!cmbSettlementName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append(" and c.strSettelmentCode='" + mapSettlementNameCode.get(cmbSettlementName.getSelectedItem().toString()) + "' ");
	    }
	    sb.append("group by date(a.dteBillDate),c.strSettelmentDesc "
		    + "order by date(a.dteBillDate),c.strSettelmentDesc; ");

	    funFillSettlementData(sb);

	    /*
             * update group wise subtotal settlement wise
	     */
	    funFillSettlementWiseGroupWiseAmtMapForLive();
	    funFillSettlementWiseGroupWiseAmtMapForQFile();
//            for (Map.Entry<String, Double> entry : mapSettlemetWiseAmt.entrySet())
//            {
//                System.out.println(entry.getKey() + "->" + entry.getValue());
//            }
	    funUpdateSettWiseGroupWiseAmtForLive();
	    funUpdateSettWiseGroupWiseAmtForQFile();

	    sb.setLength(0);
	    sb.append("select DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y'),c.strTaxDesc,sum(b.dblTaxAmount) "
		    + "from "
		    + "tblbillhd a,tblbilltaxdtl b,tbltaxhd c "
		    + "where a.strBillNo=b.strBillNo "
		    + "and date(a.dteBillDate)=date(b.dteBillDate) "
		    + "and b.strTaxCode=c.strTaxCode "
		    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append("and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ");
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sb.append(" and a.strPOSCode='" + selectedPOSCode + "' ");
	    }
	    sb.append("group by date(a.dteBillDate),b.strTaxCode "
		    + "order by date(a.dteBillDate),b.strTaxCode; ");
	    funFillTaxData(sb);
	    //fill Live Data

	    /*
             * update group wise Taxees
	     */
	    funUpdateSettWiseGroupWiseTaxAmtForLive();
	    funUpdateSettWiseGroupWiseTaxAmtForQFile();

	    //set null values to 0.00
	    for (int row = 0; row < tblDayWiseSalesSummary.getRowCount(); row++)
	    {
		for (int col = 0; col < tblDayWiseSalesSummary.getColumnCount(); col++)
		{
		    if (null == tblDayWiseSalesSummary.getValueAt(row, col))
		    {
			tblDayWiseSalesSummary.setValueAt("0.00", row, col);
		    }
		}
	    }

	    //set day wise discount and roundoff
	    dm.addColumn("Discount".toUpperCase());
	    totalDm.addColumn("Disc Total".toUpperCase());

	    Map<String, clsBillHd> mapDayWiseDiscRoundOff = new HashMap<>();

	    sb.setLength(0);
	    sb.append("SELECT DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y'),sum(a.dblDiscountAmt),sum(a.dblRoundOff) "
		    + "FROM tblqbillhd a  "
		    + "WHERE date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");	  
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sb.append(" and a.strPOSCode='" + selectedPOSCode + "' ");
	    }
	   
	    sb.append("GROUP BY DATE(a.dteBillDate) "
		    + "ORDER BY DATE(a.dteBillDate); ");
	    rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
	    while (rsSales.next())
	    {
		String keyDate = rsSales.getString(1);
		if (mapDayWiseDiscRoundOff.containsKey(keyDate))
		{
		    clsBillHd objBillHd = mapDayWiseDiscRoundOff.get(keyDate);
		    objBillHd.setDblDiscountAmt(objBillHd.getDblDiscountAmt() + rsSales.getDouble(2));//disc
		    objBillHd.setDblRoundOff(objBillHd.getDblRoundOff() + rsSales.getDouble(3));//roundOff

		    mapDayWiseDiscRoundOff.put(keyDate, objBillHd);
		}
		else
		{
		    clsBillHd objBillHd = new clsBillHd();
		    objBillHd.setDteBillDate(keyDate);
		    objBillHd.setDblDiscountAmt(rsSales.getDouble(2));//disc
		    objBillHd.setDblRoundOff(rsSales.getDouble(3));//roundOff

		    mapDayWiseDiscRoundOff.put(keyDate, objBillHd);
		}
	    }
	    rsSales.close();

	    sb.setLength(0);
	    sb.append("SELECT DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y'),sum(a.dblDiscountAmt),sum(a.dblRoundOff) "
		    + "FROM tblbillhd a "
		    + "WHERE date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "'  ");	 
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sb.append(" and a.strPOSCode='" + selectedPOSCode + "' ");
	    }
	   
	    sb.append("GROUP BY DATE(a.dteBillDate) "
		    + "ORDER BY DATE(a.dteBillDate); ");
	    rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
	    while (rsSales.next())
	    {
		String keyDate = rsSales.getString(1);
		if (mapDayWiseDiscRoundOff.containsKey(keyDate))
		{
		    clsBillHd objBillHd = mapDayWiseDiscRoundOff.get(keyDate);
		    objBillHd.setDblDiscountAmt(objBillHd.getDblDiscountAmt() + rsSales.getDouble(2));//disc
		    objBillHd.setDblRoundOff(objBillHd.getDblRoundOff() + rsSales.getDouble(3));//roundOff

		    mapDayWiseDiscRoundOff.put(keyDate, objBillHd);
		}
		else
		{
		    clsBillHd objBillHd = new clsBillHd();
		    objBillHd.setDteBillDate(keyDate);
		    objBillHd.setDblDiscountAmt(rsSales.getDouble(2));//disc
		    objBillHd.setDblRoundOff(rsSales.getDouble(3));//roundOff

		    mapDayWiseDiscRoundOff.put(keyDate, objBillHd);
		}
	    }
	    rsSales.close();

	    //set disc amount
	    int lasColumn = tblDayWiseSalesSummary.getColumnCount() - 1;
	    for (int row = 0; row < tblDayWiseSalesSummary.getRowCount(); row++)
	    {
		//System.out.println("set disc. tblDayWiseSalesSummary.getValueAt(row, 0).toString()->"+tblDayWiseSalesSummary.getValueAt(row, 0).toString());

		clsBillHd objBillHd = new clsBillHd();
		objBillHd.setDblDiscountAmt(0.00);
		if (mapDayWiseDiscRoundOff.containsKey(tblDayWiseSalesSummary.getValueAt(row, 0).toString()))
		{
		    objBillHd = mapDayWiseDiscRoundOff.get(tblDayWiseSalesSummary.getValueAt(row, 0).toString());
		}

		tblDayWiseSalesSummary.setValueAt(objBillHd.getDblDiscountAmt(), row, lasColumn);
	    }

	    //set day wise grand total
	    dm.addColumn("Grand Total".toUpperCase());
	    totalDm.addColumn("Grand Total".toUpperCase());

	    sb.setLength(0);
	    sb.append("SELECT DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y'),sum(b.dblSettlementAmt),sum(a.dblSubTotal)-sum(a.dblDiscountAmt) "
		    + "FROM tblqbillhd a,tblqbillsettlementdtl b,tblsettelmenthd c "
		    + "WHERE a.strBillNo=b.strBillNo  "
		    + "and date(a.dteBillDate)=date(b.dteBillDate) "
		    + "AND b.strSettlementCode=c.strSettelmentCode  "
		    + "and b.dblSettlementAmt>0 "
		    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append("and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ");
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sb.append(" and a.strPOSCode='" + selectedPOSCode + "' ");
	    }
	    if (!cmbSettlementName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append(" and c.strSettelmentCode='" + mapSettlementNameCode.get(cmbSettlementName.getSelectedItem().toString()) + "' ");
	    }
	    sb.append("GROUP BY DATE(a.dteBillDate) "
		    + "ORDER BY DATE(a.dteBillDate); ");
	    rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
	    while (rsSales.next())
	    {
		mapDayGrandTotal.put(rsSales.getString(1), rsSales.getDouble(2));
		//mapDayNetTotal.put(rsSales.getString(1), rsSales.getDouble(3));
	    }
	    rsSales.close();

	    sb.setLength(0);
	    sb.append("SELECT DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y'),sum(b.dblSettlementAmt),sum(a.dblSubTotal)-sum(a.dblDiscountAmt) "
		    + "FROM tblbillhd a,tblbillsettlementdtl b,tblsettelmenthd c "
		    + "WHERE a.strBillNo=b.strBillNo  "
		    + "and date(a.dteBillDate)=date(b.dteBillDate) "
		    + "AND b.strSettlementCode=c.strSettelmentCode  "
		    + "and b.dblSettlementAmt>0 "
		    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append("and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ");
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sb.append(" and a.strPOSCode='" + selectedPOSCode + "' ");
	    }
	    if (!cmbSettlementName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append(" and c.strSettelmentCode='" + mapSettlementNameCode.get(cmbSettlementName.getSelectedItem().toString()) + "' ");
	    }
	    sb.append("GROUP BY DATE(a.dteBillDate) "
		    + "ORDER BY DATE(a.dteBillDate); ");
	    rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
	    while (rsSales.next())
	    {
		if (mapDayGrandTotal.containsKey(rsSales.getString(1)))
		{
		    mapDayGrandTotal.put(rsSales.getString(1), mapDayGrandTotal.get(rsSales.getString(1)) + rsSales.getDouble(2));
		    // mapDayNetTotal.put(rsSales.getString(1), mapDayNetTotal.get(rsSales.getString(1)) + rsSales.getDouble(3));
		}
		else
		{
		    mapDayGrandTotal.put(rsSales.getString(1), rsSales.getDouble(2));
		    // mapDayNetTotal.put(rsSales.getString(1), rsSales.getDouble(3));
		}
	    }
	    rsSales.close();

	    lasColumn = tblDayWiseSalesSummary.getColumnCount() - 1;
	    for (int row = 0; row < tblDayWiseSalesSummary.getRowCount(); row++)
	    {
		if (mapDayGrandTotal.containsKey(tblDayWiseSalesSummary.getValueAt(row, 0).toString()))
		{
		    tblDayWiseSalesSummary.setValueAt(mapDayGrandTotal.get(tblDayWiseSalesSummary.getValueAt(row, 0).toString()), row, lasColumn);
		}
		else
		{
		    tblDayWiseSalesSummary.setValueAt(0.00, row, lasColumn);
		}
	    }

	    dm.addColumn("RoundOff".toUpperCase());
	    totalDm.addColumn("RoundOff Total".toUpperCase());
	    //set roundoff amount
	    lasColumn = tblDayWiseSalesSummary.getColumnCount() - 1;
	    for (int row = 0; row < tblDayWiseSalesSummary.getRowCount(); row++)
	    {
		clsBillHd objBillHd = new clsBillHd();
		objBillHd.setDblRoundOff(0.00);

		if (mapDayWiseDiscRoundOff.containsKey(tblDayWiseSalesSummary.getValueAt(row, 0).toString()))
		{
		    objBillHd = mapDayWiseDiscRoundOff.get(tblDayWiseSalesSummary.getValueAt(row, 0).toString());
		}

		tblDayWiseSalesSummary.setValueAt(objBillHd.getDblRoundOff(), row, lasColumn);
	    }

	    String sqlGrandTotal = "SELECT DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y'),sum(a.dblGrandTotal),sum(a.dblSubTotal)-sum(a.dblDiscountAmt)"
		    + "FROM tblqbillhd a "
		    + "WHERE date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ";
	    if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlGrandTotal += "and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ";
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sqlGrandTotal = sqlGrandTotal + " and a.strPOSCode='" + selectedPOSCode + "' ";
	    }

	    sqlGrandTotal = sqlGrandTotal + "GROUP BY DATE(a.dteBillDate) "
		    + "ORDER BY DATE(a.dteBillDate); ";
	    rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlGrandTotal);
	    while (rsSales.next())
	    {
		mapDayNetTotal.put(rsSales.getString(1), rsSales.getDouble(3));
	    }

	    sqlGrandTotal = "SELECT DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y'),sum(a.dblGrandTotal),sum(a.dblSubTotal)-sum(a.dblDiscountAmt)"
		    + "FROM tblbillhd a "
		    + "where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ";
	    if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlGrandTotal += "and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ";
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sqlGrandTotal = sqlGrandTotal + " and a.strPOSCode='" + selectedPOSCode + "' ";
	    }

	    sqlGrandTotal = sqlGrandTotal + "GROUP BY DATE(a.dteBillDate) "
		    + "ORDER BY DATE(a.dteBillDate); ";
	    rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlGrandTotal);
	    while (rsSales.next())
	    {
		if (mapDayNetTotal.containsKey(rsSales.getString(1)))
		{
		    mapDayNetTotal.put(rsSales.getString(1), mapDayNetTotal.get(rsSales.getString(1)) + rsSales.getDouble(3));
		}
		else
		{
		    mapDayNetTotal.put(rsSales.getString(1), rsSales.getDouble(3));
		}
	    }
	    rsSales.close();

	    dm.addColumn("Net Total".toUpperCase());
	    totalDm.addColumn("Net Total".toUpperCase());
	    lasColumn = tblDayWiseSalesSummary.getColumnCount() - 1;
	    for (int row = 0; row < tblDayWiseSalesSummary.getRowCount(); row++)
	    {
		if (mapDayNetTotal.containsKey(tblDayWiseSalesSummary.getValueAt(row, 0).toString()))
		{
		    tblDayWiseSalesSummary.setValueAt(mapDayNetTotal.get(tblDayWiseSalesSummary.getValueAt(row, 0).toString()), row, lasColumn);
		}
		else
		{
		    tblDayWiseSalesSummary.setValueAt(0.00, row, lasColumn);
		}
	    }

	    //set day total
	    // set total
	    Object[] totalRow = new Object[tblDayWiseSalesSummary.getColumnCount()];
	    totalRow[0] = "Totals".toUpperCase();
	    totalRow[1] = selectedPOSName.toUpperCase();
	    for (int col = 2; col < tblDayWiseSalesSummary.getColumnCount(); col++)
	    {
		double total = 0.00;
		for (int row = 0; row < tblDayWiseSalesSummary.getRowCount(); row++)
		{
		    if (null == tblDayWiseSalesSummary.getValueAt(row, col))
		    {
			tblDayWiseSalesSummary.setValueAt("0.00", row, col);
		    }
		    else
		    {
			total += Double.parseDouble(tblDayWiseSalesSummary.getValueAt(row, col).toString());
		    }
		}
		totalRow[col] = gDecimalFormat.format(total);
	    }

	    //
	    totalDm.setRowCount(0);
	    totalDm.addRow(totalRow);

	    // set total
	    tblTotal.getColumnModel().getColumn(0).setPreferredWidth(70);
	    tblTotal.getColumnModel().getColumn(1).setPreferredWidth(100);
	    for (int cnt = 2; cnt < tblTotal.getColumnCount(); cnt++)
	    {
		tblTotal.getColumnModel().getColumn(cnt).setPreferredWidth(100);
		tblTotal.getColumnModel().getColumn(cnt).setCellRenderer(rightRenderer);
	    }

	    tblDayWiseSalesSummary.getColumnModel().getColumn(0).setPreferredWidth(70);
	    tblDayWiseSalesSummary.getColumnModel().getColumn(1).setPreferredWidth(100);
	    for (int cnt = 2; cnt < tblDayWiseSalesSummary.getColumnCount(); cnt++)
	    {
		tblDayWiseSalesSummary.getColumnModel().getColumn(cnt).setPreferredWidth(100);
		tblDayWiseSalesSummary.getColumnModel().getColumn(cnt).setCellRenderer(rightRenderer);
	    }

	    //delete columns whoes amount <=0
	    //funDeleteColumn();
	    tblDayWiseSalesSummary.repaint();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funFillGroupWiseSalesData(StringBuilder sqlGroups)
    {
	try
	{
	    ResultSet rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlGroups.toString());
	    while (rsSales.next())
	    {
		String date = rsSales.getString(1);
		totalGroupAmtTotal += rsSales.getDouble(3);//subtotal|| subTotal-disc
		totalDiscAmtTotal += rsSales.getDouble(4);//discount
		double netTotal = rsSales.getDouble(5);//subTotal-disc

		for (int tblRow = 0; tblRow < tblDayWiseSalesSummary.getRowCount(); tblRow++)
		{
		    if (tblDayWiseSalesSummary.getValueAt(tblRow, 0).toString().equalsIgnoreCase(rsSales.getString(1)))
		    {
			for (int tblCol = 2; tblCol < tblDayWiseSalesSummary.getColumnCount(); tblCol++)
			{
			    if (tblDayWiseSalesSummary.getColumnName(tblCol).toUpperCase().equals(rsSales.getString(2).toUpperCase()))
			    {
				if (null == tblDayWiseSalesSummary.getValueAt(tblRow, tblCol))
				{
				    tblDayWiseSalesSummary.setValueAt(gDecimalFormat.format(rsSales.getDouble(3)), tblRow, tblCol);
				}
				else
				{
				    Double value = Double.parseDouble(tblDayWiseSalesSummary.getValueAt(tblRow, tblCol).toString()) + rsSales.getDouble(3);
				    tblDayWiseSalesSummary.setValueAt(value, tblRow, tblCol);
				}
				break;
			    }
			}
		    }
		    else
		    {
			continue;
		    }
		}
	    }
	    rsSales.close();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funUpdateSettWiseGroupWiseAmtForLive()
    {
	sb.setLength(0);
	sb.append("select DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y'),g.strGroupName "
		+ "from tblbillhd a,tblbilldtl b,tblitemmaster e "
		+ ",tblsubgrouphd f ,tblgrouphd g  "
		+ "where a.strBillNo=b.strBillNo "
		+ "and b.strItemCode=e.strItemCode "
		+ "and e.strSubGroupCode=f.strSubGroupCode "
		+ "and f.strGroupCode=g.strGroupCode "
//		+ "AND b.dblAmount>0  "
		+ "AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' and '" + toDate + "' ");
	if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	{
	    sb.append("and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ");
	}
	if (!selectedPOSCode.equalsIgnoreCase("All"))
	{
	    sb.append(" and a.strPOSCode='" + selectedPOSCode + "' ");
	}

	sb.append(" GROUP BY DATE(a.dteBillDate),g.strGroupCode,g.strGroupName ");

	try
	{
	    ResultSet rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
	    while (rsSales.next())
	    {
		for (int tblRow = 0; tblRow < tblDayWiseSalesSummary.getRowCount(); tblRow++)
		{
		    if (tblDayWiseSalesSummary.getValueAt(tblRow, 0).toString().equalsIgnoreCase(rsSales.getString(1)))//match date
		    {
			for (int tblCol = 2; tblCol < tblDayWiseSalesSummary.getColumnCount(); tblCol++)
			{
			    if (tblDayWiseSalesSummary.getColumnName(tblCol).toUpperCase().equals(rsSales.getString(2).toUpperCase()))//match groupName
			    {
				double dateWiseSettWiseAmt = mapSettlemetWiseAmt.get(tblDayWiseSalesSummary.getValueAt(tblRow, 0) + "!" + cmbSettlementName.getSelectedItem().toString().toUpperCase());
				double dateWiseTotalSettAmt = mapSettlemetWiseAmt.get(tblDayWiseSalesSummary.getValueAt(tblRow, 0) + "!" + "All".toUpperCase());
				double dateWiseSettWisePer = (dateWiseSettWiseAmt / dateWiseTotalSettAmt) * 100;

				double groupSubTotal = Double.parseDouble(tblDayWiseSalesSummary.getValueAt(tblRow, tblCol).toString());
				tblDayWiseSalesSummary.setValueAt(gDecimalFormat.format((dateWiseSettWisePer / 100) * groupSubTotal), tblRow, tblCol);
			    }
			}
		    }
		    else
		    {
			continue;
		    }
		}
	    }
	    rsSales.close();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}

    }

    private void funUpdateSettWiseGroupWiseAmtForQFile()
    {
	sb.setLength(0);
	sb.append("select DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y'),g.strGroupName "
		+ "from tblqbillhd a,tblqbilldtl b,tblitemmaster e "
		+ ",tblsubgrouphd f ,tblgrouphd g  "
		+ "where a.strBillNo=b.strBillNo "
		+ "and b.strItemCode=e.strItemCode "
		+ "and e.strSubGroupCode=f.strSubGroupCode "
		+ "and f.strGroupCode=g.strGroupCode "
//		+ "AND b.dblAmount>0  "
		+ "AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' and '" + toDate + "' ");
	if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	{
	    sb.append("and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ");
	}
	if (!selectedPOSCode.equalsIgnoreCase("All"))
	{
	    sb.append(" and a.strPOSCode='" + selectedPOSCode + "' ");
	}

	sb.append(" GROUP BY DATE(a.dteBillDate),g.strGroupCode,g.strGroupName ");

	try
	{
	    ResultSet rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
	    while (rsSales.next())
	    {
		for (int tblRow = 0; tblRow < tblDayWiseSalesSummary.getRowCount(); tblRow++)
		{
		    if (tblDayWiseSalesSummary.getValueAt(tblRow, 0).toString().equalsIgnoreCase(rsSales.getString(1)))//match date
		    {
			for (int tblCol = 2; tblCol < tblDayWiseSalesSummary.getColumnCount(); tblCol++)
			{
			    if (tblDayWiseSalesSummary.getColumnName(tblCol).toUpperCase().equals(rsSales.getString(2).toUpperCase()))//match groupName
			    {
				// System.out.println("row="+tblRow+"\t col="+tblCol);
				double dateWiseSettWiseAmt = mapSettlemetWiseAmt.get(tblDayWiseSalesSummary.getValueAt(tblRow, 0).toString() + "!" + cmbSettlementName.getSelectedItem().toString().toUpperCase());
				double dateWiseTotalSettAmt = mapSettlemetWiseAmt.get(tblDayWiseSalesSummary.getValueAt(tblRow, 0).toString() + "!" + "All".toUpperCase());
				double dateWiseSettWisePer = (dateWiseSettWiseAmt / dateWiseTotalSettAmt) * 100;

				double groupSubTotal = Double.parseDouble(tblDayWiseSalesSummary.getValueAt(tblRow, tblCol).toString());
				tblDayWiseSalesSummary.setValueAt(gDecimalFormat.format((dateWiseSettWisePer / 100) * groupSubTotal), tblRow, tblCol);
			    }
			}
		    }
		    else
		    {
			continue;
		    }
		}
	    }
	    rsSales.close();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funFillSettlementWiseGroupWiseAmtMapForLive()
    {
	try
	{
	    //fill live Data     
	    sb.setLength(0);
	    sb.append("select DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y'),'" + selectedPOSName + "',c.strSettelmentDesc,sum(b.dblSettlementAmt) "
		    + "from tblbillhd a,tblbillsettlementdtl b,tblsettelmenthd c "
		    + "where a.strBillNo=b.strBillNo "
		    + "and date(a.dteBillDate)=date(b.dteBillDate) "
		    + "and b.strSettlementCode=c.strSettelmentCode "
		    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append("and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ");
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sb.append(" and a.strPOSCode='" + selectedPOSCode + "' ");
	    }
//            if (!cmbSettlementName.getSelectedItem().toString().equalsIgnoreCase("All"))
//            {
//                sb.append(" and c.strSettelmentCode='" + mapSettlementNameCode.get(cmbSettlementName.getSelectedItem().toString()) + "' ");
//            }
	    sb.append("group by date(a.dteBillDate),c.strSettelmentDesc "
		    + "order by date(a.dteBillDate),c.strSettelmentDesc; ");

	    ResultSet rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
	    while (rsSales.next())
	    {
		//date+SettlementDesc
		if (mapSettlemetWiseAmt.containsKey(rsSales.getString(1) + "!" + "All".toUpperCase()))
		{
		    double oldSettlementAmt = mapSettlemetWiseAmt.get(rsSales.getString(1) + "!" + "All".toUpperCase());
		    mapSettlemetWiseAmt.put(rsSales.getString(1) + "!" + "All".toUpperCase(), oldSettlementAmt + rsSales.getDouble(4));
		}
		else
		{
		    mapSettlemetWiseAmt.put(rsSales.getString(1) + "!" + "All".toUpperCase(), rsSales.getDouble(4));
		}

		if (mapSettlemetWiseAmt.containsKey(rsSales.getString(1) + "!" + rsSales.getString(3).toUpperCase()))
		{
		    double oldSettlementAmt = mapSettlemetWiseAmt.get(rsSales.getString(1) + "!" + rsSales.getString(3).toUpperCase());
		    mapSettlemetWiseAmt.put(rsSales.getString(1) + "!" + rsSales.getString(3).toUpperCase(), oldSettlementAmt + rsSales.getDouble(4));
		}
		else
		{
		    mapSettlemetWiseAmt.put(rsSales.getString(1) + "!" + rsSales.getString(3).toUpperCase(), rsSales.getDouble(4));
		}
	    }
	    rsSales.close();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funFillSettlementWiseGroupWiseAmtMapForQFile()
    {
	try
	{
	    //fill live Data     
	    sb.setLength(0);
	    sb.append("select DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y'),'" + selectedPOSName + "',c.strSettelmentDesc,sum(b.dblSettlementAmt) "
		    + "from tblqbillhd a,tblqbillsettlementdtl b,tblsettelmenthd c "
		    + "where a.strBillNo=b.strBillNo "
		    + "and date(a.dteBillDate)=date(b.dteBillDate) "
		    + "and b.strSettlementCode=c.strSettelmentCode "
		    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	    if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sb.append("and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ");
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sb.append(" and a.strPOSCode='" + selectedPOSCode + "' ");
	    }
//            if (!cmbSettlementName.getSelectedItem().toString().equalsIgnoreCase("All"))
//            {
//                sb.append(" and c.strSettelmentCode='" + mapSettlementNameCode.get(cmbSettlementName.getSelectedItem().toString()) + "' ");
//            }
	    sb.append("group by date(a.dteBillDate),c.strSettelmentDesc "
		    + "order by date(a.dteBillDate),c.strSettelmentDesc; ");

	    ResultSet rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
	    while (rsSales.next())
	    {
		//date+SettlementDesc
		if (mapSettlemetWiseAmt.containsKey(rsSales.getString(1) + "!" + "All".toUpperCase()))
		{
		    double oldSettlementAmt = mapSettlemetWiseAmt.get(rsSales.getString(1) + "!" + "All".toUpperCase());
		    mapSettlemetWiseAmt.put(rsSales.getString(1) + "!" + "All".toUpperCase(), oldSettlementAmt + rsSales.getDouble(4));
		}
		else
		{
		    mapSettlemetWiseAmt.put(rsSales.getString(1) + "!" + "All".toUpperCase(), rsSales.getDouble(4));
		}

		if (mapSettlemetWiseAmt.containsKey(rsSales.getString(1) + "!" + rsSales.getString(3).toUpperCase()))
		{
		    double oldSettlementAmt = mapSettlemetWiseAmt.get(rsSales.getString(1) + "!" + rsSales.getString(3).toUpperCase());
		    mapSettlemetWiseAmt.put(rsSales.getString(1) + "!" + rsSales.getString(3).toUpperCase(), oldSettlementAmt + rsSales.getDouble(4));
		}
		else
		{
		    mapSettlemetWiseAmt.put(rsSales.getString(1) + "!" + rsSales.getString(3).toUpperCase(), rsSales.getDouble(4));
		}
	    }
	    rsSales.close();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funUpdateSettWiseGroupWiseTaxAmtForLive()
    {
	sb.setLength(0);
	sb.append("select DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y'),c.strTaxDesc,sum(b.dblTaxAmount) "
		+ "from "
		+ "tblbillhd a,tblbilltaxdtl b,tbltaxhd c "
		+ "where a.strBillNo=b.strBillNo "
		+ "and date(a.dteBillDate)=date(b.dteBillDate) "
		+ "and b.strTaxCode=c.strTaxCode "
		+ "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	{
	    sb.append("and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ");
	}
	if (!selectedPOSCode.equalsIgnoreCase("All"))
	{
	    sb.append(" and a.strPOSCode='" + selectedPOSCode + "' ");
	}
	sb.append("group by date(a.dteBillDate),b.strTaxCode "
		+ "order by date(a.dteBillDate),b.strTaxCode; ");

	try
	{
	    ResultSet rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
	    while (rsSales.next())
	    {
		for (int tblRow = 0; tblRow < tblDayWiseSalesSummary.getRowCount(); tblRow++)
		{
		    if (tblDayWiseSalesSummary.getValueAt(tblRow, 0).toString().equalsIgnoreCase(rsSales.getString(1)))
		    {
			for (int tblCol = 2; tblCol < tblDayWiseSalesSummary.getColumnCount(); tblCol++)
			{
			    if (tblDayWiseSalesSummary.getColumnName(tblCol).toUpperCase().equals(rsSales.getString(2).toUpperCase()))
			    {
				double dateWiseSettWiseAmt = mapSettlemetWiseAmt.get(tblDayWiseSalesSummary.getValueAt(tblRow, 0) + "!" + cmbSettlementName.getSelectedItem().toString().toUpperCase());
				double dateWiseTotalSettAmt = mapSettlemetWiseAmt.get(tblDayWiseSalesSummary.getValueAt(tblRow, 0) + "!" + "All".toUpperCase());
				double dateWiseSettWisePer = 0.00;
				if (dateWiseTotalSettAmt == 0)
				{
				    dateWiseSettWisePer = 0.00;
				}
				else
				{
				    dateWiseSettWisePer = (dateWiseSettWiseAmt / dateWiseTotalSettAmt) * 100;
				}

				double taxAmt = Double.parseDouble(tblDayWiseSalesSummary.getValueAt(tblRow, tblCol).toString());
				tblDayWiseSalesSummary.setValueAt(gDecimalFormat.format((dateWiseSettWisePer / 100) * taxAmt), tblRow, tblCol);
			    }
			}
		    }
		    else
		    {
			continue;
		    }
		}
	    }
	    rsSales.close();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}

    }

    private void funUpdateSettWiseGroupWiseTaxAmtForQFile()
    {
	sb.setLength(0);
	sb.append("select DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y'),c.strTaxDesc,sum(b.dblTaxAmount) "
		+ "from "
		+ "tblqbillhd a,tblqbilltaxdtl b,tbltaxhd c "
		+ "where a.strBillNo=b.strBillNo "
		+ "and date(a.dteBillDate)=date(b.dteBillDate) "
		+ "and b.strTaxCode=c.strTaxCode "
		+ "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	{
	    sb.append("and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ");
	}
	if (!selectedPOSCode.equalsIgnoreCase("All"))
	{
	    sb.append(" and a.strPOSCode='" + selectedPOSCode + "' ");
	}
	sb.append("group by date(a.dteBillDate),b.strTaxCode "
		+ "order by date(a.dteBillDate),b.strTaxCode; ");

	try
	{
	    ResultSet rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
	    while (rsSales.next())
	    {
		for (int tblRow = 0; tblRow < tblDayWiseSalesSummary.getRowCount(); tblRow++)
		{
		    if (tblDayWiseSalesSummary.getValueAt(tblRow, 0).toString().equalsIgnoreCase(rsSales.getString(1)))
		    {
			for (int tblCol = 2; tblCol < tblDayWiseSalesSummary.getColumnCount(); tblCol++)
			{
			    if (tblDayWiseSalesSummary.getColumnName(tblCol).toUpperCase().equals(rsSales.getString(2).toUpperCase()))
			    {
				//System.out.println("row=" + tblRow + "\tcol=" + tblCol);
				//System.out.println("tblDayWiseSalesSummary.getColumnName(tblCol).toUpperCase()->"+tblDayWiseSalesSummary.getColumnName(tblCol).toUpperCase());
				//System.out.println("rsSales.getString(2).toUpperCase()->"+rsSales.getString(2).toUpperCase());

				double dateWiseSettWiseAmt = mapSettlemetWiseAmt.get(tblDayWiseSalesSummary.getValueAt(tblRow, 0) + "!" + cmbSettlementName.getSelectedItem().toString().toUpperCase());
				double dateWiseTotalSettAmt = mapSettlemetWiseAmt.get(tblDayWiseSalesSummary.getValueAt(tblRow, 0) + "!" + "All".toUpperCase());
				double dateWiseSettWisePer = 0.00;
				if (dateWiseTotalSettAmt == 0)
				{
				    dateWiseSettWisePer = 0.00;
				}
				else
				{
				    dateWiseSettWisePer = (dateWiseSettWiseAmt / dateWiseTotalSettAmt) * 100;
				}
				double taxAmt = Double.parseDouble(tblDayWiseSalesSummary.getValueAt(tblRow, tblCol).toString());
				tblDayWiseSalesSummary.setValueAt(gDecimalFormat.format((dateWiseSettWisePer / 100) * taxAmt), tblRow, tblCol);
			    }
			}
		    }
		    else
		    {
			continue;
		    }
		}
	    }
	    rsSales.close();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    class DateComparator implements Comparator<String>
    {

	SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

	public DateComparator()
	{
	}

	@Override
	public int compare(String str1, String str2)
	{
	    try
	    {
		Date date1 = dateFormat.parse(str1);
		Date date2 = dateFormat.parse(str2);

		if (date1.getTime() == date2.getTime())
		{
		    return 0;
		}
		else if (date1.getTime() < date2.getTime())
		{
		    return -1;
		}
		else if (date1.getTime() > date2.getTime())
		{
		    return 1;
		}
	    }
	    catch (ParseException e)
	    {
		e.printStackTrace();
	    }
	    return 0;
	}

    }

    private void funDeleteColumn()
    {
	try
	{
	    for (int cnt = 2; cnt < tblTotal.getColumnCount(); cnt++)
	    {
		if (!(Double.parseDouble(tblTotal.getValueAt(0, cnt).toString()) > 0))
		{
		    tblDayWiseSalesSummary.getColumnModel().removeColumn(tblDayWiseSalesSummary.getColumnModel().getColumn(cnt));
		    tblTotal.getColumnModel().removeColumn(tblTotal.getColumnModel().getColumn(cnt));

		}
		else
		{
		    continue;
		}
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funFillSettlementNameComboBox()
    {
	try
	{

	    String selectQuery = "select a.strSettelmentCode,a.strSettelmentDesc,a.strSettelmentType "
		    + "from tblsettelmenthd a";
	    ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(selectQuery);
	    cmbSettlementName.addItem("All");
	    mapSettlementNameCode.put("All", "All");
	    while (rs.next())
	    {
		cmbSettlementName.addItem(rs.getString(2));//settName
		mapSettlementNameCode.put(rs.getString(2), rs.getString(1));//name->code
	    }
	    rs.close();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funFillGroupNameComboBox()
    {
	try
	{

	    String selectQuery = "select a.strGroupCode,a.strGroupName from tblgrouphd a";
	    ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(selectQuery);
	    cmbGroupName.addItem("All");
	    mapGroupNameCode.put("All", "All");
	    while (rs.next())
	    {
		cmbGroupName.addItem(rs.getString(2));//groupName
		mapGroupNameCode.put(rs.getString(2), rs.getString(1));//name->code
	    }
	    rs.close();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funResetFields()
    {
	totalGroupAmtTotal = 0;
	totalSettleAmount = 0;
	totalTaxAmount = 0;
	totalDiscAmtTotal = 0;
    }

}
