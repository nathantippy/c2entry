/**
 * Created with IntelliJ IDEA.
 * User: nate
 * Date: 7/3/12
 * Time: 7:07 PM
 * To change this template use File | Settings | File Templates.
 */
package com.collective2.signalEntry;

import com.collective2.signalEntry.adapter.BackEndAdapter;

public class C2ServiceFactory {

    protected final BackEndAdapter adapter;

    public C2ServiceFactory(BackEndAdapter adapter) {
        this.adapter = adapter;
    }

    public BackEndAdapter adapter() {
        return adapter;
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
