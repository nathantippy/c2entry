/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  8/5/12
 */

package com.collective2.signalEntry;

import java.util.Deque;

public interface C2ElementVisitor {
    public void visit(C2Element element, String data, Deque<String> stack);
}
