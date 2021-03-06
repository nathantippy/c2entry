/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy   7/4/12
 */
package com.collective2.signalEntry.implementation;

import com.collective2.signalEntry.C2Element;
import com.collective2.signalEntry.C2ServiceException;
import com.collective2.signalEntry.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.Map;

import static com.collective2.signalEntry.C2Element.*;
import static com.collective2.signalEntry.Parameter.*;

public enum Command {

    Signal(true, "signal") {

        protected EnumSet<Parameter> paraRequired() {
            return EnumSet.of(SystemId, Password, Instrument, Symbol, TimeInForce);
        }
        @SuppressWarnings("unchecked")
        protected EnumSet<Parameter>[] paraRequiredExclusiveSets() {
            return new EnumSet[] {
                    EnumSet.of(Action),
                    EnumSet.of(Dollars, Quantity, AccountPercent),
                    EnumSet.of(RelativeLimitOrder, RelativeStopOrder, MarketOrder)
            };
        }
        protected EnumSet<Parameter> paraOptional() {
            return EnumSet.of(OCAId, ForceNoOCA, Delay, XReplace, ConditionalUpon);
        }
        @SuppressWarnings("unchecked")
        protected EnumSet<Parameter>[] paraOptionalExclusiveSets() {
            return new EnumSet[] {
                    EnumSet.of(CancelsAt, CancelsAtRelative),
                    EnumSet.of(ParkUntil, ParkUntilDateTime),
                    EnumSet.of(RelativeStopLoss),
                    EnumSet.of(RelativeProfitTarget)
            };
        }
        protected EnumSet<C2Element> possibleResult() {
            return EnumSet.of(ElementSignalId,ElementStatus,ElementComments,ElementProfitTaretSignalId,ElementStopLossSignalId);
        }
    },
    RequestOCAId(false, "requestocaid") {
        protected EnumSet<Parameter> paraRequired() {
            return EnumSet.of(SystemId, Password);
        }
        protected EnumSet<C2Element> possibleResult() {
            return EnumSet.of(ElementStatus,ElementOCAId);
        }
    },
    Cancel(true, "cancel") {
        protected EnumSet<Parameter> paraRequired() {
            return EnumSet.of(SystemId, Password, SignalId);
        }
        protected EnumSet<C2Element> possibleResult() {
            return EnumSet.of(ElementStatus);
        }
    },
    CloseAllPositions(true, "closeallpositions") {
        protected EnumSet<Parameter> paraRequired() {
            return EnumSet.of(SystemId, Password, SignalId);
        }
        protected EnumSet<C2Element> possibleResult() {
            return EnumSet.of(ElementStatus);
        }
    },
    CancelAllPending(true, "cancelallpending") {
        protected EnumSet<Parameter> paraRequired() {
            return EnumSet.of(SystemId, Password);
        }
        protected EnumSet<C2Element> possibleResult() {
            return EnumSet.of(ElementStatus);
        }
    },
    FlushPendingSignals(true, "flushpendingsignals") {
        protected EnumSet<Parameter> paraRequired() {
            return EnumSet.of(SystemId, Password);
        }
        protected EnumSet<C2Element> possibleResult() {
            return EnumSet.of(ElementStatus);
        }
    },
    GetBuyPower(false, "getbuypower") {
        protected EnumSet<Parameter> paraRequired() {
            return EnumSet.of(SystemId, Password);
        }
        protected EnumSet<C2Element> possibleResult() {
            return EnumSet.of(ElementStatus,ElementCalcTime,ElementBuyPower);
        }
    },
    SignalStatus(false, "signalstatus") {
        protected EnumSet<Parameter> paraRequired() {
            return EnumSet.of(EMail, Password, SignalId);
        }
        @Override
        protected EnumSet<Parameter> paraOptional() {
            return EnumSet.of(ShowDetails, ShowRelated);
        }
        protected EnumSet<C2Element> possibleResult() {
            return EnumSet.of(ElementSignalId,ElementSystemName,ElementPostedWhen,
                              ElementEMailedWhen,ElementKilledWhen,ElementTradedWhen,ElementTradePrice,
                              //when detail is on these will also appear
                              ElementAction,ElementQuant,ElementSymbol,ElementLimit,ElementStop,ElementMarket,
                              ElementTimeInForce,ElementOCAGroupIdDetail);
        }
    },
    PositionStatus(false, "positionstatus") {
        protected EnumSet<Parameter> paraRequired() {
            return EnumSet.of(SystemId, Password, Symbol);
        }
        protected EnumSet<C2Element> possibleResult() {
            return EnumSet.of(ElementStatus,ElementCalcTime,ElementSymbol,ElementPosition);
        }
    },
    AllSystems(false, "allsystems") {
        protected EnumSet<Parameter> paraRequired() {
            return EnumSet.of(EMail, Password);
        }
        protected EnumSet<C2Element> possibleResult() {
            return EnumSet.of(ElementStatus,ElementErrorType,ElementComment);
        }
    },
    AllSignals(false, "getallsignals") {
        protected EnumSet<Parameter> paraRequired() {
            return EnumSet.of(EMail, Password);
        }
        protected EnumSet<C2Element> possibleResult() {
            return EnumSet.of(ElementStatus,ElementSystemId,ElementSignalId);
        }
    },
    GetSystemHypothetical(false, "getsystemhypothetical") {
        protected EnumSet<Parameter> paraRequired() {
            return EnumSet.of(Systems, Password, EMail);
        }
        protected EnumSet<C2Element> possibleResult() {
            return EnumSet.of(ElementSystemId,ElementSystemName,ElementTotalEquityAvail,ElementCash,ElementEquity,ElementMarginUsed);
        }
    },
    AddToOCAGroup(true, "addtoocagroup") {
        protected EnumSet<Parameter> paraRequired() {
            return EnumSet.of(SignalId, Password, EMail, OCAGroupIdToGrow);
        }
        protected EnumSet<C2Element> possibleResult() {
            return EnumSet.of(ElementStatus,ElementDetails);
        }
    },
    NewComment(true, "newcomment") {
        protected EnumSet<Parameter> paraRequired() {
            return EnumSet.of(SystemId, Password, Commentary, SignalId);
        }
        protected EnumSet<C2Element> possibleResult() {
            return EnumSet.of(ElementStatus,ElementSignalId,ElementPreviousComment);
        }
    },
    SetMinBuyPower(true, "setminbuypower") {
        protected EnumSet<Parameter> paraRequired() {
            return EnumSet.of(SystemId, Password, BuyPower);
        }
        protected EnumSet<C2Element> possibleResult() {
            return EnumSet.of(ElementStatus);
        }
    },
    SendSubscriberBroadcast(true, "sendSubscriberBroadcast") {
        protected EnumSet<Parameter> paraRequired() {
            return EnumSet.of(SystemId, Password, EMail, Message);
        }
        protected EnumSet<C2Element> possibleResult() {
            return EnumSet.of(ElementStatus);
        }
    },
    GetSystemEquity(false, "getsystemequity") {
        protected EnumSet<Parameter> paraRequired() {
            return EnumSet.of(SystemId, Password);
        }
        protected EnumSet<C2Element> possibleResult() {
            return EnumSet.of(ElementStatus,ElementCalcTime,ElementSystemEquity);
        }
    },
    Reverse(true, "reverse")  {
        protected EnumSet<Parameter> paraRequired() {
            return EnumSet.of(SystemId, Password, Symbol);
        }
        protected EnumSet<Parameter> paraOptional() {
            return EnumSet.of(TriggerPrice, TimeInForce, Quantity);
        }
        protected EnumSet<C2Element> possibleResult() {
            return EnumSet.of(ElementStatus);
        }
    };

