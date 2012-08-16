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

import java.math.BigDecimal;

public class MarketOrder extends OrderSignal {

    public MarketOrder(int id, long time, Instrument instrument, String symbol, Action action, QuantityComputable quantityComputable, long cancelAtMs, Duration duration) {
        super(id, time, instrument, symbol, action, quantityComputable, cancelAtMs, duration);
    }

    @Override
    public boolean process(DataProvider dataProvider, Portfolio portfolio, BigDecimal commission) {

        if (!isConditionProcessed()) {
            return false;
        }

        long time = dataProvider.endingTime();
        if (processed || cancel || time>cancelAtMs) {
            //cancel instead of submit order
            //still return true but no need to add any transaction to the portfolio
            return true;
        } else {

            BigDecimal marketPrice = dataProvider.endingPrice(symbol);

            Integer quantity = quantityComputable.quantity(marketPrice, portfolio, dataProvider, conditionalUpon());
            entryQuantity(quantity);

            switch(action) {
                case BTC:
                case BTO:
                    portfolio.position(symbol).addTransaction(quantity, time, marketPrice, commission);
                    break;
                case SSHORT:
                case STC:
                case STO:
                    portfolio.position(symbol).addTransaction(-quantity, time, marketPrice, commission);
                    break;

            }
            processed = true;
            return true;
        }
    }
}
