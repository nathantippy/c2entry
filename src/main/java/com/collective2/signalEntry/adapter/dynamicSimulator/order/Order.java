/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  7/29/12
 */
package com.collective2.signalEntry.adapter.dynamicSimulator.order;

import com.collective2.signalEntry.adapter.dynamicSimulator.DataProvider;
import com.collective2.signalEntry.adapter.dynamicSimulator.Portfolio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public abstract class Order implements Comparable<Order> {
    //private final Request request;
    protected final int id;
    protected final long time;
    private String comment;
    protected final String symbol;
    protected boolean cancel;
    protected boolean processed;
    protected Order conditionalUpon;
    private Integer entryQuantity;

    public Order(int id, long time, String symbol) {
        this.id = id;
        this.time = time;
        this.symbol = symbol;
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

    protected boolean isConditionProcessed() {
        return (conditionalUpon == null) || (conditionalUpon.isProcessed());
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

    public void cancel() {
        cancel = true;
    }

    public Integer entryQuantity() {
        return entryQuantity;
    }

    public void entryQuantity(Integer quantity) {
        entryQuantity = quantity;
    }
}
