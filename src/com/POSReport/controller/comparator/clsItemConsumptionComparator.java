/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.POSReport.controller.comparator;

import com.POSGlobal.controller.clsBillDtl;
import com.POSGlobal.controller.clsItemWiseConsumption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author ajjim
 */

public class clsItemConsumptionComparator implements Comparator<clsItemWiseConsumption>
{

    private List<Comparator<clsItemWiseConsumption>> listComparators;

    @SafeVarargs
    public clsItemConsumptionComparator(Comparator<clsItemWiseConsumption>... comparators)
    {
        this.listComparators = Arrays.asList(comparators);
    }

    @Override
    public int compare(clsItemWiseConsumption o1, clsItemWiseConsumption o2)
    {
        for (Comparator<clsItemWiseConsumption> comparator : listComparators)
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