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
import java.util.Map;

public class SimulatedResponseGetAllSignals extends SimulatedResponse {
	private static final Logger logger = LoggerFactory.getLogger(SimulatedResponseGetAllSignals.class);

    public SimulatedResponseGetAllSignals(String status, Map<Integer, List<Integer>> allPendingSignals) {
        super(buildEvents(status,allPendingSignals));
    }

    private static Iterator<XMLEvent> buildEvents(String status, Map<Integer, List<Integer>> allPendingSignals) {
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
        List<XMLEvent> queue = new ArrayList<XMLEvent>();
	        queue.add(eventFactory.createStartDocument());
	        queue.add(eventFactory.createStartElement("", "", "collective2"));
	
	        queue.add(eventFactory.createStartElement("", "", "status"));
	        queue.add(eventFactory.createCharacters(status));
	        queue.add(eventFactory.createEndElement("", "", "status"));
	
	        queue.add(eventFactory.createStartElement("", "", "allPendingSignals"));
	
	        for(Map.Entry<Integer,List<Integer>> entry: allPendingSignals.entrySet()) {
	
	            queue.add(eventFactory.createStartElement("", "", "system"));
	
	            queue.add(eventFactory.createStartElement("", "", "systemid"));
	            queue.add(eventFactory.createCharacters(entry.getKey().toString()));
	            queue.add(eventFactory.createEndElement("", "", "systemid"));
	
	            queue.add(eventFactory.createStartElement("", "", "pendingblock"));
	            for(Integer signalid:entry.getValue()) {
	                queue.add(eventFactory.createStartElement("", "", "signalid"));
	                queue.add(eventFactory.createCharacters(signalid.toString()));
	                queue.add(eventFactory.createEndElement("", "", "signalid"));
	            }
	
	            queue.add(eventFactory.createEndElement("", "", "pendingblock"));
	
	            queue.add(eventFactory.createEndElement("", "", "system"));
	
	        }
	
	        queue.add(eventFactory.createEndElement("", "", "allPendingSignals"));
	
	
	        queue.add(eventFactory.createEndElement("", "", "collective2"));
	        queue.add(eventFactory.createEndDocument());
        return queue.iterator();

    }
}
