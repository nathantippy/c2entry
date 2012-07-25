/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/4/12
 */
package com.collective2.signalEntry.adapter;

import java.io.*;
import java.net.URLConnection;


import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import com.collective2.signalEntry.implementation.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.collective2.signalEntry.C2ServiceException;

public class Collective2Adapter implements C2EntryServiceAdapter {

    private static final Logger   logger = LoggerFactory.getLogger(Collective2Adapter.class);

    private final XMLInputFactory factory;
    private final static int timeoutInMs = 1000;//one second

    //must be static lock because there is only one collective2
    private final static Object lock = new Object();

    public Collective2Adapter() {
        factory = XMLInputFactory.newInstance();
    }

    public XMLEventReader transmit(Request request) {

            try {
                //ensure that no commands to collective2 are ever sent in parallel
                //finish fully reading previous command and close its connection
                //before beginning the next.

                //1. helps lower connection requirements on server side.
                //2. helps keep server from waiting on client side parse
                //3. helps eliminate overlap of sequential signals

                synchronized (lock) {
                    URLConnection connection = request.buildURL().openConnection();
                    connection.setConnectTimeout(timeoutInMs);

                    //pull all the data off the server ASAP
                    ByteArrayOutputStream baost = new ByteArrayOutputStream();

                    InputStream is = connection.getInputStream();
                    int bite;
                    do {
                       bite = is.read();
                       if (bite==-1)  {
                            break;
                       }
                       baost.write(bite);
                    } while (bite!=-1);
                    is.close();
                    //now disconnected from server send finished xml to reader
                    return factory.createXMLEventReader(new ByteArrayInputStream(baost.toByteArray()));
                }
            } catch (XMLStreamException e) {
                String msg = "Unable to parse XML response from Collective2. Sent:"+request;
                logger.error(msg, e);
                throw new C2ServiceException(msg, e, false); //do not send again
            } catch (IOException e) {
                String msg = "Unable to transmit request to Collective2. Attempted:"+request;
                logger.error(msg, e);
                throw new C2ServiceException(msg, e, true); //caller should try again later
            }

    }

}
