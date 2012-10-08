/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  7/27/12
 */
package com.collective2.signalEntry.journal;

import com.collective2.signalEntry.C2ServiceException;
import com.collective2.signalEntry.Parameter;
import com.collective2.signalEntry.implementation.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;

public class C2EntryServiceLogFileJournal implements C2EntryServiceJournal {

    private final static byte STATE_PENDING   = 'P'; //NEW REQUESTS
    private final static byte STATE_SENT      = 'S'; //SENT TO C2
    private final static byte STATE_DROPPED   = 'D'; //BULK DROPPED
    private final static byte STATE_REJECTED  = 'R'; //HUMAN REJECTED

    private final ByteBuffer BYTE_BUFFER_SENT;
    private final ByteBuffer BYTE_BUFFER_DROPPED;
    private final ByteBuffer BYTE_BUFFER_PENDING_PADDED;
    private final ByteBuffer BYTE_BUFFER_REJECTED;
    private final ByteBuffer BYTE_BUFFER_BLANK_DATETIME_PADDED;
    private final ByteBuffer BYTE_BUFFER_NEWLINE;

    private final int DONE_DATETIME_OFFSET = 22;
    private final int URL_OFFSET = 42;

    private final int ROLL_THRESHOLD;

    private final static Logger logger = LoggerFactory.getLogger(C2EntryServiceLogFileJournal.class);

    private File                    logFile;
    private FileChannel             randomChannel;
    private final List < Request >  requests = new ArrayList<Request>();
    private final List < Long >     positions = new ArrayList<Long>();
    private Long                    lastPosition;
    private final List<File>        oldFiles = new ArrayList<File>();

    /*

    File format00000000000000000000000000001111111111222222222222222222222222223333333333444444444444444
    0             1                2345678901234567890  1              2345678901234567890   1         2
    state(1char)(one space)created(yyyy-MM-DD hh:mm:ss)(one space)done(yyyy-MM-DD hh:mm:ss)(one space)url

    P - pending
    S - sent
    D - dropped
    R - rejected

    */

