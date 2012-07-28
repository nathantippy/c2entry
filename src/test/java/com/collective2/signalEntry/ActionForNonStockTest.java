/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  7/28/12
 */
package com.collective2.signalEntry;


import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ActionForNonStockTest {

    @Test
    public void checkStringsTest() {

        assertEquals("BTC",ActionForNonStock.BuyToClose.toString());
        assertEquals("STO",ActionForNonStock.SellToOpen.toString());
        assertEquals("BTO",ActionForNonStock.BuyToOpen.toString());
        assertEquals("STC",ActionForNonStock.SellToClose.toString());

    }

}
