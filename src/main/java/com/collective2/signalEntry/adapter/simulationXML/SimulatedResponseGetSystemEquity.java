/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/10/12
 */
package com.collective2.signalEntry.adapter.simulationXML;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import javax.xml.stream.events.XMLEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimulatedResponseGetSystemEquity extends SimulatedResponse {

    private static final Logger logger = LoggerFactory.getLogger(SimulatedResponseGetSystemEquity.class);

    
    public SimulatedResponseGetSystemEquity(String status, Long calctime, Number systemEquity) {
        super(buildEvents(status,calctime,systemEquity));
    }

    private static Iterator<XMLEvent> buildEvents(String status, Long calctime, Number systemEquity) {
        /*
        <collective2>
                <status>OK</status>
                <calctime>1342299909</calctime>
                <systemEquity>8755.68</systemEquity>
        </collective2>
        */
        List<XMLEvent> queue = new ArrayList<XMLEvent>();
            queue.add(eventFactory.createStartDocument());
            queue.add(eventFactory.createStartElement("", "", "collective2"));

            queue.add(eventFactory.createStartElement("", "", "status"));
            queue.add(eventFactory.createCharacters(status));
            queue.add(eventFactory.createEndElement("", "", "status"));

            queue.add(eventFactory.createStartElement("", "", "calctime"));
            queue.add(eventFactory.createCharacters(calctime.toString()));
            queue.add(eventFactory.createEndElement("", "", "calctime"));

            queue.add(eventFactory.createStartElement("", "", "systemEquity"));
            queue.add(eventFactory.createCharacters(systemEquity.toString()));
            queue.add(eventFactory.createEndElement("", "", "systemEquity"));

            queue.add(eventFactory.createEndElement("", "", "collective2"));
            queue.add(eventFactory.createEndDocument());
        return queue.iterator();
    }
}
