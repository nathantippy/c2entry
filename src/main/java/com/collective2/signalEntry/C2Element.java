/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/6/12
 */
package com.collective2.signalEntry;


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
    ElementTradePrice("tradeprice"),
    None("N/A");

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

}
