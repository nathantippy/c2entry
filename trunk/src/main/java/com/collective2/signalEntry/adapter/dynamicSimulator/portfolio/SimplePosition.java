/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  8/2/12
 */
package com.collective2.signalEntry.adapter.dynamicSimulator.portfolio;

import com.collective2.signalEntry.C2ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.MathContext;

public class SimplePosition implements Position {

    private static final Logger logger = LoggerFactory.getLogger(SimplePosition.class);

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
    public void addTransaction(Integer quantity, long time, BigDecimal price, BigDecimal commission, boolean isClosing) {

        if (isClosing) {
            //check that we have an open position.
            if (0 == totalQuantity.intValue()) {
                throw new C2ServiceException("Can not close position because it is not open for:"+symbol,false);
            }
        }

        BigDecimal adj = price.multiply(new BigDecimal(quantity)).negate().subtract(commission);

        totalCost = totalCost.add(adj);
        if (0 == totalQuantity && null == openPrice) {
            openPrice = price;
        }
        totalQuantity =  totalQuantity + quantity;

        //logger.warn("new "+(isClosing?"closing":"opening")+" transaction quantity:"+quantity+" price:"+price+" commission:"+commission+" newTotalQuantity:"+totalQuantity+(isClosing ? " newTotalCost:"+totalCost: "")+" cashFlow:"+adj);

        simplePortfolio.updatePortfolio(symbol, totalQuantity, totalCost, adj, this);

    }

    @Override
    public BigDecimal openPrice() {
        return openPrice;
    }

}
