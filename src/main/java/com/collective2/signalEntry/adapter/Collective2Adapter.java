/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/4/12
 */
package com.collective2.signalEntry.adapter;

import java.io.IOException;
import java.util.Map;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.collective2.signalEntry.C2ServiceException;
import com.collective2.signalEntry.Parameter;

public class Collective2Adapter extends BackEndAdapter {

    private static final Logger   logger = LoggerFactory.getLogger(Collective2Adapter.class);

    private final XMLInputFactory factory;

    public Collective2Adapter() {
        factory = XMLInputFactory.newInstance();
    }

    public XMLEventReader transmit(Map<Parameter, Object> paraMap) {

        try {
            return factory.createXMLEventReader(buildURL(paraMap).openStream());
        } catch (XMLStreamException e) {
            String msg = "Unable to parse XML from Collective2. Sent:"+buildURL(paraMap,true);
            logger.error(msg, e);
            throw new C2ServiceException(msg, e);
        } catch (IOException e) {
            String msg = "Unable to get data from Collective2. Sent:"+buildURL(paraMap,true);
            logger.error(msg, e);
            throw new C2ServiceException(msg, e);
        }
    }

}
