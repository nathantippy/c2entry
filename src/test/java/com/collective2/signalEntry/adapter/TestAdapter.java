/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/14/12
 */
package com.collective2.signalEntry.adapter;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.Map;

import javax.xml.stream.XMLEventReader;

import com.collective2.signalEntry.Parameter;

public class TestAdapter extends StaticSimulationAdapter {

    // keeping it under 255 will allow calls to work in older browsers
    // modern browsers usually top out at 2048
    private final int MAX_URL_LENGTH                = 255;
    private final int MAX_SERIALIZE_OVERHEAD_FACTOR = 2;

    private String    lastURLString;

    public String getLastURLString() {
        return lastURLString;
    }

    @Override
    public XMLEventReader transmit(Map<Parameter, Object> paraMap) {

        //ensure the URL is not too long
        URL url = buildURL(paraMap);
        lastURLString = url.toString();
        assertTrue("Length of url must be under " + MAX_URL_LENGTH + " to work in all supported browsers. URL: " + lastURLString, lastURLString.length() < MAX_URL_LENGTH);

        // at this location we will attempt to serialize the object to ensure it
        // can be done
        try {

            ByteArrayOutputStream baost = new ByteArrayOutputStream(MAX_URL_LENGTH*MAX_SERIALIZE_OVERHEAD_FACTOR);
            ObjectOutputStream oost = new ObjectOutputStream(baost);
            oost.writeObject(paraMap);
        } catch (IOException e) {
            e.printStackTrace();
            fail("Unable to serialize the paraMap:" + e.getLocalizedMessage());
        }

        return super.transmit(paraMap);
    }

}