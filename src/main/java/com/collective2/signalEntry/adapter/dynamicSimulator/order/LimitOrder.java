/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  7/29/12
 */
package com.collective2.signalEntry.adapter.dynamicSimulator.order;

import com.collective2.signalEntry.C2ServiceException;
import com.collective2.signalEntry.Duration;
import com.collective2.signalEntry.Instrument;
import com.collective2.signalEntry.adapter.dynamicSimulator.DataProvider;
import com.collective2.signalEntry.adapter.dynamicSimulator.Portfolio;
import com.collective2.signalEntry.adapter.dynamicSimulator.quantity.QuantityComputable;
import com.collective2.signalEntry.implementation.Action;
import com.collective2.signalEntry.implementation.RelativeNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public class LimitOrder extends OrderSignal {

    private static final Logger logger = LoggerFactory.getLogger(LimitOrder.class);

    private final RelativeNumber relativeLimit;

    public LimitOrder(int id, long time, Instrument instrument, String symbol, RelativeNumber limit, Action action, QuantityComputable quantityComputable, long cancelAtMs, Duration duration) {
        super(id, time, instrument, symbol, action, quantityComputable, cancelAtMs, duration);
        this.relativeLimit = limit;
    }

    @Override
    public boolean process(DataProvider dataProvider, Portfolio portfolio, BigDecimal commission) {
        logger.trace("process LimitOrder");

        if (processed || cancel) {
            //cancel instead of submit order
            //still return true but no need to add any transaction to the portfolio
            return true;
        } else {

            BigDecimal absoluteLimit = absolutePrice(relativeLimit,dataProvider,portfolio);
            BigDecimal price;
            Integer quantity;
            switch(action) {
                case BTC:
                    if (conditionalUpon()==null) {
                        throw new C2ServiceException("BuyToClose requires conditional open order",false);
                    }
                    //close order but make sure its not already been closed
                    if ((conditionalUpon().isProcessed() && conditionalUpon().isClosed()) || conditionalUpon.cancel) {
                        cancelOrder();
                        return true;
                    }
                case BTO:
                    //must only buy if we can do it under absoluteLimit.

                    if (absoluteLimit.compareTo(dataProvider.lowPrice(symbol))<0) {
                        logger.trace("do not trigger "+action+" "+absoluteLimit+"  "+dataProvider.lowPrice(symbol));
                        return false;// do not trigger can not get good deal under limit price.
                    }

                    if (absoluteLimit.compareTo(dataProvider.highPrice(symbol))>0) {
                        //limit is above high so anything between low and high is ok
                        price = priceSelection(dataProvider.lowPrice(symbol), dataProvider.highPrice(symbol));
                    } else {
                        //limit is between low and high so only values between low and limit are ok
                        price = priceSelection(dataProvider.lowPrice(symbol), absoluteLimit);
                    }
                    //using the price used for the transaction determine the right quantity
                    quantity = quantityComputable.quantity(price, portfolio, dataProvider, conditionalUpon());
                    entryQuantity(quantity);
                    //create the transaction in the portfolio
                    portfolio.position(symbol).addTransaction(quantity, time, price, commission);
                    if (action==Action.BTC) {
                        conditionalUpon().closeOrder();
                    }
                    processed = true;
                    return true;

                case STC:
                    if (conditionalUpon()==null) {
                        throw new C2ServiceException("SellToClose requires conditional open order",false);
                    }
                    //close order but make sure its not already been closed
                    if ((conditionalUpon().isProcessed() && conditionalUpon().isClosed()) || conditionalUpon.cancel) {
                        cancelOrder();
                        return true;
                    }
                case SSHORT:
                case STO:
                    //must only buy if we can do it under absoluteLimit.

                    if (absoluteLimit.compareTo(dataProvider.highPrice(symbol))>0) {
                        logger.trace("do not trigger "+action+" "+absoluteLimit+"  "+dataProvider.highPrice(symbol));
                        return false;// do not trigger can not get good deal under limit price.
                    }

                    if (absoluteLimit.compareTo(dataProvider.lowPrice(symbol))<0) {
                        //limit is below low so anything between low and high is ok
                        price = priceSelection(dataProvider.highPrice(symbol), dataProvider.lowPrice(symbol));
                    } else {
                        //limit is between low and high so only values between high and limit are ok
                        price = priceSelection(dataProvider.highPrice(symbol), absoluteLimit);
                    }
                    //using the price used for the transaction determine the right quantity
                    quantity = quantityComputable.quantity(price, portfolio, dataProvider, conditionalUpon());
                    entryQuantity(quantity);
                    //create the transaction in the portfolio
                    portfolio.position(symbol).addTransaction(-quantity, time, price, commission);
                    if (action==Action.STC) {
                        conditionalUpon().closeOrder();
                    }
                    processed = true;
                    return true;

                default:
                    throw new UnsupportedOperationException("Unsupported action:"+action);

            }
        }
    }

}
