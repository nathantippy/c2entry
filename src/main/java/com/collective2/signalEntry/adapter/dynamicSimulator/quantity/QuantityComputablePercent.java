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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public class QuantityComputablePercent implements QuantityComputable {

    private final Number percent;
    private final Portfolio portfolio;
    private static final Logger logger = LoggerFactory.getLogger(QuantityComputablePercent.class);

    public QuantityComputablePercent(Number percent, Portfolio portfolio) {
        this.percent = percent;
        this.portfolio = portfolio;
    }

    public Integer quantity(BigDecimal price, DataProvider dataProvider) {
        if (null==price || null==dataProvider) {
            logger.warn("Unable to compute quantity by percent, price:"+price+" "+dataProvider,new Exception());
            return 0;
        }
        double dollars = portfolio.cash().add(portfolio.equity(dataProvider)).doubleValue()*percent.doubleValue();
        return (int)(dollars/price.doubleValue());
    }
}
