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

/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  7/29/12
 */

public class StopOrder extends OrderSignal {

    private static final Logger logger = LoggerFactory.getLogger(StopOrder.class);

    private final RelativeNumber relativeStop;

    public StopOrder(int id, long time, Instrument instrument, String symbol, RelativeNumber stop, Action action, QuantityComputable quantityComputable, long cancelAtMs, Duration duration) {
        super(id, time, instrument, symbol, action, quantityComputable, cancelAtMs, duration);
        this.relativeStop = stop;
    }

    @Override
    public boolean process(DataProvider dataProvider, Portfolio portfolio, BigDecimal commission) {
        logger.trace("process StopOrder");

        if (processed || cancel) {
            //cancel instead of submit order
            //still return true but no need to add any transaction to the portfolio
            return true;
        } else {

            BigDecimal absoluteStop = absolutePrice(relativeStop,dataProvider,portfolio);
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
                    //must only buy if we can do it above absoluteLimit.

                    if (absoluteStop.compareTo(dataProvider.highPrice(symbol))>0) {
                        return false;// do not trigger can not buy above this stop price
                    }

                    if (absoluteStop.compareTo(dataProvider.lowPrice(symbol))<0) {
                        //stop is below low so anything between low and high is ok
                        price = priceSelection(dataProvider.lowPrice(symbol), dataProvider.highPrice(symbol));
                    } else {
                        //limit is between low and high so only values between stop and high are ok
                        price = priceSelection(absoluteStop, dataProvider.highPrice(symbol));
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
                    //must only sell if we can do it under absoluteStop.

                    if (absoluteStop.compareTo(dataProvider.lowPrice(symbol))<0) {
                        return false;// do not trigger can not sell under stop price
                    }

                    //must be under stop that we sell
                    if (absoluteStop.compareTo(dataProvider.highPrice(symbol))>0) {
                        //stop is above high so anything between low and high is ok
                        price = priceSelection(dataProvider.highPrice(symbol), dataProvider.lowPrice(symbol));
                    } else {
                        //stop is between low and high so only values between low and limit are ok
                        price = priceSelection(absoluteStop, dataProvider.lowPrice(symbol));
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
