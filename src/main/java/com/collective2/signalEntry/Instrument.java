/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  7/28/12
 */
package com.collective2.signalEntry;


public enum Instrument {
    Stock("stock"), Option("option"), Future("future"), Forex("forex");

    final private String value;

    Instrument(String value) {
        this.value = value;
    }

    public String toString() {
        return value;
    }
}
