/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  9/20/12
 */
package com.collective2.signalEntry.approval;

import com.collective2.signalEntry.implementation.Request;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class ApprovalRequestableConsole implements C2EntryHumanApproval {

    private List<Request> needsApprovalList = new ArrayList<Request>();

    @Override
    public void waitForApproval(Iterator<Request> needsApproval) {
        int size;
        synchronized (needsApprovalList) {
            needsApprovalList.clear();
            while (needsApproval.hasNext()) {
                needsApprovalList.add(needsApproval.next());
            }
            size = needsApprovalList.size();
        }

        int i = 0;
        while (i<size) { //note: by design may grow while its walked
            Request request;
            synchronized (needsApprovalList) {
                request = needsApprovalList.get(i);
            }
            //only ask for approval once even if items are still on the list
            if (!request.isApprovalKnown()) {
                System.out.println("");
                System.out.println(request.buildURL());
                System.out.print("approve (Y/N):");

                char c = System.console().readLine().trim().charAt(0);
                request.setApproved('Y'==c || 'y'==c);
                System.out.println(c);
            }
            i++;
            synchronized (needsApprovalList) {
                size = needsApprovalList.size();
            }

        }
        synchronized(needsApprovalList) {
            needsApprovalList.clear();
        }
    }

    @Override
    public void oneMoreRequest(Request request) {
        synchronized(needsApprovalList) {
            if (!needsApprovalList.isEmpty()) {
                needsApprovalList.add(request);
            }
        }
    }
}
