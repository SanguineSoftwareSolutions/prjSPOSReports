/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.POSReport.controller.comparator;

import com.POSGlobal.controller.clsBillDtl;
import com.POSGlobal.controller.clsItemWiseConsumption;
import com.POSReport.controller.clsBillItemDtlBean;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author ajjim
 */

public class clsDiscountComparator implements Comparator<clsBillItemDtlBean>
{

    private List<Comparator<clsBillItemDtlBean>> listComparators;

    @SafeVarargs
    public clsDiscountComparator(Comparator<clsBillItemDtlBean>... comparators)
    {
        this.listComparators = Arrays.asList(comparators);
    }

    @Override
    public int compare(clsBillItemDtlBean o1, clsBillItemDtlBean o2)
    {
        for (Comparator<clsBillItemDtlBean> comparator : listComparators)
        {
            int result = comparator.compare(o1, o2);
            if (result != 0)
            {
                return result;
            }
        }
        return 0;
    }
}