/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.POSReport.view;

import com.POSGlobal.controller.clsBillDtl;
import com.POSGlobal.controller.clsBillItemDtlBean;
import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsGroupSubGroupWiseSales;
import com.POSGlobal.controller.clsManagerReportBean;
import com.POSGlobal.controller.clsManagersReport;
import com.POSGlobal.controller.clsPosConfigFile;
import com.POSGlobal.controller.clsSendMail;
import com.POSGlobal.controller.clsUtility;
import com.POSGlobal.controller.clsUtility2;
import com.POSGlobal.view.frmOkPopUp;
import com.POSReport.controller.clsGroupSubGroupItemBean;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JPanel;
import javax.swing.Timer;
import org.apache.poi.ss.usermodel.ExcelStyleDateFormatter;

public class frmGrossSalesSummary extends javax.swing.JFrame
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
    String dashedLineOf150Chars = "";

    /**
     * this Function is used for Component initialization
     */
    public frmGrossSalesSummary()
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
    private void funGenerateTextReport() throws Exception
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

	    funGenerateTextFile(fromDate, toDate, posCode);

	}
    }

    private void funSendReportOnMail() throws Exception
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

	    funGenerateTextFile(fromDate, toDate, posCode);

	    String filePath = System.getProperty("user.dir");
	    File file = new File(filePath + File.separator + "Temp" + File.separator + "Gross Sales Summary.txt");
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
        lblformName.setText("       Gross Sales Summary");
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
        lblAPC.setText("Gross Sales Summary");
        lblAPC.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        pnlAPC.add(lblAPC);
        lblAPC.setBounds(240, 40, 250, 30);

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
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMainLayout.createSequentialGroup()
                .addContainerGap(25, Short.MAX_VALUE)
                .addComponent(pnlAPC, javax.swing.GroupLayout.PREFERRED_SIZE, 795, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
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

	try
	{
	    funGenerateTextReport();
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
	try
	{
	    funSendReportOnMail();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}


    }//GEN-LAST:event_btnSendOnMailMouseClicked

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("Gross Sales Summary");
    }//GEN-LAST:event_btnCloseActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("Gross Sales Summary");
    }//GEN-LAST:event_formWindowClosed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("Gross Sales Summary");
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

    private void funGenerateTextFile(String fromDate, String toDate, String posCode)
    {
	try
	{
	    objUtility2.funCreateTempFolder();

	    String filePath = System.getProperty("user.dir");
	    File file = new File(filePath + File.separator + "Temp" + File.separator + "Gross Sales Summary.txt");
	    PrintWriter pw = new PrintWriter(file);

	    //settlement break up
	    funGrossSalesGenerator(fromDate, toDate, posCode, pw);

	    //settlement wise group wise break up
//            funSettlementWiseGroupWiseBreakupData(fromDate, toDate, posCode, pw);
	    //
	    pw.println();
	    pw.println();
//	    if ("linux".equalsIgnoreCase(clsPosConfigFile.gPrintOS))
//	    {
//		pw.println("V");//Linux
//	    }
//	    else if ("windows".equalsIgnoreCase(clsPosConfigFile.gPrintOS))
//	    {
//		if ("Inbuild".equalsIgnoreCase(clsPosConfigFile.gPrinterType))
//		{
//		    pw.println("V");
//		}
//		else
//		{
//		    pw.println("m");//windows
//		}
//	    }

	    pw.flush();
	    pw.close();

	    Desktop dt = Desktop.getDesktop();
	    dt.open(file);
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private int funGrossSalesGenerator(String fromDate, String toDate, String posCode, PrintWriter pw) throws Exception
    {
	StringBuilder sbSqlLiveFile = new StringBuilder();
	StringBuilder sbSqlQFile = new StringBuilder();
//	
	String firstBill = "", lastBill = "";

	Date dateFromDate = dteFromDate.getDate();
	Date dateToDate = dteToDate.getDate();
	Date currentDate = new Date();

	boolean isDayEndHappend = objUtility2.isDayEndHappened(toDate);
	if (!isDayEndHappend)
	{
	    pw.println();
	    pw.print(objUtility.funPrintTextWithAlignment("DAY END NOT DONE.", 100, "center"));
	    pw.println();
	}

	SimpleDateFormat dateFormat = new SimpleDateFormat("E dd-MMM-yyyy");

	SimpleDateFormat dateFormat24Hrs = new SimpleDateFormat("E dd-MMM-yyyy HH:mm:ss");

	String fromDayAndDate = dateFormat.format(dateFromDate);
	String toDayAndDate = dateFormat.format(dateToDate);
	String todaysDate = dateFormat24Hrs.format(currentDate);

	pw.println(clsGlobalVarClass.gClientName);
	if (clsGlobalVarClass.gClientAddress2.trim().length() > 0)
	{
	    pw.println(clsGlobalVarClass.gClientAddress2);
	}
	if (clsGlobalVarClass.gClientAddress3.trim().length() > 0)
	{
	    pw.println(clsGlobalVarClass.gClientAddress3);
	}
	pw.println("Report : Gross Sales Summary");
	pw.println("Reporting For :" + "  " + fromDayAndDate + " " + "To" + " " + toDayAndDate);
	pw.println("Printed On :" + "  " + todaysDate);

	String sqlTip = "", sqlNoOfBill = "", sqlDiscount = "";

	Map<String, Map<String, clsManagerReportBean>> mapBillWiseData = new TreeMap<String, Map<String, clsManagerReportBean>>();
	Map<String, Map<String, String>> mapBillWiseSettlementNames = new TreeMap<String, Map<String, String>>();
	Map<String, Map<String, String>> mapBillWiseTaxNames = new TreeMap<String, Map<String, String>>();
	Map<String, Map<String, String>> mapBillWiseGroupNames = new TreeMap<String, Map<String, String>>();

	int maxSettlementNameLength = 24;
	int maxGroupNameLength = 12;
	int maxTaxNameLength = 0;
	int maxLineCount = 0;

	Map<String, Integer> mapGroupNameWithLength = new TreeMap<>();

	final String ORDERFORSUMMARY = "VSC";

	Comparator<String> taxNameSorting = new Comparator<String>()
	{
	    @Override
	    public int compare(String o1, String o2)
	    {
		return ORDERFORSUMMARY.indexOf(o1.charAt(0)) - ORDERFORSUMMARY.indexOf(o2.charAt(0));
	    }
	};

	Map<String, Integer> mapTaxNameWithLength = null;
	if (clsGlobalVarClass.gClientCode.equalsIgnoreCase("240.001"))
	{
	    mapTaxNameWithLength = new TreeMap<>(taxNameSorting);
	}
	else
	{
	    mapTaxNameWithLength = new TreeMap<>();
	}

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
	sbSqlLiveFile.append(" select a.strBillNo,c.strSettelmentCode,c.strSettelmentDesc,b.dblSettlementAmt,DATE_FORMAT(date(a.dteBillDate),'%d-%m-%Y')dteBillDate "
		+ ",a.dblRoundOff "
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
	sbSqlLiveFile.append(" order BY a.strBillNo,c.strSettelmentDesc ");
	System.out.println(sbSqlLiveFile);

	ResultSet rsSettleManager = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLiveFile.toString());
	while (rsSettleManager.next())
	{

	    String strBillNo = rsSettleManager.getString(1);
	    String settlementCode = rsSettleManager.getString(2);
	    String settlementDesc = rsSettleManager.getString(3);
	    double settleAmt = rsSettleManager.getDouble(4);
	    String billDate = rsSettleManager.getString(5);
	    double roundOffAmt = rsSettleManager.getDouble(6);

	    if (settlementDesc.length() > maxSettlementNameLength)
	    {
		maxSettlementNameLength = settlementDesc.length();
	    }

	    totalSettleAmt = totalSettleAmt + settleAmt;

	    if (mapBillWiseSettlementNames.containsKey(strBillNo))
	    {
		Map<String, String> mapSettlementNames = mapBillWiseSettlementNames.get(strBillNo);

		mapSettlementNames.put(settlementCode, settlementDesc);
	    }
	    else
	    {
		Map<String, String> mapSettlementNames = new TreeMap<>();

		mapSettlementNames.put(settlementCode, settlementDesc);

		mapBillWiseSettlementNames.put(strBillNo, mapSettlementNames);
	    }

	    if (mapBillWiseData.containsKey(strBillNo))
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = mapBillWiseData.get(strBillNo);

		//put settlement dtl
		if (mapBillWiseSettlementWiseData.containsKey(settlementCode))
		{
		    clsManagerReportBean objManagerReportBean = mapBillWiseSettlementWiseData.get(settlementCode);
		    objManagerReportBean.setDblSettlementAmt(objManagerReportBean.getDblSettlementAmt() + settleAmt);

		    mapBillWiseSettlementWiseData.put(settlementCode, objManagerReportBean);
		}
		else
		{
		    clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		    objManagerReportBean.setStrSettlementCode(settlementCode);
		    objManagerReportBean.setStrSettlementDesc(settlementDesc);
		    objManagerReportBean.setDblSettlementAmt(settleAmt);

		    mapBillWiseSettlementWiseData.put(settlementCode, objManagerReportBean);
		}
		//put total settlement dtl
		if (mapBillWiseSettlementWiseData.containsKey("TotalSettlementAmt"))
		{
		    clsManagerReportBean objManagerReportBean = mapBillWiseSettlementWiseData.get("TotalSettlementAmt");
		    objManagerReportBean.setDblSettlementAmt(objManagerReportBean.getDblSettlementAmt() + settleAmt);

		    mapBillWiseSettlementWiseData.put("TotalSettlementAmt", objManagerReportBean);
		}
		else
		{
		    clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		    objManagerReportBean.setStrSettlementCode("TotalSettlementAmt");
		    objManagerReportBean.setStrSettlementDesc("TotalSettlementAmt");
		    objManagerReportBean.setDblSettlementAmt(settleAmt);

		    mapBillWiseSettlementWiseData.put("TotalSettlementAmt", objManagerReportBean);
		}

		mapBillWiseData.put(strBillNo, mapBillWiseSettlementWiseData);
	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();

		//put settlement dtl
		clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		objManagerReportBean.setStrSettlementCode(settlementCode);
		objManagerReportBean.setStrSettlementDesc(settlementDesc);
		objManagerReportBean.setDblSettlementAmt(settleAmt);
		objManagerReportBean.setDblRoundOffAmt(roundOffAmt);

		mapBillWiseSettlementWiseData.put(settlementCode, objManagerReportBean);

		//put total settlement dtl
		objManagerReportBean = new clsManagerReportBean();
		objManagerReportBean.setStrSettlementCode("TotalSettlementAmt");
		objManagerReportBean.setStrSettlementDesc("TotalSettlementAmt");
		objManagerReportBean.setDblSettlementAmt(settleAmt);

		mapBillWiseSettlementWiseData.put("TotalSettlementAmt", objManagerReportBean);

		mapBillWiseData.put(strBillNo, mapBillWiseSettlementWiseData);
	    }
	}
	rsSettleManager.close();

	sbSqlQFile.setLength(0);
	sbSqlQFile.append(" select a.strBillNo,c.strSettelmentCode,c.strSettelmentDesc,b.dblSettlementAmt,DATE_FORMAT(date(a.dteBillDate),'%d-%m-%Y')dteBillDate "
		+ ",a.dblRoundOff "
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
	sbSqlQFile.append(" order BY a.strBillNo,c.strSettelmentDesc ");
	rsSettleManager = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFile.toString());

	while (rsSettleManager.next())
	{

	    String strBillNo = rsSettleManager.getString(1);
	    String settlementCode = rsSettleManager.getString(2);
	    String settlementDesc = rsSettleManager.getString(3);
	    double settleAmt = rsSettleManager.getDouble(4);
	    String billDate = rsSettleManager.getString(5);
	    double roundOffAmt = rsSettleManager.getDouble(6);

	    if (settlementDesc.length() > maxSettlementNameLength)
	    {
		maxSettlementNameLength = settlementDesc.length();
	    }

	    totalSettleAmt = totalSettleAmt + settleAmt;

	    if (mapBillWiseSettlementNames.containsKey(strBillNo))
	    {
		Map<String, String> mapSettlementNames = mapBillWiseSettlementNames.get(strBillNo);

		mapSettlementNames.put(settlementCode, settlementDesc);
	    }
	    else
	    {
		Map<String, String> mapSettlementNames = new TreeMap<>();

		mapSettlementNames.put(settlementCode, settlementDesc);

		mapBillWiseSettlementNames.put(strBillNo, mapSettlementNames);
	    }

	    if (mapBillWiseData.containsKey(strBillNo))
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = mapBillWiseData.get(strBillNo);

		//put settlement dtl
		if (mapBillWiseSettlementWiseData.containsKey(settlementCode))
		{
		    clsManagerReportBean objManagerReportBean = mapBillWiseSettlementWiseData.get(settlementCode);
		    objManagerReportBean.setDblSettlementAmt(objManagerReportBean.getDblSettlementAmt() + settleAmt);

		    mapBillWiseSettlementWiseData.put(settlementCode, objManagerReportBean);
		}
		else
		{
		    clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		    objManagerReportBean.setStrSettlementCode(settlementCode);
		    objManagerReportBean.setStrSettlementDesc(settlementDesc);
		    objManagerReportBean.setDblSettlementAmt(settleAmt);

		    mapBillWiseSettlementWiseData.put(settlementCode, objManagerReportBean);
		}
		//put total settlement dtl
		if (mapBillWiseSettlementWiseData.containsKey("TotalSettlementAmt"))
		{
		    clsManagerReportBean objManagerReportBean = mapBillWiseSettlementWiseData.get("TotalSettlementAmt");
		    objManagerReportBean.setDblSettlementAmt(objManagerReportBean.getDblSettlementAmt() + settleAmt);

		    mapBillWiseSettlementWiseData.put("TotalSettlementAmt", objManagerReportBean);
		}
		else
		{
		    clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		    objManagerReportBean.setStrSettlementCode("TotalSettlementAmt");
		    objManagerReportBean.setStrSettlementDesc("TotalSettlementAmt");
		    objManagerReportBean.setDblSettlementAmt(settleAmt);

		    mapBillWiseSettlementWiseData.put("TotalSettlementAmt", objManagerReportBean);
		}

		mapBillWiseData.put(strBillNo, mapBillWiseSettlementWiseData);
	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();
		//put settlement dtl

		clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		objManagerReportBean.setStrSettlementCode(settlementCode);
		objManagerReportBean.setStrSettlementDesc(settlementDesc);
		objManagerReportBean.setDblSettlementAmt(settleAmt);
		objManagerReportBean.setDblRoundOffAmt(roundOffAmt);

		mapBillWiseSettlementWiseData.put(settlementCode, objManagerReportBean);

		//put total settlement dtl
		objManagerReportBean = new clsManagerReportBean();
		objManagerReportBean.setStrSettlementCode("TotalSettlementAmt");
		objManagerReportBean.setStrSettlementDesc("TotalSettlementAmt");
		objManagerReportBean.setDblSettlementAmt(settleAmt);

		mapBillWiseSettlementWiseData.put("TotalSettlementAmt", objManagerReportBean);

		mapBillWiseData.put(strBillNo, mapBillWiseSettlementWiseData);
	    }
	}
	rsSettleManager.close();

	/**
	 * live taxes
	 */
	String sqlTax = "SELECT a.strBillNo,ifnull(c.strTaxCode,'VAT'),ifnull(c.strTaxDesc,'VAT'),ifnull(SUM(b.dblTaxAmount),0) "
		+ " from tblbillhd a "
		+ " left outer join tblbilltaxdtl b on a.strBillNo=b.strBillNo AND DATE(a.dteBillDate)= DATE(b.dteBillDate) AND a.strClientCode=b.strClientCode "
		+ " left outer join tbltaxhd c on b.strTaxCode=c.strTaxCode "
		+ " where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ";
	if (!posCode.equalsIgnoreCase("All"))
	{
	    sqlTax += " and a.strPOSCode='" + posCode + "' ";
	}
	sqlTax += " group by a.strBillNo,c.strTaxCode";
	ResultSet rsTaxDtl1 = clsGlobalVarClass.dbMysql.executeResultSet(sqlTax);
	while (rsTaxDtl1.next())
	{
	    String strBillNo = rsTaxDtl1.getString(1);
	    String taxCode = rsTaxDtl1.getString(2);
	    String taxDesc = rsTaxDtl1.getString(3);
	    double taxAmt = rsTaxDtl1.getDouble(4);

	    mapTaxNameWithLength.put(taxDesc, taxDesc.length());
	    if (taxDesc.length() > maxTaxNameLength)
	    {
		maxTaxNameLength = taxDesc.length();
	    }

	    totalTaxAmt = totalTaxAmt + taxAmt;

	    if (mapBillWiseTaxNames.containsKey(strBillNo))
	    {
		Map<String, String> mapTaxNames = mapBillWiseTaxNames.get(strBillNo);

		mapTaxNames.put(taxCode, taxDesc);
	    }
	    else
	    {
		Map<String, String> mapTaxNames = new TreeMap<>();

		mapTaxNames.put(taxCode, taxDesc);

		mapBillWiseTaxNames.put(strBillNo, mapTaxNames);
	    }

	    if (mapBillWiseData.containsKey(strBillNo))
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = mapBillWiseData.get(strBillNo);

		//put tax dtl
		if (mapBillWiseSettlementWiseData.containsKey(taxCode))
		{
		    clsManagerReportBean objManagerReportBean = mapBillWiseSettlementWiseData.get(taxCode);
		    objManagerReportBean.setDblSettlementAmt(objManagerReportBean.getDblTaxAmt() + taxAmt);

		    mapBillWiseSettlementWiseData.put(taxCode, objManagerReportBean);
		}
		else
		{
		    clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		    objManagerReportBean.setStrTaxCode(taxCode);
		    objManagerReportBean.setStrTaxDesc(taxDesc);
		    objManagerReportBean.setDblTaxAmt(taxAmt);

		    mapBillWiseSettlementWiseData.put(taxCode, objManagerReportBean);
		}

		//put total tax dtl
		if (mapBillWiseSettlementWiseData.containsKey("TotalTaxAmt"))
		{
		    clsManagerReportBean objManagerReportBean = mapBillWiseSettlementWiseData.get("TotalTaxAmt");
		    objManagerReportBean.setDblTaxAmt(objManagerReportBean.getDblTaxAmt() + taxAmt);

		    mapBillWiseSettlementWiseData.put("TotalTaxAmt", objManagerReportBean);
		}
		else
		{
		    clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		    objManagerReportBean.setStrTaxCode("TotalTaxAmt");
		    objManagerReportBean.setStrTaxDesc("TotalTaxAmt");
		    objManagerReportBean.setDblTaxAmt(taxAmt);

		    mapBillWiseSettlementWiseData.put("TotalTaxAmt", objManagerReportBean);
		}

		mapBillWiseData.put(strBillNo, mapBillWiseSettlementWiseData);
	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();

		clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		objManagerReportBean.setStrTaxCode(taxCode);
		objManagerReportBean.setStrTaxDesc(taxDesc);
		objManagerReportBean.setDblTaxAmt(taxAmt);

		//put total tax dtl
		objManagerReportBean = new clsManagerReportBean();
		objManagerReportBean.setStrTaxCode("TotalTaxAmt");
		objManagerReportBean.setStrTaxDesc("TotalTaxAmt");
		objManagerReportBean.setDblTaxAmt(taxAmt);

		mapBillWiseSettlementWiseData.put("TotalTaxAmt", objManagerReportBean);

		mapBillWiseSettlementWiseData.put(taxCode, objManagerReportBean);

		mapBillWiseData.put(strBillNo, mapBillWiseSettlementWiseData);
	    }
	}
	rsTaxDtl1.close();

	/**
	 * Q taxes
	 */
	sqlTax = "SELECT a.strBillNo,ifnull(c.strTaxCode,'VAT'),ifnull(c.strTaxDesc,'VAT'),ifnull(SUM(b.dblTaxAmount),0) "
		+ " from tblqbillhd a "
		+ " left outer join tblqbilltaxdtl b on a.strBillNo=b.strBillNo AND DATE(a.dteBillDate)= DATE(b.dteBillDate) AND a.strClientCode=b.strClientCode "
		+ " left outer join tbltaxhd c on b.strTaxCode=c.strTaxCode "
		+ " where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' ";
	if (!posCode.equalsIgnoreCase("All"))
	{
	    sqlTax += " and a.strPOSCode='" + posCode + "' ";
	}
	sqlTax += " group by a.strBillNo,c.strTaxCode";
	rsTaxDtl1 = clsGlobalVarClass.dbMysql.executeResultSet(sqlTax);
	while (rsTaxDtl1.next())
	{
	    String strBillNo = rsTaxDtl1.getString(1);
	    String taxCode = rsTaxDtl1.getString(2);
	    String taxDesc = rsTaxDtl1.getString(3);
	    double taxAmt = rsTaxDtl1.getDouble(4);

	    if (taxDesc.length() > maxTaxNameLength)
	    {
		maxTaxNameLength = taxDesc.length();
	    }

	    mapTaxNameWithLength.put(taxDesc, taxDesc.length());

	    totalTaxAmt = totalTaxAmt + taxAmt;

	    if (mapBillWiseTaxNames.containsKey(strBillNo))
	    {
		Map<String, String> mapTaxNames = mapBillWiseTaxNames.get(strBillNo);

		mapTaxNames.put(taxCode, taxDesc);
	    }
	    else
	    {
		Map<String, String> mapTaxNames = new TreeMap<>();

		mapTaxNames.put(taxCode, taxDesc);

		mapBillWiseTaxNames.put(strBillNo, mapTaxNames);
	    }

	    if (mapBillWiseData.containsKey(strBillNo))
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = mapBillWiseData.get(strBillNo);

		//put tax dtl
		if (mapBillWiseSettlementWiseData.containsKey(taxCode))
		{
		    clsManagerReportBean objManagerReportBean = mapBillWiseSettlementWiseData.get(taxCode);
		    objManagerReportBean.setDblSettlementAmt(objManagerReportBean.getDblTaxAmt() + taxAmt);

		    mapBillWiseSettlementWiseData.put(taxCode, objManagerReportBean);
		}
		else
		{
		    clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		    objManagerReportBean.setStrTaxCode(taxCode);
		    objManagerReportBean.setStrTaxDesc(taxDesc);
		    objManagerReportBean.setDblTaxAmt(taxAmt);

		    mapBillWiseSettlementWiseData.put(taxCode, objManagerReportBean);
		}

		//put total tax dtl
		if (mapBillWiseSettlementWiseData.containsKey("TotalTaxAmt"))
		{
		    clsManagerReportBean objManagerReportBean = mapBillWiseSettlementWiseData.get("TotalTaxAmt");
		    objManagerReportBean.setDblTaxAmt(objManagerReportBean.getDblTaxAmt() + taxAmt);

		    mapBillWiseSettlementWiseData.put("TotalTaxAmt", objManagerReportBean);
		}
		else
		{
		    clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		    objManagerReportBean.setStrTaxCode("TotalTaxAmt");
		    objManagerReportBean.setStrTaxDesc("TotalTaxAmt");
		    objManagerReportBean.setDblTaxAmt(taxAmt);

		    mapBillWiseSettlementWiseData.put("TotalTaxAmt", objManagerReportBean);
		}

		mapBillWiseData.put(strBillNo, mapBillWiseSettlementWiseData);
	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();

		clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		objManagerReportBean.setStrTaxCode(taxCode);
		objManagerReportBean.setStrTaxDesc(taxDesc);
		objManagerReportBean.setDblTaxAmt(taxAmt);

		objManagerReportBean = new clsManagerReportBean();
		objManagerReportBean.setStrTaxCode("TotalTaxAmt");
		objManagerReportBean.setStrTaxDesc("TotalTaxAmt");
		objManagerReportBean.setDblTaxAmt(taxAmt);

		mapBillWiseSettlementWiseData.put("TotalTaxAmt", objManagerReportBean);

		mapBillWiseSettlementWiseData.put(taxCode, objManagerReportBean);

		mapBillWiseData.put(strBillNo, mapBillWiseSettlementWiseData);
	    }
	}
	rsTaxDtl1.close();

	maxTaxNameLength = maxTaxNameLength + 1;

	//set discount,roundoff,tip
	sbSqlLiveFile.setLength(0);
	sbSqlLiveFile.append(" SELECT sum(a.dblDiscountAmt),sum(a.dblRoundOff),sum(a.dblTipAmount),a.strBillNo "
		+ " from tblbillhd a  "
		+ " where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		+ "  ");
	if (!posCode.equalsIgnoreCase("All"))
	{
	    sbSqlLiveFile.append(" and a.strPOSCode='" + posCode + "' ");
	}
	sbSqlLiveFile.append(" group by a.strBillNo ");
	System.out.println(sbSqlLiveFile);

	rsSettleManager = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLiveFile.toString());
	while (rsSettleManager.next())
	{
	    double discAmt = rsSettleManager.getDouble(1);//discAmt
	    double roundOffAmt = rsSettleManager.getDouble(2);//roundOff
	    double tipAmt = rsSettleManager.getDouble(3);//tipAmt
	    //int noOfBills = rsSettleManager.getInt(4);//bill count
	    totalDiscAmt = totalDiscAmt + discAmt;
	    totalRoundOffAmt = totalRoundOffAmt + roundOffAmt;//roundOff
	    totalTipAmt = totalTipAmt + tipAmt;//tipAmt
	    //totalBills = totalBills + noOfBills;//bill count
	    String strBillNo = rsSettleManager.getString(4);//billDate

	    //discount
	    if (mapBillWiseData.containsKey(strBillNo))
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = mapBillWiseData.get(strBillNo);
		if (mapBillWiseSettlementWiseData.containsKey("DiscAmt"))
		{
		    clsManagerReportBean objDiscAmt = mapBillWiseSettlementWiseData.get("DiscAmt");
		    objDiscAmt.setDblDiscAmt(objDiscAmt.getDblDiscAmt() + discAmt);
		}
		else
		{
		    clsManagerReportBean objDiscAmt = new clsManagerReportBean();
		    objDiscAmt.setDblDiscAmt(discAmt);

		    mapBillWiseSettlementWiseData.put("DiscAmt", objDiscAmt);
		}

	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();

		clsManagerReportBean objDiscAmt = new clsManagerReportBean();
		objDiscAmt.setDblDiscAmt(discAmt);

		mapBillWiseSettlementWiseData.put("DiscAmt", objDiscAmt);

		mapBillWiseData.put(strBillNo, mapBillWiseSettlementWiseData);
	    }

	    //roundoff
	    if (mapBillWiseData.containsKey(strBillNo))
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = mapBillWiseData.get(strBillNo);
		if (mapBillWiseSettlementWiseData.containsKey("RoundOffAmt"))
		{
		    clsManagerReportBean objRoundOffAmt = mapBillWiseSettlementWiseData.get("RoundOffAmt");
		    objRoundOffAmt.setDblRoundOffAmt(objRoundOffAmt.getDblRoundOffAmt() + roundOffAmt);
		}
		else
		{
		    clsManagerReportBean objRoundOffAmt = new clsManagerReportBean();
		    objRoundOffAmt.setDblRoundOffAmt(roundOffAmt);

		    mapBillWiseSettlementWiseData.put("RoundOffAmt", objRoundOffAmt);
		}

	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();

		clsManagerReportBean objRoundOffAmt = new clsManagerReportBean();
		objRoundOffAmt.setDblRoundOffAmt(roundOffAmt);

		mapBillWiseSettlementWiseData.put("RoundOffAmt", objRoundOffAmt);

		mapBillWiseData.put(strBillNo, mapBillWiseSettlementWiseData);
	    }

	    //tip
	    if (mapBillWiseData.containsKey(strBillNo))
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = mapBillWiseData.get(strBillNo);
		if (mapBillWiseSettlementWiseData.containsKey("TipAmt"))
		{
		    clsManagerReportBean objTipAmt = mapBillWiseSettlementWiseData.get("TipAmt");
		    objTipAmt.setDblTipAmt(objTipAmt.getDblTipAmt() + tipAmt);
		}
		else
		{
		    clsManagerReportBean objTipAmt = new clsManagerReportBean();
		    objTipAmt.setDblTipAmt(tipAmt);

		    mapBillWiseSettlementWiseData.put("TipAmt", objTipAmt);
		}

	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();

		clsManagerReportBean objTipAmt = new clsManagerReportBean();
		objTipAmt.setDblTipAmt(tipAmt);

		mapBillWiseSettlementWiseData.put("TipAmt", objTipAmt);

		mapBillWiseData.put(strBillNo, mapBillWiseSettlementWiseData);
	    }

	}
	rsSettleManager.close();

	sbSqlQFile.setLength(0);
	sbSqlQFile.append(" SELECT sum(a.dblDiscountAmt),sum(a.dblRoundOff),sum(a.dblTipAmount),a.strBillNo "
		+ " from tblqbillhd a"
		+ " where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
		+ "  ");
	if (!posCode.equalsIgnoreCase("All"))
	{
	    sbSqlQFile.append(" and a.strPOSCode='" + posCode + "' ");
	}
	sbSqlQFile.append(" group by a.strBillNo ");
	System.out.println(sbSqlQFile);

	rsSettleManager = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFile.toString());
	while (rsSettleManager.next())
	{
	    double discAmt = rsSettleManager.getDouble(1);//discAmt
	    double roundOffAmt = rsSettleManager.getDouble(2);//roundOff
	    double tipAmt = rsSettleManager.getDouble(3);//tipAmt
	    // int noOfBills = rsSettleManager.getInt(4);//bill count
	    totalDiscAmt = totalDiscAmt + discAmt;
	    totalRoundOffAmt = totalRoundOffAmt + roundOffAmt;//roundOff
	    totalTipAmt = totalTipAmt + tipAmt;//tipAmt
	    //  totalBills = totalBills + noOfBills;//bill count
	    String strBillNo = rsSettleManager.getString(4);//billDate

	    //discount
	    if (mapBillWiseData.containsKey(strBillNo))
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = mapBillWiseData.get(strBillNo);
		if (mapBillWiseSettlementWiseData.containsKey("DiscAmt"))
		{
		    clsManagerReportBean objDiscAmt = mapBillWiseSettlementWiseData.get("DiscAmt");
		    objDiscAmt.setDblDiscAmt(objDiscAmt.getDblDiscAmt() + discAmt);
		}
		else
		{
		    clsManagerReportBean objDiscAmt = new clsManagerReportBean();
		    objDiscAmt.setDblDiscAmt(discAmt);

		    mapBillWiseSettlementWiseData.put("DiscAmt", objDiscAmt);
		}

	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();

		clsManagerReportBean objDiscAmt = new clsManagerReportBean();
		objDiscAmt.setDblDiscAmt(discAmt);

		mapBillWiseSettlementWiseData.put("DiscAmt", objDiscAmt);

		mapBillWiseData.put(strBillNo, mapBillWiseSettlementWiseData);
	    }

	    //roundoff
	    if (mapBillWiseData.containsKey(strBillNo))
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = mapBillWiseData.get(strBillNo);
		if (mapBillWiseSettlementWiseData.containsKey("RoundOffAmt"))
		{
		    clsManagerReportBean objRoundOffAmt = mapBillWiseSettlementWiseData.get("RoundOffAmt");
		    objRoundOffAmt.setDblRoundOffAmt(objRoundOffAmt.getDblRoundOffAmt() + roundOffAmt);
		}
		else
		{
		    clsManagerReportBean objRoundOffAmt = new clsManagerReportBean();
		    objRoundOffAmt.setDblRoundOffAmt(roundOffAmt);

		    mapBillWiseSettlementWiseData.put("RoundOffAmt", objRoundOffAmt);
		}

	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();

		clsManagerReportBean objRoundOffAmt = new clsManagerReportBean();
		objRoundOffAmt.setDblRoundOffAmt(roundOffAmt);

		mapBillWiseSettlementWiseData.put("RoundOffAmt", objRoundOffAmt);

		mapBillWiseData.put(strBillNo, mapBillWiseSettlementWiseData);
	    }

	    //tip
	    if (mapBillWiseData.containsKey(strBillNo))
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = mapBillWiseData.get(strBillNo);
		if (mapBillWiseSettlementWiseData.containsKey("TipAmt"))
		{
		    clsManagerReportBean objTipAmt = mapBillWiseSettlementWiseData.get("TipAmt");
		    objTipAmt.setDblTipAmt(objTipAmt.getDblTipAmt() + tipAmt);
		}
		else
		{
		    clsManagerReportBean objTipAmt = new clsManagerReportBean();
		    objTipAmt.setDblTipAmt(tipAmt);

		    mapBillWiseSettlementWiseData.put("TipAmt", objTipAmt);
		}

	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapDateWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();

		clsManagerReportBean objTipAmt = new clsManagerReportBean();
		objTipAmt.setDblTipAmt(tipAmt);

		mapDateWiseSettlementWiseData.put("TipAmt", objTipAmt);

		mapBillWiseData.put(strBillNo, mapDateWiseSettlementWiseData);
	    }
	}
	rsSettleManager.close();

	/**
	 * fill live date wise group wise data
	 */
	StringBuilder sqlGroupData = new StringBuilder();

	sqlGroupData.setLength(0);
	sqlGroupData.append("select  a.strBillNo,e.strGroupCode,e.strGroupName,sum(b.dblAmount)SubTotal,sum(b.dblDiscountAmt)Discount,sum(b.dblAmount)-sum(b.dblDiscountAmt)NetTotal "
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
	sqlGroupData.append("group by  a.strBillNo,e.strGroupCode ");
	ResultSet rsGroupsData = clsGlobalVarClass.dbMysql.executeResultSet(sqlGroupData.toString());
	while (rsGroupsData.next())
	{
	    String strBillNo = rsGroupsData.getString(1);//date
	    String groupCode = rsGroupsData.getString(2);//groupCode
	    String groupName = rsGroupsData.getString(3);//groupName
	    double subTotal = rsGroupsData.getDouble(4); //subTotal
	    double discount = rsGroupsData.getDouble(5); //discount
	    double netTotal = rsGroupsData.getDouble(6); //netTotal

	    if (groupName.length() > maxGroupNameLength)
	    {
		maxGroupNameLength = groupName.length();
	    }
	    mapGroupNameWithLength.put(groupName, groupName.length());

	    if (mapBillWiseGroupNames.containsKey(strBillNo))
	    {
		Map<String, String> mapGroupNames = mapBillWiseGroupNames.get(strBillNo);

		mapGroupNames.put(groupCode, groupName);
	    }
	    else
	    {
		Map<String, String> mapGroupNames = new TreeMap<>();

		mapGroupNames.put(groupCode, groupName);

		mapBillWiseGroupNames.put(strBillNo, mapGroupNames);
	    }

	    if (mapBillWiseData.containsKey(strBillNo))
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = mapBillWiseData.get(strBillNo);

		if (mapBillWiseSettlementWiseData.containsKey(groupCode))
		{
		    clsManagerReportBean objGroupDtl = mapBillWiseSettlementWiseData.get(groupCode);

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

		    mapBillWiseSettlementWiseData.put(groupCode, objGroupDtl);
		}

		//put total settlement dtl
		if (mapBillWiseSettlementWiseData.containsKey("TotalGroupAmt"))
		{
		    clsManagerReportBean objManagerReportBean = mapBillWiseSettlementWiseData.get("TotalGroupAmt");
		    objManagerReportBean.setDblSubTotal(objManagerReportBean.getDblSubTotal() + subTotal);
		    objManagerReportBean.setDblNetTotal(objManagerReportBean.getDblNetTotal() + netTotal);

		    mapBillWiseSettlementWiseData.put("TotalGroupAmt", objManagerReportBean);
		}
		else
		{
		    clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		    objManagerReportBean.setStrGroupCode("TotalGroupAmt");
		    objManagerReportBean.setStrGroupName("TotalGroupAmt");
		    objManagerReportBean.setDblSubTotal(subTotal);
		    objManagerReportBean.setDblNetTotal(netTotal);

		    mapBillWiseSettlementWiseData.put("TotalGroupAmt", objManagerReportBean);
		}
	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();

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

		mapBillWiseSettlementWiseData.put("TotalGroupAmt", objManagerReportBean);

		mapBillWiseSettlementWiseData.put(groupCode, objGroupDtl);

		mapBillWiseData.put(strBillNo, mapBillWiseSettlementWiseData);
	    }
	}
	rsGroupsData.close();

	/**
	 * fill live modifiers date wise group wise data
	 */
	sqlGroupData.setLength(0);
	sqlGroupData.append("select a.strBillNo,e.strGroupCode,e.strGroupName,sum(b.dblAmount)SubTotal,sum(b.dblDiscAmt)Discount,sum(b.dblAmount)-sum(b.dblDiscAmt)NetTotal "
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
	sqlGroupData.append("group by a.strBillNo,e.strGroupCode ");
	rsGroupsData = clsGlobalVarClass.dbMysql.executeResultSet(sqlGroupData.toString());
	while (rsGroupsData.next())
	{
	    String strBillNo = rsGroupsData.getString(1);//date
	    String groupCode = rsGroupsData.getString(2);//groupCode
	    String groupName = rsGroupsData.getString(3);//groupName
	    double subTotal = rsGroupsData.getDouble(4); //subTotal
	    double discount = rsGroupsData.getDouble(5); //discount
	    double netTotal = rsGroupsData.getDouble(6); //netTotal

	    if (groupName.length() > maxGroupNameLength)
	    {
		maxGroupNameLength = groupName.length();
	    }
	    mapGroupNameWithLength.put(groupName, groupName.length());

	    if (mapBillWiseGroupNames.containsKey(strBillNo))
	    {
		Map<String, String> mapGroupNames = mapBillWiseGroupNames.get(strBillNo);

		mapGroupNames.put(groupCode, groupName);
	    }
	    else
	    {
		Map<String, String> mapGroupNames = new TreeMap<>();

		mapGroupNames.put(groupCode, groupName);

		mapBillWiseGroupNames.put(strBillNo, mapGroupNames);
	    }

	    if (mapBillWiseData.containsKey(strBillNo))
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = mapBillWiseData.get(strBillNo);

		if (mapBillWiseSettlementWiseData.containsKey(groupCode))
		{
		    clsManagerReportBean objGroupDtl = mapBillWiseSettlementWiseData.get(groupCode);

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

		    mapBillWiseSettlementWiseData.put(groupCode, objGroupDtl);
		}

		//put total settlement dtl
		if (mapBillWiseSettlementWiseData.containsKey("TotalGroupAmt"))
		{
		    clsManagerReportBean objManagerReportBean = mapBillWiseSettlementWiseData.get("TotalGroupAmt");
		    objManagerReportBean.setDblSubTotal(objManagerReportBean.getDblSubTotal() + subTotal);
		    objManagerReportBean.setDblNetTotal(objManagerReportBean.getDblNetTotal() + netTotal);

		    mapBillWiseSettlementWiseData.put("TotalGroupAmt", objManagerReportBean);
		}
		else
		{
		    clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		    objManagerReportBean.setStrGroupCode("TotalGroupAmt");
		    objManagerReportBean.setStrGroupName("TotalGroupAmt");
		    objManagerReportBean.setDblSubTotal(subTotal);
		    objManagerReportBean.setDblNetTotal(netTotal);

		    mapBillWiseSettlementWiseData.put("TotalGroupAmt", objManagerReportBean);
		}
	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();

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

		mapBillWiseSettlementWiseData.put("TotalGroupAmt", objManagerReportBean);

		mapBillWiseSettlementWiseData.put(groupCode, objGroupDtl);

		mapBillWiseData.put(strBillNo, mapBillWiseSettlementWiseData);
	    }
	}
	rsGroupsData.close();

	/**
	 * fill Q date wise group wise data
	 */
	sqlGroupData.setLength(0);
	sqlGroupData.append("select a.strBillNo,e.strGroupCode,e.strGroupName,sum(b.dblAmount)SubTotal,sum(b.dblDiscountAmt)Discount,sum(b.dblAmount)-sum(b.dblDiscountAmt)NetTotal "
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
	sqlGroupData.append("group by a.strBillNo,e.strGroupCode ");
	rsGroupsData = clsGlobalVarClass.dbMysql.executeResultSet(sqlGroupData.toString());
	while (rsGroupsData.next())
	{
	    String strBillNo = rsGroupsData.getString(1);//date
	    String groupCode = rsGroupsData.getString(2);//groupCode
	    String groupName = rsGroupsData.getString(3);//groupName
	    double subTotal = rsGroupsData.getDouble(4); //subTotal
	    double discount = rsGroupsData.getDouble(5); //discount
	    double netTotal = rsGroupsData.getDouble(6); //netTotal

	    if (groupName.length() > maxGroupNameLength)
	    {
		maxGroupNameLength = groupName.length();
	    }
	    mapGroupNameWithLength.put(groupName, groupName.length());

	    if (mapBillWiseGroupNames.containsKey(strBillNo))
	    {
		Map<String, String> mapGroupNames = mapBillWiseGroupNames.get(strBillNo);

		mapGroupNames.put(groupCode, groupName);
	    }
	    else
	    {
		Map<String, String> mapGroupNames = new TreeMap<>();

		mapGroupNames.put(groupCode, groupName);

		mapBillWiseGroupNames.put(strBillNo, mapGroupNames);
	    }

	    if (mapBillWiseData.containsKey(strBillNo))
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = mapBillWiseData.get(strBillNo);

		if (mapBillWiseSettlementWiseData.containsKey(groupCode))
		{
		    clsManagerReportBean objGroupDtl = mapBillWiseSettlementWiseData.get(groupCode);

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

		    mapBillWiseSettlementWiseData.put(groupCode, objGroupDtl);
		}

		//put total settlement dtl
		if (mapBillWiseSettlementWiseData.containsKey("TotalGroupAmt"))
		{
		    clsManagerReportBean objManagerReportBean = mapBillWiseSettlementWiseData.get("TotalGroupAmt");
		    objManagerReportBean.setDblSubTotal(objManagerReportBean.getDblSubTotal() + subTotal);
		    objManagerReportBean.setDblNetTotal(objManagerReportBean.getDblNetTotal() + netTotal);

		    mapBillWiseSettlementWiseData.put("TotalGroupAmt", objManagerReportBean);
		}
		else
		{
		    clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		    objManagerReportBean.setStrGroupCode("TotalGroupAmt");
		    objManagerReportBean.setStrGroupName("TotalGroupAmt");
		    objManagerReportBean.setDblSubTotal(subTotal);
		    objManagerReportBean.setDblNetTotal(netTotal);

		    mapBillWiseSettlementWiseData.put("TotalGroupAmt", objManagerReportBean);
		}
	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();

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

		mapBillWiseSettlementWiseData.put("TotalGroupAmt", objManagerReportBean);

		mapBillWiseSettlementWiseData.put(groupCode, objGroupDtl);

		mapBillWiseData.put(strBillNo, mapBillWiseSettlementWiseData);
	    }
	}
	rsGroupsData.close();

	/**
	 * fill Q modifiers date wise group wise data
	 */
	sqlGroupData.setLength(0);
	sqlGroupData.append("select a.strBillNo,e.strGroupCode,e.strGroupName,sum(b.dblAmount)SubTotal,sum(b.dblDiscAmt)Discount,sum(b.dblAmount)-sum(b.dblDiscAmt)NetTotal "
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
	sqlGroupData.append("group by a.strBillNo,e.strGroupCode ");
	rsGroupsData = clsGlobalVarClass.dbMysql.executeResultSet(sqlGroupData.toString());
	while (rsGroupsData.next())
	{
	    String strBillNo = rsGroupsData.getString(1);//date
	    String groupCode = rsGroupsData.getString(2);//groupCode
	    String groupName = rsGroupsData.getString(3);//groupName
	    double subTotal = rsGroupsData.getDouble(4); //subTotal
	    double discount = rsGroupsData.getDouble(5); //discount
	    double netTotal = rsGroupsData.getDouble(6); //netTotal

	    if (groupName.length() > maxGroupNameLength)
	    {
		maxGroupNameLength = groupName.length();
	    }
	    mapGroupNameWithLength.put(groupName, groupName.length());

	    if (mapBillWiseGroupNames.containsKey(strBillNo))
	    {
		Map<String, String> mapGroupNames = mapBillWiseGroupNames.get(strBillNo);

		mapGroupNames.put(groupCode, groupName);
	    }
	    else
	    {
		Map<String, String> mapGroupNames = new TreeMap<>();

		mapGroupNames.put(groupCode, groupName);

		mapBillWiseGroupNames.put(strBillNo, mapGroupNames);
	    }

	    if (mapBillWiseData.containsKey(strBillNo))
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = mapBillWiseData.get(strBillNo);

		if (mapBillWiseSettlementWiseData.containsKey(groupCode))
		{
		    clsManagerReportBean objGroupDtl = mapBillWiseSettlementWiseData.get(groupCode);

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

		    mapBillWiseSettlementWiseData.put(groupCode, objGroupDtl);
		}

		//put total settlement dtl
		if (mapBillWiseSettlementWiseData.containsKey("TotalGroupAmt"))
		{
		    clsManagerReportBean objManagerReportBean = mapBillWiseSettlementWiseData.get("TotalGroupAmt");
		    objManagerReportBean.setDblSubTotal(objManagerReportBean.getDblSubTotal() + subTotal);
		    objManagerReportBean.setDblNetTotal(objManagerReportBean.getDblNetTotal() + netTotal);

		    mapBillWiseSettlementWiseData.put("TotalGroupAmt", objManagerReportBean);
		}
		else
		{
		    clsManagerReportBean objManagerReportBean = new clsManagerReportBean();
		    objManagerReportBean.setStrGroupCode("TotalGroupAmt");
		    objManagerReportBean.setStrGroupName("TotalGroupAmt");
		    objManagerReportBean.setDblSubTotal(subTotal);
		    objManagerReportBean.setDblNetTotal(netTotal);

		    mapBillWiseSettlementWiseData.put("TotalGroupAmt", objManagerReportBean);
		}
	    }
	    else
	    {
		Map<String, clsManagerReportBean> mapBillWiseSettlementWiseData = new TreeMap<String, clsManagerReportBean>();

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

		mapBillWiseSettlementWiseData.put("TotalGroupAmt", objManagerReportBean);

		mapBillWiseSettlementWiseData.put(groupCode, objGroupDtl);

		mapBillWiseData.put(strBillNo, mapBillWiseSettlementWiseData);
	    }
	}
	rsGroupsData.close();

	/**
	 * new logic for gross sales
	 */
	final String CASHCARDGUESTCREDIT = "CASH GUEST CREDIT CARD  Total";

	StringBuilder settlementStringBuilder = new StringBuilder();
	String sqlSettlements = "select a.strSettelmentDesc "
		+ "from tblsettelmenthd a "
		+ "order by a.strSettelmentDesc";
	ResultSet rsSettlements = clsGlobalVarClass.dbMysql.executeResultSet(sqlSettlements);
	while (rsSettlements.next())
	{
	    settlementStringBuilder.append(" " + rsSettlements.getString(1));
	}
	rsSettlements.close();
	settlementStringBuilder.append(" Total");

	final String COMMONSORTING = settlementStringBuilder.toString();

	Comparator<String> settlementSorting = new Comparator<String>()
	{
	    @Override
	    public int compare(String o1, String o2)
	    {
		if (clsGlobalVarClass.gClientCode.equals("240.001"))//TOAKS		    
		{
		    return CASHCARDGUESTCREDIT.indexOf(o1) - CASHCARDGUESTCREDIT.indexOf(o2);
		}
		else
		{
		    return COMMONSORTING.indexOf(o1) - COMMONSORTING.indexOf(o2);
		}

	    }
	};

	Map<String, Map<String, Double>> mapSettelemtWiseGroupBreakup = new TreeMap<>(settlementSorting);

	Map<String, Map<String, Double>> mapSettelemtWiseTaxBreakup = new TreeMap<>();
	/**
	 * new logic for gross sales
	 */

	if (mapBillWiseData.size() > 0)
	{
	    for (Map.Entry<String, Map<String, clsManagerReportBean>> entrySet : mapBillWiseData.entrySet())
	    {
		String strBillNo = entrySet.getKey();
		Map<String, clsManagerReportBean> mapBillWiseGroupTaxSettlementData = entrySet.getValue();

		clsManagerReportBean objTotalSettlementAmt = mapBillWiseGroupTaxSettlementData.get("TotalSettlementAmt");
		double totalSettlementAmt = 0;
		if (objTotalSettlementAmt != null)
		{
		    totalSettlementAmt = objTotalSettlementAmt.getDblSettlementAmt();
		}
		clsManagerReportBean objTotalTaxAmt = mapBillWiseGroupTaxSettlementData.get("TotalTaxAmt");
		double totalTaxAmt = 0;
		if (objTotalTaxAmt != null)
		{
		    totalTaxAmt = objTotalTaxAmt.getDblTaxAmt();
		}

		clsManagerReportBean objTotalGroupAmt = mapBillWiseGroupTaxSettlementData.get("TotalGroupAmt");
		//double totalGroupSubTotal = objTotalGroupAmt.getDblSubTotal();
		double totalGroupNetTotal = 0;
		if (objTotalGroupAmt != null)
		{
		    totalGroupNetTotal = objTotalGroupAmt.getDblNetTotal();
		}

		String labelSettlement = "SETTLEMENT          |";

		String horizontalTotalLabel = "  TOTALS   |";

		//pw.println();
		//pw.print(strBillNo);
		Map<String, String> mapBillWiseTaxeNames = mapBillWiseTaxNames.get(strBillNo);

		Iterator<String> ketIterator = mapBillWiseTaxeNames.keySet().iterator();
		while (ketIterator.hasNext())
		{
		    String taxCode = ketIterator.next();
		    if (taxCode == null || taxCode.isEmpty())
		    {
			ketIterator.remove();
		    }
		}

		if (mapBillWiseTaxeNames != null)
		{
		    if (mapBillWiseGroupNames.containsKey(strBillNo))
		    {
			Map<String, String> mapGroupNames = mapBillWiseGroupNames.get(strBillNo);
			for (Map.Entry<String, String> entryGroupNames : mapGroupNames.entrySet())
			{

			    String groupCode = entryGroupNames.getKey();
			    String groupName = entryGroupNames.getValue();
			    if (groupName.length() > maxGroupNameLength)
			    {
				maxGroupNameLength = groupName.length();
			    }

			    clsManagerReportBean objGroupDtl = mapBillWiseGroupTaxSettlementData.get(groupCode);
			    //double groupSubTotal = objGroupDtl.getDblSubTotal();
			    double groupNetTotal = objGroupDtl.getDblNetTotal();

			    /**
			     * print a line
			     */
			    int lineCount = funGetLineCount(strBillNo, labelSettlement, groupName, horizontalTotalLabel, mapBillWiseData, mapBillWiseSettlementNames, mapBillWiseTaxNames);
			    //pw.println();
			    for (int i = 0; i < lineCount; i++)
			    {
				////pw.print("-");
			    }
			    if (lineCount > maxLineCount)
			    {
				maxLineCount = lineCount;
			    }

			    /**
			     * print header line
			     */
			    //pw.println();
			    //pw.print(objUtility.funPrintTextWithAlignment(labelSettlement, labelSettlement.length(), "Left"));
			    //pw.print(objUtility.funPrintTextWithAlignment(groupName + "|", groupName.length(), "Left"));
			    if (mapBillWiseTaxeNames != null)
			    {
				for (String taxDesc : mapBillWiseTaxeNames.values())
				{
				    String labelTaxDesc = taxDesc + "|";
				    //pw.print(objUtility.funPrintTextWithAlignment(labelTaxDesc, labelTaxDesc.length(), "Left"));
				}
			    }
			    //pw.print(objUtility.funPrintTextWithAlignment(horizontalTotalLabel, horizontalTotalLabel.length(), "Left"));

			    /**
			     * print settlement wise data
			     */
			    //pw.println();
			    Map<String, String> mapSettlementNames = mapBillWiseSettlementNames.get(strBillNo);

			    if (mapSettlementNames != null)
			    {
				for (Map.Entry<String, String> entrySettlements : mapSettlementNames.entrySet())
				{
				    String settlementCode = entrySettlements.getKey();
				    String settlementName = entrySettlements.getValue();

				    double horizontalTotalAmt = 0.00;

				    clsManagerReportBean objSettlementDtl = mapBillWiseGroupTaxSettlementData.get(settlementCode);

				    double groupSubTotalForThisSettlement = 0.00;
				    if (totalSettlementAmt > 0)
				    {
					groupSubTotalForThisSettlement = (groupNetTotal / totalSettlementAmt) * objSettlementDtl.getDblSettlementAmt();
					//groupSubTotalForThisSettlement = (groupNetTotal / totalSettlementAmt) * objSettlementDtl.getDblSettlementAmt();
				    }
				    horizontalTotalAmt += groupSubTotalForThisSettlement;

				    //new added for groups
				    if (mapSettelemtWiseGroupBreakup.containsKey(settlementName))
				    {
					Map<String, Double> mapGroupBreakup = mapSettelemtWiseGroupBreakup.get(settlementName);
					if (mapGroupBreakup.containsKey(groupName))
					{
					    mapGroupBreakup.put(groupName, mapGroupBreakup.get(groupName) + groupSubTotalForThisSettlement);

					    mapSettelemtWiseGroupBreakup.put(settlementName, mapGroupBreakup);
					}
					else
					{
					    mapGroupBreakup.put(groupName, groupSubTotalForThisSettlement);

					    mapSettelemtWiseGroupBreakup.put(settlementName, mapGroupBreakup);
					}
				    }
				    else
				    {
					Map<String, Double> mapGroupBreakup = new TreeMap<String, Double>();

					mapGroupBreakup.put(groupName, groupSubTotalForThisSettlement);

					mapSettelemtWiseGroupBreakup.put(settlementName, mapGroupBreakup);
				    }

				    //new added for groups total
				    String totalSettlement = "Total";
				    if (mapSettelemtWiseGroupBreakup.containsKey(totalSettlement))
				    {
					Map<String, Double> mapGroupBreakup = mapSettelemtWiseGroupBreakup.get(totalSettlement);
					if (mapGroupBreakup.containsKey(groupName))
					{
					    mapGroupBreakup.put(groupName, mapGroupBreakup.get(groupName) + groupSubTotalForThisSettlement);

					    mapSettelemtWiseGroupBreakup.put(totalSettlement, mapGroupBreakup);
					}
					else
					{
					    mapGroupBreakup.put(groupName, groupSubTotalForThisSettlement);

					    mapSettelemtWiseGroupBreakup.put(totalSettlement, mapGroupBreakup);
					}
				    }
				    else
				    {
					Map<String, Double> mapGroupBreakup = new TreeMap<String, Double>();

					mapGroupBreakup.put(groupName, groupSubTotalForThisSettlement);

					mapSettelemtWiseGroupBreakup.put(totalSettlement, mapGroupBreakup);
				    }

				    //pw.println();
				    //pw.print(objUtility.funPrintTextWithAlignment(settlementName, labelSettlement.length(), "Left"));
				    //pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(Math.rint(groupSubTotalForThisSettlement) + "|"), groupName.length(), "Right"));
				    if (mapBillWiseTaxeNames != null)
				    {
					for (Map.Entry<String, String> entryTaxNames : mapBillWiseTaxeNames.entrySet())
					{
					    String taxCode = entryTaxNames.getKey();
					    String taxName = entryTaxNames.getValue();

					    String labelTaxDesc = taxName + "|";

					    clsManagerReportBean objTaxDtl = mapBillWiseGroupTaxSettlementData.get(taxCode);
					    double taxAmt = objTaxDtl.getDblTaxAmt();

					    double taxWiseGroupTotal = funGetTaxWiseGroupTotal(strBillNo, taxCode, mapBillWiseGroupTaxSettlementData);

					    double taxAmtForThisTax = 0.00;
					    boolean isApplicable = isApplicableTaxOnGroup(taxCode, groupCode);

					    if (taxWiseGroupTotal > 0 && isApplicable)
					    {
						taxAmtForThisTax = (taxAmt / taxWiseGroupTotal) * groupSubTotalForThisSettlement;
					    }
					    horizontalTotalAmt += taxAmtForThisTax;

					    //new added for taxes
					    String key = settlementName + "!" + groupName + "!" + taxName;
					    if (mapSettelemtWiseTaxBreakup.containsKey(settlementName))
					    {
						Map<String, Double> mapTaxBreakup = mapSettelemtWiseTaxBreakup.get(settlementName);
						if (mapTaxBreakup.containsKey(key))
						{
						    mapTaxBreakup.put(key, mapTaxBreakup.get(key) + taxAmtForThisTax);

						    mapSettelemtWiseTaxBreakup.put(settlementName, mapTaxBreakup);
						}
						else
						{
						    mapTaxBreakup.put(key, taxAmtForThisTax);

						    mapSettelemtWiseTaxBreakup.put(settlementName, mapTaxBreakup);
						}
					    }
					    else
					    {
						Map<String, Double> mapTaxBreakup = new TreeMap<String, Double>();

						mapTaxBreakup.put(key, taxAmtForThisTax);

						mapSettelemtWiseTaxBreakup.put(settlementName, mapTaxBreakup);
					    }

					    //new added for total taxes
					    String totalKey = totalSettlement + "!" + groupName + "!" + taxName;
					    if (mapSettelemtWiseTaxBreakup.containsKey(totalSettlement))
					    {
						Map<String, Double> mapTaxBreakup = mapSettelemtWiseTaxBreakup.get(totalSettlement);
						if (mapTaxBreakup.containsKey(totalKey))
						{
						    mapTaxBreakup.put(totalKey, mapTaxBreakup.get(totalKey) + taxAmtForThisTax);

						    mapSettelemtWiseTaxBreakup.put(totalSettlement, mapTaxBreakup);
						}
						else
						{
						    mapTaxBreakup.put(totalKey, taxAmtForThisTax);

						    mapSettelemtWiseTaxBreakup.put(totalSettlement, mapTaxBreakup);
						}
					    }
					    else
					    {
						Map<String, Double> mapTaxBreakup = new TreeMap<String, Double>();

						mapTaxBreakup.put(totalKey, taxAmtForThisTax);

						mapSettelemtWiseTaxBreakup.put(totalSettlement, mapTaxBreakup);
					    }

					    //pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(Math.rint(taxAmtForThisTax)) + "|", labelTaxDesc.length(), "Right"));
					}
				    }
				    //pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(Math.rint(horizontalTotalAmt)) + "|", horizontalTotalLabel.length(), "Right"));
				}
			    }
			    /**
			     * print total line
			     */
			    //pw.println();
			    for (int i = 0; i < lineCount; i++)
			    {
				//pw.print("-");
			    }
			    //pw.println();
			    //pw.print(objUtility.funPrintTextWithAlignment(groupName.toUpperCase() + " TOTALS", labelSettlement.length(), "Left"));
			    //pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(Math.rint(groupNetTotal)) + "|", groupName.length(), "Right"));
			    if (mapBillWiseTaxeNames != null)
			    {
				for (Map.Entry<String, String> entryTaxNames : mapBillWiseTaxeNames.entrySet())
				{
				    String taxCode = entryTaxNames.getKey();
				    String taxName = entryTaxNames.getValue();

				    String labelTaxDesc = taxName + "|";
				    double taxAmt = 0.00;

				    boolean isApplicable = isApplicableTaxOnGroup(taxCode, groupCode);
				    if (isApplicable)
				    {
					double taxWiseGroupTotal = funGetTaxWiseGroupTotal(strBillNo, taxCode, mapBillWiseGroupTaxSettlementData);
					clsManagerReportBean objTaxDtl = mapBillWiseGroupTaxSettlementData.get(taxCode);
					double totalTaxAmtForGroup = objTaxDtl.getDblTaxAmt();

					if (taxWiseGroupTotal > 0)
					{
					    taxAmt = (totalTaxAmtForGroup / taxWiseGroupTotal) * groupNetTotal;
					}
				    }
				    //pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(Math.rint(taxAmt)) + "|", labelTaxDesc.length(), "Right"));
				}
			    }
			    //pw.println();
			    for (int i = 0; i < lineCount; i++)
			    {
				//pw.print("-");
			    }
			    //pw.println();
			    //pw.println();

			}
		    }
		    else
		    {
			continue;
		    }
		}
		/**
		 * print total line
		 */
		//pw.println();
//                for (int i = 0; i < maxLineCount; i++)
//                {
//                    ////pw.print("-");
//                }
		////pw.println();
		////pw.print(objUtility.funPrintTextWithAlignment(strBillNo + " TOTALS", labelSettlement.length(), "Left"));
		//pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(Math.rint(totalGroupNetTotal)) + "|", maxGroupNameLength, "Right"));
		double BillTotal = totalGroupNetTotal;
		if (mapBillWiseTaxeNames != null)
		{
		    for (Map.Entry<String, String> entryTaxNames : mapBillWiseTaxeNames.entrySet())
		    {
			String taxCode = entryTaxNames.getKey();
			String taxName = entryTaxNames.getValue();

			String labelTaxDesc = "  " + taxName + "|";

			clsManagerReportBean objTaxDtl = mapBillWiseGroupTaxSettlementData.get(taxCode);
			double totalTaxAmtForGroup = objTaxDtl.getDblTaxAmt();
			BillTotal += totalTaxAmtForGroup;
			//pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(Math.rint(totalTaxAmtForGroup)) + "|", labelTaxDesc.length(), "Right"));
		    }
		}

		//pw.print(objUtility.funPrintTextWithAlignment(Math.rint(BillTotal) + "|", horizontalTotalLabel.length(), "Center"));
		//pw.println();
//                for (int i = 0; i < maxLineCount; i++)
//                {
//                    //pw.print("-");
//                }
		//pw.println();
		//pw.println();
	    }
	}

	DecimalFormat decimalFormat2Decimal = new DecimalFormat("0.00");

	if ("Total After Tip Amount".length() > maxSettlementNameLength)
	{
	    maxSettlementNameLength = "Total After Tip Amount".length();
	}

	Iterator<String> keyIterator = mapTaxNameWithLength.keySet().iterator();
	while (keyIterator.hasNext())
	{
	    String taxCode = keyIterator.next();
	    if (taxCode == null || taxCode.isEmpty())
	    {
		keyIterator.remove();
	    }
	}

	maxLineCount = maxSettlementNameLength + (maxGroupNameLength * 2) + (maxTaxNameLength * mapTaxNameWithLength.size()) + maxSettlementNameLength + 15;

	for (int i = 0; i < maxLineCount; i++)
	{
	    pw.print("-");
	}

	String settlementLabelForPrinting = "Settlement              ";
	for (int i = settlementLabelForPrinting.length(); i < maxSettlementNameLength; i++)
	{
	    settlementLabelForPrinting += " ";
	}

	pw.println();
	pw.print(objUtility.funPrintTextWithAlignment(settlementLabelForPrinting + "|", maxSettlementNameLength, "left"));
	pw.print(objUtility.funPrintTextWithAlignment("Tax Category" + "|", maxGroupNameLength, "left"));
	pw.print(objUtility.funPrintTextWithAlignment("Sale Amt" + "|", maxSettlementNameLength, "right"));
	for (Map.Entry<String, Integer> taxEntry : mapTaxNameWithLength.entrySet())
	{
	    String taxName = taxEntry.getKey();
	    pw.print(objUtility.funPrintTextWithAlignment(taxName + "|", maxTaxNameLength, "right"));
	}
	pw.print(objUtility.funPrintTextWithAlignment("Total" + "|", maxSettlementNameLength, "right"));
