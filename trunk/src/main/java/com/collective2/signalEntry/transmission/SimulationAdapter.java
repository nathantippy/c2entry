/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/4/12
 */
package com.collective2.signalEntry.transmission;

import static com.collective2.signalEntry.C2Element.ElementCash;
import static com.collective2.signalEntry.C2Element.ElementEquity;
import static com.collective2.signalEntry.C2Element.ElementMarginUsed;
import static com.collective2.signalEntry.C2Element.ElementStatus;
import static com.collective2.signalEntry.C2Element.ElementSystemId;
import static com.collective2.signalEntry.C2Element.ElementSystemName;
import static com.collective2.signalEntry.C2Element.ElementTotalEquityAvail;

import java.util.*;

import javax.xml.stream.XMLEventReader;

import com.collective2.signalEntry.C2Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.collective2.signalEntry.C2ServiceException;
import com.collective2.signalEntry.Parameter;
import com.collective2.signalEntry.implementation.Command;
import com.collective2.signalEntry.transmission.simulationXML.SimulatedResponseAddToOCAGroup;
import com.collective2.signalEntry.transmission.simulationXML.SimulatedResponseAllSystems;
import com.collective2.signalEntry.transmission.simulationXML.SimulatedResponseCancel;
import com.collective2.signalEntry.transmission.simulationXML.SimulatedResponseCancelAllPending;
import com.collective2.signalEntry.transmission.simulationXML.SimulatedResponseCloseAllPositions;
import com.collective2.signalEntry.transmission.simulationXML.SimulatedResponseFlushPendingSignals;
import com.collective2.signalEntry.transmission.simulationXML.SimulatedResponseGetAllSignals;
import com.collective2.signalEntry.transmission.simulationXML.SimulatedResponseGetBuyPower;
import com.collective2.signalEntry.transmission.simulationXML.SimulatedResponseGetSystemEquity;
import com.collective2.signalEntry.transmission.simulationXML.SimulatedResponseGetSystemHypothetical;
import com.collective2.signalEntry.transmission.simulationXML.SimulatedResponseNewComment;
import com.collective2.signalEntry.transmission.simulationXML.SimulatedResponsePositionStatus;
import com.collective2.signalEntry.transmission.simulationXML.SimulatedResponseRequestOCAId;
import com.collective2.signalEntry.transmission.simulationXML.SimulatedResponseReverse;
import com.collective2.signalEntry.transmission.simulationXML.SimulatedResponseSendSubscriberBroadcast;
import com.collective2.signalEntry.transmission.simulationXML.SimulatedResponseSetMinBuyPower;
import com.collective2.signalEntry.transmission.simulationXML.SimulatedResponseSignal;
import com.collective2.signalEntry.transmission.simulationXML.SimulatedResponseSignalStatus;

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
public class SimulationAdapter extends BackEndAdapter {

    private static final Logger logger = LoggerFactory.getLogger(SimulationAdapter.class);

    @Override
    public XMLEventReader transmit(Map<Parameter, Object> paraMap) {

        Command command = ((Command) paraMap.get(Parameter.SignalEntryCommand));
        switch (command) {
            case Signal:
                // return fixed values a real simulator could do better here
                // but, this is good enough for the unit tests
                return new SimulatedResponseSignal(10344682, "Signal 10344682 accepted for immediate processing.");
            case GetBuyPower:
                // return fixed values a real simulator could do better here
                // but, this is good enough for the unit tests
                return new SimulatedResponseGetBuyPower("OK", 1136058468l, 68300.00d);
            case AddToOCAGroup:
                // return fixed values a real simulator could do better here
                // but, this is good enough for the unit tests
                return new SimulatedResponseAddToOCAGroup("OK", "Signal 12345 now added to ocagroup 9876");
            case AllSystems:
                // return fixed values a real simulator could do better here
                // but, this is good enough for the unit tests
                return new SimulatedResponseAllSystems("OK", "", 123, 456);
            case Cancel:
                // return fixed values a real simulator could do better here
                // but, this is good enough for the unit tests
                return new SimulatedResponseCancel("OK");
            case CancelAllPending:
                // return fixed values a real simulator could do better here
                // but, this is good enough for the unit tests
                return new SimulatedResponseCancelAllPending("OK");
            case CloseAllPositions:
                // return fixed values a real simulator could do better here
                // but, this is good enough for the unit tests
                return new SimulatedResponseCloseAllPositions("OK");
            case FlushPendingSignals:
                // return fixed values a real simulator could do better here
                // but, this is good enough for the unit tests
                return new SimulatedResponseFlushPendingSignals("OK");
            case AllSignals:
                // return fixed values a real simulator could do better here
                // but, this is good enough for the unit tests
                Map<Integer, List<Integer>> allPendingSignals = new HashMap<Integer, List<Integer>>();
                List<Integer> pendingSignalList = new ArrayList<Integer>();
                pendingSignalList.add(1);
                pendingSignalList.add(2);
                allPendingSignals.put(1235, pendingSignalList);
                return new SimulatedResponseGetAllSignals("OK", allPendingSignals);
            case GetSystemEquity:
                // return fixed values a real simulator could do better here
                // but, this is good enough for the unit tests
                return new SimulatedResponseGetSystemEquity("OK", 1342299909l, 8755.68d);
            case GetSystemHypothetical:
                // return fixed values a real simulator could do better here
                // but, this is good enough for the unit tests
                List<Map<C2Element,Object>> data = new ArrayList<Map<C2Element,Object>>();
                String[] systemArray = paraMap.get(Parameter.Systems).toString().split("\\.");

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
                return new SimulatedResponseGetSystemHypothetical(data);
            case NewComment:
                // return fixed values a real simulator could do better here
                // but, this is good enough for the unit tests
                return new SimulatedResponseNewComment("OK: Signal  29148580 comment created", 29148580, "");
            case PositionStatus:
                // return fixed values a real simulator could do better here
                // but, this is good enough for the unit tests
                return new SimulatedResponsePositionStatus("OK", "2006-09-11 10:40:35:000", "EURUSD", 4);
            case RequestOCAId:
                // return fixed values a real simulator could do better here
                // but, this is good enough for the unit tests
                return new SimulatedResponseRequestOCAId(17195788, "You may use the ocaid above when adding new signals.");
            case Reverse:
                // return fixed values a real simulator could do better here
                // but, this is good enough for the unit tests
                return new SimulatedResponseReverse("OK");
            case SendSubscriberBroadcast:
                // return fixed values a real simulator could do better here
                // but, this is good enough for the unit tests
                return new SimulatedResponseSendSubscriberBroadcast("OK");
            case SetMinBuyPower:
                // return fixed values a real simulator could do better here
                // but, this is good enough for the unit tests
                return new SimulatedResponseSetMinBuyPower("OK");
            case SignalStatus:
                // return fixed values a real simulator could do better here
                // but, this is good enough for the unit tests
                return new SimulatedResponseSignalStatus((Integer)paraMap.get(Parameter.SignalId), "Velocity Forex System", "2006-05-19 15:34:50:000", "2006-05-19 15:45:28:000", "0", "2006-05-19 22:08:53:000", 20.87);
        }
        throw new C2ServiceException("Unspported command :" + command);

    }

}
