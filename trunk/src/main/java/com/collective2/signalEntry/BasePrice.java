/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/6/12
 */
package com.collective2.signalEntry;

public enum BasePrice {
    SessionOpenPlus('O'), PositionOpenPlus('T'), RTQuotePlus('Q'), Absolute(' ');

    private final char prefix;

    BasePrice(char prefix) {
        this.prefix = prefix;
    }

    public char prefix() {
        return prefix;
    }
}
