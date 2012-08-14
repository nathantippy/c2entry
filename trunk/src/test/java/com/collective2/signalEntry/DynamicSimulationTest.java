/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  8/2/12
 */
package com.collective2.signalEntry;

import com.collective2.signalEntry.adapter.DynamicSimulationAdapter;
import com.collective2.signalEntry.adapter.dynamicSimulator.DataProvider;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;

import static com.collective2.signalEntry.Money.USD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DynamicSimulationTest {

    private final double DELTA=.00000001d;

        // TODO: add new test for this simulationAdapter.addGainListener(0, GainListenerManager.ONE_YEAR_MS/12, new SystemOutGainListener());


    @Test
    public void marketOrderBuyTest() {

        // validates commands and returns hard coded (canned) responses
        DynamicSimulationAdapter simulationAdapter = new DynamicSimulationAdapter(0l);

        String password = "P455w0rd";
        String eMail = "someone@somewhere.com";
        Integer systemId = simulationAdapter.createSystem(new BigDecimal("10000"),"first system",password,new BigDecimal("10.00"));
        simulationAdapter.subscribe(eMail,systemId);
        C2ServiceFactory factory = new C2ServiceFactory(simulationAdapter);
        C2EntryService sentryService = factory.signalEntryService(password, systemId, eMail);

        sentryService.sendSystemHypotheticalRequest(systemId).visitC2Elements(new C2ElementVisitor() {
            @Override
            public void visit(C2Element element, String data, Deque<String> stack) {

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
        });

        Response openResponse = sentryService.stockSignal(ActionForStock.BuyToOpen)
                                        .marketOrder().quantity(10).symbol("msft")
                                        .duration(Duration.GoodTilCancel).send();

        Integer signalId = openResponse.getInteger(C2Element.ElementSignalId);

        assertEquals(0,signalId.intValue());

        long time = 100000l;
        BigDecimal fixedPrice = new BigDecimal("80.43");

        DataProvider dataProvider = new DynamicSimulationMockDataProvider(time,fixedPrice,fixedPrice,fixedPrice,fixedPrice,time);

        simulationAdapter.tick(dataProvider);

        Number buyPower = sentryService.buyPower(); // 10000 - ((10 * 80.43)+10) = 10000-814.30
        assertEquals(9185.7d,buyPower.doubleValue(),DELTA);

        Number systemEquity = sentryService.systemEquity(); //10 * 80.43 = 804.30
        assertEquals(804.30d,systemEquity.doubleValue(),DELTA);

        final Set<C2Element> checkedElements = new HashSet<C2Element>();
        sentryService.sendSystemHypotheticalRequest(systemId).visitC2Elements(new C2ElementVisitor() {
            @Override
            public void visit(C2Element element, String data, Deque<String> stack) {

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
        });
        assertEquals("expected to check 4 elements",4,checkedElements.size());


        Response closeResponse = sentryService.stockSignal(ActionForStock.SellToClose)
                .marketOrder().quantity(10).symbol("msft")
                .duration(Duration.GoodTilCancel).send();

        closeResponse.getXML();

        long closeTime = 200000l;
        BigDecimal closePrice = new BigDecimal("160.86");

        dataProvider = new DynamicSimulationMockDataProvider(time,fixedPrice,fixedPrice,closePrice,closePrice,closeTime);

        simulationAdapter.tick(dataProvider);

        buyPower = sentryService.buyPower();
        assertEquals(10784.30d,buyPower.doubleValue(),DELTA);

        systemEquity = sentryService.systemEquity(); //10 * 80.43 = 804.30
        assertEquals(0d,systemEquity.doubleValue(),DELTA);

    }



//    @Test
//    public void limitOrderBuyTest() {
//
//        // validates commands and returns hard coded (canned) responses
//        DynamicSimulationAdapter simulationAdapter = new DynamicSimulationAdapter(0l);
//
//        String password = "P455w0rd";
//        String eMail = "someone@somewhere.com";
//        Integer systemId = simulationAdapter.createSystem(new BigDecimal("10000"),"first system",password,new BigDecimal("10.00"));
//        simulationAdapter.subscribe(eMail,systemId);
//        C2ServiceFactory factory = new C2ServiceFactory(simulationAdapter);
//        C2EntryService sentryService = factory.signalEntryService(password, systemId, eMail);
//
//        sentryService.sendSystemHypotheticalRequest(systemId).visitC2Elements(new C2ElementVisitor() {
//            @Override
//            public void visit(C2Element element, String data, Deque<String> stack) {
//
//                switch (element) {
//                    case ElementTotalEquityAvail:
//                        assertEquals(10000d,Double.parseDouble(data),DELTA);
//                        break;
//                    case ElementCash:
//                        assertEquals(10000d,Double.parseDouble(data),DELTA);
//                        break;
//                    case ElementEquity:
//                        assertEquals(0d,Double.parseDouble(data),DELTA);
//                        break;
//                    case ElementMarginUsed:
//                        assertEquals(0d,Double.parseDouble(data),DELTA);
//                        break;
//                }
//            }
//        });
//
//        Response openResponse = sentryService.stockSignal(ActionForStock.BuyToOpen)
//                .limitOrder(USD(90)).quantity(10).symbol("msft")
//                .duration(Duration.GoodTilCancel).send();
//
//        Integer signalId = openResponse.getInteger(C2Element.ElementSignalId);
//
//        assertEquals(0,signalId.intValue());
//
//        long time = 100000l;
//        BigDecimal fixedPrice = new BigDecimal("80.43");
//
//        DataProvider dataProvider = new DynamicSimulationMockDataProvider(time,fixedPrice,fixedPrice,fixedPrice,fixedPrice,time);
//
//        simulationAdapter.tick(dataProvider);
//
//        Number buyPower = sentryService.buyPower(); // 10000 - ((10 * 80.43)+10) = 10000-814.30
//        assertEquals(9185.7d,buyPower.doubleValue(),DELTA);
//
//        Number systemEquity = sentryService.systemEquity(); //10 * 80.43 = 804.30
//        assertEquals(804.30d,systemEquity.doubleValue(),DELTA);
//
//        final Set<C2Element> checkedElements = new HashSet<C2Element>();
//        sentryService.sendSystemHypotheticalRequest(systemId).visitC2Elements(new C2ElementVisitor() {
//            @Override
//            public void visit(C2Element element, String data, Deque<String> stack) {
//
//                switch (element) {
//                    case ElementTotalEquityAvail:
//                        checkedElements.add(element);
//                        assertEquals(9990d, Double.parseDouble(data), DELTA);
//                        break;
//                    case ElementCash:
//                        checkedElements.add(element);
//                        assertEquals(9185.70d, Double.parseDouble(data), DELTA);
//                        break;
//                    case ElementEquity:
//                        checkedElements.add(element);
//                        assertEquals(804.30d, Double.parseDouble(data), DELTA);
//                        break;
//                    case ElementMarginUsed:
//                        checkedElements.add(element);
//                        assertEquals(0d, Double.parseDouble(data), DELTA);
//                        break;
//                }
//            }
//        });
//        assertEquals("expected to check 4 elements",4,checkedElements.size());
//
//
//        Response closeResponse = sentryService.stockSignal(ActionForStock.SellToClose)
//                .limitOrder(USD(160.86)).quantity(10).symbol("msft")
//                .duration(Duration.GoodTilCancel).send();
//
//        closeResponse.getXML();
//
//        long closeTime = 200000l;
//        BigDecimal closePrice = new BigDecimal("160.86");
//
//        dataProvider = new DynamicSimulationMockDataProvider(time,fixedPrice,fixedPrice,closePrice,closePrice,closeTime);
//
//        simulationAdapter.tick(dataProvider);
//
//        buyPower = sentryService.buyPower();
//        assertEquals(10784.30d,buyPower.doubleValue(),DELTA);
//
//        systemEquity = sentryService.systemEquity(); //10 * 80.43 = 804.30
//        assertEquals(0d,systemEquity.doubleValue(),DELTA);
//
//    }

}
