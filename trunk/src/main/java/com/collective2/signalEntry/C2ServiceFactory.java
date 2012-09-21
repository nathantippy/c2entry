/**
 * Created with IntelliJ IDEA.
 * User: nate
 * Date: 7/3/12
 * Time: 7:07 PM
 * To change this template use File | Settings | File Templates.
 */
package com.collective2.signalEntry;

import com.collective2.signalEntry.adapter.C2EntryServiceAdapter;
import com.collective2.signalEntry.adapter.Collective2Adapter;
import com.collective2.signalEntry.approval.C2EntryHumanApproval;
import com.collective2.signalEntry.implementation.ResponseManager;
import com.collective2.signalEntry.journal.C2EntryServiceJournal;
import com.collective2.signalEntry.journal.C2EntryServiceLogFileJournal;
import com.collective2.signalEntry.journal.C2EntryServiceMemoryJournal;

import java.io.File;

public class C2ServiceFactory {

    private final static long networkDownRetryDelay = 10000l;//try every 10 seconds
    protected final ResponseManager responseManager;

    public C2ServiceFactory() {
        this.responseManager = new ResponseManager(new Collective2Adapter(),
                new C2EntryServiceMemoryJournal(),
                C2EntryHumanApproval.ApproveAll,
                networkDownRetryDelay);
    }

    public C2ServiceFactory(C2EntryServiceAdapter adapter) {
        this.responseManager = new ResponseManager(adapter,
                new C2EntryServiceMemoryJournal(),
                C2EntryHumanApproval.ApproveAll,
                networkDownRetryDelay);
    }

    public C2ServiceFactory(C2EntryServiceAdapter adapter,
                            C2EntryHumanApproval approval) {
        this.responseManager = new ResponseManager(adapter,
                new C2EntryServiceMemoryJournal(),
                approval,
                networkDownRetryDelay);
    }

    public C2ServiceFactory(C2EntryServiceAdapter adapter,
                            C2EntryServiceJournal journal) {
        this.responseManager = new ResponseManager(adapter,
                journal,
                C2EntryHumanApproval.ApproveAll,
                networkDownRetryDelay);
    }

    public C2ServiceFactory(C2EntryServiceAdapter adapter,
                            File logJournalFile,
                            int rollAfterBytes) {
        this.responseManager = new ResponseManager(adapter,
                new C2EntryServiceLogFileJournal(logJournalFile, rollAfterBytes),
                C2EntryHumanApproval.ApproveAll,
                networkDownRetryDelay);
    }

    public C2ServiceFactory(C2EntryServiceAdapter adapter,
                            File logJournalFile,
                            int rollAfterBytes,
                            C2EntryHumanApproval approval) {
        this.responseManager = new ResponseManager(adapter,
                new C2EntryServiceLogFileJournal(logJournalFile, rollAfterBytes),
                approval,
                networkDownRetryDelay);
    }

    public C2ServiceFactory(C2EntryServiceAdapter adapter,
                            C2EntryServiceJournal journal,
                            C2EntryHumanApproval approval,
                            long networkDownRetryMS) {
        this.responseManager = new ResponseManager(adapter, journal, approval, networkDownRetryMS);
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

}
