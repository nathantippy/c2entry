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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public class MarketOrder extends OrderSignal {

    private static final Logger logger = LoggerFactory.getLogger(MarketOrder.class);

    public MarketOrder(int id, long time, Instrument instrument, String symbol, Action action, QuantityComputable quantityComputable, long cancelAtMs, Duration duration) {
        super(id, time, instrument, symbol, action, quantityComputable, cancelAtMs, duration);
    }

    @Override
    public boolean process(DataProvider dataProvider, Portfolio portfolio, BigDecimal commission) {
        logger.trace("process MarketOrder");

        if (processed || cancel) {
            //cancel instead of submit order
            //still return true but no need to add any transaction to the portfolio
            return true;
        } else {

            BigDecimal marketPrice = dataProvider.endingPrice(symbol);

            Integer quantity = quantityComputable.quantity(marketPrice, portfolio, dataProvider, conditionalUpon());
            entryQuantity(quantity);

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
                    portfolio.position(symbol).addTransaction(quantity, time, marketPrice, commission);
                    if (action==Action.BTC) {
                        conditionalUpon().closeOrder();
                    }
                    processed = true;
                    break;
                case STC:
                    if (conditionalUpon()==null) {
                        throw new C2ServiceException("ShortToClose requires conditional open order",false);
                    }
                    //close order but make sure its not already been closed
                    if ((conditionalUpon().isProcessed() && conditionalUpon().isClosed()) || conditionalUpon.cancel) {
                        cancelOrder();
                        return true;
                    }
                case SSHORT:
                case STO:
                    portfolio.position(symbol).addTransaction(-quantity, time, marketPrice, commission);

                    if (action==Action.STC) {
                        conditionalUpon().closeOrder();
                    }
                    processed = true;
                    break;

            }
            processed = true;
            return true;
        }
    }
}
