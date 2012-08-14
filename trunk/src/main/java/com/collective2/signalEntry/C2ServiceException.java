/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/7/12
 */
package com.collective2.signalEntry;

public class C2ServiceException extends RuntimeException {

    final boolean tryAgain;
    RuntimeException overideCause;

    public C2ServiceException(Throwable cause, boolean tryAgain) {
        super(cause);
        this.tryAgain = tryAgain;
    }

    public C2ServiceException(String message, Throwable cause, boolean tryAgain) {
        super(message, cause);
        this.tryAgain = tryAgain;
    }

    public C2ServiceException(String message, boolean tryAgain) {
        super(message);
        this.tryAgain = tryAgain;
    }

    public boolean tryAgain() {
        return tryAgain;
    }

    @Override
    public synchronized Throwable getCause() {
        if (overideCause != null) {
            return overideCause;
        }
        return super.getCause();
    }

    public void overrideCause(RuntimeException c2se) {
        overideCause = c2se;
    }
}
