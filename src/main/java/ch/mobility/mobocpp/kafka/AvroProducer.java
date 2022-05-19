package ch.mobility.mobocpp.kafka;

import ch.mobility.mob2ocpp.*;
import ch.mobility.mobocpp.ui.DateTimeHelper;
import ch.mobility.mobocpp.ui.ServerMain;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Instant;
import java.util.Properties;

class AvroProducer {

    private static AvroProducer INSTANCE = null;

    public static AvroProducer get() {
        if (INSTANCE == null) {
            INSTANCE = new AvroProducer();
        }
        return INSTANCE;
    }

    private final String topicName = "mob2ocpp";
    private final KafkaProducer producer;

    private AvroProducer() {
        System.out.println("AvroProducer Topic: " + topicName);

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, ServerMain.HOST + ":" + ServerMain.PORT_BOOTSTRAP);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);

        // https://www.confluent.io/blog/put-several-event-types-kafka-topic/
        props.put("value.subject.name.strategy", "io.confluent.kafka.serializers.subject.RecordNameStrategy");

        props.put("schema.registry.url", "http://" + ServerMain.HOST + ":" + ServerMain.PORT_SCHEMA_REGISTRY);

        producer = new KafkaProducer(props);
    }

    public void close() {
        producer.flush();
        producer.close();
    }

    private void send(GenericRecord genericRecord) {
        if (genericRecord != null) {
            ProducerRecord<String, GenericRecord> record = new ProducerRecord<>(topicName, genericRecord);
            try {
                producer.send(record);
                System.out.println("Gesendet: " + genericRecord.getClass().getSimpleName() + "=" + genericRecord);
            } catch (SerializationException e) {
                System.err.println("AvroProducer Error: " + (e.getCause() == null ? e.getMessage() : e.getCause().getMessage()));
                e.printStackTrace(System.err);
            }
        }
    }

    private static CSRequest request(String messageId) {
        CSRequest csRequest = CSRequest.newBuilder()
                .setMessageId(messageId)
                .setRequestCreatedAt(DateTimeHelper.format(Instant.now()))
                .build();
        return csRequest;
    }

    public void requestStatusConnected(String messageId) {
        CSStatusConnectedRequest request = CSStatusConnectedRequest.newBuilder()
                .setRequestInfo(request(messageId))
                .build();
        send(request);
    }

    public void requestStatusForId(String messageId, String id, Integer connectorId, Integer daysOfHistoryData) {
        CSStatusForIdRequest request = CSStatusForIdRequest.newBuilder()
                .setRequestInfo(request(messageId))
                .setId(id)
                .setConnectorId(connectorId)
                .setDaysOfHistoryData(daysOfHistoryData)
                .build();
        send(request);
    }

    public void requestStart(String messageId, String id, Integer connectorId, String tagId, Integer maxCurrent) {
        CSStartChargingRequest request = CSStartChargingRequest.newBuilder()
                .setRequestInfo(request(messageId))
                .setId(id)
                .setConnectorId(connectorId)
                .setTagId(tagId)
                .setMaxCurrent(maxCurrent)
                .build();
        send(request);
    }

    public void requestStop(String messageId, String id, Integer connectorId) {
        CSStopChargingRequest request = CSStopChargingRequest.newBuilder()
                .setRequestInfo(request(messageId))
                .setId(id)
                .setConnectorId(connectorId)
                .build();
        send(request);
    }

    public void requestReset(String messageId, String id) {
        CSResetRequest request = CSResetRequest.newBuilder()
                .setRequestInfo(request(messageId))
                .setId(id)
                .build();
        send(request);
    }

    public void requestUnlock(String messageId, String id, Integer connectorId) {
        CSUnlockRequest request = CSUnlockRequest.newBuilder()
                .setRequestInfo(request(messageId))
                .setId(id)
                .setConnectorId(connectorId)
                .build();
        send(request);
    }

    public void requestProfile(String messageId, String id, Integer connectorId, Integer maxCurrent) {
        CSChangeChargingCurrentRequest request = CSChangeChargingCurrentRequest.newBuilder()
                .setRequestInfo(request(messageId))
                .setId(id)
                .setConnectorId(connectorId)
                .setMaxCurrent(maxCurrent)
                .build();
        send(request);
    }

    public void requestTrigger(String messageId, String id, Integer connectorId, String trigger) {
        CSTriggerRequest request = CSTriggerRequest.newBuilder()
                .setRequestInfo(request(messageId))
                .setId(id)
                .setConnectorId(connectorId)
                .setTriggertype(TriggerKeywordsV1XEnum.valueOf(trigger.toUpperCase()))
                .build();
        send(request);
    }

    private static void log(String message) {
        System.out.println(message);
    }
}
