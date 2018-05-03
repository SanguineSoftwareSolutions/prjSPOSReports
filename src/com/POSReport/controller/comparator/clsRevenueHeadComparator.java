/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.POSReport.controller.comparator;

import com.POSReport.controller.clsRevenueBean;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author ajjim
 */
public class clsRevenueHeadComparator implements Comparator<clsRevenueBean>
{

    private List<Comparator<clsRevenueBean>> listComparators;

    @SafeVarargs
    public clsRevenueHeadComparator(Comparator<clsRevenueBean>... comparators)
    {
        this.listComparators = Arrays.asList(comparators);
    }

    @Override
    public int compare(clsRevenueBean o1, clsRevenueBean o2)
    {
        for (Comparator<clsRevenueBean> comparator : listComparators)
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