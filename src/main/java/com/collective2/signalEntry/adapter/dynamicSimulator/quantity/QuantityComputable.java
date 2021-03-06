package com.collective2.signalEntry.adapter.dynamicSimulator.quantity;

import com.collective2.signalEntry.adapter.dynamicSimulator.DataProvider;

import java.math.BigDecimal;

/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  7/30/12
 */

public interface QuantityComputable {

    Integer quantity(BigDecimal price, DataProvider dataProvider);

}
