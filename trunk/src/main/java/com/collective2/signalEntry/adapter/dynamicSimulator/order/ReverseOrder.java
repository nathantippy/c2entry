package com.collective2.signalEntry.adapter.dynamicSimulator.order;

import com.collective2.signalEntry.Duration;
import com.collective2.signalEntry.adapter.dynamicSimulator.DataProvider;
import com.collective2.signalEntry.adapter.dynamicSimulator.portfolio.Portfolio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  7/29/12
 */

public class ReverseOrder extends Order {

    private static final Logger logger = LoggerFactory.getLogger(ReverseOrder.class);

    private BigDecimal triggerPrice;
    private Integer quantity;

    public ReverseOrder(int id, long time, String symbol, Duration timeInForce) {
        super(id, time, symbol, Long.MAX_VALUE, timeInForce, null); //TODO: must be implemented as 2 actions
    }

    @Override
    public boolean  process(DataProvider dataProvider, Portfolio portfolio, BigDecimal commission) {
        logger.trace("process ReverseOrder");

        throw new UnsupportedOperationException();
    }


    public void triggerPrice(BigDecimal price) {
        this.triggerPrice = price;
    }

    public void quantity(Integer quantity) {
        this.quantity = quantity;
    }
}
