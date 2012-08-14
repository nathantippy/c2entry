package com.collective2.signalEntry;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  8/4/12
 */

public class Money {

    public static BigDecimal USD(String value) {
        return new BigDecimal(value);
    }                          //TODO: needs test if we keep it

    public static BigDecimal USD(Number value) {
        return new BigDecimal(value.doubleValue(),new MathContext(2));
    }
}
