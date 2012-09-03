package com.collective2.signalEntry.adapter.dynamicSimulator.order;

import com.collective2.signalEntry.BasePrice;
import com.collective2.signalEntry.C2ServiceException;
import com.collective2.signalEntry.adapter.dynamicSimulator.DataProvider;
import com.collective2.signalEntry.adapter.dynamicSimulator.portfolio.Portfolio;
import com.collective2.signalEntry.adapter.dynamicSimulator.quantity.QuantityComputable;
import com.collective2.signalEntry.implementation.Action;
import com.collective2.signalEntry.implementation.RelativeNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Date;

/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  8/26/12
 */

public class OrderProcessorMarket implements OrderProcessor {

    private static final Logger logger = LoggerFactory.getLogger(OrderProcessorMarket.class);
    private final String symbol;
    private final long time;
    private BigDecimal transactionPrice;

    public OrderProcessorMarket(long time, String symbol) {
        this.time = time;
        this.symbol = symbol;
    }

    public String symbol() {
        return symbol;
    }

    public long time() {
        return time;
    }

    public BigDecimal transactionPrice() {
        return transactionPrice;
    }

    public boolean process(DataProvider dataProvider, Portfolio portfolio, BigDecimal commission, Order order, Action action,
                           QuantityComputable quantityComputable) {
        logger.trace("process MarketOrder");

            transactionPrice = dataProvider.openingPrice(symbol);
            if (transactionPrice.doubleValue()==0d) {
                logger.warn("missing opening price for "+symbol()+" on "+new Date(dataProvider.openingTime())+" "+dataProvider.openingTime());
                return true;
            }

            Integer quantity = quantityComputable.quantity(transactionPrice,dataProvider);
            if (quantity.intValue()==0){
                return true;
            }

            order.entryQuantity(quantity);

            switch(action) {
                case BTC:
                    if (order.conditionalUpon()==null && portfolio.position(symbol).quantity().intValue()==0) {
                        throw new C2ServiceException("BuyToClose requires conditional open order",false);
                    }
                    //close order but make sure its not already been closed
                    if (order.conditionalUpon()!=null){
                        if ((order.conditionalUpon().isProcessed() && order.conditionalUpon().isClosed()) || order.conditionalUpon().isCancel()) {
                            order.cancelOrder(dataProvider.openingTime());
                            return true;
                        }
                    }
                case BTO:
                    portfolio.position(symbol).addTransaction(quantity, time, transactionPrice, commission, Action.BTC==action);
                    if (action==Action.BTC && order.conditionalUpon()!=null) {
                        order.conditionalUpon().closeOrder();
                    }
                    order.processed = true;
                    break;
                case STC:
                    if (order.conditionalUpon()==null && portfolio.position(symbol).quantity().intValue()==0) {
                        throw new C2ServiceException("ShortToClose requires conditional open order",false);
                    }
                    //close order but make sure its not already been closed
                    if (order.conditionalUpon()!=null) {
                        if ((order.conditionalUpon().isProcessed() && order.conditionalUpon().isClosed()) || order.conditionalUpon().isCancel()) {
                            order.cancelOrder(dataProvider.openingTime());
                            return true;
                        }
                    }
                case SSHORT:
                case STO:
                    portfolio.position(symbol).addTransaction(-quantity, time, transactionPrice, commission, Action.STC==action);

                    if (action==Action.STC && order.conditionalUpon()!=null) {
                        order.conditionalUpon().closeOrder();
                    }
                    order.processed = true;
                    break;

            }
            order.processed = true;
            return true;

    }

    @Override
    public RelativeNumber triggerPrice() {
        return new RelativeNumber(BasePrice.Absolute,BigDecimal.ZERO);
    }
}
