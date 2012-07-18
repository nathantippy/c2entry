/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/6/12
 */
package com.collective2.signalEntry.implementation;

import com.collective2.signalEntry.C2ServiceFactory;
import com.collective2.signalEntry.Parameter;
import com.collective2.signalEntry.adapter.BackEndAdapter;

public class ReversalBase extends ImplReversal {

    private final C2ServiceFactory factory;
    private final Integer systemId;
    private final String password;
    private final String symbol;

    public ReversalBase(Integer systemId, String password, String symbol, C2ServiceFactory factory) {
        this.factory    = factory;
        this.systemId   = systemId;
        this.password   = password;
        this.symbol     = symbol;
    }

    protected BackEndAdapter initLockedAdapter() {

    	BackEndAdapter adapter = factory.adapter();
    	adapter.lock();
        adapter.para(Parameter.SignalEntryCommand, Command.Reverse);
        adapter.para(Parameter.SystemId, systemId);
        adapter.para(Parameter.Password, password);
        adapter.para(Parameter.Symbol,symbol);
        return adapter;

    }



}
