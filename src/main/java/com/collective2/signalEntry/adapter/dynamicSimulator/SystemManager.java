package com.collective2.signalEntry.adapter.dynamicSimulator;

import com.collective2.signalEntry.*;
import com.collective2.signalEntry.adapter.dynamicSimulator.order.*;
import com.collective2.signalEntry.adapter.dynamicSimulator.quantity.QuantityComputable;
import com.collective2.signalEntry.adapter.dynamicSimulator.quantity.QuantityFactory;
import com.collective2.signalEntry.implementation.Action;
import com.collective2.signalEntry.implementation.Command;
import com.collective2.signalEntry.implementation.RelativeNumber;
import com.collective2.signalEntry.implementation.Request;

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

    private final Portfolio portfolio;
    private final Integer systemId;
    private final String systemName;
    private final String password;

    private final BigDecimal commission;

    private final SortedSet<Order> scheduled;//waiting for the right time
    private final List<Order> archive;//all signals listed here by index
    private final List<Order> active;//waiting on market conditions
    private final Map<Integer, List<Order>> ocaMap;

    //Tick:
    //     1. check all active against new market conditions
    //     2. run all scheduled for this time (only remove those that execute)
    //     3. create new actives as needed

    private final AtomicInteger ocaCounter;
    private final QuantityFactory quantityFactory;


    private Number minBuyPower = BigDecimal.ZERO; //margin call when this is hit.

    public SystemManager(Portfolio portfolio, Integer systemId, String systemName, String password, BigDecimal commission) {
        this.portfolio = portfolio;
        this.systemId = systemId;
        this.systemName = systemName;
        this.password = password;
        this.ocaCounter = new AtomicInteger();
        this.archive = new ArrayList<Order>();
        this.scheduled = new ConcurrentSkipListSet<Order>();
        this.active = new ArrayList<Order>();
        this.quantityFactory = new QuantityFactory();
        this.ocaMap = new HashMap<Integer,List<Order>>();
        this.commission = commission;
    }

    public Integer id() {
        return systemId;
    }

    public String name() {
        return systemName;
    }


    public int[] scheduleSignal(long timeToExecute, Request request) {

        //TODO: no support for setting signalid from the caller

        synchronized(archive) {
            Integer conditionalUponId = (Integer)request.get(Parameter.ConditionalUpon);
            Order conditionalUponOrder = (conditionalUponId==null? null : archive.get(conditionalUponId));


            int id = archive.size();

            String symbol = (String)request.get(Parameter.Symbol);

            Order order = null;
            if (request.command()== Command.Reverse) {

                ReverseOrder reverseOrder =  new ReverseOrder(id,timeToExecute,symbol);

                BigDecimal price = (BigDecimal)request.get(TriggerPrice);
                if (price!=null) {
                    reverseOrder.triggerPrice(price);
                }
                Duration duration = (Duration)request.get(OrderDuration);
                if (duration!=null) {
                    reverseOrder.duration(duration);
                }
                Integer quantity = (Integer)request.get(Quantity);
                if (quantity!=null) {
                    reverseOrder.quantity(quantity);
                }

                order = reverseOrder;
            } else {

                //use to compute quantity
                QuantityComputable quantityComputable = quantityFactory.computable(request);

                //GTC or Day
                Duration duration = (Duration)request.get(Parameter.OrderDuration);

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
                Action action;
                Instrument instrument = (Instrument)request.get(Parameter.Instrument);
                switch(instrument) {
                    case Stock:
                        action = ((ActionForStock)request.get(Parameter.StockAction)).action();
                        break;
                    default:
                        action = ((ActionForNonStock)request.get(Parameter.NonStockAction)).action();
                        break;
                }

                OrderSignal signal;
                //convert everything to relatives, should have already been relatives?
                RelativeNumber limit = (RelativeNumber)request.get(Parameter.RelativeLimitOrder);
                if (limit != null) {
                    signal = new LimitOrder(id,timeToExecute, instrument, symbol, limit, action, quantityComputable, cancelAtMs, duration);
                }  else {
                    RelativeNumber stop = (RelativeNumber)request.get(Parameter.RelativeStopOrder);
                    if (stop != null) {
                        signal = new StopOrder(id,timeToExecute, instrument, symbol, stop, action, quantityComputable, cancelAtMs, duration);
                    } else {
                        //market
                        signal = new MarketOrder(id,timeToExecute, instrument, symbol, action, quantityComputable, cancelAtMs, duration);
                    }
                }

                Integer ocaId = (Integer)request.get(OCAId);
                if (ocaId!=null) {
                    signal.oneCancelsAnother(ocaId);
                    //add this signal to the group under this ocaId
                    ocaMap.get(ocaId).add(signal);
                }

                Integer xReplace = (Integer)request.get(XReplace);
                if (xReplace!=null) {

                    Order oldOrder = archive.get(xReplace);
                    conditionalUponOrder = oldOrder.conditionalUpon();
                    oldOrder.cancel();
                    request.remove(XReplace);

                }

                order = signal;
            }

            if (conditionalUponOrder!=null) {
                order.conditionalUpon(conditionalUponOrder);
                //do normal schedule however in addition to time and other
                //critera the upon must have triggered
            }

            //must be done before adding the all-in-one stop or target because they must also be added
            archive.add(order);//needed for long term lookup by id order
            scheduled.add(order);//needed for processing by time order

            Integer ocaId = null;

            Integer forceNoOCA = (Integer)request.get(ForceNoOCA);
            if (forceNoOCA==null || forceNoOCA.intValue()!=1) {
                ocaId = generateNewOCAId();
            }
            int stopLossSignalId = -1;
            int profitTargetSignalId = -1;

            //generate and schedule requests for all-in-one
            RelativeNumber stopLoss = (RelativeNumber)request.get(Parameter.RelativeStopLoss);
            if (stopLoss!=null) {
                Request stopRequest = request.baseClone();

                Instrument instrument = (Instrument)request.get(Parameter.Instrument);
                switch(instrument) {
                    case Stock:
                        stopRequest.put(Parameter.StockAction,ActionForStock.SellToClose);   //TODO: is this right for shorts?
                        break;
                    default:
                        stopRequest.put(Parameter.NonStockAction,ActionForNonStock.SellToClose);  //TODO: is this right for shorts?
                        break;
                }
                stopRequest.put(Parameter.RelativeStopOrder,stopLoss); //TODO: is this right for shorts?

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
                Request profitTargetRequest = request.baseClone();

                Instrument instrument = (Instrument)request.get(Parameter.Instrument);
                switch(instrument) {
                    case Stock:
                        profitTargetRequest.put(Parameter.StockAction,ActionForStock.SellToClose);   //TODO: is this right for shorts?
                        break;
                    default:
                        profitTargetRequest.put(Parameter.NonStockAction,ActionForNonStock.SellToClose);  //TODO: is this right for shorts?
                        break;
                }
                profitTargetRequest.put(Parameter.RelativeLimitOrder,profitTarget); //TODO: is this right for shorts?

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


    public void cancelSignal(Integer id) {
        scheduled.remove(archive.get(id));
    }

    public void cancelAllPending() {
        scheduled.clear();
    }

    public Portfolio portfolio() {
        return portfolio;
    }

    public List<Integer> allPendingSignals() {

        List<Integer> result = new ArrayList<Integer>();
        for(Order signal:scheduled) {
            result.add(signal.id());
        }
        return result;
    }

    public String newComment(Integer signalId, String newComment) {

        Order signal = archive.get(signalId);
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

       // portfolio.
         //                                hhhh
        //based on each positions instrument
        return BigDecimal.ZERO;//TODO: margin was not used in first release, not implemented yet

    }


    //TODO: this simulation does not take into account splits or dividends


    public boolean isPassword(String password) {
        return true;//TODO: add implementation with test
    }

    public void flushPendingSignals(DataProvider dataProvider) {
        tick(Long.MAX_VALUE,dataProvider);
    }

    public void tick(long time, DataProvider dataProvider) {
       //flush everything <= time
       if (!scheduled.isEmpty()) {

           Iterator<Order> orderIterator = scheduled.iterator();

           while (orderIterator.hasNext()) {

               Order signal = orderIterator.next();

               if (signal.time()>time) {
                    return;//do not process any more
               }

               if (signal.process(dataProvider,portfolio,commission)) {
                   scheduled.remove(signal);

                   if (signal.oneCancelsAnother()!=null) {
                       //this processed so cancel all the others in this same group before moving to next
                       List<Order> ocaList = ocaMap.get(signal.oneCancelsAnother());
                       for (Order order:ocaList) {
                           if (order!=signal) {
                               order.cancel();
                           }
                       }
                       //oca triggered now remove so its not triggered again.
                       ocaList.clear();
                   }

               }
           }
       }
    }

    public String statusMessage() {
        return name()+" "+portfolio().statusMessage();
    }

    //TODO: DataProvider does not need open flag
    //TODO: add unit teest for gain listener and add boolean for log or system out



}
