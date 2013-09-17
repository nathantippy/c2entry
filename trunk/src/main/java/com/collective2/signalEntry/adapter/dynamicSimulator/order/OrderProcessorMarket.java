package com.collective2.signalEntry.adapter.dynamicSimulator.order;

import com.collective2.signalEntry.adapter.dynamicSimulator.DataProvider;
import com.collective2.signalEntry.adapter.dynamicSimulator.portfolio.Portfolio;
import com.collective2.signalEntry.adapter.dynamicSimulator.quantity.QuantityComputable;
import com.collective2.signalEntry.implementation.SignalAction;
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
    private Integer transactionQuantity = 0;
    private BigDecimal transactionPrice = BigDecimal.ZERO;

    public OrderProcessorMarket(long time, String symbol) {
        this.time = time;
        this.symbol = symbol;
    }

    public String symbol() {
        return symbol;
    }

    public String toString() {
        return "MarketOrder transactionPrice:"+transactionPrice+" transactionQuanity:"+transactionQuantity;
    }

    public long time() {
        return time;
    }

    public BigDecimal transactionPrice() {
        return transactionPrice;
    }

    public Integer transactionQuantity() {
        return transactionQuantity;
    }

    public boolean process(DataProvider dataProvider, Portfolio portfolio, BigDecimal commission, Order order, SignalAction action,
                           QuantityComputable quantityComputable, DataProvider dayOpenData) {
            logger.trace("process MarketOrder");


            BigDecimal myOpenPrice;

            //if my conditional upon was today we can never use open because the time
            // for it has already gone by. For and open we must use
            //the transaction price of our conditional upon order
            if (null != order.conditionalUpon() && order.conditionalUpon().isTradedThisSession(dataProvider)) {
                myOpenPrice = order.conditionalUpon().tradePrice();
                assert(myOpenPrice.compareTo(BigDecimal.ZERO)>0) : "Data error: open price must not be negative.";
            }
                //do not allow new open position if the price has dropped to zero
                myOpenPrice = dataProvider.openingPrice(symbol);
                if (BigDecimal.ZERO.compareTo(myOpenPrice)>=0 && (SignalAction.BTO == action || SignalAction.STO == action) ) {
                    logger.trace("missing opening price for "+symbol()+" on "+new Date(dataProvider.startingTime())+" "+dataProvider.startingTime());
                    order.cancelOrder(dataProvider.startingTime());
                    return true;
                }


            Integer quantity = quantityComputable.quantity(myOpenPrice,dataProvider);
            if (quantity.intValue()==0){
                System.err.println("no quantity so transaction not set");

                return true;
            }
            transactionPrice = myOpenPrice; //only set transaction price if the transaction is executed
            transactionQuantity = quantity;

            switch(action) {
                case BTC:
                    if (order.conditionalUpon()==null && portfolio.position(symbol).quantity().intValue()==0) {
                        order.cancelOrder(dataProvider.startingTime());
                        return true;
                    }
                    //close order but make sure its not already been closed
                    if (order.conditionalUpon()!=null){
                        if ((order.conditionalUpon().isProcessed() && order.conditionalUpon().isClosed()) || order.conditionalUpon().isCancel()) {
                            order.cancelOrder(dataProvider.startingTime());
                            return true;
                        }
                    }
                case BTO:
                    portfolio.position(symbol).addTransaction(quantity, time, transactionPrice, commission, SignalAction.BTC==action);
                    if (action== SignalAction.BTC && order.conditionalUpon()!=null) {
                        order.conditionalUpon().closeOrder();
                    }
                    order.processed = true;
                    break;
                case STC:
                    if (order.conditionalUpon()==null && portfolio.position(symbol).quantity().intValue()==0) {
                        order.cancelOrder(dataProvider.startingTime());
                        return true;
                    }
                    //close order but make sure its not already been closed
                    if (order.conditionalUpon()!=null) {
                        if ((order.conditionalUpon().isProcessed() && order.conditionalUpon().isClosed()) || order.conditionalUpon().isCancel()) {
                            order.cancelOrder(dataProvider.startingTime());
                            return true;
                        }
                    }
                case SSHORT:
                case STO:
                    portfolio.position(symbol).addTransaction(-quantity, time, transactionPrice, commission, SignalAction.STC==action);

                    if (action== SignalAction.STC && order.conditionalUpon()!=null) {
                        order.conditionalUpon().closeOrder();
                    }
                    order.processed = true;
                    break;

            }
            order.processed = true;
            return true;

    }

    @Override
    public BigDecimal triggerPrice() {
        return transactionPrice;
    }
}
