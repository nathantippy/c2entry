/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/8/12
 */
package com.collective2.signalEntry.implementation;

import java.io.Serializable;
import java.math.BigDecimal;

import com.collective2.signalEntry.BasePrice;
import com.collective2.signalEntry.C2ServiceException;

public class RelativeNumber implements Serializable {

    private final Number number;
    private final String prefix;

    public RelativeNumber(String stringValue) {
        //parse
        switch(stringValue.charAt(0)) {
            case 'O':
            case 'T':;
            case 'Q':
                prefix = stringValue.substring(0,1);

                switch(stringValue.charAt(3)) {
                    case 'D': //negative
                        number = new BigDecimal(stringValue.substring(4)).negate();
                        break;
                    case 'B': //postive
                        number = new BigDecimal(stringValue.substring(4));
                        break;
                    default:
                        throw new C2ServiceException("Unable to parse "+stringValue,false);
                }
                break;
            default:
                prefix = "";
                number = new BigDecimal(stringValue);
        }

    }

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
