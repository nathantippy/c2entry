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

public class SimulatedResponseGetBuyPower extends SimulatedResponse {
    private static final Logger logger = LoggerFactory.getLogger(SimulatedResponseGetBuyPower.class);

    private final String        status;
    private final Long          calctime;
    private final Number        buypower;

    public SimulatedResponseGetBuyPower(String status, Long calctime, Number buypower) {
        this.status = status;
        this.calctime = calctime;
        this.buypower = buypower;
    }

    @Override
    public void serverSideEventProduction(BlockingQueue<XMLEvent> queue) {
        /*
         * <collective2> <status>OK</status> <calctime>1136058468</calctime>
         * <buypower>68300.00</buypower> </collective2>
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

            queue.put(eventFactory.createStartElement("", "", "buypower"));
            queue.put(eventFactory.createCharacters(buypower.toString()));
            queue.put(eventFactory.createEndElement("", "", "buypower"));

            queue.put(eventFactory.createEndElement("", "", "collective2"));
            queue.put(eventFactory.createEndDocument());
        } catch (InterruptedException e) {
            logger.trace("exit on interruption", e);
        }

    }
}
