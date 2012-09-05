/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/10/12
 */
package com.collective2.signalEntry.adapter.simulationXML;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import javax.xml.stream.events.XMLEvent;

import com.collective2.signalEntry.Duration;
import com.collective2.signalEntry.implementation.Action;
import com.collective2.signalEntry.implementation.RelativeNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimulatedResponseSignalStatus extends SimulatedResponse {

    private static final Logger logger = LoggerFactory.getLogger(SimulatedResponseSignalStatus.class);

    public SimulatedResponseSignalStatus(Integer signalId, String systemname, String postedwhen,
                                         String emailedwhen, String killedwhen,
                                         String tradedwhen, BigDecimal tradeprice) {
        super(buildEvents(signalId,systemname,postedwhen,emailedwhen,killedwhen,tradedwhen,tradeprice,
                        false,null,0,null,null,null,null,null,null));
    }

    public SimulatedResponseSignalStatus(Integer signalId, String systemname, String postedwhen,
                                         String emailedwhen, String killedwhen,
                                         String tradedwhen, BigDecimal tradeprice,
                                         Action action, int quantity, String symbol, BigDecimal limit,
                                         BigDecimal stop, BigDecimal market,
                                         Duration duration, Integer ocaGroupId) {
        super(buildEvents(signalId,systemname,postedwhen,emailedwhen,killedwhen,tradedwhen,tradeprice,
                true,action,quantity,symbol,limit,stop,market,duration,ocaGroupId));
    }

    private static Iterator<XMLEvent> buildEvents(Integer signalId, String systemname, String postedwhen, String emailedwhen, String killedwhen,
                                                  String tradedwhen,
                                                  BigDecimal tradeprice, //trade price is zero until filled, real price and may not match desired limit or open
                                                  boolean showDetails, Action action, int quantity, String symbol,
                                                  BigDecimal limit, //original signal value unless its relative in which case its not available
                                                  BigDecimal stop,  //original signal value unless its relative in which case its not available
                                                  BigDecimal market,
                                                  Duration duration, Integer ocaGroupId) {

        /*
         * <collective2> <signal> <signalid>20919494</signalid>
         * <systemname>Velocity Forex System</systemname> <postedwhen>2006-05-19
         * 15:34:50:000</postedwhen> <emailedwhen>2006-05-19
         * 15:45:28:000</emailedwhen> <killedwhen>0</killedwhen>
         * <tradedwhen>2006-05-19 22:08:53:000</tradedwhen>
         * <tradeprice>20.87</tradeprice> </signal> </collective2>
         */
        List<XMLEvent> queue = new ArrayList<XMLEvent>();
            queue.add(eventFactory.createStartDocument());
            queue.add(eventFactory.createStartElement("", "", "collective2"));

            queue.add(eventFactory.createStartElement("", "", "signal"));

            queue.add(eventFactory.createStartElement("", "", "signalid"));
            queue.add(eventFactory.createCharacters(signalId.toString()));
            queue.add(eventFactory.createEndElement("", "", "signalid"));

            queue.add(eventFactory.createStartElement("", "", "systemname"));
            queue.add(eventFactory.createCharacters(systemname));
            queue.add(eventFactory.createEndElement("", "", "systemname"));

            queue.add(eventFactory.createStartElement("", "", "postedwhen"));
            queue.add(eventFactory.createCharacters(postedwhen));
            queue.add(eventFactory.createEndElement("", "", "postedwhen"));

            queue.add(eventFactory.createStartElement("", "", "emailedwhen"));
            queue.add(eventFactory.createCharacters(emailedwhen));
            queue.add(eventFactory.createEndElement("", "", "emailedwhen"));

            queue.add(eventFactory.createStartElement("", "", "killedwhen"));
            queue.add(eventFactory.createCharacters(killedwhen));
            queue.add(eventFactory.createEndElement("", "", "killedwhen"));

            queue.add(eventFactory.createStartElement("", "", "tradedwhen"));
            queue.add(eventFactory.createCharacters(tradedwhen));
            queue.add(eventFactory.createEndElement("", "", "tradedwhen"));

            queue.add(eventFactory.createStartElement("", "", "tradeprice"));
            queue.add(eventFactory.createCharacters(tradeprice.toString()));
            queue.add(eventFactory.createEndElement("", "", "tradeprice"));

            /*
              <action>BTO</action>
              <quant>100</quant>
              <symbol>AAPL</symbol>
              <limit>10.25</limit>
              <stop>0</stop>
              <market>0</market>
              <tif>DAY</tif>
              <ocagroupid></ocagroupid>
            */
            if (showDetails) {
                queue.add(eventFactory.createStartElement("", "", "action"));
                queue.add(eventFactory.createCharacters(action.toString()));
                queue.add(eventFactory.createEndElement("", "", "action"));

                queue.add(eventFactory.createStartElement("", "", "quant"));
                queue.add(eventFactory.createCharacters(Integer.toString(quantity)));
                queue.add(eventFactory.createEndElement("", "", "quant"));

                queue.add(eventFactory.createStartElement("", "", "symbol"));
                queue.add(eventFactory.createCharacters(symbol));
                queue.add(eventFactory.createEndElement("", "", "symbol"));

                queue.add(eventFactory.createStartElement("", "", "limit"));
                queue.add(eventFactory.createCharacters(limit.toString()));
                queue.add(eventFactory.createEndElement("", "", "limit"));

                queue.add(eventFactory.createStartElement("", "", "stop"));
                queue.add(eventFactory.createCharacters(stop.toString()));
                queue.add(eventFactory.createEndElement("", "", "stop"));

                queue.add(eventFactory.createStartElement("", "", "market"));
                queue.add(eventFactory.createCharacters(market.toString()));
                queue.add(eventFactory.createEndElement("", "", "market"));

                queue.add(eventFactory.createStartElement("", "", "tif"));
                queue.add(eventFactory.createCharacters(duration.toString()));
                queue.add(eventFactory.createEndElement("", "", "tif"));

                queue.add(eventFactory.createStartElement("", "", "ocagroupid"));
                queue.add(eventFactory.createCharacters(ocaGroupId==null?"":ocaGroupId.toString()));
                queue.add(eventFactory.createEndElement("", "", "ocagroupid"));

            }

            //TODO: add parent and child relationship support


            queue.add(eventFactory.createEndElement("", "", "signal"));

            queue.add(eventFactory.createEndElement("", "", "collective2"));
            queue.add(eventFactory.createEndDocument());
        return queue.iterator();

    }


}
