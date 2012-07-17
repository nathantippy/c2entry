/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/6/12
 */
package com.collective2.signalEntry;

public enum ActionForStock {
    BuyToOpen("BTO"), SellShort("SSHORT"), BuyToClose("BTC"), SellToClose("STC");

    final private String value;

    ActionForStock(String value) {
        this.value = value;
    }

    public String toString() {
        return value;
    }
}
