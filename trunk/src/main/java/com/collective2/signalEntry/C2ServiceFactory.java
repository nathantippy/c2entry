/**
 * Created with IntelliJ IDEA.
 * User: nate
 * Date: 7/3/12
 * Time: 7:07 PM
 * To change this template use File | Settings | File Templates.
 */
package com.collective2.signalEntry;

import com.collective2.signalEntry.adapter.C2EntryServiceAdapter;
import com.collective2.signalEntry.implementation.C2EntryServiceJournal;

public class C2ServiceFactory {

    protected final C2EntryServiceAdapter entryServiceAdapter;
    protected final C2EntryServiceJournal entryServiceJournal;

    public C2ServiceFactory(C2EntryServiceAdapter adapter) {
        this.entryServiceAdapter = adapter;
        this.entryServiceJournal = C2EntryServiceJournal.memoryJournal;
    }

    public C2ServiceFactory(C2EntryServiceAdapter adapter, C2EntryServiceJournal journal) {
        this.entryServiceAdapter = adapter;
        this.entryServiceJournal = journal;
    }

    C2EntryServiceAdapter entryServiceAdapter() {
        return entryServiceAdapter;
    }

    C2EntryServiceJournal entryServiceJournal() {
        return entryServiceJournal;
    }

    public C2EntryService signalEntryService(String password, Integer systemId) {
        return new C2EntryService(this, password, systemId);
    }

    public C2EntryService signalEntryService(String password, String email) {
        return new C2EntryService(this, password, email);
    }

    public C2EntryService signalEntryService(String password, Integer systemId, String email) {
        return new C2EntryService(this, password, systemId, email);
    }

    // other services can be added here in the future as we find the need.

}
