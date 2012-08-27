/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  7/30/12
 */
package com.collective2.signalEntry.adapter.dynamicSimulator.quantity;

import com.collective2.signalEntry.adapter.dynamicSimulator.DataProvider;
import com.collective2.signalEntry.adapter.dynamicSimulator.portfolio.Portfolio;
import com.collective2.signalEntry.adapter.dynamicSimulator.order.Order;

public class QuantityComputablePercent implements QuantityComputable {

    private final Number percent;

    public QuantityComputablePercent(Number percent) {
        this.percent = percent;
    }

    @Override
    public Integer quantity(Number price, Portfolio portfolio, DataProvider dataProvider, Order entryOrder) {

        double dollars = portfolio.cash().add(portfolio.equity(dataProvider)).doubleValue()*percent.doubleValue();
        return (int)(dollars/price.doubleValue());
    }
}
