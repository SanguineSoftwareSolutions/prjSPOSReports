/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.POSReport.controller.comparator;

import com.POSGlobal.controller.clsGroupSubGroupWiseSales;
import com.POSReport.controller.clsGroupSubGroupItemBean;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author ajjim
 */
public class clsGroupSubGroupWiseSalesComparator implements Comparator<clsGroupSubGroupWiseSales>
{

    private List<Comparator<clsGroupSubGroupWiseSales>> listComparators;

    @SafeVarargs
    public clsGroupSubGroupWiseSalesComparator(Comparator<clsGroupSubGroupWiseSales>... comparators)
    {
        this.listComparators = Arrays.asList(comparators);
    }

    @Override
    public int compare(clsGroupSubGroupWiseSales o1, clsGroupSubGroupWiseSales o2)
    {
        for (Comparator<clsGroupSubGroupWiseSales> comparator : listComparators)
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
