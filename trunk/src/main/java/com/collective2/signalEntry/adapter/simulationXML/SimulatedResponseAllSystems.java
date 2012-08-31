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

public class SimulatedResponseAllSystems extends SimulatedResponse {


    private static final Logger logger = LoggerFactory.getLogger(SimulatedResponseAddToOCAGroup.class);


    public SimulatedResponseAllSystems(String status, String comment, Integer ... systems) {
        super(buildEvents(status,comment,systems));
    }

    private static Iterator<XMLEvent> buildEvents(String status, String comment, Integer[] systems) {
        /*
        <collective2>
            <status>error</status>
            <errortype>No systems owned</errortype>
            <comment></comment>
        </collective2>
        */
        List<XMLEvent> queue = new ArrayList<XMLEvent>();

            queue.add(eventFactory.createStartDocument());
            queue.add(eventFactory.createStartElement("", "", "collective2"));

            queue.add(eventFactory.createStartElement("", "", "status"));
            queue.add(eventFactory.createCharacters(status));
            queue.add(eventFactory.createEndElement("", "", "status"));

            if (systems.length==0) {
                queue.add(eventFactory.createStartElement("", "", "errortype"));
                queue.add(eventFactory.createCharacters("No systems owned"));
                queue.add(eventFactory.createEndElement("", "", "errortype"));
            } else {
                queue.add(eventFactory.createStartElement("", "", "systemsowned"));
                for(Integer system:systems) {
                    queue.add(eventFactory.createStartElement("", "", "system"));
                    
                    queue.add(eventFactory.createStartElement("", "", "systemid"));
                    queue.add(eventFactory.createCharacters(system.toString()));
                    queue.add(eventFactory.createEndElement("", "", "systemid"));

                    queue.add(eventFactory.createStartElement("", "", "systemname"));
                    queue.add(eventFactory.createCharacters("nameOF"+system));
                    queue.add(eventFactory.createEndElement("", "", "systemname"));
                    
                    queue.add(eventFactory.createEndElement("", "", "system"));
                }
                queue.add(eventFactory.createEndElement("", "", "systemsowned"));
                
            }
            queue.add(eventFactory.createStartElement("", "", "comment"));
            queue.add(eventFactory.createCharacters(comment));
            queue.add(eventFactory.createEndElement("", "", "comment"));

            queue.add(eventFactory.createEndElement("", "", "collective2"));
            queue.add(eventFactory.createEndDocument());

          return queue.iterator();


    }
}
