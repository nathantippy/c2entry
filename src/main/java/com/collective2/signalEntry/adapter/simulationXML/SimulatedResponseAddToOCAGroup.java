/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/10/12
 */
package com.collective2.signalEntry.adapter.simulationXML;

import java.util.concurrent.BlockingQueue;

import javax.xml.stream.events.XMLEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimulatedResponseAddToOCAGroup extends SimulatedResponse {

    private static final Logger logger = LoggerFactory.getLogger(SimulatedResponseAddToOCAGroup.class);
    private final String status;
    private final String details;

    public SimulatedResponseAddToOCAGroup(String status, String details) {
        this.status = status;
        this.details = details;
    }

    @Override
    public void serverSideEventProduction(BlockingQueue<XMLEvent> queue) {
        /*
        <collective2>
            <status>OK</status>
            <details>Signal 12345 now added to ocagroup 9876</details>
        </collective2>
         */
        try {
            queue.put(eventFactory.createStartDocument());
            queue.put(eventFactory.createStartElement("", "", "collective2"));

            queue.put(eventFactory.createStartElement("", "", "status"));
            queue.put(eventFactory.createCharacters(status));
            queue.put(eventFactory.createEndElement("", "", "status"));

            queue.put(eventFactory.createStartElement("", "", "details"));
            queue.put(eventFactory.createCharacters(details));
            queue.put(eventFactory.createEndElement("", "", "details"));

            queue.put(eventFactory.createEndElement("", "", "collective2"));
            queue.put(eventFactory.createEndDocument());
        } catch (InterruptedException e) {
            logger.trace("exit on interruption",e);
        }
    }
}
