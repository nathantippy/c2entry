/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/7/12
 */
package com.collective2.signalEntry.transmission.simulationXML;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;


public abstract class SimulatedResponse implements XMLEventReader {

    private final BlockingQueue<XMLEvent> queue = new SynchronousQueue<XMLEvent>();
    protected static final XMLEventFactory eventFactory = XMLEventFactory.newInstance();
    private XMLEvent next=null;

    public SimulatedResponse() {
        startServer();
        waitOnNext();
    }

    private void waitOnNext() {
        if (next!=null && next.isEndDocument()) {
            next = null;
        } else {
            try {
                next = queue.take();
            } catch (InterruptedException e) {
                //do nothing just exit
            }
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

    public abstract void serverSideEventProduction(BlockingQueue<XMLEvent> queue);

    private void startServer() {
        //faux server  how can I wrap this inside above object?
        new Thread(new Runnable() {
            public void run() {
                serverSideEventProduction(queue);
            }
        }
        ).start();
    }
}
