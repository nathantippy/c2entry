/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  7/28/12
 */

package com.collective2.signalEntry.journal;


import com.collective2.signalEntry.C2ServiceException;
import com.collective2.signalEntry.implementation.Command;
import com.collective2.signalEntry.implementation.Request;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class BasicJournalTest {

    //all C2EntryServiceJournal implementations must pass these minimal tests

    @Test
    public void memoryJournalTest() {
        C2EntryServiceJournal journal = new C2EntryServiceMemoryJournal();
        journalTest(journal);
    }

    @Test
    public void logFileJournalTest() {
        C2EntryServiceJournal journal = null;
        try {
            journal = new C2EntryServiceLogFileJournal(File.createTempFile("unitTest", "log"),4096);
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        journalTest(journal);
    }

    private void journalTest(C2EntryServiceJournal journal) {
        //starts out empty
        assertTrue(!journal.pending().hasNext());

        //successful addition of one request
        Request request = new Request(Command.Signal);
        journal.append(request);
        assertEquals(request,journal.pending().next());

        //successful removal of one request
        journal.markSent(request);
        assertTrue(!journal.pending().hasNext());

        //successful addition of two requests in order
        Request request2 = new Request(Command.Cancel);
        assert(!request.equals(request2));
        journal.append(request);
        journal.append(request2);
        Iterator<Request> iterator = journal.pending();
        assertTrue(iterator.hasNext());
        assertEquals(request,iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals(request2,iterator.next());

        //successful drop pending
        journal.dropPending();
        assertTrue(!journal.pending().hasNext());

        //successful interleave test
        journal.append(request);
        journal.append(request2);
        journal.markSent(request);
        iterator = journal.pending();
        assertTrue(iterator.hasNext());
        assertEquals(request2,iterator.next());

        //successful dropPending when we have already done so
        journal.dropPending();
        journal.dropPending();//must not throw

        //successful out of order failure
        try{
            journal.append(request);
            journal.append(request2);
            journal.markSent(request2);//must not work
            fail();
        } catch (C2ServiceException e) {
            //success
            assertTrue(e.getMessage().startsWith("Expected"));
        }


    }


}
