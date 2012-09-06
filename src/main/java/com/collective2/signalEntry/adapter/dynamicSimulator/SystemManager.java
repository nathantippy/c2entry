package com.collective2.signalEntry.adapter.dynamicSimulator;

import com.collective2.signalEntry.C2ServiceException;
import com.collective2.signalEntry.Duration;
import com.collective2.signalEntry.Instrument;
import com.collective2.signalEntry.Parameter;
import com.collective2.signalEntry.adapter.dynamicSimulator.order.*;
import com.collective2.signalEntry.adapter.dynamicSimulator.portfolio.Portfolio;
import com.collective2.signalEntry.adapter.dynamicSimulator.quantity.QuantityComputable;
import com.collective2.signalEntry.adapter.dynamicSimulator.quantity.QuantityFactory;
import com.collective2.signalEntry.implementation.Command;
import com.collective2.signalEntry.implementation.RelativeNumber;
import com.collective2.signalEntry.implementation.Request;
import com.collective2.signalEntry.implementation.SignalAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

import static com.collective2.signalEntry.Parameter.*;

/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  7/28/12
 */

public class SystemManager {

    private static final Logger logger = LoggerFactory.getLogger(SystemManager.class);
    private static final int BITS_FOR_SYSTEM_ID = 6; //64 max systems

    private final Portfolio portfolio;
    private final Integer systemId;
    private final String systemName;
    private final String password;

    private final BigDecimal commission;

    private DataProvider  dayMarketOpenData;
    private DataProvider  dayMarketCloseData;
    private DataProvider  lastDataProvider;

    private final SortedSet<Order> scheduled;//waiting for the right time
    private final List<Order> archive;//all signals listed here by index
    private final Map<Integer, List<Order>> ocaMap;
    private final Set<String> subscribers;

    //Tick:
    //     1. check all active against new market conditions
    //     2. run all scheduled for this time (only remove those that execute)
    //     3. create new actives as needed

    private final AtomicInteger ocaCounter;
    private final QuantityFactory quantityFactory;

    private Number minBuyPower = BigDecimal.ZERO; //margin call when this is hit.
    public static final int NO_ID = Integer.MIN_VALUE;

    public SystemManager(Portfolio portfolio, Integer systemId, String systemName, String password, BigDecimal commission) {
        this.portfolio = portfolio;
        this.systemId = systemId;
        if (systemId>=(1<<BITS_FOR_SYSTEM_ID)) {
            throw new C2ServiceException("Too many systems defined, limit is "+(1<<BITS_FOR_SYSTEM_ID),false);
        }
        this.systemName = systemName;
        this.password = password;
        this.ocaCounter = new AtomicInteger();
        this.archive = new ArrayList<Order>();
        this.scheduled = new ConcurrentSkipListSet<Order>();
        this.quantityFactory = new QuantityFactory();
        this.ocaMap = new HashMap<Integer,List<Order>>();
        this.commission = commission;
        this.subscribers = new HashSet<String>(); //email
    }

    public Integer id() {
        return systemId;
    }

    public String name() {
        return systemName;
    }

    public static int extractSystemId(int signalId) {
        //the system id is found in the lower BITS_FOR_SYSTEM_ID bits
        return signalId - ((signalId >>> BITS_FOR_SYSTEM_ID) << BITS_FOR_SYSTEM_ID);
    }

