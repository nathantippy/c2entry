/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  8/2/12
 */
package com.collective2.signalEntry.adapter.dynamicSimulator;

import java.math.BigDecimal;
import java.math.MathContext;

public class SimplePosition implements Position {
    private static final MathContext DECIMAL4 = new MathContext(4);
    private Integer totalQuantity = 0;
    private BigDecimal totalCost = BigDecimal.ZERO;
    private final String symbol;
    private final SimplePortfolio simplePortfolio;
    private BigDecimal openPrice = null;

    public SimplePosition(SimplePortfolio simplePortfolio, String symbol) {
        this.simplePortfolio = simplePortfolio;
        this.symbol = symbol;
    }

    @Override
    public Integer quantity() {
        return totalQuantity;
    }

    @Override
    public void addTransaction(Integer quantity, long time, BigDecimal price, BigDecimal commission) {

        BigDecimal adj = price.multiply(new BigDecimal(quantity)).negate().subtract(commission);

        totalCost = totalCost.add(adj);
        if (0 == totalQuantity && null == openPrice) {
            openPrice = price;
        }
        totalQuantity =  totalQuantity + quantity;

        simplePortfolio.updatePortfolio(symbol, totalQuantity, totalCost, adj);

    }

    @Override
    public BigDecimal openPrice() {
        return openPrice;
    }

}
