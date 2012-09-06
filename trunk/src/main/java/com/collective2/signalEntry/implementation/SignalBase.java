/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/3/12
 */
package com.collective2.signalEntry.implementation;

import com.collective2.signalEntry.ActionForNonStock;
import com.collective2.signalEntry.ActionForStock;
import com.collective2.signalEntry.Instrument;
import com.collective2.signalEntry.Parameter;

public class SignalBase extends ImplSignal {

    private final ResponseManager responseManager;
    private final Integer systemId;
    private final String password;
    private final SignalAction action;
    private final Instrument instrument;

    public SignalBase(Integer systemId, String password, ActionForStock action, ResponseManager responseManager) {
        this(systemId,password,action.action(),Instrument.Stock,responseManager);
    }

    public SignalBase(Integer systemId, String password, ActionForNonStock action, Instrument instrument, ResponseManager responseManager) {
        this(systemId,password,action.action(),instrument,responseManager);
        assert(instrument!=Instrument.Stock);
    }

    private SignalBase(Integer systemId, String password, SignalAction action, Instrument instrument, ResponseManager responseManager) {

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
        request.put(Parameter.Action, action);
        return request;

    }

}
