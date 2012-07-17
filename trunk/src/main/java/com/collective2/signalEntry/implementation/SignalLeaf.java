/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/15/12
 */
package com.collective2.signalEntry.implementation;

import com.collective2.signalEntry.Parameter;
import com.collective2.signalEntry.transmission.BackEndAdapter;

public class SignalLeaf extends ImplSignal {

    private final ImplSignal base;
    private final Parameter  parameter;
    private final Object     value;

    SignalLeaf(ImplSignal base, Parameter parameter, Object value) {
        parameter.validateValue(value);

        this.base = base;
        this.parameter = parameter;
        this.value = value;

    }

    protected BackEndAdapter initLockedAdapter() {
        BackEndAdapter adapter = base.initLockedAdapter();
        adapter.para(parameter, value);
        return adapter;
    }

}
