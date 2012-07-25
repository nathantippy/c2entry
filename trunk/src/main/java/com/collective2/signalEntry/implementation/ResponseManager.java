/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  7/22/12
 */
package com.collective2.signalEntry.implementation;

import com.collective2.signalEntry.C2ServiceException;
import com.collective2.signalEntry.Parameter;
import com.collective2.signalEntry.Response;
import com.collective2.signalEntry.adapter.C2EntryServiceAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLEventReader;
import java.util.Iterator;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ResponseManager {
    private static final Logger         logger = LoggerFactory.getLogger(ResponseManager.class);

    private final C2EntryServiceAdapter adapter;
    private final C2EntryServiceJournal journal;

    private final Lock                  lock;

    private static final String        threadName = "ResponseManager";
    private static final ThreadFactory threadFactory = new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName(threadName);
            return thread;
        }
    };

    //Must be singleThreadExecutor only because each callable must be called sequentially
    private final ExecutorService       executor = Executors.newSingleThreadExecutor(threadFactory);
    private final static Runnable       placeHolder = new Runnable() {
        @Override
        public void run() {
        }
    };

    public ResponseManager(C2EntryServiceAdapter adapter, C2EntryServiceJournal journal, String password) {
        this.adapter        = adapter;
        this.journal        = journal;
        this.lock           = new ReentrantLock();
        reloadPendingRequests(password);
    }

    private void reloadPendingRequests(String password) {
        //reload old pending requests
        Iterator<Request> iterator = this.journal.pending();
        if (iterator.hasNext()) {
            //never modify object passed in or this may leak the password out!
            Request request = iterator.next().secureClone();
            if (request.containsKey(Parameter.Password)) {
                request.remove(Parameter.Password);
                request.put(Parameter.Password, password);
            }
            executor.submit(new ImplResponse(this, request)); //Note: may want to add a recovery listener here
        }
        while(iterator.hasNext()) {
            //never modify object passed in or this may leak the password out!
            Request request = iterator.next().secureClone();
            if (request.containsKey(Parameter.Password)) {
                request.remove(Parameter.Password);
                request.put(Parameter.Password, password);
            }
            executor.submit(new ImplResponse(this, request)); //Note: may want to add a recovery listener here
        }
    }

    synchronized public Response fetchResponse(Request request) {
        ImplResponse newResponse = new ImplResponse(this, request);
        journal.persist(request.secureClone());
        executor.submit(newResponse);
        return newResponse;
    }



    public void awaitPending() {
        try {
            //if executor has gotten down to this one then everything else is done
            executor.submit(placeHolder).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            throw new C2ServiceException("awaitPending",e.getCause(),false);
        }

    }

    //rarely needed in production....  if ever
    public void shutdown() {
        executor.shutdown();
        try {
            executor.awaitTermination(20,TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public XMLEventReader xmlEventReader( final ImplResponse response) {
        try {
            return executor.submit(response).get();//block until eventReader has been populated.
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new C2ServiceException("xmlEventReader",e,!response.hasData());
        } catch (ExecutionException e) {
            if (e.getCause() instanceof C2ServiceException) {
                throw (C2ServiceException)e.getCause();
            } else {
                throw new C2ServiceException("xmlEventReader",e.getCause(),!response.hasData());
            }
        }
    }

    public XMLEventReader transmit(Request request) {
        //global lock that enables all processing to pause if needed.
        lock.lock();
        try {
            boolean  tryAgain = false;
            do {
                try {
                    XMLEventReader eventReader = adapter.transmit(request);
                    //TODO: move unlock into here and add unlock mechanism for clearing fault?
                    synchronized (this) {
                        journal.markSent(request.secureClone());
                        //TODO: what should be done if journal throws trying to clear this?
                    }
                    return eventReader;

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
            throw new C2ServiceException("Unable to execute.",true);
        } finally {
            lock.unlock();
        }
    }
}
