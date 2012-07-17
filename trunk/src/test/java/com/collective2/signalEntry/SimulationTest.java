/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/15/12
 */
package com.collective2.signalEntry;

import static com.collective2.signalEntry.C2Element.ElementBuyPower;
import static com.collective2.signalEntry.C2Element.ElementCalcTime;
import static com.collective2.signalEntry.C2Element.ElementSignalId;
import static com.collective2.signalEntry.C2Element.ElementStatus;
import static com.collective2.signalEntry.Parameter.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.logging.Handler;
import java.util.logging.Level;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.collective2.signalEntry.implementation.Command;
import com.collective2.signalEntry.transmission.TestAdapter;

/**
 * This notice shall not be removed. See the "LICENSE.txt" file found in the
 * root folder for the full license governing this code. Nathan Tippy 7/5/12
 */
public class SimulationTest {

    private final static Integer          systemId      = 1234;
    private final static String           password      = "pa55word";
    private final static String           email         = "something@somewhere.com";
    private final static TestAdapter      backEnd       = new TestAdapter();
    private final static C2ServiceFactory factory       = new C2ServiceFactory(backEnd);
    private final static C2EntryService   sentryService = factory.signalEntryService(password, systemId, email);

    private final Number testNumber = 12.34;
    private final Number testNegativeNumber = -43.21;
    private final Integer testInteger = 42;


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


