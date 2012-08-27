/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  7/29/12
 */
package com.collective2.signalEntry.adapter.dynamicSimulator.order;

import com.collective2.signalEntry.ActionForNonStock;
import com.collective2.signalEntry.Duration;
import com.collective2.signalEntry.adapter.dynamicSimulator.DataProvider;
import com.collective2.signalEntry.adapter.dynamicSimulator.portfolio.Portfolio;
import com.collective2.signalEntry.implementation.Action;

import java.math.BigDecimal;
import java.util.Date;


public abstract class Order implements Comparable<Order> {
    //private final Request request;
    protected final int id;
    protected final long time;
    private String comment;
    protected final String symbol;
    protected boolean cancel;
    protected boolean closed;
    protected boolean processed;
    protected Order conditionalUpon;
    private int entryQuantity;
    private final Duration timeInForce;
    private final long expireAtMs;
    protected final Action action;

    private Date postedWhen;  //TODO: SET WITH SCHEDULED
    private Date eMailedWhen; //TODO: SET WITH???
    private Date killedWhen;  //TODO: SET WITH CANCEL
    private Date tradedWhen;  //TODO: SET WITH ENTRY QUANTTY

    //ASSUMPTION: MARKETS ARE EVER OPEN LONGER THAN 12 HOURS
    private static final long ONE_TRADING_DAY = 60000l*60*36;

    public Order(int id, long time, String symbol, long cancelAtMs, Duration timeInForce, Action action) {
        this.id = id;
        this.time = time;
        this.symbol = symbol;
        this.expireAtMs = cancelAtMs;
        this.timeInForce = timeInForce;
        this.action = action;
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

    public abstract boolean process(DataProvider dataProvider, Portfolio portfolio, BigDecimal commission);

    public Integer oneCancelsAnother() {
        return null;
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
        return postedWhen.toString();
    }

    public String eMailedWhen() {
        return eMailedWhen.toString();
    }

    public String killedWhen() {
        return killedWhen.toString();
    }

    public String tradedWhen() {
        return tradedWhen.toString();
    }

    public BigDecimal tradePrice() {
        return BigDecimal.ZERO; //TODO: SET WITH ENTRY QUANTTY
    }

    public int quantity() {
        return (conditionalUpon==null?entryQuantity():conditionalUpon.entryQuantity());
    }

    public BigDecimal limit() {
        return BigDecimal.ZERO;  //TODO: override by right order
    }

    public BigDecimal stop() {
        return BigDecimal.ZERO;  //TODO: override by right order
    }

    public BigDecimal market() {
        return BigDecimal.ZERO; //TODO: override by right order
    }

    public String symbol() {
        return symbol;
    }

    public Duration timeInForce() {
        return timeInForce;
    }

    public Action action() {
        return action;
    }
}
