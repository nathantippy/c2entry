/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/21/12
 */
package com.collective2.signalEntry;

import junit.framework.Assert;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class C2ServiceExceptionTest {

    @Test
    public void messageTest() {

        C2ServiceException e = new C2ServiceException("hello world", false);

        assertEquals("hello world", e.getMessage());
        assertFalse(e.tryAgain());

        e = new C2ServiceException("hello world", true);
        assertEquals("hello world", e.getMessage());
        assertTrue(e.tryAgain());

    }

    @Test
    public void causeTest() {
        Exception cause = new Exception();
        C2ServiceException e =  new C2ServiceException(cause,false);
        assertTrue(cause==e.getCause());
        assertFalse(e.tryAgain());

        e = new C2ServiceException(cause,true);
        assertTrue(cause==e.getCause());
        assertTrue(e.tryAgain());

    }

    @Test
    public void causeWithMessageTest() {
        Exception cause = new Exception();
        String message = "hi";
        C2ServiceException e = new C2ServiceException(message, cause, true);
        assertTrue(cause==e.getCause());
        assertEquals(message, e.getMessage());
        assertTrue(e.tryAgain());

        e =  new C2ServiceException(message, cause, false);

        assertTrue(cause==e.getCause());
        assertEquals(message, e.getMessage());
        assertFalse(e.tryAgain());

    }
}