//	pw.println();
//	for (int i = 0; i < maxLineCount; i++)
//	{
//	    pw.print("-");
//	}
//	pw.println();

	double finalNetTotal = 0.00, finalSettlementTotal = 0.00;
	Map<String, Double> mapFinalTaxTotal = new TreeMap<String, Double>();
	mapFinalTaxTotal.put("Total", 0.00);

	/**
	 * Due to the round off issues in final settlement amount which is
	 * calculated by percentage calculation on taxes and groups giving
	 * difference in round off so this logic
	 */
	StringBuilder sbSqlLive = new StringBuilder();
	StringBuilder sqlFilter = new StringBuilder();

	Map<String, clsBillItemDtlBean> mapSettlementModes = new HashMap<>();
	double grossRevenue = 0;

	sbSqlLive.setLength(0);
	sbSqlLive.append("select ifnull(c.strPosCode,'All'),a.strSettelmentDesc, ifnull(SUM(b.dblSettlementAmt),0.00) "
		+ ",ifnull(d.strposname,'All'), if(c.strPOSCode is null,0,COUNT(*)) "
		+ "from tblsettelmenthd a "
		+ "left outer join tblbillsettlementdtl b on a.strSettelmentCode=b.strSettlementCode and date(b.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
		+ "left outer join tblbillhd c on b.strBillNo=c.strBillNo and date(b.dteBillDate)=date(c.dteBillDate) "
		+ "left outer join tblposmaster d on c.strPOSCode=d.strPosCode ");

	sbSqlQFile.setLength(0);
	sbSqlQFile.append("select ifnull(c.strPosCode,'All'),a.strSettelmentDesc, ifnull(SUM(b.dblSettlementAmt),0.00) "
		+ ",ifnull(d.strposname,'All'), if(c.strPOSCode is null,0,COUNT(*)) "
		+ "from tblsettelmenthd a "
		+ "left outer join tblqbillsettlementdtl b on a.strSettelmentCode=b.strSettlementCode and date(b.dteBillDate) BETWEEN '" + fromDate + "' AND '" + toDate + "' "
		+ "left outer join tblqbillhd c on b.strBillNo=c.strBillNo and date(b.dteBillDate)=date(c.dteBillDate) "
		+ "left outer join tblposmaster d on c.strPOSCode=d.strPosCode ");

	sqlFilter.append(" where a.strSettelmentType!='Complementary' "
		+ "and a.strApplicable='Yes' ");

	if (!"All".equalsIgnoreCase(posCode))
	{
	    sqlFilter.append("and  c.strPosCode='" + posCode + "' ");
	}
	sqlFilter.append("group by a.strSettelmentCode "
		+ "order by b.dblSettlementAmt desc ");

	sbSqlLive.append(sqlFilter);
	sbSqlQFile.append(sqlFilter);

	ResultSet rsData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLive.toString());
	while (rsData.next())
	{
	    String settlementName = rsData.getString(2);
	    if (mapSettlementModes.containsKey(settlementName))
	    {
		clsBillItemDtlBean obj = mapSettlementModes.get(settlementName);

		obj.setDblSettlementAmt(obj.getDblSettlementAmt() + rsData.getDouble(3));
		obj.setNoOfBills(obj.getNoOfBills() + rsData.getInt(5));

	    }
	    else
	    {
		clsBillItemDtlBean obj = new clsBillItemDtlBean();
		obj.setStrPosCode(rsData.getString(1));
		obj.setStrSettelmentMode(settlementName);
		obj.setDblSettlementAmt(rsData.getDouble(3));
		obj.setStrPosName(rsData.getString(4));
		obj.setNoOfBills(rsData.getInt(5));

		mapSettlementModes.put(settlementName, obj);

	    }

	    grossRevenue += rsData.getDouble(3);

	}

	rsData = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlQFile.toString());
	while (rsData.next())
	{
	    String settlementName = rsData.getString(2);
	    if (mapSettlementModes.containsKey(settlementName))
	    {
		clsBillItemDtlBean obj = mapSettlementModes.get(settlementName);

		obj.setDblSettlementAmt(obj.getDblSettlementAmt() + rsData.getDouble(3));
		obj.setNoOfBills(obj.getNoOfBills() + rsData.getInt(5));

	    }
	    else
	    {
		clsBillItemDtlBean obj = new clsBillItemDtlBean();
		obj.setStrPosCode(rsData.getString(1));
		obj.setStrSettelmentMode(settlementName);
		obj.setDblSettlementAmt(rsData.getDouble(3));
		obj.setStrPosName(rsData.getString(4));
		obj.setNoOfBills(rsData.getInt(5));

		mapSettlementModes.put(settlementName, obj);

	    }

	    grossRevenue += rsData.getDouble(3);
	}

	for (Map.Entry<String, Map<String, Double>> settlementEntry : mapSettelemtWiseGroupBreakup.entrySet())
	{
	    String settlementName = settlementEntry.getKey();

	    double settlementWiseNetTotal = 0.00;
	    Map<String, Double> mapSettlementWiseTaxTotal = new TreeMap<String, Double>();
	    mapSettlementWiseTaxTotal.put("Total", 0.00);

	    Map<String, Double> mapGroupBreakup = settlementEntry.getValue();

	    String settlementNameForPrinting = settlementName;
	    for (int i = settlementName.length(); i < maxSettlementNameLength; i++)
	    {
		settlementNameForPrinting += " ";
	    }

	    pw.println();
	    pw.print(objUtility.funPrintTextWithAlignment(settlementNameForPrinting, maxSettlementNameLength, "left"));

	    for (Map.Entry<String, Integer> groupEntry : mapGroupNameWithLength.entrySet())
	    {
		double total = 0;
		String groupName = groupEntry.getKey();
		int groupNameLength = groupEntry.getValue();

		double groupNetTotal = 0.00;
		if (mapGroupBreakup.containsKey(groupName))
		{
		    groupNetTotal = mapGroupBreakup.get(groupName);
		}

		total = total + groupNetTotal;
		if (!settlementName.equalsIgnoreCase("Total"))
		{
		    settlementWiseNetTotal += groupNetTotal;
		    finalNetTotal += groupNetTotal;
		}

		String emptySettlementNameForPrinting = "";
		for (int i = emptySettlementNameForPrinting.length(); i < maxSettlementNameLength; i++)
		{
		    emptySettlementNameForPrinting += " ";
		}

		String groupNameForPrinting = groupName;
		for (int i = groupName.length(); i < maxGroupNameLength; i++)
		{
		    groupNameForPrinting += " ";
		}

		pw.println();
		pw.print(objUtility.funPrintTextWithAlignment(emptySettlementNameForPrinting + "|", maxSettlementNameLength, "right"));
		pw.print(objUtility.funPrintTextWithAlignment(groupNameForPrinting + "|", maxGroupNameLength, "left"));
		pw.print(objUtility.funPrintTextWithAlignment(decimalFormat2Decimal.format(groupNetTotal) + "|", maxSettlementNameLength, "right"));

		if (mapSettelemtWiseTaxBreakup.containsKey(settlementName))
		{
		    Map<String, Double> mapTaxBreakup = mapSettelemtWiseTaxBreakup.get(settlementName);

		    for (Map.Entry<String, Integer> taxEntry : mapTaxNameWithLength.entrySet())
		    {
			String taxName = taxEntry.getKey();
			String key = settlementName + "!" + groupName + "!" + taxName;

			double taxAmt = 0.00;
			if (mapTaxBreakup.containsKey(key))
			{
			    taxAmt = mapTaxBreakup.get(key);
			}

			total = total + taxAmt;

			if (mapSettlementWiseTaxTotal.containsKey(taxName))
			{
			    mapSettlementWiseTaxTotal.put(taxName, mapSettlementWiseTaxTotal.get(taxName) + taxAmt);
			}
			else
			{
			    mapSettlementWiseTaxTotal.put(taxName, taxAmt);
			}

			if (!settlementName.equalsIgnoreCase("Total"))
			{
			    if (mapFinalTaxTotal.containsKey(taxName))
			    {
				mapFinalTaxTotal.put(taxName, mapFinalTaxTotal.get(taxName) + taxAmt);
			    }
			    else
			    {
				mapFinalTaxTotal.put(taxName, taxAmt);
			    }
			}

			pw.print(objUtility.funPrintTextWithAlignment(decimalFormat2Decimal.format(taxAmt) + "|", maxTaxNameLength, "right"));
		    }

		}
		else
		{
		    continue;
		}

		pw.print(objUtility.funPrintTextWithAlignment(decimalFormat2Decimal.format(total) + "|", maxSettlementNameLength, "right"));
	    }
	    //print settlement wise total

	    if (!settlementName.equalsIgnoreCase("Total"))
	    {
		double finalSettlementWiseTotal = settlementWiseNetTotal;

		String settlementTotalForPrinting = "Total " + settlementName;
		for (int i = settlementTotalForPrinting.length(); i < maxSettlementNameLength; i++)
		{
		    settlementTotalForPrinting += " ";
		}

		String emptyGroupNameForPrinting = "";
		for (int i = emptyGroupNameForPrinting.length(); i < maxGroupNameLength; i++)
		{
		    emptyGroupNameForPrinting += " ";
		}

		pw.println();
		pw.print(objUtility.funPrintTextWithAlignment(settlementTotalForPrinting + "|", maxSettlementNameLength, "left"));
		pw.print(objUtility.funPrintTextWithAlignment(emptyGroupNameForPrinting + "|", maxGroupNameLength, "left"));
		pw.print(objUtility.funPrintTextWithAlignment(decimalFormat2Decimal.format(settlementWiseNetTotal) + "|", maxSettlementNameLength, "right"));
		for (Map.Entry<String, Integer> taxEntry : mapTaxNameWithLength.entrySet())
		{
		    String taxName = taxEntry.getKey();
		    double settlementWiseTaxAmt = 0.00;
		    if (mapSettlementWiseTaxTotal.containsKey(taxName))
		    {
			settlementWiseTaxAmt = mapSettlementWiseTaxTotal.get(taxName);
		    }
		    finalSettlementWiseTotal += settlementWiseTaxAmt;
		    pw.print(objUtility.funPrintTextWithAlignment(decimalFormat2Decimal.format(settlementWiseTaxAmt) + "|", maxTaxNameLength, "right"));
		}

		double roundOffForThisSettlement = (totalRoundOffAmt / totalSettleAmt) * finalSettlementWiseTotal;
		finalSettlementWiseTotal = finalSettlementWiseTotal + roundOffForThisSettlement;

		double settlementAmount = mapSettlementModes.get(settlementName).getDblSettlementAmt();

		pw.print(objUtility.funPrintTextWithAlignment(decimalFormat2Decimal.format(settlementAmount) + "|", maxSettlementNameLength, "right"));
		pw.println();

	    }
	}
	//print grand total
	double finalTotalSettlementAmounr = finalNetTotal;

	String totalForPrinting = "Total";
	for (int i = totalForPrinting.length(); i < maxSettlementNameLength; i++)
	{
	    totalForPrinting += " ";
	}

	String emptyGroupNameForPrinting = "";
	for (int i = emptyGroupNameForPrinting.length(); i < maxGroupNameLength; i++)
	{
	    emptyGroupNameForPrinting += " ";
	}

	String emptyNetTotalForPrinting = "";
	for (int i = emptyNetTotalForPrinting.length(); i < maxGroupNameLength; i++)
	{
	    emptyNetTotalForPrinting += " ";
	}

	String emptyTaxNameForPrinting = "";
	for (int i = emptyTaxNameForPrinting.length(); i < maxTaxNameLength; i++)
	{
	    emptyTaxNameForPrinting += " ";
	}

	String roundOffForPrinting = "Round Off";
	for (int i = "Round Off".length(); i < maxSettlementNameLength; i++)
	{
	    roundOffForPrinting += " ";
	}

	String tipForPrinting = "Tip Amount";
	for (int i = "Tip Amount".length(); i < maxSettlementNameLength; i++)
	{
	    tipForPrinting += " ";
	}

	String totalAfterTipForPrinting = "Total After Tip Amount";
	for (int i = totalAfterTipForPrinting.length(); i < maxSettlementNameLength; i++)
	{
	    totalAfterTipForPrinting += " ";
	}

	pw.println();
	pw.print(objUtility.funPrintTextWithAlignment(totalForPrinting + "|", maxSettlementNameLength, "left"));
	pw.print(objUtility.funPrintTextWithAlignment(emptyGroupNameForPrinting + "|", maxGroupNameLength, "right"));
	pw.print(objUtility.funPrintTextWithAlignment(decimalFormat2Decimal.format(finalNetTotal) + "|", maxSettlementNameLength, "right"));
	for (Map.Entry<String, Integer> taxEntry : mapTaxNameWithLength.entrySet())
	{
	    String taxName = taxEntry.getKey();
	    double settlementWiseTaxAmt = 0.00;
	    if (mapFinalTaxTotal.containsKey(taxName))
	    {
		settlementWiseTaxAmt = mapFinalTaxTotal.get(taxName);
	    }
	    finalTotalSettlementAmounr += settlementWiseTaxAmt;
	    pw.print(objUtility.funPrintTextWithAlignment(decimalFormat2Decimal.format(settlementWiseTaxAmt) + "|", maxTaxNameLength, "right"));
	}

	pw.print(objUtility.funPrintTextWithAlignment(decimalFormat2Decimal.format(finalTotalSettlementAmounr + totalRoundOffAmt) + "|", maxSettlementNameLength, "right"));
