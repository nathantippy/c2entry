/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/4/12
 */
package com.collective2.signalEntry.adapter;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.stream.XMLEventReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.collective2.signalEntry.C2ServiceException;
import com.collective2.signalEntry.Parameter;
import com.collective2.signalEntry.implementation.Command;

public abstract class BackEndAdapter {

    private static final Logger                 logger      = LoggerFactory.getLogger(BackEndAdapter.class);

    private final String                        urlProtocol = "http";
    private final int                           urlPort     = -1;                                              // default
                                                                                                               // for
                                                                                                               // protocol
    private final String                        urlHost     = "www.collective2.com";
    private final String                        urlFile     = "/cgi-perl/signal.mpl";

    // ensures that we only talk to the server sequentially with the URL
    // requests
    private static final ReentrantLock          lock        = new ReentrantLock();
    // since all calls are sequential the same structures will be used here to
    // hold
    // all the command properties in order to minimize garbage collection.
    private static final Map<Parameter, Object> activeMap   = new EnumMap<Parameter, Object>(Parameter.class);

    public String toString() {
        return buildURL(activeMap, true).toString();
    }

    public URL buildURL(Map<Parameter, Object> activeMap) {
        return buildURL(activeMap, false);
    }

    public URL buildURL(Map<Parameter, Object> activeMap, boolean hidePassword) {
        StringBuilder urlFileQuery = new StringBuilder(urlFile);

        for (Parameter p : activeMap.keySet()) {
            try {
                if (Parameter.Password == p && hidePassword) {
                    urlFileQuery.append(p.key()).append("PASSWORD");
                } else {
                    urlFileQuery.append(p.key());
                    if (p.urlEncode()) {
                        urlFileQuery.append(URLEncoder.encode(activeMap.get(p).toString(), "UTF-8"));
                    } else {
                        urlFileQuery.append(activeMap.get(p));
                    }
                }
            } catch (UnsupportedEncodingException e) {
                // should never happen
                String msg = "UTF-8 is not supported on this platform?";
                logger.error(msg, e);
                throw new C2ServiceException(msg, e);
            }
        }

        try {
            return new URL(urlProtocol, urlHost, urlPort, urlFileQuery.toString(), null);
        } catch (MalformedURLException e) {
            throw new C2ServiceException(e);
        }

    }

    public XMLEventReader transmit() {
        assert (lock.isHeldByCurrentThread());

        // validate that all required params are set before adapter
        Command activeCommand = ((Command) activeMap.get(Parameter.SignalEntryCommand));
        activeCommand.validate(activeMap);

        XMLEventReader response = transmit(activeMap);
        assert (response != null) : "null response from:" + activeMap;
        return response;
    }

    public void unlock() {
        lock.unlock();
    }

    /**
     * Implementation must talk to the back end and return the XMLEventReader
     * 
     * @returnpublic void lockTransmit() { }
     */
    public abstract XMLEventReader transmit(Map<Parameter, Object> paraMap);

    public void para(Parameter parameter, Object value) {
        assert (lock.isHeldByCurrentThread());

        // ((Command)activeMap.get(Parameter.SignalEntryCommand)).validateApplicable(activeMap,parameter);
        parameter.validateValue(value);

        activeMap.put(parameter, value);
    }

    public void lock() {
        lock.lock();
        activeMap.clear();
    }

    public <T> T para(Parameter para) {
        return (T) activeMap.get(para);

    }
}
