/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/6/12
 */
package com.collective2.signalEntry;

import com.collective2.signalEntry.implementation.SignalAction;

public enum ActionForStock {
    BuyToOpen(SignalAction.BTO), SellShort(SignalAction.SSHORT), BuyToClose(SignalAction.BTC), SellToClose(SignalAction.STC);

    final private SignalAction value;

    ActionForStock(SignalAction value) {
        this.value = value;
    }

    public SignalAction action(){
        return this.value;
    }

    public String toString() {
        return value.name();
    }
}
