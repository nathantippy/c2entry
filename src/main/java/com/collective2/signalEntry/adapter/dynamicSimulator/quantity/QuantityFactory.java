package com.collective2.signalEntry.adapter.dynamicSimulator.quantity;

import com.collective2.signalEntry.C2ServiceException;
import com.collective2.signalEntry.Parameter;
import com.collective2.signalEntry.adapter.dynamicSimulator.SystemManager;
import com.collective2.signalEntry.implementation.Request;

/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  7/30/12
 */

public class QuantityFactory {

    public QuantityComputable computable(Request request, SystemManager systemManager) {
        //Dollars, Quantity, AccountPercent

        Integer quantity = (Integer)request.get(Parameter.Quantity);
        if (quantity != null) {
            return new QuantityComputableFixed(quantity);
        }
        Number dollars = (Number)request.get(Parameter.Dollars);
        if (dollars != null) {
            return new QuantityComputableDollars(dollars);
        }
        Number percent = (Number)request.get(Parameter.AccountPercent);
        if (percent != null) {
            return new QuantityComputablePercent(percent);
        }

        //part of all-in-one signal where we need to close out the same sized position we opened
        Integer condUpon = (Integer)request.get(Parameter.ConditionalUpon);
        if (condUpon != null) {
            return new QuantityComputableEntry(systemManager.lookupOrder(condUpon));
        }

        throw new C2ServiceException("Unable to determine how to calculate quantity.",false);

    }


}