    private void testAllOrderTypes(Number testNumber, Number testNegativeNumber, Integer testInteger, String durationURL, Signal base) {
        for (Parameter reqOrder : EnumSet.of(MarketOrder, LimitOrder, RelativeLimitOrder, StopOrder, RelativeStopOrder)) {
            String reqOrderURL = null;
            Signal baseOrder = null;
            switch (reqOrder) {
                case MarketOrder:
                    baseOrder = base.marketOrder();
                    reqOrderURL = durationURL;//url does not change
                    break;
                case LimitOrder:
                    baseOrder = base.limitOrder(testNumber);
                    reqOrderURL = durationURL + "&limit=" + testNumber;
                    break;
                case RelativeLimitOrder:
                    baseOrder = base.limitOrder(BasePrice.QuoteNow, testNumber);
                    reqOrderURL = durationURL + "&limit=Q%2B" + testNumber;
                    break;
                case StopOrder:
                    baseOrder = base.stopOrder(testNumber);
                    reqOrderURL = durationURL + "&stop=" + testNumber;
                    break;
                case RelativeStopOrder:
                    baseOrder = base.stopOrder(BasePrice.Opening, testNumber);
                    reqOrderURL = durationURL + "&stop=O%2B" + testNumber;
                    break;
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

    private void testEachOptionalParameter(Number testNumber, Number testNegativeNumber, Integer testInteger, String allReqURL, Signal allReq) {
        allReq.oneCancelsAnother(testInteger).send().getInteger(ElementSignalId);
        assertEquals(allReqURL + "&ocaid=" + testInteger, backEnd.getLastURLString());

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

        allReq.stopLoss(BasePrice.Opening, testNumber).send().getInteger(ElementSignalId);
        assertEquals(allReqURL + "&stoploss=O%2B" + testNumber, backEnd.getLastURLString());

        allReq.stopLoss(BasePrice.QuoteNow, testNumber, true).send().getInteger(ElementSignalId); // ForceNoOCA
        assertEquals(allReqURL + "&stoploss=Q%2B" + testNumber + "&forcenooca=1", backEnd.getLastURLString());

        allReq.profitTarget(testNumber).send().getInteger(ElementSignalId);
        assertEquals(allReqURL + "&profittarget=" + testNumber, backEnd.getLastURLString());

        allReq.profitTarget(BasePrice.TradeFill, testNegativeNumber).send().getInteger(ElementSignalId);
        assertEquals(allReqURL + "&profittarget=T%2D" + Math.abs(testNegativeNumber.doubleValue()), backEnd.getLastURLString());

        allReq.profitTarget(BasePrice.Opening, testNegativeNumber, true).send().getInteger(ElementSignalId); // ForceNoOCA
        assertEquals(allReqURL + "&profittarget=O%2D" + Math.abs(testNegativeNumber.doubleValue()) + "&forcenooca=1", backEnd.getLastURLString());
    }

    @Test
    public void buyPowerTest() {
        /*
         * <collective2> <status>OK</status> <calctime>1136058468</calctime>
         * <buypower>68300.00</buypower> </collective2>
         */

        String status = sentryService.buyPower().getString(ElementStatus);
        assertFalse("status was:" + status, status.isEmpty());

        assertTrue(sentryService.buyPower().isOk());

        Long calctime = sentryService.buyPower().getLong(ElementCalcTime);
        assertTrue(sentryService.buyPower().getXML(), calctime > 0);

        Double doubleBuyPower = sentryService.buyPower().getDouble(ElementBuyPower);
        assertFalse(sentryService.buyPower().getXML(), Double.isNaN(doubleBuyPower));

        BigDecimal bigDecimalBuyPower = sentryService.buyPower().getBigDecimal(ElementBuyPower);
        assertTrue(sentryService.buyPower().getXML(), bigDecimalBuyPower.compareTo(BigDecimal.ZERO) != 0);

    }



    @Test
    public void allSignalsTest() {
        assertTrue(sentryService.allSignals().isOk());

    }

    @Test
    public void allSystemsTest() {
        assertTrue(sentryService.allSystems().isOk());
    }

    @Test
    public void positionStatusTest() {

        String symbol = "msft";
        assertTrue(sentryService.positionStatus(symbol).isOk());
    }

    @Test
    public void addToOCAGroupTest() {
        Integer OCAGroupId = 2;
        Integer signalId = 1;
        assertTrue(sentryService.addToOCAGroup(signalId, OCAGroupId));
    }

    @Test
    public void signalStatusTest() {
        Integer signalId = 314159;
        assertEquals(signalId, sentryService.signalStatus(signalId, true, Related.Children).getInteger(ElementSignalId));
        assertEquals(signalId, sentryService.signalStatus(signalId, true, Related.Parent).getInteger(ElementSignalId));
        assertEquals(signalId, sentryService.signalStatus(signalId, false, Related.Children).getInteger(ElementSignalId));
        assertEquals(signalId, sentryService.signalStatus(signalId, false, Related.Parent).getInteger(ElementSignalId));
        
        assertEquals(signalId, sentryService.signalStatus(signalId, true).getInteger(ElementSignalId));
        assertEquals(signalId, sentryService.signalStatus(signalId, false).getInteger(ElementSignalId));
    }

    @Test
    public void setMinBuyPowerTest() {

        Number power = 1234.56d;
        assertTrue(sentryService.setMinBuyPower(power));
    }

    @Test
    public void requestOCAIdTest() {
        Integer id = sentryService.requestOneCancelsAnotherId();
        assertTrue(id > 0);
    }

    @Test
    public void cancelTest() {
        Integer signalId = 1;
        assertTrue(sentryService.cancel(signalId));
    }

    @Test
    public void cancelAllPendingTest() {
        assertTrue(sentryService.cancelAllPending());
    }

    @Test
    public void closeAllPositionsTest() {
        Integer signalId = 1;
        assertTrue(sentryService.closeAllPositions(signalId));
    }

    @Test
    public void flushPendingSignalsTest() {
        assertTrue(sentryService.flushPendingSignals());
    }

    @Test
    public void newCommentTest() {
        String comment = "hello";
        Integer signalId = 1;
        assertTrue(sentryService.newComment(comment,signalId).isOk());
        assertTrue(sentryService.newComment(comment,signalId).getInteger(C2Element.ElementSignalId)>0);
    }

    @Test
    public void systemEquityTest() {

        assertTrue(sentryService.systemEquity().isOk());
        assertTrue(sentryService.systemEquity().getLong(C2Element.ElementCalcTime)>0);
        assertFalse(Double.isNaN(sentryService.systemEquity().getDouble(C2Element.ElementSystemEquity)));
    }



    @Test
    public void hypoTest() {
        
        assertTrue(sentryService.systemHypothetical(1,2,3,4).getXML(),sentryService.systemHypothetical(1,2,3,4).getInteger(C2Element.ElementSystemId)>0);
        assertTrue(sentryService.systemHypothetical(1,2,3,4).getDouble(C2Element.ElementEquity)>0);
        
    }

    @Test
    public void reverseTest() {
        
        Reverse reverse = sentryService.reversal("msft");

        assertEquals("Command:http://www.collective2.com/cgi-perl/signal.mpl?cmd=reverse&systemid=1234&pw=PASSWORD&symbol=msft",reverse.toString());
        Response response = reverse.triggerPrice(12.23d).duration(Duration.DayOrder).send();
        assertTrue(response.isOk());

    }

    @Test
    public void sendSubscriberBroadcastTest() {

        String message = "hello world";
        assertTrue(sentryService.sendSubscriberBroadcast(message));

    }


}
