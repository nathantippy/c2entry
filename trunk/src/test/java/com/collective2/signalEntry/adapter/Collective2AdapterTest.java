/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy    7/21/12
 */
package com.collective2.signalEntry.adapter;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.logging.Handler;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;

import com.collective2.signalEntry.C2ServiceException;
import com.collective2.signalEntry.implementation.Command;
import com.collective2.signalEntry.implementation.Request;

public class Collective2AdapterTest {

    @Before
    public void dumpLog() {
        //remove loggers to speed up test
        //same events are captured by looking at the exceptions
        for(Handler h: Logger.getLogger("").getHandlers()) {
            Logger.getLogger("").removeHandler(h);
        }
    }

    @Test
    public void timeoutTest() {

        Collective2Adapter adapter = new Collective2Adapter();
        //mock request that does not return actual URL to collective2
        Request mockRequest = new Request(Command.AllSignals) {
            @Override
            public URL buildURL() {
                try {
                    return new URL("file://nofile.nowhere");

                } catch (MalformedURLException e) {
                    fail();
                    return null;
                }
            };
        };

        try{
            IterableXMLEventReader xml = adapter.transmit(mockRequest);
            fail();//bad url should have timed out
        } catch (C2ServiceException e) {
            //if timeout is not set then ConnectException is returned.
            assertTrue("cause: "+e.getCause(),
                       e.getCause() instanceof UnknownHostException ||
                       e.getCause() instanceof ConnectException  ||
                       e.getCause() instanceof SocketTimeoutException);
            assertTrue(e.tryAgain());//recommended to try again when network it back up
        }
    }

    @Test
    public void transmitTest() {

        Collective2Adapter adapter = new Collective2Adapter();
        //mock request that does not return actual URL to collective2
        Request mockRequest = new Request(Command.Cancel) {
            @Override
            public URL buildURL() {
                return getClass().getClassLoader().getResource("staticResponse.xml");
            };
        };

        IterableXMLEventReader xml = adapter.transmit(mockRequest);
        assertNotNull(xml);
    }

}
