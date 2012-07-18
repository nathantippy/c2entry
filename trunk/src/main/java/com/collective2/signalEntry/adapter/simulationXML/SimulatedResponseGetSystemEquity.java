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

public class SimulatedResponseGetSystemEquity extends SimulatedResponse {

    private static final Logger logger = LoggerFactory.getLogger(SimulatedResponseGetSystemEquity.class);
    
    private final String status;
    private final Long calctime;
    private final Number systemEquity;
    
    public SimulatedResponseGetSystemEquity(String status, Long calctime, Number systemEquity) {
        this.status = status;
        this.calctime = calctime;
        this.systemEquity = systemEquity;
    }

    @Override
    public void serverSideEventProduction(BlockingQueue<XMLEvent> queue) {
        /*
        <collective2>
                <status>OK</status>
                <calctime>1342299909</calctime>
                <systemEquity>8755.68</systemEquity>
        </collective2>
        */
        try {
            queue.put(eventFactory.createStartDocument());
            queue.put(eventFactory.createStartElement("", "", "collective2"));

            queue.put(eventFactory.createStartElement("", "", "status"));
            queue.put(eventFactory.createCharacters(status));
            queue.put(eventFactory.createEndElement("", "", "status"));

            queue.put(eventFactory.createStartElement("", "", "calctime"));
            queue.put(eventFactory.createCharacters(calctime.toString()));
            queue.put(eventFactory.createEndElement("", "", "calctime"));

            queue.put(eventFactory.createStartElement("", "", "systemEquity"));
            queue.put(eventFactory.createCharacters(systemEquity.toString()));
            queue.put(eventFactory.createEndElement("", "", "systemEquity"));

            queue.put(eventFactory.createEndElement("", "", "collective2"));
            queue.put(eventFactory.createEndDocument());
        } catch (InterruptedException e) {
            logger.trace("exit on interruption", e);
        }
    }
}
