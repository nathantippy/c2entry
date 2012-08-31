/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/15/12
 */
package com.collective2.signalEntry.adapter.simulationXML;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import javax.xml.stream.events.XMLEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This notice shall not be removed. See the "LICENSE.txt" file found in the
 * root folder for the full license governing this code. Nathan Tippy 7/10/12
 */
public class SimulatedResponseSendSubscriberBroadcast extends SimulatedResponse {
    private static final Logger logger = LoggerFactory.getLogger(SimulatedResponseSendSubscriberBroadcast.class);

    public SimulatedResponseSendSubscriberBroadcast(String status) {
        super(buildEvents(status));
    }

    private static Iterator<XMLEvent> buildEvents(String status) {
        List<XMLEvent> queue = new ArrayList<XMLEvent>();
            queue.add(eventFactory.createStartDocument());
            queue.add(eventFactory.createStartElement("", "", "collective2"));

            queue.add(eventFactory.createStartElement("", "", "status"));
            queue.add(eventFactory.createCharacters(status));
            queue.add(eventFactory.createEndElement("", "", "status"));

            queue.add(eventFactory.createEndElement("", "", "collective2"));
            queue.add(eventFactory.createEndDocument());
        return queue.iterator();
    }
}
