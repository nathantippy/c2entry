/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/15/12
 */
package com.collective2.signalEntry;

import static com.collective2.signalEntry.C2Element.ElementOCAId;

import com.collective2.signalEntry.implementation.Command;
import com.collective2.signalEntry.implementation.DotString;
import com.collective2.signalEntry.implementation.InstantCommand;
import com.collective2.signalEntry.implementation.ReversalBase;
import com.collective2.signalEntry.implementation.SignalBase;
import com.collective2.signalEntry.transmission.BackEndAdapter;

public class C2EntryService {

    private final C2ServiceFactory serviceFactory;

    private Integer                commonSystemId;
    private String                 commonPassword;
    private String                 commonEMail;

    public C2EntryService(C2ServiceFactory serviceFactory, String password, int systemId) {
        this.serviceFactory = serviceFactory;
        this.commonPassword = password;
        this.commonSystemId = systemId;
        this.commonEMail = null;
    }

    public C2EntryService(C2ServiceFactory serviceFactory, String password, String eMail) {
        this.serviceFactory = serviceFactory;
        this.commonPassword = password;
        this.commonSystemId = null;
        this.commonEMail = eMail;
    }

    public C2EntryService(C2ServiceFactory serviceFactory, String password, int systemId, String eMail) {
        this.serviceFactory = serviceFactory;
        this.commonPassword = password;
        this.commonSystemId = systemId;
        this.commonEMail = eMail;
    }

    public void systemId(Integer id) {
        this.commonSystemId = id;
    }

    public void password(String password) {
        this.commonPassword = password;
    }

    public void eMail(String eMail) {
        this.commonEMail = eMail;
    }

    public C2ServiceFactory serviceFactory() {
        return serviceFactory;
    }

    public Signal stockSignal(ActionForStock action) {
        return new SignalBase(commonSystemId, commonPassword, action, "stock", serviceFactory);
    }

    public Signal optionSignal(ActionForNonStock action) {
        return new SignalBase(commonSystemId, commonPassword, action, "option", serviceFactory);
    }

    public Signal futureSignal(ActionForNonStock action) {
        return new SignalBase(commonSystemId, commonPassword, action, "future", serviceFactory);
    }

    public Signal forexSignal(ActionForNonStock action) {
        return new SignalBase(commonSystemId, commonPassword, action, "forex", serviceFactory);
    }

    public Reverse reversal(String symbol) {
        return new ReversalBase(commonSystemId, commonPassword, symbol, serviceFactory);
    }

    public Integer requestOneCancelsAnotherId() {
        // never keep an instant command long term because it locks the adapter
        // until the get
        InstantCommand ic = new InstantCommand(serviceFactory) {

            @Override
            protected void initAdapter(BackEndAdapter adapter) {
                adapter.para(Parameter.SignalEntryCommand, Command.RequestOCAId);
                adapter.para(Parameter.SystemId, commonSystemId);
                adapter.para(Parameter.Password, commonPassword);
            }
        };

        return ic.send().getInteger(ElementOCAId);
    }

    public boolean cancel(final Integer signalId) {
        // never keep an instant command long term because it locks the adapter
        // until the get
        InstantCommand ic = new InstantCommand(serviceFactory) {

            @Override
            protected void initAdapter(BackEndAdapter adapter) {
                adapter.para(Parameter.SignalEntryCommand, Command.Cancel);
                adapter.para(Parameter.SignalId, signalId);
                adapter.para(Parameter.SystemId, commonSystemId);
                adapter.para(Parameter.Password, commonPassword);
            }

        };
        return ic.send().isOk();
    }

    public boolean cancelAllPending() {
        // never keep an instant command long term because it locks the adapter
        // until the get
        InstantCommand ic = new InstantCommand(serviceFactory) {

            @Override
            protected void initAdapter(BackEndAdapter adapter) {
                adapter.para(Parameter.SignalEntryCommand, Command.CancelAllPending);
                adapter.para(Parameter.SystemId, commonSystemId);
                adapter.para(Parameter.Password, commonPassword);
            }
        };

        return ic.send().isOk();
    }

    public boolean flushPendingSignals() {
        // never keep an instant command long term because it locks the adapter
        // until the get
        InstantCommand ic = new InstantCommand(serviceFactory) {

            @Override
            protected void initAdapter(BackEndAdapter adapter) {
                adapter.para(Parameter.SignalEntryCommand, Command.FlushPendingSignals);
                adapter.para(Parameter.SystemId, commonSystemId);
                adapter.para(Parameter.Password, commonPassword);
            }
        };

        return ic.send().isOk();
    }

    public boolean closeAllPositions(final Integer signalId) {
        // never keep an instant command long term because it locks the adapter
        // until the get
        InstantCommand ic = new InstantCommand(serviceFactory) {

            @Override
            protected void initAdapter(BackEndAdapter adapter) {
                adapter.para(Parameter.SignalEntryCommand, Command.CloseAllPositions);
                adapter.para(Parameter.SignalId, signalId);
                adapter.para(Parameter.SystemId, commonSystemId);
                adapter.para(Parameter.Password, commonPassword);
            }
        };

        return ic.send().isOk();
    }

    public Response buyPower() {
        return new InstantCommand(serviceFactory) {

            @Override
            protected void initAdapter(BackEndAdapter adapter) {
                adapter.para(Parameter.SignalEntryCommand, Command.GetBuyPower);
                adapter.para(Parameter.SystemId, commonSystemId);
                adapter.para(Parameter.Password, commonPassword);
            }
        }.send();
    }

