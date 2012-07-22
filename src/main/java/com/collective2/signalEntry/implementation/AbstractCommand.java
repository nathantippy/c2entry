/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/6/12
 */
package com.collective2.signalEntry.implementation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.collective2.signalEntry.Response;
import com.collective2.signalEntry.adapter.BackEndAdapter;

public abstract class AbstractCommand {

    private final static Logger logger = LoggerFactory.getLogger(AbstractCommand.class);

    public Response send() {

        Request request = buildRequest();
        request.validate();
        return new ImplResponse(backEndAdapter().transmit(request),request.getCommand());

    }
    
    public String toString() {
        return "Command:"+ buildRequest();
    }
    
    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return toString().equals(obj);
    }

    protected abstract Request buildRequest();
    protected abstract BackEndAdapter backEndAdapter();

}
