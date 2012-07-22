/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/8/12
 */
package com.collective2.signalEntry.implementation;

import java.io.Serializable;

import com.collective2.signalEntry.BasePrice;

public class RelativeNumber implements Serializable {

    private final Number number;
    private final String prefix;

    public RelativeNumber(BasePrice base, Number value) {

        prefix = base.prefix();
        number = value;

    }

    public String toString() {

        //This is encoded when the url is built
        //Plus  %2B
        //Minus %2D

        String value = number.toString();
        if (prefix.isEmpty()) { //only for BasePrice.Absolute
            return value;
        } else {
            if (value.startsWith("-")) {
                return prefix+"%2D"+value.substring(1);
            } else {
                return prefix+"%2B"+value;
            }
        }
    }


}
