/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  8/4/12
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

public abstract class OrderSignal extends Order {

    protected final Instrument instrument;
    protected final Action action;
    protected final QuantityComputable quantityComputable;
    protected PriceSelector priceSelector;
    protected final long cancelAtMs;
    protected final Duration duration;
    protected Integer oneCancelsAnother;

    public OrderSignal(int id, long time, Instrument instrument, String symbol, Action action, QuantityComputable quantityComputable, long cancelAtMs, Duration duration) {
        super(id, time, symbol);
        this.instrument = instrument;
        this.action = action;
        this.quantityComputable = quantityComputable;
        this.cancelAtMs = cancelAtMs;
        this.duration = duration;
        this.priceSelector = PriceSelector.DEFAULT;
    }

    public void setPriceSelector(PriceSelector priceSelector) {
        this.priceSelector = priceSelector;
    }

    protected BigDecimal absolutePrice(RelativeNumber relativeLimit, DataProvider dataProvider, Portfolio portfolio) {

        //Q use most recent quote which is dataProvider.endingPrice() - its the best we have
        //O use day open price, use open price if market was open else use end price..
        //T fill price of the open buy/sell  portfolio.position(symbol).openPrice();

        switch (relativeLimit.prefix()) {
            case 'Q':
                return dataProvider.endingPrice(symbol).add(relativeLimit.value());
            case 'O':
                if (dataProvider.wasOpen()) {
                    return dataProvider.openingPrice(symbol).add(relativeLimit.value());
                } else {
                    return dataProvider.endingPrice(symbol).add(relativeLimit.value());
                }
            case 'T':
                return portfolio.position(symbol).openPrice().add(relativeLimit.value());
            default:
                return relativeLimit.value();
        }
    }

    protected BigDecimal priceSelection(BigDecimal bestPrice, BigDecimal worstPrice) {
        return priceSelector.select(bestPrice, worstPrice);
    }


    public void oneCancelsAnother(Integer ocaId) {
        this.oneCancelsAnother = ocaId;
    }

    public Integer oneCancelsAnother() {
        return oneCancelsAnother;
    }

    public void stopLoss(RelativeNumber stopLoss) {

    }

    public void profitTarget(RelativeNumber profitTarget) {

    }

    public void forceNoOCA(Integer forceNoOCA) {

    }

    public void xReplace(Integer xReplace) {

    }
}
