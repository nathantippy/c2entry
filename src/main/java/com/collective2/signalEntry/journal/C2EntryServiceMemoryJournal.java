/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  7/27/12
 */
package com.collective2.signalEntry.journal;

import com.collective2.signalEntry.C2ServiceException;
import com.collective2.signalEntry.implementation.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;



public class C2EntryServiceMemoryJournal implements C2EntryServiceJournal {

    private final static Logger logger = LoggerFactory.getLogger(C2EntryServiceMemoryJournal.class);
    private final List < Request > list = new ArrayList<Request>();

    @Override
    public Iterator<Request> pending() {
        return list.iterator();
    }

    @Override
    public void append(Request request) {
        list.add(request);
    }

    @Override
    public void markRejected(Request request) {
        Request oldPending = list.remove(0);
        if (!request.equals(oldPending)) {
            throw new C2ServiceException("Expected to finish "+oldPending+" but instead was rejected "+request,false);
        }
    }

    @Override
    public void markSent(Request request) {
        Request oldPending = list.remove(0);
        if (!request.equals(oldPending)) {
            throw new C2ServiceException("Expected to finish "+oldPending+" but instead was sent "+request,false);
        }
    }

    @Override
    public Request[] dropPending() {
        Request[] dropped = new Request[list.size()];
        int d = 0;
        while (d<list.size()) {
            dropped[d] = list.get(d++).secureClone();
        }

        logger.warn("dropped pending requests");
        list.clear();
        return dropped;
    }

}
