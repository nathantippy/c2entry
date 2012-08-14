/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/21/12
 */
package com.collective2.signalEntry.implementation;

import com.collective2.signalEntry.C2ServiceException;
import com.collective2.signalEntry.Parameter;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class RequestTest {

    @Test
    public void commandSetTest() {
        Request request = new Request(Command.AllSystems);
        assertEquals(Command.AllSystems,request.command());
    }

    @Test
    public void urlTest() {
        Request request = new Request(Command.FlushPendingSignals);
        request.put(Parameter.SystemId, 1234);
        request.put(Parameter.Password, "PA55");
        String stringURL = request.buildURL().toString();

        assertEquals("http://www.collective2.com/cgi-perl/signal.mpl?cmd=flushpendingsignals&systemid=1234&pw=PA55", stringURL);
    }

    @Test
    public void urlUnsupportedEncodingTest() {
        Request request = new Request(Command.FlushPendingSignals) {
            @Override
            protected String encode(Object value) throws UnsupportedEncodingException {
                throw new UnsupportedEncodingException("Martian OS does not support UTF-8");
            }                                        //TODO:remove the logger for this test.
        };
        request.put(Parameter.SystemId, 1234);
        request.put(Parameter.Password, "PA55");
        try{
            String stringURL = request.buildURL().toString();
            fail();
        } catch (C2ServiceException e) {
            assertTrue(e.getCause() instanceof UnsupportedEncodingException);
        }
    }

    @Test
    public void urlParseTest() {

        Request request = new Request(Command.Cancel);
        request.put(Parameter.SignalId,1234);
        request.put(Parameter.SystemId,5432);
        request.put(Parameter.Password,"toomanysecrets");

        String urlString = request.buildURL().toString();
        Request rebuilt = request.parseURL(urlString);

        assertEquals(request,rebuilt);
    }
}
