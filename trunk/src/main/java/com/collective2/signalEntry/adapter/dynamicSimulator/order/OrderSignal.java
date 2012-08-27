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
import com.collective2.signalEntry.adapter.dynamicSimulator.portfolio.Portfolio;
import com.collective2.signalEntry.adapter.dynamicSimulator.quantity.QuantityComputable;
import com.collective2.signalEntry.implementation.Action;

import java.math.BigDecimal;

public class OrderSignal extends Order {

    protected final Instrument instrument;
    protected final QuantityComputable quantityComputable;
    protected final OrderProcessor processor;

    protected PriceSelector priceSelector;

    protected Integer oneCancelsAnother;

    public OrderSignal(int id, long time, Instrument instrument, String symbol,
                       Action action, QuantityComputable quantityComputable, long cancelAtMs, Duration timeInForce, OrderProcessor processor) {
        super(id, time, symbol, cancelAtMs, timeInForce, action);
        this.instrument = instrument;
        this.quantityComputable = quantityComputable;
        this.priceSelector = PriceSelector.DEFAULT;
        this.processor = processor;
    }

    public void setPriceSelector(PriceSelector priceSelector) {
        this.priceSelector = priceSelector;
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

    @Override
    public boolean process(DataProvider dataProvider, Portfolio portfolio, BigDecimal commission) {
        return processor.process(dataProvider,portfolio,commission,this);
    }

}
