/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.POSReport.view;

import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsManagerReportBean;
import com.POSGlobal.controller.clsPosConfigFile;
import com.POSGlobal.controller.clsSendMail;
import com.POSGlobal.controller.clsUtility;
import com.POSGlobal.controller.clsUtility2;
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
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JPanel;
import javax.swing.Timer;

public class frmSettlementWiseGroupWiseBreakup extends javax.swing.JFrame
{

    String fromDate, toDate, insertQuery, updateQuery, imagePath;
    private clsUtility objUtility;
    private Map<String, String> hmPOS;
    private StringBuilder sb = new StringBuilder();
    private clsUtility2 objUtility2;
    private final DecimalFormat decFormatter;
    private double totalDiscAmt;
    private double totalSettleAmt;
    private double totalRoundOffAmt;
    private double totalTaxAmt;
    private double totalTipAmt;
    private int totalBills;

    /**
     * this Function is used for Component initialization
     */
    public frmSettlementWiseGroupWiseBreakup()
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

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	objUtility = new clsUtility();
	objUtility2 = new clsUtility2();
	decFormatter = new DecimalFormat("0.00");

	imagePath = System.getProperty("user.dir");
	imagePath = imagePath + File.separator + "ReportImage";
	fillComboBox();
	setFormToInDateChosser();

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
     * this function is used Filling POS Code ComboBoxs
     */
    public void fillComboBox()
    {
	try
	{
	    if (clsGlobalVarClass.gShowOnlyLoginPOSReports)
	    {
		hmPOS = new HashMap<String, String>();
		cmbPosCode.addItem(clsGlobalVarClass.gPOSName);
		hmPOS.put(clsGlobalVarClass.gPOSName, clsGlobalVarClass.gPOSCode);

	    }
	    else
	    {
		hmPOS = new HashMap<String, String>();
		cmbPosCode.addItem("All");
		hmPOS.put("All", "All");
		sb.setLength(0);
		sb.append("select strPosName,strPosCode from tblposmaster");
		ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
		while (rs.next())
		{
		    cmbPosCode.addItem(rs.getString(1));
		    hmPOS.put(rs.getString(1), rs.getString(2));
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
     * this Function is used for Set Form To Date chooser
     */
    public void setFormToInDateChosser()
    {
	dteFromDate.setDate(objUtility.funGetDateToSetCalenderDate());
	dteToDate.setDate(objUtility.funGetDateToSetCalenderDate());
    }

    //Function to get calender date in 'YYYY-MM-DD' in format
    private void funGenerateTextReport(String btnClick) throws Exception
    {

	if ((dteToDate.getDate().getTime() - dteFromDate.getDate().getTime()) < 0)
	{
	    new frmOkPopUp(this, "Invalid date", "Error", 1).setVisible(true);
	}
	else
	{
	    fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());
	    toDate = objUtility.funGetFromToDate(dteToDate.getDate());
	    String posCode = hmPOS.get(cmbPosCode.getSelectedItem().toString());

	    Date dt1 = dteFromDate.getDate();
	    Date dt2 = dteToDate.getDate();
	    SimpleDateFormat ddmmyyyyDateFormat = new SimpleDateFormat("dd-MM-yyyy");
	    String fromDateToDisplay = ddmmyyyyDateFormat.format(dt1);
	    String toDateToDisplay = ddmmyyyyDateFormat.format(dt2);
	    funGenerateTextFile(fromDate, toDate, posCode, btnClick);

	}
    }

    private void funSendReportOnMail(String btnClick) throws Exception
    {

	if ((dteToDate.getDate().getTime() - dteFromDate.getDate().getTime()) < 0)
	{
	    new frmOkPopUp(this, "Invalid date", "Error", 1).setVisible(true);
	}
	else
	{
	    fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());
	    toDate = objUtility.funGetFromToDate(dteToDate.getDate());
	    String posCode = cmbPosCode.getSelectedItem().toString();

	    funGenerateTextFile(fromDate, toDate, posCode, btnClick);

	    String filePath = System.getProperty("user.dir");
	    File file = new File(filePath + File.separator + "Temp" + File.separator + "Settlement Wise Group Wise Breakup.txt");
	    new clsSendMail().funSendMail(clsGlobalVarClass.gReceiverEmailIds, file.getAbsolutePath());
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
        pnlbackground = new JPanel()
        {
            public void paintComponent(Graphics g)
            {
                Image img = Toolkit.getDefaultToolkit().getImage(
                    getClass().getResource("/com/POSReport/images/imgBGJPOS.png"));
                g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), this);
            }
        };

        ;
        pnlMain = new javax.swing.JPanel();
        pnlAPC = new javax.swing.JPanel();
        lblposCode = new javax.swing.JLabel();
        cmbPosCode = new javax.swing.JComboBox();
        lblFromDate = new javax.swing.JLabel();
        dteFromDate = new com.toedter.calendar.JDateChooser();
        dteToDate = new com.toedter.calendar.JDateChooser();
        lblToDate = new javax.swing.JLabel();
        btnView = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();
        lblAPC = new javax.swing.JLabel();
        btnSendOnMail = new javax.swing.JButton();

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
        lblProductName.setText("SPOS -");
        pnlheader.add(lblProductName);

        lblModuleName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblModuleName.setForeground(new java.awt.Color(255, 255, 255));
        pnlheader.add(lblModuleName);

        lblformName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblformName.setForeground(new java.awt.Color(255, 255, 255));
        lblformName.setText("Settlement Wise Group Wise Breakup");
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

        pnlbackground.setLayout(new java.awt.GridBagLayout());

        pnlMain.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, new java.awt.Color(204, 204, 204), new java.awt.Color(204, 204, 204), new java.awt.Color(153, 153, 153), new java.awt.Color(153, 153, 153)));
        pnlMain.setMinimumSize(new java.awt.Dimension(800, 570));
        pnlMain.setOpaque(false);

        pnlAPC.setOpaque(false);
        pnlAPC.setLayout(null);

        lblposCode.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblposCode.setText("POS Name :");
        pnlAPC.add(lblposCode);
        lblposCode.setBounds(250, 120, 90, 30);

        cmbPosCode.setToolTipText("Select POS");
        pnlAPC.add(cmbPosCode);
        cmbPosCode.setBounds(340, 120, 150, 30);

        lblFromDate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblFromDate.setText("From Date :");
        pnlAPC.add(lblFromDate);
        lblFromDate.setBounds(250, 170, 90, 29);

        dteFromDate.setToolTipText("Select From Date");
        dteFromDate.setPreferredSize(new java.awt.Dimension(119, 35));
        pnlAPC.add(dteFromDate);
        dteFromDate.setBounds(340, 170, 150, 30);

        dteToDate.setToolTipText("Select To Date");
        dteToDate.setPreferredSize(new java.awt.Dimension(119, 35));
        pnlAPC.add(dteToDate);
        dteToDate.setBounds(340, 220, 150, 30);

        lblToDate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblToDate.setText("To Date :");
        pnlAPC.add(lblToDate);
        lblToDate.setBounds(250, 220, 90, 30);

