/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/21/12
 */
package com.collective2.signalEntry;

import com.collective2.signalEntry.adapter.BackEndAdapter;
import com.collective2.signalEntry.adapter.StaticSimulationAdapter;
import com.collective2.signalEntry.implementation.Request;
import org.junit.Test;

import javax.xml.stream.XMLEventReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class AsyncTest {

    @Test
    public void exampleServiceUsageTest() {

        BackEndAdapter simulationAdapter = new StaticSimulationAdapter() {
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
        String eMail = "someone@somewhere.com";
        C2EntryService sentryService = factory.signalEntryService(password, systemId, eMail);

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

        //all 10 should still be transmitted in order, checked by the adapter above.

    }

    //NOTE: add persist test and network down retry test

}
