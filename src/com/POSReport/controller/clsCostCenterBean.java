/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.POSReport.controller;

/**
 *
 * @author ajjim
 */
public class clsCostCenterBean
{
    private String strPOSCode;
    private String strPOSName;
    private String strCostCenterCode;
    private String strCostCenterName;    
    private double dblSubTotal;
    private double dblDiscAmount;
    private double dblSalesAmount;
    private String strItemCode;
    private String strItemName;    
    private double dblQuantity;

    public clsCostCenterBean()
    {
    }

    public String getStrPOSCode()
    {
        return strPOSCode;
    }

    public void setStrPOSCode(String strPOSCode)
    {
        this.strPOSCode = strPOSCode;
    }

    public String getStrPOSName()
    {
        return strPOSName;
    }

    public void setStrPOSName(String strPOSName)
    {
        this.strPOSName = strPOSName;
    }

    public String getStrCostCenterCode()
    {
        return strCostCenterCode;
    }

    public void setStrCostCenterCode(String strCostCenterCode)
    {
        this.strCostCenterCode = strCostCenterCode;
    }

    public String getStrCostCenterName()
    {
        return strCostCenterName;
    }

    public void setStrCostCenterName(String strCostCenterName)
    {
        this.strCostCenterName = strCostCenterName;
    }

    public double getDblSubTotal()
    {
        return dblSubTotal;
    }

    public void setDblSubTotal(double dblSubTotal)
    {
        this.dblSubTotal = dblSubTotal;
    }

    public double getDblDiscAmount()
    {
        return dblDiscAmount;
    }

    public void setDblDiscAmount(double dblDiscAmount)
    {
        this.dblDiscAmount = dblDiscAmount;
    }

    public double getDblSalesAmount()
    {
        return dblSalesAmount;
    }

    public void setDblSalesAmount(double dblSalesAmount)
    {
        this.dblSalesAmount = dblSalesAmount;
    }

    public String getStrItemCode()
    {
        return strItemCode;
    }

    public void setStrItemCode(String strItemCode)
    {
        this.strItemCode = strItemCode;
    }

    public String getStrItemName()
    {
        return strItemName;
    }

    public void setStrItemName(String strItemName)
    {
        this.strItemName = strItemName;
    }

    public double getDblQuantity()
    {
        return dblQuantity;
    }

    public void setDblQuantity(double dblQuantity)
    {
        this.dblQuantity = dblQuantity;
    }
    
    
}
