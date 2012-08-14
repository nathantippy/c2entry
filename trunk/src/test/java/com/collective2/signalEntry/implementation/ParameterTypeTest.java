/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  7/28/12
 */
package com.collective2.signalEntry.implementation;


import com.collective2.signalEntry.*;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class ParameterTypeTest {

    @Test
    public void dogFoodTest() {

        for(ParameterType type: ParameterType.values()) {

            Object input = null;
            switch (type) {
                case IntDotStringType:
                    input = new DotString<Integer>(1,2,3);
                    break;
                case ActionForNonStockType:
                    input = ActionForNonStock.SellToOpen;
                    break;
                case ActionForStockType:
                    input = ActionForStock.SellShort;
                    break;
                case CommandType:
                    input = Command.GetSystemHypothetical;
                    break;
                case DigitsFixed14:
                    input = "20060618093115";
                    break;
                case DurationType:
                    input = Duration.DayOrder;
                    break;
                case InstrumentType:
                    input = Instrument.Future;
                    break;
                case IntegerType:
                    input = new Integer(123);
                    break;
                case NumberType:
                    input = new Double(12.3);
                    break;
                case MoneyType:
                    input = new BigDecimal("42.42");
                    break;
                case RelatedType:
                    input = Related.Children;
                    break;
                case RelativeNumberType:
                    input = new RelativeNumber(BasePrice.SessionOpenPlus,new BigDecimal(123));
                    break;
                case StringType:
                    input = "this is a stick.";
                    break;
            }
            //can we take the string parse it back then validate it
            type.validate(input);
            Object rebuiltObject = type.parse(input.toString());
            assertEquals("testing:"+type+" parsed:"+input.toString(),input.toString(),rebuiltObject.toString());
            type.validate(rebuiltObject);

        }


    }

}
