package com.collective2.signalEntry.adapter.dynamicSimulator.order;

import com.sun.org.apache.bcel.internal.generic.RETURN;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Random;

/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  8/4/12
 */

public interface PriceSelector {
    PriceSelector DEFAULT = new PriceSelector() {

        Random generator = new Random();
        public BigDecimal select(BigDecimal bestPrice, BigDecimal worstPrice, boolean firstIsTrigger) {
            BigDecimal difference = worstPrice.subtract(bestPrice);

            double random = generator.nextGaussian();
            if (random > 4) {
                random = 4;
            } else if (random < -4) {
                random = -4;
            }

            random = Math.abs(random);
            double unit = difference.doubleValue()/4d;
            double offset = random*unit;
            double value = bestPrice.doubleValue()+offset;

            int scale = Math.max(bestPrice.scale(),worstPrice.scale());

            if (firstIsTrigger) {
                return bestPrice;
            } else {
                return worstPrice;
            }


          //  return new BigDecimal(value,new MathContext(scale));

     //       return bestPrice;//TODO: need other implmentations

            //double middle = bestPrice.doubleValue()+(4d*unit);
//            double result = middle+offset;
//            assert(result>=Math.min(bestPrice.doubleValue(),worstPrice.doubleValue()));
//            assert(result<=Math.max(bestPrice.doubleValue(), worstPrice.doubleValue()));
//
//
        }

    };

    //TODO: needs open for leading edge?
    BigDecimal select(BigDecimal bestPrice, BigDecimal worstPrice, boolean firstIsTrigger);
}
