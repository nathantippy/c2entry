/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/4/12
 */
package com.collective2.signalEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.collective2.signalEntry.implementation.Command;
import com.collective2.signalEntry.implementation.DotString;
import com.collective2.signalEntry.implementation.RelativeNumber;

public enum Parameter {
    // do not mess with this order, it controls the order in the final URL and
    // the unit tests are dependent upon it.
    // SignalEntryCcommand must be the first one 
    // because it start with the query flag ? instead of & like the others
    SignalEntryCommand("?cmd=", Command.class),

    SystemId("&systemid=", Integer.class),
    Systems("&systemid=", DotString.class),

    Password("&pw=", String.class), 
    EMail("&c2email=", String.class),
    SignalId("&signalid=", Integer.class),

    Instrument("&instrument=", String.class), // enum to check string?
    Symbol("&symbol=", String.class),


    StockAction("&action=", ActionForStock.class),
    NonStockAction("&action=", ActionForNonStock.class),

    OrderDuration("&duration=", Duration.class),

    LimitOrder("&limit=", Number.class),
    StopOrder("&stop=", Number.class),
    RelativeLimitOrder("&limit=", RelativeNumber.class),
    RelativeStopOrder("&stop=", RelativeNumber.class),
    MarketOrder("", String.class),

    Dollars("&dollars=", Number.class),
    Quantity("&quant=", Integer.class),
    AccountPercent("&accountpercent=", Number.class),

    TriggerPrice("&triggerprice=", Number.class),

    OCAId("&ocaid=", Integer.class),

    StopLoss("&stoploss=", Number.class), 
    ProfitTarget("&profittarget=", Number.class),

    RelativeStopLoss("&stoploss=", RelativeNumber.class),
    RelativeProfitTarget("&profittarget=", RelativeNumber.class),

    ForceNoOCA("&forcenooca=", Integer.class),

    Delay("&delay=", Integer.class), ParkUntil("&parkuntil=", Number.class),
    CancelsAt("&cancelsat=", Number.class),
    CancelsAtRelative("&cancelsatrelative=", Number.class), 
    ParkUntilDateTime("&parkuntildatetime=", String.class),

    ShowRelated("&showrelated=", Related.class), 
    ShowDetails("&showdetails=", Integer.class), // 1 is true

    XReplace("&xreplace=", Integer.class),

    ConditionalUpon("&conditionalupon=", Integer.class),

    OCAGroupId("&ocagroupid=", Integer.class),
    Commentary("&commentary=", String.class),

    BuyPower("&buypower=", Number.class),
    Message("&message=", String.class);

    private static final Logger logger = LoggerFactory.getLogger(Parameter.class);
    private final String        URLKey;
    private final Class         type;

    private Parameter(String URLKey, Class clazz) {
        this.URLKey = URLKey;
        this.type = clazz;
    }

    public String key() {
        return URLKey;
    }

    public boolean urlEncode() {
        return type == String.class;
    }

    public void validateValue(Object value) {
        if (value == null) {
            String message = "Null value for parameter is not supported.";
            logger.error(message);
            throw new C2ServiceException(message, false);
        }
        if (!type.isInstance(value)) {
            String message = "Invalid value '" + value + "' for parameter " + this.name();
            logger.error(message);
            throw new C2ServiceException(message, false);
        }
    }
}
