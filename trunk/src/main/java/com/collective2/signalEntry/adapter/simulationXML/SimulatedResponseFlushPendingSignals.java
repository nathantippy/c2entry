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

public class SimulatedResponseFlushPendingSignals extends SimulatedResponse {
	private static final Logger logger = LoggerFactory.getLogger(SimulatedResponseFlushPendingSignals.class);
    private final String status;

    public SimulatedResponseFlushPendingSignals(String status) {
        this.status = status;
    }

    @Override
    public void serverSideEventProduction(BlockingQueue<XMLEvent> queue) {
    	try{
	        queue.put(eventFactory.createStartDocument());
	        queue.put(eventFactory.createStartElement("", "", "collective2"));
	
	        queue.put(eventFactory.createStartElement("", "", "status"));
	        queue.put(eventFactory.createCharacters(status));
	        queue.put(eventFactory.createEndElement("", "", "status"));
	
	        queue.put(eventFactory.createEndElement("", "", "collective2"));
	        queue.put(eventFactory.createEndDocument());
        } catch (InterruptedException e) {
            logger.trace("exit on interruption",e);
        }
    }
}
