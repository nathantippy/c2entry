package com.collective2.signalEntry.adapter.dynamicSimulator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  8/3/12
 */

public class GainListenerManager {

    public final static long ONE_YEAR_MS = 31558464000l;

    long             lastTime = Long.MIN_VALUE;
    List<BigDecimal> lastTotalEquityList;

    long             firstTime;
    List<BigDecimal> firstTotalEquityList;

    long             start;
    long             period;
    GainListener     listener;

    public GainListenerManager(long start, long period, GainListener listener) {
        this.start = start;
        this.period = period;
        this.listener = listener;

    }

    public void send(final long now, Executor gainExecutor, List<SystemManager> systems, DataProvider dataProvider) {
        assert(now>=0);
        if (isTimeToSend(now)) {

            final List<BigDecimal> totalEquityList = new ArrayList<BigDecimal>();
            final List<String> nameList = new ArrayList<String>();
            for(SystemManager system:systems) {
                BigDecimal totalEquity = system.portfolio().cash().add(system.portfolio().equity(dataProvider));
                totalEquityList.add(totalEquity);
                nameList.add(system.statusMessage());//name with details

            }

            //must have old values and same number of systems
            if (lastTime>=0 && totalEquityList.size()==lastTotalEquityList.size()) {
                //we have all the old values and the new values, ready for sending.
                gainExecutor.execute(new Runnable() {
                    @Override
                    public void run() {

                            long unitLength = now - lastTime;
                            long fullLength = now - firstTime;

                            double unitYears = unitLength/(double)ONE_YEAR_MS;
                            double fullYears = fullLength/(double)ONE_YEAR_MS;

                            List<Double> unitCAGRList = new ArrayList<Double>();
                            List<Double> fullCAGRList = new ArrayList<Double>();

                            for(int i=0;i<totalEquityList.size();i++) {
                                double unitCAGR;
                                double fullCAGR;

                                if (unitYears<1) {
                                    unitCAGR =  (totalEquityList.get(i).doubleValue()/lastTotalEquityList.get(i).doubleValue()*unitYears)-1d;
                                } else {
                                    unitCAGR = computeDiscountRate(lastTotalEquityList.get(i).doubleValue(),totalEquityList.get(i).doubleValue(),unitYears);
                                }
                                unitCAGRList.add(unitCAGR);

                                if (fullYears<1) {
                                    fullCAGR =  (totalEquityList.get(i).doubleValue()/firstTotalEquityList.get(i).doubleValue()*fullYears)-1d;
                                } else {
                                    fullCAGR = computeDiscountRate(firstTotalEquityList.get(i).doubleValue(),totalEquityList.get(i).doubleValue(),fullYears);
                                }
                                fullCAGRList.add(fullCAGR);
                            }

                            listener.gainData(now, nameList, firstTotalEquityList, fullCAGRList, lastTotalEquityList, unitCAGRList, totalEquityList);

                    }

                    /*
                     DiscountRate also known as Compound Annual Growth Rate (CAGR)
                     computed by:   r   =   ((FinalValue / PresentValue)^(1 / Years))  -  1
                    */
                    public double computeDiscountRate(double pV, double fV, double years) {
                        assert(years>0);
                        return Math.pow(fV/pV, (1d/years))-1d;
                    }

                });

            }
            //store these values for next time
            lastTotalEquityList = totalEquityList;
            if (firstTotalEquityList == null) {
                firstTotalEquityList = totalEquityList;
                firstTime = now;
            }
            lastTime = now;
        }

    }

    private boolean isTimeToSend(long now) {
         if (now>start) {
            long duration = now-start;
            return 0==(duration%now);
         }
        return false;
    }
}
