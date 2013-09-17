/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/15/12
 */
package com.collective2.signalEntry;

import com.collective2.signalEntry.adapter.TestAdapter;
import org.junit.Before;
import org.junit.Test;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Logger;

import static com.collective2.signalEntry.C2Element.*;
import static com.collective2.signalEntry.Parameter.*;
import static org.junit.Assert.*;

/**
 * This notice shall not be removed. See the "LICENSE.txt" file found in the
 * root folder for the full license governing this code. Nathan Tippy 7/5/12
 */
public class StaticSimulationTest {

    private final static Integer          systemId      = 1234;
    private final static String           password      = "pa55word";
    private final static String           email         = "something@somewhere.com";
    private final static TestAdapter      backEnd       = new TestAdapter();
    private final static C2ServiceFactory factory       = new C2ServiceFactory(backEnd);
    private final static C2EntryService   sentryService = factory.signalEntryService(password, systemId, email);

    private final BigDecimal testNumber = new BigDecimal("12.34");
    private final BigDecimal testNegativeNumber = new BigDecimal("-43.21");
    private final Integer testInteger = 42;

    @Before
    public void dumpLog() {
        //remove loggers to speed up test
        //same events are captured by looking at the exceptions
        for(Handler h: Logger.getLogger("").getHandlers()) {
             Logger.getLogger("").removeHandler(h);
        }
    }

    @Test
    public void getFactoryTest() {
        assertEquals(factory,sentryService.serviceFactory());
    }

    @Test
    public void stockSignalTest() {
        String rootURL = "http://www.collective2.com/cgi-perl/signal.mpl?cmd=signal&systemid=1234&pw=pa55word&instrument=stock&symbol=MSFT";
        for (ActionForStock action : ActionForStock.values()) {
            String actionURL = rootURL + "&action=" + action.toString();

            for (Duration duration : com.collective2.signalEntry.Duration.values()) {
                String durationURL = actionURL + "&duration=" + duration.toString();
                Signal base = sentryService.stockSignal(action).symbol("MSFT").duration(duration);
                testAllOrderTypes(testNumber, testNegativeNumber, testInteger, durationURL, base);
            }

        }
    }

    @Test
    public void forexSignalTest() {
        String rootURL = "http://www.collective2.com/cgi-perl/signal.mpl?cmd=signal&systemid=1234&pw=pa55word&instrument=forex&symbol=MSFT";
        for (ActionForNonStock action : ActionForNonStock.values()) {
            String actionURL = rootURL + "&action=" + action.toString();

            for (Duration duration : com.collective2.signalEntry.Duration.values()) {
                String durationURL = actionURL + "&duration=" + duration.toString();
                Signal base = sentryService.forexSignal(action).symbol("MSFT").duration(duration);
                testAllOrderTypes(testNumber, testNegativeNumber, testInteger, durationURL, base);
            }

        }
    }

    @Test
    public void optionSignalTest() {
        String rootURL = "http://www.collective2.com/cgi-perl/signal.mpl?cmd=signal&systemid=1234&pw=pa55word&instrument=option&symbol=MSFT";
        for (ActionForNonStock action : ActionForNonStock.values()) {
            String actionURL = rootURL + "&action=" + action.toString();

            for (Duration duration : com.collective2.signalEntry.Duration.values()) {
                String durationURL = actionURL + "&duration=" + duration.toString();
                Signal base = sentryService.optionSignal(action).symbol("MSFT").duration(duration);
                testAllOrderTypes(testNumber, testNegativeNumber, testInteger, durationURL, base);
            }

        }
    }

    @Test
    public void futureSignalTest() {
        String rootURL = "http://www.collective2.com/cgi-perl/signal.mpl?cmd=signal&systemid=1234&pw=pa55word&instrument=future&symbol=MSFT";
        for (ActionForNonStock action : ActionForNonStock.values()) {
            String actionURL = rootURL + "&action=" + action.toString();

            for (Duration duration : com.collective2.signalEntry.Duration.values()) {
                String durationURL = actionURL + "&duration=" + duration.toString();
                Signal base = sentryService.futureSignal(action).symbol("MSFT").duration(duration);
                testAllOrderTypes(testNumber, testNegativeNumber, testInteger, durationURL, base);
            }

        }
    }