//	pw.println();
//	for (int i = 0; i < maxLineCount; i++)
//	{
//	    pw.print("-");
//	}

//	pw.println();
//	pw.print(objUtility.funPrintTextWithAlignment(roundOffForPrinting + "|", maxSettlementNameLength, "left"));
//	pw.print(objUtility.funPrintTextWithAlignment(emptyGroupNameForPrinting + "|", maxGroupNameLength, "right"));
//	pw.print(objUtility.funPrintTextWithAlignment(emptyNetTotalForPrinting + "|", maxSettlementNameLength, "right"));
//	for (Map.Entry<String, Integer> taxEntry : mapTaxNameWithLength.entrySet())
//	{
//	    String taxName = taxEntry.getKey();
//
//	    pw.print(objUtility.funPrintTextWithAlignment("" + "|", maxTaxNameLength, "right"));
//	}
//	pw.print(objUtility.funPrintTextWithAlignment(decimalFormat2Decimal.format(totalRoundOffAmt) + "|", maxSettlementNameLength, "right"));
//
//	pw.println();
//	pw.print(objUtility.funPrintTextWithAlignment(totalForPrinting + "|", maxSettlementNameLength, "left"));
//	pw.print(objUtility.funPrintTextWithAlignment(emptyGroupNameForPrinting + "|", maxGroupNameLength, "right"));
//	pw.print(objUtility.funPrintTextWithAlignment(emptyNetTotalForPrinting + "|", maxSettlementNameLength, "right"));
//	for (Map.Entry<String, Integer> taxEntry : mapTaxNameWithLength.entrySet())
//	{
//	    String taxName = taxEntry.getKey();
//
//	    pw.print(objUtility.funPrintTextWithAlignment("" + "|", maxTaxNameLength, "right"));
//	}
//	pw.print(objUtility.funPrintTextWithAlignment(decimalFormat2Decimal.format(Math.rint(finalTotalSettlementAmounr + totalRoundOffAmt)) + "|", maxSettlementNameLength, "right"));
//
//	pw.println();
//	pw.print(objUtility.funPrintTextWithAlignment(tipForPrinting + "|", maxSettlementNameLength, "left"));
//	pw.print(objUtility.funPrintTextWithAlignment(emptyGroupNameForPrinting + "|", maxGroupNameLength, "right"));
//	pw.print(objUtility.funPrintTextWithAlignment(emptyNetTotalForPrinting + "|", maxSettlementNameLength, "right"));
//	for (Map.Entry<String, Integer> taxEntry : mapTaxNameWithLength.entrySet())
//	{
//	    String taxName = taxEntry.getKey();
//
//	    pw.print(objUtility.funPrintTextWithAlignment("" + "|", maxTaxNameLength, "right"));
//	}
//	pw.print(objUtility.funPrintTextWithAlignment(decimalFormat2Decimal.format(totalTipAmt) + "|", maxSettlementNameLength, "right"));
//
//	pw.println();
//	pw.print(objUtility.funPrintTextWithAlignment(totalAfterTipForPrinting + "|", maxSettlementNameLength, "left"));
//	pw.print(objUtility.funPrintTextWithAlignment(emptyGroupNameForPrinting + "|", maxGroupNameLength, "right"));
//	pw.print(objUtility.funPrintTextWithAlignment(emptyNetTotalForPrinting + "|", maxSettlementNameLength, "right"));
//	for (Map.Entry<String, Integer> taxEntry : mapTaxNameWithLength.entrySet())
//	{
//	    String taxName = taxEntry.getKey();
//	    pw.print(objUtility.funPrintTextWithAlignment("" + "|", maxTaxNameLength, "right"));
//	}
//	pw.print(objUtility.funPrintTextWithAlignment(decimalFormat2Decimal.format(Math.rint(finalTotalSettlementAmounr + totalRoundOffAmt + totalTipAmt)) + "|", maxSettlementNameLength, "right"));
	pw.println();
	pw.println();
	pw.println();
	pw.println();
	pw.println();
	pw.println();

	pw.println("End Of Gross Sales Summary");

	return 1;
    }

    private int funGetLineCount(String billNo, String labelSettlement, String labelGroupName, String horizontalTotalLabel, Map<String, Map<String, clsManagerReportBean>> mapBillWiseData, Map<String, Map<String, String>> mapBillWiseSettlemetNames, Map<String, Map<String, String>> mapBillWiseTaxNames)
    {

	StringBuilder stringBuilder = new StringBuilder();
	stringBuilder.append(labelSettlement);
	stringBuilder.append(labelGroupName);

	Map<String, String> map = mapBillWiseTaxNames.get(billNo);
	if (map != null)
	{
	    for (String taxDesc : map.values())
	    {
		String labelTaxDesc = taxDesc + "|";
		stringBuilder.append(labelTaxDesc);
	    }
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

    private double funGetTaxWiseGroupTotal(String billDate, String taxCode, Map<String, clsManagerReportBean> mapBillWiseGroupTaxSettlementData)
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

		if (mapBillWiseGroupTaxSettlementData.containsKey(groupCode))
		{
		    clsManagerReportBean objGroupDtl = mapBillWiseGroupTaxSettlementData.get(groupCode);
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
}
