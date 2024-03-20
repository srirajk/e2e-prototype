package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class KafkaProducerUtility {

    private static final Logger logger = LogManager.getLogger(KafkaProducerUtility.class);
    private String topic;
    private KafkaProducer<String, JsonNode> kafkaProducer;
    public KafkaProducerUtility(final String bootstrapServers, final String topic) {
        this.topic = topic;
        var props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.example.serializer.KafkaJsonSerializer");
        this.kafkaProducer = new KafkaProducer<>(props);
    }

    public void produceRecord(final String key, final JsonNode value, Map<String, byte[]> headers) {
        logger.debug("producing the key {} and data {}", key, value);
        var record = new ProducerRecord(topic, null, key, value, buildHeaders(headers));
        this.kafkaProducer.send(record);
    }

    public void shutdown() {
        this.kafkaProducer.flush();
        this.kafkaProducer.close();
    }

    private List<RecordHeader> buildHeaders(Map<String,byte[]> headers) {
        if (Objects.nonNull(headers)) {
            return headers.entrySet().parallelStream().map(entry -> new RecordHeader(entry.getKey(), entry.getValue())).collect(Collectors.toList());
        }
        return new ArrayList<RecordHeader>();
    }
}
