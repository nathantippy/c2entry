/**
 * Created with IntelliJ IDEA.
 * User: nate
 * Date: 7/3/12
 * Time: 7:07 PM
 * To change this template use File | Settings | File Templates.
 */
package com.collective2.signalEntry;

import com.collective2.signalEntry.adapter.C2EntryServiceAdapter;
import com.collective2.signalEntry.implementation.ResponseManager;
import com.collective2.signalEntry.journal.C2EntryServiceJournal;
import com.collective2.signalEntry.journal.C2EntryServiceMemoryJournal;

public class C2ServiceFactory {

    private final static long networkDownRetryDelay = 10000l;//try every 10 seconds
    protected final ResponseManager responseManager;

    public C2ServiceFactory(C2EntryServiceAdapter adapter) {
        this.responseManager = new ResponseManager(adapter, new C2EntryServiceMemoryJournal(), networkDownRetryDelay);
    }

    public C2ServiceFactory(C2EntryServiceAdapter adapter, C2EntryServiceJournal journal) {
        this.responseManager = new ResponseManager(adapter, journal, networkDownRetryDelay);
    }

    public C2EntryService signalEntryService(String password, Integer systemId) {
        return new C2EntryService(responseManager, this, password, systemId);
    }

    public C2EntryService signalEntryService(String password, String email) {
        return new C2EntryService(responseManager, this, password, email);
    }

    public C2EntryService signalEntryService(String password, Integer systemId, String email) {
        return new C2EntryService(responseManager, this, password, systemId, email);
    }

    // other services can be added here in the future as we find the need.

}
