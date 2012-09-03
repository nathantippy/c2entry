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
    private final Portfolio portfolio;

    public QuantityComputablePercent(Number percent, Portfolio portfolio) {
        this.percent = percent;
        this.portfolio = portfolio;
    }

    public Integer quantity(Number price, DataProvider dataProvider) {
        if (null==price || null==dataProvider) {
            return 0;
        }
        double dollars = portfolio.cash().add(portfolio.equity(dataProvider)).doubleValue()*percent.doubleValue();
        return (int)(dollars/price.doubleValue());
    }
}
