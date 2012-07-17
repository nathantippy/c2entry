/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/10/12
 */
package com.collective2.signalEntry.transmission.simulationXML;

import java.util.concurrent.BlockingQueue;

import javax.xml.stream.events.XMLEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimulatedResponseRequestOCAId extends SimulatedResponse {
    private static final Logger logger = LoggerFactory.getLogger(SimulatedResponsePositionStatus.class);

    private final Integer       ocaid;
    private final String        status;

    public SimulatedResponseRequestOCAId(Integer ocaid, String status) {
        this.ocaid = ocaid;
        this.status = status;
    }

    @Override
    public void serverSideEventProduction(BlockingQueue<XMLEvent> queue) {
        /*
         * <collective2> <ocaid>17195788</ocaid> <status>You may use the ocaid
         * above when adding new signals.</status> </collective2>
         */
        try {
            queue.put(eventFactory.createStartDocument());
            queue.put(eventFactory.createStartElement("", "", "collective2"));

            queue.put(eventFactory.createStartElement("", "", "ocaid"));
            queue.put(eventFactory.createCharacters(ocaid.toString()));
            queue.put(eventFactory.createEndElement("", "", "ocaid"));

            queue.put(eventFactory.createStartElement("", "", "status"));
            queue.put(eventFactory.createCharacters(status));
            queue.put(eventFactory.createEndElement("", "", "status"));

            queue.put(eventFactory.createEndElement("", "", "collective2"));
            queue.put(eventFactory.createEndDocument());
        } catch (InterruptedException e) {
            logger.trace("exit on interruption", e);
        }
        // To change body of implemented methods use File | Settings | File
        // Templates.
    }
}
