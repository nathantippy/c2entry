/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/15/12
 */
package com.collective2.signalEntry;

import static com.collective2.signalEntry.C2Element.ElementOCAId;

import com.collective2.signalEntry.implementation.*;

public class C2EntryService {

    private final C2ServiceFactory serviceFactory;

    private Integer commonSystemId;
    private String commonPassword;
    private String commonEMail;

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
        return new SignalBase(commonSystemId, commonPassword, action, "stock", serviceFactory.adapter());
    }

    public Signal optionSignal(ActionForNonStock action) {
        return new SignalBase(commonSystemId, commonPassword, action, "option", serviceFactory.adapter());
    }

    public Signal futureSignal(ActionForNonStock action) {
        return new SignalBase(commonSystemId, commonPassword, action, "future", serviceFactory.adapter());
    }

    public Signal forexSignal(ActionForNonStock action) {
        return new SignalBase(commonSystemId, commonPassword, action, "forex", serviceFactory.adapter());
    }

    public Reverse reversal(String symbol) {
        return new ReversalBase(commonSystemId, commonPassword, symbol, serviceFactory.adapter());
    }

    private Response send(Request request) {
        request.validate();
        return new ImplResponse(serviceFactory.adapter().transmit(request), request.getCommand());
    }

    public Integer requestOneCancelsAnotherId() {
        return sendRequestOneCancelsAnotherId().getInteger(ElementOCAId);
    }

    public Response sendRequestOneCancelsAnotherId() {
        Request request = new Request(Command.RequestOCAId);
        request.put(Parameter.SystemId, commonSystemId);
        request.put(Parameter.Password, commonPassword);
        return send(request);
    }

    public boolean cancel(final Integer signalId) {
        return sendCancel(signalId).isOk();
    }

    public Response sendCancel(final Integer signalId) {
        Request request = new Request(Command.Cancel);
        request.put(Parameter.SignalId, signalId);
        request.put(Parameter.SystemId, commonSystemId);
        request.put(Parameter.Password, commonPassword);
        return send(request);
    }

    public boolean cancelAllPending() {
        return sendCancelAllPending().isOk();
    }

    public Response sendCancelAllPending() {
        Request request = new Request(Command.CancelAllPending);
        request.put(Parameter.SystemId, commonSystemId);
        request.put(Parameter.Password, commonPassword);
        return send(request);
    }

    public boolean flushPendingSignals() {
        return sendFlushPendingSignals().isOk();
    }

    public Response sendFlushPendingSignals() {
        Request request = new Request(Command.FlushPendingSignals);
        request.put(Parameter.SystemId, commonSystemId);
        request.put(Parameter.Password, commonPassword);
        return send(request);
    }

    public boolean closeAllPositions(final Integer signalId) {
        Request request = new Request(Command.CloseAllPositions);
        request.put(Parameter.SignalId, signalId);
        request.put(Parameter.SystemId, commonSystemId);
        request.put(Parameter.Password, commonPassword);
        return send(request).isOk();
    }

    public Response buyPower() {
        Request request = new Request(Command.GetBuyPower);
        request.put(Parameter.SystemId, commonSystemId);
        request.put(Parameter.Password, commonPassword);
        return send(request);
    }

    public Response signalStatus(final Integer signalId, final boolean showDetails, final Related showRelated) {
        Request request = new Request(Command.SignalStatus);
        request.put(Parameter.EMail, commonEMail);
        request.put(Parameter.Password, commonPassword);
        request.put(Parameter.SignalId, signalId);
        request.put(Parameter.ShowRelated, showRelated);
        if (showDetails) {
            request.put(Parameter.ShowDetails, 1);
        }
        return send(request);
    }

    public Response signalStatus(final Integer signalId, final boolean showDetails) {
        Request request = new Request(Command.SignalStatus);
        request.put(Parameter.EMail, commonEMail);
        request.put(Parameter.Password, commonPassword);
        request.put(Parameter.SignalId, signalId);
        if (showDetails) {
            request.put(Parameter.ShowDetails, 1);
        }
        return send(request);
    }


    public Response allSystems() {
        Request request = new Request(Command.AllSystems);
        request.put(Parameter.EMail, commonEMail);
        request.put(Parameter.Password, commonPassword);
        return send(request);
    }

    public Response systemHypothetical(final Integer... system) {
        Request request = new Request(Command.GetSystemHypothetical);
        request.put(Parameter.Password, commonPassword);
        request.put(Parameter.EMail, commonEMail);
        request.put(Parameter.Systems, new DotString<Integer>(system));
        return send(request);

    }

    public Response allSignals() {
        Request request = new Request(Command.AllSignals);
        request.put(Parameter.Password, commonPassword);
        request.put(Parameter.EMail, commonEMail);
        return send(request);
    }

    public boolean addToOCAGroup(final Integer signalId, final Integer OCAGroup) {
        Request request = new Request(Command.AddToOCAGroup);
        request.put(Parameter.Password, commonPassword);
        request.put(Parameter.EMail, commonEMail);
        request.put(Parameter.SignalId, signalId);
        request.put(Parameter.OCAGroupId, OCAGroup);
        return send(request).isOk();
    }

    public boolean setMinBuyPower(final Number buyPower) {
        Request request = new Request(Command.SetMinBuyPower);
        request.put(Parameter.SystemId, commonSystemId);
        request.put(Parameter.Password, commonPassword);
        request.put(Parameter.BuyPower, buyPower);
        return send(request).isOk();
    }

    public boolean sendSubscriberBroadcast(final String message) {
        Request request = new Request(Command.SendSubscriberBroadcast);
        request.put(Parameter.SystemId, commonSystemId);
        request.put(Parameter.Password, commonPassword);
        request.put(Parameter.EMail, commonEMail);
        request.put(Parameter.Message, message);
        return send(request).isOk();
    }

    public Response systemEquity() {
        Request request = new Request(Command.GetSystemEquity);
        request.put(Parameter.SystemId, commonSystemId);
        request.put(Parameter.Password, commonPassword);
        return send(request);
    }

    public Response positionStatus(final String symbol) {
        Request request = new Request(Command.PositionStatus);
        request.put(Parameter.SystemId, commonSystemId);
        request.put(Parameter.Password, commonPassword);
        request.put(Parameter.Symbol, symbol);
        return send(request);
    }

    public Response newComment(final String comment, final Integer signalId) {
        Request request = new Request(Command.NewComment);
        request.put(Parameter.SystemId, commonSystemId);
        request.put(Parameter.Password, commonPassword);
        request.put(Parameter.Commentary, comment);
        request.put(Parameter.SignalId, signalId);
        return send(request);

    }

}
