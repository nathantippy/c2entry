Domain specific Java API for submitting trade signals to Collective2.  Ensures URLs are built with all the required parameters and provides an abstraction layer to simulate signal entry for off-line testing.


URLs produced by this API will conform to the documentation below.  If you find any issues please report them so the unit tests can be expanded to cover the new features.

[Collective2 Signal Entry API](http://collective2.com/content/automation.htm)

## Features ##
  * Validates URL construction
    * Parameters are checked to ensure they belong to the requested command.
    * Commands are checked to ensure they contain the required parameters.
    * Parameters are checked to ensure they are not in conflict with each other.
    * Strong type checking is applied to parameter values.
  * Helper methods are added for easy parsing of the returned XML.
    * isOK() is on most responses (others might use getInteger())
    * StAX reader is provided if application needs full response details.
  * Simulator provided for testing off line
    * Simulator DOES NOT simulate trades (but it could be extended to do this)
    * Simulator returns hard coded responses to simulate only existence of a server
  * Background asynchronous transmission of of signals.
    * All signals are sent in the same order the response objects are returned.
    * Calling any of the get methods on a response object will block until all previous responses are fetched.
  * Auto retry of signal delivery
    * Default (RAM only) journal will continue to retry every 10 seconds if network is down.
    * File based journal will continue to retry even after a restart of the JVM.



## Examples ##
### Factory Construction ###
```
   isLive = false;
   C2ServiceFactory factory;

   if (isLive) {
     // connects to collective2.com and returns the responses
     C2EntryServiceAdapter liveAdapter = new Collective2Adapter();
     factory = new C2ServiceFactory(liveAdapter);
   } else {
     // validates commands and returns hard coded (canned) responses
     C2EntryServiceAdapter simulationAdapter = new StaticSimulationAdapter();
     factory = new C2ServiceFactory(simulationAdapter);
   }
```

For optional flat file persistence try the following
```
   int rollingLogLimit = 1048576; //1Mb for each log file
   File file = new File("/tmp/journalFile.log");
   C2EntryServiceJournal journal = new C2EntryServiceLogFileJournal(file, rollingLogLimit);

   factory = new C2ServiceFactory(new StaticSimulationAdapter(), journal);
            
```

### Service Construction ###
```
        String password = "PA55WORD";
        Integer systemId = 99999999;
        String eMail = "someone@somewhere.com";
        C2EntryService sentryService;

        sentryService = factory.signalEntryService(password, systemId, eMail);

```

### Service Usage ###
```
        List<Response> responseList = new ArrayList<Response>();

        responseList.add(sentryService.stockSignal(ActionForStock.BuyToOpen)
                                      .marketOrder().quantity(10).symbol("msft")
                                      .duration(Duration.GoodTilCancel).send());

        responseList.add(sentryService.stockSignal(ActionForStock.BuyToOpen)
                                        .limitOrder(new BigDecimal("23.4")).quantity(10).symbol("www")
                                        .duration(Duration.GoodTilCancel).send());

        responseList.add(sentryService.stockSignal(ActionForStock.BuyToOpen)
                                        .limitOrder(BasePrice.SessionOpenPlus,new BigDecimal(".50")).quantity(10).symbol("ibm")
                                        .duration(Duration.GoodTilCancel).send());

        //after some time...  ask for an arbitrary signal id.

        Integer signalId = responseList.get(2).getInteger(C2Element.ElementSignalId);

```