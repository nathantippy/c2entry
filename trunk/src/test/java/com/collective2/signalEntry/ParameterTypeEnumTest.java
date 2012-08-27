/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  7/28/12
 */
package com.collective2.signalEntry;


import com.collective2.signalEntry.*;
import com.collective2.signalEntry.implementation.*;
import org.junit.Test;
import static com.collective2.signalEntry.ActionForNonStock.*;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ParameterTypeEnumTest {

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

    @Test
    public void relatedTest() {

        for(Related related: Related.values()) {
            assertEquals(related,Related.valueOf(related.name())); //valueOf call marks this enum as covered
            assertEquals(related, ParameterType.lookupEnum(related.toString(), Related.values()));
        }
    }

    @Test
    public void instrumentTest() {

        for(Instrument e: Instrument.values()) {
            assertEquals(e,Instrument.valueOf(e.name())); //valueOf call marks this enum as covered
            assertEquals(e,ParameterType.lookupEnum(e.toString(),Instrument.values()));
        }
    }

    @Test
    public void durationTest() {

        for(Duration e: Duration.values()) {
            assertEquals(e,Duration.valueOf(e.name())); //valueOf call marks this enum as covered
            assertEquals(e,ParameterType.lookupEnum(e.toString(),Duration.values()));
        }
    }

    @Test
    public void actionTest() {

        for(Action e: Action.values()) {
            assertEquals(e,Action.valueOf(e.name())); //valueOf call marks this enum as covered
            assertEquals(e,ParameterType.lookupEnum(e.toString(),Action.values()));
        }
    }

    @Test
    public void commandTest() {

        for(Command e: Command.values()) {
            assertEquals(e,Command.valueOf(e.name())); //valueOf call marks this enum as covered
            assertEquals(e,ParameterType.lookupEnum(e.toString(),Command.values()));
        }
    }

    @Test
    public void stockActionTest() {

        for(ActionForStock e: ActionForStock.values()) {
            assertEquals(e,ActionForStock.valueOf(e.name())); //valueOf call marks this enum as covered
            assertEquals(e,ParameterType.lookupEnum(e.toString(),ActionForStock.values()));
        }
    }

    @Test
    public void nonStockActionTest() {

        assertEquals(BuyToClose, ActionForNonStock.valueOf(BuyToClose.name()));
        assertEquals(BuyToClose, ActionForNonStock.lookupEnum(BuyToClose.toString()));

        for(ActionForNonStock e: ActionForNonStock.values()) {
            assertEquals(e,ActionForNonStock.valueOf(ActionForNonStock.class,e.name())); //valueOf call marks this enum as covered
            assertEquals(e,ActionForNonStock.lookupEnum(e.toString()));
            assertEquals(e.action(),Action.valueOf(e.action().name()));
        }
    }

    @Test
    public void c2ElementTest() {

        for(C2Element e: C2Element.values()) {
            assertEquals(e,C2Element.valueOf(e.name())); //valueOf call marks this enum as covered
            assertEquals(e,ParameterType.lookupEnum(e.toString(),C2Element.values()));
        }
    }

    @Test
    public void parameterTest() {

        for(Parameter e: Parameter.values()) {
            assertEquals(e,Parameter.valueOf(e.name())); //valueOf call marks this enum as covered
            assertEquals(e,ParameterType.lookupEnum(e.toString(),Parameter.values()));
        }
    }

}
