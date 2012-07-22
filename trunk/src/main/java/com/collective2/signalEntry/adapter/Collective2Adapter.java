/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/4/12
 */
package com.collective2.signalEntry.adapter;

import java.io.IOException;
import java.net.URLConnection;
import java.util.Map;


import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import com.collective2.signalEntry.implementation.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.collective2.signalEntry.C2ServiceException;
import com.collective2.signalEntry.Parameter;

public class Collective2Adapter implements BackEndAdapter {

    private static final Logger   logger = LoggerFactory.getLogger(Collective2Adapter.class);

    private final XMLInputFactory factory;
    private final static int timeoutInMs = 1000;//one second
    private final static Object lock = new Object();

    public Collective2Adapter() {
        factory = XMLInputFactory.newInstance();
    }

    public XMLEventReader transmit(Request request) {

            try {
                //ensure that no commands to collective2 are ever sent in parallel
                synchronized (lock) {
                    URLConnection connection = request.buildURL().openConnection();
                    connection.setConnectTimeout(timeoutInMs);
                    return factory.createXMLEventReader(connection.getInputStream());
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
