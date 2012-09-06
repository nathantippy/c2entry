/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/4/12
 */
package com.collective2.signalEntry;

import com.collective2.signalEntry.implementation.ParameterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum Parameter {
    // do not mess with this order, it controls the order in the final URL and
    // the unit tests are dependent upon it.
    // SignalEntryCcommand must be the first one 
    // because it start with the query flag ? instead of & like the others
    SignalEntryCommand("?cmd=",             ParameterType.CommandType),

    SystemId("&systemid=",                  ParameterType.IntegerType),
    Systems("&systemid=",                   ParameterType.IntDotStringType),

    Password("&pw=",                        ParameterType.StringType),
    EMail("&c2email=",                      ParameterType.StringType),
    SignalId("&signalid=",                  ParameterType.IntegerType),

    Instrument("&instrument=",              ParameterType.InstrumentType),
    Symbol("&symbol=",                      ParameterType.StringType),
    Action("&action=",                      ParameterType.ActionType),

    TimeInForce("&duration=",               ParameterType.DurationType),

    RelativeLimitOrder("&limit=",           ParameterType.RelativeNumberType),
    RelativeStopOrder("&stop=",             ParameterType.RelativeNumberType),
    MarketOrder("",                         ParameterType.StringType),

    Dollars("&dollars=",                    ParameterType.NumberType),
    Quantity("&quant=",                     ParameterType.IntegerType),
    AccountPercent("&accountpercent=",      ParameterType.NumberType),

    TriggerPrice("&triggerprice=",          ParameterType.MoneyType),

    OCAId("&ocaid=",                        ParameterType.IntegerType),

    RelativeStopLoss("&stoploss=",          ParameterType.RelativeNumberType),
    RelativeProfitTarget("&profittarget=",  ParameterType.RelativeNumberType),

    ForceNoOCA("&forcenooca=",              ParameterType.IntegerType),

    Delay("&delay=",                        ParameterType.IntegerType),
    ParkUntil("&parkuntil=",                ParameterType.NumberType),
    CancelsAt("&cancelsat=",                ParameterType.NumberType),
    CancelsAtRelative("&cancelsatrelative=",ParameterType.NumberType),
    ParkUntilDateTime("&parkuntildatetime=",ParameterType.DigitsFixed14), //parkuntildatetime=YYYYMMDDhhmmss
                                                                          //parkuntildatetime=20060618093115
    ShowRelated("&showrelated=",            ParameterType.RelatedType),
    ShowDetails("&showdetails=",            ParameterType.IntegerType), // 1 is true

    XReplace("&xreplace=",                  ParameterType.IntegerType),

    ConditionalUpon("&conditionalupon=",    ParameterType.IntegerType),

    OCAGroupIdToGrow("&ocagroupid=",        ParameterType.IntegerType),
    Commentary("&commentary=",              ParameterType.StringType),

    BuyPower("&buypower=",                  ParameterType.MoneyType),
    Message("&message=",                    ParameterType.StringType);

    private static final Logger logger = LoggerFactory.getLogger(Parameter.class);
    private final String        URLKey;
    private final ParameterType type;

    private Parameter(String URLKey, ParameterType type) {
        this.URLKey = URLKey;
        this.type = type;
    }

    public String key() {
        return URLKey;
    }

    public Object parse(String value) {
        return type.parse(value);
    }

    public boolean shouldEncode() {
        return type.isClass(String.class);
    }

    public void validateValue(Object value) {
        type.validate(value);
    }

    public boolean isNumber() {
        return type.isClass(Number.class);
    }
}
