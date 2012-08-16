package com.collective2.signalEntry.adapter.dynamicSimulator.order;

import com.collective2.signalEntry.Duration;
import com.collective2.signalEntry.adapter.dynamicSimulator.DataProvider;
import com.collective2.signalEntry.adapter.dynamicSimulator.Portfolio;

import java.math.BigDecimal;

/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  7/29/12
 */

public class ReverseOrder extends Order {

    BigDecimal triggerPrice;
    Duration duration;
    Integer quantity;

    public ReverseOrder(int id, long time, String symbol) {
        super(id, time, symbol);
    }

    @Override
    public boolean process(DataProvider dataProvider, Portfolio portfolio, BigDecimal commission) {

        if (!isConditionProcessed()) {
            return false;
        }

        throw new UnsupportedOperationException();
    }


    public void triggerPrice(BigDecimal price) {
        this.triggerPrice = price;
    }

    public void duration(Duration duration) {
        this.duration = duration;
    }

    public void quantity(Integer quantity) {
        this.quantity = quantity;
    }
}
