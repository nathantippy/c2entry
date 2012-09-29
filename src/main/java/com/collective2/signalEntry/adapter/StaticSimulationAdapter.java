/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/4/12
 */
package com.collective2.signalEntry.adapter;

import com.collective2.signalEntry.*;
import com.collective2.signalEntry.adapter.dynamicSimulator.order.Order;
import com.collective2.signalEntry.adapter.dynamicSimulator.order.OrderProcessor;
import com.collective2.signalEntry.adapter.dynamicSimulator.order.OrderProcessorMarket;
import com.collective2.signalEntry.adapter.dynamicSimulator.quantity.QuantityComputable;
import com.collective2.signalEntry.adapter.simulationXML.*;
import com.collective2.signalEntry.implementation.Request;
import com.collective2.signalEntry.implementation.SignalAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import java.util.*;

import static com.collective2.signalEntry.C2Element.*;

/**
 * Simulated response adapter for running tests without actually hitting
 * collective2. The responses are not related to what is sent but they are good
 * enough for a minimal test.
 * 
 * For a real client side simulator this class should be extended. The
 * SimulatedResponse* Classes can be used to simulate the return XML so the
 * simulator does not have to worry about any of those details.
 * 
 */
public class StaticSimulationAdapter implements C2EntryServiceAdapter {

    private static final Logger logger = LoggerFactory.getLogger(StaticSimulationAdapter.class);

    @Override
    public IterableXMLEventReader transmit(Request request) {

        XMLEventReader xmlEventReader;

        switch (request.command()) {
            case Signal:
                // return fixed values a real simulator could do better here
                // but, this is good enough for the unit tests
                xmlEventReader = new SimulatedResponseSignal(10344682, "Order 10344682 accepted for immediate processing.");
                break;
            case GetBuyPower:
                // return fixed values a real simulator could do better here
                // but, this is good enough for the unit tests
                xmlEventReader = new SimulatedResponseGetBuyPower("OK", 1136058468l, 68300.00d);
                break;
            case AddToOCAGroup:
                // return fixed values a real simulator could do better here
                // but, this is good enough for the unit tests
                xmlEventReader = new SimulatedResponseAddToOCAGroup("OK", "Order 12345 now added to ocagroup 9876");
                break;
            case AllSystems:
                // return fixed values a real simulator could do better here
                // but, this is good enough for the unit tests
                xmlEventReader = new SimulatedResponseAllSystems("OK", "", 123, 456);
                break;
            case Cancel:
                // return fixed values a real simulator could do better here
                // but, this is good enough for the unit tests
                xmlEventReader =  new SimulatedResponseCancel("OK");
                break;
            case CancelAllPending:
                // return fixed values a real simulator could do better here
                // but, this is good enough for the unit tests
                xmlEventReader = new SimulatedResponseCancelAllPending("OK");
                break;
            case CloseAllPositions:
                // return fixed values a real simulator could do better here
                // but, this is good enough for the unit tests
                xmlEventReader = new SimulatedResponseCloseAllPositions("OK");
                break;
            case FlushPendingSignals:
                // return fixed values a real simulator could do better here
                // but, this is good enough for the unit tests
                xmlEventReader = new SimulatedResponseFlushPendingSignals("OK");
                break;
            case AllSignals:
                // return fixed values a real simulator could do better here
                // but, this is good enough for the unit tests
                Map<Integer, List<Integer>> allPendingSignals = new HashMap<Integer, List<Integer>>();
                List<Integer> pendingSignalList = new ArrayList<Integer>();
                pendingSignalList.add(1);
                pendingSignalList.add(2);
                allPendingSignals.put(1235, pendingSignalList);
                xmlEventReader = new SimulatedResponseGetAllSignals("OK", allPendingSignals);
                break;
            case GetSystemEquity:
                // return fixed values a real simulator could do better here
                // but, this is good enough for the unit tests
                xmlEventReader = new SimulatedResponseGetSystemEquity("OK", 1342299909l, 8755.68d);
                break;
            case GetSystemHypothetical:
                // return fixed values a real simulator could do better here
                // but, this is good enough for the unit tests
                List<Map<C2Element,Object>> data = new ArrayList<Map<C2Element,Object>>();
                String[] systemArray = request.get(Parameter.Systems).toString().split("\\.");

                for(String sys:systemArray) {
                    Integer sid = Integer.parseInt(sys);
                    Map<C2Element,Object> map = new EnumMap<C2Element, Object>(C2Element.class);
                    
                    map.put(ElementSystemId, sid);
                    map.put(ElementSystemName, "hello");
                    map.put(ElementTotalEquityAvail, 100.23d);
                    map.put(ElementCash, 42.3d);
                    map.put(ElementEquity, 2000.4d);
                    map.put(ElementMarginUsed, 432d);
                    
                    data.add(map);
                }
                xmlEventReader = new SimulatedResponseGetSystemHypothetical(data);
                break;
            case NewComment:
                // return fixed values a real simulator could do better here
                // but, this is good enough for the unit tests
                xmlEventReader = new SimulatedResponseNewComment("OK: Order  29148580 comment created", 29148580, "");
                break;
            case PositionStatus:
                // return fixed values a real simulator could do better here
                // but, this is good enough for the unit tests
                xmlEventReader = new SimulatedResponsePositionStatus("OK", System.currentTimeMillis(), "EURUSD", 4);
                break;
            case RequestOCAId:
                // return fixed values a real simulator could do better here
                // but, this is good enough for the unit tests
                xmlEventReader = new SimulatedResponseRequestOCAId(17195788, "You may use the ocaid above when adding new signals.");
                break;
            case Reverse:
                // return fixed values a real simulator could do better here
                // but, this is good enough for the unit tests
                xmlEventReader = new SimulatedResponseReverse("OK");
                break;
            case SendSubscriberBroadcast:
                // return fixed values a real simulator could do better here
                // but, this is good enough for the unit tests
                xmlEventReader = new SimulatedResponseSendSubscriberBroadcast("OK");
                break;
            case SetMinBuyPower:
                // return fixed values a real simulator could do better here
                // but, this is good enough for the unit tests
                xmlEventReader = new SimulatedResponseSetMinBuyPower("OK");
                break;
            case SignalStatus:
                // return fixed values a real simulator could do better here
                // but, this is good enough for the unit tests
                SignalAction action = null;
                QuantityComputable quantityComputable = null;
                long cancelAtMs = Long.MAX_VALUE;
                Duration timeInForce = null;
                long time = System.currentTimeMillis();
                OrderProcessor processor = new OrderProcessorMarket(time,"WWW");
                Order conditionalUpon = null;
                Order order = new Order(null, (Integer)request.get(Parameter.SignalId), Instrument.Stock, "WWW", action, quantityComputable, cancelAtMs, timeInForce, processor, conditionalUpon);
                Related showRelated = null;
                String systemName ="Velocity Forex System";
                xmlEventReader = new SimulatedResponseSignalStatus(systemName, false, showRelated, order);
                break;
            default:
                throw new C2ServiceException("Unsupported command :" + request, false);

        }
        try {
            return new IterableXMLEventReader(xmlEventReader);
        } catch (XMLStreamException e) {
            logger.warn("Bad XML produced by simulator, should never happen.", e);
            throw new C2ServiceException(e,false);
        }
    }

}
