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

public class OrderProcessorLimit implements OrderProcessor {

    private static final Logger logger = LoggerFactory.getLogger(OrderProcessorLimit.class);
    private final RelativeNumber relativeLimit;
    private final String symbol;

    public OrderProcessorLimit(String symbol, RelativeNumber relativeLimit) {
        this.relativeLimit = relativeLimit;
        this.symbol = symbol;
    }

    public String symbol() {
        return symbol;
    }

    public boolean process(DataProvider dataProvider, Portfolio portfolio, BigDecimal commission, Order order, Action action,
                           QuantityComputable computableQuantity) {
        logger.trace("process LimitOrder");

            BigDecimal absoluteLimit = RelativeNumberHelper.toAbsolutePrice(symbol, relativeLimit, dataProvider, portfolio);
            BigDecimal price;

            switch(action) {
                case BTC:
                    if (order.conditionalUpon()==null && portfolio.position(symbol).quantity().intValue()==0) {
                        throw new C2ServiceException("BuyToClose requires conditional open order",false);
                    }
                    //close order but make sure its not already been closed
                    if (order.conditionalUpon()!=null) {
                        if ((order.conditionalUpon().isProcessed() && order.conditionalUpon().isClosed()) || order.conditionalUpon.cancel) {
                            order.cancelOrder();
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
                    BigDecimal openPrice = dataProvider.openingPrice(symbol);
                    if (openPrice.compareTo(absoluteLimit)<0) {
                        price = openPrice;
                    } else {
                        //open price was above limit but we know that low was under limit
                        //assume that as price goes down it will trigger at the limit
                        price = absoluteLimit;
                    }
                    {
                        Integer quantity = computableQuantity.quantity(price,portfolio,dataProvider);
                        if (quantity.intValue()==0)  {
                            return true;
                        }
                        order.entryQuantity(quantity);
                        //create the transaction in the portfolio
                        portfolio.position(symbol).addTransaction(quantity, order.time, price, commission, Action.BTC==action);
                        if (action==Action.BTC && order.conditionalUpon()!=null) {
                            order.conditionalUpon().closeOrder();
                        }
                        order.processed = true;
                    }
                    return true;

                case STC:
                    if (order.conditionalUpon()==null && portfolio.position(symbol).quantity().intValue()==0) {
                        throw new C2ServiceException("SellToClose requires conditional open order",false);
                    }
                    //close order but make sure its not already been closed
                    if (order.conditionalUpon()!=null) {
                        if ((order.conditionalUpon().isProcessed() && order.conditionalUpon().isClosed()) || order.conditionalUpon.cancel) {
                            order.cancelOrder();
                            return true;
                        }
                    }
                case SSHORT:
                case STO:
                    //must only buy if we can do it under absoluteLimit.

                    if (absoluteLimit.compareTo(dataProvider.highPrice(symbol))>0) {
                        logger.trace("do not trigger "+order.action+" "+absoluteLimit+"  "+dataProvider.highPrice(symbol));
                        return false;// do not trigger can not get good deal under limit price.
                    }

                    if (absoluteLimit.compareTo(dataProvider.lowPrice(symbol))<0) {
                        //limit is below low so anything between low and high is ok
                        price = dataProvider.openingPrice(symbol);//order.priceSelection(dataProvider.highPrice(order.symbol), dataProvider.lowPrice(order.symbol));
                    } else {
                        //limit is between low and high so only values between high and limit are ok
                        price = absoluteLimit;//order.priceSelection(dataProvider.highPrice(order.symbol), absoluteLimit);
                    }
                    {
                        Integer quantity = computableQuantity.quantity(price,portfolio,dataProvider);
                        if (quantity.intValue()==0)  {
                            return true;
                        }
                        order.entryQuantity(quantity);
                        //create the transaction in the portfolio
                        portfolio.position(symbol).addTransaction(-quantity, order.time, price, commission, Action.STC==action);
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
