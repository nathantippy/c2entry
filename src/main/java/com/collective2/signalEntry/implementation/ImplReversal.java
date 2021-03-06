/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/6/12
 */
package com.collective2.signalEntry.implementation;

import com.collective2.signalEntry.Duration;
import com.collective2.signalEntry.Parameter;
import com.collective2.signalEntry.Reverse;

import java.math.BigDecimal;

public abstract class ImplReversal extends AbstractCommand implements Reverse {

    public Reverse duration(Duration duration) {
        return new ReversalLeaf(this, Parameter.TimeInForce, duration);
    }

    public Reverse triggerPrice(BigDecimal value) {
        return  new ReversalLeaf(this, Parameter.TriggerPrice, value);
    }

    public Reverse quantity(Integer value) {
        return new ReversalLeaf(this, Parameter.Quantity, value);
    }
}
