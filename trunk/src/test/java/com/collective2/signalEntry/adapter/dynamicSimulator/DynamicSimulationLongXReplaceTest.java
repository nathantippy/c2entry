/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  8/2/12
 */
package com.collective2.signalEntry.adapter.dynamicSimulator;

import com.collective2.signalEntry.*;
import com.collective2.signalEntry.adapter.DynamicSimulationAdapter;
import com.collective2.signalEntry.adapter.dynamicSimulator.portfolio.Portfolio;
import com.collective2.signalEntry.adapter.dynamicSimulator.portfolio.SimplePortfolio;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class DynamicSimulationLongXReplaceTest {

    private final double DELTA=.00000001d;

    @Test
    public void longBuySellTest() {

        // validates commands and returns hard coded (canned) responses
        DynamicSimulationAdapter simulationAdapter = new DynamicSimulationAdapter(0l);

        String password = "P455w0rd";
        String eMail = "someone@somewhere.com";
        Portfolio portfolio = new SimplePortfolio(new BigDecimal("10000"));
        BigDecimal commission = new BigDecimal("10.00");
        Integer systemId = simulationAdapter.createSystem("first system",password,portfolio,commission);
        simulationAdapter.subscribe(eMail,systemId);
        C2ServiceFactory factory = new C2ServiceFactory(simulationAdapter);
        C2EntryService sentryService = factory.signalEntryService(password, systemId, eMail);

        validateStartingBalances(systemId, sentryService);

        assertEquals(0, portfolio.position("msft").quantity().intValue());
        Response openResponse = sentryService.stockSignal(ActionForStock.BuyToOpen)
                                        .marketOrder().quantity(10).symbol("msft")
                                        .duration(Duration.GoodTilCancel).send();

        Integer signalId = openResponse.getInteger(C2Element.ElementSignalId);

        assertEquals(0,signalId.intValue());

        long timeStep = 60000l*60l*24l;
        long time = 100000l;
        BigDecimal fixedPrice = new BigDecimal("80.43");

        DynamicSimulationMockDataProvider dataProvider = new DynamicSimulationMockDataProvider(time,fixedPrice,fixedPrice,fixedPrice,fixedPrice,time);

        simulationAdapter.tick(dataProvider,sentryService);

        Number buyPower = sentryService.buyPower(); // 10000 - ((10 * 80.43)+10) = 10000-814.30
        assertEquals(9185.7d,buyPower.doubleValue(),DELTA);

        Number systemEquity = sentryService.systemEquity(); //10 * 80.43 = 804.30
        assertEquals(804.30d,systemEquity.doubleValue(),DELTA);

        final Set<C2Element> checkedElements = new HashSet<C2Element>();
        sentryService.sendSystemHypotheticalRequest(systemId).visitC2Elements(new C2ElementVisitor() {
            @Override
            public void visit(C2Element element, String data) {

                switch (element) {
                    case ElementTotalEquityAvail:
                        checkedElements.add(element);
                        assertEquals(9990d, Double.parseDouble(data), DELTA);
                        break;
                    case ElementCash:
                        checkedElements.add(element);
                        assertEquals(9185.70d, Double.parseDouble(data), DELTA);
                        break;
                    case ElementEquity:
                        checkedElements.add(element);
                        assertEquals(804.30d, Double.parseDouble(data), DELTA);
                        break;
                    case ElementMarginUsed:
                        checkedElements.add(element);
                        assertEquals(0d, Double.parseDouble(data), DELTA);
                        break;
                }
            }
        },C2Element.ElementTotalEquityAvail,
                C2Element.ElementCash,
                C2Element.ElementEquity,
                C2Element.ElementMarginUsed);
        assertEquals("expected to check 4 elements",4,checkedElements.size());
        assertEquals(10, portfolio.position("msft").quantity().intValue());

        ///////////////////
        //market order sell test
        ///////////////////
        Response marketOrderResponse = sentryService.stockSignal(ActionForStock.SellToClose)
                .marketOrder().quantity(10).symbol("msft")
                .duration(Duration.GoodTilCancel).send();

        //force request response now
        marketOrderResponse.getXML();

        ///////////////////////
        //tick for market buy
        /////////////////////
        long closeTime = 200000l;
        BigDecimal closePrice = new BigDecimal("160.86");
        dataProvider = new DynamicSimulationMockDataProvider(time,fixedPrice,closePrice,fixedPrice,closePrice,closeTime);
        simulationAdapter.tick(dataProvider,sentryService);

        ////////////////////
        //confirm values
        ///////////////////
        assertEquals(0, portfolio.position("msft").quantity().intValue());
        buyPower = sentryService.buyPower();
        assertEquals(9980d,buyPower.doubleValue(),DELTA);

        systemEquity = sentryService.systemEquity(); //10 * 80.43 = 804.30
        assertEquals(0d,systemEquity.doubleValue(),DELTA);

        ///////////////////
        //limit order test
        //////////////////
        BigDecimal failLimit = new BigDecimal("65.10");
        Response limitOrderResponse = sentryService.stockSignal(ActionForStock.BuyToOpen)
                .limitOrder(failLimit).quantity(10).symbol("msft")
                .duration(Duration.DayOrder).send();


        ///////////////////////////
        //tick for limit order fail
        ////////////////////////////
        dataProvider.incTime(timeStep);
        simulationAdapter.tick(dataProvider,sentryService);

        ////////////////////
        //confirm unchanged values
        ///////////////////
        assertEquals(0, portfolio.position("msft").quantity().intValue());
        buyPower = sentryService.buyPower();
        assertEquals(9980d,buyPower.doubleValue(),DELTA);

        systemEquity = sentryService.systemEquity(); //10 * 80.43 = 804.30
        assertEquals(0d,systemEquity.doubleValue(),DELTA);

        ///////////////////
        //limit order test
        //////////////////
        BigDecimal successLimit = new BigDecimal("170.10");
        limitOrderResponse = sentryService.stockSignal(ActionForStock.BuyToOpen)
                .limitOrder(successLimit).quantity(10).symbol("msft")
                .duration(Duration.DayOrder).send();

        //force request response now
        Integer openSignalId = limitOrderResponse.getInteger(C2Element.ElementSignalId);

        ///////////////////////////
        //tick for limit order success
        ////////////////////////////
        dataProvider.incTime(timeStep);
        simulationAdapter.tick(dataProvider,sentryService);

        ////////////////////
        //confirm changed
        ///////////////////
        BigDecimal entryPrice = portfolio.position("msft").openPrice();
        assertTrue(entryPrice.compareTo(successLimit)<=0);

        BigDecimal shares = new BigDecimal("10");
        assertEquals(shares.intValue(), portfolio.position("msft").quantity().intValue());

        BigDecimal openEquity = entryPrice.multiply(shares);
        BigDecimal expectedBuyPower = new BigDecimal("9980").subtract(commission.add(openEquity));

        buyPower = sentryService.buyPower();
        assertEquals(expectedBuyPower.doubleValue(),buyPower.doubleValue(),DELTA);

        BigDecimal closeEquity = closePrice.multiply(shares);
        systemEquity = sentryService.systemEquity();
        assertEquals(closeEquity.doubleValue(),systemEquity.doubleValue(),DELTA);


        ///////////////////
        //market limit sell fail test
        ///////////////////
        BigDecimal sellLimitFail = new BigDecimal("180.10");
        Response marketLimitSellResponse = sentryService.stockSignal(ActionForStock.SellToClose)
                .limitOrder(sellLimitFail).quantity(10).symbol("msft")
                .duration(Duration.DayOrder).send();

        //force request response now
        marketLimitSellResponse.getXML();

        ///////////////////////////
        //tick for limit order fail
        ////////////////////////////
        dataProvider.incTime(timeStep);
        simulationAdapter.tick(dataProvider,sentryService);

        ////////////////////
        //confirm nothing changed
        ///////////////////
        entryPrice = portfolio.position("msft").openPrice();
        assertTrue(entryPrice.compareTo(successLimit)<=0);

        shares = new BigDecimal("10");
        assertEquals(shares.intValue(), portfolio.position("msft").quantity().intValue());

        openEquity = entryPrice.multiply(shares);
        expectedBuyPower = new BigDecimal("9980").subtract(commission.add(openEquity));

        buyPower = sentryService.buyPower();
        assertEquals(expectedBuyPower.doubleValue(),buyPower.doubleValue(),DELTA);

        closeEquity = closePrice.multiply(shares);
        systemEquity = sentryService.systemEquity();
        assertEquals(closeEquity.doubleValue(),systemEquity.doubleValue(),DELTA);

        ///////////////////
        //market limit sell success test
        ///////////////////
        BigDecimal sellLimitSuccess = new BigDecimal("110.10");
        marketLimitSellResponse = sentryService.stockSignal(ActionForStock.SellToClose)
                .limitOrder(sellLimitSuccess).quantity(10).symbol("msft")
                .duration(Duration.DayOrder).send();

        ///////////////////////////
        //tick for limit order success
        ////////////////////////////
        dataProvider.incTime(timeStep);
        simulationAdapter.tick(dataProvider,sentryService);

        shares = new BigDecimal("0");
        assertEquals(shares.intValue(), portfolio.position("msft").quantity().intValue());

        //extra simple methods

        boolean ok = sentryService.flushPendingSignals();
        assertTrue(ok);

        String firstMessage = "first test message";
        String secondMessage = "second test message";
        String lastMessage =  sentryService.sendNewCommentRequest(firstMessage,openSignalId).getString(C2Element.ElementPreviousComment);
        assertFalse(firstMessage.equals(lastMessage));
        lastMessage =  sentryService.sendNewCommentRequest(secondMessage,openSignalId).getString(C2Element.ElementPreviousComment);
        assertEquals(firstMessage, lastMessage);


    }

    @Test
    public void allInOneBuySellTestStoppedOutOCA() {
        allInOneBuySellTestStoppedOut(false, Duration.GoodTilCancel);
    }
    @Test
    public void allInOneBuySellTestStoppedOutNoOCA() {
        allInOneBuySellTestStoppedOut(true, Duration.GoodTilCancel);
    }

    @Test
    public void allInOneBuySellTestStoppedOutOCADayOrder() {
        allInOneBuySellTestStoppedOut(false, Duration.DayOrder);
    }
    @Test
    public void allInOneBuySellTestStoppedOutNoOCADayOrder() {
        allInOneBuySellTestStoppedOut(true, Duration.DayOrder);
    }

    private void allInOneBuySellTestStoppedOut(boolean noOCA, Duration timeInForce) {

        // validates commands and returns hard coded (canned) responses
        DynamicSimulationAdapter simulationAdapter = new DynamicSimulationAdapter(0l);

        String password = "P455w0rd";
        String eMail = "someone@somewhere.com";
        Portfolio portfolio = new SimplePortfolio(new BigDecimal("10000"));
        BigDecimal commission = new BigDecimal("10.00");
        Integer systemId = simulationAdapter.createSystem("first system",password,portfolio,commission);
        simulationAdapter.subscribe(eMail,systemId);
        C2ServiceFactory factory = new C2ServiceFactory(simulationAdapter);
        C2EntryService sentryService = factory.signalEntryService(password, systemId, eMail);

        validateStartingBalances(systemId, sentryService);

        BigDecimal stopLoss = new BigDecimal("20.50");
        BigDecimal profitTarget = new BigDecimal("120.50");

        assertEquals(0, portfolio.position("msft").quantity().intValue());
        Response openResponse = sentryService.stockSignal(ActionForStock.BuyToOpen)
                .marketOrder().quantity(10).symbol("msft")
                .stopLoss(BasePrice.Absolute,stopLoss,noOCA).profitTarget(profitTarget)
                .duration(timeInForce).send();

        final AtomicInteger signalId = new AtomicInteger(0);
        final AtomicInteger profitTargetSignalId = new AtomicInteger(0);
        final AtomicInteger stopLossSignalId = new AtomicInteger(0);

        openResponse.visitC2Elements(new C2ElementVisitor() {
            @Override
            public void visit(C2Element element, String data) {
                switch(element) {
                    case ElementSignalId:
                        signalId.set(Integer.valueOf(data));
                        break;
                    case ElementProfitTaretSignalId:
                        profitTargetSignalId.set(Integer.valueOf(data));
                        break;
                    case ElementStopLossSignalId:
                        stopLossSignalId.set(Integer.valueOf(data));
                        break;
                }
            }
        }, C2Element.ElementSignalId, C2Element.ElementStopLossSignalId, C2Element.ElementProfitTaretSignalId );


        long timeStep = 60000l*60l*24l;
        long openTime = 0l;
        long closeTime = openTime+timeStep;

        BigDecimal lowPrice = new BigDecimal("80");
        BigDecimal highPrice = new BigDecimal("100");
        DynamicSimulationMockDataProvider dataProvider = new DynamicSimulationMockDataProvider(
                openTime,lowPrice,highPrice,lowPrice,highPrice,closeTime);


        final Set<Integer> pendingSignalIdSet = new HashSet<Integer>();
        sentryService.sendAllSignalsRequest().visitC2Elements(new C2ElementVisitor() {
            @Override
            public void visit(C2Element element, String data) {
                if (C2Element.ElementSignalId==element) {
                    pendingSignalIdSet.add(Integer.parseInt(data));
                }
            }
        },C2Element.ElementSignalId);

        assertEquals(profitTargetSignalId.toString(),3,pendingSignalIdSet.size());
        assertTrue(pendingSignalIdSet.contains(profitTargetSignalId.intValue()));
        assertTrue(pendingSignalIdSet.contains(stopLossSignalId.intValue()));

        //confirm that the target can be adjusted
        BigDecimal newTarget = new BigDecimal("10");
        Response adjClose = sentryService.stockSignal(ActionForStock.SellToClose)
                .stopOrder(newTarget).quantity(10).symbol("msft")
                .duration(Duration.GoodTilCancel)
                .xReplace(stopLossSignalId.intValue()).send();

        Integer newCloseSignalId = adjClose.getInteger(C2Element.ElementSignalId);

        //confirm the pending list of signals has been updated
        updateSetWithPendingSignalIds(sentryService, pendingSignalIdSet);

        assertTrue(pendingSignalIdSet.contains(newCloseSignalId));
        if (noOCA) {
            assertTrue(pendingSignalIdSet.toString(),pendingSignalIdSet.contains(profitTargetSignalId.intValue()));
            assertEquals(3,pendingSignalIdSet.size());
        } //else can not be tested because it's modified when tick is called

        //not filled yet so quantity should be zero
        //this however has the quantity because QuantityComputableFixed is used above
        assertEquals(Integer.valueOf(10),sentryService.sendSignalStatusRequest(newCloseSignalId,true).getInteger(C2Element.ElementQuant));

        //send tick data to force open position if its still in force
        dataProvider.incTime(timeStep,new BigDecimal("13"));
        simulationAdapter.tick(dataProvider,sentryService);

        updateSetWithPendingSignalIds(sentryService, pendingSignalIdSet);

        if (timeInForce==Duration.DayOrder) {
            assertEquals(0, portfolio.position("msft").quantity().intValue());
            assertTrue(pendingSignalIdSet.isEmpty());
        } else {
            assertEquals(10, portfolio.position("msft").quantity().intValue());
            assertTrue(pendingSignalIdSet.contains(newCloseSignalId));

            //get signal details
            assertEquals(Integer.valueOf(10),sentryService.sendSignalStatusRequest(newCloseSignalId,true).getInteger(C2Element.ElementQuant));

            if (noOCA) {
                assertTrue(pendingSignalIdSet.toString()+" can not find profit target signal "+profitTargetSignalId,pendingSignalIdSet.contains(profitTargetSignalId.intValue()));
                assertEquals(2,pendingSignalIdSet.size());
                assertEquals(Integer.valueOf(10),sentryService.sendSignalStatusRequest(profitTargetSignalId.intValue(),true).getInteger(C2Element.ElementQuant));

            } else {
                assertEquals(1,pendingSignalIdSet.size());
            }
        }
        dataProvider.incTime(timeStep,new BigDecimal("9"));
        simulationAdapter.tick(dataProvider,sentryService);
        assertEquals(0, portfolio.position("msft").quantity().intValue());

        //for oca orders the other order will get cancelled when the first is triggered.
        //for non oca orders there will be one closing order however it should not be valid
        //because its a close against something closed.

        //has any signal been left behind if we used good till cancel
        if (timeInForce!=Duration.DayOrder) {
            //ensure we have no pending signals now that position is closed
            updateSetWithPendingSignalIds(sentryService, pendingSignalIdSet);
            assertEquals("should not have found any signals but found:"+pendingSignalIdSet, (noOCA ? 1 : 0),pendingSignalIdSet.size());

            //testing cancel of the remaining order because its open has already been closed
            if (noOCA) {
                sentryService.cancel(pendingSignalIdSet.iterator().next());

                //ensure we have no pending signals now that last one is cancelled
                updateSetWithPendingSignalIds(sentryService, pendingSignalIdSet);
                assertEquals("should not have found any signals but found:"+pendingSignalIdSet, 0,pendingSignalIdSet.size());

            }
        }

    }

    private void updateSetWithPendingSignalIds(C2EntryService sentryService, final Set<Integer> pendingSignalIdSet) {
        pendingSignalIdSet.clear();
        sentryService.sendAllSignalsRequest().visitC2Elements(new C2ElementVisitor() {
            @Override
            public void visit(C2Element element, String data) {
                if (C2Element.ElementSignalId==element) {
                    pendingSignalIdSet.add(Integer.parseInt(data));
                }
            }
        },C2Element.ElementSignalId);
    }

    @Test
    public void allInOneBuySellTestProfitTargetHitOCA() {
        allInOneBuySellTestProfitTargetHit(false, Duration.GoodTilCancel);
    }

    @Test
    public void allInOneBuySellTestProfitTargetHitNoOCA() {
        allInOneBuySellTestProfitTargetHit(true, Duration.GoodTilCancel);
    }

    @Test
    public void allInOneBuySellTestProfitTargetHitOCADayOrder() {
        allInOneBuySellTestProfitTargetHit(false, Duration.DayOrder);
    }

    @Test
    public void allInOneBuySellTestProfitTargetHitNoOCADayOrder() {
        allInOneBuySellTestProfitTargetHit(true, Duration.DayOrder);
    }


    private void allInOneBuySellTestProfitTargetHit(boolean noOCA, Duration timeInForce) {

        // validates commands and returns hard coded (canned) responses
        DynamicSimulationAdapter simulationAdapter = new DynamicSimulationAdapter(0l);

        String password = "P455w0rd";
        String eMail = "someone@somewhere.com";
        Portfolio portfolio = new SimplePortfolio(new BigDecimal("10000"));
        BigDecimal commission = new BigDecimal("10.00");
        Integer systemId = simulationAdapter.createSystem("first system",password,portfolio,commission);
        simulationAdapter.subscribe(eMail,systemId);
        C2ServiceFactory factory = new C2ServiceFactory(simulationAdapter);
        C2EntryService sentryService = factory.signalEntryService(password, systemId, eMail);

        validateStartingBalances(systemId, sentryService);

        BigDecimal stopLoss = new BigDecimal("20.50");
        BigDecimal profitTarget = new BigDecimal("120.50");

        assertEquals(0, portfolio.position("msft").quantity().intValue());

        Response openResponse = sentryService.stockSignal(ActionForStock.BuyToOpen)
                .marketOrder().quantity(10).symbol("msft")
                .stopLoss(stopLoss).profitTarget(BasePrice.Absolute,profitTarget,noOCA)
                .duration(timeInForce).send();

        final AtomicInteger signalId = new AtomicInteger(0);
        final AtomicInteger profitTargetSignalId = new AtomicInteger(0);
        final AtomicInteger stopLossSignalId = new AtomicInteger(0);

        openResponse.visitC2Elements(new C2ElementVisitor() {
            @Override
            public void visit(C2Element element, String data) {
                switch(element) {
                    case ElementSignalId:
                        signalId.set(Integer.valueOf(data));
                        break;
                    case ElementProfitTaretSignalId:
                        profitTargetSignalId.set(Integer.valueOf(data));
                        break;
                    case ElementStopLossSignalId:
                        stopLossSignalId.set(Integer.valueOf(data));
                        break;
                }
            }
        }, C2Element.ElementSignalId, C2Element.ElementStopLossSignalId, C2Element.ElementProfitTaretSignalId );

        sentryService.awaitPending();
        assertTrue(signalId.intValue()>=0);
        assertTrue(profitTargetSignalId.intValue()>0);
        assertTrue(stopLossSignalId.intValue()>0);

        long timeStep = 60000l*60l*24l;
        long openTime = 0l;
        long closeTime = openTime+timeStep;

        BigDecimal closePrice = new BigDecimal("160.86");
        BigDecimal lowPrice = new BigDecimal("80");
        BigDecimal highPrice = new BigDecimal("100");
        DynamicSimulationMockDataProvider dataProvider = new DynamicSimulationMockDataProvider(
                openTime,lowPrice,highPrice,lowPrice,highPrice,closeTime);

        final Set<Integer> pendingSignalIdSet = new HashSet<Integer>();
        sentryService.sendAllSignalsRequest().visitC2Elements(new C2ElementVisitor() {
            @Override
            public void visit(C2Element element, String data) {
                if (C2Element.ElementSignalId==element) {
                    pendingSignalIdSet.add(Integer.parseInt(data));
                }
            }
        },C2Element.ElementSignalId);

        assertEquals(profitTargetSignalId.toString(),3,pendingSignalIdSet.size());
        assertTrue(pendingSignalIdSet.contains(profitTargetSignalId.intValue()));
        assertTrue(pendingSignalIdSet.contains(stopLossSignalId.intValue()));

        //confirm that the target can be adjusted
        BigDecimal newTarget = new BigDecimal("160");
        Response adjClose = sentryService.stockSignal(ActionForStock.SellToClose)
                .limitOrder(newTarget).quantity(10).symbol("msft")   //TODO: is quantity optional wth xReplace?
                .duration(Duration.GoodTilCancel)
                .xReplace(profitTargetSignalId.intValue()).send();

        Integer newCloseSignalId = adjClose.getInteger(C2Element.ElementSignalId);

        dataProvider.incTime(timeStep,new BigDecimal("121"));
        simulationAdapter.tick(dataProvider,sentryService);

        //these tests do not work for DayOrders because all the signals have already expired
        if (timeInForce==Duration.GoodTilCancel) {
            assertEquals(10, portfolio.position("msft").quantity().intValue());

            updateSetWithPendingSignalIds(sentryService, pendingSignalIdSet);

            assertTrue(pendingSignalIdSet.contains(newCloseSignalId));
            if (noOCA) {
                assertTrue(pendingSignalIdSet.contains(stopLossSignalId.intValue()));
                assertEquals(2,pendingSignalIdSet.size());
            } else {
                assertEquals(1,pendingSignalIdSet.size());
            }
            dataProvider.incTime(timeStep,new BigDecimal("161"));
            simulationAdapter.tick(dataProvider,sentryService);
            assertEquals(0, portfolio.position("msft").quantity().intValue());

            //for oca orders the other order will get cancelled when the first is triggered.
            //for non oca orders there will be one closing order however it should not be valid
            //because its a close against something closed.

            //ensure we have no pending signals now that position is closed
            updateSetWithPendingSignalIds(sentryService, pendingSignalIdSet);
            assertEquals("should not have found any signals but found:"+pendingSignalIdSet, (noOCA ? 1 : 0),pendingSignalIdSet.size());

            //testing cancel of the remaining order because its open has already been closed
            if (noOCA) {
                sentryService.cancel(pendingSignalIdSet.iterator().next());

                //ensure we have no pending signals now that last one is cancelled
                updateSetWithPendingSignalIds(sentryService, pendingSignalIdSet);
                assertEquals("should not have found any signals but found:"+pendingSignalIdSet, 0,pendingSignalIdSet.size());

            }
        }

    }

    private void validateStartingBalances(Integer systemId, C2EntryService sentryService) {
        sentryService.sendSystemHypotheticalRequest(systemId).visitC2Elements(new C2ElementVisitor() {
            @Override
            public void visit(C2Element element, String data) {

                switch (element) {
                    case ElementTotalEquityAvail:
                        assertEquals(10000d,Double.parseDouble(data),DELTA);
                        break;
                    case ElementCash:
                        assertEquals(10000d,Double.parseDouble(data),DELTA);
                        break;
                    case ElementEquity:
                        assertEquals(0d,Double.parseDouble(data),DELTA);
                        break;
                    case ElementMarginUsed:
                        assertEquals(0d,Double.parseDouble(data),DELTA);
                        break;
                }
            }
        },C2Element.ElementTotalEquityAvail,
                C2Element.ElementCash,
                C2Element.ElementEquity,
                C2Element.ElementMarginUsed);
    }

    //TODO: copy this class for shorts test.

}
