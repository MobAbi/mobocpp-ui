package ch.mobility.mobocpp.kafka;

import ch.mobility.mobocpp.ui.ServerMain;
import ch.mobility.ocpp2mob.CSResponse;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

class AvroConsumer<T extends GenericRecord>
{
    private static final String RESPONSE_INFO = "ResponseInfo";

    private static AvroConsumer INSTANCE = null;

    public static AvroConsumer get() {
        if (INSTANCE == null) {
            INSTANCE = new AvroConsumer();
        }
        return INSTANCE;
    }

    private static Duration duration = Duration.ofMillis(100);
    final ConsumerThread consumerThread;

    private AvroConsumer() {
        consumerThread = new ConsumerThread();
        new Thread(consumerThread).start();
    }

    public void close() {
        consumerThread.stop();
    }

    private static class ConsumerThread<T extends GenericRecord> implements Runnable {

        private static final String topicName = "ocpp2mob";
        private Consumer<String, GenericRecord> consumer = null;
        private boolean run = true;
        private Class expectedClass = null;
        private String expectedMessageId = null;
        private List<T> receivedMessages = null;

        private Consumer<String, GenericRecord> createConsumer() {

            Properties props = new Properties();

            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, ServerMain.HOST + ":" + ServerMain.PORT_BOOTSTRAP);
            props.put(ConsumerConfig.GROUP_ID_CONFIG, "mobility-ui");
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);

            props.put("schema.registry.url", "http://" + ServerMain.HOST + ":" + ServerMain.PORT_SCHEMA_REGISTRY);
            props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);  //ensures records are properly converted

            Consumer<String, GenericRecord> consumer = new KafkaConsumer<String, GenericRecord>(props);
            consumer.subscribe(Arrays.asList(topicName));

            log("AvroConsumer subscribed to topic " + topicName);
            return consumer;
        }

        void stop() {
            run = false;
        }

        void receive(Class expectedClass, String expectedMessageId) {
//            log("Start to expect: " + expected);
            this.expectedClass = expectedClass;
            this.expectedMessageId = expectedMessageId;
            this.receivedMessages = new ArrayList<>();
        }

        List<T> getReceived() {
            return this.receivedMessages;
        }

        @Override
        public void run() {
            try {
                consumer = createConsumer();
                log("Consumer start...");
                while (run) {
                    try {
                        ConsumerRecords<String, GenericRecord> records = consumer.poll(duration);
                        for (ConsumerRecord<String, GenericRecord> record : records) {
                            log("record.value().getClass(): " + record.value().getClass());
                            if (this.expectedClass != null) {
                                if (record.value().getClass().isAssignableFrom(this.expectedClass)) {
                                    final T value = (T)record.value();
                                    if (hasMessageId(value, this.expectedMessageId)) {
                                        this.receivedMessages.add(value);
//                                    log("Match: " + this.expected);
                                    } else {
//                                      log("!!!!!!!!!!!!! MessageId passt nicht: " + this.expectedMessageId + " <> " + record.value());
                                    }
                                } else {
//                                    log("!!!!!!!!!!!!! Kein Match: " + this.expected + " <> " + record.value());
                                }
                            } else {
//                                log("Kein expected: " + record.value());
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Exception: " + e.getMessage());
                    }
                }
            } finally {
                consumer.close();
            }
            log("Consumer stop");
        }

        private boolean hasMessageId(T value, String expectedMessageId) {
            if (value == null) {
                throw new IllegalArgumentException("Parameter <value> must not be null");
            }
            if (expectedMessageId == null) {
                throw new IllegalArgumentException("Parameter <expectedMessageId> must not be null");
            }
            try {
                final Field responseInfoField = value.getClass().getDeclaredField(RESPONSE_INFO);
                responseInfoField.setAccessible(true);
                final CSResponse csResponse = (CSResponse)responseInfoField.get(value);
                if (expectedMessageId.equals(csResponse.getMessageId())) {
                    return true;
                }
            } catch (NoSuchFieldException e ) {
                throw new IllegalStateException("Missing field <" + RESPONSE_INFO + "> in Class <" + value.getClass().getName() + ">", e);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
            return false;
        }
    }

    public List<T> receive(Class expectedClass, String expectedMessageId, long wait, int maxMobOCPPBackends) {
        consumerThread.receive(expectedClass, expectedMessageId);
        log("Start receive, waiting " + wait + " ms...");
        final long start = System.currentTimeMillis();
        boolean doWait = true;
        while (doWait) {
            try { Thread.sleep(50); } catch (InterruptedException e) {}
            long waited = System.currentTimeMillis() - start;
            if (waited > wait) {
                log("Timeout: " + wait);
                doWait = false;
            }
            if (consumerThread.getReceived().size() >= maxMobOCPPBackends) {
                log("Expected number of responses received: " + maxMobOCPPBackends);
                doWait = false;
            }
        }
        List<T> received = consumerThread.getReceived();
        consumerThread.receive(null, null);
        log("receiving done: " + received);
        System.out.println("Empfangen: " + received);
        return received;
    }

    private static void log(String message) {
//        System.out.println(message);
    }
}
