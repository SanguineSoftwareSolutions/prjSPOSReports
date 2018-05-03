/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.POSReport.view;

import com.POSGlobal.controller.clsGlobalVarClass;
import com.POSGlobal.controller.clsUtility;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.text.html.HTMLEditorKit;

/**
 *
 * @author sss11
 */
public class frmTableStatusReport extends javax.swing.JFrame {
     private clsUtility objUtility;

    public frmTableStatusReport()
    {
        initComponents();
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        objUtility=new clsUtility();
       funFillAllTables();
    }
     /**
     *  Component initialization
     */
    public void funFillAllTables()
    {  
        try
        {
            int cntRows=0,totalTables=0;
            
           // lblProductName.setText("JPOS - Table Status  All Tables");
            String sql ="select count(strTableName) from tbltablemaster";
            ResultSet rsTables = clsGlobalVarClass.dbMysql.executeResultSet(sql);
            if(rsTables.next())
            {
                cntRows=rsTables.getInt(1);
                totalTables=rsTables.getInt(1);
            }
            if(cntRows>=10)
            {
                cntRows=10;
            }
            sql ="select strTableName,strStatus from tbltablemaster order by strTableName";
            rsTables = clsGlobalVarClass.dbMysql.executeResultSet(sql);
            List<Map> listAllTables=new ArrayList();
            java.util.Vector vAllTables=new java.util.Vector();
            Map<String,String> mapAllTables=new HashMap<String,String>();
            int cnt=0,cnt1=0;
            while(rsTables.next())
            {
                String tableName =rsTables.getString(1);
                //System.out.println(tableName);
                String status=rsTables.getString(2);
                vAllTables.add(tableName);
                String color="green";
                if(status.equalsIgnoreCase("occupied"))
                {
                    color="red";
                }
                else if(status.equalsIgnoreCase("billed"))
                {
                    color="blue";
                }
                mapAllTables.put(tableName,color);
                //System.out.println(mapAllTables.size());                
                cnt++;
                if(cnt%cntRows==0)
                {
                    cnt1=cnt1+cntRows;
                    listAllTables.add(mapAllTables);
                    mapAllTables=new HashMap<String,String>();
                }                
            }
            //System.out.println("Cnt="+cnt+"\tTotal="+totalTables);
            if(cnt>cnt1)
            {
                listAllTables.add(mapAllTables);
            }
            
            txtPanelTables.setEditorKit(new HTMLEditorKit());
            String text="";
            int k=0;
            int m=cntRows;
            //System.out.println("List Size="+listAllTables.size());
            for(int i=0;i<listAllTables.size();i++)
            {
                Map mapTempTables=listAllTables.get(i);
                if(i>0)
                {
                    m=m+mapTempTables.size();
                }
                for(int j=k;j<m;j++)
                {
                    String color=mapTempTables.get(vAllTables.elementAt(j).toString()).toString();
                    text+="&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
                    text+= "<font size=\"4\" face=\"verdana\" color=\""+color+"\">"+vAllTables.elementAt(j).toString()+"</font>";
                }
                text+="<br><br>";
                k=m;
                //m=m+cntRows;
            }
            String finalText="<html>"+text+"</html>";
            txtPanelTables.setText(finalText);
            
        }catch(Exception e)
         {
                e.printStackTrace();
         }
                
    }
    
