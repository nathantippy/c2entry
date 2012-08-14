/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/6/12
 */
package com.collective2.signalEntry;

import com.collective2.signalEntry.implementation.Action;

public enum ActionForStock {
    BuyToOpen(Action.BTO), SellShort(Action.SSHORT), BuyToClose(Action.BTC), SellToClose(Action.STC);

    final private Action value;

    ActionForStock(Action value) {
        this.value = value;
    }

    public Action action(){
        return this.value;
    }

    public String toString() {
        return value.name();
    }
}
