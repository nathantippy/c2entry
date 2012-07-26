/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  7/22/12
 */
package com.collective2.signalEntry.implementation;

import com.collective2.signalEntry.Parameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public interface C2EntryServiceJournal {
    C2EntryServiceJournal memoryJournal =new C2EntryServiceJournal() {

        List<Request> list = new ArrayList<Request>();

        @Override
        public Iterator<Request> pending() {
            return list.iterator();
        }

        @Override
        public void persist(Request request) {
            list.add(request);
        }

        @Override
        public void markSent(Request request) {
            list.remove(0);
        }
    };


    Iterator<Request> pending();
    void persist(Request request);
    void markSent(Request request);


}
