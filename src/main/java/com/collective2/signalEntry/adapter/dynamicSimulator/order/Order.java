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
import com.collective2.signalEntry.adapter.dynamicSimulator.portfolio.Portfolio;
import com.collective2.signalEntry.adapter.dynamicSimulator.quantity.QuantityComputable;
import com.collective2.signalEntry.implementation.SignalAction;
import java.math.BigDecimal;
import java.util.Date;

public class Order implements Comparable<Order> {
    //private final Request request;
    protected final int id;
    private String comment;
    private boolean cancel;
    protected boolean closed;
    protected boolean processed;
    protected final Order conditionalUpon;

 //   private int entryQuantity;

    private final Duration timeInForce;
    private final long expireAtMs;
    protected final SignalAction action;

    protected final Instrument instrument;
    protected final QuantityComputable quantityComputable;
    protected final OrderProcessor processor;

    protected Integer oneCancelsAnother;

    private final long postedWhen;
    private final long eMailedWhen;
    private long killedWhen;
    private long tradedWhen;

    public String toString() {
        return id+" "+processor.toString()+" transactionPrice:"+processor.transactionPrice()+" quantity:"+tradeQuantity();
    }

    public Order(int id, Instrument instrument, String symbol,
                 SignalAction action, QuantityComputable quantityComputable,
                 long cancelAtMs, Duration timeInForce,
                 OrderProcessor processor,
                 Order conditionalUpon) {
        this.id = id;
        this.expireAtMs = cancelAtMs;
        this.timeInForce = timeInForce;
        this.action = action;

        this.instrument = instrument;
        this.quantityComputable = quantityComputable;
        this.processor = processor;
        this.conditionalUpon = conditionalUpon;

        this.postedWhen = processor.time();
        this.eMailedWhen = processor.time();

        //If you attempt to add a new order and make it conditional on an order
        // that has already been filled or canceled, this will not be permitted.
        // The parent order must still be pending.
        if (conditionalUpon!=null && !conditionalUpon.isPending()) {
            throw new C2ServiceException("Can not be conditional on an order that is not pending.",false);
        }
    }

    public long time() {
        return processor.time();
    }

    public Order conditionalUpon() {
        return conditionalUpon;
    }

    public boolean isConditionProcessed() {
        //clear to continue if conditional is filled, cancelled or expired
        return (conditionalUpon == null) || (!conditionalUpon.isPending());
    }

    private long firstTimeAttempted;

    public boolean isInForce(long now, DataProvider dayMarketOpenData, DataProvider dayMarketCloseData) {
        if (0==firstTimeAttempted) {
            firstTimeAttempted = now;
        }
        return (timeInForce == Duration.GoodTilCancel) ||
               (dayMarketOpenData == null) ||
               (timeInForce == Duration.DayOrder &&
                (firstTimeAttempted>=dayMarketCloseData.startingTime()) &&
                (dayMarketCloseData==null || firstTimeAttempted<dayMarketCloseData.endingTime())
               );
    }

    public boolean isExpired(long now) {
        return (now> expireAtMs);
    }

    public int id() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Order signal = (Order) o;

        if (id != signal.id) return false;
        if (time() != signal.time()) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (int) (time() ^ (time() >>> 32));
        return result;
    }

    @Override
    public int compareTo(Order that) {
        int result = Long.compare(this.time(), that.time());
        if (result == 0) {
            //if two things happen at the same "time" then use the
            //order they were submitted to determine which is first.
            result = Integer.compare(this.id, that.id);
        }
        return result;
    }

    public String comment() {
        return comment;
    }

    public void comment(String comment) {
        this.comment = comment;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void cancelOrder(long time) {
        cancel = true;
        killedWhen = time;
    }

    public void closeOrder() {
        closed = true;
    }

    public boolean isPending() {
        if (processed) {
            if (tradeQuantity()<=0) {
                throw new C2ServiceException("bad quantity for processed "+tradeQuantity(),false);
            }
            if (tradePrice().compareTo(BigDecimal.ZERO)<=0) {
                throw new C2ServiceException("bad price for processed "+tradePrice(),false);
            }
            return false;
        }

        //not filled, cancelled or expired
        return (!cancel)&&(!processed)&&(conditionalUpon==null || !conditionalUpon.cancel);
    }

    public Integer tradeQuantity() {
        Integer result = processor.transactionQuantity();
        if (result==0) {
            //signal status request with quantity value can only work if the values are known
            BigDecimal price = null;
            DataProvider dataProvider = null;
            return  this.quantityComputable.quantity(price,dataProvider);

        }
        return result;
    }


    public BigDecimal tradePrice() {
        return processor.transactionPrice();
    }

    public boolean isClosed() {
        return cancel || closed;
    }

    public boolean isCancel() {
        return cancel;
    }

    public String postedWhen() {
        return 0==postedWhen?"":new Date(postedWhen).toString();
    }

    public String eMailedWhen() {
        return 0==eMailedWhen?"":new Date(eMailedWhen).toString();
    }

    public String killedWhen() {
        return 0==killedWhen?"":new Date(killedWhen).toString();
    }

    public String tradedWhen() {
        return 0==tradedWhen ? "" : new Date(tradedWhen).toString();
    }

    public int quantity() {
        return (conditionalUpon==null ? tradeQuantity() : conditionalUpon.tradeQuantity());
    }

    public String symbol() {
        return processor.symbol();
    }

    public Duration timeInForce() {
        return timeInForce;
    }

    public SignalAction action() {
        return action;
    }

    public void oneCancelsAnother(Integer ocaId) {
        this.oneCancelsAnother = ocaId;
    }

    public Integer oneCancelsAnother() {
        return oneCancelsAnother;
    }

    public boolean process(DataProvider dataProvider, Portfolio portfolio, BigDecimal commission, DataProvider dayOpen) {

        if (processed || cancel) {
            //cancel instead of submit order
            //still return true but no need to add any transaction to the portfolio
            return true;
        }
        //if this is a conditional order and that one has been canceled then cancel this one
        if (conditionalUpon!=null && conditionalUpon.isCancel()) {
            cancelOrder(dataProvider.startingTime());
            return true;
        }

        boolean result = processor.process(dataProvider,portfolio,commission,this, action, quantityComputable, dayOpen);
        if (result) {
            tradedWhen = dataProvider.startingTime(); //may be later but this is the best our simulator can do
        }
        return result;
    }

    public BigDecimal limit() {
        return processor instanceof OrderProcessorLimit ? processor.triggerPrice()  : BigDecimal.ZERO;
    }

    public BigDecimal stop() {
        return processor instanceof OrderProcessorStop ? processor.triggerPrice()  : BigDecimal.ZERO;
    }

    public BigDecimal market() {
        return processor instanceof OrderProcessorMarket ? processor.triggerPrice()  : BigDecimal.ZERO;
    }

    public boolean isTradedThisSession(DataProvider dataProvider) {
        return tradedWhen>=dataProvider.startingTime() && tradedWhen<dataProvider.endingTime();
    }
}
