/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/15/12
 */
package com.collective2.signalEntry;

import com.collective2.signalEntry.adapter.C2EntryServiceAdapter;
import com.collective2.signalEntry.adapter.Collective2Adapter;
import com.collective2.signalEntry.adapter.DynamicSimulationAdapter;
import com.collective2.signalEntry.adapter.StaticSimulationAdapter;
import com.collective2.signalEntry.adapter.dynamicSimulator.DataProvider;
import com.collective2.signalEntry.adapter.dynamicSimulator.SimpleGainListener;
import com.collective2.signalEntry.approval.ApprovalRequestableConsole;
import com.collective2.signalEntry.approval.C2EntryHumanApproval;
import com.collective2.signalEntry.journal.C2EntryServiceJournal;
import com.collective2.signalEntry.journal.C2EntryServiceLogFileJournal;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.collective2.signalEntry.BasePrice.SessionOpenPlus;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

//TODO: must  update examples on the site

public class ExamplesTest {

    // these are the examples found on the project home page
    // make sure that anything given as an example really does work.

    @Test
    public void exampleLiveFactoryConstructionTest() {
        String password = "password";
        Integer systemId = 42;

        // simplest possible connection to collective2.com
        try {
            C2EntryServiceAdapter liveAdapter = new Collective2Adapter();
            C2ServiceFactory factory = new C2ServiceFactory(liveAdapter);
            C2EntryService entryService = factory.signalEntryService(password,systemId);
            assertNotNull(entryService);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void exampleLiveFileJournalFactoryConstructionTest() {
        //optional flat file based journal
        int rollingLogLimit = 1048576; //1Mb for each log file
        File file = new File("/tmp/journalFile.log");

        String password = "password";
        Integer systemId = 42;

        // journal connection to collective2.com
        // saves the requests to disk in case of unexpected shutdown while
        // retrying to transmit.
        try {
            C2EntryServiceJournal journal = new C2EntryServiceLogFileJournal(file, rollingLogLimit);

            C2EntryServiceAdapter liveAdapter = new Collective2Adapter();
            C2ServiceFactory factory = new C2ServiceFactory(liveAdapter, journal);
            C2EntryService entryService = factory.signalEntryService(password,systemId);
            assertNotNull(entryService);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        //do not do this in production but for the test we must not leave clutter behind.
        file.deleteOnExit();
    }

    @Test
    public void exampleLiveHumanApprovalFactoryConstructionTest() {
        String password = "password";
        Integer systemId = 42;

        // after .send() is called on signals the user will be prompted at the
        // console prompt for approval before transmission. The included class is just
        // an example and can be implemented with other GUI technologies.
        try {
            C2EntryServiceAdapter liveAdapter = new Collective2Adapter();
            C2EntryHumanApproval approval = new ApprovalRequestableConsole();
            C2ServiceFactory factory = new C2ServiceFactory(liveAdapter,approval);
            C2EntryService entryService = factory.signalEntryService(password,systemId);
            assertNotNull(entryService);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }


    @Test
    public void exampleServiceConstructionTest() {

        // validates commands and returns hard coded (canned) responses
        // for live production replace StaticSimulationAdapter with Collective2Adapter
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
        // for live production replace StaticSimulationAdapter with Collective2Adapter
        C2EntryServiceAdapter simulationAdapter = new StaticSimulationAdapter();
        C2ServiceFactory factory = new C2ServiceFactory(simulationAdapter);

        String password = "PA55WORD";
        Integer systemId = 99999999;
        String eMail = "someone@somewhere.com";
        C2EntryService sentryService = factory.signalEntryService(password, systemId, eMail);

        Response response = sentryService.stockSignal(ActionForStock.BuyToOpen)
                            .marketOrder().quantity(10).symbol("MSFT")
                            .duration(Duration.GoodTilCancel).send();
        Integer signalId = response.getInteger(C2Element.ElementSignalId);

        //each operator in the signal chain returns an immutable signal that can be used again
        //so composition patters can be used like this

        Signal base = sentryService.stockSignal(ActionForStock.BuyToOpen)
                                   .duration(Duration.DayOrder)
                                   .marketOrder();

        //base holds all the attributes these orders share in common.

        base.symbol("IBM").quantity(101).send();
        base.symbol("WWW").quantity(50).send();
    }

    @Test
    public void exampleAsyncServiceUsageTest() {
        // validates commands and returns hard coded (canned) responses
        // for live production replace StaticSimulationAdapter with Collective2Adapter
        C2EntryServiceAdapter simulationAdapter = new StaticSimulationAdapter();
        C2ServiceFactory factory = new C2ServiceFactory(simulationAdapter);

        String password = "PA55WORD";
        Integer systemId = 99999999;
        String eMail = "someone@somewhere.com";
        C2EntryService sentryService = factory.signalEntryService(password, systemId, eMail);

        List<Response> responseList = new ArrayList<Response>();


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

    @Test
    public void exampleBackTestingFactoryConstructionTest() {
        String password = "password";

        boolean marginAccount = true;//C2 always simulates margin

        BigDecimal startingBuyPower = new BigDecimal("10000");
        String mySystemName = "testSystem";
        BigDecimal commission = new BigDecimal("10");

        // simplest possible back test simulator setup
        try {
            DynamicSimulationAdapter adapter = new DynamicSimulationAdapter(marginAccount);

            //register our system and get our id from the simulator
            Integer systemId = adapter.createSystem(startingBuyPower, mySystemName, password, commission);

            C2ServiceFactory factory = new C2ServiceFactory(adapter);
            C2EntryService entryService = factory.signalEntryService(password,systemId);
            assertNotNull(entryService);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void exampleBackTestingUsageTest() {
        String password = "password";

        boolean marginAccount = true;//C2 always simulates margin

        BigDecimal startingBuyPower = new BigDecimal("10000");
        String mySystemName = "testSystem";
        BigDecimal commission = new BigDecimal("10");

        Iterator<DataProvider> dataIterator = myDataProvider();

        // simplest possible back test simulator setup
        try {
            DynamicSimulationAdapter adapter = new DynamicSimulationAdapter(marginAccount);

            //as the simulation progresses write a summary of the gains to system out.
            long startTimeMs = 0; //do not report gain before this time has passed.
            long periodMs = 60000*60*24*10;//print gains once every 10 days
            adapter.addGainListener(startTimeMs, periodMs, new SimpleGainListener(System.out));

            //register our system and get our id from the simulator
            Integer systemId = adapter.createSystem(startingBuyPower, mySystemName, password, commission);

            C2ServiceFactory factory = new C2ServiceFactory(adapter);
            C2EntryService entryService = factory.signalEntryService(password,systemId);

            while (dataIterator.hasNext()) {

                //get data provider for the next time period
                DataProvider data = dataIterator.next();

                //first, send the data for this time period to the simulator
                adapter.tick(data, entryService); //multiple ticks can be sent for finer granularity

                //second use that data for building up your own signals
                mySignalGenerator(data, entryService);

            }

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    private void mySignalGenerator(DataProvider dataProvider, C2EntryService entryService) {

        //use dataProvider and/or other stuff to generate your trade signals

        entryService.stockSignal(ActionForStock.BuyToOpen)
                .marketOrder().quantity(10).symbol("msft")
                .duration(Duration.DayOrder).send();

    }

    private Iterator<DataProvider> myDataProvider() {

        //must provide you own sequence of DataProvider objects from your source data.

        return Collections.emptyIterator();
    }

}
