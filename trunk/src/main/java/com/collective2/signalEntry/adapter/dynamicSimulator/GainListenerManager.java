package com.collective2.signalEntry.adapter.dynamicSimulator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

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

    long             firstTime = Long.MIN_VALUE;
    List<BigDecimal> firstTotalEquityList;

    long             start;
    long             period;
    long             lastSentTime;
    GainListener     listener;
    boolean          isFinal;

    public GainListenerManager(long start, long period, GainListener listener) {
        this.start = start;
        this.period = period;
        this.lastSentTime = start-period;
        this.listener = listener;

    }

    public void send(Executor gainExecutor, DataProvider dataProvider, SystemManager ... systems) {
        final long now = dataProvider.endingTime();

        //establish start time
        if (firstTime == Long.MIN_VALUE) {
            firstTime = dataProvider.startingTime();
        }
        lastTime = dataProvider.startingTime();
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
            if (firstTotalEquityList!=null && totalEquityList.size()==lastTotalEquityList.size()) {
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
                                    if (unitYears==0) {
                                        unitCAGR = Double.NaN;
                                    } else {
                                        unitCAGR = ((totalEquityList.get(i).doubleValue()/lastTotalEquityList.get(i).doubleValue())-1d)/unitYears;
                                    }
                                } else {
                                    unitCAGR = computeDiscountRate(lastTotalEquityList.get(i).doubleValue(),totalEquityList.get(i).doubleValue(),unitYears);
                                }
                                unitCAGRList.add(unitCAGR);

                                if (fullYears<1) {
                                    if (fullYears==0) {
                                        fullCAGR = Double.NaN;
                                    } else {
                                        fullCAGR =  ((totalEquityList.get(i).doubleValue()/firstTotalEquityList.get(i).doubleValue())-1d)/fullYears;
                                    }
                                } else {
                                    fullCAGR = computeDiscountRate(firstTotalEquityList.get(i).doubleValue(),totalEquityList.get(i).doubleValue(),fullYears);
                                }
                                fullCAGRList.add(fullCAGR);
                            }

                            //move down so its seen every time!
                            listener.gainData(now, nameList, firstTotalEquityList, fullCAGRList, lastTotalEquityList, unitCAGRList, totalEquityList);

                            //store these equity values for next time
                            lastTotalEquityList = totalEquityList;
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

            if (firstTotalEquityList == null) {
                //kick start these values
                firstTotalEquityList = totalEquityList;
                lastTotalEquityList = totalEquityList;
            }

            if (totalEquityList.size()!=lastTotalEquityList.size()) {
                lastTotalEquityList = totalEquityList;
            }

        }

    }

    private boolean isTimeToSend(long now) {
         if (isFinal) {
             return true;
         }
         if (now>start) {
            long duration = now-lastSentTime;
             if (duration>=period) {
                 lastSentTime+=period;
                 return true;
             }
         }
        return false;
    }

    public void sendFinal(ExecutorService gainExecutor, DataProvider dataProvider, SystemManager systemManager) {
        isFinal = true;
        send(gainExecutor,dataProvider,systemManager);
    }
}