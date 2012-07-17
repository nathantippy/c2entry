/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/15/12
 */
package com.collective2.signalEntry.implementation;

import java.io.Serializable;

public class DotString<T extends Serializable> implements Serializable {

    final T[] data;

    public DotString(T[] data) {
        this.data = data;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (T item : data) {
            builder.append(item.toString()).append('.');
        }
        int len = builder.length();
        if (len > 0) {
            builder.setLength(len - 1);
        }
        return builder.toString();
    }

}
