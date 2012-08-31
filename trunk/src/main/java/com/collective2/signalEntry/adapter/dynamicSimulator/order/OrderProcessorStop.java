package com.collective2.signalEntry.adapter.dynamicSimulator.order;

import com.collective2.signalEntry.C2ServiceException;
import com.collective2.signalEntry.adapter.dynamicSimulator.DataProvider;
import com.collective2.signalEntry.adapter.dynamicSimulator.portfolio.Portfolio;
import com.collective2.signalEntry.adapter.dynamicSimulator.quantity.QuantityComputable;
import com.collective2.signalEntry.implementation.Action;
import com.collective2.signalEntry.implementation.RelativeNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

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

    public OrderProcessorStop(String symbol, RelativeNumber relativeStop) {
        this.relativeStop = relativeStop;
        this.symbol = symbol;
    }

    public String symbol() {
        return symbol;
    }

    public boolean process(DataProvider dataProvider, Portfolio portfolio, BigDecimal commission, Order order, Action action,
                           QuantityComputable computableQuantity) {
        logger.trace("process StopOrder");


            BigDecimal absoluteStop = RelativeNumberHelper.toAbsolutePrice(symbol, relativeStop, dataProvider, portfolio);
            BigDecimal price;
            switch(action) {
                case BTC:
                    if (order.conditionalUpon()==null && portfolio.position(symbol).quantity().intValue()==0) {
                        throw new C2ServiceException("BuyToClose requires conditional open order",false);
                    }
                    //close order but make sure its not already been closed
                    if (order.conditionalUpon()!=null){
                        if ((order.conditionalUpon().isProcessed() && order.conditionalUpon().isClosed()) || order.conditionalUpon.cancel) {
                            order.cancelOrder();
                            return true;
                        }
                    }
                case BTO:
                    //must only buy if we can do it above absoluteLimit.

                    if (absoluteStop.compareTo(dataProvider.highPrice(symbol))>0) {
                        return false;// do not trigger can not buy above this stop price
                    }

                    if (absoluteStop.compareTo(dataProvider.lowPrice(symbol))<0) {
                        //stop is below low so anything between low and high is ok

                        price = dataProvider.openingPrice(symbol);//order.priceSelection(dataProvider.openingPrice(order.symbol),dataProvider.lowPrice(order.symbol), dataProvider.highPrice(order.symbol));
                    } else {
                        //limit is between low and high so only values between stop and high are ok
                        price = absoluteStop;//order.priceSelection(absoluteStop, dataProvider.highPrice(order.symbol));
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
                    if (order.conditionalUpon()!=null){
                        if ((order.conditionalUpon().isProcessed() && order.conditionalUpon().isClosed()) || order.conditionalUpon.cancel) {
                            order.cancelOrder();
                            return true;
                        }
                    }
                case SSHORT:
                case STO:
                    //must only sell if we can do it under absoluteStop.

                    if (absoluteStop.compareTo(dataProvider.lowPrice(symbol))<0) {
                        return false;// do not trigger can not sell under stop price
                    }

                    //if open price is under stop must stop out now
                    BigDecimal openPrice = dataProvider.openingPrice(symbol);
                    if (openPrice.compareTo(absoluteStop)<0) {
                        price = openPrice;
                    } else {
                        //open is above stop but low is < stop so when we move down it will trigger
                        price = absoluteStop;
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
        return relativeStop;
    }
}