    private void testAllOrderTypes(BigDecimal testNumber, BigDecimal testNegativeNumber, Integer testInteger, String durationURL, Signal base) {
        for (Parameter reqOrder : EnumSet.of(MarketOrder, RelativeLimitOrder, RelativeStopOrder)) {
            String reqOrderURL = null;
            Signal baseOrder = null;
            switch (reqOrder) {
                case MarketOrder:
                    baseOrder = base.marketOrder();
                    reqOrderURL = durationURL;//url does not change
                    break;
                case RelativeLimitOrder:
                    baseOrder = base.limitOrder(BasePrice.RTQuotePlus, testNumber);
                    reqOrderURL = durationURL + "&limit=Q%2B" + testNumber;
                    break;
                case RelativeStopOrder:
                    baseOrder = base.stopOrder(BasePrice.SessionOpenPlus, testNumber);
                    reqOrderURL = durationURL + "&stop=O%2B" + testNumber;
                    break;
                default:
                    //nothing
            }

            // for each root
            try {
                baseOrder.send().getInteger(ElementSignalId);
                fail("Should have thrown because quantity was not set.");
            } catch (C2ServiceException c2ex) {
                // success
            }

            for (Parameter reqOne : EnumSet.of(Dollars, Quantity, AccountPercent)) {
                String allReqURL = null;
                Signal allReq = null;
                switch (reqOne) {
                    case Dollars:
                        allReq = baseOrder.dollars(testNumber);
                        allReqURL = reqOrderURL + "&dollars=" + testNumber;
                        break;
                    case Quantity:
                        allReq = baseOrder.quantity(testInteger);
                        allReqURL = reqOrderURL + "&quant=" + testInteger;
                        break;
                    case AccountPercent:
                        allReq = baseOrder.accountPercent(testNumber);
                        allReqURL = reqOrderURL + "&accountpercent=" + testNumber;
                        break;
                    default:
                        //nothing
                }
                try {
                    allReq.send().getInteger(ElementSignalId);// must not
                                                              // throw we
                                                              // now have
                                                              // all the
                                                              // valid
                                                              // values
                    assertEquals(allReqURL, backEnd.getLastURLString());

                } catch (C2ServiceException c2ex) {
                    c2ex.printStackTrace();
                    fail("Should not have thrown, all the required values have been set");
                }



                // now test each of the optional parameters
                testEachOptionalParameter(testNumber, testNegativeNumber, testInteger, allReqURL, allReq);

            }
        }
    }

    private void testEachOptionalParameter(BigDecimal testNumber, BigDecimal testNegativeNumber, Integer testInteger, String allReqURL, Signal allReq) {
        allReq.oneCancelsAnother(testInteger).send().getInteger(ElementSignalId);
        assertEquals(allReqURL + "&ocaid=" + testInteger, backEnd.getLastURLString());

        validateXML(allReq.oneCancelsAnother(testInteger).send().getXML());

        allReq.conditionalUpon(testInteger).send().getInteger(ElementSignalId);
        assertEquals(allReqURL + "&conditionalupon=" + testInteger, backEnd.getLastURLString());

        allReq.delay(testInteger).send().getInteger(ElementSignalId);
        assertEquals(allReqURL + "&delay=" + testInteger, backEnd.getLastURLString());

        allReq.xReplace(testInteger).send().getInteger(ElementSignalId);
        assertEquals(allReqURL + "&xreplace=" + testInteger, backEnd.getLastURLString());

        allReq.cancelsAt(testInteger).send().getInteger(ElementSignalId);
        assertEquals(allReqURL + "&cancelsat=" + testInteger, backEnd.getLastURLString());

        allReq.cancelsAtRelative(testInteger).send().getInteger(ElementSignalId);
        assertEquals(allReqURL + "&cancelsatrelative=" + testInteger, backEnd.getLastURLString());

        allReq.parkUntil(testInteger).send().getInteger(ElementSignalId);
        assertEquals(allReqURL + "&parkuntil=" + testInteger, backEnd.getLastURLString());

        allReq.parkUntil(2012, 5, 2, 0, 23, 2).send().getInteger(ElementSignalId);
        assertEquals(allReqURL + "&parkuntildatetime=20120502002302", backEnd.getLastURLString());

        allReq.stopLoss(testNumber).send().getInteger(ElementSignalId);
        assertEquals(allReqURL + "&stoploss=" + testNumber, backEnd.getLastURLString());

        allReq.stopLoss(BasePrice.SessionOpenPlus, testNumber).send().getInteger(ElementSignalId);
        assertEquals(allReqURL + "&stoploss=O%2B" + testNumber, backEnd.getLastURLString());

        allReq.stopLoss(BasePrice.RTQuotePlus, testNumber, true).send().getInteger(ElementSignalId); // ForceNoOCA
        assertEquals(allReqURL + "&stoploss=Q%2B" + testNumber + "&forcenooca=1", backEnd.getLastURLString());

        allReq.profitTarget(testNumber).send().getInteger(ElementSignalId);
        assertEquals(allReqURL + "&profittarget=" + testNumber, backEnd.getLastURLString());

        allReq.profitTarget(BasePrice.PositionOpenPlus, testNegativeNumber).send().getInteger(ElementSignalId);
        assertEquals(allReqURL + "&profittarget=T%2D" + Math.abs(testNegativeNumber.doubleValue()), backEnd.getLastURLString());

        allReq.profitTarget(BasePrice.SessionOpenPlus, testNegativeNumber, true).send().getInteger(ElementSignalId); // ForceNoOCA
        assertEquals(allReqURL + "&profittarget=O%2D" + Math.abs(testNegativeNumber.doubleValue()) + "&forcenooca=1", backEnd.getLastURLString());

        validateXML(allReq.profitTarget(BasePrice.SessionOpenPlus, testNegativeNumber, true).send().getXML());
    }

