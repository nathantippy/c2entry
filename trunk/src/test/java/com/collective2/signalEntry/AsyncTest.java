/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/21/12
 */
package com.collective2.signalEntry;

import com.collective2.signalEntry.adapter.C2EntryServiceAdapter;
import com.collective2.signalEntry.adapter.StaticSimulationAdapter;
import com.collective2.signalEntry.implementation.ImplResponse;
import com.collective2.signalEntry.implementation.Request;
import com.collective2.signalEntry.journal.C2EntryServiceJournal;
import com.collective2.signalEntry.journal.C2EntryServiceMemoryJournal;
import org.junit.Test;

import javax.xml.stream.XMLEventReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.Assert.*;

public class AsyncTest {

    @Test
    public void exampleAsyncTest() {

        C2EntryServiceAdapter simulationAdapter = new StaticSimulationAdapter() {
            String lastValue = "";

            @Override
            public XMLEventReader transmit(Request request) {
                //each symbol must be greater than the last to ensure the right order
                assertTrue(((String)request.get(Parameter.Symbol)).compareTo(lastValue)>0);
                //setup for next test
                lastValue = (String)request.get(Parameter.Symbol);

                return super.transmit(request);
            }
        };
        C2ServiceFactory factory = new C2ServiceFactory(simulationAdapter);

        String password = "PA55WORD";
        Integer systemId = 99999999;

        C2EntryService sentryService = factory.signalEntryService(password, systemId);

        List<Response> responseList = new ArrayList<Response>();

        int i = -1;
        while(++i<10) {

            Response response = sentryService.stockSignal(ActionForStock.BuyToOpen)
                    .marketOrder().quantity(10).symbol("msft"+i)
                    .duration(Duration.GoodTilCancel).send();

            responseList.add(response);
        }
        //ask about the last one
        Integer signalId9 = responseList.get(9).getInteger(C2Element.ElementSignalId);
        //ask about one in the middle
        Integer signalId5 = responseList.get(5).getInteger(C2Element.ElementSignalId);

        //all 10 are still transmitted in order, checked by the adapter above.

    }

    @Test
    public void exampleOffLinePersistTest() {

        final ReentrantLock lock = new ReentrantLock();

        C2EntryServiceAdapter simulationAdapter = new StaticSimulationAdapter() {
            String lastValue = "";

            @Override
            public XMLEventReader transmit(Request request) {
                //do not allow transmit until we confirm everything got saved
               // lock.lock();
                if (lock.tryLock()) {
                    try {
                        return super.transmit(request);
                    } finally {
                        lock.unlock();;
                    }
               } else {
                    throw new C2ServiceException("simulated timeout, the internet is down.",true);
               }
            }
        };

        C2EntryServiceJournal journal = new C2EntryServiceMemoryJournal() {

            @Override
            public Iterator<Request> pending() {
                return super.pending();
            }

            @Override
            public void append(Request request) {
                assertEquals("*****",request.get(Parameter.Password));

                super.append(request);
            }

            @Override
            public void markSent(Request request) {

                Request oldest = super.pending().next();
                assertEquals("\n"+request+"\n"+oldest ,
                                  request,     oldest);

                super.markSent(request);
            }

            @Override
            public void dropPending() {
                super.dropPending();
            }
        };

        C2ServiceFactory factory = new C2ServiceFactory(simulationAdapter, journal);

        String password = "PA55WORD";
        Integer systemId = 99999999;
        String eMail = "someone@somewhere.com";
        C2EntryService sentryService = factory.signalEntryService(password, systemId, eMail);

        lock.lock(); //prevent transmit

        ImplResponse allSys = (ImplResponse)sentryService.sendAllSystemsRequest();
        ImplResponse allSig = (ImplResponse)sentryService.sendAllSignalsRequest();

        //check that everything got saved
        Iterator<Request> pending = journal.pending();
        assertTrue(pending.hasNext());
        assertEquals(allSys.secureRequest(),pending.next());
        assertTrue(pending.hasNext());
        assertEquals(allSig.secureRequest().secureClone(), pending.next());
        assertFalse(pending.hasNext());

        //Now dump and rebuild factory from scratch so its forced to build response list.
        //sentryService.awaitPending();//.shutdown();//must stop old daemon before starting new one

        lock.unlock();


        factory = new C2ServiceFactory(simulationAdapter, journal);
        sentryService = factory.signalEntryService(password, systemId, eMail);

        sentryService.awaitPending();

        assertFalse(journal.pending().hasNext());
    }
}
