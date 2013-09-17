package com.collective2.signalEntry.adapter.dynamicSimulator.portfolio;

import java.math.BigDecimal;

public class SimplePortfolioFactory implements PortfolioFactory {

    PositionFactory positionFactory = new SimplePositionFactory();
    
    @Override
    public Portfolio createPortfolio(BigDecimal openingCash) {
        return new SimplePortfolio(openingCash,positionFactory);
    }

    @Override
    public boolean report() {
        return true;
    }

}
