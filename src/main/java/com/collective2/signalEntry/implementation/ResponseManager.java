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
import com.collective2.signalEntry.journal.C2EntryServiceJournal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLEventReader;
import java.util.Iterator;
import java.util.concurrent.*;

public class ResponseManager {
    private static final Logger         logger = LoggerFactory.getLogger(ResponseManager.class);

    private final C2EntryServiceAdapter adapter;
    private final C2EntryServiceJournal journal;
    private C2ServiceException          haltingException;
    private final long                  networkDownRetryDelay;


    private static final String        threadName = "C2EntryServiceResponseManager";
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
    private ExecutorService             executor = Executors.newSingleThreadExecutor(threadFactory);
    private final static Runnable       placeHolder = new Runnable() {
        @Override
        public void run() {
        }
    };

    public ResponseManager(C2EntryServiceAdapter adapter, C2EntryServiceJournal journal, String password, long networkDownRetryDelay) {
        this.adapter                = adapter;
        this.journal                = journal;
        this.networkDownRetryDelay  = networkDownRetryDelay;
        reloadPendingRequests(password);
    }

    public Exception getHaltingException(){
        return haltingException;
    }

    public void reset() {
        //dumps all pending, after getting halting exception journal can be asked for pending if desired.
        this.journal.dropPending();
        this.executor.shutdownNow();
        this.executor = Executors.newSingleThreadExecutor(threadFactory);
    }


    private void reloadPendingRequests(String password) {
        //reload old pending requests
        Iterator<Request> iterator = this.journal.pending();
        if (iterator.hasNext()) {
            //never modify object passed in or this may leak the password out!
            Request request = iterator.next().secureClone();
            if (request.containsKey(Parameter.Password)) {
                request.put(Parameter.Password, password);
            }
            executor.submit(new ImplResponse(this, request)); //Note: may want to add a recovery listener here
        }
        while(iterator.hasNext()) {
            //never modify object passed in or this may leak the password out!
            Request request = iterator.next().secureClone();
            if (request.containsKey(Parameter.Password)) {
                request.put(Parameter.Password, password);
            }
            executor.submit(new ImplResponse(this, request)); //Note: may want to add a recovery listener here
        }
    }

    public Response fetchResponse(Request request) {
        ImplResponse newResponse = new ImplResponse(this, request);
        synchronized (this) {
            journal.append(request.secureClone());
            assert(request.validate());
            assert(newResponse.secureRequest().validate());
            executor.submit(newResponse);
        }
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
        //all down stream requests must see the same halting exception until its reset.
        if (haltingException !=null ) {
            throw haltingException;
        }

        boolean  tryAgain = false;
        do {
            try {
                journal.awaitApproval(request.secureClone());
                //exceptions thrown here are because
                // * the network is down and we should try later
                // * the response was not readable - must stop all
                XMLEventReader eventReader = adapter.transmit(request);
                synchronized (this) {
                    //exceptions thrown here are because
                    // * database was unable to change flag on request to sent - must stop all
                    journal.markSent(request.secureClone());
                }
                return eventReader;

            } catch (C2ServiceException e) {
                tryAgain = e.tryAgain();//if true wait for configured delay and try again.
                if (tryAgain) {
                    try {
                        Thread.sleep(networkDownRetryDelay);
                    } catch (InterruptedException ie) {
                        throw e; //this is not a halting exception
                    }
                } else {
                    haltingException = e;
                    throw haltingException;
                }
            } catch (Exception ex) {
                haltingException = new C2ServiceException(request.toString(),ex,false);
                throw haltingException;
            }
        } while (tryAgain);
        throw new C2ServiceException("Unable to execute.",true);

    }
}
