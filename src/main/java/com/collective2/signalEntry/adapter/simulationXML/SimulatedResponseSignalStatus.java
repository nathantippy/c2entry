/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/10/12
 */
package com.collective2.signalEntry.adapter.simulationXML;

import java.math.BigDecimal;
import java.util.concurrent.BlockingQueue;

import javax.xml.stream.events.XMLEvent;

import com.collective2.signalEntry.Duration;
import com.collective2.signalEntry.implementation.Action;
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
    private final BigDecimal    tradeprice;

    private boolean     showDetails;
    private Action      action;
    private int         quantity;
    private String      symbol;
    private BigDecimal  limit;
    private BigDecimal  stop;
    private BigDecimal  market;
    private Duration    duration;
    private Integer     ocaGroupId;

    private boolean     showChildren = false;
    private boolean     showParents = false;

    public SimulatedResponseSignalStatus(Integer signalId, String systemname, String postedwhen,
                                         String emailedwhen, String killedwhen,
                                         String tradedwhen, BigDecimal tradeprice) {
        this.signalId = signalId;
        this.systemname = systemname;
        this.postedwhen = postedwhen;
        this.emailedwhen = emailedwhen;
        this.killedwhen = killedwhen;
        this.tradedwhen = tradedwhen;
        this.tradeprice = tradeprice;

    }


    public void showDetails(Action action, int quantity, String symbol, BigDecimal limit, BigDecimal stop, BigDecimal market,
                            Duration duration, Integer ocaGroupId) {
        this.showDetails = true;
        this.action = action;
        this.quantity = quantity;
        this.symbol = symbol;
        this.limit = limit;
        this.stop = stop;
        this.market = market;
        this.duration = duration;
        this.ocaGroupId = ocaGroupId;
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
                queue.put(eventFactory.createStartElement("", "", "action"));
                queue.put(eventFactory.createCharacters(action.toString()));
                queue.put(eventFactory.createEndElement("", "", "action"));

                queue.put(eventFactory.createStartElement("", "", "quant"));
                queue.put(eventFactory.createCharacters(Integer.toString(quantity)));
                queue.put(eventFactory.createEndElement("", "", "quant"));

                queue.put(eventFactory.createStartElement("", "", "symbol"));
                queue.put(eventFactory.createCharacters(symbol));
                queue.put(eventFactory.createEndElement("", "", "symbol"));

                queue.put(eventFactory.createStartElement("", "", "limit"));
                queue.put(eventFactory.createCharacters(limit.toString()));
                queue.put(eventFactory.createEndElement("", "", "limit"));

                queue.put(eventFactory.createStartElement("", "", "stop"));
                queue.put(eventFactory.createCharacters(stop.toString()));
                queue.put(eventFactory.createEndElement("", "", "stop"));

                queue.put(eventFactory.createStartElement("", "", "market"));
                queue.put(eventFactory.createCharacters(market.toString()));
                queue.put(eventFactory.createEndElement("", "", "market"));

                queue.put(eventFactory.createStartElement("", "", "tif"));
                queue.put(eventFactory.createCharacters(duration.toString()));
                queue.put(eventFactory.createEndElement("", "", "tif"));

                queue.put(eventFactory.createStartElement("", "", "ocagroupid"));
                queue.put(eventFactory.createCharacters(ocaGroupId==null?"":ocaGroupId.toString()));
                queue.put(eventFactory.createEndElement("", "", "ocagroupid"));

            }

            if (showChildren) {
               throw new UnsupportedOperationException("Not implemented yet");

            }

            if (showParents) {
                throw new UnsupportedOperationException("Not implemented yet");

            }


            queue.put(eventFactory.createEndElement("", "", "signal"));

            queue.put(eventFactory.createEndElement("", "", "collective2"));
            queue.put(eventFactory.createEndDocument());
        } catch (InterruptedException e) {
            logger.trace("exit on interruption", e);
        }

    }


}
