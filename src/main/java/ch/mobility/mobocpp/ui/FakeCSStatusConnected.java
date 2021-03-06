package ch.mobility.mobocpp.ui;

import ch.mobility.ocpp2mob.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FakeCSStatusConnected {

    static String KeyLadestationOhneStammdaten = "CS-bei-Mobility-Unbekannt"; // F1
    static String KeyLadestationBekanntKeineVerbindungLetzerKontaktKleinerN = "CS-W1"; // W1
    static String KeyLadestationBekanntKeineVerbindungLetzerKontaktGroesserN = "CS-F1"; // F1
    static String KeyLadestationBekanntVerbindungBestehtLadestationMeldetFehler = "CS-W2"; // W2
    static String KeyLadestationBekanntVerbindungBestehtKeinFahrzeugAngeschlossen = "CS-O1"; // O1
    static String KeyLadestationBekanntVerbindungBestehtFahrzeugAngeschlossenNichtAmLadenA = "CS-O2a"; // O2a
    static String KeyLadestationBekanntVerbindungBestehtFahrzeugAngeschlossenNichtAmLadenB = "CS-O2b"; // O2b
    static String KeyLadestationBekanntVerbindungBestehtFahrzeugAngeschlossenNichtAmLadenC = "CS-O2c"; // O2c
    static String KeyLadestationBekanntVerbindungBestehtFahrzeugAngeschlossenAmLaden = "CS-O3"; // O3

    public static CSStatusConnectedResponse addFakeCS(Map<String, Instant> lastContact) {
        // Wir nehmen mal N = 5 Minuten an:
        final Instant w1 = Instant.now().minus(3, ChronoUnit.MINUTES);
        final Instant f1 = Instant.now().minus(8, ChronoUnit.MINUTES);
//        log("Zeitpunkt W1: " + w1);
//        log("Zeitpunkt F1: " + f1);
        lastContact.put(KeyLadestationOhneStammdaten, Instant.now());
        lastContact.put(KeyLadestationBekanntKeineVerbindungLetzerKontaktKleinerN, w1);
        lastContact.put(KeyLadestationBekanntKeineVerbindungLetzerKontaktGroesserN, f1);
        lastContact.put(KeyLadestationBekanntVerbindungBestehtLadestationMeldetFehler, Instant.now());
        lastContact.put(KeyLadestationBekanntVerbindungBestehtKeinFahrzeugAngeschlossen, Instant.now());
        lastContact.put(KeyLadestationBekanntVerbindungBestehtFahrzeugAngeschlossenNichtAmLadenA, Instant.now());
        lastContact.put(KeyLadestationBekanntVerbindungBestehtFahrzeugAngeschlossenNichtAmLadenB, Instant.now());
        lastContact.put(KeyLadestationBekanntVerbindungBestehtFahrzeugAngeschlossenNichtAmLadenC, Instant.now());
        lastContact.put(KeyLadestationBekanntVerbindungBestehtFahrzeugAngeschlossenAmLaden, Instant.now());

        List<CSStatusConnected> result = new ArrayList<>();
        result.add(createFakeCSConnected(lastContact, KeyLadestationOhneStammdaten, ConnectorStatusEnum.Available, ChargingStateEnum.Idle));
        result.add(createFakeCSConnected(lastContact, KeyLadestationBekanntVerbindungBestehtLadestationMeldetFehler, ConnectorStatusEnum.Faulted, null));
        result.add(createFakeCSConnected(lastContact, KeyLadestationBekanntVerbindungBestehtKeinFahrzeugAngeschlossen, ConnectorStatusEnum.Available, ChargingStateEnum.Idle));
        result.add(createFakeCSConnected(lastContact, KeyLadestationBekanntVerbindungBestehtFahrzeugAngeschlossenNichtAmLadenA, ConnectorStatusEnum.Occupied, ChargingStateEnum.EVConnected));
        result.add(createFakeCSConnected(lastContact, KeyLadestationBekanntVerbindungBestehtFahrzeugAngeschlossenNichtAmLadenB, ConnectorStatusEnum.Occupied, ChargingStateEnum.SuspendedEV));
        result.add(createFakeCSConnected(lastContact, KeyLadestationBekanntVerbindungBestehtFahrzeugAngeschlossenNichtAmLadenC, ConnectorStatusEnum.Occupied, ChargingStateEnum.SuspendedEVSE));
        result.add(createFakeCSConnected(lastContact, KeyLadestationBekanntVerbindungBestehtFahrzeugAngeschlossenAmLaden, ConnectorStatusEnum.Occupied, ChargingStateEnum.Charging));

        final CSResponse csResponseFake = CSResponse.newBuilder()
                .setResponseCreatedAt(DateTimeHelper.now())
                .setOCPPBackendId("fake")
                .setMessageId("fake")
                .build();
        return CSStatusConnectedResponse.newBuilder()
                .setResponseInfo(csResponseFake)
                .setCSStatusList(result)
                .build();
    }

    private static CSStatusConnected createFakeCSConnected(
            Map<String, Instant> lastContact,
            String id,
            ConnectorStatusEnum connectorStatus,
            ChargingStateEnum chargingState) {
        Instant lastContactValue = lastContact.get(id);

        List<CSStatusConnectedConnector> list = new ArrayList<>();
        CSStatusConnectedConnector.Builder builder = CSStatusConnectedConnector.newBuilder();
        builder.setConnectorId(1)
                .setConnectorStatus(connectorStatus)
                .setChargingState(chargingState);
        list.add(builder.build());

        return CSStatusConnected.newBuilder()
                .setId(id)
                .setCSStatusConnectorList(list)
                .setLastContact(DateTimeHelper.format(lastContactValue))
                .setModel("FakeModel")
                .setVendor("FakeVendor")
                .build();
    }

    public static void log(String msg) {
        System.out.println(msg);
    }
}
