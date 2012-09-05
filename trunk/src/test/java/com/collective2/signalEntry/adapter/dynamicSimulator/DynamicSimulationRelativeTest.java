package com.collective2.signalEntry.adapter.dynamicSimulator;

import com.collective2.signalEntry.*;
import com.collective2.signalEntry.adapter.DynamicSimulationAdapter;
import com.collective2.signalEntry.adapter.dynamicSimulator.portfolio.Portfolio;
import com.collective2.signalEntry.adapter.dynamicSimulator.portfolio.SimplePortfolio;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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


    //relative price testing

    //TODO: this is called so often it must be integrated into the response as   collectIntegerSet(ElementSignalId)
    private Set<Integer> pendingSignalIds(C2EntryService sentryService) {
        final Set<Integer> pendingSignalIdSet = new HashSet<Integer>();
        sentryService.sendAllSignalsRequest().visitC2Elements(new C2ElementVisitor() {
            @Override
            public void visit(C2Element element, String data) {
                if (C2Element.ElementSignalId==element) {
                    pendingSignalIdSet.add(Integer.parseInt(data));
                }
            }
        },C2Element.ElementSignalId);
        return pendingSignalIdSet;
    }


    //TODO: duplcate these with false and test stops
    @Test
    public void  testOpenPosition() {
        //limit was above open so stop should be open-5
        runTest(true, BasePrice.PositionOpenPlus,new BigDecimal("82"),new BigDecimal("75.43"));
    }

    @Test
    public void  testLimitPosition() {
        //limit was below open so stop should be limit-5
        runTest(true, BasePrice.PositionOpenPlus,new BigDecimal("79.99"),new BigDecimal("74.99"));
    }

    @Test
    public void  testOpenSession() {
        //limit was above open so stop should be open-5
        runTest(true, BasePrice.SessionOpenPlus,new BigDecimal("82"),new BigDecimal("75.43"));
    }

    @Test
    public void  testLimitSession() {
        //limit was below open but stop should be open-5
        runTest(true, BasePrice.SessionOpenPlus,new BigDecimal("79.99"),new BigDecimal("75.43"));
    }

    @Test
    public void  testOpenRT() {
        //limit was above open so stop should be open-5
        runTest(true, BasePrice.RTQuotePlus,new BigDecimal("82"),new BigDecimal("75.43"));
    }

    @Test
    public void  testLimitRT() {
        //limit was below open but stop should be open-5
        runTest(true, BasePrice.RTQuotePlus,new BigDecimal("79.99"),new BigDecimal("75.43"));
    }

    public void runTest(boolean isLimit, BasePrice base, BigDecimal triggerPrice, BigDecimal expectedStop) {


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

        Set<Integer> pending = pendingSignalIds(sentryService);

        assertEquals(1,pending.size());
        Integer id = pending.iterator().next();

        BigDecimal stop = sentryService.sendSignalStatusRequest(id,true).getBigDecimal(C2Element.ElementStop);
        assertEquals(expectedStop,stop);

    }




}