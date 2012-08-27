package com.collective2.signalEntry.adapter.dynamicSimulator.order;

import com.collective2.signalEntry.C2ServiceException;
import com.collective2.signalEntry.adapter.dynamicSimulator.DataProvider;
import com.collective2.signalEntry.adapter.dynamicSimulator.portfolio.Portfolio;
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

    public OrderProcessorStop(RelativeNumber relativeStop) {
        this.relativeStop = relativeStop;
    }

    @Override
    public boolean process(DataProvider dataProvider, Portfolio portfolio, BigDecimal commission, OrderSignal order) {
        logger.trace("process StopOrder");

        if (order.processed || order.cancel) {
            //cancel instead of submit order
            //still return true but no need to add any transaction to the portfolio
            return true;
        } else {

            BigDecimal absoluteStop = RelativeNumberHelper.toAbsolutePrice(order.symbol, relativeStop, dataProvider, portfolio);
            BigDecimal price;
            Integer quantity;
            switch(order.action) {
                case BTC:
                    if (order.conditionalUpon()==null && portfolio.position(order.symbol).quantity().intValue()==0) {
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

                    if (absoluteStop.compareTo(dataProvider.highPrice(order.symbol))>0) {
                        return false;// do not trigger can not buy above this stop price
                    }

                    if (absoluteStop.compareTo(dataProvider.lowPrice(order.symbol))<0) {
                        //stop is below low so anything between low and high is ok
                        price = order.priceSelection(dataProvider.lowPrice(order.symbol), dataProvider.highPrice(order.symbol));
                    } else {
                        //limit is between low and high so only values between stop and high are ok
                        price = order.priceSelection(absoluteStop, dataProvider.highPrice(order.symbol));
                    }
                    //using the price used for the transaction determine the right quantity
                    quantity = order.quantityComputable.quantity(price, portfolio, dataProvider, order.conditionalUpon());
                    if (quantity.intValue()==0) {
                        return true;
                    }
                    order.entryQuantity(quantity);
                    //create the transaction in the portfolio
                    portfolio.position(order.symbol).addTransaction(quantity, order.time, price, commission, Action.BTC==order.action);
                    if (order.action==Action.BTC && order.conditionalUpon()!=null) {
                        order.conditionalUpon().closeOrder();
                    }
                    order.processed = true;
                    return true;

                case STC:
                    if (order.conditionalUpon()==null && portfolio.position(order.symbol).quantity().intValue()==0) {
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

                    if (absoluteStop.compareTo(dataProvider.lowPrice(order.symbol))<0) {
                        return false;// do not trigger can not sell under stop price
                    }

                    //must be under stop that we sell
                    if (absoluteStop.compareTo(dataProvider.highPrice(order.symbol))>0) {
                        //stop is above high so anything between low and high is ok
                        price = order.priceSelection(dataProvider.highPrice(order.symbol), dataProvider.lowPrice(order.symbol));
                    } else {
                        //stop is between low and high so only values between low and limit are ok
                        price = order.priceSelection(absoluteStop, dataProvider.lowPrice(order.symbol));
                    }
                    //using the price used for the transaction determine the right quantity
                    quantity = order.quantityComputable.quantity(price, portfolio, dataProvider, order.conditionalUpon());
                    if (quantity.intValue()==0) {
                        return true;
                    }
                    order.entryQuantity(quantity);
                    //create the transaction in the portfolio
                    portfolio.position(order.symbol).addTransaction(-quantity, order.time, price, commission, Action.STC==order.action);
                    if (order.action==Action.STC && order.conditionalUpon()!=null) {
                        order.conditionalUpon().closeOrder();
                    }
                    order.processed = true;
                    return true;


                default:
                    throw new UnsupportedOperationException("Unsupported action:"+order.action);

            }
        }
    }
}