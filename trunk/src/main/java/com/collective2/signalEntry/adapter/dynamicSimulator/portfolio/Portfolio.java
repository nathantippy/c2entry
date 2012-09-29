/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  7/29/12
 */
package com.collective2.signalEntry.adapter.dynamicSimulator.portfolio;

import com.collective2.signalEntry.adapter.dynamicSimulator.DataProvider;

import java.math.BigDecimal;
import java.util.Collection;

public interface Portfolio {

    public Collection<String> positions();

    public Position position(String symbol);

    void closeAllPositions();

    BigDecimal cash();

    BigDecimal equity(DataProvider dataProvider);

    String statusMessage();

}
