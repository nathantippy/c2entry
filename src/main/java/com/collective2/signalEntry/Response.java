/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/7/12
 */
package com.collective2.signalEntry;

import java.math.BigDecimal;

import javax.xml.stream.XMLEventReader;

public interface Response {
    
    String getString(C2Element element);

    Integer getInteger(C2Element element);

    Long getLong(C2Element element);

    BigDecimal getBigDecimal(C2Element element);

    Double getDouble(C2Element element);

    XMLEventReader getXMLEventReader();

    Boolean isOk();
    
    String getXML();
}