    public int[] scheduleSignal(long timeToExecute, Request request) {

        synchronized(archive) {
            Integer conditionalUponId = (Integer)request.get(Parameter.ConditionalUpon);
            Order conditionalUponOrder = (conditionalUponId==null? null : archive.get(conditionalUponId>>BITS_FOR_SYSTEM_ID));

            int signalIdOnly = archive.size();

            if (signalIdOnly > (1<<(32-(1+BITS_FOR_SYSTEM_ID)))  ) {
                throw new C2ServiceException("Too many signals, limit is:"+(1<<(32-(1+BITS_FOR_SYSTEM_ID))),false);
            }

            int id = (signalIdOnly << BITS_FOR_SYSTEM_ID)+systemId;

            String symbol = (String)request.get(Parameter.Symbol);
            SignalAction action = null;
            Order order = null;
            if (request.command()== Command.Reverse) {

                Duration timeInForce = (Duration)request.get(TimeInForce);
                if (timeInForce!=null) {
                    //default
                    timeInForce = Duration.GoodTilCancel;
                }

                Integer existingQuantity = portfolio.position(symbol).quantity();

                //buy or sell to make quantity revert to zero
                int closeId;
                Request requestToClose = request.baseConditional();
                requestToClose.put(Parameter.Quantity,existingQuantity);
                requestToClose.put(Parameter.MarketOrder,true);
                requestToClose.put(Parameter.Symbol,symbol);

                if (existingQuantity>0)  {
                    //STC

                    //requestToClose.put(Parameter.StockAction, ActionForStockType.SellToClose);


                    // schedule signal
                }  else {
                    //BTC
                    //TODO: finish reverse implementation
                    //requestToClose.put(Parameter.StockAction, ActionForStockType.SellToClose);

                }

                int[] parentId = scheduleSignal(timeToExecute,requestToClose);

                Request requestToOpen = request.baseConditional();
                //requestToOpen.put(Parameter.StockAction, ActionForStoack)




                //
                // conditioned upon that one do the same but open a position.



                throw new UnsupportedOperationException("Full reverse no yet implmented");
                //ReverseOrder reverseOrder =  new ReverseOrder(id,timeToExecute,symbol,timeInForce);

//                BigDecimal price = (BigDecimal)request.get(TriggerPrice);
//                if (price!=null) {
//                    reverseOrder.triggerPrice(price);
//                }
//
//                Integer quantity = (Integer)request.get(Quantity);
//                if (quantity!=null) {
//                    reverseOrder.quantity(quantity);
//                }
//
//                order = reverseOrder;
            } else {

                //use to compute quantity
                QuantityComputable quantityComputable = quantityFactory.computable(request,this);

                //GTC or Day
                Duration timeInForce = (Duration)request.get(Parameter.TimeInForce);

                //cancel at this fixed time
                long cancelAtMs = Long.MAX_VALUE;
                Number cancelAt = (Number)request.get(Parameter.CancelsAt);
                if (cancelAt != null) {
                    cancelAtMs = 1000l * cancelAt.intValue();
                }
                Number cancelAtRelative = (Number)request.get(Parameter.CancelsAtRelative);
                if (cancelAtRelative != null) {
                    cancelAtMs = timeToExecute + (1000l * cancelAtRelative.intValue());
                }

                //TODO: these must be watched by async submit   CancelsAt  (seconds time), CancelsAtRelative (seconds after submit)

                assert(request.command() == Command.Signal);

                Instrument instrument = (Instrument)request.get(Parameter.Instrument);
                action = (SignalAction)request.get(Parameter.Action);

                Integer xReplace = (Integer)request.get(XReplace);
                if (xReplace!=null) {
                    if (xReplace<0 || (xReplace>>BITS_FOR_SYSTEM_ID)>=archive.size()) {
                        throw new C2ServiceException("Invalid signalId "+xReplace+" not found.",false);
                    }
                    Order oldOrder = archive.get(xReplace>>BITS_FOR_SYSTEM_ID);
                    oldOrder.cancelOrder(timeToExecute);//cancel old order to be replaced
                    conditionalUponOrder = oldOrder.conditionalUpon();
                    assert(conditionalUponOrder!=null);
                    assert(conditionalUponOrder.quantity()>0 || conditionalUponOrder.isPending());

                }

                Order signal;
                //convert everything to relatives, should have already been relatives?
                RelativeNumber limit = (RelativeNumber)request.get(Parameter.RelativeLimitOrder);
                if (limit != null) {
                    OrderProcessor limitProcessor = new OrderProcessorLimit(timeToExecute, symbol,limit);
                    signal = new Order(id, instrument, symbol, action, quantityComputable, cancelAtMs, timeInForce,limitProcessor,conditionalUponOrder);
                }  else {
                    RelativeNumber stop = (RelativeNumber)request.get(Parameter.RelativeStopOrder);
                    if (stop != null) {
                        OrderProcessor stopProcessor = new OrderProcessorStop(timeToExecute, symbol,stop);
                        signal = new Order(id, instrument, symbol, action, quantityComputable, cancelAtMs, timeInForce, stopProcessor,conditionalUponOrder);
                    } else {
                        //market
                        OrderProcessor marketProcessor = new OrderProcessorMarket(timeToExecute, symbol);
                        signal = new Order(id, instrument, symbol, action, quantityComputable, cancelAtMs, timeInForce, marketProcessor,conditionalUponOrder);
                    }
                }

                Integer ocaId = (Integer)request.get(OCAId);
                if (ocaId!=null) {
                    signal.oneCancelsAnother(ocaId);
                    //add this signal to the group under this ocaId
                    ocaMap.get(ocaId).add(signal);
                }
                order = signal;
            }

            //must be done before adding the all-in-one stop or target because they must also be added
            archive.add(order);//needed for long term lookup by id order
            scheduled.add(order);//needed for processing by time order

            Integer ocaId = null;

            Integer forceNoOCA = (Integer)request.get(ForceNoOCA);
            if (forceNoOCA==null || forceNoOCA.intValue()!=1) {
                ocaId = generateNewOCAId();
            }
            int stopLossSignalId = NO_ID;
            int profitTargetSignalId = NO_ID;

            //generate and schedule requests for all-in-one
            RelativeNumber stopLoss = (RelativeNumber)request.get(Parameter.RelativeStopLoss);
            if (stopLoss!=null) {
                Request stopRequest = request.baseConditional();

                Instrument instrument = (Instrument)request.get(Parameter.Instrument);

                //all in one stopLoss, this is sell to close on buy and buy to close on sell.

                if (action == SignalAction.BTO) {
                    stopRequest.put(Parameter.Action, SignalAction.STC);
                } else if (action == SignalAction.SSHORT || action == SignalAction.STO) {
                    stopRequest.put(Parameter.Action, SignalAction.BTC);
                } else {
                    throw new UnsupportedOperationException("ActionType:"+action+" all-in-on stop loss undefined");
                }

                stopRequest.put(Parameter.RelativeStopOrder,stopLoss);

                //this is the only place the code will be able to get quantity because it can not be set
                //here when Dollar or Percent is used.
                stopRequest.put(Parameter.ConditionalUpon,id);
                //get quantity after previous order is submitted, must have exact shares!

                if (ocaId!=null) {
                    stopRequest.put(Parameter.OCAId,ocaId);
                }


                stopLossSignalId = scheduleSignal(timeToExecute, stopRequest)[0];
                //must return <stoplosssignalid>35584025</stoplosssignalid>

            }

            //generate and schedule requests for all-in-one
            RelativeNumber profitTarget = (RelativeNumber)request.get(RelativeProfitTarget);
            if (profitTarget!=null) {
                Request profitTargetRequest = request.baseConditional();

                Instrument instrument = (Instrument)request.get(Parameter.Instrument);

                if (action == SignalAction.BTO) {
                    profitTargetRequest.put(Parameter.Action, SignalAction.STC);
                } else if (action == SignalAction.SSHORT || action == SignalAction.STO) {
                    profitTargetRequest.put(Parameter.Action, SignalAction.BTC);
                } else {
                    throw new UnsupportedOperationException("ActionType:"+action+" all-in-on stop loss undefined");
                }

                profitTargetRequest.put(Parameter.RelativeLimitOrder,profitTarget);

                //this is the only place the code will be able to get quantity because it can not be set
                //here when Dollar or Percent is used.
                profitTargetRequest.put(Parameter.ConditionalUpon,id);
                //get quantity after previous order is submitted, must have exact shares!

                if (ocaId!=null) {
                    profitTargetRequest.put(Parameter.OCAId,ocaId);
                }

                profitTargetSignalId = scheduleSignal(timeToExecute, profitTargetRequest)[0];
                //must return <profittargetsignalid>35584025</profittargetsignalid>

            }

            return new int[] {id,stopLossSignalId,profitTargetSignalId};
        }
    }

