/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  9/18/12
 */
package com.collective2.signalEntry.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.StringReader;
import java.util.Iterator;

public class IterableXMLEventReader implements XMLEventReader, Iterable<XMLEvent> {

    private static final Logger logger = LoggerFactory.getLogger(IterableXMLEventReader.class);

    private static final XMLInputFactory factory = XMLInputFactory.newInstance();

    private final String            rawXML;
    private final XMLEventReader    xmlEventReader;

    public IterableXMLEventReader(XMLEventReader xmlEventReader) throws XMLStreamException {
        this(captureXMLText(xmlEventReader));
    }

    public IterableXMLEventReader(String rawXML) throws XMLStreamException {
         this.rawXML = rawXML;
         this.xmlEventReader = factory.createXMLEventReader(new StringReader(rawXML));
    }

    private static String captureXMLText(XMLEventReader reader) {
        StringBuilder builder = new StringBuilder(320);
        try{
            while (reader.hasNext()) {
                XMLEvent event;
                try {
                    event = reader.nextEvent();
                } catch (XMLStreamException e) {
                    e.printStackTrace();
                    return builder.toString();
                }
                if (!event.isEndDocument()) {
                    String value = event.toString();
                    if (null != value) {
                        builder.append(value.trim());
                    }
                }
            }
        } finally {
            try {
                reader.close();
            } catch (XMLStreamException e) {
                logger.warn("Unable to close xml stream", e);
            }
        }
        return builder.toString();
    }

    public String rawXML() {
        return rawXML;
    }

    @Override
    public Iterator<XMLEvent> iterator() {
        return restart();
    }

    public IterableXMLEventReader restart() {
        try {
            return new IterableXMLEventReader(rawXML);
        } catch (XMLStreamException e) {
            logger.warn("Should have caught this problem upon construction", e);
            throw new RuntimeException(e); //this should never happen
        }
    }

    @Override
    public XMLEvent nextEvent() throws XMLStreamException {
        return xmlEventReader.nextEvent();
    }

    @Override
    public boolean hasNext() {
        return xmlEventReader.hasNext();
    }

    @Override
    public Object next() {
        return xmlEventReader.next();
    }

    @Override
    public void remove() {
        xmlEventReader.remove();
    }

    @Override
    public XMLEvent peek() throws XMLStreamException {
        return xmlEventReader.peek();
    }

    @Override
    public String getElementText() throws XMLStreamException {
        return xmlEventReader.getElementText();
    }

    @Override
    public XMLEvent nextTag() throws XMLStreamException {
        return xmlEventReader.nextTag();
    }

    @Override
    public Object getProperty(String name) throws IllegalArgumentException {
        return xmlEventReader.getProperty(name);
    }

    @Override
    public void close() throws XMLStreamException {
        xmlEventReader.close();
    }
}
