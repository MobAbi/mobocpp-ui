package ch.mobility.mobocpp.kafka;

import ch.mobility.kafka.AvroConsumer;
import ch.mobility.kafka.AvroProducer;
import ch.mobility.mob2ocpp.TriggerKeywordsV1XEnum;
import ch.mobility.mobocpp.stammdaten.StammdatenAccessor;
import ch.mobility.mobocpp.stammdaten.StammdatenLadestation;
import ch.mobility.mobocpp.util.LadestationMitLaufendenLadevorgang;
import ch.mobility.mobocpp.util.LadestationMitLaufendenLadevorgangListener;
import ch.mobility.ocpp2mob.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AvroProsumer implements Runnable {

    private static LadestationMitLaufendenLadevorgangListener ladestationMitLaufendenLadevorgangListener = null;

    public static void init(LadestationMitLaufendenLadevorgangListener ladestationMitLaufendenLadevorgangListener, String hostIPvalue) {
        AvroProsumer.ladestationMitLaufendenLadevorgangListener = ladestationMitLaufendenLadevorgangListener;
        AvroProducer.init(hostIPvalue);
        AvroConsumer.init(hostIPvalue, "mobocpp-ui");
        AvroProsumer.get();// Start "Update Number of Backends-Thread"
    }

    private static final long UPDATE_INTERVAL = 60L * 1000L;
    private static final long MAX_WAIT_FOR_RESPONSE_MS = 5000L;
    private static final int MAX_NUMBER_OF_OCPP_BACKENDS = 5;

    private static AvroProsumer INSTANCE = null;

    public static synchronized AvroProsumer get() {
        if (INSTANCE == null) {
            INSTANCE = new AvroProsumer();
        }
        return INSTANCE;
    }

    final long wait;
    int maxMobOCPPBackends;
    boolean run;

    private AvroProducer getAvroProducer() {
        return AvroProducer.get();
    }

    private AvroConsumer getAvroConsumer() {
        return AvroConsumer.get();
    }

    private AvroProsumer() {
        this.wait = MAX_WAIT_FOR_RESPONSE_MS;
        this.maxMobOCPPBackends = MAX_NUMBER_OF_OCPP_BACKENDS;
        this.run = true;
        new Thread(this).start();
    }

    public void close() {
        this.run = false;
        AvroProducer.get().close();
        AvroConsumer.get().close();
    }

    private List<CSStatusConnectedResponse> getStatusConnected(long wait, int maxMobOCPPBackends) {
        synchronized (this) {
            final String messageId = UUID.randomUUID().toString();
            getAvroProducer().requestStatusConnected(messageId);
            return getAvroConsumer().receive(CSStatusConnectedResponse.class, messageId, wait, maxMobOCPPBackends);
        }
    }

    // TODO Je CS die Antworten gruppieren und wenn mehr als ein Treffer, dann nur denjenigen verwenden mit den jüngsten Heartbeat-TS
    public List<CSRecentlyConnectedResponse> getRecentlyConnected(Integer daysOfHistoryData) {
        synchronized (this) {
            final String messageId = UUID.randomUUID().toString();
            getAvroProducer().requestRecentlyConnected(messageId, daysOfHistoryData);
            return getAvroConsumer().receive(CSRecentlyConnectedResponse.class, messageId, MAX_WAIT_FOR_RESPONSE_MS, MAX_NUMBER_OF_OCPP_BACKENDS);
        }
    }

    public List<CSStatusConnectedResponse> getStatusConnected() {
        return getStatusConnected(this.wait, this.maxMobOCPPBackends);
    }

    public List<CSStatusForIdResponse> getStatusForId(String csId, Integer connectorId, Integer daysOfHistoryData) {
        synchronized (this) {
            final String messageId = UUID.randomUUID().toString();
            getAvroProducer().requestStatusForId(messageId, csId, connectorId, daysOfHistoryData);
            return getAvroConsumer().receive(CSStatusForIdResponse.class, messageId, wait, 1);
        }
    }

    public List<CSResetResponse> doReset(String csId) {
        synchronized (this) {
            final String messageId = UUID.randomUUID().toString();
            getAvroProducer().requestReset(messageId, csId);
            return getAvroConsumer().receive(CSResetResponse.class, messageId, wait, 1);
        }
    }

    public List<CSUnlockResponse> doUnlock(String csId) {
        synchronized (this) {
            final String messageId = UUID.randomUUID().toString();
            getAvroProducer().requestUnlock(messageId, csId, Integer.valueOf(1)); // TODO Parameter
            return getAvroConsumer().receive(CSUnlockResponse.class, messageId, wait, 1);
        }
    }

    public List<CSTriggerResponse> doTriggerStatus(String csId) {
        synchronized (this) {
            final String messageId = UUID.randomUUID().toString();
            getAvroProducer().requestTrigger(messageId, csId, Integer.valueOf(1), TriggerKeywordsV1XEnum.STATUS.name()); // TODO Parameter
            return getAvroConsumer().receive(CSTriggerResponse.class, messageId, wait, 1);
        }
    }

    public List<CSTriggerResponse> doTriggerMeterValues(String csId) {
        synchronized (this) {
            final String messageId = UUID.randomUUID().toString();
            getAvroProducer().requestTrigger(messageId, csId, Integer.valueOf(1), TriggerKeywordsV1XEnum.METER.name()); // TODO Parameter
            return getAvroConsumer().receive(CSTriggerResponse.class, messageId, wait, 1);
        }
    }

    public List<CSChangeChargingCurrentResponse> doProfile(String csId, Integer maxCurrent) {
        synchronized (this) {
            final String messageId = UUID.randomUUID().toString();
            getAvroProducer().requestProfile(messageId, csId, Integer.valueOf(1), maxCurrent,  null);
            return getAvroConsumer().receive(CSChangeChargingCurrentResponse.class, messageId, wait, 1);
        }
    }

    public List<CSStartChargingResponse> doStart(String csId) {
        synchronized (this) {
            final String messageId = UUID.randomUUID().toString();
            getAvroProducer().requestStart(messageId, csId, Integer.valueOf(1), "freecharging", null, null);
            return getAvroConsumer().receive(CSStartChargingResponse.class, messageId, wait, 1);
        }
    }

    public List<CSStopChargingResponse> doStop(String csId) {
        synchronized (this) {
            final String messageId = UUID.randomUUID().toString();
            getAvroProducer().requestStop(messageId, csId, null);
            return getAvroConsumer().receive(CSStopChargingResponse.class, messageId, wait, 1);
        }
    }

    @Override
    public void run() {
        long waitStart = System.currentTimeMillis() - UPDATE_INTERVAL;

        while (run) {
            if (isWaitUpdateIntervalUp(waitStart)) {
                waitStart = System.currentTimeMillis();
                final List<CSStatusConnectedResponse> statusConnected =
                        getStatusConnected(MAX_WAIT_FOR_RESPONSE_MS, MAX_NUMBER_OF_OCPP_BACKENDS);
                if (!statusConnected.isEmpty()) {
                    if (AvroProsumer.ladestationMitLaufendenLadevorgangListener != null) {
                        AvroProsumer.ladestationMitLaufendenLadevorgangListener.notify(
                                berechneLadestationenMitLaufendenLadevorgang(statusConnected));
                    }

                    if (this.maxMobOCPPBackends != statusConnected.size()) {
                        log("Anzahl verbundener OCPP-Backends geaendert: von " + this.maxMobOCPPBackends + " zu " + statusConnected.size());
                        this.maxMobOCPPBackends = statusConnected.size();
                    } else {
                        log("Anzahl verbundener OCPP-Backends unveraendert " + this.maxMobOCPPBackends);
                    }
                }
            }
            try { Thread.sleep(100); } catch (InterruptedException e) {}
        }
//        log(this.getClass().getSimpleName() + " OCPP-Backend Counter Thread DONE.");
    }

    private List<LadestationMitLaufendenLadevorgang> berechneLadestationenMitLaufendenLadevorgang(List<CSStatusConnectedResponse> statusConnected) {
        List<LadestationMitLaufendenLadevorgang> result = new ArrayList<>();
        for (CSStatusConnectedResponse csStatusConnectedResponse : statusConnected) {
            if (csStatusConnectedResponse.getResponseInfo().getError() == null) {
                for (CSStatusConnected csStatusConnected : csStatusConnectedResponse.getCSStatusList()) {
                    ConnectorStatusEnum connectorStatus = ConnectorStatusEnum.valueOf(csStatusConnected.getCPConnectorStatus());
                    final ChargingStateEnum chargingState = ChargingStateEnum.valueOf(csStatusConnected.getCPChargingState());
                    if (isChargingTransactionActive(connectorStatus, chargingState)) {
                        result.add(new LadestationMitLaufendenLadevorgang() {
                            @Override
                            public StammdatenLadestation getStammdatenLadestation() {
                                return StammdatenAccessor.get().getStammdatenLadestationById(csStatusConnected.getId());
                            }

                            @Override
                            public Instant getZeitpunktLadevorgangStart() {
                                return Instant.now(); // TODO Ermitteln !?
                            }

                            @Override
                            public Optional<Instant> getZeitpunktLadekabelEingesteckt() {
                                return Optional.empty();
                            }

                            @Override
                            public Optional<Integer> getLadestromAmpereL1() {
                                return Optional.empty();
                            }

                            @Override
                            public Optional<Integer> getLadestromAmpereL2() {
                                return Optional.empty();
                            }

                            @Override
                            public Optional<Integer> getLadestromAmpereL3() {
                                return Optional.empty();
                            }
                        });
                    }
                }
            }
        }
        return result;
    }

    private boolean isChargingTransactionActive(ConnectorStatusEnum connectorStatus, ChargingStateEnum chargingState) {
        if (ConnectorStatusEnum.Occupied.equals(connectorStatus)) {
            if (ChargingStateEnum.Charging.equals(chargingState)) {
                return true;
            }
            if (ChargingStateEnum.SuspendedEV.equals(chargingState)) {
                return true;
            }
            if (ChargingStateEnum.SuspendedEVSE.equals(chargingState)) {
                return true;
            }
        }
        return false;
    }

    private boolean isWaitUpdateIntervalUp(long waitStart) {
        final long waited = System.currentTimeMillis() - waitStart;
        if (waited > UPDATE_INTERVAL) {
            return true;
        }
        return false;
    }

    private static void log(String message) {
        System.out.println(message);
    }
}
