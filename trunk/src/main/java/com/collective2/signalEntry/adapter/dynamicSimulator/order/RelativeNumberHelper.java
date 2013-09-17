
/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  8/26/12
 */
package com.collective2.signalEntry.adapter.dynamicSimulator.order;

import com.collective2.signalEntry.C2ServiceException;
import com.collective2.signalEntry.adapter.dynamicSimulator.DataProvider;
import com.collective2.signalEntry.adapter.dynamicSimulator.portfolio.Portfolio;
import com.collective2.signalEntry.adapter.dynamicSimulator.portfolio.Position;
import com.collective2.signalEntry.implementation.RelativeNumber;

import java.math.BigDecimal;

public class RelativeNumberHelper {

    public static BigDecimal toAbsolutePrice(String symbol, RelativeNumber relativeLimit, DataProvider currentPriceData, Portfolio portfolio, DataProvider dayOpenData) {

        switch (relativeLimit.prefix()) {

            case 'Q'://Q use most recent quote at session open which is dataProvider.openingPrice() - its the best we have
                return currentPriceData.openingPrice(symbol).add(relativeLimit.value());

            case 'O'://O use day open price, use open price if market was open else use end price..
                if (null == dayOpenData) {
                    throw new C2ServiceException("Relative price based on open price failure. No open price data.",false);
                }
                return dayOpenData.openingPrice(symbol).add(relativeLimit.value());

            case 'T'://T fill price of the open buy/sell  portfolio.position(symbol).openPrice();
                Position pos = portfolio.position(symbol);
                if (null == pos) {
                    throw new C2ServiceException("No position found for symbol "+symbol,false);
                }
                if (pos.quantity()==0) {
                    return BigDecimal.ZERO;
                }

                if (null == pos.openPrice()) {
                    throw new C2ServiceException("No open price found for position "+symbol,false);
                }
                return pos.openPrice().add(relativeLimit.value());

            default:
                return relativeLimit.value();
        }
    }

}
