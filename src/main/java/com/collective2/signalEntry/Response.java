/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/7/12
 */
package com.collective2.signalEntry;

import com.collective2.signalEntry.adapter.IterableXMLEventReader;
import com.collective2.signalEntry.implementation.Command;

import java.math.BigDecimal;
import java.util.Collection;

public interface Response {
    
    String getString(C2Element element);

    Integer getInteger(C2Element element);

    Long getLong(C2Element element);

    BigDecimal getBigDecimal(C2Element element);

    Double getDouble(C2Element element);

    IterableXMLEventReader getXMLEventReader();

    Boolean isOk();

    String getXML();

    Command command();

    void visitC2Elements(C2ElementVisitor c2ElementVisitor, C2Element ... expected);

    <Integer> Collection<Integer> collectIntegers(Collection<Integer> target,C2Element element);

}
