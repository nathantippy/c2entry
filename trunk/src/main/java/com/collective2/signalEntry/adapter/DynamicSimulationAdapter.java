/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  7/28/12
 */
package com.collective2.signalEntry.adapter;

import com.collective2.signalEntry.C2Element;
import com.collective2.signalEntry.C2ServiceException;
import com.collective2.signalEntry.Parameter;
import com.collective2.signalEntry.adapter.dynamicSimulator.*;
import com.collective2.signalEntry.adapter.simulationXML.*;
import com.collective2.signalEntry.implementation.Command;
import com.collective2.signalEntry.implementation.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLEventReader;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class DynamicSimulationAdapter implements C2EntryServiceAdapter {

    //TODO: simulator is most important feature followed by user in the loop.

    private static final Logger logger = LoggerFactory.getLogger(DynamicSimulationAdapter.class);
    private static final String OK = "ok";
    private static final String ERROR = "error";
    private long time;
    private final List<SystemManager> systems = new ArrayList<SystemManager>();
    private final Object lock = new Object();
    private DataProvider lastTickDataProvider;

    private final List<GainListenerManager> gainListeners = new ArrayList<GainListenerManager>();
    private final static ThreadFactory threadFactory = new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {

            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName("DynamicSimulationAdapter GainListenerManager");
            return thread;
        }
    };
    private final Executor gainExecutor = Executors.newSingleThreadExecutor(threadFactory);

    public DynamicSimulationAdapter(long startTime) {
        time = startTime;
    }

    public void tick(DataProvider dataProvider) {
        //can not tick and transmit at same moment in order to keep simulator repeatable
        synchronized(lock) {
               //look at ordered list of signals for each system and do all those older than time now.
            this.time = dataProvider.endingTime();
            for(SystemManager system:systems) {
                system.tick(time,dataProvider);
            }
            lastTickDataProvider = dataProvider;
        }

        for(GainListenerManager manager:gainListeners) {
            manager.send(time, gainExecutor, systems, dataProvider);
        }

    };

    public void addGainListener(long start, long period, GainListener listener) {

        gainListeners.add(new GainListenerManager(start,period,listener));

    }

    @Override
    public XMLEventReader transmit(Request request) {

        logger.trace("transmit "+request.toString());

        synchronized(lock) {
            SystemManager system = null;
            if (request.containsKey(Parameter.SystemId)) {
                system = lookupSystem(request);
                if (system==null) {
                    return new SimulatedResponseCancel(ERROR);//TODO: make bad password/system response, test server
                }
            }

            Command command = request.command();
            switch (command) {
                case Signal:

                     long timeToExecute = extractTimeToExecute(request);
                     int[] signalIdArray = system.scheduleSignal(timeToExecute,request);
                     int signalId             = signalIdArray[0];
                     int stopLossSignalId     = signalIdArray[1];
                     int profitTargetSignalId = signalIdArray[2];
                     return new SimulatedResponseSignal(signalId,OK);

                case GetBuyPower:
                   //  long lastTickTime = lastTickDataProvider.endingTime();
                     BigDecimal buyPower = system.portfolio().cash().add(system.totalMargin());
                     return new SimulatedResponseGetBuyPower(OK,time,buyPower);

                case Cancel:

                    Integer id = (Integer)request.get(Parameter.SignalId);
                    system.cancelSignal(id);
                    return new SimulatedResponseCancel(OK);

                case CancelAllPending:

                    system.cancelAllPending();
                    return new SimulatedResponseCancelAllPending(OK);

                case CloseAllPositions:

                    system.portfolio().closeAllPositions();
                    return new SimulatedResponseCloseAllPositions(OK);

                case FlushPendingSignals:

                    system.flushPendingSignals(lastTickDataProvider);
                    return new SimulatedResponseFlushPendingSignals(OK);

                case AllSignals:

                    Map<Integer,List<Integer>> allPendingSignals = new HashMap<Integer,List<Integer>>();
                    for(SystemManager sys:systems) {
                        Integer sysId = sys.id();
                        allPendingSignals.put(sysId,sys.allPendingSignals());
                    }
                    return new SimulatedResponseGetAllSignals(OK, allPendingSignals);

                case GetSystemEquity:

                    long lastTickTime = lastTickDataProvider.endingTime();
                    Number equity = system.portfolio().equity(lastTickDataProvider);
                    return new SimulatedResponseGetSystemEquity(OK,lastTickTime,equity);

                case GetSystemHypothetical:

                    List<Map<C2Element, Object>> data = new ArrayList<Map<C2Element, Object>>();

                    for(SystemManager sys:systems) {
                        Map<C2Element, Object> map = new HashMap<C2Element, Object>();
                        //populate this map for this system.
                        map.put(C2Element.ElementSystemId, sys.id());
                        map.put(C2Element.ElementSystemName, sys.name());

                        //NOTE: very basic margin calculations where negative cash is margin, should improve some day.
                        BigDecimal sysCash = sys.portfolio().cash();
                        BigDecimal sysEquity = sys.portfolio().equity(lastTickDataProvider);
                        BigDecimal totalEquityAvail = sysCash.add(sysEquity);
                        BigDecimal marginUsed = (sysCash.compareTo(BigDecimal.ZERO)<0 ? sysCash.negate() : BigDecimal.ZERO);

                        map.put(C2Element.ElementTotalEquityAvail, totalEquityAvail);
                        map.put(C2Element.ElementCash, sysCash);
                        map.put(C2Element.ElementEquity, sysEquity);
                        map.put(C2Element.ElementMarginUsed, marginUsed);

                        data.add(map);
                    }

                    return new SimulatedResponseGetSystemHypothetical(data);

                case PositionStatus:

                    String symbol = (String)request.get(Parameter.Symbol);
                    Integer position = system.portfolio().position(symbol).quantity();
                    return new SimulatedResponsePositionStatus(OK,time,symbol,position);

                case RequestOCAId:

                    Integer ocaid = system.generateNewOCAId();
                    return new SimulatedResponseRequestOCAId(ocaid,OK);

                case Reverse:
                    //the same as a signal but its done now and can not be parked
                    Integer reverseId = system.scheduleSignal(time, request)[0];
                    return new SimulatedResponseReverse(OK);

                case SendSubscriberBroadcast:

                    String email = (String)request.get(Parameter.EMail);
                    String message = (String)request.get(Parameter.Message);
                    system.sendSubscriberBroadcast(email,message);
                    return new SimulatedResponseSendSubscriberBroadcast(OK);

                case SetMinBuyPower:

                    Number minBuyPower = (Number)request.get(Parameter.BuyPower);
                    system.minBuyPower(minBuyPower);
                    return new SimulatedResponseSetMinBuyPower(OK);

                case NewComment:

                    Integer signalIdForComment = (Integer)request.get(Parameter.SignalId);
                    String comment   = (String)request.get(Parameter.Commentary);
                    String previousComment = system.newComment(signalIdForComment,comment);
                    return new SimulatedResponseNewComment(OK,signalIdForComment,previousComment);

                case AllSystems:     //lookup the systems here
                    throw new UnsupportedOperationException("Not implemented at this time");

                case AddToOCAGroup:   //lookup existing signalid and add it to this existing oca group
                    throw new UnsupportedOperationException("Not implemented at this time");

                case SignalStatus:
                    //no system provided for this call
                    //lookup for subscribers
                    throw new UnsupportedOperationException("Not implemented at this time");

            }
            throw new C2ServiceException("Unsupported command :" + command,false);
        }
    }

    private long extractTimeToExecute(Request request) {
        long timeToExecute = time;
        if (request.containsKey(Parameter.ParkUntil)) {
            timeToExecute = 1000l*(Long)request.get(Parameter.ParkUntil);// time in seconds
            request.remove(Parameter.ParkUntil);//allows tracking of what remains to be done
        }
        if (request.containsKey(Parameter.ParkUntilDateTime)) {
            String text = (String)request.get(Parameter.ParkUntilDateTime); //full time must convert to seconds
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
            try {
                timeToExecute = sdf.parse(text).getTime();
            } catch (ParseException e) {
                logger.error("Unable to park signal, ran immediately", e);
            }
            request.remove(Parameter.ParkUntilDateTime);//allows tracking of what remains to be done
        }
        if (request.containsKey(Parameter.Delay)) {
            Integer delay = (Integer)request.get(Parameter.Delay);// delay n seconds
            timeToExecute += (delay*1000l);
            request.remove(Parameter.Delay);//allows tracking of what remains to be done
        }
        return timeToExecute;
    }


    private SystemManager lookupSystem(Request request) {

        String password = (String)request.get(Parameter.Password);
        Integer systemId = (Integer)request.get(Parameter.SystemId);
        if (systemId>systems.size()) {
            return null;
        }
        SystemManager system = systems.get(systemId);
        if (system.isPassword(password)) {
            return system;
        } else {
            return null;
        }

    }

    public Integer createSystem(BigDecimal buyPower, String name, String password, BigDecimal commission) {
        Portfolio portfolio = new SimplePortfolio(buyPower);
        return createSystem(name,password, portfolio, commission);
    }

    public synchronized Integer createSystem(String name, String password, Portfolio portfolio, BigDecimal commission) {

        Integer systemId = systems.size();

        SystemManager system = new SystemManager(portfolio, systemId, name, password, commission);

        systems.add(system);

        return systemId;
    }

    public void subscribe(String eMail, Integer systemId) {
        //TODO: no implementation
    }
}