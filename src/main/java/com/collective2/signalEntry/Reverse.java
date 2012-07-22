/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/6/12
 */
package com.collective2.signalEntry;

import java.util.concurrent.Callable;

public interface Reverse {

    Reverse duration(Duration duration);

    Reverse triggerPrice(Number value);

    Reverse quantity(Number value);

    Response send();

}
