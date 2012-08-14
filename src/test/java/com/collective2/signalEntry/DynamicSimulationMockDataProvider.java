/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  8/5/12
 */
package com.collective2.signalEntry;

import com.collective2.signalEntry.adapter.dynamicSimulator.DataProvider;

import java.math.BigDecimal;

public class DynamicSimulationMockDataProvider implements DataProvider {

    final long openingTime;
    final BigDecimal open;
    final BigDecimal high;
    final BigDecimal low;                  //TODO beginning ending   opening closing
    final BigDecimal close;
    final long endingTime;

    //only used when testing single symbols so that field is never checked
    public DynamicSimulationMockDataProvider(long openingTime, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, long endingTime) {
        this.openingTime = openingTime;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.endingTime = endingTime;
    }

    @Override
    public BigDecimal openingPrice(String symbol) {
        return open;
    }

    @Override
    public BigDecimal endingPrice(String symbol) {
        return close;
    }

    @Override
    public boolean wasOpen() {
        return true;
    }

    @Override
    public BigDecimal highPrice(String symbol) {
        return high;
    }

    @Override
    public BigDecimal lowPrice(String symbol) {
        return low;
    }

    @Override
    public long openingTime() {
        return openingTime;
    }

    @Override
    public long endingTime() {
        return endingTime;
    }
}
