/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/8/12
 */
package com.collective2.signalEntry.implementation;

import com.collective2.signalEntry.BasePrice;
import com.collective2.signalEntry.C2ServiceException;

import java.io.Serializable;
import java.math.BigDecimal;

public class RelativeNumber implements Serializable {

    private final BigDecimal number;
    private final char prefix;

    public RelativeNumber() {
        number = BigDecimal.ZERO;
        prefix = BasePrice.Absolute.prefix();
    }

    public RelativeNumber(String stringValue) {
        //parse
        switch(stringValue.charAt(0)) {
            case 'O':   //session opening price
            case 'T':   //fill price of the opening portion of the trade (no BTO or STO)
            case 'Q':   //real-time quote-feed price of the instrument at the moment the order is released for processing
                prefix = stringValue.charAt(0);

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
                prefix = ' ';
                number = new BigDecimal(stringValue);
        }

    }

    public RelativeNumber(BasePrice base, BigDecimal value) {

        prefix = base.prefix();
        number = value;

    }

    public String toString() {

        //This is encoded when the url is built
        //Plus  %2B
        //Minus %2D

        String value = number.toString();
        if (' '==prefix) { //only for BasePrice.Absolute
            return value;
        } else {
            if (value.startsWith("-")) {
                return prefix+"%2D"+value.substring(1);
            } else {
                return prefix+"%2B"+value;
            }
        }
    }

    public char prefix() {
        return prefix;
    }

    public BigDecimal value() {
        return number;
    }
}
