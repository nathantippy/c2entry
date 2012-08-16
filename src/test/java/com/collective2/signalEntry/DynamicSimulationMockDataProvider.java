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

    long openingTime;
    BigDecimal open;
    BigDecimal high;
    BigDecimal low;
    BigDecimal close;
    long endingTime;

    //only used when testing single symbols so that field is never checked
    public DynamicSimulationMockDataProvider(long openingTime, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, long endingTime) {
        this.openingTime = openingTime;
        assert(high.compareTo(low)>=0) : "high must be greater or equal to low";
        assert(high.compareTo(open)>=0) : "high must be greater or equal to open";
        assert(high.compareTo(close)>=0) : "high must be greater or equal to close";
        assert(low.compareTo(open)<=0) : "low must be less or equal to open";
        assert(low.compareTo(close)<=0) : "low must be less or equal to close";

        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.endingTime = endingTime;
    }

    public void incTime(long step) {
        openingTime = endingTime;
        endingTime += step;
    }

    public void incTime(long step, BigDecimal flatLine) {
        openingTime = endingTime;
        endingTime += step;
        open = flatLine;
        close = flatLine;
        high = flatLine;
        low = flatLine;
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
