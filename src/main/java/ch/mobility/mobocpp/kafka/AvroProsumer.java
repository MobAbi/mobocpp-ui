package ch.mobility.mobocpp.kafka;

import ch.mobility.ocpp2mob.CSStatusConnectedResponse;
import ch.mobility.ocpp2mob.CSStatusForIdResponse;

import java.util.List;
import java.util.UUID;

public class AvroProsumer {

    private static AvroProsumer INSTANCE = null;

    public static AvroProsumer get() {
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
        final String messageId = UUID.randomUUID().toString();
        getAvroProducer().requestStatusConnected(messageId);
        return getAvroConsumer().receive(CSStatusConnectedResponse.class, messageId, wait, maxMobOCPPBackends);
    }

    public List<CSStatusForIdResponse> getStatusForId(String csId, Integer connectorId, Integer daysOfHistoryData) {
        final String messageId = UUID.randomUUID().toString();
        getAvroProducer().requestStatusForId(messageId, csId, connectorId, daysOfHistoryData);
        return getAvroConsumer().receive(CSStatusForIdResponse.class, messageId, wait, maxMobOCPPBackends);
    }
}
