package ch.mobility.mobocpp.kafka;

import ch.mobility.mob2ocpp.TriggerKeywordsV1XEnum;
import ch.mobility.ocpp2mob.*;

import java.util.List;
import java.util.UUID;

public class AvroProsumer {

    private static AvroProsumer INSTANCE = null;

    public static synchronized AvroProsumer get() {
        if (INSTANCE == null) {
            INSTANCE = new AvroProsumer(3000, 1);
        }
        return INSTANCE;
    }

    final long wait;
    final int maxMobOCPPBackends;

    private AvroProducer getAvroProducer() {
        return AvroProducer.get();
    }

    private AvroConsumer getAvroConsumer() {
        return AvroConsumer.get();
    }

    private AvroProsumer(long wait, int maxMobOCPPBackends) {
        this.wait = wait;
        this.maxMobOCPPBackends = maxMobOCPPBackends;
    }

    public void close() {
        AvroProducer.get().close();
        AvroConsumer.get().close();
    }

    public List<CSStatusConnectedResponse> getStatusConnected() {
        synchronized (this) {
            final String messageId = UUID.randomUUID().toString();
            getAvroProducer().requestStatusConnected(messageId);
            return getAvroConsumer().receive(CSStatusConnectedResponse.class, messageId, wait, maxMobOCPPBackends);
        }
    }

    public List<CSStatusForIdResponse> getStatusForId(String csId, Integer connectorId, Integer daysOfHistoryData) {
        synchronized (this) {
            final String messageId = UUID.randomUUID().toString();
            getAvroProducer().requestStatusForId(messageId, csId, connectorId, daysOfHistoryData);
            return getAvroConsumer().receive(CSStatusForIdResponse.class, messageId, wait, maxMobOCPPBackends);
        }
    }

    public List<CSChangeChargingCurrentResponse> doReset(String csId) {
        synchronized (this) {
            final String messageId = UUID.randomUUID().toString();
            getAvroProducer().requestReset(messageId, csId);
            return getAvroConsumer().receive(CSChangeChargingCurrentResponse.class, messageId, wait, maxMobOCPPBackends);
        }
    }

    public List<CSUnlockResponse> doUnlock(String csId) {
        synchronized (this) {
            final String messageId = UUID.randomUUID().toString();
            getAvroProducer().requestUnlock(messageId, csId, 1); // TODO Parameter
            return getAvroConsumer().receive(CSUnlockResponse.class, messageId, wait, maxMobOCPPBackends);
        }
    }

    public List<CSTriggerResponse> doTriggerStatus(String csId) {
        synchronized (this) {
            final String messageId = UUID.randomUUID().toString();
            getAvroProducer().requestTrigger(messageId, csId, 1, TriggerKeywordsV1XEnum.STATUS.name()); // TODO Parameter
            return getAvroConsumer().receive(CSTriggerResponse.class, messageId, wait, maxMobOCPPBackends);
        }
    }
}
