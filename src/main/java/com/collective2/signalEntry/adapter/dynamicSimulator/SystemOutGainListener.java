/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  8/3/12
 */

package com.collective2.signalEntry.adapter.dynamicSimulator;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;

public class SystemOutGainListener implements GainListener {

    @Override
    public void gainData(long now, List<String> systemNameList,
                         List<BigDecimal> beginEquityList, List<Double> fullCAGRList,
                         List<BigDecimal> lastEquityList,  List<Double> lastCAGRList,
                         List<BigDecimal> currentEquityList) {

        StringBuffer row = new StringBuffer();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(now);
        isoDateTime(calendar, row);


        for(int i = 0;i<systemNameList.size();i++) {
            writeSystem(systemNameList.get(i),fullCAGRList.get(i),lastCAGRList.get(i),currentEquityList.get(i),row);
        }


        System.out.println(row);
    }

    private void writeSystem(String name, Double fullCAGR, Double lastCAGR, BigDecimal equity, StringBuffer row) {

        DecimalFormat def = new DecimalFormat("##.00");

        row.append(' ').append(name).append(' ');
        row.append(def.format(fullCAGR*100d)).append("% ");
        row.append(def.format(lastCAGR*100d)).append("% ");
        row.append(equity);

    }


    private void isoDateTime(Calendar calendar, StringBuffer buffer) {

        int year = calendar.get(Calendar.YEAR);
        buffer.append((char) ('0' + (year / 1000)));
        year = year%1000;
        buffer.append((char) ('0' + (year / 100)));
        year = year%100;
        buffer.append((char) ('0' + (year / 10)));
        year = year%10;
        buffer.append((char) ('0' + (year)));

        buffer.append((char) '-');

        int month = calendar.get(Calendar.MONTH);
        buffer.append((char) ('0' + (month / 10)));
        month = month%10;
        buffer.append((char) ('0' + (month)));

        buffer.append((char) '-');

        int day = calendar.get(Calendar.DAY_OF_MONTH);
        buffer.append((char) ('0' + (day / 10)));
        day = day%10;
        buffer.append((char) ('0' + (day)));

    }

}
