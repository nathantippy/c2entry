/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/6/12
 */
package com.collective2.signalEntry;

import com.collective2.signalEntry.implementation.SignalAction;

public enum ActionForNonStock {
    BuyToOpen(SignalAction.BTO), SellToOpen(SignalAction.STO), BuyToClose(SignalAction.BTC), SellToClose(SignalAction.STC);

    final private SignalAction value;

    ActionForNonStock(SignalAction value) {
        this.value = value;
    }

    public SignalAction action(){
        return this.value;
    }

    public String toString() {
        return value.name();
    }

    public static ActionForNonStock lookupEnum(String stringValue) {

        for(ActionForNonStock e:values()) {
            if (e.toString().equalsIgnoreCase(stringValue)) {
                return e;
            }
        }
        throw new C2ServiceException("Unable to find:"+stringValue+" in "+values().getClass(),false);
    }
}
