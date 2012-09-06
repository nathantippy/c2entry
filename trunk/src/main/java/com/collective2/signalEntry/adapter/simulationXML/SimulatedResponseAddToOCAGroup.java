/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/10/12
 */
package com.collective2.signalEntry.adapter.simulationXML;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SimulatedResponseAddToOCAGroup extends SimulatedResponse {

    private static final Logger logger = LoggerFactory.getLogger(SimulatedResponseAddToOCAGroup.class);

    public SimulatedResponseAddToOCAGroup(String status, String details) {
        super(buildEvents(status,details));
    }

    private static Iterator<XMLEvent> buildEvents(String status, String details) {
        /*
        <collective2>
            <status>OK</status>
            <details>Order 12345 now added to ocagroup 9876</details>
        </collective2>
         */
        List<XMLEvent> queue = new ArrayList<XMLEvent>(10);
        queue.add(eventFactory.createStartDocument());
        queue.add(eventFactory.createStartElement("", "", "collective2"));

        queue.add(eventFactory.createStartElement("", "", "status"));
        queue.add(eventFactory.createCharacters(status));
        queue.add(eventFactory.createEndElement("", "", "status"));

        queue.add(eventFactory.createStartElement("", "", "details"));
        queue.add(eventFactory.createCharacters(details));
        queue.add(eventFactory.createEndElement("", "", "details"));

        queue.add(eventFactory.createEndElement("", "", "collective2"));
        queue.add(eventFactory.createEndDocument());
        return queue.iterator();
    }
}
