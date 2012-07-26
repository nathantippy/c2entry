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
import java.util.Arrays;
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

    public Request secureClone() {
        Request clone = (Request) super.clone();
        if (clone.containsKey(Parameter.Password)) {
            clone.remove((Parameter.Password));
            clone.put(Parameter.Password,"*****");
        }
        return clone;
    }

    public String toString() {
        return buildURL(this, true).toString();
    }

    public static Request parseURL(String url) {

        String[] split = url.split("\\?|&");
        if (split.length<=0 || !split[0].endsWith("signal.mpl")) {
            throw new C2ServiceException("Can not parse url base "+url,false);
        }
        //pull out the command from index 1
        Command command = null;
        if (split[1].startsWith("cmd=")) {
            command = (Command)Parameter.SignalEntryCommand.parse(split[1].substring(4));
         } else {
            throw new C2ServiceException("Can not parse out command "+url,false);
        }


        //all the fields start at 2
        Request request = new Request(command);

        //do not process zero or one
        int i = split.length;
        while(--i>1) {
            String[] arg = split[i].split("=");
            String endsWith = arg[0]+'=';

            Parameter found = lookupParameter(endsWith);
            Object value = found.parse(arg[1]);
            request.put(found,value);
        }

        return request;
    }

    private static Parameter lookupParameter(String endsWith) {
        Parameter found = null;
        for(Parameter p:Parameter.values()) {
            if (p.key().endsWith(endsWith)) {
                found = p;
                break;
            }
        }
        return found;
    }

    public URL buildURL() {
        return buildURL(this, false);
    }

    private URL buildURL(Map<Parameter, Object> activeMap, boolean hidePassword) {
        StringBuilder urlFileQuery = new StringBuilder(urlFile);

        for (Parameter p : activeMap.keySet()) {
            if (Parameter.Password == p && hidePassword) {
                urlFileQuery.append(p.key()).append("PASSWORD");
            } else {
                urlFileQuery.append(p.key());
                if (p.shouldEncode()) {
                    try {
                        urlFileQuery.append(encode(activeMap.get(p)));
                    } catch (UnsupportedEncodingException e) {
                        // should never happen
                        String msg = "UTF-8 is not supported on this platform?";
                        logger.error(msg, e);
                        throw new C2ServiceException(msg, e, false);
                    }
                } else {
                    urlFileQuery.append(activeMap.get(p));
                }
            }
        }

        try {
            return new URL(urlProtocol, urlHost, urlPort, urlFileQuery.toString(), null);
        } catch (MalformedURLException e) {
            throw new C2ServiceException(e, false);
        }
    }

    protected String encode(Object value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value.toString(), "UTF-8");
    }

    public void validate() {
        command().validate(this);
    }
}
