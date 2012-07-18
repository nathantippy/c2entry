/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/10/12
 */
package com.collective2.signalEntry.adapter.simulationXML;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import javax.xml.stream.events.XMLEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimulatedResponseGetAllSignals extends SimulatedResponse {
	private static final Logger logger = LoggerFactory.getLogger(SimulatedResponseGetAllSignals.class);

    private final String status;
    private final Map<Integer, List<Integer>> allPendingSignals;


    public SimulatedResponseGetAllSignals(String status, Map<Integer, List<Integer>> allPendingSignals) {
        this.status = status;
        this.allPendingSignals = allPendingSignals;
    }


    @Override
    public void serverSideEventProduction(BlockingQueue<XMLEvent> queue) {
        /*
        <collective2>
            <status>OK</status>
            <allPendingSignals>
                <system>
                    <systemid>13262198</systemid>
                    <pendingblock>
                    </pendingblock>
                </system>
                <system>
                    <systemid>11138919</systemid>
                    <pendingblock>
                    </pendingblock>
                </system>
                <system>
                    <systemid>12627477</systemid>
                    <pendingblock>
                    <signalid>26123758</signalid>
                    </pendingblock>
                </system>
                <system>
                    <systemid>12646748</systemid>
                    <pendingblock>
                    </pendingblock>
                </system>

                <system>
                    <systemid>15816213</systemid>
                    <pendingblock>
                        <signalid>26990089</signalid>
                        <signalid>27176705</signalid>
                        <signalid>27176730</signalid>
                        <signalid>27192490</signalid>
                        <signalid>27229357</signalid>
                        <signalid>27229363</signalid>
                        <signalid>27271727</signalid>
                        <signalid>27431975</signalid>
                        <signalid>27567130</signalid>
                        <signalid>27621823</signalid>
                        <signalid>27621827</signalid>
                        <signalid>27621830</signalid>
                        <signalid>27621833</signalid>
                        <signalid>27621836</signalid>
                        <signalid>27621839</signalid>
                        <signalid>27621842</signalid>
                        <signalid>27621845</signalid>
                        <signalid>27621848</signalid>
                        <signalid>27621851</signalid>
                        <signalid>27621856</signalid>
                        <signalid>27621859</signalid>
                        <signalid>27621945</signalid>
                        <signalid>27621949</signalid>
                        <signalid>27621952</signalid>
                        <signalid>27621955</signalid>
                        <signalid>27621959</signalid>
                        <signalid>27621963</signalid>
                    </pendingblock>
                </system>

            </allPendingSignals>

         </collective2>
         */
    	try{
	        queue.put(eventFactory.createStartDocument());
	        queue.put(eventFactory.createStartElement("", "", "collective2"));
	
	        queue.put(eventFactory.createStartElement("", "", "status"));
	        queue.put(eventFactory.createCharacters(status));
	        queue.put(eventFactory.createEndElement("", "", "status"));
	
	        queue.put(eventFactory.createStartElement("", "", "allPendingSignals"));
	
	        for(Map.Entry<Integer,List<Integer>> entry:allPendingSignals.entrySet()) {
	
	            queue.put(eventFactory.createStartElement("", "", "system"));
	
	            queue.put(eventFactory.createStartElement("", "", "systemid"));
	            queue.put(eventFactory.createCharacters(entry.getKey().toString()));
	            queue.put(eventFactory.createEndElement("", "", "systemid"));
	
	            queue.put(eventFactory.createStartElement("", "", "pendingblock"));
	            for(Integer signalid:entry.getValue()) {
	                queue.put(eventFactory.createStartElement("", "", "signalid"));
	                queue.put(eventFactory.createCharacters(signalid.toString()));
	                queue.put(eventFactory.createEndElement("", "", "signalid"));
	            }
	
	            queue.put(eventFactory.createEndElement("", "", "pendingblock"));
	
	            queue.put(eventFactory.createEndElement("", "", "system"));
	
	        }
	
	        queue.put(eventFactory.createEndElement("", "", "allPendingSignals"));
	
	
	        queue.put(eventFactory.createEndElement("", "", "collective2"));
	        queue.put(eventFactory.createEndDocument());
        } catch (InterruptedException e) {
            logger.trace("exit on interruption",e);
        }

    }
}
