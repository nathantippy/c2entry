/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/6/12
 */
package com.collective2.signalEntry.implementation;

import com.collective2.signalEntry.Parameter;
import com.collective2.signalEntry.adapter.BackEndAdapter;

public class ReversalLeaf extends ImplReversal {

    private final ImplReversal base;
    private final Parameter parameter;
    private final Object value;

    ReversalLeaf(ImplReversal base, Parameter parameter, Object value) {
        parameter.validateValue(value);
        this.base = base;
        this.parameter = parameter;
        this.value = value;
    }

    protected ResponseManager responseManager() {
        return base.responseManager();
    }

    protected Request buildRequest() {
        Request request = base.buildRequest();
        request.put(parameter, value);
        return request;
    }
}
