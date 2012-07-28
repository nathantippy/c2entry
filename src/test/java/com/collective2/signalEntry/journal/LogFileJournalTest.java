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

import static org.junit.Assert.*;

public class LogFileJournalTest {
    //testing only the extra features of LogFile that are not covered by the basic test


    @Test
    public void persistenceTest() {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("unitTest", "log");
         } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        C2EntryServiceLogFileJournal journal = new C2EntryServiceLogFileJournal(tempFile,4096);
        //////////////////
        //starts out empty
        assertTrue(!journal.pending().hasNext());

        /////////////////////////
        //successful addition of one request
        Request request = new Request(Command.Signal);
        journal.append(request);

        //stop journal and force reload from disk
        journal.close();
        journal = new C2EntryServiceLogFileJournal(tempFile,4096);

        assertEquals(request, journal.pending().next());

        ///////////////////////////////
        //successful removal of one request
        journal.markSent(request);

        //stop journal and force reload from disk
        journal.close();
        journal = new C2EntryServiceLogFileJournal(tempFile,4096);

        assertTrue("check file:"+tempFile,!journal.pending().hasNext());

        //////////////////////////////////
        //successful addition of two requests in order
        Request request2 = new Request(Command.Cancel);
        assert(!request.equals(request2));
        journal.append(request);

        //stop journal and force reload from disk
        journal.close();
        journal = new C2EntryServiceLogFileJournal(tempFile,4096);

        journal.append(request2);

        //stop journal and force reload from disk
        journal.close();
        journal = new C2EntryServiceLogFileJournal(tempFile,4096);

        Iterator<Request> iterator = journal.pending();
        assertTrue(iterator.hasNext());
        assertEquals(request,iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals(request2,iterator.next());

        /////////////////////////
        //successful drop pending
        journal.dropPending();

        //stop journal and force reload from disk
        journal.close();
        journal = new C2EntryServiceLogFileJournal(tempFile,4096);

        assertTrue(!journal.pending().hasNext());

        //////////////////////////
        //successful interleave test
        journal.append(request);

        //stop journal and force reload from disk
        journal.close();
        journal = new C2EntryServiceLogFileJournal(tempFile,4096);

        journal.append(request2);

        //stop journal and force reload from disk
        journal.close();
        journal = new C2EntryServiceLogFileJournal(tempFile,4096);

        journal.markSent(request);

        //stop journal and force reload from disk
        journal.close();
        journal = new C2EntryServiceLogFileJournal(tempFile,4096);

        iterator = journal.pending();
        assertTrue(iterator.hasNext());
        assertEquals(request2,iterator.next());

        //////////////////////////////////
        //successful dropPending when we have already done so
        journal.dropPending();

        //stop journal and force reload from disk
        journal.close();
        journal = new C2EntryServiceLogFileJournal(tempFile,4096);

        journal.dropPending();//must not throw

        //////////////////////////
        //successful out of order failure
        try{
            journal.append(request);
            journal.append(request2);

            //stop journal and force reload from disk
            journal.close();
            journal = new C2EntryServiceLogFileJournal(tempFile,4096);

            journal.markSent(request2);//must not work
            fail();
        } catch (C2ServiceException e) {
            //success
            assertTrue(e.getMessage().startsWith("Expected"));
        }

        journal.dropPending();

    }

    @Test
    public void logRollingTest() {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("unitTest", "log");
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        C2EntryServiceLogFileJournal journal = new C2EntryServiceLogFileJournal(tempFile,50);

        Request request = new Request(Command.Signal);
        Request request2 = new Request(Command.Cancel);

        journal.append(request);
        journal.markSent(request);

        assertFalse(journal.pending().hasNext());

        assertFalse(journal.oldLogFiles().hasNext());
        journal.append(request2); //this will fire new log because old one is over 50 bytes and done
        assertTrue(journal.pending().hasNext());

        Iterator<File> iterator = journal.oldLogFiles();
        assertTrue(iterator.hasNext());
        assertTrue(iterator.next().exists());
        assertFalse(iterator.hasNext());

        journal.close();
        journal = new C2EntryServiceLogFileJournal(tempFile,50);

        iterator = journal.oldLogFiles();
        assertTrue(iterator.hasNext());
        assertTrue(iterator.next().exists());
        assertFalse(iterator.hasNext());


    }
}
