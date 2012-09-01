package com.collective2.signalEntry.adapter.dynamicSimulator.order;

import com.collective2.signalEntry.Duration;
import com.collective2.signalEntry.adapter.dynamicSimulator.DynamicSimulationMockDataProvider;
import com.collective2.signalEntry.Instrument;
import com.collective2.signalEntry.adapter.dynamicSimulator.DataProvider;
import com.collective2.signalEntry.adapter.dynamicSimulator.portfolio.Portfolio;
import com.collective2.signalEntry.adapter.dynamicSimulator.portfolio.SimplePortfolio;
import com.collective2.signalEntry.adapter.dynamicSimulator.quantity.QuantityComputable;
import com.collective2.signalEntry.adapter.dynamicSimulator.quantity.QuantityComputableFixed;
import com.collective2.signalEntry.implementation.Action;
import com.collective2.signalEntry.implementation.RelativeNumber;
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

public class StopOrderTest {

    private final long start = 0;
    private final long stop = 1000;
    private final BigDecimal open = new BigDecimal("3");
    private final BigDecimal high = new BigDecimal("7");
    private final BigDecimal low  = new BigDecimal("1");
    private final BigDecimal close = new BigDecimal("6");

    private final DataProvider dataProvider = new DynamicSimulationMockDataProvider(start,open,high,low,close,stop);
    private final BigDecimal commission = new BigDecimal("9");

    @Test
    public void  stopBTOTestOpenOpen() {
        RelativeNumber buyBelow = new RelativeNumber("4");//must buy ABOVE this price
        RelativeNumber sellAbove = new RelativeNumber("3");//must sell BELOW this price

        BigDecimal expectedBuy = new BigDecimal("4");
        BigDecimal expectedSell = new BigDecimal("3");

        stopBTOTest(buyBelow, sellAbove, expectedBuy, expectedSell);
    }

    @Test
    public void  stopBTOTestLimitLimit() {
        RelativeNumber buyBelow = new RelativeNumber("2");//must buy ABOVE this price
        RelativeNumber sellAbove = new RelativeNumber("2");//must sell BELOW this price

        BigDecimal expectedBuy = new BigDecimal("2");
        BigDecimal expectedSell = new BigDecimal("2");

        stopBTOTest(buyBelow, sellAbove, expectedBuy, expectedSell);
    }

    @Test
    public void  stopBTOTestTopTop() {
        RelativeNumber buyBelow = new RelativeNumber("1");//must buy ABOVE this price
        RelativeNumber sellAbove = new RelativeNumber("7");//must sell BELOW this price

        BigDecimal expectedBuy = new BigDecimal("1");
        BigDecimal expectedSell = new BigDecimal("3");

        stopBTOTest(buyBelow, sellAbove, expectedBuy, expectedSell);
    }

    private void stopBTOTest(RelativeNumber buyBelow, RelativeNumber sellAbove, BigDecimal expectedBuy, BigDecimal expectedSell) {

        BigDecimal startingCash = new BigDecimal("1000.00");
        Portfolio portfolio = new SimplePortfolio(startingCash);

        int id = 42;
        long time = stop;
        Instrument instrument = Instrument.Forex;
        String symbol = "GG";
        Action action = Action.BTO;
        Integer quantity = 10;
        QuantityComputable quantityComputable = new QuantityComputableFixed(quantity);
        long cancelAtMs = Long.MAX_VALUE;
        Duration timeInForce = Duration.GoodTilCancel;


        OrderProcessorStop processor = new OrderProcessorStop(time, symbol, buyBelow);
        Order order = new Order(id,instrument,symbol,action,quantityComputable,cancelAtMs,timeInForce,processor);

        //test only the processor and do it outside the order
        boolean processed = processor.process(dataProvider, portfolio, commission, order, action, quantityComputable);

        assertTrue(processed);
        BigDecimal expectedCash = startingCash.subtract(expectedBuy.multiply(new BigDecimal(quantity)).add(commission));

        assertEquals("buy ",expectedBuy, processor.transactionPrice());
        assertEquals(expectedCash, portfolio.cash());
        assertEquals(quantity,portfolio.position("GG").quantity());
        assertEquals(new BigDecimal("60"),portfolio.equity(dataProvider));

        //sell to close this open position.

        action = Action.STC;

        OrderProcessorStop sellProcessor = new OrderProcessorStop(time, symbol, sellAbove);
        order = new Order(id,instrument,symbol,action,quantityComputable,cancelAtMs,timeInForce,sellProcessor);

        //test only the processor and do it outside the order
        processed = sellProcessor.process(dataProvider, portfolio, commission, order, action, quantityComputable);

        assertTrue(processed);
        expectedCash = expectedCash.add(expectedSell.multiply(new BigDecimal(quantity))).subtract(commission);

        assertEquals("sell ",expectedSell, sellProcessor.transactionPrice());
        assertEquals(expectedCash,portfolio.cash());
        assertEquals(Integer.valueOf(0),portfolio.position("GG").quantity());
        assertEquals(new BigDecimal("0"),portfolio.equity(dataProvider));

    }

