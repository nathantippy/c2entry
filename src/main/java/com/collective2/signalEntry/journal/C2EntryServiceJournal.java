/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  7/22/12
 */
package com.collective2.signalEntry.journal;

import com.collective2.signalEntry.implementation.Request;

import java.util.Iterator;

public interface C2EntryServiceJournal {

    Iterator<Request> pending();
    void append(Request request);
    void markSent(Request request);
    void markRejected(Request request);
    Request[] dropPending();
}
