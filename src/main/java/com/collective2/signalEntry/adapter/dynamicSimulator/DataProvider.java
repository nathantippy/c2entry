/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  7/28/12
 */
package com.collective2.signalEntry.adapter.dynamicSimulator;


import java.math.BigDecimal;

/**
 * DataProvider for running back tests.  This data
 * must be scrubbed and perfect. If there are any
 * odd numbers like zeros it will cause the simulation
 * to act just as though the price of the security dropped
 * to zero and this is probably not what was desired.
 */
public interface DataProvider {

    BigDecimal openingPrice(String symbol);
    BigDecimal endingPrice(String symbol);

    BigDecimal highPrice(String symbol);
    BigDecimal lowPrice(String symbol);

    long startingTime();
    boolean isStartingTimeMarketOpen();

    long endingTime();
    boolean isEndingTimeMarketClose();
}
