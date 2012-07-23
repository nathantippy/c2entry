/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  7/22/12
 */
package com.collective2.signalEntry.implementation;

import com.collective2.signalEntry.Response;
import com.collective2.signalEntry.adapter.BackEndAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

public class ResponseManager {
    private static final Logger         logger = LoggerFactory.getLogger(ResponseManager.class);
    private Thread                      daemon;
    private final BackEndAdapter        adapter;
    private ImplResponse                lastResponse; //tail of linked list
    private ImplResponse                headResponse; //head of linked list of pending responses
    private final AtomicInteger         pendingCount; //count of responses in linked list
    private final EntryServiceJournal   journal;

    public ResponseManager(BackEndAdapter adapter) {
        this.adapter        = adapter;
        this.pendingCount   = new AtomicInteger();
        this.journal        = EntryServiceJournal.No_Op;

        //reload old pending requests
        Iterator<Request> iterator = this.journal.pending();
        if (iterator.hasNext()) {
            Request request = iterator.next();
            headResponse = new ImplResponse(this, request);
            lastResponse = headResponse;
        }
        while(iterator.hasNext()) {
            Request request = iterator.next();
            ImplResponse newResponse = new ImplResponse(this, request);
            lastResponse.next(newResponse);
            lastResponse = newResponse;
        }

    }

    public BackEndAdapter adapter() {
        return adapter;
    }

    ImplResponse head() {
        return headResponse;
    }

    synchronized void finished(ImplResponse response) {
        assert(response == headResponse);
        if (headResponse == response) {
            pendingCount.decrementAndGet();
            journal.markSent(response.request());
            headResponse = headResponse.next();
            if (headResponse==null) {
                lastResponse = null;
                daemon = null; //cleanup thread
            }
        } else {
            logger.warn("thread issue: finished response is not head.");
        }
    }


    synchronized public Response fetchResponse(Request request) {
        ImplResponse newResponse = new ImplResponse(this, request);
        journal.persist(request);
        if (lastResponse == null) {
            assert(headResponse==null);
            headResponse = newResponse;
            assert(headResponse!=null);
            //start new thread to finish new list of requests
            assert(daemon==null) : "Daemon thread already running";
            daemon = new Thread(new Runnable() {
                @Override
                public void run() {
                    ImplResponse temp = headResponse;
                    while (temp!=null) {

                        temp.transmit();
                        //next can be modified under this same lock
                        synchronized(ResponseManager.this) {
                            temp = temp.next();
                        }
                    }
                    //thread exits when linked list is empty
                }
            });
            daemon.setDaemon(true);
            daemon.setName("ResponseManager");
            daemon.start();

        } else {
            assert(headResponse!=null);
            lastResponse.next(newResponse);
        }

        pendingCount.incrementAndGet();
        lastResponse = newResponse;
        return newResponse;
    }


}
