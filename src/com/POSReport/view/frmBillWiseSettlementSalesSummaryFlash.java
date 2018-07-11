package com.POSReport.view;

import com.POSGlobal.controller.clsExportDocument;
import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsPosConfigFile;
import com.POSGlobal.controller.clsSalesFlashColumns;
import com.POSGlobal.controller.clsTaxCalculationDtls;
import com.POSGlobal.controller.clsUtility;
import com.POSGlobal.view.frmOkPopUp;
import com.POSReport.controller.clsBillRegisterReport;
import com.POSReport.controller.clsBillWiseSalesReport;
import com.POSReport.controller.comparator.clsSalesFlashComparator;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

public class frmBillWiseSettlementSalesSummaryFlash extends javax.swing.JFrame
{

    private String selectQuery;
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

    private Map<String, String> mapAllTaxes;
    private Map<String, String> mapGroupNameCode;
    private final DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();
    private ArrayList<clsSalesFlashColumns> arrTempListBillWiseSales;

    public frmBillWiseSettlementSalesSummaryFlash()
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
	 * this Function is used for Component initialization
	 */
	funSetLookAndFeel();

	reportName = "BillWiseSettlementSalesSummaryFlash";
	exportFormName = "BillWiseSettlementSalesSummaryFlash";
	this.setLocationRelativeTo(null);
	//load all POS
	funFillPOSComboBox();
	//load all POS
	mapSettlementNameCode = new HashMap<String, String>();
	mapGroupNameCode = new HashMap<String, String>();
	funFillSettlementNameComboBox();
	funFillGroupNameComboBox();

	dteFromDate.setDate(objUtility.funGetDateToSetCalenderDate());
	dteToDate.setDate(objUtility.funGetDateToSetCalenderDate());

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
        tblBillWiseSalesSummary = new javax.swing.JTable();
        btnExecute = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();
        pnltotal = new javax.swing.JScrollPane();
        tblTotal = new javax.swing.JTable();
        btnExport = new javax.swing.JButton();
        lblPosCode1 = new javax.swing.JLabel();
        cmbGroupWise = new javax.swing.JComboBox();
        lblOperationType = new javax.swing.JLabel();
        cmbOperationType = new javax.swing.JComboBox();
        lblSettlementName = new javax.swing.JLabel();
        cmbGroupName = new javax.swing.JComboBox();
        lblGroupName = new javax.swing.JLabel();
        cmbSettlementName = new javax.swing.JComboBox();

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

        tblBillWiseSalesSummary.setFont(new java.awt.Font("Microsoft Sans Serif", 0, 11)); // NOI18N
        tblBillWiseSalesSummary.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {

            },
            new String []
            {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tblBillWiseSalesSummary.setRowHeight(25);
        pnlDayEnd.setViewportView(tblBillWiseSalesSummary);

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

        tblTotal.setFont(new java.awt.Font("Microsoft Sans Serif", 1, 12)); // NOI18N
        tblTotal.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {
                {null, null, null, null}
            },
            new String []
            {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        )
        {
            boolean[] canEdit = new boolean []
            {
                true, false, false, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
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
        btnExport.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnExportActionPerformed(evt);
            }
        });

        lblPosCode1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblPosCode1.setText("View By    :");

