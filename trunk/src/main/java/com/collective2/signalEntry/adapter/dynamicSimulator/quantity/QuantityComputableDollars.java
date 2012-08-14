package com.collective2.signalEntry.adapter.dynamicSimulator.quantity;

import com.collective2.signalEntry.adapter.dynamicSimulator.DataProvider;
import com.collective2.signalEntry.adapter.dynamicSimulator.Portfolio;

/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  7/30/12
 */

public class QuantityComputableDollars implements QuantityComputable {

    private final Number dollars;

    public QuantityComputableDollars(Number dollars) {
        this.dollars = dollars;
    }

    @Override
    public Integer quantity(Number price, Portfolio portfolio, DataProvider dataProvider) {
        return (int)(dollars.doubleValue()/price.doubleValue());
    }
}
