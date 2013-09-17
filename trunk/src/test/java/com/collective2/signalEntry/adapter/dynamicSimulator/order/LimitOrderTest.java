package com.collective2.signalEntry.adapter.dynamicSimulator.order;

import com.collective2.signalEntry.Duration;
import com.collective2.signalEntry.Instrument;
import com.collective2.signalEntry.adapter.dynamicSimulator.DataProvider;
import com.collective2.signalEntry.adapter.dynamicSimulator.DynamicSimulationMockDataProvider;
import com.collective2.signalEntry.adapter.dynamicSimulator.portfolio.Portfolio;
import com.collective2.signalEntry.adapter.dynamicSimulator.portfolio.SimplePortfolio;
import com.collective2.signalEntry.adapter.dynamicSimulator.portfolio.SimplePortfolioFactory;
import com.collective2.signalEntry.adapter.dynamicSimulator.quantity.QuantityComputable;
import com.collective2.signalEntry.adapter.dynamicSimulator.quantity.QuantityComputableFixed;
import com.collective2.signalEntry.implementation.RelativeNumber;
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

public class LimitOrderTest {

    private final long start = 0;
    private final long stop = 1000;
    private final BigDecimal open = new BigDecimal("3");
    private final BigDecimal high = new BigDecimal("7");
    private final BigDecimal low  = new BigDecimal("1");
    private final BigDecimal close = new BigDecimal("6");

    private final DataProvider dataProvider = new DynamicSimulationMockDataProvider(start,open,high,low,close,stop);
    private final BigDecimal commission = new BigDecimal("9");

    @Test
    public void  limitBTOTestOpenOpen() {
        RelativeNumber buyBelow = new RelativeNumber("4");//must buy below this price
        RelativeNumber sellAbove = new RelativeNumber("3");//must sell above this price

        BigDecimal expectedBuy = new BigDecimal("3");
        BigDecimal expectedSell = new BigDecimal("3");

        limitBTOTest(buyBelow, sellAbove, expectedBuy, expectedSell);
    }

    @Test
    public void  limitBTOTestLimitLimit() {
        RelativeNumber buyBelow = new RelativeNumber("2");//must buy below this price
        RelativeNumber sellAbove = new RelativeNumber("4");//must sell above this price

        BigDecimal expectedBuy = new BigDecimal("2");
        BigDecimal expectedSell = new BigDecimal("4");

        limitBTOTest(buyBelow, sellAbove, expectedBuy, expectedSell);
    }

    @Test
    public void  limitBTOTestTopTop() {
        RelativeNumber buyBelow = new RelativeNumber("1");//must buy below this price
        RelativeNumber sellAbove = new RelativeNumber("7");//must sell above this price

        BigDecimal expectedBuy = new BigDecimal("1");
        BigDecimal expectedSell = new BigDecimal("7");

        limitBTOTest(buyBelow, sellAbove, expectedBuy, expectedSell);
    }

    private void limitBTOTest(RelativeNumber buyBelow, RelativeNumber sellAbove, BigDecimal expectedBuy, BigDecimal expectedSell) {

        BigDecimal startingCash = new BigDecimal("1000.00");
        Portfolio portfolio = new SimplePortfolioFactory().createPortfolio(startingCash);

        int id = 42;
        long time = stop;
        Instrument instrument = Instrument.Forex;
        String symbol = "GG";
        SignalAction action = SignalAction.BTO;
        Integer quantity = 10;
        QuantityComputable quantityComputable = new QuantityComputableFixed(quantity);
        long cancelAtMs = Long.MAX_VALUE;
        Duration timeInForce = Duration.GoodTilCancel;


        OrderProcessorLimit processor = new OrderProcessorLimit(time, symbol, buyBelow);
        Order order = new Order(null,id,instrument,symbol,action,quantityComputable,cancelAtMs,timeInForce,processor,null);

        //test only the processor and do it outside the order
        boolean processed = processor.process(dataProvider, portfolio, commission, order, action, quantityComputable,null);

        assertTrue(processed);
        BigDecimal expectedCash = startingCash.subtract(expectedBuy.multiply(new BigDecimal(quantity)).add(commission));

        assertEquals(expectedBuy, processor.transactionPrice());
        assertEquals(expectedCash, portfolio.cash());
        assertEquals(quantity,portfolio.position("GG").quantity());
        assertEquals(new BigDecimal("60"),portfolio.equity(dataProvider));

        //sell to close this open position.

        action = SignalAction.STC;

        OrderProcessorLimit sellProcessor = new OrderProcessorLimit(time, symbol, sellAbove);
        order = new Order(null, id,instrument,symbol,action,quantityComputable,cancelAtMs,timeInForce,sellProcessor,null);

        //test only the processor and do it outside the order
        processed = sellProcessor.process(dataProvider, portfolio, commission, order, action, quantityComputable,null);

        assertTrue(processed);
        expectedCash = expectedCash.add(expectedSell.multiply(new BigDecimal(quantity))).subtract(commission);

        assertEquals(expectedSell, sellProcessor.transactionPrice());
        assertEquals(expectedCash,portfolio.cash());
        assertEquals(Integer.valueOf(0),portfolio.position("GG").quantity());
        assertEquals(new BigDecimal("0"),portfolio.equity(dataProvider));

    }

