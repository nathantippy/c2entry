/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/10/12
 */
package com.collective2.signalEntry.adapter.simulationXML;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.events.XMLEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.collective2.signalEntry.Duration;
import com.collective2.signalEntry.Related;
import com.collective2.signalEntry.adapter.dynamicSimulator.order.Order;
import com.collective2.signalEntry.implementation.SignalAction;

public class SimulatedResponseSignalStatus extends SimulatedResponse {

    private static final Logger logger = LoggerFactory.getLogger(SimulatedResponseSignalStatus.class);

    public SimulatedResponseSignalStatus(String systemname, boolean detail, Related showRelated, Order order) {
        super(buildEvents(systemname,detail, showRelated, order, true).iterator());
    }

    private static List<XMLEvent> buildEvents(String systemname, boolean showDetails,
                                              Related showRelated, Order order, boolean outside) {

        String postedwhen = order.postedWhen();
        String emailedWhen = order.eMailedWhen();
        String killedWhen = order.killedWhen();
        String tradedWhen = order.tradedWhen();
        BigDecimal tradePrice = order.tradePrice();

        Integer signalId = order.id();

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
            if (outside) {
                queue.add(eventFactory.createStartElement("", "", "collective2"));
            }

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
            queue.add(eventFactory.createCharacters(emailedWhen));
            queue.add(eventFactory.createEndElement("", "", "emailedwhen"));

            queue.add(eventFactory.createStartElement("", "", "killedwhen"));
            queue.add(eventFactory.createCharacters(killedWhen));
            queue.add(eventFactory.createEndElement("", "", "killedwhen"));

            queue.add(eventFactory.createStartElement("", "", "tradedwhen"));
            queue.add(eventFactory.createCharacters(tradedWhen));
            queue.add(eventFactory.createEndElement("", "", "tradedwhen"));

            queue.add(eventFactory.createStartElement("", "", "tradeprice"));
            queue.add(eventFactory.createCharacters(tradePrice.toString()));
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
                SignalAction action = order.action();
                int quantity = order.quantity();
                String symbol = order.symbol();
                BigDecimal limit = order.limit(); //original signal value unless its relative in which case its not available
                BigDecimal stop = order.stop();  //original signal value unless its relative in which case its not available
                BigDecimal market = order.market();
                Duration duration = order.timeInForce();
                Integer ocaGroupId = order.oneCancelsAnother();

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

            if (showRelated!=null) {
                switch (showRelated) {
                    case Children:
                        for(Order child: order.uponThis()) {
                            queue.add(eventFactory.createStartElement("", "", "child"));
                            queue.addAll(buildEvents(systemname,showDetails,showRelated,child, false));

                            queue.add(eventFactory.createEndElement("", "", "child"));
                        }
                        break;
                    case Parent:
                        queue.add(eventFactory.createStartElement("", "", "parent"));
                        Order parent = order.conditionalUpon();
                        queue.addAll(buildEvents(systemname,showDetails,showRelated,parent, false));

                        queue.add(eventFactory.createEndElement("", "", "parent"));
                        break;
                }
            }

            queue.add(eventFactory.createEndElement("", "", "signal"));

            if (outside) {
                queue.add(eventFactory.createEndElement("", "", "collective2"));
            }

            queue.add(eventFactory.createEndDocument());
        return queue;

    }


}
