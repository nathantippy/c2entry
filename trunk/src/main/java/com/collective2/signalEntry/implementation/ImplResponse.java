/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/15/12
 */
package com.collective2.signalEntry.implementation;

import static com.collective2.signalEntry.C2Element.ElementStatus;

import java.math.BigDecimal;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import com.collective2.signalEntry.adapter.BackEndAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.collective2.signalEntry.*;

public class ImplResponse implements Response {

    private final static Logger  logger = LoggerFactory.getLogger(ImplResponse.class);

    //lazy init
    private XMLEventReader                  eventReader;

    private final Request                   request;
    private final ResponseManager           manager;
    private ImplResponse                    next;

    //only created by ResponseManager
    ImplResponse(ResponseManager manager, Request request) {
        //fast fail check
        request.validate();

        this.manager = manager;
        this.request = request;

    }

    Request request() {
        return request;
    }

    void transmit() {
        synchronized (this) {
            boolean  tryAgain = false;
            do {
                try {
                     if (eventReader==null) {
                        eventReader = manager.adapter().transmit(request);
                        manager.finished(this);
                     }
                } catch (C2ServiceException e) {
                    tryAgain = e.tryAgain();//if true wait for configured delay and try again.
                    if (tryAgain) {
                        try {
                            Thread.sleep(10000l);//10 seconds, NOTE: add configuration for this
                        } catch (InterruptedException ie) {
                            throw e;
                        }
                    } else {
                        throw e;
                    }
                }
            } while (tryAgain);
        }
    }

    public XMLEventReader getXMLEventReader() {

        //before transmitting this request all previous requests must be completed
        ImplResponse temp = manager.head();
        while (temp!=this) {
            synchronized (this) {
               if (eventReader != null) {
                   break;
               }
            }
            temp.transmit(); //never call this while holding my own lock here
            temp = temp.next();
        }

        transmit();
        assert(eventReader!=null);
        return eventReader;
    }


    public Integer getInteger(C2Element element) {
        String value = getString(element);
        if (value.isEmpty()) {
            return -1;
        } else {
            return Integer.parseInt(value);
        }
    }

    public Long getLong(C2Element element) {
        String value = getString(element);
        if (value.isEmpty()) {
            return -1l;
        } else {
            return Long.parseLong(value);
        }
    }

    public Double getDouble(C2Element element) {
        String value = getString(element);
        if (value.isEmpty()) {
            return Double.NaN;
        } else {
            return Double.parseDouble(value);
        }
    }

    public BigDecimal getBigDecimal(C2Element element) {
        String value = getString(element);
        if (value.isEmpty()) {
            return BigDecimal.ZERO;
        } else {
            return new BigDecimal(value);
        }
    }

    /**
     * Helper method for pulling a single value out of the returned XML. The
     * same element name is never nested in the collective2 xml. The C2Element
     * enum contains the leaf element names which can be used. For instances
     * where the element occurs multiple times only the first instance is
     * returned.
     * 
     * @param element
     * @return value if found and "" the empty string if element was not found.
     *         myValue.isEmpty() should be used to determine if it was not
     *         found.
     * 
     */
    public String getString(C2Element element) {

        request.command().validate(element);

        XMLEventReader reader = getXMLEventReader();
        String name = "";

        while (reader.hasNext()) {
            XMLEvent event = null;
            try {
                event = reader.nextEvent();
            } catch (XMLStreamException e) {
                e.printStackTrace(); // To change body of catch statement use
                                     // File | Settings | File Templates.
            }
            if (event != null) {
                if (event.isStartElement()) {

                    name = event.asStartElement().getName().getLocalPart();

                } else if (event.isCharacters() && name.equalsIgnoreCase(element.localElementName())) {
                    try {
                        reader.close();
                    } catch (XMLStreamException e) {
                        e.printStackTrace(); // To change body of catch
                                             // statement use File | Settings |
                                             // File Templates.
                    }
                    return event.asCharacters().getData();
                }
            }
        }
        try {
            reader.close();
        } catch (XMLStreamException e) {
            logger.warn("Unable to close xml stream", e);
        }
        return "";
    }

    public Boolean isOk() {
        String value = getString(ElementStatus).trim().toLowerCase();
        return value.startsWith("ok");
    }
    
    public String getXML() {
       
            XMLEventReader reader = getXMLEventReader();
            StringBuilder builder = new StringBuilder();
            int tabs = 0;
            while (reader.hasNext()) {
                XMLEvent event;
                try {
                    event = reader.nextEvent();
                } catch (XMLStreamException e) {
                    e.printStackTrace();
                    return builder.toString();
                }
                if (event.isEndElement()) {
                    tabs--;
                }
                builder.append("\n");
                int x = tabs;
                while (--x > 0) {
                    builder.append("  ");
                }
                String line = event.toString().trim();
                if (line.length()>0) {
                    builder.append(line);
                }

                if (event.isStartElement()) {
                    tabs++;
                }
            }
            try {
                reader.close();
            } catch (XMLStreamException e) {
                logger.warn("Unable to close xml stream", e);
            }
            builder.append("/n");
            return builder.toString();

    }

    public void next(ImplResponse newResponse) {
        next = newResponse;
    }

    public ImplResponse next() {
        return next;
    }
}
