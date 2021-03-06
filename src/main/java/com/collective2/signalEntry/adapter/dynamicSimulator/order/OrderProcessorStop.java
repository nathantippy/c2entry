package com.collective2.signalEntry.adapter.dynamicSimulator.order;

import java.math.BigDecimal;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.collective2.signalEntry.BasePrice;
import com.collective2.signalEntry.adapter.dynamicSimulator.DataProvider;
import com.collective2.signalEntry.adapter.dynamicSimulator.portfolio.Portfolio;
import com.collective2.signalEntry.adapter.dynamicSimulator.quantity.QuantityComputable;
import com.collective2.signalEntry.implementation.RelativeNumber;
import com.collective2.signalEntry.implementation.SignalAction;

/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  8/26/12
 */

public class OrderProcessorStop implements OrderProcessor {

    private static final Logger logger = LoggerFactory.getLogger(OrderProcessorStop.class);
    private final RelativeNumber relativeStop;
    private final String symbol;
    private final long time;
    private Integer transactionQuantity = 0;
    private BigDecimal transactionPrice = BigDecimal.ZERO;;
    private BigDecimal absoluteStop = BigDecimal.ZERO;

    public OrderProcessorStop(long time, String symbol, RelativeNumber relativeStop) {
        this.time = time;
        this.symbol = symbol;
        this.relativeStop = relativeStop;
    }

    public String symbol() {
        return symbol;
    }

    public String toString() {
        return "StopOrder transactionPrice:"+transactionPrice+" absoluteStop:"+absoluteStop+" relativeStop:"+relativeStop+" transactionQuanity:"+transactionQuantity;
    }

    public long time() {
        return time;
    }

    public BigDecimal transactionPrice() {
        return transactionPrice;
    }

    public Integer transactionQuantity() {
        return transactionQuantity;
    }

    public boolean process(DataProvider dataProvider, Portfolio portfolio, BigDecimal commission, Order order, SignalAction action,
                           QuantityComputable computableQuantity, DataProvider dayOpenData) {
            logger.trace("process StopOrder");

            absoluteStop = RelativeNumberHelper.toAbsolutePrice(symbol, relativeStop, dataProvider, portfolio, dayOpenData);
            
            //if converted stop price is negative set it to zero for reasons of sanity. Record trace so system developers can make improvements
            if (BigDecimal.ZERO.compareTo(absoluteStop)>0 /*&& (null != order.conditionalUpon()) && order.conditionalUpon().isProcessed()  */) {
                logger.trace("absolute stop value was "+absoluteStop+" from "+relativeStop);//+" condUpon:"+order.conditionalUpon().symbol()+" "+order.conditionalUpon().tradeQuantity()+" "+order.conditionalUpon().tradePrice()+" "+order.conditionalUpon().action());
                absoluteStop = BigDecimal.ZERO;
            }

            BigDecimal myOpenPrice;

            //if my conditional upon was today we can never use open because the time
            // for it has already gone by. For and open we must use
            //the transaction price of our conditional upon order
            if (null != order.conditionalUpon() && order.conditionalUpon().isTradedThisSession(dataProvider)) {
                myOpenPrice = order.conditionalUpon().tradePrice();
                assert(myOpenPrice.compareTo(BigDecimal.ZERO)>0);
            } else {
             //TODO: confirm this else need be here because without it we are writing over the trade price
                //can not allow position open if the price as dropped to zero
                myOpenPrice = dataProvider.openingPrice(symbol);
            }    
                
            if (BigDecimal.ZERO.compareTo(myOpenPrice)>=0 && (SignalAction.BTO == action || SignalAction.STO == action) ) {
                logger.trace("missing opening price for "+symbol()+" on "+new Date(dataProvider.startingTime())+" "+dataProvider.startingTime());
                order.cancelOrder(dataProvider.startingTime());
                return true;
            }


            switch(action) {
                case BTC:
                    if (order.conditionalUpon()==null && portfolio.position(symbol).quantity().intValue()==0) {
                        order.cancelOrder(dataProvider.startingTime());
                        return true;
                    }
                    //close order but make sure its not already been closed
                    if (order.conditionalUpon()!=null){
                        if ((order.conditionalUpon().isProcessed() && order.conditionalUpon().isClosed()) || order.conditionalUpon.isCancel()) {
                            order.cancelOrder(dataProvider.startingTime());
                            return true;
                        }
                    }
                case BTO:
                    //must only buy if we can do it above absoluteLimit.

                    if (absoluteStop.compareTo(dataProvider.highPrice(symbol))>0) {
                        return false;// do not trigger can not buy above this stop price
                    }

                    //buy above the stop price
                    if (absoluteStop.compareTo(myOpenPrice)<0) {
                        transactionPrice = myOpenPrice;
                    } else {
                        transactionPrice = absoluteStop;
                    }

                    Integer buyQuantity = computableQuantity.quantity(transactionPrice,dataProvider);
                    if (buyQuantity.intValue()>0) {
                        transactionQuantity = buyQuantity;
                        //create the transaction in the portfolio
                        portfolio.position(symbol).addTransaction(buyQuantity, time, transactionPrice, commission, SignalAction.BTC==action);
                        if (action== SignalAction.BTC && order.conditionalUpon()!=null) {
                            order.conditionalUpon().closeOrder();
                        }
                        order.processed = true;
                    }
                    return true;

                case STC:
                    if (order.conditionalUpon()==null && portfolio.position(symbol).quantity().intValue()==0) {
                        order.cancelOrder(dataProvider.startingTime());
                        return true;
                    }
                    //close order but make sure its not already been closed
                    if (order.conditionalUpon()!=null){
                        if ((order.conditionalUpon().isProcessed() && order.conditionalUpon().isClosed()) || order.conditionalUpon.isCancel()) {
                            order.cancelOrder(dataProvider.startingTime());
                            return true;
                        }
                    }
                case SSHORT:
                case STO:
                    //must only sell if we can do it under absoluteStop.

                    if (absoluteStop.compareTo(dataProvider.lowPrice(symbol))<0) {
                        return false;// do not trigger can not sell under stop price
                    }

                    //must sell below stop
                    if (myOpenPrice.compareTo(absoluteStop)<0) {
                        transactionPrice = myOpenPrice;
                    } else {
                        transactionPrice = absoluteStop;
                    }

                    Integer shortQuantity = computableQuantity.quantity(transactionPrice,dataProvider);
                    if (shortQuantity.intValue()>0) {
                        transactionQuantity = shortQuantity;
                        //create the transaction in the portfolio
                        portfolio.position(symbol).addTransaction(-shortQuantity, time, transactionPrice, commission, SignalAction.STC==action);
                        if (action== SignalAction.STC && order.conditionalUpon()!=null) {
                            order.conditionalUpon().closeOrder();
                        }
                        order.processed = true;
                    }
                    return true;


                default:
                    throw new UnsupportedOperationException("Unsupported action:"+order.action);

            }

    }

    @Override
    public BigDecimal triggerPrice() {
        //if we still have the default value and the value can be resolved do so.
        if (BigDecimal.ZERO == absoluteStop && BasePrice.Absolute.prefix() == relativeStop.prefix()) {
            absoluteStop = relativeStop.value();
        }
        return absoluteStop;
    }
}
