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

public class SimulatedResponseSignal extends SimulatedResponse {

    private static final Logger logger = LoggerFactory.getLogger(SimulatedResponseSignal.class);
    private final Integer       signalId;
    private final String        message;

    public SimulatedResponseSignal(Integer signalId, String message) {
        this.signalId = signalId;
        this.message = message;
    }

    @Override
    public void serverSideEventProduction(BlockingQueue<XMLEvent> queue) {
        /*
         * <collective2> <signalid>10344682</signalid> <comments>Order 10344682
         * accepted for immediate processing.</comments> </collective2>
         */
        try {
            queue.put(eventFactory.createStartDocument());
            queue.put(eventFactory.createStartElement("", "", "collective2"));

            queue.put(eventFactory.createStartElement("", "", "signalid"));
            queue.put(eventFactory.createCharacters(signalId.toString()));
            queue.put(eventFactory.createEndElement("", "", "signalid"));

            queue.put(eventFactory.createStartElement("", "", "comments"));
            queue.put(eventFactory.createCharacters(message));
            queue.put(eventFactory.createEndElement("", "", "comments"));

            queue.put(eventFactory.createEndElement("", "", "collective2"));
            queue.put(eventFactory.createEndDocument());
        } catch (InterruptedException e) {
            logger.trace("exit on interruption", e);
        }
    }

    /*
     * //allinone
     * 
     * <collective2> <ack>Order Received</ack> <status>OK</status>
     * <signalid>35584023</signalid> <comments>Order 35584023 accepted for
     * immediate processing.</comments> <oca></oca> <delay></delay>
     * <stoplosssignalid>35584025</stoplosssignalid> </collective2>
     * 
     * 
     * 
     * //xreplace <collective2> <ack>Order Received</ack> <status>OK</status>
     * <signalid>35584086</signalid> Order 35584086 accepted for immediate
     * processing.</comments> <oca></oca> <delay></delay> </collective2>
     */

}
