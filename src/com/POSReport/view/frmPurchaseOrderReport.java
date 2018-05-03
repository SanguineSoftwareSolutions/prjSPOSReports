/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.POSReport.view;

import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsPosConfigFile;
import com.POSGlobal.controller.clsPurchaseOrderDtl;
import com.POSGlobal.controller.clsPurchaseOrderHd;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.Timer;

public class frmPurchaseOrderReport extends javax.swing.JFrame
{

    String fromDate, toDate, insertQuery, updateQuery, imagePath;
    private clsUtility objUtility;
    private Map<String, String> hmSupplier;
    private StringBuilder sb = new StringBuilder();
    private clsUtility2 objUtility2;

    /**
     * this Function is used for Component initialization
     */
    public frmPurchaseOrderReport()
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

	imagePath = System.getProperty("user.dir");
	imagePath = imagePath + File.separator + "ReportImage";
	funFillSupplierCombo();
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
     *
     * this function is used Filling POS Code ComboBoxs
     */
    private void funFillSupplierCombo()
    {
	try
	{
	    hmSupplier = new HashMap<String, String>();
	    cmbSupplier.addItem("All");
	    hmSupplier.put("All", "All");
	    sb.setLength(0);
	    sb.append("select strSupplierName,strSupplierCode from tblsuppliermaster");
	    ResultSet rs = clsGlobalVarClass.dbMysql.executeResultSet(sb.toString());
	    while (rs.next())
	    {
		cmbSupplier.addItem(rs.getString(1));
		hmSupplier.put(rs.getString(1), rs.getString(2));
	    }
	    rs.close();
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
	    String supplierCode = hmSupplier.get(cmbSupplier.getSelectedItem().toString());

	    funGenerateTextFile(fromDate, toDate, supplierCode, btnClick);
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
	    String posCode = cmbSupplier.getSelectedItem().toString();

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
        pnlbackground = new JPanel() {
            public void paintComponent(Graphics g) {
                Image img = Toolkit.getDefaultToolkit().getImage(
                    getClass().getResource("/com/POSReport/images/imgBGJPOS.png"));
                g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), this);
            }
        };

        ;
        pnlMain = new javax.swing.JPanel();
        pnlAPC = new javax.swing.JPanel();
        lblSupplierCode = new javax.swing.JLabel();
        cmbSupplier = new javax.swing.JComboBox();
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
        lblProductName.setText("SPOS -");
        pnlheader.add(lblProductName);

        lblModuleName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblModuleName.setForeground(new java.awt.Color(255, 255, 255));
        pnlheader.add(lblModuleName);

        lblformName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblformName.setForeground(new java.awt.Color(255, 255, 255));
        lblformName.setText("Purchase Order Report");
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

        lblSupplierCode.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblSupplierCode.setText("Supplier    :");
        pnlAPC.add(lblSupplierCode);
        lblSupplierCode.setBounds(250, 120, 90, 30);

        cmbSupplier.setToolTipText("Select POS");
        pnlAPC.add(cmbSupplier);
        cmbSupplier.setBounds(340, 120, 150, 30);

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
        btnView.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
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
        pnlAPC.add(btnClose);
        btnClose.setBounds(670, 500, 97, 41);

