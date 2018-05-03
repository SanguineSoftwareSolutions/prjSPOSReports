/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.POSReport.view;

import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsPosConfigFile;
import com.POSGlobal.controller.clsUtility;
import com.POSGlobal.view.frmShowTextFile;

import com.POSReport.controller.clsAPCReport;
import com.POSReport.controller.comparator.clsBillComparator;
import com.POSReport.controller.clsBillItemDtlBean;
import com.POSReport.controller.clsKOTAnalysisBean;
import com.POSReport.controller.comparator.clsWaiterWiseAPCComparator;
import com.POSReport.controller.comparator.clsWaiterWiseSalesComparator;
import java.awt.Dimension;
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
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.swing.JRViewer;

/**
 *
 * @author sss11
 */
public class frmFoodCosting extends javax.swing.JFrame
{

    String fromDate, toDate, imagePath;
    private clsUtility objUtility;
    private StringBuilder sb = new StringBuilder();
    List<clsAPCReport> listOfDtl = new LinkedList<clsAPCReport>();
    HashMap hm = new HashMap();
    double dinningAmt = 0.00;

    /**
     * this Function is used for Component initialization
     */
    public frmFoodCosting()
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
	clsPosConfigFile pc = new clsPosConfigFile();
	imagePath = System.getProperty("user.dir");
	imagePath = imagePath + "\\ReportImage";
	fillComboBox();
	funSetFormToInDateChosser();

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
		cmbPosCode.addItem(clsGlobalVarClass.gPOSName + " " + clsGlobalVarClass.gPOSCode);
	    }
	    else
	    {
		cmbPosCode.addItem("All");
		sb.setLength(0);
		sb.append("select strPosName,strPosCode from tblposmaster");
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
     * this Function is used for Set Form To Date chooser
     */
    private void funSetFormToInDateChosser()
    {

	dteFromDate.setDate(objUtility.funGetDateToSetCalenderDate());
	dteToDate.setDate(objUtility.funGetDateToSetCalenderDate());

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
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        lblAPC = new javax.swing.JLabel();
        lblReportType = new javax.swing.JLabel();
        cmbReportType = new javax.swing.JComboBox();

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
        lblformName.setText("-APC");
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
        lblposCode.setBounds(250, 70, 90, 30);

        cmbPosCode.setToolTipText("Select POS");
        cmbPosCode.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                cmbPosCodeActionPerformed(evt);
            }
        });
        pnlAPC.add(cmbPosCode);
        cmbPosCode.setBounds(340, 70, 150, 30);

        lblFromDate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblFromDate.setText("From Date :");
        pnlAPC.add(lblFromDate);
        lblFromDate.setBounds(250, 120, 90, 29);

        dteFromDate.setToolTipText("Select From Date");
        dteFromDate.setPreferredSize(new java.awt.Dimension(119, 35));
        dteFromDate.addHierarchyListener(new java.awt.event.HierarchyListener()
        {
            public void hierarchyChanged(java.awt.event.HierarchyEvent evt)
            {
                dteFromDateHierarchyChanged(evt);
            }
        });
        dteFromDate.addPropertyChangeListener(new java.beans.PropertyChangeListener()
        {
            public void propertyChange(java.beans.PropertyChangeEvent evt)
            {
                dteFromDatePropertyChange(evt);
            }
        });
        pnlAPC.add(dteFromDate);
        dteFromDate.setBounds(340, 120, 150, 30);

        dteToDate.setToolTipText("Select To Date");
        dteToDate.setPreferredSize(new java.awt.Dimension(119, 35));
        dteToDate.addHierarchyListener(new java.awt.event.HierarchyListener()
        {
            public void hierarchyChanged(java.awt.event.HierarchyEvent evt)
            {
                dteToDateHierarchyChanged(evt);
            }
        });
        dteToDate.addPropertyChangeListener(new java.beans.PropertyChangeListener()
        {
            public void propertyChange(java.beans.PropertyChangeEvent evt)
            {
                dteToDatePropertyChange(evt);
            }
        });
        pnlAPC.add(dteToDate);
        dteToDate.setBounds(340, 170, 150, 30);

        lblToDate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblToDate.setText("To Date :");
        pnlAPC.add(lblToDate);
        lblToDate.setBounds(250, 170, 90, 30);

        jButton1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        jButton1.setText("VIEW");
        jButton1.setToolTipText("View Report");
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn2.png"))); // NOI18N
        jButton1.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                jButton1MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt)
            {
                jButton1MouseEntered(evt);
            }
        });
        pnlAPC.add(jButton1);
        jButton1.setBounds(510, 500, 96, 41);

        jButton2.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton2.setForeground(new java.awt.Color(255, 255, 255));
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        jButton2.setText("CLOSE");
        jButton2.setToolTipText("Close Window");
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn2.png"))); // NOI18N
        jButton2.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                jButton2MouseClicked(evt);
            }
        });
        jButton2.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton2ActionPerformed(evt);
            }
        });
        pnlAPC.add(jButton2);
        jButton2.setBounds(650, 500, 97, 41);

        lblAPC.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        lblAPC.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblAPC.setText("Food Costing Report");
        lblAPC.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        pnlAPC.add(lblAPC);
        lblAPC.setBounds(220, 10, 330, 30);

        lblReportType.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblReportType.setText("Report Type :");
        pnlAPC.add(lblReportType);
        lblReportType.setBounds(250, 210, 86, 33);

        cmbReportType.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        cmbReportType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Text File-40 Column Report" }));
        cmbReportType.setToolTipText("Select Date Wise");
        pnlAPC.add(cmbReportType);
        cmbReportType.setBounds(340, 210, 150, 33);

        javax.swing.GroupLayout pnlMainLayout = new javax.swing.GroupLayout(pnlMain);
        pnlMain.setLayout(pnlMainLayout);
        pnlMainLayout.setHorizontalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMainLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlAPC, javax.swing.GroupLayout.DEFAULT_SIZE, 796, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlMainLayout.setVerticalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlAPC, javax.swing.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE)
        );

        pnlbackground.add(pnlMain, new java.awt.GridBagConstraints());

        getContentPane().add(pnlbackground, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("Food Costing");
    }//GEN-LAST:event_formWindowClosed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("Food Costing");
    }//GEN-LAST:event_formWindowClosing

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton2ActionPerformed
    {//GEN-HEADEREND:event_jButton2ActionPerformed
	// TODO add your handling code here:
	dispose();
	objUtility = null;
	clsGlobalVarClass.hmActiveForms.remove("Food Costing");
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton2MouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_jButton2MouseClicked
    {//GEN-HEADEREND:event_jButton2MouseClicked
	// TODO add your handling code here:
	dispose();
	clsGlobalVarClass.hmActiveForms.remove("Food Costing");
    }//GEN-LAST:event_jButton2MouseClicked

    private void jButton1MouseEntered(java.awt.event.MouseEvent evt)//GEN-FIRST:event_jButton1MouseEntered
    {//GEN-HEADEREND:event_jButton1MouseEntered
	// TODO add your handling code here:
    }//GEN-LAST:event_jButton1MouseEntered

    /**
     * *
     * This Function Is Used Insert Data And Call Reports
     *
     * @param evt
     */
    private void jButton1MouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_jButton1MouseClicked
    {//GEN-HEADEREND:event_jButton1MouseClicked
	// TODO add your handling code here:
	try
	{
	    funFoodCostingWiseTextReport();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }//GEN-LAST:event_jButton1MouseClicked

    private void dteToDatePropertyChange(java.beans.PropertyChangeEvent evt)//GEN-FIRST:event_dteToDatePropertyChange
    {//GEN-HEADEREND:event_dteToDatePropertyChange
	// TODO add your handling code here:
	//ToDate();
    }//GEN-LAST:event_dteToDatePropertyChange

    private void dteToDateHierarchyChanged(java.awt.event.HierarchyEvent evt)//GEN-FIRST:event_dteToDateHierarchyChanged
    {//GEN-HEADEREND:event_dteToDateHierarchyChanged
	// TODO add your handling code here:
	//ToDate();
    }//GEN-LAST:event_dteToDateHierarchyChanged

    private void dteFromDatePropertyChange(java.beans.PropertyChangeEvent evt)//GEN-FIRST:event_dteFromDatePropertyChange
    {//GEN-HEADEREND:event_dteFromDatePropertyChange
	// TODO add your handling code here:
	//FromDate();
    }//GEN-LAST:event_dteFromDatePropertyChange

    private void dteFromDateHierarchyChanged(java.awt.event.HierarchyEvent evt)//GEN-FIRST:event_dteFromDateHierarchyChanged
    {//GEN-HEADEREND:event_dteFromDateHierarchyChanged
	// TODO add your handling code here:

	//FromDate();
    }//GEN-LAST:event_dteFromDateHierarchyChanged

    private void cmbPosCodeActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cmbPosCodeActionPerformed
    {//GEN-HEADEREND:event_cmbPosCodeActionPerformed
	// TODO add your handling code here:
    }//GEN-LAST:event_cmbPosCodeActionPerformed

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
	    java.util.logging.Logger.getLogger(frmFoodCosting.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	}
	catch (InstantiationException ex)
	{
	    java.util.logging.Logger.getLogger(frmFoodCosting.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	}
	catch (IllegalAccessException ex)
	{
	    java.util.logging.Logger.getLogger(frmFoodCosting.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	}
	catch (javax.swing.UnsupportedLookAndFeelException ex)
	{
	    java.util.logging.Logger.getLogger(frmFoodCosting.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	}
	//</editor-fold>
	//</editor-fold>
	//</editor-fold>
	//</editor-fold>

	/* Create and display the form */
	java.awt.EventQueue.invokeLater(new Runnable()
	{
	    public void run()
	    {
		new frmFoodCosting().setVisible(true);
	    }
	});
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cmbPosCode;
    private javax.swing.JComboBox cmbReportType;
    private com.toedter.calendar.JDateChooser dteFromDate;
    private com.toedter.calendar.JDateChooser dteToDate;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel lblAPC;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblFromDate;
    private javax.swing.JLabel lblHOSign;
    private javax.swing.JLabel lblModuleName;
    private javax.swing.JLabel lblPosName;
    private javax.swing.JLabel lblProductName;
    private javax.swing.JLabel lblReportType;
    private javax.swing.JLabel lblToDate;
    private javax.swing.JLabel lblUserCode;
    private javax.swing.JLabel lblformName;
    private javax.swing.JLabel lblposCode;
    private javax.swing.JPanel pnlAPC;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JPanel pnlbackground;
    private javax.swing.JPanel pnlheader;
    // End of variables declaration//GEN-END:variables

    private void funFoodCostingWiseTextReport() throws Exception
    {
	int count = 0;
	String costCenterCode = "";
	funCreateTempFolder();
	String filePath = System.getProperty("user.dir");
	File file = new File(filePath + "/Temp/Temp_ItemWiseReport.txt");
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
	funPrintBlankLines("Item Wise Report", pw);

	pw.println();
	pw.println("POS  :" + cmbPosCode.getSelectedItem().toString());
	pw.println("From :" + funGetCalenderDate("From") + "  To :" + funGetCalenderDate("To"));

	pw.println("---------------------------------------");
	pw.println();
	pw.println("Item Name                     Qty    Amt");
	pw.println("---------------------------------------");

	int cnt = 0;
	String prevParentItemName = "";
	double foodCost = 0.00, grossProfit = 0.00, foodPer = 0.00, grossPer = 0.00;
	String fromDate = (dteFromDate.getDate().getYear() + 1900) + "-" + (dteFromDate.getDate().getMonth() + 1) + "-" + (dteFromDate.getDate().getDate());
	String toDate = (dteToDate.getDate().getYear() + 1900) + "-" + (dteToDate.getDate().getMonth() + 1) + "-" + (dteToDate.getDate().getDate());

	String sqlStkOutData;
	StringBuilder sbDayEndStkOutCode = new StringBuilder();

	String posCode = cmbPosCode.getSelectedItem().toString();
	StringBuilder sb = new StringBuilder(posCode);
	int len = posCode.length();
	int lastInd = sb.lastIndexOf(" ");
	String pos = sb.substring(lastInd + 1, len).toString();

	fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());

	toDate = objUtility.funGetFromToDate(dteToDate.getDate());
	String receivedUom = "", recipeUom = "";
	double recipeConversion = 0.00, receivedConversion = 0.00, totQty = 0.00, totAmount = 0.00;

	sbDayEndStkOutCode.append("select a.dtePOSDate,a.strWSStockAdjustmentNo from tbldayendprocess a\n"
		+ "where a.dtePOSDate between '" + fromDate + "' and '" + toDate + "' ");
	if (!pos.equals("All"))
	{
	    sbDayEndStkOutCode.append(" and a.strPOSCode='" + pos + "' ");
	}
	String[] arrStkCodes = null;
	ResultSet rsDayEndStkOutCode = clsGlobalVarClass.dbMysql.executeResultSet(sbDayEndStkOutCode.toString());
	while (rsDayEndStkOutCode.next())
	{

	    String stkOutCode = rsDayEndStkOutCode.getString(2);
	    if (!stkOutCode.equalsIgnoreCase(""))
	    {
		arrStkCodes = stkOutCode.split(",");

	    }
	}
	if (fromDate.equals(toDate))
	{
	    for (String stkcode : arrStkCodes)
	    {
		sqlStkOutData = "select b.strItemCode,c.strItemName,sum(b.dblQuantity),sum(b.dblAmount) \n"
			+ ",a.strNarration,b.strParentCode,b.strRemark,  "
			+ "c.strUOM,c.strRecipeUOM,c.dblReceivedConversion,c.dblRecipeConversion"
			+ ",sum(b.dblParentItemQty),b.dblParentItemRate "
			+ "from tblstkouthd a,tblstkoutdtl b,tblitemmaster c\n"
			+ " where  a.strStkOutCode = b.strStkOutCode and \n"
			+ "b.strItemCode = c.strItemCode  \n"
			+ "and date(a.dteStkOutDate) between '" + fromDate + "' and '" + toDate + "'";
		if (!pos.equals("All"))
		{
		    sqlStkOutData += " and a.strPOSCode='" + pos + "' ";
		}
		sqlStkOutData += "group by b.strItemCode,b.strParentCode \n"
			+ " order by b.strParentCode,b.strItemCode";

		ResultSet rsStkOutData = clsGlobalVarClass.dbMysql.executeResultSet(sqlStkOutData);
		while (rsStkOutData.next())
		{
		    String strNarration = "", parentItemCode = "", parentItemName = "";
		    double amount = 0.00, parentItemQty = 0, parentItemRate = 0;
		    totQty = Double.parseDouble(rsStkOutData.getString(3));
		    totAmount = Double.parseDouble(rsStkOutData.getString(4));
		    receivedUom = rsStkOutData.getString(8);
		    recipeUom = rsStkOutData.getString(9);
		    receivedConversion = Double.parseDouble(rsStkOutData.getString(10));
		    recipeConversion = Double.parseDouble(rsStkOutData.getString(11));
		    String strDispQty = funGetDispalyUOMQty(totQty, receivedUom, recipeUom, receivedConversion, recipeConversion);
		    parentItemQty = rsStkOutData.getDouble(12);
		    parentItemRate = rsStkOutData.getDouble(13);
		    amount = parentItemQty * parentItemRate;
		    strNarration = rsStkOutData.getString(5);
		    costCenterCode = strNarration;

		    parentItemCode = rsStkOutData.getString(6);
		    String sqlParentName = "select a.strItemName\n"
			    + " from tblitemmaster a\n"
			    + " where a.strItemCode  = '" + parentItemCode + "'";
		    ResultSet rsParentItemNameData = clsGlobalVarClass.dbMysql.executeResultSet(sqlParentName);
		    if (rsParentItemNameData.next())
		    {
			parentItemName = rsParentItemNameData.getString(1);
		    }

		    count++;
		    if (!parentItemName.equals(prevParentItemName))
		    {
			if (cnt > 0)
			{
			    DecimalFormat decFormat = new DecimalFormat(".##");
			    pw.println("---------------------------------------");
			    pw.print("Gross Profit:" + grossProfit + "    Food Cost:" + foodCost);
			    pw.println();
			    pw.print("             " + decFormat.format(grossPer) + "%" + "            " + decFormat.format(foodPer) + "%");
			    pw.println();
			    pw.println("---------------------------------------");
			    pw.println();
			    grossProfit = 0.00;
			    foodCost = 0.00;
			    grossPer = 0.00;
			    foodPer = 0.00;
			}

			//pw.print(parentItemName);
			if (parentItemName.length() > 22)
			{
			    pw.print(funPrintTextWithAlignment(parentItemName, 22, "LEFT"));
			    pw.println();
			    pw.print(funPrintTextWithAlignment(parentItemName.substring(22, parentItemName.length()), 22, "LEFT"));
			}
			else
			{
			    pw.print(funPrintTextWithAlignment(parentItemName, 22, "LEFT"));
			}

			// pw.print("       "+qty+"   ");
			pw.print(funPrintTextWithAlignment(String.valueOf(parentItemQty), 11, "RIGHT"));
			pw.print(funPrintTextWithAlignment(String.valueOf(amount), 7, "RIGHT"));

			pw.println();
			pw.println("-------------");
			pw.println();

		    }
		    prevParentItemName = parentItemName;

		    //pw.print(rsStkOutData.getString(2));
		    if (rsStkOutData.getString(2).length() > 22)
		    {
			pw.print(funPrintTextWithAlignment(rsStkOutData.getString(2), 22, "LEFT"));
			pw.println();
			pw.print(funPrintTextWithAlignment(rsStkOutData.getString(2).substring(22, rsStkOutData.getString(2).length()), 22, "LEFT"));
		    }
		    else
		    {
			pw.print(funPrintTextWithAlignment(rsStkOutData.getString(2), 22, "LEFT"));
		    }
		    //pw.print(funPrintTextWithAlignment(rsStkOutData.getString(2), 11, "LEFT"));
		    //pw.print("       "+strDispQty+"  ");
		    pw.print(funPrintTextWithAlignment(strDispQty, 11, "RIGHT"));
		    pw.print(funPrintTextWithAlignment(rsStkOutData.getString(4), 7, "RIGHT"));
		    pw.println();

		    foodCost += Double.parseDouble(rsStkOutData.getString(4));
		    grossProfit = amount - foodCost;
		    grossPer = (grossProfit / amount) * 100;
		    foodPer = 100 - grossPer;
		    cnt++;

		    if (rsStkOutData.isLast())
		    {
			DecimalFormat decFormat = new DecimalFormat(".##");
			pw.println("---------------------------------------");
			pw.print("Gross Profit:" + grossProfit + "    Food Cost:" + foodCost);
			pw.println();
			pw.print("             " + decFormat.format(grossPer) + "%" + "            " + decFormat.format(foodPer) + "%");

			pw.println();

			grossProfit = 0.00;
			foodCost = 0.00;
			grossPer = 0.00;
			foodPer = 0.00;
		    }
		}

		rsStkOutData.close();
	    }
	}
	else
	{

	    String sqlCostCenter = "select ifnull(a.strNarration,'') from tblstkouthd a\n"
		    + "where (a.dteStkOutDate) between '" + fromDate + "' and '" + toDate + "'\n"
		    + "group by a.strNarration\n"
		    + "order by a.strNarration";
	    ResultSet rsCostCenter = clsGlobalVarClass.dbMysql.executeResultSet(sqlCostCenter);
	    while (rsCostCenter.next())
	    {
		String strCostCenterCode = rsCostCenter.getString(1);

		sqlStkOutData = "select b.strItemCode,c.strItemName,sum(b.dblQuantity),sum(b.dblAmount) \n"
			+ ",a.strNarration,b.strParentCode,b.strRemark,  "
			+ "c.strUOM,c.strRecipeUOM,c.dblReceivedConversion,c.dblRecipeConversion "
			+ ",sum(b.dblParentItemQty),b.dblParentItemRate "
			+ "from tblstkouthd a,tblstkoutdtl b,tblitemmaster c\n"
			+ " where  a.strStkOutCode = b.strStkOutCode and \n"
			+ "b.strItemCode = c.strItemCode and \n"
			+ " date(a.dteStkOutDate) between '" + fromDate + "' and '" + toDate + "' and "
			+ "a.strNarration = '" + strCostCenterCode + "'";

		if (!pos.equals("All"))
		{
		    sqlStkOutData += " and a.strPOSCode='" + pos + "' ";
		}
		sqlStkOutData += "group by b.strItemCode,b.strParentCode \n"
			+ " order by b.strParentCode,b.strItemCode";

		ResultSet rsStkOutData = clsGlobalVarClass.dbMysql.executeResultSet(sqlStkOutData);
		while (rsStkOutData.next())
		{
		    String strNarration = "", parentItemCode = "", parentItemName = "";
		    double amount = 0.00, parentItemQty = 0, parentItemRate = 0;;

		    totQty = Double.parseDouble(rsStkOutData.getString(3));
		    totAmount = Double.parseDouble(rsStkOutData.getString(4));
		    receivedUom = rsStkOutData.getString(8);
		    recipeUom = rsStkOutData.getString(9);
		    receivedConversion = Double.parseDouble(rsStkOutData.getString(10));
		    recipeConversion = Double.parseDouble(rsStkOutData.getString(11));
		    String strDispQty = funGetDispalyUOMQty(totQty, receivedUom, recipeUom, receivedConversion, recipeConversion);
		    parentItemQty = rsStkOutData.getDouble(12);
		    parentItemRate = rsStkOutData.getDouble(13);
		    amount = parentItemQty * parentItemRate;
		    strNarration = rsStkOutData.getString(5);

		    costCenterCode = strNarration;

		    parentItemCode = rsStkOutData.getString(6);
		    String sqlParentName = "select a.strItemName\n"
			    + " from tblitemmaster a\n"
			    + " where a.strItemCode  = '" + parentItemCode + "'";
		    ResultSet rsParentItemNameData = clsGlobalVarClass.dbMysql.executeResultSet(sqlParentName);
		    if (rsParentItemNameData.next())
		    {
			parentItemName = rsParentItemNameData.getString(1);
		    }

		    count++;

		    if (!parentItemName.equals(prevParentItemName))
		    {

			if (cnt > 0)
			{
			    DecimalFormat decFormat = new DecimalFormat(".##");
			    pw.println("---------------------------------------");
			    pw.print("Gross Profit:" + grossProfit + "    Food Cost:" + foodCost);
			    pw.println();
			    pw.print("             " + decFormat.format(grossPer) + "%" + "            " + decFormat.format(foodPer) + "%");
			    pw.println();
			    pw.println("---------------------------------------");
			    pw.println();
			    grossProfit = 0.00;
			    foodCost = 0.00;
			    grossPer = 0.00;
			    foodPer = 0.00;
			}
			if (parentItemName.length() > 22)
			{
			    pw.print(funPrintTextWithAlignment(parentItemName, 22, "LEFT"));
			    pw.println();
			    pw.print(funPrintTextWithAlignment(parentItemName.substring(22, parentItemName.length()), 22, "LEFT"));
			}
			else
			{
			    pw.print(funPrintTextWithAlignment(parentItemName, 22, "LEFT"));
			}
			pw.print(funPrintTextWithAlignment(String.valueOf(parentItemQty), 11, "RIGHT"));
			pw.print(funPrintTextWithAlignment(String.valueOf(amount), 7, "RIGHT"));
			pw.println();
			pw.println("-------------");
			pw.println();

		    }
		    prevParentItemName = parentItemName;

		    if (rsStkOutData.getString(2).length() > 22)
		    {
			pw.print(funPrintTextWithAlignment(rsStkOutData.getString(2), 22, "LEFT"));
			pw.println();
			pw.print(funPrintTextWithAlignment(rsStkOutData.getString(2).substring(22, rsStkOutData.getString(2).length()), 22, "LEFT"));
		    }
		    else
		    {
			pw.print(funPrintTextWithAlignment(rsStkOutData.getString(2), 22, "LEFT"));
		    }
		    pw.print(funPrintTextWithAlignment(strDispQty, 11, "RIGHT"));
		    pw.print(funPrintTextWithAlignment(rsStkOutData.getString(4), 7, "RIGHT"));
		    pw.println();

		    foodCost += Double.parseDouble(rsStkOutData.getString(4));
		    grossProfit = amount - foodCost;
		    grossPer = (grossProfit / amount) * 100;
		    foodPer = 100 - grossPer;

		    cnt++;

		    if (rsStkOutData.isLast())
		    {
			DecimalFormat decFormat = new DecimalFormat(".##");
			pw.println("---------------------------------------");
			pw.print("Gross Profit:" + grossProfit + "    Food Cost:" + foodCost);
			pw.println();
			pw.print("             " + decFormat.format(grossPer) + "%" + "            " + decFormat.format(foodPer) + "%");

			pw.println();
			grossProfit = 0.00;
			foodCost = 0.00;
			grossPer = 0.00;
			foodPer = 0.00;
		    }
		}

		rsStkOutData.close();

	    }
	}

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
	    funShowTextFile(file, "Text Sales Report");
	}

    }

    private String funGetDispalyUOMQty(double totQty, String receivedUom, String recipeUom, double receivedConversion, double recipeConversion)
    {
	String strDispQty = "";
	DecimalFormat df3Zeors = new DecimalFormat("0.000");
	if (totQty < 1)
	{
	    double qtytemp = Double.parseDouble(df3Zeors.format(totQty * recipeConversion).toString());
	    strDispQty = qtytemp + " " + recipeUom;

	}
	else
	{
	    Double qty = totQty;
	    String[] spqty = (qty.toString()).split("\\.");
	    double lowest = qty - Double.parseDouble(spqty[0]);
	    double qtytemp = Double.parseDouble(df3Zeors.format(lowest * recipeConversion).toString());
	    strDispQty = spqty[0] + " " + receivedUom + "."
		    + qtytemp + " " + recipeUom;
	}

	return strDispQty;
    }

    private void funCreateTempFolder()
    {
	String filePath = System.getProperty("user.dir");
	File TextKOT = new File(filePath + "/Temp");
	if (!TextKOT.exists())
	{
	    TextKOT.mkdirs();
	}
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
	    BufferedReader KOTIn = new BufferedReader(fread);

	    String line = "";
	    while ((line = KOTIn.readLine()) != null)
	    {
		data = data + line + "\n";
	    }
	    new frmShowTextFile(data, reportName, file, clsGlobalVarClass.gBillPrintPrinterPort).setVisible(true);
	    fread.close();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
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
//    private int funPrintTextWithAlignment(String align, String textToPrint, int totalLength, PrintWriter pw)
//    {
//        int len = totalLength - textToPrint.length();
//        for (int cnt = 0; cnt < len; cnt++)
//        {
//            pw.print(" ");
//        }
//
//        DecimalFormat decFormat = new DecimalFormat("######.00");
//        pw.print(decFormat.format(Double.parseDouble(textToPrint)));
//        return 1;
//    }
//    
//    private void funPrintQtyWithML(String align, String textToPrint, int totalLength, PrintWriter pw)
//    {
//        int len = totalLength - textToPrint.length();
//        for (int cnt = 0; cnt < len; cnt++)
//        {
//            pw.print(" ");
//        }
//       pw.print(textToPrint);
//    }
//    
    public String funPrintTextWithAlignment(String text, int totalLength, String alignment)
    {
	StringBuilder sbText = new StringBuilder();

	if (alignment.equalsIgnoreCase("Center"))
	{
	    int textLength = text.length();
	    int totalSpace = (totalLength - textLength) / 2;
	    for (int i = 0; i < totalSpace; i++)
	    {
		sbText.append(" ");
	    }
	    sbText.append(text);

	}

	else if (alignment.equalsIgnoreCase("Left"))
	{
	    sbText.setLength(0);
	    int textLength = text.length();
	    int totalSpace = (totalLength - textLength);

	    if (totalSpace < 0)
	    {
		sbText.append(text.substring(0, totalLength));
	    }
	    else
	    {
		sbText.append(text);

		for (int i = 0; i < totalSpace; i++)
		{
		    sbText.append(" ");
		}
	    }

	}
	else
	{
	    sbText.setLength(0);
	    int textLength = text.length();
	    int totalSpace = (totalLength - textLength);

	    if (totalSpace < 0)
	    {
		sbText.append(text.substring(0, totalLength));
	    }
	    else
	    {
		for (int i = 0; i < totalSpace; i++)
		{
		    sbText.append(" ");
		}
		sbText.append(text);
	    }

	}

	return sbText.toString();
    }

    /**
     * Draw Under Line
     *
     * @param textToPrint
     * @param pw
     * @return
     */
    private int funDrawUnderLine(String textToPrint, PrintWriter pw)
    {
	for (int cnt = 0; cnt < textToPrint.length(); cnt++)
	{
	    pw.print("-");
	}
	pw.println();
	return 1;
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
     * Get Calender Date
     *
     * @throws Exception
     */
    private String funGetCalenderDate(String type)
    {
	String date = "";
	if (type.equalsIgnoreCase("From"))
	{
	    date = dteFromDate.getDate().getDate() + "-" + (dteFromDate.getDate().getMonth() + 1) + "-" + (dteFromDate.getDate().getYear() + 1900);
	}
	else
	{
	    date = dteToDate.getDate().getDate() + "-" + (dteToDate.getDate().getMonth() + 1) + "-" + (dteToDate.getDate().getYear() + 1900);
	}
	return date;
    }

}
