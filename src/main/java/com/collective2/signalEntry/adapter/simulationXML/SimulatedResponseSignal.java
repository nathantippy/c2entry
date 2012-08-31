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

import com.collective2.signalEntry.C2Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimulatedResponseSignal extends SimulatedResponse {

    private static final Logger logger = LoggerFactory.getLogger(SimulatedResponseSignal.class);

    public SimulatedResponseSignal(Integer signalId, String message) {
        super(buildEvents(signalId,null,null,message));
    }

    public SimulatedResponseSignal(Integer signalId, Integer stopLossSignalId, Integer profitTaretSignalId, String message) {
        super(buildEvents(signalId,stopLossSignalId,profitTaretSignalId,message));
    }

    private static Iterator<XMLEvent> buildEvents(Integer signalId, Integer stopLossSignalId, Integer profitTaretSignalId, String message) {
        /*
         * <collective2> <signalid>10344682</signalid> <comments>Order 10344682
         * accepted for immediate processing.</comments> </collective2>
         */
        List<XMLEvent> queue = new ArrayList<XMLEvent>();
            queue.add(eventFactory.createStartDocument());
            queue.add(eventFactory.createStartElement("", "", "collective2"));

            queue.add(eventFactory.createStartElement("", "", C2Element.ElementSignalId.localElementName()));
            queue.add(eventFactory.createCharacters(signalId.toString()));
            queue.add(eventFactory.createEndElement("", "", C2Element.ElementSignalId.localElementName()));

            if (stopLossSignalId != null) {
                queue.add(eventFactory.createStartElement("", "", C2Element.ElementStopLossSignalId.localElementName()));
                queue.add(eventFactory.createCharacters(stopLossSignalId.toString()));
                queue.add(eventFactory.createEndElement("", "", C2Element.ElementStopLossSignalId.localElementName()));
            }

            if (profitTaretSignalId != null) {
                queue.add(eventFactory.createStartElement("", "", C2Element.ElementProfitTaretSignalId.localElementName()));
                queue.add(eventFactory.createCharacters(profitTaretSignalId.toString()));
                queue.add(eventFactory.createEndElement("", "", C2Element.ElementProfitTaretSignalId.localElementName()));
            }

            queue.add(eventFactory.createStartElement("", "", "comments"));
            queue.add(eventFactory.createCharacters(message));
            queue.add(eventFactory.createEndElement("", "", "comments"));

            queue.add(eventFactory.createEndElement("", "", "collective2"));
            queue.add(eventFactory.createEndDocument());
        return queue.iterator();
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