    public void cancelSignal(Integer id, long time) {
        Order order = archive.get(id>>BITS_FOR_SYSTEM_ID);
        order.cancelOrder(time);
    }

    public void cancelAllPending(long time) {
        for(Order order:scheduled) {
            order.cancelOrder(time);
        }
    }

    public Order lookupOrder(Integer signalId) {
        Order order = archive.get(signalId>>BITS_FOR_SYSTEM_ID);
        return order;
    }

    public Portfolio portfolio() {
        return portfolio;
    }

    public List<Integer> allPendingSignals() {
        List<Integer> result = new ArrayList<Integer>();
        for(Order signal:scheduled) {
            //not filled, cancelled or expired!
            if (signal.isPending()) {
                result.add(signal.id());
            }
        }
        return result;
    }

    public String newComment(Integer signalId, String newComment) {

        Order signal = archive.get(signalId>>BITS_FOR_SYSTEM_ID);
        String previousComment = signal.comment();
        signal.comment(newComment);
        return previousComment;

    }

    public Integer generateNewOCAId() {
        Integer result = ocaCounter.getAndIncrement();
        ocaMap.put(result,new ArrayList<Order>());//empty list ready for members
        return result;
    }

    public void sendSubscriberBroadcast(String email, String message) {
        System.out.println("Sending:"+message+" from:"+email);
        //write this to standard out, don't really support email.
    }

