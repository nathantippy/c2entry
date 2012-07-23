/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/21/12
 */
package com.collective2.signalEntry.implementation;

import com.collective2.signalEntry.C2ServiceException;
import com.collective2.signalEntry.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.EnumMap;
import java.util.Map;

public class Request extends EnumMap<Parameter, Object> {

    private static final Logger  logger      = LoggerFactory.getLogger(Request.class);
    private static final String  urlProtocol = "http";
    private static final int     urlPort     = -1;
    private static final String  urlHost     = "www.collective2.com";
    private static final String  urlFile     = "/cgi-perl/signal.mpl";

    public Request(Command command) {
        super((Parameter.class));
        super.put(Parameter.SignalEntryCommand, command);
    }

    public Command command() {
        return (Command) get(Parameter.SignalEntryCommand);
    }

    @Override
    public Object put(Parameter parameter, Object value) {
        command().validateApplicable(this, parameter);
        parameter.validateValue(value);
        return super.put(parameter, value);
    }

    public String toString() {
        return buildURL(this, true).toString();
    }

    public URL buildURL() {
        return buildURL(this, false);
    }

    private URL buildURL(Map<Parameter, Object> activeMap, boolean hidePassword) {
        StringBuilder urlFileQuery = new StringBuilder(urlFile);

        for (Parameter p : activeMap.keySet()) {
            try {
                if (Parameter.Password == p && hidePassword) {
                    urlFileQuery.append(p.key()).append("PASSWORD");
                } else {
                    urlFileQuery.append(p.key());
                    if (p.urlEncode()) {
                        String value = activeMap.get(p).toString();
                        urlFileQuery.append(encode(value));
                    } else {
                        urlFileQuery.append(activeMap.get(p));
                    }
                }
            } catch (UnsupportedEncodingException e) {
                // should never happen
                String msg = "UTF-8 is not supported on this platform?";
                logger.error(msg, e);
                throw new C2ServiceException(msg, e, false);
            }
        }

        try {
            return new URL(urlProtocol, urlHost, urlPort, urlFileQuery.toString(), null);
        } catch (MalformedURLException e) {
            throw new C2ServiceException(e, false);
        }

    }

    String encode(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, "UTF-8");
    }

    public void validate() {
        command().validate(this);
    }
}
