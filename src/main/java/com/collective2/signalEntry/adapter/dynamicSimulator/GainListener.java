package com.collective2.signalEntry.adapter.dynamicSimulator;

import java.math.BigDecimal;
import java.util.List;

/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  8/3/12
 */

public interface GainListener {
    void gainData(long now, List<String> systemNameList, List<BigDecimal> beginEquityList, List<Double> fullCAGRList, List<BigDecimal> lastEquityList, List<Double> lastCAGRList, List<BigDecimal> currentEquityList);
}
