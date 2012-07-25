/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/4/12
 */
package com.collective2.signalEntry.adapter;

import com.collective2.signalEntry.implementation.Request;

import javax.xml.stream.XMLEventReader;
import java.util.Map;

public interface C2EntryServiceAdapter {

    public abstract XMLEventReader transmit(Request request);


}
