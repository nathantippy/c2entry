package com.collective2.signalEntry.adapter.dynamicSimulator;

import com.collective2.signalEntry.*;
import com.collective2.signalEntry.adapter.DynamicSimulationAdapter;
import com.collective2.signalEntry.adapter.dynamicSimulator.DynamicSimulationMockDataProvider;
import com.collective2.signalEntry.adapter.dynamicSimulator.portfolio.Portfolio;
import com.collective2.signalEntry.adapter.dynamicSimulator.portfolio.SimplePortfolio;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  8/26/12
 */

public class DynamicSimulationQuanityTest {

    private final double DELTA=.00000001d;

    @Test
    public void quantityDollarsTest() {
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
        Response openResponse = sentryService.stockSignal(ActionForStock.BuyToOpen)
                .marketOrder().dollars(new BigDecimal("814.3")).symbol("msft")
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
    }

    @Test
    public void quantityPercentTest() {
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

        Response openResponse = sentryService.stockSignal(ActionForStock.BuyToOpen)
                .marketOrder().accountPercent(new BigDecimal("0.08143")).symbol("msft")
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
    }

}
