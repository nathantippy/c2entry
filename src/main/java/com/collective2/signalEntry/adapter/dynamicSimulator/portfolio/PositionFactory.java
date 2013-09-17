package com.collective2.signalEntry.adapter.dynamicSimulator.portfolio;

public interface PositionFactory {

    Position createPosition(Portfolio simplePortfolio, String symbol);

}
