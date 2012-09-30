/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/10/12
 */
package com.collective2.signalEntry.adapter.simulationXML;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SimulatedResponseError extends SimulatedResponse {
    private static final Logger logger = LoggerFactory.getLogger(SimulatedResponseError.class);

    public SimulatedResponseError(String status, String errortype, String comment) {
        super(buildEvents(status,errortype,comment));
    }

    private static Iterator<XMLEvent> buildEvents(String status, String errortype, String comment) {

        List<XMLEvent> queue = new ArrayList<XMLEvent>();
        queue.add(eventFactory.createStartDocument());
        queue.add(eventFactory.createStartElement("", "", "collective2"));

        queue.add(eventFactory.createStartElement("", "", "status"));
        queue.add(eventFactory.createCharacters(status));
        queue.add(eventFactory.createEndElement("", "", "status"));

        queue.add(eventFactory.createStartElement("", "", "errortype"));
        queue.add(eventFactory.createCharacters(errortype));
        queue.add(eventFactory.createEndElement("", "", "errortype"));

        queue.add(eventFactory.createStartElement("", "", "comment"));
        queue.add(eventFactory.createCharacters(comment));
        queue.add(eventFactory.createEndElement("", "", "comment"));

        queue.add(eventFactory.createEndElement("", "", "collective2"));
        queue.add(eventFactory.createEndDocument());

        return queue.iterator();

    }
}
