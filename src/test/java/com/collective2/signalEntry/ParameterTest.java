/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/21/12
 */
package com.collective2.signalEntry;

import org.junit.Before;
import org.junit.Test;

import java.util.logging.Handler;
import java.util.logging.Logger;

import static org.junit.Assert.*;

public class ParameterTest {

    @Before
    public void dumpLog() {
        //remove loggers to speed up test
        //same events are captured by looking at the exceptions
        for(Handler h: Logger.getLogger("").getHandlers()) {
            Logger.getLogger("").removeHandler(h);
        }
    }

    @Test
    public void badValuesTest() {

        try{
            Parameter.Dollars.validateValue("hello");
            fail();
        } catch (C2ServiceException e) {
            assertTrue(e.getMessage().startsWith("Invalid value"));
        }

        try{
            Parameter.Dollars.validateValue(null);
            fail();
        } catch (C2ServiceException e) {
            assertTrue(e.getMessage().startsWith("Null value"));
        }
    }

    @Test
    public void urlEncodeTest() {

        assertTrue(Parameter.EMail.shouldEncode());
        assertFalse(Parameter.Quantity.shouldEncode());
        assertFalse(Parameter.RelativeLimitOrder.shouldEncode());
    }

    @Test
    public void urlKeyTest() {
        assertEquals("&forcenooca=",Parameter.ForceNoOCA.key());
    }
}