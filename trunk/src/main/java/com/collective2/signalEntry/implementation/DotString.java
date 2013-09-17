/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/15/12
 */
package com.collective2.signalEntry.implementation;

import java.io.Serializable;
import java.util.Arrays;

public class DotString<T extends Serializable> implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 5533962589955858983L;
    final T[] data;

    public DotString(T ... data) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DotString<?> dotString = (DotString<?>) o;

        if (!Arrays.equals(data, dotString.data)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return data != null ? Arrays.hashCode(data) : 0;
    }
}
