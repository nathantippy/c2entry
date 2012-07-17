/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/6/12
 */
package com.collective2.signalEntry;

public enum BasePrice {
    Opening("O"), TradeFill("T"), QuoteNow("Q"), Absolute("");

    private final String prefix;

    BasePrice(String prefix) {
        this.prefix = prefix;
    }

    public String prefix() {
        return prefix;
    }
}
