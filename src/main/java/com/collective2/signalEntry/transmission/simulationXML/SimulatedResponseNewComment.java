/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/15/12
 */
package com.collective2.signalEntry.transmission.simulationXML;

import java.util.concurrent.BlockingQueue;

import javax.xml.stream.events.XMLEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This notice shall not be removed. See the "LICENSE.txt" file found in the
 * root folder for the full license governing this code. Nathan Tippy 7/10/12
 */
public class SimulatedResponseNewComment extends SimulatedResponse {
    private static final Logger logger = LoggerFactory.getLogger(SimulatedResponseNewComment.class);

    private final String        status;
    private final Integer       signalid;
    private final String        previousComment;

    public SimulatedResponseNewComment(String status, Integer signalid, String previousComment) {
        this.status = status;
        this.signalid = signalid;
        this.previousComment = previousComment;
    }

    @Override
    public void serverSideEventProduction(BlockingQueue<XMLEvent> queue) {
        /*
         * <collective2> <status>OK: Signal 29148580 comment created</status>
         * <signalid> 29148580</signalid> <previousComment></previousComment>
         * </collective2>
         */
        try {
            queue.put(eventFactory.createStartDocument());
            queue.put(eventFactory.createStartElement("", "", "collective2"));

            queue.put(eventFactory.createStartElement("", "", "status"));
            queue.put(eventFactory.createCharacters(status));
            queue.put(eventFactory.createEndElement("", "", "status"));

            queue.put(eventFactory.createStartElement("", "", "signalid"));
            queue.put(eventFactory.createCharacters(signalid.toString()));
            queue.put(eventFactory.createEndElement("", "", "signalid"));

            queue.put(eventFactory.createStartElement("", "", "previousComment"));
            queue.put(eventFactory.createCharacters(previousComment));
            queue.put(eventFactory.createEndElement("", "", "previousComment"));

            queue.put(eventFactory.createEndElement("", "", "collective2"));
            queue.put(eventFactory.createEndDocument());
        } catch (InterruptedException e) {
            logger.trace("exit on interruption", e);
        }

    }
}
