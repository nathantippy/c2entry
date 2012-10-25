package com.collective2.signalEntry.adapter.dynamicSimulator.portfolio;

import java.math.BigDecimal;

/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  7/29/12
 */

public interface Position {

    Integer quantity();

    void applySplit(Number split);

    void addTransaction(Integer quantity, long time, BigDecimal price, BigDecimal commission, boolean isClosing);

    BigDecimal openPrice();

}
