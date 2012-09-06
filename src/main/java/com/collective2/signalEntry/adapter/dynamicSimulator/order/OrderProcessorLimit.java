/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  8/26/12
 */

package com.collective2.signalEntry.adapter.dynamicSimulator.order;

import com.collective2.signalEntry.adapter.dynamicSimulator.DataProvider;
import com.collective2.signalEntry.adapter.dynamicSimulator.portfolio.Portfolio;
import com.collective2.signalEntry.adapter.dynamicSimulator.quantity.QuantityComputable;
import com.collective2.signalEntry.implementation.RelativeNumber;
import com.collective2.signalEntry.implementation.SignalAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Date;

public class OrderProcessorLimit implements OrderProcessor {

    private static final Logger logger = LoggerFactory.getLogger(OrderProcessorLimit.class);
    private final RelativeNumber relativeLimit;
    private final String symbol;
    private final long time;
    private Integer transactionQuantity = 0;
    private BigDecimal transactionPrice = BigDecimal.ZERO;
    private BigDecimal absoluteLimit = BigDecimal.ZERO;

    public OrderProcessorLimit(long time, String symbol, RelativeNumber relativeLimit) {
        this.time = time;
        this.symbol = symbol;
        this.relativeLimit = relativeLimit;
    }

    public String symbol() {
        return symbol;
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
            logger.trace("process LimitOrder");

            absoluteLimit = RelativeNumberHelper.toAbsolutePrice(symbol, relativeLimit, dataProvider, portfolio, dayOpenData);

            if (BigDecimal.ZERO.compareTo(absoluteLimit)>=0) {
                logger.warn("unable to build limit");
                return true;
            }

            BigDecimal myOpenPrice;

            //if my conditional upon was today we can never use open because the time
            // for it has already gone by. For and open we must use
            //the transaction price of our conditional upon order
            if (null != order.conditionalUpon() && order.conditionalUpon().isTradedThisSession(dataProvider)) {
                myOpenPrice = order.conditionalUpon().tradePrice();
                assert(myOpenPrice.compareTo(BigDecimal.ZERO)>0);
            } else {
                myOpenPrice = dataProvider.openingPrice(symbol);
                if (BigDecimal.ZERO.compareTo(myOpenPrice)>=0) {
                    logger.warn("missing opening price for "+symbol()+" on "+new Date(dataProvider.startingTime())+" "+dataProvider.startingTime());
                    return true;
                }
            }

            switch(action) {
                case BTC:
                    if (order.conditionalUpon()==null && portfolio.position(symbol).quantity().intValue()==0) {
                        order.cancelOrder(dataProvider.startingTime());
                        return true;
                    }
                    //close order but make sure its not already been closed
                    if (order.conditionalUpon()!=null) {
                        if ((order.conditionalUpon().isProcessed() && order.conditionalUpon().isClosed()) || order.conditionalUpon.isCancel()) {
                            order.cancelOrder(dataProvider.startingTime());
                            return true;
                        }
                    }
                case BTO:
                    //must only buy if we can do it UNDER absoluteLimit.

                    //if limit is under low for the day this order does not trigger
                    if (absoluteLimit.compareTo(dataProvider.lowPrice(symbol))<0) {
                        logger.trace("do not trigger "+action+" "+absoluteLimit+"  "+dataProvider.lowPrice(symbol));
                        return false;// do not trigger can not get good deal under limit price.
                    }

                    //must buy under this limit
                    if (myOpenPrice.compareTo(absoluteLimit)<0) {
                        transactionPrice = myOpenPrice;
                    } else {
                        //open price was above limit but we know that low was under limit
                        //assume that as price goes down it will trigger at the limit
                        transactionPrice = absoluteLimit;
                    }

                    Integer buyQuantity = computableQuantity.quantity(transactionPrice,dataProvider);
                    if (buyQuantity.intValue()>0)  {
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
                    if (order.conditionalUpon()!=null) {
                        if ((order.conditionalUpon().isProcessed() && order.conditionalUpon().isClosed()) || order.conditionalUpon.isCancel()) {
                            order.cancelOrder(dataProvider.startingTime());
                            return true;
                        }
                    }
                case SSHORT:
                case STO:
                    //must sell only above this price

                    if (absoluteLimit.compareTo(dataProvider.highPrice(symbol))>0) {
                        logger.trace("do not trigger "+order.action+" "+absoluteLimit+"  "+dataProvider.highPrice(symbol));
                        return false;// do not trigger can not get good deal under limit price.
                    }

                    //must sell higher than limit
                    if (absoluteLimit.compareTo(myOpenPrice)<0) {
                        //limit is below low so anything between low and high is ok
                        transactionPrice = myOpenPrice;
                    } else {
                        //trigger at limit when its reached
                        transactionPrice = absoluteLimit;
                    }

                    Integer sellQuantity = computableQuantity.quantity(transactionPrice,dataProvider);
                    if (sellQuantity.intValue()>0)  {
                        transactionQuantity = sellQuantity;
                        //create the transaction in the portfolio
                        portfolio.position(symbol).addTransaction(-sellQuantity, time, transactionPrice, commission, SignalAction.STC==action);
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
        return absoluteLimit;
    }


}
