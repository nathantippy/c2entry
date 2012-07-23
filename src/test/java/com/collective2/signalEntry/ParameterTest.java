/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/21/12
 */
package com.collective2.signalEntry;

import org.junit.Test;

import static org.junit.Assert.*;

public class ParameterTest {

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

        assertTrue(Parameter.EMail.urlEncode());
        assertFalse(Parameter.Quantity.urlEncode());

    }

    @Test
    public void urlKeyTest() {
        assertEquals("&forcenooca=",Parameter.ForceNoOCA.key());
    }
}