    private static final Logger logger = LoggerFactory.getLogger(Command.class);
    private final EnumSet<Parameter> required;
    private final EnumSet<Parameter>[] requiredExclusiveGroups;
    private final EnumSet<Parameter> optional;
    private final EnumSet<Parameter>[] optionalExclusiveGroups;
    private final EnumSet<C2Element> possibleResult;
    private final String URLToken;
    private final boolean approvalRequired;

    private Command(boolean approvalRequired, String URLToken) {
        this.approvalRequired        = approvalRequired;
        this.URLToken                = URLToken;

        this.required                = paraRequired();
        this.requiredExclusiveGroups = paraRequiredExclusiveSets();
        this.optional                = paraOptional();
        this.optionalExclusiveGroups = paraOptionalExclusiveSets();
        this.possibleResult          = possibleResult();
    }

    public boolean approvalRequired() {
        return approvalRequired;
    }

    protected abstract EnumSet<C2Element> possibleResult();
    protected abstract EnumSet<Parameter> paraRequired();
    @SuppressWarnings("unchecked")
    protected EnumSet<Parameter>[] paraRequiredExclusiveSets() {
        return new EnumSet[] {};
    }
    protected EnumSet<Parameter> paraOptional() {
        return EnumSet.noneOf(Parameter.class);
    }
    @SuppressWarnings("unchecked")
    protected EnumSet<Parameter>[] paraOptionalExclusiveSets() {
        return new EnumSet[] {};
    }

