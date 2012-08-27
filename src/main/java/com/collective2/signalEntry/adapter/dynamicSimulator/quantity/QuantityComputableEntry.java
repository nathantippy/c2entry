package com.collective2.signalEntry.adapter.dynamicSimulator.quantity;

import com.collective2.signalEntry.adapter.dynamicSimulator.DataProvider;
import com.collective2.signalEntry.adapter.dynamicSimulator.portfolio.Portfolio;
import com.collective2.signalEntry.adapter.dynamicSimulator.order.Order;

/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  8/15/12
 */

public class QuantityComputableEntry implements QuantityComputable {

    private final Integer condUpon;

    public QuantityComputableEntry(Integer condUpon) {
        assert(condUpon!=null);
        this.condUpon = condUpon;
    }

    @Override
    public Integer quantity(Number price, Portfolio portfolio, DataProvider dataProvider, Order entryOrder) {

        assert(entryOrder.id() == condUpon.intValue()) : "Was conditional upon "+condUpon+" but found "+entryOrder.id();

        return entryOrder.entryQuantity();
    }
}
