package com.collective2.signalEntry.adapter.dynamicSimulator.order;

import com.collective2.signalEntry.C2ServiceException;
import com.collective2.signalEntry.adapter.dynamicSimulator.DataProvider;
import com.collective2.signalEntry.adapter.dynamicSimulator.portfolio.Portfolio;
import com.collective2.signalEntry.implementation.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  8/26/12
 */

public class OrderProcessorMarket implements OrderProcessor {

    private static final Logger logger = LoggerFactory.getLogger(OrderProcessorMarket.class);

    @Override
    public boolean process(DataProvider dataProvider, Portfolio portfolio, BigDecimal commission, OrderSignal order) {
        logger.trace("process MarketOrder");

        if (order.processed || order.cancel) {
            //cancel instead of submit order
            //still return true but no need to add any transaction to the portfolio
            return true;
        } else {

            BigDecimal marketPrice = dataProvider.endingPrice(order.symbol);

            Integer quantity = order.quantityComputable.quantity(marketPrice, portfolio, dataProvider, order.conditionalUpon());
            if (quantity.intValue()==0) {
                return true;
            }
            order.entryQuantity(quantity);

            switch(order.action) {
                case BTC:
                    if (order.conditionalUpon()==null && portfolio.position(order.symbol).quantity().intValue()==0) {
                        throw new C2ServiceException("BuyToClose requires conditional open order",false);
                    }
                    //close order but make sure its not already been closed
                    if (order.conditionalUpon()!=null){
                        if ((order.conditionalUpon().isProcessed() && order.conditionalUpon().isClosed()) || order.conditionalUpon().cancel) {
                            order.cancelOrder();
                            return true;
                        }
                    }
                case BTO:
                    portfolio.position(order.symbol).addTransaction(quantity, order.time, marketPrice, commission, Action.BTC==order.action);
                    if (order.action==Action.BTC && order.conditionalUpon()!=null) {
                        order.conditionalUpon().closeOrder();
                    }
                    order.processed = true;
                    break;
                case STC:
                    if (order.conditionalUpon()==null && portfolio.position(order.symbol).quantity().intValue()==0) {
                        throw new C2ServiceException("ShortToClose requires conditional open order",false);
                    }
                    //close order but make sure its not already been closed
                    if (order.conditionalUpon()!=null) {
                        if ((order.conditionalUpon().isProcessed() && order.conditionalUpon().isClosed()) || order.conditionalUpon().cancel) {
                            order.cancelOrder();
                            return true;
                        }
                    }
                case SSHORT:
                case STO:
                    portfolio.position(order.symbol).addTransaction(-quantity, order.time, marketPrice, commission, Action.STC==order.action);

                    if (order.action==Action.STC && order.conditionalUpon()!=null) {
                        order.conditionalUpon().closeOrder();
                    }
                    order.processed = true;
                    break;

            }
            order.processed = true;
            return true;
        }
    }
}
