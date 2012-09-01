/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/7/12
 */
package com.collective2.signalEntry.adapter.simulationXML;

import java.nio.channels.AsynchronousByteChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.*;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;


public abstract class SimulatedResponse implements XMLEventReader {

    private final Iterator<XMLEvent> events;
    protected static final XMLEventFactory eventFactory = XMLEventFactory.newInstance();
    private XMLEvent next = null;

    public SimulatedResponse(Iterator<XMLEvent> events) {
        this.events = events;
        waitOnNext();
    }

    private void waitOnNext() {
        if (events.hasNext()) {
            next = events.next();
        } else {
            next = null;
        }
    }

    public XMLEvent nextEvent() throws XMLStreamException {
        XMLEvent result = next;
        waitOnNext();
        return result;
    }

    public boolean hasNext() {
        return next!=null;
    }

    public Object next() {
        XMLEvent result = next;
        waitOnNext();
        return result;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public XMLEvent peek() throws XMLStreamException {
        return next;
    }

    public String getElementText() throws XMLStreamException {
        XMLEvent event = (XMLEvent) next();
        return event.asCharacters().toString();
    }

    public XMLEvent nextTag() throws XMLStreamException {
        return nextEvent();
    }

    public Object getProperty(String name) throws IllegalArgumentException {
        return "Faux";
    }

    public void close() throws XMLStreamException {
        //Faux object, nothing to close
    }

}
