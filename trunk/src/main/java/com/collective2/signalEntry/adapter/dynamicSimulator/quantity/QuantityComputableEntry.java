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

    private final Order condUponOrder;

    public QuantityComputableEntry(Order condUponOrder) {
        assert(condUponOrder!=null);
        this.condUponOrder = condUponOrder;
    }

    @Override
    public Integer quantity(Number price, DataProvider dataProvider) {
        return condUponOrder.entryQuantity();
    }
}
