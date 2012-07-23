/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  7/22/12
 */
package com.collective2.signalEntry.implementation;

import java.util.Collections;
import java.util.Iterator;

public interface EntryServiceJournal {
    EntryServiceJournal No_Op = new EntryServiceJournal() {
        @Override
        public Iterator<Request> pending() {
            return Collections.emptyIterator();
        }

        @Override
        public void persist(Request request) {
            //do nothing
        }

        @Override
        public void markSent(Request request) {
           //do nothing
        }
    };


    Iterator<Request> pending();
    void persist(Request request);
    void markSent(Request request);


}
