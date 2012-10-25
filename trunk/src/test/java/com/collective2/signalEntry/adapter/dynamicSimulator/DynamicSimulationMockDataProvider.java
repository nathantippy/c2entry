/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  8/5/12
 */
package com.collective2.signalEntry.adapter.dynamicSimulator;

import java.math.BigDecimal;

public class DynamicSimulationMockDataProvider implements DataProvider {

    private final long openingTime;
    private final BigDecimal open;
    private final BigDecimal high;
    private final BigDecimal low;
    private final BigDecimal close;
    private final long endingTime;
    private final double noSplit = 1d;

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

    public DynamicSimulationMockDataProvider incTime(long step) {
        return new DynamicSimulationMockDataProvider(endingTime,open,high,low,close,endingTime+step);
    }

    public DynamicSimulationMockDataProvider incTime(long step, BigDecimal flatLine) {
        return new DynamicSimulationMockDataProvider(endingTime,flatLine,flatLine,flatLine,flatLine,endingTime+step);
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
    public BigDecimal highPrice(String symbol) {
        return high;
    }

    @Override
    public BigDecimal lowPrice(String symbol) {
        return low;
    }

    @Override
    public long startingTime() {
        return openingTime;
    }

    @Override
    public boolean isStartingTimeMarketOpen() {
        return true;
    }

    @Override
    public long endingTime() {
        return endingTime;
    }

    @Override
    public boolean isEndingTimeMarketClose() {
        return true;
    }

    @Override
    public Number splitAfterMarketClose(String symbol) {
        return noSplit;
    }

    @Override
    public boolean hasVolume(String symbol) {
        return true;
    }
}