        btnView.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        btnView.setForeground(new java.awt.Color(255, 255, 255));
        btnView.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnView.setText("VIEW");
        btnView.setToolTipText("View Report");
        btnView.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnView.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn2.png"))); // NOI18N
        btnView.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnViewMouseClicked(evt);
            }
        });
        pnlAPC.add(btnView);
        btnView.setBounds(400, 500, 96, 41);

        btnClose.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        btnClose.setForeground(new java.awt.Color(255, 255, 255));
        btnClose.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnClose.setText("CLOSE");
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
        pnlAPC.add(btnClose);
        btnClose.setBounds(670, 500, 97, 41);

        lblAPC.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        lblAPC.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblAPC.setText("Settlement Wise Group Wise Tax Breakup");
        lblAPC.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        pnlAPC.add(lblAPC);
        lblAPC.setBounds(130, 40, 520, 30);

        btnSendOnMail.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        btnSendOnMail.setForeground(new java.awt.Color(255, 255, 255));
        btnSendOnMail.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgModBtn.png"))); // NOI18N
        btnSendOnMail.setText("<html>SEND ON<br> MAIL</html>");
        btnSendOnMail.setToolTipText("Close Window");
        btnSendOnMail.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSendOnMail.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn2.png"))); // NOI18N
        btnSendOnMail.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnSendOnMailMouseClicked(evt);
            }
        });
        pnlAPC.add(btnSendOnMail);
        btnSendOnMail.setBounds(530, 500, 110, 41);

        javax.swing.GroupLayout pnlMainLayout = new javax.swing.GroupLayout(pnlMain);
        pnlMain.setLayout(pnlMainLayout);
        pnlMainLayout.setHorizontalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addComponent(pnlAPC, javax.swing.GroupLayout.PREFERRED_SIZE, 795, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlMainLayout.setVerticalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlAPC, javax.swing.GroupLayout.DEFAULT_SIZE, 566, Short.MAX_VALUE)
        );

        pnlbackground.add(pnlMain, new java.awt.GridBagConstraints());

        getContentPane().add(pnlbackground, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnViewMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnViewMouseClicked
	// TODO add your handling code here:
	String btnClick = "View";
	try
	{
	    funGenerateTextReport(btnClick);
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }//GEN-LAST:event_btnViewMouseClicked

    private void btnCloseMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCloseMouseClicked
	// TODO add your handling code here:
	dispose();
	clsGlobalVarClass.hmActiveForms.remove("Managers Report");
    }//GEN-LAST:event_btnCloseMouseClicked

    private void btnSendOnMailMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnSendOnMailMouseClicked
	// TODO add your handling code here:
	String btnClick = "SendOnMail";
	try
	{
	    funSendReportOnMail(btnClick);
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}


    }//GEN-LAST:event_btnSendOnMailMouseClicked

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("Settlement Wise Group Wise Breakup");
    }//GEN-LAST:event_btnCloseActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("Settlement Wise Group Wise Breakup");
    }//GEN-LAST:event_formWindowClosed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("Settlement Wise Group Wise Breakup");
    }//GEN-LAST:event_formWindowClosing


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnSendOnMail;
    private javax.swing.JButton btnView;
    private javax.swing.JComboBox cmbPosCode;
    private com.toedter.calendar.JDateChooser dteFromDate;
    private com.toedter.calendar.JDateChooser dteToDate;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.JLabel lblAPC;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblFromDate;
    private javax.swing.JLabel lblHOSign;
    private javax.swing.JLabel lblModuleName;
    private javax.swing.JLabel lblPosName;
    private javax.swing.JLabel lblProductName;
    private javax.swing.JLabel lblToDate;
    private javax.swing.JLabel lblUserCode;
    private javax.swing.JLabel lblformName;
    private javax.swing.JLabel lblposCode;
    private javax.swing.JPanel pnlAPC;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JPanel pnlbackground;
    private javax.swing.JPanel pnlheader;
    // End of variables declaration//GEN-END:variables

    private void funGenerateTextFile(String fromDate, String toDate, String posCode, String btnClick)
    {
	try
	{
	    objUtility2.funCreateTempFolder();

	    String filePath = System.getProperty("user.dir");
	    File file = new File(filePath + File.separator + "Temp" + File.separator + "Settlement Wise Group Wise Tax Breakup.txt");
	    PrintWriter pw = new PrintWriter(file);

	    String dashedLineOf150Chars = "------------------------------------------------------------------------------------------------------------------------------------------------------";

	    pw.println(clsGlobalVarClass.gClientName);
	    if (clsGlobalVarClass.gClientAddress2.trim().length() > 0)
	    {
		pw.println(clsGlobalVarClass.gClientAddress2);
	    }
	    if (clsGlobalVarClass.gClientAddress3.trim().length() > 0)
	    {
		pw.println(clsGlobalVarClass.gClientAddress3);
	    }
	    pw.println("Report : Date Wise Settlement Wise Group Wise Tax Breakup");
	    pw.println("Reporting Date:" + "  " + fromDate + " " + "To" + " " + toDate);
	    pw.println();
	    pw.println(dashedLineOf150Chars);//line

	    //settlement break up
	    funSettlementWiseData(fromDate, toDate, posCode, pw);

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
	    if (btnClick.equalsIgnoreCase("View"))
	    {
		Desktop dt = Desktop.getDesktop();
		dt.open(file);
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private int funSettlementWiseData(String fromDate, String toDate, String posCode, PrintWriter pw) throws Exception
    {

	pw.println();
	pw.println("Settlement Wise Group Wise Tax Breakup".toUpperCase());
	pw.println();
	pw.println("---------------------------");

	String sqlTip = "", sqlNoOfBill = "", sqlDiscount = "";
	StringBuilder sbSqlLiveFile = new StringBuilder();
	StringBuilder sbSqlQFile = new StringBuilder();

	Map<String, Map<String, clsManagerReportBean>> mapDateWiseData = new TreeMap<String, Map<String, clsManagerReportBean>>();
	Map<String, Map<String, String>> mapDateWiseSettlementNames = new TreeMap<String, Map<String, String>>();
	Map<String, Map<String, String>> mapDateWiseTaxNames = new TreeMap<String, Map<String, String>>();
	Map<String, Map<String, String>> mapDateWiseGroupNames = new TreeMap<String, Map<String, String>>();

	LinkedHashMap<String, String> mapAllGroups = new LinkedHashMap<>();
	LinkedHashMap<String, String> mapAllTaxes = new LinkedHashMap<>();
	LinkedHashMap<String, String> mapAllSettlements = new LinkedHashMap<>();

	//Map<String, clsManagerReportBean> mapDateWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();
	//Map<String, Double> mapDateWiseDiscTipRoundOffData = new TreeMap<String, Double>();
	//Map<Integer, String> mapTaxHeaders = new TreeMap<Integer, String>();
	//Map<String, Double> mapDateWiseTaxBreakupData = new TreeMap<String, Double>();
	//Map<String, clsGroupSubGroupItemBean> mapDateWiseGroupWiseData = new HashMap<String, clsGroupSubGroupItemBean>();
	int cntTax = 1;
	totalTaxAmt = 0.00;
	totalSettleAmt = 0.00;
	totalDiscAmt = 0.00;
	totalTipAmt = 0.00;
	totalRoundOffAmt = 0.00;
	totalBills = 0;

	sbSqlLiveFile.setLength(0);
	sbSqlLiveFile.append(" select c.strSettelmentCode,c.strSettelmentDesc,sum(b.dblSettlementAmt),DATE_FORMAT(date(a.dteBillDate),'%d-%m-%Y')dteBillDate "
		+ " from tblbillhd a,tblbillsettlementdtl b,tblsettelmenthd c "
		+ " where a.strBillNo=b.strBillNo "
		+ " and date(a.dteBillDate)=date(b.dteBillDate) "
		+ " and b.strSettlementCode=c.strSettelmentCode "
		+ " and a.strClientCode=b.strClientCode "//and a.strSettelmentMode!='MultiSettle'
		+ " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		+ " and c.strSettelmentType!='Complementary' "
		+ " ");
	if (!posCode.equalsIgnoreCase("All"))
	{
	    sbSqlLiveFile.append(" and a.strPOSCode='" + posCode + "' ");
	}
	sbSqlLiveFile.append(" GROUP BY date(a.dteBillDate),c.strSettelmentDesc "
		+ " order BY date(a.dteBillDate),c.strSettelmentDesc ");
	System.out.println(sbSqlLiveFile);

	ResultSet rsSettleManager = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLiveFile.toString());
	while (rsSettleManager.next())
	{

	    String settlementCode = rsSettleManager.getString(1);
	    String settlementDesc = rsSettleManager.getString(2);
	    double settleAmt = rsSettleManager.getDouble(3);
	    String billDate = rsSettleManager.getString(4);

	    totalSettleAmt = totalSettleAmt + settleAmt;

	    if (mapDateWiseSettlementNames.containsKey(billDate))
	    {
		Map<String, String> mapSettlementNames = mapDateWiseSettlementNames.get(billDate);

		mapSettlementNames.put(settlementCode, settlementDesc);
	    }
	    else
	    {
		Map<String, String> mapSettlementNames = new TreeMap<>();

		mapSettlementNames.put(settlementCode, settlementDesc);

		mapDateWiseSettlementNames.put(billDate, mapSettlementNames);
	    }

	    if (mapDateWiseData.containsKey(billDate))
	    {
		Map<String, clsManagerReportBean> mapDateWiseSettlementWiseData = mapDateWiseData.get(billDate);

		//put settlement dtl
		if (mapDateWiseSettlementWiseData.containsKey(settlementCode))
		{
		    clsManagerReportBean objManagerReportBean = mapDateWiseSettlementWiseData.get(settlementCode);
		    objManagerReportBean.setDblSettlementAmt(objManagerReportBean.getDblSettlementAmt() + settleAmt);

		    mapDateWiseSettlementWiseData.put(settlementCode, objManagerReportBean);
		}
		else
		{
		    clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		    objManagerReportBean.setStrSettlementCode(settlementCode);
		    objManagerReportBean.setStrSettlementDesc(settlementDesc);
		    objManagerReportBean.setDblSettlementAmt(settleAmt);

		    mapDateWiseSettlementWiseData.put(settlementCode, objManagerReportBean);
		}
		//put total settlement dtl
		if (mapDateWiseSettlementWiseData.containsKey("TotalSettlementAmt"))
		{
		    clsManagerReportBean objManagerReportBean = mapDateWiseSettlementWiseData.get("TotalSettlementAmt");
		    objManagerReportBean.setDblSettlementAmt(objManagerReportBean.getDblSettlementAmt() + settleAmt);

		    mapDateWiseSettlementWiseData.put("TotalSettlementAmt", objManagerReportBean);
		}
		else
		{
		    clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		    objManagerReportBean.setStrSettlementCode("TotalSettlementAmt");
		    objManagerReportBean.setStrSettlementDesc("TotalSettlementAmt");
		    objManagerReportBean.setDblSettlementAmt(settleAmt);

		    mapDateWiseSettlementWiseData.put("TotalSettlementAmt", objManagerReportBean);
		}

		mapDateWiseData.put(billDate, mapDateWiseSettlementWiseData);
	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapDateWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();

		//put settlement dtl
		clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		objManagerReportBean.setStrSettlementCode(settlementCode);
		objManagerReportBean.setStrSettlementDesc(settlementDesc);
		objManagerReportBean.setDblSettlementAmt(settleAmt);

		mapDateWiseSettlementWiseData.put(settlementCode, objManagerReportBean);

		//put total settlement dtl
		objManagerReportBean = new clsManagerReportBean();
		objManagerReportBean.setStrSettlementCode("TotalSettlementAmt");
		objManagerReportBean.setStrSettlementDesc("TotalSettlementAmt");
		objManagerReportBean.setDblSettlementAmt(settleAmt);

		mapDateWiseSettlementWiseData.put("TotalSettlementAmt", objManagerReportBean);

		mapDateWiseData.put(billDate, mapDateWiseSettlementWiseData);
	    }
	}
	rsSettleManager.close();

	sbSqlQFile.setLength(0);
	sbSqlQFile.append(" select c.strSettelmentCode,c.strSettelmentDesc,sum(b.dblSettlementAmt),DATE_FORMAT(date(a.dteBillDate),'%d-%m-%Y')dteBillDate "
		+ " from tblqbillhd a,tblqbillsettlementdtl b,tblsettelmenthd c "
		+ " where a.strBillNo=b.strBillNo "
		+ " and date(a.dteBillDate)=date(b.dteBillDate) "
		+ " and b.strSettlementCode=c.strSettelmentCode "
		+ " and a.strClientCode=b.strClientCode "//and a.strSettelmentMode!='MultiSettle' 
		+ " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		+ " and c.strSettelmentType!='Complementary' ");
	if (!posCode.equalsIgnoreCase("All"))
	{
	    sbSqlQFile.append(" and a.strPOSCode='" + posCode + "' ");
	}
	sbSqlQFile.append(" GROUP BY date(a.dteBillDate),c.strSettelmentDesc "
		+ " order BY date(a.dteBillDate),c.strSettelmentDesc ");
	rsSettleManager = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFile.toString());

	while (rsSettleManager.next())
	{
	    String settlementCode = rsSettleManager.getString(1);
	    String settlementDesc = rsSettleManager.getString(2);
	    double settleAmt = rsSettleManager.getDouble(3);
	    String billDate = rsSettleManager.getString(4);

	    totalSettleAmt = totalSettleAmt + settleAmt;

	    if (mapDateWiseSettlementNames.containsKey(billDate))
	    {
		Map<String, String> mapSettlementNames = mapDateWiseSettlementNames.get(billDate);

		mapSettlementNames.put(settlementCode, settlementDesc);
	    }
	    else
	    {
		Map<String, String> mapSettlementNames = new TreeMap<>();

		mapSettlementNames.put(settlementCode, settlementDesc);

		mapDateWiseSettlementNames.put(billDate, mapSettlementNames);
	    }

	    if (mapDateWiseData.containsKey(billDate))
	    {
		Map<String, clsManagerReportBean> mapDateWiseSettlementWiseData = mapDateWiseData.get(billDate);

		//put settlement dtl
		if (mapDateWiseSettlementWiseData.containsKey(settlementCode))
		{
		    clsManagerReportBean objManagerReportBean = mapDateWiseSettlementWiseData.get(settlementCode);
		    objManagerReportBean.setDblSettlementAmt(objManagerReportBean.getDblSettlementAmt() + settleAmt);

		    mapDateWiseSettlementWiseData.put(settlementCode, objManagerReportBean);
		}
		else
		{
		    clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		    objManagerReportBean.setStrSettlementCode(settlementCode);
		    objManagerReportBean.setStrSettlementDesc(settlementDesc);
		    objManagerReportBean.setDblSettlementAmt(settleAmt);

		    mapDateWiseSettlementWiseData.put(settlementCode, objManagerReportBean);
		}
		//put total settlement dtl
		if (mapDateWiseSettlementWiseData.containsKey("TotalSettlementAmt"))
		{
		    clsManagerReportBean objManagerReportBean = mapDateWiseSettlementWiseData.get("TotalSettlementAmt");
		    objManagerReportBean.setDblSettlementAmt(objManagerReportBean.getDblSettlementAmt() + settleAmt);

		    mapDateWiseSettlementWiseData.put("TotalSettlementAmt", objManagerReportBean);
		}
		else
		{
		    clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		    objManagerReportBean.setStrSettlementCode("TotalSettlementAmt");
		    objManagerReportBean.setStrSettlementDesc("TotalSettlementAmt");
		    objManagerReportBean.setDblSettlementAmt(settleAmt);

		    mapDateWiseSettlementWiseData.put("TotalSettlementAmt", objManagerReportBean);
		}

		mapDateWiseData.put(billDate, mapDateWiseSettlementWiseData);
	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapDateWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();
		//put settlement dtl

		clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		objManagerReportBean.setStrSettlementCode(settlementCode);
		objManagerReportBean.setStrSettlementDesc(settlementDesc);
		objManagerReportBean.setDblSettlementAmt(settleAmt);

		mapDateWiseSettlementWiseData.put(settlementCode, objManagerReportBean);

		//put total settlement dtl
		objManagerReportBean = new clsManagerReportBean();
		objManagerReportBean.setStrSettlementCode("TotalSettlementAmt");
		objManagerReportBean.setStrSettlementDesc("TotalSettlementAmt");
		objManagerReportBean.setDblSettlementAmt(settleAmt);

		mapDateWiseSettlementWiseData.put("TotalSettlementAmt", objManagerReportBean);

		mapDateWiseData.put(billDate, mapDateWiseSettlementWiseData);
	    }
	}
	rsSettleManager.close();

	/**
	 * live taxes
	 */
	String sqlTax = "select DATE_FORMAT(date(a.dteBillDate),'%d-%m-%Y')dteBillDate,c.strTaxCode,c.strTaxDesc,sum(b.dblTaxAmount) "
		+ " from tblbillhd a,tblbilltaxdtl b,tbltaxhd c "
		+ " where a.strBillNo=b.strBillNo "
		+ " and date(a.dteBillDate)=date(b.dteBillDate) "
		+ " and b.strTaxCode=c.strTaxCode "
		+ " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "'"
		+ " and a.strClientCode=b.strClientCode ";
	if (!posCode.equalsIgnoreCase("All"))
	{
	    sqlTax += " and a.strPOSCode='" + posCode + "' ";
	}
	sqlTax += " group by date(a.dteBillDate),c.strTaxCode";
	ResultSet rsTaxDtl1 = clsGlobalVarClass.dbMysql.executeResultSet(sqlTax);
	while (rsTaxDtl1.next())
	{
	    String billDate = rsTaxDtl1.getString(1);
	    String taxCode = rsTaxDtl1.getString(2);
	    String taxDesc = rsTaxDtl1.getString(3);
	    double taxAmt = rsTaxDtl1.getDouble(4);

	    totalTaxAmt = totalTaxAmt + taxAmt;

	    if (mapDateWiseTaxNames.containsKey(billDate))
	    {
		Map<String, String> mapTaxNames = mapDateWiseTaxNames.get(billDate);

		mapTaxNames.put(taxCode, taxDesc);
	    }
	    else
	    {
		Map<String, String> mapTaxNames = new TreeMap<>();

		mapTaxNames.put(taxCode, taxDesc);

		mapDateWiseTaxNames.put(billDate, mapTaxNames);
	    }

	    if (mapDateWiseData.containsKey(billDate))
	    {
		Map<String, clsManagerReportBean> mapDateWiseSettlementWiseData = mapDateWiseData.get(billDate);

		//put tax dtl
		if (mapDateWiseSettlementWiseData.containsKey(taxCode))
		{
		    clsManagerReportBean objManagerReportBean = mapDateWiseSettlementWiseData.get(taxCode);
		    objManagerReportBean.setDblSettlementAmt(objManagerReportBean.getDblTaxAmt() + taxAmt);

		    mapDateWiseSettlementWiseData.put(taxCode, objManagerReportBean);
		}
		else
		{
		    clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		    objManagerReportBean.setStrTaxCode(taxCode);
		    objManagerReportBean.setStrTaxDesc(taxDesc);
		    objManagerReportBean.setDblTaxAmt(taxAmt);

		    mapDateWiseSettlementWiseData.put(taxCode, objManagerReportBean);
		}

		//put total tax dtl
		if (mapDateWiseSettlementWiseData.containsKey("TotalTaxAmt"))
		{
		    clsManagerReportBean objManagerReportBean = mapDateWiseSettlementWiseData.get("TotalTaxAmt");
		    objManagerReportBean.setDblTaxAmt(objManagerReportBean.getDblTaxAmt() + taxAmt);

		    mapDateWiseSettlementWiseData.put("TotalTaxAmt", objManagerReportBean);
		}
		else
		{
		    clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		    objManagerReportBean.setStrTaxCode("TotalTaxAmt");
		    objManagerReportBean.setStrTaxDesc("TotalTaxAmt");
		    objManagerReportBean.setDblTaxAmt(taxAmt);

		    mapDateWiseSettlementWiseData.put("TotalTaxAmt", objManagerReportBean);
		}

		mapDateWiseData.put(billDate, mapDateWiseSettlementWiseData);
	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapDateWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();

		clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		objManagerReportBean.setStrTaxCode(taxCode);
		objManagerReportBean.setStrTaxDesc(taxDesc);
		objManagerReportBean.setDblTaxAmt(taxAmt);

		//put total tax dtl
		objManagerReportBean = new clsManagerReportBean();
		objManagerReportBean.setStrTaxCode("TotalTaxAmt");
		objManagerReportBean.setStrTaxDesc("TotalTaxAmt");
		objManagerReportBean.setDblTaxAmt(taxAmt);

		mapDateWiseSettlementWiseData.put("TotalTaxAmt", objManagerReportBean);

		mapDateWiseSettlementWiseData.put(taxCode, objManagerReportBean);

		mapDateWiseData.put(billDate, mapDateWiseSettlementWiseData);
	    }
	}
	rsTaxDtl1.close();

	/**
	 * Q taxes
	 */
	sqlTax = "select DATE_FORMAT(date(a.dteBillDate),'%d-%m-%Y')dteBillDate,c.strTaxCode,c.strTaxDesc,sum(b.dblTaxAmount) "
		+ " from tblqbillhd a,tblqbilltaxdtl b,tbltaxhd c "
		+ " where a.strBillNo=b.strBillNo "
		+ " and date(a.dteBillDate)=date(b.dteBillDate) "
		+ " and b.strTaxCode=c.strTaxCode "
		+ " and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "'"
		+ " and a.strClientCode=b.strClientCode ";
	if (!posCode.equalsIgnoreCase("All"))
	{
	    sqlTax += " and a.strPOSCode='" + posCode + "' ";
	}
	sqlTax += " group by date(a.dteBillDate),c.strTaxCode";
	rsTaxDtl1 = clsGlobalVarClass.dbMysql.executeResultSet(sqlTax);
	while (rsTaxDtl1.next())
	{
	    String billDate = rsTaxDtl1.getString(1);
	    String taxCode = rsTaxDtl1.getString(2);
	    String taxDesc = rsTaxDtl1.getString(3);
	    double taxAmt = rsTaxDtl1.getDouble(4);

	    totalTaxAmt = totalTaxAmt + taxAmt;

	    if (mapDateWiseTaxNames.containsKey(billDate))
	    {
		Map<String, String> mapTaxNames = mapDateWiseTaxNames.get(billDate);

		mapTaxNames.put(taxCode, taxDesc);
	    }
	    else
	    {
		Map<String, String> mapTaxNames = new TreeMap<>();

		mapTaxNames.put(taxCode, taxDesc);

		mapDateWiseTaxNames.put(billDate, mapTaxNames);
	    }

	    if (mapDateWiseData.containsKey(billDate))
	    {
		Map<String, clsManagerReportBean> mapDateWiseSettlementWiseData = mapDateWiseData.get(billDate);

		//put tax dtl
		if (mapDateWiseSettlementWiseData.containsKey(taxCode))
		{
		    clsManagerReportBean objManagerReportBean = mapDateWiseSettlementWiseData.get(taxCode);
		    objManagerReportBean.setDblSettlementAmt(objManagerReportBean.getDblTaxAmt() + taxAmt);

		    mapDateWiseSettlementWiseData.put(taxCode, objManagerReportBean);
		}
		else
		{
		    clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		    objManagerReportBean.setStrTaxCode(taxCode);
		    objManagerReportBean.setStrTaxDesc(taxDesc);
		    objManagerReportBean.setDblTaxAmt(taxAmt);

		    mapDateWiseSettlementWiseData.put(taxCode, objManagerReportBean);
		}

		//put total tax dtl
		if (mapDateWiseSettlementWiseData.containsKey("TotalTaxAmt"))
		{
		    clsManagerReportBean objManagerReportBean = mapDateWiseSettlementWiseData.get("TotalTaxAmt");
		    objManagerReportBean.setDblTaxAmt(objManagerReportBean.getDblTaxAmt() + taxAmt);

		    mapDateWiseSettlementWiseData.put("TotalTaxAmt", objManagerReportBean);
		}
		else
		{
		    clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		    objManagerReportBean.setStrTaxCode("TotalTaxAmt");
		    objManagerReportBean.setStrTaxDesc("TotalTaxAmt");
		    objManagerReportBean.setDblTaxAmt(taxAmt);

		    mapDateWiseSettlementWiseData.put("TotalTaxAmt", objManagerReportBean);
		}

		mapDateWiseData.put(billDate, mapDateWiseSettlementWiseData);
	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapDateWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();

		clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		objManagerReportBean.setStrTaxCode(taxCode);
		objManagerReportBean.setStrTaxDesc(taxDesc);
		objManagerReportBean.setDblTaxAmt(taxAmt);

		objManagerReportBean = new clsManagerReportBean();
		objManagerReportBean.setStrTaxCode("TotalTaxAmt");
		objManagerReportBean.setStrTaxDesc("TotalTaxAmt");
		objManagerReportBean.setDblTaxAmt(taxAmt);

		mapDateWiseSettlementWiseData.put("TotalTaxAmt", objManagerReportBean);

		mapDateWiseSettlementWiseData.put(taxCode, objManagerReportBean);

		mapDateWiseData.put(billDate, mapDateWiseSettlementWiseData);
	    }
	}
	rsTaxDtl1.close();

	//set discount,roundoff,tip
	sbSqlLiveFile.setLength(0);
	sbSqlLiveFile.append(" SELECT sum(a.dblDiscountAmt),sum(a.dblRoundOff),sum(a.dblTipAmount),count(*),DATE_FORMAT(date(a.dteBillDate),'%d-%m-%Y')dteBillDate "
		+ " from tblbillhd a "
		+ " where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		+ " ");
	if (!posCode.equalsIgnoreCase("All"))
	{
	    sbSqlLiveFile.append(" and a.strPOSCode='" + posCode + "' ");
	}
	sbSqlLiveFile.append(" group by date(a.dteBillDate) ");
	System.out.println(sbSqlLiveFile);

	rsSettleManager = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLiveFile.toString());
	while (rsSettleManager.next())
	{
	    double discAmt = rsSettleManager.getDouble(1);//discAmt
	    double roundOffAmt = rsSettleManager.getDouble(2);//roundOff
	    double tipAmt = rsSettleManager.getDouble(3);//tipAmt
	    int noOfBills = rsSettleManager.getInt(4);//bill count
	    totalDiscAmt = totalDiscAmt + discAmt;
	    totalRoundOffAmt = totalRoundOffAmt + roundOffAmt;//roundOff
	    totalTipAmt = totalTipAmt + tipAmt;//tipAmt
	    totalBills = totalBills + noOfBills;//bill count
	    String billdate = rsSettleManager.getString(5);//billDate

	    //discount
	    if (mapDateWiseData.containsKey(billdate))
	    {
		Map<String, clsManagerReportBean> mapDateWiseSettlementWiseData = mapDateWiseData.get(billdate);
		if (mapDateWiseSettlementWiseData.containsKey("DiscAmt"))
		{
		    clsManagerReportBean objDiscAmt = mapDateWiseSettlementWiseData.get("DiscAmt");
		    objDiscAmt.setDblDiscAmt(objDiscAmt.getDblDiscAmt() + discAmt);
		}
		else
		{
		    clsManagerReportBean objDiscAmt = new clsManagerReportBean();
		    objDiscAmt.setDblDiscAmt(discAmt);

		    mapDateWiseSettlementWiseData.put("DiscAmt", objDiscAmt);
		}

	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapDateWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();

		clsManagerReportBean objDiscAmt = new clsManagerReportBean();
		objDiscAmt.setDblDiscAmt(discAmt);

		mapDateWiseSettlementWiseData.put("DiscAmt", objDiscAmt);

		mapDateWiseData.put(billdate, mapDateWiseSettlementWiseData);
	    }

	    //roundoff
	    if (mapDateWiseData.containsKey(billdate))
	    {
		Map<String, clsManagerReportBean> mapDateWiseSettlementWiseData = mapDateWiseData.get(billdate);
		if (mapDateWiseSettlementWiseData.containsKey("RoundOffAmt"))
		{
		    clsManagerReportBean objRoundOffAmt = mapDateWiseSettlementWiseData.get("RoundOffAmt");
		    objRoundOffAmt.setDblRoundOffAmt(objRoundOffAmt.getDblRoundOffAmt() + roundOffAmt);
		}
		else
		{
		    clsManagerReportBean objRoundOffAmt = new clsManagerReportBean();
		    objRoundOffAmt.setDblRoundOffAmt(roundOffAmt);

		    mapDateWiseSettlementWiseData.put("RoundOffAmt", objRoundOffAmt);
		}

	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapDateWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();

		clsManagerReportBean objRoundOffAmt = new clsManagerReportBean();
		objRoundOffAmt.setDblRoundOffAmt(roundOffAmt);

		mapDateWiseSettlementWiseData.put("RoundOffAmt", objRoundOffAmt);

		mapDateWiseData.put(billdate, mapDateWiseSettlementWiseData);
	    }

	    //tip
	    if (mapDateWiseData.containsKey(billdate))
	    {
		Map<String, clsManagerReportBean> mapDateWiseSettlementWiseData = mapDateWiseData.get(billdate);
		if (mapDateWiseSettlementWiseData.containsKey("TipAmt"))
		{
		    clsManagerReportBean objTipAmt = mapDateWiseSettlementWiseData.get("TipAmt");
		    objTipAmt.setDblTipAmt(objTipAmt.getDblTipAmt() + tipAmt);
		}
		else
		{
		    clsManagerReportBean objTipAmt = new clsManagerReportBean();
		    objTipAmt.setDblTipAmt(tipAmt);

		    mapDateWiseSettlementWiseData.put("TipAmt", objTipAmt);
		}

	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapDateWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();

		clsManagerReportBean objTipAmt = new clsManagerReportBean();
		objTipAmt.setDblTipAmt(tipAmt);

		mapDateWiseSettlementWiseData.put("TipAmt", objTipAmt);

		mapDateWiseData.put(billdate, mapDateWiseSettlementWiseData);
	    }
	    //no of bills
	    if (mapDateWiseData.containsKey(billdate))
	    {
		Map<String, clsManagerReportBean> mapDateWiseSettlementWiseData = mapDateWiseData.get(billdate);
		if (mapDateWiseSettlementWiseData.containsKey("NoOfBills"))
		{
		    clsManagerReportBean objNoOfBills = mapDateWiseSettlementWiseData.get("NoOfBills");
		    objNoOfBills.setIntNofOfBills(objNoOfBills.getIntNofOfBills() + noOfBills);
		}
		else
		{
		    clsManagerReportBean objNoOfBills = new clsManagerReportBean();
		    objNoOfBills.setIntNofOfBills(noOfBills);

		    mapDateWiseSettlementWiseData.put("NoOfBills", objNoOfBills);
		}

	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapDateWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();

		clsManagerReportBean objNoOfBills = new clsManagerReportBean();
		objNoOfBills.setIntNofOfBills(noOfBills);

		mapDateWiseSettlementWiseData.put("NoOfBills", objNoOfBills);

		mapDateWiseData.put(billdate, mapDateWiseSettlementWiseData);
	    }

	}
	rsSettleManager.close();

	sbSqlQFile.setLength(0);
	sbSqlQFile.append(" SELECT sum(a.dblDiscountAmt),sum(a.dblRoundOff),sum(a.dblTipAmount),count(*),DATE_FORMAT(date(a.dteBillDate),'%d-%m-%Y')dteBillDate "
		+ " from tblqbillhd a "
		+ " where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		+ " ");
	if (!posCode.equalsIgnoreCase("All"))
	{
	    sbSqlQFile.append(" and a.strPOSCode='" + posCode + "' ");
	}
	sbSqlQFile.append(" group by date(a.dteBillDate) ");
	System.out.println(sbSqlQFile);

	rsSettleManager = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFile.toString());
	while (rsSettleManager.next())
	{
	    double discAmt = rsSettleManager.getDouble(1);//discAmt
	    double roundOffAmt = rsSettleManager.getDouble(2);//roundOff
	    double tipAmt = rsSettleManager.getDouble(3);//tipAmt
	    int noOfBills = rsSettleManager.getInt(4);//bill count
	    totalDiscAmt = totalDiscAmt + discAmt;
	    totalRoundOffAmt = totalRoundOffAmt + roundOffAmt;//roundOff
	    totalTipAmt = totalTipAmt + tipAmt;//tipAmt
	    totalBills = totalBills + noOfBills;//bill count
	    String billdate = rsSettleManager.getString(5);//billDate

	    //discount
	    if (mapDateWiseData.containsKey(billdate))
	    {
		Map<String, clsManagerReportBean> mapDateWiseSettlementWiseData = mapDateWiseData.get(billdate);
		if (mapDateWiseSettlementWiseData.containsKey("DiscAmt"))
		{
		    clsManagerReportBean objDiscAmt = mapDateWiseSettlementWiseData.get("DiscAmt");
		    objDiscAmt.setDblDiscAmt(objDiscAmt.getDblDiscAmt() + discAmt);
		}
		else
		{
		    clsManagerReportBean objDiscAmt = new clsManagerReportBean();
		    objDiscAmt.setDblDiscAmt(discAmt);

		    mapDateWiseSettlementWiseData.put("DiscAmt", objDiscAmt);
		}

	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapDateWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();

		clsManagerReportBean objDiscAmt = new clsManagerReportBean();
		objDiscAmt.setDblDiscAmt(discAmt);

		mapDateWiseSettlementWiseData.put("DiscAmt", objDiscAmt);

		mapDateWiseData.put(billdate, mapDateWiseSettlementWiseData);
	    }

	    //roundoff
	    if (mapDateWiseData.containsKey(billdate))
	    {
		Map<String, clsManagerReportBean> mapDateWiseSettlementWiseData = mapDateWiseData.get(billdate);
		if (mapDateWiseSettlementWiseData.containsKey("RoundOffAmt"))
		{
		    clsManagerReportBean objRoundOffAmt = mapDateWiseSettlementWiseData.get("RoundOffAmt");
		    objRoundOffAmt.setDblRoundOffAmt(objRoundOffAmt.getDblRoundOffAmt() + roundOffAmt);
		}
		else
		{
		    clsManagerReportBean objRoundOffAmt = new clsManagerReportBean();
		    objRoundOffAmt.setDblRoundOffAmt(roundOffAmt);

		    mapDateWiseSettlementWiseData.put("RoundOffAmt", objRoundOffAmt);
		}

	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapDateWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();

		clsManagerReportBean objRoundOffAmt = new clsManagerReportBean();
		objRoundOffAmt.setDblRoundOffAmt(roundOffAmt);

		mapDateWiseSettlementWiseData.put("RoundOffAmt", objRoundOffAmt);

		mapDateWiseData.put(billdate, mapDateWiseSettlementWiseData);
	    }

	    //tip
	    if (mapDateWiseData.containsKey(billdate))
	    {
		Map<String, clsManagerReportBean> mapDateWiseSettlementWiseData = mapDateWiseData.get(billdate);
		if (mapDateWiseSettlementWiseData.containsKey("TipAmt"))
		{
		    clsManagerReportBean objTipAmt = mapDateWiseSettlementWiseData.get("TipAmt");
		    objTipAmt.setDblTipAmt(objTipAmt.getDblTipAmt() + tipAmt);
		}
		else
		{
		    clsManagerReportBean objTipAmt = new clsManagerReportBean();
		    objTipAmt.setDblTipAmt(tipAmt);

		    mapDateWiseSettlementWiseData.put("TipAmt", objTipAmt);
		}

	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapDateWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();

		clsManagerReportBean objTipAmt = new clsManagerReportBean();
		objTipAmt.setDblTipAmt(tipAmt);

		mapDateWiseSettlementWiseData.put("TipAmt", objTipAmt);

		mapDateWiseData.put(billdate, mapDateWiseSettlementWiseData);
	    }
	    //no of bills
	    if (mapDateWiseData.containsKey(billdate))
	    {
		Map<String, clsManagerReportBean> mapDateWiseSettlementWiseData = mapDateWiseData.get(billdate);
		if (mapDateWiseSettlementWiseData.containsKey("NoOfBills"))
		{
		    clsManagerReportBean objNoOfBills = mapDateWiseSettlementWiseData.get("NoOfBills");
		    objNoOfBills.setIntNofOfBills(objNoOfBills.getIntNofOfBills() + noOfBills);
		}
		else
		{
		    clsManagerReportBean objNoOfBills = new clsManagerReportBean();
		    objNoOfBills.setIntNofOfBills(noOfBills);

		    mapDateWiseSettlementWiseData.put("NoOfBills", objNoOfBills);
		}

	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapDateWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();

		clsManagerReportBean objNoOfBills = new clsManagerReportBean();
		objNoOfBills.setIntNofOfBills(noOfBills);

		mapDateWiseSettlementWiseData.put("NoOfBills", objNoOfBills);

		mapDateWiseData.put(billdate, mapDateWiseSettlementWiseData);
	    }
	}
	rsSettleManager.close();

	/**
	 * fill live date wise group wise data
	 */
	StringBuilder sqlGroupData = new StringBuilder();

	sqlGroupData.setLength(0);
	sqlGroupData.append("select DATE_FORMAT(date(a.dteBillDate),'%d-%m-%Y'),e.strGroupCode,e.strGroupName,sum(b.dblAmount)SubTotal,sum(b.dblDiscountAmt)Discount,sum(b.dblAmount)-sum(b.dblDiscountAmt)NetTotal "
		+ "from tblbillhd a,tblbilldtl b,tblitemmaster c,tblsubgrouphd d,tblgrouphd e "
		+ "where a.strBillNo=b.strBillNo "
		+ "AND DATE(a.dteBillDate)= DATE(b.dteBillDate) "
		+ "and b.strItemCode=c.strItemCode "
		+ "and c.strSubGroupCode=d.strSubGroupCode "
		+ "and d.strGroupCode=e.strGroupCode "
		+ "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	if (!posCode.equalsIgnoreCase("All"))
	{
	    sqlGroupData.append(" and a.strPOSCode='" + posCode + "' ");
	}
	sqlGroupData.append("group by date(a.dteBillDate),e.strGroupCode ");
	ResultSet rsGroupsData = clsGlobalVarClass.dbMysql.executeResultSet(sqlGroupData.toString());
	while (rsGroupsData.next())
	{
	    String billDate = rsGroupsData.getString(1);//date
	    String groupCode = rsGroupsData.getString(2);//groupCode
	    String groupName = rsGroupsData.getString(3);//groupName
	    double subTotal = rsGroupsData.getDouble(4); //subTotal
	    double discount = rsGroupsData.getDouble(5); //discount
	    double netTotal = rsGroupsData.getDouble(6); //netTotal

	    if (mapDateWiseGroupNames.containsKey(billDate))
	    {
		Map<String, String> mapGroupNames = mapDateWiseGroupNames.get(billDate);

		mapGroupNames.put(groupCode, groupName);
	    }
	    else
	    {
		Map<String, String> mapGroupNames = new TreeMap<>();

		mapGroupNames.put(groupCode, groupName);

		mapDateWiseGroupNames.put(billDate, mapGroupNames);
	    }

	    if (mapDateWiseData.containsKey(billDate))
	    {
		Map<String, clsManagerReportBean> mapDateWiseSettlementWiseData = mapDateWiseData.get(billDate);

		if (mapDateWiseSettlementWiseData.containsKey(groupCode))
		{
		    clsManagerReportBean objGroupDtl = mapDateWiseSettlementWiseData.get(groupCode);

		    objGroupDtl.setDblSubTotal(objGroupDtl.getDblSubTotal() + subTotal);
		    objGroupDtl.setDblDisAmt(objGroupDtl.getDblDisAmt() + discount);
		    objGroupDtl.setDblNetTotal(objGroupDtl.getDblNetTotal() + netTotal);
		}
		else
		{
		    clsManagerReportBean objGroupDtl = new clsManagerReportBean();
		    objGroupDtl.setStrGroupCode(groupCode);
		    objGroupDtl.setStrGroupName(groupName);
		    objGroupDtl.setDblSubTotal(subTotal);
		    objGroupDtl.setDblDisAmt(discount);
		    objGroupDtl.setDblNetTotal(netTotal);

		    mapDateWiseSettlementWiseData.put(groupCode, objGroupDtl);
		}

		//put total settlement dtl
		if (mapDateWiseSettlementWiseData.containsKey("TotalGroupAmt"))
		{
		    clsManagerReportBean objManagerReportBean = mapDateWiseSettlementWiseData.get("TotalGroupAmt");
		    objManagerReportBean.setDblSubTotal(objManagerReportBean.getDblSubTotal() + subTotal);
		    objManagerReportBean.setDblNetTotal(objManagerReportBean.getDblNetTotal() + netTotal);

		    mapDateWiseSettlementWiseData.put("TotalGroupAmt", objManagerReportBean);
		}
		else
		{
		    clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		    objManagerReportBean.setStrGroupCode("TotalGroupAmt");
		    objManagerReportBean.setStrGroupName("TotalGroupAmt");
		    objManagerReportBean.setDblSubTotal(subTotal);
		    objManagerReportBean.setDblNetTotal(netTotal);

		    mapDateWiseSettlementWiseData.put("TotalGroupAmt", objManagerReportBean);
		}
	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapDateWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();

		clsManagerReportBean objGroupDtl = new clsManagerReportBean();
		objGroupDtl.setStrGroupCode(groupCode);
		objGroupDtl.setStrGroupName(groupName);
		objGroupDtl.setDblSubTotal(subTotal);
		objGroupDtl.setDblDisAmt(discount);
		objGroupDtl.setDblNetTotal(netTotal);

		//put total settlement dtl
		clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		objManagerReportBean.setStrGroupCode("TotalGroupAmt");
		objManagerReportBean.setStrGroupName("TotalGroupAmt");
		objManagerReportBean.setDblSubTotal(subTotal);
		objManagerReportBean.setDblNetTotal(netTotal);

		mapDateWiseSettlementWiseData.put("TotalGroupAmt", objManagerReportBean);

		mapDateWiseSettlementWiseData.put(groupCode, objGroupDtl);

		mapDateWiseData.put(billDate, mapDateWiseSettlementWiseData);
	    }
	}
	rsGroupsData.close();

	/**
	 * fill live modifiers date wise group wise data
	 */
	sqlGroupData.setLength(0);
	sqlGroupData.append("select DATE_FORMAT(date(a.dteBillDate),'%d-%m-%Y'),e.strGroupCode,e.strGroupName,sum(b.dblAmount)SubTotal,sum(b.dblDiscAmt)Discount,sum(b.dblAmount)-sum(b.dblDiscAmt)NetTotal "
		+ "from tblbillhd a,tblbillmodifierdtl b,tblitemmaster c,tblsubgrouphd d,tblgrouphd e "
		+ "where a.strBillNo=b.strBillNo "
		+ "AND DATE(a.dteBillDate)= DATE(b.dteBillDate) "
		+ "and left(b.strItemCode,7)=c.strItemCode "
		+ "and c.strSubGroupCode=d.strSubGroupCode "
		+ "and d.strGroupCode=e.strGroupCode "
		+ "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	if (!posCode.equalsIgnoreCase("All"))
	{
	    sqlGroupData.append(" and a.strPOSCode='" + posCode + "' ");
	}
	sqlGroupData.append("group by date(a.dteBillDate),e.strGroupCode ");
	rsGroupsData = clsGlobalVarClass.dbMysql.executeResultSet(sqlGroupData.toString());
	while (rsGroupsData.next())
	{
	    String billDate = rsGroupsData.getString(1);//date
	    String groupCode = rsGroupsData.getString(2);//groupCode
	    String groupName = rsGroupsData.getString(3);//groupName
	    double subTotal = rsGroupsData.getDouble(4); //subTotal
	    double discount = rsGroupsData.getDouble(5); //discount
	    double netTotal = rsGroupsData.getDouble(6); //netTotal

	    if (mapDateWiseGroupNames.containsKey(billDate))
	    {
		Map<String, String> mapGroupNames = mapDateWiseGroupNames.get(billDate);

		mapGroupNames.put(groupCode, groupName);
	    }
	    else
	    {
		Map<String, String> mapGroupNames = new TreeMap<>();

		mapGroupNames.put(groupCode, groupName);

		mapDateWiseGroupNames.put(billDate, mapGroupNames);
	    }

	    if (mapDateWiseData.containsKey(billDate))
	    {
		Map<String, clsManagerReportBean> mapDateWiseSettlementWiseData = mapDateWiseData.get(billDate);

		if (mapDateWiseSettlementWiseData.containsKey(groupCode))
		{
		    clsManagerReportBean objGroupDtl = mapDateWiseSettlementWiseData.get(groupCode);

		    objGroupDtl.setDblSubTotal(objGroupDtl.getDblSubTotal() + subTotal);
		    objGroupDtl.setDblDisAmt(objGroupDtl.getDblDisAmt() + discount);
		    objGroupDtl.setDblNetTotal(objGroupDtl.getDblNetTotal() + netTotal);
		}
		else
		{
		    clsManagerReportBean objGroupDtl = new clsManagerReportBean();
		    objGroupDtl.setStrGroupCode(groupCode);
		    objGroupDtl.setStrGroupName(groupName);
		    objGroupDtl.setDblSubTotal(subTotal);
		    objGroupDtl.setDblDisAmt(discount);
		    objGroupDtl.setDblNetTotal(netTotal);

		    mapDateWiseSettlementWiseData.put(groupCode, objGroupDtl);
		}

		//put total settlement dtl
		if (mapDateWiseSettlementWiseData.containsKey("TotalGroupAmt"))
		{
		    clsManagerReportBean objManagerReportBean = mapDateWiseSettlementWiseData.get("TotalGroupAmt");
		    objManagerReportBean.setDblSubTotal(objManagerReportBean.getDblSubTotal() + subTotal);
		    objManagerReportBean.setDblNetTotal(objManagerReportBean.getDblNetTotal() + netTotal);

		    mapDateWiseSettlementWiseData.put("TotalGroupAmt", objManagerReportBean);
		}
		else
		{
		    clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		    objManagerReportBean.setStrGroupCode("TotalGroupAmt");
		    objManagerReportBean.setStrGroupName("TotalGroupAmt");
		    objManagerReportBean.setDblSubTotal(subTotal);
		    objManagerReportBean.setDblNetTotal(netTotal);

		    mapDateWiseSettlementWiseData.put("TotalGroupAmt", objManagerReportBean);
		}
	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapDateWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();

		clsManagerReportBean objGroupDtl = new clsManagerReportBean();
		objGroupDtl.setStrGroupCode(groupCode);
		objGroupDtl.setStrGroupName(groupName);
		objGroupDtl.setDblSubTotal(subTotal);
		objGroupDtl.setDblDisAmt(discount);
		objGroupDtl.setDblNetTotal(netTotal);

		//put total settlement dtl
		clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		objManagerReportBean.setStrGroupCode("TotalGroupAmt");
		objManagerReportBean.setStrGroupName("TotalGroupAmt");
		objManagerReportBean.setDblSubTotal(subTotal);
		objManagerReportBean.setDblNetTotal(netTotal);

		mapDateWiseSettlementWiseData.put("TotalGroupAmt", objManagerReportBean);

		mapDateWiseSettlementWiseData.put(groupCode, objGroupDtl);

		mapDateWiseData.put(billDate, mapDateWiseSettlementWiseData);
	    }
	}
	rsGroupsData.close();

	/**
	 * fill Q date wise group wise data
	 */
	sqlGroupData.setLength(0);
	sqlGroupData.append("select DATE_FORMAT(date(a.dteBillDate),'%d-%m-%Y'),e.strGroupCode,e.strGroupName,sum(b.dblAmount)SubTotal,sum(b.dblDiscountAmt)Discount,sum(b.dblAmount)-sum(b.dblDiscountAmt)NetTotal "
		+ "from tblqbillhd a,tblqbilldtl b,tblitemmaster c,tblsubgrouphd d,tblgrouphd e "
		+ "where a.strBillNo=b.strBillNo "
		+ "AND DATE(a.dteBillDate)= DATE(b.dteBillDate) "
		+ "and b.strItemCode=c.strItemCode "
		+ "and c.strSubGroupCode=d.strSubGroupCode "
		+ "and d.strGroupCode=e.strGroupCode "
		+ "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	if (!posCode.equalsIgnoreCase("All"))
	{
	    sqlGroupData.append(" and a.strPOSCode='" + posCode + "' ");
	}
	sqlGroupData.append("group by date(a.dteBillDate),e.strGroupCode ");
	rsGroupsData = clsGlobalVarClass.dbMysql.executeResultSet(sqlGroupData.toString());
	while (rsGroupsData.next())
	{
	    String billDate = rsGroupsData.getString(1);//date
	    String groupCode = rsGroupsData.getString(2);//groupCode
	    String groupName = rsGroupsData.getString(3);//groupName
	    double subTotal = rsGroupsData.getDouble(4); //subTotal
	    double discount = rsGroupsData.getDouble(5); //discount
	    double netTotal = rsGroupsData.getDouble(6); //netTotal

	    if (mapDateWiseGroupNames.containsKey(billDate))
	    {
		Map<String, String> mapGroupNames = mapDateWiseGroupNames.get(billDate);

		mapGroupNames.put(groupCode, groupName);
	    }
	    else
	    {
		Map<String, String> mapGroupNames = new TreeMap<>();

		mapGroupNames.put(groupCode, groupName);

		mapDateWiseGroupNames.put(billDate, mapGroupNames);
	    }

	    if (mapDateWiseData.containsKey(billDate))
	    {
		Map<String, clsManagerReportBean> mapDateWiseSettlementWiseData = mapDateWiseData.get(billDate);

		if (mapDateWiseSettlementWiseData.containsKey(groupCode))
		{
		    clsManagerReportBean objGroupDtl = mapDateWiseSettlementWiseData.get(groupCode);

		    objGroupDtl.setDblSubTotal(objGroupDtl.getDblSubTotal() + subTotal);
		    objGroupDtl.setDblDisAmt(objGroupDtl.getDblDisAmt() + discount);
		    objGroupDtl.setDblNetTotal(objGroupDtl.getDblNetTotal() + netTotal);
		}
		else
		{
		    clsManagerReportBean objGroupDtl = new clsManagerReportBean();
		    objGroupDtl.setStrGroupCode(groupCode);
		    objGroupDtl.setStrGroupName(groupName);
		    objGroupDtl.setDblSubTotal(subTotal);
		    objGroupDtl.setDblDisAmt(discount);
		    objGroupDtl.setDblNetTotal(netTotal);

		    mapDateWiseSettlementWiseData.put(groupCode, objGroupDtl);
		}

		//put total settlement dtl
		if (mapDateWiseSettlementWiseData.containsKey("TotalGroupAmt"))
		{
		    clsManagerReportBean objManagerReportBean = mapDateWiseSettlementWiseData.get("TotalGroupAmt");
		    objManagerReportBean.setDblSubTotal(objManagerReportBean.getDblSubTotal() + subTotal);
		    objManagerReportBean.setDblNetTotal(objManagerReportBean.getDblNetTotal() + netTotal);

		    mapDateWiseSettlementWiseData.put("TotalGroupAmt", objManagerReportBean);
		}
		else
		{
		    clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		    objManagerReportBean.setStrGroupCode("TotalGroupAmt");
		    objManagerReportBean.setStrGroupName("TotalGroupAmt");
		    objManagerReportBean.setDblSubTotal(subTotal);
		    objManagerReportBean.setDblNetTotal(netTotal);

		    mapDateWiseSettlementWiseData.put("TotalGroupAmt", objManagerReportBean);
		}
	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapDateWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();

		clsManagerReportBean objGroupDtl = new clsManagerReportBean();
		objGroupDtl.setStrGroupCode(groupCode);
		objGroupDtl.setStrGroupName(groupName);
		objGroupDtl.setDblSubTotal(subTotal);
		objGroupDtl.setDblDisAmt(discount);
		objGroupDtl.setDblNetTotal(netTotal);

		//put total settlement dtl
		clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		objManagerReportBean.setStrGroupCode("TotalGroupAmt");
		objManagerReportBean.setStrGroupName("TotalGroupAmt");
		objManagerReportBean.setDblSubTotal(subTotal);
		objManagerReportBean.setDblNetTotal(netTotal);

		mapDateWiseSettlementWiseData.put("TotalGroupAmt", objManagerReportBean);

		mapDateWiseSettlementWiseData.put(groupCode, objGroupDtl);

		mapDateWiseData.put(billDate, mapDateWiseSettlementWiseData);
	    }
	}
	rsGroupsData.close();

	/**
	 * fill Q modifiers date wise group wise data
	 */
	sqlGroupData.setLength(0);
	sqlGroupData.append("select DATE_FORMAT(date(a.dteBillDate),'%d-%m-%Y'),e.strGroupCode,e.strGroupName,sum(b.dblAmount)SubTotal,sum(b.dblDiscAmt)Discount,sum(b.dblAmount)-sum(b.dblDiscAmt)NetTotal "
		+ "from tblqbillhd a,tblqbillmodifierdtl b,tblitemmaster c,tblsubgrouphd d,tblgrouphd e "
		+ "where a.strBillNo=b.strBillNo "
		+ "AND DATE(a.dteBillDate)= DATE(b.dteBillDate) "
		+ "and left(b.strItemCode,7)=c.strItemCode "
		+ "and c.strSubGroupCode=d.strSubGroupCode "
		+ "and d.strGroupCode=e.strGroupCode "
		+ "and date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ");
	if (!posCode.equalsIgnoreCase("All"))
	{
	    sqlGroupData.append(" and a.strPOSCode='" + posCode + "' ");
	}
	sqlGroupData.append("group by date(a.dteBillDate),e.strGroupCode ");
	rsGroupsData = clsGlobalVarClass.dbMysql.executeResultSet(sqlGroupData.toString());
	while (rsGroupsData.next())
	{
	    String billDate = rsGroupsData.getString(1);//date
	    String groupCode = rsGroupsData.getString(2);//groupCode
	    String groupName = rsGroupsData.getString(3);//groupName
	    double subTotal = rsGroupsData.getDouble(4); //subTotal
	    double discount = rsGroupsData.getDouble(5); //discount
	    double netTotal = rsGroupsData.getDouble(6); //netTotal

	    if (mapDateWiseGroupNames.containsKey(billDate))
	    {
		Map<String, String> mapGroupNames = mapDateWiseGroupNames.get(billDate);

		mapGroupNames.put(groupCode, groupName);
	    }
	    else
	    {
		Map<String, String> mapGroupNames = new TreeMap<>();

		mapGroupNames.put(groupCode, groupName);

		mapDateWiseGroupNames.put(billDate, mapGroupNames);
	    }

	    if (mapDateWiseData.containsKey(billDate))
	    {
		Map<String, clsManagerReportBean> mapDateWiseSettlementWiseData = mapDateWiseData.get(billDate);

		if (mapDateWiseSettlementWiseData.containsKey(groupCode))
		{
		    clsManagerReportBean objGroupDtl = mapDateWiseSettlementWiseData.get(groupCode);

		    objGroupDtl.setDblSubTotal(objGroupDtl.getDblSubTotal() + subTotal);
		    objGroupDtl.setDblDisAmt(objGroupDtl.getDblDisAmt() + discount);
		    objGroupDtl.setDblNetTotal(objGroupDtl.getDblNetTotal() + netTotal);
		}
		else
		{
		    clsManagerReportBean objGroupDtl = new clsManagerReportBean();
		    objGroupDtl.setStrGroupCode(groupCode);
		    objGroupDtl.setStrGroupName(groupName);
		    objGroupDtl.setDblSubTotal(subTotal);
		    objGroupDtl.setDblDisAmt(discount);
		    objGroupDtl.setDblNetTotal(netTotal);

		    mapDateWiseSettlementWiseData.put(groupCode, objGroupDtl);
		}

		//put total settlement dtl
		if (mapDateWiseSettlementWiseData.containsKey("TotalGroupAmt"))
		{
		    clsManagerReportBean objManagerReportBean = mapDateWiseSettlementWiseData.get("TotalGroupAmt");
		    objManagerReportBean.setDblSubTotal(objManagerReportBean.getDblSubTotal() + subTotal);
		    objManagerReportBean.setDblNetTotal(objManagerReportBean.getDblNetTotal() + netTotal);

		    mapDateWiseSettlementWiseData.put("TotalGroupAmt", objManagerReportBean);
		}
		else
		{
		    clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		    objManagerReportBean.setStrGroupCode("TotalGroupAmt");
		    objManagerReportBean.setStrGroupName("TotalGroupAmt");
		    objManagerReportBean.setDblSubTotal(subTotal);
		    objManagerReportBean.setDblNetTotal(netTotal);

		    mapDateWiseSettlementWiseData.put("TotalGroupAmt", objManagerReportBean);
		}
	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapDateWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();

		clsManagerReportBean objGroupDtl = new clsManagerReportBean();
		objGroupDtl.setStrGroupCode(groupCode);
		objGroupDtl.setStrGroupName(groupName);
		objGroupDtl.setDblSubTotal(subTotal);
		objGroupDtl.setDblDisAmt(discount);
		objGroupDtl.setDblNetTotal(netTotal);

		//put total settlement dtl
		clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		objManagerReportBean.setStrGroupCode("TotalGroupAmt");
		objManagerReportBean.setStrGroupName("TotalGroupAmt");
		objManagerReportBean.setDblSubTotal(subTotal);
		objManagerReportBean.setDblNetTotal(netTotal);

		mapDateWiseSettlementWiseData.put("TotalGroupAmt", objManagerReportBean);

		mapDateWiseSettlementWiseData.put(groupCode, objGroupDtl);

		mapDateWiseData.put(billDate, mapDateWiseSettlementWiseData);
	    }
	}
	rsGroupsData.close();

	/**
	 * start new logic
	 */
	StringBuilder sqlBillWiseGroupBuilder = new StringBuilder();
	StringBuilder sqlBillWiseSettlementBuilder = new StringBuilder();
	StringBuilder sqlBillWiseTaxBuilder = new StringBuilder();

	Map<String, Map<String, clsManagerReportBean>> mapBillWiseGroupBuilder = new HashMap<>();
	Map<String, Map<String, clsManagerReportBean>> mapBillWiseSettlementBuilder = new HashMap<>();
	Map<String, Map<String, clsManagerReportBean>> mapBillWiseTaxBuilder = new HashMap<>();

	//live bills
	sqlBillWiseGroupBuilder.setLength(0);
	sqlBillWiseGroupBuilder.append("SELECT a.strBillNo,DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y'),e.strGroupCode,e.strGroupName"
		+ ", SUM(b.dblAmount)SubTotal, SUM(b.dblDiscountAmt)Discount, SUM(b.dblAmount)- SUM(b.dblDiscountAmt)NetTotal,sum(b.dblTaxAmount) "
		+ "FROM tblbillhd a,tblbilldtl b,tblitemmaster c,tblsubgrouphd d,tblgrouphd e\n"
		+ "WHERE a.strBillNo=b.strBillNo \n"
		+ "AND DATE(a.dteBillDate)= DATE(b.dteBillDate) \n"
		+ "AND b.strItemCode=c.strItemCode \n"
		+ "AND c.strSubGroupCode=d.strSubGroupCode \n"
		+ "AND d.strGroupCode=e.strGroupCode \n"
		+ "AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' ");
	if (!posCode.equalsIgnoreCase("All"))
	{
	    sqlBillWiseGroupBuilder.append("AND a.strPOSCode='" + posCode + "' ");
	}
	sqlBillWiseGroupBuilder.append("GROUP BY a.strBillNo,DATE(a.dteBillDate),e.strGroupCode ");

	ResultSet rsBillWiseGroupBuilder = clsGlobalVarClass.dbMysql.executeResultSet(sqlBillWiseGroupBuilder.toString());
	while (rsBillWiseGroupBuilder.next())
	{
	    String billNo = rsBillWiseGroupBuilder.getString(1);//billNo
	    String billDate = rsBillWiseGroupBuilder.getString(2);//date
	    String groupCode = rsBillWiseGroupBuilder.getString(3);//groupCode
	    String groupName = rsBillWiseGroupBuilder.getString(4);//groupName
	    double subTotal = rsBillWiseGroupBuilder.getDouble(5); //subTotal
	    double discount = rsBillWiseGroupBuilder.getDouble(6); //discount
	    double netTotal = rsBillWiseGroupBuilder.getDouble(7); //netTotal
	    double taxTotal = rsBillWiseGroupBuilder.getDouble(8); //taxTotal

	    String billNoBillDateKey = billNo + "!" + billDate;
	    String billNoBillDateAllGroupKey = billNo + "!" + billDate + "!" + "All";

	    if (mapBillWiseGroupBuilder.containsKey(billNoBillDateKey))
	    {
		Map<String, clsManagerReportBean> mapGroupDtl = mapBillWiseGroupBuilder.get(billNoBillDateKey);
		if (mapGroupDtl.containsKey(groupCode))
		{
		    clsManagerReportBean objGroupDtl = mapGroupDtl.get(groupCode);

		    objGroupDtl.setDblSubTotal(objGroupDtl.getDblSubTotal() + subTotal);
		    objGroupDtl.setDblDisAmt(objGroupDtl.getDblDisAmt() + discount);
		    objGroupDtl.setDblNetTotal(objGroupDtl.getDblNetTotal() + netTotal);
		    objGroupDtl.setDblTaxAmt(objGroupDtl.getDblTaxAmt() + taxTotal);
		}
		else
		{
		    clsManagerReportBean objGroupDtl = new clsManagerReportBean();
		    objGroupDtl.setDblSubTotal(subTotal);
		    objGroupDtl.setDblDisAmt(discount);
		    objGroupDtl.setDblNetTotal(netTotal);
		    objGroupDtl.setDblTaxAmt(taxTotal);

		    mapGroupDtl.put(groupCode, objGroupDtl);
		}

		if (mapGroupDtl.containsKey("All"))
		{
		    clsManagerReportBean objGroupDtl = mapGroupDtl.get("All");

		    objGroupDtl.setDblSubTotal(objGroupDtl.getDblSubTotal() + subTotal);
		    objGroupDtl.setDblDisAmt(objGroupDtl.getDblDisAmt() + discount);
		    objGroupDtl.setDblNetTotal(objGroupDtl.getDblNetTotal() + netTotal);
		    objGroupDtl.setDblTaxAmt(objGroupDtl.getDblTaxAmt() + taxTotal);
		}
		else
		{
		    clsManagerReportBean objGroupDtl = new clsManagerReportBean();
		    objGroupDtl.setDblSubTotal(subTotal);
		    objGroupDtl.setDblDisAmt(discount);
		    objGroupDtl.setDblNetTotal(netTotal);
		    objGroupDtl.setDblTaxAmt(taxTotal);

		    mapGroupDtl.put("All", objGroupDtl);
		}

	    }
	    else
	    {

		Map<String, clsManagerReportBean> mapGroupDtl = new HashMap<>();

		clsManagerReportBean objGroupDtl = new clsManagerReportBean();

		objGroupDtl.setStrBillNo(billNo);
		objGroupDtl.setDteBill(billDate);
		objGroupDtl.setStrGroupCode(groupCode);
		objGroupDtl.setStrGroupName(groupName);
		objGroupDtl.setDblSubTotal(subTotal);
		objGroupDtl.setDblDisAmt(discount);
		objGroupDtl.setDblNetTotal(netTotal);
		objGroupDtl.setDblTaxAmt(taxTotal);

		mapGroupDtl.put(groupCode, objGroupDtl);

		objGroupDtl = new clsManagerReportBean();

		objGroupDtl.setStrBillNo(billNo);
		objGroupDtl.setDteBill(billDate);
		objGroupDtl.setStrGroupCode("All");
		objGroupDtl.setStrGroupName("All");
		objGroupDtl.setDblSubTotal(subTotal);
		objGroupDtl.setDblDisAmt(discount);
		objGroupDtl.setDblNetTotal(netTotal);
		objGroupDtl.setDblTaxAmt(objGroupDtl.getDblTaxAmt() + taxTotal);

		mapGroupDtl.put("All", objGroupDtl);

		mapBillWiseGroupBuilder.put(billNoBillDateKey, mapGroupDtl);
	    }
	}
	rsBillWiseGroupBuilder.close();

	//live bill modifires
	sqlBillWiseGroupBuilder.setLength(0);
	sqlBillWiseGroupBuilder.append("SELECT a.strBillNo,DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y'),e.strGroupCode,e.strGroupName, SUM(b.dblAmount)SubTotal, SUM(b.dblDiscAmt)Discount, SUM(b.dblAmount)- SUM(b.dblDiscAmt)NetTotal\n"
		+ "FROM tblbillhd a,tblbillmodifierdtl b,tblitemmaster c,tblsubgrouphd d,tblgrouphd e\n"
		+ "WHERE a.strBillNo=b.strBillNo \n"
		+ "AND DATE(a.dteBillDate)= DATE(b.dteBillDate) \n"
		+ "AND left(b.strItemCode,7)=c.strItemCode \n"
		+ "AND c.strSubGroupCode=d.strSubGroupCode \n"
		+ "AND d.strGroupCode=e.strGroupCode "
		+ "AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' ");
	if (!posCode.equalsIgnoreCase("All"))
	{
	    sqlBillWiseGroupBuilder.append("AND a.strPOSCode='" + posCode + "' ");
	}
	sqlBillWiseGroupBuilder.append("GROUP BY a.strBillNo,DATE(a.dteBillDate),e.strGroupCode ");

	rsBillWiseGroupBuilder = clsGlobalVarClass.dbMysql.executeResultSet(sqlBillWiseGroupBuilder.toString());
	while (rsBillWiseGroupBuilder.next())
	{
	    String billNo = rsBillWiseGroupBuilder.getString(1);//billNo
	    String billDate = rsBillWiseGroupBuilder.getString(2);//date
	    String groupCode = rsBillWiseGroupBuilder.getString(3);//groupCode
	    String groupName = rsBillWiseGroupBuilder.getString(4);//groupName
	    double subTotal = rsBillWiseGroupBuilder.getDouble(5); //subTotal
	    double discount = rsBillWiseGroupBuilder.getDouble(6); //discount
	    double netTotal = rsBillWiseGroupBuilder.getDouble(7); //netTotal

	    String billNoBillDateKey = billNo + "!" + billDate;
	    String billNoBillDateAllGroupKey = billNo + "!" + billDate + "!" + "All";

	    if (mapBillWiseGroupBuilder.containsKey(billNoBillDateKey))
	    {
		Map<String, clsManagerReportBean> mapGroupDtl = mapBillWiseGroupBuilder.get(billNoBillDateKey);
		if (mapGroupDtl.containsKey(groupCode))
		{
		    clsManagerReportBean objGroupDtl = mapGroupDtl.get(groupCode);

		    objGroupDtl.setDblSubTotal(objGroupDtl.getDblSubTotal() + subTotal);
		    objGroupDtl.setDblDisAmt(objGroupDtl.getDblDisAmt() + discount);
		    objGroupDtl.setDblNetTotal(objGroupDtl.getDblNetTotal() + netTotal);
		}
		else
		{
		    clsManagerReportBean objGroupDtl = new clsManagerReportBean();
		    objGroupDtl.setDblSubTotal(subTotal);
		    objGroupDtl.setDblDisAmt(discount);
		    objGroupDtl.setDblNetTotal(netTotal);

		    mapGroupDtl.put(groupCode, objGroupDtl);
		}

		if (mapGroupDtl.containsKey("All"))
		{
		    clsManagerReportBean objGroupDtl = mapGroupDtl.get("All");

		    objGroupDtl.setDblSubTotal(objGroupDtl.getDblSubTotal() + subTotal);
		    objGroupDtl.setDblDisAmt(objGroupDtl.getDblDisAmt() + discount);
		    objGroupDtl.setDblNetTotal(objGroupDtl.getDblNetTotal() + netTotal);
		}
		else
		{
		    clsManagerReportBean objGroupDtl = new clsManagerReportBean();
		    objGroupDtl.setDblSubTotal(subTotal);
		    objGroupDtl.setDblDisAmt(discount);
		    objGroupDtl.setDblNetTotal(netTotal);

		    mapGroupDtl.put("All", objGroupDtl);
		}

	    }
	    else
	    {

		Map<String, clsManagerReportBean> mapGroupDtl = new HashMap<>();

		clsManagerReportBean objGroupDtl = new clsManagerReportBean();

		objGroupDtl.setStrBillNo(billNo);
		objGroupDtl.setDteBill(billDate);
		objGroupDtl.setStrGroupCode(groupCode);
		objGroupDtl.setStrGroupName(groupName);
		objGroupDtl.setDblSubTotal(subTotal);
		objGroupDtl.setDblDisAmt(discount);
		objGroupDtl.setDblNetTotal(netTotal);

		mapGroupDtl.put(groupCode, objGroupDtl);

		objGroupDtl = new clsManagerReportBean();

		objGroupDtl.setStrBillNo(billNo);
		objGroupDtl.setDteBill(billDate);
		objGroupDtl.setStrGroupCode("All");
		objGroupDtl.setStrGroupName("All");
		objGroupDtl.setDblSubTotal(subTotal);
		objGroupDtl.setDblDisAmt(discount);
		objGroupDtl.setDblNetTotal(netTotal);

		mapGroupDtl.put("All", objGroupDtl);

		mapBillWiseGroupBuilder.put(billNoBillDateKey, mapGroupDtl);
	    }
	}
	rsBillWiseGroupBuilder.close();

	//Q bills
	sqlBillWiseGroupBuilder.setLength(0);
	sqlBillWiseGroupBuilder.append("SELECT a.strBillNo,DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y'),e.strGroupCode,e.strGroupName"
		+ ", SUM(b.dblAmount)SubTotal, SUM(b.dblDiscountAmt)Discount, SUM(b.dblAmount)- SUM(b.dblDiscountAmt)NetTotal "
		+ ",sum(b.dblTaxAmount) "
		+ "FROM tblqbillhd a,tblqbilldtl b,tblitemmaster c,tblsubgrouphd d,tblgrouphd e\n"
		+ "WHERE a.strBillNo=b.strBillNo \n"
		+ "AND DATE(a.dteBillDate)= DATE(b.dteBillDate) \n"
		+ "AND b.strItemCode=c.strItemCode \n"
		+ "AND c.strSubGroupCode=d.strSubGroupCode \n"
		+ "AND d.strGroupCode=e.strGroupCode \n"
		+ "AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' ");
	if (!posCode.equalsIgnoreCase("All"))
	{
	    sqlBillWiseGroupBuilder.append("AND a.strPOSCode='" + posCode + "' ");
	}
	sqlBillWiseGroupBuilder.append("GROUP BY a.strBillNo,DATE(a.dteBillDate),e.strGroupCode ");

	rsBillWiseGroupBuilder = clsGlobalVarClass.dbMysql.executeResultSet(sqlBillWiseGroupBuilder.toString());
	while (rsBillWiseGroupBuilder.next())
	{
	    String billNo = rsBillWiseGroupBuilder.getString(1);//billNo
	    String billDate = rsBillWiseGroupBuilder.getString(2);//date
	    String groupCode = rsBillWiseGroupBuilder.getString(3);//groupCode
	    String groupName = rsBillWiseGroupBuilder.getString(4);//groupName
	    double subTotal = rsBillWiseGroupBuilder.getDouble(5); //subTotal
	    double discount = rsBillWiseGroupBuilder.getDouble(6); //discount
	    double netTotal = rsBillWiseGroupBuilder.getDouble(7); //netTotal
	    double taxTotal = rsBillWiseGroupBuilder.getDouble(8); //taxTotal

	    String billNoBillDateKey = billNo + "!" + billDate;
	    String billNoBillDateAllGroupKey = billNo + "!" + billDate + "!" + "All";

	    if (mapBillWiseGroupBuilder.containsKey(billNoBillDateKey))
	    {
		Map<String, clsManagerReportBean> mapGroupDtl = mapBillWiseGroupBuilder.get(billNoBillDateKey);
		if (mapGroupDtl.containsKey(groupCode))
		{
		    clsManagerReportBean objGroupDtl = mapGroupDtl.get(groupCode);

		    objGroupDtl.setDblSubTotal(objGroupDtl.getDblSubTotal() + subTotal);
		    objGroupDtl.setDblDisAmt(objGroupDtl.getDblDisAmt() + discount);
		    objGroupDtl.setDblNetTotal(objGroupDtl.getDblNetTotal() + netTotal);
		    objGroupDtl.setDblTaxAmt(objGroupDtl.getDblTaxAmt() + taxTotal);
		}
		else
		{
		    clsManagerReportBean objGroupDtl = new clsManagerReportBean();
		    objGroupDtl.setDblSubTotal(subTotal);
		    objGroupDtl.setDblDisAmt(discount);
		    objGroupDtl.setDblNetTotal(netTotal);
		    objGroupDtl.setDblTaxAmt(taxTotal);

		    mapGroupDtl.put(groupCode, objGroupDtl);
		}

		if (mapGroupDtl.containsKey("All"))
		{
		    clsManagerReportBean objGroupDtl = mapGroupDtl.get("All");

		    objGroupDtl.setDblSubTotal(objGroupDtl.getDblSubTotal() + subTotal);
		    objGroupDtl.setDblDisAmt(objGroupDtl.getDblDisAmt() + discount);
		    objGroupDtl.setDblNetTotal(objGroupDtl.getDblNetTotal() + netTotal);
		    objGroupDtl.setDblTaxAmt(objGroupDtl.getDblTaxAmt() + taxTotal);
		}
		else
		{
		    clsManagerReportBean objGroupDtl = new clsManagerReportBean();
		    objGroupDtl.setDblSubTotal(subTotal);
		    objGroupDtl.setDblDisAmt(discount);
		    objGroupDtl.setDblNetTotal(netTotal);
		    objGroupDtl.setDblTaxAmt(taxTotal);

		    mapGroupDtl.put("All", objGroupDtl);
		}

	    }
	    else
	    {

		Map<String, clsManagerReportBean> mapGroupDtl = new HashMap<>();

		clsManagerReportBean objGroupDtl = new clsManagerReportBean();

		objGroupDtl.setStrBillNo(billNo);
		objGroupDtl.setDteBill(billDate);
		objGroupDtl.setStrGroupCode(groupCode);
		objGroupDtl.setStrGroupName(groupName);
		objGroupDtl.setDblSubTotal(subTotal);
		objGroupDtl.setDblDisAmt(discount);
		objGroupDtl.setDblNetTotal(netTotal);
		objGroupDtl.setDblTaxAmt(taxTotal);

		mapGroupDtl.put(groupCode, objGroupDtl);

		objGroupDtl = new clsManagerReportBean();

		objGroupDtl.setStrBillNo(billNo);
		objGroupDtl.setDteBill(billDate);
		objGroupDtl.setStrGroupCode("All");
		objGroupDtl.setStrGroupName("All");
		objGroupDtl.setDblSubTotal(subTotal);
		objGroupDtl.setDblDisAmt(discount);
		objGroupDtl.setDblNetTotal(netTotal);
		objGroupDtl.setDblTaxAmt(objGroupDtl.getDblTaxAmt() + taxTotal);

		mapGroupDtl.put("All", objGroupDtl);

		mapBillWiseGroupBuilder.put(billNoBillDateKey, mapGroupDtl);
	    }

	}
	rsBillWiseGroupBuilder.close();

	//Q bill modifires
	sqlBillWiseGroupBuilder.setLength(0);
	sqlBillWiseGroupBuilder.append("SELECT a.strBillNo,DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y'),e.strGroupCode,e.strGroupName, SUM(b.dblAmount)SubTotal, SUM(b.dblDiscAmt)Discount, SUM(b.dblAmount)- SUM(b.dblDiscAmt)NetTotal\n"
		+ "FROM tblqbillhd a,tblqbillmodifierdtl b,tblitemmaster c,tblsubgrouphd d,tblgrouphd e\n"
		+ "WHERE a.strBillNo=b.strBillNo \n"
		+ "AND DATE(a.dteBillDate)= DATE(b.dteBillDate) \n"
		+ "AND left(b.strItemCode,7)=c.strItemCode \n"
		+ "AND c.strSubGroupCode=d.strSubGroupCode \n"
		+ "AND d.strGroupCode=e.strGroupCode "
		+ "AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' ");
	if (!posCode.equalsIgnoreCase("All"))
	{
	    sqlBillWiseGroupBuilder.append("AND a.strPOSCode='" + posCode + "' ");
	}
	sqlBillWiseGroupBuilder.append("GROUP BY a.strBillNo,DATE(a.dteBillDate),e.strGroupCode ");

	rsBillWiseGroupBuilder = clsGlobalVarClass.dbMysql.executeResultSet(sqlBillWiseGroupBuilder.toString());
	while (rsBillWiseGroupBuilder.next())
	{
	    String billNo = rsBillWiseGroupBuilder.getString(1);//billNo
	    String billDate = rsBillWiseGroupBuilder.getString(2);//date
	    String groupCode = rsBillWiseGroupBuilder.getString(3);//groupCode
	    String groupName = rsBillWiseGroupBuilder.getString(4);//groupName
	    double subTotal = rsBillWiseGroupBuilder.getDouble(5); //subTotal
	    double discount = rsBillWiseGroupBuilder.getDouble(6); //discount
	    double netTotal = rsBillWiseGroupBuilder.getDouble(7); //netTotal

	    String billNoBillDateKey = billNo + "!" + billDate;
	    String billNoBillDateAllGroupKey = billNo + "!" + billDate + "!" + "All";

	    if (mapBillWiseGroupBuilder.containsKey(billNoBillDateKey))
	    {
		Map<String, clsManagerReportBean> mapGroupDtl = mapBillWiseGroupBuilder.get(billNoBillDateKey);
		if (mapGroupDtl.containsKey(groupCode))
		{
		    clsManagerReportBean objGroupDtl = mapGroupDtl.get(groupCode);

		    objGroupDtl.setDblSubTotal(objGroupDtl.getDblSubTotal() + subTotal);
		    objGroupDtl.setDblDisAmt(objGroupDtl.getDblDisAmt() + discount);
		    objGroupDtl.setDblNetTotal(objGroupDtl.getDblNetTotal() + netTotal);
		}
		else
		{
		    clsManagerReportBean objGroupDtl = new clsManagerReportBean();
		    objGroupDtl.setDblSubTotal(subTotal);
		    objGroupDtl.setDblDisAmt(discount);
		    objGroupDtl.setDblNetTotal(netTotal);

		    mapGroupDtl.put(groupCode, objGroupDtl);
		}

		if (mapGroupDtl.containsKey("All"))
		{
		    clsManagerReportBean objGroupDtl = mapGroupDtl.get("All");

		    objGroupDtl.setDblSubTotal(objGroupDtl.getDblSubTotal() + subTotal);
		    objGroupDtl.setDblDisAmt(objGroupDtl.getDblDisAmt() + discount);
		    objGroupDtl.setDblNetTotal(objGroupDtl.getDblNetTotal() + netTotal);
		}
		else
		{
		    clsManagerReportBean objGroupDtl = new clsManagerReportBean();
		    objGroupDtl.setDblSubTotal(subTotal);
		    objGroupDtl.setDblDisAmt(discount);
		    objGroupDtl.setDblNetTotal(netTotal);

		    mapGroupDtl.put("All", objGroupDtl);
		}

	    }
	    else
	    {

		Map<String, clsManagerReportBean> mapGroupDtl = new HashMap<>();

		clsManagerReportBean objGroupDtl = new clsManagerReportBean();

		objGroupDtl.setStrBillNo(billNo);
		objGroupDtl.setDteBill(billDate);
		objGroupDtl.setStrGroupCode(groupCode);
		objGroupDtl.setStrGroupName(groupName);
		objGroupDtl.setDblSubTotal(subTotal);
		objGroupDtl.setDblDisAmt(discount);
		objGroupDtl.setDblNetTotal(netTotal);

		mapGroupDtl.put(groupCode, objGroupDtl);

		objGroupDtl = new clsManagerReportBean();

		objGroupDtl.setStrBillNo(billNo);
		objGroupDtl.setDteBill(billDate);
		objGroupDtl.setStrGroupCode("All");
		objGroupDtl.setStrGroupName("All");
		objGroupDtl.setDblSubTotal(subTotal);
		objGroupDtl.setDblDisAmt(discount);
		objGroupDtl.setDblNetTotal(netTotal);

		mapGroupDtl.put("All", objGroupDtl);

		mapBillWiseGroupBuilder.put(billNoBillDateKey, mapGroupDtl);
	    }

	}
	rsBillWiseGroupBuilder.close();

	//live settlement
	sqlBillWiseSettlementBuilder.setLength(0);
	sqlBillWiseSettlementBuilder.append("SELECT a.strBillNo,c.strSettelmentCode,c.strSettelmentDesc, SUM(b.dblSettlementAmt), DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y')dteBillDate\n"
		+ "FROM tblbillhd a,tblbillsettlementdtl b,tblsettelmenthd c\n"
		+ "WHERE a.strBillNo=b.strBillNo \n"
		+ "AND DATE(a.dteBillDate)= DATE(b.dteBillDate) \n"
		+ "AND b.strSettlementCode=c.strSettelmentCode \n"
		+ "AND a.strClientCode=b.strClientCode \n"
		+ "AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' \n"
		+ "AND c.strSettelmentType!='Complementary' \n");
	if (!posCode.equalsIgnoreCase("All"))
	{
	    sqlBillWiseSettlementBuilder.append("AND a.strPOSCode='" + posCode + "'");
	}
	sqlBillWiseSettlementBuilder.append("GROUP BY a.strBillNo,DATE(a.dteBillDate),c.strSettelmentDesc\n"
		+ "ORDER BY a.strBillNo,DATE(a.dteBillDate),c.strSettelmentDesc ");
	ResultSet rsBillWiseSettlement = clsGlobalVarClass.dbMysql.executeResultSet(sqlBillWiseSettlementBuilder.toString());
	while (rsBillWiseSettlement.next())
	{
	    String billNo = rsBillWiseSettlement.getString(1);//billNo
	    String settlementCode = rsBillWiseSettlement.getString(2);//settleCode
	    String settlementDesc = rsBillWiseSettlement.getString(3);//sett name
	    double settleAmt = rsBillWiseSettlement.getDouble(4);//sett amt
	    String billDate = rsBillWiseSettlement.getString(5);//bill date

	    String billNoBillDateKey = billNo + "!" + billDate;
	    String billNoBillDateAllSettleKey = billNo + "!" + billDate + "!" + "All";

	    //bill wise settlement wise
	    if (mapBillWiseSettlementBuilder.containsKey(billNoBillDateKey))
	    {
		Map<String, clsManagerReportBean> mapSettlement = mapBillWiseSettlementBuilder.get(billNoBillDateKey);

		if (mapSettlement.containsKey(settlementCode))
		{
		    clsManagerReportBean objManagerReportBean = mapSettlement.get(settlementCode);

		    objManagerReportBean.setDblSettlementAmt(objManagerReportBean.getDblSettlementAmt() + settleAmt);
		}
		else
		{
		    clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		    objManagerReportBean.setStrSettlementCode(settlementCode);
		    objManagerReportBean.setStrSettlementDesc(settlementDesc);
		    objManagerReportBean.setDblSettlementAmt(settleAmt);
		    objManagerReportBean.setStrBillNo(billNo);
		    objManagerReportBean.setDteBill(billDate);

		    mapSettlement.put(settlementCode, objManagerReportBean);
		}

		if (mapSettlement.containsKey("All"))
		{
		    clsManagerReportBean objManagerReportBean = mapSettlement.get("All");

		    objManagerReportBean.setDblSettlementAmt(objManagerReportBean.getDblSettlementAmt() + settleAmt);
		}
		else
		{
		    clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		    objManagerReportBean.setStrSettlementCode("All");
		    objManagerReportBean.setStrSettlementDesc("All");
		    objManagerReportBean.setDblSettlementAmt(settleAmt);
		    objManagerReportBean.setStrBillNo(billNo);
		    objManagerReportBean.setDteBill(billDate);

		    mapSettlement.put("All", objManagerReportBean);
		}
	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapSettlement = new HashMap<>();

		clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		objManagerReportBean.setStrSettlementCode(settlementCode);
		objManagerReportBean.setStrSettlementDesc(settlementDesc);
		objManagerReportBean.setDblSettlementAmt(settleAmt);
		objManagerReportBean.setStrBillNo(billNo);
		objManagerReportBean.setDteBill(billDate);

		mapSettlement.put(settlementCode, objManagerReportBean);

		objManagerReportBean = new clsManagerReportBean();
		objManagerReportBean.setStrSettlementCode("All");
		objManagerReportBean.setStrSettlementDesc("All");
		objManagerReportBean.setDblSettlementAmt(settleAmt);
		objManagerReportBean.setStrBillNo(billNo);
		objManagerReportBean.setDteBill(billDate);

		mapSettlement.put("All", objManagerReportBean);

		mapBillWiseSettlementBuilder.put(billNoBillDateKey, mapSettlement);
	    }
	}
	rsBillWiseSettlement.close();

	//Q settlement
	sqlBillWiseSettlementBuilder.setLength(0);
	sqlBillWiseSettlementBuilder.append("SELECT a.strBillNo,c.strSettelmentCode,c.strSettelmentDesc, SUM(b.dblSettlementAmt), DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y')dteBillDate\n"
		+ "FROM tblqbillhd a,tblqbillsettlementdtl b,tblsettelmenthd c\n"
		+ "WHERE a.strBillNo=b.strBillNo \n"
		+ "AND DATE(a.dteBillDate)= DATE(b.dteBillDate) \n"
		+ "AND b.strSettlementCode=c.strSettelmentCode \n"
		+ "AND a.strClientCode=b.strClientCode \n"
		+ "AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' \n"
		+ "AND c.strSettelmentType!='Complementary' \n");
	if (!posCode.equalsIgnoreCase("All"))
	{
	    sqlBillWiseSettlementBuilder.append("AND a.strPOSCode='" + posCode + "'");
	}
	sqlBillWiseSettlementBuilder.append("GROUP BY a.strBillNo,DATE(a.dteBillDate),c.strSettelmentDesc\n"
		+ "ORDER BY a.strBillNo,DATE(a.dteBillDate),c.strSettelmentDesc ");
	rsBillWiseSettlement = clsGlobalVarClass.dbMysql.executeResultSet(sqlBillWiseSettlementBuilder.toString());
	while (rsBillWiseSettlement.next())
	{
	    String billNo = rsBillWiseSettlement.getString(1);//billNo
	    String settlementCode = rsBillWiseSettlement.getString(2);//settleCode
	    String settlementDesc = rsBillWiseSettlement.getString(3);//sett name
	    double settleAmt = rsBillWiseSettlement.getDouble(4);//sett amt
	    String billDate = rsBillWiseSettlement.getString(5);//bill date

	    String billNoBillDateKey = billNo + "!" + billDate;
	    String billNoBillDateAllSettleKey = billNo + "!" + billDate + "!" + "All";

	    //bill wise settlement wise
	    if (mapBillWiseSettlementBuilder.containsKey(billNoBillDateKey))
	    {
		Map<String, clsManagerReportBean> mapSettlement = mapBillWiseSettlementBuilder.get(billNoBillDateKey);

		if (mapSettlement.containsKey(settlementCode))
		{

		    clsManagerReportBean objManagerReportBean = mapSettlement.get(settlementCode);

		    objManagerReportBean.setDblSettlementAmt(objManagerReportBean.getDblSettlementAmt() + settleAmt);
		}
		else
		{
		    clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		    objManagerReportBean.setStrSettlementCode(settlementCode);
		    objManagerReportBean.setStrSettlementDesc(settlementDesc);
		    objManagerReportBean.setDblSettlementAmt(settleAmt);
		    objManagerReportBean.setStrBillNo(billNo);
		    objManagerReportBean.setDteBill(billDate);

		    mapSettlement.put(settlementCode, objManagerReportBean);
		}

		if (mapSettlement.containsKey("All"))
		{
		    clsManagerReportBean objManagerReportBean = mapSettlement.get("All");

		    objManagerReportBean.setDblSettlementAmt(objManagerReportBean.getDblSettlementAmt() + settleAmt);
		}
		else
		{
		    clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		    objManagerReportBean.setStrSettlementCode("All");
		    objManagerReportBean.setStrSettlementDesc("All");
		    objManagerReportBean.setDblSettlementAmt(settleAmt);
		    objManagerReportBean.setStrBillNo(billNo);
		    objManagerReportBean.setDteBill(billDate);

		    mapSettlement.put("All", objManagerReportBean);
		}
	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapSettlement = new HashMap<>();

		clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		objManagerReportBean.setStrSettlementCode(settlementCode);
		objManagerReportBean.setStrSettlementDesc(settlementDesc);
		objManagerReportBean.setDblSettlementAmt(settleAmt);
		objManagerReportBean.setStrBillNo(billNo);
		objManagerReportBean.setDteBill(billDate);

		mapSettlement.put(settlementCode, objManagerReportBean);

		objManagerReportBean = new clsManagerReportBean();
		objManagerReportBean.setStrSettlementCode("All");
		objManagerReportBean.setStrSettlementDesc("All");
		objManagerReportBean.setDblSettlementAmt(settleAmt);
		objManagerReportBean.setStrBillNo(billNo);
		objManagerReportBean.setDteBill(billDate);

		mapSettlement.put("All", objManagerReportBean);

		mapBillWiseSettlementBuilder.put(billNoBillDateKey, mapSettlement);
	    }
	}
	rsBillWiseSettlement.close();

	//live taxes
	sqlBillWiseTaxBuilder.setLength(0);
	sqlBillWiseTaxBuilder.append("SELECT a.strBillNo,c.strTaxCode,c.strTaxDesc, SUM(b.dblTaxAmount), DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y')dteBillDate "
		+ "FROM tblbillhd a,tblbilltaxdtl b,tbltaxhd c "
		+ "WHERE a.strBillNo=b.strBillNo  "
		+ "AND DATE(a.dteBillDate)= DATE(b.dteBillDate)  "
		+ "AND b.strTaxCode=c.strTaxCode  "
		+ "AND a.strClientCode=b.strClientCode  "
		+ "AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' ");
	if (!posCode.equalsIgnoreCase("All"))
	{
	    sqlBillWiseTaxBuilder.append("AND a.strPOSCode='" + posCode + "'");
	}
	sqlBillWiseTaxBuilder.append("GROUP BY a.strBillNo,DATE(a.dteBillDate),c.strTaxDesc\n"
		+ "ORDER BY a.strBillNo,DATE(a.dteBillDate),c.strTaxDesc ");
	ResultSet rsBillWiseTaxes = clsGlobalVarClass.dbMysql.executeResultSet(sqlBillWiseTaxBuilder.toString());
	while (rsBillWiseTaxes.next())
	{
	    String billNo = rsBillWiseTaxes.getString(1);//billNo
	    String taxCode = rsBillWiseTaxes.getString(2);//taxCode
	    String taxDesc = rsBillWiseTaxes.getString(3);//taxDesc name
	    double taxAmt = rsBillWiseTaxes.getDouble(4);//taxAmt amt
	    String billDate = rsBillWiseTaxes.getString(5);//bill date

	    String billNoBillDateKey = billNo + "!" + billDate;
	    String billNoBillDateAllSettleKey = billNo + "!" + billDate + "!" + "All";

	    //bill wise settlement wise
	    if (mapBillWiseTaxBuilder.containsKey(billNoBillDateKey))
	    {
		Map<String, clsManagerReportBean> mapTaxes = mapBillWiseTaxBuilder.get(billNoBillDateKey);

		if (mapTaxes.containsKey(taxCode))
		{
		    clsManagerReportBean objManagerReportBean = mapTaxes.get(taxCode);

		    objManagerReportBean.setDblSettlementAmt(objManagerReportBean.getDblTaxAmt() + taxAmt);
		}
		else
		{
		    clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		    objManagerReportBean.setStrTaxCode(taxCode);
		    objManagerReportBean.setStrTaxDesc(taxDesc);
		    objManagerReportBean.setDblTaxAmt(taxAmt);
		    objManagerReportBean.setStrBillNo(billNo);
		    objManagerReportBean.setDteBill(billDate);

		    mapTaxes.put(taxCode, objManagerReportBean);
		}

		if (mapTaxes.containsKey("All"))
		{
		    clsManagerReportBean objManagerReportBean = mapTaxes.get("All");

		    objManagerReportBean.setDblSettlementAmt(objManagerReportBean.getDblTaxAmt() + taxAmt);
		}
		else
		{
		    clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		    objManagerReportBean.setStrSettlementCode("All");
		    objManagerReportBean.setStrSettlementDesc("All");
		    objManagerReportBean.setDblTaxAmt(taxAmt);
		    objManagerReportBean.setStrBillNo(billNo);
		    objManagerReportBean.setDteBill(billDate);

		    mapTaxes.put("All", objManagerReportBean);
		}
	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapTaxes = new HashMap<>();

		clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		objManagerReportBean.setStrTaxCode(taxCode);
		objManagerReportBean.setStrTaxDesc(taxDesc);
		objManagerReportBean.setDblTaxAmt(taxAmt);
		objManagerReportBean.setStrBillNo(billNo);
		objManagerReportBean.setDteBill(billDate);

		mapTaxes.put(taxCode, objManagerReportBean);

		objManagerReportBean = new clsManagerReportBean();
		objManagerReportBean.setStrSettlementCode("All");
		objManagerReportBean.setStrSettlementDesc("All");
		objManagerReportBean.setDblSettlementAmt(taxAmt);
		objManagerReportBean.setStrBillNo(billNo);
		objManagerReportBean.setDteBill(billDate);

		mapTaxes.put("All", objManagerReportBean);

		mapBillWiseTaxBuilder.put(billNoBillDateKey, mapTaxes);
	    }
	}
	rsBillWiseTaxes.close();

	//Q taxes
	sqlBillWiseTaxBuilder.setLength(0);
	sqlBillWiseTaxBuilder.append("SELECT a.strBillNo,c.strTaxCode,c.strTaxDesc, SUM(b.dblTaxAmount), DATE_FORMAT(DATE(a.dteBillDate),'%d-%m-%Y')dteBillDate "
		+ "FROM tblqbillhd a,tblqbilltaxdtl b,tbltaxhd c "
		+ "WHERE a.strBillNo=b.strBillNo  "
		+ "AND DATE(a.dteBillDate)= DATE(b.dteBillDate)  "
		+ "AND b.strTaxCode=c.strTaxCode  "
		+ "AND a.strClientCode=b.strClientCode  "
		+ "AND DATE(a.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' ");
	if (!posCode.equalsIgnoreCase("All"))
	{
	    sqlBillWiseTaxBuilder.append("AND a.strPOSCode='" + posCode + "'");
	}
	sqlBillWiseTaxBuilder.append("GROUP BY a.strBillNo,DATE(a.dteBillDate),c.strTaxDesc\n"
		+ "ORDER BY a.strBillNo,DATE(a.dteBillDate),c.strTaxDesc ");
	rsBillWiseTaxes = clsGlobalVarClass.dbMysql.executeResultSet(sqlBillWiseTaxBuilder.toString());
	while (rsBillWiseTaxes.next())
	{
	    String billNo = rsBillWiseTaxes.getString(1);//billNo
	    String taxCode = rsBillWiseTaxes.getString(2);//taxCode
	    String taxDesc = rsBillWiseTaxes.getString(3);//taxDesc name
	    double taxAmt = rsBillWiseTaxes.getDouble(4);//taxAmt amt
	    String billDate = rsBillWiseTaxes.getString(5);//bill date

	    String billNoBillDateKey = billNo + "!" + billDate;
	    String billNoBillDateAllSettleKey = billNo + "!" + billDate + "!" + "All";

	    //bill wise settlement wise
	    if (mapBillWiseTaxBuilder.containsKey(billNoBillDateKey))
	    {
		Map<String, clsManagerReportBean> mapTaxes = mapBillWiseTaxBuilder.get(billNoBillDateKey);

		if (mapTaxes.containsKey(taxCode))
		{
		    clsManagerReportBean objManagerReportBean = mapTaxes.get(taxCode);

		    objManagerReportBean.setDblSettlementAmt(objManagerReportBean.getDblTaxAmt() + taxAmt);
		}
		else
		{
		    clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		    objManagerReportBean.setStrTaxCode(taxCode);
		    objManagerReportBean.setStrTaxDesc(taxDesc);
		    objManagerReportBean.setDblTaxAmt(taxAmt);
		    objManagerReportBean.setStrBillNo(billNo);
		    objManagerReportBean.setDteBill(billDate);

		    mapTaxes.put(taxCode, objManagerReportBean);
		}

		if (mapTaxes.containsKey("All"))
		{
		    clsManagerReportBean objManagerReportBean = mapTaxes.get("All");

		    objManagerReportBean.setDblSettlementAmt(objManagerReportBean.getDblTaxAmt() + taxAmt);
		}
		else
		{
		    clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		    objManagerReportBean.setStrSettlementCode("All");
		    objManagerReportBean.setStrSettlementDesc("All");
		    objManagerReportBean.setDblTaxAmt(taxAmt);
		    objManagerReportBean.setStrBillNo(billNo);
		    objManagerReportBean.setDteBill(billDate);

		    mapTaxes.put("All", objManagerReportBean);
		}
	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapTaxes = new HashMap<>();

		clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		objManagerReportBean.setStrTaxCode(taxCode);
		objManagerReportBean.setStrTaxDesc(taxDesc);
		objManagerReportBean.setDblTaxAmt(taxAmt);
		objManagerReportBean.setStrBillNo(billNo);
		objManagerReportBean.setDteBill(billDate);

		mapTaxes.put(taxCode, objManagerReportBean);

		objManagerReportBean = new clsManagerReportBean();
		objManagerReportBean.setStrSettlementCode("All");
		objManagerReportBean.setStrSettlementDesc("All");
		objManagerReportBean.setDblSettlementAmt(taxAmt);
		objManagerReportBean.setStrBillNo(billNo);
		objManagerReportBean.setDteBill(billDate);

		mapTaxes.put("All", objManagerReportBean);

		mapBillWiseTaxBuilder.put(billNoBillDateKey, mapTaxes);
	    }
	}
	rsBillWiseTaxes.close();

	/**
	 * calculation logic
	 */
	Map<String, Map<String, clsManagerReportBean>> mapDateWiseGroupWiseSettlementData = new HashMap<>();
	Map<String, Map<String, clsManagerReportBean>> mapDateWiseGroupWiseSettlementWiseTaxData = new HashMap<>();

	if (mapDateWiseData.size() > 0)
	{
	    for (Map.Entry<String, Map<String, clsManagerReportBean>> billWiseGroupIterator : mapBillWiseGroupBuilder.entrySet())
	    {
		String billNoBillDateKey = billWiseGroupIterator.getKey();
		String billDate = billNoBillDateKey.split("!")[1];

		Map<String, clsManagerReportBean> mapBillGroups = billWiseGroupIterator.getValue();

		for (Map.Entry<String, clsManagerReportBean> entryOfBillGroups : mapBillGroups.entrySet())
		{
		    String groupCode = entryOfBillGroups.getKey();
		    clsManagerReportBean objGroupDtl = entryOfBillGroups.getValue();

		    clsManagerReportBean objAllGroupsDtl = mapBillGroups.get("All");

		    if (!mapBillWiseSettlementBuilder.containsKey(billNoBillDateKey))
		    {
			//System.out.println("billNoBillDateKey->"+billNoBillDateKey);
			break;
		    }

		    Map<String, clsManagerReportBean> mapSettlementDtl = mapBillWiseSettlementBuilder.get(billNoBillDateKey);
		    for (Map.Entry<String, clsManagerReportBean> entryOfBillSettlements : mapSettlementDtl.entrySet())
		    {
			String settlementCode = entryOfBillSettlements.getKey();
			clsManagerReportBean objSettlementDtl = entryOfBillSettlements.getValue();

			clsManagerReportBean objAllSettlementsDtl = mapSettlementDtl.get("All");

			double settlementAmtForThisGroup = 0;
			if (objAllGroupsDtl.getDblNetTotal() > 0)
			{
			    settlementAmtForThisGroup = (objSettlementDtl.getDblSettlementAmt() / objAllGroupsDtl.getDblNetTotal()) * objGroupDtl.getDblNetTotal();
			}
			double netTotalAmtForThisSettlement = 0;
			if (objAllSettlementsDtl.getDblSettlementAmt() > 0)
			{
			    netTotalAmtForThisSettlement = (objSettlementDtl.getDblSettlementAmt() / objAllSettlementsDtl.getDblSettlementAmt()) * objGroupDtl.getDblNetTotal();
			}

			if (mapBillWiseTaxBuilder.containsKey(billNoBillDateKey))
			{
			    Map<String, clsManagerReportBean> mapTaxes = mapBillWiseTaxBuilder.get(billNoBillDateKey);

			    for (Map.Entry<String, clsManagerReportBean> taxEntry : mapTaxes.entrySet())
			    {
				String taxCode = taxEntry.getKey();
				clsManagerReportBean objTax = taxEntry.getValue();
				double taxAmtForThisSettlement = 0;
				if (objAllGroupsDtl.getDblNetTotal() > 0)
				{
				    taxAmtForThisSettlement = (objTax.getDblTaxAmt() / objAllGroupsDtl.getDblNetTotal()) * netTotalAmtForThisSettlement;
				}

				String groupWiseSettlementTaxKey = groupCode + "!" + settlementCode + "!" + taxCode;
				//filltax data
				if (mapDateWiseGroupWiseSettlementWiseTaxData.containsKey(billDate))
				{
				    Map<String, clsManagerReportBean> mapGroupWiseSettlementTaxData = mapDateWiseGroupWiseSettlementWiseTaxData.get(billDate);
				    if (mapGroupWiseSettlementTaxData.containsKey(groupWiseSettlementTaxKey))
				    {
					clsManagerReportBean objGroupWiseSettlementTaxData = mapGroupWiseSettlementTaxData.get(groupWiseSettlementTaxKey);

					objGroupWiseSettlementTaxData.setDblTaxAmt(objGroupWiseSettlementTaxData.getDblTaxAmt() + taxAmtForThisSettlement);

				    }
				    else
				    {
					clsManagerReportBean objGroupWiseSettlementTaxData = new clsManagerReportBean();
					objGroupWiseSettlementTaxData.setStrGroupCode(groupCode);
					objGroupWiseSettlementTaxData.setStrGroupName(objGroupDtl.getStrGroupName());
					objGroupWiseSettlementTaxData.setStrSettlementCode(settlementCode);
					objGroupWiseSettlementTaxData.setStrSettlementDesc(objSettlementDtl.getStrSettlementDesc());
					objGroupWiseSettlementTaxData.setDblTaxAmt(taxAmtForThisSettlement);
					objGroupWiseSettlementTaxData.setStrTaxDesc(objTax.getStrTaxDesc());

					mapGroupWiseSettlementTaxData.put(groupWiseSettlementTaxKey, objGroupWiseSettlementTaxData);

					mapDateWiseGroupWiseSettlementWiseTaxData.put(billDate, mapGroupWiseSettlementTaxData);
				    }
				}
				else//no billdate
				{
				    Map<String, clsManagerReportBean> mapGroupWiseSettlementTaxData = new HashMap<>();
				    if (mapGroupWiseSettlementTaxData.containsKey(groupWiseSettlementTaxKey))
				    {
					clsManagerReportBean objGroupWiseSettlementTaxData = mapGroupWiseSettlementTaxData.get(groupWiseSettlementTaxKey);

					objGroupWiseSettlementTaxData.setDblTaxAmt(objGroupWiseSettlementTaxData.getDblTaxAmt() + taxAmtForThisSettlement);

				    }
				    else
				    {
					clsManagerReportBean objGroupWiseSettlementTaxData = new clsManagerReportBean();
					objGroupWiseSettlementTaxData.setStrGroupCode(groupCode);
					objGroupWiseSettlementTaxData.setStrGroupName(objGroupDtl.getStrGroupName());
					objGroupWiseSettlementTaxData.setStrSettlementCode(settlementCode);
					objGroupWiseSettlementTaxData.setStrSettlementDesc(objSettlementDtl.getStrSettlementDesc());
					objGroupWiseSettlementTaxData.setDblTaxAmt(taxAmtForThisSettlement);
					objGroupWiseSettlementTaxData.setStrTaxDesc(objTax.getStrTaxDesc());

					mapGroupWiseSettlementTaxData.put(groupWiseSettlementTaxKey, objGroupWiseSettlementTaxData);

					mapDateWiseGroupWiseSettlementWiseTaxData.put(billDate, mapGroupWiseSettlementTaxData);
				    }
				}
			    }
			}
			//fill settlement data
			if (mapDateWiseGroupWiseSettlementData.containsKey(billDate))
			{
			    Map<String, clsManagerReportBean> mapGroupWiseSettlementData = mapDateWiseGroupWiseSettlementData.get(billDate);

			    String groupWiseSettlementKey = groupCode + "!" + settlementCode;

			    if (mapGroupWiseSettlementData.containsKey(groupWiseSettlementKey))
			    {
				clsManagerReportBean objGroupWiseSettlementData = mapGroupWiseSettlementData.get(groupWiseSettlementKey);

				objGroupWiseSettlementData.setDblGroupSettlementAmt(objGroupWiseSettlementData.getDblGroupSettlementAmt() + settlementAmtForThisGroup);
				objGroupWiseSettlementData.setDblNetTotal(objGroupWiseSettlementData.getDblNetTotal() + netTotalAmtForThisSettlement);

				mapGroupWiseSettlementData.put(groupWiseSettlementKey, objGroupWiseSettlementData);
			    }
			    else
			    {
				clsManagerReportBean objGroupWiseSettlementData = new clsManagerReportBean();
				objGroupWiseSettlementData.setStrGroupCode(groupCode);
				objGroupWiseSettlementData.setStrGroupName(objGroupDtl.getStrGroupName());
				objGroupWiseSettlementData.setStrSettlementCode(settlementCode);
				objGroupWiseSettlementData.setStrSettlementDesc(objSettlementDtl.getStrSettlementDesc());

				objGroupWiseSettlementData.setStrGroupCodeSettlementCode(groupWiseSettlementKey);
				objGroupWiseSettlementData.setDblGroupSettlementAmt(settlementAmtForThisGroup);
				objGroupWiseSettlementData.setDblNetTotal(netTotalAmtForThisSettlement);

				mapGroupWiseSettlementData.put(groupWiseSettlementKey, objGroupWiseSettlementData);
			    }
			}
			else
			{
			    Map<String, clsManagerReportBean> mapGroupWiseSettlementData = new HashMap<>();

			    String groupWiseSettlementKey = groupCode + "!" + settlementCode;

			    clsManagerReportBean objGroupWiseSettlementData = new clsManagerReportBean();
			    objGroupWiseSettlementData.setStrGroupCode(groupCode);
			    objGroupWiseSettlementData.setStrGroupName(objGroupDtl.getStrGroupName());
			    objGroupWiseSettlementData.setStrSettlementCode(settlementCode);
			    objGroupWiseSettlementData.setStrSettlementDesc(objSettlementDtl.getStrSettlementDesc());

			    objGroupWiseSettlementData.setStrGroupCodeSettlementCode(groupWiseSettlementKey);
			    objGroupWiseSettlementData.setDblGroupSettlementAmt(settlementAmtForThisGroup);
			    objGroupWiseSettlementData.setDblNetTotal(netTotalAmtForThisSettlement);

			    mapGroupWiseSettlementData.put(groupWiseSettlementKey, objGroupWiseSettlementData);

			    mapDateWiseGroupWiseSettlementData.put(billDate, mapGroupWiseSettlementData);
			}
		    }
		}
	    }
	}

	/**
	 * end new logic
	 */
	//priting logic
	LinkedHashMap<String, Map<String, Double>> mapGroupWiseConsolidated = new LinkedHashMap<>();

	LinkedHashMap<String, Double> mapGrandTotalOrderData = new LinkedHashMap<>();
	mapGrandTotalOrderData.put("GROUP TOTALS", 0.00);
	int grandTotalLines = 0;
	if (mapDateWiseData.size() > 0)
	{
	    for (Map.Entry<String, Map<String, clsManagerReportBean>> entrySet : mapDateWiseData.entrySet())
	    {
		String billDate = entrySet.getKey();
		Map<String, clsManagerReportBean> mapDateWiseGroupTaxSettlementData = entrySet.getValue();

		clsManagerReportBean objTotalSettlementAmt = mapDateWiseGroupTaxSettlementData.get("TotalSettlementAmt");
		double totalSettlementAmt = objTotalSettlementAmt.getDblSettlementAmt();

		clsManagerReportBean objTotalTaxAmt = mapDateWiseGroupTaxSettlementData.get("TotalTaxAmt");
		double totalTaxAmt = objTotalTaxAmt.getDblTaxAmt();

		clsManagerReportBean objTotalGroupAmt = mapDateWiseGroupTaxSettlementData.get("TotalGroupAmt");
		//double totalGroupSubTotal = objTotalGroupAmt.getDblSubTotal();
		double totalGroupNetTotal = objTotalGroupAmt.getDblNetTotal();

		int maxLineCount = 0;

		String labelSettlement = "SETTLEMENT          |";
		int maxGroupNameLength = 0;
		String horizontalTotalLabel = "  TOTALS   |";

		pw.println();
		pw.print(billDate);

		Map<String, String> mapDateWiseTaxeNames = mapDateWiseTaxNames.get(billDate);

		if (mapDateWiseGroupNames.containsKey(billDate))
		{
		    Map<String, String> mapGroupNames = mapDateWiseGroupNames.get(billDate);
		    for (Map.Entry<String, String> entryGroupNames : mapGroupNames.entrySet())
		    {
			String groupCode = entryGroupNames.getKey();
			String groupName = entryGroupNames.getValue();

			mapAllGroups.put(groupName, groupName);

			if (groupName.length() > maxGroupNameLength)
			{
			    maxGroupNameLength = groupName.length();
			}

			clsManagerReportBean objGroupDtl = mapDateWiseGroupTaxSettlementData.get(groupCode);
			//double groupSubTotal = objGroupDtl.getDblSubTotal();
			double groupNetTotal = objGroupDtl.getDblNetTotal();

			/**
			 * print a line
			 */
			int lineCount = funGetLineCount(billDate, labelSettlement, groupName, horizontalTotalLabel, mapDateWiseData, mapDateWiseSettlementNames, mapDateWiseTaxNames);
			pw.println();
			for (int i = 0; i < lineCount; i++)
			{
			    pw.print("-");
			}
			if (lineCount > maxLineCount)
			{
			    maxLineCount = lineCount;
			}

			/**
			 * print header line
			 */
			pw.println();
			pw.print(objUtility.funPrintTextWithAlignment(labelSettlement, labelSettlement.length(), "Left"));
			pw.print(objUtility.funPrintTextWithAlignment(groupName + "|", groupName.length(), "Left"));

			for (String taxDesc : mapDateWiseTaxeNames.values())
			{
			    String labelTaxDesc = taxDesc + "|";
			    pw.print(objUtility.funPrintTextWithAlignment(labelTaxDesc, labelTaxDesc.length(), "Left"));

			    mapAllTaxes.put(taxDesc, taxDesc);
			}
			pw.print(objUtility.funPrintTextWithAlignment(horizontalTotalLabel, horizontalTotalLabel.length(), "Left"));

			/**
			 * print settlement wise data
			 */
			pw.println();

			Map<String, String> mapSettlementNames = mapDateWiseSettlementNames.get(billDate);
			for (Map.Entry<String, String> entrySettlements : mapSettlementNames.entrySet())
			{
			    String settlementCode = entrySettlements.getKey();
			    String settlementName = entrySettlements.getValue();

			    mapAllSettlements.put(settlementName, settlementName);

			    double horizontalSettlementTotalAmt = 0.00;

			    clsManagerReportBean objSettlementDtl = mapDateWiseGroupTaxSettlementData.get(settlementCode);

			    double groupSubTotalForThisSettlement = 0.00;
			    if (totalSettlementAmt > 0)
			    {
//                                groupSubTotalForThisSettlement = (groupNetTotal / totalSettlementAmt) * objSettlementDtl.getDblSettlementAmt();

				Map<String, clsManagerReportBean> mapGroupSettlementData = mapDateWiseGroupWiseSettlementData.get(billDate);
				if (mapGroupSettlementData.containsKey(groupCode + "!" + settlementCode))
				{
				    clsManagerReportBean objGroupWiseSettlementData = mapGroupSettlementData.get(groupCode + "!" + settlementCode);
				    groupSubTotalForThisSettlement = objGroupWiseSettlementData.getDblNetTotal();
				}
				else
				{
				    groupSubTotalForThisSettlement = 0;
				}
			    }
			    horizontalSettlementTotalAmt += groupSubTotalForThisSettlement;

			    pw.println();
			    pw.print(objUtility.funPrintTextWithAlignment(settlementName, labelSettlement.length(), "Left"));
			    pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(Math.rint(groupSubTotalForThisSettlement) + "|"), groupName.length(), "Right"));

			    /**
			     * setting consolidated Group Wise settlement
			     */
			    if (mapGroupWiseConsolidated.containsKey(groupName))
			    {
				Map<String, Double> mapGroupWiseConsolidatedDtl = mapGroupWiseConsolidated.get(groupName);
				if (mapGroupWiseConsolidatedDtl.containsKey(groupName + "!" + settlementName))
				{
				    double oldAmt = mapGroupWiseConsolidatedDtl.get(groupName + "!" + settlementName);

				    mapGroupWiseConsolidatedDtl.put(groupName + "!" + settlementName, oldAmt + groupSubTotalForThisSettlement);
				}
				else
				{
				    mapGroupWiseConsolidatedDtl.put(groupName + "!" + settlementName, groupSubTotalForThisSettlement);
				}
			    }
			    else
			    {

				Map<String, Double> mapGroupWiseConsolidatedDtl = new LinkedHashMap<>();
				mapGroupWiseConsolidatedDtl.put(groupName + "!" + settlementName, groupSubTotalForThisSettlement);

				mapGroupWiseConsolidated.put(groupName, mapGroupWiseConsolidatedDtl);
			    }

			    for (Map.Entry<String, String> entryTaxNames : mapDateWiseTaxeNames.entrySet())
			    {
				String taxCode = entryTaxNames.getKey();
				String taxName = entryTaxNames.getValue();

				String labelTaxDesc = taxName + "|";

				clsManagerReportBean objTaxDtl = mapDateWiseGroupTaxSettlementData.get(taxCode);
				double taxAmt = objTaxDtl.getDblTaxAmt();

				double taxWiseGroupTotal = funGetTaxWiseGroupTotal(billDate, taxCode, mapDateWiseGroupTaxSettlementData);

				double taxAmtForThisTax = 0.00;
				boolean isTaxApplicableOnGroup = false;

				if (isApplicableTaxOnSettlement(taxCode, settlementCode))
				{
				    isTaxApplicableOnGroup = isApplicableTaxOnGroup(taxCode, groupCode);
				}

//                                if (taxWiseGroupTotal > 0 && isTaxApplicableOnGroup)
//                                {
//                                    taxAmtForThisTax = (taxAmt / taxWiseGroupTotal) * groupSubTotalForThisSettlement;
//                                }
				if (mapDateWiseGroupWiseSettlementWiseTaxData.containsKey(billDate))
				{
				    Map<String, clsManagerReportBean> mapTaxes = mapDateWiseGroupWiseSettlementWiseTaxData.get(billDate);
				    if (mapTaxes.containsKey(groupCode + "!" + settlementCode + "!" + taxCode))
				    {
					clsManagerReportBean objTax = mapTaxes.get(groupCode + "!" + settlementCode + "!" + taxCode);
					taxAmtForThisTax = objTax.getDblTaxAmt();
				    }
				}
				horizontalSettlementTotalAmt += taxAmtForThisTax;
				pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(Math.rint(taxAmtForThisTax)) + "|", labelTaxDesc.length(), "Right"));

				/**
				 * setting consolidated Group Wise Taxes
				 */
				if (mapGroupWiseConsolidated.containsKey(groupName))
				{
				    Map<String, Double> mapGroupWiseConsolidatedDtl = mapGroupWiseConsolidated.get(groupName);
				    if (mapGroupWiseConsolidatedDtl.containsKey(groupName + "!" + settlementName + "!" + taxName))
				    {
					double oldAmt = mapGroupWiseConsolidatedDtl.get(groupName + "!" + settlementName + "!" + taxName);

					mapGroupWiseConsolidatedDtl.put(groupName + "!" + settlementName + "!" + taxName, oldAmt + taxAmtForThisTax);
				    }
				    else
				    {
					mapGroupWiseConsolidatedDtl.put(groupName + "!" + settlementName + "!" + taxName, taxAmtForThisTax);
				    }
				}
				else
				{

				    Map<String, Double> mapGroupWiseConsolidatedDtl = new LinkedHashMap<>();
				    mapGroupWiseConsolidatedDtl.put(groupName + "!" + settlementName + "!" + taxName, taxAmtForThisTax);

				    mapGroupWiseConsolidated.put(groupName, mapGroupWiseConsolidatedDtl);
				}
			    }
			    pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(Math.rint(horizontalSettlementTotalAmt)) + "|", horizontalTotalLabel.length(), "Right"));

			    /**
			     * setting consolidated Group Wise Totals
			     */
			    if (mapGroupWiseConsolidated.containsKey(groupName))
			    {
				Map<String, Double> mapGroupWiseConsolidatedDtl = mapGroupWiseConsolidated.get(groupName);
				if (mapGroupWiseConsolidatedDtl.containsKey(groupName + "!" + settlementName + "!" + "TOTALS"))
				{
				    double oldAmt = mapGroupWiseConsolidatedDtl.get(groupName + "!" + settlementName + "!" + "TOTALS");

				    mapGroupWiseConsolidatedDtl.put(groupName + "!" + settlementName + "!" + "TOTALS", oldAmt + horizontalSettlementTotalAmt);
				}
				else
				{
				    mapGroupWiseConsolidatedDtl.put(groupName + "!" + settlementName + "!" + "TOTALS", horizontalSettlementTotalAmt);
				}
			    }
			    else
			    {

				Map<String, Double> mapGroupWiseConsolidatedDtl = new LinkedHashMap<>();
				mapGroupWiseConsolidatedDtl.put(groupName + "!" + settlementName + "!" + "TOTALS", horizontalSettlementTotalAmt);

				mapGroupWiseConsolidated.put(groupName, mapGroupWiseConsolidatedDtl);
			    }
			}
			/**
			 * print total line
			 */
			pw.println();
			for (int i = 0; i < lineCount; i++)
			{
			    pw.print("-");
			}
			pw.println();

			if (maxLineCount > grandTotalLines)
			{
			    grandTotalLines = maxLineCount;
			}

			double verticleGroupTotalAmt = 0.00;
			pw.print(objUtility.funPrintTextWithAlignment(groupName.toUpperCase() + " TOTALS", labelSettlement.length(), "Left"));
			pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(Math.rint(groupNetTotal)) + "|", groupName.length(), "Right"));

			verticleGroupTotalAmt += groupNetTotal;

			for (Map.Entry<String, String> entryTaxNames : mapDateWiseTaxeNames.entrySet())
			{
			    String taxCode = entryTaxNames.getKey();
			    String taxName = entryTaxNames.getValue();

			    String labelTaxDesc = taxName + "|";
			    double taxAmt = 0.00;

			    boolean isApplicable = isApplicableTaxOnGroup(taxCode, groupCode);
			    if (isApplicable)
			    {
				double taxWiseGroupTotal = funGetTaxWiseGroupTotal(billDate, taxCode, mapDateWiseGroupTaxSettlementData);
				clsManagerReportBean objTaxDtl = mapDateWiseGroupTaxSettlementData.get(taxCode);
				double totalTaxAmtForGroup = objTaxDtl.getDblTaxAmt();

//                                if (taxWiseGroupTotal > 0)
//                                {
//                                    taxAmt = (totalTaxAmtForGroup / taxWiseGroupTotal) * groupNetTotal;
//                                }
				if (mapDateWiseGroupWiseSettlementWiseTaxData.containsKey(billDate))
				{
				    Map<String, clsManagerReportBean> mapTaxes = mapDateWiseGroupWiseSettlementWiseTaxData.get(billDate);
				    if (mapTaxes.containsKey(groupCode + "!All!" + taxCode))
				    {
					clsManagerReportBean objTax = mapTaxes.get(groupCode + "!All!" + taxCode);
					taxAmt = objTax.getDblTaxAmt();
				    }
				}
			    }
			    pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(Math.rint(taxAmt)) + "|", labelTaxDesc.length(), "Right"));

			    verticleGroupTotalAmt += taxAmt;
			}
			pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(Math.rint(verticleGroupTotalAmt)) + "|", horizontalTotalLabel.length(), "Right"));
			pw.println();
			for (int i = 0; i < lineCount; i++)
			{
			    pw.print("-");
			}
			pw.println();
			pw.println();

		    }
		}
		else
		{
		    continue;
		}
		/**
		 * print total line
		 */
		pw.println();
		double verticleDateTotalAmt = 0.00;
		for (int i = 0; i < maxLineCount; i++)
		{
		    pw.print("-");
		}
		pw.println();
		pw.print(objUtility.funPrintTextWithAlignment(billDate + " TOTALS", labelSettlement.length(), "Left"));
		pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(Math.rint(totalGroupNetTotal)) + "|", maxGroupNameLength, "Right"));

		verticleDateTotalAmt += totalGroupNetTotal;

		if (mapGrandTotalOrderData.containsKey("GROUP TOTALS"))
		{
		    double oldNetTotal = mapGrandTotalOrderData.get("GROUP TOTALS");
		    mapGrandTotalOrderData.put("GROUP TOTALS", oldNetTotal + totalGroupNetTotal);
		}

		for (Map.Entry<String, String> entryTaxNames : mapDateWiseTaxeNames.entrySet())
		{
		    String taxCode = entryTaxNames.getKey();
		    String taxName = entryTaxNames.getValue();

		    String labelTaxDesc = "  " + taxName + "|";

		    clsManagerReportBean objTaxDtl = mapDateWiseGroupTaxSettlementData.get(taxCode);
		    double totalTaxAmtForGroup = objTaxDtl.getDblTaxAmt();

		    pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(Math.rint(totalTaxAmtForGroup)) + "|", labelTaxDesc.length(), "Right"));

		    verticleDateTotalAmt += totalTaxAmtForGroup;

		    if (mapGrandTotalOrderData.containsKey(taxName))
		    {
			double oldTaxTotal = mapGrandTotalOrderData.get(taxName);
			mapGrandTotalOrderData.put(taxName, oldTaxTotal + totalTaxAmtForGroup);
		    }
		    else
		    {
			mapGrandTotalOrderData.put(taxName, totalTaxAmtForGroup);
		    }

		}
		pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(Math.rint(verticleDateTotalAmt)) + "|", horizontalTotalLabel.length(), "Right"));
		if (mapGrandTotalOrderData.containsKey("TOTAL SETTLEMENT"))
		{
		    double oldSettleAmt = mapGrandTotalOrderData.get("TOTAL SETTLEMENT");
		    mapGrandTotalOrderData.put("TOTAL SETTLEMENT", oldSettleAmt + verticleDateTotalAmt);
		}
		else
		{
		    mapGrandTotalOrderData.put("TOTAL SETTLEMENT", verticleDateTotalAmt);
		}

		pw.println();
		for (int i = 0; i < maxLineCount; i++)
		{
		    pw.print("-");
		}
		pw.println();
		pw.println();
	    }

	    /**
	     * Print Group Wise Concolidated
	     */
	    String lblGroupWiseConcolidated = "Group Wise Concolidated";
	    pw.println();
	    pw.print(lblGroupWiseConcolidated);
	    pw.println();
	    for (int i = 0; i < lblGroupWiseConcolidated.length(); i++)
	    {
		pw.print("-");
	    }

	    /**
	     * print a line
	     */
	    int lineCount = funGetLineCount("SETTLEMENT          ", "     GROUP NAME|", mapAllTaxes, "     TOTALS|");
	    pw.println();
	    for (int i = 0; i < lineCount; i++)
	    {
		pw.print("-");
	    }
	    pw.println();

	    String lblSettlement = "SETTLEMENT          ", lblGroupName = "     GROUP NAME|", lblHorizontalTotals = "     TOTALS|";

	    LinkedHashMap<String, Double> mapGroupWiseConsolidatedTotals = new LinkedHashMap<>();
	    for (String groupName : mapAllGroups.values())
	    {
		mapGroupWiseConsolidatedTotals.clear();

		pw.println();
		for (int i = 0; i < lineCount; i++)
		{
		    pw.print("-");
		}

		pw.println();
		pw.print(objUtility.funPrintTextWithAlignment(lblSettlement, lblSettlement.length(), "Left"));
		pw.print(objUtility.funPrintTextWithAlignment(groupName + "|", lblGroupName.length(), "Right"));

		for (String taxDesc : mapAllTaxes.values())
		{
		    String labelTaxDesc = "   " + taxDesc + "|";
		    pw.print(objUtility.funPrintTextWithAlignment(labelTaxDesc, labelTaxDesc.length(), "Right"));
		}
		pw.print(objUtility.funPrintTextWithAlignment(lblHorizontalTotals, lblHorizontalTotals.length(), "Right"));

		pw.println();
		for (int i = 0; i < lineCount; i++)
		{
		    pw.print("-");
		}

		pw.println();
		for (String settlementName : mapAllSettlements.values())
		{
		    pw.println();
		    pw.print(objUtility.funPrintTextWithAlignment(settlementName, lblSettlement.length(), "Left"));

		    Map<String, Double> mapGroupWiseConsolidatedDtl = mapGroupWiseConsolidated.get(groupName);

		    double groupSubTotalForThisSettlement = 0;
		    if (mapGroupWiseConsolidatedDtl.containsKey(groupName + "!" + settlementName))
		    {
			groupSubTotalForThisSettlement = mapGroupWiseConsolidatedDtl.get(groupName + "!" + settlementName);
		    }
		    pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(Math.rint(groupSubTotalForThisSettlement) + "|"), lblGroupName.length(), "Right"));

		    if (mapGroupWiseConsolidatedTotals.containsKey(groupName))
		    {
			double oldAmt = mapGroupWiseConsolidatedTotals.get(groupName);
			mapGroupWiseConsolidatedTotals.put(groupName, oldAmt + groupSubTotalForThisSettlement);
		    }
		    else
		    {
			mapGroupWiseConsolidatedTotals.put(groupName, groupSubTotalForThisSettlement);
		    }

		    for (String taxDesc : mapAllTaxes.values())
		    {
			String labelTaxDesc = "   " + taxDesc + "|";
			double taxAmt = 0;
			if (mapGroupWiseConsolidatedDtl.containsKey(groupName + "!" + settlementName + "!" + taxDesc))
			{
			    taxAmt = mapGroupWiseConsolidatedDtl.get(groupName + "!" + settlementName + "!" + taxDesc);
			}
			pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(Math.rint(taxAmt) + "|"), labelTaxDesc.length(), "Right"));

			if (mapGroupWiseConsolidatedTotals.containsKey(taxDesc))
			{
			    double oldAmt = mapGroupWiseConsolidatedTotals.get(taxDesc);
			    mapGroupWiseConsolidatedTotals.put(taxDesc, oldAmt + taxAmt);
			}
			else
			{
			    mapGroupWiseConsolidatedTotals.put(taxDesc, taxAmt);
			}
		    }
		    double totalAmt = 0.00;
		    if (mapGroupWiseConsolidatedDtl.containsKey(groupName + "!" + settlementName + "!" + "TOTALS"))
		    {
			totalAmt = mapGroupWiseConsolidatedDtl.get(groupName + "!" + settlementName + "!" + "TOTALS");
		    }
		    pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(Math.rint(totalAmt) + "|"), lblHorizontalTotals.length(), "Right"));

		    if (mapGroupWiseConsolidatedTotals.containsKey("TOTALSAMOUNT"))
		    {
			double oldAmt = mapGroupWiseConsolidatedTotals.get("TOTALSAMOUNT");
			mapGroupWiseConsolidatedTotals.put("TOTALSAMOUNT", oldAmt + totalAmt);
		    }
		    else
		    {
			mapGroupWiseConsolidatedTotals.put("TOTALSAMOUNT", totalAmt);
		    }
		}
		pw.println();
		for (int i = 0; i < lineCount; i++)
		{
		    pw.print("-");
		}
		pw.println();
		pw.print(objUtility.funPrintTextWithAlignment(groupName + " TOTALS", lblSettlement.length(), "Left"));
		double groupSubTotalForThisSettlement = mapGroupWiseConsolidatedTotals.get(groupName);
		pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(Math.rint(groupSubTotalForThisSettlement) + "|"), lblGroupName.length(), "Right"));
		for (String taxDesc : mapAllTaxes.values())
		{
		    String labelTaxDesc = "   " + taxDesc + "|";

		    double taxAmt = mapGroupWiseConsolidatedTotals.get(taxDesc);
		    pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(Math.rint(taxAmt) + "|"), labelTaxDesc.length(), "Right"));
		}
		double totalAmt = mapGroupWiseConsolidatedTotals.get("TOTALSAMOUNT");
		pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(Math.rint(totalAmt) + "|"), lblHorizontalTotals.length(), "Right"));
		pw.println();
		for (int i = 0; i < lineCount; i++)
		{
		    pw.print("-");
		}

		pw.println();
		pw.println();
		pw.println();
	    }

	    /**
	     * print All Groups Concolidated
	     */
	    pw.println();
	    pw.print("All Groups Consolidated");

	    pw.println();
	    for (int i = 0; i < grandTotalLines; i++)
	    {
		pw.print("-");
	    }
	    pw.println();
	    for (Map.Entry<String, Double> grandTotalsEntry : mapGrandTotalOrderData.entrySet())
	    {
		String label = grandTotalsEntry.getKey();
		pw.print(objUtility.funPrintTextWithAlignment(label + "|", 17, "Right"));
	    }
	    pw.println();
	    for (int i = 0; i < grandTotalLines; i++)
	    {
		pw.print("-");
	    }
	    pw.println();
	    for (Map.Entry<String, Double> grandTotalsEntry : mapGrandTotalOrderData.entrySet())
	    {
		double value = grandTotalsEntry.getValue();
		pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(Math.rint(value)) + "|", 17, "Right"));
	    }

	    pw.println();
	    for (int i = 0; i < grandTotalLines; i++)
	    {
		pw.print("-");
	    }
	    pw.println();
	    pw.println();

	}
	return 1;
    }

    private int funGetLineCount(String billDate, String labelSettlement, String labelGroupName, String horizontalTotalLabel, Map<String, Map<String, clsManagerReportBean>> mapDateWiseData, Map<String, Map<String, String>> mapDateWiseSettlemetNames, Map<String, Map<String, String>> mapDateWiseTaxNames)
    {

	StringBuilder stringBuilder = new StringBuilder();
	stringBuilder.append(labelSettlement);
	stringBuilder.append(labelGroupName);

	Map<String, String> map = mapDateWiseTaxNames.get(billDate);
	for (String taxDesc : map.values())
	{
	    String labelTaxDesc = taxDesc + "|";
	    stringBuilder.append(labelTaxDesc);
	}
	stringBuilder.append(horizontalTotalLabel);

	return stringBuilder.length();
    }

    private boolean isApplicableTaxOnGroup(String taxCode, String groupCode)
    {
	boolean isApplicable = false;
	try
	{
	    String sql = "select a.strTaxCode,a.strGroupCode,a.strGroupName,a.strApplicable "
		    + "from tbltaxongroup a "
		    + "where a.strTaxCode='" + taxCode + "' "
		    + "and a.strGroupCode='" + groupCode + "' "
		    + "and a.strApplicable='true' ";
	    ResultSet rsIsApplicable = clsGlobalVarClass.dbMysql.executeResultSet(sql);
	    if (rsIsApplicable.next())
	    {
		isApplicable = true;
	    }
	    rsIsApplicable.close();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	finally
	{
	    return isApplicable;
	}
    }

    private boolean isApplicableTaxOnSettlement(String taxCode, String settlementCode)
    {
	boolean isApplicable = false;
	try
	{
	    String sql = "select a.strTaxCode,a.strSettlementCode,a.strSettlementName,a.strApplicable "
		    + "from tblsettlementtax a  "
		    + "where a.strTaxCode='" + taxCode + "'  "
		    + "and a.strSettlementCode='" + settlementCode + "'  "
		    + "and a.strApplicable='true' ";
	    ResultSet rsIsApplicable = clsGlobalVarClass.dbMysql.executeResultSet(sql);
	    if (rsIsApplicable.next())
	    {
		isApplicable = true;
	    }
	    rsIsApplicable.close();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	finally
	{
	    return isApplicable;
	}
    }

    private double funGetTaxWiseGroupTotal(String billDate, String taxCode, Map<String, clsManagerReportBean> mapDateWiseGroupTaxSettlementData)
    {
	double taxWiseGroupTotal = 0.00;

	try
	{
	    String sql = "select distinct(b.strGroupCode),b.strGroupName,a.strTaxOnGD "
		    + "from tbltaxhd a,tbltaxongroup b "
		    + "where a.strTaxCode=b.strTaxCode "
		    + "and b.strTaxCode='" + taxCode + "' "
		    + "and b.strApplicable='true' ";
	    ResultSet rsIsApplicable = clsGlobalVarClass.dbMysql.executeResultSet(sql);
	    while (rsIsApplicable.next())
	    {
		String groupCode = rsIsApplicable.getString(1);//groupCode
		String taxOnGD = rsIsApplicable.getString(3);//taxOnGD

		if (mapDateWiseGroupTaxSettlementData.containsKey(groupCode))
		{
		    clsManagerReportBean objGroupDtl = mapDateWiseGroupTaxSettlementData.get(groupCode);
		    if (taxOnGD.equalsIgnoreCase("Gross"))
		    {
			taxWiseGroupTotal += objGroupDtl.getDblSubTotal();
		    }
		    else
		    {
			taxWiseGroupTotal += objGroupDtl.getDblNetTotal();
		    }
		}
	    }
	    rsIsApplicable.close();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	finally
	{
	    return taxWiseGroupTotal;
	}
    }

    private int funGetLineCount(String settlement__________, String group_name____, LinkedHashMap<String, String> mapAllTaxes, String totals________)
    {
	StringBuilder stringBuilder = new StringBuilder();
	stringBuilder.append(settlement__________);
	stringBuilder.append(group_name____);
	stringBuilder.append(totals________);

	for (String taxDesc : mapAllTaxes.values())
	{
	    String labelTaxDesc = "   " + taxDesc + "|";
	    stringBuilder.append(labelTaxDesc);
	}

	return stringBuilder.length();
    }
}
