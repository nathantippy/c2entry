/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/15/12
 */
package com.collective2.signalEntry.implementation;

import com.collective2.signalEntry.C2Element;
import com.collective2.signalEntry.C2ElementVisitor;
import com.collective2.signalEntry.C2ServiceException;
import com.collective2.signalEntry.Response;
import com.collective2.signalEntry.adapter.IterableXMLEventReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.collective2.signalEntry.C2Element.ElementStatus;

public class ImplResponse implements Response {

    private final static Logger  logger = LoggerFactory.getLogger(ImplResponse.class);

    private final Future<IterableXMLEventReader>    futureEventReader;
    private final Request                           request;
    private final ResponseManager                   manager;


    //only created by ResponseManager
    ImplResponse(ResponseManager manager, Request request, Future<IterableXMLEventReader> futureEventReader) {
        //fast fail check
        request.validate();

        this.manager = manager;
        this.request = request;
        this.futureEventReader = futureEventReader;

        assert(secureRequest().validate());
    }

    public Request secureRequest() {
        return request.secureClone();
    }

    public Command command() {
        return request.command();
    }

    //do call this method, it loops back to the above call
    public IterableXMLEventReader getXMLEventReader() {
        try {
            return futureEventReader.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new C2ServiceException(e,false);
        } catch (ExecutionException e) {
            throw new C2ServiceException(e,false);
        }
    }


    public Integer getInteger(C2Element element) {
        String value = getString(element).trim();
        if (value.isEmpty()) {
            return -1;
        } else {
            return Integer.parseInt(value);
        }
    }

    public Long getLong(C2Element element) {
        String value = getString(element).trim();
        if (value.isEmpty()) {
            return -1l;
        } else {
            return Long.parseLong(value);
        }
    }

    public Double getDouble(C2Element element) {
        String value = getString(element).trim();
        if (value.isEmpty()) {
            return Double.NaN;
        } else {
            return Double.parseDouble(value);
        }
    }

    public BigDecimal getBigDecimal(C2Element element) {
        String value = getString(element).trim();
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
                throw new C2ServiceException("Unable to read xml",e,false);
            }
            if (event != null) {
                if (event.isStartElement()) {

                    name = event.asStartElement().getName().getLocalPart();

                } else if (event.isCharacters() && name.equalsIgnoreCase(element.localElementName())) {
                    try {
                        reader.close();
                    } catch (XMLStreamException e) {
                        logger.warn("Unable to close xml stream", e);
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
        return getXMLEventReader().rawXML();
    }


    @Override
    public void visitC2Elements(C2ElementVisitor c2ElementVisitor, C2Element ... expected) {
        if (expected.length == 0) {
            logger.warn("expected types can not be checked unless they are passed in",new Exception());
        } else {
            for(C2Element element: expected) {
                request.command().validate(element);
            }
        }
        XMLEventReader reader = getXMLEventReader();
        try {
            Deque<String> stack = new ArrayDeque<String>();
            while (reader.hasNext()) {
                XMLEvent event =  reader.nextEvent();
                if (event.isEndElement()) {
                    stack.pop();
                } else if (event.isStartElement()) {
                    stack.push(event.asStartElement().getName().getLocalPart());
                } else if (event.isCharacters()) {
                    String data = event.asCharacters().getData().trim();
                    C2Element element = C2Element.binaryLookup(stack.peek());
                    if (element != null) {
                        c2ElementVisitor.visit(element, data);
                    }
                }
            }
        } catch (XMLStreamException e) {
            logger.warn("visitC2Elements",e);
        } finally {
            try {
                reader.close();
            } catch (XMLStreamException e) {
                logger.warn("Unable to close xml stream", e);
            }
        }
    }

    @Override
    public <C extends Collection<Integer>> C collectIntegers(final C target, C2Element element) {
    //public Collection<Integer> collectIntegers(final Collection<Integer> target, C2Element element) {

        visitC2Elements(new C2ElementVisitor() {
            @Override
            public void visit(C2Element element, String data) {
                if (C2Element.ElementSignalId==element) {
                    target.add(Integer.parseInt(data));
                }
            }
        },C2Element.ElementSignalId);

        return target;
    }

}
