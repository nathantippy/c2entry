/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/10/12
 */
package com.collective2.signalEntry.transmission.simulationXML;

import static com.collective2.signalEntry.C2Element.*;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import javax.xml.stream.events.XMLEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.collective2.signalEntry.C2Element;

public class SimulatedResponseGetSystemHypothetical extends SimulatedResponse {
    private static final Logger logger = LoggerFactory.getLogger(SimulatedResponseGetBuyPower.class);
    private final List<Map<C2Element,Object>> data;
    
    public SimulatedResponseGetSystemHypothetical(List<Map<C2Element,Object>> data) {
        this.data = data;
    }
    
    @Override
    public void serverSideEventProduction(BlockingQueue<XMLEvent> queue) {
        /*
         * <collective2> <hypotheticalEquity> <system>
         * <systemid>13889808</systemid> <systemid>Absolute Returns</systemid>
         * <totalequityavail>48281.90</totalequityavail> <cash>101864.90</cash>
         * <equity>-8863</equity> <marginused>44720</marginused> </system>
         * <system> <systemid>13202557</systemid>
         * <systemid>extreme-os</systemid>
         * <totalequityavail>218505.23</totalequityavail> <cash>226803.23</cash>
         * <equity>894200</equity> <marginused>902498</marginused> </system>
         * </hypotheticalEquity> </collective2>
         */
        try {
            queue.put(eventFactory.createStartDocument());
            queue.put(eventFactory.createStartElement("", "", "collective2"));
            queue.put(eventFactory.createStartElement("", "", "hypotheticalEquity"));

            //this implementation is missing the second systemid (not sure if its required)
            EnumSet<C2Element> fields = EnumSet.of(ElementSystemId,ElementTotalEquityAvail,ElementCash,ElementEquity,ElementMarginUsed);

            for(Map<C2Element,Object> map: data) {
                queue.put(eventFactory.createStartElement("", "", "system"));
                
                for (C2Element field:fields) {
                    queue.put(eventFactory.createStartElement("", "", field.localElementName()));
                    queue.put(eventFactory.createCharacters(map.get(field).toString()));
                    queue.put(eventFactory.createEndElement("", "", field.localElementName()));
                }
                
                queue.put(eventFactory.createEndElement("", "", "system"));
            }
            

            queue.put(eventFactory.createEndElement("", "", "hypotheticalEquity"));
            queue.put(eventFactory.createEndElement("", "", "collective2"));
            queue.put(eventFactory.createEndDocument());
        } catch (InterruptedException e) {
            logger.trace("exit on interruption", e);
        }

    }
}