    public Response signalStatus(final Integer signalId, final boolean showDetails, final Related showRelated) {
        return new InstantCommand(serviceFactory) {

            @Override
            protected void initAdapter(BackEndAdapter adapter) {
                adapter.para(Parameter.SignalEntryCommand, Command.SignalStatus);
                adapter.para(Parameter.EMail, commonEMail);
                adapter.para(Parameter.Password, commonPassword);
                adapter.para(Parameter.SignalId, signalId);
                adapter.para(Parameter.ShowRelated, showRelated);
                if (showDetails) {
                    adapter.para(Parameter.ShowDetails, 1);
                }
            }
        }.send();
    }
    
    public Response signalStatus(final Integer signalId, final boolean showDetails) {
        return new InstantCommand(serviceFactory) {

            @Override
            protected void initAdapter(BackEndAdapter adapter) {
                adapter.para(Parameter.SignalEntryCommand, Command.SignalStatus);
                adapter.para(Parameter.EMail, commonEMail);
                adapter.para(Parameter.Password, commonPassword);
                adapter.para(Parameter.SignalId, signalId);
                if (showDetails) {
                    adapter.para(Parameter.ShowDetails, 1);
                }
            }
        }.send();
    }
    
    
    public Response allSystems() {
        return new InstantCommand(serviceFactory) {

            @Override
            protected void initAdapter(BackEndAdapter adapter) {
                adapter.para(Parameter.SignalEntryCommand, Command.AllSystems);
                adapter.para(Parameter.EMail, commonEMail);
                adapter.para(Parameter.Password, commonPassword);
            }
        }.send();
    }

    public Response systemHypothetical(final Integer... system) {

        return new InstantCommand(serviceFactory) {

            @Override
            protected void initAdapter(BackEndAdapter adapter) {
                adapter.para(Parameter.SignalEntryCommand, Command.GetSystemHypothetical);
                adapter.para(Parameter.Password, commonPassword);
                adapter.para(Parameter.EMail, commonEMail);
                adapter.para(Parameter.Systems, new DotString<Integer>(system));
            }
        }.send();

    }

    public Response allSignals() {
        // never keep an instant command long term because it locks the adapter
        // until the get
        return new InstantCommand(serviceFactory) {

            @Override
            protected void initAdapter(BackEndAdapter adapter) {
                adapter.para(Parameter.SignalEntryCommand, Command.AllSignals);
                adapter.para(Parameter.Password, commonPassword);
                adapter.para(Parameter.EMail, commonEMail);
            }
        }.send();
    }

    public boolean addToOCAGroup(final Integer signalId, final Integer OCAGroup) {
        // never keep an instant command long term because it locks the adapter
        // until the get
        InstantCommand ic = new InstantCommand(serviceFactory) {

            @Override
            protected void initAdapter(BackEndAdapter adapter) {
                adapter.para(Parameter.SignalEntryCommand, Command.AddToOCAGroup);
                adapter.para(Parameter.Password, commonPassword);
                adapter.para(Parameter.EMail, commonEMail);
                adapter.para(Parameter.SignalId, signalId);
                adapter.para(Parameter.OCAGroupId, OCAGroup);
            }

        };

        return ic.send().isOk();
    }

    public boolean setMinBuyPower(final Number buyPower) {
        // never keep an instant command long term because it locks the adapter
        // until the get
        InstantCommand ic = new InstantCommand(serviceFactory) {

            @Override
            protected void initAdapter(BackEndAdapter adapter) {
                adapter.para(Parameter.SignalEntryCommand, Command.SetMinBuyPower);
                adapter.para(Parameter.SystemId, commonSystemId);
                adapter.para(Parameter.Password, commonPassword);
                adapter.para(Parameter.BuyPower, buyPower);
            }

        };

        return ic.send().isOk();
    }

    public boolean sendSubscriberBroadcast(final String message) {
        InstantCommand ic = new InstantCommand(serviceFactory) {

            @Override
            protected void initAdapter(BackEndAdapter adapter) {
                adapter.para(Parameter.SignalEntryCommand, Command.SendSubscriberBroadcast);
                adapter.para(Parameter.SystemId, commonSystemId);
                adapter.para(Parameter.Password, commonPassword);
                adapter.para(Parameter.EMail, commonEMail);
                adapter.para(Parameter.Message, message);
            }
        };

        return ic.send().isOk();
    }

    public Response systemEquity() {
        return new InstantCommand(serviceFactory) {

            @Override
            protected void initAdapter(BackEndAdapter adapter) {
                adapter.para(Parameter.SignalEntryCommand, Command.GetSystemEquity);
                adapter.para(Parameter.SystemId, commonSystemId);
                adapter.para(Parameter.Password, commonPassword);
            }
        }.send();
    }

    public Response positionStatus(final String symbol) {
        return new InstantCommand(serviceFactory) {

            @Override
            protected void initAdapter(BackEndAdapter adapter) {
                adapter.para(Parameter.SignalEntryCommand, Command.PositionStatus);
                adapter.para(Parameter.SystemId, commonSystemId);
                adapter.para(Parameter.Password, commonPassword);
                adapter.para(Parameter.Symbol, symbol);
            }

        }.send();
    }

    public Response newComment(final String comment, final Integer signalId) {
        return new InstantCommand(serviceFactory) {

            @Override
            protected void initAdapter(BackEndAdapter adapter) {
                adapter.para(Parameter.SignalEntryCommand, Command.NewComment);
                adapter.para(Parameter.SystemId, commonSystemId);
                adapter.para(Parameter.Password, commonPassword);
                adapter.para(Parameter.Commentary, comment);
                adapter.para(Parameter.SignalId, signalId);

            }
        }.send();

    }

}
