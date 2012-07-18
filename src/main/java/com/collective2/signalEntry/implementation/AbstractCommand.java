/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/6/12
 */
package com.collective2.signalEntry.implementation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.collective2.signalEntry.Parameter;
import com.collective2.signalEntry.Response;
import com.collective2.signalEntry.adapter.BackEndAdapter;

public abstract class AbstractCommand {

    private final static Logger logger = LoggerFactory.getLogger(AbstractCommand.class);
    private String lazyString;
    
    public Response send() {
        BackEndAdapter initLockedAdapter = null;
        try{
            initLockedAdapter = initLockedAdapter();
            Command command = initLockedAdapter.para(Parameter.SignalEntryCommand);
            return new ImplResponse(initLockedAdapter.transmit(),command);
        } finally {
            if (initLockedAdapter!=null) {
                initLockedAdapter.unlock();
            }
        }
    }
    
    public String toString() {
        //this string will always be the same because all the given params and values
        //are stored as final.  As a result this string is used for equals and hash.
        if (lazyString==null) {
            BackEndAdapter initLockedAdapter = null;
            try{
                initLockedAdapter = initLockedAdapter();
                lazyString = "Command:"+initLockedAdapter.toString();
            } finally {
                if (initLockedAdapter!=null) {
                    initLockedAdapter.unlock();
                }
            }
        }
        return lazyString;
    }
    
    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return toString().equals(obj);
    }

    protected abstract BackEndAdapter initLockedAdapter();

}
