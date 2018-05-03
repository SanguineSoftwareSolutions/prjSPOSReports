package com.POSReport.view;

import com.POSGlobal.controller.clsBillDtl;
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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static java.util.Map.Entry.comparingByKey;
import java.util.TimeZone;
import java.util.TreeMap;
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
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class frmKDSFlash extends javax.swing.JFrame
{

    private String selectQuery;
    private String fromDate;
    private String toDate, reportName;
    DefaultTableModel dm, totalDm;
    private String exportFormName, ExportReportPath;
    private java.util.Vector vSalesReportExcelColLength;
    private String selectedPOSCode;
    private StringBuilder sb = new StringBuilder();
    private clsUtility objUtility;
    private Map<String, String> mapCostCenterNameCode;
    private Map<String, Double> mapSettlemetWiseAmt;
    private DecimalFormat decimalFormtFor2DecPoint;
    private Map<String, String> mapAllTaxes;
    private Map<String, String> mapWaiterNameCode;

    public frmKDSFlash()
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
	    cmbType.removeAllItems();
	    cmbType.addItem("Detail");

	    cmbReportType.addItemListener(new ItemListener()
	    {
		@Override
		public void itemStateChanged(ItemEvent e)
		{
		    if (cmbReportType.getSelectedItem().toString().equalsIgnoreCase("SubGroup"))
		    {
			cmbType.removeAllItems();
			cmbType.addItem("Detail");
			cmbType.addItem("Summary");
		    }
		    else
		    {
			cmbType.removeAllItems();
			cmbType.addItem("Detail");
		    }
		}
	    });

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}

	/**
	 * this Function is used for Component initialization
	 */
	funSetLookAndFeel();

	reportName = "KDS Flash";
	exportFormName = "KDSFlash";
	this.setLocationRelativeTo(null);
	//load all POS
	funFillPOSComboBox();
	//load all POS
	mapCostCenterNameCode = new HashMap<String, String>();
	mapWaiterNameCode = new HashMap<String, String>();
	funFillCostCenterNameComboBox();
	funFillWaiterNameComboBox();

	dteFromDate.setDate(objUtility.funGetDateToSetCalenderDate());
	dteToDate.setDate(objUtility.funGetDateToSetCalenderDate());

	ExportReportPath = clsPosConfigFile.exportReportPath;

	decimalFormtFor2DecPoint = new DecimalFormat("0.00");

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
		selectQuery = "select strPosName,strPosCode from tblposmaster order by strPosCode ";
		ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(selectQuery);
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
        tblKDS = new javax.swing.JTable();
        btnExecute = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();
        btnExport = new javax.swing.JButton();
        lblCostCenterName = new javax.swing.JLabel();
        cmbWaiterName = new javax.swing.JComboBox();
        lblWaiterName = new javax.swing.JLabel();
        cmbCostCenterName = new javax.swing.JComboBox();
        lblTxtAvgProcessTme = new javax.swing.JLabel();
        lblAvgProcessTme = new javax.swing.JLabel();
        lblReportType = new javax.swing.JLabel();
        cmbReportType = new javax.swing.JComboBox();
        lblTotOrders = new javax.swing.JLabel();
        lblTxtTotOrders = new javax.swing.JLabel();
        lblDelayedOrders = new javax.swing.JLabel();
        lblTxtDelayedOrders = new javax.swing.JLabel();
        lblTotOrdersPer = new javax.swing.JLabel();
        lblTotOrderTargetAvg = new javax.swing.JLabel();
        lblTxtTotOrderTargetAvg = new javax.swing.JLabel();
        delayOrderTargetAvg = new javax.swing.JLabel();
        lblTxtDelayOrderTargetAvg = new javax.swing.JLabel();
        lblDOTAPer = new javax.swing.JLabel();
        lblTotOrderPer = new javax.swing.JLabel();
        lblDelayOrderTargetAvgPer = new javax.swing.JLabel();
        cmbType = new javax.swing.JComboBox();
        pnlSalesTotal7 = new javax.swing.JScrollPane();
        tblTotal7 = new javax.swing.JTable();
        lblWAvgTT = new javax.swing.JLabel();
        lblWAvgAT = new javax.swing.JLabel();
        lblMinimumProcessTme = new javax.swing.JLabel();
        lblMaximumProcessTime = new javax.swing.JLabel();
        lblMinimumDelayTime = new javax.swing.JLabel();
        lblMaximumDelayTime = new javax.swing.JLabel();
        lbllTxtWAvgAT = new javax.swing.JLabel();
        lbltxtWAvgTT = new javax.swing.JLabel();
        lblTxtMinimumDelayTime = new javax.swing.JLabel();
        lblTxtMaximumDelayTime = new javax.swing.JLabel();
        lblTxtMinimumProcessTme = new javax.swing.JLabel();
        lblTxtMaximumProcessTime = new javax.swing.JLabel();

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
        lblformName.setText("-Bill Wise Settlement Sales Summary Flash");
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
        lblPosCode.setText("POS           :");

        cmbPosCode.setToolTipText("Select POS");

        lblFromDate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblFromDate.setText("From Date :");

        dteFromDate.setToolTipText("Select From Date");
        dteFromDate.setPreferredSize(new java.awt.Dimension(119, 35));

        lblToDate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblToDate.setText("To Date :");

        dteToDate.setToolTipText("Select To Date");
        dteToDate.setPreferredSize(new java.awt.Dimension(119, 35));

        tblKDS.setAutoCreateRowSorter(true);
        tblKDS.setFont(new java.awt.Font("Microsoft Sans Serif", 0, 11)); // NOI18N
        tblKDS.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {

            },
            new String []
            {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tblKDS.setRowHeight(25);
        pnlDayEnd.setViewportView(tblKDS);

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

        btnExport.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnExport.setForeground(new java.awt.Color(255, 255, 255));
        btnExport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnExport.setText("Export");
        btnExport.setToolTipText("Close Window");
        btnExport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExport.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn2.png"))); // NOI18N
        btnExport.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnExportActionPerformed(evt);
            }
        });

        lblCostCenterName.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblCostCenterName.setText("Cost Center :");

        cmbWaiterName.setToolTipText("Select POS");
        cmbWaiterName.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                cmbWaiterNameActionPerformed(evt);
            }
        });

        lblWaiterName.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblWaiterName.setText("Waiter Name:");

        cmbCostCenterName.setToolTipText("Select POS");
        cmbCostCenterName.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                cmbCostCenterNameActionPerformed(evt);
            }
        });

        lblTxtAvgProcessTme.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblTxtAvgProcessTme.setText("Average Prcessing Time                   :");

        lblAvgProcessTme.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblAvgProcessTme.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblAvgProcessTme.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        lblReportType.setText("Report Type :");

        cmbReportType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Cost Center", "Group", "SubGroup", "Menu Head" }));

        lblTotOrders.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblTotOrders.setText("Total Orders                                    :");

        lblTxtTotOrders.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblTxtTotOrders.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTxtTotOrders.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        lblDelayedOrders.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblDelayedOrders.setText("Delayed Orders                 : ");

        lblTxtDelayedOrders.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblTxtDelayedOrders.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTxtDelayedOrders.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        lblTotOrdersPer.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblTotOrdersPer.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTotOrdersPer.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        lblTotOrderTargetAvg.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblTotOrderTargetAvg.setText("Average Target Time        :");
        lblTotOrderTargetAvg.setToolTipText("");

        lblTxtTotOrderTargetAvg.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblTxtTotOrderTargetAvg.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTxtTotOrderTargetAvg.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        delayOrderTargetAvg.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        delayOrderTargetAvg.setText("Delayed Order Target Time Average :");

        lblTxtDelayOrderTargetAvg.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblTxtDelayOrderTargetAvg.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTxtDelayOrderTargetAvg.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        lblDOTAPer.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblDOTAPer.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblDOTAPer.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        lblTotOrderPer.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblTotOrderPer.setText("Delay Orders Per                  :");

        lblDelayOrderTargetAvgPer.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblDelayOrderTargetAvgPer.setText("Delayed Order Target Average Per :");

        cmbType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Detail", " ", " " }));

        tblTotal7.setFont(new java.awt.Font("DejaVu Sans", 1, 14)); // NOI18N
        tblTotal7.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {
                {null, null, null, null, null, null, null, null, null}
            },
            new String []
            {
                "", "", "", "", "", "", "", "", ""
            }
        ));
        tblTotal7.setRowHeight(25);
        pnlSalesTotal7.setViewportView(tblTotal7);

        lblWAvgTT.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lblWAvgTT.setText("Weighted Avg. Target Time :");

        lblWAvgAT.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lblWAvgAT.setText("Weighted Avg. Actual Time                       :");

        lblMinimumProcessTme.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblMinimumProcessTme.setText("Minimum Preocess Time                   :");

        lblMaximumProcessTime.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblMaximumProcessTime.setText("Maximum Process Time      :");

        lblMinimumDelayTime.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblMinimumDelayTime.setText("Minimum Delayed Time                    :");

        lblMaximumDelayTime.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblMaximumDelayTime.setText("Maximum Delayed Time      :");

        lbllTxtWAvgAT.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lbllTxtWAvgAT.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lbllTxtWAvgAT.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        lbltxtWAvgTT.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lbltxtWAvgTT.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lbltxtWAvgTT.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        lblTxtMinimumDelayTime.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblTxtMinimumDelayTime.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTxtMinimumDelayTime.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        lblTxtMaximumDelayTime.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblTxtMaximumDelayTime.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTxtMaximumDelayTime.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        lblTxtMinimumProcessTme.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblTxtMinimumProcessTme.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTxtMinimumProcessTme.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        lblTxtMaximumProcessTime.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblTxtMaximumProcessTime.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTxtMaximumProcessTime.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        javax.swing.GroupLayout pnlMainLayout = new javax.swing.GroupLayout(pnlMain);
        pnlMain.setLayout(pnlMainLayout);
        pnlMainLayout.setHorizontalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblTxtAvgProcessTme, javax.swing.GroupLayout.PREFERRED_SIZE, 163, Short.MAX_VALUE)
                            .addComponent(lblTotOrders, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblTxtTotOrders, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblAvgProcessTme, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(delayOrderTargetAvg, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblMinimumProcessTme, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblMinimumDelayTime, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblWAvgAT, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lbllTxtWAvgAT, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblTxtMinimumProcessTme, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblTxtMinimumDelayTime, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblTxtDelayOrderTargetAvg, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(pnlMainLayout.createSequentialGroup()
                                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(pnlMainLayout.createSequentialGroup()
                                        .addGap(4, 4, 4)
                                        .addComponent(lblTotOrderTargetAvg, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(lblDelayedOrders, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(lblTxtTotOrderTargetAvg, javax.swing.GroupLayout.DEFAULT_SIZE, 77, Short.MAX_VALUE)
                                    .addComponent(lblTxtDelayedOrders, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(pnlMainLayout.createSequentialGroup()
                                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(lblWAvgTT, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lblMaximumDelayTime, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lblMaximumProcessTime, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(lblTxtMaximumProcessTime, javax.swing.GroupLayout.DEFAULT_SIZE, 77, Short.MAX_VALUE)
                                        .addComponent(lblTxtMaximumDelayTime, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addComponent(lbltxtWAvgTT, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(8, 8, 8)
                        .addComponent(lblTotOrderPer)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblTotOrdersPer, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addComponent(lblDelayOrderTargetAvgPer, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblDOTAPer, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMainLayout.createSequentialGroup()
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pnlSalesTotal7, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlDayEnd, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblPosCode)
                            .addComponent(lblCostCenterName))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(cmbPosCode, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cmbCostCenterName, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblWaiterName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblReportType, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(7, 7, 7)
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlMainLayout.createSequentialGroup()
                                .addComponent(cmbWaiterName, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnExecute, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnExport, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMainLayout.createSequentialGroup()
                                .addComponent(cmbReportType, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(cmbType, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(lblFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dteFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(lblToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(pnlMainLayout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE))
                            .addComponent(dteToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlMainLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {delayOrderTargetAvg, lblTotOrders, lblTxtAvgProcessTme});

        pnlMainLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {lblAvgProcessTme, lblTxtTotOrders});

        pnlMainLayout.setVerticalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(pnlMainLayout.createSequentialGroup()
                            .addComponent(cmbPosCode, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(cmbCostCenterName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(pnlMainLayout.createSequentialGroup()
                            .addComponent(lblPosCode, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(lblCostCenterName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cmbReportType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(dteFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(dteToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cmbType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(cmbWaiterName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                .addComponent(btnExport, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                .addComponent(btnExecute, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addComponent(lblReportType)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblWaiterName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlDayEnd, javax.swing.GroupLayout.PREFERRED_SIZE, 351, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3)
                .addComponent(pnlSalesTotal7, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addComponent(lblTxtTotOrders)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblAvgProcessTme, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lblTxtDelayedOrders)
                            .addComponent(lblTotOrders)
                            .addComponent(lblDelayedOrders)
                            .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(lblTotOrderPer)
                                .addComponent(lblTotOrdersPer)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lblTxtAvgProcessTme, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(lblTotOrderTargetAvg)
                                .addComponent(lblTxtTotOrderTargetAvg)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblWAvgTT, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(lblWAvgAT, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lbllTxtWAvgAT, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lbltxtWAvgTT, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMainLayout.createSequentialGroup()
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblMinimumProcessTme, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblTxtMinimumProcessTme, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblTxtMaximumProcessTime, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(lblTxtMaximumDelayTime, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblMinimumDelayTime, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(lblTxtMinimumDelayTime, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMainLayout.createSequentialGroup()
                        .addComponent(lblMaximumProcessTime, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblMaximumDelayTime, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblDelayOrderTargetAvgPer)
                    .addComponent(lblDOTAPer)
                    .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(delayOrderTargetAvg, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblTxtDelayOrderTargetAvg, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        pnlMainLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {cmbPosCode, lblFromDate, lblPosCode, lblToDate});

        pnlMainLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {dteFromDate, dteToDate});

        pnlMainLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {cmbReportType, cmbType, lblReportType, lblWaiterName});

        pnlMainLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {lblAvgProcessTme, lblDOTAPer, lblDelayOrderTargetAvgPer, lblDelayedOrders, lblTotOrderPer, lblTotOrderTargetAvg, lblTotOrders, lblTotOrdersPer, lblTxtDelayedOrders, lblTxtTotOrderTargetAvg, lblTxtTotOrders, lblWAvgTT});

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
	clsGlobalVarClass.hmActiveForms.remove("KDS Flash");
    }//GEN-LAST:event_btnCloseMouseClicked

    private void btnExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportActionPerformed

	try
	{
	    String avgProcessTime = lblAvgProcessTme.getText();
	    File theDir = new File(ExportReportPath);
	    File file = new File(ExportReportPath + "/" + exportFormName + objUtility.funGetDateInString() + ".xls");
	    if (!theDir.exists())
	    {
		theDir.mkdir();
		funExportFile(tblKDS, file, avgProcessTime);
		//sendMail();
	    }
	    else
	    {
		funExportFile(tblKDS, file, avgProcessTime);
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
	clsGlobalVarClass.hmActiveForms.remove("KDS Flash");
    }//GEN-LAST:event_btnCloseActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("KDS Flash");
    }//GEN-LAST:event_formWindowClosed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("KDS Flash");
    }//GEN-LAST:event_formWindowClosing

    private void cmbWaiterNameActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cmbWaiterNameActionPerformed
    {//GEN-HEADEREND:event_cmbWaiterNameActionPerformed
	// TODO add your handling code here:
    }//GEN-LAST:event_cmbWaiterNameActionPerformed

    private void cmbCostCenterNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbCostCenterNameActionPerformed
	// TODO add your handling code here:
    }//GEN-LAST:event_cmbCostCenterNameActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnExecute;
    private javax.swing.JButton btnExport;
    private javax.swing.JComboBox cmbCostCenterName;
    private javax.swing.JComboBox cmbPosCode;
    private javax.swing.JComboBox cmbReportType;
    private javax.swing.JComboBox cmbType;
    private javax.swing.JComboBox cmbWaiterName;
    private javax.swing.JLabel delayOrderTargetAvg;
    private com.toedter.calendar.JDateChooser dteFromDate;
    private com.toedter.calendar.JDateChooser dteToDate;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.JLabel lblAvgProcessTme;
    private javax.swing.JLabel lblCostCenterName;
    private javax.swing.JLabel lblDOTAPer;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblDelayOrderTargetAvgPer;
    private javax.swing.JLabel lblDelayedOrders;
    private javax.swing.JLabel lblFromDate;
    private javax.swing.JLabel lblHOSign;
    private javax.swing.JLabel lblMaximumDelayTime;
    private javax.swing.JLabel lblMaximumProcessTime;
    private javax.swing.JLabel lblMinimumDelayTime;
    private javax.swing.JLabel lblMinimumProcessTme;
    private javax.swing.JLabel lblModuleName;
    private javax.swing.JLabel lblPosCode;
    private javax.swing.JLabel lblPosName;
    private javax.swing.JLabel lblProductName;
    private javax.swing.JLabel lblReportType;
    private javax.swing.JLabel lblToDate;
    private javax.swing.JLabel lblTotOrderPer;
    private javax.swing.JLabel lblTotOrderTargetAvg;
    private javax.swing.JLabel lblTotOrders;
    private javax.swing.JLabel lblTotOrdersPer;
    private javax.swing.JLabel lblTxtAvgProcessTme;
    private javax.swing.JLabel lblTxtDelayOrderTargetAvg;
    private javax.swing.JLabel lblTxtDelayedOrders;
    private javax.swing.JLabel lblTxtMaximumDelayTime;
    private javax.swing.JLabel lblTxtMaximumProcessTime;
    private javax.swing.JLabel lblTxtMinimumDelayTime;
    private javax.swing.JLabel lblTxtMinimumProcessTme;
    private javax.swing.JLabel lblTxtTotOrderTargetAvg;
    private javax.swing.JLabel lblTxtTotOrders;
    private javax.swing.JLabel lblUserCode;
    private javax.swing.JLabel lblWAvgAT;
    private javax.swing.JLabel lblWAvgTT;
    private javax.swing.JLabel lblWaiterName;
    private javax.swing.JLabel lblformName;
    private javax.swing.JLabel lbllTxtWAvgAT;
    private javax.swing.JLabel lbltxtWAvgTT;
    private javax.swing.JPanel pnlBackGround;
    private javax.swing.JScrollPane pnlDayEnd;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JScrollPane pnlSalesTotal7;
    private javax.swing.JPanel pnlheader;
    private javax.swing.JTable tblKDS;
    private javax.swing.JTable tblTotal7;
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
	    SimpleDateFormat yyyyMMddDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	    Date fDate = dteFromDate.getDate();
	    Date tDate = dteToDate.getDate();

	    fromDate = yyyyMMddDateFormat.format(fDate);
	    toDate = yyyyMMddDateFormat.format(tDate);
	    String selectedPOS = cmbPosCode.getSelectedItem().toString();
	    if (selectedPOS.equalsIgnoreCase("All"))
	    {
		selectedPOSCode = selectedPOS;
	    }
	    else
	    {
		int lastIndexOf = selectedPOS.lastIndexOf(" ");
		selectedPOSCode = selectedPOS.substring(lastIndexOf + 1);
	    }

	    if (cmbReportType.getSelectedItem().toString().equalsIgnoreCase("Group"))
	    {
		funFillDataGroupWise();
	    }
	    else if (cmbReportType.getSelectedItem().toString().equalsIgnoreCase("SubGroup"))
	    {
		funFillDataSubGroupWise();
	    }
	    else if (cmbReportType.getSelectedItem().toString().equalsIgnoreCase("Menu Head"))
	    {
		funFillDataMenuHeadWise();
	    }
	    else
	    {
		funFillData();
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funFillData()
    {
	try
	{
	    funShowBelowLabels();
	    fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());
	    toDate = objUtility.funGetFromToDate(dteToDate.getDate());
	    double sumQty = 0.00;
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

	    dm.getDataVector().removeAllElements();
	    dm = new DefaultTableModel();
	    dm.addColumn("Bill No");
	    dm.addColumn("KOT No");
	    dm.addColumn("Waiter Name");
	    dm.addColumn("Item Name");
	    dm.addColumn("Qty");
	    dm.addColumn("KOT Time");
	    dm.addColumn("Process Time");
	    dm.addColumn("PickUp Time");
	    dm.addColumn("KOT-Process Time");
	    dm.addColumn("Process-PickUp Time");
	    dm.addColumn("Min Time");
	    dm.addColumn("Max Time");

	    //Map<String,List<clsBillDtl>> hmKDSFlashData=new HashMap<String,List<clsBillDtl>>();
	    Map<String, List<clsBillDtl>> hmKDSFlashData = new TreeMap<String, List<clsBillDtl>>();
	    long minProcessTime = 0, maxProcessTime = 0;
	    long minDelayedTime = 0, maxDelayedTime = 0;
	    boolean isFirstRecord = true;
	    String maxDelayTime = "", minDelayTime = "";
	    StringBuilder sbSql = new StringBuilder();

	    sbSql.append("SELECT a.strBillNo,b.strKOTNo, DATE_FORMAT(DATE(b.dteBillDate),'%d-%m-%Y') dteKOTDate,"
		    + " b.strItemName, SUM(b.dblQuantity),SUM(b.dblAmount) AS Amount,sum(b.dblAmount)-sum(b.dblDiscountAmt) AS SubTotal, "
		    + " TIME(b.dteBillDate) as tmeKOTTime,TIME(b.tmeOrderProcessing) as OrderProcessingTime, "
		    + " TIME(b.tmeOrderPickup) as OrderPickupTime,b.strItemCode "
		    + " ,if(TIME(b.tmeOrderProcessing)=time('00:00:00'),time('00:00:00'),IF(TIME(b.tmeOrderProcessing)< TIME(b.dteBillDate), ADDTIME(TIME(b.tmeOrderProcessing), TIMEDIFF('24:00:00', TIME(b.dteBillDate))), TIMEDIFF(IF(TIME(b.tmeOrderProcessing)='00:00:00', TIME(b.dteBillDate), TIME(b.tmeOrderProcessing)), TIME(b.dteBillDate)))) AS processtimediff "
		    + " ,if(TIME(b.tmeOrderPickup)=time('00:00:00'),time('00:00:00'),IF(TIME(b.tmeOrderPickup)< TIME(b.tmeOrderProcessing), ADDTIME(TIME(b.tmeOrderPickup), TIMEDIFF('24:00:00', TIME(b.tmeOrderProcessing))), TIMEDIFF(IF(TIME(b.tmeOrderPickup)='00:00:00', TIME(b.tmeOrderProcessing), TIME(b.tmeOrderPickup)), TIME(b.tmeOrderProcessing)))) AS pickuptimediff "
		    + " ,e.strCostCenterName,f.strWShortName "
		    + " ,time(CONCAT('00',':',c.intProcTimeMin,':','00'))intProcTimeMin "
		    + ",time(CONCAT('00',':',c.tmeTargetMiss,':','00'))tmeTargetMiss  "
		    + " FROM tblbillhd a,tblbilldtl b,tblitemmaster c,tblmenuitempricingdtl d,tblcostcentermaster e,tblwaitermaster f "
		    + " WHERE a.strBillNo=b.strBillNo AND DATE(a.dtBillDate)= DATE(b.dtBillDate) "
		    + " and b.strItemCode=c.strItemCode and c.strItemCode=d.strItemCode "
		    + " and (a.strPOSCode=d.strPosCode or d.strPosCode='All') and d.strCostCenterCode=e.strCostCenterCode and a.strWaiterNo=f.strWaiterNo "
		    + " and DATE(a.dtBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "'  ");
	    if (!cmbCostCenterName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sbSql.append("and d.strCostCenterCode='" + mapCostCenterNameCode.get(cmbCostCenterName.getSelectedItem().toString()) + "'  ");
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sbSql.append(" and a.strPOSCode='" + selectedPOSCode + "'  ");
	    }
	    if (!cmbWaiterName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sbSql.append(" and a.strWaiterNo='" + mapWaiterNameCode.get(cmbWaiterName.getSelectedItem().toString()) + "'  ");
	    }

	    sbSql.append("GROUP BY a.strBillNo,b.strItemCode ");
	    sbSql.append(" Order By a.strBillNo desc");
	    ResultSet rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());

	    String time = "", processTime = "", targetTime = "";
	    long masterProcesTime = 0;
	    long sumOfKOTProcTime = 0, transProcessTime = 0, sumOfDelayOrders = 0, masterTargetTime = 0, sumOfDelayOrderTargetTime = 0;
	    long sumOfMasterTarTime = 0, sumMasterProcessTime = 0, sumOfMasterProcTimeXMasterTarTime = 0, sumOfMasterProcTimeXTransProcTime = 0;
	    long totDelayOrderTotAvg = 0;
	    int countOfDelayOrder = 0;
	    long noOfItemsCount = 0;
	    Date date1, dateProcessTime, itemTargetTme;
	    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
	    timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	    SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");
	    fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
	    while (rsSales.next())
	    {
		clsBillDtl objBill = new clsBillDtl();
		objBill.setStrBillNo(rsSales.getString(1));           //Bill No
		objBill.setStrKOTNo(rsSales.getString(2));            //KOT No
		objBill.setDteBillDate(rsSales.getString(3));        //Kot Date
		objBill.setStrItemName(rsSales.getString(4));        //Item Name
		objBill.setDblQuantity(rsSales.getDouble(5));        //Qty
		objBill.setDblAmount(rsSales.getDouble(6));          //Amount
		objBill.setDblBillAmt(rsSales.getDouble(7));         //Sub Total
		objBill.setTmeBillTime(rsSales.getString(8));        //Kot Time
		objBill.setTmeOrderProcessing(rsSales.getString(9)); //Process Time
		objBill.setTmeBillSettleTime(rsSales.getString(10));  //Puckup Time
		objBill.setStrProcessTimeDiff(rsSales.getString(12));  // diff kot & process Time
		objBill.setStrPickUpTimeDiff(rsSales.getString(13));  // diff process & pickup Time
		objBill.setStrCounterCode(rsSales.getString(14));     // costCenterName
		objBill.setStrWShortName(rsSales.getString(15));        // waiterName
		objBill.setStrItemProcessTime(rsSales.getString(16));        // item process time
		targetTime = rsSales.getString(17);
		itemTargetTme = timeFormat.parse(targetTime);
		objBill.setStrItemTargetTime(targetTime);        // item target time
		masterTargetTime = itemTargetTme.getTime();
		sumOfMasterTarTime = sumOfMasterTarTime + itemTargetTme.getTime();

		masterProcesTime = timeFormat.parse(rsSales.getString(16)).getTime();
		sumMasterProcessTime = sumMasterProcessTime + masterProcesTime;

		processTime = rsSales.getString(12);
		dateProcessTime = timeFormat.parse(processTime);
		transProcessTime = dateProcessTime.getTime();

		if (isFirstRecord)
		{
		    minProcessTime = transProcessTime;
		    maxProcessTime = transProcessTime;

		    minDelayedTime = transProcessTime;
		    maxDelayedTime = transProcessTime;
		}

		if (transProcessTime < minProcessTime)
		{
		    minProcessTime = transProcessTime;
		}
		if (transProcessTime > maxProcessTime)
		{
		    maxProcessTime = transProcessTime;
		}

		if (transProcessTime > masterTargetTime)
		{
		    long delayTime = (transProcessTime - masterTargetTime);

		    if (delayTime < minDelayedTime)
		    {
			minDelayedTime = delayTime;
		    }

		    if (delayTime > maxDelayedTime)
		    {
			maxDelayedTime = delayTime;
		    }

		    sumOfDelayOrders = sumOfDelayOrders + transProcessTime;
		    sumOfDelayOrderTargetTime = sumOfDelayOrderTargetTime + masterTargetTime;
		    countOfDelayOrder++;
		}

		noOfItemsCount = noOfItemsCount + 1;
		time = rsSales.getString(12);
		date1 = timeFormat.parse(time);
		sumOfKOTProcTime = sumOfKOTProcTime + date1.getTime();

		String key = rsSales.getString(14) + "!" + rsSales.getString(3);

		List<clsBillDtl> arrListBillDtl = new ArrayList<clsBillDtl>();
		if (hmKDSFlashData.containsKey(key))
		{
		    arrListBillDtl = hmKDSFlashData.get(key);
		    arrListBillDtl.add(objBill);
		}
		else
		{
		    arrListBillDtl.add(objBill);
		}
		hmKDSFlashData.put(key, arrListBillDtl);

		sumOfMasterProcTimeXMasterTarTime = sumOfMasterProcTimeXMasterTarTime + (masterProcesTime * masterTargetTime);
		sumOfMasterProcTimeXTransProcTime = sumOfMasterProcTimeXTransProcTime + (masterProcesTime * transProcessTime);

		isFirstRecord = false;
	    }
	    rsSales.close();

	    sbSql.setLength(0);
	    sbSql.append("SELECT a.strBillNo,b.strKOTNo,DATE_FORMAT(DATE(b.dteBillDate),'%d-%m-%Y') dteKOTDate,"
		    + " b.strItemName, SUM(b.dblQuantity),SUM(b.dblAmount) AS Amount,sum(b.dblAmount)-sum(b.dblDiscountAmt) AS SubTotal, "
		    + " TIME(b.dteBillDate) as tmeKOTTime,TIME(b.tmeOrderProcessing) as OrderProcessingTime, "
		    + " TIME(b.tmeOrderPickup) as OrderPickupTime,b.strItemCode "
		    + " ,if(TIME(b.tmeOrderProcessing)=time('00:00:00'),time('00:00:00'),IF(TIME(b.tmeOrderProcessing)< TIME(b.dteBillDate), ADDTIME(TIME(b.tmeOrderProcessing), TIMEDIFF('24:00:00', TIME(b.dteBillDate))), TIMEDIFF(IF(TIME(b.tmeOrderProcessing)='00:00:00', TIME(b.dteBillDate), TIME(b.tmeOrderProcessing)), TIME(b.dteBillDate)))) AS processtimediff "
		    + " ,if(TIME(b.tmeOrderPickup)=time('00:00:00'),time('00:00:00'),IF(TIME(b.tmeOrderPickup)< TIME(b.tmeOrderProcessing), ADDTIME(TIME(b.tmeOrderPickup), TIMEDIFF('24:00:00', TIME(b.tmeOrderProcessing))), TIMEDIFF(IF(TIME(b.tmeOrderPickup)='00:00:00', TIME(b.tmeOrderProcessing), TIME(b.tmeOrderPickup)), TIME(b.tmeOrderProcessing)))) AS pickuptimediff "
		    + " ,e.strCostCenterName,f.strWShortName"
		    + ",time(CONCAT('00',':',c.intProcTimeMin,':','00'))intProcTimeMin "
		    + ",time(CONCAT('00',':',c.tmeTargetMiss,':','00'))tmeTargetMiss   "
		    + " FROM tblqbillhd a,tblqbilldtl b,tblitemmaster c,tblmenuitempricingdtl d,tblcostcentermaster e,tblwaitermaster f "
		    + " WHERE a.strBillNo=b.strBillNo AND DATE(a.dtBillDate)= DATE(b.dtBillDate) "
		    + " and b.strItemCode=c.strItemCode and c.strItemCode=d.strItemCode "
		    + " and (a.strPOSCode=d.strPosCode or d.strPosCode='All') and d.strCostCenterCode=e.strCostCenterCode and a.strWaiterNo=f.strWaiterNo "
		    + " AND DATE(a.dtBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' ");
	    if (!cmbCostCenterName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sbSql.append("and d.strCostCenterCode='" + mapCostCenterNameCode.get(cmbCostCenterName.getSelectedItem().toString()) + "'  ");
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sbSql.append(" and a.strPOSCode='" + selectedPOSCode + "'  ");
	    }
	    if (!cmbWaiterName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sbSql.append(" and a.strWaiterNo='" + mapWaiterNameCode.get(cmbWaiterName.getSelectedItem().toString()) + "'  ");
	    }
	    sbSql.append("GROUP BY a.strBillNo,b.strItemCode ");
	    sbSql.append(" Order By a.strBillNo desc");
	    ResultSet rsQSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());

	    while (rsQSales.next())
	    {
		clsBillDtl objBill = new clsBillDtl();
		objBill.setStrBillNo(rsQSales.getString(1));           //Bill No
		objBill.setStrKOTNo(rsQSales.getString(2));            //KOT No
		objBill.setDteBillDate(rsQSales.getString(3));        //Kot Date
		objBill.setStrItemName(rsQSales.getString(4));        //Item Name
		objBill.setDblQuantity(rsQSales.getDouble(5));        //Qty
		objBill.setDblAmount(rsQSales.getDouble(6));          //Amount
		objBill.setDblBillAmt(rsQSales.getDouble(7));         //Sub Total
		objBill.setTmeBillTime(rsQSales.getString(8));        //Kot Time
		objBill.setTmeOrderProcessing(rsQSales.getString(9)); //Process Time
		objBill.setTmeBillSettleTime(rsQSales.getString(10)); //Puckup Time                
		objBill.setStrProcessTimeDiff(rsQSales.getString(12));  // diff kot & process Time

		//System.out.println(rsQSales.getString(1)+"   "+rsQSales.getString(2));
		objBill.setStrPickUpTimeDiff(rsQSales.getString(13));  // diff process & pickup Time
		objBill.setStrCounterCode(rsQSales.getString(14));     // costCenterName
		objBill.setStrWShortName(rsQSales.getString(15));        // waiterName
		objBill.setStrItemProcessTime(rsQSales.getString(16));        // item process time
		targetTime = rsQSales.getString(17);
		itemTargetTme = timeFormat.parse(targetTime);
		objBill.setStrItemTargetTime(targetTime);        // item target time
		masterTargetTime = itemTargetTme.getTime();
		sumOfMasterTarTime = sumOfMasterTarTime + itemTargetTme.getTime();
		processTime = rsQSales.getString(12);
		dateProcessTime = timeFormat.parse(processTime);
		transProcessTime = dateProcessTime.getTime();

		masterProcesTime = timeFormat.parse(rsQSales.getString(16)).getTime();
		sumMasterProcessTime = sumMasterProcessTime + masterProcesTime;
		if (isFirstRecord)
		{
		    minProcessTime = transProcessTime;
		    maxProcessTime = transProcessTime;

		    minDelayedTime = transProcessTime;
		    maxDelayedTime = transProcessTime;
		}

		if (transProcessTime < minProcessTime)
		{
		    minProcessTime = transProcessTime;
		}
		if (transProcessTime > maxProcessTime)
		{
		    maxProcessTime = transProcessTime;
		}

		if (transProcessTime > masterTargetTime)
		{
		    long delayTime = (transProcessTime - masterTargetTime);

		    if (delayTime < minDelayedTime)
		    {
			minDelayedTime = delayTime;
		    }

		    if (delayTime > maxDelayedTime)
		    {
			maxDelayedTime = delayTime;
		    }

		    sumOfDelayOrders = sumOfDelayOrders + transProcessTime;
		    sumOfDelayOrderTargetTime = sumOfDelayOrderTargetTime + masterTargetTime;
		    countOfDelayOrder++;
		}

		noOfItemsCount = noOfItemsCount + 1;

		time = rsQSales.getString(12);
		date1 = timeFormat.parse(time);
		sumOfKOTProcTime = sumOfKOTProcTime + date1.getTime();

		List<clsBillDtl> arrListBillDtl = new ArrayList<clsBillDtl>();
		//String key=rsQSales.getString(3)+"!"+rsQSales.getString(14);
		String key = rsQSales.getString(14) + "!" + rsQSales.getString(3);

		if (hmKDSFlashData.containsKey(key))
		{
		    arrListBillDtl = hmKDSFlashData.get(key);
		    arrListBillDtl.add(objBill);
		}
		else
		{
		    arrListBillDtl.add(objBill);
		}
		hmKDSFlashData.put(key, arrListBillDtl);

		sumOfMasterProcTimeXMasterTarTime = sumOfMasterProcTimeXMasterTarTime + (masterProcesTime * masterTargetTime);
		sumOfMasterProcTimeXTransProcTime = sumOfMasterProcTimeXTransProcTime + (masterProcesTime * transProcessTime);

		isFirstRecord = false;

	    }
	    rsQSales.close();
	    String avgProTime = "", quantity = "", DOAvg = "", totOrdTarAvg = "", delayOrderTargAvg = "";
	    String totDelayOrderTotAvgPer = "", weightedAvgTargetTime = "", weightedAvgActualTime = "";
	    long first = 0, second = 0, longWeightedAvgTargetTime = 0, longWeightedAvgActualTime = 0, longAvgMasterTarTime;
	    double finalDelayedOrder = 0;
	    double totOrderePer = 0;
	    long finalDota = 0;
	    if (!isFirstRecord)
	    {
		finalDelayedOrder = countOfDelayOrder;

		longWeightedAvgTargetTime = sumOfMasterProcTimeXMasterTarTime / sumMasterProcessTime;
		longWeightedAvgActualTime = sumOfMasterProcTimeXTransProcTime / sumMasterProcessTime;
		longAvgMasterTarTime = sumOfMasterTarTime / noOfItemsCount;

		weightedAvgTargetTime = fmt.format(new Date(longWeightedAvgTargetTime));
		weightedAvgActualTime = fmt.format(new Date(longWeightedAvgActualTime));
		first = (sumMasterProcessTime * sumOfMasterTarTime) / sumMasterProcessTime;
		avgProTime = fmt.format(new Date(sumOfKOTProcTime / noOfItemsCount));
		if (noOfItemsCount != 0)
		{
		    avgProTime = fmt.format(new Date(sumOfKOTProcTime / noOfItemsCount));
		    totOrderePer = ((finalDelayedOrder / noOfItemsCount) * 100);
		    totOrdTarAvg = fmt.format(new Date(longAvgMasterTarTime));
		    second = sumOfMasterTarTime / noOfItemsCount;
		}
		else
		{
		    avgProTime = fmt.format(new Date(sumOfKOTProcTime));
		    totOrderePer = ((finalDelayedOrder) * 100);
		    totOrdTarAvg = fmt.format(new Date(sumOfMasterTarTime));
		    second = sumOfMasterTarTime;
		}

		if (countOfDelayOrder != 0)
		{
		    DOAvg = fmt.format(new Date(sumOfDelayOrderTargetTime / countOfDelayOrder));
		    delayOrderTargAvg = fmt.format(new Date(sumOfDelayOrderTargetTime / countOfDelayOrder));
		}
		else
		{
		    DOAvg = fmt.format(new Date(sumOfDelayOrderTargetTime));
		    delayOrderTargAvg = fmt.format(new Date(sumOfDelayOrderTargetTime));

		}

		double finalPer = (double) (longAvgMasterTarTime / (double) longWeightedAvgTargetTime) * 100;

		totDelayOrderTotAvgPer = String.valueOf(decimalFormtFor2DecPoint.format(finalPer));

	    }
	    //System.out.println("The sum is "+date3);

	    System.out.println(hmKDSFlashData.keySet());

	    for (Map.Entry<String, List<clsBillDtl>> entry : hmKDSFlashData.entrySet())
	    {
		Object[] records = new Object[16];
		records[0] = "<html><font color=red ><b>" + entry.getKey().split("!")[0] + "</b></font></html>";
		records[1] = "<html><font color=red ><b>" + entry.getKey().split("!")[1] + "</b></font></html>";
		dm.addRow(records);

		for (int cnt = 0; cnt < entry.getValue().size(); cnt++)
		{
		    clsBillDtl objBill = entry.getValue().get(cnt);

		    records = new Object[14];
		    records[0] = objBill.getStrBillNo();
		    records[1] = objBill.getStrKOTNo();
		    records[2] = objBill.getStrWShortName();
		    records[3] = objBill.getStrItemName();
		    records[4] = objBill.getDblQuantity() + "  ";
		    records[5] = " " + objBill.getTmeBillTime();
		    records[6] = objBill.getTmeOrderProcessing();
		    records[7] = objBill.getTmeBillSettleTime();
		    records[8] = objBill.getStrProcessTimeDiff();
		    records[9] = objBill.getStrPickUpTimeDiff();
		    records[10] = objBill.getStrItemProcessTime();
		    records[11] = objBill.getStrItemTargetTime();

		    sumQty = sumQty + objBill.getDblQuantity();
		    dm.addRow(records);
		}

		records = new Object[14];
		dm.addRow(records);
	    }
	    tblKDS.setModel(dm);

	    DefaultTableCellRenderer rightRenderer1 = new DefaultTableCellRenderer();
	    rightRenderer1.setHorizontalAlignment(JLabel.RIGHT);
	    DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
	    centerRenderer.setHorizontalAlignment(JLabel.CENTER);

	    tblKDS.getColumnModel().getColumn(4).setCellRenderer(rightRenderer1);
	    tblKDS.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

	    tblKDS.getColumnModel().getColumn(0).setPreferredWidth(70);// Bill No
	    tblKDS.getColumnModel().getColumn(1).setPreferredWidth(70);// KOT No
	    tblKDS.getColumnModel().getColumn(2).setPreferredWidth(90);// waiter Name
	    tblKDS.getColumnModel().getColumn(3).setPreferredWidth(190);// Item Name
	    tblKDS.getColumnModel().getColumn(4).setPreferredWidth(40);// Qty
	    tblKDS.getColumnModel().getColumn(5).setPreferredWidth(80);// KOT Time
	    tblKDS.getColumnModel().getColumn(6).setPreferredWidth(80);// Process Time
	    tblKDS.getColumnModel().getColumn(7).setPreferredWidth(80);// PickedUp Time
	    tblKDS.getColumnModel().getColumn(8).setPreferredWidth(95);// KOT Time - Process Time
	    tblKDS.getColumnModel().getColumn(9).setPreferredWidth(95);// Process Time - Pickup Time
	    tblKDS.getColumnModel().getColumn(10).setPreferredWidth(95);// Item Process Time
	    tblKDS.getColumnModel().getColumn(11).setPreferredWidth(95);// Item Target Time

	    final TableRowSorter<TableModel> sorter;
	    sorter = new TableRowSorter<TableModel>(dm);
	    tblKDS.setRowSorter(sorter);

	    lblAvgProcessTme.setText(avgProTime);
	    lblTxtDelayedOrders.setText(String.valueOf(finalDelayedOrder));
	    lblTxtTotOrders.setText(String.valueOf(noOfItemsCount));
	    lblTotOrdersPer.setText(String.valueOf(Math.round(totOrderePer) + " %"));
	    lblTxtTotOrderTargetAvg.setText(totOrdTarAvg);
	    lblTxtDelayOrderTargetAvg.setText(delayOrderTargAvg);
	    lblDOTAPer.setText(String.valueOf(totDelayOrderTotAvgPer) + " %");
	    lbllTxtWAvgAT.setText(weightedAvgActualTime);
	    lbltxtWAvgTT.setText(weightedAvgTargetTime);
	    lblTxtMinimumProcessTme.setText(String.valueOf(fmt.format(new Date(minProcessTime))));
	    lblTxtMaximumProcessTime.setText(String.valueOf(fmt.format(new Date(maxProcessTime))));
	    lblTxtMinimumDelayTime.setText(String.valueOf(fmt.format(new Date(minDelayedTime))));
	    lblTxtMaximumDelayTime.setText(String.valueOf(fmt.format(new Date(maxDelayedTime))));

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funFillDataGroupWise()
    {
	try
	{

	    funShowBelowLabels();
	    fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());
	    toDate = objUtility.funGetFromToDate(dteToDate.getDate());
	    double sumQty = 0.00;
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

	    dm.getDataVector().removeAllElements();
	    dm = new DefaultTableModel();

	    dm.addColumn("Group Name");
	    dm.addColumn("Qty");
	    dm.addColumn("Average Process Time");

	    //Map<String,List<clsBillDtl>> hmKDSFlashData=new HashMap<String,List<clsBillDtl>>();
	    Map<String, List<clsBillDtl>> hmKDSFlashData = new TreeMap<String, List<clsBillDtl>>();

	    StringBuilder sbSql = new StringBuilder();

	    sbSql.append("SELECT a.strBillNo,b.strKOTNo, DATE_FORMAT(DATE(b.dteBillDate),'%d-%m-%Y') dteKOTDate,"
		    + " b.strItemName, SUM(b.dblQuantity),SUM(b.dblAmount) AS Amount,sum(b.dblAmount)-sum(b.dblDiscountAmt) AS SubTotal, "
		    + " TIME(b.dteBillDate) as tmeKOTTime,TIME(b.tmeOrderProcessing) as OrderProcessingTime, "
		    + " TIME(b.tmeOrderPickup) as OrderPickupTime,b.strItemCode, "
		    + " if(TIME(b.tmeOrderProcessing)<TIME(b.dteBillDate),ADDTIME(TIME(b.tmeOrderProcessing),TIMEDIFF('24:00:00',TIME(b.dteBillDate))),TIMEDIFF(IF(TIME(b.tmeOrderProcessing)='00:00:00', TIME(b.dteBillDate), TIME(b.tmeOrderProcessing)), TIME(b.dteBillDate)) )  AS processtimediff, "
		    + " TIMEDIFF(if(TIME(b.tmeOrderPickup)='00:00:00',TIME(b.tmeOrderProcessing),TIME(b.tmeOrderPickup)),TIME(b.tmeOrderProcessing)) AS pickuptimediff,"
		    + " e.strCostCenterName,f.strWShortName,g.strGroupName "
		    + " FROM tblbillhd a,tblbilldtl b,tblitemmaster c,tblmenuitempricingdtl d,tblcostcentermaster e,tblwaitermaster f "
		    + " ,tblgrouphd g,tblsubgrouphd h"
		    + " WHERE a.strBillNo=b.strBillNo AND DATE(a.dtBillDate)= DATE(b.dtBillDate) "
		    + " and b.strItemCode=c.strItemCode and c.strItemCode=d.strItemCode "
		    + " and (a.strPOSCode=d.strPosCode or d.strPosCode='All') and d.strCostCenterCode=e.strCostCenterCode and a.strWaiterNo=f.strWaiterNo "
		    + " and DATE(a.dtBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "'  "
		    + " and c.strSubGroupCode = h.strSubGroupCode "
		    + " AND h.strGroupCode = g.strGroupCode ");
	    if (!cmbCostCenterName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sbSql.append(" and d.strCostCenterCode='" + mapCostCenterNameCode.get(cmbCostCenterName.getSelectedItem().toString()) + "'  ");
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sbSql.append(" and a.strPOSCode='" + selectedPOSCode + "'  ");
	    }
	    if (!cmbWaiterName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sbSql.append(" and a.strWaiterNo='" + mapWaiterNameCode.get(cmbWaiterName.getSelectedItem().toString()) + "'  ");
	    }

	    sbSql.append(" GROUP BY g.strGroupCode  ");
	    sbSql.append(" Order By  g.strGroupCode DESC");
	    ResultSet rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());

	    String time = "", timeForGroupWise = "";
	    long sum = 0, sumOfGroupWise = 0;
	    int i = 0;
	    String date4 = "";
	    int qty = 0, quantity = 0;
	    Date date1;
	    Date dateForGroup;
	    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
	    timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	    while (rsSales.next())
	    {
		clsBillDtl objBill = new clsBillDtl();

		qty = qty + rsSales.getInt(5);
		quantity = quantity + rsSales.getInt(5);
		objBill.setDblQuantity(qty);
		objBill.setStrGroupName(rsSales.getString(16));
		time = rsSales.getString(12);
		date1 = timeFormat.parse(time);
		sum = sum + date1.getTime();

		timeForGroupWise = rsSales.getString(12);
		dateForGroup = timeFormat.parse(timeForGroupWise);
		sumOfGroupWise = dateForGroup.getTime();

		date4 = timeFormat.format(new Date(sumOfGroupWise / qty));
		objBill.setTmeOrderProcessing(date4);
		//String key=rsSales.getString(3)+"!"+rsSales.getString(14);
		String key = rsSales.getString(16) + "!" + rsSales.getString(3);

		List<clsBillDtl> arrListBillDtl = new ArrayList<clsBillDtl>();
		if (hmKDSFlashData.containsKey(key))
		{
		    arrListBillDtl = hmKDSFlashData.get(key);
		    arrListBillDtl.add(objBill);
		}
		else
		{
		    arrListBillDtl.add(objBill);
		}
		hmKDSFlashData.put(key, arrListBillDtl);

		i++;
	    }

	    sbSql.setLength(0);
	    sbSql.append("SELECT a.strBillNo,b.strKOTNo,DATE_FORMAT(DATE(b.dteBillDate),'%d-%m-%Y') dteKOTDate,"
		    + " b.strItemName, SUM(b.dblQuantity),SUM(b.dblAmount) AS Amount,sum(b.dblAmount)-sum(b.dblDiscountAmt) AS SubTotal, "
		    + " TIME(b.dteBillDate) as tmeKOTTime,TIME(b.tmeOrderProcessing) as OrderProcessingTime, "
		    + " TIME(b.tmeOrderPickup) as OrderPickupTime,b.strItemCode, "
		    + " if(TIME(b.tmeOrderProcessing)<TIME(b.dteBillDate),ADDTIME(TIME(b.tmeOrderProcessing),TIMEDIFF('24:00:00',TIME(b.dteBillDate))),TIMEDIFF(IF(TIME(b.tmeOrderProcessing)='00:00:00', TIME(b.dteBillDate), TIME(b.tmeOrderProcessing)), TIME(b.dteBillDate)) )  AS processtimediff, "
		    + " TIMEDIFF(if(TIME(b.tmeOrderPickup)='00:00:00',TIME(b.tmeOrderProcessing),TIME(b.tmeOrderPickup)),TIME(b.tmeOrderProcessing)) AS pickuptimediff,"
		    + " e.strCostCenterName,f.strWShortName,g.strGroupName "
		    + " FROM tblqbillhd a,tblqbilldtl b,tblitemmaster c,tblmenuitempricingdtl d,tblcostcentermaster e,tblwaitermaster f "
		    + " ,tblgrouphd g,tblsubgrouphd h"
		    + " WHERE a.strBillNo=b.strBillNo AND DATE(a.dtBillDate)= DATE(b.dtBillDate) "
		    + " and b.strItemCode=c.strItemCode and c.strItemCode=d.strItemCode "
		    + " and (a.strPOSCode=d.strPosCode or d.strPosCode='All') and d.strCostCenterCode=e.strCostCenterCode and a.strWaiterNo=f.strWaiterNo "
		    + " AND DATE(a.dtBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
		    + " and c.strSubGroupCode = h.strSubGroupCode "
		    + " AND h.strGroupCode = g.strGroupCode ");
	    if (!cmbCostCenterName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sbSql.append(" and d.strCostCenterCode='" + mapCostCenterNameCode.get(cmbCostCenterName.getSelectedItem().toString()) + "'  ");
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sbSql.append(" and a.strPOSCode='" + selectedPOSCode + "'  ");
	    }
	    if (!cmbWaiterName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sbSql.append(" and a.strWaiterNo='" + mapWaiterNameCode.get(cmbWaiterName.getSelectedItem().toString()) + "'  ");
	    }
	    sbSql.append(" GROUP BY g.strGroupCode ");
	    sbSql.append(" Order By g.strGroupCode DESC");
	    ResultSet rsQSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());

	    while (rsQSales.next())
	    {
		clsBillDtl objBill = new clsBillDtl();

		qty = qty + rsQSales.getInt(5);
		quantity = quantity + rsQSales.getInt(5);
		objBill.setDblQuantity(qty);
		objBill.setStrGroupName(rsQSales.getString(16));
		time = rsQSales.getString(12);
		date1 = timeFormat.parse(time);
		sum = sum + date1.getTime();

		timeForGroupWise = rsQSales.getString(12);
		dateForGroup = timeFormat.parse(timeForGroupWise);
		sumOfGroupWise = dateForGroup.getTime();
		date4 = timeFormat.format(new Date(sumOfGroupWise / qty));
		objBill.setTmeOrderProcessing(date4);

		List<clsBillDtl> arrListBillDtl = new ArrayList<clsBillDtl>();
		//String key=rsQSales.getString(3)+"!"+rsQSales.getString(14);
		String key = rsQSales.getString(16) + "!" + rsQSales.getString(3);

		if (hmKDSFlashData.containsKey(key))
		{
		    arrListBillDtl = hmKDSFlashData.get(key);
		    arrListBillDtl.add(objBill);
		}
		else
		{
		    arrListBillDtl.add(objBill);
		}
		hmKDSFlashData.put(key, arrListBillDtl);

		i++;
	    }
	    String date3 = "";
	    if (i > 0)
	    {
		date3 = timeFormat.format(new Date(sum / qty));
	    }
	    //System.out.println("The sum is "+date3);

	    System.out.println(hmKDSFlashData.keySet());

	    for (Map.Entry<String, List<clsBillDtl>> entry : hmKDSFlashData.entrySet())
	    {
		Object[] records = new Object[3];

		for (int cnt = 0; cnt < entry.getValue().size(); cnt++)
		{
		    clsBillDtl objBill = entry.getValue().get(cnt);

		    records = new Object[3];

		    records[0] = entry.getKey().split("!")[0];
		    records[1] = objBill.getDblQuantity() + "  ";
		    records[2] = objBill.getTmeOrderProcessing();

		    sumQty = sumQty + objBill.getDblQuantity();
		    dm.addRow(records);
		}

		records = new Object[3];
		dm.addRow(records);
	    }
	    tblKDS.setModel(dm);

	    DefaultTableCellRenderer rightRenderer1 = new DefaultTableCellRenderer();
	    rightRenderer1.setHorizontalAlignment(JLabel.RIGHT);
	    tblKDS.getColumnModel().getColumn(1).setCellRenderer(rightRenderer1);

	    DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
	    centerRenderer.setHorizontalAlignment(JLabel.CENTER);

	    tblKDS.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
	    tblKDS.getColumnModel().getColumn(0).setPreferredWidth(300);
	    tblKDS.getColumnModel().getColumn(1).setPreferredWidth(100);
	    tblKDS.getColumnModel().getColumn(2).setPreferredWidth(200);

	    lblAvgProcessTme.setText(date3);
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funFillDataSubGroupWise()
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

	    if (cmbType.getSelectedItem().toString().equalsIgnoreCase("Detail"))
	    {
		funShowBelowLabels();
		funFillSubGroupWiseDetailData();
	    }
	    else
	    {
		funFillSubGroupWiseSummaryData();
		tblTotal7.setVisible(true);

	    }

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funFillDataMenuHeadWise()
    {
	try
	{

	    funShowBelowLabels();
	    fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());
	    toDate = objUtility.funGetFromToDate(dteToDate.getDate());
	    double sumQty = 0.00;
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

	    dm.getDataVector().removeAllElements();
	    dm = new DefaultTableModel();

	    dm.addColumn("Menu Head Name");
	    dm.addColumn("Qty");
	    dm.addColumn("Average Process Time");

	    //Map<String,List<clsBillDtl>> hmKDSFlashData=new HashMap<String,List<clsBillDtl>>();
	    Map<String, List<clsBillDtl>> hmKDSFlashData = new TreeMap<String, List<clsBillDtl>>();

	    StringBuilder sbSql = new StringBuilder();

	    sbSql.append("SELECT a.strBillNo,b.strKOTNo, DATE_FORMAT(DATE(b.dteBillDate),'%d-%m-%Y') dteKOTDate,"
		    + " b.strItemName, SUM(b.dblQuantity),SUM(b.dblAmount) AS Amount,sum(b.dblAmount)-sum(b.dblDiscountAmt) AS SubTotal, "
		    + " TIME(b.dteBillDate) as tmeKOTTime,TIME(b.tmeOrderProcessing) as OrderProcessingTime, "
		    + " TIME(b.tmeOrderPickup) as OrderPickupTime,b.strItemCode, "
		    + " if(TIME(b.tmeOrderProcessing)<TIME(b.dteBillDate),ADDTIME(TIME(b.tmeOrderProcessing),TIMEDIFF('24:00:00',TIME(b.dteBillDate))),TIMEDIFF(IF(TIME(b.tmeOrderProcessing)='00:00:00', TIME(b.dteBillDate), TIME(b.tmeOrderProcessing)), TIME(b.dteBillDate)) )  AS processtimediff, "
		    + " TIMEDIFF(if(TIME(b.tmeOrderPickup)='00:00:00',TIME(b.tmeOrderProcessing),TIME(b.tmeOrderPickup)),TIME(b.tmeOrderProcessing)) AS pickuptimediff,"
		    + " e.strCostCenterName,f.strWShortName,g.strMenuName "
		    + " FROM tblbillhd a,tblbilldtl b,tblitemmaster c,tblmenuitempricingdtl d,tblcostcentermaster e,tblwaitermaster f "
		    + " ,tblmenuhd g"
		    + " WHERE a.strBillNo=b.strBillNo AND DATE(a.dtBillDate)= DATE(b.dtBillDate) "
		    + " and b.strItemCode=c.strItemCode and c.strItemCode=d.strItemCode "
		    + " and (a.strPOSCode=d.strPosCode or d.strPosCode='All') and d.strCostCenterCode=e.strCostCenterCode and a.strWaiterNo=f.strWaiterNo "
		    + " and DATE(a.dtBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "'  "
		    + " and d.strMenuCode = g.strMenuCode ");
	    if (!cmbCostCenterName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sbSql.append("and d.strCostCenterCode='" + mapCostCenterNameCode.get(cmbCostCenterName.getSelectedItem().toString()) + "'  ");
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sbSql.append(" and a.strPOSCode='" + selectedPOSCode + "'  ");
	    }
	    if (!cmbWaiterName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sbSql.append(" and a.strWaiterNo='" + mapWaiterNameCode.get(cmbWaiterName.getSelectedItem().toString()) + "'  ");
	    }

	    sbSql.append(" GROUP BY g.strMenuCode  ");
	    sbSql.append(" Order By g.strMenuCode DESC");
	    ResultSet rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());

	    String time = "", timeForGroupWise = "";
	    long sum = 0, sumOfGroupWise = 0;
	    int i = 0;
	    String date4 = "";
	    int qty = 0, quantity = 0;
	    Date date1;
	    Date dateForGroup;
	    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
	    timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	    while (rsSales.next())
	    {
		clsBillDtl objBill = new clsBillDtl();

		qty = qty + rsSales.getInt(5);
		quantity = quantity + rsSales.getInt(5);
		objBill.setDblQuantity(qty);
		objBill.setStrGroupName(rsSales.getString(16));
		time = rsSales.getString(12);
		date1 = timeFormat.parse(time);
		sum = sum + date1.getTime();

		timeForGroupWise = rsSales.getString(12);
		dateForGroup = timeFormat.parse(timeForGroupWise);
		sumOfGroupWise = dateForGroup.getTime();

		date4 = timeFormat.format(new Date(sumOfGroupWise / qty));
		objBill.setTmeOrderProcessing(date4);
		//String key=rsSales.getString(3)+"!"+rsSales.getString(14);
		String key = rsSales.getString(16) + "!" + rsSales.getString(3);

		List<clsBillDtl> arrListBillDtl = new ArrayList<clsBillDtl>();
		if (hmKDSFlashData.containsKey(key))
		{
		    arrListBillDtl = hmKDSFlashData.get(key);
		    arrListBillDtl.add(objBill);
		}
		else
		{
		    arrListBillDtl.add(objBill);
		    qty = 0;
		}
		hmKDSFlashData.put(key, arrListBillDtl);

		/*sbSql.setLength(0);
                sbSql.append("SELECT a.strBillNo,b.strModifierName, b.strItemCode, "
                        + "SUM(b.dblQuantity), SUM(b.dblAmount) AS Amount,sum(b.dblAmount)-sum(b.dblDiscPer) AS SubTotal "
                        + "FROM tblbillhd a,tblbillmodifierdtl b "
                        + "WHERE a.strBillNo=b.strBillNo and a.strBillNo='" + rsSales.getString(1) + "' and LEFT(b.strItemCode,7)='" + rsSales.getString(11) + "' "
                        + "GROUP BY a.strBillNo,b.strItemCode  "
                        + "ORDER BY a.strBillNo desc");
                ResultSet rsModLiveSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
                while (rsModLiveSales.next())
                {
                    objBill = new clsBillDtl();

                    qty = qty + rsModLiveSales.getInt(4);
                    quantity = quantity + rsModLiveSales.getInt(4);
                    objBill.setDblQuantity(qty);
                    objBill.setStrGroupName(rsSales.getString(16));
                    time = rsSales.getString(12);
                    date1 = timeFormat.parse(time);
                    sum = sum + date1.getTime();

                    timeForGroupWise = rsSales.getString(12);
                    dateForGroup = timeFormat.parse(timeForGroupWise);
                    sumOfGroupWise = dateForGroup.getTime();
                    date4 = timeFormat.format(new Date(sumOfGroupWise / qty));
                    objBill.setTmeOrderProcessing(date4);

                    //key=rsSales.getString(3)+"!"+rsSales.getString(14);
                    key = rsSales.getString(16) + "!" + rsSales.getString(3);
                    if (hmKDSFlashData.containsKey(key))
                    {
                        arrListBillDtl = hmKDSFlashData.get(key);
                        arrListBillDtl.add(objBill);
                    }
                    else
                    {
                        arrListBillDtl = new ArrayList<clsBillDtl>();
                        arrListBillDtl.add(objBill);
                        qty = 0;
                    }
                    hmKDSFlashData.put(key, arrListBillDtl);
                }*/
		i++;

	    }

	    sbSql.setLength(0);
	    sbSql.append("SELECT a.strBillNo,b.strKOTNo,DATE_FORMAT(DATE(b.dteBillDate),'%d-%m-%Y') dteKOTDate,"
		    + " b.strItemName, SUM(b.dblQuantity),SUM(b.dblAmount) AS Amount,sum(b.dblAmount)-sum(b.dblDiscountAmt) AS SubTotal, "
		    + " TIME(b.dteBillDate) as tmeKOTTime,TIME(b.tmeOrderProcessing) as OrderProcessingTime, "
		    + " TIME(b.tmeOrderPickup) as OrderPickupTime,b.strItemCode, "
		    + " if(TIME(b.tmeOrderProcessing)<TIME(b.dteBillDate),ADDTIME(TIME(b.tmeOrderProcessing),TIMEDIFF('24:00:00',TIME(b.dteBillDate))),TIMEDIFF(IF(TIME(b.tmeOrderProcessing)='00:00:00', TIME(b.dteBillDate), TIME(b.tmeOrderProcessing)), TIME(b.dteBillDate)) )  AS processtimediff, "
		    + " TIMEDIFF(if(TIME(b.tmeOrderPickup)='00:00:00',TIME(b.tmeOrderProcessing),TIME(b.tmeOrderPickup)),TIME(b.tmeOrderProcessing)) AS pickuptimediff,"
		    + " e.strCostCenterName,f.strWShortName,g.strMenuName "
		    + " FROM tblqbillhd a,tblqbilldtl b,tblitemmaster c,tblmenuitempricingdtl d,tblcostcentermaster e,tblwaitermaster f "
		    + " ,tblmenuhd g"
		    + " WHERE a.strBillNo=b.strBillNo AND DATE(a.dtBillDate)= DATE(b.dtBillDate) "
		    + " and b.strItemCode=c.strItemCode and c.strItemCode=d.strItemCode "
		    + " and (a.strPOSCode=d.strPosCode or d.strPosCode='All') and d.strCostCenterCode=e.strCostCenterCode and a.strWaiterNo=f.strWaiterNo "
		    + " AND DATE(a.dtBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
		    + " and d.strMenuCode = g.strMenuCode ");
	    if (!cmbCostCenterName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sbSql.append("and d.strCostCenterCode='" + mapCostCenterNameCode.get(cmbCostCenterName.getSelectedItem().toString()) + "'  ");
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sbSql.append(" and a.strPOSCode='" + selectedPOSCode + "'  ");
	    }
	    if (!cmbWaiterName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sbSql.append(" and a.strWaiterNo='" + mapWaiterNameCode.get(cmbWaiterName.getSelectedItem().toString()) + "'  ");
	    }
	    sbSql.append(" GROUP BY g.strMenuCode ");
	    sbSql.append(" Order By g.strMenuCode DESC");
	    ResultSet rsQSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());

	    while (rsQSales.next())
	    {
		clsBillDtl objBill = new clsBillDtl();

		qty = qty + rsQSales.getInt(5);
		quantity = quantity + rsQSales.getInt(5);
		objBill.setDblQuantity(qty);
		objBill.setStrGroupName(rsQSales.getString(16));
		time = rsQSales.getString(12);
		date1 = timeFormat.parse(time);
		sum = sum + date1.getTime();

		timeForGroupWise = rsQSales.getString(12);
		dateForGroup = timeFormat.parse(timeForGroupWise);
		sumOfGroupWise = dateForGroup.getTime();
		date4 = timeFormat.format(new Date(sumOfGroupWise / qty));
		objBill.setTmeOrderProcessing(date4);

		List<clsBillDtl> arrListBillDtl = new ArrayList<clsBillDtl>();
		//String key=rsQSales.getString(3)+"!"+rsQSales.getString(14);
		String key = rsQSales.getString(16) + "!" + rsQSales.getString(3);

		if (hmKDSFlashData.containsKey(key))
		{
		    arrListBillDtl = hmKDSFlashData.get(key);
		    arrListBillDtl.add(objBill);
		}
		else
		{
		    arrListBillDtl.add(objBill);
		}
		hmKDSFlashData.put(key, arrListBillDtl);

		/*String sqlQMod = "SELECT a.strBillNo,b.strModifierName, b.strItemCode, "
                        + "SUM(b.dblQuantity), SUM(b.dblAmount) AS Amount,sum(b.dblAmount)-sum(b.dblDiscPer) AS SubTotal "
                        + "FROM tblqbillhd a,tblqbillmodifierdtl b "
                        + "WHERE a.strBillNo=b.strBillNo and a.strBillNo='" + rsQSales.getString(1) + "' and LEFT(b.strItemCode,7)='" + rsQSales.getString(11) + "' "
                        + "GROUP BY a.strBillNo,b.strItemCode  "
                        + "ORDER BY a.strBillNo desc";
                ResultSet rsQModSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlQMod);

                while (rsQModSales.next())
                {
                    objBill = new clsBillDtl();

                    qty = qty + rsQModSales.getInt(4);
                    quantity = quantity + rsQModSales.getInt(4);
                    objBill.setDblQuantity(qty);
                    objBill.setStrGroupName(rsQSales.getString(16));
                    time = rsQSales.getString(12);
                    date1 = timeFormat.parse(time);
                    sum = sum + date1.getTime();

                    timeForGroupWise = rsQSales.getString(12);
                    dateForGroup = timeFormat.parse(timeForGroupWise);
                    sumOfGroupWise = dateForGroup.getTime();
                    date4 = timeFormat.format(new Date(sumOfGroupWise / qty));
                    objBill.setTmeOrderProcessing(date4);

                    //key=rsQSales.getString(3)+"!"+rsQSales.getString(14);
                    key = rsQSales.getString(16) + "!" + rsQSales.getString(3);

                    if (hmKDSFlashData.containsKey(key))
                    {
                        arrListBillDtl = hmKDSFlashData.get(key);
                        arrListBillDtl.add(objBill);
                    }
                    else
                    {
                        arrListBillDtl = new ArrayList<clsBillDtl>();
                        arrListBillDtl.add(objBill);
                    }
                    hmKDSFlashData.put(key, arrListBillDtl);
                }*/
		i++;
	    }
	    String date3 = "";
	    if (i > 0)
	    {
		date3 = timeFormat.format(new Date(sum / quantity));
	    }
	    //System.out.println("The sum is "+date3);

	    System.out.println(hmKDSFlashData.keySet());

	    for (Map.Entry<String, List<clsBillDtl>> entry : hmKDSFlashData.entrySet())
	    {
		Object[] records = new Object[14];

		for (int cnt = 0; cnt < entry.getValue().size(); cnt++)
		{
		    clsBillDtl objBill = entry.getValue().get(cnt);

		    records = new Object[3];

		    records[0] = entry.getKey().split("!")[0];
		    records[1] = objBill.getDblQuantity() + "  ";
		    records[2] = objBill.getTmeOrderProcessing();

		    sumQty = sumQty + objBill.getDblQuantity();
		    dm.addRow(records);
		}

		records = new Object[3];
		dm.addRow(records);
	    }
	    tblKDS.setModel(dm);

	    DefaultTableCellRenderer rightRenderer1 = new DefaultTableCellRenderer();
	    rightRenderer1.setHorizontalAlignment(JLabel.RIGHT);
	    tblKDS.getColumnModel().getColumn(1).setCellRenderer(rightRenderer1);

	    DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
	    centerRenderer.setHorizontalAlignment(JLabel.CENTER);

	    tblKDS.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
	    tblKDS.getColumnModel().getColumn(0).setPreferredWidth(300);
	    tblKDS.getColumnModel().getColumn(1).setPreferredWidth(100);
	    tblKDS.getColumnModel().getColumn(2).setPreferredWidth(200);

	    lblAvgProcessTme.setText(date3);
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    /*
    private static HashMap sortByValues(HashMap map) { 
       List list = new LinkedList(map.entrySet());
       // Defined Custom Comparator here
       Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
               return ((Comparable) ((Map.Entry) (o1)).getValue())
                  .compareTo(((Map.Entry) (o2)).getValue());
            }
       });
    }*/
    private void funExportFile(JTable tblDayWiseSalesSummary, File file, String avgProcessTime)
    {
	try
	{
	    vSalesReportExcelColLength = new java.util.Vector();
	    int columnCount = tblDayWiseSalesSummary.getModel().getColumnCount();
	    for (int i = 0; i < columnCount; i++)
	    {
		vSalesReportExcelColLength.add("10#Left");
	    }

	    WritableWorkbook workbook1 = Workbook.createWorkbook(file);
	    WritableSheet sheet1 = workbook1.createSheet("First Sheet", 0);
	    TableModel model = tblDayWiseSalesSummary.getModel();
	    sheet1.addCell(new Label(0, 0, reportName));

	    for (int i = 0; i < model.getColumnCount(); i++)
	    {
		Label column = new Label(i, 1, model.getColumnName(i));
		System.out.println("Count= = " + i);
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
		    //System.out.println("Bill= "+model.getValueAt(k, 0).toString()+"\tCounter k= "+k+"\tCounter J= "+j);
		    String data = "";
		    if (null == model.getValueAt(k, j))
		    {
			data = "";
		    }
		    else if (model.getValueAt(k, j).toString().startsWith("<html>"))
		    {
			data = model.getValueAt(k, j).toString();
			data = data.replaceAll("<html><font color=red ><b>", "");
			data = data.replaceAll("</b></font></html>", "");
		    }
		    else
		    {
			data = model.getValueAt(k, j).toString();
		    }
		    Label row = new Label(j, i + 1, data);
		    sheet1.setColumnView(j, data.length() + colLen);
		    sheet1.addCell(row);
		}
		k++;
	    }
	    funAddLastOfExportReport(workbook1, avgProcessTime);
	    workbook1.write();
	    workbook1.close();

	    //Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + ExportReportPath + "/" + exportFormName + objUtility.funGetDateInString() + ".xls");
	    //sendMail();
//            clsExportDocument exportDocument = new clsExportDocument();
//            exportDocument.funExportToPDF(tblDayWiseSalesSummary, tblTotal, exportFormName);
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

    private void funAddLastOfExportReport(WritableWorkbook workbook1, String avgProcessTime)
    {
	try
	{
	    int i = 0, j = 0, LastIndexReport = 0;
	    String delayedOrders = "", totalOrders = "", delayedOrderAvergae = "", totalOrderTargetAverage = "";
	    String delayedOrderTargetAverage = "", totOrdersPer = "", delayOrderTotPer = "";
	    delayedOrders = lblTxtDelayedOrders.getText();
	    totalOrders = lblTxtTotOrders.getText();
	    totOrdersPer = lblTotOrdersPer.getText();
	    totalOrderTargetAverage = lblTxtTotOrderTargetAvg.getText();
	    delayedOrderTargetAverage = lblTxtDelayOrderTargetAvg.getText();
	    delayOrderTotPer = lblDOTAPer.getText();

	    if (exportFormName.equals("SalesSummary"))
	    {
		LastIndexReport = 5;
	    }

	    WritableSheet sheet2 = workbook1.getSheet(0);
	    int r = sheet2.getRows();

	    if (!(cmbType.getSelectedItem().toString().equalsIgnoreCase("Summary")))
	    {
		WritableSheet sheet3 = workbook1.getSheet(0);
		r = sheet3.getRows();
		Label row1 = new Label(1, r + 1, "Total Orders :" + totalOrders);
		sheet3.addCell(row1);

		WritableSheet sheet4 = workbook1.getSheet(0);
		r = sheet4.getRows();
		Label row2 = new Label(1, r + 1, "Delayed Orders :" + delayedOrders);
		sheet4.addCell(row2);

		Label row3 = new Label(1, r + 1, "Delayed Order Per :" + lblTotOrdersPer.getText().trim());
		sheet4.addCell(row3);

		WritableSheet sheet6 = workbook1.getSheet(0);
		r = sheet6.getRows();
		Label row4 = new Label(1, r + 1, "Average Prcessing Time :" + lblAvgProcessTme.getText().trim());
		sheet6.addCell(row4);

		WritableSheet sheet7 = workbook1.getSheet(0);
		r = sheet7.getRows();
		Label row5 = new Label(1, r + 1, "Average Target Time :" + lblTxtTotOrderTargetAvg.getText().trim());
		sheet7.addCell(row5);

		WritableSheet sheet8 = workbook1.getSheet(0);
		r = sheet8.getRows();
		Label row6 = new Label(1, r + 1, "Weighted Avg. Actual Time :" + lbllTxtWAvgAT.getText().trim());
		sheet8.addCell(row6);

		Label row7 = new Label(1, r + 1, "Weighted Avg. Target Time :" + lbltxtWAvgTT.getText().trim());
		sheet8.addCell(row7);

		WritableSheet sheet10 = workbook1.getSheet(0);
		r = sheet10.getRows();
		Label row8 = new Label(1, r + 1, "Minimum Preocess Time :" + lblTxtMinimumProcessTme.getText().trim());
		sheet10.addCell(row8);

		WritableSheet sheet11 = workbook1.getSheet(0);
		r = sheet11.getRows();
		Label row9 = new Label(1, r + 1, "Maximum Process Time :" + lblTxtMaximumProcessTime.getText().trim());
		sheet11.addCell(row9);

		WritableSheet sheet12 = workbook1.getSheet(0);
		r = sheet12.getRows();
		Label row10 = new Label(1, r + 1, "Minimum Delayed Time :" + lblTxtMinimumDelayTime.getText().trim());
		sheet12.addCell(row10);

		WritableSheet sheet13 = workbook1.getSheet(0);
		r = sheet13.getRows();
		Label row11 = new Label(1, r + 1, "Maximum Delayed Time :" + lblTxtMaximumDelayTime.getText().trim());
		sheet13.addCell(row11);

		WritableSheet sheet14 = workbook1.getSheet(0);
		r = sheet14.getRows();
		Label row12 = new Label(1, r + 1, "Delayed Order Target Time Avg. :" + lblTxtDelayOrderTargetAvg.getText().trim());
		sheet14.addCell(row12);

		WritableSheet sheet15 = workbook1.getSheet(0);
		r = sheet15.getRows();
		Label row13 = new Label(1, r + 1, "Delayed Order Target Avg. Per :" + lblDOTAPer.getText().trim());
		sheet15.addCell(row13);

	    }
	    if (cmbReportType.getSelectedItem().toString().equalsIgnoreCase("SubGroup") && cmbType.getSelectedItem().toString().equalsIgnoreCase("Summary"))
	    {
		WritableSheet sheet11 = workbook1.getSheet(0);

		r = sheet11.getRows();
		//System.out.println(r);
		//System.out.println("Row Cnt="+tblTotal.getRowCount());
		//System.out.println("Col Cnt="+tblTotal.getColumnCount());
		for (i = r; i < tblTotal7.getRowCount() + r; i++)
		{
		    for (j = 0; j < tblTotal7.getColumnCount(); j++)
		    {
			Label row = new Label(LastIndexReport + j, i + 1, tblTotal7.getValueAt(0, j).toString());
			sheet2.addCell(row);
		    }
		}
	    }

	    WritableSheet sheet12 = workbook1.getSheet(0);
	    r = sheet12.getRows();
	    Formatter fmt = new Formatter();
	    Calendar cal = Calendar.getInstance();
	    fmt.format("%tr", cal);
	    Label row = new Label(1, r + 2, " Created On : " + objUtility.funGetDateInString() + " At : " + fmt + " By : " + clsGlobalVarClass.gUserCode + " ");
	    sheet12.addCell(row);
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funFillTaxData(String sqlTax)
    {
	try
	{
	    ResultSet rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlTax);
	    while (rsSales.next())
	    {
		for (int tblRow = 0; tblRow < tblKDS.getRowCount(); tblRow++)
		{
		    if (tblKDS.getValueAt(tblRow, 0).toString().equalsIgnoreCase(rsSales.getString(1)))
		    {
			for (int tblCol = 3; tblCol < tblKDS.getColumnCount(); tblCol++)
			{
			    if (tblKDS.getColumnName(tblCol).toUpperCase().equals(rsSales.getString(2).toUpperCase()))
			    {
				if (null == tblKDS.getValueAt(tblRow, tblCol))
				{
				    tblKDS.setValueAt(rsSales.getString(3), tblRow, tblCol);
				}
				else
				{
				    Double value = Double.parseDouble(tblKDS.getValueAt(tblRow, tblCol).toString()) + rsSales.getDouble(3);
				    tblKDS.setValueAt(value, tblRow, tblCol);
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

    /*
    private void funFillTableForItemGroupWise()
    {
        StringBuffer sbSql = new StringBuffer();

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

            Vector<String> billNoCol = new Vector<>();
            Vector<String> posCol = new Vector<>();
            Vector<String> dateCol = new Vector<>();
            //Map<String, String> mapBillNo = new HashMap<String, String>();
            mapSettlemetWiseAmt = new HashMap<>();
            mapAllTaxes=new HashMap<>();

            String taxCalType = "";

            //fill Q Bill No and POS  
            sbSql.setLength(0);
            sbSql.append("select a.strBillNo,DATE_FORMAT(a.dteBillDate,'%d-%m-%Y') as date "
                    + "from "
                    + "tblqbillhd a,tblqbillsettlementdtl b,tblsettelmenthd c,tblqbilldtl d ,tblitemmaster e,tblsubgrouphd f,tblgrouphd g "
                    + "where  a.strBillNo=b.strBillNo  and a.strBillNo=d.strBillNo and b.strBillNo=d.strBillNo "
                    + "and d.strItemCode=e.strItemCode and e.strSubGroupCode=f.strSubGroupCode and f.strGroupCode=g.strGroupCode  "
                    + "and date(a.dteBillDate)=date(b.dteBillDate) "
                    + "and a.strClientCode=b.strClientCode  "
                    + "and b.strSettlementCode=c.strSettelmentCode "
                    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
            if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
            {
                sbSql.append("and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ");
            }
            if (!selectedPOSCode.equalsIgnoreCase("All"))
            {
                sbSql.append(" and a.strPOSCode='" + selectedPOSCode + "' ");
            }
            if (!cmbCostCenterName.getSelectedItem().toString().equalsIgnoreCase("All"))
            {
                sbSql.append(" and c.strSettelmentCode='" + mapSettlementNameCode.get(cmbCostCenterName.getSelectedItem().toString()) + "' ");
            }
            if (!cmbWaiterName.getSelectedItem().toString().equalsIgnoreCase("All"))
            {
                sbSql.append(" and f.strGroupCode='" + mapGroupNameCode.get(cmbWaiterName.getSelectedItem().toString()) + "' ");
            }
            sbSql.append("group by a.strClientCode,a.strBillNo "
                    + "order by a.strClientCode,a.strPOSCOde,a.strBillNo,a.dteBillDate; ");
            ResultSet rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
            while (rsSales.next())
            {
                //mapBillNo.put(rsSales.getString(1), rsSales.getString(2));
                billNoCol.add(rsSales.getString(1));
                dateCol.add(rsSales.getString(2));
                posCol.add(selectedPOSName.toUpperCase());
            }
            rsSales.close();

            //fill Live Bill No and POS     
            sbSql.setLength(0);
            sbSql.append("select a.strBillNo,DATE_FORMAT(a.dteBillDate,'%d-%m-%Y') as date "
                    + "from "
                    + "tblbillhd a,tblbillsettlementdtl b,tblsettelmenthd c,tblbilldtl d ,tblitemmaster e,tblsubgrouphd f,tblgrouphd g  "
                    + "where  a.strBillNo=b.strBillNo  and a.strBillNo=d.strBillNo and b.strBillNo=d.strBillNo "
                    + "and d.strItemCode=e.strItemCode and e.strSubGroupCode=f.strSubGroupCode and f.strGroupCode=g.strGroupCode "
                    + "and date(a.dteBillDate)=date(b.dteBillDate) "
                    + "and a.strClientCode=b.strClientCode "
                    + "and b.strSettlementCode=c.strSettelmentCode "
                    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
            if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
            {
                sbSql.append("and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ");
            }
            if (!selectedPOSCode.equalsIgnoreCase("All"))
            {
                sbSql.append(" and a.strPOSCode='" + selectedPOSCode + "' ");
            }
            if (!cmbCostCenterName.getSelectedItem().toString().equalsIgnoreCase("All"))
            {
                sbSql.append(" and c.strSettelmentCode='" + mapSettlementNameCode.get(cmbCostCenterName.getSelectedItem().toString()) + "' ");
            }
            if (!cmbWaiterName.getSelectedItem().toString().equalsIgnoreCase("All"))
            {
                sbSql.append(" and f.strGroupCode='" + mapGroupNameCode.get(cmbWaiterName.getSelectedItem().toString()) + "' ");
            }
            sbSql.append("group by a.strClientCode,a.strBillNo "
                    + "order by a.strClientCode,a.strPOSCOde,a.strBillNo,a.dteBillDate; ");
            rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
            while (rsSales.next())
            {
                billNoCol.add(rsSales.getString(1));
                dateCol.add(rsSales.getString(2));
                posCol.add(selectedPOSName.toUpperCase());
            }
            rsSales.close();

      
            dm.addColumn("BILL NO", billNoCol);
            dm.addColumn("DATE", dateCol);
            dm.addColumn("POS", posCol);
            totalDm.addColumn("TOTALS");
            totalDm.addColumn("");
            totalDm.addColumn("POS");

            //add group columns
            String sqlGroups = "select a.strGroupName from tblgrouphd a ";
            if (!cmbWaiterName.getSelectedItem().toString().equalsIgnoreCase("All"))
            {
                sqlGroups+="where a.strGroupCode='" + mapGroupNameCode.get(cmbWaiterName.getSelectedItem().toString()) + "' ";
            }
            ResultSet rsGroups = clsGlobalVarClass.dbMysql.executeResultSet(sqlGroups);
            while (rsGroups.next())
            {
                dm.addColumn(rsGroups.getString(1).toUpperCase());
                totalDm.addColumn(rsGroups.getString(1).toUpperCase());
            }
            rsGroups.close();

            int cntArrLen = 0;
            //fill Live settlement whoes amt>0
            sbSql.setLength(0);
            sbSql.append("SELECT c.strSettelmentDesc "
                    + "FROM tblbillhd a,tblbillsettlementdtl b,tblsettelmenthd c "
                    + "WHERE a.strBillNo=b.strBillNo  "
                    + "and date(a.dteBillDate)=date(b.dteBillDate) "
                    + "and a.strClientCode=b.strClientCode   "
                    + "AND b.strSettlementCode=c.strSettelmentCode   "
                    + "and b.dblSettlementAmt>0 "
                    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
            if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
            {
                sbSql.append("and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ");
            }
            if (!selectedPOSCode.equalsIgnoreCase("All"))
            {
                sbSql.append(" and a.strPOSCode='" + selectedPOSCode + "' ");
            }
            if (!cmbCostCenterName.getSelectedItem().toString().equalsIgnoreCase("All"))
            {
                sbSql.append(" and c.strSettelmentCode='" + mapSettlementNameCode.get(cmbCostCenterName.getSelectedItem().toString()) + "' ");
            }
            sbSql.append("GROUP BY strSettelmentDesc "
                    + "ORDER BY c.strSettelmentDesc; ");
            rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
            while (rsSales.next())
            {
                //mapSettlements.put(rsSales.getString(1).toUpperCase(), rsSales.getString(1).toUpperCase());
                dm.addColumn(rsSales.getString(1).toUpperCase());
                totalDm.addColumn(rsSales.getString(1).toUpperCase());
                cntArrLen++;
            }
            rsSales.close();

            //fill Q settlement whoes amt>0
            sbSql.setLength(0);
            sbSql.append("SELECT c.strSettelmentDesc "
                    + "FROM tblqbillhd a,tblqbillsettlementdtl b,tblsettelmenthd c "
                    + "WHERE a.strBillNo=b.strBillNo  "
                    + "and date(a.dteBillDate)=date(b.dteBillDate) "
                    + "and a.strClientCode=b.strClientCode   "
                    + "AND b.strSettlementCode=c.strSettelmentCode "
                    + "and b.dblSettlementAmt>0 "
                    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
            if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
            {
                sbSql.append("and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ");
            }
            if (!selectedPOSCode.equalsIgnoreCase("All"))
            {
                sbSql.append(" and a.strPOSCode='" + selectedPOSCode + "' ");
            }
            if (!cmbCostCenterName.getSelectedItem().toString().equalsIgnoreCase("All"))
            {
                sbSql.append(" and c.strSettelmentCode='" + mapSettlementNameCode.get(cmbCostCenterName.getSelectedItem().toString()) + "' ");
            }
            sbSql.append("GROUP BY strSettelmentDesc "
                    + "ORDER BY c.strSettelmentDesc; ");
            rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
            while (rsSales.next())
            {
                //mapSettlements.put(rsSales.getString(1).toUpperCase(), rsSales.getString(1).toUpperCase());
                dm.addColumn(rsSales.getString(1).toUpperCase());
                totalDm.addColumn(rsSales.getString(1).toUpperCase());
            }
            rsSales.close();

           
            
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

            cntArrLen = cntArrLen + 3;
            Object[] arrObjRecords1 = new Object[cntArrLen];
            arrObjRecords1[0] = "TOTALS";
            arrObjRecords1[1] = "";
            arrObjRecords1[2] = selectedPOSName.toUpperCase();
            for (int cnt = 3; cnt < cntArrLen; cnt++)
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
            tblKDS.setRowHeight(25);
            tblKDS.setModel(dm);
            tblKDS.setAutoscrolls(true);
            tblKDS.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

            String columnNameForBillDtl = "sum(b.dblAmount)-sum(b.dblDiscountAmt)";
            if (taxCalType.equalsIgnoreCase("Backward"))
            {
                columnNameForBillDtl = "sum(b.dblAmount)-sum(b.dblDiscountAmt)-sum(b.dblTaxAmount)";
            }

            //fill Q data group 
            sqlGroups = "select a.strBillNo,g.strGroupName," + columnNameForBillDtl + " "
                    + "from tblqbillhd a,tblqbilldtl b,tblitemmaster e "
                    + ",tblsubgrouphd f ,tblgrouphd g "
                    + "where a.strBillNo=b.strBillNo  "
                    + "and date(a.dteBillDate)=date(b.dteBillDate) "
                    + "and a.strClientCode=b.strClientCode  "
                    + "and b.strItemCode=e.strItemCode "
                    + "and e.strSubGroupCode=f.strSubGroupCode "
                    + "and f.strGroupCode=g.strGroupCode "
                    + "AND b.dblAmount>0  "
                    + "AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' and '" + toDate + "' ";
            if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
            {
                sqlGroups += "and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ";
            }
            if (!selectedPOSCode.equalsIgnoreCase("All"))
            {
                sqlGroups = sqlGroups + " and a.strPOSCode='" + selectedPOSCode + "' ";
            }
            if (!cmbWaiterName.getSelectedItem().toString().equalsIgnoreCase("All"))
            {
                 sqlGroups = sqlGroups + " and g.strGroupCode='" + mapGroupNameCode.get(cmbWaiterName.getSelectedItem().toString()) + "' ";
            }

            sqlGroups = sqlGroups + "GROUP BY a.strClientCode,a.strBillNo,g.strGroupCode,g.strGroupName ";
            funFillGroupWiseSalesData(sqlGroups);

            //fill Q modifier data group 
            sqlGroups = "SELECT a.strBillNo,g.strGroupName, SUM(h.dblAmount)- SUM(h.dblDiscAmt) "
                    + "FROM tblqbillhd a,tblitemmaster e,tblsubgrouphd f,tblgrouphd g,tblqbillmodifierdtl h "
                    + "WHERE a.strBillNo=h.strBillNo  "
                    + "and date(a.dteBillDate)=date(h.dteBillDate) "
                    + "AND a.strClientCode=h.strClientCode "
                    + "AND e.strSubGroupCode=f.strSubGroupCode "
                    + "AND f.strGroupCode=g.strGroupCode "
                    + "AND a.strBillNo=h.strBillNo "
                    + "AND e.strItemCode=LEFT(h.strItemCode,7) "
                    + "and h.dblAmount>0 "
                    + "AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' and '" + toDate + "' ";
            if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
            {
                sqlGroups += "and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ";
            }
            if (!selectedPOSCode.equalsIgnoreCase("All"))
            {
                sqlGroups = sqlGroups + " and a.strPOSCode='" + selectedPOSCode + "' ";
            }
            if (!cmbWaiterName.getSelectedItem().toString().equalsIgnoreCase("All"))
            {
                 sqlGroups = sqlGroups + " and g.strGroupCode='" + mapGroupNameCode.get(cmbWaiterName.getSelectedItem().toString()) + "' ";
            }

            sqlGroups = sqlGroups + "GROUP BY a.strClientCode,a.strBillNo,g.strGroupCode,g.strGroupName ";
            funFillGroupWiseSalesData(sqlGroups);

            //fill Q Data           
            String sqlTransRecords = "select a.strBillNo,c.strSettelmentDesc,sum(b.dblSettlementAmt) "
                    + "from "
                    + "tblqbillhd a,tblqbillsettlementdtl b,tblsettelmenthd c "
                    + "where  "
                    + "a.strBillNo=b.strBillNo  "
                    + "and date(a.dteBillDate)=date(b.dteBillDate) "
                    + "and a.strClientCode=b.strClientCode   "
                    + "and b.strSettlementCode=c.strSettelmentCode "
                    + "and b.dblSettlementAmt>0 "
                    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ";
            if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
            {
                sqlTransRecords += "and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ";
            }
            if (!selectedPOSCode.equalsIgnoreCase("All"))
            {
                sqlTransRecords = sqlTransRecords + " and a.strPOSCode='" + selectedPOSCode + "' ";
            }
            if (!cmbCostCenterName.getSelectedItem().toString().equalsIgnoreCase("All"))
            {
                sqlTransRecords = sqlTransRecords + " and c.strSettelmentCode='" + mapSettlementNameCode.get(cmbCostCenterName.getSelectedItem().toString()) + "' ";
            }
            sqlTransRecords = sqlTransRecords + "group by a.strClientCode,a.strBillNo,b.strSettlementCode "
                    + "order by a.strBillNo,b.strSettlementCode;";
            funFillSettlementData(sqlTransRecords);

            String sqlTax = "select a.strBillNo,c.strTaxDesc,sum(b.dblTaxAmount) "
                    + "from "
                    + "tblqbillhd a,tblqbilltaxdtl b,tbltaxhd c "
                    + "where a.strBillNo=b.strBillNo  "
                    + "and date(a.dteBillDate)=date(b.dteBillDate) "
                    + "and a.strClientCode=b.strClientCode   "
                    + "and b.strTaxCode=c.strTaxCode "
                    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ";
            if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
            {
                sqlTax += "and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ";
            }
            if (!selectedPOSCode.equalsIgnoreCase("All"))
            {
                sqlTax = sqlTax + " and a.strPOSCode='" + selectedPOSCode + "' ";
            }
            sqlTax = sqlTax + "group by a.strClientCode,a.strBillNo,b.strTaxCode "
                    + "order by a.strClientCode,a.strBillNo,b.strTaxCode;  ";
            funFillTaxData(sqlTax);

            //fill live data group             
            sqlGroups = "select a.strBillNo,g.strGroupName," + columnNameForBillDtl + " "
                    + "from tblbillhd a,tblbilldtl b,tblitemmaster e "
                    + ",tblsubgrouphd f ,tblgrouphd g "
                    + "where a.strBillNo=b.strBillNo  "
                    + "and date(a.dteBillDate)=date(b.dteBillDate) "
                    + "and a.strClientCode=b.strClientCode  "
                    + "and b.strItemCode=e.strItemCode "
                    + "and e.strSubGroupCode=f.strSubGroupCode "
                    + "and f.strGroupCode=g.strGroupCode "
                    + "AND b.dblAmount>0  "
                    + "AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' and '" + toDate + "' ";
            if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
            {
                sqlGroups += "and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ";
            }
            if (!selectedPOSCode.equalsIgnoreCase("All"))
            {
                sqlGroups = sqlGroups + " and a.strPOSCode='" + selectedPOSCode + "' ";
            }
            sqlGroups = sqlGroups + "GROUP BY a.strClientCode,a.strBillNo,g.strGroupCode,g.strGroupName ";
            funFillGroupWiseSalesData(sqlGroups);

            //fill live modifier data group 
            sqlGroups = "SELECT a.strBillNo,g.strGroupName, SUM(h.dblAmount)- SUM(h.dblDiscAmt) "
                    + "FROM tblbillhd a,tblitemmaster e,tblsubgrouphd f,tblgrouphd g,tblbillmodifierdtl h "
                    + "WHERE a.strBillNo=h.strBillNo  "
                    + "and date(a.dteBillDate)=date(h.dteBillDate) "
                    + "AND a.strClientCode=h.strClientCode "
                    + "AND e.strSubGroupCode=f.strSubGroupCode "
                    + "AND f.strGroupCode=g.strGroupCode "
                    + "AND a.strBillNo=h.strBillNo "
                    + "AND e.strItemCode=LEFT(h.strItemCode,7) "
                    + "and h.dblAmount>0 "
                    + "AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' and '" + toDate + "' ";
            if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
            {
                sqlGroups += "and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ";
            }
            if (!selectedPOSCode.equalsIgnoreCase("All"))
            {
                sqlGroups = sqlGroups + " and a.strPOSCode='" + selectedPOSCode + "' ";
            }
            sqlGroups = sqlGroups + "GROUP BY a.strClientCode,a.strBillNo,g.strGroupCode,g.strGroupName ";
            funFillGroupWiseSalesData(sqlGroups);

            //fill live Data           
            sqlTransRecords = "select a.strBillNo,c.strSettelmentDesc,sum(b.dblSettlementAmt) "
                    + "from "
                    + "tblbillhd a,tblbillsettlementdtl b,tblsettelmenthd c "
                    + "where  "
                    + "a.strBillNo=b.strBillNo  "
                    + "and date(a.dteBillDate)=date(b.dteBillDate) "
                    + "and a.strClientCode=b.strClientCode   "
                    + "and b.strSettlementCode=c.strSettelmentCode "
                    + "and b.dblSettlementAmt>0 "
                    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ";
            if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
            {
                sqlTransRecords += "and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ";
            }
            if (!selectedPOSCode.equalsIgnoreCase("All"))
            {
                sqlTransRecords = sqlTransRecords + " and a.strPOSCode='" + selectedPOSCode + "' ";
            }
            if (!cmbCostCenterName.getSelectedItem().toString().equalsIgnoreCase("All"))
            {
                sqlTransRecords = sqlTransRecords + " and c.strSettelmentCode='" + mapSettlementNameCode.get(cmbCostCenterName.getSelectedItem().toString()) + "' ";
            }
            sqlTransRecords = sqlTransRecords + "group by a.strClientCode,a.strBillNo,b.strSettlementCode "
                    + "order by a.strBillNo,b.strSettlementCode;";
            funFillSettlementData(sqlTransRecords);

            sqlTax = "select a.strBillNo,c.strTaxDesc,sum(b.dblTaxAmount) "
                    + "from "
                    + "tblbillhd a,tblbilltaxdtl b,tbltaxhd c  "
                    + "where a.strBillNo=b.strBillNo  "
                    + "and date(a.dteBillDate)=date(b.dteBillDate) "
                    + "and a.strClientCode=b.strClientCode   "
                    + "and b.strTaxCode=c.strTaxCode "
                    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ";
            if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
            {
                sqlTax += "and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ";
            }
            if (!selectedPOSCode.equalsIgnoreCase("All"))
            {
                sqlTax = sqlTax + " and a.strPOSCode='" + selectedPOSCode + "' ";
            }

            sqlTax = sqlTax + "group by a.strClientCode,a.strBillNo,b.strTaxCode "
                    + "order by a.strClientCode,a.strBillNo,b.strTaxCode;  ";
            funFillTaxData(sqlTax);

            
            funFillSettlementWiseBillWiseAmtMapForLive();
            funFillSettlementWiseBillWiseAmtMapForQFile();
//            for (Map.Entry<String, Double> entry : mapSettlemetWiseAmt.entrySet())
//            {
//                System.out.println(entry.getKey() + "->" + entry.getValue());
//            }
            funUpdateSettWiseBillWiseAmtForLive(columnNameForBillDtl);
            funUpdateSettWiseBillWiseAmtForQFile(columnNameForBillDtl);
            
            funUpdateSettWiseBillWiseTaxAmtForLive();
            funUpdateSettWiseBillWiseTaxAmtForQFile();

            //set null values to 0.00
            for (int row = 0; row < tblKDS.getRowCount(); row++)
            {
                for (int col = 0; col < tblKDS.getColumnCount(); col++)
                {
                    if (null == tblKDS.getValueAt(row, col))
                    {
                        tblKDS.setValueAt("0.00", row, col);
                    }
                }
            }

            //set day total
            dm.addColumn("Grand Total".toUpperCase());
            totalDm.addColumn("Grand Total".toUpperCase());
            Map<String, Double> mapDayGrandTotal = new HashMap<>();
            Map<String, String> mapCardNo = new HashMap<>();
            String sqlGrandTotal = "SELECT a.strBillNo,sum(b.dblSettlementAmt),b.strCardName  "
                    + "FROM tblqbillhd a,tblqbillsettlementdtl b,tblsettelmenthd c,tblposmaster d "
                    + "WHERE a.strBillNo=b.strBillNo    "
                    + "and date(a.dteBillDate)=date(b.dteBillDate) "
                    + "and a.strClientCode=b.strClientCode    "
                    + "AND b.strSettlementCode=c.strSettelmentCode  "
                    + "AND a.strPOSCode=d.strPosCode  "
                    + "and b.dblSettlementAmt>0 "
                    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ";
            if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
            {
                sqlGrandTotal += "and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ";
            }
            if (!selectedPOSCode.equalsIgnoreCase("All"))
            {
                sqlGrandTotal = sqlGrandTotal + " and a.strPOSCode='" + selectedPOSCode + "' ";
            }
            if (!cmbCostCenterName.getSelectedItem().toString().equalsIgnoreCase("All"))
            {
                sqlGrandTotal = sqlGrandTotal + " and c.strSettelmentCode='" + mapSettlementNameCode.get(cmbCostCenterName.getSelectedItem().toString()) + "' ";
            }
            sqlGrandTotal = sqlGrandTotal + "GROUP BY a.strClientCode,a.strBillNo "
                    + "ORDER BY a.strClientCode,a.strBillNo; ";
            rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlGrandTotal);
            while (rsSales.next())
            {
                mapDayGrandTotal.put(rsSales.getString(1), rsSales.getDouble(2));
                mapCardNo.put(rsSales.getString(1), rsSales.getString(3));
            }
            rsSales.close();

            //live
            sqlGrandTotal = "SELECT a.strBillNo,sum(b.dblSettlementAmt),b.strCardName  "
                    + "FROM tblbillhd a,tblbillsettlementdtl b,tblsettelmenthd c,tblposmaster d "
                    + "WHERE a.strBillNo=b.strBillNo  "
                    + "and date(a.dteBillDate)=date(b.dteBillDate) "
                    + "and a.strClientCode=b.strClientCode   "
                    + "AND b.strSettlementCode=c.strSettelmentCode  "
                    + "AND a.strPOSCode=d.strPosCode  "
                    + "and b.dblSettlementAmt>0 "
                    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ";
            if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
            {
                sqlGrandTotal += "and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ";
            }
            if (!selectedPOSCode.equalsIgnoreCase("All"))
            {
                sqlGrandTotal = sqlGrandTotal + " and a.strPOSCode='" + selectedPOSCode + "' ";
            }
            if (!cmbCostCenterName.getSelectedItem().toString().equalsIgnoreCase("All"))
            {
                sqlGrandTotal = sqlGrandTotal + " and c.strSettelmentCode='" + mapSettlementNameCode.get(cmbCostCenterName.getSelectedItem().toString()) + "' ";
            }
            sqlGrandTotal = sqlGrandTotal + "GROUP BY a.strClientCode,a.strBillNo "
                    + "ORDER BY a.strClientCode,a.strBillNo; ";
            rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlGrandTotal);
            while (rsSales.next())
            {
                if (mapDayGrandTotal.containsKey(rsSales.getString(1)))
                {
                    mapDayGrandTotal.put(rsSales.getString(1), mapDayGrandTotal.get(rsSales.getString(1)) + rsSales.getDouble(2));
                }
                else
                {
                    mapDayGrandTotal.put(rsSales.getString(1), rsSales.getDouble(2));
                }

                if (mapCardNo.containsKey(rsSales.getString(1)))
                {
                    mapCardNo.put(rsSales.getString(1), mapCardNo.get(rsSales.getString(1)) + rsSales.getString(3));
                }
                else
                {
                    mapCardNo.put(rsSales.getString(1), rsSales.getString(3));
                }
            }
            rsSales.close();

            int lasColumn = tblKDS.getColumnCount() - 1;
            for (int row = 0; row < tblKDS.getRowCount(); row++)
            {
                tblKDS.setValueAt(mapDayGrandTotal.get(tblKDS.getValueAt(row, 0).toString()), row, lasColumn);
            }

            // set total
            Object[] totalRow = new Object[tblKDS.getColumnCount()];
            totalRow[0] = "Totals".toUpperCase();
            totalRow[1] = "";
            totalRow[2] = selectedPOSName.toUpperCase();
            for (int col = 3; col < tblKDS.getColumnCount(); col++)
            {
                double total = 0.00;
                for (int row = 0; row < tblKDS.getRowCount(); row++)
                {
                    if (tblKDS.getValueAt(row, col) == null)
                    {
                        tblKDS.setValueAt("0.00", row, col);
                        total += Double.parseDouble(tblKDS.getValueAt(row, col).toString());
                    }
                    else
                    {
                        total += Double.parseDouble(tblKDS.getValueAt(row, col).toString());
                    }
                }
                totalRow[col] = String.format("%.2f", total);
            }
            totalDm.setRowCount(0);
            totalDm.addRow(totalRow);

            tblTotal.getColumnModel().getColumn(0).setPreferredWidth(70);
            tblTotal.getColumnModel().getColumn(1).setPreferredWidth(100);
            for (int cnt = 3; cnt < tblTotal.getColumnCount(); cnt++)
            {
                tblTotal.getColumnModel().getColumn(cnt).setPreferredWidth(100);
                tblTotal.getColumnModel().getColumn(cnt).setCellRenderer(rightRenderer);
            }

            tblKDS.getColumnModel().getColumn(0).setPreferredWidth(70);
            tblKDS.getColumnModel().getColumn(1).setPreferredWidth(100);
            for (int cnt = 3; cnt < tblKDS.getColumnCount(); cnt++)
            {
                tblKDS.getColumnModel().getColumn(cnt).setPreferredWidth(100);
                tblKDS.getColumnModel().getColumn(cnt).setCellRenderer(rightRenderer);
            }

            //set settle card no if exist
            dm.addColumn("Card No".toUpperCase());
            int lasColumnForCardName = tblKDS.getColumnCount() - 1;
            for (int row = 0; row < tblKDS.getRowCount(); row++)
            {
                tblKDS.setValueAt(mapCardNo.get(tblKDS.getValueAt(row, 0).toString()), row, lasColumnForCardName);
            }

            //delete columns whoes amount <=0
            funDeleteColumn();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
     */
    private void funFillGroupWiseSalesData(String sqlGroups)
    {
	try
	{
	    ResultSet rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlGroups);
	    while (rsSales.next())
	    {
		for (int tblRow = 0; tblRow < tblKDS.getRowCount(); tblRow++)
		{
		    if (tblKDS.getValueAt(tblRow, 0).toString().equalsIgnoreCase(rsSales.getString(1)))
		    {
			for (int tblCol = 3; tblCol < tblKDS.getColumnCount(); tblCol++)
			{
			    if (tblKDS.getColumnName(tblCol).toUpperCase().equals(rsSales.getString(2).toUpperCase()))
			    {
				if (null == tblKDS.getValueAt(tblRow, tblCol))
				{
				    tblKDS.setValueAt(rsSales.getString(3), tblRow, tblCol);
				}
				else
				{
				    Double value = Double.parseDouble(tblKDS.getValueAt(tblRow, tblCol).toString()) + rsSales.getDouble(3);
				    tblKDS.setValueAt(value, tblRow, tblCol);
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

    private void funDeleteColumn()
    {
	try
	{
//            for (int cnt = 3; cnt < tblTotal.getColumnCount(); cnt++)
//            {
//                if (!(Double.parseDouble(tblTotal.getValueAt(0, cnt).toString()) > 0))
//                {
//                    tblKDS.getColumnModel().removeColumn(tblKDS.getColumnModel().getColumn(cnt));
//                    tblTotal.getColumnModel().removeColumn(tblTotal.getColumnModel().getColumn(cnt));
//                }
//                else
//                {
//                    continue;
//                }
//            }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funFillCostCenterNameComboBox()
    {
	try
	{

	    selectQuery = "select a.strCostCenterCode,a.strCostCenterName from tblcostcentermaster a";
	    ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(selectQuery);
	    cmbCostCenterName.addItem("All");
	    mapCostCenterNameCode.put("All", "All");
	    while (rs.next())
	    {
		cmbCostCenterName.addItem(rs.getString(2));//costCenterName
		mapCostCenterNameCode.put(rs.getString(2), rs.getString(1));//name->code
	    }
	    rs.close();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funFillWaiterNameComboBox()
    {
	try
	{

	    String selectQuery = "select a.strWaiterNo,a.strWShortName,a.strWFullName from tblwaitermaster a";
	    ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(selectQuery);
	    cmbWaiterName.addItem("All");
	    mapWaiterNameCode.put("All", "All");
	    while (rs.next())
	    {
		cmbWaiterName.addItem(rs.getString(2));//waiterName
		mapWaiterNameCode.put(rs.getString(2), rs.getString(1));//name->code
	    }
	    rs.close();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    public void funFillSubGroupWiseDetailData()
    {
	try
	{
	    double sumQty = 0.00;

	    tblTotal7.setVisible(false);

	    dm.getDataVector().removeAllElements();
	    dm = new DefaultTableModel();
	    dm.addColumn("Bill No");
	    dm.addColumn("KOT No");
	    dm.addColumn("Waiter Name");
	    dm.addColumn("Item Name");
	    dm.addColumn("Qty");
	    dm.addColumn("KOT Time");
	    dm.addColumn("Process Time");
	    dm.addColumn("PickUp Time");
	    dm.addColumn("KOT-Process Time");
	    dm.addColumn("Process-PickUp Time");

	    Map<String, List<clsBillDtl>> hmKDSFlashData = new TreeMap<String, List<clsBillDtl>>();
	    long masterProcesTime = 0;

	    String maxDelayTime = "", minDelayTime = "";

	    String time = "", processTime = "", targetTime = "";
	    long sum = 0, totProcessTime = 0, sumOfDelayOrders = 0, longMasterTarTime = 0, sumOfDelayOrderTargetTime = 0, transProcessTime = 0;
	    long sumOfTotOrdTarAvg = 0, sumMasterProcessTime = 0;
	    long totDelayOrderTotAvg = 0;
	    int countOfDelayOrder = 0;

	    int noOfItemsCount = 0;
	    Date date1, dateProcessTime, itemTargetTme;
	    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
	    timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	    SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");
	    fmt.setTimeZone(TimeZone.getTimeZone("UTC"));

	    long minProcessTime = 0, maxProcessTime = 0;
	    long minDelayedTime = 0, maxDelayedTime = 0;
	    long sumOfMasterTarTime = 0, sumOfMasterProcTimeXMasterTarTime = 0, sumOfMasterProcTimeXTransProcTime = 0, sumOfKOTProcTime = 0;
	    boolean isFirstRecord = true;

	    StringBuilder sbSql = new StringBuilder();

	    sbSql.append("SELECT a.strBillNo,b.strKOTNo, DATE_FORMAT(DATE(b.dteBillDate),'%d-%m-%Y') dteKOTDate,"
		    + " b.strItemName, SUM(b.dblQuantity),SUM(b.dblAmount) AS Amount,sum(b.dblAmount)-sum(b.dblDiscountAmt) AS SubTotal, "
		    + " TIME(b.dteBillDate) as tmeKOTTime,TIME(b.tmeOrderProcessing) as OrderProcessingTime, "
		    + " TIME(b.tmeOrderPickup) as OrderPickupTime,b.strItemCode, "
		    + " if(TIME(b.tmeOrderProcessing)<TIME(b.dteBillDate),ADDTIME(TIME(b.tmeOrderProcessing),TIMEDIFF('24:00:00',TIME(b.dteBillDate))),TIMEDIFF(IF(TIME(b.tmeOrderProcessing)='00:00:00', TIME(b.dteBillDate), TIME(b.tmeOrderProcessing)), TIME(b.dteBillDate)) )  AS processtimediff, "
		    + " TIMEDIFF(if(TIME(b.tmeOrderPickup)='00:00:00',TIME(b.tmeOrderProcessing),TIME(b.tmeOrderPickup)),TIME(b.tmeOrderProcessing)) AS pickuptimediff,"
		    + " e.strCostCenterName,f.strWShortName,h.strSubGroupName"
		    + ",time(CONCAT('00',':',c.intProcTimeMin,':','00'))intProcTimeMin "
		    + ",time(CONCAT('00',':',c.tmeTargetMiss,':','00'))tmeTargetMiss "
		    + " FROM tblbillhd a,tblbilldtl b,tblitemmaster c,tblmenuitempricingdtl d,tblcostcentermaster e,tblwaitermaster f "
		    + " ,tblsubgrouphd h"
		    + " WHERE a.strBillNo=b.strBillNo AND DATE(a.dtBillDate)= DATE(b.dtBillDate) "
		    + " and b.strItemCode=c.strItemCode and c.strItemCode=d.strItemCode "
		    + " and (a.strPOSCode=d.strPosCode or d.strPosCode='All') and d.strCostCenterCode=e.strCostCenterCode and a.strWaiterNo=f.strWaiterNo "
		    + " and DATE(a.dtBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "'  "
		    + " and c.strSubGroupCode = h.strSubGroupCode ");
	    if (!cmbCostCenterName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sbSql.append("and d.strCostCenterCode='" + mapCostCenterNameCode.get(cmbCostCenterName.getSelectedItem().toString()) + "'  ");
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sbSql.append(" and a.strPOSCode='" + selectedPOSCode + "'  ");
	    }
	    if (!cmbWaiterName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sbSql.append(" and a.strWaiterNo='" + mapWaiterNameCode.get(cmbWaiterName.getSelectedItem().toString()) + "'  ");
	    }

	    sbSql.append(" GROUP BY h.strSubGroupCode,b.strItemCode  ");
	    sbSql.append(" Order By h.strSubGroupCode DESC");
	    ResultSet rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
	    while (rsSales.next())
	    {
		clsBillDtl objBill = new clsBillDtl();

		objBill.setStrBillNo(rsSales.getString(1));           //Bill No
		objBill.setStrKOTNo(rsSales.getString(2));            //KOT No
		objBill.setDteBillDate(rsSales.getString(3));        //Kot Date
		objBill.setStrItemName(rsSales.getString(4));        //Item Name
		objBill.setDblQuantity(rsSales.getDouble(5));        //Qty
		objBill.setDblAmount(rsSales.getDouble(6));          //Amount
		objBill.setDblBillAmt(rsSales.getDouble(7));         //Sub Total
		objBill.setTmeBillTime(rsSales.getString(8));        //Kot Time
		objBill.setTmeOrderProcessing(rsSales.getString(9)); //Process Time
		objBill.setTmeBillSettleTime(rsSales.getString(10));  //Puckup Time
		objBill.setStrProcessTimeDiff(rsSales.getString(12));  // diff kot & process Time
		objBill.setStrPickUpTimeDiff(rsSales.getString(13));  // diff process & pickup Time
		objBill.setStrCounterCode(rsSales.getString(14));     // costCenterName
		objBill.setStrWShortName(rsSales.getString(15));        // waiterName
		objBill.setStrGroupName(rsSales.getString(16));
		objBill.setStrItemProcessTime(rsSales.getString(17));        // item process time
		targetTime = rsSales.getString(18);
		itemTargetTme = timeFormat.parse(targetTime);
		objBill.setStrItemTargetTime(targetTime);        // item target time
		longMasterTarTime = itemTargetTme.getTime();
		sumOfMasterTarTime = sumOfMasterTarTime + longMasterTarTime;
		sumOfTotOrdTarAvg = sumOfTotOrdTarAvg + itemTargetTme.getTime();

		masterProcesTime = timeFormat.parse(rsSales.getString(17)).getTime();
		sumMasterProcessTime = sumMasterProcessTime + masterProcesTime;
		processTime = rsSales.getString(12);
		dateProcessTime = timeFormat.parse(processTime);
		transProcessTime = dateProcessTime.getTime();

		if (isFirstRecord)
		{
		    minProcessTime = transProcessTime;
		    maxProcessTime = transProcessTime;

		    minDelayedTime = transProcessTime;
		    maxDelayedTime = transProcessTime;
		}

		if (transProcessTime < minProcessTime)
		{
		    minProcessTime = transProcessTime;
		}
		if (transProcessTime > maxProcessTime)
		{
		    maxProcessTime = transProcessTime;
		}

		if (transProcessTime > longMasterTarTime)
		{
		    long delayTime = (transProcessTime - longMasterTarTime);

		    if (delayTime < minDelayedTime)
		    {
			minDelayedTime = delayTime;
		    }

		    if (delayTime > maxDelayedTime)
		    {
			maxDelayedTime = delayTime;
		    }

		    sumOfDelayOrders = sumOfDelayOrders + transProcessTime;
		    sumOfDelayOrderTargetTime = sumOfDelayOrderTargetTime + longMasterTarTime;
		    countOfDelayOrder++;
		}

		noOfItemsCount = noOfItemsCount + 1;
		time = rsSales.getString(12);
		date1 = timeFormat.parse(time);
		sumOfKOTProcTime = sumOfKOTProcTime + date1.getTime();

		String key = rsSales.getString(16) + "!" + rsSales.getString(3);

		List<clsBillDtl> arrListBillDtl = new ArrayList<clsBillDtl>();
		if (hmKDSFlashData.containsKey(key))
		{
		    arrListBillDtl = hmKDSFlashData.get(key);
		    arrListBillDtl.add(objBill);
		}
		else
		{
		    arrListBillDtl.add(objBill);
		}
		hmKDSFlashData.put(key, arrListBillDtl);

		sumOfMasterProcTimeXMasterTarTime = sumOfMasterProcTimeXMasterTarTime + (masterProcesTime * longMasterTarTime);
		sumOfMasterProcTimeXTransProcTime = sumOfMasterProcTimeXTransProcTime + (masterProcesTime * transProcessTime);

		isFirstRecord = false;
	    }
	    rsSales.close();

	    sbSql.setLength(0);
	    sbSql.append("SELECT a.strBillNo,b.strKOTNo,DATE_FORMAT(DATE(b.dteBillDate),'%d-%m-%Y') dteKOTDate,"
		    + " b.strItemName, SUM(b.dblQuantity),SUM(b.dblAmount) AS Amount,sum(b.dblAmount)-sum(b.dblDiscountAmt) AS SubTotal, "
		    + " TIME(b.dteBillDate) as tmeKOTTime,TIME(b.tmeOrderProcessing) as OrderProcessingTime, "
		    + " TIME(b.tmeOrderPickup) as OrderPickupTime,b.strItemCode, "
		    + " if(TIME(b.tmeOrderProcessing)<TIME(b.dteBillDate),ADDTIME(TIME(b.tmeOrderProcessing),TIMEDIFF('24:00:00',TIME(b.dteBillDate))),TIMEDIFF(IF(TIME(b.tmeOrderProcessing)='00:00:00', TIME(b.dteBillDate), TIME(b.tmeOrderProcessing)), TIME(b.dteBillDate)) )  AS processtimediff, "
		    + " TIMEDIFF(if(TIME(b.tmeOrderPickup)='00:00:00',TIME(b.tmeOrderProcessing),TIME(b.tmeOrderPickup)),TIME(b.tmeOrderProcessing)) AS pickuptimediff,"
		    + " e.strCostCenterName,f.strWShortName,h.strSubGroupName"
		    + ",time(CONCAT('00',':',c.intProcTimeMin,':','00'))intProcTimeMin "
		    + ",time(CONCAT('00',':',c.tmeTargetMiss,':','00'))tmeTargetMiss "
		    + " FROM tblqbillhd a,tblqbilldtl b,tblitemmaster c,tblmenuitempricingdtl d,tblcostcentermaster e,tblwaitermaster f "
		    + " ,tblsubgrouphd h"
		    + " WHERE a.strBillNo=b.strBillNo AND DATE(a.dtBillDate)= DATE(b.dtBillDate) "
		    + " and b.strItemCode=c.strItemCode and c.strItemCode=d.strItemCode "
		    + " and (a.strPOSCode=d.strPosCode or d.strPosCode='All') and d.strCostCenterCode=e.strCostCenterCode and a.strWaiterNo=f.strWaiterNo "
		    + " AND DATE(a.dtBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
		    + " and c.strSubGroupCode = h.strSubGroupCode ");
	    if (!cmbCostCenterName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sbSql.append("and d.strCostCenterCode='" + mapCostCenterNameCode.get(cmbCostCenterName.getSelectedItem().toString()) + "'  ");
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sbSql.append(" and a.strPOSCode='" + selectedPOSCode + "'  ");
	    }
	    if (!cmbWaiterName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sbSql.append(" and a.strWaiterNo='" + mapWaiterNameCode.get(cmbWaiterName.getSelectedItem().toString()) + "'  ");
	    }
	    sbSql.append(" GROUP BY h.strSubGroupCode,b.strItemCode ");
	    sbSql.append(" Order By h.strSubGroupCode DESC");
	    ResultSet rsQSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());

	    while (rsQSales.next())
	    {
		clsBillDtl objBill = new clsBillDtl();
		objBill.setStrBillNo(rsQSales.getString(1));           //Bill No
		objBill.setStrKOTNo(rsQSales.getString(2));            //KOT No
		objBill.setDteBillDate(rsQSales.getString(3));        //Kot Date
		objBill.setStrItemName(rsQSales.getString(4));        //Item Name
		objBill.setDblQuantity(rsQSales.getDouble(5));        //Qty
		objBill.setDblAmount(rsQSales.getDouble(6));          //Amount
		objBill.setDblBillAmt(rsQSales.getDouble(7));         //Sub Total
		objBill.setTmeBillTime(rsQSales.getString(8));        //Kot Time
		objBill.setTmeOrderProcessing(rsQSales.getString(9)); //Process Time
		objBill.setTmeBillSettleTime(rsQSales.getString(10)); //Puckup Time                
		objBill.setStrProcessTimeDiff(rsQSales.getString(12));  // diff kot & process Time

		//System.out.println(rsQSales.getString(1)+"   "+rsQSales.getString(2));
		objBill.setStrPickUpTimeDiff(rsQSales.getString(13));  // diff process & pickup Time
		objBill.setStrCounterCode(rsQSales.getString(14));     // costCenterName
		objBill.setStrWShortName(rsQSales.getString(15));        // waiterName
		objBill.setStrItemProcessTime(rsQSales.getString(17));        // item process time
		targetTime = rsQSales.getString(18);
		itemTargetTme = timeFormat.parse(targetTime);
		objBill.setStrItemTargetTime(targetTime);        // item target time
		longMasterTarTime = itemTargetTme.getTime();
		sumOfMasterTarTime = sumOfMasterTarTime + longMasterTarTime;
		sumOfTotOrdTarAvg = sumOfTotOrdTarAvg + itemTargetTme.getTime();

		masterProcesTime = timeFormat.parse(rsQSales.getString(17)).getTime();
		sumMasterProcessTime = sumMasterProcessTime + masterProcesTime;
		processTime = rsQSales.getString(12);
		dateProcessTime = timeFormat.parse(processTime);
		transProcessTime = dateProcessTime.getTime();

		if (isFirstRecord)
		{
		    minProcessTime = transProcessTime;
		    maxProcessTime = transProcessTime;

		    minDelayedTime = transProcessTime;
		    maxDelayedTime = transProcessTime;
		}

		if (transProcessTime < minProcessTime)
		{
		    minProcessTime = transProcessTime;
		}
		if (transProcessTime > maxProcessTime)
		{
		    maxProcessTime = transProcessTime;
		}

		if (transProcessTime > longMasterTarTime)
		{
		    long delayTime = (transProcessTime - longMasterTarTime);

		    if (delayTime < minDelayedTime)
		    {
			minDelayedTime = delayTime;
		    }

		    if (delayTime > maxDelayedTime)
		    {
			maxDelayedTime = delayTime;
		    }

		    sumOfDelayOrders = sumOfDelayOrders + transProcessTime;
		    sumOfDelayOrderTargetTime = sumOfDelayOrderTargetTime + longMasterTarTime;
		    countOfDelayOrder++;
		}

		noOfItemsCount = noOfItemsCount + 1;
		time = rsQSales.getString(12);
		date1 = timeFormat.parse(time);
		sumOfKOTProcTime = sumOfKOTProcTime + date1.getTime();

		String key = rsQSales.getString(16) + "!" + rsQSales.getString(3);

		List<clsBillDtl> arrListBillDtl = new ArrayList<clsBillDtl>();
		if (hmKDSFlashData.containsKey(key))
		{
		    arrListBillDtl = hmKDSFlashData.get(key);
		    arrListBillDtl.add(objBill);
		}
		else
		{
		    arrListBillDtl.add(objBill);
		}
		hmKDSFlashData.put(key, arrListBillDtl);

		sumOfMasterProcTimeXMasterTarTime = sumOfMasterProcTimeXMasterTarTime + (masterProcesTime * longMasterTarTime);
		sumOfMasterProcTimeXTransProcTime = sumOfMasterProcTimeXTransProcTime + (masterProcesTime * transProcessTime);

		isFirstRecord = false;
	    }
	    rsQSales.close();

	    String avgProTime = "", quantity = "", DOAvg = "", totOrdTarAvg = "", delayOrderTargAvg = "";
	    String totDelayOrderTotAvgPer = "", weightedAvgTargetTime = "", weightedAvgActualTime = "";
	    long first = 0, second = 0, longWeightedAvgTargetTime = 0, longWeightedAvgActualTime = 0, longAvgMasterTarTime;
	    double finalDelayedOrder = 0;
	    double totOrderePer = 0;
	    long finalDota = 0;
	    if (!isFirstRecord)
	    {
		finalDelayedOrder = countOfDelayOrder;

		longWeightedAvgTargetTime = sumOfMasterProcTimeXMasterTarTime / sumMasterProcessTime;
		longWeightedAvgActualTime = sumOfMasterProcTimeXTransProcTime / sumMasterProcessTime;
		longAvgMasterTarTime = sumOfMasterTarTime / noOfItemsCount;

		weightedAvgTargetTime = fmt.format(new Date(longWeightedAvgTargetTime));
		weightedAvgActualTime = fmt.format(new Date(longWeightedAvgActualTime));
		first = (sumMasterProcessTime * sumOfMasterTarTime) / sumMasterProcessTime;
		avgProTime = fmt.format(new Date(sumOfKOTProcTime / noOfItemsCount));
		if (noOfItemsCount != 0)
		{
		    avgProTime = fmt.format(new Date(sumOfKOTProcTime / noOfItemsCount));
		    totOrderePer = ((finalDelayedOrder / noOfItemsCount) * 100);
		    totOrdTarAvg = fmt.format(new Date(longAvgMasterTarTime));
		    second = sumOfMasterTarTime / noOfItemsCount;
		}
		else
		{
		    avgProTime = fmt.format(new Date(sumOfKOTProcTime));
		    totOrderePer = ((finalDelayedOrder) * 100);
		    totOrdTarAvg = fmt.format(new Date(sumOfMasterTarTime));
		    second = sumOfMasterTarTime;
		}

		if (countOfDelayOrder != 0)
		{
		    DOAvg = fmt.format(new Date(sumOfDelayOrderTargetTime / countOfDelayOrder));
		    delayOrderTargAvg = fmt.format(new Date(sumOfDelayOrderTargetTime / countOfDelayOrder));
		}
		else
		{
		    DOAvg = fmt.format(new Date(sumOfDelayOrderTargetTime));
		    delayOrderTargAvg = fmt.format(new Date(sumOfDelayOrderTargetTime));

		}

		double finalPer = (double) (longAvgMasterTarTime / (double) longWeightedAvgTargetTime) * 100;

		totDelayOrderTotAvgPer = String.valueOf(decimalFormtFor2DecPoint.format(finalPer));

	    }

	    System.out.println(hmKDSFlashData.keySet());

	    for (Map.Entry<String, List<clsBillDtl>> entry : hmKDSFlashData.entrySet())
	    {
		Object[] records = new Object[16];
		records[0] = "<html><font color=red line-height= 50%> <b>" + entry.getKey().split("!")[0] + "</b></font></html>";
		records[1] = "<html><font color=red ><b>" + entry.getKey().split("!")[1] + "</b></font></html>";
		dm.addRow(records);

		for (int cnt = 0; cnt < entry.getValue().size(); cnt++)
		{
		    clsBillDtl objBill = entry.getValue().get(cnt);

		    records = new Object[14];
		    records[0] = objBill.getStrBillNo();
		    records[1] = objBill.getStrKOTNo();
		    records[2] = objBill.getStrWShortName();
		    records[3] = objBill.getStrItemName();
		    records[4] = objBill.getDblQuantity() + "  ";
		    records[5] = " " + objBill.getTmeBillTime();
		    records[6] = objBill.getTmeOrderProcessing();
		    records[7] = objBill.getTmeBillSettleTime();
		    records[8] = objBill.getStrProcessTimeDiff();
		    records[9] = objBill.getStrPickUpTimeDiff();
		    records[10] = objBill.getStrItemProcessTime();
		    records[11] = objBill.getStrItemTargetTime();
		    sumQty = sumQty + objBill.getDblQuantity();
		    dm.addRow(records);
		}

		records = new Object[14];
		dm.addRow(records);
	    }
	    tblKDS.setModel(dm);

	    DefaultTableCellRenderer rightRenderer1 = new DefaultTableCellRenderer();
	    rightRenderer1.setHorizontalAlignment(JLabel.RIGHT);
	    DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
	    centerRenderer.setHorizontalAlignment(JLabel.CENTER);

	    tblKDS.getColumnModel().getColumn(4).setCellRenderer(rightRenderer1);
	    tblKDS.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

	    tblKDS.getColumnModel().getColumn(0).setPreferredWidth(120);// Bill No
	    tblKDS.getColumnModel().getColumn(1).setPreferredWidth(70);// KOT No
	    tblKDS.getColumnModel().getColumn(2).setPreferredWidth(120);// waiter Name
	    tblKDS.getColumnModel().getColumn(3).setPreferredWidth(220);// Item Name
	    tblKDS.getColumnModel().getColumn(4).setPreferredWidth(40);// Qty
	    tblKDS.getColumnModel().getColumn(5).setPreferredWidth(80);// KOT Time
	    tblKDS.getColumnModel().getColumn(6).setPreferredWidth(80);// Process Time
	    tblKDS.getColumnModel().getColumn(7).setPreferredWidth(80);// PickedUp Time
	    tblKDS.getColumnModel().getColumn(8).setPreferredWidth(120);// KOT Time - Process Time
	    tblKDS.getColumnModel().getColumn(9).setPreferredWidth(120);// Process Time - Pickup Time
	    lblAvgProcessTme.setText(avgProTime);
	    lblTxtDelayedOrders.setText(String.valueOf(finalDelayedOrder));
	    lblTxtTotOrders.setText(String.valueOf(noOfItemsCount));
	    lblTotOrdersPer.setText(String.valueOf(totOrderePer + "%"));
	    lblTxtTotOrderTargetAvg.setText(totOrdTarAvg);
	    lblTxtDelayOrderTargetAvg.setText(delayOrderTargAvg);
	    lblDOTAPer.setText(String.valueOf(totDelayOrderTotAvgPer) + " %");
	    lbllTxtWAvgAT.setText(weightedAvgTargetTime);
	    lbltxtWAvgTT.setText(weightedAvgActualTime);
	    lblTxtMinimumProcessTme.setText(String.valueOf(fmt.format(new Date(minProcessTime))));
	    lblTxtMaximumProcessTime.setText(String.valueOf(fmt.format(new Date(maxProcessTime))));
	    lblTxtMinimumDelayTime.setText(String.valueOf(fmt.format(new Date(minDelayedTime))));
	    lblTxtMaximumDelayTime.setText(String.valueOf(fmt.format(new Date(maxDelayedTime))));
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    public void funShowBelowLabels()
    {
	lblAvgProcessTme.setVisible(true);
	lblTxtAvgProcessTme.setVisible(true);
	lblDelayedOrders.setVisible(true);
	lblTxtDelayedOrders.setVisible(true);
	lblTotOrders.setVisible(true);
	lblTxtTotOrders.setVisible(true);
	lblTotOrderPer.setVisible(true);
	lblTotOrdersPer.setVisible(true);
	lblTotOrderTargetAvg.setVisible(true);
	lblTxtTotOrderTargetAvg.setVisible(true);
	delayOrderTargetAvg.setVisible(true);
	lblTxtDelayOrderTargetAvg.setVisible(true);
	lblDelayOrderTargetAvgPer.setVisible(true);
	lblDOTAPer.setVisible(true);
    }

    public void funHideBelowLabels()
    {
	lblAvgProcessTme.setVisible(false);
	lblTxtAvgProcessTme.setVisible(false);
	lblDelayedOrders.setVisible(false);
	lblTxtDelayedOrders.setVisible(false);
	lblTotOrders.setVisible(false);
	lblTxtTotOrders.setVisible(false);
	lblTotOrderPer.setVisible(false);
	lblTotOrdersPer.setVisible(false);
	lblTotOrderTargetAvg.setVisible(false);
	lblTxtTotOrderTargetAvg.setVisible(false);
	delayOrderTargetAvg.setVisible(false);
	lblTxtDelayOrderTargetAvg.setVisible(false);
	lblDelayOrderTargetAvgPer.setVisible(false);
	lblDOTAPer.setVisible(false);
	lblWAvgAT.setVisible(false);
	lbllTxtWAvgAT.setVisible(false);
	lblWAvgTT.setVisible(false);
	lbltxtWAvgTT.setVisible(false);
	lblMaximumDelayTime.setVisible(false);
	lblTxtMaximumDelayTime.setVisible(false);
	lblMaximumProcessTime.setVisible(false);
	lblTxtMaximumProcessTime.setVisible(false);
	lblMinimumDelayTime.setVisible(false);
	lblTxtMinimumDelayTime.setVisible(false);
	lblMinimumProcessTme.setVisible(false);
	lblTxtMinimumProcessTme.setVisible(false);
    }

    public void funFillSubGroupWiseSummaryData()
    {
	try
	{
	    funHideBelowLabels();
	    double sumQty = 0.00;
	    String date3 = "", DOAvg = "", totOrdTarAvg = "", delayOrderTargAvg = "";
	    String totDelayOrderTotAvgPer = "";
	    long first = 0, second = 0, sumTotOrders = 0, sumTotOrderTarAvg = 0, sumDOAvg = 0, sumTotOrderTAvg = 0, sumDelayOrderTAvg = 0, sumDelayOrderTAvgPer = 0;
	    int finalDelayedOrder = 0, sumDelayOrders = 0;
	    double totOrderePer = 0, sumTotOrderPer = 0;
	    long finalDota = 0;
	    long f1, f2;
	    int i1 = 0;
	    long l = 0;
	    long sumOfAvgTime = 0, dispTotAvgProcTme = 0;
	    int totQty = 0;

	    dm.getDataVector().removeAllElements();
	    dm = new DefaultTableModel();
	    dm.addColumn("Sub Group Name");
	    dm.addColumn("Average Prcessing Time");
	    dm.addColumn("Delayed Orders");
	    dm.addColumn("Total Orders ");
	    dm.addColumn("Total Orders %");
	    dm.addColumn("Delayed Order Avergae");
	    dm.addColumn("Total Order Target Average");
	    dm.addColumn("Delayed Order Target Average");
	    dm.addColumn("Weighted Avg. Target Time");
	    dm.addColumn("Weighted Avg. Actual Time");
	    dm.addColumn("Delay Order Target Avg %");

	    totalDm = new DefaultTableModel();
	    totalDm.addColumn("");
	    totalDm.addColumn("");
	    totalDm.addColumn("");
	    totalDm.addColumn("");
	    totalDm.addColumn("");
	    totalDm.addColumn("");
	    totalDm.addColumn("");
	    totalDm.addColumn("");
	    totalDm.addColumn("");
	    totalDm.addColumn("");
	    totalDm.addColumn("");
	    tblTotal7.setModel(totalDm);

	    //Map<String,List<clsBillDtl>> hmKDSFlashData=new HashMap<String,List<clsBillDtl>>();
	    Map<String, List<clsBillDtl>> hmKDSFlashData = new TreeMap<String, List<clsBillDtl>>();

	    StringBuilder sbSql = new StringBuilder();

	    sbSql.append("SELECT a.strBillNo,b.strKOTNo, DATE_FORMAT(DATE(b.dteBillDate),'%d-%m-%Y') dteKOTDate,"
		    + " b.strItemName, SUM(b.dblQuantity),SUM(b.dblAmount) AS Amount,sum(b.dblAmount)-sum(b.dblDiscountAmt) AS SubTotal, "
		    + " TIME(b.dteBillDate) as tmeKOTTime,TIME(b.tmeOrderProcessing) as OrderProcessingTime, "
		    + " TIME(b.tmeOrderPickup) as OrderPickupTime,b.strItemCode, "
		    + " if(TIME(b.tmeOrderProcessing)<TIME(b.dteBillDate),ADDTIME(TIME(b.tmeOrderProcessing),TIMEDIFF('24:00:00',TIME(b.dteBillDate))),TIMEDIFF(IF(TIME(b.tmeOrderProcessing)='00:00:00', TIME(b.dteBillDate), TIME(b.tmeOrderProcessing)), TIME(b.dteBillDate)) )  AS processtimediff, "
		    + " TIMEDIFF(if(TIME(b.tmeOrderPickup)='00:00:00',TIME(b.tmeOrderProcessing),TIME(b.tmeOrderPickup)),TIME(b.tmeOrderProcessing)) AS pickuptimediff,"
		    + " e.strCostCenterName,f.strWShortName,h.strSubGroupName "
		    + ",time(CONCAT('00',':',c.intProcTimeMin,':','00'))intProcTimeMin "
		    + ",time(CONCAT('00',':',c.tmeTargetMiss,':','00'))tmeTargetMiss "
		    + " FROM tblbillhd a,tblbilldtl b,tblitemmaster c,tblmenuitempricingdtl d,tblcostcentermaster e,tblwaitermaster f "
		    + " ,tblsubgrouphd h"
		    + " WHERE a.strBillNo=b.strBillNo AND DATE(a.dtBillDate)= DATE(b.dtBillDate) "
		    + " and b.strItemCode=c.strItemCode and c.strItemCode=d.strItemCode "
		    + " and (a.strPOSCode=d.strPosCode or d.strPosCode='All') and d.strCostCenterCode=e.strCostCenterCode and a.strWaiterNo=f.strWaiterNo "
		    + " and DATE(a.dtBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "'  "
		    + " and c.strSubGroupCode = h.strSubGroupCode ");
	    if (!cmbCostCenterName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sbSql.append("and d.strCostCenterCode='" + mapCostCenterNameCode.get(cmbCostCenterName.getSelectedItem().toString()) + "'  ");
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sbSql.append(" and a.strPOSCode='" + selectedPOSCode + "'  ");
	    }
	    if (!cmbWaiterName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sbSql.append(" and a.strWaiterNo='" + mapWaiterNameCode.get(cmbWaiterName.getSelectedItem().toString()) + "'  ");
	    }

	    sbSql.append(" GROUP BY h.strSubGroupCode ");
	    sbSql.append(" Order By h.strSubGroupCode DESC");
	    ResultSet rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());

	    String time = "", processTime = "", targetTime = "", weightedAvgTarTme = "", weightedAvgActualTme = "";
	    long masterTargTime = 0, sumOfDelayOrders = 0, totTaregetTime = 0, sumOfDelayOrderTargetTime = 0;
	    long totWeighAvgActTme = 0, totWeighAvgTarTme = 0, sumOfTotOrdTarAvg = 0, sumMasterProcessTime = 0;
	    long sumWeightedAvgActualTme = 0, sumWeightedAvgTargetTme = 0;
	    int i = 0, j = 0, countOfDelayOrder = 0;
	    long masterProcTime = 0;

	    Date date1, dateProcessTime, itemTargetTme;
	    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
	    timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	    SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");
	    fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
	    while (rsSales.next())
	    {
		long sum = 0;
		int qty = 0;
		clsBillDtl objBill = new clsBillDtl();
		objBill.setStrBillNo(rsSales.getString(1));           //Bill No
		objBill.setStrKOTNo(rsSales.getString(2));            //KOT No
		objBill.setDteBillDate(rsSales.getString(3));        //Kot Date
		objBill.setStrItemName(rsSales.getString(4));        //Item Name
		objBill.setDblQuantity(rsSales.getDouble(5));        //Qty
		objBill.setDblAmount(rsSales.getDouble(6));          //Amount
		objBill.setDblBillAmt(rsSales.getDouble(7));         //Sub Total
		objBill.setTmeBillTime(rsSales.getString(8));        //Kot Time
		objBill.setTmeOrderProcessing(rsSales.getString(9)); //Process Time
		objBill.setTmeBillSettleTime(rsSales.getString(10));  //Puckup Time
		objBill.setStrProcessTimeDiff(rsSales.getString(12));  // diff kot & process Time
		objBill.setStrPickUpTimeDiff(rsSales.getString(13));  // diff process & pickup Time
		objBill.setStrCounterCode(rsSales.getString(14));     // costCenterName
		objBill.setStrWShortName(rsSales.getString(15));        // waiterName
		objBill.setStrGroupName(rsSales.getString(16));
		objBill.setStrItemProcessTime(rsSales.getString(17));        // item process time
		targetTime = rsSales.getString(18);
		itemTargetTme = timeFormat.parse(targetTime);
		objBill.setStrItemTargetTime(targetTime);        // item target time
		totTaregetTime = itemTargetTme.getTime();
		sumOfTotOrdTarAvg = sumOfTotOrdTarAvg + itemTargetTme.getTime();

		processTime = rsSales.getString(9);
		dateProcessTime = timeFormat.parse(processTime);
		masterTargTime = dateProcessTime.getTime();

		masterProcTime = timeFormat.parse(rsSales.getString(17)).getTime();
		totWeighAvgActTme = (masterProcTime * totTaregetTime) / masterProcTime;
		weightedAvgTarTme = timeFormat.format(new Date((masterProcTime * totTaregetTime) / masterProcTime));
		sumWeightedAvgTargetTme = sumWeightedAvgTargetTme + totWeighAvgActTme;
		objBill.setStrWeightedAvgTarTme(weightedAvgTarTme);

		totWeighAvgActTme = (masterTargTime * masterProcTime) / masterProcTime;
		weightedAvgActualTme = timeFormat.format(new Date((masterProcTime * totTaregetTime) / masterProcTime));
		sumWeightedAvgActualTme = sumWeightedAvgActualTme + totWeighAvgActTme;
		objBill.setStrWeightedAvgActualTme(weightedAvgActualTme);

		if (masterTargTime > totTaregetTime)
		{

		    sumOfDelayOrders = sumOfDelayOrders + masterTargTime;
		    sumOfDelayOrderTargetTime = sumOfDelayOrderTargetTime + totTaregetTime;
		    countOfDelayOrder++;
		    j++;
		}

		qty = qty + rsSales.getInt(5);
		time = rsSales.getString(12);
		date1 = timeFormat.parse(time);
		sum = sum + date1.getTime();
		sumOfAvgTime = sumOfAvgTime + sum;
		dispTotAvgProcTme = dispTotAvgProcTme + (sum / qty);
		System.out.println(timeFormat.format(new Date(dispTotAvgProcTme)));
		totQty = totQty + qty;
		date3 = timeFormat.format(new Date(sum / qty));
		objBill.setAvgProcessingTime(date3);

		finalDelayedOrder = countOfDelayOrder;

		objBill.setDelayOrders(String.valueOf(finalDelayedOrder));
		sumTotOrders = sumTotOrders + qty;
		objBill.setTotOrders(String.valueOf(qty));

		totOrderePer = ((finalDelayedOrder / qty) * 100);
		sumTotOrderPer = sumTotOrderPer + totOrderePer;
		objBill.setTotOrderesPer(String.valueOf(totOrderePer + "%"));
		totOrdTarAvg = fmt.format(new Date(sumOfTotOrdTarAvg / qty));
		sumTotOrderTarAvg = sumTotOrderTarAvg + (sumOfTotOrdTarAvg / qty);
		objBill.setTotAvg(totOrdTarAvg);
		if (countOfDelayOrder != 0)
		{
		    DOAvg = fmt.format(new Date(sumOfDelayOrderTargetTime / countOfDelayOrder));
		    delayOrderTargAvg = fmt.format(new Date(sumOfDelayOrderTargetTime / countOfDelayOrder));
		    first = sumOfDelayOrderTargetTime / countOfDelayOrder;
		    sumDOAvg = sumDOAvg + (sumOfDelayOrderTargetTime / countOfDelayOrder);
		    sumDelayOrderTAvg = sumDelayOrderTAvg + (sumOfDelayOrderTargetTime / countOfDelayOrder);
		}
		else
		{
		    DOAvg = fmt.format(new Date(sumOfDelayOrderTargetTime));
		    first = sumOfDelayOrderTargetTime;
		    delayOrderTargAvg = fmt.format(new Date(sumOfDelayOrderTargetTime));
		    sumDOAvg = sumDOAvg + sumOfDelayOrderTargetTime;
		    sumDelayOrderTAvg = sumDelayOrderTAvg + sumOfDelayOrderTargetTime;
		}

		objBill.setDoAvg(DOAvg);
		objBill.setDotAvg(delayOrderTargAvg);
		second = sumOfTotOrdTarAvg / qty;
		f1 = first;
		f2 = second;
		finalDota = (f1 / f2) * 100;
		i1 = (int) finalDota;
		l = Long.parseLong(String.valueOf(i1));
		if (finalDota != 0)
		{
		    totDelayOrderTotAvgPer = fmt.format(new Date(l));
		}
		sumDelayOrderTAvgPer = sumDelayOrderTAvgPer + l;
		objBill.setDotAvgPer(totDelayOrderTotAvgPer);

		String key = rsSales.getString(16) + "!" + rsSales.getString(3);

		List<clsBillDtl> arrListBillDtl = new ArrayList<clsBillDtl>();
		if (hmKDSFlashData.containsKey(key))
		{
		    arrListBillDtl = hmKDSFlashData.get(key);
		    arrListBillDtl.add(objBill);
		}
		else
		{
		    arrListBillDtl = new ArrayList<clsBillDtl>();
		    arrListBillDtl.add(objBill);

		}
		hmKDSFlashData.put(key, arrListBillDtl);

		i++;

	    }

	    sbSql.setLength(0);
	    sbSql.append("SELECT a.strBillNo,b.strKOTNo,DATE_FORMAT(DATE(b.dteBillDate),'%d-%m-%Y') dteKOTDate,"
		    + " b.strItemName, SUM(b.dblQuantity),SUM(b.dblAmount) AS Amount,sum(b.dblAmount)-sum(b.dblDiscountAmt) AS SubTotal, "
		    + " TIME(b.dteBillDate) as tmeKOTTime,TIME(b.tmeOrderProcessing) as OrderProcessingTime, "
		    + " TIME(b.tmeOrderPickup) as OrderPickupTime,b.strItemCode, "
		    + " if(TIME(b.tmeOrderProcessing)<TIME(b.dteBillDate),ADDTIME(TIME(b.tmeOrderProcessing),TIMEDIFF('24:00:00',TIME(b.dteBillDate))),TIMEDIFF(IF(TIME(b.tmeOrderProcessing)='00:00:00', TIME(b.dteBillDate), TIME(b.tmeOrderProcessing)), TIME(b.dteBillDate)) )  AS processtimediff, "
		    + " TIMEDIFF(if(TIME(b.tmeOrderPickup)='00:00:00',TIME(b.tmeOrderProcessing),TIME(b.tmeOrderPickup)),TIME(b.tmeOrderProcessing)) AS pickuptimediff,"
		    + " e.strCostCenterName,f.strWShortName,h.strSubGroupName"
		    + ",time(CONCAT('00',':',c.intProcTimeMin,':','00'))intProcTimeMin "
		    + ",time(CONCAT('00',':',c.tmeTargetMiss,':','00'))tmeTargetMiss "
		    + " FROM tblqbillhd a,tblqbilldtl b,tblitemmaster c,tblmenuitempricingdtl d,tblcostcentermaster e,tblwaitermaster f "
		    + " ,tblsubgrouphd h"
		    + " WHERE a.strBillNo=b.strBillNo AND DATE(a.dtBillDate)= DATE(b.dtBillDate) "
		    + " and b.strItemCode=c.strItemCode and c.strItemCode=d.strItemCode "
		    + " and (a.strPOSCode=d.strPosCode or d.strPosCode='All') and d.strCostCenterCode=e.strCostCenterCode and a.strWaiterNo=f.strWaiterNo "
		    + " AND DATE(a.dtBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
		    + " and c.strSubGroupCode = h.strSubGroupCode ");
	    if (!cmbCostCenterName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sbSql.append("and d.strCostCenterCode='" + mapCostCenterNameCode.get(cmbCostCenterName.getSelectedItem().toString()) + "'  ");
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sbSql.append(" and a.strPOSCode='" + selectedPOSCode + "'  ");
	    }
	    if (!cmbWaiterName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sbSql.append(" and a.strWaiterNo='" + mapWaiterNameCode.get(cmbWaiterName.getSelectedItem().toString()) + "'  ");
	    }
	    sbSql.append(" GROUP BY h.strSubGroupCode ");
	    sbSql.append(" Order By h.strSubGroupCode DESC");
	    ResultSet rsQSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());

	    while (rsQSales.next())
	    {
		long sum = 0;
		int qty = 0;
		clsBillDtl objBill = new clsBillDtl();
		objBill.setStrBillNo(rsQSales.getString(1));           //Bill No
		objBill.setStrKOTNo(rsQSales.getString(2));            //KOT No
		objBill.setDteBillDate(rsQSales.getString(3));        //Kot Date
		objBill.setStrItemName(rsQSales.getString(4));        //Item Name
		objBill.setDblQuantity(rsQSales.getDouble(5));        //Qty
		objBill.setDblAmount(rsQSales.getDouble(6));          //Amount
		objBill.setDblBillAmt(rsQSales.getDouble(7));         //Sub Total
		objBill.setTmeBillTime(rsQSales.getString(8));        //Kot Time
		objBill.setTmeOrderProcessing(rsQSales.getString(9)); //Process Time
		objBill.setTmeBillSettleTime(rsQSales.getString(10)); //Puckup Time                
		objBill.setStrProcessTimeDiff(rsQSales.getString(12));  // diff kot & process Time

		//System.out.println(rsQSales.getString(1)+"   "+rsQSales.getString(2));
		objBill.setStrPickUpTimeDiff(rsQSales.getString(13));  // diff process & pickup Time
		objBill.setStrCounterCode(rsQSales.getString(14));     // costCenterName
		objBill.setStrWShortName(rsQSales.getString(15));        // waiterName
		objBill.setStrItemProcessTime(rsQSales.getString(17));        // item process time
		targetTime = rsQSales.getString(18);
		itemTargetTme = timeFormat.parse(targetTime);
		objBill.setStrItemTargetTime(targetTime);        // item target time
		totTaregetTime = itemTargetTme.getTime();
		sumOfTotOrdTarAvg = sumOfTotOrdTarAvg + itemTargetTme.getTime();

		processTime = rsQSales.getString(9);
		dateProcessTime = timeFormat.parse(processTime);
		masterTargTime = dateProcessTime.getTime();

		masterProcTime = timeFormat.parse(rsQSales.getString(17)).getTime();
		totWeighAvgActTme = (masterProcTime * totTaregetTime) / masterProcTime;
		weightedAvgTarTme = timeFormat.format(new Date((masterProcTime * totTaregetTime) / masterProcTime));
		sumWeightedAvgTargetTme = sumWeightedAvgTargetTme + totWeighAvgActTme;
		objBill.setStrWeightedAvgTarTme(weightedAvgTarTme);

		totWeighAvgActTme = (masterTargTime * masterProcTime) / masterProcTime;
		weightedAvgActualTme = timeFormat.format(new Date((masterProcTime * totTaregetTime) / masterProcTime));
		sumWeightedAvgActualTme = sumWeightedAvgActualTme + totWeighAvgActTme;
		objBill.setStrWeightedAvgActualTme(weightedAvgActualTme);

		if (masterTargTime > totTaregetTime)
		{

		    sumOfDelayOrders = sumOfDelayOrders + masterTargTime;
		    sumOfDelayOrderTargetTime = sumOfDelayOrderTargetTime + totTaregetTime;
		    countOfDelayOrder++;
		    j++;
		}

		qty = qty + rsQSales.getInt(5);

		time = rsQSales.getString(12);
		date1 = timeFormat.parse(time);
		sum = sum + date1.getTime();
		sumOfAvgTime = sumOfAvgTime + sum;
		dispTotAvgProcTme = dispTotAvgProcTme + (sum / qty);
		System.out.println(timeFormat.format(new Date(dispTotAvgProcTme)));
		totQty = totQty + qty;
		date3 = timeFormat.format(new Date(sum / qty));
		objBill.setAvgProcessingTime(date3);

		finalDelayedOrder = countOfDelayOrder;

		objBill.setDelayOrders(String.valueOf(finalDelayedOrder));
		sumTotOrders = sumTotOrders + qty;
		objBill.setTotOrders(String.valueOf(qty));

		totOrderePer = ((finalDelayedOrder / qty) * 100);
		sumTotOrderPer = sumTotOrderPer + totOrderePer;
		objBill.setTotOrderesPer(String.valueOf(totOrderePer + "%"));
		totOrdTarAvg = fmt.format(new Date(sumOfTotOrdTarAvg / qty));
		sumTotOrderTarAvg = sumTotOrderTarAvg + (sumOfTotOrdTarAvg / qty);
		objBill.setTotAvg(totOrdTarAvg);
		if (countOfDelayOrder != 0)
		{
		    DOAvg = fmt.format(new Date(sumOfDelayOrderTargetTime / countOfDelayOrder));
		    delayOrderTargAvg = fmt.format(new Date(sumOfDelayOrderTargetTime / countOfDelayOrder));
		    first = sumOfDelayOrderTargetTime / countOfDelayOrder;
		    sumDOAvg = sumDOAvg + (sumOfDelayOrderTargetTime / countOfDelayOrder);
		    sumDelayOrderTAvg = sumDelayOrderTAvg + (sumOfDelayOrderTargetTime / countOfDelayOrder);
		}
		else
		{
		    DOAvg = fmt.format(new Date(sumOfDelayOrderTargetTime));
		    first = sumOfDelayOrderTargetTime;
		    delayOrderTargAvg = fmt.format(new Date(sumOfDelayOrderTargetTime));
		    sumDOAvg = sumDOAvg + sumOfDelayOrderTargetTime;
		    sumDelayOrderTAvg = sumDelayOrderTAvg + sumOfDelayOrderTargetTime;
		}

		objBill.setDoAvg(DOAvg);
		objBill.setDotAvg(delayOrderTargAvg);
		second = sumOfTotOrdTarAvg / qty;
		f1 = first;
		f2 = second;
		finalDota = (f1 / f2) * 100;
		i1 = (int) finalDota;
		l = Long.parseLong(String.valueOf(i1));
		if (finalDota != 0)
		{
		    totDelayOrderTotAvgPer = fmt.format(new Date(l));
		}
		sumDelayOrderTAvgPer = sumDelayOrderTAvgPer + l;
		objBill.setDotAvgPer(totDelayOrderTotAvgPer);

		List<clsBillDtl> arrListBillDtl = new ArrayList<clsBillDtl>();
		//String key=rsQSales.getString(3)+"!"+rsQSales.getString(14);
		String key = rsQSales.getString(16) + "!" + rsQSales.getString(3);

		if (hmKDSFlashData.containsKey(key))
		{
		    arrListBillDtl = hmKDSFlashData.get(key);
		    arrListBillDtl.add(objBill);
		}
		else
		{
		    arrListBillDtl.add(objBill);
		}
		hmKDSFlashData.put(key, arrListBillDtl);

		i++;
	    }

	    String finalTOTAvg = "", finalDOTAvg = "", finalDOTAvgPer = "", date4 = "";
	    String finalDoAvg = "", weightedAvgTargetTime = "", weightedAvgActualTime = "";

	    if (i > 0)
	    {
		date4 = timeFormat.format(new Date(dispTotAvgProcTme / i));

		finalDoAvg = timeFormat.format(new Date(sumDOAvg / i));
		finalTOTAvg = fmt.format(new Date(sumTotOrderTarAvg / i));
		finalDOTAvg = fmt.format(new Date(sumDelayOrderTAvg / i));
		weightedAvgTargetTime = timeFormat.format(new Date(sumWeightedAvgTargetTme));
		finalDOTAvgPer = fmt.format(new Date(sumWeightedAvgTargetTme / (sumTotOrderTarAvg / i)));
		weightedAvgActualTime = timeFormat.format(new Date(sumWeightedAvgActualTme));
	    }
	    //System.out.println("The sum is "+date3);

	    System.out.println(hmKDSFlashData.keySet());

	    for (Map.Entry<String, List<clsBillDtl>> entry : hmKDSFlashData.entrySet())
	    {
		Object[] records = new Object[18];

		for (int cnt = 0; cnt < entry.getValue().size(); cnt++)
		{
		    clsBillDtl objBill = entry.getValue().get(cnt);

		    records = new Object[14];
		    records[0] = entry.getKey().split("!")[0];
		    records[1] = objBill.getAvgProcessingTime();
		    records[2] = objBill.getDelayOrders();
		    records[3] = objBill.getTotOrders() + "  ";
		    records[4] = "  " + objBill.getTotOrderesPer();
		    records[5] = objBill.getDoAvg();
		    records[6] = objBill.getTotAvg();
		    records[7] = objBill.getDotAvg();
		    records[8] = objBill.getStrWeightedAvgTarTme();
		    records[9] = objBill.getStrWeightedAvgActualTme();
		    records[10] = objBill.getDotAvgPer();
		    sumQty = sumQty + objBill.getDblQuantity();
		    dm.addRow(records);
		}

		records = new Object[16];
		dm.addRow(records);
	    }
	    Object[] ob1 =
	    {
		"Total", date4, finalDelayedOrder, sumTotOrders, "", finalDoAvg, finalTOTAvg, finalDOTAvg, weightedAvgTargetTime, weightedAvgActualTime, finalDOTAvgPer
	    };
	    totalDm.addRow(ob1);
	    tblKDS.setModel(dm);
	    tblTotal7.setModel(totalDm);
	    tblTotal7.setRowHeight(40);

	    DefaultTableCellRenderer rightRenderer1 = new DefaultTableCellRenderer();
	    rightRenderer1.setHorizontalAlignment(JLabel.RIGHT);
	    DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
	    centerRenderer.setHorizontalAlignment(JLabel.CENTER);

	    tblKDS.getColumnModel().getColumn(2).setCellRenderer(rightRenderer1);
	    tblKDS.getColumnModel().getColumn(3).setCellRenderer(rightRenderer1);
	    tblKDS.getColumnModel().getColumn(4).setCellRenderer(rightRenderer1);
	    tblKDS.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

	    tblKDS.getColumnModel().getColumn(0).setPreferredWidth(120);// Bill No
	    tblKDS.getColumnModel().getColumn(1).setPreferredWidth(120);// KOT No
	    tblKDS.getColumnModel().getColumn(2).setPreferredWidth(90);// waiter Name
	    tblKDS.getColumnModel().getColumn(3).setPreferredWidth(90);// Item Name
	    tblKDS.getColumnModel().getColumn(4).setPreferredWidth(100);// Qty
	    tblKDS.getColumnModel().getColumn(5).setPreferredWidth(100);// KOT Time
	    tblKDS.getColumnModel().getColumn(6).setPreferredWidth(120);// Process Time
	    tblKDS.getColumnModel().getColumn(7).setPreferredWidth(120);// PickedUp Time
	    tblKDS.getColumnModel().getColumn(8).setPreferredWidth(120);// KOT Time - Process Time

	    tblTotal7.setSize(400, 500);
	    rightRenderer1.setHorizontalAlignment(JLabel.RIGHT);
	    tblTotal7.getColumnModel().getColumn(2).setCellRenderer(rightRenderer1);
	    tblTotal7.getColumnModel().getColumn(3).setCellRenderer(rightRenderer1);
	    tblTotal7.getColumnModel().getColumn(4).setCellRenderer(rightRenderer1);
	    tblTotal7.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    tblTotal7.getColumnModel().getColumn(0).setPreferredWidth(120);
	    tblTotal7.getColumnModel().getColumn(1).setPreferredWidth(120);
	    tblTotal7.getColumnModel().getColumn(2).setPreferredWidth(90);
	    tblTotal7.getColumnModel().getColumn(3).setPreferredWidth(90);
	    tblTotal7.getColumnModel().getColumn(4).setPreferredWidth(100);
	    tblTotal7.getColumnModel().getColumn(5).setPreferredWidth(100);
	    tblTotal7.getColumnModel().getColumn(6).setPreferredWidth(120);
	    tblTotal7.getColumnModel().getColumn(7).setPreferredWidth(120);
	    tblTotal7.getColumnModel().getColumn(8).setPreferredWidth(120);

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }
}
