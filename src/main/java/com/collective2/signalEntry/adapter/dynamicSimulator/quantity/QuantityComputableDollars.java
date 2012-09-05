package com.collective2.signalEntry.adapter.dynamicSimulator.quantity;

import com.collective2.signalEntry.adapter.dynamicSimulator.DataProvider;
import com.collective2.signalEntry.adapter.dynamicSimulator.portfolio.Portfolio;
import com.collective2.signalEntry.adapter.dynamicSimulator.order.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  7/30/12
 */

public class QuantityComputableDollars implements QuantityComputable {

    private final Number dollars;
    private static final Logger logger = LoggerFactory.getLogger(QuantityComputableDollars.class);

    public QuantityComputableDollars(Number dollars) {
        this.dollars = dollars;
    }

    @Override
    public Integer quantity(BigDecimal price, DataProvider dataProvider) {
        if (null == price || price.compareTo(BigDecimal.ZERO)<=0) {
            logger.warn("Unable to compute quantity by dollars, price:"+price);
            return 0;
        }
        return  (int)(dollars.doubleValue()/price.doubleValue());
    }
}
