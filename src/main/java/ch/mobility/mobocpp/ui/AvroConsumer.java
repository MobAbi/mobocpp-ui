package ch.mobility.mobocpp.ui;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class AvroConsumer<T extends GenericRecord>
{
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
        private Class expected = null;
        private List<T> toFill = null;

        private Consumer<String, GenericRecord> createConsumer() {

            Properties props = new Properties();

            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
            props.put(ConsumerConfig.GROUP_ID_CONFIG, "mobility");
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);

            props.put("schema.registry.url", "http://192.168.1.48:8081");
            props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);  //ensures records are properly converted

            Consumer<String, GenericRecord> consumer = new KafkaConsumer<String, GenericRecord>(props);
            consumer.subscribe(Arrays.asList(topicName));

            log("AvroConsumer subscribed to topic " + topicName);
            return consumer;
        }

        void stop() {
            run = false;
        }

        void receive(Class expected) {
//            log("Start to expect: " + expected);
            this.expected = expected;
            this.toFill = new ArrayList<>();
        }

        List<T> getReceived() {
            return this.toFill;
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
                            if (this.expected != null) {
                                if (record.value().getClass().isAssignableFrom(this.expected)) {
                                    this.toFill.add((T) record.value());
//                                    log("Match: " + this.expected);
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
    }

    public List<T> receive(Class expected, long wait, int maxMobOCPPBackends) {
        consumerThread.receive(expected);
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
        consumerThread.receive(null);
        log("receiving done: " + received);
        System.out.println("Empfangen: " + received);
        return received;
    }

    private static void log(String message) {
//        System.out.println(message);
    }
}
