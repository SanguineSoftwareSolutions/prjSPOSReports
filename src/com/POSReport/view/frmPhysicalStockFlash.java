package com.POSReport.view;

import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsPosConfigFile;
import com.POSGlobal.controller.clsUtility;
import com.POSGlobal.view.frmOkPopUp;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;

import java.io.FileReader;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Comparator;
import java.util.Date;
import javax.swing.JLabel;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class frmPhysicalStockFlash extends javax.swing.JFrame
{

    private String selectQuery;
    private String fromDate;
    private String toDate;
    DefaultTableModel dm, totalDm;

    private java.util.Vector vSalesReportExcelColLength;
    private String selectedPOSCode;
    private clsUtility objUtility;
    private DecimalFormat gDecimalFormat = clsGlobalVarClass.funGetGlobalDecimalFormatter();

    public frmPhysicalStockFlash()
    {
	try
	{
	    initComponents();
	    objUtility = new clsUtility();

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
	funFillPOSComboBox();
	dteFromDate.setDate(objUtility.funGetDateToSetCalenderDate());
	dteToDate.setDate(objUtility.funGetDateToSetCalenderDate());

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

	    }
	    else
	    {
		int lastIndexOf = selectedPOS.lastIndexOf(" ");

		selectedPOSCode = selectedPOS.substring(lastIndexOf + 1);
	    }

	    funFillTable();
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
	    dm.addColumn("");
	    dm.addColumn("DATE");
	    dm.addColumn("Computer Stock");
	    dm.addColumn("Physical Stock");
	    dm.addColumn("Variance");

	    String sqlPhyStk = "select a.strPSPCode,DATE_FORMAT(a.dteDateCreated,'%d-%m-%Y') as dtePhyStkDate,"
		    + " sum(b.dblCompStk),sum(b.dblPhyStk),sum(b.dblVariance),sum(b.dblVairanceAmt)"
		    + " from  tblpsphd a ,tblpspdtl b where a.strPSPCode=b.strPSPCode  "
		    + " and date(a.dteDateCreated) between '" + fromDate + "' and '" + toDate + "' ";
	    if (!selectedPOSCode.equalsIgnoreCase("All"))
	    {
		sqlPhyStk = sqlPhyStk + " and a.strPOSCode='" + selectedPOSCode + "' ";
	    }
	    sqlPhyStk = sqlPhyStk + " group by a.strPSPCode  order by a.strPSPCode";
	    ResultSet rsPhyStk = clsGlobalVarClass.dbMysql.executeResultSet(sqlPhyStk);
	    while (rsPhyStk.next())
	    {
		Object[] ob =
		{
		    rsPhyStk.getString(1), rsPhyStk.getString(2), gDecimalFormat.format(rsPhyStk.getString(3)), gDecimalFormat.format(rsPhyStk.getString(4)), gDecimalFormat.format(rsPhyStk.getString(5))
		};
		dm.addRow(ob);

	    }
	    rsPhyStk.close();

	    DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
	    rightRenderer.setHorizontalAlignment(JLabel.RIGHT);

	    tblPhyStkVarianceReport.setRowHeight(25);
	    tblPhyStkVarianceReport.setModel(dm);
	    tblPhyStkVarianceReport.setAutoscrolls(true);
	    tblPhyStkVarianceReport.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    tblPhyStkVarianceReport.setShowHorizontalLines(true);

	    tblPhyStkVarianceReport.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
	    tblPhyStkVarianceReport.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
	    tblPhyStkVarianceReport.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

	    tblPhyStkVarianceReport.getColumnModel().getColumn(0).setPreferredWidth(0);
	    tblPhyStkVarianceReport.getColumnModel().getColumn(1).setPreferredWidth(220);
	    tblPhyStkVarianceReport.getColumnModel().getColumn(2).setPreferredWidth(210);
	    tblPhyStkVarianceReport.getColumnModel().getColumn(3).setPreferredWidth(210);
	    tblPhyStkVarianceReport.getColumnModel().getColumn(4).setPreferredWidth(210);

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    public void funPhysicalStockReport(String physicalStockCode, String phyStkDate) throws Exception
    {
	int count = 0;
	String posName = "";

	double grandCompStkTotal = 0.00, quantityTotal = 0.00, grandPhyStkTotal = 0.00;
	funCreateTempFolder();

	String filePath = System.getProperty("user.dir");
	File file = new File(filePath + File.separator + "Temp" + File.separator + "Temp_PhysicalStockReport.txt");
	PrintWriter pw = new PrintWriter(file);
	funPrintBlankLines(clsGlobalVarClass.gClientName, pw);
	funPrintBlankLines(clsGlobalVarClass.gClientAddress1, pw);
	if (clsGlobalVarClass.gClientAddress2.trim().length() > 0)
	{
	    funPrintBlankLines(clsGlobalVarClass.gClientAddress2, pw);
	}
	if (clsGlobalVarClass.gClientAddress3.trim().length() > 0)
	{
	    funPrintBlankLines(clsGlobalVarClass.gClientAddress3, pw);
	}
	pw.println();
	funPrintBlankLines("Physical Stock Slip", pw);

	String sqlPos = " select b.strPosName from tblpsphd a ,tblposmaster b "
		+ " where a.strPSPCode='" + physicalStockCode + "' and a.strPOSCode=b.strPosCode ";

	ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(sqlPos);
	if (rs.next())
	{
	    posName = rs.getString(1);
	}
	pw.println();
	pw.println();
	pw.println("POS :" + posName);
	pw.println("Date:" + phyStkDate);
	pw.println("Physical Stock No:" + physicalStockCode);

	pw.println("---------------------------------------");

	pw.println();
	pw.println("Item Name");
	pw.println("    CompStock       PhyStock      Variance");
	pw.println("---------------------------------------");

	StringBuilder sqlPhysicalStockData = new StringBuilder();

	sqlPhysicalStockData.append(" select a.strPSPCode,b.strItemName,a.strItemCode,a.dblCompStk,a.dblPhyStk,"
		+ " a.dblVariance,a.dblVairanceAmt "
		+ " from  tblPSPdtl a, tblItemMaster b "
		+ " where a.strPSPCode='" + physicalStockCode + "' and a.strItemCode=b.strItemCode ");

	ResultSet rsPhysicalStockData = clsGlobalVarClass.dbMysql.executeResultSet(sqlPhysicalStockData.toString());
	while (rsPhysicalStockData.next())
	{
	    count++;

	    pw.println(rsPhysicalStockData.getString(2));
	    funPrintTextWithAlignment("right", rsPhysicalStockData.getString(4), 12, pw); // CompStk
	    funPrintTextWithAlignment("right", rsPhysicalStockData.getString(5), 12, pw); // PhyStk
	    funPrintTextWithAlignment("right", rsPhysicalStockData.getString(6), 13, pw); // Variance
	    pw.println();
	    pw.println();

	    grandCompStkTotal += Double.parseDouble(rsPhysicalStockData.getString(4));
	    grandPhyStkTotal += Double.parseDouble(rsPhysicalStockData.getString(5));
	    quantityTotal += Double.parseDouble(rsPhysicalStockData.getString(6));
	}
	rsPhysicalStockData.close();
	pw.println();
	pw.println("---------------------------------------");
	pw.println("GRAND TOTAL");
	pw.println();

	funPrintTextWithAlignment("right", String.valueOf(Math.rint(grandCompStkTotal)), 12, pw);
	funPrintTextWithAlignment("right", String.valueOf(Math.rint(grandPhyStkTotal)), 12, pw);
	funPrintTextWithAlignment("right", String.valueOf(Math.rint(quantityTotal)), 12, pw);

	pw.println();
	pw.println("---------------------------------------");
	pw.println();
	pw.println();
	pw.println();
	pw.println();
	pw.println();
	if ("linux".equalsIgnoreCase(clsPosConfigFile.gPrintOS))
	{
	    pw.println("V");//Linux
	}
	else if ("windows".equalsIgnoreCase(clsPosConfigFile.gPrintOS))
	{
	    if ("Inbuild".equalsIgnoreCase(clsPosConfigFile.gPrinterType))
	    {
		pw.println("V");
	    }
	    else
	    {
		pw.println("m");//windows
	    }
	}

	pw.flush();
	pw.close();

	if (count > 0)
	{
	    funShowTextFile(file, "Physical Stock Report");
	}
    }

    /**
     * create Temp Folder for text report
     */
    private void funCreateTempFolder()
    {
	String filePath = System.getProperty("user.dir");
	File TextKOT = new File(filePath + File.separator + "Temp");
	if (!TextKOT.exists())
	{
	    TextKOT.mkdirs();
	}
    }

    /**
     * Print blank Lines
     *
     * @param textToPrint
     * @param pw
     * @return
     */
    private int funPrintBlankLines(String textToPrint, PrintWriter pw)
    {
	pw.println();
	int len = 40 - textToPrint.length();
	len = len / 2;
	for (int cnt = 0; cnt < len; cnt++)
	{
	    pw.print(" ");
	}
	pw.print(textToPrint);
	return len;
    }

    /**
     * Print Text With Alignment
     *
     * @param align
     * @param textToPrint
     * @param totalLength
     * @param pw
     * @return
     */
    private int funPrintTextWithAlignment(String align, String textToPrint, int totalLength, PrintWriter pw)
    {
	int len = totalLength - textToPrint.length();
	for (int cnt = 0; cnt < len; cnt++)
	{
	    pw.print(" ");
	}

	DecimalFormat decFormat = new DecimalFormat("######.00");
	pw.print(decFormat.format(Double.parseDouble(textToPrint)));
	return 1;
    }

    /**
     * Show text file Method
     *
     * @param file
     * @param reportName
     */
    private void funShowTextFile(File file, String reportName)
    {
	try
	{
	    String data = "";
	    FileReader fread = new FileReader(file);
	    BufferedReader brText = new BufferedReader(fread);

	    String line = "";
	    while ((line = brText.readLine()) != null)
	    {
		data = data + line + "\n";
	    }
	    new com.POSGlobal.view.frmShowTextFile(data, reportName, file, "").setVisible(true);
	    fread.close();
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
        tblPhyStkVarianceReport = new javax.swing.JTable();
        btnExecute = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();

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

        tblPhyStkVarianceReport.setFont(new java.awt.Font("Microsoft Sans Serif", 0, 11)); // NOI18N
        tblPhyStkVarianceReport.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tblPhyStkVarianceReport.setRowHeight(25);
        tblPhyStkVarianceReport.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblPhyStkVarianceReportMouseClicked(evt);
            }
        });
        pnlDayEnd.setViewportView(tblPhyStkVarianceReport);

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

        javax.swing.GroupLayout pnlMainLayout = new javax.swing.GroupLayout(pnlMain);
        pnlMain.setLayout(pnlMainLayout);
        pnlMainLayout.setHorizontalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlDayEnd, javax.swing.GroupLayout.DEFAULT_SIZE, 869, Short.MAX_VALUE)
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addComponent(lblPosCode)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbPosCode, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addComponent(btnExecute, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(33, 33, 33)
                        .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblFromDate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 51, Short.MAX_VALUE)
                .addComponent(dteFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(38, 38, 38)
                .addComponent(lblToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dteToDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(119, 119, 119))
        );
        pnlMainLayout.setVerticalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblPosCode, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cmbPosCode, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(dteFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dteToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnExecute, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlDayEnd, javax.swing.GroupLayout.PREFERRED_SIZE, 506, javax.swing.GroupLayout.PREFERRED_SIZE))
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
	    btnExecute.setEnabled(true);
	    funExecuteButtonClicked();
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
	clsGlobalVarClass.hmActiveForms.remove("PhysicalStockFlash");
    }//GEN-LAST:event_btnCloseMouseClicked

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
	dispose();
	clsGlobalVarClass.hmActiveForms.remove("PhysicalStockFlash");
    }//GEN-LAST:event_btnCloseActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
	// TODO add your handling code here:
	dispose();
	clsGlobalVarClass.hmActiveForms.remove("PhysicalStockFlash");
    }//GEN-LAST:event_formWindowClosed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
	// TODO add your handling code here:
	dispose();
	clsGlobalVarClass.hmActiveForms.remove("PhysicalStockFlash");
    }//GEN-LAST:event_formWindowClosing

    private void tblPhyStkVarianceReportMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblPhyStkVarianceReportMouseClicked
	// TODO add your handling code here:
	int row = tblPhyStkVarianceReport.getSelectedRow();
	String phyStkNo = tblPhyStkVarianceReport.getValueAt(row, 0).toString();
	String phyStkDate = tblPhyStkVarianceReport.getValueAt(row, 1).toString();
	try
	{
	    if (evt.getClickCount() == 2)
	    {
		funPhysicalStockReport(phyStkNo, phyStkDate);

	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }//GEN-LAST:event_tblPhyStkVarianceReportMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnExecute;
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
    private javax.swing.JTable tblPhyStkVarianceReport;
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

}