    /**
     * Fill  Occupied Tables
     */
    public void funFillOccupiedTables()
    {  
        try
        {
            int cntRows=0;
            lblProductName.setText("JPOS - Table Status  Busy Tables");
            /*String sql ="select a.strTableName,a.strStatus from tbltablemaster a,tblitemrtemp b "
                    + "where a.strTableNo=b.strTableNo";*/
            String sql ="select count(strTableName) from tbltablemaster where strStatus='Occupied' order by strTableName";
            ResultSet rsTables = clsGlobalVarClass.dbMysql.executeResultSet(sql);
            if(rsTables.next())
            {
                cntRows=rsTables.getInt(1);
            }
            if(cntRows>=10)
            {
                cntRows=10;
            }
            /*String sql ="select a.strTableName,a.strStatus from tbltablemaster a,tblitemrtemp b "
                    + "where a.strTableNo=b.strTableNo";*/
            sql ="select strTableName,strStatus from tbltablemaster where strStatus='Occupied' order by strTableName";
            rsTables = clsGlobalVarClass.dbMysql.executeResultSet(sql);
            List<Map> listOccupiedTables=new ArrayList();
            java.util.Vector vOccpiedTables=new java.util.Vector();
            Map<String,String> mapOccupiedTables=new HashMap<String,String>();
            int cnt=0,cnt1=0;
            while(rsTables.next())
            {                
                String tableName =rsTables.getString(1);
                String status=rsTables.getString(2);
                vOccpiedTables.add(tableName);
                String color="green";                
                if(status.equalsIgnoreCase("occupied"))
                {
                    color="red";
                }
                else if(status.equalsIgnoreCase("billed"))
                {
                    color="blue";
                }
                mapOccupiedTables.put(tableName,color);
                cnt++;
                if(cnt%cntRows==0)
                {
                    cnt1=cnt1+cntRows;
                    listOccupiedTables.add(mapOccupiedTables);
                    mapOccupiedTables=new HashMap<String,String>();
                }                
            }
            if(cnt>cnt1)
            {
                listOccupiedTables.add(mapOccupiedTables);
            }
            
            txtPanelTables.setEditorKit(new HTMLEditorKit());
            String text="";
            int k=0;
            int m=cntRows;
            for(int i=0;i<listOccupiedTables.size();i++)
            {
                Map mapTempTables=listOccupiedTables.get(i);
                if(i>0)
                {
                    m=m+mapTempTables.size();
                }
                for(int j=k;j<m;j++)
                {
                    String color=mapTempTables.get(vOccpiedTables.elementAt(j).toString()).toString();
                    text+="&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
                    text+= "<font size=\"4\" face=\"verdana\" color=\""+color+"\">"+vOccpiedTables.elementAt(j).toString()+"</font>";
                }
                text+="<br><br>";
                k=m;
                //m=m+cntRows;
            }
            String finalText="<html>"+text+"</html>";
            txtPanelTables.setText(finalText);
            //txtPanelTables.setForeground(Color.red);
            
        }catch(Exception e)
         {
                e.printStackTrace();
         }                
    }
    
    /**
     * Fill Billed Tables
     */
    public void funFillBilledTables()
    {  
        try
        {
            int cntRows=0;
            lblProductName.setText("JPOS - Table Status  Billed Tables");
            /*String sql ="select a.strTableName,a.strStatus from tbltablemaster a,tblitemrtemp b "
                    + "where a.strTableNo=b.strTableNo";*/
            String sql ="select count(strTableName) from tbltablemaster where strStatus='Billed' order by strTableName";
            ResultSet rsTables = clsGlobalVarClass.dbMysql.executeResultSet(sql);
            if(rsTables.next())
            {
                cntRows=rsTables.getInt(1);
            }
            if(cntRows>=10)
            {
                cntRows=10;
            }
            sql ="select strTableName,strStatus from tbltablemaster where strStatus='Billed' order by strTableName";
            rsTables = clsGlobalVarClass.dbMysql.executeResultSet(sql);
            List<Map> listBilledTables=new ArrayList();
            java.util.Vector vBilledTables=new java.util.Vector();
            Map<String,String> mapBilledTables=new HashMap<String,String>();
            int cnt=0,cnt1=0;
            while(rsTables.next())
            {
                String tableName =rsTables.getString(1);
                String status=rsTables.getString(2);
                vBilledTables.add(tableName);
                String color="green";
                if(status.equalsIgnoreCase("Billed"))
                {
                    color="blue";
                }
                mapBilledTables.put(tableName,color);
                cnt++;
                if(cnt%cntRows==0)
                {
                    cnt1=cnt1+cntRows;
                    listBilledTables.add(mapBilledTables);
                    mapBilledTables=new HashMap<String,String>();
                }
            }
            if(cnt>cnt1)
            {
                listBilledTables.add(mapBilledTables);
            }
            
            txtPanelTables.setEditorKit(new HTMLEditorKit());
            String text="";
            int k=0;
            int m=cntRows;
            for(int i=0;i<listBilledTables.size();i++)
            {
                Map mapTempTables=listBilledTables.get(i);
                if(i>0)
                {
                    m=m+mapTempTables.size();
                }
                for(int j=k;j<m;j++)
                {
                    String color=mapTempTables.get(vBilledTables.elementAt(j).toString()).toString();
                    text+="&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
                    text+= "<font size=\"4\" face=\"verdana\" color=\""+color+"\">"+vBilledTables.elementAt(j).toString()+"</font>";
                }
                text+="<br><br>";
                k=m;
                //m=m+cntRows;
            }
            String finalText="<html>"+text+"</html>";
            txtPanelTables.setText(finalText);
            //txtPanelTables.setForeground(Color.red);
            
        }catch(Exception e)
         {
                e.printStackTrace();
         }                
    }
    
