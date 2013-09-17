package com.collective2.signalEntry.adapter.dynamicSimulator.order;

import com.collective2.signalEntry.Duration;
import com.collective2.signalEntry.Instrument;
import com.collective2.signalEntry.adapter.dynamicSimulator.DataProvider;
import com.collective2.signalEntry.adapter.dynamicSimulator.DynamicSimulationMockDataProvider;
import com.collective2.signalEntry.adapter.dynamicSimulator.portfolio.Portfolio;
import com.collective2.signalEntry.adapter.dynamicSimulator.portfolio.SimplePortfolio;
import com.collective2.signalEntry.adapter.dynamicSimulator.portfolio.SimplePortfolioFactory;
import com.collective2.signalEntry.adapter.dynamicSimulator.quantity.QuantityComputable;
import com.collective2.signalEntry.adapter.dynamicSimulator.quantity.QuantityComputableEntry;
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

public class SimpleOrderTest {

    private final long start = 0;
    private final long stop = 1000;
    private final BigDecimal open = new BigDecimal("3");
    private final BigDecimal high = new BigDecimal("7");
    private final BigDecimal low  = new BigDecimal("2");
    private final BigDecimal close = new BigDecimal("6");

    private final DataProvider dataProvider = new DynamicSimulationMockDataProvider(start,open,high,low,close,stop);
    private final BigDecimal commission = new BigDecimal("9");


    @Test
    public void  conditionalOrderTest() {

        Portfolio portfolio = new SimplePortfolioFactory().createPortfolio(new BigDecimal("1000.00"));

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
        Order buyOrder = new Order(null, id,instrument,symbol,action,quantityComputable,cancelAtMs,timeInForce,processor, null);

        //final quantity is not known until this order is processed
        //this however has the quantity because QuantityComputableFixed is used above
        assertEquals(Integer.valueOf(10), Integer.valueOf(buyOrder.quantity()));

        action = SignalAction.STC;
        quantityComputable = new QuantityComputableEntry(buyOrder);
        Order sellOrder = new Order(null, id,instrument,symbol,action,quantityComputable,cancelAtMs,timeInForce,processor, buyOrder);

        //final quantity is not known until this order is processed
        //this however has the quantity because QuantityComputableFixed is used above
        assertEquals(Integer.valueOf(10), Integer.valueOf(sellOrder.quantity()));

        assertTrue(buyOrder.process(dataProvider,portfolio,commission,null));

        assertEquals(quantity, Integer.valueOf(buyOrder.quantity()));
        assertEquals(quantity, Integer.valueOf(sellOrder.quantity()));

        assertTrue(sellOrder.process(dataProvider, portfolio, commission,null));

        assertEquals(new BigDecimal("982.00"),portfolio.cash());
        assertEquals(Integer.valueOf(0),portfolio.position("GG").quantity());
        assertEquals(new BigDecimal("0"),portfolio.equity(dataProvider));

    }


}
