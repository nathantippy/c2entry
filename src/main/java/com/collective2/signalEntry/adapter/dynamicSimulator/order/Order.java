/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  7/29/12
 */
package com.collective2.signalEntry.adapter.dynamicSimulator.order;

import com.collective2.signalEntry.ActionForNonStock;
import com.collective2.signalEntry.Duration;
import com.collective2.signalEntry.Instrument;
import com.collective2.signalEntry.adapter.dynamicSimulator.DataProvider;
import com.collective2.signalEntry.adapter.dynamicSimulator.portfolio.Portfolio;
import com.collective2.signalEntry.adapter.dynamicSimulator.quantity.QuantityComputable;
import com.collective2.signalEntry.implementation.Action;
import com.collective2.signalEntry.implementation.RelativeNumber;

import java.math.BigDecimal;
import java.util.Date;


public class Order implements Comparable<Order> {
    //private final Request request;
    protected final int id;
    protected final long time;
    private String comment;
    protected boolean cancel;
    protected boolean closed;
    protected boolean processed;
    protected Order conditionalUpon;
    private int entryQuantity;
    private final Duration timeInForce;
    private final long expireAtMs;
    protected final Action action;

    protected final Instrument instrument;
    protected final QuantityComputable quantityComputable;
    protected final OrderProcessor processor;

    protected PriceSelector priceSelector;

    protected Integer oneCancelsAnother;

    private Date postedWhen;  //TODO: SET WITH SCHEDULED
    private Date eMailedWhen; //TODO: SET WITH???
    private Date killedWhen;  //TODO: SET WITH CANCEL
    private Date tradedWhen;  //TODO: SET WITH ENTRY QUANTTY

    //ASSUMPTION: MARKETS ARE EVER OPEN LONGER THAN 12 HOURS
    private static final long ONE_TRADING_DAY = 60000l*60*36;


    public Order(int id, long time, Instrument instrument, String symbol,
                 Action action, QuantityComputable quantityComputable,
                 long cancelAtMs, Duration timeInForce,
                 OrderProcessor processor) {
        this.id = id;
        this.time = time;
        this.expireAtMs = cancelAtMs;
        this.timeInForce = timeInForce;
        this.action = action;

        this.instrument = instrument;
        this.quantityComputable = quantityComputable;
        this.priceSelector = PriceSelector.DEFAULT;
        this.processor = processor;
    }

    public long time() {
        return time;
    }

    public Order conditionalUpon() {
        return conditionalUpon;
    }

    public void conditionalUpon(Order conditionalUpon) {
        assert(this.conditionalUpon==null) : "only supports a single condition";
        this.conditionalUpon = conditionalUpon;
    }

    public boolean isConditionProcessed() {
        //clear to continue if conditional is filled, cancelled or expired
        return (conditionalUpon == null) || (!conditionalUpon.isPending());
    }

    public boolean isInForce(long now) {
        //TODO: wrong must start at open of market time not yesterday close!

        return timeInForce==Duration.GoodTilCancel || (Duration.DayOrder==timeInForce && now<(time + ONE_TRADING_DAY));
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
        if (time != signal.time) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (int) (time ^ (time >>> 32));
        return result;
    }

    @Override
    public int compareTo(Order that) {
        int result = Long.compare(this.time, that.time);
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

    public void cancelOrder() {
        cancel = true;
    }

    public void closeOrder() {
        closed = true;
    }

    public boolean isPending() {
        //not filled, cancelled or expired
        return (!cancel)&&(!processed);
    }

    public Integer entryQuantity() {
        return entryQuantity;
    }

    public void entryQuantity(Integer quantity) {
        entryQuantity = quantity;
    }

    public boolean isClosed() {
        return cancel || closed;
    }

    public String postedWhen() {
        return postedWhen==null?"":postedWhen.toString();
    }

    public String eMailedWhen() {
        return eMailedWhen==null?"":eMailedWhen.toString();
    }

    public String killedWhen() {
        return killedWhen==null?"":killedWhen.toString();
    }

    public String tradedWhen() {
        return tradedWhen==null?"":tradedWhen.toString();
    }

    public BigDecimal tradePrice() {
        return BigDecimal.ZERO; //TODO: SET WITH ENTRY QUANTTY
    }

    public int quantity() {
        return (conditionalUpon==null?entryQuantity():conditionalUpon.entryQuantity());
    }

    public String symbol() {
        return processor.symbol();
    }

    public Duration timeInForce() {
        return timeInForce;
    }

    public Action action() {
        return action;
    }

    public void setPriceSelector(PriceSelector priceSelector) {
        this.priceSelector = priceSelector;
    }

    protected BigDecimal priceSelection(BigDecimal bestPrice, BigDecimal worstPrice, boolean firstIsTrigger) {
        return priceSelector.select(bestPrice, worstPrice, firstIsTrigger);
    }


    public void oneCancelsAnother(Integer ocaId) {
        this.oneCancelsAnother = ocaId;
    }

    public Integer oneCancelsAnother() {
        return oneCancelsAnother;
    }

    public boolean process(DataProvider dataProvider, Portfolio portfolio, BigDecimal commission) {

        if (processed || cancel) {
            //cancel instead of submit order
            //still return true but no need to add any transaction to the portfolio
            return true;
        }

    //    Integer quantity = quantityComputable.quantity(price, portfolio, dataProvider, conditionalUpon());
     //   if (quantity.intValue()==0) {
      //      return true;
       // }

        return processor.process(dataProvider,portfolio,commission,this, action, quantityComputable);
    }

    //TODO: change to return type and single trigger price.
    public RelativeNumber limit() {
        return processor instanceof OrderProcessorLimit ? processor.triggerPrice()  : new RelativeNumber();
    }

    public RelativeNumber stop() {
        return processor instanceof OrderProcessorStop ? processor.triggerPrice()  : new RelativeNumber();
    }

    public RelativeNumber market() {
        return processor instanceof OrderProcessorMarket ? processor.triggerPrice()  : new RelativeNumber();
    }
}
