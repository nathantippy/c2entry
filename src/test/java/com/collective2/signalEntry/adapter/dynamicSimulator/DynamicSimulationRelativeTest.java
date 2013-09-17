package com.collective2.signalEntry.adapter.dynamicSimulator;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.HashSet;

import org.junit.Test;

import com.collective2.signalEntry.ActionForStock;
import com.collective2.signalEntry.BasePrice;
import com.collective2.signalEntry.C2Element;
import com.collective2.signalEntry.C2EntryService;
import com.collective2.signalEntry.C2ServiceFactory;
import com.collective2.signalEntry.Duration;
import com.collective2.signalEntry.Response;
import com.collective2.signalEntry.Signal;
import com.collective2.signalEntry.adapter.DynamicSimulationAdapter;
import com.collective2.signalEntry.adapter.dynamicSimulator.portfolio.Portfolio;
import com.collective2.signalEntry.adapter.dynamicSimulator.portfolio.SimplePortfolio;
import com.collective2.signalEntry.adapter.dynamicSimulator.portfolio.SimplePortfolioFactory;

/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  9/3/12
 */

public class DynamicSimulationRelativeTest {

    private final BigDecimal stopSize = new BigDecimal("-5");

    private final long timeStep = 60000l*60l*24l;
    private final long time = 100000l;
    private final BigDecimal openData = new BigDecimal("80.43");
    private final BigDecimal lowData = new BigDecimal("79.50");
    private final DynamicSimulationMockDataProvider dataProvider = new DynamicSimulationMockDataProvider(time,openData,openData,lowData,openData,time+timeStep);

    @Test
    public void  testLimitOpenPosition() {
        //limit was above open so stop should be open-5
        runTest(true, BasePrice.PositionOpenPlus,new BigDecimal("82"),new BigDecimal("75.43"),1);
    }

    @Test
    public void  testLimitTriggerPosition() {
        //limit was below open so stop should be limit-5
        runTest(true, BasePrice.PositionOpenPlus,new BigDecimal("79.99"),new BigDecimal("74.99"),1);
    }

    @Test
    public void  testLimitOpenSession() {
        //limit was above open so stop should be open-5
        runTest(true, BasePrice.SessionOpenPlus,new BigDecimal("82"),new BigDecimal("75.43"),1);
    }

    @Test
    public void  testLimitTriggerSession() {
        //limit was below open but stop should be open-5
        runTest(true, BasePrice.SessionOpenPlus,new BigDecimal("79.99"),new BigDecimal("75.43"),1);
    }

    @Test
    public void  testLimitOpenRT() {
        //limit was above open so stop should be open-5
        runTest(true, BasePrice.RTQuotePlus,new BigDecimal("82"),new BigDecimal("75.43"),1);
    }

    @Test
    public void  testLimitTriggerRT() {
        //limit was below open but stop should be open-5
        runTest(true, BasePrice.RTQuotePlus,new BigDecimal("79.99"),new BigDecimal("75.43"),1);
    }

    @Test
    public void  testStopOpenPosition() {  //will never trigger and have 2 pending orders
        // isLimit:false, base:posOpen+, triggerPrice:82, expectedStop:82, expectedPendingCount:2
        //       open:80.43       low:79.50   high:80.43  close:80.43       stopSize:-5    stopOrder (trigger) buy at this price or better
        runTest(false, BasePrice.PositionOpenPlus, new BigDecimal("82"), new BigDecimal("82"), 2);
    }

    @Test
    public void  testStopTriggerPosition() {
        // isLimit:false, base:posOpen+, triggerPrice:79.99, expectedStop:75.43, expectedPendingCount:2
        //       open:80.43       low:79.50   high:80.43  close:80.43       stopSize:-5    stopOrder (trigger) buy at this price or better
        // triggers when we hit open of  80.43 because its > 79.99 so only the stop is pending (1 pending)
        // the stop will be 5 under the open price or 75.43
        runTest(false, BasePrice.PositionOpenPlus,new BigDecimal("79.99"),new BigDecimal("75.43"),1);
    }

    @Test
    public void  testStopOpenSession() {
        //will never trigger and have 2 pending orders
        runTest(false, BasePrice.SessionOpenPlus,new BigDecimal("82"),new BigDecimal("82"),2);
    }

    @Test
    public void  testStopTriggerSession() {
        //because sessionopen and position open are the same this will have same results as the test
        //testStopTriggerPosition above
        runTest(false, BasePrice.SessionOpenPlus,new BigDecimal("79.99"),new BigDecimal("75.43"),1);
    }

    @Test
    public void  testStopOpenRT() {
        //will never trigger and have 2 pending orders
        runTest(false, BasePrice.RTQuotePlus,new BigDecimal("82"),new BigDecimal("82"),2);
    }

    @Test
    public void  testStopTriggerRT() {
        //the last time is the same as the open time so this will have the same stop as pending as testStopTriggerSession
        runTest(false, BasePrice.RTQuotePlus,new BigDecimal("79.99"),new BigDecimal("75.43"),1);
    }


    public void runTest(boolean isLimit, BasePrice base, BigDecimal triggerPrice, BigDecimal expectedStop, int expectedPendingCount) {


        // validates commands and returns hard coded (canned) responses
        DynamicSimulationAdapter simulationAdapter = new DynamicSimulationAdapter(false);

        String password = "P455w0rd";
        String eMail = "someone@somewhere.com";
        Portfolio portfolio = new SimplePortfolioFactory().createPortfolio(new BigDecimal("10000"));
        BigDecimal commission = new BigDecimal("10.00");
        Integer systemId = simulationAdapter.createSystem("first system",password,portfolio,commission);
        simulationAdapter.subscribe(eMail,systemId,password);
        C2ServiceFactory factory = new C2ServiceFactory(simulationAdapter);
        C2EntryService sentryService = factory.signalEntryService(password, systemId, eMail);

        assertEquals(0, portfolio.position("msft").quantity().intValue());

        Signal msft = sentryService.stockSignal(ActionForStock.BuyToOpen)
                .quantity(10).symbol("msft")
                .stopLoss(base, stopSize)
                .duration(Duration.GoodTilCancel);
        if (isLimit) {
            msft = msft.limitOrder(triggerPrice);
        } else {
            msft = msft.stopOrder(triggerPrice);
        }

        Response openResponse = msft.send();

        Integer signalId = openResponse.getInteger(C2Element.ElementSignalId);

        assertEquals(0,signalId.intValue());


        simulationAdapter.tick(dataProvider,sentryService);

        HashSet<Integer> pending = sentryService.sendAllSignalsRequest()
                                                   .collectIntegers(new HashSet<Integer>(),
                                                                    C2Element.ElementSignalId);

        assertEquals(expectedPendingCount, pending.size());
        Integer id = pending.iterator().next();

        BigDecimal stop = sentryService.sendSignalStatusRequest(id,true).getBigDecimal(C2Element.ElementStop);
        assertEquals(expectedStop,stop);

    }




}
