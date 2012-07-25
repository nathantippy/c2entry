/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/21/12
 */
package com.collective2.signalEntry.implementation;

import com.collective2.signalEntry.BasePrice;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RelativeNumberTest {

    @Test
    public void AbsoluteTest() {
        RelativeNumber number = new RelativeNumber(BasePrice.Absolute, 12.3d);
        assertEquals("12.3",number.toString());
    }

    @Test
    public void OpeningTest() {
        RelativeNumber number = new RelativeNumber(BasePrice.Opening, 12.3d);
        assertEquals("O%2B12.3",number.toString());
    }

    @Test
    public void QuoteNowTest() {
        RelativeNumber number = new RelativeNumber(BasePrice.QuoteNow, 12.3d);
        assertEquals("Q%2B12.3",number.toString());
    }

    @Test
    public void TradeFillTest() {
        RelativeNumber number = new RelativeNumber(BasePrice.TradeFill, 12.3d);
        assertEquals("T%2B12.3",number.toString());
    }
}
