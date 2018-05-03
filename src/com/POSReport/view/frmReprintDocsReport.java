package com.POSReport.view;

import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsPosConfigFile;
import com.POSGlobal.controller.clsReprintDocs;

import com.POSGlobal.controller.clsUtility;
import com.POSGlobal.view.frmOkPopUp;

import java.awt.Desktop;
import java.awt.Dimension;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.File;

import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.text.DecimalFormat;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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

public class frmReprintDocsReport extends javax.swing.JFrame
{

    private String imagePath, reportName;
    private clsUtility objUtility;
    private String fromDate, toDate;

    public frmReprintDocsReport()
    {
	try
	{
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

	    objUtility = new clsUtility();
	    dteFromDate.setDate(objUtility.funGetDateToSetCalenderDate());
	    dteToDate.setDate(objUtility.funGetDateToSetCalenderDate());
	    imagePath = System.getProperty("user.dir");
	    imagePath = imagePath + File.separator + "ReportImage";

	    String sql = null;
	    ResultSet rs = null;
	    if (clsGlobalVarClass.gShowOnlyLoginPOSReports)
	    {
		cmbPosCode.addItem(clsGlobalVarClass.gPOSName + "                                        " + clsGlobalVarClass.gPOSCode);
	    }
	    else
	    {
		cmbPosCode.addItem("All");
		cmbUser.addItem("All");
		cmbDocumentNo.addItem("All");
		sql = "select strPosName,strPosCode from tblposmaster";
		rs = clsGlobalVarClass.dbMysql.executeResultSet(sql);
		while (rs.next())
		{
		    cmbPosCode.addItem(rs.getString(1) + "                                        " + rs.getString(2));
		}
		rs.close();
	    }

	    sql = "select DISTINCT strUserCreated from tblaudit";
	    rs = clsGlobalVarClass.dbMysql.executeResultSet(sql);
	    while (rs.next())
	    {
		cmbUser.addItem(rs.getString(1));
	    }
	    rs.close();

	    sql = "select distinct strDocNo from tblaudit where strTransactionName='Bill'";
	    rs = clsGlobalVarClass.dbMysql.executeResultSet(sql);
	    while (rs.next())
	    {
		cmbDocumentNo.addItem(rs.getString(1));
	    }
	    rs.close();

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

    private void funReprintDocsReport() throws Exception
    {
	fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());

	toDate = objUtility.funGetFromToDate(dteToDate.getDate());
	funCreateTempFolder();
	String filePath = System.getProperty("user.dir");
	File file = new File(filePath + File.separator + "Temp" + File.separator + "Temp_ReprintReport.txt");

	int count = funReprintDocsTextReport(file, fromDate, toDate);

	if (count > 0)
	{
	    Desktop dt = Desktop.getDesktop();
	    dt.open(file);
	}
	else
	{
	    JOptionPane.showMessageDialog(this, "No Records Found");
	}
    }

    private void funReprintSummary() throws Exception
    {
	fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());

	toDate = objUtility.funGetFromToDate(dteToDate.getDate());
	funCreateTempFolder();
	String filePath = System.getProperty("user.dir");
	File file = new File(filePath + File.separator + "Temp" + File.separator + "ReprintSummaryReport.txt");

	int count = funReprintDocsSummaryReport(file, fromDate, toDate);

	if (count > 0)
	{
	    Desktop dt = Desktop.getDesktop();
	    dt.open(file);
	}
	else
	{
	    JOptionPane.showMessageDialog(this, "No Records Found");
	}
    }

    private int funReprintDocsSummaryReport(File file, String fromDate, String toDate) throws Exception
    {
	PrintWriter pw = new PrintWriter(file);

	String sqlFromDate = fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());

	String sqlToDate = objUtility.funGetFromToDate(dteToDate.getDate());

	pw.println("Reprint Docs Report");
	pw.println(clsGlobalVarClass.gClientName);
	pw.println(clsGlobalVarClass.gClientAddress1);
	if (clsGlobalVarClass.gClientAddress2.trim().length() > 0)
	{
	    pw.println(clsGlobalVarClass.gClientAddress2);
	}
	if (clsGlobalVarClass.gClientAddress3.trim().length() > 0)
	{
	    pw.println(clsGlobalVarClass.gClientAddress3);
	}
	pw.println("Reporting Time:" + "  " + fromDate + " " + "To" + " " + toDate);
	pw.println();
	pw.println("------------------------------------------------------------------------------------------------------------------------------");
	pw.print(objUtility.funPrintTextWithAlignment(("Bill No"), 15, "Left"));
	pw.print(objUtility.funPrintTextWithAlignment(("Reprint Date"), 15, "Left"));
	pw.print(objUtility.funPrintTextWithAlignment(("User"), 10, "Left"));
	pw.print(objUtility.funPrintTextWithAlignment(("Total"), 16, "Right"));
	pw.println(objUtility.funPrintTextWithAlignment(("Count"), 16, "Right"));

	pw.println("------------------------------------------------------------------------------------------------------------------------------");
	pw.println();

	StringBuilder sbSql = new StringBuilder();
	sbSql.setLength(0);
	sbSql.append("select  a.strBillNo,DATE_FORMAT(b.dtePOSDate,'%m-%d-%Y'),b.strUserCreated,a.dblGrandTotal,count(*)as count \n"
		+ "from tblbillhd a,tblaudit b left outer join tblreasonmaster c on b.strReasonCode=c.strReasonCode where a.strBillNo=b.strDocNo "
		+ " and date(b.dtePOSDate) between '" + sqlFromDate + "' and '" + sqlToDate + "'");
	if (!cmbUser.getSelectedItem().toString().equals("All"))
	{
	    sbSql.append(" and b.strUserCreated='" + cmbUser.getSelectedItem().toString() + "'");
	}
	if (!cmbPosCode.getSelectedItem().toString().equals("All"))
	{
	    sbSql.append(" and a.strPOSCode='" + cmbPosCode.getSelectedItem().toString() + "'");
	}
	if (!cmbDocumentNo.getSelectedItem().toString().equals("All"))
	{
	    sbSql.append(" and b.strDocNo='" + cmbDocumentNo.getSelectedItem().toString() + "'");
	}
	sbSql.append(" group by a.strBillNo,b.strUserCreated ");
	ResultSet rsBillWise = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
	while (rsBillWise.next())
	{
	    pw.print(objUtility.funPrintTextWithAlignment(rsBillWise.getString(1), 15, "Left"));
	    pw.print(objUtility.funPrintTextWithAlignment(rsBillWise.getString(2), 15, "Left"));
	    pw.print(objUtility.funPrintTextWithAlignment(rsBillWise.getString(3), 15, "Left"));
	    pw.print(objUtility.funPrintTextWithAlignment(rsBillWise.getString(4), 14, "Right"));
	    pw.println(objUtility.funPrintTextWithAlignment(rsBillWise.getString(5), 12, "Right"));
	}
	rsBillWise.close();

