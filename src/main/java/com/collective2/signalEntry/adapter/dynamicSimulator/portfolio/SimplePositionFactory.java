package com.collective2.signalEntry.adapter.dynamicSimulator.portfolio;

public class SimplePositionFactory implements PositionFactory {

    @Override
    public Position createPosition(Portfolio portfolio, String symbol) {
        return new SimplePosition((SimplePortfolio) portfolio, symbol);
    }

}