    @Test
    public void  limitSTOTest() {

        RelativeNumber sellAbove = new RelativeNumber("3");//must sell above this price
        RelativeNumber buyBelow = new RelativeNumber("4");//must buy below this price

        BigDecimal expectedSell = new BigDecimal("3");
        BigDecimal expectedBuy = new BigDecimal("3");

        limitShortTest(SignalAction.STO, sellAbove, buyBelow, expectedSell, expectedBuy);
    }

    @Test
    public void  limitSShortTest() {

        RelativeNumber sellAbove = new RelativeNumber("4");//must sell above this price
        RelativeNumber buyBelow = new RelativeNumber("2");//must buy below this price

        BigDecimal expectedSell = new BigDecimal("4");
        BigDecimal expectedBuy = new BigDecimal("2");

        limitShortTest(SignalAction.SSHORT, sellAbove, buyBelow, expectedSell, expectedBuy);
    }


    private void  limitShortTest(SignalAction sellAction, RelativeNumber sellAbove, RelativeNumber buyBelow, BigDecimal expectedSell, BigDecimal expectedBuy) {

        BigDecimal startingCash = new BigDecimal("1000.00");
        Portfolio portfolio = new SimplePortfolioFactory().createPortfolio(startingCash);

        int id = 42;
        long time = stop;
        Instrument instrument = Instrument.Forex;
        String symbol = "GG";
        Integer quantity = 10;
        QuantityComputable quantityComputable = new QuantityComputableFixed(quantity);
        long cancelAtMs = Long.MAX_VALUE;
        Duration timeInForce = Duration.GoodTilCancel;

        OrderProcessorLimit processor = new OrderProcessorLimit(time,symbol,sellAbove);
        Order order = new Order(null, id,instrument,symbol,sellAction,quantityComputable,cancelAtMs,timeInForce,processor,null);

        //test only the processor and do it outside the order
        boolean processed = processor.process(dataProvider, portfolio, commission, order, sellAction, quantityComputable,null);

        BigDecimal expectedCash = startingCash.add(expectedSell.multiply(new BigDecimal(quantity)).subtract(commission));

        assertTrue(processed);
        assertEquals("sell ", expectedSell, processor.transactionPrice());
        assertEquals(expectedCash,portfolio.cash());
        assertEquals(Integer.valueOf(-quantity),portfolio.position("GG").quantity());
        assertEquals(new BigDecimal("-60"),portfolio.equity(dataProvider));

        //Buy to cover this short position

        SignalAction action = SignalAction.BTC;
        OrderProcessorLimit buyProcessor = new OrderProcessorLimit(time,symbol,buyBelow);
        order = new Order(null, id,instrument,symbol,action,quantityComputable,cancelAtMs,timeInForce,buyProcessor,null);

        processed = buyProcessor.process(dataProvider, portfolio, commission, order, action, quantityComputable,null);
        expectedCash = expectedCash.subtract(expectedBuy.multiply(new BigDecimal(quantity))).subtract(commission);

        assertTrue(processed);
        assertEquals("buy ",expectedBuy, buyProcessor.transactionPrice());
        assertEquals(expectedCash,portfolio.cash());
        assertEquals(Integer.valueOf(0),portfolio.position("GG").quantity());
        assertEquals(new BigDecimal("0"),portfolio.equity(dataProvider));

    }

}
