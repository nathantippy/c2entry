/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/4/12
 */
package com.collective2.signalEntry.implementation;

import com.collective2.signalEntry.*;

import java.math.BigDecimal;

import static com.collective2.signalEntry.Parameter.*;
public abstract class ImplSignal extends AbstractCommand implements Signal {

    private ImplSignal wrap(Parameter para, Object value) {
        return new SignalLeaf(this, para, value);
    }

    public Signal symbol(String symbol) {
        return wrap(Symbol, symbol);
    }

    public Signal duration(Duration duration) {
        return wrap(OrderDuration, duration);
    }

    public Signal quantity(Integer value) {
        return wrap(Quantity, value);
    }

    public Signal dollars(Number value) {
        return wrap(Dollars, value);
    }

    public Signal accountPercent(Number value) {
        return wrap(AccountPercent, value);
    }

    public Signal limitOrder(BasePrice base, BigDecimal value) {
        return wrap(RelativeLimitOrder, new RelativeNumber(base,value));
    }

    public Signal limitOrder(BigDecimal value) {
        return wrap(RelativeLimitOrder, new RelativeNumber(BasePrice.Absolute,value));
    }

    public Signal stopOrder(BasePrice base, BigDecimal value) {
        return wrap(RelativeStopOrder, new RelativeNumber(base,value));
    }

    public Signal stopOrder(BigDecimal value) {
        return wrap(RelativeStopOrder, new RelativeNumber(BasePrice.Absolute,value));
    }

    public Signal marketOrder() {
    	return wrap(MarketOrder,"");
    }

    public Signal stopLoss(BasePrice base, BigDecimal value, boolean noOCA) {
        ImplSignal result = wrap(RelativeStopLoss, new RelativeNumber(base,value));
        if (noOCA) {
            return new SignalLeaf(result, ForceNoOCA, 1);
        }
        return result;
    }

    public Signal delay(Integer seconds) {
        return wrap(Delay,seconds);
    }

    public Signal stopLoss(BasePrice base, BigDecimal value) {
        return wrap(RelativeStopLoss, new RelativeNumber(base,value));
    }

    public Signal stopLoss(BigDecimal value) {
        return wrap(RelativeStopLoss, new RelativeNumber(BasePrice.Absolute,value));
    }

    public Signal profitTarget(BasePrice base, BigDecimal value, boolean noOCA) {
        ImplSignal result = wrap(RelativeProfitTarget, new RelativeNumber(base,value));
        if (noOCA) {
            return new SignalLeaf(result, ForceNoOCA, 1);
        }
        return result;
    }

    public Signal profitTarget(BasePrice base, BigDecimal value) {
        return wrap(RelativeProfitTarget, new RelativeNumber(base,value));
    }

    public Signal profitTarget(BigDecimal value) {
        return wrap(RelativeProfitTarget, new RelativeNumber(BasePrice.Absolute,value));
    }

    public Signal oneCancelsAnother(Integer id) {
        return wrap(OCAId, id);
    }

    public Signal oneCancelsAnother(Response response) {
        return wrap(OCAId, response);
    }

    public Signal conditionalUpon(Integer id) {
        return wrap(ConditionalUpon, id);
    }

    public Signal xReplace(Integer id) {
        return wrap(XReplace, id);
    }

    public Signal parkUntil(Number seconds) {
        return wrap(ParkUntil, seconds);
    }

    public Signal parkUntil(int year, int month, int day, int hour, int minutes, int seconds) {
        StringBuilder builder = new StringBuilder();
        assert(year>=1900);
        assert(year<=9999);
        builder.append(year);

        assert(month<13);
        assert(month>0);
        if (month<10) {
            builder.append('0');
        }
        builder.append(month);

        assert(day>0);
        assert(day<33);
        if (day<10) {
            builder.append('0');
        }
        builder.append(day);

        assert(hour>=0);
        assert(hour<25);
        if (hour<10) {
            builder.append('0');
        }
        builder.append(hour);

        assert(minutes>=0);
        assert(minutes<61);
        if (minutes<10) {
            builder.append('0');
        }
        builder.append(minutes);

        assert(seconds>=0);
        assert(seconds<61);
        if (seconds<10) {
            builder.append('0');
        }
        builder.append(seconds);

        return wrap(ParkUntilDateTime,builder.toString());
    }

    public Signal cancelsAt(Number seconds) {
        return wrap(CancelsAt, seconds);
    }

    public Signal cancelsAtRelative(Number seconds) {
        return wrap(CancelsAtRelative, seconds);
    }

}
