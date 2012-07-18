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

public class SimulatedResponseAllSystems extends SimulatedResponse {


    private static final Logger logger = LoggerFactory.getLogger(SimulatedResponseAddToOCAGroup.class);
    private final String status;
    private final Integer[] systems;
    private final String comment;


    public SimulatedResponseAllSystems(String status, String comment, Integer ... systems) {
        this.status = status;
        this.systems = systems;
        this.comment = comment;
    }

    @Override
    public void serverSideEventProduction(BlockingQueue<XMLEvent> queue) {
        /*
        <collective2>
            <status>error</status>
            <errortype>No systems owned</errortype>
            <comment></comment>
        </collective2>
        */

        try {
            queue.put(eventFactory.createStartDocument());
            queue.put(eventFactory.createStartElement("", "", "collective2"));

            queue.put(eventFactory.createStartElement("", "", "status"));
            queue.put(eventFactory.createCharacters(status));
            queue.put(eventFactory.createEndElement("", "", "status"));

            if (systems.length==0) {
                queue.put(eventFactory.createStartElement("", "", "errortype"));
                queue.put(eventFactory.createCharacters("No systems owned"));
                queue.put(eventFactory.createEndElement("", "", "errortype"));
            } else {
                queue.put(eventFactory.createStartElement("", "", "systemsowned"));
                for(Integer system:systems) {
                    queue.put(eventFactory.createStartElement("", "", "system"));
                    
                    queue.put(eventFactory.createStartElement("", "", "systemid"));
                    queue.put(eventFactory.createCharacters(system.toString()));
                    queue.put(eventFactory.createEndElement("", "", "systemid"));

                    queue.put(eventFactory.createStartElement("", "", "systemname"));
                    queue.put(eventFactory.createCharacters("nameOF"+system));
                    queue.put(eventFactory.createEndElement("", "", "systemname"));
                    
                    queue.put(eventFactory.createEndElement("", "", "system"));
                }
                queue.put(eventFactory.createEndElement("", "", "systemsowned"));
                
            }
            queue.put(eventFactory.createStartElement("", "", "comment"));
            queue.put(eventFactory.createCharacters(comment));
            queue.put(eventFactory.createEndElement("", "", "comment"));

            queue.put(eventFactory.createEndElement("", "", "collective2"));
            queue.put(eventFactory.createEndDocument());
        } catch (InterruptedException e) {
            logger.trace("exit on interruption",e);
        }


    }
}
