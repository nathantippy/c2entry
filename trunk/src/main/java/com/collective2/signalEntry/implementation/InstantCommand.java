/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/10/12
 */
package com.collective2.signalEntry.implementation;

import com.collective2.signalEntry.C2ServiceFactory;
import com.collective2.signalEntry.adapter.BackEndAdapter;

public abstract class InstantCommand extends AbstractCommand {

    private final C2ServiceFactory factory;

    public InstantCommand(C2ServiceFactory factory) {
    	this.factory = factory;
    }

    protected abstract void initAdapter(BackEndAdapter adapter);
    
    @Override
    protected BackEndAdapter initLockedAdapter() {
    	BackEndAdapter adapter = factory.adapter();
    	adapter.lock();
    	initAdapter(adapter);
    	return adapter;
    }
}
