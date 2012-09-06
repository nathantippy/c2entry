/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/4/12
 */
package com.collective2.signalEntry.adapter;

import com.collective2.signalEntry.C2ServiceException;
import com.collective2.signalEntry.implementation.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class Collective2Adapter implements C2EntryServiceAdapter {

    private static final Logger   logger = LoggerFactory.getLogger(Collective2Adapter.class);

    private final XMLInputFactory factory;
    private final static int timeoutInMs = 1000;//one second
    private final static int bufferSize =   320;//do not set any smaller, if needed do set bigger

    //must be static lock because there is only one collective2
    private final Object lock = new Object();

    public Collective2Adapter() {
        factory = XMLInputFactory.newInstance();
    }

    public XMLEventReader transmit(Request request) {

                //ensure that no commands to collective2 are ever sent in parallel
                //finish fully reading previous command and close its connection
                //before beginning the next.

                //1. lower connection requirements on server side.
                //2. keep server from waiting on client side parse
                //3. eliminate overlap of sequential signals
                //4. holds full response for debug in case of parse error
                //5. no extra data copy for smallest and most common response size

                URL url = request.buildURL();
                synchronized (lock) {
                    InputStream is = null;
                    int curSize = bufferSize;
                    int readSize = curSize;
                    byte[] buffer = new byte[curSize];
                    int count = 0;
                    int readOff = 0;
                    try{
                        URLConnection connection = url.openConnection();
                        connection.setConnectTimeout(timeoutInMs);
                        is = connection.getInputStream();

                        //the longer the response the slower the pull but the majority
                        //of responses are short and will never require a second copy.

                        while ((count = is.read(buffer,readOff,readSize))>0 ) {

                            readOff += count;
                            readSize -= count;

                            if (readSize==0) {
                                //remove this after doing more testing with live systems.
                                //logger.warn(readOff+" fectch more:"+new String(buffer,0,readOff));

                                //not enough space
                                int newSize = curSize*2;
                                byte[] newBuffer = new byte[newSize];

                                System.arraycopy(buffer,0,newBuffer,0,curSize);

                                readOff = curSize;  //the length read so far is the index where we start next
                                readSize = curSize; //we doubled the cur size so this is how much more we need
                                curSize = newSize;  //new larger size
                                buffer = newBuffer; //new buffer of the larger size
                            }
                        }
                        return factory.createXMLEventReader(new ByteArrayInputStream(buffer,0,readOff));

                    } catch (XMLStreamException e) {
                        String msg = "Unable to parse XML response from Collective2. Sent:"+request+" Received:"+new String(buffer,0,readOff+count);
                        logger.error(msg, e);
                        throw new C2ServiceException(msg, e, false); //do not send again
                    } catch (IOException e) {
                        String msg = "Unable to transmit request to Collective2. Attempted:"+request;
                        logger.error(msg, e);
                        throw new C2ServiceException(msg, e, true); //caller should try again later
                    } finally {
                        if (is!=null) {
                            try {
                                is.close();
                            } catch (IOException e) {
                                logger.warn("Unable to close connection", e);
                            }
                        }
                    }
                }
    }

}
