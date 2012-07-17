package com.collective2.signalEntry.transmission;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.EnumMap;
import java.util.Map;

import javax.xml.stream.XMLEventReader;

import org.junit.Test;

import com.collective2.signalEntry.Parameter;
import com.collective2.signalEntry.implementation.Command;

/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/16/12
 */
public class BackEndAdapterTest {

    @Test
    public void backEndAdapterLockTest() {

        BackEndAdapter bae = new BackEndAdapter() {
            @Override
            public XMLEventReader transmit(Map<Parameter, Object> paraMap) {
                return null;  //just for testing
            }
        };

        bae.lock();
        try{
            bae.unlock();
        } catch (Throwable t) {
            fail();
        }
        try {
            bae.unlock();
            fail();
        } catch (Throwable t) {
            //success
        }
    }
    
    @Test
    public void backEndAdapterURLTest() {
        
        final String expectedLive = "http://www.collective2.com/cgi-perl/signal.mpl?cmd=addtoocagroup&systemid=1234&pw=too+many+secrets";
        final String expectedDebug = "http://www.collective2.com/cgi-perl/signal.mpl?cmd=addtoocagroup&systemid=1234&pw=PASSWORD";
        
        
        BackEndAdapter bae = new BackEndAdapter() {
            @Override
            public XMLEventReader transmit(Map<Parameter, Object> paraMap) {
                
                String liveURL = buildURL(paraMap).toString();
                assertEquals(expectedLive,liveURL);
                
                String debugURL = buildURL(paraMap,true).toString();
                assertEquals(expectedDebug,debugURL);
                
                return null;  //just for testing
            }
        };

        Map<Parameter, Object> paraMap = new EnumMap<Parameter,Object>(Parameter.class);
        
        paraMap.put(Parameter.SignalEntryCommand, Command.AddToOCAGroup.toString());
        paraMap.put(Parameter.Password, "too many secrets");
        paraMap.put(Parameter.SystemId, 1234);
        
        bae.transmit(paraMap);
    }

}