    public void minBuyPower(Number buyPower) {
        minBuyPower = buyPower;
    }

    public BigDecimal totalMargin() {


        //based on each positions instrument
        return BigDecimal.ZERO;//TODO: margin not used in first release, not implemented yet

    }

    public boolean isPassword(String text) {
        return password.equals(text);
    }

    public void flushPendingSignals(DataProvider dataProvider) {
        tick(Long.MAX_VALUE,dataProvider);
    }

    public void tick(long time, DataProvider dataProvider) {
        //special case where Long.MAX_VALUE is used to flush pending signals
        //in this case do not record the open/close
        if (time < Long.MAX_VALUE) {
            recordMarketOpenClose(dataProvider);
       }
       //flush everything <= time
       if (!scheduled.isEmpty()) {

           Iterator<Order> orderIterator = scheduled.iterator();

           logger.trace("count of scheduled:?", scheduled.size());

           while (orderIterator.hasNext()) {

               Order signal = orderIterator.next();

               if (signal.time()>time) {
                    //these are future orders not to be processed (parked) until this time is reached
                    return;//do not process any more, sorted list so all orders after this point will also be parked
               }

               if (signal.isConditionProcessed()) {
                   long nowTime = dataProvider.startingTime();
                   if (!signal.isInForce(nowTime,dayMarketOpenData,dayMarketCloseData)) {
                       signal.cancelOrder(nowTime);
                   }

                   if (signal.isExpired(nowTime)) {
                       signal.cancelOrder(nowTime);
                   }

                   if (signal.process(dataProvider,portfolio,commission,dayMarketOpenData)) {
                       logger.trace("processed, signal " + signal);
                       scheduled.remove(signal);

                       if (signal.oneCancelsAnother()!=null) {
                           //this processed so cancel all the others in this same group before moving to next
                           List<Order> ocaList = ocaMap.get(signal.oneCancelsAnother());
                           for (Order order:ocaList) {
                               if (order!=signal) {
                                   order.cancelOrder(dataProvider.startingTime());
                               }
                           }
                           //oca triggered now remove so its not triggered again.
                           ocaList.clear();
                       }
                   }

               } else {
                   logger.trace("skipped, signal " + signal);
               }
           }
       }
    }

    private void recordMarketOpenClose(DataProvider dataProvider) {

        validateDataProvider(dataProvider);

        if (dataProvider.isStartingTimeMarketOpen()) {
            //start of a new day so set the open and clear the end
            dayMarketOpenData = dataProvider;
            dayMarketCloseData = null;
        }
        if (dataProvider.isEndingTimeMarketClose()) {
            dayMarketCloseData = dataProvider;
        }
    }

    private void validateDataProvider(DataProvider dataProvider) {
        if (dataProvider.endingTime()<= dataProvider.startingTime()) {
            throw new C2ServiceException("DataProvider must not have same starting and ending times.",false);
        }
        if (dataProvider.endingTime()<dataProvider.startingTime()) {
            throw new C2ServiceException("DataProvider startingTime must be before endingTime.",false);
        }
        if (null != lastDataProvider) {
            if (lastDataProvider == dataProvider) { //checking for same instance not equivalence
                throw new C2ServiceException("DataProvider instances are held for simulation so instances must be immutable and never reused. ",false);
            }
            if (dataProvider.startingTime()<lastDataProvider.endingTime()) {
                throw new C2ServiceException("DataProviders must not overlap times.",false);
            }

        }
        lastDataProvider = dataProvider;
    }

    public String statusMessage() {
        return name()+" "+portfolio().statusMessage();
    }

    public void subscribe(String eMail) {
        subscribers.add(eMail);
    }

    public boolean isSubscribed(String eMail) {
        return subscribers.contains(eMail);
    }

    public void unSubscribe(String eMail) {
        subscribers.remove(eMail);
    }

    public List<Order> allSignalsConditionalUpon(Integer signalIdInput) {
        List<Order> orders = new ArrayList<Order>();
        for(Order order:archive) {
            if (order.conditionalUpon()!=null && order.conditionalUpon().id()==signalIdInput.intValue()) {
                orders.add(order);
            }
        }
        return orders;
    }
}
