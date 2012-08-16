/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  7/29/12
 */
package com.collective2.signalEntry.adapter.dynamicSimulator.order;

import com.collective2.signalEntry.Duration;
import com.collective2.signalEntry.Instrument;
import com.collective2.signalEntry.adapter.dynamicSimulator.DataProvider;
import com.collective2.signalEntry.adapter.dynamicSimulator.Portfolio;
import com.collective2.signalEntry.adapter.dynamicSimulator.quantity.QuantityComputable;
import com.collective2.signalEntry.implementation.Action;
import com.collective2.signalEntry.implementation.RelativeNumber;

import java.math.BigDecimal;

public class LimitOrder extends OrderSignal {

    RelativeNumber relativeLimit;

    public LimitOrder(int id, long time, Instrument instrument, String symbol, RelativeNumber limit, Action action, QuantityComputable quantityComputable, long cancelAtMs, Duration duration) {
        super(id, time, instrument, symbol, action, quantityComputable, cancelAtMs, duration);
        this.relativeLimit = limit;
    }

    @Override
    public boolean process(DataProvider dataProvider, Portfolio portfolio, BigDecimal commission) {

        //return true will move this to the processed list and
        //will cancel everything in the same OCA group if this order is in an OCA group.

        if (!isConditionProcessed()) {
            return false;
        }

        long time = dataProvider.endingTime();
        if (processed || cancel || time>cancelAtMs) {
            //cancel instead of submit order
            //still return true but no need to add any transaction to the portfolio
            return true;
        } else {

            BigDecimal absoluteLimit = absolutePrice(relativeLimit,dataProvider,portfolio);

            switch(action) {
                case BTC:
                case BTO:
                    //must only buy if we can do it under absoluteLimit.
                    if (dataProvider.wasOpen()) {
                        if (absoluteLimit.compareTo(dataProvider.lowPrice(symbol))<0) {
                            return false;// do not trigger can not get good deal under limit price.
                        }

                        BigDecimal price;
                        if (absoluteLimit.compareTo(dataProvider.highPrice(symbol))>0) {
                            //limit is above high so anything between low and high is ok
                            price = priceSelection(dataProvider.lowPrice(symbol), dataProvider.highPrice(symbol));
                        } else {
                            //limit is between low and high so only values between low and limit are ok
                            price = priceSelection(dataProvider.lowPrice(symbol), absoluteLimit);
                        }
                        //using the price used for the transaction determine the right quantity
                        Integer quantity = quantityComputable.quantity(price, portfolio, dataProvider, conditionalUpon());
                        entryQuantity(quantity);
                        //create the transaction in the portfolio
                        portfolio.position(symbol).addTransaction(quantity, time, price, commission);

                        processed = true;
                        return true;

                    } else {
                        //only look at most recent price, dataProvider was not open.
                        BigDecimal price = dataProvider.endingPrice(symbol);
                        if (absoluteLimit.compareTo(price)<0) {
                            return false;// do not trigger can not get good deal under limit price.
                        } else {
                            //limit is above or equal to last price so it can be used.
                            //using the price used for the transaction determine the right quantity
                            Integer quantity = quantityComputable.quantity(price, portfolio, dataProvider, conditionalUpon());
                            entryQuantity(quantity);
                            //create the transaction in the portfolio
                            portfolio.position(symbol).addTransaction(quantity, time, price, commission);
                            processed = true;
                            return true;
                        }
                    }
                case SSHORT:
                case STC:
                case STO:
                    //must only buy if we can do it under absoluteLimit.
                    if (dataProvider.wasOpen()) {  //sell at price higher than limit if limit>high this is not possible
                        if (absoluteLimit.compareTo(dataProvider.highPrice(symbol))>0) {
                            return false;// do not trigger can not get good deal under limit price.
                        }

                        BigDecimal price;
                        if (absoluteLimit.compareTo(dataProvider.lowPrice(symbol))<0) {
                            //limit is below low so anything between low and high is ok
                            price = priceSelection(dataProvider.highPrice(symbol), dataProvider.lowPrice(symbol));
                        } else {
                            //limit is between low and high so only values between high and limit are ok
                            price = priceSelection(dataProvider.highPrice(symbol), absoluteLimit);
                        }
                        //using the price used for the transaction determine the right quantity
                        Integer quantity = quantityComputable.quantity(price, portfolio, dataProvider, conditionalUpon());
                        entryQuantity(quantity);
                        //create the transaction in the portfolio
                        portfolio.position(symbol).addTransaction(-quantity, time, price, commission);
                        processed = true;
                        return true;

                    } else {
                        //only look at most recent price, dataProvider was not open.
                        BigDecimal price = dataProvider.endingPrice(symbol);
                        if (absoluteLimit.compareTo(price)>0) {
                            return false;// do not trigger can not get sell deal above limit price.
                        } else {
                            //limit is above or equal to last price so it can be used.
                            //using the price used for the transaction determine the right quantity
                            Integer quantity = quantityComputable.quantity(price, portfolio, dataProvider, conditionalUpon());
                            entryQuantity(quantity);
                            //create the transaction in the portfolio
                            portfolio.position(symbol).addTransaction(-quantity, time, price, commission);
                            processed = true;
                            return true;
                        }
                    }
                default:
                    throw new UnsupportedOperationException("Unsupported action:"+action);

            }
        }
    }

}
