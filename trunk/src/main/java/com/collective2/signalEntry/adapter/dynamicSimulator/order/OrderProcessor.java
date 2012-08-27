/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  8/26/12
 */

package com.collective2.signalEntry.adapter.dynamicSimulator.order;

import com.collective2.signalEntry.adapter.dynamicSimulator.DataProvider;
import com.collective2.signalEntry.adapter.dynamicSimulator.portfolio.Portfolio;

import java.math.BigDecimal;

public interface OrderProcessor {

    boolean process(DataProvider dataProvider, Portfolio portfolio, BigDecimal commission, OrderSignal order);

}
