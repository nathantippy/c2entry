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

public class SimulatedResponseSignalStatus extends SimulatedResponse {

    private static final Logger logger = LoggerFactory.getLogger(SimulatedResponseSignalStatus.class);
    private final Integer       signalId;
    private final String        systemname;
    private final String        postedwhen;
    private final String        emailedwhen;
    private final String        killedwhen;
    private final String        tradedwhen;
    private final Number        tradeprice;

    public SimulatedResponseSignalStatus(Integer signalId, String systemname, String postedwhen, String emailedwhen, String killedwhen, String tradedwhen, Number tradeprice) {
        this.signalId = signalId;
        this.systemname = systemname;
        this.postedwhen = postedwhen;
        this.emailedwhen = emailedwhen;
        this.killedwhen = killedwhen;
        this.tradedwhen = tradedwhen;
        this.tradeprice = tradeprice;

    }

    @Override
    public void serverSideEventProduction(BlockingQueue<XMLEvent> queue) {

        /*
         * <collective2> <signal> <signalid>20919494</signalid>
         * <systemname>Velocity Forex System</systemname> <postedwhen>2006-05-19
         * 15:34:50:000</postedwhen> <emailedwhen>2006-05-19
         * 15:45:28:000</emailedwhen> <killedwhen>0</killedwhen>
         * <tradedwhen>2006-05-19 22:08:53:000</tradedwhen>
         * <tradeprice>20.87</tradeprice> </signal> </collective2>
         */
        try {
            queue.put(eventFactory.createStartDocument());
            queue.put(eventFactory.createStartElement("", "", "collective2"));

            queue.put(eventFactory.createStartElement("", "", "signal"));

            queue.put(eventFactory.createStartElement("", "", "signalid"));
            queue.put(eventFactory.createCharacters(signalId.toString()));
            queue.put(eventFactory.createEndElement("", "", "signalid"));

            queue.put(eventFactory.createStartElement("", "", "systemname"));
            queue.put(eventFactory.createCharacters(systemname));
            queue.put(eventFactory.createEndElement("", "", "systemname"));

            queue.put(eventFactory.createStartElement("", "", "postedwhen"));
            queue.put(eventFactory.createCharacters(postedwhen));
            queue.put(eventFactory.createEndElement("", "", "postedwhen"));

            queue.put(eventFactory.createStartElement("", "", "emailedwhen"));
            queue.put(eventFactory.createCharacters(emailedwhen));
            queue.put(eventFactory.createEndElement("", "", "emailedwhen"));

            queue.put(eventFactory.createStartElement("", "", "killedwhen"));
            queue.put(eventFactory.createCharacters(killedwhen));
            queue.put(eventFactory.createEndElement("", "", "killedwhen"));

            queue.put(eventFactory.createStartElement("", "", "tradedwhen"));
            queue.put(eventFactory.createCharacters(tradedwhen));
            queue.put(eventFactory.createEndElement("", "", "tradedwhen"));

            queue.put(eventFactory.createStartElement("", "", "tradeprice"));
            queue.put(eventFactory.createCharacters(tradeprice.toString()));
            queue.put(eventFactory.createEndElement("", "", "tradeprice"));

            queue.put(eventFactory.createEndElement("", "", "signal"));

            queue.put(eventFactory.createEndElement("", "", "collective2"));
            queue.put(eventFactory.createEndDocument());
        } catch (InterruptedException e) {
            logger.trace("exit on interruption", e);
        }

    }
}