	sbSql.setLength(0);
	sbSql.append("select  a.strBillNo,DATE_FORMAT(b.dtePOSDate,'%m-%d-%Y'),b.strUserCreated,a.dblGrandTotal,count(*)as count \n"
		+ "from tblqbillhd a,tblaudit b left outer join tblreasonmaster c on b.strReasonCode=c.strReasonCode where a.strBillNo=b.strDocNo "
		+ " and date(b.dtePOSDate) between '" + sqlFromDate + "' and '" + sqlToDate + "'");
	if (!cmbUser.getSelectedItem().toString().equals("All"))
	{
	    sbSql.append(" and b.strUserCreated='" + cmbUser.getSelectedItem().toString() + "'");
	}
	if (!cmbPosCode.getSelectedItem().toString().equals("All"))
	{
	    sbSql.append(" and a.strPOSCode='" + cmbPosCode.getSelectedItem().toString() + "'");
	}
	if (!cmbDocumentNo.getSelectedItem().toString().equals("All"))
	{
	    sbSql.append(" and b.strDocNo='" + cmbDocumentNo.getSelectedItem().toString() + "'");
	}
	sbSql.append(" group by a.strBillNo,b.strUserCreated");
	rsBillWise = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
	while (rsBillWise.next())
	{
	    pw.print(objUtility.funPrintTextWithAlignment(rsBillWise.getString(1), 15, "Left"));
	    pw.print(objUtility.funPrintTextWithAlignment(rsBillWise.getString(2), 15, "Left"));
	    pw.print(objUtility.funPrintTextWithAlignment(rsBillWise.getString(3), 15, "Left"));
	    pw.print(objUtility.funPrintTextWithAlignment(rsBillWise.getString(4), 14, "Right"));
	    pw.println(objUtility.funPrintTextWithAlignment(rsBillWise.getString(5), 12, "Right"));
	}
	rsBillWise.close();

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

