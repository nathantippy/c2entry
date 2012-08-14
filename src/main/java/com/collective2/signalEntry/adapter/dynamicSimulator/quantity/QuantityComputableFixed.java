/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  7/30/12
 */
package com.collective2.signalEntry.adapter.dynamicSimulator.quantity;

import com.collective2.signalEntry.adapter.dynamicSimulator.DataProvider;
import com.collective2.signalEntry.adapter.dynamicSimulator.Portfolio;


public class QuantityComputableFixed implements QuantityComputable {

    private final Integer quantity;

    public QuantityComputableFixed(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public Integer quantity(Number price, Portfolio portfolio, DataProvider dataProvider) {
        return quantity;
    }
}
