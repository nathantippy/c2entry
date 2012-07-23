/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/3/12
 */
package com.collective2.signalEntry.implementation;


import com.collective2.signalEntry.Parameter;
import com.collective2.signalEntry.adapter.BackEndAdapter;

public class SignalBase extends ImplSignal {

    private final ResponseManager responseManager;
    private final Integer systemId;
    private final String password;
    private final Object action;
    private final String instrument;

    public SignalBase(Integer systemId, String password, Object action, String instrument, ResponseManager responseManager) {

        this.responseManager = responseManager;
        this.systemId        = systemId;
        this.password        = password;
        this.action          = action;
        this.instrument      = instrument;
    }

    protected ResponseManager responseManager() {
        return responseManager;
    }

    protected Request buildRequest() {

        Request request = new Request(Command.Signal);
        request.put(Parameter.SystemId, systemId);
        request.put(Parameter.Password, password);

        request.put(Parameter.Instrument, instrument);
        if ("stock".equals(instrument)) {
            request.put(Parameter.StockAction, action);
        } else {
            request.put(Parameter.NonStockAction, action);
        }
        return request;

    }

}
