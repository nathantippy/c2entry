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
        public BigDecimal select(BigDecimal bestPrice, BigDecimal worstPrice) {

            BigDecimal difference = worstPrice.subtract(bestPrice);

            double random = generator.nextGaussian();
            if (random>4) {
                random = 4;
            } else if (random<-4) {
                random = -4;
            }
            double unit = difference.doubleValue()/8d;
            double middle = bestPrice.doubleValue()+(4d*unit);
            double offset = random*unit;
            double result = middle+offset;
            assert(result>=Math.min(bestPrice.doubleValue(),worstPrice.doubleValue()));
            assert(result<=Math.max(bestPrice.doubleValue(), worstPrice.doubleValue()));

            int scale = Math.max(bestPrice.scale(),worstPrice.scale());

            return new BigDecimal(result,new MathContext(scale));
        }

    };

    BigDecimal select(BigDecimal bestPrice, BigDecimal worstPrice);
}
