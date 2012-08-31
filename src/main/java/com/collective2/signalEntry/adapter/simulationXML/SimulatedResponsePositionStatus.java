/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/10/12
 */
package com.collective2.signalEntry.adapter.simulationXML;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import javax.xml.stream.events.XMLEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimulatedResponsePositionStatus extends SimulatedResponse {
	private static final Logger logger = LoggerFactory.getLogger(SimulatedResponsePositionStatus.class);

    public SimulatedResponsePositionStatus(String status, Long calctime, String symbol, Integer position) {
        super(buildEvents(status,calctime,symbol,position));
    }

    private static Iterator<XMLEvent> buildEvents(String status, Long calctime, String symbol, Integer position) {
        /*
        <collective2>
            <status>OK</status>
            <positionstatus>
                            <calctime>2006-09-11 10:40:35:000</calctime>
                            <symbol>EURUSD</symbol>
                            <position>4</position>
            </positionstatus>
        </collective2>
        */
        List<XMLEvent> queue = new ArrayList<XMLEvent>();

        queue.add(eventFactory.createStartDocument());
        queue.add(eventFactory.createStartElement("", "", "collective2"));

        queue.add(eventFactory.createStartElement("", "", "status"));
        queue.add(eventFactory.createCharacters(status));
        queue.add(eventFactory.createEndElement("", "", "status"));

        queue.add(eventFactory.createStartElement("", "", "positionstatus"));
            queue.add(eventFactory.createStartElement("", "", "calctime"));
            queue.add(eventFactory.createCharacters(new Date(calctime).toString()));
            queue.add(eventFactory.createEndElement("", "", "calctime"));

            queue.add(eventFactory.createStartElement("", "", "symbol"));
            queue.add(eventFactory.createCharacters(symbol));
            queue.add(eventFactory.createEndElement("", "", "symbol"));

            queue.add(eventFactory.createStartElement("", "", "position"));
            queue.add(eventFactory.createCharacters(position.toString()));
            queue.add(eventFactory.createEndElement("", "", "position"));

        queue.add(eventFactory.createEndElement("", "", "positionstatus"));

        queue.add(eventFactory.createEndElement("", "", "collective2"));
        queue.add(eventFactory.createEndDocument());

        return queue.iterator();

    }
}
