/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  7/28/12
 */
package com.collective2.signalEntry.adapter.dynamicSimulator;


import java.math.BigDecimal;

public interface DataProvider {

    BigDecimal openingPrice(String symbol);
    BigDecimal endingPrice(String symbol);

    BigDecimal highPrice(String symbol);
    BigDecimal lowPrice(String symbol);

    long openingTime();
    long endingTime();
}
