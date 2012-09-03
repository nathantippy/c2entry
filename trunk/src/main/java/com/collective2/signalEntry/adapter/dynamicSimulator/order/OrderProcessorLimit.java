/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  8/26/12
 */

package com.collective2.signalEntry.adapter.dynamicSimulator.order;

import com.collective2.signalEntry.C2ServiceException;
import com.collective2.signalEntry.adapter.dynamicSimulator.DataProvider;
import com.collective2.signalEntry.adapter.dynamicSimulator.portfolio.Portfolio;
import com.collective2.signalEntry.adapter.dynamicSimulator.quantity.QuantityComputable;
import com.collective2.signalEntry.implementation.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.collective2.signalEntry.implementation.RelativeNumber;

import java.math.BigDecimal;
import java.util.Date;

public class OrderProcessorLimit implements OrderProcessor {

    private static final Logger logger = LoggerFactory.getLogger(OrderProcessorLimit.class);
    private final RelativeNumber relativeLimit;
    private final String symbol;
    private final long time;
    private BigDecimal transactionPrice;

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

    public boolean process(DataProvider dataProvider, Portfolio portfolio, BigDecimal commission, Order order, Action action,
                           QuantityComputable computableQuantity) {
        logger.trace("process LimitOrder");

            BigDecimal absoluteLimit = RelativeNumberHelper.toAbsolutePrice(symbol, relativeLimit, dataProvider, portfolio);

            switch(action) {
                case BTC:
                    if (order.conditionalUpon()==null && portfolio.position(symbol).quantity().intValue()==0) {
                        order.cancelOrder(dataProvider.openingTime());
                        return true;
                    }
                    //close order but make sure its not already been closed
                    if (order.conditionalUpon()!=null) {
                        if ((order.conditionalUpon().isProcessed() && order.conditionalUpon().isClosed()) || order.conditionalUpon.isCancel()) {
                            order.cancelOrder(dataProvider.openingTime());
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

                    //if open price is lower than limit the buy must happen on open
                    BigDecimal openPriceData = dataProvider.openingPrice(symbol);
                    if (openPriceData.doubleValue()==0d) {
                        logger.warn("missing opening price for "+symbol()+" on "+new Date(dataProvider.openingTime())+" "+dataProvider.openingTime());
                        return true;
                    }

                    if (openPriceData.compareTo(absoluteLimit)<0) {
                        transactionPrice = openPriceData;
                    } else {
                        //open price was above limit but we know that low was under limit
                        //assume that as price goes down it will trigger at the limit
                        transactionPrice = absoluteLimit;
                    }

                    Integer buyQuantity = computableQuantity.quantity(transactionPrice,dataProvider);
                    if (buyQuantity.intValue()>0)  {
                        order.entryQuantity(buyQuantity);
                        //create the transaction in the portfolio
                        portfolio.position(symbol).addTransaction(buyQuantity, time, transactionPrice, commission, Action.BTC==action);
                        if (action==Action.BTC && order.conditionalUpon()!=null) {
                            order.conditionalUpon().closeOrder();
                        }
                        order.processed = true;
                    }
                    return true;

                case STC:
                    if (order.conditionalUpon()==null && portfolio.position(symbol).quantity().intValue()==0) {
                        order.cancelOrder(dataProvider.openingTime());
                        return true;
                    }
                    //close order but make sure its not already been closed
                    if (order.conditionalUpon()!=null) {
                        if ((order.conditionalUpon().isProcessed() && order.conditionalUpon().isClosed()) || order.conditionalUpon.isCancel()) {
                            order.cancelOrder(dataProvider.openingTime());
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

                    //if limit is under open then trigger at open
                    BigDecimal openPrice = dataProvider.openingPrice(symbol);
                    if (openPrice.doubleValue()==0d) {
                        logger.warn("missing opening price for "+symbol()+" on "+new Date(dataProvider.openingTime())+" "+dataProvider.openingTime());
                        return true;
                    }
                    if (absoluteLimit.compareTo(openPrice)<0) {
                        //limit is below low so anything between low and high is ok
                        transactionPrice = openPrice;
                    } else {
                        //trigger at limit when its reached
                        transactionPrice = absoluteLimit;
                    }

                    Integer sellQuantity = computableQuantity.quantity(transactionPrice,dataProvider);
                    if (sellQuantity.intValue()>0)  {
                        order.entryQuantity(sellQuantity);
                        //create the transaction in the portfolio
                        portfolio.position(symbol).addTransaction(-sellQuantity, time, transactionPrice, commission, Action.STC==action);
                        if (action==Action.STC && order.conditionalUpon()!=null) {
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
    public RelativeNumber triggerPrice() {
        return relativeLimit;
    }


}
