/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/15/12
 */
package com.collective2.signalEntry;

import static org.junit.Assert.assertTrue;

import com.collective2.signalEntry.adapter.StaticSimulationAdapter;
import org.junit.Test;

import com.collective2.signalEntry.adapter.BackEndAdapter;
import com.collective2.signalEntry.adapter.Collective2Adapter;

public class ExamplesTest {

    // these are the examples found on the project home page
    // make sure that anything given as an example really does work.

    @Test
    public void exampleFactoryConstructionTest() {

        boolean isLive = true;
        do {
            isLive = !isLive; // set to constant on example page
            C2ServiceFactory factory;

            if (isLive) {
                // connects to collective2.com and returns the responses
                BackEndAdapter liveAdapter = new Collective2Adapter();
                factory = new C2ServiceFactory(liveAdapter);
            } else {
                // validates commands and returns hard coded (canned) responses
                BackEndAdapter simulationAdapter = new StaticSimulationAdapter();
                factory = new C2ServiceFactory(simulationAdapter);
            }

            if (isLive) {
                assertTrue(factory.adapter() instanceof Collective2Adapter);
            } else {
                assertTrue(factory.adapter() instanceof StaticSimulationAdapter);
            }
        } while (isLive == false);

    }

    @Test
    public void exampleServiceConstructionTest() {

        // validates commands and returns hard coded (canned) responses
        BackEndAdapter simulationAdapter = new StaticSimulationAdapter();
        C2ServiceFactory factory = new C2ServiceFactory(simulationAdapter);

        String password = "PA55WORD";
        Integer systemId = 99999999;
        String eMail = "someone@somewhere.com";
        C2EntryService sentryService;

        /*
           three different constructors are provided for the service
           some commands require systemId and other require email address.

           sentryService = factory.signalEntryService(password, eMail);
           sentryService = factory.signalEntryService(password, systemId);
           sentryService = factory.signalEntryService(password, systemId, eMail);

           if after the fact one of these needs to be set

           sentryService.systemId(systemId);
           sentryService.password(password);
           sentryService.eMail(eMail);
        */
        sentryService = factory.signalEntryService(password, eMail);
        sentryService = factory.signalEntryService(password, systemId);
        sentryService = factory.signalEntryService(password, systemId, eMail);

        // if after the fact one of these needs to be set there are methods
        sentryService.systemId(systemId);
        sentryService.password(password);
        sentryService.eMail(eMail);

    }

    @Test
    public void exampleServiceUsageTest() {
        // validates commands and returns hard coded (canned) responses
        BackEndAdapter simulationAdapter = new StaticSimulationAdapter();
        C2ServiceFactory factory = new C2ServiceFactory(simulationAdapter);

        String password = "PA55WORD";
        Integer systemId = 99999999;
        String eMail = "someone@somewhere.com";
        C2EntryService sentryService = factory.signalEntryService(password, systemId, eMail);

        Response response = sentryService.stockSignal(ActionForStock.BuyToOpen)
                            .marketOrder().quantity(10).symbol("msft")
                            .duration(Duration.GoodTilCancel).send();
        Integer signalId = response.getInteger(C2Element.ElementSignalId);
        
    }

}
