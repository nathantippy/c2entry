/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/7/12
 */
package com.collective2.signalEntry;

public class C2ServiceException extends RuntimeException {

    public C2ServiceException(Throwable cause) {
        super(cause);
    }

    public C2ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public C2ServiceException(String message) {
        super(message);
    }
}