    /**
     * Fill Vacant Tables
     */
    public void funFillVacantTables()
    {  
        try
        {
            int cntRows=0;
            lblProductName.setText("JPOS - Table Status  Vacant Tables");
            /*String sql ="select a.strTableName,a.strStatus from tbltablemaster a,tblitemrtemp b "
                    + "where a.strTableNo=b.strTableNo";*/
            String sql ="select count(strTableName) from tbltablemaster where strStatus='Normal' order by strTableName";
            ResultSet rsTables = clsGlobalVarClass.dbMysql.executeResultSet(sql);
            if(rsTables.next())
            {
                cntRows=rsTables.getInt(1);
            }
            if(cntRows>=10)
            {
                cntRows=10;
            }
            
            /*String sql ="select a.strTableName,a.strStatus from tbltablemaster a,tblitemrtemp b "
                    + "where a.strTableNo=b.strTableNo";*/
            sql ="select strTableName,strStatus from tbltablemaster where strStatus='Normal' order by strTableName";
            rsTables = clsGlobalVarClass.dbMysql.executeResultSet(sql);
            List<Map> listVacantTables=new ArrayList();
            java.util.Vector vVacantTables=new java.util.Vector();
            Map<String,String> mapVacantTables=new HashMap<String,String>();
            int cnt=0,cnt1=0;
            while(rsTables.next())
            {                
                String tableName =rsTables.getString(1);
                
                vVacantTables.add(tableName);
                String color="green";
                mapVacantTables.put(tableName,color);
                cnt++;
                if(cnt%cntRows==0)
                {
                    cnt1=cnt1+cntRows;
                    listVacantTables.add(mapVacantTables);
                    mapVacantTables=new HashMap<String,String>();
                }
            }
            if(cnt>cnt1)
            {
                listVacantTables.add(mapVacantTables);
            }
            
            txtPanelTables.setEditorKit(new HTMLEditorKit());
            String text="";
            int k=0;
            int m=cntRows;
            for(int i=0;i<listVacantTables.size();i++)
            {
                Map mapTempTables=listVacantTables.get(i);
                if(i>0)
                {
                    m=m+mapTempTables.size();
                }
                for(int j=k;j<m;j++)
                {
                    String color=mapTempTables.get(vVacantTables.elementAt(j).toString()).toString();
                    text+="&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
                    text+= "<font size=\"4\" face=\"verdana\" color=\""+color+"\">"+vVacantTables.elementAt(j).toString()+"</font>";
                }
                text+="<br><br>";
                k=m;
                //m=m+cntRows;
            }
            String finalText="<html>"+text+"</html>";
            txtPanelTables.setText(finalText);
            //txtPanelTables.setForeground(Color.red);
            
        }catch(Exception e)
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
        btnAll = new javax.swing.JButton();
        btnVacant = new javax.swing.JButton();
        btnBilled = new javax.swing.JButton();
        btnOccupied = new javax.swing.JButton();
        btnExit = new javax.swing.JButton();
        lblVacant = new javax.swing.JLabel();
        lbloccupied = new javax.swing.JLabel();
        lblBilled = new javax.swing.JLabel();
        pnltables = new javax.swing.JScrollPane();
        txtPanelTables = new javax.swing.JTextPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setExtendedState(MAXIMIZED_BOTH);
        setMinimumSize(new java.awt.Dimension(800, 600));
        setUndecorated(true);

        pnlheader.setBackground(new java.awt.Color(69, 164, 238));
        pnlheader.setLayout(new javax.swing.BoxLayout(pnlheader, javax.swing.BoxLayout.LINE_AXIS));

        lblProductName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblProductName.setForeground(new java.awt.Color(255, 255, 255));
        lblProductName.setText("SPOS -");
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
        lblformName.setText("- Table Status");
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

        pnlBackGround.setLayout(new java.awt.GridBagLayout());

        pnlMain.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, new java.awt.Color(204, 204, 204), new java.awt.Color(204, 204, 204), new java.awt.Color(153, 153, 153), new java.awt.Color(153, 153, 153)));
        pnlMain.setMinimumSize(new java.awt.Dimension(800, 570));
        pnlMain.setOpaque(false);

        btnAll.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        btnAll.setForeground(new java.awt.Color(255, 255, 255));
        btnAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgPOSChooser3.png"))); // NOI18N
        btnAll.setText("ALL");
        btnAll.setToolTipText("View ALL");
        btnAll.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAll.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnAllMouseClicked(evt);
            }
        });

        btnVacant.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        btnVacant.setForeground(new java.awt.Color(255, 255, 255));
        btnVacant.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgPOSChooser2.png"))); // NOI18N
        btnVacant.setText("Vacant");
        btnVacant.setToolTipText("View Vacant");
        btnVacant.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnVacant.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnVacantMouseClicked(evt);
            }
        });

        btnBilled.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        btnBilled.setForeground(new java.awt.Color(255, 255, 255));
        btnBilled.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgPOSChooser3.png"))); // NOI18N
        btnBilled.setText("Billed");
        btnBilled.setToolTipText("View Billed");
        btnBilled.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnBilled.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnBilledMouseClicked(evt);
            }
        });
        btnBilled.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnBilledActionPerformed(evt);
            }
        });

        btnOccupied.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        btnOccupied.setForeground(new java.awt.Color(255, 255, 255));
        btnOccupied.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgPOSChooser2.png"))); // NOI18N
        btnOccupied.setText("Occupied");
        btnOccupied.setToolTipText("View Occupied");
        btnOccupied.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnOccupied.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnOccupiedMouseClicked(evt);
            }
        });

        btnExit.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        btnExit.setForeground(new java.awt.Color(255, 255, 255));
        btnExit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/POSReport/images/imgPOSChooser3.png"))); // NOI18N
        btnExit.setText("EXIT");
        btnExit.setToolTipText("Exit Window ");
        btnExit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExit.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnExitMouseClicked(evt);
            }
        });

        lblVacant.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        lblVacant.setForeground(new java.awt.Color(0, 204, 0));
        lblVacant.setText("VACANT");

        lbloccupied.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        lbloccupied.setForeground(new java.awt.Color(255, 0, 0));
        lbloccupied.setText("OCCUPIED");

        lblBilled.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        lblBilled.setForeground(new java.awt.Color(0, 0, 255));
        lblBilled.setText("BILLED");

        txtPanelTables.setEditable(false);
        pnltables.setViewportView(txtPanelTables);

        javax.swing.GroupLayout pnlMainLayout = new javax.swing.GroupLayout(pnlMain);
        pnlMain.setLayout(pnlMainLayout);
        pnlMainLayout.setHorizontalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMainLayout.createSequentialGroup()
                .addGap(0, 10, Short.MAX_VALUE)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addComponent(pnltables, javax.swing.GroupLayout.PREFERRED_SIZE, 660, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnAll, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnVacant, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnBilled, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnOccupied, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnExit, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(lbloccupied, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(110, 110, 110)
                        .addComponent(lblVacant, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(170, 170, 170)
                        .addComponent(lblBilled, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        pnlMainLayout.setVerticalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnltables, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(btnAll, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20)
                        .addComponent(btnVacant, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20)
                        .addComponent(btnBilled, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20)
                        .addComponent(btnOccupied, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)
                        .addComponent(btnExit, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(20, 20, 20)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lbloccupied, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblVacant, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblBilled))
                .addGap(0, 16, Short.MAX_VALUE))
        );

        pnlBackGround.add(pnlMain, new java.awt.GridBagConstraints());

        getContentPane().add(pnlBackGround, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAllMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnAllMouseClicked
        funFillAllTables();
    }//GEN-LAST:event_btnAllMouseClicked

    private void btnVacantMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnVacantMouseClicked
        // TODO add your handling code here:
        funFillVacantTables();
    }//GEN-LAST:event_btnVacantMouseClicked

    private void btnBilledMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnBilledMouseClicked
        // TODO add your handling code here:
        funFillBilledTables();
    }//GEN-LAST:event_btnBilledMouseClicked

    private void btnBilledActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBilledActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnBilledActionPerformed

    private void btnOccupiedMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnOccupiedMouseClicked
        // TODO add your handling code here:
        funFillOccupiedTables();
    }//GEN-LAST:event_btnOccupiedMouseClicked

    private void btnExitMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnExitMouseClicked
        // TODO add your handling code here:
        dispose();
    }//GEN-LAST:event_btnExitMouseClicked

    private void lblProductNameMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblProductNameMouseClicked
    {//GEN-HEADEREND:event_lblProductNameMouseClicked
        // TODO add your handling code here:
        objUtility=new clsUtility();
    }//GEN-LAST:event_lblProductNameMouseClicked

    private void lblformNameMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblformNameMouseClicked
    {//GEN-HEADEREND:event_lblformNameMouseClicked
        // TODO add your handling code here:
        objUtility=new clsUtility();
    }//GEN-LAST:event_lblformNameMouseClicked

    private void lblPosNameMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblPosNameMouseClicked
    {//GEN-HEADEREND:event_lblPosNameMouseClicked
        // TODO add your handling code here:
        objUtility=new clsUtility();
    }//GEN-LAST:event_lblPosNameMouseClicked

    private void lblUserCodeMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblUserCodeMouseClicked
    {//GEN-HEADEREND:event_lblUserCodeMouseClicked
        // TODO add your handling code here:
        objUtility=new clsUtility();
    }//GEN-LAST:event_lblUserCodeMouseClicked

    private void lblDateMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblDateMouseClicked
    {//GEN-HEADEREND:event_lblDateMouseClicked
        // TODO add your handling code here:
        objUtility=new clsUtility();
    }//GEN-LAST:event_lblDateMouseClicked

    private void lblHOSignMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lblHOSignMouseClicked
    {//GEN-HEADEREND:event_lblHOSignMouseClicked
        // TODO add your handling code here:
        objUtility=new clsUtility();
    }//GEN-LAST:event_lblHOSignMouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(frmTableStatusReport.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(frmTableStatusReport.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(frmTableStatusReport.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(frmTableStatusReport.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new frmTableStatusReport().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAll;
    private javax.swing.JButton btnBilled;
    private javax.swing.JButton btnExit;
    private javax.swing.JButton btnOccupied;
    private javax.swing.JButton btnVacant;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.JLabel lblBilled;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblHOSign;
    private javax.swing.JLabel lblModuleName;
    private javax.swing.JLabel lblPosName;
    private javax.swing.JLabel lblProductName;
    private javax.swing.JLabel lblUserCode;
    private javax.swing.JLabel lblVacant;
    private javax.swing.JLabel lblformName;
    private javax.swing.JLabel lbloccupied;
    private javax.swing.JPanel pnlBackGround;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JPanel pnlheader;
    private javax.swing.JScrollPane pnltables;
    private javax.swing.JTextPane txtPanelTables;
    // End of variables declaration//GEN-END:variables
}
