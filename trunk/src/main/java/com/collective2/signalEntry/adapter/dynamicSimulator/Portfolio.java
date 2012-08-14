/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  7/29/12
 */
package com.collective2.signalEntry.adapter.dynamicSimulator;

import java.math.BigDecimal;

public interface Portfolio {

    public Position position(String symbol);

    void closeAllPositions();

    BigDecimal cash();

    BigDecimal equity(DataProvider dataProvider);

    String statusMessage();
}
