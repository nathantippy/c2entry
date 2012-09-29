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
import com.collective2.signalEntry.adapter.IterableXMLEventReader;
import com.collective2.signalEntry.approval.C2EntryHumanApproval;
import com.collective2.signalEntry.journal.C2EntryServiceJournal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.concurrent.*;

public class ResponseManager {
    private static final Logger         logger = LoggerFactory.getLogger(ResponseManager.class);

    private final C2EntryServiceAdapter adapter;
    private final C2EntryServiceJournal journal;
    private final long                  networkDownRetryDelay;
    private final boolean               showDeepStacks = true;
    private final C2EntryHumanApproval  approvalRequestable;
    private boolean                     isClean;//is false when out of sync with journal
    private C2ServiceException          haltingException;


    private static final String        threadName = "C2EntryServiceResponseManager";
    private static final ThreadFactory threadFactory = new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName(threadName);
            thread.setPriority(Thread.MAX_PRIORITY);
            return thread;
        }
    };

    //Must be singleThreadExecutor only because each callable must be called sequentially
    private final static ExecutorService    executor = Executors.newSingleThreadExecutor(threadFactory);
    private final static Runnable           placeHolder = new Runnable() {
        @Override
        public void run() {
        }
    };

    public ResponseManager(C2EntryServiceAdapter    adapter,
                           C2EntryServiceJournal    journal,
                           C2EntryHumanApproval     approvalRequestable,
                           long                     networkDownRetryDelay
                           ) {
        this.adapter                = adapter;
        this.journal                = journal;
        this.approvalRequestable    = approvalRequestable;
        this.networkDownRetryDelay  = networkDownRetryDelay;
    }

    public Exception getHaltingException(){
        return haltingException;
    }

    public void awaitPending(long seconds) throws TimeoutException {
        try {
            //if executor has gotten down to this one then everything else is done
            executor.submit(placeHolder).get(seconds,TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            throw new C2ServiceException("awaitPending",e.getCause(),false);
        }
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

    public Request[] dropPending() {
        return journal.dropPending();
    }

    public synchronized void reloadPendingRequests(String password) {
        if (!isClean) {
            //reload old pending requests
            Iterator<Request> iterator = this.journal.pending();
            if (iterator.hasNext()) {
                //never modify object passed in or this may leak the password out!
                Request journalInstance = iterator.next();
                Request request = journalInstance.secureClone();
                if (request.containsKey(Parameter.Password)) {
                    request.put(Parameter.Password, password);
                }
                Response r = new ImplResponse(this, request, executor.submit(callable(request, journalInstance)));
                 //Note: may want to add a recovery listener here
            }
            while(iterator.hasNext()) {
                //never modify object passed in or this may leak the password out!
                Request journalInstance = iterator.next();
                Request request = journalInstance.secureClone();
                if (request.containsKey(Parameter.Password)) {
                    request.put(Parameter.Password, password);
                }
                Response r = new ImplResponse(this, request, executor.submit(callable(request, journalInstance)));
                 //Note: may want to add a recovery listener here
            }
            isClean=true;
        }
    }

    public Response fetchResponse(Request request) {
        assert(request.validate());

        synchronized (this) {
            Request journalInstance = request.secureClone();//use the same instance for all journal work
            journal.append(journalInstance); //must add to journal before submit to executor
            if (!journalInstance.isApprovalKnown() && journalInstance.command().approvalRequired()) {
                approvalRequestable.oneMoreRequest(journalInstance);
            }
            return new ImplResponse(this, request, executor.submit(callable(request,journalInstance)));
        }
    }

    private Callable<IterableXMLEventReader> callable(final Request request, final Request journalInstance) {

        return new Callable<IterableXMLEventReader>() {

            C2ServiceException optionalStackTrace = showDeepStacks ?
                                                    new C2ServiceException("Originating Call Stack",false) :
                                                    null;

            @Override
            public IterableXMLEventReader call() throws Exception {

                try {
                    if (!journalInstance.isApprovalKnown() && journalInstance.command().approvalRequired()) {
                        //must get approval for these!
                        assert(journal.pending().next().equals(journalInstance));
                        approvalRequestable.waitForApproval(journal.pending());
                    }

                    if (!journalInstance.isApproved()) {
                       journal.markRejected(journalInstance);
                       return new IterableXMLEventReader("<rejected>not approved</rejected>");
                    };

                    //was validated upon construction but assert it was not changed in the meantime
                    assert(request.validate());
                    //all down stream requests must see the same halting exception until its reset.
                    if (haltingException != null ) {
                        throw haltingException;
                    }

                    boolean  tryAgain = false;
                    do {
                        try {
                            //exceptions thrown here are because
                            // * the network is down and we should try later
                            // * the response was not readable - must stop all
                            IterableXMLEventReader eventReader = adapter.transmit(request);
                            synchronized (ResponseManager.this) {
                                //exceptions thrown here are because
                                // * database was unable to change flag on request to sent - must stop all
                                journal.markSent(journalInstance);
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

                } catch (RuntimeException e) {
                    if (optionalStackTrace!=null) {
                        optionalStackTrace.overrideCause(e);
                        throw optionalStackTrace;
                    } else {
                        throw e;
                    }
                }
            }

        };
    }


}
