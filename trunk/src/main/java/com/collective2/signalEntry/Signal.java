/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/3/12
 */
package com.collective2.signalEntry;

import java.math.BigDecimal;

public interface Signal {

    Signal limitOrder(BasePrice base, BigDecimal value);

    Signal limitOrder(BigDecimal value);

    Signal stopOrder(BasePrice base, BigDecimal value);

    Signal stopOrder(BigDecimal value);

    Signal stopLoss(BasePrice base, BigDecimal value, boolean noOCA);

    Signal stopLoss(BasePrice base, BigDecimal value);

    Signal stopLoss(BigDecimal value);

    Signal profitTarget(BasePrice base, BigDecimal value, boolean noOCA);

    Signal profitTarget(BasePrice base, BigDecimal value);

    Signal profitTarget(BigDecimal value);

    Signal marketOrder();

    Signal symbol(String symbol);

    Signal duration(Duration duration);

    Signal delay(Integer seconds);

    Signal oneCancelsAnother(Integer id);

    Signal conditionalUpon(Integer id);

    Signal xReplace(Integer id);

    Signal parkUntil(Number seconds);

    Signal parkUntil(int year, int month, int day, int hour, int minutes, int seconds);

    Signal cancelsAt(Number seconds);

    Signal cancelsAtRelative(Number seconds);

    Signal quantity(Integer value);

    Signal dollars(Number value);

    Signal accountPercent(Number value);

    Response send();
}
