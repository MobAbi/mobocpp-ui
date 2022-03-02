package ch.mobility.mobocpp.ui;

import ch.mobility.mob2ocpp.*;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Instant;
import java.util.Properties;
import java.util.UUID;

public class AvroProducer {

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
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);

        // https://www.confluent.io/blog/put-several-event-types-kafka-topic/
        props.put("value.subject.name.strategy", "io.confluent.kafka.serializers.subject.RecordNameStrategy");

        props.put("schema.registry.url", "http://192.168.1.48:8081");

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
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    private static CSRequest request() {
        CSRequest csRequest = CSRequest.newBuilder()
                .setMessageId(UUID.randomUUID().toString())
                .setRequestCreatedAt(DateTimeHelper.format(Instant.now()))
                .build();
        return csRequest;
    }

    public void requestStatusConnected() {
        CSStatusConnectedRequest request = CSStatusConnectedRequest.newBuilder()
                .setRequestInfo(request())
                .build();
        send(request);
    }

    public void requestStatusForId(String id, Integer connectorId, Integer daysOfHistoryData) {
        CSStatusForIdRequest request = CSStatusForIdRequest.newBuilder()
                .setRequestInfo(request())
                .setId(id)
                .setConnectorId(connectorId)
                .setDaysOfHistoryData(daysOfHistoryData)
                .build();
        send(request);
    }

    private static GenericRecord requestStart(String id, Integer connectorId, String tagId, Integer maxCurrent) {
        CSStartChargingRequest request = CSStartChargingRequest.newBuilder()
                .setRequestInfo(request())
                .setId(id)
                .setConnectorId(connectorId)
                .setTagId(tagId)
                .setMaxCurrent(maxCurrent)
                .build();
        return request;
    }

    private static GenericRecord requestStop(String id, Integer connectorId) {
        CSStopChargingRequest request = CSStopChargingRequest.newBuilder()
                .setRequestInfo(request())
                .setId(id)
                .setConnectorId(connectorId)
                .build();
        return request;
    }

    private static GenericRecord requestReset(String id) {
        CSResetRequest request = CSResetRequest.newBuilder()
                .setRequestInfo(request())
                .setId(id)
                .build();
        return request;
    }

    private static GenericRecord requestUnlock(String id, Integer connectorId) {
        CSUnlockRequest request = CSUnlockRequest.newBuilder()
                .setRequestInfo(request())
                .setId(id)
                .setConnectorId(connectorId)
                .build();
        return request;
    }

    private static GenericRecord requestProfile(String id, Integer connectorId, Integer maxCurrent) {
        CSChangeChargingCurrentRequest request = CSChangeChargingCurrentRequest.newBuilder()
                .setRequestInfo(request())
                .setId(id)
                .setConnectorId(connectorId)
                .setMaxCurrent(maxCurrent)
                .build();
        return request;
    }

    private static GenericRecord requestTrigger(String id, Integer connectorId, String trigger) {
        CSTriggerRequest request = CSTriggerRequest.newBuilder()
                .setRequestInfo(request())
                .setId(id)
                .setConnectorId(connectorId)
                .setTriggertype(TriggerKeywordsV1XEnum.valueOf(trigger.toUpperCase()))
                .build();
        return request;
    }

    private static void log(String message) {
        System.out.println(message);
    }
}
