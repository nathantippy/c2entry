/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/15/12
 */
package com.collective2.signalEntry;

import com.collective2.signalEntry.adapter.C2EntryServiceAdapter;
import com.collective2.signalEntry.adapter.Collective2Adapter;
import com.collective2.signalEntry.adapter.StaticSimulationAdapter;
import com.collective2.signalEntry.journal.C2EntryServiceJournal;
import com.collective2.signalEntry.journal.C2EntryServiceLogFileJournal;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.collective2.signalEntry.BasePrice.SessionOpenPlus;

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
                C2EntryServiceAdapter liveAdapter = new Collective2Adapter();
                factory = new C2ServiceFactory(liveAdapter);
            } else {
                // validates commands and returns hard coded (canned) responses
                C2EntryServiceAdapter simulationAdapter = new StaticSimulationAdapter();
                factory = new C2ServiceFactory(simulationAdapter);
            }

        } while (isLive == false);

    }

    @Test
    public void exampleFileJournalFactoryConstructionTest() {

        boolean isLive = true;
        do {
            isLive = !isLive; // set to constant on example page
            C2ServiceFactory factory;

            //optional flat file based journal
            int rollingLogLimit = 1048576; //1Mb for each log file
            File file = new File("/tmp/journalFile.log");
            C2EntryServiceJournal journal = new C2EntryServiceLogFileJournal(file, rollingLogLimit);

            if (isLive) {
                // connects to collective2.com and returns the responses
                C2EntryServiceAdapter liveAdapter = new Collective2Adapter();
                factory = new C2ServiceFactory(liveAdapter, journal);
            } else {
                // validates commands and returns hard coded (canned) responses
                C2EntryServiceAdapter simulationAdapter = new StaticSimulationAdapter();
                factory = new C2ServiceFactory(simulationAdapter, journal);
            }

        } while (isLive == false);

    }

    @Test
    public void exampleServiceConstructionTest() {

        // validates commands and returns hard coded (canned) responses
        C2EntryServiceAdapter simulationAdapter = new StaticSimulationAdapter();
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
        C2EntryServiceAdapter simulationAdapter = new StaticSimulationAdapter();
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

    @Test
    public void exampleAsyncServiceUsageTest() {
        // validates commands and returns hard coded (canned) responses
        C2EntryServiceAdapter simulationAdapter = new StaticSimulationAdapter();
        C2ServiceFactory factory = new C2ServiceFactory(simulationAdapter);

        String password = "PA55WORD";
        Integer systemId = 99999999;
        String eMail = "someone@somewhere.com";
        C2EntryService sentryService = factory.signalEntryService(password, systemId, eMail);

        List<Response> responseList = new ArrayList<Response>();
                                    //TODO: must  update examples on the site

        responseList.add(sentryService.stockSignal(ActionForStock.BuyToOpen)
                                      .marketOrder().quantity(10).symbol("msft")
                                      .duration(Duration.GoodTilCancel).send());

        responseList.add(sentryService.stockSignal(ActionForStock.BuyToOpen)
                                        .limitOrder(new BigDecimal("23.4"))
                                        .quantity(10).symbol("www")
                                        .duration(Duration.GoodTilCancel).send());

        responseList.add(sentryService.stockSignal(ActionForStock.BuyToOpen)
                                        .limitOrder(SessionOpenPlus, new BigDecimal(".50"))
                                         .quantity(10).symbol("ibm")
                                        .duration(Duration.GoodTilCancel).send());

        //after some time...  ask for an arbitrary signal id.

        Integer signalId = responseList.get(2).getInteger(C2Element.ElementSignalId);

    }

}
