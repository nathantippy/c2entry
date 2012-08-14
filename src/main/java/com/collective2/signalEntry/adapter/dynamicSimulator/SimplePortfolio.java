/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  8/2/12
 */
package com.collective2.signalEntry.adapter.dynamicSimulator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.util.*;


public class SimplePortfolio implements Portfolio {

    private final Map<String,SimplePosition> positionMap = new HashMap<String, SimplePosition>();
    private BigDecimal cash;
    private static final MathContext DECIMAL4 = new MathContext(4);
    private final List<SimplePosition> closedPositions = new ArrayList<SimplePosition>();
    private int totalPositionsClosed;
    private int winPositionsClosed;
    private int lossPositionsClosed;
    private BigDecimal winPositionsTotal = BigDecimal.ZERO;
    private BigDecimal lossPositionsTotal = BigDecimal.ZERO;

    public SimplePortfolio(BigDecimal openCash) {
        cash = openCash;
    }

    @Override
    public Position position(String symbol) {

        SimplePosition position = positionMap.get(symbol);
        if (position==null) {
            position = new SimplePosition(this, symbol);
            positionMap.put(symbol,position);
        }
        return position;
    }

    @Override
    public void closeAllPositions() {
        positionMap.clear();
    }

    @Override
    public BigDecimal cash() {
        return cash;
    }

    @Override
    public BigDecimal equity(DataProvider dataProvider) {
        BigDecimal total = BigDecimal.ZERO;
        for(Map.Entry<String,SimplePosition> pos:positionMap.entrySet()) {

            Integer quantity = pos.getValue().quantity();
            BigDecimal price = dataProvider.endingPrice(pos.getKey());
            total = total.add(price.multiply(new BigDecimal(quantity)));
        }
        return total;
    }

    @Override
    public String statusMessage() {
        DecimalFormat def = new DecimalFormat("##.00");
        StringBuilder builder = new StringBuilder();

        builder.append("positions:").append(totalPositionsClosed);

        if (totalPositionsClosed>0) {
            double pctWin = 100d* winPositionsClosed /(double)totalPositionsClosed;
            builder.append(" # profitable:").append(winPositionsClosed).append('(').append(def.format(pctWin)).append("'%)");
        }
        if (winPositionsClosed>0) {
            double avgWin = winPositionsTotal.doubleValue()/(double) winPositionsClosed;
            builder.append(" avg win:").append(def.format(avgWin));
        }
        if (lossPositionsClosed>0) {
            double avgLoss = lossPositionsTotal.doubleValue()/(double)lossPositionsClosed;
            builder.append(" avg loss:").append(def.format(avgLoss));
        }
        return builder.toString();
    }

    //package protected, only needed by SimplePosition
    void updatePortfolio(String symbol, Integer totalQuantity, BigDecimal totalGain, BigDecimal adj) {

        //this portfolio implementation only supports positions and not specific transaction lots
        //as a result accumulated stats can only be measured when positions get closed
        if (0 == totalQuantity) {
            totalPositionsClosed ++;

            //the position has closed
            if (totalGain.compareTo(BigDecimal.ZERO)>0) {
                //positive gain
                winPositionsClosed++;
                winPositionsTotal = winPositionsTotal.add(totalGain);

            } else {
                //negative gain
                lossPositionsClosed ++;
                lossPositionsTotal = lossPositionsTotal.add(totalGain);

            }
            //remove this one so it it does not get mixed with new positions.
            SimplePosition closedPosition = positionMap.remove(symbol);
            //keep for investigation later
            closedPositions.add(closedPosition);

        }
        cash = cash.add(adj);
    }

    public List<SimplePosition> closedPositions() {
        return Collections.unmodifiableList(closedPositions);
    }



}
