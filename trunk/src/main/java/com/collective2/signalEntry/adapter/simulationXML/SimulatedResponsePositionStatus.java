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

public class SimulatedResponsePositionStatus extends SimulatedResponse {
	private static final Logger logger = LoggerFactory.getLogger(SimulatedResponsePositionStatus.class);

    private final String status;
    private final String calctime;
    private final String symbol;
    private final Integer position;


    public SimulatedResponsePositionStatus(String status, String calctime, String symbol, Integer position) {
        this.status = status;
        this.calctime = calctime;
        this.symbol = symbol;
        this.position = position;


    }

    @Override
    public void serverSideEventProduction(BlockingQueue<XMLEvent> queue) {
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
    	try {

        queue.put(eventFactory.createStartDocument());
        queue.put(eventFactory.createStartElement("", "", "collective2"));

        queue.put(eventFactory.createStartElement("", "", "status"));
        queue.put(eventFactory.createCharacters(status));
        queue.put(eventFactory.createEndElement("", "", "status"));

        queue.put(eventFactory.createStartElement("", "", "positionstatus"));
            queue.put(eventFactory.createStartElement("", "", "calctime"));
            queue.put(eventFactory.createCharacters(calctime));
            queue.put(eventFactory.createEndElement("", "", "calctime"));

            queue.put(eventFactory.createStartElement("", "", "symbol"));
            queue.put(eventFactory.createCharacters(symbol));
            queue.put(eventFactory.createEndElement("", "", "symbol"));

            queue.put(eventFactory.createStartElement("", "", "position"));
            queue.put(eventFactory.createCharacters(position.toString()));
            queue.put(eventFactory.createEndElement("", "", "position"));

        queue.put(eventFactory.createEndElement("", "", "positionstatus"));

        queue.put(eventFactory.createEndElement("", "", "collective2"));
        queue.put(eventFactory.createEndDocument());

        } catch (InterruptedException e) {
            logger.trace("exit on interruption",e);
        }


    }
}
