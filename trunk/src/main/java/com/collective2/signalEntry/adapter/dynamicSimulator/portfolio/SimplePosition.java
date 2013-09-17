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
import java.util.Date;

public class SimplePosition implements Position {

    private static final Logger logger = LoggerFactory.getLogger(SimplePosition.class);

    private static final MathContext DECIMAL4 = new MathContext(4);
    private Integer totalQuantity = 0;
    protected BigDecimal totalCost = BigDecimal.ZERO;
    protected final String symbol;
    private final SimplePortfolio simplePortfolio;
    private BigDecimal openPrice = null;
    private long openTime;

    protected SimplePosition(SimplePortfolio simplePortfolio, String symbol) {
        this.simplePortfolio = simplePortfolio;
        this.symbol = symbol;
    }

    @Override
    public Integer quantity() {
        return totalQuantity;
    }

    @Override
    public void applySplit(Number split) {
        logger.info("Adjusted {} for split {}",symbol,split);
        //good enough for simulations but if one implements a real portfolio one should think twice about doing this.
        totalQuantity = (int)Math.rint(split.doubleValue()*totalQuantity.doubleValue());
    }

    @Override
    public void addTransaction(Integer quantity, long time, BigDecimal price, BigDecimal commission, boolean isClosing) {

        assert(isClosing || 0 == totalQuantity.intValue()): "quantity is "+totalQuantity+" for "+symbol;

        if (0==totalQuantity.intValue()) {
            //ensure cost is zero if quantity is zero
            totalCost = BigDecimal.ZERO;
            assert(!isClosing);
            
        }

        assert(quantity.intValue()!=0) : "Zero quantity not allowed for transaction";
        assert(isClosing || BigDecimal.ZERO.compareTo(price)<0) : "Zero price not allowed for open transaction "+price;

        BigDecimal initialCost = totalCost;

        BigDecimal totalSellPrice = price.multiply(new BigDecimal(quantity)).negate().subtract(commission);
        
        totalCost = totalCost.add(totalSellPrice);
        long duration = -1;
        if (isClosing) {
            
         //   BigDecimal expectedInitialCost = openPrice.multiply(new BigDecimal(quantity)).negate().subtract(commission);

//            if (!initialCost.equals(expectedInitialCost)) {
//                throw new UnsupportedOperationException(initialCost+" != "+expectedInitialCost);
//            }
            
            assert((totalQuantity + quantity == 0)) : "holding "+totalQuantity+" but closing "+quantity+" for "+symbol;
            //check that we have an open position.
            if (totalQuantity + quantity != 0) {
                //auto close: only amount open TODO: serious, investigate how we get into this situation.
                quantity = -totalQuantity;
                //throw new C2ServiceException("Can not close position "+quantity+" because it is not open with "+totalQuantity+" for "+symbol+" at price "+price,false);
            }
            duration = time-openTime;
           // simplePortfolio.updateDuration(duration, totalCost);

          //  System.out.println(new Date(openTime)+"  ->  "+new Date(time)+" quantity "+quantity+" gain "+totalCost+" price "+openPrice+" to "+price);

        } else {
            if (quantity==0 && totalQuantity==0) {
                throw new C2ServiceException("Can not open position in "+symbol+" of zero quantity", false);
            }
            
            //if (Math.abs(totalCost.doubleValue()) > 1000000) {
            //    throw new C2ServiceException(totalCost+" bad cost "+price+"  "+quantity+" "+commission,false );
            //}
            
            openTime = time;
            if (BigDecimal.ZERO.compareTo(price)>=0 && quantity>0) {
                //this can really happen but it would be expected to be rare, confirm this with a secondary source
                logger.warn("Lost 100% of position due to zero price data for "+symbol+" on "+new Date(time));
            }

            //logger.trace("open new {} position of {} existing "+totalQuantity,symbol,quantity);
        }


        totalQuantity =  totalQuantity + quantity;

   //TODO:off while testing for optimals
//        //watch for big loss/gain
//        BigDecimal pctFlag = new BigDecimal(".5");   //NOTE: hardcoded to only look for losses now
//        if (0==totalQuantity.intValue() && totalCost.doubleValue()<0 && totalCost.abs().compareTo(initialCost.abs().multiply(pctFlag))>0) {
//            System.err.println("LARGE CHANGE:"+symbol+" Gain:"+totalCost+" Invest:"+initialCost+" closing:"+isClosing+" price:"+price+" quantity:"+quantity+" commission:"+commission+" DATE:"+new Date(time)+" openPrice: "+openPrice+" openDate:"+new Date(openTime));
//        }

       // if (verbose) {
       //     System.err.println(new Date(time)+" update portfolio "+symbol +" quantity:"+totalQuantity+" gain:"+totalCost+" flow:"+adj+" "+isClosing);
       // }
        simplePortfolio.updatePortfolio(symbol, totalQuantity, totalCost, totalSellPrice, this, isClosing, duration);

       
        if ((0 == totalQuantity.intValue()) || null == openPrice) {
            openPrice = price;
        }
        
        //TODO: no transactions should ever be this big must track down how this happened.
        if (Math.abs(totalCost.doubleValue()) > 1000000) {
            throw new C2ServiceException(totalCost+" bad cost "+price+"  "+quantity+" "+commission+" init "+initialCost+" totalSale "+totalSellPrice+" date "+new Date(time),false );
        }
        /*
         * WARNING: stats gathering
com.collective2.signalEntry.C2ServiceException: -1661142.6100 bad cost 39.7200  706 10.00 init -1633090.2900 totalSale -28052.3200 date Tue Mar 18 15:00:00 CDT 2008
    at com.collective2.signalEntry.adapter.dynamicSimulator.portfolio.SimplePosition.addTransaction(SimplePosition.java:127)
    at com.collective2.signalEntry.adapter.dynamicSimulator.order.OrderProcessorLimit.process(OrderProcessorLimit.java:124)
    at com.collective2.signalEntry.adapter.dynamicSimulator.order.Order.process(Order.java:266)
    at com.collective2.signalEntry.adapter.dynamicSimulator.SystemManager.tick(SystemManager.java:429)
    at com.collective2.signalEntry.adapter.DynamicSimulationAdapter.tick(DynamicSimulationAdapter.java:96)
    at com.javanut.wallnut.electo.backTest.IndicatusVisitorBackTester.visit(IndicatusVisitorBackTester.java:143)
    at com.javanut.wallnut.cogito.simulator.QuantusIndicatusProcessorSystemTester.process(QuantusIndicatusProcessorSystemTester.java:30)
    at com.javanut.wallnut.cogito.simulator.StatsGatheringSimulator.process(StatsGatheringSimulator.java:96)
    at com.javanut.wallnut.cogito.CogitoRunnable.run(CogitoRunnable.java:211)
    at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1110)
    at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:603)
         */
        
    }

    @Override
    public BigDecimal openPrice() {
        return openPrice;
    }

    @Override
    public Long openTime() {
        return openTime;
    }

}