        cmbGroupWise.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "BILL REGISTER", "ITEM'S GROUP WISE", "NONE" }));
        cmbGroupWise.setToolTipText("Select POS");

        lblOperationType.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblOperationType.setText("Operation Type :");

        cmbOperationType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "All", "Dine In", "Direct Biller", "Home Delivery", "Take Away" }));
        cmbOperationType.setToolTipText("Select POS");

        lblSettlementName.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblSettlementName.setText("Settlement Name:");

        cmbGroupName.setToolTipText("Select POS");
        cmbGroupName.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                cmbGroupNameActionPerformed(evt);
            }
        });

        lblGroupName.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblGroupName.setText("Group Name:");

        cmbSettlementName.setToolTipText("Select POS");
        cmbSettlementName.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                cmbSettlementNameActionPerformed(evt);
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
                        .addGap(7, 7, 7)
                        .addComponent(cmbPosCode, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(lblFromDate)
                        .addGap(3, 3, 3)
                        .addComponent(dteFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(dteToDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(lblPosCode1, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbGroupWise, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addComponent(lblSettlementName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbSettlementName, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblOperationType)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cmbOperationType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblGroupName, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbGroupName, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnExecute, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnExport, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(22, 22, 22))
        );
        pnlMainLayout.setVerticalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cmbPosCode, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(dteFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dteToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPosCode, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblPosCode1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cmbGroupWise, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblSettlementName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cmbSettlementName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblOperationType, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cmbOperationType, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblGroupName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addComponent(btnExport, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addComponent(btnExecute, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addComponent(cmbGroupName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlDayEnd, javax.swing.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
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
	clsGlobalVarClass.hmActiveForms.remove("BillWiseSettlementSalesSummaryFlash");
    }//GEN-LAST:event_btnCloseMouseClicked

    private void btnExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportActionPerformed

	try
	{
	    File theDir = new File(ExportReportPath);

	    if (cmbGroupWise.getSelectedItem().toString().equalsIgnoreCase("BILL REGISTER"))
	    {
		exportFormName = "Bill Register";
		reportName = "Bill Register";

		clsBillRegisterReport objBillRegisterReport = new clsBillRegisterReport();
		objBillRegisterReport.funFillTableForBillRegister(dteFromDate, dteToDate, fromDate, toDate, selectedPOSCode, tblBillWiseSalesSummary, this);
	    }
	    else
	    {
		File file = new File(ExportReportPath + "/" + exportFormName + objUtility.funGetDateInString() + ".xls");
		if (!theDir.exists())
		{
		    theDir.mkdir();
		    funExportFile(tblBillWiseSalesSummary, file);
		    //sendMail();
		}
		else
		{
		    funExportFile(tblBillWiseSalesSummary, file);
		    //sendMail();
		}
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
	clsGlobalVarClass.hmActiveForms.remove("BillWiseSettlementSalesSummaryFlash");
    }//GEN-LAST:event_btnCloseActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("BillWiseSettlementSalesSummaryFlash");
    }//GEN-LAST:event_formWindowClosed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("BillWiseSettlementSalesSummaryFlash");
    }//GEN-LAST:event_formWindowClosing

    private void cmbGroupNameActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cmbGroupNameActionPerformed
    {//GEN-HEADEREND:event_cmbGroupNameActionPerformed
	// TODO add your handling code here:
    }//GEN-LAST:event_cmbGroupNameActionPerformed

    private void cmbSettlementNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbSettlementNameActionPerformed
	// TODO add your handling code here:
    }//GEN-LAST:event_cmbSettlementNameActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnExecute;
    private javax.swing.JButton btnExport;
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
    private javax.swing.JTable tblBillWiseSalesSummary;
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
	    SimpleDateFormat yyyyMMddDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	    Date fDate = dteFromDate.getDate();
	    Date tDate = dteToDate.getDate();

	    fromDate = yyyyMMddDateFormat.format(fDate);
	    toDate = yyyyMMddDateFormat.format(tDate);
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
	    else if (cmbGroupWise.getSelectedItem().toString().equalsIgnoreCase("BILL REGISTER"))
	    {
		funFillTableForBillRegister();
	    }
	    else//none
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

	    Vector<String> billNoCol = new Vector<>();
	    Vector<String> posCol = new Vector<>();
	    Vector<String> dateCol = new Vector<>();
	    Map<String, String> mapBillNo = new HashMap<String, String>();
	    //fill Q Bill No and POS            
	    String sqlBills = "select a.strBillNo,DATE_FORMAT(a.dteBillDate,'%d-%m-%Y') as date  "
		    + "from "
		    + "tblqbillhd a,tblqbillsettlementdtl b,tblsettelmenthd c "
		    + "where  "
		    + "a.strBillNo=b.strBillNo "
		    + "and b.strSettlementCode=c.strSettelmentCode "
		    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ";
	    if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlBills += "and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ";
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sqlBills = sqlBills + " and a.strPOSCode='" + selectedPOSCode + "' ";
	    }
	    if (!cmbSettlementName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlBills = sqlBills + " and c.strSettelmentCode='" + mapSettlementNameCode.get(cmbSettlementName.getSelectedItem().toString()) + "' ";
	    }
	    sqlBills = sqlBills + "group by a.strBillNo "
		    + "order by a.strBillNo; ";
	    ResultSet rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlBills);
	    while (rsSales.next())
	    {
		mapBillNo.put(rsSales.getString(1), rsSales.getString(2));
	    }
	    //fill Live Bill No and POS            
	    sqlBills = "select a.strBillNo,DATE_FORMAT(a.dteBillDate,'%d-%m-%Y') as date  "
		    + "from "
		    + "tblbillhd a,tblbillsettlementdtl b,tblsettelmenthd c "
		    + "where  "
		    + "a.strBillNo=b.strBillNo "
		    + "and b.strSettlementCode=c.strSettelmentCode "
		    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ";
	    if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlBills += "and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ";
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sqlBills = sqlBills + " and a.strPOSCode='" + selectedPOSCode + "' ";
	    }
	    if (!cmbSettlementName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlBills = sqlBills + " and c.strSettelmentCode='" + mapSettlementNameCode.get(cmbSettlementName.getSelectedItem().toString()) + "' ";
	    }
	    sqlBills = sqlBills + "group by a.strBillNo "
		    + "order by a.strBillNo; ";
	    rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlBills);
	    while (rsSales.next())
	    {
		mapBillNo.put(rsSales.getString(1), rsSales.getString(2));
	    }
	    Iterator<Map.Entry<String, String>> itBillNos = mapBillNo.entrySet().iterator();
	    while (itBillNos.hasNext())
	    {
		Map.Entry<String, String> entry = itBillNos.next();
		String billNo = entry.getKey();
		String billDate = entry.getValue();
		billNoCol.add(billNo);
		dateCol.add(billDate);
		posCol.add(selectedPOSName.toUpperCase());
	    }
	    //Sort bills
	    Collections.sort(billNoCol);
	    dm.addColumn("BILL NO", billNoCol);
	    dm.addColumn("DATE", dateCol);
	    dm.addColumn("POS", posCol);
	    totalDm.addColumn("TOTALS");
	    totalDm.addColumn("");
	    totalDm.addColumn("POS");

	    Map<String, String> mapSettlements = new HashMap<>();
	    //fill Q settlement whoes amt>0
	    String sqlSettlement = "SELECT c.strSettelmentDesc "
		    + "FROM tblqbillhd a,tblqbillsettlementdtl b,tblsettelmenthd c "
		    + "WHERE a.strBillNo=b.strBillNo  "
		    + "AND b.strSettlementCode=c.strSettelmentCode   "
		    + "and b.dblSettlementAmt>0 "
		    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ";
	    if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlSettlement += "and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ";
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sqlSettlement = sqlSettlement + " and a.strPOSCode='" + selectedPOSCode + "' ";
	    }
	    if (!cmbSettlementName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlSettlement = sqlSettlement + " and c.strSettelmentCode='" + mapSettlementNameCode.get(cmbSettlementName.getSelectedItem().toString()) + "' ";
	    }
	    sqlSettlement = sqlSettlement + "GROUP BY strSettelmentDesc "
		    + "ORDER BY strSettelmentDesc; ";
	    rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlSettlement);
	    while (rsSales.next())
	    {
		mapSettlements.put(rsSales.getString(1).toUpperCase(), rsSales.getString(1).toUpperCase());
	    }
	    rsSales.close();
	    //fill Live settlement whoes amt>0
	    sqlSettlement = "SELECT c.strSettelmentDesc "
		    + "FROM tblbillhd a,tblbillsettlementdtl b,tblsettelmenthd c "
		    + "WHERE a.strBillNo=b.strBillNo  "
		    + "AND b.strSettlementCode=c.strSettelmentCode   "
		    + "and b.dblSettlementAmt>0 "
		    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ";
	    if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlSettlement += "and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ";
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sqlSettlement = sqlSettlement + " and a.strPOSCode='" + selectedPOSCode + "' ";
	    }
	    if (!cmbSettlementName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlSettlement = sqlSettlement + " and c.strSettelmentCode='" + mapSettlementNameCode.get(cmbSettlementName.getSelectedItem().toString()) + "' ";
	    }
	    sqlSettlement = sqlSettlement + "GROUP BY strSettelmentDesc "
		    + "ORDER BY strSettelmentDesc; ";
	    rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlSettlement);
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
	    mapAllTaxes = new HashMap<>();
	    String taxCalType = "";
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
	    tblBillWiseSalesSummary.setRowHeight(25);
	    tblBillWiseSalesSummary.setModel(dm);
	    tblBillWiseSalesSummary.setAutoscrolls(true);
	    tblBillWiseSalesSummary.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

	    //fill Q Data           
	    String sqlTransRecords = "select a.strBillNo,c.strSettelmentDesc,sum(b.dblSettlementAmt) "
		    + "from "
		    + "tblqbillhd a,tblqbillsettlementdtl b,tblsettelmenthd c "
		    + "where  "
		    + "a.strBillNo=b.strBillNo "
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
	    if (!cmbSettlementName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlTransRecords = sqlTransRecords + " and c.strSettelmentCode='" + mapSettlementNameCode.get(cmbSettlementName.getSelectedItem().toString()) + "' ";
	    }
	    sqlTransRecords = sqlTransRecords + "group by a.strBillNo,b.strSettlementCode "
		    + "order by a.strBillNo,b.strSettlementCode;";
	    funFillSettlementData(sqlTransRecords);

	    String sqlTax = "select a.strBillNo,c.strTaxDesc,sum(b.dblTaxAmount) "
		    + "from "
		    + "tblqbillhd a,tblqbilltaxdtl b,tbltaxhd c,tblqbillsettlementdtl d,tblsettelmenthd e "
		    + "where a.strBillNo=b.strBillNo "
		    + "and b.strTaxCode=c.strTaxCode "
		    + "and a.strBillNo=d.strBillNo "
		    + "and d.strSettlementCode=e.strSettelmentCode "
		    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ";
	    if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlTax += "and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ";
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sqlTax = sqlTax + " and a.strPOSCode='" + selectedPOSCode + "' ";
	    }
	    if (!cmbSettlementName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlTax = sqlTax + " and e.strSettelmentCode='" + mapSettlementNameCode.get(cmbSettlementName.getSelectedItem().toString()) + "' ";
	    }
	    sqlTax = sqlTax + "group by a.strBillNo,b.strTaxCode "
		    + "order by a.strBillNo,b.strTaxCode;  ";
	    funFillTaxData(sqlTax);

	    //fill Live Data
	    sqlTransRecords = "select a.strBillNo,c.strSettelmentDesc,sum(b.dblSettlementAmt) "
		    + "from "
		    + "tblbillhd a,tblbillsettlementdtl b,tblsettelmenthd c "
		    + "where  "
		    + " a.strBillNo=b.strBillNo "
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
	    if (!cmbSettlementName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlTransRecords = sqlTransRecords + " and c.strSettelmentCode='" + mapSettlementNameCode.get(cmbSettlementName.getSelectedItem().toString()) + "' ";
	    }
	    sqlTransRecords = sqlTransRecords + "group by a.strBillNo,b.strSettlementCode "
		    + "order by a.strBillNo,b.strSettlementCode;";
	    funFillSettlementData(sqlTransRecords);

	    sqlTax = "select a.strBillNo,c.strTaxDesc,sum(b.dblTaxAmount) "
		    + "from "
		    + "tblbillhd a,tblbilltaxdtl b,tbltaxhd c,tblbillsettlementdtl d,tblsettelmenthd e "
		    + "where a.strBillNo=b.strBillNo "
		    + "and b.strTaxCode=c.strTaxCode "
		    + "and a.strBillNo=d.strBillNo "
		    + "and d.strSettlementCode=e.strSettelmentCode "
		    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ";
	    if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlTax += "and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ";
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sqlTax = sqlTax + " and a.strPOSCode='" + selectedPOSCode + "' ";
	    }
	    if (!cmbSettlementName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlTax = sqlTax + " and e.strSettelmentCode='" + mapSettlementNameCode.get(cmbSettlementName.getSelectedItem().toString()) + "' ";
	    }
	    sqlTax = sqlTax + "group by a.strBillNo,b.strTaxCode "
		    + "order by a.strBillNo,b.strTaxCode;  ";
	    funFillTaxData(sqlTax);

	    //set null values to 0.00
	    for (int row = 0; row < tblBillWiseSalesSummary.getRowCount(); row++)
	    {
		for (int col = 0; col < tblBillWiseSalesSummary.getColumnCount(); col++)
		{
		    if (null == tblBillWiseSalesSummary.getValueAt(row, col))
		    {
			tblBillWiseSalesSummary.setValueAt("0.00", row, col);
		    }
		}
	    }

	    //set day total
	    dm.addColumn("Grand Total".toUpperCase());
	    totalDm.addColumn("Grand Total".toUpperCase());
	    Map<String, Double> mapDayGrandTotal = new HashMap<>();
	    //Q
	    String sqlGrandTotal = "SELECT a.strBillNo,sum(b.dblSettlementAmt) "
		    + "FROM tblqbillhd a,tblqbillsettlementdtl b,tblsettelmenthd c,tblposmaster d "
		    + "WHERE a.strBillNo=b.strBillNo  "
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
	    if (!cmbSettlementName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlGrandTotal = sqlGrandTotal + " and c.strSettelmentCode='" + mapSettlementNameCode.get(cmbSettlementName.getSelectedItem().toString()) + "' ";
	    }
	    sqlGrandTotal = sqlGrandTotal + "GROUP BY a.strBillNo "
		    + "ORDER BY a.strBillNo; ";
	    rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlGrandTotal);
	    while (rsSales.next())
	    {
		mapDayGrandTotal.put(rsSales.getString(1), rsSales.getDouble(2));
	    }
	    rsSales.close();

	    sqlGrandTotal = "SELECT a.strBillNo,sum(b.dblSettlementAmt) "
		    + "FROM tblbillhd a,tblbillsettlementdtl b,tblsettelmenthd c,tblposmaster d "
		    + "WHERE a.strBillNo=b.strBillNo  "
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
	    if (!cmbSettlementName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlGrandTotal = sqlGrandTotal + " and c.strSettelmentCode='" + mapSettlementNameCode.get(cmbSettlementName.getSelectedItem().toString()) + "' ";
	    }
	    sqlGrandTotal = sqlGrandTotal + "GROUP BY a.strBillNo "
		    + "ORDER BY a.strBillNo; ";
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
	    }
	    rsSales.close();
	    int lasColumn = tblBillWiseSalesSummary.getColumnCount() - 1;
	    for (int row = 0; row < tblBillWiseSalesSummary.getRowCount(); row++)
	    {
		tblBillWiseSalesSummary.setValueAt(mapDayGrandTotal.get(tblBillWiseSalesSummary.getValueAt(row, 0).toString()), row, lasColumn);
	    }

	    // set total
	    Object[] totalRow = new Object[tblBillWiseSalesSummary.getColumnCount()];
	    totalRow[0] = "Totals".toUpperCase();
	    totalRow[1] = "";
	    totalRow[2] = selectedPOSName.toUpperCase();
	    for (int col = 3; col < tblBillWiseSalesSummary.getColumnCount(); col++)
	    {
		double total = 0.00;
		for (int row = 0; row < tblBillWiseSalesSummary.getRowCount(); row++)
		{
		    if (tblBillWiseSalesSummary.getValueAt(row, col) == null)
		    {
			tblBillWiseSalesSummary.setValueAt("0.00", row, col);
			total += Double.parseDouble(tblBillWiseSalesSummary.getValueAt(row, col).toString());
		    }
		    else
		    {
			total += Double.parseDouble(tblBillWiseSalesSummary.getValueAt(row, col).toString());
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
	    for (int cnt = 3; cnt < tblTotal.getColumnCount(); cnt++)
	    {
		tblTotal.getColumnModel().getColumn(cnt).setPreferredWidth(100);
		tblTotal.getColumnModel().getColumn(cnt).setCellRenderer(rightRenderer);
	    }

	    tblBillWiseSalesSummary.getColumnModel().getColumn(0).setPreferredWidth(70);
	    tblBillWiseSalesSummary.getColumnModel().getColumn(1).setPreferredWidth(100);
	    for (int cnt = 3; cnt < tblBillWiseSalesSummary.getColumnCount(); cnt++)
	    {
		tblBillWiseSalesSummary.getColumnModel().getColumn(cnt).setPreferredWidth(100);
		tblBillWiseSalesSummary.getColumnModel().getColumn(cnt).setCellRenderer(rightRenderer);
	    }

	    //delete columns whoes amount <=0
	    funDeleteColumn();

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funFillSettlementData(String sqlTransRecords)
    {
	try
	{
	    ResultSet rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlTransRecords);
	    while (rsSales.next())
	    {
		for (int tblRow = 0; tblRow < tblBillWiseSalesSummary.getRowCount(); tblRow++)
		{
		    if (tblBillWiseSalesSummary.getValueAt(tblRow, 0).toString().equalsIgnoreCase(rsSales.getString(1)))
		    {
			for (int tblCol = 3; tblCol < tblBillWiseSalesSummary.getColumnCount(); tblCol++)
			{
			    if (tblBillWiseSalesSummary.getColumnName(tblCol).toUpperCase().equals(rsSales.getString(2).toUpperCase()))
			    {
				if (null == tblBillWiseSalesSummary.getValueAt(tblRow, tblCol))
				{
				    tblBillWiseSalesSummary.setValueAt(gDecimalFormat.format(rsSales.getDouble(3)), tblRow, tblCol);
				}
				else
				{
				    Double value = Double.parseDouble(tblBillWiseSalesSummary.getValueAt(tblRow, tblCol).toString()) + rsSales.getDouble(3);
				    tblBillWiseSalesSummary.setValueAt(gDecimalFormat.format(value), tblRow, tblCol);
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

    private void funExportFile(JTable tblDayWiseSalesSummary, File file)
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
	    funAddLastOfExportReport(workbook1);
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

    private void funFillTaxData(String sqlTax)
    {
	try
	{
	    ResultSet rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlTax);
	    while (rsSales.next())
	    {
		for (int tblRow = 0; tblRow < tblBillWiseSalesSummary.getRowCount(); tblRow++)
		{
		    if (tblBillWiseSalesSummary.getValueAt(tblRow, 0).toString().equalsIgnoreCase(rsSales.getString(1)))
		    {
			for (int tblCol = 4; tblCol < tblBillWiseSalesSummary.getColumnCount(); tblCol++)
			{
			    if (tblBillWiseSalesSummary.getColumnName(tblCol).toUpperCase().equals(rsSales.getString(2).toUpperCase()))
			    {
				if (null == tblBillWiseSalesSummary.getValueAt(tblRow, tblCol))
				{
				    tblBillWiseSalesSummary.setValueAt(gDecimalFormat.format(rsSales.getDouble(3)), tblRow, tblCol);
				}
				else
				{
				    Double value = Double.parseDouble(tblBillWiseSalesSummary.getValueAt(tblRow, tblCol).toString()) + rsSales.getDouble(3);
				    tblBillWiseSalesSummary.setValueAt(gDecimalFormat.format(value), tblRow, tblCol);
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
	    mapAllTaxes = new HashMap<>();

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
	    if (!cmbSettlementName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sbSql.append(" and c.strSettelmentCode='" + mapSettlementNameCode.get(cmbSettlementName.getSelectedItem().toString()) + "' ");
	    }
	    if (!cmbGroupName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sbSql.append(" and f.strGroupCode='" + mapGroupNameCode.get(cmbGroupName.getSelectedItem().toString()) + "' ");
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
	    if (!cmbSettlementName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sbSql.append(" and c.strSettelmentCode='" + mapSettlementNameCode.get(cmbSettlementName.getSelectedItem().toString()) + "' ");
	    }
	    if (!cmbGroupName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sbSql.append(" and f.strGroupCode='" + mapGroupNameCode.get(cmbGroupName.getSelectedItem().toString()) + "' ");
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

	    /*
             Iterator<Map.Entry<String,String>> itBillNos = mapBillNo.entrySet().iterator();
             while (itBillNos.hasNext())
             {
             Map.Entry<String,String> entry= itBillNos.next();
             String billNo = entry.getKey();
             String billDate = entry.getValue();
             billNoCol.add(billNo);
             dateCol.add(billDate);
             posCol.add(selectedPOSName.toUpperCase());
             }*/
	    dm.addColumn("BILL NO", billNoCol);
	    dm.addColumn("DATE", dateCol);
	    dm.addColumn("POS", posCol);
	    totalDm.addColumn("TOTALS");
	    totalDm.addColumn("");
	    totalDm.addColumn("POS");

	    //add group columns
	    String sqlGroups = "select a.strGroupName from tblgrouphd a ";
	    if (!cmbGroupName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlGroups += "where a.strGroupCode='" + mapGroupNameCode.get(cmbGroupName.getSelectedItem().toString()) + "' ";
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
	    if (!cmbSettlementName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sbSql.append(" and c.strSettelmentCode='" + mapSettlementNameCode.get(cmbSettlementName.getSelectedItem().toString()) + "' ");
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
	    if (!cmbSettlementName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sbSql.append(" and c.strSettelmentCode='" + mapSettlementNameCode.get(cmbSettlementName.getSelectedItem().toString()) + "' ");
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
	    tblBillWiseSalesSummary.setRowHeight(25);
	    tblBillWiseSalesSummary.setModel(dm);
	    tblBillWiseSalesSummary.setAutoscrolls(true);
	    tblBillWiseSalesSummary.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

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
	    if (!cmbGroupName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlGroups = sqlGroups + " and g.strGroupCode='" + mapGroupNameCode.get(cmbGroupName.getSelectedItem().toString()) + "' ";
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
	    if (!cmbGroupName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlGroups = sqlGroups + " and g.strGroupCode='" + mapGroupNameCode.get(cmbGroupName.getSelectedItem().toString()) + "' ";
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
	    if (!cmbSettlementName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlTransRecords = sqlTransRecords + " and c.strSettelmentCode='" + mapSettlementNameCode.get(cmbSettlementName.getSelectedItem().toString()) + "' ";
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
	    if (!cmbSettlementName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlTransRecords = sqlTransRecords + " and c.strSettelmentCode='" + mapSettlementNameCode.get(cmbSettlementName.getSelectedItem().toString()) + "' ";
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

	    /*            
             update group wise subtotal settlement wise
	     */
	    funFillSettlementWiseBillWiseAmtMapForLive();
	    funFillSettlementWiseBillWiseAmtMapForQFile();
//            for (Map.Entry<String, Double> entry : mapSettlemetWiseAmt.entrySet())
//            {
//                System.out.println(entry.getKey() + "->" + entry.getValue());
//            }
	    funUpdateSettWiseBillWiseAmtForLive(columnNameForBillDtl);
	    funUpdateSettWiseBillWiseAmtForQFile(columnNameForBillDtl);
	    /*            
             update group wise Taxees 
	     */
	    funUpdateSettWiseBillWiseTaxAmtForLive();
	    funUpdateSettWiseBillWiseTaxAmtForQFile();

	    //set null values to 0.00
	    for (int row = 0; row < tblBillWiseSalesSummary.getRowCount(); row++)
	    {
		for (int col = 0; col < tblBillWiseSalesSummary.getColumnCount(); col++)
		{
		    if (null == tblBillWiseSalesSummary.getValueAt(row, col))
		    {
			tblBillWiseSalesSummary.setValueAt("0.00", row, col);
		    }
		}
	    }

	    //set day total
	    dm.addColumn("Grand Total".toUpperCase());
	    totalDm.addColumn("Grand Total".toUpperCase());
	    Map<String, Double> mapDayGrandTotal = new HashMap<>();
	    Map<String, String> mapCardNo = new HashMap<>();
	    String sqlGrandTotal = "SELECT a.strBillNo,sum(b.dblSettlementAmt),if(b.strGiftVoucherCode='',b.strCardName,b.strGiftVoucherCode)    "
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
	    if (!cmbSettlementName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlGrandTotal = sqlGrandTotal + " and c.strSettelmentCode='" + mapSettlementNameCode.get(cmbSettlementName.getSelectedItem().toString()) + "' ";
	    }
	    sqlGrandTotal = sqlGrandTotal + "GROUP BY a.strClientCode,a.strBillNo,b.strSettlementCode "
		    + "ORDER BY a.strClientCode,a.strBillNo; ";
	    rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlGrandTotal);
	    while (rsSales.next())
	    {
		mapDayGrandTotal.put(rsSales.getString(1), rsSales.getDouble(2));
		mapCardNo.put(rsSales.getString(1), rsSales.getString(3));
	    }
	    rsSales.close();

	    //live
	    sqlGrandTotal = "SELECT a.strBillNo,sum(b.dblSettlementAmt),if(b.strGiftVoucherCode='',b.strCardName,b.strGiftVoucherCode)    "
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
	    if (!cmbSettlementName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlGrandTotal = sqlGrandTotal + " and c.strSettelmentCode='" + mapSettlementNameCode.get(cmbSettlementName.getSelectedItem().toString()) + "' ";
	    }
	    sqlGrandTotal = sqlGrandTotal + "GROUP BY a.strClientCode,a.strBillNo,b.strSettlementCode "
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

	    int lasColumn = tblBillWiseSalesSummary.getColumnCount() - 1;
	    for (int row = 0; row < tblBillWiseSalesSummary.getRowCount(); row++)
	    {
		tblBillWiseSalesSummary.setValueAt(mapDayGrandTotal.get(tblBillWiseSalesSummary.getValueAt(row, 0).toString()), row, lasColumn);
	    }

	    // set total
	    Object[] totalRow = new Object[tblBillWiseSalesSummary.getColumnCount()];
	    totalRow[0] = "Totals".toUpperCase();
	    totalRow[1] = "";
	    totalRow[2] = selectedPOSName.toUpperCase();
	    for (int col = 3; col < tblBillWiseSalesSummary.getColumnCount(); col++)
	    {
		double total = 0.00;
		for (int row = 0; row < tblBillWiseSalesSummary.getRowCount(); row++)
		{
		    if (tblBillWiseSalesSummary.getValueAt(row, col) == null)
		    {
			tblBillWiseSalesSummary.setValueAt("0.00", row, col);
			total += Double.parseDouble(tblBillWiseSalesSummary.getValueAt(row, col).toString());
		    }
		    else
		    {
			total += Double.parseDouble(tblBillWiseSalesSummary.getValueAt(row, col).toString());
		    }
		}
		totalRow[col] = gDecimalFormat.format(total);
	    }

	    //set settle card no if exist
	    dm.addColumn("Card No".toUpperCase());
	    int lasColumnForCardName = tblBillWiseSalesSummary.getColumnCount() - 1;
	    for (int row = 0; row < tblBillWiseSalesSummary.getRowCount(); row++)
	    {
		tblBillWiseSalesSummary.setValueAt(mapCardNo.get(tblBillWiseSalesSummary.getValueAt(row, 0).toString()), row, lasColumnForCardName);
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

	    tblBillWiseSalesSummary.getColumnModel().getColumn(0).setPreferredWidth(70);
	    tblBillWiseSalesSummary.getColumnModel().getColumn(1).setPreferredWidth(100);
	    for (int cnt = 3; cnt < tblBillWiseSalesSummary.getColumnCount(); cnt++)
	    {
		tblBillWiseSalesSummary.getColumnModel().getColumn(cnt).setPreferredWidth(100);
		tblBillWiseSalesSummary.getColumnModel().getColumn(cnt).setCellRenderer(rightRenderer);
	    }

	    //delete columns whoes amount <=0
	    funDeleteColumn();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funFillGroupWiseSalesData(String sqlGroups)
    {
	try
	{
	    ResultSet rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlGroups);
	    while (rsSales.next())
	    {
		for (int tblRow = 0; tblRow < tblBillWiseSalesSummary.getRowCount(); tblRow++)
		{
		    if (tblBillWiseSalesSummary.getValueAt(tblRow, 0).toString().equalsIgnoreCase(rsSales.getString(1)))
		    {
			for (int tblCol = 3; tblCol < tblBillWiseSalesSummary.getColumnCount(); tblCol++)
			{
			    if (tblBillWiseSalesSummary.getColumnName(tblCol).toUpperCase().equals(rsSales.getString(2).toUpperCase()))
			    {
				if (null == tblBillWiseSalesSummary.getValueAt(tblRow, tblCol))
				{
				    tblBillWiseSalesSummary.setValueAt(gDecimalFormat.format(rsSales.getDouble(3)), tblRow, tblCol);
				}
				else
				{
				    Double value = Double.parseDouble(tblBillWiseSalesSummary.getValueAt(tblRow, tblCol).toString()) + rsSales.getDouble(3);
				    tblBillWiseSalesSummary.setValueAt(gDecimalFormat.format(value), tblRow, tblCol);
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
	    for (int cnt = 3; cnt < tblTotal.getColumnCount(); cnt++)
	    {
		if (!(Double.parseDouble(tblTotal.getValueAt(0, cnt).toString()) > 0))
		{
		    tblBillWiseSalesSummary.getColumnModel().removeColumn(tblBillWiseSalesSummary.getColumnModel().getColumn(cnt));
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

	    selectQuery = "select a.strSettelmentCode,a.strSettelmentDesc,a.strSettelmentType "
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

    private void funFillSettlementWiseBillWiseAmtMapForLive()
    {
	try
	{
	    //fill Q Data           
	    String sqlTransRecords = "select a.strBillNo,c.strSettelmentDesc,sum(b.dblSettlementAmt) "
		    + "from "
		    + "tblbillhd a,tblbillsettlementdtl b,tblsettelmenthd c "
		    + "where  "
		    + "a.strBillNo=b.strBillNo  "
		    + "and date(a.dteBillDate)=date(b.dteBillDate) "
		    + "and a.strClientCode=b.strClientCode   "
		    + "and b.strSettlementCode=c.strSettelmentCode "
		    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ";
	    if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlTransRecords += "and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ";
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sqlTransRecords = sqlTransRecords + " and a.strPOSCode='" + selectedPOSCode + "' ";
	    }
	    sqlTransRecords = sqlTransRecords + "group by a.strClientCode,a.strBillNo,b.strSettlementCode "
		    + "order by a.strBillNo,b.strSettlementCode;";

	    ResultSet rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlTransRecords);
	    while (rsSales.next())
	    {
		//date+SettlementDesc
		if (mapSettlemetWiseAmt.containsKey(rsSales.getString(1) + "!" + "All".toUpperCase()))
		{
		    double oldSettlementAmt = mapSettlemetWiseAmt.get(rsSales.getString(1) + "!" + "All".toUpperCase());
		    mapSettlemetWiseAmt.put(rsSales.getString(1) + "!" + "All".toUpperCase(), oldSettlementAmt + rsSales.getDouble(3));
		}
		else
		{
		    mapSettlemetWiseAmt.put(rsSales.getString(1) + "!" + "All".toUpperCase(), rsSales.getDouble(3));
		}

		if (mapSettlemetWiseAmt.containsKey(rsSales.getString(1) + "!" + rsSales.getString(2).toUpperCase()))
		{
		    double oldSettlementAmt = mapSettlemetWiseAmt.get(rsSales.getString(1) + "!" + rsSales.getString(2).toUpperCase());
		    mapSettlemetWiseAmt.put(rsSales.getString(1) + "!" + rsSales.getString(2).toUpperCase(), oldSettlementAmt + rsSales.getDouble(3));
		}
		else
		{
		    mapSettlemetWiseAmt.put(rsSales.getString(1) + "!" + rsSales.getString(2).toUpperCase(), rsSales.getDouble(3));
		}
	    }
	    rsSales.close();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funFillSettlementWiseBillWiseAmtMapForQFile()
    {
	try
	{
	    //fill Q Data           
	    String sqlTransRecords = "select a.strBillNo,c.strSettelmentDesc,sum(b.dblSettlementAmt) "
		    + "from "
		    + "tblqbillhd a,tblqbillsettlementdtl b,tblsettelmenthd c "
		    + "where  "
		    + "a.strBillNo=b.strBillNo  "
		    + "and date(a.dteBillDate)=date(b.dteBillDate) "
		    + "and a.strClientCode=b.strClientCode   "
		    + "and b.strSettlementCode=c.strSettelmentCode "
		    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ";
	    if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlTransRecords += "and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ";
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sqlTransRecords = sqlTransRecords + " and a.strPOSCode='" + selectedPOSCode + "' ";
	    }
	    sqlTransRecords = sqlTransRecords + "group by a.strClientCode,a.strBillNo,b.strSettlementCode "
		    + "order by a.strBillNo,b.strSettlementCode;";

	    ResultSet rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlTransRecords);
	    while (rsSales.next())
	    {
		//date+SettlementDesc
		if (mapSettlemetWiseAmt.containsKey(rsSales.getString(1) + "!" + "All".toUpperCase()))
		{
		    double oldSettlementAmt = mapSettlemetWiseAmt.get(rsSales.getString(1) + "!" + "All".toUpperCase());
		    mapSettlemetWiseAmt.put(rsSales.getString(1) + "!" + "All".toUpperCase(), oldSettlementAmt + rsSales.getDouble(3));
		}
		else
		{
		    mapSettlemetWiseAmt.put(rsSales.getString(1) + "!" + "All".toUpperCase(), rsSales.getDouble(3));
		}

		if (mapSettlemetWiseAmt.containsKey(rsSales.getString(1) + "!" + rsSales.getString(2).toUpperCase()))
		{
		    double oldSettlementAmt = mapSettlemetWiseAmt.get(rsSales.getString(1) + "!" + rsSales.getString(2).toUpperCase());
		    mapSettlemetWiseAmt.put(rsSales.getString(1) + "!" + rsSales.getString(2).toUpperCase(), oldSettlementAmt + rsSales.getDouble(3));
		}
		else
		{
		    mapSettlemetWiseAmt.put(rsSales.getString(1) + "!" + rsSales.getString(2).toUpperCase(), rsSales.getDouble(3));
		}
	    }
	    rsSales.close();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funUpdateSettWiseBillWiseAmtForLive(String columnNameForBillDtl)
    {

	String sqlGroups = "select a.strBillNo,g.strGroupName," + columnNameForBillDtl + " "
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

	try
	{
	    ResultSet rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlGroups);
	    while (rsSales.next())
	    {
		for (int tblRow = 0; tblRow < tblBillWiseSalesSummary.getRowCount(); tblRow++)
		{
		    if (tblBillWiseSalesSummary.getValueAt(tblRow, 0).toString().equalsIgnoreCase(rsSales.getString(1)))//match billNo
		    {
			for (int tblCol = 2; tblCol < tblBillWiseSalesSummary.getColumnCount(); tblCol++)
			{
			    if (tblBillWiseSalesSummary.getColumnName(tblCol).toUpperCase().equals(rsSales.getString(2).toUpperCase()))//match groupName
			    {
				double billWiseSettWiseAmt = mapSettlemetWiseAmt.get(tblBillWiseSalesSummary.getValueAt(tblRow, 0) + "!" + cmbSettlementName.getSelectedItem().toString().toUpperCase());
				double billWiseTotalSettAmt = mapSettlemetWiseAmt.get(tblBillWiseSalesSummary.getValueAt(tblRow, 0) + "!" + "All".toUpperCase());
				double billWiseSettWisePer = 0.00;
				if (billWiseTotalSettAmt != 0)
				{
				    billWiseSettWisePer = (billWiseSettWiseAmt / billWiseTotalSettAmt) * 100;
				}

				double groupSubTotal = Double.parseDouble(tblBillWiseSalesSummary.getValueAt(tblRow, tblCol).toString());
				tblBillWiseSalesSummary.setValueAt(gDecimalFormat.format((billWiseSettWisePer / 100) * groupSubTotal), tblRow, tblCol);
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

    private void funUpdateSettWiseBillWiseAmtForQFile(String columnNameForBillDtl)
    {
	String sqlGroups = "select a.strBillNo,g.strGroupName," + columnNameForBillDtl + " "
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

	sqlGroups = sqlGroups + "GROUP BY a.strClientCode,a.strBillNo,g.strGroupCode,g.strGroupName ";

	try
	{
	    ResultSet rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlGroups);
	    while (rsSales.next())
	    {
		for (int tblRow = 0; tblRow < tblBillWiseSalesSummary.getRowCount(); tblRow++)
		{
		    if (tblBillWiseSalesSummary.getValueAt(tblRow, 0).toString().equalsIgnoreCase(rsSales.getString(1)))//match billNo
		    {
			for (int tblCol = 2; tblCol < tblBillWiseSalesSummary.getColumnCount(); tblCol++)
			{
			    if (tblBillWiseSalesSummary.getColumnName(tblCol).toUpperCase().equals(rsSales.getString(2).toUpperCase()))//match groupName
			    {
				double billWiseSettWiseAmt = mapSettlemetWiseAmt.get(tblBillWiseSalesSummary.getValueAt(tblRow, 0) + "!" + cmbSettlementName.getSelectedItem().toString().toUpperCase());
				double billWiseTotalSettAmt = mapSettlemetWiseAmt.get(tblBillWiseSalesSummary.getValueAt(tblRow, 0) + "!" + "All".toUpperCase());
				double billWiseSettWisePer = 0.00;
				if (billWiseTotalSettAmt != 0)
				{
				    billWiseSettWisePer = (billWiseSettWiseAmt / billWiseTotalSettAmt) * 100;
				}

				double groupSubTotal = Double.parseDouble(tblBillWiseSalesSummary.getValueAt(tblRow, tblCol).toString());
				tblBillWiseSalesSummary.setValueAt(gDecimalFormat.format((billWiseSettWisePer / 100) * groupSubTotal), tblRow, tblCol);
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

    private void funUpdateSettWiseBillWiseTaxAmtForLive()
    {
	String sqlTax = "select a.strBillNo,c.strTaxDesc,sum(b.dblTaxAmount) "
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

	try
	{
	    ResultSet rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlTax);
	    while (rsSales.next())
	    {
		for (int tblRow = 0; tblRow < tblBillWiseSalesSummary.getRowCount(); tblRow++)
		{
		    if (tblBillWiseSalesSummary.getValueAt(tblRow, 0).toString().equalsIgnoreCase(rsSales.getString(1)))//match billNo
		    {
			for (int tblCol = 2; tblCol < tblBillWiseSalesSummary.getColumnCount(); tblCol++)
			{
			    if (tblBillWiseSalesSummary.getColumnName(tblCol).toUpperCase().equals(rsSales.getString(2).toUpperCase()))//match tax
			    {
				double billWiseSettWiseAmt = mapSettlemetWiseAmt.get(tblBillWiseSalesSummary.getValueAt(tblRow, 0) + "!" + cmbSettlementName.getSelectedItem().toString().toUpperCase());
				double billWiseTotalSettAmt = mapSettlemetWiseAmt.get(tblBillWiseSalesSummary.getValueAt(tblRow, 0) + "!" + "All".toUpperCase());
				double billWiseSettWisePer = 0.00;
				if (billWiseTotalSettAmt > 0)
				{
				    billWiseSettWisePer = (billWiseSettWiseAmt / billWiseTotalSettAmt) * 100;
				}
				double taxAmt =0;
				if(tblBillWiseSalesSummary.getValueAt(tblRow, tblCol)!=null)
				{
				    taxAmt= Double.parseDouble(tblBillWiseSalesSummary.getValueAt(tblRow, tblCol).toString());
				}
				tblBillWiseSalesSummary.setValueAt(gDecimalFormat.format((billWiseSettWisePer / 100) * taxAmt), tblRow, tblCol);
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

    private void funUpdateSettWiseBillWiseTaxAmtForQFile()
    {
	String sqlTax = "select a.strBillNo,c.strTaxDesc,sum(b.dblTaxAmount) "
		+ "from "
		+ "tblqbillhd a,tblqbilltaxdtl b,tbltaxhd c  "
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

	try
	{
	    ResultSet rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlTax);
	    while (rsSales.next())
	    {
		for (int tblRow = 0; tblRow < tblBillWiseSalesSummary.getRowCount(); tblRow++)
		{
		    if (tblBillWiseSalesSummary.getValueAt(tblRow, 0).toString().equalsIgnoreCase(rsSales.getString(1)))//match billNo
		    {
			for (int tblCol = 2; tblCol < tblBillWiseSalesSummary.getColumnCount() - 1; tblCol++)
			{
			    if (tblBillWiseSalesSummary.getColumnName(tblCol).toUpperCase().equals(rsSales.getString(2).toUpperCase()))//match tax
			    {
				double billWiseSettWiseAmt = mapSettlemetWiseAmt.get(tblBillWiseSalesSummary.getValueAt(tblRow, 0) + "!" + cmbSettlementName.getSelectedItem().toString().toUpperCase());
				double billWiseTotalSettAmt = mapSettlemetWiseAmt.get(tblBillWiseSalesSummary.getValueAt(tblRow, 0) + "!" + "All".toUpperCase());
				double billWiseSettWisePer = 0.00;
				if (billWiseTotalSettAmt > 0)
				{
				    billWiseSettWisePer = (billWiseSettWiseAmt / billWiseTotalSettAmt) * 100;
				}

				double taxAmt = 0.00;
				if (tblBillWiseSalesSummary.getValueAt(tblRow, tblCol) != null)
				{
				    taxAmt = Double.parseDouble(tblBillWiseSalesSummary.getValueAt(tblRow, tblCol).toString());
				}
				tblBillWiseSalesSummary.setValueAt(gDecimalFormat.format((billWiseSettWisePer / 100) * taxAmt), tblRow, tblCol);
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

    private void funFillTableForBillRegister()
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

	    DecimalFormat decFormat = new DecimalFormat("0");
	    StringBuilder sbSqlBillWise = new StringBuilder();
	    StringBuilder sbSqlBillWiseQFile = new StringBuilder();

	    sbSqlBillWise.setLength(0);
	    sbSqlBillWise.append("select a.strBillNo,left(a.dteBillDate,10),left(right(a.dteDateCreated,8),5) as BillTime "
		    + " ,ifnull(b.strTableName,'') as TableName,f.strPOSName, ifnull(d.strSettelmentDesc,'') as payMode "
		    + " ,ifnull(a.dblSubTotal,0.00),IFNULL(a.dblDiscountPer,0), IFNULL(a.dblDiscountAmt,0.00),a.dblTaxAmt "
		    + " ,ifnull(c.dblSettlementAmt,0.00),a.strUserCreated "
		    + " ,a.strUserEdited,a.dteDateCreated,a.dteDateEdited,a.strClientCode,a.strWaiterNo "
		    + " ,a.strCustomerCode,a.dblDeliveryCharges,ifnull(c.strRemark,''),ifnull(e.strCustomerName ,'NA') "
		    + " ,a.dblTipAmount,'" + clsGlobalVarClass.gUserCode + "',a.strDiscountRemark,ifnull(h.strReasonName ,'NA')"
		    + ",a.intShiftCode,a.dblRoundOff,a.intBillSeriesPaxNo,ifnull(i.dblAdvDeposite,0),ifnull(k.strAdvOrderTypeName,'') "
		    + ",ifnull(l.strBillSeries,''),d.strSettelmentType "
		    + " from tblbillhd  a "
		    + " left outer join  tbltablemaster b on a.strTableNo=b.strTableNo "
		    + " left outer join tblposmaster f on a.strPOSCode=f.strPOSCode "
		    + " left outer join tblbillsettlementdtl c on a.strBillNo=c.strBillNo and a.strClientCode=c.strClientCode  and date(a.dteBillDate)=date(c.dteBillDate)  "
		    + " left outer join tblsettelmenthd d on c.strSettlementCode=d.strSettelmentCode "
		    + " left outer join tblcustomermaster e on a.strCustomerCode=e.strCustomerCode "
		    + " left outer join tblreasonmaster h on a.strReasonCode=h.strReasonCode "
		    + " left outer join tbladvancereceipthd i on a.strAdvBookingNo=i.strAdvBookingNo "
		    + " LEFT OUTER JOIN tbladvbookbillhd j ON a.strAdvBookingNo=j.strAdvBookingNo "
		    + " left outer join tbladvanceordertypemaster k on j.strOrderType=k.strAdvOrderTypeCode "
		    + " left outer join tblbillseriesbilldtl l on a.strBillNo=l.strHdBillNo "
		    + " where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		    + "  ");
	    if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sbSqlBillWise.append("and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ");
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sbSqlBillWise.append(" and a.strPOSCode='" + selectedPOSCode + "' ");
	    }
	    if (!cmbSettlementName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sbSqlBillWise.append(" and c.strSettlementCode='" + mapSettlementNameCode.get(cmbSettlementName.getSelectedItem().toString()) + "' ");
	    }
	    sbSqlBillWise.append("  ");
	    sbSqlBillWise.append(" order by date(a.dteBillDate),a.strBillNo  ");

	    sbSqlBillWiseQFile.setLength(0);
	    sbSqlBillWiseQFile.append("select a.strBillNo,left(a.dteBillDate,10),left(right(a.dteDateCreated,8),5) as BillTime "
		    + " ,ifnull(b.strTableName,'') as TableName,f.strPOSName, ifnull(d.strSettelmentDesc,'') as payMode "
		    + " ,ifnull(a.dblSubTotal,0.00),IFNULL(a.dblDiscountPer,0), IFNULL(a.dblDiscountAmt,0.00),a.dblTaxAmt "
		    + " ,ifnull(c.dblSettlementAmt,0.00),a.strUserCreated "
		    + " ,a.strUserEdited,a.dteDateCreated,a.dteDateEdited,a.strClientCode,a.strWaiterNo "
		    + " ,a.strCustomerCode,a.dblDeliveryCharges,ifnull(c.strRemark,''),ifnull(e.strCustomerName ,'NA') "
		    + " ,a.dblTipAmount,'" + clsGlobalVarClass.gUserCode + "',a.strDiscountRemark,ifnull(h.strReasonName ,'NA')"
		    + ",a.intShiftCode,a.dblRoundOff,a.intBillSeriesPaxNo,ifnull(i.dblAdvDeposite,0),ifnull(k.strAdvOrderTypeName,'') "
		    + ",ifnull(l.strBillSeries,''),d.strSettelmentType "
		    + " from tblqbillhd  a "
		    + " left outer join  tbltablemaster b on a.strTableNo=b.strTableNo "
		    + " left outer join tblposmaster f on a.strPOSCode=f.strPOSCode "
		    + " left outer join tblqbillsettlementdtl c on a.strBillNo=c.strBillNo and a.strClientCode=c.strClientCode  and date(a.dteBillDate)=date(c.dteBillDate)  "
		    + " left outer join tblsettelmenthd d on c.strSettlementCode=d.strSettelmentCode "
		    + " left outer join tblcustomermaster e on a.strCustomerCode=e.strCustomerCode "
		    + " left outer join tblreasonmaster h on a.strReasonCode=h.strReasonCode "
		    + " left outer join tblqadvancereceipthd i on a.strAdvBookingNo=i.strAdvBookingNo "
		    + " LEFT OUTER JOIN tblqadvbookbillhd j ON a.strAdvBookingNo=j.strAdvBookingNo "
		    + " left outer join tbladvanceordertypemaster k on j.strOrderType=k.strAdvOrderTypeCode "
		    + " left outer join tblbillseriesbilldtl l on a.strBillNo=l.strHdBillNo "
		    + " where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		    + "  ");
	    if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sbSqlBillWiseQFile.append("and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ");
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sbSqlBillWiseQFile.append(" and a.strPOSCode='" + selectedPOSCode + "' ");
	    }
	    if (!cmbSettlementName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sbSqlBillWiseQFile.append(" and c.strSettlementCode='" + mapSettlementNameCode.get(cmbSettlementName.getSelectedItem().toString()) + "' ");
	    }
	    sbSqlBillWiseQFile.append("   ");
	    sbSqlBillWiseQFile.append(" order by date(a.dteBillDate),a.strBillNo  ");

	    //System.out.println(sbSqlBillWise);
	    //System.out.println(sbSqlBillWiseQFile);
	    double totalDiscAmt = 0, totalSubTotal = 0, totalTaxAmt = 0, totalAdvAmt = 0, totalSettleAmt = 0, totalTipAmt = 0, totalRoundOffAmt = 0;
	    boolean flgRecords = false;
	    int totalPAX = 0;

	    Map<String, List<clsSalesFlashColumns>> hmBillWiseSales = new HashMap<String, List<clsSalesFlashColumns>>();
	    int seqNo = 1;
	    //for live data
	    ResultSet rsBillWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlBillWise.toString());
	    while (rsBillWiseSales.next())
	    {
		List<clsSalesFlashColumns> arrListBillWiseSales = new ArrayList<clsSalesFlashColumns>();
		flgRecords = true;
		String[] spDate = rsBillWiseSales.getString(2).split("-");
		String billDate = spDate[2] + "-" + spDate[1] + "-" + spDate[0];//billDate

		clsSalesFlashColumns objSalesFlashColumns = new clsSalesFlashColumns();
		objSalesFlashColumns.setStrField1(rsBillWiseSales.getString(1));
		objSalesFlashColumns.setStrField2(billDate);
		objSalesFlashColumns.setStrField3(rsBillWiseSales.getString(3));
		objSalesFlashColumns.setStrField4(rsBillWiseSales.getString(4));
		if (clsGlobalVarClass.gCMSIntegrationYN)
		{
		    objSalesFlashColumns.setStrField5(rsBillWiseSales.getString(18));//Member Code
		}
		else
		{
		    objSalesFlashColumns.setStrField5(rsBillWiseSales.getString(21));//Cust Name
		}
		objSalesFlashColumns.setStrField6(rsBillWiseSales.getString(5));
		objSalesFlashColumns.setStrField7(rsBillWiseSales.getString(6));
		objSalesFlashColumns.setStrField8(rsBillWiseSales.getString(19));
		objSalesFlashColumns.setStrField9(rsBillWiseSales.getString(7));
		objSalesFlashColumns.setStrField10(rsBillWiseSales.getString(8));
		objSalesFlashColumns.setStrField11(rsBillWiseSales.getString(9));
		objSalesFlashColumns.setStrField12(rsBillWiseSales.getString(10));
		objSalesFlashColumns.setStrField13(rsBillWiseSales.getString(11));
		objSalesFlashColumns.setStrField14(rsBillWiseSales.getString(20));

		objSalesFlashColumns.setStrField15(rsBillWiseSales.getString(22));
		objSalesFlashColumns.setStrField16(rsBillWiseSales.getString(24));
		objSalesFlashColumns.setStrField17(rsBillWiseSales.getString(25));
		objSalesFlashColumns.setStrField18(rsBillWiseSales.getString(26));//shift
		objSalesFlashColumns.setStrField19(rsBillWiseSales.getString(27));//roundOff
		objSalesFlashColumns.setStrField20(rsBillWiseSales.getString(28));//intBillSeriesPaxNo
		objSalesFlashColumns.setStrField21(rsBillWiseSales.getString(29));//dblAdvDeposite
		objSalesFlashColumns.setStrField22(rsBillWiseSales.getString(30));//strAdvOrderTypeName
		objSalesFlashColumns.setStrField23(rsBillWiseSales.getString(31));//bill series

		objSalesFlashColumns.setStrCustomerName(rsBillWiseSales.getString(21));
		objSalesFlashColumns.setStrSettlementType(rsBillWiseSales.getString(32));//settlement type

//                objSalesFlashColumns.setSeqNo(Integer.parseInt(billNo.split("-")[0]));
		objSalesFlashColumns.setSeqNo(seqNo++);

		if (null != hmBillWiseSales.get(rsBillWiseSales.getString(1) + "!" + billDate))
		{
		    arrListBillWiseSales = hmBillWiseSales.get(rsBillWiseSales.getString(1) + "!" + billDate);
		    objSalesFlashColumns.setStrField9("0");
		    objSalesFlashColumns.setStrField10("0");
		    objSalesFlashColumns.setStrField11("0");
		    objSalesFlashColumns.setStrField12("0");
		    objSalesFlashColumns.setStrField15("0");
		    objSalesFlashColumns.setStrField19("0");//roundoff
		    objSalesFlashColumns.setStrField20("0");//intBillSeriesPaxNo
		    objSalesFlashColumns.setStrField21("0");//dblAdvDeposite

		    objSalesFlashColumns.setStrField24("MultiSettle");//MultiSettle
		}
		arrListBillWiseSales.add(objSalesFlashColumns);
		hmBillWiseSales.put(rsBillWiseSales.getString(1) + "!" + billDate, arrListBillWiseSales);

		totalDiscAmt += Double.parseDouble(objSalesFlashColumns.getStrField11());
		totalSubTotal += Double.parseDouble(objSalesFlashColumns.getStrField9());
		totalTaxAmt += Double.parseDouble(objSalesFlashColumns.getStrField12());
		totalAdvAmt += Double.parseDouble(objSalesFlashColumns.getStrField21());
		totalSettleAmt += Double.parseDouble(objSalesFlashColumns.getStrField13());// Grand Total                
		totalTipAmt += Double.parseDouble(objSalesFlashColumns.getStrField15());// tip Amt  
		totalRoundOffAmt += Double.parseDouble(objSalesFlashColumns.getStrField19());// roundoff Amt  
		totalPAX += Integer.parseInt(objSalesFlashColumns.getStrField20());//intBillSeriesPaxNo

	    }
	    rsBillWiseSales.close();

	    //for qfile data
	    rsBillWiseSales = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlBillWiseQFile.toString());
	    while (rsBillWiseSales.next())
	    {
		List<clsSalesFlashColumns> arrListBillWiseSales = new ArrayList<clsSalesFlashColumns>();
		flgRecords = true;

//                String billNo1=rsBillWiseSales.getString(1);
//                String billNo=billNo1.substring(1, billNo1.length());
		String[] spDate = rsBillWiseSales.getString(2).split("-");
		String billDate = spDate[2] + "-" + spDate[1] + "-" + spDate[0];//billDate

		clsSalesFlashColumns objSalesFlashColumns = new clsSalesFlashColumns();
		objSalesFlashColumns.setStrField1(rsBillWiseSales.getString(1));
		objSalesFlashColumns.setStrField2(billDate);
		objSalesFlashColumns.setStrField3(rsBillWiseSales.getString(3));
		objSalesFlashColumns.setStrField4(rsBillWiseSales.getString(4));
		if (clsGlobalVarClass.gCMSIntegrationYN)
		{
		    objSalesFlashColumns.setStrField5(rsBillWiseSales.getString(18));//Member Code
		}
		else
		{
		    objSalesFlashColumns.setStrField5(rsBillWiseSales.getString(21));//Cust Name
		}
		objSalesFlashColumns.setStrField6(rsBillWiseSales.getString(5));
		objSalesFlashColumns.setStrField7(rsBillWiseSales.getString(6));
		objSalesFlashColumns.setStrField8(rsBillWiseSales.getString(19));
		objSalesFlashColumns.setStrField9(rsBillWiseSales.getString(7));
		objSalesFlashColumns.setStrField10(rsBillWiseSales.getString(8));
		objSalesFlashColumns.setStrField11(rsBillWiseSales.getString(9));
		objSalesFlashColumns.setStrField12(rsBillWiseSales.getString(10));
		objSalesFlashColumns.setStrField13(rsBillWiseSales.getString(11));
		objSalesFlashColumns.setStrField14(rsBillWiseSales.getString(20));
		objSalesFlashColumns.setStrField15(rsBillWiseSales.getString(22));
		objSalesFlashColumns.setStrField16(rsBillWiseSales.getString(24));
		objSalesFlashColumns.setStrField17(rsBillWiseSales.getString(25));
		objSalesFlashColumns.setStrField18(rsBillWiseSales.getString(26));//shift
		objSalesFlashColumns.setStrField19(rsBillWiseSales.getString(27));//roundOff
		objSalesFlashColumns.setStrField20(rsBillWiseSales.getString(28));//intBillSeriesPaxNo
		objSalesFlashColumns.setStrField21(rsBillWiseSales.getString(29));//dblAdvDeposite
		objSalesFlashColumns.setStrField22(rsBillWiseSales.getString(30));//strAdvOrderTypeName
		objSalesFlashColumns.setStrField23(rsBillWiseSales.getString(31));//bill series

		objSalesFlashColumns.setStrCustomerName(rsBillWiseSales.getString(21));//customerName
		objSalesFlashColumns.setStrSettlementType(rsBillWiseSales.getString(32));//settlement type

//                objSalesFlashColumns.setSeqNo(Integer.parseInt(billNo.split("-")[0]));
		objSalesFlashColumns.setSeqNo(seqNo++);

		if (null != hmBillWiseSales.get(rsBillWiseSales.getString(1) + "!" + billDate))
		{
		    arrListBillWiseSales = hmBillWiseSales.get(rsBillWiseSales.getString(1) + "!" + billDate);
		    objSalesFlashColumns.setStrField9("0");
		    objSalesFlashColumns.setStrField10("0");
		    objSalesFlashColumns.setStrField11("0");
		    objSalesFlashColumns.setStrField12("0");
		    objSalesFlashColumns.setStrField15("0");
		    objSalesFlashColumns.setStrField19("0");//roundoff
		    objSalesFlashColumns.setStrField20("0");//intBillSeriesPaxNo
		    objSalesFlashColumns.setStrField21("0");//dblAdvDeposite

		    objSalesFlashColumns.setStrField24("MultiSettle");//MultiSettle
		}
		arrListBillWiseSales.add(objSalesFlashColumns);
		hmBillWiseSales.put(rsBillWiseSales.getString(1) + "!" + billDate, arrListBillWiseSales);

		totalDiscAmt += Double.parseDouble(objSalesFlashColumns.getStrField11());
		totalSubTotal += Double.parseDouble(objSalesFlashColumns.getStrField9());
		totalTaxAmt += Double.parseDouble(objSalesFlashColumns.getStrField12());
		totalAdvAmt += Double.parseDouble(objSalesFlashColumns.getStrField21());
		totalSettleAmt += Double.parseDouble(objSalesFlashColumns.getStrField13());// Grand Total 
		totalTipAmt += Double.parseDouble(objSalesFlashColumns.getStrField15());// tip Amt  
		totalRoundOffAmt += Double.parseDouble(objSalesFlashColumns.getStrField19());// roundoff Amt  
		totalPAX += Integer.parseInt(objSalesFlashColumns.getStrField20());//intBillSeriesPaxNo
	    }
	    rsBillWiseSales.close();

	    dm.setRowCount(0);
	    totalDm.setRowCount(0);

	    dm.addColumn("Tbl No");
	    dm.addColumn("Bill No");
	    dm.addColumn("Aomunt");
	    dm.addColumn("Disc");

	    totalDm.addColumn("");
	    totalDm.addColumn("");
	    totalDm.addColumn("");
	    totalDm.addColumn("");

	    int cntArrLen = 0;

	    cntArrLen = cntArrLen + 4;

	    //fill TAX
	    mapAllTaxes = new HashMap<>();
	    String taxCalType = "";
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

	    for (Map.Entry<String, String> taxEntry : mapAllTaxes.entrySet())
	    {

		String taxCode = taxEntry.getKey();
		String taxName = taxEntry.getValue();

		dm.addColumn(taxName.toUpperCase());
		totalDm.addColumn("");

		cntArrLen++;

	    }
	    dm.addColumn("Tip");
	    dm.addColumn("Adv.");
	    dm.addColumn("Total");
	    dm.addColumn("Settle");

	    totalDm.addColumn("");
	    totalDm.addColumn("");
	    totalDm.addColumn("");
	    totalDm.addColumn("");

	    cntArrLen = cntArrLen + 4;

	    for (String key : hmBillWiseSales.keySet())
	    {
		List<clsSalesFlashColumns> listOfBills = hmBillWiseSales.get(key);

		for (clsSalesFlashColumns objSalesFlashColumns : listOfBills)
		{
		    Map<String, clsTaxCalculationDtls> mapOfTaxes = new HashMap<>();
		    for (Map.Entry<String, String> taxEntry : mapAllTaxes.entrySet())
		    {
			String taxCode = taxEntry.getKey();
			String taxName = taxEntry.getValue();

			clsTaxCalculationDtls objTax = new clsTaxCalculationDtls();
			objTax.setTaxCode(taxCode);
			objTax.setTaxName(taxName);
			objTax.setTaxAmount(0.00);

			mapOfTaxes.put(taxName, objTax);
		    }

		    objSalesFlashColumns.setMapOfTaxes(mapOfTaxes);
		}
	    }

	    String sqlTax = "select a.strBillNo,c.strTaxDesc,b.dblTaxAmount,date(a.dteBillDate) "
		    + "from "
		    + "tblqbillhd a,tblqbilltaxdtl b,tbltaxhd c,tblqbillsettlementdtl d,tblsettelmenthd e "
		    + "where a.strBillNo=b.strBillNo "
		    + "and b.strTaxCode=c.strTaxCode "
		    + "and a.strBillNo=d.strBillNo "
		    + "and d.strSettlementCode=e.strSettelmentCode "
		    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ";
	    if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlTax += "and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ";
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sqlTax = sqlTax + " and a.strPOSCode='" + selectedPOSCode + "' ";
	    }
	    if (!cmbSettlementName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlTax = sqlTax + " and e.strSettelmentCode='" + mapSettlementNameCode.get(cmbSettlementName.getSelectedItem().toString()) + "' ";
	    }
	    sqlTax = sqlTax + "group by a.strBillNo,b.strTaxCode "
		    + "order by a.strBillNo,b.strTaxCode;  ";
	    ResultSet rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlTax);
	    while (rsSales.next())
	    {
		String[] spDate = rsSales.getString(4).split("-");
		String billNo = rsSales.getString(1);
		String billDate = spDate[2] + "-" + spDate[1] + "-" + spDate[0];//billDate
		String billKey = billNo + "!" + billDate;

		List<clsSalesFlashColumns> listOfBills = hmBillWiseSales.get(billKey);
		clsSalesFlashColumns objSalesFlashColumns = listOfBills.get(0);
		Map<String, clsTaxCalculationDtls> mapOfTaxesLocal = objSalesFlashColumns.getMapOfTaxes();
		if (mapOfTaxesLocal.containsKey(rsSales.getString(2)))
		{
		    clsTaxCalculationDtls objTax = mapOfTaxesLocal.get(rsSales.getString(2));
		    objTax.setTaxAmount(rsSales.getDouble(3));
		}

	    }
	    rsSales.close();

	    sqlTax = "select a.strBillNo,c.strTaxDesc,b.dblTaxAmount,date(a.dteBillDate) "
		    + "from "
		    + "tblbillhd a,tblbilltaxdtl b,tbltaxhd c,tblbillsettlementdtl d,tblsettelmenthd e "
		    + "where a.strBillNo=b.strBillNo "
		    + "and b.strTaxCode=c.strTaxCode "
		    + "and a.strBillNo=d.strBillNo "
		    + "and d.strSettlementCode=e.strSettelmentCode "
		    + "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ";
	    if (!cmbOperationType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlTax += "and a.strOperationType='" + cmbOperationType.getSelectedItem().toString() + "' ";
	    }
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sqlTax = sqlTax + " and a.strPOSCode='" + selectedPOSCode + "' ";
	    }
	    if (!cmbSettlementName.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlTax = sqlTax + " and e.strSettelmentCode='" + mapSettlementNameCode.get(cmbSettlementName.getSelectedItem().toString()) + "' ";
	    }
	    sqlTax = sqlTax + "group by a.strBillNo,b.strTaxCode "
		    + "order by a.strBillNo,b.strTaxCode;  ";
	    rsSales = clsGlobalVarClass.dbMysql.executeResultSet(sqlTax);
	    while (rsSales.next())
	    {
		String[] spDate = rsSales.getString(4).split("-");
		String billNo = rsSales.getString(1);
		String billDate = spDate[2] + "-" + spDate[1] + "-" + spDate[0];//billDate
		String billKey = billNo + "!" + billDate;

		List<clsSalesFlashColumns> listOfBills = hmBillWiseSales.get(billKey);
		clsSalesFlashColumns objSalesFlashColumns = listOfBills.get(0);
		Map<String, clsTaxCalculationDtls> mapOfTaxesLocal = objSalesFlashColumns.getMapOfTaxes();
		if (mapOfTaxesLocal.containsKey(rsSales.getString(2)))
		{
		    clsTaxCalculationDtls objTax = mapOfTaxesLocal.get(rsSales.getString(2));
		    objTax.setTaxAmount(rsSales.getDouble(3));
		}

	    }
	    rsSales.close();

	    final String BILLSERIESSORTING = "FL";
	    Comparator<clsSalesFlashColumns> BILLSERIES = new Comparator<clsSalesFlashColumns>()
	    {
		// This is where the sorting happens.
		public int compare(clsSalesFlashColumns o1, clsSalesFlashColumns o2)
		{
		    return BILLSERIESSORTING.indexOf(o1.getStrField23()) - BILLSERIESSORTING.indexOf(o2.getStrField23());
		}
	    };

	    Comparator<clsSalesFlashColumns> BILLNO = new Comparator<clsSalesFlashColumns>()
	    {
		// This is where the sorting happens.
		public int compare(clsSalesFlashColumns o1, clsSalesFlashColumns o2)
		{
		    return o1.getStrField1().compareTo(o2.getStrField1());
		}
	    };

	    arrTempListBillWiseSales = new ArrayList<clsSalesFlashColumns>();
	    for (List<clsSalesFlashColumns> listOfFlashColumns : hmBillWiseSales.values())
	    {
		arrTempListBillWiseSales.addAll(listOfFlashColumns);
	    }

	    //sort arrTempListBillWiseSales 
	    if (clsGlobalVarClass.gEnableBillSeries)
	    {
		Collections.sort(arrTempListBillWiseSales, new clsSalesFlashComparator(BILLSERIES, BILLNO));
	    }

	    Vector billsRow = new Vector();
	    billsRow.add("Bills");
	    billsRow.add("");
	    billsRow.add("");
	    billsRow.add("");
	    for (String tax : mapAllTaxes.values())
	    {
		billsRow.add("");
	    }
	    billsRow.add("");
	    billsRow.add("");
	    billsRow.add("");
	    billsRow.add("");

	    dm.addRow(billsRow);

	    Vector foodBills = new Vector();
	    foodBills.add("Food Bills");
	    foodBills.add("");
	    foodBills.add("");
	    foodBills.add("");
	    for (String tax : mapAllTaxes.values())
	    {
		foodBills.add("");
	    }
	    foodBills.add("");
	    foodBills.add("");
	    foodBills.add("");
	    foodBills.add("");

	    dm.addRow(foodBills);

	    boolean liquorBillsPrint = true;
	    for (clsSalesFlashColumns objSalesFlashColumns : arrTempListBillWiseSales)
	    {

		if (objSalesFlashColumns.getStrField24() != null && objSalesFlashColumns.getStrField24().equalsIgnoreCase("MultiSettle"))
		{
		    Vector multiSettleRow = new Vector();
		    multiSettleRow.add("Part Settlement");
		    multiSettleRow.add("");
		    multiSettleRow.add("0.00");
		    multiSettleRow.add("0.00");
		    for (String tax : mapAllTaxes.values())
		    {
			multiSettleRow.add("0.00");
		    }
		    multiSettleRow.add("0.00");
		    multiSettleRow.add("0.00");
		    multiSettleRow.add(gDecimalFormat.format(Double.parseDouble(objSalesFlashColumns.getStrField13())));
		    multiSettleRow.add(objSalesFlashColumns.getStrField7());

		    dm.addRow(multiSettleRow);

		    continue;
		}

		if (objSalesFlashColumns.getStrField23().equalsIgnoreCase("L") && liquorBillsPrint)
		{
		    Vector liquorBillLabelRow = new Vector();
		    liquorBillLabelRow.add("Liquor Bills");
		    liquorBillLabelRow.add("");
		    liquorBillLabelRow.add("");
		    liquorBillLabelRow.add("");
		    for (String tax : mapAllTaxes.values())
		    {
			liquorBillLabelRow.add("");
		    }
		    liquorBillLabelRow.add("");
		    liquorBillLabelRow.add("");
		    liquorBillLabelRow.add("");
		    liquorBillLabelRow.add("");

		    dm.addRow(liquorBillLabelRow);

		    liquorBillsPrint = false;
		}

		Vector row = new Vector();
		row.add(objSalesFlashColumns.getStrField4());
		row.add(objSalesFlashColumns.getStrField1());
		row.add(gDecimalFormat.format(Double.parseDouble(objSalesFlashColumns.getStrField9())));
		row.add(gDecimalFormat.format(Double.parseDouble(objSalesFlashColumns.getStrField11())));

		Map<String, clsTaxCalculationDtls> mapOfTaxesLocal = objSalesFlashColumns.getMapOfTaxes();
		for (clsTaxCalculationDtls objTaxCalculationDtls : mapOfTaxesLocal.values())
		{
		    row.add(objTaxCalculationDtls.getTaxAmount());
		}

		row.add(gDecimalFormat.format(Double.parseDouble(objSalesFlashColumns.getStrField15())));
		row.add(gDecimalFormat.format(Double.parseDouble(objSalesFlashColumns.getStrField21())));
		row.add(gDecimalFormat.format(Double.parseDouble(objSalesFlashColumns.getStrField13())));
		row.add(objSalesFlashColumns.getStrField7());

		dm.addRow(row);

		if (Double.parseDouble(objSalesFlashColumns.getStrField11()) > 0)
		{
		    Vector discountRemark = new Vector();
		    discountRemark.add(objSalesFlashColumns.getStrField16());
		    discountRemark.add("");
		    discountRemark.add("");
		    discountRemark.add("");
		    for (String tax : mapAllTaxes.values())
		    {
			discountRemark.add("");
		    }
		    discountRemark.add("");
		    discountRemark.add("");
		    discountRemark.add("");
		    discountRemark.add("");

		    dm.addRow(discountRemark);
		}
	    }

//	    Vector totalRow = new Vector();
//	    totalRow.add("Total");
//	    totalRow.add("");
//	    totalRow.add(0.00);
//	    totalRow.add(0.00);
//	    for (Map.Entry<String, String> taxEntry : mapAllTaxes.entrySet())
//	    {
//		totalRow.add(0.00);
//	    }
//	    totalRow.add(0.00);
//	    totalRow.add(0.00);
//	    totalRow.add(0.00);
//	    totalRow.add("");
//
//	    totalDm.addRow(totalRow);
	    int detailRowCount = dm.getRowCount();

	    Vector blankLine = new Vector();
	    blankLine.add("");
	    blankLine.add("");
	    blankLine.add("");
	    blankLine.add("");
	    for (Map.Entry<String, String> taxEntry : mapAllTaxes.entrySet())
	    {
		blankLine.add("");
	    }
	    blankLine.add("");
	    blankLine.add("");
	    blankLine.add("");
	    blankLine.add("");

	    dm.addRow(blankLine);

	    Vector totalRow = new Vector();
	    totalRow.add("Total");
	    totalRow.add("");
	    totalRow.add(0.00);
	    totalRow.add(0.00);
	    for (Map.Entry<String, String> taxEntry : mapAllTaxes.entrySet())
	    {
		totalRow.add(0.00);
	    }
	    totalRow.add(0.00);
	    totalRow.add(0.00);
	    totalRow.add(0.00);
	    totalRow.add("");

	    dm.addRow(totalRow);
	    int totalRowNo = dm.getRowCount() - 1;

	    DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
	    rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
	    tblTotal.setRowHeight(30);
	    tblTotal.setModel(totalDm);
	    tblTotal.setAutoscrolls(true);
	    tblTotal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    tblBillWiseSalesSummary.setRowHeight(25);
	    tblBillWiseSalesSummary.setModel(dm);
	    tblBillWiseSalesSummary.setAutoscrolls(true);
	    tblBillWiseSalesSummary.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

	    // set total
	    for (int col = 2; col < tblBillWiseSalesSummary.getColumnCount() - 1; col++)
	    {
		double total = 0.00;
		for (int row = 0; row < detailRowCount; row++)
		{
		    if (tblBillWiseSalesSummary.getValueAt(row, col) == null || !tblBillWiseSalesSummary.getValueAt(row, col).toString().isEmpty())
		    {
			total += Double.parseDouble(tblBillWiseSalesSummary.getValueAt(row, col).toString());
		    }
		}
		tblBillWiseSalesSummary.setValueAt(gDecimalFormat.format(total - totalRoundOffAmt), totalRowNo, col);
	    }

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    public Map<String, String> getMapAllTaxes()
    {
	return mapAllTaxes;
    }

    public void setMapAllTaxes(Map<String, String> mapAllTaxes)
    {
	this.mapAllTaxes = mapAllTaxes;
    }

    public ArrayList<clsSalesFlashColumns> getArrTempListBillWiseSales()
    {
	return arrTempListBillWiseSales;
    }

    public void setArrTempListBillWiseSales(ArrayList<clsSalesFlashColumns> arrTempListBillWiseSales)
    {
	this.arrTempListBillWiseSales = arrTempListBillWiseSales;
    }

    public JTable getTblBillWiseSalesSummary()
    {
	return tblBillWiseSalesSummary;
    }

    public void setTblBillWiseSalesSummary(JTable tblBillWiseSalesSummary)
    {
	this.tblBillWiseSalesSummary = tblBillWiseSalesSummary;
    }

}
