
/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  8/26/12
 */
package com.collective2.signalEntry.adapter.dynamicSimulator.order;

import com.collective2.signalEntry.adapter.dynamicSimulator.DataProvider;
import com.collective2.signalEntry.adapter.dynamicSimulator.portfolio.Portfolio;
import com.collective2.signalEntry.implementation.RelativeNumber;

import java.math.BigDecimal;

public class RelativeNumberHelper {

    public static BigDecimal toAbsolutePrice(String symbol, RelativeNumber relativeLimit, DataProvider dataProvider, Portfolio portfolio) {

        //Q use most recent quote which is dataProvider.endingPrice() - its the best we have
        //O use day open price, use open price if market was open else use end price..
        //T fill price of the open buy/sell  portfolio.position(symbol).openPrice();

        switch (relativeLimit.prefix()) {
            case 'Q':
                return dataProvider.endingPrice(symbol).add(relativeLimit.value());
            case 'O':
                return dataProvider.openingPrice(symbol).add(relativeLimit.value());
            case 'T':
                return portfolio.position(symbol).openPrice().add(relativeLimit.value());
            default:
                return relativeLimit.value();
        }
    }

}