    public C2EntryServiceLogFileJournal(File file, int rollThreshold) {

        ROLL_THRESHOLD = rollThreshold;

        BYTE_BUFFER_SENT = ByteBuffer.allocate(1);
        BYTE_BUFFER_SENT.put(STATE_SENT);
        BYTE_BUFFER_SENT.rewind();

        BYTE_BUFFER_DROPPED = ByteBuffer.allocate(1);
        BYTE_BUFFER_DROPPED.put(STATE_DROPPED);
        BYTE_BUFFER_DROPPED.rewind();

        BYTE_BUFFER_REJECTED = ByteBuffer.allocate(1);
        BYTE_BUFFER_REJECTED.put(STATE_REJECTED);
        BYTE_BUFFER_REJECTED.rewind();


        //has one space of padding on the right
        BYTE_BUFFER_PENDING_PADDED = ByteBuffer.allocate(2);
        BYTE_BUFFER_PENDING_PADDED.put(STATE_PENDING);
        BYTE_BUFFER_PENDING_PADDED.put((byte)' ');
        BYTE_BUFFER_PENDING_PADDED.rewind();

        //has two spaces of padding one on each end
        int i = 21;
        BYTE_BUFFER_BLANK_DATETIME_PADDED = ByteBuffer.allocate(i);
        while (--i>=0) {
            BYTE_BUFFER_BLANK_DATETIME_PADDED.put((byte)' ');
        }
        BYTE_BUFFER_BLANK_DATETIME_PADDED.rewind();

        BYTE_BUFFER_NEWLINE = ByteBuffer.allocate(1);
        BYTE_BUFFER_NEWLINE.put((byte)'\n');
        BYTE_BUFFER_NEWLINE.rewind();

        try {
            logFile = file;
            //ensure we have random access and all writes are synchronous
            RandomAccessFile ras = new RandomAccessFile(file,"rws");

            randomChannel = ras.getChannel();

            //keep this file open until the jvm shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                     close();
                }
            }));

            ByteBuffer buf = ByteBuffer.allocate(4096);

            boolean loadPending = false;
            StringBuilder builder = new StringBuilder();

            int col = 0;
            int length;
            long bytePos = 0;
            try {
                while ((length=randomChannel.read(buf))>0) {
                    buf.flip();

                    while (buf.hasRemaining()){
                        byte value = buf.get();
                        if ('\r' != value) {
                            if (col == 0 && STATE_PENDING == value) {
                                //we have found a pending row
                                loadPending = true;

                            }
                            if (loadPending) {
                                if (col==0) {
                                    //remember position
                                    positions.add(lastPosition = bytePos);
                                }

                                if (col >= URL_OFFSET && '\n' != value) {
                                    builder.append((char)value);
                                }
                            }
                            col = ('\n' == value ? 0 : col+1);
                            if (0 == col && loadPending) {
                                //save last row we are on to the next one
                                Request request = Request.parseURL(builder.toString());
                                builder.setLength(0);

                                //when loading pending requests if any are time dependent must throw failure
                                //can not be done upon write because this is used as a journal and if its never used
                                //as a source of recovery then there is no failure.
                                if (null != request.get(Parameter.CancelsAtRelative)) {
                                    //cant try again or delay because the request is time dependent relative to submission time
                                    throw new C2ServiceException("Can not retry request which makes use of CancelsAtRelative feature", false);
                                }

                                requests.add(request);
                            }
                        }
                        bytePos++;
                    }
                    buf.clear();
                }
            } catch (IOException e) {
                throwError("Unable to initialize journal.", e, false);
            }

            final String localName = file.getName();
            File directory = file.getParentFile();
            if (directory==null) {
                throw new C2ServiceException("Log file must not been in the root of file system.",false);
            }
            File[] oldLogs = directory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith(localName+".");
                }
            });
            int j = oldLogs.length;
            while (--j>=0) {
                oldFiles.add(oldLogs[j]);
            }
            Collections.sort(oldFiles);

        } catch (FileNotFoundException e) {
            throwError("Unable to initialize journal.", e, false);
        }
    }

    @Override
    public Iterator<Request> pending() {
        return requests.iterator();
    }

    @Override
    public void append(Request request) {

        Long position=null;
        try {
            assert (randomChannel.position()==randomChannel.size()) :  randomChannel.position()+"!="+randomChannel.size();

            //determine if we need to roll the log file.
            //Only done when:
            //1. there are no pending requests
            //2. the log has crossed the threshold size
            if (requests.isEmpty() && randomChannel.size()>ROLL_THRESHOLD) {
                //before writing this new request roll the log

                ByteBuffer lastDate = ByteBuffer.allocate(19);
                randomChannel.read(lastDate, lastPosition+DONE_DATETIME_OFFSET);
                byte[] dateBytes = lastDate.array();
                dateBytes[10]='_';
                dateBytes[13]=dateBytes[16]='-';

                randomChannel.close();

                File oldFile = new File(logFile.getAbsolutePath() + '.' + new String(dateBytes));
                logFile.renameTo(oldFile);
                oldFiles.add(oldFile);

                RandomAccessFile ras = new RandomAccessFile(logFile,"rws");
                randomChannel = ras.getChannel();

            }

            //save for later
            position = randomChannel.position();
            ByteBuffer[] scatter = new ByteBuffer[] {BYTE_BUFFER_PENDING_PADDED,
                                                     isoDateTime(Calendar.getInstance()),
                                                     BYTE_BUFFER_BLANK_DATETIME_PADDED,
                                                     ByteBuffer.wrap(request.buildURL().toString().getBytes()),
                                                     BYTE_BUFFER_NEWLINE
                                                    };
            randomChannel.write(scatter);

            BYTE_BUFFER_PENDING_PADDED.rewind();
            BYTE_BUFFER_BLANK_DATETIME_PADDED.rewind();
            BYTE_BUFFER_NEWLINE.rewind();

        } catch (IOException e) {
            throwError("Unable to append new request.",e,true);//try again may be out of drive space or unable to get lock etc.
        }
        ///only set if we did not throw
        requests.add(request);
        positions.add(lastPosition = position);
    }

    @Override
    public void markRejected(Request request) {
        mark(request,BYTE_BUFFER_REJECTED);
    }

    @Override
    public void markSent(Request request) {
        mark(request,BYTE_BUFFER_SENT);
    }

    /**
     * Set the mark byte on this row and set the time
     * @param request
     * @param mark
     */
    private void mark(Request request, ByteBuffer mark) {

        //remove from in memory list
        Request oldPending = requests.remove(0);
        if (!request.equals(oldPending)) {
            throw new C2ServiceException("Expected to finish "+oldPending+" but instead was sent "+request,false);
        }
        long pendingPosition = positions.remove(0);
        try {
            //mark oldest pending as sent with a timestamp of now
            randomChannel.write(mark, pendingPosition);
            mark.rewind();
            randomChannel.write(isoDateTime(Calendar.getInstance()), pendingPosition+ DONE_DATETIME_OFFSET);

        } catch (IOException e) {
            throwError("Unable to mark request as sent must halt to prevent duplicate transmissions",e,false);
        }

    }

    private void throwError(String message, IOException e, boolean tryAgain) {
        logger.error(message, e);
        throw new C2ServiceException(message, e, tryAgain);
    }

    private ByteBuffer isoDateTime(Calendar calendar) {
        ByteBuffer isoDateTime = ByteBuffer.allocate(19); //yyyy-MM-DD hh:mm:ss

        int year = calendar.get(Calendar.YEAR);
        isoDateTime.put((byte)('0'+(year/1000)));
        year = year%1000;
        isoDateTime.put((byte)('0'+(year/100)));
        year = year%100;
        isoDateTime.put((byte)('0'+(year/10)));
        year = year%10;
        isoDateTime.put((byte)('0'+(year)));

        isoDateTime.put((byte)'-');

        int month = calendar.get(Calendar.MONTH);
        isoDateTime.put((byte)('0'+(month/10)));
        month = month%10;
        isoDateTime.put((byte)('0'+(month)));

        isoDateTime.put((byte)'-');

        int day = calendar.get(Calendar.DAY_OF_MONTH);
        isoDateTime.put((byte)('0'+(day/10)));
        day = day%10;
        isoDateTime.put((byte)('0'+(day)));

        isoDateTime.put((byte)' ');

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        isoDateTime.put((byte)('0'+(hour/10)));
        hour = hour%10;
        isoDateTime.put((byte)('0'+(hour)));

        isoDateTime.put((byte)':');

        int minute = calendar.get(Calendar.MINUTE);
        isoDateTime.put((byte)('0'+(minute/10)));
        minute = minute%10;
        isoDateTime.put((byte)('0'+(minute)));

        isoDateTime.put((byte)':');

        int second = calendar.get(Calendar.SECOND);
        isoDateTime.put((byte)('0'+(second/10)));
        second = second%10;
        isoDateTime.put((byte)('0'+(second)));

        isoDateTime.rewind();
        return isoDateTime;
    }

    @Override
    public Request[] dropPending() {

        Request[] dropped = new Request[requests.size()];
        int d = 0;

        //write update for each of these
        while (!requests.isEmpty()) {
            dropped[d++] = requests.remove(0).secureClone();
            long pendingPosition = positions.remove(0);
            try {
                randomChannel.write(BYTE_BUFFER_DROPPED, pendingPosition);
                BYTE_BUFFER_DROPPED.rewind();
                randomChannel.write(isoDateTime(Calendar.getInstance()), pendingPosition+ DONE_DATETIME_OFFSET);
            } catch (IOException e) {
                throwError("Unable to drop all pending requests.",e,false);
            }
        }

        return dropped;
    }

    void close() {
        try {
            if (randomChannel.isOpen()) {
                randomChannel.close();
            }
        } catch (IOException e) {
            logger.warn("Unable to close random access file:"+logFile, e);
        }
    }

    Iterator<File> oldLogFiles() {
        return oldFiles.iterator();
    }
}