    public String toString() {
    	return URLToken;
    }

    /**
     * Throws Collective2ServiceException if Parameter is not applicable to this Command.
     * The Parameter must be found in the list of documented Parameters and must not
     * already be set.
     *
     * @param activeParaMap map of parameters already set
     * @param p parameter to be set
     */
    public void validateApplicable(Map<Parameter,Object> activeParaMap, Parameter p) {

        boolean found = required.contains(p)||optional.contains(p);
        if (!found) {
            for(EnumSet<Parameter> set:requiredExclusiveGroups) {
               if (set.contains(p)) {
                   found = true;
                   //found in this exclusive set so make sure nothing else in the
                   //set has already been used
                   for(Parameter inSet:set) {
                       if (activeParaMap.containsKey(inSet)) {
                           String message;
                           if (inSet!=p) {
                                message = "parameter "+inSet+" has already been set and "+p+" can not also be set.";
                           } else {
                                message = "parameter "+p+" has already been set and can not be set again.";
                           }
                           logger.error(message);
                           throw new C2ServiceException(message,false);
                       }
                   }
                   break;
               }
            }
        } else {
            //was found in required or optional list, so make sure its not already set
            if (activeParaMap.containsKey(p)) {
                String message = "parameter "+p+" has already been set and can not be set again.";
                logger.error(message);
                throw new C2ServiceException(message,false);
            }
        }

        if (!found) {
            for(EnumSet<Parameter> set:optionalExclusiveGroups) {
                if (set.contains(p)) {
                    found = true;
                    //found in this exclusive set so make sure nothing else in the
                    //set has already been used
                    for(Parameter inSet:set) {
                        if (activeParaMap.containsKey(inSet)) {
                            String message;
                            if (inSet!=p) {
                                message = "parameter "+inSet+" has already been set and "+p+" can not also be set.";
                            } else {
                                message = "parameter "+p+" has already been set and can not be set again.";
                            }
                            logger.error(message);
                            throw new C2ServiceException(message,false);
                        }
                    }
                    break;
                }
            }
        }

        if (!found) {
            String message = "parameter "+p+" is not applicable for "+name();
            logger.error(message);
            throw new C2ServiceException(message,false);
        }
    }

    /**
     * Throws Collective2ServiceException if any of the required parameters have not been set.
     * @param activeParaMap map of parameters already set
     */
    public void validate(Map<Parameter,Object> activeParaMap) {
        //ensure all of required are found in activeParaMap

        for(Parameter p:required) {
            if (!activeParaMap.containsKey(p)) {
                String message = "required parameter "+p+" was not set.";
                logger.error(message);
                throw new C2ServiceException(message,false);
            }
        }
        for(EnumSet<Parameter> set:requiredExclusiveGroups) {
            //return false if any required param is missing
            boolean ok = false;
            for(Parameter p:set) {
                if (activeParaMap.containsKey(p)) {
                    ok = true;
                    break;
                }
            }
            if (!ok) {
                String message = "required parameter (one of these) "+ set+" was not set.";
                logger.error(message);
                throw new C2ServiceException(message,false);
            }
        }
    }

    public void validate(C2Element element) {
        if (!possibleResult.contains(element)) {
            String message = "element "+element.localElementName()+
                             " is not part of the expected response for "+this;
            logger.error(message);
            throw new C2ServiceException(message,false);
        }
    }

}
