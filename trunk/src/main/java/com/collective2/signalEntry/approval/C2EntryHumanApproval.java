/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  9/20/12
 */

package com.collective2.signalEntry.approval;

import com.collective2.signalEntry.implementation.Request;

import java.util.Iterator;

public interface C2EntryHumanApproval {
    C2EntryHumanApproval ApproveAll = new C2EntryHumanApproval() {

        public void waitForApproval(Iterator<Request> needsApproval) {
            while (needsApproval.hasNext()) {
                Request r = needsApproval.next();
                if (!r.isApprovalKnown()) {
                    r.setApproved(true);
                }
            }
        }

        public void oneMoreRequest(Request request) {
            request.setApproved(true);
        }
    };

    /**
     * Interact with the user to determine if these requests are approved for transmit.
     * The user is only required to approve/reject the first request on the iterator.
     *
     * @param needsApprovalList
     */
    void waitForApproval(Iterator<Request> needsApprovalList);

    /**
     * Ignore this call unless it occurs while the GUI is open for the waitForAproval method.
     * If that is the case this request should be added to the list of things to be approved.
     *
     * @param request
     */
    void oneMoreRequest(Request request);
}
