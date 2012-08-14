/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/6/12
 */
package com.collective2.signalEntry;

import java.math.BigDecimal;

public interface Reverse {

    Reverse duration(Duration duration);

    Reverse triggerPrice(BigDecimal value);

    Reverse quantity(Integer value);

    Response send();

}
