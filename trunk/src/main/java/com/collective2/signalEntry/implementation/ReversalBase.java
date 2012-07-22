/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/6/12
 */
package com.collective2.signalEntry.implementation;

import com.collective2.signalEntry.Parameter;
import com.collective2.signalEntry.adapter.BackEndAdapter;

public class ReversalBase extends ImplReversal {

    private final BackEndAdapter adapter;
    private final Integer systemId;
    private final String password;
    private final String symbol;

    public ReversalBase(Integer systemId, String password, String symbol, BackEndAdapter adapter) {
        this.adapter    = adapter;
        this.systemId   = systemId;
        this.password   = password;
        this.symbol     = symbol;
    }

    protected BackEndAdapter backEndAdapter() {
        return adapter;
    }

    protected Request buildRequest() {
    	Request request = new Request(Command.Reverse);
        request.put(Parameter.SystemId, systemId);
        request.put(Parameter.Password, password);
        request.put(Parameter.Symbol, symbol);
        return request;

    }



}
