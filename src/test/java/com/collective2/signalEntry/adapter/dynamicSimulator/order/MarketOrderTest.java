package com.collective2.signalEntry.adapter.dynamicSimulator.order;

import com.collective2.signalEntry.Duration;
import com.collective2.signalEntry.Instrument;
import com.collective2.signalEntry.adapter.dynamicSimulator.DataProvider;
import com.collective2.signalEntry.adapter.dynamicSimulator.DynamicSimulationMockDataProvider;
import com.collective2.signalEntry.adapter.dynamicSimulator.portfolio.Portfolio;
import com.collective2.signalEntry.adapter.dynamicSimulator.portfolio.SimplePortfolio;
import com.collective2.signalEntry.adapter.dynamicSimulator.quantity.QuantityComputable;
import com.collective2.signalEntry.adapter.dynamicSimulator.quantity.QuantityComputableFixed;
import com.collective2.signalEntry.implementation.SignalAction;
import org.junit.Test;

import java.math.BigDecimal;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  8/29/12
 */

public class MarketOrderTest {

    private final long start = 0;
    private final long stop = 1000;
    private final BigDecimal open = new BigDecimal("3");
    private final BigDecimal high = new BigDecimal("7");
    private final BigDecimal low  = new BigDecimal("2");
    private final BigDecimal close = new BigDecimal("6");

    private final DataProvider dataProvider = new DynamicSimulationMockDataProvider(start,open,high,low,close,stop);
    private final BigDecimal commission = new BigDecimal("9");


    @Test
    public void  marketBTOTest() {

        Portfolio portfolio = new SimplePortfolio(new BigDecimal("1000.00"));

        int id = 42;
        long time = stop;
        Instrument instrument = Instrument.Forex;
        String symbol = "GG";
        SignalAction action = SignalAction.BTO;
        Integer quantity = 10;
        QuantityComputable quantityComputable = new QuantityComputableFixed(quantity);
        long cancelAtMs = Long.MAX_VALUE;
        Duration timeInForce = Duration.GoodTilCancel;
        OrderProcessorMarket processor = new OrderProcessorMarket(time, symbol);
        Order order = new Order(id,instrument,symbol,action,quantityComputable,cancelAtMs,timeInForce,processor, null);

        //test only the processor and do it outside the order
        boolean processed = processor.process(dataProvider, portfolio, commission, order, action, quantityComputable, null);

        assertTrue(processed);
        assertEquals(new BigDecimal("961.00"),portfolio.cash());
        assertEquals(quantity,portfolio.position("GG").quantity());
        assertEquals(new BigDecimal("60"),portfolio.equity(dataProvider));

        //sell to close this open position.

        action = SignalAction.STC;
        order = new Order(id,instrument,symbol,action,quantityComputable,cancelAtMs,timeInForce,processor, null);

        //test only the processor and do it outside the order
        processed = processor.process(dataProvider, portfolio, commission, order, action, quantityComputable, null);

        assertTrue(processed);
        assertEquals(new BigDecimal("982.00"),portfolio.cash());
        assertEquals(Integer.valueOf(0),portfolio.position("GG").quantity());
        assertEquals(new BigDecimal("0"),portfolio.equity(dataProvider));

    }

    @Test
    public void  marketSTOTest() {
        marketShortTest(SignalAction.STO);
    }

    @Test
    public void  marketSShortTest() {
        marketShortTest(SignalAction.SSHORT);
    }


    private void  marketShortTest(SignalAction sellAction) {

        Portfolio portfolio = new SimplePortfolio(new BigDecimal("1000.00"));

        int id = 42;
        long time = stop;
        Instrument instrument = Instrument.Forex;
        String symbol = "GG";
        Integer quantity = 10;
        QuantityComputable quantityComputable = new QuantityComputableFixed(quantity);
        long cancelAtMs = Long.MAX_VALUE;
        Duration timeInForce = Duration.GoodTilCancel;
        OrderProcessorMarket processor = new OrderProcessorMarket(time, symbol);
        Order order = new Order(id,instrument,symbol,sellAction,quantityComputable,cancelAtMs,timeInForce,processor, null);

        //test only the processor and do it outside the order
        boolean processed = processor.process(dataProvider, portfolio, commission, order, sellAction, quantityComputable, null);

        assertTrue(processed);
        assertEquals(new BigDecimal("1021.00"),portfolio.cash());
        assertEquals(Integer.valueOf(-quantity),portfolio.position("GG").quantity());
        assertEquals(new BigDecimal("-60"),portfolio.equity(dataProvider));

        //Buy to cover this short position

        SignalAction action = SignalAction.BTC;
        order = new Order(id,instrument,symbol,action,quantityComputable,cancelAtMs,timeInForce,processor, null);

        processed = processor.process(dataProvider, portfolio, commission, order, action, quantityComputable, null);

        assertTrue(processed);
        assertEquals(new BigDecimal("982.00"),portfolio.cash());
        assertEquals(Integer.valueOf(0),portfolio.position("GG").quantity());
        assertEquals(new BigDecimal("0"),portfolio.equity(dataProvider));

    }

}
