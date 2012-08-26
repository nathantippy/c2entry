/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/6/12
 */
package com.collective2.signalEntry;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;

public enum C2Element {

    //Must be in alpha order by localElementName so
    //binary search can be used for faster lookup

    ElementAck("ack"),
    ElementAction("action"),
    ElementBuyPower("buypower"),
    ElementCalcTime("calctime"),
    ElementCash("cash"),
    ElementComment("comment"),
    ElementComments("comments"),
    ElementDetails("details"),
    ElementEMailedWhen("emailedwhen"),
    ElementEquity("equity"),
    ElementErrorType("errortype"),
    ElementKilledWhen("killedwhen"),
    ElementLimit("limit"),
    ElementMarginUsed("marginused"),
    ElementMarket("market"),
    ElementOCAGroupIdDetail("ocagroupid"),
    ElementOCAId("ocaid"),
    ElementPosition("position"),
    ElementPostedWhen("postedwhen"),
    ElementPreviousComment("previousComment"),
    ElementProfitTaretSignalId("profittargetsignalid"),
    ElementQuant("quant"),
    ElementSignalId("signalid"),
    ElementStatus("status"),
    ElementStop("stop"),
    ElementStopLossSignalId("stoplosssignalid"),
    ElementSymbol("symbol"),
    ElementSystemEquity("systemequity"),
    ElementSystemId("systemid"),
    ElementSystemName("systemname"),
    ElementTimeInForce("tif"),
    ElementTotalEquityAvail("totalequityavail"),
    ElementTradedWhen("tradedwhen"),
    ElementTradePrice("tradeprice");

    final private String localElementName;

    C2Element(String localElementName) {
        this.localElementName = localElementName;
    }

    public String localElementName() {
        return localElementName;
    }

    public String toString() {
        return localElementName;
    }

    public static C2Element binaryLookup(String localName) {
        assert(inOrder(values()));

        int index = Arrays.binarySearch(values(), localName, new Comparator<Serializable>() {
            @Override
            public int compare(Serializable o1, Serializable o2) {
                String s1 = o1.toString();
                String s2 = o2.toString();
                return s1.compareTo(s2);
            }
        });
        if (index<0) {
            return null;
        } else {
            return values()[index];
        }
    }

    private static boolean inOrder(C2Element[] values) {
        String last = "";
        for(C2Element c2e:values) {
            if (last.compareTo(c2e.localElementName())>=0) {
                return false;
            } else {
                last = c2e.localElementName();
            }
        }
        return true;
    }
}