    @Test
    public void buyPowerTest() {
        /*
         * <collective2> <status>OK</status> <calctime>1136058468</calctime>
         * <buypower>68300.00</buypower> </collective2>
         */

        String status = sentryService.sendBuyPowerRequest().getString(ElementStatus);
        assertFalse("status was:" + status, status.isEmpty());

        assertTrue(sentryService.sendBuyPowerRequest().isOk());

        Long calctime = sentryService.sendBuyPowerRequest().getLong(ElementCalcTime);
        assertTrue(sentryService.sendBuyPowerRequest().getXML(), calctime > 0);

        Double doubleBuyPower = sentryService.sendBuyPowerRequest().getDouble(ElementBuyPower);
        assertFalse(sentryService.sendBuyPowerRequest().getXML(), Double.isNaN(doubleBuyPower));

        BigDecimal bigDecimalBuyPower = sentryService.sendBuyPowerRequest().getBigDecimal(ElementBuyPower);
        assertTrue(sentryService.sendBuyPowerRequest().getXML(), bigDecimalBuyPower.compareTo(BigDecimal.ZERO) != 0);

    }

    public void validateXML(String xml) {
        //test method walks all the returned xml.
        //the stax parser will end as soon as what was looked for is found
        //therefore without this method none of the xml blocks will
        //ever be read all the way to the end.
        try {
            List<StartElement> stack = new ArrayList<StartElement>();
            InputStream stream = new ByteArrayInputStream(xml.getBytes());
            XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(stream);
            while (reader.hasNext()) {
                XMLEvent event = reader.nextEvent();
                if (event.isStartElement()) {

                    StartElement startElement = event.asStartElement();
                    stack.add(startElement);

                }
                if (event.isEndElement()) {

                    StartElement top = stack.remove(stack.size()-1);
                    assertEquals(top.getName(),event.asEndElement().getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void allSignalsTest() {
        assertTrue(sentryService.sendAllSignalsRequest().isOk());

        int firstSignalId = sentryService.sendAllSignalsRequest().getInteger(C2Element.ElementSignalId);
        assertTrue(firstSignalId>0);

        validateXML(sentryService.sendAllSignalsRequest().getXML());

    }

    @Test
    public void allSystemsTest() {
        assertTrue(sentryService.sendAllSystemsRequest().isOk());
        validateXML(sentryService.sendAllSystemsRequest().getXML());
    }

    @Test
    public void positionStatusTest() {

        String symbol = "msft";
        assertTrue(sentryService.sendPositionStatusRequest(symbol).isOk());
        validateXML(sentryService.sendPositionStatusRequest(symbol).getXML());
    }

    @Test
    public void addToOCAGroupTest() {
        Integer OCAGroupId = 2;
        Integer signalId = 1;
        assertTrue(sentryService.addToOCAGroup(signalId, OCAGroupId));
        validateXML(sentryService.sendAddToOCAGroupRequest(signalId,OCAGroupId).getXML());
    }

    @Test
    public void signalStatusTest() {
        Integer signalId = 314159;
        assertEquals(signalId, sentryService.sendSignalStatusRequest(signalId, true, Related.Children).getInteger(ElementSignalId));
        assertEquals(signalId, sentryService.sendSignalStatusRequest(signalId, true, Related.Parent).getInteger(ElementSignalId));
        assertEquals(signalId, sentryService.sendSignalStatusRequest(signalId, false, Related.Children).getInteger(ElementSignalId));
        assertEquals(signalId, sentryService.sendSignalStatusRequest(signalId, false, Related.Parent).getInteger(ElementSignalId));
        
        assertEquals(signalId, sentryService.sendSignalStatusRequest(signalId, true).getInteger(ElementSignalId));
        assertEquals(signalId, sentryService.sendSignalStatusRequest(signalId, false).getInteger(ElementSignalId));

        validateXML(sentryService.sendSignalStatusRequest(signalId, false).getXML());
    }

    @Test
    public void setMinBuyPowerTest() {

        Number power = new BigDecimal(1234.56d);
        assertTrue(sentryService.setMinBuyPower(power));
    }

    @Test
    public void requestOCAIdTest() {
        Integer id = sentryService.oneCancelsAnotherId();
        assertTrue(id > 0);
        validateXML(sentryService.sendOneCancelsAnotherIdRequest().getXML());
    }

    @Test
    public void cancelTest() {
        Integer signalId = 1;
        assertTrue(sentryService.cancel(signalId));
        validateXML(sentryService.sendCancelRequest(signalId).getXML());
    }

    @Test
    public void cancelAllPendingTest() {
        assertTrue(sentryService.cancelAllPending());
        validateXML(sentryService.sendCancelAllPendingRequest().getXML());
    }

    @Test
    public void closeAllPositionsTest() {
        Integer signalId = 1;
        assertTrue(sentryService.closeAllPositions(signalId));
        validateXML(sentryService.sendCloseAllPositionsRequest(signalId).getXML());
    }

    @Test
    public void flushPendingSignalsTest() {
        assertTrue(sentryService.flushPendingSignals());
        validateXML(sentryService.sendFlushPendingSignalsRequest().getXML());
    }

    @Test
    public void newCommentTest() {
        String comment = "hello";
        Integer signalId = 1;
        assertTrue(sentryService.sendNewCommentRequest(comment,signalId).isOk());
        assertTrue(sentryService.sendNewCommentRequest(comment,signalId).getInteger(C2Element.ElementSignalId)>0);
        validateXML(sentryService.sendNewCommentRequest(comment,signalId).getXML());

    }

    @Test
    public void systemEquityTest() {

        assertTrue(sentryService.sendSystemEquityRequest().isOk());
        assertTrue(sentryService.sendSystemEquityRequest().getLong(C2Element.ElementCalcTime)>0);
        assertFalse(Double.isNaN(sentryService.sendSystemEquityRequest().getDouble(C2Element.ElementSystemEquity)));
        validateXML(sentryService.sendSystemEquityRequest().getXML());
    }



    @Test
    public void hypoTest() {
        
        assertTrue(sentryService.sendSystemHypotheticalRequest(1,2,3,4).getXML(),sentryService.sendSystemHypotheticalRequest(1,2,3,4).getInteger(C2Element.ElementSystemId)>0);
        assertTrue(sentryService.sendSystemHypotheticalRequest(1,2,3,4).getDouble(C2Element.ElementEquity)>0);
        validateXML(sentryService.sendSystemHypotheticalRequest(1,2,3,4).getXML());
    }

    @Test
    public void reverseTest() {
        
        Reverse reverse = sentryService.reversal("msft");

        assertEquals("Command:http://www.collective2.com/cgi-perl/signal.mpl?cmd=reverse&systemid=1234&pw=PASSWORD&symbol=msft",reverse.toString());
        Response response = reverse.triggerPrice(new BigDecimal("12.23")).duration(Duration.DayOrder).quantity(10).send();
        assertTrue(response.isOk());
        validateXML(reverse.triggerPrice(new BigDecimal("12.23")).duration(Duration.DayOrder).quantity(10).send().getXML());
    }

    @Test
    public void sendSubscriberBroadcastTest() {

        String message = "hello world";
        assertTrue(sentryService.sendSubscriberBroadcastRequest(message).isOk());
        validateXML(sentryService.sendSubscriberBroadcastRequest(message).getXML());


    }


}