	return 1;
    }

    private int funReprintDocsTextReport(File file, String fromDate, String toDate) throws Exception
    {
	PrintWriter pw = new PrintWriter(file);

	String sqlFromDate = (dteFromDate.getDate().getYear() + 1900) + "-" + (dteFromDate.getDate().getMonth() + 1) + "-" + (dteFromDate.getDate().getDate());
	String sqlToDate = (dteToDate.getDate().getYear() + 1900) + "-" + (dteToDate.getDate().getMonth() + 1) + "-" + (dteToDate.getDate().getDate());
	pw.println("Reprint Docs Report");
	pw.println(clsGlobalVarClass.gClientName);
	pw.println(clsGlobalVarClass.gClientAddress1);
	if (clsGlobalVarClass.gClientAddress2.trim().length() > 0)
	{
	    pw.println(clsGlobalVarClass.gClientAddress2);
	}
	if (clsGlobalVarClass.gClientAddress3.trim().length() > 0)
	{
	    pw.println(clsGlobalVarClass.gClientAddress3);
	}
	pw.println("Reporting Time:" + "  " + fromDate + " " + "To" + " " + toDate);
	pw.println();
	pw.println("------------------------------------------------------------------------------------------------------------------------------");
	pw.print(objUtility.funPrintTextWithAlignment(("Bill No"), 15, "Left"));
	pw.print(objUtility.funPrintTextWithAlignment(("Reprint Date"), 15, "Left"));
	pw.print(objUtility.funPrintTextWithAlignment(("Reprint Time"), 15, "Left"));
	pw.print(objUtility.funPrintTextWithAlignment(("User"), 10, "Left"));
	pw.print(objUtility.funPrintTextWithAlignment(("Reason"), 20, "Left"));
	pw.print(objUtility.funPrintTextWithAlignment(("Remarks"), 20, "Left"));
	pw.println(objUtility.funPrintTextWithAlignment(("Total"), 16, "Right"));
	pw.println("------------------------------------------------------------------------------------------------------------------------------");
	pw.println();

	StringBuilder sbSql = new StringBuilder();
	sbSql.setLength(0);
	sbSql.append("select a.strBillNo,DATE_FORMAT(b.dtePOSDate,'%m-%d-%Y'),b.strUserCreated,ifnull(c.strReasonName,'') "
		+ " ,b.strRemarks,a.dblGrandTotal,time(b.dtePOSDate) "
		+ " from tblbillhd a,tblaudit b left outer join tblreasonmaster c on b.strReasonCode=c.strReasonCode "
		+ " where a.strBillNo=b.strDocNo "
		+ " and date(b.dtePOSDate) between '" + sqlFromDate + "' and '" + sqlToDate + "' ");
	if (!cmbUser.getSelectedItem().toString().equals("All"))
	{
	    sbSql.append(" and b.strUserCreated='" + cmbUser.getSelectedItem().toString() + "'");
	}
	if (!cmbPosCode.getSelectedItem().toString().equals("All"))
	{
	    sbSql.append(" and a.strPOSCode='" + cmbPosCode.getSelectedItem().toString() + "'");
	}
	if (!cmbDocumentNo.getSelectedItem().toString().equals("All"))
	{
	    sbSql.append(" and b.strDocNo='" + cmbDocumentNo.getSelectedItem().toString() + "'");
	}
	ResultSet rsBillWise = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
	while (rsBillWise.next())
	{
	    pw.print(objUtility.funPrintTextWithAlignment(rsBillWise.getString(1), 15, "Left"));
	    pw.print(objUtility.funPrintTextWithAlignment(rsBillWise.getString(2), 15, "Left"));
	    pw.print(objUtility.funPrintTextWithAlignment(rsBillWise.getString(7), 15, "Left"));
	    pw.print(objUtility.funPrintTextWithAlignment(rsBillWise.getString(3), 10, "Left"));
	    pw.print(objUtility.funPrintTextWithAlignment(rsBillWise.getString(4), 20, "Left"));
	    pw.print(objUtility.funPrintTextWithAlignment(rsBillWise.getString(5), 20, "Left"));
	    pw.println(objUtility.funPrintTextWithAlignment(rsBillWise.getString(6), 16, "Right"));
	}
	rsBillWise.close();

	sbSql.setLength(0);
	sbSql.append("select a.strBillNo,DATE_FORMAT(b.dtePOSDate,'%m-%d-%Y'),b.strUserCreated,ifnull(c.strReasonName,'') "
		+ " ,b.strRemarks,a.dblGrandTotal,time(b.dtePOSDate) "
		+ " from tblqbillhd a,tblaudit b left outer join tblreasonmaster c on b.strReasonCode=c.strReasonCode "
		+ " where a.strBillNo=b.strDocNo  "
		+ " and date(b.dtePOSDate) between '" + sqlFromDate + "' and '" + sqlToDate + "' ");
	if (!cmbUser.getSelectedItem().toString().equals("All"))
	{
	    sbSql.append(" and b.strUserCreated='" + cmbUser.getSelectedItem().toString() + "'");
	}
	if (!cmbPosCode.getSelectedItem().toString().equals("All"))
	{
	    sbSql.append(" and a.strPOSCode='" + cmbPosCode.getSelectedItem().toString() + "'");
	}
	if (!cmbDocumentNo.getSelectedItem().toString().equals("All"))
	{
	    sbSql.append(" and b.strDocNo='" + cmbDocumentNo.getSelectedItem().toString() + "'");
	}
	rsBillWise = clsGlobalVarClass.dbMysql.executeResultSet(sbSql.toString());
	while (rsBillWise.next())
	{
	    pw.print(objUtility.funPrintTextWithAlignment(rsBillWise.getString(1), 15, "Left"));
	    pw.print(objUtility.funPrintTextWithAlignment(rsBillWise.getString(2), 15, "Left"));
	    pw.print(objUtility.funPrintTextWithAlignment(rsBillWise.getString(7), 15, "Left"));
	    pw.print(objUtility.funPrintTextWithAlignment(rsBillWise.getString(3), 10, "Left"));
	    pw.print(objUtility.funPrintTextWithAlignment(rsBillWise.getString(4), 20, "Left"));
	    pw.print(objUtility.funPrintTextWithAlignment(rsBillWise.getString(5), 20, "Left"));
	    pw.println(objUtility.funPrintTextWithAlignment(rsBillWise.getString(6), 16, "Right"));
	}
	rsBillWise.close();

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
	return 1;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlheader = new javax.swing.JPanel();
        lblProductName = new javax.swing.JLabel();
        lblModuleName = new javax.swing.JLabel();
        lblfromName = new javax.swing.JLabel();
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
        };

        ;
        pnlMain = new javax.swing.JPanel();
        lblReprintDocsReport = new javax.swing.JLabel();
        cmbPosCode = new javax.swing.JComboBox();
        lblposName = new javax.swing.JLabel();
        cmbUser = new javax.swing.JComboBox();
        cmbType = new javax.swing.JComboBox();
        dteFromDate = new com.toedter.calendar.JDateChooser();
        dteToDate = new com.toedter.calendar.JDateChooser();
        lblToDate = new javax.swing.JLabel();
        lblFromDate = new javax.swing.JLabel();
        lbltype = new javax.swing.JLabel();
        btnBack = new javax.swing.JButton();
        btnView = new javax.swing.JButton();
        lblReportType = new javax.swing.JLabel();
        cmbReportType = new javax.swing.JComboBox();
        lblUser = new javax.swing.JLabel();
        lblBillNumber = new javax.swing.JLabel();
        cmbDocumentNo = new javax.swing.JComboBox();

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
        pnlheader.add(lblProductName);

        lblModuleName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblModuleName.setForeground(new java.awt.Color(255, 255, 255));
        pnlheader.add(lblModuleName);

        lblfromName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblfromName.setForeground(new java.awt.Color(255, 255, 255));
        lblfromName.setText("-Reprint Docs Report");
        pnlheader.add(lblfromName);
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

        pnlBackGround.setOpaque(false);
        pnlBackGround.setLayout(new java.awt.GridBagLayout());

        pnlMain.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, new java.awt.Color(204, 204, 204), new java.awt.Color(204, 204, 204), new java.awt.Color(153, 153, 153), new java.awt.Color(153, 153, 153)));
        pnlMain.setMinimumSize(new java.awt.Dimension(800, 570));
        pnlMain.setOpaque(false);

        lblReprintDocsReport.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        lblReprintDocsReport.setText("Reprint Docs Report ");
        lblReprintDocsReport.setToolTipText("");

        cmbPosCode.setBackground(new java.awt.Color(51, 102, 255));
        cmbPosCode.setForeground(new java.awt.Color(255, 255, 255));
        cmbPosCode.setToolTipText("Select POS");
        cmbPosCode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbPosCodeActionPerformed(evt);
            }
        });

        lblposName.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblposName.setText("POS Name              :");

        cmbUser.setBackground(new java.awt.Color(51, 102, 255));
        cmbUser.setForeground(new java.awt.Color(255, 255, 255));
        cmbUser.setToolTipText("Select Cost Center");

        cmbType.setBackground(new java.awt.Color(51, 102, 255));
        cmbType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Detail", "Summary", " " }));
        cmbType.setToolTipText("Select  View Type");
        cmbType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbTypeActionPerformed(evt);
            }
        });

        dteFromDate.setToolTipText("Select From Date");

        dteToDate.setToolTipText("Select To Date");

        lblToDate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblToDate.setText("To Date                   :");

        lblFromDate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblFromDate.setText("From Date               :");

        lbltype.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lbltype.setText("Type                      :");

        btnBack.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnBack.setForeground(new java.awt.Color(255, 255, 255));
        btnBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnBack.setText("CLOSE");
        btnBack.setToolTipText("Close Window");
        btnBack.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnBack.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn2.png"))); // NOI18N
        btnBack.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnBackMouseClicked(evt);
            }
        });
        btnBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackActionPerformed(evt);
            }
        });

        btnView.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnView.setForeground(new java.awt.Color(255, 255, 255));
        btnView.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn1.png"))); // NOI18N
        btnView.setText("VIEW");
        btnView.setToolTipText("View Report");
        btnView.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnView.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgCmnBtn2.png"))); // NOI18N
        btnView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnViewActionPerformed(evt);
            }
        });

        lblReportType.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblReportType.setText("Report Type             :");

        cmbReportType.setBackground(new java.awt.Color(51, 102, 255));
        cmbReportType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "A4 Size Report", "Text File-40 Column Report", "Excel Report" }));
        cmbReportType.setToolTipText("Select POS");
        cmbReportType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbReportTypeActionPerformed(evt);
            }
        });

        lblUser.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblUser.setText("User                       :");

        lblBillNumber.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblBillNumber.setText("Document No            :");

        cmbDocumentNo.setBackground(new java.awt.Color(51, 102, 255));
        cmbDocumentNo.setToolTipText("Select POS");
        cmbDocumentNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbDocumentNoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlMainLayout = new javax.swing.GroupLayout(pnlMain);
        pnlMain.setLayout(pnlMainLayout);
        pnlMainLayout.setHorizontalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMainLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(btnView, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(42, 42, 42)
                .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26))
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addGap(238, 238, 238)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlMainLayout.createSequentialGroup()
                                .addComponent(lblFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0)
                                .addComponent(dteFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(pnlMainLayout.createSequentialGroup()
                                .addComponent(lblToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0)
                                .addComponent(dteToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(pnlMainLayout.createSequentialGroup()
                                .addComponent(lblReportType, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cmbReportType, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(pnlMainLayout.createSequentialGroup()
                                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(lblposName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lblUser, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                                    .addComponent(lbltype, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(cmbType, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(cmbUser, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(cmbPosCode, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(pnlMainLayout.createSequentialGroup()
                                .addGap(8, 8, 8)
                                .addComponent(lblReprintDocsReport, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(264, Short.MAX_VALUE))
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addComponent(lblBillNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cmbDocumentNo, 0, 170, Short.MAX_VALUE)
                        .addGap(256, 256, 256))))
        );
        pnlMainLayout.setVerticalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(lblReprintDocsReport, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(59, 59, 59)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblposName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbPosCode, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblUser, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbUser, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lbltype, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbType, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dteFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dteToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(29, 29, 29)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblReportType, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbReportType, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblBillNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbDocumentNo, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnView, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(43, 43, 43))
        );

        lblUser.getAccessibleContext().setAccessibleName("User:");

        pnlBackGround.add(pnlMain, new java.awt.GridBagConstraints());

        getContentPane().add(pnlBackGround, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents


    private void cmbTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbTypeActionPerformed
	// TODO add your handling code here:
    }//GEN-LAST:event_cmbTypeActionPerformed

    private void btnBackMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnBackMouseClicked
	// TODO add your handling code here:
	dispose();
	clsGlobalVarClass.hmActiveForms.remove("Reprint Docs Report");
    }//GEN-LAST:event_btnBackMouseClicked

    private void funCreateTempFolder()
    {
	String filePath = System.getProperty("user.dir");
	File TextKOT = new File(filePath + File.separator + "Temp");
	if (!TextKOT.exists())
	{
	    TextKOT.mkdirs();
	}
    }

    private void funCreateDetailJasperReport()
    {
	if ((dteToDate.getDate().getTime() - dteFromDate.getDate().getTime()) < 0)
	{
	    new frmOkPopUp(this, "Invalid date", "Error", 1).setVisible(true);
	}
	else
	{
	    Date dt1 = dteFromDate.getDate();
	    Date dt2 = dteToDate.getDate();
	    SimpleDateFormat ddmmyyyyDateFormat = new SimpleDateFormat("dd-MM-yyyy");
	    String fromDateToDisplay = ddmmyyyyDateFormat.format(dt1);
	    String toDateToDisplay = ddmmyyyyDateFormat.format(dt2);
	    fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());
	    toDate = objUtility.funGetFromToDate(dteToDate.getDate());
	    String pos = cmbPosCode.getSelectedItem().toString();

	    try
	    {
		////For Settlement details of Live and Q data
		StringBuilder sqlQData = new StringBuilder();

		List<clsReprintDocs> listOfReprintTextData = new ArrayList<>();
		sqlQData.setLength(0);
		sqlQData.append("select a.strBillNo,DATE_FORMAT(b.dtePOSDate,'%m-%d-%Y'),b.strUserCreated,ifnull(c.strReasonName,''),\n"
			+ " b.strRemarks,a.dblGrandTotal,\n"
			+ "time(b.dtePOSDate)  from tblbillhd a,tblaudit b left outer join tblreasonmaster c  \n"
			+ "on b.strReasonCode=c.strReasonCode  "
			+ " where a.strBillNo=b.strDocNo  "
			+ " and date(b.dtePOSDate) between '" + fromDate + "' and '" + toDate + "' ");

		if (!cmbUser.getSelectedItem().toString().equals("All"))
		{
		    sqlQData.append(" and b.strUserCreated='" + cmbUser.getSelectedItem().toString() + "'");
		}
		if (!cmbDocumentNo.getSelectedItem().toString().equals("All"))
		{
		    sqlQData.append(" and b.strDocNo='" + cmbDocumentNo.getSelectedItem().toString() + "'");
		}
		ResultSet rsSettlementWiseQData = clsGlobalVarClass.dbMysql.executeResultSet(sqlQData.toString());

		while (rsSettlementWiseQData.next())
		{
		    clsReprintDocs objReprint = new clsReprintDocs();
		    objReprint.setBillNo(rsSettlementWiseQData.getString(1));
		    objReprint.setDate(rsSettlementWiseQData.getString(2));
		    objReprint.setUser(rsSettlementWiseQData.getString(3));
		    objReprint.setReason(rsSettlementWiseQData.getString(4));
		    objReprint.setRemark(rsSettlementWiseQData.getString(5));
		    objReprint.setTotal(rsSettlementWiseQData.getDouble(6));
		    objReprint.setTime(rsSettlementWiseQData.getString(7));
		    listOfReprintTextData.add(objReprint);
		}
		rsSettlementWiseQData.close();
		
		sqlQData.setLength(0);
		sqlQData.append("select a.strBillNo,DATE_FORMAT(b.dtePOSDate,'%m-%d-%Y'),b.strUserCreated,ifnull(c.strReasonName,''),\n"
			+ " b.strRemarks,a.dblGrandTotal,\n"
			+ "time(b.dtePOSDate)  from tblqbillhd a,tblaudit b left outer join tblreasonmaster c  \n"
			+ "on b.strReasonCode=c.strReasonCode  "
			+ " where a.strBillNo=b.strDocNo  "
			+ " and date(b.dtePOSDate) between '" + fromDate + "' and '" + toDate + "' ");

		if (!cmbUser.getSelectedItem().toString().equals("All"))
		{
		    sqlQData.append(" and b.strUserCreated='" + cmbUser.getSelectedItem().toString() + "'");
		}
		if (!cmbDocumentNo.getSelectedItem().toString().equals("All"))
		{
		    sqlQData.append(" and b.strDocNo='" + cmbDocumentNo.getSelectedItem().toString() + "'");
		}
		rsSettlementWiseQData = clsGlobalVarClass.dbMysql.executeResultSet(sqlQData.toString());
		while (rsSettlementWiseQData.next())
		{
		    clsReprintDocs objReprint = new clsReprintDocs();
		    objReprint.setBillNo(rsSettlementWiseQData.getString(1));
		    objReprint.setDate(rsSettlementWiseQData.getString(2));
		    objReprint.setUser(rsSettlementWiseQData.getString(3));
		    objReprint.setReason(rsSettlementWiseQData.getString(4));
		    objReprint.setRemark(rsSettlementWiseQData.getString(5));
		    objReprint.setTotal(rsSettlementWiseQData.getDouble(6));
		    objReprint.setTime(rsSettlementWiseQData.getString(7));
		    listOfReprintTextData.add(objReprint);
		}
		rsSettlementWiseQData.close();

		fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());
		toDate = objUtility.funGetFromToDate(dteToDate.getDate());

		HashMap hm = new HashMap();
		hm.put("posName", pos);
		hm.put("fromDate", fromDateToDisplay);
		hm.put("toDate", toDateToDisplay);
		hm.put("userName", clsGlobalVarClass.gUserName);
		hm.put("imagePath", imagePath);
		hm.put("clientName", clsGlobalVarClass.gClientName);

		reportName = "com/POSReport/reports/rptReprintReport.jasper";
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);
		JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(listOfReprintTextData);

		JasperPrint print = JasperFillManager.fillReport(is, hm, beanColDataSource);
		List<JRPrintPage> pages = print.getPages();
		if (pages.size() == 0)
		{
		    JOptionPane.showMessageDialog(null, "Data not present for selected dates!!!");
		}
		else
		{
		    JRViewer viewer = new JRViewer(print);
		    JFrame jf = new JFrame();
		    jf.getContentPane().add(viewer);
		    jf.validate();
		    jf.setVisible(true);
		    jf.setSize(new Dimension(850, 750));
		    jf.setLocationRelativeTo(this);
		}

	    }
	    catch (Exception e)
	    {
		e.printStackTrace();
	    }
	}
    }

    private void funCreateSummaryJasperReport()
    {
	if ((dteToDate.getDate().getTime() - dteFromDate.getDate().getTime()) < 0)
	{
	    new frmOkPopUp(this, "Invalid date", "Error", 1).setVisible(true);
	}
	else
	{
	    Date dt1 = dteFromDate.getDate();
	    Date dt2 = dteToDate.getDate();
	    SimpleDateFormat ddmmyyyyDateFormat = new SimpleDateFormat("dd-MM-yyyy");
	    String fromDateToDisplay = ddmmyyyyDateFormat.format(dt1);
	    String toDateToDisplay = ddmmyyyyDateFormat.format(dt2);
	    fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());
	    toDate = objUtility.funGetFromToDate(dteToDate.getDate());
	    String pos = cmbPosCode.getSelectedItem().toString();

	    try
	    {
		////For Settlement details of Live and Q data
		StringBuilder sqlQData = new StringBuilder();

		List<clsReprintDocs> listOfReprintTextData = new ArrayList<>();
		sqlQData.setLength(0);
		sqlQData.append("select  a.strBillNo,DATE_FORMAT(b.dtePOSDate,'%m-%d-%Y'),b.strUserCreated,a.dblGrandTotal,count(*)as count \n"
			+ "from tblbillhd a,tblaudit b left outer join tblreasonmaster c on b.strReasonCode=c.strReasonCode  where a.strBillNo=b.strDocNo  "
			+ " and date(b.dtePOSDate) between '" + fromDate + "' and '" + toDate + "'");
		if (!cmbUser.getSelectedItem().toString().equals("All"))
		{
		    sqlQData.append(" and b.strUserCreated='" + cmbUser.getSelectedItem().toString() + "'");
		}

		if (!cmbDocumentNo.getSelectedItem().toString().equals("All"))
		{
		    sqlQData.append(" and b.strDocNo='" + cmbDocumentNo.getSelectedItem().toString() + "'");
		}
		sqlQData.append(" group by a.strBillNo,b.strUserCreated ");

		ResultSet rsSettlementWiseQData = clsGlobalVarClass.dbMysql.executeResultSet(sqlQData.toString());

		while (rsSettlementWiseQData.next())
		{
		    clsReprintDocs objReprint = new clsReprintDocs();
		    objReprint.setBillNo(rsSettlementWiseQData.getString(1));
		    objReprint.setDate(rsSettlementWiseQData.getString(2));
		    objReprint.setUser(rsSettlementWiseQData.getString(3));
		    objReprint.setTotal(rsSettlementWiseQData.getDouble(4));
		    objReprint.setCount(rsSettlementWiseQData.getInt(5));
		    listOfReprintTextData.add(objReprint);
		}
		sqlQData.setLength(0);
		sqlQData.append("select  a.strBillNo,DATE_FORMAT(b.dtePOSDate,'%m-%d-%Y'),b.strUserCreated,a.dblGrandTotal,count(*)as count \n"
			+ "from tblqbillhd a,tblaudit b left outer join tblreasonmaster c on b.strReasonCode=c.strReasonCode where a.strBillNo=b.strDocNo  "
			+ " and date(b.dtePOSDate) between '" + fromDate + "' and '" + toDate + "'");
		if (!cmbUser.getSelectedItem().toString().equals("All"))
		{
		    sqlQData.append(" and b.strUserCreated='" + cmbUser.getSelectedItem().toString() + "'");
		}

		if (!cmbDocumentNo.getSelectedItem().toString().equals("All"))
		{
		    sqlQData.append(" and b.strDocNo='" + cmbDocumentNo.getSelectedItem().toString() + "'");
		}
		sqlQData.append(" group by a.strBillNo,b.strUserCreated ");
		rsSettlementWiseQData = clsGlobalVarClass.dbMysql.executeResultSet(sqlQData.toString());

		while (rsSettlementWiseQData.next())
		{
		    clsReprintDocs objReprint = new clsReprintDocs();
		    objReprint.setBillNo(rsSettlementWiseQData.getString(1));
		    objReprint.setDate(rsSettlementWiseQData.getString(2));
		    objReprint.setUser(rsSettlementWiseQData.getString(3));
		    objReprint.setTotal(rsSettlementWiseQData.getDouble(4));
		    objReprint.setCount(rsSettlementWiseQData.getInt(5));
		    listOfReprintTextData.add(objReprint);
		}

		fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());
		toDate = objUtility.funGetFromToDate(dteToDate.getDate());

		HashMap hm = new HashMap();
		hm.put("posName", pos);
		hm.put("fromDate", fromDateToDisplay);
		hm.put("toDate", toDateToDisplay);
		hm.put("userName", clsGlobalVarClass.gUserName);
		hm.put("fromDateToDisplay", fromDateToDisplay);
		hm.put("toDateToDisplay", toDateToDisplay);
		hm.put("imagePath", imagePath);
		hm.put("clientName", clsGlobalVarClass.gClientName);

		reportName = "com/POSReport/reports/rptReprintSummaryReport.jasper";
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(reportName);
		JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(listOfReprintTextData);

		JasperPrint print = JasperFillManager.fillReport(is, hm, beanColDataSource);
		List<JRPrintPage> pages = print.getPages();
		if (pages.size() == 0)
		{
		    JOptionPane.showMessageDialog(null, "Data not present for selected dates!!!");
		}
		else
		{
		    JRViewer viewer = new JRViewer(print);
		    JFrame jf = new JFrame();
		    jf.getContentPane().add(viewer);
		    jf.validate();
		    jf.setVisible(true);
		    jf.setSize(new Dimension(850, 750));
		    jf.setLocationRelativeTo(this);
		}

	    }
	    catch (Exception e)
	    {
		e.printStackTrace();
	    }
	}
    }

    private void funGenerateExcelSheetOfReport()
    {
	Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
	List<String> arrHeaderList = new ArrayList<String>();
	List<String> arrListTotal = new ArrayList<String>();
	if ((dteToDate.getDate().getTime() - dteFromDate.getDate().getTime()) < 0)
	{
	    new frmOkPopUp(this, "Invalid date", "Error", 1).setVisible(true);
	}
	else
	{
	    Date dt1 = dteFromDate.getDate();
	    Date dt2 = dteToDate.getDate();
	    SimpleDateFormat ddmmyyyyDateFormat = new SimpleDateFormat("dd-MM-yyyy");
	    String fromDateToDisplay = ddmmyyyyDateFormat.format(dt1);
	    String toDateToDisplay = ddmmyyyyDateFormat.format(dt2);
	    fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());
	    toDate = objUtility.funGetFromToDate(dteToDate.getDate());

	    try
	    {
		////For Settlement details of Live and Q data
		StringBuilder sqlQData = new StringBuilder();

		List<clsReprintDocs> listOfReprintTextData = new ArrayList<clsReprintDocs>();
		sqlQData.setLength(0);
		sqlQData.append("select a.strBillNo,DATE_FORMAT(b.dtePOSDate,'%m-%d-%Y'),b.strUserCreated,ifnull(c.strReasonName,'') "
			+ " ,b.strRemarks,a.dblGrandTotal,time(b.dtePOSDate) "
			+ " from tblbillhd a,tblaudit b left outer join tblreasonmaster c on b.strReasonCode=c.strReasonCode "
			+ " where a.strBillNo=b.strDocNo  "
			+ " and date(b.dtePOSDate) between '" + fromDate + "' and '" + toDate + "' ");

		if (!cmbUser.getSelectedItem().toString().equals("All"))
		{
		    sqlQData.append(" and b.strUserCreated='" + cmbUser.getSelectedItem().toString() + "'");
		}

		if (!cmbDocumentNo.getSelectedItem().toString().equals("All"))
		{
		    sqlQData.append(" and b.strDocNo='" + cmbDocumentNo.getSelectedItem().toString() + "'");
		}
		ResultSet rsSettlementWiseQData = clsGlobalVarClass.dbMysql.executeResultSet(sqlQData.toString());

		while (rsSettlementWiseQData.next())
		{
		    clsReprintDocs objReprint = new clsReprintDocs();
		    objReprint.setBillNo(rsSettlementWiseQData.getString(1));
		    objReprint.setDate(rsSettlementWiseQData.getString(2));
		    objReprint.setUser(rsSettlementWiseQData.getString(3));
		    objReprint.setReason(rsSettlementWiseQData.getString(4));
		    objReprint.setRemark(rsSettlementWiseQData.getString(5));
		    objReprint.setTotal(rsSettlementWiseQData.getDouble(6));
		    objReprint.setTime(rsSettlementWiseQData.getString(7));
		    listOfReprintTextData.add(objReprint);
		}

		sqlQData.setLength(0);
		sqlQData.append("select a.strBillNo,DATE_FORMAT(b.dtePOSDate,'%m-%d-%Y'),b.strUserCreated,ifnull(c.strReasonName,'') "
			+ " ,b.strRemarks,a.dblGrandTotal,time(b.dtePOSDate) "
			+ " from tblqbillhd a,tblaudit b left outer join tblreasonmaster c on b.strReasonCode=c.strReasonCode "
			+ " where a.strBillNo=b.strDocNo  "
			+ " and date(b.dtePOSDate) between '" + fromDate + "' and '" + toDate + "' ");

		if (!cmbUser.getSelectedItem().toString().equals("All"))
		{
		    sqlQData.append(" and b.strUserCreated='" + cmbUser.getSelectedItem().toString() + "'");
		}

		if (!cmbDocumentNo.getSelectedItem().toString().equals("All"))
		{
		    sqlQData.append(" and b.strDocNo='" + cmbDocumentNo.getSelectedItem().toString() + "'");
		}
		rsSettlementWiseQData = clsGlobalVarClass.dbMysql.executeResultSet(sqlQData.toString());
		int i = 1;
		while (rsSettlementWiseQData.next())
		{
		    clsReprintDocs objReprint = new clsReprintDocs();
		    objReprint.setBillNo(rsSettlementWiseQData.getString(1));
		    objReprint.setDate(rsSettlementWiseQData.getString(2));
		    objReprint.setUser(rsSettlementWiseQData.getString(3));
		    objReprint.setReason(rsSettlementWiseQData.getString(4));
		    objReprint.setRemark(rsSettlementWiseQData.getString(5));
		    objReprint.setTotal(rsSettlementWiseQData.getDouble(6));
		    objReprint.setTime(rsSettlementWiseQData.getString(7));
		    listOfReprintTextData.add(objReprint);

		}
		DecimalFormat decFormatFor2Decimal = new DecimalFormat("0.00");
		for (clsReprintDocs objReprint : listOfReprintTextData)
		{
		    List<String> arrListItem = new ArrayList<String>();
		    arrListItem.add(objReprint.getBillNo());
		    arrListItem.add(objReprint.getDate());
		    arrListItem.add(objReprint.getUser());
		    arrListItem.add(objReprint.getReason());
		    arrListItem.add(objReprint.getRemark());
		    arrListItem.add(String.valueOf(decFormatFor2Decimal.format(objReprint.getTotal())));
		    arrListItem.add(objReprint.getTime());
		    mapExcelItemDtl.put(i, arrListItem);
		    i++;
		}

		arrHeaderList.add("Serial No");
		arrHeaderList.add("Bill No");
		arrHeaderList.add("Date");
		arrHeaderList.add("User");
		arrHeaderList.add("Reason");
		arrHeaderList.add("Remark");
		arrHeaderList.add("Total");
		arrHeaderList.add("Time");

		List<String> arrparameterList = new ArrayList<String>();
		arrparameterList.add("Reprint Docs Details Report");
		arrparameterList.add("POS" + " : " + cmbPosCode.getSelectedItem().toString());
		arrparameterList.add("FromDate" + " : " + fromDateToDisplay);
		arrparameterList.add("ToDate" + " : " + toDateToDisplay);
		arrparameterList.add("User" + " : " + cmbUser.getSelectedItem().toString());
		arrparameterList.add(" ");

		objUtility.funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "reprintDetailExcelSheet");

	    }
	    catch (Exception e)
	    {
		e.printStackTrace();
	    }
	}
    }

    private void funGenerateSummaryExcelSheetOfReport()
    {
	Map<Integer, List<String>> mapExcelItemDtl = new HashMap<Integer, List<String>>();
	List<String> arrHeaderList = new ArrayList<String>();
	List<String> arrListTotal = new ArrayList<String>();
	if ((dteToDate.getDate().getTime() - dteFromDate.getDate().getTime()) < 0)
	{
	    new frmOkPopUp(this, "Invalid date", "Error", 1).setVisible(true);
	}
	else
	{
	    Date dt1 = dteFromDate.getDate();
	    Date dt2 = dteToDate.getDate();
	    SimpleDateFormat ddmmyyyyDateFormat = new SimpleDateFormat("dd-MM-yyyy");
	    String fromDateToDisplay = ddmmyyyyDateFormat.format(dt1);
	    String toDateToDisplay = ddmmyyyyDateFormat.format(dt2);
	    fromDate = objUtility.funGetFromToDate(dteFromDate.getDate());
	    toDate = objUtility.funGetFromToDate(dteToDate.getDate());
	    try
	    {
		////For Settlement details of Live and Q data
		StringBuilder sqlQData = new StringBuilder();

		List<clsReprintDocs> listOfReprintTextData = new ArrayList<clsReprintDocs>();
		sqlQData.setLength(0);
		sqlQData.append("select  a.strBillNo,DATE_FORMAT(b.dtePOSDate,'%m-%d-%Y'),b.strUserCreated,a.dblGrandTotal,count(*)as count \n"
			+ "from tblbillhd a,tblaudit b left outer join tblreasonmaster c on b.strReasonCode=c.strReasonCode where a.strBillNo=b.strDocNo "
			+ " and date(b.dtePOSDate) between '" + fromDate + "' and '" + toDate + "'");
		if (!cmbUser.getSelectedItem().toString().equals("All"))
		{
		    sqlQData.append(" and b.strUserCreated='" + cmbUser.getSelectedItem().toString() + "'");
		}

		if (!cmbDocumentNo.getSelectedItem().toString().equals("All"))
		{
		    sqlQData.append(" and b.strDocNo='" + cmbDocumentNo.getSelectedItem().toString() + "'");
		}
		sqlQData.append(" group by a.strBillNo,b.strUserCreated ");
		ResultSet rsSettlementWiseQData = clsGlobalVarClass.dbMysql.executeResultSet(sqlQData.toString());

		while (rsSettlementWiseQData.next())
		{
		    clsReprintDocs objReprint = new clsReprintDocs();
		    objReprint.setBillNo(rsSettlementWiseQData.getString(1));
		    objReprint.setDate(rsSettlementWiseQData.getString(2));
		    objReprint.setUser(rsSettlementWiseQData.getString(3));
		    objReprint.setTotal(rsSettlementWiseQData.getDouble(4));
		    objReprint.setCount(rsSettlementWiseQData.getInt(5));
		    listOfReprintTextData.add(objReprint);
		}

		sqlQData.setLength(0);
		sqlQData.append("select  a.strBillNo,DATE_FORMAT(b.dtePOSDate,'%m-%d-%Y'),b.strUserCreated,a.dblGrandTotal,count(*)as count \n"
			+ "from tblqbillhd a,tblaudit b left outer join tblreasonmaster c on b.strReasonCode=c.strReasonCode where a.strBillNo=b.strDocNo  "
			+ " and date(b.dtePOSDate) between '" + fromDate + "' and '" + toDate + "'");
		if (!cmbUser.getSelectedItem().toString().equals("All"))
		{
		    sqlQData.append(" and b.strUserCreated='" + cmbUser.getSelectedItem().toString() + "'");
		}

		if (!cmbDocumentNo.getSelectedItem().toString().equals("All"))
		{
		    sqlQData.append(" and b.strDocNo='" + cmbDocumentNo.getSelectedItem().toString() + "'");
		}
		sqlQData.append(" group by a.strBillNo,b.strUserCreated ");
		rsSettlementWiseQData = clsGlobalVarClass.dbMysql.executeResultSet(sqlQData.toString());
		int i = 1;
		while (rsSettlementWiseQData.next())
		{
		    clsReprintDocs objReprint = new clsReprintDocs();
		    objReprint.setBillNo(rsSettlementWiseQData.getString(1));
		    objReprint.setDate(rsSettlementWiseQData.getString(2));
		    objReprint.setUser(rsSettlementWiseQData.getString(3));
		    objReprint.setTotal(rsSettlementWiseQData.getDouble(4));
		    objReprint.setCount(rsSettlementWiseQData.getInt(5));
		    listOfReprintTextData.add(objReprint);
		}

		DecimalFormat decFormatFor2Decimal = new DecimalFormat("0.00");
		for (clsReprintDocs objReprint : listOfReprintTextData)
		{
		    List<String> arrListItem = new ArrayList<String>();
		    arrListItem.add(objReprint.getBillNo());
		    arrListItem.add(objReprint.getDate());
		    arrListItem.add(objReprint.getUser());
		    arrListItem.add(String.valueOf(decFormatFor2Decimal.format(objReprint.getTotal())));
		    arrListItem.add(String.valueOf(objReprint.getCount()));
		    mapExcelItemDtl.put(i, arrListItem);
		    i++;
		}

		arrHeaderList.add("Serial No");
		arrHeaderList.add("Bill No");
		arrHeaderList.add("Date");
		arrHeaderList.add("User");
		arrHeaderList.add("Total");
		arrHeaderList.add("Count");

		List<String> arrparameterList = new ArrayList<String>();
		arrparameterList.add("Reprint Docs Summary Report");
		arrparameterList.add("POS" + " : " + cmbPosCode.getSelectedItem().toString());
		arrparameterList.add("FromDate" + " : " + fromDateToDisplay);
		arrparameterList.add("ToDate" + " : " + toDateToDisplay);
		arrparameterList.add("User" + " : " + cmbUser.getSelectedItem().toString());
		arrparameterList.add(" ");

		objUtility.funCreateExcelSheet(arrparameterList, arrHeaderList, mapExcelItemDtl, arrListTotal, "reprintSummaryExcelSheet");

	    }
	    catch (Exception e)
	    {
		e.printStackTrace();
	    }
	}
    }


    private void btnViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnViewActionPerformed

	try
	{
	    if (cmbReportType.getSelectedItem().toString().equalsIgnoreCase("A4 Size Report"))
	    {
		if (cmbType.getSelectedItem().toString().equals("Detail"))
		{
		    funCreateDetailJasperReport();
		}
		else
		{
		    funCreateSummaryJasperReport();
		}
	    }

	    else if (cmbReportType.getSelectedItem().toString().equalsIgnoreCase("Text File-40 Column Report"))
	    {
		if (cmbType.getSelectedItem().toString().equals("Detail"))
		{
		    funReprintDocsReport();
		}
		else
		{
		    funReprintSummary();
		}
	    }
	    else
	    {
		if (cmbType.getSelectedItem().toString().equals("Detail"))
		{
		    funGenerateExcelSheetOfReport();
		}
		else
		{
		    funGenerateSummaryExcelSheetOfReport();
		}
	    }

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }//GEN-LAST:event_btnViewActionPerformed

    private void cmbPosCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbPosCodeActionPerformed
	// TODO add your handling code here:

    }//GEN-LAST:event_cmbPosCodeActionPerformed

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("Reprint Docs Report");
    }//GEN-LAST:event_btnBackActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("Reprint Docs Report");
    }//GEN-LAST:event_formWindowClosed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
	// TODO add your handling code here:
	clsGlobalVarClass.hmActiveForms.remove("Reprint Docs Report");
    }//GEN-LAST:event_formWindowClosing

    private void cmbReportTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbReportTypeActionPerformed
	// TODO add your handling code here:
    }//GEN-LAST:event_cmbReportTypeActionPerformed

    private void cmbDocumentNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbDocumentNoActionPerformed
	// TODO add your handling code here:
    }//GEN-LAST:event_cmbDocumentNoActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnView;
    private javax.swing.JComboBox cmbDocumentNo;
    private javax.swing.JComboBox cmbPosCode;
    private javax.swing.JComboBox cmbReportType;
    private javax.swing.JComboBox cmbType;
    private javax.swing.JComboBox cmbUser;
    private com.toedter.calendar.JDateChooser dteFromDate;
    private com.toedter.calendar.JDateChooser dteToDate;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.JLabel lblBillNumber;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblFromDate;
    private javax.swing.JLabel lblHOSign;
    private javax.swing.JLabel lblModuleName;
    private javax.swing.JLabel lblPosName;
    private javax.swing.JLabel lblProductName;
    private javax.swing.JLabel lblReportType;
    private javax.swing.JLabel lblReprintDocsReport;
    private javax.swing.JLabel lblToDate;
    private javax.swing.JLabel lblUser;
    private javax.swing.JLabel lblUserCode;
    private javax.swing.JLabel lblfromName;
    private javax.swing.JLabel lblposName;
    private javax.swing.JLabel lbltype;
    private javax.swing.JPanel pnlBackGround;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JPanel pnlheader;
    // End of variables declaration//GEN-END:variables
}
