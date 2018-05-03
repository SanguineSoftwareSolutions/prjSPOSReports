

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.POSReport.controller.comparator;

import com.POSReport.controller.clsCostCenterBean;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author ajjim
 */
public class clsCostCenterComparator implements Comparator<clsCostCenterBean>
{

    private List<Comparator<clsCostCenterBean>> listComparators;

    @SafeVarargs
    public clsCostCenterComparator(Comparator<clsCostCenterBean>... comparators)
    {
        this.listComparators = Arrays.asList(comparators);
    }

    @Override
    public int compare(clsCostCenterBean o1, clsCostCenterBean o2)
    {
        for (Comparator<clsCostCenterBean> comparator : listComparators)
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