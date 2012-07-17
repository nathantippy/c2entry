/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/6/12
 */
package com.collective2.signalEntry;

public enum C2Element {

    ElementSignalId("signalid"),
    ElementSystemId("systemid"),
    ElementComments("comments"),
    ElementPreviousComment("previousComment"),
    ElementOCAId("ocaid"),
    ElementStatus("status"),
    ElementAck("ack"),
    ElementSystemName("systemname"),
    ElementPostedWhen("postedwhen"),
    ElementEMailedWhen("emailedwhen"),
    ElementKilledWhen("killedwhen"),
    ElementTradedWhen("tradedwhen"),
    ElementTradePrice("tradeprice"),
    ElementErrorType("errortype"),
    ElementTotalEquityAvail("totalequityavail"),
    ElementCash("cash"),
    ElementEquity("equity"),
    ElementMarginUsed("marginused"),
    ElementSystemEquity("systemequity"),
    ElementComment("comment"),
    ElementSymbol("symbol"),
    ElementDetails("details"),
    ElementStopLossSignalId("stoplosssignalid"),
    ElementCalcTime("calctime"),
    ElementBuyPower("buypower"),
    ElementPosition("position");

    final private String localElementName;

    C2Element(String localElementName) {
        this.localElementName = localElementName;
    }

    public String localElementName() {
        return localElementName;
    }
}
