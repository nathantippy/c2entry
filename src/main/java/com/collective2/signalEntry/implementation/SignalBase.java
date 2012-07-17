/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/3/12
 */
package com.collective2.signalEntry.implementation;


import com.collective2.signalEntry.C2ServiceFactory;
import com.collective2.signalEntry.Parameter;
import com.collective2.signalEntry.transmission.BackEndAdapter;

public class SignalBase extends ImplSignal {

    private final C2ServiceFactory factory;
    private final Integer systemId;
    private final String password;
    private final Object action;
    private final String instrument;

    public SignalBase(Integer systemId, String password, Object action, String instrument, C2ServiceFactory factory) {

        this.factory    = factory;
        this.systemId   = systemId;
        this.password   = password;
        this.action     = action;
        this.instrument = instrument;
    }

    protected BackEndAdapter initLockedAdapter() {
    	
        BackEndAdapter adapter = factory.adapter();
        adapter.lock();
        adapter.para(Parameter.SignalEntryCommand, Command.Signal);
        adapter.para(Parameter.SystemId, systemId);
        adapter.para(Parameter.Password, password);

        adapter.para(Parameter.Instrument, instrument);
        if ("stock".equals(instrument)) {
            adapter.para(Parameter.StockAction, action);
        } else {
            adapter.para(Parameter.NonStockAction, action);
        }
        return adapter;

    }

}
