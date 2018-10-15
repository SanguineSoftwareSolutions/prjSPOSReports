/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.POSReport.controller.comparator;

import com.POSGlobal.controller.clsBillDtl;
import com.POSGlobal.controller.clsItemWiseConsumption;
import com.POSReport.controller.clsItemConsumptionMonthWiseBean;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author ajjim
 */

public class clsItemConsumptionMonthWiseComparator implements Comparator<clsItemConsumptionMonthWiseBean>
{

    private List<Comparator<clsItemConsumptionMonthWiseBean>> listComparators;

    @SafeVarargs
    public clsItemConsumptionMonthWiseComparator(Comparator<clsItemConsumptionMonthWiseBean>... comparators)
    {
        this.listComparators = Arrays.asList(comparators);
    }

    @Override
    public int compare(clsItemConsumptionMonthWiseBean o1, clsItemConsumptionMonthWiseBean o2)
    {
        for (Comparator<clsItemConsumptionMonthWiseBean> comparator : listComparators)
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