        lblAPC.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        lblAPC.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblAPC.setText("Purchase Order Report");
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
        btnSendOnMail.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
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
	clsGlobalVarClass.hmActiveForms.remove("Purchase Order Report");
    }//GEN-LAST:event_btnCloseActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("Purchase Order Report");
    }//GEN-LAST:event_formWindowClosed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("Purchase Order Report");
    }//GEN-LAST:event_formWindowClosing


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnSendOnMail;
    private javax.swing.JButton btnView;
    private javax.swing.JComboBox cmbSupplier;
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
    private javax.swing.JLabel lblSupplierCode;
    private javax.swing.JLabel lblToDate;
    private javax.swing.JLabel lblUserCode;
    private javax.swing.JLabel lblformName;
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
	    File file = new File(filePath + File.separator + "Temp" + File.separator + "Purchase_Order_Report.txt");
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
	    pw.println("Report : Purchase Order Report");
	    pw.println("Reporting Date:" + "  " + fromDate + " " + "To" + " " + toDate);
	    pw.println();
	    pw.println(dashedLineOf150Chars);//line

	    //settlement break up
	    funPurchaseOrderReportfunSettlementWiseData(fromDate, toDate, posCode, pw);

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

    private int funPurchaseOrderReportfunSettlementWiseData(String fromDate, String toDate, String supplierCode, PrintWriter pw) throws Exception
    {
	String dashedLineOf150Chars = "------------------------------------------------------------------------------------------------------------------------------------------------------";

	pw.println();
	pw.println(dashedLineOf150Chars);

	StringBuilder sbSqlLiveFile = new StringBuilder();

	double totalTaxAmt = 0.00;
	double totalSubTotal = 0.00;
	double totalGrandTotal = 0.00;
	Map<String, Map<String, clsPurchaseOrderHd>> hmPurchaseOrderData = new HashMap<String, Map<String, clsPurchaseOrderHd>>();

	sbSqlLiveFile.setLength(0);
	sbSqlLiveFile.append(" select a.strPOCode,a.dtePODate,a.dteDeliveryDate,d.strSupplierName,d.intMobileNumber "
		+ " ,c.strItemName,b.dblOrderQty,b.dblPurchaseRate,b.dblAmount,a.dblSubTotal,a.dblTaxAmt,a.dblGrandTotal "
		+ " ,a.dblExtraAmt "
		+ " from tblpurchaseorderhd a,tblpurchaseorderdtl b,tblitemmaster c,tblsuppliermaster d "
		+ " where a.strPOCode=b.strPOCode and b.strItemCode=c.strItemCode and a.strSupplierCode=d.strSupplierCode "
		+ " and a.strClientCode=b.strClientCode and b.strClientCode=c.strClientCode "
		+ " and a.strClientCode=b.strClientCode and date(a.dteDeliveryDate) between '" + fromDate + "' and '" + toDate + "' ");
	if (!supplierCode.equalsIgnoreCase("All"))
	{
	    sbSqlLiveFile.append(" and a.strSupplierCode='" + supplierCode + "' ");
	}
	System.out.println(sbSqlLiveFile);

	ResultSet rsPOreport = clsGlobalVarClass.dbMysql.executeResultSet(sbSqlLiveFile.toString());
	while (rsPOreport.next())
	{
	    String dtPODate = rsPOreport.getString(2).split(" ")[0];
	    String dtDelDate = rsPOreport.getString(3).split(" ")[0];
	    String PODate = dtPODate.split("-")[2] + "-" + dtPODate.split("-")[1] + "-" + dtPODate.split("-")[0];
	    String deliveryDate = rsPOreport.getString(3).split("-")[2] + "-" + dtDelDate.split("-")[1] + "-" + dtDelDate.split("-")[0];
	    String POCode = rsPOreport.getString(1);
	    String supplierName = rsPOreport.getString(4);

	    clsPurchaseOrderDtl objPurchaseOrderDtl = new clsPurchaseOrderDtl();
	    objPurchaseOrderDtl.setItemName(rsPOreport.getString(6));
	    objPurchaseOrderDtl.setOrderQty(rsPOreport.getDouble(7));
	    objPurchaseOrderDtl.setPurchaseRate(rsPOreport.getDouble(8));
	    objPurchaseOrderDtl.setAmount(rsPOreport.getDouble(9));

	    if (hmPurchaseOrderData.containsKey(supplierName))
	    {
		Map<String, clsPurchaseOrderHd> hmPOData = hmPurchaseOrderData.get(supplierName);

		if (hmPOData.containsKey(POCode))
		{
		    clsPurchaseOrderHd objPurchaseOrderHd = hmPOData.get(POCode);
		    objPurchaseOrderHd.getListPurchaseOrderDtl().add(objPurchaseOrderDtl);
		}
		else
		{
		    clsPurchaseOrderHd objPurchaseOrderHd = new clsPurchaseOrderHd();
		    objPurchaseOrderHd.setPOCode(POCode);
		    objPurchaseOrderHd.setPODate(PODate);
		    objPurchaseOrderHd.setDeliveryDate(deliveryDate);

		    List<clsPurchaseOrderDtl> listPurOrderDtl = new ArrayList<clsPurchaseOrderDtl>();
		    listPurOrderDtl.add(objPurchaseOrderDtl);
		    objPurchaseOrderHd.setListPurchaseOrderDtl(listPurOrderDtl);
		    hmPOData.put(POCode, objPurchaseOrderHd);

		    totalSubTotal += rsPOreport.getDouble(10);
		    totalTaxAmt += rsPOreport.getDouble(11);
		    totalGrandTotal += rsPOreport.getDouble(12);
		}
		hmPurchaseOrderData.put(supplierName, hmPOData);
	    }
	    else
	    {
		Map<String, clsPurchaseOrderHd> hmPOData = new HashMap<String, clsPurchaseOrderHd>();

		clsPurchaseOrderHd objPurchaseOrderHd = new clsPurchaseOrderHd();
		objPurchaseOrderHd.setPOCode(POCode);
		objPurchaseOrderHd.setPODate(PODate);
		objPurchaseOrderHd.setDeliveryDate(deliveryDate);

		List<clsPurchaseOrderDtl> listPurOrderDtl = new ArrayList<clsPurchaseOrderDtl>();
		listPurOrderDtl.add(objPurchaseOrderDtl);
		objPurchaseOrderHd.setListPurchaseOrderDtl(listPurOrderDtl);
		hmPOData.put(POCode, objPurchaseOrderHd);

		hmPurchaseOrderData.put(supplierName, hmPOData);

		totalSubTotal += rsPOreport.getDouble(10);
		totalTaxAmt += rsPOreport.getDouble(11);
		totalGrandTotal += rsPOreport.getDouble(12);
	    }
	}

	for (Map.Entry<String, Map<String, clsPurchaseOrderHd>> entry : hmPurchaseOrderData.entrySet())
	{
	    pw.print(objUtility.funPrintTextWithAlignment("Supplier Name  ", 15, "Left"));
	    pw.print(objUtility.funPrintTextWithAlignment(entry.getKey(), entry.getKey().length(), "Left"));
	    pw.println();
	    Map<String, clsPurchaseOrderHd> hmPOData = entry.getValue();

	    pw.println(dashedLineOf150Chars);

	    pw.print(objUtility.funPrintTextWithAlignment(" ", 15, "Left"));
	    pw.print(objUtility.funPrintTextWithAlignment("Item Name", 15, "Left"));
	    pw.print(objUtility.funPrintTextWithAlignment("Quantity", 15, "Right"));
	    pw.print(objUtility.funPrintTextWithAlignment("Purchase Rate", 15, "Right"));
	    pw.print(objUtility.funPrintTextWithAlignment("Amount", 15, "Right"));
	    pw.println();
	    pw.println();

	    for (Map.Entry<String, clsPurchaseOrderHd> entryPO : hmPOData.entrySet())
	    {
		pw.println(dashedLineOf150Chars);

		pw.print(objUtility.funPrintTextWithAlignment("PO Code ", 15, "Left"));
		pw.print(objUtility.funPrintTextWithAlignment(entryPO.getValue().getPOCode(), entryPO.getValue().getPOCode().length(), "Left"));
		pw.println();

		pw.print(objUtility.funPrintTextWithAlignment("PO Date ", 15, "Left"));
		pw.print(objUtility.funPrintTextWithAlignment(entryPO.getValue().getPODate(), entryPO.getValue().getPODate().length(), "Left"));
		pw.println();

		pw.print(objUtility.funPrintTextWithAlignment("Delivery Date ", 15, "Left"));
		pw.print(objUtility.funPrintTextWithAlignment(entryPO.getValue().getDeliveryDate(), entryPO.getValue().getDeliveryDate().length(), "Left"));
		pw.println();
		pw.println();

		double subTotal = 0, taxAmt = 0, grandTotal = 0, totalQty = 0;

		for (clsPurchaseOrderDtl objPurOrderDtl : entryPO.getValue().getListPurchaseOrderDtl())
		{
		    pw.print(objUtility.funPrintTextWithAlignment(" ", 15, "Left"));
		    pw.print(objUtility.funPrintTextWithAlignment(objPurOrderDtl.getItemName(), 15, "Left"));
		    pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(objPurOrderDtl.getOrderQty()), 15, "Right"));
		    pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(objPurOrderDtl.getPurchaseRate()), 15, "Right"));
		    pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(objPurOrderDtl.getAmount()), 15, "Right"));
		    pw.println();
		    totalQty += objPurOrderDtl.getOrderQty();
		    subTotal += objPurOrderDtl.getAmount();
		}
		pw.print(objUtility.funPrintTextWithAlignment(" ", 15, "Left"));
		pw.println("-----------------------------------------------------------------------------------------------------------------");

		pw.print(objUtility.funPrintTextWithAlignment("Total", 15, "Left"));
		pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(totalQty), 30, "Right"));
		pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(subTotal), 30, "Right"));
		pw.println();
	    }
	}

	pw.println(dashedLineOf150Chars);
	pw.print(objUtility.funPrintTextWithAlignment("Sub Total", 15, "Left"));
	pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(totalSubTotal), 15, "Right"));
	pw.println();
	pw.print(objUtility.funPrintTextWithAlignment("Tax Amt", 15, "Left"));
	pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(totalTaxAmt), 15, "Right"));
	pw.println();
	pw.print(objUtility.funPrintTextWithAlignment("Grand Total", 15, "Left"));
	pw.print(objUtility.funPrintTextWithAlignment(String.valueOf(totalGrandTotal), 15, "Right"));
	pw.println();

	return 1;

    }
}
