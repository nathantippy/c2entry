package com.collective2.signalEntry.adapter.dynamicSimulator.portfolio;

import java.math.BigDecimal;

public interface PortfolioFactory {

    Portfolio createPortfolio(BigDecimal openingCash);
    boolean report();
    
}
