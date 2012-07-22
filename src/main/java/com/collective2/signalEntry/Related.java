/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/16/12
 */
package com.collective2.signalEntry;

public enum Related {
    Children("children"), Parent("parent");
    
    private final String value;
    
    Related(String value) {
        this.value = value;
    }
    
    public String toString() {
        return value;
    }
    
}
