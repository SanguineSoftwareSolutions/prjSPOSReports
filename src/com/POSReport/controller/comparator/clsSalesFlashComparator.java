/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.POSReport.controller.comparator;

import com.POSGlobal.controller.clsBillDtl;
import com.POSGlobal.controller.clsSalesFlashColumns;
import com.POSReport.controller.clsBillItemDtlBean;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author ajjim
 */

public class clsSalesFlashComparator implements Comparator<clsSalesFlashColumns>
{

    private List<Comparator<clsSalesFlashColumns>> listComparators;

    @SafeVarargs
    public clsSalesFlashComparator(Comparator<clsSalesFlashColumns>... comparators)
    {
        this.listComparators = Arrays.asList(comparators);
    }

    @Override
    public int compare(clsSalesFlashColumns o1, clsSalesFlashColumns o2)
    {
        for (Comparator<clsSalesFlashColumns> comparator : listComparators)
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