    @Test
    public void  stopSTOTest() {

        RelativeNumber sellAbove = new RelativeNumber("3");//must sell BELOW this price
        RelativeNumber buyBelow = new RelativeNumber("4");//must buy ABOVE this price

        BigDecimal expectedSell = new BigDecimal("3");
        BigDecimal expectedBuy = new BigDecimal("4");

        stopShortTest(Action.STO, sellAbove, buyBelow, expectedSell, expectedBuy);
    }

    @Test
    public void  stopSShortTest() {

        RelativeNumber sellAbove = new RelativeNumber("2");//must sell BELOW this price
        RelativeNumber buyBelow = new RelativeNumber("2");//must buy ABOVE this price

        BigDecimal expectedSell = new BigDecimal("2");
        BigDecimal expectedBuy = new BigDecimal("2");

        stopShortTest(Action.SSHORT, sellAbove, buyBelow, expectedSell, expectedBuy);
    }


    private void stopShortTest(Action sellAction, RelativeNumber sellAbove, RelativeNumber buyBelow, BigDecimal expectedSell, BigDecimal expectedBuy) {

        BigDecimal startingCash = new BigDecimal("1000.00");
        Portfolio portfolio = new SimplePortfolio(startingCash);

        int id = 42;
        long time = stop;
        Instrument instrument = Instrument.Forex;
        String symbol = "GG";
        Integer quantity = 10;
        QuantityComputable quantityComputable = new QuantityComputableFixed(quantity);
        long cancelAtMs = Long.MAX_VALUE;
        Duration timeInForce = Duration.GoodTilCancel;

        OrderProcessorStop processor = new OrderProcessorStop(time,symbol,sellAbove);
        Order order = new Order(id,instrument,symbol,sellAction,quantityComputable,cancelAtMs,timeInForce,processor);

        //test only the processor and do it outside the order
        boolean processed = processor.process(dataProvider, portfolio, commission, order, sellAction, quantityComputable);

        BigDecimal expectedCash = startingCash.add(expectedSell.multiply(new BigDecimal(quantity)).subtract(commission));

        assertTrue(processed);
        assertEquals("sell ", expectedSell, processor.transactionPrice());
        assertEquals(expectedCash,portfolio.cash());
        assertEquals(Integer.valueOf(-quantity),portfolio.position("GG").quantity());
        assertEquals(new BigDecimal("-60"),portfolio.equity(dataProvider));

        //Buy to cover this short position

        Action action = Action.BTC;
        OrderProcessorStop buyProcessor = new OrderProcessorStop(time,symbol,buyBelow);
        order = new Order(id,instrument,symbol,action,quantityComputable,cancelAtMs,timeInForce,buyProcessor);

        processed = buyProcessor.process(dataProvider, portfolio, commission, order, action, quantityComputable);
        expectedCash = expectedCash.subtract(expectedBuy.multiply(new BigDecimal(quantity))).subtract(commission);

        assertTrue(processed);
        assertEquals("buy ",expectedBuy, buyProcessor.transactionPrice());
        assertEquals(expectedCash,portfolio.cash());
        assertEquals(Integer.valueOf(0),portfolio.position("GG").quantity());
        assertEquals(new BigDecimal("0"),portfolio.equity(dataProvider));

    }

}
