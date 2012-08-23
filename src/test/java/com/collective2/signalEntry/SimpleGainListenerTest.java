package com.collective2.signalEntry;

import com.collective2.signalEntry.adapter.DynamicSimulationAdapter;
import com.collective2.signalEntry.adapter.dynamicSimulator.GainListener;
import com.collective2.signalEntry.adapter.dynamicSimulator.Portfolio;
import com.collective2.signalEntry.adapter.dynamicSimulator.SimpleGainListener;
import com.collective2.signalEntry.adapter.dynamicSimulator.SimplePortfolio;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  8/17/12
 */

public class SimpleGainListenerTest {

    @Test
    public void simpleGainListenerTest() {

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

        long timeStep = 60000l*60l*24l;

        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        PrintStream printStream = new PrintStream(baos);

        GainListener listener = new SimpleGainListener(printStream);
        long start = 0;
        long period = timeStep;
        simulationAdapter.addGainListener(start,period,listener);


        BigDecimal stopLoss = new BigDecimal("20.50");
        BigDecimal profitTarget = new BigDecimal("120.50");

        assertEquals(0, portfolio.position("msft").quantity().intValue());
        Response openResponse = sentryService.stockSignal(ActionForStock.BuyToOpen)
                .marketOrder().quantity(10).symbol("msft")
                .stopLoss(stopLoss).profitTarget(profitTarget)
                .duration(Duration.GoodTilCancel).send();


        long openTime = 0l;
        long closeTime = openTime+timeStep;

        BigDecimal closePrice = new BigDecimal("160.86");
        BigDecimal lowPrice = new BigDecimal("80");
        BigDecimal highPrice = new BigDecimal("100");
        DynamicSimulationMockDataProvider dataProvider = new DynamicSimulationMockDataProvider(
                openTime,lowPrice,highPrice,lowPrice,highPrice,closeTime);

        simulationAdapter.tick(dataProvider, sentryService);

        assertEquals(10, portfolio.position("msft").quantity().intValue());

        dataProvider.incTime(timeStep, new BigDecimal("22"));
        simulationAdapter.tick(dataProvider, sentryService);

        assertEquals(10, portfolio.position("msft").quantity().intValue());

        dataProvider.incTime(timeStep,new BigDecimal("10"));
        simulationAdapter.tick(dataProvider, sentryService);


        simulationAdapter.awaitGainListeners();

        //results
        System.out.println(baos.toString());



//
//        //should have hit sell stop with this low price
//        assertEquals(0, portfolio.position("msft").quantity().intValue());

    }

}
