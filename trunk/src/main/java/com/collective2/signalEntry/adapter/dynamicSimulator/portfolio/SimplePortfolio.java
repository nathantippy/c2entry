/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  8/2/12
 */
package com.collective2.signalEntry.adapter.dynamicSimulator.portfolio;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.collective2.signalEntry.adapter.dynamicSimulator.DataProvider;


public class SimplePortfolio implements Portfolio {

    /*
     * TODO: Average CostBases position portfolio.
     * 
     * Also provides mode for infinite cash
     */
    
    private static final Logger logger = LoggerFactory.getLogger(SimplePortfolio.class);

    protected final Map<String,Position> positionMap = new HashMap<String,Position>();
    
    private BigDecimal cash;
    private static final MathContext DECIMAL4 = new MathContext(4);
    private long totalPositionsClosed;
    private long winPositionsClosed;
    private long lossPositionsClosed;
    private BigDecimal winPositionsTotal = BigDecimal.ZERO;
    private BigDecimal lossPositionsTotal = BigDecimal.ZERO;
    
    private long totalDuration;
    private long countDuration;
    private long zeroDurationCount;
    private final PositionFactory positionFactory;
    
    
    public SimplePortfolio(BigDecimal openCash, PositionFactory positionFactory) {
        this.cash = openCash;
        this.positionFactory = positionFactory;
    }
    
    @Override
    public Collection<String> positions() {
        return positionMap.keySet();
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
    public void addCash(BigDecimal value) {
        cash = cash.add(value);
    }
    
    @Override
    public int openPositionCount() {
        int count = 0;
        for(Position p: positionMap.values()) {
            assert(p.quantity()>=0);
            if (p.quantity()>0) {
                count++;
            }
        }
        return count;
    }
    
    @Override
    public BigDecimal equity(DataProvider dataProvider) {
        BigDecimal total = BigDecimal.ZERO;
        for(Map.Entry<String,Position> pos:positionMap.entrySet()) {

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

        builder.append("cash:").append(cash.intValue());
        builder.append(" pos:[cl:").append(totalPositionsClosed);
        builder.append(" op:").append(openPositionCount()).append("]");

        if (totalPositionsClosed>0) {
            double pctWin = 100d* winPositionsClosed /(double)totalPositionsClosed;
            builder.append(" # prfit:").append(winPositionsClosed).append('(').append(def.format(pctWin)).append("%)");
        }
        if (winPositionsClosed>0) {
            double avgWin = winPositionsTotal.doubleValue()/(double)winPositionsClosed;
            builder.append(" avgWin:").append(def.format(avgWin));
        }
        if (lossPositionsClosed>0) {
            double avgLoss = lossPositionsTotal.doubleValue()/(double)lossPositionsClosed;
            builder.append(" avgLoss:").append(def.format(avgLoss));
        }

        if (countDuration-zeroDurationCount>0) {
            int avg = (int)Math.rint((totalDuration/(countDuration-zeroDurationCount))/(60000f*60f*24f));
           // long pctZero = 100l*zeroDurationCount/(countDuration+zeroDurationCount);//TODO: this is large and can be changed with trend assumption?
            builder.append(" avgDays:"+avg);//+" avgGain:"+(totalGain/countDuration)+" sameDayGain:"+(zeroGain/zeroDurationCount));
        }


        return builder.toString();
    }

    public void updateDuration(long duration, BigDecimal gain) {
        totalDuration += duration;
        countDuration ++;
        //totalGain += gain.doubleValue();
        if (0 == duration) {
            zeroDurationCount ++;
            //zeroGain += gain.doubleValue();
        }


    }

    public double pctLosses() {
       return lossPositionsClosed /(double)totalPositionsClosed;
    }

    public Position position(String symbol) {

        Position position = positionMap.get(symbol);
        if (position==null) {
            position = positionFactory.createPosition(this,symbol);
            //do not add to map because updatePortfolio will take care of that when it has something worth writing
        }
        return position;
    }



    //package protected, only needed by SimplePosition
    protected void updatePortfolio(String symbol, Integer totalQuantity, BigDecimal totalGain, BigDecimal adj, Position position, boolean isClosing, long duration) {
        
        cash = cash.add(adj);
        
        //this portfolio implementation only supports positions and not specific transaction lots
        //as a result accumulated stats can only be measured when positions get closed
        if (0 == totalQuantity.intValue()) {
            assert(isClosing);
            totalPositionsClosed ++;
            
            //the position has closed
            if (totalGain.compareTo(BigDecimal.ZERO)>0) {
                //positive gain
                winPositionsClosed++;
                winPositionsTotal = winPositionsTotal.add(totalGain);
                
                //valueAsOfLastGain = this.

            } else {
                //negative gain
                lossPositionsClosed ++;
                lossPositionsTotal = lossPositionsTotal.add(totalGain);

            }
            
            totalDuration += duration;
            countDuration ++;
            if (0 == duration) {
                zeroDurationCount ++;
            }
            
            //remove this one so it it does not get mixed with new positions.
            positionMap.remove(symbol);
          //  System.err.println("duration:"+duration);

        } else {
            assert(!isClosing);
            positionMap.put(symbol,position);
        }



        
        
    }

    
}
