/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/14/12
 */
package com.collective2.signalEntry;

import static com.collective2.signalEntry.C2Element.ElementStatus;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.collective2.signalEntry.adapter.BackEndAdapter;
import com.collective2.signalEntry.adapter.Collective2Adapter;

public class LiveTest {

    public static final Logger    logger                 = LoggerFactory.getLogger(LiveTest.class);

    public static final String    PROPERTY_KEY_SYSTEM_ID = "c2entry_systemid";
    public static final String    PROPERTY_KEY_PASSWORD  = "c2entry_password";
    public static final String    PROPERTY_KEY_EMAIL     = "c2entry_email";

    private static C2EntryService sentryService;

    private static String         commonSystemId;
    private static String         commonPassword;
    private static String         commonEMail;

    // use property to test response of your system, with password and email
    // this confirms that nothing changed on the collective2 side of the
    // connection.
    // only run this test as a last diagnostic step! we do not want to load the
    // servers.

    // pull system properties that will be added to the IDE and never the
    // project

    @BeforeClass
    public static void initService() {
        BackEndAdapter backEnd = new Collective2Adapter();
        C2ServiceFactory factory = new C2ServiceFactory(backEnd);
        assertEquals(backEnd, factory.adapter());

        commonSystemId = System.getProperty(PROPERTY_KEY_SYSTEM_ID);
        commonPassword = System.getProperty(PROPERTY_KEY_PASSWORD);
        commonEMail = System.getProperty(PROPERTY_KEY_EMAIL);

        if (commonSystemId != null && commonPassword != null && commonEMail != null) {
            sentryService = factory.signalEntryService(commonPassword, Integer.parseInt(commonSystemId), commonEMail);
        } else {
            sentryService = null;
            logger.info("Live test skipped; " + (commonSystemId == null ? PROPERTY_KEY_SYSTEM_ID : " ") + " " + (commonPassword == null ? PROPERTY_KEY_PASSWORD : " ") + " " + (commonEMail == null ? PROPERTY_KEY_EMAIL : " ") + " system property(s) needs to be set.");
        }

    }

    @Test
    public void getAllSystemsTest() {
        if (sentryService != null) {
            assertTrue(sentryService.allSystems().getString(ElementStatus).toLowerCase().startsWith("ok"));

            boolean foundSystemId = false;

            XMLEventReader reader = sentryService.allSystems().getXMLEventReader();
            String lastItem = "";
            while (reader.hasNext()) {
                String item = reader.next().toString();
                if (lastItem.equals("<systemid>")) {
                    if (item.equals(commonSystemId)) {
                        foundSystemId = true;
                    }
                }
                lastItem = item;
            }
            assertTrue(foundSystemId);

        }
    }
    
    @Test
    public void getSystemHypotheticalTest() {
        logger.info("hypothetical test");
        if (sentryService != null) {
            
            Integer systemId = Integer.parseInt(commonSystemId);
            Response response = sentryService.systemHypothetical(systemId);
            XMLEventReader reader = response.getXMLEventReader();
            while (reader.hasNext()) {
                String peekLine = null;
                try {
                     peekLine = reader.peek().toString().trim();
                } catch (XMLStreamException e) {
                    logger.error("Unable to peek.", e);
                }

                String line = reader.next().toString().trim();
                assertEquals(peekLine,line);
            }
            
        }
    }
    
    @Test
    public void allSignalsTest() {
        if (sentryService!=null) {
            assertTrue(sentryService.allSignals().isOk());
        }
    }
    


}
