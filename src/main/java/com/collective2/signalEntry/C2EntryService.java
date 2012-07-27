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

    //responsible for ensuring the order of the signals
    private final ResponseManager responseManager;

    private Integer commonSystemId;
    private String commonPassword;
    private String commonEMail;

    public C2EntryService(C2ServiceFactory serviceFactory, String password, int systemId) {
        this.serviceFactory = serviceFactory;
        this.commonPassword = password;
        this.commonSystemId = systemId;
        this.commonEMail = null;
        this.responseManager = new ResponseManager(serviceFactory.entryServiceAdapter(),
                                                   serviceFactory.entryServiceJournal(),
                                                   password);
    }

    public C2EntryService(C2ServiceFactory serviceFactory, String password, String eMail) {
        this.serviceFactory = serviceFactory;
        this.commonPassword = password;
        this.commonSystemId = null;
        this.commonEMail = eMail;
        this.responseManager = new ResponseManager(serviceFactory.entryServiceAdapter(),
                                                   serviceFactory.entryServiceJournal(),
                                                   password);
    }

    public C2EntryService(C2ServiceFactory serviceFactory, String password, int systemId, String eMail) {
        this.serviceFactory = serviceFactory;
        this.commonPassword = password;
        this.commonSystemId = systemId;
        this.commonEMail = eMail;
        this.responseManager = new ResponseManager(serviceFactory.entryServiceAdapter(),
                                                   serviceFactory.entryServiceJournal(),
                                                   password);
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
        return new SignalBase(commonSystemId, commonPassword, action, "stock", responseManager);
    }

    public Signal optionSignal(ActionForNonStock action) {
        return new SignalBase(commonSystemId, commonPassword, action, "option", responseManager);
    }

    public Signal futureSignal(ActionForNonStock action) {
        return new SignalBase(commonSystemId, commonPassword, action, "future", responseManager);
    }

    public Signal forexSignal(ActionForNonStock action) {
        return new SignalBase(commonSystemId, commonPassword, action, "forex", responseManager);
    }

    public Reverse reversal(String symbol) {
        return new ReversalBase(commonSystemId, commonPassword, symbol, responseManager);
    }

    private Response send(Request request) {
        return responseManager.fetchResponse(request);
    }

    public Integer oneCancelsAnotherId() {
        return sendOneCancelsAnotherIdRequest().getInteger(ElementOCAId);
    }

    public Response sendOneCancelsAnotherIdRequest() {
        Request request = new Request(Command.RequestOCAId);
        request.put(Parameter.SystemId, commonSystemId);
        request.put(Parameter.Password, commonPassword);
        return send(request);
    }

    public boolean cancel(final Integer signalId) {
        return sendCancelRequest(signalId).isOk();
    }

    public Response sendCancelRequest(final Integer signalId) {
        Request request = new Request(Command.Cancel);
        request.put(Parameter.SignalId, signalId);
        request.put(Parameter.SystemId, commonSystemId);
        request.put(Parameter.Password, commonPassword);
        return send(request);
    }

    public boolean cancelAllPending() {
        return sendCancelAllPendingRequest().isOk();
    }

    public Response sendCancelAllPendingRequest() {
        Request request = new Request(Command.CancelAllPending);
        request.put(Parameter.SystemId, commonSystemId);
        request.put(Parameter.Password, commonPassword);
        return send(request);
    }

    public boolean flushPendingSignals() {
        return sendFlushPendingSignalsRequest().isOk();
    }

    public Response sendFlushPendingSignalsRequest() {
        Request request = new Request(Command.FlushPendingSignals);
        request.put(Parameter.SystemId, commonSystemId);
        request.put(Parameter.Password, commonPassword);
        return send(request);
    }

    public boolean closeAllPositions(final Integer signalId) {
       return sendCloseAllPositionsRequest(signalId).isOk();
    }

    public Response sendCloseAllPositionsRequest(final Integer signalId) {
        Request request = new Request(Command.CloseAllPositions);
        request.put(Parameter.SignalId, signalId);
        request.put(Parameter.SystemId, commonSystemId);
        request.put(Parameter.Password, commonPassword);
        return send(request);
    }

    public Response sendBuyPowerRequest() {
        Request request = new Request(Command.GetBuyPower);
        request.put(Parameter.SystemId, commonSystemId);
        request.put(Parameter.Password, commonPassword);
        return send(request);
    }

    public Response sendSignalStatusRequest(final Integer signalId, final boolean showDetails, final Related showRelated) {
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

    public Response sendSignalStatusRequest(final Integer signalId, final boolean showDetails) {
        Request request = new Request(Command.SignalStatus);
        request.put(Parameter.EMail, commonEMail);
        request.put(Parameter.Password, commonPassword);
        request.put(Parameter.SignalId, signalId);
        if (showDetails) {
            request.put(Parameter.ShowDetails, 1);
        }
        return send(request);
    }


    public Response sendAllSystemsRequest() {
        Request request = new Request(Command.AllSystems);
        request.put(Parameter.EMail, commonEMail);
        request.put(Parameter.Password, commonPassword);
        return send(request);
    }

    public Response sendSystemHypotheticalRequest(final Integer... system) {
        Request request = new Request(Command.GetSystemHypothetical);
        request.put(Parameter.Password, commonPassword);
        request.put(Parameter.EMail, commonEMail);
        request.put(Parameter.Systems, new DotString<Integer>(system));
        return send(request);

    }

    public Response sendAllSignalsRequest() {
        Request request = new Request(Command.AllSignals);
        request.put(Parameter.Password, commonPassword);
        request.put(Parameter.EMail, commonEMail);
        return send(request);
    }

    public boolean addToOCAGroup(final Integer signalId, final Integer OCAGroup) {
        return sendAddToOCAGroupRequest(signalId,OCAGroup).isOk();
    }
    public Response sendAddToOCAGroupRequest(final Integer signalId, final Integer OCAGroup) {
        Request request = new Request(Command.AddToOCAGroup);
        request.put(Parameter.Password, commonPassword);
        request.put(Parameter.EMail, commonEMail);
        request.put(Parameter.SignalId, signalId);
        request.put(Parameter.OCAGroupId, OCAGroup);
        return send(request);
    }

    public boolean setMinBuyPower(final Number buyPower) {
        return sendSetMinBuyPowerRequest(buyPower).isOk();
    }

    public Response sendSetMinBuyPowerRequest(final Number buyPower) {
        Request request = new Request(Command.SetMinBuyPower);
        request.put(Parameter.SystemId, commonSystemId);
        request.put(Parameter.Password, commonPassword);
        request.put(Parameter.BuyPower, buyPower);
        return send(request);
    }

    public boolean subscriberBroadcast(final String message) {
        return sendSubscriberBroadcastRequest(message).isOk();
    }

    public Response sendSubscriberBroadcastRequest(final String message) {
        Request request = new Request(Command.SendSubscriberBroadcast);
        request.put(Parameter.SystemId, commonSystemId);
        request.put(Parameter.Password, commonPassword);
        request.put(Parameter.EMail, commonEMail);
        request.put(Parameter.Message, message);
        return send(request);
    }

    public Response sendSystemEquityRequest() {
        Request request = new Request(Command.GetSystemEquity);
        request.put(Parameter.SystemId, commonSystemId);
        request.put(Parameter.Password, commonPassword);
        return send(request);
    }

    public Response sendPositionStatusRequest(final String symbol) {
        Request request = new Request(Command.PositionStatus);
        request.put(Parameter.SystemId, commonSystemId);
        request.put(Parameter.Password, commonPassword);
        request.put(Parameter.Symbol, symbol);
        return send(request);
    }

    public Response sendNewCommentRequest(final String comment, final Integer signalId) {
        Request request = new Request(Command.NewComment);
        request.put(Parameter.SystemId, commonSystemId);
        request.put(Parameter.Password, commonPassword);
        request.put(Parameter.Commentary, comment);
        request.put(Parameter.SignalId, signalId);
        return send(request);

    }

    public void awaitPending() {
        responseManager.awaitPending();
    }

    void shutdown() {
        responseManager.shutdown();
    }
}
